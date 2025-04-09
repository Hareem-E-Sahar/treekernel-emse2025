package org.hlj.web.verifycode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hlj.commons.conversion.ConversionUtil;
import org.hlj.commons.image.ImageUtil;
import org.hlj.commons.math.MathUtil;
import org.hlj.commons.random.*;
import org.hlj.log.log4j.common.SysLog;
import org.hlj.param.constants.ImageConstants;
import org.hlj.param.params.WebParams;
import org.hlj.web.constants.HttpConstants;
import org.hlj.web.util.AttributeUtil;
import org.hlj.web.util.ResponseUtil;

/**
 * 生成验证图片,并把验证码保存到sessin中
 * @author WD
 * @since JDK5
 * @version 1.0 2009-03-01
 */
public final class VerifyCode {

    /**
	 * 功能描述 : 生成验证图片,并把验证码保存到sessin中
	 * @param request
	 * @param response
	 */
    public static final void make(HttpServletRequest request, HttpServletResponse response) {
        make(request, response, WebParams.getVerifyKey());
    }

    /**
	 * 功能描述 : 生成验证图片,并把验证码保存到sessin中
	 * @param request
	 * @param response
	 * @param key 保存在session中的key
	 */
    public static final void make(HttpServletRequest request, HttpServletResponse response, String key) {
        try {
            ResponseUtil.noCache(response);
            response.setContentType(HttpConstants.CONTENT_TYPE_JPEG);
            String rand = randString();
            int height = 20;
            int width = 20 * WebParams.getVerifyLength();
            int charWidth = (width - height) / WebParams.getVerifyLength();
            int charHeight = 16;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Font codeFont = new Font(WebParams.getVerifyFont(), Font.CENTER_BASELINE, 18);
            Graphics g = image.getGraphics();
            g.setColor(getRandColor(10, 50));
            g.drawRect(0, 0, width - 1, height - 1);
            g.setColor(getRandColor(200, 240));
            g.fillRect(0, 0, width, height);
            g.setFont(codeFont);
            g.setColor(getRandColor(160, 200));
            for (int i = 0; i < 155; i++) {
                int x = RandomUtil.nextInt(width);
                int y = RandomUtil.nextInt(height);
                int xl = RandomUtil.nextInt(12);
                int yl = RandomUtil.nextInt(12);
                g.drawLine(x, y, x + xl, y + yl);
            }
            for (int i = 0; i < WebParams.getVerifyLength(); i++) {
                g.setColor(new Color(20 + RandomUtil.nextInt(110), 20 + RandomUtil.nextInt(110), 20 + RandomUtil.nextInt(110)));
                g.drawString(rand.substring(i, i + 1), charWidth * i + 10, charHeight);
            }
            g.dispose();
            AttributeUtil.set(request, response, key, rand);
            ImageUtil.write(image, ImageConstants.JPEG, response.getOutputStream());
        } catch (Exception e) {
            SysLog.error(e);
        }
    }

    /**
	 * 获得验证码长度
	 * @return 验证码长度
	 */
    public static final int getLength() {
        return WebParams.getVerifyLength();
    }

    /**
	 * 设置验证码长度
	 * @param length 验证码长度
	 */
    public static final void setLength(int length) {
        WebParams.setVerifyLength(length);
    }

    /**
	 * 获得验证码字符集
	 * @return 验证码字符集
	 */
    public static final String getSource() {
        return String.valueOf(WebParams.getVerifyCode());
    }

    /**
	 * 设置验证码字符集
	 * @param source 验证码字符集
	 */
    public static final void setSource(String source) {
        WebParams.setVerifyCode(source);
    }

    /**
	 * 获得保存到session中的key
	 * @return 保存到session中的key
	 */
    public static final String getKey() {
        return WebParams.getVerifyKey();
    }

    /**
	 * 设置保存到session中的key
	 * @param key 保存到session中的key
	 */
    public static final void setKey(String key) {
        WebParams.setVerifyKey(key);
    }

    /**
	 * 获得验证码
	 * @param request Request
	 * @return 验证码
	 */
    public static final String getValue(HttpServletRequest request) {
        return ConversionUtil.toString(AttributeUtil.get(request, WebParams.getVerifyKey()));
    }

    /**
	 * 获得验证码
	 * @param request Request
	 * @param response Response
	 * @return 验证码
	 */
    public static final void removeValue(HttpServletRequest request, HttpServletResponse response) {
        AttributeUtil.remove(request, response, WebParams.getVerifyKey());
    }

    /**
	 * 产生随机字符串
	 * @return
	 */
    private static String randString() {
        char[] buf = new char[WebParams.getVerifyLength()];
        char[] code = WebParams.getVerifyCode();
        for (int i = 0; i < WebParams.getVerifyLength(); i++) {
            buf[i] = code[RandomUtil.nextInt(code.length)];
        }
        return String.valueOf(buf);
    }

    /**
	 * 给定范围获得随机颜色
	 * @param fc
	 * @param bc
	 * @return
	 */
    private static Color getRandColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + RandomUtil.nextInt(bc - fc);
        int g = fc + RandomUtil.nextInt(bc - fc);
        int b = fc + RandomUtil.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
	 * 私有构造
	 */
    private VerifyCode() {
    }
}
