package org.easyway.collisions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import org.easyway.geometry2D.Point2D;
import org.easyway.geometry2D.Segment;
import org.easyway.interfaces.sprites.IPlain2D;
import org.easyway.interfaces.sprites.ISpriteColl;
import org.easyway.objects.sprites2D.Mask;
import org.easyway.system.StaticRef;
import org.easyway.tiles.TileSprite;
import org.easyway.utils.Utility;

/**
 * Collisions permette di identificare l'eventualit� che 2 sprite vadano in
 * collisione
 */
public class CollisionUtils {

    /**
	 * controlla se si � verificata una collisione rettangolare tra gli sprite
	 * spr1 e spr2
	 */
    public static boolean rectangleHit(IPlain2D spr1, IPlain2D spr2) {
        if (spr1 == null || spr2 == null) {
            Utility.error("Null Sprite!", "Collisions.trueHit(IPlain,IPlain)");
            return false;
        }
        double x1 = spr1.getX();
        double x2 = spr2.getX();
        double y1 = spr1.getY();
        double y2 = spr2.getY();
        if (-x1 + x2 + spr2.getWidth() - 1 >= 0 && x1 + spr1.getWidth() - 1 - x2 >= 0) {
            if (-y1 + y2 + spr2.getHeight() - 1 >= 0 && y1 + spr1.getHeight() - 1 - y2 >= 0) return true;
        }
        return false;
    }

    public static boolean circleHit(IPlain2D spr1, IPlain2D spr2) {
        if (spr1 == null || spr2 == null) {
            Utility.error("Null Sprite!", "Collisions.circleHit(IPlain,IPlain)");
            return false;
        }
        double r1 = (spr1.getHeight() * spr1.getHeight() + spr1.getWidth() * spr1.getWidth()) / 2;
        double r2 = (spr2.getHeight() * spr2.getHeight() + spr2.getWidth() * spr2.getWidth()) / 2;
        double d = Math.pow((spr1.getX() - spr2.getX()), 2) + Math.pow((spr1.getY() - spr2.getY()), 2);
        if (d < r1 + r2) return true;
        return false;
    }

    public static void hitted(ISpriteColl src, ISpriteColl dest) {
        if (!src.getAddedToCollisionList()) {
            StaticRef.collisionableLoopList.add(src);
            src.setAddedToCollisionList(true);
        }
        if (!dest.getAddedToCollisionList()) {
            StaticRef.collisionableLoopList.add(dest);
            dest.setAddedToCollisionList(true);
        }
        ArrayList<ISpriteColl> collList = src.getCollisionList();
        if (collList != null) collList.add(dest);
        collList = dest.getCollisionList();
        if (collList != null) collList.add(src);
    }

    public static boolean trueHit(ISpriteColl home, ISpriteColl dest) {
        return trueHit(home, dest, home.getMask(), dest.getMask());
    }

