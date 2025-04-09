public class Test {    public synchronized long writePendingCount() {
        return writeCount - readCount;
    }
}