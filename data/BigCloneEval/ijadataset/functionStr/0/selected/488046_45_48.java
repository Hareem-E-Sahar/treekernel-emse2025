public class Test {    @Override
    public void endWrite() {
        readWriteLock.writeLock().unlock();
    }
}