package com.tavanduc.uml.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import com.daohoangson.uml.gui.Diagram;
import com.daohoangson.uml.structures.Structure;

/**
 * Base Relationship class. No instance of this class should be created.
 * 
 * @author Dao Hoang Son
 * @version 1.2
 * 
 */
public abstract class Relationship {

    /**
	 * The original diagram
	 */
    private Diagram diagram;

    /**
	 * The 2 structures involved in this relationship
	 */
    private Structure from, to;

    /**
	 * The color to be drawn
	 */
    protected Color cfg_color = Color.BLACK;

    /**
	 * The length of dashes. SPecifies 0 to draw straight line (no dash)
	 */
    protected int cfg_dash_length = 0;

    /**
	 * The distance between lines and components
	 */
    protected int cfg_distance;

    /**
	 * Determines if we are in debug mode.
	 */
    public static boolean debugging = false;

    /**
	 * Constructor
	 * 
	 * @param diagram
	 *            the root diagram
	 * @param from
	 *            the source structure
	 * @param to
	 *            the destination structure
	 */
    public Relationship(Diagram diagram, Structure from, Structure to) {
        this.diagram = diagram;
        this.from = from;
        this.to = to;
    }

    /**
	 * Gets the source structure
	 * 
	 * @return the source structure
	 */
    public Structure getFrom() {
        return from;
    }

    /**
	 * Gets the destination structure
	 * 
	 * @return the destination structure
	 */
    public Structure getTo() {
        return to;
    }

    /**
	 * Customized primary drawing method for each relationships. Must be
	 * overridden in subclasses and should call
	 * {@link #drawConnectingPath(Graphics, double, double)} with appropriate
	 * arguments
	 * 
	 * @param g
	 *            the target <code>Graphics</code> to be drawn
	 * @param size_factor
	 *            specifies the zooming level of the relationship. You may want
	 *            to take a look at {@link Diagram#setSizeFactor(float)}
	 */
    public abstract void draw(Graphics g, float size_factor);

    /**
	 * Customized drawing method for each relationship. Must be overridden in
	 * subclasses.
	 * 
	 * @param g
	 *            the target graphics (the same passed in
	 *            {@link #draw(Graphics, float)}
	 * @param ps
	 *            the {@link PointSet} object holding drawing data
	 */
    protected abstract void drawEndPoints(Graphics g, PointSet ps);

    /**
	 * Selects and draws the main connection line(s). Finally, draw end points
	 * by calling subclass's {@link #drawEndPoints(Graphics, PointSet)}
	 * 
	 * @param g
	 *            the target graphics
	 * @param start_length
	 *            the length should be left at the start
	 * @param end_length
	 *            the length should be left at the end
	 */
    protected void drawConnectingPath(Graphics g, double start_length, double end_length) {
        if (Relationship.debugging) {
            System.err.println("Drawing from " + getFrom() + " to " + getTo());
        }
        Rectangle fromBound = diagram.getBoundsFor(getFrom());
        Rectangle toBound = diagram.getBoundsFor(getTo());
        if (fromBound == null || toBound == null) {
            return;
        }
        PointSet ps;
        Color original_color = g.getColor();
        g.setColor(cfg_color);
        if (fromBound.equals(toBound)) {
            int x1 = fromBound.x + fromBound.width;
            int y1 = fromBound.y + fromBound.height / 3 * 2;
            int x3 = (int) (x1 + Math.max(Math.max(start_length, end_length), 15) + 5);
            int y3 = fromBound.y + fromBound.height / 3;
            ps = drawPath(g, x1, y1, x3, y3, start_length, end_length, 39);
        } else if (fromBound.y + fromBound.height < toBound.y) {
            int x1 = fromBound.x + fromBound.width / 2;
            int y1 = fromBound.y + fromBound.height;
            int x2;
            if (fromBound.x < toBound.x) {
                x2 = toBound.x;
            } else {
                x2 = toBound.x + toBound.width;
            }
            int y2 = toBound.y + toBound.height / 2;
            ps = drawPath(g, x1, y1, x2, y2, start_length, end_length, 20);
        } else if (fromBound.y > toBound.y + toBound.height) {
            int x1 = fromBound.x + fromBound.width / 2;
            int y1 = fromBound.y;
            int x2 = toBound.x + toBound.width / 2;
            int y2 = toBound.y + toBound.height;
            ps = drawPath(g, x1, y1, x2, y2, start_length, end_length, 30);
        } else {
            int x1, y1, x2, y2;
            if (fromBound.x + fromBound.width < toBound.x) {
                x2 = toBound.x;
                y2 = toBound.y + toBound.height / 2;
                x1 = fromBound.x + fromBound.width;
                y1 = y2;
            } else {
                x2 = toBound.x + toBound.width;
                y2 = toBound.y + toBound.height / 2;
                x1 = fromBound.x;
                y1 = y2;
            }
            if (fromBound.y + 1 < y1 && y1 < fromBound.y + fromBound.height - 1) {
                ps = drawLine(g, x1, y1, x2, y2, start_length, end_length);
            } else {
                x1 = fromBound.x + fromBound.width / 2;
                if (y1 < fromBound.y) {
                    y1 = fromBound.y;
                } else {
                    y1 = fromBound.y + fromBound.height;
                }
                ps = drawPath(g, x1, y1, x2, y2, start_length, end_length, 20);
            }
        }
        drawEndPoints(g, ps);
        g.setColor(original_color);
    }

