public class Test {    public void acquireWriter(final Thread writerThread) {
        _writers++;
        _writersThreads.add(writerThread);
    }
}