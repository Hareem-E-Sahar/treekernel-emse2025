public class Test {    private DummyClient newClient() {
        DummyClient client = new DummyClient("dummy");
        client.connect(port).login();
        return client;
    }
}