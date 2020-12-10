package MKAgent;

import java.util.ArrayList;
import java.util.Collections;
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

    // is kind 2 greedy
    private boolean greedy;
    private boolean greedy2;

    public Node() {
        move = null;
        this.noOfVisits = 0;
        this.totalScore = 0;
        this.greedy = false;
        this.greedy2 = false;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side, Move move, boolean greedy, boolean greedy2, Board board) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.side = side;
        this.greedy = greedy;
        this.greedy2 = greedy2;
        this.children = new ArrayList<>();
    }

    public Node(int noOfVisits, int totalScore, Side side, Move move,boolean greedy, boolean greedy2, Board board, Node parent, ArrayList<Node> children) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.side = side;
        this.greedy = greedy;
        this.greedy2 = greedy2;
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
        this.greedy = node.greedy;
        this.greedy2 = node.greedy2;
    }


    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild() {
        return this.children.get((int)(Math.random() * this.children.size()));
    }

//    public Node getBestChild() {
//        return UCT.chooseBestUCTNode(this);
//    }

    public Node getBestChild() {
        return Collections.max(this.children, (first, second) -> {
            int vit1 = first.totalScore;
            int vit2 = second.totalScore;
            return Double.compare(vit1, vit2);
        });
        //return UCT.chooseBestUCTNode(this);
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

        for (int i = 0; i < board.getNoOfHoles(); i++) {
            Board nodeBoard = new Board(board);
            Side moveSide;
            if(this.greedy)
                moveSide = this.side;
            else
                moveSide = this.side.opposite();

            Move nodeMove = new Move(moveSide, i + 1);

            if (Kalah.isLegalMove(nodeBoard, nodeMove))
            {
                Node child;
                // Greedy is an unusual move, so assuming opponent's decision leads to this move.
                boolean is_greedy = isKind1GreedyChild(board, moveSide, nodeMove.getHole());
                //System.err.println("hole: " + (i+1)  + "greedy1: " + is_greedy);

                boolean is2_greedy = isKind2GreedyChild(board, moveSide, nodeMove.getHole());

                if (is_greedy)
                {
                    //Move greedyMove = new Move(moveSide, nodeMove.getHole());
                    Kalah.makeMove(nodeBoard, nodeMove);

                    child = new Node(0,0,moveSide,nodeMove,true,false,nodeBoard,this, new ArrayList<>());
                }
                else if (is2_greedy)
                {
                    //System.err.println("is a greedy child");
                    //Move greedy2Move = new Move(side, nodeMove.getHole());
                    Kalah.makeMove(nodeBoard, nodeMove);

                    child = new Node(0,0, moveSide,nodeMove,false,true,nodeBoard,this, new ArrayList<>());
                }
                else
                {
                    Kalah.makeMove(nodeBoard, nodeMove);
                    child = new Node(0, 0, moveSide, nodeMove,false, false, nodeBoard, this, new ArrayList<>());
                }

                children.add(child);
            }
        }
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

    public boolean getGreedy(){
        return this.greedy;
    }
    public void setGreedy(boolean greedy){
        this.greedy = greedy;
    }

    public boolean getGreedy2(){
        return this.greedy2;
    }
    public void setGreedy2(boolean greedy2){
        this.greedy2 = greedy2;
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
