public class Test {        public void stop() {
            running = false;
            writerThread.interrupt();
        }
}