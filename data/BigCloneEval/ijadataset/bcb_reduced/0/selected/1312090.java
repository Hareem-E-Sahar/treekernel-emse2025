package twitter4j.http;

import twitter4j.Configuration;
import twitter4j.TwitterException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.AccessControlException;

/**
 * A utility class to handle HTTP request/response.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class HttpClient implements java.io.Serializable {

    private static final int OK = 200;

    private static final int NOT_MODIFIED = 304;

    private static final int BAD_REQUEST = 400;

    private static final int NOT_AUTHORIZED = 401;

    private static final int FORBIDDEN = 403;

    private static final int NOT_FOUND = 404;

    private static final int NOT_ACCEPTABLE = 406;

    private static final int INTERNAL_SERVER_ERROR = 500;

    private static final int BAD_GATEWAY = 502;

    private static final int SERVICE_UNAVAILABLE = 503;

    private static final boolean DEBUG = Configuration.getDebug();

    private String basic;

    private int retryCount = Configuration.getRetryCount();

    private int retryIntervalMillis = Configuration.getRetryIntervalSecs() * 1000;

    private String userId = Configuration.getUser();

    private String password = Configuration.getPassword();

    private String proxyHost = Configuration.getProxyHost();

    private int proxyPort = Configuration.getProxyPort();

    private String proxyAuthUser = Configuration.getProxyUser();

    private String proxyAuthPassword = Configuration.getProxyPassword();

    private int connectionTimeout = Configuration.getConnectionTimeout();

    private int readTimeout = Configuration.getReadTimeout();

    private static final long serialVersionUID = 808018030183407996L;

    private static boolean isJDK14orEarlier = false;

    private Map<String, String> requestHeaders = new HashMap<String, String>();

    private OAuth oauth = null;

    private String requestTokenURL = Configuration.getScheme() + "twitter.com/oauth/request_token";

    private String authorizationURL = Configuration.getScheme() + "twitter.com/oauth/authorize";

    private String authenticationURL = Configuration.getScheme() + "twitter.com/oauth/authenticate";

    private String accessTokenURL = Configuration.getScheme() + "twitter.com/oauth/access_token";

    private OAuthToken oauthToken = null;

    static {
        try {
            String versionStr = System.getProperty("java.specification.version");
            if (null != versionStr) {
                isJDK14orEarlier = 1.5d > Double.parseDouble(versionStr);
            }
        } catch (AccessControlException ace) {
            isJDK14orEarlier = true;
        }
    }

    public HttpClient(String userId, String password) {
        this();
        setUserId(userId);
        setPassword(password);
    }

    public HttpClient() {
        this.basic = null;
        setUserAgent(null);
        setOAuthConsumer(null, null);
        setRequestHeader("Accept-Encoding", "gzip");
    }

    public void setUserId(String userId) {
        this.userId = userId;
        encodeBasicAuthenticationString();
    }

    public void setPassword(String password) {
        this.password = password;
        encodeBasicAuthenticationString();
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuthenticationEnabled() {
        return null != basic || null != oauth;
    }

    /**
     * Sets the consumer key and consumer secret.<br>
     * System property -Dtwitter4j.oauth.consumerKey and -Dhttp.oauth.consumerSecret override this attribute.
     * @param consumerKey Consumer Key
     * @param consumerSecret Consumer Secret
     * @since Twitter4J 2.0.0
     * @see <a href="http://twitter.com/oauth_clients">Applications Using Twitter</a>
     */
    public void setOAuthConsumer(String consumerKey, String consumerSecret) {
        consumerKey = Configuration.getOAuthConsumerKey(consumerKey);
        consumerSecret = Configuration.getOAuthConsumerSecret(consumerSecret);
        if (null != consumerKey && null != consumerSecret && 0 != consumerKey.length() && 0 != consumerSecret.length()) {
            this.oauth = new OAuth(consumerKey, consumerSecret);
        }
    }

    /**
     *
     * @return request token
     * @throws TwitterException tw
     * @since Twitter4J 2.0.0
     */
    public RequestToken getOAuthRequestToken() throws TwitterException {
        this.oauthToken = new RequestToken(httpRequest(requestTokenURL, new PostParameter[0], true), this);
        return (RequestToken) this.oauthToken;
    }

    /**
     * @param callback_url callback url
     * @return request token
     * @throws TwitterException tw
     * @since Twitter4J 2.0.9
     */
    public RequestToken getOauthRequestToken(String callback_url) throws TwitterException {
        this.oauthToken = new RequestToken(httpRequest(requestTokenURL, new PostParameter[] { new PostParameter("oauth_callback", callback_url) }, true), this);
        return (RequestToken) this.oauthToken;
    }

    /**
     *
     * @param token request token
     * @return access token
     * @throws TwitterException
     * @since Twitter4J 2.0.0
     */
    public AccessToken getOAuthAccessToken(RequestToken token) throws TwitterException {
        try {
            this.oauthToken = token;
            this.oauthToken = new AccessToken(httpRequest(accessTokenURL, new PostParameter[0], true));
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
        return (AccessToken) this.oauthToken;
    }

    /**
     *
     * @param token request token
     * @return access token
     * @throws TwitterException
     * @since Twitter4J 2.0.8
     */
    public AccessToken getOAuthAccessToken(RequestToken token, String pin) throws TwitterException {
        try {
            this.oauthToken = token;
            this.oauthToken = new AccessToken(httpRequest(accessTokenURL, new PostParameter[] { new PostParameter("oauth_verifier", pin) }, true));
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
        return (AccessToken) this.oauthToken;
    }

    /**
     *
     * @param token request token
     * @param tokenSecret request token secret
     * @return access token
     * @throws TwitterException
     * @since Twitter4J 2.0.1
     */
    public AccessToken getOAuthAccessToken(String token, String tokenSecret) throws TwitterException {
        try {
            this.oauthToken = new OAuthToken(token, tokenSecret) {
            };
            this.oauthToken = new AccessToken(httpRequest(accessTokenURL, new PostParameter[0], true));
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
        return (AccessToken) this.oauthToken;
    }

    /**
     *
     * @param token request token
     * @param tokenSecret request token secret
     * @param oauth_verifier oauth_verifier or pin
     * @return access token
     * @throws TwitterException
     * @since Twitter4J 2.0.8
     */
    public AccessToken getOAuthAccessToken(String token, String tokenSecret, String oauth_verifier) throws TwitterException {
        try {
            this.oauthToken = new OAuthToken(token, tokenSecret) {
            };
            this.oauthToken = new AccessToken(httpRequest(accessTokenURL, new PostParameter[] { new PostParameter("oauth_verifier", oauth_verifier) }, true));
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
        return (AccessToken) this.oauthToken;
    }

    /**
     * Sets the authorized access token
     * @param token authorized access token
     * @since Twitter4J 2.0.0
     */
    public void setOAuthAccessToken(AccessToken token) {
        this.oauthToken = token;
    }

    public void setRequestTokenURL(String requestTokenURL) {
        this.requestTokenURL = requestTokenURL;
    }

    public String getRequestTokenURL() {
        return requestTokenURL;
    }

    public void setAuthorizationURL(String authorizationURL) {
        this.authorizationURL = authorizationURL;
    }

    public String getAuthorizationURL() {
        return authorizationURL;
    }

    /**
     * since Twitter4J 2.0.10
     */
    public String getAuthenticationRL() {
        return authenticationURL;
    }

    public void setAccessTokenURL(String accessTokenURL) {
        this.accessTokenURL = accessTokenURL;
    }

    public String getAccessTokenURL() {
        return accessTokenURL;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets proxy host.
     * System property -Dtwitter4j.http.proxyHost or http.proxyHost overrides this attribute.
     * @param proxyHost
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = Configuration.getProxyHost(proxyHost);
    }

    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets proxy port.
     * System property -Dtwitter4j.http.proxyPort or -Dhttp.proxyPort overrides this attribute.
     * @param proxyPort
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = Configuration.getProxyPort(proxyPort);
    }

    public String getProxyAuthUser() {
        return proxyAuthUser;
    }

    /**
     * Sets proxy authentication user.
     * System property -Dtwitter4j.http.proxyUser overrides this attribute.
     * @param proxyAuthUser
     */
    public void setProxyAuthUser(String proxyAuthUser) {
        this.proxyAuthUser = Configuration.getProxyUser(proxyAuthUser);
    }

    public String getProxyAuthPassword() {
        return proxyAuthPassword;
    }

    /**
     * Sets proxy authentication password.
     * System property -Dtwitter4j.http.proxyPassword overrides this attribute.
     * @param proxyAuthPassword
     */
    public void setProxyAuthPassword(String proxyAuthPassword) {
        this.proxyAuthPassword = Configuration.getProxyPassword(proxyAuthPassword);
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced by this URLConnection.
     * System property -Dtwitter4j.http.connectionTimeout overrides this attribute.
     * @param connectionTimeout - an int that specifies the connect timeout value in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = Configuration.getConnectionTimeout(connectionTimeout);
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout to a specified timeout, in milliseconds. System property -Dtwitter4j.http.readTimeout overrides this attribute.
     * @param readTimeout - an int that specifies the timeout value to be used in milliseconds
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = Configuration.getReadTimeout(readTimeout);
    }

    private void encodeBasicAuthenticationString() {
        if (null != userId && null != password) {
            this.basic = "Basic " + new String(new BASE64Encoder().encode((userId + ":" + password).getBytes()));
        }
    }

    public void setRetryCount(int retryCount) {
        if (retryCount >= 0) {
            this.retryCount = Configuration.getRetryCount(retryCount);
        } else {
            throw new IllegalArgumentException("RetryCount cannot be negative.");
        }
    }

    public void setUserAgent(String ua) {
        setRequestHeader("User-Agent", Configuration.getUserAgent(ua));
    }

    public String getUserAgent() {
        return getRequestHeader("User-Agent");
    }

    public void setRetryIntervalSecs(int retryIntervalSecs) {
        if (retryIntervalSecs >= 0) {
            this.retryIntervalMillis = Configuration.getRetryIntervalSecs(retryIntervalSecs) * 1000;
        } else {
            throw new IllegalArgumentException("RetryInterval cannot be negative.");
        }
    }

    public Response post(String url, PostParameter[] postParameters, boolean authenticated) throws TwitterException {
        return httpRequest(url, postParameters, authenticated);
    }

    public Response post(String url, boolean authenticated) throws TwitterException {
        return httpRequest(url, new PostParameter[0], authenticated);
    }

    public Response post(String url, PostParameter[] PostParameters) throws TwitterException {
        return httpRequest(url, PostParameters, false);
    }

    public Response post(String url) throws TwitterException {
        return httpRequest(url, new PostParameter[0], false);
    }

    public Response get(String url, boolean authenticated) throws TwitterException {
        return httpRequest(url, null, authenticated);
    }

    public Response get(String url) throws TwitterException {
        return httpRequest(url, null, false);
    }

    protected Response httpRequest(String url, PostParameter[] postParams, boolean authenticated) throws TwitterException {
        int retriedCount;
        int retry = retryCount + 1;
        Response res = null;
        for (retriedCount = 0; retriedCount < retry; retriedCount++) {
            int responseCode = -1;
            try {
                HttpURLConnection con = null;
                OutputStream osw = null;
                try {
                    con = getConnection(url);
                    con.setDoInput(true);
                    setHeaders(url, postParams, con, authenticated);
                    if (null != postParams) {
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        con.setDoOutput(true);
                        String postParam = encodeParameters(postParams);
                        log("Post Params: ", postParam);
                        byte[] bytes = postParam.getBytes("UTF-8");
                        con.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                        osw = con.getOutputStream();
                        osw.write(bytes);
                        osw.flush();
                        osw.close();
                    } else {
                        con.setRequestMethod("GET");
                    }
                    res = new Response(con);
                    responseCode = con.getResponseCode();
                    if (DEBUG) {
                        log("Response: ");
                        Map<String, List<String>> responseHeaders = con.getHeaderFields();
                        for (String key : responseHeaders.keySet()) {
                            List<String> values = responseHeaders.get(key);
                            for (String value : values) {
                                if (null != key) {
                                    log(key + ": " + value);
                                } else {
                                    log(value);
                                }
                            }
                        }
                    }
                    if (responseCode != OK) {
                        if (responseCode < INTERNAL_SERVER_ERROR || retriedCount == retryCount) {
                            throw new TwitterException(getCause(responseCode) + "\n" + res.asString(), responseCode);
                        }
                    } else {
                        break;
                    }
                } finally {
                    try {
                        osw.close();
                    } catch (Exception ignore) {
                    }
                }
            } catch (IOException ioe) {
                if (retriedCount == retryCount) {
                    throw new TwitterException(ioe.getMessage(), ioe, responseCode);
                }
            }
            try {
                if (DEBUG && null != res) {
                    res.asString();
                }
                log("Sleeping " + retryIntervalMillis + " millisecs for next retry.");
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException ignore) {
            }
        }
        return res;
    }

    public static String encodeParameters(PostParameter[] postParams) {
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < postParams.length; j++) {
            if (j != 0) {
                buf.append("&");
            }
            try {
                buf.append(URLEncoder.encode(postParams[j].name, "UTF-8")).append("=").append(URLEncoder.encode(postParams[j].value, "UTF-8"));
            } catch (java.io.UnsupportedEncodingException neverHappen) {
            }
        }
        return buf.toString();
    }

    /**
     * sets HTTP headers
     *
     * @param connection    HttpURLConnection
     * @param authenticated boolean
     */
    private void setHeaders(String url, PostParameter[] params, HttpURLConnection connection, boolean authenticated) {
        log("Request: ");
        if (null != params) {
            log("POST ", url);
        } else {
            log("GET ", url);
        }
        if (authenticated) {
            if (basic == null && oauth == null) {
            }
            String authorization = null;
            if (null != oauth) {
                authorization = oauth.generateAuthorizationHeader(params != null ? "POST" : "GET", url, params, oauthToken);
            } else if (null != basic) {
                authorization = this.basic;
            } else {
                throw new IllegalStateException("Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
            }
            connection.addRequestProperty("Authorization", authorization);
            log("Authorization: " + authorization);
        }
        for (String key : requestHeaders.keySet()) {
            connection.addRequestProperty(key, requestHeaders.get(key));
            log(key + ": " + requestHeaders.get(key));
        }
    }

    public void setRequestHeader(String name, String value) {
        requestHeaders.put(name, value);
    }

    public String getRequestHeader(String name) {
        return requestHeaders.get(name);
    }

    private HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection con = null;
        if (proxyHost != null && !proxyHost.equals("")) {
        } else {
            con = (HttpURLConnection) new URL(url).openConnection();
        }
        if (connectionTimeout > 0 && !isJDK14orEarlier) {
            con.setConnectTimeout(connectionTimeout);
        }
        if (readTimeout > 0 && !isJDK14orEarlier) {
            con.setReadTimeout(readTimeout);
        }
        return con;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpClient)) return false;
        HttpClient that = (HttpClient) o;
        if (connectionTimeout != that.connectionTimeout) return false;
        if (proxyPort != that.proxyPort) return false;
        if (readTimeout != that.readTimeout) return false;
        if (retryCount != that.retryCount) return false;
        if (retryIntervalMillis != that.retryIntervalMillis) return false;
        if (accessTokenURL != null ? !accessTokenURL.equals(that.accessTokenURL) : that.accessTokenURL != null) return false;
        if (!authenticationURL.equals(that.authenticationURL)) return false;
        if (!authorizationURL.equals(that.authorizationURL)) return false;
        if (basic != null ? !basic.equals(that.basic) : that.basic != null) return false;
        if (oauth != null ? !oauth.equals(that.oauth) : that.oauth != null) return false;
        if (oauthToken != null ? !oauthToken.equals(that.oauthToken) : that.oauthToken != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (proxyAuthPassword != null ? !proxyAuthPassword.equals(that.proxyAuthPassword) : that.proxyAuthPassword != null) return false;
        if (proxyAuthUser != null ? !proxyAuthUser.equals(that.proxyAuthUser) : that.proxyAuthUser != null) return false;
        if (proxyHost != null ? !proxyHost.equals(that.proxyHost) : that.proxyHost != null) return false;
        if (!requestHeaders.equals(that.requestHeaders)) return false;
        if (!requestTokenURL.equals(that.requestTokenURL)) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = basic != null ? basic.hashCode() : 0;
        result = 31 * result + retryCount;
        result = 31 * result + retryIntervalMillis;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (proxyHost != null ? proxyHost.hashCode() : 0);
        result = 31 * result + proxyPort;
        result = 31 * result + (proxyAuthUser != null ? proxyAuthUser.hashCode() : 0);
        result = 31 * result + (proxyAuthPassword != null ? proxyAuthPassword.hashCode() : 0);
        result = 31 * result + connectionTimeout;
        result = 31 * result + readTimeout;
        result = 31 * result + requestHeaders.hashCode();
        result = 31 * result + (oauth != null ? oauth.hashCode() : 0);
        result = 31 * result + requestTokenURL.hashCode();
        result = 31 * result + authorizationURL.hashCode();
        result = 31 * result + authenticationURL.hashCode();
        result = 31 * result + (accessTokenURL != null ? accessTokenURL.hashCode() : 0);
        result = 31 * result + (oauthToken != null ? oauthToken.hashCode() : 0);
        return result;
    }

    private static void log(String message) {
        if (DEBUG) {
            System.out.println("[" + new java.util.Date() + "]" + message);
        }
    }

    private static void log(String message, String message2) {
        if (DEBUG) {
            log(message + message2);
        }
    }

    private static String getCause(int statusCode) {
        String cause = null;
        switch(statusCode) {
            case NOT_MODIFIED:
                break;
            case BAD_REQUEST:
                cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";
                break;
            case NOT_AUTHORIZED:
                cause = "Authentication credentials were missing or incorrect.";
                break;
            case FORBIDDEN:
                cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";
                break;
            case NOT_FOUND:
                cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";
                break;
            case NOT_ACCEPTABLE:
                cause = "Returned by the Search API when an invalid format is specified in the request.";
                break;
            case INTERNAL_SERVER_ERROR:
                cause = "Something is broken.  Please post to the group so the Twitter team can investigate.";
                break;
            case BAD_GATEWAY:
                cause = "Twitter is down or being upgraded.";
                break;
            case SERVICE_UNAVAILABLE:
                cause = "Service Unavailable: The Twitter servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.";
                break;
            default:
                cause = "";
        }
        return statusCode + ":" + cause;
    }
}
