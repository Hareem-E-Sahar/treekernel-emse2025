public class Test {    private void shutdownSocketAndChannels() {
        shutdownImpl();
        try {
            getChannel().close();
        } catch (IOException ignored) {
        }
    }
}