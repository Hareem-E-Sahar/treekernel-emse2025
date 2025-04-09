package awtx;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import awtx.table.*;

/**
 * Fast Table for displaying row/column data
 */
public class Table extends Scrollpane implements TableModelListener {

    private Content compContent;

    private Header compHeader;

    private Vector listeners = new Vector();

    private boolean isMultiSelectionAllowed = false;

    private TableModel model = null;

    private int[] translation = null;

    private boolean isSortable = false;

    private PopupProvider popupProvider = null;

    private static DefaultCellRenderer dcr = new DefaultCellRenderer();

    private static DefaultHeaderCellRenderer dhr = new DefaultHeaderCellRenderer();

    private int initialColumnLayout = NONE;

    public static final int NONE = 0, SIZE_ALL_COLUMNS = 1;

    public static final int CLICK = 0, CCLICK = 1, DCLICK = 2, POPUP = 3;

    public static final int UP = 1, DOWN = 0;

    private int sortedColumn = -1;

    private int sortedDir = DOWN;

    private BitSet selectedRows = new BitSet(64);

    private int draggedColumn = -1;

    private int colWidths[] = new int[0];

    private int minWidth = 20;

    private Insets cellInsets = new Insets(1, 1, 1, 1);

    private CellRenderer colRenderer[];

    private CellRenderer headerRenderer[];

