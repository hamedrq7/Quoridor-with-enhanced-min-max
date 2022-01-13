import java.util.*;

public class Board {
    // black starts from top (row 0)
    // white starts from down(row 8)

    int ROWS_NUM = 9;
    int COLS_NUM = 9;

    public int black_row;
    public int black_col;
    public int white_row;
    public int white_col;

    Set<Set> map = new HashSet<Set>(); // ?
    Piece[][] boardMap = new Piece[ROWS_NUM][COLS_NUM]; //
    HashMap<Piece, Piece> paired_block_pieces  = new HashMap<Piece, Piece>(); //

    public Board() {
        black_row = 0;
        black_col = COLS_NUM/2;
        white_row = ROWS_NUM-1;
        white_col = COLS_NUM/2;

        for (int y = 0; y < ROWS_NUM; y++) {
            Set<Piece> row = new HashSet<Piece>(); /// ?

            for (int x = 0; x < COLS_NUM; x++) {
                String state = "empty";
                String right_side = "free";
                String up_side = "free";
                String left_side = "free";
                String down_side = "free";
                boolean is_white_goal = false;
                boolean is_black_goal = false;
                boolean is_border_piece = false;

                if (y == 0) {
                    //-------------------------- is_border_piece = true ???????
                    is_white_goal = true;
                    up_side = "block";
                    if (x == black_col) {
                        state = "black";
                    }
                }
                else if (y == ROWS_NUM - 1) {
                    is_border_piece = true;
                    is_black_goal = true;
                    down_side = "block";

                    if (x == white_col) {
                        state = "white";
                    }
                }
                if (x == 0){
                    left_side = "block";
                }
                else if (x == COLS_NUM - 1){
                    is_border_piece = true;
                    right_side = "block";
                }

                boardMap[y][x] = new Piece(
                        x,
                        y,
                        state,
                        right_side,
                        up_side,
                        left_side,
                        down_side,
                        is_white_goal,
                        is_black_goal,
                        is_border_piece
                );
            }

        }

    }

    public Piece get_piece(int x, int y){
        x = Math.min(x, COLS_NUM - 1);
        y = Math.min(y, ROWS_NUM - 1);
        return this.boardMap[y][x];
    }


    public Set<Piece> get_white_goal_pieces(){
        Set<Piece> temp = new HashSet<Piece>();

        for (int x = 0; x < COLS_NUM; x++) {
            temp.add(this.boardMap[0][x]);
        }
        return temp;
    }


    public Set<Piece> get_black_goal_pieces(){
        Set<Piece> temp = new HashSet<Piece>();

        for (int x = 0; x < COLS_NUM; x++) {
            temp.add(this.boardMap[ROWS_NUM - 1][x]);
        }
        return temp;
    }

    //return all neighbors that are not in "block" side
    //ex: p1 p2 , if p1.r_side is "block", p1.get_piece_neighbors does not return p2
    public Set<Piece> get_piece_neighbors(Piece piece){
        Set<Piece> neighbors = new HashSet<Piece>();
        String temp = piece.get_position();
        int x = Integer.parseInt(temp.split(",")[0]);
        int y = Integer.parseInt(temp.split(",")[1]);

        if (x + 1 < COLS_NUM && !piece.r_side.equals("block")) neighbors.add(this.get_piece((x + 1), y));
        if (y + 1 < ROWS_NUM && !piece.d_side.equals("block")) neighbors.add(this.get_piece(x, (y + 1)));
        if (x - 1 >= 0 && !piece.l_side.equals("block")) neighbors.add(this.get_piece((x - 1), y));
        if (y - 1 >= 0 && !piece.u_side.equals("block")) neighbors.add(this.get_piece(x, (y - 1)));

        return neighbors;

    }

    // return true if player can reach its goal pieces (according to current pos and walls in board)
    // search every where it can to reach goal pieces
    public boolean is_reachable(Player player){
        Set<Player> players = new HashSet<Player>();
        Set<Piece> destination = new HashSet<Piece>();

        Queue<Piece> queue = new LinkedList<Piece>();
        HashMap<Piece, Boolean> visited = new HashMap<Piece, Boolean>();

        if (player.color.equals("white")) destination = this.get_white_goal_pieces();
        else destination = this.get_black_goal_pieces();

        for (int y = 0; y < ROWS_NUM; y++) {
            for (int x = 0; x < COLS_NUM; x++) {
                visited.put(this.boardMap[y][x], false);
            }
        }

        String player_pos = player.get_position();
        int x = Integer.parseInt(player_pos.split(",")[0]);
        int y = Integer.parseInt(player_pos.split(",")[1]);

        Piece player_piece = this.get_piece(x, y);

        queue.add(player_piece);
        visited.put(player_piece, true);
        boolean can_be_reached = false;

        while (queue.size() != 0){
            Piece piece = ((LinkedList<Piece>) queue).removeFirst();

            if (destination.contains(piece)){
                can_be_reached = true;
                break;
            }

            Set<Piece> temp = new HashSet<Piece>();
            temp = this.get_piece_neighbors(piece);

            for (Piece p : temp) {
                if (!visited.get(p)){
                    queue.add(p);
                    visited.put(p, true);
                }
            }
        }
        return can_be_reached;
    }

