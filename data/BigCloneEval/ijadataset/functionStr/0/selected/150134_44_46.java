public class Test {    public synchronized long readPendingCount() {
        return readCount - writeCount;
    }
}