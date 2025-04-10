package edu.jas.root;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import edu.jas.arith.Rational;
import edu.jas.arith.BigRational;
import edu.jas.poly.Complex;
import edu.jas.poly.ComplexRing;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.PolyUtil;
import edu.jas.structure.RingElem;
import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;
import edu.jas.util.ArrayUtil;

/**
 * Complex roots implemented by Sturm sequences. Algorithms use exact method
 * derived from Wilf's numeric Routh-Hurwitz method.
 * @param <C> coefficient type.
 * @author Heinz Kredel
 */
public class ComplexRootsSturm<C extends RingElem<C> & Rational> extends ComplexRootsAbstract<C> {

    private static final Logger logger = Logger.getLogger(ComplexRootsSturm.class);

    private final boolean debug = logger.isDebugEnabled();

    /**
     * Constructor.
     * @param cf coefficient factory.
     */
    public ComplexRootsSturm(RingFactory<Complex<C>> cf) {
        super(cf);
    }

    /**
     * Cauchy index of rational function f/g on interval.
     * @param a interval bound for I = [a,b].
     * @param b interval bound for I = [a,b].
     * @param f univariate polynomial.
     * @param g univariate polynomial.
     * @return winding number of f/g in I.
     */
    public long indexOfCauchy(C a, C b, GenPolynomial<C> f, GenPolynomial<C> g) {
        List<GenPolynomial<C>> S = sturmSequence(g, f);
        if (debug) {
            logger.info("sturmSeq = " + S);
        }
        RingFactory<C> cfac = f.ring.coFac;
        List<C> l = PolyUtil.<C>evaluateMain(cfac, S, a);
        List<C> r = PolyUtil.<C>evaluateMain(cfac, S, b);
        long v = RootUtil.<C>signVar(l) - RootUtil.<C>signVar(r);
        return v;
    }

    /**
     * Routh index of complex function f + i g on interval.
     * @param a interval bound for I = [a,b].
     * @param b interval bound for I = [a,b].
     * @param f univariate polynomial.
     * @param g univariate polynomial != 0.
     * @return index number of f + i g.
     */
    public long[] indexOfRouth(C a, C b, GenPolynomial<C> f, GenPolynomial<C> g) {
        List<GenPolynomial<C>> S = sturmSequence(f, g);
        RingFactory<C> cfac = f.ring.coFac;
        List<C> l = PolyUtil.<C>evaluateMain(cfac, S, a);
        List<C> r = PolyUtil.<C>evaluateMain(cfac, S, b);
        long v = RootUtil.<C>signVar(l) - RootUtil.<C>signVar(r);
        long d = f.degree(0);
        if (d < g.degree(0)) {
            d = g.degree(0);
        }
        long ui = (d - v) / 2;
        long li = (d + v) / 2;
        return new long[] { ui, li };
    }

    /**
     * Sturm sequence.
     * @param f univariate polynomial.
     * @param g univariate polynomial.
     * @return a Sturm sequence for f and g.
     */
    public List<GenPolynomial<C>> sturmSequence(GenPolynomial<C> f, GenPolynomial<C> g) {
        List<GenPolynomial<C>> S = new ArrayList<GenPolynomial<C>>();
        if (f == null || f.isZERO()) {
            return S;
        }
        if (f.isConstant()) {
            S.add(f.monic());
            return S;
        }
        GenPolynomial<C> F = f;
        S.add(F);
        GenPolynomial<C> G = g;
        while (!G.isZERO()) {
            GenPolynomial<C> r = F.remainder(G);
            F = G;
            G = r.negate();
            S.add(F);
        }
        if (F.isConstant()) {
            return S;
        }
        List<GenPolynomial<C>> Sp = new ArrayList<GenPolynomial<C>>(S.size());
        for (GenPolynomial<C> p : S) {
            p = p.divide(F);
            Sp.add(p);
        }
        return Sp;
    }

    /**
     * Complex root count of complex polynomial on rectangle.
     * @param rect rectangle.
     * @param a univariate complex polynomial.
     * @return root count of a in rectangle.
     */
    @Override
    public long complexRootCount(Rectangle<C> rect, GenPolynomial<Complex<C>> a) throws InvalidBoundaryException {
        return windingNumber(rect, a);
    }

    /**
     * Winding number of complex function A on rectangle.
     * @param rect rectangle.
     * @param A univariate complex polynomial.
     * @return winding number of A arround rect.
     */
    public long windingNumber(Rectangle<C> rect, GenPolynomial<Complex<C>> A) throws InvalidBoundaryException {
        Boundary<C> bound = new Boundary<C>(rect, A);
        ComplexRing<C> cr = (ComplexRing<C>) A.ring.coFac;
        RingFactory<C> cf = cr.ring;
        C zero = cf.getZERO();
        C one = cf.getONE();
        long ix = 0L;
        for (int i = 0; i < 4; i++) {
            long ci = indexOfCauchy(zero, one, bound.getRealPart(i), bound.getImagPart(i));
            ix += ci;
        }
        if (ix % 2L != 0) {
            throw new InvalidBoundaryException("odd winding number " + ix);
        }
        return ix / 2L;
    }

