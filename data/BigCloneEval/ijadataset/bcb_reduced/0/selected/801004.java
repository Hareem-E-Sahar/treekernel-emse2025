package com.cjframework.common.servlet;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * 随机图片验证码生成器
 * <pre>
 * 本类提供随机图片验证码生成器，调用ValidateCodeCreator()方法
 * 将生成随机图片与验证码
 * 随机图片：将被输出到response.getOutputStream()
 * 验证码:将被保存到 Session 里默认名称为：validateCode
 * </pre>
 * CJFrameWork Version: 1.0
 * @author caojian
 */
public class ValidateCodeCreator {

    /**
	 * 随机生成的验证码
	 */
    private String validateCode;

    private static final String randomSrc = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";

    /**
	 * 验证码的位数
	 */
    private final int validateCodeLen = 6;

    /**
	 * 内存图象的 width
	 */
    private final int bufferedImageWidth = 156;

    /**
	 * 内存图象的 height
	 */
    private final int bufferedImageHeight = 19;

    /**
	 * 产生验证码与验证码图象
	 * @return 验证码图象
	 */
    public BufferedImage creatImageAndCode() {
        validateCode = "";
        BufferedImage validateImage = new BufferedImage(bufferedImageWidth, bufferedImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics myGraphic = validateImage.getGraphics();
        Random random = new Random();
        myGraphic.setColor(new Color(255, 255, 255));
        myGraphic.fillRect(0, 0, bufferedImageWidth, bufferedImageHeight);
        myGraphic.setFont(new Font("Times New Roman", Font.ITALIC, 20));
        myGraphic.setColor(getRandColor(190, 220));
        for (int i = 0; i < 19; i++) {
            int x = random.nextInt(bufferedImageWidth);
            int y = random.nextInt(bufferedImageHeight);
            int xl = random.nextInt(bufferedImageWidth);
            int yl = random.nextInt(bufferedImageHeight);
            myGraphic.drawLine(x, y, x + xl, y + yl);
        }
        for (int i = 0; i < validateCodeLen; i++) {
            int index = random.nextInt(randomSrc.length());
            String rand = String.valueOf(randomSrc.charAt(index));
            validateCode += rand;
            myGraphic.setColor(getRandColor(0, 190));
            myGraphic.drawString(rand, 23 * i + 12, 16);
        }
        myGraphic.dispose();
        return validateImage;
    }

    /**
     * 给定范围获得随机颜色
     * @param fc
     * @param bc
     * @return 随机颜色
     */
    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public String getValidateCode() {
        return validateCode;
    }
}
