public class Test {    public GraphViewer getGraphicalViewer() {
        if (graphicalViewer != null) {
            return graphicalViewer;
        }
        throw new NullPointerException();
    }
}