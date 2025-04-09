public class Test {    public Channel getOutChannel() {
        if (hasOutMessage()) {
            return getInMessage().getChannel();
        }
        return getDeadLetterChannel();
    }
}