import trees.tree.*;
import trees.pcrstartree.*;

public class Main {
    public static void main(String[] args) {
        Node node = new Node();
        PCRStarNode node2 = new PCRStarNode();

        PCRStarTree tree = new PCRStarTree(2, 4);
        tree.root = node2;

    }
}
