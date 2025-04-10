package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on a SWT <code>Table</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. It
 * is designed to be instantiated with a pre-existing SWT table control and
 * configured with a domain-specific content provider, table label provider,
 * element filter (optional), and element sorter (optional).
 * </p>
 * <p>
 * Label providers for table viewers must implement either the
 * <code>ITableLabelProvider</code> or the <code>ILabelProvider</code>
 * interface (see <code>TableViewer.setLabelProvider</code> for more details).
 * </p>
 * <p>
 * As of 3.1 the TableViewer now supports the SWT.VIRTUAL
 * flag. It is important to note that if SWT.VIRTUAL is in use
 * that the Widget based APIs will return null in both the
 * cases where either the element is not specified or not
 * created yet. 
 * </p>
 * <p>
 * Users of SWT.VIRTUAL should also avoid using getItems()
 * from the Table within the TreeViewer as this does not
 * necessarily generate a callback for the TreeViewer 
 * to populate the items. It also has the side effect of
 * creating all of the items thereby eliminating the
 * performance improvements of SWT.VIRTUAL.
 * </p>
 * @see SWT#VIRTUAL
 * @see #doFindItem(Object)
 * @see #internalRefresh(Object, boolean)
 */
public class TableViewer extends StructuredViewer {

    private class VirtualManager {

        /**
		 * The currently invisible elements as provided 
		 * by the content provider or by addition.
		 * This will not be populated by an ILazyStructuredContentProvider
		 * as an ILazyStructuredContentProvider is only queried
		 * on the virtual callabck.
		 */
        private Object[] cachedElements = new Object[0];

        /**
		 * Create a new instance of the receiver.
		 *
		 */
        public VirtualManager() {
            addTableListener();
        }

        /**
		 * Add the listener for SetData on the table
		 */
        private void addTableListener() {
            table.addListener(SWT.SetData, new Listener() {

                public void handleEvent(Event event) {
                    TableItem item = (TableItem) event.item;
                    final int index = table.indexOf(item);
                    Object element = resolveElement(index);
                    if (element == null) {
                        IContentProvider contentProvider = getContentProvider();
                        if (contentProvider instanceof ILazyContentProvider) {
                            ((ILazyContentProvider) contentProvider).updateElement(index);
                            return;
                        }
                    }
                    associate(element, item);
                    updateItem(item, element);
                }
            });
        }

        /**
		 * Get the element at index.Resolve it lazily if this
		 * is available.
		 * @param index
		 * @return Object or <code>null</code> if it could
		 * not be found
		 */
        protected Object resolveElement(int index) {
            Object element = null;
            if (index < cachedElements.length) element = cachedElements[index];
            return element;
        }

        /**
		 * A non visible item has been added.
		 * @param element
		 * @param index
		 */
        public void notVisibleAdded(Object element, int index) {
            int requiredCount = index + 1;
            if (requiredCount > getTable().getItemCount()) {
                getTable().setItemCount(requiredCount);
                Object[] newCache = new Object[requiredCount];
                System.arraycopy(cachedElements, 0, newCache, 0, cachedElements.length);
                cachedElements = newCache;
            }
            cachedElements[index] = element;
        }
    }

    private VirtualManager virtualManager;

    /**
	 * TableColorAndFontNoOp is an optimization for tables without
	 * color and font support.
	 * @see ITableColorProvider
	 * @see ITableFontProvider
	 */
    private class TableColorAndFontNoOp {

        /**
		 * Create a new instance of the receiver.
		 *
		 */
        TableColorAndFontNoOp() {
        }

        /**
		 * Set the fonts and colors for the tableItem if there is a color
		 * and font provider available.
		 * @param tableItem The item to update.
		 * @param element The element being represented
		 * @param column The column index
		 */
        public void setFontsAndColors(TableItem tableItem, Object element, int column) {
        }
    }

    /**
	 * TableColorAndFontCollector is an helper class for color and font
	 * support for tables that support the ITableFontProvider and
	 * the ITableColorProvider.
	 * @see ITableColorProvider
	 * @see ITableFontProvider
	 */
    private class TableColorAndFontCollector extends TableColorAndFontNoOp {

