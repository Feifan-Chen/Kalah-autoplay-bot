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
        children = null;
    }

    public Node(int noOfVisits, int totalScore, Side side, Move move, Board board) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.side = side;
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
        this.children = node.children;
        this.side = node.side;
        this.move = node.move;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild(ArrayList<Integer> availableChildren) {
        int index = (int) (Math.random() * availableChildren.size());
        return this.children.get(availableChildren.get(index));
        //return this.children.get((int)(Math.random() * this.children.size()));
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

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalScore() {
        return this.totalScore;
    }

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
        if (children == null) {
            children = new ArrayList<>();
            for (int i = 0; i < board.getNoOfHoles(); i++) {
                Board nodeBoard = new Board(getBoard());
                Move nodeMove = new Move(side, i + 1);
                if (Kalah.isLegalMove(nodeBoard, nodeMove)) {
                    Kalah.makeMove(nodeBoard, nodeMove);
                    Node child = new Node(0, 0, side, nodeMove, nodeBoard, this, null);
                    children.add(child);
                }
            }
        }
        assert(children.size() != 0);

        return children;
    }

    public ArrayList<Integer> checkAvailableChildren(){
        ArrayList<Node> children = getChildren();
        ArrayList<Integer> available = new ArrayList<>();
        for(int i = 0; i < children.size(); i++) {
            if (children.get(i).getNoOfVisits() == 0)
                available.add(i);
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
}
