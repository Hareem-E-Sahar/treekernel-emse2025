package com.ecmdeveloper.plugin.search.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.EventObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.CopyTemplateAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import com.ecmdeveloper.plugin.core.util.PluginMessage;
import com.ecmdeveloper.plugin.search.Activator;
import com.ecmdeveloper.plugin.search.actions.AddTableAction;
import com.ecmdeveloper.plugin.search.actions.ConvertToTextAction;
import com.ecmdeveloper.plugin.search.actions.EditQueryComponentAction;
import com.ecmdeveloper.plugin.search.actions.ExecuteSearchAction;
import com.ecmdeveloper.plugin.search.actions.RemoveTableAction;
import com.ecmdeveloper.plugin.search.actions.SetMainQueryAction;
import com.ecmdeveloper.plugin.search.actions.SetMaxCountAction;
import com.ecmdeveloper.plugin.search.actions.SetTimeLimitAction;
import com.ecmdeveloper.plugin.search.actions.ShowPartSqlAction;
import com.ecmdeveloper.plugin.search.actions.ShowSqlAction;
import com.ecmdeveloper.plugin.search.actions.ToggleDistinctAction;
import com.ecmdeveloper.plugin.search.actions.ToggleIncludeSubclassesAction;
import com.ecmdeveloper.plugin.search.actions.ToggleSearchAllVersionsAction;
import com.ecmdeveloper.plugin.search.dnd.QueryDropTargetListener;
import com.ecmdeveloper.plugin.search.dnd.TextTransferDropTargetListener;
import com.ecmdeveloper.plugin.search.model.Query;
import com.ecmdeveloper.plugin.search.model.QueryDiagram;
import com.ecmdeveloper.plugin.search.parts.GraphicalPartFactory;
import com.ecmdeveloper.plugin.search.store.QueryFileStore;
import com.ecmdeveloper.plugin.search.util.IconFiles;

/**
 * @author ricardo.belfor
 *
 */
public class GraphicalQueryEditor extends GraphicalEditorWithFlyoutPalette implements PropertyChangeListener {

    private static final String REPLACE_PROMPT_MESSAGE = "A query with the name \"{0}\" already exists, do you want to replace this query?";

    private static final String QUERY_EDITOR = "Query Editor";

    public static final String ID = "com.ecmdeveloper.plugin.search.searchEditor";

    private Query query;

    private boolean modified;

    private QueryProxy queryProxy = new QueryProxy();

    private PaletteRoot root;

    private QueryFieldsTable queryFieldsTable;

    private ToolItem removeTableItem;

    private IAction actions[] = { new AddTableAction(this), new RemoveTableAction(this), new EditQueryComponentAction(this), new ToggleIncludeSubclassesAction(this), new ToggleDistinctAction(this), new ShowSqlAction(this), new SetMainQueryAction(this), new ConvertToTextAction(this), new ExecuteSearchAction(this), new SetMaxCountAction(this), new SetTimeLimitAction(this), new ShowPartSqlAction(this), new ToggleSearchAllVersionsAction(this) };

    private Text maxCountText;

    private Text timeLimitText;

    private ToolItem distinctButton;

    private ToolItem includeSubClassesButton;

    private ToolItem searchAllVersionsButton;

    public GraphicalQueryEditor() {
        setEditDomain(new DefaultEditDomain(this));
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        query = (Query) getEditorInput().getAdapter(Query.class);
        query.addPropertyChangeListener(this);
        queryProxy.setQuery(query);
        modified = false;
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        setPartName(input.getName());
    }

