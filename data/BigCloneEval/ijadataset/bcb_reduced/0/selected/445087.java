package org.red5.server.net.rtmp.message;

public class Header implements Constants {

    private byte channelId = 0;

    private int timer = 0;

    private int size = 0;

    private byte dataType = 0;

    private int streamId = 0;

    private boolean timerRelative = true;

    public byte getChannelId() {
        return channelId;
    }

    public void setChannelId(byte channelId) {
        this.channelId = channelId;
    }

    public byte getDataType() {
        return dataType;
    }

    public void setDataType(byte dataType) {
        this.dataType = dataType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public boolean isTimerRelative() {
        return timerRelative;
    }

    public void setTimerRelative(boolean timerRelative) {
        this.timerRelative = timerRelative;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Header)) {
            return false;
        }
        final Header header = (Header) other;
        return (header.getChannelId() == channelId && header.getDataType() == dataType && header.getSize() == size && header.getTimer() == timer && header.getStreamId() == streamId);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ChannelId: ").append(channelId).append(", ");
        sb.append("Timer: ").append(timer).append(" (" + (timerRelative ? "relative" : "absolute") + ")").append(", ");
        sb.append("Size: ").append(size).append(", ");
        sb.append("DateType: ").append(dataType).append(", ");
        sb.append("StreamId: ").append(streamId);
        return sb.toString();
    }

    @Override
    public Object clone() {
        final Header header = new Header();
        header.setChannelId(channelId);
        header.setTimer(timer);
        header.setSize(size);
        header.setDataType(dataType);
        header.setStreamId(streamId);
        return header;
    }
}
