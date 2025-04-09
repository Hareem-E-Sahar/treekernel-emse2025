public class Test {    @Override
    public void beginWrite() {
        readWriteLock.writeLock().lock();
    }
}