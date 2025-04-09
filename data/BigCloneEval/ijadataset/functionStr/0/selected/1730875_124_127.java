public class Test {    public void wakeThreads() {
        readThread.interrupt();
        writeThread.interrupt();
    }
}