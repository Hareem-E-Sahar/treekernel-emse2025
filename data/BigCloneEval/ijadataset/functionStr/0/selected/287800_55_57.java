public class Test {    public Recipient replyRecipient() {
        return new IRCChannelRecipient(generatedBy, getChannel());
    }
}