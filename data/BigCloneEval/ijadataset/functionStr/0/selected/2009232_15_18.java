public class Test {    @Override
    public List<Channel> retrieve() {
        return new ArrayList<Channel>(channelService.getChannels());
    }
}