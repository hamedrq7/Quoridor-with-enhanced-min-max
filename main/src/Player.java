import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Player {
    int x;
    int y;
    String color;
    int walls_count; // number of walls that player can put, in the current turn, if equals 0, it can not put wall
    Board board;
    int moves_count; // ?
    public Queue<String> actions_logs = null;

    public Player(String  color, int x, int y, Board board) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.walls_count = 10;
        this.board = board;
        this.actions_logs = new LinkedList<String>();
        this.moves_count = 0;
    }

    public String get_position(){
        return this.x + "," + this.y;
    }

    public void move(int x , int y){
        this.board.get_piece(this.x, this.y).state = "empty";

        this.x = x;
        this.y = y;

        this.board.get_piece(this.x, this.y).state = this.color;
    }

    //if horizontal:
    //      [x,y]  [np1]
    //      [np2]  [np3]
    // x,y.d_side, np1.d_side, np2.u_side, np3.u_side = "block"
    // ??? why this: this.board.paired_block_pieces.put(piece, neighbor_piece1);

    //if vertical:
    //      [x,y]  [np2]
    //      [np1]  [np3]
    // x,y.r_side, np1.r_side, np2.l_side, np3.l_side = "block"
    // ??? why this: this.board.paired_block_pieces.put(piece, neighbor_piece1);
    public void put_wall(int x, int y, String orientation){
        this.walls_count -= 1;

        Piece piece = this.board.get_piece(x, y);
        if (orientation.equals("horizontal")){
            Piece neighbor_piece1 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece2 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.d_side = "block";
            neighbor_piece1.d_side = "block";
            neighbor_piece2.u_side = "block";
            neighbor_piece3.u_side = "block";
            this.board.paired_block_pieces.put(piece, neighbor_piece1);
        }
        else if (orientation.equals("vertical")){
            Piece neighbor_piece1 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece2 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.r_side = "block";
            neighbor_piece1.r_side = "block";
            neighbor_piece2.l_side = "block";
            neighbor_piece3.l_side = "block";
            this.board.paired_block_pieces.put(piece, neighbor_piece1);
        }
    }

    //command = "move#x#y" or "wall#x#y#vertical" or "wall#x#y#horizantal"

    //updates action_logs
    //takes the action using .move() or .put_wall()
    // if its an actual play, increases player.moves_count
    public void play(String command, boolean is_evaluating){

        //if (!is_evaluating){
            this.moves_count += 1;
        //}


        String[] split_cmd = command.split("#");

        if (split_cmd[0].equals("move")){
            int x = Integer.parseInt(split_cmd[1]);
            int y = Integer.parseInt(split_cmd[2]);
            this.actions_logs.add("move#" + this.x + "#" + this.y + "#" + x + "#" + y);
            this.move(x, y);
        }
        else {
            int x = Integer.parseInt(split_cmd[1]);
            int y = Integer.parseInt(split_cmd[2]);
            String orientation = split_cmd[3];
            this.actions_logs.add(command);
            this.put_wall(x, y, orientation);
        }
    }

    //undoes last action in action_log
    // if last action is move, move the player back
    // if last action is put_wall, removes the wall
    public void undo_last_action(){

        ////////////////////////
        this.moves_count--;

        String last_action  = ((LinkedList<String>) actions_logs).removeLast();
        String[] splitted_command = last_action.split("#");

        if (splitted_command[0].equals("wall")){ this.remove_wall(last_action); }
        else {
            int x = Integer.parseInt(splitted_command[1]);
            int y = Integer.parseInt(splitted_command[2]);
            this.move(x, y);
        }
    }

    //remove the wall that was put in the command input
    //increase the walls that player can put
    //just undoes the put wall basically
    public void remove_wall(String command){

        this.walls_count += 1;

        String [] splitted_command = command.split("#");
        int x = Integer.parseInt(splitted_command[1]);
        int y = Integer.parseInt(splitted_command[2]);
        String orientation = splitted_command[3];

        Piece piece = this.board.get_piece(x, y);
        if (orientation.equals("horizontal")){
            Piece neighbor_piece1 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece2 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.d_side = "free";
            neighbor_piece1.d_side = "free";
            neighbor_piece2.u_side = "free";
            neighbor_piece3.u_side = "free";
            this.board.paired_block_pieces.remove(piece, neighbor_piece1);
        }
        else if (orientation.equals("vertical")){
            Piece neighbor_piece1 = this.board.get_piece(x, (y + 1));
            Piece neighbor_piece2 = this.board.get_piece((x + 1), y);
            Piece neighbor_piece3 = this.board.get_piece((x + 1), (y + 1));
            piece.r_side = "free";
            neighbor_piece1.r_side = "free";
            neighbor_piece2.l_side = "free";
            neighbor_piece3.l_side = "free";
            this.board.paired_block_pieces.remove(piece, neighbor_piece1);
        }
    }

    public boolean is_winner(){
        String player_pos = this.get_position();
        int x = Integer.parseInt(player_pos.split(",")[0]);
        int y = Integer.parseInt(player_pos.split(",")[1]);
        Piece player_piece = this.board.get_piece(x, y);

        if (this.color.equals("white")){
            if (this.board.get_white_goal_pieces().contains(player_piece)){
                return true;
            }
        }

        if (this.color.equals("black")){
            if (this.board.get_black_goal_pieces().contains(player_piece)){
                return true;
            }
        }
        return false;
    }

    //gets the piece that we want to put the wall in + orientation,
    //checks if its possible to put the wall ( by using the .r_side and... of piece class)
    public boolean can_place_wall(Piece piece, String orientation){
        if (this.walls_count > 0){
            String pos = piece.get_position();
            int x = Integer.parseInt(pos.split(",")[0]);
            int y = Integer.parseInt(pos.split(",")[1]);
            if (!piece.is_border_piece){
                if (orientation.equals("horizontal")){
                    if (piece.d_side.equals("free") &&
                            this.board.get_piece((x + 1), y).d_side.equals("free")){
                        if (!this.board.paired_block_pieces.containsKey(piece) &&
                            !this.board.paired_block_pieces.containsValue(this.board.get_piece(x, (y + 1)))){
                                return true;
                        }
                    }
                }
                if (orientation.equals("vertical")){
                    if (piece.r_side.equals("free") &&
                            this.board.get_piece(x, (y + 1)).r_side.equals("free")){
                        if (!this.board.paired_block_pieces.containsKey(piece) &&
                                !this.board.paired_block_pieces.containsValue(this.board.get_piece((x + 1), y))){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    //only gets the opponent(we already have the player that we want to get the legal actions for
    //**** return Set<String> as the legal commands
    public Set<String> get_legal_actions(Player opponent){
        Piece player_piece = this.board.get_piece(this.x, this.y);
        Piece opponent_piece = this.board.get_piece(opponent.x, opponent.y);

        Set<String> legal_moves = new HashSet<String>();

        if (!player_piece.r_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x + 1) + "," + this.y)){
                legal_moves.add("move#" + (this.x + 1) + "#" + this.y);
            }
            else {
                if (opponent_piece.r_side.equals("free")){
                    legal_moves.add("move#" + (this.x + 2) + "#" + this.y);
                }
                else {
                    if (opponent_piece.u_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y - 1));
                    }
                    if (opponent_piece.d_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y + 1));
                    }
                }
            }
        }

        if (!player_piece.d_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x) + "," + (this.y + 1))){
                legal_moves.add("move#" + (this.x) + "#" + (this.y + 1));
            }
            else {
                if (opponent_piece.d_side.equals("free")){
                    legal_moves.add("move#" + (this.x) + "#" + (this.y + 2));
                }
                else {
                    if (opponent_piece.r_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y + 1));
                    }
                    if (opponent_piece.l_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y + 1));
                    }
                }
            }
        }

        if (!player_piece.l_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x - 1) + "," + (this.y))){
                legal_moves.add("move#" + (this.x - 1) + "#" + this.y);
            }
            else {
                if (opponent_piece.l_side.equals("free")){
                    legal_moves.add("move#" + (this.x - 2) + "#" + (this.y));
                }
                else {
                    if (opponent_piece.u_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y - 1));
                    }
                    if (opponent_piece.d_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y + 1));
                    }
                }
            }
        }

        if (!player_piece.u_side.equals("block")){
            if (!opponent_piece.get_position().equals((this.x) + "," + (this.y - 1))){
                legal_moves.add("move#" + (this.x) + "#" + (this.y - 1));
            }
            else {
                if (opponent_piece.u_side.equals("free")){
                    legal_moves.add("move#" + (this.x) + "#" + (this.y - 2));
                }
                else {
                    if (opponent_piece.l_side.equals("free")){
                        legal_moves.add("move#" + (this.x - 1) + "#" + (this.y - 1));
                    }
                    if (opponent_piece.r_side.equals("free")){
                        legal_moves.add("move#" + (this.x + 1) + "#" + (this.y - 1));
                    }
                }
            }
        }

        Set<String> orientation = new HashSet<String>();
        orientation.add("vertical");
        orientation.add("horizontal");
        for (int y = 0; y < this.board.ROWS_NUM; y++) {
            for (int x = 0; x < this.board.COLS_NUM; x++) {
                for (String or : orientation) {
                    if (this.can_place_wall(this.board.boardMap[y][x], or)){
                        String command = "wall#" + this.board.boardMap[y][x].x + "#" + this.board.boardMap[y][x].y + "#" + or;
                        this.put_wall(this.board.boardMap[y][x].x, this.board.boardMap[y][x].y, or);
                        if (this.board.is_reachable(opponent) && this.board.is_reachable(this)){
                            legal_moves.add(command);
                        }
                        this.remove_wall(command);
                    }
                }
            }
        }
        return legal_moves;
    }

}
