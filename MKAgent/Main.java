package MKAgent;
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

    private static Side mySide;
    private static Side oppSide;

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
        return selection(Collections.max(node.getChildren(), Comparator.comparing(Node::getUCTValue)));
    }

    private static Node expand(Node leafNode) {
        Board board = leafNode.getBoard();
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            Board nodeBoard = new Board(board);
            Move nodeMove = new Move(leafNode.getWhosTurnNext(), i);
            if (Kalah.isLegalMove(nodeBoard, nodeMove)) {
                Side turn = Kalah.makeMove(nodeBoard, nodeMove);
                Node child = new Node(0, 0, nodeMove.getSide(), turn, nodeMove, nodeBoard, leafNode, new ArrayList<>());
                leafNode.addChild(child);
            }
        }
        return leafNode.getChildren().get(0);
    }

    private static Node rollout(Node node, long timeAllowed) {
        Board board = node.getBoard();
        if (Kalah.gameOver(board)) {
            backPropagation(node, board.payoffs(mySide));
            return node;
        }

        ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, node.getWhosTurnNext());
        Move randMove = legalMoves.get(new Random().nextInt(legalMoves.size()));
        Board simulateBoard = new Board(board);
        Side turn = Kalah.makeMove(simulateBoard, randMove);
        Node simulateNode = new Node(0, 0, randMove.getSide(), turn, randMove, simulateBoard, node, new ArrayList<>());

        for (Node child : node.getChildren()) {
            if (child.equals(simulateNode)) {
                simulateNode = child;
                break;
            }
        }
        return rollout(simulateNode, timeAllowed);
    }

    private static void backPropagation(Node node, int payoff) {
        node.setNoOfVisits(node.getNoOfVisits() + 1);
        node.setTotalScore(node.getTotalScore() + payoff);
        Node parent = node.getParent();
        if (parent != null)
            backPropagation(parent, payoff);
    }

    private static Move MCTSNextMove(Board board, long timeAllowed) {
        int generation = 0;
        final int GEN_LIMIT = 20000;

        long endTime = System.currentTimeMillis() + timeAllowed*10000000;

        Node root = new Node(0, 0, null, mySide, null, board, null, new ArrayList<>());

        while (System.currentTimeMillis() < endTime && generation < GEN_LIMIT) {
            generation++;

            // Selection.
            Node selectedNode = selection(root);

            Node nodeToExplore = selectedNode;
            // Expansion.
            if (selectedNode.getNoOfVisits() == 0)
                nodeToExplore = expand(selectedNode);

            // Rollout.
            rollout(nodeToExplore, timeAllowed);

//            // Backpropagation.
//            backPropagation(rolloutNode);
        }

        // We need the move that leads to the best result.
        Node best_child = Collections.max(root.getChildren(), (first, second) -> {
            double firstReward = (double)first.getTotalScore()/first.getNoOfVisits();
            double secondReward = (double)second.getTotalScore()/second.getNoOfVisits();
            if (firstReward > secondReward)
                return 1;
            else if (firstReward < secondReward)
                return -1;
            return 0;
        });
        return best_child.getMove();
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
                        mySide = Side.SOUTH;
                        oppSide = Side.NORTH;
                        sendMsg(Protocol.createMoveMsg(1));
                    }
                    else
                    {
                        mySide = Side.NORTH;
                        oppSide = Side.SOUTH;
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

                Protocol.MoveTurn r = Protocol.interpretStateMsg(msg, kalah.getBoard());

                if (r.move == -1) {
                    mySide = mySide.opposite();
                    oppSide = oppSide.opposite();
                }

                if (!r.again) {
                    continue;
                }

                // Calculate next move using MCTS
                Move next_move = MCTSNextMove(kalah.getBoard(), timeAllowed);

                msg = Protocol.createMoveMsg(next_move.getHole());

                // If North side, decide whether to swap by:
                // simulate the next payoff, and calculate the payoff of opp-player,
                // if that payoff is greater, then create swap message.
                if (may_swap)
                {
                    Board move_board = new Board(kalah.getBoard());
                    Kalah.makeMove(move_board, next_move);


                    int original_payoff = kalah.getBoard().payoffs(mySide);
                    int after_swap_payoff = kalah.getBoard().payoffs(oppSide);

                    if (after_swap_payoff >= original_payoff)
                    {
                        mySide = mySide.opposite();
                        oppSide = oppSide.opposite();
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
