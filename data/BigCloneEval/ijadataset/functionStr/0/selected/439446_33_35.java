public class Test {    public void shutdown() throws IOException {
        getChannel().send(SHUTDOWN, Value.create());
    }
}