public class Test {    public ExportToExcelDialog(Shell parentShell, ERDiagram diagram, IEditorPart editorPart, GraphicalViewer viewer) {
        super(parentShell, 3);
        this.diagram = diagram;
        this.editorPart = editorPart;
        this.viewer = viewer;
    }
}