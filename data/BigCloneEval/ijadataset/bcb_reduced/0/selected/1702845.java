package net.oauth.client.httpclient4;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import net.oauth.client.ExcerptInputStream;
import net.oauth.http.HttpMessage;
import net.oauth.http.HttpResponseMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

/**
 * Utility methods for an OAuth client based on the <a
 * href="http://hc.apache.org">Apache HttpClient</a>.
 * 
 * @author Sean Sullivan
 */
public class HttpClient4 implements net.oauth.http.HttpClient {

    public HttpClient4() {
        this(SHARED_CLIENT);
    }

    public HttpClient4(HttpClientPool clientPool) {
        this.clientPool = clientPool;
    }

    private final HttpClientPool clientPool;

    public HttpResponseMessage execute(HttpMessage request, Map<String, Object> parameters) throws IOException {
        final String method = request.method;
        final String url = request.url.toExternalForm();
        final InputStream body = request.getBody();
        final boolean isDelete = DELETE.equalsIgnoreCase(method);
        final boolean isPost = POST.equalsIgnoreCase(method);
        final boolean isPut = PUT.equalsIgnoreCase(method);
        byte[] excerpt = null;
        HttpRequestBase httpRequest;
        if (isPost || isPut) {
            HttpEntityEnclosingRequestBase entityEnclosingMethod = isPost ? new HttpPost(url) : new HttpPut(url);
            if (body != null) {
                ExcerptInputStream e = new ExcerptInputStream(body);
                excerpt = e.getExcerpt();
                String length = request.removeHeaders(HttpMessage.CONTENT_LENGTH);
                entityEnclosingMethod.setEntity(new InputStreamEntity(e, (length == null) ? -1 : Long.parseLong(length)));
            }
            httpRequest = entityEnclosingMethod;
        } else if (isDelete) {
            httpRequest = new HttpDelete(url);
        } else {
            httpRequest = new HttpGet(url);
        }
        for (Map.Entry<String, String> header : request.headers) {
            httpRequest.addHeader(header.getKey(), header.getValue());
        }
        HttpParams params = httpRequest.getParams();
        for (Map.Entry<String, Object> p : parameters.entrySet()) {
            String name = p.getKey();
            String value = p.getValue().toString();
            if (FOLLOW_REDIRECTS.equals(name)) {
                params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.parseBoolean(value));
            } else if (READ_TIMEOUT.equals(name)) {
                params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.parseInt(value));
            } else if (CONNECT_TIMEOUT.equals(name)) {
                params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.parseInt(value));
            }
        }
        params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        HttpClient client = clientPool.getHttpClient(new URL(httpRequest.getURI().toString()));
        HttpResponse httpResponse = client.execute(httpRequest);
        return new HttpMethodResponse(httpRequest, httpResponse, excerpt, request.getContentCharset());
    }

    private static final HttpClientPool SHARED_CLIENT = new SingleClient();

    /**
     * A pool that simply shares a single HttpClient. An HttpClient owns a pool
     * of TCP connections. So, callers that share an HttpClient will share
     * connections. Sharing improves performance (by avoiding the overhead of
     * creating connections) and uses fewer resources in the client and its
     * servers.
     */
    private static class SingleClient implements HttpClientPool {

        SingleClient() {
            HttpClient client = new DefaultHttpClient();
            ClientConnectionManager mgr = client.getConnectionManager();
            if (!(mgr instanceof ThreadSafeClientConnManager)) {
                HttpParams params = client.getParams();
                client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
            }
            this.client = client;
        }

        private final HttpClient client;

        public HttpClient getHttpClient(URL server) {
            return client;
        }
    }
}
