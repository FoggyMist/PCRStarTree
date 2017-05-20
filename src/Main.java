import trees.tree.*;
import trees.rectangle.*;
import trees.rtree.*;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;


public class Main {
    public static void main(String[] args) {

        RTree tree = new RTree(2, 4);

        int storedIndex = -1;
        Rectangle storedRect = null;

        // tree.insert(1, new Rectangle(1,1,3,3));
        for(int a = 1; a <= 40; a++) {
            double x = ThreadLocalRandom.current().nextDouble(-100, 100);
            double y = ThreadLocalRandom.current().nextDouble(-100, 100);
            double height = ThreadLocalRandom.current().nextDouble(1, 40);
            double width = ThreadLocalRandom.current().nextDouble(1, 40);
            Rectangle rect = new Rectangle(
                x,
                y,
                x+width,
                y+height
            );

            tree.insert(a, rect);

            if(a == 27) {
                storedIndex = a;
                storedRect = rect;
            }
        }

        tree.delete(storedIndex, storedRect);
        System.out.println("removing: " + storedIndex + " | " + storedRect);
        tree.dump();

        Vector<Integer> results = tree.search(new Rectangle(-10, -10, 10, 10));

        System.out.println("Ids found:");
        for(Integer a : results) {
            System.out.println(a);
        }

    }
}
