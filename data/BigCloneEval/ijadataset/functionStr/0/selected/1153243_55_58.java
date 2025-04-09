public class Test {    @Override
    public void onWsClose(short code, String reason) {
        logger.debug("#wsClose cid:" + getChannelId());
    }
}