package com.sun.jndi.ldap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
import javax.naming.CommunicationException;
import javax.naming.ServiceUnavailableException;
import javax.naming.NamingException;
import javax.naming.InterruptedNamingException;
import javax.naming.ldap.Control;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import sun.misc.IOUtils;

/**
  * A thread that creates a connection to an LDAP server.
  * After the connection, the thread reads from the connection.
  * A caller can invoke methods on the instance to read LDAP responses
  * and to send LDAP requests.
  * <p>
  * There is a one-to-one correspondence between an LdapClient and
  * a Connection. Access to Connection and its methods is only via
  * LdapClient with two exceptions: SASL authentication and StartTLS.
  * SASL needs to access Connection's socket IO streams (in order to do encryption
  * of the security layer). StartTLS needs to do replace IO streams
  * and close the IO  streams on nonfatal close. The code for SASL
  * authentication can be treated as being the same as from LdapClient
  * because the SASL code is only ever called from LdapClient, from
  * inside LdapClient's synchronized authenticate() method. StartTLS is called
  * directly by the application but should only occur when the underlying
  * connection is quiet.
  * <p>
  * In terms of synchronization, worry about data structures
  * used by the Connection thread because that usage might contend
  * with calls by the main threads (i.e., those that call LdapClient).
  * Main threads need to worry about contention with each other.
  * Fields that Connection thread uses:
  *     inStream - synced access and update; initialized in constructor;
  *           referenced outside class unsync'ed (by LdapSasl) only
  *           when connection is quiet
  *     traceFile, traceTagIn, traceTagOut - no sync; debugging only
  *     parent - no sync; initialized in constructor; no updates
  *     pendingRequests - sync
  *     pauseLock - per-instance lock;
  *     paused - sync via pauseLock (pauseReader())
  * Members used by main threads (LdapClient):
  *     host, port - unsync; read-only access for StartTLS and debug messages
  *     setBound(), setV3() - no sync; called only by LdapClient.authenticate(),
  *             which is a sync method called only when connection is "quiet"
  *     getMsgId() - sync
  *     writeRequest(), removeRequest(),findRequest(), abandonOutstandingReqs() -
  *             access to shared pendingRequests is sync
  *     writeRequest(),  abandonRequest(), ldapUnbind() - access to outStream sync
  *     cleanup() - sync
  *     readReply() - access to sock sync
  *     unpauseReader() - (indirectly via writeRequest) sync on pauseLock
  * Members used by SASL auth (main thread):
  *     inStream, outStream - no sync; used to construct new stream; accessed
  *             only when conn is "quiet" and not shared
  *     replaceStreams() - sync method
  * Members used by StartTLS:
  *     inStream, outStream - no sync; used to record the existing streams;
  *             accessed only when conn is "quiet" and not shared
  *     replaceStreams() - sync method
  * <p>
  * Handles anonymous, simple, and SASL bind for v3; anonymous and simple
  * for v2.
  * %%% made public for access by LdapSasl %%%
  *
  * @author Vincent Ryan
  * @author Rosanna Lee
  * @author Jagane Sundar
  */
public final class Connection implements Runnable {

    private static final boolean debug = false;

    private static final int dump = 0;

    private final Thread worker;

    private boolean v3 = true;

    public final String host;

    public final int port;

    private boolean bound = false;

    private OutputStream traceFile = null;

    private String traceTagIn = null;

    private String traceTagOut = null;

    public InputStream inStream;

    public OutputStream outStream;

    public Socket sock;

    private final LdapClient parent;

    private int outMsgId = 0;

    private LdapRequest pendingRequests = null;

    volatile IOException closureReason = null;

    volatile boolean useable = true;

    private int readTimeout;

    void setV3(boolean v) {
        v3 = v;
    }

    void setBound() {
        bound = true;
    }

    Connection(LdapClient parent, String host, int port, String socketFactory, int connectTimeout, int readTimeout, OutputStream trace) throws NamingException {
        this.host = host;
        this.port = port;
        this.parent = parent;
        this.readTimeout = readTimeout;
        if (trace != null) {
            traceFile = trace;
            traceTagIn = "<- " + host + ":" + port + "\n\n";
            traceTagOut = "-> " + host + ":" + port + "\n\n";
        }
        try {
            sock = createSocket(host, port, socketFactory, connectTimeout);
            if (debug) {
                System.err.println("Connection: opening socket: " + host + "," + port);
            }
            inStream = new BufferedInputStream(sock.getInputStream());
            outStream = new BufferedOutputStream(sock.getOutputStream());
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            CommunicationException ce = new CommunicationException(host + ":" + port);
            ce.setRootCause(realException);
            throw ce;
        } catch (Exception e) {
            CommunicationException ce = new CommunicationException(host + ":" + port);
            ce.setRootCause(e);
            throw ce;
        }
        worker = Obj.helper.createThread(this);
        worker.setDaemon(true);
        worker.start();
    }

