package reconcile.weka.classifiers.evaluation;

import reconcile.weka.classifiers.Classifier;
import reconcile.weka.core.Attribute;
import reconcile.weka.core.FastVector;
import reconcile.weka.core.Instance;
import reconcile.weka.core.Instances;
import reconcile.weka.core.Utils;

/**
 * Generates points illustrating prediction tradeoffs that can be obtained
 * by varying the threshold value between classes. For example, the typical 
 * threshold value of 0.5 means the predicted probability of "positive" must be
 * higher than 0.5 for the instance to be predicted as "positive". The 
 * resulting dataset can be used to visualize precision/recall tradeoff, or 
 * for ROC curve analysis (true positive rate vs false positive rate).
 *
 * @author Len Trigg (len@reeltwo.com)
 * @version $Revision: 1.1 $
 */
public class ThresholdCurve {

    /** The name of the relation used in threshold curve datasets */
    public static final String RELATION_NAME = "ThresholdCurve";

    public static final String TRUE_POS_NAME = "True Positives";

    public static final String FALSE_NEG_NAME = "False Negatives";

    public static final String FALSE_POS_NAME = "False Positives";

    public static final String TRUE_NEG_NAME = "True Negatives";

    public static final String FP_RATE_NAME = "False Positive Rate";

    public static final String TP_RATE_NAME = "True Positive Rate";

    public static final String PRECISION_NAME = "Precision";

    public static final String RECALL_NAME = "Recall";

    public static final String FALLOUT_NAME = "Fallout";

    public static final String FMEASURE_NAME = "FMeasure";

    public static final String THRESHOLD_NAME = "Threshold";

    /**
   * Calculates the performance stats for the default class and return 
   * results as a set of Instances. The
   * structure of these Instances is as follows:<p> <ul> 
   * <li> <b>True Positives </b>
   * <li> <b>False Negatives</b>
   * <li> <b>False Positives</b>
   * <li> <b>True Negatives</b>
   * <li> <b>False Positive Rate</b>
   * <li> <b>True Positive Rate</b>
   * <li> <b>Precision</b>
   * <li> <b>Recall</b>  
   * <li> <b>Fallout</b>  
   * <li> <b>Threshold</b> contains the probability threshold that gives
   * rise to the previous performance values. 
   * </ul> <p>
   * For the definitions of these measures, see TwoClassStats <p>
   *
   * @see TwoClassStats
   * @param classIndex index of the class of interest.
   * @return datapoints as a set of instances, null if no predictions
   * have been made.
   */
    public Instances getCurve(FastVector predictions) {
        if (predictions.size() == 0) {
            return null;
        }
        return getCurve(predictions, ((NominalPrediction) predictions.elementAt(0)).distribution().length - 1);
    }

    /**
   * Calculates the performance stats for the desired class and return 
   * results as a set of Instances.
   *
   * @param classIndex index of the class of interest.
   * @return datapoints as a set of instances.
   */
    public Instances getCurve(FastVector predictions, int classIndex) {
        if ((predictions.size() == 0) || (((NominalPrediction) predictions.elementAt(0)).distribution().length <= classIndex)) {
            return null;
        }
        double totPos = 0, totNeg = 0;
        double[] probs = getProbabilities(predictions, classIndex);
        for (int i = 0; i < probs.length; i++) {
            NominalPrediction pred = (NominalPrediction) predictions.elementAt(i);
            if (pred.actual() == Prediction.MISSING_VALUE) {
                System.err.println(getClass().getName() + " Skipping prediction with missing class value");
                continue;
            }
            if (pred.weight() < 0) {
                System.err.println(getClass().getName() + " Skipping prediction with negative weight");
                continue;
            }
            if (pred.actual() == classIndex) {
                totPos += pred.weight();
            } else {
                totNeg += pred.weight();
            }
        }
        Instances insts = makeHeader();
        int[] sorted = Utils.sort(probs);
        TwoClassStats tc = new TwoClassStats(totPos, totNeg, 0, 0);
        double threshold = 0;
        double cumulativePos = 0;
        double cumulativeNeg = 0;
        for (int i = 0; i < sorted.length; i++) {
            if ((i == 0) || (probs[sorted[i]] > threshold)) {
                tc.setTruePositive(tc.getTruePositive() - cumulativePos);
                tc.setFalseNegative(tc.getFalseNegative() + cumulativePos);
                tc.setFalsePositive(tc.getFalsePositive() - cumulativeNeg);
                tc.setTrueNegative(tc.getTrueNegative() + cumulativeNeg);
                threshold = probs[sorted[i]];
                insts.add(makeInstance(tc, threshold));
                cumulativePos = 0;
                cumulativeNeg = 0;
                if (i == sorted.length - 1) {
                    break;
                }
            }
            NominalPrediction pred = (NominalPrediction) predictions.elementAt(sorted[i]);
            if (pred.actual() == Prediction.MISSING_VALUE) {
                System.err.println(getClass().getName() + " Skipping prediction with missing class value");
                continue;
            }
            if (pred.weight() < 0) {
                System.err.println(getClass().getName() + " Skipping prediction with negative weight");
                continue;
            }
            if (pred.actual() == classIndex) {
                cumulativePos += pred.weight();
            } else {
                cumulativeNeg += pred.weight();
            }
        }
        return insts;
    }

