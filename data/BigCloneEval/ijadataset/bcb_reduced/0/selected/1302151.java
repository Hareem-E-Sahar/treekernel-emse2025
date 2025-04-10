package org.jlibrtp.protocols.rtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * <p>Title: Handler for rtp:// protocol</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007-2008</p>
 *
 * <p>Company: VoiceInteraction</p>
 *
 * @author Renato Cassaca
 * @version 1.0
 */
public class Handler extends URLStreamHandler {

    /** Logger instance. */
    private static final Logger LOGGER = Logger.getLogger(Handler.class.getName());

    public Handler() {
        super();
    }

    protected URLConnection openConnection(URL url) throws IOException {
        try {
            return new RTPURLConnection(url);
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid provided URL");
        }
    }

    /**
     * Returns the default port for a URL parsed by this handler.
     *
     * @return the default port for a <code>URL</code> parsed by this handler.
     */
    protected int getDefaultPort() {
        return 0;
    }

    /**
     * Get the IP address of our host.
     *
     * @param u a URL object
     * @return an <code>InetAddress</code> representing the host IP address.
     */
    protected synchronized InetAddress getHostAddress(URL u) {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            LOGGER.warning(ex.getMessage());
            return null;
        }
    }
}
