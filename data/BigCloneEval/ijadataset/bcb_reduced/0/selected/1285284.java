package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract base class for viewers that contain lists of items (such as a combo or list).
 * Most of the viewer implementation is in this base class, except for the minimal code that
 * actually communicates with the underlying widget.
 * 
 * @see org.eclipse.jface.viewers.ListViewer
 * @see org.eclipse.jface.viewers.ComboViewer
 * 
 * @since 3.0
 */
public abstract class AbstractListViewer extends StructuredViewer {

    /**
     * A list of viewer elements (element type: <code>Object</code>).
     */
    private java.util.List listMap = new ArrayList();

    /**
     * Adds the given string to the underlying widget at the given index
     *  
     * @param string the string to add
     * @param index position to insert the string into
     */
    protected abstract void listAdd(String string, int index);

    /**
     * Sets the text of the item at the given index in the underlying widget.
     * 
     * @param index index to modify
     * @param string new text
     */
    protected abstract void listSetItem(int index, String string);

    /**
     * Returns the zero-relative indices of the items which are currently
     * selected in the underlying widget.  The array is empty if no items are selected.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its selection, so modifying the array will
     * not affect the receiver. 
     * </p>
     * @return the array of indices of the selected items
     */
    protected abstract int[] listGetSelectionIndices();

    /**
     * Returns the number of items contained in the underlying widget.
     *
     * @return the number of items
     */
    protected abstract int listGetItemCount();

    /**
     * Sets the underlying widget's items to be the given array of items.
     *
     * @param labels the array of label text
     */
    protected abstract void listSetItems(String[] labels);

    /**
     * Removes all of the items from the underlying widget.
     */
    protected abstract void listRemoveAll();

    /**
     * Removes the item from the underlying widget at the given
     * zero-relative index.
     * 
     * @param index the index for the item
     */
    protected abstract void listRemove(int index);

    /**
     * Selects the items at the given zero-relative indices in the underlying widget.
     * The current selection is cleared before the new items are selected.
     * <p>
     * Indices that are out of range and duplicate indices are ignored.
     * If the receiver is single-select and multiple indices are specified,
     * then all indices are ignored.
     *
     * @param ixs the indices of the items to select
     */
    protected abstract void listSetSelection(int[] ixs);

    /**
     * Shows the selection.  If the selection is already showing in the receiver,
     * this method simply returns.  Otherwise, the items are scrolled until
     * the selection is visible.
     */
    protected abstract void listShowSelection();

    /**
     * Deselects all selected items in the underlying widget.
     */
    protected abstract void listDeselectAll();

    /**
     * Adds the given elements to this list viewer.
     * If this viewer does not have a sorter, the elements are added at the end
     * in the order given; otherwise the elements are inserted at appropriate positions.
     * <p>
     * This method should be called (by the content provider) when elements 
     * have been added to the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * </p>
     *
     * @param elements the elements to add
     */
    public void add(Object[] elements) {
        assertElementsNotNull(elements);
        Object[] filtered = filter(elements);
        ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        for (int i = 0; i < filtered.length; i++) {
            Object element = filtered[i];
            int ix = indexForElement(element);
            listAdd(getLabelProviderText(labelProvider, element), ix);
            listMap.add(ix, element);
            mapElement(element, getControl());
        }
    }

    /**
     * Return the text for the element from the labelProvider.
     * If it is null then return the empty String.
     * @param labelProvider ILabelProvider
     * @param element
     * @return String. Return the emptyString if the labelProvider
     * returns null for the text.
     * 
     * @since 3.1
     */
    private String getLabelProviderText(ILabelProvider labelProvider, Object element) {
        String text = labelProvider.getText(element);
        if (text == null) return "";
        return text;
    }

    /**
     * Adds the given element to this list viewer.
     * If this viewer does not have a sorter, the element is added at the end;
     * otherwise the element is inserted at the appropriate position.
     * <p>
     * This method should be called (by the content provider) when a single element 
     * has been added to the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * Note that there is another method for efficiently processing the simultaneous
     * addition of multiple elements.
     * </p>
     *
     * @param element the element
     */
    public void add(Object element) {
        add(new Object[] { element });
    }

    protected Widget doFindInputItem(Object element) {
        if (element != null && equals(element, getRoot())) return getControl();
        return null;
    }

    protected Widget doFindItem(Object element) {
        if (element != null) {
            if (listMap.contains(element)) return getControl();
        }
        return null;
    }

    protected void doUpdateItem(Widget data, Object element, boolean fullMap) {
        if (element != null) {
            int ix = listMap.indexOf(element);
            if (ix >= 0) {
                ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
                listSetItem(ix, getLabelProviderText(labelProvider, element));
            }
        }
    }

