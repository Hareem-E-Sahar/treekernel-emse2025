package ti.sutc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import ti.mcore.Environment;
import ti.mcore.EventListeners;
import ti.mcore.u.log.PlatoLogger;

/**
 * The manager of all possible connection types.
 */
public class SUTConnectionManager {

    private static SUTConnectionManager singleton = new SUTConnectionManager();

    private static PlatoLogger LOGGER = PlatoLogger.getLogger(SUTConnectionManager.class);

    /**
   * The list of {@link ConnectionListener}s
   */
    private EventListeners<ConnectionListener> connectionListenerList = new EventListeners<ConnectionListener>();

    /**
   * private to enforce singleton pattern.
   */
    private SUTConnectionManager() {
        Environment.getEnvironment().addShutdownHook(new Runnable() {

            public void run() {
                shutdown();
            }
        });
    }

    /**
   * Access the connection manager.
   */
    public static SUTConnectionManager getSUTConnectionManager() {
        return singleton;
    }

    /**
   * table mapping SUTC name to instance of SUTConnection (if it is
   * already constructed) or a Class<SUTConnection (if it is not already
   * constructed)
   */
    private Hashtable<String, Object> sutcTable = new Hashtable<String, Object>();

    /**
   * list of active connections
   */
    private LinkedList<SUTConnection> activeConnections = new LinkedList<SUTConnection>();

    /**
   * Add the listener to listen for connection state changes with default
   * priority.
   * 
   * @param l   the listener
   */
    public void addConnectionListener(ConnectionListener l) {
        connectionListenerList.add(l);
    }

    /**
   * Add the listener to listen for connection state changes.  A listener
   * with a higher numerical priority will be called before a listener
   * with a lower priority.
   * 
   * @param l         the listener
   * @param priority  a higher number numerically is higher priority
   */
    public void addConnectionListener(ConnectionListener l, int priority) {
        connectionListenerList.add(l, priority);
    }

    /**
   * Remove the listener from listening for connection state changes.
   * 
   * @param l   the listener
   */
    public void removeConnectionListener(ConnectionListener l) {
        connectionListenerList.remove(l);
    }

    void fireConnectionMade(final ConnectionEvent evt) {
        synchronized (activeConnections) {
            activeConnections.add(evt.getSUTConnection());
        }
        connectionListenerList.fire(new EventListeners.EventDispatcher<ConnectionListener>() {

            public void dispatch(ConnectionListener listener) {
                listener.connectionMade(evt);
            }
        });
    }

    void fireFastConnectionMade(final ConnectionEvent evt) {
        connectionListenerList.fire(new EventListeners.EventDispatcher<ConnectionListener>() {

            public void dispatch(ConnectionListener listener) {
                listener.fastConnectionMade(evt);
            }
        });
    }

    void fireConnectionLost(final ConnectionEvent evt) {
        synchronized (activeConnections) {
            activeConnections.remove(evt.getSUTConnection());
        }
        connectionListenerList.fire(new EventListeners.EventDispatcher<ConnectionListener>() {

            public void dispatch(ConnectionListener listener) {
                listener.connectionLost(evt);
            }
        });
    }

    public synchronized void registerConnection(String name, Class<? extends SUTConnection> sutcClass) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (sutcTable == null) sutcTable = new Hashtable<String, Object>();
        sutcTable.put(name, sutcClass);
    }

    public synchronized void degisterConnection(String name) {
        SUTConnection sutc = getConnection(name);
        if (sutc != null) {
            sutcTable.remove(name);
        }
    }

    public void connect(String name) throws Throwable {
        LOGGER.dbg("connect: " + name);
        SUTConnection sutc = getConnection(name);
        if (sutc != null) sutc.connect();
    }

    public void disconnect(String name) throws Throwable {
        LOGGER.dbg("disconnect: " + name);
        SUTConnection sutc = getConnection(name);
        if (sutc != null) sutc.disconnect();
    }

    public boolean isConnected(String name) {
        SUTConnection sutc = getConnection(name);
        if (sutc == null) return false;
        return activeConnections.contains(sutc);
    }

    public synchronized SUTConnection getConnection(String name) {
        Object val = sutcTable.get(name);
        if (val instanceof Class<?>) {
            try {
                Constructor<?> c = ((Class<?>) val).getConstructor(new Class[] { String.class });
                val = c.newInstance(new Object[] { name });
                sutcTable.put(name, val);
            } catch (Throwable t) {
                sutcTable.remove(name);
                LOGGER.logError(t);
            }
        }
        return (SUTConnection) val;
    }

    public Collection<SUTConnection> getActiveConnections() {
        synchronized (activeConnections) {
            HashSet<SUTConnection> uniqueConnections = new HashSet<SUTConnection>();
            uniqueConnections.addAll(activeConnections);
            return uniqueConnections;
        }
    }

    private void shutdown() {
        for (SUTConnection sutc : getActiveConnections()) {
            try {
                sutc.disconnect();
            } catch (Throwable e) {
                LOGGER.logError(e);
            }
        }
        for (int i = 0; (i < 50) && (activeConnections.size() > 0); i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.logError(e);
            }
        }
    }
}
