package MKAgent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.EOFException;
import java.io.Reader;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main {
    /**
     * Input from the game engine.
     */
    private final static Reader input = new BufferedReader(new InputStreamReader(System.in));

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

    public static Side ourSide;
    public static boolean secondTurn;

    public static void main(final String[] array) {
        try {
            while (true) {
                System.err.println();
                String recvMsg = recvMsg();
                System.err.print("received: " + recvMsg);
                switch (Protocol.getMessageType(recvMsg)) {
                    case START: {
                        System.err.println("A start.");
                        boolean interpretStartMsg = Protocol.interpretStartMsg(recvMsg);
                        System.err.println("Starting player? " + interpretStartMsg);
                        if (interpretStartMsg) {
                            ourSide = Side.SOUTH;
                            sendMsg(Protocol.createMoveMsg(Tree.getBestMove(new Board(7, 7))));
                            continue;
                        }
                        ourSide = Side.NORTH;
                        secondTurn = true;
                        continue;
                    }
                    case STATE: {
                        System.err.println("A state.");
                        Board board = new Board(7, 7);
                        Protocol.MoveTurn interpretStateMsg = Protocol.interpretStateMsg(recvMsg, board);
                        System.err.println("This was the move " + interpretStateMsg.move);
                        System.err.println("Is the game over? " + interpretStateMsg.end);
                        if (!interpretStateMsg.end) {
                            System.err.println("Is it our turn? " + interpretStateMsg.again);
                        }
                        System.err.print("The board:\n" + board);
                        if (!interpretStateMsg.again) {
                            continue;
                        }
                        if (secondTurn && interpretStateMsg.move == 1) {
                            sendMsg(Protocol.createSwapMsg());
                            ourSide = ourSide.opposite();
                            System.err.println("I have swapped");
                            break;
                        }
                        secondTurn = false;

                        if (interpretStateMsg.move == -1) {
                            ourSide = ourSide.opposite();
                        }
                        sendMsg(Protocol.createMoveMsg(Tree.getBestMove(board)));
                        break;
                    }
                    case END: {
                        System.err.println("An end. Bye bye!");
                        return;
                    }
                }
            }
        }
        catch (InvalidMessageException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("This shouldn't happen: " + e.getMessage());
        }
    }
}
