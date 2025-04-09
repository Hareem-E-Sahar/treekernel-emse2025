package edu.uiuc.geom;

import java.lang.*;
import java.awt.*;
import java.util.*;

/**
 * PackerLayout is used to lay out widget components.
 *
 * @version     1.1, 97/12/05
 * @author      Daeron Meyer
 *
 * Based heavily on the work of John Ousterhout, creator of the Tk Toolkit.
 *
 */
public class PackerLayout extends Object implements LayoutManager {

    Hashtable compinfo;

    Hashtable nameinfo;

    Component firstcomp, lastcomp;

    public static final String initText = "PackerLayout (c) 1995 by Daeron Meyer\n";

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

    static final String F_NAME = "name";

    static final String ANCH_TOK_N = "n";

    static final String ANCH_TOK_NE = "ne";

    static final String ANCH_TOK_E = "e";

    static final String ANCH_TOK_SE = "se";

    static final String ANCH_TOK_S = "s";

    static final String ANCH_TOK_SW = "sw";

    static final String ANCH_TOK_W = "w";

    static final String ANCH_TOK_NW = "nw";

    static final String ANCH_TOK_CENTER = "center";

    static final int ANCHOR_N = 0;

    static final int ANCHOR_NE = 1;

    static final int ANCHOR_E = 2;

    static final int ANCHOR_SE = 3;

    static final int ANCHOR_S = 4;

    static final int ANCHOR_SW = 5;

    static final int ANCHOR_W = 6;

    static final int ANCHOR_NW = 7;

    static final int ANCHOR_CENTER = 8;

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

    static final String CHAR_SEMI = ";";

    static final String CHAR_EQUAL = "=";

    static final String CHAR_ALL = CHAR_SEMI + CHAR_EQUAL;

    static final String COMPONENT_NEXT = "next";

    static final String COMPONENT_PREV = "prev";

    static final int debug = 0;

    /**
     * Constructs a new Packer Layout.
     */
    public PackerLayout() {
        compinfo = new Hashtable();
        nameinfo = new Hashtable();
        firstcomp = null;
        lastcomp = null;
    }

