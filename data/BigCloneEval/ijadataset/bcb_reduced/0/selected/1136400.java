package net.sf.ketu.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public final class KetuServlet extends HttpServlet implements Servlet {

    /** */
    private static final long serialVersionUID = -2312350221741541740L;

    /** */
    private Properties pom;

    /**
     *
     */
    public KetuServlet() {
        super();
    }

    public void init() throws ServletException {
        pom = new Properties();
        try {
            InputStream istream = getServletContext().getResourceAsStream("META-INF/maven/net.sf.ketu/ketu-webapp/pom.properties");
            try {
                pom.load(istream);
            } finally {
                istream.close();
            }
        } catch (IOException ex) {
            throw new ServletException("Failed to initialize servlet", ex);
        }
    }

    /**
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     *
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        dumpRequest(request);
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.endsWith(".xpi")) {
            String xpiName = "ketu-" + pom.getProperty("version") + ".xpi";
            if (pathInfo.contains("ketu")) {
                if (pathInfo.equals("/" + xpiName)) {
                    System.out.println("writeKetuXpi: " + xpiName);
                    try {
                        writeKetuXpi(response);
                        System.out.println("writeKetuXpi is successful");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw ex;
                    } finally {
                        System.out.println("writeKetuXpi has ended");
                    }
                } else {
                    System.out.println("sendRedirect: " + xpiName);
                    response.sendRedirect(xpiName);
                }
            } else {
                System.out.println("sendError: " + pathInfo);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (pathInfo.endsWith(".ketu")) {
            System.out.println("setContentType");
            response.setContentType("application/x-ketu");
        }
    }

    private void writeKetuXpi(HttpServletResponse response) throws IOException {
        response.setContentType("application/x-xpinstall");
        response.setContentLength(33466);
        OutputStream ostream = response.getOutputStream();
        try {
            ZipOutputStream zip = new ZipOutputStream(ostream);
            zip.setLevel(9);
            ZipEntry entry = new ZipEntry("install.js");
            zip.putNextEntry(entry);
            copyToOutputStream(zip, "WEB-INF/install.js");
            entry = new ZipEntry("libketuplugin.so");
            zip.putNextEntry(entry);
            extractToOutputStream(zip, "WEB-INF/lib/ketu-plugin-0.2-SNAPSHOT-native-i386-linux.jar", "lib/i386/linux/libketuplugin.so");
            zip.close();
        } finally {
            ostream.flush();
        }
    }

    private void copyToOutputStream(OutputStream ostream, String resource) throws IOException {
        InputStream istream = getServletContext().getResourceAsStream(resource);
        try {
            int ch;
            while ((ch = istream.read()) > -1) {
                ostream.write(ch);
            }
        } finally {
            istream.close();
        }
    }

    private void extractToOutputStream(OutputStream ostream, String resource, String entryPath) throws IOException {
        InputStream istream = getServletContext().getResourceAsStream(resource);
        try {
            ZipInputStream zip = new ZipInputStream(istream);
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entryPath.equals(entry.getName())) {
                    break;
                }
            }
            if (entry == null) {
                return;
            }
            int ch;
            while ((ch = zip.read()) > -1) {
                ostream.write(ch);
            }
        } finally {
            istream.close();
        }
    }

    private void dumpRequest(HttpServletRequest request) {
        System.out.println("REQUEST:");
        String contextPath = request.getContextPath();
        System.out.println(" contextPath = " + contextPath);
        String requestURI = request.getRequestURI();
        System.out.println(" requestURI = " + requestURI);
        String requestURL = request.getRequestURL().toString();
        System.out.println(" requestURL = " + requestURL);
        String pathInfo = request.getPathInfo();
        System.out.println(" pathInfo = " + pathInfo);
        String pathTranslated = request.getPathTranslated();
        System.out.println(" pathTranslated = " + pathTranslated);
        String servletPath = request.getServletPath();
        System.out.println(" servletPath = " + servletPath);
    }
}
