package org.fit.cssbox.layout;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;
import cz.vutbr.web.css.*;
import org.w3c.dom.*;

/**
 * A box corresponding to a block element
 *
 * @author  radek
 */
public class BlockBox extends ElementBox {

    public static final CSSProperty.Float FLOAT_NONE = CSSProperty.Float.NONE;

    public static final CSSProperty.Float FLOAT_LEFT = CSSProperty.Float.LEFT;

    public static final CSSProperty.Float FLOAT_RIGHT = CSSProperty.Float.RIGHT;

    public static final CSSProperty.Clear CLEAR_NONE = CSSProperty.Clear.NONE;

    public static final CSSProperty.Clear CLEAR_LEFT = CSSProperty.Clear.LEFT;

    public static final CSSProperty.Clear CLEAR_RIGHT = CSSProperty.Clear.RIGHT;

    public static final CSSProperty.Clear CLEAR_BOTH = CSSProperty.Clear.BOTH;

    public static final CSSProperty.Position POS_STATIC = CSSProperty.Position.STATIC;

    public static final CSSProperty.Position POS_RELATIVE = CSSProperty.Position.RELATIVE;

    public static final CSSProperty.Position POS_ABSOLUTE = CSSProperty.Position.ABSOLUTE;

    public static final CSSProperty.Position POS_FIXED = CSSProperty.Position.FIXED;

    public static final CSSProperty.TextAlign ALIGN_LEFT = CSSProperty.TextAlign.LEFT;

    public static final CSSProperty.TextAlign ALIGN_RIGHT = CSSProperty.TextAlign.RIGHT;

    public static final CSSProperty.TextAlign ALIGN_CENTER = CSSProperty.TextAlign.CENTER;

    public static final CSSProperty.TextAlign ALIGN_JUSTIFY = CSSProperty.TextAlign.JUSTIFY;

    public static final CSSProperty.Overflow OVERFLOW_VISIBLE = CSSProperty.Overflow.VISIBLE;

    public static final CSSProperty.Overflow OVERFLOW_HIDDEN = CSSProperty.Overflow.HIDDEN;

    public static final CSSProperty.Overflow OVERFLOW_SCROLL = CSSProperty.Overflow.SCROLL;

    public static final CSSProperty.Overflow OVERFLOW_AUTO = CSSProperty.Overflow.AUTO;

    /** the minimal width of the space between the floating blocks that
     * can be used for placing the in-flow content */
    protected static final int INFLOW_SPACE_THRESHOLD = 15;

    /** Does this box contain blocks? */
    protected boolean contblock;

    /** Contains any in-flow boxes? */
    protected boolean anyinflow;

    /** Floating boxes left */
    protected FloatList fleft;

    /** Floating boxes right */
    protected FloatList fright;

    /** If we are a floating box, in which FloatList we are placed. */
    protected FloatList fown;

    /** x coordinate of the content box relative to the content box of the
     * top owner of the FloatList (from left)  */
    protected int floatXl;

    /** x coordinate of the content box relative to the content box of the
     * top owner of the FloatList (from right) */
    protected int floatXr;

    /** y coordinate of the content box relative to the content box of the 
        top owner of the FloatList */
    protected int floatY;

    /** Minimal size of the box. Values of -1 mean 'not set'. */
    protected Dimension min_size;

    /** Maximal size of the box. Values of -1 mean 'not set'. */
    protected Dimension max_size;

    /** true if the box width has been set explicitly */
    protected boolean wset;

    /** true if the box height has been set explicitly */
    protected boolean hset;

    /** true if the width is relative [%] */
    protected boolean wrelative;

    /** the preferred width or -1 if something is set to "auto" */
    protected int preferredWidth;

    /** Minimal width necessary for the last executed layout. */
    protected int lastPreferredWidth;

    /** True means that the layout inside has been already computed
     * and the width shouldn't be changed anymore */
    protected boolean widthComputed = false;

    /** Originally declared margin. This property saves the original values
     * where the efficient left and right margin may
     * be computed from the containing box */
    protected LengthSet declMargin;

    /** Floating property */
    protected CSSProperty.Float floating;

    /** Clearing property */
    protected CSSProperty.Clear clearing;

    /** Position property */
    protected CSSProperty.Position position;

    /** Overflow property */
    protected CSSProperty.Overflow overflow;

    /** Position coordinates */
    protected LengthSet coords;

    /** The top position coordinate is set explicitely */
    protected boolean topset;

    /** The top position coordinate is set explicitely */
    protected boolean leftset;

    /** The top position coordinate is set explicitely */
    protected boolean bottomset;

    /** The top position coordinate is set explicitely */
    protected boolean rightset;

    /** the left position should be set to static position during the layout */
    protected boolean leftstatic;

    /** the top position should be set to static position during the layout */
    protected boolean topstatic;

    /** Reference box for absolutely positioned boxes. It is used
     * when some of the absolute coordinates are based on the static position */
    protected Box absReference;

    /** Text-align property */
    protected CSSProperty.TextAlign align;

    /** Creates a new instance of BlockBox */
    public BlockBox(Element n, Graphics2D g, VisualContext ctx) {
        super(n, g, ctx);
        isblock = true;
        contblock = false;
        anyinflow = false;
        fleft = null;
        fright = null;
        fown = null;
        floatXl = 0;
        floatXr = 0;
        floatY = 0;
        floating = FLOAT_NONE;
        clearing = CLEAR_NONE;
        position = POS_STATIC;
        overflow = OVERFLOW_VISIBLE;
        align = ALIGN_LEFT;
        topset = false;
        leftset = false;
        bottomset = false;
        rightset = false;
        topstatic = false;
        if (style != null) loadBlockStyle();
    }

    /** Convert an inline box to a block box */
    public BlockBox(InlineBox src) {
        super(src.el, src.g, src.ctx);
        setStyle(src.getStyle());
        viewport = src.viewport;
        cblock = src.cblock;
        clipblock = src.clipblock;
        isblock = true;
        contblock = false;
        anyinflow = false;
        style = src.style;
        fleft = null;
        fright = null;
        fown = null;
        floatXl = 0;
        floatXr = 0;
        floatY = 0;
        floating = FLOAT_NONE;
        clearing = CLEAR_NONE;
        position = POS_STATIC;
        overflow = OVERFLOW_VISIBLE;
        align = ALIGN_LEFT;
        topset = false;
        leftset = false;
        bottomset = false;
        rightset = false;
        topstatic = false;
        nested = src.nested;
        startChild = src.startChild;
        endChild = src.endChild;
        loadBlockStyle();
    }

    public void copyValues(BlockBox src) {
        super.copyValues(src);
        contblock = src.contblock;
        anyinflow = src.anyinflow;
        setFloats(src.fleft, src.fright, src.floatXl, src.floatXr, src.floatY);
        fown = src.fown;
        floating = src.floating;
        clearing = src.clearing;
        position = src.position;
        overflow = src.overflow;
        align = src.align;
        topset = src.topset;
        leftset = src.leftset;
        bottomset = src.bottomset;
        rightset = src.rightset;
        topstatic = src.topstatic;
        if (src.declMargin != null) declMargin = new LengthSet(src.declMargin);
    }

