package cluster.spectral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Edge;
import model.Node;
import model.Timer;
import util.GraphUtil;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.Functions;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * @author Cassio
 */
public class SpectralClusterer {

    public static final int DEFAULT_SPATIAL_RADIUS = 20;

    private static final int DEFAULT_SPATIAL_DELTA = 10;

    public static final String DEFAULT_ALGORITHM = "Lanczos";

    private static final int LANCZOS_FLOOR = 10;

    private static final double LANCZOS_LOAD = 0.05;

    private static final int LANCZOS_CEILING = 30;

    private boolean m_log = false;

    private String m_algorithm = DEFAULT_ALGORITHM;

    private double m_radius = 1;

    private List m_cluster1 = new ArrayList(), m_cluster2 = new ArrayList();

    DoubleMatrix2D graphAdjacencyMatrix = null;

    public SpectralClusterer(int radius) {
        this.m_radius = (double) radius / 10;
    }

    public void setLog(boolean log) {
        m_log = log;
    }

    public void setAlgorithm(String algorithm) {
        m_algorithm = algorithm;
    }

    private double aff(List a, List b) {
        double sum = 0;
        for (Iterator aIterator = a.iterator(); aIterator.hasNext(); ) {
            int i = (Integer) aIterator.next();
            for (Iterator bIterator = b.iterator(); bIterator.hasNext(); ) {
                int j = (Integer) bIterator.next();
                sum += graphAdjacencyMatrix.get(i, j);
                ;
            }
        }
        return sum;
    }

    public double ncut(List a, List b, List v) {
        if (a.size() == 0 || b.size() == 0 || v.size() == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double nassoc = (aff(a, a) / aff(a, v) + aff(b, b) / aff(b, v));
        double x = aff(a, b);
        double d1 = aff(a, v);
        double d2 = aff(b, v);
        double ncut = (x / d1 + x / d2);
        return ncut;
    }

    Map<Integer, Node> nodeMatrixIndex;

    public Set<Set<Node>> cluster(Graph<Node, Edge> graph) {
        Pair p = GraphUtil.graphToAllDistanceMatrix(graph);
        nodeMatrixIndex = (Map<Integer, Node>) p.getSecond();
        return this.cluster((DoubleMatrix2D) p.getFirst());
    }

    public Set<Set<Node>> cluster(DoubleMatrix2D adjacencyMatrix) {
        this.graphAdjacencyMatrix = adjacencyMatrix;
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < this.graphAdjacencyMatrix.rows(); i++) {
            indexes.add(i);
        }
        List<List<Integer>> in = new ArrayList<List<Integer>>();
        in.add(indexes);
        List<List<Integer>> out = new ArrayList<List<Integer>>();
        final long start = System.currentTimeMillis();
        int count = 0;
        while (!in.isEmpty()) {
            List parent = (List) in.remove(0);
            this.split(parent);
            List cluster1 = this.getCluster1();
            List cluster2 = this.getCluster2();
            if (this.ncut(cluster1, cluster2, parent) > 0.5) {
                out.add(parent);
            } else {
                in.add(cluster1);
                in.add(cluster2);
            }
            count++;
        }
        Set<Set<Node>> ret = null;
        if (nodeMatrixIndex != null) {
            ret = new HashSet<Set<Node>>();
            for (List<Integer> cluster : out) {
                Set<Node> c = new HashSet<Node>();
                for (Integer n : cluster) {
                    c.add(nodeMatrixIndex.get(n));
                }
                ret.add(c);
            }
        }
        return ret;
    }

    private void split(List indexes) {
        split(indexes, new Timer());
    }

    private void split(List indexes, Timer timer) {
        m_cluster1 = new ArrayList();
        m_cluster2 = new ArrayList();
        if (indexes.size() <= 1) {
            return;
        }
        timer.start("Generate W, D");
        DoubleMatrix2D adjacencyMatrix = DoubleFactory2D.dense.make(indexes.size(), indexes.size());
        for (int i = 0; i < indexes.size(); i++) {
            int temp1 = (Integer) indexes.get(i);
            for (int j = 0; j < indexes.size(); j++) {
                int temp2 = (Integer) indexes.get(j);
                adjacencyMatrix.set(i, j, graphAdjacencyMatrix.get(temp1, temp2));
            }
        }
        if (m_log) {
            System.out.println("Ajacency matrix:" + adjacencyMatrix);
        }
        DoubleMatrix2D diagonalMatrix = DoubleFactory2D.dense.make(indexes.size(), indexes.size());
        for (int row = 0; row < indexes.size(); row++) {
            double sum = 0;
            for (int col = 0; col < indexes.size(); col++) {
                sum += adjacencyMatrix.get(row, col);
            }
            diagonalMatrix.set(row, row, sum);
        }
        if (m_log) {
            System.out.println("Diagonal matrix:" + diagonalMatrix);
        }
        timer.end();
        timer.start("Compute laplacian matrix");
        DoubleMatrix2D laplacianMatrix = diagonalMatrix.copy().assign(adjacencyMatrix, Functions.minus);
        timer.end();
        if (m_log) {
            System.out.println("Laplacian matrix:" + laplacianMatrix);
        }
        timer.start("Cholesky decomposition");
        LanczosDecomposition lanczosDecomposition = new LanczosDecomposition(laplacianMatrix, getLanczosK(laplacianMatrix), false);
        DoubleMatrix1D eigenvalues;
        DoubleMatrix2D eigenvectors;
        eigenvalues = lanczosDecomposition.getRealEigenvalues();
        eigenvectors = lanczosDecomposition.getV();
        timer.end();
        if (m_log) {
            System.out.println("Eigenvalues:" + eigenvalues);
            System.out.println("Eigenvectors:" + eigenvectors);
        }
        timer.start("Select eigenvector");
        double minEigenvalue = Double.POSITIVE_INFINITY;
        int minEigenvalueIndex = 0;
        for (int index = 0; index < eigenvalues.size(); index++) {
            if (eigenvalues.get(index) < minEigenvalue) {
                boolean hasNegAndPos = false;
                double lastVal = 0;
                for (int j = 0; (j < eigenvectors.rows()) && (!hasNegAndPos); j++) {
                    double v = eigenvectors.get(j, index);
                    if (lastVal < 0 && v > 0) hasNegAndPos = true; else if (lastVal >= 0 && v < 0) hasNegAndPos = true; else lastVal = v;
                }
                if (hasNegAndPos) {
                    minEigenvalue = eigenvalues.get(index);
                    minEigenvalueIndex = index;
                }
            }
        }
        timer.end();
        DoubleMatrix1D eigenvector = eigenvectors.viewColumn(minEigenvalueIndex);
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
            if (eigenvector.get(index) <= split) {
                m_cluster1.add(index);
            } else {
                m_cluster2.add(index);
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

    private List getCluster1() {
        return m_cluster1;
    }

    private List getCluster2() {
        return m_cluster2;
    }

    private DoubleMatrix2D getGraphAdjacencyMatrix() {
        return graphAdjacencyMatrix;
    }

    private void setGraphAdjacencyMatrix(DoubleMatrix2D graphAdjacencyMatrix) {
        this.graphAdjacencyMatrix = graphAdjacencyMatrix;
    }
}
