package com.qasystems.swing.table;

import com.qasystems.debug.DebugWriter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A sorter for TableModels.
 * The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. Sorter does not store or copy
 * the data in the TableModel, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * model. When the model changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its model via the mapping
 * array. That way the Sorter appears to hold another copy of
 * the table with the rows in a different order.
 * The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 * <br>
 * <b>Note:</b> the basis of this class is part of the
 * Java Tutorial examples.<br>
 * <br>
 * Taken from Philip Milne, version 1.5 12/17/97
 */
public class Sorter extends Map implements ChangeListener {

    private int[] INDEXES = null;

    private Vector SORTINGCOLUMNS = new Vector();

    /**
   * Holds the current sort order.
   */
    private boolean ASCENDING = true;

    private int CURRENT_SORTED_COLUMN = -1;

    private int COMPARES = 0;

    /**
   * Default constructor.
   */
    public Sorter() {
        super();
        setIndexes(new int[0]);
    }

    /**
   * Default constructor.
   *
   * @param model the model
   */
    public Sorter(TableModel model) {
        this();
        setModel(model);
    }

    /**
   * Set the TableModel for this classs.
   *
   * @param model the model
   */
    public synchronized void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /**
   * Get the value of a cell in the model.
   *
   * @param aRow the row index of the cell
   * @param aColumn the column index of the cell
   * @return the value of the cell
   */
    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return ((INDEXES.length > 0) ? MODEL.getValueAt(INDEXES[aRow], aColumn) : new Integer(-1));
    }

    /**
   * get the real index into the model
   *
   * @param row the rownumber in the sorted index
   * @return the rownumber in the model
   */
    public int getIndexedRowNumber(int row) {
        checkModel();
        return ((INDEXES.length > 0) ? INDEXES[row] : (-1));
    }

    /**
   * Set the value of a cell in the model.
   *
   * @param aValue the value to set in the cell
   * @param aRow the row index of the cell
   * @param aColumn the column index of the cell
   */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        MODEL.setValueAt(aValue, INDEXES[aRow], aColumn);
    }

    /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
    public int getMaxRowCount() {
        return ((MODEL == null) ? 0 : MODEL.getRowCount());
    }

    /**
   * Queries the filtered model for the number of rows
   *
   * @return the number of rows in filter.
   */
    public int getRowCount() {
        return ((INDEXES == null) ? 0 : INDEXES.length);
    }

    /**
   * Implementation of the TableModelListener interface.
   *
   * @param e the TableModelEvent
   */
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
    }

    /**
   * Sorts data in the column in ascending order. The column model i
   * used to update the table header.
   *
   * @param column the column index
   */
    public void sortByColumn(int column) {
        sortByColumn(column, ASCENDING);
    }

    /**
   * Reset this object.
   */
    public void reset() {
        setSortOrder(true);
        setCurrentSortedColumn(-1);
    }

    /**
   * Returns the current sort order.
   *
   * @return the following:
   * <ul>
   *   <li><tt>true</tt>, sorted in ascending order</li>
   *   <li><tt>false</tt>, sorted in descending order</li>
   * </ul>
   */
    public boolean getSortOrder() {
        return (ASCENDING);
    }

    /**
   * Return the current sorted column.
   *
   * @return the following
   * <ul>
   *   <li><tt>1</tt>, no column sorted at the moment </li>
   *   <li><tt>CURRENT_SORTED_COLUMN</tt>, otherwise</li>
   * </ul>
   */
    public int getCurrentSortedColumn() {
        return ((CURRENT_SORTED_COLUMN == -1) ? 1 : CURRENT_SORTED_COLUMN);
    }

    /**
   * Sorts data in the column.
   *
   * @param column the column index
   * @param ascending the sort order as follows,<br>
   * <ul>
   *   <li><tt>true</tt>, sort in ascending order</li>
   *   <li><tt>false</tt>, sort in descending order</li>
   * </ul>
   */
    private void sortByColumn(int column, boolean ascending) {
        setSortOrder(ascending);
        SORTINGCOLUMNS.removeAllElements();
        SORTINGCOLUMNS.addElement(new Integer(column));
        sort();
        super.tableChanged(new TableModelEvent(this));
        setCurrentSortedColumn(column);
    }

    private synchronized void setSortOrder(boolean order) {
        ASCENDING = order;
    }

    protected synchronized void setIndexes(int[] indexes) {
        INDEXES = indexes;
    }

    protected synchronized void setIndex(int index, int value) {
        INDEXES[index] = value;
    }

    private synchronized void setCurrentSortedColumn(int column) {
        CURRENT_SORTED_COLUMN = column;
    }

    private synchronized void setCompares(int compares) {
        COMPARES = compares;
    }

    protected void checkModel() {
        if (INDEXES.length > MODEL.getRowCount()) {
            new DebugWriter().writeMessage("Sorter not informed of a change in model.");
        }
    }

    private HeaderRenderer getRenderer(TableColumnModel columnModel, int column) {
        final TableColumn sortedColumn = columnModel.getColumn(column);
        return ((HeaderRenderer) sortedColumn.getHeaderRenderer());
    }

    /**
   * Notifies the column model which column is sorted.
   *
   * @param columnModel the column model
   * @param column the column index of the cell
   * @see HeaderRenderer
   */
    public void setSortedColumn(TableColumnModel columnModel, int column) {
        setSortedColumn(columnModel, column, ASCENDING);
    }

    /**
   * Notifies the column model which column is sorted.
   *
   * @param columnModel the column model
   * @param column the column index of the cell
   * @param order the sort order, <tt>true</tt> for ascending order
   * @see HeaderRenderer
   */
    private void setSortedColumn(TableColumnModel columnModel, int column, boolean order) {
        if (column == CURRENT_SORTED_COLUMN) {
            getRenderer(columnModel, column).setSortType((order ? HeaderRenderer.SORT_ASCENDING : HeaderRenderer.SORT_DESCENDING));
        } else {
            if (CURRENT_SORTED_COLUMN >= 0) {
                getRenderer(columnModel, CURRENT_SORTED_COLUMN).setSortType(HeaderRenderer.NO_SORT);
            }
            getRenderer(columnModel, column).setSortType(HeaderRenderer.SORT_ASCENDING);
        }
    }

    private int compareRowsByColumn(int row1, int row2, int column) {
        final Class type = MODEL.getColumnClass(column);
        final TableModel data = MODEL;
        final Object o1 = data.getValueAt(row1, column);
        final Object o2 = data.getValueAt(row2, column);
        if ((o1 == null) && (o2 == null)) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
        }
        if (java.lang.Number.class.equals(type.getSuperclass())) {
            final Number n1 = (Number) data.getValueAt(row1, column);
            final double d1 = n1.doubleValue();
            final Number n2 = (Number) data.getValueAt(row2, column);
            final double d2 = n2.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (java.util.Date.class.equals(type)) {
            final Date d1 = (Date) data.getValueAt(row1, column);
            final long n1 = d1.getTime();
            final Date d2 = (Date) data.getValueAt(row2, column);
            final long n2 = d2.getTime();
            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (String.class.equals(type)) {
            final String s1 = (String) data.getValueAt(row1, column);
            final String s2 = (String) data.getValueAt(row2, column);
            final int result = s1.compareTo(s2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (Boolean.class.equals(type)) {
            final Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            final boolean b1 = bool1.booleanValue();
            final Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            final boolean b2 = bool2.booleanValue();
            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
        } else {
            final Object v1 = data.getValueAt(row1, column);
            final String s1 = v1.toString();
            final Object v2 = data.getValueAt(row2, column);
            final String s2 = v2.toString();
            final int result = s1.compareTo(s2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
   * Implementation of the ChangeListener interface.
   *
   * @param event the change event
   */
    public void stateChanged(ChangeEvent event) {
        tableChanged(new TableModelEvent(this));
    }

    private void sort() {
        checkModel();
        setCompares(0);
        shuttlesort((int[]) INDEXES.clone(), INDEXES, 0, INDEXES.length);
    }

    private int compare(int row1, int row2) {
        int retval = 0;
        setCompares(COMPARES + 1);
        for (int level = 0; level < SORTINGCOLUMNS.size(); level++) {
            final Integer column = (Integer) SORTINGCOLUMNS.elementAt(level);
            final int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                retval = ASCENDING ? result : (-result);
            }
        }
        return retval;
    }

    /**
   * DOCUMENT ME!
   */
    public void reallocateIndexes() {
        final int rowCount = MODEL.getRowCount();
        setIndexes(new int[rowCount]);
        for (int row = 0; row < rowCount; row++) {
            setIndex(row, row);
        }
    }

    private void shuttlesort(int[] from, int[] to, int low, int high) {
        if ((high - low) < 2) {
            return;
        }
        final int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (((high - low) >= 4) && (compare(from[middle - 1], from[middle]) <= 0)) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if ((q >= high) || ((p < middle) && (compare(from[p], from[q]) <= 0))) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    /**
   * Add a mouse listener to the header of the table
   * @param table the table to listen to
   */
    public void addMouseListenerToHeaderInTable(JTable table) {
        final JTable tableView = table;
        final MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                final TableColumnModel columnModel = tableView.getColumnModel();
                final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                final int column = tableView.convertColumnIndexToModel(viewColumn);
                if ((e.getClickCount() == 1) && (column != -1)) {
                    if (column == CURRENT_SORTED_COLUMN) {
                        setSortOrder(!ASCENDING);
                    }
                    sortByColumn(column);
                }
            }
        };
        tableView.getTableHeader().addMouseListener(listMouseListener);
    }

    /**
   * Returns <i>class_name</i>@<i>object_hashcode</i>.
   *
   * @return the string
   */
    public String toString() {
        return (getClass().getName() + "@" + Integer.toHexString(hashCode()));
    }
}
