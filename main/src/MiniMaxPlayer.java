import java.util.*;

public class MiniMaxPlayer extends Player{

    long timeElapsedInBFS = 0;
    long timeElapsedInMinDis = 0;
    public final int cutOffDepth; // cut it off if depth is  greater or equal (>=) than this, also depth starts from 0 (root.depth = 0)
    private double MAX_DEPTH = 2.0;
    private double INFINITY = 99999.0;

    public int state_count_FP = 0;
    public int state_count_without_pruning = 0;
    public int state_count_with_pruning = 0;
    public int state_count_with_pruning_and_tt = 0;
    public int redundant_states = 0;

    private int NumberOfBestMoves = 10;

    //
    public Hashtable<String, Double> tt = new Hashtable<>();

    public MiniMaxPlayer(String color, int x, int y, Board board, int cutOffDepth) {
        super(color, x, y, board);
        this.cutOffDepth = cutOffDepth;

    }

    // returns lowest distance of this.player and opponent from the goal
    // expands current position using bfs, reaches all destination points,
    // updates minimum distance from destination using distance of all destination points
    public String bfs(MiniMaxPlayer opponent){
        double self_distance = 0.0;         //
        double opponent_distance = 0.0;     //
        Set<MiniMaxPlayer> players = new HashSet<MiniMaxPlayer>();
        players.add(this);
        players.add(opponent);

        Set<Piece> destination = new HashSet<Piece>();
        for (MiniMaxPlayer player : players) {
            if (player.color.equals("white")) destination = this.board.get_white_goal_pieces();
            else destination = this.board.get_black_goal_pieces();

            Queue<Piece> queue = new LinkedList<Piece>();
            HashMap<Piece, Boolean> visited = new HashMap<Piece, Boolean>();
            HashMap<Piece, Double> distances = new HashMap<Piece, Double>();

            for (int y = 0; y < this.board.ROWS_NUM; y++) {
                for (int x = 0; x < this.board.COLS_NUM; x++) {
                    visited.put(this.board.boardMap[y][x], false);
                    distances.put(this.board.boardMap[y][x], this.INFINITY);
                }
            }

            String player_pos = player.get_position();
            int x = Integer.parseInt(player_pos.split(",")[0]);
            int y = Integer.parseInt(player_pos.split(",")[1]);
            Piece player_piece = this.board.get_piece(x , y);

            queue.add(player_piece);
            visited.put(player_piece, true);
            distances.put(player_piece, 0.0);

            while (queue.size() != 0){
                Piece piece = ((LinkedList<Piece>) queue).removeFirst();

                Set<Piece> piece_temp = new HashSet<Piece>();

                piece_temp = this.board.get_piece_neighbors(piece);
                for (Piece p : piece_temp) {
                    if (!visited.get(p)){
                        double t = distances.get(piece) + 1;

                        distances.put(p, t);
                        visited.put(p, true);
                        queue.add(p);
                    }
                }

                double min_distance = this.INFINITY;

                for (Piece p_key : distances.keySet()) {
                    if (destination.contains(p_key)){
                        if (distances.get(p_key) < min_distance){
                            min_distance = distances.get(p_key);
                        }
                    }
                }

                if (player == this) self_distance = min_distance;
                else opponent_distance = min_distance;
            }
        }

        return self_distance + "," + opponent_distance;
    }

    //Next Row actually 
    public double distanceToNextColumn(MiniMaxPlayer opponent){
        int currRow, nextRow;
        if(this.color == "black") {
            currRow = this.y;
            nextRow = currRow+1;
        }
        else {
            currRow = this.y;
            nextRow = currRow-1;
        }

        Queue<Piece> q = new LinkedList<Piece>();
        HashMap<Piece, Boolean> v = new HashMap<>();
        HashMap<Piece, Double> d = new HashMap<Piece, Double>();

        for (int y = 0; y < this.board.ROWS_NUM; y++) {
            for (int x = 0; x < this.board.COLS_NUM; x++) {
                v.put(this.board.boardMap[y][x], false);
                d.put(this.board.boardMap[y][x], this.INFINITY);
            }
        }

        String player_pos = this.get_position();
        int x = Integer.parseInt(player_pos.split(",")[0]);
        int y = Integer.parseInt(player_pos.split(",")[1]);
        Piece player_piece = this.board.get_piece(x , y);

        q.add(player_piece);
        v.put(player_piece, true);
        d.put(player_piece, 0.0);

        while (q.size() != 0) {
            Piece piece = ((LinkedList<Piece>) q).removeFirst();
            Set<Piece> piece_temp = new HashSet<Piece>();
            piece_temp = this.board.get_piece_neighbors(piece);

            for (Piece p : piece_temp) {
                if (!v.get(p)){
                    double t = d.get(piece) + 1;

                    if(p.y == nextRow) {
                        return t;
                    }

                    d.put(p, t);
                    v.put(p, true);
                    q.add(p);
                }
            }

        }
        if(this.is_winner()) {
            return 1000;
        }
        else {
            System.out.println("error 63");
            return -1;
        }

    }

