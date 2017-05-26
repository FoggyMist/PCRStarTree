package trees.rtree;

import trees.tree.*;
import trees.rectangle.*;
import java.util.Vector;

public class RTree extends Tree {
    public RTree(int m, int M) {
        super(m, M);
        initializeRoot();
    }

    public void initializeRoot() {
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

        insert(index, r, newNode);
    }

    public void insert(int index, Rectangle r, Node newNode) {
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

        return chooseLeaf(node, r, 0);
    }

    public Node chooseLeaf(Node node, Rectangle r, int level) {
        // CL2: [leaf check]
        // if (node) is leaf, return (node)
        if(node.isLeafNode()) {
            return node;
        } else { // CL3: [choose subtree]
            // if (node) is not a leaf, let (candidate) be the entry in (node)
            // whose rectangle (mbr) needs least enlargement to include (r)
            // resolve tries by choosing the netry with the rectangle
            // of smallest area

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

            return chooseLeaf(choice, r, 0);
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

            // \PROBABLY ALREADY DONE IN splitNode() and node.add()\
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
            riseTreeLevel(n, nn);
        }
    }

    public void riseTreeLevel(Node n, Node nn) {
        root = new RNode(this);
        root.add(n);
        root.add(nn);
    }

    public void delete(int index, Rectangle r) {
        // D1: [find node containing record]
        // invoke findLeaf() to locate the leaf node (l) containing (index)
        Node leaf = findLeaf(index, r, root);
        // stop if record was not found
        if(leaf == null) {
            System.out.println("no record found");
            return;
        }

        // D2: [delete record]
        // remove (index) from (leaf)
        for(Node child : leaf.childrenNodes) {
            if(child.index == index) {
                leaf.remove(child);
                break;
            }
        }

        // D3: [propagate changes]
        // invoke condenseTree, pasing (leaf)
        condenseTree(leaf);

        // D4: [shorten tree]
        // if the root node has only one child after the tree has been adjusted,
        if(root.childrenNodes.size() == 1) {
            // make the child the new root
            Node newRoot = root.childrenNodes.get(0);
            newRoot.parent = null;
            root = newRoot;
        }
    }

    public Node findLeaf(int index, Rectangle r, Node node) {
        Node result = null;
        // FL1: [search subtrees]
        // if (node) is not a leaf
        if(!node.isLeafNode()) {
            // check each entry (child) in (node)
            for(Node child : node.childrenNodes) {
                // to deremine if (child.mrb) overlaps (r)
                if(child.mbr.isOverlapping(r)) {
                    // for each such enty invoke findLeaf() on the tree
                    // whose root is pointed to by (child)
                    Node tmpResult = findLeaf(index, r, child);
                    // until (index) is found or all etnries have been checked
                    while (tmpResult != null && !tmpResult.isLeafNode()) {
                        tmpResult = findLeaf(index, r, tmpResult);
                    }

                    if(tmpResult != null) {
                        result = tmpResult;
                    }
                }
            }
        } else {
            // FL2: [search leaf node for record]
            // if (node) is a leaf
            // check each entry to see if it matches (index)
            for(Node child : node.childrenNodes) {
                if(child.index == index) {
                    // if (index) is found return (node)
                    return node;
                }
            }
        }

        return result;
    }

    public void condenseTree(Node leaf) {
        // CT1: [initliazie]
        // set (node) = (leaf)
        Node node = leaf;
        // set (q) the set of eliminated nodes, to be empty
        Vector<Node> q = new Vector<Node>();
        // CT2: [find parent entry]
        // if (node) is the root go to CT6
        while(node != root) {
            // otherwise let (p) be the parent of (node)
            Node p = node.parent;
            // and let (child) be (node)'s entry in (p)
            // \DOESNT MAKE SENSE IN OOP/JAVA, ITS JUST NODE = NODE\
            // CT3: [eliminate under-full node]
            // if (node) has fewer than (m) entries
            if(node.childrenNodes.size() < m) {
                // delete (node) from (p)
                p.remove(node);
                // and add (node) to (q) set
                q.add(node);
            } else {
                // CT4: [adjust covering rectangle]
                // if (node) has not been eliminated, adjust (node.mbr)
                // to tightly contain all netries in (node)
                Rectangle tighterMbr = node.childrenNodes.get(0).mbr;
                for(Node child : node.childrenNodes) {
                    tighterMbr = Rectangle.enlarge(tighterMbr, child.mbr);
                }

                node.mbr = tighterMbr;
            }

            // CT5: [move up one level in tree]
            // set (node) = (p) and repeat from CT2
            node = p;
        }

        // CT6: [re-insert orphaned entries]
        // re-insert all entries of nodes in set(q)
        for(Node orphan : q) {
            if(orphan.index < 1) {
                // r*tree uses this method and propably requires level to be calculated
                System.out.println("reinserting node with negative index in delete procedure!");
            }

            insert(orphan.index, orphan.mbr);
        }
    }
}
