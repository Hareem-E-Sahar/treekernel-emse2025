public class Test {    public boolean onHandshaked() {
        logger.debug("#handshaked client.id:" + getChannelId());
        server.asyncRead(null);
        return true;
    }
}