    //heuristic functions
    public double evaluate(MiniMaxPlayer opponent){
        if(this.is_winner()) return 10000;

        String distances = this.bfs(opponent);


        double self_distance = Double.parseDouble(distances.split(",")[0]);
        double opponent_distance  = Double.parseDouble(distances.split(",")[1]);

        double total_score = (5 * opponent_distance - self_distance) * (
                1 + this.walls_count / 2.0
                );

        return total_score;
    }
    public double zeroSumEvaluate(MiniMaxPlayer opponent) {
        if(this.is_winner()) return 1;
        else if (opponent.is_winner()) return -1;
        else return 0;
    }
    public double heuristic(MiniMaxPlayer opponent) {
        if(this.is_winner()) {
            System.out.println("in " + this.color + " reached win");
            return 10000;
        }
        //if(opponent.is_winner()) return -10000;

        long startTime = System.nanoTime();

        String distances = this.bfs(opponent);

        long endTime = System.nanoTime();

        // get the difference between the two nano time valuess
        timeElapsedInBFS += endTime - startTime;

        double self_distance = Double.parseDouble(distances.split(",")[0]);
        double opponent_distance  = Double.parseDouble(distances.split(",")[1]);

        double total_score = (4 * opponent_distance - self_distance) * (
                1 + this.walls_count / 2.0
        );

        return total_score;
    }
    public double topMoveEvaluation(MiniMaxPlayer opponent) {
        int f1, opponentPositionFeature, f2;
        if(this.color=="black") {
            f1 = this.y;
            opponentPositionFeature  = 8 - opponent.y;
        }
        else {
            f1 = 8 - this.y;
            opponentPositionFeature = opponent.y;
        }
        f2 = f1 - opponentPositionFeature;

        long startT = System.nanoTime();
        int f3 = (int) this.distanceToNextColumn(opponent);
        int f4 = (int) opponent.distanceToNextColumn(opponent);
        long endT = System.nanoTime();

        timeElapsedInMinDis += endT - startT;

        //System.out.println(f1 + "| " + this.y + ", " + f2 + "| " + opponent.y);
        return f1 * 10 + f2 * 6.1 + (1/f3) * 14.45 + (f4) * 6.52;
    }

    // returns the best action ("best" based of heuristic function)
    // depth = 1
    public String get_best_action(MiniMaxPlayer opponent){
        double best_action_value = - (this.INFINITY);
        String best_action = "";
        Set<String> legal_move = new HashSet<String>();
        legal_move = this.get_legal_actions(opponent);
        for (String action : legal_move) {
            this.play(action, true);
            if (this.is_winner()){
                this.undo_last_action();
                return action;
            }

            double action_value = this.evaluate(opponent);
            if (action_value > best_action_value){
                best_action_value = action_value;
                best_action = action;
            }

            // ***********
            this.undo_last_action();
        }

        return best_action;
    }

    //Forward pruning:
    // Alpha–beta pruning prunes branches of the tree that can have no effect on the final evaluation,
    // but forward pruning prunes moves that appear to be poor moves, but might possibly be good Forward pruning
    // ones. Thus, the strategy saves computation time at the risk of making an error. In Shannon’s
    // terms, this is a Type B strategy.

    //Beam Search:
    // One approach to forward pruning is beam search: on each ply, consider
    // only a “beam” of the n best moves (according to the evaluation function) rather than considering
    // all possible moves. Unfortunately, this approach is rather dangerous because there is no
    // guarantee that the best move will not be pruned away.

    public ArrayList<Pair> getTopMoves(Set<String> childActions, MiniMaxPlayer self, MiniMaxPlayer opponent) {
        ArrayList<Pair> actionsEval = new ArrayList<>();

        for (String childAction : childActions) {
            self.play(childAction, true);
            actionsEval.add(new Pair(childAction, heuristic(opponent)));
            self.undo_last_action();
        }

        Collections.sort(actionsEval, new Comparator<Pair>() {
            @Override
            public int compare(Pair a1, Pair a2)  {
                if(a1.eval < a2.eval) return 1;
                else if(a1.eval > a2.eval) return -1;
                else return 0;
            }
        });

        //System.out.println(actionsEval);
        return actionsEval;
    }
    
