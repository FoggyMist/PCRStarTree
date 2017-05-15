package trees.tree;

import trees.tree.*;
import trees.rectangle.*;
import java.util.Vector;

public class Node {
    public Node() {

    }

    public Tree tree = null;
    public boolean isLeafNode = false;
    public boolean isEndpoint = false;
    public Node parent = null;
    public Rectangle mbr = null;
    public Vector<Node> childrenNodes = null;
    public Integer index = new Integer(-1);

    public Node splitNode(Node newNode) {
        return new Node();
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
        return new Node();
    }

    // TODO
    public Node parent() {
        return new Node();
    }

    // TODO
    public void addChild(Node newChild, int dunno) {

    }
}
