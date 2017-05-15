package trees.rectangle;

public class Rectangle {
    public Rectangle(double x1, double y1, double x2, double y2) {
        lowerLeftPoint = new double[] {x1, y1};
        upperRightPoint = new double[] {x2, y2};
    }

    public double[] lowerLeftPoint = null;
    public double[] upperRightPoint = null;

}
