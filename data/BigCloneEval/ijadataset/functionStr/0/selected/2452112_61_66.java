public class Test {    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        GraphicalViewer viewer = configureViewer();
        configureContextMenu(viewer);
    }
}