public class Test {    public SyncSortedSet(SortedSet set, ReadWriteLock rwl) {
        super(set, rwl.readLock(), rwl.writeLock());
    }
}