package org.ofbiz.webapp.xmlrpc;

import org.apache.xmlrpc.client.*;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.util.HttpUtil;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.ofbiz.base.util.SSLUtil;
import org.ofbiz.base.util.GeneralException;
import org.xml.sax.SAXException;
import javax.net.ssl.HttpsURLConnection;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;

/**
 * AliasSupportedTransportFactory
 */
public class AliasSupportedTransportFactory extends XmlRpcTransportFactoryImpl {

    private final AliasSupportedTransport transport;

    public AliasSupportedTransportFactory(org.apache.xmlrpc.client.XmlRpcClient client, KeyStore ks, String password, String alias) {
        super(client);
        transport = new AliasSupportedTransport(client, ks, password, alias);
    }

    public XmlRpcTransport getTransport() {
        return transport;
    }

    class AliasSupportedTransport extends XmlRpcHttpTransport {

        protected static final String userAgent = USER_AGENT + " (Sun HTTP Transport)";

        private URLConnection con;

        private String password;

        private String alias;

        private KeyStore ks;

        protected AliasSupportedTransport(org.apache.xmlrpc.client.XmlRpcClient client, KeyStore ks, String password, String alias) {
            super(client, userAgent);
            this.password = password;
            this.alias = alias;
            this.ks = ks;
        }

        public Object sendRequest(XmlRpcRequest req) throws XmlRpcException {
            XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) req.getConfig();
            URL serverUrl = config.getServerURL();
            if (serverUrl == null) {
                throw new XmlRpcException("Invalid server URL");
            }
            try {
                con = openConnection(serverUrl);
                con.setUseCaches(false);
                con.setDoInput(true);
                con.setDoOutput(true);
            } catch (IOException e) {
                throw new XmlRpcException("Failed to create URLConnection: " + e.getMessage(), e);
            }
            return super.sendRequest(req);
        }

        protected URLConnection openConnection(URL url) throws IOException {
            URLConnection con = url.openConnection();
            if ("HTTPS".equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection scon = (HttpsURLConnection) con;
                try {
                    scon.setSSLSocketFactory(SSLUtil.getSSLSocketFactory(ks, password, alias));
                    scon.setHostnameVerifier(SSLUtil.getHostnameVerifier(SSLUtil.HOSTCERT_MIN_CHECK));
                } catch (GeneralException e) {
                    throw new IOException(e.getMessage());
                } catch (GeneralSecurityException e) {
                    throw new IOException(e.getMessage());
                }
            }
            return con;
        }

        protected void setRequestHeader(String header, String value) {
            con.setRequestProperty(header, value);
        }

        protected void close() throws XmlRpcClientException {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
        }

        protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig config) {
            return HttpUtil.isUsingGzipEncoding(con.getHeaderField("Content-Encoding"));
        }

        protected InputStream getInputStream() throws XmlRpcException {
            try {
                return con.getInputStream();
            } catch (IOException e) {
                throw new XmlRpcException("Failed to create input stream: " + e.getMessage(), e);
            }
        }

        protected void writeRequest(ReqWriter pWriter) throws IOException, XmlRpcException, SAXException {
            pWriter.write(con.getOutputStream());
        }
    }
}
