package org.sinaxe.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * PackerLayout is used to lay out widget components.
 *
 * @version     1.3, 03/10/02
 * @author      Joseph Coffland
 *              Original programming: Daeron Meyer
 *
 * Based heavily on the work of John Ousterhout, creator of the Tk Toolkit.
 *
 */
public class PackerLayout implements LayoutManager {

    Hashtable compinfo;

    PackTable firstcomp, lastcomp;

    public static final String initText = "PackerLayout (c) 1995 by Daeron Meyer\n" + "Optimized and enhanced 2001 by Joe Coffland\n";

    static final int ANCHOR_N = 0;

    static final int ANCHOR_NE = 1;

    static final int ANCHOR_E = 2;

    static final int ANCHOR_SE = 3;

    static final int ANCHOR_S = 4;

    static final int ANCHOR_SW = 5;

    static final int ANCHOR_W = 6;

    static final int ANCHOR_NW = 7;

    static final int ANCHOR_CENTER = 8;

    static final int ISIDE_TOP = 0;

    static final int ISIDE_BOTTOM = 1;

    static final int ISIDE_LEFT = 2;

    static final int ISIDE_RIGHT = 3;

    static final int debug = 0;

    private class PackTable {

        static final String F_ANCHOR = "anchor";

        static final String F_EXPAND = "expand";

        static final String F_FILL = "fill";

        static final String F_FILLX = "fillx";

        static final String F_FILLY = "filly";

        static final String F_IPADX = "ipadx";

        static final String F_IPADY = "ipady";

        static final String F_PADX = "padx";

        static final String F_PADY = "pady";

        static final String F_SIDE = "side";

        static final String ANCH_TOK_N = "n";

        static final String ANCH_TOK_NE = "ne";

        static final String ANCH_TOK_E = "e";

        static final String ANCH_TOK_SE = "se";

        static final String ANCH_TOK_S = "s";

        static final String ANCH_TOK_SW = "sw";

        static final String ANCH_TOK_W = "w";

        static final String ANCH_TOK_NW = "nw";

        static final String ANCH_TOK_CENTER = "center";

        static final String EXPAND_TRUE = "true";

        static final String EXPAND_FALSE = "false";

        static final String EXPAND_YES = "1";

        static final String EXPAND_NO = "0";

        static final String FILL_NONE = "none";

        static final String FILL_X = "x";

        static final String FILL_Y = "y";

        static final String FILL_BOTH = "both";

        static final String SIDE_TOP = "top";

        static final String SIDE_BOTTOM = "bottom";

        static final String SIDE_LEFT = "left";

        static final String SIDE_RIGHT = "right";

        static final String WIDTH = "width";

        static final String HEIGHT = "height";

        static final String CHAR_SEMI = ";";

        static final String CHAR_EQUAL = "=";

        static final String CHAR_ALL = CHAR_SEMI + CHAR_EQUAL;

        static final String DEBUG = "debug";

        public int anchor = ANCHOR_CENTER;

        public boolean expand = false;

        public boolean fillx = false;

        public boolean filly = false;

        public int ipadx = 0;

        public int ipady = 0;

        public int pady = 0;

        public int padx = 0;

        public int side = ISIDE_TOP;

        public PackTable next = null;

        public PackTable prev = null;

        public Component comp = null;

        public int width = -1;

        public int height = -1;

        public int debug = 0;

        PackTable(Component comp, String parseStr) {
            this.comp = comp;
            parse(parseStr);
        }

