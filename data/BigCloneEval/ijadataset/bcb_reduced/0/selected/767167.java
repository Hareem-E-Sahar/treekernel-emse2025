package tralesld.visual.tree;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class TreeViewPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public TreeView t;

    public boolean edgyLines;

    FontMetrics fm;

    TreeViewMouseListener mouseListener = null;

    private boolean movableNodes;

    private boolean antialiasing;

    private boolean visibleEdges;

    private int nodePositioning;

    public static final int CENTER_NODES = 0;

    public static final int LEFT_ALIGNMENT = 1;

    public List<TreeViewExtension> viewExtensionsBeforeMainRendering;

    public List<TreeViewExtension> viewExtensionsAfterMainRendering;

    public TreeViewPanel() {
        t = null;
        edgyLines = false;
        movableNodes = false;
        antialiasing = true;
        visibleEdges = true;
        nodePositioning = TreeViewPanel.CENTER_NODES;
        viewExtensionsBeforeMainRendering = new ArrayList<TreeViewExtension>();
        viewExtensionsAfterMainRendering = new ArrayList<TreeViewExtension>();
    }

    public void setMouseListener(TreeViewMouseListener mouseListener) {
        this.mouseListener = mouseListener;
        this.addMouseListener(mouseListener);
    }

    public void setMovableNodes(boolean movableNodes) {
        this.movableNodes = movableNodes;
        if (movableNodes) {
            this.mouseListener = new TreeViewMouseMoveListener(this);
            this.addMouseListener(mouseListener);
            this.addMouseMotionListener((TreeViewMouseMoveListener) mouseListener);
        } else {
            this.removeMouseListener(mouseListener);
            this.removeMouseMotionListener((TreeViewMouseMoveListener) mouseListener);
            this.mouseListener = null;
        }
    }

    public boolean isVisibleEdges() {
        return visibleEdges;
    }

    public void setVisibleEdges(boolean visibleEdges) {
        this.visibleEdges = visibleEdges;
    }

    public int getNodePositioning() {
        return nodePositioning;
    }

    public void setNodePositioning(int nodePositioning) {
        this.nodePositioning = nodePositioning;
    }

    public boolean isMovableNodes() {
        return movableNodes;
    }

    public void setAntialiasing(boolean anti) {
        this.antialiasing = anti;
    }

    public void paint(Graphics cnv) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted!");
        }
        Graphics2D canvas = (Graphics2D) cnv;
        if (antialiasing) {
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        int fontSize = t.getFontSize();
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        canvas.setFont(font);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (t != null) {
            clearCanvas(canvas);
            for (TreeViewExtension ext : viewExtensionsBeforeMainRendering) {
                ext.paintOnTreePanel(this, canvas);
            }
            canvas.setStroke(new BasicStroke(1));
            printTreeNodes(canvas);
            if (visibleEdges) {
                printTreeEdges(canvas);
            }
            for (TreeViewExtension ext : viewExtensionsAfterMainRendering) {
                ext.paintOnTreePanel(this, canvas);
            }
        }
    }

    public void clearCanvas(Graphics canvas) {
        Dimension newD = new Dimension(t.getTotalTreeWidth() + 200, t.getTotalTreeHeight() + 100);
        this.setSize(newD);
        this.setMinimumSize(newD);
        this.setMaximumSize(newD);
        this.setPreferredSize(newD);
        this.setBackground(new Color(220, 220, 220));
        canvas.setColor(new Color(220, 220, 220));
        canvas.fillRect(0, 0, 2000, 2000);
        canvas.fillRect(0, 0, this.getSize().width, this.getSize().height);
    }

    public void printBoxAroundNodeTag(Graphics canvas, int nodeID) {
        if (t.markedNodes.contains(nodeID)) {
            canvas.setFont(new Font(canvas.getFont().getFamily(), Font.BOLD, 12));
        } else {
            canvas.setFont(new Font(canvas.getFont().getFamily(), Font.PLAIN, 12));
        }
        FontMetrics fm = canvas.getFontMetrics();
        int width = fm.stringWidth(t.treeNodes.get(nodeID).tag);
        int x = t.treeNodes.get(nodeID).x - width / 2;
        int y = t.treeNodes.get(nodeID).y - 10;
        if (nodePositioning == LEFT_ALIGNMENT) {
            x += width / 2;
        }
        x += t.getIndent(nodeID);
        Color color = t.getNodeColor(nodeID);
        if (color != null) {
            canvas.setColor(color);
            canvas.fillRect(x - 2, y, width + 4, t.getFontSize());
            canvas.setColor(Color.BLACK);
            canvas.drawRect(x - 2, y, width + 4, t.getFontSize());
        }
    }

    public void printEdgeArrow(Graphics canvas, int nodeID) {
        TreeViewNode node = t.treeNodes.get(nodeID);
        int x1 = t.treeNodes.get(node.getParent()).x;
        int y1 = t.treeNodes.get(node.getParent()).y + 2;
        int x2 = node.x;
        int y2 = node.y - 10;
        String edgeDir = node.edgeDir;
        if (edgeDir.equals("up")) {
            double slope = ((y1 - y2 + 0.1) / (x1 - x2 + 0.1));
            double lowerSlope = (slope - 1) / (1 + slope);
            double higherSlope = -(1 / lowerSlope);
            Polygon arrowhead = new Polygon();
            arrowhead.addPoint(x1, y1);
            if ((x2 <= x1 && slope < -1) || (x2 >= x1 && slope > -1)) arrowhead.addPoint(x1 + (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)), y1 + (int) ((10 / Math.sqrt(1 + lowerSlope * lowerSlope)) * lowerSlope));
            if ((x2 <= x1 && slope > -1) || (x2 > x1 && slope < -1)) arrowhead.addPoint(x1 - (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)), y1 - (int) ((10 / Math.sqrt(1 + lowerSlope * lowerSlope)) * lowerSlope));
            if ((x2 <= x1 && slope > 1) || (x2 > x1 && slope < 1)) arrowhead.addPoint(x1 + (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)), y1 + (int) ((10 / Math.sqrt(1 + higherSlope * higherSlope)) * higherSlope));
            if ((x2 <= x1 && slope < 1) || (x2 >= x1 && slope > 1)) arrowhead.addPoint(x1 - (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)), y1 - (int) ((10 / Math.sqrt(1 + higherSlope * higherSlope)) * higherSlope));
            canvas.fillPolygon(arrowhead);
        } else if (edgeDir.equals("down")) {
            double slope = ((y2 - y1 - 0.1) / (x2 - x1 + 0.1));
            double lowerSlope = (slope - 1) / (1 + slope);
            double higherSlope = -(1 / lowerSlope);
            Polygon arrowhead = new Polygon();
            arrowhead.addPoint(x2, y2);
            if ((x2 < x1 && slope < -1) || (x2 >= x1 && slope > -1)) arrowhead.addPoint(x2 - (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)), y2 - (int) ((10 / Math.sqrt(1 + lowerSlope * lowerSlope)) * lowerSlope));
            if ((x2 < x1 && slope > -1) || (x2 > x1 && slope < -1)) arrowhead.addPoint(x2 + (int) (10 / Math.sqrt(1 + lowerSlope * lowerSlope)), y2 + (int) ((10 / Math.sqrt(1 + lowerSlope * lowerSlope)) * lowerSlope));
            if ((x2 < x1 && slope > 1) || (x2 > x1 && slope < 1)) arrowhead.addPoint(x2 - (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)), y2 - (int) ((10 / Math.sqrt(1 + higherSlope * higherSlope)) * higherSlope));
            if ((x2 < x1 && slope < 1) || (x2 >= x1 && slope > 1)) arrowhead.addPoint(x2 + (int) (10 / Math.sqrt(1 + higherSlope * higherSlope)), y2 + (int) ((10 / Math.sqrt(1 + higherSlope * higherSlope)) * higherSlope));
            canvas.fillPolygon(arrowhead);
        }
    }

    public void printEdgeTag(Graphics canvas, int nodeID, boolean edgyLines) {
        TreeViewNode node = t.treeNodes.get(nodeID);
        FontMetrics fm = canvas.getFontMetrics();
        int width = fm.stringWidth(node.tag) / 2;
        if (node.edgeTag.length() > 0) {
            int x1 = t.treeNodes.get(node.getParent()).x;
            int y1 = t.treeNodes.get(node.getParent()).y + 2;
            int x2 = node.x;
            int y2 = node.y - 10;
            int y = (y1 + y2) / 2 - 10;
            int x = x2;
            if (!edgyLines) {
                x = (x1 + x2) / 2;
            }
            Color color = node.edgeTagColor;
            if (color == null) {
                canvas.setColor(Color.WHITE);
            } else {
                canvas.setColor(color);
            }
            canvas.fillRect(x - width, y, 2 * width, 12);
            canvas.setColor(Color.BLACK);
            canvas.drawRect(x - width, y, 2 * width, 12);
            canvas.drawString(node.edgeTag, x - width / 2, (y1 + y2) / 2);
        }
    }

    public void printNodeTag(Graphics2D canvas, int nodeID) {
        TreeViewNode node = t.treeNodes.get(nodeID);
        fm = canvas.getFontMetrics();
        int width = fm.stringWidth(node.tag) / 2;
        canvas.setColor(Color.BLACK);
        int x = (int) (t.treeNodes.get(nodeID).x - width);
        if (nodePositioning == LEFT_ALIGNMENT) {
            x += width;
        }
        x += t.getIndent(nodeID);
        int y = t.treeNodes.get(nodeID).y;
        String tag = t.treeNodes.get(nodeID).tag;
        if (t.markedNodes.contains(nodeID)) {
            canvas.setStroke(new BasicStroke(2));
            canvas.setFont(new Font(canvas.getFont().getFontName(), Font.BOLD, 12));
        }
        canvas.drawString(tag, x + 2, y);
        canvas.setStroke(new BasicStroke(1));
        canvas.setFont(new Font(canvas.getFont().getFontName(), Font.PLAIN, 12));
    }

    public void printOvalAroundNodeTag(Graphics canvas, int nodeID) {
        int x = t.treeNodes.get(nodeID).x - (int) (t.treeNodes.get(nodeID).tag.length() * 8 * t.getZoomFactor());
        int y = t.treeNodes.get(nodeID).y - 10;
        int width = (int) (t.treeNodes.get(nodeID).tag.length() * 16 * t.getZoomFactor());
        Color color = t.getNodeColor(nodeID);
        if (color != null) {
            canvas.setColor(color);
            canvas.fillOval(x, y, width, t.getFontSize());
            canvas.setColor(Color.BLACK);
            canvas.drawOval(x, y, width, t.getFontSize());
        }
    }

    public void printTreeEdges(Graphics canvas) {
        canvas.setColor(Color.BLACK);
        for (int i = 0; i < t.getNodeLevels().size(); i++) {
            ArrayList<Integer> nodes = t.getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (t.treeNodes.get(nodes.get(j)).getParent() != -1) {
                    int x1 = t.treeNodes.get(t.treeNodes.get(nodes.get(j)).getParent()).x;
                    int y1 = t.treeNodes.get(t.treeNodes.get(nodes.get(j)).getParent()).y + 2;
                    int x2 = t.treeNodes.get(nodes.get(j)).x;
                    int y2 = t.treeNodes.get(nodes.get(j)).y - 10;
                    if (edgyLines) {
                        drawLineAccordingToType(canvas, t.treeNodes.get(nodes.get(j)).edgeType, x1, y1, x2, y1);
                        drawLineAccordingToType(canvas, t.treeNodes.get(nodes.get(j)).edgeType, x2, y1, x2, y2);
                    } else {
                        drawLineAccordingToType(canvas, t.treeNodes.get(nodes.get(j)).edgeType, x1, y1, x2, y2);
                    }
                    printEdgeTag(canvas, nodes.get(j), edgyLines);
                    printEdgeArrow(canvas, nodes.get(j));
                }
            }
        }
    }

    public void printTreeNodes(Graphics2D cnv) {
        for (int i = 0; i < t.getNodeLevels().size(); i++) {
            ArrayList<Integer> nodes = t.getNodeLevels().get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (t.nodeShape == TreeView.BOX_SHAPE) {
                    printBoxAroundNodeTag(cnv, nodes.get(j));
                } else if (t.nodeShape == TreeView.OVAL_SHAPE) {
                    printOvalAroundNodeTag(cnv, nodes.get(j));
                }
                printNodeTag(cnv, nodes.get(j));
            }
        }
    }

    public void toggleEdgyLines() {
        if (edgyLines) {
            edgyLines = false;
        } else {
            edgyLines = true;
        }
    }

    public void displayTreeView(TreeView t) {
        this.t = t;
        repaint();
    }

    public static void drawLineAccordingToType(Graphics g, String type, int x0, int y0, int x1, int y1) {
        if (type.equals("dotted")) {
            drawDottedLine(g, x0, y0, x1, y1, g.getColor(), 1, 1);
        } else {
            g.drawLine(x0, y0, x1, y1);
        }
    }

    public Rectangle getNodeCoordinates(int nodeID) {
        int width = t.treeNodes.get(nodeID).tag.length() * 6;
        int x = t.treeNodes.get(nodeID).x - width / 2;
        int y = t.treeNodes.get(nodeID).y - 10;
        if (nodePositioning == LEFT_ALIGNMENT) {
            x += width / 2;
        }
        x += t.getIndent(nodeID);
        return new Rectangle(x - 2, y, width + 4, t.getFontSize());
    }

    public static void drawDottedLine(Graphics g, int x0, int y0, int x1, int y1, Color color, int dashLen, int spaceLen) {
        Color c = g.getColor();
        g.setColor(color);
        int dx = x1 - x0;
        int dy = y1 - y0;
        float t = 0.5f;
        g.setColor(color);
        g.drawLine(x0, y0, x0, y0);
        int dashCount = 0;
        int spaceCount = 0;
        boolean doPlot = dashLen > 1;
        if (Math.abs(dx) > Math.abs(dy)) {
            float m = (float) dy / (float) dx;
            t += y0;
            dx = (dx < 0) ? -1 : 1;
            m *= dx;
            while (x0 != x1) {
                x0 += dx;
                t += m;
                if (doPlot) {
                    g.drawLine(x0, (int) t, x0, (int) t);
                    dashCount++;
                    if (dashCount >= dashLen) {
                        dashCount = 0;
                        spaceCount = 0;
                        doPlot = false;
                    }
                } else {
                    spaceCount++;
                    if (spaceCount >= spaceLen) {
                        spaceCount = 0;
                        dashCount = 0;
                        doPlot = true;
                    }
                }
            }
        } else if (dy != 0) {
            float m = (float) dx / (float) dy;
            t += x0;
            dy = (dy < 0) ? -1 : 1;
            m *= dy;
            while (y0 != y1) {
                y0 += dy;
                t += m;
                if (doPlot) {
                    g.drawLine((int) t, y0, (int) t, y0);
                    dashCount++;
                    if (dashCount >= dashLen) {
                        dashCount = 0;
                        spaceCount = 0;
                        doPlot = false;
                    }
                } else {
                    spaceCount++;
                    if (spaceCount >= spaceLen) {
                        spaceCount = 0;
                        dashCount = 0;
                        doPlot = true;
                    }
                }
            }
        }
        g.setColor(c);
    }
}
