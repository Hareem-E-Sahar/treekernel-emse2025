package uk.co.westhawk.tablelayout;

import java.lang.*;
import java.awt.*;
import java.util.*;

/**
 * TableLayout is a layout manager which allows components to be
 * arranged
 * in a tabular form. The TableLayout component has a layout
 * resource which is used to specify the column and row position of each
 * component. Components can span rows and/or columns.  Layout options
 * are available to control the initial sizes, justification, and dynamic
 * resizing
 *
 * @author Birgit Arkesteijn
 * @version $Revision: 1.1 $ $Date: 2004/05/27 16:06:08 $
 */
public class TableLayout extends Object implements LayoutManager {

    private static final String version_id = "@(#)$Id: TableLayout.java,v 1.1 2004/05/27 16:06:08 lasmith Exp $ Copyright West Consulting bv";

    boolean force_shrink;

    int col_spacing;

    int row_spacing;

    TableLoc layout;

    Vector same_width;

    Vector same_height;

    int margin_width;

    int margin_height;

    TableCol cols;

    TableRow rows;

    /**
 *
 */
    public TableLayout() {
        layout = new TableLoc();
        force_shrink = true;
        col_spacing = 1;
        row_spacing = 1;
        same_width = new Vector();
        same_height = new Vector();
        margin_width = 0;
        margin_height = 0;
        cols = (TableCol) null;
        rows = (TableRow) null;
    }

    /**
 * Adds the specified component with the layout string to the layout
 *
 * @param layout represents the wanted layout of the component
 * @param comp the component to be added
 * @see uk.co.westhawk.tablelayout.TableLocRec
 */
    public void addLayoutComponent(String name, Component comp) {
        try {
            TableLocRec rec = new TableLocRec(name);
            layout.addElement(rec, comp);
        } catch (Exception e) {
            System.out.println("TableLayout: Syntax error in component: " + name);
            return;
        }
    }

    /**
 * Removes the specified component from the layout
 *
 * @param comp the component to be removed
 */
    public void removeLayoutComponent(Component comp) {
        int index;
        layout.removeElement(comp);
        if ((index = same_width.indexOf(comp)) > -1) {
            same_width.removeElementAt(index);
        }
        if ((index = same_height.indexOf(comp)) > -1) {
            same_height.removeElementAt(index);
        }
    }

    /**
 * Calculates the preferred size dimensions for the specified panel,
 * given the components in the specified target container
 *
 * @param target the component to be laid out
 */
    public Dimension preferredLayoutSize(Container target) {
        int width, height;
        width = 0;
        height = 0;
        Insets insets = target.getInsets();
        int nmembers = target.getComponentCount();
        if (nmembers > 0) {
            for (int i = 0; i < nmembers; i++) {
                Component current = target.getComponent(i);
                TableLocRec rec = layout.element(current);
                if (rec == null) break;
                rec.orig_width = current.getPreferredSize().width;
                rec.orig_height = current.getPreferredSize().height;
                rec.same_width = 0;
                rec.same_height = 0;
            }
            considerSameWidth();
            considerSameHeight();
            cols = new TableCol(this);
            rows = new TableRow(this);
            width = cols.getPreferredSize() + insets.left + insets.right;
            height = rows.getPreferredSize() + insets.top + insets.bottom;
        }
        return new Dimension(width, height);
    }

    /**
 * Calculates the minimum size dimensions for the specified panel,
 * given the components in the specified target container
 *
 * @param target the component to be laid out
 */
    public Dimension minimumLayoutSize(Container target) {
        int width, height;
        width = 0;
        height = 0;
        Insets insets = target.getInsets();
        int nmembers = target.getComponentCount();
        if (nmembers > 0) {
            for (int i = 0; i < nmembers; i++) {
                Component current = target.getComponent(i);
                TableLocRec rec = layout.element(current);
                if (rec == null) break;
                rec.orig_width = current.getMinimumSize().width;
                rec.orig_height = current.getMinimumSize().height;
                rec.same_width = 0;
                rec.same_height = 0;
            }
            considerSameWidth();
            considerSameHeight();
            cols = new TableCol(this);
            rows = new TableRow(this);
            width = cols.totalSize() + insets.left + insets.right;
            height = rows.totalSize() + insets.top + insets.bottom;
        }
        return new Dimension(width, height);
    }

