package org.vizzini.ui.table;

import org.vizzini.util.AbstractEnumeratedType;
import org.vizzini.util.IEnumeratedType;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Provides a sorter for TableModels. The sorter has a model (conforming to
 * TableModel) and itself implements TableModel. TableSorter does not store or
 * copy the data in the TableModel, instead it maintains an array of integers
 * which it keeps the same size as the number of rows in its model. When the
 * model changes it notifies the sorter that something has changed eg.
 * "rowsAdded" so that its internal array of integers can be reallocated. As
 * requests are made of the sorter (like getValueAt(row, col) it redirects them
 * to its model via the mapping array. That way the TableSorter appears to hold
 * another copy of the table with the rows in a different order. The sorting
 * algorthm used is stable which means that it does not move around rows when
 * its comparison function returns 0 to denote that they are equivalent.
 */
public class TableSorter extends TableMap {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Flag indicating if using an ascending sort. */
    protected boolean _isAscending = false;

    /** Compares. */
    private int _compares;

    /** Indexes. */
    private int[] _indexes;

    /** Sorting columns. */
    private Vector<Integer> _sortingColumns = new Vector<Integer>();

    /**
     * Construct this object.
     */
    public TableSorter() {
        _indexes = new int[0];
    }

    /**
     * Construct this object with the given parameter.
     *
     * @param  model  Table model.
     */
    public TableSorter(TableModel model) {
        setModel(model);
    }

    /**
     * Add a mouse listener to the given table.
     *
     * @param  table  Table.
     */
    public void addMouseListenerToHeaderInTable(JTable table) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)) && (column != -1)) {
                    int col = getSortingColumn();
                    if (col == column) {
                        sorter.sortByColumn(column, !_isAscending);
                        JTableHeader th = tableView.getTableHeader();
                        th.repaint();
                        return;
                    }
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    boolean asc = (shiftPressed == 0);
                    sorter.sortByColumn(column, asc);
                    JTableHeader th = tableView.getTableHeader();
                    th.repaint();
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    /**
     * Check the model.
     */
    public void checkModel() {
        if (_indexes.length != _model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    /**
     * Compare two rows.
     *
     * @param   row1  First row.
     * @param   row2  Second row.
     *
     * @return  -1, 0, 1
     */
    public int compare(int row1, int row2) {
        _compares++;
        for (int level = 0; level < _sortingColumns.size(); level++) {
            Integer column = _sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return _isAscending ? result : (-result);
            }
        }
        return 0;
    }

    /**
     * Compare rows by column.
     *
     * @param   row1    First row.
     * @param   row2    Second row.
     * @param   column  Column.
     *
     * @return  -1, 0, 1
     */
    public int compareRowsByColumn(int row1, int row2, int column) {
        Class<?> type = _model.getColumnClass(column);
        TableModel data = _model;
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);
        if ((o1 == null) && (o2 == null)) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        if (type.getSuperclass() == java.lang.Number.class) {
            Number n1 = (Number) data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number) data.getValueAt(row2, column);
            double d2 = n2.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == Date.class) {
            Date d1 = (Date) data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) data.getValueAt(row2, column);
            long n2 = d2.getTime();
            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == String.class) {
            String s1 = (String) data.getValueAt(row1, column);
            String s2 = (String) data.getValueAt(row2, column);
            int result = s1.compareTo(s2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
        } else if (type.getSuperclass() == AbstractEnumeratedType.class) {
            IEnumeratedType int1 = (IEnumeratedType) data.getValueAt(row1, column);
            IEnumeratedType int2 = (IEnumeratedType) data.getValueAt(row2, column);
            int result = int1.compareTo(int2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
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
     * @param   aRow  Row.
     *
     * @return  the desorted row index.
     */
    public int getDesortedRowIndex(int aRow) {
        int answer = -1;
        if ((0 <= aRow) && (aRow < _indexes.length)) {
            for (int i = 0; i < _indexes.length; i++) {
                if (_indexes[i] == aRow) {
                    answer = i;
                    break;
                }
            }
        }
        return answer;
    }

    /**
     * @param   aRow  Row.
     *
     * @return  the sorted row index.
     */
    public int getSortedRowIndex(int aRow) {
        int answer = -1;
        if ((0 <= aRow) && (aRow < _indexes.length)) {
            answer = _indexes[aRow];
        }
        return answer;
    }

    /**
     * @see  org.vizzini.ui.table.TableMap#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return _model.getValueAt(_indexes[aRow], aColumn);
    }

    /**
     * Answer true if the given column is sorted in ascending order.
     *
     * @param   table   Table.
     * @param   column  Column index.
     *
     * @return  Boolean.TRUE if the given column is sorted in ascending order
     *          Boolean.FALSE if the given column is sorted in descending order,
     *          null if the given column is not sorted.
     *
     * @since   v0.2
     */
    public Boolean isColumnSortedAscending(JTable table, int column) {
        Boolean answer = null;
        int col = getSortingColumn();
        col = table.convertColumnIndexToView(col);
        if (col == column) {
            answer = (_isAscending ? Boolean.TRUE : Boolean.FALSE);
        }
        return answer;
    }

    /**
     * Perform an n2 sort.
     */
    public void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(_indexes[i], _indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    /**
     * Reset the index values.
     *
     * @since  v0.2
     */
    public void reallocateIndexes() {
        int rowCount = _model.getRowCount();
        int[] newIndexes = new int[rowCount];
        if (_indexes == null) {
            for (int row = 0; row < rowCount; row++) {
                newIndexes[row] = row;
            }
            _indexes = newIndexes;
        } else {
            int oldCount = _indexes.length;
            if (oldCount > rowCount) {
                for (int row = 0; row < rowCount; row++) {
                    newIndexes[row] = row;
                }
            } else {
                for (int row = 0; row < oldCount; row++) {
                    newIndexes[row] = _indexes[row];
                }
                for (int row = oldCount; row < rowCount; row++) {
                    newIndexes[row] = row;
                }
            }
            _indexes = newIndexes;
            sort(this);
        }
    }

    /**
     * @see  org.vizzini.ui.table.TableMap#setModel(javax.swing.table.TableModel)
     */
    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /**
     * @see  org.vizzini.ui.table.TableMap#setValueAt(java.lang.Object, int,
     *       int)
     */
    @Override
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        _model.setValueAt(aValue, _indexes[aRow], aColumn);
    }

    /**
     * Perform a shuttle sort.
     *
     * @param  from  From array.
     * @param  to    To array.
     * @param  low   Low index.
     * @param  high  High index.
     */
    public void shuttlesort(int[] from, int[] to, int low, int high) {
        if ((high - low) < 2) {
            return;
        }
        int middle = (low + high) / 2;
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
     * Perform a sort.
     *
     * @param  sender  Sender.
     */
    public void sort(Object sender) {
        checkModel();
        _compares = 0;
        shuttlesort(_indexes.clone(), _indexes, 0, _indexes.length);
    }

    /**
     * Sort by column.
     *
     * @param  column  Column.
     */
    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    /**
     * Sort by column.
     *
     * @param  column       Column.
     * @param  isAscending  Flag indicating if the sort is ascending.
     */
    public void sortByColumn(int column, boolean isAscending) {
        _isAscending = isAscending;
        _sortingColumns.removeAllElements();
        _sortingColumns.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    /**
     * Swap the rows at the given indexes.
     *
     * @param  i  First index.
     * @param  j  Second index.
     */
    public void swap(int i, int j) {
        int tmp = _indexes[i];
        _indexes[i] = _indexes[j];
        _indexes[j] = tmp;
    }

    /**
     * @see  org.vizzini.ui.table.TableMap#tableChanged(javax.swing.event.TableModelEvent)
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        super.tableChanged(e);
    }

    /**
     * @return  the sorting column index.
     */
    protected int getSortingColumn() {
        int answer = -1;
        if (!_sortingColumns.isEmpty()) {
            Integer columnNum = _sortingColumns.elementAt(0);
            if (columnNum != null) {
                answer = columnNum.intValue();
            }
        }
        return answer;
    }
}