        public void parse(String parseStr) throws NoSuchElementException {
            String tok;
            StringTokenizer st = new StringTokenizer(parseStr, CHAR_ALL, true);
            while (st.hasMoreTokens()) {
                tok = st.nextToken();
                if (!st.nextToken().equals(CHAR_EQUAL)) throw new NoSuchElementException();
                String val = st.nextToken();
                if (tok.equals(F_ANCHOR)) {
                    if (val.equals(ANCH_TOK_N)) anchor = ANCHOR_N; else if (val.equals(ANCH_TOK_NE)) anchor = ANCHOR_NE; else if (val.equals(ANCH_TOK_E)) anchor = ANCHOR_E; else if (val.equals(ANCH_TOK_SE)) anchor = ANCHOR_SE; else if (val.equals(ANCH_TOK_S)) anchor = ANCHOR_S; else if (val.equals(ANCH_TOK_SW)) anchor = ANCHOR_SW; else if (val.equals(ANCH_TOK_W)) anchor = ANCHOR_W; else if (val.equals(ANCH_TOK_NW)) anchor = ANCHOR_NW; else if (val.equals(ANCH_TOK_CENTER)) anchor = ANCHOR_CENTER; else throw new NoSuchElementException();
                } else if (tok.equals(F_EXPAND)) {
                    if (val.equals(EXPAND_TRUE) || val.equals(EXPAND_YES)) expand = true; else if (val.equals(EXPAND_FALSE) || val.equals(EXPAND_NO)) expand = false; else throw new NoSuchElementException();
                } else if (tok.equals(F_FILL)) {
                    if (val.equals(FILL_NONE)) {
                        filly = fillx = false;
                    } else if (val.equals(FILL_X)) {
                        fillx = true;
                        filly = false;
                    } else if (val.equals(FILL_Y)) {
                        filly = true;
                        fillx = false;
                    } else if (val.equals(FILL_BOTH)) {
                        fillx = filly = true;
                    } else throw new NoSuchElementException();
                } else if (tok.equals(F_IPADX)) {
                    ipadx = Integer.parseInt(val);
                } else if (tok.equals(F_IPADY)) {
                    ipady = Integer.parseInt(val);
                } else if (tok.equals(F_PADX)) {
                    padx = Integer.parseInt(val);
                } else if (tok.equals(F_PADY)) {
                    pady = Integer.parseInt(val);
                } else if (tok.equals(F_SIDE)) {
                    if (val.equals(SIDE_TOP)) {
                        side = ISIDE_TOP;
                    } else if (val.equals(SIDE_LEFT)) {
                        side = ISIDE_LEFT;
                    } else if (val.equals(SIDE_RIGHT)) {
                        side = ISIDE_RIGHT;
                    } else if (val.equals(SIDE_BOTTOM)) {
                        side = ISIDE_BOTTOM;
                    } else throw new NoSuchElementException();
                } else if (tok.equals(WIDTH)) {
                    width = Integer.parseInt(val);
                } else if (tok.equals(HEIGHT)) {
                    height = Integer.parseInt(val);
                } else if (tok.equals(DEBUG)) {
                    debug = Integer.parseInt(val);
                } else throw new NoSuchElementException(tok);
                if (st.hasMoreTokens() && !st.nextToken().equals(CHAR_SEMI)) throw new NoSuchElementException();
            }
        }

        public int getPreferredWidth() {
            return comp.getPreferredSize().width;
        }

        public int getPreferredHeight() {
            return comp.getPreferredSize().height;
        }

        public String toString() {
            String out = new String();
            out += "anchor = " + anchor + "\n";
            out += "expand = " + (expand ? "True" : "False") + "\n";
            out += "fillx = " + (fillx ? "True" : "False") + "\n";
            out += "filly = " + (filly ? "True" : "False") + "\n";
            out += "ipadx = " + ipadx + "\n";
            out += "ipady = " + ipady + "\n";
            out += "padx = " + padx + "\n";
            out += "pady = " + pady + "\n";
            out += "side = ";
            switch(side) {
                case ISIDE_TOP:
                    out += "Top";
                    break;
                case ISIDE_BOTTOM:
                    out += "Bottom";
                    break;
                case ISIDE_LEFT:
                    out += "Left";
                    break;
                case ISIDE_RIGHT:
                    out += "Right";
                    break;
            }
            out += "\n";
            return out;
        }
    }

    /**
     * Constructs a new Packer Layout.
     */
    public PackerLayout() {
        compinfo = new Hashtable();
        firstcomp = null;
        lastcomp = null;
    }

    /**
     * Adds the specified component to the layout.
     * @param name information about attachments
     * @param comp the the component to be added
     */
    public void addLayoutComponent(String name, Component comp) {
        if (comp == null) return;
        PackTable packTable = new PackTable(comp, name);
        compinfo.put(comp, packTable);
        if (firstcomp == null) {
            firstcomp = packTable;
            lastcomp = packTable;
        } else {
            lastcomp.next = packTable;
            packTable.prev = lastcomp;
            lastcomp = packTable;
        }
    }

    /**
     * Removes the specified component from the layout.
     * @param comp the component to remove
     */
    public void removeLayoutComponent(Component comp) {
        PackTable packTable = (PackTable) compinfo.get(comp);
        if (packTable.prev == null) firstcomp = packTable.next;
        if (packTable.next == null) lastcomp = packTable.prev;
        if (packTable.prev != null) packTable.prev.next = packTable.next;
        if (packTable.next != null) packTable.next.prev = packTable.prev;
        compinfo.remove(comp);
    }