    private Object createInetSocketAddress(String host, int port) throws NoSuchMethodException {
        try {
            Class inetSocketAddressClass = Class.forName("java.net.InetSocketAddress");
            Constructor inetSocketAddressCons = inetSocketAddressClass.getConstructor(new Class[] { String.class, int.class });
            return inetSocketAddressCons.newInstance(new Object[] { host, new Integer(port) });
        } catch (ClassNotFoundException e) {
            throw new NoSuchMethodException();
        } catch (InstantiationException e) {
            throw new NoSuchMethodException();
        } catch (InvocationTargetException e) {
            throw new NoSuchMethodException();
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException();
        }
    }

    private Socket createSocket(String host, int port, String socketFactory, int connectTimeout) throws Exception {
        Socket socket = null;
        if (socketFactory != null) {
            Class socketFactoryClass = Obj.helper.loadClass(socketFactory);
            Method getDefault = socketFactoryClass.getMethod("getDefault", new Class[] {});
            Object factory = getDefault.invoke(null, new Object[] {});
            Method createSocket = null;
            if (connectTimeout > 0) {
                try {
                    createSocket = socketFactoryClass.getMethod("createSocket", new Class[] {});
                    Method connect = Socket.class.getMethod("connect", new Class[] { Class.forName("java.net.SocketAddress"), int.class });
                    Object endpoint = createInetSocketAddress(host, port);
                    socket = (Socket) createSocket.invoke(factory, new Object[] {});
                    if (debug) {
                        System.err.println("Connection: creating socket with " + "a timeout using supplied socket factory");
                    }
                    connect.invoke(socket, new Object[] { endpoint, new Integer(connectTimeout) });
                } catch (NoSuchMethodException e) {
                }
            }
            if (socket == null) {
                createSocket = socketFactoryClass.getMethod("createSocket", new Class[] { String.class, int.class });
                if (debug) {
                    System.err.println("Connection: creating socket using " + "supplied socket factory");
                }
                socket = (Socket) createSocket.invoke(factory, new Object[] { host, new Integer(port) });
            }
        } else {
            if (connectTimeout > 0) {
                try {
                    Constructor socketCons = Socket.class.getConstructor(new Class[] {});
                    Method connect = Socket.class.getMethod("connect", new Class[] { Class.forName("java.net.SocketAddress"), int.class });
                    Object endpoint = createInetSocketAddress(host, port);
                    socket = (Socket) socketCons.newInstance(new Object[] {});
                    if (debug) {
                        System.err.println("Connection: creating socket with " + "a timeout");
                    }
                    connect.invoke(socket, new Object[] { endpoint, new Integer(connectTimeout) });
                } catch (NoSuchMethodException e) {
                }
            }
            if (socket == null) {
                if (debug) {
                    System.err.println("Connection: creating socket");
                }
                socket = new Socket(host, port);
            }
        }
        return socket;
    }

    synchronized int getMsgId() {
        return ++outMsgId;
    }

    LdapRequest writeRequest(BerEncoder ber, int msgId) throws IOException {
        return writeRequest(ber, msgId, false);
    }

    LdapRequest writeRequest(BerEncoder ber, int msgId, boolean pauseAfterReceipt) throws IOException {
        LdapRequest req = new LdapRequest(msgId, pauseAfterReceipt);
        addRequest(req);
        if (traceFile != null) {
            Ber.dumpBER(traceFile, traceTagOut, ber.getBuf(), 0, ber.getDataLen());
        }
        unpauseReader();
        if (debug) {
            System.err.println("Writing request to: " + outStream);
        }
        try {
            synchronized (this) {
                outStream.write(ber.getBuf(), 0, ber.getDataLen());
                outStream.flush();
            }
        } catch (IOException e) {
            cleanup(null, true);
            throw (closureReason = e);
        }
        return req;
    }

    /**
     * Reads a reply; waits until one is ready.
     */
    BerDecoder readReply(LdapRequest ldr) throws IOException, NamingException {
        BerDecoder rber;
        boolean waited = false;
        while (((rber = ldr.getReplyBer()) == null) && !waited) {
            try {
                synchronized (this) {
                    if (sock == null) {
                        throw new ServiceUnavailableException(host + ":" + port + "; socket closed");
                    }
                }
                synchronized (ldr) {
                    rber = ldr.getReplyBer();
                    if (rber == null) {
                        if (readTimeout > 0) {
                            ldr.wait(readTimeout);
                            waited = true;
                        } else {
                            ldr.wait(15 * 1000);
                        }
                    } else {
                        break;
                    }
                }
            } catch (InterruptedException ex) {
                throw new InterruptedNamingException("Interrupted during LDAP operation");
            }
        }
        if ((rber == null) && waited) {
            removeRequest(ldr);
            throw new NamingException("LDAP response read timed out, timeout used:" + readTimeout + "ms.");
        }
        return rber;
    }

