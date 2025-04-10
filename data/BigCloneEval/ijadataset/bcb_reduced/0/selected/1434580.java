package com.google.gdata.client;

import com.google.gdata.util.common.base.CharEscapers;
import com.google.gdata.util.common.base.StringUtil;
import com.google.gdata.client.GoogleService.AccountDeletedException;
import com.google.gdata.client.GoogleService.AccountDisabledException;
import com.google.gdata.client.GoogleService.CaptchaRequiredException;
import com.google.gdata.client.GoogleService.InvalidCredentialsException;
import com.google.gdata.client.GoogleService.NotVerifiedException;
import com.google.gdata.client.GoogleService.ServiceUnavailableException;
import com.google.gdata.client.GoogleService.SessionExpiredException;
import com.google.gdata.client.GoogleService.TermsNotAgreedException;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.http.HttpAuthToken;
import com.google.gdata.util.AuthenticationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating Google authentication tokens (ClientLogin and
 * AuthSub).
 *
 * 
 */
public class GoogleAuthTokenFactory implements AuthTokenFactory {

    private String applicationName;

    private String serviceName;

    private String username;

    private String password;

    private String domainName;

    private String loginProtocol;

    private HttpAuthToken authToken;

    private TokenListener tokenListener;

    /**
   * The path name of the Google accounts management handler.
   */
    public static final String GOOGLE_ACCOUNTS_PATH = "/accounts";

    /**
   * The path name of the Google login handler.
   */
    public static final String GOOGLE_LOGIN_PATH = "/accounts/ClientLogin";

    /**
   * The UserToken encapsulates the token retrieved as a result of
   * authenticating to Google using a user's credentials.
   */
    public static class UserToken implements HttpAuthToken {

        private String token;

        public UserToken(String token) {
            this.token = token;
        }

        public String getValue() {
            return token;
        }

        /**
     * Returns an authorization header to be used for a HTTP request
     * for the respective authentication token.
     *
     * @param requestUrl the URL being requested
     * @param requestMethod the HTTP method of the request
     * @return the "Authorization" header to be used for the request
     */
        public String getAuthorizationHeader(URL requestUrl, String requestMethod) {
            return "GoogleLogin auth=" + token;
        }
    }

    /**
   * Encapsulates the token used by web applications to login on behalf of
   * a user.
   */
    public static class AuthSubToken implements HttpAuthToken {

        private String token;

        private PrivateKey key;

        public AuthSubToken(String token, PrivateKey key) {
            this.token = token;
            this.key = key;
        }

        public String getValue() {
            return token;
        }

