public class Test {    public Channel getInChannel() {
        if (hasInMessage()) {
            return getInMessage().getChannel();
        }
        return getDeadLetterChannel();
    }
}