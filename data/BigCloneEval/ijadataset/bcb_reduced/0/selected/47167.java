package org.hlj.commons.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.hlj.commons.close.CloseUtil;
import org.hlj.commons.io.file.FileUtil;
import org.hlj.log.log4j.common.SysLog;
import org.hlj.param.constants.ImageConstants;

/**
 * 对普通图片处理。
 * @author WD
 * @since JDK5
 * @version 1.0 2009-03-01
 */
public final class ImageUtil {

    private static final String fontName;

    private static Font font;

    private static Color color;

    private static String formatName;

    static {
        fontName = "宋体";
        font = new Font(fontName, Font.PLAIN, 15);
        color = Color.WHITE;
        formatName = ImageConstants.JPEG;
    }

    /**
	 * 添加文字到图片
	 * @param text 要添加的文字
	 * @param file 添加文字的图片文件
	 * @throws CustomException 写失败
	 */
    public static final void writeString(String text, File file) {
        writeString(text, file, -1, -1);
    }

    /**
	 * 添加文字到图片
	 * @param text 要添加的文字
	 * @param file 添加文字的图片文件
	 * @param x 添加位置的X轴
	 * @param y 添加位置的Y轴
	 * @throws CustomException 写失败
	 */
    public static final void writeString(String text, File file, int x, int y) {
        try {
            writeString(text, ImageIO.read(file), FileUtil.getOutputStream(file), x, y);
        } catch (Exception e) {
            SysLog.error(e);
        }
    }

    /**
	 * 添加文字到图片
	 * @param text 要添加的文字
	 * @param image 添加文字的图片对象
	 * @param out 输出流
	 * @throws CustomException 写错误
	 */
    public static final void writeString(String text, BufferedImage image, OutputStream out) {
        writeString(text, image, out, -1, -1);
    }

    /**
	 * 添加文字到图片
	 * @param text 要添加的文字
	 * @param image 添加文字的图片对象
	 * @param out 输出流 把图片输出到这个流上
	 * @param x 添加位置的X轴
	 * @param y 添加位置的Y轴
	 * @throws CustomException 写错误
	 */
    public static final void writeString(String text, BufferedImage image, OutputStream out, int x, int y) {
        Graphics g = image.getGraphics();
        g.setFont(font);
        g.setColor(color);
        if (x == -1) {
            x = (image.getWidth(null) - getStringWidth(text, g.getFontMetrics())) / 2;
        }
        if (y == -1) {
            y = (image.getHeight(null) + font.getSize()) / 2;
        }
        g.drawString(text, x, y);
        g.dispose();
        write(image, formatName, out);
    }

    /**
	 * 写图片
	 * @param image 图片对象
	 * @param formatName 图片类型
	 * @param out 输出流
	 * @throws CustomException 写错误
	 */
    public static final void write(BufferedImage image, String formatName, OutputStream out) {
        try {
            ImageIO.write(image, formatName, out);
        } catch (Exception e) {
            SysLog.error(e);
        } finally {
            CloseUtil.close(out);
        }
    }

    /**
	 * 添加图片到图片上
	 * @param draw 要添加的图片
	 * @param image 写到的图片
	 * @throws CustomException 写失败
	 */
    public static final void writeImage(File draw, File image) {
        writeImage(draw, image, -1, -1);
    }

    /**
	 * 添加图片到图片上
	 * @param draw 要添加的图片
	 * @param image 写到的图片
	 * @param x X坐标
	 * @param y Y坐标
	 * @throws CustomException 写失败
	 */
    public static final void writeImage(File draw, File image, int x, int y) {
        try {
            writeImage(ImageIO.read(draw), ImageIO.read(image), FileUtil.getOutputStream(image), x, y);
        } catch (Exception e) {
            SysLog.error(e);
        }
    }

    /**
	 * 添加图片到图片上
	 * @param draw 要添加的图片
	 * @param image 写到的图片
	 * @param out 输出流
	 * @throws CustomException 写错误
	 */
    public static final void writeImage(Image draw, BufferedImage image, OutputStream out) {
        writeImage(draw, image, out, -1, -1);
    }

    /**
	 * 添加图片到图片上
	 * @param draw 要添加的图片
	 * @param image 写到的图片
	 * @param out 输出流
	 * @param x 添加位置的X轴
	 * @param y 添加位置的Y轴
	 * @throws CustomException 写错误
	 */
    public static final void writeImage(Image draw, BufferedImage image, OutputStream out, int x, int y) {
        Graphics g = image.getGraphics();
        g.setFont(font);
        g.setColor(color);
        if (x == -1) {
            x = (image.getWidth(null) - draw.getWidth(null)) / 2;
        }
        if (y == -1) {
            y = (image.getHeight(null) - draw.getHeight(null)) / 2;
        }
        g.drawImage(draw, x, y, null);
        g.dispose();
        write(image, formatName, out);
    }

    /**
	 * 获得图片格式名
	 * @return 图片格式名
	 */
    public static final String getFormatName() {
        return formatName;
    }

    /**
	 * 设置图片格式名
	 * @param formatName 图片格式名
	 */
    public static final void setFormatName(String formatName) {
        ImageUtil.formatName = formatName;
    }

    /**
	 * 获得字体
	 * @return 字体
	 */
    public static final Font getFont() {
        return font;
    }

    /**
	 * 设置字体
	 * @param font 字体
	 */
    public static final void setFont(Font font) {
        ImageUtil.font = font;
    }

    /**
	 * 设置字体
	 * @param name 字体名称
	 * @param style 字体样式常量
	 * @param size 字体大小
	 */
    public static final void setFont(String name, int style, int size) {
        ImageUtil.font = new Font(name, style, size);
    }

    /**
	 * 获得颜色
	 * @return 颜色
	 */
    public static final Color getColor() {
        return color;
    }

    /**
	 * 设置颜色
	 * @param color 颜色
	 */
    public static final void setColor(Color color) {
        ImageUtil.color = color;
    }

    /**
	 * 设置颜色
	 * @param r 红色分量 0-255
	 * @param g 绿色分量 0-255
	 * @param b 蓝色分量 0-255
	 */
    public static final void setColor(int r, int g, int b) {
        ImageUtil.color = new Color(r, g, b);
    }

    /**
	 * 获得文字高度
	 * @param text 文字内容
	 * @param fm FontMetrics对象
	 * @return 宽度
	 */
    private static int getStringWidth(String text, FontMetrics fm) {
        int intReturn = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            intReturn += fm.charWidth(chars[i]);
        }
        return intReturn;
    }

    private ImageUtil() {
    }
}
