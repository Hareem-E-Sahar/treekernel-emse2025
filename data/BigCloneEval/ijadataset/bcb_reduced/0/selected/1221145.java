package com.speed.irc.script;

import java.util.Random;
import com.speed.irc.event.ApiEvent;
import com.speed.irc.event.ChannelUserEvent;
import com.speed.irc.event.ChannelUserListener;
import com.speed.irc.event.PrivateMessageEvent;
import com.speed.irc.event.PrivateMessageListener;
import com.speed.irc.types.Bot;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

/**
 * Greets people as they join the channel or speak a greeting.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Shivam Mistry
 */
public class HelloBot extends Bot implements ChannelUserListener, PrivateMessageListener {

    private static final String[] HELLO_PHRASES = new String[] { "Hello", "Hi", "Hey", "Yo", "Wassup", "helo", "herro", "hiya", "hai", "heya" };

    private static final Random RANDOM_GENERATOR = new Random();

    private Channel[] channels;

    public HelloBot(final String server, final int port) {
        super(server, port);
    }

    public static void main(String[] args) {
        new HelloBot("irc.strictfp.com", 6667);
    }

    public Channel[] getChannels() {
        return channels;
    }

    public String getNick() {
        return "London";
    }

    public void onStart() {
        channels = new Channel[] { new Channel("#irc", server) };
        channels[0].setAutoRejoin(true);
        server.setAutoReconnect(true);
        server.setReadDebug(true);
    }

    @Override
    public void apiEventReceived(final ApiEvent e) {
        super.apiEventReceived(e);
        if (e.getOpcode() == ApiEvent.SERVER_QUIT) {
            logger.info("WE HAVE QUIT FROM THE SERVER.");
        }
    }

    public void messageReceived(PrivateMessageEvent e) {
        final String message = e.getMessage().getMessage();
        final String sender = e.getMessage().getSender();
        if (message.contains("!raw") && sender.equals("Speed")) {
            server.sendRaw(message.replaceFirst("!raw", "").trim());
        } else if (message.equals("!quit") && sender.equals("Speed")) {
            server.quit("bai");
        }
        if (e.getMessage().getConversable() == null || !(e.getMessage().getConversable() instanceof Channel)) {
            return;
        }
        final Channel channel = (Channel) e.getMessage().getConversable();
        final ChannelUser user = channel.getUser(sender);
        for (String s : HELLO_PHRASES) {
            if (message.toLowerCase().equals(s.toLowerCase()) || (message.contains("London") && message.toLowerCase().contains(s.toLowerCase()))) {
                channel.sendMessage(HELLO_PHRASES[RANDOM_GENERATOR.nextInt(HELLO_PHRASES.length - 1)] + " " + sender + " with rights: " + user.getRights());
            }
        }
    }

    public void channelUserJoined(ChannelUserEvent e) {
        System.out.println(e.getChannel().getName());
        e.getChannel().sendMessage(HELLO_PHRASES[RANDOM_GENERATOR.nextInt(HELLO_PHRASES.length - 1)] + " " + e.getUser().getNick());
    }

    public void channelUserParted(ChannelUserEvent e) {
    }

    public void channelUserModeChanged(ChannelUserEvent e) {
    }

    public void channelUserKicked(ChannelUserEvent e) {
    }
}
