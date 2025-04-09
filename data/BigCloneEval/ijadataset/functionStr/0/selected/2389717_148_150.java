public class Test {    boolean isMetaMessage() {
        return getChannel().startsWith(Bayeux.META);
    }
}