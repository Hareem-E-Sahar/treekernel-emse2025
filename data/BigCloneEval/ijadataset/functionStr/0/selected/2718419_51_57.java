public class Test {    @Override
    public void createPartControl(Composite parent) {
        createGraphicalViewer(parent);
        makeActions();
        hookContextMenu();
        contributeToActionBars();
    }
}