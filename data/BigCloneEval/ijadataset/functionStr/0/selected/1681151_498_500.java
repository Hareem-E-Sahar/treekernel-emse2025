public class Test {    protected boolean hasChannelChanges(TGTrackImpl track, int channelId) {
        return (track.getChannelId() != channelId);
    }
}