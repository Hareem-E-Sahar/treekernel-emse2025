public class Test {    @Override
    public final String getName() {
        return getChannelName() != null ? getChannelName() : getShortId();
    }
}