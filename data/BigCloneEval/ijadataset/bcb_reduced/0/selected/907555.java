package com.yuchengtech.test.pack.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Random;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.yuchengtech.simpleServer.core.HttpRequest;
import com.yuchengtech.simpleServer.core.HttpResponse;
import com.yuchengtech.simpleServer.core.exception.SimpleServerException;
import com.yuchengtech.simpleServer.servlet.SimpleServlet;

public class CheckCode extends SimpleServlet {

    private static final String[] rands = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

    @Override
    public void destory() throws SimpleServerException {
    }

    @Override
    public void init() throws SimpleServerException {
    }

    public void service(HttpRequest request, HttpResponse response) throws SimpleServerException {
        response.setMimeType("image/jpeg");
        response.setCharset(null);
        BufferedImage image = makeImage();
        OutputStream os = response.getOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
        try {
            encoder.encode(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BufferedImage makeImage() {
        final int width = 60, height = 20;
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
        String sRand = "";
        for (int i = 0; i < 4; i++) {
            String rand = rands[random.nextInt(16)];
            sRand += rand;
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 16);
        }
        return image;
    }

    Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
