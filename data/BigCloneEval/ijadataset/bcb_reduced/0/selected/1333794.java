package tuner3d.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import tuner3d.genome.Region;

public class ArrowLine {

    public static int thick = 5;

    public static int thick2 = thick * 2;

    public static int head = 8;

    public static int side = 4;

    public static int ascend = 5;

    public static int descend = 12;

    public static final int LEVEL = 100;

    public static final int ANGLE = -90;

    private String caption, tag;

    private int begin, end, mid, level;

    private boolean direct;

    private Color color;

    private Polygon arrow;

    /*************************************
	 *             direct=true 
	 *                ascend---|2 caption        
     *           0____________1_|\__head        
     *    begin__|--thick _level_ \__end  
     *           |____________5_  /3       
     *           6      | side--|/        
	 *                 mid      4|---descend                    
     *                          tag     
	 *************************************/
    public ArrowLine(int begin, int end, int level, boolean direct) {
        caption = tag = "";
        this.direct = direct;
        this.begin = begin;
        this.end = end;
        this.level = level;
        this.mid = (begin + end) / 2;
        color = new Color(0, 0, 0);
        int[] xpoints = new int[7];
        int[] ypoints = new int[7];
        if (end - begin < head) end = begin + head;
        mid = (begin + end) / 2;
        ypoints[0] = level - thick;
        ypoints[1] = level - thick;
        ypoints[2] = level - thick - side;
        ypoints[3] = level;
        ypoints[4] = level + thick + side;
        ypoints[5] = level + thick;
        ypoints[6] = level + thick;
        if (direct) {
            xpoints[0] = begin;
            xpoints[1] = end - head;
            xpoints[2] = end - head;
            xpoints[3] = end;
            xpoints[4] = end - head;
            xpoints[5] = end - head;
            xpoints[6] = begin;
        } else {
            xpoints[0] = end;
            xpoints[1] = begin + head;
            xpoints[2] = begin + head;
            xpoints[3] = begin;
            xpoints[4] = begin + head;
            xpoints[5] = begin + head;
            xpoints[6] = end;
        }
        arrow = new Polygon(xpoints, ypoints, 7);
    }

    public int getLevel() {
        return level;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static void drawArrow(Graphics g, ArrowLine arrow) {
        arrow.draw(g);
    }

    public static void drawArrow(Graphics g, ArrowLine arrow, String caption) {
        arrow.setCaption(caption);
        arrow.draw(g);
    }

    public static void drawArrow(Graphics g, ArrowLine arrow, String caption, Color c) {
        arrow.setCaption(caption);
        arrow.setColor(c);
        arrow.draw(g);
    }

    public static void drawArrow(Graphics g, ArrowLine arrow, String caption, String tag) {
        arrow.setCaption(caption);
        arrow.setTag(tag);
        arrow.draw(g);
    }

    public static void drawArrow(Graphics g, ArrowLine arrow, String caption, String tag, Color c) {
        arrow.setColor(c);
        arrow.setCaption(caption);
        arrow.setTag(tag);
        arrow.draw(g);
    }

    public void draw(Graphics g) {
        draw(g, ANGLE);
    }

    public void draw(Graphics g, int angle) {
        Color oldColor = g.getColor();
        g.setColor(color);
        g.fillPolygon(arrow);
        g.setColor(Color.BLACK);
        g.drawPolygon(arrow);
        g.setColor(oldColor);
        if (tag.equals("t") || tag.equals("r")) {
            if (direct) g.drawString(tag, end - head, level + side + thick + descend); else g.drawString(tag, begin, level + side + thick + descend);
        }
        Graphics2D g2d = (Graphics2D) g;
        if (direct) {
            g2d.translate(mid, level - side - thick);
            g2d.rotate(angle * Math.PI / 180);
            g2d.drawString(caption, 0, 0);
            g2d.rotate(-angle * Math.PI / 180);
            g2d.translate(-mid, side + thick - level);
        } else {
            g2d.translate(mid + head / 2, level - side - thick);
            g2d.rotate(angle * Math.PI / 180);
            g2d.drawString(caption, 0, 0);
            g2d.rotate(-angle * Math.PI / 180);
            g2d.translate(-mid - head / 2, side + thick - level);
        }
    }

    public static ArrowLine convert(Region region, int first, int xOffset, int yOffset, float scale) {
        return convert(region, first, xOffset, yOffset, scale, LEVEL);
    }

    public static ArrowLine convert(Region region, int first, int xOffset, int yOffset, float scale, int level) {
        int begin = xOffset + (int) ((region.getBegin() - first) * scale);
        int end = xOffset + (int) ((region.getEnd() - first) * scale);
        return new ArrowLine(begin, end, level, region.getStrand());
    }
}
