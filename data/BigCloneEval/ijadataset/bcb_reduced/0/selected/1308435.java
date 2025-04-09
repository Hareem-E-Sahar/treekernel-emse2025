package org.xaware.ide.xadev.richui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.framework.Bundle;
import org.xaware.common.AdvisorFactory;
import org.xaware.common.exception.XAwareAdvisorException;
import org.xaware.ide.xadev.XA_Designer_Plugin;
import org.xaware.ide.xadev.common.ControlFactory;
import org.xaware.ide.xadev.common.EditorUtils;
import org.xaware.ide.xadev.common.GlobalConstants;
import org.xaware.ide.xadev.common.ResourceUtils;
import org.xaware.ide.xadev.gui.ChangeEvent;
import org.xaware.ide.xadev.gui.ChangeListener;
import org.xaware.ide.xadev.gui.actions.ActionController;
import org.xaware.ide.xadev.gui.actions.SelectAllAction;
import org.xaware.ide.xadev.gui.actions.XAUndoContext;
import org.xaware.ide.xadev.gui.editor.BizdocExecutionManager;
import org.xaware.ide.xadev.gui.editor.IXAwareEditor;
import org.xaware.ide.xadev.gui.model.DocumentModel;
import org.xaware.ide.xadev.gui.view.outline.XAContentOutlinePage;
import org.xaware.ide.xadev.richui.editor.service.IServiceEditor;
import org.xaware.ide.xadev.richui.editor.util.RichUIEditorUtil;
import org.xaware.shared.util.XAwareException;
import org.xaware.shared.util.logging.XAwareLogger;

/**
 * Base Class for All Editors in designer.
 * 
 * Original code was moved from service editor.
 * 
 * @author Tim Ferguson
 * @author Vasu Thadaka
 * 
 */
public abstract class AbstractBaseEditor extends FormEditor implements ChangeListener, IXAwareEditor, IPropertyListener, IResourceChangeListener, IReferenceModificationListener, ISaveablePart2 {

    private final XAwareLogger logger = XAwareLogger.getXAwareLogger(AbstractBaseEditor.class.getName());

    ;

    /** Undo context for the service editor. */
    private IUndoContext undoContext;

    /** Holds Undo action handler instance. */
    private UndoActionHandler undoAction;

    /** Holds Redo action handler instance. */
    private RedoActionHandler redoAction;

    /** Holds reference to the selectAllAction instance. */
    private Action selectAllAction = null;

    /** holds the undo handler activation instance. */
    private IHandlerActivation undoHandlerActivation;

    /** holds the redo handler activation instance. */
    private IHandlerActivation redoHandlerActivation;

    /** holds the select all handler activation instance. */
    private IHandlerActivation selectAllHandlerActivation;

    /** Execution Manager instance. */
    protected BizdocExecutionManager executionManager;

    /** holds the cut handler activation instance. */
    private IHandlerActivation cutHandlerActivation;

    /** holds the copy handler activation instance. */
    private IHandlerActivation copyHandlerActivation;

    /** holds the delete handler activation instance. */
    private IHandlerActivation deleteHandlerActivation;

    /** holds the paste handler activation instance. */
    private IHandlerActivation pasteHandlerActivation;

    /** Page Listeners copy. */
    private ListenerList pageListeners = new ListenerList();

    /**
     * Holds the instance that provides the outline page for this editor.
     */
    private XAContentOutlinePage xaOutlinePage;

    protected boolean internalChange = false;

    /** Bundle where the pages exist. */
    private Bundle bundlesToLook = null;

    /** Messages for this editor. */
    @SuppressWarnings("unchecked")
    private static HashMap<IFile, HashMap<Class, ArrayList<Object>>> messagesList = new HashMap<IFile, HashMap<Class, ArrayList<Object>>>();

    /** True if page is in initialization mode. */
    private boolean initializationMode = false;

    /**
     * Default constructor.
     * 
     * @throws XAwareException
     */
    public AbstractBaseEditor() throws XAwareException {
        try {
            AdvisorFactory.getAdvisor(getAdvisorComponentName());
        } catch (XAwareAdvisorException exception) {
            throw new XAwareException(exception.getMessage(), exception);
        }
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setPartName(input.getName());
        executionManager = new BizdocExecutionManager(this);
        undoContext = new XAUndoContext();
        undoAction = new UndoActionHandler(getEditorSite(), undoContext);
        undoAction.setActionDefinitionId("org.eclipse.ui.edit.undo");
        undoAction.setAccelerator(SWT.CTRL | 'Z');
        redoAction = new RedoActionHandler(getEditorSite(), undoContext);
        redoAction.setActionDefinitionId("org.eclipse.ui.edit.redo");
        redoAction.setAccelerator(SWT.CTRL | 'Y');
        selectAllAction = new SelectAllAction();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        registerAsReferenceModificationListener();
    }

