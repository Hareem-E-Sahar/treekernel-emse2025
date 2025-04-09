package net.sf.pegasos.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;
import javax.swing.JLabel;

public class JAdvancedLabel extends JLabel {

    /**
     * 
     */
    private static final long serialVersionUID = 2527360488997915844L;

    public static final int EFFECT_PLAIN = 0;

    public static final int EFFECT_GRADIENT = 1;

    public static final int EFFECT_IMAGE = 2;

    public static final int EFFECT_IMAGE_ANIMATION = 3;

    public static final int EFFECT_COLOR_ANIMATION = 4;

    protected int effectIndex = EFFECT_PLAIN;

    protected double shearFactor = 0.0;

    protected Color outlineColor;

    protected Stroke stroke;

    protected GradientPaint gradient;

    protected Image foregroundImage;

    protected Thread animator;

    protected boolean isRunning = false;

    protected int m_delay;

    protected int m_xShift;

    protected int alpha = 0;

    public JAdvancedLabel() {
        super();
    }

    public JAdvancedLabel(String text) {
        super(text);
    }

    public JAdvancedLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
    }

    public JAdvancedLabel(Icon image) {
        super(image);
    }

    public JAdvancedLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    public JAdvancedLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }

    public void setEffectIndex(int e) {
        effectIndex = e;
        repaint();
    }

    public int getEffectIndex() {
        return effectIndex;
    }

    public void setShearFactor(double s) {
        shearFactor = s;
        repaint();
    }

    public double getShearFactor() {
        return shearFactor;
    }

    public void setOutlineColor(Color c) {
        outlineColor = c;
        repaint();
    }

    public Color getOutlineColor() {
        return outlineColor;
    }

    public void setStroke(Stroke s) {
        stroke = s;
        repaint();
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setGradient(GradientPaint g) {
        gradient = g;
        repaint();
    }

    public GradientPaint getGradient() {
        return gradient;
    }

    public void setForegroundImage(Image img) {
        foregroundImage = img;
        repaint();
    }

    public Image getForegroundImage() {
        return foregroundImage;
    }

    public void startAnimation(int delay) {
        if (animator != null) {
            return;
        }
        m_delay = delay;
        m_xShift = 0;
        isRunning = true;
        animator = new Thread() {

            double arg = 0;

            public void run() {
                while (isRunning) {
                    if (effectIndex == EFFECT_IMAGE_ANIMATION) {
                        m_xShift += 10;
                    } else if (effectIndex == EFFECT_COLOR_ANIMATION && gradient != null) {
                        arg += Math.PI / 10;
                        double cos = Math.cos(arg);
                        double f1 = (1 + cos) / 2;
                        double f2 = (1 - cos) / 2;
                        arg = arg % (Math.PI * 2);
                        Color c1 = gradient.getColor1();
                        Color c2 = gradient.getColor2();
                        int r = (int) (c1.getRed() * f1 + c2.getRed() * f2);
                        r = Math.min(Math.max(r, 0), 255);
                        int g = (int) (c1.getGreen() * f1 + c2.getGreen() * f2);
                        g = Math.min(Math.max(g, 0), 255);
                        int b = (int) (c1.getBlue() * f1 + c2.getBlue() * f2);
                        b = Math.min(Math.max(b, 0), 255);
                        setForeground(new Color(r, g, b));
                    }
                    repaint();
                    try {
                        sleep(m_delay);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        };
        animator.start();
    }

    public void stopAnimation() {
        isRunning = false;
        animator = null;
    }

    protected void fillByImage(Graphics2D g2, Shape shape, int xOffset) {
        if (foregroundImage == null) {
            return;
        }
        int wImg = foregroundImage.getWidth(this);
        int hImg = foregroundImage.getHeight(this);
        if (wImg <= 0 || hImg <= 0) {
            return;
        }
        g2.setClip(shape);
        Rectangle bounds = shape.getBounds();
        for (int xx = bounds.x + xOffset; xx < bounds.x + bounds.width; xx += wImg) {
            for (int yy = bounds.y; yy < bounds.y + bounds.height; yy += hImg) {
                g2.drawImage(foregroundImage, xx, yy, this);
            }
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        this.setFont(this.getFont().deriveFont(height));
    }

    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        this.setFont(this.getFont().deriveFont(r.height / 2));
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        this.setFont(this.getFont().deriveFont(d.height / 2));
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.setFont(this.getFont().deriveFont(height / 2));
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        if (alpha > 255) {
            this.alpha = 255;
        } else if (alpha < 0) {
            this.alpha = 0;
        } else {
            this.alpha = alpha;
        }
        Color foregroundColor = this.getForeground();
        this.setForeground(new Color(foregroundColor.getRed(), foregroundColor.getGreen(), foregroundColor.getBlue(), this.alpha));
        this.outlineColor = new Color(this.outlineColor.getRed(), this.outlineColor.getGreen(), this.outlineColor.getBlue(), this.alpha);
    }
}
