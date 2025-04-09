package org.ccpo.helper;

public class GACrossoverHelper {

    public static double[] executeHaupt(double gmin, double gmax, int zecs, double value1, double value2) {
        double[] vals = new double[2];
        double beta;
        beta = Math.random();
        vals[0] = beta * value1 + (1 - beta) * value2;
        vals[1] = (1 - beta) * value1 + beta * value2;
        return vals;
    }

    public static double[] executeNatural(double gmin, double gmax, int zecs, double value1, double value2, Double a, Double b) {
        if (a == null) {
            a = 1.0;
        }
        if (b == null) {
            b = 0.5;
        }
        double child1 = value1;
        double child2 = value2;
        double gena1 = Math.min(value1, value2);
        double gena2 = Math.max(value1, value2);
        double sigma1 = Math.min(gena1 - gmin, b * (gena2 - gena1)) / a;
        double sigma2 = Math.min(gmax - gena2, b * (gena2 - gena1)) / a;
        double u1, u2, v;
        while (!((child1 >= gmin) && (child1 <= gmax) && (child2 >= gmin) && (child2 <= gmax))) {
            u1 = Math.random();
            u2 = Math.random();
            v = Math.random();
            if (u2 == 0) u2 = 1E-20;
            double sinus, cosinus, radical;
            sinus = Math.sin(2 * Math.PI * u1);
            cosinus = Math.cos(2 * Math.PI * u1);
            radical = Math.sqrt(-2 * Math.log(u2));
            double trig1, trig2;
            if (v <= 0.5) {
                trig1 = sinus;
                trig2 = cosinus;
            } else {
                trig1 = cosinus;
                trig2 = sinus;
            }
            child1 = gena1 + sigma1 * trig1 * radical;
            child2 = gena2 + sigma2 * trig2 * radical;
        }
        if (child1 > gmax) child1 = gmax;
        if (child2 > gmax) child2 = gmax;
        if (child1 < gmin) child1 = gmin;
        if (child2 < gmin) child2 = gmin;
        return new double[] { child1, child2 };
    }

    public static double[] executeTriangle(double min, double max, int zecs, double value1, double value2, Double k) {
        if (k == null) {
            k = 1.0;
        }
        double child1 = 0;
        double child2 = 0;
        double g1 = Math.min(value1, value2);
        double g2 = Math.max(value1, value2);
        double vmin1 = 0, mod1 = 0, vmax1 = 0;
        double vmin2 = 0, mod2 = 0, vmax2 = 0;
        double kprec = Math.pow(1, -zecs);
        if (g1 != g2) {
            double d = g2 - g1;
            if ((min < g1) && (g1 < g2)) {
                vmin1 = g1 - k * d;
                if (vmin1 < min) vmin1 = min;
                mod1 = g1;
                vmax1 = g2;
                vmin2 = g1;
                mod2 = g2;
                vmax2 = g2 + k * d;
                if (vmax2 > max) vmax2 = max;
            }
            if ((min == g1) && (g1 < g2)) {
                vmin1 = min;
                mod1 = (g1 + g2) / 2;
                vmax1 = g2;
                vmin2 = min;
                mod2 = (g1 + g2) / 2;
                vmax2 = g2;
            }
            if ((g1 < g2) && (g2 == max)) {
                vmin1 = g1;
                mod1 = (g1 + g2) / 2;
                vmax1 = g2;
                vmin2 = g1;
                mod2 = (g1 + g2) / 2;
                vmax2 = g2;
            }
        } else {
            double d = Math.min(g1 - min, max - g2);
            if ((min < g1) && (g1 == g2) && (g2 < max)) {
                vmin1 = g1 - kprec;
                mod1 = g1;
                vmax1 = g1 + kprec;
                vmin2 = g2 - kprec;
                mod2 = g2;
                vmax2 = g2 + kprec;
            }
            if ((g1 == g2) && (g2 == min)) {
                vmin1 = g1;
                mod1 = g1 + (d / 2);
                vmax1 = g1 + d;
                vmin2 = g2;
                mod2 = g2 + (d / 2);
                vmax2 = g2 + d;
            }
            if ((g1 == g2) && (g2 == max)) {
                vmin1 = g1 - d;
                mod1 = g1 - (d / 2);
                vmax1 = g1;
                vmin2 = g2 - d;
                mod2 = g2 - (d / 2);
                vmax2 = g2;
            }
        }
        if (vmin1 < min) vmin1 = min;
        if (vmin2 < min) vmin2 = min;
        if (vmax1 > max) vmax1 = max;
        if (vmax2 > max) vmax2 = max;
        child1 = triangle(vmin1, mod1, vmax1);
        child2 = triangle(vmin2, mod2, vmax2);
        return new double[] { child1, child2 };
    }

