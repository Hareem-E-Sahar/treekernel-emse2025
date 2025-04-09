package org.wcb.gui.effect;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class Dissolver extends JComponent implements Runnable {

    protected Frame frame;

    protected Window fullscreen;

    protected int count;

    protected BufferedImage frameBuffer;

    protected BufferedImage screenBuffer;

    private Logger LOG = Logger.getLogger(Dissolver.class.getName());

    public void dissolveExit(JFrame frame) {
        try {
            this.frame = frame;
            Robot robot = new Robot();
            Rectangle frame_rectangle = frame.getBounds();
            frameBuffer = robot.createScreenCapture(frame_rectangle);
            frame.setVisible(false);
            Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screen_rect = new Rectangle(0, 0, screensize.width, screensize.height);
            screenBuffer = robot.createScreenCapture(screen_rect);
            fullscreen = new Window(new JFrame());
            fullscreen.setSize(screensize);
            fullscreen.add(this);
            this.setSize(screensize);
            fullscreen.setVisible(true);
            new Thread(this).start();
        } catch (AWTException awt) {
            LOG.log(Level.WARNING, "Dissolve problem ", awt);
        }
    }

    /**
	* Run the dissolver.
	*/
    public void run() {
        count = 0;
        try {
            Thread.currentThread().sleep(100);
            for (int i = 0; i < 20; i++) {
                count = i;
                fullscreen.repaint();
                Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        System.exit(0);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.drawImage(screenBuffer, -fullscreen.getX(), -fullscreen.getY(), null);
        Composite old_comp = g2.getComposite();
        Composite fade = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - ((float) count) / 20f);
        g2.setComposite(fade);
        g2.drawImage(frameBuffer, frame.getX(), frame.getY(), null);
        g2.setComposite(old_comp);
    }
}
