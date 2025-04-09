package vademecum.visualizer.d3.scatter.interactions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import vademecum.ui.visualizer.vgraphics.AbstractInteraction;
import vademecum.ui.visualizer.vgraphics.IMouseOverDrawable;
import vademecum.visualizer.d3.scatter.ScatterPlot3D;

public class Feature3DArrangement extends AbstractInteraction implements IMouseOverDrawable {

    /** Logger */
    private static Log log = LogFactory.getLog(Feature3DArrangement.class);

    boolean snapToGrid = false;

    int triggerID = 5;

    String interactionName = "Arrangement";

    String interactionDescription = "Moving and Resizing - Mode";

    int interactionPriority = 10;

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

    int gridSpacing = 20;

    JPopupMenu pMenu;

    @Override
    public JLabel getInteractionLabel() {
        JLabel label = new JLabel("Arrangement");
        return label;
    }

    @Override
    public JMenuItem getMenuItem() {
        JMenuItem item = new JMenuItem("Move/Resize");
        return item;
    }

    private Point calcSnapPosition(Point p) {
        int xGrid = (int) (Math.floor(p.x / this.gridSpacing)) * this.gridSpacing;
        int yGrid = (int) (Math.floor(p.y / this.gridSpacing)) * this.gridSpacing;
        return new Point(xGrid, yGrid);
    }