    /**
     * Registers this editor as reference modification listener. default implementation does nothing. Sub classes may
     * override.
     * 
     * @throws PartInitException
     */
    public void registerAsReferenceModificationListener() throws PartInitException {
    }

    /**
     * Executes with olders values.
     * 
     * @throws PartInitException
     */
    public void execute() throws XAwareException {
        this.execute(false);
    }

    public void execute(boolean rerun) throws XAwareException {
        if (isDirty()) {
            int response;
            try {
                response = ControlFactory.showConfirmDialog("'" + getIFile().getName() + "' has been modified, Save changes?", "Execute", true);
            } catch (PartInitException e) {
                throw new XAwareException("Unable to get the IFile instance.", e);
            }
            if (response == 0) this.doSave(new NullProgressMonitor()); else if (response != 1) return;
        }
        IWorkbenchPage page = XA_Designer_Plugin.getActiveWorkbenchWindow().getActivePage();
        if (IWorkbenchPage.STATE_MAXIMIZED == page.getPartState(page.getReference(page.getActivePart()))) {
            page.toggleZoom(page.getReference(page.getActivePart()));
        }
        executionManager.execute(rerun);
    }

    public abstract int getType();

    public abstract String getTypeName();

    /**
     * Returns the component name for getting the advisor. 
     * 
     * @return Advisor Component name.
     */
    protected abstract String getAdvisorComponentName();

    /**
     * Returns the model that the pages are working on
     * 
     * @return
     */
    public abstract DocumentModel getPagesModel();

    public int getSelectionCount() {
        return 0;
    }

    public void updateClipboardActions() {
    }

    /** Updates the UNDO and REDO states to actions. */
    public void updateUndoState() {
        if (undoAction == null) {
            return;
        }
        undoAction.update();
        redoAction.update();
        undoAction.setToolTipText(undoAction.getText());
        redoAction.setToolTipText(redoAction.getText());
        getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
        getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
        getEditorSite().getActionBars().setGlobalActionHandler(GlobalConstants.ID_UNDO_TOOLBAR, undoAction);
        getEditorSite().getActionBars().setGlobalActionHandler(GlobalConstants.ID_REDO_TOOLBAR, redoAction);
        getEditorSite().getActionBars().updateActionBars();
    }

    /**
     * Registers actions like undo, redo etc.
     */
    private void registerEditorActions() {
        final IHandlerService service = (IHandlerService) getEditorSite().getService(IHandlerService.class);
        final ActionController actionController = ActionController.getInstance();
        if ((service != null) && (actionController != null)) {
            undoHandlerActivation = service.activateHandler(undoAction.getActionDefinitionId(), new ActionHandler(undoAction));
            redoHandlerActivation = service.activateHandler(redoAction.getActionDefinitionId(), new ActionHandler(redoAction));
            selectAllHandlerActivation = service.activateHandler(selectAllAction.getActionDefinitionId(), new ActionHandler(selectAllAction));
            Action cutNodeAction = actionController.getDesktopAction(ActionController.EDIT_CUT_NODE_ACTION);
            cutHandlerActivation = service.activateHandler(cutNodeAction.getActionDefinitionId(), new ActionHandler(cutNodeAction));
            Action copyNodeAction = actionController.getDesktopAction(ActionController.EDIT_COPY_NODE_ACTION);
            copyHandlerActivation = service.activateHandler(copyNodeAction.getActionDefinitionId(), new ActionHandler(copyNodeAction));
            Action pasteNodeAction = actionController.getDesktopAction(ActionController.EDIT_PASTE_NODE_ACTION);
            pasteHandlerActivation = service.activateHandler(pasteNodeAction.getActionDefinitionId(), new ActionHandler(pasteNodeAction));
            Action deleteNodeAction = actionController.getDesktopAction(ActionController.EDIT_DELETE_NODE_ACTION);
            deleteHandlerActivation = service.activateHandler(deleteNodeAction.getActionDefinitionId(), new ActionHandler(deleteNodeAction));
        }
    }

