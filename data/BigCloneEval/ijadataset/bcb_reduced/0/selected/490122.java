package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
import org.tigr.util.awt.ProgressDialog;

public class QTC extends AbstractAlgorithm {

    private boolean stop = false;

    private int function;

    private float factor;

    ProgressDialog progress;

    private int number_of_genes;

    private int number_of_samples;

    boolean useAbsolute;

    float diameter;

    int minimumClusterSize;

    Vector allClusters = new Vector();

    private double xMax, xMin;

    private int clusterSize = Integer.MAX_VALUE;

    private boolean qtcGenes;

    private FloatMatrix expMatrix;

    private JackknifedMatrixBySpecifiedExp[] jacked;

    private float[][] proximity;

    private float adjustedDiameter;

    private int hcl_function;

    private boolean hcl_absolute;

    public synchronized AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        AlgorithmParameters map = data.getParams();
        function = map.getInt("distance-function", PEARSON);
        factor = map.getFloat("distance-factor", 1.0f);
        qtcGenes = map.getBoolean("qtc-cluster-genes");
        useAbsolute = map.getBoolean("use-absolute", false);
        diameter = map.getFloat("diameter", 0.2f);
        minimumClusterSize = map.getInt("min-cluster-size", 1);
        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);
        boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        int method_linkage = map.getInt("method-linkage", 0);
        boolean calculate_genes = map.getBoolean("calculate-genes", false);
        boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
        this.expMatrix = data.getMatrix("experiment");
        this.number_of_genes = this.expMatrix.getRowDimension();
        this.number_of_samples = this.expMatrix.getColumnDimension();
        JFrame dummyFrame = new JFrame();
        progress = new ProgressDialog(dummyFrame, "QTClust -- Progress", false, 6);
        JPanel progressPanel = progress.getLabelPanel();
        JPanel superPanel = new JPanel();
        superPanel.setLayout(new BorderLayout());
        JButton abortButton = new JButton("Abort");
        abortButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stop = true;
                progress.dismiss();
            }
        });
        superPanel.add(progressPanel, BorderLayout.NORTH);
        superPanel.add(abortButton, BorderLayout.SOUTH);
        progress.setMainPanel(superPanel);
        AlgorithmData result = new AlgorithmData();
        if (stop) {
            result.addParam("aborted", "true");
            return result;
        }
        Vector[] clusters = calculate(useAbsolute, diameter, minimumClusterSize);
        if (stop) {
            result.addParam("aborted", "true");
            return result;
        }
        FloatMatrix means = getMeans(clusters);
        FloatMatrix variances = getVariances(clusters, means);
        AlgorithmEvent event = null;
        if (hierarchical_tree) {
            event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, clusters.length, "Calculate Hierarchical Trees");
            fireValueChanged(event);
            event.setIntValue(0);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);
            fireValueChanged(event);
        }
        Cluster result_cluster = new Cluster();
        NodeList nodeList = result_cluster.getNodeList();
        int[] features;
        for (int i = 0; i < clusters.length; i++) {
            if (stop) {
                throw new AbortException();
            }
            features = convert2int(clusters[i]);
            Node node = new Node(features);
            nodeList.addNode(node);
            if (hierarchical_tree) {
                node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                event.setIntValue(i + 1);
                fireValueChanged(event);
            }
        }
        result.addCluster("cluster", result_cluster);
        result.addParam("number-of-clusters", String.valueOf(clusters.length));
        result.addMatrix("clusters_means", means);
        result.addMatrix("clusters_variances", variances);
        return result;
    }

    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        if (qtcGenes) experiment = getSubExperiment(this.expMatrix, features); else experiment = experiment = getSubExperimentReducedCols(this.expMatrix, features);
        data.addMatrix("experiment", experiment);
        data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
        data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
        data.addParam("method-linkage", String.valueOf(method));
        HCL hcl = new HCL();
        AlgorithmData result;
        if (genes) {
            data.addParam("calculate-genes", String.valueOf(true));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        if (experiments) {
            data.addParam("calculate-genes", String.valueOf(false));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        return nodeList;
    }

    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
        target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
        target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
        target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
        target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }

    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
        FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
        for (int i = 0; i < features.length; i++) {
            subExperiment.A[i] = experiment.A[features[i]];
        }
        return subExperiment;
    }

    /**
     *  Creates a matrix with reduced columns (samples) as during experiment clustering
     */
    private FloatMatrix getSubExperimentReducedCols(FloatMatrix experiment, int[] features) {
        FloatMatrix copyMatrix = experiment.copy();
        FloatMatrix subExperiment = new FloatMatrix(features.length, copyMatrix.getColumnDimension());
        for (int i = 0; i < features.length; i++) {
            subExperiment.A[i] = copyMatrix.A[features[i]];
        }
        subExperiment = subExperiment.transpose();
        return subExperiment;
    }

    /**

     * Checking the result of hcl algorithm calculation.

     * @throws AlgorithmException, if the result is incorrect.

     */
    private void validate(AlgorithmData result) throws AlgorithmException {
        if (result.getIntArray("child-1-array") == null) {
            throw new AlgorithmException("parameter 'child-1-array' is null");
        }
        if (result.getIntArray("child-2-array") == null) {
            throw new AlgorithmException("parameter 'child-2-array' is null");
        }
        if (result.getIntArray("node-order") == null) {
            throw new AlgorithmException("parameter 'node-order' is null");
        }
        if (result.getMatrix("height") == null) {
            throw new AlgorithmException("parameter 'height' is null");
        }
    }

    private int[] convert2int(Vector source) {
        int[] int_matrix = new int[source.size()];
        for (int i = 0; i < int_matrix.length; i++) {
            int_matrix[i] = (int) ((Integer) source.get(i)).intValue();
        }
        return int_matrix;
    }

    private FloatMatrix getMeans(Vector[] clusters) {
        FloatMatrix means = new FloatMatrix(clusters.length, number_of_samples);
        FloatMatrix mean;
        for (int i = 0; i < clusters.length; i++) {
            int n = clusters[i].size();
            for (int j = 0; j < number_of_samples; j++) {
                float currentMean = 0f;
                int denom = 0;
                for (int k = 0; k < n; k++) {
                    float value = expMatrix.get(((Integer) clusters[i].get(k)).intValue(), j);
                    if (!Float.isNaN(value)) {
                        currentMean += value;
                        denom++;
                    }
                }
                means.set(i, j, currentMean / denom);
            }
        }
        return means;
    }

    private FloatMatrix getVariances(Vector[] clusters, FloatMatrix means) {
        final int rows = means.getRowDimension();
        final int columns = means.getColumnDimension();
        FloatMatrix variances = new FloatMatrix(rows, columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float mean = means.get(row, column);
                int validN = 0;
                int size = clusters[row].size();
                float sum = 0f;
                float value;
                for (int i = 0; i < size; i++) {
                    value = expMatrix.get(((Integer) clusters[row].get(i)).intValue(), column);
                    if (!Float.isNaN(value)) {
                        float diff = value - mean;
                        sum += diff * diff;
                        validN++;
                    }
                }
                variances.set(row, column, (float) Math.sqrt(sum) / (validN - 1));
            }
        }
        return variances;
    }

    public boolean isAborted() {
        return stop;
    }

    synchronized float getAdjustedDiameter() {
        if ((function == Algorithm.EUCLIDEAN) || (function == Algorithm.MANHATTAN)) {
            return (float) (diameter * getMaximumDistance());
        } else if ((!useAbsolute) && (function == Algorithm.PEARSON) || (function == Algorithm.SPEARMANRANK) || (function == Algorithm.KENDALLSTAU) || (function == Algorithm.MUTUALINFORMATION) || (function == Algorithm.PEARSONUNCENTERED) || (function == Algorithm.COSINE)) {
            return 2 * diameter;
        }
        return diameter;
    }

    private double getMaximumDistance() {
        double maxDistance = Double.NEGATIVE_INFINITY;
        double currentDistance;
        for (int i = 0; i < number_of_genes; i++) {
            for (int j = 0; j < number_of_genes; j++) {
                if (j != i) {
                    currentDistance = getDistance(expMatrix, i, j);
                    if (maxDistance < currentDistance) maxDistance = currentDistance;
                }
            }
        }
        return maxDistance;
    }

    private double getDistance(FloatMatrix expMatrix, int gene1, int gene2) {
        double distance = 0;
        double x;
        if (function == Algorithm.PEARSON) {
            distance = ExperimentUtil.genePearson(expMatrix, null, gene1, gene2, factor);
        } else if (function == Algorithm.SPEARMANRANK) {
            distance = ExperimentUtil.geneSpearmanRank(expMatrix, null, gene1, gene2, factor);
        } else if (function == Algorithm.KENDALLSTAU) {
            distance = ExperimentUtil.geneKendallsTau(expMatrix, null, gene1, gene2, factor);
        } else if (function == Algorithm.COSINE) {
            distance = ExperimentUtil.geneCosine(expMatrix, null, gene1, gene2, factor);
        } else if (function == Algorithm.MUTUALINFORMATION) {
            distance = ExperimentUtil.geneMutualInformation(expMatrix, null, gene1, gene2, factor);
        } else if (function == Algorithm.PEARSONUNCENTERED) {
            distance = ExperimentUtil.genePearsonUncentered(expMatrix, null, gene1, gene2, factor);
        } else if (function == Algorithm.PEARSONSQARED) {
            float temp = ExperimentUtil.genePearson(expMatrix, null, gene1, gene2, factor);
            distance = temp * temp;
        } else if ((function == Algorithm.EUCLIDEAN) || (function == Algorithm.MANHATTAN)) {
            distance = ExperimentUtil.geneDistance(expMatrix, null, gene1, gene2, function, factor, useAbsolute);
        } else if (function == Algorithm.COVARIANCE) {
            x = ExperimentUtil.geneCovariance(expMatrix, null, gene1, gene2, factor);
            if (useAbsolute) {
                x = Math.abs(x);
            }
            distance = (xMax - x) / (xMax - xMin);
        } else if (function == Algorithm.DOTPRODUCT) {
            x = ExperimentUtil.geneDotProduct(expMatrix, null, gene1, gene2, factor);
            if (useAbsolute) {
                x = Math.abs(x);
            }
            distance = (xMax - x) / (xMax - xMin);
        }
        if ((function == Algorithm.PEARSON) || (function == Algorithm.SPEARMANRANK) || (function == Algorithm.KENDALLSTAU) || (function == Algorithm.COSINE) || (function == Algorithm.MUTUALINFORMATION) || (function == Algorithm.PEARSONUNCENTERED) || (function == Algorithm.PEARSONSQARED)) {
            if (useAbsolute) {
                distance = 1 - Math.abs(distance);
            } else {
                distance = 1 - distance;
            }
        }
        return distance;
    }

    private double getMaxCovarOrDotProd() {
        double xMax = Double.NEGATIVE_INFINITY;
        double xCurrent = Double.NEGATIVE_INFINITY;
        ;
        for (int i = 0; i < number_of_genes; i++) {
            for (int j = 0; j < number_of_genes; j++) {
                if (j != i) {
                    if (function == Algorithm.DOTPRODUCT) {
                        xCurrent = ExperimentUtil.geneDotProduct(expMatrix, null, i, j, factor);
                        if (useAbsolute) {
                            xCurrent = Math.abs(xCurrent);
                        }
                    } else if (function == Algorithm.COVARIANCE) {
                        xCurrent = ExperimentUtil.geneCovariance(expMatrix, null, i, j, factor);
                        if (useAbsolute) {
                            xCurrent = Math.abs(xCurrent);
                        }
                    }
                    if (xMax < xCurrent) xMax = xCurrent;
                }
            }
        }
        return xMax;
    }

    private double getMinCovOrDotProd() {
        double xMin = Double.POSITIVE_INFINITY;
        double xCurrent = Double.POSITIVE_INFINITY;
        for (int i = 0; i < number_of_genes; i++) {
            for (int j = 0; j < number_of_genes; j++) {
                if (j != i) {
                    if (function == Algorithm.DOTPRODUCT) {
                        xCurrent = ExperimentUtil.geneDotProduct(expMatrix, null, i, j, factor);
                        if (useAbsolute) {
                            xCurrent = Math.abs(xCurrent);
                        }
                    } else if (function == Algorithm.COVARIANCE) {
                        xCurrent = ExperimentUtil.geneCovariance(expMatrix, null, i, j, factor);
                        if (useAbsolute) {
                            xCurrent = Math.abs(xCurrent);
                        }
                    }
                    if (xMin > xCurrent) xMin = xCurrent;
                }
            }
        }
        return xMin;
    }

    public void setMinMaxCovOrDotProd() {
        this.xMax = getMaxCovarOrDotProd();
        this.xMin = getMinCovOrDotProd();
    }

    private Vector getAllClusters(Vector unassignedUniqueIDIndices) {
        if (stop) {
            return null;
        }
        while (true) {
            Vector currentLargestCluster = getLargestCluster(unassignedUniqueIDIndices);
            allClusters.add(currentLargestCluster);
            int clusterSize = currentLargestCluster.size();
            progress.setMessage(4, "# of assigned genes: " + (number_of_genes - unassignedUniqueIDIndices.size()));
            progress.setMessage(5, "# of genes not yet assigned: " + unassignedUniqueIDIndices.size());
            progress.setMessage(2, "# of clusters formed: " + allClusters.size());
            progress.setMessage(3, "size of last cluster formed: " + currentLargestCluster.size());
            unassignedUniqueIDIndices.removeAll(currentLargestCluster);
            if (clusterSize < minimumClusterSize) {
                currentLargestCluster.addAll(unassignedUniqueIDIndices);
                return allClusters;
            } else if (unassignedUniqueIDIndices.size() == 0) {
                allClusters.add(new Vector());
                return allClusters;
            }
        }
    }

    /**

      * This returns the cluster that a gene would produce. This has specific permission to

     * mangle the vector of indices passed to it.

     *

     * @param candidateIndices does NOT contain an entry for seedIndex.

     */
    private Vector getClusterForAGene(Integer seedIndex, Vector candidateIndices) {
        Vector cluster = new Vector();
        cluster.add(seedIndex);
        Integer MostRecentAdditionI = seedIndex;
        int mostRecentAdditioni = seedIndex.intValue();
        float[] geneDiameterSoFar = new float[number_of_genes];
        for (int local = 0; local < candidateIndices.size(); local++) geneDiameterSoFar[((Integer) candidateIndices.get(local)).intValue()] = Float.NEGATIVE_INFINITY;
        while (true) {
            int bestLocalIndex = -1;
            float bestDistance = Float.POSITIVE_INFINITY;
            CANDIDATE_SEARCH: for (int local = 0; local < candidateIndices.size(); ) {
                int i = ((Integer) candidateIndices.get(local)).intValue();
                geneDiameterSoFar[i] = Math.max(proximity[i][mostRecentAdditioni], geneDiameterSoFar[i]);
                if (geneDiameterSoFar[i] > adjustedDiameter) {
                    candidateIndices.remove(local);
                    continue CANDIDATE_SEARCH;
                }
                if (geneDiameterSoFar[i] < bestDistance) {
                    bestDistance = geneDiameterSoFar[i];
                    bestLocalIndex = local;
                }
                local++;
            }
            if (bestLocalIndex == -1) break;
            MostRecentAdditionI = (Integer) candidateIndices.remove(bestLocalIndex);
            mostRecentAdditioni = MostRecentAdditionI.intValue();
            cluster.add(MostRecentAdditionI);
        }
        return cluster;
    }

    private double getJackknifeDistance(int gene1, int gene2) {
        JackknifedMatrixBySpecifiedExp jackMatrix = null;
        double jackknifeDistance, currentDistance;
        jackknifeDistance = Math.abs(getDistance(expMatrix, gene1, gene2));
        for (int i = 0; i < number_of_samples; i++) {
            jackMatrix = jacked[i];
            currentDistance = getDistance(jackMatrix, gene1, gene2);
            if (useAbsolute == true) {
                jackknifeDistance = Math.max(jackknifeDistance, Math.abs(currentDistance));
            } else {
                jackknifeDistance = Math.max(jackknifeDistance, currentDistance);
            }
        }
        return jackknifeDistance;
    }

    private Vector getLargestCluster(Vector unassignedUIDIndices) {
        Vector currentCluster;
        Vector largestClusterTies = new Vector();
        int largestClusterSize = 0;
        for (int i = 0; i < unassignedUIDIndices.size(); i++) {
            Vector tempUnassigned = (Vector) unassignedUIDIndices.clone();
            Integer seedCandidate = (Integer) tempUnassigned.remove(i);
            currentCluster = getClusterForAGene(seedCandidate, tempUnassigned);
            if (currentCluster.size() == largestClusterSize) {
                largestClusterTies.add(currentCluster);
            } else if (currentCluster.size() > largestClusterSize) {
                largestClusterTies.clear();
                largestClusterTies.add(currentCluster);
                largestClusterSize = currentCluster.size();
            }
        }
        int randCluster = (int) (Math.random() * largestClusterTies.size());
        return (Vector) largestClusterTies.get(randCluster);
    }

    public void abort() {
        stop = true;
    }

    private Vector[] calculate(boolean useAbsolute, float diameter, int minimumClusterSize) throws AlgorithmException {
        long startTime, calculationTime;
        Vector[] clusters;
        if (stop) return null;
        if ((function == Algorithm.COVARIANCE) || (function == Algorithm.DOTPRODUCT)) {
            setMinMaxCovOrDotProd();
        }
        progress.setMessage(0, "<html>" + "<p>Distance: " + AbstractAlgorithm.getDistanceName(function) + "<p>Absolute? " + ((useAbsolute == true) ? "Yes" : "No") + "<p>Minimum cluster size: " + minimumClusterSize + "<p>Threshold diameter: " + diameter + "</html>");
        progress.setTimerLabel(1, "Running for ", " seconds.", 1000);
        progress.setMessage(2, "# of clusters formed: 0");
        progress.setMessage(3, "size of last cluster formed: 0");
        progress.setMessage(4, "# of assigned genes: 0");
        progress.setMessage(5, "# of genes not yet assigned: " + number_of_genes);
        progress.setVisible(true);
        if (stop) return null;
        this.adjustedDiameter = getAdjustedDiameter();
        this.jacked = new JackknifedMatrixBySpecifiedExp[number_of_samples];
        for (int i = 0; i < jacked.length; i++) {
            jacked[i] = new JackknifedMatrixBySpecifiedExp(expMatrix, i);
        }
        this.proximity = new float[number_of_genes][number_of_genes];
        for (int i = 0; i < number_of_genes; i++) {
            for (int j = 0; j <= i; j++) {
                proximity[i][j] = (float) getJackknifeDistance(i, j);
                proximity[j][i] = proximity[i][j];
            }
        }
        this.jacked = null;
        Vector allUniqueIDIndices = new Vector();
        Vector clusterVector = new Vector();
        allUniqueIDIndices = new Vector();
        for (int i = 0; i < number_of_genes; i++) {
            allUniqueIDIndices.add(new Integer(i));
        }
        if (stop) return null;
        startTime = System.currentTimeMillis();
        clusterVector = getAllClusters(allUniqueIDIndices);
        if (stop) return null;
        progress.dismiss();
        calculationTime = System.currentTimeMillis() - startTime;
        clusters = (Vector[]) clusterVector.toArray(new Vector[clusterVector.size()]);
        return clusters;
    }

    private class JackknifedMatrixBySpecifiedExp extends FloatMatrix {

        FloatMatrix origMatrix;

        int removedExperiment;

        public JackknifedMatrixBySpecifiedExp(FloatMatrix origMatrix, int removedExperiment) {
            super(origMatrix.m, origMatrix.n - 1);
            this.origMatrix = origMatrix;
            this.removedExperiment = removedExperiment;
            int jackMatrixExpIndex;
            for (int i = 0; i < m; i++) {
                jackMatrixExpIndex = 0;
                for (int j = 0; j < origMatrix.n; j++) {
                    if (j != removedExperiment) {
                        A[i][jackMatrixExpIndex] = origMatrix.A[i][j];
                        jackMatrixExpIndex++;
                    }
                }
            }
        }
    }
}
