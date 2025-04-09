package projectviewer.vpt;

import java.util.Comparator;
import java.util.Collections;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *	Node implementation for the Virtual Project Tree. Keeps track of children
 *	and provides basic functionality for the nodes.
 *
 *	<p>Based on the <code>TreeNode</code> class from the VirtualProjectTree
 *	plugin by Shad Stafford.</p>
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: VPTNode.java 6023 2003-01-21 20:41:13Z vanza $
 */
public abstract class VPTNode extends DefaultMutableTreeNode {

    protected static final Color treeSelectionForeground = UIManager.getColor("Tree.selectionForeground");

    protected static final Color treeNoSelectionForeground = UIManager.getColor("Tree.textForeground");

    protected static final Color treeSelectionBackground = UIManager.getColor("Tree.selectionBackground");

    protected static final Color treeNoSelectionBackground = UIManager.getColor("Tree.textBackground");

    public static final VPTNodeType ROOT = new VPTNodeType("root");

    public static final VPTNodeType PROJECT = new VPTNodeType("project");

    public static final VPTNodeType DIRECTORY = new VPTNodeType("directory");

    public static final VPTNodeType FILE = new VPTNodeType("file");

    protected VPTNodeType nodeType;

    protected VPTNode vParent;

    protected String name;

    protected VPTNode(VPTNodeType type, String name) {
        this(type, name, type != FILE);
    }

    protected VPTNode(VPTNodeType type, String name, boolean allowsChildren) {
        this.nodeType = type;
        this.name = name;
        setAllowsChildren(allowsChildren);
    }

    /**
	 *	Sort the children list for this node using the default node comparator.
	 *	The trees containing the node are not notified of the update.
	 */
    public void sortChildren() {
        if (children != null && children.size() > 1) Collections.sort(children, new VPTNodeComparator());
    }

    /**
	 *	The "delete()" method should remove the resource from the the disk,
	 *	if applicable. This method does not call remove().
	 *
	 *	@return		Whether the deletion was successful or not.
	 */
    public boolean delete() {
        return false;
    }

    /** Returns true if this node is a file. */
    public boolean isFile() {
        return (nodeType == FILE);
    }

    /** Returns true if this node is a file. */
    public boolean isDirectory() {
        return (nodeType == DIRECTORY);
    }

    /** Returns true if this node is a file. */
    public boolean isProject() {
        return (nodeType == PROJECT);
    }

    /** Returns whether this node is a root node. */
    public boolean isRoot() {
        return (nodeType == ROOT);
    }

    /**
	 *	Tells if the resource is currently opened in jEdit. This only makes
	 *	sense for files, so the default just returns "false" and is
	 *	overridden in the file implementation.
	 */
    public boolean isOpened() {
        return false;
    }

    public void setParent(VPTNode parent) {
        super.setParent(parent);
        vParent = parent;
    }

    /**
	 *	Returns the name of this node. The name is the text that will appear
	 *	in the project tree.
	 */
    public String getName() {
        return name;
    }

    /**	Changes the name of the node. */
    public void setName(String name) {
        this.name = name;
    }

    /**
	 *	Returns whether the underlying resource can be written to. It makes
	 *	more sense for files and directories, for example, to check if it is
	 *	possible to delete them.
	 */
    public boolean canWrite() {
        return false;
    }

    /** Returns a string representation of the current node. */
    public String toString() {
        return "VPTNode [" + getName() + "]";
    }

    /**
	 *	This method should return whether it makes sense to "open" the node.
	 *	For example, for file nodes, it should be reasonable to open the file
	 *	in a jEdit buffer, so this method should return "true" and implement
	 *	{@link #open() open()} and {@link #close() close()} to execute
	 *	the opening and closing operations.
	 */
    public boolean canOpen() {
        return false;
    }

    /**
	 *	"Opens" the node. The default implementation does nothing. If a node can
	 *	be opened, it should implement the opening action in this method.
	 */
    public void open() {
    }

    /**
	 *	"Closes" the node. This should "undo" what was done by
	 *	{@link #open() open()}, normally.
	 */
    public void close() {
    }

