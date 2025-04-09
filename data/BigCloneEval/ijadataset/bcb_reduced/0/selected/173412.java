package geomss.geom.nurbs;

import java.util.List;
import java.util.ResourceBundle;
import javolution.util.FastTable;
import javolution.context.ObjectFactory;
import javolution.context.ArrayFactory;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.XMLSerializable;
import javolution.text.Text;
import javolution.text.TextBuilder;
import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;

/**
*  <p>A collection of knot values that are associated with a NURBS curve.</p>
*
*  <pre>
*  References:
*	1.) Piegl, L., Tiller, W., The Nurbs Book, 2nd Edition, Springer-Verlag, Berlin, 1997.
*  </pre>
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author Samuel Gerber    Date:  May 14, 2009, Version 1.0.
*  @version January 30, 2012
**/
public class KnotVector implements Cloneable, XMLSerializable {

    /**
	*  The resource bundle for this package.
	**/
    private static final ResourceBundle RESOURCES = geomss.geom.AbstractGeomElement.RESOURCES;

    private boolean _open;

    private Float64Vector _knots;

    private int _degree;

    private int _nu;

    /**
	* Create a Knotvector from the given knot values of the desired degree.
	* 
	* @param degree  degree of NURBS curve
	* @param knots   knot values
	* @throws IllegalArgumentException if the knot vector is not valid.
	**/
    public static KnotVector newInstance(int degree, Float64Vector knots) {
        int numKnots = knots.getDimension();
        for (int i = 1; i < numKnots; i++) {
            if (knots.getValue(i - 1) > knots.getValue(i)) {
                throw new IllegalArgumentException(RESOURCES.getString("knotsNotIncreasingErr"));
            }
        }
        for (int i = 0; i < numKnots; ++i) {
            double kv = knots.getValue(i);
            if (kv > 1.0 || kv < 0.0) throw new IllegalArgumentException(RESOURCES.getString("invalidKnotValue").replace("<VAL/>", Double.toString(kv)));
        }
        KnotVector o = FACTORY.object();
        o._knots = knots;
        o._degree = degree;
        o._nu = knots.getDimension() - degree - 2;
        o._open = true;
        for (int k = 0; k < degree && o._open == true; k++) {
            if (knots.getValue(k) != knots.getValue(k + 1)) o._open = false;
        }
        int m = knots.getDimension() - 1;
        for (int k = m; k > m - degree && o._open == true; k--) {
            if (knots.getValue(k) != knots.getValue(k - 1)) o._open = false;
        }
        return o;
    }

    /**
	* Create a Knotvector from the given list of knot values of the desired degree.
	* 
	* @param degree  degree of NURBS curve
	* @param knots   A list of knot values
	* @throws IllegalArgumentException if the knot vector is not valid.
	**/
    public static KnotVector newInstance(int degree, List<Double> knots) throws IllegalArgumentException {
        FastTable<Float64> kvList = FastTable.newInstance();
        for (double value : knots) {
            kvList.add(Float64.valueOf(value));
        }
        Float64Vector kv = Float64Vector.valueOf(kvList);
        FastTable.recycle(kvList);
        return newInstance(degree, kv);
    }

    /**
	* Create a Knotvector from the given knot values of the desired degree.
	* 
	* @param degree  degree of NURBS curve
	* @param knots   knot values
	* @throws IllegalArgumentException if the knot vector is not valid.
	**/
    public static KnotVector newInstance(int degree, double... knots) throws IllegalArgumentException {
        return newInstance(degree, Float64Vector.valueOf(knots));
    }

    /**
	* Returns the span (position of corresponding knot values in knot vector) a
	* given parameter value belongs to.
	* 
	* @param s  parameter value to find the span for
	* @return Position of span.
	**/
    public int findSpan(double s) {
        if (s >= _knots.getValue(_nu + 1)) return _nu;
        int low = _degree;
        int high = _nu + 1;
        int mid = (low + high) / 2;
        while ((s < _knots.getValue(mid) || s >= _knots.getValue(mid + 1)) && low < high) {
            if (s < _knots.getValue(mid)) high = mid; else low = mid;
            mid = (low + high) / 2;
        }
        return mid;
    }

