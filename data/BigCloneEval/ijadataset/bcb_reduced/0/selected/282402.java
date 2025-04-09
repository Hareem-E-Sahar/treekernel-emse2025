package org.mars_sim.msp.ui.standard.monitor;

import java.util.Arrays;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * This TableModel acts as a proxy to provide sorting on a remote Table Model.
 * It is based on the TableSorter provided as part of the Swing UI Tutorial but
 * this version has been simplified as it assumes that only column types that
 * are Comparable will be sorted.
 * Also only one column can be used as a sorting key
 */
public class TableSorter extends AbstractTableModel implements TableModelListener, MonitorModel {

    int indexes[];

    boolean sortAscending = false;

    int sortedColumn;

    MonitorModel sourceModel;

    /**
     * Create a sorter model that provides sorting in front of the specified
     * model.
     * @param model Real source of data.
     */
    public TableSorter(MonitorModel model) {
        sourceModel = model;
        sourceModel.addTableModelListener(this);
        reallocateIndexes();
    }

    /**
     * This method signifies whether this model has a natural ordering.
     * @return TRUE as this model has embedded sorting.
     */
    public boolean getOrdered() {
        return true;
    }

    /**
     * Compare two rows according to their cell values
     */
    private int compare(int row1, int row2) {
        Comparable obj1 = (Comparable) sourceModel.getValueAt(row1, sortedColumn);
        Object obj2 = sourceModel.getValueAt(row2, sortedColumn);
        int result = 0;
        if (obj1 == null) {
            result = (obj2 == null ? 0 : 1);
        } else if (obj2 == null) {
            result = -1;
        } else {
            result = obj1.compareTo(obj2);
        }
        if (result != 0) {
            return sortAscending ? result : -result;
        }
        return 0;
    }

    /**
     * Reset index row mappings
     */
    private void reallocateIndexes() {
        int rowCount = sourceModel.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    /**
     * Fired when there are changed to the source table. If any rows have been
     * deleted/inserted or a change to the sorted column, then re-sort
     */
    public void tableChanged(TableModelEvent e) {
        boolean resort = false;
        TableModelEvent newEvent = null;
        int type = e.getType();
        if ((type == TableModelEvent.DELETE) || (type == TableModelEvent.INSERT)) {
            sortModel();
            newEvent = new TableModelEvent(this);
        } else if ((e.getColumn() == sortedColumn) || (e.getColumn() == TableModelEvent.ALL_COLUMNS)) {
            if (sortModel()) {
                newEvent = new TableModelEvent(this, 0, sourceModel.getRowCount(), e.getColumn());
            }
        }
        if (newEvent == null) {
            int firstRow = e.getFirstRow();
            int lastRow = e.getLastRow();
            if (e.getFirstRow() == e.getLastRow()) {
                firstRow = indexes[e.getFirstRow()];
                lastRow = indexes[e.getFirstRow()];
            } else {
                firstRow = 0;
                lastRow = sourceModel.getRowCount();
            }
            newEvent = new TableModelEvent(this, firstRow, lastRow, e.getColumn());
        }
        fireTableChanged(newEvent);
    }

    /**
     * Simple N2 sort
     */
    private void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) < 0) {
                    swap(i, j);
                }
            }
        }
    }

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

    public int getColumnCount() {
        return sourceModel.getColumnCount();
    }

    public Class getColumnClass(int columnIndex) {
        return sourceModel.getColumnClass(columnIndex);
    }

    public String getColumnName(int columnIndex) {
        return sourceModel.getColumnName(columnIndex);
    }

    public String getName() {
        return sourceModel.getName();
    }

    public int getRowCount() {
        return sourceModel.getRowCount();
    }

    /**
     * Return the cell value according to the ordered rows. This method will
     * remap the specified row according to the sorted mapping.
     * @param aRow Row offset.
     * @param aColumn Column offset.
     * @return Value of cell.
     */
    public Object getValueAt(int aRow, int aColumn) {
        return sourceModel.getValueAt(indexes[aRow], aColumn);
    }

    /**
     * Get a list of objects from the source model. The row indexes are remapped
     * according to the sorted mappings.
     *
     * @param rows Indexes of rows in the sorted model.
     * @return List of objects.
     */
    public Object getObject(int row) {
        return sourceModel.getObject(indexes[row]);
    }

    /**
     * The mapping only affects the contents of the data rows.
     * Pass all requests to these rows through the mapping array: "indexes".
     * @param aValue New value for cell.
     * @param aRow Row offset.
     * @param aColumn Column offset.
     */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        sourceModel.setValueAt(aValue, indexes[aRow], aColumn);
    }

    /**
     * The sorting model has no data so pass the update request to the
     * source model.
     *
     * @return Status of the soruce model.
     */
    public String update() {
        return sourceModel.update();
    }

    /**
     * Sort the table model by the column specified in an particular order.
     * If the specified column does not use a Comparable object then it
     * can not be sorted.
     *
     * @param column Column index of sorted column.
     * @param ascending Sort in the ascending order.
     */
    public void sortByColumn(int column, boolean ascending) {
        sortAscending = ascending;
        sortedColumn = column;
        boolean sorted = sortModel();
        if (sorted) {
            fireTableRowsUpdated(0, sourceModel.getRowCount());
        }
    }

    /**
     * This method sorts the table according to the defined settings.
     * It recreates the row mappings.
     *
     * @return Return whether a reordering has been performed
     */
    private boolean sortModel() {
        int original[] = (int[]) indexes.clone();
        reallocateIndexes();
        Class cellType = sourceModel.getColumnClass(sortedColumn);
        if (Comparable.class.isAssignableFrom(cellType)) {
            n2sort();
        }
        return !Arrays.equals(original, indexes);
    }

    private void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }
}