    public abstract Control getControl();

    /**
     * Returns the element with the given index from this list viewer.
     * Returns <code>null</code> if the index is out of range.
     *
     * @param index the zero-based index
     * @return the element at the given index, or <code>null</code> if the
     *   index is out of range
     */
    public Object getElementAt(int index) {
        if (index >= 0 && index < listMap.size()) return listMap.get(index);
        return null;
    }

    /**
     * The list viewer implementation of this <code>Viewer</code> framework
     * method returns the label provider, which in the case of list
     * viewers will be an instance of <code>ILabelProvider</code>.
     */
    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    protected List getSelectionFromWidget() {
        int[] ixs = listGetSelectionIndices();
        ArrayList list = new ArrayList(ixs.length);
        for (int i = 0; i < ixs.length; i++) {
            Object e = getElementAt(ixs[i]);
            if (e != null) list.add(e);
        }
        return list;
    }

    protected int indexForElement(Object element) {
        ViewerSorter sorter = getSorter();
        if (sorter == null) return listGetItemCount();
        int count = listGetItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = listMap.get(mid);
            int compare = sorter.compare(this, data, element);
            if (compare == 0) {
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = listMap.get(mid);
                    compare = sorter.compare(this, data, element);
                }
                return mid;
            }
            if (compare < 0) min = mid + 1; else max = mid - 1;
        }
        return min;
    }

    protected void inputChanged(Object input, Object oldInput) {
        listMap.clear();
        Object[] children = getSortedChildren(getRoot());
        int size = children.length;
        listRemoveAll();
        String[] labels = new String[size];
        for (int i = 0; i < size; i++) {
            Object el = children[i];
            labels[i] = getLabelProviderText((ILabelProvider) getLabelProvider(), el);
            listMap.add(el);
            mapElement(el, getControl());
        }
        listSetItems(labels);
    }

    protected void internalRefresh(Object element) {
        Control list = getControl();
        if (element == null || equals(element, getRoot())) {
            if (listMap != null) listMap.clear();
            unmapAllElements();
            List selection = getSelectionFromWidget();
            list.setRedraw(false);
            listRemoveAll();
            Object[] children = getSortedChildren(getRoot());
            String[] items = new String[children.length];
            ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
            for (int i = 0; i < items.length; i++) {
                Object el = children[i];
                items[i] = getLabelProviderText(labelProvider, el);
                listMap.add(el);
                mapElement(el, list);
            }
            listSetItems(items);
            list.setRedraw(true);
            setSelectionToWidget(selection, false);
        } else {
            doUpdateItem(list, element, true);
        }
    }

    /**
     * Removes the given elements from this list viewer.
     *
     * @param elements the elements to remove
     */
    private void internalRemove(final Object[] elements) {
        Object input = getInput();
        for (int i = 0; i < elements.length; ++i) {
            if (equals(elements[i], input)) {
                setInput(null);
                return;
            }
            int ix = listMap.indexOf(elements[i]);
            if (ix >= 0) {
                listRemove(ix);
                listMap.remove(ix);
                unmapElement(elements[i], getControl());
            }
        }
    }

    /**
     * Removes the given elements from this list viewer.
     * The selection is updated if required.
     * <p>
     * This method should be called (by the content provider) when elements 
     * have been removed from the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * </p>
     *
     * @param elements the elements to remove
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
     * Removes the given element from this list viewer.
     * The selection is updated if necessary.
     * <p>
     * This method should be called (by the content provider) when a single element 
     * has been removed from the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * Note that there is another method for efficiently processing the simultaneous
     * removal of multiple elements.
     * </p>
     *
     * @param element the element
     */
    public void remove(Object element) {
        remove(new Object[] { element });
    }

    /**
     * The list viewer implementation of this <code>Viewer</code> framework
     * method ensures that the given label provider is an instance
     * of <code>ILabelProvider</code>.
     */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
    }

    protected void setSelectionToWidget(List in, boolean reveal) {
        if (in == null || in.size() == 0) {
            listDeselectAll();
        } else {
            int n = in.size();
            int[] ixs = new int[n];
            int count = 0;
            for (int i = 0; i < n; ++i) {
                Object el = in.get(i);
                int ix = listMap.indexOf(el);
                if (ix >= 0) ixs[count++] = ix;
            }
            if (count < n) {
                System.arraycopy(ixs, 0, ixs = new int[count], 0, count);
            }
            listSetSelection(ixs);
            if (reveal) {
                listShowSelection();
            }
        }
    }

    int getElementIndex(Object element) {
        return listMap.indexOf(element);
    }
}
