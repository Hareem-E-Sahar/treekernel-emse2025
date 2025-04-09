package com.memoire.bu;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.border.Border;
import com.memoire.fu.FuLib;

/**
 * A multi-line label.
 */
public class BuLabelMultiLine extends BuLabel {

    private int pw_ = Integer.MAX_VALUE;

    public BuLabelMultiLine() {
        this(null, LEFT, TOP);
    }

    public BuLabelMultiLine(String _text) {
        this(_text, LEFT, TOP);
    }

    public BuLabelMultiLine(String _text, int _halign) {
        this(_text, _halign, TOP);
    }

    public BuLabelMultiLine(String _text, int _halign, int _valign) {
        super(_text, _halign);
        setVerticalAlignment(_valign);
    }

    /**
   * @deprecated use getPreferredWidth() instead.
   */
    protected final int getBestWidth() {
        return getPreferredWidth();
    }

    public int getPreferredWidth() {
        return pw_;
    }

    public void setPreferredWidth(int _pw) {
        pw_ = _pw;
        updateSize();
    }

    public Dimension getPreferredSize() {
        Dimension r;
        int w = getWidth();
        if (w == 0) {
            w = getPreferredWidth();
            if (w == Integer.MAX_VALUE) {
                r = super.getPreferredSize();
            } else {
                r = computeHeight(w);
                if (r.width < w) r.width = w;
            }
        } else {
            r = computeHeight(w);
        }
        return r;
    }

    protected void updateSize() {
        setPreferredSize(computeHeight(getPreferredWidth()));
    }

    public boolean isUnderlined() {
        return false;
    }

    public void setIcon(Icon _icon) {
        super.setIcon(_icon);
        updateSize();
    }

    public final void setText(String _text) {
        String text = _text;
        if (text != null) {
            text = FuLib.replace(text, "\r", "");
            text = FuLib.replace(text, "\t", "        ");
        }
        super.setText(text);
        updateSize();
    }

    public final void setBorder(Border _border) {
        super.setBorder(_border);
        updateSize();
    }

    public final void setFont(Font _font) {
        super.setFont(_font);
        updateSize();
    }

    public static final int NONE = 0;

    public static final int LETTER = 1;

    public static final int WORD = 2;

    private int wrapMode_ = NONE;

    public int getWrapMode() {
        return wrapMode_;
    }

    public void setWrapMode(int _wrapMode) {
        if (wrapMode_ != _wrapMode) {
            wrapMode_ = _wrapMode;
            updateSize();
        }
    }

    private boolean firstMode_;

    public boolean isFirstMode() {
        return firstMode_;
    }

    public void setFirstMode(boolean _firstMode) {
        if (firstMode_ != _firstMode) {
            firstMode_ = _firstMode;
            updateSize();
        }
    }

    protected void paintComponent(Graphics _g) {
        if (isOpaque()) {
            Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
            r = r.intersection(_g.getClipBounds());
            _g.setColor(getBackground());
            _g.fillRect(r.x, r.y, r.width, r.height);
        }
        Insets insets = getInsets();
        Font ft = getFont();
        String tt = getText();
        int xm = insets.left;
        int ym = insets.top;
        int wm = getWidth() - insets.left - insets.right;
        int hm = getHeight() - insets.top - insets.bottom;
        int al = getHorizontalAlignment();
        int va = getVerticalAlignment();
        switch(va) {
            case CENTER:
                {
                    int ph = Math.max(0, getHeight() - getPreferredSize().height);
                    ym += ph / 2;
                    hm -= 2 * (ph / 2);
                }
                break;
            case BOTTOM:
                {
                    int ph = Math.max(0, getHeight() - getPreferredSize().height);
                    ym += ph;
                    hm -= 2 * (ph / 2);
                }
                break;
        }
        Icon icon = getIcon();
        if (icon != null) {
            int gap = getIconTextGap();
            int wi = icon.getIconWidth();
            int hi = icon.getIconHeight();
            int hp = getHorizontalTextPosition();
            int vp = getVerticalTextPosition();
            int xi, yi;
            switch(hp) {
                case LEFT:
                    xi = xm + Math.max(0, wm - wi - gap);
                    wm -= wi + gap;
                    break;
                case CENTER:
                    xi = xm + Math.max(0, (wm - wi) / 2);
                    break;
                default:
                    xi = xm;
                    xm += wi + gap;
                    wm -= wi + gap;
                    break;
            }
            switch(vp) {
                case TOP:
                    yi = ym + Math.max(0, hm - hi - gap);
                    if (hp == CENTER) hm -= hi + gap;
                    break;
                case CENTER:
                    yi = ym + Math.max(0, (hm - hi) / 2);
                    break;
                default:
                    yi = ym;
                    if (hp == CENTER) {
                        ym += hi + gap;
                        hm -= hi + gap;
                    }
                    break;
            }
            icon.paintIcon(this, _g, xi, yi);
        }
        boolean first = firstMode_;
        if (first) ft = BuLib.deriveFont(ft, Font.BOLD, 0);
        _g.setFont(ft);
        _g.setColor(getForeground());
        FontMetrics fm = _g.getFontMetrics();
        int x = xm;
        int y = fm.getAscent() + ym;
        int hs = fm.getAscent() + fm.getDescent() - 1;
        while (!tt.equals("")) {
            int i, j;
            String s;
            i = tt.indexOf('\n');
            if (i >= 0) {
                s = tt.substring(0, i);
                tt = tt.substring(i + 1);
            } else {
                s = tt;
                tt = "";
            }
            while (!"".equals(s)) {
                int ws = 0;
                String ss = null;
                boolean forced = false;
                j = 0;
                if (getWrapMode() == WORD) {
                    for (j = s.length(); j > 0; j--) {
                        if ((j < s.length()) && !Character.isWhitespace(s.charAt(j - 1))) continue;
                        ws = BuFontChooser.stringWidthWithStyle(fm, s.substring(0, j), style_);
                        if (ws < wm) {
                            ss = s.substring(0, j);
                            s = s.substring(j).trim();
                            break;
                        }
                    }
                    if (j == 0) forced = true;
                }
                if (forced || (getWrapMode() == LETTER)) {
                    for (j = s.length(); j > 0; j--) {
                        if (Character.isWhitespace(s.charAt(j - 1))) continue;
                        ws = BuFontChooser.stringWidthWithStyle(fm, s.substring(0, j), style_);
                        if (ws < wm) {
                            ss = s.substring(0, j);
                            s = s.substring(j).trim();
                            break;
                        }
                    }
                }
                if (j == 0) {
                    ss = s;
                    s = "";
                    ws = BuFontChooser.stringWidthWithStyle(fm, ss, style_);
                }
                if (al == LEFT) x = xm; else if (al == RIGHT) x = xm + wm - ws; else x = xm + (wm - ws) / 2;
                drawSingleLine(_g, ss, x, y, ws);
                y += hs;
                if (first) {
                    first = false;
                    _g.setColor(_g.getColor().brighter());
                    _g.setFont(BuLib.deriveFont(ft, Font.PLAIN, -1));
                    fm = _g.getFontMetrics();
                    hs = fm.getAscent() + fm.getDescent() - 1;
                }
            }
        }
    }

