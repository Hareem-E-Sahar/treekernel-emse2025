public class Test {    public Channel join(final Set<? extends ClientSession> sessions) {
        getChannel().join(sessions);
        return this;
    }
}