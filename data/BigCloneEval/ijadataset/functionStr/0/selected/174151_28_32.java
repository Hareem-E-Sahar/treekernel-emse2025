public class Test {    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getGraphicalViewer().setEditPartFactory(new ModelEditPartFactory());
    }
}