    /**
     * Unregisters actions like undo, redo.
     */
    private void unregisterEditorActions() {
        final IHandlerService service = (IHandlerService) getEditorSite().getService(IHandlerService.class);
        final ActionController actionController = ActionController.getInstance();
        if ((service != null) && (actionController != null)) {
            if (undoAction != null) {
                service.deactivateHandler(undoHandlerActivation);
            }
            if (redoAction != null) {
                service.deactivateHandler(redoHandlerActivation);
            }
            if (selectAllAction != null) {
                service.deactivateHandler(selectAllHandlerActivation);
            }
            if (cutHandlerActivation != null) {
                service.deactivateHandler(cutHandlerActivation);
            }
            if (copyHandlerActivation != null) {
                service.deactivateHandler(copyHandlerActivation);
            }
            if (pasteHandlerActivation != null) {
                service.deactivateHandler(pasteHandlerActivation);
            }
            if (deleteHandlerActivation != null) {
                service.deactivateHandler(deleteHandlerActivation);
            }
        }
    }

    /**
     * Sets the given undo context instance to the undo/redo action handlers.
     * 
     * @param context
     *            undo context to be set to the undo/redo action handlers.
     */
    public void updateUndoContextToActions(IUndoContext context) {
        if (context == null) {
            context = undoContext;
        }
        undoAction.setContext(context);
        redoAction.setContext(context);
    }

    @Override
    public void dispose() {
        try {
            cleanMessagesFor(getIFile());
        } catch (PartInitException e) {
            logger.severe("Exception occured while cleaning up the messages.", e);
        }
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        unregisterEditorActions();
        unregisterAsReferenceModificationListener();
        super.dispose();
    }

    /**
     * Unregister this editor as a reference modification listener. By default does nothing. Sub classes may override to
     * provide the behaviour.
     */
    public void unregisterAsReferenceModificationListener() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public boolean referencedFilesSaved() {
        return true;
    }

    public void setDisplayPartName(String string) {
        setPartName(string);
    }

    @Override
    public void doSaveAs() {
    }

    /** Returns the list of pages instance. */
    @SuppressWarnings("unchecked")
    public Vector getPages() {
        return pages;
    }

    /** Returns the pages for this editor. */
    protected abstract List<EditorPageDefinition> getPageDefinitions();

    /**
     * Initialize page.
     * 
     */
    protected abstract void initializePage(AbstractBaseEditorPage page, EditorPageDefinition pageDefinition);

