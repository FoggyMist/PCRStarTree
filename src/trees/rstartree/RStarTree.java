package trees.rstartree;

import trees.tree.*;
import trees.rtree.*;
import trees.rectangle.*;
import java.util.*;

public class RStarTree extends RTree {
    public RStarTree(int m, int M) {
        super(m, M);
    }

    @Override
    public void initializeRoot() {
        root = new RStarNode(this);
    }

    @Override
    public void insert(int index, Rectangle r) {
        // System.out.println("--- new insert started ---");
        // System.out.println("inserting id: " + index +"; size: " + r);
        Node newNode = new RStarNode(this);
        newNode.index = new Integer(index);
        newNode.mbr = r;

        int height = height();
        int level = height - 1;
        overflowsDone = new Vector<Boolean>(height);
        for(int a = 0; a < height; a++) {
            overflowsDone.add(false);
        }
        insert(index, r, newNode, level);
    }

    Vector<Boolean> overflowsDone;

    public void insert(int index, Rectangle r, Node newNode, int level) {
        // System.out.println(level + " insert(" + index +";" + r +";" + newNode +";" + level);
        // I1: invoke chooseLeaf(), with the level as a parameter,
        // to find an appropriate (node), in witch to place the (newNode)
        Node node = chooseLeaf(root, r, level);
        // System.out.println("choosen leaf: " + node);

        // \GO TO THE RIGHT LEVEL\
        int treeLevel = height() - 1;
        while(treeLevel > level) {
            node = node.parent;
            treeLevel--;
        }

        // System.out.println("rolled up the choosen leaf to: " + node);


        // I2: if (node) has less than (M) entries, accomodate (newNode) in (node)
        if(node.childrenNodes.size() < M) {
            node.add(newNode);
            return;
        } else {
            // if (node) has (M) entries, invoke overflowTreatment()
            // with the level of (node) as parameter [for reinsertion or split]

            Node overflowResult = overflowTreatment(level, node, newNode);
            // I3: if overflowTreatment() was called and split was performed,
            // propagate overflowTreatment() upwards if necessary,
            // if overflowTreatment() cause a split of the root, create new root
            while(overflowResult != null && level >= 0) {
                if(node.parent == null) {
                    riseTreeLevel(node, overflowResult);
                    overflowResult = null;
                } else {
                    node = node.parent;
                    level--;
                    if(node.childrenNodes.size() < M) {
                        node.add(overflowResult);
                        overflowResult = null;
                    } else {
                        overflowResult = overflowTreatment(level, node, overflowResult);
                    }
                }
            }

        }

        // I4: adjust mbrs... \ALREADY DONE\
    }


    public Node overflowTreatment(int level, Node node, Node newNode) {
        Node nn = null;
        // OT1: if the level is not the root level
        // and this is the first call of overflowTreatment() in the given level
        // during the insertion of one data rectangle
        if(level != 0 && !overflowsDone.get(level)) {
            // then invoke reInsert()
            overflowsDone.set(level, true);
            reInsert(node, newNode, level);
        } else {
            // else invoke splitNode()
            nn = node.splitNode(newNode);
        }

        return nn;
    }

