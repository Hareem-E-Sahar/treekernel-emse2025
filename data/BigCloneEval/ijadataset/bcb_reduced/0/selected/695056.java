package tralesld.visual.tree;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.util.*;
import tralesld.struct.tree.*;

public class TreeView {

    protected ArrayList<ArrayList<Integer>> nodeLevels;

    public HashMap<Integer, TreeViewNode> treeNodes;

    public int rootID;

    public boolean usesTerminals;

    TreeModel model;

    protected int treeNodesDistance = 50;

    protected int treeLevelHeight = 25;

    public int nodeShape;

    private int totalTreeWidth;

    private int totalTreeHeight;

    private int selectionRadius;

    private int selectedNode;

    private double zoomFactor = 1.0;

    private int fontSize = 12;

    protected HashSet<Integer> collapsedNodes;

    protected HashSet<Integer> invisibleNodes;

    protected HashSet<Integer> markedNodes;

    protected HashMap<Integer, Integer> indentations;

    public static int BOX_SHAPE = 0;

    public static int OVAL_SHAPE = 1;

    public TreeView() {
        this(null, 150, 30);
    }

    public TreeView(TreeModel model) {
        this(model, 100, 30);
    }

    public TreeView(TreeModel model, int treeNodesDistance, int treeLevelHeight) {
        rootID = -1;
        selectedNode = -1;
        nodeShape = BOX_SHAPE;
        setTotalTreeWidth(0);
        setTotalTreeHeight(0);
        this.treeNodesDistance = treeNodesDistance;
        this.treeLevelHeight = treeLevelHeight;
        this.selectionRadius = 50;
        this.usesTerminals = false;
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        treeNodes = new HashMap<Integer, TreeViewNode>();
        collapsedNodes = new HashSet<Integer>();
        invisibleNodes = new HashSet<Integer>();
        markedNodes = new HashSet<Integer>();
        indentations = new HashMap<Integer, Integer>();
        if (model != null) {
            this.model = model;
            createTreeStructure();
            calculateCoordinates();
        }
    }

    public void generateNode(int i, String tag) {
        TreeViewNode node = new TreeViewNode(i, tag);
        treeNodes.put(i, node);
    }

    public void addChild(int parent, int child) {
        List<Integer> childList = treeNodes.get(parent).getChildList();
        childList.add(child);
        int oldParent = treeNodes.get(child).getParent();
        if (oldParent != -1) {
            treeNodes.get(oldParent).getChildList().remove((Object) child);
        }
        treeNodes.get(child).setParent(parent);
    }

    public int getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(int selectedNode) {
        this.selectedNode = selectedNode;
    }

    public int getTreeNodesDistance() {
        return treeNodesDistance;
    }

    public void setTreeNodesDistance(int treeNodesDistance) {
        this.treeNodesDistance = treeNodesDistance;
    }

    public int getTreeLevelHeight() {
        return treeLevelHeight;
    }

    public void setTreeLevelHeight(int treeLevelHeight) {
        this.treeLevelHeight = treeLevelHeight;
    }

    public int getSelectionRadius() {
        return selectionRadius;
    }

    public HashMap<Integer, Integer> getIndentations() {
        return indentations;
    }

    public void setIndentations(HashMap<Integer, Integer> indentations) {
        this.indentations = indentations;
    }

    public int getIndent(int nodeID) {
        if (indentations.get(nodeID) == null) return 0; else return indentations.get(nodeID);
    }

    public void setSelectionRadius(int selectionRadius) {
        this.selectionRadius = selectionRadius;
    }

    private void createTreeStructure() {
        if (model.root != -1) {
            TreeModelNode rootModel = model.nodes.get(model.root);
            TreeViewNode root = new TreeViewNode(rootModel, -1, new ArrayList<Integer>(), 100, 50);
            root.edgeTag = rootModel.parentEdgeLabel;
            treeNodes.put(root.id, root);
            rootID = root.id;
            createSubtreeStructure(rootModel, root, model);
        }
    }

