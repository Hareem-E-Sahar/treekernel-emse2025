package net.sf.refactorit.ui.graph;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * @author Anton Safonov
 */
public abstract class BinEdge extends Edge {

    public static final int OWNERSHIP_DEFAULT_LENGTH = DEFAULT_LENGTH * 2;

    public static final int LOCAL_DEPENDENCY_DEFAULT_LENGTH = OWNERSHIP_DEFAULT_LENGTH * 2;

    public static final int GLOBAL_DEPENDENCY_DEFAULT_LENGTH = LOCAL_DEPENDENCY_DEFAULT_LENGTH * 2;

    public static final int INHERITANCE_DEFAULT_LENGTH = OWNERSHIP_DEFAULT_LENGTH * 4;

    protected static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    static {
        setEdgeMouseOverColor(new Color(32, 32, 32));
    }

    private int numberOfDeps = 1;

    private boolean bidirectional = false;

    public static final BinEdge createFor(TGPanel panel, BinNode fromNode, BinNode toNode, boolean ownership) {
        if (toNode == null || fromNode == null || toNode == fromNode) {
            return null;
        }
        BinEdge edge = fromNode.fastFindEdge(toNode);
        if (edge != null) {
            edge.tighten();
        } else {
            if (ownership) {
                if (fromNode.getBin() instanceof BinMethod) {
                    edge = new OwnershipMethodEdge(fromNode, toNode);
                } else {
                    edge = new OwnershipEdge(fromNode, toNode);
                }
                panel.addEdge(edge);
            } else {
                edge = toNode.fastFindEdge(fromNode);
                if (edge != null) {
                    edge.tighten();
                    edge.setBidirectional(true);
                } else {
                    if (isInherits(fromNode.getBin(), toNode.getBin())) {
                        if (toNode.getBin() instanceof BinInterface) {
                            edge = new InterfaceInheritanceEdge(fromNode, toNode);
                        } else {
                            edge = new InheritanceEdge(fromNode, toNode);
                        }
                    } else {
                        if (isNeighbour(fromNode.getBin(), toNode.getBin())) {
                            edge = new LocalDependencyEdge(fromNode, toNode);
                        } else {
                            edge = new GlobalDependencyEdge(fromNode, toNode);
                        }
                    }
                    panel.addEdge(edge);
                }
            }
        }
        return edge;
    }

    private static final boolean isNeighbour(final BinItem bin1, final BinItem bin2) {
        if (bin1 == bin2) {
            return true;
        }
        BinItemVisitable parent1 = BinClassificator.getParent(bin1);
        BinItemVisitable parent2 = BinClassificator.getParent(bin2);
        if (parent1 == null || parent2 == null || parent1 instanceof Project || parent2 instanceof Project) {
            return false;
        }
        if (parent1 == parent2 || bin1 == parent2 || parent1 == bin2) {
            return true;
        }
        return false;
    }

    private static final boolean isInherits(final BinItem bin1, final BinItem bin2) {
        if (bin1 == bin2 || !(bin1 instanceof BinCIType) || !(bin2 instanceof BinCIType)) {
            return false;
        }
        return ((BinCIType) bin1).getTypeRef().isDerivedFrom(((BinCIType) bin2).getTypeRef());
    }

    public int getDefaultLength() {
        return Edge.DEFAULT_LENGTH;
    }

    public int tighten() {
        int len = (int) (getLength() * 0.9);
        if (len < (getDefaultLength() / 2)) {
            len = (getDefaultLength() / 2);
        }
        setLength(len);
        ++this.numberOfDeps;
        return len;
    }

    protected BinEdge(BinNode fromNode, BinNode toNode, int len) {
        super(fromNode, toNode, len);
    }

    public BinItem getBinFrom() {
        return ((BinNode) getFrom()).getBin();
    }

    public BinItem getBinTo() {
        return ((BinNode) getTo()).getBin();
    }

    public abstract boolean isDependency();

    public String getDescription() {
        return null;
    }

