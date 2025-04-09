package com.handy.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.handy.util.gifencoder.AnimatedGifEncoder;
import com.handy.util.gifencoder.GifDecoder;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * (1)AnimatedGifEncoder和GifDecoder，以及这两个类涉及到的相关类，在网上搜索一下就可以找到。
 * (2)在linux系统下，如果你想支持更多系统外的字体，使用下面两句话，可以不用为系统添加字体，直接把字体文件拷贝到相应位置即可，但是需要jdk1.5环境。
 *    File file = new File(fontPath);  //字体文件
 *    Font font = Font.createFont(Font.TRUETYPE_FONT, file); //根据字体文件所在位置,创建新的字体对象
 *    如果是jdk1.5以下版本则需要为系统添加字体，因为createFont(int fontFormat, File fontFile) 
 *    这个方法，是从1.5才开始有的。
 * (3)g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 
 *    我在测试中发现，当设置的字体过大的时候，会出现很明星的锯齿，后来在网上找到了这个解决方法。
 * @author 网络收集
 *
 */
public class ImageTool {

    /**
	 * 把多张jpg图片合成一张,多张jpg图合成gif动画。
	 * 
	 * @param pic
	 *            String[] 多个jpg文件名 包含路径
	 * @param newPic
	 *            String 生成的gif文件名 包含路径
	 */
    public static void jpgToGif(String pic[], String newPic) {
        try {
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(0);
            e.start(newPic);
            BufferedImage src[] = new BufferedImage[pic.length];
            for (int i = 0; i < src.length; i++) {
                e.setDelay(200);
                src[i] = ImageIO.read(new File(pic[i]));
                e.addFrame(src[i]);
            }
            e.finish();
        } catch (Exception e) {
            System.out.println("jpgToGif Failed:");
            e.printStackTrace();
        }
    }