    /**
   * Calculates the n point precision result, which is the precision averaged
   * over n evenly spaced (w.r.t recall) samples of the curve.
   *
   * @param tcurve a previously extracted threshold curve Instances.
   * @param n the number of points to average over.
   * @return the n-point precision.
   */
    public static double getNPointPrecision(Instances tcurve, int n) {
        if (!RELATION_NAME.equals(tcurve.relationName()) || (tcurve.numInstances() == 0)) {
            return Double.NaN;
        }
        int recallInd = tcurve.attribute(RECALL_NAME).index();
        int precisInd = tcurve.attribute(PRECISION_NAME).index();
        double[] recallVals = tcurve.attributeToDoubleArray(recallInd);
        int[] sorted = Utils.sort(recallVals);
        double isize = 1.0 / (n - 1);
        double psum = 0;
        for (int i = 0; i < n; i++) {
            int pos = binarySearch(sorted, recallVals, i * isize);
            double recall = recallVals[sorted[pos]];
            double precis = tcurve.instance(sorted[pos]).value(precisInd);
            while ((pos != 0) && (pos < sorted.length - 1)) {
                pos++;
                double recall2 = recallVals[sorted[pos]];
                if (recall2 != recall) {
                    double precis2 = tcurve.instance(sorted[pos]).value(precisInd);
                    double slope = (precis2 - precis) / (recall2 - recall);
                    double offset = precis - recall * slope;
                    precis = isize * i * slope + offset;
                    break;
                }
            }
            psum += precis;
        }
        return psum / n;
    }

    /**
   * Calculates the area under the ROC curve.  This is normalised so
   * that 0.5 is random, 1.0 is perfect and 0.0 is bizarre.
   *
   * @param tcurve a previously extracted threshold curve Instances.
   * @return the ROC area, or Double.NaN if you don't pass in 
   * a ThresholdCurve generated Instances. 
   */
    public static double getROCArea(Instances tcurve) {
        final int n = tcurve.numInstances();
        if (!RELATION_NAME.equals(tcurve.relationName()) || (n == 0)) {
            return Double.NaN;
        }
        final int tpInd = tcurve.attribute(TRUE_POS_NAME).index();
        final int fpInd = tcurve.attribute(FALSE_POS_NAME).index();
        final double[] tpVals = tcurve.attributeToDoubleArray(tpInd);
        final double[] fpVals = tcurve.attributeToDoubleArray(fpInd);
        final double tp0 = tpVals[0];
        final double fp0 = fpVals[0];
        double area = 0.0;
        double xlast = 1.0;
        double ylast = 1.0;
        for (int i = 1; i < n; i++) {
            final double x = fpVals[i] / fp0;
            final double y = tpVals[i] / tp0;
            final double areaDelta = (y + ylast) * (xlast - x) / 2.0;
            area += areaDelta;
            xlast = x;
            ylast = y;
        }
        if (xlast > 0.0) {
            final double areaDelta = ylast * xlast / 2.0;
            area += areaDelta;
        }
        return area;
    }