    //minimax with forward pruning
    public String miniMaxFP(MiniMaxPlayer opponent) {

        Set<String> childActions = this.get_legal_actions(opponent);
        String bestChildAction = "";
        double bestChildValue = - INFINITY;

        System.out.println(childActions.size());

        Double alpha = - INFINITY;
        Double beta  =   INFINITY;

        int depth = 1;

        ArrayList<Pair> actionsEval = getTopMoves(childActions, this, opponent);

        for(int i = 0; i < Math.min(NumberOfBestMoves, actionsEval.size()); i++) {
            this.play(actionsEval.get(i).action, true);
            state_count_FP++;
            double currChildValue = 0;

            currChildValue = minValueFP(opponent, alpha, beta, depth);

            this.undo_last_action();

            if(currChildValue > bestChildValue) {
                bestChildValue = currChildValue;
                bestChildAction = actionsEval.get(i).action;
            }

            alpha = Math.max(alpha, currChildValue);

        }

        return bestChildAction;
    }
    public double maxValueFP(MiniMaxPlayer opponent, Double alpha, Double beta, int depth) {

        if(cutoffSearch(opponent, depth, true)) {
            return heuristic(opponent);
        }

        // now we go further
        Set<String> childActions = this.get_legal_actions(opponent);
        //String bestChildAction = "";
        double maxChildValue = - INFINITY;

        String currentMaxNodeState = stateToString(opponent, depth, "max");

        if(tt.containsKey(currentMaxNodeState)) {
            redundant_states++;
            return tt.get(currentMaxNodeState);
        }
        else {

            ArrayList<Pair> actionsEval = getTopMoves(childActions, this, opponent);

            for(int i = 0; i < Math.min(NumberOfBestMoves, actionsEval.size()); i++) {
                this.play(actionsEval.get(i).action, true);
                state_count_FP++;
                double currChildValue = 0;

                currChildValue = minValueFP(opponent, alpha, beta, depth+1);

                this.undo_last_action();

                if(currChildValue > maxChildValue) {
                    maxChildValue = currChildValue;
                }

                if(maxChildValue >= beta) {
                    //prune
                    return maxChildValue;
                }

                alpha = Math.max(alpha, maxChildValue);

            }
            tt.put(currentMaxNodeState, maxChildValue);
        }

        return maxChildValue;
    }
    public double minValueFP(MiniMaxPlayer opponent, Double alpha, Double beta, int depth) {
        //opponent plays this

        if(cutoffSearch(opponent, depth, false)) {
            return heuristic(opponent);
        }

        Set<String> childActions = opponent.get_legal_actions(this);
        //String worstChildAction = "";
        double minChildValue = + INFINITY;

        String currentMinNodeState = stateToString(opponent, depth, "min");

        if(tt.containsKey(currentMinNodeState)) {
            redundant_states++;
            return tt.get(currentMinNodeState);
        }
        else {

            ArrayList<Pair> actionsEval = getTopMoves(childActions, opponent, this);

            for(int i = 0; i < Math.min(NumberOfBestMoves, actionsEval.size()); i++) {
                opponent.play(actionsEval.get(i).action, true);
                state_count_FP++;
                double currChildValue = 0;

                currChildValue = maxValueFP(opponent, alpha, beta, depth+1);

                opponent.undo_last_action();

                if(currChildValue < minChildValue) {
                    minChildValue = currChildValue;
                }

                if(minChildValue <= alpha) {
                    //prune
                    return minChildValue;
                }

                beta = Math.min(beta, minChildValue);

            }
            tt.put(currentMinNodeState, minChildValue);
        }

        return minChildValue;
    }


    // output string : board#self_walls#opponent_walls#
    public String stateToString(MiniMaxPlayer opponent, int depth, String playerMinOrMax) {
        StringBuffer sb = new StringBuffer();
        //sb.append(playerMinOrMax);
        //sb.append('#');
        sb.append(this.board.toString());
        sb.append('#');
        sb.append(this.walls_count);
        sb.append('#');
        sb.append(opponent.walls_count);
        sb.append('#');
        sb.append(depth);

        return sb.toString();
    }
    
