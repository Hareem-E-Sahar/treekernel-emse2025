package com.sun.midp.io.j2me.cbs;

import com.sun.midp.io.j2me.ProtocolBase;
import com.sun.midp.io.j2me.sms.TextEncoder;
import com.sun.midp.security.Permissions;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * CBS message connection implementation.
 *
 * Cell Broadcast message connections are receive only.
 *
 */
public class Protocol extends ProtocolBase {

    /** Currently opened connections. */
    protected static Vector openConnections = new Vector();

    /** Local handle for port number. */
    private int m_imsgid = 0;

    /** Name of current connection. */
    protected String url;

    /**  DCS: GSM Alphabet  */
    protected static final int GSM_TEXT = 0;

    /**  DCS: Binary */
    protected static final int GSM_BINARY = 1;

    /**  DCS: Unicode UCS-2 */
    protected static final int GSM_UCS2 = 2;

    /** Creates a message connection protocol handler. */
    public Protocol() {
        super();
        ADDRESS_PREFIX = "cbs://";
    }

    /**
     * Gets the connection parameter in string mode.
     * @return string that contains a parameter 
     */
    protected String getAppID() {
        if (m_imsgid > 0) {
            return new String(Integer.toString(m_imsgid));
        } else {
            return null;
        }
    }

    /**
     * Sets the connection parameter in string mode.
     * @param newValue new value of connection parameter 
     */
    protected void setAppID(String newValue) {
        try {
            m_imsgid = Integer.parseInt(newValue);
        } catch (NumberFormatException exc) {
            m_imsgid = 0;
        }
    }

    /**
     * The internal representation of the CBS data structure. This is also
     * intended to represent the serialized format of the data fields in a
     * CBS data packet.
     */
    private class CBSPacket {

        /** Type of message */
        public int encodingType;

        /** Message ID (Port/Channel number). */
        public int msgID;

        /** Message buffer */
        public byte[] message;
    }

    ;

    /**
     * Native function to open a CBS connection.
     *
     * @param msgID    The message ID to be matched against incoming messages.
     * @param msid     Midlet Suite ID.
     *
     * @return    returns handle to the open CBS connection.
     */
    private native int open0(int msgID, int msid) throws IOException;

    /**
     * Unblock the receive thread.
     *
     * @param msid The MIDlet suite ID.
     *
     * @return  returns handle to the connection.
     */
    protected int unblock00(int msid) throws IOException {
        return open0(0, msid);
    }

    /**
     * Native function to close cbs connection
     *
     * @param port    The port number to close.
     * @param handle  The CBS handle returned by open0.
     * @param deRegister Deregistration appID when parameter is 1.
     *
     * @return        0 on success, -1 on failure.
     */
    private native int close0(int port, int handle, int deRegister);

    /**
     * Close connection.
     *
     * @param connHandle handle returned by open0
     * @param deRegister Deregistration appID when parameter is 1.
     * @return    0 on success, -1 on failure
     */
    protected int close00(int connHandle, int deRegister) {
        return close0(m_imsgid, connHandle, deRegister);
    }

    /**
     * Receives a CBS message.
     *
     * @param port    The port used for incoming messages.
     * @param msid     Midlet Suite ID.
     * @param handle    The handle used to open the CBS connection.
     * @param cbsPacket    The received packet.
     *
     * @return    The number of bytes received.
     *
     * @exception IOException  if an I/O error occurs
     */
    private static native synchronized int receive0(int port, int msid, int handle, CBSPacket cbsPacket) throws IOException;

    /**
     * Waits until message available
     *
     * @param port    The port used for incoming messages.
     * @param handle    The handle to the CBS connection.
     *
     * @return    <code>0</code> on success, <code>-1</code> on failure
     *
     * @exception IOException  if an I/O error occurs
     */
    private native int waitUntilMessageAvailable0(int port, int handle) throws IOException;

    /**
     * Waits until message available
     *
     * @param handle handle to connection
     * @return 0 on success, -1 on failure
     * @exception IOException  if an I/O error occurs
     */
    protected int waitUntilMessageAvailable00(int handle) throws IOException {
        return waitUntilMessageAvailable0(m_imsgid, handle);
    }

