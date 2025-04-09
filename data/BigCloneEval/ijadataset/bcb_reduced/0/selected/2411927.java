package vademecum.ui.visualizer.vgraphics.basicfeatures;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import vademecum.ui.visualizer.vgraphics.AbstractFeature;
import vademecum.ui.visualizer.vgraphics.IMouseOverPaintable;

public class FeatureMoveResize extends AbstractFeature implements IMouseOverPaintable {

    int featureID = 5;

    String featureName = "Move/Resize";

    String featureDescription = "Moving and Resizing - Mode";

    int featurePriority = 10;

    int internalMouseOverFlag = 0;

    static final int ACTIVATION_INNER_AREA = 0;

    static final int ACTIVATION_BORDER_AREA = 1;

    static final int ACTIVATION_BTL = 2;

    static final int ACTIVATION_BTM = 3;

    static final int ACTIVATION_BTR = 4;

    static final int ACTIVATION_BML = 5;

    static final int ACTIVATION_BMR = 6;

    static final int ACTIVATION_BBL = 7;

    static final int ACTIVATION_BBM = 8;

    static final int ACTIVATION_BBR = 9;

    int anchorThickness = 6;

    private Point screenPosition;

    private Point lastDragPosition;

    @Override
    public JLabel getFeatureLabel() {
        JLabel label = new JLabel("Move&Resize");
        return label;
    }

    @Override
    public JMenuItem getMenuItem() {
        JMenuItem item = new JMenuItem("Move/Resize");
        return item;
    }

