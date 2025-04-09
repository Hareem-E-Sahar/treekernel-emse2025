public class Test {        @Override
        protected AbstractSelectableChannel getChannel() throws IOException {
            return sockEmul;
        }
}