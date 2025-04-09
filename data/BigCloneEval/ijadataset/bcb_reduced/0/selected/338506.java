package javatest.guiTest;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GuiTest {

    /**
     * @param args
     * @throws AWTException 
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws AWTException, IOException, InterruptedException {
        JFrame gui = new JFrame();
        JPanel panel = new JPanel();
        JButton button = new JButton();
        button.setText("print Screen");
        button.addActionListener(new PrintScreenActionListener());
        panel.add(button);
        gui.add(panel);
        gui.setSize(150, 60);
        gui.setVisible(true);
        gui.setTitle("yaha");
        while (gui.isVisible()) {
            Thread.sleep(1000);
        }
        System.exit(0);
    }
}

class PrintScreenActionListener extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 3085241282699293291L;

    private static final String IMG_TYPE = "BMP";

    @Override
    public void actionPerformed(ActionEvent pE) {
        Robot robot;
        try {
            robot = new Robot();
            robot.setAutoDelay(1000);
            BufferedImage prscrn = robot.createScreenCapture(new Rectangle(1280, 1024));
            ImageIO.write(prscrn, IMG_TYPE, new File("C:\\prscrn_" + new Date().toString().replace(":", "-") + "." + IMG_TYPE));
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
