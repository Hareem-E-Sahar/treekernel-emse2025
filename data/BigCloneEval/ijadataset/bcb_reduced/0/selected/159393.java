package de.mpiwg.vspace.vspacemaps.diagram.part;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import de.mpiwg.vspace.maps.metamodel.vspacemaps.VSpaceMap;
import de.mpiwg.vspace.maps.metamodel.vspacemaps.VspacemapsFactory;
import de.mpiwg.vspace.vspacemaps.diagram.edit.parts.VSpaceMapEditPart;

/**
 * @generated
 */
public class VspacemapsDiagramEditorUtil {

    /**
	 * @generated
	 */
    public static Map<?, ?> getSaveOptions() {
        HashMap<String, Object> saveOptions = new HashMap<String, Object>();
        saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
        saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
        return saveOptions;
    }

    /**
	 * @generated
	 */
    public static boolean openDiagram(Resource diagram) throws PartInitException {
        String path = diagram.getURI().toPlatformString(true);
        IResource workspaceResource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
        if (workspaceResource instanceof IFile) {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            return null != page.openEditor(new FileEditorInput((IFile) workspaceResource), VspacemapsDiagramEditor.ID);
        }
        return false;
    }

    /**
	 * @generated
	 */
    public static void setCharset(IFile file) {
        if (file == null) {
            return;
        }
        try {
            file.setCharset("UTF-8", new NullProgressMonitor());
        } catch (CoreException e) {
            VspacemapsDiagramEditorPlugin.getInstance().logError("Unable to set charset for file " + file.getFullPath(), e);
        }
    }

    /**
	 * @generated
	 */
    public static String getUniqueFileName(IPath containerFullPath, String fileName, String extension) {
        if (containerFullPath == null) {
            containerFullPath = new Path("");
        }
        if (fileName == null || fileName.trim().length() == 0) {
            fileName = "default";
        }
        IPath filePath = containerFullPath.append(fileName);
        if (extension != null && !extension.equals(filePath.getFileExtension())) {
            filePath = filePath.addFileExtension(extension);
        }
        extension = filePath.getFileExtension();
        fileName = filePath.removeFileExtension().lastSegment();
        int i = 1;
        while (ResourcesPlugin.getWorkspace().getRoot().exists(filePath)) {
            i++;
            filePath = containerFullPath.append(fileName + i);
            if (extension != null) {
                filePath = filePath.addFileExtension(extension);
            }
        }
        return filePath.lastSegment();
    }

    /**
	 * Runs the wizard in a dialog.
	 * 
	 * @generated
	 */
    public static void runWizard(Shell shell, Wizard wizard, String settingsKey) {
        IDialogSettings pluginDialogSettings = VspacemapsDiagramEditorPlugin.getInstance().getDialogSettings();
        IDialogSettings wizardDialogSettings = pluginDialogSettings.getSection(settingsKey);
        if (wizardDialogSettings == null) {
            wizardDialogSettings = pluginDialogSettings.addNewSection(settingsKey);
        }
        wizard.setDialogSettings(wizardDialogSettings);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.create();
        dialog.getShell().setSize(Math.max(500, dialog.getShell().getSize().x), 500);
        dialog.open();
    }

    /**
	 * This method should be called within a workspace modify operation since it creates resources.
	 * @generated
	 */
    public static Resource createDiagram(URI diagramURI, URI modelURI, IProgressMonitor progressMonitor) {
        TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE.createEditingDomain();
        progressMonitor.beginTask(Messages.VspacemapsDiagramEditorUtil_CreateDiagramProgressTask, 3);
        final Resource diagramResource = editingDomain.getResourceSet().createResource(diagramURI);
        final Resource modelResource = editingDomain.getResourceSet().createResource(modelURI);
        final String diagramName = diagramURI.lastSegment();
        AbstractTransactionalCommand command = new AbstractTransactionalCommand(editingDomain, Messages.VspacemapsDiagramEditorUtil_CreateDiagramCommandLabel, Collections.EMPTY_LIST) {

            protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                VSpaceMap model = createInitialModel();
                attachModelToResource(model, modelResource);
                Diagram diagram = ViewService.createDiagram(model, VSpaceMapEditPart.MODEL_ID, VspacemapsDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
                if (diagram != null) {
                    diagramResource.getContents().add(diagram);
                    diagram.setName(diagramName);
                    diagram.setElement(model);
                }
                try {
                    modelResource.save(de.mpiwg.vspace.vspacemaps.diagram.part.VspacemapsDiagramEditorUtil.getSaveOptions());
                    diagramResource.save(de.mpiwg.vspace.vspacemaps.diagram.part.VspacemapsDiagramEditorUtil.getSaveOptions());
                } catch (IOException e) {
                    VspacemapsDiagramEditorPlugin.getInstance().logError("Unable to store model and diagram resources", e);
                }
                return CommandResult.newOKCommandResult();
            }
        };
        try {
            OperationHistoryFactory.getOperationHistory().execute(command, new SubProgressMonitor(progressMonitor, 1), null);
        } catch (ExecutionException e) {
            VspacemapsDiagramEditorPlugin.getInstance().logError("Unable to create model and diagram", e);
        }
        setCharset(WorkspaceSynchronizer.getFile(modelResource));
        setCharset(WorkspaceSynchronizer.getFile(diagramResource));
        return diagramResource;
    }

