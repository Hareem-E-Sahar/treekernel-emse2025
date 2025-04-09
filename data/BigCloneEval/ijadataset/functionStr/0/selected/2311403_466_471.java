public class Test {    private void onWcWrittenHeader() {
        logger.debug("#writtenRequestHeader cid:" + getChannelId());
        if (wsClient != null) {
            wsClient.onWcWrittenHeader(userContext);
        }
    }
}