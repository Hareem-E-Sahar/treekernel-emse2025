package org.xnap.commons.gui.table;

import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class TableSorter extends AbstractTableModel implements SortableModel {

    protected int indexes[] = new int[0];

    protected int revIndexes[] = new int[0];

    protected ArrayList<Integer> sortingColumns = new ArrayList<Integer>();

    protected Order sortOrder = Order.UNSORTED;

    /**
     * Counts number of compares.
     */
    protected int compares;

    protected int lastSortedColumn = -1;

    protected boolean maintainSortOrder;

    private TableModel tableModel;

    private TableModelListener tableModelListener;

    public TableSorter() {
        this.tableModelListener = new TableModelHandler();
    }

    public TableSorter(TableModel tableModel) {
        this();
        setTableModel(tableModel);
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    public Class<?> getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(mapToIndex(row), column);
    }

    public Object getValueAt(int row, int column) {
        synchronized (indexes) {
            return tableModel.getValueAt(mapToIndex(row), column);
        }
    }

    public void setValueAt(Object aValue, int row, int column) {
        synchronized (indexes) {
            tableModel.setValueAt(aValue, mapToIndex(row), column);
        }
    }

    public int getSortedColumn() {
        return lastSortedColumn;
    }

    public Order getSortOrder() {
        return sortOrder;
    }

    /**
     * Returns the mapped row index.
     */
    public int mapToIndex(int i) {
        return indexes[i];
    }

    public void setMaintainSortOrder(boolean newValue) {
        maintainSortOrder = newValue;
    }

    public void setSortOrder(Order newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException();
        }
        sortOrder = newValue;
    }

    public void setTableModel(TableModel tableModel) {
        if (this.tableModel != null) {
            this.tableModel.removeTableModelListener(tableModelListener);
        }
        this.tableModel = tableModel;
        if (this.tableModel != null) {
            this.tableModel.addTableModelListener(tableModelListener);
        }
        reallocateIndexes();
        fireTableStructureChanged();
    }

    /**
     * Sorts the table.
	 *
	 * @return true, if table is sorted ascending; false, if descending
     */
    public Order sortByColumn(int column, Order sortOrder, boolean revert) {
        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("Column is invalid");
        }
        sortingColumns.clear();
        sortingColumns.add(new Integer(column));
        setSortOrder(sortOrder);
        if (!sort() && revert) {
            setSortOrder(sortOrder.next());
            sort();
        }
        lastSortedColumn = column;
        return getSortOrder();
    }

    public void resort() {
        if (lastSortedColumn != -1) {
            sortByColumn(lastSortedColumn, getSortOrder(), false);
        }
    }

    /**
     * Compares two rows.
     */
    protected int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            int column = sortingColumns.get(level);
            int result = compareRowsByColumn(row1, row2, column);
            if (result != 0) {
                return (sortOrder == Order.ASCENDING) ? result : -result;
            }
        }
        return 0;
    }

    /**
     * Compares two rows by a column.
     */
    @SuppressWarnings("unchecked")
    protected int compareRowsByColumn(int row1, int row2, int column) {
        Class type = getColumnClass(column);
        Object o1 = tableModel.getValueAt(row1, column);
        Object o2 = tableModel.getValueAt(row2, column);
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        if (type == String.class) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int result = s1.compareToIgnoreCase(s2);
            if (s1.length() == 0 ^ s2.length() == 0) {
                result = -result;
            }
            return result;
        } else if (o1 instanceof Comparable) {
            return ((Comparable) o1).compareTo(o2);
        } else {
            return o1.toString().compareTo(o2.toString());
        }
    }

    protected void reallocateIndexes(TableModelEvent e) {
        int rowCount = getRowCount();
        if (rowCount == indexes.length) return;
        int newIndexes[] = new int[rowCount];
        int newRevIndexes[] = new int[rowCount];
        synchronized (indexes) {
            int row = 0;
            if (e != null && e.getType() == TableModelEvent.DELETE) {
                int skipped = 0;
                for (; row < indexes.length; row++) {
                    int dec = 0;
                    boolean skip = false;
                    for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                        if (i < indexes[row]) dec++; else if (i == indexes[row]) skip = true;
                    }
                    if (skip) {
                        skipped++;
                        continue;
                    }
                    newIndexes[row - skipped] = indexes[row] - dec;
                    newRevIndexes[indexes[row] - dec] = row - skipped;
                }
            } else if (e != null && (e.getType() == TableModelEvent.UPDATE && e.getLastRow() >= indexes.length)) {
                for (int i = 0; i < rowCount; i++) {
                    newIndexes[i] = i;
                    newRevIndexes[i] = i;
                }
            } else {
                for (; row < indexes.length && row < rowCount; row++) {
                    newIndexes[row] = indexes[row];
                    newRevIndexes[row] = revIndexes[row];
                }
                for (; row < rowCount; row++) {
                    newIndexes[row] = row;
                    newRevIndexes[row] = row;
                }
            }
            indexes = newIndexes;
            revIndexes = newRevIndexes;
        }
    }

    protected void reallocateIndexes() {
        reallocateIndexes(null);
    }

    /**
     * Returns false if nothing has changed.
     */
    protected boolean sort() {
        synchronized (indexes) {
            if (sortOrder == Order.UNSORTED) {
                for (int i = 0; i < indexes.length; i++) {
                    indexes[i] = i;
                    revIndexes[i] = i;
                }
                return false;
            }
            compares = 0;
            int[] oldIndexes = new int[indexes.length];
            System.arraycopy(indexes, 0, oldIndexes, 0, indexes.length);
            if (shuttlesort(oldIndexes, indexes, 0, indexes.length)) {
                for (int i = 0; i < indexes.length; i++) {
                    revIndexes[indexes[i]] = i;
                }
                fireTableChanged(new TableModelEvent(this));
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return false;
        }
        boolean changed = false;
        int middle = (low + high) / 2;
        changed |= shuttlesort(to, from, low, middle);
        changed |= shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4) {
            if (compare(from[middle - 1], from[middle]) <= 0) {
                for (int i = low; i < high; i++) {
                    to[i] = from[i];
                }
                return changed;
            } else {
                if (compare(from[high - 1], from[low]) < 0) {
                    int i = low;
                    for (; q < high; q++) {
                        to[i++] = from[q];
                    }
                    for (; i < high; i++) {
                        to[i] = from[p++];
                    }
                    return changed;
                }
            }
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                changed |= (p < middle);
                to[i] = from[q++];
            }
        }
        return changed;
    }

    private class TableModelHandler implements TableModelListener {

        /**
	     * We need to mangle these events to fire the right indicies.
	     */
        public void tableChanged(TableModelEvent e) {
            reallocateIndexes(e);
            if (maintainSortOrder) {
                if (sort()) {
                    return;
                }
            }
            if (e.getType() == TableModelEvent.DELETE || (e.getType() == TableModelEvent.UPDATE && e.getLastRow() >= revIndexes.length)) {
                fireTableChanged(new TableModelEvent(TableSorter.this));
            } else {
                for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                    TableModelEvent t = new TableModelEvent(TableSorter.this, revIndexes[i], revIndexes[i], e.getColumn(), e.getType());
                    fireTableChanged(t);
                }
            }
        }
    }

    public boolean getMaintainSortOrder() {
        return maintainSortOrder;
    }
}
