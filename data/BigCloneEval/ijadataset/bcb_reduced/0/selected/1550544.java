package jat.forces;

import java.lang.Math;
import jat.math.*;
import jat.forces.nrlmsise.NRLMSISE_data;
import jat.matvec.data.Matrix;
import jat.matvec.data.VectorN;
import jat.timeRef.EarthRef;
import jat.timeRef.Geodetic;
import jat.spacecraft.Spacecraft;
import jat.spacetime.BodyRef;
import jat.spacetime.EarthTrueOfDateRef;
import jat.spacetime.ReferenceFrameTranslater;
import jat.spacetime.Time;
import jat.cm.Constants;

/**
 * NRLMSISE drag model.  Translated from a code written in c, originally
 * developed by Mike Picone, Alan Hedin, and Doug Drob and implemented by
 * Dominik Brodowski.  
 * 
 *  * The NRLMSISE-00 model was developed by Mike Picone, Alan Hedin, and
 * Doug Drob. They also wrote a NRLMSISE-00 distribution package in 
 * FORTRAN which is available at
 * http://uap-www.nrl.navy.mil/models_web/msis/msis_home.htm
 *
 * Dominik Brodowski implemented and maintains this C version. You can
 * reach him at devel@brodo.de. See the file "DOCUMENTATION" for details,
 * and check http://www.brodo.de/english/pub/nrlmsise/index.html for
 * updated releases of this package.
 * 
 * Translated to Java by Richard C. Page III
 * 
 * @author Richard C. Page III
 *
 */
public class NRLMSISE_Drag extends AtmosphericDrag {

    private double ap = 15;

    private double f107Daily = 150;

    private double f107Average = 150;

    private final double dgtr = Constants.deg2rad;

    /**   
     *   Switches: to turn on and off particular variations use these switches.
     *   0 is off, 1 is on, and 2 is main effects off but cross terms on.
     *
     *   Standard values are 0 for switch 0 and 1 for switches 1 to 23. The 
     *   array "switches" needs to be set accordingly by the calling program. 
     *   The arrays sw and swc are set internally.
     *
     *   switches[i]:
     *    i - explanation
     *   -----------------
     *    0 - output in centimeters instead of meters
     *    1 - F10.7 effect on mean
     *    2 - time independent
     *    3 - symmetrical annual
     *    4 - symmetrical semiannual
     *    5 - asymmetrical annual
     *    6 - asymmetrical semiannual
     *    7 - diurnal
     *    8 - semidiurnal
     *    9 - daily ap [when this is set to -1 (!) the pointer
     *                  ap_a in struct nrlmsise_input must
     *                  point to a struct ap_array]
     *   10 - all UT/long effects
     *   11 - longitudinal
     *   12 - UT and mixed UT/long
     *   13 - mixed AP/UT/LONG
     *   14 - terdiurnal
     *   15 - departures from diffusive equilibrium
     *   16 - all TINF var
     *   17 - all TLB var
     *   18 - all TN1 var
     *   19 - all S var
     *   20 - all TN2 var
     *   21 - all NLB var
     *   22 - all TN3 var
     *   23 - turbo scale height var
     */
    protected class struct_nrlmsise_flags {

        int switches[] = new int[24];

        double sw[] = new double[24];

        double swc[] = new double[24];

        public struct_nrlmsise_flags() {
            for (int i = 0; i < 24; i++) {
                switches[i] = 0;
                sw[i] = 0.0;
                swc[i] = 0.0;
            }
        }
    }

    /** Array containing the following magnetic values:
     *   0 : daily AP
     *   1 : 3 hr AP index for current time
     *   2 : 3 hr AP index for 3 hrs before current time
     *   3 : 3 hr AP index for 6 hrs before current time
     *   4 : 3 hr AP index for 9 hrs before current time
     *   5 : Average of eight 3 hr AP indicies from 12 to 33 hrs 
     *           prior to current time
     *   6 : Average of eight 3 hr AP indicies from 36 to 57 hrs 
     *           prior to current time 
     */
    protected class struct_ap_array {

        double a[] = new double[7];

        public struct_ap_array() {
            for (int i = 0; i < 7; i++) a[i] = 0.0;
        }
    }

    /**
     *   NOTES ON INPUT VARIABLES: 
     *      UT, Local Time, and Longitude are used independently in the
     *      model and are not of equal importance for every situation.  
     *      For the most physically realistic calculation these three
     *      variables should be consistent (lst=sec/3600 + g_long/15).
     *      The Equation of Time departures from the above formula
     *      for apparent local time can be included if available but
     *      are of minor importance.
     *
     *      f107 and f107A values used to generate the model correspond
     *      to the 10.7 cm radio flux at the actual distance of the Earth
     *      from the Sun rather than the radio flux at 1 AU. The following
     *      site provides both classes of values:
     *      ftp://ftp.ngdc.noaa.gov/STP/SOLAR_DATA/SOLAR_RADIO/FLUX/
     *
     *      f107, f107A, and ap effects are neither large nor well
     *      established below 80 km and these parameters should be set to
     *      150., 150., and 4. respectively.
     */
    protected class struct_nrlmsise_input {

        int year;

        int doy;

        double sec;

        double alt;

        double g_lat;

        double g_long;

        double lst;

        double f107A;

        double f107;

        double ap;

        struct_ap_array ap_a;

        public struct_nrlmsise_input() {
            year = 0;
            doy = 0;
            sec = 0.0;
            alt = 0.0;
            g_lat = 0.0;
            g_long = 0.0;
            lst = 0.0;
            f107A = 0.0;
            f107 = 0.0;
            ap = 0.0;
            ap_a = new struct_ap_array();
        }
    }

    /** 
     *   OUTPUT VARIABLES:
     *      d[0] - HE NUMBER DENSITY(CM-3)
     *      d[1] - O NUMBER DENSITY(CM-3)
     *      d[2] - N2 NUMBER DENSITY(CM-3)
     *      d[3] - O2 NUMBER DENSITY(CM-3)
     *      d[4] - AR NUMBER DENSITY(CM-3)                       
     *      d[5] - TOTAL MASS DENSITY(GM/CM3) [includes d[8] in td7d]
     *      d[6] - H NUMBER DENSITY(CM-3)
     *      d[7] - N NUMBER DENSITY(CM-3)
     *      d[8] - Anomalous oxygen NUMBER DENSITY(CM-3)
     *      t[0] - EXOSPHERIC TEMPERATURE
     *      t[1] - TEMPERATURE AT ALT
     * 
     *
     *      O, H, and N are set to zero below 72.5 km
     *
     *      t[0], Exospheric temperature, is set to global average for
     *      altitudes below 120 km. The 120 km gradient is left at global
     *      average value for altitudes below 72 km.
     *
     *      d[5], TOTAL MASS DENSITY, is NOT the same for subroutines GTD7 
     *      and GTD7D
     *
     *        SUBROUTINE GTD7 -- d[5] is the sum of the mass densities of the
     *        species labeled by indices 0-4 and 6-7 in output variable d.
     *        This includes He, O, N2, O2, Ar, H, and N but does NOT include
     *        anomalous oxygen (species index 8).
     *
     *        SUBROUTINE GTD7D -- d[5] is the "effective total mass density
     *        for drag" and is the sum of the mass densities of all species
     *        in this model, INCLUDING anomalous oxygen.
     */
    protected class struct_nrlmsise_output {

        double d[] = new double[9];

        doublestar t[] = new doublestar[2];

        public struct_nrlmsise_output() {
            for (int i = 0; i < 9; i++) d[i] = 0;
            for (int i = 0; i < 2; i++) t[i] = new doublestar();
        }
    }

    protected doublestar gsurf = new doublestar();

    protected doublestar re = new doublestar();

    protected double dd;

    protected double dm04, dm16, dm28, dm32, dm40, dm01, dm14;

    protected double meso_tn1[] = new double[5];

    protected double meso_tn2[] = new double[4];

    protected double meso_tn3[] = new double[6];

    protected double meso_tgn1[] = new double[2];

    protected double meso_tgn2[] = new double[2];

    protected double meso_tgn3[] = new double[2];

    public double pt[] = NRLMSISE_data.pt;

    public double pd[][] = NRLMSISE_data.pd;

    public double ps[] = NRLMSISE_data.ps;

    public double pdl[][] = NRLMSISE_data.pdl;

