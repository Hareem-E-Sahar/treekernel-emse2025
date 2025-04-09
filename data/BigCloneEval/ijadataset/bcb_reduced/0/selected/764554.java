package scaffoldhunter.vis.view;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.PText;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.LinkedList;

/**
 * This class visualizes a circular sector with a label by extending
 * the Piccolo scenegraph element class PNode.
 * The arc is defined by the position of two nodes and the fixed
 * center. 
 * 
 * @author Kriege
 */
public class CircularSector extends PNode {

    private Arc2D.Double arc;

    private VNode firstNode;

    private VNode lastNode;

    private PText label;

    private VRadialLayout layout;

    /**
	 * Creates a new circular sector
	 * @param firstNode the first node (in the clockwise oder of siblings) that
	 * lies in this sector
	 * @param lastNode the last node in this sector
	 * @param label the text label of the sector
	 * @param color	the color of the sector
	 * @param layout
	 */
    public CircularSector(VNode firstNode, VNode lastNode, String label, Color color, VRadialLayout layout) {
        this.layout = layout;
        this.firstNode = firstNode;
        this.lastNode = lastNode;
        this.arc = new Arc2D.Double();
        arc.setArcType(Arc2D.PIE);
        if (!label.equals("")) {
            this.label = new PText(label);
            this.addChild(this.label);
        }
        updateArc();
        setPaint(color);
    }

    /**
	 * Updates the size of the sector according to the position of the nodes
	 */
    public void updateArc() {
        double r = layout.getOuterRadius() + layout.getRadiusAdd() * 1 / 2;
        arc.setFrame(-r, -r, 2 * r, 2 * r);
        double start = 2 * Math.PI - layout.getSector(lastNode).getEndAngle();
        double end = 2 * Math.PI - layout.getSector(firstNode).getStartAngle();
        arc.setAngleStart(Math.toDegrees(start));
        arc.setAngleExtent(Math.toDegrees(end - start));
        setBounds(arc.getBounds2D());
        double ang = (start + end) / 2;
        double x = Math.cos(ang) * layout.getInnerRadius() * 0.75;
        double y = -Math.sin(ang) * layout.getInnerRadius() * 0.75;
        if (label != null) {
            label.centerFullBoundsOnPoint(x, y);
            label.setScale(6);
        }
        invalidatePaint();
    }

    public void colorNodesBackground() {
        LinkedList<VNode> q = new LinkedList<VNode>();
        VNode v = firstNode;
        do {
            q.offer(v);
            v = v.getClockwiseSibling();
        } while (v.getAnticlockwiseSibling() != lastNode);
        while (!q.isEmpty()) {
            v = q.poll();
            v.setPaint(getPaint());
            q.addAll(v.getTreeChildren());
        }
    }

    public void paint(PPaintContext aPaintContext) {
        Graphics2D g = aPaintContext.getGraphics();
        g.setPaint(getPaint());
        g.fill(arc);
        g.setPaint(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.draw(arc);
    }
}
