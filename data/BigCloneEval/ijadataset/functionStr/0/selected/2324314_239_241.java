public class Test {    public String getChannelString() {
        return _nodeChannelRef != null ? _nodeChannelRef.toString() : getPV();
    }
}