public class Test {    public ChannelDAO getChannelDAO() {
        if (channelDAO == null) channelDAO = new ChannelDAO();
        return channelDAO;
    }
}