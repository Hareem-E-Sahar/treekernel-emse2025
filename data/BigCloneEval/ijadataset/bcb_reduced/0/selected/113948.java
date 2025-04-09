package padrmi;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import padrmi.action.AddressAction;
import padrmi.action.DirectoryExistsAction;
import padrmi.action.FileExistsAction;
import padrmi.action.FileInputStreamAction;
import padrmi.action.HostNameAction;
import padrmi.action.InvocationAction;
import padrmi.action.JarFileAction;
import padrmi.action.PropertyReadAction;
import padrmi.action.ProtectionDomainAction;
import padrmi.action.ServerSocketBindAction;
import padrmi.action.ShutdownHookAction;
import padrmi.action.SocketAcceptAction;
import padrmi.action.SocketAddressAction;
import padrmi.action.SocketConnectAction;
import padrmi.exception.PpAuthorizationException;
import padrmi.exception.PpException;
import padrmi.exception.PpGuidException;
import padrmi.exception.PpLocalClassException;
import padrmi.exception.PpLocalIOException;
import padrmi.exception.PpMethodException;
import padrmi.exception.PpObjectException;
import padrmi.exception.PpRemoteClassException;
import padrmi.exception.PpRemoteException;
import padrmi.exception.PpRemoteIOException;

/**
 * PP (PadRMI Protocol) Server, a backend to PP connection and stub factory. The
 * communication occuring between servers is fully asynchronous, hence there are
 * few types of methods on the server:
 * 
 * <ul>
 * <li>request methods (user interface to client role of remote invocation)</li>
 * <li>add/remove methods (user interface to server role of remote invocation)</li>
 * <li>process methods, which take care of processing incoming requests</li>
 * <li>handler methods, which relay received answers to added listeners</li>
 * </ul>
 * 
 * Server can handle NAT by using another host name and port that it is bind to.
 * It also allows adding routes to other guids.
 * 
 * Each server add its own shutdown hook, so it quits cleanly whenever virtual
 * machine terminates.
 * 
 * @see Server#process(Message)
 */
public class Server implements Runnable, Closeable {

    /**
	 * Size of buffer used to copy content of resources.
	 */
    private static final int SIZE_BUFFER = 4096;

    /**
	 * Timeout after which an unused TCP connection is closed.
	 */
    private static final long CONNECTION_POOL_TIMEOUT = 300000L;

    /**
	 * Sleep interval length of connection pool cleaning thread.
	 */
    private static final long CONNECTION_POOL_THREAD_SLEEP = 30000L;

    /**
	 * Protocol name used by URLs.
	 */
    public static final String PROTOCOL = "pp";

    /**
	 * Property name that holds a port number that is used to listen for
	 * incoming connections.
	 */
    public static final String PROPERTY_BIND_ADDRESS = "padrmi.bind.address";

    /**
	 * Property name that holds an address of interface that is used to listen
	 * for incoming connections.
	 */
    public static final String PROPERTY_BIND_PORT = "padrmi.bind.port";

    /**
	 * Property name that holds the maximum length of the queue of unprocessed
	 * incoming connections.
	 */
    public static final String PROPERTY_BIND_BACKLOG = "padrmi.bind.backlog";

    /**
	 * Property name that holds an address that is visible in Internet. It is
	 * optional and used to traverse NAT.
	 */
    public static final String PROPERTY_ADDRESS = "padrmi.address";

    /**
	 * Property name that holds a port number that is visible in Internet. It is
	 * optional and used to traverse NAT.
	 */
    public static final String PROPERTY_PORT = "padrmi.port";

    /**
	 * Property name that holds list of paths with resources that will be
	 * available throug instance of server. Paths are separated by OS specific
	 * path seperator character.
	 * 
	 * @see java.io.File#pathSeparator
	 */
    public static final String PROPERTY_PATH = "padrmi.path";

    /**
	 * Property name that holds a codebase URL for loading unknown classes.
	 */
    public static final String PROPERTY_CODEBASE = "padrmi.codebase";

    /**
	 * Property name that holds a GUID of this server. It is optional and
	 * randomly generated if not defined..
	 */
    public static final String PROPERTY_GUID = "padrmi.guid";

    /**
	 * Property name that denotes whether to pring debug informatio or not. It
	 * is optional and defaults to <tt>false</tt>. Only value <tt>true</tt>
	 * sets this property to true, everything else means <tt>false</tt>.
	 */
    public static final String PROPERTY_DEBUG = "padrmi.debug";

    /**
	 * Number of elements in user info part of URL.
	 * 
	 * @see URL#getUserInfo()
	 */
    public static final int POSITIONS_USERINFO = 2;

    /**
	 * Number of elements in path part of URL that specifies remote method.
	 */
    public static final int POSITIONS_INVOCATION = 4;

    /**
	 * Number of elements in path part of URL that specifies resource.
	 */
    public static final int POSITIONS_RESOURCE = 3;

    /**
	 * Position of user name in user info part of URL.
	 * 
	 * @see URL#getUserInfo()
	 */
    public static final int POSITION_USERNAME = 0;

    /**
	 * Postion of password in user info part of URL.
	 */
    public static final int POSITION_PASSWORD = 1;

    /**
	 * Positon of GUID in path part of URL.
	 */
    public static final int POSITION_GUID = 1;

    /**
	 * Positon of object name in path part of URL.
	 */
    public static final int POSITION_OBJECT = 2;

    /**
	 * Position of method name in path part of URL.
	 */
    public static final int POSITION_METHOD = 3;

    /**
	 * Positon of resource path in path part of URL.
	 */
    public static final int POSITION_RESOURCE = 2;

    /**
	 * Acknowledgement: transmission ok
	 */
    public static final byte ACK_OK = 0;

    /**
	 * Acknowledgement: transmission failed
	 */
    public static final byte ACK_ERROR = 1;

