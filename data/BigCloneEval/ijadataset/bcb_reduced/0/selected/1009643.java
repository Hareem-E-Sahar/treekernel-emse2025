package allensoft.gui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;

/** A TableModel that sorts the data contained in another table model. */
public class SortedTableModel extends AbstractTableModel {

    public SortedTableModel(TableModel model, Comparator comparator) {
        m_Model = model;
        m_Model.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getLastRow() != Integer.MAX_VALUE) {
                    int nColumn = e.getColumn();
                    for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                        int nSortedRow = getSortedIndex(i);
                        if (nSortedRow != -1) {
                            if (nColumn == TableModelEvent.ALL_COLUMNS) fireTableRowsUpdated(nSortedRow, nSortedRow); else fireTableCellUpdated(nSortedRow, nColumn);
                        }
                    }
                } else resort();
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == TableModelEvent.ALL_COLUMNS && e.getFirstRow() == TableModelEvent.HEADER_ROW && e.getLastRow() == TableModelEvent.HEADER_ROW) fireTableStructureChanged();
            }
        });
        m_Comparator = comparator;
        sortByColumn(0, true);
    }

    public SortedTableModel(TableModel model) {
        this(model, new DefaultComparator());
    }

    public Comparator getComparator() {
        return m_Comparator;
    }

    public void setComparator(Comparator comparator) {
        m_Comparator = comparator;
        resort();
    }

    public void sortByColumn(int nColumn, boolean bAscending) {
        m_nColumn = nColumn;
        m_bAscending = bAscending;
        resort();
    }

    public void resort() {
        m_Indexes = new int[m_Model.getRowCount()];
        for (int i = 0; i < m_Model.getRowCount(); i++) m_Indexes[i] = i;
        shuttlesort((int[]) m_Indexes.clone(), m_Indexes, 0, m_Indexes.length);
        fireTableDataChanged();
    }

    public TableModel getModel() {
        return m_Model;
    }

    public int getSortColumn() {
        return m_nColumn;
    }

    public boolean isAscending() {
        return m_bAscending;
    }

    /**
	 * Returns the number of rows in the model. A
	 * <code>JTable</code> uses this method to determine how many rows it
	 * should display.  This method should be quick, as it
	 * is called frequently during rendering.
	 *
	 * @return the number of rows in the model
	 * @see #getColumnCount
	 */
    public int getRowCount() {
        return m_Indexes.length;
    }

    /**
	 * Returns the number of columns in the model. A
	 * <code>JTable</code> uses this method to determine how many columns it
	 * should create and display by default.
	 *
	 * @return the number of columns in the model
	 * @see #getRowCount
	 */
    public int getColumnCount() {
        return m_Model.getColumnCount();
    }

    /**
	 * Returns the name of the m_nColumn at <code>columnIndex</code>.  This is used
	 * to initialize the table's m_nColumn header name.  Note: this name does
	 * not need to be unique; two columns in a table can have the same name.
	 *
	 * @param	columnIndex	the index of the m_nColumn
	 * @return  the name of the m_nColumn
	 */
    public String getColumnName(int columnIndex) {
        return m_Model.getColumnName(columnIndex);
    }

    /**
	 * Returns the most specific superclass for all the cell values
	 * in the m_nColumn.  This is used by the <code>JTable</code> to set up a
	 * default renderer and editor for the m_nColumn.
	 *
	 * @param columnIndex  the index of the m_nColumn
	 * @return the common ancestor class of the object values in the model.
	 */
    public Class getColumnClass(int columnIndex) {
        return m_Model.getColumnClass(columnIndex);
    }

    /**
	 * Returns true if the cell at <code>rowIndex</code> and
	 * <code>columnIndex</code>
	 * is editable.  Otherwise, <code>setValueAt</code> on the cell will not
	 * change the value of that cell.
	 *
	 * @param	rowIndex	the row whose value to be queried
	 * @param	columnIndex	the m_nColumn whose value to be queried
	 * @return	true if the cell is editable
	 * @see #setValueAt
	 */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return m_Model.isCellEditable(m_Indexes[rowIndex], columnIndex);
    }

    /**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>.
	 *
	 * @param	rowIndex	the row whose value is to be queried
	 * @param	columnIndex 	the m_nColumn whose value is to be queried
	 * @return	the value Object at the specified cell
	 */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return m_Model.getValueAt(m_Indexes[rowIndex], columnIndex);
    }

    /**
	 * Sets the value in the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code> to <code>aValue</code>.
	 *
	 * @param	aValue		 the new value
	 * @param	rowIndex	 the row whose value is to be changed
	 * @param	columnIndex 	 the m_nColumn whose value is to be changed
	 * @see #getValueAt
	 * @see #isCellEditable
	 */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        m_Model.setValueAt(aValue, m_Indexes[rowIndex], columnIndex);
    }

    private int compareRows(int row1, int row2) {
        Class type = m_Model.getColumnClass(m_nColumn);
        Object o1 = m_Model.getValueAt(row1, m_nColumn);
        Object o2 = m_Model.getValueAt(row2, m_nColumn);
        int nResult = m_Comparator.compare(o1, o2);
        return m_bAscending ? nResult : -nResult;
    }

    private void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4 && compareRows(from[middle - 1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compareRows(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    public void addMouseListenerToHeaderInTable(JTable table) {
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    boolean ascending = (column == m_nColumn) ? !m_bAscending : true;
                    sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    /** The comparator used when none is specified during construction. */
    public static class DefaultComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 == null && o2 == null) return 0; else if (o1 == null) return -1; else if (o2 == null) return 1;
            if (o1 instanceof Boolean) {
                boolean b1 = ((Boolean) o1).booleanValue();
                boolean b2 = ((Boolean) o2).booleanValue();
                if (b1 && !b2) return 1;
                if (!b1 && b2) return -1;
                return 0;
            } else if (o1 instanceof Comparable) return ((Comparable) o1).compareTo(o2);
            return 0;
        }
    }

    /** Gets the row index in this sorted table model that corresponds to the row index in the origonal model. */
    private int getSortedIndex(int nModelIndex) {
        for (int i = 0; i < m_Indexes.length; i++) {
            if (m_Indexes[i] == nModelIndex) return i;
        }
        return -1;
    }

    private TableModel m_Model;

    private int[] m_Indexes;

    private int m_nColumn;

    private boolean m_bAscending;

    private Comparator m_Comparator;
}
