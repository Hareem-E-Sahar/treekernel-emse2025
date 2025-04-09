public class Test {    public int getChannels() {
        if (hasReadHeaders()) {
            return audioFormat.getChannels();
        }
        return 2;
    }
}