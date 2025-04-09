package com.sderhy;

import tools.*;
import java.net.*;
import java.awt.*;
import java.awt.datatransfer.*;

public class PixObject implements java.io.Serializable, java.lang.Cloneable {

    static int num = 0;

    public URL url;

    public Image image, scaled;

    public String etiquette;

    protected static int stamp = 120;

    public boolean isDicom = false;

    public boolean isShowing = false;

    private boolean infoFlag = false;

    protected Canvas c;

    public int w, h;

    private int dx1, dx2, dy1, dy2;

    protected String[] DicomAttributes;

    public PixObject(URL url, Image image, Canvas c, boolean isDicom, String[] info) {
        this.url = url;
        this.c = c;
        this.image = image;
        w = image.getWidth(c);
        h = image.getHeight(c);
        scaled = getStamp();
        if (info == null) infoFlag = false; else infoFlag = true;
        this.DicomAttributes = info;
    }

    public PixObject(URL url, Image im, int num) {
        this.url = url;
        this.image = im;
        this.num = num;
        this.c = c;
        w = image.getWidth(c);
        h = image.getHeight(c);
        scaled = getStamp();
        this.isDicom = isDicom;
    }

    public Dimension getSize() {
        return new Dimension(w, h);
    }

    public static int getStampSize() {
        return stamp;
    }

    public static void setStampSize(int aSize) {
        if (aSize > 16 && aSize < 300) stamp = aSize;
    }

    public void repaint() {
        scaled = getStamp();
    }

    public void changeBackground() {
        Graphics g = scaled.getGraphics();
        g.setColor(c.getBackground());
        g.fillRect(0, 0, dx1, stamp);
        g.fillRect(0, 0, stamp, dy1);
        g.fillRect(dx2, 0, stamp, stamp);
        g.fillRect(0, dy2, stamp, stamp);
        new Rectang(stamp).paintInside(g);
    }

    public Image getStamp() {
        dx1 = 0;
        dy1 = 0;
        int hs = 0;
        int ws = 0;
        dx2 = 0;
        dy2 = 0;
        if (w >= h) {
            hs = stamp * h / w;
            ws = stamp;
            dy1 = (stamp - hs) / 2;
            dy2 = (stamp + hs) / 2;
            dx2 = stamp;
        } else if (h > w) {
            ws = stamp * w / h;
            hs = stamp;
            dx1 = (stamp - ws) / 2;
            dx2 = (stamp + ws) / 2;
            dy2 = stamp;
        }
        Image scaled = c.createImage(stamp, stamp);
        Graphics g = scaled.getGraphics();
        g.drawImage(image, dx1, dy1, dx2, dy2, 0, 0, w, h, c);
        g.setColor(c.getBackground());
        new Rectang(stamp).paintInside(g);
        return scaled;
    }

    public String[] getInfo() {
        return DicomAttributes;
    }

    public Object clone() {
        return new PixObject(url, image, num);
    }

    public void flush() {
        image.flush();
        scaled.flush();
        tools.Tools.gc("pixObject flush()");
    }
}
