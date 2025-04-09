package com.litt.core.util.html;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 彩色图片验证码
 * @author <a href="mailto:littcai@hotmail.com">空心大白菜</a>
 * @date 2007-01-09
 * @version 1.0
 *
 */
public class VerifyCode extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final Font mFont = new Font("Times New Roman", Font.PLAIN, 18);

    private static final String choose = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int width = 60, height = 20;
        ServletOutputStream out = response.getOutputStream();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics gra = image.getGraphics();
        Random random = new Random();
        gra.setColor(getRandColor(200, 250));
        gra.fillRect(0, 0, width, height);
        gra.setColor(Color.black);
        gra.setFont(mFont);
        gra.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gra.drawLine(x, y, x + xl, y + yl);
        }
        char[] sRand = { '0', '0', '0', '0' };
        char temp;
        for (int i = 0; i < 4; i++) {
            temp = choose.charAt(random.nextInt(choose.length()));
            sRand[i] = temp;
            gra.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            gra.drawString(String.valueOf(temp), 13 * i + 6, 16);
        }
        session.setAttribute("verifyCode", String.valueOf(sRand));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        encoder.encode(image);
        out.close();
    }

    static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
