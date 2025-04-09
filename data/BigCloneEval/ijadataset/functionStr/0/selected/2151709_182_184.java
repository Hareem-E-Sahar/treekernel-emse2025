public class Test {    public List getChannelsByUrl(String url) {
        return this.channelDAO.findChannelsByUrl(url);
    }
}