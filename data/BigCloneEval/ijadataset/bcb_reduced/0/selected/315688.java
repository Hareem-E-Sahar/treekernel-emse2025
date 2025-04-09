package computerlaboratorymanager.server;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.imageio.ImageIO;

public class DeskImageServer implements Runnable {

    private int port = 881;

    private Robot robot = null;

    Rectangle size = null;

    public DeskImageServer(int port) throws AWTException {
        this.port = port;
        this.robot = new Robot();
        this.robot.delay(0);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        size = new Rectangle(0, 0, screenSize.width, screenSize.height);
    }

    public void run() {
        ServerSocket sock = null;
        OutputStream os = null;
        try {
            sock = new ServerSocket();
            sock.bind(new InetSocketAddress(port));
            sock.setSoTimeout(500);
            sock.setPerformancePreferences(1, 1, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (RemoteServer.isRunning()) {
            Socket client = null;
            try {
                client = sock.accept();
                os = client.getOutputStream();
            } catch (SocketTimeoutException se) {
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (RemoteServer.isRunning()) {
                    BufferedImage image = robot.createScreenCapture(size);
                    ImageIO.write(image, "jpeg", os);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
