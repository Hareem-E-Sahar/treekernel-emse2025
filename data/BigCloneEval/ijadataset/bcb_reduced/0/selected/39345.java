package org.asteriskjava.live.internal;

import org.asteriskjava.live.*;
import org.asteriskjava.manager.ResponseEvents;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.*;
import org.asteriskjava.util.DateUtil;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;
import java.util.*;

/**
 * Manages channel events on behalf of an AsteriskServer.
 *
 * @author srt
 * @version $Id: ChannelManager.java 1381 2009-10-19 19:48:15Z srt $
 */
class ChannelManager {

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * How long we wait before we remove hung up channels from memory (in milliseconds).
     */
    private static final long REMOVAL_THRESHOLD = 15 * 60 * 1000L;

    private static final long SLEEP_TIME_BEFORE_GET_VAR = 50L;

    private final AsteriskServerImpl server;

    /**
     * A map of all active channel by their unique id.
     */
    private final Set<AsteriskChannelImpl> channels;

    /**
     * Creates a new instance.
     *
     * @param server the server this channel manager belongs to.
     */
    ChannelManager(AsteriskServerImpl server) {
        this.server = server;
        this.channels = new HashSet<AsteriskChannelImpl>();
    }

    void initialize() throws ManagerCommunicationException {
        initialize(null);
    }

    void initialize(List<String> variables) throws ManagerCommunicationException {
        ResponseEvents re;
        disconnected();
        StatusAction sa = new StatusAction();
        sa.setVariables(variables);
        re = server.sendEventGeneratingAction(sa);
        for (ManagerEvent event : re.getEvents()) {
            if (event instanceof StatusEvent) {
                handleStatusEvent((StatusEvent) event);
            }
        }
    }

    void disconnected() {
        synchronized (channels) {
            channels.clear();
        }
    }

    /**
     * Returns a collection of all active AsteriskChannels.
     *
     * @return a collection of all active AsteriskChannels.
     */
    Collection<AsteriskChannel> getChannels() {
        Collection<AsteriskChannel> copy;
        synchronized (channels) {
            copy = new ArrayList<AsteriskChannel>(channels.size() + 2);
            for (AsteriskChannel channel : channels) {
                if (channel.getState() != ChannelState.HUNGUP) {
                    copy.add(channel);
                }
            }
        }
        return copy;
    }

    private void addChannel(AsteriskChannelImpl channel) {
        synchronized (channels) {
            channels.add(channel);
        }
    }

    /**
     * Removes channels that have been hung more than {@link #REMOVAL_THRESHOLD} milliseconds.
     */
    private void removeOldChannels() {
        Iterator<AsteriskChannelImpl> i;
        synchronized (channels) {
            i = channels.iterator();
            while (i.hasNext()) {
                final AsteriskChannel channel = i.next();
                final Date dateOfRemoval = channel.getDateOfRemoval();
                if (channel.getState() == ChannelState.HUNGUP && dateOfRemoval != null) {
                    final long diff = DateUtil.getDate().getTime() - dateOfRemoval.getTime();
                    if (diff >= REMOVAL_THRESHOLD) {
                        i.remove();
                    }
                }
            }
        }
    }