    public double ptl[][] = NRLMSISE_data.ptl;

    public double pma[][] = NRLMSISE_data.pma;

    public double sam[] = NRLMSISE_data.sam;

    public double ptm[] = NRLMSISE_data.ptm;

    public double pdm[][] = NRLMSISE_data.pdm;

    public double pavgm[] = NRLMSISE_data.pavgm;

    protected double dfa;

    protected double plg[][] = new double[4][9];

    protected double ctloc, stloc;

    protected double c2tloc, s2tloc;

    protected double s3tloc, c3tloc;

    protected double apdf;

    protected double apt[] = new double[4];

    /**
     * Inner class doublestar simulates pass by reference in c for a 
     * single double value
     */
    protected static class doublestar {

        public double val = 0;

        public doublestar() {
            val = 0;
        }

        public doublestar(double x) {
            val = x;
        }
    }

    double cos(double x) {
        return Math.cos(x);
    }

    double sin(double x) {
        return Math.sin(x);
    }

    double exp(double x) {
        return Math.exp(x);
    }

    double pow(double x, double y) {
        return Math.pow(x, y);
    }

    double log(double x) {
        return Math.log(x);
    }

    double log10(double x) {
        return Math.log10(x);
    }

    double sqrt(double x) {
        return Math.sqrt(x);
    }

    double abs(double x) {
        return Math.abs(x);
    }

    /**
     * Simulates the casting of a double type in a c if statement
     * @param x The double expression
     * @return The boolean value of the double (if(x==0) 'true' else 'false')
     */
    boolean bool(double x) {
        if (x == 0) return false; else return true;
    }

    /**
     * Constructor
     * @param sc Spacecraft containing drag_coefficient, area, and mass
     */
    public NRLMSISE_Drag(Spacecraft sc) {
        super(sc);
    }

    /**
     * Constructor
     * @param cd Drag coefficient
     * @param area Cross-section [m^2]
     * @param mass Spacecraft mass [kg]
     */
    public NRLMSISE_Drag(double cd, double area, double mass) {
        super(cd, area, mass);
    }

    /** Abstract function from AtmosphericDrag - computes atmospheric density
     * @param ref EarthRef object.
     * @param r Position vector.
     * @return Atmospheric density in kg/m^3
     */
    public double computeDensity(Time t, BodyRef ref, VectorN r) {
        struct_nrlmsise_output output = new struct_nrlmsise_output();
        struct_nrlmsise_input input = new struct_nrlmsise_input();
        struct_nrlmsise_flags flags = new struct_nrlmsise_flags();
        struct_ap_array aph = new struct_ap_array();
        Matrix eci2ecef = ref.inertial_to_body(t);
        VectorN r_ecef = eci2ecef.times(r);
        Geodetic geod = new Geodetic(r_ecef);
        double alt = geod.getHAE() / 1000.0;
        if (alt > 1000) return 0;
        int i;
        flags.switches[0] = 0;
        for (i = 1; i < 24; i++) flags.switches[i] = 1;
        input.doy = t.dayOfYear();
        input.year = 2004;
        input.sec = t.secOfDay();
        input.alt = alt;
        input.g_lat = geod.getLatitude() * MathUtils.RAD2DEG;
        input.g_long = geod.getLongitude() * MathUtils.RAD2DEG;
        input.lst = input.sec / 3600 + input.g_long / 15;
        input.f107A = this.f107Average;
        input.f107 = this.f107Daily;
        input.ap = this.ap;
        if (alt > 500) {
            gtd7d(input, flags, output);
        } else {
            gtd7(input, flags, output);
        }
        return output.d[5] * 1000;
    }

    /**
     * Set the Daily value of the 10.7cm solar flux
     * @param x
     */
    public void setF107Daily(double x) {
        f107Daily = x;
    }

    /**
     * Set the 81-day average value of the 10.7cm solar flux
     * @param x
     */
    public void setF107Average(double x) {
        f107Average = x;
    }

    /**
     * Set the daily magnetic index
     * @param x
     */
    public void setAP(double x) {
        ap = x;
    }

    protected void tselec(struct_nrlmsise_flags flags) {
        int i;
        for (i = 0; i < 24; i++) {
            if (i != 9) {
                if (flags.switches[i] == 1) flags.sw[i] = 1; else flags.sw[i] = 0;
                if (flags.switches[i] > 0) flags.swc[i] = 1; else flags.swc[i] = 0;
            } else {
                flags.sw[i] = flags.switches[i];
                flags.swc[i] = flags.switches[i];
            }
        }
    }

    protected void glatf(double lat, doublestar gv, doublestar reff) {
        double c2;
        c2 = cos(2.0 * dgtr * lat);
        gv.val = 980.616 * (1.0 - 0.0026373 * c2);
        reff.val = 2.0 * (gv.val) / (3.085462E-6 + 2.27E-9 * c2) * 1.0E-5;
    }

    protected double ccor(double alt, double r, double h1, double zh) {
        double e;
        double ex;
        e = (alt - zh) / h1;
        if (e > 70) return exp(0);
        if (e < -70) return exp(r);
        ex = exp(e);
        e = r / (1.0 + ex);
        return exp(e);
    }

    protected double ccor2(double alt, double r, double h1, double zh, double h2) {
        double e1, e2;
        double ex1, ex2;
        double ccor2v;
        e1 = (alt - zh) / h1;
        e2 = (alt - zh) / h2;
        if ((e1 > 70) || (e2 > 70)) return exp(0);
        if ((e1 < -70) && (e2 < -70)) return exp(r);
        ex1 = exp(e1);
        ex2 = exp(e2);
        ccor2v = r / (1.0 + 0.5 * (ex1 + ex2));
        return exp(ccor2v);
    }

    protected double scalh(double alt, double xm, double temp) {
        double g;
        double rgas = 831.4;
        g = gsurf.val / (pow((1.0 + alt / re.val), 2.0));
        g = rgas * temp / (g * xm);
        return g;
    }

    protected double dnet(double dd, double dm, double zhm, double xmm, double xm) {
        double a;
        double ylog;
        a = zhm / (xmm - xm);
        if (!((dm > 0) && (dd > 0))) {
            System.out.println("dnet log error " + dm + " " + dd + " " + xm);
            if ((dd == 0) && (dm == 0)) dd = 1;
            if (dm == 0) return dd;
            if (dd == 0) return dm;
        }
        ylog = a * log(dm / dd);
        if (ylog < -10) return dd;
        if (ylog > 10) return dm;
        a = dd * pow((1.0 + exp(ylog)), (1.0 / a));
        return a;
    }

    protected void splini(double[] xa, double[] ya, double[] y2a, int n, double x, doublestar y) {
        double yi = 0;
        int klo = 0;
        int khi = 1;
        double xx, h, a, b, a2, b2;
        while ((x > xa[klo]) && (khi < n)) {
            xx = x;
            if (khi < (n - 1)) {
                if (x < xa[khi]) xx = x; else xx = xa[khi];
            }
            h = xa[khi] - xa[klo];
            a = (xa[khi] - xx) / h;
            b = (xx - xa[klo]) / h;
            a2 = a * a;
            b2 = b * b;
            yi += ((1.0 - a2) * ya[klo] / 2.0 + b2 * ya[khi] / 2.0 + ((-(1.0 + a2 * a2) / 4.0 + a2 / 2.0) * y2a[klo] + (b2 * b2 / 4.0 - b2 / 2.0) * y2a[khi]) * h * h / 6.0) * h;
            klo++;
            khi++;
        }
        y.val = yi;
    }

