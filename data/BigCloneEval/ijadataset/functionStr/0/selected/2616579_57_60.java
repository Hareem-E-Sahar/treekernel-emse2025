public class Test {    @Override
    protected void execute(Command command) {
        this.editor.getGraphicalViewer().getEditDomain().getCommandStack().execute(command);
    }
}