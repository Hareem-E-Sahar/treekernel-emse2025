public class Test {    public void setActiveEditor(DiagramEditor editor) {
        if (myPopupDialog != null) {
            myPopupDialog.setInput(myGraphicalViewer);
        }
    }
}