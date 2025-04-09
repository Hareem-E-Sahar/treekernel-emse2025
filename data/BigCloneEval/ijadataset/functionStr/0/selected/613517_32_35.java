public class Test {    public ResponseTimeMonitorThread() {
        channel = ChannelFactory.instance().getChannel();
        monitors = new LinkedList();
    }
}