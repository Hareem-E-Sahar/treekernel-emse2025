package Integrators;

/**
 * A class the creates a Gaussian integrator object
 * it works out the weights and abscissas using
 * Gaussian-legendre method. Adapted from numerical
 * methods in C Chapter 4.5
 * 
 * Copyright 2011 Chris M. Harrison and Avery Meiksin
 * Contact: aam@roe.ac.uk
 * 
 *    This file is part of IGMtrasnsmission.
 *
 *    IGMtransmission is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    IGMtransmission is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with IGMtransmission.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Gaussian {

    public double xi[], wi[];

    public int n;

    public Gaussian(double data[][], int n) {
        set(data, n);
    }

    public void set(double data[][], int n) {
        double x1 = data[0][1];
        System.out.println("Filter lower wavelength " + x1 + "A");
        double x2 = data[0][data[0].length - 1];
        System.out.println("Filter upper wavelength " + x2 + "A");
        double xl, xm, z1, z, pp, p3, p2, p1;
        int m = (n + 1) / 2;
        double EPS = 3 * Math.pow(10, -11);
        double x[] = new double[n + 1];
        double w[] = new double[n + 1];
        xm = 0.5 * (x1 + x2);
        xl = 0.5 * (x2 - x1);
        for (int i = 1; i <= m; i++) {
            z = Math.cos(Math.PI * (i - 0.25) / (n + 0.5));
            do {
                p1 = 1.0;
                p2 = 0.0;
                for (int j = 1; j <= n; j++) {
                    p3 = p2;
                    p2 = p1;
                    p1 = ((2.0 * j - 1.0) * z * p2 - (j - 1.0) * p3) / j;
                }
                pp = n * (z * p1 - p2) / (z * z - 1.0);
                z1 = z;
                z = z1 - p1 / pp;
            } while (Math.abs(z - z1) > EPS);
            x[i] = xm - xl * z;
            x[n + 1 - i] = xm + xl * z;
            w[i] = 2.0 * xl / ((1.0 - z * z) * pp * pp);
            w[n + 1 - i] = w[i];
        }
        setX(x);
        setW(w);
        setN(n);
    }

    public double[] interpolate(Gaussian gaussian, double data[][]) {
        double interpolateMe[] = new double[gaussian.n + 1];
        for (int i = 1; i < gaussian.n + 1; i++) {
            for (int j = 1; j < data[0].length; j++) {
                if (data[0][j] == gaussian.xi[i]) {
                    interpolateMe[i] = data[1][j];
                } else if (data[0][j] < gaussian.xi[i] && gaussian.xi[i] < data[0][j + 1]) {
                    interpolateMe[i] = data[1][j] + (gaussian.xi[i] - data[0][j]) * ((data[1][j + 1] - data[1][j]) / (data[0][j + 1] - data[0][j]));
                }
            }
        }
        return interpolateMe;
    }

    public double integrate(Gaussian gaussian, double data[]) {
        double s = 0;
        for (int i = 1; i <= gaussian.n; i++) {
            s += gaussian.wi[i] * (data[i]);
        }
        return s;
    }

    public void setX(double xx[]) {
        this.xi = xx;
    }

    public void setW(double ww[]) {
        this.wi = ww;
    }

    public void setN(int nn) {
        this.n = nn;
    }

    public double[] getX() {
        return this.xi;
    }

    public double[] getW() {
        return this.wi;
    }

    public double getN() {
        return this.n;
    }
}
