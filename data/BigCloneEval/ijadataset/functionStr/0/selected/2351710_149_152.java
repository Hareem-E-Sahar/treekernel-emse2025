public class Test {    public void stopThread() {
        io.getOut().write("Stopping ExecutorThread...");
        this.running = false;
    }
}