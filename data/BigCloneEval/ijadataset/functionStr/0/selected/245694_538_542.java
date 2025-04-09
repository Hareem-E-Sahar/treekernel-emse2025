public class Test {        protected SelectableChannel getChannel() {
            synchronized (sync) {
                return dch;
            }
        }
}