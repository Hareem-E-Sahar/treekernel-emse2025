public class Test {    public final CharSequence getUnsuscribeScript() {
        return "dojox.cometd.unsubscribe('/" + getChannelId() + "');\n";
    }
}