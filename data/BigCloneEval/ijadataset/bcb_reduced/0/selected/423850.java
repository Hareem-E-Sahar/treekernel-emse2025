package org.yajul.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 * Provides a simple proxy for TCP/IP connections.
 */
public class SpyProxy extends AbstractServerSocketListener {

    private static Logger log = Logger.getLogger(SpyProxy.class);

    private static boolean argDebugBinary = false;

    private static boolean argDebugText = false;

    private static String argServerHost;

    private static int argLocalPortNumber = 0;

    private static int argServerPortNumber = 0;

    private static final int SOCKET_TIMEOUT = 0;

    /**
     * java SpyProxy [-d] serverHost serverPort [localPort]
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
            parseApplicationArguments(args, SpyProxy.class.getName());
            SpyProxy proxy = new SpyProxy(argServerHost, argServerPortNumber, argLocalPortNumber);
            proxy.setDebugBinary(argDebugBinary);
            proxy.setDebugText(argDebugText);
            Thread thread = new Thread(proxy);
            thread.start();
        } catch (Exception ex) {
            System.err.println("Unexpected exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    protected static void parseApplicationArguments(String[] args, String className) {
        int offset = 0;
        if (args.length == 0) {
            usage(className);
            System.exit(-1);
        }
        while (args[offset].charAt(0) == '-') {
            if (args[0].equals("-d")) {
                argDebugBinary = true;
                offset++;
            } else if (args[0].equals("-t")) {
                argDebugText = true;
                offset++;
            }
        }
        if (args.length - offset < 2) {
            usage(className);
            System.exit(-1);
        }
        argServerHost = args[offset++];
        String argServerPort = args[offset++];
        String argLocalPort = argServerPort;
        if (args.length > offset) argLocalPort = args[offset++];
        try {
            argLocalPortNumber = Integer.parseInt(argLocalPort);
            argServerPortNumber = Integer.parseInt(argServerPort);
        } catch (NumberFormatException x) {
            usage(className);
            System.exit(-1);
        }
    }

    /**
     * prints out message for usage
     * @see #main(String[])
     */
    protected static void usage(String className) {
        System.out.println("Usage: java " + className + " [-d] [-t] serverHost serverPort [localPort]");
        System.out.println("Where -d prints binary trace information to stdout");
        System.out.println("      -t prints text trace information to stdout (ideal for WebServices and XML over HTTPConstants)");
        System.out.println("      serverHost is the host name or IP address of the target server");
        System.out.println("      serverPort is the port on the target server");
        System.out.println("      localPort is the service port for the proxy. If local port is not specified, it would be the same as serverPort");
    }

    private boolean debugBinary;

    private boolean debugText;

    private boolean showConnections;

    /** The host to forward all requests to. **/
    private InetAddress serverAddress;

    /** The port to forward all requests to. **/
    private int serverPort;

    private DateFormat dateFormat;

    /**
     * Creates a new SpyProxy object with the given underlying server
     * and the given proxy port.
     * @param serverHost the server host
     * @param serverPort the server port
     * @param proxyPort the proxy port
     * @exception java.net.UnknownHostException
     * @exception java.io.IOException
     */
    public SpyProxy(String serverHost, int serverPort, int proxyPort) throws UnknownHostException, IOException {
        super(proxyPort);
        serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        setConnectionTimeout(SOCKET_TIMEOUT);
    }

    /**
     * turns on binary debugging messages
     * @param debugBinaryOn true to turn on.  off by default
     */
    public void setDebugBinary(boolean debugBinaryOn) {
        debugBinary = debugBinaryOn;
    }

    public boolean isShowConnections() {
        return showConnections;
    }

    public void setShowConnections(boolean showConnections) {
        this.showConnections = showConnections;
    }

    /**
     * returns the state of debugging binary messages
     * @return  true if on.  off by default
     */
    public boolean isDebugBinary() {
        return debugBinary;
    }

    /**
     * turns on text debugging messages
     * @param debugTextOn true to turn on.  off by default
     */
    public void setDebugText(boolean debugTextOn) {
        debugText = debugTextOn;
    }

    /**
     * returns the state of debugging text messages
     * @return  true if on.  off by default
     */
    public boolean isDebugText() {
        return debugText;
    }

    /**
     * returns the state of any debugging is on
     *@return true if debugText or debugBinary is true
     */
    public boolean isDebug() {
        return debugText || debugBinary;
    }

