package org.tigr.microarray.mev.cluster.gui.helpers.ktree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.util.Vector;
import javax.swing.JPanel;

/**
 *
 * @author  braisted
 */
public class Ktree extends JPanel {

    /**
     * Array of tree nodes.  Nodes should be represeted by level.
     * Ordering represents left to right order
     */
    private ITreeNode[][] nodes;

    /**
     * Root node
     */
    private ITreeNode root;

    private int xMargin = 15;

    private int yMargin = 15;

    private int maxWidth = 0;

    private int interNodeHeight = 40;

    private int interNodeWidth = 15;

    private boolean nodeSelected = false;

    private boolean isStraitConnector = false;

    Vector selectedPathNodes;

    ITreeNode selectedNode;

    boolean isNodeSelected;

    /** Creates a new instance of Ktree */
    public Ktree(ITreeNode root) {
        nodes = new ITreeNode[1][1];
        nodes[0][0] = root;
        this.root = root;
        this.selectedPathNodes = new Vector();
        init();
    }

    /** Creates a new instance of Ktree with the provided node array structure */
    public Ktree(ITreeNode[][] data) {
        nodes = data;
        root = nodes[0][0];
        this.selectedPathNodes = new Vector();
        init();
    }

    /** Creates a new instance of Ktree with the provided node data as an array of
     * Vectors.  One vector for each level in the tree.  Nodes know the relationships.
     */
    public Ktree(Vector[] data) {
        init();
        this.selectedPathNodes = new Vector();
    }

    private void init() {
        setBackground(Color.white);
        updateSize();
    }

    public boolean addNode(ITreeNode[] parentNodes, ITreeNode childNodetoAdd, int levl) {
        return true;
    }

    public boolean deleteNode(ITreeNode node) {
        return true;
    }

    public void setNodes(ITreeNode[][] newNodes) {
        this.nodes = newNodes;
    }

    private void minimizeBranchingOverlap() {
    }

    public ITreeNode getRoot() {
        return this.root;
    }

    public int getMaxLevelWidth() {
        int max = -1;
        for (int i = 0; i < nodes.length; i++) {
            max = Math.max(max, nodes[i].length);
        }
        maxWidth = max;
        return max;
    }

    public Vector getSelectedPathNodes() {
        return selectedPathNodes;
    }

    public int getTreePixelWidth() {
        int levelWidth = getMaxLevelWidth();
        int treeWidth = 2 * xMargin + levelWidth * (interNodeWidth + nodes[0][0].getWidth()) - interNodeWidth;
        return treeWidth;
    }

    public int getTreePixelHeight() {
        int levelHeight = nodes.length;
        int treeHeight = 2 * xMargin + levelHeight * (interNodeHeight + nodes[0][0].getHeight()) - interNodeHeight;
        return treeHeight;
    }

    public void setStraightConnectorStyle(boolean isStraitConn) {
        this.isStraitConnector = isStraitConn;
    }

    public void setInterNodeHeight(int height) {
        this.interNodeHeight = height;
    }

    public void setInterNodeWidth(int width) {
        this.interNodeWidth = width;
    }

    public void setSelectedNode(ITreeNode node) {
        this.selectedNode = node;
        this.isNodeSelected = true;
    }

    public void setSelectionPaths(Vector nodes) {
        this.selectedPathNodes = nodes;
    }

    public void clearSelection() {
        this.isNodeSelected = false;
        this.selectedPathNodes = new Vector();
    }

    public boolean checkSelection(int x, int y, int selectionPolarity) {
        this.selectedNode = getSelectedNode(x, y);
        if (this.selectedNode == null) {
            clearSelection();
            return false;
        } else {
            this.isNodeSelected = true;
            setSelectionPaths(getPathNodes(this.selectedNode, selectionPolarity));
        }
        return true;
    }

    public ITreeNode getSelectedNode() {
        return selectedNode;
    }

    private ITreeNode getSelectedNode(int x, int y) {
        ITreeNode node = null;
        int level = (int) ((y - yMargin) / (interNodeHeight + nodes[0][0].getHeight()));
        if (level < 0 || level >= nodes.length) return null;
        for (int i = 0; i < nodes[level].length; i++) {
            if (nodes[level][i].contains(x, y)) {
                node = nodes[level][i];
                break;
            }
        }
        return node;
    }

