package co.edu.unal.ungrid.graph;

import java.applet.Applet;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class Residue extends Applet implements Runnable {

    public static final long serialVersionUID = 200609220000001L;

    Thread th;

    int n = 0, m = 0;

    int sNode = 0;

    int tNode = 0;

    int flag = -1;

    Node v[];

    Edge e[];

    public void set(int flag, int n, int m, int snode, int tnode, Node v[], Edge e[]) {
        this.flag = flag;
        this.n = n;
        this.m = m;
        this.sNode = snode;
        this.tNode = tnode;
        this.v = v;
        this.e = e;
        setBackground(Color.white);
    }

    public void paintNode(Graphics g, Node n, FontMetrics fm) {
        int x = n.x;
        int y = n.y;
        int w = fm.stringWidth(n.name) + 10;
        int h = fm.getHeight() + 4;
        n.width = w;
        n.height = h;
        Color c;
        if (n.dist < 0) c = Color.gray; else c = Color.blue;
        g.setColor(c);
        g.drawRect(x - w / 2, y - h / 2, w, h);
        g.setColor(getBackground());
        g.fillRect(x - w / 2 + 1, y - h / 2 + 1, w - 1, h - 1);
        g.setColor(c);
        g.drawString(n.name, x - (w - 10) / 2, (y - (h - 4) / 2) + fm.getAscent());
    }

    int[] xy(int a, int b, int w, int h, int dx, int dy) {
        int x[] = new int[2];
        w -= ((a >= 0) ? 2 : -2) * dx - 3;
        h -= ((b >= 0) ? 2 : -2) * dy - 2;
        if (Math.abs(w * b) >= Math.abs(h * a)) {
            x[0] = ((b >= 0) ? 1 : -1) * a * h / b / 2;
            x[1] = ((b >= 0) ? 1 : -1) * h / 2;
        } else {
            x[0] = ((a >= 0) ? 1 : -1) * w / 2;
            x[1] = ((a >= 0) ? 1 : -1) * b * w / a / 2;
        }
        x[0] += dx;
        x[1] += dy;
        return x;
    }

    void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        int a = x1 - x2;
        int b = y1 - y2;
        double aa = Math.sqrt(a * a + b * b) / 16.0;
        double bb = b / aa;
        aa = a / aa;
        g.drawLine(x2, y2, x2 + (int) ((aa * 12 - bb * 5) / 13), y2 + (int) ((aa * 5 + bb * 12) / 13));
        g.drawLine(x1, y1, x2, y2);
    }

    void drawEdge(Graphics g, FontMetrics fm, int x1, int y1, int x2, int y2, int c) {
        int a = x1 - x2;
        int b = y1 - y2;
        drawArrow(g, x1, y1, x2, y2);
        int w = fm.stringWidth("" + c);
        int h = fm.getHeight();
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;
        if ((b < 0) || ((b == 0) && (a < 0))) x += fm.stringWidth(" "); else x -= w + fm.stringWidth(" ");
        if ((a > 0) || ((a == 0) && (b < 0))) y += 1; else y -= h;
        g.setColor(getBackground());
        g.fillRect(x, y, w, h);
        if (c != 0) {
            g.setColor(Color.black);
            g.drawString("" + c, x, y + fm.getAscent());
        }
    }

    public void paintEdge(Graphics g, Edge e, FontMetrics fm) {
        Node v1 = v[e.rndd_plus];
        Node v2 = v[e.rndd_minus];
        Color c;
        if ((e.st > 0) && (v1.dist < v2.dist)) c = Color.red; else if ((e.st == 0) && (v1.dist < v2.dist)) c = Color.blue; else c = Color.gray;
        int a = v1.x - v2.x;
        int b = v1.y - v2.y;
        double aa = Math.sqrt(a * a + b * b) / 2.0;
        double bb = -b / aa;
        aa = a / aa;
        int x1[] = xy(-a, -b, v1.width, v1.height, (int) bb, (int) aa);
        int x2[] = xy(a, b, v2.width, v2.height, (int) bb, (int) aa);
        if (e.capacity - e.flow == 0) g.setColor(getBackground()); else g.setColor(c);
        drawEdge(g, fm, v1.x + x1[0], v1.y + x1[1], v2.x + x2[0], v2.y + x2[1], e.capacity - e.flow);
        if ((e.st > 0) && (v1.dist > v2.dist)) c = Color.red; else if ((e.st == 0) && (v1.dist > v2.dist)) c = Color.blue; else c = Color.gray;
        x1 = xy(a, b, v2.width, v2.height, -(int) bb, -(int) aa);
        x2 = xy(-a, -b, v1.width, v1.height, -(int) bb, -(int) aa);
        if (e.flow == 0) g.setColor(getBackground()); else g.setColor(c);
        drawEdge(g, fm, v2.x + x1[0], v2.y + x1[1], v1.x + x2[0], v1.y + x2[1], e.flow);
    }

    public void paint(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < n; i++) paintNode(g, v[i], fm);
        for (int i = 0; i < m; i++) paintEdge(g, e[i], fm);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void start() {
        if (th == null) {
            th = new Thread(this);
            th.start();
        }
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            if (flag-- > 0) repaint();
        }
    }
}
