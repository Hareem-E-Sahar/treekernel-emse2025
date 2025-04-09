public class Test {    public Channel leave(final ClientSession session) {
        getChannel().leave(session);
        return this;
    }
}