public class Test {    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        System.out.println("NSDiagramEditor.configureGraphicalViewer");
        GraphicalViewer viewer = getGraphicalViewer();
        viewer.setEditPartFactory(new CDPartFactory(this));
        viewer.setRootEditPart(new ScalableFreeformRootEditPart());
        GraphicalViewerKeyHandler keyHandler = new GraphicalViewerKeyHandler(viewer);
        keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
        viewer.setKeyHandler(keyHandler);
        ContextMenuProvider cmProvider = new ClassDiagramEditorContextMenuProvider(viewer, getActionRegistry());
        viewer.setContextMenu(cmProvider);
        getSite().registerContextMenu(cmProvider, viewer);
    }
}