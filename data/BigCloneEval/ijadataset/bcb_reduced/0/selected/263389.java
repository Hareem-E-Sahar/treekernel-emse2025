package geomss.geom.nurbs;

import java.util.List;
import java.util.ResourceBundle;
import static java.lang.Math.*;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Angle;
import javax.measure.unit.Unit;
import javax.measure.unit.SI;
import javolution.util.FastTable;
import javolution.context.ArrayFactory;
import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;
import jahuwaldt.tools.math.MathTools;
import jahuwaldt.js.param.Parameter;
import geomss.geom.DimensionException;
import geomss.geom.GeomList;
import geomss.geom.GeomVector;
import geomss.geom.Vector;
import geomss.geom.GeomPoint;
import geomss.geom.Point;
import geomss.geom.GeomUtil;
import geomss.geom.Curve;
import geomss.geom.IntersectType;

/**
*  A collection of utility methods for working with NURBS curves.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author Samuel Gerber    Date:  May 14, 2009, Version 1.0.
*  @version April 16, 2012
**/
public final class CurveUtils {

    /**
	*  The resource bundle for this package.
	**/
    private static final ResourceBundle RESOURCES = geomss.geom.AbstractGeomElement.RESOURCES;

    /**
	*  Elevate the degree of a curve to the specified degree.  If the
	*  specified degree is <= the current degree of the curve, then
	*  no change is made and a reference to the input curve is returned.
	*
	*  @param curve  The curve to have the degree elevated.
	*  @param degree The desired degree for the curve to be elevated to.
	*  @return The input NurbsCurve with the degree elevated to the specified degree.
	**/
    public static NurbsCurve elevateToDegree(NurbsCurve curve, int degree) {
        int p = curve.getDegree();
        if (degree <= p) return curve;
        int t = degree - p;
        return degreeElevate(curve, t);
    }

