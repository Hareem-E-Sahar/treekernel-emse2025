package org.eclipse.bpel.common.ui.details.viewers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on an SWT <code>Combo</code> widget.
 * 
 * @see CComboViewer
 */
public class ComboViewer extends StructuredViewer {

    private org.eclipse.swt.widgets.Combo combo;

    private java.util.List comboMap = new ArrayList();

    /**
	 * Creates a combo viewer on a newly-created combo control under the given parent.
	 * The combo control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>.
	 * The viewer has no input, no content provider, a default label provider, 
	 * no sorter, and no filters.
	 *
	 * @param parent the parent control
	 */
    public ComboViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    }

    /**
	 * Creates a combo viewer on a newly-created combo control under the given parent.
	 * The combo control is created using the given SWT style bits.
	 * The viewer has no input, no content provider, a default label provider, 
	 * no sorter, and no filters.
	 *
	 * @param parent the parent control
	 * @param style the SWT style bits
	 */
    public ComboViewer(Composite parent, int style) {
        this(new org.eclipse.swt.widgets.Combo(parent, style));
    }

    /**
	 * Creates a combo viewer on the given combo control.
	 * The viewer has no input, no content provider, a default label provider, 
	 * no sorter, and no filters.
	 *
	 * @param combo the combo control
	 */
    public ComboViewer(org.eclipse.swt.widgets.Combo combo) {
        this.combo = combo;
        hookControl(combo);
    }

    /**
	 * Adds the given elements to this combo viewer.
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
        Object[] filtered = filter(elements);
        ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        for (int i = 0; i < filtered.length; i++) {
            Object element = filtered[i];
            int ix = indexForElement(element);
            combo.add(labelProvider.getText(element), ix);
            comboMap.add(ix, element);
            mapElement(element, combo);
        }
    }

    /**
	 * Adds the given element to this combo viewer.
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
        if (element != null && element.equals(getRoot())) return getCombo();
        return null;
    }

    protected Widget doFindItem(Object element) {
        if (element != null) {
            if (comboMap.contains(element)) return getCombo();
        }
        return null;
    }

    protected void doUpdateItem(Widget data, Object element, boolean fullMap) {
        if (element != null) {
            int ix = comboMap.indexOf(element);
            if (ix >= 0) {
                ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
                combo.setItem(ix, labelProvider.getText(element));
            }
        }
    }

    public Control getControl() {
        return combo;
    }

    /**
	 * Returns the element with the given index from this combo viewer.
	 * Returns <code>null</code> if the index is out of range.
	 *
	 * @param index the zero-based index
	 * @return the element at the given index, or <code>null</code> if the
	 *   index is out of range
	 */
    public Object getElementAt(int index) {
        if (index >= 0 && index < comboMap.size()) return comboMap.get(index);
        return null;
    }

    /**
	 * The combo viewer implementation of this <code>Viewer</code> framework
	 * method returns the label provider, which in the case of combo
	 * viewers will be an instance of <code>ILabelProvider</code>.
	 */
    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    /**
	 * Returns this combo viewer's combo control.
	 *
	 * @return the combo control
	 */
    public org.eclipse.swt.widgets.Combo getCombo() {
        return combo;
    }

    protected List getSelectionFromWidget() {
        int idx = getCombo().getSelectionIndex();
        if (idx < 0) return new ArrayList(0);
        ArrayList list = new ArrayList(1);
        Object e = getElementAt(idx);
        if (e != null) list.add(e);
        return list;
    }

    protected int indexForElement(Object element) {
        ViewerSorter sorter = getSorter();
        if (sorter == null) return combo.getItemCount();
        int count = combo.getItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = comboMap.get(mid);
            int compare = sorter.compare(this, data, element);
            if (compare == 0) {
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = comboMap.get(mid);
                    compare = sorter.compare(this, data, element);
                }
                return mid;
            }
            if (compare < 0) min = mid + 1; else max = mid - 1;
        }
        return min;
    }

    protected void inputChanged(Object input, Object oldInput) {
        comboMap.clear();
        Object[] children = getSortedChildren(getRoot());
        int size = children.length;
        org.eclipse.swt.widgets.Combo combo = getCombo();
        combo.removeAll();
        String[] labels = new String[size];
        for (int i = 0; i < size; i++) {
            Object el = children[i];
            labels[i] = ((ILabelProvider) getLabelProvider()).getText(el);
            comboMap.add(el);
            mapElement(el, combo);
        }
        combo.setItems(labels);
    }

    protected void internalRefresh(Object element) {
        if (element == null || element.equals(getRoot())) {
            if (comboMap != null) comboMap.clear();
            unmapAllElements();
            List selection = getSelectionFromWidget();
            combo.setRedraw(false);
            combo.removeAll();
            Object[] children = getSortedChildren(getRoot());
            ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
            for (int i = 0; i < children.length; i++) {
                Object el = children[i];
                combo.add(labelProvider.getText(el), i);
                comboMap.add(el);
                mapElement(el, combo);
            }
            combo.setRedraw(true);
            setSelectionToWidget(selection, false);
        } else {
            doUpdateItem(combo, element, true);
        }
    }

    /**
	 * Removes the given elements from this combo viewer.
	 *
	 * @param elements the elements to remove
	 */
    private void internalRemove(final Object[] elements) {
        Object input = getInput();
        for (int i = 0; i < elements.length; ++i) {
            if (elements[i].equals(input)) {
                setInput(null);
                return;
            }
            int ix = comboMap.indexOf(elements[i]);
            if (ix >= 0) {
                combo.remove(ix);
                comboMap.remove(ix);
                unmapElement(elements[i], combo);
            }
        }
    }

    /**
	 * Removes the given elements from this combo viewer.
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
        preservingSelection(new Runnable() {

            public void run() {
                internalRemove(elements);
            }
        });
    }

    /**
	 * Removes the given element from this combo viewer.
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

    public void reveal(Object element) {
    }

    /**
	 * The combo viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance
	 * of <code>ILabelProvider</code>.
	 */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
    }

    protected void setSelectionToWidget(List in, boolean reveal) {
        org.eclipse.swt.widgets.Combo combo = getCombo();
        if (in == null || in.size() == 0) {
            combo.deselectAll();
        } else {
            int n = in.size();
            int[] ixs = new int[n];
            int count = 0;
            for (int i = 0; i < n; ++i) {
                Object el = in.get(i);
                int ix = comboMap.indexOf(el);
                if (ix >= 0) ixs[count++] = ix;
            }
            if (count < n) {
                System.arraycopy(ixs, 0, ixs = new int[count], 0, count);
            }
            if (ixs.length > 0) {
                combo.select(ixs[0]);
            }
        }
    }
}
