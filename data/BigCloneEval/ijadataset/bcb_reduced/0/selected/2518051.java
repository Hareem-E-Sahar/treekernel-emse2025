package tjacobs.ui.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;
import tjacobs.Arrays;
import tjacobs.MathUtils;
import tjacobs.io.DataFetcher;
import tjacobs.io.IOUtils;
import tjacobs.util.Span;

public class PaintUtils {

    private PaintUtils() {
        super();
    }

    public static StringBuffer printPoint(Point p) {
        return new StringBuffer("" + p.x + "," + p.y);
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static void drawArc(Graphics2D g, int x, int y, int width, int height, double start, double end, int innerXOffset, int innerYOffset) {
        Area a = createArc(x, y, width, height, start, end, innerXOffset, innerYOffset);
        g.draw(a);
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static void fillArc(Graphics2D g, int x, int y, int width, int height, double start, double end, int innerXOffset, int innerYOffset) {
        Area a = createArc(x, y, width, height, start, end, innerXOffset, innerYOffset);
        g.fill(a);
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static Area createArc(int x, int y, int width, int height, double start, double end, int innerXOffset, int innerYOffset) {
        Shape s = new Ellipse2D.Double(x, y, width, height);
        Area a = new Area(s);
        int center_x = x + width / 2;
        int center_y = y + height / 2;
        int xs[] = new int[6];
        int ys[] = new int[6];
        xs[0] = center_x;
        ys[0] = center_y;
        double middle = start + (end - start) / 2;
        double quarter1 = start + (middle - start) / 2;
        double quarter2 = middle + (end - middle) / 2;
        int pt1_x = (int) (center_x + width * Math.cos(start));
        int pt1_y = (int) (center_y + height * Math.sin(start));
        int pt2_x = (int) (center_x + width * Math.cos(end));
        int pt2_y = (int) (center_y + height * Math.sin(end));
        int mid_x = (int) (center_x + width * Math.cos(middle));
        int mid_y = (int) (center_y + height * Math.sin(middle));
        int quar1_x = (int) (center_x + height * Math.cos(quarter1));
        int quar1_y = (int) (center_y + height * Math.sin(quarter1));
        int quar2_x = (int) (center_x + height * Math.cos(quarter2));
        int quar2_y = (int) (center_y + height * Math.sin(quarter2));
        xs[1] = pt1_x;
        ys[1] = pt1_y;
        xs[2] = quar1_x;
        ys[2] = quar1_y;
        xs[3] = mid_x;
        ys[3] = mid_y;
        xs[4] = quar2_x;
        ys[4] = quar2_y;
        xs[5] = pt2_x;
        ys[5] = pt2_y;
        Polygon p = new Polygon(xs, ys, 6);
        Area clip = new Area(p);
        a.intersect(clip);
        Ellipse2D.Double inner = new Ellipse2D.Double(x + innerXOffset, y + innerYOffset, width - innerXOffset * 2, height - innerYOffset * 2);
        a.subtract(new Area(inner));
        return a;
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static Shape getLineShape(Line2D line, float lineWidth) {
        Shape myStrokeShape;
        Stroke s = new BasicStroke(lineWidth);
        myStrokeShape = s.createStrokedShape(line);
        return myStrokeShape;
    }

    /** @deprecated
	 * @see getLineShape
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param height
	 * @return
	 */
    public static Path2D createLineWithWidth(double x1, double y1, double x2, double y2, double height) {
        double angle = MathUtils.angle(x1, y1, x2, y2);
        Path2D.Double path = new Path2D.Double();
        double adjust_x_by = Math.sin(angle) * height / 2;
        double adjust_y_by = Math.cos(angle) * height / 2;
        path.moveTo(x1 - adjust_x_by, y1 + adjust_y_by);
        path.lineTo(x1 + adjust_x_by, y1 - adjust_y_by);
        path.lineTo(x2 + adjust_x_by, y2 - adjust_y_by);
        path.lineTo(x2 - adjust_x_by, y2 + adjust_y_by);
        return path;
    }

    public static void testLineWithWidth() {
        JPanel jp = new JPanel();
        MouseInputAdapter mml = new MouseInputAdapter() {

            Point initial;

            Path2D myPath;

            Area myArea;

            Shape myStrokeShape;

            public void mousePressed(MouseEvent me) {
                initial = me.getPoint();
            }

            public void mouseReleased(MouseEvent me) {
                initial = null;
            }

            public void mouseDragged(MouseEvent me) {
                Line2D.Float line = new Line2D.Float(initial.x, initial.y, me.getX(), me.getY());
                Shape lineShape = getLineShape(line, 10);
                Graphics g = me.getComponent().getGraphics();
                me.getComponent().paint(g);
                myStrokeShape = lineShape;
                ((Graphics2D) g).fill(lineShape);
            }

            public void mouseMoved(MouseEvent me) {
                if (myPath != null && myPath.contains(me.getPoint())) {
                    System.out.println("contains");
                }
                if (myArea != null && myArea.contains(me.getPoint())) {
                    System.out.println("area contains");
                }
                if (myStrokeShape != null && myStrokeShape.contains(me.getPoint())) {
                    System.out.println("stroke contains");
                }
            }
        };
        jp.addMouseListener(mml);
        jp.addMouseMotionListener(mml);
        WindowUtilities.visualize(jp);
    }

    public static BufferedImage optimizeImage(BufferedImage img) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        boolean istransparent = img.getColorModel().hasAlpha();
        BufferedImage img2 = gc.createCompatibleImage(img.getWidth(), img.getHeight(), istransparent ? Transparency.BITMASK : Transparency.OPAQUE);
        Graphics2D g = img2.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return img2;
    }

    public static List<String> getLinesForText(String txt, int wid, Font font, Graphics2D g, boolean whiteSpaceWrap) {
        List<String> lines = new ArrayList<String>();
        if (txt == null || txt.length() == 0) return lines;
        FontMetrics fm = g.getFontMetrics(font);
        int lineStart = 0;
        int lineEnd = txt.indexOf('\n');
        int cwid = fm.charWidth('t');
        int guess = Math.max(wid / cwid, 1);
        do {
            if (whiteSpaceWrap) {
                StringBuilder sb = new StringBuilder();
                char[] tester = new char[2 * guess];
                int idx = lineStart;
                while (idx < lineEnd) {
                    int times = Math.min(guess, lineEnd - idx);
                    for (int i = 0; i < times; i++) {
                        sb.append(txt.charAt(idx));
                        idx++;
                    }
                    int lnwid = 0;
                    do {
                        if (idx >= lineEnd) break;
                        sb.getChars(0, sb.length(), tester, 0);
                        lnwid = fm.charsWidth(tester, 0, sb.length());
                        idx++;
                    } while (lnwid < wid);
                    if (lnwid > wid) {
                        while (sb.length() != 0 && !Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                            sb.setLength(sb.length() - 1);
                            idx--;
                        }
                        idx--;
                    }
                    lines.add(sb.toString().trim());
                    sb.setLength(0);
                }
            } else {
                char[] chars = txt.toCharArray();
                int len1 = lineEnd - lineStart;
                int start = 0;
                if (len1 == 1) {
                    lines.add("");
                } else {
                    while (start < len1) {
                        System.out.println("loop");
                        int len = Math.min(len1 - start, guess);
                        System.out.println("start = " + start);
                        int seg_wid = fm.charsWidth(chars, lineStart + start, len);
                        if (seg_wid < wid) {
                            do {
                                if (len + start >= len1) break;
                                len++;
                                seg_wid = fm.charsWidth(chars, lineStart + start, len);
                            } while ((seg_wid < wid));
                            if (seg_wid > wid) {
                                len--;
                            }
                        } else while (seg_wid > wid) {
                            if (len == 1) break;
                            len--;
                            seg_wid = fm.charsWidth(chars, lineStart + start, len);
                        }
                        lines.add(txt.substring(lineStart + start, lineStart + start + len).trim());
                        start += len;
                    }
                }
            }
            if (lineEnd == -1) break;
            lineStart = lineEnd + 1;
            lineEnd = txt.indexOf('\n', lineStart);
        } while (lineEnd != -1);
        return lines;
    }

    public static void paintTextLines(Graphics2D g2, List<String> lines) {
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            g2.drawString(line, 0, (i + 1) * fm.getHeight());
        }
    }

    private static String kant;

    public static void getKant() {
        try {
            DataFetcher fetcher = IOUtils.loadData(new URL("http://interconnected.org/home/more/2000/08/kant/"));
            byte[] data = fetcher.readCompletely();
            kant = new String(data);
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    public static void fill(BufferedImage im, int x, int y, Color fillColor) {
        int oldColor = im.getRGB(x, y);
        ArrayList<Span<Integer>> areas = new ArrayList<Span<Integer>>();
        int start = x, end = x;
        while (im.getRGB(start - 1, y) == oldColor) {
            start--;
        }
        while (im.getRGB(end + 1, y) == oldColor) {
            end++;
        }
        Span<Integer> s = new Span<Integer>(start, end);
        areas.add(s);
    }

    public static void wrappedTextTest() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        final JPanel txt = new JPanel() {

            private static final long serialVersionUID = 1L;

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                List<String> lines = getLinesForText(kant, Math.max(300, getWidth()), getFont(), g2, false);
                PaintUtils.paintTextLines(g2, lines);
            }

            public Dimension getPreferredSize() {
                List<String> lines = getLinesForText(kant, Math.max(300, getWidth()), getFont(), (Graphics2D) getGraphics(), true);
                FontMetrics fm = getGraphics().getFontMetrics();
                return new Dimension(Math.max(300, getWidth()), lines.size() * fm.getHeight());
            }
        };
        JScrollPane sp = new JScrollPane(txt);
        p.add(sp, BorderLayout.CENTER);
        JFrame jf = new JFrame("wrapped text test");
        JMenuBar bar = new JMenuBar();
        JMenuItem reset = new JMenuItem("Reset");
        reset.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getKant();
                txt.setSize(txt.getPreferredSize());
                txt.repaint();
            }
        });
        bar.add(reset);
        jf.setJMenuBar(bar);
        jf.getContentPane().add(p);
        jf.setBounds(100, 100, 400, 400);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static Polygon createStandardStar(double width, double height, int points, double centerRatio, double angleOffset) {
        int pts = points * 2;
        int xs[] = new int[pts];
        int ys[] = new int[pts];
        double xrad = width / 2;
        double yrad = height / 2;
        int innerx = (int) (xrad * centerRatio);
        int innery = (int) (yrad * centerRatio);
        double startangle = 0 + angleOffset;
        double anglePer = 2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = startangle + anglePer * i;
            xs[i * 2] = (int) (xrad + xrad * Math.sin(angle));
            ys[i * 2] = (int) (yrad - yrad * Math.cos(angle));
            xs[i * 2 + 1] = (int) (xrad + innerx * Math.sin(angle + anglePer / 2));
            ys[i * 2 + 1] = (int) (yrad - innery * Math.cos(angle + anglePer / 2));
        }
        Polygon p = new Polygon(xs, ys, pts);
        return p;
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static void rotateArea(Area a, double rotation, Point2D rotateAround) {
        AffineTransform at1 = AffineTransform.getTranslateInstance(rotateAround.getX(), rotateAround.getY());
        at1.rotate(rotation);
        at1.translate(-rotateAround.getX(), -rotateAround.getY());
        a.transform(at1);
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static void rotateArea(Area a, double rotation) {
        Rectangle2D bounds = a.getBounds2D();
        rotateArea(a, rotation, new Point2D.Double(bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2));
    }

    /**
	 * @deprecated
	 * @see ShapeUtils
	 */
    public static Point getCenter(Shape s) {
        Rectangle r = s.getBounds();
        double totalX = 0, totalY = 0;
        int pts = 0;
        for (int i = 0; i < r.width; i++) {
            for (int j = 0; j < r.height; j++) {
                if (s.contains(i, j)) {
                    totalX += i;
                    totalY += j;
                    pts++;
                }
            }
        }
        totalX /= pts;
        totalY /= pts;
        return new Point((int) Math.round(totalX), (int) Math.round(totalY));
    }

    public static void rotateTest() {
        final Polygon p = createStandardStar(100, 100, 5, 0.2, 0);
        p.translate(50, 50);
        final Area myArea = new Area(p);
        final JPanel jp = new JPanel() {

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                ((Graphics2D) g).fill(myArea);
            }
        };
        jp.setPreferredSize(new Dimension(200, 200));
        Action a = new AbstractAction("Rotate The Star!") {

            public void actionPerformed(ActionEvent ae) {
                rotateArea(myArea, Math.PI / 6);
                jp.repaint();
            }
        };
        WindowUtilities.visualize(jp);
        WindowUtilities.visualize(a);
    }

