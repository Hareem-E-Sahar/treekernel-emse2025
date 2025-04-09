public class Test {    ChannelStream createSource() throws IOException {
        return info.srcProtocol.getChannelStream(this);
    }
}