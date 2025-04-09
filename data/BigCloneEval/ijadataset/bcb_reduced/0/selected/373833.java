package com.unsins.core.web.servlets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.unsins.core.SystemConstants;

/**
 * 类说明
 *
 * @author odpsoft
 * @create  2008 2008-10-30 
 */
public class ImageServlet extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4268052368429271031L;

    private static final String CONTENT_TYPE = "image/png";

    private static final String FONT_TYPE = "DialogInput";

    private static final String IMAGE_TYPE = "PNG";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int width = 60, height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font(FONT_TYPE, Font.ITALIC, 18));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 255; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        String sRand = "";
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(random.nextInt(10));
            sRand += rand;
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 16);
        }
        HttpSession session = request.getSession();
        session.setAttribute(SystemConstants.SESSION_VERIFY_CODE, sRand);
        g.dispose();
        ServletOutputStream outputstream = response.getOutputStream();
        ImageIO.write(image, IMAGE_TYPE, ImageIO.createImageOutputStream(outputstream));
        outputstream.flush();
        outputstream.close();
        outputstream = null;
        image = null;
    }

    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
