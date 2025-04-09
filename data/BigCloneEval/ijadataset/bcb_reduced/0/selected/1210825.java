package org.vnetcon.xml.ws.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.*;
import javax.xml.bind.JAXBContext;
import org.vnetcon.xml.ws.servlet.dao.WebMethod;
import org.vnetcon.xml.ws.servlet.request.RequestHandler;
import org.vnetcon.xml.ws.servlet.schema.SchemaGenerator;

/**
 * This is the start class for WSS (WebServiceServlet)
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
@SuppressWarnings("serial")
public class WebServiceServlet extends HttpServlet {

    String version = "v0.6 (beta release)";

    public static JAXBContext jcEnvelope = null;

    public static JAXBContext jcBody = null;

    String wsClassAsString = null;

    String schemaFilePath = null;

    Class<?> wsClass = null;

    ArrayList<WebMethod> webMethods = null;

    HashMap<String, WebMethod> hashWebMethods = null;

    SchemaGenerator sg = null;

    String strWsdl = null;

    String strXsd = null;

    /**
	 * This will load the user created "WebService" class which is actually
	 * actually normal class which public methods are treated as WebService
	 * methods.
	 * 
	 * @throws Exception
	 */
    private void loadWebServiceClass() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        Method methods[] = null;
        int i = 0;
        webMethods = new ArrayList<WebMethod>();
        wsClass = cl.loadClass(wsClassAsString);
        methods = wsClass.getDeclaredMethods();
        while (i < methods.length) {
            Method m = methods[i];
            if (m.toString().startsWith("public ")) {
                WebMethod wm = new WebMethod(this.wsClass, m);
                webMethods.add(wm);
                hashWebMethods.put(wm.getMethodName(), wm);
            }
            i++;
        }
    }

    /**
	 * This will initialize WSS.
	 * User defined WebServiceClass and SchemaFile parameters are read here.
	 * 
	 */
    @Override
    public void init() {
        try {
            jcEnvelope = JAXBContext.newInstance(org.xmlsoap.schemas.soap.envelope.Envelope.class);
            jcBody = JAXBContext.newInstance(org.xmlsoap.schemas.soap.envelope.Body.class);
            hashWebMethods = new HashMap<String, WebMethod>();
            this.wsClassAsString = this.getInitParameter("WebServiceClass");
            this.schemaFilePath = this.getInitParameter("SchemaFilePath");
            this.loadWebServiceClass();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to initialize webservice class: " + e);
            e.printStackTrace(System.out);
        }
    }

    /**
	 * doGet is responsible for answering wsdl (http://host/wss?wsdl)
	 * and xsd (http://host/wss?xsd=1 requests.
	 * 
	 * In here SchemaGenerator instances are created and it will return
	 * wsdl and xsd information for clients.
	 * 
	 */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestUrl = "http://" + req.getServerName() + ":" + req.getServerPort() + req.getServletPath();
        if (req.isSecure()) {
            requestUrl = "https://" + req.getServerName() + ":" + req.getServerPort() + req.getServletPath();
        }
        String requestQuery = req.getQueryString();
        if (sg == null) {
            try {
                sg = new SchemaGenerator(requestUrl, this.schemaFilePath, this.wsClass, this.webMethods);
                this.strWsdl = sg.getWSDL();
                this.strXsd = sg.getSchema();
            } catch (Exception e) {
                System.out.println("Failded to create SchemaGenerator in GET: " + e);
                System.out.println("schemaFilePath: " + this.schemaFilePath);
                e.printStackTrace();
            }
        }
        if (requestQuery == null) {
            resp.setContentType("text/html");
            resp.getWriter().println("<H1>WebService</H1>");
            resp.getWriter().println("WSDL url: <a href=\"" + requestUrl + "?wsdl\">" + requestUrl + "?wsdl</a>");
            resp.getWriter().println("<br><br><br>");
            resp.getWriter().println("<br><br><hr>");
            resp.getWriter().println("powered by WSS, WebServiceServlet " + this.version + "<br><a href=\"http://www.vnetcon.org\">www.vnetcon.org</a>");
            return;
        }
        if (!requestQuery.toLowerCase().equals("wsdl") && !requestQuery.toLowerCase().equals("xsd=1")) {
            resp.setContentType("text/html");
            resp.getWriter().println("<H1>WebService</H1>");
            resp.getWriter().println("WSDL url: <a href=\"" + requestUrl + "?wsdl\">" + requestUrl + "?wsdl</a>");
            resp.getWriter().println("<br><br><br>");
            resp.getWriter().println("<br><br><hr>");
            resp.getWriter().println("powered by WSS, WebServiceServlet " + this.version + "<br><a href=\"http://www.vnetcon.org\">www.vnetcon.org</a>");
            return;
        }
        if (requestQuery.toLowerCase().equals("wsdl")) {
            resp.setContentType("text/xml");
            resp.getWriter().println(this.strWsdl);
        }
        if (requestQuery.toLowerCase().equals("xsd=1")) {
            resp.setContentType("text/xml");
            resp.getWriter().println(this.strXsd);
        }
    }

    /**
	 * doPost receives  SOAP (Simple Object Access Protocol) requests from
	 * WebService clients. The main request handler RequestHandler class
	 * is instances are created here and received SOAP messages are passed
	 * to it.
	 * 
	 */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int iLength = req.getContentLength();
        InputStream reqIn = req.getInputStream();
        OutputStream reqOut = resp.getOutputStream();
        byte buffer[] = null;
        String requestUrl = "http://" + req.getServerName() + ":" + req.getServerPort() + req.getServletPath();
        if (req.isSecure()) {
            requestUrl = "https://" + req.getServerName() + ":" + req.getServerPort() + req.getServletPath();
        }
        if (sg == null) {
            try {
                sg = new SchemaGenerator(requestUrl, this.schemaFilePath, this.wsClass, this.webMethods);
                this.strWsdl = sg.getWSDL();
                this.strXsd = sg.getSchema();
            } catch (Exception e) {
                System.out.println("Failed to create SchemaGenerator in POST: " + e);
                e.printStackTrace();
            }
        }
        if (iLength > 0) {
            RequestHandler rh = null;
            String requestString = null;
            String reply = null;
            buffer = new byte[iLength];
            reqIn.read(buffer, 0, iLength);
            requestString = new String(buffer, 0, iLength);
            try {
                byte[] buf = null;
                rh = new RequestHandler(requestString, this.wsClass, this.hashWebMethods, requestUrl);
                reply = rh.execute();
                resp.setContentType("text/xml");
                buf = reply.getBytes();
                if (buf.length > 10000000) {
                    throw new Exception("Buffer size exceed GAE limit 10.000.000 bytes: " + buf.length);
                }
                resp.setContentLength(buf.length);
                reqOut.write(buf);
                reqOut.flush();
                reqOut.close();
                reqIn.close();
                rh = null;
                Runtime.getRuntime().gc();
            } catch (Exception ex) {
                resp.setContentType("text/plain");
                resp.getWriter().println("WebService error:\n\n");
                ex.printStackTrace(resp.getWriter());
                ex.printStackTrace(System.out);
            }
        } else {
            resp.setContentType("text/html");
            resp.getWriter().println("<H1>WebService</H1>");
            resp.getWriter().println("WSDL url: <a href=\"" + requestUrl + "?wsdl\">" + requestUrl + "?wsdl</a>");
            resp.getWriter().println("<br><br><br>");
            resp.getWriter().println("<br><br><hr>");
            resp.getWriter().println("powered by WSS, WebServiceServlet " + this.version + "<br><a href=\"http://www.vnetcon.org\">www.vnetcon.org</a>");
        }
    }
}
