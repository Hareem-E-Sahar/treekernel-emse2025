public class Test {    @Override
    public final int getChannel() {
        state.checkReleased();
        return channel;
    }
}