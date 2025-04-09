import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

/**
 * Implements CTEQ6 parton distribution functions.
 */
public class PdfCteq6 {

    int currentSet;

    URL base;

    public static final int MXX = 201;

    public static final int MXQ = 25;

    public static final int MXF = 6;

    public static final int MAXVAL = 4;

    public static final int MXPQX = (MXF + MAXVAL + 1) * MXQ * MXX;

    double a1;

    double[] xv;

    double[] tv;

    double[] upd;

    int nx;

    int nt;

    int nfmx;

    int mxval;

    double qini;

    double qmax;

    double xmin;

    double alambda;

    int nfl;

    int iorder;

    double[] amass;

    boolean setChange;

    public static void main(String[] args) throws IOException {
        PdfCteq6 pdf = new PdfCteq6(200, new File("cteq_pdf"));
        double x = 0.12755102040816327;
        double q = 5000.0;
        double[] answers = new double[] { 3.583511, 1.611860, 0.376504, 0.518768, 0.282262, 0.171954, 0.114409, 0.0, 3.286047 };
        double zero = 0.000001;
        for (int i = 0; i < 9; i++) {
            double a = pdf.getParton(x, q, i);
            System.out.println("Real: " + answers[i] + ", Us: " + a + ", difference<1E-6: " + (Math.abs(answers[i] - a) < zero));
        }
    }

    /**
     * Read in pdf parameterization.
     *
     * @param ipdf pdf index (should be between 200 and 240, inclusive)
     * @param base URL to find tbl files with pdf parameterizations
     */
    public PdfCteq6(int ipdf, URL base) throws IllegalArgumentException, NumberFormatException, IOException, MalformedURLException {
        if (ipdf > 240 || ipdf < 200) {
            throw new IllegalArgumentException("Requested pdf set " + ipdf + " out of range (200 to 240)");
        }
        this.currentSet = -1;
        this.base = base;
        readPds(ipdf);
    }

    public PdfCteq6(int ipdf, File base) throws IllegalArgumentException, NumberFormatException, IOException, MalformedURLException {
        if (ipdf > 240 || ipdf < 200) {
            throw new IllegalArgumentException("Requested pdf set " + ipdf + " out of range (200 to 240)");
        }
        this.currentSet = -1;
        readPds(ipdf, base);
    }

    public void readPds(int ipdf, File base) throws IOException, MalformedURLException, NumberFormatException {
        if (ipdf == currentSet) return;
        StringBuilder sb = new StringBuilder("ctq61.");
        int offset = ipdf - 200;
        if (offset < 10) sb.append('0');
        sb.append(offset).append(".tbl");
        FileTableReader tbl = new FileTableReader(new File(base, sb.toString()));
        tbl.skipLine();
        iorder = (int) tbl.readDouble();
        nfl = (int) tbl.readDouble();
        alambda = tbl.readDouble();
        amass = new double[MXF];
        for (int i = 0; i < MXF; ++i) amass[i] = tbl.readDouble();
        tbl.skipLine();
        mxval = 2;
        nx = (int) tbl.readDouble();
        nt = (int) tbl.readDouble();
        nfmx = (int) tbl.readDouble();
        tbl.skipLine();
        qini = tbl.readDouble();
        qmax = tbl.readDouble();
        tv = new double[nt + 1];
        for (int i = 0; i <= nt; ++i) tv[i] = tbl.readDouble();
        tbl.skipLine();
        xmin = tbl.readDouble();
        xv = new double[nx + 1];
        for (int i = 0; i <= nx; ++i) xv[i] = tbl.readDouble();
        for (int i = 0; i <= nt; ++i) {
            tv[i] = Math.log(Math.log(tv[i] / alambda));
        }
        int nblk = (nx + 1) * (nt + 1);
        int npts = nblk * (nfmx + 1 + mxval);
        tbl.skipLine();
        upd = new double[npts];
        for (int i = 0; i < npts; ++i) upd[i] = tbl.readDouble();
        tbl.close();
        this.currentSet = ipdf;
        this.setChange = true;
    }

