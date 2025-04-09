public class Test {    protected void populateChannelLines() {
        flowLines = null;
        if (!visibilityControl.getHideChannels().getValue()) {
            getChannelManager().addLines();
        }
    }
}