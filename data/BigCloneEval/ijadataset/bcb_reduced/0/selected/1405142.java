package skellib.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;
import skellib.core.Labels;
import skellib.core.Source;
import skellib.libsvm.svm_node;
import skellib.libsvm.svm_problem;
import skellib.model.kernel.Kernel;
import skellib.model.kernel.vectorial.RBF;
import skellib.tools.KernelMatrixTimer;
import skellib.tools.StatisticUtils;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class KernelMatrix extends Matrix implements Source, Serializable {

    public boolean addInstance(float _value, String representation) {
        return false;
    }

    public String getIdentifier() {
        return "KernelMatrix";
    }

    public String InstanceToString(int i) {
        return null;
    }

    String source;

    private static final long serialVersionUID = 1L;

    public Object getInstance(int i) {
        return getArray()[i];
    }

    public int getSize() {
        return getValues().length;
    }

    public float getValue(int i) {
        return (float) getValues()[i];
    }

    double[] values;

    int[][] class_ind;

    Labels labels;

    double[] dev;

    public KernelMatrix addDiag(double sigma) {
        double[][] temp_matrix = new double[size()][size()];
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                temp_matrix[i][j] = super.get(i, j) + sigma;
            }
        }
        return new KernelMatrix(temp_matrix, values);
    }

    public double[] standardizedValues() {
        double mean = StatisticUtils.mean(values);
        double dev = Math.sqrt(StatisticUtils.variance(values));
        for (int i = 0; i < values.length; i++) {
            values[i] = (values[i] - mean) / dev;
        }
        return new double[] { mean, dev };
    }

    public void normalizedValues() {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < getSize(); i++) {
            if (getValue(i) < min) min = getValue(i);
            if (getValue(i) > max) max = getValue(i);
        }
        for (int i = 0; i < getSize(); i++) {
            values[i] -= min;
            values[i] = values[i] / (max - min);
        }
    }

    public void normalizeMatrix() {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                if (get(i, j) < min) min = get(i, j);
                if (get(i, j) > max) max = get(i, j);
            }
        }
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                double d = get(i, j);
                d -= min;
                d = d / (max - min);
                set(i, j, d);
            }
        }
    }

    public Matrix getPseudoInverse() {
        SingularValueDecomposition svd = super.svd();
        Matrix U = svd.getU();
        Matrix S = svd.getS();
        Matrix V = svd.getV();
        for (int i = 0; i < S.getRowDimension(); i++) {
            if (S.get(i, i) != 0.0) {
                S.set(i, i, 1.0 / S.get(i, i));
            }
        }
        return V.times(S.times(U.transpose()));
    }

    /**
	 * Make the Kernelmatrix positiv semidefinit by adding the smallest (largest negative)
	 * eigenvalue to the diagonal elements if the matrix is not already p.s.d.
	 * @return a positive semidefinite Kernelmatrix
	 */
    public KernelMatrix makePSD() throws Exception {
        EigenvalueDecomposition eig = this.eig();
        double[] val = eig.getRealEigenvalues();
        double min = 0.0;
        for (int i = 0; i < val.length; i++) {
            if (val[i] < min) {
                min = val[i];
            }
        }
        if (min >= 0.0) {
            System.out.println("Matrix is already p.s.d.");
            return this;
        } else {
            System.out.println("Matrix is not p.s.d. - adding " + min + "to diagonal");
            return addDiag(Math.abs((float) min));
        }
    }

    public double[] getValues() {
        return values;
    }

    public static int determineSizeOfKernel(String s) {
        BufferedReader labelreader = null;
        try {
            labelreader = new BufferedReader(new FileReader(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector<Double> t = new Vector<Double>();
        String line = "";
        int z = 0;
        try {
            while ((line = labelreader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                Double d = new Double(st.nextToken());
                if (!t.contains(d)) z++;
                t.add(d);
            }
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return t.size();
    }

    public static KernelMatrix generateFromFiles(String matrix_file, String label_file) {
        try {
            BufferedReader label_reader = new BufferedReader(new FileReader(label_file));
            String line = "";
            Vector<Double> temp = new Vector<Double>();
            while ((line = label_reader.readLine()) != null) {
                temp.add(Double.parseDouble(line));
            }
            double[] lab = new double[temp.size()];
            double[][] mat = new double[lab.length][lab.length];
            int i = 0;
            BufferedReader matrix_reader = new BufferedReader(new FileReader(matrix_file));
            while ((line = matrix_reader.readLine()) != null) {
                lab[i] = temp.get(i);
                StringTokenizer st = new StringTokenizer(line);
                int j = 0;
                while (st.hasMoreElements()) {
                    mat[i][j] = Double.parseDouble(st.nextToken());
                    j++;
                }
                i++;
            }
            return new KernelMatrix(mat, lab);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KernelMatrix(Source source, Kernel kernel) {
        super(source.getSize(), source.getSize());
        double[][] matrix = new double[source.getSize()][source.getSize()];
        values = new double[source.getSize()];
        KernelMatrixTimer kmt = new KernelMatrixTimer(source.getSize(), Thread.currentThread(), 30);
        kmt.start();
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j <= i; j++) {
                Object o_i = source.getInstance(i);
                Object o_j = source.getInstance(j);
                matrix[i][j] = kernel.kernel(o_i, o_j);
                matrix[j][i] = matrix[i][j];
                kmt.calculationdone();
                values[i] = source.getValue(i);
            }
        }
        kmt.finalreport();
        kmt.stop();
        labels = new Labels();
        int[] ind = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            ind[i] = i;
            labels.addIfNotContained(values[i]);
        }
        super.setMatrix(ind, ind, new Matrix(matrix));
        class_ind = labels.getClassIndices();
        System.out.println("Matrix for " + kernel.getIdentifier() + " successfully generated");
    }

    public KernelMatrix(float[][] matrix, float[] _values) {
        super(matrix.length, matrix.length);
        values = new double[_values.length];
        labels = new Labels();
        for (int i = 0; i < values.length; i++) {
            values[i] = _values[i];
            labels.addIfNotContained(values[i]);
            for (int j = 0; j < matrix[i].length; j++) {
                super.set(i, j, matrix[i][j]);
            }
        }
        class_ind = labels.getClassIndices();
    }

    public KernelMatrix(double[][] matrix, double[] _values) {
        super(matrix);
        values = _values;
        labels = new Labels();
        for (int i = 0; i < values.length; i++) {
            labels.addIfNotContained(values[i]);
        }
        class_ind = labels.getClassIndices();
    }

    public KernelMatrix(String in, int n, boolean b) {
        super(n, n);
        if (n == 0) {
            System.err.println("Empty Matrix");
            System.exit(1);
        }
        BufferedReader labelreader = null;
        BufferedReader matrixreader = null;
        labels = new Labels();
        try {
            labelreader = new BufferedReader(new FileReader(in));
            matrixreader = new BufferedReader(new FileReader(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector<Double> t = new Vector<Double>();
        String line = "";
        int z = 0;
        try {
            while ((line = labelreader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                Double d = new Double(st.nextToken());
                if (!t.contains(d)) z++;
                t.add(d);
                labels.addIfNotContained(d);
            }
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        values = new double[t.size()];
        double[][] series = new double[values.length][values.length];
        int[] ind = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = t.get(i).doubleValue();
            ind[i] = i;
        }
        t.clear();
        int i = 0;
        try {
            while ((line = matrixreader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                st.nextElement();
                st.nextElement();
                int j = 0;
                while (j < n) {
                    float sim = 0.0f;
                    String s = "";
                    try {
                        s = st.nextToken();
                    } catch (RuntimeException e1) {
                        s = "" + (j + 1) + ":0.0";
                    }
                    int g = 0;
                    int h = 0;
                    try {
                        g = s.indexOf(":");
                        h = Integer.parseInt(s.substring(0, g));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        System.out.println(s);
                    }
                    sim = Float.parseFloat(s.substring(g + 1));
                    while (j < h - 1) {
                        series[i][j] = 0.0f;
                        j++;
                    }
                    series[i][j] = sim;
                    j++;
                }
                i++;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(values.length + " entries loaded");
        double[][] M = new double[n][n];
        for (i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = RBF.rbf(series[i], series[j]);
                M[j][i] = M[i][j];
            }
        }
        super.setMatrix(ind, ind, new Matrix(M));
        class_ind = labels.getClassIndices();
    }

    public KernelMatrix(String in, int n) {
        super(n, n);
        if (n == 0) {
            System.err.println("Empty Matrix");
            System.exit(1);
        }
        System.out.println("Loading Kernelmatrix " + in);
        BufferedReader labelreader = null;
        BufferedReader matrixreader = null;
        labels = new Labels();
        try {
            labelreader = new BufferedReader(new FileReader(in));
            matrixreader = new BufferedReader(new FileReader(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector<Double> t = new Vector<Double>();
        String line = "";
        int z = 0;
        try {
            while ((line = labelreader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                Double d = new Double(st.nextToken());
                if (!t.contains(d)) z++;
                t.add(d);
                labels.addIfNotContained(d);
            }
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        values = new double[t.size()];
        double[][] series = new double[values.length][values.length];
        int[] ind = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = t.get(i).doubleValue();
            ind[i] = i;
        }
        t.clear();
        int i = 0;
        try {
            while ((line = matrixreader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                st.nextElement();
                st.nextElement();
                int j = 0;
                while (j < n) {
                    float sim = 0.0f;
                    String s = "";
                    try {
                        s = st.nextToken();
                    } catch (RuntimeException e1) {
                        s = "" + (j + 1) + ":0.0";
                    }
                    int g = 0;
                    int h = 0;
                    try {
                        g = s.indexOf(":");
                        h = Integer.parseInt(s.substring(0, g));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        System.out.println(s);
                    }
                    sim = Float.parseFloat(s.substring(g + 1));
                    while (j < h - 1) {
                        series[i][j] = 0.0f;
                        j++;
                    }
                    if (sim < 0.0 || sim > 1.0) {
                        System.out.println("ERROR: kernel(" + i + "," + j + ") = " + sim + " in " + s);
                    }
                    series[i][j] = sim;
                    j++;
                }
                i++;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(values.length + " entries loaded");
        super.setMatrix(ind, ind, new Matrix(series));
        class_ind = labels.getClassIndices();
    }

    public int size() {
        return values.length;
    }

    public Matrix solve() {
        return solve(new Matrix(values, values.length));
    }

    public KernelMatrix getClass(int x) {
        double[] _values = new double[labels.getCount(x)];
        double[][] _matrix = new double[_values.length][_values.length];
        for (int i = 0; i < _values.length; i++) {
            int s = class_ind[x][i];
            _values[i] = values[s];
            for (int j = 0; j < _values.length; j++) {
                int t = class_ind[x][j];
                _matrix[i][j] = get(s, t);
            }
        }
        return new KernelMatrix(_matrix, _values);
    }

    public svm_problem getTestsetFor(int[] indices) {
        Arrays.sort(indices);
        svm_problem prob = new svm_problem();
        prob.l = values.length;
        prob.y = new double[prob.l];
        prob.x = new svm_node[prob.l][indices.length];
        for (int i = 0; i < values.length; i++) {
            prob.y[i] = values[i];
            for (int j = 0; j < indices.length; j++) {
                prob.x[i][j] = new svm_node();
                prob.x[i][j].index = j;
                prob.x[i][j].value = get(i, indices[j]);
            }
        }
        return prob;
    }

    public svm_problem getForLibSVM() {
        svm_problem prob = new svm_problem();
        prob.l = values.length;
        prob.y = new double[prob.l];
        prob.x = new svm_node[prob.l][prob.l + 1];
        for (int i = 0; i < prob.l; i++) {
            prob.y[i] = values[i];
            prob.x[i][0] = new svm_node();
            prob.x[i][0].index = 0;
            prob.x[i][0].value = i + 1;
            for (int j = 1; j < prob.l + 1; j++) {
                prob.x[i][j] = new svm_node();
                prob.x[i][j].index = j;
                prob.x[i][j].value = get(i, j - 1);
            }
        }
        return prob;
    }

    public KernelMatrix getSubKernelMatrix(int[] indices) {
        double[][] _matrix = super.getMatrix(indices, indices).getArray();
        double[] _values = new double[indices.length];
        for (int i = 0; i < _values.length; i++) {
            _values[i] = values[indices[i]];
        }
        return new KernelMatrix(_matrix, _values);
    }

    public void writeTo(String out) throws Exception {
        FileWriter w = new FileWriter(out);
        for (int i = 0; i < values.length; i++) {
            w.write(values[i] + " 0:" + (i + 1));
            for (int j = 0; j < values.length; j++) {
                w.write(" " + (j + 1) + ":" + get(i, j));
            }
            if (i < values.length - 1) w.write("\n");
        }
        w.close();
    }

    public void writeTo(String out, String format) throws Exception {
        if (format.equalsIgnoreCase("LIBSVM")) writeTo(out);
        FileWriter w = new FileWriter(out);
        if (format.equalsIgnoreCase("MATLAB")) {
            for (int i = 0; i < values.length; i++) {
                w.write("" + values[i]);
                for (int j = 0; j < values.length; j++) {
                    w.write(" " + get(i, j));
                }
                w.write("\n");
            }
        }
        w.close();
    }

    public static void main(String[] args) {
    }

    public String getLabel(int i) {
        return null;
    }

    public String getName(int i) {
        return null;
    }

    public String getSourceName() {
        return this.source;
    }

    public void setSourceName(String s) {
        this.source = s;
    }

    public boolean addInstance(String representation) {
        return false;
    }
}
