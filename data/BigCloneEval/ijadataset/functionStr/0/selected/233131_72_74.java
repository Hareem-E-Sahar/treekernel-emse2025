public class Test {        public void operationComplete(ChannelFuture future) {
            remove(future.getChannel());
        }
}