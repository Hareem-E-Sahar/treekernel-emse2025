public class Test {    public boolean isDisconnected() {
        return !getChannel().isConnected();
    }
}