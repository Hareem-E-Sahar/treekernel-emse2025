public class Test {    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getGraphicalViewer().setEditPartFactory(new HumanDiagramEditPartFactory());
    }
}