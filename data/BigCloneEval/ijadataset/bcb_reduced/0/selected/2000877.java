package servlet.opac;

import ejb.bprocess.util.NewGenLibRoot;
import ejb.bprocess.util.StaticValues;
import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author  Administrator
 * @version
 */
public class displayLocationImage extends HttpServlet {

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /** Destroys the servlet.
     */
    public void destroy() {
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String libid = request.getParameter("ownlibid");
        String cord = request.getParameter("coordinates");
        java.util.StringTokenizer st1 = new java.util.StringTokenizer(cord, "|");
        String filename = st1.nextToken();
        String cord1 = st1.nextToken();
        java.util.StringTokenizer st2 = new java.util.StringTokenizer(cord1, ",");
        int x1 = Integer.parseInt(st2.nextToken().trim());
        int y1 = Integer.parseInt(st2.nextToken().trim());
        int x2 = Integer.parseInt(st2.nextToken().trim());
        int y2 = Integer.parseInt(st2.nextToken().trim());
        byte[] byx = new byte[100];
        try {
            String filepath = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("Maps");
            filepath += "/LIB_" + libid + "/" + filename.trim() + ".jpg";
            java.io.File actualfile = new java.io.File(filepath);
            java.nio.channels.FileChannel fc = (new java.io.FileInputStream(actualfile)).getChannel();
            int fileLength = (int) fc.size();
            java.nio.MappedByteBuffer bb = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fileLength);
            byx = new byte[bb.capacity()];
            bb.get(byx);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        javax.swing.ImageIcon icon = null;
        java.awt.Image image = null;
        java.awt.image.BufferedImage imgBuff = null;
        icon = new javax.swing.ImageIcon(byx);
        image = icon.getImage();
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        imgBuff = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics g3 = imgBuff.getGraphics();
        g3.drawImage(image, 0, 0, null);
        g3.setColor(java.awt.Color.RED);
        g3.fillRect(x1, y1, x2, y2);
        response.setContentType("image/jpeg");
        javax.imageio.ImageIO.write(imgBuff, "jpg", new java.io.File(NewGenLibRoot.getRoot() + "/image.jpg"));
        javax.imageio.ImageIO.write(imgBuff, "jpg", response.getOutputStream());
        imgBuff.flush();
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
}