        /**
     * Returns an authorization header to be used for a HTTP request
     * for the respective authentication token.
     *
     * @param requestUrl the URL being requested
     * @param requestMethod the HTTP method of the request
     * @return the "Authorization" header to be used for the request
     */
        public String getAuthorizationHeader(URL requestUrl, String requestMethod) {
            try {
                return AuthSubUtil.formAuthorizationHeader(token, key, requestUrl, requestMethod);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
   * Encapsulates the OAuth information used by applications to login on behalf
   * of a user.  This class generates an unique authorization header for each
   * request.
   */
    public static class OAuthToken implements HttpAuthToken {

        OAuthParameters parameters;

        GoogleOAuthHelper oauthHelper;

        /**
     * Create a new {@link OAuthToken} object.  Store the
     * {@link OAuthParameters} and {@link OAuthSigner} to use when generating
     * the header.  The following OAuth parameters are required:
     * <ul>
     * <li>oauth_consumer_key
     * <li>oauth_token
     * </ul>
     *
     * @param parameters the required OAuth parameters
     * @param signer the {@link OAuthSigner} object to use when to generate the
     *        OAuth signature.
     */
        public OAuthToken(OAuthParameters parameters, OAuthSigner signer) {
            this.parameters = parameters;
            oauthHelper = new GoogleOAuthHelper(signer);
        }

        /**
     * Generates the OAuth authorization header using the user's OAuth
     * parameters, the request url and the request method.
     *
     * @param requestUrl the URL being requested
     * @param requestMethod the HTTP method of the request
     * @return the authorization header to be used for the request
     */
        public String getAuthorizationHeader(URL requestUrl, String requestMethod) {
            try {
                return oauthHelper.getAuthorizationHeader(requestUrl.toString(), requestMethod, parameters);
            } catch (OAuthException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
   * Constructs a factory for creating authentication tokens for connecting
   * to a Google service with name {@code serviceName} for an application
   * with the name {@code applicationName}. The default domain
   * (www.google.com) will be used to authenticate.
   *
   * @param serviceName     the name of the Google service to which we are
   *                        connecting. Sample names of services might include
   *                        "cl" (Calendar), "mail" (GMail), or
   *                        "blogger" (Blogger)
   * @param applicationName the name of the client application accessing the
   *                        service.  Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   * @param tokenListener   listener for token changes
   */
    public GoogleAuthTokenFactory(String serviceName, String applicationName, TokenListener tokenListener) {
        this(serviceName, applicationName, "https", "www.google.com", tokenListener);
    }

    /**
   * Constructs a factory for creating authentication tokens for connecting
   * to a Google service with name {@code serviceName} for an application
   * with the name {@code applicationName}. The service will authenticate
   * at the provided {@code domainName}.
   *
   * @param serviceName     the name of the Google service to which we are
   *                        connecting. Sample names of services might include
   *                        "cl" (Calendar), "mail" (GMail), or
   *                        "blogger" (Blogger)
   * @param applicationName the name of the client application accessing the
   *                        service. Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   * @param protocol        name of protocol to use for authentication
   *                        ("http"/"https")
   * @param domainName      the name of the domain hosting the login handler
   * @param tokenListener   listener for token changes
   */
    public GoogleAuthTokenFactory(String serviceName, String applicationName, String protocol, String domainName, TokenListener tokenListener) {
        this.serviceName = serviceName;
        this.applicationName = applicationName;
        this.domainName = domainName;
        this.loginProtocol = protocol;
        this.tokenListener = tokenListener;
    }

    /**
   * Sets the credentials of the user to authenticate requests to the server.
   *
   * @param username the name of the user (an email address)
   * @param password the password of the user
   * @throws AuthenticationException if authentication failed.
   */
    public void setUserCredentials(String username, String password) throws AuthenticationException {
        setUserCredentials(username, password, ClientLoginAccountType.HOSTED_OR_GOOGLE);
    }

    /**
   * Sets the credentials of the user to authenticate requests to the server.
   *
   * @param username the name of the user (an email address)
   * @param password the password of the user
   * @param accountType the account type: HOSTED, GOOGLE, or HOSTED_OR_GOOGLE
   * @throws AuthenticationException if authentication failed.
   */
    public void setUserCredentials(String username, String password, ClientLoginAccountType accountType) throws AuthenticationException {
        setUserCredentials(username, password, null, null, accountType);
    }

    /**
   * Sets the credentials of the user to authenticate requests to the server.
   * A CAPTCHA token and a CAPTCHA answer can also be optionally provided
   * to authenticate when the authentication server requires that a
   * CAPTCHA be answered.
   *
   * @param username the name of the user (an email id)
   * @param password the password of the user
   * @param captchaToken the CAPTCHA token issued by the server
   * @param captchaAnswer the answer to the respective CAPTCHA token
   * @throws AuthenticationException if authentication failed
   */
    public void setUserCredentials(String username, String password, String captchaToken, String captchaAnswer) throws AuthenticationException {
        setUserCredentials(username, password, captchaToken, captchaAnswer, ClientLoginAccountType.HOSTED_OR_GOOGLE);
    }

    /**
   * Sets the credentials of the user to authenticate requests to the server.
   * A CAPTCHA token and a CAPTCHA answer can also be optionally provided
   * to authenticate when the authentication server requires that a
   * CAPTCHA be answered.
   *
   * @param username the name of the user (an email id)
   * @param password the password of the user
   * @param captchaToken the CAPTCHA token issued by the server
   * @param captchaAnswer the answer to the respective CAPTCHA token
   * @param accountType the account type: HOSTED, GOOGLE, or HOSTED_OR_GOOGLE
   * @throws AuthenticationException if authentication failed
   */
    public void setUserCredentials(String username, String password, String captchaToken, String captchaAnswer, ClientLoginAccountType accountType) throws AuthenticationException {
        this.username = username;
        this.password = password;
        String token = getAuthToken(username, password, captchaToken, captchaAnswer, serviceName, applicationName, accountType);
        setUserToken(token);
    }

    /**
   * Sets the AuthToken that should be used to authenticate requests to the
   * server. This is useful if the caller has some other way of accessing the
   * AuthToken, versus calling getAuthToken with credentials to request the
   * AuthToken using ClientLogin.
   *
   * @param token the AuthToken in ascii form
   */
    public void setUserToken(String token) {
        setAuthToken(new UserToken(token));
    }

    /**
   * Sets the AuthSub token to be used to authenticate a user.
   *
   * @param token the AuthSub token retrieved from Google
   */
    public void setAuthSubToken(String token) {
        setAuthSubToken(token, null);
    }

    /**
   * Sets the AuthSub token to be used to authenticate a user.  The token
   * will be used in secure-mode, and the provided private key will be used
   * to sign all requests.
   *
   * @param token the AuthSub token retrieved from Google
   * @param key the private key to be used to sign all requests
   */
    public void setAuthSubToken(String token, PrivateKey key) {
        setAuthToken(new AuthSubToken(token, key));
    }

    /**
   * Sets the OAuth credentials used to generate the authorization header.
   * The following OAuth parameters are required:
   * <ul>
   * <li>oauth_consumer_key
   * <li>oauth_token
   * </ul>
   *
   * @param parameters the OAuth parameters to use to generated the header
   * @param signer the signing method to use for signing the header
   * @throws OAuthException
   */
    public void setOAuthCredentials(OAuthParameters parameters, OAuthSigner signer) throws OAuthException {
        parameters.assertOAuthConsumerKeyExists();
        setAuthToken(new OAuthToken(parameters, signer));
    }

    /**
   * Set the authentication token.
   *
   * @param authToken authentication token
   */
    public void setAuthToken(HttpAuthToken authToken) {
        this.authToken = authToken;
        if (tokenListener != null) {
            tokenListener.tokenChanged(authToken);
        }
    }

    public HttpAuthToken getAuthToken() {
        return authToken;
    }

    /**
   * Retrieves the authentication token for the provided set of credentials for
   * either a Google or a hosted domain.
   *
   * @param username the name of the user (an email address)
   * @param password the password of the user
   * @param captchaToken the CAPTCHA token if CAPTCHA is required (Optional)
   * @param captchaAnswer the respective answer of the CAPTCHA token (Optional)
   * @param serviceName the name of the service to which a token is required
   * @param applicationName the application requesting the token
   * @return the token
   * @throws AuthenticationException if authentication failed
   */
    public String getAuthToken(String username, String password, String captchaToken, String captchaAnswer, String serviceName, String applicationName) throws AuthenticationException {
        return getAuthToken(username, password, captchaToken, captchaAnswer, serviceName, applicationName, ClientLoginAccountType.HOSTED_OR_GOOGLE);
    }

    /**
   * Retrieves the authentication token for the provided set of credentials.
   *
   * @param username the name of the user (an email address)
   * @param password the password of the user
   * @param captchaToken the CAPTCHA token if CAPTCHA is required (Optional)
   * @param captchaAnswer the respective answer of the CAPTCHA token (Optional)
   * @param serviceName the name of the service to which a token is required
   * @param applicationName the application requesting the token
   * @param accountType the account type: HOSTED, GOOGLE, or HOSTED_OR_GOOGLE
   * @return the token
   * @throws AuthenticationException if authentication failed
   */
    public String getAuthToken(String username, String password, String captchaToken, String captchaAnswer, String serviceName, String applicationName, ClientLoginAccountType accountType) throws AuthenticationException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Email", username);
        params.put("Passwd", password);
        params.put("source", applicationName);
        params.put("service", serviceName);
        params.put("accountType", accountType.getValue());
        if (captchaToken != null) {
            params.put("logintoken", captchaToken);
        }
        if (captchaAnswer != null) {
            params.put("logincaptcha", captchaAnswer);
        }
        String postOutput;
        try {
            URL url = new URL(loginProtocol + "://" + domainName + GOOGLE_LOGIN_PATH);
            postOutput = makePostRequest(url, params);
        } catch (IOException e) {
            AuthenticationException ae = new AuthenticationException("Error connecting with login URI");
            ae.initCause(e);
            throw ae;
        }
        Map<String, String> tokenPairs = StringUtil.string2Map(postOutput.trim(), "\n", "=", true);
        String token = tokenPairs.get("Auth");
        if (token == null) {
            throw getAuthException(tokenPairs);
        }
        return token;
    }

    /**
   * Makes a HTTP POST request to the provided {@code url} given the
   * provided {@code parameters}.  It returns the output from the POST
   * handler as a String object.
   *
   * @param url the URL to post the request
   * @param parameters the parameters to post to the handler
   * @return the output from the handler
   * @throws IOException if an I/O exception occurs while creating, writing,
   *                     or reading the request
   */
    public static String makePostRequest(URL url, Map<String, String> parameters) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        StringBuilder content = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (!first) {
                content.append("&");
            }
            content.append(CharEscapers.uriEscaper().escape(parameter.getKey())).append("=");
            content.append(CharEscapers.uriEscaper().escape(parameter.getValue()));
            first = false;
        }
        OutputStream outputStream = null;
        try {
            outputStream = urlConnection.getOutputStream();
            outputStream.write(content.toString().getBytes("utf-8"));
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        InputStream inputStream = null;
        StringBuilder outputBuilder = new StringBuilder();
        try {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }
            String string;
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while (null != (string = reader.readLine())) {
                    outputBuilder.append(string).append('\n');
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return outputBuilder.toString();
    }

    /**
   * Returns the respective {@code AuthenticationException} given the return
   * values from the login URI handler.
   *
   * @param pairs name/value pairs returned as a result of a bad authentication
   * @return the respective {@code AuthenticationException} for the given error
   */
    private AuthenticationException getAuthException(Map<String, String> pairs) {
        String errorName = pairs.get("Error");
        if ("BadAuthentication".equals(errorName)) {
            return new InvalidCredentialsException("Invalid credentials");
        } else if ("AccountDeleted".equals(errorName)) {
            return new AccountDeletedException("Account deleted");
        } else if ("AccountDisabled".equals(errorName)) {
            return new AccountDisabledException("Account disabled");
        } else if ("NotVerified".equals(errorName)) {
            return new NotVerifiedException("Not verified");
        } else if ("TermsNotAgreed".equals(errorName)) {
            return new TermsNotAgreedException("Terms not agreed");
        } else if ("ServiceUnavailable".equals(errorName)) {
            return new ServiceUnavailableException("Service unavailable");
        } else if ("CaptchaRequired".equals(errorName)) {
            String captchaPath = pairs.get("CaptchaUrl");
            StringBuilder captchaUrl = new StringBuilder();
            captchaUrl.append(loginProtocol).append("://").append(domainName).append(GOOGLE_ACCOUNTS_PATH).append('/').append(captchaPath);
            return new CaptchaRequiredException("Captcha required", captchaUrl.toString(), pairs.get("CaptchaToken"));
        } else {
            return new AuthenticationException("Error authenticating " + "(check service name)");
        }
    }

    public void handleSessionExpiredException(SessionExpiredException sessionExpired) throws SessionExpiredException, AuthenticationException {
        if (username != null && password != null) {
            String token = getAuthToken(username, password, null, null, serviceName, applicationName);
            setUserToken(token);
        } else {
            throw sessionExpired;
        }
    }
}
