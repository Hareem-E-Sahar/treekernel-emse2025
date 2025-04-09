public class Test {    public String getData(String file, long timeout) throws AgiException {
        return getChannel().getData(file, timeout);
    }
}