package org.ranjith.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A toolbar button UI delegate to draw tool bar
 * button like that of apple mail.
 * Heavily using TigerToolBarButtonUI of Jhon Lipsky.
 * @author ranjith
 * 
 */
public class ToolBarButtonUI extends BasicButtonUI {

    public static final int POSITION_LEFT = 0;

    public static final int POSITION_CENTER = 1;

    public static final int POSITION_RIGHT = 2;

    int position;

    private static final ToolBarButtonUI LEFTBUTTON = new ToolBarButtonUI(POSITION_LEFT);

    private static final ToolBarButtonUI CENTERBUTTON = new ToolBarButtonUI(POSITION_CENTER);

    private static final ToolBarButtonUI RIGHTBUTTON = new ToolBarButtonUI(POSITION_RIGHT);

    public ToolBarButtonUI(int position) {
        this.position = position;
    }

    public static ComponentUI createUI(JComponent b) {
        return createUI(b, POSITION_LEFT);
    }

    public static ComponentUI createUI(JComponent b, int position) {
        switch(position) {
            case POSITION_LEFT:
                return LEFTBUTTON;
            case POSITION_RIGHT:
                return RIGHTBUTTON;
            default:
                return CENTERBUTTON;
        }
    }

    protected Color getSelectColor() {
        return new Color(183, 234, 98);
    }

