public class Test {    public void onTimeout(Object userContext) {
        logger.debug("#timeout.cid:" + getChannelId());
        asyncClose(userContext);
        super.onTimeout(userContext);
    }
}