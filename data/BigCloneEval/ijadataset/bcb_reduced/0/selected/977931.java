package pl.kane.autokomp.applications;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.SysexMessage;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.w3c.dom.css.RGBColor;
import pl.kane.autokomp.Template;

public class TemplateCapture implements ActionListener, MouseListener, MouseMotionListener {

    public static final int COLOR_WHITE = -2303524;

    private static TemplateCapture instance = null;

    private Point mousePosition;

    private static ImageIcon icon;

    private static BufferedImage image;

    private static JFrame frame;

    private static JLabel label;

    static JButton stopButton;

    public TemplateCapture() {
        super();
    }

    public static TemplateCapture getInstance() {
        if (instance == null) return new TemplateCapture(); else return instance;
    }

    public BufferedImage getScreen(Rectangle rect) {
        BufferedImage img = null;
        try {
            Robot robot = new Robot();
            img = robot.createScreenCapture(rect);
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            return img;
        }
    }

    private static void createAndShowGUI(TemplateCapture ak) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("AutoKomp 2.0");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addMouseListener(ak);
        frame.addMouseMotionListener(ak);
        frame.getContentPane().addMouseListener(ak);
        frame.getContentPane().addMouseMotionListener(ak);
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        JMenu aboutMenu = new JMenu("About");
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        JMenuItem newAction = new JMenuItem("New");
        JMenuItem openAction = new JMenuItem("Open");
        JMenuItem saveAction = new JMenuItem("Save");
        JMenuItem exitAction = new JMenuItem("Exit");
        fileMenu.add(newAction);
        fileMenu.add(openAction);
        fileMenu.add(saveAction);
        fileMenu.add(exitAction);
        exitAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        saveAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                int returnVal = fc.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Template template = new Template(image);
                    template.saveTemplate(fc.getSelectedFile().getPath());
                }
            }
        });
        newAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                frame.hide();
                try {
                    Dimension dim = null;
                    dim = Toolkit.getDefaultToolkit().getScreenSize();
                    Robot robot;
                    robot = new Robot();
                    robot.delay(100);
                    BufferedImage img = robot.createScreenCapture(new Rectangle(1, 1, (int) dim.getWidth(), (int) dim.getHeight()));
                    icon = new ImageIcon(img);
                    label.setIcon(icon);
                    frame.repaint();
                    label.repaint();
                } catch (AWTException e) {
                    e.printStackTrace();
                }
                frame.show();
            }
        });
        label = new JLabel("Hello World");
        stopButton = new JButton("Stop");
        stopButton.setVerticalTextPosition(AbstractButton.CENTER);
        stopButton.setHorizontalTextPosition(AbstractButton.LEADING);
        stopButton.setMnemonic(KeyEvent.VK_ESCAPE);
        stopButton.setActionCommand("stop");
        Dimension dim = null;
        dim = Toolkit.getDefaultToolkit().getScreenSize();
        icon = new ImageIcon(ak.getScreen(new Rectangle(1, 1, (int) dim.getWidth(), (int) dim.getHeight())));
        label.setIcon(icon);
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        final TemplateCapture instance = new TemplateCapture();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(instance);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if ("stop".equals(e.getActionCommand())) {
            Robot robot;
            try {
                robot = new Robot();
                robot.mouseMove(600, 400);
            } catch (AWTException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        image = this.getScreen(new Rectangle((mousePosition.x + (int) label.getLocationOnScreen().getX()), mousePosition.y + (int) label.getLocationOnScreen().getY(), 35, 35));
        icon = new ImageIcon(image);
        label.setIcon(icon);
        label.setText(label.getLocationOnScreen().toString());
        frame.repaint();
        label.repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }
}
