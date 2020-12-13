package MKAgent;
//import java.awt.desktop.SystemEventListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main {
    /**
     * Input from the game engine.
     */
    private static final Reader input = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Sends a message to the game engine.
     * @param msg The message.
     */
    public static void sendMsg (String msg) {
        System.out.print(msg);
        System.out.flush();
    }

    /**
     * Receives a message from the game engine. Messages are terminated by
     * a '\n' character.
     * @return The message.
     * @throws IOException if there has been an I/O error.
     */
    public static String recvMsg() throws IOException {
        StringBuilder message = new StringBuilder();
        int newCharacter;

        do {
            newCharacter = input.read();
            if (newCharacter == -1)
                throw new EOFException("Input ended unexpectedly.");
            message.append((char)newCharacter);
        } while((char)newCharacter != '\n');

        return message.toString();
    }

    private static final int MAX_DEPTH = 5;

    private static Node selection(Node node) {
        Node ret = node;

        while (ret.getChildren().size() != 0)
        {

            ArrayList<Node> children = ret.getChildren();
            for (Node child: children)
                if (child.getNoOfVisits() == 0)
                    return child.getParent();

            ret = UCT.chooseBestUCTNode(ret);
        }
        return ret;
    }
//CHANGE;1;7,7,7,7,7,7,7,0,0,8,8,8,8,8,8,1;YOU
    private static Node expand(Node parent) {
        if (parent.getNoOfVisits() == 1)
                parent.expand();
          //  System.err.println("parent children size " + parent.getChildren().size());

        ArrayList<Node> children = parent.getChildren();
        //System.err.println(children.size());
        for (Node node: children)
            if (node.getNoOfVisits() == 0)
                return node;

        return parent.getRandomChild();

//        ArrayList<Node> greedy_children = new ArrayList<Node>();
//        for(Node node: parent.getChildren())
//        {
//            if(move_gain(node))
//                greedy_children.add(node);
//        }
//        if(greedy_children.size() == 0)
//            return parent.getRandomChild();
//        else
//            return greedy_children.getrandom();
    }

    private static int simulate(Node give_node, long timeAllowed) {

        Node node = new Node(give_node);
        // do this at most 10 times?
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeAllowed / 10;

        // save the current player's side
        Side my_side =  node.getSide();

        Side side = node.getSide().opposite();
        Board board = node.getBoard();
        Kalah kalah = new Kalah(board);

        while(!Kalah.gameOver(board))
        {
            // Get all legal moves
            ArrayList<Move> legalMoves = kalah.getAllLegalMoves(side);

            // Get a random move from above results
            Random rand = new Random();
            Move next_move = legalMoves.get(rand.nextInt(legalMoves.size()));

            // Make a move on the board and return the next side of player
            side = Kalah.makeMove(board, next_move);
            //System.err.println(board);
        }

        return board.payoffs(my_side);

//        if (board.payoffs(my_side) > 0)
//            return 1;
//        else
//            return 0;
    }

