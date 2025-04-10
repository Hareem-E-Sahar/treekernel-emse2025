package org.jecars.servlets;

import com.google.gdata.data.DateTime;
import com.google.gdata.util.ParseException;
import com.google.gdata.util.common.base.StringUtil;
import java.io.*;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.*;
import javax.servlet.http.*;
import nl.msd.jdots.JD_Taglist;
import org.jecars.*;
import org.jecars.apps.CARS_AccountsApp;
import org.jecars.jaas.CARS_Digest;
import org.jecars.support.BASE64Decoder;
import org.jecars.support.CARS_Buffer;
import org.jecars.tools.CARS_DefaultToolInterface;

/**
 * JeCARS_RESTServlet
 *
 * @version $Id: JeCARS_RESTServlet.java,v 1.6 2009/06/05 14:43:05 weertj Exp $
 */
public class JeCARS_RESTServlet extends HttpServlet {

    protected static final Logger gLog = Logger.getLogger(JeCARS_RESTServlet.class.getPackage().getName());

    /** The maximum number of concurrent threads running before replying with a SERVICE UNAVAILABLE
   */
    protected static final int MAXTHREADCOUNT = 255;

    private static final AtomicInteger THREADCOUNT = new AtomicInteger(0);

    private static final CARS_Factory CARSFACTORY = new CARS_Factory();

    public static enum AUTH_TYPE {

        BASIC, DIGEST
    }

    ;

    public static final String WWW_AUTH = "WWW-Authenticate";

    private static String gHOSTNAME = "jecars.org";

    private static String gOPAQUE = "JeCARS";

    private static String gNONCE_PREFIX = "PRE_";

    private static String gNONCE_POSTFIX = "_POST";

    private static String gREALM = null;

    private static String gBASIC_REALM = null;

    private static String gDIGEST_REALM = null;

    private static MessageDigest gMD;

    private static final Object MD_LOCK = new Object();

    private static String gCurrentFullContext = null;

    protected static volatile AUTH_TYPE gCURRENT_AUTH = AUTH_TYPE.BASIC;

    protected static volatile EnumSet<AUTH_TYPE> gALLOWED_AUTH = EnumSet.of(AUTH_TYPE.DIGEST, AUTH_TYPE.BASIC);

    private static final JeCARS_WebDAVServlet WEBDAV = new JeCARS_WebDAVServlet();

    private ICARS_LongPolling mLongPolling = new CARS_DefaultLongPolling();

    {
        try {
            gMD = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ne) {
            gLog.log(Level.SEVERE, ne.getMessage(), ne);
        }
    }

    /** getCurrentFullContext
   * 
   * @return
   */
    public static String getCurrentFullContext() {
        return gCurrentFullContext;
    }

    /** setHostname
   * 
   * @param pHostname
   */
    public static void setHostname(final String pHostname) {
        gHOSTNAME = pHostname;
        setRealm(gHOSTNAME);
        gBASIC_REALM = null;
        gDIGEST_REALM = null;
        return;
    }

    /** setRealm
   *
   * @param pRealm
   */
    public static void setRealm(final String pRealm) {
        gREALM = pRealm;
        return;
    }

    /** getRealm
   * 
   * @return
   */
    public static String getRealm() {
        return gREALM;
    }

    /** getCurrentRealm
   *
   * @return
   */
    public static String getCurrentRealm() {
        if (gCURRENT_AUTH.equals(AUTH_TYPE.BASIC)) {
            if (gBASIC_REALM == null) {
                gBASIC_REALM = "BASIC realm=\"" + gREALM + "\"";
            }
            return gBASIC_REALM;
        } else {
            synchronized (MD_LOCK) {
                if (gDIGEST_REALM == null) {
                    gDIGEST_REALM = "Digest realm=\"" + gREALM + "\",qop=\"auth\",opaque=\"" + StringUtil.bytesToHexString(gMD.digest(gOPAQUE.getBytes())) + "\",nonce=\"";
                }
                final String nonce = gNONCE_PREFIX + System.nanoTime() + gNONCE_POSTFIX;
                return gDIGEST_REALM + StringUtil.bytesToHexString(nonce.getBytes()) + "\"";
            }
        }
    }

