package org.brickred.socialauth.util;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.exception.SocialAuthException;

/**
 * This class is used to make HTTP requests. We did try NOT writing this class
 * and instead use Apache Commons HTTP Client. However it has been reported by
 * users that Commons HTTP Client does not work on Google AppEngine. Hence we
 * have handcoded this class using HTTPURLConnection and incorporated only as
 * much functionality as is needed for OAuth clients.
 * 
 * This class may be completed rewritten in future, or may be removed if a
 * version of Commons HTTP Client compatible with AppEngine is released.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class HttpUtil {

    private static final Log LOG = LogFactory.getLog(HttpUtil.class);

    private static Proxy proxyObj = null;

    private static int timeoutValue = 0;

    static {
        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
            SSLContext.setDefault(ctx);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            LOG.warn("SSLContext is not supported by your applicaiton server." + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.warn("Error while createing SSLContext");
            e.printStackTrace();
        }
    }

    /**
	 * Makes HTTP request using java.net.HTTPURLConnection
	 * 
	 * @param urlStr
	 *            the URL String
	 * @param requestMethod
	 *            Method type
	 * @param body
	 *            Body to pass in request.
	 * @param header
	 *            Header parameters
	 * @return Response Object
	 * @throws Exception
	 */
    public static Response doHttpRequest(final String urlStr, final String requestMethod, final String body, final Map<String, String> header) throws Exception {
        HttpURLConnection conn;
        try {
            URL url = new URL(urlStr);
            if (proxyObj != null) {
                conn = (HttpURLConnection) url.openConnection(proxyObj);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            if (timeoutValue > 0) {
                LOG.debug("Setting connection timeout : " + timeoutValue);
                conn.setConnectTimeout(timeoutValue);
            }
            if (requestMethod != null) {
                conn.setRequestMethod(requestMethod);
            }
            if (header != null) {
                for (String key : header.keySet()) {
                    conn.setRequestProperty(key, header.get(key));
                }
            }
            OutputStreamWriter wr = null;
            if (body != null) {
                if (requestMethod != null && !MethodType.GET.toString().equals(requestMethod) && !MethodType.DELETE.toString().equals(requestMethod)) {
                    wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(body);
                    wr.flush();
                }
            }
            conn.connect();
        } catch (Exception e) {
            throw new SocialAuthException(e);
        }
        return new Response(conn);
    }

    /**
	 * Generates a query string from given Map while sorting the parameters in
	 * the canonical order as required by oAuth before signing
	 * 
	 * @param params
	 *            Parameters Map to generate query string
	 * @return String
	 * @throws Exception
	 */
    public static String buildParams(final Map<String, String> params) throws Exception {
        List<String> argList = new ArrayList<String>();
        for (String key : params.keySet()) {
            String val = params.get(key);
            if (val != null && val.length() > 0) {
                String arg = key + "=" + encodeURIComponent(val);
                argList.add(arg);
            }
        }
        Collections.sort(argList);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < argList.size(); i++) {
            s.append(argList.get(i));
            if (i != argList.size() - 1) {
                s.append("&");
            }
        }
        return s.toString();
    }

    private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

    public static String encodeURIComponent(final String value) throws Exception {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException wow) {
            throw new SocialAuthException(wow.getMessage(), wow);
        }
    }

    private static String getHex(final byte buf[]) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (int i = 0; i < buf.length; i++) {
            int n = buf[i] & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();
    }

    /**
	 * It decodes the given string
	 * 
	 * @param encodedURI
	 * @return
	 */
    public static String decodeURIComponent(final String encodedURI) {
        char actualChar;
        StringBuffer buffer = new StringBuffer();
        int bytePattern, sumb = 0;
        for (int i = 0, more = -1; i < encodedURI.length(); i++) {
            actualChar = encodedURI.charAt(i);
            switch(actualChar) {
                case '%':
                    {
                        actualChar = encodedURI.charAt(++i);
                        int hb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                        actualChar = encodedURI.charAt(++i);
                        int lb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                        bytePattern = (hb << 4) | lb;
                        break;
                    }
                case '+':
                    {
                        bytePattern = ' ';
                        break;
                    }
                default:
                    {
                        bytePattern = actualChar;
                    }
            }
            if ((bytePattern & 0xc0) == 0x80) {
                sumb = (sumb << 6) | (bytePattern & 0x3f);
                if (--more == 0) {
                    buffer.append((char) sumb);
                }
            } else if ((bytePattern & 0x80) == 0x00) {
                buffer.append((char) bytePattern);
            } else if ((bytePattern & 0xe0) == 0xc0) {
                sumb = bytePattern & 0x1f;
                more = 1;
            } else if ((bytePattern & 0xf0) == 0xe0) {
                sumb = bytePattern & 0x0f;
                more = 2;
            } else if ((bytePattern & 0xf8) == 0xf0) {
                sumb = bytePattern & 0x07;
                more = 3;
            } else if ((bytePattern & 0xfc) == 0xf8) {
                sumb = bytePattern & 0x03;
                more = 4;
            } else {
                sumb = bytePattern & 0x01;
                more = 5;
            }
        }
        return buffer.toString();
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /**
	 * 
	 * Sets the proxy host and port. This will be implicitly called if
	 * "proxy.host" and "proxy.port" properties are given in properties file
	 * 
	 * @param host
	 *            proxy host
	 * @param port
	 *            proxy port
	 */
    public static void setProxyConfig(final String host, final int port) {
        if (host != null) {
            int proxyPort = port;
            if (proxyPort < 0) {
                proxyPort = 0;
            }
            LOG.debug("Setting proxy - Host : " + host + "   port : " + port);
            proxyObj = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
        }
    }

    /**
	 * Sets the connection time out. This will be implicitly called if
	 * "http.connectionTimeOut" property is given in properties file
	 * 
	 * @param timeout
	 *            httpconnection timeout value
	 */
    public static void setConnectionTimeout(final int timeout) {
        timeoutValue = timeout;
    }
}
