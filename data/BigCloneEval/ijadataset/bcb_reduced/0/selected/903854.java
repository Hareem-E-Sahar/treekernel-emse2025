package com.tivo.kmttg.main;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**
     * CookieManager is a simple utilty for handling cookies when working
     * with <code>java.net.URL</code> and <code>java.net.URLConnection</code>
     * objects.
     * 
     * <code>
     *     Cookiemanager cm = new CookieManager();
     *     URL url = new URL("http://www.hccp.org/test/cookieTest.jsp");
     *     
     *      . . . 
     *
     *     // getting cookies:
     *     URLConnection conn = url.openConnection();
     *     conn.connect();
     *
     *     // setting cookies
     *     cm.storeCookies(conn);
     *     cm.setCookies(url.openConnection());
     * </code>
     *     @author <a href="mailto:spam@hccp.org">Ian Brown</a>
     *      
     **/
@SuppressWarnings("unchecked")
public class CookieManager {

    private Map store;

    private static final String SET_COOKIE = "Set-Cookie";

    private static final String COOKIE_VALUE_DELIMITER = ";";

    private static final String PATH = "path";

    private static final String EXPIRES = "expires";

    private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";

    private static final String SET_COOKIE_SEPARATOR = "; ";

    private static final String COOKIE = "Cookie";

    private static final char NAME_VALUE_SEPARATOR = '=';

    private static final char DOT = '.';

    private DateFormat dateFormat;

    public CookieManager() {
        store = new HashMap();
        dateFormat = new SimpleDateFormat(DATE_FORMAT);
    }

    /**
     * Retrieves and stores cookies returned by the host on the other side
     * of the the open java.net.URLConnection.
     *
     * The connection MUST have been opened using the <i>connect()</i>
     * method or a IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if <i>conn</i> is not open.
     */
    public void storeCookies(URLConnection conn) throws IOException {
        String domain = getDomainFromHost(conn.getURL().getHost());
        Map domainStore;
        if (store.containsKey(domain)) {
            domainStore = (Map) store.get(domain);
        } else {
            domainStore = new HashMap();
            store.put(domain, domainStore);
        }
        String headerName = null;
        for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equalsIgnoreCase(SET_COOKIE)) {
                Map cookie = new HashMap();
                StringTokenizer st = new StringTokenizer(conn.getHeaderField(i), COOKIE_VALUE_DELIMITER);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
                    String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
                    domainStore.put(name, cookie);
                    cookie.put(name, value);
                }
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    cookie.put(token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase(), token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length()));
                }
            }
        }
    }

    /**
     * Prior to opening a URLConnection, calling this method will set all
     * unexpired cookies that match the path or subpaths for thi underlying URL
     *
     * The connection MUST NOT have been opened 
     * method or an IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must NOT be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if <i>conn</i> has already been opened.
     */
    public void setCookies(URLConnection conn) throws IOException {
        URL url = conn.getURL();
        String domain = getDomainFromHost(url.getHost());
        String path = url.getPath();
        Map domainStore = (Map) store.get(domain);
        if (domainStore == null) return;
        StringBuffer cookieStringBuffer = new StringBuffer();
        Iterator cookieNames = domainStore.keySet().iterator();
        while (cookieNames.hasNext()) {
            String cookieName = (String) cookieNames.next();
            Map cookie = (Map) domainStore.get(cookieName);
            if (comparePaths((String) cookie.get(PATH), path) && isNotExpired((String) cookie.get(EXPIRES))) {
                cookieStringBuffer.append(cookieName);
                cookieStringBuffer.append("=");
                cookieStringBuffer.append((String) cookie.get(cookieName));
                if (cookieNames.hasNext()) cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
            }
        }
        try {
            conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
        } catch (java.lang.IllegalStateException ise) {
            IOException ioe = new IOException("Illegal State! Cookies cannot be set on a URLConnection that is already connected. Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
            throw ioe;
        }
    }

    private String getDomainFromHost(String host) {
        if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
            return host.substring(host.indexOf(DOT) + 1);
        } else {
            return host;
        }
    }

    private boolean isNotExpired(String cookieExpires) {
        if (cookieExpires == null) return true;
        Date now = new Date();
        try {
            return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
        } catch (java.text.ParseException pe) {
            pe.printStackTrace();
            return false;
        }
    }

    private boolean comparePaths(String cookiePath, String targetPath) {
        if (cookiePath == null) {
            return true;
        } else if (cookiePath.equals("/")) {
            return true;
        } else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of stored cookies organized by domain.
     */
    public String toString() {
        return store.toString();
    }

    public static void main(String[] args) {
        CookieManager cm = new CookieManager();
        try {
            URL url = new URL("http://www.hccp.org/test/cookieTest.jsp");
            URLConnection conn = url.openConnection();
            conn.connect();
            cm.storeCookies(conn);
            System.out.println(cm);
            cm.setCookies(url.openConnection());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