    /**
	 * Calculates and draw a straight line from 2 points (must be a vertical
	 * line or a horizontal line)
	 * 
	 * @param g
	 *            the <code>Graphics</code> object to be drawn
	 * @param x1
	 *            the x coordinate of the first point
	 * @param y1
	 *            the y coordinate of the first point
	 * @param x2
	 *            the x coordinate of the second point
	 * @param y2
	 *            the y coordinate of the second point
	 * @param start_length
	 *            the length to be left at the start
	 * @param end_length
	 *            the length to be left at the end
	 * @return a {@link PointSet} containing left lines and the delta angles
	 */
    private PointSet drawLine(Graphics g, int x1, int y1, int x2, int y2, double start_length, double end_length) {
        if (Relationship.debugging) {
            System.err.println("Drawing from " + x1 + "," + y1 + " to " + x2 + "," + y2 + ". Start = " + start_length + ". End = " + end_length);
        }
        double delta;
        int x3, y3, x4, y4;
        if (y1 != y2) {
            if (x1 < x2) {
                delta = Math.atan((x2 - x1) / (double) (y2 - y1)) + Math.PI;
            } else {
                delta = Math.atan((x2 - x1) / (double) (y2 - y1));
            }
        } else {
            if (x1 < x2) {
                delta = Math.PI * 0.5;
            } else {
                delta = Math.PI * 0.5 * -1;
            }
        }
        x3 = (int) Math.ceil(x1 - start_length * Math.sin(delta));
        y3 = (int) Math.ceil(y1 + start_length * Math.cos(delta));
        x4 = (int) Math.ceil(x2 - end_length * Math.sin(delta));
        y4 = (int) Math.ceil(y2 + end_length * Math.cos(delta));
        PointSet ps = drawLineSmart(g, x3, y3, x4, y4);
        return new PointSet(x1, y1, ps.x1, ps.y1, delta, ps.x2, ps.y2, x2, y2, delta);
    }

