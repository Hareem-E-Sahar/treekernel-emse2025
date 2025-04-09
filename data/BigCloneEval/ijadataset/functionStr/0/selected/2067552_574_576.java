public class Test {    protected int getChannelForStreamId(int streamId) {
        return (streamId - 1) * 5 + 4;
    }
}