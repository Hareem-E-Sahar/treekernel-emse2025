package ch.oxinia.webdav.davcommander;

import java.util.Vector;
import java.util.Date;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * 
 */
public class TableSorter extends TableMap {

    int indexes[];

    Vector sortingColumns = new Vector();

    Vector sortingDirections = new Vector();

    Vector fixedSortingColumns = new Vector();

    int lastColumn;

    boolean lastDirection;

    int compares;

    /**
	 * Constructor
	 * 
	 * @param model
	 */
    public TableSorter(TableModel model, Vector fixedSortingColumns) {
        setModel(model);
        this.fixedSortingColumns = fixedSortingColumns;
    }

    /**
	 * 
	 * @param model
	 */
    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /**
	 * 
	 * @param row1
	 * @param row2
	 * @param column
	 * @return
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
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);
            if (result < 0) return -1; else if (result > 0) return 1; else return 0;
        }
    }

    /**
	 * 
	 * @param row1
	 * @param row2
	 * @return
	 */
    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < fixedSortingColumns.size(); level++) {
            int column = (Integer) fixedSortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column);
            if (result != 0) {
                result = compareRowsByColumn(row1, row2, column);
                return result;
            }
        }
        for (int level = 0; level < sortingColumns.size(); level++) {
            int column = (Integer) sortingColumns.elementAt(level);
            boolean ascending = (Boolean) sortingDirections.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column);
            if (result != 0) return ascending ? result : -result;
        }
        return 0;
    }

    /**
     *
     */
    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) indexes[row] = row;
    }

    /**
	 * 
	 * @param e
	 */
    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        super.tableChanged(e);
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    /**
     *
     */
    public void checkModel() {
    }

    /**
	 * 
	 * @param sender
	 */
    public void sort(Object sender) {
        checkModel();
        compares = 0;
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
    }

    /**
     * 
     */
    public void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    /**
	 * This is a home-grown implementation which we have not had time to
	 * research - it may perform poorly in some circumstances. It requires twice
	 * the space of an in-place algorithm and makes NlogN assigments shuttling
	 * the values between the two arrays. The number of compares appears to vary
	 * between N-1 and NlogN depending on the initial order but the main reason
	 * for using it here is that, unlike qsort, it is stable.
	 * 
	 * @param from
	 * @param to
	 * @param low
	 * @param high
	 */
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

    /**
	 * 
	 * @param i
	 * @param j
	 */
    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    /**
	 * The mapping only affects the contents of the data rows. Pass all requests
	 * to these rows through the mapping array: "indexes".
	 * 
	 * @param aRow
	 * 
	 * @return
	 */
    public int getTrueRow(int aRow) {
        try {
            return indexes[aRow];
        } catch (Exception e) {
            return -1;
        }
    }

    /**
	 * 
	 * @param aRow
	 * @param aColumn
	 * 
	 * @return
	 */
    public Object getValueAt(int aRow, int aColumn) {
        Object o = null;
        checkModel();
        try {
            o = model.getValueAt(indexes[aRow], aColumn);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return o;
    }

    /**
	 * 
	 * @param aValue
	 * @param aRow
	 * @param aColumn
	 */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    /**
	 * 
	 * @param column
	 */
    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    /**
	 * 
	 * @param column
	 * @param ascending
	 */
    public void sortByColumn(int column, boolean ascending) {
        lastColumn = column;
        lastDirection = ascending;
        int existingIndex = sortingColumns.indexOf(column);
        if (existingIndex != -1) {
            sortingColumns.remove(existingIndex);
            sortingDirections.remove(existingIndex);
        }
        sortingColumns.insertElementAt(column, 0);
        sortingDirections.insertElementAt(ascending, 0);
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    /**
	 * There is no-where else to put this. Add a mouse listener to the Table to
	 * trigger a table sort when a column heading is clicked in the JTable.
	 * 
	 * @param table
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
                    boolean asc = true;
                    if (column == lastColumn) {
                        asc = !lastDirection;
                    }
                    sorter.sortByColumn(column, asc);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
