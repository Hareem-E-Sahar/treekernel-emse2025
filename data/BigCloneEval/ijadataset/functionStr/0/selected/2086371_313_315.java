public class Test {        synchronized int getWriteHoldCount() {
            return isWriteLockedByCurrentThread() ? writeHolds_ : 0;
        }
}