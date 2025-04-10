package org.apache.http.impl.conn;

import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.localserver.ServerTestBase;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.util.EntityUtils;

public class TestSCMWithServer extends ServerTestBase {

    public TestSCMWithServer(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestSCMWithServer.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return new TestSuite(TestSCMWithServer.class);
    }

    /**
     * Helper to instantiate a <code>SingleClientConnManager</code>.
     *
     * @param params    the parameters, or
     *                  <code>null</code> to use defaults
     * @param schreg    the scheme registry, or
     *                  <code>null</code> to use defaults
     *
     * @return  a connection manager to test
     */
    public SingleClientConnManager createSCCM(HttpParams params, SchemeRegistry schreg) {
        if (params == null) params = defaultParams;
        if (schreg == null) schreg = supportedSchemes;
        return new SingleClientConnManager(params, schreg);
    }

    /**
     * Tests that SCM can still connect to the same host after
     * a connection was aborted.
     */
    public void testOpenAfterAbort() throws Exception {
        HttpParams mgrpar = defaultParams.copy();
        ConnManagerParams.setMaxTotalConnections(mgrpar, 1);
        SingleClientConnManager mgr = createSCCM(mgrpar, null);
        final HttpHost target = getServerHttp();
        final HttpRoute route = new HttpRoute(target, null, false);
        ManagedClientConnection conn = mgr.getConnection(route, null);
        assertTrue(conn instanceof AbstractClientConnAdapter);
        ((AbstractClientConnAdapter) conn).abortConnection();
        conn = mgr.getConnection(route, null);
        assertFalse("connection should have been closed", conn.isOpen());
        conn.open(route, httpContext, defaultParams);
        mgr.releaseConnection(conn, -1, null);
        mgr.shutdown();
    }

    /**
     * Tests releasing with time limits.
     */
    public void testReleaseConnectionWithTimeLimits() throws Exception {
        HttpParams mgrpar = defaultParams.copy();
        ConnManagerParams.setMaxTotalConnections(mgrpar, 1);
        SingleClientConnManager mgr = createSCCM(mgrpar, null);
        final HttpHost target = getServerHttp();
        final HttpRoute route = new HttpRoute(target, null, false);
        final int rsplen = 8;
        final String uri = "/random/" + rsplen;
        HttpRequest request = new BasicHttpRequest("GET", uri, HttpVersion.HTTP_1_1);
        ManagedClientConnection conn = mgr.getConnection(route, null);
        conn.open(route, httpContext, defaultParams);
        HttpResponse response = Helper.execute(request, conn, target, httpExecutor, httpProcessor, defaultParams, httpContext);
        assertEquals("wrong status in first response", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        byte[] data = EntityUtils.toByteArray(response.getEntity());
        assertEquals("wrong length of first response entity", rsplen, data.length);
        mgr.releaseConnection(conn, 100, TimeUnit.MILLISECONDS);
        conn = mgr.getConnection(route, null);
        assertFalse("connection should have been closed", conn.isOpen());
        conn.open(route, httpContext, defaultParams);
        httpContext.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        response = httpExecutor.execute(request, conn, httpContext);
        httpExecutor.postProcess(response, httpProcessor, httpContext);
        assertEquals("wrong status in second response", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        data = EntityUtils.toByteArray(response.getEntity());
        assertEquals("wrong length of second response entity", rsplen, data.length);
        conn.markReusable();
        mgr.releaseConnection(conn, 100, TimeUnit.MILLISECONDS);
        conn = mgr.getConnection(route, null);
        assertTrue("connection should have been open", conn.isOpen());
        httpContext.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        response = httpExecutor.execute(request, conn, httpContext);
        httpExecutor.postProcess(response, httpProcessor, httpContext);
        assertEquals("wrong status in third response", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        data = EntityUtils.toByteArray(response.getEntity());
        assertEquals("wrong length of third response entity", rsplen, data.length);
        conn.markReusable();
        mgr.releaseConnection(conn, 100, TimeUnit.MILLISECONDS);
        Thread.sleep(150);
        conn = mgr.getConnection(route, null);
        assertTrue("connection should have been closed", !conn.isOpen());
        conn.open(route, httpContext, defaultParams);
        httpContext.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        response = httpExecutor.execute(request, conn, httpContext);
        httpExecutor.postProcess(response, httpProcessor, httpContext);
        assertEquals("wrong status in third response", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        data = EntityUtils.toByteArray(response.getEntity());
        assertEquals("wrong length of fourth response entity", rsplen, data.length);
        mgr.shutdown();
    }

    public void testCloseExpiredConnections() throws Exception {
        HttpParams mgrpar = defaultParams.copy();
        ConnManagerParams.setMaxTotalConnections(mgrpar, 1);
        SingleClientConnManager mgr = createSCCM(mgrpar, null);
        final HttpHost target = getServerHttp();
        final HttpRoute route = new HttpRoute(target, null, false);
        ManagedClientConnection conn = mgr.getConnection(route, null);
        conn.open(route, httpContext, defaultParams);
        mgr.releaseConnection(conn, 100, TimeUnit.MILLISECONDS);
        mgr.closeExpiredConnections();
        conn = mgr.getConnection(route, null);
        assertTrue(conn.isOpen());
        mgr.releaseConnection(conn, 100, TimeUnit.MILLISECONDS);
        Thread.sleep(150);
        mgr.closeExpiredConnections();
        conn = mgr.getConnection(route, null);
        assertFalse(conn.isOpen());
        mgr.shutdown();
    }

    public void testAlreadyLeased() throws Exception {
        HttpParams mgrpar = defaultParams.copy();
        ConnManagerParams.setMaxTotalConnections(mgrpar, 1);
        SingleClientConnManager mgr = createSCCM(mgrpar, null);
        final HttpHost target = getServerHttp();
        final HttpRoute route = new HttpRoute(target, null, false);
        ManagedClientConnection conn = mgr.getConnection(route, null);
        mgr.releaseConnection(conn, 100, TimeUnit.MILLISECONDS);
        mgr.getConnection(route, null);
        try {
            mgr.getConnection(route, null);
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException ex) {
            mgr.shutdown();
        }
    }
}
