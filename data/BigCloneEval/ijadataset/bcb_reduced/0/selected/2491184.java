package org.jvoicexml.jsapi2.jse.protocols.playback;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;

/**
 * Protocol handler for the capture protocol.
 *
 * @author Renato Cassaca
 * @author Dirk Schnelle-Walka
 * @version 1.0
 */
public class Handler extends URLStreamHandler {

    /**
     * Constructs a new object.
     */
    public Handler() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    protected URLConnection openConnection(final URL url) throws IOException {
        return new PlaybackURLConnection(url);
    }

    /**
     * {@inheritDoc}
     */
    protected int getDefaultPort() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    protected synchronized InetAddress getHostAddress(final URL url) {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}
