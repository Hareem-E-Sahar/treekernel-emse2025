package de.teamwork.irc.msgutils;

import java.util.StringTokenizer;
import java.util.ArrayList;
import de.teamwork.irc.*;

/**
 * Wrapper for easy handling of RPL_NAMREPLY messages.
 * <p>
 * <b>Syntax:</b> <code>353 &lt;channel&gt; :&lt;nick&gt; *(" " &lt;nick&gt;)</code>
 * <p>
 * No description available.
 *
 * @author Christoph Daniel Schulze
 * @version $Id: NamesReply.java 3 2003-01-07 14:16:38Z captainnuss $
 */
public class NamesReply {

    /**
     * Instantiation is not allowed.
     */
    private NamesReply() {
    }

    /**
     * Creates a new RPL_NAMREPLY message.
     *
     * @param msgNick String object containing the nick of the guy this
     *                message comes from. Should usually be "".
     * @param msgUser String object containing the user name of the guy this
     *                message comes from. Should usually be "".
     * @param msgHost String object containing the host name of the guy this
     *                message comes from. Should usually be "".
     * @param channel String object containing the channel name.
     * @param nicks   ArrayList containing the nick names.
     */
    public static IRCMessage createMessage(String msgNick, String msgUser, String msgHost, String channel, ArrayList nicks) {
        StringBuffer niks = new StringBuffer((String) nicks.get(0));
        for (int i = 1; i < nicks.size(); i++) niks.append(" " + (String) nicks.get(i));
        String[] args = new String[] { channel, niks.toString() };
        return new IRCMessage(msgNick, msgUser, msgHost, IRCMessageTypes.RPL_NAMREPLY, args);
    }

    /**
     * Returns the channel name.
     *
     * @return String containing the channel name.
     */
    public static String getChannel(IRCMessage msg) {
        return (String) msg.getArgs().elementAt(msg.getArgs().size() - 2);
    }

    /**
     * Returns the nick names.
     *
     * @return ArrayList containing the nick names.
     */
    public static ArrayList getNicks(IRCMessage msg) {
        ArrayList list = new ArrayList(1);
        if (msg.getArgs().size() == 0) return list;
        String reply = (String) msg.getArgs().elementAt(msg.getArgs().size() - 1);
        if (reply.equals("")) return list;
        StringTokenizer t = new StringTokenizer(reply, " ", false);
        while (t.hasMoreTokens()) list.add(t.nextToken());
        return list;
    }
}
