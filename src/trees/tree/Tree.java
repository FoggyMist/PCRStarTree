package trees.tree;



public class Tree {
    public Tree() {

    }

    public Node root = null;
    public void addRoot(Node newRoot) {
        root = newRoot;
    }

    // TODO
    public int height() {
        int height = 0;
        Node node = root;
        while(node != null) {
            node = node.childrenNodes.get(0);
            height++;
        }

        return height;
    }

    // TODO
    public int leafNodeSize = 0;
    // TODO
    public int nonleafNodeSize = 0;
}
