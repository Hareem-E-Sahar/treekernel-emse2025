package org.dinopolis.timmon.frontend.treetable;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.JTableHeader;

/** 
 * This class implements a table sorter.
 */
public class TableSorter extends TableMap {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /** the sorted indizes */
    private int indexes_[];

    /** the sorting columns */
    private Vector sorting_columns_ = new Vector();

    /** true if sorted in ascending order */
    private boolean ascending_ = true;

    /** the currently sorted column, or -1 of no column is sorted */
    private int sorted_column_ = -1;

    /** the numner of compares */
    private int compares_;

    /** the table */
    private JTable table_;

    /**
   * The Default Constructor
   */
    public TableSorter() {
        indexes_ = new int[0];
    }

    /**
   * Constructor taking a TableModel <code>model</code> as its
   * argument. 
   *
   * @param model the underlaying table model.
   */
    public TableSorter(TableModel model) {
        setModel(model);
    }

    /**
   * Sets the underlaying model to the given value
   * <code>model</code>. 
   *
   * @param the underlaying model to set.
   */
    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /**
   * Compares two rows using the value at column <code>column</code>. 
   *
   * @param row1 the first row to compare.
   * @param row2 the second row to compare.
   * @param row1 the column to compare.
   * @return the compared result.
   */
    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model_.getColumnClass(column);
        TableModel data = model_;
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);
        if (o1 == null && o2 == null) {
            return (0);
        } else if (o1 == null) {
            return (-1);
        } else if (o2 == null) {
            return 1;
        }
        if (type.getSuperclass() == java.lang.Number.class) {
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
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) o1;
            long n1 = d1.getTime();
            Date d2 = (Date) o2;
            long n2 = d2.getTime();
            if (n1 < n2) {
                return (-1);
            } else if (n1 > n2) {
                return (1);
            } else {
                return (0);
            }
        } else if (type == String.class) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int result = s1.compareTo(s2);
            if (result < 0) {
                return (-1);
            } else if (result > 0) {
                return (1);
            } else {
                return (0);
            }
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) o1;
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) o2;
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) {
                return (0);
            } else if (b1) {
                return (1);
            } else {
                return (-1);
            }
        } else {
            String s1 = o1.toString();
            String s2 = o2.toString();
            int result = s1.compareTo(s2);
            if (result < 0) {
                return (-1);
            } else if (result > 0) {
                return (1);
            } else {
                return (0);
            }
        }
    }

    /**
   * Compares two columns.
   *
   * @param row1 the first row to compare.
   * @param row2 the second row to compare.
   * @return the compared result.
   */
    public int compare(int row1, int row2) {
        compares_++;
        for (int level = 0; level < sorting_columns_.size(); level++) {
            Integer column = (Integer) sorting_columns_.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return (ascending_ ? result : -result);
            }
        }
        return (0);
    }

    /**
   * Reallocates all sorted indexes.
   */
    private synchronized void reallocateIndexes() {
        int row_count = model_.getRowCount();
        indexes_ = new int[row_count];
        sorted_column_ = -1;
        for (int row = 0; row < row_count; row++) indexes_[row] = row;
    }

    /**
   * This fine grain notification tells listeners the exact range
   * of cells, rows, or columns that changed.
   *
   * @param event the table model event
   */
    public void tableChanged(TableModelEvent event) {
        if (sorted_column_ < 0) {
            super.tableChanged(event);
            if (table_ != null) table_.getTableHeader().repaint();
            return;
        }
        int first = event.getFirstRow();
        int last = event.getLastRow();
        if (event.getType() != TableModelEvent.UPDATE) {
            reallocateIndexes();
        } else {
            synchronized (this) {
                if (indexes_.length != model_.getRowCount()) {
                    int[] old = indexes_;
                    indexes_ = new int[model_.getRowCount()];
                    System.arraycopy(old, 0, indexes_, 0, Math.min(old.length, indexes_.length));
                    for (int count = old.length; count < indexes_.length; count++) indexes_[count] = count;
                    sortByColumn(sorted_column_, ascending_);
                }
                if (first < indexes_.length) first = indexes_[first];
                if (last < indexes_.length) last = indexes_[last];
            }
        }
        super.tableChanged(new TableModelEvent(this, first, last, event.getColumn(), event.getType()));
        if (table_ != null) table_.getTableHeader().repaint();
    }

    /**
   * Sorts the corresponding column.
   *
   * @param sender the sender of this event.
   */
    private void sort(Object sender) {
        compares_ = 0;
        shuttlesort((int[]) indexes_.clone(), indexes_, 0, indexes_.length);
    }

    /**
   * The central sorting methid.
   *
   * @param from the array holding the unsorted positions.
   * @param to the array holding the partly sorted positions.
   * @param low the lower position.
   * @param high the higher position.
   */
    private void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) return;
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) to[i] = from[i];
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) to[i] = from[p++]; else to[i] = from[q++];
        }
    }

    /**
   * Returns the value for the cell at <code>column</code> and
   * <code>row</code>.
   * This method is forwarded to the underlaying model.
   *
   * @param	row the row whose value is to be queried.
   * @param	column the column whose value is to be queried.
   * @return the value Object at the specified cell.
   */
    public synchronized Object getValueAt(int row, int column) {
        if (sorted_column_ >= 0) return (model_.getValueAt(indexes_[row], column));
        return (model_.getValueAt(row, column));
    }

    /**
   * Sets the value in the cell at <code>column</code> and
   * <code>row</code> to <code>value</code>.
   * This method is forwarded to the underlaying model.
   *
   * @param	value the new value.
   * @param	row the row whose value is to be changed.
   * @param	column the column whose value is to be changed.
   * @see #getValueAt
   * @see #isCellEditable
   */
    public synchronized void setValueAt(Object value, int row, int column) {
        if (sorted_column_ >= 0) {
            model_.setValueAt(value, indexes_[row], column);
            return;
        }
        model_.setValueAt(value, row, column);
    }

    /**
   * Sorts using the specified column
   *
   * @param column the column that is to be sorted.
   * @param ascending true if sorting should be done in ascending
   * order, false otherwise.
   */
    private synchronized void sortByColumn(int column, boolean ascending) {
        if (indexes_.length != model_.getRowCount()) reallocateIndexes();
        ascending_ = ascending;
        sorted_column_ = column;
        sorting_columns_.removeAllElements();
        sorting_columns_.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    /**
   * Adds a new created mouse listener to the given tables header to
   * initiate sort events on mouse clicks.
   *
   * @param table the needed table.
   */
    public void addMouseListenerToHeaderInTable(JTable table) {
        table_ = table;
        table.getTableHeader().setDefaultRenderer(new TableSorterCellRenderer(table.getTableHeader().getDefaultRenderer()));
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    boolean ascending = (shiftPressed == 0);
                    if ((ascending) && (sorted_column_ == column)) ascending = !ascending_;
                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    /**
   * The Renderer that is used to render the table header cells.
   */
    class TableSorterCellRenderer implements TableCellRenderer {

        private TableCellRenderer original_renderer_;

        /**
     * Constructor taking the original renderer as argument.
     *
     * @param original_renderer the original renderer, may be null.
     */
        TableSorterCellRenderer(TableCellRenderer original_renderer) {
            original_renderer_ = original_renderer;
        }

        /**
     * Returns the table cell renderer component.
     * 
     * @param table the table
     * @param value the value
     * @param is_selected true, if the object is currently selected. 
     * @param has_focus true, if the object currently has the focus.
     * @param row the current row.
     * @param column the current column. 
     * @return the rendered component.
     */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean is_selected, boolean has_focus, int row, int column) {
            Component original = original_renderer_.getTableCellRendererComponent(table, value, is_selected, has_focus, row, column);
            if (original instanceof JLabel) {
                JLabel label = (JLabel) original;
                label.setIcon(null);
                if (sorted_column_ == table.convertColumnIndexToModel(column)) {
                    if (ascending_) label.setIcon(TreeTableConstants.UP_ICON); else label.setIcon(TreeTableConstants.DOWN_ICON);
                }
            }
            return (original);
        }
    }
}
