package com.flazr;

import static com.flazr.Header.Type.LARGE;
import static com.flazr.Header.Type.MEDIUM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Invoke {

    private static final Logger logger = LoggerFactory.getLogger(Invoke.class);

    private String methodName;

    private int sequenceId;

    private int channelId;

    private int time;

    private int streamId = -1;

    private Object[] args;

    public Invoke() {
    }

    public Invoke(String methodName, int channelId, Object... args) {
        this.methodName = methodName;
        this.channelId = channelId;
        this.args = args;
    }

    public Invoke(int streamId, String methodName, int channelId, Object... args) {
        this(methodName, channelId, args);
        this.streamId = streamId;
    }

    public int getLastArgAsInt() {
        return new Double(args[args.length - 1].toString()).intValue();
    }

    public AmfObject getSecondArgAsAmfObject() {
        return (AmfObject) args[1];
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public String getMethodName() {
        return methodName;
    }

    @SuppressWarnings("unchecked")
    public Packet encode(RtmpSession session) {
        sequenceId = session.getNextInvokeId();
        session.getInvokedMethods().put(sequenceId, methodName);
        Header prevHeader = session.getPrevHeadersOut().get(channelId);
        Header.Type headerType = prevHeader == null ? LARGE : MEDIUM;
        Header header = new Header(headerType, channelId, Packet.Type.INVOKE);
        if (streamId != -1) {
            header.setStreamId(streamId);
        }
        List<Object> list = new ArrayList<Object>();
        list.add(methodName);
        list.add(sequenceId);
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof Map) {
                    list.add(new AmfObject((Map) arg));
                } else {
                    list.add(arg);
                }
            }
        } else {
            list.add(null);
        }
        header.setTime(time);
        ByteBuffer body = AmfProperty.encode(list.toArray());
        Packet packet = new Packet(header, body);
        session.getPrevHeadersOut().put(channelId, header);
        logger.info("encoded invoke: " + toString());
        return packet;
    }

    public void decode(Packet packet) {
        channelId = packet.getHeader().getChannelId();
        streamId = packet.getHeader().getStreamId();
        AmfObject object = new AmfObject();
        object.decode(packet.getData(), false);
        List<AmfProperty> properties = object.getProperties();
        methodName = (String) properties.get(0).getValue();
        double temp = (Double) properties.get(1).getValue();
        sequenceId = (int) temp;
        if (properties.size() > 2) {
            int argsLength = properties.size() - 2;
            args = new Object[argsLength];
            for (int i = 0; i < argsLength; i++) {
                args[i] = properties.get(i + 2).getValue();
            }
        }
        logger.info("decoded invoke: " + toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[method: ").append(methodName);
        sb.append(", sequenceId: ").append(sequenceId);
        if (streamId != -1) {
            sb.append(", streamId: ").append(streamId);
        }
        sb.append(", args: ").append(Arrays.toString(args)).append(']');
        return sb.toString();
    }
}
