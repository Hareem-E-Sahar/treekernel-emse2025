package util;

/**
 * 1) All operations will be done over given inputs. 2) All inputs should be
 * appropriate! Checks should be done outsidely..
 * 
 */
public class DoubleArrayMath {

    protected static DoubleArrayMath singleton;

    private DoubleArrayMath() {
    }

    public static DoubleArrayMath newDoubleArrayMath() {
        if (singleton == null) {
            singleton = new DoubleArrayMath();
        }
        return singleton;
    }

    public double[] duplicate(double[] i) {
        double[] j = new double[i.length];
        System.arraycopy(i, 0, j, 0, i.length);
        return j;
    }

    public void add(double[] l, double j) {
        for (int k = 0; k < l.length; k++) {
            l[k] += j;
        }
    }

    public void sub(double[] i, double j) {
        add(i, -j);
    }

    public void mult(double[] l, double j) {
        for (int k = 0; k < l.length; k++) {
            l[k] *= j;
        }
    }

    public void div(double[] i, double j) {
        mult(i, 1 / j);
    }

    public void add(double[] m, double[] j) {
        for (int k = 0; k < m.length; k++) {
            m[k] += j[k];
        }
    }

    public void sub(double[] i, double[] j) {
        int length = i.length;
        for (int k = 0; k < length; k++) {
            i[k] -= j[k];
        }
    }

    public void mult(double[] m, double[] j) {
        for (int k = 0; k < m.length; k++) {
            m[k] *= j[k];
        }
    }

    public void div(double[] m, double[] j) {
        for (int k = 0; k < m.length; k++) {
            m[k] /= j[k];
        }
    }

    public void log(double[] m) {
        for (int k = 0; k < m.length; k++) {
            m[k] = Math.log(m[k]);
        }
    }

    public void exp(double[] m) {
        for (int k = 0; k < m.length; k++) {
            m[k] = Math.exp(m[k]);
        }
    }

    public void pow(double[] m, double j) {
        for (int k = 0; k < m.length; k++) m[k] = Math.pow(m[k], j);
    }

