public class Test {    public Scatter2DVariableSelector(vademecum.visualizer.D2.scatter.ScatterPlot2D plot2D) {
        super((JFrame) plot2D.getFigurePanel().getGraphicalViewer());
        setTitle("Variable Selector");
        scatter2D = plot2D;
        init();
    }
}