package org.eclipse.gmf.runtime.diagram.ui.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.common.ui.action.actions.global.ClipboardContentsHelper;
import org.eclipse.gmf.runtime.common.ui.action.actions.global.ClipboardManager;
import org.eclipse.gmf.runtime.common.ui.action.global.GlobalActionId;
import org.eclipse.gmf.runtime.common.ui.services.action.global.AbstractGlobalActionHandler;
import org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionContext;
import org.eclipse.gmf.runtime.common.ui.util.ICustomData;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.commands.ClipboardCommand;
import org.eclipse.gmf.runtime.diagram.ui.internal.commands.CopyCommand;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.diagram.ui.providers.internal.DiagramProvidersDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.providers.internal.DiagramProvidersPlugin;
import org.eclipse.gmf.runtime.diagram.ui.requests.PasteViewRequest;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.ui.properties.actions.PropertyPageViewAction;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Class that implements the <code>IGlobalActionHandler</code> interface.
 * Contains behaviour common to GMF diagrams.
 * 
 * @author Vishy Ramaswamy
 */
public class DiagramGlobalActionHandler extends AbstractGlobalActionHandler {

    /** Remember the "open" commands associated with the selected edit parts. */
    private ICommand openCommand = null;

    /**
	 * Constructor for DiagramGlobalActionHandler.
	 */
    public DiagramGlobalActionHandler() {
        super();
    }

    public ICommand getCommand(IGlobalActionContext cntxt) {
        IWorkbenchPart part = cntxt.getActivePart();
        if (!(part instanceof IDiagramWorkbenchPart)) {
            return null;
        }
        IDiagramWorkbenchPart diagramPart = (IDiagramWorkbenchPart) part;
        ICommand command = null;
        String actionId = cntxt.getActionId();
        if (actionId.equals(GlobalActionId.DELETE)) {
            CompoundCommand deleteCC = getDeleteCommand(diagramPart, cntxt);
            if (deleteCC != null && deleteCC.canExecute()) command = new CommandProxy(deleteCC);
        } else if (actionId.equals(GlobalActionId.COPY)) {
            command = getCopyCommand(cntxt, diagramPart, false);
        } else if (actionId.equals(GlobalActionId.CUT)) {
            command = getCutCommand(cntxt, diagramPart);
        } else if (actionId.equals(GlobalActionId.OPEN)) {
            command = openCommand;
        } else if (actionId.equals(GlobalActionId.PASTE)) {
            PasteViewRequest pasteReq = createPasteViewRequest();
            Object[] objects = ((IStructuredSelection) cntxt.getSelection()).toArray();
            if (objects.length == 1) {
                Command paste = ((EditPart) objects[0]).getCommand(pasteReq);
                if (paste != null) {
                    CommandStack cs = diagramPart.getDiagramEditDomain().getDiagramCommandStack();
                    cs.execute(paste);
                    diagramPart.getDiagramEditPart().getFigure().invalidate();
                    diagramPart.getDiagramEditPart().getFigure().validate();
                    selectAddedObject(diagramPart.getDiagramGraphicalViewer(), DiagramCommandStack.getReturnValues(paste));
                    return null;
                }
            }
        } else if (actionId.equals(GlobalActionId.SAVE)) {
            part.getSite().getPage().saveEditor((IEditorPart) diagramPart, false);
        } else if (actionId.equals(GlobalActionId.PROPERTIES)) {
            new PropertyPageViewAction().run();
        }
        return command;
    }

