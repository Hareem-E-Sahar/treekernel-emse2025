package net.taylor.mda.properties.views;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.StringMatcher;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;

/**
 * Shows a list of Ecore resources to the user with a text entry field
 * for a string pattern used to filter the list of resources. The Ecore
 * resources are identified as all contents in a ResourceSet that have
 * a required Ecore base class.
 * <p>
 */
public class TypeListSelectionDialog extends SelectionDialog {

    protected String ELEMENT_SELECTION_TITLE = " Selector";

    protected String ELEMENT_SELECTION_LABEL = "Select a type (? - any character, * = any string):";

    protected String ELEMENT_SELECTION_MATCHING = "Matching types:";

    protected String ELEMENT_SELECTION_PATH = "In packages:";

    Text pattern;

    Table resourceNames;

    Table folderNames;

    String patternString;

    List list;

    static Collator collator = Collator.getInstance();

    boolean gatherResourcesDynamically = true;

    StringMatcher stringMatcher;

    UpdateFilterThread updateFilterThread;

    UpdateGatherThread updateGatherThread;

    ResourceDescriptor[] descriptors;

    int descriptorsSize;

    ILabelProvider labelProvider;

    boolean okEnabled = false;

    static class ResourceDescriptor implements Comparable {

        String label;

        ArrayList resources = new ArrayList();

        boolean resourcesSorted = true;

        public int compareTo(Object o) {
            return collator.compare(label, ((ResourceDescriptor) o).label);
        }
    }

    class UpdateFilterThread extends Thread {

        boolean stop = false;

        int firstMatch = 0;

        int lastMatch = descriptorsSize - 1;

        public void run() {
            Display display = resourceNames.getDisplay();
            final int itemIndex[] = { 0 };
            final int itemCount[] = { 0 };
            final boolean[] disposed = { false };
            display.syncExec(new Runnable() {

                public void run() {
                    if (resourceNames.isDisposed()) {
                        disposed[0] = true;
                        return;
                    }
                    itemCount[0] = resourceNames.getItemCount();
                }
            });
            if (disposed[0]) return;
            int last;
            if ((patternString.indexOf('?') == -1) && (patternString.endsWith("*")) && (patternString.indexOf('*') == patternString.length() - 1)) {
                firstMatch = getFirstMatch();
                if (firstMatch == -1) {
                    firstMatch = 0;
                    lastMatch = -1;
                } else {
                    lastMatch = getLastMatch();
                }
                last = lastMatch;
                for (int i = firstMatch; i <= lastMatch; i++) {
                    if (i % 50 == 0) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (stop || resourceNames.isDisposed()) {
                        disposed[0] = true;
                        return;
                    }
                    final int index = i;
                    display.syncExec(new Runnable() {

                        public void run() {
                            if (stop || resourceNames.isDisposed()) return;
                            updateItem(index, itemIndex[0], itemCount[0]);
                            itemIndex[0]++;
                        }
                    });
                }
            } else {
                last = lastMatch;
                boolean setFirstMatch = true;
                for (int i = firstMatch; i <= lastMatch; i++) {
                    if (i % 50 == 0) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (stop || resourceNames.isDisposed()) {
                        disposed[0] = true;
                        return;
                    }
                    final int index = i;
                    if (match(descriptors[index].label)) {
                        if (setFirstMatch) {
                            setFirstMatch = false;
                            firstMatch = index;
                        }
                        last = index;
                        display.syncExec(new Runnable() {

                            public void run() {
                                if (stop || resourceNames.isDisposed()) return;
                                updateItem(index, itemIndex[0], itemCount[0]);
                                itemIndex[0]++;
                            }
                        });
                    }
                }
            }
            if (disposed[0]) return;
            lastMatch = last;
            display.syncExec(new Runnable() {

                public void run() {
                    if (resourceNames.isDisposed()) return;
                    itemCount[0] = resourceNames.getItemCount();
                    if (itemIndex[0] < itemCount[0]) {
                        resourceNames.setRedraw(false);
                        resourceNames.remove(itemIndex[0], itemCount[0] - 1);
                        resourceNames.setRedraw(true);
                    }
                    if (resourceNames.getItemCount() == 0) {
                        folderNames.removeAll();
                        updateOKState(false);
                    }
                }
            });
        }
    }