    /**
     * Returns the preferred dimensions for this layout given the
     * components in the specified target container.
     * @param target the component which needs to be laid out
     * @see Container
     * @see #minimumSize
     */
    public Dimension preferredLayoutSize(Container target) {
        return getLayoutSize(target, false);
    }

    /**
     * Returns the minimum dimensions needed to layout the
     * components contained in the specified target container.
     * @param target the component which needs to be laid out
     * @see #preferredSize
     */
    public Dimension minimumLayoutSize(Container target) {
        return getLayoutSize(target, true);
    }

    public Dimension getLayoutSize(Container target, boolean min) {
        Insets insets = target.getInsets();
        Dimension dim = new Dimension(0, 0);
        Dimension d, dmax = new Dimension(0, 0);
        int nmembers = target.getComponentCount();
        Component[] components = target.getComponents();
        for (int i = 0; i < nmembers; i++) {
            d = components[i].getMinimumSize();
            if (!min) {
                Dimension p = components[i].getPreferredSize();
                d.width = Math.max(p.width, d.width);
                d.height = Math.max(p.height, d.height);
            }
            PackTable packTable = (PackTable) compinfo.get(components[i]);
            if (packTable == null) break;
            if (packTable.width > -1) d.width = packTable.width;
            if (packTable.height > -1) d.height = packTable.height;
            if (debug > 0) {
                System.out.println(min ? "minimum" : "prefered" + " size: " + String.valueOf(d.width) + "x" + String.valueOf(d.height));
            }
            d.width += (packTable.padx * 2) + packTable.ipadx + dim.width;
            d.height += (packTable.pady * 2) + packTable.ipady + dim.height;
            if (packTable.side == ISIDE_TOP || packTable.side == ISIDE_BOTTOM) {
                if (d.width > dmax.width) dmax.width = d.width;
                dim.height = d.height;
            } else {
                if (d.height > dmax.height) dmax.height = d.height;
                dim.width = d.width;
            }
        }
        if (dim.width > dmax.width) dmax.width = dim.width;
        if (dim.height > dmax.height) dmax.height = dim.height;
        dmax.width += (insets.left + insets.right);
        dmax.height += (insets.top + insets.bottom);
        if (debug > 0) {
            System.out.println("Insets: " + String.valueOf(insets.left) + " " + String.valueOf(insets.right) + " " + String.valueOf(insets.top) + " " + String.valueOf(insets.bottom));
            System.out.println("Container " + (min ? "minimum" : "prefered") + " size: " + String.valueOf(dmax.width) + "x" + String.valueOf(dmax.height));
        }
        return dmax;
    }

