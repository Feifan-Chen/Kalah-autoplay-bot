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
        if (node.isLeafNode()) {
            if (node.getNoOfVisits() == 0)
                return node;
            return expand(node);
        }
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

        if (leafNode.getChildren().size() == 0)
            return leafNode;
        return leafNode.getChildren().get(new Random().nextInt(leafNode.getChildren().size()));
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

    private static Node getBestChild(ArrayList<Node> children) {
        return Collections.max(children, (first, second) -> {
            double firstReward = first.getRobust();
            double secondReward = second.getRobust();
            if (firstReward > secondReward)
                return 1;
            else if (firstReward < secondReward)
                return -1;
            return 0;
        });
    }

    private static Move MCTSNextMove(Board board, long timeAllowed) {
        int generation = 0;
        final int GEN_LIMIT = 1000000;

        long endTime = System.currentTimeMillis() + timeAllowed*100;

        Node root = new Node(0, 0, null, mySide, null, board, null, new ArrayList<>());

        while (System.currentTimeMillis() < endTime && generation < GEN_LIMIT) {
            generation++;

            // Selection.
            Node nodeToExplore = selection(root);

            // Rollout.
            rollout(nodeToExplore, timeAllowed);
        }

        // We need the move that leads to the best result.
        ArrayList<Node> children = root.getChildren();
        Node best_child = getBestChild(children);
        return best_child.getMove();
    }

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
                    mySide = mySide.opposite();
                    oppSide = oppSide.opposite();
                    sendMsg(Protocol.createSwapMsg());
                    may_swap = false;
                    continue;
                }

                // Calculate next move using MCTS
                Move next_move = MCTSNextMove(kalah.getBoard(), timeAllowed);
                msg = Protocol.createMoveMsg(next_move.getHole());

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
