public class Test {    protected String getChannelNumberAsString(Channel channel) {
        return ((ChannelImpl) channel).getNumberAsString();
    }
}