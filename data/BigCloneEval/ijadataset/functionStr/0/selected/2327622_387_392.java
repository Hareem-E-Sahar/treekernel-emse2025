public class Test {    public int getChannelLength() {
        if (atracChannel == null) {
            return atracFileSize;
        }
        return atracChannel.length();
    }
}