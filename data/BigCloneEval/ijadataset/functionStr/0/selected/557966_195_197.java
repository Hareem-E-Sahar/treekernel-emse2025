public class Test {    public Channel getChannel(int channelid) throws SQLException {
        return getDbCon().getChannel(channelid);
    }
}