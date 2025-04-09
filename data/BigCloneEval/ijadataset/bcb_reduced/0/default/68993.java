import java.text.DecimalFormat;

public class Triangle extends Polygons {

    private int x1;

    private int y1;

    private int x2;

    private int y2;

    private int x3;

    private int y3;

    public double area() {
        double area = 0.5 * Math.abs(((x1 - x2) * (y1 - y3) - (x1 - x3) * (y1 - y2)));
        return area;
    }

    public double perimeter() {
        double perimeter = (Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))) + (Math.sqrt((x2 - x3) * (x2 - x3) + (y2 - y3) * (y2 - y3))) + (Math.sqrt((x3 - x1) * (x3 - x1) + (y3 - y1) * (y3 - y1)));
        return perimeter;
    }

    public void setWeightCenter() {
        double px = (x1 + x2) / 2;
        double py = (y1 + y2) / 2;
        this.weightCenterX = (x3 + 2 * px) / 3;
        this.weightCenterY = (y3 + 2 * py) / 3;
    }

    public Triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    public void printInfo() {
        StringBuilder figureinfo = new StringBuilder();
        DecimalFormat myFormatter = new DecimalFormat(this.pattern);
        figureinfo.append("x1:").append(myFormatter.format(x1)).append(" ").append("y1:").append(myFormatter.format(y1)).append(" ").append("x2:").append(myFormatter.format(x2)).append(" ").append("y2:").append(myFormatter.format(y2)).append(" ").append("x3:").append(myFormatter.format(x3)).append(" ").append("y3:").append(myFormatter.format(y3));
        this.setWeightCenter();
        figureinfo.append(" ").append("perimeter:").append(myFormatter.format(this.perimeter())).append(" ").append("area:").append(myFormatter.format(this.area())).append(" ").append("Weight Center X:").append(myFormatter.format(this.weightCenterX)).append(" ").append("Weight center Y:").append(myFormatter.format(this.weightCenterY));
        String str = new String(figureinfo);
        System.out.println(str);
    }
}
