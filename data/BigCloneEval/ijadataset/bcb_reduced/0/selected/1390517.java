package com.vscorp.ui.swing.table.objectSource;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import com.vscorp.ui.swing.table.TableMap;

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
 * @author Philip Milne
 */
public class ObjectSourceTableSorter extends TableMap {

    int[] indexes;

    Vector sortingColumns = new Vector();

    boolean ascending = true;

    int compares;

    private java.util.Vector sortVector = new java.util.Vector();

    public ObjectSourceTableSorter() {
        indexes = new int[0];
    }

    public ObjectSourceTableSorter(TableModel model) {
        setModel(model);
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    public int compareRowsByColumn(int row1, int row2, ObjectSourceSortableTableColumn column) {
        TableModel data = model;
        Object o1 = (Object) column.getColumnObject(data.getValueAt(row1, 0));
        Object o2 = (Object) column.getColumnObject(data.getValueAt(row2, 0));
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        if (o2 instanceof java.lang.Number) {
            Number n1 = (Number) o1;
            double d1 = n1.doubleValue();
            Number n2 = (Number) o2;
            double d2 = n2.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (o2 instanceof java.util.Date) {
            Date d1 = (Date) o1;
            long n1 = d1.getTime();
            Date d2 = (Date) o2;
            long n2 = d2.getTime();
            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (o2 instanceof String) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int result = s1.compareTo(s2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (o2 instanceof Boolean) {
            Boolean bool1 = (Boolean) o1;
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) o2;
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
        } else {
            Object v1 = o1;
            String s1 = v1.toString();
            Object v2 = o2;
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

    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            ObjectSourceSortableTableColumn column = (ObjectSourceSortableTableColumn) sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column);
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        super.tableChanged(e);
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

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

    public void sortByColumn(ObjectSourceSortableTableColumn column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(ObjectSourceSortableTableColumn column, boolean ascending) {
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(column);
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    public void setSortIconInHeaders(TableColumnModel columnModel, TableColumn column, boolean ascending) {
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn currentColumn = columnModel.getColumn(i);
            TableCellRenderer renderer = new JTableHeader().getDefaultRenderer();
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(null, currentColumn.getHeaderValue(), false, false, -1, -1);
            if (currentColumn == column) {
                if (ascending) {
                    label.setIcon(null);
                } else {
                    label.setIcon(null);
                }
            } else {
                label.setIcon(null);
            }
            label.setHorizontalTextPosition(SwingConstants.LEADING);
            currentColumn.setHeaderRenderer(renderer);
        }
    }

    public void clearSort(TableColumnModel columnModel) {
        sortVector.removeAllElements();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn currentColumn = columnModel.getColumn(i);
            TableCellRenderer renderer = new JTableHeader().getDefaultRenderer();
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(null, currentColumn.getHeaderValue(), false, false, -1, -1);
            label.setIcon(null);
            currentColumn.setHeaderRenderer(renderer);
        }
    }

    public void doSort(TableColumnModel model, TableColumn column) {
        if (!(column instanceof ObjectSourceSortableTableColumn)) {
            return;
        }
        boolean ascending;
        if (sortVector.contains(column)) {
            ascending = false;
            sortVector.remove(column);
        } else {
            ascending = true;
            sortVector.removeAllElements();
            sortVector.add(column);
        }
        sortByColumn((ObjectSourceSortableTableColumn) column, ascending);
        setSortIconInHeaders(model, column, ascending);
    }

    public void addMouseListenerToHeaderInTable(JTable table) {
        final ObjectSourceTableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                TableColumn column = columnModel.getColumn(viewColumn);
                if (e.getClickCount() == 1 && column != null) {
                    sorter.doSort(columnModel, column);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
