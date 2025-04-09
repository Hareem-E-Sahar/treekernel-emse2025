public class Test {    public SyncSet(Set set, ReadWriteLock rwl) {
        super(set, rwl.readLock(), rwl.writeLock());
    }
}