    @Override
    public BlockBox copyBox() {
        BlockBox ret = new BlockBox(el, g, ctx);
        ret.copyValues(this);
        return ret;
    }

    @Override
    public void initBox() {
        setFloats(new FloatList(this), new FloatList(this), 0, 0, 0);
    }

    @Override
    public void addSubBox(Box box) {
        super.addSubBox(box);
        if (box.isInFlow()) {
            anyinflow = true;
            if (box.isBlock()) contblock = true;
        }
    }

    @Override
    public void setStyle(NodeData s) {
        super.setStyle(s);
        loadBlockStyle();
    }

    @Override
    public String toString() {
        return "<" + el.getTagName() + " id=\"" + el.getAttribute("id") + "\" class=\"" + el.getAttribute("class") + "\">";
    }

    @Override
    public boolean mayContainBlocks() {
        return true;
    }

    public boolean containsBlocks() {
        return contblock;
    }

    public void setFloats(FloatList left, FloatList right, int xl, int xr, int y) {
        fleft = left;
        fright = right;
        floatXl = xl;
        floatXr = xr;
        floatY = y;
    }

    public void setOwnerFloatList(FloatList list) {
        fown = list;
    }

    public FloatList getOwnerFloatList() {
        return fown;
    }

    public CSSProperty.Float getFloating() {
        return floating;
    }

    public String getFloatingString() {
        return floating.toString();
    }

    public CSSProperty.Clear getClearing() {
        return clearing;
    }

    public String getClearingString() {
        return clearing.toString();
    }

    public String getPositionString() {
        return position.toString();
    }

    public String getOverflowString() {
        return overflow.toString();
    }

    public int getFloatY() {
        return floatY;
    }

    /** Returns true if the box is in the normal text flow (not absolutely
     * positioned nor floating) */
    @Override
    public boolean isInFlow() {
        return (displayed && floating == FLOAT_NONE && position != POS_ABSOLUTE && position != POS_FIXED);
    }

    /** Returns true if the box is absolutely positioned. */
    public boolean isPositioned() {
        return (displayed && (position == POS_ABSOLUTE || position == POS_FIXED));
    }

    /** Returns true if the box is floating. */
    public boolean isFloating() {
        return (displayed && floating != FLOAT_NONE);
    }

    @Override
    public boolean containsFlow() {
        return anyinflow;
    }

    @Override
    public boolean isWhitespace() {
        if (anyinflow || encloseFloats()) return super.isWhitespace(); else return true;
    }

    @Override
    public boolean marginsAdjoin() {
        if (padding.top > 0 || padding.bottom > 0 || border.top > 0 || border.bottom > 0) {
            return false;
        } else if (min_size.height > 0) {
            return false;
        } else if (hset) {
            return content.height == 0;
        } else if (!anyinflow) {
            return true;
        } else {
            for (int i = startChild; i < endChild; i++) {
                Box box = getSubBox(i);
                if (box instanceof ElementBox) {
                    if (!((ElementBox) box).marginsAdjoin()) return false;
                } else {
                    if (!box.isWhitespace()) return false;
                }
            }
            return true;
        }
    }

    /** Returns true if the element displays at least something */
    @Override
    public boolean affectsDisplay() {
        boolean ret = containsFlow();
        if (border.top > 0 || border.bottom > 0) ret = true;
        if (padding.top > 0 || padding.bottom > 0) ret = true;
        return ret;
    }

    @Override
    public boolean canSplitInside() {
        return false;
    }

    @Override
    public boolean canSplitBefore() {
        return true;
    }

    @Override
    public boolean canSplitAfter() {
        return true;
    }

    @Override
    public boolean startsWithWhitespace() {
        return false;
    }

    @Override
    public boolean endsWithWhitespace() {
        return false;
    }

    @Override
    public void setIgnoreInitialWhitespace(boolean b) {
    }

    /**
     * Checks if the width of the block may be increase to enclose the children. This is true for special blocks
     * only such as table cells.
     * @return <code>true</code> if the width may be increased
     */
    public boolean canIncreaseWidth() {
        return false;
    }

    /**
     * Checks whether the block should enclose all the floating children as well.
     * See http://www.w3.org/TR/CSS21/visudet.html#root-height
     * @return <code>true</code> if the floats should enclosed
     */
    protected boolean encloseFloats() {
        return overflow != OVERFLOW_VISIBLE || floating != FLOAT_NONE || position == POS_ABSOLUTE || display == ElementBox.DISPLAY_INLINE_BLOCK;
    }

    /**
     * If none of the width parts is "auto", returns the total preferred width. Otherwise,
     * -1 is returned.
     * @return the preferred width
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    public Box getAbsReference() {
        return absReference;
    }

    /**
     * Sets the width of the content while considering the min- and max- width.
     * @param width the width the set if possible
     */
    public void setContentWidth(int width) {
        int w = width;
        if (max_size.width != -1 && w > max_size.width) w = max_size.width;
        if (min_size.width != -1 && w < min_size.width) w = min_size.width;
        content.width = w;
    }

    /**
     * Sets the height of the content while considering the min- and max- height.
     * @param height the height the set if possible
     */
    public void setContentHeight(int height) {
        int h = height;
        if (max_size.height != -1 && h > max_size.height) h = max_size.height;
        if (min_size.height != -1 && h < min_size.height) h = min_size.height;
        content.height = h;
    }

    @Override
    public int totalHeight() {
        if (border.top == 0 && border.bottom == 0 && padding.top == 0 && padding.bottom == 0 && content.height == 0) return Math.max(emargin.top, emargin.bottom); else return emargin.top + border.top + padding.top + content.height + padding.bottom + border.bottom + emargin.bottom;
    }

    /**
     * Moves down all the floating boxes contained in this box and its children. 
     */
    protected void moveFloatsDown(int ofs) {
        floatY += ofs;
        for (int i = startChild; i < endChild; i++) {
            Box box = getSubBox(i);
            if (box instanceof BlockBox) {
                BlockBox block = (BlockBox) box;
                if (block.isInFlow()) block.moveFloatsDown(ofs); else if (block.getFloating() != BlockBox.FLOAT_NONE) block.moveDown(ofs);
            }
        }
    }

    /** 
     * Aligns the subboxes in a line according to the selected alignment settings.
     * @param line The line box to be aligned
     */
    private void alignLineHorizontally(LineBox line) {
        int dif = content.width - line.getLimits() - line.getWidth();
        if (align != ALIGN_LEFT && dif > 0) {
            for (int i = line.getStart(); i < line.getEnd(); i++) {
                Box subbox = getSubBox(i);
                if (!subbox.isBlock()) {
                    if (align == ALIGN_RIGHT) subbox.moveRight(dif); else if (align == ALIGN_CENTER) subbox.moveRight(dif / 2);
                }
            }
        }
    }

