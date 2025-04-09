public class Test {        public ChannelUpstreamHandler getChannelUpstreamHandler() {
            return new NettyRpcClientChannelUpstreamHandler();
        }
}