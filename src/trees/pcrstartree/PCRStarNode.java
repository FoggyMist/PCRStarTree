package trees.pcrstartree;

import trees.rstartree.*;
import trees.rectangle.*;
import trees.tree.*;
import java.util.*;


public class PCRStarNode {
    public static Random rng = new Random();

    public static int uniqueRootId = -1;

    public PCRStarNode(PCRStarTree t) {
        tree = t;
        childrenNodes = new Vector<PCRStarNode>(tree.M);
        mbr = new Rectangle(0, 0, 0, 0);
        index = new Integer(--Node.uniqueRootId);
    }

    public PCRStarTree tree = null;
    public PCRStarNode parent = null;
    public Rectangle mbr = null;
    public Vector<PCRStarNode> childrenNodes = null;
    public Integer index;

    public int aggregateNumberOfLeafNodes = 0;
    public int aggregateNumberOfNonLeafNodes = 0;


    int depth = 0;

    public PCRStarNode insertNode(PCRStarNode insertingNode, int insertDepth) {
        depth = 0;
        PCRStarNode currentNode = this;
        // System.out.println(depth + " | " + insertDepth);

        while(depth < insertDepth) {
            // System.out.println(depth + " | " + insertDepth);
            currentNode = currentNode.chooseSubTree(insertingNode.mbr);
            depth = depth + 1;
        }

        while(insertingNode != null && currentNode != null) {
            currentNode.forceAdd(insertingNode);
            if(
                (currentNode.isLeafNode() && currentNode.childrenNodes.size() > tree.leafNodeSize)
                || (currentNode.isNonleafNode() && currentNode.childrenNodes.size() > tree.nonleafNodeSize)
            ) {
                insertingNode = currentNode.overflowTreatment();
            } else {
                insertingNode = null;
            }

            currentNode = currentNode.parent;
        }

        return insertingNode;
    }

    int overflowTreatmentCount = 0;

    public PCRStarNode overflowTreatment() {
        if(overflowTreatmentCount < 0) {
            System.out.println("overflowTreatmentCount < 0 assert failed");
        }

        PCRStarNode splitResult = null;

        overflowTreatmentCount += 1;
        if(overflowTreatmentCount == 1 && parent != null) {
            reInsert();
        } else {
            splitResult = split();
        }

        return splitResult;
    }

    public PCRStarNode split() {
        PCRStarNode splitNode = new PCRStarNode(tree);
        // System.out.println("Split:");
        int axis = chooseSplitAxis();
        int splitIndex = chooseSplitIndexInt(axis);
        // System.out.println("size " + childrenNodes.size());


        for(int a = childrenNodes.size() - 1; a >= splitIndex; a--) {
            PCRStarNode transferNode = childrenNodes.get(splitIndex);
            childrenNodes.remove(transferNode);
            updateCountAggregates(transferNode, -1);
            splitNode.add(transferNode);
        }

        // update parent MBRs
        condenseTree();

        return splitNode;
    }

    public void reInsert() {
        Vector<PCRStarNode> reinsertList = new Vector<PCRStarNode>();
        for(int i = childrenNodes.size() - tree.M; i < childrenNodes.size(); i++) {
            PCRStarNode transferNode = childrenNodes.get(i);
            childrenNodes.remove(transferNode);
            updateCountAggregates(transferNode, -1);
            reinsertList.add(transferNode);
        }

        // update parent MBRs
        condenseTree();

        while(reinsertList.size() > 0) {
            PCRStarNode transferNode = reinsertList.get(0);
            reinsertList.remove(0);

            PCRStarNode  newNode = tree.root.insertNode(transferNode, tree.height() - height() - 0);
            if(newNode != null) {
                tree.addRoot(newNode);
            }
        }
    }

    public int chooseSplitAxis() {
        // CSA1: for each axis
        double lowestS = Double.POSITIVE_INFINITY;
        int splitAxis = -1;
        // System.out.println("---");
        for(int axis = 0; axis < 2; axis++) { // this implementation supports only 2d rectangles
            // sort the entries by the lower, then by upper value of thier rectangles
            // and determine all distributions as described:
            // margin-value = margin[mbr(firstGroup)] + margin[mbr(secondGroup)]

            // System.out.println("before sorting:");
            // for(PCRStarNode child: childrenNodes) {
            //     System.out.println(child);
            // }

            sortByDimension(axis);

            // System.out.println("after sorting:");
            // for(PCRStarNode child: childrenNodes) {
            //     System.out.println(child);
            // }

            // compute (s), the sum of all margin-values of different distributions
            double s = 0;
            Vector<PCRStarNode> firstGroup = new Vector<PCRStarNode>(tree.M - 1);
            Vector<PCRStarNode> secondGroup = new Vector<PCRStarNode>(tree.M - 1);
            secondGroup.addAll(childrenNodes);
            for(int a = 0; a < tree.m; a++) {
                PCRStarNode transferNode = childrenNodes.get(a);
                firstGroup.add(transferNode);
                secondGroup.remove(transferNode);
            }

            // firstGroup.add(childrenNodes.get(0));
            // secondGroup.addAll(childrenNodes);
            // secondGroup.remove(childrenNodes.get(0));

            int loopEnd = childrenNodes.size() - tree.m;
            for(int a = tree.m; a < loopEnd; a++) {
                Rectangle firstMbr = new Rectangle(firstGroup.get(0).mbr);
                Rectangle secondMbr = new Rectangle(secondGroup.get(0).mbr);
                for(PCRStarNode firstGroupNode : firstGroup) {
                    firstMbr = Rectangle.enlarge(firstGroupNode.mbr, firstMbr);
                }
                for(PCRStarNode secondGroupNode : secondGroup) {
                    secondMbr = Rectangle.enlarge(secondGroupNode.mbr, secondMbr);
                }

                double margin = mbr.area() - firstMbr.area() - secondMbr.area();
                s += margin;
                // move to next distribution,
                // take first node from second group and transfer it to first
                PCRStarNode n = secondGroup.get(0);
                secondGroup.remove(n);
                firstGroup.add(n);
            }

            // CSA2: choose the axis with the minimum (s) as (splitAxis)
            if(s < lowestS) {
                lowestS = s;
                splitAxis = axis;
            }
        }

        sortByDimension(splitAxis);

        // System.out.println("splitAxis " + splitAxis);
        return splitAxis;
    }

