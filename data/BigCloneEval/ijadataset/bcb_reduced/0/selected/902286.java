package com.endfocus.layout;

import java.awt.*;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.SwingConstants;

public class ColumnLayout implements LayoutManager2, Serializable {

    /**
	 * Each row height will be based on the maximum height of the components in that row 
	 */
    public static final int MAXIMUM_HEIGHT = -2;

    /**
	 * Each row height will be based on the maximum height of the tallest row in the layout
	 */
    public static final int ALL_MAXIMUM_HEIGHT = -1;

    public static final int EQUAL_WIDTHS = -1;

    public static final int MAXIMUM_WIDTH = -2;

    public static final int ALL_MAXIMUM_WIDTHS = -3;

    public static final int SCALE_COMPONENTS = -4;

    public static final int FILL_COLUMN = 1;

    public static final int ABSOLUTE_WIDTH = 2;

    public static final int MAX_COMPONENT_WIDTH = 3;

    public static final int PREFERRED_WIDTH = 4;

    public static final int HORIZONTAL = 0xF;

    public static final int VERTICAL = 0xF0;

    public static final int HCENTER = 1;

    public static final int LEFT = 2;

    public static final int RIGHT = 4;

    public static final int VCENTER = 32;

    public static final int TOP = 64;

    public static final int BOTTOM = 128;

    public static final String NEXT_ROW = "NextRow";

    Vector components = new Vector();

    ColumnAttributes[] attributes;

    int preferredRowHeight = MAXIMUM_HEIGHT;

    int rowGap = 3;

    int layoutStyle = 0;

    Insets insets;

    DummyComponent dummyComponent = new DummyComponent();

    String name = "ColumnLayout";

    /**
	 * Creates a default single column layout - essentially a flow layout
	 */
    public ColumnLayout() {
        this(1);
    }

    public ColumnLayout(int columns, String name) {
        this(columns);
        this.name = name;
    }

    public ColumnLayout(int columns) {
        setColumnCount(columns);
    }

    public void setColumnCount(int columns) {
        attributes = new ColumnAttributes[columns];
        for (int i = 0; i < columns; i++) {
            attributes[i] = new ColumnAttributes();
        }
    }

    public int getColumnCount() {
        return attributes.length;
    }

    public void setLayoutStyle(int layoutStyle) {
        this.layoutStyle = layoutStyle;
    }

    public void setColumnWidth(int column, int width) {
        if (width == ALL_MAXIMUM_WIDTHS) {
            for (int i = 0; i < attributes.length; i++) {
                attributes[i].setWidth(MAXIMUM_WIDTH);
            }
        } else {
            attributes[column].setWidth(width);
        }
    }

    public int getColumnWidth(int column) {
        return attributes[column].getWidth();
    }

    public void setColumnAlignment(int column, int alignment) {
        if ((alignment & VERTICAL) == 0) alignment |= VCENTER;
        attributes[column].setAlignment(alignment);
    }

    public int getColumnAlignment(int column) {
        return attributes[column].getAlignment();
    }

    public void setColumnGap(int column, int gap) {
        attributes[column].setGap(gap);
    }

    public int getColumnGap(int column) {
        return attributes[column].getGap();
    }

    public void setRowGap(int gap) {
        rowGap = gap;
    }

    public int getRowGap() {
        return rowGap;
    }

    public void setPreferredRowHeight(int preferredRowHeight) {
        this.preferredRowHeight = preferredRowHeight;
    }

    public int getPreferredRowHeight() {
        return preferredRowHeight;
    }

    public void setColumnFillStyle(int column, int fillStyle) {
        attributes[column].setFillStyle(fillStyle);
    }

    public int getColumnFillStyle(int column) {
        return attributes[column].getFillStyle();
    }

    public void setAbsoluteComponentWidthInColumn(int column, int width) {
        attributes[column].setComponentWidth(width);
    }

    public int getAbsoluteComponentWidthInColumn(int column) {
        return attributes[column].getComponentWidth();
    }

    public void addLayoutComponent(String constraint, Component c) {
    }

    public void layoutContainer(Container target) {
        int noColumns = attributes.length;
        int componentCount = components.size();
        Dimension dim = target.getPreferredSize();
        insets = target.getInsets();
        int gap = 0;
        for (int i = 0; i < noColumns; i++) {
            gap += attributes[i].getGap();
        }
        int maxWidth = dim.width - gap - insets.left - insets.right;
        computeMaxComponentWidths();
        switch(preferredRowHeight) {
            case MAXIMUM_HEIGHT:
                layoutIndividualRowHeights(noColumns, componentCount, maxWidth);
                break;
            case ALL_MAXIMUM_HEIGHT:
                int maxRowHeight = getMaximumRowHeight();
                layoutFixedHeight(maxRowHeight, noColumns, componentCount, maxWidth);
                break;
            default:
                layoutFixedHeight(preferredRowHeight, noColumns, componentCount, maxWidth);
                break;
        }
    }

