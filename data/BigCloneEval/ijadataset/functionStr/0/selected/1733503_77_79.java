public class Test {    public Set<ClientChannel> getChannelsOfType(final String channelType) {
        return this.channelsByType.get(channelType);
    }
}