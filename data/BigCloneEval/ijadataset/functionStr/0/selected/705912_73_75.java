public class Test {    private TGChannel getChannel() {
        return TuxGuitar.instance().getSongManager().getChannel(this.channelId);
    }
}