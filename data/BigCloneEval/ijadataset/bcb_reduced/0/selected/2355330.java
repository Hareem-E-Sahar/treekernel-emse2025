package org.asteriskjava.manager.event;

/**
 * A CoreShowChannelEvent is triggered for each active channel in response to a CoreShowChannelsAction.
 *
 * @author sebastian gutierrez
 * @version $Id: CoreShowChannelEvent.java 1348 2009-07-10 13:48:31Z srt $
 * @see org.asteriskjava.manager.action.CoreShowChannelsAction
 * @since 1.0.0
 */
public class CoreShowChannelEvent extends ResponseEvent {

    /**
     * Serializable version identifier.
     */
    static final long serialVersionUID = 0L;

    private String uniqueid;

    private String channel;

    private String context;

    private String extension;

    private String priority;

    private String ChannelState;

    private String channelstatedesc;

    private String application;

    private String applicationdata;

    private String calleridnum;

    private String duration;

    private String accountcode;

    private String bridgedChannel;

    private String bridgeduniqueid;

    public CoreShowChannelEvent(Object source) {
        super(source);
    }

    /**
     * Returns the channel state
     *
     * @return channel state
     */
    public String getChannelState() {
        return ChannelState;
    }

    public void setChannelState(String ChannelState) {
        this.ChannelState = ChannelState;
    }

    /**
     * Returns the Account code
     *
     * @return accountcode
     */
    public String getAccountcode() {
        return accountcode;
    }

    public void setAccountcode(String accountcode) {
        this.accountcode = accountcode;
    }

    /**
     * Returns the Aplication is runnning that channel at that time
     *
     * @return aplication name
     */
    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * Returns the Aplication Data is runnning that channel at that time
     * this is the parameters passed to that dialplan application
     *
     * @return aplication data
     */
    public String getApplicationdata() {
        return applicationdata;
    }

    public void setApplicationdata(String applicationdata) {
        this.applicationdata = applicationdata;
    }

    /**
     * Returns the Bridged Channel if is bridged to one
     *
     * @return Channel name
     */
    public String getBridgedChannel() {
        return bridgedChannel;
    }

    public void setBridgedChannel(String bridgedChannel) {
        this.bridgedChannel = bridgedChannel;
    }

    /**
     * Returns the Bridged UniqueID
     *
     * @return uniqueid
     */
    public String getBridgeduniqueid() {
        return bridgeduniqueid;
    }

    public void setBridgeduniqueid(String bridgeduniqueid) {
        this.bridgeduniqueid = bridgeduniqueid;
    }

    /**
     * Returns the CallerID
     *
     * @return callerid
     */
    public String getCalleridnum() {
        return calleridnum;
    }

    public void setCalleridnum(String calleridnum) {
        this.calleridnum = calleridnum;
    }

    /**
     * Returns the Originate Channel name
     *
     * @return Channel name
     */
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns the Channel state description (RING,...)
     *
     * @return description
     */
    public String getChannelstatedesc() {
        return channelstatedesc;
    }

    public void setChannelstatedesc(String channelstatedesc) {
        this.channelstatedesc = channelstatedesc;
    }

    /**
     * Returns the Context the channel is
     *
     * @return context
     */
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Returns the duration of the call
     *
     * @return duration
     */
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Returns the Extension dialed
     *
     * @return extension
     */
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the Priority the channel actualy is
     *
     * @return priority
     */
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Returns the Uniqueid
     *
     * @return uniqueid
     */
    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }
}
