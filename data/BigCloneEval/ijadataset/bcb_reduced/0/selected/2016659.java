package sun.rmi.transport.tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.*;
import java.rmi.server.RMISocketFactory;
import sun.rmi.runtime.Log;
import sun.rmi.transport.*;
import sun.rmi.transport.proxy.*;

public class TCPConnection implements Connection {

    private Socket socket;

    private Channel channel;

    private InputStream in = null;

    private OutputStream out = null;

    private long expiration = Long.MAX_VALUE;

    private long lastuse = Long.MIN_VALUE;

    private long roundtrip = 5;

    /**
     * Constructor used for creating a connection to accept call
     * (an input connection)
     */
    TCPConnection(TCPChannel ch, Socket s, InputStream in, OutputStream out) {
        socket = s;
        channel = ch;
        this.in = in;
        this.out = out;
    }

    /**
     * Constructor used by subclass when underlying input and output streams
     * are already available.
     */
    TCPConnection(TCPChannel ch, InputStream in, OutputStream out) {
        this(ch, null, in, out);
    }

    /**
     * Constructor used when socket is available, but not underlying
     * streams.
     */
    TCPConnection(TCPChannel ch, Socket s) {
        this(ch, s, null, null);
    }

    /**
     * Gets the output stream for this connection
     */
    public OutputStream getOutputStream() throws IOException {
        if (out == null) out = new BufferedOutputStream(socket.getOutputStream());
        return out;
    }

    /**
     * Release the output stream for this connection.
     */
    public void releaseOutputStream() throws IOException {
        if (out != null) out.flush();
    }

    /**
     * Gets the input stream for this connection.
     */
    public InputStream getInputStream() throws IOException {
        if (in == null) in = new BufferedInputStream(socket.getInputStream());
        return in;
    }

    /**
     * Release the input stream for this connection.
     */
    public void releaseInputStream() {
    }

    /**
     * Determine if this connection can be used for multiple operations.
     * If the socket implements RMISocketInfo, then we can query it about
     * this; otherwise, assume that it does provide a full-duplex
     * persistent connection like java.net.Socket.
     */
    public boolean isReusable() {
        if ((socket != null) && (socket instanceof RMISocketInfo)) return ((RMISocketInfo) socket).isReusable(); else return true;
    }

    /**
     * Set the expiration time of this connection.
     * @param time The time at which the time out expires.
     */
    void setExpiration(long time) {
        expiration = time;
    }

    /**
     * Set the timestamp at which this connection was last used successfully.
     * The connection will be pinged for liveness if reused long after
     * this time.
     * @param time The time at which the connection was last active.
     */
    void setLastUseTime(long time) {
        lastuse = time;
    }

    /**
     * Returns true if the timeout has expired on this connection;
     * otherwise returns false.
     * @param time The current time.
     */
    boolean expired(long time) {
        return expiration <= time;
    }

    /**
     * Probes the connection to see if it still alive and connected to
     * a responsive server.  If the connection has been idle for too
     * long, the server is pinged.  ``Too long'' means ``longer than the
     * last ping round-trip time''.
     * <P>
     * This method may misdiagnose a dead connection as live, but it
     * will never misdiagnose a live connection as dead.
     * @return true if the connection and server are recently alive
     */
    public boolean isDead() {
        InputStream i;
        OutputStream o;
        long start = System.currentTimeMillis();
        if ((roundtrip > 0) && (start < lastuse + roundtrip)) return (false);
        try {
            i = getInputStream();
            o = getOutputStream();
        } catch (IOException e) {
            return (true);
        }
        int response = 0;
        try {
            o.write(TransportConstants.Ping);
            o.flush();
            response = i.read();
        } catch (IOException ex) {
            TCPTransport.tcpLog.log(Log.VERBOSE, "exception: ", ex);
            TCPTransport.tcpLog.log(Log.BRIEF, "server ping failed");
            return (true);
        }
        if (response == TransportConstants.PingAck) {
            roundtrip = (System.currentTimeMillis() - start) * 2;
            return (false);
        }
        if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
            TCPTransport.tcpLog.log(Log.BRIEF, (response == -1 ? "server has been deactivated" : "server protocol error: ping response = " + response));
        }
        return (true);
    }

    /**
     * Close the connection.  */
    public void close() throws IOException {
        TCPTransport.tcpLog.log(Log.BRIEF, "close connection");
        if (socket != null) socket.close(); else {
            in.close();
            out.close();
        }
    }

    /**
     * Returns the channel for this connection.
     */
    public Channel getChannel() {
        return channel;
    }
}
