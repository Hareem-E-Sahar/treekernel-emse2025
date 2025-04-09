package cn.lzh.common.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * ������У��ͼƬ�Ĺ�����
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class EncryptedImgGenerator {

    private ThreadLocal<String> sRand = new ThreadLocal<String>();

    private Random random = new Random();

    private static char[] charArray = new char[36];

    static {
        for (int i = 0; i < 10; i++) {
            charArray[i] = (char) ('0' + i);
        }
        for (int i = 0; i < 26; i++) {
            charArray[i + 10] = (char) ('A' + i);
        }
    }

    private Color getRandColor(int fc, int bc) {
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
	 * �����֤�������֤ͼ��
	 * @return ��֤ͼ��
	 */
    public BufferedImage creatImage() {
        int width = 60, height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(getRandColor(180, 230));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        g.setColor(getRandColor(140, 180));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(24);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        g.setColor(getRandColor(20, 100));
        g.drawString(sRand.get(), 2, 16);
        g.dispose();
        return image;
    }

    /**
	 * ȡ����֤��
	 * @return ��֤��
	 */
    public String getSRand() {
        return sRand.get();
    }

    /**
	 * ������֤��
	 * @param rand
	 */
    public void setSRand(String rand) {
        sRand.set(rand);
    }

    /**
	 * ����������֤��
	 * @return ��֤��
	 */
    public String generateRandomCode() {
        char[] randC = new char[4];
        for (int i = 0; i < 4; i++) {
            randC[i] = charArray[random.nextInt(36)];
        }
        return new String(randC);
    }
}
