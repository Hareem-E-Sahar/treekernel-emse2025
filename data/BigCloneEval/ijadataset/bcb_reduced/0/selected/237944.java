package tasklist;

import java.util.Comparator;
import javax.swing.tree.*;

/**
 * Sorts nodes added to this model by using a comparator.
 * Does not allow duplicate nodes to be added.
 */
public class SortableTreeModel extends DefaultTreeModel {

    private Comparator<DefaultMutableTreeNode> comparator;

    private static Comparator<DefaultMutableTreeNode> duplicateComparator = new TreeNodeStringComparator();

    /**
     * @param root Root node of the tree.
     * @param comparator A comparator used to sort nodes.
     */
    public SortableTreeModel(TreeNode root, Comparator<DefaultMutableTreeNode> comparator) {
        super(root);
        this.comparator = comparator;
    }

    /**
     * @param root Root node of the tree.
     * @param asksAllowsChildren a boolean, false if any node can have children, 
     * true if each node is asked to see if it can have children
     * @param comparator A comparator used to sort nodes
     */
    public SortableTreeModel(TreeNode root, boolean asksAllowsChildren, Comparator<DefaultMutableTreeNode> comparator) {
        super(root, asksAllowsChildren);
        this.comparator = comparator;
    }

    /**
     * @param child the node to insert
     * @param parent the node to insert the child into
     */
    public void insertNodeInto(MutableTreeNode child, MutableTreeNode parent) {
        if (child == null || parent == null) {
            return;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (duplicateComparator.compare((DefaultMutableTreeNode) child, (DefaultMutableTreeNode) parent.getChildAt(i)) == 0) {
                return;
            }
        }
        int index = findIndexFor(child, parent);
        super.insertNodeInto(child, parent, index);
    }

    /**
     * @param child the node to insert
     * @param parent the node to insert the child into
     * @param index not used, the actual index will be calculated 
     */
    public void insertNodeInto(MutableTreeNode child, MutableTreeNode parent, int index) {
        insertNodeInto(child, parent);
    }

    /**
     * @param child the node to insert
     * @param parent the node to insert the child into
     * @return the index of where the child should be inserted into the children
     * of the parent node to maintain sort order
     */
    private int findIndexFor(MutableTreeNode child, MutableTreeNode parent) {
        int cc = parent.getChildCount();
        if (cc == 0) {
            return 0;
        }
        if (cc == 1) {
            return comparator.compare((DefaultMutableTreeNode) child, (DefaultMutableTreeNode) parent.getChildAt(0)) <= 0 ? 0 : 1;
        }
        return findIndexFor(child, parent, 0, cc - 1);
    }

    /**
     * @param child the node to insert
     * @param parent the node to insert the child into
     * @param i1 start offset to being looking for the insertion point
     * @param i2 end offset to stop looking for the insertion point
     * @return the index of where the child should be inserted into the children
     * of the parent node to maintain sort order
     */
    private int findIndexFor(MutableTreeNode child, MutableTreeNode parent, int i1, int i2) {
        if (i1 == i2) {
            return comparator.compare((DefaultMutableTreeNode) child, (DefaultMutableTreeNode) parent.getChildAt(i1)) <= 0 ? i1 : i1 + 1;
        }
        int half = (i1 + i2) / 2;
        if (comparator.compare((DefaultMutableTreeNode) child, (DefaultMutableTreeNode) parent.getChildAt(half)) <= 0) {
            return findIndexFor(child, parent, i1, half);
        }
        return findIndexFor(child, parent, half + 1, i2);
    }
}