    /**
   * Gets the index of the instance with the closest threshold value to the
   * desired target
   *
   * @param tcurve a set of instances that have been generated by this class
   * @param threshold the target threshold
   * @return the index of the instance that has threshold closest to
   * the target, or -1 if this could not be found (i.e. no data, or
   * bad threshold target)
   */
    public static int getThresholdInstance(Instances tcurve, double threshold) {
        if (!RELATION_NAME.equals(tcurve.relationName()) || (tcurve.numInstances() == 0) || (threshold < 0) || (threshold > 1.0)) {
            return -1;
        }
        if (tcurve.numInstances() == 1) {
            return 0;
        }
        double[] tvals = tcurve.attributeToDoubleArray(tcurve.numAttributes() - 1);
        int[] sorted = Utils.sort(tvals);
        return binarySearch(sorted, tvals, threshold);
    }

    private static int binarySearch(int[] index, double[] vals, double target) {
        int lo = 0, hi = index.length - 1;
        while (hi - lo > 1) {
            int mid = lo + (hi - lo) / 2;
            double midval = vals[index[mid]];
            if (target > midval) {
                lo = mid;
            } else if (target < midval) {
                hi = mid;
            } else {
                while ((mid > 0) && (vals[index[mid - 1]] == target)) {
                    mid--;
                }
                return mid;
            }
        }
        return lo;
    }

    private double[] getProbabilities(FastVector predictions, int classIndex) {
        double[] probs = new double[predictions.size()];
        for (int i = 0; i < probs.length; i++) {
            NominalPrediction pred = (NominalPrediction) predictions.elementAt(i);
            probs[i] = pred.distribution()[classIndex];
        }
        return probs;
    }

    private Instances makeHeader() {
        FastVector fv = new FastVector();
        fv.addElement(new Attribute(TRUE_POS_NAME));
        fv.addElement(new Attribute(FALSE_NEG_NAME));
        fv.addElement(new Attribute(FALSE_POS_NAME));
        fv.addElement(new Attribute(TRUE_NEG_NAME));
        fv.addElement(new Attribute(FP_RATE_NAME));
        fv.addElement(new Attribute(TP_RATE_NAME));
        fv.addElement(new Attribute(PRECISION_NAME));
        fv.addElement(new Attribute(RECALL_NAME));
        fv.addElement(new Attribute(FALLOUT_NAME));
        fv.addElement(new Attribute(FMEASURE_NAME));
        fv.addElement(new Attribute(THRESHOLD_NAME));
        return new Instances(RELATION_NAME, fv, 100);
    }

    private Instance makeInstance(TwoClassStats tc, double prob) {
        int count = 0;
        float[] vals = new float[11];
        vals[count++] = (float) tc.getTruePositive();
        vals[count++] = (float) tc.getFalseNegative();
        vals[count++] = (float) tc.getFalsePositive();
        vals[count++] = (float) tc.getTrueNegative();
        vals[count++] = (float) tc.getFalsePositiveRate();
        vals[count++] = (float) tc.getTruePositiveRate();
        vals[count++] = (float) tc.getPrecision();
        vals[count++] = (float) tc.getRecall();
        vals[count++] = (float) tc.getFallout();
        vals[count++] = (float) tc.getFMeasure();
        vals[count++] = (float) prob;
        return new Instance(1, vals);
    }

    /**
   * Tests the ThresholdCurve generation from the command line.
   * The classifier is currently hardcoded. Pipe in an arff file.
   *
   * @param args currently ignored
   */
    public static void main(String[] args) {
        try {
            Instances inst = new Instances(new java.io.InputStreamReader(System.in));
            if (false) {
                System.out.println(ThresholdCurve.getNPointPrecision(inst, 11));
            } else {
                inst.setClassIndex(inst.numAttributes() - 1);
                ThresholdCurve tc = new ThresholdCurve();
                EvaluationUtils eu = new EvaluationUtils();
                Classifier classifier = new reconcile.weka.classifiers.functions.Logistic();
                FastVector predictions = new FastVector();
                for (int i = 0; i < 2; i++) {
                    eu.setSeed(i);
                    predictions.appendElements(eu.getCVPredictions(classifier, inst, 10));
                }
                Instances result = tc.getCurve(predictions);
                System.out.println(result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