    public void mouseClicked(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
        if (refBase.getParentMode() == this.getFeatureID()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            refBase.setCursor(cursor);
            if (screenPosition == null | lastDragPosition == null) {
                screenPosition = refBase.getLocation();
                lastDragPosition = refBase.getLocation();
            }
        }
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        if (refBase.getParentMode() == this.featureID) {
            Point point = e.getPoint();
            lastDragPosition.x = screenPosition.x + point.x;
            lastDragPosition.y = screenPosition.y + point.y;
        }
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mouseDragged(MouseEvent e) {
        if (refBase.getParentMode() == this.featureID) {
            System.out.println("Drag-Event : Listener des Features Move/Resize spricht an!");
            Point moveEnd = e.getPoint();
            System.out.println(moveEnd);
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
                Rectangle boundRect = refBase.getBounds();
                Point point = e.getPoint();
                int newX = screenPosition.x + point.x;
                int newY = screenPosition.y + point.y;
                int xChange = newX - lastDragPosition.x;
                int yChange = newY - lastDragPosition.y;
                lastDragPosition = new Point(newX, newY);
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_INNER_AREA) {
                    int pw = refBase.getParent().getWidth();
                    int ph = refBase.getParent().getHeight();
                    if (screenPosition.x > (-1) * point.x && screenPosition.y > (-1) * point.y && screenPosition.x < pw - point.x && screenPosition.y < ph - point.y) {
                        screenPosition.x += xChange;
                        screenPosition.y += yChange;
                        refBase.validate();
                        refBase.setLocation(screenPosition);
                    }
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BTL) {
                    screenPosition.x += xChange;
                    screenPosition.y += yChange;
                    boundRect.y = screenPosition.y;
                    boundRect.height = boundRect.height - yChange;
                    boundRect.x = screenPosition.x;
                    boundRect.width = boundRect.width - xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BTM) {
                    screenPosition.y += yChange;
                    boundRect.y = screenPosition.y;
                    boundRect.height = boundRect.height - yChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BTR) {
                    screenPosition.y += yChange;
                    boundRect.y = screenPosition.y;
                    boundRect.height = boundRect.height - yChange;
                    boundRect.width = boundRect.width + xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BML) {
                    screenPosition.x += xChange;
                    boundRect.x = screenPosition.x;
                    boundRect.width = boundRect.width - xChange;
                    refBase.setBounds(boundRect);
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BMR) {
                    boundRect.width = boundRect.width + xChange;
                    refBase.setBounds(boundRect);
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BBL) {
                    screenPosition.x += xChange;
                    boundRect.height = boundRect.height + yChange;
                    boundRect.x = screenPosition.x;
                    boundRect.width = boundRect.width - xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BBM) {
                    boundRect.height = boundRect.height + yChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BBR) {
                    boundRect.height = boundRect.height + yChange;
                    boundRect.width = boundRect.width + xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                }
                refBase.repaint();
            }
        }
    }

    public Point translateScreenToComponent(Point screenPoint) {
        Insets insets = refBase.getInsets();
        int x = (int) ((screenPoint.getX() - insets.left));
        int y = (int) ((screenPoint.getY() - insets.top));
        return new Point(x, y);
    }

    public void mouseMoved(MouseEvent e) {
        if (refBase.getParentMode() == this.featureID) {
            System.out.println("mouse moved");
            Point p = e.getPoint();
            this.setResizeMode(checkState(p));
            System.out.println("Resize mode : " + this.getResizeMode());
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_INNER_AREA) {
                cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BTL) {
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BTM) {
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BTR) {
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BML) {
                cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BMR) {
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BBL) {
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BBM) {
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == FeatureMoveResize.ACTIVATION_BBR) {
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            }
            refBase.setCursor(cursor);
        }
    }

    private int checkState(Point mousepoint) {
        Point p = translateScreenToComponent(mousepoint);
        int radius = 7;
        Rectangle r = this.getAnchorRect();
        r.setBounds(r.x + anchorThickness, r.y + anchorThickness, r.width - anchorThickness, r.height - anchorThickness);
        if (r.x < p.x && r.width > p.x && r.y < p.y && r.height > p.y) {
            return FeatureMoveResize.ACTIVATION_INNER_AREA;
        } else {
            int x = r.x;
            int y = r.y;
            int width = r.x + r.width - 6;
            int height = r.y + r.height - 6;
            int centerx = (x + width) / 2;
            int centery = (y + height) / 2;
            if (p.distance(x, y) < radius) return FeatureMoveResize.ACTIVATION_BTL;
            if (p.distance(centerx, y) < radius) return FeatureMoveResize.ACTIVATION_BTM;
            if (p.distance(width, y) < radius) return FeatureMoveResize.ACTIVATION_BTR;
            if (p.distance(x, centery) < radius) return FeatureMoveResize.ACTIVATION_BML;
            if (p.distance(width, centery) < radius) return FeatureMoveResize.ACTIVATION_BMR;
            if (p.distance(x, height) < radius) return FeatureMoveResize.ACTIVATION_BBL;
            if (p.distance(centerx, height) < radius) return FeatureMoveResize.ACTIVATION_BBM;
            if (p.distance(width, height) < radius) return FeatureMoveResize.ACTIVATION_BBR;
            return FeatureMoveResize.ACTIVATION_BORDER_AREA;
        }
    }

    private void setResizeMode(int flag) {
        this.internalMouseOverFlag = flag;
    }

    private int getResizeMode() {
        return internalMouseOverFlag;
    }

    public void propertyChange(PropertyChangeEvent arg0) {
    }

    public void drawWhenMouseOver(Graphics2D g2) {
        drawAnchorBox(g2);
    }

    private void drawAnchorBox(Graphics2D g2) {
        Rectangle r = getAnchorRect();
        int x = r.x;
        int y = r.y;
        int width = r.x + r.width - 6;
        int height = r.y + r.height - 6;
        int centerx = (x + width) / 2;
        int centery = (y + height) / 2;
        Vector<Point> selPts = new Vector<Point>();
        selPts.add(new Point(x, y));
        selPts.add(new Point(x, height));
        selPts.add(new Point(width, y));
        selPts.add(new Point(width, height));
        selPts.add(new Point(centerx, y));
        selPts.add(new Point(centerx, height));
        selPts.add(new Point(x, centery));
        selPts.add(new Point(width, centery));
        g2.setColor(Color.green);
        for (Point kPkt : selPts) {
            g2.fillRect(kPkt.x, kPkt.y, 6, 6);
        }
    }

    private Rectangle getAnchorRect() {
        Rectangle ref = refBase.getBounds();
        Rectangle r = new Rectangle(0, 0, ref.width, ref.height);
        return r;
    }

    private void drawCustomBorder(Graphics2D g2) {
        if (!refBase.isActive()) {
            Color c = Color.WHITE;
            refBase.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, c));
        } else {
        }
    }

    @Override
    public int getFeatureID() {
        return this.featureID;
    }

    @Override
    public String getName() {
        return this.featureName;
    }

    @Override
    public int getPriority() {
        return this.featurePriority;
    }

    @Override
    public String getFeatureType() {
        return "General";
    }

    /**
	 * (non-Javadoc)
	 * @see vademecum.ui.visualizer.vgraphics.AbstractFeature#initPopupMenu()
	 */
    @Override
    public void initPopupMenu() {
    }

    /**
	 * (non-Javadoc)
	 * @see vademecum.ui.visualizer.vgraphics.AbstractFeature#showPopupMenu()
	 */
    @Override
    public void showPopupMenu(int x, int y) {
    }
}