        ITableFontProvider fontProvider = null;

        ITableColorProvider colorProvider = null;

        /**
		 * Create an instance of the receiver. Set the color and font
		 * providers if provider can be cast to the correct type.
		 * @param provider IBaseLabelProvider
		 */
        public TableColorAndFontCollector(IBaseLabelProvider provider) {
            if (provider instanceof ITableFontProvider) fontProvider = (ITableFontProvider) provider;
            if (provider instanceof ITableColorProvider) colorProvider = (ITableColorProvider) provider;
        }

        /**
		 * Set the fonts and colors for the tableItem if there is a color
		 * and font provider available.
		 * @param tableItem The item to update.
		 * @param element The element being represented
		 * @param column The column index
		 */
        public void setFontsAndColors(TableItem tableItem, Object element, int column) {
            if (colorProvider != null) {
                tableItem.setBackground(column, colorProvider.getBackground(element, column));
                tableItem.setForeground(column, colorProvider.getForeground(element, column));
            }
            if (fontProvider != null) tableItem.setFont(column, fontProvider.getFont(element, column));
        }
    }

    /**
	 * Internal table viewer implementation.
	 */
    private TableEditorImpl tableViewerImpl;

    /**
	 * This viewer's table control.
	 */
    private Table table;

    /**
	 * This viewer's table editor.
	 */
    private TableEditor tableEditor;

    /**
	 * The color and font collector for the cells.
	 */
    private TableColorAndFontNoOp tableColorAndFont = new TableColorAndFontNoOp();

