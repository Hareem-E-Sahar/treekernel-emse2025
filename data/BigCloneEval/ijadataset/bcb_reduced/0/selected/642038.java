package org.melati;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import org.melati.admin.AdminUtils;
import org.melati.poem.Database;
import org.melati.poem.NoAccessTokenPoemException;
import org.melati.poem.NotInSessionPoemException;
import org.melati.poem.Persistent;
import org.melati.poem.PoemThread;
import org.melati.poem.Table;
import org.melati.poem.User;
import org.melati.servlet.MelatiContext;
import org.melati.template.HTMLMarkupLanguage;
import org.melati.template.TemplateContext;
import org.melati.template.TemplateEngine;
import org.melati.template.WMLMarkupLanguage;
import org.melati.util.AcceptCharset;
import org.melati.util.DatabaseInitException;
import org.melati.util.HttpUtil;
import org.melati.util.HttpHeader;
import org.melati.util.MelatiBufferedWriter;
import org.melati.util.MelatiLocale;
import org.melati.util.MelatiSimpleWriter;
import org.melati.util.MelatiStringWriter;
import org.melati.util.MelatiWriter;
import org.melati.util.StringUtils;
import org.melati.util.servletcompat.HttpServletRequestCompat;

/**
 * This is the main entry point for using Melati.
 * <p>
 * You will need to create a MelatiConfig in order to construct a Melati.
 * <p>
 * If you are using servlets, you will want to construct a melati with 
 * a request and response object.  Otherwise, simply pass in a Writer.
 * <p>
 * A Melati exists once per request.
 * <p>
 * Melati is typically used with Servlets, POEM (Persistent Object Engine for
 * Melati) and a Template Engine
 *
 * @see org.melati.MelatiConfig
 * @see org.melati.servlet.ConfigServlet
 * @see org.melati.servlet.PoemServlet
 * @see org.melati.servlet.TemplateServlet
 */
public class Melati {

    private static String DEFAULT_ENCODING = "UTF8";

    private MelatiConfig config;

    private MelatiContext context;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private Database database = null;

    private Table table = null;

    private Persistent object = null;

    private TemplateEngine templateEngine;

    private TemplateContext templateContext;

    private boolean gotwriter = false;

    private boolean flushing = false;

    private boolean buffered = true;

    private MelatiWriter writer;

    private String encoding;

    /**
   * Construct a Melati for use with Servlets
   *
   * @param config - the MelatiConfig
   * @param request - the Servlet Request
   * @param response - the Servlet Response
   */
    public Melati(MelatiConfig config, HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.config = config;
    }

    /**
   * Construct a melati for use in 'stand alone' mode NB: you will not have
   * access to servlet related stuff (eg sessions)
   *
   * @param config - the MelatiConfig
   * @param writer - the Writer that all output is written to
   */
    public Melati(MelatiConfig config, MelatiWriter writer) {
        this.config = config;
        this.writer = writer;
    }

    /**
   * Get the servlet request object
   *
   * @return the Servlet Request
   */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
   * It is sometimes convenient to reconstruct the request object and
   * reset it.  for example, when returning from a log-in page
   *
   * @see org.melati.login.HttpSessionAccessHandler
   * @param request - new request object
   */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
   * Get the servlet response object
   *
   * @return - the Servlet Response
   */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
   * Set the MelatiContext for this request.  If the Context has a
   * LogicalDatabase set, this will be used to establish a connection
   * to the database.
   *
   * @param context - a MelatiContext
   * @throws DatabaseInitException - if the database fails to initialise for
   *                                 some reason
   * @see org.melati.LogicalDatabase
   * @see org.melati.servlet.PoemServlet
   */
    public void setContext(MelatiContext context) throws DatabaseInitException {
        this.context = context;
        if (context.getLogicalDatabase() != null) database = LogicalDatabase.getDatabase(context.getLogicalDatabase());
    }

    /**
   * Load a POEM Table and POEM Object for use in this request.  This is useful
   * as often Servlet requests are relevant for a single Table and/or Object.
   *
   * The Table name and Object id are set from the MelatiContext.
   *
   * @see org.melati.admin.Admin
   * @see org.melati.servlet.PoemServlet
   */
    public void loadTableAndObject() {
        if (context.getTable() != null && database != null) table = database.getTable(context.getTable());
        if (context.getTroid() != null && table != null) object = table.getObject(context.getTroid().intValue());
    }

