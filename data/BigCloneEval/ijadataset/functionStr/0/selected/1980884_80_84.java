public class Test {    @Override
    protected void initializeGraphicalViewer() {
        getGraphicalViewer().addDropTargetListener(new ChartTemplateTransferDropTargetListener(getGraphicalViewer()));
        drawChart();
    }
}