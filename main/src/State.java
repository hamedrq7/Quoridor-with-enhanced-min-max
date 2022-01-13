// this object is used only in transition table for miniMax

//this state object is from heuristic point of view
// suppose we have 2 states, both have the same board,
// but one is at turn x and one is at turn y (white player player x turn to get to state 1 and y turns to get to state 2)
// the value that the heuristic function gives is the same for both states, SINCE parameters that heuristic value is based on are same in both states

// so if heuristic function only takes self distance and opponent distance, this state class can have only 2 attributes , self distance and opponent distance
// it does not matter for the heuristic function if

import java.util.Objects;

public class State {
    private String board;
    private int self_remaining_walls;
    private int opponent_remaining_walls;

    private int hashCode;

    public State(Board board, Player self, Player opponent) {
        this.board = board.toString();
        this.self_remaining_walls = self.walls_count;
        this.opponent_remaining_walls = opponent.walls_count;
        this.hashCode = Objects.hash(board, self_remaining_walls, opponent_remaining_walls);
    }

    public boolean isEqual(Board board, Player self, Player opponent) {
        if(this.board.equals(board.toString()) && this.self_remaining_walls==self.walls_count
                && this.opponent_remaining_walls==opponent.walls_count) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        State s2 = (State) o;

        if(this.board.equals(s2.getBoard()) && this.self_remaining_walls==s2.getSelf_remaining_walls()
            && this.opponent_remaining_walls==s2.getOpponent_remaining_walls()) {
            return true;
        }
        else return false;

    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "\n" + this.board + "\n" + self_remaining_walls + ", " + opponent_remaining_walls + "\n";
    }

    public String getBoard() {
        return board;
    }

    public int getSelf_remaining_walls() {
        return self_remaining_walls;
    }

    public int getOpponent_remaining_walls() {
        return opponent_remaining_walls;
    }
}
