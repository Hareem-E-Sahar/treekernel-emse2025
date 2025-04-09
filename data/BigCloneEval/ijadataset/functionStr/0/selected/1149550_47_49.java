public class Test {    public static String getChannelname(IRCMessage msg) {
        return (String) msg.getArgs().elementAt(0);
    }
}