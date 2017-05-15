package trees.pcrstartree;

import trees.rstartree.*;
import trees.rectangle.*;

public class PCRStarTree extends RStarTree {
    public PCRStarTree() {
        super();
    }

    // @Override
    public void insert(Rectangle r) {
        PCRStarNode newNode = root.insertNode(r, height());

        if(newNode != null) {
            addRoot(newNode);
        }
    }
}
