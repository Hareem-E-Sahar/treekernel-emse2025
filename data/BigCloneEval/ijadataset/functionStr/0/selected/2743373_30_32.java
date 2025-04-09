public class Test {    public void releaseWriteLock() {
        readWriteLock.writeLock().unlock();
    }
}