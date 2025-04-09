public class Test {    private void stopThread() {
        runit = false;
        timerThread.interrupt();
        writeMessage("Stopped thread.");
    }
}