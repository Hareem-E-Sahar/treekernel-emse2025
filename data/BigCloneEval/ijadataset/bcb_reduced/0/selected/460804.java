package gov.sns.apps.jeri.tools.swing;

import java.awt.Cursor;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import java.util.Date;
import javax.swing.table.JTableHeader;

public class TableSorter extends javax.swing.table.AbstractTableModel implements TableModelListener {

    protected TableModel model;

    int indexes[];

    int sortingColumn;

    boolean ascending = true;

    public TableSorter(TableModel model) {
        setModel(model);
    }

    /**
     * Provides a way of switching out the model for the sorter.
     * 
     * @param model
     */
    public void setModel(TableModel model) {
        if (this.model != null) this.model.removeTableModelListener(this);
        this.model = model;
        model.addTableModelListener(this);
        reallocateIndexes();
    }

    @Override
    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn);
    }

    public int getColumnCount() {
        return model.getColumnCount();
    }

    @Override
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn);
    }

    public int getRowCount() {
        return model.getRowCount();
    }

    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return model.getValueAt(indexes[aRow], aColumn);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return model.isCellEditable(indexes[row], column);
    }

    @Override
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        TableModel data = model;
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);
        if (o1 == null && o2 == null) return 0; else if (o1 == null) return -1; else if (o2 == null) return 1;
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
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);
            if (result < 0) return -1; else if (result > 0) return 1; else return 0;
        }
    }

    public int compare(int row1, int row2) {
        int result = compareRowsByColumn(row1, row2, sortingColumn);
        return ascending ? result : -result;
    }

    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent e) {
        if (indexes.length != model.getRowCount()) {
            reallocateIndexes();
        }
        fireTableChanged(e);
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) System.err.println("Sorter not informed of a change in model.");
    }

    public void sort() {
        checkModel();
        shuttlesort(indexes.clone(), indexes, 0, indexes.length);
    }

    public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) return;
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
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) to[i] = from[p++]; else to[i] = from[q++];
        }
    }

    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    public void addMouseListenerToHeaderInTable(JTable _table) {
        final TableSorter sorter = this;
        final JTable table = _table;
        table.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = table.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = table.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) try {
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    sorter.ascending = (shiftPressed == 0);
                    sorter.sortingColumn = column;
                    sorter.sort();
                } finally {
                    table.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        JTableHeader th = table.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    /**
     * Returns the index of the row in the table.
     * 
     * @param tableRow The index of the row in the table.
     * @return The index of the row in the model
     */
    public int getModelRowNumber(int tableRow) {
        return indexes[tableRow];
    }

    /**
     * Returns the index of the row in the table.
     * 
     * @param modelRow The index of the row in the model.
     * @return The index of the row in the table, or a negative number if not found.
     */
    public int getTableRowNumber(int modelRow) {
        for (int i = 0; i < indexes.length; i++) if (indexes[i] == modelRow) return i;
        return -1;
    }

    /**
     * Returns the <CODE>TableModel</CODE> that was used to create the 
     * <CODE>TableSorter</CODE>.
     * 
     * @return The <CODE>TableModel</CODE> that holds the data.
     */
    public final TableModel getModel() {
        return model;
    }
}
