public class Test {        public void onPart(String user, String nick, String chan) {
            getChannel(chan, true).getChannelMux().onPart(user, nick, chan);
        }
}