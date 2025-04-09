package net.sf.javadc.util.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Layout manager based on idea of design grid. For description please see tutorial included in download bundle.
 * 
 * @see net.sf.javadc.util.layout.HIGConstraints
 * @version 1.0 1/19/2002
 * @author Daniel Michalik (dmi@autel.cz), Romano Caserta (caserta@disy.net), Frank Behrens (frank@pinky.sax.de), Sven
 *         Behrens (behrens@disy.net) Alberto Ricart (aricart@smartsoft.com), Peter Reilly (Peter.Reilly@marconi.com)
 */
public class HIGLayout implements LayoutManager2, java.io.Serializable {

    /**
     *  
     */
    private static final long serialVersionUID = 6017504099136886347L;

    private int[] colWidths;

    private int[] rowHeights;

    private int colCount;

    private int rowCount;

    private ArrayList[] colComponents;

    private ArrayList[] rowComponents;

    private int[] widenWeights;

    private int[] heightenWeights;

    private int[] preferredWidths;

    private int[] preferredHeights;

    private int widenWeightsSum = 0;

    private int heightenWeightsSum = 0;

    private final int invisible = 0;

    ;

    private HashMap components = new HashMap();

    private int cacheColumnsX[];

    private int cacheRowsY[];

    private Dimension cachePreferredLayoutSize;

    private Dimension cacheMinimumLayoutSize;

    /**
     * Construct a new layout object. Length of passed arrays define number of columns and number of rows. Each width or
     * height can be less then 0, equal 0 or greater then 0. Passed arrays defines design grid - sizes and dependences
     * between columns and rows. For details see tutorial.
     * 
     * @param widths array of column widths.
     * @param heights array of row heights.
     */
    public HIGLayout(int[] widths, int[] heights) {
        colCount = widths.length;
        rowCount = heights.length;
        colWidths = new int[colCount + 1];
        System.arraycopy(widths, 0, colWidths, 1, colCount);
        rowHeights = new int[rowCount + 1];
        System.arraycopy(heights, 0, rowHeights, 1, rowCount);
        widenWeights = new int[colCount + 1];
        heightenWeights = new int[rowCount + 1];
        preferredWidths = new int[colCount + 1];
        preferredHeights = new int[rowCount + 1];
        colComponents = new ArrayList[colCount + 1];
        rowComponents = new ArrayList[rowCount + 1];
    }

    /**
     * Adds the specified component to the layout, using the HIGConstraints constraint object. Constraints object is
     * copied so passed instance can be safely modifed.
     * 
     * @param comp the component to be added
     * @param HIGConstraints object determining where/how the component is added to the layout.
     * @see net.sf.javadc.util.layout.HIGConstraints
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        synchronized (comp.getTreeLock()) {
            HIGConstraints constr = (HIGConstraints) constraints;
            if (constr.x > colCount) {
                throw new RuntimeException("Column index in constraint object cannot be greater then " + colCount + ".");
            }
            if (constr.x + constr.w - 1 > colCount) {
                throw new RuntimeException("Width in constraint object cannot be greater then " + (colCount - constr.x + 1) + ".");
            }
            if (constr.y > rowCount) {
                throw new RuntimeException("Row index in constraint object cannot be greater then " + rowCount + ".");
            }
            if (constr.y + constr.h - 1 > rowCount) {
                throw new RuntimeException("Height in constraint object cannot be greater then " + (rowCount - constr.y + 1) + ".");
            }
            if (constr.w == 1) {
                if (colComponents[constr.x] == null) {
                    colComponents[constr.x] = new ArrayList(3);
                }
                colComponents[constr.x].add(comp);
            }
            if (constr.h == 1) {
                if (rowComponents[constr.y] == null) {
                    rowComponents[constr.y] = new ArrayList(3);
                }
                rowComponents[constr.y].add(comp);
            }
            components.put(comp, new HIGConstraints(constr));
        }
    }

    /**
     * @deprecated replaced by <code>addLayoutComponent(Component, Object)</code>. Throws
     *             <EM>UnsupportedOperationException</EM>.
     */
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns 0.
     */
    public float getLayoutAlignmentX(Container target) {
        return 0f;
    }