    public void mouseClicked(MouseEvent e) {
        if (refBase.getInteractionMode() == this.triggerID) {
            if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
                this.showPopupMenu(e.getX() - 20, e.getY() - 10);
            }
        }
    }

    public void mouseEntered(MouseEvent arg0) {
        if (refBase.getInteractionMode() == this.getTriggerID()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            refBase.setCursor(cursor);
            if (screenPosition == null || lastDragPosition == null) {
                screenPosition = refBase.getLocation();
                lastDragPosition = refBase.getLocation();
            }
        }
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        if (refBase.getInteractionMode() == this.triggerID) {
            Point point = e.getPoint();
            if (snapToGrid == true) {
                point = this.calcSnapPosition(point);
            }
            screenPosition = refBase.getLocation();
            lastDragPosition.x = screenPosition.x + point.x;
            lastDragPosition.y = screenPosition.y + point.y;
        }
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mouseDragged(MouseEvent e) {
        if (refBase.getInteractionMode() == this.triggerID) {
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
                Rectangle boundRect = refBase.getBounds();
                Point point = e.getPoint();
                if (snapToGrid == true) {
                    point = this.calcSnapPosition(point);
                }
                int newX = screenPosition.x + point.x;
                int newY = screenPosition.y + point.y;
                int xChange = newX - lastDragPosition.x;
                int yChange = newY - lastDragPosition.y;
                lastDragPosition = new Point(newX, newY);
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_INNER_AREA) {
                    int pw = refBase.getParent().getWidth();
                    int ph = refBase.getParent().getHeight();
                    if (screenPosition.x > (-1) * point.x && screenPosition.y > (-1) * point.y && screenPosition.x < pw - point.x && screenPosition.y < ph - point.y) {
                        screenPosition.x += xChange;
                        screenPosition.y += yChange;
                        if (snapToGrid == true) {
                            screenPosition = this.calcSnapPosition(screenPosition);
                        }
                        log.debug("Setting new Location on Screen " + screenPosition);
                        refBase.validate();
                        refBase.setLocation(screenPosition);
                    }
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BTL) {
                    screenPosition.x += xChange;
                    screenPosition.y += yChange;
                    boundRect.y = screenPosition.y;
                    boundRect.height = boundRect.height - yChange;
                    boundRect.x = screenPosition.x;
                    boundRect.width = boundRect.width - xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BTM) {
                    screenPosition.y += yChange;
                    boundRect.y = screenPosition.y;
                    boundRect.height = boundRect.height - yChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BTR) {
                    screenPosition.y += yChange;
                    boundRect.y = screenPosition.y;
                    boundRect.height = boundRect.height - yChange;
                    boundRect.width = boundRect.width + xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BML) {
                    screenPosition.x += xChange;
                    boundRect.x = screenPosition.x;
                    boundRect.width = boundRect.width - xChange;
                    refBase.setBounds(boundRect);
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BMR) {
                    boundRect.width = boundRect.width + xChange;
                    refBase.setBounds(boundRect);
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BBL) {
                    screenPosition.x += xChange;
                    boundRect.height = boundRect.height + yChange;
                    boundRect.x = screenPosition.x;
                    boundRect.width = boundRect.width - xChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BBM) {
                    boundRect.height = boundRect.height + yChange;
                    refBase.setBounds(boundRect);
                    refBase.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                }
                if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BBR) {
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
        if (refBase.getInteractionMode() == this.triggerID) {
            log.debug("mouse moved");
            Point p = e.getPoint();
            this.setResizeMode(checkState(p));
            log.debug("Resize mode : " + this.getResizeMode());
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_INNER_AREA) {
                cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BTL) {
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BTM) {
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BTR) {
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BML) {
                cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BMR) {
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BBL) {
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BBM) {
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            }
            if (this.getResizeMode() == Feature3DArrangement.ACTIVATION_BBR) {
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
            return Feature3DArrangement.ACTIVATION_INNER_AREA;
        } else {
            int x = r.x;
            int y = r.y;
            int width = r.x + r.width - 6;
            int height = r.y + r.height - 6;
            int centerx = (x + width) / 2;
            int centery = (y + height) / 2;
            if (p.distance(x, y) < radius) return Feature3DArrangement.ACTIVATION_BTL;
            if (p.distance(centerx, y) < radius) return Feature3DArrangement.ACTIVATION_BTM;
            if (p.distance(width, y) < radius) return Feature3DArrangement.ACTIVATION_BTR;
            if (p.distance(x, centery) < radius) return Feature3DArrangement.ACTIVATION_BML;
            if (p.distance(width, centery) < radius) return Feature3DArrangement.ACTIVATION_BMR;
            if (p.distance(x, height) < radius) return Feature3DArrangement.ACTIVATION_BBL;
            if (p.distance(centerx, height) < radius) return Feature3DArrangement.ACTIVATION_BBM;
            if (p.distance(width, height) < radius) return Feature3DArrangement.ACTIVATION_BBR;
            return Feature3DArrangement.ACTIVATION_BORDER_AREA;
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

    @Override
    public int getTriggerID() {
        return this.triggerID;
    }

    @Override
    public String getName() {
        return this.interactionName;
    }

    @Override
    public int getPriority() {
        return this.interactionPriority;
    }

    @Override
    public String getInteractionType() {
        return "General";
    }

    /**
	 * Turn on/off 'snap to grid' (per default : off)
	 * @param b
	 */
    public void setSnapToGrid(boolean b) {
        this.snapToGrid = b;
    }

    /**
	 * Check whether snap to grid is on or off 
	 * @return
	 */
    public boolean getSnapToGrid() {
        return this.snapToGrid;
    }

    /**
	 * (non-Javadoc)
	 * @see vademecum.ui.visualizer.vgraphics.AbstractInteraction#initPopupMenu()
	 */
    @Override
    public void initPopupMenu() {
        pMenu = new JPopupMenu("Arrangement");
        JCheckBoxMenuItem cbm = new JCheckBoxMenuItem("Snap to Grid");
        cbm.setSelected(getSnapToGrid());
        cbm.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    JCheckBoxMenuItem cbm = (JCheckBoxMenuItem) e.getSource();
                    boolean selected = cbm.isSelected();
                    setSnapToGrid(selected);
                }
            }
        });
        pMenu.add(cbm);
        JMenuItem expand = new JMenuItem("Maximize");
        expand.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                int w = refBase.getParent().getBounds().width;
                int h = refBase.getParent().getBounds().height - 5;
                refBase.setBounds(0, 0, w, h);
                ((ScatterPlot3D) refBase).updateCanvasSize();
            }
        });
        pMenu.add(expand);
        JMenu backGMenu = new JMenu("BackGround");
        JMenuItem bItem = new JMenuItem("Invisible");
        bItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                refBase.setBackgroundVisible(!refBase.isBackgroundVisible());
                refBase.repaint();
            }
        });
        backGMenu.add(bItem);
        bItem = new JMenuItem("Color");
        bItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                Color c = JColorChooser.showDialog(null, "Select Background Color", Color.WHITE);
                refBase.setBackgroundColor(c);
                refBase.repaint();
            }
        });
        backGMenu.add(bItem);
        pMenu.add(backGMenu);
        JMenu subMenu = new JMenu("Border");
        JMenuItem eItem = new JMenuItem("Invisible");
        eItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                refBase.setBorderVisible(!refBase.isBorderVisible());
                refBase.repaint();
            }
        });
        subMenu.add(eItem);
        eItem = new JMenuItem("Color");
        eItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                Color c = JColorChooser.showDialog(null, "Select Background Color", Color.WHITE);
                refBase.setBorderColor(c);
                refBase.repaint();
            }
        });
        subMenu.add(eItem);
        eItem = new JMenuItem("Square");
        eItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                refBase.setBorderStyle(0);
                refBase.setBorderVisible(true);
                refBase.repaint();
            }
        });
        subMenu.add(eItem);
        eItem = new JMenuItem("Round");
        eItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                refBase.setBorderStyle(1);
                refBase.setBorderVisible(true);
                refBase.repaint();
            }
        });
        subMenu.add(eItem);
        pMenu.add(subMenu);
    }

    /**
	 * (non-Javadoc)
	 * @see vademecum.ui.visualizer.vgraphics.AbstractInteraction#showPopupMenu()
	 */
    @Override
    public void showPopupMenu(int x, int y) {
        pMenu.show(refBase, x, y);
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void setProperties(Properties p) {
    }
}
