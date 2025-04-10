public class Test {    @SuppressWarnings("unchecked")
    protected void createActions(GraphicalViewer viewer) {
        ActionRegistry registry = getActionRegistry();
        IAction action;
        ZoomManager zoomManager = (ZoomManager) getAdapter(ZoomManager.class);
        double[] zoomLevels = { .25, .5, .75, 1.0, 1.5, 2.0, 2.5, 3, 4 };
        zoomManager.setZoomLevels(zoomLevels);
        List<String> zoomContributionLevels = new ArrayList<String>();
        zoomContributionLevels.add(ZoomManager.FIT_ALL);
        zoomContributionLevels.add(ZoomManager.FIT_WIDTH);
        zoomContributionLevels.add(ZoomManager.FIT_HEIGHT);
        zoomManager.setZoomLevelContributions(zoomContributionLevels);
        IAction zoomIn = new ZoomInAction(zoomManager);
        IAction zoomOut = new ZoomOutAction(zoomManager);
        registry.registerAction(zoomIn);
        registry.registerAction(zoomOut);
        IHandlerService service = (IHandlerService) getEditorSite().getService(IHandlerService.class);
        service.activateHandler(zoomIn.getActionDefinitionId(), new ActionHandler(zoomIn));
        service.activateHandler(zoomOut.getActionDefinitionId(), new ActionHandler(zoomOut));
        action = new SelectAllAction(this);
        registry.registerAction(action);
        action = new PrintDiagramAction(this);
        registry.registerAction(action);
        action = new DirectEditAction(this);
        action.setId(ActionFactory.RENAME.getId());
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = registry.getAction(ActionFactory.DELETE.getId());
        action.setText(Messages.AbstractDiagramEditor_2);
        action.setToolTipText(action.getText());
        getUpdateCommandStackActions().add((UpdateAction) action);
        PasteAction pasteAction = new PasteAction(this, viewer);
        registry.registerAction(pasteAction);
        getSelectionActions().add(pasteAction.getId());
        action = new CutAction(this, pasteAction);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new CopyAction(this, pasteAction);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new ToggleGridEnabledAction();
        registry.registerAction(action);
        action = new ToggleGridVisibleAction();
        registry.registerAction(action);
        action = new ToggleSnapToAlignmentGuidesAction();
        registry.registerAction(action);
        action = new MatchWidthAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new MatchHeightAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.LEFT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.RIGHT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.TOP);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.BOTTOM);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.CENTER);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.MIDDLE);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new DefaultEditPartSizeAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new ResetAspectRatioAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new PropertiesAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        action = new FillColorAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new ConnectionLineWidthAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new ConnectionLineColorAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new FontAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new FontColorAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new ExportAsImageAction(viewer);
        registry.registerAction(action);
        action = new ExportAsImageToClipboardAction(viewer);
        registry.registerAction(action);
        action = new ConnectionRouterAction.BendPointConnectionRouterAction(this);
        registry.registerAction(action);
        action = new ConnectionRouterAction.ShortestPathConnectionRouterAction(this);
        registry.registerAction(action);
        action = new ConnectionRouterAction.ManhattanConnectionRouterAction(this);
        registry.registerAction(action);
        action = new SendBackwardAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new BringForwardAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new SendToBackAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new BringToFrontAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        for (TextAlignmentAction a : TextAlignmentAction.createActions(this)) {
            registry.registerAction(a);
            getSelectionActions().add(a.getId());
            getUpdateCommandStackActions().add(a);
        }
        for (TextPositionAction a : TextPositionAction.createActions(this)) {
            registry.registerAction(a);
            getSelectionActions().add(a.getId());
            getUpdateCommandStackActions().add(a);
        }
        action = new LockObjectAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new BorderColorAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
        getUpdateCommandStackActions().add((UpdateAction) action);
        action = new FullScreenAction(this);
        registry.registerAction(action);
        action = new SelectElementInTreeAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
    }
}