public class Test {                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    e.getChannel().close();
                }
}