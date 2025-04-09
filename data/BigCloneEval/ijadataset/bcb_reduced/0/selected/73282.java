package vncserver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.awt.Robot;
import java.net.Socket;
import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author manish
 */
public class Handler {

    private MouseHandler mouseHandler;

    private KeyboardHandler keyboardHandler;

    private BufferedImage image;

    public Robot robot;

    private String command;

    private int x, y, button;

    private boolean isMouseInput;

    private int keyCode;

    private String typedString = "";

    private Thread processThread;

    private Socket connection;

    private ObjectInputStream input;

    private ObjectOutputStream output;

    public Handler(Socket connection) {
        this.connection = connection;
        try {
            robot = new Robot();
        } catch (Exception e) {
            System.err.println("Error in creating Robot");
        }
        mouseHandler = new MouseHandler(robot);
        keyboardHandler = new KeyboardHandler(robot);
        processThread = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    processCommand();
                }
            }
        });
        processThread.start();
    }

    private void sendScreenshot() {
        try {
            robot.delay(300);
            output.flush();
            image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            File file = new File(".VNCserver.gif");
            file.createNewFile();
            FileOutputStream fileout = new FileOutputStream(file);
            ImageIO.write(image, "gif", fileout);
            fileout.flush();
            fileout.close();
            byte[] imageBytes = new byte[(int) file.length()];
            FileInputStream filein = new FileInputStream(file);
            filein.read(imageBytes);
            output.writeObject(imageBytes);
            output.flush();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            output.close();
            input.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processCommand() {
        command = "";
        x = y = button = 0;
        isMouseInput = false;
        keyCode = 0;
        typedString = "";
        try {
            isMouseInput = input.readBoolean();
            command = (String) input.readObject();
            if (isMouseInput) {
                x = input.readInt();
                y = input.readInt();
                button = input.readInt();
                if (command.equals("MCLK")) {
                    mouseHandler.mouseClicked(x, y, button);
                } else if (command.equals("MPRS")) {
                    mouseHandler.mousePressed(x, y, button);
                } else if (command.equals("MRLS")) {
                    mouseHandler.mouseReleased(x, y, button);
                }
            } else {
                if (command.equals("EXIT")) {
                    connection.close();
                }
                if (command.equals("TYPE")) {
                    typedString = (String) input.readObject();
                    robot.delay(2000);
                    keyboardHandler.typeString(typedString);
                }
                if (command.equals("RFSH")) {
                }
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        sendScreenshot();
    }
}
