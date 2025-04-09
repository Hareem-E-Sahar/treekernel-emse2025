public class Test {    public ShowInContext getShowInContext() {
        return new ShowInContext(getEditorInput(), getGraphicalViewer().getSelection());
    }
}