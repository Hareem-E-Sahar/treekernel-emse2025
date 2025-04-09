public class Test {    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getGraphicalViewer().addDropTargetListener(createTransferDropTargetListener());
    }
}