    public void paint(Graphics g, JComponent c) {
        AbstractButton vButton = (AbstractButton) c;
        ButtonModel vButtonModel = vButton.getModel();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = c.getWidth();
        int h = c.getHeight();
        int x = 0;
        int y = 0;
        boolean isPressed = vButtonModel.isArmed() && vButtonModel.isPressed();
        boolean isRollover = vButtonModel.isRollover();
        boolean isSelected = vButtonModel.isSelected();
        paintButtonBody(g2d, vButton, x, y, w, h, isPressed, isRollover, isSelected);
        paintButtonBorder(g2d, x, y, w, h, isPressed, isRollover, isSelected);
        paintText(g2d, vButton.getText(), vButton.getForeground(), w, h, isPressed, isRollover, isSelected);
        g2d.dispose();
    }

    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
        return;
    }

    private void paintButtonBody(Graphics2D g2, AbstractButton b, int x, int y, int w, int h, boolean isPressed, boolean isRollover, boolean isSelected) {
        Shape vOldClip = g2.getClip();
        if (position == POSITION_LEFT) {
            Shape vButtonShape = new RoundRectangle2D.Double((double) x, (double) y, (double) w + h, (double) h - 2, h - 2, h - 2);
            g2.setClip(vButtonShape);
        } else if (position == POSITION_RIGHT) {
            Shape vButtonShape = new RoundRectangle2D.Double((double) x - h - 1, (double) y, (double) w + h, (double) h - 2, h - 2, h - 2);
            g2.setClip(vButtonShape);
        } else if (position == POSITION_CENTER) {
            Shape vButtonShape = new Rectangle2D.Double((double) x, (double) y, (double) w, (double) h - 2);
            g2.setClip(vButtonShape);
        }
        Color vTopStartColor = topStartColor;
        Color vTopEndColor = topEndColor;
        Color vBottomStartColor = bottomStartColor;
        Color vBottomEndColor = bottomEndColor;
        if (isPressed || isSelected) {
            vTopStartColor = pressedTopStartColor;
            vTopEndColor = pressedTopEndColor;
            vBottomStartColor = pressedBottomStartColor;
            vBottomEndColor = pressedBottomEndColor;
        } else if (isRollover) {
            vTopStartColor = rolloverTopStartColor;
            vTopEndColor = rolloverTopEndColor;
            vBottomStartColor = rolloverBottomStartColor;
            vBottomEndColor = rolloverBottomEndColor;
        }
        Paint vTopPaint = new GradientPaint(0, y + 1, vTopStartColor, 0, y + h / 2 - 1, vTopEndColor);
        g2.setPaint(vTopPaint);
        g2.fillRect(x, y + 1, w, h / 2 - 1);
        Paint vBottomPaint = new GradientPaint(0, y + h / 2, vBottomStartColor, 0, y + h - 1, vBottomEndColor);
        g2.setPaint(vBottomPaint);
        g2.fillRect(x, y + h / 2, w, h / 2 - 1);
        g2.setClip(vOldClip);
        paintIcon(g2, b, isRollover, isSelected);
    }

    private void paintIcon(Graphics2D g2, AbstractButton b, boolean isRollover, boolean isSelected) {
        int x;
        int y;
        Icon vIcon = null;
        if (isSelected && isRollover) {
            vIcon = b.getRolloverSelectedIcon();
        } else if (isSelected) {
            vIcon = b.getSelectedIcon();
        } else if (isRollover) {
            vIcon = b.getRolloverIcon();
        }
        if (vIcon == null) {
            vIcon = b.getIcon();
        }
        if (vIcon != null) {
            x = (b.getWidth() - vIcon.getIconWidth()) / 2 + 1;
            y = (b.getHeight() - vIcon.getIconHeight()) / 2;
            Composite vComposite = g2.getComposite();
            if (!b.isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            }
            vIcon.paintIcon(b, g2, x, y);
            if (!b.isEnabled()) {
                g2.setComposite(vComposite);
            }
        }
    }

    private void paintButtonBorder(Graphics2D g2, int x, int y, int w, int h, boolean isPressed, boolean isRollover, boolean isSelected) {
        if (isRollover) {
            g2.setColor(rolloverBorderColor);
        } else if (isPressed || isSelected) {
            g2.setColor(pressedBorderColor);
        } else {
            g2.setColor(borderColor);
        }
        switch(position) {
            case POSITION_LEFT:
                g2.drawLine(x + h / 2, y, x + w, y);
                g2.drawLine(x + h / 2, y + h - 2, x + w, y + h - 2);
                g2.drawArc(x, y, h - 2, h - 2, 90, 180);
                g2.setColor(lightSeparatorColor);
                g2.drawLine(x + w - 1, y + 1, x + w - 1, y + h - 3);
                break;
            case POSITION_CENTER:
                g2.drawLine(x, y, x + w, y);
                g2.drawLine(x, y + h - 2, x + w, y + h - 2);
                g2.setColor(lightSeparatorColor);
                g2.drawLine(x + w - 1, y + 1, x + w - 1, y + h - 3);
                g2.setColor(darkSeparatorColor);
                g2.drawLine(x, y + 1, x, y + h - 3);
                break;
            case POSITION_RIGHT:
                g2.drawLine(x, y, x + w - h / 2, y);
                g2.drawLine(x, y + h - 2, x + w - h / 2, y + h - 2);
                g2.drawArc(x + w - h, y, h - 2, h - 2, 270, 180);
                g2.setColor(darkSeparatorColor);
                g2.drawLine(x, y + 1, x, y + h - 3);
                break;
            default:
                g2.drawLine(x + h / 2, y, x + w - h / 2, y);
                g2.drawLine(x + h / 2, y + h - 2, x + w - h / 2, y + h - 2);
                g2.drawArc(x, y, h - 2, h - 2, 90, 180);
                g2.drawArc(x + w - h, y, h - 2, h - 2, 270, 180);
        }
    }

    private void paintText(Graphics g, String text, Color color, int w, int h, boolean isPressed, boolean isRollover, boolean isSelected) {
        FontMetrics fm = g.getFontMetrics();
        int swid = fm.stringWidth(text);
        int shgt = fm.getHeight();
        int x = (w - swid) / 2;
        int y = (h - shgt) / 2 + fm.getAscent();
        if (isPressed) {
            y += 1;
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        g2d.setColor(color);
        int offset = 0;
        g2d.drawString(text, offset, offset);
    }

    private Color topStartColor = new Color(0xDFE3EA);

    private Color topEndColor = new Color(0xB9C3D1);

    private Color bottomStartColor = new Color(0xA7B3C5);

    private Color bottomEndColor = new Color(0xA7B3C5);

    private Color rolloverTopStartColor = new Color(0xD5DBE4);

    private Color rolloverTopEndColor = new Color(0xA2B1C4);

    private Color rolloverBottomStartColor = new Color(0x899DB5);

    private Color rolloverBottomEndColor = new Color(0xA2B8C9);

    private Color pressedTopStartColor = new Color(0xCDD2D9);

    private Color pressedTopEndColor = new Color(0x8E9AAA);

    private Color pressedBottomStartColor = new Color(0x75869E);

    private Color pressedBottomEndColor = new Color(0x97ACCB);

    private Color darkSeparatorColor = new Color(127, 138, 154, 75);

    private Color lightSeparatorColor = new Color(255, 255, 255, 25);

    private Color pressedBorderColor = new Color(0x556172);

    private Color rolloverBorderColor = new Color(0x68778C);

    private Color borderColor = new Color(0x7F8A9A);
}
