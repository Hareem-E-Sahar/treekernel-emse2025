public class Test {            @Override
            public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                handleClientGone((InetSocketAddress) e.getChannel().getRemoteAddress());
            }
}