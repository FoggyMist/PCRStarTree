package trees.pcrstartree;

import trees.rstartree.*;
import trees.rectangle.*;
import trees.tree.*;

public class PCRStarTree {
    public PCRStarTree(int m, int M) {
        this.m = m;
        this.M = M;
        leafNodeSize = M;
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
        PCRStarNode newRoot = new PCRStarNode(this);
        newRoot.add(root);
        newRoot.add(node);
        root = newRoot;
        // root.add(node);
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
}
