package iwork.eheap2;

import iwork.eheap2.net.*;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.*;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.X509V1CertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *  This class provides an interface to an event heap in which the application
 *  is participating.  An event heap is characterized by the server name for
 *  the machine on which it is running and the port being used on that machine.
 */
public class SecureEventHeap implements EventHeapConfigValues, NetConfigValues, FieldValueTypes {

    /**Variable used to register filtering events
     */
    public static final String EH_FILTER_EVENTS = "filterEvents";

    private Set registrations = new HashSet();

    private static Boolean allowReRegistration = new Boolean(false);

    /** This is a thread class that is set up as a shutdown hook to shut down
   * the Event Heap cleanly (meaning all unsent data is flushed, among other
   * things 
   */
    static class ShutdownEventHeap extends Thread {

        public void run() {
            synchronized (connections) {
                Iterator connectionIter = connections.values().iterator();
                while (connectionIter.hasNext()) ((BufferedSenderReceiver) connectionIter.next()).stop();
            }
        }
    }

    /** Inner class to enable hack to find name of class at the bottom of the
   * call stack, which is presumably also the name of the application.  Used
   * by createApplicationName to come up with an application name for
   * all EventHeap client objects in this JVM.
   */
    static class AppNameFinder extends SecurityManager {

        public String getAppName() {
            Class[] callStack = getClassContext();
            return callStack[callStack.length - 1].getName();
        }
    }

    /** Inner class that is used for event registration, and is passed to the
   * client application as a handle to allow deregistration for events.*/
    class Registration extends Thread implements EventRegistration {

        SecureEvent[] ourTemplateEvents;

        EventCallback ourCallback;

        String registrationType;

        WireBundle registerBundle = null;

        boolean active = true;

        /** Sets everything up and starts the callback thread running.
     *
     * @throws EventHeapException An exception may be thrown if any of the
     * templateEvents are malformed.
     */
        Registration(SecureEvent[] templateEvents, EventCallback callback, String streamType) throws EventHeapException {
            ourTemplateEvents = templateEvents;
            ourCallback = callback;
            registrationType = streamType;
            prepareTemplateArray(templateEvents);
            setName("Event Callback Thread");
            setDaemon(true);
            start();
        }

        /** See {@link EventRegistration} description. */
        public void deregister() {
            active = false;
        }

        /** Thread to make callbacks. */
        public void run() {
            while (true) {
                SecureEvent[] retEvents = null;
                SecureEvent retEvent = null;
                try {
                    retEvent = callStream(this);
                    if (retEvent != null) System.out.println(retEvent.toStringComplete());
                    if (retEvent != null) retEvents = createReturnArray(retEvent, ourTemplateEvents); else continue;
                } catch (EventHeapException e) {
                    continue;
                }
                if (active) {
                    if (!ourCallback.returnEvent(retEvents)) {
                        deregister();
                        break;
                    }
                } else {
                    break;
                }
            }
            try {
                SecureEvent[] deregisterArray = new SecureEvent[1];
                deregisterArray[0] = new SecureEvent(EHS_DEREGISTER_EVENT);
                deregisterArray[0].setPostValue(EHS_STREAMID, registerBundle.returnTag);
                callVoid(EH_DEREGISTER, deregisterArray, serverTimeout);
                theServer.deregisterStream(registerBundle.returnTag);
            } catch (Exception e) {
            }
        }
    }

    /** Used to track the highest sequence numbers submitted through this
   * object.
   */
    Map localSequenceNumMap = Collections.synchronizedMap(new HashMap());

    /** A random number between 0 and MAX_INT that should be unique per
   * SourceID per program run.  It is used in event sequencing.
   */
    Integer sessionID;

    /** Maps eventTypes to maps containing sources which have generated this
   * event type.  NOTE: you always must synchronize on this and on the
   * maps it contains. */
    Map eventTypes = new HashMap();

    /** The server machine to which we are supposed to be connected */
    String machine;

    /** The port on the server machine to which we are supposed to be 
   *  connected */
    int port;

    /** The connection to the remote Event Heap server */
    BufferedSenderReceiver theServer;

    /** Static map containing all BufferedSenderReceivers for this JVM */
    private static Map connections = new HashMap();

    /** The amount of time for calls to block before returning even if they
   * don't have a result */
    long serverTimeout = 0;

    /** the current debug level for this EventHeap object */
    private static int debugLevel = 0;

    /** the current debug stream for this EventHeap object */
    private static PrintStream debugStream = System.err;

    private static boolean firstOne = true;

    private static KeyStore ks;

    private static KeyManagerFactory kmf;

    private static TrustManagerFactory tmf;

    private static SSLContext sslContext;

    private static SSLSocketFactory socketFactory;

    /** The unique source represented by this object. */
    String source = null;

    /** Serves as a lock for the static variables application and device */
    static Integer staticLock = new Integer(1);

    /** The name of the running application.  Applies to all EventHeap objects
   * in a given JVM.
   */
    static String application = null;

    /** The name of the device on which this application is running.
   * Applies to all EventHeap objects in a given JVM. */
    static String device = null;

    /** Serves as a lock for the per instance variables person and group. */
    Integer instanceLock = new Integer(1);

    /** The person to whom all events generated by this EventHeap client
   *  object will be attributed, and for whom incoming events will be
   *  matched.  May be null. */
    private String person = null;

    /** The group to which all events generated by this EventHeap client
   *  object will be attributed, and for whom incoming events will be
   *  matched.  May be null. */
    String group = null;

