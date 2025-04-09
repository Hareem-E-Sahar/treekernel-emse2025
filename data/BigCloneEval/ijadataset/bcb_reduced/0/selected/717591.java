package phex.gui.models;

import java.util.Comparator;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

public class FWSortedTableModel extends AbstractTableModel implements ISortableModel {

    /**
     * The model that will be sorted.
     */
    protected FWTableModel tableModel;

    /**
     * The column index to sort.
     */
    private int sortedColumn;

    /**
     * The comparator used to sort the column.
     */
    private Comparator columnComparator;

    /**
     * The sorting direction.
     */
    private boolean isAscending;

    /**
     * Holds the mapping of the visible row index to the data model index.
     */
    protected int[] sortedIndexMapping;

    /**
     * Creates a new FWSortedTableModel. By default
     * sorting for all columns is enabled.
     */
    public FWSortedTableModel(FWTableModel pipedModel) {
        tableModel = pipedModel;
        tableModel.addTableModelListener(new ModelChangeHandler());
        sortedColumn = -1;
        sortedIndexMapping = new int[0];
        reallocateIndexes();
    }

    public FWTableModel getPipedTableModel() {
        return tableModel;
    }

    public Object getValueAt(int row, int column) {
        reallocateIndexes();
        if (row >= sortedIndexMapping.length) {
            return "";
        }
        return tableModel.getValueAt(sortedIndexMapping[row], column);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        tableModel.setValueAt(aValue, sortedIndexMapping[rowIndex], columnIndex);
    }

    /**
     * Maps the index of the row in the view at viewRowIndex to the index of the
     * row in the model. Returns the index of the corresponding row in the
     * model. If viewRowIndex is less than zero, returns -1.
     * @params viewRowIndex - the index of the row in the view
     * @returns the index of the corresponding row in the model
     **/
    public int convertRowIndexToModel(int viewRowIndex) {
        if (viewRowIndex < 0 || viewRowIndex >= sortedIndexMapping.length) {
            return -1;
        }
        return sortedIndexMapping[viewRowIndex];
    }

    /**
     * Maps the index of the row in the model at modelRowIndex to the index of the
     * row in the view. Returns the index of the corresponding row in the
     * view. If modelRowIndex is less than zero, returns modelRowIndex.
     * @params modelRowIndex - the index of the row in the model
     * @returns the index of the corresponding row in the view
     **/
    public int convertRowIndexToView(int modelRowIndex) {
        reallocateIndexes();
        if (modelRowIndex < 0) {
            return modelRowIndex;
        }
        for (int i = 0; i < sortedIndexMapping.length; i++) {
            if (sortedIndexMapping[i] == modelRowIndex) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void sortByColumn(int column, boolean isSortedAscending) {
        isAscending = isSortedAscending;
        if (column != sortedColumn) {
            sortedColumn = column;
            columnComparator = tableModel.getColumnComparator(sortedColumn);
            if (columnComparator == null) {
                columnComparator = new NaturalComparator();
            }
        }
        sort();
        fireTableChanged(new TableModelEvent(this));
    }

    public synchronized int getSortByColumn() {
        return sortedColumn;
    }

    public synchronized boolean isSortedAscending() {
        return isAscending;
    }

    /**
     * Reallocated the indexes if necessary...
     */
    private synchronized void reallocateIndexes() {
        int rowCount = tableModel.getRowCount();
        int oldRowCount = sortedIndexMapping.length;
        if (rowCount == oldRowCount) {
            return;
        }
        int[] newIndices = new int[rowCount];
        if (rowCount > oldRowCount) {
            System.arraycopy(sortedIndexMapping, 0, newIndices, 0, oldRowCount);
            for (int i = oldRowCount; i < rowCount; i++) {
                newIndices[i] = i;
            }
        } else {
            for (int row = 0; row < rowCount; row++) {
                newIndices[row] = row;
            }
        }
        sortedIndexMapping = newIndices;
        oldRowCount = rowCount;
        if (sortedColumn == -1) {
            return;
        }
        mergeSort((int[]) sortedIndexMapping.clone(), sortedIndexMapping, 0, sortedIndexMapping.length);
        fireTableChanged(new TableModelEvent(this));
    }

    private synchronized void sort() {
        reallocateIndexes();
        mergeSort((int[]) sortedIndexMapping.clone(), sortedIndexMapping, 0, sortedIndexMapping.length);
    }

    private int compare(int row1, int row2) {
        Object o1 = tableModel.getComparableValueAt(row1, sortedColumn);
        Object o2 = tableModel.getComparableValueAt(row2, sortedColumn);
        int result;
        if (o1 == null && o2 == null) {
            result = 0;
        } else if (o1 == null) {
            result = -1;
        } else if (o2 == null) {
            result = 1;
        } else {
            result = columnComparator.compare(o1, o2);
        }
        if (result != 0) {
            return isAscending ? result : -result;
        }
        return 0;
    }

    private synchronized void swap(int[] x, int a, int b) {
        int tmp = x[a];
        x[a] = x[b];
        x[b] = tmp;
    }

    /**
     * Sorts the specified range of the specified array of objects according
     * to the order induced by the column comparator.  The range to be
     * sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
     * range to be sorted is empty.)  All elements in the range must be
     * <i>mutually comparable</i> by the column comparator (that is,
     * <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt>
     * for any elements <tt>e1</tt> and <tt>e2</tt> in the range).<p>
     *
     * This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.<p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is
     * omitted if the highest element in the low sublist is less than the
     * lowest element in the high sublist).  This algorithm offers guaranteed
     * n*log(n) performance, and can approach linear performance on nearly
     * sorted lists.
     */
    private synchronized void mergeSort(int[] src, int[] dest, int low, int high) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && compare(dest[j - 1], dest[j]) > 0; j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid);
        mergeSort(dest, src, mid, high);
        if (compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || (p < mid && compare(src[p], src[q]) <= 0)) {
                dest[i] = src[p++];
            } else {
                dest[i] = src[q++];
            }
        }
    }

    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    public String getColumnName(int aColumn) {
        return tableModel.getColumnName(aColumn);
    }

    public Class getColumnClass(int aColumn) {
        return tableModel.getColumnClass(aColumn);
    }

    public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(row, column);
    }

    class NaturalComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo((Comparable) o2);
        }
    }

    class ModelChangeHandler implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            fireTableChanged(e);
        }
    }
}
