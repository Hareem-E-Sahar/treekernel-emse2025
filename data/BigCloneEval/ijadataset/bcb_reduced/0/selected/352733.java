package belvedere.graphview;

import javax.swing.*;
import java.awt.*;
import belvedere.model.*;
import belvedere.Belvedere4;
import java.util.ResourceBundle;
import belvedere.util.ImageFetcher;

/**
 * Graphical elements to represent a Relation in a GraphView
 * 
 * @author David J. Burger, Nathan Dwyer
 */
public class GraphViewRelation implements GraphViewConstituent {

    /**
   * <Code>BelevederGraph</CODE> in which this relation
   * is embedded
   */
    private BelvedereGraph graph;

    /**
   * Model element represented by this object
   */
    private Relation relation;

    /**
   * <code>GraphViewComponent</CODE> representing the
   * origin of the <CODE>Relation</CODE>
   */
    private GraphViewConstituent origin;

    /**
   * <code>GraphViewComponent</CODE> representing the
   * destination of the <CODE>Relation</CODE>
   */
    private GraphViewConstituent destination;

    /**
   * Selected flag. Set to true if this should draw
   * itself in the selected mode
   */
    private boolean isSelected = false;

    /**
   * Hoverd flag. Set to true if this should draw
   * itself in the hovered mode
   */
    private boolean isHovered = false;

    /**
   * Initial/default strength value for the constituent
   */
    private static final int DEFAULT_STRENGTH = 3;

    /**
   * Stroke object used to draw lines
   */
    protected BasicStroke borderStroke = new BasicStroke(DEFAULT_STRENGTH);

    /**
   * Used in hit test calculations
   */
    public static int sensitivity = 3000;

    /**
   * Font size use to draw the relation indicator, that is +, -, or ?.
   */
    protected static final int FONT_SIZE = 28;

    /**
   * Monospace font to make it easier to hit the center of the circle.
   */
    protected static final Font font = new Font("Courier", Font.BOLD, FONT_SIZE);

    /**
   * Used to fetch internationalized strings.
   */
    private ResourceBundle properties = Belvedere4.getResources();

    /**
   * Associated UI component that launches a web browser
   * 
   * @see GraphViewURL
   */
    protected GraphViewURL urlBtn;

    /**
   * Diamter of circle that encloses the <code>Relation</code> indicator.
   */
    private static final int DEFAULT_CIRCLE_DIAMETER = 30;

    /**
   * Half the diameter to avoid divisions during drawing.
   */
    private static final int HALF_CIRCLE_DIAMETER = 15;

    private static final int ARROW_LENGTH = 25;

    private static final int ARROW_WIDTH = 10;

    /**
   * The width of the glyph that indicates a URL is present so that clicks
   * within the glyph can be determined.
   */
    public static final int WEB_GLYPH_OFFSET = 7;

    /**
   * Constructor. Sets up graphical representation of
   * the <CODE>Relation</CODE> and polls the relation
   * for rendering information.
   * 
   * @param graph    <CODE>BelvedereGraph</code> in which this UI component
   *                 is emebedded
   * 
   * @param relation <CODE>Model</CODE> constituent represented by this
   *                 UI component
   */
    public GraphViewRelation(BelvedereGraph graph, Relation relation) {
        super();
        this.graph = graph;
        this.relation = relation;
        this.urlBtn = new GraphViewURL(this);
        graph.add(urlBtn);
        update();
    }

    /**
   * Set the origin object for this relation. Updates
   * the model.
   * 
   * @param origin New origin component
   */
    void setOrigin(GraphViewConstituent origin) {
        this.origin = origin;
        if (this.relation.getOrigin() != origin.getConstituent()) this.relation.setOrigin(origin.getConstituent());
    }

    /**
   * Returns the origin <CODE>GraphViewComponent</CODE>
   * 
   * @return Origin for this relation
   */
    GraphViewConstituent getOrigin() {
        return this.origin;
    }

