package org.dy.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author vLua TODO 2004-12-22 16:02:52
 */
public class VerifyCodeServlet extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int IMAGE_WIDTH = 48;

    private static final int IMAGE_HEIGHT = 18;

    private static final int FONT_SIZE = 16;

    public static final String SESSION_KEY = "verify_code";

    private static final String SVG_SOURCE1 = "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">" + "<svg width=\"" + IMAGE_WIDTH + "\" height=\"" + IMAGE_HEIGHT + "\" xmlns=\"http://www.w3.org/2000/svg\">" + "<text x=\"0\" y=\"" + IMAGE_HEIGHT + "\" font-family=\"Arial\" font-size=\"" + IMAGE_HEIGHT + "\" fill=\"black\">";

    private static final String SVG_SOURCE2 = "</text></svg>";

    private boolean svgMode = false;

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String vcode = sn2vcode();
        req.getSession().setAttribute(SESSION_KEY, vcode);
        if (svgMode) outSVG(vcode, resp); else outJPEG(vcode, resp);
    }

    private void outSVG(String vcode, HttpServletResponse resp) throws IOException {
        resp.setContentType("image/svg+xml");
        resp.getOutputStream().print(SVG_SOURCE1 + vcode + SVG_SOURCE2);
    }

    private void outJPEG(String vcode, HttpServletResponse resp) throws IOException {
        resp.setContentType("image/jpeg");
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Random random = new Random();
        Graphics g = image.getGraphics();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g.setFont(new Font("Times New Roman", Font.HANGING_BASELINE, FONT_SIZE));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(IMAGE_WIDTH);
            int y = random.nextInt(IMAGE_HEIGHT);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        for (int i = 1; i <= 4; i++) {
            String rand = vcode.substring(i - 1, i);
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * (i - 1) + 0, 16);
        }
        ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();
        JPEGImageWriteParam params = new JPEGImageWriteParam(null);
        ImageOutputStream ios = ImageIO.createImageOutputStream(resp.getOutputStream());
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), params);
        writer.dispose();
        ios.close();
    }

    static String sn2vcode() {
        String sRand = "";
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(random.nextInt(10));
            sRand += rand;
        }
        return sRand;
    }

    public void init() throws ServletException {
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        } catch (Throwable e) {
            svgMode = true;
        }
    }

    /**
	 * ��������ɫ
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
    public Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
