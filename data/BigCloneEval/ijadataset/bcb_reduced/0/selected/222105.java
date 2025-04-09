package org.makagiga.commons;

import java.awt.AlphaComposite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

public final class Fade {

    private static final float FADE_SPEED = 0.04f;

    private static final float FADE_START = 0.1f;

    private static final int FADE_TIMEOUT = 30;

    private boolean active;

    private BufferedImage foreground;

    private float alpha = FADE_START;

    private Image background;

    private MTimer timer;

    private WeakReference<RootPaneContainer> window;

    public Fade(final RootPaneContainer window) {
        this.window = new WeakReference<RootPaneContainer>(window);
    }

    public MPanel createContentPane(final int margin) {
        MPanel p = new MPanel() {

            @Override
            public void paint(final Graphics graphics) {
                if (active) Fade.this.paint((Graphics2D) graphics); else super.paint(graphics);
            }
        };
        p.setMargin(margin);
        return p;
    }

    public boolean isActive() {
        return active;
    }

    public void paint(final Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.drawImage(foreground, 0, 0, null);
    }

    public void start() {
        MLogger.debug("fade", "start");
        Container contentPane = getContentPane();
        if (contentPane == null) return;
        Point location = contentPane.getLocation();
        SwingUtilities.convertPointToScreen(location, contentPane);
        Dimension size = contentPane.getSize();
        Rectangle rectangle = new Rectangle(location.x, location.y, size.width, size.height);
        if (rectangle.isEmpty()) {
            MLogger.debug("fade", "Empty content pane (?)");
            return;
        }
        try {
            background = new Robot().createScreenCapture(rectangle);
        } catch (Exception exception) {
            MLogger.exception(exception);
            return;
        }
        foreground = (BufferedImage) contentPane.createImage(size.width, size.height);
        if (foreground == null) return;
        Graphics g = foreground.createGraphics();
        contentPane.paint(g);
        g.dispose();
        active = true;
        timer = new MTimer(FADE_TIMEOUT) {

            @Override
            protected void onTimeout() {
                Fade.this.doFade();
            }
        };
        timer.start();
    }

    public void stop() {
        MLogger.debug("fade", "stop");
        active = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void doFade() {
        Container contentPane = getContentPane();
        if (contentPane == null) return;
        alpha = Math.min(1.0f, alpha += FADE_SPEED);
        if (alpha == 1.0f) {
            timer.stop();
            stop();
            contentPane.repaint();
        } else {
            contentPane.repaint();
            timer.restart();
        }
    }

    private Container getContentPane() {
        RootPaneContainer rootPaneContainer = window.get();
        if (rootPaneContainer == null) return null;
        return rootPaneContainer.getContentPane();
    }
}