    private AsteriskChannelImpl addNewChannel(String uniqueId, String name, Date dateOfCreation, String callerIdNumber, String callerIdName, ChannelState state, String account) {
        final AsteriskChannelImpl channel;
        final String traceId;
        channel = new AsteriskChannelImpl(server, name, uniqueId, dateOfCreation);
        channel.setCallerId(new CallerId(callerIdName, callerIdNumber));
        channel.setAccount(account);
        channel.stateChanged(dateOfCreation, state);
        logger.info("Adding channel " + channel.getName() + "(" + channel.getId() + ")");
        if (SLEEP_TIME_BEFORE_GET_VAR > 0) {
            try {
                Thread.sleep(SLEEP_TIME_BEFORE_GET_VAR);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        traceId = getTraceId(channel);
        channel.setTraceId(traceId);
        addChannel(channel);
        if (traceId != null && (!name.toLowerCase(Locale.ENGLISH).startsWith("local/") || (name.endsWith(",1") || name.endsWith(";1")))) {
            final OriginateCallbackData callbackData;
            callbackData = server.getOriginateCallbackDataByTraceId(traceId);
            if (callbackData != null && callbackData.getChannel() == null) {
                callbackData.setChannel(channel);
                try {
                    callbackData.getCallback().onDialing(channel);
                } catch (Throwable t) {
                    logger.warn("Exception dispatching originate progress.", t);
                }
            }
        }
        server.fireNewAsteriskChannel(channel);
        return channel;
    }

    void handleStatusEvent(StatusEvent event) {
        AsteriskChannelImpl channel;
        final Extension extension;
        boolean isNew = false;
        Map<String, String> variables = event.getVariables();
        channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            Date dateOfCreation;
            if (event.getSeconds() != null) {
                dateOfCreation = new Date(DateUtil.getDate().getTime() - (event.getSeconds() * 1000L));
            } else {
                dateOfCreation = DateUtil.getDate();
            }
            channel = new AsteriskChannelImpl(server, event.getChannel(), event.getUniqueId(), dateOfCreation);
            isNew = true;
            if (variables != null) {
                for (String variable : variables.keySet()) {
                    channel.updateVariable(variable, variables.get(variable));
                }
            }
        }
        if (event.getContext() == null && event.getExtension() == null && event.getPriority() == null) {
            extension = null;
        } else {
            extension = new Extension(event.getContext(), event.getExtension(), event.getPriority());
        }
        synchronized (channel) {
            channel.setCallerId(new CallerId(event.getCallerIdName(), event.getCallerIdNum()));
            channel.setAccount(event.getAccountCode());
            if (event.getChannelState() != null) {
                channel.stateChanged(event.getDateReceived(), ChannelState.valueOf(event.getChannelState()));
            }
            channel.extensionVisited(event.getDateReceived(), extension);
            if (event.getBridgedChannel() != null) {
                final AsteriskChannelImpl linkedChannel = getChannelImplByName(event.getBridgedChannel());
                if (linkedChannel != null) {
                    channel.channelLinked(event.getDateReceived(), linkedChannel);
                    synchronized (linkedChannel) {
                        linkedChannel.channelLinked(event.getDateReceived(), channel);
                    }
                }
            }
        }
        if (isNew) {
            logger.info("Adding new channel " + channel.getName());
            addChannel(channel);
            server.fireNewAsteriskChannel(channel);
        }
    }

    /**
     * Returns a channel from the ChannelManager's cache with the given name
     * If multiple channels are found, returns the most recently CREATED one.
     * If two channels with the very same date exist, avoid HUNGUP ones.
     *
     * @param name the name of the requested channel.
     * @return the (most recent) channel if found, in any state, or null if none found.
     */
    AsteriskChannelImpl getChannelImplByName(String name) {
        Date dateOfCreation = null;
        AsteriskChannelImpl channel = null;
        if (name == null) {
            return null;
        }
        synchronized (channels) {
            for (AsteriskChannelImpl tmp : channels) {
                if (tmp.getName() != null && tmp.getName().equals(name)) {
                    if (dateOfCreation == null || tmp.getDateOfCreation().after(dateOfCreation) || (tmp.getDateOfCreation().equals(dateOfCreation) && tmp.getState() != ChannelState.HUNGUP)) {
                        channel = tmp;
                        dateOfCreation = channel.getDateOfCreation();
                    }
                }
            }
        }
        return channel;
    }

    /**
     * Returns a NON-HUNGUP channel from the ChannelManager's cache with the given name.
     *
     * @param name the name of the requested channel.
     * @return the NON-HUNGUP channel if found, or null if none is found.
     */
    AsteriskChannelImpl getChannelImplByNameAndActive(String name) {
        AsteriskChannelImpl channel = null;
        if (name == null) {
            return null;
        }
        synchronized (channels) {
            for (AsteriskChannelImpl tmp : channels) {
                if (tmp.getName() != null && tmp.getName().equals(name) && tmp.getState() != ChannelState.HUNGUP) {
                    channel = tmp;
                }
            }
        }
        return channel;
    }

    AsteriskChannelImpl getChannelImplById(String id) {
        if (id == null) {
            return null;
        }
        synchronized (channels) {
            for (AsteriskChannelImpl channel : channels) {
                if (id.equals(channel.getId())) {
                    return channel;
                }
            }
        }
        return null;
    }

    /**
     * Returns the other side of a local channel.
     * <p/>
     * Local channels consist of two sides, like
     * "Local/1234@from-local-60b5,1" and "Local/1234@from-local-60b5,2" (for Asterisk 1.4) or
     * "Local/1234@from-local-60b5;1" and "Local/1234@from-local-60b5;2" (for Asterisk 1.6)
     * this method returns the other side.
     *
     * @param localChannel one side
     * @return the other side, or <code>null</code> if not available or if the given channel
     *         is not a local channel.
     */
    AsteriskChannelImpl getOtherSideOfLocalChannel(AsteriskChannel localChannel) {
        final String name;
        final char num;
        if (localChannel == null) {
            return null;
        }
        name = localChannel.getName();
        if (name == null || !name.startsWith("Local/") || (name.charAt(name.length() - 2) != ',' && name.charAt(name.length() - 2) != ';')) {
            return null;
        }
        num = name.charAt(name.length() - 1);
        if (num == '1') {
            return getChannelImplByName(name.substring(0, name.length() - 1) + "2");
        } else if (num == '2') {
            return getChannelImplByName(name.substring(0, name.length() - 1) + "1");
        } else {
            return null;
        }
    }

    void handleNewChannelEvent(NewChannelEvent event) {
        final AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            if (event.getChannel() == null) {
                logger.info("Ignored NewChannelEvent with empty channel name (uniqueId=" + event.getUniqueId() + ")");
            } else {
                addNewChannel(event.getUniqueId(), event.getChannel(), event.getDateReceived(), event.getCallerIdNum(), event.getCallerIdName(), ChannelState.valueOf(event.getChannelState()), event.getAccountCode());
            }
        } else {
            synchronized (channel) {
                channel.nameChanged(event.getDateReceived(), event.getChannel());
                channel.setCallerId(new CallerId(event.getCallerIdName(), event.getCallerIdNum()));
                channel.stateChanged(event.getDateReceived(), ChannelState.valueOf(event.getChannelState()));
            }
        }
    }