    /**
	 * Denotes whether to pring debug information or not. Debug information
	 * means printing stack traces of all exceptions and all accesed resource
	 * and invocation URLs.
	 */
    public static boolean debug = false;

    /**
	 * Default server which is extensively used by URL connections. It is
	 * discourged to use more than one default instance of server per Java VM.
	 * 
	 * @see PpURLConnection
	 */
    private static Server defaultServer;

    /**
	 * Id of server.
	 */
    private String guid;

    /**
	 * Host name of interface that is used to listen for incoming connections.
	 */
    private String bindHost;

    /**
	 * Port number that is used to listen for incoming connections.
	 */
    private int bindPort;

    /**
	 * Host name that is send as server address to another server. It may be
	 * different from <code>bindHost</code> in order to traverse NAT.
	 */
    private String host;

    /**
	 * Port number that is send as server port to another server. It may be
	 * different from <code>bindPort</code> in order to traverse NAT.
	 */
    private int port;

    /**
	 * Array of directories that is searched through to find requested resource.
	 */
    private File[] path;

    /**
	 * URL of this server. It is calculated at object creation using
	 * <code>host</code> and <code>port</code>.
	 */
    private URL url;

    /**
	 * Id of the next listener, incremented for every listener.
	 */
    private long listenerId = 0L;

    /**
	 * Server socket that listens to connections.
	 */
    private ServerSocket serverSocket;

    /**
	 * Proxy factory associated with server.
	 */
    private ProxyFactory proxyFactory = new ProxyFactory(this);

    /**
	 * Timeout invocator associated with server.
	 */
    private TimeoutInvocator timeoutInvocator = new TimeoutInvocator(this);

    /**
	 * Objects that are available on server, organized by names.
	 */
    private Map<String, ObjectEntry> objects = new ConcurrentHashMap<String, ObjectEntry>();

    /**
	 * Routes to other servers, orginized by GUIDs. Used to traverse NAT.
	 */
    private Map<String, RouteEntry> routes = new ConcurrentHashMap<String, RouteEntry>();

    /**
	 * Listeners waiting for resource.
	 */
    private Map<Long, ResourceListener> resourceListeners = new ConcurrentHashMap<Long, ResourceListener>();

    /**
	 * Listeners waiting for invocation result.
	 */
    private Map<Long, InvocationListener> invocationListeners = new ConcurrentHashMap<Long, InvocationListener>();

    /**
	 * Pool of TCP connections, indexed by "host:port"
	 */
    private Map<String, ConnectionPoolEntry> connectionPool = new HashMap<String, ConnectionPoolEntry>();

    /**
	 * Reference to connection pool thread.
	 */
    private ConnectionPoolKeeper connPoolKeeper;

    /**
	 * Data structure to hold socket and timestamp
	 */
    private class ConnectionPoolEntry {

        protected Socket socket;

        protected InputStream in;

        protected OutputStream out;

        protected long timestamp;

        protected ConnectionPoolEntry(Socket socket) throws IOException {
            this.socket = socket;
            out = socket.getOutputStream();
            in = socket.getInputStream();
            timestamp = System.currentTimeMillis();
        }

