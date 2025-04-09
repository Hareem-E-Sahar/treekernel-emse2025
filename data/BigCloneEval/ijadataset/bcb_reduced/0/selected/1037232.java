package com.xtech.common.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import com.xtech.common.entities.Entity;

public class XTableSorter {

    int indexes[];

    Vector sortingColumns = new Vector();

    boolean ascending;

    int compares;

    int sortColumn;

    EntitiesModel model;

    public XTableSorter() {
        indexes = new int[0];
    }

    public XTableSorter(EntitiesModel model) {
        setModel(model);
    }

    public void setModel(EntitiesModel model) {
        this.model = model;
        sortColumn = -1;
        ascending = true;
        reallocateIndexes();
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        Object o1 = model.getModelValueAt(row1, column);
        Object o2 = model.getModelValueAt(row2, column);
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        if (type.getSuperclass() == java.lang.Number.class) {
            Number n1 = (Number) model.getModelValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number) model.getModelValueAt(row2, column);
            double d2 = n2.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) model.getModelValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) model.getModelValueAt(row2, column);
            long n2 = d2.getTime();
            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == String.class) {
            String s1 = (String) model.getModelValueAt(row1, column);
            String s2 = (String) model.getModelValueAt(row2, column);
            int result = s1.compareTo(s2);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) model.getModelValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) model.getModelValueAt(row2, column);
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) {
                return 0;
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
        } else {
            Object v1 = model.getModelValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = model.getModelValueAt(row2, column);
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
            Integer column = (Integer) sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
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
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
            reallocateIndexes();
        }
    }

    public void sort(Object sender) {
        checkModel();
        compares = 0;
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
    }

    public void n2sort() {
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = i + 1; j < model.getRowCount(); j++) {
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

    /**
	 * Mapea la fila del modelo ordenado (visible) con la fila del modelo original
	 * @param modelRow
	 * @return
	 * @author jscruz
	 * @since XERP
	 */
    public int sort2Model(int sortedRow) {
        if (sortedRow >= 0) return indexes[sortedRow]; else return sortedRow;
    }

    /**
	 * Mapea una fila del modelo original con la fila del modelo ordenado (visible)
	 * @param modelRow
	 * @return
	 * @author jscruz
	 * @since XERP
	 */
    public int model2Sort(int modelRow) {
        if (modelRow < 0 && modelRow >= indexes.length) return modelRow;
        for (int i = 0; i < indexes.length; i++) {
            int inx = indexes[i];
            if (inx == modelRow) return i;
        }
        System.out.println("model2Sort: ERROR! No encontre la fila " + modelRow);
        return modelRow;
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
        Entity selected = model.getSelectedEntity();
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
        model.selectEntity(selected);
    }

    public void setTable(JTable table) {
        final XTableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    if (column == sortColumn) {
                        ascending = !ascending;
                    } else {
                        ascending = true;
                    }
                    sorter.sortByColumn(column, ascending);
                    sortColumn = column;
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        if (th == null) return;
        th.addMouseListener(listMouseListener);
    }

    public void reSort() {
        if (getSortColumn() >= 0) sortByColumn(getSortColumn(), isAscending());
    }

    /**
	 * @return
	 * @author jscruz
	 * @since XERP
	 */
    public boolean isAscending() {
        return ascending;
    }

    /**
	 * @return
	 * @author jscruz
	 * @since XERP
	 */
    public int getSortColumn() {
        return sortColumn;
    }

    /**
	 * @param b
	 * @author jscruz
	 * @since XERP
	 */
    public void setAscending(boolean b) {
        ascending = b;
    }

    /**
	 * @param i
	 * @author jscruz
	 * @since XERP
	 */
    public void setSortColumn(int i) {
        sortColumn = i;
    }
}
