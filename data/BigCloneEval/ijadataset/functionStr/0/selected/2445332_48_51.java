public class Test {    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channels.remove(e.getChannel());
    }
}