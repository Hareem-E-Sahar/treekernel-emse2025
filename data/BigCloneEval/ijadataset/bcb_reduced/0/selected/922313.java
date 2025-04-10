package topiaryexplorer;

import java.awt.Graphics2D;
import java.awt.BasicStroke;
import javax.swing.*;
import processing.core.*;
import processing.pdf.*;
import processing.opengl.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import javax.jnlp.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.Polygon;

public class TreeVis extends PApplet {

    private double MARGIN = 5;

    private float TREEMARGIN = 5;

    private final int SELECTED_COLOR = 0xff66CCFF;

    private final int HIGHLIGHTED_COLOR = 0xffFF0000;

    private final int HOVER_COLOR = 0xffFFFF00;

    private Color backgroundColor = new Color(255, 255, 255);

    private double xscale;

    private double yscale;

    private double treerotation = 0;

    private float lineWidthScale = 1;

    private Node root;

    private double xstart = MARGIN;

    private double ystart = MARGIN;

    private double selectXStart = 0;

    private double selectYStart = 0;

    private double selectXEnd = 0;

    private double selectYEnd = 0;

    private double oldwidth = 0;

    private double oldheight = 0;

    private double rootdepth = 0;

    private String treeLayout = "Rectangular";

    private boolean drawExternalNodeLabels = false;

    private boolean drawInternalNodeLabels = false;

    private boolean draggingLabel = false;

    private float collapsedPixel = 10000000;

    private double collapsedLevel = 0;

    private boolean zoomDrawNodeLabels = false;

    private boolean majorityColoring = true;

    private boolean mirrored = false;

    private boolean colorBranches = false;

    private boolean selectMode = false;

    private boolean selectingMode = false;

    private Polygon selectPoly = new Polygon();

    private Node selectedNode;

    private Node mouseOverNode;

    private Node mouseOverNodeToReplace;

    private Set hilightedNodes = new java.util.HashSet();

    private List listeners = new java.util.ArrayList();

    private float wedgeFontSize = 12;

    private int wedgeFontColor = 255;

    private String wFont = "SansSerif";

    private PFont wedgeFont = createFont(wFont, (int) wedgeFontSize);

    private float nodeFontSize = 10;

    private int nodeFontColor = 0;

    private String nFont = "SansSerif";

    private PFont tipFont = createFont("SansSerif", 10);

    private TreeWindow frame = null;

    private int labelXOffset = 0;

    private int labelYOffset = 0;

    private double wedgeHeightScale = 1;

    private boolean drawWedgeLabels = true;

    private boolean drawNodeLabels = false;

    private PFont nodeFont = createFont(nFont, (int) nodeFontSize);

    private int pieChartRadius = 15;

    private String collapseMode = "Root";

    /**
     * setup() is called once to initialize the applet
     */
    public void setup() {
        size(800, 600);
        smooth();
        textFont(nodeFont);
        oldwidth = width;
        oldheight = height;
    }

    /**
     * draw() is called whenever the tree needs to be re-drawn.
     * @see #redraw()
     */
    public void draw() {
        background(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
        if (oldwidth != width || oldheight != height) {
            oldwidth = width;
            oldheight = height;
            fireStateChanged();
        }
        try {
            g.pushMatrix();
            g.translate((float) xstart, (float) ystart);
            g.rotate((float) (treerotation * Math.PI / 180.0));
            g.translate((float) -xstart, (float) -ystart);
            drawTree(root);
            g.popMatrix();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParent(TreeWindow f) {
        frame = f;
    }

    public double getMargin() {
        return MARGIN;
    }

    public double getTreeMargin() {
        return TREEMARGIN;
    }

    public double getYScale() {
        return yscale;
    }

    public double getXScale() {
        return xscale;
    }

    public double getYStart() {
        return ystart;
    }

    public double getXStart() {
        return xstart;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color c) {
        backgroundColor = c;
    }

    public String getTreeLayout() {
        return treeLayout;
    }

    public Node getTree() {
        return root;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(Node node) {
        selectedNode = node;
    }

    public Set getHilightedNodes() {
        return hilightedNodes;
    }

    public boolean getDrawExternalNodeLabels() {
        return drawExternalNodeLabels;
    }

    public boolean getDrawInternalNodeLabels() {
        return drawInternalNodeLabels;
    }

    public void setDrawExternalNodeLabels(boolean b) {
        drawExternalNodeLabels = b;
        checkBounds();
    }

    public void setDrawInternalNodeLabels(boolean b) {
        drawInternalNodeLabels = b;
    }

    public void setCollapsedPixel(float pixel) {
        collapsedPixel = pixel;
    }

    public float getCollapsedPixel() {
        return collapsedPixel;
    }

    public void setCollapsedLevel(double level) {
        collapsedLevel = level;
    }

    public double getCollapsedLevel() {
        return collapsedLevel;
    }

    public float getLineWidthScale() {
        return lineWidthScale;
    }

    public void setLineWidthScale(float f) {
        lineWidthScale = f;
    }

    public void setMajorityColoring(boolean cond) {
        majorityColoring = cond;
    }

    public boolean getMajorityColoring() {
        return majorityColoring;
    }

    public void setLabelXOffset(int i) {
        labelXOffset = i;
        redraw();
    }

    public int getLabelXOffset() {
        return labelXOffset;
    }

    public void setLabelYOffset(int i) {
        labelYOffset = i;
        redraw();
    }

    public int getLabelYOffset() {
        return labelYOffset;
    }

    public void setWedgeHeight(double d) {
        wedgeHeightScale = d;
    }

    public double getWedgeHeight() {
        return wedgeHeightScale;
    }

    public void setWedgeFontFace(String s) {
        wFont = s;
        wedgeFont = createFont(wFont, (int) wedgeFontSize);
    }

    public void setWedgeFontSize(float d) {
        if (d > 0) {
            wedgeFontSize = d;
            wedgeFont = createFont(wFont, (int) wedgeFontSize);
            redraw();
        }
    }

    public float getWedgeFontSize() {
        return wedgeFontSize;
    }

    public void setWedgeFontColor(int c) {
        wedgeFontColor = c;
        redraw();
    }

    public boolean getMirrored() {
        return mirrored;
    }

    public void setMirrored(boolean b) {
        mirrored = b;
        redraw();
    }

    public void setDrawWedgeLabels(boolean b) {
        drawWedgeLabels = b;
        redraw();
    }

    public void setDrawNodeLabels(boolean b) {
        drawNodeLabels = b;
        redraw();
    }

    public void setNodeFontFace(String s) {
        nFont = s;
        nodeFont = createFont(nFont, (int) nodeFontSize);
    }

    public void setNodeFontSize(float d) {
        if (d > 0) {
            nodeFontSize = d;
            nodeFont = createFont(nFont, (int) nodeFontSize);
            redraw();
        }
    }

    public float getNodeFontSize() {
        return nodeFontSize;
    }

    public void setNodeFontColor(int c) {
        nodeFontColor = c;
    }

    public void setColorBranches(boolean b) {
        colorBranches = b;
    }

    public boolean getColorBranches() {
        return colorBranches;
    }

    public boolean getZoomDrawNodeLabels() {
        return zoomDrawNodeLabels;
    }

    public boolean getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(boolean b) {
        selectMode = b;
    }

    public void setPieChartRadius(int i) {
        pieChartRadius = i;
    }

    public void setCollapseMode(String s) {
        collapseMode = s;
    }

    public int getCurrentVerticalScrollPosition() {
        if (root == null) return 0;
        if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            return (int) (root.depth() * yscale + TREEMARGIN - ystart);
        }
        return (int) -(ystart);
    }

    public void setVerticalScrollPosition(int value) {
        ystart = -value;
        if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            ystart = root.depth() * yscale + TREEMARGIN - value;
        }
        fireStateChanged();
        redraw();
    }

    public int getCurrentHorizontalScrollPosition() {
        if (root == null) return 0;
        if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            return (int) (root.depth() * xscale + TREEMARGIN - xstart);
        }
        return (int) -(xstart);
    }

    public void setHorizontalScrollPosition(int value) {
        xstart = -value;
        if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            xstart = root.depth() * xscale + TREEMARGIN - value;
        }
        fireStateChanged();
        redraw();
    }

