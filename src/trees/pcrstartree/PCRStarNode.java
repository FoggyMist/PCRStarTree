package trees.pcrstartree;

import trees.rectangle.*;
import java.util.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import trees.pcrstartree.util.AggregateController;

public class PCRStarNode implements Serializable {
    public static Random rng = new Random();
    public static int pointerByteSize = 2;
    public static int childrenNodeByteSize = 22; // 4 length + 9x2 adresses
    // parentPointer, childrenNodes, index, treePointer, value, rectangle,
    // overflowTreatment, depth = 2x pointers, 2x int, 1x double 1x full vector/array
    // = 2x2 + 2x4 + 8 + (4 + 2x tree.leafNodeSize+1) = 20 + (4 + 9x2) = 42
    public static int byteSize = 50;
    public static int uniqueNodeId = -1;

	public AggregateController aggregateController = new AggregateController();
    private double value = 0;
    public double getValue() { return value; }
    public void setValue(double newVal) {
        value = newVal;
        // updateAggregates()
        // parent.updateAggregates()
    }

    public PCRStarNode(PCRStarTree t, int id, double val) {
        tree = t;
        index = new Integer(id);
        value = val;
        mbr = new Rectangle(0, 0, 0, 0);
        childrenNodes = new Vector<PCRStarNode>(tree.M);
        readCount.put(index, 0);
        writeCount.put(index, 0);
        readByte.put(index, 0);
        writeByte.put(index, 0);
        writting(byteSize);
    }


    public PCRStarNode(PCRStarTree t, int id) {
        this(t, id, 0);
    }

    public PCRStarNode(PCRStarTree t) {
        this(t, --PCRStarNode.uniqueNodeId);
    }

    public PCRStarTree tree = null;
    public PCRStarNode parent = null;
    public Rectangle mbr = null;
    public Vector<PCRStarNode> childrenNodes = null;
    public Integer index;

    public int aggregateNumberOfLeafNodes = 0;
    public int aggregateNumberOfNonLeafNodes = 0;

    public static HashMap<Integer, Integer> readCount = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Integer> writeCount = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Integer> readByte = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Integer> writeByte = new HashMap<Integer, Integer>();

    public void reading(int change) {
        increment(readCount, readByte, change);
    }

    private void writting(int change) {
        increment(writeCount, writeByte, change);
    }

    public void increment(
        HashMap<Integer, Integer> counter,
        HashMap<Integer, Integer> byteCounter,
        int byteChange
    ) {
        int currentCount = counter.get(index) + 1;
        counter.put(index, currentCount);

        int currentByte = byteCounter.get(index) + byteChange;
        byteCounter.put(index, currentByte);
    }

    int depth = 0;

    public PCRStarNode insertNode(PCRStarNode insertingNode, int insertDepth) {
        depth = 0;
        PCRStarNode currentNode = this;

        while(depth < insertDepth) {
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

        reading(childrenNodeByteSize);
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
        int axis = chooseSplitAxis();
        int splitIndex = chooseSplitIndexInt(axis);

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
        for(int axis = 0; axis < 2; axis++) { // this implementation supports only 2d rectangles
            // sort the entries by the lower, then by upper value of thier rectangles
            // and determine all distributions as described:
            // margin-value = margin[mbr(firstGroup)] + margin[mbr(secondGroup)]

            sortByDimension(axis);

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
            // overlap-value = area[mbr(firstGroup) \intersection\ mbr(secondGroup)]
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
            node.reading(childrenNodeByteSize);
            for(PCRStarNode child : node.childrenNodes) {
                child.reading(Rectangle.byteSize);
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
            node.reading(childrenNodeByteSize);
            for(PCRStarNode child : node.childrenNodes) {
                child.reading(Rectangle.byteSize);
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
            reading(childrenNodeByteSize);
            Rectangle newMbr = new Rectangle(childrenNodes.get(0).mbr);
            for(PCRStarNode child : childrenNodes) {
                child.reading(Rectangle.byteSize);
                newMbr = Rectangle.enlarge(newMbr, child.mbr);
            }

            mbr = newMbr;
            writting(Rectangle.byteSize);
        }

        if(parent != null) {
            parent.condenseTree();
        }
    }

    public void updateMbr(Rectangle newRect) {
        mbr = Rectangle.enlarge(mbr, newRect);
        writting(Rectangle.byteSize);

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
            writting(Rectangle.byteSize);
        }
        // to add an element only array pointer and (int)length are required
        reading(pointerByteSize + 4);
        writting(pointerByteSize + 4);
        childrenNodes.add(n);
        n.parent = this;
        n.writting(pointerByteSize);
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
        writting(pointerByteSize + 4);
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
        reading(childrenNodeByteSize);
        for(PCRStarNode child : childrenNodes) {
            child.reading(Rectangle.byteSize);
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
        node.reading(pointerByteSize);
        while(node != null && node.childrenNodes.size() > 0) {
            node = node.childrenNodes.get(0);
            node.reading(pointerByteSize);
            height++;
        }

        return height;
    }

    public boolean isLeafNode() {
        reading(pointerByteSize + 4); // array + it's length
        if(childrenNodes.size() == 0) {
            return true;
        } else {
            childrenNodes.get(0).reading(pointerByteSize + 4);
            return childrenNodes.get(0).childrenNodes.size() == 0;
        }
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

        reading(childrenNodeByteSize);
        for(PCRStarNode child : childrenNodes) {
            child.reading(Rectangle.byteSize);
        }
    }

    public PCRStarNode search(Rectangle r) {
        reading(Rectangle.byteSize);
        if(this.mbr.isOverlapping(r)) {
            if(this.isLeafNode()) {
                return this;
            }

            reading(childrenNodeByteSize);
            for(PCRStarNode child : childrenNodes) {
                PCRStarNode nodeFound = child.search(r);
                if(nodeFound != null) {
                    return nodeFound;
                }
            }
        }

        return null;
    }

    public Vector<PCRStarNode> wideSearch(Rectangle r) {
        Vector<PCRStarNode> results = new Vector<PCRStarNode>();

        reading(Rectangle.byteSize);
        if(this.mbr.isOverlapping(r)) {
            reading(childrenNodeByteSize);

            if(this.isLeafNode()) {
                for(PCRStarNode child : childrenNodes) {
                    if(child.mbr.isOverlapping(r)) {
                        results.add(child);
                    }
                }

                return results;
            }

            for(PCRStarNode child : childrenNodes) {
                child.reading(Rectangle.byteSize);
                if(child.mbr.isOverlapping(r)) {
                    results.addAll(child.wideSearch(r));
                }
            }
        }

        return results;
    }
	
	public double checkValueFor(String aggregate) {
		return aggregateController.checkValueFor(aggregate);
	}
	
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.#");
        String parentId;
        if(parent == null) {
            parentId = "NONE, this is root";
        } else {
            parentId = parent.index.toString();
        }
        return "id: " + index + " | value: " + df.format(value) + " | rect: " + mbr
        + " | parent: " + parentId + " | has " + childrenNodes.size() + " children"
        // + " | (aggregates) nodes: " + aggregateNumberOfNonLeafNodes
        // + ", leaves: " + aggregateNumberOfLeafNodes;
        + " | (access) read: " + readCount.get(index)
        + ", write: " + writeCount.get(index)
        + " | (byte) read: " + readByte.get(index)
        + ", write: " + writeByte.get(index);
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
