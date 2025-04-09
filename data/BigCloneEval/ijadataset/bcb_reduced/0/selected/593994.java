package moa.classifiers;

import weka.core.Utils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import moa.AbstractMOAObject;
import moa.core.DoubleVector;

/**
 * Class for observing the class data distribution for a numeric attribute as in VFML.
 * Used in naive Bayes and decision trees to monitor data statistics on leaves.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public class VFMLNumericAttributeClassObserver extends AbstractMOAObject implements AttributeClassObserver {

    private static final long serialVersionUID = 1L;

    protected class Bin implements Serializable {

        private static final long serialVersionUID = 1L;

        public double lowerBound, upperBound;

        public DoubleVector classWeights = new DoubleVector();

        public int boundaryClass;

        public double boundaryWeight;
    }

    protected List<Bin> binList = new ArrayList<Bin>();

    protected int numBins;

    public VFMLNumericAttributeClassObserver(int numBins) {
        this.numBins = numBins;
    }

    @Override
    public void observeAttributeClass(double attVal, int classVal, double weight) {
        if (Utils.isMissingValue(attVal)) {
        } else {
            if (this.binList.size() < 1) {
                Bin newBin = new Bin();
                newBin.classWeights.addToValue(classVal, weight);
                newBin.boundaryClass = classVal;
                newBin.boundaryWeight = weight;
                newBin.upperBound = attVal;
                newBin.lowerBound = attVal;
                this.binList.add(newBin);
            } else {
                int index = -1;
                boolean found = false;
                int min = 0;
                int max = this.binList.size() - 1;
                index = 0;
                while ((min <= max) && !found) {
                    int i = (min + max) / 2;
                    Bin bin = this.binList.get(i);
                    if (((attVal >= bin.lowerBound) && (attVal < bin.upperBound)) || ((i == this.binList.size() - 1) && (attVal >= bin.lowerBound) && (attVal <= bin.upperBound))) {
                        found = true;
                        index = i;
                    } else if (attVal < bin.lowerBound) {
                        max = i - 1;
                    } else {
                        min = i + 1;
                    }
                }
                boolean first = false;
                boolean last = false;
                if (!found) {
                    Bin bin = this.binList.get(0);
                    if (bin.lowerBound > attVal) {
                        index = 0;
                        first = true;
                    } else {
                        index = this.binList.size() - 1;
                        last = true;
                    }
                }
                Bin bin = this.binList.get(index);
                if ((bin.lowerBound == attVal) || (this.binList.size() >= this.numBins)) {
                    bin.classWeights.addToValue(classVal, weight);
                    if ((bin.boundaryClass == classVal) && (bin.lowerBound == attVal)) {
                        bin.boundaryWeight += weight;
                    }
                } else {
                    Bin newBin = new Bin();
                    newBin.classWeights.addToValue(classVal, weight);
                    newBin.boundaryWeight = weight;
                    newBin.boundaryClass = classVal;
                    newBin.upperBound = bin.upperBound;
                    newBin.lowerBound = attVal;
                    double percent = 0.0;
                    if (!((bin.upperBound - bin.lowerBound == 0) || last || first)) {
                        percent = 1.0 - ((attVal - bin.lowerBound) / (bin.upperBound - bin.lowerBound));
                    }
                    bin.classWeights.addToValue(bin.boundaryClass, -bin.boundaryWeight);
                    DoubleVector weightToShift = new DoubleVector(bin.classWeights);
                    weightToShift.scaleValues(percent);
                    newBin.classWeights.addValues(weightToShift);
                    bin.classWeights.subtractValues(weightToShift);
                    bin.classWeights.addToValue(bin.boundaryClass, bin.boundaryWeight);
                    if (last) {
                        bin.upperBound = attVal;
                        newBin.upperBound = attVal;
                        this.binList.add(newBin);
                    } else if (first) {
                        newBin.upperBound = bin.lowerBound;
                        this.binList.add(0, newBin);
                    } else {
                        newBin.upperBound = bin.upperBound;
                        bin.upperBound = attVal;
                        this.binList.add(index + 1, newBin);
                    }
                }
            }
        }
    }

    @Override
    public double probabilityOfAttributeValueGivenClass(double attVal, int classVal) {
        return 0.0;
    }

    @Override
    public AttributeSplitSuggestion getBestEvaluatedSplitSuggestion(SplitCriterion criterion, double[] preSplitDist, int attIndex, boolean binaryOnly) {
        AttributeSplitSuggestion bestSuggestion = null;
        DoubleVector rightDist = new DoubleVector();
        for (Bin bin : this.binList) {
            rightDist.addValues(bin.classWeights);
        }
        DoubleVector leftDist = new DoubleVector();
        for (Bin bin : this.binList) {
            leftDist.addValues(bin.classWeights);
            rightDist.subtractValues(bin.classWeights);
            double[][] postSplitDists = new double[][] { leftDist.getArrayCopy(), rightDist.getArrayCopy() };
            double merit = criterion.getMeritOfSplit(preSplitDist, postSplitDists);
            if ((bestSuggestion == null) || (merit > bestSuggestion.merit)) {
                bestSuggestion = new AttributeSplitSuggestion(new NumericAttributeBinaryTest(attIndex, bin.upperBound, false), postSplitDists, merit);
            }
        }
        return bestSuggestion;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
    }
}
