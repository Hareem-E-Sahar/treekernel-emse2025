public class Test {    @Override
    public Channel<V> getChannel() {
        return wrapped.getChannel();
    }
}