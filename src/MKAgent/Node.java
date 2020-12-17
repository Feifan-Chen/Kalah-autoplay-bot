package MKAgent;

import java.util.ArrayList;

public class Node implements Comparable<Node> {
    public ArrayList<Node> children;
    public Node parent;
    public Board board;
    public Side nextSide;
    public Double score;
    public int depth;
    public Integer moveFromParent;
    public Side ourSide;

    public Node(Node parent, Board board, int depth, Integer moveFromParent, Side nextSide, Side ourSide) {
        this.children = new ArrayList<>();
        this.parent = parent;
        this.board = board;
        this.moveFromParent = moveFromParent;
        this.depth = depth;
        this.nextSide = nextSide;
        this.score = null;
        this.ourSide = ourSide;
    }

    public Node(double score) {
        this.score = score;
        this.children = new ArrayList<>();
    }

    @Override
    public int compareTo(Node another) {
        double doubleValue = this.score;
        double doubleValue2 = another.score;
        if (doubleValue > doubleValue2) {
            return 1;
        }
        return (doubleValue < doubleValue2) ? -1 : 0;
    }
}