    /**
	 * controlla se si � verificata una collisione precisa al pixel tra gli
	 * sprite home e spr
	 */
    public static boolean trueHit(IPlain2D home, IPlain2D dest, Mask mhome, Mask mdest) {
        if (home == null || dest == null) {
            Utility.error("Null Plain!", "Collisions.trueHit(IPlain,IPlain,Mask,Mask)");
            return false;
        }
        if (mhome == null || mdest == null) {
            Utility.error("Null Masks! - " + home.toString() + " " + dest.toString(), "Collisions.trueHit(IPlain,IPlain,Mask,Mask)");
            return false;
        }
        if (mhome.full && mdest.full) {
            return true;
        }
        if (home.getWidth() != mhome.width || home.getHeight() != mhome.height || dest.getWidth() != mdest.width || dest.getHeight() != mdest.height) {
            return trueHitResized(home, dest, mhome, mdest);
        }
        final double x = home.getX();
        final double y = home.getY();
        final double x1 = dest.getX();
        final double y1 = dest.getY();
        final int xstart = (int) Math.max(x, x1);
        final int ystart = (int) Math.max(y, y1);
        final int xend = (int) Math.min(x + mhome.width - 1, x1 + mdest.width - 1);
        final int yend = (int) Math.min(y + mhome.height - 1, y1 + mdest.height - 1);
        final int Toty = Math.abs(yend - ystart);
        final int Totx = Math.abs(xend - xstart);
        final int xstarth = Math.abs(xstart - (int) x);
        final int ystarth = Math.abs(ystart - (int) y);
        final int xstartd = Math.abs(xstart - (int) x1);
        final int ystartd = Math.abs(ystart - (int) y1);
        int X, Y;
        int ny, ny1, nx, nx1;
        if (!(mhome.full || mdest.full)) {
            for (Y = 0; Y < Toty; Y++) {
                ny = ystarth + Y;
                ny1 = ystartd + Y;
                for (X = 0; X < Totx; X++) {
                    nx = xstarth + X;
                    nx1 = xstartd + X;
                    if (mhome.mask[nx][ny] && mdest.mask[nx1][ny1]) return true;
                }
            }
        } else {
            Mask mask = mhome.full ? mdest : mhome;
            final int ys = mhome.full ? ystartd : ystarth;
            final int xs = mhome.full ? xstartd : xstarth;
            for (Y = 0; Y < Toty; Y++) {
                ny = ys + Y;
                for (X = 0; X < Totx; X++) {
                    nx = xs + X;
                    if (mask.mask[nx][ny]) return true;
                }
            }
        }
        return false;
    }

    public static boolean trueHitAlpha(ISpriteColl home, ISpriteColl dest, float alpha, float beta) {
        return newAlphaTest(home, dest, home.getMask(), dest.getMask(), alpha, beta);
    }

