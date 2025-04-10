package org.speakmon.babble.events;

import java.util.EventObject;
import org.speakmon.babble.UserInfo;

/**
 * This event models an ACTION command to a channel.
 * @version $Id: ActionEvent.java 239 2004-07-28 05:09:17Z speakmon $
 * @author Ben Speakmon
 */
public class ActionEvent extends EventObject {

    private UserInfo userInfo;

    /**
     * Holds value of property channel.
     */
    private String channel;

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Creates a new ActionEvent.
     * @param source the <code>UserInfo</code> object for the user that sent the ACTION command
     */
    public ActionEvent(Object source) {
        super(source);
        userInfo = (UserInfo) source;
    }

    /**
     * Returns the <code>UserInfo</code> source object.
     * @return the <code>UserInfo</code> source object
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Returns the channel the command was sent to.
     * @return the command's channel
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Sets the channel that the command was sent to.
     * @param channel New value of property channel.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Gets the action text.
     * @return the action text
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the action text.
     * @param description the action text
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