    //minimax Transposition table
    public String miniMaxTT(MiniMaxPlayer opponent) {

        Set<String> childActions = this.get_legal_actions(opponent);
        String bestChildAction = "";
        double bestChildValue = - INFINITY;

        System.out.println(childActions.size());

        Double alpha = - INFINITY;
        Double beta  =   INFINITY;

        int depth = 1;

        for (String childAction : childActions) {
            this.play(childAction, true);
            state_count_with_pruning_and_tt++;
            double currChildValue = 0;

            currChildValue = minValueTT(opponent, alpha, beta, depth);

            this.undo_last_action();

            if(currChildValue > bestChildValue) {
                bestChildValue = currChildValue;
                bestChildAction = childAction;
            }

            alpha = Math.max(alpha, currChildValue);

            //System.out.println("miniMax: " + currChildValue);
        }

        return bestChildAction;
    }
    public double maxValueTT(MiniMaxPlayer opponent, Double alpha, Double beta, int depth) {

        if(cutoffSearch(opponent, depth, true)) {
            return heuristic(opponent);
        }

        // now we go further
        Set<String> childActions = this.get_legal_actions(opponent);
        //String bestChildAction = "";
        double maxChildValue = - INFINITY;

        String currentMaxNodeSate = stateToString(opponent, depth, "max");

        if(tt.containsKey(currentMaxNodeSate)) {
            redundant_states++;
            return tt.get(currentMaxNodeSate);
        }
        else {

            for (String childAction : childActions) {

                this.play(childAction, true);
                state_count_with_pruning_and_tt++;
                double currActionValue = 0;
                currActionValue = minValueTT(opponent, alpha, beta, depth+1);

                this.undo_last_action();

                if(currActionValue > maxChildValue) {
                    //bestChildAction = childAction;
                    maxChildValue = currActionValue;
                }

                if(maxChildValue >= beta) {
                    //prune
                    return maxChildValue;
                }

                alpha = Math.max(alpha, maxChildValue);
                //System.out.println("maxValue: " + currActionValue);
            }

            tt.put(currentMaxNodeSate, maxChildValue);
        }


        return maxChildValue;
    }
    public double minValueTT(MiniMaxPlayer opponent, Double alpha, Double beta, int depth) {
        //opponent plays this

        if(cutoffSearch(opponent, depth, false)) {
            return heuristic(opponent);
        }

        Set<String> childActions = opponent.get_legal_actions(this);
        //String worstChildAction = "";
        double minChildValue = + INFINITY;

        String currentMinNodeState = stateToString(opponent, depth, "min");

        if(tt.containsKey(currentMinNodeState)) {
            redundant_states++;
            return tt.get(currentMinNodeState);
        }
        else {

            for (String childAction: childActions) {

                opponent.play(childAction, true);
                state_count_with_pruning_and_tt++;
                double currActionValue = 0;

                currActionValue = maxValueTT(opponent, alpha, beta, depth+1);

                opponent.undo_last_action();

                if(currActionValue < minChildValue) {
                    //worstChildAction = childAction;
                    minChildValue = currActionValue;
                }
                //System.out.println("minValue: " + currActionValue);

                if(minChildValue <= alpha) {
                    //prune
                    return minChildValue;
                }

                beta = Math.min(beta, minChildValue);
            }

            tt.put(currentMinNodeState, minChildValue);
        }

        return minChildValue;
    }

    // play it,  then pass it
    public String miniMaxPruning(MiniMaxPlayer opponent) {

        Set<String> childActions = this.get_legal_actions(opponent);
        String bestChildAction = "";
        double bestChildValue = - INFINITY;

        System.out.println(childActions.size());

        Double alpha = - INFINITY;
        Double beta  =   INFINITY;

        for (String childAction : childActions) {

            this.play(childAction, true);
            state_count_with_pruning++;
            double currChildValue = minValuePruning(opponent, alpha, beta, 1);
            this.undo_last_action();

            if(currChildValue > bestChildValue) {
                bestChildValue = currChildValue;
                bestChildAction = childAction;
            }

            alpha = Math.max(alpha, currChildValue);

            //System.out.println("miniMax: " + currChildValue);
        }

        return bestChildAction;
    }
    public double maxValuePruning(MiniMaxPlayer opponent, Double alpha, Double beta, int depth) {

        if(cutoffSearch(opponent, depth, true)) {
            return heuristic(opponent);
        }

        // now we go further
        Set<String> childActions = this.get_legal_actions(opponent);
        //String bestChildAction = "";
        double maxChildValue = - INFINITY;

        for (String childAction : childActions) {

            this.play(childAction, true);
            state_count_with_pruning++;
            double currActionValue = minValuePruning(opponent, alpha, beta, depth+1);
            this.undo_last_action();

            if(currActionValue > maxChildValue) {
                //bestChildAction = childAction;
                maxChildValue = currActionValue;
            }

            if(maxChildValue >= beta) {
                //prune
                return maxChildValue;
            }

            alpha = Math.max(alpha, maxChildValue);
            //System.out.println("maxValue: " + currActionValue);
        }

        return maxChildValue;
    }
    public double minValuePruning(MiniMaxPlayer opponent, Double alpha, Double beta, int depth) {
        //opponent plays this

        if(cutoffSearch(opponent, depth, false)) {
            return heuristic(opponent);
        }

        Set<String> childActions = opponent.get_legal_actions(this);
        //String worstChildAction = "";
        double minChildValue = + INFINITY;

        for (String childAction: childActions) {

            opponent.play(childAction, true);
            state_count_with_pruning++;
            double currActionValue = maxValuePruning(opponent, alpha, beta, depth+1);
            opponent.undo_last_action();

            if(currActionValue < minChildValue) {
                //worstChildAction = childAction;
                minChildValue = currActionValue;
            }
            //System.out.println("minValue: " + currActionValue);

            if(minChildValue <= alpha) {
                //prune
                return minChildValue;
            }

            beta = Math.min(beta, minChildValue);
        }

        return minChildValue;
    }

