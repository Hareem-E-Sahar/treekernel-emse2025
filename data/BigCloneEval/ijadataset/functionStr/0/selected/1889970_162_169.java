public class Test {    private void appendSingleLine(final StringBuilder b) {
        for (ChannelChange change : this) {
            b.append(change.getChannelId());
            b.append("[");
            b.append(change.getDmxValue());
            b.append("] ");
        }
    }
}