public class Test {    private LocalStreamForwarder getChannel() throws IOException {
        if (channel == null) channel = connection.createLocalStreamForwarder(scgiConfig.getHost(), scgiConfig.getPort());
        return channel;
    }
}