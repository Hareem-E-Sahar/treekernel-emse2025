public class Test {    public AsyncLogger() {
        _writerThread = new WriterThread();
        _writerThread.start();
    }
}