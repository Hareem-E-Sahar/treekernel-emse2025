public class Test {    public void aquireWriteLock() {
        readWriteLock.writeLock().lock();
    }
}