        protected void close() {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
	 * Thread that cleans up connection pool regularly
	 */
    private class ConnectionPoolKeeper extends Thread {

        private volatile boolean stopping = false, stopped = false;

        protected volatile boolean active = true;

        public void run() {
            List<String> removeList = new ArrayList<String>();
            List<ConnectionPoolEntry> closeList = new ArrayList<ConnectionPoolEntry>();
            while (!stopping) {
                synchronized (connectionPool) {
                    long threshold = System.currentTimeMillis() - CONNECTION_POOL_TIMEOUT;
                    for (String key : connectionPool.keySet()) {
                        if (connectionPool.get(key).timestamp < threshold) {
                            removeList.add(key);
                        }
                    }
                    for (String key : removeList) {
                        closeList.add(connectionPool.remove(key));
                    }
                    removeList.clear();
                }
                for (ConnectionPoolEntry conn : closeList) {
                    synchronized (conn) {
                        conn.close();
                    }
                }
                closeList.clear();
                try {
                    sleep(CONNECTION_POOL_THREAD_SLEEP);
                } catch (InterruptedException ie) {
                }
            }
            stopped = true;
        }

        public void stopIt() {
            stopping = true;
            while (!stopped) {
                interrupt();
                try {
                    sleep(1000L);
                } catch (InterruptedException ie) {
                }
            }
        }

        public void shutdown() {
            Collection<ConnectionPoolEntry> conns;
            synchronized (connectionPool) {
                conns = connectionPool.values();
                connectionPool.clear();
                active = false;
            }
            for (ConnectionPoolEntry conn : conns) {
                synchronized (conn) {
                    conn.close();
                }
            }
        }
    }

    /**
	 * Class loader which is used to deserialize arguments and results. It is
	 * needed only in case when classes passed as arguments or results are not
	 * accesible throug current classloader. Used by JavaGo because concrete
	 * type of JavaGo programs is defined somewhere else, not even at method
	 * caller side.
	 */
    private ClassLoader classLoader = null;

    /**
	 * Sets debug attribute.
	 */
    static {
        Server.debug = Boolean.parseBoolean(AccessController.doPrivileged(new PropertyReadAction(PROPERTY_DEBUG)));
    }

    /**
	 * Creates server using parameters defined by system properties or fallbacks
	 * to default in case properties are not defined.
	 * 
	 * @throws IOException
	 *             if initialization fails
	 */
    public Server() throws IOException {
        this(AccessController.doPrivileged(new PropertyReadAction(PROPERTY_GUID)), AccessController.doPrivileged(new PropertyReadAction(PROPERTY_BIND_ADDRESS)), AccessController.doPrivileged(new PropertyReadAction(PROPERTY_BIND_PORT)) != null ? Integer.parseInt(AccessController.doPrivileged(new PropertyReadAction(PROPERTY_BIND_PORT))) : 0, AccessController.doPrivileged(new PropertyReadAction(PROPERTY_BIND_BACKLOG)) != null ? Integer.parseInt(AccessController.doPrivileged(new PropertyReadAction(PROPERTY_BIND_BACKLOG))) : 50, AccessController.doPrivileged(new PropertyReadAction(PROPERTY_ADDRESS)), AccessController.doPrivileged(new PropertyReadAction(PROPERTY_PORT)) != null ? Integer.parseInt(AccessController.doPrivileged(new PropertyReadAction(PROPERTY_PORT))) : 0, AccessController.doPrivileged(new PropertyReadAction(PROPERTY_PATH)), AccessController.doPrivileged(new PropertyReadAction(PROPERTY_CODEBASE)));
    }

    /**
	 * Creates server, thats <code>host</code> and <code>port</code> are
	 * equal to <code>bindHost</code> and <code>bindPort</code>.
	 * 
	 * @param guid
	 *            guid of server
	 * @param host
	 *            host name
	 * @param port
	 *            port
	 * @param backlog
	 *            maximum length of incoming connections queue
	 * @param path
	 *            list of paths
	 * @param codebase
	 *            codebase
	 * @throws IOException
	 *             if creation fails
	 */
    public Server(String guid, String host, int port, int backlog, String path, String codebase) throws IOException {
        this(guid, host, port, backlog, host, port, path, codebase);
    }

    /**
	 * Creates server and binds server socket.
	 * 
	 * @param guid
	 *            guid of server
	 * @param bindHost
	 *            host name of interface to listen on
	 * @param bindPort
	 *            port number to listion on
	 * @param backlog
	 *            maximum length of incoming connections queue
	 * @param host
	 *            host name to be send to other servers
	 * @param port
	 *            port number to be send to other servers
	 * @param path
	 *            list of paths
	 * @param codebase
	 *            codebase
	 * @throws IOException
	 *             if creation or port binding fails
	 */
    public Server(String guid, String bindHost, int bindPort, int backlog, String host, int port, String path, String codebase) throws IOException {
        InetAddress bindAddress;
        try {
            bindAddress = AccessController.doPrivileged(new AddressAction(bindHost));
        } catch (PrivilegedActionException e) {
            throw (UnknownHostException) e.getException();
        }
        InetSocketAddress socketAddress = AccessController.doPrivileged(new SocketAddressAction(bindAddress, bindPort));
        try {
            serverSocket = AccessController.doPrivileged(new ServerSocketBindAction(socketAddress, backlog));
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        assert serverSocket.isBound() : "socket is not bound";
        if (guid == null) {
            generateGUID();
        } else {
            this.guid = guid;
        }
        this.bindHost = bindHost != null ? bindHost : AccessController.doPrivileged(new HostNameAction(serverSocket.getInetAddress()));
        this.bindPort = bindPort != 0 ? bindPort : serverSocket.getLocalPort();
        this.host = host != null ? host : this.bindHost;
        this.port = port != 0 ? port : this.bindPort;
        url = new URL(PROTOCOL, this.host, this.port, "/" + this.guid);
        if (path != null) {
            String[] paths = path.split(File.pathSeparator);
            this.path = new File[paths.length];
            for (int i = 0; i < paths.length; i++) {
                File f = new File(paths[i]);
                if (AccessController.doPrivileged(new DirectoryExistsAction(f)) || (AccessController.doPrivileged(new FileExistsAction(f)) && f.getName().endsWith(".jar"))) {
                    this.path[i] = f;
                } else {
                    throw new IOException("path component " + paths[i] + " neither refers to a directory nor to a jar file");
                }
            }
        }
        AccessController.doPrivileged(new ShutdownHookAction(new ShutdownHook(this)));
        if (codebase != null && !codebase.isEmpty()) {
            classLoader = URLClassLoaderFactory.getURLClassLoader(new URL(codebase));
        }
        assert this.bindHost != null;
        assert this.bindPort != 0;
        assert this.host != null;
        assert this.port != 0;
        connPoolKeeper = new ConnectionPoolKeeper();
        connPoolKeeper.start();
    }

    /**
	 * Generates new random GUID.
	 * 
	 * @see UUID#randomUUID()
	 */
    public void generateGUID() {
        guid = UUID.randomUUID().toString();
    }

    /**
	 * Performs remote method invocation and registers a listener to receive the
	 * callback.
	 * 
	 * @param url
	 *            remote method URL
	 * @param arguments
	 *            arguments for the remote method
	 * @param listener
	 *            a listener to receive the result, exception or failure
	 * @throws PpException
	 *             when message sending fails
	 */
    public void invocation(URL url, Object[] arguments, InvocationListener listener) throws PpException {
        if (debug) {
            System.out.println("Invocation: " + url);
        }
        long lid = addInvocationListener(listener);
        Message message = new Message();
        message.setProperty(Property.ADDRESS_SENDER, getHost());
        message.setProperty(Property.PORT_SENDER, String.valueOf(getPort()));
        message.setProperty(Property.ADDRESS_RECEIVER, url.getHost());
        message.setProperty(Property.PORT_RECEIVER, String.valueOf(url.getPort()));
        String[] path = url.getPath().split("/", POSITIONS_INVOCATION);
        message.setProperty(Property.GUID_SENDER, guid);
        message.setProperty(Property.GUID_RECEIVER, path[POSITION_GUID]);
        message.setProperty(Property.TYPE, Type.INVOCATION.name());
        message.setProperty(Property.OBJECT, path[POSITION_OBJECT]);
        message.setProperty(Property.METHOD, path[POSITION_METHOD]);
        message.setProperty(Property.LISTENER, String.valueOf(lid));
        if (url.getUserInfo() != null) {
            String[] userinfo = url.getUserInfo().split(":", POSITIONS_USERINFO);
            if (userinfo.length > POSITION_USERNAME) {
                message.setProperty(Property.USERNAME, userinfo[POSITION_USERNAME]);
            }
            if (userinfo.length > POSITION_PASSWORD) {
                message.setProperty(Property.PASSWORD, userinfo[POSITION_PASSWORD]);
            }
        }
        try {
            message.setContent(serializeObject(arguments));
        } catch (IOException e) {
            throw new PpLocalIOException("Arguments serialization failure", e);
        }
        try {
            send(message);
        } catch (IOException e) {
            throw new PpLocalIOException("Sending failure", e);
        }
    }

    /**
	 * Prepares message with request for a resource and sends it to another
	 * server. Listener is added to the pool of waiting listeners with new
	 * unique id.
	 * 
	 * @param url
	 *            URL of resource
	 * @param listener
	 *            listener which is called after resource is available or
	 *            exception has been received
	 * @throws PpLocalIOException
	 *             if sending fails
	 */
    public void resource(URL url, ResourceListener listener) throws PpLocalIOException {
        if (debug) {
            System.out.println("Resource: " + url);
        }
        long lid = addResourceListener(listener);
        Message message = new Message();
        message.setProperty(Property.ADDRESS_SENDER, getHost());
        message.setProperty(Property.PORT_SENDER, String.valueOf(getPort()));
        message.setProperty(Property.ADDRESS_RECEIVER, url.getHost());
        message.setProperty(Property.PORT_RECEIVER, String.valueOf(url.getPort()));
        String[] path = url.getPath().split("/", POSITIONS_RESOURCE);
        message.setProperty(Property.GUID_SENDER, guid);
        message.setProperty(Property.GUID_RECEIVER, path[POSITION_GUID]);
        message.setProperty(Property.TYPE, Type.RESOURCE.name());
        message.setProperty(Property.RESOURCE, path[POSITION_RESOURCE]);
        message.setProperty(Property.LISTENER, String.valueOf(lid));
        try {
            send(message);
        } catch (IOException e) {
            throw new PpLocalIOException("Sending failure", e);
        }
    }

    /**
	 * Processes incoming messages and distributes across responsible methods or
	 * other servers. The incoming messages are passed to <code>route</code>
	 * (if GUID does not match this server), <code>process*</code> (if it is
	 * request) or <code>handler*</code> (if it is answer) methods.
	 * 
	 * @param message
	 *            message to be considered
	 * @throws IOException
	 *             if processing fails
	 */
    void process(Message message) throws IOException {
        String receiverGUID = message.getProperty(Property.GUID_RECEIVER);
        if (guid.equals(receiverGUID)) {
            Type type = Type.valueOf(message.getProperty(Property.TYPE));
            if (type == Type.INVOCATION) {
                processInvocation(message);
            } else if (type == Type.INVOCATION_RESULT) {
                handleInvocationResult(message);
            } else if (type == Type.INVOCATION_ERROR) {
                handleInvocationError(message);
            } else if (type == Type.RESOURCE) {
                processResource(message);
            } else if (type == Type.RESOURCE_RESULT) {
                handleResourceResult(message);
            } else if (type == Type.RESOURCE_ERROR) {
                handleResourceError(message);
            }
        } else {
            route(message);
        }
    }

    /**
	 * Routes message thats receiver is not this instance of server. Receiver
	 * address and receiver port is exchanged using <code>routes</code> table.
	 * If there is no route to GUID and message is invocation or resource
	 * request, answer with <code>PpGUIDException is sent back</code> to
	 * sender. If the same problem occurs for answer, the message is
	 * disregarded.
	 * 
	 * @param message
	 *            message to be routed
	 * @throws IOException
	 *             if error reply message cannot be sent
	 */
    private void route(Message message) throws IOException {
        String receiverGUID = message.getProperty(Property.GUID_RECEIVER);
        if (routes.containsKey(receiverGUID)) {
            RouteEntry entry = routes.get(receiverGUID);
            message.setProperty(Property.ADDRESS_RECEIVER, entry.host);
            message.setProperty(Property.PORT_RECEIVER, String.valueOf(entry.port));
            try {
                send(message);
            } catch (IOException io) {
                Message answer = new Message();
                Type type = Type.valueOf(message.getProperty(Property.TYPE));
                switch(type) {
                    case INVOCATION:
                        setAnswerAddress(message, answer);
                        answer.setProperty(Property.TYPE, Type.INVOCATION_ERROR.name());
                        answer.setContent(serializeObject(new PpRemoteException("unable to route message to " + entry.host + ":" + entry.port, io)));
                        send(answer);
                        break;
                    case RESOURCE:
                        setAnswerAddress(message, answer);
                        answer.setProperty(Property.TYPE, Type.RESOURCE_ERROR.name());
                        answer.setContent(serializeObject(new PpRemoteException("unable to route message to " + entry.host + ":" + entry.port, io)));
                        send(answer);
                        break;
                    default:
                        if (debug) {
                            System.err.println("message dropped");
                            System.err.print(message);
                        }
                        break;
                }
            }
        } else {
            Message answer = new Message();
            Type type = Type.valueOf(message.getProperty(Property.TYPE));
            switch(type) {
                case INVOCATION:
                    setAnswerAddress(message, answer);
                    answer.setProperty(Property.TYPE, Type.INVOCATION_ERROR.name());
                    answer.setContent(serializeObject(new PpGuidException(receiverGUID)));
                    send(answer);
                    break;
                case RESOURCE:
                    setAnswerAddress(message, answer);
                    answer.setProperty(Property.TYPE, Type.RESOURCE_ERROR.name());
                    answer.setContent(serializeObject(new PpGuidException(receiverGUID)));
                    send(answer);
                    break;
                default:
                    if (debug) {
                        System.err.println("message dropped");
                        System.err.print(message);
                    }
                    break;
            }
        }
    }

    /**
	 * Processes request of invocation. The following steps are taken in this
	 * order:
	 * <ol>
	 * <li>finding object by its name,</li>
	 * <li>checking username and password,</li>
	 * <li>deserializing arguments,</li>
	 * <li>finding apriopriate method in declared interface,</li>
	 * <li>preparing protection domain and access control context,</li>
	 * <li><b>calling method in access control context,</b></li>
	 * <li>serializing result or exception,</li>
	 * <li>sending answer.</li>
	 * </ol>
	 * 
	 * @see #findMethod(Class, String, Object[])
	 * 
	 * @param message
	 * @throws IOException
	 */
    private void processInvocation(Message message) throws IOException {
        Message answer = new Message();
        String objectName = message.getProperty(Property.OBJECT);
        String objectUsername = message.getProperty(Property.USERNAME);
        String objectPassword = message.getProperty(Property.PASSWORD);
        String methodName = message.getProperty(Property.METHOD);
        setAnswerAddress(message, answer);
        answer.setProperty(Property.OBJECT, message.getProperty(Property.OBJECT));
        answer.setProperty(Property.METHOD, message.getProperty(Property.METHOD));
        try {
            ObjectEntry entry = objects.get(objectName);
            if (entry == null) {
                throw new PpObjectException("No such object: " + objectName);
            }
            if (entry.username != null && !entry.username.equals(objectUsername)) {
                throw new PpAuthorizationException("Invalid user name: " + objectUsername);
            }
            if (entry.password != null && !entry.password.equals(objectPassword)) {
                throw new PpAuthorizationException("Invalid password: " + objectPassword);
            }
            Object[] arguments = null;
            try {
                arguments = (Object[]) deserializeObject(message.getContent());
            } catch (IOException e) {
                throw new PpRemoteIOException("IO error on remote server", e);
            } catch (ClassNotFoundException e) {
                throw new PpRemoteClassException("Class not found by remote server", e);
            }
            Method method = findMethod(entry.iface, methodName, arguments);
            if (method == null) {
                throw new PpMethodException("No such method: " + methodName);
            }
            Object object = entry.object;
            Object result = null;
            try {
                ProtectionDomain protectionDomain = AccessController.doPrivileged(new ProtectionDomainAction(object.getClass()));
                ProtectionDomain[] protectionDomains = new ProtectionDomain[] { protectionDomain };
                AccessControlContext accessControlContext = new AccessControlContext(protectionDomains);
                result = AccessController.doPrivileged(new InvocationAction(method, object, arguments), accessControlContext);
            } catch (PrivilegedActionException e) {
                throw (Exception) e.getException();
            }
            answer.setProperty(Property.TYPE, Type.INVOCATION_RESULT.name());
            try {
                answer.setContent(serializeObject(result));
            } catch (IOException e) {
                throw new PpRemoteIOException("IO error on remote server", e);
            }
        } catch (Exception e) {
            answer.setProperty(Property.TYPE, Type.INVOCATION_ERROR.name());
            try {
                answer.setContent(serializeObject(e));
            } catch (IOException e1) {
                answer.setContent(serializeObject(new PpRemoteIOException(message.toString(), e1)));
            }
        }
        send(answer);
    }

    /**
	 * Processes request of resource. The following steps are taken in this
	 * order:
	 * <ol>
	 * <li>finding object by its name</li>
	 * <li>checking username and password</li>
	 * <li>deserializing arguments</li>
	 * <li>finding apriopriate method in declared interface</li>
	 * <li>preparing protection domain and access control context</li>
	 * <li><b>calling method in access control context</b></li>
	 * <li>serializing result or exception</li>
	 * <li>sending answer</li>
	 * </ol>
	 * 
	 * @see #findMethod(Class, String, Object[])
	 * 
	 * @param message
	 * @throws IOException
	 */
    private void processResource(Message message) throws IOException {
        Message answer = new Message();
        String resourceName = message.getProperty(Property.RESOURCE);
        setAnswerAddress(message, answer);
        answer.setProperty(Property.RESOURCE, message.getProperty(Property.RESOURCE));
        try {
            byte[] resourceContent = resourceContent(resourceName);
            answer.setProperty(Property.TYPE, Type.RESOURCE_RESULT.name());
            answer.setContent(resourceContent);
        } catch (Exception e) {
            answer.setProperty(Property.TYPE, Type.RESOURCE_ERROR.name());
            try {
                answer.setContent(serializeObject(e));
            } catch (IOException e1) {
                answer.setContent(serializeObject(new PpRemoteIOException(message.toString(), e1)));
            }
        }
        send(answer);
    }

    /**
	 * Handles answer message with result and calls listener waiting for it.
	 * 
	 * @param message
	 *            answer
	 */
    private void handleInvocationResult(Message message) {
        long lid = Integer.valueOf(message.getProperty(Property.LISTENER));
        InvocationListener listener = removeInvocationListener(lid);
        if (listener == null) {
            System.err.println("error: no invocation listener associated with (duplicate?!) message:");
            if (debug) System.err.print(message);
            return;
        }
        try {
            Object result = deserializeObject(message.getContent());
            listener.result(result);
        } catch (IOException e) {
            listener.exception(new PpLocalIOException(message.toString(), e));
        } catch (ClassNotFoundException e) {
            listener.exception(new PpLocalClassException(message.toString(), e));
        }
    }

    /**
	 * Handles answer message with error and calls listener waiting for it.
	 * 
	 * @param message
	 *            answer
	 */
    private void handleInvocationError(Message message) {
        long lid = Integer.valueOf(message.getProperty(Property.LISTENER));
        InvocationListener listener = removeInvocationListener(lid);
        if (listener == null) {
            System.err.println("error: no invocation listener associated with (duplicate?!) message:");
            if (debug) System.err.print(message);
            return;
        }
        try {
            Exception exception = (Exception) deserializeObject(message.getContent());
            listener.exception(exception);
        } catch (IOException e) {
            listener.exception(new PpLocalIOException(message.toString(), e));
        } catch (ClassNotFoundException e) {
            listener.exception(new PpLocalClassException(message.toString(), e));
        }
    }

    /**
	 * Handles answer message with resource and calls listener waiting for it.
	 * 
	 * @param message
	 *            answer
	 */
    private void handleResourceResult(Message message) {
        long lid = Integer.valueOf(message.getProperty(Property.LISTENER));
        ResourceListener listener = removeResourceListener(lid);
        if (listener == null) {
            System.err.println("error: no resource listener associated with (duplicate?!) message:");
            if (debug) System.err.print(message);
            return;
        }
        byte[] content = message.getContent();
        listener.data(content);
    }

    /**
	 * Handles answer message with exception and calls listener waiting for it.
	 * 
	 * @param message
	 *            answer
	 */
    private void handleResourceError(Message message) {
        long lid = Integer.valueOf(message.getProperty(Property.LISTENER));
        ResourceListener listener = removeResourceListener(lid);
        if (listener == null) {
            System.err.println("error: no resource listener associated with (duplicate?!) message:");
            if (debug) System.err.print(message);
            return;
        }
        byte[] exception = message.getContent();
        listener.exception(exception);
    }

    /**
	 * Finds first method with specified name and correct number of arguments.
	 * May fail with methods with the same number of names and arguments but
	 * different arguments' types.
	 * 
	 * @param iface
	 * @param name
	 * @param arguments
	 * @return method with correct name and number of arguments
	 */
    private Method findMethod(Class<? extends PpRemote> iface, String name, Object[] arguments) {
        int argumentsCount = arguments == null ? 0 : arguments.length;
        Method[] methods = iface.getMethods();
        for (Method method : methods) {
            if (name.equals(method.getName()) && method.getParameterTypes().length == argumentsCount) {
                return method;
            }
        }
        return null;
    }

    /**
	 * Sets the return address on the answer message, by inspecting request and
	 * requesting server. Duplicating code in all <code>process...</code>
	 * methods.
	 * 
	 * @param message
	 *            the received request message
	 * @param answer
	 *            answer being generated
	 */
    private void setAnswerAddress(Message message, Message answer) {
        answer.setProperty(Property.ADDRESS_SENDER, getHost());
        answer.setProperty(Property.PORT_SENDER, String.valueOf(getPort()));
        answer.setProperty(Property.ADDRESS_RECEIVER, message.getProperty(Property.ADDRESS_SENDER));
        answer.setProperty(Property.PORT_RECEIVER, message.getProperty(Property.PORT_SENDER));
        answer.setProperty(Property.GUID_SENDER, getGUID());
        answer.setProperty(Property.GUID_RECEIVER, message.getProperty(Property.GUID_SENDER));
        answer.setProperty(Property.LISTENER, message.getProperty(Property.LISTENER));
    }

    /**
	 * Finds the resource on path and reads its content.
	 * 
	 * @see #path
	 * 
	 * @param resourceName
	 *            name of resource, path to file
	 * @return contents of resource
	 * @throws IOException
	 *             if error occurs while accessing resource
	 */
    private byte[] resourceContent(String resourceName) throws IOException {
        if (path == null) {
            throw new IOException("No resources available");
        }
        InputStream input = null;
        for (File f : path) {
            if (f.isDirectory()) {
                File file = new File(f, resourceName.replace('/', File.separatorChar));
                boolean exists = AccessController.doPrivileged(new FileExistsAction(file));
                if (exists) {
                    try {
                        input = AccessController.doPrivileged(new FileInputStreamAction(file));
                    } catch (PrivilegedActionException e) {
                        throw (IOException) e.getException();
                    }
                    break;
                }
            } else {
                try {
                    JarFile jar = AccessController.doPrivileged(new JarFileAction(f));
                    ZipEntry entry = jar.getEntry(resourceName);
                    if (entry != null) {
                        input = jar.getInputStream(entry);
                        break;
                    }
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
            }
        }
        if (input == null) {
            throw new FileNotFoundException("No such file: " + resourceName);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int size;
        byte[] buffer = new byte[SIZE_BUFFER];
        while ((size = input.read(buffer)) != -1) {
            output.write(buffer, 0, size);
        }
        input.close();
        output.close();
        return output.toByteArray();
    }

    /**
	 * Serializes object to array of bytes.
	 * 
	 * @param object
	 *            object to serialize
	 * @return serialized content as array of bytes
	 * @throws IOException
	 *             if serialization fails
	 */
    private byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(arrayStream);
        objectStream.writeObject(object);
        objectStream.close();
        return arrayStream.toByteArray();
    }

    /**
	 * Deserialize object from byte array.
	 * 
	 * @param content
	 *            bytes with content
	 * @return deserialized object
	 * @throws IOException
	 *             if deserialization fails
	 * @throws ClassNotFoundException
	 *             if object is of uknown class
	 */
    Object deserializeObject(byte[] content) throws IOException, ClassNotFoundException {
        ByteArrayInputStream arrayStream = new ByteArrayInputStream(content);
        ObjectInputStream objectStream = (classLoader != null ? new ObjectInputStreamWithLoader(arrayStream, classLoader) : new ObjectInputStream(arrayStream));
        return objectStream.readObject();
    }

    /**
	 * Sends message to receiver using TCP connection.
	 * 
	 * @param message
	 *            message to be sent
	 * @throws IOException
	 *             if TCP connection or serialization fails
	 */
    private void send(Message message) throws IOException {
        String host = message.getProperty(Property.ADDRESS_RECEIVER);
        int port = Integer.valueOf(message.getProperty(Property.PORT_RECEIVER));
        String key = host + ":" + port;
        boolean reusing = false, tempConn = false;
        ConnectionPoolEntry conn;
        synchronized (connectionPool) {
            if (connectionPool.containsKey(key)) {
                conn = connectionPool.get(key);
                conn.timestamp = System.currentTimeMillis();
                reusing = true;
            } else {
                try {
                    conn = new ConnectionPoolEntry(AccessController.doPrivileged(new SocketConnectAction(host, port)));
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                if (connPoolKeeper.active) connectionPool.put(key, conn); else tempConn = true;
            }
        }
        try {
            synchronized (conn) {
                message.write(conn.out);
                conn.out.flush();
                int ack = conn.in.read();
                if (ack < 0 || (byte) ack != ACK_OK) throw new IOException("sending the message failed");
            }
        } catch (IOException io) {
            synchronized (connectionPool) {
                connectionPool.remove(key);
            }
            synchronized (conn) {
                conn.close();
            }
            if (reusing) {
                if (debug) {
                    System.err.println("reusing TCP connection to " + key + " failed:");
                    io.printStackTrace();
                }
                try {
                    conn = new ConnectionPoolEntry(AccessController.doPrivileged(new SocketConnectAction(host, port)));
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                try {
                    message.write(conn.out);
                    conn.out.flush();
                    int ack = conn.in.read();
                    if (ack < 0 || (byte) ack != ACK_OK) throw new IOException("sending the message failed");
                } catch (IOException e) {
                    conn.close();
                    throw e;
                }
                synchronized (connectionPool) {
                    if (connPoolKeeper.active) connectionPool.put(key, conn); else tempConn = true;
                }
            } else {
                throw io;
            }
        }
        if (tempConn) {
            conn.close();
        }
    }

    /**
	 * Receives message from TCP connection established by another server.
	 * 
	 * @see Receiver
	 * 
	 * @param socket
	 * @return read message
	 * @throws IOException
	 *             if TCP connection or deserialization fails
	 */
    Message receive(InputStream in, OutputStream out) throws IOException {
        Message message;
        try {
            message = new Message(in);
            out.write(Server.ACK_OK);
            out.flush();
        } catch (IOException e) {
            try {
                out.write(Server.ACK_ERROR);
                out.flush();
            } catch (IOException ex) {
            }
            throw e;
        }
        return message;
    }

    /**
	 * Receives messages in new threads as long as server socket is open,
	 * otherwise stops.
	 * 
	 * @see java.lang.Runnable#run()
	 */
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                try {
                    new Thread(new ReceiverConnection(this, AccessController.doPrivileged(new SocketAcceptAction(serverSocket)))).start();
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
            } catch (SocketException e) {
                continue;
            } catch (IOException e) {
                System.err.println("error receiving message:");
                e.printStackTrace();
            }
        }
    }

    /**
	 * Adds route to routing table. Message with <code>guid</code> will be
	 * sent to <code>host:port</code>.
	 * 
	 * @param guid
	 *            GUID
	 * @param host
	 *            host name
	 * @param port
	 *            port
	 */
    public void addRoute(String guid, String host, int port) {
        assert guid != null;
        assert host != null;
        assert port != 0;
        RouteEntry entry = new RouteEntry(guid, host, port);
        routes.put(guid, entry);
    }

    /**
	 * Removes route.
	 * 
	 * @param guid
	 *            GUID
	 */
    public void removeRoute(String guid) {
        routes.remove(guid);
    }

    /**
	 * Registers object making resource available to other servers under the
	 * path: <br>
	 * <code>pp://server_address:server_port/server_id/name</code> and methods
	 * under the corresponding addresses:
	 * <code>pp://server_address:server_port/server_id/name/method_name</code>
	 * 
	 * @param name
	 *            name under which the object is made available
	 * @param object
	 *            object reference
	 * @param iface
	 *            iterface under which object is available
	 * @param username
	 *            optional user name for acces control, may be null
	 * @param password
	 *            optional password for acces control, may be null
	 */
    public void addObject(String name, PpRemote object, Class<? extends PpRemote> iface, String username, String password) {
        assert name != null;
        assert object != null;
        assert iface != null;
        ObjectEntry entry = new ObjectEntry(name, object, iface, username, password);
        objects.put(name, entry);
    }

    /**
	 * Deregisters object.
	 * 
	 * @param name
	 *            name under which the object is registered
	 * @return object
	 */
    public PpRemote removeObject(String name) {
        return objects.remove(name).object;
    }

    /**
	 * Registers invocation listener under new unique ID.
	 * 
	 * @param listener
	 *            listener to be registered
	 * @return ID given to listener
	 */
    private long addInvocationListener(InvocationListener listener) {
        if (listener == null) {
            throw new NullPointerException("invocation listener is null");
        }
        long lid = nextListenerId();
        invocationListeners.put(lid, listener);
        return lid;
    }

    /**
	 * Deregisteres listener. All messages to this listener will be disregarded.
	 * 
	 * @param lid
	 *            ID of listener
	 * @return listener
	 */
    private InvocationListener removeInvocationListener(long lid) {
        return invocationListeners.remove(lid);
    }

    /**
	 * Registers resource listener under new unique ID.
	 * 
	 * @param listener
	 *            listener to be registered
	 * @return ID given to listener
	 */
    private long addResourceListener(ResourceListener listener) {
        if (listener == null) {
            throw new NullPointerException("resource listener is null");
        }
        long lid = nextListenerId();
        resourceListeners.put(lid, listener);
        return lid;
    }

    /**
	 * Deregisteres listener. All messages to this listener will be disregarded.
	 * 
	 * @param lid
	 *            ID of listener
	 * @return listener
	 */
    private ResourceListener removeResourceListener(long lid) {
        return resourceListeners.remove(lid);
    }

    /**
	 * Computes next id unique for all messages from that server. Thread safe
	 * implementation.
	 * 
	 * @see #listenerId
	 * 
	 * @return unique id
	 */
    private synchronized long nextListenerId() {
        assert listenerId >= 0 : "id overflow";
        return ++listenerId;
    }

    /**
	 * Gets host name or IP address of interface to which the socket is bound.
	 * 
	 * @return host name or IP address
	 */
    public String getBindHost() {
        return bindHost;
    }

    /**
	 * Gets port number of the socket that servers listen on.
	 * 
	 * @return port number
	 */
    public int getBindPort() {
        return bindPort;
    }

    /**
	 * Gets host name or IP address that is visible on Internet. Used to
	 * traverse NAT.
	 * 
	 * @return host name or IP address
	 */
    public String getHost() {
        return host;
    }

    /**
	 * Gets port that is visible on Internet. Used to traverse NAT.
	 * 
	 * @return port number
	 */
    public int getPort() {
        return port;
    }

    /**
	 * Gets server uid.
	 * 
	 * @return server uid
	 */
    public String getGUID() {
        return guid;
    }

    public File[] getPath() {
        return path;
    }

    /**
	 * Gets URL of server instance.
	 * 
	 * @return URL of server instance
	 */
    public URL getURL() {
        return url;
    }

    /**
	 * Checks if server is stoped (socket closed).
	 * 
	 * @return true if server is stopped (closed)
	 */
    public boolean isClosed() {
        return serverSocket.isClosed();
    }

    /**
	 * Stops server by closing binded server socket.
	 * 
	 * @throws IOException
	 * 
	 * @see java.io.Closeable#close()
	 */
    public void close() throws IOException {
        connPoolKeeper.stopIt();
        if (serverSocket != null) {
            serverSocket.close();
        }
        connPoolKeeper.shutdown();
    }

    /**
	 * Exchanges GUID in URL.
	 * 
	 * @param url
	 *            source URL with old GUID
	 * @param guid
	 *            new GUID
	 * @return URL with new GUID
	 * @throws MalformedURLException
	 *             if URL creation fails
	 */
    public static URL exchangeGUID(URL url, String guid) throws MalformedURLException {
        return new URL(url.getProtocol() + "://" + url.getAuthority() + "/" + guid);
    }

    /**
	 * Creates and sets default server. If there is already a default server it
	 * does nothing.
	 * 
	 * @throws IOException
	 *             if creation and server start fails
	 */
    public static void startDefaultServer() throws IOException {
        if (defaultServer == null) {
            defaultServer = new Server();
            new Thread(defaultServer).start();
        }
    }

    /**
	 * Stops (closes) default protocol server.
	 * 
	 * @throws IOException
	 *             if closing failed
	 */
    public static void stopDefaultServer() throws IOException {
        if (defaultServer != null) {
            defaultServer.close();
            defaultServer = null;
        }
    }

    /**
	 * Gets default protocol server.
	 * 
	 * @return default protocol server
	 */
    public static Server getDefaultServer() {
        return defaultServer;
    }

    /**
	 * Sets default protocol server.
	 * 
	 * @param server
	 *            server to become default
	 */
    public static void setDefaultServer(Server server) {
        defaultServer = server;
    }

    /**
	 * Gets proxy factor associeted with server instance.
	 * 
	 * @return proxy factory
	 */
    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    /**
	 * Gets timeout invocator associeted with server instance.
	 * 
	 * @return timeout invocator
	 */
    public TimeoutInvocator getTimeoutInvocator() {
        return timeoutInvocator;
    }

    /**
	 * Object description.
	 */
    private class ObjectEntry {

        /**
		 * Name of the object.
		 */
        public String name;

        /**
		 * Object itself.
		 */
        public PpRemote object;

        /**
		 * Interface that's methods are called on object.
		 */
        public Class<? extends PpRemote> iface;

        /**
		 * Name of user that can call methods.
		 */
        public String username;

        /**
		 * Password to call methods.
		 */
        public String password;

        /**
		 * Creates new description of object.
		 * 
		 * @param name
		 *            name of object
		 * @param object
		 *            object itself
		 * @param iface
		 *            interface
		 * @param username
		 *            user name
		 * @param password
		 *            password
		 */
        public ObjectEntry(String name, PpRemote object, Class<? extends PpRemote> iface, String username, String password) {
            this.name = name;
            this.object = object;
            this.iface = iface;
            this.username = username;
            this.password = password;
        }

        /**
		 * Equality is defined by equality of names.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof ObjectEntry) {
                ObjectEntry entry = (ObjectEntry) obj;
                return name.equals(entry.name);
            }
            return false;
        }

        /**
		 * Hashcode of name.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        /**
		 * Name of object.
		 * 
		 * @see java.lang.Object#toString()
		 */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
	 * Route description.
	 */
    private class RouteEntry {

        /**
		 * GUID
		 */
        public String guid;

        /**
		 * Host name.
		 */
        public String host;

        /**
		 * Port number.
		 */
        public int port;

        /**
		 * Creates route description.
		 * 
		 * @param guid
		 *            GUID
		 * @param host
		 *            host name
		 * @param port
		 *            port number
		 */
        public RouteEntry(String guid, String host, int port) {
            this.guid = guid;
            this.host = host;
            this.port = port;
        }

        /**
		 * Equality defined by GUID.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof RouteEntry) {
                RouteEntry entry = (RouteEntry) obj;
                return guid.equals(entry.guid);
            }
            return false;
        }

        /**
		 * Hash code of GUID.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
        @Override
        public int hashCode() {
            return guid.hashCode();
        }

        /**
		 * GUID.
		 * 
		 * @see java.lang.Object#toString()
		 */
        @Override
        public String toString() {
            return guid;
        }
    }
}