    public void paint(Graphics g, TGPanel tgPanel) {
        if (!intersects(tgPanel.getSize())) {
            return;
        }
        Stroke old = null;
        if (highlighted) {
            old = ((Graphics2D) g).getStroke();
            ((Graphics2D) g).setStroke(new BasicStroke(Edge.HIGHLIGHT_WIDTH));
        }
        int x1 = (int) getFromDrawX();
        int y1 = (int) getFromDrawY();
        int x2 = (int) getToDrawX();
        int y2 = (int) getToDrawY();
        boolean isOver = tgPanel.getMouseOverE() == this;
        g.setColor((highlighted) ? HIGHLIGHT_COLOR : (isOver) ? MOUSE_OVER_COLOR : col);
        paintToEnd(g, x1, y1, x2, y2);
        paintFromEnd(g, x1, y1, x2, y2);
        if (isOver) {
            int x3;
            int y3;
            Point point = getOverPoint();
            if (point != null && Math.abs(y1 - y2) > 40) {
                y3 = (int) point.getY() - 5;
                x3 = x1 - (x1 - x2) * (y1 - y3) / (y1 - y2);
            } else {
                x3 = (x1 + x2) / 2;
                y3 = (y1 + y2) / 2;
            }
            y3 += 3;
            String desc = getDescription();
            if (desc != null) {
                g.setColor(Color.black);
                g.drawString(desc, x3 + 6, y3);
            }
            if (isDependency()) {
                Font oldFont = g.getFont();
                g.setFont(Node.SMALL_TAG_FONT);
                g.setColor(col);
                final String str = Integer.toString(this.numberOfDeps);
                final int stringWidth = g.getFontMetrics().stringWidth(str);
                final int ovalWidth = stringWidth + 5;
                g.fillOval(x3 - ovalWidth / 2, y3 - 9, ovalWidth, 11);
                g.setColor(Color.white);
                g.drawString(str, x3 - stringWidth / 2, y3);
                g.setFont(oldFont);
            }
        }
        if (old != null) {
            ((Graphics2D) g).setStroke(old);
        }
    }

    protected abstract void paintToEnd(Graphics g, int x1, int y1, int x2, int y2);

    protected void paintFromEnd(Graphics g, int x1, int y1, int x2, int y2) {
    }

    public static final double distance(final double x1, final double y1, final double x2, final double y2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static final int sign(final int number) {
        return number < 0 ? -1 : 1;
    }

    public static final int sign(final double number) {
        return number < 0 ? -1 : 1;
    }

    /** Decenters 'to' end depending on the angle */
    public double getToDrawX() {
        double w2 = to.getWidth() / 2 - 4;
        double x1 = from.drawx;
        double y1 = from.drawy;
        double x2 = to.drawx;
        double y2 = to.drawy;
        final double distance = distance(x1, y1, x2, y2);
        if (distance > 0) {
            double shift = (w2 * (x1 - x2)) / distance;
            x2 += shift;
        }
        return x2;
    }

    /** Decenters 'to' end depending on the angle */
    public double getFromDrawX() {
        double w2 = from.getWidth() / 4 - 4;
        double x1 = from.drawx;
        double y1 = from.drawy;
        double x2 = to.drawx;
        double y2 = to.drawy;
        final double distance = distance(x1, y1, x2, y2);
        if (distance > 0) {
            double shift = (w2 * (x2 - x1)) / distance;
            x1 += shift;
        }
        return x1;
    }

    protected double[] calculateEnd(double x1, double y1, double x2, double y2, double h2, double w2, double radius) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dy2, dx2;
        if (dx == 0) {
            dx2 = 0;
            dy2 = (h2 + radius) * sign(dy);
        } else if (dy == 0) {
            dx2 = (w2 + radius) * sign(dx);
            dy2 = 0;
        } else {
            dy2 = (h2 + radius) * sign(dy);
            dx2 = dy2 * dx / dy;
            if (Math.abs(dx2) > (w2 + radius)) {
                dx2 = (w2 + radius) * sign(dx);
                dy2 = dx2 * dy / dx;
            }
        }
        double x3 = x2 + dx2;
        double y3 = y2 + dy2;
        if (x3 < x2) {
            x3 -= 1;
        }
        if (y3 < y2) {
            y3 -= 1;
        } else {
            y3 += 1;
        }
        double angle;
        if (dx == 0) {
            angle = Math.PI / 2 * sign(dy);
        } else {
            angle = Math.atan(dy / dx);
            if (dx < 0) {
                angle += Math.PI;
            }
        }
        return new double[] { x3, y3, angle };
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }

    public void setBidirectional(final boolean bidirectional) {
        this.bidirectional = bidirectional;
    }
}

