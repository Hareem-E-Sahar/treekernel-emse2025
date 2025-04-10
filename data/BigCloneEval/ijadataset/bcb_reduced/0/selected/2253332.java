package org.spbu.pldoctoolkit.graph.diagram.infproduct.part;

import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gmf.runtime.common.ui.services.marker.MarkerNavigationService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramDropTargetListener;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.editor.FileDiagramEditor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.document.StorageDiagramDocumentProvider;
import org.spbu.pldoctoolkit.graph.diagram.infproduct.edit.parts.DrlModelEditPartFactory;
import org.spbu.pldoctoolkit.graph.diagram.infproduct.navigator.DrlModelNavigatorItem;

/**
 * @generated
 */
public class DrlModelDiagramEditor extends DiagramDocumentEditor implements IGotoMarker {

    /**
	 * @generated
	 */
    public static final String ID = "org.spbu.pldoctoolkit.graph.diagram.infproduct.part.DrlModelDiagramEditorID";

    /**
	 * @generated
	 */
    public DrlModelDiagramEditor() {
        super(true);
    }

    /**
	 * @generated
	 */
    protected PaletteRoot createPaletteRoot(PaletteRoot existingPaletteRoot) {
        PaletteRoot root = super.createPaletteRoot(existingPaletteRoot);
        new DrlModelPaletteFactory().fillPalette(root);
        return root;
    }

    /**
	 * @generated
	 */
    protected void setDocumentProvider(IEditorInput input) {
        if (input instanceof IFileEditorInput || input instanceof URIEditorInput) {
            setDocumentProvider(DrlModelDiagramEditorPlugin.getInstance().getDocumentProvider());
        } else {
            super.setDocumentProvider(input);
        }
    }

    /**
	 * @generated
	 */
    public void gotoMarker(IMarker marker) {
        MarkerNavigationService.getInstance().gotoMarker(this, marker);
    }

    /**
	 * @generated
	 */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
	 * @generated
	 */
    public void doSaveAs() {
        performSaveAs(new NullProgressMonitor());
    }

    /**
	 * @generated
	 */
    protected void performSaveAs(IProgressMonitor progressMonitor) {
        Shell shell = getSite().getShell();
        IEditorInput input = getEditorInput();
        SaveAsDialog dialog = new SaveAsDialog(shell);
        IFile original = input instanceof IFileEditorInput ? ((IFileEditorInput) input).getFile() : null;
        if (original != null) {
            dialog.setOriginalFile(original);
        }
        dialog.create();
        IDocumentProvider provider = getDocumentProvider();
        if (provider == null) {
            return;
        }
        if (provider.isDeleted(input) && original != null) {
            String message = NLS.bind("The original file ''{0}'' has been deleted.", original.getName());
            dialog.setErrorMessage(null);
            dialog.setMessage(message, IMessageProvider.WARNING);
        }
        if (dialog.open() == Window.CANCEL) {
            if (progressMonitor != null) {
                progressMonitor.setCanceled(true);
            }
            return;
        }
        IPath filePath = dialog.getResult();
        if (filePath == null) {
            if (progressMonitor != null) {
                progressMonitor.setCanceled(true);
            }
            return;
        }
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFile file = workspaceRoot.getFile(filePath);
        final IEditorInput newInput = new FileEditorInput(file);
        IEditorMatchingStrategy matchingStrategy = getEditorDescriptor().getEditorMatchingStrategy();
        IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        for (int i = 0; i < editorRefs.length; i++) {
            if (matchingStrategy.matches(editorRefs[i], newInput)) {
                MessageDialog.openWarning(shell, "Problem During Save As...", "Save could not be completed. Target file is already open in another editor.");
                return;
            }
        }
        boolean success = false;
        try {
            provider.aboutToChange(newInput);
            getDocumentProvider(newInput).saveDocument(progressMonitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
            success = true;
        } catch (CoreException x) {
            IStatus status = x.getStatus();
            if (status == null || status.getSeverity() != IStatus.CANCEL) {
                ErrorDialog.openError(shell, "Save Problems", "Could not save file.", x.getStatus());
            }
        } finally {
            provider.changed(newInput);
            if (success) {
                setInput(newInput);
            }
        }
        if (progressMonitor != null) {
            progressMonitor.setCanceled(!success);
        }
    }

    /**
	 * @generated
	 */
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getDiagramGraphicalViewer().addDropTargetListener(new DropTargetListener(getDiagramGraphicalViewer(), LocalSelectionTransfer.getTransfer()) {

            protected Object getJavaObject(TransferData data) {
                return LocalSelectionTransfer.getTransfer().nativeToJava(data);
            }
        });
        getDiagramGraphicalViewer().addDropTargetListener(new DropTargetListener(getDiagramGraphicalViewer(), LocalTransfer.getInstance()) {

            protected Object getJavaObject(TransferData data) {
                return LocalTransfer.getInstance().nativeToJava(data);
            }
        });
    }

    /**
	 * @generated
	 */
    private abstract class DropTargetListener extends DiagramDropTargetListener {

        /**
		 * @generated
		 */
        public DropTargetListener(EditPartViewer viewer, Transfer xfer) {
            super(viewer, xfer);
        }

        /**
		 * @generated
		 */
        protected List getObjectsBeingDropped() {
            TransferData data = getCurrentEvent().currentDataType;
            Collection uris = new HashSet();
            Object transferedObject = getJavaObject(data);
            if (transferedObject instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) transferedObject;
                for (Iterator it = selection.iterator(); it.hasNext(); ) {
                    Object nextSelectedObject = it.next();
                    if (nextSelectedObject instanceof DrlModelNavigatorItem) {
                        View view = ((DrlModelNavigatorItem) nextSelectedObject).getView();
                        nextSelectedObject = view.getElement();
                    } else if (nextSelectedObject instanceof IAdaptable) {
                        IAdaptable adaptable = (IAdaptable) nextSelectedObject;
                        nextSelectedObject = adaptable.getAdapter(EObject.class);
                    }
                    if (nextSelectedObject instanceof EObject) {
                        EObject modelElement = (EObject) nextSelectedObject;
                        Resource modelElementResource = modelElement.eResource();
                        uris.add(modelElementResource.getURI().appendFragment(modelElementResource.getURIFragment(modelElement)));
                    }
                }
            }
            List result = new ArrayList();
            for (Iterator it = uris.iterator(); it.hasNext(); ) {
                URI nextURI = (URI) it.next();
                EObject modelObject = getEditingDomain().getResourceSet().getEObject(nextURI, true);
                result.add(modelObject);
            }
            return result;
        }

        /**
		 * @generated
		 */
        protected abstract Object getJavaObject(TransferData data);
    }

    /**
	 * @generated
	 */
    protected PreferencesHint getPreferencesHint() {
        return DrlModelDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT;
    }

    /**
	 * @generated
	 */
    public String getContributorId() {
        return DrlModelDiagramEditorPlugin.ID;
    }

    /**
	 * @generated
	 */
    protected IDocumentProvider getDocumentProvider(IEditorInput input) {
        if (input instanceof IFileEditorInput || input instanceof URIEditorInput) {
            return DrlModelDiagramEditorPlugin.getInstance().getDocumentProvider();
        }
        return super.getDocumentProvider(input);
    }

    /**
	 * @generated
	 */
    public TransactionalEditingDomain getEditingDomain() {
        IDocument document = getEditorInput() != null ? getDocumentProvider().getDocument(getEditorInput()) : null;
        if (document instanceof IDiagramDocument) {
            return ((IDiagramDocument) document).getEditingDomain();
        }
        return super.getEditingDomain();
    }
}
