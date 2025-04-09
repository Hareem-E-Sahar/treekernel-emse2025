package org.glossitope.container.actions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.TextPainter;
import org.jdesktop.swingx.painter.effects.NeonBorderEffect;
import org.joshy.util.u;

/**
 *
 * @author joshy
 */
public class ShowSplashscreen {

    /** Creates a new instance of ShowSplashscreen */
    public ShowSplashscreen() {
    }

    public void show() {
        final JFrame frame = new JFrame();
        frame.setUndecorated(true);
        try {
            Robot robot = new Robot();
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            int x = 0;
            int y = 0;
            final BufferedImage img = robot.createScreenCapture(new Rectangle(x, y, size.width, size.height));
            final JXPanel panel = new JXPanel();
            Painter painter = new AbstractPainter<JXPanel>() {

                protected void doPaint(Graphics2D g, JXPanel component, int width, int height) {
                    Point origin = new Point(0, 0);
                    SwingUtilities.convertPointFromScreen(origin, component);
                    g.drawImage(img, origin.x, origin.y, null);
                }
            };
            SplashAnim splash = new SplashAnim();
            CompoundPainter rotator = new CompoundPainter(splash);
            TextPainter text = new TextPainter("org.glossitope");
            text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 160));
            text.setVerticalAlignment(TextPainter.VerticalAlignment.TOP);
            text.setFillPaint(Color.BLACK);
            text.setBorderPaint(Color.BLACK);
            text.setAreaEffects(new NeonBorderEffect());
            text.setStyle(TextPainter.Style.OUTLINE);
            panel.setBackgroundPainter(new CompoundPainter(painter, splash, text));
            frame.add(panel);
            frame.pack();
            frame.setBackground(new Color(0, 0, 0, 0));
            frame.setSize(500, 500);
            frame.setLocation(300, 100);
            frame.setVisible(true);
            Animator anim = PropertySetter.createAnimator(1500, splash, "rotation", 0, 180, 90, 180 + 90, 180, 360, 360 - 90);
            anim.addTarget(new TimingTargetAdapter() {

                public void timingEvent(float f) {
                    panel.repaint();
                }
            });
            anim.addTarget(new PropertySetter(splash, "color", Color.BLACK, Color.RED, Color.BLACK));
            anim.addTarget(new PropertySetter(text, "fillPaint", Color.WHITE, Color.BLACK, Color.WHITE));
            anim.setRepeatCount(1);
            anim.addTarget(new TimingTargetAdapter() {

                public void end() {
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
            anim.start();
        } catch (Exception ex) {
            u.p(ex);
        }
    }

    public class SplashAnim extends AbstractPainter {

        private double rotation;

        private Color color;

        protected void doPaint(Graphics2D g, Object component, int width, int height) {
            g = (Graphics2D) g.create();
            g.setColor(getColor());
            g.translate(width / 2, height / 2);
            g.rotate(rotation * Math.PI * 2.0 / 360.0);
            g.fillRect(10, 10, 50, 50);
            g.fillRect(10, -60, 50, 50);
            g.fillRect(-60, -60, 50, 50);
            g.fillRect(-60, 10, 50, 50);
            g.setStroke(new BasicStroke(3f));
            g.setColor(Color.BLACK);
            g.drawRect(10, 10, 50, 50);
            g.drawRect(10, -60, 50, 50);
            g.drawRect(-60, -60, 50, 50);
            g.drawRect(-60, 10, 50, 50);
            g.dispose();
        }

        public double getRotation() {
            return rotation;
        }

        public void setRotation(double rotation) {
            this.rotation = rotation;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }
}
