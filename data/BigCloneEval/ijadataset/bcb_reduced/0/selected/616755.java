package rmi.remote.screenshot;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ScreenShot implements RobotAction {

    public Object execute(Robot robot) throws IOException {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Rectangle shotArea = new Rectangle(defaultToolkit.getScreenSize());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bout);
        encoder.encode(robot.createScreenCapture(shotArea));
        return bout.toByteArray();
    }

    public String toString() {
        return "ScreenShot";
    }
}