    /**
     * Checks the internal setting of the receive permission. Called from
     * the <code>receive</code> and <code>setMessageListener</code> methods.
     *
     * @exception InterruptedIOException if permission dialog was pre-empted.
     */
    protected void checkReceivePermission() throws InterruptedIOException {
        if (readPermission == false) {
            try {
                midletSuite.checkForPermission(Permissions.CBS_RECEIVE, "cbs:receive");
                readPermission = true;
            } catch (InterruptedException ie) {
                throw new InterruptedIOException("Interrupted while trying " + "to ask the user permission.");
            }
        }
    }

    /**
     * Constructs a new message object of a text or binary type. When the
     * <code>TEXT_MESSAGE</code> constant is passed in, the created
     * object implements the <code>TextMessage</code> interface.
     * When the <code>BINARY_MESSAGE</code> constant is passed in, the
     * created object implements the <code>BinaryMessage</code>
     * interface.
     * <p>
     * If this method is called in a sending mode, a new <code>Message</code>
     * object is requested from the connection. For example:
     * <p>
     * <code>Message msg = conn.newMessage(TEXT_MESSAGE);</code>
     * <p>
     * The newly created <code>Message</code> does not have the destination
     * address set. It must be set by the application before the message is
     * sent.
     * <p>
     * If this method is called in receiving mode, the
     * <code>Message</code> object does have
     * its address set. The application can act on the object to extract
     * the address and message data.
     * <p>
     * <!-- The <code>type</code> parameter indicates the number of bytes
     * that should be
     * allocated for the message. No restrictions are placed on the application
     * for the value of <code>size</code>.
     * A value of <code>null</code> is permitted and creates a
     * <code>Message</code> object
     * with a 0-length message. -->
     *
     * @param type <code>TEXT_MESSAGE</code> or
     *     <code>BINARY_MESSAGE</code>.
     *
     * @return A new CBS <code>Message</code> object.
     */
    public Message newMessage(String type) {
        String address = ADDRESS_PREFIX;
        if (m_imsgid != 0) {
            address = address + ":" + String.valueOf(m_imsgid);
        }
        return newMessage(type, address);
    }

    /**
     * Constructs a new message object of a text or binary type and specifies
     * a destination address.
     * When the
     * <code>TEXT_MESSAGE</code> constant is passed in, the created
     * object implements the <code>TextMessage</code> interface.
     * When the <code>BINARY_MESSAGE</code> constant is passed in, the
     * created object implements the <code>BinaryMessage</code>
     * interface.
     * <p>
     * The destination address <code>addr</code> has the following format:
     * </p>
     * <p>
     * <code>cbs://<em>phone_number</em>:<em>port</em></code>
     * </p>
     *
     * @param type <code>TEXT_MESSAGE</code> or
     *     <code>BINARY_MESSAGE</code>.
     * @param addr The destination address of the message.
     *
     * @return  A new CBS <code>Message</code> object.
     */
    public Message newMessage(String type, String addr) {
        Message msg = null;
        if (type.equals(MessageConnection.TEXT_MESSAGE)) {
            msg = new TextObject(addr);
        } else if (type.equals(MessageConnection.BINARY_MESSAGE)) {
            msg = new BinaryObject(addr);
        } else {
            throw new IllegalArgumentException("Message type not supported.");
        }
        return msg;
    }

    /**
     * Cell broadcast connections are read-only connections. Calling this
     * method causes an <code>IOException</code> to be thrown.
     *
     * @param msg    Placeholder: A <code>Message</code> object.
     *
     * @exception    IOException always thrown.
     */
    public void send(Message msg) throws IOException {
        throw new IOException("Send not supported.");
    }