    public int chooseSplitIndexInt(int axis) {
        // CSI1: along the chosen split axis, choose the distribution
        // with the minimum overlap-value, resolve ties by choosing the distribution
        // with minimum area-value

        int splitIndex = -2;
        double minimumOverlap = Double.POSITIVE_INFINITY;
        Vector<PCRStarNode> firstGroup = new Vector<PCRStarNode>(tree.M - 1);
        Vector<PCRStarNode> secondGroup = new Vector<PCRStarNode>(tree.M + 1);
        Rectangle minimumMbr1 = null;
        Rectangle minimumMbr2 = null;

        secondGroup.addAll(childrenNodes);
        for(int a = 0; a < tree.m; a++) {
            PCRStarNode transferNode = childrenNodes.get(a);
            firstGroup.add(transferNode);
            secondGroup.remove(transferNode);
        }

        int loopEnd = childrenNodes.size() - tree.m;
        for(int a = tree.m; a < loopEnd; a++) {
            Rectangle firstMbr = new Rectangle(firstGroup.get(0).mbr);
            Rectangle secondMbr = new Rectangle(secondGroup.get(0).mbr);
            for(PCRStarNode firstGroupNode : firstGroup) {
                firstMbr = Rectangle.enlarge(firstGroupNode.mbr, firstMbr);
            }
            for(PCRStarNode secondGroupNode : secondGroup) {
                secondMbr = Rectangle.enlarge(secondGroupNode.mbr, secondMbr);
            }

            double overlap = 0;
            // overlap-value = area[mbr(firstGroup) ∩ mbr(secondGroup)]
            Rectangle intersection = Rectangle.shrink(firstMbr, secondMbr);
            if(intersection != null) {
                overlap = intersection.area();
            }

            if(overlap < minimumOverlap) {
                minimumOverlap = overlap;
                splitIndex = a;
                minimumMbr1 = new Rectangle(firstMbr);
                minimumMbr2 = new Rectangle(secondMbr);
            } else if(overlap == minimumOverlap) {
                // area = area[mbr(firstGroup)] + area[mbr(secondGroup)]
                double currentArea = firstMbr.area() + secondMbr.area();
                double minimumArea = minimumMbr1.area() + minimumMbr2.area();
                if(currentArea < minimumArea) {
                    splitIndex = a;
                    minimumMbr1 = new Rectangle(firstMbr);
                    minimumMbr2 = new Rectangle(secondMbr);
                }
            }

            // move to next distribution,
            // take first node from second group and transfer it to first
            PCRStarNode n = secondGroup.get(0);
            secondGroup.remove(n);
            firstGroup.add(n);
        }

        // System.out.println("splitIndex " + splitIndex);
        return splitIndex;
    }


