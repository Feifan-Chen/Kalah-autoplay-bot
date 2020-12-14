package MKAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import static MKAgent.Main.mySide;

public class Node implements Comparable<Node> {
    // Describes how we get here, null for root node.
    private Move move;

    // Describes who should move next.
    private Side whosTurnNext;

    private Node parent;
    private Board board;
    private int noOfVisits;
    private double totalScore;
    private ArrayList<Node> children;

    public Node() {
        move = null;
        this.noOfVisits = 0;
        this.totalScore = 0;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, double totalScore, Side whosTurnNext, Move move, Board board, Node parent, ArrayList<Node> children) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
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
        this.whosTurnNext = node.whosTurnNext;
        this.move = node.move;
    }

    public boolean isLeafNode() {
        return this.children.size() == 0;
    }

    public static int getRootVisit(Node node) {
        Node parent = node.getParent();
        if (parent == null)
            return node.getNoOfVisits();
        return getRootVisit(parent);
    }

    public double getMax() {
        return (double)totalScore/noOfVisits;
    }

    public double getRobust() {
        return noOfVisits;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild() {
        return this.children.get((int)(Math.random() * this.children.size()));
    }

    // Setters, Getters
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

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getTotalScore() {
        return this.totalScore;
    }

    public void incrementScore(double payoff) {this.totalScore += payoff; }

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


    public static boolean isKind2GreedyChild(Board board, Side side, int hole) {
        int noOfSeeds = board.getSeeds(side, hole);
        Board currentBoard = new Board(board);
        //int round = noOfSeeds / 15;
        //System.err.println(round);
        int endHole = (noOfSeeds % 15 + hole) % 15;
        //System.err.println(endHole);
        if (endHole > 7 || endHole == 0){
            //System.err.println("here");
            // 注意 当endHole为8时，是Kind1GreedyChild
            return false;}
        Move nodeMove = new Move(side,hole);
        Kalah.makeMove(currentBoard,nodeMove);
        //System.err.println(currentBoard.toString());
        //System.err.println(currentBoard.getSeedsInStore(side) - board.getSeedsInStore(side) - round > 1);
        //return currentBoard.getSeedsInStore(side) - board.getSeedsInStore(side) - round > 1;
        //System.err.println(currentBoard.getSeeds(side, endHole));
//        if(board.getSeeds(side,endHole) == 0 && currentBoard.getSeeds(side,endHole) == 0)
//            if(board.getSeedsOp(side, endHole) > 0 && currentBoard.getSeedsOp(side, endHole) ==0)
//                return true;
//        return false;
        return (board.getSeeds(side, endHole) == 0);
    }



//    public ArrayList<Node> checkAvailableChildren(){
//        ArrayList<Node> children = getChildren();
//        ArrayList<Node> available = new ArrayList<>();
//        for(Node child : children) {
//            if (child.getNoOfVisits() == 0)
//                available.add(child);
//        }
//        return available;
//    }

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

    public Double getUCTValue() {
        if (Kalah.gameOver(board))
            return (double)0;

        if (noOfVisits == 0)
            return Double.MAX_VALUE;

        /*
            UCB(Si) = avg(Vi) + c*sqrt(ln(N)/ni)
            Vi is average value of its children nodes
            c is constant (usually 2)
            N is total number of visits
            n is current node's number of visits
         */
        double visits = noOfVisits;
        if(this.move.getSide() == mySide)
            return ( totalScore/visits + 2 * Math.sqrt(2 * Math.log(this.getParent().getNoOfVisits())/ visits));
        else
            return ( (1- totalScore/visits) + 2 * Math.sqrt(2 * Math.log(this.getParent().getNoOfVisits())/ visits));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(move, node.move) &&
                Objects.equals(parent, node.parent) &&
                Objects.equals(board, node.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(move, whosTurnNext, parent, board, noOfVisits, totalScore, children);
    }

    @Override
    public int compareTo(Node o) {
        return this.getUCTValue().compareTo(o.getUCTValue());
    }

    public boolean childrenAllVisited() {
        for(Node child : this.children){
            if(child.getNoOfVisits() == 0)
                return false;
        }
        return true;
    }

    public Node getRandomAvaliableChild() {
        int noOfChildren = this.children.size();
        for(Node child : this.children){
            if(child.getNoOfVisits() == 0)
                return child;
        }
        return this;
    }

    public Node getBestChild(){
        return Collections.max(this.children, (first, second) -> {
            int vit1 = first.noOfVisits;
            int vit2 = second.noOfVisits;
            return Double.compare(vit1, vit2);
        });

    }
}
