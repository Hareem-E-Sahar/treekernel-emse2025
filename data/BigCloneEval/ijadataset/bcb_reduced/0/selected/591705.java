package com.aelitis.azureus.core.networkmanager.impl;

import java.io.IOException;
import java.util.*;
import org.gudy.azureus2.core3.util.AEDiagnostics;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.SystemTime;
import com.aelitis.azureus.core.networkmanager.*;

/**
 * 
 */
public class MultiPeerDownloader2 implements RateControlledEntity {

    private static final int MOVE_TO_IDLE_TIME = 500;

    private static final Object ADD_ACTION = new Object();

    private static final Object REMOVE_ACTION = new Object();

    private volatile ArrayList connections_cow = new ArrayList();

    private final AEMonitor connections_mon = new AEMonitor("MultiPeerDownloader");

    private final RateHandler main_handler;

    private List pending_actions;

    private connectionList active_connections = new connectionList();

    private connectionList idle_connections = new connectionList();

    private long last_idle_check;

    /**
	 * Create new downloader using the given "global" rate handler to limit all peers managed by this downloader.
	 * @param main_handler
	 */
    public MultiPeerDownloader2(RateHandler _main_handler) {
        main_handler = _main_handler;
    }

    public RateHandler getRateHandler() {
        return (main_handler);
    }

    /**
	 * Add the given connection to the downloader.
	 * @param connection to add
	 */
    public void addPeerConnection(NetworkConnectionBase connection) {
        try {
            connections_mon.enter();
            ArrayList conn_new = new ArrayList(connections_cow.size() + 1);
            conn_new.addAll(connections_cow);
            conn_new.add(connection);
            connections_cow = conn_new;
            if (pending_actions == null) {
                pending_actions = new ArrayList();
            }
            pending_actions.add(new Object[] { ADD_ACTION, connection });
        } finally {
            connections_mon.exit();
        }
    }

    /**
	 * Remove the given connection from the downloader.
	 * @param connection to remove
	 * @return true if the connection was found and removed, false if not removed
	 */
    public boolean removePeerConnection(NetworkConnectionBase connection) {
        try {
            connections_mon.enter();
            ArrayList conn_new = new ArrayList(connections_cow);
            boolean removed = conn_new.remove(connection);
            if (!removed) return false;
            connections_cow = conn_new;
            if (pending_actions == null) {
                pending_actions = new ArrayList();
            }
            pending_actions.add(new Object[] { REMOVE_ACTION, connection });
            return true;
        } finally {
            connections_mon.exit();
        }
    }

    public boolean canProcess(EventWaiter waiter) {
        if (main_handler.getCurrentNumBytesAllowed() < 1) return false;
        return true;
    }

    public long getBytesReadyToWrite() {
        return (0);
    }

    public int getConnectionCount() {
        return (connections_cow.size());
    }

    public int getReadyConnectionCount(EventWaiter waiter) {
        int res = 0;
        for (Iterator it = connections_cow.iterator(); it.hasNext(); ) {
            NetworkConnectionBase connection = (NetworkConnectionBase) it.next();
            if (connection.getTransportBase().isReadyForRead(waiter) == 0) {
                res++;
            }
        }
        return (res);
    }

