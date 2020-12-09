package MKAgent;

public class Test {
    public static void testIsKind1GreedyChild() {
        Board board = new Board(7, 7);
        Side side = Side.SOUTH;
        board.setSeeds(side, 1, 16);
        assert(Node.isKind1GreedyChild(board, side, 1));

        board.setSeeds(side, 7, 32);
        assert(!Node.isKind1GreedyChild(board, side, 7));
    }

    public static void testIsKind2GreedyChild() {
        Board board = new Board(7, 7);
        Side side = Side.NORTH;
        board.setSeeds(side, 1, 2);
        board.setSeeds(side.opposite(), 5, 4);
        assert(Node.isKind2GreedyChild(board, side, 1));

        board.setSeeds(side, 2, 34);
        board.setSeeds(side.opposite(), 6, 3);
        assert(Node.isKind2GreedyChild(board, side, 2));

        board.setSeeds(side.opposite(), 6, 0);
        assert(!Node.isKind2GreedyChild(board, side, 2));

        board.setSeeds(side, 2, 37);
        assert(!Node.isKind2GreedyChild(board, side, 2));
    }

    public static void main(String[] args) {
        testIsKind1GreedyChild();
        testIsKind2GreedyChild();
    }
}