    private void createSubtreeStructure(TreeModelNode modelNode, TreeViewNode viewNode, TreeModel model) {
        for (int i = 0; i < modelNode.children.size(); i++) {
            TreeModelNode currentChild = model.nodes.get(modelNode.children.get(i));
            TreeViewNode childNode = new TreeViewNode(currentChild, viewNode.id, new ArrayList<Integer>(), viewNode.x, 50);
            childNode.edgeTag = currentChild.parentEdgeLabel;
            viewNode.x += treeNodesDistance * zoomFactor;
            treeNodes.put(childNode.id, childNode);
            viewNode.children.add(childNode.id);
            createSubtreeStructure(currentChild, childNode, model);
        }
    }

    public void createNodeLayers() {
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        if (model != null && model.usesTerminals) {
            getNodeLevels().add(model.terminals);
        }
        TreeViewNode root = treeNodes.get(rootID);
        ArrayList<Integer> rootLevel = new ArrayList<Integer>();
        rootLevel.add(rootID);
        getNodeLevels().add(rootLevel);
        ArrayList<Integer> children = root.children;
        while (true) {
            ArrayList<Integer> grandchildren = new ArrayList<Integer>();
            for (int i = 0; i < children.size(); i++) {
                for (int j = 0; j < treeNodes.get(children.get(i)).children.size(); j++) {
                    int nodeID = treeNodes.get(children.get(i)).children.get(j);
                    if (model != null && model.usesTerminals) {
                        if (!model.terminals.contains(nodeID)) {
                            grandchildren.add(nodeID);
                        }
                    } else {
                        if (!invisibleNodes.contains(nodeID)) {
                            grandchildren.add(nodeID);
                        } else {
                            grandchildren.addAll(getVisibleChildren(nodeID));
                        }
                    }
                }
            }
            getNodeLevels().add(children);
            children = grandchildren;
            if (grandchildren.size() == 0) {
                break;
            }
        }
    }

    private ArrayList<Integer> getVisibleChildren(int nodeID) {
        ArrayList<Integer> visibleChildren = new ArrayList<Integer>();
        for (int childID : treeNodes.get(nodeID).children) {
            if (invisibleNodes.contains(childID)) {
                visibleChildren.addAll(getVisibleChildren(childID));
            } else {
                visibleChildren.add(childID);
            }
        }
        return visibleChildren;
    }

