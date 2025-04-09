public class Test {    @Override
    public void dispose() {
        server.getChannelManager().removeChannel(getID());
    }
}