    class UpdateGatherThread extends Thread {

        boolean stop = false;

        int lastMatch = -1;

        int firstMatch = 0;

        boolean refilter = false;

        public void run() {
            Display display = resourceNames.getDisplay();
            final int itemIndex[] = { 0 };
            final int itemCount[] = { 0 };
            final boolean[] disposed = { false };
            display.syncExec(new Runnable() {

                public void run() {
                    if (resourceNames.isDisposed()) {
                        disposed[0] = true;
                        return;
                    }
                    itemCount[0] = resourceNames.getItemCount();
                }
            });
            if (disposed[0]) {
                return;
            }
            if (!refilter) {
                for (int i = 0; i <= lastMatch; i++) {
                    if (i % 50 == 0) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (stop || resourceNames.isDisposed()) {
                        disposed[0] = true;
                        return;
                    }
                    final int index = i;
                    display.syncExec(new Runnable() {

                        public void run() {
                            if (stop || resourceNames.isDisposed()) return;
                            updateItem(index, itemIndex[0], itemCount[0]);
                            itemIndex[0]++;
                        }
                    });
                }
            } else {
                for (int i = firstMatch; i <= lastMatch; i++) {
                    if (i % 50 == 0) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (stop || resourceNames.isDisposed()) {
                        disposed[0] = true;
                        return;
                    }
                    final int index = i;
                    if (match(descriptors[index].label)) {
                        display.syncExec(new Runnable() {

                            public void run() {
                                if (stop || resourceNames.isDisposed()) return;
                                updateItem(index, itemIndex[0], itemCount[0]);
                                itemIndex[0]++;
                            }
                        });
                    }
                }
            }
            if (disposed[0]) {
                return;
            }
            display.syncExec(new Runnable() {

                public void run() {
                    if (resourceNames.isDisposed()) {
                        return;
                    }
                    itemCount[0] = resourceNames.getItemCount();
                    if (itemIndex[0] < itemCount[0]) {
                        resourceNames.setRedraw(false);
                        resourceNames.remove(itemIndex[0], itemCount[0] - 1);
                        resourceNames.setRedraw(true);
                    }
                    if (resourceNames.getItemCount() == 0) {
                        folderNames.removeAll();
                        updateOKState(false);
                    }
                }
            });
        }
    }

    /**
     * Creates a new instance of the class.  When this constructor is used to
     * create the dialog, resources will be gathered dynamically as the pattern
     * string is specified.  Only resources of the given types that match the 
     * pattern string will be listed.  To further filter the matching resources,
     * @see #select(ENamedElement)
     * 
     * @param parentShell shell to parent the dialog on
     * @param container container to get resources from
     * @param typeMask mask containing ENamedElement types to be considered
     */
    public TypeListSelectionDialog(Shell parentShell, List list, ILabelProvider labelProvider) {
        super(parentShell);
        this.list = list;
        this.labelProvider = labelProvider;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        setTitle(ELEMENT_SELECTION_TITLE);
    }