    /**
     * Lays out the container. This method will actually reshape the
     * components in target in order to satisfy the constraints.
     * @param target the specified component being laid out.
     * @see Container
     */
    public void layoutContainer(Container target) {
        Insets insets = target.getInsets();
        Dimension dim = target.getSize();
        int cavityX = 0, cavityY = 0;
        int cavityWidth = dim.width - (insets.left + insets.right);
        int cavityHeight = dim.height - (insets.top + insets.bottom);
        int frameX, frameY, frameWidth, frameHeight;
        int width, height, x, y;
        PackTable current;
        if (cavityWidth < 0) cavityWidth = 0;
        if (cavityHeight < 0) cavityHeight = 0;
        if (debug > 0) {
            System.out.println("Laying out container at size: " + String.valueOf(cavityWidth) + "x" + String.valueOf(cavityHeight));
        }
        for (current = firstcomp; current != null; current = current.next) {
            int padx = current.padx * 2;
            int pady = current.pady * 2;
            int ipadx = current.ipadx;
            int ipady = current.ipady;
            boolean fillx = current.fillx;
            boolean filly = current.filly;
            int minHeight = current.height;
            int minWidth = current.width;
            if (minHeight == -1) minHeight = current.comp.getMinimumSize().height;
            if (minWidth == -1) minWidth = current.comp.getMinimumSize().width;
            current.comp.doLayout();
            if (current.side == ISIDE_TOP || current.side == ISIDE_BOTTOM) {
                frameWidth = cavityWidth;
                frameHeight = minHeight + pady + ipady;
                if (current.expand) frameHeight += YExpansion(current, cavityHeight);
                cavityHeight -= frameHeight;
                if (cavityHeight < 0) {
                    frameHeight += cavityHeight;
                    cavityHeight = 0;
                }
                frameX = cavityX;
                if (current.side == ISIDE_TOP) {
                    frameY = cavityY;
                    cavityY += frameHeight;
                } else frameY = cavityY + cavityHeight;
            } else {
                frameHeight = cavityHeight;
                frameWidth = minWidth + padx + ipadx;
                if (current.expand) frameWidth += XExpansion(current, cavityWidth);
                cavityWidth -= frameWidth;
                if (cavityWidth < 0) {
                    frameWidth += cavityWidth;
                    cavityWidth = 0;
                }
                frameY = cavityY;
                if (current.side == ISIDE_LEFT) {
                    frameX = cavityX;
                    cavityX += frameWidth;
                } else frameX = cavityX + cavityWidth;
            }
            width = minWidth + ipadx;
            if (fillx && (width < (frameWidth - padx))) width = frameWidth - padx;
            height = minHeight + ipady;
            if (filly && (height < (frameHeight - pady))) height = frameHeight - pady;
            padx /= 2;
            pady /= 2;
            switch(current.anchor) {
                case ANCHOR_N:
                    x = frameX + (frameWidth - width) / 2;
                    y = frameY + pady;
                    break;
                case ANCHOR_NE:
                    x = frameX + frameWidth - width - padx;
                    y = frameY + pady;
                    break;
                case ANCHOR_E:
                    x = frameX + frameWidth - width - padx;
                    y = frameY + (frameHeight - height) / 2;
                    break;
                case ANCHOR_SE:
                    x = frameX + frameWidth - width - padx;
                    y = frameY + frameHeight - height - pady;
                    break;
                case ANCHOR_S:
                    x = frameX + (frameWidth - width) / 2;
                    y = frameY + frameHeight - height - pady;
                    break;
                case ANCHOR_SW:
                    x = frameX + padx;
                    y = frameY + frameHeight - height - pady;
                    break;
                case ANCHOR_W:
                    x = frameX + padx;
                    y = frameY + (frameHeight - height) / 2;
                    break;
                case ANCHOR_NW:
                    x = frameX + padx;
                    y = frameY + pady;
                    break;
                case ANCHOR_CENTER:
                default:
                    x = frameX + (frameWidth - width) / 2;
                    y = frameY + (frameHeight - height) / 2;
                    break;
            }
            if (debug > 0 || current.debug > 0) {
                System.out.println("size: " + width + "x" + height);
            }
            current.comp.setBounds(insets.left + x, y + insets.top, width, height);
        }
    }

    int XExpansion(PackTable current, int cavityWidth) {
        int numExpand, minExpand, curExpand;
        int childWidth;
        minExpand = cavityWidth;
        numExpand = 0;
        for (; current != null; current = current.next) {
            int minWidth = current.comp.getMinimumSize().width;
            childWidth = minWidth + (current.padx * 2) + current.ipadx;
            if (current.side == ISIDE_TOP || current.side == ISIDE_BOTTOM) {
                curExpand = (cavityWidth - childWidth) / numExpand;
                if (curExpand < minExpand) minExpand = curExpand;
            } else {
                cavityWidth -= childWidth;
                if (current.expand) numExpand++;
            }
        }
        curExpand = cavityWidth / numExpand;
        if (curExpand < minExpand) minExpand = curExpand;
        if (minExpand < 0) return 0; else return minExpand;
    }

    int YExpansion(PackTable current, int cavityHeight) {
        int numExpand, minExpand, curExpand;
        int childHeight;
        minExpand = cavityHeight;
        numExpand = 0;
        for (; current != null; current = current.next) {
            int minHeight = current.comp.getMinimumSize().height;
            childHeight = minHeight + (current.pady * 2) + current.ipady;
            if (current.side == ISIDE_LEFT || current.side == ISIDE_RIGHT) {
                curExpand = (cavityHeight - childHeight) / numExpand;
                if (curExpand < minExpand) minExpand = curExpand;
            } else {
                cavityHeight -= childHeight;
                if (current.expand) numExpand++;
            }
        }
        curExpand = cavityHeight / numExpand;
        if (curExpand < minExpand) minExpand = curExpand;
        if (minExpand < 0) return 0; else return minExpand;
    }
}
