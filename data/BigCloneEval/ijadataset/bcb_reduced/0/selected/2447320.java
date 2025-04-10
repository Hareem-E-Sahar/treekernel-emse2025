package net.grinder.plugin.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.grinder.common.GrinderException;
import net.grinder.common.Logger;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.PluginProcessContext;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.script.InvalidContextException;
import net.grinder.script.Statistics;
import net.grinder.script.Grinder.ScriptContext;
import net.grinder.script.Statistics.StatisticsForTest;
import net.grinder.statistics.StatisticsIndexMap;
import net.grinder.util.StreamCopier;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import HTTPClient.NVPair;
import HTTPClient.ParseException;
import HTTPClient.ProtocolNotSuppException;
import HTTPClient.URI;

/**
 * An individual HTTP request for use in scripts.
 *
 * <p>Scripts can set default values for the URL, headers, and data.
 * There are several overloaded methods corresponding to each HTTP
 * method (GET, POST, ...) that allow specific values to override the
 * defaults.</p>
 *
 * <p><strong>WARNING:</strong> The default values set with the various
 * <code>set</code> methods, and apply to all users of the
 * <code>HTTPRequest</code>. If a worker thread needs to set a thread specific
 * value, it should either use its own <code>HTTPRequest</code>, or not use the
 * defaults and pass the value as an argument to the HTTP method.</p>
 *
 * @author Philip Aston
 * @version $Revision: 3847 $
 */
public class HTTPRequest {

    static {
        HTTPPlugin.getPlugin();
    }

    private static final Pattern s_pathParser = Pattern.compile("([^?#]*)(\\?([^#]*))?(#(.*))?");

    private static final Pattern s_absoluteURIPattern = Pattern.compile("^[^:/?#]*:.*");

    private volatile URI m_defaultURL;

    private volatile NVPair[] m_defaultHeaders = new NVPair[0];

    private volatile byte[] m_defaultData;

    private volatile NVPair[] m_defaultFormData;

    /**
   * Creates a new <code>HTTPRequest</code> instance.
   */
    public HTTPRequest() {
    }

    /**
   * Gets the default URL.
   *
   * @return The default URL to be used for this request, or
   * <code>null</code> if the default URL has not been set.
   */
    public final String getUrl() {
        final URI url = m_defaultURL;
        return url != null ? url.toString() : null;
    }

    /**
   * Sets the default URL. The value given must be an absolute URL,
   * including protocol and the server information.
   *
   * <p>See the {@link HTTPRequest warning above} regarding thread safety.
   * Multiple worker threads that need to set a specific URL should either not
   * share the same <code>HTTPRequest</code>, or pass the URL as an argument
   * to the call to the HTTP method.</p>
   *
   * @param url The URL to be used for this request.
   * @throws ParseException If the URL cannot be parsed.
   * @throws URLException If the URL is not absolute.
   */
    public final void setUrl(String url) throws ParseException, URLException {
        if (!isAbsolute(url)) {
            throw new URLException("URL must be absolute");
        }
        m_defaultURL = new URI(url);
    }

    /**
   * Gets the default headers.
   *
   * @return The default headers to be used for this request.
   */
    public final NVPair[] getHeaders() {
        return m_defaultHeaders;
    }

    /**
   * Merges two NVPair arrays.
   *
   * @param defaultPairs
   *          Default array.
   * @param overridePairs
   *          Array to merge. Entries take precedence over
   *          <code>defaultPairs</code> entries with the same name.
   * @return The merged arrays. For efficiency's sake, we do not filter out
   *         <code>null</code> entries.
   */
    private NVPair[] mergeArrays(NVPair[] defaultPairs, NVPair[] overridePairs) {
        if (defaultPairs.length == 0) {
            return overridePairs;
        }
        if (overridePairs.length == 0) {
            return defaultPairs;
        }
        final NVPair[] result = new NVPair[defaultPairs.length + overridePairs.length];
        final Set seen = new HashSet();
        for (int i = 0; i < overridePairs.length; ++i) {
            result[i] = overridePairs[i];
            seen.add(overridePairs[i].getName());
        }
        for (int i = 0; i < defaultPairs.length; ++i) {
            if (!seen.contains(defaultPairs[i].getName())) {
                result[overridePairs.length + i] = defaultPairs[i];
            }
        }
        return result;
    }

