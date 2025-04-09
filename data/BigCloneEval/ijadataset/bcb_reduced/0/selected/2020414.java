package mipt.crec.lab.common.modules.gui.matrix;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.Array;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import mipt.aaf.edit.form.AbstractFieldEditor;
import mipt.aaf.edit.form.FieldVetoException;
import mipt.aaf.edit.swing.form.SwingFieldEditor;
import mipt.crec.lab.gui.Resources;
import mipt.data.Data;
import mipt.data.DataList;
import mipt.data.DataSet;
import mipt.data.MutableComparableData;
import mipt.data.MutableComparableDataSet;
import mipt.data.MutableData;
import mipt.data.impl.ComparableDataList;
import mipt.gui.controls.ButtonAction;
import mipt.math.arg.func.formula.FormulaDependenceAnalyzer;
import mipt.math.array.BandedMatrix;
import mipt.math.ui.GraphicFormulaCellRenderer;

/**
 * �������� ������, ������, �������. �������� ��������� � ���, ��� � ��������
 * ������ ������ ��������� Data-������. ��������� ����������� Data-������ ����� ����
 * �����, �� ��������� ������� ������ ���������� ���������� {@link TableModel} (� ������ ������
 * �������� {@link AbstractMatrixTableModel}). �������� ��������� ��� ��������� ������
 * �� ����������� ������������ �������. � ��������� ���� ����� ��� ������ � ��������, ���
 * � �������� ������ ����� ������� �������� ��������� (�� ���� � ���������� ��������). �� ���������
 * ������������ ������ ���� �������, ��� ������������� ����� �������� ������ � �������� ���������� �������.
 * ����� �������� ����� ���� ������ �������, � ����� ����������� �������������, �� ���������
 * ��� ������� ��������� ��� "y1", "y2", "y3" � �.�.
 * 
 * ��������� ����������� ������ �� ���������� GUI � ����������� �� ����� ���������� � ������� � � ��������� �������. ��������, ���
 * ��������� ������� ��������� ���������� ����� ����������� ��������� �������.
 *
 * @author Anton Korchak
 */
public class MatrixFieldEditor extends SwingFieldEditor {

    /**
	 * {@link Action}, ���������� �� ��� �������� ��������� � ���������� �������, ��������,
	 * ��������. ��������� ��� �������� ����������� � ������������. � ������ ������� ���� {@link Action}
	 * ������������ �� ��������� �������� �������.
	 * � ������� ������� �������� ��� ��������������� ���������.
	 *
	 * @author Anton Korchak
	 */
    protected class MatrixAction extends ButtonAction {

        /**
		 * �� ����������� �� ����� �������������� �������.
		 */
        public static final int NONE = 0;

        /**
		 * ����������� ������� �������� � ����������� ���������.
		 */
        public static final int ADD = 1;

        /**
		 * ����������� ������� �������� � ��������� ���������.
		 */
        public static final int REMOVE = 2;

        /**
		 * ������ �� �������� �������. �� ����� ���� ��� ������ ������
		 * �� ������ ������. ������ {@link MatrixAction} �������� ��� �������
		 * ��������� � ����� ��� ���������� {@link MatrixFieldEditor}.
		 */
        protected MatrixFieldEditor fieldEditor = null;

        /**
		 * �������� ����� {@link Action}.
		 */
        protected int currentMode = MatrixFieldEditor.SOLID;

        /**
		 * �������������� ����� {@link Action}.
		 */
        protected int action = NONE;

        /**
		 * ������ �� ��������������� {@link MatrixAction}. ����� ���� ������������� ��� ���������
		 * ���������� ������� {@link Action}.
		 */
        protected Action matrixAction = null;

        /**
		 * ����������� ������.
		 * @param text - ��� ��������, ������������� ����� {@link ResourceBundle} �
		 * ����������� ���.
		 * @param mnemonicIndex - �����������.
		 * @param icon - ������ ��������.
		 * @param fieldEditor - ������ �� ���� �������� ������� � �����. ����� ��� ������ ����������
		 * ������ �� ���� ����� ���������. � �������� ����� ���������� null.
		 * @param currentMode - �������� �����, ��������������� ��� ��������. ��������, <code>MatrixFieldEditor.BOTH</code>.
		 * @param action - �������������� �����, ��������������� ��� ��������. ��������, <code>MatrixAction.ADD</code>.
		 * @param matrixAction - ������ �� �������������� ������� {@link Action}. ����� ���� null.
		 */
        protected MatrixAction(String text, int mnemonicIndex, Icon icon, MatrixFieldEditor fieldEditor, int currentMode, int action, Action matrixAction) {
            super(Resources.getString(text, bundle), mnemonicIndex, icon);
            this.fieldEditor = fieldEditor;
            this.currentMode = currentMode;
            this.action = action;
            this.matrixAction = matrixAction;
        }

        /**
		 * ����������� ������.
		 * @param text - ��� ��������, ������������� ����� {@link ResourceBundle} �
		 * ����������� ���.
		 * @param mnemonicIndex - �����������.
		 * @param fieldEditor - ������ �� ���� �������� ������� � �����. ����� ��� ������ ����������
		 * ������ �� ���� ����� ���������. � �������� ����� ���������� null.
		 * @param currentMode - �������� �����, ��������������� ��� ��������. ��������, <code>MatrixFieldEditor.BOTH</code>.
		 * @param action - �������������� �����, ��������������� ��� ��������. ��������, <code>MatrixAction.ADD</code>.
		 * @param matrixAction - ������ �� �������������� ������� {@link Action}. ����� ���� null.
		 */
        protected MatrixAction(String text, int mnemonicIndex, MatrixFieldEditor fieldEditor, int currentMode, int action, Action matrixAction) {
            super(Resources.getString(text, bundle), mnemonicIndex);
            this.fieldEditor = fieldEditor;
            this.currentMode = currentMode;
            this.action = action;
            this.matrixAction = matrixAction;
        }

        /**
		 * ����������� ������.
		 * @param text - ��� ��������, ������������� ����� {@link ResourceBundle} �
		 * ����������� ���.
		 * @param fieldEditor - ������ �� ���� �������� ������� � �����. ����� ��� ������ ����������
		 * ������ �� ���� ����� ���������. � �������� ����� ���������� null.
		 * @param currentMode - �������� �����, ��������������� ��� ��������. ��������, <code>MatrixFieldEditor.BOTH</code>.
		 * @param action - �������������� �����, ��������������� ��� ��������. ��������, <code>MatrixAction.ADD</code>.
		 * @param matrixAction - ������ �� �������������� ������� {@link Action}. ����� ���� null.
		 */
        public MatrixAction(String text, MatrixFieldEditor fieldEditor, int currentMode, int action, Action matrixAction) {
            super(Resources.getString(text, bundle));
            this.fieldEditor = fieldEditor;
            this.currentMode = currentMode;
            this.action = action;
            this.matrixAction = matrixAction;
        }

        /**
		 * @see mipt.gui.controls.ButtonAction#createCopy()
		 */
        protected ButtonAction createCopy() {
            return new MatrixAction(getName(), getMnemonic(), getIcon(), fieldEditor, currentMode, action, matrixAction);
        }

        /**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
        public void actionPerformed(ActionEvent e) {
            switch(currentMode) {
                case MatrixFieldEditor.COLUMN:
                    if (action == ADD) addColumn();
                    if (action == REMOVE) removeColumn();
                    break;
                case MatrixFieldEditor.ROW:
                    if (action == ADD) addRow();
                    if (action == REMOVE) removeRow();
                    break;
                case MatrixFieldEditor.BOTH:
                    if (action == ADD) {
                        addRow();
                        addColumn();
                    }
                    if (action == REMOVE) {
                        removeRow();
                        removeColumn();
                    }
                    break;
            }
            if (shouldNotyfy()) fireFieldChanging();
        }

        /**
		 * ���������� ������� � �������. ����� ���������� ���������� ��� ������ �
		 * ������ ������� ��� � ���������� ���������� ������� �� View.
		 */
        protected void addColumn() {
            int selectedRow = getSelectedRow();
            int selectedColumn = getSelectedColumn();
            int index = selectedColumn;
            getTableModel().addColumn(index);
            selectedColumn = selectedColumn >= getTable().getColumnCount() ? getTable().getColumnCount() - 1 : selectedColumn;
            selectedRow = selectedRow >= getTable().getRowCount() ? getTable().getRowCount() - 1 : selectedRow;
            getTable().changeSelection(selectedRow, selectedRow, false, false);
            if (getTableModel().getColumnCount() == 2) matrixAction.setEnabled(true);
        }

