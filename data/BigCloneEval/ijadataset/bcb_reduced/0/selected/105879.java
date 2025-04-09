package net.turingcomplete.phosphor.shared;

import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.*;
import javax.swing.table.*;

public class TableSorter extends TableMap {

    private int indexes[] = new int[0];

    private Vector sortingColumns = new Vector();

    private boolean ascending = true;

    private int compares;

    private int sortedColumn = -1;

    private boolean caseSensitive = false;

    private final Icon upIcon = GUIHelpers.getIcon(this, "/toolbarButtonGraphics/navigation/Up16.gif");

    private final Icon downIcon = GUIHelpers.getIcon(this, "/toolbarButtonGraphics/navigation/Down16.gif");

    public TableSorter() {
    }

    public TableSorter(AbstractTableModel model) {
        setModel(model);
    }

    public void setModel(AbstractTableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        reallocateIndexes();
    }

    public int getSortedColumn() {
        return sortedColumn;
    }

    public void sort(int column, JTable table) {
        if (column < 0 || column >= getColumnCount()) return;
        sortByColumn(column);
        Enumeration e = table.getColumnModel().getColumns();
        while (e.hasMoreElements()) {
            TableColumn tc = (TableColumn) e.nextElement();
            DefaultTableCellRenderer hr = (DefaultTableCellRenderer) tc.getHeaderRenderer();
            if (hr == null) {
                hr = new DefaultTableCellRenderer() {

                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setBorder(BorderFactory.createRaisedBevelBorder());
                        return component;
                    }
                };
                tc.setHeaderRenderer(hr);
            }
            if (tc.getModelIndex() == column) if (ascending) {
                hr.setIcon(upIcon);
            } else hr.setIcon(downIcon); else {
                hr.setIcon(null);
            }
        }
        sortedColumn = column;
        fireTableDataChanged();
    }

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
        if (type == String.class) {
            String s1 = (String) data.getValueAt(row1, column);
            String s2 = (String) data.getValueAt(row2, column);
            if (caseSensitive) {
                return s1.compareTo(s2);
            } else {
                return s1.compareToIgnoreCase(s2);
            }
        } else if (o1 instanceof Comparable) {
            return ((Comparable) o1).compareTo(o2);
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
            String s1 = data.getValueAt(row1, column).toString();
            String s2 = data.getValueAt(row2, column).toString();
            if (caseSensitive) {
                return s1.compareTo(s2);
            } else {
                return s1.compareToIgnoreCase(s2);
            }
        }
    }

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

    public void reallocateIndexes() {
        int[] newIndexes = new int[model.getRowCount()];
        int row = 0, indexesRow = 0;
        for (; indexesRow < indexes.length && row < newIndexes.length; ++indexesRow) {
            if (indexes[indexesRow] < newIndexes.length) {
                newIndexes[row++] = indexes[indexesRow];
            }
        }
        for (; row < newIndexes.length; ++row) newIndexes[row] = row;
        indexes = newIndexes;
    }

    public void checkModel() {
        Assert.that(indexes.length == model.getRowCount(), "Sorter not informed of a change in model.");
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

    public void sortByColumn(int column) {
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
    }

    /** converts the model row into the sorted row, so that you can tell the model what rows have changed */
    public int getModelRow(int row) {
        for (int i = 0; ; ++i) if (row == indexes[i]) return i;
    }

    /** converts the model row into the sorted row, so that you can tell the model what rows have changed 
	 *	lastRow is ignored as it is better not to try to correlate the interval
	 *	In order to deleted row:
	 *	- modelRow = getModelRow(row)
	 *	- removeRow
	 *	- reallocateInexes
	 *	- fireTableRowsDeleted(modelRow)
	 */
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        reallocateIndexes();
        firstRow = getModelRow(firstRow);
        super.fireTableRowsInserted(firstRow, firstRow);
    }

    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        firstRow = getModelRow(firstRow);
        super.fireTableRowsUpdated(firstRow, firstRow);
    }

    public void fireTableCellUpdated(int row, int column) {
        row = getModelRow(row);
        super.fireTableRowsUpdated(row, column);
    }

    public void addMouseListenerToHeaderInTable(JTable table) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent ev) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(ev.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (ev.getClickCount() == 1 && column != -1) {
                    int shiftPressed = ev.getModifiers() & InputEvent.SHIFT_MASK;
                    if (sortedColumn == column) ascending = !ascending; else ascending = true;
                    if (shiftPressed == 1) ascending = !ascending;
                    sorter.sort(column, tableView);
                    if (shiftPressed == 1) ascending = !ascending;
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
