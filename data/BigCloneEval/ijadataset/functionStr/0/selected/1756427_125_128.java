public class Test {    protected void initializeGraphicalViewerContents() {
        super.initializeGraphicalViewerContents();
        getDiagramGraphicalViewer().addDropTargetListener(new DropTargetListener());
    }
}