    /**
	 * Create a new instance of domain element associated with canvas.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
    private static VSpaceMap createInitialModel() {
        return VspacemapsFactory.eINSTANCE.createVSpaceMap();
    }

    /**
	 * Store model element in the resource.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
    private static void attachModelToResource(VSpaceMap model, Resource resource) {
        resource.getContents().add(model);
    }

    /**
	 * @generated
	 */
    public static void selectElementsInDiagram(IDiagramWorkbenchPart diagramPart, List<EditPart> editParts) {
        diagramPart.getDiagramGraphicalViewer().deselectAll();
        EditPart firstPrimary = null;
        for (EditPart nextPart : editParts) {
            diagramPart.getDiagramGraphicalViewer().appendSelection(nextPart);
            if (firstPrimary == null && nextPart instanceof IPrimaryEditPart) {
                firstPrimary = nextPart;
            }
        }
        if (!editParts.isEmpty()) {
            diagramPart.getDiagramGraphicalViewer().reveal(firstPrimary != null ? firstPrimary : (EditPart) editParts.get(0));
        }
    }

    /**
	 * @generated
	 */
    private static int findElementsInDiagramByID(DiagramEditPart diagramPart, EObject element, List<EditPart> editPartCollector) {
        IDiagramGraphicalViewer viewer = (IDiagramGraphicalViewer) diagramPart.getViewer();
        final int intialNumOfEditParts = editPartCollector.size();
        if (element instanceof View) {
            EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(element);
            if (editPart != null) {
                editPartCollector.add(editPart);
                return 1;
            }
        }
        String elementID = EMFCoreUtil.getProxyID(element);
        @SuppressWarnings("unchecked") List<EditPart> associatedParts = viewer.findEditPartsForElement(elementID, IGraphicalEditPart.class);
        for (EditPart nextPart : associatedParts) {
            EditPart parentPart = nextPart.getParent();
            while (parentPart != null && !associatedParts.contains(parentPart)) {
                parentPart = parentPart.getParent();
            }
            if (parentPart == null) {
                editPartCollector.add(nextPart);
            }
        }
        if (intialNumOfEditParts == editPartCollector.size()) {
            if (!associatedParts.isEmpty()) {
                editPartCollector.add(associatedParts.get(0));
            } else {
                if (element.eContainer() != null) {
                    return findElementsInDiagramByID(diagramPart, element.eContainer(), editPartCollector);
                }
            }
        }
        return editPartCollector.size() - intialNumOfEditParts;
    }

    /**
	 * @generated
	 */
    public static View findView(DiagramEditPart diagramEditPart, EObject targetElement, LazyElement2ViewMap lazyElement2ViewMap) {
        boolean hasStructuralURI = false;
        if (targetElement.eResource() instanceof XMLResource) {
            hasStructuralURI = ((XMLResource) targetElement.eResource()).getID(targetElement) == null;
        }
        View view = null;
        LinkedList<EditPart> editPartHolder = new LinkedList<EditPart>();
        if (hasStructuralURI && !lazyElement2ViewMap.getElement2ViewMap().isEmpty()) {
            view = lazyElement2ViewMap.getElement2ViewMap().get(targetElement);
        } else if (findElementsInDiagramByID(diagramEditPart, targetElement, editPartHolder) > 0) {
            EditPart editPart = editPartHolder.get(0);
            view = editPart.getModel() instanceof View ? (View) editPart.getModel() : null;
        }
        return (view == null) ? diagramEditPart.getDiagramView() : view;
    }

    /**
	 * XXX This is quite suspicious code (especially editPartTmpHolder) and likely to be removed soon
	 * @generated
	 */
    public static class LazyElement2ViewMap {

        /**
		 * @generated
		 */
        private Map<EObject, View> element2ViewMap;

        /**
		 * @generated
		 */
        private View scope;

        /**
		 * @generated
		 */
        private Set<? extends EObject> elementSet;

        /**
		 * @generated
		 */
        public LazyElement2ViewMap(View scope, Set<? extends EObject> elements) {
            this.scope = scope;
            this.elementSet = elements;
        }

        /**
		 * @generated
		 */
        public final Map<EObject, View> getElement2ViewMap() {
            if (element2ViewMap == null) {
                element2ViewMap = new HashMap<EObject, View>();
                for (EObject element : elementSet) {
                    if (element instanceof View) {
                        View view = (View) element;
                        if (view.getDiagram() == scope.getDiagram()) {
                            element2ViewMap.put(element, view);
                        }
                    }
                }
                buildElement2ViewMap(scope, element2ViewMap, elementSet);
            }
            return element2ViewMap;
        }

        /**
		 * @generated
		 */
        private static boolean buildElement2ViewMap(View parentView, Map<EObject, View> element2ViewMap, Set<? extends EObject> elements) {
            if (elements.size() == element2ViewMap.size()) {
                return true;
            }
            if (parentView.isSetElement() && !element2ViewMap.containsKey(parentView.getElement()) && elements.contains(parentView.getElement())) {
                element2ViewMap.put(parentView.getElement(), parentView);
                if (elements.size() == element2ViewMap.size()) {
                    return true;
                }
            }
            boolean complete = false;
            for (Iterator<?> it = parentView.getChildren().iterator(); it.hasNext() && !complete; ) {
                complete = buildElement2ViewMap((View) it.next(), element2ViewMap, elements);
            }
            for (Iterator<?> it = parentView.getSourceEdges().iterator(); it.hasNext() && !complete; ) {
                complete = buildElement2ViewMap((View) it.next(), element2ViewMap, elements);
            }
            for (Iterator<?> it = parentView.getTargetEdges().iterator(); it.hasNext() && !complete; ) {
                complete = buildElement2ViewMap((View) it.next(), element2ViewMap, elements);
            }
            return complete;
        }
    }
}
