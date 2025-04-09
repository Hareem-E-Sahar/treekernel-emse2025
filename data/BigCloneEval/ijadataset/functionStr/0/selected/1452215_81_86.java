public class Test {    public String getLastChannel() {
        if (isInChannel()) {
            lastChannel = getChannelOwner();
        }
        return lastChannel;
    }
}