import trees.tree.*;
import trees.rectangle.*;
import trees.rstartree.*;
import trees.pcrstartree.*;
import trees.rtree.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {

        PCRStarTree tree = new PCRStarTree(2, 4);
        PCRStarTree sortedInsertTree = new PCRStarTree(2, 4);

        Vector<Rectangle> sortedRect = new Vector<Rectangle>();

        int storedIndex = -1;
        Rectangle storedRect = null;

        // tree.insert(1, new Rectangle(1,1,3,3));
        for(int a = 1; a <= 27; a++) {
            double x = ThreadLocalRandom.current().nextDouble(-100, 100);
            double y = ThreadLocalRandom.current().nextDouble(-100, 100);
            double height = ThreadLocalRandom.current().nextDouble(1, 2);
            double width = ThreadLocalRandom.current().nextDouble(1, 2);
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

            sortedRect.add(rect);
        }

        Collections.sort(sortedRect, (Rectangle n1, Rectangle n2) -> {
            if(n1.lowerLeftPoint[1] < n2.lowerLeftPoint[1]) {
                return -1;
            } else if(n1.lowerLeftPoint[1] > n2.lowerLeftPoint[1]) {
                return 1;
            } else { // n1 lower == n2 lower, then compare upper
                if(n1.upperRightPoint[1] < n2.upperRightPoint[1]) {
                    return -1;
                } else if(n1.upperRightPoint[1] > n2.upperRightPoint[1]) {
                    return 1;
                 } else { // n1 == n2
                    return 0;
                }
            }
        });

        int id = 1;
        for(Rectangle r : sortedRect) {
            sortedInsertTree.insert(id++, r);
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

        try{
            PrintWriter writer = new PrintWriter("viewer/data2.js", "UTF-8");
            writer.println("var data2 = " + sortedInsertTree.toJSON() + ";");
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
        //
        // System.out.println("wide search:");
        // Vector<Integer> wideResults = tree.wideSearch(new Rectangle(-10, -10, 10, 10));
        // if(wideResults.size() == 0) {
        //     System.out.println("no results found");
        // } else {
        //     System.out.println("Ids found:");
        //     for(Integer a : wideResults) {
        //         System.out.println(a);
        //     }
        // }

    }
}
