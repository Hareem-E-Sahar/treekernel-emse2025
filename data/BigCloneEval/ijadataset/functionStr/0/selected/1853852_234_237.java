public class Test {        public Object getAdapter(Class type) {
            if (type == ZoomManager.class) return getGraphicalViewer().getProperty(ZoomManager.class.toString());
            return null;
        }
}