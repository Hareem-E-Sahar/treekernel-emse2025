import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import com.acet.nativ.NativeWindow;

public class WindowCapture {

    public static BufferedImage capture(int windowID) throws IOException {
        NativeWindow window = NativeWindow.getWindow(windowID);
        if (window == null) {
            throw new IOException("invalid window handle");
        }
        if (!window.isVisible()) {
            throw new IOException("window not visible");
        }
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new IOException(e);
        }
        BufferedImage capture = null;
        if (window.isVisible()) {
            Rectangle bounds = window.getBounds();
            Rectangle dimensions = new Rectangle(bounds.x, bounds.y, bounds.width - bounds.x, bounds.height - bounds.y);
            capture = robot.createScreenCapture(dimensions);
            BufferedImage pointer = ImageIO.read(new File("res/icons/cursor.png"));
            putPointer(dimensions, capture, pointer);
        }
        return capture;
    }

    private static void putPointer(Rectangle bounds, BufferedImage image, BufferedImage pointer) {
        Point point = MouseInfo.getPointerInfo().getLocation();
        if (bounds.contains(point.x, point.y)) {
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(pointer, null, point.x - bounds.x, point.y - bounds.y);
            g2d.dispose();
        }
    }

    static void write(BufferedImage bi, OutputStream file, float quality) throws IOException {
        ImageOutputStream out = ImageIO.createImageOutputStream(file);
        ImageWriter writer = (ImageWriter) ImageIO.getImageWritersBySuffix("jpeg").next();
        writer.setOutput(out);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        writer.write(null, new IIOImage(bi, null, null), param);
    }

    public static void main(String[] args) throws Exception {
        if (args[0] == null || args[0].trim().length() < 1) {
            throw new IllegalArgumentException("invalid string: args[0]");
        }
        int windowID = 0;
        Enumeration e = NativeWindow.getWindowList();
        while (e.hasMoreElements()) {
            NativeWindow window = (NativeWindow) e.nextElement();
            if (window.getTitle().contains(args[0])) {
                windowID = window.getWindowID();
            }
        }
        if (windowID == 0) throw new IllegalStateException("no Firefox found");
        BufferedImage capture = WindowCapture.capture(windowID);
        FileOutputStream output = new FileOutputStream(args[0] + "_capture" + ".jpg");
        write(capture, output, 0.7f);
    }
}
