public class Test {    public GraphicalViewerImpl() {
        createDefaultRoot();
        setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE), MouseWheelDelegateHandler.SINGLETON);
    }
}