    public void readPds(int ipdf) throws IOException, MalformedURLException, NumberFormatException {
        if (ipdf == currentSet) return;
        StringBuilder sb = new StringBuilder("ctq61.");
        int offset = ipdf - 200;
        if (offset < 10) sb.append('0');
        sb.append(offset).append(".tbl");
        URL pdfFile = new URL(base, sb.toString());
        TblReader tbl = new TblReader(pdfFile);
        tbl.skipLine();
        iorder = (int) tbl.readDouble();
        nfl = (int) tbl.readDouble();
        alambda = tbl.readDouble();
        for (int i = 0; i < MXF; ++i) amass[i] = tbl.readDouble();
        tbl.skipLine();
        mxval = 2;
        nx = (int) tbl.readDouble();
        nt = (int) tbl.readDouble();
        nfmx = (int) tbl.readDouble();
        tbl.skipLine();
        qini = tbl.readDouble();
        qmax = tbl.readDouble();
        tv = new double[nt + 1];
        for (int i = 0; i <= nt; ++i) tv[i] = tbl.readDouble();
        tbl.skipLine();
        xmin = tbl.readDouble();
        xv = new double[nx + 1];
        for (int i = 0; i <= nx; ++i) xv[i] = tbl.readDouble();
        for (int i = 0; i <= nt; ++i) {
            tv[i] = Math.log(Math.log(tv[i] / alambda));
        }
        int nblk = (nx + 1) * (nt + 1);
        int npts = nblk * (nfmx + 1 + mxval);
        tbl.skipLine();
        upd = new double[npts];
        for (int i = 0; i < npts; ++i) upd[i] = tbl.readDouble();
        tbl.close();
        this.currentSet = ipdf;
        this.setChange = true;
    }

    /**
     * Return momentum-weighted parton density.
     * (getPdf returns simple parton density)
     */
    public double getParton(double x, double q, int iparton) throws IllegalArgumentException {
        if (x > 0.95) return 0.0;
        switch(iparton) {
            case 0:
            case 1:
                return getPdf(iparton + 1, x, q) - getPdf(-iparton - 1, x, q);
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return getPdf(1 - iparton, x, q);
            case 7:
                return 0.0;
            case 8:
                return getPdf(0, x, q);
            default:
                if (x > 0.95) return 0.0; else throw new IllegalArgumentException("Requested parton " + iparton + " out of range");
        }
    }

    /**
     * Accessor function to convert fortran index to Java index
     */
    public double getUpd(int index) {
        return upd[index - 1];
    }

    /**
     * Accessor function to get quark mass
     */
    public double getMass(int index) {
        return amass[index - 1];
    }

    /**
     * simple parton density evaluator
     */
    public double getPdf(int iparton, double x, double q) throws IllegalArgumentException {
        if (x < 0.0 || x > 1.0) {
            throw new IllegalArgumentException("x = " + x + " out of range");
        }
        if (q < alambda) {
            throw new IllegalArgumentException("Q = " + q + " out of range");
        }
        if (iparton < -nfmx || iparton > nfmx) {
            throw new IllegalArgumentException("iparton = " + iparton + " out of range");
        }
        return Math.max(partonx6(iparton, x, q), 0.0);
    }

    public static final double ONEP = 1.00001;

    public static final double XPOW = 0.3;

    public static final int NQVEC = 4;

    private double xold, qold;

    private double[] xvpow;

    private int jx, jq, jlx, jlq;

    private double ss, const1, const2, const3, const4, const5, const6;

    private double sy2, sy3, s23, tt, t12, t13, t23, t24, t34, ty2, ty3;

    private double tmp1, tmp2, tdet;

