public class Test {    public Channel getChannel(String chanName) {
        return channels.get(chanName.toLowerCase());
    }
}