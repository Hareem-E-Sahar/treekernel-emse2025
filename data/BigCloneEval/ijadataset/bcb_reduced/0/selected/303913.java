package horcher.maths2;

import java.util.Collection;
import java.util.Iterator;

public class Spectrum extends Datensatz<ComplexD> {

    private static final long serialVersionUID = 1L;

    private final double deltaF;

    /**
	 * Creates an Spectrum with the values given by Collection, where the last
	 * element represents the lowest frequency <code>deltaF</code>.
	 * 
	 * @param c
	 *          Collection containing the data
	 * @param deltaF
	 *          lowest frequency
	 */
    public Spectrum(final Collection<ComplexD> c, final double deltaF) {
        super(c);
        if (c.size() % 2 != 0) throw new IllegalArgumentException("Length of Collection must be a multiple of 2!");
        this.deltaF = deltaF;
        if (deltaF <= 0) throw new IllegalArgumentException("Frequenzy resolution must be higher than zero!");
    }

    public Spectrum(final double deltaF) {
        super();
        this.deltaF = deltaF;
        if (deltaF <= 0) throw new IllegalArgumentException("Frequenzy resolution must be higher than zero!");
    }

    public Spectrum(final int length, final double deltaF) {
        super(length - length % 2);
        if (length % 2 != 0) throw new IllegalArgumentException("Length must be a multiple of 2!");
        this.deltaF = deltaF;
        if (deltaF <= 0) throw new IllegalArgumentException("Frequenzy resolution must be higher than zero!");
    }

    public Kreuzleistungsspektrum correlate(final Spectrum yS) {
        final Iterator<ComplexD> it = this.iterator();
        final Iterator<ComplexD> it2 = yS.iterator();
        final Spectrum kreuzleistungsspektrum = new Spectrum(this.deltaF);
        ComplexD tmp;
        while (it.hasNext() && it2.hasNext()) {
            tmp = it2.next().copy();
            tmp.conjugate();
            tmp.mul(it.next());
            tmp.setIsComplex();
            kreuzleistungsspektrum.add(tmp);
        }
        return new Kreuzleistungsspektrum(kreuzleistungsspektrum);
    }

    /**
	 * Does a correlation of this signal with an other signal.
	 * 
	 * @param yS
	 *          the other signal
	 * @return the correlation of both signals
	 */
    public Correlation correlation(final Spectrum yS) {
        return this.correlate(yS).correlation();
    }

    /**
	 * Does a inverse Fast-Fourier-Transformation. Returns a signal leading with
	 * this spectrum.
	 * 
	 * @return signal with this spectrum
	 */
    public Signal fftI() {
        final Signal ret = new Signal(this.copy(), (int) (this.getDeltaFrequency() * this.size()));
        int mit2, iter, irem, it, it2, nxp, nxp2, m, mxp, j1, j2, k, n, i, j;
        ComplexD t, w;
        n = ret.size();
        for (iter = 0, irem = n / 2; irem != 0; irem /= 2, iter++) ;
        if (SIN_TABLE.dim != n) SIN_TABLE.ini_fft(n);
        for (it = 0, nxp2 = n, it2 = 1; it < iter; it++, it2 *= 2) {
            nxp = nxp2;
            nxp2 = nxp / 2;
            for (m = 0, mit2 = 0; m < nxp2; m++, mit2 += it2) {
                w = new ComplexD(SIN_TABLE.cosinus[mit2], SIN_TABLE.sinus[mit2]);
                for (mxp = nxp, j1 = m; mxp <= n; mxp += nxp, j1 += nxp) {
                    j2 = j1 + nxp2;
                    t = ret.get(j1).copy();
                    t.sub(ret.get(j2));
                    ret.get(j1).addi(ret.get(j2));
                    t.mul(w);
                    t.setIsComplex();
                    ret.set(j2, t);
                }
            }
        }
        for (i = 0, j = 0; i < n - 1; i++) {
            if (i < j) ret.change(i, j);
            k = n / 2;
            while (k <= j) {
                j -= k;
                k = (k + 1) / 2;
            }
            j += k;
        }
        return ret;
    }

    public synchronized double getDeltaF() {
        return this.deltaF;
    }

    /**
	 * Returns the lowest contained frequency
	 * 
	 * @return lowest contained frequency
	 */
    public synchronized double getDeltaFrequency() {
        return this.deltaF;
    }

    public double getHighestFrequency() {
        return this.getDeltaFrequency() * this.size();
    }

    public void zBewerten() {
        double f = 0;
        double Z_GRENZE = 61;
        System.out.println(this.deltaF);
        for (ComplexD tmp : this) {
            f += this.deltaF;
            if (f < Z_GRENZE || f > (this.getHighestFrequency() - Z_GRENZE)) {
                tmp.mul(new ComplexD(0, 0));
            }
        }
    }

    public void aBewerten() {
        double f = 0;
        int n1 = 12200 * 12200;
        float n2 = 20.6f * 20.6f;
        double LOG10E = 0.4342944819032518;
        double a;
        double f2;
        for (ComplexD tmp : this) {
            f += this.deltaF;
            f2 = f * f;
            a = n1 * f2 * f2;
            a /= ((f2 + n2) * (f2 + n1) * Math.sqrt(f2 + 107.7 * 107.7) * Math.sqrt(f2 + 737.9 * 737.9));
            a /= 0.79434639580229505;
            a = 20 * LOG10E * Math.log(a);
            if (Math.abs(a) < 0.0001) a = 0;
            tmp.mul(new ComplexD(a, 0));
        }
    }

    public void cBewerten() {
        double f = 0;
        int n1 = 12200 * 12200;
        float n2 = 20.6f * 20.6f;
        double LOG10E = 0.4342944819032518;
        double c;
        double f2;
        for (ComplexD tmp : this) {
            f += this.deltaF;
            f2 = f * f;
            c = n1 * f2;
            c /= ((f2 + n1) * (f2 + n2));
            c /= 0.9929048655202054;
            c = 20 * LOG10E * Math.log(c);
            if (Math.abs(c) < 0.0001) c = 0;
            tmp.mul(new ComplexD(c, 0));
        }
    }
}
