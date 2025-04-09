package com.microbrain.cosmos.web.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import com.microbrain.cosmos.core.log.CosmosLogFactory;

/**
 * <p>
 * ���ͼ��ɹ��ߡ�
 * </p>
 * 
 * @author Richard Sun (Richard.SunRui@gmail.com)
 * @version 1.0, 08/12/10
 * @see java.awt.image.BufferedImage
 * @since CFDK 1.0
 */
public class ImageUtils {

    /**
	 * ˽�л����캯��
	 */
    private ImageUtils() {
    }

    /**
	 * ��־��¼�ࡣ
	 */
    private static final Log log = CosmosLogFactory.getLog();

    /**
	 * Ĭ�Ͽ?
	 */
    private static final int WIDTH = 50;

    /**
	 * Ĭ�ϸߡ�
	 */
    private static final int HEIGHT = 20;

    /**
	 * ��������ɫ��
	 * 
	 * @param fc
	 *            ��ɫ��Χ��
	 * @param bc
	 *            ��ɫ��Χ��
	 * @return �����ɫ����
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

    /**
	 * �������ͼ��
	 * 
	 * @param width
	 *            ���ͼ�?
	 * @param height
	 *            ���ͼ�ߡ�
	 * @param os
	 *            �������
	 * @return ���ͼ�ϵ����֡�
	 * @throws IOException
	 *             IO��д�쳣��
	 */
    public static String random(int width, int height, OutputStream os) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        StringBuilder sRand = new StringBuilder();
        String rand = null;
        for (int i = 0; i < 4; i++) {
            int x = random.nextInt(2);
            if (x == 0) {
                int j = random.nextInt(26);
                int k = (0 == random.nextInt(2)) ? (j + 65) : (j + 97);
                if (k == 111 || k == 108 || k == 73 || k == 79) {
                    i--;
                    continue;
                }
                char c = (char) k;
                rand = String.valueOf(c);
            } else {
                rand = String.valueOf(random.nextInt(10));
            }
            sRand.append(rand.toUpperCase());
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 10 * i + 6, 16 + random.nextInt(3));
        }
        g.dispose();
        try {
            ImageIO.write(image, "JPEG", os);
        } catch (IOException e) {
            log.error("Exception in generate image.", e);
            throw e;
        }
        return sRand.toString();
    }

    /**
	 * ����Ĭ�ϴ�С�����ͼ��
	 * 
	 * @param os
	 *            �������
	 * @return ���ͼ�ϵ����֡�
	 * @throws IOException
	 *             IO�쳣��
	 */
    public static String random(OutputStream os) throws IOException {
        return random(WIDTH, HEIGHT, os);
    }
}
