package jmri.jmrix.rps;

import javax.vecmath.Point3d;

/**
 * Implementation of 1st algorithm for reducing Readings
 * <P>
 * This algorithm was provided by Robert Ashenfelter
 * based in part on the work of Ralph Bucher in his paper
 * "Exact Solution for Three Dimensional Hyperbolic Positioning  Algorithm and 
 * Synthesizable VHDL Model for Hardware Implementation".
 * <P>
 * Neither Ashenfelter nor Bucher provide any guarantee as to the
 * intellectual property status of this algorithm.  
 * Use it at your own risk.
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.8 $
 */
public class Ash1_0Algorithm implements Calculator {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2")
    public Ash1_0Algorithm(Point3d[] sensors, double vsound) {
        this.sensors = sensors;
        this.Vs = vsound;
    }

    public Ash1_0Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, double vsound) {
        this(null, vsound);
        sensors = new Point3d[3];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
    }

    public Ash1_0Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, Point3d sensor4, double vsound) {
        this(null, vsound);
        sensors = new Point3d[4];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
        sensors[3] = sensor4;
    }

    double Vs;

    double Xt = 0.0;

    double Yt = 0.0;

    double Zt = 0.0;

    public Measurement convert(Reading r) {
        int nr = r.getNValues();
        if (nr != sensors.length) log.error("Mismatch: " + nr + " readings, " + sensors.length + " receivers");
        nr = Math.min(nr, sensors.length);
        double[] Tr = new double[nr];
        double[] Xr = new double[nr];
        double[] Yr = new double[nr];
        double[] Zr = new double[nr];
        for (int i = 0; i < nr; i++) {
            Tr[i] = r.getValue(i);
            Xr[i] = sensors[i].x;
            Yr[i] = sensors[i].y;
            Zr[i] = sensors[i].z;
        }
        RetVal result = RPSpos(nr, Tr, Xr, Yr, Zr, Vs, Xt, Yt, Zt);
        Xt = result.x;
        Yt = result.y;
        Zt = result.z;
        Vs = result.vs;
        log.debug("x = " + Xt + " y = " + Yt + " z0 = " + Zt + " code = " + result.code);
        return new Measurement(r, Xt, Yt, Zt, Vs, result.code, "Ash1_0Algorithm");
    }

    /**
     * Seed the conversion using an estimated position
     */
    public Measurement convert(Reading r, Point3d guess) {
        this.Xt = guess.x;
        this.Yt = guess.y;
        this.Zt = guess.z;
        return convert(r);
    }

    /**
     * Seed the conversion using a last measurement
     */
    public Measurement convert(Reading r, Measurement last) {
        if (last != null) {
            this.Xt = last.getX();
            this.Yt = last.getY();
            this.Zt = last.getZ();
        }
        if (this.Xt > 9.E99) this.Xt = 0;
        if (this.Yt > 9.E99) this.Yt = 0;
        if (this.Zt > 9.E99) this.Zt = 0;
        return convert(r);
    }

    Point3d sensors[];

    static int OFFSET = 0;

    static int TMAX = 35000;

    static final int NMAX = 15;

    double x, y, z, x0, y0, z0, x1, y1, z1, x2, y2, z2, Rmax;

    double xi, yi, zi, ri, xj, yj, zj, rj, xk, yk, zk, rk;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN")
    RetVal RPSpos(int nr, double Tr[], double Xr[], double Yr[], double Zr[], double Vs, double Xt, double Yt, double Zt) {
        int i, j, k, ns;
        double Rq;
        double Rs[] = new double[NMAX];
        double Xs[] = new double[NMAX];
        double Ys[] = new double[NMAX];
        double Zs[] = new double[NMAX];
        double d, da, db, d11, d12, d21, d22;
        double x1a = 0, y1a = 0, z1a = 0, x1b = 0, y1b = 0, z1b = 0;
        double x2a = 0, y2a = 0, z2a = 0, x2b = 0, y2b = 0, z2b = 0;
        double Ww, Xw, Yw, Zw, w;
        ns = 0;
        Rs[NMAX - 1] = TMAX;
        Rmax = Vs * TMAX;
        for (i = 0; i < nr; i++) {
            if (Tr[i] == 0) continue;
            Rq = Vs * (Tr[i] + OFFSET);
            if (Rq >= Rmax) continue;
            if (ns == 0) {
                Rs[0] = Rq;
                Xs[0] = Xr[i];
                Ys[0] = Yr[i];
                Zs[0] = Zr[i];
                ns = 1;
            } else {
                j = ((ns == NMAX) ? (ns - 1) : (ns++));
                for (; ; j--) {
                    if ((j > 0) && (Rq < Rs[j - 1])) {
                        Rs[j] = Rs[j - 1];
                        Xs[j] = Xs[j - 1];
                        Ys[j] = Ys[j - 1];
                        Zs[j] = Zs[j - 1];
                    } else {
                        if ((j < NMAX - 1) || (Rq < Rs[j])) {
                            Rs[j] = Rq;
                            Xs[j] = Xr[i];
                            Ys[j] = Yr[i];
                            Zs[j] = Zr[i];
                        }
                        break;
                    }
                }
            }
        }
        if (ns < 3) {
            Xt = Yt = Zt = 9.9999999e99;
            return new RetVal(1, Xt, Yt, Zt, Vs);
        }
        da = db = 0.0;
        for (i = 0; i < ns; i++) {
            j = (i + 1) % ns;
            k = (i + 2) % ns;
            xi = Xs[i];
            yi = Ys[i];
            zi = Zs[i];
            ri = Rs[i];
            xj = Xs[j];
            yj = Ys[j];
            zj = Zs[j];
            rj = Rs[j];
            xk = Xs[k];
            yk = Ys[k];
            zk = Zs[k];
            rk = Rs[k];
            if (gps3() == 0) {
                x = x1;
                y = y1;
                z = z1;
                if (wgt() > 0.0) {
                    x = x2;
                    y = y2;
                    z = z2;
                    if (wgt() > 0.0) {
                        d = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
                        if ((d < da) || (da == 0.0)) {
                            da = d;
                            x1a = x1;
                            y1a = y1;
                            z1a = z1;
                            x2a = x2;
                            y2a = y2;
                            z2a = z2;
                        }
                        if (d >= db) {
                            db = d;
                            x1b = x1;
                            y1b = y1;
                            z1b = z1;
                            x2b = x2;
                            y2b = y2;
                            z2b = z2;
                        }
                    }
                }
            }
        }
        if ((da == 0.0) && (db == 0.0)) {
            x = y = z = 0.0;
        } else {
            d11 = (x1a - x1b) * (x1a - x1b) + (y1a - y1b) * (y1a - y1b) + (z1a - z1b) * (z1a - z1b);
            d12 = (x1a - x2b) * (x1a - x2b) + (y1a - y2b) * (y1a - y2b) + (z1a - z2b) * (z1a - z2b);
            d21 = (x2a - x1b) * (x2a - x1b) + (y2a - y1b) * (y2a - y1b) + (z2a - z1b) * (z2a - z1b);
            d22 = (x2a - x2b) * (x2a - x2b) + (y2a - y2b) * (y2a - y2b) + (z2a - z2b) * (z2a - z2b);
            if ((d11 < d12) && (d11 < d21) && (d11 < d22)) {
                x = (x1a + x1b) / 2;
                y = (y1a + y1b) / 2;
                z = (z1a + z1b) / 2;
            } else if ((d12 < d21) && (d12 < d22)) {
                x = (x1a + x2b) / 2;
                y = (y1a + y2b) / 2;
                z = (z1a + z2b) / 2;
            } else if (d21 < d22) {
                x = (x2a + x1b) / 2;
                y = (y2a + y1b) / 2;
                z = (z2a + z1b) / 2;
            } else {
                x = (x2a + x2b) / 2;
                y = (y2a + y2b) / 2;
                z = (z2a + z2b) / 2;
            }
        }
        Ww = Xw = Yw = Zw = 0.0;
        for (i = 0; i < ns - 2; i++) {
            xi = Xs[i];
            yi = Ys[i];
            zi = Zs[i];
            ri = Rs[i];
            for (j = i + 1; j < ns - 1; j++) {
                xj = Xs[j];
                yj = Ys[j];
                zj = Zs[j];
                rj = Rs[j];
                for (k = j + 1; k < ns; k++) {
                    xk = Xs[k];
                    yk = Ys[k];
                    zk = Zs[k];
                    rk = Rs[k];
                    if (gps3() == 0) {
                        if (Ww == 0.0) {
                            x = x0;
                            y = y0;
                            z = z0;
                        }
                        if ((w = wgt()) > 0.0) {
                            Ww += w;
                            Xw += w * x0;
                            Yw += w * y0;
                            Zw += w * z0;
                            x = Xw / Ww;
                            y = Yw / Ww;
                            z = Zw / Ww;
                        }
                    }
                }
            }
        }
        if (Ww > 0.0) {
            Xt = x;
            Yt = y;
            Zt = z;
            return new RetVal(0, Xt, Yt, Zt, Vs);
        } else {
            Xt = Yt = Zt = 9.9999999e99;
            return new RetVal(2, Xt, Yt, Zt, Vs);
        }
    }

    double wgt() {
        double w;
        w = (1 - ri / Rmax) * (1 - rj / Rmax) * (1 - rk / Rmax);
        w *= 1.0 - Math.pow(((x - xi) * (x - xj) + (y - yi) * (y - yj) + (z - zi) * (z - zj)) / ri / rj, 2);
        w *= 1.0 - Math.pow(((x - xi) * (x - xk) + (y - yi) * (y - yk) + (z - zi) * (z - zk)) / ri / rk, 2);
        w *= 1.0 - Math.pow(((x - xj) * (x - xk) + (y - yj) * (y - yk) + (z - zj) * (z - zk)) / rj / rk, 2);
        w *= 0.05 + Math.abs((zi + zj + zk - 3 * z) / (ri + rj + rk));
        w *= (((yk - yi) * (zj - zi) - (yj - yi) * (zk - zi)) * (x - xi) + ((zk - zi) * (xj - xi) - (zj - zi) * (xk - xi)) * (y - yi) + ((xk - xi) * (yj - yi) - (xj - xi) * (yk - yi)) * (z - zi)) / (ri * rj * rk);
        w = Math.abs(w);
        if ((w > 0.5) || (w < .0000005)) w = 0.0;
        return (w);
    }

    int gps3() {
        @SuppressWarnings("unused") double xij, yij, zij, rij, xik, yik, zik, rik;
        @SuppressWarnings("unused") double xjk, yjk, zjk, rjk;
        double Ax, Ay, Az, Bx, By, Bz, Dx, Dy, Dz;
        @SuppressWarnings("unused") double Ca, Cb, Cc, Cd, Ce, Cf, Ci, Cj, Cx, Cy, Cz;
        double e1, e2;
        xik = xi - xk;
        yik = yi - yk;
        zik = zi - zk;
        xjk = xj - xk;
        yjk = yj - yk;
        zjk = zj - zk;
        Ci = (xi * xi - xk * xk + yi * yi - yk * yk + zi * zi - zk * zk - ri * ri + rk * rk) / 2;
        Cj = (xj * xj - xk * xk + yj * yj - yk * yk + zj * zj - zk * zk - rj * rj + rk * rk) / 2;
        Dz = xik * yjk - xjk * yik;
        Dy = zik * xjk - zjk * xik;
        Dx = yik * zjk - yjk * zik;
        if ((Math.abs(Dx) > Math.abs(Dy)) && (Math.abs(Dx) > Math.abs(Dz))) {
            Ay = (zik * xjk - zjk * xik) / Dx;
            By = (zjk * Ci - zik * Cj) / Dx;
            Az = (yjk * xik - yik * xjk) / Dx;
            Bz = (yik * Cj - yjk * Ci) / Dx;
            Ax = Ay * Ay + Az * Az + 1.0;
            Bx = (Ay * (yk - By) + Az * (zk - Bz) + xk) / Ax;
            Cx = Bx * Bx - (By * By + Bz * Bz - 2 * yk * By - 2 * zk * Bz + yk * yk + zk * zk + xk * xk - rk * rk) / Ax;
            if (Cx < 0.0) {
                x0 = y0 = z0 = 9.9999999e99;
                return 1;
            }
            x1 = Bx + Math.sqrt(Cx);
            y1 = Ay * x1 + By;
            z1 = Az * x1 + Bz;
            x2 = 2 * Bx - x1;
            y2 = Ay * x2 + By;
            z2 = Az * x2 + Bz;
        } else if (Math.abs(Dy) > Math.abs(Dz)) {
            Az = (xik * yjk - xjk * yik) / Dy;
            Bz = (xjk * Ci - xik * Cj) / Dy;
            Ax = (zjk * yik - zik * yjk) / Dy;
            Bx = (zik * Cj - zjk * Ci) / Dy;
            Ay = Az * Az + Ax * Ax + 1.0;
            By = (Az * (zk - Bz) + Ax * (xk - Bx) + yk) / Ay;
            Cy = By * By - (Bz * Bz + Bx * Bx - 2 * zk * Bz - 2 * xk * Bx + zk * zk + xk * xk + yk * yk - rk * rk) / Ay;
            if (Cy < 0.0) {
                x0 = y0 = z0 = 9.9999999e99;
                return 1;
            }
            y1 = By + Math.sqrt(Cy);
            z1 = Az * y1 + Bz;
            x1 = Ax * y1 + Bx;
            y2 = 2 * By - y1;
            z2 = Az * y2 + Bz;
            x2 = Ax * y2 + Bx;
        } else {
            if (Dz == 0.0) {
                x0 = y0 = z0 = 9.9999999e99;
                return 1;
            }
            Ax = (yik * zjk - yjk * zik) / Dz;
            Bx = (yjk * Ci - yik * Cj) / Dz;
            Ay = (xjk * zik - xik * zjk) / Dz;
            By = (xik * Cj - xjk * Ci) / Dz;
            Az = Ax * Ax + Ay * Ay + 1.0;
            Bz = (Ax * (xk - Bx) + Ay * (yk - By) + zk) / Az;
            Cz = Bz * Bz - (Bx * Bx + By * By - 2 * xk * Bx - 2 * yk * By + xk * xk + yk * yk + zk * zk - rk * rk) / Az;
            if (Cz < 0.0) {
                x0 = y0 = z0 = 9.9999999e99;
                return 1;
            }
            z1 = Bz + Math.sqrt(Cz);
            x1 = Ax * z1 + Bx;
            y1 = Ay * z1 + By;
            z2 = 2 * Bz - z1;
            x2 = Ax * z2 + Bx;
            y2 = Ay * z2 + By;
        }
        e1 = (x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1);
        e2 = (x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2);
        if (e1 <= e2) {
            x0 = x1;
            y0 = y1;
            z0 = z1;
        } else {
            x0 = x2;
            y0 = y2;
            z0 = z2;
        }
        return 0;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Ash1_0Algorithm.class.getName());

    /**
     * Internal class to handle return value.
     *
     * More of a struct, really
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UUF_UNUSED_FIELD")
    static class RetVal {

        RetVal(int code, double x, double y, double z, double vs) {
            this.code = code;
            this.x = x;
            this.y = y;
            this.z = z;
            this.vs = vs;
        }

        int code;

        double x, y, z, t, vs;
    }
}
