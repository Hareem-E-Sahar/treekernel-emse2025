package org.f2o.absurdum.puck.gui.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Line2D;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.panels.ArrowPanel;
import org.f2o.absurdum.puck.gui.panels.CharHasItemPanel;
import org.f2o.absurdum.puck.gui.panels.CharHasSpellPanel;
import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.GenericRelationshipPanel;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.ItemHasItemPanel;
import org.f2o.absurdum.puck.gui.panels.PathPanel;
import org.f2o.absurdum.puck.gui.panels.RoomHasCharPanel;
import org.f2o.absurdum.puck.gui.panels.RoomHasItemPanel;
import org.f2o.absurdum.puck.gui.panels.RoomPanel;
import org.f2o.absurdum.puck.gui.panels.SpellHasEffectPanel;
import org.f2o.absurdum.puck.i18n.UIMessages;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 10:42:50
 */
public class StructuralArrow extends Arrow {

    private Node source;

    private Node destination;

    private GraphElementPanel associatedPanel;

    public GraphElementPanel getAssociatedPanel() {
        if (associatedPanel == null) {
            if (source instanceof RoomNode && destination instanceof RoomNode) associatedPanel = new PathPanel(this); else if (source instanceof RoomNode && destination instanceof ItemNode) associatedPanel = new RoomHasItemPanel(this); else if (source instanceof RoomNode && destination instanceof CharacterNode) associatedPanel = new RoomHasCharPanel(this); else if (source instanceof CharacterNode && destination instanceof ItemNode) associatedPanel = new CharHasItemPanel(this); else if (source instanceof CharacterNode && destination instanceof SpellNode) associatedPanel = new CharHasSpellPanel(this); else if (source instanceof ItemNode && destination instanceof ItemNode) associatedPanel = new ItemHasItemPanel(this); else if (source instanceof SpellNode && destination instanceof AbstractEntityNode) associatedPanel = new SpellHasEffectPanel(this); else associatedPanel = new GenericRelationshipPanel(this, source.getClass(), destination.getClass());
        }
        return associatedPanel;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public void setSource(Node n) {
        source = n;
    }

    public void setDestination(Node n) {
        destination = n;
    }

    private static final int NORTH = 0;

    private static final int SOUTH = 1;

    private static final int EAST = 2;

    private static final int WEST = 3;

    public String getMostLikelyDirection() {
        int[] pCoords = getPaintingCoords();
        double angle = calcAngle((float) pCoords[0], (float) pCoords[1], (float) pCoords[2], (float) pCoords[3]);
        while (angle > 2 * Math.PI) angle -= 2 * Math.PI;
        while (angle < 0) angle += 2 * Math.PI;
        if (angle >= 0.0 && angle <= Math.PI / 4) return UIMessages.getInstance().getMessage("dir.e");
        if (angle >= Math.PI / 4 && angle <= 3 * Math.PI / 4) return UIMessages.getInstance().getMessage("dir.s");
        if (angle >= 3 * Math.PI / 4 && angle <= 5 * Math.PI / 4) return UIMessages.getInstance().getMessage("dir.w");
        if (angle >= 5 * Math.PI / 4 && angle <= 7 * Math.PI / 4) return UIMessages.getInstance().getMessage("dir.n");
        if (angle >= 7 * Math.PI / 4 && angle <= 8 * Math.PI / 4) return UIMessages.getInstance().getMessage("dir.e"); else return UIMessages.getInstance().getMessage("dir.u");
    }

    public int[] getPaintingCoords() {
        if (destination == null) {
            System.out.println(this);
            System.out.println("Source: " + this.getSource());
            System.out.println("Destination: " + this.getDestination());
            System.out.println("Name: " + this.getName());
            System.out.println("Panel: " + this.getAssociatedPanel());
            System.out.println("Panel class: " + this.getAssociatedPanel().getClass());
        }
        double srcCenterX = source.getBounds().getCenterX();
        double srcCenterY = source.getBounds().getCenterY();
        double dstCenterX = destination.getBounds().getCenterX();
        double dstCenterY = destination.getBounds().getCenterY();
        double dstRelPosX = dstCenterX - srcCenterX;
        double dstRelPosY = dstCenterY - srcCenterY;
        int dstPosition = NORTH;
        if (dstRelPosX >= 0 && dstRelPosY >= 0) {
            if (dstRelPosX > dstRelPosY) dstPosition = EAST; else dstPosition = SOUTH;
        } else if (dstRelPosX >= 0 && dstRelPosY <= 0) {
            if (dstRelPosX > -dstRelPosY) dstPosition = EAST; else dstPosition = NORTH;
        } else if (dstRelPosX <= 0 && dstRelPosY <= 0) {
            if (-dstRelPosX > -dstRelPosY) dstPosition = WEST; else dstPosition = NORTH;
        } else if (dstRelPosX <= 0 && dstRelPosY >= 0) {
            if (-dstRelPosX > dstRelPosY) dstPosition = WEST; else dstPosition = SOUTH;
        }
        double srcX = 0.0, srcY = 0.0;
        double dstX = 0.0, dstY = 0.0;
        if (dstPosition == NORTH) {
            srcX = source.getBounds().getMinX();
            srcY = source.getBounds().getMinY();
            dstX = destination.getBounds().getMinX();
            dstY = destination.getBounds().getMaxY();
        }
        if (dstPosition == EAST) {
            srcX = source.getBounds().getMaxX();
            srcY = source.getBounds().getMinY();
            dstX = destination.getBounds().getMinX();
            dstY = destination.getBounds().getMinY();
        }
        if (dstPosition == SOUTH) {
            srcX = source.getBounds().getMaxX();
            srcY = source.getBounds().getMaxY();
            dstX = destination.getBounds().getMaxX();
            dstY = destination.getBounds().getMinY();
        }
        if (dstPosition == WEST) {
            srcX = source.getBounds().getMinX();
            srcY = source.getBounds().getMaxY();
            dstX = destination.getBounds().getMaxX();
            dstY = destination.getBounds().getMaxY();
        }
        return new int[] { (int) srcX, (int) srcY, (int) dstX, (int) dstY };
    }

    public static double calcAngle(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        double angle = 0.0d;
        if (dx == 0.0) {
            if (dy == 0.0) angle = 0.0; else if (dy > 0.0) angle = Math.PI / 2.0; else angle = Math.PI * 3.0 / 2.0;
        } else if (dy == 0.0) {
            if (dx > 0.0) angle = 0.0; else angle = Math.PI;
        } else {
            if (dx < 0.0) angle = Math.atan(dy / dx) + Math.PI; else if (dy < 0.0) angle = Math.atan(dy / dx) + (2 * Math.PI); else angle = Math.atan(dy / dx);
        }
        return angle;
    }

    public void paintLinkToDoorIfAny(Graphics g, int srcX, int srcY, int dstX, int dstY, double viewZoom, double viewXOffset, double viewYOffset) {
        if (getAssociatedPanel() instanceof PathPanel) {
            PathPanel pp = (PathPanel) getAssociatedPanel();
            ItemNode door = pp.getDoor();
            if (door != null) {
                int centerX = (srcX + dstX) / 2;
                int centerY = (srcY + dstY) / 2;
                int doorX = (int) door.getBounds().getCenterX();
                int doorY = (int) door.getBounds().getCenterY();
                int viewDoorX = (int) (((int) doorX - viewXOffset) * viewZoom);
                int viewDoorY = (int) (((int) doorY - viewYOffset) * viewZoom);
                Color oldColor = g.getColor();
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(centerX, centerY, viewDoorX, viewDoorY);
                g.setColor(oldColor);
            }
        }
    }

    public void paintToView(Graphics g, double viewZoom, double viewXOffset, double viewYOffset) {
        int[] coords = getPaintingCoords();
        coords[0] = (int) (((int) coords[0] - viewXOffset) * viewZoom);
        coords[1] = (int) (((int) coords[1] - viewYOffset) * viewZoom);
        coords[2] = (int) (((int) coords[2] - viewXOffset) * viewZoom);
        coords[3] = (int) (((int) coords[3] - viewYOffset) * viewZoom);
        paint(g, (int) coords[0], (int) coords[1], (int) coords[2], (int) coords[3]);
        paintLinkToDoorIfAny(g, (int) coords[0], (int) coords[1], (int) coords[2], (int) coords[3], viewZoom, viewXOffset, viewYOffset);
    }

    /**
	 * Draws a single arrow.
	 * Note that StructuralArrow's paint() method may do more than one call to this method, since, if cut mode is used
	 * used, the structural arrow is replaced by an arrow from the source to a placeholder and an arrow from the other
	 * placeholder to the target.
	 * @param g
	 * @param srcX
	 * @param srcY
	 * @param dstX
	 * @param dstY
	 * @param text
	 */
    private void paintSingleArrow(Graphics g, int srcX, int srcY, int dstX, int dstY, String text, Color arrowColor, Color textColor) {
        g.setColor(arrowColor);
        if (PuckConfiguration.getInstance().getProperty("showArrows").equals("true") || isHighlighted()) {
            g.drawLine((int) srcX, (int) srcY, (int) dstX, (int) dstY);
            Line2D l2d = new Line2D.Double(dstX, dstY, srcX, srcY);
            double angle = calcAngle(dstX, dstY, srcX, srcY);
            double ang1 = angle + 0.4;
            double ang2 = angle - 0.4;
            double dest1X = dstX + 10 * Math.cos(ang1);
            double dest1Y = dstY + 10 * Math.sin(ang1);
            double dest2X = dstX + 10 * Math.cos(ang2);
            double dest2Y = dstY + 10 * Math.sin(ang2);
            g.drawLine((int) dstX, (int) dstY, (int) dest1X, (int) dest1Y);
            g.drawLine((int) dstX, (int) dstY, (int) dest2X, (int) dest2Y);
        }
        if (PuckConfiguration.getInstance().getProperty("showArrowNames").equals("true") || isHighlighted()) {
            g.setColor(textColor);
            Font oldFont = g.getFont();
            Font newFont = oldFont.deriveFont(getNameFontSize());
            g.setFont(newFont);
            int swidth = g.getFontMetrics().stringWidth(getName());
            int textXCoord = (int) (srcX + dstX) / 2 + 5 - swidth / 2;
            int textYCoord = (int) (srcY + dstY) / 2 + 5;
            if (dstY > srcY) textYCoord -= 5; else textYCoord += 5;
            g.drawString(text, textXCoord, textYCoord);
            g.setFont(oldFont);
        }
    }

    private double MAX_DISTANCE_UNTIL_CUT_MODE = 300.0;

    private double DISTANCE_TO_SOURCE_CUT = 50.0;

    private double DISTANCE_TO_TARGET_CUT = 50.0;

    public void paint(Graphics g, int srcX, int srcY, int dstX, int dstY) {
        Color oldColor = g.getColor();
        Color arrowColor;
        if (isSelected()) arrowColor = new Color((float) 0.5, (float) 0.0, (float) 0.5); else arrowColor = new Color((float) 0.9, (float) 0.5, (float) 0.9);
        double arrowLength = Math.sqrt((srcX - dstX) * (srcX - dstX) + (srcY - dstY) * (srcY - dstY));
        if (isSelected() || arrowLength <= MAX_DISTANCE_UNTIL_CUT_MODE) {
            paintSingleArrow(g, srcX, srcY, dstX, dstY, getName(), arrowColor, oldColor);
        } else {
            double piece1RelLen = DISTANCE_TO_SOURCE_CUT / arrowLength;
            double piece2RelLen = DISTANCE_TO_TARGET_CUT / arrowLength;
            int piece1DstX = (int) ((double) srcX + ((double) (dstX - srcX)) * piece1RelLen);
            int piece1DstY = (int) ((double) srcY + ((double) (dstY - srcY)) * piece1RelLen);
            int piece2SrcX = (int) ((double) dstX + ((double) (srcX - dstX)) * piece2RelLen);
            int piece2SrcY = (int) ((double) dstY + ((double) (srcY - dstY)) * piece2RelLen);
            if (PuckConfiguration.getInstance().getProperty("showArrows").equals("true") || isHighlighted()) {
                g.drawOval(piece1DstX - 2, piece1DstY - 2, 5, 5);
                g.drawOval(piece2SrcX - 2, piece2SrcY - 2, 5, 5);
            }
            Font oldFont = g.getFont();
            Font newFont = oldFont.deriveFont(getNameFontSize());
            g.setColor(Color.DARK_GRAY);
            g.setFont(newFont);
            if (PuckConfiguration.getInstance().getProperty("showArrowNames").equals("true") || isHighlighted()) {
                String placeHolder1Text = "[a " + destination.getName() + "]";
                int placeHolder1StringWidth = g.getFontMetrics().stringWidth(placeHolder1Text);
                int placeHolder1NameX = piece1DstX - placeHolder1StringWidth / 2;
                int placeHolder1NameY = piece1DstY;
                if (piece1DstY > srcY + 20) placeHolder1NameY += 15; else if (piece1DstY < srcY - 20) placeHolder1NameY -= 15;
                g.drawString(placeHolder1Text, placeHolder1NameX, placeHolder1NameY);
            }
            if (PuckConfiguration.getInstance().getProperty("showArrowNames").equals("true") || isHighlighted()) {
                String placeHolder2Text = "[de " + source.getName() + "]";
                int placeHolder2StringWidth = g.getFontMetrics().stringWidth(placeHolder2Text);
                int placeHolder2NameX = piece2SrcX - placeHolder2StringWidth / 2;
                int placeHolder2NameY = piece2SrcY;
                if (piece2SrcY + 20 < dstY) placeHolder2NameY -= 15; else if (piece2SrcY - 20 > dstY) placeHolder2NameY += 15;
                g.drawString(placeHolder2Text, placeHolder2NameX, placeHolder2NameY);
                g.setFont(oldFont);
            }
            paintSingleArrow(g, srcX, srcY, piece1DstX, piece1DstY, getName(), arrowColor, oldColor);
            paintSingleArrow(g, piece2SrcX, piece2SrcY, dstX, dstY, getName(), arrowColor, oldColor);
        }
        g.setColor(oldColor);
    }

    public void paintTo(Graphics g, int x, int y) {
        double srcX = source.getBounds().getCenterX();
        double srcY = source.getBounds().getCenterY();
        paint(g, (int) srcX, (int) srcY, x, y);
    }

    public Object clone() {
        StructuralArrow sa = new StructuralArrow();
        sa.setSource(source);
        sa.setDestination(destination);
        return sa;
    }

    public String getName() {
        if (associatedPanel != null) {
            return associatedPanel.getNameForElement();
        } else return "";
    }
}
