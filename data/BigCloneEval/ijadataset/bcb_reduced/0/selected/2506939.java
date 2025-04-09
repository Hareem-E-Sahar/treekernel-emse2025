package org.snipsnap.graph.dimensions.pic;

import java.util.*;
import org.snipsnap.graph.context.*;
import org.snipsnap.graph.dimensions.*;
import org.snipsnap.graph.dimensions.rec.*;
import org.snipsnap.graph.graph.*;

public class ContractRecsForVertical {

    private Tree tree;

    private Dim[] recDim;

    private PicInfo picInfo;

    private int last;

    private int current;

    private boolean visited = false;

    private TreeNode parentNode;

    private int counter;

    private int f = 2;

    private int vLimit;

    public ContractRecsForVertical(Tree tree, RendererContext context) {
        this.tree = tree;
        this.picInfo = ((PicInfo) context.getPicInfo());
        this.vLimit = picInfo.getVLimit();
    }

    public void contractGraph() {
        TreeNode root = (TreeNode) tree.getRoot();
        int numberOfRows = tree.getDepth();
        recDim = new ConverterForTree(tree).convertNodeDimToRecDim(tree.getRoot(), 1);
        counter = vLimit;
        iterateForNoChildren(root, 1);
        for (int i = numberOfRows; i > 1; i--) {
            iterateForParents(root, 1);
            counter--;
        }
    }

    public void iterateForNoChildren(TreeNode treeNode, int row) {
        if (row == 2) f = 2;
        ArrayList nodelist = treeNode.getChildrenList();
        Iterator iterator = nodelist.iterator();
        while (iterator.hasNext()) {
            TreeNode node = (TreeNode) iterator.next();
            if ((row + 1) != vLimit && node.getChildrenList().size() != 0) {
                iterateForNoChildren(node, row + 1);
            } else {
                if ((row + 1) <= vLimit) {
                    current = node.getX();
                    if (visited == true && current > last) {
                        int diff = current - last - picInfo.getDSameRow() * f;
                        int move = current - diff;
                        node.setX(move);
                        if ((row + 1) == vLimit) {
                            for (int i = 0; i < node.getChildrenList().size(); i++) {
                                TreeNode nd = (TreeNode) node.getChildrenList().get(i);
                                nd.setX(move);
                            }
                        }
                    }
                    last = node.getX() + recDim[row + 1].getWidth();
                    if (visited == false) visited = true;
                    f = 1;
                }
            }
        }
    }

    public void iterateForParents(TreeNode root, int row) {
        ArrayList nodelist = root.getChildrenList();
        Iterator iterator = nodelist.iterator();
        while (iterator.hasNext()) {
            TreeNode node = (TreeNode) iterator.next();
            if ((row + 1) != counter) {
                iterateForParents(node, row + 1);
            } else {
                if (!(node.getParent().equals(parentNode))) {
                    parentNode = node.getParent();
                    setParentInMiddleOfChildren(parentNode, row);
                }
            }
        }
    }

    private void setParentInMiddleOfChildren(TreeNode node, int row) {
        int childrenSize = node.getChildrenList().size();
        int coord_firstChild = 0;
        int coord_lastChild = 0;
        int middle = 0;
        coord_firstChild = ((TreeNode) node.getChildrenList().get(0)).getX() + recDim[row + 1].getWidth();
        coord_lastChild = ((TreeNode) node.getChildrenList().get(childrenSize - 1)).getX();
        middle = (coord_lastChild + coord_firstChild) / 2;
        node.setX(middle - recDim[row].getWidth() / 2);
    }
}
