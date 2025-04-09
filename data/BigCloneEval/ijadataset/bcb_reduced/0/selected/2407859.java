package org.scopemvc.view.swing;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <font color="READ">ALPHA VERSION - may change in the next release</font> <p>
 *
 * A sorter for TableModels. The sorter has a model conforming to STableModel.
 * SSortTableModel does not store or copy the data in the TableModel, instead it
 * maintains an array of integers which it keeps the same size as the number of
 * rows in its model. When the model changes it notifies the sorter that
 * something has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like getValueAt(row,
 * col) it redirects them to its model via the mapping array. That way the
 * SSortTableModel appears to hold another copy of the table with the rows in a
 * different order. The sorting algorthm used is stable which means that it does
 * not move around rows when its comparison function returns 0 to denote that
 * they are equivalent. </p> <p>
 *
 * This class is based on code from the Swing Tutorial: <a
 * href="http://java.sun.com/docs/books/tutorial/uiswing/components/example-swing/SSortTableModel.java">
 * SSortTableModel</a> </p>
 *
 * @author Philip Milne
 * @author Patrik Nordwall
 * @version $Revision: 1.4 $
 * @created 29 October 2002
 * @todo Sorting should be able to use custom Comparators (ludovicc)
 * @todo We should use the sorting functionality provided by SAbstractListModel,
 *      there are now 2 way to do sorting, and this is really bad. See
 *      ListModelAdaptor in particular for sorting.
 */
public class SSortTableModel extends STableModel implements TableModelListener {

    private static final Log LOG = LogFactory.getLog(SSortTableModel.class);

    private int indexes[];

    private int[] sortingColumns;

    private boolean ascending = true;

    private int compares;

    /**
     * Constructor for the SSortTableModel object
     *
     * @param inTable The table using this TableModel
     */
    public SSortTableModel(JTable inTable) {
        super(inTable);
        indexes = new int[0];
        addTableModelListener(this);
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex.</code>
     *
     * @param inRowIndex the row whose value is to be queried
     * @param inColumnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int inRowIndex, int inColumnIndex) {
        checkModel();
        return super.getValueAt(indexes[inRowIndex], inColumnIndex);
    }

    /**
     * Sets the value for the cell at <code>columnIndex</code> and <code>rowIndex.</code>
     *
     * @param inValue The new value of the cell
     * @param inRowIndex The row of the cell
     * @param inColumnIndex The column of the cell
     */
    public void setValueAt(Object inValue, int inRowIndex, int inColumnIndex) {
        checkModel();
        super.setValueAt(inValue, indexes[inRowIndex], inColumnIndex);
    }