    private synchronized void addRequest(LdapRequest ldapRequest) {
        LdapRequest ldr = pendingRequests;
        if (ldr == null) {
            pendingRequests = ldapRequest;
            ldapRequest.next = null;
        } else {
            ldapRequest.next = pendingRequests;
            pendingRequests = ldapRequest;
        }
    }

    synchronized LdapRequest findRequest(int msgId) {
        LdapRequest ldr = pendingRequests;
        while (ldr != null) {
            if (ldr.msgId == msgId) {
                return ldr;
            }
            ldr = ldr.next;
        }
        return null;
    }

    synchronized void removeRequest(LdapRequest req) {
        LdapRequest ldr = pendingRequests;
        LdapRequest ldrprev = null;
        while (ldr != null) {
            if (ldr == req) {
                ldr.cancel();
                if (ldrprev != null) {
                    ldrprev.next = ldr.next;
                } else {
                    pendingRequests = ldr.next;
                }
                ldr.next = null;
            }
            ldrprev = ldr;
            ldr = ldr.next;
        }
    }

    void abandonRequest(LdapRequest ldr, Control[] reqCtls) {
        removeRequest(ldr);
        BerEncoder ber = new BerEncoder(256);
        int abandonMsgId = getMsgId();
        try {
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeInt(abandonMsgId);
            ber.encodeInt(ldr.msgId, LdapClient.LDAP_REQ_ABANDON);
            if (v3) {
                LdapClient.encodeControls(ber, reqCtls);
            }
            ber.endSeq();
            if (traceFile != null) {
                Ber.dumpBER(traceFile, traceTagOut, ber.getBuf(), 0, ber.getDataLen());
            }
            synchronized (this) {
                outStream.write(ber.getBuf(), 0, ber.getDataLen());
                outStream.flush();
            }
        } catch (IOException ex) {
        }
    }

    synchronized void abandonOutstandingReqs(Control[] reqCtls) {
        LdapRequest ldr = pendingRequests;
        while (ldr != null) {
            abandonRequest(ldr, reqCtls);
            pendingRequests = ldr = ldr.next;
        }
    }

    private void ldapUnbind(Control[] reqCtls) {
        BerEncoder ber = new BerEncoder(256);
        int unbindMsgId = getMsgId();
        try {
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeInt(unbindMsgId);
            ber.encodeByte(LdapClient.LDAP_REQ_UNBIND);
            ber.encodeByte(0);
            if (v3) {
                LdapClient.encodeControls(ber, reqCtls);
            }
            ber.endSeq();
            if (traceFile != null) {
                Ber.dumpBER(traceFile, traceTagOut, ber.getBuf(), 0, ber.getDataLen());
            }
            synchronized (this) {
                outStream.write(ber.getBuf(), 0, ber.getDataLen());
                outStream.flush();
            }
        } catch (IOException ex) {
        }
    }

    /**
     * @param reqCtls Possibly null request controls that accompanies the
     *    abandon and unbind LDAP request.
     * @param notifyParent true means to call parent LdapClient back, notifying
     *    it that the connection has been closed; false means not to notify
     *    parent. If LdapClient invokes cleanup(), notifyParent should be set to
     *    false because LdapClient already knows that it is closing
     *    the connection. If Connection invokes cleanup(), notifyParent should be
     *    set to true because LdapClient needs to know about the closure.
     */
    void cleanup(Control[] reqCtls, boolean notifyParent) {
        boolean nparent = false;
        synchronized (this) {
            useable = false;
            if (sock != null) {
                if (debug) {
                    System.err.println("Connection: closing socket: " + host + "," + port);
                }
                try {
                    if (!notifyParent) {
                        abandonOutstandingReqs(reqCtls);
                    }
                    if (bound) {
                        ldapUnbind(reqCtls);
                    }
                } finally {
                    try {
                        outStream.flush();
                        sock.close();
                        unpauseReader();
                    } catch (IOException ie) {
                        if (debug) System.err.println("Connection: problem closing socket: " + ie);
                    }
                    if (!notifyParent) {
                        LdapRequest ldr = pendingRequests;
                        while (ldr != null) {
                            ldr.cancel();
                            ldr = ldr.next;
                        }
                    }
                    sock = null;
                }
                nparent = notifyParent;
            }
        }
        if (nparent) {
            parent.processConnectionClosure();
        }
    }

