public class Test {    public boolean isAnyTrackConnectedToChannel(TGChannel channel) {
        return getManager().isAnyTrackConnectedToChannel(channel.getChannelId());
    }
}