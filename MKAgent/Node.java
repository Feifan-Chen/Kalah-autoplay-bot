package MKAgent;

import java.util.ArrayList;
import java.util.Objects;

public class Node implements Comparable<Node> {
    // Describes how we get here, null for root node.
    private Move move;

    // Describes whose decision leads to this node.
    private Side side;
    private Side whosTurnNext;
    private boolean root = false;

    private Node parent;
    private Board board;
    private int noOfVisits;
    private int totalScore;
    private ArrayList<Node> children;

    public Node() {
        move = null;
        this.noOfVisits = 0;
        this.totalScore = 0;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side, Side whosTurnNext, Move move, Board board, Node parent, ArrayList<Node> children) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.side = side;
        this.whosTurnNext = whosTurnNext;
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
        this.whosTurnNext = node.whosTurnNext;
        this.move = node.move;
    }

    public static boolean isLeafNode(Node node) {
        return node.children.size() == 0;
    }

    public static int getRootVisit(Node node) {
        Node parent = node.getParent();
        if (parent == null)
            return node.getNoOfVisits();
        getRootVisit(parent);

        // should not get here
        return -1;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild() {
        return this.children.get((int)(Math.random() * this.children.size()));
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

    public void setWhosTurnNext(Side whosTurnNext) {
        this.whosTurnNext = whosTurnNext;
    }

    public Side getWhosTurnNext() {
        return whosTurnNext;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public Double getUCTValue() {
        if (noOfVisits == 0)
            return Double.MAX_VALUE;

        /*
            UCB(Si) = avg(Vi) + c*sqrt(logN/ni)
            Vi is average value of its children nodes
            c is constant (usually 2)
            N is total number of visits
            n is current node's number of visits
         */
        double visits = noOfVisits;
        return (totalScore / visits + 2 * Math.sqrt(Math.log(getRootVisit(this)) / visits));
    }

    @Override
    public int compareTo(Node o) {
        return this.getUCTValue().compareTo(o.getUCTValue());
    }
}
