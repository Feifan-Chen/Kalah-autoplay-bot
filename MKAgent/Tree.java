package MKAgent;

public class Tree {
    private Node root;

    public Tree() {
        root = new Node();
    }

    public Tree(Node root) {
        this.root = root;
    }

    public void addChild(Node parent, Node child) {
        parent.addChild(child);
    }

    // Getter and setter.
    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