    /**
     * Adjust the pattern string for matching.
     */
    protected String adjustPattern() {
        String text = pattern.getText().trim();
        if (text.endsWith("<")) {
            return text.substring(0, text.length() - 1);
        }
        if (!text.equals("") && !text.endsWith("*")) {
            return text + "*";
        }
        return text;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    protected void cancelPressed() {
        setResult(null);
        super.cancelPressed();
    }

    /**
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        boolean result = super.close();
        labelProvider.dispose();
        return result;
    }

    /**
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create() {
        super.create();
        pattern.setFocus();
        getButton(IDialogConstants.OK_ID).setEnabled(okEnabled);
    }

    /**
     * Creates the contents of this dialog, initializes the
     * listener and the update thread.
     * 
     * @param parent parent to create the dialog widgets in
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Label l = new Label(dialogArea, SWT.NONE);
        l.setText(ELEMENT_SELECTION_LABEL);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(data);
        pattern = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
        pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        l = new Label(dialogArea, SWT.NONE);
        l.setText(ELEMENT_SELECTION_MATCHING);
        data = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(data);
        resourceNames = new Table(dialogArea, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 12 * resourceNames.getItemHeight();
        resourceNames.setLayoutData(data);
        l = new Label(dialogArea, SWT.NONE);
        l.setText(ELEMENT_SELECTION_PATH);
        data = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(data);
        folderNames = new Table(dialogArea, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 300;
        data.heightHint = 4 * folderNames.getItemHeight();
        folderNames.setLayoutData(data);
        if (gatherResourcesDynamically) {
            updateGatherThread = new UpdateGatherThread();
        } else {
            updateFilterThread = new UpdateFilterThread();
        }
        pattern.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN) resourceNames.setFocus();
            }
        });
        pattern.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                refresh(false);
            }
        });
        resourceNames.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                updateFolders((ResourceDescriptor) e.item.getData());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                okPressed();
            }
        });
        folderNames.addSelectionListener(new SelectionAdapter() {

            public void widgetDefaultSelected(SelectionEvent e) {
                okPressed();
            }
        });
        applyDialogFont(dialogArea);
        return dialogArea;
    }

    /**
     */
    private void filterResources(boolean force) {
        String oldPattern = force ? null : patternString;
        patternString = adjustPattern();
        if (!force && patternString.equals(oldPattern)) return;
        updateFilterThread.stop = true;
        stringMatcher = new StringMatcher(patternString, true, false);
        UpdateFilterThread oldThread = updateFilterThread;
        updateFilterThread = new UpdateFilterThread();
        if (patternString.equals("")) {
            updateFilterThread.firstMatch = 0;
            updateFilterThread.lastMatch = -1;
            updateFilterThread.start();
            return;
        }
        if (oldPattern != null && (oldPattern.length() != 0) && oldPattern.endsWith("*") && patternString.endsWith("*")) {
            int matchLength = oldPattern.length() - 1;
            if (patternString.regionMatches(0, oldPattern, 0, matchLength)) {
                updateFilterThread.firstMatch = oldThread.firstMatch;
                updateFilterThread.lastMatch = oldThread.lastMatch;
                updateFilterThread.start();
                return;
            }
        }
        updateFilterThread.firstMatch = 0;
        updateFilterThread.lastMatch = descriptorsSize - 1;
        updateFilterThread.start();
    }

    /**
     * Use a binary search to get the first match for the patternString.
     * This method assumes the patternString does not contain any '?' 
     * characters and that it contains only one '*' character at the end
     * of the string.
     */
    private int getFirstMatch() {
        int high = descriptorsSize;
        int low = -1;
        boolean match = false;
        ResourceDescriptor desc = new ResourceDescriptor();
        desc.label = patternString.substring(0, patternString.length() - 1);
        while (high - low > 1) {
            int index = (high + low) / 2;
            String label = descriptors[index].label;
            if (match(label)) {
                high = index;
                match = true;
            } else {
                int compare = descriptors[index].compareTo(desc);
                if (compare == -1) {
                    low = index;
                } else {
                    high = index;
                }
            }
        }
        if (match) return high; else return -1;
    }

    /**
     */
    private void gatherResources(boolean force) {
        String oldPattern = force ? null : patternString;
        patternString = adjustPattern();
        if (!force && patternString.equals(oldPattern)) return;
        updateGatherThread.stop = true;
        updateGatherThread = new UpdateGatherThread();
        if (patternString.equals("")) {
            updateGatherThread.start();
            return;
        }
        stringMatcher = new StringMatcher(patternString, true, false);
        if (oldPattern != null && (oldPattern.length() != 0) && oldPattern.endsWith("*") && patternString.endsWith("*")) {
            int matchLength = oldPattern.length() - 1;
            if (patternString.regionMatches(0, oldPattern, 0, matchLength)) {
                updateGatherThread.refilter = true;
                updateGatherThread.firstMatch = 0;
                updateGatherThread.lastMatch = descriptorsSize - 1;
                updateGatherThread.start();
                return;
            }
        }
        final ArrayList resources = new ArrayList();
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

            @SuppressWarnings("unchecked")
            public void run() {
                getMatchingResources(resources);
                NamedElement resourcesArray[] = new NamedElement[resources.size()];
                resources.toArray(resourcesArray);
                initDescriptors(resourcesArray);
            }
        });
        updateGatherThread.firstMatch = 0;
        updateGatherThread.lastMatch = descriptorsSize - 1;
        updateGatherThread.start();
    }

    /**
     * Return an image for a resource descriptor.
     * 
     * @param desc resource descriptor to return image for
     * @return an image for a resource descriptor.
     */
    private Image getImage(ResourceDescriptor desc) {
        NamedElement r = (NamedElement) desc.resources.get(0);
        return labelProvider.getImage(r);
    }

    /**
     * Use a binary search to get the last match for the patternString.
     * This method assumes the patternString does not contain any '?' 
     * characters and that it contains only one '*' character at the end
     * of the string.
     */
    private int getLastMatch() {
        int high = descriptorsSize;
        int low = -1;
        boolean match = false;
        ResourceDescriptor desc = new ResourceDescriptor();
        desc.label = patternString.substring(0, patternString.length() - 1);
        while (high - low > 1) {
            int index = (high + low) / 2;
            String label = descriptors[index].label;
            if (match(label)) {
                low = index;
                match = true;
            } else {
                int compare = descriptors[index].compareTo(desc);
                if (compare == -1) {
                    low = index;
                } else {
                    high = index;
                }
            }
        }
        if (match) return low; else return -1;
    }

    /**
     * Gather the resources of the specified type that match the current
     * pattern string.
     * 
     * @param resources resources that match
     */
    @SuppressWarnings("unchecked")
    private void getMatchingResources(final ArrayList resources) {
        for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            Object object = iterator.next();
            if (object != null && ((NamedElement) object).getName() != null && match(((NamedElement) object).getName())) resources.add(object);
        }
    }

    private Image getParentImage(EObject eObject) {
        EObject eContainer = eObject.eContainer();
        return labelProvider.getImage(eContainer);
    }

    public String getParentLabel(EObject eObject) {
        EObject eContainer = eObject.eContainer();
        if (!(eContainer instanceof Package)) return "";
        String parentLabel = getParentLabel(eContainer);
        if ("".equals(parentLabel)) return labelProvider.getText(eContainer); else return parentLabel + "." + labelProvider.getText(eContainer);
    }

    /**
     * Creates a ResourceDescriptor for each ENamedElement,
     * sorts them and removes the duplicated ones.
     * 
     * @param resources resources to create resource descriptors for
     */
    private void initDescriptors(final NamedElement resources[]) {
        BusyIndicator.showWhile(null, new Runnable() {

            @SuppressWarnings("unchecked")
            public void run() {
                descriptors = new ResourceDescriptor[resources.length];
                for (int i = 0; i < resources.length; i++) {
                    NamedElement r = resources[i];
                    ResourceDescriptor d = new ResourceDescriptor();
                    d.label = r.getName();
                    d.resources.add(r);
                    descriptors[i] = d;
                }
                Arrays.sort(descriptors);
                descriptorsSize = descriptors.length;
                int index = 0;
                if (descriptorsSize < 2) return;
                ResourceDescriptor current = descriptors[index];
                for (int i = 1; i < descriptorsSize; i++) {
                    if (current.resources.size() > 1) {
                        current.resourcesSorted = false;
                    }
                    descriptors[index + 1] = descriptors[i];
                    index++;
                    current = descriptors[index];
                }
                descriptorsSize = index + 1;
            }
        });
    }

    /**
     * Returns true if the label matches the chosen pattern.
     * 
     * @param label label to match with the current pattern
     * @return true if the label matches the chosen pattern. 
     * 	false otherwise.
     */
    private boolean match(String label) {
        if ((patternString == null) || (patternString.equals("")) || (patternString.equals("*"))) return true;
        return stringMatcher.match(label);
    }

    /**
     * The user has selected a resource and the dialog is closing.
     * Set the selected resource as the dialog result.
     */
    @SuppressWarnings("unchecked")
    protected void okPressed() {
        TableItem items[] = folderNames.getSelection();
        if (items.length == 1) {
            ArrayList result = new ArrayList();
            result.add(items[0].getData());
            setResult(result);
        }
        super.okPressed();
    }

    /**
     * Use this method to further filter resources.  As resources are gathered,
     * if a resource matches the current pattern string, this method will be called.
     * If this method answers false, the resource will not be included in the list
     * of matches and the resource's children will NOT be considered for matching.
     */
    protected boolean select(NamedElement resource) {
        return true;
    }

    /**
     * Refreshes the filtered list of resources.
     * Called when the text in the pattern text entry has changed.
     * 
     * @param force if <code>true</code> a refresh is forced, if <code>false</code> a refresh only
     *   occurs if the pattern has changed
     * 
     * @since 3.1
     */
    protected void refresh(boolean force) {
        if (gatherResourcesDynamically) {
            gatherResources(force);
        } else {
            filterResources(force);
        }
    }

    /**
     * A new resource has been selected. Change the contents
     * of the folder names list.
     * 
     * @desc resource descriptor of the selected resource
     */
    private void updateFolders(final ResourceDescriptor desc) {
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

            @SuppressWarnings("unchecked")
            public void run() {
                if (!desc.resourcesSorted) {
                    Collections.sort(desc.resources, new Comparator() {

                        public int compare(Object o1, Object o2) {
                            String s1 = getParentLabel((NamedElement) o1);
                            String s2 = getParentLabel((NamedElement) o2);
                            return collator.compare(s1, s2);
                        }
                    });
                    desc.resourcesSorted = true;
                }
                folderNames.removeAll();
                for (int i = 0; i < desc.resources.size(); i++) {
                    TableItem newItem = new TableItem(folderNames, SWT.NONE);
                    NamedElement r = (NamedElement) desc.resources.get(i);
                    newItem.setText(getParentLabel(r));
                    newItem.setImage(getParentImage(r));
                    newItem.setData(r);
                }
                folderNames.setSelection(0);
            }
        });
    }

    /**
     * Update the specified item with the new info from the resource 
     * descriptor.
     * Create a new table item if there is no item. 
     * 
     * @param index index of the resource descriptor
     * @param itemPos position of the existing item to update
     * @param itemCount number of items in the resources table widget
     */
    private void updateItem(int index, int itemPos, int itemCount) {
        ResourceDescriptor desc = descriptors[index];
        TableItem item;
        if (itemPos < itemCount) {
            item = resourceNames.getItem(itemPos);
            if (item.getData() != desc) {
                item.setText(desc.label);
                item.setData(desc);
                item.setImage(getImage(desc));
                if (itemPos == 0) {
                    resourceNames.setSelection(0);
                    updateFolders(desc);
                }
            }
        } else {
            item = new TableItem(resourceNames, SWT.NONE);
            item.setText(desc.label);
            item.setData(desc);
            item.setImage(getImage(desc));
            if (itemPos == 0) {
                resourceNames.setSelection(0);
                updateFolders(desc);
            }
        }
        updateOKState(true);
    }

    /**
     * Update the enabled state of the OK button.  To be called when
     * the resource list is updated.
     * @param state the new enabled state of the button
     */
    protected void updateOKState(boolean state) {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (okButton != null && !okButton.isDisposed() && state != okEnabled) {
            okButton.setEnabled(state);
            okEnabled = state;
        }
    }
}
