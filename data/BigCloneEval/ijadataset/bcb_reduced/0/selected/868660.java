package edu.uga.galileo.slash.servlet;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.uga.galileo.slash.logging.LogListener;
import edu.uga.galileo.slash.logging.Logger;
import edu.uga.galileo.slash.model.Configuration;
import edu.uga.galileo.slash.model.StatisticsParser;
import edu.uga.galileo.slash.services.GeolocationLookupService;
import edu.uga.galileo.slash.services.InstitutionLookupService;
import edu.uga.galileo.slash.services.ReverseDNSLookupService;
import edu.uga.galileo.slash.utilities.StackTracer;
import edu.uga.galileo.slash.utilities.StringUtils;

/**
 * The entry point for all application requests, this servlet performs all
 * startup initialization in the system.
 * 
 * @author <a href="mailto:mdurant@uga.edu">Mark Durant</a>
 * @version 1.0
 */
public class Controller extends HttpServlet implements LogListener {

    /**
	 * Default UID for serializable class.
	 * 
	 * TODO: finalize it with a generated one when finished?
	 */
    private static final long serialVersionUID = 1L;

    /**
	 * This will get set to <code>false</code> if the logger tells this class
	 * that a <code>FATAL</code> error occurred in the system, and it will
	 * block all requests.
	 */
    private boolean acceptRequests = true;

    /**
	 * The institution lookup service to use in resolving IP addresses to
	 * cities, states, and countries.
	 */
    private static InstitutionLookupService institutionLookupService;

    /**
	 * The reverse DNS lookup service to use in resolving IP addresses to top
	 * level domains.
	 */
    private static ReverseDNSLookupService reverseDNSLookupService;

    /**
	 * The geolocation lookup service to use in resolving IP addresses to city,
	 * region, country information.
	 */
    private static GeolocationLookupService geolocationLookupService;

    /**
	 * The statistics parser.
	 */
    private static StatisticsParser parser;

    /**
	 * Perform any necessary startup functions.
	 */
    public void init() {
        Logger.addLogListener(this);
        ServletContext context = getServletContext();
        Configuration.setServletContext(context);
        Enumeration initParamNames = context.getInitParameterNames();
        String name, value;
        while (initParamNames.hasMoreElements()) {
            name = (String) initParamNames.nextElement();
            value = context.getInitParameter(name);
            Configuration.addConfigValue(name, value);
        }
        Logger.setLogLevel(Configuration.getInt("debugLevel"));
        String listenerList;
        if ((listenerList = Configuration.getString("logListeners")) != null) {
            String[] listeners = listenerList.split(",");
            for (String listener : listeners) {
                try {
                    Class c = Class.forName("edu.uga.galileo.slash.logging." + listener.trim());
                    Constructor constructor = c.getConstructor((Class[]) null);
                    Logger.addLogListener((LogListener) constructor.newInstance((Object[]) null));
                } catch (Exception e) {
                    Logger.error("Couldn't instantiate LogListener '" + listener + "'", e);
                }
            }
        }
        try {
            institutionLookupService = InstitutionLookupService.getInstance();
            Logger.info("InstitutionLookupService INITIALIZED");
            reverseDNSLookupService = ReverseDNSLookupService.getInstance();
            Logger.info("ReverseDNSLookupService INITIALIZED");
            geolocationLookupService = GeolocationLookupService.getInstance();
            Logger.info("GeolocationLookupService INITIALIZED");
            parser = StatisticsParser.getInstance();
        } catch (IOException e) {
            Logger.fatal("Couldn't get institution lookup service.", e);
        }
        Logger.info("Controller servlet INITIALIZED");
    }

    /**
	 * @see javax.servlet.Servlet#destroy()
	 */
    public void destroy() {
        parser.destroy();
    }

