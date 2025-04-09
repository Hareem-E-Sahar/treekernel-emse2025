public class Test {            public void run() {
                threadAssertFalse(lock.writeLock().tryLock());
            }
}