    @Override
    public void dispose() {
        super.dispose();
        query.removePropertyChangeListener(this);
        queryFieldsTable.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String propertyName = propertyChangeEvent.getPropertyName();
        if (propertyName.equals(Query.TABLE_ADDED) || propertyName.equals(Query.TABLE_REMOVED)) {
            setQueryComponentsVisibility();
            removeTableItem.setEnabled(!query.getQueryTables().isEmpty());
        }
        boolean wasDirty = isDirty();
        modified = true;
        if (!wasDirty) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    private void setQueryComponentsVisibility() {
        PaletteDrawer paletteDrawer = getQueryComponentsDrawer();
        if (query.isContentEngineQuery() == null) {
            setCeEntriesVisibility(paletteDrawer, false);
            setCmisEntriesVisibility(paletteDrawer, false);
        } else if (query.isContentEngineQuery()) {
            setCeEntriesVisibility(paletteDrawer, true);
            setCmisEntriesVisibility(paletteDrawer, false);
        } else if (!query.isContentEngineQuery()) {
            setCeEntriesVisibility(paletteDrawer, false);
            setCmisEntriesVisibility(paletteDrawer, true);
        }
    }

    private void setCmisEntriesVisibility(PaletteDrawer paletteDrawer, boolean visibility) {
        for (String entryId : QueryPaletteFactory.CMIS_ONLY_ENTRIES) {
            PaletteEntry paletteEntry = getPaletteEntry(paletteDrawer, entryId);
            paletteEntry.setVisible(visibility);
        }
        searchAllVersionsButton.setEnabled(visibility);
    }

    private void setCeEntriesVisibility(PaletteDrawer paletteDrawer, boolean visibility) {
        for (String entryId : QueryPaletteFactory.CE_ONLY_ENTRIES) {
            PaletteEntry paletteEntry = getPaletteEntry(paletteDrawer, entryId);
            paletteEntry.setVisible(visibility);
        }
        distinctButton.setEnabled(visibility);
        includeSubClassesButton.setEnabled(visibility);
        timeLimitText.setEnabled(visibility);
        searchAllVersionsButton.setEnabled(visibility);
        maxCountText.setEnabled(visibility);
    }

    private PaletteEntry getPaletteEntry(PaletteDrawer paletteDrawer, String entryId) {
        for (Object object : paletteDrawer.getChildren()) {
            if (object instanceof PaletteEntry) {
                PaletteEntry paletteEntry = (PaletteEntry) object;
                if (entryId.equals(paletteEntry.getId())) {
                    return paletteEntry;
                }
            }
        }
        throw new IllegalArgumentException("No palette entry found for " + entryId);
    }

    private PaletteDrawer getQueryComponentsDrawer() {
        for (Object object : root.getChildren()) {
            if (object instanceof PaletteDrawer) {
                PaletteDrawer paletteDrawer = (PaletteDrawer) object;
                if (paletteDrawer.getId().equals(QueryPaletteFactory.QUERY_COMPONENTS_DRAWER)) {
                    return paletteDrawer;
                }
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isDirty() {
        return modified || super.isDirty();
    }

    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        composite.setLayout(new GridLayout());
        Composite c = new Composite(composite, SWT.NONE);
        c.setLayout(new FillLayout());
        createToolBar(c);
        SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
        queryFieldsTable = new QueryFieldsTable(query, sashForm);
        setQueryComponentsVisibility();
        super.createPartControl(sashForm);
    }

    private Control createToolBar(Composite composite) {
        ToolBar toolBar = new ToolBar(composite, SWT.NONE);
        createExecuteButton(toolBar);
        createAddTableButton(toolBar);
        removeTableItem = createDeleteTableButton(toolBar);
        createIncludeSubClassesButton(toolBar);
        createSearchAllVersionsButton(toolBar);
        createDistinctButton(toolBar);
        createShowSQLButton(toolBar);
        new ToolItem(toolBar, SWT.SEPARATOR);
        maxCountText = createMaxCountText(composite);
        timeLimitText = createMaxTimeText(composite);
        return toolBar;
    }

    private Text createMaxCountText(Composite composite) {
        ToolBar toolbar = new ToolBar(composite, SWT.FLAT);
        ToolItem ti = new ToolItem(toolbar, SWT.NONE);
        ti.setText("Max Count:");
        ToolItem ti2 = new ToolItem(toolbar, SWT.SEPARATOR);
        Text text = new Text(toolbar, SWT.BORDER);
        text.setText("0000");
        text.pack();
        if (query.getMaxCount() != null) {
            text.setText(query.getMaxCount().toString());
        } else {
            text.setText("");
        }
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                IAction action = getActionRegistry().getAction(SetMaxCountAction.ID);
                action.run();
            }
        });
        ti2.setWidth(text.getBounds().width);
        ti2.setControl(text);
        return text;
    }

