public class Test {    public ChannelWrapper(final String pv) {
        _channel = ChannelFactory.defaultFactory().getChannel(pv);
    }
}