    private int style_;

    public int getStyle() {
        return style_;
    }

    public void setStyle(int _style) {
        style_ = _style;
    }

    protected void drawSingleLine(Graphics _g, String _s, int _x, int _y, int _w) {
        Color bg = getBackground();
        if (!isOpaque()) {
            Container p = getParent();
            if (p != null) bg = p.getBackground();
        }
        BuFontChooser.drawStringWithStyle(_g, _s, _x, _y, style_ | (isUnderlined() ? BuFontChooser.UNDERLINED : 0), bg);
    }

    public Dimension computeHeight(int _width) {
        Font ft = getFont();
        if (ft == null) return new Dimension(0, 0);
        Insets insets = getInsets();
        String tt = getText();
        int xm = insets.left;
        int ym = insets.top;
        int wm = _width - insets.right - insets.left;
        int wmin = 0;
        int hmin = 0;
        Icon icon = getIcon();
        if (icon != null) {
            int gap = getIconTextGap();
            int wi = icon.getIconWidth();
            int hi = icon.getIconHeight();
            int hp = getHorizontalTextPosition();
            switch(hp) {
                case CENTER:
                    wmin = wi + insets.left + insets.right;
                    break;
                default:
                    xm += wi + gap;
                    wm -= wi + gap;
                    break;
            }
            switch(getVerticalTextPosition()) {
                case CENTER:
                    hmin = hi + insets.top + insets.bottom;
                    break;
                default:
                    if (hp == CENTER) ym += hi + gap;
                    break;
            }
        }
        FontMetrics fm = BuLib.getFontMetrics(this, ft);
        int hs = fm.getAscent() + fm.getDescent() - 1;
        int y = ym;
        int x = xm;
        while (!tt.equals("")) {
            int i, j;
            String s;
            i = tt.indexOf('\n');
            if (i >= 0) {
                s = tt.substring(0, i);
                tt = tt.substring(i + 1);
            } else {
                s = tt;
                tt = "";
            }
            while (!"".equals(s)) {
                int ws = 0;
                String ss = null;
                boolean forced = false;
                j = 0;
                if (getWrapMode() == WORD) {
                    for (j = s.length(); j > 0; j--) {
                        if ((j < s.length()) && !Character.isWhitespace(s.charAt(j - 1))) continue;
                        ws = BuFontChooser.stringWidthWithStyle(fm, s.substring(0, j), style_);
                        if (ws < wm) {
                            ss = s.substring(0, j);
                            s = s.substring(j).trim();
                            break;
                        }
                    }
                    if (j == 0) forced = true;
                }
                if (forced || (getWrapMode() == LETTER)) {
                    for (j = s.length(); j > 0; j--) {
                        if (Character.isWhitespace(s.charAt(j - 1))) continue;
                        ws = BuFontChooser.stringWidthWithStyle(fm, s.substring(0, j), style_);
                        if (ws < wm) {
                            ss = s.substring(0, j);
                            s = s.substring(j).trim();
                            break;
                        }
                    }
                }
                if (j == 0) {
                    ss = s;
                    s = "";
                    ws = BuFontChooser.stringWidthWithStyle(fm, ss, style_);
                }
                x = Math.max(x, xm + ws);
                y += hs;
            }
        }
        return new Dimension(Math.max(wmin, x + insets.right + 1), Math.max(hmin, y + insets.bottom + 1));
    }
}
