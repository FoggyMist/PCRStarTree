import trees.tree.*;
import trees.rtree.*;

public class Main {
    public static void main(String[] args) {

        RTree tree = new RTree(2, 4);
        RNode node = new RNode(tree);
        tree.root = node;

    }
}
