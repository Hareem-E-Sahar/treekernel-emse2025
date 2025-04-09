package pcgen.gui.utils;

import pcgen.util.Logging;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

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
 * @author Bryan McRoberts <merton_monk@users.sourceforge.net>
 * @version $Revision: 1.17 $
 */
public final class TableSorter extends TableMap {

    static final long serialVersionUID = 3442230718911502935L;

    private int[] indexes = null;

    private boolean ascending = true;

    private boolean hasBeenWarned = false;

    private int sortingColumn = -1;

    /**
	 * Constructor
	 */
    public TableSorter() {
        indexes = new int[0];
    }

    /**
	 * Constructor
	 * @param model
	 */
    public TableSorter(TableModel model) {
        setModel(model);
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    /**
	 * Get the translated row
	 * @param row
	 * @return the translated row
	 */
    public int getRowTranslated(int row) {
        if ((row >= 0) && (row < indexes.length)) {
            return indexes[row];
        }
        return -1;
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        if ((aRow < 0) || (aRow >= indexes.length)) {
            Logging.errorPrint("Row " + aRow + " illegal.");
            return null;
        }
        return model.getValueAt(indexes[aRow], aColumn);
    }

    /**
	 * There is no-where else to put this.
	 * Add a mouse listener to the Table to trigger a table sort
	 * when a column heading is clicked in the JTable.
	 * @param table
	 */
    public void addMouseListenerToHeaderInTable(JTable table) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                final TableColumnModel columnModel = tableView.getColumnModel();
                final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                final int column = tableView.convertColumnIndexToModel(viewColumn);
                if ((e.getClickCount() == 1) && (column != -1)) {
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    if (shiftPressed == 0) {
                        if (column == sortingColumn) {
                            sorter.sortByColumn(column, !ascending);
                        } else {
                            sorter.sortByColumn(column, true);
                        }
                    } else {
                        sorter.sortByColumn(column, false);
                    }
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    public void tableChanged(TableModelEvent e) {
        reallocateIndexes();
        if (sortingColumn >= 0) {
            sortByColumn(sortingColumn, ascending);
        }
        super.tableChanged(e);
    }

    /**
	 * Translate the row (return its index)
	 * @param row
	 * @return index of row
	 */
    public int translateRow(int row) {
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] == row) {
                return i;
            }
        }
        return -1;
    }

    private void checkModel() {
        if ((indexes.length != model.getRowCount()) && !hasBeenWarned) {
            Logging.errorPrint("Sorter not informed of a change in model named: " + model.getClass().getName());
            Logging.errorPrint("Indexes Length = " + indexes.length + " Model Row Count = " + model.getRowCount());
            Logging.errorPrint("Fixing by reallocating the indexes.  Please report this as a bug.", new Throwable());
            reallocateIndexes();
            hasBeenWarned = true;
        }
    }

    private int compare(int row1, int row2) {
        final Class type = model.getColumnClass(sortingColumn);
        final int lessThan = (ascending ? (-1) : 1);
        final int greaterThan = (ascending ? 1 : (-1));
        final Object o1 = getValueAt(row1, sortingColumn);
        final Object o2 = getValueAt(row2, sortingColumn);
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return lessThan;
        }
        if (o2 == null) {
            return greaterThan;
        }
        if (type == Integer.class) {
            final int i1 = ((Integer) o1).intValue();
            final int i2 = ((Integer) o2).intValue();
            if (i1 < i2) {
                return lessThan;
            }
            if (i1 > i2) {
                return greaterThan;
            }
            return 0;
        } else if (type.getSuperclass() == Number.class) {
            final double d1 = ((Number) o1).doubleValue();
            final double d2 = ((Number) o2).doubleValue();
            if (d1 < d2) {
                return lessThan;
            }
            if (d1 > d2) {
                return greaterThan;
            }
            return 0;
        } else if (type == Date.class) {
            final long n1 = ((Date) o1).getTime();
            final long n2 = ((Date) o2).getTime();
            if (n1 < n2) {
                return lessThan;
            }
            if (n1 > n2) {
                return greaterThan;
            }
            return 0;
        } else if (type == String.class) {
            final String s1 = (String) o1;
            final String s2 = (String) o2;
            if (ascending) {
                return s1.compareToIgnoreCase(s2);
            }
            return s2.compareToIgnoreCase(s1);
        } else if (type == Boolean.class) {
            final boolean b1 = ((Boolean) o1).booleanValue();
            final boolean b2 = ((Boolean) o2).booleanValue();
            if (b1 == b2) {
                return 0;
            }
            if (b1) {
                return greaterThan;
            }
            return lessThan;
        } else {
            final String s1 = o1.toString();
            final String s2 = o2.toString();
            if (ascending) {
                return s1.compareToIgnoreCase(s2);
            }
            return s2.compareToIgnoreCase(s1);
        }
    }

    private void mergeSort(int[] indices, int[] workspace, final int start, final int end) {
        final int numElem = end - start;
        if (numElem > 1) {
            final int mid = (start + end) / 2;
            mergeSort(indices, workspace, start, mid);
            mergeSort(indices, workspace, mid, end);
            int i = start;
            int j = start;
            int k = mid;
            while ((j < mid) && (k < end)) {
                if (compare(j, k) <= 0) {
                    workspace[i++] = indices[j++];
                } else {
                    workspace[i++] = indices[k++];
                }
            }
            if (j < mid) {
                final int numLeft = mid - j;
                System.arraycopy(indices, j, indices, end - numLeft, numLeft);
                System.arraycopy(workspace, start, indices, start, numElem - numLeft);
            } else {
                final int numLeft = end - k;
                System.arraycopy(workspace, start, indices, start, numElem - numLeft);
            }
        }
    }

    private void reallocateIndexes() {
        final int rowCount = model.getRowCount();
        if ((indexes == null) || (rowCount != indexes.length)) {
            indexes = new int[rowCount];
            for (int row = 0; row < rowCount; ++row) {
                indexes[row] = row;
            }
        }
    }

    private void sort() {
        checkModel();
        int[] workspace = new int[indexes.length];
        mergeSort(indexes, workspace, 0, indexes.length);
    }

    private void sortByColumn(int column, boolean argAscending) {
        this.ascending = argAscending;
        sortingColumn = column;
        sort();
        super.tableChanged(new TableModelEvent(this));
    }
}
