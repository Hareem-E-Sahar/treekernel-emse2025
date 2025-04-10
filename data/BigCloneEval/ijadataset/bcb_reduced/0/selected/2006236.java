package org.apache.http.examples.client;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * An example of HttpClient can be customized to authenticate 
 * preemptively using DIGEST scheme.
 * <b/>
 * Generally, preemptive authentication can be considered less
 * secure than a response to an authentication challenge
 * and therefore discouraged.
 * <b/>
 * This code is NOT officially supported! Use at your risk.
 */
public class ClientPreemptiveDigestAuthentication {

    public static void main(String[] args) throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getCredentialsProvider().setCredentials(new AuthScope("localhost", 80), new UsernamePasswordCredentials("username", "password"));
        BasicHttpContext localcontext = new BasicHttpContext();
        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("realm", "some realm");
        digestAuth.overrideParamter("nonce", "whatever");
        localcontext.setAttribute("preemptive-auth", digestAuth);
        httpclient.addRequestInterceptor(new PreemptiveAuth(), 0);
        httpclient.addResponseInterceptor(new PersistentDigest());
        HttpHost targetHost = new HttpHost("localhost", 80, "http");
        HttpGet httpget = new HttpGet("/");
        System.out.println("executing request: " + httpget.getRequestLine());
        System.out.println("to target: " + targetHost);
        for (int i = 0; i < 3; i++) {
            HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                entity.consumeContent();
            }
        }
    }

    static class PreemptiveAuth implements HttpRequestInterceptor {

        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
        }
    }

    static class PersistentDigest implements HttpResponseInterceptor {

        public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
            if (authState != null) {
                AuthScheme authScheme = authState.getAuthScheme();
                if (authScheme instanceof DigestScheme) {
                    context.setAttribute("preemptive-auth", authScheme);
                }
            }
        }
    }
}