    public static boolean trueHitAlpha2(IPlain2D home, IPlain2D dest, Mask mhome, Mask mdest, float alpha, float beta) {
        class Point extends Point2D {

            private static final long serialVersionUID = 4155987867699953900L;

            public Point(float x, float y) {
                super(x, y);
                U = S = B = D = false;
                segments = new Segment[2];
            }

            public void rotate(double cosalpha, double sinalpha) {
                double nx, ny;
                nx = getX() * cosalpha - getY() * sinalpha;
                ny = getX() * sinalpha + getY() * cosalpha;
                set((float) nx, (float) ny);
            }

            public boolean U, S, B, D;

            public Segment segments[];

            public void USBD(Point b, Point c, Point d) {
                Vector<Point> v = new Vector<Point>(4);
                v.add(this);
                v.add(b);
                v.add(c);
                v.add(d);
                Point temp;
                {
                    Point max = v.get(0);
                    Point max2 = null;
                    float maxval = v.get(0).getY();
                    for (int i = 0; i < 4; i++) {
                        temp = v.get(i);
                        if (maxval < temp.getY()) {
                            max = temp;
                            maxval = temp.getY();
                        }
                        if (maxval == temp.getY()) {
                            max2 = temp;
                        }
                    }
                    max.U = true;
                    if (max2 != null) max2.U = true;
                    max = v.get(0);
                    max2 = null;
                    maxval = v.get(0).getX();
                    for (int i = 0; i < 4; i++) {
                        temp = v.get(i);
                        if (maxval < temp.getX()) {
                            max = temp;
                            maxval = temp.getX();
                        }
                        if (maxval == temp.getX()) {
                            max2 = temp;
                        }
                    }
                    max.D = true;
                    if (max2 != null) max2.D = true;
                }
                {
                    Point min = v.get(0);
                    Point min2 = null;
                    float minval = v.get(0).getY();
                    for (int i = 0; i < 4; i++) {
                        temp = v.get(i);
                        if (minval > temp.getY()) {
                            min = temp;
                            minval = temp.getY();
                        }
                        if (minval == temp.getY()) {
                            min2 = temp;
                        }
                    }
                    min.U = true;
                    if (min2 != null) min2.U = true;
                    min = v.get(0);
                    min2 = null;
                    minval = v.get(0).getX();
                    for (int i = 0; i < 4; i++) {
                        temp = v.get(i);
                        if (minval > temp.getX()) {
                            min = temp;
                            minval = temp.getX();
                        }
                        if (minval == temp.getX()) {
                            min2 = temp;
                        }
                    }
                    min.D = true;
                    if (min2 != null) min2.D = true;
                }
            }

            public void createSegments(Point b, Point c, Point d) {
                Segment temp;
                Segment rette[] = new Segment[4];
                Point points[][] = new Point[4][2];
                temp = new Segment(this, b);
                this.segments[0] = temp;
                points[0][0] = this;
                b.segments[0] = temp;
                points[0][1] = b;
                rette[0] = temp;
                temp = new Segment(this, c);
                this.segments[1] = temp;
                points[1][0] = this;
                c.segments[0] = temp;
                points[1][1] = c;
                rette[1] = temp;
                temp = new Segment(b, d);
                b.segments[1] = temp;
                points[2][0] = b;
                d.segments[0] = temp;
                points[2][1] = d;
                rette[2] = temp;
                temp = new Segment(c, d);
                c.segments[1] = temp;
                points[3][0] = c;
                d.segments[1] = temp;
                points[3][1] = d;
                rette[3] = temp;
                Point A, B;
                for (int i = 0; i < 4; i++) {
                    A = points[i][0];
                    B = points[i][1];
                    if ((rette[i].U = (A.U && B.U)) || (rette[i].S = (A.S && B.S)) || (rette[i].B = (A.B && B.B)) || (rette[i].D = (A.D && B.D))) {
                        continue;
                    }
                    rette[i].U = A.U || B.U;
                    rette[i].S = A.S || B.S;
                    rette[i].B = A.B || B.B;
                    rette[i].D = A.D || B.D;
                }
            }
        }
        alpha = -alpha;
        beta = -beta;
        alpha *= rad;
        beta *= rad;
        final float xh = home.getX();
        final float yh = home.getY();
        final float xd = dest.getX();
        final float yd = dest.getY();
        final float widthh = home.getWidth();
        final float heighth = home.getHeight();
        final float widthd = dest.getWidth();
        final float heightd = dest.getHeight();
        final float centerxh = (xh + widthh) / 2;
        final float centeryh = (yh + heighth) / 2;
        final float centerxd = (xd + widthd) / 2;
        final float centeryd = (yd + heightd) / 2;
        final double cosalpha = Math.cos(alpha);
        final double sinalpha = Math.sin(alpha);
        final double cosbeta = Math.cos(beta);
        final double sinbeta = Math.sin(beta);
        Point hp1 = new Point(xh - centerxh, xh - centeryh);
        Point hp2 = new Point(xh + widthh - centerxh, yh - centeryh);
        Point hp3 = new Point(xh - centerxh, yh + heighth - centeryh);
        Point hp4 = new Point(xh + widthh - centerxh, yh + heighth - centeryh);
        Point dp1 = new Point(xd - centerxd, yd - centeryd);
        Point dp2 = new Point(xd + widthd - centerxd, yd - centeryd);
        Point dp3 = new Point(xd - centerxd, yd + heightd - centeryd);
        Point dp4 = new Point(xd + widthd - centerxd, yd + heightd - centeryd);
        hp1.rotate(cosalpha, sinalpha);
        hp2.rotate(cosalpha, sinalpha);
        hp3.rotate(cosalpha, sinalpha);
        hp4.rotate(cosalpha, sinalpha);
        dp1.rotate(cosbeta, sinbeta);
        dp2.rotate(cosbeta, sinbeta);
        dp3.rotate(cosbeta, sinbeta);
        dp4.rotate(cosbeta, sinbeta);
        hp1.USBD(hp2, hp3, hp4);
        dp1.USBD(dp2, dp3, dp4);
        hp1.createSegments(hp2, hp3, hp4);
        dp1.createSegments(dp2, dp3, dp4);
        return false;
    }

