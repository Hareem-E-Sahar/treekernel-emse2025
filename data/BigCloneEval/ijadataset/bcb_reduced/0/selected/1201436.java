package com.market.b2c.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.interceptor.ServletResponseAware;
import com.market.b2c.suport.BaseAction;
import com.market.b2c.suport.util.MessageDigestUtils;
import com.market.b2c.suport.util.web.CookieUtils;

/**
 * ��֤��action
 * author: zhangde 
 * date:   Sep 9, 2009 
 */
public class VerifyNumberAction extends BaseAction implements ServletResponseAware {

    public String get() throws IOException {
        response.setContentType("APPLICATION/OCTET-STREAM");
        int width = 60, height = 20;
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
        String sRand = "";
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(random.nextInt(10));
            sRand += rand;
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 16);
        }
        Date date = new Date();
        String dateString = String.valueOf(date.getTime());
        String key = sRand + dateString;
        String md5Key = MessageDigestUtils.Md5By16(key);
        Cookie cookie = new Cookie(CookieUtils.COOKIE_VERIFYNUMBER_NAME, md5Key.substring(0, md5Key.length() - 2) + "_" + dateString);
        cookie.setPath(CookieUtils.COOKIE_DEFAULT_PATH);
        CookieUtils.writeCookie(cookie);
        g.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageOutputStream imageOut = ImageIO.createImageOutputStream(output);
        ImageIO.write(image, "PNG", response.getOutputStream());
        imageOut.close();
        return NONE;
    }

    public Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private HttpServletResponse response;

    public void setServletResponse(HttpServletResponse response) {
        this.response = response;
    }
}
