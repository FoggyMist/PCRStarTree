package trees.pcrstartree;

import trees.rstartree.*;
import trees.tree.*;

public class PCRStarNode extends RStarNode {
    public PCRStarNode(Tree t) {
        super(t);
    }


    @Override
    public Node insertNode(Node insertingNode, int insertDepth) {
        int depth = 0;
        Node currentNode = this;

        while(depth < insertDepth) {
            currentNode.chooseSubTree(insertingNode);
            depth = depth + 1;
        }

        while(!insertingNode.isEmpty() && !currentNode.isEmpty()) {
            currentNode.addChild(insertingNode, 1);
            if(
                (currentNode.isLeafNode() && currentNode.size() > tree.leafNodeSize)
                || (currentNode.isNonleafNode() && currentNode.size() > tree.nonleafNodeSize)
            ) {
                insertingNode = currentNode.overflowTreatment();
            } else {
                insertingNode = null;
            }

            currentNode = currentNode.parent();
        }

        return insertingNode;
    }
}
