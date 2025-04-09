public class Test {    public Action getChannelUpAction() {
        if (this.channelUpAction == null) {
            this.channelUpAction = new ChannelUpAction();
        }
        return channelUpAction;
    }
}