    /**
     * Receives the bytes that have been sent over the connection,
     * constructs a <code>Message</code> object, and returns it.
     * <p>
     * If there are no <code>Message</code>s waiting on the connection,
     * this method will block until a message
     * is received, or the <code>MessageConnection</code> is closed.
     *
     * @return a <code>Message</code> object
     * @exception java.io.IOException if an error occurs while receiving
     *         a message.
     * @exception java.io.InterruptedIOException if this
     *         <code>MessageConnection</code> object is closed during this
     *         receive call.
     * @exception java.lang.SecurityException if the application does not have
     *         permission to receive messages using the given port number.
     */
    public synchronized Message receive() throws IOException {
        checkReceivePermission();
        ensureOpen();
        if (m_mode == Connector.WRITE) {
            throw new IOException("Invalid connection mode.");
        }
        Message msg = null;
        int length = 0;
        try {
            CBSPacket cbsPacket = new CBSPacket();
            length = receive0(m_imsgid, midletSuite.getID(), connHandle, cbsPacket);
            if (length < 0) {
                throw new InterruptedIOException("Connection closed.");
            }
            String type = MessageConnection.TEXT_MESSAGE;
            boolean isTextMessage = true;
            if (cbsPacket.encodingType == GSM_BINARY) {
                type = MessageConnection.BINARY_MESSAGE;
                isTextMessage = false;
            }
            msg = newMessage(type, new String(ADDRESS_PREFIX + ":" + cbsPacket.msgID));
            if (isTextMessage) {
                String text = null;
                if (cbsPacket.message != null) {
                    if (cbsPacket.encodingType == GSM_TEXT) {
                        text = new String(TextEncoder.toString(TextEncoder.decode(cbsPacket.message)));
                    } else {
                        text = new String(TextEncoder.toString(cbsPacket.message));
                    }
                } else {
                    text = new String("");
                }
                ((TextObject) msg).setPayloadText(text);
            } else {
                if (cbsPacket.message != null) {
                    ((BinaryObject) msg).setPayloadData(cbsPacket.message);
                } else {
                    ((BinaryObject) msg).setPayloadData(new byte[0]);
                }
            }
        } catch (InterruptedIOException ex) {
            throw new InterruptedIOException("MessageConnection closed.");
        } catch (IOException ex) {
            io2InterruptedIOExc(ex, "receiving");
        }
        return msg;
    }

    /**
     * Returns the number of segments required to send the given
     * <code>Message</code>.
     *
     * <p>Note: The message is not actually sent. The number of protocol
     * segments is simply computed.
     * </p>
     * <p>This method will compute the number of segments needed when this
     * message is split into the protocol segments using the appropriate
     * features of the underlying protocol. This method does not take into
     * account possible limitations of the implementation that may limit the
     * number of segments that can be sent using this feature. These limitations
     * are protocol specific and are documented with the adapter definition for
     * that protocol.
     * </p>
     * @param message The message to be used for the computation.
     *
     * @return The number of protocol segments needed for sending the message.
     *     Returns <code>0</code> if the <code>Message</code> object cannot be
     *     sent using the underlying protocol.
     */
    public int numberOfSegments(Message message) {
        return (message != null) ? 1 : 0;
    }

    /**
     * Closes the connection. Resets the connection <code>open</code> flag to
     * <code>false</code>. Subsequent operations on a closed connection should
     * throw an appropriate exception.
     *
     * @exception IOException  if an I/O error occurs
     */
    public void close() throws IOException {
        int save_imsgid = m_imsgid;
        m_imsgid = 0;
        close0(save_imsgid, connHandle, 1);
        setMessageListener(null);
        connHandle = 0;
        open = false;
        m_mode = 0;
        for (int i = 0, n = openConnections.size(); i < n; i++) {
            if (openConnections.elementAt(i) == this) {
                openConnections.removeElementAt(i);
                break;
            }
        }
    }

