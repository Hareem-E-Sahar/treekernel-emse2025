public class Test {    protected void onChannelData(SshMsgChannelData msg) throws IOException {
        try {
            socket.getOutputStream().write(msg.getChannelData());
        } catch (IOException ex) {
        }
    }
}