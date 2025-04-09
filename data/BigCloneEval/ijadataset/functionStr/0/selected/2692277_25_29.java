public class Test {    public PrintAction(IWorkbenchPart part, GraphicalViewer graphicalViewer) {
        super(part);
        myGraphicalViewer = graphicalViewer;
        setActionDefinitionId(ID);
    }
}