public class Test {    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        if (report != null) {
            getGraphicalViewer().setContents(report);
        }
    }
}