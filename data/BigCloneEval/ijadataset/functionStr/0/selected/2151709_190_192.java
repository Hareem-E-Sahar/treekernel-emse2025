public class Test {    public List getChannelsLikeTitle(String title) {
        return this.channelDAO.findChannelsLikeTitle("%" + title.toLowerCase() + "%");
    }
}