    /**
     * Interpolate pdf between lattice points.
     */
    protected double partonx6(int iprtn, double x, double q) {
        if (x != xold || q != qold) {
            if (setChange) {
                setChange = false;
                xvpow = new double[xv.length];
                xvpow[0] = 0.0;
                for (int i = 1; i <= nx; ++i) xvpow[i] = Math.pow(xv[i], XPOW);
            }
            xold = x;
            qold = q;
            tt = Math.log(Math.log(q / alambda));
            jlx = -1;
            int ju = nx + 1;
            while (ju - jlx > 1) {
                int jm = (ju + jlx) / 2;
                if (x >= xv[jm]) jlx = jm; else ju = jm;
            }
            jx = -1;
            if (jlx <= -1) {
                throw new IllegalArgumentException("x <= 0 in partonx6, x = " + x);
            } else if (jlx == 0) {
                jx = 0;
            } else if (jlx <= nx - 2) {
                jx = jlx - 1;
            } else if (jlx == nx - 1 || x < ONEP) {
                jx = jlx - 2;
            } else {
                throw new IllegalArgumentException("x > 1 in partonx6, x = " + x);
            }
            ss = Math.pow(x, XPOW);
            if (jlx >= 2 && jlx <= nx - 2) {
                double svec1 = xvpow[jx];
                double svec2 = xvpow[jx + 1];
                double svec3 = xvpow[jx + 2];
                double svec4 = xvpow[jx + 3];
                double s12 = svec1 - svec2;
                double s13 = svec1 - svec3;
                s23 = svec2 - svec3;
                double s24 = svec2 - svec4;
                double s34 = svec3 - svec4;
                sy2 = ss - svec2;
                sy3 = ss - svec3;
                const1 = s13 / s23;
                const2 = s12 / s23;
                const3 = s34 / s23;
                const4 = s24 / s23;
                double s1213 = s12 + s13;
                double s2434 = s24 + s34;
                double sdet = s12 * s34 - s1213 * s2434;
                double tmp = sy2 * sy3 / sdet;
                const5 = (s34 * sy2 - s2434 * sy3) * tmp / s12;
                const6 = (s1213 * sy2 - s12 * sy3) * tmp / s34;
            }
            jlq = -1;
            ju = nt + 1;
            while (ju - jlq > 1) {
                int jm = (ju + jlq) / 2;
                if (tt >= tv[jm]) jlq = jm; else ju = jm;
            }
            if (jlq <= 0) {
                jq = 0;
            } else if (jlq <= nt - 2) {
                jq = jlq - 1;
            } else {
                jq = nt - 3;
            }
            if (jlq >= 1 && jlq <= nt - 2) {
                double tvec1 = tv[jq];
                double tvec2 = tv[jq + 1];
                double tvec3 = tv[jq + 2];
                double tvec4 = tv[jq + 4];
                t12 = tvec1 - tvec2;
                t13 = tvec1 - tvec3;
                t23 = tvec2 - tvec3;
                t24 = tvec2 - tvec4;
                t34 = tvec3 - tvec4;
                ty2 = tt - tvec2;
                ty3 = tt - tvec3;
                tmp1 = t12 + t13;
                tmp2 = t24 + t34;
                tdet = t12 * t34 - tmp1 * tmp2;
            }
        }
        int ip = (iprtn > mxval ? -iprtn : iprtn);
        int jtmp = ((ip + nfmx) * (nt + 1) + (jq - 1)) * (nx + 1) + jx + 1;
        double[] fvec = new double[NQVEC];
        for (int it = 1; it <= NQVEC; ++it) {
            int j1 = jtmp + it * (nx + 1);
            if (jx == 0) {
                double[] fij = new double[4];
                fij[0] = 0.0;
                fij[1] = getUpd(j1 + 1) * xv[1] * xv[1];
                fij[2] = getUpd(j1 + 2) * xv[2] * xv[2];
                fij[3] = getUpd(j1 + 3) * xv[3] * xv[3];
                double fx = polint4f(xvpow, 0, fij, 0, ss);
                if (x > 0.0) fvec[it - 1] = fx / (x * x);
            } else if (jlx == nx - 1) {
                double fx = polint4f(xvpow, nx - 3, upd, j1 - 1, ss);
                fvec[it - 1] = fx;
            } else {
                double sf2 = getUpd(j1 + 1);
                double sf3 = getUpd(j1 + 2);
                double g1 = sf2 * const1 - sf3 * const2;
                double g4 = -sf2 * const3 + sf3 * const4;
                fvec[it - 1] = (const5 * (getUpd(j1) - g1) + const6 * (getUpd(j1 + 3) - g4) + sf2 * sy3 - sf3 * sy2) / s23;
            }
        }
        double ff = -1.0;
        if (jlq <= 0) {
            ff = polint4f(tv, 0, fvec, 0, tt);
        } else if (jlq >= nt - 1) {
            ff = polint4f(tv, nt - 3, fvec, 0, tt);
        } else {
            double tf2 = fvec[1];
            double tf3 = fvec[2];
            double g1 = (tf2 * t13 - tf3 * t12) / t23;
            double g4 = (-tf2 * t34 + tf3 * t24) / t23;
            double h00 = ((t34 * ty2 - tmp2 * ty3) * (fvec[0] - g1) / t12 + (tmp1 * ty2 - t12 * ty3) * (fvec[3] - g4) / t34);
            ff = (h00 * ty2 * ty3 / tdet + tf2 * ty3 - tf3 * ty2) / t23;
        }
        return ff;
    }