    /**
	 * A PRECISE test collision<br>
	 * O( n + m ) where: n = |mhome.mask|, m = |mdest.mask|
	 * 
	 * @param home
	 * @param dest
	 * @param mhome
	 * @param mdest
	 * @param alpha
	 * @param beta
	 * @return
	 */
    public static boolean newAlphaTest(IPlain2D home, IPlain2D dest, Mask mhome, Mask mdest, float alpha, float beta) {
        alpha *= rad;
        beta *= rad;
        final int x = (int) home.getX();
        final int y = (int) home.getY();
        final int x1 = (int) dest.getX();
        final int y1 = (int) dest.getY();
        final int width = home.getWidth();
        final int height = home.getHeight();
        final int width1 = dest.getWidth();
        final int height1 = dest.getHeight();
        final int xstart = (int) Math.min(x, x1);
        final int ystart = (int) Math.min(y, y1);
        final int relativex = x - xstart;
        final int relativex1 = x1 - xstart;
        final int relativey = y - ystart;
        final int relativey1 = y1 - ystart;
        final int diag = (int) Math.sqrt(width * width + height * height);
        final int diag1 = (int) Math.sqrt(width1 * width1 + height1 * height1);
        final int totX = diag + diag1 + 2;
        final int totY = diag + diag1 + 4;
        int area[][] = new int[totX][totY];
        final int acx = diag / 2 + relativex;
        final int acy = diag / 2 + relativey;
        final int acx1 = diag1 / 2 + relativex1;
        final int acy1 = diag1 / 2 + relativey1;
        final int cx = width / 2;
        final int cy = height / 2;
        final int cx1 = width1 / 2;
        final int cy1 = height1 / 2;
        final float scalex = (float) width / (float) mhome.width;
        final float scaley = (float) height / (float) mhome.height;
        final float scalex1 = (float) width1 / (float) mdest.width;
        final float scaley1 = (float) height1 / (float) mdest.height;
        int i = 0, j = 0, i1 = 0, j1 = 0;
        int px, py;
        int npx;
        boolean noend = true, noend1 = true;
        final double sinalpha = Math.sin(alpha);
        final double cosalpha = Math.cos(alpha);
        final double sinbeta = Math.sin(beta);
        final double cosbeta = Math.cos(beta);
        while (true) {
            if (noend) {
                if (mhome.mask[(int) (i / scalex)][(int) (j / scaley)]) {
                    npx = px = i - cx;
                    py = j - cy;
                    npx = (int) Math.round(((double) px * cosalpha - (double) py * sinalpha));
                    py = (int) Math.round(((double) py * cosalpha + (double) px * sinalpha));
                    npx += acx;
                    py += acy;
                    if (py < totY && npx < totX) {
                        if (area[npx][py] == 2) {
                            return true;
                        }
                        area[npx][py] = 1;
                    }
                }
                ++i;
                if (i == width) {
                    i = 0;
                    ++j;
                    if (j == height) {
                        if (noend1 == false) return false;
                        noend = false;
                    }
                }
            }
            if (noend1) {
                if (mdest.mask[(int) (i1 / scalex1)][(int) (j1 / scaley1)]) {
                    npx = px = i1 - cx1;
                    py = j1 - cy1;
                    npx = (int) Math.round(((double) px * cosbeta - (double) py * sinbeta));
                    py = (int) Math.round(((double) py * cosbeta + (double) px * sinbeta));
                    npx += acx1;
                    py += acy1;
                    if (py < totY && npx < totX) {
                        if (area[npx][py] == 1) {
                            return true;
                        }
                        area[npx][py] = 2;
                    }
                }
                ++i1;
                if (i1 == width1 - 1) {
                    i1 = 0;
                    ++j1;
                    if (j1 == height1 - 1) {
                        if (noend == false) return false;
                        noend1 = false;
                    }
                }
            }
        }
    }

