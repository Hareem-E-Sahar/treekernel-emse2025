public class Test {    public SendBackwardAction(IWorkbenchPart part) {
        super(part);
        setText(TEXT);
        setId(ID);
        setSelectionProvider((ISelectionProvider) part.getAdapter(GraphicalViewer.class));
    }
}