/*
    // Playgame - each layer is a move.
    private static int minimax_pruning(Node node, int alpha, int beta, int depth, Side my_side)
    {
        int payoff = 0;
        if (depth == 0 || Kalah.gameOver(node.getBoard()))
            return node.getTotalScore();

        // Max node
        if (node.getSide().opposite().equals(my_side))
        {
            int max = Integer.MIN_VALUE;

            ArrayList<Node> children = node.getChildren();
            for(Node child :children)
                payoff = minimax_pruning(child, alpha, beta, depth - 1, my_side.opposite());

            max = Integer.max(max, payoff);

            alpha = Integer.max(alpha, max);

            if(beta <= alpha)
                break;

            return max;

        }
        else
        {
            int min = Integer.MAX_VALUE;

            ArrayList<Node> children = node.getChildren();

            for(Node child :children)
                payoff = minimax_pruning(child, alpha, beta, depth - 1, my_side);

            min = Integer.min(min, payoff);

            alpha = Integer.min(beta, min);

            if(beta <= alpha)
                break;

            return min;

        }
    }
*/
    /*
    public Best chooseMove(final boolean side, final int[] board,
                           int alpha, int beta, final int depth, final int maxDepth)
    {
        final Best myBest = new Best();
        Best reply;
        final int num;

        if (Board.checkGameOver(board) || depth == maxDepth) {
            final Best fakeBest = new Best();
            fakeBest.setScore(returnPositionScore(board));
            return fakeBest;
        }

        if (side) {
            myBest.setScore(alpha);
            num = numberOfEngine;
        } else {
            myBest.setScore(beta);
            num = numberOfOpponent;
        }

        for (final int move: searchAvailableMoves(board)) {
            board[move] = num;
            reply = chooseMove(!side, board, alpha, beta, depth + 1, maxDepth);
            board[move] = 0;
            if (side && reply.getScore() > myBest.getScore()) {
                myBest.setMove(move);
                myBest.setScore(reply.getScore());
                alpha = reply.getScore();

            } else if (!side && reply.getScore() < myBest.getScore()) {
                myBest.setMove(move);
                myBest.setScore(reply.getScore());
                beta = reply.getScore();

            }
            if (alpha >= beta) {
                return myBest;
            }
        }

        return myBest;
    }

*/

    private static void backPropagation(Node node, int payoff, Node root) {
        //while it is not back to the root, update payoff value and increase
        //
        // System.err.println("payoff :" + payoff);
        Node currentNode = node;
        //System.err.println(node);
        int score = payoff;
        if(node.getGreedy()){
            //System.err.println("greedy");
            score = score + payoff;
        }
        else if(node.getGreedy2()){
            //System.err.println(score);
            score = payoff*payoff;
        }
        //int double_payoff = payoff*payoff;
        do {
            //System.err.println("before backup " + currentNode + "visit : " + currentNode.getNoOfVisits());
            currentNode.incrementOneVisit();
            //System.err.println("backup " + currentNode + "visit : " + currentNode.getNoOfVisits());
            if(currentNode.getSide() == node.getSide() ){
                currentNode.incrementScore(score);
            }
            currentNode = currentNode.getParent();
            //System.err.println(currentNode.getTotalScore());
        }
        while (!currentNode.equals(root));

        currentNode.incrementOneVisit();

     //   System.err.println("COunt" + count);
        //reward delay method.
//        int delay_moves = 0;
//        double reward_weight = 1;
//        while (!currentNode.equals(root)){
//          currentNode.incrementOneVisit();
//          if(currentNode.getSide() == node.getSide() ){
//              delay_moves++;
//              double weight = Math.pow(reward_weight, (double)delay_moves);
//              currentNode.incrementScore(payoff * weight);
//          }
//          currentNode = currentNode.getParent();
//        }currentBoard
    }

    public static Move MCTSNextMove(Board board, Side side, long timeAllowed) {
        // Side should be me, not the opponent.
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeAllowed;
        Side opposite = side.opposite();
        Tree tree = new Tree();
        Node root = tree.getRoot();
        root.setNoOfVisits(1);
        root.setBoard(board);
        root.setSide(opposite);
        root.setGreedy(false);

        while (System.currentTimeMillis() < endTime) {
            // Selection.
            //System.err.println("root " + root + " visit " + root.getNoOfVisits());
            Node selectedNode = selection(root);
            if (Kalah.gameOver(selectedNode.getBoard())){
                //System.err.println("end of game");
                break;
            }
         //   System.err.println("selection board" + selectedNode.getBoard());
            //System.err.println("selection " + selectedNode + "visit " + selectedNode.getNoOfVisits());
           // System.err.println("num of visit" + selectedNode.getNoOfVisits() + "total score: " + selectedNode.getTotalScore());
          //  System.err.println("Node" + selectedNode);
           // System.err.println("sum of visit" + selectedNode.getNoOfVisits());
            // Expansion.
            Node nodeToExplore = expand(selectedNode);
            //System.err.println("nodeToExplore " + nodeToExplore + "visit " + nodeToExplore.getNoOfVisits());

            // Simulation.
            int payoff = 0;
            for (int i = 0; i< 15; i++)
                payoff += simulate(nodeToExplore, timeAllowed);

            // Backpropagation.
            backPropagation(nodeToExplore, payoff, root);

         //   System.err.println("after selection " + selectedNode + "visit " + selectedNode.getNoOfVisits());
           // System.err.println("after nodeToExplore " + nodeToExplore + "visit " + nodeToExplore.getNoOfVisits());
        }

        // We need the move that leads to the best result.
        return root.getBestChild().getMove();
    }

    public static double heuristic(Board board, Side side) {
        return Evaluation.getValue(board, side);
    }

    public static Object[] minimax(Side side, Board board, double alpha, double beta, int depth, boolean maxPlayer) {
        // Return value should be Object[Move, Double].
        if (depth == MAX_DEPTH || Kalah.gameOver(board)) {
            return new Object[] {null, heuristic(board, side)};
        }
        // System.err.println("Depth: " + depth);
        double bestValue;
        double value;
        Move bestMove = null;

        if (maxPlayer) {
            bestValue = -1 * Double.MAX_VALUE;
            ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, side);
            // System.err.println("NoOfLegalMoves: " + legalMoves.size());
            for (Move move : legalMoves) {
                Board copiedBoard = new Board(board);
                Side nextPlayer = Kalah.makeMove(copiedBoard, move);
                if (nextPlayer == side) {
                    // An extra move, do not deepen the tree.
                    value = (double) minimax(side, copiedBoard, alpha, beta, depth, true)[1];
                }
                else {
                    value = (double) minimax(side.opposite(), copiedBoard, alpha, beta, depth + 1, false)[1];
                }

                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }

                alpha = Math.max(alpha, bestValue);

                // Pruning.
                if (beta <= alpha)
                    break;
            }
        }
        else {
            bestValue = Double.MAX_VALUE;
            ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, side.opposite());
            // System.err.println("NoOfLegalMoves: " + legalMoves.size());
            for (Move move : legalMoves) {
                Board copiedBoard = new Board(board);
                Side nextPlayer = Kalah.makeMove(copiedBoard, move);
                if (nextPlayer == side.opposite()) {
                    // An extra move, do not deepen the tree.
                    value = (double) minimax(side.opposite(), copiedBoard, alpha, beta, depth, false)[1];
                }
                else {
                    value = (double) minimax(side, copiedBoard, alpha, beta, depth + 1, true)[1];
                }

                if (value < bestValue) {
                    bestValue = value;
                    bestMove = move;
                }

                beta = Math.min(beta, bestValue);

                // Pruning.
                if (beta <= alpha)
                    break;
            }
        }
        return new Object[] {bestMove, bestValue};
    }

    /**
     * The main method, invoked when the program is started.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Side my_side = null;
        boolean may_swap = false;

        // Record the board locally.
        Kalah kalah = new Kalah(new Board(7,7));

        // long timeAllowed = 10000;

        try {
            String msg = recvMsg();
            MsgType msg_type = Protocol.getMessageType(msg);

            // Start of the game.
            switch (msg_type)
            {
                // Determine who is on which side.
                // If this side is South, then make a move first;
                // If this side is North, then enable may_swap.
                case START: System.err.println("A start.");
                    boolean south = Protocol.interpretStartMsg(msg);
                    System.err.println("Starting player? " + south);
                    if(south)
                    {
                        my_side = Side.SOUTH;
                        sendMsg(Protocol.createMoveMsg(1));
                    }
                    else
                    {
                        my_side = Side.NORTH;
                        may_swap = true;
                    }
                    break;
                case END: System.err.println("An end. Bye bye!"); return;
                default:
                    System.err.println("State message expected");
                    break;
            }

            // Continues the game
            while (true)
            {
                msg = recvMsg();
                msg_type = Protocol.getMessageType(msg);

                if (msg_type == MsgType.END)
                    return;

                if (msg_type != MsgType.STATE)
                    throw new InvalidMessageException("State message expected");

                Protocol.MoveTurn r = Protocol.interpretStateMsg (msg, kalah.getBoard());

                if (r.move == -1) {
                    my_side = my_side.opposite();
                }

                if (!r.again || r.end) {
                    continue;
                }

                /*
                  只有当not (!r.again || r.end)时，以下代码才会执行。
                  等同于 只有当(r.again && !r.end)时，以下代码才会执行，命题逻辑化简。
                 */
                // Calculate next move using minimax.
                // Double.Min_Value is 0.
                Move next_move = (Move)(minimax(my_side, kalah.getBoard(), -1 * Double.MAX_VALUE, Double.MAX_VALUE,
                                  0, true)[0]);
                msg = Protocol.createMoveMsg(next_move.getHole());

                // If North side, decide whether to swap by:
                // simulate the next payoff, and calculate the payoff of opp-player,
                // if that payoff is greater, then create swap message.
                if (may_swap)
                {
                    Board move_board = new Board(kalah.getBoard());
                    Kalah.makeMove(move_board, next_move);


                    int original_payoff = kalah.getBoard().payoffs(my_side);
                    int after_swap_payoff = move_board.payoffs(my_side.opposite());

                    if (after_swap_payoff >= original_payoff)
                    {
                        my_side = my_side.opposite();
                        msg = Protocol.createSwapMsg();
                    }
                }
                may_swap = false;

                // send message to game engine.
                sendMsg(msg);
            }
        }
        catch (InvalidMessageException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e)
        {
            System.err.println("This shouldn't happen: " + e.getMessage());
        }
    }
}
