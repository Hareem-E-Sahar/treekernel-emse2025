public class Test {    public void sendNoteOff(int channel, int key, int velocity) {
        byte[] event = new byte[3];
        event[0] = (byte) (0x80 | this.jackOutputPort.getRouter().getChannelRoute(channel));
        event[1] = (byte) key;
        event[2] = (byte) velocity;
        this.jackClient.addEventToQueue(this.jackOutputPort.getRouter().getPortRoute(channel), event);
    }
}