    private NVPair[] mergeHeaders(NVPair[] headers) {
        return mergeArrays(getHeaders(), headers);
    }

    /**
   * Sets the default headers.
   *
   * <p>See the {@link HTTPRequest warning above} regarding thread safety.
   * Multiple worker threads that need to set specific headers should either not
   * share the same <code>HTTPRequest</code>, or pass the headers as an argument
   * to the call to the HTTP method.</p>
   *
   * @param headers The default headers to be used for this request.
   */
    public final void setHeaders(NVPair[] headers) {
        m_defaultHeaders = headers;
    }

    /**
   * Returns a string representation of the object and URL headers.
   *
   * @return a string representation of the object
   */
    public String toString() {
        final StringBuffer result = new StringBuffer("");
        final URI url = m_defaultURL;
        if (url == null) {
            result.append("<Undefined URL>\n");
        } else {
            result.append(url.toString());
            result.append("\n");
        }
        final NVPair[] defaultHeaders = m_defaultHeaders;
        for (int i = 0; i < defaultHeaders.length; i++) {
            result.append(defaultHeaders[i].getName());
            result.append(": ");
            result.append(defaultHeaders[i].getValue());
            result.append("\n");
        }
        return result.toString();
    }

    /**
   * Gets the default data.
   *
   * @return The default data to be used for this request.
   */
    public final byte[] getData() {
        return m_defaultData;
    }

    /**
   * Sets the default data.
   *
   * <p>See the {@link HTTPRequest warning above} regarding thread safety.
   * Multiple worker threads that need to set specific data should either not
   * share the same <code>HTTPRequest</code>, or pass the data as an argument
   * to the call to <code>POST</code>, or <code>PUT</code>.</p>
   *
   * @param data The default data to be used for this request.
   */
    public final void setData(byte[] data) {
        m_defaultData = data;
    }

    /**
   * Sets the default data from a file.
   *
   * <p>See the {@link HTTPRequest warning above} regarding thread safety.
   * Multiple worker threads that need to set specific data should either not
   * share the same <code>HTTPRequest</code>, or pass the data as an argument
   * to the call to <code>POST</code>, or <code>PUT</code>. If the later is
   * done, this method can still be used to read data from a file.</p>
   *
   * @param filename Path name of data file.
   * @return The data read from the file.
   * @throws IOException If the file could not be read.
   */
    public final byte[] setDataFromFile(String filename) throws IOException {
        final File file = new File(filename);
        final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream((int) file.length());
        new StreamCopier(4096, true).copy(new FileInputStream(file), byteArrayStream);
        final byte[] defaultData = byteArrayStream.toByteArray();
        m_defaultData = defaultData;
        return defaultData;
    }

    /**
   * Gets the default form data.
   *
   * @return The default form or query data to be used for this
   * request.
   */
    public final NVPair[] getFormData() {
        return m_defaultFormData;
    }

    /**
   * Sets the default form data.
   *
   * @param formData The default form or query data to be used for
   * this request.
   */
    public final void setFormData(NVPair[] formData) {
        m_defaultFormData = formData;
    }