    private static final double rad = Math.PI / 180;

    /**
	 * test two object with a roteation this is a fast but imprecise test
	 */
    public static boolean trueHitAlpha(IPlain2D home, IPlain2D dest, Mask mhome, Mask mdest, float alpha, float beta) {
        if (mhome == null || mdest == null) {
            Utility.error("Null Masks!", "Collisions.trueHitAlpha(IPlain,IPlain,Mask,Mask,float,float)");
            return false;
        }
        alpha = -alpha;
        beta = -beta;
        alpha *= rad;
        beta *= rad;
        final double x = home.getX();
        final double y = home.getY();
        final double x1 = dest.getX();
        final double y1 = dest.getY();
        final int xstart = (int) Math.max(x, x1);
        final int ystart = (int) Math.max(y, y1);
        final int xend = (int) Math.min(x + home.getWidth() - 1, x1 + dest.getWidth() - 1);
        final int yend = (int) Math.min(y + home.getHeight() - 1, y1 + dest.getHeight() - 1);
        final int Toty = Math.abs(yend - ystart);
        final int Totx = Math.abs(xend - xstart);
        final int xstarth = Math.abs(xstart - (int) x);
        final int ystarth = Math.abs(ystart - (int) y);
        final int xstartd = Math.abs(xstart - (int) x1);
        final int ystartd = Math.abs(ystart - (int) y1);
        float fhx = (float) home.getWidth() / (float) mhome.width;
        float fhy = (float) home.getHeight() / (float) mhome.height;
        float fdx = (float) dest.getWidth() / (float) mdest.width;
        float fdy = (float) dest.getHeight() / (float) mdest.height;
        final float ky = (float) Math.sin(alpha);
        final float kx = (float) Math.cos(alpha);
        final float ky1 = (float) Math.sin(beta);
        final float kx1 = (float) Math.cos(beta);
        final int cx = (mhome.width - 1) / 2;
        final int cy = (mhome.height - 1) / 2;
        final int cx1 = (mdest.width - 1) / 2;
        final int cy1 = (mdest.height - 1) / 2;
        float xp, yp, xp1, yp1;
        int ny, ny1, nx, nx1;
        int X, Y;
        for (Y = 0; Y < Toty; Y++) {
            yp = (int) ((ystarth + Y) / fhy) - cy;
            yp1 = (int) ((ystartd + Y) / fdy) - cy1;
            for (X = 0; X < Totx; X++) {
                xp = (int) ((xstarth + X) / fhx) - cx;
                xp1 = (int) ((xstartd + X) / fdx) - cx1;
                nx = Math.round(xp * kx - yp * ky) + cx;
                ny = Math.round(xp * ky + yp * kx) + cy;
                nx1 = Math.round(xp1 * kx1 - yp1 * ky) + cx1;
                ny1 = Math.round(xp1 * ky1 + yp1 * kx) + cy1;
                if (ny < 0 || ny >= mhome.height || ny1 < 0 || ny1 >= mdest.height || nx < 0 || nx >= mhome.width || nx1 < 0 || nx1 >= mdest.width) continue;
                if (mhome.mask[nx][ny] && mdest.mask[nx1][ny1]) return true;
            }
        }
        return false;
    }

