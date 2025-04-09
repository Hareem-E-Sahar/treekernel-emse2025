package com.ergal.ezweb.example.pages;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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

/**
 * Servlet implementation class VerifyImage 生成验证码的图片servlet
 */
public class VerifyImage extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String[] CHARS = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

    /**
	 * @see HttpServlet#HttpServlet()
	 */
    public VerifyImage() {
        super();
    }

    public void generateImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        HttpSession session = request.getSession();
        int width = 80, height = 22;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        g.setColor(getRandColor(120, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        String sRand = "";
        for (int i = 0; i < 5; i++) {
            String rand = getRandomString();
            sRand += rand;
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 16);
        }
        session.setAttribute("rand", sRand);
        g.dispose();
        ServletOutputStream responseOutputStream = response.getOutputStream();
        ImageIO.write(image, "JPEG", responseOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

    /**
	 * 给定范围获得随机颜色 三个都在200到250之间
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
    Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        generateImage(request, response);
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        generateImage(request, response);
    }

    /**
	 * 获取随机的数字0-9
	 * 
	 * @return
	 */
    private String getRandomNumber() {
        Random random = new Random();
        int x = random.nextInt(10);
        String intStr = String.valueOf(x);
        return intStr;
    }

    /**
	 * 获取随机字母
	 * 
	 * @return
	 */
    private String getRandomChar() {
        Random random = new Random();
        int x = random.nextInt(26);
        return CHARS[x].toUpperCase();
    }

    /**
	 * 获取一个随机的字符串 可以是数字 也可以是大写字母
	 * 
	 * @return
	 */
    private String getRandomString() {
        String randomString;
        Random random = new Random();
        int intOrChar = random.nextInt(2);
        if (intOrChar == 0) {
            randomString = getRandomNumber();
        } else {
            randomString = getRandomChar();
        }
        return randomString;
    }

    /**
	 * 旋转图片指定的角度
	 * @param bufferedimage
	 * @param degree
	 * @return
	 */
    private BufferedImage rotateImage(final BufferedImage bufferedimage, final int degree) {
        int w = bufferedimage.getWidth();
        int h = bufferedimage.getHeight();
        int type = bufferedimage.getColorModel().getTransparency();
        BufferedImage img;
        Graphics2D graphics2d;
        (graphics2d = (img = new BufferedImage(w, h, type)).createGraphics()).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);
        graphics2d.drawImage(bufferedimage, 0, 0, null);
        graphics2d.dispose();
        return img;
    }
}