    /** setLongPolling
   * 
   * @param pLP 
   */
    public void setLongPolling(final ICARS_LongPolling pLP) {
        mLongPolling = pLP;
        return;
    }

    /** init
   * 
   * @throws javax.servlet.ServletException
   */
    @Override
    public void init() throws ServletException {
        gLog.log(Level.INFO, "Using " + CARS_Main.PRODUCTNAME + " version: " + CARS_Main.VERSION);
        setHostname(gHOSTNAME);
        setRealm(gHOSTNAME);
        final String cotf = getInitParameter("CIRCLE_OF_TRUST_FILE");
        if (cotf == null) {
            gLog.log(Level.INFO, "Circle of trust file not set");
        } else {
            final File cotfF = new File(cotf);
            gLog.log(Level.INFO, "Circle of trust file: " + cotfF.getAbsolutePath());
            try {
                CARS_AccountsApp.setCircleOfTrustFile(cotfF);
            } catch (IOException ie) {
                gLog.log(Level.WARNING, ie.getMessage(), ie);
            }
        }
        final String elfEnable = getInitParameter("ENABLE_FILE_LOG");
        final String elf = getInitParameter("EVENT_LOG_FILE");
        if ((elf == null) || (elfEnable == null)) {
            gLog.log(Level.WARNING, "Event log file Init parameters not set, event log disabled");
        } else {
            final File elfF = new File(elf);
            gLog.log(Level.INFO, "Event log file: " + elfF.getAbsolutePath());
            try {
                if ((elfF.exists()) || (elfF.createNewFile())) {
                    gLog.log(Level.INFO, "Event log file enabled: " + elfEnable);
                    final boolean enabled = Boolean.parseBoolean(elfEnable);
                    CARS_EventManager.setEventLogFile(elfF);
                    CARS_EventManager.setEnableFileLog(enabled);
                } else {
                    gLog.log(Level.WARNING, "Cannot write event log file: " + elfF.getAbsolutePath());
                }
            } catch (IOException ie) {
                gLog.log(Level.WARNING, ie.getMessage(), ie);
            }
        }
        try {
            CARS_Factory.setServletContext(getServletContext());
            final String realPath = getServletContext().getRealPath("WEB-INF/cars_repository.xml");
            if (realPath == null) {
                gLog.log(Level.INFO, "XML config file = WEB-INF/cars_repository.xml path not found");
            } else {
                final File configXML = new File(realPath);
                if (configXML.exists()) {
                    CARS_Factory.gJecarsProperties.setProperty("jecars.ConfigFile", configXML.getAbsolutePath());
                } else {
                    gLog.log(Level.INFO, "XML config file = " + configXML.getAbsolutePath() + " not found");
                }
            }
            String initVar = getInitParameter("JECARS_CONFIGFILE");
            gLog.log(Level.INFO, "INIT-PARAM: JECARS_CONFIGFILE = " + initVar);
            if (initVar != null) {
                CARS_Factory.gJecarsProperties.setProperty("jecars.ConfigFile", initVar);
            }
            initVar = getInitParameter("JECARS_REPHOME");
            gLog.log(Level.INFO, "INIT-PARAM: JECARS_REPHOME = " + initVar);
            if (initVar != null) {
                CARS_Factory.gJecarsProperties.setProperty("jecars.RepHome", initVar);
            }
            initVar = getInitParameter("JECARS_REPLOGHOME");
            gLog.log(Level.INFO, "INIT-PARAM: JECARS_REPLOGHOME = " + initVar);
            if (initVar != null) {
                CARS_Factory.gJecarsProperties.setProperty("jecars.RepLogHome", initVar);
            }
            initVar = getInitParameter("JECARS_NAMESPACES");
            gLog.log(Level.INFO, "INIT-PARAM: JECARS_NAMESPACES = " + initVar);
            if (initVar != null) {
                CARS_Factory.gJecarsProperties.setProperty("jecars.Namespaces", initVar);
            }
            initVar = getInitParameter("JECARS_CNDFILES");
            gLog.log(Level.INFO, "INIT-PARAM: JECARS_CNDFILES = " + initVar);
            if (initVar != null) {
                CARS_Factory.gJecarsProperties.setProperty("jecars.CNDFiles", initVar);
            }
            gLog.log(Level.INFO, "Trying to read /WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME);
            final InputStream is = getServletContext().getResourceAsStream("/WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME);
            if (is == null) {
                gLog.log(Level.INFO, "/WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME + " not found");
            } else {
                gLog.log(Level.INFO, "Reading /WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME);
                CARS_Factory.gJecarsProperties.load(is);
                is.close();
            }
            CARSFACTORY.init(null, false);
            WEBDAV.init(getServletContext(), this);
        } catch (Exception e) {
            gLog.log(Level.SEVERE, null, e);
        }
        return;
    }

    /** getResourcePathPrefix
   * 
   * @return
   */
    protected String getResourcePathPrefix() {
        return null;
    }

    /** destroy
   */
    @Override
    public void destroy() {
        gLog.log(Level.INFO, "Servlet destroy request ");
        CARS_DefaultToolInterface.destroy();
        CARS_Factory.shutdown();
        return;
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet JeCARS_RESTServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet JeCARS_RESTServlet at " + request.getPathInfo() + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
        return;
    }

    /** createActionContext
     *
     * @param pRequest
     * @param pResponse
     * @return
     * @throws java.lang.Exception
     */
    protected CARS_ActionContext createActionContext(final HttpServletRequest pRequest, final HttpServletResponse pResponse) throws IOException, ParseException, NoSuchAlgorithmException {
        CARS_ActionContext ac = null;
        String auth = pRequest.getHeader("Authorization");
        if (auth != null) {
            String username = null, password = null;
            if (auth.toUpperCase().startsWith("BASIC ") && gALLOWED_AUTH.contains(AUTH_TYPE.BASIC)) {
                final String userpassEncoded = auth.substring(6);
                final String encoding = new String(BASE64Decoder.decodeBuffer(userpassEncoded));
                final int in = encoding.indexOf(':');
                username = encoding.substring(0, in);
                password = encoding.substring(in + 1);
                ac = CARS_ActionContext.createActionContext(username, password.toCharArray());
            } else if (auth.toUpperCase().startsWith("DIGEST ") && gALLOWED_AUTH.contains(AUTH_TYPE.DIGEST)) {
                final HashMap<String, String> authMap = CARS_Digest.parseAuthenticationString(auth);
                final MessageDigest md = MessageDigest.getInstance("MD5");
                username = authMap.get("username");
                final String uri = authMap.get("uri");
                final byte[] md5 = md.digest((pRequest.getMethod() + ":" + uri).getBytes());
                final String ha2 = StringUtil.bytesToHexString(md5);
                password = authMap.get("response") + "\n:" + authMap.get("nonce") + ":" + authMap.get("nc") + ":" + authMap.get("cnonce") + ":" + authMap.get("qop") + ":" + ha2;
                ac = CARS_ActionContext.createActionContext(username, (AUTH_TYPE.DIGEST.toString() + "|" + password).toCharArray());
            } else if (auth.toUpperCase().startsWith("GOOGLELOGIN AUTH=")) {
                final String key = auth.substring(17);
                ac = CARS_ActionContext.createActionContext(key);
            } else {
                pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                pResponse.sendError(pResponse.SC_UNAUTHORIZED);
            }
        }
        if (auth == null) {
            final String q = pRequest.getQueryString();
            if (q != null) {
                final int ix = q.indexOf("GOOGLELOGIN_AUTH=");
                if (ix != -1) {
                    auth = q.substring(ix + "GOOGLELOGIN_AUTH=".length());
                    if (auth.indexOf('&') != -1) {
                        auth = auth.substring(0, auth.indexOf('&'));
                    }
                    ac = CARS_ActionContext.createActionContext(auth);
                }
            }
        }
        final String pathInfo = pRequest.getPathInfo();
        if ((auth == null) && (pathInfo != null)) {
            if ("/accounts/login".equals(pathInfo)) {
                pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                pResponse.sendError(pResponse.SC_UNAUTHORIZED);
            } else {
                if (pathInfo.startsWith("/accounts/")) {
                    ac = CARS_ActionContext.createActionContext("anonymous", "anonymous".toCharArray());
                } else {
                    pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                    pResponse.sendError(pResponse.SC_UNAUTHORIZED);
                }
            }
        } else {
            if (pathInfo == null) {
                return null;
            }
        }
        if (ac != null) {
            ac.setIfModifiedSince(pRequest.getHeader("If-Modified-Since"));
            ac.setIfNoneMatch(pRequest.getHeader("If-None-Match"));
            ac.setRemoteHost(pRequest.getRemoteHost());
            ac.setUserAgent(pRequest.getHeader("User-Agent"));
            ac.setReferer(pRequest.getHeader("Referer"));
            ac.setServerPort(pRequest.getServerPort());
            final String range = pRequest.getHeader("Range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    String brange = range.substring("bytes=".length());
                    int ix = brange.indexOf('-');
                    if (ix != -1) {
                        String end = brange.substring(ix + 1);
                        if ("".equals(end)) {
                            ac.setRange(Long.parseLong(brange.substring(0, ix)), Long.MAX_VALUE);
                        } else {
                            ac.setRange(Long.parseLong(brange.substring(0, ix)), Long.parseLong(brange.substring(ix + 1)));
                        }
                    }
                }
            }
        }
        return ac;
    }

    /** resultToOutput
     *
     * @param pResult
     * @param pResponse
     * @param pSendData
     */
    private void resultToOutput(final Object pResult, final HttpServletResponse pResponse, final boolean pSendData) {
        PrintWriter outp = null;
        OutputStream os = null;
        try {
            if (pResult instanceof StringBuilder) {
                pResponse.setContentLength(((StringBuilder) pResult).length());
                if (pSendData) {
                    outp = pResponse.getWriter();
                    outp.print(((StringBuilder) pResult).toString());
                }
            } else if (pResult instanceof CARS_Buffer) {
                pResponse.setContentLength(((CARS_Buffer) pResult).length());
                if (pSendData) {
                    outp = pResponse.getWriter();
                    outp.print(((CARS_Buffer) pResult).toString());
                }
            } else if (pResult instanceof InputStream) {
                pResponse.setHeader("Accept-Ranges", "bytes");
                if (pSendData) {
                    os = pResponse.getOutputStream();
                    final BufferedInputStream bis = new BufferedInputStream((InputStream) pResult);
                    final BufferedOutputStream bos = new BufferedOutputStream(os);
                    try {
                        final byte[] buff = new byte[200000];
                        int bytesRead;
                        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                            bos.write(buff, 0, bytesRead);
                            bos.flush();
                        }
                    } finally {
                        if (bis != null) bis.close();
                        if (bos != null) bos.close();
                    }
                }
            }
        } catch (IOException ioe) {
            if ((ioe.getCause() != null) && (ioe.getCause().getMessage() != null)) {
                if ((ioe.getCause().getMessage().indexOf("Connection reset by peer") == -1) || (ioe.getCause().getMessage().indexOf("Software caused connection abort") == -1)) {
                }
            } else {
                gLog.log(Level.WARNING, null, ioe);
            }
        } catch (Exception e) {
            gLog.log(Level.WARNING, null, e);
        } finally {
            if (outp != null) {
                outp.flush();
                outp.close();
            }
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException ioe) {
                    if ((ioe.getCause() != null) && (ioe.getCause().getMessage() != null)) {
                        if ((ioe.getCause().getMessage().indexOf("Connection reset by peer") == -1) || (ioe.getCause().getMessage().indexOf("Software caused connection abort") == -1)) {
                        }
                    } else {
                        gLog.log(Level.WARNING, null, ioe);
                    }
                }
            }
        }
        return;
    }