    /**
     * Opens a CBS connection. This method is called from the
     * <code>Connector.open()</code> method to obtain the destination
     * address given in the <code>name</code> parameter.
     * <p>
     * The format for the <code>name</code> string for this method is:
     * </p>
     * <p>
     * <code>cbs://<em>[phone_number</em>:<em>][port_number]</em></code>
     * </p>
     * <p>
     * where the <em>phone_number:</em> is optional. If the
     * <em>phone_number</em> parameter is present, the connection is being
     * opened in client mode. This means that messages can be sent. If the
     * parameter is absent, the connection is being opened in server mode.
     * This means that messages can be sent and received.
     * <p>
     * The connection that is opened is to a low-level transport mechanism
     * which can be any of the following:
     * <ul>
     * <li>a datagram Short Message Peer-to-Peer (SMPP)
     * to a service center </li>
     * <li>a <code>comm</code> connection to a phone device with
     *     AT-commands</li>.
     * <li>a native CBS stack</li>
     *  </ul>
     * Currently, the <code>mode</code> and <code>timeouts</code> parameters are
     * ignored.
     *
     * @param name the target of the connection
     * @param mode indicates whether the caller
     *             intends to write to the connection. Currently,
     *             this parameter is ignored.
     * @param timeouts indicates whether the caller
     *                 wants timeout exceptions. Currently,
     *             this parameter is ignored.
     * @return this connection
     * @exception IOException if the connection is closed or unavailable
     */
    public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
        return openPrimInternal(name, mode, timeouts);
    }

    /**
     * Open and return an input stream for a connection.
     * This method always throw
     * <code>IllegalArgumentException</code>.
     *
     * @return                              An input stream.
     * @exception IOException               if an I/O error occurs.
     * @exception IllegalArgumentException  is thrown for all requests.
     */
    public InputStream openInputStream() throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    /**
     * Open and return a data input stream for a connection.
     * This method always throw
     * <code>IllegalArgumentException</code>.
     *a
     * @return                              An input stream.
     * @exception IOException               if an I/O error occurs.
     * @exception IllegalArgumentException  is thrown for all requests.
     */
    public DataInputStream openDataInputStream() throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    /**
     * Open and return an output stream for a connection.
     * This method always throw
     * <code>IllegalArgumentException</code>.
     *
     * @return                              An output stream.
     * @exception IOException               if an I/O error occurs.
     * @exception IllegalArgumentException  is thrown for all requests.
     */
    public OutputStream openOutputStream() throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    /**
     * Open and return a data output stream for a connection.
     * This method always throw
     * <code>IllegalArgumentException</code>.
     *
     * @return                              An output stream.
     * @exception IOException               if an I/O error occurs.
     * @exception IllegalArgumentException  is thrown for all requests.
     */
    public DataOutputStream openDataOutputStream() throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    /**
     * Opens a CBS connection. This is the internal entry point that
     * allows the CBS protocol handler to use the reserved port for
     * CBS emulated messages.
     *
     * @param name        The name of the connection, which as a format of
     *                    "//:portNumber" for CBS connections.
     * @param mode        Must be READ, only.
     * @param timeouts    Indicates whether the caller wants time-out
     *                    exceptions. Currently ignored.
     * @return the successfully opened connection.
     * @exception IOException if the connection is closed or unavailable.
     */
    public synchronized Connection openPrimInternal(String name, int mode, boolean timeouts) throws IOException {
        if (mode == Connector.WRITE) {
            throw new IllegalArgumentException("WRITE not supported.");
        }
        if ((mode != Connector.READ) && (mode != Connector.READ_WRITE)) {
            throw new IllegalArgumentException("Invalid I/O constraint.");
        }
        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException("Missing protocol separator.");
        }
        int colon = name.indexOf(':');
        if (colon != 2) {
            throw new IllegalArgumentException("Host not supported.");
        }
        String msgIDText = name.substring(colon + 1);
        int msgID = 0;
        try {
            msgID = Integer.parseInt(msgIDText);
            if ((msgID > 65535) || (msgID < 0)) {
                throw new IllegalArgumentException("Message ID out of range.");
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Message ID formatted badly.");
        }
        if (openPermission == false) {
            try {
                midletSuite.checkForPermission(Permissions.CBS_SERVER, "cbs:open");
                openPermission = true;
            } catch (InterruptedException ie) {
                throw new InterruptedIOException("Interrupted while trying " + "to ask the user permission.");
            }
        }
        for (int i = 0, n = openConnections.size(); i < n; i++) {
            if (((Protocol) openConnections.elementAt(i)).url.equals(name)) {
                throw new IOException("Connection already open.");
            }
        }
        url = name;
        m_imsgid = 0;
        if (msgIDText != null) {
            m_imsgid = msgID;
        }
        try {
            connHandle = open0(m_imsgid, midletSuite.getID());
        } catch (IOException ioexcep) {
            m_mode = 0;
            throw new IOException("Unable to open CBS connection.");
        } catch (OutOfMemoryError oomexcep) {
            m_mode = 0;
            throw new IOException("Unable to open CBS connection.");
        }
        openConnections.addElement(this);
        m_mode = mode;
        open = true;
        return this;
    }
}
