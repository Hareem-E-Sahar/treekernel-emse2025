public class Test {    public List getFavourites() {
        return this.channelDAO.getChannels(IChannelDAO.FAVOURITES);
    }
}