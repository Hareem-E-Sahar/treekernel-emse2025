package com.moviejukebox.fanarttv.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;

/**
 * Web browser with simple cookies support
 */
public final class WebBrowser {

    private static Map<String, String> browserProperties = new HashMap<String, String>();

    private static Map<String, Map<String, String>> cookies = new HashMap<String, Map<String, String>>();

    private static String proxyHost = null;

    private static String proxyPort = null;

    private static String proxyUsername = null;

    private static String proxyPassword = null;

    private static String proxyEncodedPassword = null;

    private static int webTimeoutConnect = 25000;

    private static int webTimeoutRead = 90000;

    /**
     * Constructor for WebBrowser. Does instantiates the browser properties.
     */
    private WebBrowser() {
    }

    /**
     * Populate the browser properties if needed
     */
    private static void populateBrowserProperties() {
        if (browserProperties.isEmpty()) {
            browserProperties.put("User-Agent", "Mozilla/5.25 Netscape/5.0 (Windows; I; Win95)");
        }
    }

    /**
     * Request the web page at the specified URL
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String request(String url) throws IOException {
        return request(new URL(url));
    }

    /**
     * Open a connection using proxy parameters if they exist.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static URLConnection openProxiedConnection(URL url) throws IOException {
        if (proxyHost != null) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", proxyHost);
            System.getProperties().put("proxyPort", proxyPort);
        }
        URLConnection cnx = url.openConnection();
        if (proxyUsername != null) {
            cnx.setRequestProperty("Proxy-Authorization", proxyEncodedPassword);
        }
        return cnx;
    }

    /**
     * Request the web page at the specified URL
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String request(URL url) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader in = null;
        URLConnection cnx = null;
        try {
            cnx = openProxiedConnection(url);
            sendHeader(cnx);
            readHeader(cnx);
            in = new BufferedReader(new InputStreamReader(cnx.getInputStream(), getCharset(cnx)));
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if ((cnx != null) && (cnx instanceof HttpURLConnection)) {
                ((HttpURLConnection) cnx).disconnect();
            }
        }
        return content.toString();
    }

    /**
     * Set the header information for the connection
     *
     * @param cnx
     */
    private static void sendHeader(URLConnection cnx) {
        populateBrowserProperties();
        for (Map.Entry<String, String> browserProperty : browserProperties.entrySet()) {
            cnx.setRequestProperty(browserProperty.getKey(), browserProperty.getValue());
        }
        String cookieHeader = createCookieHeader(cnx);
        if (!cookieHeader.isEmpty()) {
            cnx.setRequestProperty("Cookie", cookieHeader);
        }
    }

    /**
     * Create the cookies for the header
     *
     * @param cnx
     * @return
     */
    private static String createCookieHeader(URLConnection cnx) {
        String host = cnx.getURL().getHost();
        StringBuilder cookiesHeader = new StringBuilder();
        for (Map.Entry<String, Map<String, String>> domainCookies : cookies.entrySet()) {
            if (host.endsWith(domainCookies.getKey())) {
                for (Map.Entry<String, String> cookie : domainCookies.getValue().entrySet()) {
                    cookiesHeader.append(cookie.getKey());
                    cookiesHeader.append("=");
                    cookiesHeader.append(cookie.getValue());
                    cookiesHeader.append(";");
                }
            }
        }
        if (cookiesHeader.length() > 0) {
            cookiesHeader.deleteCharAt(cookiesHeader.length() - 1);
        }
        return cookiesHeader.toString();
    }

