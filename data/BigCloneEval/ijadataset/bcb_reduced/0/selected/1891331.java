package org.dspace.app.xmlui.cocoon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.configuration.XMLUIConfiguration;
import org.dspace.app.xmlui.utils.AuthenticationUtil;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.core.ConfigurationManager;
import org.dspace.harvest.OAIHarvester;

/**
 * This is a wrapper servlet around the cocoon servlet that prefroms two functions, 1) it 
 * initializes DSpace / XML UI configuration parameters, and 2) it will preform inturrupted 
 * request resumption.
 * 
 * @author scott philips
 */
public class DSpaceCocoonServletFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(DSpaceCocoonServletFilter.class);

    private static final long serialVersionUID = 1L;

    /**
     * The DSpace config paramater, this is where the path to the DSpace
     * configuration file can be obtained
     */
    public static final String DSPACE_CONFIG_PARAMETER = "dspace-config";

    /**
     * This method holds code to be removed in the next version 
     * of the DSpace XMLUI, it is now managed by a Shared Context 
     * Listener inthe dspace-api project. 
     * 
     * It is deprecated, rather than removed to maintain backward 
     * compatibility for local DSpace 1.5.x customized overlays.
     * 
     * TODO: Remove in trunk
     *
     * @deprecated Use Servlet Context Listener provided 
     * in dspace-api (remove in > 1.5.x)
     * @throws ServletException
     */
    private void initDSpace(FilterConfig arg0) throws ServletException {
        try {
            String osName = System.getProperty("os.name");
            if (osName != null) {
                osName = osName.toLowerCase();
            }
            if (osName != null && osName.contains("windows")) {
                URL url = new URL("http://localhost/");
                URLConnection urlConn = url.openConnection();
                urlConn.setDefaultUseCaches(false);
            }
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        String dspaceConfig = null;
        dspaceConfig = arg0.getInitParameter(DSPACE_CONFIG_PARAMETER);
        if (dspaceConfig == null) {
            dspaceConfig = arg0.getServletContext().getInitParameter(DSPACE_CONFIG_PARAMETER);
        }
        if (dspaceConfig == null || "".equals(dspaceConfig)) {
            throw new ServletException("\n\nDSpace has failed to initialize. This has occurred because it was unable to determine \n" + "where the dspace.cfg file is located. The path to the configuration file should be stored \n" + "in a context variable, '" + DSPACE_CONFIG_PARAMETER + "', in either the local servlet or global contexts. \n" + "No context variable was found in either location.\n\n");
        }
        try {
            if (!ConfigurationManager.isConfigured()) {
                ConfigurationManager.loadConfig(dspaceConfig);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("\n\nDSpace has failed to initialize, during stage 2. Error while attempting to read the \n" + "DSpace configuration file (Path: '" + dspaceConfig + "'). \n" + "This has likely occurred because either the file does not exist, or it's permissions \n" + "are set incorrectly, or the path to the configuration file is incorrect. The path to \n" + "the DSpace configuration file is stored in a context variable, 'dspace-config', in \n" + "either the local servlet or global context.\n\n", e);
        }
    }

    /**
     * Before this servlet will become functional replace 
     */
    public void init(FilterConfig arg0) throws ServletException {
        this.initDSpace(arg0);
        String webappConfigPath = null;
        String installedConfigPath = null;
        try {
            webappConfigPath = arg0.getServletContext().getRealPath("/") + File.separator + "WEB-INF" + File.separator + "xmlui.xconf";
            installedConfigPath = ConfigurationManager.getProperty("dspace.dir") + File.separator + "config" + File.separator + "xmlui.xconf";
            XMLUIConfiguration.loadConfig(webappConfigPath, installedConfigPath);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("\n\nDSpace has failed to initialize, during stage 3. Error while attempting to read \n" + "the XML UI configuration file (Path: " + webappConfigPath + " or '" + installedConfigPath + "').\n" + "This has likely occurred because either the file does not exist, or it's permissions \n" + "are set incorrectly, or the path to the configuration file is incorrect. The XML UI \n" + "configuration file should be named \"xmlui.xconf\" and located inside the standard \n" + "DSpace configuration directory. \n\n", e);
        }
        if (ConfigurationManager.getBooleanProperty("oai", "harvester.autoStart")) {
            try {
                OAIHarvester.startNewScheduler();
            } catch (RuntimeException e) {
                LOG.error(e.getMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Before passing off a request to the cocoon servlet check to see if there is a request that 
     * should be resumed? If so replace the real request with a faked request and pass that off to 
     * cocoon.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain arg2) throws IOException, ServletException {
        HttpServletRequest realRequest = (HttpServletRequest) request;
        HttpServletResponse realResponse = (HttpServletResponse) response;
        try {
            realRequest = AuthenticationUtil.resumeRequest(realRequest);
            String requestUri = realRequest.getRequestURI();
            String contextPath = realRequest.getContextPath();
            String uri = requestUri.replace(contextPath, "");
            if (uri == null || uri.length() == 0) {
                String locationWithTrailingSlash = realRequest.getRequestURI() + "/";
                realResponse.reset();
                realResponse.sendRedirect(locationWithTrailingSlash);
            } else if ((ConfigurationManager.getBooleanProperty("xmlui.force.ssl")) && (realRequest.getSession().getAttribute("dspace.current.user.id") != null) && (!realRequest.isSecure())) {
                StringBuffer location = new StringBuffer("https://");
                location.append(ConfigurationManager.getProperty("dspace.hostname")).append(realRequest.getContextPath()).append(realRequest.getServletPath()).append(realRequest.getQueryString() == null ? "" : ("?" + realRequest.getQueryString()));
                realResponse.sendRedirect(location.toString());
            } else {
                arg2.doFilter(realRequest, realResponse);
            }
        } catch (RuntimeException e) {
            ContextUtil.abortContext(realRequest);
            LOG.error("Serious Runtime Error Occurred Processing Request!", e);
            throw e;
        } catch (Exception e) {
            ContextUtil.abortContext(realRequest);
            LOG.error("Serious Error Occurred Processing Request!", e);
        } finally {
            ContextUtil.completeContext(realRequest);
        }
    }

    public void destroy() {
    }
}
