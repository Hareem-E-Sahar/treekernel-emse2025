package org.iwidget.desktop.ui;

import org.iwidget.desktop.core.IwidgetRepository;
import org.iwidget.desktop.model.AboutElement;
import org.iwidget.desktop.model.WidgetElement;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

/**
 *
 * @author Muhammad Hakim A
 */
public class AboutFrame extends JDialog implements MouseListener {

    public AboutFrame(WidgetElement widget, AboutElement aboutData) {
        super(new Frame());
        this.widget = widget;
        imageList = aboutData.getImages();
        imageCounter = 0;
        if (widget == null) {
            imageList.add("./images/about.png");
        }
        setUndecorated(true);
        initFrame();
        addMouseListener(this);
    }

    private void initFrame() {
        setVisible(false);
        ImageIcon image;
        if (widget != null) {
            byte bytes[] = IwidgetRepository.getInstance().getObject(widget.getName(), (String) imageList.get(imageCounter));
            image = new ImageIcon(bytes);
            bytes = null;
        } else {
            image = new ImageIcon((String) imageList.get(imageCounter));
        }
        graphic = image.getImage();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width / 2 - image.getIconWidth() / 2;
        int y = screen.height / 2 - image.getIconHeight() / 2;
        setLocation(x, y);
        setSize(image.getIconWidth(), image.getIconHeight());
        takePhoto(new Rectangle(x, y, image.getIconWidth(), image.getIconHeight()));
        setVisible(true);
    }

    private void takePhoto(Rectangle rect) {
        try {
            Robot robot = new Robot();
            img = robot.createScreenCapture(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        if (isVisible()) try {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(img, 0, 0, this);
            g2.drawImage(graphic, 0, 0, this);
            if (imageCounter == 0 && widget == null) {
                String string = "1.0.0";
                g2.setFont(new Font("Helv", 1, 12));
                g2.setColor(Color.GRAY);
                g2.drawString(string, 239, 149);
                g2.setColor(Color.WHITE);
                g2.drawString(string, 238, 148);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } else super.repaint();
    }

    public void mousePressed(MouseEvent e) {
        imageCounter++;
        if (imageCounter < imageList.size()) {
            initFrame();
        } else {
            dispose();
            img = null;
            graphic = null;
            imageList = null;
            aboutData = null;
            widget = null;
            System.gc();
        }
    }

    public void mouseReleased(MouseEvent mouseevent) {
    }

    public void mouseClicked(MouseEvent mouseevent) {
    }

    public void mouseEntered(MouseEvent mouseevent) {
    }

    public void mouseExited(MouseEvent mouseevent) {
    }

    private static final long serialVersionUID = 0x3739373430383031L;

    private WidgetElement widget;

    private AboutElement aboutData;

    private Image graphic;

    private BufferedImage img;

    private ArrayList imageList;

    private int imageCounter;
}
