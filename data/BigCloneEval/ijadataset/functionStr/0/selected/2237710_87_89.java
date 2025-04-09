public class Test {    protected float getValidYLength() {
        return (1 << ((AClip) getChannelModel().getParent().getParent()).getSampleWidth());
    }
}