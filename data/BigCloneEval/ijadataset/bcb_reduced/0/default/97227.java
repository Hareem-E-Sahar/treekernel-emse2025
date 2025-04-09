import java.io.*;
import java.awt.*;
import javax.imageio.*;
import java.awt.image.*;
import utils.MiscUtils;

public class SnapD {

    String filePath;

    String fileType;

    public SnapD() {
        MiscUtils utils = new MiscUtils();
        filePath = utils.getCaptureFileName();
        fileType = "png";
        capture();
    }

    public void capture() {
        filePath = filePath + "." + fileType;
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            ImageIO.write(image, fileType, new File(filePath));
        } catch (Exception ex) {
            System.out.println("ERROR : " + ex);
        }
    }

    public static void main(String[] args) {
        SnapD snap = new SnapD();
    }
}