    private void alignLineVertically(LineBox line) {
        for (int i = line.getStart(); i < line.getEnd(); i++) {
            Box subbox = getSubBox(i);
            if (!subbox.isBlock()) {
                int dif = line.alignBox((Inline) subbox);
                if (subbox instanceof InlineBox) dif = dif - ((ElementBox) subbox).getContentOffsetY();
                if (subbox instanceof InlineElement) ((InlineElement) subbox).setLineBox(line);
                int y = line.getY() + (line.getLead() / 2) + dif;
                subbox.moveDown(y);
            }
        }
    }

    /**
     * Computes the efficient sizes of in-flow margins for collapsing
     */
    @Override
    public void computeEfficientMargins() {
        emargin.top = margin.top;
        emargin.bottom = margin.bottom;
        if (containsBlocks() && containsFlow()) {
            BlockBox firstseparated = null;
            int mbottom = 0;
            for (int i = 0; i < getSubBoxNumber(); i++) {
                BlockBox subbox = (BlockBox) getSubBox(i);
                if (subbox.isDisplayed() && subbox.isInFlow()) {
                    subbox.computeEfficientMargins();
                    boolean boxempty = subbox.marginsAdjoin();
                    if (firstseparated == null && !separatedFromTop(this)) {
                        if (subbox.emargin.top > emargin.top) emargin.top = subbox.emargin.top;
                    }
                    if (boxempty) {
                        if (subbox.emargin.bottom > mbottom) mbottom = subbox.emargin.bottom;
                    } else mbottom = subbox.emargin.bottom;
                    if (!boxempty && firstseparated == null) firstseparated = subbox;
                }
            }
            if (mbottom > emargin.bottom && !separatedFromBottom(this)) emargin.bottom = mbottom;
        }
        if (marginsAdjoin()) emargin.top = emargin.bottom = Math.max(emargin.top, emargin.bottom);
    }