    public String toString() {

        String toString = "";

        String VERTICAL_WALL = "\u2503";
        String HORIZONTAL_WALL = "\u2501";
        String WHITE_PLAYER = "\u265F";
        String BLACK_PLAYER = "\u2659";
        String SQUARE = "\u00B7";
        String HALFSPACE = "\u2009";
        String THREEPEREMSPACE = "\u2004";

        for (int y = 0; y < this.ROWS_NUM; y++) {
            for (int x = 0; x < this.COLS_NUM; x++) {
                if (x == 0) {
                    toString += VERTICAL_WALL + " ";
                }

                Piece piece = this.get_piece(x, y);

                if (piece.state.equals("empty")) {
                    toString += SQUARE + " ";
                }
                else if (piece.state.equals("white")) {
                    toString += WHITE_PLAYER + " ";
                }
                else {
                    toString += BLACK_PLAYER + HALFSPACE;
                }

                if (piece.r_side.equals("block")) {
                    toString += VERTICAL_WALL + " ";
                }
                else {
                    toString += " " + " ";
                }
            }

            toString += "\n";

            if (y != this.ROWS_NUM - 1){
                toString += VERTICAL_WALL + " ";

                for (int x = 0; x < this.COLS_NUM; x++) {
                    Piece piece = this.get_piece(x, y);

                    if (piece.d_side.equals("block")){
                        if (this.get_piece((x + 1), y).d_side.equals("block")){
                            toString += HORIZONTAL_WALL + HORIZONTAL_WALL;
                        }
                        else {
                            toString += HORIZONTAL_WALL + " ";
                        }
                    }
                    else {
                        toString += " " + " ";
                    }
                    if (piece.r_side.equals("block") &&
                            this.get_piece(x, (y + 1)).r_side.equals("block")){
                        toString += VERTICAL_WALL + " ";
                    }
                    else if (piece.d_side.equals("block") &&
                            this.get_piece((x + 1), y).d_side.equals("block")){
                        toString += HORIZONTAL_WALL + HORIZONTAL_WALL;
                    }
                    else {
                        toString += " " + " ";
                    }
                }

                toString += "\n";
            }
        }
        return toString;
    }

    public void print_map(){

        String toString = "";

        String VERTICAL_WALL = "\u2503";
        String HORIZONTAL_WALL = "\u2501";
        String WHITE_PLAYER = "\u265F";
        String BLACK_PLAYER = "\u2659";
        String SQUARE = "\u00B7";
        String HALFSPACE = "\u2009";
        String THREEPEREMSPACE = "\u2004";

        for (int y = 0; y < this.ROWS_NUM; y++) {
            for (int x = 0; x < this.COLS_NUM; x++) {
                if (x == 0) {
                    System.out.print(VERTICAL_WALL + " ");
                    toString += VERTICAL_WALL + " ";
                }

                Piece piece = this.get_piece(x, y);

                if (piece.state.equals("empty")) {
                    System.out.print(SQUARE + " ");
                    toString += SQUARE + " ";
                }
                else if (piece.state.equals("white")) {
                    System.out.print(WHITE_PLAYER+ " ");
                    toString += WHITE_PLAYER + " ";
                }
                else {
                    System.out.print(BLACK_PLAYER + HALFSPACE);
                    toString += BLACK_PLAYER + HALFSPACE;
                }

                if (piece.r_side.equals("block")) {
                    System.out.print(VERTICAL_WALL + " ");
                    toString += VERTICAL_WALL + " ";
                }
                else {
                    System.out.print(" " + " ");
                    toString += " " + " ";
                }
            }

            System.out.println();
            toString += "\n";

            if (y != this.ROWS_NUM - 1){
                System.out.print(VERTICAL_WALL + " ");
                toString += VERTICAL_WALL + " ";

                for (int x = 0; x < this.COLS_NUM; x++) {
                    Piece piece = this.get_piece(x, y);

                    if (piece.d_side.equals("block")){
                        if (this.get_piece((x + 1), y).d_side.equals("block")){
                            System.out.print(HORIZONTAL_WALL + HORIZONTAL_WALL);
                            toString += HORIZONTAL_WALL + HORIZONTAL_WALL;
                        }
                        else {
                            System.out.print(HORIZONTAL_WALL + " ");
                            toString += HORIZONTAL_WALL + " ";
                        }
                    }
                    else {
                        System.out.print(" " + " ");
                        toString += " " + " ";
                    }
                    if (piece.r_side.equals("block") &&
                            this.get_piece(x, (y + 1)).r_side.equals("block")){
                        System.out.print(VERTICAL_WALL + " ");
                        toString += VERTICAL_WALL + " ";
                    }
                    else if (piece.d_side.equals("block") &&
                            this.get_piece((x + 1), y).d_side.equals("block")){
                        System.out.print(HORIZONTAL_WALL + HORIZONTAL_WALL);
                        toString += HORIZONTAL_WALL + HORIZONTAL_WALL;
                    }
                    else {
                        System.out.print(" " + " ");
                        toString += " " + " ";
                    }
                }

                System.out.println();
                toString += "\n";
            }
        }
    }

}
