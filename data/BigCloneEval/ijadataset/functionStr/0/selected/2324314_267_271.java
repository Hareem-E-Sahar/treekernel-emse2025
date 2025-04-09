public class Test {    public Channel getChannel() {
        synchronized (this) {
            return _channel;
        }
    }
}