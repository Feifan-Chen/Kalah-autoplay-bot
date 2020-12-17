package MKAgent;

import java.util.ArrayList;

public class Tree {

    public static final int MAX_DEPTH = 7;
    public Node root;

    public Tree(final Node root) {
        this.root = root;
    }

    private Node minimaxMain(Node node) {
        final Node[] array = new Node[node.children.size()];
        if (node.children.isEmpty()) {
            return node;
        }
        for (int i = 0; i < node.children.size(); ++i) {
            array[i] = this.minimaxMain(node.children.get(i));
        }
        return (Main.ourSide == node.nextSide) ? this.max(array) : this.min(array);
    }

    private Node min(final Node[] array) {
        Node node = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (node != null && node.score != null) {
                if (array[i] != null && array[i].score != null && array[i].score < node.score) {
                    node = array[i];
                }
            }
            else {
                node = array[i];
            }
        }
        return node;
    }

    private Node max(final Node[] array) {
        Node node = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (node != null && node.score != null) {
                if (array[i] != null && array[i].score != null && array[i].score > node.score) {
                    node = array[i];
                }
            }
            else {
                node = array[i];
            }
        }
        return node;
    }

    public int alphaBeta(Node node) {
        Node node2 = this.alphaBetaMain(node);
        if (node2 == null || node2.parent == null) {
            node2 = this.minimaxMain(node);
        }
        return this.getBestMove(node2);
    }

    public Node alphaBetaMain(Node node) {
        return this.alphaBetaMax(node, new Node(Double.MIN_VALUE), new Node(Double.MAX_VALUE));
    }

    private Node alphaBetaMax(Node node, Node node2, Node node3) {
        if (node.children.isEmpty()) {
            return node;
        }
        for (int i = 0; i < node.children.size(); ++i) {
            Node node4;
            if (node.children.get(i).nextSide == node.ourSide) {
                node4 = this.alphaBetaMax(node.children.get(i), node2, node3);
            }
            else {
                node4 = this.alphaBetaMin(node.children.get(i), node2, node3);
            }
            if (node4.score == null) {
                return node3;
            }
            if (node3.score == null) {
                return node4;
            }
            if (node4.score >= node3.score) {
                return node3;
            }
            if (node4.score > node2.score) {
                node2 = node4;
            }
        }
        return node2;
    }

    private Node alphaBetaMin(Node node, Node node2, Node node3) {
        if (node.children.isEmpty()) {
            return node;
        }
        for (int i = 0; i < node.children.size(); ++i) {
            Node node4;
            if (node.children.get(i).nextSide == node.ourSide) {
                node4 = this.alphaBetaMax(node.children.get(i), node2, node3);
            }
            else {
                node4 = this.alphaBetaMin(node.children.get(i), node2, node3);
            }
            if (node4.score == null) {
                return node2;
            }
            if (node2.score == null) {
                return node4;
            }
            if (node4.score <= node2.score) {
                return node2;
            }
            if (node4.score < node3.score) {
                node3 = node4;
            }
        }
        return node3;
    }

    private int getBestMove(Node node) {
        Node parent;
        for (parent = node; parent.parent.parent != null; parent = parent.parent) {
        }
        return parent.moveFromParent;
    }

    public static Tree buildTree(Board board, int maxDepth) {
        Node node = new Node(null, board, 0, null, Main.ourSide, Main.ourSide);
        Tree tree = new Tree(node);
        buildTreeRecurse(node, 1, maxDepth);
        return tree;
    }

    private static void buildTreeRecurse(Node node, int currentDepth, int maxDepth) {
        if (currentDepth <= maxDepth) {
            ArrayList<Integer> possibleMoves = node.board.getPossibleMoves(node.nextSide);
            for (Integer possibleMove : possibleMoves) {
                try {
                    Board clone = node.board.clone();
                    Side nextSide = Kalah.makeMove(clone, new Move(node.nextSide, possibleMove));
                    Node child = new Node(node, clone, currentDepth, possibleMove, nextSide, node.ourSide);
                    node.children.add(child);
                    buildTreeRecurse(child, currentDepth + 1, maxDepth);
                } catch (Exception ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }
        }
        else {
            double d = Heuristic.getScore(node.board, node.nextSide);
            if (node.nextSide != Main.ourSide) {
                d *= -1.0;
            }
            node.score = d;
        }
    }

    private static int getBestMoveWithDepth(Board board, int maxDepth) {
        try {
            Tree tree = buildTree(board, maxDepth);
            return tree.alphaBeta(tree.root);
        }
        catch (OutOfMemoryError outOfMemoryError) {
            return getBestMoveWithDepth(board, maxDepth - 1);
        }
    }

    public static int getBestMove(Board board) {
        return getBestMoveWithDepth(board, MAX_DEPTH);
    }
}
