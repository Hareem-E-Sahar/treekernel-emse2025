public class Test {    protected void sendDataFromForwarderOut(BigEndianHeapChannelBuffer buffer) {
        Globals.getInstance().getForwarder().getChannel().write(buffer);
    }
}