    @Override
    protected void addPages() {
        try {
            List<EditorPageDefinition> pages = getPageDefinitions();
            String prevId;
            String nextId;
            for (EditorPageDefinition pageDef : pages) {
                int index = pages.indexOf(pageDef);
                String className = pageDef.getServiceEditorClass();
                Class<?> classInstance = null;
                if (bundlesToLook != null) {
                    logger.finest("Loading class:" + className + " from bundle:" + bundlesToLook);
                    classInstance = bundlesToLook.loadClass(className);
                } else {
                    classInstance = Class.forName(className);
                }
                AbstractBaseEditorPage page = (AbstractBaseEditorPage) classInstance.getConstructor(AbstractBaseEditor.class, String.class, String.class).newInstance(this, pageDef.getId(), pageDef.getTitle());
                if (index == 0) {
                    prevId = "";
                } else {
                    prevId = pages.get(index - 1).getId();
                }
                if (index == (pages.size() - 1)) {
                    nextId = "";
                } else {
                    nextId = pages.get(index + 1).getId();
                }
                initializePage(page, pageDef);
                page.setSiblings(prevId, nextId);
                addPage(page);
            }
            addAdditionalPages();
            registerEditorActions();
        } catch (Exception exception) {
            ControlFactory.showStackTrace("Exception occured while creating the XAware Editor.", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Adds additional pages to editor.
     * 
     * @throws PartInitException
     */
    protected abstract void addAdditionalPages() throws PartInitException;

    /**
     * gives the undocontext for this editor.
     * 
     * @return undoContext undocontext for this editor.
     */
    public IUndoContext getUndoContext() {
        Object page = pages.get(getActivePage());
        if (page instanceof IEditorPage) return ((IEditorPage) page).getUndoContext();
        return null;
    }

    public void stateChanged(ChangeEvent e) {
        editorStateChanged(e);
        this.editorDirtyStateChanged();
    }

    /** Triggered up on state change. */
    protected abstract void editorStateChanged(ChangeEvent e);

    @Override
    public void doSave(IProgressMonitor monitor) {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        boolean errorsExist = false;
        for (Object page : this.pages) {
            if (page != null && page instanceof FormPage) {
                FormPage formPage = (FormPage) page;
                if (formPage.getManagedForm().getForm().getMessageType() == IMessage.ERROR) {
                    errorsExist = true;
                    break;
                }
            }
        }
        if (errorsExist) {
            int result = ControlFactory.showConfirmDialog("Error(s) exist in some of the pages. Do you want to save it in any way?");
            if (result != IDialogConstants.OK_ID) return;
        }
        for (Object page : this.pages) {
            if (page != null && page instanceof ISaveablePart) {
                ISaveablePart saveable = (ISaveablePart) page;
                if (saveable.isDirty()) saveable.doSave(monitor);
            }
        }
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.xaware.ide.xadev.richui.editor.service.IServiceEditor#initializeAllPages()
     */
    public void initializeAllPages() {
        int index = 0;
        for (Object page : pages) {
            if (page instanceof AbstractBaseEditorPage) {
                initializePage((AbstractBaseEditorPage) page, index);
            }
            index++;
        }
    }

    /** Initialize given page. */
    public void initializePage(AbstractBaseEditorPage editorPage) {
        int index = pages.indexOf(editorPage);
        initializePage(editorPage, index);
    }

    /** Initialize given page with given index. */
    public void initializePage(AbstractBaseEditorPage editorPage, int index) {
        if (!editorPage.isInitialized()) {
            setInitializationMode(true);
            try {
                pageChange(index);
            } finally {
                setInitializationMode(false);
            }
        }
    }

    /**
     * Method from super class. Used to mark the internal xml editor with markers when Search operation is performed.
     * 
     * @param adapter
     *            Instance of Class.
     * 
     * @return Object instance.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class key) {
        IEditorPage current = (IEditorPage) this.getActivePageInstance();
        Object returnObject = null;
        if (current != null) {
            returnObject = current.getAdapter(key);
        }
        if (returnObject == null) {
            if (key.equals(IContentOutlinePage.class) && !(this instanceof IServiceEditor)) {
                if (xaOutlinePage == null) {
                    xaOutlinePage = new XAContentOutlinePage(this);
                }
                return xaOutlinePage;
            }
        }
        if (returnObject == null) returnObject = super.getAdapter(key);
        return returnObject;
    }

    /** Returns true if any of the boolean is changed. */
    protected abstract boolean isModelChanged();

    @Override
    public boolean isDirty() {
        boolean b = isModelChanged();
        if (b) {
            return true;
        } else {
            for (Object page : this.pages) {
                if (page != null) {
                    if (page instanceof AbstractBaseEditorPage) {
                        b = ((AbstractBaseEditorPage) page).isDirty();
                        if (b) {
                            return true;
                        }
                    }
                }
            }
        }
        return super.isDirty();
    }

    /** Gets the active editor page irrespective of IFormPage instance. */
    public Object getActiveEditorPage() {
        int index = super.getActivePage();
        if (index != -1) return pages.get(index);
        return null;
    }

    public void fireEditorPropertyChange(int propertyId) {
        firePropertyChange(propertyId);
    }

    public void propertyChanged(Object source, int propId) {
        if (propId == ISaveablePart.PROP_DIRTY) {
            firePropertyChange(ISaveablePart.PROP_DIRTY);
        }
    }

    @Override
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        updateUndoContextToActions(getUndoContext());
        if (getActivePageInstance() == null) {
            firePageChanged(new PageChangedEvent(this, getActiveEditorPage()));
        }
        Object page = getActiveEditorPage();
        if (page instanceof AbstractBaseEditorPage) {
            boolean isLastPage = pages.size() == newPageIndex;
            boolean isFirstPage = 0 == newPageIndex;
            if (!isLastPage && newPageIndex + 1 < pages.size()) {
                if (!(pages.get(newPageIndex + 1) instanceof AbstractBaseEditorPage)) isLastPage = true;
            }
            AbstractBaseEditorPage editorPage = (AbstractBaseEditorPage) page;
            editorPage.getNextAction().setEnabled(!isLastPage);
            editorPage.getBackAction().setEnabled(!isFirstPage);
        }
        if (page instanceof IEditorPart) {
            ((IEditorPart) page).setFocus();
        }
        ActionController.getInstance().updateActionsStateForEditor(this);
    }

    public void addPageChangedListener(IPageChangedListener listener) {
        pageListeners.add(listener);
        super.addPageChangedListener(listener);
    }

    public void removePageChangedListener(IPageChangedListener listener) {
        pageListeners.remove(listener);
        super.removePageChangedListener(listener);
    }

    /** Code copied from the baseclass to get page listeners. */
    private void firePageChanged(final PageChangedEvent event) {
        Object[] listeners = pageListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final IPageChangedListener l = (IPageChangedListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {

                public void run() {
                    l.pageChanged(event);
                }
            });
        }
    }

    /**
     * Implemented for IResourceChangeListener interface. This controls the closing of the editor when resources are
     * changed some way other than the editor itself.
     */
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getSource() == this) {
            return;
        }
        switch(event.getType()) {
            case IResourceChangeEvent.PRE_CLOSE:
            case IResourceChangeEvent.PRE_DELETE:
                if (EditorUtils.isSameProjectAsEditorInput(event.getResource(), getEditorInput())) {
                    EditorUtils.closeEditor(this, false);
                }
                break;
            case IResourceChangeEvent.POST_CHANGE:
                try {
                    event.getDelta().accept(new AbstractBaseEditorDeltaVisitor(this));
                } catch (final CoreException e) {
                    logger.finest("Caught exception " + e.toString() + " during change processing", e);
                }
            default:
                break;
        }
    }

