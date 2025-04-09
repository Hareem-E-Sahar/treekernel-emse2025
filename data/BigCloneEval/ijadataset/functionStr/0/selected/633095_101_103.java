public class Test {    private ClientChannel getChannel(int player, String channel) {
        return getSession(player).getChannel(channel);
    }
}