    /**
   * Makes an HTTP <code>DELETE</code> request.
   *
   * @return Contains details of the servers response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse DELETE() throws Exception {
        return DELETE(null, getHeaders());
    }

    /**
   * Makes an HTTP <code>DELETE</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse DELETE(String uri) throws Exception {
        return DELETE(uri, getHeaders());
    }

    /**
   * Makes an HTTP <code>DELETE</code> request.
   *
   * @param uri
   *          The URI. If a default URL has been specified with {@link #setUrl},
   *          this value need not be absolute and, if relative, it will be
   *          resolved relative to the default URL. Otherwise this value must be
   *          an absolute URL.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception
   *              If an error occurs.
   */
    public final HTTPResponse DELETE(final String uri, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Delete(path, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>GET</code> request.
   *
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse GET() throws Exception {
        return GET((String) null);
    }

    /**
   * Makes an HTTP <code>GET</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse GET(String uri) throws Exception {
        return GET(uri, getFormData(), getHeaders());
    }

    /**
   * Makes an HTTP <code>GET</code> request.
   *
   * @param queryData Request headers. Replaces all the values set
   * by {@link #setFormData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse GET(NVPair[] queryData) throws Exception {
        return GET(null, queryData, getHeaders());
    }

    /**
   * Makes an HTTP <code>GET</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param queryData Request headers. Replaces all the values set
   * by {@link #setFormData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse GET(final String uri, final NVPair[] queryData) throws Exception {
        return GET(uri, queryData, getHeaders());
    }

    /**
   * Makes an HTTP <code>GET</code> request.
   *
   * @param uri
   *          The URI. If a default URL has been specified with {@link #setUrl},
   *          this value need not be absolute and, if relative, it will be
   *          resolved relative to the default URL. Otherwise this value must be
   *          an absolute URL.
   * @param queryData
   *          Request headers. Replaces all the values set by
   *          {@link #setFormData}.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception
   *              If an error occurs.
   */
    public final HTTPResponse GET(final String uri, final NVPair[] queryData, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Get(path, queryData, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>HEAD</code> request.
   *
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse HEAD() throws Exception {
        return HEAD(null, getFormData(), getHeaders());
    }

    /**
   * Makes an HTTP <code>HEAD</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse HEAD(String uri) throws Exception {
        return HEAD(uri, getHeaders());
    }

    /**
   * Makes an HTTP <code>HEAD</code> request.
   *
   * @param queryData Request headers. Replaces all the values set
   * by {@link #setFormData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse HEAD(NVPair[] queryData) throws Exception {
        return HEAD(null, queryData, getHeaders());
    }

    /**
   * Makes an HTTP <code>HEAD</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param queryData Request headers. Replaces all the values set
   * by {@link #setFormData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse HEAD(final String uri, final NVPair[] queryData) throws Exception {
        return HEAD(uri, queryData, getHeaders());
    }

    /**
   * Makes an HTTP <code>HEAD</code> request.
   *
   * @param uri
   *          The URI. If a default URL has been specified with {@link #setUrl},
   *          this value need not be absolute and, if relative, it will be
   *          resolved relative to the default URL. Otherwise this value must be
   *          an absolute URL.
   * @param queryData
   *          Request headers. Replaces all the values set by
   *          {@link #setFormData}.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception
   *              If an error occurs.
   */
    public final HTTPResponse HEAD(final String uri, final NVPair[] queryData, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Head(path, queryData, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>OPTIONS</code> request.
   *
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse OPTIONS() throws Exception {
        return OPTIONS(null, getData(), getHeaders());
    }

    /**
   * Makes an HTTP <code>OPTIONS</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse OPTIONS(String uri) throws Exception {
        return OPTIONS(uri, getData(), getHeaders());
    }

    /**
   * Makes an HTTP <code>OPTIONS</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param data Data to be submitted in the body of the request.
   * Overrides the value set with {@link #setData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse OPTIONS(final String uri, final byte[] data) throws Exception {
        return OPTIONS(uri, data, getHeaders());
    }

    /**
   * Makes an HTTP <code>OPTIONS</code> request.
   *
   * @param uri
   *          The URI. If a default URL has been specified with {@link #setUrl},
   *          this value need not be absolute and, if relative, it will be
   *          resolved relative to the default URL. Otherwise this value must be
   *          an absolute URL.
   * @param data
   *          Data to be submitted in the body of the request. Overrides the
   *          value set with {@link #setData}.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception
   *              If an error occurs.
   */
    public final HTTPResponse OPTIONS(final String uri, final byte[] data, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Options(path, mergeHeaders(headers), data);
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST() throws Exception {
        return POST((String) null);
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST(String uri) throws Exception {
        final byte[] data = getData();
        if (data != null) {
            return POST(uri, data, getHeaders());
        } else {
            return POST(uri, getFormData(), getHeaders());
        }
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @param formData Data to be submitted as an
   * <code>application/x-www-form-urlencoded</code> encoded request
   * body.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST(NVPair[] formData) throws Exception {
        return POST(null, formData, getHeaders());
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param formData Data to be submitted as an
   * <code>application/x-www-form-urlencoded</code> encoded request
   * body.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST(String uri, NVPair[] formData) throws Exception {
        return POST(uri, formData, getHeaders());
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param formData Data to be submitted as an
   * <code>application/x-www-form-urlencoded</code> encoded request
   * body.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST(final String uri, final NVPair[] formData, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Post(path, formData, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param data Data to be submitted in the body of the request.
   * Overrides the value set with {@link #setData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST(String uri, byte[] data) throws Exception {
        return POST(uri, data, getHeaders());
    }

    /**
   * Makes an HTTP <code>POST</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param data Data to be submitted in the body of the request.
   * Overrides the value set with {@link #setData}.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse POST(final String uri, final byte[] data, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Post(path, data, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>PUT</code> request.
   *
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse PUT() throws Exception {
        return PUT(null, getData(), getHeaders());
    }

    /**
   * Makes an HTTP <code>PUT</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse PUT(String uri) throws Exception {
        return PUT(uri, getData(), getHeaders());
    }

    /**
   * Makes an HTTP <code>PUT</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @param data Data to be submitted in the body of the request.
   * Overrides the value set with {@link #setData}.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse PUT(String uri, byte[] data) throws Exception {
        return PUT(uri, data, getHeaders());
    }

    /**
   * Makes an HTTP <code>PUT</code> request.
   *
   * @param uri
   *          The URI. If a default URL has been specified with {@link #setUrl},
   *          this value need not be absolute and, if relative, it will be
   *          resolved relative to the default URL. Otherwise this value must be
   *          an absolute URL.
   * @param data
   *          Data to be submitted in the body of the request. Overrides the
   *          value set with {@link #setData}.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception
   *              If an error occurs.
   */
    public final HTTPResponse PUT(final String uri, final byte[] data, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Put(path, data, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Makes an HTTP <code>TRACE</code> request.
   *
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse TRACE() throws Exception {
        return TRACE(null, getHeaders());
    }

    /**
   * Makes an HTTP <code>TRACE</code> request.
   *
   * @param uri The URI. If a default URL has been specified with
   * {@link #setUrl}, this value need not be absolute and, if
   * relative, it will be resolved relative to the default URL.
   * Otherwise this value must be an absolute URL.
   * @return Contains details of the server's response.
   * @throws Exception If an error occurs.
   */
    public final HTTPResponse TRACE(String uri) throws Exception {
        return TRACE(uri, getHeaders());
    }

    /**
   * Makes an HTTP <code>TRACE</code> request.
   *
   * @param uri
   *          The URI. If a default URL has been specified with {@link #setUrl},
   *          this value need not be absolute and, if relative, it will be
   *          resolved relative to the default URL. Otherwise this value must be
   *          an absolute URL.
   * @param headers
   *          Request headers. Overrides headers with matching names set by
   *          {@link #setHeaders}.
   * @return Contains details of the server's response.
   * @throws Exception
   *              If an error occurs.
   */
    public final HTTPResponse TRACE(final String uri, final NVPair[] headers) throws Exception {
        return new AbstractRequest(uri) {

            HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException {
                return connection.Trace(path, mergeHeaders(headers));
            }
        }.getHTTPResponse();
    }

    /**
   * Subclasses of HTTPRequest that wish to post-process responses
   * should override this method.
   *
   * @param response The response.
   */
    protected void processResponse(HTTPResponse response) {
    }

    private abstract class AbstractRequest {

        private final URI m_url;

        public AbstractRequest(String uri) throws ParseException, URLException {
            final URI defaultURL = m_defaultURL;
            if (uri == null) {
                if (defaultURL == null) {
                    throw new URLException("URL not specified");
                }
                m_url = defaultURL;
            } else if (isAbsolute(uri)) {
                m_url = new URI(uri);
            } else {
                if (defaultURL == null) {
                    throw new URLException("URL must be absolute");
                }
                if (uri.startsWith("//")) {
                    final Matcher matcher = s_pathParser.matcher(uri);
                    matcher.matches();
                    final String path = matcher.group(1);
                    final String query = matcher.group(2);
                    final String fragment = matcher.group(3);
                    m_url = new URI(defaultURL.getScheme(), defaultURL.getUserinfo(), defaultURL.getHost(), defaultURL.getPort(), path, query, fragment);
                } else {
                    m_url = new URI(defaultURL, uri);
                }
            }
        }

        public final HTTPResponse getHTTPResponse() throws GrinderException, IOException, ModuleException, ParseException, ProtocolNotSuppException {
            final HTTPPlugin plugin = HTTPPlugin.getPlugin();
            final PluginProcessContext pluginProcessContext = plugin.getPluginProcessContext();
            final HTTPPluginThreadState threadState = (HTTPPluginThreadState) pluginProcessContext.getPluginThreadListener();
            final PluginThreadContext threadContext = threadState.getThreadContext();
            final String pathAndQuery = m_url.getPathAndQuery();
            final String fragment = m_url.getFragment();
            final String path = fragment != null ? pathAndQuery + '#' + fragment : pathAndQuery;
            final HTTPConnection connection = threadState.getConnectionWrapper(m_url).getConnection();
            final long startTime = connection.getTimeAuthority().getTimeInMilliseconds();
            final HTTPResponse httpResponse;
            try {
                httpResponse = doRequest(connection, path);
            } catch (InterruptedIOException e) {
                throw new TimeoutException(e);
            }
            final byte[] data = httpResponse.getData();
            final int responseLength = data.length;
            httpResponse.getInputStream().close();
            threadContext.pauseClock();
            final long dnsTime = connection.getDnsTime() - startTime;
            final long connectTime = connection.getConnectTime() - startTime;
            final long timeToFirstByte = httpResponse.getTimeToFirstByte() - startTime;
            final int statusCode = httpResponse.getStatusCode();
            final String message = httpResponse.getOriginalURI() + " -> " + statusCode + " " + httpResponse.getReasonLine() + ", " + responseLength + " bytes";
            final ScriptContext scriptContext = pluginProcessContext.getScriptContext();
            final Logger logger = scriptContext.getLogger();
            switch(statusCode) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case 307:
                    logger.output(message + " [Redirect, ensure the next URL is " + httpResponse.getHeader("Location") + "]");
                    break;
                default:
                    logger.output(message);
                    break;
            }
            try {
                final Statistics statistics = scriptContext.getStatistics();
                if (statistics.isTestInProgress()) {
                    final StatisticsForTest statisticsForCurrentTest = statistics.getForCurrentTest();
                    statisticsForCurrentTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY, responseLength);
                    statisticsForCurrentTest.setLong(StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_STATUS_KEY, statusCode);
                    if (dnsTime >= 0) {
                        statisticsForCurrentTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_DNS_TIME_KEY, dnsTime);
                    }
                    if (connectTime >= 0) {
                        statisticsForCurrentTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY, connectTime);
                    }
                    statisticsForCurrentTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_FIRST_BYTE_TIME_KEY, timeToFirstByte);
                    if (statusCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                        statisticsForCurrentTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_ERRORS_KEY, 1);
                    }
                }
            } catch (InvalidContextException e) {
                throw new PluginException("Failed to set statistic", e);
            }
            processResponse(httpResponse);
            threadState.setLastResponse(httpResponse);
            threadContext.resumeClock();
            return httpResponse;
        }

        abstract HTTPResponse doRequest(HTTPConnection connection, String path) throws IOException, ModuleException;
    }

    private static boolean isAbsolute(String uri) {
        return s_absoluteURIPattern.matcher(uri).matches();
    }
}
