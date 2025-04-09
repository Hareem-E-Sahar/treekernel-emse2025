import java.text.DecimalFormat;

public class Segment extends Polygons {

    private int x1;

    private int y1;

    private int x2;

    private int y2;

    public double area() {
        return 0;
    }

    public double perimeter() {
        double perimeter = 2 * Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return perimeter;
    }

    public void setWeightCenter() {
        this.weightCenterX = (x1 + x2) / 2;
        this.weightCenterY = (y1 + y2) / 2;
    }

    public Segment(int x1, int x2, int y1, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public void printInfo() {
        StringBuilder figureinfo = new StringBuilder();
        DecimalFormat myFormatter = new DecimalFormat(this.pattern);
        figureinfo.append("x1:").append(myFormatter.format(x1)).append(" ").append("y1:").append(myFormatter.format(y1)).append(" ").append("x2:").append(myFormatter.format(x2)).append(" ").append("y2:").append(myFormatter.format(y2));
        this.setWeightCenter();
        figureinfo.append(" ").append("perimeter:").append(myFormatter.format(this.perimeter())).append(" ").append("area:").append(myFormatter.format(this.area())).append(" ").append("Weight Center X:").append(myFormatter.format(this.weightCenterX)).append(" ").append("Weight center Y:").append(myFormatter.format(this.weightCenterY));
        String str = new String(figureinfo);
        System.out.println(str);
    }
}
