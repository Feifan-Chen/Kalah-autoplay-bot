package MKAgent;

import java.util.ArrayList;
import java.util.Collections;

public class Evaluation {
    public static int getValue(Board board, Side mySide) {
        int value = getSeedDiff(board, Side.SOUTH) + maxMoveScore(board, Side.SOUTH);
        if (mySide == Side.SOUTH)
            return value;
        return -value;
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

    private static int maxMoveScore(Board board, Side playerSide) {
        ArrayList<Integer> scores = new ArrayList<>();
        Move simulationMove = new Move(Side.SOUTH, 1);
        int score = getMaxCaptureScore(board, playerSide, simulationMove);
        if (playerSide == Main.mySide) {
            Board simulationBoard = new Board(board);
            Kalah.makeMove(simulationBoard, simulationMove);
            score -= getMaxCaptureScore(simulationBoard, playerSide.opposite(), simulationMove);
        }
        scores.add(score);
        for (Move move : getMoveAgainMoves(board, playerSide)) {
            Board simulationBoard = new Board(board);
            Kalah.makeMove(simulationBoard, move);
            scores.add(1+maxMoveScore(simulationBoard, playerSide));
        }
        return Collections.max(scores);
    }
}