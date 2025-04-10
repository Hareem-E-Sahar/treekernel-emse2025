package org.openmeetings.client.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Logger;
import org.openmeetings.client.beans.ClientConnectionBean;
import org.openmeetings.client.beans.ClientImageFrame;
import org.openmeetings.client.beans.ClientVirtualScreenBean;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author sebastianwagner
 * 
 * 
 * 
 * This Raster does additionally check the Screen for updates
 * Plus based on the Mode does only send certain Tiles of the 
 * Raster to have a higher grade of 
 * 
 *
 */
public class ClientRasterByMode {

    private static Logger log = Logger.getLogger(ClientRasterByMode.class);

    private static final int tileWidth = 512;

    private static final int tileHeight = 512;

    public static synchronized List<ClientImageFrame> tileScreen(int mode) {
        try {
            int counter = mode;
            List<ClientImageFrame> clientImageFrames = new LinkedList<ClientImageFrame>();
            int tileNumberWidth = Double.valueOf(Math.floor(ClientVirtualScreenBean.vScreenSpinnerWidth / tileWidth)).intValue();
            int tileNumberHeight = Double.valueOf(Math.floor(ClientVirtualScreenBean.vScreenSpinnerHeight / tileHeight)).intValue();
            int xOffset = ClientVirtualScreenBean.vScreenSpinnerX;
            int yOffset = ClientVirtualScreenBean.vScreenSpinnerY;
            log.debug("tileNumberWidth,tileNumberHeight " + tileNumberWidth + "," + tileNumberHeight);
            log.debug("xOffset,yOffset " + xOffset + "," + yOffset);
            Robot robot = ClientVirtualScreenBean.robot;
            if (robot == null) robot = new Robot();
            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            for (int x = 0; x <= tileNumberWidth; x++) {
                for (int y = 0; y <= tileNumberHeight; y++) {
                    int modulo = counter % 4;
                    if (modulo == 0) {
                        int rect_x = xOffset + (x * tileWidth);
                        int rect_y = yOffset + (y * tileHeight);
                        log.debug("rect_x,rect_y,tileWidth,tileHeight " + rect_x + "," + rect_y + "," + tileWidth + "," + tileHeight);
                        int rectWidth = tileWidth;
                        int rectHeight = tileHeight;
                        if (rect_x + rectWidth > screenSize.width) {
                            rectWidth = screenSize.width - rect_x;
                        }
                        if (rect_y + rectHeight > screenSize.height) {
                            rectHeight = screenSize.height - rect_y;
                        }
                        Rectangle screenRectangle = new Rectangle(rect_x, rect_y, rectWidth, rectHeight);
                        Rectangle shrinkedRectAngle = new Rectangle(Math.round(rect_x * ClientConnectionBean.imgQuality), Math.round(rect_y * ClientConnectionBean.imgQuality), Math.round(rectWidth * ClientConnectionBean.imgQuality), Math.round(rectHeight * ClientConnectionBean.imgQuality));
                        BufferedImage imageScreen = robot.createScreenCapture(screenRectangle);
                        Image img = imageScreen.getScaledInstance(Double.valueOf(tileWidth * ClientConnectionBean.imgQuality).intValue(), Double.valueOf(tileHeight * ClientConnectionBean.imgQuality).intValue(), Image.SCALE_SMOOTH);
                        BufferedImage image = new BufferedImage(Double.valueOf(tileWidth * ClientConnectionBean.imgQuality).intValue(), Double.valueOf(tileHeight * ClientConnectionBean.imgQuality).intValue(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D biContext = image.createGraphics();
                        biContext.drawImage(img, 0, 0, null);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                        JPEGEncodeParam encpar = encoder.getDefaultJPEGEncodeParam(image);
                        encpar.setQuality(ClientConnectionBean.imgQuality, false);
                        encoder.setJPEGEncodeParam(encpar);
                        encoder.encode(image);
                        imageScreen.flush();
                        byte[] payload = out.toByteArray();
                        ByteArrayOutputStream byteGzipOut = new ByteArrayOutputStream();
                        GZIPOutputStream gZipOut = new GZIPOutputStream(byteGzipOut);
                        gZipOut.write(payload);
                        gZipOut.close();
                        log.debug("byteGzipOut LENGTH " + byteGzipOut.toByteArray().length);
                        log.debug("payload LENGTH " + payload.length);
                        log.debug("TILE x,y " + shrinkedRectAngle.x + " " + shrinkedRectAngle.y);
                        clientImageFrames.add(new ClientImageFrame(shrinkedRectAngle, byteGzipOut.toByteArray()));
                        image.flush();
                        img.flush();
                        byteGzipOut.flush();
                        gZipOut.flush();
                    }
                    counter++;
                }
            }
            return clientImageFrames;
        } catch (Exception err) {
            log.error("[checkFrame]", err);
        }
        return null;
    }
}