    /**
	 * Draws a straight line between 2 points but avoid all components on the
	 * way
	 * 
	 * @param g
	 *            the <code>Graphics</code> object to be drawn
	 * @param x1
	 *            the x coordinate of the first point
	 * @param y1
	 *            the y coordinate of the first point
	 * @param x2
	 *            the x coordinate of the second point
	 * @param y2
	 *            the y coordinate of the second point
	 * 
	 * @see #onTheWay(Rectangle[], int, int, int, int)
	 */
    private PointSet drawLineSmart(Graphics g, int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2) {
            return new PointSet(x1, y1, x2, y2, -1);
        }
        Rectangle[] onTheWay = Relationship.onTheWay(diagram.getBoundsForAll(), x1, y1, x2, y2);
        int x0 = x1;
        int y0 = y1;
        int x = x0;
        int y = y0;
        for (int i = 0; i < onTheWay.length; i++) {
            Rectangle rect = onTheWay[i];
            if (x1 == x2) {
                int p1y, p2y, p3y;
                int px = rect.x - cfg_distance;
                if (y1 < y2) {
                    p1y = y;
                    p2y = rect.y - cfg_distance;
                    p3y = rect.y + rect.height + cfg_distance;
                } else {
                    p1y = y;
                    p2y = rect.y + rect.height + cfg_distance;
                    p3y = rect.y - cfg_distance;
                }
                if (Math.abs(rect.x - cfg_distance - x) < Math.abs(rect.x + rect.width + cfg_distance - x)) {
                    px = rect.x - cfg_distance;
                } else {
                    px = rect.x + rect.width + cfg_distance;
                }
                if (!Relationship.isBetween(p2y, y1, y2)) {
                    y0 = p3y;
                    y = p3y;
                } else if (!Relationship.isBetween(p3y, y1, y2)) {
                    drawLinePlain(g, x, p1y, x, p2y);
                    y = p2y;
                    return new PointSet(x0, y0, x, y, -1);
                } else {
                    drawLinePlain(g, x, p1y, x, p2y);
                    drawLinePlain(g, x, p2y, px, p2y);
                    drawLinePlain(g, px, p2y, px, p3y);
                    drawLinePlain(g, px, p3y, x, p3y);
                    y = p3y;
                }
            } else {
                int p1x, p2x, p3x, py;
                if (x1 < x2) {
                    p1x = x;
                    p2x = rect.x - cfg_distance;
                    p3x = rect.x + rect.width + cfg_distance;
                } else {
                    p1x = x;
                    p2x = rect.x + rect.width + cfg_distance;
                    p3x = rect.x - cfg_distance;
                }
                if (Math.abs(rect.y - cfg_distance - y) < Math.abs(rect.y + rect.height + cfg_distance - y)) {
                    py = rect.y - cfg_distance;
                } else {
                    py = rect.y + rect.height + cfg_distance;
                }
                if (!Relationship.isBetween(p2x, x1, x2)) {
                    x0 = p3x;
                    x = p3x;
                } else if (!Relationship.isBetween(p3x, x1, x2)) {
                    drawLinePlain(g, p1x, y, p2x, y);
                    x = p2x;
                    return new PointSet(x0, y0, x, y, -1);
                } else {
                    drawLinePlain(g, p1x, y, p2x, y);
                    drawLinePlain(g, p2x, y, p2x, py);
                    drawLinePlain(g, p2x, py, p3x, py);
                    drawLinePlain(g, p3x, py, p3x, y);
                    x = p3x;
                }
            }
        }
        if (x != x2 || y != y2) {
            drawLinePlain(g, x, y, x2, y2);
            x = x2;
            y = y2;
        }
        return new PointSet(x0, y0, x, y, -1);
    }

    /**
	 * Draws a straight line or a dashed line between 2 points
	 * 
	 * @param g
	 *            the <code>Graphics</code> object to be drawn
	 * @param x1
	 *            the x coordinate of the first point
	 * @param y1
	 *            the y coordinate of the first point
	 * @param x2
	 *            the x coordinate of the second point
	 * @param y2
	 *            the y coordinate of the second point
	 * 
	 * @see #cfg_dash_length
	 */
    private void drawLinePlain(Graphics g, int x1, int y1, int x2, int y2) {
        if (cfg_dash_length == 0) {
            g.drawLine(x1, y1, x2, y2);
        } else {
            Graphics2D g2d = (Graphics2D) g;
            Stroke backup = g2d.getStroke();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { cfg_dash_length }, 0);
            g2d.setStroke(dashed);
            g2d.drawLine(x1, y1, x2, y2);
            g2d.setStroke(backup);
        }
    }

    /**
	 * Draws the path between 2 points
	 * 
	 * @param g
	 *            the <code>Graphics</code> object to be drawn
	 * @param x1
	 *            the x coordinate of the first point
	 * @param y1
	 *            the y coordinate of the first point
	 * @param x2
	 *            the x coordinate of the second point
	 * @param y2
	 *            the y coordinate of the second point
	 * @param start_length
	 *            the length to be left at the start
	 * @param end_length
	 *            the length to be left at the end
	 * @param mode
	 *            the mode for path. Possible values are
	 *            <ul>
	 *            <li>30: 3 lines. Go half the height before turning</li>
	 *            <li>31: 3 lines. Go half the width before turning</li>
	 *            <li>39: 3 lines. (x1,y1) to (x2,y1) to (x2,y2) and finally
	 *            come back to (x1,y2)</li>
	 *            <li>20: 2 lines. Keep the x coordinate as long as possible</li>
	 *            <li>21: 2 lines. Keep the y coordinate as long as possible</li>
	 *            </ul>
	 * @return a {@link PointSet} containing left lines and the delta angles
	 */
    private PointSet drawPath(Graphics g, int x1, int y1, int x2, int y2, double start_length, double end_length, int mode) {
        if (x1 == x2 && y1 == y2) {
            return new PointSet(x1, y1, x2, y2, -1);
        }
        int tmp_x1, tmp_y1, tmp_x2, tmp_y2;
        switch(mode) {
            case 30:
                tmp_x1 = x1;
                tmp_y1 = (y1 + y2) / 2;
                tmp_x2 = x2;
                tmp_y2 = tmp_y1;
                break;
            case 31:
                tmp_x1 = (x1 + x2) / 2;
                tmp_y1 = y1;
                tmp_x2 = tmp_x1;
                tmp_y2 = y2;
                break;
            case 39:
                tmp_x1 = x2;
                tmp_y1 = y1;
                tmp_x2 = x2;
                tmp_y2 = y2;
                x2 = x1;
                break;
            case 20:
                tmp_x1 = x1;
                tmp_y1 = y2;
                tmp_x2 = tmp_x1;
                tmp_y2 = tmp_y1;
                break;
            case 21:
            default:
                tmp_x1 = x2;
                tmp_y1 = y1;
                tmp_x2 = tmp_x1;
                tmp_y2 = tmp_y1;
                break;
        }
        PointSet ps1 = drawLine(g, x1, y1, tmp_x1, tmp_y1, start_length, 0);
        PointSet ps2 = drawLine(g, tmp_x1, tmp_y1, tmp_x2, tmp_y2, 0, 0);
        PointSet ps3 = drawLine(g, tmp_x2, tmp_y2, x2, y2, 0, end_length);
        if (tmp_x1 != tmp_x2 || tmp_y1 != tmp_y2) {
            if (ps1.x3 != tmp_x1 || ps1.y3 != tmp_y1) {
                drawPath(g, ps1.x3, ps1.y3, ps2.x2, ps2.y2, 0, 0, 20 + 1 - mode % 2);
            }
            if (ps2.x3 != tmp_x2 || ps2.y3 != tmp_y2) {
                drawPath(g, ps2.x3, ps2.y3, ps3.x2, ps3.y2, 0, 0, 20 + mode % 2);
            }
        } else {
            if (ps1.x3 != tmp_x1 || ps1.y3 != tmp_y1) {
                drawPath(g, ps1.x3, ps1.y3, ps3.x2, ps3.y2, 0, 0, 20 + 1 - mode % 2);
            }
        }
        return new PointSet(x1, y1, ps1.x2, ps1.y2, ps1.delta1, ps3.x3, ps3.y3, x2, y2, ps3.delta2);
    }

    /**
	 * Checks if a number is between 2 other numbers
	 * 
	 * @param x
	 *            the number need checking
	 * @param x1
	 *            the range first edge
	 * @param x2
	 *            the range second edge
	 * @return true if it is between (inclusive)
	 */
    private static boolean isBetween(int x, int x1, int x2) {
        if (x1 > x2) {
            return x1 >= x && x >= x2;
        } else {
            return x1 <= x && x <= x2;
        }
    }

    /**
	 * Checks if a range of numbers is overlap another range
	 * 
	 * @param range1_x1
	 *            the first range first edge
	 * @param range1_x2
	 *            the first range second edge
	 * @param range2_x1
	 *            the second range first edge
	 * @param range2_x2
	 *            the second range second edge
	 * @return true if the 2 ranges are overlapped (not inclusive)
	 */
    private static boolean isOverlap(int range1_x1, int range1_x2, int range2_x1, int range2_x2) {
        if (range2_x1 < range2_x2) {
            range2_x1++;
            range2_x2--;
        } else {
            range2_x1--;
            range2_x2++;
        }
        return Relationship.isBetween(range1_x1, range2_x1, range2_x2) || Relationship.isBetween(range1_x2, range2_x1, range2_x2);
    }

    /**
	 * Finds all rectangles on the way of a line between 2 points
	 * 
	 * @param rectangles
	 *            an array of rectangles to check against
	 * @param x1
	 *            the x coordinate of the first point
	 * @param y1
	 *            the y coordinate of the first point
	 * @param x2
	 *            the x coordinate of the second point
	 * @param y2
	 *            the y coordinate of the second point
	 * @return an array of found rectangles
	 */
    private static Rectangle[] onTheWay(Rectangle[] rectangles, int x1, int y1, int x2, int y2) {
        List<Rectangle> found = new LinkedList<Rectangle>();
        for (int i = 0; i < rectangles.length; i++) {
            Rectangle rect = rectangles[i];
            int xmin = rect.x;
            int xmax = rect.x + rect.width;
            int ymin = rect.y;
            int ymax = rect.y + rect.height;
            boolean isOnTheWay = false;
            if (x1 == x2) {
                if (xmin < x1 && x1 < xmax && Relationship.isOverlap(rect.y, rect.y + rect.height, y1, y2)) {
                    isOnTheWay = true;
                }
            }
            if (y1 == y2) {
                if (ymin < y1 && y1 < ymax && Relationship.isOverlap(rect.x, rect.x + rect.width, x1, x2)) {
                    isOnTheWay = true;
                }
            }
            if (isOnTheWay) {
                found.add(rect);
            }
        }
        int comparator_direction;
        if (x1 < x2 || y1 < y2) {
            comparator_direction = 1;
        } else {
            comparator_direction = -1;
        }
        Collections.sort(found, new RectangleComparator(comparator_direction));
        if (Relationship.debugging) {
            System.err.println(x1 + "," + y1 + " ~> " + x2 + "," + y2 + ": " + found);
        }
        return found.toArray(new Rectangle[0]);
    }
}

