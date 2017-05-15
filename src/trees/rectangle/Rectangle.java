package trees.rectangle;

public class Rectangle {
    public Rectangle(double x1, double y1, double x2, double y2) {
        lowerLeftPoint = new double[] {Math.min(x1, x2), Math.min(y1, y2)};
        upperRightPoint = new double[] {Math.max(x1, x2), Math.max(y1, y2)};
    }

    public Rectangle() {
        lowerLeftPoint = new double[2];
        upperRightPoint = new double[2];
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

        result.lowerLeftPoint[0] = Math.max(
            parent.lowerLeftPoint[0],
            child.lowerLeftPoint[0]
        );

        result.lowerLeftPoint[1] = Math.max(
            parent.lowerLeftPoint[1],
            child.lowerLeftPoint[1]
        );

        return result;
    }

    public double area() {
        return (Math.abs(upperRightPoint[0] - lowerLeftPoint[0])) * (Math.abs(upperRightPoint[1] - lowerLeftPoint[1]));
    }
}
