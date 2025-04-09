package com.jz.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * <p>
 * <a href="CreateAuthNumberUtils.java.html"><i>View Source</i></a>
 * </p>
 *
 * @author 5jxiang
 * @version $Id$
 */
public class CreateAuthNumberUtils {

    private static String[] NUMBER_ARRAY = { "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "R", "S", "T", "V", "U", "W", "X", "Y", "Z" };

    private static Color getRandColor(int cc, int bb) {
        Random random = new Random();
        if (cc > 255) cc = 255;
        if (bb > 255) bb = 255;
        int r = cc + random.nextInt(bb - cc);
        int g = cc + random.nextInt(bb - cc);
        int b = cc + random.nextInt(bb - cc);
        return new Color(r, g, b);
    }

    public static BufferedImage createNumberImage(String[] imageStr) {
        int width = 63;
        int height = 17;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int i_x = random.nextInt(width);
            int i_y = random.nextInt(height);
            int i_xl = random.nextInt(12);
            int i_yl = random.nextInt(12);
            g.drawLine(i_x, i_y, i_x + i_xl, i_y + i_yl);
        }
        for (int i = 0; i < imageStr.length; i++) {
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(imageStr[i], 13 * i + 6, 16);
        }
        g.dispose();
        return image;
    }

    public static String[] getAuthNumbers() {
        String[] authNumbers = new String[4];
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            authNumbers[i] = NUMBER_ARRAY[random.nextInt(NUMBER_ARRAY.length)];
        }
        return authNumbers;
    }
}
