package AccordionTreeDrawer;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import javax.media.opengl.GL;
import AccordionDrawer.*;

/**
 * A class representing a node of a (phylognenetic) tree.
 * The tree that this node belongs to is of type Tree.
 * Nodes have fields that store a pre- and post-ordering. 
 *
 * A TreeNode has a list of children, a unique key, a leftmostleaf 
 * and a rightmost leaf
 *
 * @author  Tamara Munzner, Li Zhang, Yunhong Zhou
 * @version 
 * @see     Tree
 * @see     GridCell
 */
public class TreeNode extends Object implements CellGeom, Comparable {

    ArrayList children;

    /** key is unique for nodes in one tree */
    public int key;

    /** The GridCell that this node is attached to  */
    public GridCell cell;

    private int fontSize;

    Double bcnScore;

    int computedFrame;

    double midYPosition;

    public int getMin() {
        return key;
    }

    public int getMax() {
        return rightmostLeaf.key;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public boolean pick(int x, int y) {
        return false;
    }

    /**
     * Draws this node inside cell, the GridCell that it is attached
     * to, if cell is drawn large enough Prints the label of the node
     * if the GridCell is drawn large enough
     * @author   Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou
     * 
     * @param    col The color to draw this node in
     * @param    plane The plane to draw this node in
     *
     * @see      AccordionDrawer.GridCell
     */
    public void drawInCell(Color col, double plane) {
        for (int xy = 0; xy < 2; xy++) {
            if (getEdge(xy)) drawInCell(col, plane, xy == 0);
        }
    }

    public void drawInCell(ArrayList col, double plane) {
        if (cell.getDrawnFrame() >= cell.drawer.getFrameNum()) return;
        cell.setDrawnFrame(cell.drawer.getFrameNum());
        if (isLeaf()) ((AccordionTreeDrawer) cell.drawer).leafDrawCount++; else ((AccordionTreeDrawer) cell.drawer).internalDrawCount++;
        Color c = null;
        if (col != null && col.size() > 0) {
            c = (Color) col.get(0);
            plane = cell.drawer.hiliteplane - .004 * c.getAlpha();
        }
        drawInCell(c, plane);
    }

    public void setCell(GridCell c) {
        cell = c;
    }

    public GridCell getCell() {
        return cell;
    }

    public boolean getEdge(int xy) {
        if (xy == 0) return !isRoot(); else return !isLeaf();
    }

    /** implement Comparable interface - sort on key field */
    public int compareTo(Object o) {
        if (key == ((TreeNode) o).key) return 0; else if (key < ((TreeNode) o).key) return -1; else return 1;
    }

    public TreeNode parent;

    /** 
     * node name with default "". 
     * Most internal nodes have no name and all leaf nodes have a name 
     */
    String name = "";

    public String label = "";

    int height;

    /** weight is actually edge length for the edge immediately above the node */
    public float weight = 0.0f;

    /**
	 * Draw label of this TreeEdge at maximum size (intended for
	 * mouseover highlighting).
	 * 
	 * @author   Tamara Munzner
	 */
    public void drawLabelBig(int x, int y, int fontheight, boolean horiz) {
        AccordionTreeDrawer drawer = (AccordionTreeDrawer) getCell().drawer;
        if (!horiz || drawer.nogeoms || label.length() < 1) return;
        drawLabelBox(drawer.flashLabelBox, fontheight, true);
    }

    public TreeNode leftmostLeaf, rightmostLeaf;

    public int numberLeaves;

    public TreeNode preorderNext = null;

    public TreeNode posorderNext = null;

    public TreeNode() {
        children = new ArrayList(2);
        bcnScore = new Double(0.0);
    }

    public void drawInCell(Color col, double plane, boolean horiz) {
        boolean labelAtLeaves = false;
        int X = horiz ? 0 : 1;
        int Y = horiz ? 1 : 0;
        AccordionTreeDrawer atd = (AccordionTreeDrawer) cell.drawer;
        boolean isBase = false;
        if (col == null) {
            col = atd.objectColor;
            isBase = true;
        }
        if (atd.nogeoms) return;
        AccordionTreeDrawer.countDrawnFrame++;
        GL gl = (GL) atd.getGL();
        Color drawColor = setGLColor(atd, col);
        int thick = atd.getLineThickness();
        if (col != atd.getObjectColor() && !atd.isDoingFlash()) thick += 2;
        float[] posStart = new float[2], posEnd = new float[2];
        setPositions(posStart, posEnd, horiz);
        if (atd.takeSnapshot) {
            if ((atd.basePass && isBase) || (atd.groupPass && !isBase)) try {
                int cellMinPix[] = { atd.w2s(posStart[X], X), atd.w2s(posStart[Y], Y) };
                int cellMaxPix[] = { atd.w2s(posEnd[X], X), atd.w2s(posEnd[Y], Y) };
                atd.snapShotWriter.write(thick + " setlinewidth newpath " + cellMinPix[X] + " " + cellMinPix[Y] + " moveto " + cellMaxPix[X] + " " + cellMaxPix[Y] + " lineto " + "closepath " + "gsave " + drawColor.getRed() / 255f + " " + drawColor.getGreen() / 255f + " " + drawColor.getBlue() / 255f + " setrgbcolor " + "stroke grestore\n");
            } catch (IOException ioe) {
                System.out.println("Error: IOException while trying to write cell to file: " + atd.snapShotWriter.toString());
            }
        } else {
            atd.setColorGL(drawColor);
            gl.glLineWidth(thick);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3d(posStart[0], posStart[1], plane);
            gl.glVertex3d(posEnd[0], posEnd[1], plane);
            gl.glEnd();
        }
        if (horiz) {
            if (atd.takeSnapshot) {
                if ((atd.basePass && isBase) || (atd.groupPass && !isBase)) try {
                    int cellMaxPix[] = { atd.w2s(posEnd[X], X), atd.w2s(posEnd[Y], Y) };
                    atd.snapShotWriter.write("newpath " + (cellMaxPix[X] - thick) + " " + (cellMaxPix[Y] - thick) + " moveto " + (cellMaxPix[X] - thick) + " " + (cellMaxPix[Y] + thick) + " lineto " + (cellMaxPix[X] + thick) + " " + (cellMaxPix[Y] + thick) + " lineto " + (cellMaxPix[X] + thick) + " " + (cellMaxPix[Y] - thick) + " lineto " + "closepath " + "gsave " + drawColor.getRed() / 255f + " " + drawColor.getGreen() / 255f + " " + drawColor.getBlue() / 255f + " setrgbcolor " + "eofill grestore\n");
                } catch (IOException ioe) {
                    System.out.println("Error: IOException while trying to write cell to file: " + atd.snapShotWriter.toString());
                }
            } else {
                gl.glPointSize((float) (thick + 2.0f));
                gl.glBegin(GL.GL_POINTS);
                gl.glVertex3d(posEnd[0], posEnd[1], plane);
                gl.glEnd();
            }
        }
        if (horiz && atd.drawlabels && label != null && label.length() > 0 && !atd.basePass && !atd.groupPass) {
            int min = ((AccordionTreeDrawer) atd).minFontHeight;
            int max = ((AccordionTreeDrawer) atd).maxFontHeight;
            int mid = max;
            int fitsheight = min;
            LabelBox fits = makeLabelBox(min, -1, -1, posStart, posEnd, labelAtLeaves);
            if (!intersectLabelBox(fits)) {
                if (max != min) {
                    LabelBox maxBox = makeLabelBox(max, -1, -1, posStart, posEnd, labelAtLeaves);
                    if (!intersectLabelBox(maxBox)) {
                        fits = maxBox;
                        fitsheight = max;
                    } else while (min + 1 < max) {
                        mid = (int) ((min + max) / 2.0);
                        LabelBox lb = makeLabelBox(mid, -1, -1, posStart, posEnd, labelAtLeaves);
                        if (intersectLabelBox(lb)) max = mid; else {
                            fits = lb;
                            fitsheight = mid;
                            min = mid;
                        }
                    }
                }
                drawLabelBox(fits, fitsheight, false);
            }
        }
    }

    /**
	 * clean the node itself and the tree edge attached with it.
	 * @see TreeEdge.close
	 * @see Tree.close
	 *
	 */
    public void close() {
        children.clear();
        children = null;
        name = null;
        cell = null;
        parent = null;
        label = null;
        leftmostLeaf = null;
        rightmostLeaf = null;
        preorderNext = null;
        posorderNext = null;
        bcnScore = null;
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public TreeNode(String s, float w) {
        children = new ArrayList(2);
        name = new String(s);
        weight = w;
    }

    public TreeNode(String s) {
        children = new ArrayList(2);
        name = new String(s);
    }

    public void setName(String s) {
        name = s;
    }

    public int numberChildren() {
        return children.size();
    }

    public TreeNode getChild(int i) {
        if (i < children.size()) return (TreeNode) children.get(i); else return null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isRoot() {
        return (null == parent);
    }

    /** @return the total number of leaves decedant to this node */
    public int getLeafCount() {
        if (this == null) return 0; else if (isLeaf()) return 1; else {
            int sum = 0;
            for (int i = 0; i < numberChildren(); i++) sum += getChild(i).getLeafCount();
            return sum;
        }
    }

    public boolean equals(TreeNode n) {
        return (name.equals(n.name));
    }

    public void addChild(TreeNode n) {
        children.add(n);
        n.parent = this;
    }

    public TreeNode parent() {
        return parent;
    }

    public void setWeight(double w) {
        weight = (float) w;
    }

    public float getWeight() {
        return weight;
    }

    public TreeNode firstChild() {
        return (TreeNode) children.get(0);
    }

    public TreeNode lastChild() {
        return (TreeNode) children.get(children.size() - 1);
    }

    /** 
     * @return <code>true</code> if this is an ancestor of <code>that</code>
     * @param that Another TreeNode object 
     */
    public boolean isAncestorOf(TreeNode that) {
        if (leftmostLeaf.getLindex() > that.leftmostLeaf.getLindex() || rightmostLeaf.getLindex() < that.rightmostLeaf.getLindex()) return false;
        return true;
    }

    /**
     * Compute the lowest common ancestor between this node and <code>that</that>.
     * The two nodes must belong to the same tree.
     * @author   Li Zhang
     * 
     * @param    that A TreeNode in the Tree that this TreeNode belongs to
     * @return   the lowest common ancestor between this node and "that"
     */
    public TreeNode lca(TreeNode that) {
        if (that.isAncestorOf(this)) return that;
        TreeNode current = this;
        while (current != null) {
            if (current.isAncestorOf(that)) return current;
            current = current.parent;
        }
        return null;
    }

    /**
     * Compute the lowest common ancestor between this leaf and "that"
     * The two nodes must belong to the same tree and must be leaves
     * @author   Li Zhang
     * 
     * @param    that A TreeNode in the Tree that this TreeNode belongs to
     * @return   the lowest common ancestor between this leaf and <code>that</code>, null if one of the nodes is not a leaf 
     */
    public TreeNode leafLca(TreeNode that) {
        if (!isLeaf()) return null;
        if (!that.isLeaf()) return null;
        TreeNode current = this;
        int a = that.getLindex();
        if (getLindex() > that.getLindex()) {
            current = that;
            a = getLindex();
        }
        for (; current != null; current = current.parent) {
            if (current.rightmostLeaf.getLindex() >= a) return current;
        }
        return null;
    }

    public void print() {
        if (name != null) System.out.print("node name: " + name + "\t"); else System.out.print("node name null,\t");
        System.out.println("key: " + key);
    }

    public void printSubtree() {
        print();
        for (int i = 0; i < children.size(); i++) getChild(i).printSubtree();
    }

    public void setSubtreeExtremeLeaves() {
        if (isLeaf()) {
            leftmostLeaf = this;
            rightmostLeaf = this;
            return;
        }
        int i;
        for (i = 0; i < children.size(); i++) {
            getChild(i).setSubtreeExtremeLeaves();
        }
        leftmostLeaf = firstChild().leftmostLeaf;
        rightmostLeaf = lastChild().rightmostLeaf;
    }

    public void setExtremeLeaves() {
        if (isLeaf()) {
            leftmostLeaf = this;
            rightmostLeaf = this;
            return;
        }
        leftmostLeaf = firstChild().leftmostLeaf;
        rightmostLeaf = lastChild().rightmostLeaf;
    }

    public void linkNodesInPreorder() {
        if (isLeaf()) return;
        preorderNext = firstChild();
        for (int i = 0; i < numberChildren() - 1; i++) getChild(i).rightmostLeaf.preorderNext = getChild(i + 1);
    }

    public void linkNodesInPostorder() {
        if (isLeaf()) return;
        for (int i = 0; i < numberChildren() - 1; i++) getChild(i).posorderNext = getChild(i + 1).leftmostLeaf;
        lastChild().posorderNext = this;
    }

    /** set numberLeaves field for each subnode */
    int setSubtreeNumberLeaves() {
        numberLeaves = 0;
        if (isLeaf()) numberLeaves = 1; else for (int i = 0; i < children.size(); i++) numberLeaves += getChild(i).setSubtreeNumberLeaves();
        return numberLeaves;
    }

    public int setNumberLeaves() {
        numberLeaves = 0;
        if (isLeaf()) numberLeaves = 1; else for (int i = 0; i < children.size(); i++) numberLeaves += getChild(i).numberLeaves;
        return numberLeaves;
    }

    public String toString() {
        return name + "(" + key + " @ " + height + ")";
    }

    public void setFontSize(int fs) {
        fontSize = fs;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean isInteriorNode() {
        return children.size() > 0 ? true : false;
    }

    public void setBcnScore(float n) {
        bcnScore = new Double(n);
    }

    public Double getBcnScore() {
        return bcnScore;
    }

    public double getMidY() {
        if (computedFrame >= cell.drawer.getFrameNum()) return midYPosition;
        if (isLeaf()) {
            midYPosition = getSize(AccordionDrawer.Y) * 0.5 + cell.getMinSplitAbsolute(AccordionDrawer.Y);
        } else if (children.size() == 1) {
            midYPosition = ((TreeNode) children.get(0)).getMidY();
        } else {
            TreeNode child0 = (TreeNode) children.get(0);
            TreeNode childN = (TreeNode) children.get(children.size() - 1);
            midYPosition = (child0.cell.getMaxSplitAbsolute(AccordionDrawer.Y) + childN.cell.getMinSplitAbsolute(AccordionDrawer.Y)) / 2;
        }
        computedFrame = cell.drawer.getFrameNum();
        return midYPosition;
    }

    public double getMinY() {
        if (isLeaf()) return -1.0;
        return ((TreeNode) children.get(0)).getMidY();
    }

    public double getMaxY() {
        if (isLeaf()) return -1.0;
        return ((TreeNode) children.get(children.size() - 1)).getMidY();
    }

    public double getSize(int xy) {
        return cell.getMaxSplitAbsolute(xy) - cell.getMinSplitAbsolute(xy);
    }

    public TreeNode pickNode(double x, double y, double xFuzz, double yFuzz) {
        if (isNodePicked(x, y, xFuzz, yFuzz)) return this;
        if (!isLeaf() && x > cell.getMaxSplitAbsolute(AccordionDrawer.X) && xyInRange(y, 0.0, AccordionDrawer.Y)) return pickDescend(x, y, xFuzz, yFuzz);
        return null;
    }

    public boolean isNodePicked(double x, double y, double xFuzz, double yFuzz) {
        double min[] = { cell.getMinSplitAbsolute(AccordionDrawer.X), cell.getMinSplitAbsolute(AccordionDrawer.Y) };
        double max[] = { cell.getMaxSplitAbsolute(AccordionDrawer.X), cell.getMaxSplitAbsolute(AccordionDrawer.Y) };
        double midY = getMidY();
        return (x > max[AccordionDrawer.X] - xFuzz && x < max[AccordionDrawer.X] + xFuzz && y > min[AccordionDrawer.Y] - yFuzz && y < max[AccordionDrawer.Y] + yFuzz) || (x > min[AccordionDrawer.X] - xFuzz && x < max[AccordionDrawer.X] + xFuzz && y > midY - yFuzz && y < midY + yFuzz);
    }

    private boolean xyInRange(double value, double fuzz, int xy) {
        return cell.getMinSplitAbsolute(xy) - fuzz <= value && cell.getMaxSplitAbsolute(xy) + fuzz >= value;
    }

    public TreeNode pickDescend(double x, double y, double xFuzz, double yFuzz) {
        Stack pickingStack = new Stack();
        pickingStack.push(this);
        while (pickingStack.size() > 0) {
            TreeNode currRoot = (TreeNode) pickingStack.pop();
            if (currRoot.isNodePicked(x, y, xFuzz, yFuzz)) return currRoot;
            if (currRoot.isLeaf() || !currRoot.xyInRange(y, yFuzz, AccordionDrawer.Y) || currRoot.cell.getMinSplitAbsolute(AccordionDrawer.X) > x) {
                continue;
            }
            int minChild = 0;
            int maxChild = currRoot.children.size() - 1;
            int midChild = (minChild + maxChild + 1) / 2;
            TreeNode currChild = (TreeNode) (currRoot.children.get(midChild));
            while (minChild != maxChild && !currChild.xyInRange(y, 0.0, AccordionDrawer.Y)) {
                if (currChild.cell.getMinSplitAbsolute(AccordionDrawer.Y) > y) maxChild = midChild; else minChild = midChild;
                if (minChild + 1 == maxChild && midChild == minChild) midChild = maxChild; else midChild = (minChild + maxChild) / 2;
                currChild = (TreeNode) (currRoot.children.get(midChild));
            }
            if (currChild.isNodePicked(x, y, xFuzz, yFuzz)) {
                return currChild;
            }
            if (midChild > 0) pickingStack.push(currRoot.children.get(midChild - 1));
            if (midChild < currRoot.children.size() - 1) pickingStack.push(currRoot.children.get(midChild + 1));
            pickingStack.push(currChild);
        }
        return null;
    }

    public int getLindex() {
        if (cell == null) System.out.println("Error when finding Lindex for " + this);
        return ((StaticSplitLine) cell.getMaxLine(AccordionDrawer.Y)).getSplitIndex();
    }

    public void drawAscend(int frameNum, float plane) {
        if (cell.getDrawnFrame() < frameNum) {
            drawInCell(cell.drawer.getColorsForCellGeom(this), plane);
            TreeNode n = parent;
            while (n != null) {
                n.drawInCell(cell.drawer.getColorsForCellGeom(n), plane);
                n.cell.setDrawnFrame(frameNum);
                n = n.parent;
            }
        }
    }

    public void drawDescend(int frameNum, float plane, int min, int max, Tree tree) {
        drawInCell(cell.drawer.getColorsForCellGeom(this), plane);
        TreeNode currNode = this;
        int minChild = 0;
        int maxChild = currNode.children.size() - 1;
        int midChild = (minChild + maxChild) / 2;
        TreeNode minLeaf = tree.getLeaf(min + 1);
        TreeNode maxLeaf = tree.getLeaf(max);
        while (!currNode.isLeaf()) {
            TreeNode midNode = currNode.getChild(midChild);
            if (midNode.leftmostLeaf.key > maxLeaf.key) {
                maxChild = midChild - 1;
            } else if (midNode.rightmostLeaf.key < minLeaf.key) {
                minChild = midChild + 1;
            } else {
                midNode.drawInCell(cell.drawer.getColorsForCellGeom(midNode), plane);
                currNode = midNode;
                minChild = 0;
                maxChild = currNode.children.size() - 1;
            }
            midChild = (minChild + maxChild) / 2;
        }
    }

    public void drawInCell(double plane, boolean doFlash) {
    }

    /**
	 * Draws this TreeEdge inside the GridCell to which it is
	 * attached, and its label if appropriate.
	 *
	 * Find font size to use for drawing the label, by checking for
	 * occlusions. Don't draw it all if it's occluded even at the
	 * smallest size.
	 * 
	 * @author   Tamara Munzner, Li Zhang
	 * 
	 * @param    col The color to draw this node in
	 * @param    plane Z depth at which to draw label
	 *
	 * @see      AccordionDrawer.GridCell
	 */
    private Color setGLColor(AccordionTreeDrawer atd, Color col) {
        float rgbcol[] = new float[3];
        Color newrgb;
        if (atd.dimcolors || atd.dimbrite) {
            col.getRGBColorComponents(rgbcol);
            float hsbcol[] = Color.RGBtoHSB((int) (rgbcol[0] * 255), (int) (rgbcol[1] * 255), (int) (rgbcol[2] * 255), (new float[3]));
            float howdim;
            if (hsbcol[1] > 0f && atd.dimcolors) {
                InteractionBox b = atd.makeBox(this);
                if (null == b) System.out.println("interaction box null");
                howdim = (float) (b.getMax(AccordionDrawer.Y) - b.getMin(AccordionDrawer.Y)) / (float) atd.getWinMax(1);
                howdim = .5f + 40 * howdim;
                howdim = (howdim > 1.0) ? 1.0f : howdim;
                hsbcol[1] = (float) howdim;
            }
            if (atd.dimbrite && col == atd.getObjectColor()) {
                int treeheight = atd.tree.getHeight();
                howdim = (height - 1.0f) / (treeheight - 1.0f);
                hsbcol[2] = (float) (0.2 + howdim * .7);
            }
            newrgb = Color.getHSBColor(hsbcol[0], hsbcol[1], hsbcol[2]);
        } else {
            newrgb = col;
        }
        return newrgb;
    }

    public void setPositions(float[] start, float[] end, boolean horiz) {
        AccordionTreeDrawer atd = (AccordionTreeDrawer) cell.drawer;
        int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
        GridCell c = getCell();
        double[] minStuck = { atd.getSplitAxis(Y).getMinStuckValue(), atd.getSplitAxis(Y).getMinStuckValue() };
        double[] rangeSize = { atd.getSplitAxis(X).getMaxStuckValue() - minStuck[X], atd.getSplitAxis(Y).getMaxStuckValue() - minStuck[Y] };
        if (horiz) {
            start[X] = (float) c.getMinSplitAbsolute(X);
            end[X] = (float) c.getMaxSplitAbsolute(X);
            start[Y] = end[Y] = (float) getMidY();
        } else {
            start[X] = end[X] = (float) c.getMaxSplitAbsolute(X);
            start[Y] = (float) getMinY();
            end[Y] = (float) getMaxY();
        }
    }

    /** 
	     * Create a LabelBox for the given fontheight
	     * @param    x horizontal base location in screen/pixel coordinates
	     * @param    y vertical base location in screen/pixel coordinates
	     * @author Tamara Munzner
	     * @see	 AccordionDrawer.LabelBox
	     */
    protected LabelBox makeLabelBox(int fontheight, int x, int y, float[] start, float[] end, boolean labelAtLeaves) {
        AccordionTreeDrawer d = (AccordionTreeDrawer) getCell().drawer;
        int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
        int bottomLeft[] = new int[2];
        int topRight[] = new int[2];
        int bottomLeftBackground[] = new int[2];
        int startPix[] = { d.w2s(start[X], X), d.w2s(start[Y], Y) };
        int endPix[] = { d.w2s(end[X], X), d.w2s(end[Y], Y) };
        String name = label;
        int namewidth = AccordionDrawer.stringWidth(name, fontheight);
        int oldnamewidth = 0;
        int labelwidth = namewidth + 2;
        int numChars;
        int[] winSize = { d.getWinsize(X), d.getWinsize(Y) };
        int moveover = (isLeaf() || labelAtLeaves) ? labelwidth + d.labeloffset[X] : -d.labeloffset[X];
        if (labelAtLeaves) {
            startPix[X] = d.w2s(d.getSplitAxis(X).getMaxStuckValue(), X);
            endPix[X] = startPix[X] + labelwidth;
            moveover = d.labeloffset[X];
        }
        if (d.getLabelPosRight()) {
            topRight[X] = endPix[X] + moveover;
        } else topRight[X] = startPix[X] - d.labeloffset[X];
        if (!d.getLabelPosRight()) {
            topRight[Y] = startPix[Y] - d.labeloffset[Y] - fontheight;
        } else topRight[Y] = (isLeaf() || labelAtLeaves) ? endPix[Y] - (int) (fontheight / 2.0) : endPix[Y] - d.labeloffset[Y] - fontheight;
        int labelheight = fontheight + 2;
        bottomLeftBackground[X] = topRight[X] - labelwidth;
        bottomLeftBackground[Y] = topRight[Y] + labelheight;
        if (x >= 0 && y >= 0 && d.getLabelPopup()) {
            bottomLeftBackground[X] = x - (int) (labelwidth / 2.0);
            topRight[X] = bottomLeftBackground[X] + labelwidth;
            bottomLeftBackground[Y] = y - 5;
            topRight[Y] = bottomLeftBackground[Y] - labelheight;
        }
        if ((bottomLeftBackground[X]) < 0) {
            bottomLeftBackground[X] = 0;
            topRight[X] = bottomLeftBackground[X] + labelwidth;
        } else if (topRight[X] > winSize[X]) {
        }
        if (bottomLeftBackground[Y] > winSize[Y]) {
            bottomLeftBackground[Y] = winSize[Y];
            topRight[Y] = bottomLeftBackground[Y] - labelheight;
        } else if (topRight[Y] < 0) {
            topRight[Y] = 0;
            bottomLeftBackground[Y] = labelheight;
        }
        bottomLeft[Y] = bottomLeftBackground[Y] + d.labelbuffer[Y];
        bottomLeft[X] = bottomLeftBackground[X] - d.labelbuffer[X];
        return new LabelBox(bottomLeft, topRight, bottomLeftBackground, d.getFrameNum(), name);
    }

    /** 
	 * Occlusion check of LabelBox against array of all drawn labels. 
	 * {@link #drawnArray AccordionDrawer.drawnArray} is sorted in y -
	 * the skinny direction, to avoid work. We do a binary search to
	 * check for occlusions, so it's log in the number of pixels (with
	 * a very small constant).
	 * 
	 * @author  Tamara Munzner
	 * @see AccordionDrawer.LabelBox
	 */
    private boolean intersectLabelBox(LabelBox lb) {
        AccordionTreeDrawer d = (AccordionTreeDrawer) getCell().drawer;
        boolean intersects = false;
        ArrayList drawnLabels = d.getDrawnLabels();
        int listlength = drawnLabels.size();
        int high = (listlength == 1) ? listlength : listlength - 1;
        int low = 0;
        int current = -1;
        int prevcurrent = -2;
        int foundIndex = -1;
        LabelBox found;
        while ((low < high) && (!intersects) && (current <= high) && (foundIndex < 0)) {
            prevcurrent = current;
            current = (int) ((high + low) / 2.0);
            if (current == prevcurrent && low + 1 == high) {
                low = high;
                current = high;
            }
            if (current == listlength) current = current - 1;
            LabelBox prevlab = (LabelBox) drawnLabels.get(current);
            if (lb.compareBtoT(prevlab, low, high)) high = current; else if (prevlab.compareBtoT(lb, low, high)) low = current; else {
                found = prevlab;
                foundIndex = current;
                while (!intersects && prevlab.bottomBiggerThanTop(lb) && current >= 0) {
                    prevlab = (LabelBox) drawnLabels.get(current);
                    intersects = lb.intersectBoxes(prevlab);
                    current--;
                }
                current = foundIndex;
                while (!intersects && lb.bottomBiggerThanTop(prevlab) && current <= listlength - 1) {
                    prevlab = (LabelBox) drawnLabels.get(current);
                    intersects = lb.intersectBoxes(prevlab);
                    current++;
                }
            }
        }
        return intersects;
    }

    /** Draw a LabelBox. 
	     * @author	 Tamara Munzner, Li Zhang
	     * @param	 lb LabelBox to use
	     * @param    fontheight Size of font to use, in points/pixels.
	     * @param	 drawBig whether to draw maximum size ignoring occlusions
	     */
    private void drawLabelBox(LabelBox lb, int fontheight, boolean drawBig) {
        int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
        AccordionTreeDrawer d = (AccordionTreeDrawer) getCell().drawer;
        GL gl = (GL) d.getGL();
        ArrayList drawnLabels = d.getDrawnLabels();
        if (!drawBig) {
            drawnLabels.add(lb);
            Collections.sort(drawnLabels);
        }
        int[] pos = { lb.pos(0), lb.pos(1) };
        int[] topRightPos = { lb.topRightPos(0), lb.topRightPos(1) };
        double labelplane = d.getLabelplane();
        if (d.labeldrawback || drawBig) {
            float thecol[] = new float[3];
            if (drawBig) d.getLabelBackHiColor().getRGBColorComponents(thecol); else d.getLabelBackColor().getRGBColorComponents(thecol);
            if (!drawBig && d.labeltransp) {
                gl.glColor4f(thecol[0], thecol[1], thecol[2], .5f);
            } else {
                gl.glColor3f(thecol[0], thecol[1], thecol[2]);
            }
            gl.glBegin(GL.GL_POLYGON);
            gl.glVertex3d(d.s2w(pos[0], 0), d.s2w(pos[1], 1), labelplane);
            gl.glVertex3d(d.s2w(topRightPos[0], 0), d.s2w(pos[1], 1), labelplane);
            gl.glVertex3d(d.s2w(topRightPos[0], 0), d.s2w(topRightPos[1], 1), labelplane);
            gl.glVertex3d(d.s2w(pos[0], 0), d.s2w(topRightPos[1], 1), labelplane);
            gl.glEnd();
        }
        int descent = AccordionDrawer.getDescent(fontheight);
        String name = lb.getName();
        double b[] = { d.s2w(pos[X] + 1, X), d.s2w(pos[Y] - descent - 1, Y) };
        if (drawBig) {
            d.setColorGL(d.getLabelHiColor());
            d.drawText(b[X], b[Y], name, fontheight, d.getLabelColor(), labelplane, null);
        } else if (!d.labeldrawback) {
            d.drawText(b[X], b[Y], name, fontheight, d.getLabelColor(), labelplane, d.getLabelBackColor());
        } else d.drawText(b[X], b[Y], name, fontheight, d.getLabelColor(), labelplane, null);
    }

    public SplitLine getMinLine(int xy) {
        return null;
    }

    public SplitLine getMaxLine(int xy) {
        return null;
    }
}
