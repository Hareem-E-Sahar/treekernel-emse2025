package rene.zirkel.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import javax.swing.JComponent;
import rene.gui.Global;
import rene.zirkel.ZirkelFrame;
import rene.zirkel.objects.ConstructionObject;
import rene.zirkel.objects.PointObject;
import atp.sHotEqn;

public class MyGraphics13 extends MyGraphics {

    Graphics2D G;

    BasicStroke Thin, Normal, Thick, SuperThick, DCross, DCrossNormal;

    AlphaComposite C, CO;

    JComponent ZC = null;

    LatexOutput LOut;

    public MyGraphics13(final Graphics g, double factor, final JComponent zc, final LatexOutput lout) {
        LOut = lout;
        G = (Graphics2D) g;
        if (Global.getParameter("quality", true)) {
            G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            G.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            G.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            G.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            G.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        }
        final float dash[] = { (float) (factor * 5.0), (float) (factor * 5.0) };
        if (factor < Global.getParameter("minlinesize", 1.0)) {
            factor = Global.getParameter("minlinesize", 1.0);
        }
        Thin = new BasicStroke((float) (factor), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        Normal = new BasicStroke((float) (factor));
        Thick = new BasicStroke((float) (factor * 2.0), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        DCross = new BasicStroke((float) (factor * 3.0), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        DCrossNormal = new BasicStroke((float) (factor * 1.5), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        SuperThick = new BasicStroke((float) (20), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        C = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5);
        CO = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1.0);
        G.setComposite(CO);
        ZC = zc;
    }

    public MyGraphics13(final Graphics g) {
        this(g, 0.5, null, null);
    }

    public MyGraphics13(final Graphics g, final JComponent zc) {
        this(g, 0.5, zc, null);
    }

    public MyGraphics13(final Graphics g, final double factor) {
        this(g, factor, null, null);
    }

    @Override
    public void setColor(final Color c) {
        G.setColor(c);
    }

    @Override
    public void setColor(final ConstructionObject o) {
        G.setStroke(Normal);
        Color Col, LightCol, BrighterCol, BrighterLightCol;
        if (o.getSpecialColor() != null) {
            final int i = o.getConditionalColor();
            if (i != -1) {
                Col = ZirkelFrame.Colors[i];
                LightCol = ZirkelFrame.LightColors[i];
                BrighterCol = ZirkelFrame.BrighterColors[i];
                BrighterLightCol = ZirkelFrame.BrighterLightColors[i];
            } else {
                final double lambda = 0.4;
                Col = o.getSpecialColor();
                final int r = (int) (255 * (1 - lambda) + Col.getRed() * lambda);
                final int g = (int) (255 * (1 - lambda) + Col.getGreen() * lambda);
                final int b = (int) (255 * (1 - lambda) + Col.getBlue() * lambda);
                LightCol = new Color(r, g, b);
                BrighterCol = Col.brighter();
                BrighterLightCol = LightCol.brighter();
            }
        } else {
            final int i = o.getColorIndex();
            Col = ZirkelFrame.Colors[i];
            LightCol = ZirkelFrame.LightColors[i];
            BrighterCol = ZirkelFrame.BrighterColors[i];
            BrighterLightCol = ZirkelFrame.BrighterLightColors[i];
        }
        if (o.isJobTarget()) {
            setColor(ZirkelFrame.TargetColor);
        } else if (o.indicated()) {
            setColor(ZirkelFrame.IndicateColor);
        } else if (o.selected()) {
            setColor(ZirkelFrame.SelectColor);
        } else {
            if (o.getColorType() == ConstructionObject.THIN) {
                if (o.isHidden()) {
                    setColor(BrighterLightCol);
                } else {
                    setColor(LightCol);
                }
            } else {
                if (o.isHidden()) {
                    setColor(BrighterCol);
                } else {
                    setColor(Col);
                }
            }
        }
        if (o.getColorType() == ConstructionObject.THIN) {
            G.setStroke(Thin);
        } else if (o.getColorType() == ConstructionObject.THICK) {
            G.setStroke(Thick);
        } else {
            G.setStroke(Normal);
        }
    }

    @Override
    public void clearRect(final int x, final int y, final int w, final int h, final Color c) {
        G.setColor(c);
        G.fillRect(x, y, w, h);
    }

    @Override
    public void setFillColor(final ConstructionObject o) {
        G.setStroke(Normal);
        if (o.isJobTarget()) {
            setColor(ZirkelFrame.TargetColor);
        } else if ((o instanceof PointObject) && o.indicated()) {
            setColor(ZirkelFrame.IndicateColor);
        } else {
            Color Col, LightCol, BrighterCol, BrighterLightCol;
            if (o.getSpecialColor() != null) {
                final int i = o.getConditionalColor();
                if (i != -1) {
                    Col = ZirkelFrame.Colors[i];
                    LightCol = ZirkelFrame.LightColors[i];
                    BrighterCol = ZirkelFrame.BrighterColors[i];
                    BrighterLightCol = ZirkelFrame.BrighterLightColors[i];
                } else {
                    final double lambda = 0.4;
                    Col = o.getSpecialColor();
                    final int r = (int) (255 * (1 - lambda) + Col.getRed() * lambda);
                    final int g = (int) (255 * (1 - lambda) + Col.getGreen() * lambda);
                    final int b = (int) (255 * (1 - lambda) + Col.getBlue() * lambda);
                    LightCol = new Color(r, g, b);
                    BrighterCol = Col.brighter();
                    BrighterLightCol = LightCol.brighter();
                }
            } else {
                final int i = o.getColorIndex();
                Col = ZirkelFrame.Colors[i];
                LightCol = ZirkelFrame.LightColors[i];
                BrighterCol = ZirkelFrame.BrighterColors[i];
                BrighterLightCol = ZirkelFrame.BrighterLightColors[i];
            }
            if (o.getColorType() != ConstructionObject.THICK) {
                if (o.isHidden()) {
                    setColor(BrighterLightCol);
                } else {
                    setColor(LightCol);
                }
            } else {
                if (o.isHidden()) {
                    setColor(BrighterCol);
                } else {
                    setColor(Col);
                }
            }
            if (o.getColorType() == ConstructionObject.THIN) {
                G.setStroke(Thin);
            } else if (o.getColorType() == ConstructionObject.THICK) {
                G.setStroke(Thick);
            } else {
                G.setStroke(Normal);
            }
        }
    }

    @Override
    public void setLabelColor(final ConstructionObject o) {
        if (o.labelSelected()) {
            setColor(ZirkelFrame.SelectColor);
        } else if (o.isFilled()) {
            final int type = o.getColorType();
            o.setColorType(ConstructionObject.NORMAL);
            setColor(o);
            o.setColorType(type);
        } else {
            setColor(o);
        }
    }

    @Override
    public void drawRect(final double x, final double y, final double w, final double h) {
        if (test(x) || test(y) || test(x + w) || test(y + h)) {
            return;
        }
        G.setStroke(Normal);
        G.draw(new Rectangle2D.Double(x, y, w, h));
    }

    public void drawMarkerRect(final double x, final double y, final double w, final double h) {
        if (test(x) || test(y) || test(x + w) || test(y + h)) {
            return;
        }
        G.setColor(ZirkelFrame.IndicateColor);
        G.setStroke(SuperThick);
        G.draw(new Rectangle2D.Double(x, y, w, h));
        G.setStroke(Normal);
    }

    @Override
    public void drawLine(final double x, final double y, final double x1, final double y1) {
        if (test(x) || test(y) || test(x1) || test(y1)) {
            return;
        }
        G.setStroke(Normal);
        G.draw(new Line2D.Double(x, y, x1, y1));
    }

    @Override
    public void drawThickLine(final double x, final double y, final double x1, final double y1) {
        if (test(x) || test(y) || test(x1) || test(y1)) {
            return;
        }
        G.setStroke(Thick);
        G.draw(new Line2D.Double(x, y, x1, y1));
        G.setStroke(Normal);
    }

    public void drawMarkerLine(final double x, final double y, final double x1, final double y1) {
        if (test(x) || test(y) || test(x1) || test(y1)) {
            return;
        }
        G.setColor(ZirkelFrame.IndicateColor);
        G.setStroke(SuperThick);
        G.draw(new Line2D.Double(x, y, x1, y1));
        G.setStroke(Normal);
    }

    @Override
    public void drawLine(final double x, final double y, final double x1, final double y1, final ConstructionObject o) {
        if (test(x) || test(y) || test(x1) || test(y1)) {
            return;
        }
        G.draw(new Line2D.Double(x, y, x1, y1));
    }

    public boolean test(final double x) {
        return Math.abs(x) > 1e5;
    }

    @Override
    public void drawArc(final double x, final double y, final double w, final double h, final double a, final double b) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        }
        G.setStroke(Normal);
        G.draw(new Arc2D.Double(x, y, w, h, a, b, Arc2D.OPEN));
    }

    @Override
    public void drawArc(final double x, final double y, final double w, final double h, final double a, final double b, final ConstructionObject o) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        } else {
            G.draw(new Arc2D.Double(x, y, w, h, a, b, Arc2D.OPEN));
        }
    }

    @Override
    public FontMetrics getFontMetrics() {
        return G.getFontMetrics();
    }

    @Override
    public void drawString(final String s, final double x, final double y) {
        if (test(x) || test(y)) {
            return;
        }
        G.drawString(s, (float) x, (float) y);
    }

    @Override
    public void drawOval(final double x, final double y, final double w, final double h) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        }
        G.setStroke(Normal);
        G.draw(new Ellipse2D.Double(x, y, w, h));
    }

    @Override
    public void drawOval(final double x, final double y, final double w, final double h, final ConstructionObject o) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        } else {
            G.draw(new Ellipse2D.Double(x, y, w, h));
        }
    }

    @Override
    public void drawCircle(final double x, final double y, final double r, final ConstructionObject o) {
        if (r > 10 * (W + H)) {
            drawLargeCircleArc(x, y, r, 0, 360);
        } else {
            G.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
        }
    }

    public void drawMarkerArc(final double x, final double y, final double r, final double a, final double b) {
        if (test(x) || test(y) || test(r)) {
            return;
        }
        G.setColor(ZirkelFrame.IndicateColor);
        G.setStroke(SuperThick);
        G.draw(new Arc2D.Double(x - r, y - r, 2 * r, 2 * r, a, b, Arc2D.OPEN));
    }

    @Override
    public void drawCircleArc(final double x, final double y, final double r, final double a, final double b, final ConstructionObject o) {
        if (r > 10 * (W + H)) {
            drawLargeCircleArc(x, y, r, a, b);
        } else {
            G.draw(new Arc2D.Double(x - r, y - r, 2 * r, 2 * r, a, b, Arc2D.OPEN));
        }
    }

    void drawLargeCircleArc(final double x, final double y, final double r, final double a, final double b) {
        final double dw = Math.sqrt((W + H) / r / 10);
        double w = a;
        double x0 = x + r * Math.cos(w / 180 * Math.PI);
        double y0 = y - r * Math.sin(w / 180 * Math.PI);
        w = w + dw;
        while (w < a + b + dw) {
            if (w > a + b) {
                w = a + b;
            }
            final double x1 = x + r * Math.cos(w / 180 * Math.PI);
            final double y1 = y - r * Math.sin(w / 180 * Math.PI);
            final double dx = (x0 + x1) / 2, dy = (y0 + y1) / 2;
            if (Math.sqrt(dx * dx + dy * dy) <= 10 * (W + H)) {
                G.draw(new Line2D.Double(x0, y0, x1, y1));
            }
            x0 = x1;
            y0 = y1;
            w += dw;
        }
    }

    @Override
    public void fillRect(final double x, final double y, final double w, final double h, final boolean outline, final boolean transparent, final ConstructionObject o) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        }
        setFillColor(o);
        if (transparent && !o.isSolid()) {
            G.setComposite(C);
        }
        G.fill(new Rectangle2D.Double(x, y, w, h));
        if (transparent && !o.isSolid()) {
            G.setComposite(CO);
        }
        if (outline) {
            setColor(o);
            G.draw(new Rectangle2D.Double(x, y, w, h));
        }
    }

    @Override
    public void fillOval(final double x, final double y, final double w, final double h, final boolean outline, final boolean transparent, final ConstructionObject o) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        }
        if (o.getColorType() != ConstructionObject.INVISIBLE) {
            setFillColor(o);
            if (transparent && !o.isSolid()) {
                G.setComposite(C);
            }
            try {
                G.fill(new Ellipse2D.Double(x, y, w, h));
            } catch (final Exception e) {
            }
            if (transparent && !o.isSolid()) {
                G.setComposite(CO);
            }
        }
        if (outline) {
            setColor(o);
            drawOval(x, y, w, h);
        }
    }

    @Override
    public void fillArc(final double x, final double y, final double w, final double h, final double a, final double b, final boolean outline, final boolean transparent, final boolean arcb, final ConstructionObject o) {
        if (test(x) || test(y) || test(w) || test(h)) {
            return;
        }
        setFillColor(o);
        if (transparent && !o.isSolid()) {
            G.setComposite(C);
        }
        final Arc2D arc = new Arc2D.Double(x, y, w, h, a, b, arcb ? Arc2D.PIE : Arc2D.CHORD);
        G.fill(arc);
        if (transparent && !o.isSolid()) {
            G.setComposite(CO);
        }
        if (outline) {
            setColor(o);
            arc.setArcType(Arc2D.OPEN);
            G.setStroke(Normal);
            G.draw(arc);
        }
    }

    int xx[] = new int[64], yy[] = new int[64];

    @Override
    public void fillPolygon(final double x[], final double y[], final int n, final boolean outline, final boolean transparent, final ConstructionObject o) {
        if (o.getColorType() != ConstructionObject.INVISIBLE) {
            setFillColor(o);
            if (transparent && !o.isSolid()) {
                G.setComposite(C);
            }
        }
        if (n > xx.length) {
            xx = new int[n];
            yy = new int[n];
        }
        for (int i = 0; i < n; i++) {
            xx[i] = (int) (x[i]);
            if (test(x[i])) {
                return;
            }
            yy[i] = (int) (y[i]);
            if (test(y[i])) {
                return;
            }
        }
        if (o.getColorType() != ConstructionObject.INVISIBLE) {
            G.fillPolygon(xx, yy, n);
            if (transparent && !o.isSolid()) {
                G.setComposite(CO);
            }
        }
        if (outline) {
            setColor(o);
            G.setStroke(Normal);
            for (int i = 0; i < n - 1; i++) {
                drawLine(xx[i], yy[i], xx[i + 1], yy[i + 1]);
            }
            drawLine(xx[n - 1], yy[n - 1], xx[0], yy[0]);
        }
    }

    @Override
    public void fillPolygon(final double x[], final double y[], final int n, final ConstructionObject o) {
        if (n > xx.length) {
            xx = new int[n];
            yy = new int[n];
        }
        for (int i = 0; i < n; i++) {
            xx[i] = (int) (x[i]);
            if (test(x[i])) {
                return;
            }
            yy[i] = (int) (y[i]);
            if (test(y[i])) {
                return;
            }
        }
        if (o.isFilled()) {
            setFillColor(o);
            if ((o.getColorType() != ConstructionObject.THICK) && (!o.isSolid())) {
                G.setComposite(C);
            }
            G.fillPolygon(xx, yy, n);
            if ((o.getColorType() != ConstructionObject.THICK) && (!o.isSolid())) {
                G.setComposite(CO);
            }
        }
        if (!o.isFilled() || o.indicated() || o.selected() || o.getColorType() == ConstructionObject.NORMAL) {
            setColor(o);
            if (o.indicated() || o.selected()) {
                G.setStroke(Normal);
            } else {
                if (o.getColorType() == ConstructionObject.THIN) {
                    G.setStroke(Thin);
                } else if (o.getColorType() == ConstructionObject.THICK) {
                    G.setStroke(Thick);
                } else {
                    G.setStroke(Normal);
                }
            }
            for (int i = 0; i < n - 1; i++) {
                drawLine(xx[i], yy[i], xx[i + 1], yy[i + 1], o);
            }
            drawLine(xx[n - 1], yy[n - 1], xx[0], yy[0], o);
        }
    }

    @Override
    public void drawImage(final Image i, final int x, final int y, final ImageObserver o) {
        G.drawImage(i, x, y, o);
    }

    @Override
    public void drawImage(final Image i, final int x, final int y, final int w, final int h, final ImageObserver o) {
        G.drawImage(i, x, y, w, h, o);
    }

    FontStruct FS = new FontStruct();

    @Override
    public void setFont(final int h, final boolean bold) {
        Font f = FS.getFont(h, bold);
        if (f != null) {
            G.setFont(f);
        } else {
            f = new Font(Global.getParameter("font.name", "dialog"), bold ? Font.BOLD : Font.PLAIN, h);
            FS.storeFont(h, bold, f);
            G.setFont(f);
        }
    }

    int fsize;

    boolean flarge, fbold;

    int ffactor = Global.getParameter("ffactor", 130);

    @Override
    public void setDefaultFont(final int h, final boolean large, final boolean bold) {
        ffactor = Global.getParameter("ffactor", 130);
        fsize = h;
        flarge = large;
        fbold = bold;
        setFont(large, bold);
    }

    @Override
    public void setFont(final boolean large, final boolean bold) {
        int size = fsize;
        if (large) {
            size = size * ffactor / 100;
        }
        if (flarge) {
            size = size * ffactor / 100;
        }
        setFont(size, bold || fbold);
    }

    @Override
    public void drawImage(final Image i, final double x, final double y, final double x1, final double y1, final double x2, final double y2, final ImageObserver o) {
        try {
            final int w = i.getWidth(o), h = i.getHeight(o);
            final AffineTransform AT = new AffineTransform((x1 - x) / w, (y1 - y) / w, (x2 - x) / h, (y2 - y) / h, x, y);
            G.drawImage(i, AT, o);
        } catch (final Exception e) {
        }
    }

    @Override
    public Graphics getGraphics() {
        return G;
    }

    int StrH = 0, StrW = 0, StrAsc = 0;

    boolean StrTex = false;

    String Str = null;

    public void computeString(String s) {
        if (s.equals(Str)) {
            return;
        }
        StrH = StrW = StrAsc = 0;
        StrTex = false;
        Str = s;
        while (s != "") {
            int n = s.indexOf('$');
            while (n > 0 && s.charAt(n - 1) == '\\') {
                n = s.indexOf('$', n + 1);
            }
            if (n < 0) {
                StrH = Math.max(StrH, getFontMetrics().getHeight());
                StrW += getFontMetrics().stringWidth(s);
                StrAsc = Math.max(StrAsc, getFontMetrics().getAscent());
                break;
            } else {
                StrTex = true;
                String ss = s.substring(0, n);
                s = s.substring(n + 1);
                StrH = Math.max(StrH, getFontMetrics().getHeight());
                StrW += getFontMetrics().stringWidth(ss);
                StrAsc = Math.max(StrAsc, getFontMetrics().getAscent());
                n = s.indexOf('$');
                while (n > 0 && s.charAt(n - 1) == '\\') {
                    n = s.indexOf('$', n + 1);
                }
                ss = s;
                if (n >= 0) {
                    ss = s.substring(0, n);
                    s = s.substring(n + 1);
                } else {
                    s = "";
                }
                if (HE == null || !HE.getEquation().equals(ss)) {
                    setHotEqn(ss);
                }
                StrH = Math.max(StrH, heightHotEqn(G));
                StrW += widthHotEqn(G);
                StrAsc = Math.max(StrAsc, ascentHotEqn(G));
            }
        }
    }

    public int getW() {
        return StrW;
    }

    @Override
    public int stringWidth(final String s) {
        computeString(s);
        return StrW;
    }

    @Override
    public int stringHeight(final String s) {
        computeString(s);
        return StrH;
    }

    public boolean isTex(final String s) {
        computeString(s);
        return StrTex;
    }

    @Override
    public int drawStringExtended(String s, final double x, final double y) {
        if (s.startsWith("$$")) {
            s = s.substring(2);
            if (s.endsWith("$$")) {
                s = s.substring(0, s.length() - 2);
            }
            if (HE == null || !HE.getEquation().equals(s)) {
                setHotEqn(s);
            }
            if (LOut == null || !LOut.println("$$" + s + "$$", x, y + ascentHotEqn(G))) {
                return paintHotEqn((int) x, (int) y, G);
            } else {
                return heightHotEqn(G);
            }
        }
        computeString(s);
        if (LOut != null) {
            if (StrTex && LOut.printDollar()) {
                LOut.println(s, x, y + StrAsc, true);
                return StrH;
            } else if (!StrTex) {
                if (LOut.println(s, x, y + StrAsc)) {
                    return StrH;
                }
            }
        }
        int w = 0;
        while (s != "") {
            int n = s.indexOf('$');
            while (n > 0 && s.charAt(n - 1) == '\\') {
                n = s.indexOf('$', n + 1);
            }
            if (n < 0) {
                drawString(translateDollar(s), x + w, y + StrAsc);
                w += getFontMetrics().stringWidth(s);
                break;
            } else {
                String ss = s.substring(0, n);
                s = s.substring(n + 1);
                drawString(translateDollar(ss), x + w, y + StrAsc);
                w += getFontMetrics().stringWidth(ss);
                n = s.indexOf('$');
                while (n > 0 && s.charAt(n - 1) == '\\') {
                    n = s.indexOf('$', n + 1);
                }
                ss = s;
                if (n >= 0) {
                    ss = s.substring(0, n);
                    s = s.substring(n + 1);
                } else {
                    s = "";
                }
                if (HE == null || !HE.getEquation().equals(ss)) {
                    setHotEqn(ss);
                }
                paintHotEqn((int) x + w, (int) y + StrAsc - ascentHotEqn(G), G);
                w += widthHotEqn(G);
            }
        }
        StrW = w;
        return StrH;
    }

    public String translateDollar(String s) {
        int n;
        while ((n = s.indexOf("\\$")) >= 0) {
            s = s.substring(0, n) + "$" + s.substring(n + 2);
        }
        return s;
    }

    @Override
    public int stringAscent(final String s) {
        return getFontMetrics().getAscent();
    }

    sHotEqn HE = null;

    public void setHotEqn(final String s) {
        if (ZC == null) {
            return;
        }
        if (HE == null) {
            HE = new sHotEqn(ZC);
        }
        HE.setEquation(s);
    }

    public int paintHotEqn(final int c, final int r, final Graphics g) {
        if (HE == null) {
            return 0;
        }
        return HE.paint(c, r, g);
    }

    public int heightHotEqn(final Graphics g) {
        if (HE == null) {
            return 0;
        }
        return HE.getSizeof(HE.getEquation(), g).height;
    }

    public int ascentHotEqn(final Graphics g) {
        if (HE == null) {
            return 0;
        }
        return HE.getAscent(HE.getEquation(), g);
    }

    public int widthHotEqn(final Graphics g) {
        if (HE == null) {
            return 0;
        }
        return HE.getSizeof(HE.getEquation(), g).width;
    }

    @Override
    public void fillOval(final double x, final double y, final double w, final double h, final Color WithColor) {
        try {
            G.setColor(WithColor);
            G.fill(new Ellipse2D.Double(x, y, w, h));
        } catch (final Exception e) {
        }
    }

    @Override
    public void fillRect(final double x, final double y, final double w, final double h, final Color WithColor) {
        try {
            G.setColor(WithColor);
            G.fill(new Rectangle2D.Double(x, y, w, h));
        } catch (final Exception e) {
        }
    }

    @Override
    public void drawDiamond(final double x, final double y, final double w, final boolean isThick, final ConstructionObject o) {
        final double dw = w / 2;
        final int dx[] = new int[4], dy[] = new int[4];
        dx[0] = (int) (x + dw);
        dy[0] = (int) (y);
        dx[1] = (int) (x + w);
        dy[1] = (int) (y + dw);
        dx[2] = (int) (x + dw);
        dy[2] = (int) (y + w);
        dx[3] = (int) (x);
        dy[3] = (int) (y + dw);
        if (isThick) {
            setColor(o);
        } else {
            G.setColor(new Color(250, 250, 250));
        }
        G.fillPolygon(dx, dy, 4);
        if (!isThick) {
            setColor(o);
            G.drawPolygon(dx, dy, 4);
        }
    }

    @Override
    public void drawDcross(final double x, final double y, final double w, final boolean isThick, final ConstructionObject o) {
        final double x1 = x + w, y1 = y + w;
        setColor(o);
        if (isThick) {
            G.setStroke(DCross);
        } else {
            G.setStroke(DCrossNormal);
        }
        G.draw(new Line2D.Double(x, y, x1, y1));
        G.draw(new Line2D.Double(x, y1, x1, y));
        G.setStroke(Normal);
    }

    @Override
    public void setAntialiasing(final boolean bool) {
        if (bool) {
            G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    @Override
    public void drawAxisLine(final double x, final double y, final double x1, final double y1) {
        if (test(x) || test(y) || test(x1) || test(y1)) {
            return;
        }
        G.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        G.setStroke(new BasicStroke(0.05f));
        G.draw(new Line2D.Double(x, y, x1, y1));
        G.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
