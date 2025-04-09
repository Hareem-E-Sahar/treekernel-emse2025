public class Test {    protected void onGetCurrentChannelResponse(GetCurrentChannelResponse response) {
        this.client.getState().setCurrentChannel(response.getChannel());
    }
}