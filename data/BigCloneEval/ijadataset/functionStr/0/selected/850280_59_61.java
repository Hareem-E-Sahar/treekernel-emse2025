public class Test {    public Iterator<ClientSession> getSessions() {
        return getChannel().getSessions();
    }
}