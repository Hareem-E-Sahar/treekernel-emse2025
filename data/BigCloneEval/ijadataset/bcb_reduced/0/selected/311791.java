package druid.core.er;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

public class ErLink {

    public static final int SHAPE_SIZE = 13;

    public static final int SHAPE_NONE = 0;

    public static final int SHAPE_BALL_FILLED = 1;

    public static final int SHAPE_BALL_EMPTY = 2;

    public static final int SHAPE_ROAR_FILLED = 3;

    public static final int SHAPE_ROAR_EMPTY = 4;

    public static final int STYLE_NORMAL = 0;

    public static final int STYLE_DASHED = 1;

    private int iStartingShape;

    private int iEndingShape;

    private int iLineStyle;

    private ErScrEntity scrEntStart;

    private ErScrEntity scrEntEnd;

    private Rectangle rcStart;

    private Rectangle rcEnd;

    private IntersPoint ipStart = new IntersPoint();

    private IntersPoint ipEnd = new IntersPoint();

    private int aRoarX[] = new int[4];

    private int aRoarY[] = new int[4];

    public ErLink(ErScrEntity start, ErScrEntity end, int startSh, int endSh, int style) {
        scrEntStart = start;
        scrEntEnd = end;
        rcStart = start.getBounds();
        rcEnd = end.getBounds();
        iStartingShape = startSh;
        iEndingShape = endSh;
        iLineStyle = style;
    }

    public ErScrEntity getStartEntity() {
        return scrEntStart;
    }

    public ErScrEntity getEndEntity() {
        return scrEntEnd;
    }

    public int getDistanceFromPoint(int x, int y) {
        float x0 = rcStart.x + rcStart.width / 2;
        float y0 = rcStart.y + rcStart.height / 2;
        float x1 = rcEnd.x + rcEnd.width / 2;
        float y1 = rcEnd.y + rcEnd.height / 2;
        float dx = x1 - x0;
        float dy = y1 - y0;
        float t = ((x - x0) * dx + (y - y0) * dy) / (dx * dx + dy * dy);
        if (t < 0 || t > 1) return -1;
        float px = x0 + t * dx;
        float py = y0 + t * dy;
        return (int) Math.sqrt((px - x) * (px - x) + (py - y) * (py - y));
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int sx = rcStart.x + rcStart.width / 2;
        int sy = rcStart.y + rcStart.height / 2;
        int ex = rcEnd.x + rcEnd.width / 2;
        int ey = rcEnd.y + rcEnd.height / 2;
        int dx = sx - ex;
        int dy = sy - ey;
        int lineLen = (int) Math.sqrt(dx * dx + dy * dy);
        int minW = Math.min(rcStart.width, rcStart.height);
        int minH = Math.min(rcEnd.width, rcEnd.height);
        if (lineLen < (minW + minH) / 2) return;
        calcIntersection(rcStart, sx, sy, ex, ey, ipStart);
        calcIntersection(rcEnd, ex, ey, sx, sy, ipEnd);
        int stImgX = 0;
        int stImgY = 0;
        int enImgX = 0;
        int enImgY = 0;
        if (iStartingShape == SHAPE_NONE) {
            sx = ipStart.x;
            sy = ipStart.y;
        } else {
            stImgX = ipStart.x + (SHAPE_SIZE * ipStart.dX) / 2;
            stImgY = ipStart.y + (SHAPE_SIZE * ipStart.dY) / 2;
            sx = stImgX + SHAPE_SIZE / 2;
            sy = stImgY + SHAPE_SIZE / 2;
        }
        if (iEndingShape == SHAPE_NONE) {
            ex = ipEnd.x;
            ey = ipEnd.y;
        } else {
            enImgX = ipEnd.x + (SHAPE_SIZE * ipEnd.dX) / 2;
            enImgY = ipEnd.y + (SHAPE_SIZE * ipEnd.dY) / 2;
            ex = enImgX + SHAPE_SIZE / 2;
            ey = enImgY + SHAPE_SIZE / 2;
        }
        g.setColor(Color.black);
        if (iLineStyle == STYLE_NORMAL) {
            g.drawLine(sx, sy, ex, ey);
        } else {
            dx = ex - sx;
            dy = ey - sy;
            lineLen = ((int) Math.sqrt(dx * dx + dy * dy)) >> 3;
            float t1;
            float t2;
            float x1;
            float y1;
            float x2;
            float y2;
            Line2D.Float line = new Line2D.Float();
            for (int i = 0; i < lineLen; i++) {
                t1 = i / ((float) lineLen);
                t2 = (i + 1) / ((float) lineLen);
                x1 = sx + t1 * dx;
                y1 = sy + t1 * dy;
                x2 = sx + t2 * dx;
                y2 = sy + t2 * dy;
                line.x1 = x1;
                line.y1 = y1;
                line.x2 = (x1 + x2) / 2;
                line.y2 = (y1 + y2) / 2;
                g2d.draw(line);
            }
        }
        drawShape(g2d, iStartingShape, stImgX, stImgY);
        drawShape(g2d, iEndingShape, enImgX, enImgY);
    }