    /**
   * Get the MelatiContext for this Request.
   *
   * @return - the MelatiContext for this Request
   */
    public MelatiContext getContext() {
        return context;
    }

    /**
   * Get the POEM Database for this Request.
   *
   * @return - the POEM Database for this Request
   * @see #setContext
   */
    public Database getDatabase() {
        return database;
    }

    /**
   * Get the POEM Table (if any) in use for this Request.
   *
   * @return the POEM Table for this Request
   * @see #loadTableAndObject
   */
    public Table getTable() {
        return table;
    }

    /**
   * Get the POEM Object (if any) in use for this Request
   *
   * @return the POEM Object for this Request
   * @see #loadTableAndObject
   */
    public Persistent getObject() {
        return object;
    }

    /**
   * Get the Method (if any) that has been set for this Request.
   *
   * @return the Method for this Request
   * @see org.melati.servlet.MelatiContext
   * @see org.melati.servlet.ConfigServlet#melatiContext
   * @see org.melati.servlet.PoemServlet#melatiContext
   */
    public String getMethod() {
        return context.getMethod();
    }

    /**
   * Set the template engine to be used for this Request.
   *
   * @param te - the template engine to be used
   * @see org.melati.servlet.TemplateServlet
   */
    public void setTemplateEngine(TemplateEngine te) {
        templateEngine = te;
    }

    /**
   * Get the template engine in use for this Request.
   *
   * @return - the template engine to be used
   */
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
   * Set the TemplateContext to be used for this Request.
   *
   * @param tc - the template context to be used
   * @see org.melati.servlet.TemplateServlet
   */
    public void setTemplateContext(TemplateContext tc) {
        templateContext = tc;
    }

    /**
   * Get the TemplateContext used for this Request.
   *
   * @return - the template context being used
   */
    public TemplateContext getTemplateContext() {
        return templateContext;
    }

    /**
   * Get the MelatiConfig associated with this Request.
   *
   * @return - the template context being used
   */
    public MelatiConfig getConfig() {
        return config;
    }

