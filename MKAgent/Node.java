package MKAgent;

import java.util.ArrayList;
import java.util.Objects;

public class Node {
    // Describes how we get here, null for root node.
    private Move move;

    // Describes whose decision leads to this node.
    private Side side;

    private Node parent;
    private Board board;
    private int noOfVisits;
    private int totalScore;
    private ArrayList<Node> children;

    public Node() {
        move = null;
        this.noOfVisits = 0;
        this.totalScore = 0;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side, Move move, Board board) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.side = side;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side, Move move, Board board, Node parent, ArrayList<Node> children) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.side = side;
        this.parent = parent;
        this.children = children;
    }

    public Node(Node node) {
        this.noOfVisits = node.noOfVisits;
        this.totalScore = node.totalScore;
        this.parent = node.parent;
        this.board = new Board(node.board);
        // Should be a deep copy.
        this.children = new ArrayList<>();
        this.children.addAll(node.children);
        this.side = node.side;
        this.move = node.move;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild() {
        return this.children.get((int)(Math.random() * this.children.size()));
    }

    public Node getBestChild() {
        return UCT.chooseBestUCTNode(this);
    }

    // Setters, Getters
    public void setSide(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return this.side;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return this.parent;
    }

    public void setNoOfVisits(int noOfVisits) {
        this.noOfVisits = noOfVisits;
    }

    public int getNoOfVisits() {
        return this.noOfVisits;
    }

    public void incrementOneVisit() {this.noOfVisits += 1; }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalScore() {
        return this.totalScore;
    }

    public void incrementScore(int payoff) {this.totalScore += payoff; }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return this.board;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public ArrayList<Node> getChildren() {
        return this.children;
    }

    public void expand() {
        children = new ArrayList<>();
        ArrayList<Node> not_greedy_children = new ArrayList<>();

        for (int i = 0; i < board.getNoOfHoles(); i++) {
            Board nodeBoard = new Board(board);
            //System.err.println("board " + board);
           // System.err.println("nodeBoard " + nodeBoard);
            Move nodeMove = new Move(side.opposite(), i + 1);
            if (Kalah.isLegalMove(nodeBoard, nodeMove)) {
                //System.err.println("check");
                boolean is_greedy = is_greedy_child(nodeBoard, nodeMove);
                Kalah.makeMove(nodeBoard, nodeMove);
                Node child = new Node(0, 0, side.opposite(), nodeMove, nodeBoard, this, new ArrayList<>());
                //System.err.println("children board " + child.getBoard());
                if (is_greedy)
                    children.add(child);

                not_greedy_children.add(child);
            }
        }

        //System.err.println("greedy size "  + not_greedy_children.size());
        if(children.size() == 0)
            children = not_greedy_children;

        //System.err.println("children size "  + children.size());

    }

    public boolean is_greedy_child(Board board, Move move)
    {
        // If the seeds in the hole equal to the 8-move
        // means this move will have another turn.
        if (board.getSeeds(move.getSide(), move.getHole()) == (8 - move.getHole()))
            return true;
        else
            return false;
    }

    public ArrayList<Node> checkAvailableChildren(){
        ArrayList<Node> children = getChildren();
        ArrayList<Node> available = new ArrayList<>();
        for(Node child : children) {
            if (child.getNoOfVisits() == 0)
                available.add(child);
        }
        return available;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Move getMove() {
        return this.move;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(move, node.move) &&
                side == node.side &&
                Objects.equals(parent, node.parent) &&
                Objects.equals(board, node.board);
    }

    public static boolean isKind1GreedyChild(Board board, Side side, int hole) {
        /*计分板 hole7 hole6 hole5 hole4 hole3 hole2 hole1
                hole1 hole2 hole3 hole4 hole5 hole6 hole7 计分板
         */
        int noOfSeeds = board.getSeeds(side, hole);
        if (noOfSeeds <= 7) {
            return noOfSeeds + hole == 8;
        }
        else if (noOfSeeds >= 16) {
            // 至少需要15个子才能转完一圈（转完后回到自身）。
            return noOfSeeds % 15 + hole == 8;
        }
        else
            // 7和16之间一定不会出现greedy child
            return false;
    }

    public static boolean isKind2GreedyChild(Board board, Side side, int hole) {
        int noOfSeeds = board.getSeeds(side, hole);
        int endHole = noOfSeeds % 15 + hole;
        if (endHole > 7)
            // 注意 当endHole为8时，是Kind1GreedyChild
            return false;
        return board.getSeeds(side, endHole) == 0 && board.getSeedsOp(side, endHole) > 0;
    }
}