        /**
		 * ���������� ������ � �������. ����� ���������� ���������� ��� ������ �
		 * ������ ������� ��� � ���������� ���������� ������ �� View.
		 */
        protected void addRow() {
            int selectedRow = getSelectedRow();
            int selectedColumn = getSelectedColumn();
            int index = selectedRow;
            getTableModel().addRow(index);
            selectedColumn = selectedColumn >= getTable().getColumnCount() ? getTable().getColumnCount() - 1 : selectedColumn;
            selectedRow = selectedRow >= getTable().getRowCount() ? getTable().getRowCount() - 1 : selectedRow;
            getTable().changeSelection(selectedRow, selectedRow, false, false);
            if (getTableModel().getRowCount() == 2) matrixAction.setEnabled(true);
        }

        /**
		 * �������� ������� �� �������. ����� ���������� �������� ��� ������ ��
		 * ������ ������� ��� � ���������� �������� ������� �� View.
		 * ����������� ���������� �������� ����� 1.
		 */
        protected void removeColumn() {
            int selectedRow = getSelectedRow();
            int selectedColumn = getSelectedColumn();
            int index = selectedColumn;
            if (index == getTableModel().getColumnCount()) index--;
            if (index == 0 && getTableModel().getColumnCount() == 1) return;
            getTableModel().removeColumn(index);
            selectedColumn = selectedColumn >= getTable().getColumnCount() ? getTable().getColumnCount() - 1 : selectedColumn;
            getTable().changeSelection(selectedRow, selectedColumn, false, false);
            if (getTableModel().getColumnCount() == 1) setEnabled(false);
        }

        /**
		 * �������� ������ �� �������. ����� ���������� �������� ��� ������ ��
		 * ������ ������� ��� � ���������� �������� ������ �� View.
		 * ����������� ���������� ����� ����� 1.
		 */
        protected void removeRow() {
            int selectedRow = getSelectedRow();
            int selectedColumn = getSelectedColumn();
            int index = selectedRow;
            if (index == getTableModel().getRowCount()) index--;
            if (index == 0 && getTableModel().getRowCount() == 1) return;
            getTableModel().removeRow(index);
            selectedRow = selectedRow >= getTable().getRowCount() ? getTable().getRowCount() - 1 : selectedRow;
            getTable().changeSelection(selectedRow, selectedColumn, false, false);
            if (getTableModel().getRowCount() == 1) setEnabled(false);
        }

        /**
		 * ��������� ������� ����������� �������. ���� ������� �� ��� �������,
		 * �� ������������ ������ ���������� �������.
		 * @return ������ ����������� �������, ��� ���� ��� ����������� �������,
		 * �� ������ ���������� �������.
		 */
        protected int getSelectedColumn() {
            int index = getTable().getSelectedColumn();
            if (index == -1) index = getTableModel().getColumnCount();
            return index;
        }

        /**
		 * ��������� ������� (����������) ����������� �������. ���� ������� �� ��� �������,
		 * �� ������������ ������ ���������� �������.
		 * @return ������ ����������� �������, ��� ���� ��� ����������� �������,
		 * �� ������ ���������� �������.
		 */
        protected int getSelectedRow() {
            int index = getTable().getSelectedRow();
            if (index == -1) index = getTableModel().getRowCount();
            return index;
        }
    }

    /**
	 * ������ ������ �������. �������� ����������� �������� ����������� ������
	 * (� ������ ������� �������������� �� ������ �� ������� Data-����������).
	 * �������� �������� ����������� ������ ������ � �������. ��� ���������
	 * ���������� ���� ������� ������ ���� �������� � ����������.
	 * ��������� ������������ ���� ������ � ����������� (������ ��).
	 * ���� ������ �������������� �� Data, ����� ��� ������ ��������.
	 * 
	 * @author Anton Korchak
	 */
    public abstract class AbstractMatrixTableModel<D, E> extends AbstractTableModel {

        /**
		 * ������ ������.
		 */
        private D matrixData = null;

        /**
		 * Whether to notify about all changes in matrix to controller.
		 * Default value is true.
		 */
        private boolean isNotify = true;

        /**
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        /**
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
        public String getColumnName(int column) {
            if (!isDisplayHeaders()) return null;
            if (columnNameBase == EMPTY) return " ";
            return Resources.getString(columnNameBase, bundle) + (column + 1);
        }

        /**
		 * ���������� "������" ������ � ������.
		 * ����� ���������� ���������� ������ �������� ����� ������������� ��
		 * ��������� ���� ��������������. ��� ��������� ��������� ����� ������
		 * ��������� � ��������� ����� (addDataRow).
		 * @param index - ������, ��� ������� ������� ���� ��������� ������.
		 * @see #newData(Object)
		 * @see #addDataRow(int)
		 */
        public void addRow(int index) {
            addDataRow(index);
            if (getRowCount() == 2 && matrixAction != null) matrixAction.setEnabled(true);
            if (shouldNotyfy()) fireTableRowsInserted(index, index);
        }

        /**
		 * ���������� ��������� ���������� "������" � ������ ������.
		 * @param index - �����, ��� ������� ����������� ������.
		 */
        protected abstract void addDataRow(int index);

        /**
		 * ���������� "�������" ������ � ������.
		 * ����� ���������� ���������� ������ �������� ����� ������������� ��
		 * ��������� ���� ��������������. ��� ��������� ��������� ����� ������
		 * ��������� � ��������� ����� (addDataColumn).
		 * ����� ����������� ���������� ������ ���� ��������.
		 * @param index - ������, ��� ������� ������ ���� �������� �������.
		 * @see #newData(Object)
		 * @see #addDataColumn(int)
		 */
        public void addColumn(int index) {
            addDataColumn(index);
            if (getColumnCount() == 2 && matrixAction != null) matrixAction.setEnabled(true);
            if (shouldNotyfy()) fireTableStructureChanged();
        }

        /**
		 * ���������� ��������� ���������� "�������" � ������ ������.
		 * @param index - �����, ��� ������� ����������� �������.
		 */
        protected abstract void addDataColumn(int index);

        /**
		 * �������� "�������" ������ �� ������.
		 * ����� ���������� �������� ������ �������� ����� ������������� ��
		 * ��������� ���� ��������������.
		 * ��� ��������� ��������� ����� ������ ��������� � ��������� �����
		 * (removeDataColumn).
		 * ����� ����������� ��������� ������ ���� ��������.
		 * @param index - ������ �������, ������� ����� ������.
		 */
        public void removeColumn(int index) {
            removeDataColumn(index);
            if (getTableModel().getColumnCount() == 1 && matrixAction != null) matrixAction.setEnabled(false);
            if (shouldNotyfy()) fireTableStructureChanged();
        }

        /**
		 * ���������� ��������� �������� "�������" �� ������ ������.
		 * @param index - ������ �������, ������� ����� ������.
		 */
        protected abstract void removeDataColumn(int index);

        /**
		 * �������� "������" ������ �� ������.
		 * ����� ���������� �������� ������ �������� ����� ������������� ��
		 * ��������� ���� ��������������.
		 * ��� ��������� ��������� ����� ������ ��������� � ��������� �����
		 * (removeDataRow).
		 * @param index - ������ ������, ������� ����� �������.
		 */
        public void removeRow(int index) {
            removeDataRow(index);
            if (getTableModel().getRowCount() == 1 && matrixAction != null) matrixAction.setEnabled(false);
            if (shouldNotyfy()) fireTableRowsDeleted(index, index);
        }

        /**
		 * @see javax.swing.table.AbstractTableModel#fireTableRowsInserted(int, int)
		 */
        public void fireTableRowsInserted(int firstRow, int lastRow) {
            super.fireTableRowsInserted(firstRow, lastRow);
            for (int i = firstRow - 1; i <= lastRow + 1; i++) updateTableView(i);
        }

