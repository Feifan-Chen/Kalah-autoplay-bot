package MKAgent;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main {
    /**
     * Input from the game engine.
     */
    private static final Reader input = new BufferedReader(new InputStreamReader(System.in));
    public static Side mySide;
    //private static Side mySide;
    private static Side oppSide;
    private static final int MAX_DEPTH = 3;

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

    public static Node selectionAndExpansion(Node node) {
        //if node has children which is all visited, check next level.
        if(!node.isLeafNode()){
            if(node.childrenAllVisited()) {
                return selectionAndExpansion(Collections.max(node.getChildren(), Comparator.comparing(Node::getUCTValue)));
            }
            else
                return node.getRandomAvaliableChild();
        }

        //if node is has no children, expand it
        return expansion(node);

//        if (node.isLeafNode()) {
//            if (node.getNoOfVisits() == 0)
//                return expansion(node);
//            return node;
//        }
//        return selectionAndExpansion(Collections.max(node.getChildren(), Comparator.comparing(Node::getUCTValue)));
    }

    public static Node expansion(Node leafNode) {
        Board board = leafNode.getBoard();
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            Board nodeBoard = new Board(board);
            Move nodeMove = new Move(leafNode.getWhosTurnNext(), i);
            if (Kalah.isLegalMove(nodeBoard, nodeMove)) {
                Side turn = Kalah.makeMove(nodeBoard, nodeMove);
                Node child = new Node(0, 0, turn, nodeMove, nodeBoard, leafNode, new ArrayList<>());
                leafNode.addChild(child);
            }
        }

        if (leafNode.getChildren().size() == 0) {
            return leafNode;
        }
        return leafNode.getChildren().get(new Random().nextInt(leafNode.getChildren().size()));
    }

    public static void rolloutAndBackPropagation(Node node, long timeAllowed) {
        Node simulateNode = new Node(node);
        Board board = simulateNode.getBoard();
        Side side = simulateNode.getWhosTurnNext();

        while(!Kalah.gameOver(board))
        {
            ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, side);
            Move next_move = legalMoves.get(new Random().nextInt(legalMoves.size()));
            side = Kalah.makeMove(board, next_move);
        }
        int result;
        double score;
        result = board.payoffs(mySide);
        if(result > 0){
            if (result > 30){
                score = 1;
            }
            score = 0.5;
        }
        else
            score = 0;

        backPropagation(node, score);
    }

    public static void backPropagation(Node node, double payoff) {
        node.setNoOfVisits(node.getNoOfVisits() + 1);
        node.setTotalScore(node.getTotalScore() + payoff);
        Node parent = node.getParent();
        if (parent != null)
            backPropagation(parent, payoff);
    }

    public static Node getMaxRobustChild(Node root) {
        ArrayList<Node> children = root.getChildren();
        double maxVisited = -1;
        double maxReward = -Double.MAX_VALUE;
        for (Node child : children) {
            double childVisited = child.getNoOfVisits();
            double childReward = child.getTotalScore()/childVisited;
            if (childVisited > maxVisited)
                maxVisited = childVisited;
            if (childReward > maxReward)
                maxReward = childReward;
        }
        for (Node child : children) {
            double childVisited = child.getNoOfVisits();
            double childReward = child.getTotalScore()/childVisited;
            if (childVisited == maxVisited && childReward == maxReward)
                return child;
        }
        return null;
    }


    private static double MCTSNextMove(Board board, long timeAllowed, Side side) {

        int generation = 0;
        final int GEN_LIMIT = Integer.MAX_VALUE;

        long endTime = System.currentTimeMillis() + timeAllowed;


        Node root = new Node(0, 0, side, null, board, null, new ArrayList<>());

        Node bestChild = null;

        boolean inLimit = true;


        while (inLimit || bestChild == null) {
            inLimit = System.currentTimeMillis() < endTime && generation < GEN_LIMIT;
            generation++;

            // Selection and Expansion.
            Node nodeToExplore = selectionAndExpansion(root);

            // Rollout and BackPropagation.
            rolloutAndBackPropagation(nodeToExplore, timeAllowed);

            if (!inLimit)
            {
                bestChild = getMaxRobustChild(root);
                if (bestChild == null)
                {
                    double highest = Double.MIN_VALUE;
                    for(Node child : root.getChildren()){
                        if (highest < child.getTotalScore())
                            highest = child.getTotalScore();
                    }
                    return highest;
                }
            }
        }

//        for (Node child : root.getChildren())
//            System.err.println("visit : " + child.getNoOfVisits() + "genearation " + generation);


        //return root.getBestChild().getMove();
        return bestChild.getTotalScore();
    }

    public static double heuristic(Board board, Side side) {
        return board.payoffs(mySide);
    }


    private static  Object[] minimax_pruning(Node node, double alpha, double beta, int depth)
    {

        double payoff;

        if (Kalah.gameOver(node.getBoard()))
        {

            if (node.getBoard().payoffs(mySide) > 0)
                return new Object[]{null, 1.0};
            else
                return new Object[]{null, 0.0};

        }

        if (depth == 0)
        {
            return new Object[] {null, MCTSNextMove(node.getBoard(), 8, node.getWhosTurnNext())};
        }

        // Max node
        if (node.getWhosTurnNext() == mySide)
        {
            double max = Double.MIN_VALUE;
            int bestMove = 1;
            double bestValue = Double.MIN_VALUE;
            expansion(node);
            ArrayList<Node> children = node.getChildren();

            for(Node child :children)
            {
                payoff = (double)minimax_pruning(child, alpha, beta, depth - 1)[1];
                max = Double.max(max, payoff);
                alpha = Double.max(alpha, max);

                if (bestValue <= alpha)
                {
                    bestMove =child.getMove().getHole();
                    bestValue = alpha;
                }

                if(beta <= alpha)
                    break;
            }
            return new Object[]{bestMove, bestValue};

        }
        else
        {
            double min = Double.MAX_VALUE;
            int bestMove = 1;
            double bestValue = Double.MAX_VALUE;

            expansion(node);
            ArrayList<Node> children = node.getChildren();

            for(Node child :children)
            {
                payoff = (double) minimax_pruning(child, alpha, beta, depth - 1)[1];
                min = Double.min(min, payoff);
                beta = Double.min(beta, min);

                if (bestValue >= beta)
                {
                    bestMove = child.getMove().getHole();
                    bestValue = beta;
                }

                if(beta <= alpha)
                    break;
            }
            return new Object[]{bestMove, beta};

        }
    }

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


    //    public static Object[] minimax(Side side, Board board, double alpha, double beta, int depth, boolean maxPlayer) {