    /**
	*  Elevate the degree of a curve by the specified number of times.
	*
	*  @param curve  The curve to have the degree elevated.
	*  @param t      The number of times to elevate the degree.
	*  @return The input NurbsCurve with the degree elevated by the specified amount.
	**/
    public static NurbsCurve degreeElevate(NurbsCurve curve, int t) {
        if (t <= 0) return curve;
        int dim = curve.getPhyDimension();
        KnotVector U = curve.getKnotVector();
        List<ControlPoint> Pw = curve.getControlPoints();
        int n = Pw.size() - 1;
        int p = curve.getDegree();
        int m = n + p + 1;
        int ph = p + t;
        int ph2 = ph / 2;
        double[][] bezalfs = allocate2DArray(ph + 1, p + 1);
        ControlPoint[] bpts = CONTROLPOINTARRAY_FACTORY.array(p + 1);
        ControlPoint[] ebpts = CONTROLPOINTARRAY_FACTORY.array(ph + 1);
        ControlPoint[] Nextbpts = CONTROLPOINTARRAY_FACTORY.array(p - 1);
        double[] alphas = ArrayFactory.DOUBLES_FACTORY.array(p - 1);
        double[][] Bin = allocate2DArray(ph + 1, ph2 + 1);
        binomialCoef(Bin, ph + 1, ph2 + 1);
        bezalfs[0][0] = bezalfs[ph][p] = 1.0;
        for (int i = 1; i <= ph2; i++) {
            double inv = 1.0 / Bin[ph][i];
            int mpi = min(p, i);
            for (int j = max(0, i - t); j <= mpi; j++) bezalfs[i][j] = inv * Bin[p][j] * Bin[t][i - j];
        }
        for (int i = ph2 + 1; i < ph; i++) {
            int mpi = min(p, i);
            for (int j = max(0, i - t); j <= mpi; j++) bezalfs[i][j] = bezalfs[ph - i][p - j];
        }
        ControlPoint[] Qw = CONTROLPOINTARRAY_FACTORY.array(Pw.size() + Pw.size() * t);
        double[] Uh = ArrayFactory.DOUBLES_FACTORY.array(Pw.size() + Pw.size() * t + ph + 1);
        int mh = ph;
        int kind = ph + 1;
        double ua = U.getValue(0);
        double ub = 0;
        int r = -1;
        int oldr = r;
        int a = p;
        int b = p + 1;
        int cind = 1;
        int rbz, lbz = 1;
        Qw[0] = Pw.get(0).copy();
        for (int i = 0; i <= ph; i++) Uh[i] = ua;
        for (int i = 0; i <= p; i++) bpts[i] = Pw.get(i).copy();
        while (b < m) {
            int i = b;
            while (b < m && abs(U.getValue(b) - U.getValue(b + 1)) < MathTools.EPS) ++b;
            int mul = b - i + 1;
            mh += mul + t;
            ub = U.getValue(b);
            oldr = r;
            r = p - mul;
            if (oldr > 0) lbz = (oldr + 2) / 2; else lbz = 1;
            if (r > 0) rbz = ph - (r + 1) / 2; else rbz = ph;
            if (r > 0) {
                double numer = ub - ua;
                for (int k = p; k > mul; k--) alphas[k - mul - 1] = numer / (U.getValue(a + k) - ua);
                for (int j = 1; j <= r; j++) {
                    int save = r - j;
                    int s = mul + j;
                    for (int k = p; k >= s; k--) {
                        bpts[k] = applyAlpha(bpts[k], bpts[k - 1], alphas[k - s]);
                    }
                    Nextbpts[save] = bpts[p];
                }
            }
            for (i = lbz; i <= ph; i++) {
                ebpts[i] = ControlPoint.newInstance(dim);
                int mpi = min(p, i);
                for (int j = max(0, i - t); j <= mpi; j++) {
                    ControlPoint q1 = bpts[j].applyWeight();
                    q1.times(bezalfs[i][j]);
                    ControlPoint q2 = ebpts[i].applyWeight();
                    q2.plus(q1);
                    ebpts[i] = q2.getHomogeneous();
                    ControlPoint.recycle(q1);
                    ControlPoint.recycle(q2);
                }
            }
            if (oldr > 1) {
                int first = kind - 2;
                int last = kind;
                double den = ub - ua;
                double bet = (ub - Uh[kind - 1]) / den;
                for (int tr = 1; tr < oldr; tr++) {
                    i = first;
                    int j = last;
                    int kj = j - kind + 1;
                    while (j - i > tr) {
                        if (i < cind) {
                            double alf = (ub - Uh[i]) / (ua - Uh[i]);
                            Qw[i] = applyAlpha(Qw[i], Qw[i - 1], alf);
                        }
                        if (j >= lbz) {
                            if (j - tr <= kind - ph + oldr) {
                                double gam = (ub - Uh[j - tr]) / den;
                                ebpts[kj] = applyAlpha(ebpts[kj], ebpts[kj + 1], gam);
                            } else {
                                ebpts[kj] = applyAlpha(ebpts[kj], ebpts[kj + 1], bet);
                            }
                        }
                        ++i;
                        --j;
                        --kj;
                    }
                    --first;
                    ++last;
                }
            }
            if (a != p) for (i = 0; i < ph - oldr; i++) {
                Uh[kind++] = ua;
            }
            for (int j = lbz; j <= rbz; j++) {
                Qw[cind++] = ebpts[j];
            }
            if (b < m) {
                for (int j = 0; j < r; j++) bpts[j] = Nextbpts[j];
                for (int j = r; j <= p; j++) bpts[j] = Pw.get(b - p + j).copy();
                a = b;
                ++b;
                ua = ub;
            } else {
                for (i = 0; i <= ph; i++) Uh[kind + i] = ub;
            }
        }
        FastTable<ControlPoint> newP = FastTable.newInstance();
        n = mh - ph;
        for (int i = 0; i < n; ++i) newP.add(Qw[i]);
        FastTable<Float64> newKVList = FastTable.newInstance();
        m = n + ph + 1;
        for (int i = 0; i < m; ++i) newKVList.add(Float64.valueOf(Uh[i]));
        KnotVector newKV = KnotVector.newInstance(ph, Float64Vector.valueOf(newKVList));
        BasicNurbsCurve newCrv = BasicNurbsCurve.newInstance(newP, newKV);
        recycle2DArray(bezalfs);
        recycle2DArray(Bin);
        ArrayFactory.DOUBLES_FACTORY.recycle(alphas);
        ArrayFactory.DOUBLES_FACTORY.recycle(Uh);
        CONTROLPOINTARRAY_FACTORY.recycle(bpts);
        CONTROLPOINTARRAY_FACTORY.recycle(ebpts);
        CONTROLPOINTARRAY_FACTORY.recycle(Nextbpts);
        CONTROLPOINTARRAY_FACTORY.recycle(Qw);
        FastTable.recycle((FastTable) Pw);
        FastTable.recycle(newP);
        FastTable.recycle(newKVList);
        return newCrv;
    }

