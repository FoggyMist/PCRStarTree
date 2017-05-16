package trees.rtree;

import trees.tree.*;
import trees.rectangle.*;
import java.util.Vector;

public class RTree extends Tree {
    public RTree(int m, int M) {
        super(m, M);
        root = new RNode(this);
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

        // System.out.println("looking for ids in rectangle = " + r);

        // S1: if (n) is not a leaf
        if(!n.isLeafNode()) {
            // System.out.println("not a leaf");
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
            // System.out.println("found a leaf!");
            //check all entries (childNode)
            for(Node childNode : n.childrenNodes) {
                // System.out.println("it has a children");
                // to determine whether (childNode) overlaps (r)
                if(childNode.mbr.isOverlapping(r)) {
                    // System.out.println("and it is overlapping");
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

        // I1: [find position for new record]
        // invoke chooseLeaf() to select a leaf node in which to place (newNode)
        Node leaf = chooseLeaf(r);
        Node leaf2 = null;
        // I2: [add record to leaf node]
        // if (leaf) has room for another entry, install (newNode)
        if(leaf.childrenNodes.size() < M) {
            leaf.add(newNode);
        } else { // otherwise invoke splitNode() to obtain (leaf) and (leaf2)
            // containing (newNode) and all the old entries of (leaf)
            leaf2 = leaf.splitNode(newNode);
        }

        // I3: [propagate changes upward]
        // invoke adjustTree on (leaf),
        // also passing (leaf2) if split was preformed
        // I4: [grow tree taller] \DONE INSIDE adjustTree()\
        adjustTree(leaf, leaf2);

    }

    public Node chooseLeaf(Rectangle r) {
        // CL1: [initialize]
        // set (node) to be the root node
        Node node = root;

        return chooseLeaf(node, r);
    }

    public Node chooseLeaf(Node node, Rectangle r) {
        // CL2: [leaf check]
        // if (node) is leaf, return (node)
        if(node.isLeafNode()) {
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

    public void adjustTree(Node l, Node ll) {
        boolean hasSplitRoot = false;

        // AT1: [initialize]
        // set (n) = (l), if (l) was split previously set (nn) to be resulting second node
        Node n = l;
        Node nn = ll;
        // AT2: [check if done]
        // if (n) is the root, stop
        while(n != root) {
            // \PROPABLY ALREADY DONE IN splitNode() and node.add()\
            // AT3: [adjust covering rectangle in parent entry]
            // let (p) be the parent node of (n)
            Node p = n.parent;
            Node pp = null;
            // // let (en) be n-th entry in (p)
            // Rectangle newMbr = new Rectangle(n.childrenNodes.get(0).mbr));
            // for(int a = 1; a < n.childrenNodes.size(); a++) {
            //     // adjust (en.mbr) so that it tightly encloses all entry rectangles in (n)
            //     Node en = n.childrenNodes.get(a);
            //     newMbr = Rectangle.enlarge(newMbr, en.mbr);
            // }
            // n = newMbr;

            // \PROPABLY ALREADY DONE IN splitNode() and node.add()\
            // AT4: [propagate node split upward]
            // if (n) has a partner (nn) resulting from an earier split
            if(nn != null) {
                // create new entry (enn) with (enn.parent) pointing to (nn)
                // and (enn.mbr) enclosing all rectangles in (nn)
                // add (enn) to (p) if there is room
                if(p.childrenNodes.size() < M) {
                    p.add(nn);
                } else {
                    // otherwise invoke splitNode() to produce (p) and (pp)
                    // containing (enn) and all (p)'s old entries
                    pp = p.splitNode(nn);
                    if(p == root) {
                        hasSplitRoot = true;
                    }
                }
            }

            // AT5: [move up to next leve]
            // set (n) = (p) and set (nn) = (pp) if sa split occured
            n = p;
            nn = pp;

            // repeat from AT2
        }

        // I4: [grow tree taller]
        // if node split propagation cause the root to split
        // create new root whose children are two resulting nodes
        if(hasSplitRoot
            || (nn != null && n == root)
        ) {
            root = new RNode(this);
            root.add(n);
            root.add(nn);
        }
    }

}
