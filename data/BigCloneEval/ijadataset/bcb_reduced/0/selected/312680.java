package com.zhongkai.tools;

import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.servlet.http.*;
import java.awt.*;
import java.awt.image.*;
import org.apache.log4j.*;

/**
 * <p>Title:商务e</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author 余伟明
 * funtion 生成验证码图片于内存中
 * @version 1.0
 */
public class ValidatorCodeTool {

    Logger log = Logger.getLogger(this.getClass());

    private int picWidth = 100;

    private int picHeight = 20;

    private String sessionName = "validatecode";

    private int codeLength = 6;

    private int colorStartValue = 200;

    private int colorEndValue = 255;

    private String charsetName = "Times New Roman";

    private int fontSize = 18;

    private String charset = "a-zA-Z0-9";

    private HttpServletResponse response;

    private HttpServletRequest request;

    private int fontDistance = 13;

    private int fontY = 16;

    private int fontX = 6;

    public ValidatorCodeTool(HttpServletResponse response, HttpServletRequest request) {
        this.request = request;
        this.response = response;
    }

    public void displayValidatorCode() {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        HttpSession session = request.getSession();
        if (picWidth <= 0) {
            picWidth = 100;
        }
        if (picHeight <= 0) {
            picHeight = 20;
        }
        int width = picWidth, height = picHeight;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        if (colorStartValue < 0 || colorStartValue > 255) {
            colorStartValue = 200;
        }
        if (colorEndValue < 0 || colorEndValue > 255) {
            colorEndValue = 250;
        }
        g.setColor(getRandColor(colorStartValue, colorEndValue));
        g.fillRect(0, 0, width, height);
        String charsetName1 = "Times New Roman";
        if (charsetName != null && !charsetName.equals("")) {
            charsetName1 = charsetName;
        }
        if (fontSize <= 0) {
            fontSize = 18;
        }
        g.setFont(new Font(charsetName1, Font.PLAIN, fontSize));
        String rstr = "";
        RandomStrg rst = new RandomStrg();
        String charset1 = "a-zA-Z0-9";
        if (charset != null && !charset.equals("")) {
            charset1 = charset;
        }
        rst.setCharset(charset1);
        String codeLength1 = "6";
        if (codeLength > 0) {
            codeLength1 = new Integer(codeLength).toString();
        }
        rst.setLength(codeLength1);
        try {
            rst.generateRandomObject();
            rstr = rst.getRandom();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String sRand = "";
        sRand = rstr;
        for (int i = 0; i < sRand.length(); i++) {
            String rand = String.valueOf(rstr.charAt(i));
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, fontDistance * i + fontX, fontY);
        }
        session.setAttribute(sessionName, sRand);
        g.dispose();
        try {
            ImageIO.write(image, "JPEG", response.getOutputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public String getCharset() {
        return charset;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public int getColorEndValue() {
        return colorEndValue;
    }

    public int getColorStartValue() {
        return colorStartValue;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getPicHeight() {
        return picHeight;
    }

    public int getPicWidth() {
        return picWidth;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getFontY() {
        return fontY;
    }

    public int getFontDistance() {
        return fontDistance;
    }

    public int getFontX() {
        return fontX;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public void setColorEndValue(int colorEndValue) {
        this.colorEndValue = colorEndValue;
    }

    public void setColorStartValue(int colorStartValue) {
        this.colorStartValue = colorStartValue;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void setPicHeight(int picHeight) {
        this.picHeight = picHeight;
    }

    public void setPicWidth(int picWidth) {
        this.picWidth = picWidth;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public void setFontY(int fontY) {
        this.fontY = fontY;
    }

    public void setFontDistance(int fontDistance) {
        this.fontDistance = fontDistance;
    }

    public void setFontX(int fontX) {
        this.fontX = fontX;
    }
}
