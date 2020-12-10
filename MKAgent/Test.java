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
        //System.err.println(Node.isKind2GreedyChild(board, side, 1));

        board.setSeeds(side, 2, 34);
        board.setSeeds(side.opposite(), 6, 3);
        assert(Node.isKind2GreedyChild(board, side, 2));

        board.setSeeds(side.opposite(), 6, 0);
        assert(!Node.isKind2GreedyChild(board, side, 2));

        board.setSeeds(side, 2, 37);
        assert(!Node.isKind2GreedyChild(board, side, 2));
    }


    public static void testMCTGreedy()
    {
        Side my_side = Side.NORTH;
        Board board = new Board(7, 7);

//        for (int i = 1; i < 8; i ++)
//        {
//            board.setSeeds(my_side, i, 2);
//            board.setSeeds(my_side.opposite(), i, 2);
//        }
        board.setSeeds(my_side, 1, 0);
        board.setSeeds(my_side, 2, 16);
        board.setSeeds(my_side, 3, 5);
        board.setSeeds(my_side, 4, 0);
        board.setSeeds(my_side, 5, 0);
        board.setSeeds(my_side, 6, 2);
        board.setSeeds(my_side, 7, 9);
        board.setSeeds(my_side.opposite(), 1, 4);
        board.setSeeds(my_side.opposite(), 2, 6);
        board.setSeeds(my_side.opposite(), 3, 1);
        board.setSeeds(my_side.opposite(), 4, 2);
        board.setSeeds(my_side.opposite(), 5, 8);
        board.setSeeds(my_side.opposite(), 6, 1);
        board.setSeeds(my_side.opposite(), 7, 16);

        System.err.println(Node.isKind2GreedyChild(board, my_side, 7));

        for (int i = 0; i < 10; i++)
        {
            System.err.println("original board " + board);
            Board temp = new Board(board);
            Move move = Main.MCTSNextMove(temp, my_side, 10000);
            System.err.println("MOVE side: " + move.getSide() + " hole: " + move.getHole());
            Kalah.makeMove(temp, move);
            System.err.println("Board After" + temp);
        }
    }


    public static void main(String[] args) {
        testIsKind1GreedyChild();
        testIsKind2GreedyChild();

        testMCTGreedy();
    }
}
