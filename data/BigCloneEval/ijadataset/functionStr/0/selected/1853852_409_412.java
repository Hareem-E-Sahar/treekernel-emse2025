public class Test {    public void resetRoot(Container c) {
        currentRootObject_ = c;
        getGraphicalViewer().setContents(c);
    }
}