    /** Layout the sub-elements.
     * @param availw Maximal width available to the child elements
     * @param force Use the area even if the used width is greater than maxwidth
     * @param linestart Indicates whether the element is placed at the line start
     * @return <code>true</code> if the box has been succesfully placed
     */
    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart) {
        if (!displayed) {
            content.setSize(0, 0);
            bounds.setSize(0, 0);
            return true;
        }
        clearSplitted();
        if (!hasFixedWidth()) {
            int min = getMinimalContentWidthLimit();
            int max = getMaximalContentWidth();
            int availcont = availw - emargin.left - border.left - padding.left - emargin.right - border.right - padding.right;
            int pref = Math.min(max, availcont);
            if (pref < min) pref = min;
            setContentWidth(pref);
            updateChildSizes();
        }
        widthComputed = true;
        setAvailableWidth(totalWidth());
        if (!contblock) layoutInline(); else layoutBlocks();
        return true;
    }

    /**
     * Lay out inline boxes inside of this block
     */
    protected void layoutInline() {
        int x1 = fleft.getWidth(floatY) - floatXl;
        int x2 = fright.getWidth(floatY) - floatXr;
        if (x1 < 0) x1 = 0;
        if (x2 < 0) x2 = 0;
        int wlimit = getAvailableContentWidth();
        int minx1 = 0 - floatXl;
        int minx2 = 0 - floatXr;
        if (minx1 < 0) minx1 = 0;
        if (minx2 < 0) minx2 = 0;
        int x = x1;
        int y = 0;
        int maxw = 0;
        int prefw = 0;
        int lnstr = 0;
        int lastbreak = 0;
        boolean someinflow = false;
        boolean lastwhite = false;
        Vector<LineBox> lines = new Vector<LineBox>();
        LineBox curline = new LineBox(this, 0, 0);
        lines.add(curline);
        for (int i = 0; i < getSubBoxNumber(); i++) {
            Box subbox = getSubBox(i);
            if (subbox.isBlock()) {
                BlockBox sb = (BlockBox) subbox;
                BlockLayoutStatus stat = new BlockLayoutStatus();
                stat.inlineWidth = x - x1;
                stat.y = y;
                stat.maxh = 0;
                boolean atstart = (x <= x1);
                if (sb.getClearing() != CLEAR_NONE) {
                    int ny = stat.y;
                    if (sb.getClearing() == CLEAR_LEFT) ny = fleft.getMaxY() - floatY; else if (sb.getClearing() == CLEAR_RIGHT) ny = fright.getMaxY() - floatY; else if (sb.getClearing() == CLEAR_BOTH) ny = Math.max(fleft.getMaxY(), fright.getMaxY()) - floatY;
                    if (stat.y < ny) stat.y = ny;
                }
                if (sb.getFloating() == FLOAT_LEFT || sb.getFloating() == FLOAT_RIGHT) {
                    layoutBlockFloating(sb, wlimit, stat);
                    if (stat.inlineWidth > 0 && curline.getStart() < i) {
                        for (int j = curline.getStart(); j < i; j++) {
                            Box child = getSubBox(j);
                            if (!child.isBlock()) child.moveRight(sb.getWidth());
                        }
                        x += sb.getWidth();
                    }
                } else {
                    layoutBlockPositioned(sb, wlimit, stat);
                }
                if (stat.prefw > prefw) prefw = stat.prefw;
                x1 = fleft.getWidth(y + floatY) - floatXl;
                x2 = fright.getWidth(y + floatY) - floatXr;
                if (x1 < 0) x1 = 0;
                if (x2 < 0) x2 = 0;
                if (atstart && x < x1) x = x1;
                continue;
            }
            if (subbox.canSplitBefore()) lastbreak = i;
            someinflow = true;
            boolean split;
            do {
                split = false;
                int space = wlimit - x1 - x2;
                boolean narrowed = (x1 > minx1 || x2 > minx2);
                boolean f = (x == x1 || lastbreak == lnstr) && !narrowed;
                if (lastwhite) subbox.setIgnoreInitialWhitespace(true);
                boolean fit = false;
                if (space >= INFLOW_SPACE_THRESHOLD || !narrowed) fit = subbox.doLayout(wlimit - x - x2, f, x == x1);
                if (fit) {
                    if (subbox.isInFlow()) {
                        subbox.setPosition(x, 0);
                        x += subbox.getWidth();
                    }
                    curline.considerBox((Inline) subbox);
                }
                boolean over = (x > wlimit - x2);
                if (!fit && narrowed && (x == x1 || lastbreak == lnstr)) {
                    if (x > maxw) maxw = x;
                    y += getLineHeight();
                    curline.setY(y);
                    x1 = fleft.getWidth(y + floatY) - floatXl;
                    x2 = fright.getWidth(y + floatY) - floatXr;
                    if (x1 < 0) x1 = 0;
                    if (x2 < 0) x2 = 0;
                    x = x1;
                    split = true;
                } else if ((!fit && lastbreak > lnstr) || (fit && (over || subbox.getRest() != null))) {
                    curline.setWidth(x - x1);
                    curline.setLimits(x1, x2);
                    if (x > maxw) maxw = x;
                    y += curline.getMaxLineHeight();
                    x1 = fleft.getWidth(y + floatY) - floatXl;
                    x2 = fright.getWidth(y + floatY) - floatXr;
                    if (x1 < 0) x1 = 0;
                    if (x2 < 0) x2 = 0;
                    x = x1;
                    if (!fit) {
                        lnstr = i;
                        curline.setEnd(lnstr);
                        curline = new LineBox(this, lnstr, y);
                        lines.add(curline);
                        split = true;
                    } else if (over || subbox.getRest() != null) {
                        if (subbox.getRest() != null) insertSubBox(i + 1, subbox.getRest());
                        lnstr = i + 1;
                        curline.setEnd(lnstr);
                        curline = new LineBox(this, lnstr, y);
                        lines.add(curline);
                    }
                }
                lastwhite = subbox.collapsesSpaces() && subbox.endsWithWhitespace();
            } while (split);
            if (subbox.canSplitAfter()) lastbreak = i + 1;
        }
        if (someinflow) {
            if (x > maxw) maxw = x;
            if (maxw > prefw) prefw = maxw;
        }
        lastPreferredWidth = prefw;
        if (!hasFixedHeight()) {
            y += curline.getMaxLineHeight();
            if (encloseFloats()) {
                int mfy = getFloatHeight() - floatY;
                if (mfy > y) y = mfy;
            }
            setContentHeight(y);
            updateSizes();
            updateChildSizes();
        }
        setSize(totalWidth(), totalHeight());
        curline.setWidth(x);
        curline.setLimits(x1, x2);
        curline.setEnd(getSubBoxNumber());
        for (Iterator<LineBox> it = lines.iterator(); it.hasNext(); ) {
            LineBox line = it.next();
            alignLineHorizontally(line);
            alignLineVertically(line);
        }
    }

    /**
     * Lay out nested block boxes in this box
     */
    protected void layoutBlocks() {
        int wlimit = getAvailableContentWidth();
        BlockLayoutStatus stat = new BlockLayoutStatus();
        int mtop = 0;
        int mbottom = 0;
        for (int i = 0; i < getSubBoxNumber(); i++) {
            int nexty = stat.y;
            BlockBox subbox = (BlockBox) getSubBox(i);
            if (subbox.isDisplayed()) {
                boolean clearance = false;
                if (subbox.getClearing() != CLEAR_NONE) {
                    int ny = stat.y;
                    if (subbox.getClearing() == CLEAR_LEFT) ny = fleft.getMaxY() - floatY; else if (subbox.getClearing() == CLEAR_RIGHT) ny = fright.getMaxY() - floatY; else if (subbox.getClearing() == CLEAR_BOTH) ny = Math.max(fleft.getMaxY(), fright.getMaxY()) - floatY;
                    if (stat.y < ny) {
                        stat.y = ny;
                        clearance = true;
                    }
                }
                if (subbox.isInFlow()) {
                    boolean boxempty = subbox.marginsAdjoin();
                    int borderY = stat.y;
                    if (stat.lastinflow != null) borderY -= stat.lastinflow.emargin.bottom;
                    if (subbox.emargin.top > mtop) mtop = subbox.emargin.top;
                    if (stat.firstseparated == null && separatedFromTop(this)) {
                        borderY += mtop;
                    }
                    if (stat.firstseparated != null) {
                        if (clearance) borderY += mtop + mbottom; else borderY += Math.max(mtop, mbottom);
                    }
                    stat.lastinflow = subbox;
                    if (!boxempty && stat.firstseparated == null) stat.firstseparated = subbox;
                    if (!boxempty) {
                        stat.lastseparated = subbox;
                        mtop = 0;
                        mbottom = subbox.emargin.bottom;
                    }
                    if (stat.lastseparated != null) {
                        if (subbox.emargin.bottom > mbottom) mbottom = subbox.emargin.bottom;
                    }
                    stat.y = borderY - subbox.margin.top;
                    layoutBlockInFlow(subbox, wlimit, stat);
                    if (subbox.getRest() != null) insertSubBox(i + 1, subbox.getRest());
                    nexty = stat.y;
                } else if (subbox.getFloating() == FLOAT_LEFT || subbox.getFloating() == FLOAT_RIGHT) {
                    layoutBlockFloating(subbox, wlimit, stat);
                } else {
                    layoutBlockPositioned(subbox, wlimit, stat);
                }
                stat.y = nexty;
            }
        }
        if (!separatedFromBottom(this)) {
            stat.y -= mbottom;
        }
        if (!hasFixedHeight()) {
            if (encloseFloats()) {
                int mfy = getFloatHeight() - floatY;
                if (mfy > stat.y) stat.y = mfy;
            }
            setContentHeight(stat.y);
            updateSizes();
            updateChildSizes();
        }
        setSize(totalWidth(), totalHeight());
    }

    protected void layoutBlockInFlow(BlockBox subbox, int wlimit, BlockLayoutStatus stat) {
        int newfloatXl = floatXl + subbox.margin.left + subbox.border.left + subbox.padding.left;
        int newfloatXr = floatXr + subbox.margin.right + subbox.border.right + subbox.padding.right;
        int newfloatY = floatY + subbox.margin.top + subbox.border.top + subbox.padding.top;
        subbox.setFloats(fleft, fright, newfloatXl, newfloatXr, stat.y + newfloatY);
        subbox.setPosition(0, stat.y);
        subbox.doLayout(wlimit, true, true);
        stat.y += subbox.getHeight();
        if (subbox.getWidth() > stat.maxw) stat.maxw = subbox.getWidth();
        int pref = subbox.getPreferredWidth();
        if (pref == -1) pref = subbox.getMaximalWidth();
        if (pref > stat.prefw) stat.prefw = pref;
    }

    /**
     * Calculates the position for a floating box in the given context.
     * @param subbox the box to be placed
     * @param wlimit the width limit for placing all the boxes
     * @param stat status of the layout that should be updated
     */
    protected void layoutBlockFloating(BlockBox subbox, int wlimit, BlockLayoutStatus stat) {
        subbox.setFloats(new FloatList(subbox), new FloatList(subbox), 0, 0, 0);
        subbox.doLayout(wlimit, true, true);
        FloatList f = (subbox.getFloating() == FLOAT_LEFT) ? fleft : fright;
        FloatList of = (subbox.getFloating() == FLOAT_LEFT) ? fright : fleft;
        int floatX = (subbox.getFloating() == FLOAT_LEFT) ? floatXl : floatXr;
        int oFloatX = (subbox.getFloating() == FLOAT_LEFT) ? floatXr : floatXl;
        int fy = stat.y + floatY;
        if (fy < f.getLastY()) fy = f.getLastY();
        int fx = f.getWidth(fy);
        if (fx < floatX) fx = floatX;
        if (fx == 0 && floatX < 0) fx = floatX;
        int ofx = of.getWidth(fy);
        if (ofx < oFloatX) ofx = oFloatX;
        if (ofx == 0 && oFloatX < 0) ofx = oFloatX;
        while ((fx > floatX || ofx > oFloatX || stat.inlineWidth > 0) && (stat.inlineWidth + fx - floatX + ofx - oFloatX + subbox.getWidth() > wlimit)) {
            int nexty1 = f.getNextY(fy);
            int nexty2 = of.getNextY(fy);
            if (nexty1 != -1 && nexty2 != -1) fy = Math.min(f.getNextY(fy), of.getNextY(fy)); else if (nexty2 != -1) fy = nexty2; else if (nexty1 != -1) fy = nexty1; else fy += Math.max(stat.maxh, getLineHeight());
            fx = f.getWidth(fy);
            if (fx < floatX) fx = floatX;
            if (fx == 0 && floatX < 0) fx = floatX;
            ofx = of.getWidth(fy);
            if (ofx < oFloatX) ofx = oFloatX;
            if (ofx == 0 && oFloatX < 0) ofx = oFloatX;
            stat.inlineWidth = 0;
        }
        subbox.setPosition(fx, fy);
        f.add(subbox);
        int floatw = maxFloatWidth(fy, fy + subbox.getHeight());
        if (floatw > stat.maxw) stat.maxw = floatw;
        if (stat.maxw > wlimit) stat.maxw = wlimit;
        int pref = subbox.getPreferredWidth();
        if (pref == -1) pref = wlimit;
        if (pref > stat.prefw) stat.prefw = pref;
    }

    protected void layoutBlockPositioned(BlockBox subbox, int wlimit, BlockLayoutStatus stat) {
        subbox.setFloats(new FloatList(subbox), new FloatList(subbox), 0, 0, 0);
        subbox.doLayout(wlimit, true, true);
    }

    @Override
    public void absolutePositions() {
        if (displayed) {
            int x = cblock.getAbsoluteContentX() + bounds.x;
            int y = cblock.getAbsoluteContentY() + bounds.y;
            if (floating == FLOAT_NONE) {
                if (position == POS_RELATIVE) {
                    x += leftset ? coords.left : (-coords.right);
                    y += topset ? coords.top : (-coords.bottom);
                } else if (position == POS_ABSOLUTE || position == POS_FIXED) {
                    if (topstatic || leftstatic) {
                        updateStaticPosition();
                    }
                    x = cblock.getAbsoluteBackgroundBounds().x + coords.left;
                    y = cblock.getAbsoluteBackgroundBounds().y + coords.top;
                }
            } else if (floating == FLOAT_LEFT) {
                BlockBox listowner = fown.getOwner();
                x = listowner.getAbsoluteContentX() + bounds.x;
                y = listowner.getAbsoluteContentY() + bounds.y;
            } else if (floating == FLOAT_RIGHT) {
                BlockBox listowner = fown.getOwner();
                x = listowner.getAbsoluteContentX() + listowner.getContentWidth() - bounds.width - bounds.x - 2;
                y = listowner.getAbsoluteContentY() + bounds.y;
            }
            absbounds.x = x;
            absbounds.y = y;
            absbounds.width = bounds.width;
            absbounds.height = bounds.height;
            if (isDisplayed()) {
                if (clipblock == viewport) viewport.updateBoundsFor(absbounds); else viewport.updateBoundsFor(getClippedBounds());
                for (int i = startChild; i < endChild; i++) getSubBox(i).absolutePositions();
            }
        }
    }

    /** Obtains the static position of an absolutely positioned box
     * from the reference box (if any) */
    private void updateStaticPosition() {
        if (absReference != null && cblock != null) {
            Rectangle ab = new Rectangle(absReference.getAbsoluteBounds());
            Rectangle cb = cblock.getAbsoluteBounds();
            ab.x = ab.x - cb.x;
            ab.y = ab.y - cb.y;
            if (topstatic) coords.top = ab.y + ab.height - 1 - cblock.emargin.top - cblock.border.top;
            if (leftstatic) coords.left = ab.x - cblock.emargin.left - cblock.border.left;
        } else {
            if (topstatic) coords.top = cblock.padding.top;
            if (leftstatic) coords.left = cblock.padding.left;
        }
    }

    @Override
    public int getAvailableContentWidth() {
        int ret = availwidth - margin.left - border.left - padding.left - padding.right - border.right - margin.right;
        if (max_size.width != -1 && ret > max_size.width) ret = max_size.width;
        return ret;
    }

    @Override
    public int getMinimalWidth() {
        int ret = 0;
        if (wset && !wrelative) ret = content.width; else ret = getMinimalContentWidth();
        ret += declMargin.left + padding.left + border.left + declMargin.right + padding.right + border.right;
        return ret;
    }

    /**
     * Computes the minimal width of the box content from the contained sub-boxes.
     * @return the minimal content width
     */
    protected int getMinimalContentWidth() {
        int ret = 0;
        for (int i = startChild; i < endChild; i++) {
            Box box = getSubBox(i);
            boolean affect = true;
            if (box instanceof BlockBox) {
                BlockBox block = (BlockBox) box;
                if (block.position == POS_ABSOLUTE || block.position == POS_FIXED) affect = false;
            }
            if (affect) {
                int w = box.getMinimalWidth();
                if (w > ret) ret = w;
            }
        }
        return ret;
    }

    @Override
    public int getMaximalWidth() {
        int ret;
        if (wset && !wrelative) ret = content.width; else ret = getMaximalContentWidth();
        ret += declMargin.left + padding.left + border.left + declMargin.right + padding.right + border.right;
        return ret;
    }

    /**
     * Computes the maximal width of the box content from the contained sub-boxes.
     * @return the maximal content width
     */
    protected int getMaximalContentWidth() {
        int sum = 0;
        int max = 0;
        for (int i = startChild; i < endChild; i++) {
            Box subbox = getSubBox(i);
            if (subbox.isBlock()) {
                BlockBox block = (BlockBox) subbox;
                if (block.getFloating() != BlockBox.FLOAT_NONE) {
                    sum += subbox.getMaximalWidth();
                } else if (!block.isInFlow()) {
                } else {
                    int sm = subbox.getMaximalWidth();
                    if (sm > max) max = sm;
                    if (sum > max) max = sum;
                    sum = 0;
                }
            } else {
                sum += subbox.getMaximalWidth();
            }
        }
        return Math.max(sum, max);
    }

    /**
     * Determines the minimal width as it is limited by the ancestors (for in-flow boxes)
     * or by the explicit setting of <code>width</code> or <code>min-width</code> properties.
     * @return the minimal width
     */
    public int getMinimalContentWidthLimit() {
        int ret;
        int dif = declMargin.left + padding.left + border.left + declMargin.right + padding.right + border.right;
        if (wset) ret = content.width; else if (min_size.width != -1) ret = min_size.width; else if (isInFlow()) ret = cblock.getMinimalContentWidthLimit() - dif; else ret = 0;
        return ret;
    }

    /**
     * @return true if the width is specified relatively
     */
    public boolean isRelative() {
        return wrelative;
    }

    @Override
    public boolean hasFixedWidth() {
        return wset || isInFlow();
    }

    @Override
    public boolean hasFixedHeight() {
        return hset;
    }

    public LengthSet getCoords() {
        return coords;
    }

    /**
     * Computes the maximal height of the floating boxes inside of this box and
     * all its children.
     * @return the total height of the floating boxes
     */
    public int getFloatHeight() {
        if (fleft != null && fright != null) {
            int mfy = Math.max(fleft.getMaxYForOwner(this), fright.getMaxYForOwner(this));
            if (this.containsBlocks()) {
                for (int i = 0; i < getSubBoxNumber(); i++) {
                    Box subbox = getSubBox(i);
                    if (subbox instanceof BlockBox) {
                        int cmfy = ((BlockBox) subbox).getFloatHeight();
                        if (cmfy > mfy) mfy = cmfy;
                    }
                }
            }
            return mfy;
        } else return 0;
    }

    @Override
    public void draw(Graphics2D g, int turn, int mode) {
        ctx.updateGraphics(g);
        if (isDisplayed() && isDeclaredVisible()) {
            Shape oldclip = g.getClip();
            g.setClip(clipblock.getAbsoluteContentBounds());
            int nestTurn = turn;
            switch(turn) {
                case DRAW_ALL:
                    if (mode == DRAW_BOTH || mode == DRAW_BG) drawBackground(g);
                    nestTurn = DRAW_ALL;
                    break;
                case DRAW_NONFLOAT:
                    if (floating == FLOAT_NONE) {
                        if (mode == DRAW_BOTH || mode == DRAW_BG) drawBackground(g);
                        nestTurn = DRAW_NONFLOAT;
                    }
                    break;
                case DRAW_FLOAT:
                    if (floating != FLOAT_NONE) {
                        if (mode == DRAW_BOTH || mode == DRAW_BG) drawBackground(g);
                        nestTurn = DRAW_ALL;
                    }
                    break;
            }
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                for (int i = startChild; i < endChild; i++) getSubBox(i).draw(g, nestTurn, mode);
            }
            g.setClip(oldclip);
        }
    }

    protected void loadBlockStyle() {
        floating = style.getProperty("float");
        if (floating == null) floating = FLOAT_NONE;
        clearing = style.getProperty("clear");
        if (clearing == null) clearing = CLEAR_NONE;
        position = style.getProperty("position");
        if (position == null) position = POS_STATIC;
        overflow = style.getProperty("overflow");
        if (overflow == null) overflow = OVERFLOW_VISIBLE;
        align = style.getProperty("text-align");
        if (align == null) align = ALIGN_LEFT;
        if (display == ElementBox.DISPLAY_NONE) {
            position = POS_STATIC;
            floating = FLOAT_NONE;
        } else if (position == POS_ABSOLUTE || position == POS_FIXED) {
            floating = FLOAT_NONE;
        }
    }

    /**
     * Loads the top, left, bottom and right coordinates from the style
     */
    protected void loadPosition() {
        CSSDecoder dec = new CSSDecoder(ctx);
        int contw = cblock.getContentWidth();
        int conth = cblock.getContentHeight();
        CSSProperty.Top ptop = style.getProperty("top");
        CSSProperty.Right pright = style.getProperty("right");
        CSSProperty.Bottom pbottom = style.getProperty("bottom");
        CSSProperty.Left pleft = style.getProperty("left");
        topset = !(ptop == null || ptop == CSSProperty.Top.AUTO);
        rightset = !(pright == null || pright == CSSProperty.Right.AUTO);
        bottomset = !(pbottom == null || pbottom == CSSProperty.Bottom.AUTO);
        leftset = !(pleft == null || pleft == CSSProperty.Left.AUTO);
        coords = new LengthSet();
        if (topset) coords.top = dec.getLength(getLengthValue("top"), (ptop == CSSProperty.Top.AUTO), 0, 0, conth);
        if (rightset) coords.right = dec.getLength(getLengthValue("right"), (pright == CSSProperty.Right.AUTO), 0, 0, contw);
        if (bottomset) coords.bottom = dec.getLength(getLengthValue("bottom"), (pbottom == CSSProperty.Bottom.AUTO), 0, 0, conth);
        if (leftset) coords.left = dec.getLength(getLengthValue("left"), (pleft == CSSProperty.Left.AUTO), 0, 0, contw);
    }

    /** Compute the total width of a block element according to the min-, max-,
     * width properties */
    protected int blockWidth() {
        return content.width;
    }

    /** Compute the height of a block element according to the min-, max-,
     * height properties */
    protected int blockHeight() {
        return content.height;
    }

    /** 
     * Returns maximal width of floats (left and right together) in 
     * a specified Y interval in this block.
     * @param y1 the starting Y value
     * @param y2 the end Y value
     * @return the maximal sum of the left and the right float width
     */
    protected int maxFloatWidth(int y1, int y2) {
        int ret = 0;
        for (int y = y1; y <= y2; y++) {
            int w = fleft.getWidth(y) + fright.getWidth(y);
            if (w > ret) ret = w;
        }
        return ret;
    }

    @Override
    protected void loadSizes() {
        loadSizes(false);
    }

    @Override
    public void updateSizes() {
        loadSizes(true);
    }

    /**
     * Load the sizes from CSS properties.
     * @param update Update mode after the size of the containing box has been updated.
     * The content size is not reset, the margins are recomputed.
     */
    protected void loadSizes(boolean update) {
        CSSDecoder dec = new CSSDecoder(ctx);
        if (cblock == null) {
            System.err.println(toString() + " has no cblock");
            return;
        }
        int contw = cblock.getContentWidth();
        int conth = cblock.getContentHeight();
        if (!update) loadBorders(dec, contw);
        loadPadding(dec, contw);
        loadPosition();
        if (!update) {
            content = new Dimension(0, 0);
            margin = new LengthSet();
            declMargin = new LengthSet();
        }
        loadWidthsHeights(dec, contw, conth, update);
        if (!update) emargin = new LengthSet(margin);
    }

    /**
     * Loads the padding sizes from the style.
     * 
     * @param dec CSS decoder used for decoding the style
     * @param contw containing block width for decoding percentages
     */
    protected void loadPadding(CSSDecoder dec, int contw) {
        padding = new LengthSet();
        padding.top = dec.getLength(getLengthValue("padding-top"), false, null, null, contw);
        padding.right = dec.getLength(getLengthValue("padding-right"), false, null, null, contw);
        padding.bottom = dec.getLength(getLengthValue("padding-bottom"), false, null, null, contw);
        padding.left = dec.getLength(getLengthValue("padding-left"), false, null, null, contw);
    }

    /**
     * Loads the widths and margins from the style.
     * 
     * @param dec CSS decoder used for decoding the style
     * @param contw containing block width for decoding percentages
     */
    protected void loadWidthsHeights(CSSDecoder dec, int contw, int conth, boolean update) {
        min_size = new Dimension(dec.getLength(getLengthValue("min-width"), false, -1, -1, contw), dec.getLength(getLengthValue("min-height"), false, -1, -1, conth));
        max_size = new Dimension(dec.getLength(getLengthValue("max-width"), false, -1, -1, contw), dec.getLength(getLengthValue("max-height"), false, -1, -1, conth));
        if (max_size.width != -1 && max_size.width < min_size.width) max_size.width = min_size.width;
        if (max_size.height != -1 && max_size.height < min_size.height) max_size.height = min_size.height;
        TermLengthOrPercent width = getLengthValue("width");
        computeWidths(width, style.getProperty("width") == CSSProperty.Width.AUTO, true, cblock, update);
        if (max_size.width != -1 && content.width > max_size.width) {
            width = getLengthValue("max-width");
            computeWidths(width, false, false, cblock, update);
        }
        if (min_size.width != -1 && content.width < min_size.width) {
            width = getLengthValue("min-width");
            computeWidths(width, false, false, cblock, update);
        }
        TermLengthOrPercent height = getLengthValue("height");
        computeHeights(height, style.getProperty("height") == CSSProperty.Height.AUTO, true, cblock, update);
        if (max_size.height != -1 && content.height > max_size.height) {
            height = getLengthValue("max-height");
            computeHeights(height, false, false, cblock, update);
        }
        if (min_size.height != -1 && content.height < min_size.height) {
            height = getLengthValue("min-height");
            computeHeights(height, false, false, cblock, update);
        }
    }

    /** 
     * Calculates widths and margins according to
     *  http://www.w3.org/TR/CSS21/visudet.html#Computing_widths_and_margins .
     * @param width the specified width or null for auto
     * @param exact true if this is the exact width, false when it's a max/min width
     * @param cblock containing block
     * @param wknown <code>true</code>, if the containing block width is known
     * @param update <code>true</code>, if we're just updating the size to a new containing block size
     */
    protected void computeWidths(TermLengthOrPercent width, boolean auto, boolean exact, BlockBox cblock, boolean update) {
        if (position == POS_ABSOLUTE) computeWidthsAbsolute(width, auto, exact, cblock.getContentWidth() + cblock.padding.left + cblock.padding.right, update); else computeWidthsInFlow(width, auto, exact, cblock.getContentWidth(), update);
    }

    protected void computeWidthsInFlow(TermLengthOrPercent width, boolean auto, boolean exact, int contw, boolean update) {
        CSSDecoder dec = new CSSDecoder(ctx);
        if (width == null) auto = true;
        boolean mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        boolean mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mright = getLengthValue("margin-right");
        preferredWidth = -1;
        if (!widthComputed) update = false;
        if (auto) {
            if (exact) wset = false;
            margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
            margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
            declMargin.left = margin.left;
            declMargin.right = margin.right;
            if (!update || isInFlow()) {
                content.width = contw - margin.left - border.left - padding.left - padding.right - border.right - margin.right;
                if (content.width < 0) content.width = 0;
            }
            preferredWidth = -1;
        } else {
            if (exact) {
                wset = true;
                wrelative = width.isPercentage();
            }
            content.width = dec.getLength(width, auto, 0, 0, contw);
            margin.left = dec.getLength(mleft, mleftauto, 0, 0, contw);
            margin.right = dec.getLength(mright, mrightauto, 0, 0, contw);
            declMargin.left = margin.left;
            declMargin.right = margin.right;
            boolean prefer = !width.isPercentage();
            int prefml = (mleft == null) || mleft.isPercentage() || mleftauto ? 0 : margin.left;
            int prefmr = (mright == null) || mright.isPercentage() || mrightauto ? 0 : margin.right;
            if (prefer) preferredWidth = prefml + border.left + padding.left + content.width + padding.right + border.right + prefmr;
            if (isInFlow() && prefer) {
                if (mleftauto && mrightauto) {
                    int rest = contw - content.width - border.left - padding.left - padding.right - border.right;
                    if (rest < 0) rest = 0;
                    margin.left = (rest + 1) / 2;
                    margin.right = rest / 2;
                } else if (mleftauto) {
                    margin.left = contw - content.width - border.left - padding.left - padding.right - border.right - margin.right;
                } else if (mrightauto) {
                    margin.right = contw - content.width - border.left - padding.left - padding.right - border.right - margin.left;
                    if (margin.right < 0 && cblock.canIncreaseWidth()) margin.right = 0;
                } else {
                    margin.right = contw - content.width - border.left - padding.left - padding.right - border.right - margin.left;
                    if (margin.right < 0 && cblock.canIncreaseWidth()) margin.right = 0;
                }
            }
        }
    }

    protected void computeWidthsAbsolute(TermLengthOrPercent width, boolean auto, boolean exact, int contw, boolean update) {
        CSSDecoder dec = new CSSDecoder(ctx);
        if (width == null) auto = true;
        boolean mleftauto = style.getProperty("margin-left") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mleft = getLengthValue("margin-left");
        boolean mrightauto = style.getProperty("margin-right") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mright = getLengthValue("margin-right");
        preferredWidth = -1;
        if (!widthComputed) update = false;
        if (auto) {
            if (exact) wset = false;
            if (!update) content.width = dec.getLength(width, auto, 0, 0, contw);
            preferredWidth = -1;
        } else {
            if (exact) {
                wset = true;
                wrelative = width.isPercentage();
            }
            content.width = dec.getLength(width, auto, 0, 0, contw);
        }
        int constr = 0;
        if (wset) constr++;
        if (leftset) constr++;
        if (rightset) constr++;
        if (constr < 3) {
            if (mleftauto) margin.left = 0; else margin.left = dec.getLength(mleft, false, 0, 0, contw);
            if (mrightauto) margin.right = 0; else margin.right = dec.getLength(mright, false, 0, 0, contw);
        } else {
            if (mleftauto && mrightauto) {
                int rest = contw - coords.left - coords.right - border.left - border.right - padding.left - padding.right - content.width;
                margin.left = (rest + 1) / 2;
                margin.right = rest / 2;
            } else if (mleftauto) {
                margin.right = dec.getLength(mright, false, 0, 0, contw);
                margin.left = contw - coords.right - border.left - border.right - padding.left - padding.right - content.width - margin.right;
            } else if (mrightauto) {
                margin.left = dec.getLength(mright, false, 0, 0, contw);
                margin.right = contw - coords.right - border.left - border.right - padding.left - padding.right - content.width - margin.left;
            } else {
                margin.left = dec.getLength(mleft, false, 0, 0, contw);
                margin.right = dec.getLength(mright, false, 0, 0, contw);
            }
        }
        declMargin.left = margin.left;
        declMargin.right = margin.right;
        if (!leftset && !rightset) {
            leftstatic = true;
            coords.right = contw - coords.left - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
        } else if (!leftset) {
            coords.left = contw - coords.right - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
        } else if (!rightset) {
            coords.right = contw - coords.left - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
        } else {
            if (auto) content.width = contw - coords.left - coords.right - border.left - border.right - padding.left - padding.right - margin.left - margin.right; else coords.right = contw - coords.left - border.left - border.right - padding.left - padding.right - content.width - margin.left - margin.right;
        }
    }

    /** 
     * Calculates heights and margins according to
     *  http://www.w3.org/TR/CSS21/visudet.html#Computing_heights_and_margins
     * @param height the specified width
     * @param exact true if this is the exact height, false when it's a max/min height
     * @param contw containing block width
     * @param conth containing block height
     * @param wknown <code>true</code>, if the containing block width is known
     * @param update <code>true</code>, if we're just updating the size to a new containing block size
     */
    protected void computeHeights(TermLengthOrPercent height, boolean auto, boolean exact, BlockBox cblock, boolean update) {
        if (position == POS_ABSOLUTE) {
            int contw = cblock.getContentWidth() + cblock.padding.left + cblock.padding.right;
            int conth = cblock.getContentHeight() + cblock.padding.top + cblock.padding.bottom;
            computeHeightsAbsolute(height, auto, exact, contw, conth, update);
        } else computeHeightsInFlow(height, auto, exact, cblock.getContentWidth(), cblock.getContentHeight(), update);
        declMargin.top = margin.top;
        declMargin.bottom = margin.bottom;
    }

    protected void computeHeightsInFlow(TermLengthOrPercent height, boolean auto, boolean exact, int contw, int conth, boolean update) {
        CSSDecoder dec = new CSSDecoder(ctx);
        if (height == null) auto = true;
        boolean mtopauto = style.getProperty("margin-top") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mtop = getLengthValue("margin-top");
        boolean mbottomauto = style.getProperty("margin-bottom") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mbottom = getLengthValue("margin-bottom");
        if (cblock != null && cblock.hset) {
            hset = (exact && !auto && height != null);
            if (!update) content.height = dec.getLength(height, auto, 0, 0, conth);
        } else {
            hset = (exact && !auto && height != null && !height.isPercentage());
            if (!update) content.height = dec.getLength(height, auto, 0, 0, 0);
        }
        if (mtopauto) margin.top = 0; else margin.top = dec.getLength(mtop, false, 0, 0, contw);
        if (mbottomauto) margin.bottom = 0; else margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
    }

    protected void computeHeightsAbsolute(TermLengthOrPercent height, boolean auto, boolean exact, int contw, int conth, boolean update) {
        CSSDecoder dec = new CSSDecoder(ctx);
        if (height == null) auto = true;
        boolean mtopauto = style.getProperty("margin-top") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mtop = getLengthValue("margin-top");
        boolean mbottomauto = style.getProperty("margin-bottom") == CSSProperty.Margin.AUTO;
        TermLengthOrPercent mbottom = getLengthValue("margin-bottom");
        if (cblock != null && cblock.hset) {
            hset = (exact && !auto && height != null);
            if (!update) content.height = dec.getLength(height, auto, 0, 0, conth);
        } else {
            hset = (exact && !auto && height != null && !height.isPercentage());
            if (!update) content.height = dec.getLength(height, auto, 0, 0, 0);
        }
        int constr = 0;
        if (hset) constr++;
        if (topset) constr++;
        if (bottomset) constr++;
        if (constr < 3) {
            if (mtopauto) margin.top = 0; else margin.top = dec.getLength(mtop, false, 0, 0, contw);
            if (mbottomauto) margin.bottom = 0; else margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
        } else {
            if (mtopauto && mbottomauto) {
                int rest = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - content.height;
                margin.top = (rest + 1) / 2;
                margin.bottom = rest / 2;
            } else if (mtopauto) {
                margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
                margin.top = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - content.height - margin.bottom;
            } else if (mbottomauto) {
                margin.top = dec.getLength(mtop, false, 0, 0, contw);
                margin.bottom = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - content.height - margin.top;
            } else {
                margin.top = dec.getLength(mtop, false, 0, 0, contw);
                margin.bottom = dec.getLength(mbottom, false, 0, 0, contw);
            }
        }
        if (!topset && !bottomset) {
            topstatic = true;
            coords.bottom = conth - coords.top - border.top - border.bottom - padding.top - padding.bottom - margin.top - margin.bottom - content.height;
        } else if (!topset) {
            coords.top = conth - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - margin.top - margin.bottom - content.height;
        } else if (!bottomset) {
            coords.bottom = conth - coords.top - border.top - border.bottom - padding.top - padding.bottom - margin.top - margin.bottom - content.height;
        } else {
            if (auto) content.height = conth - coords.top - coords.bottom - border.top - border.bottom - padding.top - padding.bottom - margin.top - margin.bottom; else coords.bottom = conth - coords.top - border.top - border.bottom - padding.top - padding.bottom - margin.top - margin.bottom - content.height;
        }
    }

    /**
     * Re-calculates the sizes of all the child block boxes recursively.
     */
    public void updateChildSizes() {
        for (int i = 0; i < getSubBoxNumber(); i++) {
            Box child = getSubBox(i);
            if (child instanceof BlockBox) {
                BlockBox block = (BlockBox) child;
                int oldw = block.getContentWidth();
                int oldh = block.getContentHeight();
                block.updateSizes();
                block.setSize(block.totalWidth(), block.totalHeight());
                if (block.getContentWidth() != oldw || block.getContentHeight() != oldh) {
                    block.updateChildSizes();
                }
            }
        }
    }

    /**
     * Checks if the box content is separated from the top margin by a border or padding.
     */
    private boolean separatedFromTop(ElementBox box) {
        return (box.border.top > 0 || box.padding.top > 0);
    }

    /**
     * Checks if the box content is separated from the bottom margin by a border or padding.
     */
    private boolean separatedFromBottom(ElementBox box) {
        return (box.border.bottom > 0 || box.padding.bottom > 0);
    }

    /**
     * Remove the previously splitted child boxes
     */
    private void clearSplitted() {
        for (Iterator<Box> it = nested.iterator(); it.hasNext(); ) {
            Box box = it.next();
            if (box.splitted) {
                it.remove();
                endChild--;
            }
        }
    }
}

/**
 * A class describing the status of the block box layout
 */
class BlockLayoutStatus {

    /** width of inline boxes currently placed on the line */
    public int inlineWidth;

    /** current <em>y</em> coordinate relatively to the content box */
    public int y;

    /** maximal width of the boxes laid out */
    public int maxw;

    /** preferred width of the boxes laid out */
    public int prefw;

    /** maximal height of the boxes laid out on current line */
    public int maxh;

    /** first placed non-empty box for collapsing margins */
    public BlockBox firstseparated;

    /** last placed non-empty box for collapsing margins */
    public BlockBox lastseparated;

    /** last placed in-flow box for collapsing margins */
    public BlockBox lastinflow;

    /** Creates a new initialized layout status */
    public BlockLayoutStatus() {
        inlineWidth = 0;
        y = 0;
        maxw = 0;
        prefw = 0;
        maxh = 0;
        firstseparated = null;
        lastseparated = null;
        lastinflow = null;
    }
}
