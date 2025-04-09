public class Test {    public static String getChannel(IRCMessage msg) {
        return msg.getArgs().get(msg.getArgs().size() - 3);
    }
}