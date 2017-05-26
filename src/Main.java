import trees.tree.*;
import trees.rectangle.*;
import trees.rstartree.*;
import trees.pcrstartree.*;
import trees.rtree.*;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;


public class Main {
    public static void main(String[] args) {

        PCRStarTree tree = new PCRStarTree(2, 4);

        int storedIndex = -1;
        Rectangle storedRect = null;

        // tree.insert(1, new Rectangle(1,1,3,3));
        for(int a = 1; a <= 160; a++) {
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

        // tree.delete(storedIndex, storedRect);
        // System.out.println("removing: " + storedIndex + " | " + storedRect);
        tree.dump();
        // System.out.println(tree.toJSON());


        System.out.println("leaf search:");
        PCRStarNode leafResults = tree.search(new Rectangle(-10, -10, 10, 10));
        if(leafResults == null) {
            System.out.println("no results found");
        } else {
            System.out.println("Ids found:");

            for(PCRStarNode a : leafResults.childrenNodes) {
                System.out.println(a.index);
            }

        }

        System.out.println("wide search:");
        Vector<Integer> wideResults = tree.wideSearch(new Rectangle(-10, -10, 10, 10));
        if(wideResults.size() == 0) {
            System.out.println("no results found");
        } else {
            System.out.println("Ids found:");
            for(Integer a : wideResults) {
                System.out.println(a);
            }
        }

    }
}
