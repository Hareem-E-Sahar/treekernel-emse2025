package com.xinsdd.client.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.ImageIcon;

/**
 * 描述： 描述该文件做什么
 */
public class VerifyCode {

    static Random r = new Random();

    static String ssource = "abcdefghijklmnopqrstuvwxyz" + "0123456789";

    static char[] src = ssource.toCharArray();

    /**
	 * 产生随机字符串
	 * 
	 * @param length
	 * @return
	 */
    public String getCode(int length) {
        char[] buf = new char[length];
        int rnd;
        for (int i = 0; i < length; i++) {
            rnd = Math.abs(r.nextInt()) % src.length;
            buf[i] = src[rnd];
        }
        return new String(buf);
    }

    /**
	 * 给定范围获得随机颜色
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
	 * 调用该方法将得到的验证码生成图象
	 * 
	 * @param sCode
	 *            验证码
	 * @return 
	 * @return
	 */
    public ImageIcon getImage(String sCode, int width, int height) {
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
        for (int i = 0; i < sCode.length(); i++) {
            String rand = sCode.substring(i, i + 1);
            g.setColor(new Color(20 + random.nextInt(60), 20 + random.nextInt(120), 20 + random.nextInt(180)));
            g.drawString(rand, 13 * i + 6, height / 2);
        }
        g.dispose();
        return new ImageIcon(image);
    }
}