    /**
   * Get the PathInfo for this Request split into Parts by '/'.
   *
   * @return - an array of the parts found on the PathInfo
   */
    public String[] getPathInfoParts() {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 1) return new String[0];
        pathInfo = pathInfo.substring(1);
        return StringUtils.split(pathInfo, '/');
    }

    /**
   * Get the Session for this Request.
   *
   * @return - the Session for this Request
   */
    public HttpSession getSession() {
        return getRequest().getSession(true);
    }

    /**
   * Get the AdminUtils object for this Request.
   *
   * @return - the AdminUtils
   * @see org.melati.admin.Admin
   */
    public AdminUtils getAdminUtils() {
        return new AdminUtils(HttpServletRequestCompat.getContextPath(getRequest()), getRequest().getServletPath(), config.getStaticURL() + "/admin", context.getLogicalDatabase());
    }

    /**
   * Get the URL for the Logout Page.
   *
   * @return - the URL for the Logout Page
   * @see org.melati.login.Logout
   */
    public String getLogoutURL() {
        StringBuffer url = new StringBuffer();
        HttpUtil.appendRelativeZoneURL(url, getRequest());
        url.append('/');
        url.append(MelatiConfig.logoutPageServletClassName());
        url.append('/');
        url.append(context.getLogicalDatabase());
        return url.toString();
    }

    /**
   * Get the URL for this Servlet Zone.
   *
   * @return - the URL for this Servlet Zone
   * @see org.melati.util.HttpUtil#zoneURL
   */
    public String getZoneURL() {
        return HttpUtil.zoneURL(getRequest());
    }

    /**
   * Get the URL for this request.
   *
   * @return - the URL for this request
   * @see org.melati.util.HttpUtil#servletURL
   */
    public String getServletURL() {
        return HttpUtil.servletURL(getRequest());
    }

    /**
   * Get the URL for the JavascriptLibrary.
   *
   * @return - the URL for the JavascriptLibrary
   * @see org.melati.MelatiConfig#getJavascriptLibraryURL
   */
    public String getJavascriptLibraryURL() {
        return config.getJavascriptLibraryURL();
    }

    /**
   * Returns a MelatiLocale object based on the Accept-Language header
   * of this request.
   *
   * If we are using Melati outside of a servlet context then the 
   * configured locale is returned. 
   * 
   * @return a MelatiLocale object
   */
    public MelatiLocale getMelatiLocale() {
        HttpServletRequest r = getRequest();
        String acceptLanguage = null;
        if (r != null) acceptLanguage = r.getHeader("Accept-Language");
        if (acceptLanguage == null) return config.getLocale();
        return config.getLocale(acceptLanguage);
    }

    /**
   * Suggest a response character encoding and if necessary choose a
   * request encoding.
   * <p>
   * If the request encoding is provided then we choose a response
   * encoding to meet our preferences on the assumption that the
   * client will also indicate next time what its request
   * encoding is.
   * The result can optionally be set in code or possibly in
   * templates using {@link #setResponseContentType(String)}.
   * <p>
   * Otherwise we tread carefully. We assume that the encoding is
   * the first supported encoding of the client's preferences for
   * responses, as indicated by Accept-Charsets, and avoid giving
   * it any reason to change.
   * <p>
   * Actually, the server preference is a bit dodgy for
   * the response because if it does persuade the client to
   * change encodings and future requests include query strings
   * that we are providing now then we may end up with the
   * query strings being automatically decoded using the wrong
   * encoding by request.getParameter(). But by the time we
   * end up with values in such parameters the client and
   * server will probably have settled on particular encodings.
   */
    public void establishCharsets() throws ServletException {
        AcceptCharset ac;
        String acs = request.getHeader("Accept-Charset");
        assert acs == null || acs.trim().length() > 0 : "Accept-Charset should not be empty but can be absent";
        if (acs != null && acs.trim().length() == 0) {
            acs = null;
        }
        try {
            ac = new AcceptCharset(acs, config.getPreferredCharsets());
        } catch (HttpHeader.HttpHeaderException e) {
            ServletException t = new ServletException("An error was apparently detected in your HTTP request header " + " worthy of response code: " + HttpServletResponse.SC_BAD_REQUEST + ": \"" + acs + '"');
            t.initCause(e);
            throw t;
        }
        if (request.getCharacterEncoding() == null) {
            responseCharset = ac.clientChoice();
            try {
                request.setCharacterEncoding(responseCharset);
            } catch (UnsupportedEncodingException e) {
                assert false : "This has already been checked by AcceptCharset";
            }
        } else {
            responseCharset = ac.serverChoice();
        }
    }

    /**
   * Suggested character encoding for use in responses.
   */
    protected String responseCharset = null;

    /**
   * Sets the content type for use in the response.
   * <p>
   * Use of this method is optional and does not work in standalone
   * mode.
   * <p>
   * If the type starts with "text/" and does not contain a semicolon
   * and a good response character set has been established based on
   * the request Accept-Charset header and server preferences, then this
   * and semicolon separator are automatically appended to the type.
   * I am guessing that this makes sense.
   * <p>
   * Whether this function should be called at all may depend on 
   * the application and templates.
   * <p>
   * It should be called before any calls to {@link #getEncoding()}
   * and before writing the response.
   *
   * @see #establishCharsets()
   * @todo Test this out in applications then change the admin templates.
   */
    public void setResponseContentType(String type) {
        if (responseCharset != null && type.startsWith("text/") && type.indexOf(";") == -1) {
            type += "; charset=" + responseCharset;
        }
        response.setContentType(type);
    }

    /**
   * Get a HTMLMarkupLanguage for use when generating HTML in templates.
   *
   * @return - a HTMLMarkupLanguage
   * @see org.melati.template.TempletLoader
   * @see org.melati.util.MelatiLocale
   */
    public HTMLMarkupLanguage getHTMLMarkupLanguage() {
        return new HTMLMarkupLanguage(this, config.getTempletLoader(), getMelatiLocale());
    }

    /**
   * Get a WMLMarkupLanguage for use when generating WML in templates.
   *
   * @return - a WMLMarkupLanguage
   * @see org.melati.template.TempletLoader
   * @see org.melati.util.MelatiLocale
   */
    public WMLMarkupLanguage getWMLMarkupLanguage() {
        return new WMLMarkupLanguage(this, config.getTempletLoader(), getMelatiLocale());
    }

    /**
   * The URL of the servlet request associated with this <TT>Melati</TT>, with
   * a modified or added form parameter setting (query string component).
   *
   * @param field   The name of the form parameter
   * @param value   The new value for the parameter (unencoded)
   * @return        The request URL with <TT>field=value</TT>.  If there is
   *                already a binding for <TT>field</TT> in the query string
   *                it is replaced, not duplicated.  If there is no query
   *                string, one is added.
   * @see org.melati.MelatiUtil
   */
    public String sameURLWith(String field, String value) {
        return MelatiUtil.sameURLWith(getRequest(), field, value);
    }

    /**
   * The URL of the servlet request associated with this <TT>Melati</TT>, with
   * a modified or added form flag setting (query string component).
   *
   * @param field   The name of the form parameter
   * @return        The request URL with <TT>field=1</TT>.  If there is
   *                already a binding for <TT>field</TT> in the query string
   *                it is replaced, not duplicated.  If there is no query
   *                string, one is added.
   * @see org.melati.MelatiUtil
   */
    public String sameURLWith(String field) {
        return sameURLWith(field, "1");
    }

    /**
   * The URL of the servlet request associated with this <TT>Melati</TT>.
   *
   * @return a string
   */
    public String getSameURL() {
        String qs = getRequest().getQueryString();
        return getRequest().getRequestURI() + (qs == null ? "" : '?' + qs);
    }

    /**
   * Turn off buffering of the output stream.
   *
   * By default, melati will buffer the output, which will not be written
   * to the output stream until you call melati.write();
   *
   * Buffering allows us to catch AccessPoemExceptions and redirect the user
   * to the login page.  This could not be done if any bytes have been written
   * to the client
   *
   * @see org.melati.test.FlushingServletTest
   * @throws IOException if a writer has already been selected
   */
    public void setBufferingOff() throws IOException {
        if (gotwriter) throw new IOException("You have already requested a Writer, " + "and can't change it's properties now");
        buffered = false;
    }

    /**
   * Turn on flushing of the output stream.
   *
   * @throws IOException if there is a problem with the writer
   */
    public void setFlushingOn() throws IOException {
        if (gotwriter) throw new IOException("You have already requested a Writer, " + "and can't change it's properties now");
        flushing = true;
    }

    /**
   * Have we asked to access the Writer for this request?
   * <p>
   * If you have not accessed the Writer, it is reasonable to assume that
   * nothing has been written to the output stream.
   *
   * @return - have we sucessfully called getWriter()?
   */
    public boolean gotWriter() {
        return gotwriter;
    }

    /**
   * Return the encoding that is used for URL encoded query
   * strings.
   * <p>
   * The requirement here is that parameters can be encoded in
   * query strings included in URLs in the body of responses.
   * User interaction may result in subsequent requests with such
   * a URL. The HTML spec. describes encoding of non-alphanumeric
   * ASCII using % and ASCII hex codes and, in the case of forms.
   * says the client may use the response encoding by default.
   * Sun's javadoc for <code>java.net.URLEncoder</code>
   * recommends UTF-8 but the default is the Java platform
   * encoding. Most significantly perhaps,
   * org.mortbay.http.HttpRequest uses the request encoding.
   * We should check that this is correct in the servlet specs.
   * <p>
   * So we assume that the servlet runner may dictate the
   * encoding that will work for multi-national characters in
   * field values encoded in URL's (but not necessarily forms).
   * <p>
   * If the request encoding is used then we have to try and
   * predict it. It will be the same for a session unless a client
   * has some reason to change it. E.g. if we respond to a request
   * in a different encoding and the client is influenced.
   * (See {@link #establishCharsets()}.
   * But that is only a problem if the first or second request
   * in a session includes field values encoded in the URL and
   * user options include manually entering the same in a form
   * or changing their browser configuration.
   * Or we can change the server configuration.
   * <p>
   * It would be better if we had control over what encoding
   * the servlet runner used to decode parameters.
   * Perhaps one day we will.
   * <p>
   * So this method implements the current policy and currently
   * returns the current request encoding.
   * It assumes {@link #establishCharsets()} has been called to
   * set the request encoding if necessary.
   *
   * @see #establishCharsets()
   * see also org.melati.admin.Admin#selection(TemplateContext, Melati)
   */
    public String getURLQueryEncoding() {
        return request.getCharacterEncoding();
    }

    /**
   * Convenience method to URL encode a URL query string.
   * <p>
   * These is here because it uses knownledge of this object and
   * other methods.
   *
   * see also org.melati.admin.Admin#selection(TemplateContext, Melati)
   */
    public String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, getURLQueryEncoding());
        } catch (UnsupportedEncodingException e) {
            assert false : "The URL query encoding is supported";
            return string;
        }
    }

    /**
   * Return the encoding that is used for writing.
   * <p>
   * This should always return an encoding and it should be the same
   * for duration of use of an instance.
   *
   * @return Response encoding or a default in stand alone mode
   * @see #setResponseContentType(String)
   */
    public String getEncoding() {
        if (encoding == null) encoding = response == null ? DEFAULT_ENCODING : response.getCharacterEncoding();
        return encoding;
    }

    /**
   * Get a Writer for this request.
   *
   * If you have not accessed the Writer, it is reasonable to assume that
   * nothing has been written to the output stream.
   *
   * @return - one of:
   *
   * - the Writer that was used to construct the Melati
   * - the Writer associated with the Servlet Response
   * - a buffered Writer
   * - a ThrowingPrintWriter
   * @throws IOException if there is a problem with the writer
   */
    public MelatiWriter getWriter() throws IOException {
        if (writer == null) writer = createWriter();
        if (writer != null) gotwriter = true;
        return writer;
    }

    /**
   * Get a StringWriter.
   *
   * @return - one of:
   *
   * - a MelaitStringWriter from the template engine
   * - a new MelatiStringWriter
   *
   */
    public MelatiWriter getStringWriter() {
        if (templateEngine == null) {
            return new MelatiStringWriter();
        }
        return templateEngine.getStringWriter();
    }

    private MelatiWriter createWriter() throws IOException {
        MelatiWriter writerL = null;
        if (response != null) {
            if (templateEngine != null) {
                writerL = templateEngine.getServletWriter(response, buffered);
            } else {
                if (buffered) {
                    writerL = new MelatiBufferedWriter(response.getWriter());
                } else {
                    writerL = new MelatiSimpleWriter(response.getWriter());
                }
            }
        }
        if (flushing) writerL.setFlushingOn();
        return writerL;
    }

    /**
   * Write the buffered output to the Writer
   * we also need to stop the flusher if it has started.
   *
   * @throws IOException if there is a problem with the writer
   */
    public void write() throws IOException {
        if (gotwriter) writer.close();
    }

    /**
   * Get a PassbackVariableExceptionHandler for the TemplateEngine.
   * This allows an Exception to be handled inline during Template expansion
   * for example, if you would like to render AccessPoemExceptions to a
   * String to be displayed on the page that is returned to the client.
   *
   * @return - PassbackVariableExceptionHandler specific to the
   * template engine
   *
   * @see org.melati.template.MarkupLanguage#rendered(java.lang.Throwable e)
   * @see org.melati.poem.TailoredQuery
   */
    public Object getPassbackVariableExceptionHandler() {
        return templateEngine.getPassbackVariableExceptionHandler();
    }

    /**
   * Set the <code>VariableExceptionHandler</code> to the 
   * passed in parameter.
   * 
   * @param veh a <code>VariableExceptionHandler</code>.
   */
    public void setVariableExceptionHandler(Object veh) {
        templateContext.setVariableExceptionHandler(veh);
    }

    /**
   * Get a User for this request (if they are logged in)
   *
   * @return - a User for this request
   */
    public User getUser() {
        try {
            return (User) PoemThread.accessToken();
        } catch (NotInSessionPoemException e) {
            return null;
        } catch (NoAccessTokenPoemException e) {
            return null;
        } catch (ClassCastException e) {
            return null;
        }
    }
}
