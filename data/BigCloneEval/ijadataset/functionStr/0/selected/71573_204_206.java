public class Test {    public char waitForDigit(int timeout) throws AgiException {
        return getChannel().waitForDigit(timeout);
    }
}