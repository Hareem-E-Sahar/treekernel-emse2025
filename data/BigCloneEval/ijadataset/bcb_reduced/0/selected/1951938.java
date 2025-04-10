package org.apache.harmony.luni.internal.net.www.protocol.file;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.apache.harmony.luni.internal.nls.Messages;

/**
 * This is the handler that is responsible for reading files from the file
 * system.
 */
public class Handler extends URLStreamHandler {

    /**
     * Answers a connection to the a file pointed by this <code>URL</code> in
     * the file system
     * 
     * @return A connection to the resource pointed by this url.
     * @param url
     *            URL The URL to which the connection is pointing to
     * 
     */
    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    /**
     * The behaviour of this method is the same as openConnection(URL).
     * <code>proxy</code> is not used in FileURLConnection.
     * 
     * @param url
     *            the URL which the connection is pointing to
     * @param proxy
     *            Proxy
     * @return a connection to the resource pointed by this url.
     * 
     * @throws IOException
     *             if this handler fails to establish a connection.
     * @throws IllegalArgumentException
     *             if the url argument is null.
     * @throws UnsupportedOperationException
     *             if the protocol handler doesn't support this method.
     */
    @Override
    public URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (null == url) {
            throw new IllegalArgumentException(Messages.getString("luni.1B"));
        }
        String host = url.getHost();
        if (host == null || host.length() == 0 || host.equalsIgnoreCase("localhost")) {
            return new FileURLConnection(url);
        }
        URL ftpURL = new URL("ftp", host, url.getFile());
        return (proxy == null) ? ftpURL.openConnection() : ftpURL.openConnection(proxy);
    }

    /**
     * Parse the <code>string</code>str into <code>URL</code> u which
     * already have the context properties. The string generally have the
     * following format: <code><center>/c:/windows/win.ini</center></code>.
     * 
     * @param u
     *            The URL object that's parsed into
     * @param str
     *            The string equivalent of the specification URL
     * @param start
     *            The index in the spec string from which to begin parsing
     * @param end
     *            The index to stop parsing
     * 
     * @see java.net.URLStreamHandler#toExternalForm(URL)
     * @see java.net.URL
     */
    @Override
    protected void parseURL(URL u, String str, int start, int end) {
        if (end < start) {
            return;
        }
        String parseString = "";
        if (start < end) {
            parseString = str.substring(start, end).replace('\\', '/');
        }
        super.parseURL(u, parseString, 0, parseString.length());
    }
}
