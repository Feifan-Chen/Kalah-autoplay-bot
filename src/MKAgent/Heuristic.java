package MKAgent;

import java.util.Collections;
import java.util.ArrayList;

public class Heuristic
{
    public static int getScore(Board board, Side side) {
        final int n = getSeedDiff(board, Side.SOUTH) + maxMoveScore(board, Side.SOUTH, 0);
        if (side == Side.SOUTH) {
            return n;
        }
        return -n;
    }

    private static int getSeedDiff(Board board, Side side) {
        return board.getSeedsInStore(side) - board.getSeedsInStore(side.opposite());
    }

    private static ArrayList<Move> getMovesEndAtPos(Board board, Side side, int n) {
        final ArrayList<Move> list = new ArrayList<>();
        for (int i = 1; i <= board.getNoOfHoles(); ++i) {
            if (board.getSeeds(side, i) == n - i) {
                list.add(new Move(side, i));
            }
        }
        return list;
    }
    
    private static boolean canMoveEndAtPos(Board board, Side side, int n) {
        for (int i = 1; i <= board.getNoOfHoles(); ++i) {
            if (board.getSeeds(side, i) == n - i) {
                return true;
            }
        }
        return false;
    }
    
    private static ArrayList<Move> getMoveAgainMoves(Board board, Side side) {
        return getMovesEndAtPos(board, side, board.getNoOfHoles() + 1);
    }
    
    private static int getMaxCaptureScore(Board board, Side side, Move move) {
        int seedsOp = 0;
        for (int i = 1; i <= board.getNoOfHoles(); ++i) {
            if (board.getSeeds(side, i) == 0 && canMoveEndAtPos(board, side, i) && board.getSeedsOp(side, i) > seedsOp) {
                move.setSide(side);
                move.setHole(i);
                seedsOp = board.getSeedsOp(side, i);
            }
        }
        return seedsOp + 1;
    }
    
    private static int maxMoveScore(Board board, Side side, int n) {
        ArrayList<Integer> coll = new ArrayList<>();
        Move move = new Move(Side.SOUTH, 1);
        int maxCaptureScore = getMaxCaptureScore(board, side, move);
        if (side == Main.ourSide) {
            final Board board2 = new Board(board);
            Kalah.makeMove(board2, move);
            maxCaptureScore -= getMaxCaptureScore(board2, side.opposite(), move);
        }
        coll.add(maxCaptureScore);
        for (Move move2 : getMoveAgainMoves(board, side)) {
            Board board3 = new Board(board);
            Kalah.makeMove(board3, move2);
            coll.add(1 + maxMoveScore(board3, side, maxCaptureScore));
        }
        return Collections.max(coll);
    }
}