    /**
     * Adds the specified component to the layout.
     * @param name information about attachments
     * @param comp the the component to be added
     */
    public void addLayoutComponent(String name, Component comp) {
        String realname = null;
        String tok, val;
        Hashtable packtable = new Hashtable();
        try {
            if (comp == null) return;
            StringTokenizer st = new StringTokenizer(name, CHAR_ALL, true);
            realname = new String(st.nextToken());
            packtable.put(F_ANCHOR, new Integer(ANCHOR_CENTER));
            packtable.put(F_EXPAND, new Boolean(false));
            packtable.put(F_FILLX, new Boolean(false));
            packtable.put(F_FILLY, new Boolean(false));
            packtable.put(F_IPADX, new Integer(0));
            packtable.put(F_IPADY, new Integer(0));
            packtable.put(F_PADX, new Integer(0));
            packtable.put(F_PADY, new Integer(0));
            packtable.put(F_SIDE, SIDE_TOP);
            nameinfo.put(realname, comp);
            compinfo.put(comp, packtable);
            packtable.put(F_NAME, realname);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equals(CHAR_SEMI)) {
                    if (st.hasMoreTokens()) tok = st.nextToken(); else return;
                    if (!st.nextToken().equals(CHAR_EQUAL)) throw new NoSuchElementException();
                    if (tok.equals(F_ANCHOR)) {
                        val = st.nextToken();
                        if (val.equals(ANCH_TOK_N)) packtable.put(tok, new Integer(ANCHOR_N)); else if (val.equals(ANCH_TOK_NE)) packtable.put(tok, new Integer(ANCHOR_NE)); else if (val.equals(ANCH_TOK_E)) packtable.put(tok, new Integer(ANCHOR_E)); else if (val.equals(ANCH_TOK_SE)) packtable.put(tok, new Integer(ANCHOR_SE)); else if (val.equals(ANCH_TOK_S)) packtable.put(tok, new Integer(ANCHOR_S)); else if (val.equals(ANCH_TOK_SW)) packtable.put(tok, new Integer(ANCHOR_SW)); else if (val.equals(ANCH_TOK_W)) packtable.put(tok, new Integer(ANCHOR_W)); else if (val.equals(ANCH_TOK_NW)) packtable.put(tok, new Integer(ANCHOR_NW)); else if (val.equals(ANCH_TOK_CENTER)) packtable.put(tok, new Integer(ANCHOR_CENTER)); else throw new NoSuchElementException();
                    } else if (tok.equals(F_EXPAND)) {
                        val = st.nextToken();
                        if (val.equals(EXPAND_TRUE) || val.equals(EXPAND_YES)) packtable.put(tok, new Boolean(true)); else if (val.equals(EXPAND_FALSE) || val.equals(EXPAND_NO)) packtable.put(tok, new Boolean(false)); else throw new NoSuchElementException();
                    } else if (tok.equals(F_FILL)) {
                        val = st.nextToken();
                        if (val.equals(FILL_NONE)) {
                            packtable.put(new String(F_FILLX), new Boolean(false));
                            packtable.put(new String(F_FILLY), new Boolean(false));
                        } else if (val.equals(FILL_X)) {
                            packtable.put(new String(F_FILLX), new Boolean(true));
                            packtable.put(new String(F_FILLY), new Boolean(false));
                        } else if (val.equals(FILL_Y)) {
                            packtable.put(new String(F_FILLX), new Boolean(false));
                            packtable.put(new String(F_FILLY), new Boolean(true));
                        } else if (val.equals(FILL_BOTH)) {
                            packtable.put(new String(F_FILLX), new Boolean(true));
                            packtable.put(new String(F_FILLY), new Boolean(true));
                        } else throw new NoSuchElementException();
                    } else if (tok.equals(F_IPADX) || tok.equals(F_IPADY) || tok.equals(F_PADX) || tok.equals(F_PADY)) {
                        val = st.nextToken();
                        packtable.put(tok, Integer.valueOf(val));
                    } else if (tok.equals(F_SIDE)) {
                        val = st.nextToken();
                        if (val.equals(SIDE_TOP) || val.equals(SIDE_LEFT) || val.equals(SIDE_RIGHT) || val.equals(SIDE_BOTTOM)) {
                            packtable.put(tok, val);
                        } else throw new NoSuchElementException();
                    }
                } else throw new NoSuchElementException();
            }
        } catch (Exception e) {
            if (realname != null) {
                System.out.println("PackerLayout: Syntax error in component: " + realname);
                nameinfo.remove(realname);
                compinfo.remove(comp);
            }
            return;
        }
        if (firstcomp == null) {
            firstcomp = comp;
            lastcomp = comp;
        } else {
            Hashtable opack = (Hashtable) compinfo.get(lastcomp);
            opack.put(COMPONENT_NEXT, comp);
            packtable.put(COMPONENT_PREV, lastcomp);
            lastcomp = comp;
        }
    }

    /**
     * Removes the specified component from the layout.
     * @param comp the component to remove
     */
    public void removeLayoutComponent(Component comp) {
        Component prev, next;
        Hashtable opack = (Hashtable) compinfo.get(comp);
        prev = (Component) opack.get(COMPONENT_PREV);
        next = (Component) opack.get(COMPONENT_NEXT);
        if (prev == null) {
            firstcomp = next;
        }
        if (next == null) {
            lastcomp = prev;
        }
        if (prev != null) {
            Hashtable npack = (Hashtable) compinfo.get(prev);
            if (next != null) npack.put(COMPONENT_NEXT, next); else npack.remove(COMPONENT_NEXT);
        }
        if (next != null) {
            Hashtable npack = (Hashtable) compinfo.get(next);
            if (prev != null) npack.put(COMPONENT_PREV, next); else npack.remove(COMPONENT_PREV);
        }
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
        Dimension dim = minimumLayoutSize(target);
        Dimension cdim = target.getSize();
        if (cdim.width < dim.width) cdim.width = dim.width;
        if (cdim.height < dim.height) cdim.height = dim.height;
        return cdim;
    }

    /**
     * Returns the minimum dimensions needed to layout the
     * components contained in the specified target container.
     * @param target the component which needs to be laid out 
     * @see #preferredSize
     */
    public Dimension minimumLayoutSize(Container target) {
        Insets insets = target.getInsets();
        Dimension dim = new Dimension(0, 0);
        Dimension d, dmax = new Dimension(0, 0);
        int nmembers = target.getComponentCount();
        for (int i = 0; i < nmembers; i++) {
            Component m = target.getComponent(i);
            d = m.getMinimumSize();
            Hashtable ptable = (Hashtable) compinfo.get(m);
            if (ptable == null) break;
            if (debug > 0) {
                String realname = (String) ptable.get(F_NAME);
                System.out.println(realname + " minimum size: " + String.valueOf(d.width) + "x" + String.valueOf(d.height));
            }
            int padx = ((Integer) ptable.get(F_PADX)).intValue() * 2;
            int pady = ((Integer) ptable.get(F_PADY)).intValue() * 2;
            int ipadx = ((Integer) ptable.get(F_IPADX)).intValue();
            int ipady = ((Integer) ptable.get(F_IPADY)).intValue();
            String side = (String) ptable.get(F_SIDE);
            d.width += padx + ipadx + dim.width;
            d.height += pady + ipady + dim.height;
            if (side.equals(SIDE_TOP) || side.equals(SIDE_BOTTOM)) {
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
            System.out.println("Container minimum size: " + String.valueOf(dmax.width) + "x" + String.valueOf(dmax.height));
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
        Component current = firstcomp;
        if (debug > 0) {
            System.out.println("Laying out container at size: " + String.valueOf(cavityWidth) + "x" + String.valueOf(cavityHeight));
        }
        while (current != null) {
            Hashtable ptable = (Hashtable) compinfo.get(current);
            String side = (String) ptable.get(F_SIDE);
            int padx = ((Integer) ptable.get(F_PADX)).intValue() * 2;
            int pady = ((Integer) ptable.get(F_PADY)).intValue() * 2;
            int ipadx = ((Integer) ptable.get(F_IPADX)).intValue();
            int ipady = ((Integer) ptable.get(F_IPADY)).intValue();
            boolean expand = ((Boolean) ptable.get(F_EXPAND)).booleanValue();
            boolean fillx = ((Boolean) ptable.get(F_FILLX)).booleanValue();
            boolean filly = ((Boolean) ptable.get(F_FILLY)).booleanValue();
            int anchor = ((Integer) ptable.get(F_ANCHOR)).intValue();
            String name = (String) ptable.get(F_NAME);
            current.doLayout();
            if (side.equals(SIDE_TOP) || side.equals(SIDE_BOTTOM)) {
                frameWidth = cavityWidth;
                frameHeight = current.getPreferredSize().height + pady + ipady;
                if (expand) frameHeight += YExpansion(current, cavityHeight);
                cavityHeight -= frameHeight;
                if (cavityHeight < 0) {
                    frameHeight += cavityHeight;
                    cavityHeight = 0;
                }
                frameX = cavityX;
                if (side.equals(SIDE_TOP)) {
                    frameY = cavityY;
                    cavityY += frameHeight;
                } else {
                    frameY = cavityY + cavityHeight;
                }
            } else {
                frameHeight = cavityHeight;
                frameWidth = current.getPreferredSize().width + padx + ipadx;
                if (expand) frameWidth += XExpansion(current, cavityWidth);
                cavityWidth -= frameWidth;
                if (cavityWidth < 0) {
                    frameWidth += cavityWidth;
                    cavityWidth = 0;
                }
                frameY = cavityY;
                if (side.equals(SIDE_LEFT)) {
                    frameX = cavityX;
                    cavityX += frameWidth;
                } else {
                    frameX = cavityX + cavityWidth;
                }
            }
            width = current.getPreferredSize().width + ipadx;
            if (fillx || (width > (frameWidth - padx))) width = frameWidth - padx;
            height = current.getPreferredSize().height + ipady;
            if (filly || (height > (frameHeight - pady))) height = frameHeight - pady;
            padx /= 2;
            pady /= 2;
            switch(anchor) {
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
            if (debug > 0) {
                System.out.println("Component size: " + String.valueOf(width) + "x" + String.valueOf(height));
            }
            current.setBounds(insets.left + x, y + insets.top, width, height);
            current = (Component) ptable.get(COMPONENT_NEXT);
        }
    }

    int XExpansion(Component current, int cavityWidth) {
        Hashtable ptable = (Hashtable) compinfo.get(current);
        int numExpand, minExpand, curExpand;
        int childWidth;
        minExpand = cavityWidth;
        numExpand = 0;
        for (; current != null; current = (Component) ptable.get(COMPONENT_NEXT)) {
            ptable = (Hashtable) compinfo.get(current);
            int padx = ((Integer) ptable.get(F_PADX)).intValue() * 2;
            int ipadx = ((Integer) ptable.get(F_IPADX)).intValue();
            boolean expand = ((Boolean) ptable.get(F_EXPAND)).booleanValue();
            String side = (String) ptable.get(F_SIDE);
            childWidth = current.getPreferredSize().width + padx + ipadx;
            if (side.equals(SIDE_TOP) || side.equals(SIDE_BOTTOM)) {
                curExpand = (cavityWidth - childWidth) / numExpand;
                if (curExpand < minExpand) minExpand = curExpand;
            } else {
                cavityWidth -= childWidth;
                if (expand) numExpand++;
            }
        }
        curExpand = cavityWidth / numExpand;
        if (curExpand < minExpand) minExpand = curExpand;
        if (minExpand < 0) return 0; else return minExpand;
    }

    int YExpansion(Component current, int cavityHeight) {
        Hashtable ptable = (Hashtable) compinfo.get(current);
        int numExpand, minExpand, curExpand;
        int childHeight;
        minExpand = cavityHeight;
        numExpand = 0;
        for (; current != null; current = (Component) ptable.get(COMPONENT_NEXT)) {
            ptable = (Hashtable) compinfo.get(current);
            int pady = ((Integer) ptable.get(F_PADY)).intValue() * 2;
            int ipady = ((Integer) ptable.get(F_IPADY)).intValue();
            boolean expand = ((Boolean) ptable.get(F_EXPAND)).booleanValue();
            String side = (String) ptable.get(F_SIDE);
            childHeight = current.getPreferredSize().height + pady + ipady;
            if (side.equals(SIDE_LEFT) || side.equals(SIDE_RIGHT)) {
                curExpand = (cavityHeight - childHeight) / numExpand;
                if (curExpand < minExpand) minExpand = curExpand;
            } else {
                cavityHeight -= childHeight;
                if (expand) {
                    numExpand++;
                }
            }
        }
        curExpand = cavityHeight / numExpand;
        if (curExpand < minExpand) minExpand = curExpand;
        if (minExpand < 0) return 0; else return minExpand;
    }

    /**
     * Returns the String representation of this class...
     */
    public String toString() {
        return getClass().getName();
    }
}