    public static boolean trueHitResized(IPlain2D home, IPlain2D dest, Mask mhome, Mask mdest) {
        final double x = home.getX();
        final double y = home.getY();
        final double x1 = dest.getX();
        final double y1 = dest.getY();
        final int xstart = (int) Math.max(x, x1);
        final int ystart = (int) Math.max(y, y1);
        final int xend = (int) Math.min(x + home.getWidth() - 1, x1 + dest.getWidth() - 1);
        final int yend = (int) Math.min(y + home.getHeight() - 1, y1 + dest.getHeight() - 1);
        final int Toty = Math.abs(yend - ystart);
        final int Totx = Math.abs(xend - xstart);
        final int xstarth = Math.abs(xstart - (int) x);
        final int ystarth = Math.abs(ystart - (int) y);
        final int xstartd = Math.abs(xstart - (int) x1);
        final int ystartd = Math.abs(ystart - (int) y1);
        int X, Y;
        final float fhx = (float) home.getWidth() / (float) mhome.width;
        final float fhy = (float) home.getHeight() / (float) mhome.height;
        final float fdx = (float) dest.getWidth() / (float) mdest.width;
        final float fdy = (float) dest.getHeight() / (float) mdest.height;
        int ny, ny1, nx, nx1;
        if (!(mhome.full || mdest.full)) {
            for (Y = 0; Y < Toty; Y++) {
                ny = (int) ((ystarth + Y) / fhy);
                ny1 = (int) ((ystartd + Y) / fdy);
                for (X = 0; X < Totx; X++) {
                    nx = (int) ((xstarth + X) / fhx);
                    nx1 = (int) ((xstartd + X) / fdx);
                    if (mhome.mask[nx][ny] && mdest.mask[nx1][ny1]) return true;
                }
            }
        } else {
            Mask mask = mhome.full ? mdest : mhome;
            final int ys = mhome.full ? ystartd : ystarth;
            final int xs = mhome.full ? xstartd : xstarth;
            final float factorx = mhome.full ? fdx : fhx;
            final float factory = mhome.full ? fdy : fhy;
            for (Y = 0; Y < Toty; Y++) {
                ny = (int) ((ys + Y) / factory);
                for (X = 0; X < Totx; X++) {
                    nx = (int) ((xs + X) / factorx);
                    if (mask.mask[nx][ny]) return true;
                }
            }
        }
        return false;
    }

    /**
	 * check if the ArrayList contain one or more Tiles of type defined.<br>
	 * returns ONLY the FIRST TileSprite found.
	 * 
	 * @param list
	 *            the ArrayList<ISpriteColl>
	 * @param tiles
	 *            the types of Tiles to search
	 * @return the FIRST TileSprite found
	 * @see #searchTile(ArrayList, String[])
	 */
    public static TileSprite containTile(ArrayList<ISpriteColl> list, String... tiles) {
        ISpriteColl spr;
        TileSprite founded = null;
        Iterator<ISpriteColl> iterator = list.iterator();
        while (iterator.hasNext()) {
            spr = iterator.next();
            if (spr.getType().get().equals("$_TILESPRITE")) {
                for (String s : tiles) if (((TileSprite) spr).getTileString().equals(s)) {
                    founded = (TileSprite) spr;
                    break;
                }
            }
        }
        return founded;
    }

    /**
	 * check if the ArrayList contain one or more Tiles of type defined.<br>
	 * returns ALL the TileSprite found.
	 * 
	 * @param list
	 *            the ArrayList<ISpriteColl>
	 * @param tiles
	 *            the types of Tiles to search
	 * @return ALL the TileSprite found
	 * @see #containTile(ArrayList, String[])
	 */
    public static ArrayList<TileSprite> searchTile(ArrayList<ISpriteColl> list, String... tiles) {
        ArrayList<TileSprite> tlist = new ArrayList<TileSprite>(10);
        ISpriteColl spr;
        Iterator<ISpriteColl> iterator = list.iterator();
        while (iterator.hasNext()) {
            spr = iterator.next();
            if (spr.getType().get().equals("$_TILESPRITE")) {
                for (String s : tiles) if (((TileSprite) spr).getTileString().equals(s)) {
                    tlist.add((TileSprite) spr);
                }
            }
        }
        return tlist;
    }
}
