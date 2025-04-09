package com.zzsoft.app.base.sysuser.login;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import com.zzsoft.app.base.sysuser.C_Base_SysUser;
import framework.zze2p.mod.pojo4.Pojo_4HO;
import framework.zze2p.mod.pojo4.Pojo_4I;

/**
 * ������ͼƬ��֤��
 * @author chengxg
 *
 */
public class ImageBO implements ImageBOI {

    private static final long serialVersionUID = -3938318741402322164L;

    /**
	 * ���������ɫ
	 * @param fc 
	 * @param bc
	 * @return �����ɫ
	 */
    private static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static String charsLong = "23456789abcdefghjklmnpqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ";

    private static String charsShort = "0123456789";

    private static String chars = charsShort;

    /**
     * ������ͼƬ��֤��
     * @return Pojo_4I 
     *      �������ԣ�  C_Base_SysUser.Login.ImageString ���
     *                C_Base_SysUser.Login.Image  ����Ӧ��ͼ���
     */
    public Pojo_4I getImageCode() {
        Pojo_4I pojo = new Pojo_4HO();
        int charsLength = chars.length();
        int width = 80, height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(230, 250));
        g.fillRect(0, 0, width, height);
        g.setColor(getRandColor(200, 230));
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(width);
            int yl = random.nextInt(width);
            g.drawLine(x, y, x + xl, y + yl);
        }
        g.setFont(new Font("Times New Roman", Font.ITALIC, height));
        g.setColor(getRandColor(120, 200));
        for (int i = 0; i < 35; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        StringBuilder sRand = new StringBuilder();
        String[] fontNames = { "Times New Roman", "Arial", "Book antiqua", "" };
        for (int i = 0; i < 4; i++) {
            g.setFont(new Font(fontNames[random.nextInt(3)], Font.ITALIC, height));
            char rand = chars.charAt(random.nextInt(charsLength));
            sRand.append(rand);
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(String.valueOf(rand), 16 * i + random.nextInt(6) + 3, height - random.nextInt(4));
        }
        pojo.set(C_Base_SysUser.Login.ImageString, sRand.toString());
        g.dispose();
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        pojo.set(C_Base_SysUser.Login.BufferedImage, image);
        return pojo;
    }
}