        /**
		 * @see javax.swing.table.AbstractTableModel#fireTableStructureChanged()
		 */
        public void fireTableStructureChanged() {
            super.fireTableStructureChanged();
            updateTableView(-1);
        }

        /**
		 * The same as fireTableStructureChanged() but without GUI update.
		 * @see #fireTableStructureChanged()
		 */
        public void fireTableStructureOnlyChanged() {
            super.fireTableStructureChanged();
        }

        /**
		 * ��������� ����� �������������� �������� {@link GraphicFormulaCellRenderer},
		 * �� ��� ����� ���������� ������� ����� ������������� ������� ��� �������, �
		 * ������ ������ �����.
		 * @param rowIndex - ����� ������, ������ ������� ���������� ��������, ���� < 0,
		 * �� ����������� ��� ������ �������.
		 */
        public void updateTableView(int rowIndex) {
            if (rowIndex >= getRowCount()) return;
            if (rowIndex < 0) {
                if (getCellRenderer() instanceof GraphicFormulaCellRenderer) ((GraphicFormulaCellRenderer) getCellRenderer()).updateTableRowHeights(getTable(), true);
            } else {
                if (getCellRenderer() instanceof GraphicFormulaCellRenderer) {
                    ((GraphicFormulaCellRenderer) getCellRenderer()).updateTableRowHeight(rowIndex, getTable(), true);
                }
            }
        }

        /**
		 * @see javax.swing.table.AbstractTableModel#fireTableRowsDeleted(int, int)
		 */
        public void fireTableRowsDeleted(int firstRow, int lastRow) {
            super.fireTableRowsDeleted(firstRow, lastRow);
            updateTableView(firstRow - 1);
            updateTableView(lastRow + 1);
        }

        /**
		 * Convertion of table model in accordance to type. This method may be usefull int case of
		 * new model data setting.
		 * @param eventType - type of convertion. MatrixEvent contsnts are used.
		 * 
		 * @see MatrixEvent#CONVERTION_INTO_DIAGONAL
		 * @see MatrixEvent#CONVERTION_INTO_FILLED
		 */
        public void fireTableConvert(int eventType) {
        }

        /**
		 * ���������� ��������� �������� "������" �� ������ ������.
		 * @param index - ������ ������, ������� ����� �������.
		 */
        protected abstract void removeDataRow(int index);

        /**
		 * �������� ������������� ������, �������������� �������� �������.
		 * ���� ������� ������ �������������� ���������� �������������, �.�.
		 * �������� ����� ����������� ��� �������������� ���� ������.
		 * �� ��� ��������� �������� ��������� ������. ������ ������ ���������
		 * �������� �� ���������.
		 * @param value - ����������� ��������.
		 * @return ����� ��������, �������������� ������ ������.
		 * @see #defaultValue(int,int)
		 */
        protected abstract E newData(Object value);

        /**
		 * �������� �� ���������, ������� ������ ��������� "�����������" ����� ������� �������.
		 * ���������� �� defaultValue(int, int) - �������������� ������ ������� ���.
		 */
        public Object defaultValue() {
            return "0.0";
        }

        /**
		 * �������� �� ���������, ������� ������ ��������� ����� ������� �������.
		 * ���� ������� �������� � ������ <code>BOTH</code>, �� �� ��������� ����� ��������� �������.
		 * ������� �������������� ��� ���������� �������.
		 * @param column - ����� ������� ������ ��������.
		 * @param row - ����� ������ ������ ��������.
		 * @return �� ���������: 1, ���� ������������ �������, 0 - �����.
		 * @see #defaultValue()
		 * @see MatrixFieldEditor#BOTH
		 */
        public Object defaultValue(int column, int row) {
            if (column < 0 || row < 0) return defaultValue();
            if (column == row && getSizeMode() == BOTH) return "1.0";
            return defaultValue();
        }

        /**
		 * ������, � �� ��� ��������, ������ ������� ������.
		 * ������� �������� ��������� ������������� ������� ������ ������.
		 * ������� ������ ������.
		 * @param matrixData - ������ ������.
		 */
        public void setData(D matrixData) {
            this.matrixData = matrixData;
            if (shouldNotyfy()) fireTableStructureChanged();
        }

        /**
		 * ��������� �������� ��������� ��� ������ ������ �������.
		 * @return ������ ������ �������.
		 * @see #setData(Object)
		 */
        public final D getData() {
            return matrixData;
        }

        /**
		 * Setting state of GUI and controller notification.
		 * See comment to class.
		 * @param isNotify - true, to notify, false - else. Default value is true.
		 */
        public void setAllChangesNotification(boolean isNotify) {
            this.isNotify = isNotify;
        }

        /**
		 * Whether to notify both controller and GUI about all changes in matrix. 
		 * @return true - if notify, else - false.
		 */
        public final boolean shouldNotyfy() {
            return isNotify;
        }
    }

    /**
	 * ������, ������������ ��� ������ � ������� ���� DataSet.
	 * ������������������� ������ �������� �����, �.�. ���������� ���
	 * �������� � ������ ����������. ����� ���������� ������ ���, ������� ���
	 * ��� ������. 
	 * 
	 * @author Anton Korchak
	 */
    public class MatrixTableModel extends AbstractMatrixTableModel<DataSet, Data> {

        /**
		 * ��� �������� ���� � Data.
		 */
        public static final String MAIN_FIELD = "constructor";

        /**
		 * ��� �������� ��������� ������, ���� �������, ���� ������.
		 */
        protected boolean isMatrix = true;

        /**
		 * �������� �� ������� ��� ������ ������������������. �� ���������
		 * ��� ������� ������������� �����������.
		 */
        private boolean isTransposed = false;

        /**
		 * ���� �������� ������ ��� false, �� ��������������� ������ View,
		 * ���� true, �� Data ��������������� �����.
		 */
        private boolean isImplicit = true;

        /**
		 * Whether to convert model from one representation into another one.
		 * Default value is true.
		 */
        private boolean shouldConvert = true;

        /**
		 * This variable may used from different sides. In this class this variable is
		 * minimal number of diagonals that may be in filled representation.
		 */
        private int diagonalsAllowed = BandedMatrix.FIVE_DIAGONAL_MATRIX;

        /**
		 * ������� �����������.
		 */
        public MatrixTableModel() {
        }

        /**
		 * ����������� ������. 
		 * @param matrixData - ������ ������.
		 */
        public MatrixTableModel(DataSet matrixData) {
            setData(matrixData);
        }