    /** _processHttpHeaders
     *
     * @param pAC
     * @param pResponse
     * @throws RepositoryException
     */
    private void _processHttpHeaders(final CARS_ActionContext pAC, final HttpServletResponse pResponse, final JD_Taglist pResponseHeaders) throws RepositoryException {
        final Map<String, ArrayList<String>> headers = pAC.getHttpHeaders();
        if (headers != null) {
            for (String key : headers.keySet()) {
                final List<String> values = headers.get(key);
                if (values != null) {
                    for (String value : values) {
                        pResponse.setHeader(key, value);
                    }
                }
            }
        }
        pResponse.setHeader("Jecars-Version", CARS_Main.VERSION_ID);
        if (pAC.canBeCachedResult()) {
            pResponse.setHeader("ETag", pAC.getETag());
        }
        if (pAC.getContentsLength() != -1) pResponse.setContentLength((int) pAC.getContentsLength());
        if (pResponseHeaders != null) {
            final Iterator it = pResponseHeaders.getIterator();
            while (it.hasNext()) {
                final String key = (String) it.next();
                pResponse.setHeader(key, (String) pResponseHeaders.getData(key));
            }
        }
        return;
    }

    private void debugPrintRequestHeader(HttpServletRequest pRequest) {
        System.out.println(" --- HttpServletRequest --- " + pRequest.getPathInfo() + " / " + pRequest.getQueryString());
        Enumeration e = pRequest.getHeaderNames();
        String h;
        while (e.hasMoreElements()) {
            h = (String) e.nextElement();
            if (h != null) {
                System.out.println(h + "\t= " + pRequest.getHeader(h));
            }
        }
        return;
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(final HttpServletRequest pRequest, final HttpServletResponse pResponse) throws ServletException, IOException {
        final String q = pRequest.getQueryString();
        if (q != null) {
            if (q.indexOf("X-HTTP-Method-Override=") != -1) {
                if (q.indexOf("X-HTTP-Method-Override=DELETE") != -1) {
                    doDelete(pRequest, pResponse);
                    return;
                }
                if (q.indexOf("X-HTTP-Method-Override=PUT") != -1) {
                    doPut(pRequest, pResponse);
                    return;
                }
                if (q.indexOf("X-HTTP-Method-Override=POST") != -1) {
                    doPost(pRequest, pResponse);
                    return;
                }
                if (q.indexOf("X-HTTP-Method-Override=HEAD") != -1) {
                    doHead(pRequest, pResponse);
                    return;
                }
            }
        }
        try {
            if (THREADCOUNT.get() > MAXTHREADCOUNT) {
                pResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                gLog.log(Level.WARNING, "SC_SERVICE_UNAVAILABLE", (Exception) null);
                return;
            }
            THREADCOUNT.incrementAndGet();
            final CARS_ActionContext ac = createActionContext(pRequest, pResponse);
            if (ac != null) {
                try {
                    final String baseurl = pRequest.getScheme() + "://" + pRequest.getServerName() + ':' + pRequest.getServerPort();
                    ac.setContextPath(pRequest.getContextPath() + pRequest.getServletPath());
                    ac.setPathInfo(new String(pRequest.getPathInfo().getBytes("ISO-8859-1"), "UTF-8"));
                    ac.setQueryString(q);
                    ac.setParameterMap(pRequest.getParameterMap());
                    ac.setBaseURL(baseurl);
                    gCurrentFullContext = ac.getBaseContextURL();
                    CARSFACTORY.performGetAction(ac, null);
                    boolean getResult = true;
                    if ("true".equals(CARS_Factory.getJecarsProperties().getProperty(CARS_Factory.JECARSPROP_ETAG, "false"))) {
                        final long lastMod = ac.getLastModified();
                        if (ac.getIfNoneMatch() != null) {
                            if (ac.getIfNoneMatch().compareTo(ac.getETag()) == 0) {
                                pResponse.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                                pResponse.setHeader("ETag", ac.getETag());
                                getResult = false;
                            }
                        }
                        if ((ac.canBeCachedResult()) && (lastMod != 0) && (ac.getIfModifiedSince() != null)) {
                            if (ac.getIfModifiedSince().getValue() / 1000 >= (lastMod / 1000)) {
                                pResponse.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                                pResponse.setHeader("ETag", ac.getETag());
                                getResult = false;
                            }
                        }
                    }
                    if (getResult) {
                        final JD_Taglist responseHeaders = JD_Taglist.allocTaglist();
                        final Object result = ac.getResult(responseHeaders);
                        pResponse.setContentType(ac.getContentType());
                        pResponse.setStatus(ac.getErrorCode());
                        _processHttpHeaders(ac, pResponse, responseHeaders);
                        resultToOutput(result, pResponse, true);
                        responseHeaders.clear();
                        pResponse.setStatus(ac.getErrorCode());
                    }
                } catch (AccessDeniedException ade) {
                    pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                    pResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } catch (CredentialExpiredException cee) {
                    pResponse.setHeader("Location", cee.getMessage());
                    pResponse.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);
                } finally {
                    ac.destroy();
                }
            }
        } catch (CARS_LongPollRequestException le) {
            try {
                mLongPolling.endHandleLongPolling(le.getNodePath(), pRequest, pResponse, null);
                System.out.println("Long poll request pending");
            } catch (RepositoryException re) {
                gLog.log(Level.WARNING, null, re);
                pResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage());
            }
        } catch (Exception e) {
            gLog.log(Level.WARNING, null, e);
            pResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            THREADCOUNT.decrementAndGet();
        }
        return;
    }

    /** doHead
     * 
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doHead(final HttpServletRequest pRequest, final HttpServletResponse pResponse) throws ServletException, IOException {
        try {
            if (THREADCOUNT.get() > MAXTHREADCOUNT) {
                pResponse.sendError(pResponse.SC_SERVICE_UNAVAILABLE);
                gLog.log(Level.WARNING, "SC_SERVICE_UNAVAILABLE", (Exception) null);
                return;
            }
            THREADCOUNT.incrementAndGet();
            final CARS_ActionContext ac = createActionContext(pRequest, pResponse);
            if (ac != null) {
                try {
                    ac.setContextPath(pRequest.getContextPath() + pRequest.getServletPath());
                    ac.setPathInfo(new String(pRequest.getPathInfo().getBytes("ISO-8859-1"), "UTF-8"));
                    ac.setQueryString(pRequest.getQueryString());
                    ac.setBaseURL(pRequest.getScheme() + "://" + pRequest.getServerName() + ":" + pRequest.getServerPort());
                    CARSFACTORY.performHeadAction(ac);
                    pResponse.setContentType(ac.getContentType());
                    pResponse.setStatus(ac.getErrorCode());
                    if (ac.getLastModified() != 0) {
                        final DateTime dtime = new DateTime(ac.getLastModified());
                        pResponse.setHeader("Last-Modified", dtime.toStringRfc822());
                    }
                    _processHttpHeaders(ac, pResponse, null);
                    pResponse.setStatus(ac.getErrorCode());
                } catch (AccessDeniedException ade) {
                    pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                    pResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } catch (CredentialExpiredException cee) {
                    pResponse.setHeader("Location", cee.getMessage());
                    pResponse.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);
                } finally {
                    ac.destroy();
                }
            }
        } catch (Exception e) {
            gLog.log(Level.WARNING, null, e);
        } finally {
            THREADCOUNT.decrementAndGet();
        }
        return;
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
        try {
            if (THREADCOUNT.get() > MAXTHREADCOUNT) {
                pResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            THREADCOUNT.incrementAndGet();
            final CARS_ActionContext ac = createActionContext(pRequest, pResponse);
            ServletInputStream sis = null;
            if (ac != null) {
                try {
                    ac.setContextPath(pRequest.getContextPath() + pRequest.getServletPath());
                    String pathInfo = new String(pRequest.getPathInfo().getBytes("ISO-8859-1"), "UTF-8");
                    if (pRequest.getHeader("Slug") != null) {
                        if (pathInfo.endsWith("/")) {
                            pathInfo += pRequest.getHeader("Slug");
                        } else {
                            pathInfo += "/" + pRequest.getHeader("Slug");
                        }
                    }
                    ac.setPathInfo(pathInfo);
                    ac.setQueryString(pRequest.getQueryString());
                    if ((pRequest.getContentType() == null) || pRequest.getContentType().equals("application/x-www-form-urlencoded")) {
                        ac.setParameterMap(pRequest.getParameterMap());
                    }
                    sis = pRequest.getInputStream();
                    ac.setBaseURL(pRequest.getScheme() + "://" + pRequest.getServerName() + ":" + pRequest.getServerPort());
                    ac.setBodyStream(sis, pRequest.getContentType());
                    CARSFACTORY.performPostAction(ac);
                    pResponse.setContentType(ac.getContentType());
                    final String createdNodePath = ac.getCreatedNodePath();
                    if (createdNodePath != null) {
                        pResponse.setHeader("Location", ac.getBaseContextURL() + createdNodePath);
                    }
                    pResponse.setStatus(ac.getErrorCode());
                    final JD_Taglist responseHeaders = JD_Taglist.allocTaglist();
                    resultToOutput(ac.getResult(responseHeaders), pResponse, true);
                    _processHttpHeaders(ac, pResponse, responseHeaders);
                    responseHeaders.clear();
                    pResponse.setStatus(ac.getErrorCode());
                } catch (AccessDeniedException ade) {
                    pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                    pResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } finally {
                    if (sis != null) sis.close();
                    ac.destroy();
                }
            }
        } catch (Exception e) {
            gLog.log(Level.WARNING, null, e);
        } finally {
            THREADCOUNT.decrementAndGet();
        }
        return;
    }

    /** doPut
     *
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPut(final HttpServletRequest pRequest, final HttpServletResponse pResponse) throws ServletException, IOException {
        try {
            if (THREADCOUNT.get() > MAXTHREADCOUNT) {
                pResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            THREADCOUNT.incrementAndGet();
            final CARS_ActionContext ac = createActionContext(pRequest, pResponse);
            ServletInputStream sis = null;
            if (ac != null) {
                try {
                    ac.setContextPath(pRequest.getContextPath() + pRequest.getServletPath());
                    ac.setPathInfo(new String(pRequest.getPathInfo().getBytes("ISO-8859-1"), "UTF-8"));
                    ac.setQueryString(pRequest.getQueryString());
                    sis = pRequest.getInputStream();
                    ac.setBaseURL(pRequest.getScheme() + "://" + pRequest.getServerName() + ":" + pRequest.getServerPort());
                    ac.setBodyStream(sis, pRequest.getContentType());
                    CARSFACTORY.performPutAction(ac, null);
                    pResponse.setContentType(ac.getContentType());
                    pResponse.setStatus(ac.getErrorCode());
                    final JD_Taglist responseHeaders = JD_Taglist.allocTaglist();
                    resultToOutput(ac.getResult(responseHeaders), pResponse, true);
                    _processHttpHeaders(ac, pResponse, responseHeaders);
                    responseHeaders.clear();
                    pResponse.setStatus(ac.getErrorCode());
                } catch (AccessDeniedException ade) {
                    pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                    pResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } finally {
                    ac.destroy();
                    if (sis != null) {
                        sis.close();
                    }
                }
            }
        } catch (Exception e) {
            gLog.log(Level.WARNING, null, e);
        } finally {
            THREADCOUNT.decrementAndGet();
        }
        return;
    }

    /** doDelete
     *
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doDelete(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
        try {
            if (THREADCOUNT.get() > MAXTHREADCOUNT) {
                pResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            THREADCOUNT.incrementAndGet();
            final CARS_ActionContext ac = createActionContext(pRequest, pResponse);
            if (ac != null) {
                final PrintWriter outp = pResponse.getWriter();
                try {
                    ac.setContextPath(pRequest.getContextPath() + pRequest.getServletPath());
                    ac.setPathInfo(new String(pRequest.getPathInfo().getBytes("ISO-8859-1"), "UTF-8"));
                    ac.setQueryString(pRequest.getQueryString());
                    ac.setBaseURL(pRequest.getScheme() + "://" + pRequest.getServerName() + ":" + pRequest.getServerPort());
                    CARSFACTORY.performDeleteAction(ac);
                    pResponse.setContentType(ac.getContentType());
                    pResponse.setStatus(ac.getErrorCode());
                    final JD_Taglist responseHeaders = JD_Taglist.allocTaglist();
                    resultToOutput(ac.getResult(responseHeaders), pResponse, true);
                    _processHttpHeaders(ac, pResponse, responseHeaders);
                    responseHeaders.clear();
                    pResponse.setStatus(ac.getErrorCode());
                } catch (AccessDeniedException ade) {
                    pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                    pResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } finally {
                    outp.flush();
                    outp.close();
                    ac.destroy();
                }
            }
        } catch (Exception e) {
            gLog.log(Level.WARNING, null, e);
        } finally {
            THREADCOUNT.decrementAndGet();
        }
        return;
    }

    /** service
     *
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service(final HttpServletRequest pRequest, final HttpServletResponse pResponse) throws ServletException, IOException {
        final String pathInfo = pRequest.getPathInfo();
        if ((pathInfo != null) && (pathInfo.startsWith("/webdav"))) {
            try {
                if (THREADCOUNT.get() > MAXTHREADCOUNT) {
                    pResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    gLog.log(Level.WARNING, "SC_SERVICE_UNAVAILABLE", (Exception) null);
                    return;
                }
                THREADCOUNT.incrementAndGet();
                final CARS_ActionContext ac = createActionContext(pRequest, pResponse);
                if (ac != null) {
                    try {
                        ac.setContextPath(pRequest.getContextPath() + pRequest.getServletPath());
                        final String path = new String(pathInfo.getBytes("ISO-8859-1"), "UTF-8");
                        ac.setPathInfo(path);
                        ac.setQueryString(pRequest.getQueryString());
                        ac.setBaseURL(pRequest.getScheme() + "://" + pRequest.getServerName() + ":" + pRequest.getServerPort());
                        WEBDAV.service(pRequest, pResponse, ac, CARSFACTORY);
                    } catch (AccessDeniedException ade) {
                        pResponse.setHeader(WWW_AUTH, getCurrentRealm());
                        pResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } catch (CredentialExpiredException cee) {
                        pResponse.setHeader("Location", cee.getMessage());
                        pResponse.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);
                    } finally {
                        ac.destroy();
                    }
                }
            } catch (Exception e) {
                gLog.log(Level.WARNING, null, e);
            } finally {
                THREADCOUNT.decrementAndGet();
            }
            return;
        }
        super.service(pRequest, pResponse);
        return;
    }

    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "JeCARS REST webservice servlet";
    }
}
