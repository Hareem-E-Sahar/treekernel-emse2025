public class Test {    public ChannelWrapper(String pv) {
        this(ChannelFactory.defaultFactory().getChannel(pv));
    }
}