    /**
	 * Pass any GET requests to the POST handler.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    /**
	 * Handle both GET- and POST-type requests. Sanitize the request parameters,
	 * set the {@link edu.uga.galileo.slash.bo.Command} object, check for logins
	 * going into the admin area, and pass things off to the next servlet.
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
    @SuppressWarnings("finally")
    protected void doPost(HttpServletRequest req, HttpServletResponse response) {
        if (!acceptRequests) {
            try {
                req.setAttribute("errorMessage", "A fatal error has occurred in the system, " + "and it has shut itself down.  A system administrator has " + "been contacted, so please try again later.");
                req.getRequestDispatcher(response.encodeURL("/Error.jsp")).forward(req, response);
            } catch (ServletException e) {
                Logger.warn("Couldn't forward request for URI " + req.getRequestURI() + " to the error page", e);
            } catch (IOException e) {
                Logger.warn("Couldn't forward request for URI " + req.getRequestURI() + " to the error page", e);
            } finally {
                return;
            }
        }
        HttpRequestWithModifiableParameters request = sanitizeRequest(req);
        addPrivacyPolicy(response);
        String uri = req.getRequestURI();
        String forwardTo = uri.substring(1);
        forwardTo = forwardTo.substring(forwardTo.indexOf("/do/") + 4);
        String queryString;
        if (((queryString = request.getQueryString()) != null) && (queryString.trim().length() > 0)) {
            queryString = '?' + queryString;
        } else {
            queryString = "";
        }
        request.setAttribute("originalRequest", request.getRequestURI() + queryString);
        Logger.debug("dispatching to /" + forwardTo + queryString);
        RequestDispatcher rd = request.getRequestDispatcher(response.encodeURL("/" + forwardTo + queryString));
        try {
            rd.forward(request, response);
            return;
        } catch (ServletException e) {
            Logger.warn("Couldn't forward request for URI " + request.getRequestURI() + " to /" + forwardTo + queryString, e);
        } catch (IOException e) {
            Logger.warn("Couldn't forward request for URI " + request.getRequestURI() + " to /" + forwardTo + queryString, e);
        }
        try {
            request.getRequestDispatcher(response.encodeURL("/Error.jsp")).forward(request, response);
        } catch (ServletException e) {
            Logger.warn("Couldn't forward request for URI " + request.getRequestURI() + " to the error page", e);
        } catch (IOException e) {
            Logger.warn("Couldn't forward request for URI " + request.getRequestURI() + " to the error page", e);
        }
    }

    /**
	 * Add a privacy policy to the outgoing response.
	 * 
	 * @param response
	 *            The
	 */
    private void addPrivacyPolicy(HttpServletResponse response) {
        String p3pPolicy;
        if (((p3pPolicy = Configuration.getString("p3pPrivacyPolicy")) != null) && (p3pPolicy.trim().length() > 0)) {
            Logger.debug("Setting privacy policy to '" + p3pPolicy + "' in response header");
            response.addHeader("P3P", "CP=\"" + p3pPolicy + "\"");
        }
    }

    /**
	 * Sanitize the request parameters.
	 * 
	 * @param request
	 *            The <code>HttpServletRequest</code> associated with this
	 *            request.
	 * @return An
	 *         {@link edu.uga.galileo.slash.servlet.HttpRequestWithModifiableParameters}
	 *         object with sanitized request parameters.
	 */
    private HttpRequestWithModifiableParameters sanitizeRequest(HttpServletRequest req) {
        HttpRequestWithModifiableParameters request = new HttpRequestWithModifiableParameters(req);
        String paramName;
        String[] paramValues;
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
            paramName = (String) e.nextElement();
            paramValues = request.getParameterValues(paramName);
            String[] newValues = new String[paramValues.length];
            String newValue;
            for (int m = 0; m < paramValues.length; m++) {
                newValue = StringUtils.doSpecialCharReplacements(paramValues[m]);
                newValues[m] = newValue;
            }
            request.setParameter(paramName, newValues);
        }
        return request;
    }

    /**
	 * Check to see if a request came through this class. This is done by all
	 * subclasses of {@link edu.uga.galileo.slash.servlet.ControlledHttpServlet}
	 * before handling the request.
	 * 
	 * @param request
	 *            The <code>HttpServletRequest</code>.
	 * @param response
	 *            The <code>HttpServletResponse</code>.
	 * @return <code>true</code> if the class making the check has this
	 *         class's <code>doPost</code> method in its stack trace.
	 */
    protected static boolean doControllerCheck(HttpServletRequest request, HttpServletResponse response) {
        if (StackTracer.whoCalled().indexOf("Controller.doPost") == -1) {
            try {
                request.getRequestDispatcher(response.encodeURL(Configuration.getString("errorPage"))).forward(request, response);
            } catch (ServletException e) {
                Logger.warn("Couldn't forward on controller check failure", e);
            } catch (IOException e) {
                Logger.warn("Couldn't forward on controller check failure", e);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
	 * @see edu.uga.galileo.slash.logging.LogListener#handleMessage(java.lang.String,
	 *      int, java.lang.Throwable)
	 */
    public void handleMessage(String msg, int level, Throwable e) {
        if (level == Logger.FATAL) {
            acceptRequests = false;
            if (parser != null) {
                parser.destroy();
            }
        }
    }

    /**
	 * @see edu.uga.galileo.slash.logging.LogListener#allowDeferredLogNotification()
	 */
    public boolean allowDeferredLogNotification() {
        return true;
    }

    /**
	 * Get the reverse DNS lookup service object.
	 * 
	 * @return The reverse DNS lookup service object.
	 */
    public static ReverseDNSLookupService getReverseDNSLookupService() {
        return reverseDNSLookupService;
    }

    /**
	 * Get the institution lookup service object.
	 * 
	 * @return The institution lookup service object.
	 */
    public static InstitutionLookupService getInstitutionLookupService() {
        return institutionLookupService;
    }

    /**
	 * Get the geolocation lookup service object.
	 * 
	 * @return The geolocation lookup service object.
	 */
    public static GeolocationLookupService getGeolocationLookupService() {
        return geolocationLookupService;
    }
}
