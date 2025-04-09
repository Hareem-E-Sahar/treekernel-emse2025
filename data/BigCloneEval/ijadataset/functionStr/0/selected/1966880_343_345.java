public class Test {    public void publish(Client fromClient, String toChannelId, Object data, String msgId) {
        publish(getChannelId(toChannelId), fromClient, data, msgId);
    }
}