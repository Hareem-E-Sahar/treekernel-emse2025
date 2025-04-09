package net.sourceforge.tile3d.view.base;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.sourceforge.tile3d.util.Systemproperties;

public class TestImageExport implements ActionListener {

    private Robot robot;

    private JFrame viewFrame;

    public TestImageExport() {
        initUI();
        try {
            robot = new Robot();
        } catch (AWTException awte) {
            awte.printStackTrace();
        }
    }

    private void initUI() {
        JFrame frame = new JFrame("Window Image");
        Container contents = frame.getContentPane();
        contents.setLayout(new BorderLayout());
        JButton button = new JButton("Take picture");
        button.addActionListener(this);
        contents.add(button, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        viewFrame = new JFrame("ViewFrame");
        viewFrame.getContentPane().setBackground(Color.white);
        viewFrame.setSize(500, 500);
        viewFrame.setBackground(Color.BLUE);
        viewFrame.setLocationRelativeTo(null);
        viewFrame.setVisible(true);
        viewFrame.setDefaultCloseOperation(viewFrame.DO_NOTHING_ON_CLOSE);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        createImage(new JPanel());
    }

    public static void createImage(final JPanel p_panel) {
        final JFileChooser jfc = new JFileChooser();
        int option = jfc.showSaveDialog(null);
        if (option == jfc.APPROVE_OPTION) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    BufferedImage image = capture(p_panel);
                    System.out.println("image " + image);
                    File f = jfc.getSelectedFile();
                    String type = f.getName().substring(f.getName().lastIndexOf('.') + 1);
                    System.out.println("type " + type);
                    try {
                        ImageIO.write(image, type, f);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        JOptionPane.showMessageDialog(null, ioe, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
    }

    private static BufferedImage capture(JPanel p_panel) {
        Rectangle screen = p_panel.getBounds();
        Point loc = screen.getLocation();
        SwingUtilities.convertPointToScreen(loc, p_panel);
        screen.setLocation(loc);
        Robot robot;
        try {
            robot = new Robot();
            return robot.createScreenCapture(screen);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Systemproperties.getProperties("");
    }
}