    private void calcIntersection(Rectangle r, int srcX, int srcY, int desX, int desY, IntersPoint p) {
        if (srcX == desX) {
            p.x = srcX;
            p.dX = -1;
            if (srcY < desY) {
                p.y = r.y + r.height;
                p.dY = 0;
            } else {
                p.y = r.y - 1;
                p.dY = -2;
            }
        } else {
            int dX = desX - srcX;
            int dY = desY - srcY;
            float srcM = ((float) r.height) / ((float) r.width);
            float linM = -(((float) dY) / ((float) dX));
            if (srcX < desX) {
                if (linM > srcM) {
                    p.y = r.y - 1;
                    p.x = srcX + (p.y - srcY) * dX / dY;
                    p.dX = -1;
                    p.dY = -2;
                } else if ((linM <= srcM) && (-srcM <= linM)) {
                    p.x = r.x + r.width;
                    p.y = srcY + (p.x - srcX) * dY / dX;
                    p.dX = 0;
                    p.dY = -1;
                } else {
                    p.y = r.y + r.height;
                    p.x = srcX + (p.y - srcY) * dX / dY;
                    p.dX = -1;
                    p.dY = 0;
                }
            } else {
                linM = -linM;
                if (linM > srcM) {
                    p.y = r.y - 1;
                    p.x = srcX + (p.y - srcY) * dX / dY;
                    p.dX = -1;
                    p.dY = -2;
                } else if ((linM <= srcM) && (-srcM <= linM)) {
                    p.x = r.x - 1;
                    p.y = srcY + (p.x - srcX) * dY / dX;
                    p.dX = -2;
                    p.dY = -1;
                } else {
                    p.y = r.y + r.height;
                    p.x = srcX + (p.y - srcY) * dX / dY;
                    p.dX = -1;
                    p.dY = 0;
                }
            }
        }
    }

    private void drawShape(Graphics2D g2d, int shape, int x, int y) {
        if (shape == SHAPE_BALL_EMPTY) {
            g2d.setColor(Color.white);
            g2d.fillOval(x, y, SHAPE_SIZE, SHAPE_SIZE);
            g2d.setColor(Color.black);
            g2d.drawOval(x, y, SHAPE_SIZE, SHAPE_SIZE);
        } else if (shape == SHAPE_BALL_FILLED) {
            g2d.setColor(Color.black);
            g2d.fillOval(x, y, SHAPE_SIZE, SHAPE_SIZE);
        } else if (shape == SHAPE_ROAR_EMPTY) {
            aRoarX[0] = x + SHAPE_SIZE / 2;
            aRoarX[1] = x + SHAPE_SIZE - 1;
            aRoarX[2] = x + SHAPE_SIZE / 2;
            aRoarX[3] = x;
            aRoarY[0] = y;
            aRoarY[1] = y + SHAPE_SIZE / 2;
            aRoarY[2] = y + SHAPE_SIZE - 1;
            aRoarY[3] = y + SHAPE_SIZE / 2;
            g2d.setColor(Color.white);
            g2d.fillPolygon(aRoarX, aRoarY, aRoarX.length);
            g2d.setColor(Color.black);
            g2d.drawPolygon(aRoarX, aRoarY, aRoarX.length);
        } else if (shape == SHAPE_ROAR_FILLED) {
            aRoarX[0] = x + SHAPE_SIZE / 2;
            aRoarX[1] = x + SHAPE_SIZE - 1;
            aRoarX[2] = x + SHAPE_SIZE / 2;
            aRoarX[3] = x;
            aRoarY[0] = y;
            aRoarY[1] = y + SHAPE_SIZE / 2;
            aRoarY[2] = y + SHAPE_SIZE - 1;
            aRoarY[3] = y + SHAPE_SIZE / 2;
            g2d.setColor(Color.black);
            g2d.fillPolygon(aRoarX, aRoarY, aRoarX.length);
        }
    }
}

class IntersPoint {

    int x;

    int y;

    int dX;

    int dY;

    public String toString() {
        return "[x:" + x + ", y:" + y + ", dX:" + dX + ", dY:" + dY + "]";
    }
}