    /**
   * Set the destination object for this relation. Updates
   * the model.
   * 
   * @param destination
   *               New destination for this relation
   */
    void setDestination(GraphViewConstituent destination) {
        this.destination = destination;
        if (this.relation.getDestination() != destination.getConstituent()) this.relation.setDestination(destination.getConstituent());
    }

    /**
   * Returns the destination <CODE>GraphViewComponent</CODE>
   * 
   * @return Destination component for this relation
   */
    GraphViewConstituent getDestination() {
        return this.destination;
    }

    /**
   * Draws the relation during a normal screen update
   * 
   * @param g      Graphics context
   */
    void paint(Graphics g) {
        paint(g, this.destination.getCenterX(), this.destination.getCenterY());
    }

    /**
   * Draws the relation while it is being created
   * 
   * @param g      Craphics context
   * 
   * @param destX  Current endpoint
   * 
   * @param destY  Current endpoint
   */
    void paint(Graphics g, int destX, int destY) {
        int srcX = this.origin.getCenterX();
        int srcY = this.origin.getCenterY();
        Graphics2D g2D = (Graphics2D) g;
        Color lineColor = null;
        if (this.isSelected) lineColor = BelvedereGraph.selectColor(); else if (this.isHovered) lineColor = BelvedereGraph.hoverColor(); else lineColor = this.relation.getLineColor();
        g2D.setColor(lineColor);
        g2D.setStroke(this.borderStroke);
        g2D.drawLine(srcX, srcY, destX, destY);
        boolean isFilled = true;
        String text = this.relation.getText();
        if (text.equals("")) isFilled = false;
        String iconType = (String) this.relation.getAttribute("iconType");
        if (iconType.equals("arrow")) {
            paintArrow(g2D, srcX, srcY, destX, destY, lineColor, isFilled);
        } else if (iconType.equals("mnemonic")) {
            paintMnemonic(g2D, srcX, srcY, destX, destY, lineColor, isFilled);
        }
        urlBtn.setLocation((srcX + destX) / 2 + this.WEB_GLYPH_OFFSET, (srcY + destY) / 2 - this.WEB_GLYPH_OFFSET);
    }

    /**
   * Paints a circle at the midpoint of the relation and places
   * the mnemonic symbol inside it
   *
   * @param g           the graphics2D to paint the line to
   * @param sourceX     x coordinate of one end point of line
   * @param sourceY     y coordinate of one end point of line
   * @param destX       x coordinate of second end point of line
   * @param destY       y coordinate of second end point of line
   */
    private void paintMnemonic(Graphics2D g, int srcX, int srcY, int destX, int destY, Color color, boolean isFilled) {
        int midX = (srcX + destX) / 2;
        int midY = (srcY + destY) / 2;
        if (!isFilled) {
            g.setColor(Color.WHITE);
            g.fillOval(midX - HALF_CIRCLE_DIAMETER, midY - HALF_CIRCLE_DIAMETER, DEFAULT_CIRCLE_DIAMETER, DEFAULT_CIRCLE_DIAMETER);
            g.setColor(color);
            g.drawOval(midX - HALF_CIRCLE_DIAMETER, midY - HALF_CIRCLE_DIAMETER, DEFAULT_CIRCLE_DIAMETER, DEFAULT_CIRCLE_DIAMETER);
        } else {
            g.setColor(color);
            g.fillOval(midX - HALF_CIRCLE_DIAMETER, midY - HALF_CIRCLE_DIAMETER, DEFAULT_CIRCLE_DIAMETER, DEFAULT_CIRCLE_DIAMETER);
        }
        String mnemonic = (String) this.relation.getAttribute("mnemonic");
        if (!mnemonic.equals("")) {
            g.setColor(Color.black);
            g.setFont(this.font);
            g.drawString(mnemonic, midX - 8, midY + 9);
        }
    }