    /**
 * Lays out the container in the specified panel
 *
 * @param target the component to be laid out
 */
    public void layoutContainer(Container target) {
        Dimension dim = target.getSize();
        Insets insets = target.getInsets();
        int nmembers = target.getComponentCount();
        if (nmembers > 0) {
            for (int i = 0; i < nmembers; i++) {
                Component current = target.getComponent(i);
                TableLocRec rec = layout.element(current);
                if (rec == null) break;
                current.doLayout();
                rec.orig_width = current.getPreferredSize().width;
                rec.orig_height = current.getPreferredSize().height;
                rec.same_width = 0;
                rec.same_height = 0;
            }
            considerSameWidth();
            considerSameHeight();
            cols = new TableCol(this);
            rows = new TableRow(this);
            cols.minimize();
            rows.minimize();
            tableMakeColsFitWidth(dim.width - insets.left - insets.right);
            tableMakeRowsFitHeight(dim.height - insets.top - insets.bottom);
            tableSetGeometryOfChildren(insets);
        }
    }

    /**
 * This resource is used to specify the names of components
 * which will be constrained to remain the same width as
 * the table shrinks and grows
 *
 * @param v the vector of component with the same width
 */
    public void sameWidth(Vector v) {
        same_width.addElement(v);
    }

    /**
 * This resource is used to specify the names of components
 * which will be constrained to remain the same heigth as
 * the table shrinks and grows
 *
 * @param v the vector of component with the same heigth
 */
    public void sameHeight(Vector v) {
        same_height.addElement(v);
    }

    /**
 * The minimum spacing between the left and right edges of the
 * components in the Container
 *
 * @param i the spacing
 */
    public void marginWidth(int i) {
        margin_width = i;
    }

    /**
 * The minimum spacing between the top and bottom edges of the
 * components in the Container
 *
 * @param i the spacing
 */
    public void marginHeight(int i) {
        margin_height = i;
    }

    /**
 * Specifies if components should be made smaller than their "preferred"
 * sizes.
 * The TableLayout component tries to respect the preferred geometries of
 * its components.
 *
 * Components which are locked using options including any of
 * "whWH" will continue to be excluded from
 * stretching, but others will be
 * stretched and then can be shrunk back to their initial preferred sizes
 * from the time they were last managed.
 * When the table is shrunk further, all
 * components are shrunk an equal number of pixels until they are of size 1
 * (the smallest legal size of a Components).
 *
 * By default, this resource is <em>true</em>.
 * @param force  boolean to indicate shrink should be forced
 * @see uk.co.westhawk.tablelayout.TableOpts
 */
    public void forceShrink(boolean force) {
        force_shrink = force;
    }

    /**
 * Specifies the number of pixels between columns
 *
 * @param sp the spacing between columns
 */
    public void columnSpacing(int sp) {
        col_spacing = sp;
    }

    /**
 * Specifies the number of pixels between rows
 *
 * @param sp the spacing between rows
 */
    public void rowSpacing(int sp) {
        row_spacing = sp;
    }

    private void considerSameWidth() {
        int nlist = same_width.size();
        for (int ind1 = 0; ind1 < nlist; ind1++) {
            Vector compv = (Vector) same_width.elementAt(ind1);
            int max = 0;
            int ncomp = compv.size();
            for (int ind2 = 0; ind2 < ncomp; ind2++) {
                Component comp = (Component) compv.elementAt(ind2);
                TableLocRec rec = layout.element(comp);
                if (rec == null) break;
                if (rec.orig_width > max) max = rec.orig_width;
            }
            for (int ind2 = 0; ind2 < ncomp; ind2++) {
                Component comp = (Component) compv.elementAt(ind2);
                TableLocRec rec = layout.element(comp);
                if (rec == null) break;
                rec.same_width = max;
            }
        }
    }

    private void considerSameHeight() {
        int nlist = same_height.size();
        for (int ind1 = 0; ind1 < nlist; ind1++) {
            Vector compv = (Vector) same_height.elementAt(ind1);
            int max = 0;
            int ncomp = compv.size();
            for (int ind2 = 0; ind2 < ncomp; ind2++) {
                Component comp = (Component) compv.elementAt(ind2);
                TableLocRec rec = layout.element(comp);
                if (rec == null) break;
                if (rec.orig_height > max) max = rec.orig_height;
            }
            for (int ind2 = 0; ind2 < ncomp; ind2++) {
                Component comp = (Component) compv.elementAt(ind2);
                TableLocRec rec = layout.element(comp);
                if (rec == null) break;
                rec.same_height = max;
            }
        }
    }