    public void computeMaxComponentWidths() {
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].setMaxComponentWidth(getMaxComponentWidth(i));
        }
    }

    /**
	 * Layout the components using the maximum height for each individual row
	 */
    protected void layoutIndividualRowHeights(int noColumns, int componentCount, int maxWidth) {
        int rowPosition = insets.top;
        for (int i = 0; i < componentCount; i += noColumns) {
            int columnPosition = insets.left + attributes[0].getGap();
            int rowNo = i / noColumns;
            int rowHeight = getRowHeight(rowNo);
            columnPosition = layoutRow(i, noColumns, componentCount, rowPosition, columnPosition, rowHeight, maxWidth);
            rowPosition += rowHeight;
            rowPosition += rowGap;
        }
    }

    /**
	 * Layout the components using the specified preferred height.
	 */
    protected void layoutFixedHeight(int rowHeight, int noColumns, int componentCount, int maxWidth) {
        int rowPosition = insets.top;
        for (int i = 0; i < componentCount; i += noColumns) {
            int columnPosition = insets.left + attributes[0].getGap();
            columnPosition = layoutRow(i, noColumns, componentCount, rowPosition, columnPosition, rowHeight, maxWidth);
            rowPosition += rowHeight;
            rowPosition += rowGap;
        }
    }

    protected int layoutRow(int indexOffset, int noColumns, int componentCount, int rowPosition, int columnPosition, int rowHeight, int maxWidth) {
        for (int j = 0; j < noColumns; j++) {
            int width = attributes[j].getWidth();
            if (width == EQUAL_WIDTHS) width = maxWidth / noColumns;
            int index = indexOffset + j;
            if (index < componentCount) {
                Component c = (Component) components.elementAt(index);
                Dimension dim = c.getPreferredSize();
                int componentWidth = getComponentWidth(dim, width, j);
                Point pt = getAlignedPosition(componentWidth, dim.height, columnPosition, rowPosition, width, rowHeight, attributes[j].getAlignment());
                c.setBounds(pt.x, pt.y, componentWidth, dim.height);
                columnPosition += width;
                columnPosition += attributes[j].getGap();
            } else {
                break;
            }
        }
        return columnPosition;
    }

    final int getComponentWidth(Dimension prefSize, int columnWidth, int column) {
        int componentWidth = 0;
        switch(attributes[column].getFillStyle()) {
            case FILL_COLUMN:
                componentWidth = columnWidth;
                break;
            case ABSOLUTE_WIDTH:
                componentWidth = attributes[column].getComponentWidth();
                break;
            case MAX_COMPONENT_WIDTH:
                componentWidth = attributes[column].getMaxComponentWidth();
                break;
            case PREFERRED_WIDTH:
                componentWidth = prefSize.width;
                break;
        }
        return componentWidth;
    }

    protected Point getAlignedPosition(int componentWidth, int componentHeight, int left, int top, int width, int height, int alignment) {
        Point result = new Point();
        int vertical = alignment & VERTICAL;
        switch(vertical) {
            case VCENTER:
                result.y = top + (height - componentHeight) / 2;
                break;
            case TOP:
                result.y = top;
                break;
            case BOTTOM:
                result.y = (top + height) - componentHeight;
                break;
        }
        int horizontal = alignment & HORIZONTAL;
        switch(horizontal) {
            case HCENTER:
                result.x = left + (width - componentWidth) / 2;
                break;
            case LEFT:
                result.x = left;
                break;
            case RIGHT:
                result.x = (left + width) - componentWidth;
                break;
        }
        return result;
    }

    /**
	 * Find the maximum width of a component in a particular column
	 */
    public int getMaxComponentWidth(int column) {
        int result = 0;
        int size = components.size();
        for (int i = column; i < size; i += attributes.length) {
            Component c = (Component) components.elementAt(i);
            Dimension dim = c.getPreferredSize();
            if (dim.width > result) result = dim.width;
        }
        return result;
    }

    /**
	 * Find the maximum width of a component over all columns
	 */
    public int getMaxComponentWidth() {
        int result = 0;
        int size = components.size();
        for (int i = 0; i < size; i++) {
            Component c = (Component) components.elementAt(i);
            Dimension dim = c.getPreferredSize();
            if (dim.width > result) result = dim.width;
        }
        return result;
    }

    public int getRowCount() {
        int size = components.size();
        int rowCount = size / attributes.length;
        if (size % attributes.length != 0) rowCount++;
        return rowCount;
    }

    public int getRowHeight(int row) {
        int result = 0;
        int columnCount = attributes.length;
        int rowOffset = row * columnCount;
        int size = components.size();
        for (int j = 0; j < columnCount; j++) {
            int index = j + rowOffset;
            if (index < size) {
                Component c = (Component) components.elementAt(index);
                Dimension dim = c.getPreferredSize();
                if (dim.height > result) result = dim.height;
            } else {
                break;
            }
        }
        return result;
    }

    public int getMaximumRowHeight() {
        int result = 0;
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int rowHeight = getRowHeight(i);
            if (rowHeight > result) result = rowHeight;
        }
        return result;
    }

    public Dimension minimumLayoutSize(Container target) {
        Dimension result = target.getMinimumSize();
        return result;
    }

    public Dimension preferredLayoutSize(Container target) {
        Insets targetInsets = target.getInsets();
        Dimension result = new Dimension();
        computeMaxComponentWidths();
        if (attributes[0].getWidth() == EQUAL_WIDTHS) {
            int maxWidth = getMaxComponentWidth() + attributes[0].getGap();
            result.width = maxWidth * attributes.length + attributes[0].getGap();
        } else {
            result.width = attributes[0].getGap();
            for (int i = 0; i < attributes.length; i++) {
                result.width += attributes[i].getWidth();
                result.width += attributes[i].getGap();
            }
        }
        int size = components.size();
        int rowCount = getRowCount();
        switch(preferredRowHeight) {
            case MAXIMUM_HEIGHT:
                for (int i = 0; i < rowCount; i++) {
                    int rowHeight = getRowHeight(i);
                    result.height += rowHeight;
                    result.height += rowGap;
                }
                break;
            case ALL_MAXIMUM_HEIGHT:
                result.height = rowCount * getMaximumRowHeight() + rowCount * rowGap;
                break;
            default:
                result.height = rowCount * preferredRowHeight + rowCount * rowGap;
                break;
        }
        result.width += (targetInsets.left + targetInsets.right);
        result.height += (targetInsets.top + targetInsets.bottom);
        return result;
    }

    public void removeLayoutComponent(Component c) {
        components.removeElement(c);
    }

    public void addLayoutComponent(Component component, Object constraint) {
        int index = components.indexOf(component);
        if (index == -1) {
            if (constraint == NEXT_ROW) {
                int size = components.size();
                int cols = attributes.length;
                int emptyCols = cols - (size % cols);
                for (int i = 0; i < emptyCols; i++) {
                    components.addElement(dummyComponent);
                }
            }
            components.addElement(component);
        }
    }

    public float getLayoutAlignmentX(Container target) {
        return (float) 0.0;
    }

    public float getLayoutAlignmentY(Container target) {
        return (float) 0.0;
    }

    public void invalidateLayout(Container target) {
    }

    public Dimension maximumLayoutSize(Container target) {
        return target.getSize();
    }

    public class ColumnAttributes implements Serializable {

        int width = EQUAL_WIDTHS;

        int alignment = LEFT | VCENTER;

        int gap = 3;

        int fillStyle = PREFERRED_WIDTH;

        int componentWidth = 0;

        int maxComponentWidth = 0;

        public ColumnAttributes() {
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            int result = width;
            if (width == MAXIMUM_WIDTH) result = getMaxComponentWidth();
            return result;
        }

        public void setAlignment(int alignment) {
            this.alignment = alignment;
        }

        public int getAlignment() {
            return alignment;
        }

        public void setGap(int gap) {
            this.gap = gap;
        }

        public int getGap() {
            return gap;
        }

        public void setFillStyle(int fillStyle) {
            this.fillStyle = fillStyle;
        }

        public int getFillStyle() {
            return fillStyle;
        }

        public void setComponentWidth(int componentWidth) {
            this.componentWidth = componentWidth;
        }

        public int getComponentWidth() {
            return componentWidth;
        }

        public void setMaxComponentWidth(int maxComponentWidth) {
            this.maxComponentWidth = maxComponentWidth;
        }

        public int getMaxComponentWidth() {
            return maxComponentWidth;
        }
    }

    class DummyComponent extends Component {

        Dimension prefSize = new Dimension(0, 0);

        public Dimension getPreferredSize() {
            return prefSize;
        }

        public void setBounds(int x, int y, int width, int height) {
        }
    }
}
