public class Test {    public void removeChannel() {
        if (getChannel() != null && !isDisposed()) {
            getHandle().removeChannel(getChannel());
        }
    }
}