    /**
     * Returns 0.
     */
    public float getLayoutAlignmentY(Container target) {
        return 0f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached information it should be discarded.
     */
    public void invalidateLayout(Container target) {
        cacheColumnsX = null;
        cacheRowsY = null;
        cachePreferredLayoutSize = null;
        cacheMinimumLayoutSize = null;
    }

    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dimSize = target.getSize();
            int sizeWidth = dimSize.width;
            int sizeHeight = dimSize.height;
            Insets insets = target.getInsets();
            sizeWidth -= insets.left + insets.right;
            sizeHeight -= insets.top + insets.bottom;
            int x[] = getColumnsX(sizeWidth, insets);
            int y[] = getRowsY(sizeHeight, insets);
            Component comps[] = target.getComponents();
            for (int i = comps.length - 1; i >= 0; i--) {
                Component comp = comps[i];
                HIGConstraints c = (HIGConstraints) components.get(comp);
                if (c == null) {
                    continue;
                }
                Dimension dimPref = comp.getPreferredSize();
                int width = dimPref.width;
                int height = dimPref.height;
                int cellw;
                int cellh;
                if (c.w < 0) {
                    width = -c.w;
                    cellw = x[c.x + 1] - x[c.x];
                } else {
                    width += c.wCorrection;
                    cellw = x[c.x + c.w] - x[c.x];
                }
                if (c.h < 0) {
                    height = -c.h;
                    cellh = y[c.y + 1] - y[c.y];
                } else {
                    height += c.hCorrection;
                    cellh = y[c.y + c.h] - y[c.y];
                }
                boolean allowXSize = true;
                boolean allowYSize = true;
                float dw = (cellw - width) / 2.0f;
                float dh = (cellh - height) / 2.0f;
                float compx = x[c.x] + dw;
                float compy = y[c.y] + dh;
                String anchor = c.anchor;
                boolean xSize = false;
                boolean ySize = false;
                for (int j = anchor.length() - 1; j >= 0; j--) {
                    if (anchor.charAt(j) == 'l') {
                        compx = x[c.x];
                        if (xSize && allowXSize) {
                            width = cellw;
                        }
                        xSize = true;
                    } else if (anchor.charAt(j) == 'r') {
                        if (xSize && allowXSize) {
                            width = cellw;
                        } else {
                            compx += dw;
                        }
                        xSize = true;
                    } else if (anchor.charAt(j) == 't') {
                        compy = y[c.y];
                        if (ySize && allowYSize) {
                            height = cellh;
                        }
                        ySize = true;
                    } else if (anchor.charAt(j) == 'b') {
                        if (ySize && allowYSize) {
                            height = cellh;
                        } else {
                            compy += dh;
                        }
                        ySize = true;
                    } else {
                        throw new RuntimeException("Wrong character in anchor.");
                    }
                }
                comp.setBounds((int) compx + c.xCorrection, (int) compy + c.yCorrection, width, height);
            }
        }
    }

    /**
     * Returns the maximum size of this component.
     * 
     * @see java.awt.Component#getMinimumSize()
     * @see java.awt.Component#getPreferredSize()
     * @see LayoutManager
     */
    public Dimension maximumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            if (cacheMinimumLayoutSize != null) {
                return cacheMinimumLayoutSize;
            }
            final int[] minColWidths = calcMinWidths();
            final int[] minRowHeights = calcMinHeights();
            Insets insets = target.getInsets();
            int w = insets.left + insets.right;
            int h = insets.top + insets.bottom;
            for (int i = 1; i <= colCount; i++) {
                w += minColWidths[i];
            }
            for (int i = 1; i <= rowCount; i++) {
                h += minRowHeights[i];
            }
            cacheMinimumLayoutSize = new Dimension(w, h);
            return cacheMinimumLayoutSize;
        }
    }

    /**
     * Calculates the preferred size dimensions for the specified container given the components in the specified parent
     * container.
     * 
     * @param parent the component to be laid out
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            if (cachePreferredLayoutSize != null) {
                return cachePreferredLayoutSize;
            }
            final int[] prefColWidths = calcPreferredWidths();
            final int[] prefRowHeights = calcPreferredHeights();
            Insets insets = target.getInsets();
            int w = insets.left + insets.right;
            int h = insets.top + insets.bottom;
            for (int i = 1; i <= colCount; i++) {
                w += prefColWidths[i];
            }
            for (int i = 1; i <= rowCount; i++) {
                h += prefRowHeights[i];
            }
            cachePreferredLayoutSize = new Dimension(w, h);
            return cachePreferredLayoutSize;
        }
    }

    /**
     * Removes the specified component from the layout.
     * 
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
            HIGConstraints c = (HIGConstraints) components.remove(comp);
            if (c == null) {
                return;
            }
            if (colComponents[c.x] != null) {
                colComponents[c.x].remove(comp);
            }
            if (rowComponents[c.y] != null) {
                rowComponents[c.y].remove(comp);
            }
        }
    }

    /**
     * Sets weight of specified column. Weight determines distribution of difference when resizing.
     * 
     * @param col index of column. Index must be > 0.
     */
    public void setColumnWeight(int col, int weight) {
        if (col > colCount) {
            throw new RuntimeException("Column index cannot be greater then " + colCount + ".");
        }
        widenWeights[col] = weight;
        widenWeightsSum = 0;
        for (int i = 1; i <= colCount; i++) {
            widenWeightsSum += widenWeights[i];
        }
    }

    /**
     * Sets column width, realloc arrays if there is need.
     * 
     * @since 0.97
     */
    public void setColumnWidth(int col, int width) {
        if (colCount < col) {
            colCount = col;
        }
        if (colWidths.length <= col) {
            colWidths = (int[]) reallocArray(colWidths, colCount + 3);
            widenWeights = (int[]) reallocArray(widenWeights, colCount + 3);
            colComponents = (ArrayList[]) reallocArray(colComponents, colCount + 3);
            preferredWidths = (int[]) reallocArray(preferredWidths, colCount + 3);
        }
        colWidths[col] = width;
    }

    /**
     * Sets preferred width of specified column.
     * 
     * @param col index of column. Index must be > 0.
     * @param width the width to use in pixels
     * @since 1.0
     */
    public void setPreferredColumnWidth(int col, int width) {
        if (col > colCount) {
            throw new IllegalArgumentException("Column index cannot be greater then " + colCount + ".");
        }
        preferredWidths[col] = width;
    }

    /**
     * Sets preferred height of specified row. of difference when resizing.
     * 
     * @param row index of row. Index must be > 0.
     * @param height the height in pixels
     * @since 1.0
     */
    public void setPreferredRowHeight(int row, int height) {
        if (row > rowCount) {
            throw new IllegalArgumentException("Column index cannot be greater then " + rowCount + ".");
        }
        preferredHeights[row] = height;
    }

    /**
     * Sets row height, realloc arrays if there is need.
     * 
     * @since 0.97
     */
    public void setRowHeight(int row, int height) {
        if (rowCount < row) {
            rowCount = row;
        }
        if (rowHeights.length <= row) {
            rowHeights = (int[]) reallocArray(rowHeights, rowCount + 3);
            heightenWeights = (int[]) reallocArray(heightenWeights, rowCount + 3);
            rowComponents = (ArrayList[]) reallocArray(rowComponents, rowCount + 3);
        }
        rowHeights[row] = height;
    }

    /**
     * Sets weight of specified row. Weight determines distribution of difference when resizing.
     * 
     * @param row index of row. Index must be > 0.
     */
    public void setRowWeight(int row, int weight) {
        if (row > rowCount) {
            throw new RuntimeException("Column index cannot be greater then " + rowCount + ".");
        }
        heightenWeights[row] = weight;
        heightenWeightsSum = 0;
        for (int i = 1; i <= rowCount; i++) {
            heightenWeightsSum += heightenWeights[i];
        }
    }

    private int[] calcMinHeights() {
        int[] heights = new int[rowCount + 1];
        for (int i = 1; i <= rowCount; i++) {
            if (rowHeights[i] > 0) {
                heights[i] = rowHeights[i];
            } else {
                int maxHeight = 0;
                ArrayList iComps = rowComponents[i];
                if (iComps != null) {
                    for (int j = iComps.size() - 1; j > -1; j--) {
                        Component c = (Component) iComps.get(j);
                        int height = c.isVisible() ? c.getMinimumSize().height : invisible;
                        if (height > 0) {
                            HIGConstraints constr = (HIGConstraints) components.get(c);
                            if (constr.h < 0) {
                                height = -constr.h;
                            } else {
                                height += constr.hCorrection;
                            }
                        }
                        maxHeight = height > maxHeight ? height : maxHeight;
                    }
                }
                heights[i] = maxHeight;
            }
        }
        solveCycles(rowHeights, heights);
        return heights;
    }

    private int[] calcMinWidths() {
        int[] widths = new int[colCount + 1];
        for (int i = 1; i <= colCount; i++) {
            if (colWidths[i] > 0) {
                widths[i] = colWidths[i];
            } else {
                ArrayList iComps = colComponents[i];
                int maxWidth = 0;
                if (iComps != null) {
                    for (int j = iComps.size() - 1; j > -1; j--) {
                        Component c = (Component) iComps.get(j);
                        int width = c.isVisible() ? c.getMinimumSize().width : 0;
                        if (width > 0) {
                            HIGConstraints constr = (HIGConstraints) components.get(c);
                            if (constr.w < 0) {
                                width = -constr.w;
                            } else {
                                width += constr.wCorrection;
                            }
                        }
                        maxWidth = width > maxWidth ? width : maxWidth;
                    }
                }
                widths[i] = maxWidth;
            }
        }
        solveCycles(colWidths, widths);
        return widths;
    }

    private int[] calcPreferredHeights() {
        int[] heights = new int[rowCount + 1];
        for (int i = 1; i <= rowCount; i++) {
            if (rowHeights[i] > 0) {
                heights[i] = rowHeights[i];
            } else if (preferredHeights[i] > 0) {
                heights[i] = preferredHeights[i];
            } else {
                ArrayList iComps = rowComponents[i];
                int maxHeight = 0;
                if (iComps != null) {
                    for (int j = iComps.size() - 1; j >= 0; j--) {
                        Component c = (Component) iComps.get(j);
                        int height = c.isVisible() ? c.getPreferredSize().height : invisible;
                        if (height > 0) {
                            HIGConstraints constr = (HIGConstraints) components.get(c);
                            if (constr.h < 0) {
                                height = -constr.h;
                            } else {
                                height += constr.hCorrection;
                            }
                        }
                        maxHeight = height > maxHeight ? height : maxHeight;
                    }
                }
                heights[i] = maxHeight;
            }
        }
        solveCycles(rowHeights, heights);
        return heights;
    }

    private int[] calcPreferredWidths() {
        int[] widths = new int[colCount + 1];
        for (int i = 1; i <= colCount; i++) {
            if (colWidths[i] > 0) {
                widths[i] = colWidths[i];
            } else if (preferredWidths[i] > 0) {
                widths[i] = preferredWidths[i];
            } else {
                int maxWidth = 0;
                ArrayList iComps = colComponents[i];
                if (iComps != null) {
                    for (int j = iComps.size() - 1; j > -1; j--) {
                        Component c = (Component) iComps.get(j);
                        int width = c.isVisible() ? c.getPreferredSize().width : invisible;
                        if (width > 0) {
                            HIGConstraints constr = (HIGConstraints) components.get(c);
                            if (constr.w < 0) {
                                width = -constr.w;
                            } else {
                                width += constr.wCorrection;
                            }
                        }
                        maxWidth = width > maxWidth ? width : maxWidth;
                    }
                }
                widths[i] = maxWidth;
            }
        }
        solveCycles(colWidths, widths);
        return widths;
    }

    private void distributeSizeDifference(int desiredLength, int[] lengths, int[] minLengths, int[] weights, int weightSum) {
        int preferred = 0;
        int newLength;
        for (int i = lengths.length - 1; i > 0; i--) {
            preferred += lengths[i];
        }
        double unit = (double) (desiredLength - preferred) / (double) weightSum;
        for (int i = lengths.length - 1; i > 0; i--) {
            newLength = lengths[i] + (int) (unit * weights[i]);
            lengths[i] = newLength > minLengths[i] ? newLength : minLengths[i];
        }
    }

    /**
     * @since 0.97
     */
    private Object reallocArray(Object src, int newSize) {
        Object dest = java.lang.reflect.Array.newInstance(src.getClass().getComponentType(), newSize);
        System.arraycopy(src, 0, dest, 0, java.lang.reflect.Array.getLength(src));
        return dest;
    }

    private void solveCycles(int g[], int lengths[]) {
        int path[] = new int[g.length];
        int stackptr = 0;
        byte visited[] = new byte[g.length];
        for (int i = g.length - 1; i > 0; i--) {
            if (g[i] < 0 && visited[i] == 0) {
                int current = i;
                stackptr = 0;
                int maxLength = 0;
                int last;
                do {
                    maxLength = lengths[current] > maxLength ? lengths[current] : maxLength;
                    path[stackptr++] = current;
                    visited[current] = 1;
                    last = current;
                    current = -g[current];
                } while (current > 0 && visited[current] == 0);
                if (current <= 0) {
                    maxLength = lengths[last];
                } else if (current == 0) {
                    maxLength = lengths[last];
                } else if (visited[current] == 1) {
                    int start = current;
                    maxLength = 0;
                    do {
                        maxLength = lengths[current] > maxLength ? lengths[current] : maxLength;
                        current = -g[current];
                    } while (start != current);
                } else if (visited[current] == 2) {
                    maxLength = lengths[current];
                } else {
                    throw new RuntimeException("This should not happen.");
                }
                while (stackptr > 0) {
                    lengths[path[--stackptr]] = maxLength;
                    visited[path[stackptr]] = 2;
                }
            }
        }
    }

    /**
     * returns array of x-coordinates of columns. First coordinate is stored in x[1] Reference to this array is cached,
     * so data should not be modified.
     */
    int[] getColumnsX(int targetWidth, Insets insets) {
        if (cacheColumnsX != null) {
            return cacheColumnsX;
        }
        int[] prefColWidths = calcPreferredWidths();
        int[] minColWidths = calcMinWidths();
        distributeSizeDifference(targetWidth, prefColWidths, minColWidths, widenWeights, widenWeightsSum);
        int x[] = new int[colCount + 2];
        x[1] = insets == null ? 0 : insets.left;
        for (int i = 2; i <= colCount + 1; i++) {
            x[i] = x[i - 1] + prefColWidths[i - 1];
        }
        cacheColumnsX = x;
        return x;
    }

    /**
     * returns array of y-coordinates of rows. First coordinate is stored in y[1]. Reference to this array is cached, so
     * data should not be modified.
     */
    int[] getRowsY(int targetHeight, Insets insets) {
        if (cacheRowsY != null) {
            return cacheRowsY;
        }
        int[] prefRowHeights = calcPreferredHeights();
        int[] minRowHeights = calcMinHeights();
        distributeSizeDifference(targetHeight, prefRowHeights, minRowHeights, heightenWeights, heightenWeightsSum);
        int y[] = new int[rowCount + 2];
        y[1] = insets == null ? 0 : insets.top;
        for (int i = 2; i <= rowCount + 1; i++) {
            y[i] = y[i - 1] + prefRowHeights[i - 1];
        }
        cacheRowsY = y;
        return y;
    }
}
