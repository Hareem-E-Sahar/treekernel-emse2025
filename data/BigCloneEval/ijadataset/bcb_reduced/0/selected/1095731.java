package util.swing;

import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy
 * the data in the TableModel, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * model. When the model changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its model via the mapping
 * array. That way the TableSorter appears to hold another copy of the table
 * with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 *
 * @version 1.6 2003-03-25
 * @author Philip Milne
 * @author Thomas Medack
 * @author Andreas Bartho
 */
public class TableSorter extends TableMap {

    /**
     * Field of indexes.
     */
    private int indexes[], reverseIndexes[];

    /**
     * Sorting vector.
     */
    private Vector sortingColumns = new Vector();

    /**
     * Sort ascending or descending.
     */
    private boolean ascending = true;

    /**
     * The column to be sorted.
     */
    private int actualColumn = 0;

    /**
     * Internal helper variable.
     */
    private int compares;

    /**
     * Constructor.
     */
    public TableSorter() {
        indexes = new int[0];
    }

    /**
     * Constructor.
     * @param model the wrapped {@link TableModel}
     */
    public TableSorter(AbstractTableModel model) {
        setModel(model);
    }

    /**
     * Sets the {@link TableModel}.
     *
     * @param model the model.
     */
    public void setModel(AbstractTableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /**
     * Compares columns of two specific rows.
     * @param row1 first row.
     * @param row2 second row.
     * @param column the column index.
     * @return a comparative value.
     */
    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        TableModel data = model;
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        String s1 = o1.toString();
        String s2 = o2.toString();
        if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
            java.util.Comparator cmp = model.getEntryDescriptor().getColumnOrder(column);
            return (cmp == null) ? ((Comparable) o1).compareTo(o2) : cmp.compare(model.getRecord(row1), model.getRecord(row2));
        } else {
            int result = s1.compareTo(s2);
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
     * Comparison of two rows.
     *
     * @param row1 first row.
     * @param row2 second row.
     * @return a comparative value.
     */
    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            Integer column = (Integer) sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    /**
     * Helper method.
     */
    private void reallocateIndexes() {
        int rowCount = model.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    /**
     * Reacts on TableChangeEvents, either converts them as needed or passes them to the model.
     * @param e the event.
     */
    public void tableChanged(TableModelEvent e) {
        if (indexes.length == getModel().getRowCount() && indexes.length != 0) {
            createReverseIndexes();
            super.tableChanged(new TableModelEvent(this, reverseIndexes[e.getFirstRow()]));
        } else {
            reallocateIndexes();
            super.tableChanged(e);
        }
    }

    /**
     * Creates a reverse index table.
     *
     * This method maps the line number for each table to have the view updated
     *
     * Example: indexes = [2 3 0 1 4]<br>
     * reverseIndex stores in i, where i appeares in indexes:<br>
     * 0 is on position 2, 1 is on position 3, 2 is on position 0 ... => reverseIndex = [2 3 0 1 4]<br>
     * Mapping this array with the row number sent by a TableChangeEvent event, all tables have the
     * correct lines updated (has only effect on CountingStocks)
     */
    private void createReverseIndexes() {
        reverseIndexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            for (int j = 0; j < indexes.length; j++) {
                if (indexes[j] == i) reverseIndexes[i] = j;
            }
        }
    }

    /**
     * Helper method.
     */
    private void checkModel() {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    /**
     * Sorts the model. It uses {@link shuttlesort(int, int, int, int) shuttlesort}.
     */
    private void sort() {
        checkModel();
        compares = 0;
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
    }

    /**
     * Sorting method.
     *
     * @param from the unsorted index array.
     * @param to the sorted index array (i.e. the result).
     * @param low lowest field to be taken into consideration on sorting.
     * @param high highest field to be taken into consideration on sorting.
     */
    private void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    /**
     * Helper method. Swaps two ints.
     *
     * @param i first int.
     * @param j secont int
     */
    private void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    /**
   * Changes the value of a table cell.
   *
   * @param aValue the value to set.
   * @param aRow the row of the TableCell to be changed.
   * @param aColumn the column of the table cell to be changed.
   */
    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        if (aRow >= 0 && model.getRowCount() > 0) {
            return model.getValueAt(indexes[aRow], aColumn);
        } else {
            return null;
        }
    }

    /**
     * Gets the record.
     *
     * @param row the affected table row.
     * @return the appropriate record.
     */
    public Object getRecord(int row) {
        checkModel();
        if (row >= 0 && model.getRowCount() > 0 && row < model.getRowCount()) {
            return model.getRecord(indexes[row]);
        } else {
            return null;
        }
    }

    /**
     * Changes the value of a table cell.
     *
     * @param aValue the value to set.
     * @param aRow the row of the TableCell to be changed.
     * @param aColumn the column of the table cell to be changed.
     */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        if (aRow >= 0) {
            model.setValueAt(aValue, indexes[aRow], aColumn);
        }
    }

    /**
     * Sorts the table ascending by a column.
     *
     * @param column the column by which the table should be sorted.
     */
    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    /**
     * Sorts the table by a column.
     *
     * @param column the column by which the table should be sorted.
     * @param ascending if <code>true</code> sort sequence is ascending, otherwise descending.
     */
    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort();
        super.tableChanged(new TableModelEvent(this));
    }

    /**
     * Adds a mouse listener to the table header.
     *
     * @param table the table to which header the listener is to be added
     */
    public void addMouseListenerToHeaderInTable(JTable table, final Object[] ao) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        final TableEntryDescriptor ted = getModel().getEntryDescriptor();
        ascending = true;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            int activeColumn = -1;

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (column != activeColumn) {
                    ascending = false;
                    activeColumn = column;
                }
                if (e.getClickCount() == 1 && column != -1) {
                    if (ted.canSortByColumn(column)) {
                        actualColumn = column;
                        ascending = !ascending;
                        ao[0] = new Integer(column);
                        ao[1] = new Boolean(ascending);
                        sorter.sortByColumn(actualColumn, ascending);
                        tableView.getTableHeader().resizeAndRepaint();
                    }
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
