package org.arif.components.table;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.util.Vector;
import javax.swing.ImageIcon;
import org.arif.components.table.event.*;

public class JTableProModel extends AbstractTableModel {

    public static final int GRP_EXPANDED = 0;

    public static final int GRP_COLLAPSED = 1;

    public static final int I_POSINVIEW = 0;

    public static final int I_STATE = 1;

    public static final int I_CHILDCOUNT = 2;

    public static final int I_POSINMODEL = 3;

    private JTableProDataSource source;

    private Class[] columnClasses;

    private String[] columnNames;

    protected CellAttribute cellAtt;

    private int[][] groupHeaders;

    private int sortedCol;

    private boolean isDesc = true;

    private int visibleRowCount;

    private int[] indexes;

    private int[] visible2Index;

    private boolean headersCalculated = false;

    public JTableProModel(JTableProDataSource source) {
        this.source = source;
        source.addDataEventListener(new _DataSourceListener(this));
        cellAtt = new DefaultCellAttribute(source.getRowCount(), source.getColumnCount());
        retrieveColumnNames();
        reallocateIndexes();
        calculateHeaders();
        getVisibleRowCount();
        cellAtt.setSize(new Dimension(columnNames.length, visibleRowCount));
        setSpans(false);
    }

    public void setCellAttribute(CellAttribute newCellAtt) {
        int numColumns = getColumnCount();
        int numRows = getRowCount();
        if ((newCellAtt.getSize().width != numColumns) || (newCellAtt.getSize().height != numRows)) {
            newCellAtt.setSize(new Dimension(numRows, numColumns));
        }
        cellAtt = newCellAtt;
    }

    public ImageIcon getColumnHeaderImage(int col) {
        return source.getColumnHeaderImage(col);
    }

