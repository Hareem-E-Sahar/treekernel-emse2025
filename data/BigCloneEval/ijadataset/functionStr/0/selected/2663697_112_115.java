public class Test {    protected void initializeGraphicalViewer() {
        getGraphicalViewer().setContents(diagram);
        getGraphicalViewer().addDropTargetListener(new TemplateTransferDropTargetListener(getGraphicalViewer()));
    }
}