    public void calculateCoordinates() {
        createNodeLayers();
        setTotalTreeWidth(0);
        setTotalTreeHeight((int) ((getNodeLevels().size() + 2) * treeLevelHeight * zoomFactor));
        if (!usesTerminals) {
            for (int i = getNodeLevels().size() - 1; i >= 0; i--) {
                ArrayList<Integer> nodes = getNodeLevels().get(i);
                for (int j = 0; j < nodes.size(); j++) {
                    TreeViewNode node = treeNodes.get(nodes.get(j));
                    ArrayList<Integer> children = getVisibleVirtualChildren(nodes.get(j));
                    node.subTreeWidth = constructWidthVector(children);
                }
            }
            treeNodes.get(rootID).x = (int) (treeNodes.get(rootID).subTreeWidth.maximumLeftDistance() * treeNodesDistance / 2 * zoomFactor);
            for (int i = 0; i < getNodeLevels().size(); i++) {
                ArrayList<Integer> nodes = getNodeLevels().get(i);
                int xOffset = 0;
                if (nodes.size() > 0) xOffset = (int) (treeNodes.get(nodes.get(0)).subTreeWidth.maximumLeftDistance() * treeNodesDistance / 2.0 * zoomFactor);
                int parent = -1;
                WidthVector subtreeWidth = new WidthVector();
                int numNodes = nodes.size();
                WidthVector lastSubtreeWidth;
                for (int j = 0; j < numNodes; j++) {
                    lastSubtreeWidth = subtreeWidth;
                    subtreeWidth = treeNodes.get(nodes.get(j)).subTreeWidth;
                    xOffset += WidthVector.computeNecessaryDistance(lastSubtreeWidth, subtreeWidth) * treeNodesDistance / 2 * zoomFactor;
                    int newParent = getVisibleParent(nodes.get(j));
                    if (i > 0 && newParent != parent) {
                        parent = newParent;
                        xOffset = (int) ((treeNodes.get(parent).x - (treeNodes.get(parent).subTreeWidth.start.get(1) * 0.5 - 0.5) * treeNodesDistance));
                    }
                    if (i > 0) {
                        treeNodes.get(nodes.get(j)).x = xOffset;
                    }
                    treeNodes.get(nodes.get(j)).y = (int) (treeLevelHeight * zoomFactor) * i + 50;
                }
                if (nodes.size() > 0 && treeNodes.get(nodes.get(nodes.size() - 1)).x + (int) (treeNodesDistance * zoomFactor) > getTotalTreeWidth()) {
                    setTotalTreeWidth(treeNodes.get(nodes.get(nodes.size() - 1)).x + (int) (treeNodesDistance * zoomFactor));
                }
            }
        } else {
            ArrayList<Integer> terminals = getNodeLevels().get(0);
            int xpos = (int) (100 * zoomFactor);
            for (int t : terminals) {
                treeNodes.get(t).y = (int) (getNodeLevels().size() * treeLevelHeight * zoomFactor);
                treeNodes.get(t).x = xpos;
                xpos += treeNodesDistance * zoomFactor;
            }
            setTotalTreeWidth(terminals.size() * (int) ((treeNodesDistance + 2) * zoomFactor));
            for (int j = getNodeLevels().size() - 1; j > 0; j--) {
                for (int n : getNodeLevels().get(j)) {
                    int minX = Integer.MAX_VALUE;
                    int maxX = 0;
                    for (int c : treeNodes.get(n).children) {
                        int newX = treeNodes.get(c).x;
                        if (newX < minX) minX = newX;
                        if (newX > maxX) maxX = newX;
                    }
                    treeNodes.get(n).y = 50 + (int) ((j - 1) * treeLevelHeight * zoomFactor);
                    treeNodes.get(n).x = (minX + maxX) / 2;
                }
            }
            if (model != null && model.usesPreTerminals) {
                for (int t : terminals) {
                    treeNodes.get(treeNodes.get(t).getParent()).y = treeNodes.get(t).y - (int) (treeLevelHeight * zoomFactor);
                }
            }
        }
    }

    private int getVisibleParent(int nodeID) {
        int parent = treeNodes.get(nodeID).getParent();
        while (invisibleNodes.contains(parent)) {
            parent = treeNodes.get(parent).getParent();
        }
        return parent;
    }

    private ArrayList<Integer> getVisibleVirtualChildren(int nodeID) {
        ArrayList<Integer> descendants = new ArrayList<Integer>();
        descendants.addAll(treeNodes.get(nodeID).children);
        for (int i = 0; i < descendants.size(); i++) {
            if (invisibleNodes.contains(descendants.get(i))) {
                descendants.addAll(treeNodes.get(descendants.remove(i)).children);
                i--;
            }
        }
        return descendants;
    }

    private WidthVector constructWidthVector(ArrayList<Integer> children) {
        if (children.size() > 0) {
            WidthVector sum = treeNodes.get(children.get(0)).subTreeWidth.copy();
            for (int i = 1; i < children.size(); i++) {
                sum = WidthVector.adjoin(sum, treeNodes.get(children.get(i)).subTreeWidth);
            }
            sum.start.add(0, 1);
            sum.end.add(0, 1);
            return sum;
        }
        return new WidthVector();
    }

    public String showLevels() {
        String levelString = "";
        for (ArrayList<Integer> nodeLevel : getNodeLevels()) {
            levelString += nodeLevel + "\n";
        }
        return levelString;
    }

    public void increaseVerticalNodeDistance() {
        treeLevelHeight *= 1.25;
        calculateCoordinates();
    }

    public void decreaseVerticalNodeDistance() {
        treeLevelHeight *= 0.8;
        calculateCoordinates();
    }

    public void increaseHorizontalNodeDistance() {
        treeNodesDistance *= 1.25;
        calculateCoordinates();
    }

    public void decreaseHorizontalNodeDistance() {
        treeNodesDistance *= 0.8;
        calculateCoordinates();
    }