    /**
	*  Decompose a NURBS curve into a list of Bezier segments.
	*  The returned list contains NURBS curves each of which represents
	*  a single Bezier segment.
	*
	*  @param curve The curve to be decomposed into Bezier segments.
	*  @return A list of Bezier curve segments (will be at least degree = 2).
	**/
    public static GeomList<BasicNurbsCurve> decomposeToBezier(NurbsCurve curve) {
        if (curve.getDegree() == 1) curve = degreeElevate(curve, 1);
        List<ControlPoint> Pw = curve.getControlPoints();
        KnotVector SP = curve.getKnotVector();
        int numKnots = SP.length();
        int p = SP.getDegree();
        FastTable<FastTable<ControlPoint>> Qw = FastTable.newInstance();
        FastTable<ControlPoint> Qwnb = FastTable.newInstance();
        Qw.add(Qwnb);
        for (int i = 0; i <= p; ++i) Qwnb.add(Pw.get(i).copy());
        int m = numKnots - 1;
        int a = p;
        int b = p + 1;
        int nb = 0;
        while (b < m) {
            int i = b;
            while (b < m && abs(SP.getValue(b) - SP.getValue(b + 1)) < MathTools.EPS) ++b;
            int mult = b - i + 1;
            if (mult < p) {
                double numer = SP.getValue(b) - SP.getValue(a);
                double[] alphas = ArrayFactory.DOUBLES_FACTORY.array(p - mult);
                for (int j = p; j > mult; --j) alphas[j - mult - 1] = numer / (SP.getValue(a + j) - SP.getValue(a));
                int r = p - mult;
                for (int j = 1; j <= r; ++j) {
                    int save = r - j;
                    int si = mult + j;
                    for (int k = p; k >= si; --k) {
                        double alpha = alphas[k - si];
                        Qwnb.set(k, applyAlpha(Qwnb.get(k), Qwnb.get(k - 1), alpha));
                    }
                    if (b < numKnots) {
                        if (Qw.size() <= nb + 1) {
                            FastTable<ControlPoint> tmp = FastTable.newInstance();
                            Qw.add(tmp);
                        }
                        FastTable<ControlPoint> Qwnbp1 = Qw.get(nb + 1);
                        if (Qwnbp1.size() <= save) Qwnbp1.setSize(save + 1);
                        Qwnbp1.set(save, Qwnb.get(p).copy());
                    }
                }
                ArrayFactory.DOUBLES_FACTORY.recycle(alphas);
                ++nb;
                if (Qw.size() <= nb) {
                    FastTable tmp = FastTable.newInstance();
                    tmp.setSize(p + 1);
                    Qw.add(tmp);
                }
                Qwnb = Qw.get(nb);
                if (b < numKnots) {
                    if (Qwnb.size() <= p) Qwnb.setSize(p + 1);
                    for (i = p - mult; i <= p; ++i) Qwnb.set(i, Pw.get(b - p + i).copy());
                    a = b;
                    ++b;
                }
            }
        }
        FastTable.recycle((FastTable) Pw);
        KnotVector bezierKV = bezierKnotVector(p);
        GeomList<BasicNurbsCurve> segments = GeomList.newInstance();
        for (FastTable<ControlPoint> cpLst : Qw) {
            BasicNurbsCurve crv = BasicNurbsCurve.newInstance(cpLst, bezierKV);
            segments.add(crv);
            FastTable.recycle(cpLst);
        }
        FastTable.recycle(Qw);
        return segments;
    }

    /**
	 *  Returns a list containing the parameters at the start (and end) of each
	 *  Bezier segment of the specified NURBS curve.  This first element of this list will
	 *  always be zero and the last will always be 1.
	 *
	 *  @param crv  The NURBS curve to return the Bezier segment start parameters for.
	 **/
    public static FastTable<Double> getBezierStartParameters(NurbsCurve crv) {
        KnotVector kv = crv.getKnotVector();
        int degree = kv.getDegree();
        FastTable<Double> knots = FastTable.newInstance();
        int size = kv.length() - degree;
        double oldS = -1;
        for (int i = degree; i < size; ++i) {
            double s = kv.getValue(i);
            if (s != oldS) {
                knots.add(s);
                oldS = s;
            }
        }
        return knots;
    }

    /**
	*  Return a knot vector that can be used to create a Bezier curve segment
	*  of the specified degree using the BasicNurbsCurve class.
	*
	*  @param degree  The degree of the knot vector to return.
	**/
    public static KnotVector bezierKnotVector(int degree) {
        FastTable<Float64> floatLst = FastTable.newInstance();
        for (int i = 0; i <= degree; ++i) floatLst.add(Float64.ZERO);
        for (int i = 0; i <= degree; ++i) floatLst.add(Float64.ONE);
        KnotVector kvBezier = KnotVector.newInstance(degree, Float64Vector.valueOf(floatLst));
        FastTable.recycle(floatLst);
        return kvBezier;
    }

