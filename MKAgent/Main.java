package MKAgent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

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
        while (node.getChildren().size() != 0)
            node = UCT.chooseBestUCTNode(node);
        return ret;
    }

    private static ArrayList<Node> expand(Node parent) {
        return parent.checkAvailableChildren();
    }

    private static int simulate(Node node, long timeAllowed) {

        // do this at most 10 times?
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeAllowed / 10;

        // save the current player's side
        Side my_side =  node.getSide();

        Side side = node.getSide().opposite();
        Board board = node.getBoard();
        Kalah kalah = new Kalah(board);

        while(!kalah.gameOver(board) && System.currentTimeMillis() < endTime)
        {
            // Get all legal moves
            ArrayList<Move> legalMoves = kalah.getAllLegalMoves(side);

            // Get a random move from above results
            Random rand = new Random();
            Move next_move = legalMoves.get(rand.nextInt(legalMoves.size()));

            // Make a move on the board and return the next side of player
            side = kalah.makeMove(board, next_move);
        }

        if(my_side.equals(side))
            return board.payoffs(side);
        else
            return board.payoffs(side.opposite());
    }

    private static void backPropagation(Node node, int payoff, Node root) {
        //while it is not back to the root, update payoff value and increase
        Node currentNode = node;
        while(!currentNode.equals(root)){
            currentNode.incrementOneVisit();
            if(currentNode.getSide() == node.getSide() )
                currentNode.incrementScore(payoff);
            currentNode = currentNode.getParent();
        }

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
        root.setBoard(board);
        root.setSide(opposite);

        while (System.currentTimeMillis() < endTime) {
            // Selection.
            Node selectedNode = selection(root);

            // Expansion.
            ArrayList<Node> availableChildren = expand(selectedNode);

            Node nodeToExplore = selectedNode.getRandomChild();

            // Simulation.
            int payoff = simulate(nodeToExplore, timeAllowed);
            Node nodeToExplore = Node.getRandomChild(availableChildren);
            int payoff = simulate(nodeToExplore);

            // Backpropagation.
            backPropagation(nodeToExplore, payoff, root);
        }

        // We need the move that leads to the best result.
        return root.getBestChild().getMove();
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

        long timeAllowed = 0;

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
                default: break;
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

                Protocol.MoveTurn r = Protocol.interpretStateMsg (msg, kalah.getBoard());

                if (r.move == -1) {
                    my_side = my_side.opposite();
                }

//                if (!moveTurn.again || moveTurn.end) {
//                    continue;
//                }

                // Calculate next move using MCTS
                Move next_move = MCTSNextMove(kalah.getBoard(), my_side, timeAllowed);
                msg = Protocol.createMoveMsg(next_move.getHole());

                // If North side, decide whether to swap by:
                // simulate the next payoff, and calculate the payoff of opp-player,
                // if that payoff is greater, then create swap message.
                if (may_swap)
                {
                    Board move_board = new Board(kalah.getBoard());
                    Kalah.makeMove(move_board, next_move);

                    int original_payoff = kalah.getBoard().payoffs(my_side);
                    int after_swap_payoff = kalah.getBoard().payoffs(my_side.opposite());

                    if (after_swap_payoff > original_payoff)
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
