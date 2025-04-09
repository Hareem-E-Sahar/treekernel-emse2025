public class Test {    public Lock writeLock() {
        return readWriteLock.writeLock();
    }
}