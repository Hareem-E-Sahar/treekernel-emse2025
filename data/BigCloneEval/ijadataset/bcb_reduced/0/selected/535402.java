package com.speed.irc.types;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.speed.irc.connection.Server;
import com.speed.irc.event.ChannelUserEvent;
import com.speed.irc.event.ChannelUserListener;

/**
 * Represents a channel
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
public class Channel extends Conversable implements ChannelUserListener, Runnable {

    protected String name;

    protected Server server;

    public volatile List<ChannelUser> users = new LinkedList<ChannelUser>();

    public volatile List<ChannelUser> userBuffer = new LinkedList<ChannelUser>();

    public volatile boolean isRunning = true;

    public static final int WHO_DELAY = 90000;

    protected boolean autoRejoin;

    protected String nick;

    public Mode chanMode;

    public List<String> bans = new LinkedList<String>();

    protected String topic;

    protected ScheduledFuture<?> future;

    public Future<?> getFuture() {
        return future;
    }

    /**
	 * Constructs a channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 * @param server
	 *            the server object this channel is associated with.
	 */
    public Channel(final String name, final Server server) {
        this.name = name;
        this.server = server;
        this.nick = server.getNick();
        this.server.getEventManager().addListener(this);
        this.server.getChannels().put(name, this);
        chanMode = new Mode(server, "");
    }

    /**
	 * Gets the name of the channel.
	 * 
	 * @return the name of the channel
	 */
    public String getName() {
        return name;
    }

    /**
	 * Gets the list of users in the channel.
	 * 
	 * @return The list of users in the channel.
	 */
    public List<ChannelUser> getUsers() {
        return users;
    }

    /**
	 * Gets a user from the channel.
	 * 
	 * @param nick
	 *            The nick of the ChannelUser to get.
	 * @return The ChannelUser object associated with the nick or
	 *         <code>null</code>.
	 */
    public ChannelUser getUser(final String nick) {
        for (ChannelUser user : users) {
            if (user.getNick().equalsIgnoreCase(nick)) {
                return user;
            }
        }
        return null;
    }

    public boolean addChannelUser(final ChannelUser user) {
        return users.add(user);
    }

    public boolean removeChannelUser(final ChannelUser user) {
        return users.remove(user);
    }

    public boolean isAutoRejoinOn() {
        return autoRejoin;
    }

    /**
	 * Sets whether rejoining is enabled when kicked
	 * 
	 * @param on
	 *            turn auto-rejoin on or not
	 */
    public void setAutoRejoin(final boolean on) {
        autoRejoin = on;
    }

    /**
	 * Leaves the channel.
	 * 
	 * @param message
	 *            The part message, can be null for no message.
	 */
    public void part(final String message) {
        isRunning = false;
        if (message != null && !message.isEmpty()) server.sendRaw(String.format("PART %s :%s\n", name, message)); else server.sendRaw(String.format("PART %s\n", name));
    }

    /**
	 * Sends a message to the channel.
	 * 
	 * @param message
	 *            The message to be sent
	 */
    public void sendMessage(final String message) {
        server.sendRaw(String.format("PRIVMSG %s :%s\n", name, message));
    }

    public void sendNotice(String notice) {
        server.sendRaw(String.format("NOTICE %s :%s\n", name, notice));
    }

    public void run() {
        server.sendRaw("WHO " + name);
    }

    /**
	 * Gets the server the channel is on.
	 * 
	 * @return the server the channel is on
	 */
    public Server getServer() {
        return server;
    }

    /**
	 * Joins the channel.
	 */
    public void join() {
        server.sendRaw("JOIN :" + name);
        server.sendRaw("MODE " + name);
        isRunning = true;
        if (!server.getChannels().containsValue(this)) {
            server.getChannels().put(name, this);
        }
        future = server.getChanExec().scheduleWithFixedDelay(this, 0, 2, TimeUnit.MINUTES);
    }

    /**
	 * Joins the channel using the provided password.
	 * 
	 * @param password
	 *            the password to join the channel with
	 */
    public void join(final String password) {
        server.sendRaw("JOIN :" + name + " " + password);
        server.sendRaw("MODE " + name);
        isRunning = true;
        if (!server.getChannels().containsValue(this)) {
            server.getChannels().put(name, this);
        }
        future = getServer().getChanExec().schedule(this, 5, TimeUnit.SECONDS);
    }

    /**
	 * Bans then kicks the channel user with the reason specified.
	 * 
	 * @param user
	 *            the ChannelUser to kick.
	 * @param reason
	 *            The reason for kicking the channel user, can be
	 *            <code>null</code>.
	 */
    public void kickBan(final ChannelUser user, final String reason) {
        ban(user);
        kick(user, reason);
    }

    /**
	 * Attempts to ban the specified ChannelUser.
	 * 
	 * @param user
	 *            the user that should be banned.
	 */
    public void ban(final ChannelUser user) {
        final String banMask = new StringBuffer().append("*!*@").append(user.getHost()).toString();
        ban(banMask);
    }

    /**
	 * Attempts to ban the specified mask.
	 * 
	 * @param banMask
	 *            The ban-mask that should be banned.
	 */
    public void ban(final String banMask) {
        server.sendRaw(String.format("MODE %s +b %s\n", name, banMask));
    }

    /**
	 * Attempts to kick a channel user.
	 * 
	 * @param user
	 *            The ChannelUser that is to be kicked.
	 * @param reason
	 *            The reason for kicking the channel user, can be
	 *            <code>null</code>.
	 */
    public void kick(final ChannelUser user, String reason) {
        if (reason == null) {
            reason = user.getNick();
        }
        server.sendRaw(String.format("KICK %s %s :%s\n", name, user.getNick(), reason));
    }

    /**
	 * Attempts to kick a channel user.
	 * 
	 * @param nick
	 *            The nick of the user that is to be kicked.
	 * @param reason
	 *            The reason for kicking the channel user, can be
	 *            <code>null</code>.
	 */
    public void kick(final String nick, String reason) {
        final ChannelUser user = getUser(nick);
        if (user == null) {
            return;
        }
        if (reason == null) {
            reason = user.getNick();
        }
        server.sendRaw(String.format("KICK %s %s :%s\n", name, user.getNick(), reason));
    }

    /**
	 * Sets the channel's topic. Attempts to send any changes to the server.
	 * 
	 * @param topic
	 *            The new channel topic.
	 */
    public void sendTopic(final String topic) {
        server.sendRaw(String.format("TOPIC %s :%s\n", name, topic));
    }

    /**
	 * Sets the topic in the memory.
	 * 
	 * @param newTopic
	 *            the new channel topic
	 */
    public void setTopic(final String newTopic) {
        this.topic = newTopic;
    }

    /**
	 * Gets the topic.
	 * 
	 * @return the channel's topic
	 */
    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Channel && ((Channel) o).getName().equals(getName());
    }

    public void channelUserJoined(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            addChannelUser(e.getUser());
        }
    }

    public void channelUserParted(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            ChannelUser user = e.getUser();
            if (user != null) {
                removeChannelUser(user);
            }
        }
    }

    public void channelUserModeChanged(ChannelUserEvent e) {
    }

    public void channelUserKicked(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            ChannelUser user = e.getUser();
            removeChannelUser(user);
            if (user.getNick().equals(nick) && isAutoRejoinOn()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                isRunning = false;
                server.getChannels().remove(this);
                join();
            } else if (user.getNick().equals(nick)) {
                isRunning = false;
                server.getChannels().remove(this);
            }
        }
    }
}