/**
 * A temporary class to hold some important data
 * 
 * @author Dao Hoang Son
 * @version 1.0
 * 
 * @see Relationship#drawConnectingPath(Graphics, double, double)
 * 
 */
class PointSet {

    int x1, y1, x2, y2, x3, y3, x4, y4;

    double delta1, delta2;

    PointSet(int x1, int y1, int x2, int y2, double delta1, int x3, int y3, int x4, int y4, double delta2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.delta1 = delta1;
        this.x3 = x3;
        this.y3 = y3;
        this.x4 = x4;
        this.y4 = y4;
        this.delta2 = delta2;
    }

    PointSet(int x1, int y1, int x2, int y2, double delta1) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.delta1 = delta1;
        x3 = x1;
        y3 = y1;
        x4 = x2;
        y4 = y2;
        delta2 = delta1;
    }
}

class RectangleComparator implements Comparator<Rectangle> {

    private int direction;

    public RectangleComparator(int direction) {
        this.direction = direction;
    }

    public RectangleComparator() {
        this(1);
    }

    @Override
    public int compare(Rectangle s1, Rectangle s2) {
        int result = cmp(s1.x, s2.x);
        if (result == 0) {
            result = cmp(s1.y, s2.y);
        }
        if (result == 0) {
            result = cmp(s1.width, s2.width);
        }
        if (result == 0) {
            result = cmp(s1.height, s2.height);
        }
        return result * direction;
    }

    private int cmp(int x, int y) {
        if (x == y) {
            return 0;
        } else if (x > y) {
            return 1;
        } else {
            return -1;
        }
    }
}
