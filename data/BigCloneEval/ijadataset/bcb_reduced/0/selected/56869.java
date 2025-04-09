package cn.collin.commons.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * http://collincode.googlecode.com
 * 
 * @author collin.code@gmail.com
 * 
 */
public class ImageVerifyCodeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private int codeLength = 4;

    public static final String INIT_PARAM_C0DELENGTH = "codeLength";

    public static final String SESSION_ATT_NAME = "VERIFY_CODE";

    public static final String REQUEST_ATT_NAME = "verifyCode";

    private static final char[] source = "abcdefghijkmnopqrstuvwxyz023456789".toCharArray();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String verifyCode = request.getParameter("verifyCode");
        if (verifyCode != null) {
            try {
                PrintWriter pw = new PrintWriter(response.getOutputStream());
                if (request.getSession().getAttribute(SESSION_ATT_NAME).toString().equals(verifyCode)) {
                    pw.write("1");
                } else {
                    pw.write("0");
                }
                pw.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else {
            if (getInitParameter(INIT_PARAM_C0DELENGTH) != null) {
                codeLength = Integer.parseInt(getInitParameter(INIT_PARAM_C0DELENGTH));
            }
            String code = genRandomCode(codeLength);
            genAndWriteImage(code, request, response);
        }
    }

    private void genAndWriteImage(String code, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int width = 15 * codeLength;
        int height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        for (int i = 0; i < this.codeLength; i++) {
            String rand = code.substring(i, i + 1);
            g.setColor(new Color(20 + random.nextInt(60), 20 + random.nextInt(120), 20 + random.nextInt(180)));
            g.drawString(rand, 13 * i + 6, 16);
        }
        g.dispose();
        request.getSession().setAttribute(SESSION_ATT_NAME, code);
        ServletOutputStream outStream;
        try {
            outStream = response.getOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outStream);
            encoder.encode(image);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String genRandomCode(int length) {
        char[] buf = new char[length];
        int rnd;
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            rnd = Math.abs(r.nextInt()) % source.length;
            buf[i] = source[rnd];
        }
        return new String(buf);
    }

    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