    /**
	*  Method that applies the specified factor to the two specified breakpoints
	*  in the form:  P = alpha*P1 + (1-alpha)*P2.
	**/
    private static ControlPoint applyAlpha(ControlPoint P1, ControlPoint P2, double alpha) {
        ControlPoint q1 = P1.copy();
        q1.times(alpha);
        ControlPoint q2 = P2.copy();
        q2.times(1.0 - alpha);
        q1.plus(q2);
        ControlPoint.recycle(q2);
        return q1;
    }

    /**
	*  Connect together or "concatenate" a list of curves end-to-start.
	*  The end of each curve (u=1) is connected to the start (u=0) of the
	*  next curve.  No check is made for the curve ends being near each other.
	*  The resulting curve will have the physical dimenion of the highest dimension
	*  curve in the input list.  The resulting curve will have the degree of
	*  the highest degree curve in the input list.
	*
	*  @param curves  The list of curves to be connected together.
	*  @return A single {@link BasicNurbsCurve} made up of the input curves connected
	*          together end-to-start.
	*  @throws IllegalArgumentException if there are less than 2 curves.
	**/
    public static BasicNurbsCurve connectCurves(List<NurbsCurve> curves) throws IllegalArgumentException {
        int numCurves = curves.size();
        if (numCurves < 2) throw new IllegalArgumentException(RESOURCES.getString("reqTwoCurves"));
        int degree = 0;
        int dim = 0;
        for (NurbsCurve crv : curves) {
            if (crv.getDegree() > degree) degree = crv.getDegree();
            if (crv.getPhyDimension() > dim) dim = crv.getPhyDimension();
        }
        FastTable<Float64> knots = FastTable.newInstance();
        for (int i = 0; i <= degree; i++) knots.add(Float64.ZERO);
        FastTable<ControlPoint> cps = FastTable.newInstance();
        boolean firstPass = true;
        double w1 = 1;
        for (NurbsCurve crv : curves) {
            crv = crv.toDimension(dim);
            crv = elevateToDegree(crv, degree);
            KnotVector u = crv.getKnotVector();
            int uLength = u.length();
            double start = knots.getLast().doubleValue() - u.getValue(0);
            int end = uLength - degree - 1;
            for (int j = degree + 1; j < end; j++) knots.addLast(Float64.valueOf(start + u.getValue(j)));
            for (int j = 0; j < degree; j++) knots.addLast(Float64.valueOf(start + u.getValue(uLength - 1)));
            List<ControlPoint> pts = crv.getControlPoints();
            double wfactor = 1;
            if (firstPass) {
                cps.addLast(pts.get(0).copy());
                firstPass = false;
            } else {
                double w2 = pts.get(0).getWeight();
                wfactor = w1 / w2;
            }
            int numCP = pts.size();
            for (int j = 1; j < numCP; ++j) {
                ControlPoint pt = pts.get(j).copy();
                double w2 = pt.getWeight();
                pt.setWeight(w2 * wfactor);
                cps.addLast(pt);
            }
            FastTable.recycle((FastTable) pts);
            w1 = cps.getLast().getWeight();
        }
        knots.addLast(knots.getLast());
        Float64Vector knotsV = Float64Vector.valueOf(knots);
        knotsV = knotsV.times(1. / knots.getLast().doubleValue());
        KnotVector kv = KnotVector.newInstance(degree, knotsV);
        FastTable.recycle(knots);
        return BasicNurbsCurve.newInstance(cps, kv);
    }

    /**
	*  Connect together or "concatenate" a list of curves end-to-start.
	*  The end of each curve (u=1) is connected to the start (u=0) of the
	*  next curve.  No check is made for the curve ends being near each other.
	*  The resulting curve will have the physical dimenion of the highest dimension
	*  curve in the input list.  The resulting curve will have the degree of
	*  the highest degree curve in the input list.
	*
	*  @param curves  The list of curves to be connected together.
	*  @return A single {@link BasicNurbsCurve} made up of the input curves connected
	*          together end-to-start.
	*  @throws IllegalArgumentException if there are less than 2 curves.
	**/
    public static BasicNurbsCurve connectCurves(NurbsCurve... curves) throws IllegalArgumentException {
        FastTable<NurbsCurve> list = FastTable.newInstance();
        for (NurbsCurve crv : curves) list.add(crv);
        BasicNurbsCurve crv = connectCurves(list);
        FastTable.recycle(list);
        return crv;
    }