    /**
     * Interpolation utility
     */
    public static double polint4f(double[] xa, int ixb, double[] ya, int iyb, double x) {
        double h1 = xa[ixb] - x;
        double h2 = xa[ixb + 1] - x;
        double h3 = xa[ixb + 2] - x;
        double h4 = xa[ixb + 3] - x;
        double w = ya[iyb + 1] - ya[iyb];
        double den = w / (h1 - h2);
        double d1 = h2 * den;
        double c1 = h1 * den;
        w = ya[iyb + 2] - ya[iyb + 1];
        den = w / (h2 - h3);
        double d2 = h3 * den;
        double c2 = h2 * den;
        w = ya[iyb + 3] - ya[iyb + 2];
        den = w / (h3 - h4);
        double d3 = h4 * den;
        double c3 = h3 * den;
        w = c2 - d1;
        den = w / (h1 - h3);
        double cd1 = h3 * den;
        double cc1 = h1 * den;
        w = c3 - d2;
        den = w / (h2 - h4);
        double cd2 = h4 * den;
        double cc2 = h2 * den;
        w = cc2 - cd1;
        den = w / (h1 - h4);
        double dd1 = h4 * den;
        double dc1 = h1 * den;
        if ((h3 + h4) < 0.0) return ya[iyb + 3] + d3 + cd2 + dd1; else if ((h2 + h3) < 0.0) return ya[iyb + 2] + d2 + cd1 + dc1; else if ((h1 + h2) < 0.0) return ya[iyb + 1] + c2 + cd1 + dc1; else return ya[iyb] + c1 + cc1 + dc1;
    }

    /**
     * Utility class for reading .tbl files
     */
    class TblReader {

        BufferedReader br;

        String current;

        double[] xin;

        int index;

        public TblReader(URL file) throws IOException {
            br = new BufferedReader(new InputStreamReader(file.openStream()));
            br.readLine();
        }

        public void skipLine() throws IOException {
            current = br.readLine();
            readNewLine();
        }

        public void readNewLine() throws IOException, NumberFormatException {
            current = br.readLine();
            String[] word = current.trim().split("\\s+");
            xin = new double[word.length];
            for (int i = 0; i < word.length; ++i) {
                xin[i] = Double.parseDouble(word[i]);
            }
            index = 0;
        }

        public double readDouble() throws IOException {
            if (index >= xin.length) readNewLine();
            return xin[index++];
        }

        public void close() throws IOException {
            br.close();
        }
    }

    class FileTableReader {

        BufferedReader br;

        String current;

        double[] xin;

        int index;

        public FileTableReader(File file) throws IOException {
            br = new BufferedReader(new FileReader(file));
            br.readLine();
        }

        public void skipLine() throws IOException {
            current = br.readLine();
            readNewLine();
        }

        public void readNewLine() throws IOException, NumberFormatException {
            current = br.readLine();
            String[] word = current.trim().split("\\s+");
            xin = new double[word.length];
            for (int i = 0; i < word.length; ++i) {
                xin[i] = Double.parseDouble(word[i]);
            }
            index = 0;
        }

        public double readDouble() throws IOException {
            if (index >= xin.length) readNewLine();
            return xin[index++];
        }

        public void close() throws IOException {
            br.close();
        }
    }
}