class OwnershipEdge extends BinEdge {

    private static final int RADIUS = 4;

    private static final int DIAMETER = RADIUS * 2;

    protected OwnershipEdge(BinNode fromNode, BinNode toNode) {
        this(fromNode, toNode, OWNERSHIP_DEFAULT_LENGTH);
    }

    protected OwnershipEdge(BinNode fromNode, BinNode toNode, int len) {
        super(fromNode, toNode, len);
        setColor(Color.blue);
    }

    public int getDefaultLength() {
        return OWNERSHIP_DEFAULT_LENGTH;
    }

    public boolean isDependency() {
        return false;
    }

    public String getDescription() {
        return "<<belongs to>>";
    }

    /** To move wide ownership and depends edges */
    public double getToDrawX() {
        return super.getToDrawX() + 3;
    }

    protected void paintToEnd(Graphics g, int x1, int y1, int x2, int y2) {
        if (((BinNode) getFrom()).getBin() instanceof BinCIType) {
            paintNested(g, x1, y1, x2, y2);
        } else {
            paintComposition(g, x1, y1, x2, y2);
        }
    }

    /** UML Notation Guide 3.27: Nested Class Declarations */
    protected void paintNested(Graphics g, int x1, int y1, int x2, int y2) {
        double h2 = to.getHeight() / 2;
        double w2 = to.getWidth() / 2 - Math.abs(x2 - to.drawx);
        double[] newCoord = calculateEnd(x1, y1, x2, y2, h2, w2, RADIUS);
        int x3 = (int) newCoord[0];
        int y3 = (int) newCoord[1];
        double angle = newCoord[2];
        g.drawLine(x1, y1, x3, y3);
        Object antiAliasHint = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).translate(x3, y3);
        g.drawOval(-RADIUS, -RADIUS, DIAMETER, DIAMETER);
        if (angle != 0) {
            ((Graphics2D) g).rotate(angle);
        }
        g.drawLine(-RADIUS, 0, RADIUS, 0);
        g.drawLine(0, -RADIUS, 0, RADIUS);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasHint);
        ((Graphics2D) g).setTransform(IDENTITY_TRANSFORM);
    }

    /** UML Notation Guide 3.48: Composition */
    protected void paintComposition(Graphics g, int x1, int y1, int x2, int y2) {
        double h2 = to.getHeight() / 2;
        double w2 = to.getWidth() / 2 - Math.abs(x2 - to.drawx);
        double[] newCoord = calculateEnd(x1, y1, x2, y2, h2, w2, 0);
        int x3 = (int) newCoord[0];
        int y3 = (int) newCoord[1];
        double angle = newCoord[2];
        g.drawLine(x1, y1, x3, y3);
        ((Graphics2D) g).translate(x3, y3);
        if (angle != 0) {
            ((Graphics2D) g).rotate(angle);
        }
        Object antiAliasHint = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillPolygon(new int[] { 0, 5, 10, 5 }, new int[] { 0, -4, 0, 4 }, 4);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasHint);
        ((Graphics2D) g).setTransform(IDENTITY_TRANSFORM);
    }
}

class OwnershipMethodEdge extends OwnershipEdge {

    public static final int OWNERSHIP_METHOD_DEFAULT_LENGTH = (int) (OWNERSHIP_DEFAULT_LENGTH * 1.5);

    protected OwnershipMethodEdge(BinNode fromNode, BinNode toNode) {
        super(fromNode, toNode, OWNERSHIP_METHOD_DEFAULT_LENGTH);
    }

    public int getDefaultLength() {
        return OWNERSHIP_METHOD_DEFAULT_LENGTH;
    }
}

class LocalDependencyEdge extends BinEdge {

    protected LocalDependencyEdge(BinNode fromNode, BinNode toNode, int len, Color color) {
        super(fromNode, toNode, len);
        setColor(color);
    }

    protected LocalDependencyEdge(BinNode fromNode, BinNode toNode) {
        this(fromNode, toNode, LOCAL_DEPENDENCY_DEFAULT_LENGTH, Color.red);
    }

    public int getDefaultLength() {
        return LOCAL_DEPENDENCY_DEFAULT_LENGTH;
    }

    public boolean isDependency() {
        return true;
    }

    public String getDescription() {
        return "<<depends on>>";
    }

