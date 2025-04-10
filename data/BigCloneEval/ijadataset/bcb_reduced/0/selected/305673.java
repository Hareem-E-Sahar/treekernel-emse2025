package biz.xsoftware.impl.nio.cm.threaded;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.handlers.WriteCloseCallback;
import biz.xsoftware.api.nio.libs.ChannelsRunnable;

public class ThdProxyWriteHandler implements WriteCloseCallback {

    private static final Logger log = Logger.getLogger(ThdProxyWriteHandler.class.getName());

    private Channel channel;

    private WriteCloseCallback handler;

    private Executor svc;

    public ThdProxyWriteHandler(Channel c, WriteCloseCallback h, Executor s) {
        channel = c;
        handler = h;
        svc = s;
    }

    public void finished(Channel realChannel, final int id) {
        ChannelsRunnable r = new ChannelsRunnable() {

            public void run() {
                try {
                    handler.finished(channel, id);
                } catch (Exception e) {
                    log.log(Level.WARNING, channel + "Exception", e);
                }
            }

            public RegisterableChannel getChannel() {
                return channel;
            }
        };
        svc.execute(r);
    }

    public void failed(Channel c, final int id, final Throwable e) {
        ChannelsRunnable r = new ChannelsRunnable() {

            public void run() {
                try {
                    handler.failed(channel, id, e);
                } catch (Exception e) {
                    log.log(Level.WARNING, channel + "Exception", e);
                }
            }

            public RegisterableChannel getChannel() {
                return channel;
            }
        };
        svc.execute(r);
    }
}