    /**
   * Paints a directed arrow from source to destination.
   * 
   * @param g       the graphics2D to paint the line to
   * @param sourceX x coordinate of one end point of line
   * @param sourceY y coordinate of one end point of line
   * @param destX   x coordinate of second end point of line
   * @param destY   y coordinate of second end point of line
   * @param color
   * @param filled
   */
    private void paintArrow(Graphics2D g, int sourceX, int sourceY, int destX, int destY, Color color, boolean filled) {
        int dX = destX - sourceX;
        int dY = destY - sourceY;
        double length = Math.sqrt(dX * dX + dY * dY);
        if (length > this.ARROW_LENGTH) {
            double pct = this.ARROW_LENGTH / length;
            int midX = sourceX + dX / 2;
            int midY = sourceY + dY / 2;
            double offX = pct * dX / 2;
            double offY = pct * dY / 2;
            sourceX = (int) (midX - offX);
            sourceY = (int) (midY - offY);
            destX = (int) (midX + offX);
            destY = (int) (midY + offY);
        }
        int arrowX[] = new int[3];
        int arrowY[] = new int[3];
        int offX = (int) (dY * (this.ARROW_WIDTH / length));
        int offY = (int) (-dX * (this.ARROW_WIDTH / length));
        arrowX[0] = destX;
        arrowY[0] = destY;
        arrowX[1] = sourceX + offX;
        arrowY[1] = sourceY + offY;
        arrowX[2] = sourceX - offX;
        arrowY[2] = sourceY - offY;
        Polygon arrow = new Polygon(arrowX, arrowY, arrowX.length);
        if (!filled) {
            g.setColor(Color.white);
            g.fill(arrow);
            g.setColor(color);
            g.draw(arrow);
        } else {
            g.setColor(color);
            g.fill(arrow);
        }
    }

    public Constituent getConstituent() {
        return this.relation;
    }

    public void setConstituent(Constituent c) {
        if (c instanceof Relation) {
            this.relation = (Relation) c;
            update();
        }
    }

    public void update() {
        this.borderStroke = new BasicStroke(DEFAULT_STRENGTH + getConstituent().getIntAttribute("Strength"));
        this.urlBtn.update();
        urlBtn.setLocation(getCenterX() + this.WEB_GLYPH_OFFSET, getCenterY() - this.WEB_GLYPH_OFFSET);
    }

    public void setSelect(boolean isSelected) {
        if (this.isSelected != isSelected) {
            this.isSelected = isSelected;
            this.graph.repaint();
        }
    }

    public void setHover(boolean isHovered) {
        if (this.isHovered != isHovered) {
            this.isHovered = isHovered;
            this.graph.repaint();
        }
    }

    public void showPropEditor(JFrame parent) {
    }

    public boolean withinBounds(Point p) {
        int srcX = this.origin.getCenterX();
        int srcY = this.origin.getCenterY();
        int destX = this.destination.getCenterX();
        int destY = this.destination.getCenterY();
        if (Math.abs((p.y - srcY) * (destX - p.x) - (p.x - srcX) * (destY - p.y)) < this.sensitivity) {
            if (srcX <= destX) {
                if (srcY <= destY) {
                    return ((p.x >= srcX && p.x <= destX) && (p.y >= srcY && p.y <= destY));
                } else {
                    return ((p.x >= srcX && p.x <= destX) && (p.y <= srcY && p.y >= destY));
                }
            } else {
                if (srcY <= destY) {
                    return ((p.x <= srcX && p.x >= destX) && (p.y >= srcY && p.y <= destY));
                } else {
                    return ((p.x <= srcX && p.x >= destX) && (p.y <= srcY && p.y >= destY));
                }
            }
        }
        return false;
    }

    public int getCenterX() {
        int oCenter = 0;
        if (this.origin != null) oCenter = this.origin.getCenterX();
        int dCenter = 0;
        if (this.destination != null) dCenter = this.destination.getCenterX();
        return (oCenter + dCenter) / 2;
    }

    public int getCenterY() {
        int oCenter = 0;
        if (this.origin != null) oCenter = this.origin.getCenterY();
        int dCenter = 0;
        if (this.destination != null) dCenter = this.destination.getCenterY();
        return (oCenter + dCenter) / 2;
    }
}
