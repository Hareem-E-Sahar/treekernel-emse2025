public class Test {    protected ReadWriteLockBase() {
        readWriteLock = createReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }
}