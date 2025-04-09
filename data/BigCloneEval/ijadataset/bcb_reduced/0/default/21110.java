import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import com.java4less.rchart.*;
import com.java4less.rchart.encoder.*;
import java.awt.Graphics2D.*;
import java.awt.*;

public class RChartServlet extends HttpServlet {

    private boolean debug = false;

    /**
         * Handle the HTTP POST method by sending an e-mail
         *
         *
         */
    public void init() throws ServletException {
    }

    private Chart getChart(HttpServletRequest request) {
        chartLoader loader = new chartLoader(new Frame());
        loader.paintDirect = true;
        String dataFile = null;
        if (request != null) {
            if (request.getParameter("DEBUG") != null) if (request.getParameter("DEBUG").toUpperCase().compareTo("ON") == 0) debug = true;
            java.util.Enumeration ps = request.getParameterNames();
            while (ps.hasMoreElements()) {
                String name = (String) ps.nextElement();
                loader.setParameter(name, request.getParameter(name));
                if (debug) System.out.println("PARAM: " + name + "=" + request.getParameter(name));
                if (name.compareTo("DATAFILE") == 0) dataFile = request.getParameter(name);
            }
        }
        java.io.File f = new java.io.File("a.txt");
        if (debug) System.out.println(f.getAbsolutePath());
        if (dataFile != null) {
            if (debug) System.out.println("loading file " + dataFile);
            loader.loadFromFile(dataFile, false);
        }
        Chart chart = loader.build(false, false);
        if (debug) System.out.println("Chart Built");
        if (chart.tmpImage == null) {
            if (debug) System.out.println("Creating TMP Image");
            Chart.tmpImage = new java.awt.image.BufferedImage(200, 200, java.awt.image.BufferedImage.TYPE_INT_RGB);
        }
        return chart;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out;
        ServletOutputStream outb;
        String encode = "jpeg";
        if (request != null) {
            if (request.getParameter("FORMAT") != null) encode = request.getParameter("FORMAT").toLowerCase();
            if ((encode.toLowerCase().compareTo("gif") != 0) && ((encode.toLowerCase().compareTo("png") != 0))) encode = "jpeg";
        }
        response.setContentType("image/" + encode);
        outb = response.getOutputStream();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        try {
            int w = 500;
            int h = 500;
            if (request != null) {
                if (request.getParameter("WIDTH") != null) w = new Integer(request.getParameter("WIDTH")).intValue();
                if (request.getParameter("HEIGHT") != null) h = new Integer(request.getParameter("HEIGHT")).intValue();
            }
            java.awt.image.BufferedImage ChartImage = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D ChartGraphics = ChartImage.createGraphics();
            Chart c = getChart(request);
            if (debug) System.out.println("Size: " + w + " " + h);
            c.setSize(w, h);
            c.paint(ChartGraphics);
            if (encode.toLowerCase().compareTo("gif") == 0) {
                Class enClass = Class.forName("Acme.JPM.Encoders.GifEncoder");
                Class[] constructorParams = new Class[2];
                constructorParams[0] = Class.forName("java.awt.Image");
                constructorParams[1] = Class.forName("java.io.OutputStream");
                Object[] constructorObj = new Object[2];
                constructorObj[0] = ChartImage;
                constructorObj[1] = outb;
                Object encoder = enClass.getConstructor(constructorParams).newInstance(constructorObj);
                Class[] encodeParams = new Class[0];
                Object[] encodeObj = new Object[0];
                enClass.getMethod("encode", encodeParams).invoke(encoder, encodeObj);
            } else {
                if (encode.toLowerCase().compareTo("png") == 0) {
                    Class enClass = Class.forName("com.bigfoot.bugar.image.PNGEncoder");
                    Class[] constructorParams = new Class[2];
                    constructorParams[0] = Class.forName("java.awt.Image");
                    constructorParams[1] = Class.forName("java.io.OutputStream");
                    Object[] constructorObj = new Object[2];
                    constructorObj[0] = ChartImage;
                    constructorObj[1] = outb;
                    Object encoder = enClass.getConstructor(constructorParams).newInstance(constructorObj);
                    Class[] encodeParams = new Class[0];
                    Object[] encodeObj = new Object[0];
                    enClass.getMethod("encodeImage", encodeParams).invoke(encoder, encodeObj);
                } else {
                    com.sun.image.codec.jpeg.JPEGImageEncoder encoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(outb);
                    encoder.encode(ChartImage);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