    /**
	 * Returns a command to copy the context's selection to the clipboard.
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> from which the label
	 *            and selection are retrieved.
	 * @param diagramPart
	 *            the <code>IDiagramWorkbenchPart</code> from which the
	 *            diagram is retrieved.
	 * @param isUndoable
	 *            true if this command should be undoable/redoable; false
	 *            otherwise
	 * @return the copy command
	 */
    protected ICommand getCopyCommand(IGlobalActionContext cntxt, IDiagramWorkbenchPart diagramPart, final boolean isUndoable) {
        TransactionalEditingDomain editingDomain = getEditingDomain(diagramPart);
        if (editingDomain == null) {
            return null;
        }
        return new CopyCommand(editingDomain, cntxt.getLabel(), diagramPart.getDiagram(), getSelectedViews(cntxt.getSelection())) {

            public boolean canUndo() {
                return isUndoable;
            }

            public boolean canRedo() {
                return isUndoable;
            }

            protected IStatus doUndo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                if (isUndoable) {
                    return Status.OK_STATUS;
                }
                return super.doUndo(monitor, info);
            }

            protected IStatus doRedo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                if (isUndoable) {
                    return Status.OK_STATUS;
                }
                return super.doRedo(monitor, info);
            }
        };
    }

    /**
	 * Returns a command to copy the context's selection to the clipboard and to
	 * delete it.
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> from which the label
	 *            and selection are retrieved.
	 * @param diagramPart
	 *            the <code>IDiagramWorkbenchPart</code> from which the
	 *            diagram is retrieved.
	 * @return the cut command
	 */
    protected ICommand getCutCommand(IGlobalActionContext cntxt, IDiagramWorkbenchPart diagramPart) {
        TransactionalEditingDomain editingDomain = getEditingDomain(diagramPart);
        if (editingDomain == null) {
            return null;
        }
        CompositeTransactionalCommand cut = new CompositeTransactionalCommand(editingDomain, cntxt.getLabel());
        cut.compose(getCopyCommand(cntxt, diagramPart, true));
        Object[] objects = ((IStructuredSelection) cntxt.getSelection()).toArray();
        for (int i = 0; i < objects.length; i++) {
            EditPart editPart = (EditPart) objects[i];
            GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
            Command deleteCommand = editPart.getCommand(deleteReq);
            if (deleteCommand != null) {
                cut.compose(new CommandProxy(deleteCommand));
            }
        }
        if (!cut.isEmpty() && cut.canExecute()) return cut;
        return null;
    }

    /**
	 * Creates and returns the appropriate <code>PasteViewRequest</code> that
	 * is to be used to get the appropriate paste <code>Command</code> from
	 * the <code>EditPart</code>. The returned <code>PasteViewRequest</code>
	 * contains data from the clipboard
	 * 
	 * @return PasteViewRequest
	 */
    protected PasteViewRequest createPasteViewRequest() {
        PasteViewRequest pasteReq;
        ICustomData[] data = ClipboardManager.getInstance().getClipboardData(ClipboardCommand.DRAWING_SURFACE, ClipboardContentsHelper.getInstance());
        if (data == null) {
            data = ClipboardManager.getInstance().getClipboardData(ClipboardManager.COMMON_FORMAT, ClipboardContentsHelper.getInstance());
        }
        pasteReq = new PasteViewRequest(data);
        return pasteReq;
    }

    /**
	 * Returns appropriate delete command for this context.
	 * 
     * @param part the workbench part
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return CompoundCommand command
	 */
    private CompoundCommand getDeleteCommand(IDiagramWorkbenchPart part, IGlobalActionContext cntxt) {
        GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
        CompoundCommand deleteCC = new CompoundCommand(cntxt.getLabel());
        TransactionalEditingDomain editingDomain = getEditingDomain(part);
        if (editingDomain == null) {
            return deleteCC;
        }
        CompositeTransactionalCommand compositeCommand = new CompositeTransactionalCommand(editingDomain, cntxt.getLabel());
        Object[] objects = ((IStructuredSelection) cntxt.getSelection()).toArray();
        for (int i = 0; i < objects.length; i++) {
            EditPart editPart = (EditPart) objects[i];
            Command command = editPart.getCommand(deleteReq);
            if (command != null) compositeCommand.compose(new CommandProxy(command));
        }
        if (!compositeCommand.isEmpty()) {
            deleteCC.add(new ICommandProxy(compositeCommand));
        }
        return deleteCC;
    }

    private boolean isContainedInViews(List views, View view) {
        while (view != null) {
            if (views.contains(view)) {
                return true;
            }
            if (view.eContainer() instanceof View) view = (View) view.eContainer(); else break;
        }
        return false;
    }

    /**
	 * Returns the selected <code>View</code> objects, only if the selection
	 * is an <code>IStructuredSelection</code>. and only the
	 * <code>View</code> object of an <code>INodeEditPart</code> or a
	 * <code>ShapeEditPart</code>
	 * 
	 * @param sel
	 *            the selection from which to extract the View objects
	 * @return List the selected View. Could be empty if the selection doesn't
	 *         contain proper edit parts, or, could be the original if the
	 *         selection is not an <code>IStructuredSelection</code>
	 */
    protected List getSelectedViews(ISelection sel) {
        final ArrayList views = new ArrayList();
        final ArrayList editParts = new ArrayList();
        if (!(sel instanceof IStructuredSelection)) {
            return views;
        }
        for (Iterator i = ((IStructuredSelection) sel).iterator(); i.hasNext(); ) {
            Object object = i.next();
            if (!((object instanceof INodeEditPart) || (object instanceof ShapeEditPart))) {
                continue;
            }
            View view = (object instanceof IAdaptable) ? (View) ((IAdaptable) object).getAdapter(View.class) : null;
            if (view != null && view.eResource() != null) {
                views.add(view);
                editParts.add(object);
            }
        }
        if (!views.isEmpty()) {
            try {
                TransactionUtil.getEditingDomain(views.get(0)).runExclusive(new Runnable() {

                    public void run() {
                        ArrayList objects = (ArrayList) views.clone();
                        for (Iterator i = objects.iterator(); i.hasNext(); ) {
                            Object object = i.next();
                            if (object instanceof Edge) {
                                Edge view = (Edge) object;
                                View fromView = view.getSource();
                                View toView = view.getTarget();
                                if (fromView == null || toView == null || !isContainedInViews(views, fromView) || !isContainedInViews(views, toView)) {
                                    views.remove(view);
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Trace.catching(DiagramProvidersPlugin.getInstance(), DiagramProvidersDebugOptions.EXCEPTIONS_CATCHING, getClass(), "getSelectedViews()", e);
            }
        }
        boolean doesSelectionContainAShapeView = false;
        for (Iterator i = editParts.iterator(); i.hasNext(); ) {
            if (i.next() instanceof ShapeEditPart) {
                doesSelectionContainAShapeView = true;
                break;
            }
        }
        if (!doesSelectionContainAShapeView) {
            views.clear();
        }
        return views;
    }

    /**
	 * Checks to determine if the selected edit part can be opened.
	 * <p>
	 * In order to truly verify that the edit part can be opened, the
	 * corresponding "open" command must be obtained and tested for execution.
	 * This command can then be cached for the getCommand() method.
	 * <p>
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean - indicates an "open" command exists and can be executed
	 */
    private boolean canOpen(IGlobalActionContext cntxt) {
        boolean canOpenAll = true;
        openCommand = new CompositeCommand(cntxt.getLabel());
        Object[] objects = ((IStructuredSelection) cntxt.getSelection()).toArray();
        for (int i = 0; canOpenAll && i < objects.length; i++) {
            if (objects[i] instanceof EditPart) {
                EditPart editPart = (EditPart) objects[i];
                Request request = new Request(RequestConstants.REQ_OPEN);
                Command cmd = editPart.getCommand(request);
                if (cmd == null || !cmd.canExecute()) {
                    canOpenAll = false;
                } else {
                    openCommand.compose(new CommandProxy(cmd));
                }
            }
        }
        if (!canOpenAll) {
            openCommand = null;
        }
        return canOpenAll;
    }

    /**
	 * Checks if the selected IViews can be deleted.
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean indicating the enablement of the delete action
	 */
    private boolean canDelete(IGlobalActionContext cntxt) {
        return getCommand(cntxt) != null;
    }

    /**
	 * Checks if the selected IViews can be copied to the clipboard
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean indicating the enablement of the copy action
	 */
    protected boolean canCopy(IGlobalActionContext cntxt) {
        List elements = getSelectedViews(cntxt.getSelection());
        if (elements.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
	 * Checks if the selected IViews can be cut
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean indicating the enablement of the cut action
	 */
    protected boolean canCut(IGlobalActionContext cntxt) {
        String actionId = cntxt.getActionId();
        if (actionId.equals(GlobalActionId.CUT)) {
            ICommand command = getCommand(cntxt);
            if (command != null && command.canExecute()) {
                return canCopy(cntxt);
            }
        }
        return false;
    }

    /**
	 * Checks if the paste can occur
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean indicating the enablement of the paste action
	 */
    protected boolean canPaste(IGlobalActionContext cntxt) {
        return ClipboardManager.getInstance().doesClipboardHaveData(ClipboardCommand.DRAWING_SURFACE, ClipboardContentsHelper.getInstance()) || (ClipboardManager.getInstance().doesClipboardHaveData(ClipboardManager.COMMON_FORMAT, ClipboardContentsHelper.getInstance()));
    }

    /**
	 * Checks if the selected IElements will allow a print
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean indicating the enablement of the print action
	 */
    private boolean canPrint() {
        return true;
    }

    /**
	 * Checks if the selected IElements will allow a save. Save should only be
	 * enabled when no shapes are selected to avoid clutter on the context menu
	 * and if the editor is dirty.
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return boolean indicating the enablement of the save action
	 */
    protected boolean canSave(IGlobalActionContext cntxt) {
        IWorkbenchPart part = cntxt.getActivePart();
        if (part instanceof IDiagramWorkbenchPart && part instanceof IEditorPart && ((IEditorPart) part).isDirty()) {
            return true;
        }
        return false;
    }

    public boolean canHandle(final IGlobalActionContext cntxt) {
        boolean result = false;
        IWorkbenchPart part = cntxt.getActivePart();
        if (!(part instanceof IDiagramWorkbenchPart)) {
            return false;
        }
        if (!(cntxt.getSelection() instanceof IStructuredSelection)) {
            return result;
        }
        String actionId = cntxt.getActionId();
        if (actionId.equals(GlobalActionId.DELETE)) {
            result = canDelete(cntxt);
        } else if (actionId.equals(GlobalActionId.COPY)) {
            result = canCopy(cntxt);
        } else if (actionId.equals(GlobalActionId.CUT)) {
            result = canCut(cntxt);
        } else if (actionId.equals(GlobalActionId.OPEN)) {
            result = canOpen(cntxt);
        } else if (actionId.equals(GlobalActionId.PASTE)) {
            result = canPaste(cntxt);
        } else if (actionId.equals(GlobalActionId.PRINT)) {
            result = canPrint();
        } else if (actionId.equals(GlobalActionId.SAVE)) {
            result = canSave(cntxt);
        } else if (actionId.equals(GlobalActionId.PROPERTIES)) {
            result = true;
        }
        return result;
    }

    /**
	 * Select the newly added shape view by default.
	 * 
	 * @param viewer
	 *            the viewer owning the edit parts to be selected
	 * @param objects
	 *            the collection of object from which to extract the
	 *            <code>EditPart</code> to select
	 */
    protected void selectAddedObject(EditPartViewer viewer, Collection objects) {
        final List editparts = new ArrayList();
        for (Iterator i = objects.iterator(); i.hasNext(); ) {
            EditPart editPart = getEditPart(viewer, i.next());
            if (editPart != null && editPart.isSelectable()) editparts.add(editPart);
        }
        if (!editparts.isEmpty()) {
            viewer.setSelection(new StructuredSelection(editparts));
            viewer.reveal((EditPart) editparts.get(0));
        }
    }

    private EditPart getEditPart(EditPartViewer viewer, Object object) {
        if (object instanceof View) {
            return (EditPart) viewer.getEditPartRegistry().get(object);
        } else if (object instanceof IAdaptable) {
            return (EditPart) viewer.getEditPartRegistry().get(((IAdaptable) object).getAdapter(View.class));
        }
        return null;
    }

    /**
	 * Retrieve the <code>IMapMode</code> object from the
	 * <code>DiagramRootEditPart</code>
	 * 
	 * @param cntxt
	 *            the <code>IGlobalActionContext</code> holding the necessary
	 *            information needed by this action handler
	 * @return <code>IMapMode</code> object that performs coordinate mapping
	 *         from device to logical. Returns null if the context isn't valid.
	 */
    protected IMapMode getMapMode(IGlobalActionContext cntxt) {
        IWorkbenchPart part = cntxt.getActivePart();
        if (!(part instanceof IDiagramWorkbenchPart)) {
            RootEditPart rootEP = ((IDiagramWorkbenchPart) part).getDiagramGraphicalViewer().getRootEditPart();
            if (rootEP instanceof DiagramRootEditPart) {
                return ((DiagramRootEditPart) part).getMapMode();
            }
        }
        return MapModeUtil.getMapMode();
    }

    /**
     * Gets the transactional editing domain associated with the workbench
     * <code>part</code>.
     * 
     * @param part
     *            the diagram workbench part
     * @return the editing domain, or <code>null</code> if there is none.
     */
    private TransactionalEditingDomain getEditingDomain(IDiagramWorkbenchPart part) {
        TransactionalEditingDomain result = null;
        IEditingDomainProvider provider = (IEditingDomainProvider) part.getAdapter(IEditingDomainProvider.class);
        if (provider != null) {
            EditingDomain domain = provider.getEditingDomain();
            if (domain != null && domain instanceof TransactionalEditingDomain) {
                result = (TransactionalEditingDomain) domain;
            }
        }
        return result;
    }
}