    public void setTotalTreeWidth(int totalTreeWidth) {
        this.totalTreeWidth = totalTreeWidth;
    }

    public int getTotalTreeWidth() {
        return totalTreeWidth;
    }

    public void setTotalTreeHeight(int totalTreeHeight) {
        this.totalTreeHeight = totalTreeHeight;
    }

    public int getTotalTreeHeight() {
        return totalTreeHeight;
    }

    public ArrayList<ArrayList<Integer>> getNodeLevels() {
        return nodeLevels;
    }

    public void collapseNode(int i) {
        collapsedNodes.add(i);
        createNodeLayers();
        calculateCoordinates();
    }

    public void expandNode(int i) {
        if (collapsedNodes.contains(i)) {
            collapsedNodes.remove(i);
        }
        createNodeLayers();
        calculateCoordinates();
    }

    public void toggleNode(int i) {
        if (collapsedNodes.contains(i)) {
            collapsedNodes.remove(i);
        } else {
            collapsedNodes.add(i);
        }
        createNodeLayers();
        calculateCoordinates();
    }

    public void collapseAllNodes() {
        for (int i = 0; i < treeNodes.size(); i++) {
            collapsedNodes.add(i);
        }
        calculateCoordinates();
    }

    public void expandAllNodes() {
        collapsedNodes.clear();
        calculateCoordinates();
    }

    public Color getNodeColor(int nodeID) {
        TreeViewNode n = treeNodes.get(nodeID);
        if (n == null) return null;
        return n.color;
    }

    public void zoomIn() {
        if (fontSize < 30) {
            zoomFactor *= ((fontSize + 1.0) / fontSize);
            fontSize++;
            calculateCoordinates();
        }
    }

    public void zoomOut() {
        if (fontSize > 4) {
            zoomFactor *= ((fontSize - 1.0) / fontSize);
            fontSize--;
            calculateCoordinates();
        }
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSizeAndZoomFactor(int fontSize) {
        this.fontSize = fontSize;
        this.zoomFactor = fontSize / 12.0;
        calculateCoordinates();
    }

    public int getNodeAtCoordinates(int x, int y, TreeViewPanel panel) {
        int rowID = (int) ((y - 30) / (treeLevelHeight * zoomFactor));
        if (rowID >= nodeLevels.size()) return -1;
        int currentX = 0;
        ArrayList<Integer> row = nodeLevels.get(rowID);
        int currentMaxElement = row.size();
        int currentMinElement = 0;
        while (currentMaxElement > currentMinElement) {
            int newElement = row.get((currentMaxElement + currentMinElement) / 2);
            currentX = treeNodes.get(newElement).x;
            if (x < currentX - treeNodesDistance / 2) {
                currentMaxElement = (int) Math.floor((currentMaxElement + currentMinElement) / 2.0);
            } else if (x > currentX + treeNodesDistance / 2) {
                currentMinElement = (int) Math.ceil((currentMaxElement + currentMinElement) / 2.0);
            } else {
                Rectangle candRect = panel.getNodeCoordinates(newElement);
                if (candRect.contains(x, y)) {
                    return newElement;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public HashSet<Integer> getCollapsedNodes() {
        return collapsedNodes;
    }

    public void setCollapsedNodes(HashSet<Integer> collapsedNodes) {
        this.collapsedNodes = collapsedNodes;
    }

    public HashSet<Integer> getInvisibleNodes() {
        return invisibleNodes;
    }

    public void setInvisibleNodes(HashSet<Integer> invisibleNodes) {
        this.invisibleNodes = invisibleNodes;
    }

    public HashSet<Integer> getMarkedNodes() {
        return markedNodes;
    }

    public void setMarkedNodes(HashSet<Integer> markedNodes) {
        this.markedNodes = markedNodes;
    }

    public void showXCoordDebugOutput() {
        for (int nodeID : treeNodes.keySet()) {
            System.err.println("Node " + nodeID + " with x=" + treeNodes.get(nodeID).x + " and subtreeWidth=" + treeNodes.get(nodeID).subTreeWidth);
        }
    }
}