    private static final Parameter<Angle> ANGULAR_TOLERANCE = Parameter.valueOf(0.01, SI.RADIAN);

    /**
	*  Return a fillet curve between two curves. Parameter values indicate
	*  the points between which the fillet is to be produced.  The output curve
	*  will have the physical dimension of the highest dimension of the two
	*  input curves.
	*
	*  @param crv1   The 1st input curve.
	*  @param crv2   The 2nd input curve.
	*  @param eps   The geometry tolerance to use.
	*  @param sEnd1 Parameter value on the first curve telling that
	*               the part of the curve lying on this side of sFil1
	*               shall not be replaced by the fillet.
	*  @param sFil1 Parameter value of the starting point of the fillet
	*               on the first curve.
	*  @param sEnd2 Parameter value on the second curve telling that the
	*               part of the curve lying on this side of sFil2 shall
	*               not be replaced by the fillet.
	*  @param sFil2 Parameter value of the starting point of the fillet on
	*               the second curve.
	*  @param itype Indicator of type of fillet.
	*                     = 1  - Circle, interpolating point and tangent on first curve
	*                            and the point on the 2nd curve, but not
	*                            the tangent on curve 2.
	*                     = 2  - Planar conic if possible
	*                     else - A generic polynomial segment.
	*  @param degree Degree of fillet curve to return.
	*  @return A {@link BasicNurbsCurve} fillet curve.
	**/
    public static BasicNurbsCurve createFillet(Curve crv1, Curve crv2, Parameter<Length> eps, double sEnd1, double sFil1, double sEnd2, double sFil2, int itype, int degree) {
        int dim = crv1.getPhyDimension();
        if (crv2.getPhyDimension() > dim) dim = crv2.getPhyDimension();
        crv1 = crv1.toDimension(dim);
        crv2 = crv2.toDimension(dim);
        List<GeomVector<Length>> ders = crv1.getSDerivatives(sFil1, 1);
        GeomVector<Length> p1v = ders.get(0);
        GeomPoint p1 = p1v.getOrigin().copyToReal();
        GeomVector<Length> pu1 = ders.get(1);
        FastTable.recycle((FastTable) ders);
        if (sEnd1 >= sFil1) {
            pu1 = pu1.opposite();
        }
        ders = crv2.getSDerivatives(sFil2, 1);
        GeomVector<Length> p2v = ders.get(0);
        GeomPoint p2 = p2v.getOrigin().copyToReal();
        GeomVector<Length> pu2 = ders.get(1);
        FastTable.recycle((FastTable) ders);
        if (sEnd2 < sFil2) {
            pu2 = pu2.opposite();
        }
        Parameter<Length> dend = p1.distance(p2);
        if (dend.isApproxZero()) return CurveFactory.createPoint(degree, p1);
        GeomPoint ZERO_POINT = Point.newInstance(dim);
        GeomVector sdum = null;
        boolean plane = true;
        if (dim > 2) {
            sdum = pu1.cross(pu2).toUnitVector();
            GeomVector p1p2 = p1.minus(p2).toGeomVector();
            Parameter proj = p1p2.dot(sdum);
            if (proj.isLargerThan(eps)) plane = false;
        }
        if (plane && itype != 1) {
            Parameter dum = pu1.dot(pu2);
            Parameter sum1 = pu1.mag();
            Parameter sum2 = pu2.mag();
            Parameter<Angle> sang = null;
            if (sum1.isApproxZero() || sum2.isApproxZero()) sang = Parameter.ZERO_ANGLE; else {
                Parameter tcos = dum.divide(sum1.times(sum2));
                tcos = tcos.min(Parameter.ONE);
                sang = Parameter.acos(tcos);
            }
            if (Parameter.PI_ANGLE.minus(sang).isLessThan(ANGULAR_TOLERANCE)) {
                GeomVector norder1 = p2.minus(p1).toGeomVector();
                norder1.setOrigin(ZERO_POINT);
                if (norder1.angle(pu1).minus(Parameter.HALFPI_ANGLE).abs().isLessThan(ANGULAR_TOLERANCE)) itype = 1;
            } else if (sang.isLargerThan(ANGULAR_TOLERANCE)) {
                GeomVector norder1, norder2;
                if (dim == 2) {
                    norder1 = Vector.valueOf(pu1.get(1), pu1.get(0).opposite()).toUnitVector();
                    norder2 = Vector.valueOf(pu2.get(1), pu2.get(0).opposite()).toUnitVector();
                } else {
                    norder1 = sdum.cross(pu1);
                    norder2 = sdum.cross(pu2);
                }
                norder1 = norder1.minus(norder2);
                norder2 = p2.minus(p1).toGeomVector();
                norder2.setOrigin(ZERO_POINT);
                if (norder1.angle(norder2).isLessThan(ANGULAR_TOLERANCE)) itype = 1;
            }
        }
        BasicNurbsCurve crv;
        if (plane && (itype == 2 || itype == 1)) {
            if (itype == 1) crv = CurveFactory.createCircularArc(p1, pu1, p2); else crv = CurveFactory.createParabolicArc(p1, pu1.toUnitVector(), p2, pu2.toUnitVector());
        } else {
            crv = CurveFactory.createBlend(p1, pu1.toUnitVector(), p2, pu2.toUnitVector(), true, 0.5);
        }
        BasicNurbsCurve output = (BasicNurbsCurve) elevateToDegree(crv, degree);
        return output;
    }

