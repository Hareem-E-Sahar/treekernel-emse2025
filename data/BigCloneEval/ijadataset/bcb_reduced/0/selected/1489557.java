package com.iclotho.foundation.pub.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Image verify code
 */
public class FigureImage {

    private String imageCode;

    public FigureImage() {
        this.imageCode = genImageCode();
    }

    private static String genImageCode() {
        Random random = new Random();
        String sRandom = "";
        for (int i = 0; i < 5; i++) {
            String rand = String.valueOf(random.nextInt(10));
            sRandom += rand;
        }
        return sRandom;
    }

    private static BufferedImage genImage(String code) {
        int width = 65, height = 16;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 35; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(String.valueOf(code.charAt(i)), 12 * i + 4, 15);
        }
        g.dispose();
        return image;
    }

    private static Color getRandColor(int color1, int color2) {
        Random random = new Random();
        if (color1 > 255) color1 = 255;
        if (color2 > 255) color2 = 255;
        int r = color1 + random.nextInt(color2 - color1);
        int g = color1 + random.nextInt(color2 - color1);
        int b = color1 + random.nextInt(color2 - color1);
        return new Color(r, g, b);
    }

    public String getImageCode() {
        return this.imageCode;
    }

    public BufferedImage getImage() {
        return genImage(this.imageCode);
    }
}