    /**
	 *	Returns a String representing a "path" for this node. This can be any
	 *	arbitrary String, but the idea is to have the string represent some kind
	 *	of URL or file path. This makes more sense for nodes that deal with
	 *	files and directories, or even URL links.
	 */
    public abstract String getNodePath();

    /**
	 *	This method will only get called by nodes which are not recognized
	 *	by the default Comparator provided by the class VPTNodeComparator.
	 *	It's purpose is to be implemented by other types of node unknown
	 *	to the default node hierarchy, so that they can be sorted within
	 *	the tree. Since this is not going to be called on the classes
	 *	provided by the plugin, the return value does not matter. For other
	 *	implementing classes, the return value should be as the normal
	 *	"compareTo(Object)" method returns.
	 */
    public int compareToNode(VPTNode node) {
        return 1;
    }

    /**
	 *	Do a binary search with the goal of finding in what index of the child
	 *	array of this node the given child would be inserted to maintain order
	 *	according to the {@link VPTNodeComparator VPTNodeComparator} rules.
	 *
	 *	@param	child	The child to be inserted.
	 *	@return	The index where to put the child as to maintain the child array
	 *			in ascendant order.
	 */
    public int findIndexForChild(VPTNode child) {
        if (children == null || children.size() == 0) return 0;
        VPTNodeComparator c = new VPTNodeComparator();
        int b = 0, e = children.size(), i = e / 2;
        VPTNode n;
        while (e - b > 1) {
            n = (VPTNode) children.get(i);
            int comp = c.compare(child, n);
            if (comp < 0) {
                e = i;
            } else if (comp == 0) {
                i++;
                b = e = i;
            } else {
                b = i;
            }
            i = (e + b) / 2;
        }
        if (b == children.size()) return b;
        n = (VPTNode) children.get(b);
        return (c.compare(child, n) < 0 ? b : b + 1);
    }

    /** Returns the type of the node. */
    public VPTNodeType getNodeType() {
        return nodeType;
    }

    /**
	 *	Returns the icon to be shown on the tree next to the node name.
	 *
	 *	@param	expanded	If the node is currently expanded or not.
	 */
    public abstract Icon getIcon(boolean expanded);

    /**
	 *	Returns the node's foreground color.
	 *
	 *	@param	sel		If the node is currently selected.
	 */
    public Color getForegroundColor(boolean sel) {
        return (sel ? treeSelectionForeground : treeNoSelectionForeground);
    }

    /**
	 *	Returns the node's background color.
	 *
	 *	@param	sel		If the node is currently selected.
	 */
    public Color getBackgroundColor(boolean sel) {
        return (sel ? treeSelectionBackground : treeNoSelectionBackground);
    }

    /**
	 *	Class to provide a type-safe enumeration for node types.
	 */
    public static class VPTNodeType {

        private String name = null;

        public VPTNodeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
 	 *	Compares two VPTNode objects. It makes assumptions about the base nodes
	 *	provided by the plugin. If the nodes are not recognized by any of the
	 *	"isSomething" methods, the {@link VPTNode#compareToNode(VPTNode)
	 *	compareToNode(VPTNode)}	method is called.
	 */
    protected static class VPTNodeComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 == o2) return 0;
            VPTNode node1 = (VPTNode) o1;
            VPTNode node2 = (VPTNode) o2;
            if (node1.isFile()) {
                if (node2.isFile()) {
                    return node1.getName().compareTo(node2.getName());
                } else {
                    return 1;
                }
            } else if (node1.isDirectory()) {
                if (node2.isFile()) {
                    return -1;
                } else if (node2.isDirectory()) {
                    return node1.getName().compareTo(node2.getName());
                } else {
                    return 1;
                }
            } else if (node1.isProject()) {
                if (node2.isProject()) {
                    return node1.getName().compareTo(node2.getName());
                } else if (node2.isFile() || node2.isDirectory()) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (node1.isRoot()) {
                return -1;
            } else {
                return node1.compareToNode(node2);
            }
        }
    }
}
