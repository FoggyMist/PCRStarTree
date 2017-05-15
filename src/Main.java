import trees.tree.*;
import trees.pcrstartree.*;

public class Main {
    public static void main(String[] args) {
        Node node = new Node();
        PCRStarNode node2 = new PCRStarNode();
        //
        // node.check();
        // node2.check();

        PCRStarTree tree = new PCRStarTree();
        tree.root = node2;

    }
}
