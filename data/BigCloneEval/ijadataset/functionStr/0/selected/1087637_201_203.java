public class Test {    public synchronized int getChannel(int tunerNumber) {
        return tunedChannels[tunerNumber].getNumber();
    }
}