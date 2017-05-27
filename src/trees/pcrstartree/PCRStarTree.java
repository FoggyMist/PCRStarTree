package trees.pcrstartree;

import trees.rstartree.*;
import trees.rectangle.*;
import trees.tree.*;
import java.util.*;

public class PCRStarTree {
    public PCRStarTree(int m, int M) {
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
        while(node != null && node.childrenNodes.size() > 0) {
            node = node.childrenNodes.get(0);
            height++;
        }

        return height;
    }

    // TODO
    public int leafNodeSize = 0;
    // TODO
    public int nonleafNodeSize = 0;

    public void insert(int index, Rectangle r) {
        PCRStarNode newNode = new PCRStarNode(this);
        newNode.index = new Integer(index);
        newNode.mbr = r;
        newNode = root.insertNode(newNode, height() - 1);

        if(newNode != null) {
            addRoot(newNode);
        }
    }

    public void addRoot(PCRStarNode node) {
        System.out.println("new root!");


        for(PCRStarNode child : node.childrenNodes) {
            root.forceAdd(child);
        }

        System.out.println("root childrenNodes size: " + root.childrenNodes.size());

        int splitAxis = root.chooseSplitAxis();
        root.sortByDimension(splitAxis);

        PCRStarNode subNode = new PCRStarNode(this);
        int halfSize = root.childrenNodes.size();
        for(int a = root.childrenNodes.size(); a > halfSize; a--) {
            PCRStarNode transferNode = root.childrenNodes.get(a);
            root.remove(transferNode);
            subNode.add(transferNode);
        }

        root.condenseTree();
        subNode.condenseTree();

        PCRStarNode newRoot = new PCRStarNode(this);
        newRoot.mbr = new Rectangle(root.mbr);
        newRoot.add(root);
        newRoot.add(subNode);
        root = newRoot;
        // root.add(node);
    }

    public PCRStarNode search(Rectangle r) {
        return root.search(r);
    }

    public Vector<Integer> wideSearch(Rectangle r) {
        return root.wideSearch(r);
    }

    public void delete(Rectangle r) {
        PCRStarNode deletingNode = root.search(r);
        System.out.println("found node to delete: " + deletingNode);
        if(deletingNode != null) {
            int childDepth = height() - 1; // -1 because height takes leaves into account
            while(deletingNode != null && deletingNode != root) {
                PCRStarNode parent = deletingNode.parent;
                parent.remove(deletingNode);
                parent.condenseTree();
                if(deletingNode.childrenNodes.size() > 0) {
                    for(int i = 0; i < deletingNode.childrenNodes.size(); i++) {
                        root.insertNode(deletingNode.childrenNodes.get(i), childDepth);
                    }
                }

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
        System.out.println("tree-level: " + level +" | " + n);

        for(PCRStarNode node : n.childrenNodes) {
            dump(node, level + 1);
        }
    }

    public String toJSON() {
        String result = "";

        result += "{";
        result += "root: " + root.toJSON();
        result += "}";

        return result;
    }
}