    void handleNewExtenEvent(NewExtenEvent event) {
        AsteriskChannelImpl channel;
        final Extension extension;
        channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            logger.error("Ignored NewExtenEvent for unknown channel " + event.getChannel());
            return;
        }
        extension = new Extension(event.getContext(), event.getExtension(), event.getPriority(), event.getApplication(), event.getAppData());
        synchronized (channel) {
            channel.extensionVisited(event.getDateReceived(), extension);
        }
    }

    void handleNewStateEvent(NewStateEvent event) {
        AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            channel = getChannelImplByNameAndActive(event.getChannel());
            if (channel != null) {
                logger.info("Changing unique id for '" + channel.getName() + "' from " + channel.getId() + " to " + event.getUniqueId());
                channel.idChanged(event.getDateReceived(), event.getUniqueId());
            }
            if (channel == null) {
                logger.info("Creating new channel due to NewStateEvent '" + event.getChannel() + "' unique id " + event.getUniqueId());
                channel = addNewChannel(event.getUniqueId(), event.getChannel(), event.getDateReceived(), event.getCallerIdNum(), event.getCallerIdName(), ChannelState.valueOf(event.getChannelState()), null);
            }
        }
        if (event.getCallerIdNum() != null || event.getCallerIdName() != null) {
            String cidnum = "";
            String cidname = "";
            CallerId currentCallerId = channel.getCallerId();
            if (currentCallerId != null) {
                cidnum = currentCallerId.getNumber();
                cidname = currentCallerId.getName();
            }
            if (event.getCallerIdNum() != null) {
                cidnum = event.getCallerIdNum();
            }
            if (event.getCallerIdName() != null) {
                cidname = event.getCallerIdName();
            }
            CallerId newCallerId = new CallerId(cidname, cidnum);
            logger.debug("Updating CallerId (following NewStateEvent) to: " + newCallerId.toString());
            channel.setCallerId(newCallerId);
            if (event.getChannel() != null && !event.getChannel().equals(channel.getName())) {
                logger.info("Renaming channel (following NewStateEvent) '" + channel.getName() + "' to '" + event.getChannel() + "'");
                synchronized (channel) {
                    channel.nameChanged(event.getDateReceived(), event.getChannel());
                }
            }
        }
        if (event.getChannelState() != null) {
            synchronized (channel) {
                channel.stateChanged(event.getDateReceived(), ChannelState.valueOf(event.getChannelState()));
            }
        }
    }

    void handleNewCallerIdEvent(NewCallerIdEvent event) {
        AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            channel = getChannelImplByNameAndActive(event.getChannel());
            if (channel != null) {
                logger.info("Changing unique id for '" + channel.getName() + "' from " + channel.getId() + " to " + event.getUniqueId());
                channel.idChanged(event.getDateReceived(), event.getUniqueId());
            }
            if (channel == null) {
                channel = addNewChannel(event.getUniqueId(), event.getChannel(), event.getDateReceived(), event.getCallerIdNum(), event.getCallerIdName(), ChannelState.DOWN, null);
            }
        }
        synchronized (channel) {
            channel.setCallerId(new CallerId(event.getCallerIdName(), event.getCallerIdNum()));
        }
    }

    void handleHangupEvent(HangupEvent event) {
        HangupCause cause = null;
        AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            logger.error("Ignored HangupEvent for unknown channel " + event.getChannel());
            return;
        }
        if (event.getCause() != null) {
            cause = HangupCause.getByCode(event.getCause());
        }
        synchronized (channel) {
            channel.hungup(event.getDateReceived(), cause, event.getCauseTxt());
        }
        logger.info("Removing channel " + channel.getName() + " due to hangup (" + cause + ")");
        removeOldChannels();
    }

    void handleDialEvent(DialEvent event) {
        final AsteriskChannelImpl sourceChannel = getChannelImplById(event.getUniqueId());
        final AsteriskChannelImpl destinationChannel = getChannelImplById(event.getDestUniqueId());
        if (sourceChannel == null) {
            logger.error("Ignored DialEvent for unknown source channel " + event.getChannel() + " with unique id " + event.getUniqueId());
            return;
        }
        if (destinationChannel == null) {
            logger.error("Ignored DialEvent for unknown destination channel " + event.getDestination() + " with unique id " + event.getDestUniqueId());
            return;
        }
        logger.info(sourceChannel.getName() + " dialed " + destinationChannel.getName());
        getTraceId(sourceChannel);
        getTraceId(destinationChannel);
        synchronized (sourceChannel) {
            sourceChannel.channelDialed(event.getDateReceived(), destinationChannel);
        }
        synchronized (destinationChannel) {
            destinationChannel.channelDialing(event.getDateReceived(), sourceChannel);
        }
    }

    void handleBridgeEvent(BridgeEvent event) {
        final AsteriskChannelImpl channel1 = getChannelImplById(event.getUniqueId1());
        final AsteriskChannelImpl channel2 = getChannelImplById(event.getUniqueId2());
        if (channel1 == null) {
            logger.error("Ignored BridgeEvent for unknown channel " + event.getChannel1());
            return;
        }
        if (channel2 == null) {
            logger.error("Ignored BridgeEvent for unknown channel " + event.getChannel2());
            return;
        }
        if (event.isLink()) {
            logger.info("Linking channels " + channel1.getName() + " and " + channel2.getName());
            synchronized (channel1) {
                channel1.channelLinked(event.getDateReceived(), channel2);
            }
            synchronized (channel2) {
                channel2.channelLinked(event.getDateReceived(), channel1);
            }
        }
        if (event.isUnlink()) {
            logger.info("Unlinking channels " + channel1.getName() + " and " + channel2.getName());
            synchronized (channel1) {
                channel1.channelUnlinked(event.getDateReceived());
            }
            synchronized (channel2) {
                channel2.channelUnlinked(event.getDateReceived());
            }
        }
    }

    void handleRenameEvent(RenameEvent event) {
        AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            logger.error("Ignored RenameEvent for unknown channel with uniqueId " + event.getUniqueId());
            return;
        }
        logger.info("Renaming channel '" + channel.getName() + "' to '" + event.getNewname() + "', uniqueId is " + event.getUniqueId());
        synchronized (channel) {
            channel.nameChanged(event.getDateReceived(), event.getNewname());
        }
    }

    void handleCdrEvent(CdrEvent event) {
        final AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        final AsteriskChannelImpl destinationChannel = getChannelImplByName(event.getDestinationChannel());
        final CallDetailRecordImpl cdr;
        if (channel == null) {
            logger.info("Ignored CdrEvent for unknown channel with uniqueId " + event.getUniqueId());
            return;
        }
        cdr = new CallDetailRecordImpl(channel, destinationChannel, event);
        synchronized (channel) {
            channel.callDetailRecordReceived(event.getDateReceived(), cdr);
        }
    }

    private String getTraceId(AsteriskChannel channel) {
        String traceId;
        try {
            traceId = channel.getVariable(Constants.VARIABLE_TRACE_ID);
        } catch (Exception e) {
            traceId = null;
        }
        return traceId;
    }

    void handleParkedCallEvent(ParkedCallEvent event) {
        AsteriskChannelImpl channel = getChannelImplByNameAndActive(event.getChannel());
        if (channel == null) {
            logger.info("Ignored ParkedCallEvent for unknown channel " + event.getChannel());
            return;
        }
        synchronized (channel) {
            Extension ext = new Extension(null, event.getExten(), 1);
            channel.setParkedAt(ext);
            logger.info("Channel " + channel.getName() + " is parked at " + channel.getParkedAt().getExtension());
        }
    }

    void handleParkedCallGiveUpEvent(ParkedCallGiveUpEvent event) {
        AsteriskChannelImpl channel = getChannelImplByNameAndActive(event.getChannel());
        if (channel == null) {
            logger.info("Ignored ParkedCallGiveUpEvent for unknown channel " + event.getChannel());
            return;
        }
        Extension wasParkedAt = channel.getParkedAt();
        if (wasParkedAt == null) {
            logger.info("Ignored ParkedCallGiveUpEvent as the channel was not parked");
            return;
        }
        synchronized (channel) {
            channel.setParkedAt(null);
        }
        logger.info("Channel " + channel.getName() + " is unparked (GiveUp) from " + wasParkedAt.getExtension());
    }

    void handleParkedCallTimeOutEvent(ParkedCallTimeOutEvent event) {
        final AsteriskChannelImpl channel = getChannelImplByNameAndActive(event.getChannel());
        if (channel == null) {
            logger.info("Ignored ParkedCallTimeOutEvent for unknown channel " + event.getChannel());
            return;
        }
        Extension wasParkedAt = channel.getParkedAt();
        if (wasParkedAt == null) {
            logger.info("Ignored ParkedCallTimeOutEvent as the channel was not parked");
            return;
        }
        synchronized (channel) {
            channel.setParkedAt(null);
        }
        logger.info("Channel " + channel.getName() + " is unparked (Timeout) from " + wasParkedAt.getExtension());
    }

    void handleUnparkedCallEvent(UnparkedCallEvent event) {
        final AsteriskChannelImpl channel = getChannelImplByNameAndActive(event.getChannel());
        if (channel == null) {
            logger.info("Ignored UnparkedCallEvent for unknown channel " + event.getChannel());
            return;
        }
        Extension wasParkedAt = channel.getParkedAt();
        if (wasParkedAt == null) {
            logger.info("Ignored UnparkedCallEvent as the channel was not parked");
            return;
        }
        synchronized (channel) {
            channel.setParkedAt(null);
        }
        logger.info("Channel " + channel.getName() + " is unparked (moved away) from " + wasParkedAt.getExtension());
    }

    void handleVarSetEvent(VarSetEvent event) {
        if (event.getUniqueId() == null) {
            return;
        }
        final AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            logger.info("Ignored VarSetEvent for unknown channel with uniqueId " + event.getUniqueId());
            return;
        }
        synchronized (channel) {
            channel.updateVariable(event.getVariable(), event.getValue());
        }
    }

    void handleDtmfEvent(DtmfEvent event) {
        if (event.isBegin()) {
            return;
        }
        if (event.getUniqueId() == null) {
            return;
        }
        final AsteriskChannelImpl channel = getChannelImplById(event.getUniqueId());
        if (channel == null) {
            logger.info("Ignored DtmfEvent for unknown channel with uniqueId " + event.getUniqueId());
            return;
        }
        final Character dtmfDigit;
        if (event.getDigit() == null || event.getDigit().length() < 1) {
            dtmfDigit = null;
        } else {
            dtmfDigit = event.getDigit().charAt(0);
        }
        synchronized (channel) {
            if (event.isReceived()) {
                channel.dtmfReceived(dtmfDigit);
            }
            if (event.isSent()) {
                channel.dtmfSent(dtmfDigit);
            }
        }
    }
}
