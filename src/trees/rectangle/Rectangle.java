package trees.rectangle;

import java.text.DecimalFormat;
import java.math.RoundingMode;

public class Rectangle {
    public Rectangle(double x1, double y1, double x2, double y2) {
        lowerLeftPoint = new double[] {Math.min(x1, x2), Math.min(y1, y2)};
        upperRightPoint = new double[] {Math.max(x1, x2), Math.max(y1, y2)};
    }

    public Rectangle() {
        lowerLeftPoint = new double[2];
        upperRightPoint = new double[2];
    }
    public Rectangle(Rectangle r) {
        lowerLeftPoint = new double[] {
            r.lowerLeftPoint[0],
            r.lowerLeftPoint[1]
        };
        upperRightPoint = new double[] {
            r.upperRightPoint[0],
            r.upperRightPoint[1]
        };
    }

    public double[] lowerLeftPoint = null;
    public double[] upperRightPoint = null;

    public boolean isOverlapping(Rectangle r) {
        return (
            this.lowerLeftPoint[0] < r.upperRightPoint[0]
            && this.upperRightPoint[0] > r.lowerLeftPoint[0]
            && this.lowerLeftPoint[1] < r.upperRightPoint[1]
            && this.upperRightPoint[1] > r.lowerLeftPoint[1]
        );
    }


    public static Rectangle enlarge(Rectangle parent, Rectangle child) {
        Rectangle result = new Rectangle();
        result.lowerLeftPoint[0] = Math.min(
            parent.lowerLeftPoint[0],
            child.lowerLeftPoint[0]
        );

        result.lowerLeftPoint[1] = Math.min(
            parent.lowerLeftPoint[1],
            child.lowerLeftPoint[1]
        );

        result.upperRightPoint[0] = Math.max(
            parent.upperRightPoint[0],
            child.upperRightPoint[0]
        );

        result.upperRightPoint[1] = Math.max(
            parent.upperRightPoint[1],
            child.upperRightPoint[1]
        );

        return result;
    }

    public static Rectangle shrink(Rectangle r1, Rectangle r2) {
        if(!r1.isOverlapping(r2)) {
            return null;
        }

        Rectangle result = new Rectangle();
        result.lowerLeftPoint[0] = Math.max(
            r1.lowerLeftPoint[0],
            r2.lowerLeftPoint[0]
        );

        result.lowerLeftPoint[1] = Math.max(
            r1.lowerLeftPoint[1],
            r2.lowerLeftPoint[1]
        );

        result.upperRightPoint[0] = Math.min(
            r1.upperRightPoint[0],
            r2.upperRightPoint[0]
        );

        result.upperRightPoint[1] = Math.min(
            r1.upperRightPoint[1],
            r2.upperRightPoint[1]
        );

        return result;
    }

    public double area() {
        return (Math.abs(upperRightPoint[0] - lowerLeftPoint[0])) * (Math.abs(upperRightPoint[1] - lowerLeftPoint[1]));
    }

    public double[] center() {
        return new double[]{
            (upperRightPoint[0] - lowerLeftPoint[0]) / 2,
            (upperRightPoint[1] - lowerLeftPoint[1]) / 2
        };
    }

    public static double distance(double[] p1, double[] p2) {
        return Math.sqrt(
            (p1[0] - p2[0]) * (p1[0] - p2[0]) +
            (p1[1] - p2[1]) * (p1[1] - p2[1])
        );
    }

    public String toString() {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        return "["
            + df.format(lowerLeftPoint[0]) + ", "
            + df.format(lowerLeftPoint[1]) + ", "
            + df.format(upperRightPoint[0]) + ", "
            + df.format(upperRightPoint[1])
        + "]";
    }
}