    private void tableMakeColsFitWidth(int width) {
        int change, current, prefer;
        current = cols.totalSize();
        prefer = cols.getPreferredSize();
        if (width < prefer && force_shrink == false) {
            change = prefer - current;
        } else {
            change = width - current;
        }
        if (change != 0) cols.adjust(change);
    }

    private void tableMakeRowsFitHeight(int height) {
        int change, current, prefer;
        current = rows.totalSize();
        prefer = rows.getPreferredSize();
        if (height < prefer && force_shrink == false) {
            change = prefer - current;
        } else {
            change = height - current;
        }
        if (change != 0) rows.adjust(change);
    }

    private void tableSetGeometryOfChildren(Insets insets) {
        TableLocRec rec;
        Component comp;
        int sz, index;
        if (layout == (TableLoc) null || cols == (TableCol) null || rows == (TableRow) null) return;
        cols.computeOffsets(insets.left + margin_width, col_spacing);
        rows.computeOffsets(insets.top + margin_height, row_spacing);
        sz = layout.size();
        for (index = 0; index < sz; index++) {
            rec = layout.recElementAt(index);
            comp = layout.compElementAt(index);
            TableComputeChildPosition(rec, comp);
        }
    }

    private void TableComputeChildPosition(TableLocRec rec, Component comp) {
        int cell_w, cell_h;
        int cell_x, x;
        int cell_y, y;
        int width, prefer, height;
        int i, pad;
        pad = col_spacing;
        cell_w = -pad;
        for (i = 0; i < rec.col_span; i++) cell_w += cols.elementAt(rec.col + i).value + pad;
        prefer = rec.preferredWidth();
        if (rec.options.W && cell_w > prefer) {
            width = prefer;
        } else {
            width = cell_w;
        }
        if (width <= 0) width = 1;
        pad = row_spacing;
        cell_h = -pad;
        for (i = 0; i < rec.row_span; i++) cell_h += rows.elementAt(rec.row + i).value + pad;
        prefer = rec.preferredHeight();
        if (rec.options.H && cell_h > prefer) {
            height = prefer;
        } else {
            height = cell_h;
        }
        if (height <= 0) height = 1;
        cell_x = cols.elementAt(rec.col).offset;
        if (rec.options.l) x = cell_x; else if (rec.options.r) x = cell_x + cell_w - width; else x = cell_x + (cell_w - width) / 2;
        cell_y = rows.elementAt(rec.row).offset;
        if (rec.options.t) y = cell_y; else if (rec.options.b) y = cell_y + cell_h - height; else y = cell_y + (cell_h - height) / 2;
        comp.setSize(width, height);
        Point p = comp.getLocation();
        if (x != p.x || y != p.y) {
            comp.setLocation(x, y);
        }
    }

    /**
 * Returns the String representation
 */
    public String toString() {
        String colsStr, rowsStr;
        if (cols != null) {
            colsStr = cols.toString();
        } else {
            colsStr = "null";
        }
        if (rows != null) {
            rowsStr = rows.toString();
        } else {
            rowsStr = "null";
        }
        return ("TableLayout [" + "\nlayout: " + layout.toString() + "\ncols: " + colsStr + "\nrows: " + rowsStr + "\nsame_width: " + same_width.toString() + "\nsame_height: " + same_height.toString() + "\ncol_spacing: " + col_spacing + "\nrow_spacing: " + row_spacing + "\nforce_shrink: " + force_shrink + "\nmargin_width: " + margin_width + "\nmargin_height: " + margin_height + " ]");
    }

    /**
 * Creates a clone of the object. A new instance is allocated and all
 * the variables of the class are cloned
 */
    public Object clone() {
        TableLayout elem = new TableLayout();
        elem.force_shrink = force_shrink;
        elem.col_spacing = col_spacing;
        elem.row_spacing = row_spacing;
        elem.layout = (TableLoc) layout.clone();
        elem.same_width = (Vector) same_width.clone();
        elem.same_height = (Vector) same_height.clone();
        elem.margin_width = margin_width;
        elem.margin_height = margin_height;
        elem.considerSameWidth();
        elem.considerSameHeight();
        elem.cols = new TableCol(this);
        elem.rows = new TableRow(this);
        return ((Object) elem);
    }
}