    public boolean isGroupCellExpanded(int row) {
        for (int i = 0; i < groupHeaders[this.I_CHILDCOUNT].length; i++) if (groupHeaders[this.I_POSINMODEL][i] == row) return groupHeaders[this.I_STATE][i] == this.GRP_EXPANDED;
        System.out.println("****************** MAJOR ERROR!!!! ********************");
        return false;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void setAllGroupsState(boolean expand) {
        for (int i = 0; i < groupHeaders[this.I_CHILDCOUNT].length; i++) {
            groupHeaders[this.I_STATE][i] = expand ? this.GRP_EXPANDED : this.GRP_COLLAPSED;
        }
        setSpans(true);
        realignHeaderPositions();
        setSpans(false);
        getVisibleRowCount();
        this.fireTableDataChanged();
    }

    public Object getValueAt(int row, int col) {
        int index = visible2Index[row];
        if (index == -1) return "GroupRow";
        return source.getDataAt(indexes[index], col);
    }

    public Class getColumnClass(int col) {
        return columnClasses[col];
    }

    public int getRowCount() {
        return visibleRowCount;
    }

    public int getColumnCount() {
        return source.getColumnCount();
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public CellAttribute getCellAttribute() {
        return cellAtt;
    }

    public void toggleGroupCell(int row) {
        int index = getIndexInTable(row);
        groupHeaders[this.I_STATE][index] = (groupHeaders[this.I_STATE][index] - 1) * -1;
        setSpans(true);
        realignHeaderPositions();
        setSpans(false);
        getVisibleRowCount();
        this.fireTableDataChanged();
    }

    public void sortModified(int col, boolean isSortDesc) {
        this.sortedCol = col;
        isDesc = isSortDesc;
        sort(this);
        setSpans(true);
        calculateHeaders();
        getVisibleRowCount();
        cellAtt.setSize(new Dimension(columnNames.length, visibleRowCount));
        setSpans(false);
        this.fireTableDataChanged();
    }

    public String getTextOrIconForGroup(int row) {
        int index = getIndexInTable(row);
        String text = "";
        if (columnClasses[sortedCol] == ImageIcon.class) {
            JTableProCellRenderer.icon = (ImageIcon) source.getDataAt(indexes[groupHeaders[this.I_POSINVIEW][index]], sortedCol);
        } else {
            JTableProCellRenderer.icon = null;
            text = source.getDataAt(indexes[groupHeaders[this.I_POSINVIEW][index]], sortedCol).toString();
        }
        JTableProCellEditor.icon = JTableProCellRenderer.icon;
        text += " (" + groupHeaders[this.I_CHILDCOUNT][index] + " items)";
        return text;
    }

    public boolean isGroupCell(int row, int col) {
        if (!headersCalculated) calculateHeaders();
        if (getIndexInTable(row) != -1) return true;
        return false;
    }

    public void sort(Object sender) {
        checkModel();
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
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
        if (high - low >= 4 && source.compare(from[middle - 1], from[middle], sortedCol, isDesc) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && source.compare(from[p], from[q], sortedCol, isDesc) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    private void checkModel() {
        if (indexes.length != source.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    private void reallocateIndexes() {
        int rowCount = source.getRowCount();
        indexes = new int[rowCount];
        for (int row = 0; row < rowCount; row++) indexes[row] = row;
    }

    private void buildV2I() {
        visible2Index = new int[visibleRowCount];
        for (int i = 0; i < visibleRowCount; i++) {
            if (!isGroupCell(i, 0)) {
                int hRowIndex = -1;
                for (int j = 0; j < groupHeaders[this.I_CHILDCOUNT].length; j++) {
                    if (groupHeaders[this.I_POSINMODEL][j] <= i) if (hRowIndex < groupHeaders[this.I_POSINMODEL][j] || hRowIndex == -1) hRowIndex = j;
                }
                if (hRowIndex != -1) {
                    visible2Index[i] = groupHeaders[this.I_POSINVIEW][hRowIndex] + (i - groupHeaders[this.I_POSINMODEL][hRowIndex] - 1);
                }
            } else visible2Index[i] = -1;
        }
    }

    private int getIndexInTable(int posInModel) {
        for (int i = 0; i < groupHeaders[this.I_CHILDCOUNT].length; i++) if (groupHeaders[this.I_POSINMODEL][i] == posInModel) return i;
        return -1;
    }

    private void getVisibleRowCount() {
        visibleRowCount = groupHeaders[this.I_CHILDCOUNT].length;
        for (int i = 0; i < groupHeaders[this.I_CHILDCOUNT].length; i++) visibleRowCount += (groupHeaders[this.I_STATE][i] == this.GRP_EXPANDED) ? groupHeaders[this.I_CHILDCOUNT][i] : 0;
        buildV2I();
    }

    private void setSpans(boolean clearSpans) {
        int cols[] = new int[this.getColumnCount()];
        for (int i = 0; i < source.getColumnCount(); i++) cols[i] = i;
        for (int i = 0; i < groupHeaders[this.I_CHILDCOUNT].length; i++) {
            if (clearSpans) ((CellSpan) this.getCellAttribute()).split(groupHeaders[this.I_POSINMODEL][i], 0); else {
                ((CellSpan) this.getCellAttribute()).combine(new int[] { groupHeaders[this.I_POSINMODEL][i] }, cols);
            }
        }
    }

    private void calculateHeaders() {
        int childrenCount;
        int grpPosInData;
        Vector[] headers = new Vector[3];
        headers[0] = new Vector();
        headers[1] = new Vector();
        headers[2] = new Vector();
        grpPosInData = 0;
        childrenCount = 1;
        for (int i = 1; i < source.getRowCount(); i++) {
            if (source.compare(indexes[i - 1], indexes[i], sortedCol, isDesc) != 0) {
                headers[0].add(new Integer(grpPosInData));
                headers[1].add(new Integer(this.GRP_EXPANDED));
                headers[2].add(new Integer(childrenCount));
                grpPosInData = i;
                childrenCount = 0;
            }
            childrenCount++;
        }
        headers[0].add(new Integer(grpPosInData));
        headers[1].add(new Integer(this.GRP_EXPANDED));
        headers[2].add(new Integer(childrenCount));
        groupHeaders = new int[4][headers[0].size()];
        groupHeaders[I_POSINVIEW][0] = ((Integer) headers[0].get(0)).intValue();
        groupHeaders[I_STATE][0] = ((Integer) headers[1].get(0)).intValue();
        groupHeaders[I_CHILDCOUNT][0] = ((Integer) headers[2].get(0)).intValue();
        groupHeaders[I_POSINMODEL][0] = ((Integer) headers[0].get(0)).intValue();
        for (int i = 1; i < headers[0].size(); i++) {
            groupHeaders[I_POSINVIEW][i] = ((Integer) headers[0].get(i)).intValue();
            groupHeaders[I_STATE][i] = ((Integer) headers[1].get(i)).intValue();
            groupHeaders[I_CHILDCOUNT][i] = ((Integer) headers[2].get(i)).intValue();
            groupHeaders[I_POSINMODEL][i] = groupHeaders[I_POSINMODEL][i - 1] + groupHeaders[I_CHILDCOUNT][i - 1] + 1;
        }
        headersCalculated = true;
    }

    private void realignHeaderPositions() {
        int offset;
        for (int i = 1; i < groupHeaders[this.I_CHILDCOUNT].length; i++) {
            offset = (groupHeaders[this.I_STATE][i - 1] == this.GRP_EXPANDED) ? (groupHeaders[this.I_CHILDCOUNT][i - 1] + 1) : 1;
            groupHeaders[this.I_POSINMODEL][i] = groupHeaders[this.I_POSINMODEL][i - 1] + offset;
        }
    }

    protected void retrieveColumnNames() {
        this.columnNames = source.getColumnNames();
        this.columnClasses = source.getColumnClasses();
    }

    private void printHeaderTable() {
        System.out.println("\n------------- HEADER TABLE ----------");
        System.out.println("CC \t PosInModel \t PosInView \t State");
        for (int i = 0; i < groupHeaders[this.I_CHILDCOUNT].length; i++) System.out.println(groupHeaders[this.I_CHILDCOUNT][i] + "\t" + groupHeaders[this.I_POSINMODEL][i] + "\t" + groupHeaders[this.I_POSINVIEW][i] + "\t" + groupHeaders[this.I_STATE][i]);
        System.out.println("------------- HEADER TABLE ----------");
    }

    private class _DataSourceListener implements DataSourceListener {

        JTableProModel model;

        public _DataSourceListener(JTableProModel model) {
            this.model = model;
        }

        public void dataSourceChanged(DataSourceChangedEvent evt) {
            System.out.println("Data changed");
            model.setSpans(false);
            model.cellAtt = new DefaultCellAttribute(source.getRowCount(), source.getColumnCount());
            model.retrieveColumnNames();
            model.reallocateIndexes();
            model.calculateHeaders();
            model.getVisibleRowCount();
            model.cellAtt.setSize(new Dimension(columnNames.length, visibleRowCount));
            model.setSpans(false);
            model.sortModified(0, true);
            model.fireTableDataChanged();
        }
    }
}