    /**
	 *  Return a circular fillet curve between two planar non-parallel lines. The output curve
	 *  will have the physical dimension of the highest dimension of the two
	 *  input lines.  The corner where the fillet is applied depends on the directions
	 *  of the input lines.  It is the corner that is to the left of each input line when
	 *  standing at the start of the line and looking toward the end of the line.
	 *
	 *  @param p1   The 1st end of the 1st input line.
	 *  @param p2   The 2nd end of the 1st input line.
	 *  @param p3   The 1st end of the 2nd input line.
	 *  @param p4   The 2nd end of the 2nd input line.
	 *  @param radius The radius of the ciruclar fillet.
	 *  @param eps   The geometry tolerance to use.
	 *  @return A {@link BasicNurbsCurve} circular arc fillet curve.
	 **/
    public static BasicNurbsCurve createFillet(GeomPoint p1, GeomPoint p2, GeomPoint p3, GeomPoint p4, Parameter<Length> radius, Parameter<Length> eps) {
        int dim = p1.getPhyDimension();
        if (p2.getPhyDimension() > dim) dim = p2.getPhyDimension();
        if (p3.getPhyDimension() > dim) dim = p3.getPhyDimension();
        if (p4.getPhyDimension() > dim) dim = p4.getPhyDimension();
        if (dim < 2) throw new DimensionException(RESOURCES.getString("dimensionNotAtLeast2").replace("<TYPE/>", "input points").replace("<DIM/>", String.valueOf(dim)));
        int numDims = dim;
        if (dim < 3) dim = 3;
        p1 = p1.toDimension(dim);
        p2 = p2.toDimension(dim);
        p3 = p3.toDimension(dim);
        p4 = p4.toDimension(dim);
        if (p1.distance(p2).isLessThan(eps) || p3.distance(p4).isLessThan(eps)) {
            throw new IllegalArgumentException(RESOURCES.getString("degLineInput"));
        }
        GeomVector<Length> t1 = p2.toGeomVector().minus(p1.toGeomVector());
        GeomVector<Length> t2 = p4.toGeomVector().minus(p3.toGeomVector());
        GeomPoint ZERO_POINT = Point.newInstance(dim);
        GeomVector<Dimensionless> nhat = t1.cross(t2).toUnitVector();
        nhat.setOrigin(ZERO_POINT);
        GeomVector p1p3 = p1.minus(p3).toGeomVector();
        if (p1p3.mag().isApproxZero()) p1p3 = p1.minus(p4).toGeomVector();
        Parameter proj = p1p3.dot(nhat);
        if (proj.isLargerThan(eps)) throw new IllegalArgumentException(RESOURCES.getString("nonCoPlanarLines"));
        Parameter a = t1.dot(t1);
        Parameter b = t1.dot(t2);
        Parameter c = t2.dot(t2);
        Parameter denom = a.times(c).minus(b.times(b));
        if (denom.isApproxZero()) throw new IllegalArgumentException(RESOURCES.getString("parallelLinesInput"));
        GeomVector<Dimensionless> yhat = nhat.cross(t1).toUnitVector();
        yhat.setOrigin(ZERO_POINT);
        GeomPoint offset = Point.valueOf(yhat.times(radius));
        GeomPoint p1p = p1.plus(offset);
        yhat = nhat.cross(t2).toUnitVector();
        yhat.setOrigin(ZERO_POINT);
        offset = Point.valueOf(yhat.times(radius));
        GeomPoint p3p = p3.plus(offset);
        Point pc = Point.newInstance(dim).to(p1.getUnit());
        GeomUtil.lineLineIntersect(p1p, t1, p3p, t2, null, pc, null);
        GeomPoint pa = GeomUtil.pointLineClosest(pc, p1, t1.toUnitVector());
        GeomPoint pb = GeomUtil.pointLineClosest(pc, p3, t2.toUnitVector());
        BasicNurbsCurve crv = CurveFactory.createCircularArcO(pc, pa, pb);
        return crv.toDimension(numDims);
    }

