package android.net.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.harmony.xnet.provider.jsse.SSLClientSessionCache;
import org.apache.harmony.xnet.provider.jsse.SSLContextImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.net.URI;
import java.security.KeyManagementException;
import android.util.Log;
import android.content.ContentResolver;
import android.provider.Settings;
import android.text.TextUtils;
import android.os.SystemProperties;

/**
 * Subclass of the Apache {@link DefaultHttpClient} that is configured with
 * reasonable default settings and registered schemes for Android, and
 * also lets the user add {@link HttpRequestInterceptor} classes.
 * Don't create this directly, use the {@link #newInstance} factory method.
 *
 * <p>This client processes cookies but does not retain them by default.
 * To retain cookies, simply add a cookie store to the HttpContext:</p>
 *
 * <pre>context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);</pre>
 * 
 * {@hide}
 */
public final class AndroidHttpClient implements HttpClient {

    public static long DEFAULT_SYNC_MIN_GZIP_BYTES = 256;

    private static final String TAG = "AndroidHttpClient";

    /** Set if HTTP requests are blocked from being executed on this thread */
    private static final ThreadLocal<Boolean> sThreadBlocked = new ThreadLocal<Boolean>();

    /** Interceptor throws an exception if the executing thread is blocked */
    private static final HttpRequestInterceptor sThreadCheckInterceptor = new HttpRequestInterceptor() {

        public void process(HttpRequest request, HttpContext context) {
            if (sThreadBlocked.get() != null && sThreadBlocked.get()) {
                throw new RuntimeException("This thread forbids HTTP requests");
            }
        }
    };

    /**
     * Create a new HttpClient with reasonable defaults (which you can update).
     *
     * @param userAgent to report in your HTTP requests.
     * @param sessionCache persistent session cache
     * @return AndroidHttpClient for you to use for all your requests.
     */
    public static AndroidHttpClient newInstance(String userAgent, SSLClientSessionCache sessionCache) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
        HttpConnectionParams.setSoTimeout(params, 20 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClientParams.setRedirecting(params, false);
        HttpProtocolParams.setUserAgent(params, userAgent);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", socketFactoryWithCache(sessionCache), 443));
        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
        return new AndroidHttpClient(manager, params);
    }

    /**
     * Returns a socket factory backed by the given persistent session cache.
     *
     * @param sessionCache to retrieve sessions from, null for no cache
     */
    private static SSLSocketFactory socketFactoryWithCache(SSLClientSessionCache sessionCache) {
        if (sessionCache == null) {
            return SSLSocketFactory.getSocketFactory();
        }
        SSLContextImpl sslContext = new SSLContextImpl();
        try {
            sslContext.engineInit(null, null, null, sessionCache, null);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        }
        return new SSLSocketFactory(sslContext.engineGetSocketFactory());
    }

    /**
     * Create a new HttpClient with reasonable defaults (which you can update).
     * @param userAgent to report in your HTTP requests.
     * @return AndroidHttpClient for you to use for all your requests.
     */
    public static AndroidHttpClient newInstance(String userAgent) {
        return newInstance(userAgent, null);
    }

    private final HttpClient delegate;

    private RuntimeException mLeakedException = new IllegalStateException("AndroidHttpClient created and never closed");

