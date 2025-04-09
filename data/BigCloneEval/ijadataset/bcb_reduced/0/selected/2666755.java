package com.core.util;

import java.util.*;
import java.io.*;
import com.sun.image.codec.jpeg.*;
import java.awt.image.*;
import java.awt.*;
import javax.imageio.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 图像文件操作辅助类
 * 
 * 
 */
public class ImageUtil {

    private static Log log = LogFactory.getLog(ImageUtil.class);

    /**
	 * 通过内容生成图片
	 * 
	 * @param content
	 * @param width
	 * @param height
	 * @return
	 */
    public static InputStream generateImageInputStream(String content, int width, int height) {
        InputStream ret = null;
        ByteArrayOutputStream output = null;
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, width, height);
            g.setFont(new Font("Times New Roman", Font.PLAIN, 40));
            g.setColor(Color.DARK_GRAY);
            g.drawString(content, 1, 45);
            output = new ByteArrayOutputStream();
            ImageIO.write(image, "JPEG", output);
            byte[] bytes = output.toByteArray();
            ret = FileUtil.getInputStreamFromBytes(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    output = null;
                }
            }
        }
        return ret;
    }

    /**
	 * 压缩图片文件，重命名压缩后的图片文件名，并根据当前日期信息作为参数生成日期格式层级目录，上传压缩后的图像文件
	 * 
	 * @param file
	 * @param folder
	 * @param fileName
	 * @param width
	 * @param height
	 * @param proportion
	 *            等比压缩标志，true为等比压缩
	 * @return
	 */
    public static String compressImageAutoManage(File file, String folder, String fileName, int width, int height, boolean proportion) {
        String ret = null;
        try {
            if (file == null || folder == null || fileName == null) {
                return ret;
            }
            ret = FileUtil.createDirectoryAutoManage(folder);
            if (ret == null) {
                return ret;
            }
            fileName = FileUtil.renameFileName(fileName);
            if (fileName == null) {
                return null;
            }
            ret += FileUtil.SEPRATOR + fileName;
            if (!compressImage(file, folder + ret, width, height, proportion)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 压缩图像文件，将压缩后的图像文件上传至设置目录以及文件名
	 * 
	 * @param file
	 * @param folder
	 * @param width
	 * @param height
	 * @param proportion
	 * @return
	 */
    public static boolean compressImage(File file, String folder, int width, int height, boolean proportion) {
        boolean ret = false;
        FileOutputStream fileOutputStream = null;
        try {
            if (file == null || folder == null) {
                return ret;
            }
            fileOutputStream = new FileOutputStream(new File(folder));
            Image image = ImageIO.read(file);
            if (image.getWidth(null) == -1) {
                return ret;
            }
            int newWidth = 0;
            int newHeight = 0;
            if (image.getWidth(null) > width || image.getHeight(null) > height) {
                if (proportion) {
                    int rate1 = image.getWidth(null) / width;
                    int rate2 = image.getHeight(null) / height;
                    int rate = rate1 > rate2 ? rate1 : rate2;
                    newWidth = image.getWidth(null) / rate;
                    newHeight = image.getHeight(null) / rate;
                } else {
                    newWidth = width;
                    newHeight = height;
                }
            } else {
                newWidth = image.getWidth(null);
                newHeight = image.getHeight(null);
            }
            BufferedImage bufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            bufferedImage.getGraphics().drawImage(image.getScaledInstance(newWidth, newHeight, image.SCALE_SMOOTH), 0, 0, null);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fileOutputStream);
            encoder.encode(bufferedImage);
            fileOutputStream.close();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
	 * 给图像文件打印图片水印，水印文件以字符串形式提供
	 * 
	 * @param print
	 * @param target
	 * @param x
	 * @param y
	 * @return
	 */
    public static boolean printImage(String print, File file, int x, int y) {
        boolean ret = false;
        try {
            if (print == null || file == null) {
                return ret;
            }
            File printFile = new File(print);
            ret = printImage(printFile, file, x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 给图像文件打印图片水印，水印文件与图像文件均以字符串形式提供
	 * 
	 * @param print
	 *            图片水印文件完整路径
	 * @param target
	 * @param x
	 * @param y
	 * @return
	 */
    public static boolean printImage(String print, String target, int x, int y) {
        boolean ret = false;
        try {
            if (print == null || target == null) {
                return ret;
            }
            File printFile = new File(print);
            File file = new File(target);
            ret = printImage(printFile, file, x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 给图像文件打印图片水印
	 * 
	 * @param printFile
	 *            图片水印文件
	 * @param file
	 * @param x
	 * @param y
	 * @return
	 */
    public static boolean printImage(File printFile, File file, int x, int y) {
        boolean ret = false;
        FileOutputStream fileOutputStream = null;
        try {
            if (file == null || printFile == null) {
                return ret;
            }
            Image image = ImageIO.read(file);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bufferedImage.createGraphics();
            graphics.drawImage(image, 0, 0, width, height, null);
            Image printImage = ImageIO.read(printFile);
            int printWidth = printImage.getWidth(null);
            int printHeight = printImage.getHeight(null);
            graphics.drawImage(printImage, width - printWidth - x, height - printHeight - y, printWidth, printHeight, null);
            graphics.dispose();
            fileOutputStream = new FileOutputStream(file);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fileOutputStream);
            encoder.encode(bufferedImage);
            fileOutputStream.close();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
	 * 给图像文件打印文字水印，图像文件以字符串形式提供
	 * 
	 * @param print
	 * @param fileName
	 * @param fontName
	 * @param fontStyle
	 * @param color
	 * @param fontSize
	 * @param x
	 * @param y
	 * @return
	 */
    public static boolean printText(String print, String fileName, String fontName, int fontStyle, Color color, int fontSize, int x, int y) {
        boolean ret = false;
        try {
            if (print == null || fileName == null) {
                return ret;
            }
            File file = new File(fileName);
            ret = printText(print, file, fontName, fontStyle, color, fontSize, x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
	 * 给图像文件打印文字水印
	 * 
	 * @param print
	 * @param file
	 * @param fontName
	 * @param fontStyle
	 * @param color
	 * @param fontSize
	 * @param x
	 * @param y
	 * @return
	 */
    public static boolean printText(String print, File file, String fontName, int fontStyle, Color color, int fontSize, int x, int y) {
        boolean ret = false;
        FileOutputStream fileOutputStream = null;
        try {
            if (print == null || file == null) {
                return ret;
            }
            Image image = ImageIO.read(file);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bufferedImage.createGraphics();
            graphics.drawImage(image, 0, 0, width, height, null);
            graphics.setColor(color);
            graphics.setFont(new Font(fontName, fontStyle, fontSize));
            graphics.drawString(print, width - fontSize - x, height - fontSize / 2 - y);
            graphics.dispose();
            fileOutputStream = new FileOutputStream(file);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fileOutputStream);
            encoder.encode(bufferedImage);
            fileOutputStream.close();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
	 * 生成随机色
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
    public static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public static void main(String[] args) {
    }
}