    public synchronized void replaceStreams(InputStream newIn, OutputStream newOut) {
        if (debug) {
            System.err.println("Replacing " + inStream + " with: " + newIn);
            System.err.println("Replacing " + outStream + " with: " + newOut);
        }
        inStream = newIn;
        try {
            outStream.flush();
        } catch (IOException ie) {
            if (debug) System.err.println("Connection: cannot flush outstream: " + ie);
        }
        outStream = newOut;
    }

    /**
     * Used by Connection thread to read inStream into a local variable.
     * This ensures that there is no contention between the main thread
     * and the Connection thread when the main thread updates inStream.
     */
    private synchronized InputStream getInputStream() {
        return inStream;
    }

    private Object pauseLock = new Object();

    private boolean paused = false;

    private void unpauseReader() throws IOException {
        synchronized (pauseLock) {
            if (paused) {
                if (debug) {
                    System.err.println("Unpausing reader; read from: " + inStream);
                }
                paused = false;
                pauseLock.notify();
            }
        }
    }

    private void pauseReader() throws IOException {
        if (debug) {
            System.err.println("Pausing reader;  was reading from: " + inStream);
        }
        paused = true;
        try {
            while (paused) {
                pauseLock.wait();
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException("Pause/unpause reader has problems.");
        }
    }

    public void run() {
        byte inbuf[];
        int inMsgId;
        int bytesread;
        int br;
        int offset;
        int seqlen;
        int seqlenlen;
        boolean eos;
        BerDecoder retBer;
        InputStream in = null;
        try {
            while (true) {
                try {
                    inbuf = new byte[10];
                    offset = 0;
                    seqlen = 0;
                    seqlenlen = 0;
                    in = getInputStream();
                    bytesread = in.read(inbuf, offset, 1);
                    if (bytesread < 0) {
                        if (in != getInputStream()) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if (inbuf[offset++] != (Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR)) continue;
                    bytesread = in.read(inbuf, offset, 1);
                    if (bytesread < 0) break;
                    seqlen = inbuf[offset++];
                    if ((seqlen & 0x80) == 0x80) {
                        seqlenlen = seqlen & 0x7f;
                        bytesread = 0;
                        eos = false;
                        while (bytesread < seqlenlen) {
                            br = in.read(inbuf, offset + bytesread, seqlenlen - bytesread);
                            if (br < 0) {
                                eos = true;
                                break;
                            }
                            bytesread += br;
                        }
                        if (eos) break;
                        seqlen = 0;
                        for (int i = 0; i < seqlenlen; i++) {
                            seqlen = (seqlen << 8) + (inbuf[offset + i] & 0xff);
                        }
                        offset += bytesread;
                    }
                    byte[] left = IOUtils.readFully(in, seqlen, false);
                    inbuf = Arrays.copyOf(inbuf, offset + left.length);
                    System.arraycopy(left, 0, inbuf, offset, left.length);
                    offset += left.length;
                    try {
                        retBer = new BerDecoder(inbuf, 0, offset);
                        if (traceFile != null) {
                            Ber.dumpBER(traceFile, traceTagIn, inbuf, 0, offset);
                        }
                        retBer.parseSeq(null);
                        inMsgId = retBer.parseInt();
                        retBer.reset();
                        boolean needPause = false;
                        if (inMsgId == 0) {
                            parent.processUnsolicited(retBer);
                        } else {
                            LdapRequest ldr = findRequest(inMsgId);
                            if (ldr != null) {
                                synchronized (pauseLock) {
                                    needPause = ldr.addReplyBer(retBer);
                                    if (needPause) {
                                        pauseReader();
                                    }
                                }
                            } else {
                            }
                        }
                    } catch (Ber.DecodeException e) {
                    }
                } catch (IOException ie) {
                    if (debug) {
                        System.err.println("Connection: Inside Caught " + ie);
                        ie.printStackTrace();
                    }
                    if (in != getInputStream()) {
                    } else {
                        if (debug) {
                            System.err.println("Connection: rethrowing " + ie);
                        }
                        throw ie;
                    }
                }
            }
            if (debug) {
                System.err.println("Connection: end-of-stream detected: " + in);
            }
        } catch (IOException ex) {
            if (debug) {
                System.err.println("Connection: Caught " + ex);
            }
            closureReason = ex;
        } finally {
            cleanup(null, true);
        }
        if (debug) {
            System.err.println("Connection: Thread Exiting");
        }
    }
}
