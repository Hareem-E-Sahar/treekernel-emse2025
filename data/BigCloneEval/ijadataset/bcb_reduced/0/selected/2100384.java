package org.apache.http.client.protocol;

import java.io.IOException;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SM;
import org.apache.http.cookie.SetCookie2;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.localserver.BasicServerTestBase;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * Cookie2 support tests.
 *
 */
public class TestCookie2Support extends BasicServerTestBase {

    public TestCookie2Support(final String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestCookie2Support.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return new TestSuite(TestCookie2Support.class);
    }

    @Override
    protected void setUp() throws Exception {
        localServer = new LocalTestServer(null, null);
        localServer.registerDefaultHandlers();
        localServer.start();
    }

    private static class CookieVer0Service implements HttpRequestHandler {

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            ProtocolVersion httpversion = request.getRequestLine().getProtocolVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new BasicHeader("Set-Cookie", "name1=value1; path=/test"));
            StringEntity entity = new StringEntity("whatever");
            response.setEntity(entity);
        }
    }

    public void testCookieVersionSupportHeader1() throws Exception {
        this.localServer.register("*", new CookieVer0Service());
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet("/test/");
        HttpResponse response1 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e1 = response1.getEntity();
        if (e1 != null) {
            e1.consumeContent();
        }
        List<Cookie> cookies = cookieStore.getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.size());
        HttpResponse response2 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e2 = response2.getEntity();
        if (e2 != null) {
            e2.consumeContent();
        }
        HttpRequest reqWrapper = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        Header cookiesupport = reqWrapper.getFirstHeader("Cookie2");
        assertNotNull(cookiesupport);
        assertEquals("$Version=1", cookiesupport.getValue());
    }

    private static class CookieVer1Service implements HttpRequestHandler {

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            ProtocolVersion httpversion = request.getRequestLine().getProtocolVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new BasicHeader("Set-Cookie", "name1=value1; Path=\"/test\"; Version=1"));
            response.addHeader(new BasicHeader("Set-Cookie2", "name2=value2; Path=\"/test\"; Version=1"));
            StringEntity entity = new StringEntity("whatever");
            response.setEntity(entity);
        }
    }

    public void testCookieVersionSupportHeader2() throws Exception {
        this.localServer.register("*", new CookieVer1Service());
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet("/test/");
        HttpResponse response1 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e1 = response1.getEntity();
        if (e1 != null) {
            e1.consumeContent();
        }
        List<Cookie> cookies = cookieStore.getCookies();
        assertNotNull(cookies);
        assertEquals(2, cookies.size());
        HttpResponse response2 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e2 = response2.getEntity();
        if (e2 != null) {
            e2.consumeContent();
        }
        HttpRequest reqWrapper = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        Header cookiesupport = reqWrapper.getFirstHeader(SM.COOKIE2);
        assertNotNull(cookiesupport);
        assertEquals("$Version=1", cookiesupport.getValue());
    }

    private static class CookieVer2Service implements HttpRequestHandler {

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            ProtocolVersion httpversion = request.getRequestLine().getProtocolVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new BasicHeader("Set-Cookie2", "name2=value2; Path=\"/test\"; Version=2"));
            StringEntity entity = new StringEntity("whatever");
            response.setEntity(entity);
        }
    }

    public void testCookieVersionSupportHeader3() throws Exception {
        this.localServer.register("*", new CookieVer2Service());
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet("/test/");
        HttpResponse response1 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e1 = response1.getEntity();
        if (e1 != null) {
            e1.consumeContent();
        }
        List<Cookie> cookies = cookieStore.getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.size());
        HttpResponse response2 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e2 = response2.getEntity();
        if (e2 != null) {
            e2.consumeContent();
        }
        HttpRequest reqWrapper = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        Header cookiesupport = reqWrapper.getFirstHeader("Cookie2");
        assertNotNull(cookiesupport);
        assertEquals("$Version=1", cookiesupport.getValue());
    }

    private static class SetCookieVersionMixService implements HttpRequestHandler {

        public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            ProtocolVersion httpversion = request.getRequestLine().getProtocolVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new BasicHeader("Set-Cookie", "name=wrong; Path=/test"));
            response.addHeader(new BasicHeader("Set-Cookie2", "name=right; Path=\"/test\"; Version=1"));
            StringEntity entity = new StringEntity("whatever");
            response.setEntity(entity);
        }
    }

    public void testSetCookieVersionMix() throws Exception {
        this.localServer.register("*", new SetCookieVersionMixService());
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet("/test/");
        HttpResponse response1 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e1 = response1.getEntity();
        if (e1 != null) {
            e1.consumeContent();
        }
        List<Cookie> cookies = cookieStore.getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.size());
        assertEquals("right", cookies.get(0).getValue());
        assertTrue(cookies.get(0) instanceof SetCookie2);
    }
}
