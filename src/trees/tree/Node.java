package trees.tree;

import trees.rectangle.*;
import java.util.Vector;

public class Node {
    public static int uniqueRootId = -1;

    public Node(Tree t) {
        tree = t;
        childrenNodes = new Vector<Node>(tree.M);
        mbr = new Rectangle(0, 0, 0, 0);
        index = new Integer(--Node.uniqueRootId);
    }

    public Tree tree = null;
    public Node parent = null;
    public Rectangle mbr = null;
    public Vector<Node> childrenNodes = null;
    public Integer index;

    public Node splitNode(Node newNode) {
        return newNode;
    }


    public void add(Node n) {
        if(childrenNodes.size() == tree.M) {
            System.out.println("too many childrenNodes");
            return;
        }
        if(childrenNodes.size() == 0) {
            mbr = new Rectangle(n.mbr);
        }
        childrenNodes.add(n);
        n.parent = this;
        mbr = Rectangle.enlarge(mbr, n.mbr);
    }

    public void addAll(Vector<Node> nodes) {
        for(Node node : nodes) {
            add(node);
        }
    }

    public void remove(Node node) {
        childrenNodes.remove(node);
    }

    public double overlap(Rectangle r) {
        double result = 0;
        for(Node child : childrenNodes) {
            if(r != child.mbr) {
                Rectangle overlappingRect = Rectangle.shrink(child.mbr, r);
                if(overlappingRect != null) {
                    result += overlappingRect.area();
                }
            }
        }

        return result;
    }

    // TODO
    public Node insertNode(Node insertingNode, int insertDepth) {
        return insertingNode;
    }

    // TODO
    public boolean isLeafNode() {
        return (childrenNodes.size() == 0 || childrenNodes.get(0).childrenNodes.size() == 0);
    }

    // TODO
    public boolean isNonleafNode() {
        return !isLeafNode();
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

    public String toString() {
        String parentId;
        if(parent == null) {
            parentId = "isRoot";
        } else {
            parentId = parent.index.toString();
        }
        return "Node id: " + index + " | in rectangle: " + mbr
        + " | parent of " + parentId + " | has " + childrenNodes.size() + " children";
    }
}
