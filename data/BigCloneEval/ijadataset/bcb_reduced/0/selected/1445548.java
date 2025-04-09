package seevolution.trees;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A node in a Tree, this class encapsulates the structure and some methods to allow representing the tree in a TreePanel
 * @author Andres Esteban Marcos
 * @version 1.0
 */
public class TreeNode {

    private String name;

    private LinkedList<TreeNode> children;

    private TreeNode parent;

    private TreeEvent[] events;

    private int length;

    private static int HIGHLIGHT_BOUNDARY = 5;

    private static int DETECTION_BOUNDARY = 10;

    private static int MARKER_LENGTH = 20;

    private static int MARKER_WIDTH = 6;

    private static int POSITION_LENGTH = 15;

    private static int POSITION_WIDTH = 3;

    private int myx, startx;

    private int top, bottom;

    private int totalLength;

    private float xPart;

    private float scale;

    /**
	 * Creates an unnamed node
	 */
    public TreeNode() {
        this("");
    }

    /**
	 * Creates a node with the specified name
	 * @param name The name of the node
	 */
    public TreeNode(String name) {
        this.name = name;
        parent = null;
        children = new LinkedList();
        length = 0;
    }

    /**
	 * Returns one of the descendents of this node
	 * @param index The index that identifies the child
	 * @return The child
	 */
    public TreeNode getChild(int index) {
        return children.get(index);
    }

    /**
	 * Gets the number of children
	 * @return number of children
	 */
    public int getChildrenCount() {
        return children.size();
    }

    /**
	 * Returns the list of events contained in the branch that links this node with its parent
	 * @return The events
	 */
    public TreeEvent[] getEvents() {
        return events;
    }

    /**
	 * Returns the maximum length of this branch considering all its children
	 * @return The length
	 */
    public int getLength() {
        int max = 0;
        for (int i = 0; i < children.size(); i++) {
            int len = children.get(i).getLength();
            if (len > max) max = len;
        }
        return max + length;
    }

    /**
	 * Returns the name of this node
	 * @return The name
	 */
    public String getName() {
        return name;
    }

    /**
	 * Returns a reference to the parent TreeNode
	 * @return The parent
	 */
    public TreeNode getParent() {
        return parent;
    }

