package org.apache.mina.example.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.proxy.AbstractProxyIoHandler;
import org.apache.mina.proxy.handlers.ProxyRequest;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.apache.mina.proxy.handlers.http.HttpProxyRequest;
import org.apache.mina.proxy.handlers.socks.SocksProxyRequest;
import org.apache.mina.proxy.session.ProxyIoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClientSessionHandler.java - Client session handler for the mina proxy test class.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * @since MINA 2.0.0-M3
 */
public class ClientSessionHandler extends AbstractProxyIoHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientSessionHandler.class);

    /**
     * The temporary file were the request result will be written.
     */
    private File file;

    /**
     * NIO channel of the temporary file.
     */
    private FileChannel wChannel;

    /**
     * The command to issue to the proxy.
     */
    private String cmd;

    public ClientSessionHandler(String cmd) {
        this.cmd = cmd;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        logger.debug("CLIENT - Session created: " + session);
    }

    /**
     * Sends the request to the proxy server when session is opened with
     * the proxy. 
     */
    @Override
    public void proxySessionOpened(IoSession session) throws Exception {
        logger.debug("CLIENT - Session opened: " + session);
        ProxyIoSession proxyIoSession = (ProxyIoSession) session.getAttribute(ProxyIoSession.PROXY_SESSION);
        if (proxyIoSession != null) {
            ProxyRequest req = proxyIoSession.getRequest();
            byte[] c = null;
            if (req instanceof SocksProxyRequest && cmd != null) {
                logger.debug("Sending request to a SOCKS SESSION ...");
                c = cmd.getBytes(proxyIoSession.getCharsetName());
            } else if (req instanceof HttpProxyRequest && ((HttpProxyRequest) req).getHttpVerb() == HttpProxyConstants.CONNECT) {
                logger.debug("Sending request to a HTTP CONNECTED SESSION ...");
                c = (((HttpProxyRequest) req).toHttpString()).getBytes(proxyIoSession.getCharsetName());
            }
            if (c != null) {
                IoBuffer buf = IoBuffer.allocate(c.length);
                buf.put(c);
                buf.flip();
                session.write(buf);
            }
        }
    }

    /**
     * Writes the request result to a temporary file.
     */
    @Override
    public void messageReceived(IoSession session, Object message) {
        logger.debug("CLIENT - Message received: " + session);
        IoBuffer buf = (IoBuffer) message;
        try {
            if (file == null) {
                file = File.createTempFile("http", ".html");
                logger.info("Writing request result to " + file.getAbsolutePath());
                wChannel = new FileOutputStream(file, false).getChannel();
            }
            wChannel.write(buf.buf());
        } catch (IOException e) {
        }
    }

    /**
     * Closes the temporary file if it was opened. 
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        logger.debug("CLIENT - Session closed - closing result file if open.");
        if (wChannel != null) {
            wChannel.close();
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        if (session.isClosing()) {
            return;
        }
        logger.debug("CLIENT - Session idle");
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        logger.debug("CLIENT - Exception caught");
        session.close(true);
    }
}