    //DRY minimax
    public String miniMax(MiniMaxPlayer opponent) {
        Set<String> childActions = this.get_legal_actions(opponent);
        String bestChildAction = "";
        double bestChildValue = - INFINITY;

        System.out.println(childActions.size());

        for (String childAction : childActions) {

            this.play(childAction, true);
            state_count_without_pruning++;
            double currChildValue = minValue(opponent, 1);
            this.undo_last_action();
            if(currChildValue > bestChildValue) {
                bestChildValue = currChildValue;
                bestChildAction = childAction;
            }
            //System.out.println("miniMax: " + currChildValue);
        }
        return bestChildAction;
    }
    public double maxValue(MiniMaxPlayer opponent, int depth) {

        if(cutoffSearch(opponent, depth, true)) {
            return zeroSumEvaluate(opponent);
        }

        // now we go further
        Set<String> childActions = this.get_legal_actions(opponent);
        //String bestChildAction = "";
        double maxChildValue = - INFINITY;

        for (String childAction : childActions) {

            this.play(childAction, true);
            state_count_without_pruning++;
            double currActionValue = minValue(opponent, depth+1);
            this.undo_last_action();

            if(currActionValue > maxChildValue) {
                //bestChildAction = childAction;
                maxChildValue = currActionValue;
            }
            //System.out.println("maxValue: " + currActionValue);
        }

        return maxChildValue;
    }
    public double minValue(MiniMaxPlayer opponent, int depth) {
        //opponent plays this

        if(cutoffSearch(opponent, depth, false)) {
            return zeroSumEvaluate(opponent);
        }

        Set<String> childActions = opponent.get_legal_actions(this);
        //String worstChildAction = "";
        double minChildValue = + INFINITY;

        for (String childAction: childActions) {

            opponent.play(childAction, true);
            state_count_without_pruning++;
            double currActionValue = maxValue(opponent, depth+1);
            opponent.undo_last_action();

            if(currActionValue < minChildValue) {
                //worstChildAction = childAction;
                minChildValue = currActionValue;
            }
            //System.out.println("minValue: " + currActionValue);
        }

        return minChildValue;
    }


    // if the if condition is "depth >= cutOffDepth", then this function cuts off exactly at depth = depth
    // if it is ">" , then cuts off at depth+1

    // for example if we have cutOffDepth = 2, it cuts off at depth = 2 when it is ">" (it does not generate children of states with depth 2)
    // if it is ">=", then cuts off at depth = 3 (it DOES generate children of states with depth = 2)

    public boolean cutoffSearch(MiniMaxPlayer opponent, int depth, boolean isInMaxValue) {
        // if the maxValue function called this function, it means that the last action
        // belongs to MIN PLAYER, if minValue called it, it means last action belongs to MAX PLAYER

        // if this.player wins, it also means that this.player played the last action and vice versa

/*        if(this.is_winner()) {
            System.out.println("this.player wins");

            board.print_map();
            System.out.println(this.color+" played: "+ this.actions_logs);
            System.out.println(opponent.color+" played: "+ opponent.actions_logs);
        }*/

        //if(this.is_winner()) System.out.println("hellooo?");

        if(depth >= cutOffDepth || this.is_winner() || opponent.is_winner()) return true;
        else return false;
    }
}

class Pair {
    public String action;
    public Double eval;

    Pair(String a, Double e) {
        action = a;
        eval = e;
    }

    @Override
    public String toString() {
        return action + ": " + eval;
    }
}