    /**
	*  Return a blending curve between two curve ends. Parameter values indicate
	*  which ends of the input curves the blend curve is to touch.  The output curve
	*  will be a conic section if possible otherwise it will be a polynomial curve and will
	*  have the physical dimension of the highest dimension of the two input curves.
	*
	*  @param crv1   The 1st input curve.
	*  @param crv2   The 2nd input curve.
	*  @param eps   The geometry tolerance to use.
	*  @param sEnd1 Parameter value on crv1 indicating which end of curve 1 should start the blend.
	*  @param sEnd2 Parameter value on crv2 indicating which end of curve 2 should end the blend.
	*  @param degree Degree of blend curve to return.
	*  @return A {@link BasicNurbsCurve} blending curve between the input curves.
	**/
    public static BasicNurbsCurve createBlend(Curve crv1, Curve crv2, Parameter<Length> eps, double sEnd1, double sEnd2, int degree) {
        double fil1 = 1, end1 = 0;
        if (sEnd1 < 0.5) {
            fil1 = 0;
            end1 = 1;
        }
        double fil2 = 1, end2 = 0;
        if (sEnd2 < 0.5) {
            fil2 = 0;
            end2 = 1;
        }
        return createFillet(crv1, crv2, eps, end1, fil1, end2, fil2, 2, degree);
    }

    /**
	*  Set up a matrix containing all the binomial coefficients up to
	*  the specified row (n) and column (k) values.
	*
	*  <pre>
	*    bin(i,j) = (i  j)
	*    The binomical coefficients are defined as follow:
	*       (n)         n!
	*		( )  =  ------------
	*       (k)       k!(n-k)!       0<=k<=n
	*    and the following relationship applies:
	*    (n+1)     (n)   ( n )
	*    ( k ) =   (k) + (k-1)
	*  </pre>
	*
	*  @param bin  An existing matrix that will be filled in with the binomial coefficients.
	*  @param rows The number of rows (1st index) of the matrix to compute (n).
	*  @param cols The number of columns (2nd index) of the matrix to compute (k).
	**/
    private static void binomialCoef(double[][] bin, int rows, int cols) {
        for (int i = 0; i < rows; ++i) for (int j = 0; j < cols; ++j) bin[i][j] = 0;
        bin[0][0] = 1.0;
        for (int n = 0; n < rows - 1; n++) {
            int np1 = n + 1;
            bin[np1][0] = 1.0;
            for (int k = 1; k < cols; k++) {
                if (np1 >= k) bin[np1][k] = bin[n][k] + bin[n][k - 1];
            }
        }
    }

    /**
	*  Return true if the supplied control point polygon is simple and convex.
	*
	*  @param P  The list of control points to evaluate.
	**/
    public static boolean isSimpleConvexPolygon(List<ControlPoint> P) {
        int n = P.size() - 1;
        GeomPoint P0 = P.get(0).getPoint();
        GeomPoint Pn = P.get(n).getPoint();
        GeomPoint Pim1 = P.get(0).getPoint();
        GeomPoint Pi = P.get(1).getPoint();
        for (int i = 1; i < n; ++i) {
            GeomPoint Pip1 = P.get(i + 1).getPoint();
            GeomPoint V1 = GeomUtil.pointLineClosest(Pi, Pim1, Pip1.minus(Pim1).toGeomVector().toUnitVector());
            GeomVector<Length> V1Pi = Pi.minus(V1).toGeomVector();
            Parameter R;
            int no2 = n / 2;
            if (i < no2) {
                GeomVector<Length> V1Pn = Pn.minus(V1).toGeomVector();
                R = V1Pi.dot(V1Pn);
            } else {
                GeomVector<Length> V1P0 = P0.minus(V1).toGeomVector();
                R = V1Pi.dot(V1P0);
            }
            if (R.getValue() < -1e-14) return false;
            Pim1 = Pi;
            Pi = Pip1;
        }
        return true;
    }

