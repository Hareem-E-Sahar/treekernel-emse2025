public class Test {    public void addChannel(ChannelEventType type, Method method) {
        getChannelMap().put(type, method);
    }
}