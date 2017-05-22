package trees.rstartree;

import trees.rtree.*;
import trees.tree.*;
import trees.rectangle.*;
import java.util.*;


public class RStarNode extends RNode {
    public RStarNode(Tree t) {
        super(t);
    }

    @Override
    public RNode splitNode(Node n) {
        childrenNodes.add(n);
        n.parent = this;
        // S1: invoke chooseSplitAxis() to determine the axis,
        // perpendicular to which the split is preformed
        int splitAxis = chooseSplitAxis();
        // S2: invoke chooseSplitIndex to determine the best distribution
        // into two groups along that axis
        RNode nn = chooseSplitIndex(splitAxis); // <- S3 done here
        return nn;
    }

    public int chooseSplitAxis() {
        // CSA1: for each axis
        double lowestS = Double.POSITIVE_INFINITY;
        int splitAxis = -1;
        for(int axis = 0; axis < 2; axis++) { // this implementation supports only 2d rectangles
            // sort the entries by the lower, then by upper value of thier rectangles
            // and determine all distributions as described:
            // margin-value = margin[mbr(firstGroup)] + margin[mbr(secondGroup)]
            int theAxis = axis;
            Collections.sort(childrenNodes, (Node n1, Node n2) -> {
                if(n1.mbr.lowerLeftPoint[theAxis] < n2.mbr.lowerLeftPoint[theAxis]) {
                    return -1;
                } else if(n1.mbr.lowerLeftPoint[theAxis] > n2.mbr.lowerLeftPoint[theAxis]) {
                    return 1;
                } else { // n1 lower == n2 lower, then compare upper
                    if(n1.mbr.upperRightPoint[theAxis] < n2.mbr.upperRightPoint[theAxis]) {
                        return -1;
                    } else if(n1.mbr.upperRightPoint[theAxis] > n2.mbr.upperRightPoint[theAxis]) {
                        return 1;
                     } else { // n1 == n2
                        return 0;
                    }
                }
            });
            // compute (s), the sum of all margin-values of different distributions
            double s = 0;
            Vector<Node> firstGroup = new Vector<Node>(tree.M - 1);
            Vector<Node> secondGroup = new Vector<Node>(tree.M - 1);
            firstGroup.add(childrenNodes.get(0));
            secondGroup.addAll(childrenNodes);
            secondGroup.remove(childrenNodes.get(0));
            for(int a = 1; a < childrenNodes.size(); a++) {
                Rectangle firstMbr = firstGroup.get(0).mbr;
                Rectangle secondMbr = secondGroup.get(0).mbr;
                for(Node firstGroupNode : firstGroup) {
                    firstMbr = Rectangle.enlarge(firstGroupNode.mbr, firstMbr);
                }
                for(Node secondGroupNode : secondGroup) {
                    secondMbr = Rectangle.enlarge(secondGroupNode.mbr, secondMbr);
                }

                double margin = mbr.area() - firstMbr.area() - secondMbr.area();
                s += margin;
                // move to next distribution,
                // take first node from second group and transfer it to first
                Node n = secondGroup.get(0);
                secondGroup.remove(n);
                firstGroup.add(n);
            }

            // CSA2: choose the axis with the minimum (s) as (splitAxis)
            if(s < lowestS) {
                lowestS = s;
                splitAxis = axis;
            }
        }

        return splitAxis;
    }

    public RNode chooseSplitIndex(int axis) {
        // CSI1: along the chosen split axis, choose the distribution
        // with the minimum overlap-value, resolve ties by choosing the distribution
        // with minimum area-value

        double minimumOverlap = Double.POSITIVE_INFINITY;
        Vector<Node> minimumOverlapDistribution1 = new Vector<Node>(tree.M);
        Vector<Node> minimumOverlapDistribution2 = new Vector<Node>(tree.M);
        Vector<Node> firstGroup = new Vector<Node>(tree.M - 1);
        Vector<Node> secondGroup = new Vector<Node>(tree.M - 1);
        Rectangle minimumMbr1 = null;
        Rectangle minimumMbr2 = null;

        firstGroup.add(childrenNodes.get(0));
        secondGroup.addAll(childrenNodes);
        secondGroup.remove(childrenNodes.get(0));
        for(int a = 1; a < childrenNodes.size(); a++) {
            Rectangle firstMbr = firstGroup.get(0).mbr;
            Rectangle secondMbr = secondGroup.get(0).mbr;
            for(Node firstGroupNode : firstGroup) {
                firstMbr = Rectangle.enlarge(firstGroupNode.mbr, firstMbr);
            }
            for(Node secondGroupNode : secondGroup) {
                secondMbr = Rectangle.enlarge(secondGroupNode.mbr, secondMbr);
            }

            double overlap = 0;
            // overlap-value = area[mbr(firstGroup) ∩ mbr(secondGroup)]
            Rectangle intersection = Rectangle.shrink(firstMbr, secondMbr);
            if(intersection != null) {
                overlap = intersection.area();
            }

            if(overlap < minimumOverlap) {
                minimumOverlap = overlap;
                minimumOverlapDistribution1.clear();
                minimumOverlapDistribution1.addAll(firstGroup);
                minimumOverlapDistribution2.clear();
                minimumOverlapDistribution2.addAll(secondGroup);
                minimumMbr1 = new Rectangle(firstMbr);
                minimumMbr2 = new Rectangle(secondMbr);
            } else if(overlap == minimumOverlap) {
                // area = area[mbr(firstGroup)] + area[mbr(secondGroup)]
                double currentArea = firstMbr.area() + secondMbr.area();
                double minimumArea = minimumMbr1.area() + minimumMbr2.area();
                if(currentArea < minimumArea) {
                    minimumOverlapDistribution1.clear();
                    minimumOverlapDistribution1.addAll(firstGroup);
                    minimumOverlapDistribution2.clear();
                    minimumOverlapDistribution2.addAll(secondGroup);
                    minimumMbr1 = new Rectangle(firstMbr);
                    minimumMbr2 = new Rectangle(secondMbr);
                }
            }

            // move to next distribution,
            // take first node from second group and transfer it to first
            Node n = secondGroup.get(0);
            secondGroup.remove(n);
            firstGroup.add(n);
        }

        // S3: distribute the entries into two groups
        mbr = minimumMbr1;
        childrenNodes.clear();
        for(Node newChild : minimumOverlapDistribution1) {
            add(newChild);
        }
        RNode nn = new RStarNode(tree);
        nn.mbr = minimumMbr2;
        for(Node newChild : minimumOverlapDistribution2) {
            nn.add(newChild);
        }
        // nn.parent = parent;
        return nn;
    }
}
