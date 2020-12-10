package MKAgent;
import java.io.*;
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

    private static Node selectionAndExpansion(Node node) {
        if (node.isLeafNode()) {
            if (node.getNoOfVisits() == 0)
                return expansion(node);
            return node;
        }
        return selectionAndExpansion(Collections.max(node.getChildren(), Comparator.comparing(Node::getUCTValue)));
    }

    private static Node expansion(Node leafNode) {
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

        if (leafNode.getChildren().size() == 0)
            return leafNode;
        return leafNode.getChildren().get(new Random().nextInt(leafNode.getChildren().size()));
    }

    private static Node rolloutAndBackPropagation(Node node, long timeAllowed) {
        Board board = node.getBoard();
        if (Kalah.gameOver(board)) {
            backPropagation(node, board.payoffs(mySide));
            return node;
        }

        ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, node.getWhosTurnNext());
        Move randMove = legalMoves.get(new Random().nextInt(legalMoves.size()));
        Board simulateBoard = new Board(board);
        Side turn = Kalah.makeMove(simulateBoard, randMove);
        Node simulateNode = new Node(0, 0, turn, randMove, simulateBoard, node, new ArrayList<>());

        for (Node child : node.getChildren()) {
            if (child.equals(simulateNode)) {
                simulateNode = child;
                break;
            }
        }
        return rolloutAndBackPropagation(simulateNode, timeAllowed);
    }

    private static void backPropagation(Node node, int payoff) {
        node.setNoOfVisits(node.getNoOfVisits() + 1);
        node.setTotalScore(node.getTotalScore() + payoff);
        Node parent = node.getParent();
        if (parent != null)
            backPropagation(parent, payoff);
    }

    private static Node getMaxRobustChild(Node root) {
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


    private static Move MCTSNextMove(Board board, long timeAllowed) {
        int generation = 0;
        final int GEN_LIMIT = Integer.MAX_VALUE;

        long endTime = System.currentTimeMillis() + timeAllowed;

        Node root = new Node(0, 0, mySide, null, board, null, new ArrayList<>());

        Node bestChild = null;

        while ((System.currentTimeMillis() < endTime) || bestChild == null) {
            generation++;

            // Selection and Expansion.
            Node nodeToExplore = selectionAndExpansion(root);

            // Rollout and BackPropagation.
            rolloutAndBackPropagation(nodeToExplore, timeAllowed);

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
        boolean may_swap = false;

        // Record the board locally.
        Kalah kalah = new Kalah(new Board(7,7));

        long timeAllowed = 200;

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