    /**
	*  Returns <code>true</code> if an end point is the closest point in the specified
	*  Bezier curve segment to the target point.  Returns <code>false</code> if the closest point
	*  may be interior to the curve segment.
	*
	*  @param point The point to find the closest/farhtest point on this curve to/from.
	*  @param P     The control point polygon for the Bezier curve.
	*  @return <code>true</code> if an end point of the specified segment is closest to the target point.
	**/
    public static boolean isBezierEndPointNearest(GeomPoint point, List<ControlPoint> P) {
        GeomPoint P0 = P.get(0).getPoint();
        GeomPoint P1 = P.get(1).getPoint();
        int n = P.size() - 1;
        GeomPoint Pnm1 = P.get(n - 1).getPoint();
        GeomPoint Pn = P.get(n).getPoint();
        GeomVector<Length> P0P = point.minus(P0).toGeomVector();
        GeomVector<Length> P0P1 = P1.minus(P0).toGeomVector();
        GeomVector<Length> PPn = Pn.minus(point).toGeomVector();
        GeomVector<Length> Pnm1Pn = Pn.minus(Pnm1).toGeomVector();
        GeomVector<Length> PnP0 = P0.minus(Pn).toGeomVector();
        GeomVector<Length> PnP = PPn.opposite();
        Parameter R1 = P0P.dot(P0P1);
        Parameter R2 = PPn.dot(Pnm1Pn);
        Parameter R3 = PnP0.dot(PnP);
        Parameter R4 = PnP0.dot(P0P);
        if (R1.getValue() < 0 || R2.getValue() < 0 && R3.times(R4).getValue() > 0) return true;
        return false;
    }

    /**
	 *  Return the union between two knot vectors.  This merges
	 *  two knot vectors together to obtain on combined knot vector.
	 *  Warning:  The results of this method are useless if the
	 *  knot vectors being merged are not from NURBS curves of the same degree.
	 *
	 *  @param Ua The 1st knot vector to merge.
	 *  @param Ub The 2nd knot vector to merge.
	 *  @return The union of Ua and Ub.
	 **/
    public static Float64Vector knotVectorUnion(Float64Vector Ua, Float64Vector Ub) {
        int UaSize = Ua.getDimension();
        int UbSize = Ub.getDimension();
        FastTable<Float64> U = FastTable.newInstance();
        U.setSize(UaSize + UbSize);
        boolean done = false;
        int i = 0, ia = 0, ib = 0;
        double t = 0;
        while (!done) {
            double Uav = Ua.getValue(ia);
            double Ubv = Ub.getValue(ib);
            if (Uav == Ubv) {
                t = Uav;
                ++ia;
                ++ib;
            } else if (Uav < Ubv) {
                t = Uav;
                ++ia;
            } else {
                t = Ubv;
                ++ib;
            }
            U.set(i++, Float64.valueOf(t));
            done = (ia >= UaSize || ib >= UbSize);
        }
        U.setSize(i);
        Float64Vector Uv = Float64Vector.valueOf(U);
        FastTable.recycle(U);
        return Uv;
    }

    /**
	*  Allocate a recyclable 2D array using factory methods.
	*  WARNING: The array returned may not be zeroed and may be larger than
	*  the requested number of rows & columns!  The array returned
	*  by this method can be recycled by recycle2DArray().
	*
	*  @param rows The minimum number of rows (1st index) for the returned array.
	*  @param cols The minimum number of columns (2nd index) for the returned array.
	*  @see #recycle2DArray
	**/
    public static double[][] allocate2DArray(int rows, int cols) {
        double[][] arr = DOUBLEARRAY_FACTORY.array(rows);
        for (int i = 0; i < rows; ++i) arr[i] = ArrayFactory.DOUBLES_FACTORY.array(cols);
        return arr;
    }

    /**
	*  Recycle any 2D array of doubles that was created by this classes factory
	*  methods.
	*
	*  @param arr   The array to be recycled.  The array must have been created by this class's
	*				allocate2DArray() method!
	*  @see #allocate2DArray
	**/
    public static void recycle2DArray(double[][] arr) {
        int length = arr.length;
        for (int i = 0; i < length; ++i) {
            if (arr[i] != null) {
                ArrayFactory.DOUBLES_FACTORY.recycle(arr[i]);
                arr[i] = null;
            }
        }
        DOUBLEARRAY_FACTORY.recycle(arr);
    }

    /**
	*  Factory that creates recyclable arrays that can contain ControlPoint objects.
	**/
    private static ArrayFactory<ControlPoint[]> CONTROLPOINTARRAY_FACTORY = new ArrayFactory<ControlPoint[]>() {

        protected ControlPoint[] create(int size) {
            return new ControlPoint[size];
        }
    };

    /**
	*  Factory that creates recyclable arrays of doubles.
	**/
    private static ArrayFactory<double[][]> DOUBLEARRAY_FACTORY = new ArrayFactory<double[][]>() {

        protected double[][] create(int size) {
            return new double[size][];
        }
    };
}