    private AndroidHttpClient(ClientConnectionManager ccm, HttpParams params) {
        this.delegate = new DefaultHttpClient(ccm, params) {

            @Override
            protected BasicHttpProcessor createHttpProcessor() {
                BasicHttpProcessor processor = super.createHttpProcessor();
                processor.addRequestInterceptor(sThreadCheckInterceptor);
                processor.addRequestInterceptor(new CurlLogger());
                return processor;
            }

            @Override
            protected HttpContext createHttpContext() {
                HttpContext context = new BasicHttpContext();
                context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, getAuthSchemes());
                context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, getCookieSpecs());
                context.setAttribute(ClientContext.CREDS_PROVIDER, getCredentialsProvider());
                return context;
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLeakedException != null) {
            Log.e(TAG, "Leak found", mLeakedException);
            mLeakedException = null;
        }
    }

    /**
     * Block this thread from executing HTTP requests.
     * Used to guard against HTTP requests blocking the main application thread.
     * @param blocked if HTTP requests run on this thread should be denied
     */
    public static void setThreadBlocked(boolean blocked) {
        sThreadBlocked.set(blocked);
    }

    /**
     * Modifies a request to indicate to the server that we would like a
     * gzipped response.  (Uses the "Accept-Encoding" HTTP header.)
     * @param request the request to modify
     * @see #getUngzippedContent
     */
    public static void modifyRequestToAcceptGzipResponse(HttpRequest request) {
        request.addHeader("Accept-Encoding", "gzip");
    }

    /**
     * Gets the input stream from a response entity.  If the entity is gzipped
     * then this will get a stream over the uncompressed data.
     *
     * @param entity the entity whose content should be read
     * @return the input stream to read from
     * @throws IOException
     */
    public static InputStream getUngzippedContent(HttpEntity entity) throws IOException {
        InputStream responseStream = entity.getContent();
        if (responseStream == null) return responseStream;
        Header header = entity.getContentEncoding();
        if (header == null) return responseStream;
        String contentEncoding = header.getValue();
        if (contentEncoding == null) return responseStream;
        if (contentEncoding.contains("gzip")) responseStream = new GZIPInputStream(responseStream);
        return responseStream;
    }

    /**
     * Release resources associated with this client.  You must call this,
     * or significant resources (sockets and memory) may be leaked.
     */
    public void close() {
        if (mLeakedException != null) {
            getConnectionManager().shutdown();
            mLeakedException = null;
        }
    }

    public HttpParams getParams() {
        return delegate.getParams();
    }

    public ClientConnectionManager getConnectionManager() {
        return delegate.getConnectionManager();
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        return delegate.execute(request);
    }

    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        return delegate.execute(request, context);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
        return delegate.execute(target, request);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        return delegate.execute(target, request, context);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return delegate.execute(request, responseHandler);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return delegate.execute(request, responseHandler, context);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return delegate.execute(target, request, responseHandler);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return delegate.execute(target, request, responseHandler, context);
    }

    /**
     * Compress data to send to server.
     * Creates a Http Entity holding the gzipped data.
     * The data will not be compressed if it is too short.
     * @param data The bytes to compress
     * @return Entity holding the data
     */
    public static AbstractHttpEntity getCompressedEntity(byte data[], ContentResolver resolver) throws IOException {
        AbstractHttpEntity entity;
        if (data.length < getMinGzipSize(resolver)) {
            entity = new ByteArrayEntity(data);
        } else {
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            OutputStream zipper = new GZIPOutputStream(arr);
            zipper.write(data);
            zipper.close();
            entity = new ByteArrayEntity(arr.toByteArray());
            entity.setContentEncoding("gzip");
        }
        return entity;
    }

    /**
     * Retrieves the minimum size for compressing data.
     * Shorter data will not be compressed.
     */
    public static long getMinGzipSize(ContentResolver resolver) {
        String sMinGzipBytes = Settings.Gservices.getString(resolver, Settings.Gservices.SYNC_MIN_GZIP_BYTES);
        if (!TextUtils.isEmpty(sMinGzipBytes)) {
            try {
                return Long.parseLong(sMinGzipBytes);
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Unable to parse " + Settings.Gservices.SYNC_MIN_GZIP_BYTES + " " + sMinGzipBytes, nfe);
            }
        }
        return DEFAULT_SYNC_MIN_GZIP_BYTES;
    }

    /**
     * Logging tag and level.
     */
    private static class LoggingConfiguration {

        private final String tag;

        private final int level;

        private LoggingConfiguration(String tag, int level) {
            this.tag = tag;
            this.level = level;
        }

        /**
         * Returns true if logging is turned on for this configuration.
         */
        private boolean isLoggable() {
            return Log.isLoggable(tag, level);
        }

        /**
         * Returns true if auth logging is turned on for this configuration.  Can only be set on
         * insecure devices.
         */
        private boolean isAuthLoggable() {
            String secure = SystemProperties.get("ro.secure");
            return "0".equals(secure) && Log.isLoggable(tag + "-auth", level);
        }

        /**
         * Prints a message using this configuration.
         */
        private void println(String message) {
            Log.println(level, tag, message);
        }
    }

    /** cURL logging configuration. */
    private volatile LoggingConfiguration curlConfiguration;

    /**
     * Enables cURL request logging for this client.
     *
     * @param name to log messages with
     * @param level at which to log messages (see {@link android.util.Log})
     */
    public void enableCurlLogging(String name, int level) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (level < Log.VERBOSE || level > Log.ASSERT) {
            throw new IllegalArgumentException("Level is out of range [" + Log.VERBOSE + ".." + Log.ASSERT + "]");
        }
        curlConfiguration = new LoggingConfiguration(name, level);
    }

    /**
     * Disables cURL logging for this client.
     */
    public void disableCurlLogging() {
        curlConfiguration = null;
    }

    /**
     * Logs cURL commands equivalent to requests.
     */
    private class CurlLogger implements HttpRequestInterceptor {

        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            LoggingConfiguration configuration = curlConfiguration;
            if (configuration != null && configuration.isLoggable() && request instanceof HttpUriRequest) {
                configuration.println(toCurl((HttpUriRequest) request, configuration.isAuthLoggable()));
            }
        }
    }

    /**
     * Generates a cURL command equivalent to the given request.
     */
    private static String toCurl(HttpUriRequest request, boolean logAuthToken) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("curl ");
        for (Header header : request.getAllHeaders()) {
            if (!logAuthToken && (header.getName().equals("Authorization") || header.getName().equals("Cookie"))) {
                continue;
            }
            builder.append("--header \"");
            builder.append(header.toString().trim());
            builder.append("\" ");
        }
        URI uri = request.getURI();
        if (request instanceof RequestWrapper) {
            HttpRequest original = ((RequestWrapper) request).getOriginal();
            if (original instanceof HttpUriRequest) {
                uri = ((HttpUriRequest) original).getURI();
            }
        }
        builder.append("\"");
        builder.append(uri);
        builder.append("\"");
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
            HttpEntity entity = entityRequest.getEntity();
            if (entity != null && entity.isRepeatable()) {
                if (entity.getContentLength() < 1024) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    entity.writeTo(stream);
                    String entityString = stream.toString();
                    builder.append(" --data-ascii \"").append(entityString).append("\"");
                } else {
                    builder.append(" [TOO MUCH DATA TO INCLUDE]");
                }
            }
        }
        return builder.toString();
    }
}