//        // Return value should be Object[Move, Double].
//
//        // 我们希望尽可能快的赢，e.g. 深度为2时赢比深度为5时赢更好，所以获胜时的value是max-depth。
//        if (Kalah.hasWon(board, mySide))
//            return new Object[] {null, Double.MAX_VALUE - depth};
//
//        // 同理，我们希望尽可能慢的输，e.g. 深度为5时输比深度为2时输更好，所以输的时候value是min+depth。
//        if (Kalah.hasWon(board, side.opposite()))
//            return new Object[] {null, -1 * Double.MAX_VALUE + depth};
//
//        if (depth == MAX_DEPTH) {
//            return new Object[] {null, MCTSNextMove(board, 8, side)};
//        }
//        // System.err.println("Depth: " + depth);
//        double bestValue;
//        double value;
//        Move bestMove = null;
//
//        if (maxPlayer) {
//            bestValue = -1 * Double.MAX_VALUE;
//            ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, side);
//            // System.err.println("NoOfLegalMoves: " + legalMoves.size());
//            for (Move move : legalMoves) {
//                Board copiedBoard = new Board(board);
//                Side nextPlayer = Kalah.makeMove(copiedBoard, move);
//                if (nextPlayer == side) {
//                    // An extra move, do not deepen the tree.
//                    value = (double) minimax(side, copiedBoard, alpha, beta, depth, true)[1];
//                }
//                else {
//                    value = (double) minimax(side.opposite(), copiedBoard, alpha, beta, depth + 1, false)[1];
//                }
//
//                if (value > bestValue) {
//                    bestValue = value;
//                    bestMove = move;
//                }
//
//                alpha = Math.max(alpha, bestValue);
//
//                // Pruning.
//                if (beta <= alpha)
//                    break;
//            }
//        }
//        else {
//            bestValue = Double.MAX_VALUE;
//            ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, side.opposite());
//            // System.err.println("NoOfLegalMoves: " + legalMoves.size());
//            for (Move move : legalMoves) {
//                Board copiedBoard = new Board(board);
//                Side nextPlayer = Kalah.makeMove(copiedBoard, move);
//                if (nextPlayer == side.opposite()) {
//                    // An extra move, do not deepen the tree.
//                    value = (double) minimax(side.opposite(), copiedBoard, alpha, beta, depth, false)[1];
//                }
//                else {
//                    value = (double) minimax(side, copiedBoard, alpha, beta, depth + 1, true)[1];
//                }
//
//                if (value < bestValue) {
//                    bestValue = value;
//                    bestMove = move;
//                }
//
//                beta = Math.min(beta, bestValue);
//
//                // Pruning.
//                if (beta <= alpha)
//                    break;
//            }
//        }
//        return new Object[] {bestMove, bestValue};
//    }


    /**
     * The main method, invoked when the program is started.
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        boolean may_swap = false;

        // Record the board locally.
        Kalah kalah = new Kalah(new Board(7,7));

        long timeAllowed = 1000;

        try {
            String msg = recvMsg();
            MsgType msg_type = Protocol.getMessageType(msg);

            /*
             Start of the game.
             Determine who is on which side.
             If this side is South, then make a move first;
             If this side is North, then enable may_swap.
            */
            switch (msg_type) {
                case START:
                    System.err.println("A start.");
                    boolean south = Protocol.interpretStartMsg(msg);
                    System.err.println("Starting player? " + south);
                    if (south) {
                        mySide = Side.SOUTH;
                        oppSide = Side.NORTH;
                        sendMsg(Protocol.createMoveMsg(1));
                    } else {
                        mySide = Side.NORTH;
                        oppSide = Side.SOUTH;
                        may_swap = true;
                    }
                    break;
                case END:
                    System.err.println("An end. Bye bye!");
                    return;
                default:
                    System.err.println("State message expected");
                    break;
            }

            // Continues the game
            while (true)
            {
                System.err.println();
                msg = recvMsg();
                System.err.print("Received: " + msg);

                msg_type = Protocol.getMessageType(msg);

                if (msg_type == MsgType.END)
                    return;

                if (msg_type != MsgType.STATE)
                    throw new InvalidMessageException("State message expected");

                Protocol.MoveTurn r = Protocol.interpretStateMsg(msg, kalah.getBoard());

                if (r.move == -1) {
                    mySide = mySide.opposite();
                    oppSide = oppSide.opposite();
                }

                if (!r.again) {
                    continue;
                }

                if (may_swap)
                {
                    may_swap = false;
                    if (r.move <= 2) {
                        mySide = mySide.opposite();
                        oppSide = oppSide.opposite();
                        sendMsg(Protocol.createSwapMsg());
                        continue;
                    }
                }
               // Calculate next move using MCTS
                long start = System.currentTimeMillis();
                //Move next_move = MCTSNextMove(kalah.getBoard(), timeAllowed);
                Node node = new Node();
                node.setBoard(new Board(kalah.getBoard()));
                node.setWhosTurnNext(mySide);
                int next_move = (int)(minimax_pruning(node, Double.MIN_VALUE, Double.MAX_VALUE, 5)[0]);
                System.err.println(System.currentTimeMillis() - start);

                msg = Protocol.createMoveMsg(next_move);

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
