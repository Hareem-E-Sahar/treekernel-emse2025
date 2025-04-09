package com.anthonyeden.lib.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.util.*;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
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
 * @version 1.5 12/17/97
 * @author Philip Milne, Anthony Eden
 */
public class TableSorter extends TableMap {

    int indexes[];

    Vector sortingColumns = new Vector();

    boolean ascending = true;

    int column = -1;

    int compares;

    /** Construct a TableSorter with no TableModel. */
    public TableSorter() {
        indexes = new int[0];
    }

    /** Construct a TableSorter with the given TableModel.
    
        @param model The TableModel
    */
    public TableSorter(TableModel model) {
        setModel(model);
    }

    /** Set the TableModel.
    
        @param model The TableModel
    */
    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /** Compare values row by row.  This method can compare Strings, Booleans,
        Dates, or Numbers.  It compares objects by calling each object's toString()
        method and then comparing the Strings.  It can also compare objects which
        implement the <code>java.lang.Comparable</code> interface.
    
        @param row1 Row 1
        @param row2 Row 2
        @param column The column to sort
        @return 1 If row 1 is greater than row 2, -1 if row 1 is less than
            row 2, or 0 if the values match
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
        } else if (type == java.util.Date.class) {
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
        } else {
            Object v1 = data.getValueAt(row1, column);
            Comparable comp = null;
            if (v1 instanceof Comparable) {
                comp = (Comparable) v1;
            } else {
                comp = v1.toString();
            }
            int result = comp.compareTo(o2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /** Compare two rows.  All sorting columns will be sorted.
    
        @param row1 Row 1
        @param row2 Row 2
        @return 1, 0, or -1
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

    /** Reallocate the array which holds sorted indexes. */
    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    /** Fire a TableModelEvent signaling that the table has changed.
    
        @param e The TableModelEvent
    */
    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        if (column >= 0) {
            sortByColumn(column, ascending);
        }
        super.tableChanged(e);
    }

    /** Check to see if the index length matches the model row count.  If
        not then the sorter was never informed of a change in the model.
    */
    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
        }
    }

    /** Sort the table data.
    
        @param sender The object which invoked the sort
    */
    public void sort(Object sender) {
        checkModel();
        compares = 0;
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
    }

    public void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    public void shuttlesort(int from[], int to[], int low, int high) {
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

    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return model.getValueAt(indexes[aRow], aColumn);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    /** Sort by the given column in ascending order.
        
        @param column The column
    */
    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    /** Sort by the given column and order.
    
        @param column The column
        @param ascending True to sort in ascending order
    */
    public void sortByColumn(int column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    /** Get the column which is being sorted.
    
        @return The sorted column
    */
    public int getColumn() {
        return column;
    }

    /** Get the real index of the given row.
    
        @param row The row index
        @return The index
    */
    public int getRealIndex(int row) {
        return indexes[row];
    }

    /** Return true if the data is sorted in ascending order.
    
        @return True if sorted in ascending order
    */
    public boolean isAscending() {
        return ascending;
    }

    /** Add a MouseListener to the given table which will cause the table
        to be sorted when the header is clicked.  The table will be sorted
        in ascending order initially.  If the table was already sorted in
        ascending order and the same column is clicked then the order will
        be reversed.
        
        @param table The JTable
    */
    public void addMouseListenerToHeaderInTable(JTable table) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    if (TableSorter.this.column == column) {
                        ascending = !ascending;
                    } else {
                        ascending = true;
                    }
                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
