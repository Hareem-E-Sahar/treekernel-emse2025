package com.jxva.graph.strategy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import com.jxva.graph.Graph;
import com.jxva.graph.GraphException;
import com.jxva.graph.SizeArgumentException;
import com.jxva.graph.watermark.WaterMarkLogo;
import com.jxva.graph.watermark.WaterMarkText;
import com.jxva.graph.watermark.WaterMarkMaker;
import com.jxva.util.FileUtil;

/**
 * 高质量图形切割,缩放,旋转抽象类
 * @author  The Jxva Framework
 * @since   1.0
 * @version 2008-11-28 17:22:29 by Jxva
 */
public abstract class AbstractGraph implements Graph, GraphStrategy {

    protected final RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    public BufferedImage cutToSquare(BufferedImage srcBi, double size) throws GraphException {
        if (size <= 0) throw new SizeArgumentException("size");
        int srcBiWidth = srcBi.getWidth();
        int srcBiHeight = srcBi.getHeight();
        if (srcBiWidth == srcBiHeight) return resize(srcBi, size, size);
        BufferedImage destBi = null;
        if (srcBiWidth > srcBiHeight) {
            int begin = (srcBiWidth - srcBiHeight) / 2;
            int end = (srcBiWidth + srcBiHeight) / 2;
            destBi = new BufferedImage(srcBiHeight, srcBiHeight, srcBi.getType());
            Graphics2D newGraphics = (Graphics2D) destBi.getGraphics();
            newGraphics.setRenderingHints(renderHints);
            newGraphics.drawImage(srcBi, 0, 0, srcBiHeight, srcBiHeight, begin, 0, end, srcBiHeight, null);
        } else {
            int begin = (srcBiHeight - srcBiWidth) / 2;
            int end = (srcBiWidth + srcBiHeight) / 2;
            destBi = new BufferedImage(srcBiWidth, srcBiWidth, srcBi.getType());
            Graphics2D newGraphics = (Graphics2D) destBi.getGraphics();
            newGraphics.setRenderingHints(renderHints);
            newGraphics.drawImage(srcBi, 0, 0, srcBiWidth, srcBiWidth, 0, begin, srcBiWidth, end, null);
        }
        return resize(destBi, size, size);
    }

    public BufferedImage zoomToHeight(BufferedImage srcBi, double height) throws GraphException {
        if (height <= 0) throw new SizeArgumentException("height");
        return resize(srcBi, srcBi.getWidth() * height / srcBi.getHeight(), height);
    }

    public BufferedImage zoomToWidth(BufferedImage srcBi, double width) throws GraphException {
        if (width <= 0) throw new SizeArgumentException("width");
        return resize(srcBi, width, srcBi.getHeight() * width / srcBi.getWidth());
    }

    public BufferedImage zoomUnStretch(BufferedImage srcBi, double width, double height) throws GraphException {
        if (width <= 0) throw new SizeArgumentException("width");
        if (height <= 0) throw new SizeArgumentException("height");
        int srcBiWidth = srcBi.getWidth();
        int srcBiHeight = srcBi.getHeight();
        if (srcBiWidth / width > srcBiHeight / height) {
            return zoomToWidth(srcBi, width);
        } else {
            return zoomToHeight(srcBi, height);
        }
    }

    public BufferedImage toGray(BufferedImage srcBi) throws GraphException {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        return new ColorConvertOp(cs, renderHints).filter(srcBi, null);
    }

    public BufferedImage zoom(BufferedImage srcBi, double scale) throws GraphException {
        return zoom(srcBi, scale, scale);
    }

    public BufferedImage zoom(BufferedImage srcBi, double sx, double sy) throws GraphException {
        return resize(srcBi, srcBi.getWidth() * sx, srcBi.getHeight() * sy);
    }

    public BufferedImage rotate(BufferedImage srcBi, double radian, Color canvas) throws GraphException {
        int biw = srcBi.getWidth();
        int bih = srcBi.getHeight();
        radian = (360 - radian % 360);
        double x;
        double y;
        if (radian <= 90) {
            x = 0;
            y = (biw * Math.sin(radian / 180 * Math.PI));
        } else if (radian <= 180) {
            x = (biw * Math.cos(radian / 180 * Math.PI)) * (-1);
            y = (biw * Math.sin(radian / 180 * Math.PI) - bih * Math.cos(radian / 180 * Math.PI));
        } else if (radian <= 270) {
            x = (biw * Math.cos(radian / 180 * Math.PI) + bih * Math.sin(radian / 180 * Math.PI)) * (-1);
            y = (bih * Math.cos(radian / 180 * Math.PI)) * (-1);
        } else {
            x = (bih * Math.sin(radian / 180 * Math.PI)) * (-1);
            y = 0;
        }
        int w = (int) Math.round(biw * Math.abs(Math.cos(radian / 180 * Math.PI)) + bih * Math.abs(Math.sin(radian / 180 * Math.PI)));
        int h = (int) Math.round(biw * Math.abs(Math.sin(radian / 180 * Math.PI)) + bih * Math.abs(Math.cos(radian / 180 * Math.PI)));
        AffineTransform affineTransform = AffineTransform.getTranslateInstance(x, y);
        BufferedImage tempBi = new BufferedImage(w, h, srcBi.getType());
        Graphics2D biContext = tempBi.createGraphics();
        biContext.setRenderingHints(renderHints);
        biContext.setColor(canvas == null ? Color.WHITE : canvas);
        biContext.fillRect(0, 0, w, h);
        biContext.drawImage(tempBi, 0, 0, null);
        affineTransform.rotate(java.lang.Math.toRadians(360 - radian));
        AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, 1);
        return affineTransformOp.filter(srcBi, tempBi);
    }

    public BufferedImage cut(BufferedImage srcBi, Rectangle rectangle) throws GraphException {
        return srcBi.getSubimage(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public boolean convert(BufferedImage srcBi, File dest) throws GraphException {
        return ioWrite(srcBi, dest);
    }

    public BufferedImage waterMarkLogo(BufferedImage srcBi, WaterMarkLogo waterMarkLogo) throws GraphException {
        return new WaterMarkMaker().make(srcBi, waterMarkLogo);
    }

    public BufferedImage waterMarkText(BufferedImage srcBi, WaterMarkText waterMarkText) throws GraphException {
        return new WaterMarkMaker().make(srcBi, waterMarkText);
    }

    public boolean compart(BufferedImage srcBi, File destDir, int rows, int cols) throws GraphException {
        return false;
    }

    public boolean ioWrite(BufferedImage srcBi, File dest) throws GraphException {
        String format = FileUtil.getFileExtensionName(dest.getName());
        try {
            return ImageIO.write(srcBi, format, dest);
        } catch (IOException e) {
            throw new GraphException(e);
        }
    }

    public boolean ioWrite(BufferedImage srcBi, File dest, float quality) throws GraphException {
        ImageOutputStream ios = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dest);
            ios = ImageIO.createImageOutputStream(fos);
            String format = FileUtil.getFileExtensionName(dest.getName());
            ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName(format).next();
            writer.setOutput(ios);
            ImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(srcBi, null, null), param);
            ios.flush();
            writer.dispose();
            return true;
        } catch (Exception e) {
            throw new GraphException(e);
        } finally {
            try {
                if (ios != null) ios.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
        }
    }
}