    public PCRStarNode chooseSubTree(Rectangle r) {
        PCRStarNode node = this;
        if(childrenNodes.size() == 0) {
            return node;
        }

        // if the childpointers point to leaves [determine the minimuim
        // overlap cost]
        if(node.childrenNodes.get(0).isLeafNode()) {
            double smallestOverlapValue = Double.POSITIVE_INFINITY;
            PCRStarNode smallestOverlapNode = null;
            // choose the entry in (node) whose rectangle needs
            // least overlap enlargement to include the new data rectangle
            for(PCRStarNode child : node.childrenNodes) {
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
            PCRStarNode smallestEnlargementNode = null;
            for(PCRStarNode child : node.childrenNodes) {
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

        return node;
    }

    public void condenseTree() {
        if(childrenNodes.size() > 0) {
            Rectangle newMbr = new Rectangle(childrenNodes.get(0).mbr);
            for(PCRStarNode child : childrenNodes) {
                newMbr = Rectangle.enlarge(newMbr, child.mbr);
            }

            mbr = newMbr;
        }

        if(parent != null) {
            parent.condenseTree();
            // System.out.println("condensing: " + parent);
        }
    }

    public void updateMbr(Rectangle newRect) {
        mbr = Rectangle.enlarge(mbr, newRect);

        if(parent != null) {
            parent.updateMbr(newRect);
        }
    }

    public void add(PCRStarNode n) {
        if(isLeafNode()) {
            if(childrenNodes.size() == tree.leafNodeSize) {
                System.out.println("too many childrenNodes");
                return;
            }
        } else {
            if(childrenNodes.size() == tree.nonleafNodeSize) {
                System.out.println("too many childrenNodes");
                return;
            }
        }

        forceAdd(n);
    }

    public void forceAdd(PCRStarNode n) {
        if(childrenNodes.size() == 0) {
            mbr = new Rectangle(n.mbr);
        }
        childrenNodes.add(n);
        n.parent = this;
        updateMbr(n.mbr);
        updateCountAggregates(n, 1);
    }

    public void addAll(Vector<PCRStarNode> nodes) {
        for(PCRStarNode node : nodes) {
            add(node);
        }
    }

    public void remove(PCRStarNode node) {
        childrenNodes.remove(node);
        updateCountAggregates(node, -1);
    }

    private void updateCountAggregates(PCRStarNode transferNode, int changeDirection) {
        if(transferNode.aggregateNumberOfLeafNodes == 0) {
            aggregateNumberOfLeafNodes += changeDirection;
        } else {
            aggregateNumberOfNonLeafNodes += (transferNode.aggregateNumberOfNonLeafNodes + 1) * changeDirection;
            aggregateNumberOfLeafNodes += (transferNode.aggregateNumberOfLeafNodes) * changeDirection;
        }

        if(parent != null) {
            parent.updateCountAggregates(transferNode, changeDirection);
        }
    }

    public double overlap(Rectangle r) {
        double result = 0;
        for(PCRStarNode child : childrenNodes) {
            if(r != child.mbr) {
                Rectangle overlappingRect = Rectangle.shrink(child.mbr, r);
                if(overlappingRect != null) {
                    result += overlappingRect.area();
                }
            }
        }

        return result;
    }

    public int height() {
        int height = 0;
        PCRStarNode node = this;
        while(node != null && node.childrenNodes.size() > 0) {
            node = node.childrenNodes.get(0);
            height++;
        }

        return height;
    }

    public boolean isLeafNode() {
        return (childrenNodes.size() == 0 || childrenNodes.get(0).childrenNodes.size() == 0);
    }

    public boolean isNonleafNode() {
        return !isLeafNode();
    }

    public void sortByDimension(int dimension) {
        Collections.sort(childrenNodes, (PCRStarNode n1, PCRStarNode n2) -> {
            if(n1.mbr.lowerLeftPoint[dimension] < n2.mbr.lowerLeftPoint[dimension]) {
                return -1;
            } else if(n1.mbr.lowerLeftPoint[dimension] > n2.mbr.lowerLeftPoint[dimension]) {
                return 1;
            } else { // n1 lower == n2 lower, then compare upper
                if(n1.mbr.upperRightPoint[dimension] < n2.mbr.upperRightPoint[dimension]) {
                    return -1;
                } else if(n1.mbr.upperRightPoint[dimension] > n2.mbr.upperRightPoint[dimension]) {
                    return 1;
                 } else { // n1 == n2
                    return 0;
                }
            }
        });
    }

    public PCRStarNode search(Rectangle r) {
        if(this.mbr.isOverlapping(r)) {
            if(this.isLeafNode()) {
                return this;
            }

            for(PCRStarNode child : childrenNodes) {
                PCRStarNode nodeFound = child.search(r);
                if(nodeFound != null) {
                    return nodeFound;
                }
            }
        }

        return null;
    }

    public Vector<Integer> wideSearch(Rectangle r) {
        Vector<Integer> results = new Vector<Integer>();

        if(this.mbr.isOverlapping(r)) {
            if(this.isLeafNode()) {
                for(PCRStarNode child : childrenNodes) {
                    if(child.mbr.isOverlapping(r)) {
                        results.add(child.index);
                    }
                }

                return results;
            }

            for(PCRStarNode child : childrenNodes) {
                if(child.mbr.isOverlapping(r)) {
                    results.addAll(child.wideSearch(r));
                }
            }
        }

        return results;
    }

    public String toString() {
        String parentId;
        if(parent == null) {
            parentId = "NONE, this is root";
        } else {
            parentId = parent.index.toString();
        }
        return "Node id: " + index + " | in rectangle: " + mbr
        + " | parent is " + parentId + " | has " + childrenNodes.size() + " children"
        + " | (aggregates) nodes: " + aggregateNumberOfNonLeafNodes
        + ", leaves: " + aggregateNumberOfLeafNodes;
    }

    public String toJSON() {
        String result = "";

        result += "{";
        result += "mbr: " + mbr.toJSON() + ",";
        result += "nodes: [";
        for(PCRStarNode child : childrenNodes) {
            result += child.toJSON() + ",";
        }
        result += "]}";

        return result;
    }
}
