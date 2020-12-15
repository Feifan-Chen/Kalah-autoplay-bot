package MKAgent;

import java.util.ArrayList;
import java.util.Collections;

public class Evaluation {
    public static int getValue(Board board, Side playerSide) {
        int value = getSeedDiff(board, Side.SOUTH) + maxMoveScore(board, Side.SOUTH, true);
        if (playerSide == Side.SOUTH)
            return value;
        return -value;
    }

    private static int getSeedDiff(Board board, Side playerSide) {
        return board.getSeedsInStore(playerSide) - board.getSeedsInStore(playerSide.opposite());
    }

    private static boolean canMoveEndAtPos(Board board, Side playerSide, int des) {
        int oneRound = 2 * board.getNoOfHoles() + 1;
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            int seedNum = board.getSeeds(playerSide, i);
            if (seedNum > 0 && seedNum % oneRound == des - i)
                return true;
        }
        return false;
    }

    private static ArrayList<Move> getMoveAgainMoves(Board board, Side playerSide) {
        ArrayList<Move> moves = new ArrayList<>();
        int oneRound = 2 * board.getNoOfHoles() + 1;
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            int seedNum = board.getSeeds(playerSide, i);
            if (seedNum > 0 && seedNum % oneRound == board.getNoOfHoles() + 1 - i)
                moves.add(new Move(playerSide, i));
        }
        return moves;
    }

    private static int getMaxCaptureScore(Board board, Side playerSide, Move move) {
        int maxNum = 0;
        for(int i = 1; i <= board.getNoOfHoles(); ++i) {
            if (board.getSeeds(playerSide, i) == 0 && canMoveEndAtPos(board, playerSide, i)) {
                if (board.getSeedsOp(playerSide, i) > maxNum) {
                    move.setSide(playerSide);
                    move.setHole(i);
                    maxNum = board.getSeedsOp(playerSide, i);
                }
            }
        }
        // opp seeds captured and our seed
        return maxNum + 1;
    }

    // 这里可以把 Boolean simulateOpp 换成 int depth， Boolean的话跑两次 depth可以自己定跑几次
    private static int maxMoveScore(Board board, Side playerSide, Boolean simulateOpp) {
        ArrayList<Integer> scores = new ArrayList<>();
        Move simulationMove = new Move(Side.SOUTH, 1);
        int score = getMaxCaptureScore(board, playerSide, simulationMove);
        Board simulationBoard = new Board(board);
        Kalah.makeMove(simulationBoard, simulationMove);

        // 这里可以改weight
        double weight = 0.5;
        if (simulateOpp)
            score -= maxMoveScore(simulationBoard, playerSide.opposite(), false) * weight;
        scores.add(score);
        for (Move move : getMoveAgainMoves(board, playerSide)) {
            simulationBoard = new Board(board);
            Kalah.makeMove(simulationBoard, move);
            scores.add(1+maxMoveScore(simulationBoard, playerSide, simulateOpp));
        }
        return Collections.max(scores);
    }
}