    protected void splint(double[] xa, double[] ya, double[] y2a, int n, double x, doublestar y) {
        int klo = 0;
        int khi = n - 1;
        int k;
        double h;
        double a, b, yi;
        while ((khi - klo) > 1) {
            k = (khi + klo) / 2;
            if (xa[k] > x) khi = k; else klo = k;
        }
        h = xa[khi] - xa[klo];
        if (h == 0.0) System.out.println("bad XA input to splint");
        a = (xa[khi] - x) / h;
        b = (x - xa[klo]) / h;
        yi = a * ya[klo] + b * ya[khi] + ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi]) * h * h / 6.0;
        y.val = yi;
    }

    protected void spline(double[] x, double[] y, int n, double yp1, double ypn, double[] y2) {
        double[] u;
        double sig, p, qn, un;
        int i, k;
        u = new double[n];
        if (u == null) {
            System.out.println("Out Of Memory in spline - ERROR");
            return;
        }
        if (yp1 > 0.99E30) {
            y2[0] = 0;
            u[0] = 0;
        } else {
            y2[0] = -0.5;
            u[0] = (3.0 / (x[1] - x[0])) * ((y[1] - y[0]) / (x[1] - x[0]) - yp1);
        }
        for (i = 1; i < (n - 1); i++) {
            sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
            p = sig * y2[i - 1] + 2.0;
            y2[i] = (sig - 1.0) / p;
            u[i] = (6.0 * ((y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1])) / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
        }
        if (ypn > 0.99E30) {
            qn = 0;
            un = 0;
        } else {
            qn = 0.5;
            un = (3.0 / (x[n - 1] - x[n - 2])) * (ypn - (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]));
        }
        y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
        for (k = n - 2; k >= 0; k--) y2[k] = y2[k] * y2[k + 1] + u[k];
    }

    protected double zeta(double zz, double zl) {
        return ((zz - zl) * (re.val + zl) / (re.val + zz));
    }

    protected double densm(double alt, double d0, double xm, doublestar tz, int mn3, double[] zn3, double[] tn3, double[] tgn3, int mn2, double[] zn2, double[] tn2, double[] tgn2) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        double[] y2out = new double[10];
        double rgas = 831.4;
        double z, z1, z2, t1, t2, zg, zgdif;
        double yd1, yd2;
        double x;
        doublestar y = new doublestar();
        doublestar yi = new doublestar();
        double expl, gamm, glb;
        double densm_tmp;
        int mn;
        int k;
        densm_tmp = d0;
        if (alt > zn2[0]) {
            if (xm == 0.0) return tz.val; else return d0;
        }
        if (alt > zn2[mn2 - 1]) z = alt; else z = zn2[mn2 - 1];
        mn = mn2;
        z1 = zn2[0];
        z2 = zn2[mn - 1];
        t1 = tn2[0];
        t2 = tn2[mn - 1];
        zg = zeta(z, z1);
        zgdif = zeta(z2, z1);
        for (k = 0; k < mn; k++) {
            xs[k] = zeta(zn2[k], z1) / zgdif;
            ys[k] = 1.0 / tn2[k];
        }
        yd1 = -tgn2[0] / (t1 * t1) * zgdif;
        yd2 = -tgn2[1] / (t2 * t2) * zgdif * (pow(((re.val + z2) / (re.val + z1)), 2.0));
        spline(xs, ys, mn, yd1, yd2, y2out);
        x = zg / zgdif;
        splint(xs, ys, y2out, mn, x, y);
        tz.val = 1.0 / y.val;
        if (xm != 0.0) {
            glb = gsurf.val / (pow((1.0 + z1 / re.val), 2.0));
            gamm = xm * glb * zgdif / rgas;
            splini(xs, ys, y2out, mn, x, yi);
            expl = gamm * yi.val;
            if (expl > 50.0) expl = 50.0;
            densm_tmp = densm_tmp * (t1 / tz.val) * exp(-expl);
        }
        if (alt > zn3[0]) {
            if (xm == 0.0) return tz.val; else return densm_tmp;
        }
        z = alt;
        mn = mn3;
        z1 = zn3[0];
        z2 = zn3[mn - 1];
        t1 = tn3[0];
        t2 = tn3[mn - 1];
        zg = zeta(z, z1);
        zgdif = zeta(z2, z1);
        for (k = 0; k < mn; k++) {
            xs[k] = zeta(zn3[k], z1) / zgdif;
            ys[k] = 1.0 / tn3[k];
        }
        yd1 = -tgn3[0] / (t1 * t1) * zgdif;
        yd2 = -tgn3[1] / (t2 * t2) * zgdif * (pow(((re.val + z2) / (re.val + z1)), 2.0));
        spline(xs, ys, mn, yd1, yd2, y2out);
        x = zg / zgdif;
        splint(xs, ys, y2out, mn, x, y);
        tz.val = 1.0 / y.val;
        if (xm != 0.0) {
            glb = gsurf.val / (pow((1.0 + z1 / re.val), 2.0));
            gamm = xm * glb * zgdif / rgas;
            splini(xs, ys, y2out, mn, x, yi);
            expl = gamm * yi.val;
            if (expl > 50.0) expl = 50.0;
            densm_tmp = densm_tmp * (t1 / tz.val) * exp(-expl);
        }
        if (xm == 0.0) return tz.val; else return densm_tmp;
    }

    protected double densu(double alt, double dlb, double tinf, double tlb, double xm, double alpha, doublestar tz, double zlb, double s2, int mn1, double[] zn1, double[] tn1, double[] tgn1) {
        double yd2, yd1, x = 0.0;
        doublestar y = new doublestar();
        double rgas = 831.4;
        double densu_temp = 1.0;
        double za, z, zg2, tt, ta;
        double dta, z1 = 0.0, z2, t1 = 0.0, t2, zg, zgdif = 0.0;
        int mn = 0;
        int k;
        double glb;
        double expl;
        doublestar yi = new doublestar();
        double densa;
        double gamma, gamm;
        double[] xs = new double[5];
        double[] ys = new double[5];
        double[] y2out = new double[5];
        za = zn1[0];
        if (alt > za) z = alt; else z = za;
        zg2 = zeta(z, zlb);
        tt = tinf - (tinf - tlb) * exp(-s2 * zg2);
        ta = tt;
        tz.val = tt;
        densu_temp = tz.val;
        if (alt < za) {
            dta = (tinf - ta) * s2 * pow(((re.val + zlb) / (re.val + za)), 2.0);
            tgn1[0] = dta;
            tn1[0] = ta;
            if (alt > zn1[mn1 - 1]) z = alt; else z = zn1[mn1 - 1];
            mn = mn1;
            z1 = zn1[0];
            z2 = zn1[mn - 1];
            t1 = tn1[0];
            t2 = tn1[mn - 1];
            zg = zeta(z, z1);
            zgdif = zeta(z2, z1);
            for (k = 0; k < mn; k++) {
                xs[k] = zeta(zn1[k], z1) / zgdif;
                ys[k] = 1.0 / tn1[k];
            }
            yd1 = -tgn1[0] / (t1 * t1) * zgdif;
            yd2 = -tgn1[1] / (t2 * t2) * zgdif * pow(((re.val + z2) / (re.val + z1)), 2.0);
            spline(xs, ys, mn, yd1, yd2, y2out);
            x = zg / zgdif;
            splint(xs, ys, y2out, mn, x, y);
            tz.val = 1.0 / y.val;
            densu_temp = tz.val;
        }
        if (xm == 0) return densu_temp;
        glb = gsurf.val / pow((1.0 + zlb / re.val), 2.0);
        gamma = xm * glb / (s2 * rgas * tinf);
        expl = exp(-s2 * gamma * zg2);
        if (expl > 50.0) expl = 50.0;
        if (tt <= 0) expl = 50.0;
        densa = dlb * pow((tlb / tt), ((1.0 + alpha + gamma))) * expl;
        densu_temp = densa;
        if (alt >= za) return densu_temp;
        glb = gsurf.val / pow((1.0 + z1 / re.val), 2.0);
        gamm = xm * glb * zgdif / rgas;
        splini(xs, ys, y2out, mn, x, yi);
        expl = gamm * yi.val;
        if (expl > 50.0) expl = 50.0;
        if (tz.val <= 0) expl = 50.0;
        densu_temp = densu_temp * pow((t1 / tz.val), (1.0 + alpha)) * exp(-expl);
        return densu_temp;
    }

    protected double g0(double a, double[] p) {
        return (a - 4.0 + (p[25] - 1.0) * (a - 4.0 + (exp(-sqrt(p[24] * p[24]) * (a - 4.0)) - 1.0) / sqrt(p[24] * p[24])));
    }

    protected double sumex(double ex) {
        return (1.0 + (1.0 - pow(ex, 19.0)) / (1.0 - ex) * pow(ex, 0.5));
    }

    protected double sg0(double ex, double[] p, double[] ap) {
        return (g0(ap[1], p) + (g0(ap[2], p) * ex + g0(ap[3], p) * ex * ex + g0(ap[4], p) * pow(ex, 3.0) + (g0(ap[5], p) * pow(ex, 4.0) + g0(ap[6], p) * pow(ex, 12.0)) * (1.0 - pow(ex, 8.0)) / (1.0 - ex))) / sumex(ex);
    }

    protected double globe7(double[] p, struct_nrlmsise_input input, struct_nrlmsise_flags flags) {
        double t[] = new double[15];
        int i, j;
        int sw9 = 1;
        double apd;
        double xlong;
        double tloc;
        double c, s, c2, c4, s2;
        double sr = 7.2722E-5;
        double dr = 1.72142E-2;
        double hr = 0.2618;
        double cd32, cd18, cd14, cd39;
        double p32, p18, p14, p39;
        double df, dfa;
        double f1, f2;
        double tinf = 0;
        struct_ap_array ap;
        tloc = input.lst;
        for (j = 0; j < 14; j++) t[j] = 0;
        if (flags.sw[9] > 0) sw9 = 1; else if (flags.sw[9] < 0) sw9 = -1;
        xlong = input.g_long;
        c = sin(input.g_lat * dgtr);
        s = cos(input.g_lat * dgtr);
        c2 = c * c;
        c4 = c2 * c2;
        s2 = s * s;
        plg[0][1] = c;
        plg[0][2] = 0.5 * (3.0 * c2 - 1.0);
        plg[0][3] = 0.5 * (5.0 * c * c2 - 3.0 * c);
        plg[0][4] = (35.0 * c4 - 30.0 * c2 + 3.0) / 8.0;
        plg[0][5] = (63.0 * c2 * c2 * c - 70.0 * c2 * c + 15.0 * c) / 8.0;
        plg[0][6] = (11.0 * c * plg[0][5] - 5.0 * plg[0][4]) / 6.0;
        plg[1][1] = s;
        plg[1][2] = 3.0 * c * s;
        plg[1][3] = 1.5 * (5.0 * c2 - 1.0) * s;
        plg[1][4] = 2.5 * (7.0 * c2 * c - 3.0 * c) * s;
        plg[1][5] = 1.875 * (21.0 * c4 - 14.0 * c2 + 1.0) * s;
        plg[1][6] = (11.0 * c * plg[1][5] - 6.0 * plg[1][4]) / 5.0;
        plg[2][2] = 3.0 * s2;
        plg[2][3] = 15.0 * s2 * c;
        plg[2][4] = 7.5 * (7.0 * c2 - 1.0) * s2;
        plg[2][5] = 3.0 * c * plg[2][4] - 2.0 * plg[2][3];
        plg[2][6] = (11.0 * c * plg[2][5] - 7.0 * plg[2][4]) / 4.0;
        plg[2][7] = (13.0 * c * plg[2][6] - 8.0 * plg[2][5]) / 5.0;
        plg[3][3] = 15.0 * s2 * s;
        plg[3][4] = 105.0 * s2 * s * c;
        plg[3][5] = (9.0 * c * plg[3][4] - 7. * plg[3][3]) / 2.0;
        plg[3][6] = (11.0 * c * plg[3][5] - 8. * plg[3][4]) / 3.0;
        if (!(((flags.sw[7] == 0) && (flags.sw[8] == 0)) && (flags.sw[14] == 0))) {
            stloc = sin(hr * tloc);
            ctloc = cos(hr * tloc);
            s2tloc = sin(2.0 * hr * tloc);
            c2tloc = cos(2.0 * hr * tloc);
            s3tloc = sin(3.0 * hr * tloc);
            c3tloc = cos(3.0 * hr * tloc);
        }
        cd32 = cos(dr * (input.doy - p[31]));
        cd18 = cos(2.0 * dr * (input.doy - p[17]));
        cd14 = cos(dr * (input.doy - p[13]));
        cd39 = cos(2.0 * dr * (input.doy - p[38]));
        p32 = p[31];
        p18 = p[17];
        p14 = p[13];
        p39 = p[38];
        df = input.f107 - input.f107A;
        dfa = input.f107A - 150.0;
        t[0] = p[19] * df * (1.0 + p[59] * dfa) + p[20] * df * df + p[21] * dfa + p[29] * pow(dfa, 2.0);
        f1 = 1.0 + (p[47] * dfa + p[19] * df + p[20] * df * df) * flags.swc[1];
        f2 = 1.0 + (p[49] * dfa + p[19] * df + p[20] * df * df) * flags.swc[1];
        t[1] = (p[1] * plg[0][2] + p[2] * plg[0][4] + p[22] * plg[0][6]) + (p[14] * plg[0][2]) * dfa * flags.swc[1] + p[26] * plg[0][1];
        t[2] = p[18] * cd32;
        t[3] = (p[15] + p[16] * plg[0][2]) * cd18;
        t[4] = f1 * (p[9] * plg[0][1] + p[10] * plg[0][3]) * cd14;
        t[5] = p[37] * plg[0][1] * cd39;
        if (bool(flags.sw[7])) {
            double t71, t72;
            t71 = (p[11] * plg[1][2]) * cd14 * flags.swc[5];
            t72 = (p[12] * plg[1][2]) * cd14 * flags.swc[5];
            t[6] = f2 * ((p[3] * plg[1][1] + p[4] * plg[1][3] + p[27] * plg[1][5] + t71) * ctloc + (p[6] * plg[1][1] + p[7] * plg[1][3] + p[28] * plg[1][5] + t72) * stloc);
        }
        if (bool(flags.sw[8])) {
            double t81, t82;
            t81 = (p[23] * plg[2][3] + p[35] * plg[2][5]) * cd14 * flags.swc[5];
            t82 = (p[33] * plg[2][3] + p[36] * plg[2][5]) * cd14 * flags.swc[5];
            t[7] = f2 * ((p[5] * plg[2][2] + p[41] * plg[2][4] + t81) * c2tloc + (p[8] * plg[2][2] + p[42] * plg[2][4] + t82) * s2tloc);
        }
        if (bool(flags.sw[14])) {
            t[13] = f2 * ((p[39] * plg[3][3] + (p[93] * plg[3][4] + p[46] * plg[3][6]) * cd14 * flags.swc[5]) * s3tloc + (p[40] * plg[3][3] + (p[94] * plg[3][4] + p[48] * plg[3][6]) * cd14 * flags.swc[5]) * c3tloc);
        }
        if (flags.sw[9] == -1) {
            ap = input.ap_a;
            if (p[51] != 0) {
                double exp1;
                exp1 = exp(-10800.0 * sqrt(p[51] * p[51]) / (1.0 + p[138] * (45.0 - sqrt(input.g_lat * input.g_lat))));
                if (exp1 > 0.99999) exp1 = 0.99999;
                if (p[24] < 1.0E-4) p[24] = 1.0E-4;
                apt[0] = sg0(exp1, p, ap.a);
                if (bool(flags.sw[9])) {
                    t[8] = apt[0] * (p[50] + p[96] * plg[0][2] + p[54] * plg[0][4] + (p[125] * plg[0][1] + p[126] * plg[0][3] + p[127] * plg[0][5]) * cd14 * flags.swc[5] + (p[128] * plg[1][1] + p[129] * plg[1][3] + p[130] * plg[1][5]) * flags.swc[7] * cos(hr * (tloc - p[131])));
                }
            }
        } else {
            double p44, p45;
            apd = input.ap - 4.0;
            p44 = p[43];
            p45 = p[44];
            if (p44 < 0) p44 = 1.0E-5;
            apdf = apd + (p45 - 1.0) * (apd + (exp(-p44 * apd) - 1.0) / p44);
            if (bool(flags.sw[9])) {
                t[8] = apdf * (p[32] + p[45] * plg[0][2] + p[34] * plg[0][4] + (p[100] * plg[0][1] + p[101] * plg[0][3] + p[102] * plg[0][5]) * cd14 * flags.swc[5] + (p[121] * plg[1][1] + p[122] * plg[1][3] + p[123] * plg[1][5]) * flags.swc[7] * cos(hr * (tloc - p[124])));
            }
        }
        if (bool(flags.sw[10]) && (input.g_long > -1000.0)) {
            if (bool(flags.sw[11])) {
                t[10] = (1.0 + p[80] * dfa * flags.swc[1]) * ((p[64] * plg[1][2] + p[65] * plg[1][4] + p[66] * plg[1][6] + p[103] * plg[1][1] + p[104] * plg[1][3] + p[105] * plg[1][5] + flags.swc[5] * (p[109] * plg[1][1] + p[110] * plg[1][3] + p[111] * plg[1][5]) * cd14) * cos(dgtr * input.g_long) + (p[90] * plg[1][2] + p[91] * plg[1][4] + p[92] * plg[1][6] + p[106] * plg[1][1] + p[107] * plg[1][3] + p[108] * plg[1][5] + flags.swc[5] * (p[112] * plg[1][1] + p[113] * plg[1][3] + p[114] * plg[1][5]) * cd14) * sin(dgtr * input.g_long));
            }
            if (bool(flags.sw[12])) {
                t[11] = (1.0 + p[95] * plg[0][1]) * (1.0 + p[81] * dfa * flags.swc[1]) * (1.0 + p[119] * plg[0][1] * flags.swc[5] * cd14) * ((p[68] * plg[0][1] + p[69] * plg[0][3] + p[70] * plg[0][5]) * cos(sr * (input.sec - p[71])));
                t[11] += flags.swc[11] * (p[76] * plg[2][3] + p[77] * plg[2][5] + p[78] * plg[2][7]) * cos(sr * (input.sec - p[79]) + 2.0 * dgtr * input.g_long) * (1.0 + p[137] * dfa * flags.swc[1]);
            }
            if (bool(flags.sw[13])) {
                if (flags.sw[9] == -1) {
                    if (bool(p[51])) {
                        t[12] = apt[0] * flags.swc[11] * (1. + p[132] * plg[0][1]) * ((p[52] * plg[1][2] + p[98] * plg[1][4] + p[67] * plg[1][6]) * cos(dgtr * (input.g_long - p[97]))) + apt[0] * flags.swc[11] * flags.swc[5] * (p[133] * plg[1][1] + p[134] * plg[1][3] + p[135] * plg[1][5]) * cd14 * cos(dgtr * (input.g_long - p[136])) + apt[0] * flags.swc[12] * (p[55] * plg[0][1] + p[56] * plg[0][3] + p[57] * plg[0][5]) * cos(sr * (input.sec - p[58]));
                    }
                } else {
                    t[12] = apdf * flags.swc[11] * (1.0 + p[120] * plg[0][1]) * ((p[60] * plg[1][2] + p[61] * plg[1][4] + p[62] * plg[1][6]) * cos(dgtr * (input.g_long - p[63]))) + apdf * flags.swc[11] * flags.swc[5] * (p[115] * plg[1][1] + p[116] * plg[1][3] + p[117] * plg[1][5]) * cd14 * cos(dgtr * (input.g_long - p[118])) + apdf * flags.swc[12] * (p[83] * plg[0][1] + p[84] * plg[0][3] + p[85] * plg[0][5]) * cos(sr * (input.sec - p[75]));
                }
            }
        }
        tinf = p[30];
        for (i = 0; i < 14; i++) tinf = tinf + abs(flags.sw[i + 1]) * t[i];
        return tinf;
    }

    protected double glob7s(double[] p, struct_nrlmsise_input input, struct_nrlmsise_flags flags) {
        double pset = 2.0;
        double t[] = new double[14];
        double tt;
        double cd32, cd18, cd14, cd39;
        double p32, p18, p14, p39;
        int i, j;
        double dr = 1.72142E-2;
        if (p[99] == 0) p[99] = pset;
        if (p[99] != pset) {
            System.out.println("Wrong parameter set for glob7s");
            return -1;
        }
        for (j = 0; j < 14; j++) t[j] = 0.0;
        cd32 = cos(dr * (input.doy - p[31]));
        cd18 = cos(2.0 * dr * (input.doy - p[17]));
        cd14 = cos(dr * (input.doy - p[13]));
        cd39 = cos(2.0 * dr * (input.doy - p[38]));
        p32 = p[31];
        p18 = p[17];
        p14 = p[13];
        p39 = p[38];
        t[0] = p[21] * dfa;
        t[1] = p[1] * plg[0][2] + p[2] * plg[0][4] + p[22] * plg[0][6] + p[26] * plg[0][1] + p[14] * plg[0][3] + p[59] * plg[0][5];
        t[2] = (p[18] + p[47] * plg[0][2] + p[29] * plg[0][4]) * cd32;
        t[3] = (p[15] + p[16] * plg[0][2] + p[30] * plg[0][4]) * cd18;
        t[4] = (p[9] * plg[0][1] + p[10] * plg[0][3] + p[20] * plg[0][5]) * cd14;
        t[5] = (p[37] * plg[0][1]) * cd39;
        if (bool(flags.sw[7])) {
            double t71, t72;
            t71 = p[11] * plg[1][2] * cd14 * flags.swc[5];
            t72 = p[12] * plg[1][2] * cd14 * flags.swc[5];
            t[6] = ((p[3] * plg[1][1] + p[4] * plg[1][3] + t71) * ctloc + (p[6] * plg[1][1] + p[7] * plg[1][3] + t72) * stloc);
        }
        if (bool(flags.sw[8])) {
            double t81, t82;
            t81 = (p[23] * plg[2][3] + p[35] * plg[2][5]) * cd14 * flags.swc[5];
            t82 = (p[33] * plg[2][3] + p[36] * plg[2][5]) * cd14 * flags.swc[5];
            t[7] = ((p[5] * plg[2][2] + p[41] * plg[2][4] + t81) * c2tloc + (p[8] * plg[2][2] + p[42] * plg[2][4] + t82) * s2tloc);
        }
        if (bool(flags.sw[14])) {
            t[13] = p[39] * plg[3][3] * s3tloc + p[40] * plg[3][3] * c3tloc;
        }
        if (bool(flags.sw[9])) {
            if (flags.sw[9] == 1) t[8] = apdf * (p[32] + p[45] * plg[0][2] * flags.swc[2]);
            if (flags.sw[9] == -1) t[8] = (p[50] * apt[0] + p[96] * plg[0][2] * apt[0] * flags.swc[2]);
        }
        if (!((flags.sw[10] == 0) || (flags.sw[11] == 0) || (input.g_long <= -1000.0))) {
            t[10] = (1.0 + plg[0][1] * (p[80] * flags.swc[5] * cos(dr * (input.doy - p[81])) + p[85] * flags.swc[6] * cos(2.0 * dr * (input.doy - p[86]))) + p[83] * flags.swc[3] * cos(dr * (input.doy - p[84])) + p[87] * flags.swc[4] * cos(2.0 * dr * (input.doy - p[88]))) * ((p[64] * plg[1][2] + p[65] * plg[1][4] + p[66] * plg[1][6] + p[74] * plg[1][1] + p[75] * plg[1][3] + p[76] * plg[1][5]) * cos(dgtr * input.g_long) + (p[90] * plg[1][2] + p[91] * plg[1][4] + p[92] * plg[1][6] + p[77] * plg[1][1] + p[78] * plg[1][3] + p[79] * plg[1][5]) * sin(dgtr * input.g_long));
        }
        tt = 0;
        for (i = 0; i < 14; i++) tt += abs(flags.sw[i + 1]) * t[i];
        return tt;
    }

    /**   Neutral Atmosphere Empircial Model from the surface to lower
     *   exosphere.
     */
    protected void gtd7(struct_nrlmsise_input input, struct_nrlmsise_flags flags, struct_nrlmsise_output output) {
        double xlat;
        double xmm;
        int mn3 = 5;
        double zn3[] = { 32.5, 20.0, 15.0, 10.0, 0.0 };
        int mn2 = 4;
        double zn2[] = { 72.5, 55.0, 45.0, 32.5 };
        double altt;
        double zmix = 62.5;
        double tmp;
        double dm28m;
        doublestar tz = new doublestar();
        double dmc;
        double dmr;
        double dz28;
        struct_nrlmsise_output soutput = new struct_nrlmsise_output();
        int i;
        tselec(flags);
        xlat = input.g_lat;
        if (flags.sw[2] == 0) xlat = 45.0;
        glatf(xlat, gsurf, re);
        xmm = pdm[2][4];
        if (input.alt > zn2[0]) altt = input.alt; else altt = zn2[0];
        tmp = input.alt;
        input.alt = altt;
        gts7(input, flags, soutput);
        altt = input.alt;
        input.alt = tmp;
        if (bool(flags.sw[0])) dm28m = dm28 * 1.0E6; else dm28m = dm28;
        output.t[0] = soutput.t[0];
        output.t[1] = soutput.t[1];
        if (input.alt >= zn2[0]) {
            for (i = 0; i < 9; i++) output.d[i] = soutput.d[i];
            return;
        }
        meso_tgn2[0] = meso_tgn1[1];
        meso_tn2[0] = meso_tn1[4];
        meso_tn2[1] = pma[0][0] * pavgm[0] / (1.0 - flags.sw[20] * glob7s(pma[0], input, flags));
        meso_tn2[2] = pma[1][0] * pavgm[1] / (1.0 - flags.sw[20] * glob7s(pma[1], input, flags));
        meso_tn2[3] = pma[2][0] * pavgm[2] / (1.0 - flags.sw[20] * flags.sw[22] * glob7s(pma[2], input, flags));
        meso_tgn2[1] = pavgm[8] * pma[9][0] * (1.0 + flags.sw[20] * flags.sw[22] * glob7s(pma[9], input, flags)) * meso_tn2[3] * meso_tn2[3] / (pow((pma[2][0] * pavgm[2]), 2.0));
        meso_tn3[0] = meso_tn2[3];
        if (input.alt < zn3[0]) {
            meso_tgn3[0] = meso_tgn2[1];
            meso_tn3[1] = pma[3][0] * pavgm[3] / (1.0 - flags.sw[22] * glob7s(pma[3], input, flags));
            meso_tn3[2] = pma[4][0] * pavgm[4] / (1.0 - flags.sw[22] * glob7s(pma[4], input, flags));
            meso_tn3[3] = pma[5][0] * pavgm[5] / (1.0 - flags.sw[22] * glob7s(pma[5], input, flags));
            meso_tn3[4] = pma[6][0] * pavgm[6] / (1.0 - flags.sw[22] * glob7s(pma[6], input, flags));
            meso_tgn3[1] = pma[7][0] * pavgm[7] * (1.0 + flags.sw[22] * glob7s(pma[7], input, flags)) * meso_tn3[4] * meso_tn3[4] / (pow((pma[6][0] * pavgm[6]), 2.0));
        }
        dmc = 0;
        if (input.alt > zmix) dmc = 1.0 - (zn2[0] - input.alt) / (zn2[0] - zmix);
        dz28 = soutput.d[2];
        dmr = soutput.d[2] / dm28m - 1.0;
        output.d[2] = densm(input.alt, dm28m, xmm, tz, mn3, zn3, meso_tn3, meso_tgn3, mn2, zn2, meso_tn2, meso_tgn2);
        output.d[2] = output.d[2] * (1.0 + dmr * dmc);
        dmr = soutput.d[0] / (dz28 * pdm[0][1]) - 1.0;
        output.d[0] = output.d[2] * pdm[0][1] * (1.0 + dmr * dmc);
        output.d[1] = 0;
        output.d[8] = 0;
        dmr = soutput.d[3] / (dz28 * pdm[3][1]) - 1.0;
        output.d[3] = output.d[2] * pdm[3][1] * (1.0 + dmr * dmc);
        dmr = soutput.d[4] / (dz28 * pdm[4][1]) - 1.0;
        output.d[4] = output.d[2] * pdm[4][1] * (1.0 + dmr * dmc);
        output.d[6] = 0;
        output.d[7] = 0;
        output.d[5] = 1.66E-24 * (4.0 * output.d[0] + 16.0 * output.d[1] + 28.0 * output.d[2] + 32.0 * output.d[3] + 40.0 * output.d[4] + output.d[6] + 14.0 * output.d[7]);
        if (bool(flags.sw[0])) output.d[5] = output.d[5] / 1000;
        dd = densm(input.alt, 1.0, 0, tz, mn3, zn3, meso_tn3, meso_tgn3, mn2, zn2, meso_tn2, meso_tgn2);
        output.t[1].val = tz.val;
    }

    /**  This subroutine provides Effective Total Mass Density for output
     *   d[5] which includes contributions from "anomalous oxygen" which can
     *   affect satellite drag above 500 km. See the section "output" for
     *   additional details.
     */
    protected void gtd7d(struct_nrlmsise_input input, struct_nrlmsise_flags flags, struct_nrlmsise_output output) {
        gtd7(input, flags, output);
        output.d[5] = 1.66E-24 * (4.0 * output.d[0] + 16.0 * output.d[1] + 28.0 * output.d[2] + 32.0 * output.d[3] + 40.0 * output.d[4] + output.d[6] + 14.0 * output.d[7] + 16.0 * output.d[8]);
    }

    /**   To specify outputs at a pressure level (press) rather than at
     *   an altitude.
     */
    protected void ghp7(struct_nrlmsise_input input, struct_nrlmsise_flags flags, struct_nrlmsise_output output, double press) {
        double bm = 1.3806E-19;
        double rgas = 831.4;
        double test = 0.00043;
        double ltest = 12;
        double pl, p;
        double zi = 0;
        double z;
        double cl, cl2;
        double ca, cd;
        double xn, xm, diff;
        double g, sh;
        int l;
        pl = log10(press);
        if (pl >= -5.0) {
            if (pl > 2.5) zi = 18.06 * (3.00 - pl); else if ((pl > 0.075) && (pl <= 2.5)) zi = 14.98 * (3.08 - pl); else if ((pl > -1) && (pl <= 0.075)) zi = 17.80 * (2.72 - pl); else if ((pl > -2) && (pl <= -1)) zi = 14.28 * (3.64 - pl); else if ((pl > -4) && (pl <= -2)) zi = 12.72 * (4.32 - pl); else if (pl <= -4) zi = 25.3 * (0.11 - pl);
            cl = input.g_lat / 90.0;
            cl2 = cl * cl;
            if (input.doy < 182) cd = (1.0 - (double) input.doy) / 91.25; else cd = ((double) input.doy) / 91.25 - 3.0;
            ca = 0;
            if ((pl > -1.11) && (pl <= -0.23)) ca = 1.0;
            if (pl > -0.23) ca = (2.79 - pl) / (2.79 + 0.23);
            if ((pl <= -1.11) && (pl > -3)) ca = (-2.93 - pl) / (-2.93 + 1.11);
            z = zi - 4.87 * cl * cd * ca - 1.64 * cl2 * ca + 0.31 * ca * cl;
        } else z = 22.0 * pow((pl + 4.0), 2.0) + 110.0;
        l = 0;
        do {
            l++;
            input.alt = z;
            gtd7(input, flags, output);
            z = input.alt;
            xn = output.d[0] + output.d[1] + output.d[2] + output.d[3] + output.d[4] + output.d[6] + output.d[7];
            p = bm * xn * output.t[1].val;
            if (bool(flags.sw[0])) p = p * 1.0E-6;
            diff = pl - log10(p);
            if (sqrt(diff * diff) < test) return;
            if (l == ltest) {
                System.out.println("ERROR: ghp7 not converging for press " + press + ", diff " + diff);
                return;
            }
            xm = output.d[5] / xn / 1.66E-24;
            if (bool(flags.sw[0])) xm = xm * 1.0E3;
            g = gsurf.val / (pow((1.0 + z / re.val), 2.0));
            sh = rgas * output.t[1].val / (xm * g);
            if (l < 6) z = z - sh * diff * 2.302; else z = z - sh * diff;
        } while (1 == 1);
    }

    /**   Thermospheric portion of NRLMSISE-00
     */
    protected void gts7(struct_nrlmsise_input input, struct_nrlmsise_flags flags, struct_nrlmsise_output output) {
        double za;
        int i, j;
        double ddum, z;
        double zn1[] = { 120.0, 110.0, 100.0, 90.0, 72.5 };
        double tinf = 0;
        int mn1 = 5;
        double g0;
        double tlb;
        double s, z0, t0, tr12;
        double db01, db04, db14, db16, db28, db32, db40, db48;
        double zh28, zh04, zh16, zh32, zh40, zh01, zh14;
        double zhm28, zhm04, zhm16, zhm32, zhm40, zhm01, zhm14;
        double xmd;
        double b28, b04, b16, b32, b40, b01, b14;
        doublestar tz = new doublestar();
        double g28, g4, g16, g32, g40, g1, g14;
        double zhf, xmm;
        double zc04, zc16, zc32, zc40, zc01, zc14;
        double hc04, hc16, hc32, hc40, hc01, hc14;
        double hcc16, hcc32, hcc01, hcc14;
        double zcc16, zcc32, zcc01, zcc14;
        double rc16, rc32, rc01, rc14;
        double rl;
        double g16h, db16h, tho, zsht, zmho, zsho;
        double dr = 1.72142E-2;
        double alpha[] = { -0.38, 0.0, 0.0, 0.0, 0.17, 0.0, -0.38, 0.0, 0.0 };
        double altl[] = { 200.0, 300.0, 160.0, 250.0, 240.0, 450.0, 320.0, 450.0 };
        double dd;
        double hc216, hcc232;
        za = pdl[1][15];
        zn1[0] = za;
        for (j = 0; j < 9; j++) output.d[j] = 0;
        if (input.alt > zn1[0]) tinf = ptm[0] * pt[0] * (1.0 + flags.sw[16] * globe7(pt, input, flags)); else tinf = ptm[0] * pt[0];
        output.t[0].val = tinf;
        if (input.alt > zn1[4]) g0 = ptm[3] * ps[0] * (1.0 + flags.sw[19] * globe7(ps, input, flags)); else g0 = ptm[3] * ps[0];
        tlb = ptm[1] * (1.0 + flags.sw[17] * globe7(pd[3], input, flags)) * pd[3][0];
        s = g0 / (tinf - tlb);
        if (input.alt < 300.0) {
            meso_tn1[1] = ptm[6] * ptl[0][0] / (1.0 - flags.sw[18] * glob7s(ptl[0], input, flags));
            meso_tn1[2] = ptm[2] * ptl[1][0] / (1.0 - flags.sw[18] * glob7s(ptl[1], input, flags));
            meso_tn1[3] = ptm[7] * ptl[2][0] / (1.0 - flags.sw[18] * glob7s(ptl[2], input, flags));
            meso_tn1[4] = ptm[4] * ptl[3][0] / (1.0 - flags.sw[18] * flags.sw[20] * glob7s(ptl[3], input, flags));
            meso_tgn1[1] = ptm[8] * pma[8][0] * (1.0 + flags.sw[18] * flags.sw[20] * glob7s(pma[8], input, flags)) * meso_tn1[4] * meso_tn1[4] / (pow((ptm[4] * ptl[3][0]), 2.0));
        } else {
            meso_tn1[1] = ptm[6] * ptl[0][0];
            meso_tn1[2] = ptm[2] * ptl[1][0];
            meso_tn1[3] = ptm[7] * ptl[2][0];
            meso_tn1[4] = ptm[4] * ptl[3][0];
            meso_tgn1[1] = ptm[8] * pma[8][0] * meso_tn1[4] * meso_tn1[4] / (pow((ptm[4] * ptl[3][0]), 2.0));
        }
        z0 = zn1[3];
        t0 = meso_tn1[3];
        tr12 = 1.0;
        g28 = flags.sw[21] * globe7(pd[2], input, flags);
        zhf = pdl[1][24] * (1.0 + flags.sw[5] * pdl[0][24] * sin(dgtr * input.g_lat) * cos(dr * (input.doy - pt[13])));
        output.t[0].val = tinf;
        xmm = pdm[2][4];
        z = input.alt;
        db28 = pdm[2][0] * exp(g28) * pd[2][0];
        output.d[2] = densu(z, db28, tinf, tlb, 28.0, alpha[2], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[2];
        zh28 = pdm[2][2] * zhf;
        zhm28 = pdm[2][3] * pdl[1][5];
        xmd = 28.0 - xmm;
        b28 = densu(zh28, db28, tinf, tlb, xmd, (alpha[2] - 1.0), tz, ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        if (bool(flags.sw[15]) && (z <= altl[2])) {
            dm28 = densu(z, b28, tinf, tlb, xmm, alpha[2], tz, ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            output.d[2] = dnet(output.d[2], dm28, zhm28, xmm, 28.0);
        }
        g4 = flags.sw[21] * globe7(pd[0], input, flags);
        db04 = pdm[0][0] * exp(g4) * pd[0][0];
        output.d[0] = densu(z, db04, tinf, tlb, 4., alpha[0], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[0];
        if (bool(flags.sw[15]) && (z < altl[0])) {
            zh04 = pdm[0][2];
            b04 = densu(zh04, db04, tinf, tlb, 4. - xmm, alpha[0] - 1., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            dm04 = densu(z, b04, tinf, tlb, xmm, 0., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            zhm04 = zhm28;
            output.d[0] = dnet(output.d[0], dm04, zhm04, xmm, 4.);
            rl = log(b28 * pdm[0][1] / b04);
            zc04 = pdm[0][4] * pdl[1][0];
            hc04 = pdm[0][5] * pdl[1][1];
            output.d[0] = output.d[0] * ccor(z, rl, hc04, zc04);
        }
        g16 = flags.sw[21] * globe7(pd[1], input, flags);
        db16 = pdm[1][0] * exp(g16) * pd[1][0];
        output.d[1] = densu(z, db16, tinf, tlb, 16., alpha[1], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[1];
        if (bool(flags.sw[15]) && (z <= altl[1])) {
            zh16 = pdm[1][2];
            b16 = densu(zh16, db16, tinf, tlb, 16.0 - xmm, (alpha[1] - 1.0), output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            dm16 = densu(z, b16, tinf, tlb, xmm, 0., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            zhm16 = zhm28;
            output.d[1] = dnet(output.d[1], dm16, zhm16, xmm, 16.);
            rl = pdm[1][1] * pdl[1][16] * (1.0 + flags.sw[1] * pdl[0][23] * (input.f107A - 150.0));
            hc16 = pdm[1][5] * pdl[1][3];
            zc16 = pdm[1][4] * pdl[1][2];
            hc216 = pdm[1][5] * pdl[1][4];
            output.d[1] = output.d[1] * ccor2(z, rl, hc16, zc16, hc216);
            hcc16 = pdm[1][7] * pdl[1][13];
            zcc16 = pdm[1][6] * pdl[1][12];
            rc16 = pdm[1][3] * pdl[1][14];
            output.d[1] = output.d[1] * ccor(z, rc16, hcc16, zcc16);
        }
        g32 = flags.sw[21] * globe7(pd[4], input, flags);
        db32 = pdm[3][0] * exp(g32) * pd[4][0];
        output.d[3] = densu(z, db32, tinf, tlb, 32., alpha[3], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[3];
        if (bool(flags.sw[15])) {
            if (z <= altl[3]) {
                zh32 = pdm[3][2];
                b32 = densu(zh32, db32, tinf, tlb, 32. - xmm, alpha[3] - 1., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
                dm32 = densu(z, b32, tinf, tlb, xmm, 0., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
                zhm32 = zhm28;
                output.d[3] = dnet(output.d[3], dm32, zhm32, xmm, 32.);
                rl = log(b28 * pdm[3][1] / b32);
                hc32 = pdm[3][5] * pdl[1][7];
                zc32 = pdm[3][4] * pdl[1][6];
                output.d[3] = output.d[3] * ccor(z, rl, hc32, zc32);
            }
            hcc32 = pdm[3][7] * pdl[1][22];
            hcc232 = pdm[3][7] * pdl[0][22];
            zcc32 = pdm[3][6] * pdl[1][21];
            rc32 = pdm[3][3] * pdl[1][23] * (1. + flags.sw[1] * pdl[0][23] * (input.f107A - 150.));
            output.d[3] = output.d[3] * ccor2(z, rc32, hcc32, zcc32, hcc232);
        }
        g40 = flags.sw[20] * globe7(pd[5], input, flags);
        db40 = pdm[4][0] * exp(g40) * pd[5][0];
        output.d[4] = densu(z, db40, tinf, tlb, 40., alpha[4], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[4];
        if (bool(flags.sw[15]) && (z <= altl[4])) {
            zh40 = pdm[4][2];
            b40 = densu(zh40, db40, tinf, tlb, 40. - xmm, alpha[4] - 1., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            dm40 = densu(z, b40, tinf, tlb, xmm, 0., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            zhm40 = zhm28;
            output.d[4] = dnet(output.d[4], dm40, zhm40, xmm, 40.);
            rl = log(b28 * pdm[4][1] / b40);
            hc40 = pdm[4][5] * pdl[1][9];
            zc40 = pdm[4][4] * pdl[1][8];
            output.d[4] = output.d[4] * ccor(z, rl, hc40, zc40);
        }
        g1 = flags.sw[21] * globe7(pd[6], input, flags);
        db01 = pdm[5][0] * exp(g1) * pd[6][0];
        output.d[6] = densu(z, db01, tinf, tlb, 1., alpha[6], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[6];
        if (bool(flags.sw[15]) && (z <= altl[6])) {
            zh01 = pdm[5][2];
            b01 = densu(zh01, db01, tinf, tlb, 1. - xmm, alpha[6] - 1., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            dm01 = densu(z, b01, tinf, tlb, xmm, 0., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            zhm01 = zhm28;
            output.d[6] = dnet(output.d[6], dm01, zhm01, xmm, 1.);
            rl = log(b28 * pdm[5][1] * sqrt(pdl[1][17] * pdl[1][17]) / b01);
            hc01 = pdm[5][5] * pdl[1][11];
            zc01 = pdm[5][4] * pdl[1][10];
            output.d[6] = output.d[6] * ccor(z, rl, hc01, zc01);
            hcc01 = pdm[5][7] * pdl[1][19];
            zcc01 = pdm[5][6] * pdl[1][18];
            rc01 = pdm[5][3] * pdl[1][20];
            output.d[6] = output.d[6] * ccor(z, rc01, hcc01, zcc01);
        }
        g14 = flags.sw[21] * globe7(pd[7], input, flags);
        db14 = pdm[6][0] * exp(g14) * pd[7][0];
        output.d[7] = densu(z, db14, tinf, tlb, 14., alpha[7], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        dd = output.d[7];
        if (bool(flags.sw[15]) && (z <= altl[7])) {
            zh14 = pdm[6][2];
            b14 = densu(zh14, db14, tinf, tlb, 14. - xmm, alpha[7] - 1., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            dm14 = densu(z, b14, tinf, tlb, xmm, 0., output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
            zhm14 = zhm28;
            output.d[7] = dnet(output.d[7], dm14, zhm14, xmm, 14.);
            rl = log(b28 * pdm[6][1] * sqrt(pdl[0][2] * pdl[0][2]) / b14);
            hc14 = pdm[6][5] * pdl[0][1];
            zc14 = pdm[6][4] * pdl[0][0];
            output.d[7] = output.d[7] * ccor(z, rl, hc14, zc14);
            hcc14 = pdm[6][7] * pdl[0][4];
            zcc14 = pdm[6][6] * pdl[0][3];
            rc14 = pdm[6][3] * pdl[0][5];
            output.d[7] = output.d[7] * ccor(z, rc14, hcc14, zcc14);
        }
        g16h = flags.sw[21] * globe7(pd[8], input, flags);
        db16h = pdm[7][0] * exp(g16h) * pd[8][0];
        tho = pdm[7][9] * pdl[0][6];
        dd = densu(z, db16h, tho, tho, 16., alpha[8], output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        zsht = pdm[7][5];
        zmho = pdm[7][4];
        zsho = scalh(zmho, 16.0, tho);
        output.d[8] = dd * exp(-zsht / zsho * (exp(-(z - zmho) / zsht) - 1.));
        output.d[5] = 1.66E-24 * (4.0 * output.d[0] + 16.0 * output.d[1] + 28.0 * output.d[2] + 32.0 * output.d[3] + 40.0 * output.d[4] + output.d[6] + 14.0 * output.d[7]);
        db48 = 1.66E-24 * (4.0 * db04 + 16.0 * db16 + 28.0 * db28 + 32.0 * db32 + 40.0 * db40 + db01 + 14.0 * db14);
        z = sqrt(input.alt * input.alt);
        ddum = densu(z, 1.0, tinf, tlb, 0.0, 0.0, output.t[1], ptm[5], s, mn1, zn1, meso_tn1, meso_tgn1);
        if (bool(flags.sw[0])) {
            for (i = 0; i < 9; i++) output.d[i] = output.d[i] * 1.0E6;
            output.d[5] = output.d[5] / 1000;
        }
    }

    public void run() {
        struct_nrlmsise_output output[] = new struct_nrlmsise_output[17];
        struct_nrlmsise_input input[] = new struct_nrlmsise_input[17];
        for (int i = 0; i < 17; i++) {
            output[i] = new struct_nrlmsise_output();
            input[i] = new struct_nrlmsise_input();
        }
        struct_nrlmsise_flags flags = new struct_nrlmsise_flags();
        struct_ap_array aph = new struct_ap_array();
        int i;
        int j;
        for (i = 0; i < 7; i++) aph.a[i] = 100;
        flags.switches[0] = 0;
        for (i = 1; i < 24; i++) flags.switches[i] = 1;
        for (i = 0; i < 17; i++) {
            input[i].doy = 172;
            input[i].year = 0;
            input[i].sec = 29000;
            input[i].alt = 400;
            input[i].g_lat = 60;
            input[i].g_long = -70;
            input[i].lst = 16;
            input[i].f107A = 150;
            input[i].f107 = 150;
            input[i].ap = 4;
        }
        input[1].doy = 81;
        input[2].sec = 75000;
        input[2].alt = 1000;
        input[3].alt = 100;
        input[10].alt = 0;
        input[11].alt = 10;
        input[12].alt = 30;
        input[13].alt = 50;
        input[14].alt = 70;
        input[16].alt = 100;
        input[4].g_lat = 0;
        input[5].g_long = 0;
        input[6].lst = 4;
        input[7].f107A = 70;
        input[8].f107 = 180;
        input[9].ap = 40;
        input[15].ap_a = aph;
        input[16].ap_a = aph;
        for (i = 0; i < 15; i++) gtd7(input[i], flags, output[i]);
        flags.switches[9] = -1;
        for (i = 15; i < 17; i++) gtd7(input[i], flags, output[i]);
        for (i = 0; i < 17; i++) {
            System.out.print("\n");
            for (j = 0; j < 9; j++) System.out.print(" | " + output[i].d[j]);
            System.out.print(" | " + output[i].t[0].val);
            System.out.print(" | " + output[i].t[1].val + "\n");
        }
        for (i = 0; i < 3; i++) {
            System.out.print("\n");
            System.out.print("\nDAY   ");
            for (j = 0; j < 5; j++) System.out.print("         " + input[i * 5 + j].doy);
            System.out.print("\nUT    ");
            for (j = 0; j < 5; j++) System.out.print("       " + input[i * 5 + j].sec);
            System.out.print("\nALT   ");
            for (j = 0; j < 5; j++) System.out.print("        " + input[i * 5 + j].alt);
            System.out.print("\nLAT   ");
            for (j = 0; j < 5; j++) System.out.print("         " + input[i * 5 + j].g_lat);
            System.out.print("\nLONG  ");
            for (j = 0; j < 5; j++) System.out.print("         " + input[i * 5 + j].g_long);
            System.out.print("\nLST   ");
            for (j = 0; j < 5; j++) System.out.print("       " + input[i * 5 + j].lst);
            System.out.print("\nF107A ");
            for (j = 0; j < 5; j++) System.out.print("         " + input[i * 5 + j].f107A);
            System.out.print("\nF107  ");
            for (j = 0; j < 5; j++) System.out.print("         " + input[i * 5 + j].f107);
            System.out.print("\n\n");
            System.out.print("\nTINF  ");
            for (j = 0; j < 5; j++) System.out.print("     " + output[i * 5 + j].t[0].val);
            System.out.print("\nTG    ");
            for (j = 0; j < 5; j++) System.out.print("     " + output[i * 5 + j].t[1].val);
            System.out.print("\nHE    ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[0]);
            System.out.print("\nO     ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[1]);
            System.out.print("\nN2    ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[2]);
            System.out.print("\nO2    ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[3]);
            System.out.print("\nAR    ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[4]);
            System.out.print("\nH     ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[6]);
            System.out.print("\nN     ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[7]);
            System.out.print("\nANM 0 ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[8]);
            System.out.print("\nRHO   ");
            for (j = 0; j < 5; j++) System.out.print("   " + output[i * 5 + j].d[5]);
            System.out.print("\n");
        }
        System.out.println("\n");
    }

    public static void main(String[] args) {
        Spacecraft sc = new Spacecraft();
        NRLMSISE_Drag test = new NRLMSISE_Drag(sc);
        test.run();
        System.out.println("FINISHED");
    }
}
