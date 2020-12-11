package MKAgent;

import java.util.ArrayList;
import java.util.Collections;

public class Evaluation {
    public static int getValue(Node node) {
        return getValue(node.getBoard(), node.getWhosTurnNext());
    }

    public static int getValue(Board board, Side playerSide) {
        return getSeedDiff(board, playerSide) + maxMoveScore(board, playerSide);
    }

    private static int getSeedDiff(Board board, Side playerSide) {
        return board.getSeedsInStore(playerSide) - board.getSeedsInStore(playerSide.opposite());
    }

    private static ArrayList<Move> getMovesEndAtPos(Board board, Side playerSide, int des) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            if (board.getSeeds(playerSide, i) == des - i)
                moves.add(new Move(playerSide, i));
        }
        return moves;
    }

    private static boolean canMoveEndAtPos(Board board, Side playerSide, int des) {
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            if (board.getSeeds(playerSide, i) == des - i)
                return true;
        }
        return false;
    }

    private static ArrayList<Move> getMoveAgainMoves(Board board, Side playerSide) {
        return getMovesEndAtPos(board, playerSide, board.getNoOfHoles()+1);
    }

    private static int getMaxCaptureScore(Board board, Side playerSide) {
        int maxNum = 0;
        for(int i = 1; i <= board.getNoOfHoles(); ++i) {
            if (board.getSeeds(playerSide, i) == 0 && canMoveEndAtPos(board, playerSide, i)) {
                if (board.getSeedsOp(playerSide, i) > maxNum)
                    maxNum = board.getSeedsOp(playerSide, i);
            }
        }
        // opp seeds captured and our seed
        return maxNum + 1;
    }

    private static int maxMoveScore(Board board, Side playerSide) {
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(getMaxCaptureScore(board, playerSide));
        for (Move move : getMoveAgainMoves(board, playerSide)) {
            Board simulationBoard = new Board(board);
            Kalah.makeMove(simulationBoard, move);
            scores.add(1+maxMoveScore(simulationBoard, playerSide));
        }
        return Collections.max(scores);
    }
}
