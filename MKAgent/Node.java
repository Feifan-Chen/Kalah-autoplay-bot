package MKAgent;

import java.util.ArrayList;

public class Node {
    private Side side;
    private Node parent;
    private int noOfVisits;
    private int totalScore;
    private ArrayList<Node> children;

    public Node() {
        this.noOfVisits = 0;
        this.totalScore = 0;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.side = side;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side, Node parent, ArrayList<Node> children) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.side = side;
        this.parent = parent;
        this.children = children;
    }

    public Node(Node node) {
        this.noOfVisits = node.noOfVisits;
        this.totalScore = node.totalScore;
        this.parent = node.parent;
        this.children = node.children;
        this.side = node.side;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild() {
        return this.children.get((int)(Math.random() * this.children.size()));
    }

    public Node getBestChild() {
        return null;
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

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public ArrayList<Node> getChildren() {
        return this.children;
    }
}
