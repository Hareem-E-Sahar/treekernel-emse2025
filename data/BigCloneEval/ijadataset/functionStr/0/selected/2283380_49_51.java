public class Test {    public Channel getChannel(int channelNo) {
        return new Channel((Channel) channelList.elementAt(channelNo));
    }
}