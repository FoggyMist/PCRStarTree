package trees.pcrstartree;

import trees.rectangle.*;
import trees.pcrstartree.util.DefaultHashMap;
import java.util.*;

public class PCRStarTree {

	public DefaultHashMap<String, Boolean> activeAggregates = null;

	public PCRStarTree(int m, int M) {
		this(m, M, null);
    }

	public PCRStarTree(int m, int M, Vector<String> aggregateList) {
		activeAggregates = new DefaultHashMap<String, Boolean>(false);
		for (String aggregate : aggregateList) {
			activeAggregates.put(aggregate, true);
		}

		this.m = m;
		this.M = M;
		leafNodeSize = M * 2;
		nonleafNodeSize = M;
		root = new PCRStarNode(this);
	}

    public int m = 2; // lower limit of children in node
    public int M = 4; // upper limit of children in node

    public PCRStarNode root = null;


    public int height() {
        int height = 0;
        PCRStarNode node = root;
        node.reading(PCRStarNode.pointerByteSize); // we only access parent pointer here
        while(node != null && node.childrenNodes.size() > 0) {
            node = node.childrenNodes.get(0);
            node.reading(PCRStarNode.pointerByteSize);
            height++;
        }

        return height;
    }

    public int leafNodeSize = 0;
    public int nonleafNodeSize = 0;

    public void insert(int index, double value, Rectangle r) {
        PCRStarNode newNode = new PCRStarNode(this, index, value);
        newNode.mbr = r;
        newNode = root.insertNode(newNode, height() - 1);

        if(newNode != null) {
            addRoot(newNode);
        }
    }

    public void addRoot(PCRStarNode node) {
        // System.out.println("new root!");

        PCRStarNode newRoot = new PCRStarNode(this);
        newRoot.mbr = new Rectangle(root.mbr);
        newRoot.add(root);
        newRoot.add(node);
        root = newRoot;
    }

    public PCRStarNode search(Rectangle r) {
        return root.search(r);
    }

    public Vector<PCRStarNode> wideSearch(Rectangle r) {
        return root.wideSearch(r);
    }

    public void delete(Rectangle r) {
        PCRStarNode deletingNode = root.search(r);

        for(PCRStarNode child : deletingNode.childrenNodes) {
            child.reading(PCRStarNode.pointerByteSize + Rectangle.byteSize); // mbr + pointer usage
            if(child.mbr.isOverlapping(r)) {
                deletingNode = child;
                break;
            }
        }

        System.out.println("found node to delete: " + deletingNode);
        if(deletingNode != null) {
            int childDepth = height() - 1; // -1 because height takes leaves into account
            while(deletingNode != null && deletingNode != root) {
                deletingNode.reading(PCRStarNode.pointerByteSize);
                PCRStarNode parent = deletingNode.parent;
                parent.remove(deletingNode);
                parent.condenseTree();
                if(deletingNode.childrenNodes.size() > 0) {
                    for(int i = 0; i < deletingNode.childrenNodes.size(); i++) {
                        root.insertNode(deletingNode.childrenNodes.get(i), childDepth);
                    }
                }

                parent.reading(PCRStarNode.childrenNodeByteSize);
                deletingNode = null;
                if(parent.childrenNodes.size() == 0 && deletingNode != root) {
                    deletingNode = parent;
                }

                childDepth--;
            }
        }
    }

    public void dump() {
        dump(root, 0);
    }

    public void dump(PCRStarNode n, int level) {
        System.out.println("level: " + level +" | " + n);

        for(PCRStarNode node : n.childrenNodes) {
            dump(node, level + 1);
        }
    }

	public double rootValueFor(String aggregate) {
		return root.checkValueFor(aggregate);
	}

    public String toJSON() {
        String result = "";

        result += "{";
        result += "root: " + root.toJSON();
        result += "}";

        return result;
    }
}