    /**
     * List of complex roots of complex polynomial a on rectangle.
     * @param rect rectangle.
     * @param a univariate squarefree complex polynomial.
     * @return list of complex roots.
     */
    @Override
    public List<Rectangle<C>> complexRoots(Rectangle<C> rect, GenPolynomial<Complex<C>> a) throws InvalidBoundaryException {
        ComplexRing<C> cr = (ComplexRing<C>) a.ring.coFac;
        List<Rectangle<C>> roots = new ArrayList<Rectangle<C>>();
        if (a.isConstant() || a.isZERO()) {
            return roots;
        }
        long n = windingNumber(rect, a);
        if (n < 0) {
            throw new RuntimeException("negative winding number " + n);
        }
        if (n == 0) {
            return roots;
        }
        if (n == 1) {
            roots.add(rect);
            return roots;
        }
        Complex<C> eps = cr.fromInteger(1);
        eps = eps.divide(cr.fromInteger(1000));
        Complex<C> delta = rect.corners[3].subtract(rect.corners[1]);
        delta = delta.divide(cr.fromInteger(2));
        boolean work = true;
        while (work) {
            Complex<C> center = rect.corners[1].sum(delta);
            if (debug) {
                logger.info("new center = " + center);
            }
            try {
                Complex<C>[] cp = (Complex<C>[]) copyOfComplex(rect.corners, 4);
                cp[1] = new Complex<C>(cr, cp[1].getRe(), center.getIm());
                cp[2] = center;
                cp[3] = new Complex<C>(cr, center.getRe(), cp[3].getIm());
                Rectangle<C> nw = new Rectangle<C>(cp);
                List<Rectangle<C>> nwr = complexRoots(nw, a);
                roots.addAll(nwr);
                if (roots.size() == a.degree(0)) {
                    work = false;
                    break;
                }
                cp = (Complex<C>[]) copyOfComplex(rect.corners, 4);
                cp[0] = new Complex<C>(cr, cp[0].getRe(), center.getIm());
                cp[2] = new Complex<C>(cr, center.getRe(), cp[2].getIm());
                cp[3] = center;
                Rectangle<C> sw = new Rectangle<C>(cp);
                List<Rectangle<C>> swr = complexRoots(sw, a);
                roots.addAll(swr);
                if (roots.size() == a.degree(0)) {
                    work = false;
                    break;
                }
                cp = (Complex<C>[]) copyOfComplex(rect.corners, 4);
                cp[0] = center;
                cp[1] = new Complex<C>(cr, center.getRe(), cp[1].getIm());
                cp[3] = new Complex<C>(cr, cp[3].getRe(), center.getIm());
                Rectangle<C> se = new Rectangle<C>(cp);
                List<Rectangle<C>> ser = complexRoots(se, a);
                roots.addAll(ser);
                if (roots.size() == a.degree(0)) {
                    work = false;
                    break;
                }
                cp = (Complex<C>[]) copyOfComplex(rect.corners, 4);
                cp[0] = new Complex<C>(cr, center.getRe(), cp[0].getIm());
                cp[1] = center;
                cp[2] = new Complex<C>(cr, cp[2].getRe(), center.getIm());
                Rectangle<C> ne = new Rectangle<C>(cp);
                List<Rectangle<C>> ner = complexRoots(ne, a);
                roots.addAll(ner);
                work = false;
            } catch (InvalidBoundaryException e) {
                delta = delta.sum(delta.multiply(eps));
                eps = eps.sum(eps.multiply(cr.getIMAG()));
            }
        }
        return roots;
    }

    /**
     * Invariant rectangle for algebraic number.
     * @param rect root isolating rectangle for f which contains exactly one root.
     * @param f univariate polynomial, non-zero.
     * @param g univariate polynomial, gcd(f,g) == 1.
     * @return v a new rectangle contained in rect such that g(w) != 0 for w in v.
     */
    @Override
    public Rectangle<C> invariantRectangle(Rectangle<C> rect, GenPolynomial<Complex<C>> f, GenPolynomial<Complex<C>> g) throws InvalidBoundaryException {
        Rectangle<C> v = rect;
        if (g == null || g.isZERO()) {
            return v;
        }
        if (g.isConstant()) {
            return v;
        }
        if (f == null || f.isZERO() || f.isConstant()) {
            return v;
        }
        BigRational len = v.rationalLength();
        BigRational half = new BigRational(1, 2);
        while (true) {
            long n = windingNumber(v, g);
            if (n < 0) {
                throw new RuntimeException("negative winding number " + n);
            }
            if (n == 0) {
                return v;
            }
            len = len.multiply(half);
            Rectangle<C> v1 = v;
            v = complexRootRefinement(v, f, len);
            if (v.equals(v1)) {
                if (!f.gcd(g).isONE()) {
                    System.out.println("f.gcd(g) = " + f.gcd(g));
                    throw new RuntimeException("no convergence " + v);
                }
            }
        }
    }
}