    public int doProcessing(EventWaiter waiter, int max_bytes) {
        int num_bytes_allowed = main_handler.getCurrentNumBytesAllowed();
        if (num_bytes_allowed < 1) {
            return 0;
        }
        if (max_bytes > 0 && max_bytes < num_bytes_allowed) {
            num_bytes_allowed = max_bytes;
        }
        if (pending_actions != null) {
            try {
                connections_mon.enter();
                for (int i = 0; i < pending_actions.size(); i++) {
                    Object[] entry = (Object[]) pending_actions.get(i);
                    NetworkConnectionBase connection = (NetworkConnectionBase) entry[1];
                    if (entry[0] == ADD_ACTION) {
                        active_connections.add(connection);
                    } else {
                        active_connections.remove(connection);
                        idle_connections.remove(connection);
                    }
                }
                pending_actions = null;
            } finally {
                connections_mon.exit();
            }
        }
        long now = SystemTime.getSteppedMonotonousTime();
        if (now - last_idle_check > MOVE_TO_IDLE_TIME) {
            last_idle_check = now;
            connectionEntry entry = idle_connections.head();
            while (entry != null) {
                NetworkConnectionBase connection = entry.connection;
                connectionEntry next = entry.next;
                if (connection.getTransportBase().isReadyForRead(waiter) == 0) {
                    idle_connections.remove(entry);
                    active_connections.addToStart(entry);
                }
                entry = next;
            }
        }
        int num_bytes_remaining = num_bytes_allowed;
        connectionEntry entry = active_connections.head();
        int num_entries = active_connections.size();
        for (int i = 0; i < num_entries && entry != null && num_bytes_remaining > 0; i++) {
            NetworkConnectionBase connection = entry.connection;
            connectionEntry next = entry.next;
            long ready = connection.getTransportBase().isReadyForRead(waiter);
            if (ready == 0) {
                int mss = connection.getMssSize();
                int allowed = num_bytes_remaining > mss ? mss : num_bytes_remaining;
                int bytes_read = 0;
                try {
                    bytes_read = connection.getIncomingMessageQueue().receiveFromTransport(allowed);
                } catch (Throwable e) {
                    if (AEDiagnostics.TRACE_CONNECTION_DROPS) {
                        if (e.getMessage() == null) {
                            Debug.out("null read exception message: ", e);
                        } else {
                            if (e.getMessage().indexOf("end of stream on socket read") == -1 && e.getMessage().indexOf("An existing connection was forcibly closed by the remote host") == -1 && e.getMessage().indexOf("Connection reset by peer") == -1 && e.getMessage().indexOf("An established connection was aborted by the software in your host machine") == -1) {
                                System.out.println("MP: read exception [" + connection.getTransportBase().getDescription() + "]: " + e.getMessage());
                            }
                        }
                    }
                    if (!(e instanceof IOException)) {
                        Debug.printStackTrace(e);
                    }
                    connection.notifyOfException(e);
                }
                num_bytes_remaining -= bytes_read;
                active_connections.moveToEnd(entry);
            } else if (ready > MOVE_TO_IDLE_TIME) {
                active_connections.remove(entry);
                idle_connections.addToEnd(entry);
            }
            entry = next;
        }
        int total_bytes_read = num_bytes_allowed - num_bytes_remaining;
        if (total_bytes_read > 0) {
            main_handler.bytesProcessed(total_bytes_read);
            return total_bytes_read;
        }
        return 0;
    }

    public int getPriority() {
        return RateControlledEntity.PRIORITY_HIGH;
    }

    public boolean getPriorityBoost() {
        return false;
    }

    public String getString() {
        StringBuffer str = new StringBuffer();
        str.append("MPD (" + connections_cow.size() + "/" + active_connections.size() + "/" + idle_connections.size() + ": ");
        int num = 0;
        for (Iterator it = connections_cow.iterator(); it.hasNext(); ) {
            NetworkConnectionBase connection = (NetworkConnectionBase) it.next();
            if (num++ > 0) {
                str.append(",");
            }
            str.append(connection.getString());
        }
        return (str.toString());
    }

    protected static class connectionList {

        private int size;

        private connectionEntry head;

        private connectionEntry tail;

        protected connectionEntry add(NetworkConnectionBase connection) {
            connectionEntry entry = new connectionEntry(connection);
            if (head == null) {
                head = tail = entry;
            } else {
                tail.next = entry;
                entry.prev = tail;
                tail = entry;
            }
            size++;
            return (entry);
        }

        protected void addToEnd(connectionEntry entry) {
            entry.next = null;
            entry.prev = tail;
            if (tail == null) {
                head = tail = entry;
            } else {
                tail.next = entry;
                tail = entry;
            }
            size++;
        }

        protected void addToStart(connectionEntry entry) {
            entry.next = head;
            entry.prev = null;
            if (head == null) {
                head = tail = entry;
            } else {
                head.prev = entry;
                head = entry;
            }
            size++;
        }

        protected void moveToEnd(connectionEntry entry) {
            if (entry != tail) {
                connectionEntry prev = entry.prev;
                connectionEntry next = entry.next;
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                next.prev = prev;
                entry.prev = tail;
                entry.next = null;
                tail.next = entry;
                tail = entry;
            }
        }

        protected connectionEntry remove(NetworkConnectionBase connection) {
            connectionEntry entry = head;
            while (entry != null) {
                if (entry.connection == connection) {
                    remove(entry);
                    return (entry);
                } else {
                    entry = entry.next;
                }
            }
            return (null);
        }

        protected void remove(connectionEntry entry) {
            connectionEntry prev = entry.prev;
            connectionEntry next = entry.next;
            if (prev == null) {
                head = next;
            } else {
                prev.next = next;
            }
            if (next == null) {
                tail = prev;
            } else {
                next.prev = prev;
            }
            size--;
        }

        protected int size() {
            return (size);
        }

        protected connectionEntry head() {
            return (head);
        }
    }

    protected static class connectionEntry {

        private connectionEntry next;

        private connectionEntry prev;

        private NetworkConnectionBase connection;

        protected connectionEntry(NetworkConnectionBase _connection) {
            connection = _connection;
        }
    }
}
