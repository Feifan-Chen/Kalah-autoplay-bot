package MKAgent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
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
        if (Node.isLeafNode(node))
            return node;
        selection(Collections.max(node.getChildren()));
        // should never get here
        return null;
    }

    private static Node expand(Node leafNode) {
        Board board = leafNode.getBoard();
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            Board nodeBoard = new Board(board);
            Move nodeMove = new Move(leafNode.getSide().opposite(), i);
            if (Kalah.isLegalMove(nodeBoard, nodeMove)) {
                Kalah.makeMove(nodeBoard, nodeMove);
                Node child = new Node(0, 0, nodeMove.getSide(), nodeMove, nodeBoard, leafNode, new ArrayList<>());
                leafNode.addChild(child);
            }
        }
        return leafNode.getChildren().get(0);
    }

    private static int rollout(Node node, long timeAllowed) {
        return 0;
    }

    private static void backPropagation(Node node, int payoff, Node root) {

    }

    private static Move MCTSNextMove(Board board, Side side, long timeAllowed) {
        int generation = 0;
        final int GEN_LIMIT = 200;

        // Side should be me, not the opponent.
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeAllowed;
        Tree tree = new Tree();
        Node root = tree.getRoot();
        root.setBoard(board);
        root.setSide(side.opposite());

        while (System.currentTimeMillis() < endTime && generation < GEN_LIMIT) {
            generation++;

            // Selection.
            Node selectedNode = selection(root);
            if (Kalah.gameOver(selectedNode.getBoard()))
                break;

            // Expansion.
            Node nodeToExplore = expand(selectedNode);

            // Rollout.
            int payoff = rollout(nodeToExplore, timeAllowed);

            // Backpropagation.
            backPropagation(nodeToExplore, payoff, root);
        }

        // We need the move that leads to the best result.
        return Collections.max(root.getChildren()).getMove();
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

        long timeAllowed = 1000;

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


                if (!r.again || r.end) {
                    continue;
                }

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
                    //System.err.println("op: " + kalah.getBoard() + "payoff : " + original_payoff);
                    int after_swap_payoff = move_board.payoffs(my_side.opposite());
                    //System.err.println("swap" + move_board + "pay off + " + after_swap_payoff);

                    System.err.println();
                    System.err.println("1" + msg);
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
                System.err.println(msg);
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