    /**
     * Read the header information into the cookies
     *
     * @param cnx
     */
    private static void readHeader(URLConnection cnx) {
        for (Map.Entry<String, List<String>> header : cnx.getHeaderFields().entrySet()) {
            if ("Set-Cookie".equals(header.getKey())) {
                for (String cookieHeader : header.getValue()) {
                    String[] cookieElements = cookieHeader.split(" *; *");
                    if (cookieElements.length >= 1) {
                        String[] firstElem = cookieElements[0].split(" *= *");
                        String cookieName = firstElem[0];
                        String cookieValue = firstElem.length > 1 ? firstElem[1] : null;
                        String cookieDomain = null;
                        for (int i = 1; i < cookieElements.length; i++) {
                            String[] cookieElement = cookieElements[i].split(" *= *");
                            if ("domain".equals(cookieElement[0])) {
                                cookieDomain = cookieElement.length > 1 ? cookieElement[1] : null;
                                break;
                            }
                        }
                        if (cookieDomain == null) {
                            cookieDomain = cnx.getURL().getHost();
                        }
                        Map<String, String> domainCookies = cookies.get(cookieDomain);
                        if (domainCookies == null) {
                            domainCookies = new HashMap<String, String>();
                            cookies.put(cookieDomain, domainCookies);
                        }
                        domainCookies.put(cookieName, cookieValue);
                    }
                }
            }
        }
    }

    /**
     * Determine the charset for the connection
     *
     * @param cnx
     * @return
     */
    private static Charset getCharset(URLConnection cnx) {
        Charset charset = null;
        String contentType = cnx.getContentType();
        if (contentType != null) {
            Matcher m = Pattern.compile("harset *=[ '\"]*([^ ;'\"]+)[ ;'\"]*").matcher(contentType);
            if (m.find()) {
                String encoding = m.group(1);
                try {
                    charset = Charset.forName(encoding);
                } catch (UnsupportedCharsetException e) {
                }
            }
        }
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        return charset;
    }

    /**
     * Return the proxy host name
     *
     * @return
     */
    public static String getProxyHost() {
        return proxyHost;
    }

    /**
     * Set the proxy host name
     *
     * @param tvdbProxyHost
     */
    public static void setProxyHost(String tvdbProxyHost) {
        WebBrowser.proxyHost = tvdbProxyHost;
    }

    /**
     * Get the proxy port
     *
     * @return
     */
    public static String getProxyPort() {
        return proxyPort;
    }

    /**
     * Set the proxy port
     *
     * @param proxyPort
     */
    public static void setProxyPort(String proxyPort) {
        WebBrowser.proxyPort = proxyPort;
    }

    /**
     * Get the proxy username
     *
     * @return
     */
    public static String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * Set the proxy username
     *
     * @param proxyUsername
     */
    public static void setProxyUsername(String proxyUsername) {
        WebBrowser.proxyUsername = proxyUsername;
    }

    /**
     * Get the proxy password
     *
     * @return
     */
    public static String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Set the proxy password. Note this will automatically encode the password
     *
     * @param proxyPassword
     */
    public static void setProxyPassword(String proxyPassword) {
        WebBrowser.proxyPassword = proxyPassword;
        if (proxyUsername != null) {
            proxyEncodedPassword = proxyUsername + ":" + proxyPassword;
            proxyEncodedPassword = "Basic " + new String(Base64.encodeBase64((proxyUsername + ":" + proxyPassword).getBytes()));
        }
    }

    /**
     * Get the current web connect timeout value
     *
     * @return
     */
    public static int getWebTimeoutConnect() {
        return webTimeoutConnect;
    }

    /**
     * Get the current web read timeout value
     *
     * @return
     */
    public static int getWebTimeoutRead() {
        return webTimeoutRead;
    }

    /**
     * Set the web connect timeout value
     *
     * @param webTimeoutConnect
     */
    public static void setWebTimeoutConnect(int webTimeoutConnect) {
        WebBrowser.webTimeoutConnect = webTimeoutConnect;
    }

    /**
     * Set the web read timeout value
     *
     * @param webTimeoutRead
     */
    public static void setWebTimeoutRead(int webTimeoutRead) {
        WebBrowser.webTimeoutRead = webTimeoutRead;
    }
}