    /**
	 * Returns a list of all the nodes on the path from here to "end", or null if "end" is not in this branch
	 * @param end The destination of the path
	 * @return The list of nodes on the path
	 */
    public LinkedList<TreeNode> getPath(TreeNode end) {
        LinkedList<TreeNode> res = null;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == end) {
                res = new LinkedList();
                res.addLast(end);
                return res;
            }
        }
        for (int i = 0; i < children.size(); i++) if ((res = children.get(i).getPath(end)) != null) {
            res.addFirst(children.get(i));
            return res;
        }
        return null;
    }

    /**
	 * Returns the position of the tree that correspond to coordinates (x,y) on the TreePanel
	 * @param x The x coordinate on the TreePanel
	 * @param y The y coordinate on the TreePanel
	 * @return A TreePosition object identifying the position in the Tree
	 */
    public TreePosition getPosition(int x, int y) {
        if (Math.abs(x - myx) <= HIGHLIGHT_BOUNDARY && Math.abs(y - bottom) <= HIGHLIGHT_BOUNDARY) return new TreePosition(this, -1);
        if (y < top) return null;
        if (totalLength != 0) {
            if (Math.abs(x - myx) <= DETECTION_BOUNDARY && y >= top && y <= bottom - 4) {
                int intPosition = y - top > 0 ? y - top : 0;
                float realPosition = xPart + (float) intPosition / (float) totalLength;
                return new TreePosition(this, realPosition);
            } else if (Math.abs(y - top) <= DETECTION_BOUNDARY && ((x > startx && x < myx) || (x < startx && x > myx))) {
                int intPosition = Math.abs(x - startx);
                float realPosition = (float) intPosition / (float) totalLength;
                return new TreePosition(this, realPosition);
            }
        }
        TreePosition pos = null;
        for (int i = 0; i < children.size(); i++) {
            pos = children.get(i).getPosition(x, y);
            if (pos != null) return pos;
        }
        return null;
    }

    /**
	 * Changes the events associated with the branch joining this node and its parent
	 * @param events The new events
	 */
    public void setEvents(TreeEvent events[]) {
        this.events = events;
        if (events != null && events.length != 0) length = events.length;
    }

    /**
	 * Changes the parent of this node
	 * @param parent The new parent
	 */
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    /**
	 * Adds a new descendent to this node
	 * @param child The new child
	 */
    public void addChild(TreeNode child) {
        children.addLast(child);
        child.setParent(this);
    }

    /**
	 * Adds several descendents to this node
	 * @param childrenArray An array containing all the new children
	 */
    public void addChildren(TreeNode childrenArray[]) {
        for (int i = 0; i < childrenArray.length; i++) {
            children.addLast(childrenArray[i]);
            childrenArray[i].setParent(this);
        }
    }

    /**
	 * Used to check whether a node is contained in this branch or not
	 * @param node The searched node
	 * @return True if this node or one of its descendents is "node"
	 */
    public boolean containsNode(TreeNode node) {
        if (node == this) return true; else {
            boolean result = false;
            for (int i = 0; i < children.size(); i++) {
                result = children.get(i).containsNode(node);
                if (result == true) break;
            }
            return result;
        }
    }

    /**
	 * Paints the circle that identifies a highlighted node
	 * @param g The Graphics2D object on which the circle is painted
	 * @return
	 */
    public void highlightNode(Graphics2D g) {
        g.fillOval(myx - 6, bottom - 6, 12, 12);
    }

    /**
	 * Converts an index in the list of events on this branch to a position in the branch from 0f to 1f
	 * @param index The index in the list of events
	 * @return A value between 0 and 1 that represents the absolute position on the branch
	 */
    public float indexToPosition(int index) {
        return (float) index / (float) events.length;
    }

    /**
	 * Converts a position in the branch from 0f to 1f to an index in the list of events on this branch
	 * @param position A value between 0 and 1 that represents the position on the branch
	 * @return The closest index in the list of events associated to the position
	 */
    public int positionToIndex(float position) {
        return (int) Math.floor(position * events.length);
    }

    /**
	 * Paints this node and the branch joining it to its parent
	 * @param left The left margin used to paint this node and all its offspring
	 * @param right The right margin used to paint this node and all its offspring
	 * @param startx The starting horizontal position (usually the parent's x coordinate)
	 * @param top The starting vertical position (usually the parent's y coordinate)
	 * @param scale The scale used to convert branch length into pixel length
	 * @param g The graphics object on which the node is painted
	 */
    public void paint(int left, int right, int startx, int top, float scale, Graphics g) {
        myx = (left + right) / 2;
        this.startx = startx;
        int hLength = Math.abs(startx - myx);
        this.scale = scale;
        this.top = top;
        bottom = (int) (top + length * scale);
        totalLength = hLength + (bottom - top);
        xPart = (float) hLength / (float) totalLength;
        g.drawLine(myx, top, startx, top);
        g.drawLine(myx, top, myx, bottom);
        g.drawString(name, myx + 3, bottom - 3);
        g.fillOval(myx - 4, bottom - 4, 8, 8);
        int hgap;
        if (children.size() != 0) hgap = (right - left) / (children.size()); else hgap = right - left;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).paint(left + hgap * i, left + hgap * (i + 1), myx, bottom, scale, g);
        }
    }

    /**
	 * Paints the marker that identifies the position of the mouse on the tree
	 * @param position A value between 0 and 1 that marks where in the branch the marker should be painted
	 * @param g The graphics object on which the marker is painted
	 */
    public void paintMarker(float position, Graphics g) {
        if (position <= xPart) {
            int x = startx + (myx < startx ? -1 : 1) * (int) (totalLength * position);
            int y = top;
            g.drawRect(x - MARKER_WIDTH / 2, y - MARKER_LENGTH / 2, MARKER_WIDTH, MARKER_LENGTH);
        } else {
            int x = myx;
            int y = top + (int) (totalLength * (position - xPart));
            g.drawRect(x - MARKER_LENGTH / 2, y - MARKER_WIDTH / 2, MARKER_LENGTH, MARKER_WIDTH);
        }
    }

    /**
	 * Paints the rectangle that represents the current position on the event path
	 * @param position A value between 0 and 1 that marks where in the branch the position should be painted
	 * @param g The graphics object on which the position is painted.
	 */
    public void paintPosition(float position, Graphics2D g) {
        if (position <= xPart) {
            int x = startx + (myx < startx ? -1 : 1) * (int) (totalLength * position);
            int y = top;
            g.fillRect(x - POSITION_WIDTH / 2, y - POSITION_LENGTH / 2, POSITION_WIDTH, POSITION_LENGTH);
        } else {
            int x = myx;
            int y = top + (int) (totalLength * (position - xPart));
            g.fillRect(x - POSITION_LENGTH / 2, y - POSITION_WIDTH / 2, POSITION_LENGTH, POSITION_WIDTH);
        }
    }
}
