public class Test {    protected void shutdown() {
        reader.stopThread();
        writer.stopThread();
        this.interrupt();
    }
}