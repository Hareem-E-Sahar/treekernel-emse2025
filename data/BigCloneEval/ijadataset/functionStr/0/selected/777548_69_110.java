public class Test {    public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
        if (aSelection == null || aSelection.isEmpty()) {
            return;
        }
        if (false == aSelection.getFirstElement() instanceof DrlModelAbstractNavigatorItem) {
            return;
        }
        DrlModelAbstractNavigatorItem abstractNavigatorItem = (DrlModelAbstractNavigatorItem) aSelection.getFirstElement();
        View navigatorView = null;
        if (abstractNavigatorItem instanceof DrlModelNavigatorItem) {
            navigatorView = ((DrlModelNavigatorItem) abstractNavigatorItem).getView();
        } else if (abstractNavigatorItem instanceof DrlModelNavigatorGroup) {
            DrlModelNavigatorGroup navigatorGroup = (DrlModelNavigatorGroup) abstractNavigatorItem;
            if (navigatorGroup.getParent() instanceof DrlModelNavigatorItem) {
                navigatorView = ((DrlModelNavigatorItem) navigatorGroup.getParent()).getView();
            } else if (navigatorGroup.getParent() instanceof IAdaptable) {
                navigatorView = (View) ((IAdaptable) navigatorGroup.getParent()).getAdapter(View.class);
            }
        }
        if (navigatorView == null) {
            return;
        }
        IEditorInput editorInput = getEditorInput(navigatorView.getDiagram());
        IEditorPart editor = aPage.findEditor(editorInput);
        if (editor == null) {
            return;
        }
        aPage.bringToTop(editor);
        if (editor instanceof DiagramEditor) {
            DiagramEditor diagramEditor = (DiagramEditor) editor;
            ResourceSet diagramEditorResourceSet = diagramEditor.getEditingDomain().getResourceSet();
            EObject selectedView = diagramEditorResourceSet.getEObject(EcoreUtil.getURI(navigatorView), true);
            if (selectedView == null) {
                return;
            }
            GraphicalViewer graphicalViewer = (GraphicalViewer) diagramEditor.getAdapter(GraphicalViewer.class);
            EditPart selectedEditPart = (EditPart) graphicalViewer.getEditPartRegistry().get(selectedView);
            if (selectedEditPart != null) {
                graphicalViewer.select(selectedEditPart);
            }
        }
    }
}