public class Test {    public DirectChannelSource(final String pv) {
        this(ChannelFactory.defaultFactory().getChannel(pv));
    }
}