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
        return UCT.chooseBestUCTNode(node);
    }

    private static void expand(Node parent, Node node) {
        node.setParent(parent);
    }

    private static int simulate(Node node) {
        return 0;
    }

    private static void backPropagation(Node node, int payoff, Node root) {
        //while it is not back to the root, update payoff value and increase
        Node currentNode = node;
        while(currentNode != root){
            currentNode.incrementOneVisit();
            if(currentNode.getSide() == node.getSide() )
                currentNode.incrementScore(payoff);
            currentNode = currentNode.getParent();
        }

        //reward delay method.
//        int delay_moves = 0;
//        double reward_weight = 1;
//        while (currentNode != root){
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
            expand(root, selectedNode);

            // Simulation.
            Node nodeToExplore = selectedNode.getRandomChild();
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
        // TODO: implement
    }
}
