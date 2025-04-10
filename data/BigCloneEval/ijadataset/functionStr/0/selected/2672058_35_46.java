public class Test {        @Override
        boolean tryLock(Object mutex) {
            if (mutex instanceof ReentrantReadWriteLock) {
                ReentrantReadWriteLock lock = (ReentrantReadWriteLock) mutex;
                if (lock.getReadHoldCount() > 0 && lock.getWriteHoldCount() == 0) throw new IllegalStateException("Lock cannot be upgraded from read to write");
                try {
                    return lock.writeLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return false;
                }
            } else throw new IllegalStateException("Lock cannot be locked within SyncList");
        }
}