    public static double[] executeDeb(double gmin, double gmax, int zecs, double value1, double value2, Double etac) {
        if (etac == null) {
            etac = 100.0;
        }
        double child1 = 0;
        double child2 = 0;
        double xu, xl, u, genmin, genmax, betaq, beta, alfa;
        xu = gmax;
        xl = gmin;
        u = Math.random();
        genmin = Math.min(value1, value2);
        genmax = Math.max(value1, value2);
        if (value1 != value2) {
            beta = 1 + (2 * (Math.min(genmin - xl, xu - genmax)) / Math.abs(value1 - value2));
        } else beta = 1;
        alfa = 2 - Math.pow(beta, -(etac + 1));
        if (u <= (1 / alfa)) betaq = Math.pow(u * alfa, 1 / (etac + 1)); else betaq = Math.pow((1 / (2 - u * alfa)), 1 / (etac + 1));
        child1 = 0.5 * ((value1 + value2) - (betaq * Math.abs(value1 - value2)));
        child2 = 0.5 * ((value1 + value2) + (betaq * Math.abs(value1 - value2)));
        return new double[] { child1, child2 };
    }

    public static double[] executeGray(double gmin, double gmax, int zecs, double value1, double value2) {
        double range, g1, g2;
        int gi1, gi2;
        int lungime;
        range = (gmax - gmin) / Math.pow(10, -1 * zecs);
        lungime = MathHelper.nrPozitii(range, 2);
        g1 = Math.round((value1 - gmin) / (gmax - gmin) * (Math.pow(2, lungime) - 1));
        g2 = Math.round((value2 - gmin) / (gmax - gmin) * (Math.pow(2, lungime) - 1));
        int[] btg1, gtb1, gtb2, btg2 = new int[lungime];
        gtb1 = MathHelper.cod(g1, 2, lungime);
        gtb2 = MathHelper.cod(g2, 2, lungime);
        btg1 = MathHelper.binary_to_gray(gtb1, lungime);
        btg2 = MathHelper.binary_to_gray(gtb2, lungime);
        int k;
        k = (int) (Math.random() * lungime);
        for (int i = 0; i <= lungime - 1; i++) {
            if (i <= k) {
                gtb1[i] = btg1[i];
                gtb2[i] = btg2[i];
            } else {
                gtb1[i] = btg2[i];
                gtb2[i] = btg1[i];
            }
        }
        btg1 = MathHelper.gray_to_binary(gtb1, lungime);
        btg2 = MathHelper.gray_to_binary(gtb2, lungime);
        gi1 = MathHelper.nr(btg1, 2);
        gi2 = MathHelper.nr(btg2, 2);
        g1 = gmin + gi1 * ((gmax - gmin) / (Math.pow(2, lungime) - 1));
        g2 = gmin + gi2 * ((gmax - gmin) / (Math.pow(2, lungime) - 1));
        return new double[] { g1, g2 };
    }

    public static double triangle(double min, double mod, double max) {
        double u = Math.random();
        if (u <= (mod - min) / (max - min)) return min + Math.sqrt((max - min) * (mod - min) * u); else return max - Math.sqrt((max - min) * (max - mod) * (1 - u));
    }
}
