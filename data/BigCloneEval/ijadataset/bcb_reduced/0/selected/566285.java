package com.khotyn.heresy.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码生成器，可以设置验证图像高度，宽度，验证码长度
 * 
 * @author 黄挺
 * 
 */
public class ValidateCodeGenerator {

    private BufferedImage image;

    private int imageHeight;

    private int imageWidth;

    private String randCode;

    private int randCodeLength;

    private int noiseLineNumber;

    private static String randCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHILJLMNOPQRSTUVWXYZ";

    private static String[] fonts = { "Purisa", "文泉驿正黑", "Vera Sans YuanTi", "URW Gothic L", "serif" };

    private static int[] fontMode = { Font.BOLD, Font.ITALIC, Font.PLAIN };

    /**
	 * 构造函数，根据图像高度，宽度，验证码长度生成验证码和验证图像
	 * 
	 * @param imageHeight 验证图像高度
	 * @param imageWidth 验证图像宽度
	 * @param randCodeLength 验证码长度
	 * @param noiseLineNumber 噪声线段数量
	 */
    public ValidateCodeGenerator(int imageHeight, int imageWidth, int randCodeLength, int noiseLineNumber) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.randCodeLength = randCodeLength;
        this.noiseLineNumber = noiseLineNumber;
        this.generateValidateCode();
    }

    /**
	 * 获取验证图像
	 * @return 验证图像
	 */
    public BufferedImage getImage() {
        return image;
    }

    /**
	 * 设置验证图像
	 * @param image  要设置的验证图像
	 */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
	 * 获取验证图像高度
	 * @return 验证图像高度
	 */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
	 * 设置验证图像高度
	 * @param imageHeight 要设置的验证图像高度
	 */
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
	 * 获取验证图像宽度
	 * @return 验证图像宽度
	 */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
	 * 设置验证图像宽度
	 * @param imageWidth 要设置的验证图像宽度
	 */
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
	 * 获取验证码字符串
	 * @return 验证码字符串
	 */
    public String getRandCode() {
        return randCode;
    }

    /**
	 * 设置验证码字符串
	 * @param randCode 要设置的验证码字符串
	 */
    public void setRandCode(String randCode) {
        this.randCode = randCode;
    }

    /**
	 * 获取验证码字符串长度
	 * @return 验证码字符串长度
	 */
    public int getRandCodeLength() {
        return randCodeLength;
    }

    /**
	 * 设置验证码字符串长度
	 * @param randCodeLength 要设置的验证码字符串长度
	 */
    public void setRandCodeLength(int randCodeLength) {
        this.randCodeLength = randCodeLength;
    }

    /**
	 * 设置噪声线段数量
	 * @param noiseLineNumber 要设置的噪声先端数量
	 */
    public void setNoiseLineNumber(int noiseLineNumber) {
        this.noiseLineNumber = noiseLineNumber;
    }

    /**
	 * 获取噪声线段数量
	 * @return 噪声线段数量
	 */
    public int getNoiseLineNumber() {
        return noiseLineNumber;
    }

    /**
	 * 生成验证码与验证图像
	 */
    public void generateValidateCode() {
        BufferedImage tmpImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        String sRand = "";
        Graphics g = tmpImage.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, imageWidth, imageHeight);
        for (int i = 0; i < randCodeLength; i++) {
            g.setFont(new Font(fonts[random.nextInt(fonts.length)], fontMode[random.nextInt(fontMode.length)], imageHeight * 2 / 3));
            String rand = "" + randCharacters.charAt(random.nextInt(randCharacters.length()));
            sRand += rand;
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, imageWidth / 20 + i * imageWidth / 4, imageHeight * 3 / 4);
        }
        this.addNoiceLine(g);
        this.setImage(tmpImage);
        this.setRandCode(sRand.toUpperCase());
    }

    /**
	 * 在给定的范围内获取随机颜色
	 * @param fc 随机范围下边界
	 * @param bc 随机范围上边界
	 * @return 随机颜色
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
	 * 为验证码图像添加噪声线段
	 * @param g	图像上下文
	 */
    private void addNoiceLine(Graphics g) {
        Random random = new Random();
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < this.noiseLineNumber; i++) {
            int x = random.nextInt(this.imageWidth);
            int y = random.nextInt(this.imageHeight);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
    }
}