    public Integer getMaxCount() {
        String text = maxCountText.getText();
        if (!text.isEmpty()) {
            return new Integer(text);
        }
        return null;
    }

    private Text createMaxTimeText(Composite composite) {
        ToolBar toolbar = new ToolBar(composite, SWT.FLAT);
        ToolItem ti = new ToolItem(toolbar, SWT.NONE);
        ti.setText("Time Limit:");
        ToolItem ti2 = new ToolItem(toolbar, SWT.SEPARATOR);
        Text timeLimitText = new Text(toolbar, SWT.BORDER);
        timeLimitText.setText("0000");
        timeLimitText.pack();
        if (query.getTimeLimit() != null) {
            timeLimitText.setText(query.getTimeLimit().toString());
        } else {
            timeLimitText.setText("");
        }
        timeLimitText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                IAction action = getActionRegistry().getAction(SetTimeLimitAction.ID);
                action.run();
            }
        });
        ti2.setWidth(timeLimitText.getBounds().width);
        ti2.setControl(timeLimitText);
        return timeLimitText;
    }

    public Integer getTimeLimit() {
        String text = timeLimitText.getText();
        if (!text.isEmpty()) {
            return new Integer(text);
        }
        return null;
    }

    private void createExecuteButton(ToolBar toolBar) {
        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(Activator.getImage(IconFiles.EXECUTE));
        toolItem.setToolTipText("Execute Search");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(ExecuteSearchAction.ID);
                action.run();
            }
        });
    }

    private void createAddTableButton(ToolBar toolBar) {
        ToolItem addTableItem = new ToolItem(toolBar, SWT.PUSH);
        addTableItem.setImage(Activator.getImage(IconFiles.TABLE_ADD));
        addTableItem.setToolTipText("Add Table");
        addTableItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(AddTableAction.ID);
                action.run();
            }
        });
    }

    private ToolItem createDeleteTableButton(ToolBar toolBar) {
        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(Activator.getImage(IconFiles.TABLE_DELETE));
        toolItem.setToolTipText("Remove Table");
        toolItem.setEnabled(!query.getQueryTables().isEmpty());
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(RemoveTableAction.ID);
                action.run();
            }
        });
        return toolItem;
    }

    private void createIncludeSubClassesButton(ToolBar toolBar) {
        includeSubClassesButton = new ToolItem(toolBar, SWT.CHECK);
        includeSubClassesButton.setImage(Activator.getImage(IconFiles.TABLE_SUBCLASSES));
        includeSubClassesButton.setToolTipText("Include Subclasses");
        includeSubClassesButton.setSelection(query.isIncludeSubclasses());
        includeSubClassesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(ToggleIncludeSubclassesAction.ID);
                action.run();
            }
        });
    }

    private void createSearchAllVersionsButton(ToolBar toolBar) {
        searchAllVersionsButton = new ToolItem(toolBar, SWT.CHECK);
        searchAllVersionsButton.setImage(Activator.getImage(IconFiles.SEARCH_ALL_VERSIONS));
        searchAllVersionsButton.setToolTipText("Search All Versions");
        searchAllVersionsButton.setSelection(query.isSearchAllVersions());
        searchAllVersionsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(ToggleSearchAllVersionsAction.ID);
                action.run();
            }
        });
    }

    private void createDistinctButton(ToolBar toolBar) {
        distinctButton = new ToolItem(toolBar, SWT.CHECK);
        distinctButton.setImage(Activator.getImage(IconFiles.DISTINCT));
        distinctButton.setToolTipText("Distinct");
        distinctButton.setSelection(query.isDistinct());
        distinctButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(ToggleDistinctAction.ID);
                action.run();
            }
        });
    }

    private void createShowSQLButton(ToolBar toolBar) {
        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(Activator.getImage(IconFiles.SHOW_SQL));
        toolItem.setToolTipText("Show SQL");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IAction action = getActionRegistry().getAction(ShowSqlAction.ID);
                action.run();
            }
        });
    }

    @Override
    protected PaletteRoot getPaletteRoot() {
        if (root == null) {
            root = QueryPaletteFactory.createPalette(queryProxy);
        }
        return root;
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        GraphicalViewer viewer = configureViewer();
        configureContextMenu(viewer);
    }

    private GraphicalViewer configureViewer() {
        GraphicalViewer viewer = getGraphicalViewer();
        viewer.setEditPartFactory(new GraphicalPartFactory());
        viewer.setRootEditPart(new ScalableFreeformRootEditPart());
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
        return viewer;
    }

    private void configureContextMenu(GraphicalViewer viewer) {
        MenuManager menuManager = new QueryContextMenuManager(getActionRegistry());
        viewer.setContextMenu(menuManager);
    }

    @Override
    protected void initializeGraphicalViewer() {
        GraphicalViewer viewer = getGraphicalViewer();
        viewer.setContents(getQueryDiagram());
        getGraphicalViewer().addDropTargetListener((TransferDropTargetListener) new QueryDropTargetListener(getGraphicalViewer()));
        getGraphicalViewer().addDropTargetListener((TransferDropTargetListener) new TextTransferDropTargetListener(getGraphicalViewer(), TextTransfer.getInstance()));
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        saveQuery(false);
    }

    @Override
    public void doSaveAs() {
        saveQuery(true);
    }

    private void saveQuery(boolean saveAs) {
        try {
            QueryFileStore queryFileStore = new QueryFileStore();
            if (!setQueryName(queryFileStore, saveAs)) {
                return;
            }
            queryFileStore.save(query);
            modified = false;
            getCommandStack().markSaveLocation();
            setPartName(query.getName());
        } catch (IOException e) {
            PluginMessage.openError(getSite().getShell(), QUERY_EDITOR, e.getLocalizedMessage(), e);
        }
    }

    private boolean setQueryName(QueryFileStore queryFileStore, boolean saveAs) {
        while (true) {
            String newName = promptForNewName();
            if (newName != null) {
                if (saveAs || !query.getName().equalsIgnoreCase(newName)) {
                    if (queryFileStore.isExistingName(newName)) {
                        if (promptForReplace(newName) != true) {
                            continue;
                        }
                    }
                    query.setName(newName);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private String promptForNewName() {
        String newName = null;
        InputDialog inputDialog = new InputDialog(getSite().getShell(), QUERY_EDITOR, "Query Name", query.getName(), null);
        if (inputDialog.open() == InputDialog.OK) {
            newName = inputDialog.getValue();
        }
        return newName;
    }

    private boolean promptForReplace(String newName) {
        String message = MessageFormat.format(REPLACE_PROMPT_MESSAGE, newName);
        boolean answer = MessageDialog.openQuestion(getSite().getShell(), QUERY_EDITOR, message);
        return answer;
    }

    protected PaletteViewerProvider createPaletteViewerProvider() {
        return new PaletteViewerProvider(getEditDomain()) {

            protected void configurePaletteViewer(PaletteViewer viewer) {
                super.configurePaletteViewer(viewer);
                viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
            }

            protected void hookPaletteViewer(PaletteViewer viewer) {
                super.hookPaletteViewer(viewer);
                final CopyTemplateAction copy = (CopyTemplateAction) getActionRegistry().getAction(ActionFactory.COPY.getId());
                viewer.addSelectionChangedListener(copy);
            }
        };
    }

    protected QueryDiagram getQueryDiagram() {
        return query.getQueryDiagram();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createActions() {
        super.createActions();
        ActionRegistry registry = getActionRegistry();
        IAction action = new CopyTemplateAction(this);
        registry.registerAction(action);
        for (IAction action2 : actions) {
            getActionRegistry().registerAction(action2);
            getSelectionActions().add(action2.getId());
        }
    }

    public Query getQuery() {
        return query;
    }
}