    /**
	* Returns the basis function values for the given parameter value (Nik(s)). This function
	* first calculates the span which is needed in order to calculate the
	* basis functions values.
	* 
	* @param s  Parameter value to calculate basis functions for.
	* @return basis function values.  WARNING: the returned array will likely be longer than [0..degree],
	*         so do NOT depend on array.length in any iterations over the array!.
	*         The additional array elements will contain garbage and should not be used.
	*         The returned array was allocated using javolution.context.ArrayFactory.DOUBLES_FACTORY and could
	*         be recycled by the user when no longer needed.
	**/
    public double[] basisFunctions(double s) {
        return basisFunctions(findSpan(s), s);
    }

    /**
	* Returns the unweighted basis function values for the given parameter value (Nik(s)), when the span that
	* the parameter value lies in is already known.
	* 
	* @param span  The span <code>s</code> lies in
	* @param s     The parameter value to calculate basis functions for.
	* @return basis function values.  WARNING: the returned array will likely be longer than [0..degree],
	*         so do NOT depend on array.length in any iterations over the array!.
	*         The additional array elements will contain garbage and should not be used.
	*         The returned array was allocated using javolution.context.ArrayFactory.DOUBLES_FACTORY and could
	*         be recycled by the user when no longer needed.
	**/
    public double[] basisFunctions(int span, double s) {
        int degree = _degree;
        int order = degree + 1;
        double res[] = ArrayFactory.DOUBLES_FACTORY.array(order);
        res[0] = 1;
        double left[] = ArrayFactory.DOUBLES_FACTORY.array(order);
        left[0] = 0;
        double right[] = ArrayFactory.DOUBLES_FACTORY.array(order);
        right[0] = 0;
        for (int j = 1; j < order; j++) {
            left[j] = s - _knots.getValue(span + 1 - j);
            right[j] = _knots.getValue(span + j) - s;
            double saved = 0;
            for (int r = 0; r < j; r++) {
                int jmr = j - r;
                int rp1 = r + 1;
                double tmp = res[r] / (right[rp1] + left[jmr]);
                res[r] = saved + right[rp1] * tmp;
                saved = left[jmr] * tmp;
            }
            res[j] = saved;
        }
        ArrayFactory.DOUBLES_FACTORY.recycle(left);
        ArrayFactory.DOUBLES_FACTORY.recycle(right);
        return res;
    }

    /**
	* Calculates all the derivatives of all the unweighted basis functions from <code>0</code> up to the
	* given grade, <code>d^{grade}Nik(s)/d^{grade}s</code>.
	* Examples:  1st derivative (grade = 1), this returns <code>[Nik(s), dNik(s)/ds]</code>;
	*			 2nd derivative (grade = 2), this returns <code>[Nik(s), dNik(s)/ds, d^2Nik(s)/d^2s]</code>; etc.<br>
	*
	* @param span  The span <code>s</code> lies in
	* @param s     The parameter value to calculate basis functions for.
	* @param grade The grade to calculate the derivatives for (1=1st derivative, 2=2nd derivative, etc).
	* @return Basis function derivative values.  WARNING: the returned array is recycled and will likely be longer
	*         than [0..grade+1][0..degree+1], so do NOT depend on array.length in any iterations over the array!.
	*         The additional array elements will contain garbage and should not be used.
	*         The returned array could be recycled by calling <code>KnotVector.recycle2DArray(arr)</code>
	*         when no longer needed.
	* @throws IllegalArgumentException if the grade is < 0.
	* @see #basisFunctions
	* @see #recycle2DArray
	**/
    public double[][] basisFunctionDerivatives(int span, double s, int grade) {
        if (grade < 0) throw new IllegalArgumentException(RESOURCES.getString("gradeLTZeroErr"));
        int degree = _degree;
        int order = degree + 1;
        int gradeP1 = grade + 1;
        double[][] nds = new2DZeroedArray(order, order);
        nds[0][0] = 1.0;
        double[] left = ArrayFactory.DOUBLES_FACTORY.array(order);
        left[0] = 0;
        double[] right = ArrayFactory.DOUBLES_FACTORY.array(order);
        right[0] = 0;
        for (int j = 1; j < order; j++) {
            left[j] = s - _knots.getValue(span + 1 - j);
            right[j] = _knots.getValue(span + j) - s;
            double saved = 0.0;
            for (int r = 0; r < j; r++) {
                int jmr = j - r;
                int rp1 = r + 1;
                nds[j][r] = right[rp1] + left[jmr];
                double temp = nds[r][j - 1] / nds[j][r];
                nds[r][j] = saved + right[rp1] * temp;
                saved = left[jmr] * temp;
            }
            nds[j][j] = saved;
        }
        ArrayFactory.DOUBLES_FACTORY.recycle(left);
        ArrayFactory.DOUBLES_FACTORY.recycle(right);
        left = right = null;
        double[][] ders = CurveUtils.allocate2DArray(gradeP1, order);
        for (int j = 0; j < order; j++) ders[0][j] = nds[j][degree];
        double[][] a = new2DZeroedArray(2, order);
        for (int r = 0; r < order; r++) {
            int s1 = 0, s2 = 1;
            a[0][0] = 1.0;
            for (int k = 1; k < gradeP1; k++) {
                double d = 0.0;
                int rk = r - k;
                int pk = degree - k;
                int pkp1 = pk + 1;
                if (r >= k) {
                    a[s2][0] = a[s1][0] / nds[pkp1][rk];
                    d = a[s2][0] * nds[rk][pk];
                }
                int j1, j2;
                if (rk >= -1) j1 = 1; else j1 = -rk;
                if (r - 1 <= pk) j2 = k - 1; else j2 = degree - r;
                for (int j = j1; j <= j2; j++) {
                    int rkpj = rk + j;
                    a[s2][j] = (a[s1][j] - a[s1][j - 1]) / nds[pkp1][rkpj];
                    d += a[s2][j] * nds[rkpj][pk];
                }
                if (r <= pk) {
                    a[s2][k] = -a[s1][k - 1] / nds[pkp1][r];
                    d += a[s2][k] * nds[r][pk];
                }
                ders[k][r] = d;
                j1 = s1;
                s1 = s2;
                s2 = j1;
            }
        }
        recycle2DArray(a);
        recycle2DArray(nds);
        int r = degree;
        for (int k = 1; k < gradeP1; k++) {
            for (int j = 0; j < order; j++) ders[k][j] *= r;
            r *= (degree - k);
        }
        return ders;
    }

