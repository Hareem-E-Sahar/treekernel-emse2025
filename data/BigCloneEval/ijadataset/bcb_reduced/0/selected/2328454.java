package de.teamwork.irc.msgutils;

import java.text.MessageFormat;
import java.text.ParseException;
import de.teamwork.irc.*;

/**
 * Wrapper for easy handling of ERR_CANNOTSENDTOCHAN messages.
 * <p>
 * <b>Syntax:</b> <code>404 &lt;channel name&gt; "Cannot send to channel"</code>
 * <p>
 * Sent to a user who is either (a) not on a channel which is mode +n or (b) not
 * a chanop (or mode +v) on a channel which has mode +m set or where the user is
 * banned and is trying to send a PRIVMSG message to that channel.
 *
 * @author Christoph Daniel Schulze
 * @version $Id: CannotsendtochanError.java 3 2003-01-07 14:16:38Z captainnuss $
 */
public class CannotsendtochanError {

    /**
     * Instantiation is not allowed.
     */
    private CannotsendtochanError() {
    }

    /**
     * Creates a new ERR_CANNOTSENDTOCHAN message.
     *
     * @param msgNick     String object containing the nick of the guy this
     *                    message comes from. Should usually be "".
     * @param msgUser     String object containing the user name of the guy this
     *                    message comes from. Should usually be "".
     * @param msgHost     String object containing the host name of the guy this
     *                    message comes from. Should usually be "".
     * @param channelname String containing the channel name.
     */
    public static IRCMessage createMessage(String msgNick, String msgUser, String msgHost, String channelname) {
        String[] args = new String[] { channelname, "Cannot send to channel" };
        return new IRCMessage(msgNick, msgUser, msgHost, IRCMessageTypes.ERR_CANNOTSENDTOCHAN, args);
    }

    /**
     * Returns the channel name.
     *
     * @return String containing the channel name.
     */
    public static String getChannelname(IRCMessage msg) {
        return (String) msg.getArgs().elementAt(0);
    }
}
