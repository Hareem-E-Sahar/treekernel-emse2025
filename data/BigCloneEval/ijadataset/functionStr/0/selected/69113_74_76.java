public class Test {    protected float getValidYLength() {
        return ((AClip) getChannelModel().getParent().getParent()).getSampleRate() / 2;
    }
}