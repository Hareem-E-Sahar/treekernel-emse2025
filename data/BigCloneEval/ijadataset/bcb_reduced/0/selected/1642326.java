package jmetric.ui.filebrowser;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableSorter extends TableMap {

    int[] indexes;

    Vector sortingColumns = new Vector();

    boolean ascending = true;

    int compares;

    public TableSorter() {
        this.indexes = new int[0];
    }

    public TableSorter(TableModel model) {
        setModel(model);
    }

    public void addMouseListenerToHeaderInTable(JTable table) {
        TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if ((e.getClickCount() != 1) || (column == -1)) return;
                int shiftPressed = e.getModifiers() & 0x1;
                boolean ascending = shiftPressed == 0;
                TableSorter.this.sortByColumn(column, ascending);
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    public void checkModel() {
        this.model.getRowCount();
    }

    public int compare(int row1, int row2) {
        this.compares += 1;
        for (int level = 0; level < this.sortingColumns.size(); ++level) {
            Integer column = (Integer) this.sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) return (this.ascending) ? result : -result;
        }
        return 0;
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = this.model.getColumnClass(column);
        TableModel data = this.model;
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);
        if ((o1 == null) && (o2 == null)) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (type == File.class) {
            File f1 = (File) data.getValueAt(row1, column);
            File f2 = (File) data.getValueAt(row2, column);
            if (column == 0) {
                String name1 = f1.getName().toUpperCase();
                String name2 = f2.getName().toUpperCase();
                int result = name1.compareTo(name2);
                if ((f1.isDirectory()) && (!f2.isDirectory())) {
                    return -1;
                }
                if ((!f1.isDirectory()) && (f2.isDirectory())) {
                    return 1;
                }
                if (result < 0) return -1;
                if (result > 0) return 1;
                return 0;
            }
            if (column == 1) {
                if ((f1.isDirectory()) && (!f2.isDirectory())) {
                    return -1;
                }
                if ((!f1.isDirectory()) && (f2.isDirectory())) {
                    return 1;
                }
                long size1 = f1.length();
                long size2 = f2.length();
                if (size1 < size2) {
                    return -1;
                }
                if (size1 == size2) {
                    return 0;
                }
                return 1;
            }
            if (column == 2) {
                long size1 = f1.lastModified();
                long size2 = f2.lastModified();
                if (size1 < size2) {
                    return -1;
                }
                if (size1 == size2) {
                    return 0;
                }
                return 1;
            }
        }
        return 0;
    }

    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return this.model.getValueAt(this.indexes[aRow], aColumn);
    }

    public void n2sort() {
        for (int i = 0; i < super.getRowCount(); ++i) for (int j = i + 1; j < super.getRowCount(); ++j) if (compare(this.indexes[i], this.indexes[j]) == -1) swap(i, j);
    }

    public void reallocateIndexes() {
        int rowCount = this.model.getRowCount();
        this.indexes = new int[rowCount];
        for (int row = 0; row < rowCount; ++row) this.indexes[row] = row;
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        this.model.setValueAt(aValue, this.indexes[aRow], aColumn);
    }

    public void shuttlesort(int[] from, int[] to, int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if ((high - low >= 4) && (compare(from[(middle - 1)], from[middle]) <= 0)) {
            for (int i = low; i < high; ++i) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; ++i) if ((q >= high) || ((p < middle) && (compare(from[p], from[q]) <= 0))) {
            to[i] = from[(p++)];
        } else to[i] = from[(q++)];
    }

    public void sort(Object sender) {
        checkModel();
        this.compares = 0;
        shuttlesort((int[]) this.indexes.clone(), this.indexes, 0, this.indexes.length);
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        this.sortingColumns.removeAllElements();
        this.sortingColumns.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    public void swap(int i, int j) {
        int tmp = this.indexes[i];
        this.indexes[i] = this.indexes[j];
        this.indexes[j] = tmp;
    }

    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        super.tableChanged(e);
    }
}
