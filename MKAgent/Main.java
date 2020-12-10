package MKAgent;
//import java.awt.desktop.SystemEventListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        {
        //    System.err.println("parent " + parent);
            parent.expand();
          //  System.err.println("parent children size " + parent.getChildren().size());
        }


        ArrayList<Node> children = parent.getChildren();
        //System.err.println(children.size());
        for (Node node: children)
            if (node.getNoOfVisits() == 0)
            {
                return node;
            }



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

    private static Node getMaxRobustChild(Node root) {
        ArrayList<Node> children = root.getChildren();
        double maxReward = -Double.MAX_VALUE;
        double maxVisited = -1;
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

        if(my_side.equals(side))
            return board.payoffs(side);
        else
            return board.payoffs(side.opposite());
    }

    private static void backPropagation(Node node, int payoff, Node root) {
        //while it is not back to the root, update payoff value and increase
        Node currentNode = node;
        int count = 0;
        do {
            count ++;
            //System.err.println("before backup " + currentNode + "visit : " + currentNode.getNoOfVisits());
            currentNode.incrementOneVisit();
            //System.err.println("backup " + currentNode + "visit : " + currentNode.getNoOfVisits());
            if(currentNode.getSide() == node.getSide() )
                currentNode.incrementScore(payoff);
            currentNode = currentNode.getParent();
        }
        while (!currentNode.equals(root));


        currentNode.incrementOneVisit();
        //System.err.println("backup " + currentNode + "visit : " + currentNode.getNoOfVisits());
        if(currentNode.getSide() == node.getSide() )
            currentNode.incrementScore(payoff);
        currentNode = currentNode.getParent();


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
//        }
    }

    private static Move MCTSNextMove(Board board, Side side, long timeAllowed) {
        // Side should be me, not the opponent.
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeAllowed;
        Side opposite = side.opposite();
        Tree tree = new Tree();
        Node root = tree.getRoot();
        root.setNoOfVisits(1);
        root.setBoard(board);
        root.setSide(opposite);

        Node bestChild = null;

        while (System.currentTimeMillis() < endTime || bestChild == null) {
            // Selection.
            //System.err.println("root " + root + " visit " + root.getNoOfVisits());
            Node selectedNode = selection(root);
            Node nodeToExplore = selectedNode;
            if (!Kalah.gameOver(selectedNode.getBoard()))
                nodeToExplore = expand(selectedNode);

            // Simulation.
            int payoff = simulate(nodeToExplore, timeAllowed);

            // Backpropagation.
            backPropagation(nodeToExplore, payoff, root);

            if (System.currentTimeMillis() >= endTime)
                bestChild = getMaxRobustChild(root);
        }

        // We need the move that leads to the best result.
        return bestChild.getMove();
    }

    /**
     * The main method, invoked when the program is started.
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        Side my_side = null;
        boolean may_swap = false;

        // Record the board locally.
        Kalah kalah = new Kalah(new Board(7,7));

        long timeAllowed = 200;

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
                //System.err.println();
                msg = recvMsg();
                //System.err.print("Received: " + msg);

                msg_type = Protocol.getMessageType(msg);

                if (msg_type == MsgType.END)
                    return;

                if (msg_type != MsgType.STATE)
                    throw new InvalidMessageException("State message expected");

                Protocol.MoveTurn r = Protocol.interpretStateMsg (msg, kalah.getBoard());

                if (r.move == -1) {
                    my_side = my_side.opposite();
                }

                //System.err.println("again:" + r.again);
                //System.err.println("endgame:" + r.end);
//                if(!r.again){
//                        continue;
//                }
                if (!r.again || r.end) {
//                    System.err.println("again:" + r.again);
//                    System.err.println("endgame:" + r.end);
                    //System.err.println("condition happened");
                    continue;
                }

                // Calculate next move using MCTS
                Move next_move = MCTSNextMove(kalah.getBoard(), my_side, timeAllowed);
                //System.err.println(next_move.getSide() + "" + next_move.getHole());
                msg = Protocol.createMoveMsg(next_move.getHole());

                // If North side, decide whether to swap by:
                // simulate the next payoff, and calculate the payoff of opp-player,
                // if that payoff is greater, then create swap message.
                if (may_swap)
                {
                    Board move_board = new Board(kalah.getBoard());
                    Kalah.makeMove(move_board, next_move);


                    int original_payoff = kalah.getBoard().payoffs(my_side);
                    //System.err.println("op: " + kalah.getBoard() + "payoff : " + original_payoff);
                    int after_swap_payoff = move_board.payoffs(my_side.opposite());
                    //System.err.println("swap" + move_board + "pay off + " + after_swap_payoff);

                    //System.err.println();
                    //System.err.println("1" + msg);
                    if (after_swap_payoff >= original_payoff)
                    {
                        my_side = my_side.opposite();
                        msg = Protocol.createSwapMsg();
                        //System.err.println("2" + msg);
                    }
                }
                may_swap = false;

                // send message to game engine.
                sendMsg(msg);
                //System.err.println(msg);
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
