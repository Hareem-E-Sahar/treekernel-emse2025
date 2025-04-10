public class Test {    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        ScrollingGraphicalViewer scrollingGraphicalViewer = (ScrollingGraphicalViewer) getGraphicalViewer();
        root = new ERDScalableFreeformRootEditPart();
        scrollingGraphicalViewer.setRootEditPart(root);
        scrollingGraphicalViewer.setEditPartFactory(new ERPartFactory());
        scrollingGraphicalViewer.setKeyHandler((new GraphicalViewerKeyHandler(scrollingGraphicalViewer)).setParent(getCommonKeyHandler()));
        ZoomManager zoomManager = root.getZoomManager();
        List<String> zoomLevelContributions = new ArrayList<String>();
        zoomLevelContributions.add(ZoomManager.FIT_ALL);
        zoomLevelContributions.add(ZoomManager.FIT_WIDTH);
        zoomLevelContributions.add(ZoomManager.FIT_HEIGHT);
        zoomManager.setZoomLevelContributions(zoomLevelContributions);
        double ad[] = { 0.01D, 0.10D, 0.25D, 0.5D, 0.75D, 1.0D, 1.5D, 2D, 2.5D, 3D, 4D };
        zoomManager.setZoomLevels(ad);
        ERDContextMenuProvider menuProvider = new ERDContextMenuProvider(scrollingGraphicalViewer, getActionRegistry(), this);
        scrollingGraphicalViewer.setContextMenu(menuProvider);
        loadProperties();
        ZoomInAction zoomInAction = new ZoomInAction(root.getZoomManager());
        ZoomOutAction zoomOutAction = new ZoomOutAction(root.getZoomManager());
        getActionRegistry().registerAction(zoomInAction);
        getActionRegistry().registerAction(zoomOutAction);
        getSite().getKeyBindingService().registerAction(zoomInAction);
        getSite().getKeyBindingService().registerAction(zoomOutAction);
        ToggleGridVisibilityAction togglegridvisibilityaction = new ToggleGridVisibilityAction(this, getGraphicalViewer());
        getActionRegistry().registerAction(togglegridvisibilityaction);
        IAction action = new ToggleSnapToGridAction(this, getGraphicalViewer());
        getActionRegistry().registerAction(action);
        action = new ToggleLabelVisibilitAction(diagram);
        getActionRegistry().registerAction(action);
    }
}