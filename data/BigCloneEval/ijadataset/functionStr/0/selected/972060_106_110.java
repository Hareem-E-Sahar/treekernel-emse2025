public class Test {    public void destroy() throws Exception {
        if (writerThread != null) {
            writerThread.stopExecution();
        }
    }
}