    /** To move wide ownership and depends edges */
    public double getToDrawX() {
        return super.getToDrawX() - 3;
    }

    protected void paintToEnd(Graphics g, int x1, int y1, int x2, int y2) {
        double h2 = to.getHeight() / 2;
        double w2 = to.getWidth() / 2 - Math.abs(x2 - to.drawx);
        double[] newCoord = calculateEnd(x1, y1, x2, y2, h2, w2, 0);
        int x3 = (int) newCoord[0];
        int y3 = (int) newCoord[1];
        double angle = newCoord[2];
        paintLine(g, x1, y1, x3, y3);
        ((Graphics2D) g).translate(x3, y3);
        if (angle != 0) {
            ((Graphics2D) g).rotate(angle);
        }
        Object antiAliasHint = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintArrow(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasHint);
        ((Graphics2D) g).setTransform(IDENTITY_TRANSFORM);
    }

    protected void paintLine(final Graphics g, final int x1, final int y1, final int x3, final int y3) {
        g.drawLine(x1, y1, x3, y3);
    }

    protected void paintArrow(final Graphics g) {
        g.drawLine(0, 0, 9, -4);
        g.drawLine(0, 0, 9, 4);
    }

    protected void paintFromEnd(Graphics g, int x1, int y1, int x2, int y2) {
        double h2 = from.getHeight() / 2;
        double w2 = from.getWidth() / 2 - Math.abs(x1 - from.drawx);
        double[] newCoord = calculateEnd(x2, y2, x1, y1, h2, w2, 0);
        int x3 = (int) newCoord[0];
        int y3 = (int) newCoord[1];
        double angle = newCoord[2];
        ((Graphics2D) g).translate(x3, y3);
        if (angle != 0) {
            ((Graphics2D) g).rotate(angle);
        }
        Object antiAliasHint = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isBidirectional()) {
            g.drawLine(0, 0, 9, -4);
            g.drawLine(0, 0, 9, 4);
        } else {
            g.fillOval(-1, -2, 4, 4);
        }
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasHint);
        ((Graphics2D) g).setTransform(IDENTITY_TRANSFORM);
    }
}

class GlobalDependencyEdge extends LocalDependencyEdge {

    protected GlobalDependencyEdge(BinNode fromNode, BinNode toNode) {
        super(fromNode, toNode, GLOBAL_DEPENDENCY_DEFAULT_LENGTH, Color.red);
    }

    public int getDefaultLength() {
        return GLOBAL_DEPENDENCY_DEFAULT_LENGTH;
    }
}

class InheritanceEdge extends LocalDependencyEdge {

    protected InheritanceEdge(BinNode fromNode, BinNode toNode) {
        super(fromNode, toNode, INHERITANCE_DEFAULT_LENGTH, new Color(0, 196, 0));
    }

    public String getDescription() {
        return "<<extends>>";
    }

    public int getDefaultLength() {
        return INHERITANCE_DEFAULT_LENGTH;
    }

    private static final int SIZE = 8;

    private static final int HALF_SIZE = SIZE / 2;

    protected void paintArrow(final Graphics g) {
        g.drawLine(0, 0, SIZE, -HALF_SIZE);
        g.drawLine(SIZE, -HALF_SIZE, SIZE, HALF_SIZE);
        g.drawLine(0, 0, SIZE, HALF_SIZE);
        Color c = g.getColor();
        g.setColor(TGPanel.BACK_COLOR);
        g.drawLine(1, 0, SIZE - 1, 0);
        g.setColor(c);
    }
}

class InterfaceInheritanceEdge extends InheritanceEdge {

    protected InterfaceInheritanceEdge(BinNode fromNode, BinNode toNode) {
        super(fromNode, toNode);
    }

    protected void paintLine(final Graphics g, final int x1, final int y1, final int x3, final int y3) {
        float width = isHighlighted() ? Edge.HIGHLIGHT_WIDTH : Edge.DEFAULT_WIDTH;
        Stroke stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 6, 6 }, 0);
        Stroke old = ((Graphics2D) g).getStroke();
        ((Graphics2D) g).setStroke(stroke);
        super.paintLine(g, x1, y1, x3, y3);
        ((Graphics2D) g).setStroke(old);
    }

    public String getDescription() {
        return "<<implements>>";
    }
}
