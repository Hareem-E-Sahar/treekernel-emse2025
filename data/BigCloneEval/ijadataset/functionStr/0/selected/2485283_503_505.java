public class Test {    public Collection getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }
}