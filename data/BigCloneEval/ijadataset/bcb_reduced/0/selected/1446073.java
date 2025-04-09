package PlateArrayer.undospread;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import PlateArrayer.EditAdapter;
import PlateArrayer.ModelPlate;
import PlateArrayer.ModelSourceSorted;

/**
* The Paste Edit class record the changes occured to the
* spreadsheet after performing an orthogonal paste action. The paste edit support
* undo / redo of paste action.
*
* @author Team Matrix & Peter
*/
public class PasteOEdit extends AbstractUndoableEdit {

    private int startRow, numRows, startCol, numCols;

    private ModelPlate modelDest;

    private ModelSourceSorted modelSource;

    private Object arrayTemp[][];

    private Object arrayPaste[][];

    public PasteOEdit(JTable tableSource, JTable tableDest, EditAdapter clipboardEdit) {
        modelDest = (ModelPlate) tableDest.getModel();
        modelSource = (ModelSourceSorted) tableSource.getModel();
        this.startRow = tableDest.getSelectedRow();
        this.numRows = clipboardEdit.getClipboardHeight();
        this.startCol = tableDest.getSelectedColumn();
        this.numCols = clipboardEdit.getClipboardWidth();
        int newNumRows = startRow + numCols;
        int newNumCols = startCol + numRows;
        if ((modelDest.getRowCount() < newNumRows) || (modelDest.getColumnCount() < newNumCols)) {
            JOptionPane.showMessageDialog(tableSource, "Attempt to paste orthogonally outside the bounds", "PAD error", JOptionPane.ERROR_MESSAGE);
        } else {
            Object[][] arrayTmpPaste = clipboardEdit.getArrayPaste();
            arrayTemp = new Object[numCols][numRows];
            arrayPaste = new Object[numCols][numRows];
            for (int i = 0; i < numCols; i++) {
                for (int j = 0; j < numRows; j++) {
                    arrayPaste[i][j] = arrayTmpPaste[j][i];
                    arrayTemp[i][j] = modelDest.getValueAt(startRow + i, startCol + j);
                    modelDest.setValueAt(arrayPaste[i][j], startRow + i, startCol + j);
                    modelSource.makeAvailable(arrayTemp[i][j]);
                    modelSource.makeUnavailable(arrayPaste[i][j], startRow + i, startCol + j);
                }
            }
        }
        tableDest.setRowSelectionInterval(startRow, newNumRows - 1);
        tableDest.setColumnSelectionInterval(startCol, newNumCols - 1);
    }

    public void undo() throws CannotUndoException {
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                modelDest.setValueAt(arrayTemp[i][j], startRow + i, startCol + j);
                modelSource.makeAvailable(arrayPaste[i][j]);
                modelSource.makeUnavailable(arrayTemp[i][j], startRow + i, startCol + j);
            }
        }
    }

    public void redo() throws CannotRedoException {
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                modelDest.setValueAt(arrayPaste[i][j], startRow + i, startCol + j);
                modelSource.makeAvailable(arrayTemp[i][j]);
                modelSource.makeUnavailable(arrayPaste[i][j], startRow + i, startCol + j);
            }
        }
    }

    public boolean canUndo() {
        return true;
    }

    public boolean canRedo() {
        return true;
    }

    public String getPresentationName() {
        return "Paste";
    }
}