    public int getProxyPort() {
        return getPort();
    }

    protected AbstractClientConnection acceptClient(Socket in) throws IOException {
        SpyClientConnection con = null;
        try {
            con = new SpyClientConnection(in);
        } catch (ConnectException e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioex) {
                    unexpected(ioex);
                }
                unexpected(e);
            } else {
                unexpected(e);
            }
        }
        return con;
    }

    protected void unexpected(Throwable e) {
        println("Unexpected exception: " + e.getMessage());
        e.printStackTrace();
    }

    protected void serverClosed() {
        println("Shutting down...");
    }

    private void println(String message) {
        synchronized (this) {
            System.out.println(dateFormat.format(new Date()) + "~" + message);
            System.out.flush();
        }
    }

    private void print(String message) {
        synchronized (this) {
            System.out.print(message);
            System.out.flush();
        }
    }

    /**
     * Handles a connection from a client.
     *
     * This class actually connects to the server side of the proxy,
     * while the parent class AbstractClientConnection connects to the requestor.
     */
    private class SpyClientConnection extends AbstractClientConnection {

        private Socket server;

        private Channel incoming;

        private Channel outgoing;

        private boolean incomingStopped = false;

        private boolean outgoingStopped = false;

        private Channel currentChannel = null;

        /**
         * Creates a SpyClientConnection.
         * @param in the client socket
         * @throws IOException if failed.
         */
        private SpyClientConnection(Socket in) throws IOException {
            super(SpyProxy.this, in);
            server = new Socket(serverAddress, serverPort);
            if (isDebug() || showConnections) {
                println("Client " + in.getInetAddress().getHostName() + ":" + in.getPort() + " accepted, " + " proxy socket to " + server.getInetAddress().getHostName() + ":" + server.getPort() + " opened");
            }
            Socket destination = getSocket();
            incoming = new Channel(destination, getInputStream(), server, server.getOutputStream(), this, isPaused());
            outgoing = new Channel(server, server.getInputStream(), destination, destination.getOutputStream(), this, isPaused());
        }

        public void pause() {
            incoming.setPaused(true);
            outgoing.setPaused(true);
        }

        public void resume() {
            incoming.setPaused(false);
            outgoing.setPaused(false);
        }

        public void initialize(AbstractServerSocketListener listener) {
            super.initialize(listener);
        }

        public void start() {
            Thread thread = new Thread(incoming);
            thread.start();
            Thread thread2 = new Thread(outgoing);
            thread2.start();
        }

        public void shutdown() {
            if (incoming.isAlive() || outgoing.isAlive()) {
                incoming.shutdown();
                outgoing.shutdown();
                super.close();
                try {
                    server.close();
                } catch (Exception ex) {
                    unexpected(ex);
                }
            }
            server = null;
        }

        public void channelClosed(Channel channel) {
            if (channel == incoming) {
                if (isShowConnections() || isDebug()) println("Incoming stream " + channel.getName() + " closed, " + channel.getBytes() + " bytes.");
                incomingStopped = true;
            } else if (channel == outgoing) {
                if (isShowConnections() || isDebug()) println("Outgoing stream " + channel.getName() + " closed, " + channel.getBytes() + " bytes.");
                outgoingStopped = true;
            } else println("Unknown channel " + channel.toString());
            if (incomingStopped && outgoingStopped) {
                close();
            }
        }

        public void setCurrentChannel(Channel channel) {
            synchronized (this) {
                boolean channelChanged = (currentChannel != channel);
                currentChannel = channel;
                if (channelChanged && (isShowConnections() || isDebug())) {
                    if (currentChannel == incoming) {
                        if (isDebug()) print("\n");
                        println(" CLIENT " + incoming.getName() + "  => SERVER " + outgoing.getName());
                    } else {
                        if (isDebug()) print("\n");
                        println(" SERVER " + outgoing.getName() + "  => CLIENT " + incoming.getName());
                    }
                }
            }
        }
    }

    private class Channel implements Runnable {

        private static final int BUFSZ = 4 * 1024;

        private Thread thread;

        private boolean running = true;

        private Socket in;

        private Socket out;

        private InputStream reader;

        private OutputStream writer;

        private InetAddress inAddress;

        private int inPort;

        private SpyClientConnection con;

        private long bytes;

        private boolean paused;

        /**
         * Constructor
         * @param in the transmitting socket
         * @param out the receiving socket
         */
        private Channel(Socket in, InputStream inputStream, Socket out, OutputStream outputStream, SpyClientConnection con, boolean paused) {
            super();
            this.in = in;
            this.out = out;
            this.con = con;
            reader = inputStream;
            writer = outputStream;
            inAddress = this.in.getInetAddress();
            inPort = this.in.getPort();
            bytes = 0;
            this.paused = paused;
        }

        private void shutdown() {
            running = false;
        }

        public long getBytes() {
            return bytes;
        }

        public InetAddress getAddress() {
            return inAddress;
        }

        public int getPort() {
            return inPort;
        }

        public String getName() {
            return inAddress.getHostName() + ":" + inPort;
        }

        public void run() {
            byte[] cbuf = new byte[BUFSZ];
            thread = Thread.currentThread();
            int readLength = 0;
            try {
                while (running && (readLength = reader.read(cbuf, 0, cbuf.length)) != -1) {
                    while (isPaused()) {
                        log.info("Channel is paused. \"wait\"ing...");
                        synchronized (this) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    bytes += readLength;
                    con.setCurrentChannel(this);
                    if (debugBinary) printBinaryBuf(cbuf, readLength);
                    if (debugText) printTextBuf(cbuf, readLength);
                    writer.write(cbuf, 0, readLength);
                    writer.flush();
                }
            } catch (IOException e) {
                final String msg = e.getMessage();
                if (msg != null && !msg.startsWith("Socket closed")) log.error("Exception running proxy: " + msg);
                running = false;
            }
            try {
                out.close();
            } catch (IOException ioex) {
                println("Channel.run() could not close socket");
            }
            con.channelClosed(this);
        }

        /**
         * method to print what's in the buffer?
         * @param cbuf array of type byte
         * @param validLength int showing what the valid length is
         */
        private void printBinaryBuf(byte[] cbuf, int validLength) {
            StringBuffer msg = new StringBuffer();
            int offset = 0;
            while (offset < validLength) {
                int subOffset = 0;
                StringBuffer byteBuf = new StringBuffer();
                StringBuffer printBuf2 = new StringBuffer("                ");
                while (subOffset < 16) {
                    if (offset + subOffset < validLength) {
                        int curr = (int) cbuf[offset + subOffset] & 0xff;
                        byteBuf.append(hexString(curr, 2) + " ");
                        if (32 <= curr && curr < 127) printBuf2.setCharAt(subOffset, (char) curr); else printBuf2.setCharAt(subOffset, '.');
                    } else byteBuf.append("   ");
                    subOffset++;
                }
                msg.append("  " + hexString(offset, 6) + ": ");
                msg.append(byteBuf.toString());
                msg.append("[" + printBuf2 + "]");
                msg.append("\n");
                offset += subOffset;
            }
            print(msg.toString());
        }

        /**
         * HexString method; takes an int and returns a HexString.
         * @param value the input value as an integer
         * @param minLength the minimum length
         * @return returns a HexString
         */
        private String hexString(int value, int minLength) {
            String hs = Integer.toHexString(value);
            int hsLength = hs.length();
            if (hsLength < minLength) {
                StringBuffer leadingZeros = new StringBuffer();
                int neededLength = minLength - hsLength;
                while (neededLength-- > 0) leadingZeros.append("0");
                leadingZeros.append(hs);
                hs = leadingZeros.toString();
            }
            return hs;
        }

        /**
         * method to print what's in the buffer?
         * @param cbuf array of type byte
         * @param validLength int showing what the valid length is
         */
        private void printTextBuf(byte[] cbuf, int validLength) {
            String s = null;
            try {
                s = new String(cbuf, 0, validLength, "UTF-8");
            } catch (UnsupportedEncodingException x) {
                unexpected(x);
            }
            print(s);
        }

        public boolean isAlive() {
            return (thread != null) ? thread.isAlive() : false;
        }

        public final boolean isPaused() {
            return paused;
        }

        public final synchronized void setPaused(boolean paused) {
            this.paused = paused;
            notifyAll();
        }
    }

    public static boolean isArgDebugBinary() {
        return argDebugBinary;
    }

    public static boolean isArgDebugText() {
        return argDebugText;
    }

    public static int getArgLocalPortNumber() {
        return argLocalPortNumber;
    }

    public static String getArgServerHost() {
        return argServerHost;
    }

    public static int getArgServerPortNumber() {
        return argServerPortNumber;
    }
}