    /**
	 * Creates a table viewer on a newly-created table control under the given
	 * parent. The table control is created using the SWT style bits
	 * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters. The table has no columns.
	 * 
	 * @param parent
	 *            the parent control
	 */
    public TableViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    }

    /**
	 * Creates a table viewer on a newly-created table control under the given
	 * parent. The table control is created using the given style bits. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters. The table has no columns.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            SWT style bits
	 */
    public TableViewer(Composite parent, int style) {
        this(new Table(parent, style));
    }

    /**
	 * Creates a table viewer on the given table control. The viewer has no
	 * input, no content provider, a default label provider, no sorter, and no
	 * filters.
	 * 
	 * @param table
	 *            the table control
	 */
    public TableViewer(Table table) {
        this.table = table;
        hookControl(table);
        tableEditor = new TableEditor(table);
        initTableViewerImpl();
        initializeVirtualManager(table.getStyle());
    }

    /**
	 * Initialize the virtual manager to manage the virtual state
	 * if the table is VIRTUAL. If not use the default no-op
	 * version.
	 * @param style
	 */
    private void initializeVirtualManager(int style) {
        if ((style & SWT.VIRTUAL) == 0) return;
        virtualManager = new VirtualManager();
    }

    /**
	 * Adds the given elements to this table viewer. If this viewer does not
	 * have a sorter, the elements are added at the end in the order given;
	 * otherwise the elements are inserted at appropriate positions.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been added to the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 * 
	 * @param elements
	 *            the elements to add
	 */
    public void add(Object[] elements) {
        assertElementsNotNull(elements);
        Object[] filtered = filter(elements);
        for (int i = 0; i < filtered.length; i++) {
            Object element = filtered[i];
            int index = indexForElement(element);
            createItem(element, index);
        }
    }

    /**
	 * Create a new TableItem at index if required.
	 * @param element
	 * @param index
	 * 
	 * @since 3.1
	 */
    private void createItem(Object element, int index) {
        if (virtualManager == null) updateItem(new TableItem(getTable(), SWT.NONE, index), element); else {
            virtualManager.notVisibleAdded(element, index);
        }
    }

    /**
	 * Adds the given element to this table viewer. If this viewer does not have
	 * a sorter, the element is added at the end; otherwise the element is
	 * inserted at the appropriate position.
	 * <p>
	 * This method should be called (by the content provider) when a single
	 * element has been added to the model, in order to cause the viewer to
	 * accurately reflect the model. This method only affects the viewer, not
	 * the model. Note that there is another method for efficiently processing
	 * the simultaneous addition of multiple elements.
	 * </p>
	 * 
	 * @param element
	 *            the element to add
	 */
    public void add(Object element) {
        add(new Object[] { element });
    }

    /**
	 * Cancels a currently active cell editor. All changes already done in the
	 * cell editor are lost.
	 */
    public void cancelEditing() {
        tableViewerImpl.cancelEditing();
    }

    protected Widget doFindInputItem(Object element) {
        if (equals(element, getRoot())) return getTable();
        return null;
    }

    protected Widget doFindItem(Object element) {
        TableItem[] children = table.getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            Object data = item.getData();
            if (data != null && equals(data, element)) return item;
        }
        return null;
    }

    protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
        if (widget instanceof TableItem) {
            final TableItem item = (TableItem) widget;
            if (fullMap) {
                associate(element, item);
            } else {
                item.setData(element);
                mapElement(element, item);
            }
            IBaseLabelProvider prov = getLabelProvider();
            ITableLabelProvider tprov = null;
            ILabelProvider lprov = null;
            IViewerLabelProvider vprov = null;
            if (prov instanceof ILabelProvider) lprov = (ILabelProvider) prov;
            if (prov instanceof IViewerLabelProvider) {
                vprov = (IViewerLabelProvider) prov;
            }
            if (prov instanceof ITableLabelProvider) {
                tprov = (ITableLabelProvider) prov;
            }
            int columnCount = table.getColumnCount();
            TableItem ti = item;
            getColorAndFontCollector().setFontsAndColors(element);
            for (int column = 0; column < columnCount || column == 0; column++) {
                String text = "";
                Image image = null;
                tableColorAndFont.setFontsAndColors(ti, element, column);
                if (tprov == null) {
                    if (column == 0) {
                        ViewerLabel updateLabel = new ViewerLabel(item.getText(), item.getImage());
                        if (vprov != null) buildLabel(updateLabel, element, vprov); else {
                            if (lprov != null) buildLabel(updateLabel, element, lprov);
                        }
                        if (item.isDisposed()) {
                            unmapElement(element);
                            return;
                        }
                        text = updateLabel.getText();
                        image = updateLabel.getImage();
                    }
                } else {
                    text = tprov.getColumnText(element, column);
                    image = tprov.getColumnImage(element, column);
                }
                if (text == null) text = "";
                ti.setText(column, text);
                if (ti.getImage(column) != image) {
                    ti.setImage(column, image);
                }
            }
            getColorAndFontCollector().applyFontsAndColors(ti);
        }
    }

    /**
	 * Starts editing the given element.
	 * 
	 * @param element
	 *            the element
	 * @param column
	 *            the column number
	 */
    public void editElement(Object element, int column) {
        tableViewerImpl.editElement(element, column);
    }

    /**
	 * Returns the cell editors of this table viewer.
	 * 
	 * @return the list of cell editors
	 */
    public CellEditor[] getCellEditors() {
        return tableViewerImpl.getCellEditors();
    }

    /**
	 * Returns the cell modifier of this table viewer.
	 * 
	 * @return the cell modifier
	 */
    public ICellModifier getCellModifier() {
        return tableViewerImpl.getCellModifier();
    }

    /**
	 * Returns the column properties of this table viewer. The properties must
	 * correspond with the columns of the table control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * @return the list of column properties
	 */
    public Object[] getColumnProperties() {
        return tableViewerImpl.getColumnProperties();
    }

    public Control getControl() {
        return table;
    }

    /**
	 * Returns the element with the given index from this table viewer. Returns
	 * <code>null</code> if the index is out of range.
	 * <p>
	 * This method is internal to the framework.
	 * </p>
	 * 
	 * @param index
	 *            the zero-based index
	 * @return the element at the given index, or <code>null</code> if the
	 *         index is out of range
	 */
    public Object getElementAt(int index) {
        if (index >= 0 && index < table.getItemCount()) {
            TableItem i = table.getItem(index);
            if (i != null) return i.getData();
        }
        return null;
    }

    /**
	 * The table viewer implementation of this <code>Viewer</code> framework
	 * method returns the label provider, which in the case of table viewers
	 * will be an instance of either <code>ITableLabelProvider</code> or
	 * <code>ILabelProvider</code>. If it is an
	 * <code>ITableLabelProvider</code>, then it provides a separate label
	 * text and image for each column. If it is an <code>ILabelProvider</code>,
	 * then it provides only the label text and image for the first column, and
	 * any remaining columns are blank.
	 */
    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    protected List getSelectionFromWidget() {
        Widget[] items = table.getSelection();
        ArrayList list = new ArrayList(items.length);
        for (int i = 0; i < items.length; i++) {
            Widget item = items[i];
            Object e = item.getData();
            if (e != null) list.add(e);
        }
        return list;
    }

    /**
	 * Returns this table viewer's table control.
	 * 
	 * @return the table control
	 */
    public Table getTable() {
        return table;
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        Table tableControl = (Table) control;
        tableControl.addMouseListener(new MouseAdapter() {

            public void mouseDown(MouseEvent e) {
                tableViewerImpl.handleMouseDown(e);
            }
        });
    }

    protected int indexForElement(Object element) {
        ViewerSorter sorter = getSorter();
        if (sorter == null) return table.getItemCount();
        int count = table.getItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = table.getItem(mid).getData();
            int compare = sorter.compare(this, data, element);
            if (compare == 0) {
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = table.getItem(mid).getData();
                    compare = sorter.compare(this, data, element);
                }
                return mid;
            }
            if (compare < 0) min = mid + 1; else max = mid - 1;
        }
        return min;
    }

    /**
	 * Initializes the table viewer implementation.
	 */
    private void initTableViewerImpl() {
        tableViewerImpl = new TableEditorImpl(this) {

            Rectangle getBounds(Item item, int columnNumber) {
                return ((TableItem) item).getBounds(columnNumber);
            }

            int getColumnCount() {
                return getTable().getColumnCount();
            }

            Item[] getSelection() {
                return getTable().getSelection();
            }

            void setEditor(Control w, Item item, int columnNumber) {
                tableEditor.setEditor(w, (TableItem) item, columnNumber);
            }

            void setSelection(StructuredSelection selection, boolean b) {
                TableViewer.this.setSelection(selection, b);
            }

            void showSelection() {
                getTable().showSelection();
            }

            void setLayoutData(CellEditor.LayoutData layoutData) {
                tableEditor.grabHorizontal = layoutData.grabHorizontal;
                tableEditor.horizontalAlignment = layoutData.horizontalAlignment;
                tableEditor.minimumWidth = layoutData.minimumWidth;
            }

            void handleDoubleClickEvent() {
                Viewer viewer = getViewer();
                fireDoubleClick(new DoubleClickEvent(viewer, viewer.getSelection()));
                fireOpen(new OpenEvent(viewer, viewer.getSelection()));
            }
        };
    }

    protected void inputChanged(Object input, Object oldInput) {
        getControl().setRedraw(false);
        try {
            refresh();
        } finally {
            getControl().setRedraw(true);
        }
    }

    /**
	 * Inserts the given element into this table viewer at the given position.
	 * If this viewer has a sorter, the position is ignored and the element is
	 * inserted at the correct position in the sort order.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been added to the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 * 
	 * @param element
	 *            the element
	 * @param position
	 *            a 0-based position relative to the model, or -1 to indicate
	 *            the last position
	 */
    public void insert(Object element, int position) {
        tableViewerImpl.applyEditorValue();
        if (getSorter() != null || hasFilters()) {
            add(element);
            return;
        }
        if (position == -1) position = table.getItemCount();
        createItem(element, position);
    }

    protected void internalRefresh(Object element) {
        internalRefresh(element, true);
    }

    protected void internalRefresh(Object element, boolean updateLabels) {
        tableViewerImpl.applyEditorValue();
        if (element == null || equals(element, getRoot())) {
            if (virtualManager == null) internalRefreshAll(updateLabels); else {
                internalVirtualRefreshAll();
            }
        } else {
            Widget w = findItem(element);
            if (w != null) {
                updateItem(w, element);
            }
        }
    }

    /**
	 * Refresh all with virtual elements.
	 * 
	 * @since 3.1
	 */
    private void internalVirtualRefreshAll() {
        Object root = getRoot();
        IContentProvider contentProvider = getContentProvider();
        if (!(contentProvider instanceof ILazyContentProvider) && (contentProvider instanceof IStructuredContentProvider)) {
            if (root != null) virtualManager.cachedElements = ((IStructuredContentProvider) getContentProvider()).getElements(root);
        }
        getTable().clearAll();
    }

    /**
	 * Refresh all of the elements of the table. update the
	 * labels if updatLabels is true;
	 * @param updateLabels
	 * 
	 * @since 3.1
	 */
    private void internalRefreshAll(boolean updateLabels) {
        Object[] children = getSortedChildren(getRoot());
        TableItem[] items = getTable().getItems();
        int min = Math.min(children.length, items.length);
        for (int i = 0; i < min; ++i) {
            TableItem item = items[i];
            if (equals(children[i], item.getData())) {
                if (updateLabels) {
                    updateItem(item, children[i]);
                } else {
                    associate(children[i], item);
                }
            } else {
                item.setText("");
                item.setImage(new Image[Math.max(1, table.getColumnCount())]);
                disassociate(item);
            }
        }
        if (min < items.length) {
            for (int i = items.length; --i >= min; ) {
                disassociate(items[i]);
            }
            table.remove(min, items.length - 1);
        }
        if (table.getItemCount() == 0) {
            table.removeAll();
        }
        for (int i = 0; i < min; ++i) {
            TableItem item = items[i];
            if (item.getData() == null) updateItem(item, children[i]);
        }
        for (int i = min; i < children.length; ++i) {
            createItem(children[i], i);
        }
    }

    /**
	 * Removes the given elements from this table viewer.
	 * 
	 * @param elements
	 *            the elements to remove
	 */
    private void internalRemove(final Object[] elements) {
        Object input = getInput();
        for (int i = 0; i < elements.length; ++i) {
            if (equals(elements[i], input)) {
                setInput(null);
                return;
            }
        }
        int[] indices = new int[elements.length];
        int count = 0;
        for (int i = 0; i < elements.length; ++i) {
            Widget w = findItem(elements[i]);
            if (w instanceof TableItem) {
                TableItem item = (TableItem) w;
                disassociate(item);
                indices[count++] = table.indexOf(item);
            }
        }
        if (count < indices.length) {
            System.arraycopy(indices, 0, indices = new int[count], 0, count);
        }
        table.remove(indices);
        if (table.getItemCount() == 0) {
            table.removeAll();
        }
    }

    /**
	 * Returns whether there is an active cell editor.
	 * 
	 * @return <code>true</code> if there is an active cell editor, and
	 *         <code>false</code> otherwise
	 */
    public boolean isCellEditorActive() {
        return tableViewerImpl.isCellEditorActive();
    }

    /**
	 * Removes the given elements from this table viewer. The selection is
	 * updated if required.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been removed from the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 * 
	 * @param elements
	 *            the elements to remove
	 */
    public void remove(final Object[] elements) {
        assertElementsNotNull(elements);
        preservingSelection(new Runnable() {

            public void run() {
                internalRemove(elements);
            }
        });
    }

    /**
	 * Removes the given element from this table viewer. The selection is
	 * updated if necessary.
	 * <p>
	 * This method should be called (by the content provider) when a single
	 * element has been removed from the model, in order to cause the viewer to
	 * accurately reflect the model. This method only affects the viewer, not
	 * the model. Note that there is another method for efficiently processing
	 * the simultaneous removal of multiple elements.
	 * </p>
	 * 
	 * @param element
	 *            the element
	 */
    public void remove(Object element) {
        remove(new Object[] { element });
    }

    public void reveal(Object element) {
        Assert.isNotNull(element);
        Widget w = findItem(element);
        if (w instanceof TableItem) getTable().showItem((TableItem) w);
    }

    /**
	 * Sets the cell editors of this table viewer.
	 * 
	 * @param editors
	 *            the list of cell editors
	 */
    public void setCellEditors(CellEditor[] editors) {
        tableViewerImpl.setCellEditors(editors);
    }

    /**
	 * Sets the cell modifier of this table viewer.
	 * 
	 * @param modifier
	 *            the cell modifier
	 */
    public void setCellModifier(ICellModifier modifier) {
        tableViewerImpl.setCellModifier(modifier);
    }

    /**
	 * Sets the column properties of this table viewer. The properties must
	 * correspond with the columns of the table control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * @param columnProperties
	 *            the list of column properties
	 */
    public void setColumnProperties(String[] columnProperties) {
        tableViewerImpl.setColumnProperties(columnProperties);
    }

    /**
	 * The table viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of either
	 * <code>ITableLabelProvider</code> or <code>ILabelProvider</code>. If
	 * it is an <code>ITableLabelProvider</code>, then it provides a separate
	 * label text and image for each column. If it is an
	 * <code>ILabelProvider</code>, then it provides only the label text and
	 * image for the first column, and any remaining columns are blank.
	 */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ITableLabelProvider || labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
        if (labelProvider instanceof ITableFontProvider || labelProvider instanceof ITableColorProvider) tableColorAndFont = new TableColorAndFontCollector(labelProvider); else tableColorAndFont = new TableColorAndFontNoOp();
    }

    protected void setSelectionToWidget(List list, boolean reveal) {
        if (list == null) {
            table.deselectAll();
            return;
        }
        int size = list.size();
        TableItem[] items = new TableItem[size];
        TableItem firstItem = null;
        int count = 0;
        for (int i = 0; i < size; ++i) {
            Object o = list.get(i);
            Widget w = findItem(o);
            if (w instanceof TableItem) {
                TableItem item = (TableItem) w;
                items[count++] = item;
                if (firstItem == null) firstItem = item;
            }
        }
        if (count < size) {
            System.arraycopy(items, 0, items = new TableItem[count], 0, count);
        }
        table.setSelection(items);
        if (reveal && firstItem != null) {
            table.showItem(firstItem);
        }
    }

    /**
	 * Set the item count of the receiver.
	 * @param count the new table size.
	 * 
	 * @since 3.1
	 */
    public void setItemCount(int count) {
        getTable().setItemCount(count);
        getTable().redraw();
    }

    /**
	 * Replace the entries starting at index with elements.
	 * This method assumes all of these values are correct
	 * and will not call the content provider to verify.
	 * <strong>Note that this method will create a TableItem
	 * for all of the elements provided</strong>.
	 * @param element
	 * @param index
	 * @see ILazyContentProvider
	 * 
	 * @since 3.1
	 */
    public void replace(Object element, int index) {
        TableItem item = getTable().getItem(index);
        refreshItem(item, element);
    }

    /**
	 * Clear the table item at the specified index
	 * @param index the index of the table item to be cleared
	 * 
	 * @since 3.1
	 */
    public void clear(int index) {
        TableItem item = getTable().getItem(index);
        if (item.getData() != null) {
            disassociate(item);
        }
        table.clear(index);
    }

    protected Object[] getRawChildren(Object parent) {
        Assert.isTrue(!(getContentProvider() instanceof ILazyContentProvider), "Cannot get raw children with an ILazyContentProvider");
        return super.getRawChildren(parent);
    }

    protected void assertContentProviderType(IContentProvider provider) {
        Assert.isTrue(provider instanceof IStructuredContentProvider || provider instanceof ILazyContentProvider);
    }
}
