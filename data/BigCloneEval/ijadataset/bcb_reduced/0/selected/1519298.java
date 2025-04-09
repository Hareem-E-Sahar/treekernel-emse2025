package com.jxva.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import com.jxva.util.CharUtil;
import com.jxva.util.ChineseUtil;
import com.jxva.util.RandomUtil;

/**
 	<pre>
 	response.setHeader("Pragma","No-cache");  
	response.setHeader("Cache-Control","no-cache");  
	response.setDateHeader("Expires", 0);  
	response.reset();  
	response.setContentType("image/jpeg");
	RandomCode randomCode=new RandomCode();
	ImageIO.write(randomCode.createNumber(),"JPEG",response.getOutputStream());  
	session.setAttribute("randomCode",randomCode.getFlagInSession());
	</pre>
 *
 * @author  The Jxva Framework Foundation
 * @since   1.0
 * @version 2009-04-03 11:41:12 by Jxva
 */
public class RandomCode {

    private String flagInSession;

    public String getFlagInSession() {
        return flagInSession;
    }

    private Color getRandColor(int min, int max) {
        Random random = new Random();
        if (min > 255) min = 255;
        if (max > 255) max = 255;
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    public BufferedImage createNumber() {
        return createImage(true);
    }

    public BufferedImage createAlpha() {
        return createImage(false);
    }

    public BufferedImage createImage(boolean number) {
        int width = 60, height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(100, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(number ? random.nextInt(10) : RandomUtil.getRandomString(CharUtil.UPPER_CHAR_TABLE, 1));
            sb.append(rand);
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 17);
        }
        g.dispose();
        flagInSession = sb.toString();
        return image;
    }

    public BufferedImage createChinese() {
        int width = 106, height = 28;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        String[] fontTypes = { "宋体", "新宋体", "黑体", "楷体", "隶书" };
        int fontTypesLength = fontTypes.length;
        g.setColor(getRandColor(160, 200));
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        for (int i = 0; i < 6; i++) {
            g.drawString("*********************************************", 0, 5 * (i + 2));
        }
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String rand = RandomUtil.getRandomString(ChineseUtil.CHINESE_TABLE, 1);
            sb.append(rand);
            g.setColor(getRandColor(10, 150));
            g.setFont(new Font(fontTypes[random.nextInt(fontTypesLength)], Font.BOLD, 18 + random.nextInt(6)));
            g.drawString(rand, 24 * i + 3 + random.nextInt(8), 22);
        }
        g.dispose();
        flagInSession = sb.toString();
        return image;
    }
}
