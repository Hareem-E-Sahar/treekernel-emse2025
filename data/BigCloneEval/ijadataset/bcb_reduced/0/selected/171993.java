package org.apache.http.examples.client;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

/**
 * How to send a request via proxy using {@link HttpClient HttpClient}.
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 *
 *
 * <!-- empty lines above to avoid 'svn diff' context problems -->
 * @version $Revision: 672425 $
 *
 * @since 4.0
 */
public class ClientExecuteProxy {

    /**
     * The default parameters.
     * Instantiated in {@link #setup setup}.
     */
    private static HttpParams defaultParameters = null;

    /**
     * The scheme registry.
     * Instantiated in {@link #setup setup}.
     */
    private static SchemeRegistry supportedSchemes;

    /**
     * Main entry point to this example.
     *
     * @param args      ignored
     */
    public static final void main(String[] args) throws Exception {
        final HttpHost target = new HttpHost("issues.apache.org", 443, "https");
        final HttpHost proxy = new HttpHost("127.0.0.1", 8666, "http");
        setup();
        HttpClient client = createHttpClient();
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        HttpRequest req = createRequest();
        System.out.println("executing request to " + target + " via " + proxy);
        HttpEntity entity = null;
        try {
            HttpResponse rsp = client.execute(target, req);
            entity = rsp.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(rsp.getStatusLine());
            Header[] headers = rsp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                System.out.println(headers[i]);
            }
            System.out.println("----------------------------------------");
            if (rsp.getEntity() != null) {
                System.out.println(EntityUtils.toString(rsp.getEntity()));
            }
        } finally {
            if (entity != null) entity.consumeContent();
        }
    }

    private static final HttpClient createHttpClient() {
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(getParams(), supportedSchemes);
        DefaultHttpClient dhc = new DefaultHttpClient(ccm, getParams());
        return dhc;
    }

    /**
     * Performs general setup.
     * This should be called only once.
     */
    private static final void setup() {
        supportedSchemes = new SchemeRegistry();
        SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));
        sf = SSLSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("https", sf, 80));
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        defaultParameters = params;
    }

    private static final HttpParams getParams() {
        return defaultParameters;
    }

    /**
     * Creates a request to execute in this example.
     *
     * @return  a request without an entity
     */
    private static final HttpRequest createRequest() {
        HttpRequest req = new BasicHttpRequest("GET", "/", HttpVersion.HTTP_1_1);
        return req;
    }
}