    /** Static initializer for the class.  Adds a shutdown hook to insure
   * all connections get closed when the JVM terminates.
   */
    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownEventHeap());
    }

    /** See the description for {@link #EventHeap(String, String, int,
   * EventHeap, String, String)}. This constructor creates a new Event
   * Heap with machine and port the same as for oldHeap.  'sourceName'
   * must be different than in 'oldHeap', and sequencing of events
   * from this Event Heap object will be independent of the original.
   * It should be used to create a new EventHeap object that is
   * connected to the same Event Heap.  As with {@link
   * #EventHeap(String, String, int, EventHeap, String, String)},
   * setting sourceName to null will cause a name to be created
   * automatically based on the name of the application using the
   * library.
   *
   * @throws EventHeapException An exception is thrown if oldHeap is
   * null.  */
    public SecureEventHeap(SecureEventHeap oldHeap, String sourceName) throws EventHeapException {
        this(sourceName, oldHeap.machine, oldHeap.port, null, null, null);
        if (oldHeap == null) throw new EventHeapException("oldHeap must be non-null");
    }

    /** See the description for {@link #EventHeap(String, String, int,
   * EventHeap, String, String)}. This constructor calls that one with
   * port=-1 and oldHeap=null.
   */
    public SecureEventHeap(String sourceName, String machine) {
        this(sourceName, machine, -1, null, null, null);
    }

    /** See the description for {@link #EventHeap(String, String, int,
   * EventHeap, String, String)}. This constructor calls that one with
   * oldHeap=null.  
   */
    public SecureEventHeap(String sourceName, String machine, int port) {
        this(sourceName, machine, port, null, null, null);
    }

    /** See the description for {@link #EventHeap(String, String, int,
   * EventHeap, String, String)}. This constructor calls that one with
   * sourceName=null, port=-1 and oldHeap=null.  */
    public SecureEventHeap(String machine) {
        this(null, machine, -1, null, null, null);
    }

    /** See the description for {@link #EventHeap(String, String, int,
   * EventHeap, String, String)}. This constructor calls that one with
   * sourceName=null, and oldHeap=null.  */
    public SecureEventHeap(String machine, int port) {
        this(null, machine, port, null, null, null);
    }

    /** See the description for {@link #EventHeap(String, String, int,
   * EventHeap, String, String)}. This constructor calls that one with
   * sourceName=null.  */
    public SecureEventHeap(String machine, int port, SecureEventHeap oldHeap) {
        this(null, machine, port, oldHeap, null, null);
    }

    /** Creates a new Event Heap interface to the specified event heap.
   * The Event Heap which is connected is determined by the given
   * machine and port.  The SourceName specifies the 'Source' that will
   * be used in all events sent from this EventHeap object.  If
   * another Event Heap is passed in, the machine and port parameters
   * will be set to the values from it if those parameters are passed
   * in as 'null' or '-1' (for port).  <p>
   *
   * <b>C++ API Note:</b> The C++ API includes one more parameter, 
   *       clientName. This parameter is ignored in the current
   *       implementation and is left in for legacy support. 
   *
   * @param sourceName The 'Source' for all events generated by this
   * object.  It should be unique among all participants in the Event
   * Heap.  If it is null, a value will be generated based on the
   * name of the application using the library and a random number.
   *
   * @param machine The DNS name of the machine where the Event Heap
   * server is running.  Leave as 'null' to use the value in oldHeap.<p>
   *
   * @param port The port on the machine being used by the Event Heap
   * server.  Leave as -1 for default port.<p>
   *
   * @param oldHeap Another Event Heap instance.  Values for the other
   * parameters are taken from here if this is non-null and they are
   * set to 'null' or -1.  
   *
   * @param deviceName The name of the device on which this Event Heap
   * client is running.  This will be added to events sent from this
   * JVM.  This is only set once per JVM, so if there is already a 
   * value set it is ignored.  If null, the device name will be inferred
   * from the DNS name or IP address of the device running the JVM.
   *
   * @param applicationName The name of the application in which this
   * EventHeap client is being instantiated.  This will be added to
   * events sent from this JVM.  This is only set once per JVM, so if
   * there is already a value set it is ignored.  If null, the
   * application name will be inferred from the class name of the earliest
   * class in the Java stack.
   */
    public SecureEventHeap(String sourceName, String machine, int port, SecureEventHeap oldHeap, String deviceName, String applicationName) {
        boolean debug = false;
        synchronized (staticLock) {
            if (firstOne) {
                try {
                    if (debug) System.out.println("THis is the FIRST ONE!!! WOOHOO!!!");
                    firstOne = false;
                    Security.addProvider(new com.sun.crypto.provider.SunJCE());
                    Security.insertProviderAt(new BouncyCastleProvider(), 2);
                    KeyPairGenerator kg = KeyPairGenerator.getInstance("DSA");
                    kg.initialize(1024);
                    KeyPair kp = kg.generateKeyPair();
                    ks = KeyStore.getInstance("JKS");
                    ks.load(new FileInputStream("default.ks"), "iSec123".toCharArray());
                    if (debug) System.out.println("ks loaded");
                    X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
                    certGen.reset();
                    certGen.setSubjectDN(new X509Principal("CN=" + getApplicationName()));
                    certGen.setIssuerDN(new X509Principal("CN=" + sourceName + ", " + "OU=" + applicationName + ", " + "O=" + deviceName));
                    certGen.setPublicKey(kp.getPublic());
                    certGen.setSerialNumber(new BigInteger(128, new SecureRandom()));
                    certGen.setSignatureAlgorithm("SHA1withDSA");
                    GregorianCalendar date = new GregorianCalendar();
                    date.add(Calendar.DATE, -1);
                    certGen.setNotBefore(date.getTime());
                    date.add(Calendar.DATE, 1);
                    date.add(Calendar.HOUR_OF_DAY, 24);
                    certGen.setNotAfter(date.getTime());
                    if (debug) System.out.println("cert generator initialized");
                    java.security.cert.Certificate selfcert = certGen.generateX509Certificate(kp.getPrivate());
                    Socket sock = new Socket("localhost", 2000);
                    (new ObjectOutputStream(sock.getOutputStream())).writeObject(selfcert);
                    ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                    Object receivedObject;
                    java.security.cert.Certificate[] certs = null;
                    while ((receivedObject = ois.readObject()) != null) {
                        if (receivedObject instanceof java.security.cert.Certificate[]) {
                            certs = (java.security.cert.Certificate[]) receivedObject;
                            if (debug) System.out.println("Certificate Chain Received!");
                            if (debug) System.out.println(certs[0]);
                            if (debug) System.out.println(certs[1]);
                            break;
                        } else {
                            if (debug) System.out.println(receivedObject.toString());
                            break;
                        }
                    }
                    sock.close();
                    ks.setKeyEntry("default", kp.getPrivate(), "iSec123".toCharArray(), certs);
                    ks.store(new FileOutputStream("default.ks"), "iSec123".toCharArray());
                    synchronized (instanceLock) {
                        String DN = ((X509Certificate) certs[1]).getSubjectDN().toString();
                        person = DN.substring(3, DN.indexOf(','));
                    }
                    if (debug) System.out.println("ks is " + ks);
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    kmf = KeyManagerFactory.getInstance("SunX509");
                    kmf.init(ks, "iSec123".toCharArray());
                    KeyManager kms[] = kmf.getKeyManagers();
                    if (debug) System.out.println("kmf has " + kms.length + " key managers... ");
                    String aliases[] = ((X509KeyManager) kms[0]).getClientAliases("DSA", null);
                    if (debug) System.out.println("kms[0] has " + aliases.length + " aliases");
                    for (int i = 0; i < aliases.length; i++) {
                        if (debug) System.out.println("\t\t" + i + ": " + aliases[i]);
                        java.security.cert.Certificate tempcerts[] = ((X509KeyManager) kms[0]).getCertificateChain(aliases[i]);
                        for (int j = 0; j < tempcerts.length; j++) {
                            if (debug) System.out.println("\t\t\t cert " + j + ":" + tempcerts[j]);
                        }
                        if (debug) System.out.println("--- end of certs for alias ---" + aliases[i] + "\n\n\n\n");
                    }
                    tmf = TrustManagerFactory.getInstance("SunX509");
                    tmf.init(ks);
                    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                    socketFactory = sslContext.getSocketFactory();
                    if (debug) System.out.println("\n\n ----- Certificate verification -----");
                    java.security.cert.Certificate checkcerts[] = ks.getCertificateChain("default");
                    for (int i = 0; i < checkcerts.length; i++) {
                        if (debug) System.out.println("cert number " + i + " is " + checkcerts[i]);
                    }
                } catch (Exception e) {
                    if (debug) System.out.println("Error in SecureEventHeap constructor: " + e.toString());
                }
            }
        }
        synchronized (staticLock) {
            if (device == null) {
                if (deviceName != null) device = deviceName; else {
                    createDeviceName();
                }
            }
            if (application == null) {
                if (applicationName != null) application = applicationName; else {
                    createApplicationName();
                }
            }
        }
        if (sourceName != null && oldHeap != null) {
            machine = oldHeap.machine;
            port = oldHeap.port;
        }
        this.machine = machine;
        this.port = port;
        if (this.port == -1) this.port = EHEAP2_DEFAULT_SECURE_PORT;
        try {
            theServer = connect(this.machine, this.port);
        } catch (EventHeapException e) {
            System.out.println("exception caught when secure event heap trying to connect to server: \n" + e.toString());
            e.printStackTrace();
        }
        sessionID = new Integer((int) (Math.random() * (double) Integer.MAX_VALUE));
        if (sourceName == null) synchronized (staticLock) {
            source = application;
        } else source = sourceName;
        source += "_" + (int) (Math.random() * (double) Integer.MAX_VALUE);
    }

    /** Calls a method on the Event Heap server that returns an array of events, 
   * passing the given event array to that method.  
   *
   * @param methodName The server method to call.
   *
   * @param eventArray The events to pass to the server.
   *
   * @param timeout The maximum number of milliseconds to wait for a response
   * from the server.  
   *
   * @return An array of events from the remote server in response to this
   * call.  Null will be returned if the remote returned null or the
   * call timed out.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    private SecureEvent[] callEvents(String methodName, SecureEvent[] eventArray, long timeout) throws EventHeapException {
        boolean successful = false;
        SecureEvent[] retEvents = null;
        WireBundle sendBundle = new WireBundle();
        sendBundle.outTuples = eventArray;
        sendBundle.destinationTag = methodName;
        sendBundle.returnTag = null;
        while (!successful) {
            try {
                WireBundle retBundle = theServer.call(sendBundle, timeout);
                retEvents = SecureEvent.createSecureEventArray(retBundle.outTuples);
                successful = true;
            } catch (Exception e) {
                try {
                    try {
                        Thread.sleep((long) (2000 * Math.random()));
                    } catch (InterruptedException e3) {
                    }
                    theServer = connect(machine, port);
                    reRegister();
                } catch (EventHeapException e2) {
                }
            }
        }
        return retEvents;
    }

    /** Calls a method on the Event Heap server that returns a single event, 
   * passing the given event array to that method.  Sequencing information
   * for each unique event type is sent along with the events.
   *
   * @param methodName The server method to call.
   *
   * @param eventArray The events to pass to the server.
   *
   * @param timeout The maximum number of milliseconds to wait for a response
   * from the server.  
   *
   * @return The event from the remote server in response to this
   * call.  Null will be returned if the remote returned null or the
   * call timed out.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    private SecureEvent callSequencedEvent(String methodName, SecureEvent[] eventArray, long timeout) throws EventHeapException {
        boolean successful = false;
        SecureEvent retEvent = null;
        WireBundle sendBundle = new WireBundle();
        sendBundle.outTuples = eventArray;
        sendBundle.destinationTag = methodName;
        sendBundle.returnTag = null;
        sendBundle.seqInfo = createSequenceInfoMap(eventArray);
        while (!successful) {
            try {
                WireBundle retBundle = theServer.call(sendBundle, timeout);
                if (retBundle != null && retBundle.outTuples != null && retBundle.outTuples.length > 0) {
                    retEvent = new SecureEvent(retBundle.outTuples[0]);
                } else retEvent = null;
                successful = true;
            } catch (Exception e) {
                try {
                    try {
                        Thread.sleep((long) (2000 * Math.random()));
                    } catch (InterruptedException e3) {
                    }
                    theServer = connect(machine, port);
                    reRegister();
                } catch (EventHeapException e2) {
                }
            }
        }
        return retEvent;
    }

    /** Calls a method on the Event Heap server that is for a
   * notification stream.  Returns the next event from the server for
   * the notification stream represented by the Registration object.
   * A new stream is created and the stream name is noted in the
   * Registration object for future reference if this is the first
   * call for the given Registration object.
   *
   * @param registration The Registration object representing this
   * notification stream.
   *
   * @return The next stream event from the remote server for the
   * stream represented by the EventRegistration object.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.  
   */
    private SecureEvent callStream(Registration registration) throws EventHeapException {
        boolean successful = false;
        SecureEvent retEvent = null;
        if (registration.registerBundle == null) {
            registration.registerBundle = new WireBundle();
            registration.registerBundle.outTuples = registration.ourTemplateEvents;
            registration.registerBundle.destinationTag = registration.registrationType;
            registration.registerBundle.returnTag = null;
        }
        while (!successful) {
            try {
                WireBundle retBundle = theServer.callStream(registration.registerBundle);
                if (retBundle != null && retBundle.outTuples != null && retBundle.outTuples.length > 0) {
                    retEvent = new SecureEvent(retBundle.outTuples[0]);
                } else retEvent = null;
                registration.registerBundle.returnTag = retBundle.destinationTag;
                successful = true;
            } catch (Exception e) {
                try {
                    try {
                        Thread.sleep((long) (2000 * Math.random()));
                    } catch (InterruptedException e3) {
                    }
                    theServer = connect(machine, port);
                    reRegister();
                } catch (EventHeapException e2) {
                }
            }
        }
        return retEvent;
    }

    /** Calls a void method on the Event Heap server, passing the given 
   * event array.  The method won't return until an ACK of the call is
   * received from the server.
   *
   * @param methodName The server method to call.
   *
   * @param eventArray The events to pass to the server.
   *
   * @param timeout The time in milliseconds after which to return even if
   * there is no response from the server.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    private void callVoid(String methodName, SecureEvent[] eventArray, long timeout) throws EventHeapException {
        boolean successful = false;
        WireBundle sendBundle = new WireBundle();
        sendBundle.outTuples = eventArray;
        sendBundle.destinationTag = methodName;
        sendBundle.returnTag = null;
        while (!successful) {
            try {
                theServer.call(sendBundle, timeout);
                successful = true;
            } catch (Exception e) {
                try {
                    try {
                        Thread.sleep((long) (2000 * Math.random()));
                    } catch (InterruptedException e3) {
                    }
                    theServer = connect(machine, port);
                    reRegister();
                } catch (EventHeapException e2) {
                }
            }
        }
    }

    /** Calls a method on the Event Heap server that returns a String.
   * mainly used for trust group function calls
   *
   * added by WENDY
   *
   * @param methodName The server method to call.
   *
   * @param args array of string arguments
   *
   * @param timeout The maximum number of milliseconds to wait for a response
   * from the server.  
   *
   * @return The String from the remote server in response to this
   * call.  Null will be returned if the remote returned null or the
   * call timed out.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    private String callString(String methodName, String[] args, long timeout) throws EventHeapException {
        boolean successful = false;
        WireBundle sendBundle = new WireBundle();
        String separator = SecureEvent.SEPARATOR;
        SecureEvent[] argEvents = new SecureEvent[1];
        SecureEvent arg = new SecureEvent();
        if (args != null) {
            arg.addField("NUMPARAM", Integer.toString(args.length));
            for (int i = 0; i < args.length; i++) arg.addField("PARAM" + Integer.toString(i), args[i]);
        }
        argEvents[0] = arg;
        sendBundle.outTuples = argEvents;
        sendBundle.destinationTag = methodName + separator;
        sendBundle.seqInfo = null;
        sendBundle.returnTag = null;
        if (args != null) for (int i = 0; i < args.length; i++) sendBundle.destinationTag = sendBundle.destinationTag + args[i] + separator;
        boolean debug = false;
        if (debug) System.out.println("callString sending " + sendBundle.destinationTag);
        while (!successful) {
            try {
                WireBundle retBundle = theServer.call(sendBundle, timeout);
                if (retBundle != null) {
                    if (debug) System.out.println("\n^^\ncallString got answer: " + (String) retBundle.outTuples[0].getPostValue("RETURN") + "\n^^");
                    String retValue = (String) retBundle.outTuples[0].getPostValue("RETURN");
                    if (retValue.equals("")) return null;
                    return retValue;
                }
                successful = true;
            } catch (Exception e) {
                try {
                    try {
                        Thread.sleep((long) (2000 * Math.random()));
                    } catch (InterruptedException e3) {
                    }
                    theServer = connect(machine, port);
                    reRegister();
                } catch (EventHeapException e2) {
                }
            }
        }
        return null;
    }

    /** Attempts to connect to the Event Heap server on the given
   * machine at the given port.  If there is an existing active
   * connection to that Event Heap server (i.e. a server listening on
   * the port of the machine identified by the IP address of the
   * machine name), it just returns that BufferedSenderReceiver
   * object.  If there is no connection, or the connection is down, it
   * establishes a connection, adds that BufferedSenderReceiver to the
   * list of known connections, and returns the now connected
   * BufferedSenderReceiver object.  
   *
   * @param machine The machine name where the Event Heap server to
   * connect to is running.
   *
   * @param port The port on the machine on which the desired Event
   * Heap server is listening.
   *
   * @return A valid connection to the Event Heap server in the form of a
   * BufferedSenderReceiver object.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * problem connecting to the Event Heap server, or
   * one or more events are malformed.
   */
    private static synchronized BufferedSenderReceiver connect(String machine, int port) throws EventHeapException {
        try {
            String ipPortString = (InetAddress.getByName(machine)).getHostAddress() + port;
            synchronized (connections) {
                System.out.println("trying to get a connection to " + machine + ":" + port);
                BufferedSenderReceiver theConnection = (BufferedSenderReceiver) (connections.get(ipPortString));
                if (theConnection == null || (theConnection != null && !theConnection.getConnectionUpStatus())) {
                    while (true) {
                        try {
                            System.out.println("SecureEventHeap: creating a secure socket connection to machine:" + machine + ", port: " + port);
                            Socket socket = socketFactory.createSocket(machine, port);
                            System.out.println("socket created, trying to get session");
                            SSLSession sslSession = ((SSLSocket) socket).getSession();
                            System.out.println("got sslsession");
                            java.security.cert.Certificate certs[] = sslSession.getLocalCertificates();
                            System.out.println("got " + certs.length + " certificates");
                            int i = 0;
                            if (socket == null) System.out.println("socket is null"); else System.out.println("socket is not null");
                            theConnection = new BufferedSenderReceiver(socket);
                            System.out.println("SecureEventHeap: SSL connection made");
                        } catch (Exception e2) {
                            System.out.println("connection failed... " + e2.toString());
                            e2.printStackTrace();
                            Thread.sleep(10000);
                            continue;
                        }
                        break;
                    }
                    connections.put(ipPortString, theConnection);
                    synchronized (allowReRegistration) {
                        allowReRegistration = new Boolean(true);
                    }
                }
                return theConnection;
            }
        } catch (Exception e) {
            throw new EventHeapException(e);
        }
    }

    /** Clears all events from the Event Heap to which this object is
   * connected. 
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server.
   */
    public void clear() throws EventHeapException {
        callVoid(EH_CLEAR, null, serverTimeout);
    }

    /** Sets application name to a reasonable value by using the name of
   * the class at the top of the call stack.  The thread calling this
   * method must have the monitor of the staticLock object.
   */
    private static void createApplicationName() {
        try {
            application = (new AppNameFinder()).getAppName();
        } catch (SecurityException e) {
            application = "UNKNOWN_APPLICATION";
        }
    }

    /** Sets device name to a reasonable value by using the DNS or IP
   * address of the local host.  The thread calling this method must
   * have the monitor of the staticLock object.  
   */
    private static void createDeviceName() {
        try {
            device = (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException e) {
            device = "UNKNOWN_HOST";
        }
    }

    /** simple function to create an array of events from a single event
   * to allow the single template event retrieval versions to just
   * call into the multi-template event versions.
   */
    private SecureEvent[] createSecureEventArray(SecureEvent event) {
        SecureEvent[] events = new SecureEvent[1];
        events[0] = event;
        return events;
    }

    /** Called by the multi-fetch versions of the event retrieval methods,
   * this method takes a returned event and the set of templates which 
   * were used to fetch it, and returns an array where the first element
   * is the returned event and the remaining elements are the templateEvents
   * which match the returned event.  If retEvent is null, returns null.
   */
    private SecureEvent[] createReturnArray(SecureEvent retEvent, SecureEvent[] templateEvents) {
        if (templateEvents == null) {
            SecureEvent[] retEvents = new SecureEvent[1];
            retEvents[0] = retEvent;
            return retEvents;
        }
        if (retEvent != null) {
            int i;
            Vector retVector = new Vector();
            for (i = 0; i < templateEvents.length; i++) if (templateEvents[i].matches(retEvent)) retVector.add(templateEvents[i]);
            Object[] retObjects = retVector.toArray();
            SecureEvent[] retEvents = new SecureEvent[retObjects.length + 1];
            retEvents[0] = retEvent;
            for (i = 0; i < retObjects.length; i++) retEvents[i + 1] = (SecureEvent) retObjects[i];
            return retEvents;
        } else return null;
    }

    /** Creates a HashMap of SourceInfo objects containing all known
   * sources, along with last known session ID and sequence number,
   * for the given eventType.  This map is suitable to be passed to
   * sequenced requests to retrieve events from the Event Heap server.
   *
   * @param eventType the event type for which known sources are desired.
   *
   * @return An map of SourceInfo objects, one for each known source
   * of the given Event Type.
   */
    private Map createSequenceMap(String eventType) {
        synchronized (eventTypes) {
            HashMap eventSources;
            if ((eventSources = (HashMap) (eventTypes.get(eventType))) != null) {
                synchronized (eventSources) {
                    return (Map) (eventSources.clone());
                }
            } else return null;
        }
    }

    /** Given a set of template events, creates a map of SequenceInfo objects to
   * send with the templates to the Event Heap server to insure that only
   * events meeting the sequencing rules will be returned.  Specifically,
   * this creates one SequenceInfo object per unique EventType in the set
   * of template events, with the exception that no SequenceInfo object is
   * created for EventTypes that don't yet have sequencing information.  The
   * map is keyed on event type.
   *
   * @param templates The set of template events being sent to the
   * server for some retrieval call.
   *
   * @return A map of SequenceInfo objects keyed on event type, one
   * per unique EventType represented in the template events array.
   * Null is returned if there was no sequencing information for any
   * of the EventTypes represented.  
   */
    Map createSequenceInfoMap(SecureEvent[] templates) {
        Map seqInfo = new HashMap();
        for (int i = 0; i < templates.length; i++) {
            try {
                seqInfo.put(templates[i].getTemplateValue(SecureEvent.EVENTTYPE), null);
            } catch (EventHeapException e) {
            }
        }
        Iterator typeIterator = seqInfo.keySet().iterator();
        while (typeIterator.hasNext()) {
            SequenceInfo newSeqInfo = new SequenceInfo();
            String curType = (String) (typeIterator.next());
            newSeqInfo.eventType = curType;
            newSeqInfo.knownSources = createSequenceMap(curType);
            if (newSeqInfo.knownSources != null) seqInfo.put(curType, newSeqInfo); else typeIterator.remove();
        }
        if (seqInfo.size() > 0) return seqInfo; else return null;
    }

    /** Prints out a debug statement to the debug output strieam if the
   * current debugLevel is higher than the level of the statement.
   * See also {@link #setDebug(int)} and {@link
   * #setDebugStream(PrintStream)}.  */
    public static void debugPrintln(int level, String statement) {
        if (debugLevel >= level) {
            debugStream.println("EH: " + Thread.currentThread().toString() + ": " + statement);
            debugStream.flush();
        }
    }

    /** Permanently removes an event from the Event Heap.  The event passed
   * in should be one previously retrieved using one of the retrieval 
   * methods.
   *
   * @param event An event previously retrieved from the Event Heap.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public void deleteEvent(SecureEvent event) throws EventHeapException {
        callVoid(EH_DELETE_EVENT, createSecureEventArray(event), serverTimeout);
    }

    /** Returns the complete set of events currently in the Event Heap.  No
   * sequencing information is used or affected by this call. 
   *
   * @return An array of events containing a copy of all events currently
   * present in the Event Heap.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server.
   */
    public SecureEvent[] getAll() throws EventHeapException {
        return callEvents(EH_GET_ALL, null, serverTimeout);
    }

    /** Gets an event matching one of the given template events from the
   * Event Heap represented by this object.  If no matching event is
   * available it returns immediately with 'null'.  In addition to the
   * matching event it returns which template or templates matched.
   *
   * @param templateEvents The templates to be used in matching events
   * in the Event Heap passed in as elements of an array.  Only the
   * field type, name and value of non-formal fields are used in the
   * match.  Field order is irrelevant.
   *
   * @return An array of events.  The first element of the array is
   * the next most recent event in the Event Heap not yet retrieved by
   * this object which matches one or more of the given template
   * events.  Subsequent events in the array are the template events
   * used for the match which matched the returned event.  'null' is
   * returned if there are no matching events currently present.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent[] getEvent(SecureEvent[] templateEvents) throws EventHeapException {
        prepareTemplateArray(templateEvents);
        SecureEvent retEvent = callSequencedEvent(EH_GET_EVENT, templateEvents, serverTimeout);
        if (retEvent != null) return createReturnArray(processNewEvent(retEvent), templateEvents); else return null;
    }

    /** Gets an event matching the given template event from the Event
   * Heap represented by this object.  If no matching event is
   * available it returns immediately with 'null'.  
   * 
   * @param templateEvent The template to be used in matching events
   * in the Event Heap.  Only the field type, name and value of non-formal
   * fields are used in the match.  Field order is irrelevant.
   * 
   * @return The next most recent event in the Event Heap not yet
   * retrieved by this object which matches the given template event.
   * 'null' is returned if there are no matching events currently
   * present.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent getEvent(SecureEvent templateEvent) throws EventHeapException {
        SecureEvent[] retEvents = getEvent(createSecureEventArray(templateEvent));
        if (retEvents != null && retEvents.length > 0) return retEvents[0]; else return null;
    }

    /** Returns a string representation of the machine/port of the Event
   * Heap server to which this EventHeap client object is connected.
   * The string is in the form of the remotes IP address followed by a
   * ':' and the remote port (%d.%d.%d.%d:port)
   *
   * @return The IP Address:port string for the remote host 
   */
    public String getServerName() {
        if (theServer != null) return theServer.getRemoteAddress(); else return "<No Connection>";
    }

    /** Returns the application name that is added to events that are sent
   * or to templates used to match incoming events.  This value applies
   * for all EventHeap client objects in a given JVM. 
   */
    public static String getApplicationName() {
        synchronized (staticLock) {
            if (application == null) createApplicationName();
            return application;
        }
    }

    /** Returns the application name that is added to events that are sent
   * or to templates used to match incoming events.  This value applies
   * for all EventHeap client objects in a given JVM. 
   */
    public static String getDeviceName() {
        synchronized (staticLock) {
            if (device == null) createDeviceName();
            return device;
        }
    }

    /** Returns the group name that is added to events that are sent or
   * to templates used to match incoming events.  This value may be
   * different between instances of EventHeap client objects, and may
   * be null.  */
    public String getGroupName() {
        synchronized (instanceLock) {
            return group;
        }
    }

    /** Returns the machine to which this Event Heap object is connected */
    public String getMachine() {
        return machine;
    }

    /** Returns the person to whom events generated by this EventHeap
   *  client object are attributed, and for whom incoming events are
   *  matched.  This value may be different between instances of
   *  EventHeap client objects, and may be null.  */
    public String getPerson() {
        synchronized (instanceLock) {
            return person;
        }
    }

    /** Returns the machine to which this Event Heap object is connected */
    public int getPort() {
        return port;
    }

    /** Returns the source name for this Event Heap object.  
   * 
   * @return The source name for this Event Heap object 
   */
    public String getSourceName() {
        return source;
    }

    /** Returns the version of the Event Heap. 
   * 
   * @return The version of this Event Heap.
   */
    public int getVersion() {
        return EVENT_HEAP_VERSION.intValue();
    }

    /** Processes a new event.  For use if an event was retrieved using a 
   *  non-snooping retrieval method.  Specifically, this updates the
   *  event sequencing data stored in the Map eventTypes.  The returned 
   *  event will be null if the passed in event is out of sequence, or the
   *  passed in event itself if it is in sequence.
   */
    private SecureEvent processNewEvent(SecureEvent event) throws EventHeapException {
        Map eventSources;
        String eventType;
        String sourceName;
        eventType = event.getEventType();
        synchronized (eventTypes) {
            if ((eventSources = (Map) (eventTypes.get(eventType))) == null) {
                eventSources = new HashMap();
                eventTypes.put(eventType, eventSources);
            }
        }
        sourceName = (String) (event.getPostValue(SecureEvent.SOURCE));
        SourceInfo eventSource;
        synchronized (eventSources) {
            eventSource = (SourceInfo) (eventSources.get(sourceName));
            if (eventSource == null) {
                eventSource = new SourceInfo();
                eventSource.source = sourceName;
                eventSource.sessionID = -1;
                eventSource.maxSequenceNum = -1;
                eventSources.put(sourceName, eventSource);
            }
        }
        int eventSessionID = ((Integer) (event.getPostValue(SecureEvent.SESSIONID))).intValue();
        int eventSequenceNum = ((Integer) (event.getPostValue(SecureEvent.SEQUENCENUM))).intValue();
        synchronized (eventSource) {
            int lastSessionID = eventSource.sessionID;
            int lastSequenceNum = eventSource.maxSequenceNum;
            if (eventSessionID == lastSessionID && eventSequenceNum <= lastSequenceNum) {
                event = null;
            } else {
                eventSource.sessionID = eventSessionID;
                eventSource.maxSequenceNum = eventSequenceNum;
            }
        }
        return event;
    }

    /** Sets any autoset fields in the template events.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * problem setting the fields.
   */
    private void prepareTemplateArray(SecureEvent[] templateEvents) throws EventHeapException {
        if (templateEvents == null) return;
        for (int i = 0; i < templateEvents.length; i++) {
            templateEvents[i].setPostValue(SecureEvent.SOURCE, source);
            templateEvents[i].setTemplateValueCond(SecureEvent.TARGET, source, AUTOSET_OVERRIDEABLE);
            synchronized (staticLock) {
                templateEvents[i].setTemplateValueCond(SecureEvent.TARGETAPPLICATION, application, AUTOSET_OVERRIDEABLE);
                templateEvents[i].setTemplateValueCond(SecureEvent.TARGETDEVICE, device, AUTOSET_OVERRIDEABLE);
            }
            synchronized (instanceLock) {
                if (person != null) templateEvents[i].setTemplateValueCond(SecureEvent.TARGETPERSON, person, AUTOSET_OVERRIDEABLE);
                if (group != null) templateEvents[i].setTemplateValueCond(SecureEvent.TARGETGROUP, group, AUTOSET_OVERRIDEABLE);
            }
        }
    }

    /** Puts the given event into the Event Heap.  
   * 
   * @param event The event to place in the Event Heap
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public void putEvent(SecureEvent event) throws EventHeapException {
        event.setPostValue(SecureEvent.SESSIONID, sessionID);
        event.setPostValue(SecureEvent.SOURCE, source);
        event.setPostValue(SecureEvent.EVENTHEAPVERSION, EVENT_HEAP_VERSION);
        synchronized (staticLock) {
            event.setPostValue(SecureEvent.SOURCEAPPLICATION, application);
            event.setPostValue(SecureEvent.SOURCEDEVICE, device);
        }
        synchronized (instanceLock) {
            if (group != null) event.setPostValue(SecureEvent.SOURCEGROUP, group);
        }
        synchronized (localSequenceNumMap) {
            Integer sequenceNum;
            sequenceNum = (Integer) localSequenceNumMap.get(event.getPostValue(SecureEvent.EVENTTYPE));
            if (sequenceNum == null) sequenceNum = new Integer(1);
            event.setPostValue(SecureEvent.SEQUENCENUM, sequenceNum);
            sequenceNum = new Integer(sequenceNum.intValue() + 1);
            localSequenceNumMap.put(event.getEventType(), sequenceNum);
        }
        String pFlag = event.getPostValueString(SecureEvent.PRIVATEFLAG);
        String dFlag = event.getPostValueString(SecureEvent.DELETEFLAG);
        String lFlag = event.getPostValueString(SecureEvent.LOGFLAG);
        String targetPerson = event.getPostValueString(SecureEvent.TARGETPERSON);
        String targetGroup = event.getPostValueString(SecureEvent.TARGETGROUP);
        if ((targetPerson.equals("FORMAL")) && (targetGroup.equals("FORMAL")) && (pFlag.equals(SecureEvent.FLAG_PRIVATE))) throw new EventHeapException("Private Event with no target - non-readable");
        if ((!targetPerson.equals("FORMAL")) && (!targetGroup.equals("FORMAL"))) throw new EventHeapException("Cannot set both TARGETPERSON and TARGETGROUP");
        if (!((pFlag.equals(SecureEvent.FLAG_PRIVATE)) || (pFlag.equals(SecureEvent.FLAG_PUBLIC)))) throw new EventHeapException("Invalid PRIVATEFLAG value for SecureEvent");
        if (!((dFlag.equals(SecureEvent.FLAG_DELETEABLE)) || (dFlag.equals(SecureEvent.FLAG_EXPIREONLY)))) throw new EventHeapException("Invalid DELETEFLAG value for SecureEvent");
        if (!((lFlag.equals(SecureEvent.FLAG_LOGGED)) || (lFlag.equals(SecureEvent.FLAG_NONLOGGED)))) throw new EventHeapException("Invalid LOGFLAG value for SecureEvent");
        callVoid(EH_PUT_EVENT, createSecureEventArray(event), serverTimeout);
        synchronized (staticLock) {
            event.setPostValue(SecureEvent.SOURCEAPPLICATION, AUTOSET);
            event.setPostValue(SecureEvent.SOURCEDEVICE, AUTOSET);
        }
        synchronized (instanceLock) {
            if (group != null) event.setPostValue(SecureEvent.SOURCEGROUP, AUTOSET);
        }
    }

    /** Registers to receive notification any time any event is posted
   * to the Event Heap.  Sequencing is ignored, and events retrieved
   * via the callback do not effect sequencing of other events
   * retrieved by this Event Heap object (i.e. even if an event is
   * received in the callback method, it may be received again by the
   * other retrieval methods).  Callbacks will continue to be made to
   * the specified object until {@link EventRegistration#deregister()}
   * is called, or the callback routine returns false.  
   * <p> 
   * NOTE: Since every event sent to the Event Heap will be received when
   * this method is used, it is resource intensive on both the server
   * and client sides.  It should be used sparingly and only when
   * necessary (e.g. when a complete log of transactions through the
   * Event Heap is needed).  It is preferrable to use {@link
   * #registerForEvents(Event [], EventCallback)} whenever possible to
   * receive only the subset of events needed.
   *
   * @param callback An object implementing the {@link EventCallback}
   * interface.  The returnEvent method of this object is called 
   * anytime an event is placed in the Event Heap.  Note that although
   * an array of events will be passed to the callback, since no templates
   * are used for this notification stream, the array will contain a single
   * element which is the event being returned.
   *
   * @return An {@link EventRegistration} object that is a reference
   * to this registration for notification of events.  Calling the 
   * deregister method on this object will stop event notification.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.  */
    public EventRegistration registerForAll(EventCallback callback) throws EventHeapException {
        Registration reg = new Registration(null, callback, EH_REGISTER_FOR_ALL);
        registrations.add(reg);
        return reg;
    }

    /** Registers to receive notification any time an event matching one
   * or more of the specified templates is posted to the Event Heap.
   * Sequencing is ignored, and events retrieved via the callback do
   * not effect sequencing of other events retrieved by this Event
   * Heap object (i.e. even if an event is received in the callback
   * method, it may be received again by the other retrieval methods).
   * Callbacks will continue to be made to the specified object until
   * {@link EventRegistration#deregister()} is called, or the callback
   * routine returns false.
   *
   * @param templateEvents The templates to be used in matching events
   * in the Event Heap passed in as elements of an array.  Only the
   * field type, name and value of non-formal fields are used in the
   * match.  Field order is irrelevant.
   *
   * @param callback An object implementing the {@link EventCallback}
   * interface.  The returnEvent method of this object is called 
   * anytime a matching event is placed in the Event Heap.
   *
   * @return An {@link EventRegistration} object that is a reference
   * to this registration for notification of events.  Calling the 
   * deregister method on this object will stop event notification.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.  
   */
    public EventRegistration registerForEvents(SecureEvent[] templateEvents, EventCallback callback) throws EventHeapException {
        Registration reg = new Registration(templateEvents, callback, EH_REGISTER_FOR_EVENTS);
        registrations.add(reg);
        return reg;
    }

    public EventRegistration filterEvents(SecureEvent[] templateEvents) throws EventHeapException {
        return null;
    }

    /** Removes an event matching the given template event from the Event
   * Heap represented by this object.  If no matching event is
   * available it returns immediately with 'null'.  
   * 
   * @param templateEvent The template to be used in matching events
   * in the Event Heap.  Only the field type, name and value of non-formal
   * fields are used in the match.  Field order is irrelevant.
   * 
   * @return The next most recent event in the Event Heap not yet
   * retrieved by this object which matches the given template event.
   * After retrieval the event will no longer be available in the Event
   * Heap for others to retrieve.  'null' is returned if there are no
   * matching events currently present.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent removeEvent(SecureEvent templateEvent) throws EventHeapException {
        SecureEvent[] retEvents = removeEvent(createSecureEventArray(templateEvent));
        if (retEvents != null && retEvents.length > 0) return retEvents[0]; else return null;
    }

    /** Removes an event matching one of the given template events from the
   * Event Heap represented by this object.  If no matching event is
   * available it returns immediately with 'null'.  In addition to the
   * matching event it returns which template or templates matched.
   *
   * @param templateEvents The templates to be used in matching events
   * in the Event Heap passed in as elements of an array.  Only the
   * field type, name and value of non-formal fields are used in the
   * match.  Field order is irrelevant.
   *
   * @return An array of events.  The first element of the array is
   * the next most recent event in the Event Heap not yet retrieved by
   * this object which matches one or more of the given template
   * events.  Subsequent events in the array are the template events
   * used for the match which matched the returned event.  After
   * retrieval the events will no longer be available in the Event
   * Heap for other to retrieve.  'null' is returned if there are no
   * matching events currently present.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent[] removeEvent(SecureEvent[] templateEvents) throws EventHeapException {
        prepareTemplateArray(templateEvents);
        SecureEvent retEvent = callSequencedEvent(EH_REMOVE_EVENT, templateEvents, serverTimeout);
        if (retEvent != null) return createReturnArray(processNewEvent(retEvent), templateEvents); else return null;
    }

    /** Sets the debug output level for the EventHeap for this process.
   * All debug messages with level less than or equal to the current
   * 'level' will be printed.  A level of 1 or higher will cause a
   * dump of all TSpaces calls made by the Event Heap to be printed.
   * 
   * @param level the current debug level.  Calls to {@link
   * #debugPrintln(int, String)} with level set to less than the debug
   * level will be output.  
   */
    public static void setDebug(int level) {
        debugLevel = level;
    }

    /** Sets the print stream to which to output debug information */
    public static void setDebugStream(PrintStream debugOutput) {
        debugStream = debugOutput;
    }

    /** Set the group to which all events generated by this EventHeap client
   *  object will be attributed, and for whom incoming events will be
   *  matched. */
    public void setGroupName(String name) {
        synchronized (instanceLock) {
            group = name;
        }
    }

    /** Set the person to whom all events generated by this EventHeap client
   *  object will be attributed, and for whom incoming events will be
   *  matched. (Render useless in SecureEventHeap)
   */
    public void setPerson(String name) {
        return;
    }

    /** Set the server timeout.  Calls to the Event Heap will return if they
   * don't get a response before the timeout expires.  In the case of a
   * retrieve call, null will be returned.  In the case of a put, the 
   * event is not guaranteed to have been successfully placed into the 
   * Event Heap.
   *
   * <p>NOTE: Currently the timeout is only how long the client will wait
   * for the call to succeed through the underlying socket interface.  If
   * there is no connection to the server, the client will still block
   * in calls forever trying to reconnect.  This is a known bug.
   *
   * @param timeout The amount of time to wait for a response from the
   * server in ms. 0 means wait forever.  Negative values are not allowed.
   */
    public void setServerTimeout(long timeout) {
        serverTimeout = timeout;
    }

    /** Returns all events matching the template event without modifying
   * the contents of the Event Heap.  This can be used to look at what
   * is currently in the Event Heap without actually interacting.  The
   * events are returned in an array.  No sequencing is done.
   *
   * @param templateEvent The template to be used in matching events
   * in the Event Heap.  Only the field type, name and value of non-formal
   * fields are used in the match.  Field order is irrelevant.
   *
   * @return An array of all events currently in the Event Heap that
   * match the template event.  The array is a set- there are no
   * duplicate events- but the array is not guaranteed to be ordered.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent[] snoopEvents(SecureEvent templateEvent) throws EventHeapException {
        return snoopEvents(createSecureEventArray(templateEvent));
    }

    /** Returns all events matching one or more of the given template
   * events without modifying the contents of the Event Heap.  This
   * can be used to look at what is currently in the Event Heap
   * without actually interacting.  If no matching events are
   * available it returns immediately with 'null'.  The matching
   * events are returned in an array, and the {@link
   * Tuple#matches(Tuple)} method can be used to determine which
   * template events actually match any given returned event. No
   * sequencing is done.
   *
   * @param templateEvents The templates to be used in matching events
   * in the Event Heap passed in as elements of an array.  Only the
   * field type, name and value of non-formal fields are used in the
   * match.  Field order is irrelevant.
   *
   * @return An array of all events currently in the Event Heap that
   * match one of the template events.  The array is a set- there are no
   * duplicate events- but the array is not guaranteed to be ordered.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.  */
    public SecureEvent[] snoopEvents(SecureEvent[] templateEvents) throws EventHeapException {
        prepareTemplateArray(templateEvents);
        return callEvents(EH_SNOOP_EVENTS, templateEvents, serverTimeout);
    }

    /** Removes an event matching the given template event from the Event
   * Heap represented by this object.  The call will block until a matching
   * event is found. 
   *
   * @param templateEvent The template to be used in matching events
   * in the Event Heap.  Only the field type, name and value of non-formal
   * fields are used in the match.  Field order is irrelevant.
   *
   * @return The next most recent event in the Event Heap not yet
   * retrieved by this object which matches the given template event.
   * The event will no longer be available in the Event Heap for others
   * to use.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent waitToRemoveEvent(SecureEvent templateEvent) throws EventHeapException {
        SecureEvent[] retEvents = waitToRemoveEvent(createSecureEventArray(templateEvent));
        if (retEvents != null && retEvents.length > 0) return retEvents[0]; else return null;
    }

    /** Removes an event matching one of the given template events from
   * the Event Heap represented by this object.  The call will block
   * until a matching event is found.  In addition to the matching event
   * it returns which template or templates matched.<p>
   *
   * <b>C++ API Note:</b> The C++ API includes one more parameter, 
   *       int *size, which is a pointer to the size of the templateEvents
   *       array being passed in.  When the call returns the size parameter
   *       is changed to the size of the returned event array. 
   *
   * @param templateEvents The templates to be used in matching events
   * in the Event Heap passed in as elements of an array.  Only the
   * field type, name and value of non-formal fields are used in the
   * match.  Field order is irrelevant.
   *
   * @return An array of events.  The first element of the array is
   * the next most recent event in the Event Heap not yet retrieved by
   * this object which matches one or more of the given template
   * events.  Subsequent events in the array are the template events
   * used for the match which matched the returned event.  The event
   * retrieved will no longer be available in the Event Heap for
   * others to use. 
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent[] waitToRemoveEvent(SecureEvent[] templateEvents) throws EventHeapException {
        prepareTemplateArray(templateEvents);
        SecureEvent retEvent = callSequencedEvent(EH_WAIT_TO_REMOVE_EVENT, templateEvents, serverTimeout);
        if (retEvent != null) return createReturnArray(processNewEvent(retEvent), templateEvents); else return null;
    }

    /** Retrieves an event matching the given template event from the Event
   * Heap represented by this object.  The call will block until a matching
   * event is found.
   *
   * @param templateEvent The template to be used in matching events
   * in the Event Heap.  Only the field type, name and value of non-formal
   * fields are used in the match.  Field order is irrelevant.
   *
   * @return The next most recent event in the Event Heap not yet
   * retrieved by this object which matches the given template event.
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent waitForEvent(SecureEvent templateEvent) throws EventHeapException {
        SecureEvent[] retEvents = waitForEvent(createSecureEventArray(templateEvent));
        if (retEvents != null && retEvents.length > 0) return retEvents[0]; else return null;
    }

    /** Retrieves an event matching one of the given template events from
   * the Event Heap represented by this object.  The call will block
   * until a matching event is found.  In addition to the matching event
   * it returns which template or templates matched.
   *
   * @param templateEvents The templates to be used in matching events
   * in the Event Heap passed in as elements of an array.  Only the
   * field type, name and value of non-formal fields are used in the
   * match.  Field order is irrelevant.
   *
   * @return An array of events.  The first element of the array is
   * the next most recent event in the Event Heap not yet retrieved by
   * this object which matches one or more of the given template
   * events.  Subsequent events in the array are the template events
   * used for the match which matched the returned event.  
   *
   * @throws EventHeapException An exception is thrown if there was some
   * non-recoverable problem communicating to the Event Heap server, or
   * one or more events are malformed.
   */
    public SecureEvent[] waitForEvent(SecureEvent[] templateEvents) throws EventHeapException {
        prepareTemplateArray(templateEvents);
        SecureEvent retEvent = callSequencedEvent(EH_WAIT_FOR_EVENT, templateEvents, serverTimeout);
        if (retEvent != null) return createReturnArray(processNewEvent(retEvent), templateEvents); else return null;
    }

    /** Creates a trust group on the EventHeap server.  Must specify if 
     *  trust group is open or close, public or private. Members of open 
     *  trust groups can add and remove members from the trust group. Only
     *  the group owner can add/remove members from a closed group. 
     *  Membership information in a public trust group is available to all
     *  users. Membership information in a private trust group is only 
     *  available to the PRA and to members of the group.
     *  If the group already exists, an exception will be thrown. 

     *  @param groupName A String that uniquely identifies the trust group
     *  @param bOpen A boolean that specifies if the group is open or closed.
     *  @param bPublic A boolean that specifies if the group is public or private
     *  
     *
     */
    public void createTrustGroup(String groupName, boolean bOpen, boolean bPublic) throws EventHeapException {
        String[] args = new String[3];
        args[0] = groupName;
        if (bOpen) args[1] = "OPEN"; else args[1] = "CLOSE";
        if (bPublic) args[2] = "PUBLIC"; else args[2] = "PRIVATE";
        String returnTag = callString(EHS_TG_CREATE, args, serverTimeout);
        if ((returnTag != null) && (returnTag.equals("OK"))) return;
        throw new EventHeapException(returnTag);
    }

    /** Deletes a trust group. Only the group owner is allowed to do this. 
     *  If group does not exist, or the caller is not the owner, an exception
     *  is thrown
     *  @param groupName A String that uniquely identifies the trust group
     */
    public void deleteTrustGroup(String groupName) throws EventHeapException {
        String[] args = new String[1];
        args[0] = groupName;
        String returnTag = callString(EHS_TG_DELETE, args, serverTimeout);
        if ((returnTag != null) && (returnTag.equals("OK"))) return;
        throw new EventHeapException(returnTag);
    }

    /** Changes the owner of the group. Exception thrown if caller is not 
     *  the current group owner, or if group does not exist
     *  @param groupName A string that uniquely identifies the trust group
     *  @param newOwner A string that uniquely identifies the new owner
     */
    public void changeOwner(String groupName, String newOwner) throws EventHeapException {
        String[] args = new String[2];
        args[0] = groupName;
        args[1] = newOwner;
        String returnTag = callString(EHS_TG_CHANGE_OWNER, args, serverTimeout);
        if ((returnTag != null) && (returnTag.equals("OK"))) return;
        throw new EventHeapException(returnTag);
    }

    /** Adds a user to the group. Exception thrown if permission denied. 
     *  @param groupName A string that uniquely identifies the trust group
     *  @param user A string that uniquely identifies the user to be
     *  added to the group
     */
    public void addUser(String groupName, String user, Date expiration) throws EventHeapException {
        String[] args = new String[3];
        args[0] = groupName;
        args[1] = user;
        if (expiration == null) args[2] = "0"; else args[2] = String.valueOf(expiration.getTime());
        String returnTag = callString(EHS_TG_ADD_USER, args, serverTimeout);
        if ((returnTag != null) && (returnTag.equals("OK"))) return;
        throw new EventHeapException(returnTag);
    }

    /** Remove a user from the group. Exception thrown if permission denied,
     *  or if user does not exist in group
     *  @param groupName A string that uniquely identifies the trust group
     *  @param user The user to be removed from the group
     */
    public void removeUser(String groupName, String user) throws EventHeapException {
        String[] args = new String[2];
        args[0] = groupName;
        args[1] = user;
        String returnTag = callString(EHS_TG_REMOVE_USER, args, serverTimeout);
        if ((returnTag != null) && (returnTag.equals("OK"))) return;
        throw new EventHeapException(returnTag);
    }

    /** Checks if a user is a member of a particular group. Returns
     *  true/false. If permission to check group membership denied or
     *  group does not exist, exception is thrown.
     *  @param groupName A string that uniquely identifies the trust group
     *  @param user The user whose membership in the group is to be checked.
     *  @param return True/False value indicating if the user belongs to the 
     *  specified trust group.
     */
    public boolean isMember(String groupName, String user) throws EventHeapException {
        String[] args = new String[2];
        args[0] = groupName;
        args[1] = user;
        String returnTag = callString(EHS_TG_IS_MEMBER, args, serverTimeout);
        if ((returnTag != null) && (returnTag.equals("YES"))) return true;
        if (returnTag.equals("NO")) return false;
        throw new EventHeapException(returnTag);
    }

    /**
     * gets an array of all groups 
     *
     * @return a <code>String[]</code> list of groups 
     * @exception EventHeapException if an error occurs
     */
    public String[] getGroups() throws EventHeapException {
        String returnTag = callString(EHS_TG_GET_GROUPS, null, serverTimeout);
        if (returnTag != null) {
            int N = Integer.parseInt(returnTag.substring(0, returnTag.indexOf(SecureEvent.SEPARATOR)));
            int[] index = new int[N + 1];
            String[] ret = new String[N];
            index[0] = returnTag.indexOf(SecureEvent.SEPARATOR);
            for (int i = 0; i < N; i++) {
                index[i + 1] = returnTag.indexOf(SecureEvent.SEPARATOR, index[i] + 1);
                ret[i] = returnTag.substring(index[i] + 1, index[i + 1]);
            }
            return ret;
        }
        throw new EventHeapException("Get Groups Returns NULL");
    }

    /**
     * gets an array of all users in a particular group
     *
     * @param grpName a <code>String</code> identifying the group
     * @return a <code>String[]</code> list of groups 
     * @exception EventHeapException if an error occurs
     */
    public String[] getUsers(String grpName) throws EventHeapException {
        String[] args = new String[1];
        args[0] = grpName;
        String returnTag = callString(EHS_TG_GET_USERS, args, serverTimeout);
        if (returnTag != null) {
            int N = Integer.parseInt(returnTag.substring(0, returnTag.indexOf(SecureEvent.SEPARATOR)));
            int[] index = new int[N + 1];
            String[] ret = new String[N];
            index[0] = returnTag.indexOf(SecureEvent.SEPARATOR);
            for (int i = 0; i < N; i++) {
                index[i + 1] = returnTag.indexOf(SecureEvent.SEPARATOR, index[i] + 1);
                ret[i] = returnTag.substring(index[i] + 1, index[i + 1]);
            }
            return ret;
        }
        throw new EventHeapException("Get Users Returns NULL");
    }

    /**
     * gets an array of all online users
     *
     * @return a <code>String[]</code> list of online users
     * @exception EventHeapException if an error occurs
     */
    public String[] getOnlineUsers() throws EventHeapException {
        String returnTag = callString(EHS_GET_ONLINE_USERS, null, serverTimeout);
        if (returnTag != null) {
            int N = Integer.parseInt(returnTag.substring(0, returnTag.indexOf(SecureEvent.SEPARATOR)));
            int[] index = new int[N + 1];
            String[] ret = new String[N];
            index[0] = returnTag.indexOf(SecureEvent.SEPARATOR);
            for (int i = 0; i < N; i++) {
                index[i + 1] = returnTag.indexOf(SecureEvent.SEPARATOR, index[i] + 1);
                ret[i] = returnTag.substring(index[i] + 1, index[i + 1]);
            }
            return ret;
        }
        throw new EventHeapException("GetOnlineUsers Returns NULL");
    }

    /**
     * gets an array of all users previously seen by server.
     *
     * @return a <code>String[]</code> list of online users
     * @exception EventHeapException if an error occurs
     */
    public String[] getAllUsers() throws EventHeapException {
        String returnTag = callString(EHS_GET_ALL_USERS, null, serverTimeout);
        if (returnTag != null) {
            int N = Integer.parseInt(returnTag.substring(0, returnTag.indexOf(SecureEvent.SEPARATOR)));
            int[] index = new int[N + 1];
            String[] ret = new String[N];
            index[0] = returnTag.indexOf(SecureEvent.SEPARATOR);
            for (int i = 0; i < N; i++) {
                index[i + 1] = returnTag.indexOf(SecureEvent.SEPARATOR, index[i] + 1);
                ret[i] = returnTag.substring(index[i] + 1, index[i + 1]);
            }
            return ret;
        }
        throw new EventHeapException("GetAllUsers Returns NULL");
    }

    /**
     * inform server to clear list of all previously seen users.
     *
     */
    public void clearUsers() throws EventHeapException {
        callVoid(EHS_CLEAR_USERS, null, serverTimeout);
    }

    /**
     * Automatically re-register active registrations during
     * auto-reconnect.
     *
     */
    private void reRegister() {
        if (true) return;
        synchronized (allowReRegistration) {
            if (allowReRegistration.equals(new Boolean(false))) return;
            allowReRegistration = new Boolean(false);
        }
        synchronized (registrations) {
            Iterator iter = (new HashSet(registrations)).iterator();
            while (iter.hasNext()) {
                Registration reg = (Registration) iter.next();
                if (reg.active) {
                    try {
                        if (reg.ourTemplateEvents == null) new Registration(null, reg.ourCallback, EH_REGISTER_FOR_ALL); else new Registration(reg.ourTemplateEvents, reg.ourCallback, EH_REGISTER_FOR_EVENTS);
                        System.out.println(" reRegistering callback...");
                    } catch (Exception e) {
                        System.out.println(" reRegistration failed.");
                        e.printStackTrace();
                    }
                } else {
                    registrations.remove(reg);
                }
            }
        }
    }
}
