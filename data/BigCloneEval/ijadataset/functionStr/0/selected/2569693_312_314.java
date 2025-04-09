public class Test {            public void dragFinished(DragSourceEvent event) {
                getEventDispatcher().dispatchNativeDragFinished(event, GraphicalViewerImpl.this);
            }
}