import trees.rectangle.*;
import trees.pcrstartree.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {

        PCRStarTree tree = new PCRStarTree(2, 4);
        PCRStarTree sortedInsertTree = new PCRStarTree(2, 4);

        int storedIndex = -1;
        Rectangle storedRect = null;

        // tree.insert(1, new Rectangle(1,1,3,3));
        for(int a = 1; a <= 27; a++) {
            double x = ThreadLocalRandom.current().nextDouble(-100, 100);
            double y = ThreadLocalRandom.current().nextDouble(-100, 100);
            double height = ThreadLocalRandom.current().nextDouble(1, 2);
            double width = ThreadLocalRandom.current().nextDouble(1, 2);
            double value = ThreadLocalRandom.current().nextDouble(0, 100);
            Rectangle rect = new Rectangle(
                x,
                y,
                x+width,
                y+height
            );

            tree.insert(a, value, rect);

            if(a == 27) {
                storedIndex = a;
                storedRect = rect;
            }
        }

        tree.delete(storedRect);
        tree.dump();
        // System.out.println("removing: " + storedIndex + " | " + storedRect);

        try{
            PrintWriter writer = new PrintWriter("viewer/data.js", "UTF-8");
            writer.println("var data = " + tree.toJSON() + ";");
            writer.close();
        } catch (Exception e) {
            System.out.println("data dump to JSON has failed");
        }

        // System.out.println("leaf search:");
        // PCRStarNode leafResults = tree.search(new Rectangle(-10, -10, 10, 10));
        // if(leafResults == null) {
        //     System.out.println("no results found");
        // } else {
        //     System.out.println("Ids found:");
        //
        //     for(PCRStarNode a : leafResults.childrenNodes) {
        //         System.out.println(a.index);
        //     }
        //
        // }

        System.out.println("wide search:");
        Vector<PCRStarNode> wideResults = tree.wideSearch(new Rectangle(-10, -10, 10, 10));
        if(wideResults.size() == 0) {
            System.out.println("no results found");
        } else {
            System.out.println("nodes found:");
            for(PCRStarNode node : wideResults) {
                System.out.println(node);
            }
        }

    }
}
