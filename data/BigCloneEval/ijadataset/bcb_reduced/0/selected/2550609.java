package org.apache.solr.client.solrj.embedded;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.log.Logger;

/**
 * Run solr using jetty
 * 
 * @version $Id: JettySolrRunner.java 820101 2009-09-29 21:53:48Z yonik $
 * @since solr 1.3
 */
public class JettySolrRunner {

    Server server;

    FilterHolder dispatchFilter;

    String context;

    public JettySolrRunner(String context, int port) {
        this.init(context, port);
    }

    public JettySolrRunner(String context, int port, String solrConfigFilename) {
        this.init(context, port);
        dispatchFilter.setInitParameter("solrconfig-filename", solrConfigFilename);
    }

    private void init(String context, int port) {
        this.context = context;
        server = new Server(port);
        server.setStopAtShutdown(true);
        Context root = new Context(server, context, Context.SESSIONS);
        root.addServlet(Servlet404.class, "/*");
        dispatchFilter = root.addFilter(SolrDispatchFilter.class, "*", Handler.REQUEST);
    }

    public void start() throws Exception {
        start(true);
    }

    public void start(boolean waitForSolr) throws Exception {
        if (!server.isRunning()) {
            server.start();
        }
        if (waitForSolr) waitForSolr(context);
    }

    public void stop() throws Exception {
        if (server.isRunning()) {
            server.stop();
            server.join();
        }
    }

    /** Waits until a ping query to the solr server succeeds,
   * retrying every 200 milliseconds up to 2 minutes.
   */
    public void waitForSolr(String context) throws Exception {
        int port = getLocalPort();
        URL url = new URL("http://localhost:" + port + context + "/select?q={!raw+f=junit_test_query}ping");
        Exception ex = null;
        for (int i = 0; i < 600; i++) {
            try {
                InputStream stream = url.openStream();
                stream.close();
            } catch (IOException e) {
                ex = e;
                Thread.sleep(200);
                continue;
            }
            return;
        }
        throw new RuntimeException("Jetty/Solr unresponsive", ex);
    }

    /**
   * Returns the Local Port of the first Connector found for the jetty Server.
   * @exception RuntimeException if there is no Connector
   */
    public int getLocalPort() {
        Connector[] conns = server.getConnectors();
        if (0 == conns.length) {
            throw new RuntimeException("Jetty Server has no Connectors");
        }
        return conns[0].getLocalPort();
    }

    /** 
   * This is a stupid hack to give jetty something to attach to
   */
    public static class Servlet404 extends HttpServlet {

        @Override
        public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
            res.sendError(404, "Can not find: " + req.getRequestURI());
        }
    }

    /**
   * A main class that starts jetty+solr 
   * This is useful for debugging
   */
    public static void main(String[] args) {
        try {
            JettySolrRunner jetty = new JettySolrRunner("/solr", 3456);
            jetty.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class NoLog implements Logger {

    private static boolean debug = System.getProperty("DEBUG", null) != null;

    private final String name;

    public NoLog() {
        this(null);
    }

    public NoLog(String name) {
        this.name = name == null ? "" : name;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public void setDebugEnabled(boolean enabled) {
        debug = enabled;
    }

    public void info(String msg, Object arg0, Object arg1) {
    }

    public void debug(String msg, Throwable th) {
    }

    public void debug(String msg, Object arg0, Object arg1) {
    }

    public void warn(String msg, Object arg0, Object arg1) {
    }

    public void warn(String msg, Throwable th) {
    }

    public Logger getLogger(String name) {
        if ((name == null && this.name == null) || (name != null && name.equals(this.name))) return this;
        return new NoLog(name);
    }

    @Override
    public String toString() {
        return "NOLOG[" + name + "]";
    }
}
