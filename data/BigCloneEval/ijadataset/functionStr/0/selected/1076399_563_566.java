public class Test {        @Override
        public MulticastChannel getChannel() {
            return AsyncDatagramChannelImpl.this;
        }
}