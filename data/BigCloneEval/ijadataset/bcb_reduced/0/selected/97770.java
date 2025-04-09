package com.gargoylesoftware.base.gui;

import com.gargoylesoftware.base.trace.Trace;
import com.gargoylesoftware.base.trace.TraceChannel;
import com.gargoylesoftware.base.util.DetailedIllegalArgumentException;
import com.gargoylesoftware.base.util.DetailedNullPointerException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingConstants;

/**
 *  The TableLayout lays out items based on a table of rows and columns.<p>
 *
 * If you are doing simple layout, you can specify constraints as strings of the format
 * "row,column".  If you want the component to stretch over multiple rows/columns
 * then you can specify the constraint as "row+rowspan,column+columnspan" as shown in
 * the sample below.
 * <pre>
 * final TableLayout layout = new TableLayout();
 * final JPanel panel = new JPanel(layout);
 *
 * panel.add( new JLabel("squirrel"), "1,1" );
 * panel.add( new JLabel("raccoon"), "1,2" );
 * panel.add( new JLabel("bluejay"), "2,1" );
 * panel.add( new JLabel("goldfish"), "2,2" );
 * panel.add( new JLabel("marshhawk"), "3,1+3" );
 * </pre>
 *
 * If you want more flexibility over the layout then this, use a {@link TableLayoutConstraints}
 * object instead of a string.  Here is a more complicated sample that uses
 * {@link TableLayoutConstraints} to customize the layout a bit more.  Note the use of
 * {@link TableLayoutDebuggingPanel} - this will draw lines on layout boundaries to help
 * debug layout problems.
 * <pre>
 * final TableLayout layout = new TableLayout();
 * final JPanel panel = new TableLayoutDebuggingPanel(layout);
 *
 * TableLayoutConstraints constraints;
 *
 * layout.setRowExpandable(1, true);
 *
 * constraints = new TableLayoutConstraints(1,1);
 * constraints.setVerticalStretch(true);
 * panel.add( new JButton("squirrel"), constraints );
 *
 * constraints = new TableLayoutConstraints(1,2);
 * constraints.setVerticalAlignment(TableLayout.TOP);
 * panel.add( new JButton("raccoon"), constraints );
 *
 * panel.add( new JButton("bluejay"), "2,1" );
 * panel.add( new JButton("goldfish"), "2,2" );
 * panel.add( new JButton("marshhawk"), "3,1+3" );
 * </pre>
 *
 *  <b>Debugging tip: </b>Most layout problems become obvious if you use a
 * {@link TableLayoutDebuggingPanel} to see where the layout boundaries are.  In those
 * rare cases where this doesn't give you enough information, try calling
 * {@link #setTraceChannel(TraceChannel)} with a non-null TraceChannel such as Trace.out
 * or Trace.err.  This will dump quite a bit of diagnostic information.
 * <pre>
 * layout.setTraceChannel(Trace.out)
 * </pre>
 *
 * @version    $Revision: 1.5 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public class TableLayout implements LayoutManager2, SwingConstants, Serializable {

    private static final long serialVersionUID = 396191633848670929L;

    private final Set rowHeaderPermanentInfos_ = new HashSet();

    private final Set columnHeaderPermanentInfos_ = new HashSet();

    private Container parent_;

    private final List constraints_ = new ArrayList();

    private TraceChannel traceChannel_ = null;

    private Header tempColumnHeaders_[] = null;

    private Header tempRowHeaders_[] = null;

    private int columnCount_ = 0;

    private int rowCount_ = 0;

    private boolean tempSizesAreValid_ = false;

    private int verticalAlignment_ = CENTER;

    private int horizontalAlignment_ = CENTER;

    private Dimension minimumSize_ = null;

    private Dimension maximumSize_ = null;

    private Dimension preferredSize_ = null;

    private Dimension actualSize_ = null;

    private boolean ignoreInvisibleComponents_ = true;

    /**
     *  Create a new TableLayout.
     */
    public TableLayout() {
    }

    /**
     *  Convenience method to create a string from a Dimension object.
     *
     * @param  dimension  Description of Parameter
     * @return            Description of the Returned Value
     */
    private static String toString(final Dimension dimension) {
        return "(" + dimension.width + "," + dimension.height + ")";
    }

    /**
     *  Set the vertical alignment. Legal values are:
     *  <ul>
     *    <li> <tt>TableLayout.TOP</tt>
     *    <li> <tt>TableLayout.CENTER</tt>
     *    <li> <tt>TableLayout.BOTTOM</tt>
     *  </ul>
     *
     *
     * @param  alignment  The new vertical alignment.
     */
    public void setVerticalAlignment(final int alignment) {
        Trace.println(traceChannel_, "setVerticalAlignment(" + alignment + ")");
        switch(alignment) {
            case TableLayout.TOP:
            case TableLayout.BOTTOM:
            case TableLayout.CENTER:
                verticalAlignment_ = alignment;
                break;
            default:
                throw new DetailedIllegalArgumentException("alignment", new Integer(alignment));
        }
    }

    /**
     *  Set the vertical alignment. Legal values are:
     *  <ul>
     *    <li> <tt>TableLayout.LEFT</tt>
     *    <li> <tt>TableLayout.CENTER</tt>
     *    <li> <tt>TableLayout.RIGHT</tt>
     *  </ul>
     *
     *
     * @param  alignment  The new horizontal alignment.
     */
    public void setHorizontalAlignment(final int alignment) {
        Trace.println(traceChannel_, "setHorizontalAlignment()");
        switch(alignment) {
            case TableLayout.LEFT:
            case TableLayout.RIGHT:
            case TableLayout.CENTER:
                horizontalAlignment_ = alignment;
                break;
            default:
                throw new DetailedIllegalArgumentException("alignment", new Integer(alignment));
        }
    }

    /**
     *  Set the minimum row height for a specific row.
     *
     * @param  index  The row that we are setting the height for.
     * @param  size   The new minimum height.
     */
    public void setMinimumRowHeight(final int index, final int size) {
        final HeaderPermanentInfo info = getPermanentInfo(rowHeaderPermanentInfos_, index, true);
        info.setMin(size);
    }

    /**
     *  Set the minimum column width for a specific column.
     *
     * @param  index  The column that we are setting the width for.
     * @param  size   The new width.
     */
    public void setMinimumColumnWidth(final int index, final int size) {
        final HeaderPermanentInfo info = getPermanentInfo(columnHeaderPermanentInfos_, index, true);
        info.setMin(size);
    }

    /**
     *  Set whether this row can be expanded beyond its preferred size.
     *
     * @param  index         The row index.
     * @param  isExpandable  true if the row is to be expandable.
     */
    public void setRowExpandable(final int index, final boolean isExpandable) {
        final HeaderPermanentInfo info = getPermanentInfo(rowHeaderPermanentInfos_, index, true);
        info.setExpandable(isExpandable);
    }

    /**
     *  Set whether this column can be expanded beyond its preferred size.
     *
     * @param  index         The column index.
     * @param  isExpandable  true if the column is to be expandable.
     */
    public void setColumnExpandable(final int index, final boolean isExpandable) {
        final HeaderPermanentInfo info = getPermanentInfo(columnHeaderPermanentInfos_, index, true);
        info.setExpandable(isExpandable);
    }

    /**
     *  Set the trace channel used for printing diagnostic information. If the
     *  channel is null then no tracing will be done.
     *
     * @param  channel  The new trace channel.
     */
    public void setTraceChannel(final TraceChannel channel) {
        traceChannel_ = channel;
    }

    /**
     *  Set whether or not we should ignore an components that are not visible.
     *
     * @param  ignore  True if we should ignore them.
     */
    public void setIgnoreInvisibleComponents(final boolean ignore) {
        ignoreInvisibleComponents_ = ignore;
    }

    /**
     *  I don't really understand what this method is supposed to return so I
     *  always return 0F. If you can explain this one to me then send me an
     *  email at mbowler@GargoyleSoftware.com and I'll fix this method up
     *  appropriately.
     *
     * @param  target  The container that this layout is managing.
     * @return         Zero.
     */
    public float getLayoutAlignmentX(final Container target) {
        return 0F;
    }

    /**
     *  I don't really understand what this method is supposed to return so I
     *  always return 0F. If you can explain this one to me then send me an
     *  email at mbowler@GargoyleSoftware.com and I'll fix this method up
     *  appropriately.
     *
     * @param  target  The container that this layout is managing.
     * @return         Zero.
     */
    public float getLayoutAlignmentY(final Container target) {
        return 0F;
    }

    /**
     *  Return the vertical alignment.
     *
     * @return    The vertical alignment.
     * @see       #setVerticalAlignment(int)
     */
    public int getVerticalAlignment() {
        return verticalAlignment_;
    }

    /**
     *  Return the horizontal alignment.
     *
     * @return    The horizontal alignment.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment_;
    }

    /**
     *  Return true if this row can be expanded beyond its preferred size. The
     *  default is false.
     *
     * @param  index  The row index
     * @return        true if the specified row is expandable.
     */
    public boolean isRowExpandable(final int index) {
        final HeaderPermanentInfo info = getPermanentInfo(rowHeaderPermanentInfos_, index, false);
        final boolean isExpandable;
        if (info == null) {
            isExpandable = false;
        } else {
            isExpandable = info.isExpandable();
        }
        return isExpandable;
    }

    /**
     *  Return true if this column can be expanded beyond its preferred size.
     *  The default is false.
     *
     * @param  index  The column.
     * @return        true if the column is expandable.
     */
    public boolean isColumnExpandable(final int index) {
        final HeaderPermanentInfo info = getPermanentInfo(columnHeaderPermanentInfos_, index, false);
        final boolean isExpandable;
        if (info == null) {
            isExpandable = false;
        } else {
            isExpandable = info.isExpandable();
        }
        return isExpandable;
    }

    /**
     *  Return the trace channel.
     *
     * @return    The trace channel or null if one wasn't set.
     */
    public TraceChannel getTraceChannel() {
        return traceChannel_;
    }

    /**
     *  Get whether or not we should ignore an components that are not visible.
     *
     * @return    True if we should ignore them.
     */
    public boolean getIgnoreInvisibleComponents() {
        return ignoreInvisibleComponents_;
    }

    /**
     *  Add the specified component to the layout with the specified
     *  constraints. This method should never be called as Container.addImpl()
     *  should call addLayoutComponent(Component,Object) instead. Not
     *  implemented.
     *
     * @param  name                            The constraints string.
     * @param  comp                            the component that is being
     *      added.
     * @throws  UnsupportedOperationException  If called.
     */
    public void addLayoutComponent(final String name, final Component comp) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Use addLayoutComponent(Component,Object)");
    }

    /**
     *  Add the specified component to the layout with the specified
     *  constraints. Throw an IllegalArgumentException if the same component is
     *  specified twice. The constraints object can be either an instance of
     *  TableLayoutConstraints or a String. If it is a string then an instance
     *  of TableLayoutConstraints will be created with the method
     *  TableLayoutConstraints.makeConstraints(String).
     *
     * @param  comp         The component that is being added.
     * @param  constraints  The constraints object.
     * @see                 TableLayoutConstraints#makeConstraints(String)
     */
    public void addLayoutComponent(final Component comp, final Object constraints) {
        assertNotNull("comp", comp);
        assertNotNull("constraints", constraints);
        TableLayoutConstraints tableLayoutConstraints = null;
        if (constraints instanceof TableLayoutConstraints) {
            tableLayoutConstraints = (TableLayoutConstraints) constraints;
        } else if (constraints instanceof String) {
            tableLayoutConstraints = TableLayoutConstraints.makeConstraints((String) constraints);
        } else {
            throw new DetailedIllegalArgumentException("constraints", constraints, "Must be an instance " + "of TableLayoutConstraints or String: " + constraints.getClass().getName());
        }
        final Iterator iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            if (((Entry) iterator.next()).getComponent() == comp) {
                throw new DetailedIllegalArgumentException("comp", comp, "Already in layout");
            }
        }
        tableLayoutConstraints.setImmutable();
        final Entry entry = new Entry(comp, tableLayoutConstraints);
        constraints_.add(entry);
        invalidateLayout();
    }

    /**
     *  Remove the specified component from the layout.
     *
     * @param  comp  The component to remove.
     */
    public void removeLayoutComponent(final Component comp) {
        Trace.println(traceChannel_, "removeLayoutComponent(" + comp + ")");
        assertNotNull("comp", comp);
        Entry entry;
        final Iterator iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            if (entry.getComponent() == comp) {
                iterator.remove();
                invalidateLayout();
                return;
            }
        }
        throw new DetailedIllegalArgumentException("comp", comp, "Not found");
    }

    /**
     *  Get the minimum size of this layout.
     *
     * @param  parent  The container that this layout is managing.
     * @return         The minimum size required for this layout.
     */
    public Dimension minimumLayoutSize(final Container parent) {
        setParent(parent);
        if (minimumSize_ == null) {
            calculateMinMaxPreferredSizes();
        }
        return minimumSize_;
    }

    /**
     *  Return the preferred layout size.
     *
     * @param  parent  The container that this layout is managing.
     * @return         The preferred layout size.
     */
    public Dimension preferredLayoutSize(final Container parent) {
        setParent(parent);
        if (preferredSize_ == null) {
            calculateMinMaxPreferredSizes();
        }
        return preferredSize_;
    }

    /**
     *  Layout all the components in this container.
     *
     * @param  parent  The container that this layout is managing.
     */
    public void layoutContainer(final Container parent) {
        setParent(parent);
        final Dimension parentSize = parent.getSize();
        Entry entry;
        int x = 0;
        int y = 0;
        int height = 0;
        int width = 0;
        int i;
        int rowIndex;
        int columnIndex;
        if (parent.getComponentCount() == 0) {
            return;
        }
        calculateSizes();
        calculatePositions(parent, parentSize);
        final Iterator iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            rowIndex = entry.getConstraints().getRow();
            columnIndex = entry.getConstraints().getColumn();
            y = tempRowHeaders_[rowIndex].getStart();
            x = tempColumnHeaders_[columnIndex].getStart();
            height = 0;
            width = 0;
            for (i = 0; i < entry.getConstraints().getRowSpan(); i++) {
                height += tempRowHeaders_[rowIndex + i].getActual();
            }
            for (i = 0; i < entry.getConstraints().getColumnSpan(); i++) {
                width += tempColumnHeaders_[columnIndex + i].getActual();
            }
            positionComponent(entry, x, y, width, height);
        }
    }

    /**
     *  Return the maximum layout size.
     *
     * @param  target  The container that this layout is managing.
     * @return         The maximum layout size.
     */
    public Dimension maximumLayoutSize(final Container target) {
        setParent(target);
        if (maximumSize_ == null) {
            calculateMinMaxPreferredSizes();
        }
        return maximumSize_;
    }

    /**
     *  Invalidate the layout and throw away and temporary calculations.
     *
     * @param  target  The container that this layout is managing.
     */
    public void invalidateLayout(final Container target) {
        setParent(target);
        invalidateLayout();
    }

    /**
     *  A debugging method that draws lines on the parent component to show
     *  where the table cell boundaries are. The lines will be drawn in the
     *  current colour.
     *
     * @param  graphics  The graphics object.
     * @see       TableLayoutDebuggingPanel
     */
    public void drawOutlines(final Graphics graphics) {
        int i;
        int j;
        Header column;
        Header row;
        for (i = 0; i < columnCount_; i++) {
            column = tempColumnHeaders_[i];
            for (j = 0; j < rowCount_; j++) {
                row = tempRowHeaders_[j];
                graphics.drawRect(column.getStart(), row.getStart(), column.getActual(), row.getActual());
            }
        }
    }

    /**
     *  Set the parent container for this layout.
     *
     * @param  newParent  The new parent value
     */
    private void setParent(final Container newParent) {
        assertNotNull("newParent", newParent);
        if ((parent_ != null) && (newParent != parent_)) {
            throw new DetailedIllegalArgumentException("newParent", newParent, "Attempt to reassign parent");
        }
        parent_ = newParent;
    }

    /**
     *  Return an array containing all the headers that are expandable.
     *
     * @param  first    Description of Parameter
     * @param  last     Description of Parameter
     * @param  headers  Description of Parameter
     * @return          The expandableHeaders value
     */
    private Header[] getExpandableHeaders(final int first, final int last, final Header[] headers) {
        final List list = new ArrayList(headers.length);
        int i;
        for (i = first; i <= last; i++) {
            if (headers[i].isExpandable()) {
                list.add(headers[i]);
            }
        }
        final Header expandableHeaders[] = new Header[list.size()];
        list.toArray(expandableHeaders);
        return expandableHeaders;
    }

    /**
     *  Return the minimum row height. The default is 0.
     *
     * @param  index  Description of Parameter
     * @return        The minimumRowHeight value
     */
    private int getMinimumRowHeight(final int index) {
        final HeaderPermanentInfo info = getPermanentInfo(rowHeaderPermanentInfos_, index, false);
        final int result;
        if (info == null) {
            result = 0;
        } else {
            result = info.getMin();
        }
        return result;
    }

    /**
     *  Return the minimum column width. The default is 0.
     *
     * @param  index  The column index.
     * @return        The minimum column width for the specified column.
     */
    private int getMinimumColumnWidth(final int index) {
        final HeaderPermanentInfo info = getPermanentInfo(columnHeaderPermanentInfos_, index, false);
        final int result;
        if (info == null) {
            result = 0;
        } else {
            result = info.getMin();
        }
        return result;
    }

    /**
     *  TODO: Provide comments
     *
     * @param  infoList        Description of Parameter
     * @param  index           Description of Parameter
     * @param  createIfNeeded  Description of Parameter
     * @return                 The permanentInfo value
     */
    private HeaderPermanentInfo getPermanentInfo(final Set infoList, final int index, final boolean createIfNeeded) {
        final Iterator iterator = infoList.iterator();
        HeaderPermanentInfo info;
        while (iterator.hasNext()) {
            info = (HeaderPermanentInfo) iterator.next();
            if (info.getIndex() == index) {
                return info;
            }
        }
        if (createIfNeeded) {
            if (traceChannel_ != null) {
                final StringBuffer buffer = new StringBuffer();
                buffer.append("getPermanentInfo() creating new info for ");
                if (infoList == rowHeaderPermanentInfos_) {
                    buffer.append("row ");
                } else {
                    buffer.append("column ");
                }
                buffer.append(index);
                Trace.println(buffer.toString());
            }
            info = new HeaderPermanentInfo(index);
            infoList.add(info);
        } else {
            info = null;
        }
        return info;
    }

    /**
     *  Return the TableLayoutConstraints object that corresponds to the
     *  specified component or null if this component could not be found.
     *
     * @param  component  Description of Parameter
     * @return            The constraints value
     */
    private TableLayoutConstraints getConstraints(final Component component) {
        Entry entry;
        final Iterator iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            if (entry.getComponent() == component) {
                return entry.getConstraints();
            }
        }
        return null;
    }

    /**
     *  Return the minimum size of the specified component. If the component is
     *  not visible and we are ignoring invisible components then return a size
     *  of 0,0
     *
     * @param  component  The component that we will be querying
     * @return            The size
     */
    private final Dimension getComponentMinimumSize(final Component component) {
        final Dimension size;
        if (component.isVisible() == false && ignoreInvisibleComponents_ == true) {
            size = new Dimension(0, 0);
        } else {
            size = component.getMinimumSize();
        }
        return size;
    }

    /**
     *  Return the minimum size of the specified component. If the component is
     *  not visible and we are ignoring invisible components then return a size
     *  of 0,0
     *
     * @param  component  The component that we will be querying
     * @return            The size
     */
    private final Dimension getComponentMaximumSize(final Component component) {
        final Dimension size;
        if (component.isVisible() == false && ignoreInvisibleComponents_ == true) {
            size = new Dimension(0, 0);
        } else {
            size = component.getMaximumSize();
        }
        return size;
    }

    /**
     *  Return the minimum size of the specified component. If the component is
     *  not visible and we are ignoring invisible components then return a size
     *  of 0,0
     *
     * @param  component  The component that we will be querying
     * @return            The size
     */
    private final Dimension getComponentPreferredSize(final Component component) {
        final Dimension size;
        if (component.isVisible() == false && ignoreInvisibleComponents_ == true) {
            size = new Dimension(0, 0);
        } else {
            size = component.getPreferredSize();
        }
        return size;
    }

    /**
     *  Calculate the various sizes.
     */
    private void calculateMinMaxPreferredSizes() {
        int i;
        int xMin = 0;
        int yMin = 0;
        int xMax = 0;
        int yMax = 0;
        int xPreferred = 0;
        int yPreferred = 0;
        calculateRowAndColumnCount();
        calculateSizes();
        if (false) {
            Trace.println("calculateMinMaxPreferredSize() tempRowHeaders_=[" + tempRowHeaders_ + "] rowCount_=[" + rowCount_ + "] columnCount=[" + columnCount_ + "] tempSizesAreValid_=[" + tempSizesAreValid_ + "]");
        }
        for (i = 0; i < rowCount_; i++) {
            yMin += tempRowHeaders_[i].getMin();
            yMax += tempRowHeaders_[i].getMax();
            yPreferred += tempRowHeaders_[i].getPreferred();
        }
        for (i = 0; i < columnCount_; i++) {
            xMin += tempColumnHeaders_[i].getMin();
            xMax += tempColumnHeaders_[i].getMax();
            xPreferred += tempColumnHeaders_[i].getPreferred();
        }
        final Insets insets = parent_.getInsets();
        final int horizontalInset = insets.left + insets.right;
        final int verticalInset = insets.top + insets.bottom;
        xMin += horizontalInset;
        yMin += verticalInset;
        xPreferred += horizontalInset;
        yPreferred += verticalInset;
        xMax += horizontalInset;
        yMax += verticalInset;
        xPreferred = Math.max(xPreferred, xMin);
        xMax = Math.max(xMax, xPreferred);
        yPreferred = Math.max(yPreferred, yMin);
        yMax = Math.max(yMax, yPreferred);
        if (areAnyExpandable(rowHeaderPermanentInfos_)) {
            yMax = Integer.MAX_VALUE;
        }
        if (areAnyExpandable(columnHeaderPermanentInfos_)) {
            xMax = Integer.MAX_VALUE;
        }
        minimumSize_ = new Dimension(xMin, yMin);
        maximumSize_ = new Dimension(xMax, yMax);
        preferredSize_ = new Dimension(xPreferred, yPreferred);
        if (constraints_.size() == 0) {
            maximumSize_ = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    /**
     *  Return true if any of the infos are expandable.
     *
     * @param  permanentInfos  The infos.
     * @return                 Description of the Returned Value
     */
    private boolean areAnyExpandable(final Set permanentInfos) {
        HeaderPermanentInfo info;
        final Iterator iterator = permanentInfos.iterator();
        while (iterator.hasNext()) {
            info = (HeaderPermanentInfo) iterator.next();
            if (info.isExpandable()) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Invalidate the layout.
     */
    private void invalidateLayout() {
        tempColumnHeaders_ = null;
        tempRowHeaders_ = null;
        tempSizesAreValid_ = false;
        minimumSize_ = null;
        maximumSize_ = null;
        preferredSize_ = null;
        actualSize_ = null;
        columnCount_ = 0;
        rowCount_ = 0;
    }

    /**
     *  Calculate all the various sizing information required for this layout.
     *  Called by layoutContainer(), minimumLayoutSize(), maximumLayoutSize()
     */
    private void calculateSizes() {
        if (false) {
            Trace.println("TableLayout.calculateSizes() " + "tempSizesAreValid_=[" + tempSizesAreValid_ + "] tempRowHeaders_=[" + tempRowHeaders_ + "] tempColumnHeaders_=[" + tempColumnHeaders_ + "]");
        }
        if (tempSizesAreValid_ && tempRowHeaders_ == null) {
            Trace.whereAmI();
        }
        Dimension minSize;
        Dimension maxSize;
        Dimension preferredSize;
        Entry entry;
        TableLayoutConstraints constraints;
        Iterator iterator;
        if (rowCount_ == 0 || columnCount_ == 0) {
            return;
        }
        if (tempSizesAreValid_) {
            return;
        }
        tempSizesAreValid_ = true;
        initTempSizes();
        iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            constraints = entry.getConstraints();
            if (constraints.getObeyMinimumSize()) {
                minSize = getComponentMinimumSize(entry.getComponent());
            } else {
                minSize = new Dimension(0, 0);
            }
            if (constraints.getObeyMaximumSize()) {
                maxSize = getComponentMaximumSize(entry.getComponent());
            } else {
                maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
            preferredSize = getComponentPreferredSize(entry.getComponent());
            if (constraints.getRowSpan() == 1) {
                Trace.println(traceChannel_, "tempRowHeaders_.length=[" + tempRowHeaders_.length + "] constraints.getRow()=[" + constraints.getRow() + "]");
                tempRowHeaders_[constraints.getRow()].setHasComponents(true);
                if (minSize.height > tempRowHeaders_[constraints.getRow()].getMin()) {
                    tempRowHeaders_[constraints.getRow()].setMin(minSize.height);
                }
                if (maxSize.height > tempRowHeaders_[constraints.getRow()].getMax()) {
                    tempRowHeaders_[constraints.getRow()].setMax(maxSize.height);
                }
                if (preferredSize.height > tempRowHeaders_[constraints.getRow()].getPreferred()) {
                    tempRowHeaders_[constraints.getRow()].setPreferred(preferredSize.height);
                }
                if (tempRowHeaders_[constraints.getRow()].getMin() > tempRowHeaders_[constraints.getRow()].getMax()) {
                    tempRowHeaders_[constraints.getRow()].setMax(tempRowHeaders_[constraints.getRow()].getMin());
                }
                if (tempRowHeaders_[constraints.getRow()].getMin() > tempRowHeaders_[constraints.getRow()].getPreferred()) {
                    tempRowHeaders_[constraints.getRow()].setPreferred(tempRowHeaders_[constraints.getRow()].getMin());
                }
            }
            if (constraints.getColumnSpan() == 1) {
                if (minSize.width > tempColumnHeaders_[constraints.getColumn()].getMin()) {
                    tempColumnHeaders_[constraints.getColumn()].setMin(minSize.width);
                }
                if (maxSize.width > tempColumnHeaders_[constraints.getColumn()].getMax()) {
                    tempColumnHeaders_[constraints.getColumn()].setMax(maxSize.width);
                }
                if (preferredSize.width > tempColumnHeaders_[constraints.getColumn()].getPreferred()) {
                    tempColumnHeaders_[constraints.getColumn()].setPreferred(preferredSize.width);
                }
                tempColumnHeaders_[constraints.getColumn()].setHasComponents(true);
                if (tempColumnHeaders_[constraints.getColumn()].getMin() > tempColumnHeaders_[constraints.getColumn()].getMax()) {
                    tempColumnHeaders_[constraints.getColumn()].setMax(tempColumnHeaders_[constraints.getColumn()].getMin());
                }
                if (tempColumnHeaders_[constraints.getColumn()].getMin() > tempColumnHeaders_[constraints.getColumn()].getPreferred()) {
                    tempColumnHeaders_[constraints.getColumn()].setPreferred(tempColumnHeaders_[constraints.getColumn()].getMin());
                }
            }
        }
        iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            constraints = entry.getConstraints();
            if (constraints.getObeyMinimumSize()) {
                minSize = getComponentMinimumSize(entry.getComponent());
            } else {
                minSize = new Dimension(0, 0);
            }
            if (constraints.getObeyMaximumSize() == true) {
                maxSize = getComponentMaximumSize(entry.getComponent());
            } else {
                maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
            preferredSize = getComponentPreferredSize(entry.getComponent());
            if (constraints.getRowSpan() != 1) {
                adjustSizesForSpanning(constraints.getRow(), constraints.getRowSpan(), tempRowHeaders_, minSize.height, preferredSize.height, maxSize.height);
            }
            if (constraints.getColumnSpan() != 1) {
                adjustSizesForSpanning(constraints.getColumn(), constraints.getColumnSpan(), tempColumnHeaders_, minSize.width, preferredSize.width, maxSize.width);
            }
        }
        adjustHeaderSizes(tempRowHeaders_);
        adjustHeaderSizes(tempColumnHeaders_);
    }

    /**
     *  Adjust the various sizes to account for components than span multiple
     *  columns/rows.
     *
     * @param  start          The starting index of the component.
     * @param  span           The number of columns/rows that the component
     *      spans.
     * @param  sizes          The headers that we are adjusting.
     * @param  minSize        The minimum size of the component.
     * @param  preferredSize  The preferred size of the component.
     * @param  maxSize        The maximum size of the component.
     */
    private void adjustSizesForSpanning(final int start, final int span, final Header sizes[], final int minSize, final int preferredSize, final int maxSize) {
        int combinedSize;
        int remainder;
        int i;
        final Header expandableHeaders[] = getExpandableHeaders(start, start + span, sizes);
        combinedSize = 0;
        for (i = 0; i < span; i++) {
            combinedSize += sizes[start + i].getMin();
        }
        if (minSize > combinedSize) {
            if (expandableHeaders.length == 0) {
                final int delta = (minSize - combinedSize) / span;
                for (i = 0; i < span; i++) {
                    sizes[start + i].setMin(sizes[start + i].getMin() + delta);
                    combinedSize += delta;
                }
                remainder = minSize - combinedSize;
                for (i = 0; i < remainder; i++) {
                    sizes[start + i].setMin(sizes[start + i].getMin() + 1);
                }
            } else {
                final int delta = (minSize - combinedSize) / expandableHeaders.length;
                for (i = 0; i < expandableHeaders.length; i++) {
                    expandableHeaders[i].setMin(expandableHeaders[i].getMin() + delta);
                    combinedSize += delta;
                }
                remainder = minSize - combinedSize;
                for (i = 0; i < remainder; i++) {
                    expandableHeaders[i].setMin(expandableHeaders[i].getMin() + 1);
                }
            }
        }
        combinedSize = 0;
        for (i = 0; i < span; i++) {
            combinedSize += sizes[start + i].getPreferred();
        }
        if (preferredSize > combinedSize) {
            if (expandableHeaders.length == 0) {
                final int delta = (preferredSize - combinedSize) / span;
                for (i = 0; i < span; i++) {
                    sizes[start + i].setPreferred(sizes[start + i].getPreferred() + delta);
                    combinedSize += delta;
                }
                remainder = preferredSize - combinedSize;
                for (i = 0; i < remainder; i++) {
                    sizes[start + i].setPreferred(sizes[start + i].getPreferred() + 1);
                }
            } else {
                final int delta = (preferredSize - combinedSize) / expandableHeaders.length;
                for (i = 0; i < expandableHeaders.length; i++) {
                    expandableHeaders[i].setPreferred(expandableHeaders[i].getPreferred() + delta);
                    combinedSize += delta;
                }
                remainder = preferredSize - combinedSize;
                for (i = 0; i < remainder; i++) {
                    expandableHeaders[i].setPreferred(expandableHeaders[i].getPreferred() + 1);
                }
            }
        }
    }

    /**
     *  Calculate all the positions of the various rows/columns
     *
     * @param  parent      Description of Parameter
     * @param  parentSize  Description of Parameter
     */
    private void calculatePositions(final Container parent, final Dimension parentSize) {
        int i;
        int rowStart;
        int columnStart;
        final Insets insets = parent.getInsets();
        calculateActualSizes(parentSize);
        switch(verticalAlignment_) {
            case TOP:
                rowStart = insets.top;
                break;
            case BOTTOM:
                rowStart = parentSize.height - actualSize_.height - insets.bottom;
                break;
            case CENTER:
                rowStart = (parentSize.height - actualSize_.height - insets.top - insets.bottom) / 2 + insets.top;
                break;
            default:
                throw new IllegalStateException("Unknown verticalAlignment: " + verticalAlignment_);
        }
        switch(horizontalAlignment_) {
            case LEFT:
                columnStart = insets.left;
                break;
            case RIGHT:
                columnStart = parentSize.width - actualSize_.width - insets.right;
                break;
            case CENTER:
                columnStart = (parentSize.width - actualSize_.width - insets.left - insets.right) / 2 + insets.left;
                break;
            default:
                throw new IllegalStateException("Unknown horizontalAlignment: " + horizontalAlignment_);
        }
        tempRowHeaders_[0].setStart(rowStart);
        for (i = 1; i < rowCount_; i++) {
            tempRowHeaders_[i].setStart(tempRowHeaders_[i - 1].getStart() + tempRowHeaders_[i - 1].getActual());
        }
        tempColumnHeaders_[0].setStart(columnStart);
        for (i = 1; i < columnCount_; i++) {
            tempColumnHeaders_[i].setStart(tempColumnHeaders_[i - 1].getStart() + tempColumnHeaders_[i - 1].getActual());
        }
        if (traceChannel_ != null) {
            Trace.println(traceChannel_, "TableLayout.calculatePositions() START parentSize=" + parentSize);
            for (i = 0; i < rowCount_; i++) {
                Trace.println(traceChannel_, "   tempRowSizes[" + i + "]=" + tempRowHeaders_[i]);
            }
            for (i = 0; i < columnCount_; i++) {
                Trace.println(traceChannel_, "   tempColumnSizes[" + i + "]=" + tempColumnHeaders_[i]);
            }
            final Component children[] = parent_.getComponents();
            StringBuffer buffer;
            TableLayoutConstraints constraints;
            for (i = 0; i < children.length; i++) {
                constraints = getConstraints(children[i]);
                buffer = new StringBuffer();
                buffer.append("   children[" + i + "] min=");
                buffer.append(toString(children[i].getMinimumSize()));
                buffer.append(" preferred=");
                buffer.append(toString(children[i].getPreferredSize()));
                buffer.append(" visible=[");
                buffer.append(children[i].isVisible());
                buffer.append(" type=[");
                buffer.append(children[i].getClass().getName());
                buffer.append("] name=[");
                buffer.append(children[i].getName());
                buffer.append("] row=[");
                buffer.append(constraints.getRow());
                buffer.append("+");
                buffer.append(constraints.getRowSpan());
                buffer.append("] column=[");
                buffer.append(constraints.getColumn());
                buffer.append("+");
                buffer.append(constraints.getColumnSpan());
                buffer.append("]");
                Trace.println(traceChannel_, buffer.toString());
            }
            Trace.println(traceChannel_, "TableLayout.calculatePositions() END");
        }
    }

    /**
     *  Position one component given the bounding coordinates
     *
     * @param  entry   Description of Parameter
     * @param  x       Description of Parameter
     * @param  y       Description of Parameter
     * @param  width   Description of Parameter
     * @param  height  Description of Parameter
     */
    private void positionComponent(final Entry entry, final int x, final int y, final int width, final int height) {
        final TableLayoutConstraints constraints = entry.getConstraints();
        final Dimension maxSize;
        final Dimension minSize = getComponentPreferredSize(entry.getComponent());
        int newWidth;
        int newHeight;
        final int newX;
        final int newY;
        if (constraints.getObeyMaximumSize() == true) {
            maxSize = getComponentMaximumSize(entry.getComponent());
        } else {
            maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        if (constraints.getVerticalStretch()) {
            newHeight = Math.min(maxSize.height, height);
        } else {
            newHeight = minSize.height;
        }
        if (constraints.getHorizontalStretch()) {
            newWidth = Math.min(maxSize.width, width);
        } else {
            newWidth = minSize.width;
        }
        if (newHeight > height) {
            newHeight = height;
        }
        if (newWidth > width) {
            newWidth = width;
        }
        switch(constraints.getVerticalAlignment()) {
            case TOP:
                newY = y;
                break;
            case BOTTOM:
                newY = y + (height - newHeight);
                break;
            case CENTER:
                newY = y + (height - newHeight) / 2;
                break;
            default:
                throw new IllegalStateException("Illegal value for verticalAlignment: " + constraints.getVerticalAlignment());
        }
        switch(constraints.getHorizontalAlignment()) {
            case LEFT:
                newX = x;
                break;
            case RIGHT:
                newX = x + (width - newWidth);
                break;
            case CENTER:
                newX = x + (width - newWidth) / 2;
                break;
            default:
                throw new IllegalStateException("Illegal value for horizontalAlignment: " + constraints.getVerticalAlignment());
        }
        entry.getComponent().setBounds(newX, newY, newWidth, newHeight);
    }

    /**
     *  The list of constraints has been modified. Update the row and column
     *  counts according to the new constraints.
     */
    private void calculateRowAndColumnCount() {
        Entry entry;
        int lastRow = -1;
        int lastColumn = -1;
        int row;
        int column;
        Iterator iterator;
        TableLayoutConstraints constraints;
        iterator = constraints_.iterator();
        while (iterator.hasNext()) {
            entry = (Entry) iterator.next();
            constraints = entry.getConstraints();
            row = constraints.getRow() + constraints.getRowSpan();
            if (row > lastRow) {
                lastRow = row;
            }
            column = constraints.getColumn() + constraints.getColumnSpan();
            if (column > lastColumn) {
                lastColumn = column;
            }
        }
        HeaderPermanentInfo info;
        iterator = rowHeaderPermanentInfos_.iterator();
        while (iterator.hasNext()) {
            info = (HeaderPermanentInfo) iterator.next();
            if (info.getIndex() > lastRow) {
                lastRow = info.getIndex();
            }
        }
        iterator = columnHeaderPermanentInfos_.iterator();
        while (iterator.hasNext()) {
            info = (HeaderPermanentInfo) iterator.next();
            if (info.getIndex() > lastColumn) {
                Trace.println(traceChannel_, "Increasing column count to " + info.getIndex());
                lastColumn = info.getIndex();
            }
        }
        rowCount_ = lastRow + 1;
        columnCount_ = lastColumn + 1;
        if (rowCount_ == 0 || columnCount_ == 0) {
            rowCount_ = 0;
            columnCount_ = 0;
        }
        if (traceChannel_ != null) {
            Trace.println(traceChannel_, "calculateRowAndColumnCount() rowCount=" + rowCount_ + " columnCount_=" + columnCount_);
        }
    }

    /**
     *  Calculate the actual sizes to be used based on the actual dimension of
     *  the parent container.
     *
     * @param  parentSize  Description of Parameter
     */
    private void calculateActualSizes(final Dimension parentSize) {
        final Dimension preferredSize = preferredLayoutSize(parent_);
        final Insets insets = parent_.getInsets();
        final int x = calculateActualSizes(tempColumnHeaders_, preferredSize.width, parentSize.width - insets.left - insets.right);
        final int y = calculateActualSizes(tempRowHeaders_, preferredSize.height, parentSize.height - insets.top - insets.bottom);
        actualSize_ = new Dimension(x, y);
    }

    /**
     *  Fix up all the headers such that minimum <= preferred <= maximum
     *
     * @param  sizes  Description of Parameter
     */
    private void adjustHeaderSizes(final Header sizes[]) {
        int i;
        Header header;
        for (i = 0; i < sizes.length; i++) {
            header = sizes[i];
            if (header.getMin() > header.getPreferred()) {
                header.setPreferred(header.getMin());
            }
            if (header.getMin() > header.getMax()) {
                header.setMax(header.getMin());
            }
            if (header.getPreferred() > header.getMax()) {
                header.setPreferred(header.getMax());
            }
        }
    }

    /**
     *  Calculate the actual sizes for the specified row or column headers.
     *  Return the actual length.
     *
     * @param  sizes            Description of Parameter
     * @param  preferredLength  Description of Parameter
     * @param  clipLength       Description of Parameter
     * @return                  Description of the Returned Value
     */
    private int calculateActualSizes(final Header sizes[], final int preferredLength, final int clipLength) {
        int i;
        if (clipLength < 1) {
            for (i = 0; i < sizes.length; i++) {
                sizes[i].setActual(0);
            }
            return 0;
        }
        if (preferredLength <= clipLength) {
            for (i = 0; i < sizes.length; i++) {
                sizes[i].setActual(sizes[i].getPreferred());
            }
            if (preferredLength < clipLength) {
                expandToFit(sizes, clipLength);
            }
        } else {
            shrinkToFit(sizes, clipLength);
        }
        int actualLength = 0;
        for (i = 0; i < sizes.length; i++) {
            actualLength += sizes[i].getActual();
        }
        return actualLength;
    }

    /**
     *  Expand the specified sizes to fit within the specified clipLength.
     *
     * @param  sizes       Description of Parameter
     * @param  clipLength  Description of Parameter
     */
    private void expandToFit(final Header sizes[], final int clipLength) {
        int i;
        int numberExpandable = 0;
        int currentLength = 0;
        for (i = 0; i < sizes.length; i++) {
            if (sizes[i].isExpandable() == true) {
                numberExpandable++;
            }
            currentLength += sizes[i].getActual();
        }
        if (numberExpandable == 0) {
            return;
        }
        int addAmount = (clipLength - currentLength) / numberExpandable;
        for (i = 0; i < sizes.length; i++) {
            if (sizes[i].isExpandable() == true) {
                sizes[i].setActual(sizes[i].getActual() + addAmount);
            }
        }
        int remaining = (clipLength - currentLength) - (addAmount * numberExpandable);
        if (remaining != 0) {
            for (i = 0; i < sizes.length; i++) {
                if (sizes[i].isExpandable() == true) {
                    sizes[i].setActual(sizes[i].getActual() + 1);
                    remaining--;
                    if (remaining == 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     *  Shrink the specified sizes to fit within the specified clipLength. Do
     *  not shrink beyond the minimum size.
     *
     * @param  sizes       Description of Parameter
     * @param  clipLength  Description of Parameter
     */
    private void shrinkToFit(final Header sizes[], final int clipLength) {
        if (clipLength < 0) {
            throw new DetailedIllegalArgumentException("clipLength", new Integer(clipLength), "may not be negative");
        }
        int i;
        int remaining = clipLength;
        for (i = 0; i < sizes.length; i++) {
            sizes[i].setActual(sizes[i].getMin());
            remaining -= sizes[i].getActual();
        }
        if (remaining < 0) {
            return;
        }
        int delta;
        int addAmount = 1;
        int numberChanged = sizes.length;
        while (numberChanged != 0) {
            addAmount = remaining / numberChanged;
            if (addAmount == 0) {
                addAmount = 1;
            }
            numberChanged = 0;
            for (i = 0; i < sizes.length; i++) {
                delta = sizes[i].getPreferred() - sizes[i].getActual();
                if (delta > 0) {
                    delta = Math.min(delta, addAmount);
                    sizes[i].setActual(sizes[i].getActual() + delta);
                    numberChanged++;
                    remaining -= delta;
                    if (remaining == 0) {
                        return;
                    }
                }
            }
        }
    }

    /**
     *  Initialize the temporary arrays (tempRowHeaders_ and tempColumnHeaders_)
     *  for use in a calculation.
     */
    private void initTempSizes() {
        int i;
        if ((tempRowHeaders_ == null) || (tempRowHeaders_.length != rowCount_)) {
            tempRowHeaders_ = new Header[rowCount_];
            for (i = 0; i < rowCount_; i++) {
                tempRowHeaders_[i] = new Header();
            }
        }
        if ((tempColumnHeaders_ == null) || (tempColumnHeaders_.length != columnCount_)) {
            tempColumnHeaders_ = new Header[columnCount_];
            for (i = 0; i < columnCount_; i++) {
                tempColumnHeaders_[i] = new Header();
            }
        }
        for (i = 0; i < tempRowHeaders_.length; i++) {
            tempRowHeaders_[i].setMin(getMinimumRowHeight(i));
            tempRowHeaders_[i].setPreferred(getMinimumRowHeight(i));
            tempRowHeaders_[i].setMax(Integer.MAX_VALUE);
            tempRowHeaders_[i].setHasComponents(false);
            tempRowHeaders_[i].setExpandable(isRowExpandable(i));
        }
        for (i = 0; i < tempColumnHeaders_.length; i++) {
            tempColumnHeaders_[i].setMin(getMinimumColumnWidth(i));
            tempColumnHeaders_[i].setPreferred(getMinimumColumnWidth(i));
            tempColumnHeaders_[i].setMax(Integer.MAX_VALUE);
            tempColumnHeaders_[i].setHasComponents(false);
            tempColumnHeaders_[i].setExpandable(isColumnExpandable(i));
        }
    }

    /**
     *  A convenience class to attach the constraints to a component.
     */
    private class Entry implements Serializable {

        private static final long serialVersionUID = 396191633848670929L;

        /**
         *  Description of the Field
         */
        private final Component component_;

        /**
         *  Description of the Field
         */
        private final TableLayoutConstraints constraints_;

        /**
         *  Constructor for the Entry object
         *
         * @param  comp         Description of Parameter
         * @param  constraints  Description of Parameter
         */
        public Entry(final Component comp, final TableLayoutConstraints constraints) {
            component_ = comp;
            constraints_ = constraints;
        }

        /**
         *  Gets the component attribute of the Entry object
         *
         * @return    The component value
         */
        public final Component getComponent() {
            return component_;
        }

        /**
         *  Gets the constraints attribute of the Entry object
         *
         * @return    The constraints value
         */
        public final TableLayoutConstraints getConstraints() {
            return constraints_;
        }

        /**
         *  Description of the Method
         *
         * @return    Description of the Returned Value
         */
        public String toString() {
            return this.getClass().getName() + " component_=" + getComponent() + " constraints_=" + getConstraints();
        }
    }

    /**
     *  A convenience class to hold information specific to a row or column.
     */
    private class Header implements Serializable {

        private static final long serialVersionUID = 396191633848670929L;

        private int min_;

        private int max_;

        private int preferred_;

        private int actual_;

        private int start_;

        private boolean hasComponents_;

        private boolean isExpandable_;

        /**
         *  Sets the actual attribute of the Header object
         *
         * @param  actual  The new actual value
         */
        public final void setActual(int actual) {
            actual_ = actual;
        }

        /**
         *  Sets the preferred attribute of the Header object
         *
         * @param  preferred  The new preferred value
         */
        public final void setPreferred(int preferred) {
            preferred_ = preferred;
        }

        /**
         *  Sets the min attribute of the Header object
         *
         * @param  min  The new min value
         */
        public final void setMin(int min) {
            min_ = min;
        }

        /**
         *  Sets the max attribute of the Header object
         *
         * @param  max  The new max value
         */
        public final void setMax(int max) {
            max_ = max;
        }

        /**
         *  Sets the start attribute of the Header object
         *
         * @param  start  The new start value
         */
        public final void setStart(int start) {
            start_ = start;
        }

        /**
         *  Sets the hasComponents attribute of the Header object
         *
         * @param  hasComponents  The new hasComponents value
         */
        public final void setHasComponents(boolean hasComponents) {
            hasComponents_ = hasComponents;
        }

        /**
         *  Sets the expandable attribute of the Header object
         *
         * @param  expandable  The new expandable value
         */
        public final void setExpandable(boolean expandable) {
            isExpandable_ = expandable;
        }

        /**
         *  Gets the actual attribute of the Header object
         *
         * @return    The actual value
         */
        public final int getActual() {
            return actual_;
        }

        /**
         *  Gets the preferred attribute of the Header object
         *
         * @return    The preferred value
         */
        public final int getPreferred() {
            return preferred_;
        }

        /**
         *  Gets the min attribute of the Header object
         *
         * @return    The min value
         */
        public final int getMin() {
            return min_;
        }

        /**
         *  Gets the max attribute of the Header object
         *
         * @return    The max value
         */
        public final int getMax() {
            return max_;
        }

        /**
         *  Gets the start attribute of the Header object
         *
         * @return    The start value
         */
        public final int getStart() {
            return start_;
        }

        /**
         *  Gets the hasComponents attribute of the Header object
         *
         * @return    The hasComponents value
         */
        public final boolean getHasComponents() {
            return hasComponents_;
        }

        /**
         *  Gets the expandable attribute of the Header object
         *
         * @return    The expandable value
         */
        public final boolean isExpandable() {
            return isExpandable_;
        }

        /**
         *  Description of the Method
         *
         * @return    Description of the Returned Value
         */
        public String toString() {
            final StringBuffer buffer = new StringBuffer();
            buffer.append("TableLayout.Header[");
            buffer.append(" min=[");
            buffer.append(getMin());
            buffer.append("] max=[");
            final int max = getMax();
            if (max == Integer.MAX_VALUE) {
                buffer.append("max_int");
            } else {
                buffer.append(max);
            }
            buffer.append("] preferred=[");
            buffer.append(getPreferred());
            buffer.append("] start=[");
            buffer.append(getStart());
            buffer.append("] actual=[");
            buffer.append(getActual());
            buffer.append("] hasComponents=[");
            buffer.append(getHasComponents());
            buffer.append("] isExpandable=[");
            buffer.append(isExpandable());
            buffer.append("]]");
            return buffer.toString();
        }
    }

    private class HeaderPermanentInfo implements Serializable {

        private static final long serialVersionUID = 396191633848670929L;

        private final int index_;

        private int min_;

        private boolean isExpandable_;

        /**
         *  Constructor for the HeaderPermanentInfo object
         *
         * @param  newIndex  Description of Parameter
         */
        public HeaderPermanentInfo(final int newIndex) {
            if (newIndex < 0) {
                throw new DetailedIllegalArgumentException("newIndex", newIndex, "May not be negative");
            }
            index_ = newIndex;
            min_ = 0;
            isExpandable_ = false;
        }

        /**
         *  Sets the min attribute of the HeaderPermanentInfo object
         *
         * @param  min  The new min value
         */
        public final void setMin(final int min) {
            min_ = min;
        }

        /**
         *  Sets the expandable attribute of the HeaderPermanentInfo object
         *
         * @param  expandable  The new expandable value
         */
        public final void setExpandable(final boolean expandable) {
            isExpandable_ = expandable;
        }

        /**
         *  Gets the min attribute of the HeaderPermanentInfo object
         *
         * @return    The min value
         */
        public final int getMin() {
            return min_;
        }

        /**
         *  Gets the index attribute of the HeaderPermanentInfo object
         *
         * @return    The index value
         */
        public final int getIndex() {
            return index_;
        }

        /**
         *  Gets the expandable attribute of the HeaderPermanentInfo object
         *
         * @return    The expandable value
         */
        public final boolean isExpandable() {
            return isExpandable_;
        }
    }

    /**
     * Verify that the specified value is not null.  If it is then throw an exception
     *
     * @param fieldName The name of the field to check
     * @param fieldValue The value of the field to check
     * @exception DetailedNullPointerException If fieldValue is null
     */
    protected final void assertNotNull(final String fieldName, final Object fieldValue) throws DetailedNullPointerException {
        if (fieldValue == null) {
            throw new DetailedNullPointerException(fieldName);
        }
    }
}
