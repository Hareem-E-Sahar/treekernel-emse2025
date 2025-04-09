public class Test {    @Transactional(readOnly = true)
    public Channel getChannel(Long id) {
        return ChannelDao.get(id);
    }
}