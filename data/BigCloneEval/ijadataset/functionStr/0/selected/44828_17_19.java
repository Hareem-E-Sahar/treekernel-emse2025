public class Test {    public synchronized void write(Packet packet) {
        status.getChannelAbsoluteTime(packet.getHeader());
    }
}