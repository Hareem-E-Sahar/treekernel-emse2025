public class Test {    public AgiReply sendCommand(AgiCommand command) throws AgiException {
        return getChannel().sendCommand(command);
    }
}