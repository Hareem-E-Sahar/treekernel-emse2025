public class Test {    public Collection<Channel> getChannels() {
        return Collections.unmodifiableCollection(myChannels.values());
    }
}