        /**
		 * ����� ����� ���������� ��������� �� ������ ������� ��� ������.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#setData(java.lang.Object)
		 */
        public void setData(DataSet matrixData) {
            determineType(matrixData);
            super.setData(matrixData);
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#fireTableConvert(int)
		 */
        public void fireTableConvert(int eventType) {
            if (eventType == MatrixEvent.CONVERTION_INTO_DIAGONAL) if (convertIntoBandedMatrix()) fireFieldChanging(new MatrixEvent(MatrixFieldEditor.this, MatrixEvent.CONVERTION_INTO_DIAGONAL));
        }

        /**
		 * ������� ���������������� ������.
		 * @param isTransposed - true, ���� ���������������, false - �����.
		 */
        public final void setTransposition(boolean isTransposed) {
            setTransposition(isTransposed, true);
        }

        /**
		 * ������� ���������������� ������.
		 * @param isTransposed - true, ���� ���������������, false - �����.
		 * @param isImplicit - true, ���� ��������������� ����� ������ ������������,
		 * � ������� ������ ����� ��������������� ��� �������������������.
		 */
        public final void setTransposition(boolean isTransposed, boolean isImplicit) {
            this.isTransposed = isTransposed;
            this.isImplicit = isImplicit;
        }

        /**
		 * �������� �� ������ ������������������.
		 * @return true, ���� ����������������, false - ���.
		 */
        public final boolean isTransposed() {
            return isTransposed;
        }

        /**
		 * ��������������� �� Data. ����� ����� ������ �� �����
		 * ������, ���� <code>isTransposed() == false</code>.
		 * @return true - ���� � View � Data ���������������, false - �����.
		 * @see #isTransposed()
		 */
        public final boolean isImplisitTransposed() {
            return isImplicit;
        }

        /**
		 * Utility method
		 */
        protected final Data getDataFromSet(DataSet datas, int index) {
            return (datas instanceof DataList) ? ((DataList) datas).getData(index) : datas.toArray()[index];
        }

        protected final void addDataToSet(DataSet datas, Data data, int index) {
            if (datas instanceof DataList) {
                ((DataList) datas).add(index, data);
            } else {
                Data[] dataArray = datas.toArray();
                int length = dataArray.length;
                Data[] newArray = new Data[length + 1];
                System.arraycopy(dataArray, 0, newArray, 0, index);
                System.arraycopy(dataArray, index, newArray, index + 1, length - index);
                newArray[index] = data;
                datas.clear();
                datas.add(newArray);
            }
        }

        protected final Data removeDataFromSet(DataSet datas, int index) {
            if (datas instanceof DataList) {
                return ((DataList) datas).removeData(index);
            } else {
                Data data = getDataFromSet(datas, index);
                datas.remove(data);
                return data;
            }
        }

        /**
		 * ���������� ��������� �������� ���������, ��� ���� ������, ���� �������.
		 * � ����������� �� ���� ����� �������������� �� ��� ���� ������ ���������.
		 * @param data - ���������, ������� ����� �����������.
		 */
        protected void determineType(DataSet datas) {
            Object element = getDataFromSet(datas, 0).get(MAIN_FIELD);
            isMatrix = element instanceof DataSet;
        }

        /**
		 * ��������� ������ �� "�������" ������ � �������� index.
		 * @param columnIndex - ������ "�������������" ������� ������.
		 * @return ������ �� "�������" ������ � �������� index
		 */
        protected final DataSet getColumn(int columnIndex) {
            if (isMatrix) return getDataFromSet(getData(), columnIndex).getDataSet(MAIN_FIELD);
            if (!isTransposed) return getData();
            DataSet newSet = (DataSet) ((MutableComparableDataSet) getData()).copy();
            newSet.clear();
            newSet.add(((DataList) getData()).getData(columnIndex));
            return newSet;
        }

        /**
		 * ��������� �������� ������, ����������� ������������ columnIndex � rowIndex.
		 * @param columnIndex - ������ "�������".
		 * @param rowIndex - ������ "������".
		 * @return ������� ������.
		 */
        protected Data getElement(int columnIndex, int rowIndex) {
            return getDataFromSet(getColumn(columnIndex), rowIndex);
        }

        /**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
        public int getColumnCount() {
            if (getData() == null) return 0;
            if (isMatrix) return getData().size();
            if (isTransposed) return getData().size();
            return 1;
        }

        /**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
        public int getRowCount() {
            try {
                if (isMatrix) return getColumn(0).size();
                if (!isTransposed) return getData().size();
                return 1;
            } catch (NullPointerException e) {
                return 0;
            }
        }

        /**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
        public Object getValueAt(int rowIndex, int columnIndex) {
            return getElement(columnIndex, rowIndex).get(MAIN_FIELD);
        }

        /**
		 * The way to check or determive double value of expression.
		 * @param formula - some mathematical expression.
		 * @return double value of expression or null if formula couldn't be calculated.
		 */
        protected Double getDouble(String formula) {
            try {
                return FormulaDependenceAnalyzer.getDependence(formula).getValue().doubleValue();
            } catch (Exception e) {
                return null;
            }
        }

        /**
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ((MutableData) getElement(columnIndex, rowIndex)).set(aValue, MAIN_FIELD);
            Double value = getDouble(aValue.toString());
            if (value == null) return;
            if (value == 0. && isMatrix) if (shouldConvert()) {
                if (convertIntoBandedMatrix()) fireFieldChanging(new MatrixEvent(MatrixFieldEditor.this, MatrixEvent.CONVERTION_INTO_DIAGONAL));
            }
            if (shouldNotyfy()) {
                updateTableView(rowIndex);
                if (isNotifyListenerInUpdate()) fireFieldChanging();
            }
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#addDataRow(int)
		 */
        protected void addDataRow(int index) {
            for (int i = 0; i < getColumnCount(); i++) {
                addDataToSet(getColumn(i), newData(defaultValue(i, index)), index);
            }
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#addDataColumn(int)
		 */
        protected void addDataColumn(int index) {
            int rowCount = getRowCount();
            Data[] newColumn = new Data[rowCount];
            for (int i = 0; i < rowCount; i++) newColumn[i] = newData(defaultValue(index, i));
            ComparableDataList dataColumn = new ComparableDataList(newColumn);
            addDataToSet(getData(), newData(dataColumn), index);
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#newData(java.lang.Object)
		 */
        protected Data newData(Object value) {
            if (value instanceof Double || value instanceof String) {
                MutableData standard = ((MutableComparableData) getDataFromSet(getColumn(0), 0)).copy();
                standard.set(value, MAIN_FIELD);
                return (Data) standard;
            }
            if (value instanceof DataSet) {
                DataList list = (DataList) value;
                Data columnData = ((MutableComparableData) ((DataList) getData()).getData(0)).copy();
                DataList columnDataSet = (DataList) columnData.getDataSet(MAIN_FIELD);
                int dif = columnDataSet.size() - list.size();
                if (dif > 0) for (int i = 0; i < dif; i++) columnDataSet.removeData(0);
                if (dif < 0) for (int i = 0; i < dif; i++) columnDataSet.add(((MutableComparableData) columnDataSet.getData(0)).copy());
                for (int i = 0; i < list.size(); i++) columnDataSet.set(i, list.getData(i));
                return columnData;
            }
            return null;
        }

        /**
		 * ��� ������������� ������� ���� ����� ������� �� ������ ���������� (�������������).
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#removeDataColumn(int)
		 */
        protected void removeDataColumn(int index) {
            if (isMatrix) {
                removeDataFromSet(getData(), index);
            } else {
                if (!isTransposed) return;
                removeDataFromSet(getData(), index);
            }
        }

        /**
		 * ��� ��������������� ������� ���� ����� ������� �� ������ ���������� (�������������).
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#removeDataRow(int)
		 */
        protected void removeDataRow(int index) {
            if (isMatrix) {
                for (int i = 0; i < getColumnCount(); i++) {
                    DataSet dataColumn = getDataFromSet(getData(), i).getDataSet(MAIN_FIELD);
                    removeDataFromSet(dataColumn, index);
                }
            } else {
                if (isTransposed) return;
                removeDataFromSet(getData(), index);
            }
        }

        /**
		 * Converting of matrix in standard representation into diagonal matrix.
		 * Very specific method, it is work for BandedMatrixTableModel.
		 * Before convertion one makes decision whether to convert table model or not.
		 * Unfortunately it is a very very slow method.
		 * @return true - if convertion was complete successful, false - else.
		 */
        private boolean convertIntoBandedMatrix() {
            int size = getRowCount();
            String[][] matrix = new String[size][size];
            for (int i = 0; i < size; i++) for (int j = 0; j < size; j++) matrix[i][j] = getValueAt(i, j).toString();
            matrix = BandedMatrix.getDiagonals(matrix, "0.0");
            int diagonalCount = matrix.length;
            if (diagonalCount > getDiagonalsAllowed()) return false;
            Data mainDiagonal = ((DataList) getData()).getData(0);
            getData().clear();
            getData().add(mainDiagonal);
            boolean flag = shouldNotyfy();
            setAllChangesNotification(false);
            setMatrixIsBanded(true);
            MatrixTableModel tableModel = (MatrixTableModel) createModel();
            tableModel.setAllChangesNotification(false);
            tableModel.setData(getData());
            tableModel.setDiagonalsAllowed(getDiagonalsAllowed());
            getTable().setModel(tableModel);
            int additionaDiagonalNumber = (2 * size - 1 - diagonalCount) / 2;
            for (int i = 0; i < diagonalCount; i++) {
                String[] diagonal = matrix[i];
                for (int j = 0; j < diagonal.length; j++) {
                    String value = diagonal[j];
                    tableModel.setValueAt(value, BandedMatrix.getRow(i + additionaDiagonalNumber, j, size), BandedMatrix.getColumn(i + additionaDiagonalNumber, j, size));
                    additionaDiagonalNumber = (2 * size - 1 - tableModel.getData().size()) / 2;
                }
            }
            setAllChangesNotification(flag);
            tableModel.setAllChangesNotification(flag);
            getTable().setModel(tableModel);
            fireTableStructureChanged();
            return true;
        }

        /**
		 * Whether to convert model from one representation into another one.
		 * @return true - if convertion is permitted, false - else. Default value is true.
		 */
        public final boolean shouldConvert() {
            return shouldConvert;
        }

        /**
		 * Permition of matrix some representation convertion or not.
		 * @param shouldConvert - true - to permite convertion, false - else.
		 */
        public final void setShouldConvert(boolean shouldConvert) {
            this.shouldConvert = shouldConvert;
        }

        /**
		 * Number of allowed diagonals in matrix in term of matrix convertion.
		 * This value may used from different sides. In {@link MatrixTableModel} class this value is a
		 * minimal number of diagonals that may be in filled representation, in {@link BandedMatrixTableModel}
		 * class this value is a maximum number of diagonals that may be in diagonal representation.
		 * @return number of allowed diagonals in matrix in different menings. Default value is FIVE_DIAGONAL_MATRIX.
		 * 
		 * @see BandedMatrix#FIVE_DIAGONAL_MATRIX
		 */
        public final int getDiagonalsAllowed() {
            return diagonalsAllowed;
        }

        /**
		 * Setting allowed diagonals number.
		 * @param diagonalsAllowed - number of allowed diagonals.
		 * @see #getDiagonalsAllowed()
		 */
        public final void setDiagonalsAllowed(int diagonalsAllowed) {
            this.diagonalsAllowed = diagonalsAllowed;
        }
    }

    /**
	 * Table model that is special for diagonal (banded) matrix.
	 * 
	 * @author Korchak Anton
	 */
    public class BandedMatrixTableModel extends MatrixTableModel {

        /**
		 * Out of diagonals element value.
		 */
        private Data zero = null;

        /**
		 * Is it possible to modify zero-elements that are out of diagonals?
		 * Default value is true.
		 */
        private boolean outOfDiagonalsMutable = true;

        /**
		 * ������� ����������� ������.
		 */
        public BandedMatrixTableModel() {
            setDiagonalsAllowed(1);
        }

        /**
		 * ����������� ������.
		 * @param matrixData - ������ ������.
		 */
        public BandedMatrixTableModel(DataSet matrixData) {
            super(matrixData);
            setDiagonalsAllowed(1);
        }

        /**
		 * Overridden in coordinate system.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#getElement(int, int)
		 */
        protected Data getElement(int columnIndex, int rowIndex) {
            int diag = getDiagonalCount() / 2;
            if (Math.abs(columnIndex - rowIndex) > diag) return getZero();
            return super.getElement(BandedMatrix.getDiagonalNumber(rowIndex, columnIndex, 2 * diag), BandedMatrix.getPositionAtDiagonal(rowIndex, columnIndex));
        }

        /**
		 * Stub for out of diagonals elements.
		 * @return zero as {@link Data}.
		 */
        protected Data getZero() {
            if (zero == null) zero = newData("0.0");
            return zero;
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#setValueAt(java.lang.Object, int, int)
		 */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            int diag = getDiagonalCount() / 2;
            int dif = Math.abs(columnIndex - rowIndex);
            if (dif > diag) {
                if (!isOutOfDiagonalsMutable()) return;
                if (!aValue.equals("0.0")) {
                    if (shouldConvert()) {
                        if (2 * dif + 1 > getDiagonalsAllowed()) {
                            convertIntoWholeMatrix();
                            if (getTableModel() == this) throw new IllegalStateException("The matrix couldn't be converted from banded type into stadart (filled) one.");
                            boolean flag = shouldNotyfy();
                            setAllChangesNotification(false);
                            getTableModel().setValueAt(aValue, rowIndex, columnIndex);
                            setAllChangesNotification(flag);
                            fireFieldChanging(new MatrixEvent(MatrixFieldEditor.this, MatrixEvent.CONVERTION_INTO_FILLED));
                            return;
                        }
                    }
                    for (int i = 0; i < dif - diag; i++) addDataDiagonals();
                    setValueAt(aValue, rowIndex, columnIndex);
                }
                return;
            }
            ((MutableData) getElement(columnIndex, rowIndex)).set(aValue, MAIN_FIELD);
            if (shouldNotyfy()) {
                updateTableView(rowIndex);
                if (isNotifyListenerInUpdate()) fireFieldChanging();
            }
        }

        /**
		 * Converting of matrix in diagonal representation into standard matrix.
		 * Unfortunately it is a very slow method.
		 */
        private void convertIntoWholeMatrix() {
            int size = getMainDiagonalLength();
            Data mainDiagonal = ((DataList) getData()).getData(getDiagonalCount() / 2);
            DataSet oldModel = (DataSet) ((MutableComparableDataSet) getData()).copy();
            getData().clear();
            getData().add(mainDiagonal);
            boolean flag = shouldNotyfy();
            setAllChangesNotification(false);
            setMatrixIsBanded(false);
            MatrixTableModel model = (MatrixTableModel) createModel();
            model.setAllChangesNotification(false);
            model.setData(getData());
            model.setShouldConvert(false);
            for (int i = 0; i < size - 1; i++) model.addDataColumn(i + 1);
            setData(oldModel);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    model.setValueAt(getValueAt(j, i), j, i);
                }
            }
            setAllChangesNotification(flag);
            model.setAllChangesNotification(flag);
            model.setShouldConvert(shouldConvert());
            getTable().setModel(model);
            fireTableStructureChanged();
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#fireTableConvert(int)
		 */
        public void fireTableConvert(int eventType) {
            if (eventType == MatrixEvent.CONVERTION_INTO_FILLED) {
                convertIntoWholeMatrix();
                fireFieldChanging(new MatrixEvent(MatrixFieldEditor.this, MatrixEvent.CONVERTION_INTO_FILLED));
            }
        }

        /**
		 * Overridden for matrix in diagonal representation.
		 * Doing nothing.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#addDataColumn(int)
		 */
        protected void addDataColumn(int index) {
        }

        /**
		 * Overridden for matrix in diagonal representation.
		 * Doing nothing.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#removeDataColumn(int)
		 */
        protected void removeDataColumn(int index) {
        }

        /**
		 * Overridden for matrix in diagonal representation.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#addDataRow(int)
		 */
        protected void addDataRow(int index) {
            int diag = getDiagonalCount() / 2;
            for (int i = 0; i < getDiagonalCount(); i++) {
                if (index == 0) ((DataList) getDiagonal(i)).add(0, newData(defaultValue(index, index - (diag - i)))); else ((DataList) getDiagonal(i)).add(index - Math.abs(diag - i), newData(defaultValue(index, index - (diag - i))));
            }
        }

        /**
		 * Changing all table structure.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#removeRow(int)
		 */
        public void removeRow(int index) {
            removeDataRow(index);
            if (shouldNotyfy()) fireTableStructureChanged();
        }

        /**
		 * Overridden for matrix in diagonal representation.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#removeDataRow(int)
		 */
        protected void removeDataRow(int index) {
            int diag = getDiagonalCount() / 2;
            for (int i = 0; i < getDiagonalCount(); i++) {
                if (index == 0) ((DataList) getDiagonal(i)).removeData(0); else ((DataList) getDiagonal(i)).removeData(index - Math.abs(diag - i));
                int last = getDiagonalCount() - 1;
                boolean condition = getDiagonal(0).isEmpty() && getDiagonal(last).isEmpty();
                if ((i == 0 || i == last) && condition) {
                    removeDataFromSet(getData(), last);
                    removeDataFromSet(getData(), 0);
                }
            }
        }

        /**
		 * Adding 2 new diagonals from both sides.
		 */
        protected void addDataDiagonals() {
            int newSize = getDiagonal(0).size() - 1;
            Data[] newDiagonal1 = new Data[newSize];
            Data[] newDiagonal2 = new Data[newSize];
            for (int i = 0; i < newSize; i++) {
                newDiagonal1[i] = newData(defaultValue(i, i + 1));
                newDiagonal2[i] = newData(defaultValue(i + 1, i));
            }
            ComparableDataList dataColumn1 = new ComparableDataList(newDiagonal1);
            ComparableDataList dataColumn2 = new ComparableDataList(newDiagonal2);
            ((DataList) getData()).add(0, newData(dataColumn1));
            getData().add(newData(dataColumn2));
        }

        /**
		 * Removing 2 diagonals from both sides.
		 */
        protected void removeDataDiagonals() {
            ((DataList) getData()).removeData(0);
            ((DataList) getData()).removeData(getData().size() - 1);
        }

        /**
		 * Determination of diagonals number.
		 * @return diagonals number. Could be only 2*N+1.
		 * @exception IllegalStateException when diagonal count is even number.
		 */
        public int getDiagonalCount() {
            int diagonalCount = super.getColumnCount();
            if (diagonalCount % 2 == 0 && diagonalCount != 0) throw new IllegalStateException("Inner error. Table model should have odd diagonals number. Diagonal count: " + diagonalCount);
            return diagonalCount;
        }

        /**
		 * Determination of length of main diagonals. 
		 * @return length of main diagonals.
		 */
        public int getMainDiagonalLength() {
            return super.getRowCount() + getDiagonalCount() / 2;
        }

        /**
		 * Getting diagonal with specified number.
		 * @param index - number of diagonal to be got.
		 * @return diagonal with <code>index</code> number.
		 */
        public DataSet getDiagonal(int index) {
            return super.getColumn(index);
        }

        /**
		 * For square matrixes only.
		 * @see #getRowCount() 
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#getColumnCount()
		 */
        public int getColumnCount() {
            return getMainDiagonalLength();
        }

        /**
		 * For square matrixes only.
		 * @see #getColumnCount()
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.MatrixTableModel#getRowCount()
		 */
        public int getRowCount() {
            return getMainDiagonalLength();
        }

        /**
		 * Is it possible to modify zero-elements that are out of diagonals?
		 * @return true if out of diagonals elenebts are mutable, false - else. Default value is true.
		 */
        public final boolean isOutOfDiagonalsMutable() {
            return outOfDiagonalsMutable;
        }

        /**
		 * Setting possibility to modify out of diagonals elements.
		 * @param outOfDiagonalsMutable - true - if zero values may be modified, false - else.
		 */
        public final void setOutOfDiagonalsMutable(boolean outOfDiagonalsMutable) {
            this.outOfDiagonalsMutable = outOfDiagonalsMutable;
        }
    }

    /**
	 * The simplest realization of model for some data. As data some Object[] array is used.
	 * So table will have only one column.
	 * 
	 * @see #getColumnCount()
	 * @author Korchak Anton
	 */
    public class SimpleTableModel extends AbstractMatrixTableModel<Object[], Object> {

        /**
		 * Doing nothing.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#addDataColumn(int)
		 * @see #getColumnCount()
		 */
        protected void addDataColumn(int index) {
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#addDataRow(int)
		 */
        protected void addDataRow(int index) {
            int size = getRowCount();
            Object[] functions = (Object[]) Array.newInstance(getData().getClass().getComponentType(), size + 1);
            if (index == 0) System.arraycopy(getData(), 0, functions, 1, size); else {
                System.arraycopy(getData(), 0, functions, 0, index);
                System.arraycopy(getData(), index, functions, index + 1, size - index);
            }
            functions[index] = newData(defaultValue(0, index));
            setData(functions);
        }

        /**
		 * New element for this model. 
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#newData(java.lang.Object)
		 */
        protected Object newData(Object value) {
            return value;
        }

        /**
		 * Do nothing.
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#removeDataColumn(int)
		 * @see #getColumnCount()
		 */
        protected void removeDataColumn(int index) {
        }

        /**
		 * @see mipt.crec.lab.common.modules.gui.matrix.MatrixFieldEditor.AbstractMatrixTableModel#removeDataRow(int)
		 */
        protected void removeDataRow(int index) {
            int size = getRowCount() - 1;
            Object[] functions = (Object[]) Array.newInstance(getData().getClass().getComponentType(), size);
            if (index == 0) System.arraycopy(getData(), 1, functions, 0, size); else {
                System.arraycopy(getData(), 0, functions, 0, index);
                System.arraycopy(getData(), index + 1, functions, index, size - index);
            }
            boolean flag = shouldNotyfy();
            setAllChangesNotification(false);
            setData(functions);
            setAllChangesNotification(flag);
        }

        /**
		 * Returns 1 because of mathematical problem.
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
        public int getColumnCount() {
            return 1;
        }

        /**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
        public int getRowCount() {
            if (getData() == null) return 0;
            return getData().length;
        }

        /**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
        public Object getValueAt(int rowIndex, int columnIndex) {
            return getData()[rowIndex];
        }

        /**
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex != 0) return;
            getData()[rowIndex] = value;
            if (shouldNotyfy()) {
                updateTableView(rowIndex);
                if (isNotifyListenerInUpdate()) fireFieldChanging();
            }
        }
    }

    /**
	 * �����, ��� ������� ������� ������� �� ����� ���� ��������.
	 */
    public static final int SOLID = 0;

    /**
	 * �����, ��� ������� ������� ������� ����� ���� ��������
	 * ������ ����������� ��������� ������. 
	 */
    public static final int ROW = 1;

    /**
	 * �����, ��� ������� ������� ������� ����� ���� ��������
	 * ������ ����������� ���������� �������. 
	 */
    public static final int COLUMN = 2;

    /**
	 * �����, ��� ������� ������� ������� ����� ���� �������� ����
	 * ����������� ������, ���� ����������� �������.
	 */
    public static final int ROW_AND_COLUMN = 3;

    /**
	 * �����, ��� ������� ������� ������� ����� ���� ��������
	 * ������������� ����������� ������ � �������. 
	 */
    public static final int BOTH = 4;

    /**
	 * ������ ��������� ������� ������� �� ���������.
	 */
    public static final String HEADER = "x";

    /**
	 * ��������� ����, ��� ���������� ������� �� ����.
	 */
    public static final String EMPTY = "\n";

    /**
	 * �������, ������� ����� ��������� ��� ��������, �
	 * ������������ ��� ����������� ���������.
	 */
    protected JTable table = null;

    private Component component;

    private JScrollPane scrollPane;

    private AdjustmentListener scrollListener;

    /**
	 * �������� GUI ����. ������������ ��� ��� �����������
	 * ����������� �������� ������, ��� � ����������� ���� ��������.
	 */
    protected ResourceBundle bundle = null;

    /**
	 * ������ ����� �������. ������ ��� ������� "columnNameBase+�����_�������" 
	 */
    protected String columnNameBase = HEADER;

    /**
	 * ���������� �� ��������� ������� �������.
	 */
    private boolean isShowHeader = true;

    /**
	 * ����� ��������� ������� �������. �� ��������� �������
	 * ������� �������� ������.
	 */
    private int sizeMode = SOLID;

    /**
	 * ����� ������� �������������� c������ � �������.
	 * �� ����� ���� -1, ���� ���� ���������.
	 */
    private int nonScrollableRows = 5;

    /**
	 * ����������� ������ ���������� ������� (���� ������, ���������� ����� AUTO_RESIZE_OFF)
	 */
    private int minimumColumnWidth = 50;

    /**
	 * ����������� ������ ������� - ����������� � ������ �������������� �������
	 * ���� �� ������� ������ minimumColumnWidth - �������� ������ ��������� (��� ���������?) 
	 */
    private int minimumTableWidth = 80;

    /**
	 * ������ � �������� ����������.
	 */
    protected JPanel buttonPanel = null;

    /**
	 * Whether matrix is banded one. Default value is false.
	 * @see #initTable()
	 */
    protected boolean isBanded = false;

    /**
	 * ������ �� ��������������� {@link Action}. ����� ���� ������������� ��� ���������
	 * ���������� ������� {@link Action}. ���� �����, ��� � � {@link MatrixAction}, �� ����� �����.
	 * //TO DO: ������� ����� ��������.
	 */
    private Action matrixAction = null;

    /**
	 * ����������� ������.
	 * @param table - ������ �� ������ ������� ��� ������.
	 * @exception IllegalArgumentException ����� �������� ������� �� ������.
	 */
    public MatrixFieldEditor(JTable table) {
        setTable(table);
        setNotifyListenerInUpdate(true);
        initTable();
    }

    /**
	 * ����������� ������.
	 * @param table - ������ �� ������ ������� ��� ������.
	 * @param isBanded - whether matrix is banded.
	 * @exception IllegalArgumentException ����� �������� ������� �� ������.
	 */
    public MatrixFieldEditor(JTable table, boolean isBanded) {
        setTable(table);
        setNotifyListenerInUpdate(true);
        this.isBanded = isBanded;
        initTable();
        setMatrixIsBanded(isBanded);
    }

    /**
	 * ����������� ������.
	 * @param table - ������ �� ������ ������� ��� ������.
	 * @param bundle - �������� GUI ����.
	 * @exception IllegalArgumentException ����� �������� ������� �� ������.
	 */
    public MatrixFieldEditor(JTable table, ResourceBundle bundle) {
        this(table);
        setBundle(bundle);
    }

    /**
	 * ����������� ������.
	 * @param table - ������ �� ������ ������� ��� ������.
	 * @param bundle - �������� GUI ����.
	 * @param isBanded - whether matrix is banded.
	 * @exception IllegalArgumentException ����� �������� ������� �� ������.
	 */
    public MatrixFieldEditor(JTable table, ResourceBundle bundle, boolean isBanded) {
        this(table, isBanded);
        setBundle(bundle);
    }

    /**
	 * Complex both model data and view update.
	 */
    public void update() {
        getTableModel().fireTableStructureOnlyChanged();
        fireFieldChanging();
    }

    /**
	 * ������� ������ GUI ����.
	 * @param bundle - �������� GUI ����.
	 */
    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
	 * ������������� �������� ������� ��������������� �����������.
	 */
    protected void initTable() {
        getTable().setModel(createModel());
        getTable().setPreferredScrollableViewportSize(new Dimension(minimumTableWidth, getTable().getRowHeight() * nonScrollableRows));
        getTable().setMinimumSize(new Dimension(minimumTableWidth, getTable().getRowHeight() * (nonScrollableRows + 1)));
        getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTable().setRowSelectionAllowed(true);
        getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTable().getColumnModel().setColumnSelectionAllowed(true);
    }

    /**
	 * Method to create suitable table model.
	 * @return table model.
	 */
    protected AbstractMatrixTableModel createModel() {
        return isBanded ? new BandedMatrixTableModel() : new MatrixTableModel();
    }

    /**
	 * ��������� ����������� ������ �������.
	 * @return ������ �������.
	 */
    public AbstractMatrixTableModel getTableModel() {
        return (AbstractMatrixTableModel) getTable().getModel();
    }

    /**
	 * ������� ���������������� ������� ��� �������.
	 * @param isTransposed - true, ���� ���������������, false - �����.
	 */
    public final void setTransposition(boolean isTransposed) {
        setTransposition(isTransposed, true);
    }

    /**
	 * ������� ���������������� ������� ��� �������.
	 * @param isTransposed - true, ���� ���������������, false - �����.
	 * @param isImplicit - true, ���� ��������������� ����� ������ ������������,
	 * � ������� ������ ����� ��������������� ��� �������������������.
	 */
    public final void setTransposition(boolean isTransposed, boolean isImplicit) {
        ((MatrixTableModel) getTableModel()).setTransposition(isTransposed, isImplicit);
    }

    /**
	 * �������� �� ������� ��� ������ ������������������.
	 * @return true, ���� ����������������, false - ���.
	 */
    public final boolean isTransposed() {
        return ((MatrixTableModel) getTableModel()).isTransposed();
    }

    /**
	 * ��������� ������ �� ������� ���������.
	 * @return ������� ���������.
	 */
    public final JTable getTable() {
        return table;
    }

    /**
	 * ������� ������� ���������.
	 * @param table - ����� ������� ���������.
	 * @exception IllegalArgumentException ����� �������� null.
	 */
    public void setTable(JTable table) {
        if (table == null) throw new IllegalArgumentException("Main table set as null.");
        this.table = table;
    }

    /**
	 * ���������� �� ��������� �������?
	 * ���� ������� false, �� ������ ����� ������ �� ������ ��������. 
	 * @param isDisplay - true, ���� ����������, false - �� ����������.
	 */
    public void displayHeaders(boolean isDisplay) {
        isShowHeader = isDisplay;
    }

    /**
	 * ���������� �� ��������� �������? 
	 * @return true, ���� ����������, false - �� ����������.
	 */
    public boolean isDisplayHeaders() {
        return isShowHeader;
    }

    /**
	 * ������� ������ ����� �������, �������� "y".
	 * ��� ������� ����� ����������� �� ������� "base+�����_�������".
	 * ���� ������ null, �� �� ��������� ������ �������� ��� "x".
	 * ��������, ���� displayHeaders(boolean) ������ �� ��������� false,
	 * �� ������� �� ����� ����� ��������� ������.
	 * @param base - ������ ����� ��������� ������� �������.
	 * @see #displayHeaders(boolean)
	 */
    public void setHeaderBase(String base) {
        if (base == null) columnNameBase = HEADER;
        columnNameBase = base;
    }

    /**
	 * ��������� ������ ������������ �������� �������.
	 * ������������ ��������� ������.
	 * @return ����� ������������ �������� �������.
	 */
    public final int getSizeMode() {
        return sizeMode;
    }

    /**
	 * ������� ������ ������������ �������� ������� (�����������
	 * ���������� � �������� ���������). �� ��������� ������ ���������
	 * � �������� ������. ��. ��������� ������.
	 * @param mode - ����� �����. 
	 */
    public final void setSizeMode(int mode) {
        sizeMode = mode;
        buttonPanel = null;
    }

    /**
	 * ��������� ������ ������, ����������� ���������� �������.
	 * � ����������� �� ������ ���������� �������� ������������ ��
	 * ��� ���� ������. ������ ����� ������������ ��� �������, �������
	 * ������� ���� �����.
	 * @return ������, ���������� ������ ����������. 
	 */
    public JComponent getButtonPane() {
        if (buttonPanel != null) return buttonPanel;
        switch(getSizeMode()) {
            case ROW:
                JButton removeRow = new JButton(getAction("Remove", MatrixFieldEditor.ROW, MatrixAction.REMOVE, null));
                matrixAction = removeRow.getAction();
                JButton addRow = new JButton(getAction("Add", MatrixFieldEditor.ROW, MatrixAction.ADD, matrixAction));
                buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                buttonPanel.add(addRow);
                buttonPanel.add(removeRow);
                return buttonPanel;
            case COLUMN:
                JButton removeColumn = new JButton(getAction("Remove", MatrixFieldEditor.COLUMN, MatrixAction.REMOVE, null));
                matrixAction = removeColumn.getAction();
                JButton addColumn = new JButton(getAction("Add", MatrixFieldEditor.COLUMN, MatrixAction.ADD, matrixAction));
                buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                buttonPanel.add(addColumn);
                buttonPanel.add(removeColumn);
                return buttonPanel;
            case ROW_AND_COLUMN:
                removeRow = new JButton(getAction("Remove Row", MatrixFieldEditor.ROW, MatrixAction.REMOVE, null));
                addRow = new JButton(getAction("Add Row", MatrixFieldEditor.ROW, MatrixAction.ADD, removeRow.getAction()));
                removeColumn = new JButton(getAction("Remove Column", MatrixFieldEditor.COLUMN, MatrixAction.REMOVE, null));
                addColumn = new JButton(getAction("Add Column", MatrixFieldEditor.COLUMN, MatrixAction.ADD, removeColumn.getAction()));
                buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                buttonPanel.add(addRow);
                buttonPanel.add(removeRow);
                buttonPanel.add(addColumn);
                buttonPanel.add(removeColumn);
                return buttonPanel;
            case BOTH:
                JButton removeVariable = new JButton(getAction("Remove variable", MatrixFieldEditor.BOTH, MatrixAction.REMOVE, null));
                matrixAction = removeVariable.getAction();
                JButton addVarible = new JButton(getAction("Add variable", MatrixFieldEditor.BOTH, MatrixAction.ADD, matrixAction));
                buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                buttonPanel.add(addVarible);
                buttonPanel.add(removeVariable);
                return buttonPanel;
            default:
                return new JPanel();
        }
    }

    /**
	 * ��������� {@link Action}, ��������������� �������� ����������. ���� ����� ���� ������, �� �������� �������� ����������.
	 * @param name - ��� ������� �� ���������� �������������.
	 * @param currentMode - �������� �����, ��������������� ��� ��������.
	 * @param action - �������������� �����, ��������������� ��� ��������.
	 * @param matrixAction - ������ �� �������������� ������� {@link Action}. ����� ���� null.
	 * @return �������� ��� ��������.
	 */
    protected final MatrixAction getAction(String name, int currentMode, int action, Action matrixAction) {
        MatrixAction result = createAction(name, currentMode, action, matrixAction);
        if (action == MatrixAction.REMOVE && getTable().getRowCount() == 1) result.setEnabled(false);
        return result;
    }

    /**
	 * Factory Method 
	 */
    protected MatrixAction createAction(String name, int currentMode, int action, Action matrixAction) {
        return new MatrixAction(name, this, currentMode, action, matrixAction);
    }

    /**
	 * @see mipt.aaf.edit.form.AbstractFieldEditor#getEditorValue()
	 */
    public Object getEditorValue() {
        return getTableModel().getData();
    }

    /**
	 * @see mipt.aaf.edit.form.AbstractFieldEditor#fireFieldChanging()
	 */
    public void fireFieldChanging() {
        int colCount = getTable().getColumnCount();
        for (int i = 0; i < colCount; i++) {
            getTable().getColumnModel().getColumn(i).setPreferredWidth(minimumColumnWidth);
        }
        int maxWidth = getTable().getWidth(), pWidth = getTable().getParent().getParent().getWidth();
        if (maxWidth > pWidth || colCount < 11) maxWidth = pWidth;
        getTable().setAutoResizeMode(maxWidth != 0 && colCount * minimumColumnWidth > maxWidth ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        super.fireFieldChanging();
    }

    /**
	 * The same as fireFieldChanging() but instead of getEditorValue() some object is transmitted.
	 * @param object - some object transmitted to listeners.
	 * @see AbstractFieldEditor#fireFieldChanging()
	 */
    public void fireFieldChanging(Object object) {
        try {
            if (notify && listener != null) listener.fieldChanging(getField(), object, null);
        } catch (FieldVetoException exc) {
            updateValue(exc.getCorrectValue());
            doAfterCorrection();
        }
    }

    /**
	 * ������� ����� �����, ������� �� ���������� ����� ��������,
	 * ����� ���������� ������ ��������� � �������.
	 * @param number - ����� ������� �������������� �������.
	 */
    public final void setNumberOfNonScrollableRows(int number) {
        nonScrollableRows = number + 1;
        getTable().getPreferredScrollableViewportSize().height = getTable().getRowHeight() * nonScrollableRows;
    }

    /**
	 * �������� ����� �����, ������� �� ���������� ����� ��������,
	 * ����� ���������� ������ ��������� � �������.
	 * @return ����� ������� �������������� �������.
	 */
    public final int getNumberOfNonScrollableRows() {
        return nonScrollableRows - 1;
    }

    /**
	 * @see mipt.gui.ComponentOwner#getComponent()
	 */
    public Component getComponent() {
        if (component == null) component = initComponent();
        return component;
    }

    protected Component initComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getScrollPane(), BorderLayout.CENTER);
        panel.add(getButtonPane(), BorderLayout.SOUTH);
        return panel;
    }

    public final JScrollPane getScrollPane() {
        if (scrollPane == null) scrollPane = initScrollPane();
        return scrollPane;
    }

    protected JScrollPane initScrollPane() {
        JScrollPane pane = new JScrollPane(getTable(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setMinimumSize(getTable().getMinimumSize());
        pane.setPreferredSize(getTable().getPreferredScrollableViewportSize());
        pane.setMaximumSize(getTable().getMaximumSize());
        return pane;
    }

    /**
	 * ������ ������ ������������� ��� ������� ���������, ������������ ��� ���� �������,
	 *  ������ ����� �� ����� ���� � ����� ��������� ��������� �������� �������� ����.
	 * ���� ����� �� ������ ����������� ������, ��� ��� ��� ��������������� ����� (��. createModel()). 
	 */
    protected void setMatrixIsBanded(boolean isBanded) {
        this.isBanded = isBanded;
        if (isBanded) {
            if (scrollListener == null) scrollListener = initScrollListener();
            getScrollPane().getHorizontalScrollBar().addAdjustmentListener(scrollListener);
            getScrollPane().getVerticalScrollBar().addAdjustmentListener(scrollListener);
        } else if (scrollListener != null) {
            getScrollPane().getHorizontalScrollBar().removeAdjustmentListener(scrollListener);
            getScrollPane().getVerticalScrollBar().removeAdjustmentListener(scrollListener);
        }
    }

    /**
	 * To know about matrix type exlernaly. Bad method.
	 * @return whether matrix is banded.
	 */
    public final boolean isBanded() {
        return isBanded;
    }

    protected AdjustmentListener initScrollListener() {
        return new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (e.getValueIsAdjusting()) return;
                if (!shouldNotyfy()) return;
                BoundedRangeModel source = ((JScrollBar) e.getSource()).getModel();
                boolean horScroll = source == getScrollPane().getHorizontalScrollBar().getModel();
                BoundedRangeModel dest = horScroll ? getScrollPane().getVerticalScrollBar().getModel() : getScrollPane().getHorizontalScrollBar().getModel();
                double k = ((double) source.getValue() - source.getMinimum()) / (source.getMaximum() - source.getExtent() - source.getMinimum());
                int value = dest.getMinimum() + (int) (k * (dest.getMaximum() - dest.getExtent() - dest.getMinimum()));
                if (Math.abs(dest.getValue() - value) < 2) return;
                dest.setValue(value);
            }
        };
    }

    /**
	 * @see mipt.aaf.edit.form.FieldEditor#setValue(java.lang.Object)
	 */
    public void setValue(Object value) {
        getTableModel().setData(value);
    }

    /**
	 * ������� ��������� �������� �������.
	 * @renderer - �������� �������� �������.
	 */
    public void setCellRenderer(TableCellRenderer renderer) {
        getTable().setDefaultRenderer(Object.class, renderer);
    }

    /**
	 * ��������� ��������� �������� �������.
	 * @return �������� �������� �������. �� null.
	 */
    public TableCellRenderer getCellRenderer() {
        return getTable().getDefaultRenderer(Object.class);
    }

    /**
	 * ������� ��������� �������� �������.
	 * @renderer - �������� �������� �������.
	 */
    public void setCellEditor(TableCellEditor editor) {
        getTable().setDefaultEditor(Object.class, editor);
    }

    /**
	 * ��������� ��������� �������� �������.
	 * @return ��������� �������� �������. �� null.
	 */
    public TableCellEditor getCellEditor() {
        return getTable().getDefaultEditor(Object.class);
    }

    /**
	 * Setting state of GUI and controller notification.
	 * See comment to class.
	 * !!!Delegation to {@link AbstractMatrixTableModel}. This is done for connection with
	 * dependent tables.
	 * @param isNotify - true, to notify, false - else. Default value is true.
	 */
    public void setAllChangesNotification(boolean isNotify) {
        getTableModel().setAllChangesNotification(isNotify);
    }

    /**
	 * Whether to notify both controller and GUI about all changes in matrix.
	 * !!!Delegation to {@link AbstractMatrixTableModel}. This is done for connection with
	 * dependent tables.
	 * @return true - if notify, else - false.
	 */
    public final boolean shouldNotyfy() {
        return getTableModel().shouldNotyfy();
    }
}
