public class Test {    public void monoToStereo() {
        if (getChannelCount() < 2) {
            convertTo(ChannelFormat.STEREO);
        }
    }
}