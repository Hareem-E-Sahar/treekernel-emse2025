public class Test {    public void onFinished() {
        logger.debug("#finished.cid:" + getChannelId());
        super.onFinished();
    }
}