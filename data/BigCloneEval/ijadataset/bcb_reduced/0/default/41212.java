import java.util.*;

public class TreeView {

    ArrayList<ArrayList<Integer>> nodeLevels;

    HashMap<Integer, TreeViewNode> treeNodes;

    int rootID;

    TreeModel model;

    int treeNodesDistance = 50;

    int treeLevelHeight = 25;

    int nodeShape;

    int totalTreeWidth;

    int totalTreeHeight;

    public static int BOX_SHAPE = 0;

    public static int OVAL_SHAPE = 1;

    public TreeView() {
        rootID = -1;
        model = null;
        nodeShape = OVAL_SHAPE;
        totalTreeWidth = 0;
        totalTreeHeight = 0;
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        treeNodes = new HashMap<Integer, TreeViewNode>();
    }

    public TreeView(TreeModel model) {
        rootID = -1;
        this.model = model;
        nodeShape = OVAL_SHAPE;
        totalTreeWidth = 0;
        totalTreeHeight = 0;
        treeNodesDistance = 50;
        treeLevelHeight = 25;
        createTreeStructure();
        calculateCoordinates();
    }

    public TreeView(TreeModel model, int treeNodesDistance, int treeLevelHeight) {
        rootID = -1;
        this.model = model;
        nodeShape = OVAL_SHAPE;
        totalTreeWidth = 0;
        totalTreeHeight = 0;
        this.treeNodesDistance = treeNodesDistance;
        this.treeLevelHeight = treeLevelHeight;
        createTreeStructure();
        calculateCoordinates();
    }

    private void createTreeStructure() {
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        treeNodes = new HashMap<Integer, TreeViewNode>();
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
            TreeViewNode childNode = new TreeViewNode(currentChild, viewNode.id, new ArrayList<Integer>(), viewNode.x, viewNode.y + treeLevelHeight);
            childNode.edgeTag = currentChild.parentEdgeLabel;
            viewNode.x += treeNodesDistance;
            treeNodes.put(childNode.id, childNode);
            viewNode.children.add(childNode.id);
            createSubtreeStructure(currentChild, childNode, model);
        }
    }

    public void createNodeLayers() {
        nodeLevels = new ArrayList<ArrayList<Integer>>();
        if (model.usesTerminals) {
            nodeLevels.add(model.terminals);
        }
        TreeViewNode root = treeNodes.get(rootID);
        ArrayList<Integer> rootLevel = new ArrayList<Integer>();
        rootLevel.add(rootID);
        nodeLevels.add(rootLevel);
        ArrayList<Integer> children = root.children;
        while (true) {
            ArrayList<Integer> grandchildren = new ArrayList<Integer>();
            for (int i = 0; i < children.size(); i++) {
                for (int j = 0; j < treeNodes.get(children.get(i)).children.size(); j++) {
                    int nodeID = treeNodes.get(children.get(i)).children.get(j);
                    if (model.usesTerminals) {
                        if (!model.terminals.contains(nodeID)) {
                            grandchildren.add(nodeID);
                        }
                    } else {
                        grandchildren.add(nodeID);
                    }
                }
            }
            nodeLevels.add(children);
            children = grandchildren;
            if (grandchildren.size() == 0) {
                break;
            }
        }
    }

    public void calculateCoordinates() {
        createNodeLayers();
        totalTreeWidth = 0;
        totalTreeHeight = (nodeLevels.size() + 1) * treeLevelHeight;
        if (!model.usesTerminals) {
            for (int i = nodeLevels.size() - 1; i >= 0; i--) {
                ArrayList<Integer> nodes = nodeLevels.get(i);
                for (int j = 0; j < nodes.size(); j++) {
                    TreeViewNode node = treeNodes.get(nodes.get(j));
                    if (node.children.size() > 0) {
                        node.subTreeWidth = collectWidths(node.children);
                    } else {
                        node.subTreeWidth = 1;
                    }
                }
            }
            treeNodes.get(rootID).x = treeNodes.get(rootID).subTreeWidth * treeNodesDistance / 2;
            for (int i = 0; i < nodeLevels.size(); i++) {
                ArrayList<Integer> nodes = nodeLevels.get(i);
                int xOffset = 100;
                int parent = -1;
                for (int j = 0; j < nodes.size(); j++) {
                    int subtreeWidth = treeNodes.get(nodes.get(j)).subTreeWidth * treeNodesDistance;
                    xOffset += subtreeWidth;
                    if (i > 0 && treeNodes.get(nodes.get(j)).parent != parent) {
                        parent = treeNodes.get(nodes.get(j)).parent;
                        xOffset = (int) (treeNodes.get(parent).x + treeNodes.get(parent).subTreeWidth * ((double) (treeNodes.get(nodes.get(j)).subTreeWidth) / treeNodes.get(parent).subTreeWidth - 0.5) * treeNodesDistance);
                    }
                    if (i > 0) {
                        treeNodes.get(nodes.get(j)).x = xOffset - subtreeWidth / 2;
                    }
                }
                if (nodes.size() > 0 && treeNodes.get(nodes.get(nodes.size() - 1)).x + treeNodesDistance > totalTreeWidth) {
                    totalTreeWidth = treeNodes.get(nodes.get(nodes.size() - 1)).x + treeNodesDistance;
                }
            }
        } else {
            ArrayList<Integer> terminals = nodeLevels.get(0);
            int xpos = 100;
            for (int t : terminals) {
                treeNodes.get(t).y = nodeLevels.size() * treeLevelHeight;
                treeNodes.get(t).x = xpos;
                xpos += treeNodesDistance;
            }
            totalTreeWidth = terminals.size() * (treeNodesDistance + 2);
            for (int j = nodeLevels.size() - 1; j > 0; j--) {
                for (int n : nodeLevels.get(j)) {
                    int minX = 1000;
                    int maxX = 0;
                    for (int c : treeNodes.get(n).children) {
                        int newX = treeNodes.get(c).x;
                        if (newX < minX) minX = newX;
                        if (newX > maxX) maxX = newX;
                    }
                    treeNodes.get(n).x = (minX + maxX) / 2;
                }
            }
            if (model.usesPreTerminals) {
                for (int t : terminals) {
                    treeNodes.get(treeNodes.get(t).parent).y = treeNodes.get(t).y - treeLevelHeight;
                }
            }
        }
    }

    private int collectWidths(ArrayList<Integer> children) {
        int sum = 0;
        for (int i = 0; i < children.size(); i++) {
            sum += treeNodes.get(children.get(i)).subTreeWidth;
        }
        return sum;
    }

    public String showLevels() {
        String levelString = "";
        for (ArrayList<Integer> nodeLevel : nodeLevels) {
            levelString += nodeLevel + "\n";
        }
        return levelString;
    }
}
