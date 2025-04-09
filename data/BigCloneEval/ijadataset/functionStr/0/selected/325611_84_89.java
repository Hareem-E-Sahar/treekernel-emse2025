public class Test {    public Channel getChannelAt(int index) {
        if (channels == null) {
            return null;
        }
        return channels.elementAt(index);
    }
}