package MKAgent;

public class Test {
    public static void testIsGreedyChild() {
        Board board = new Board(7, 7);
        Side side = Side.SOUTH;
        board.setSeeds(side, 1, 16);
        assert(Main.isGreedyChild(board, side, 1));

        board.setSeeds(side, 7, 32);
        assert(!Main.isGreedyChild(board, side, 7));
    }
    public static void main(String[] args) {
        testIsGreedyChild();
    }
}
