package org.openmeetings.server;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.openmeetings.client.beans.ClientConnectionBean;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGDecodeParam;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author sebastianwagner
 *
 */
public class TestGZipDeltaPackage extends TestCase {

    private static Logger log = Logger.getLogger(TestGZipDeltaPackage.class);

    public TestGZipDeltaPackage(String testname) {
        super(testname);
    }

    public void testTestSocket() {
        try {
            Robot robot = new Robot();
            Rectangle screenRectangle = new Rectangle(0, 0, 20, 20);
            BufferedImage imageScreen = robot.createScreenCapture(screenRectangle);
            int scaledWidth = Math.round(400 * ClientConnectionBean.imgQuality);
            int scaledHeight = Math.round(400 * ClientConnectionBean.imgQuality);
            Image img = imageScreen.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_3BYTE_BGR);
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
            ByteArrayInputStream in = new ByteArrayInputStream(payload);
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
            BufferedImage decodedImage = decoder.decodeAsBufferedImage();
            log.debug("decodedImage W: " + decodedImage.getWidth());
            log.debug("decodedImage H: " + decodedImage.getHeight());
        } catch (Exception err) {
            log.error("[TestSocket] ", err);
        }
    }
}