    public void reInsert(Node node, Node newNode, int level) {
        Vector<Node> subNodes = new Vector<Node>(M + 1);
        subNodes.addAll(node.childrenNodes);
        subNodes.add(newNode);
        // RI1: for all (M)+1 entries of a node, compute the distance
        // between the centers of thier rectangles and the center of (node.mbr)
        // RI2: sort the entries in decreasing order of thier distances
        double[] centerPoint = node.mbr.center();
        Collections.sort(subNodes, (Node n1, Node n2) -> {
            double d1 = Rectangle.distance(n1.mbr.center(), centerPoint);
            double d2 = Rectangle.distance(n2.mbr.center(), centerPoint);
            return (int)Math.signum(d1 - d2);
        });
        // RI3: remove the first (p = 30%) entries from (node) and adjust node(mbr)
        int p = (int)Math.ceil(0.3 * M);
        Vector<Node> removedNodes = new Vector<Node>(p);
        for(int a = 0; a < p; a++) {
            Node movedNode = subNodes.get(0);
            subNodes.remove(0);
            removedNodes.add(movedNode);
        }

        node.childrenNodes.clear();
        node.mbr = subNodes.get(0).mbr;
        for(Node child : subNodes) {
            node.add(child);
            // node.mbr = Rectangle.enlarge(node.mbr, child.mbr);
        }

        // RI4: in the sort defined in RI2, starting with maximum distance
        // (for far reInsert) or minimum distance (for close reInsert),
        // invoke insert to reinsert the entries
        for(Node reinsertingNode : removedNodes) {
            insert(reinsertingNode.index, reinsertingNode.mbr, reinsertingNode, level);
        }
    }

    @Override
    public void riseTreeLevel(Node n, Node nn) {
        root = new RStarNode(this);
        root.add(n);
        root.add(nn);
    }

    // a.k.a. chooseSubtree
    @Override
    public Node chooseLeaf(Node node, Rectangle r, int level) {
        // CS1: done by CL1 from rTree
        // CS2: if (node) is leaf, return (node)
        while(!node.isLeafNode()) {
            // if the childpointers point to leaves [determine the minimuim
            // overlap cost]
            if(node.childrenNodes.get(0).isLeafNode()) {
                double smallestOverlapValue = Double.POSITIVE_INFINITY;
                Node smallestOverlapNode = null;
                // choose the entry in (node) whose rectangle needs
                // least overlap enlargement to include the new data rectangle
                for(Node child : node.childrenNodes) {
                    double currentOverlapValue = child.overlap(r);
                    if(currentOverlapValue < smallestOverlapValue) {
                        smallestOverlapValue = currentOverlapValue;
                        smallestOverlapNode = child;
                    } else if(currentOverlapValue == smallestOverlapValue) {
                        // resolve ties by choosing the entry
                        // whose rectangle needs least area enlargement
                        double smallestAreaEnlargement = Rectangle.enlarge(smallestOverlapNode.mbr, r).area() - smallestOverlapNode.mbr.area();
                        double currentAreaEnlargement = Rectangle.enlarge(child.mbr, r).area() - child.mbr.area();
                        if(currentAreaEnlargement < smallestAreaEnlargement) {
                            smallestOverlapNode = child;
                        } else if(currentAreaEnlargement == smallestAreaEnlargement) {
                            // then the entry with the rectangle of smallest area
                            if(child.mbr.area() < smallestOverlapNode.mbr.area()) {
                                smallestOverlapNode = child;
                            }
                        }
                    }
                }

                node = smallestOverlapNode; // CS3
            } else {
                // if the childpointers in (node) do not point to leaves
                // [determine the minimum area cost] chose the entry in (node)
                // whose rectangle needs least area enlargement
                // to include the new data rectangle
                double smallestEnlargementValue = Double.POSITIVE_INFINITY;
                Node smallestEnlargementNode = null;
                for(Node child : node.childrenNodes) {
                    double currentEnlargement = Rectangle.enlarge(child.mbr, r).area() - child.mbr.area();
                    if(currentEnlargement < smallestEnlargementValue) {
                        smallestEnlargementValue = currentEnlargement;
                        smallestEnlargementNode = child;
                    } else if(currentEnlargement == smallestEnlargementValue) {
                        // resolve ties by choosing the entry with the rectangle of smallest area
                        if(child.mbr.area() < smallestEnlargementNode.mbr.area()) {
                            smallestEnlargementValue = currentEnlargement;
                            smallestEnlargementNode = child;
                        }
                    }
                }

                node = smallestEnlargementNode; // CS3
            }

            // CS3: set (node) to be childnode pointed to by
            // the childpointer of the chosen entry and repeat from CS2
        }

        return node;
    }

}
