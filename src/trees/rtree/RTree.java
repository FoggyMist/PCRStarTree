package trees.rtree;

import trees.tree.*;
import trees.rectangle.*;
import java.util.Vector;

public class RTree extends Tree {
    public RTree(int m, int M) {
        super(m, M);
    }

    public Vector<Integer> search(Rectangle r) {
        Vector<Integer> results = new Vector<Integer>();

        if(root == null) {
            return results;
        }

        return search(root, r);
    }

    public Vector<Integer> search(Node n, Rectangle r) {
        Vector<Integer> results = new Vector<Integer>();

        // S1: if (n) is not a leaf
        if(!n.isLeafNode) {
            // check each entry (childNode)
            for(Node childNode : n.childrenNodes) {
                // to determine whether (childNode.mbr) overlaps (r)
                if(childNode.mbr.isOverlapping(r)) {
                    // for all overlapping entries, invoke search()
                    // on the tree whose root node is pointed to by (childNode)
                    results.addAll(search(childNode, r));
                }
            }
        } else { // S2: if (n) is a leaf
            //check all entries (childNode)
            for(Node childNode : n.childrenNodes) {
                // to determine whether (childNode) overlaps (r)
                if(childNode.mbr.isOverlapping(r)) {
                    // if so, (childNode.index) is qualifying record
                    results.add(new Integer(childNode.index));
                }
            }
        }

        return results;
    }

    public void insert(int index, Rectangle r) {
        Node newNode = new RNode(this);
        newNode.index = new Integer(index);
        newNode.mbr = r;
        newNode.isLeafNode = true;

        // I1: [find position for new record]
        // invoke chooseLeaf() to select a leaf node in which to place (newNode)
        Node leaf = chooseLeaf(r);

        // I2: [add record to leaf node]
        // if (leaf) has room for another entry, install (newNode)
        if(leaf.childrenNodes.size() < M) {
            leaf.childrenNodes.add(newNode);
        } else { // otherwise invoke splitNode() to obtain (leaf) and (leaf2)
            // containing (newNode) and all the old entries of (leaf)
            // Node leaf2 = leaf.splitNode(newNode);
        }
    }

    public Node chooseLeaf(Rectangle r) {
        // CL1: [initialize]
        // set (node) to be the root node
        Node node = root;

        if(node == null) {
            // TODO create new root
            return node;
        }

        return chooseLeaf(node, r);
    }

    public Node chooseLeaf(Node node, Rectangle r) {
        // CL2: [leaf check]
        // if (node) is leaf, return (node)
        if(node.isLeafNode) {
            return node;
        } else { // CL3: [choose subtree]
            // if (node) is not a leaf, let (candidate) be the entry in (node)
            // whose rectangle (mbr) needs least enlargement to include (r)
            // resolve tries by choosing the netry with the rectangle
            // of smalles area

            double lowestArea = Double.POSITIVE_INFINITY;
            Node choice = null;
            for(Node candidate : node.childrenNodes) {
                Rectangle enlargedRect = Rectangle.enlarge(candidate.mbr, r);
                double enlargedArea = enlargedRect.area();
                if(enlargedArea < lowestArea) {
                    lowestArea = enlargedArea;
                    choice = candidate;
                }
            }

            // CL3: [descend until a leaf is reached]
            // set (node) to be (choice)

            return chooseLeaf(choice, r);
        }
    }

}
