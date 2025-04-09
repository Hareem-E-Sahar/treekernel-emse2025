public class Test {    public int getChannelStatus() throws AgiException {
        sendCommand(new ChannelStatusCommand());
        return lastReply.getResultCode();
    }
}