public class Test {    @Override
    protected MenuManager getContextMenu() {
        return new MyContextMenuProvider(this.getGraphicalViewer(), this.getActionRegistry());
    }
}