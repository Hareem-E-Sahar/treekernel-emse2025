package biz.xsoftware.impl.nio.cm.basic;

import java.nio.channels.SelectionKey;
import java.util.logging.Logger;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;
import biz.xsoftware.api.nio.handlers.DataListener;

public class WrapperAndListener {

    private static final Logger log = Logger.getLogger(WrapperAndListener.class.getName());

    private String channelName;

    private RegisterableChannel channel;

    private DataListener dataHandler;

    private ConnectionListener connectCallback;

    private ConnectionListener acceptCallback;

    public WrapperAndListener(RegisterableChannelImpl r) {
        if (r == null) throw new IllegalArgumentException("r cannot be null, bug");
        channel = r;
        channelName = "" + channel;
    }

    public String toString() {
        return channelName;
    }

    public void addListener(Object id, Object l, int validOps) {
        switch(validOps) {
            case SelectionKey.OP_ACCEPT:
                if (acceptCallback != null) throw new RuntimeException(channel + "ConnectionListener is already set, cannot be set again");
                acceptCallback = (ConnectionListener) l;
                break;
            case SelectionKey.OP_CONNECT:
                if (connectCallback != null) throw new RuntimeException(channel + "ConnectionListener is already set, cannot be set again");
                connectCallback = (ConnectionListener) l;
                break;
            case SelectionKey.OP_READ:
                if (dataHandler != null) throw new RuntimeException(channel + "DataListener is already set, cannot be set again");
                dataHandler = (DataListener) l;
                break;
            case SelectionKey.OP_WRITE:
                break;
            default:
                throw new IllegalArgumentException("type=" + l.getClass().getName() + " is not allowed");
        }
    }

    public void removeListener(int ops) {
        if ((ops & SelectionKey.OP_READ) > 0) dataHandler = null;
    }

    public ConnectionListener getAcceptCallback() {
        return acceptCallback;
    }

    public RegisterableChannel getChannel() {
        return channel;
    }

    public ConnectionListener getConnectCallback() {
        return connectCallback;
    }

    public DataListener getDataHandler() {
        return dataHandler;
    }
}
