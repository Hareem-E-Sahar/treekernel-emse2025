public class Test {    public Channel getChannel() {
        return getOutboundTransport().getChannel(this);
    }
}