    public String toString(double[] i) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < i.length - 1; j++) sb.append((i[j] + " , "));
        if (i.length > 0) sb.append(i[i.length - 1]);
        return sb.toString();
    }

    public double absSumValues(double[] diff) {
        double result = 0f;
        for (double aDiff : diff) {
            result += Math.abs(aDiff);
        }
        return result;
    }

    public double sumValues(double[] diff) {
        double result = 0f;
        for (double aDiff : diff) {
            result += aDiff;
        }
        return result;
    }

    public double toScalar(double[] i) {
        double result = 0.0;
        int length = i.length;
        for (int k = 0; k < length; k++) {
            double ik = i[k];
            result += ik * ik;
        }
        return Math.sqrt(result);
    }

    public void div(double l, double[] output) {
        for (int i = 0; i < output.length; i++) output[i] = l / output[i];
    }

    public double[] ones(int length) {
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = 1f;
        }
        return result;
    }

    public void unitize(double[] result) {
        double length = toScalar(result);
        if (length != 0 && !Double.isNaN(length)) {
            div(result, length);
        }
    }

    public void normalize(double[] result) {
        double sum = absSumValues(result);
        if (sum != 0 && !Double.isNaN(sum)) div(result, sum); else {
            double equi = 1.0 / result.length;
            for (int i = 0; i < result.length; i++) {
                result[i] = equi;
            }
        }
    }

    public double euclidianDistance(double[] mean, double[] input) {
        int length = mean.length;
        double distance = 0.0;
        for (int i = 0; i < length; i++) {
            double d = mean[i] - input[i];
            distance += d * d;
        }
        return Math.sqrt(distance);
    }

    /**
	 * Instead of euclidDistance distance, city block distance formula is
	 * applied.
	 * 
	 * @param i
	 * @return double
	 */
    public double cityBlockDistance(double[] i, double[] j) {
        int length = i.length;
        double distance = 0.0;
        for (int k = 0; k < length; k++) {
            distance += i[k] - j[k];
        }
        return distance;
    }

    /**
	 * a.b=|a|.|b|.cos(c); c=arcsin( (a.b) / (|a|.|b|) )
	 * 
	 * Zero vectors gives NAN!!
	 * 
	 * @param i
	 * @param j
	 * @return The angle between the vectors..
	 */
    public double angularDistance(double[] i, double[] j) {
        int length = i.length;
        double a_b = 0.0;
        for (int k = 0; k < length; k++) {
            a_b += i[k] * j[k];
        }
        double a = toScalar(i);
        double b = toScalar(j);
        return Math.acos(a_b / (a * b));
    }

    public void add(double[][] d1, double[][] d2) {
        for (int i = 0; i < d1.length; i++) add(d1[i], d2[i]);
    }

    public void add(double[][] d1, double a) {
        for (double[] aD1 : d1) add(aD1, a);
    }

    public void sub(double[][] d1, double[][] d2) {
        for (int i = 0; i < d1.length; i++) sub(d1[i], d2[i]);
    }

    public void sub(double[][] d1, double a) {
        add(d1, -a);
    }

    /**
	 * Takes transpose of matrix. Because the dimensions of transposed and input
	 * double[][] will be different another array is created and input is not
	 * altered!.
	 * 
	 * @param d
	 *            MxN matrix..
	 * @return NxM matrix..
	 */
    public double[][] transpose(double[][] d) {
        double[][] result = new double[d[0].length][d.length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                result[j][i] = d[i][j];
            }
        }
        return result;
    }

    public void mult(double[][] d1, double a) {
        for (double[] aD1 : d1) mult(aD1, a);
    }

    public void div(double[][] d1, double a) {
        mult(d1, 1 / a);
    }

    public void dot_mult(double[][] d1, double[][] d2) {
        for (int i = 0; i < d1.length; i++) mult(d1[i], d2[i]);
    }

    public void dot_div(double[][] d1, double[][] d2) {
        for (int i = 0; i < d1.length; i++) div(d1[i], d2[i]);
    }

    public void dot_div(double f, double[][] d1) {
        for (double[] dd : d1) div(f, dd);
    }

    /**
	 * Takes the exp of each item seperately.. a[i][j]=exp(d[i][j]);
	 * 
	 * @param d
	 */
    public void exp(double[][] d) {
        for (double[] aD : d) exp(aD);
    }

    /**
	 * Takes the log of each item seperately a[i][j]=log(d[i][j]);
	 * 
	 * @param d
	 */
    public void log(double[][] d) {
        for (double[] aD : d) log(aD);
    }

    /**
	 * Takes the power of each item seperately a[i][j]=Math.pow(d[i][j],p);
	 * 
	 * @param d
	 * @param p
	 */
    public void pow_dot(double[][] d, double p) {
        for (double[] aD : d) pow(aD, p);
    }

    public double[][] zeros(int row_count, int column_count) {
        return new double[row_count][column_count];
    }

    public double[][] ones(int row_count, int column_count) {
        double[][] zeros = zeros(row_count, column_count);
        add(zeros, 1);
        return zeros;
    }

    /**
	 * Unit matrix, with dimension DIMxDIM..
	 * 
	 * @param dim
	 * @return double[][]
	 */
    public double[][] eye(int dim) {
        double[][] result = zeros(dim, dim);
        for (int i = 0; i < dim; i++) {
            result[i][i] = 1;
        }
        return result;
    }

    /**
	 * Makes a zero matrix, and assigns the elements to its diagonal..
	 * 
	 * @param diagonal
	 * @return double [][]
	 */
    public double[][] diagonal(double[] diagonal) {
        int l = diagonal.length;
        double[][] result = zeros(l, l);
        for (int i = 0; i < l; i++) result[i][i] = diagonal[i];
        return result;
    }

    public double[] getDiagonal(double[][] matrix) {
        double[] result = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) result[i] = matrix[i][i];
        return result;
    }

    public double[][] duplicate(double[][] d) {
        double[][] result = new double[d.length][];
        for (int i = 0; i < d.length; i++) {
            result[i] = duplicate(d[i]);
        }
        return result;
    }

    public String toString(double[][] f) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < f.length - 1; i++) sb.append((toString(f[i]) + "\n"));
        if (f.length > 0) sb.append(toString(f[f.length - 1]));
        return sb.toString();
    }

    /**
	 * @param d
	 *            1xM
	 * @return Mx1
	 */
    public double[][] transpose(double[] d) {
        double[][] d1 = new double[1][];
        d1[0] = d;
        return transpose(d1);
    }

    public double[] absSumRows(double[][] d1) {
        double[] result = new double[d1.length];
        for (int i = 0; i < d1.length; i++) {
            result[i] = absSumValues(d1[i]);
        }
        return result;
    }

    public double[] sumRows(double[][] d1) {
        double[] result = new double[d1.length];
        for (int i = 0; i < d1.length; i++) {
            result[i] = sumValues(d1[i]);
        }
        return result;
    }

    public double[] absSumColumns(double[][] d1) {
        return absSumRows(transpose(d1));
    }

    /**
	 * Sums each column seperately..
	 * 
	 * @param d1
	 */
    public double[] sumColumns(double[][] d1) {
        return sumRows(transpose(d1));
    }

    public void normalizeRowWise(double[][] d1) {
        double[] sum = absSumRows(d1);
        for (int i = 0; i < d1.length; i++) div(d1[i], sum[i]);
    }

    /**
	 * Normalizes each column seperately!!..
	 * 
	 * @param d1
	 */
    public void normalizeColumnWise(double[][] d1) {
        double[] sum = absSumColumns(d1);
        for (double[] dd : d1) div(dd, sum);
    }

    /**
	 * Multiplies the matrices.
	 * 
	 * @param d1
	 *            MxN matrix
	 * @param d2
	 *            NxK matrix
	 * @return MxK matrix
	 */
    public double[][] mult(double[][] d1, double[][] d2) {
        double[][] d2t = transpose(d2);
        double[][] result = new double[d1.length][d2t.length];
        for (int i = 0; i < d1.length; i++) {
            double[] dd = d1[i];
            int length = dd.length;
            for (int j = 0; j < d2t.length; j++) {
                double[] d2tj = d2t[j];
                double value = 0.0;
                for (int k = 0; k < length; k++) {
                    value += dd[k] * d2tj[k];
                }
                result[i][j] = value;
            }
        }
        return result;
    }

    public void abs(double[] val) {
        for (int i = 0; i < val.length; i++) {
            val[i] = Math.abs(val[i]);
        }
    }
}
