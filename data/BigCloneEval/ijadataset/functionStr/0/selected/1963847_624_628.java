public class Test {    @Override
    public Object getAdapter(Class type) {
        if (type == ZoomManager.class) return ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
        return super.getAdapter(type);
    }
}