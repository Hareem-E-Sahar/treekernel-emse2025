package com.nogoodatcoding.cip.idle;

import com.nogoodatcoding.cip.CiscoIPPhoneImage;
import com.nogoodatcoding.cip.interfaces.CiscoIPPhoneXMLObject;
import com.nogoodatcoding.cip.converter.Converter;
import com.nogoodatcoding.cip.idle.interfaces.IdleService;
import com.nogoodatcoding.cip.idle.services.Default;
import com.nogoodatcoding.cip.idle.utils.Utils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author no.good.at.coding
 */
public class IdleServiceServlet extends HttpServlet {

    private static Logger log_ = Logger.getLogger(IdleServiceServlet.class);

    private static ResourceBundle messages_ = ResourceBundle.getBundle("com.nogoodatcoding.cip.idle.messages.Messages_IdleServiceServlet");

    private Map<String, Map> idleServiceInitParams = null;

    private Map<String, Class> idleServiceClasses = null;

    private Map<String, IdleService> idleServices = new HashMap<String, IdleService>();

    private IdleService nullService = new Default();

    @Override
    public void init() throws ServletException {
        super.init();
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.loadingServiceClassesMap"));
        idleServiceClasses = (Map) this.getServletContext().getAttribute(Utils.IDLE_SERVICES_MAP_NAME);
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.doneLoadingServiceClassesMap"));
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.loadingServiceInitParamsMap"));
        idleServiceInitParams = (Map) this.getServletContext().getAttribute(Utils.IDLE_SERVICES_INIT_PARAMS_MAP_NAME);
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.doneLoadingServiceInitParamsMap"));
        nullService.init(null);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isTest = false;
        IdleService service = null;
        CiscoIPPhoneXMLObject cipo = null;
        CIPPhone phone = null;
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String[]> parameters = request.getParameterMap();
        Map<String, String> serverInfo = new HashMap<String, String>();
        serverInfo.put(Utils.SERVER_INFO_KEY_SCHEME, request.getScheme());
        serverInfo.put(Utils.SERVER_INFO_KEY_SERVER_NAME, request.getServerName());
        serverInfo.put(Utils.SERVER_INFO_KEY_SERVER_PORT, request.getServerPort() + "");
        serverInfo.put(Utils.SERVER_INFO_KEY_CONTEXT_PATH, getServletContext().getContextPath());
        phone = PhoneCache.getForIPAddress(request.getRemoteAddr(), request.getHeader(Utils.CISCO_HEADER_xCiscoIPPhoneModelName), request.getHeader(Utils.CISCO_HEADER_xCiscoIPPhoneDisplay), request.getHeader(Utils.CISCO_HEADER_xCiscoIPPhoneSDKVersion));
        if (request.getParameter("test") != null) {
            IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.isTest"));
            isTest = true;
        }
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.generatingCIPO"));
        service = getService(request.getParameter("service"));
        cipo = service.processRequest(parameters, headers, serverInfo, phone);
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.cipo") + cipo);
        if (cipo == null) {
            IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.nullCIPO"));
            cipo = nullService.processRequest(parameters, headers, serverInfo, phone);
        }
        PrintWriter out = null;
        OutputStream outStream = null;
        try {
            if (isTest) {
                if (cipo instanceof CiscoIPPhoneImage) {
                    IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.isTest.image"));
                    outStream = response.getOutputStream();
                    setHeaders(headers, response);
                    response.setContentType(Utils.CONTENT_TYPE_GIF);
                    BufferedImage bi = Converter.cip2Image((CiscoIPPhoneImage) cipo);
                    ImageIO.write(bi, "GIF", outStream);
                } else {
                    IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.isTest.text"));
                    setHeaders(headers, response);
                    response.setContentType(Utils.CONTENT_TYPE_PLAINTEXT);
                    out = response.getWriter();
                    out.print(cipo);
                }
            } else {
                IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.writingOutput") + cipo);
                out = response.getWriter();
                response.setContentType(Utils.CONTENT_TYPE_XML);
                setHeaders(headers, response);
                out.print(cipo);
                IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.doneWritingOutput"));
            }
        } finally {
            if (out != null) {
                IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.closingPrintWriter"));
                out.close();
            }
            if (outStream != null) {
                IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.closingOutputStream"));
                outStream.close();
            }
        }
    }

    /**
     *
     * Returns the {@code IdleService} object that will service a request
     * If the {@code IdleService hasn't already been {@code init()}ed, it will
     * instantiate it, call {@code init()} on it and then return the object.
     *
     * @param serviceName The service that was asked for in the request
     *
     * @return The {@code IdleService} object that was configured to service
     *         the {@code serviceName} service parameter
     */
    private IdleService getService(String serviceName) {
        if (serviceName == null || serviceName.length() == 0) {
            IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.serviceNameNull"));
            serviceName = null;
        }
        IdleService idleService = idleServices.get(serviceName);
        if (idleService == null) {
            IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.serviceObjectNotFound"));
            try {
                IdleService s = (IdleService) ((idleServiceClasses.get(serviceName)).getConstructor().newInstance());
                IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.serviceInstantiated"));
                s.init(idleServiceInitParams.get(serviceName));
                IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.serviceInitialized"));
                idleServices.put(serviceName, s);
                idleService = s;
            } catch (NoSuchMethodException e) {
                IdleServiceServlet.log_.error(IdleServiceServlet.messages_.getString("idleServiceServlet.log.error.serviceInstantiation"), e);
            } catch (InstantiationException e) {
                IdleServiceServlet.log_.error(IdleServiceServlet.messages_.getString("idleServiceServlet.log.error.serviceInstantiation"), e);
            } catch (IllegalAccessException e) {
                IdleServiceServlet.log_.error(IdleServiceServlet.messages_.getString("idleServiceServlet.log.error.serviceInstantiation"), e);
            } catch (InvocationTargetException e) {
                IdleServiceServlet.log_.error(IdleServiceServlet.messages_.getString("idleServiceServlet.log.error.serviceInstantiation"), e);
            }
        }
        if (idleService == null) {
            IdleServiceServlet.log_.info(IdleServiceServlet.messages_.getString("idleServiceServlet.log.info.defaultService"));
            idleService = idleServices.get(null);
        }
        IdleServiceServlet.log_.debug(IdleServiceServlet.messages_.getString("idleServiceServlet.log.debug.serviceReturned") + idleService.getIdleServiceName());
        return idleService;
    }

    private void setHeaders(Map<String, String> headers, HttpServletResponse response) {
        String value = null;
        if (headers == null || headers.size() == 0) return;
        value = headers.get(Utils.HTTP_HEADER_REFRESH);
        if (value != null && value.length() > 0) {
            try {
                Integer.parseInt(value);
                response.setHeader(Utils.HTTP_HEADER_REFRESH, value);
            } catch (NumberFormatException e) {
                IdleServiceServlet.log_.error(IdleServiceServlet.messages_.getString("idleServiceServlet.log.error.numberFormatException.refreshHeader"), e);
            }
        }
        value = headers.get(Utils.HTTP_HEADER_CONTENT_TYPE);
        if (value != null && value.length() > 0) {
            response.setContentType(value);
        }
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