    public static BufferedImage blend(BufferedImage im, Shape shape, Color paintColor, Color bgColor, double blendPct) {
        Point p = new Point();
        double actBlend = blendPct * paintColor.getAlpha() / 255.0;
        int paintRGB = paintColor.getRGB();
        int bgRGB = bgColor == null ? 0 : bgColor.getRGB();
        for (int i = 0; i < im.getWidth(); i++) {
            p.x = i;
            for (int j = 0; j < im.getHeight(); j++) {
                p.y = j;
                if (shape.contains(p)) {
                    int rgb = im.getRGB(i, j);
                    if (rgb == paintRGB) continue;
                    int newColor = 0;
                    if (rgb == bgRGB) {
                        newColor = paintRGB;
                    } else {
                        int r = (int) (paintColor.getRed() * actBlend + ((rgb & 0xFF0000) >> 16) * (1 - actBlend));
                        int g = (int) (paintColor.getGreen() * actBlend + ((rgb & 0x00FF00) >> 8) * (1 - actBlend));
                        int b = (int) (paintColor.getBlue() * actBlend + ((rgb & 0x0000FF)) * (1 - actBlend));
                        newColor = (r << 16) + (g << 8) + b;
                    }
                    im.setRGB(i, j, newColor);
                }
            }
        }
        return im;
    }

