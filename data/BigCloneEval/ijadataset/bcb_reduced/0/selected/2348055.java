package aoetec.util.other;

import java.applet.Applet;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

public class Viewer extends Applet {

    public void init() {
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }

    public void paint(Graphics g) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("d:/curScreen.PNG"));
            g.drawImage(img, 0, 0, this);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        snap();
    }

    static void snap() throws AWTException, IOException {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.printf("width=%f, height=%f%n", d.getWidth(), d.getHeight());
        BufferedImage img = new Robot().createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
        System.out.println(ImageIO.write(img, "PNG", new File("d:/curScreen.PNG")));
    }
}

class LoadImageApplet extends Applet {

    private BufferedImage img;

    public void paint(Graphics g) {
        Socket socket = null;
        try {
            socket = new Socket("10.170.29.89", 1949);
            img = ImageIO.read(socket.getInputStream());
        } catch (IOException e) {
            g.drawString(e.getMessage(), 0, 0);
        }
        g.drawImage(img, 0, 0, null);
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(1949);
        while (true) {
            final Socket listener = serverSocket.accept();
            new Thread(new Runnable() {

                public void run() {
                    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                    System.out.printf("width=%f, height=%f%n", d.getWidth(), d.getHeight());
                    Robot robot = null;
                    try {
                        robot = new Robot();
                    } catch (AWTException e1) {
                        e1.printStackTrace();
                    }
                    while (true) {
                        try {
                            BufferedImage img = robot.createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
                            ImageIO.write(img, "PNG", listener.getOutputStream());
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    try {
                        System.out.println("close:" + listener);
                        listener.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}

class Camera {

    private String fileName;

    private String defaultName = "GuiCamera";

    static int serialNum = 0;

    private String imageFormat;

    private String defaultImageFormat = "png";

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

    /**************************************************************** 
     * 默认的文件前缀为GuiCamera，文件格式为PNG格式 
     * The default construct will use the default  
     * Image file surname "GuiCamera",  
     * and default image format "png" 
     ****************************************************************/
    public Camera() {
        fileName = defaultName;
        imageFormat = defaultImageFormat;
    }

    /**************************************************************** 
     * @param s the surname of the snapshot file 
     * @param format the format of the  image file,  
     * it can be "jpg" or "png" 
     * 本构造支持JPG和PNG文件的存储 
     ****************************************************************/
    public Camera(String s, String format) {
        fileName = s;
        imageFormat = format;
    }

    /**************************************************************** 
     * 对屏幕进行拍照 
     * snapShot the Gui once 
     ****************************************************************/
    public void snapShot() {
        try {
            BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
            serialNum++;
            String name = fileName + String.valueOf(serialNum) + "." + imageFormat;
            File f = new File(name);
            System.out.print("Save File " + name);
            ImageIO.write(screenshot, imageFormat, f);
            System.out.print("..Finished!\n");
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void main(String[] args) {
        Camera cam = new Camera("d:\\Hello", "png");
        cam.snapShot();
    }

    static void snap() {
    }
}

class Screenshot {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java Screenshot " + "WAITSECONDS OUTFILE.png");
            System.exit(1);
        }
        String outFileName = args[1];
        if (!outFileName.toLowerCase().endsWith(".png")) {
            System.err.println("Error: output file name must " + "end with \".png\".");
            System.exit(1);
        }
        try {
            long time = Long.parseLong(args[0]) * 1000L;
            System.out.println("Waiting " + (time / 1000L) + " second(s)...");
            Thread.sleep(time);
        } catch (NumberFormatException nfe) {
            System.err.println(args[0] + " does not seem to be a " + "valid number of seconds.");
            System.exit(1);
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRect);
        ImageIO.write(image, "png", new File(outFileName));
        System.out.println("Saved screen shot (" + image.getWidth() + " x " + image.getHeight() + " pixels) to file \"" + outFileName + "\".");
    }
}
