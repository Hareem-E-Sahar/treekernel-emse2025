package edu.whitman.halfway.bcomponent;

import java.io.File;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import org.apache.log4j.Logger;

public class SortingTableModel extends AbstractTableModel {

    private static Logger log = Logger.getLogger(SortingTableModel.class.getName());

    public static final int ASCENDING = 0;

    public static final int DECENDING = 1;

    protected int previousColumn = 0;

    protected TableModel baseModel;

    protected int[] indexes;

    protected boolean sortAscending = true;

    public SortingTableModel() {
        this(new DefaultTableModel());
    }

    public SortingTableModel(TableModel tm) {
        baseModel = tm;
        reallocateIndexes();
        sort(0);
    }

    public void reSort() {
        checkModel();
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length, previousColumn);
        fireTableChanged(new TableModelEvent(this));
    }

    public boolean sort(int col) {
        checkModel();
        if (col == previousColumn) {
            sortAscending = !sortAscending;
        } else {
            sortAscending = true;
            previousColumn = col;
        }
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length, col);
        fireTableChanged(new TableModelEvent(this));
        return sortAscending;
    }

    protected void checkModel() {
        if (indexes.length != baseModel.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    public void reallocateIndexes() {
        int rowCount = baseModel.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) indexes[row] = row;
    }

    public Class getColumnClass(int col) {
        return baseModel.getColumnClass(col);
    }

    public int getColumnCount() {
        return baseModel.getColumnCount();
    }

    public String getColumnName(int col) {
        return baseModel.getColumnName(col);
    }

    public int getRowCount() {
        return baseModel.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        checkModel();
        return baseModel.getValueAt(indexes[row], col);
    }

    public void setValueAt(Object obj, int row, int col) {
        checkModel();
        baseModel.setValueAt(obj, indexes[row], col);
    }

    protected int rowTransform(int row) {
        return indexes[row];
    }

    public boolean isCellEditable(int row, int column) {
        return baseModel.isCellEditable(indexes[row], column);
    }

    public void shuttlesort(int from[], int to[], int low, int high, int col) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle, col);
        shuttlesort(to, from, middle, high, col);
        int p = low;
        int q = middle;
        if (high - low >= 4 && sortUpDown(from[middle - 1], from[middle], col) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && sortUpDown(from[p], from[q], col) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    protected int sortUpDown(int row1, int row2, int col) {
        int result = compareRows(row1, row2, col);
        return (sortAscending) ? result : -result;
    }

    protected int compareRows(int row1, int row2, int column) {
        Class type = getColumnClass(column);
        Object o1 = baseModel.getValueAt(row1, column);
        Object o2 = baseModel.getValueAt(row2, column);
        if (o1 == null && o2 == null) return 0; else if (o1 == null) return 1; else if (o2 == null) return -1;
        if (type.getSuperclass() == java.lang.Number.class) {
            Number n1 = (Number) o1;
            double d1 = n1.doubleValue();
            Number n2 = (Number) o2;
            double d2 = n2.doubleValue();
            if (d1 == d2) return 0;
            return (d1 < d2) ? -1 : 1;
        } else if (type == java.util.Date.class) {
            long n1 = ((Date) o1).getTime();
            long n2 = ((Date) o2).getTime();
            if (n1 == n1) return 0;
            return (n1 < n2) ? -1 : 1;
        } else if (type == String.class) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int result = s1.compareTo(s2);
            if (result == 0) return 0;
            return (result < 0) ? -1 : 1;
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) o1;
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) o2;
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) return 0;
            return (b1) ? 1 : -1;
        } else if (type == File.class) {
            File f1 = (File) o1;
            File f2 = (File) o2;
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            String s1 = f1.getName();
            String s2 = f2.getName();
            int result = s1.toLowerCase().compareTo(s2.toLowerCase());
            if (result == 0) return 0;
            return (result < 0) ? -1 : 1;
        } else {
            String s1 = o1.toString();
            String s2 = o2.toString();
            int result = s1.compareTo(s2);
            if (result == 0) return 0;
            return (result < 0) ? -1 : 1;
        }
    }
}
