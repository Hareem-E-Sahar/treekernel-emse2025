package org.ufacekit.ui.swing.jface.viewers.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.ufacekit.ui.swing.jface.viewers.internal.swt.SWT;
import org.ufacekit.ui.swing.jface.viewers.internal.swt.widgets.Control;
import org.ufacekit.ui.swing.jface.viewers.internal.swt.widgets.Item;
import org.ufacekit.ui.swing.jface.viewers.internal.swt.widgets.Table;
import org.ufacekit.ui.swing.jface.viewers.internal.swt.widgets.Widget;
import org.ufacekit.ui.viewers.IViewer;
import org.ufacekit.ui.viewers.ViewerComparator;

/**
 * This is a widget independent class implementors of {@link Table} like widgets
 * can use to provide a viewer on top of their widget implementations.
 *
 * @param <ModelElement>
 *            the model element displayed in the viewer
 * @param <Input>
 *            the input passed to the viewer
 * @since 3.3
 */
public abstract class AbstractTableViewer<ModelElement, Input> extends ColumnViewer<ModelElement, Input> {

    /**
	 * Create the new viewer for table like widgets
	 *
	 * @param viewer
	 */
    @SuppressWarnings("unchecked")
    public AbstractTableViewer(IViewer viewer) {
        super(viewer);
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
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
        if (checkBusy()) return;
        Object[] filtered = filter(elements);
        for (int i = 0; i < filtered.length; i++) {
            Object element = filtered[i];
            int index = indexForElement(element);
            createItem(element, index);
        }
    }

    /**
	 * Create a new TableItem at index if required.
	 *
	 * @param element
	 * @param index
	 *
	 * @since 3.1
	 */
    private void createItem(Object element, int index) {
        updateItem(internalCreateNewRowPart(SWT.NONE, index).getItem(), element);
    }

    /**
	 * Create a new row. Callers can only use the returned object locally and
	 * before making the next call on the viewer since it may be re-used for
	 * subsequent method calls.
	 *
	 * @param style
	 *            the style for the new row
	 * @param rowIndex
	 *            the index of the row or -1 if the row is appended at the end
	 * @return the newly created row
	 */
    @SuppressWarnings("unchecked")
    protected abstract ViewerRow internalCreateNewRowPart(int style, int rowIndex);

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

    protected Widget doFindInputItem(Object element) {
        if (equals(element, getRoot())) {
            return getControl();
        }
        return null;
    }

