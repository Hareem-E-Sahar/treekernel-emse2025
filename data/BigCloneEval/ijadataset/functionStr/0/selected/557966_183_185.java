public class Test {    public Channel[] getChannels() throws SQLException {
        return getDbCon().getListOfChannels();
    }
}