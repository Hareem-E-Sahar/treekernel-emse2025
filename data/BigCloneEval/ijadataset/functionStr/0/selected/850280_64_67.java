public class Test {    public Channel join(final ClientSession session) {
        getChannel().join(session);
        return this;
    }
}