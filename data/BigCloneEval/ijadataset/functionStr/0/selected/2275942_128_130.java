public class Test {    public ImageProcessor getChannelProcessor() {
        if (cip != null && currentChannel != -1) return cip[currentChannel]; else return getProcessor();
    }
}