    /**
	*  Allocate a 2D array using factory methods and fill it with zeros.
	**/
    private static double[][] new2DZeroedArray(int rows, int cols) {
        double[][] arr = CurveUtils.allocate2DArray(rows, cols);
        for (int i = 0; i < rows; ++i) for (int j = 0; j < cols; ++j) arr[i][j] = 0.;
        return arr;
    }

    /**
	* Return the length of the knot vector (nu).
	**/
    public int getNu() {
        return _nu;
    }

    /**
	* Return the number of elements in the knot vector.
	**/
    public int length() {
        return _knots.getDimension();
    }

    /**
	* Return the knot values as an vector of <code>Float64</code> values.
	* 
	* @return the vector of knot values
	**/
    public Float64Vector getAll() {
        return _knots;
    }

    /**
	*  Return the knot at the specified index.
	*
	* @param i Index to get knot value for
	* @return the knot value at index <code>i</code>
	**/
    public Float64 get(int i) {
        return _knots.get(i);
    }

    /**
	* Return the knot value at a specific index as a <code>double</code>.
	*
	* @param i Index to get knot value for
	* @return the knot value at index <code>i</code> returned as a <code>double</code>.
	**/
    public double getValue(int i) {
        return _knots.getValue(i);
    }

    /**
	* Return the degree of the KnotVector
	*
	* @return Degree of the Knotvector
	**/
    public int getDegree() {
        return _degree;
    }

    /**
	*  Return the number of segments in the knot vector.
	**/
    public int getNumberOfSegments() {
        int seg = 0;
        double u = _knots.getValue(0);
        int size = _knots.getDimension();
        for (int i = 1; i < size; i++) {
            double kv = _knots.getValue(i);
            if (u != kv) {
                seg++;
                u = kv;
            }
        }
        return seg;
    }

    /**
	*  Return <code>true</code> if the knot vector is open and
	*  <code>false</code> if it is closed.
	**/
    public boolean isOpen() {
        return _open;
    }

    /**
	*  Find the multiplicity of the knot with the specified index in this knot vector.
	*
	*  @param index  the index of the knot to observe (the largest index of a repeated series of knots).
	*  @return the multiplicity of the knot
	**/
    public int findMultiplicity(int index) {
        int s = 1;
        int order = getDegree() + 1;
        for (int i = index; i > order; --i) if (getValue(i) <= getValue(i - 1)) ++s; else return s;
        return s;
    }

    /**
	*  Return a copy of this knot vector with the parameterization reversed.
	**/
    public KnotVector reverse() {
        FastTable<Float64> values = FastTable.newInstance();
        for (int i = length() - 1; i >= 0; --i) values.add(Float64.ONE.minus(get(i)));
        KnotVector kv = KnotVector.newInstance(getDegree(), Float64Vector.valueOf(values));
        FastTable.recycle(values);
        return kv;
    }