    /**
	 * 把gif图片按帧拆分成jpg图片
	 * 
	 * @param gifName
	 *            String 小gif图片(路径+名称)
	 * @param path
	 *            String 生成小jpg图片的路径
	 * @return String[] 返回生成小jpg图片的名称
	 */
    public static String[] splitGif(String gifName, String path) {
        try {
            GifDecoder decoder = new GifDecoder();
            decoder.read(gifName);
            int n = decoder.getFrameCount();
            String[] subPic = new String[n];
            for (int i = 0; i < n; i++) {
                BufferedImage frame = decoder.getFrame(i);
                subPic[i] = path + String.valueOf(i) + ".jpg";
                FileOutputStream out = new FileOutputStream(subPic[i]);
                ImageIO.write(frame, "jpeg", out);
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                encoder.encode(frame);
                out.flush();
                out.close();
            }
            return subPic;
        } catch (Exception e) {
            System.out.println("splitGif Failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static void createJpgByFont(String s, String jpgname) {
        createJpgByFont(s, null, null, null, null, jpgname);
    }

    /**
	 * 根据提供的文字生成jpg图片
	 * 
	 * @param s
	 *            String 文字
	 * @param fontSize
	 *            int 每个字的宽度和高度是一样的
	 * @param bgcolor
	 *            Color 背景色
	 * @param fontcolor
	 *            Color 字色
	 * @param fontPath
	 *            String 字体文件
	 * @param jpgname
	 *            String jpg图片名
	 */
    public static void createJpgByFont(String s, Integer fontSize, Color bgcolor, Color fontcolor, String fontPath, String jpgname) {
        if (fontSize == null) fontSize = 20;
        if (bgcolor == null || "".equals(bgcolor)) bgcolor = Color.WHITE;
        if (fontcolor == null || "".equals(fontcolor)) fontcolor = Color.BLACK;
        try {
            int bWidth = s.length() * fontSize + 10;
            int bHeight = fontSize + 5;
            BufferedImage bimage = new BufferedImage(bWidth, bHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bimage.createGraphics();
            g.setColor(bgcolor);
            g.fillRect(0, 0, bWidth, bHeight);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = null;
            if (fontPath == null || "".equals(fontPath)) {
                font = new Font("Serif", Font.PLAIN, fontSize);
            } else {
                File file = new File(fontPath);
                font = Font.createFont(Font.TRUETYPE_FONT, file);
            }
            g.setFont(font.deriveFont((float) fontSize));
            g.setColor(fontcolor);
            g.drawString(s, 5, fontSize);
            AffineTransform at = new AffineTransform();
            at.rotate(25);
            g.setTransform(at);
            g.dispose();
            FileOutputStream out = new FileOutputStream(jpgname);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bimage);
            param.setQuality(50f, true);
            encoder.encode(bimage, param);
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("createJpgByFont Failed!");
            e.printStackTrace();
        }
    }

    public static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
	 * 将多张小图合成一张大图。
	 * 
	 * @param smalls
	 *            小图集合(小图名)
	 * @param smallWidth
	 *            小图宽度
	 * @param smallHeight
	 *            小图高度
	 * @param cols
	 *            分多少列
	 * @param bgColor
	 *            大图背景
	 * @param picName
	 *            大图名称
	 */
    public static void createBigJPG(ArrayList<String> smalls, int smallWidth, int smallHeight, int cols, Color bgColor, String picName) {
        if (null == picName || "".equals(picName)) {
            log.info("合成大图时，大图文件名要指定。");
        }
        try {
            FileOutputStream out = new FileOutputStream(picName);
            createBigJPG(smalls, smallWidth, smallHeight, cols, bgColor, out);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    /**
	 * 将多张小图合成一张大图。
	 * @param smalls 小图集合(小图名)
	 * @param smallWidth 小图宽度
	 * @param smallHeight 小图高度
	 * @param cols 分多少列
	 * @param bgColor 大图背景
	 * @param out 输出流
	 */
    public static void createBigJPG(ArrayList<String> smalls, int smallWidth, int smallHeight, int cols, Color bgColor, OutputStream out) {
        if (null == smalls || smalls.isEmpty()) {
            log.info("没有小图，放弃合成大图");
            return;
        }
        int scount = smalls.size();
        int rows = 0;
        if (scount % cols == 0) {
            rows = smalls.size() / cols;
        } else {
            rows = smalls.size() / cols + 1;
        }
        int bigWidth = cols * smallWidth;
        int bigHeight = rows * smallHeight;
        BufferedImage bufImage = new BufferedImage(bigWidth, bigHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, smallWidth, smallHeight);
        int row = 0;
        int col = 0;
        int x = 0;
        int y = 0;
        for (String small : smalls) {
            ImageIcon icon = new ImageIcon(small);
            Image img = icon.getImage();
            x = col * smallWidth;
            y = row * smallHeight;
            g.drawImage(img, x, y, null);
            col++;
            if (log.isDebugEnabled()) {
                log.debug("x=" + x + ";y=" + y);
                log.debug(col);
            }
            if (col == cols) {
                col = 0;
                row++;
            }
        }
        g.dispose();
        try {
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImage);
            param.setQuality(10f, true);
            encoder.encode(bufImage, param);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (ImageFormatException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
	 * 给图片加水印，但不改变大小
	 * 
	 * @param strOriginalFileName
	 *            String(原始文件) 就是水印原始文件
	 * @param strWaterMarkFileName
	 *            String(水印文件)
	 */
    public void waterMark(String strOriginalFileName, String strWaterMarkFileName) {
        try {
            File fileOriginal = new File(strOriginalFileName);
            Image imageOriginal = ImageIO.read(fileOriginal);
            int widthOriginal = imageOriginal.getWidth(null);
            int heightOriginal = imageOriginal.getHeight(null);
            System.out.println("widthOriginal:" + widthOriginal + "theightOriginal:" + heightOriginal);
            BufferedImage bufImage = new BufferedImage(widthOriginal, heightOriginal, BufferedImage.TYPE_INT_RGB);
            Graphics g = bufImage.createGraphics();
            g.drawImage(imageOriginal, 0, 0, widthOriginal, heightOriginal, null);
            File fileWaterMark = new File(strWaterMarkFileName);
            Image imageWaterMark = ImageIO.read(fileWaterMark);
            int widthWaterMark = imageWaterMark.getWidth(null);
            int heightWaterMark = imageWaterMark.getHeight(null);
            System.out.println("widthWaterMark:" + widthWaterMark + "theightWaterMark:" + heightWaterMark);
            g.drawImage(imageWaterMark, widthOriginal - widthWaterMark, heightOriginal - heightWaterMark, widthWaterMark, heightWaterMark, null);
            g.dispose();
            FileOutputStream fos = new FileOutputStream(strOriginalFileName);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
            encoder.encode(bufImage);
            fos.flush();
            fos.close();
            fos = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Log log = LogFactory.getLog(ImageTool.class);

    public static void main(String[] args) {
        String s = "aebp";
        String jpgname = "d://test.jpg";
        ImageTool.createJpgByFont(s, jpgname);
    }
}