    /**
     * 
     * @author jweaver
     * 
     */
    class AbstractBaseEditorDeltaVisitor implements IResourceDeltaVisitor {

        private final String vistorClass = AbstractBaseEditorDeltaVisitor.class.getName();

        private IEditorPart part;

        public AbstractBaseEditorDeltaVisitor(IEditorPart part) {
            this.part = part;
        }

        public boolean visit(final IResourceDelta delta) throws CoreException {
            final String methodName = "visit";
            final IResource resource = delta.getResource();
            if (!(resource instanceof IFile)) return true;
            if (EditorUtils.isResourceSameAsEditorInput(resource, part.getEditorInput())) {
                logger.finest("Same resource as editor input.", vistorClass, methodName);
                switch(delta.getKind()) {
                    case IResourceDelta.ADDED:
                        logger.finest("In ADDED in visit()", vistorClass, methodName);
                        reloadEditor();
                        break;
                    case IResourceDelta.REMOVED:
                        logger.finest("In REMOVED in visit()", vistorClass, methodName);
                        EditorUtils.closeEditor(part, false);
                        break;
                    case IResourceDelta.CHANGED:
                        final int flags = delta.getFlags();
                        logger.finest("In CHANGED in visit() with flags = " + flags, vistorClass, methodName);
                        if ((delta.getFlags() & IResourceDelta.REPLACED) != 0 || (delta.getFlags() & IResourceDelta.MOVED_TO) != 0 || (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
                            logger.finest("In REPLACED, MOVED_TO, or MOVED_FROM CHANGE", vistorClass, methodName);
                            try {
                                if (internalChange) {
                                    logger.finest("Internal Change", vistorClass, methodName);
                                } else {
                                    logger.finest("Reloading file", vistorClass, methodName);
                                    getEditorSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {

                                        public void run() {
                                            reloadEditor();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                logger.finest("Failed to reload the file.", vistorClass, methodName, e);
                            }
                        }
                        if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                            logger.finest("In Content CHANGED", vistorClass, methodName);
                            try {
                                if (internalChange) {
                                    logger.finest("Internal Change", vistorClass, methodName);
                                } else {
                                    logger.finest("Updating from file", vistorClass, methodName);
                                    getEditorSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {

                                        public void run() {
                                            reloadEditor();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                logger.finest("Failed to reload the file.", vistorClass, methodName, e);
                            }
                        }
                        if ((delta.getFlags() & IResourceDelta.TYPE) != 0) {
                            logger.finest("In TYPE CHANGED", vistorClass, methodName);
                            EditorUtils.closeEditor(part, false);
                        }
                        if ((delta.getFlags() & IResourceDelta.SYNC) != 0) {
                            logger.info("Sync status changed.", vistorClass, methodName);
                        }
                        if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
                            logger.info("OPEN status changed.", vistorClass, methodName);
                        }
                        if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
                            logger.info("MARKERS status changed.", vistorClass, methodName);
                        }
                        if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
                            logger.info("DESCRIPTION status changed.", vistorClass, methodName);
                        }
                        if ((delta.getFlags() & IResourceDelta.ENCODING) != 0) {
                            logger.info("ENCODING status changed.", vistorClass, methodName);
                        }
                        logger.finest("Leaving CHANGED in visit()", vistorClass, methodName);
                        break;
                    default:
                        break;
                }
            }
            return true;
        }
    }

    public int promptToSaveOnClose() {
        for (Object page : pages) {
            if (page instanceof AbstractBaseEditorPage) {
                int status = ((AbstractBaseEditorPage) page).promptToSaveOnClose();
                if (status != ISaveablePart2.DEFAULT) return status;
            }
        }
        return ISaveablePart2.DEFAULT;
    }

    public void referenceModified(ArrayList<IFile> currentReferences, IFile modifiedReference, boolean remove) {
        for (Object page : pages) {
            if (page instanceof AbstractBaseEditorPage) {
                AbstractBaseEditorPage editorPage = (AbstractBaseEditorPage) page;
                if (editorPage.isInitialized()) editorPage.updateReferences(currentReferences, modifiedReference, remove);
            }
        }
    }

    public IFile getIFile() throws PartInitException {
        return ResourceUtils.getIFile(RichUIEditorUtil.getPathFromInput(getEditorInput()).toFile());
    }

    public int getTypeMask() {
        return IReferenceModificationListener.REFERENCE_MODIFICATION_NONE;
    }

    @SuppressWarnings("unchecked")
    public static boolean addMessage(IFile file, Class page, Object message) {
        HashMap<Class, ArrayList<Object>> pages = messagesList.get(file);
        if (pages == null) {
            pages = new HashMap<Class, ArrayList<Object>>();
            messagesList.put(file, pages);
        }
        ArrayList<Object> messages = pages.get(page);
        if (messages == null) {
            messages = new ArrayList();
            pages.put(page, messages);
        }
        return messages.add(message);
    }

    /** Cleans the message related to passed IFile. */
    public static void cleanMessagesFor(IFile file) {
        Set<IFile> keySet = messagesList.keySet();
        Object keyToDelete = null;
        for (IFile key : keySet) {
            if (key.equals(file)) {
                keyToDelete = key;
            }
        }
        if (keyToDelete != null) messagesList.remove(keyToDelete);
    }

    /** Gets the messages for given page. **/
    public static ArrayList<Object> getMessages(IFile file, Class page) {
        return getMessages(file, page, false);
    }

    /** Removes the messages for the given page. */
    public static void removeMessages(IFile file, Class page) {
        getMessages(file, page, true);
    }

    /** Gets the messages for given page. */
    @SuppressWarnings("unchecked")
    public static ArrayList<Object> getMessages(IFile file, Class page, boolean remove) {
        Set<IFile> keySet = messagesList.keySet();
        ArrayList<Object> messages = null;
        for (IFile key : keySet) {
            if (key.equals(file)) {
                HashMap<Class, ArrayList<Object>> pageMessages = messagesList.get(key);
                if (pageMessages != null) {
                    messages = pageMessages.get(page);
                    if (remove) {
                        pageMessages.remove(page);
                    }
                }
            }
        }
        return messages;
    }

    /**
     * @return the bundlesToLook
     */
    public Bundle getBundlesToLook() {
        return bundlesToLook;
    }

    /**
     * @param bundlesToLook
     *            the bundlesToLook to set
     */
    public void setBundlesToLook(Bundle bundlesToLook) {
        this.bundlesToLook = bundlesToLook;
    }

    /**
     * @return the initializationMode
     */
    public boolean isInitializationMode() {
        return initializationMode;
    }

    /**
     * @param initializationMode
     *            the initializationMode to set
     */
    public void setInitializationMode(boolean initializationMode) {
        this.initializationMode = initializationMode;
    }

    public boolean isExecuting() {
        return executionManager.isExecuting();
    }

    /**
     * Returns the page corresponding to the provided page's class name.
     * 
     * @param pageClassName
     *            name of the page class.
     * @return the page corresponding to the provided page's class name.
     */
    public Object getPage(String pageClassName) {
        for (Object page : getPages()) {
            if (page != null && page.getClass().getName().equals(pageClassName)) {
                return page;
            }
        }
        return null;
    }
}
