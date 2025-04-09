package org.furthurnet.furi;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.furthurnet.client.DownloadFileSetTableModel;
import org.furthurnet.servergui.FurthurFileSetTableModel;
import org.furthurnet.xmlparser.QueryResultTableModel;

public class TableSorter extends TableMap implements SortableTableModel {

    int indexes[];

    int oldRowCount = 0;

    Vector sortingColumns = new Vector();

    int compares;

    int oldSortByColumn = -1;

    boolean firstSort = true;

    boolean currentAscending = true;

    boolean oldAscending = true;

    int currentCol = -1;

    int oldCol = -1;

    private int lastSortCol = -1;

    public TableSorter() {
        indexes = new int[0];
    }

    public TableSorter(TableModel model) {
        indexes = new int[0];
        setModel(model);
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        TableModel data = model;
        if (data instanceof QueryResultTableModel) {
            throw new IllegalArgumentException("Table sorter no longer supports sorting tables of this type.  Use furi.SortableTableModel instead.");
        }
        try {
            return ((FurthurFileSetTableModel) data).compareRowsByColumn(row1, row2, column);
        } catch (Exception e) {
        }
        try {
            return ((DownloadFileSetTableModel) data).compareRowsByColumn(row1, row2, column);
        } catch (Exception e) {
        }
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
            if (d1 < d2) return -1; else if (d1 > d2) return 1; else return 0;
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) data.getValueAt(row2, column);
            long n2 = d2.getTime();
            if (n1 < n2) return -1; else if (n1 > n2) return 1; else return 0;
        } else if (type == String.class) {
            String s1 = (String) data.getValueAt(row1, column);
            String s2 = (String) data.getValueAt(row2, column);
            int result = s1.compareTo(s2);
            if (result < 0) return -1; else if (result > 0) return 1; else return 0;
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) return 0; else if (b1) return 1; else return -1;
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString().toUpperCase();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString().toUpperCase();
            double d1 = 0.0;
            double d2 = 0.0;
            double d3 = 0.0;
            double d4 = 0.0;
            int result;
            boolean parseAsString = true;
            try {
                d1 = Double.parseDouble(s1.substring(0, s1.length() - 2));
                d2 = Double.parseDouble(s2.substring(0, s2.length() - 2));
                parseAsString = false;
                d3 = Double.parseDouble(s1);
                d4 = Double.parseDouble(s2);
                d1 = d3;
                d2 = d4;
            } catch (NumberFormatException nfe) {
            }
            if (parseAsString == true) {
                result = s1.compareTo(s2);
                if (result < 0) return -1; else if (result > 0) return 1; else return 0;
            } else {
                if (d1 < d2) return -1; else if (d1 > d2) return 1; else return 0;
            }
        }
    }

    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            Integer column = (Integer) sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) return currentAscending ? result : -result;
        }
        return 0;
    }

    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
        if (rowCount == oldRowCount) return;
        if (rowCount > oldRowCount) {
            int[] newIndexes = new int[rowCount];
            for (int i = 0; i < oldRowCount; i++) {
                newIndexes[i] = indexes[i];
            }
            for (int i = oldRowCount; i < rowCount; i++) {
                newIndexes[i] = i;
            }
            indexes = newIndexes;
            oldRowCount = rowCount;
            return;
        }
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) indexes[row] = row;
        oldRowCount = rowCount;
        if (oldSortByColumn != -1) {
            sortByColumn(oldSortByColumn, oldAscending);
        }
    }

    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        super.tableChanged(e);
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            reallocateIndexes();
        }
    }

    public void resort() {
        try {
            sortByColumn(currentCol, currentAscending);
        } catch (Exception e) {
        }
    }

    public void sort(Object sender) {
        checkModel();
        compares = 0;
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
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
        if (aRow >= indexes.length) return "";
        return model.getValueAt(indexes[aRow], aColumn);
    }

    public int getActualIndex(int row) {
        checkModel();
        return indexes[row];
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortOnColumn(int col, boolean ascending) {
        sortByColumn(col, true);
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
        oldCol = currentCol;
        currentCol = column;
        oldAscending = currentAscending;
        currentAscending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
        if (firstSort || currentAscending != oldAscending || (oldCol != -1 && oldCol != currentCol)) {
            super.tableChanged(new TableModelEvent(this));
            firstSort = false;
        }
    }

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
                    boolean ascending = !isReverseSort(column);
                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    private boolean isReverseSort(int col) {
        if (col == lastSortCol) {
            lastSortCol = -1;
            return true;
        } else {
            lastSortCol = col;
            return false;
        }
    }
}
