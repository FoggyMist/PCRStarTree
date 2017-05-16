package trees.rtree;

import trees.tree.*;
import trees.rectangle.*;
import java.util.Vector;

public class RNode extends Node {
    public RNode(Tree t) {
        super(t);
    }


    @Override
    public RNode splitNode(Node n) { // quadric split
        Vector<Node> nodes = new Vector<Node>(tree.M + 1);
        nodes.addAll(childrenNodes);
        nodes.add(n);
        // QS1.1: [pick first entry for each group]
        // apply algorithm pickSeeds to choose two entries
        // to be first elements of the groups
        Node maxNode1 = null;
        Node maxNode2 = null;
        double maxiInefficiency = -1;
        Rectangle composedRect = null;

        for(int a = 0; a <= tree.M; a++) {
            for(int b = a + 1; b <= tree.M; b++) {
                // PS1: [calculate inefficiency of grouping nodes together]
                // for each pair of nodes (node1) and (node2)
                Node node1 = nodes.get(a);
                Node node2 = nodes.get(b);
                // compose rectangle (composedRect) including (node1.mbr) and (node2.mbr)
                composedRect = Rectangle.enlarge(node1.mbr, node2.mbr);
                // calcuate (inefficiency) = area(composedRect) - area(node1.mbr) - area(node2.mbr)
                double inefficiency = composedRect.area() - node1.mbr.area() - node2.mbr.area();
                // PS2: [choose the most wasteful pair]
                // choose the pair with the largest d
                if(inefficiency > maxiInefficiency) {
                    maxNode1 = node1;
                    maxNode2 = node2;
                    maxiInefficiency = inefficiency;
                }
            }
        }

        // QS1.2: [pick first entry for each group]
        // assign each to a group
        RNode group1 = new RNode(tree);
        RNode group2 = new RNode(tree);
        group1.parent = parent;
        group2.parent = parent;
        group1.add(maxNode1);
        group2.add(maxNode2);
        nodes.remove(maxNode1);
        nodes.remove(maxNode2);
        // QS2: [check if done]
        while(true) {
            // if all entries have been assigned, stop
            if(nodes.size() == 0) {
                break;
            }

            // if one group has so few entries that all the rest must be assigned
            // to it  in order for it to have the minimuim number m, assign them and stop
            if(group1.size() + nodes.size() == tree.m) {
                group1.addAll(nodes);
                break;
            }

            if(group1.size() + nodes.size() == tree.m) {
                group1.addAll(nodes);
                break;
            }

            // QS3.1: [select entry to assign]
            // invoke algorithm pickNext to choose the next entry to assign
            // PN1: [determine cost of putting each entry in each group]
            double maximumPreference = -1;
            Node preferedGroup = null;
            Node addingNode = null;
            // for each entry (spareNode) no yet in the group
            for(Node spareNode : nodes) {
                // calculate (coverage1) = the area increase required in the covering
                // rectangle of (group1) to include (spareNode)
                double coverage1 = Rectangle.enlarge(group1.mbr, spareNode.mbr).area();
                double coverage2 = Rectangle.enlarge(group2.mbr, spareNode.mbr).area();
                // PN2: [find entry with greatest preference for one group]
                // choose any entry with maximum difference between coverages
                double difference = Math.abs(coverage1 - coverage2);
                if(difference > maximumPreference) {
                    maximumPreference = difference;
                    addingNode = spareNode;
                    if(coverage1 < coverage2) {
                        preferedGroup = group1;
                    } else if(coverage1 > coverage2) {
                        preferedGroup = group2;
                    } else {
                        preferedGroup = null;
                    }
                }
            }

            // QS3.2:
            // add it to the group whose covering rectangle will have to
            // be enlarged least to accomodate it
            if(preferedGroup != null) {
                preferedGroup.add(addingNode);
            } else {
                // resolve tries by adding the entry to the group with smaller area,
                // then to one with fewer entries, then to either
                if(group1.mbr.area() == group2.mbr.area()) {
                    if(group1.size() < group2.size()) {
                        group1.add(addingNode);
                    } else {
                        group2.add(addingNode);
                    }
                } else if(group1.mbr.area() < group2.mbr.area()) {
                    group1.add(addingNode);
                } else { // node1.mbr.area() > node2.mbr.area()
                    group2.add(addingNode);
                }
            }

            nodes.remove(addingNode);
            // repeat from QS2
        }

        childrenNodes.clear();
        addAll(group1.childrenNodes);
        mbr = group1.mbr;
        return group2;
    }
}
