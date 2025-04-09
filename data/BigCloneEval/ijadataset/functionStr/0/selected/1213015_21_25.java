public class Test {    public void unregisterSession(SessionContext session) {
        synchronized (this.sessions) {
            this.sessions.remove(session.getChannel().getId());
        }
    }
}