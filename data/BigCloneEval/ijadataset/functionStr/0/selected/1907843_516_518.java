public class Test {    public SimpleChannelSource(final String pv) {
        this(ChannelFactory.defaultFactory().getChannel(pv));
    }
}