    public int getMaxVerticalScrollPosition() {
        if (root == null) return 0;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            return (int) (root.getNumberOfLeaves() * yscale + MARGIN);
        } else {
            return (int) (2 * (root.depth() * yscale + TREEMARGIN));
        }
    }

    public int getMaxHorizontalScrollPosition() {
        if (root == null) return 0;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            return (int) (root.depth() * xscale + MARGIN + TREEMARGIN);
        } else {
            return (int) (2 * (root.depth() * xscale + TREEMARGIN));
        }
    }

    public void setRotate(double value) {
        treerotation = value;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    /**
     * Notifies all the listeners that the tree's state has changed.
     */
    private void fireStateChanged() {
        if (!listeners.isEmpty()) {
            ChangeEvent evt = new ChangeEvent(this);
            for (int i = 0; i < listeners.size(); i++) {
                ((ChangeListener) listeners.get(i)).stateChanged(evt);
            }
        }
    }

    public void keyReleased() {
        if (!(keyPressed && keyCode == SHIFT)) {
            draggingLabel = false;
            mouseOverNode = findNode(mouseX, mouseY);
            mouseOverNodeToReplace = null;
            redraw();
            if (mouseOverNode == null) {
                cursor(ARROW);
            }
        }
        if (mouseOverNode == null) {
            cursor(ARROW);
        }
    }

    public void keyPressed() {
        Node n = this.selectedNode;
        if (!draggingLabel && n != null && !n.isLeaf() && key != CODED && key != BACKSPACE && key != TAB && key != ENTER && key != RETURN && key != ESC && key != DELETE) {
            n.setLabel(n.getLabel() + key);
            redraw();
        } else if (!draggingLabel && n != null && !n.isLeaf() && key == BACKSPACE) {
            if (n.getLabel().length() > 0) {
                n.setLabel(n.getLabel().substring(0, n.getLabel().length() - 1));
                redraw();
            }
        } else if (key == 's') {
            selectingMode = !selectingMode;
        }
    }

    /**
     * Convert from branch-length/row to screen coords based on scaling and translation
     */
    private double toScreenX(double col) {
        return xstart + xscale * col;
    }

    private double toScreenY(double row) {
        return ystart + yscale * row;
    }

    /**
     * Convert from screen coords to branch-length/row based on scaling and translation
     */
    private double toLength(double xs) {
        if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            xs = xs - getWidth() * 0.5;
        }
        return (xs - xstart) / xscale;
    }

    private double toRow(double ys) {
        if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            ys = ys - getHeight() * 0.5;
        }
        return (ys - ystart) / (yscale);
    }

    /**
     * mouseDragged() is called whenever the user drags the mouse
     */
    public void mouseDragged() {
        if (this.mouseOverNode == null && keyPressed == false) {
            cursor(MOVE);
            double bxdiff = mouseX - pmouseX;
            double bydiff = mouseY - pmouseY;
            xstart = xstart + bxdiff;
            ystart = ystart + bydiff;
            checkBounds();
            fireStateChanged();
        } else if (this.mouseOverNode != null) {
            if (keyPressed == true && keyCode == SHIFT && !mouseOverNode.isLeaf() && !mouseOverNode.isCollapsed()) {
                draggingLabel = true;
                Node n = findNode(mouseX, mouseY);
                if (n == null) {
                    mouseOverNodeToReplace = null;
                } else {
                    if (!n.isLeaf()) {
                        this.mouseOverNodeToReplace = n;
                    }
                }
            } else if (keyPressed == true && keyCode == SHIFT && !mouseOverNode.isLeaf() && mouseOverNode.isCollapsed()) {
                double bxdiff = mouseX - pmouseX;
                double bydiff = mouseY - pmouseY;
                mouseOverNode.setLabelYOffset(mouseOverNode.getLabelYOffset() + bydiff);
                mouseOverNode.setLabelXOffset(mouseOverNode.getLabelXOffset() + bxdiff);
            } else {
                this.mouseOverNode = null;
                this.mouseOverNodeToReplace = null;
            }
        }
        redraw();
    }

    public void mouseReleased() {
        mouseOverNodeToReplace = findNode(mouseX, mouseY);
        if (draggingLabel && mouseOverNodeToReplace != null && !mouseOverNode.isCollapsed()) {
            String s = mouseOverNode.getLabel();
            if (s.length() == 0) s = mouseOverNode.getName();
            mouseOverNodeToReplace.setLabel(s);
            mouseOverNode.setLabel("");
            selectedNode = mouseOverNodeToReplace;
        } else if (draggingLabel && mouseOverNode.isCollapsed()) {
            double bxdiff = mouseX - pmouseX;
            double bydiff = mouseY - pmouseY;
        }
        draggingLabel = false;
        mouseOverNode = findNode(mouseX, mouseY);
        mouseOverNodeToReplace = null;
        redraw();
    }

    /**
     * mouseMoved() is called whenever the mouse is moved.
     */
    public void mouseMoved() {
        if (!frame.isActive()) return;
        Node node = findNode(mouseX, mouseY);
        if (node != null) {
            cursor(HAND);
            mouseOverNode = node;
        } else {
            cursor(ARROW);
            mouseOverNode = null;
        }
        mouseOverNodeToReplace = null;
        redraw();
    }

    /**
     * mousePressed() is called whenever the mouse is pressed.
     */
    public void mousePressed() {
        Node node = findNode(mouseX, mouseY);
        if (node != null) {
            if (mouseButton == LEFT) {
                if (mouseEvent.getClickCount() == 1) {
                    selectedNode = node;
                } else if (mouseEvent.getClickCount() == 2 && !treeLayout.equals("Polar")) {
                    node.setCollapsed(!node.isCollapsed());
                }
            }
        } else {
            if (selectingMode) {
                resetSelection();
                selectXStart = mouseX;
                selectYStart = mouseY;
            } else if (mouseEvent.getClickCount() == 1) {
                selectedNode = null;
                resetSelection();
            }
        }
        redraw();
    }

    public void resetSelection() {
        selectXStart = 0;
        selectYStart = 0;
        selectXEnd = 0;
        selectYEnd = 0;
        selectPoly = null;
    }

    /**
     * Ensures that the tree is not scrolled out of its bounds, and resets it back if it is
     */
    public void checkBounds() {
        if (root == null) return;
        if (yscale > nodeFontSize) {
            zoomDrawNodeLabels = true;
        } else zoomDrawNodeLabels = false;
        float textwidth = 0;
        String s = "";
        if (drawExternalNodeLabels && zoomDrawNodeLabels) s = root.getLongestLabel();
        for (int i = 0; i < s.length(); i++) {
            textwidth += nodeFont.width(s.charAt(i));
        }
        TREEMARGIN = textwidth * nodeFont.size + 3;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            if (xscale < (getWidth() - TREEMARGIN - 5) / root.depth()) {
                resetTreeX();
            }
            if (xstart > MARGIN) {
                xstart = MARGIN;
            } else if (xstart + xscale * root.depth() < getWidth() - TREEMARGIN - 5) {
                xstart = getWidth() - MARGIN - xscale * root.depth();
            }
            if (yscale < (getHeight() - 2 * MARGIN) / root.getNumberOfLeaves()) {
                resetTreeY();
            }
            if (ystart > MARGIN) {
                ystart = MARGIN;
            } else if (ystart + yscale * root.getNumberOfLeaves() < getHeight() - MARGIN) {
                ystart = getHeight() - MARGIN - yscale * root.getNumberOfLeaves();
            }
        } else if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            if (xscale < (Math.min(getWidth(), getHeight()) * 0.5 - TREEMARGIN) / root.depth()) {
                resetTreeX();
            }
            if (xstart < getWidth() - (xscale * root.depth() + TREEMARGIN)) xstart = getWidth() - (xscale * root.depth() + TREEMARGIN);
            if (xstart > (xscale * root.depth() + TREEMARGIN)) xstart = (xscale * root.depth() + TREEMARGIN);
            if (yscale < (Math.min(getWidth(), getHeight()) * 0.5 - TREEMARGIN) / root.depth()) {
                resetTreeY();
            }
            if (ystart < getHeight() - (yscale * root.depth() + TREEMARGIN)) ystart = getHeight() - (yscale * root.depth() + TREEMARGIN);
            if (ystart > (yscale * root.depth() + TREEMARGIN)) ystart = (yscale * root.depth() + TREEMARGIN);
        }
        fireStateChanged();
        redraw();
    }

    /**
     * Resets the scale and position of the tree horizontally
     */
    public void resetTreeX() {
        float usableWidth = getWidth() - TREEMARGIN - 5;
        float usableHeight = getHeight() - TREEMARGIN - 5;
        if (root == null) return;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            xscale = usableWidth / root.depth();
            xstart = MARGIN;
        } else if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            xscale = (Math.min(usableWidth, usableHeight) * 0.5) / root.depth();
            xstart = getWidth() * 0.5;
        }
    }

    /**
     * Resets the scale and position of the tree vertically
     */
    public void resetTreeY() {
        float usableWidth = getWidth() - TREEMARGIN - 5;
        float usableHeight = getHeight() - TREEMARGIN - 5;
        if (root == null) return;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            yscale = (getHeight() - 2 * MARGIN) / root.getNumberOfLeaves();
            ystart = MARGIN;
        } else if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            yscale = (Math.min(usableWidth, usableHeight) * 0.5) / root.depth();
            ystart = getHeight() * 0.5;
        }
    }

    /**
     * Sets the tree layout
     */
    public void setTreeLayout(String layout) {
        treeLayout = layout;
        checkBounds();
        fireStateChanged();
        redraw();
    }

    public void resetTree() {
        setTree(root);
    }

    /**
     * Replace the entire tree, recalculating cached values
     *
     * @param  newRoot  the Node object that is the root of the new tree
     */
    public void setTree(Node newRoot) {
        float textwidth = 0;
        String s = newRoot.getLongestLabel();
        for (int i = 0; i < s.length(); i++) {
            textwidth += nodeFont.width(s.charAt(i));
        }
        TREEMARGIN = textwidth * nodeFont.size + 5;
        root = newRoot;
        setYOffsets(newRoot, 0);
        setXOffsets(newRoot, 0);
        rootdepth = root.depth();
        checkBounds();
        fireStateChanged();
        redraw();
    }

    public void setVerticalScaleFactor(double yvalue) {
        yscale = yvalue;
        fireStateChanged();
        redraw();
    }

    public void setHorizontalScaleFactor(double xvalue) {
        xscale = xvalue;
        fireStateChanged();
        redraw();
    }

    /**
     * Rescales the tree, keeping the point (x, y) in screen coords at the same position relative
     * to the tree.  Note that rescaling the tree only has an effect on the vertical scale.
     *
     * @param  xvalue  the new xscale value
     * @param  yvalue  the new yscale value
     * @param  x  the x position to keep the same while the tree is scaled
     * @param  y  the y position to keep the same while the tree is scaled
     */
    public void setScaleFactor(double xvalue, double yvalue, double x, double y) {
        double l = toLength(x);
        double r = toRow(y);
        yscale = yvalue;
        xscale = xvalue;
        setVerticalScrollPosition(getMaxVerticalScrollPosition() / 2);
        setHorizontalScrollPosition(getMaxHorizontalScrollPosition() / 2);
        checkBounds();
        fireStateChanged();
        redraw();
    }

    public void selectNodes() {
        int sxstart = (int) Math.min(selectXStart, selectXEnd);
        int sxend = (int) Math.max(selectXStart, selectXEnd);
        int systart = (int) Math.min(selectYStart, selectYEnd);
        int syend = (int) Math.max(selectYStart, selectYEnd);
        int[] xs = new int[] { sxstart, sxstart, sxend, sxend };
        int[] ys = new int[] { systart, systart, syend, syend };
        selectPoly = new Polygon(xs, ys, 4);
        selectNodes(root);
    }

    public void selectNodes(Node tree) {
        if (tree == null) return;
        if (tree.isHidden()) return;
        double row = 0;
        double col = 0;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            row = tree.getYOffset();
            col = tree.getXOffset();
        } else if (treeLayout.equals("Radial")) {
            row = tree.getRYOffset();
            col = tree.getRXOffset();
        } else if (treeLayout.equals("Polar")) {
            row = tree.getROffset() * Math.sin(tree.getTOffset());
            col = tree.getROffset() * Math.cos(tree.getTOffset());
        }
        double nodeX = toScreenX(col);
        double nodeY = toScreenY(row);
        if (selectPoly.contains(nodeX, nodeY)) hilightedNodes.add(tree);
        for (int i = 0; i < tree.nodes.size(); i++) {
            Node child = tree.nodes.get(i);
            selectNodes(child);
        }
    }

    /**
     * Calls findNode(Node,double,double) on the root of the tree
     *
     * @param  x  the x-value to search for the node at
     * @param  y  the y-value to search for the node at
     * @see #findNode(Node,double,double)
     */
    public Node findNode(double x, double y) {
        return findNode(root, mouseX, mouseY);
    }

    /**
     * Find the node in the tree _tree_ at the given x and y coordinates.
     *
     * @param  tree  the root of the tree to search in
     * @param  x  the x-value to search for the node at
     * @param  y  the y-value to search for the node at
     * @param  _length  an internal parameter used in recusion; set to 0 when calling
     */
    private Node findNode(Node tree, double x, double y) {
        if (tree == null) return null;
        if (tree.isHidden()) return null;
        double row = 0;
        double col = 0;
        if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
            row = tree.getYOffset();
            col = tree.getXOffset();
        } else if (treeLayout.equals("Radial")) {
            row = tree.getRYOffset();
            col = tree.getRXOffset();
        } else if (treeLayout.equals("Polar")) {
            row = tree.getROffset() * Math.sin(tree.getTOffset() + treerotation * Math.PI / 180.0);
            col = tree.getROffset() * Math.cos(tree.getTOffset() + treerotation * Math.PI / 180.0);
        }
        double nodeX = toScreenX(col);
        double nodeY = toScreenY(row);
        double minX = nodeX;
        double maxX = minX + 5;
        double minY = nodeY - 5;
        double maxY = minY + 5;
        if (tree.isCollapsed()) {
            double shortest = toScreenX(tree.shortestRootToTipDistance() - tree.getBranchLength());
            double longest = toScreenX(tree.longestRootToTipDistance() - tree.getBranchLength());
            if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
                double top = toScreenY(tree.getMaximumYOffset()) - nodeY;
                top = (top / 2) * wedgeHeightScale;
                double bottom = nodeY - toScreenY(tree.getMinimumYOffset());
                bottom = (bottom / 2) * wedgeHeightScale;
                maxY = nodeY + top;
                minY = nodeY - bottom;
                int[] xs = new int[] { (int) Math.floor(nodeX), (int) Math.floor(nodeX), (int) Math.ceil(nodeX + shortest), (int) Math.ceil(nodeX + longest) };
                int[] ys = new int[] { (int) Math.floor(minY), (int) Math.ceil(maxY), (int) Math.ceil(maxY), (int) Math.floor(minY) };
                Polygon poly = new Polygon(xs, ys, 4);
                if (poly.contains(x, y)) {
                    return tree;
                } else return null;
            } else if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
                double maxt = tree.getMaximumTOffset();
                double mint = tree.getMinimumTOffset();
                if (wedgeHeightScale < 1) {
                    double theta = Math.abs(maxt - mint);
                    double f = (theta * (1 - wedgeHeightScale)) / 2;
                    mint = mint + f;
                    maxt = maxt - f;
                }
                double x1 = tree.getParent().getRXOffset() + shortest * Math.cos(mint);
                double y1 = tree.getParent().getRYOffset() + shortest * Math.sin(mint);
                double x2 = tree.getParent().getRXOffset() + longest * Math.cos(maxt);
                double y2 = tree.getParent().getRYOffset() + longest * Math.sin(maxt);
                int[] xs = new int[] { (int) nodeX, (int) toScreenX(x1), (int) toScreenX(x2) };
                int[] ys = new int[] { (int) nodeY, (int) toScreenY(y1), (int) toScreenY(y2) };
                Polygon poly = new Polygon(xs, ys, 3);
                if (poly.contains(x, y)) {
                    return tree;
                } else return null;
            }
        }
        if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
            return tree;
        }
        for (int i = 0; i < tree.nodes.size(); i++) {
            Node child = tree.nodes.get(i);
            Node found = findNode(child, x, y);
            if (found != null) return found;
        }
        return null;
    }

    private void drawSelectBox() {
        float[] dashes = { 4.0f, 4.0f, 4.0f, 4.0f };
        BasicStroke bs = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4.0f, dashes, 0.0f);
        Graphics2D g2 = ((PGraphicsJava2D) g).g2;
        g2.setStroke(bs);
        g.noFill();
        g.rect((float) selectXStart, (float) selectYStart, (float) selectXEnd, (float) selectYEnd);
    }

    /**
     * Draws the tree to the screen.  Uses the PApplet.g handle to the screen to draw to it.
     *
     * @param  node  the root the tree to draw to the screen
     * @param  x  the x-position to draw the tree at
     */
    private void drawTree(Node node) {
        g.textFont(nodeFont);
        checkBounds();
        drawTree(node, g);
        g.noFill();
    }

    /**
     * Draw the entire sub-tree rooted at _node_, with _node_ positioned at the given (absolute, unscaled) coords.
     * Draws to the specified canvas.  beginDraw() and endDraw() should be called outside of this function if drawing
     * to a PGraphics objet.
     *
     * @param  node  the root of the tree to draw to the screen
     * @param  x  the x-position to draw the tree at
     * @param  canvas  a handle to the PGraphics object that should be drawn to.
     * @see  drawTree(Node, double)
     */
    private void drawTree(Node node, PGraphics canvas) {
        if (root == null) return;
        if (node.isHidden()) return;
        boolean isInternal = !node.isLeaf();
        if (node.getParent() == null) {
            node.setSliderCollapsed(false);
        } else if (collapseMode.equals("Root")) {
            if (node.depth() / rootdepth <= collapsedLevel) {
                node.setSliderCollapsed(true);
            } else node.setSliderCollapsed(false);
        } else if (collapseMode.equals("Parent")) {
            if (node.depth() / node.getParent().depth() <= collapsedLevel) {
                node.setSliderCollapsed(true);
            } else node.setSliderCollapsed(false);
        }
        if (isInternal) {
            if (node.isCollapsed() && !treeLayout.equals("Polar")) {
                drawWedge(node, canvas);
            } else {
                drawBranches(node, canvas);
            }
        }
        if ((isInternal && !node.isCollapsed()) || (node.isCollapsed() && isInternal && treeLayout.equals("Polar"))) {
            for (int i = 0; i < node.nodes.size(); i++) {
                Node child = node.nodes.get(i);
                drawTree(child, canvas);
            }
        }
        drawNode(node, canvas);
        if (node.getDrawPie()) {
            double x = node.getXOffset();
            double y = node.getYOffset();
            if (treeLayout.equals("Radial")) {
                x = node.getRXOffset();
                y = node.getRYOffset();
            } else if (treeLayout.equals("Polar")) {
                x = node.getROffset() * Math.cos(node.getTOffset());
                y = node.getROffset() * Math.sin(node.getTOffset());
            }
            PieChart pie = new PieChart((int) Math.round(toScreenX(x)), (int) Math.round(toScreenY(y)), pieChartRadius, node.getGroupBranchFraction(), node.getGroupBranchColor());
            drawPieChart(pie, canvas);
        }
    }

    private void drawPieChart(PieChart pie, PGraphics canvas) {
        double total = 0;
        for (int i = 0; i < pie.data.size(); i++) {
            total += pie.data.get(i);
        }
        double[] percents = new double[pie.data.size()];
        for (int i = 0; i < percents.length; i++) {
            percents[i] = pie.data.get(i) / total;
        }
        double[] angles = new double[pie.data.size()];
        for (int i = 0; i < angles.length; i++) {
            angles[i] = 360 * percents[i];
        }
        double lastAng = 0;
        canvas.stroke(0);
        canvas.strokeWeight(1);
        for (int i = 0; i < angles.length; i++) {
            canvas.fill(pie.colors.get(i).getRGB(), 175);
            canvas.arc(pie.x, pie.y, (float) pie.diameter, (float) pie.diameter, (float) lastAng, (float) (lastAng + radians((float) angles[i])));
            double midangle = lastAng + radians((float) angles[i]) / 2.0;
            lastAng = lastAng + radians((float) angles[i]);
        }
    }

    /**
     * Draw the actual node, not including branch lines and child nodes.
     *
     * @param  node  the node to draw
     * @param  x  the x value to draw the node at
     * @param  y  the y value to draw the node at
     * @param  canvas  the PGraphics object to draw the node to
     */
    private void drawNode(Node node, PGraphics canvas) {
        double offsetbias = 0;
        double x = node.getXOffset();
        double y = node.getYOffset();
        if (treeLayout.equals("Radial")) {
            x = node.getRXOffset();
            y = node.getRYOffset();
        } else if (treeLayout.equals("Polar")) {
            x = node.getROffset() * Math.cos(node.getTOffset());
            y = node.getROffset() * Math.sin(node.getTOffset());
        }
        double xs = toScreenX(x);
        double ys = toScreenY(y);
        boolean selected = (node == selectedNode);
        boolean hilighted = hilightedNodes.contains(node);
        float drawX = (float) xs;
        float drawY = (float) ys;
        String s = node.getLabel();
        if (s.length() == 0) s = node.getName();
        double textwidth = 0;
        if (node.getDrawLabel() && ((node.isLeaf() && drawExternalNodeLabels) || (!node.isLeaf() && drawInternalNodeLabels)) && zoomDrawNodeLabels) {
            for (int i = 0; i < s.length(); i++) {
                textwidth += nodeFont.width(s.charAt(i));
            }
        } else textwidth = 1;
        if ((treeLayout.equals("Polar") || treeLayout.equals("Radial")) && (node.getParent() != null) && (node.getParent().longestBranch() != node)) textwidth = 1;
        if (draggingLabel && this.mouseOverNode == node) {
            drawX = mouseX;
            drawY = mouseY;
            canvas.text(s, (float) (drawX + offsetbias), (float) (drawY));
        }
        canvas.strokeWeight((float) .2);
        Color c = null;
        if (!colorBranches) {
            canvas.stroke(0);
            canvas.fill(0);
        } else {
            c = node.getBranchColor(majorityColoring);
            if (c == null) {
                canvas.stroke(0);
                canvas.fill(0);
            } else {
                canvas.stroke(c.getRGB());
                canvas.fill(c.getRGB());
            }
        }
        double minX = drawX + offsetbias - 1;
        double fullrotation = 0;
        boolean textflip = false;
        double rotation = 0;
        double maxX = drawX + (textwidth * nodeFont.size);
        if (treeLayout.equals("Polar") || treeLayout.equals("Radial")) {
            canvas.pushMatrix();
            rotation = 0;
            if (treeLayout.equals("Radial")) {
                rotation = Math.atan2(yscale * node.getRYOffset(), xscale * node.getRXOffset());
                double xt = node.getRXOffset();
                double yt = node.getRYOffset();
                drawX = (float) Math.sqrt((xscale * xt) * (xscale * xt) + (yscale * yt) * (yscale * yt));
            } else {
                rotation = node.getTOffset();
                double xt = node.getROffset() * Math.cos(rotation);
                double yt = node.getROffset() * Math.sin(rotation);
                rotation = Math.atan2(yscale * yt, xscale * xt);
                drawX = (float) Math.sqrt((xscale * xt) * (xscale * xt) + (yscale * yt) * (yscale * yt));
            }
            fullrotation = rotation + treerotation * Math.PI / 180.0;
            if (fullrotation < 0) {
                fullrotation = fullrotation + 2 * Math.PI;
            }
            if (fullrotation > 2 * Math.PI) {
                fullrotation = fullrotation - 2 * Math.PI;
            }
            if (rotation < 0) {
                rotation = rotation + 2 * Math.PI;
            }
            if (rotation > 2 * Math.PI) {
                rotation = fullrotation - 2 * Math.PI;
            }
            drawY = (float) 0;
            canvas.translate((float) xstart, (float) ystart);
            canvas.rotate((float) rotation);
            canvas.textAlign(LEFT);
            if (fullrotation > Math.PI / 2 && fullrotation < 3 * Math.PI / 2) {
                canvas.textAlign(RIGHT);
                drawX = -drawX - (float) offsetbias - 2;
                maxX = drawX - (textwidth * nodeFont.size);
                canvas.rotate((float) Math.PI);
            }
        }
        maxX = drawX + (textwidth * nodeFont.size);
        double maxY = ((nodeFont.ascent() + nodeFont.descent()) * nodeFont.size);
        double minY = drawY - maxY;
        if (((!node.isLeaf() && drawInternalNodeLabels && !node.isCollapsed()) || (node.isLeaf() && drawExternalNodeLabels)) && node.getDrawLabel() && zoomDrawNodeLabels) {
            Color lc = node.getLabelColor(majorityColoring);
            if (lc == null) {
                canvas.stroke(0);
                canvas.fill(0);
            } else {
                canvas.fill(lc.getRGB());
                canvas.stroke(lc.getRGB());
            }
            canvas.text(s, (float) (drawX + offsetbias), (float) (drawY));
        } else {
        }
        textAlign(LEFT);
        if (selected || hilighted) {
            int sc;
            if (selected) {
                sc = SELECTED_COLOR;
                canvas.fill(sc, 64);
                canvas.stroke(sc);
                canvas.rect((float) (drawX + offsetbias), (float) minY, (float) (maxX - drawX), (float) maxY);
                canvas.noTint();
            }
            if (hilighted) {
                sc = HIGHLIGHTED_COLOR;
                canvas.stroke(sc);
                canvas.strokeWeight((int) yscale);
                canvas.line(getWidth() - 5, drawY, getWidth(), drawY);
            }
        }
        if (mouseOverNode == node || mouseOverNodeToReplace == node) {
            canvas.noFill();
            canvas.strokeWeight(1);
            canvas.stroke(255, 0, 0);
            canvas.rect((float) (drawX + offsetbias), (float) minY, (float) (maxX - drawX), (float) maxY);
            String status = "";
            if (node.isLocked()) status += "(L)";
            if (node.isLeaf()) {
                status += node.getLabel() + ";";
            } else {
                status += String.format("Sub-tree: %d leaves;", node.getNumberOfLeaves());
                String label = node.getConsensusLineage();
                if (label == null) label = "";
                status += label;
            }
            textwidth = 0;
            for (int i = 0; i < status.length(); i++) {
                textwidth += tipFont.width(status.charAt(i));
            }
            maxX = (textwidth * tipFont.size);
            drawX = getWidth() - (float) (textwidth * tipFont.size);
            drawY = getHeight();
            maxY = ((tipFont.ascent() + tipFont.descent()) * tipFont.size);
            if (treeLayout.equals("Polar") || treeLayout.equals("Radial")) {
                canvas.popMatrix();
                canvas.popMatrix();
            }
            canvas.fill(HOVER_COLOR, 150);
            canvas.noStroke();
            canvas.rect((float) (drawX), (float) drawY - 8, (float) (maxX), (float) (maxY));
            canvas.fill(0);
            canvas.stroke(0);
            canvas.textFont(tipFont);
            canvas.text(status, (float) (drawX), (float) (drawY));
            if (treeLayout.equals("Polar") || treeLayout.equals("Radial")) {
                canvas.pushMatrix();
                canvas.translate((float) xstart, (float) ystart);
                canvas.rotate((float) (treerotation * Math.PI / 180.0));
                canvas.translate((float) -xstart, (float) -ystart);
            }
            canvas.textFont(nodeFont);
            canvas.noTint();
        } else {
            if (treeLayout.equals("Polar") || treeLayout.equals("Radial")) {
                canvas.popMatrix();
            }
        }
    }

    /**
     * Draw branch lines connecting a node and its children.
     *
     * @param  node  the node the draw branches for
     * @param  x  the x value of the node
     * @param  y  the y value of the node
     * @param  canvas  the PGraphics object to draw the branches to
     */
    private void drawBranches(Node node, PGraphics canvas) {
        double x = node.getXOffset();
        double y = node.getYOffset();
        if (treeLayout.equals("Radial")) {
            x = node.getRXOffset();
            y = node.getRYOffset();
        } else if (treeLayout.equals("Polar")) {
            x = node.getROffset() * Math.cos(node.getTOffset());
            y = node.getROffset() * Math.sin(node.getTOffset());
        }
        double xs = toScreenX(x);
        double ys = toScreenY(y);
        canvas.strokeWeight((float) node.getLineWidth() * getLineWidthScale());
        Color c = null;
        if (!colorBranches) {
            canvas.stroke(0);
        } else {
            c = node.getBranchColor(majorityColoring);
            if (c == null) canvas.stroke(0); else canvas.stroke(c.getRGB());
        }
        if (treeLayout.equals("Rectangular")) {
            canvas.line((float) xs, (float) toScreenY(node.nodes.get(0).getYOffset()), (float) xs, (float) toScreenY(node.nodes.get(node.nodes.size() - 1).getYOffset()));
            for (Node k : node.nodes) {
                if (!colorBranches) {
                    canvas.stroke(0);
                } else {
                    c = k.getBranchColor(majorityColoring);
                    if (c == null) canvas.stroke(0); else canvas.stroke(c.getRGB());
                }
                canvas.strokeWeight((float) k.getLineWidth() * getLineWidthScale());
                double yp = toScreenY(k.getYOffset());
                double xp = toScreenX(k.getXOffset());
                canvas.line((float) xs, (float) yp, (float) xp, (float) yp);
            }
        } else if (treeLayout.equals("Triangular")) {
            for (Node k : node.nodes) {
                if (!colorBranches) {
                    canvas.stroke(0);
                } else {
                    c = k.getBranchColor(majorityColoring);
                    canvas.stroke(c.getRGB());
                }
                canvas.strokeWeight((float) (k.getLineWidth() * getLineWidthScale()));
                double yp = toScreenY(k.getYOffset());
                double xp = toScreenX(k.getXOffset());
                canvas.line((float) xs, (float) ys, (float) xp, (float) yp);
            }
        } else if (treeLayout.equals("Radial")) {
            for (Node k : node.nodes) {
                if (!colorBranches) {
                    canvas.stroke(0);
                } else {
                    c = k.getBranchColor(majorityColoring);
                    canvas.stroke(c.getRGB());
                }
                double d = k.getLineWidth();
                canvas.strokeWeight((float) d * getLineWidthScale());
                double xp = toScreenX(k.getRXOffset());
                double yp = toScreenY(k.getRYOffset());
                canvas.line((float) xs, (float) ys, (float) xp, (float) yp);
            }
        } else if (treeLayout.equals("Polar")) {
            canvas.ellipseMode(CENTER);
            canvas.noFill();
            double radius = node.getROffset();
            double minradius = Math.min(node.nodes.get(0).getTOffset(), node.nodes.get(node.nodes.size() - 1).getTOffset());
            double maxradius = Math.max(node.nodes.get(0).getTOffset(), node.nodes.get(node.nodes.size() - 1).getTOffset());
            canvas.arc((float) xstart, (float) ystart, (float) (xscale * 2 * radius), (float) (yscale * 2 * radius), (float) (minradius), (float) (maxradius));
            for (Node k : node.nodes) {
                if (!colorBranches) {
                    canvas.stroke(0);
                } else {
                    c = k.getBranchColor(majorityColoring);
                    canvas.stroke(c.getRGB());
                }
                double d = k.getLineWidth();
                canvas.strokeWeight((float) d * getLineWidthScale());
                double xp = k.getROffset() * Math.cos(k.getTOffset());
                double yp = k.getROffset() * Math.sin(k.getTOffset());
                double ctheta = k.getTOffset();
                double xin = radius * Math.cos(ctheta);
                double yin = radius * Math.sin(ctheta);
                canvas.line((float) toScreenX(xin), (float) toScreenY(yin), (float) toScreenX(xp), (float) toScreenY(yp));
            }
        }
    }

    /**
     * Draw a wedge in place of a tree.
     *
     * @param  node  the root of the tree to draw as a wege
     * @param  x  the x value of the root node
     * @param  y  the y value of the root node
     * @param  canvas  the PGraphics object to draw to
     */
    private void drawWedge(Node node, PGraphics canvas) {
        double x = node.getXOffset();
        double y = node.getYOffset();
        if (treeLayout.equals("Radial")) {
            x = node.getRXOffset();
            y = node.getRYOffset();
        } else if (treeLayout.equals("Polar")) {
            x = node.getROffset() * Math.cos(node.getTOffset());
            y = node.getROffset() * Math.sin(node.getTOffset());
        }
        canvas.strokeWeight((float) node.getLineWidth() * getLineWidthScale());
        Color c = null;
        if (!colorBranches) {
            canvas.stroke(0);
            canvas.fill(0);
        } else {
            c = node.getBranchColor(majorityColoring);
            if (c == null) {
                canvas.stroke(0);
                canvas.fill(0);
            } else {
                canvas.stroke(c.getRGB());
                canvas.fill(c.getRGB());
            }
        }
        double longest = node.longestRootToTipDistance() - node.getBranchLength();
        double shortest = node.shortestRootToTipDistance() - node.getBranchLength();
        double top = node.getMaximumYOffset() - y;
        top = (top / 2) * wedgeHeightScale;
        double bottom = y - node.getMinimumYOffset();
        bottom = (bottom / 2) * wedgeHeightScale;
        top = y + top;
        bottom = y - bottom;
        if (treeLayout.equals("Rectangular")) {
            if (mirrored) {
                canvas.quad((float) toScreenX(x), (float) toScreenY(bottom), (float) toScreenX(x), (float) toScreenY(top), (float) toScreenX(x - shortest), (float) toScreenY(top), (float) toScreenX(x - longest), (float) toScreenY(bottom));
            } else {
                canvas.quad((float) toScreenX(x), (float) toScreenY(bottom), (float) toScreenX(x), (float) toScreenY(top), (float) toScreenX(x + shortest), (float) toScreenY(top), (float) toScreenX(x + longest), (float) toScreenY(bottom));
            }
        } else if (treeLayout.equals("Triangular")) {
            if (mirrored) {
                canvas.triangle((float) toScreenX(x), (float) toScreenY(y), (float) toScreenX(x - shortest), (float) toScreenY(top), (float) toScreenX(x - longest), (float) toScreenY(bottom));
            } else {
                canvas.triangle((float) toScreenX(x), (float) toScreenY(y), (float) toScreenX(x + shortest), (float) toScreenY(top), (float) toScreenX(x + longest), (float) toScreenY(bottom));
            }
        } else if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
            double maxt = node.getMaximumTOffset();
            double mint = node.getMinimumTOffset();
            if (wedgeHeightScale < 1) {
                double theta = Math.abs(maxt - mint);
                double f = (theta * (1 - wedgeHeightScale)) / 2;
                mint = mint + f;
                maxt = maxt - f;
            }
            double x1 = node.getParent().getRXOffset() + shortest * Math.cos(mint);
            double y1 = node.getParent().getRYOffset() + shortest * Math.sin(mint);
            double x2 = node.getParent().getRXOffset() + longest * Math.cos(maxt);
            double y2 = node.getParent().getRYOffset() + longest * Math.sin(maxt);
            canvas.triangle((float) toScreenX(x), (float) toScreenY(y), (float) toScreenX(x1), (float) toScreenY(y1), (float) toScreenX(x2), (float) toScreenY(y2));
        }
        canvas.fill(wedgeFontColor);
        canvas.stroke(wedgeFontColor);
        if (drawWedgeLabels && node.getDrawLabel()) {
            canvas.textFont(wedgeFont);
            String s = "";
            if (drawInternalNodeLabels) {
                s = node.getLabel();
            } else if (node.getConsensusLineage() != null) {
                s = node.getConsensusLineage();
                if (s.lastIndexOf(";", s.length() - 2) != -1) s = s.substring(s.lastIndexOf(";", s.length() - 2) + 1, s.length());
            } else {
                s = "" + node.getNumberOfLeaves();
            }
            if (Math.abs((toScreenY(top) - toScreenY(bottom))) > wedgeFontSize) {
                canvas.textFont(wedgeFont);
                if (mirrored) {
                    canvas.text(s, (float) (toScreenX(x - (shortest) / 2) + labelXOffset + node.getLabelXOffset()), (float) (toScreenY(bottom + (top - bottom) / 2) + 5 + labelYOffset + node.getLabelYOffset()));
                } else canvas.text(s, (float) (toScreenX(x + (shortest) / 2) + labelXOffset + node.getLabelXOffset()), (float) (toScreenY(bottom + (top - bottom) / 2) + 5 + labelYOffset + node.getLabelYOffset()));
            }
            canvas.textFont(nodeFont);
        }
        canvas.fill(0);
        canvas.stroke(0);
    }

    /**
     * Sets the y-offset for each node in the tree to the average of its children
     *
     * @param  node  the root of the tree to set y-offsets for
     * @param  _prev  an internal variable for recusion
     */
    public void setYOffsets(Node node, double _prev) {
        if (node.isLeaf()) {
            node.setYOffset(_prev + 1);
        } else {
            double total = 0;
            for (Node n : node.nodes) {
                setYOffsets(n, _prev);
                _prev = _prev + n.getNumberOfLeaves();
                total = total + n.getYOffset();
            }
            node.setYOffset(total / node.nodes.size());
        }
        setMinMaxYOffsets(node);
    }

    public void setMinMaxYOffsets(Node node) {
        node.setMaximumYOffset(node.getYOffset());
        node.setMinimumYOffset(node.getYOffset());
        for (Node n : node.nodes) {
            node.setMaximumYOffset(Math.max(node.getMaximumYOffset(), n.getMaximumYOffset()));
            node.setMinimumYOffset(Math.min(node.getMinimumYOffset(), n.getMinimumYOffset()));
        }
    }

    public void setXOffsets(Node node, double _prev) {
        node.setXOffset(_prev + node.getBranchLength());
        for (Node n : node.nodes) {
            setXOffsets(n, _prev + node.getBranchLength());
        }
    }

    public void setROffsets(Node node, double _prev) {
        node.setROffset(_prev + node.getBranchLength());
        for (Node n : node.nodes) {
            setROffsets(n, _prev + node.getBranchLength());
        }
        node.setMaximumROffset(node.getROffset());
        node.setMinimumROffset(node.getROffset());
        for (Node n : node.nodes) {
            node.setMaximumROffset(Math.max(node.getMaximumROffset(), n.getMaximumROffset()));
            node.setMinimumROffset(Math.min(node.getMinimumROffset(), n.getMinimumROffset()));
        }
    }

    public void setTOffsets(Node node, double _prev) {
        double delta = 2 * Math.PI / root.getNumberOfLeaves();
        if (node.isLeaf()) {
            node.setTOffset(_prev + delta);
        } else {
            double total = 0;
            for (Node n : node.nodes) {
                setTOffsets(n, _prev);
                _prev = _prev + delta * n.getNumberOfLeaves();
                total = total + n.getTOffset();
            }
            node.setTOffset(total / node.nodes.size());
        }
        node.setMaximumTOffset(node.getTOffset());
        node.setMinimumTOffset(node.getTOffset());
        for (Node n : node.nodes) {
            node.setMaximumTOffset(Math.max(node.getMaximumTOffset(), n.getMaximumTOffset()));
            node.setMinimumTOffset(Math.min(node.getMinimumTOffset(), n.getMinimumTOffset()));
        }
    }

    public void setRadialOffsets(Node node) {
        if (node == root) {
            node.setRXOffset(0);
            node.setRYOffset(0);
        } else {
            Node p = node.getParent();
            node.setRXOffset(p.getRXOffset() + node.getBranchLength() * Math.cos(node.getTOffset()));
            node.setRYOffset(p.getRYOffset() + node.getBranchLength() * Math.sin(node.getTOffset()));
        }
        for (Node n : node.nodes) {
            setRadialOffsets(n);
        }
        node.setMaximumRXOffset(node.getRXOffset());
        node.setMinimumRXOffset(node.getRXOffset());
        for (Node n : node.nodes) {
            node.setMaximumRXOffset(Math.max(node.getMaximumRXOffset(), n.getMaximumRXOffset()));
            node.setMinimumRXOffset(Math.min(node.getMinimumRXOffset(), n.getMinimumRXOffset()));
        }
    }

    /**
     * Exports a screen caputre of the tree to a PDF file
     *
     * @param  path The path to write the image to
     */
    public void exportScreenCapture(String path) {
        PGraphics canvas = createGraphics(width, height, P2D);
        canvas.beginDraw();
        canvas.textFont(nodeFont);
        canvas.background(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
        canvas.pushMatrix();
        canvas.translate((float) xstart, (float) ystart);
        canvas.rotate((float) (treerotation * Math.PI / 180.0));
        canvas.translate((float) -xstart, (float) -ystart);
        drawTree(root, canvas);
        canvas.popMatrix();
        canvas.endDraw();
        canvas.save(path);
    }

    /**
     * Export the entire tree to a PDF file
     *
     * @param  path The path to write the image to
     * @param  dims The dimentions of the output image file
     */
    public void exportTreeImage(String path, int dims[]) {
        float oldLineWidth = getLineWidthScale();
        int oldWidth = getWidth();
        int oldHeight = getHeight();
        double oldXScale = xscale;
        double oldYScale = yscale;
        double oldXStart = xstart;
        double oldYStart = ystart;
        try {
            setLineWidthScale(oldLineWidth * (float) .2);
            xstart = 0;
            ystart = 0;
            width = dims[0];
            height = dims[1];
            TREEMARGIN = 0;
            if (drawExternalNodeLabels && zoomDrawNodeLabels) TREEMARGIN = textWidth(root.getLongestLabel());
            float usableWidth = 0;
            float usableHeight = 0;
            if (treeLayout.equals("Rectangular") || treeLayout.equals("Triangular")) {
                usableWidth = dims[0] - TREEMARGIN - 5;
                usableHeight = dims[1] - (float) MARGIN * 2 - 5;
                xscale = usableWidth / root.depth();
                xstart = MARGIN;
                yscale = usableHeight / root.getNumberOfLeaves();
                ystart = MARGIN;
            } else if (treeLayout.equals("Radial") || treeLayout.equals("Polar")) {
                usableWidth = dims[0] - 2 * TREEMARGIN - 5;
                usableHeight = dims[1] - 2 * TREEMARGIN - 5;
                xscale = (Math.min(usableWidth, usableHeight) * 0.5) / root.depth();
                xstart = dims[0] * 0.5;
                yscale = (Math.min(usableWidth, usableHeight) * 0.5) / root.depth();
                ystart = dims[1] * 0.5;
            }
            PGraphics canvas = createGraphics((int) (dims[0]), (int) (dims[1]), PDF, path);
            canvas.beginDraw();
            canvas.background(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
            canvas.pushMatrix();
            canvas.translate((float) xstart, (float) ystart);
            canvas.rotate((float) (treerotation * Math.PI / 180.0));
            canvas.translate((float) -xstart, (float) -ystart);
            canvas.textFont(nodeFont);
            drawTree(root, canvas);
            canvas.popMatrix();
            canvas.dispose();
            canvas.endDraw();
            if (Desktop.isDesktopSupported()) {
                try {
                    File myFile = new File(path);
                    Desktop.getDesktop().open(myFile);
                } catch (IOException ex) {
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to export pdf.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        setLineWidthScale(oldLineWidth);
        xscale = oldXScale;
        yscale = oldYScale;
        xstart = oldXStart;
        ystart = oldYStart;
        width = oldWidth;
        height = oldHeight;
        redraw();
    }
}
