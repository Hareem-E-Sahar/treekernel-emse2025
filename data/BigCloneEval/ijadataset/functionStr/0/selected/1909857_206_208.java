public class Test {    public synchronized Channel getChannel() {
        return selectionKey.channel();
    }
}