package trees.tree;

import trees.rectangle.*;
import java.util.Vector;

public class Node {
    public Node(Tree t) {
        tree = t;
        childrenNodes = new Vector<Node>(tree.M);
        mbr = new Rectangle(0, 0, 0, 0);
    }

    public Tree tree = null;
    public boolean isLeafNode = false;
    public boolean isEndpoint = false;
    public Node parent = null;
    public Rectangle mbr = null;
    public Vector<Node> childrenNodes = null;
    public Integer index = new Integer(-1);

    public Node splitNode(Node newNode) {
        return newNode;
    }


    public void add(Node n) {
        if(childrenNodes.size() == tree.M) {
            System.out.println("too many childrenNodes");
            return;
        }
        childrenNodes.add(n);
        n.parent = this;
        mbr = Rectangle.enlarge(mbr, n.mbr);
        isEndpoint = false;
    }

    public void addAll(Vector<Node> nodes) {
        for(Node node : nodes) {
            add(node);
        }
    }

    // TODO
    public Node insertNode(Node insertingNode, int insertDepth) {
        return insertingNode;
    }

    // TODO
    public boolean isLeafNode() {
        return isLeafNode;
    }

    // TODO
    public boolean isNonleafNode() {
        return !isLeafNode;
    }

    // TODO
    public Node chooseSubTree(Node insertingNode) {
        return insertingNode;
    }

    // TODO
    public boolean isEmpty() {
        return true;
    }

    // TODO
    public int size() {
        return 8;
    }

    // TODO
    public Node overflowTreatment() {
        return this;
    }

    // TODO
    public Node parent() {
        return parent;
    }

    // TODO
    public void addChild(Node newChild, int dunno) {

    }
}
