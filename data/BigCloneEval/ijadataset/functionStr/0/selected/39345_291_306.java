public class Test {    void handleNewChannelEvent(NewChannelEvent event) {
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
}