public class Piece {
    int x;
    int y;
    String state; // "empty", "white", "black"
    String r_side;// "free", "block"
    String u_side;// "free", "block"
    String l_side;// "free", "block"
    String d_side;// "free", "block"
    boolean is_white_goal;
    boolean is_black_goal;
    boolean is_border_piece;
    
    public Piece(int x,
                 int y,
                 String state,
                 String r_side,
                 String u_side,
                 String l_side,
                 String d_side,
                 boolean is_white_goal,
                 boolean is_black_goal,
                 boolean is_border_piece) {

        this.x = x;
        this.y = y;
        this.state = state;
        this.r_side = r_side;
        this.u_side = u_side;
        this.l_side = l_side;
        this.d_side = d_side;
        this.is_white_goal = is_white_goal;
        this.is_black_goal = is_black_goal;
        this.is_border_piece = is_border_piece;
    }

    public String get_position(){
        return this.x + "," + this.y;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "x=" + x +
                ", y=" + y +
                ", state='" + state + '\'' +
                ", r_side='" + r_side + '\'' +
                ", u_side='" + u_side + '\'' +
                ", l_side='" + l_side + '\'' +
                ", d_side='" + d_side + '\'' +
                ", is_white_goal=" + is_white_goal +
                ", is_black_goal=" + is_black_goal +
                ", is_border_piece=" + is_border_piece +
                '}';
    }
}
