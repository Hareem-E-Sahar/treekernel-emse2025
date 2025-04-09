public class Test {    public void onFinished() {
        logger.debug("#finished.cid:" + getChannelId());
        responseEnd();
        super.onFinished();
    }
}