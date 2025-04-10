package net.sf.jgcs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class defines a AbstractMultiThreadedPollingProtocol.
 * It enriches the AbstractProtocol class with a multi threaded mechanism
 * to poll I/O for several sessions.
 * 
 * @author Jose Pereira
 * @version 1.0
 */
public abstract class AbstractMultiThreadedPollingProtocol extends AbstractProtocol {

    private ExecutorService pool;

    protected void boot() {
        if (pool != null) return;
        super.boot();
        pool = Executors.newCachedThreadPool(new ThreadFactory() {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }

    protected void startReader(ProtocolReader worker) {
        pool.execute(worker);
    }

    public abstract class ProtocolReader<C> implements Runnable {

        private GroupConfiguration group;

        private C channel;

        public void run() {
            boolean toContinue = read();
            if (toContinue) pool.execute(this);
        }

        public void setChannel(C c) {
            channel = c;
        }

        public C getChannel() {
            return channel;
        }

        public GroupConfiguration getGroup() {
            return group;
        }

        public void setGroup(GroupConfiguration group) {
            this.group = group;
        }

        public void setFields(GroupConfiguration g, C c) {
            group = g;
            channel = c;
        }

        public abstract boolean read();
    }
}
