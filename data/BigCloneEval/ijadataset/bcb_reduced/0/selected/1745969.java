package org.apache.shindig.server.endtoend;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.auth.AuthenticationServletFilter;
import org.apache.shindig.common.PropertiesModule;
import org.apache.shindig.common.servlet.GuiceServletContextListener;
import org.apache.shindig.gadgets.DefaultGuiceModule;
import org.apache.shindig.gadgets.oauth.OAuthModule;
import org.apache.shindig.gadgets.servlet.ConcatProxyServlet;
import org.apache.shindig.gadgets.servlet.GadgetRenderingServlet;
import org.apache.shindig.gadgets.servlet.JsServlet;
import org.apache.shindig.gadgets.servlet.MakeRequestServlet;
import org.apache.shindig.protocol.DataServiceServlet;
import org.apache.shindig.protocol.JsonRpcServlet;
import org.apache.shindig.social.sample.SampleModule;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.resource.Resource;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 * Suite for running the end-to-end tests. The suite is responsible for starting up and shutting
 * down the server.
 */
public class EndToEndServer {

    private static final int JETTY_PORT = 9003;

    private static final String GADGET_BASE = "/gadgets/ifr";

    private static final String REST_BASE = "/social/rest/*";

    private static final String JSON_RPC_BASE = "/social/rpc/*";

    private static final String CONCAT_BASE = "/gadgets/concat";

    private static final String JS_BASE = "/gadgets/js/*";

    private static final String MAKE_REQUEST_BASE = "/gadgets/makeRequest";

    public static final String SERVER_URL = "http://localhost:" + JETTY_PORT;

    public static final String GADGET_BASEURL = SERVER_URL + GADGET_BASE;

    private final Server server;

    /** Fake error code for data service servlet request */
    protected int errorCode;

    /** Fake error message for data service servlet request */
    protected String errorMessage;

    public EndToEndServer() throws Exception {
        server = createServer(JETTY_PORT);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void clearDataServiceError() {
        errorCode = 0;
    }

    public void setDataServiceError(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
   * Starts the server for end-to-end tests.
   */
    private Server createServer(int port) throws Exception {
        Server newServer = new Server(port);
        ResourceHandler resources = new ResourceHandler();
        URL resource = EndToEndTest.class.getResource("/endtoend");
        resources.setBaseResource(Resource.newResource(resource));
        newServer.addHandler(resources);
        Context context = new Context(newServer, "/", Context.SESSIONS);
        context.addEventListener(new GuiceServletContextListener());
        Map<String, String> initParams = Maps.newHashMap();
        String modules = Joiner.on(":").join(SampleModule.class.getName(), DefaultGuiceModule.class.getName(), PropertiesModule.class.getName(), OAuthModule.class.getName());
        initParams.put(GuiceServletContextListener.MODULES_ATTRIBUTE, modules);
        context.setInitParams(initParams);
        ServletHolder gadgetServletHolder = new ServletHolder(new GadgetRenderingServlet());
        context.addServlet(gadgetServletHolder, GADGET_BASE);
        ServletHolder restServletHolder = new ServletHolder(new ForceErrorServlet(new DataServiceServlet()));
        restServletHolder.setInitParameter("handlers", "org.apache.shindig.social.handlers");
        context.addServlet(restServletHolder, REST_BASE);
        context.addFilter(AuthenticationServletFilter.class, REST_BASE, 0);
        ServletHolder rpcServletHolder = new ServletHolder(new ForceErrorServlet(new JsonRpcServlet()));
        rpcServletHolder.setInitParameter("handlers", "org.apache.shindig.social.handlers");
        context.addServlet(rpcServletHolder, JSON_RPC_BASE);
        context.addFilter(AuthenticationServletFilter.class, JSON_RPC_BASE, 0);
        ServletHolder concatHolder = new ServletHolder(new ConcatProxyServlet());
        context.addServlet(concatHolder, CONCAT_BASE);
        ServletHolder jsHolder = new ServletHolder(new JsServlet());
        context.addServlet(jsHolder, JS_BASE);
        ServletHolder makeRequestHolder = new ServletHolder(new MakeRequestServlet());
        context.addServlet(makeRequestHolder, MAKE_REQUEST_BASE);
        ServletHolder echoHolder = new ServletHolder(new EchoServlet());
        context.addServlet(echoHolder, "/echo");
        return newServer;
    }

    private class ForceErrorServlet implements Servlet {

        private final Servlet proxiedServlet;

        public ForceErrorServlet(Servlet proxiedServlet) {
            this.proxiedServlet = proxiedServlet;
        }

        public void init(ServletConfig servletConfig) throws ServletException {
            proxiedServlet.init(servletConfig);
        }

        public ServletConfig getServletConfig() {
            return proxiedServlet.getServletConfig();
        }

        public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
            if (errorCode > 0) {
                ((HttpServletResponse) servletResponse).sendError(errorCode, errorMessage);
            } else {
                servletRequest.setCharacterEncoding("UTF-8");
                proxiedServlet.service(servletRequest, servletResponse);
            }
        }

        public String getServletInfo() {
            return proxiedServlet.getServletInfo();
        }

        public void destroy() {
            proxiedServlet.destroy();
        }
    }

    private static class EchoServlet extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            req.setCharacterEncoding("UTF-8");
            resp.setContentType(req.getContentType());
            IOUtils.copy(req.getReader(), resp.getWriter());
        }
    }
}
