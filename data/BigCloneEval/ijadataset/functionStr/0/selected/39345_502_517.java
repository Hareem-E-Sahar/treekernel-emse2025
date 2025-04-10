public class Test {    void handleParkedCallGiveUpEvent(ParkedCallGiveUpEvent event) {
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
}