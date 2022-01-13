import java.util.*;
import java.util.concurrent.TimeUnit;
public class Main {

    public static void main(String[] args) throws InterruptedException{

        int whitePlayerDepth = 5;
        int blackPlayerDepth = 2;
        long startTime = System.nanoTime();

        Board board = new Board();

        MiniMaxPlayer white_player = new MiniMaxPlayer("white", board.white_col, board.white_row, board, whitePlayerDepth);
        MiniMaxPlayer black_player = new MiniMaxPlayer("black", board.black_col, board.black_row, board, blackPlayerDepth);
        Set<String> move = new HashSet<String >();

        int walls_count = 0;

        while (true){
            String white_action = white_player.miniMaxFP(black_player);

            white_player.play(white_action, false);
            board.print_map();
            System.out.println(
                    "white: " + white_action + ", evaluation: " + white_player.heuristic(black_player) +
                            ", left walls: " + white_player.walls_count + ", moves_count so far: " + white_player.moves_count
            );

            if (white_player.is_winner()){
                System.out.println("White player just won with " + white_player.moves_count + " moves!");
                break;
            }
            if (white_action.split("#")[0].equals("wall")) walls_count += 1;

            System.out.println("(TT) white states: " + white_player.state_count_with_pruning_and_tt);
            System.out.println("(PR) white states: " + white_player.state_count_with_pruning);
            System.out.println("(NORMAL) white states: " + white_player.state_count_without_pruning);
            System.out.println("(FP) white states: " + white_player.state_count_FP);

            TimeUnit.SECONDS.sleep(0);

            String black_action = black_player.miniMaxTT(white_player);

            black_player.play(black_action, false);
            board.print_map();
            System.out.println(
                    "black: " + black_action + ", evaluation: " + black_player.heuristic(white_player) +
                            ", left walls: " + black_player.walls_count + ", moves_count so far: " + black_player.moves_count
            );

            if (black_player.is_winner()){
                System.out.println("Black player just won with " + black_player.moves_count + " moves!");
                break;
            }

            if (black_action.split("#")[0].equals("wall")) walls_count += 1;

            System.out.println("(TT) black player states: " + black_player.state_count_with_pruning_and_tt);
            System.out.println("(PR) black player states: " + black_player.state_count_with_pruning);
            System.out.println("(NORMAL) black player states: " + black_player.state_count_without_pruning);
            System.out.println("(FP) black player states: " + black_player.state_count_FP);

            TimeUnit.SECONDS.sleep(0);

            //if(black_player.moves_count > 40) break;
        }

        System.out.println("states with pruning and tt (white): " + white_player.state_count_with_pruning_and_tt);
        System.out.println("states with pruning and tt (black): " + black_player.state_count_with_pruning_and_tt);

        System.out.println("redundant states: " + white_player.redundant_states);
        //System.out.println("black player tt: " + black_player.transitionTable);

        System.out.println("white states with pruning only: " + white_player.state_count_with_pruning);

        long endTime = System.nanoTime();

        // get the difference between the two nano time values
        long timeElapsed = endTime - startTime;

        System.out.println("Execution time in nanoseconds: " + timeElapsed);
        System.out.println("Execution time in milliseconds: " + timeElapsed / 1000000);

        System.out.println("white player time in BFS: " + white_player.timeElapsedInBFS/ 1000000);
        System.out.println("white player time in MinDis: " + white_player.timeElapsedInMinDis/ 1000000);
        System.out.println("black player time in BFS: " + black_player.timeElapsedInBFS/ 1000000);
        System.out.println("black player time in MinDis: " + black_player.timeElapsedInMinDis/ 1000000);

    }
}
