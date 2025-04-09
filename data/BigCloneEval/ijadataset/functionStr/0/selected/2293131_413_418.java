public class Test {    public BDFChannel getChannel(int index) {
        if (index < 0 || index >= channels.size()) {
            return null;
        }
        return channels.get(index);
    }
}