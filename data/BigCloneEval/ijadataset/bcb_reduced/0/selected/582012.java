package magictool.groupdisplay;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageConverter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import magictool.ExpFile;
import magictool.GrpFile;

/**
 * CircleDisplay paints an interactive circular display of genes which draw a line
 * between genes whose dissimilarity is less than a given threshold. This panel is
 * printable.
 */
public class CircleDisplay extends JPanel implements Printable {

    /**expression file*/
    protected ExpFile exp;

    /**group file*/
    protected GrpFile grp;

    /**names of the genes*/
    protected Object[] names;

    private int namePlaces[];

    /**dissimilarities of the genes*/
    protected float[][] dissims;

    private Font f = new Font("Times New Roman", Font.PLAIN, 10);

    private int maxwidth;

    private FontMetrics fm;

    /**threshold*/
    protected float thresh;

    private GeneBox[] boxes;

    /**selected gene*/
    protected int selected = -1;

    /**radius of the circle*/
    protected int radius = 0;

    boolean fixedRadius = false;

    private int ascent = 0;

    private int plus = 1;

    /**
   * Constructs a circular display for a given expression file and group file
   * @param exp expression file
   * @param grp group file
   */
    public CircleDisplay(ExpFile exp, GrpFile grp) {
        this.exp = exp;
        this.grp = grp;
        if (grp == null || grp.getNumGenes() == 0) names = exp.getGeneVector(); else names = grp.getGroup();
        namePlaces = new int[names.length];
        dissims = new float[names.length][names.length];
        for (int i = 0; i < names.length; i++) {
            namePlaces[i] = exp.findGeneName(names[i].toString());
        }
        this.setBackground(Color.white);
        for (int i = 0; i < names.length; i++) {
            for (int j = i; j < names.length; j++) {
                if (namePlaces[i] != -1 && namePlaces[j] != -1) dissims[i][j] = exp.correlation(namePlaces[i], namePlaces[j]); else dissims[i][j] = 100;
                dissims[j][i] = dissims[i][j];
            }
        }
        thresh = .2f;
        boxes = new GeneBox[names.length];
        this.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                this_mouseClicked(e);
            }
        });
    }

    private void this_mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        selected = -1;
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].isOver(x, y)) {
                selected = i;
                this.repaint();
                this.setSize(this.getSize().width + plus, this.getSize().height);
                plus *= -1;
                break;
            }
        }
    }

    /**
   * used to print the circular display - required by the printable interface
   * @param g graphics to paint panel on
   * @param pf page format
   * @param pi page number
   * @return whether page exists
   */
    public int print(Graphics g, PageFormat pf, int pi) {
        if (pi != 0) {
            return Printable.NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Dialog", Font.PLAIN, 10));
        int h = this.getTotalHeight();
        int w = this.getTotalWidth();
        CircleDisplay temp = new CircleDisplay(exp, grp);
        temp.setThresh(this.getThresh());
        temp.selected = this.selected;
        temp.setDoubleBuffered(true);
        temp.setSize(new Dimension(w, h));
        double pageHeight = pf.getImageableHeight();
        double pageWidth = pf.getImageableWidth();
        double chartWidth = (double) h;
        double chartHeight = (double) w;
        double scale = 1;
        scale = pageWidth / chartWidth;
        scale = Math.min(scale, pageHeight / chartHeight);
        g2.translate(pf.getImageableX(), pf.getImageableY());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(exp.getExpFile().getName(), 0, fm.getHeight());
        String s = "";
        g2.drawString((s = grp.getGrpFile().getName() + " - " + grp.getNumGenes() + " Genes - Threshold = " + this.getThresh()), (int) pageWidth - fm.stringWidth(s), fm.getHeight());
        g2.translate((pageWidth - chartWidth * scale) / 2, fm.getHeight() + 5);
        g2.scale(scale, scale);
        temp.paintComponent(g2);
        return Printable.PAGE_EXISTS;
    }

    /**
   * Paints the circular display
   * @param g graphics to paint the panel on
   */
    public void paintComponent(Graphics g) {
        fm = g.getFontMetrics(f);
        g.setFont(f);
        ascent = fm.getAscent() + 4;
        maxwidth = 0;
        for (int i = 0; i < names.length; i++) {
            int t;
            if ((t = fm.stringWidth(names[i].toString())) > maxwidth) maxwidth = t;
        }
        maxwidth += 4;
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        if (!fixedRadius) {
            radius = getWidth() / 2 - maxwidth - 2;
            int r2 = getHeight() / 2 - (fm.getAscent() + 4) - 2;
            if (r2 < radius) radius = r2;
        }
        float deltaangle = (float) ((2 * Math.PI) / (names.length));
        double angle = 3 * Math.PI / 2;
        for (int i = 0; i < names.length; i++) {
            int x, y;
            int linepos;
            x = Math.round(radius * (float) Math.cos(angle)) + center.x;
            y = Math.round(radius * (float) Math.sin(angle)) + center.y;
            int swidth = fm.stringWidth(names[i].toString());
            if (angle % (Math.PI / 2) == 0 || (names.length / i == 2 && names.length % i == 0)) {
                x = x - maxwidth / 2;
                if (angle == (3 * Math.PI / 2)) {
                    y = y - (fm.getAscent() + 4);
                    linepos = boxes[i].BOTTOM;
                } else linepos = boxes[i].TOP;
            } else {
                y = y - (fm.getAscent() + 4) / 2;
                if (angle > (Math.PI / 2) && angle < (1.5 * Math.PI)) {
                    x = x - maxwidth;
                    linepos = boxes[i].RIGHT;
                } else linepos = boxes[i].LEFT;
            }
            boxes[i] = new GeneBox(names[i].toString(), x, y, maxwidth, fm.getAscent() + 4, (selected == i));
            boxes[i].setLinePosStyle(linepos);
            boxes[i].drawBox(g);
            angle -= deltaangle;
        }
        for (int i = 0; i < boxes.length; i++) {
            for (int j = i; j < boxes.length; j++) {
                if (dissims[i][j] < thresh) {
                    if (boxes[i].getSelected() || boxes[j].getSelected()) {
                        if (boxes[i].getSelected()) boxes[j].setOtherSelected(true); else boxes[i].setOtherSelected(true);
                        g.setColor(Color.red);
                        int x11 = boxes[i].getLinePosX();
                        int x12 = x11;
                        int x21 = boxes[j].getLinePosX();
                        int x22 = x21;
                        int y11 = boxes[i].getLinePosY();
                        int y12 = y11;
                        int y21 = boxes[j].getLinePosY();
                        int y22 = y21;
                        if (boxes[i].getLinePosStyle() == boxes[i].TOP || boxes[i].getLinePosStyle() == boxes[i].BOTTOM) {
                            x11 += 1;
                            x12 -= 1;
                        } else {
                            y11 += 1;
                            y12 -= 1;
                        }
                        if (boxes[j].getLinePosStyle() == boxes[i].TOP || boxes[i].getLinePosStyle() == boxes[j].BOTTOM) {
                            x21 += 1;
                            x22 -= 1;
                        } else {
                            y21 += 1;
                            y22 -= 1;
                        }
                        g.drawLine(x11, y11, x21, y21);
                        g.drawLine(x12, y12, x22, y22);
                    }
                    g.setColor(Color.black);
                    g.drawLine(boxes[i].getLinePosX(), boxes[i].getLinePosY(), boxes[j].getLinePosX(), boxes[j].getLinePosY());
                }
            }
        }
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].drawBox(g);
        }
    }

    /**
   * sets the threshold
   * @param thresh threshold level
   */
    public void setThresh(float thresh) {
        this.thresh = thresh;
        this.repaint();
        this.setSize(this.getSize().width + plus, this.getSize().height);
        plus *= -1;
    }

    /**
   * returns the threshold level
   * @return threshold level
   */
    public float getThresh() {
        return thresh;
    }

    /**
   * returns the radius
   * @return radius of circle
   */
    public int getRadius() {
        return radius;
    }

    /**
   * returns the total width of display
   * @return total width of display
   */
    public int getTotalWidth() {
        return radius * 2 + (maxwidth) * 2 + 2;
    }

    /**
   * returns the total height of display
   * @return total height of display
   */
    public int getTotalHeight() {
        return radius * 2 + (ascent) * 2 + 2;
    }

    /**
   * sets the radius of the circle
   * @param newRadius radius of the circle
   */
    public void setRadius(int newRadius) {
        this.radius = newRadius;
        fixedRadius = true;
        this.repaint();
        this.setSize(this.getSize().width + plus, this.getSize().height);
        plus *= -1;
    }

    /**
   * gets a list of genes that are currently selected
   * @return array of gene names that are selected
   */
    public Object[] getAllSelected() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < boxes.length; i++) if (boxes[i].getOtherSelected() || boxes[i].getSelected()) arrayList.add(boxes[i].getName());
        Object[] list = new Object[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) list[i] = arrayList.get(i);
        return list;
    }

    /**
     * gets the number of megapixels for a saved image
     * @return number of megapixels for a saved image
     */
    public double getMegaPixels() {
        int h = getTotalHeight();
        int w = getTotalWidth();
        Font f = new Font("Dialog", Font.PLAIN, 10);
        int th = h + this.getGraphics().getFontMetrics(f).getHeight() * 3;
        return (((double) w) / 1000) * (((double) th) / 1000);
    }

    /**
   * saves a gif image of the display
   * @param name new filename
   * @param number number of gif files to create
   * @throws IOException if error creating gif image
   */
    public void saveImage(String name, int number) throws IOException {
        int h = getTotalHeight();
        int w = getTotalWidth();
        Font f = new Font("Dialog", Font.PLAIN, 10);
        int th = h + this.getGraphics().getFontMetrics(f).getHeight() * 3;
        int height = (int) Math.ceil((double) th / number);
        Image bi = createImage(w, height);
        Graphics image = bi.getGraphics();
        Graphics2D image2 = (Graphics2D) image;
        try {
            String n = "";
            if (number > 1) {
                n = name.substring(0, name.lastIndexOf(".gif"));
                String na = n.substring(n.lastIndexOf(File.separator));
                n = n + "_images";
                File f1 = new File(n);
                f1.mkdir();
                n += na;
            }
            for (int i = 0; i < number; i++) {
                image2.setFont(f);
                FontMetrics fm = image2.getFontMetrics();
                CircleDisplay temp = new CircleDisplay(exp, grp);
                temp.setThresh(getThresh());
                temp.selected = selected;
                temp.setDoubleBuffered(true);
                temp.setSize(new Dimension(w, h));
                image2.setColor(Color.white);
                image2.fillRect(0, 0, w, height);
                image2.translate(0, -i * height);
                temp.paintComponent(image2);
                image2.setColor(new Color(0, 0, 0));
                image2.drawString("Expression File: " + exp.getExpFile().getName(), 5, h + fm.getHeight() - fm.getDescent());
                try {
                    image2.drawString("Group File: " + grp.getGrpFile().getName() + " - " + grp.getNumGenes() + " Genes", 5, h + fm.getHeight() * 2 - fm.getDescent());
                } catch (Exception noname) {
                    image2.drawString("Group File: Temporary Group" + " - " + grp.getNumGenes() + " Genes", 5, h + fm.getHeight() * 2 - fm.getDescent());
                }
                image2.drawString("Threshold = " + getThresh(), 5, h + fm.getHeight() * 3 - fm.getDescent());
                image2.translate(0, i * height);
                File theFile;
                if (number == 1) theFile = new File(name); else theFile = new File(n + i + ".gif");
                ImagePlus gifImage = new ImagePlus("NewGif", bi);
                ImageConverter converter = new ImageConverter(gifImage);
                converter.convertRGBtoIndexedColor(256);
                FileSaver fileSaver = new FileSaver(gifImage);
                try {
                    fileSaver.saveAsGif(theFile.getPath());
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(null, "Error Writing .gif File - " + e2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GeneBox {

        int x, y, height, width, linePosStyle;

        String name;

        boolean selected, otherselected;

        public static final int LEFT = 0, RIGHT = 1, BOTTOM = 2, TOP = 3;

        public GeneBox(String name, int x, int y, int width, int height, boolean selected) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.name = name;
            this.selected = selected;
            linePosStyle = LEFT;
            otherselected = false;
        }

        public void drawBox(Graphics g) {
            int swidth = 0;
            if (name != null) swidth = fm.stringWidth(name);
            g.setColor(Color.lightGray.darker());
            g.drawRect(x - 1, y + 1, width, height);
            g.setColor(Color.lightGray);
            g.drawRect(x - 2, y + 2, width, height);
            if (selected) g.setColor(Color.yellow); else if (otherselected) g.setColor(Color.green); else g.setColor(Color.white);
            g.fillRect(x, y, width, height);
            g.setColor(Color.black);
            g.drawRect(x, y, width, height);
            g.drawString((name != null ? name : ""), x + (width - swidth) / 2, y + height - 2);
        }

        public void setOtherSelected(boolean selected) {
            this.otherselected = selected;
        }

        public boolean getOtherSelected() {
            return otherselected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean getSelected() {
            return selected;
        }

        public boolean isOver(int xpos, int ypos) {
            return (xpos >= x && xpos <= (x + width) && ypos >= y && ypos <= (y + height));
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getName() {
            return name;
        }

        public int getLinePosStyle() {
            return linePosStyle;
        }

        public void setLinePosStyle(int style) {
            linePosStyle = style;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getLinePosX() {
            int style = x;
            switch(linePosStyle) {
                case LEFT:
                    style = x;
                    break;
                case RIGHT:
                    style = x + width;
                    break;
                case TOP:
                    style = x + width / 2;
                    break;
                case BOTTOM:
                    style = x + width / 2;
                    break;
            }
            return style;
        }

        public int getLinePosY() {
            int style = y + height / 2;
            switch(linePosStyle) {
                case LEFT:
                    style = y + height / 2;
                    break;
                case RIGHT:
                    style = y + height / 2;
                    break;
                case TOP:
                    style = y;
                    break;
                case BOTTOM:
                    style = y + height;
                    break;
            }
            return style;
        }
    }
}