    public Vector getPathNodes(ITreeNode node, int polarity) {
        Vector ancestors, successors;
        if (polarity == 0) {
            ancestors = new Vector();
            successors = new Vector();
            ancestors.addElement(node);
            node.getAncestors(ancestors);
            node.getSuccessors(successors);
            for (int i = 0; i < successors.size(); i++) {
                ancestors.addElement(successors.elementAt(i));
            }
            return ancestors;
        } else if (polarity == 1) {
            ancestors = new Vector();
            ancestors.addElement(node);
            node.getAncestors(ancestors);
            return ancestors;
        } else if (polarity == 2) {
            successors = new Vector();
            successors.addElement(node);
            node.getSuccessors(successors);
            return successors;
        }
        return new Vector();
    }

    public void updateSize() {
        int width = getTreePixelWidth();
        int height = getTreePixelHeight();
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int nodeWidth = root.getWidth();
        int nodeHeight = root.getHeight();
        int maxLevelWidth = (maxWidth) * (nodeWidth + interNodeWidth) - interNodeWidth;
        int currX = xMargin + (maxLevelWidth - nodeWidth) / 2;
        int currY = yMargin;
        int levelWidth;
        boolean selected = false;
        if (this.selectedNode != null && this.selectedNode == nodes[0][0]) ((ITreeNodeRenderer) nodes[0][0]).renderNode(g2, currX, currY, ITreeNodeRenderer.SELECTED_NODE); else if (selectedPathNodes.contains(nodes[0][0])) ((ITreeNodeRenderer) nodes[0][0]).renderNode(g2, currX, currY, ITreeNodeRenderer.PATH_NODE); else if (this.selectedNode != null) ((ITreeNodeRenderer) nodes[0][0]).renderNode(g2, currX, currY, ITreeNodeRenderer.NON_PATH_NODE); else ((ITreeNodeRenderer) nodes[0][0]).renderNode(g2, currX, currY, ITreeNodeRenderer.STANDARD_NODE);
        for (int i = 1; i < nodes.length; i++) {
            currY += (nodeHeight + interNodeHeight);
            levelWidth = (nodes[i].length) * (nodeWidth + interNodeWidth) - interNodeWidth;
            currX = xMargin + (maxLevelWidth - levelWidth) / 2;
            for (int j = 0; j < nodes[i].length; j++) {
                if (this.selectedNode != null && this.selectedNode == nodes[i][j]) ((ITreeNodeRenderer) nodes[i][j]).renderNode(g2, currX, currY, ITreeNodeRenderer.SELECTED_NODE); else if (selectedPathNodes.contains(nodes[i][j])) ((ITreeNodeRenderer) nodes[i][j]).renderNode(g2, currX, currY, ITreeNodeRenderer.PATH_NODE); else if (this.selectedNode != null) ((ITreeNodeRenderer) nodes[i][j]).renderNode(g2, currX, currY, ITreeNodeRenderer.NON_PATH_NODE); else ((ITreeNodeRenderer) nodes[i][j]).renderNode(g2, currX, currY, ITreeNodeRenderer.STANDARD_NODE);
                currX += nodeWidth + interNodeWidth;
            }
        }
        renderConnectors(g);
    }

    private void renderConnectors(Graphics g) {
        ITreeNode currNode;
        ITreeNode[] children;
        Point start, finish;
        boolean selected = false;
        Color origColor = g.getColor();
        Graphics2D g2 = (Graphics2D) g;
        Composite composite = g2.getComposite();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[i].length; j++) {
                currNode = (nodes[i][j]);
                children = currNode.getChildren();
                if (children == null) {
                    break;
                }
                start = currNode.getBottomAnchorPoint();
                for (int k = 0; k < children.length; k++) {
                    selected = false;
                    if (this.selectedPathNodes.contains(currNode) && this.selectedPathNodes.contains(children[k])) {
                        selected = true;
                        g.setColor(Color.blue);
                    } else if (this.selectedPathNodes.size() != 0) {
                        AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
                        g2.setComposite(alphaComp);
                    }
                    finish = children[k].getTopAnchorPoint();
                    if (!isStraitConnector) {
                        CubicCurve2D conn = new CubicCurve2D.Double(start.x, start.y, start.x, start.y + 15, finish.x, finish.y - 15, finish.x, finish.y);
                        g2.draw(conn);
                        if (selected) {
                            conn = new CubicCurve2D.Double(start.x + 1, start.y, start.x + 1, start.y + 16, finish.x + 1, finish.y - 16, finish.x + 1, finish.y);
                            g2.draw(conn);
                        }
                    } else {
                        g.drawLine(start.x, start.y, finish.x, finish.y);
                        if (selected) g.drawLine(start.x + 1, start.y, finish.x + 1, finish.y);
                    }
                    if (selected) g.setColor(origColor);
                    g2.setComposite(composite);
                }
            }
        }
    }

    public static void main(String[] args) {
    }
}