    /**
     * Implementation of TableModelListener. For some simple cases we can keep
     * the sorting, otherwise reallocate indexes.
     *
     * @param inEvent The model event
     */
    public void tableChanged(TableModelEvent inEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("tableChanged: event = " + inEvent);
        }
        int rowCount = getRowCount();
        if (indexes.length != rowCount) {
            if (inEvent.getType() == TableModelEvent.INSERT && inEvent.getFirstRow() == (rowCount - 1) && inEvent.getFirstRow() == inEvent.getLastRow()) {
                int[] newIndexes = new int[rowCount];
                System.arraycopy(indexes, 0, newIndexes, 0, indexes.length);
                newIndexes[rowCount - 1] = (rowCount - 1);
                indexes = newIndexes;
            } else if (inEvent.getType() == TableModelEvent.DELETE && inEvent.getFirstRow() == inEvent.getLastRow()) {
                int[] newIndexes = new int[rowCount];
                int i = 0;
                for (int j = 0; j < indexes.length; j++) {
                    if (indexes[j] == inEvent.getFirstRow()) {
                    } else {
                        if (indexes[j] >= inEvent.getFirstRow()) {
                            newIndexes[i] = indexes[j] - 1;
                        } else {
                            newIndexes[i] = indexes[j];
                        }
                        i++;
                    }
                }
                indexes = newIndexes;
            } else {
                reallocateIndexes();
            }
        }
    }

    /**
     * Sort the model
     */
    public void sort() {
        checkModel();
        compares = 0;
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
    }

    /**
     * Converts a row index (used by the table) to the index in the list used as
     * model.
     *
     * @param inRowIndex The index of the row in the table
     * @return the index of the matching element in the (list) model
     */
    public int convertRowIndexToModel(int inRowIndex) {
        return indexes[inRowIndex];
    }

    /**
     * Converts an index in the list used as model to the row index used in the
     * table (view).
     *
     * @param inModelIndex The index of the element in the list model
     * @return The row index for the matching row in the table
     */
    public int convertModelIndexToView(int inModelIndex) {
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] == inModelIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sort the model on the given column
     *
     * @param inColumn The column used to sort the model
     * @param inAscending true if sort is ascending
     */
    public void sortByColumn(int inColumn, boolean inAscending) {
        sortByColumns(new int[] { inColumn }, inAscending);
    }

    /**
     * Sort the model on the given set of columns
     *
     * @param inColumns The set of columns used to sort the model, by order of
     *      importance
     * @param inAscending true if sort is ascending
     * @todo Multiple column sorting should also be able to use
     *      ascending/descending separately for each column
     * @todo Enable multiple sorting in the user interface (table headers)
     */
    public void sortByColumns(int[] inColumns, boolean inAscending) {
        this.ascending = inAscending;
        sortingColumns = inColumns;
        sort();
        fireTableChanged(new TableModelEvent(this));
    }

    /**
     * Compares two rows by a specific column.
     *
     * @param inRow1 The first row
     * @param inRow2 The second row
     * @param inColumn The column used for the comparison
     * @return -1 if row1 is less, 1 if row1 is greater, 0 if equal
     */
    protected int compareRowsByColumn(int inRow1, int inRow2, int inColumn) {
        Object o1 = super.getValueAt(inRow1, inColumn);
        Object o2 = super.getValueAt(inRow2, inColumn);
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            int result = ((Comparable) o1).compareTo(o2);
            return normalizeResult(result);
        } else if (getColumnClass(inColumn).getSuperclass() == java.lang.Number.class) {
            Number n1 = (Number) super.getValueAt(inRow1, inColumn);
            double d1 = n1.doubleValue();
            Number n2 = (Number) super.getValueAt(inRow2, inColumn);
            double d2 = n2.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (getColumnClass(inColumn) == Boolean.class) {
            Boolean bool1 = (Boolean) super.getValueAt(inRow1, inColumn);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) super.getValueAt(inRow2, inColumn);
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
        } else {
            Object v1 = super.getValueAt(inRow1, inColumn);
            String s1 = v1.toString();
            Object v2 = super.getValueAt(inRow2, inColumn);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);
            return normalizeResult(result);
        }
    }

    /**
     * Compares two rows by the columns set by the sortByColumns method.
     *
     * @param inRow1 The first row
     * @param inRow2 The second row
     * @return -1 if row1 is less, 1 if row1 is greater, 0 if equal
     */
    protected int compare(int inRow1, int inRow2) {
        compares++;
        for (int level = 0; level < sortingColumns.length; level++) {
            int result = compareRowsByColumn(inRow1, inRow2, sortingColumns[level]);
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    /**
     * Cancel all sorting and rebuild the indexes
     */
    protected void reallocateIndexes() {
        int rowCount = getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
        if (getTable() instanceof SSortTable) {
            SSortTable table = (SSortTable) getTable();
            table.resetHeader();
        }
    }

    /**
     * Check that the model is synchronized with the internal array of indexes
     */
    protected void checkModel() {
        if (indexes.length != getRowCount()) {
            LOG.warn("Sorter not informed of a change in model.");
        }
    }

    /**
     * N2 sort
     */
    protected void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    /**
     * This is a home-grown implementation which we have not had time to
     * research - it may perform poorly in some circumstances. It requires twice
     * the space of an in-place algorithm and makes NlogN assigments shuttling
     * the values between the two arrays. The number of compares appears to vary
     * between N-1 and NlogN depending on the initial order but the main reason
     * for using it here is that, unlike qsort, it is stable.
     *
     * @param inFrom The array of indexes to sort
     * @param inTo The array of indexes that will receive the sorted indexes
     * @param inLow The first pivot point in the sort
     * @param inHigh The second pivot point in the sort
     */
    protected void shuttlesort(int inFrom[], int inTo[], int inLow, int inHigh) {
        if (inHigh - inLow < 2) {
            return;
        }
        int middle = (inLow + inHigh) / 2;
        shuttlesort(inTo, inFrom, inLow, middle);
        shuttlesort(inTo, inFrom, middle, inHigh);
        int p = inLow;
        int q = middle;
        if (inHigh - inLow >= 4 && compare(inFrom[middle - 1], inFrom[middle]) <= 0) {
            for (int i = inLow; i < inHigh; i++) {
                inTo[i] = inFrom[i];
            }
            return;
        }
        for (int i = inLow; i < inHigh; i++) {
            if (q >= inHigh || (p < middle && compare(inFrom[p], inFrom[q]) <= 0)) {
                inTo[i] = inFrom[p++];
            } else {
                inTo[i] = inFrom[q++];
            }
        }
    }

    private int normalizeResult(int inCompareResult) {
        if (inCompareResult < 0) {
            return -1;
        } else if (inCompareResult > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private void swap(int inI, int inJ) {
        int tmp = indexes[inI];
        indexes[inI] = indexes[inJ];
        indexes[inJ] = tmp;
    }
}
