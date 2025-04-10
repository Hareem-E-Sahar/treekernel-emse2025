package netkit.graph.edgecreator;

import netkit.graph.*;
import netkit.classifiers.DataSplit;
import netkit.util.VectorMath;
import java.util.*;

public class MahalanobisDistanceEdgeCreator extends EdgeCreatorImp {

    private double[][] cm = null;

    private double[] srcInstance = null;

    private double[] destInstance = null;

    private double[] count = null;

    private double[] mean = null;

    private int[] attrDim = null;

    cern.colt.matrix.linalg.Algebra alg = new cern.colt.matrix.linalg.Algebra();

    @Override
    public String getName() {
        return "mahalanobisDistanceEdgeCreator";
    }

    @Override
    public boolean isByAttribute() {
        return false;
    }

    @Override
    public boolean canHandle(Attribute attribute) {
        return false;
    }

    @Override
    public double getWeight(Node src, Node dest) {
        if (cm == null) throw new IllegalStateException("EdgeCreator[" + getName() + "] has not yet been initialized!");
        fillInstance(src, srcInstance, mean);
        fillInstance(dest, destInstance, mean);
        VectorMath.subtract(srcInstance, destInstance);
        Arrays.fill(destInstance, 0.0D);
        for (int i = 0; i < srcInstance.length; i++) for (int j = 0; j < srcInstance.length; j++) destInstance[i] += srcInstance[j] * cm[i][j];
        return VectorMath.dotproduct(srcInstance, destInstance);
    }

    private void fillInstance(Node n, double[] instance, double[] defvals) {
        double[] vals = n.getValues();
        Arrays.fill(instance, 0.0D);
        for (int i = 0, j = 0; i < vals.length; i++) {
            if (attrDim[i] == 0) continue;
            if (attrDim[i] == 1) {
                if (n.isMissing(i)) instance[j] = defvals[j]; else {
                    instance[j] = vals[i];
                    count[j]++;
                }
                j++;
            } else {
                if (n.isMissing(i)) {
                    for (int k = 0; k < attrDim[i]; k++, j++) instance[j] = defvals[j];
                } else {
                    int iVal = j + (int) vals[i];
                    instance[iVal] = 1;
                    for (int k = 0; k < attrDim[i]; k++, j++) count[j]++;
                }
            }
        }
    }

    private void computeStatistics(final DataSplit split) {
        Attributes as = graph.getAttributes(nodeType);
        int idx = split.getView().getAttributeIndex();
        attrDim = new int[as.attributeCount()];
        Arrays.fill(attrDim, 0);
        for (int i = 0; i < as.attributeCount(); i++) {
            if (i == as.getKeyIndex() || i == idx) continue;
            Attribute a = as.getAttribute(i);
            if (a.getType() == Type.CATEGORICAL) attrDim[i] = ((AttributeCategorical) a).getTokens().length; else attrDim[i] = 1;
        }
        int dim = VectorMath.sum(attrDim);
        srcInstance = new double[dim];
        destInstance = new double[dim];
        mean = new double[dim];
        count = new double[dim];
        double[] zeroes = new double[dim];
        Arrays.fill(zeroes, 0.0D);
        Arrays.fill(mean, 0.0D);
        Arrays.fill(count, 0.0D);
        double numNodes = 0;
        for (Node n : split.getTrainSet()) {
            if (n.isMissing(idx)) return;
            numNodes++;
            fillInstance(n, srcInstance, zeroes);
            VectorMath.add(mean, srcInstance);
        }
        for (int i = 0; i < count.length; i++) {
            if (count[i] == 0) continue;
            mean[i] /= count[i];
        }
        cm = new double[dim][dim];
        for (double[] row : cm) Arrays.fill(row, 0.0D);
        for (Node n : split.getTrainSet()) {
            if (n.isMissing(idx)) return;
            Arrays.fill(count, 0);
            fillInstance(n, srcInstance, mean);
            for (int i = 0; i < count.length; i++) {
                double x = srcInstance[i] - mean[i];
                for (int j = i; j < count.length; j++) {
                    double y = srcInstance[j] - mean[j];
                    cm[i][j] += x * y;
                }
            }
        }
        if (numNodes > 0) {
            for (int i = 0; i < count.length; i++) {
                for (int j = i; j < count.length; j++) {
                    cm[i][j] /= numNodes;
                    cm[j][i] = cm[i][j];
                }
            }
        }
        cern.colt.matrix.DoubleMatrix2D coltCM = new cern.colt.matrix.impl.DenseDoubleMatrix2D(dim, dim);
        coltCM.assign(cm);
        coltCM = alg.inverse(coltCM);
        cm = coltCM.toArray();
    }

    @Override
    public void buildModel(final DataSplit split) {
        edges = null;
        super.buildModel(split);
        computeStatistics(split);
    }
}