    /**
   * Constructor with default ScrollBars
   */
    public Table() {
        setUseSpace(ALIGN_LEFT, ALIGN_TOP);
        compContent = new Content(this);
        compHeader = new Header(this);
        setQuadrant(CENTER, compContent);
        setQuadrant(NORTH, compHeader);
        MouseListener mlistener = new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                compContent.requestFocus();
                int mode = CLICK;
                if (e.isPopupTrigger()) mode = POPUP;
                if (isDoubleClick(e)) mode = DCLICK;
                if (e.isControlDown()) mode = CCLICK;
                mouseClick(e.getSource(), e.getX(), e.getY(), mode);
            }
        };
        getQuadrant(CENTER).addMouseListener(mlistener);
        getQuadrant(NORTH).addMouseListener(mlistener);
        MouseMotionListener mmlistener = new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                Table.this.mouseDragged(e.getX(), e.getY());
            }

            public void mouseMoved(MouseEvent e) {
                Table.this.mouseMoved(e.getX(), e.getY());
            }
        };
        getQuadrant(NORTH).addMouseMotionListener(mmlistener);
        KeyListener klistener = new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                Point old = getScrollPosition();
                int move = 0;
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        move = -1;
                        break;
                    case KeyEvent.VK_DOWN:
                        move = 1;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        move = -10;
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        move = 10;
                        break;
                }
                if (move != 0) {
                    int newrow;
                    if (translation == null) newrow = getSelectedRow() + move; else {
                        newrow = Math.min(getNumRows() - 1, Math.max(0, translation[getSelectedRow()] + move));
                        newrow = translation[newrow];
                    }
                    setSelectedRow(newrow);
                }
            }
        };
        compContent.addKeyListener(klistener);
    }

    /**
   * Adds a Listener
   */
    public void addTableListener(TableListener l) {
        listeners.addElement(l);
    }

    /**
   * Clears the selection
   * @param row the new selected row
   */
    public void clearSelection() {
        if (model == null) return;
        selectedRows.and(new BitSet(64));
        fireSelectionChanged();
    }

    /**
   * Helper for comparing values in translation table
   * @return 0 if equal, <0 if i<j and >0 if i>j
   */
    private int compareTranslation(int i, int j) {
        int result = (sortedDir == DOWN ? 1 : -1) * model.compareRows(translation[i], translation[j], sortedColumn);
        return result;
    }

    /**
   * Notifies listeners of action performed
   * (in case of Enter or double click)
   */
    protected void fireActionPerformed(int row) {
        Enumeration e = listeners.elements();
        while (e.hasMoreElements()) ((TableListener) e.nextElement()).actionPerformed(row);
    }

    /**
   * Notifies listeners of change in selection
   */
    protected void fireSelectionChanged() {
        getQuadrant(CENTER).repaint();
        Enumeration e = listeners.elements();
        while (e.hasMoreElements()) ((TableListener) e.nextElement()).rowSelectionChanged(getSelectedRows());
    }

    /**
   * Returns the cell insets for this table.
   * @see setCellInsets
   */
    public Insets getCellInsets() {
        return cellInsets;
    }

    /**
   * Accessor for header renderers
   */
    public CellRenderer[] getColumnHeaderRenderers() {
        return headerRenderer;
    }

    /**
   * Accessor for column renderers
   */
    public CellRenderer[] getColumnRenderers() {
        return colRenderer;
    }

    /**
   * Returns an array with all column widths
   */
    public int[] getColumnWidths() {
        return colWidths;
    }

    /**
   * Returns the header object of given column of this table
   */
    public Object getHeaderAt(int column) {
        if (model == null) return null;
        return model.getHeaderAt(column);
    }

    /**
   * Returns the data of this Table
   */
    public TableModel getModel() {
        return model;
    }

    /**
   * Returns the number of columns of this table
   */
    public int getNumColumns() {
        if (model == null) return 0;
        return model.getNumColumns();
    }

    /**
   * Returns the number of rows of this table
   */
    public int getNumRows() {
        if (model == null) return 0;
        return model.getNumRows();
    }

    /**
   * Returns a selected row
   * @return row selected last or -1
   */
    public int getSelectedRow() {
        int rows[] = getSelectedRows();
        if (rows.length == 0) return -1;
        return rows[0];
    }

    /**
   * Returns the selected rows
   */
    public int[] getSelectedRows() {
        if (model == null) return new int[0];
        int[] temp = new int[model.getNumRows()];
        int count = 0;
        for (int i = 0; i < temp.length; i++) {
            if (selectedRows.get(i)) temp[count++] = i;
        }
        int[] result = new int[count];
        System.arraycopy(temp, 0, result, 0, count);
        return result;
    }

    /**
   * the index of the sorting by column
   */
    public int getSortedColumn() {
        return sortedColumn;
    }

    /**
   * Convenient accessing method
   */
    public int getSortedDir() {
        return sortedDir;
    }

    /**
   * Returns the transformed index of a row in this Table
   */
    public int getTranslatedRow(int row) {
        if (model == null) return row;
        if (translation != null) return translation[row];
        return row;
    }

    /**
   * Event number of rows have changed
   */
    public void handleNumRowsChanged(int newRows) {
        sortedColumn = -1;
        translation = null;
        invalidate();
        validate();
        repaint();
        fireSelectionChanged();
    }

    /**
   * Event certain rows have changed
   */
    public void handleRowsChanged(int[] rows) {
        repaint();
    }

    /**
   * Method for identifying MouseDoubleClicks
   */
    protected boolean isDoubleClick(MouseEvent e) {
        return e.getClickCount() > 1;
    }

    /**
   * Checks whether a row is selected
   * @param row the candidate to be tested
   */
    public boolean isSelectedRow(int row) {
        if (row < 0) return false;
        return selectedRows.get(row);
    }

    /**
   * Returns wether this table is sortable
   */
    public boolean isSortable() {
        return isSortable;
    }

    /**
   * Helper which calculate column widths
   */
    private void layoutColumns() {
        initialColumnLayout = NONE;
    }

    /**
   * Handle a click on tableComponent
   */
    private void mouseClick(Object source, int xpos, int ypos, int mode) {
        int col = compContent.convertPos2Col(xpos);
        if (col < 0) return;
        if (source == getQuadrant(NORTH)) {
            if ((isSortable) && (draggedColumn < 0) && (ypos >= 0)) {
                setSortedColumn(col, sortedDir == UP ? DOWN : UP);
            }
            return;
        }
        int row = compContent.convertPos2Row(ypos);
        if (row < 0) {
            return;
        }
        if (translation != null) {
            row = translation[row];
        }
        switch(mode) {
            default:
                setSelectedRow(row);
                break;
            case CCLICK:
                toggleSelectedRow(row);
                break;
            case POPUP:
                setSelectedRow(row);
                if (popupProvider != null) popupProvider.providePopup(getQuadrant(CENTER), xpos, ypos);
                break;
            case DCLICK:
                setSelectedRow(row);
                fireActionPerformed(row);
                break;
        }
    }

    /**
   * MouseDragged
   */
    private void mouseDragged(int x, int y) {
        if (draggedColumn < 0) {
            return;
        }
        int pos = compContent.convertCol2Pos(draggedColumn);
        int w = Math.max(minWidth, x - pos);
        colWidths[draggedColumn] = w;
        getQuadrant(CENTER).invalidate();
    }

    /**
   * MouseMovement
   */
    private void mouseMoved(int x, int y) {
        int cursor = Cursor.DEFAULT_CURSOR;
        draggedColumn = -1;
        int col = compContent.convertPos2Col(x);
        if ((col >= 0) && (compContent.convertCol2Pos(col) + colWidths[col] - x < 8)) {
            cursor = Cursor.W_RESIZE_CURSOR;
            draggedColumn = col;
        }
        getQuadrant(NORTH).setCursor(Cursor.getPredefinedCursor(cursor));
    }

    /**
   * Helper for sorting the translation table
   */
    private void qSortTranslation(int lower, int upper) {
        if (lower >= upper) return;
        if (lower + 1 == upper) {
            if (compareTranslation(lower, upper) > 0) {
                swapTranslation(lower, upper);
            }
            return;
        }
        int pivot = lower + (upper - lower) / 2;
        int l = lower, u = upper;
        while (true) {
            while ((l < pivot) && (compareTranslation(l, pivot) <= 0)) {
                l++;
            }
            while ((u > pivot) && (compareTranslation(pivot, u) < 0)) {
                u--;
            }
            if (l >= u) {
                break;
            }
            if ((l < pivot) && (u > pivot)) {
                swapTranslation(l, u);
                l++;
                u--;
                continue;
            }
            if (l < pivot) {
                swapTranslation(l, pivot);
                pivot = l;
            } else {
                swapTranslation(pivot, u);
                pivot = u;
            }
        }
        qSortTranslation(lower, pivot - 1);
        qSortTranslation(pivot + 1, upper);
    }

    /**
   * Notification that this component isn't needed anymore
   */
    public void removeNotify() {
        setModel(null);
        super.removeNotify();
    }

    /**
   * Removes a Listener
   */
    public void removeTableListener(TableListener l) {
        listeners.removeElement(l);
    }

    /**
   * Selects all
   * @param row the new selected row
   */
    public void selectAll() {
        if (model == null) return;
        for (int i = 0; i < model.getNumRows(); i++) selectedRows.set(i);
        fireSelectionChanged();
    }

    /**
   * Sets cell insets for this table - that is the
   * space around every cell in the rendered table.
   */
    public void setCellInsets(Insets insets) {
        cellInsets = insets;
    }

    /**
   * Sets cell renderers for all columns
   * @param an array of new renderers which can contain nulls
   */
    public void setCellRenderers(CellRenderer renderers[]) {
        if (colRenderer == null) return;
        for (int c = 0; c < colWidths.length && c < renderers.length; c++) {
            if (renderers[c] != null) colRenderer[c] = renderers[c];
        }
        repaint();
    }

    /**
   * Sets a column width
   * @param widths an array with widths
   * @return wether all columns were set
   */
    public boolean setColumnWidths(int widths[]) {
        for (int c = 0; c < colWidths.length && c < widths.length; c++) {
            colWidths[c] = Math.max(minWidth, widths[c]);
        }
        doLayout();
        return widths.length == colWidths.length;
    }

    /**
   * Sets header renderers for all columns
   * @param an array of new renderers which can contain nulls
   */
    public void setHeaderCellRenderers(CellRenderer renderers[]) {
        if (colRenderer == null) return;
        for (int c = 0; c < colWidths.length && c < renderers.length; c++) {
            if (renderers[c] != null) {
                headerRenderer[c] = renderers[c];
            }
        }
        repaint();
    }

    /**
   * Sets the behaviour for intial column layout
   */
    public void setInitialColumnLayout(int l) {
        initialColumnLayout = l;
    }

    /**
   * Set Data of this Table
   */
    public void setModel(TableModel pModel) {
        if (model != null) {
            model.removeTableModelListener(this);
        }
        model = pModel;
        translation = null;
        sortedColumn = -1;
        clearSelection();
        if (model != null) {
            colWidths = new int[model.getNumColumns()];
            for (int i = 0; i < model.getNumColumns(); i++) {
                colWidths[i] = 100;
            }
            colRenderer = new CellRenderer[model.getNumColumns()];
            headerRenderer = new CellRenderer[model.getNumColumns()];
            for (int i = 0; i < model.getNumColumns(); i++) {
                colRenderer[i] = dcr;
                headerRenderer[i] = dhr;
            }
            model.addTableModelListener(this);
        }
        doLayout();
    }

    /**
   * Changes the behaviour of selecting more than one row at once
   */
    public void setMultiSelectedAllowed(boolean set) {
        isMultiSelectionAllowed = set;
    }

    /**
   * Sets the PopupProvider to be used here
   */
    public void setPopupProvider(PopupProvider provider) {
        popupProvider = provider;
    }

    /**
   * Sets the selection to a single row
   * @param row the new selected row
   */
    public void setSelectedRow(int row) {
        if (model == null) {
            return;
        }
        if ((row < 0) || (row > model.getNumRows())) {
            throw new IllegalArgumentException("setSelectedRow out of bounds");
        }
        if (selectedRows.get(row)) {
            return;
        }
        selectedRows = new BitSet(model.getNumRows());
        selectedRows.set(row);
        fireSelectionChanged();
    }

    /**
   * Dis/Enables sorting
   */
    public void setSortable(boolean on) {
        isSortable = on;
    }

    /**
   * Sets the column to be sorted by
   * @param col the sorting column (0based)
   * @param dir direction
   */
    public void setSortedColumn(int col, int dir) {
        if ((getNumRows() < 2) || (col < 0) || (col >= getNumColumns())) {
            sortedColumn = -1;
            translation = null;
        } else {
            sortedColumn = col;
            sortedDir = dir;
            if (translation == null) {
                translation = new int[getNumRows()];
            }
            for (int i = 0; i < translation.length; i++) {
                translation[i] = translation.length - 1 - i;
            }
            qSortTranslation(0, translation.length - 1);
        }
        getQuadrant(CENTER).repaint();
        getQuadrant(NORTH).repaint();
    }

    /**
   * Helper for swapping values in translation table
   */
    private void swapTranslation(int i, int j) {
        int t = translation[i];
        translation[i] = translation[j];
        translation[j] = t;
    }

    /**
   * Adds a row to the selection
   * @param row the new selected row
   */
    public void toggleSelectedRow(int row) {
        if (model == null) {
            return;
        }
        if (selectedRows.get(row)) selectedRows.clear(row); else {
            if (!isMultiSelectionAllowed) {
                selectedRows = new BitSet(model.getNumRows());
            }
            selectedRows.set(row);
        }
        fireSelectionChanged();
    }
}
