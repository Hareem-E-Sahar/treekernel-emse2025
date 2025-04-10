package edu.jas.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 * ChannelFactory implements a symmetric and non blocking way of setting up
 * sockets on the client and server side. The constructor sets up a ServerSocket
 * and accepts and stores any Socket creation requests from clients. The created
 * Sockets can the be retrieved from the store without blocking. Refactored for
 * java.util.concurrent.
 * @author Akitoshi Yoshida
 * @author Heinz Kredel.
 * @see SocketChannel
 */
public class ChannelFactory extends Thread {

    private static final Logger logger = Logger.getLogger(ChannelFactory.class);

    /**
     * default port of socket.
     */
    public static final int DEFAULT_PORT = 4711;

    /**
     * port of socket.
     */
    private final int port;

    private final BlockingQueue<SocketChannel> buf;

    /**
     * local server socket.
     */
    private volatile ServerSocket srv;

    /**
     * is local server up and running.
     */
    private volatile boolean srvrun = false;

    /**
     * is thread started.
     */
    private volatile boolean srvstart = false;

    /**
     * Constructs a ChannelFactory on the DEFAULT_PORT.
     */
    public ChannelFactory() {
        this(DEFAULT_PORT);
    }

    /**
     * Constructs a ChannelFactory.
     * @param p port.
     */
    public ChannelFactory(int p) {
        buf = new LinkedBlockingQueue<SocketChannel>();
        if (p <= 0) {
            port = DEFAULT_PORT;
        } else {
            port = p;
        }
        try {
            srv = new ServerSocket(port);
            logger.info("server bound to port " + port);
        } catch (BindException e) {
            srv = null;
            logger.warn("server not started, port used " + port);
        } catch (IOException e) {
            logger.debug("IOException " + e);
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * toString.
     */
    @Override
    public String toString() {
        return "" + this.getClass().getSimpleName() + "(" + srv + ", buf = " + buf.size() + ")";
    }

    /**
     * thread initialization and start.
     */
    public void init() {
        if (srv != null && !srvstart) {
            this.start();
            srvstart = true;
            logger.info("ChannelFactory at " + srv);
        }
    }

    /**
     * Get a new socket channel from a server socket.
     */
    public SocketChannel getChannel() throws InterruptedException {
        if (srv == null) {
            if (srvrun) {
                throw new IllegalArgumentException("dont call when no server listens");
            }
        } else if (!srvstart) {
            init();
        }
        return buf.take();
    }

    /**
     * Get a new socket channel to a given host.
     * @param h hostname
     * @param p port
     */
    public SocketChannel getChannel(String h, int p) throws IOException {
        if (p <= 0) {
            p = port;
        }
        SocketChannel c = null;
        int i = 0;
        int delay = 5;
        logger.debug("connecting to " + h);
        while (c == null) {
            try {
                c = new SocketChannel(new Socket(h, p));
            } catch (IOException e) {
                i++;
                if (i % 50 == 0) {
                    delay += delay;
                    logger.info("Server on " + h + " not ready in " + delay + "ms");
                }
                System.out.println("Server on " + h + " not ready in " + delay + "ms");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException w) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during IO wait " + w);
                }
            }
        }
        logger.debug("connected, iter = " + i);
        return c;
    }

    /**
     * Run the servers accept() in an infinite loop.
     */
    @Override
    public void run() {
        if (srv == null) {
            return;
        }
        srvrun = true;
        while (true) {
            try {
                logger.info("waiting for connection");
                Socket s = srv.accept();
                if (this.isInterrupted()) {
                    srvrun = false;
                    return;
                }
                logger.debug("connection accepted");
                SocketChannel c = new SocketChannel(s);
                buf.put(c);
            } catch (IOException e) {
                srvrun = false;
                return;
            } catch (InterruptedException e) {
                srvrun = false;
                return;
            }
        }
    }

    /**
     * Terminate the Channel Factory
     */
    public void terminate() {
        if (!srvstart) {
            logger.debug("server not started");
            return;
        }
        this.interrupt();
        try {
            if (srv != null) {
                srv.close();
                srvrun = false;
            }
            this.interrupt();
            while (!buf.isEmpty()) {
                logger.debug("closing unused SocketChannel");
                SocketChannel c = buf.poll();
                if (c != null) {
                    c.close();
                }
            }
        } catch (IOException e) {
        }
        try {
            this.join();
        } catch (InterruptedException e) {
        }
        logger.debug("ChannelFactory terminated");
    }
}
