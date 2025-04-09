package org.apache.jsp.tools;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.imageio.*;
import sunsite.tools.*;

public final class verifyCode_jsp extends org.apache.jasper.runtime.HttpJspBase implements org.apache.jasper.runtime.JspSourceDependent {

    Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

    private static java.util.Vector _jspx_dependants;

    private org.apache.jasper.runtime.ResourceInjector _jspx_resourceInjector;

    public Object getDependants() {
        return _jspx_dependants;
    }

    public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {
        PageContext pageContext = null;
        HttpSession session = null;
        ServletContext application = null;
        ServletConfig config = null;
        JspWriter out = null;
        Object page = this;
        JspWriter _jspx_out = null;
        PageContext _jspx_page_context = null;
        try {
            response.setContentType("image/JPEG;charset=GBK");
            pageContext = _jspxFactory.getPageContext(this, request, response, null, true, 8192, true);
            _jspx_page_context = pageContext;
            application = pageContext.getServletContext();
            config = pageContext.getServletConfig();
            session = pageContext.getSession();
            out = pageContext.getOut();
            _jspx_out = out;
            _jspx_resourceInjector = (org.apache.jasper.runtime.ResourceInjector) application.getAttribute("com.sun.appserv.jsp.resource.injector");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\r\n");
            out.write("   \"http://www.w3.org/TR/html4/loose.dtd\">\r\n");
            out.write('\r');
            out.write('\n');
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            String widthStr = request.getParameter("width");
            String heigthStr = request.getParameter("heigth");
            int width = 147, height = 40;
            if (widthStr != null) width = Integer.parseInt(widthStr);
            if (heigthStr != null) height = Integer.parseInt(heigthStr);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            Random random = new Random();
            g.setColor(getRandColor(200, 250));
            g.fillRect(0, 0, width, height);
            g.setFont(new Font("Arial", Font.BOLD, height / 2 - 4));
            g.setColor(getRandColor(160, 200));
            for (int i = 0; i < 155; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                int xl = random.nextInt(30);
                int yl = random.nextInt(30);
                g.drawLine(x, y, x + xl, y + yl);
            }
            g.setColor(new Color(20, 60, 255));
            g.drawLine(0, 0, 0, height - 1);
            g.drawLine(0, height - 1, width - 1, height - 1);
            g.drawLine(0, 0, width - 1, 0);
            g.drawLine(width - 1, 0, width - 1, height - 1);
            g.setColor(new Color(190, 120, 120));
            g.drawString("you are .", 7, 15);
            g.drawString("welcome.", 73, 37);
            g.setFont(new Font("Arial", Font.PLAIN, height - 4));
            String sRand = "";
            for (int i = 0; i < 4; i++) {
                String rand = String.valueOf(random.nextInt(10));
                sRand += rand;
                g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
                g.drawString(rand, width / 5 * i + 16, height - 5);
            }
            session.setAttribute(ParamsString.CheckCodeSession, sRand);
            g.dispose();
            ImageIO.write(image, "JPEG", response.getOutputStream());
            out.write(' ');
        } catch (Throwable t) {
            if (!(t instanceof SkipPageException)) {
                out = _jspx_out;
                if (out != null && out.getBufferSize() != 0) out.clearBuffer();
                if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
            }
        } finally {
            _jspxFactory.releasePageContext(_jspx_page_context);
        }
    }
}