    public static void blendTest() {
        final BufferedImage im = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Ellipse2D.Double circle1 = new Ellipse2D.Double(0, 0, 40, 40);
        Ellipse2D.Double circle2 = new Ellipse2D.Double(40, 0, 40, 40);
        Ellipse2D.Double circle3 = new Ellipse2D.Double(20, 0, 40, 40);
        Graphics g = im.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 300, 300);
        g.dispose();
        blend(im, circle1, Color.RED, Color.WHITE, 0.5);
        blend(im, circle2, Color.BLUE, Color.WHITE, 0.5);
        blend(im, circle3, Color.YELLOW, Color.WHITE, 0.5);
        JPanel jp = new JPanel() {

            public void paintComponent(Graphics g) {
                g.drawImage(im, 0, 0, null);
            }
        };
        WindowUtilities.visualize(jp);
    }

    public static void main(String[] args) {
        blendTest();
    }

    public static void standardStarTest() {
        final Polygon gon = createStandardStar(100, 100, 5, .3, 0);
        JPanel jp = new JPanel() {

            private static final long serialVersionUID = 1L;

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                ((Graphics2D) g).fill(gon);
                g.drawLine(gon.xpoints[gon.npoints - 1], gon.ypoints[gon.npoints - 1], gon.xpoints[0], gon.ypoints[0]);
            }
        };
        jp.setPreferredSize(new Dimension(100, 100));
        WindowUtilities.visualize(jp);
    }

    public static void arcTest() {
        JPanel jp = new JPanel() {

            private static final long serialVersionUID = 1L;

            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.RED);
                drawArc(g2, 10, 10, 50, 50, Math.PI / 2, 3 * Math.PI / 2, 12, 0);
                g2.setColor(Color.MAGENTA);
                fillArc(g2, 60, 10, 50, 50, 0, 3 * Math.PI / 4, 6, 6);
                g2.setColor(Color.GREEN);
                fillArc(g2, 10, 60, 50, 50, 0, 3.5 * Math.PI / 2, 15, 6);
            }
        };
        jp.setPreferredSize(new Dimension(100, 100));
        WindowUtilities.visualize(jp);
    }
}