    /**
	* Returns a copy of this KnotVector instance  
	* {@link javolution.context.AllocatorContext allocated} 
	* by the calling thread (possibly on the stack).
	*	
	* @return an identical and independant copy of this point.
	*/
    public KnotVector copy() {
        return copyOf(this);
    }

    /**
	* Returns a copy of this KnotVector instance  
	* {@link javolution.context.AllocatorContext allocated} 
	* by the calling thread (possibly on the stack).
	*	
	* @return an identical and independant copy of this point.
	**/
    public Object clone() {
        return copy();
    }

    /**
 	* Compares this ControlPoint against the specified object for strict 
 	* equality (same values and same units).
	*
 	* @param  obj the object to compare with.
 	* @return <code>true</code> if this point is identical to that
 	* 		point; <code>false</code> otherwise.
	**/
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;
        KnotVector that = (KnotVector) obj;
        if (this._degree != that._degree) return false;
        if (this._knots.equals(that._knots)) return false;
        return super.equals(obj);
    }

    /**
 	* Returns the hash code for this parameter.
 	* 
 	* @return the hash code value.
 	*/
    public int hashCode() {
        int hash = 7;
        int var_code = _degree;
        hash = hash * 31 + var_code;
        var_code = _knots.hashCode();
        hash = hash * 31 + var_code;
        hash = hash * 31 + super.hashCode();
        return hash;
    }

    /**
	* Returns the text representation of this knot vector that
	* consists of the degree followed by the knot values.
	* For example:<pre>
	*   {degree=2,{0.0, 0.0, 0.0, 1.0, 1.0, 1.0}}
	* </pre>
	*
	* @return the text representation of this geometry element.
	**/
    public Text toText() {
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append("{degree=");
        tmp.append(_degree);
        tmp.append(",");
        tmp.append(_knots.toText());
        tmp.append('}');
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp);
        return txt;
    }

    /**
	* Returns the string representation of this knot vector that
	* consists of the degree followed by the knot values.
	* For example:<pre>
	*   {degree=2,{0.0, 0.0, 0.0, 1.0, 1.0, 1.0}}
	* </pre>
	*
	* @return the text representation of this geometry element.
	**/
    public String toString() {
        return toText().toString();
    }

    /**
	*  Recycle any 2D array of doubles that was created by this classes factory
	*  methods.
	*
	*  @param arr The array to be recycled.  The array must have been created by this class
	*             or by CurveUtils.allocate2DArray()!
	**/
    public static void recycle2DArray(double[][] arr) {
        CurveUtils.recycle2DArray(arr);
    }

    /**
 	* Holds the default XML representation. For example:
 	* <pre>
 	*	&lt;KnotVector degree = "2.0"&gt;
 	*		&lt;GeomPoint unit = "m"&gt;
 	*			&lt;Float64 value="1.0" /&gt;
 	*			&lt;Float64 value="0.0" /&gt;
 	*			&lt;Float64 value="2.0" /&gt;
 	*		&lt;/GeomPoint&gt;
 	*	&lt;/KnotVector&gt;
	* </pre>
 	*/
    protected static final XMLFormat<KnotVector> XML = new XMLFormat<KnotVector>(KnotVector.class) {

        @Override
        public KnotVector newInstance(Class<KnotVector> cls, InputElement xml) throws XMLStreamException {
            int degree = xml.getAttribute("degree", 1);
            Float64Vector kv = xml.getNext();
            return KnotVector.newInstance(degree, kv);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void read(InputElement xml, KnotVector V) throws XMLStreamException {
        }

        @Override
        public void write(KnotVector V, OutputElement xml) throws XMLStreamException {
            xml.setAttribute("degree", V._degree);
            xml.add(V._knots);
        }
    };

    protected KnotVector() {
    }

    @SuppressWarnings("unchecked")
    private static final ObjectFactory<KnotVector> FACTORY = new ObjectFactory<KnotVector>() {

        @Override
        protected KnotVector create() {
            return new KnotVector();
        }

        protected void cleanup(KnotVector obj) {
        }
    };

    @SuppressWarnings("unchecked")
    private static KnotVector copyOf(KnotVector original) {
        KnotVector o = FACTORY.object();
        o._open = original._open;
        o._knots = original._knots.copy();
        o._degree = original._degree;
        o._nu = original._nu;
        return o;
    }
}
