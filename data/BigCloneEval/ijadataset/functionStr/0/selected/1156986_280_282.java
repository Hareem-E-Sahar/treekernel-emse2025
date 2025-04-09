public class Test {    public static void removeChannelListener(String name) {
        channelhandler.getChannel(name).addListener(null);
    }
}