    protected Widget doFindItem(Object element) {
        Item[] children = doGetItems();
        for (int i = 0; i < children.length; i++) {
            Item item = children[i];
            Object data = item.getData();
            if (data != null && equals(data, element)) {
                return item;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
        boolean oldBusy = isBusy();
        setBusy(true);
        try {
            if (widget instanceof Item) {
                final Item item = (Item) widget;
                if (fullMap) {
                    associate(element, item);
                } else {
                    Object data = item.getData();
                    if (data != null) {
                        unmapElement(data, item);
                    }
                    item.setData(element);
                    mapElement(element, item);
                }
                int columnCount = doGetColumnCount();
                if (columnCount == 0) columnCount = 1;
                ViewerRow viewerRowFromItem = getViewerRowFromItem(item);
                for (int column = 0; column < columnCount || column == 0; column++) {
                    ViewerColumn<ModelElement> columnViewer = getViewerColumn(column);
                    ViewerCell cellToUpdate = updateCell(viewerRowFromItem, column, element);
                    columnViewer.refresh(cellToUpdate);
                    updateCell(null, 0, null);
                    if (item.isDisposed()) {
                        unmapElement(element, item);
                        return;
                    }
                }
            }
        } finally {
            setBusy(oldBusy);
        }
    }

    protected Widget getColumnViewerOwner(int columnIndex) {
        int columnCount = doGetColumnCount();
        if (columnIndex < 0 || (columnIndex > 0 && columnIndex >= columnCount)) {
            return null;
        }
        if (columnCount == 0) return getControl();
        return doGetColumn(columnIndex);
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
	 * @return the element at the given index, or <code>null</code> if the index
	 *         is out of range
	 */
    public Object getElementAt(int index) {
        if (index >= 0 && index < doGetItemCount()) {
            Item i = doGetItem(index);
            if (i != null) {
                return i.getData();
            }
        }
        return null;
    }

    /**
	 * The table viewer implementation of this <code>Viewer</code> framework
	 * method returns the label provider, which in the case of table viewers
	 * will be an instance of either <code>ITableLabelProvider</code> or
	 * <code>ILabelProvider</code>. If it is an <code>ITableLabelProvider</code>
	 * , then it provides a separate label text and image for each column. If it
	 * is an <code>ILabelProvider</code>, then it provides only the label text
	 * and image for the first column, and any remaining columns are blank.
	 */
    @SuppressWarnings("unchecked")
    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    @SuppressWarnings("unchecked")
    protected List getSelectionFromWidget() {
        Widget[] items = doGetSelection();
        ArrayList list = new ArrayList(items.length);
        for (int i = 0; i < items.length; i++) {
            Widget item = items[i];
            Object e = item.getData();
            if (e != null) {
                list.add(e);
            }
        }
        return list;
    }

    /**
	 * @param element
	 *            the element to insert
	 * @return the index where the item should be inserted.
	 */
    @SuppressWarnings("unchecked")
    protected int indexForElement(Object element) {
        ViewerComparator comparator = getComparator();
        if (comparator == null) {
            return doGetItemCount();
        }
        int count = doGetItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = doGetItem(mid).getData();
            int compare = comparator.compare(this.getPublicViewer(), data, element);
            if (compare == 0) {
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = doGetItem(mid).getData();
                    compare = comparator.compare(this.getPublicViewer(), data, element);
                }
                return mid;
            }
            if (compare < 0) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }
        return min;
    }

    protected void inputChanged(Object input, Object oldInput) {
        getControl().setRedraw(false);
        try {
            preservingSelection(new Runnable() {

                public void run() {
                    internalRefresh(getRoot());
                }
            });
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
        if (getComparator() != null || hasFilters()) {
            add(element);
            return;
        }
        if (position == -1) {
            position = doGetItemCount();
        }
        if (checkBusy()) return;
        createItem(element, position);
    }

    protected void internalRefresh(Object element) {
        internalRefresh(element, true);
    }

    protected void internalRefresh(Object element, boolean updateLabels) {
        if (element == null || equals(element, getRoot())) {
            internalRefreshAll(updateLabels);
        } else {
            Widget w = findItem(element);
            if (w != null) {
                updateItem(w, element);
            }
        }
    }

    /**
	 * Refresh all of the elements of the table. update the labels if
	 * updatLabels is true;
	 *
	 * @param updateLabels
	 *
	 * @since 3.1
	 */
    private void internalRefreshAll(boolean updateLabels) {
        Object[] children = getSortedChildren(getRoot());
        Item[] items = doGetItems();
        int min = Math.min(children.length, items.length);
        for (int i = 0; i < min; ++i) {
            Item item = items[i];
            if (equals(children[i], item.getData())) {
                if (updateLabels) {
                    updateItem(item, children[i]);
                } else {
                    associate(children[i], item);
                }
            } else {
                disassociate(item);
            }
        }
        if (min < items.length) {
            for (int i = items.length; --i >= min; ) {
                disassociate(items[i]);
            }
            doRemove(min, items.length - 1);
        }
        if (doGetItemCount() == 0) {
            doRemoveAll();
        }
        for (int i = 0; i < min; ++i) {
            Item item = items[i];
            if (item.getData() == null) {
                updateItem(item, children[i]);
            }
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
                boolean oldBusy = isBusy();
                setBusy(false);
                try {
                    setInput(null);
                } finally {
                    setBusy(oldBusy);
                }
                return;
            }
        }
        int[] indices = new int[elements.length];
        int count = 0;
        for (int i = 0; i < elements.length; ++i) {
            Widget w = findItem(elements[i]);
            if (w instanceof Item) {
                Item item = (Item) w;
                disassociate(item);
                indices[count++] = doIndexOf(item);
            }
        }
        if (count < indices.length) {
            System.arraycopy(indices, 0, indices = new int[count], 0, count);
        }
        doRemove(indices);
        if (doGetItemCount() == 0) {
            doRemoveAll();
        }
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
        if (checkBusy()) return;
        if (elements.length == 0) {
            return;
        }
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
	 * <strong>NOTE:</strong> removing an object from a virtual table will
	 * decrement the itemCount.
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
        if (w instanceof Item) {
            doShowItem((Item) w);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setSelectionToWidget(List list, boolean reveal) {
        if (list == null) {
            doDeselectAll();
            return;
        }
        if (reveal) {
            int size = list.size();
            Item[] items = new Item[size];
            int count = 0;
            for (int i = 0; i < size; ++i) {
                Object o = list.get(i);
                Widget w = findItem(o);
                if (w instanceof Item) {
                    Item item = (Item) w;
                    items[count++] = item;
                }
            }
            if (count < size) {
                System.arraycopy(items, 0, items = new Item[count], 0, count);
            }
            doSetSelection(items);
        } else {
            doDeselectAll();
            if (!list.isEmpty()) {
                int[] indices = new int[list.size()];
                Iterator it = list.iterator();
                Item[] items = doGetItems();
                Object modelElement;
                int count = 0;
                while (it.hasNext()) {
                    modelElement = it.next();
                    boolean found = false;
                    for (int i = 0; i < items.length && !found; i++) {
                        if (equals(modelElement, items[i].getData())) {
                            indices[count++] = i;
                            found = true;
                        }
                    }
                }
                if (count < indices.length) {
                    System.arraycopy(indices, 0, indices = new int[count], 0, count);
                }
                doSelect(indices);
            }
        }
    }

    /**
	 * Replace the element at the given index with the given element. This
	 * method will not call the content provider to verify. <strong>Note that
	 * this method will materialize a TableItem the given index.</strong>.
	 *
	 * @param element
	 * @param index
	 *
	 * @since 3.1
	 */
    public void replace(Object element, int index) {
        if (checkBusy()) return;
        Item item = doGetItem(index);
        refreshItem(item, element);
    }

    protected Object[] getRawChildren(Object parent) {
        return super.getRawChildren(parent);
    }

    protected void assertContentProviderType(IContentProvider provider) {
        Assert.isTrue(provider instanceof IStructuredContentProvider);
    }

    /**
	 * Searches the receiver's list starting at the first item (index 0) until
	 * an item is found that is equal to the argument, and returns the index of
	 * that item. If no item is found, returns -1.
	 *
	 * @param item
	 *            the search item
	 * @return the index of the item
	 *
	 * @since 3.3
	 */
    protected abstract int doIndexOf(Item item);

    /**
	 * Returns the number of items contained in the receiver.
	 *
	 * @return the number of items
	 *
	 * @since 3.3
	 */
    protected abstract int doGetItemCount();

    /**
	 * Returns a (possibly empty) array of TableItems which are the items in the
	 * receiver.
	 *
	 * @return the items in the receiver
	 *
	 * @since 3.3
	 */
    protected abstract Item[] doGetItems();

    /**
	 * Returns the column at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range. Columns are returned in
	 * the order that they were created. If no TableColumns were created by the
	 * programmer, this method will throw ERROR_INVALID_RANGE despite the fact
	 * that a single column of data may be visible in the table. This occurs
	 * when the programmer uses the table like a list, adding items but never
	 * creating a column.
	 *
	 * @param index
	 *            the index of the column to return
	 * @return the column at the given index
	 * @exception IllegalArgumentException
	 *                - if the index is not between 0 and the number of elements
	 *                in the list minus 1 (inclusive)
	 *
	 * @since 3.3
	 */
    protected abstract Widget doGetColumn(int index);

    /**
	 * Returns the item at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 *
	 * @param index
	 *            the index of the item to return
	 * @return the item at the given index
	 * @exception IllegalArgumentException
	 *                - if the index is not between 0 and the number of elements
	 *                in the list minus 1 (inclusive)
	 *
	 * @since 3.3
	 */
    protected abstract Item doGetItem(int index);

    /**
	 * Returns an array of {@link Item} that are currently selected in the
	 * receiver. The order of the items is unspecified. An empty array indicates
	 * that no items are selected.
	 *
	 * @return an array representing the selection
	 *
	 * @since 3.3
	 */
    protected abstract Item[] doGetSelection();

    /**
	 * Returns the zero-relative indices of the items which are currently
	 * selected in the receiver. The order of the indices is unspecified. The
	 * array is empty if no items are selected.
	 *
	 * @return an array representing the selection
	 *
	 * @since 3.3
	 */
    protected abstract int[] doGetSelectionIndices();

    /**
	 * Resets the given item in the receiver. The text, icon and other
	 * attributes of the item are set to their default values.
	 *
	 * @param item
	 *            the item to reset
	 *
	 * @since 3.3
	 */
    protected abstract void doResetItem(Item item);

    /**
	 * Removes the items from the receiver which are between the given
	 * zero-relative start and end indices (inclusive).
	 *
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 *
	 * @exception IllegalArgumentException
	 *                - if either the start or end are not between 0 and the
	 *                number of elements in the list minus 1 (inclusive)
	 *
	 * @since 3.3
	 */
    protected abstract void doRemove(int start, int end);

    /**
	 * Removes all of the items from the receiver.
	 *
	 * @since 3.3
	 */
    protected abstract void doRemoveAll();

    /**
	 * Removes the items from the receiver's list at the given zero-relative
	 * indices.
	 *
	 * @param indices
	 *            the array of indices of the items
	 *
	 * @exception IllegalArgumentException
	 *                - if the array is null, or if any of the indices is not
	 *                between 0 and the number of elements in the list minus 1
	 *                (inclusive)
	 *
	 * @since 3.3
	 */
    protected abstract void doRemove(int[] indices);

    /**
	 * Shows the item. If the item is already showing in the receiver, this
	 * method simply returns. Otherwise, the items are scrolled until the item
	 * is visible.
	 *
	 * @param item
	 *            the item to be shown
	 *
	 * @exception IllegalArgumentException
	 *                - if the item is null
	 *
	 * @since 3.3
	 */
    protected abstract void doShowItem(Item item);

    /**
	 * Deselects all selected items in the receiver.
	 *
	 * @since 3.3
	 */
    protected abstract void doDeselectAll();

    /**
	 * Sets the receiver's selection to be the given array of items. The current
	 * selection is cleared before the new items are selected.
	 * <p>
	 * Items that are not in the receiver are ignored. If the receiver is
	 * single-select and multiple items are specified, then all items are
	 * ignored.
	 * </p>
	 *
	 * @param items
	 *            the array of items
	 *
	 * @exception IllegalArgumentException
	 *                - if the array of items is null
	 *
	 * @since 3.3
	 */
    protected abstract void doSetSelection(Item[] items);

    /**
	 * Shows the selection. If the selection is already showing in the receiver,
	 * this method simply returns. Otherwise, the items are scrolled until the
	 * selection is visible.
	 *
	 * @since 3.3
	 */
    protected abstract void doShowSelection();

    /**
	 * Selects the items at the given zero-relative indices in the receiver. The
	 * current selection is cleared before the new items are selected.
	 * <p>
	 * Indices that are out of range and duplicate indices are ignored. If the
	 * receiver is single-select and multiple indices are specified, then all
	 * indices are ignored.
	 * </p>
	 *
	 * @param indices
	 *            the indices of the items to select
	 *
	 * @exception IllegalArgumentException
	 *                - if the array of indices is null
	 *
	 * @since 3.3
	 */
    protected abstract void doSetSelection(int[] indices);

    /**
	 * Selects the items at the given zero-relative indices in the receiver. The
	 * current selection is not cleared before the new items are selected.
	 * <p>
	 * If the item at a given index is not selected, it is selected. If the item
	 * at a given index was already selected, it remains selected. Indices that
	 * are out of range and duplicate indices are ignored. If the receiver is
	 * single-select and multiple indices are specified, then all indices are
	 * ignored.
	 * </p>
	 *
	 * @param indices
	 *            the array of indices for the items to select
	 *
	 * @exception IllegalArgumentException
	 *                - if the array of indices is null
	 *
	 */
    protected abstract void doSelect(int[] indices);
}
