package sandbox;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.math.Functions;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import cluster.spectral.LanczosDecomposition;
import model.Dot;
import model.Timer;

/**
 * @author <A HREF="mailto:razvan.surdulescu@trilogy.com">Razvan Surdulescu</A>, (C) Trilogy 2003
 */
public class ClusteringAlgorithm {

    public static final int DEFAULT_SPATIAL_RADIUS = 20;

    private static final int DEFAULT_SPATIAL_DELTA = 10;

    public static final String DEFAULT_ALGORITHM = "Lanczos";

    private static final int LANCZOS_FLOOR = 10;

    private static final double LANCZOS_LOAD = 0.05;

    private static final int LANCZOS_CEILING = 30;

    private boolean m_log = false;

    private String m_algorithm = DEFAULT_ALGORITHM;

    private double m_radius = DEFAULT_SPATIAL_RADIUS;

    private double m_delta = DEFAULT_SPATIAL_DELTA;

    private List m_cluster1 = new ArrayList(), m_cluster2 = new ArrayList();

    public void setLog(boolean log) {
        m_log = log;
    }

    public void setAlgorithm(String algorithm) {
        m_algorithm = algorithm;
    }

    public void setRadius(double radius) {
        m_radius = radius;
    }

    private double aff(List a, List b) {
        double sum = 0;
        for (Iterator aIterator = a.iterator(); aIterator.hasNext(); ) {
            Dot aDot = (Dot) aIterator.next();
            for (Iterator bIterator = b.iterator(); bIterator.hasNext(); ) {
                Dot bDot = (Dot) bIterator.next();
                sum += aDot.getWeight(bDot, m_radius, m_delta);
            }
        }
        return sum;
    }

    public double ncut(List a, List b, List v) {
        if (a.size() == 0 || b.size() == 0 || v.size() == 0) {
            return Double.MAX_VALUE;
        }
        double x = aff(a, b);
        return (x / aff(a, v) + x / aff(b, v));
    }

    public void split(List dots) {
        split(dots, new Timer());
    }

    public void split(List dots, Timer timer) {
        m_cluster1 = new ArrayList();
        m_cluster2 = new ArrayList();
        if (dots.size() <= 1) {
            return;
        }
        timer.start("Generate W, D");
        DoubleMatrix2D weightMatrix = DoubleFactory2D.dense.make(dots.size(), dots.size());
        for (int row = 0; row < dots.size(); row++) {
            Dot a = (Dot) dots.get(row);
            for (int col = 0; col < dots.size(); col++) {
                Dot b = (Dot) dots.get(col);
                weightMatrix.set(row, col, a.getWeight(b, m_radius, m_delta));
            }
        }
        if (m_log) {
            System.out.println("Weight matrix:" + weightMatrix);
        }
        DoubleMatrix2D distanceMatrix = DoubleFactory2D.dense.make(dots.size(), dots.size());
        for (int row = 0; row < dots.size(); row++) {
            double sum = 0;
            for (int col = 0; col < dots.size(); col++) {
                sum += weightMatrix.get(row, col);
            }
            distanceMatrix.set(row, row, sum);
        }
        timer.end();
        if (m_log) {
            System.out.println("Distance matrix:" + distanceMatrix);
        }
        timer.start("Compute difference matrix");
        DoubleMatrix2D differenceMatrix = distanceMatrix.copy().assign(weightMatrix, Functions.minus);
        timer.end();
        timer.start("Cholesky decomposition");
        CholeskyDecomposition choleskyDecomposition = new CholeskyDecomposition(distanceMatrix);
        DoubleMatrix2D rootDistanceMatrix = choleskyDecomposition.getL();
        timer.end();
        if (m_log) {
            System.out.println("Cholesky decomposition matrix:" + rootDistanceMatrix);
        }
        timer.start("Compute eigen matrix");
        DoubleMatrix2D half = Algebra.DEFAULT.transpose(Algebra.DEFAULT.solveTranspose(Algebra.DEFAULT.transpose(rootDistanceMatrix), differenceMatrix));
        DoubleMatrix2D eigenMatrix = Algebra.DEFAULT.solve(rootDistanceMatrix, half);
        timer.end();
        if (m_log) {
            System.out.println("Eigenmatrix:" + eigenMatrix);
        }
        DoubleMatrix1D eigenvalues;
        DoubleMatrix2D eigenvectors;
        if (m_algorithm.equals("Lanczos")) {
            timer.start("Eigenvalue decomposition");
            LanczosDecomposition lanczosDecomposition = new LanczosDecomposition(eigenMatrix, getLanczosK(eigenMatrix), m_log);
            timer.end();
            eigenvalues = lanczosDecomposition.getRealEigenvalues();
            eigenvectors = lanczosDecomposition.getV();
        } else if (m_algorithm.equals("QL")) {
            timer.start("Eigenvalue decomposition");
            EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(eigenMatrix);
            timer.end();
            eigenvalues = eigenvalueDecomposition.getRealEigenvalues();
            eigenvectors = eigenvalueDecomposition.getV();
        } else {
            throw new IllegalArgumentException("Unrecognized algorithm: " + m_algorithm);
        }
        if (m_log) {
            System.out.println("Eigenvalues:" + eigenvalues);
        }
        timer.start("Select eigenvector");
        double minEigenvalue = eigenvalues.get(0);
        int minEigenvalueIndex = 0;
        for (int index = 1; index < eigenvalues.size(); index++) {
            if (eigenvalues.get(index) < minEigenvalue) {
                minEigenvalue = eigenvalues.get(index);
                minEigenvalueIndex = index;
            }
        }
        DoubleMatrix1D eigenvector = Algebra.DEFAULT.mult(Algebra.DEFAULT.inverse(Algebra.DEFAULT.transpose(rootDistanceMatrix)), eigenvectors.viewColumn(minEigenvalueIndex));
        timer.end();
        if (m_log) {
            System.out.println("Selected eigenvalue:" + eigenvalues.get(minEigenvalueIndex));
            System.out.println("Selected eigenvector:" + eigenvector);
        }
        timer.start("Cluster");
        double maxComponent = eigenvector.get(0), minComponent = eigenvector.get(0);
        for (int index = 1; index < eigenvector.size(); index++) {
            if (maxComponent < eigenvector.get(index)) {
                maxComponent = eigenvector.get(index);
            }
            if (minComponent > eigenvector.get(index)) {
                minComponent = eigenvector.get(index);
            }
        }
        double split = (maxComponent + minComponent) / 2;
        if (m_log) {
            System.out.println("Split value:" + split);
        }
        for (int index = 0; index < eigenvector.size(); index++) {
            Dot dot = (Dot) dots.get(index);
            if (eigenvector.get(index) <= split) {
                m_cluster1.add(dot);
            } else {
                m_cluster2.add(dot);
            }
        }
        timer.end();
    }

    private int getLanczosK(DoubleMatrix2D matrix) {
        int k = (int) (matrix.columns() * LANCZOS_LOAD);
        if (k < LANCZOS_FLOOR) {
            return Math.min(matrix.columns(), LANCZOS_FLOOR);
        } else if (k > LANCZOS_CEILING) {
            return Math.min(matrix.columns(), LANCZOS_CEILING);
        } else {
            return k;
        }
    }

    public List getCluster1() {
        return m_cluster1;
    }

    public List getCluster2() {
        return m_cluster2;
    }
}
