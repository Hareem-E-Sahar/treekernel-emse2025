public class Test {    @Override
    public void run() {
        GraphicalViewer viewer = (GraphicalViewer) part.getAdapter(GraphicalViewer.class);
        if (viewer != null) {
            viewer.setSelection(new StructuredSelection(getSelectableEditParts(viewer).toArray()));
        }
    }
}