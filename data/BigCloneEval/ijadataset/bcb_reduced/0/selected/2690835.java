package org.galagosearch.core.eval;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.galagosearch.core.eval.stat.Stat;

/**
 *
 * @author trevor
 */
public class SetRetrievalComparator {

    double[] baseline;

    double[] treatment;

    /** Creates a new instance of SetRetrievalComparator */
    public SetRetrievalComparator(Map<String, Double> baseline, Map<String, Double> treatment) {
        Set<String> commonQueries = new TreeSet<String>(baseline.keySet());
        commonQueries.retainAll(treatment.keySet());
        this.baseline = new double[commonQueries.size()];
        this.treatment = new double[commonQueries.size()];
        int i = 0;
        for (String key : commonQueries) {
            this.baseline[i] = baseline.get(key);
            this.treatment[i] = treatment.get(key);
            i++;
        }
    }

    private double[] multiply(double[] numbers, double boost) {
        double[] result = new double[numbers.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = numbers[i] * boost;
        }
        return result;
    }

    private double mean(double[] numbers) {
        double sum = 0;
        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i];
        }
        return sum / (double) numbers.length;
    }

    public double meanBaselineMetric() {
        return mean(baseline);
    }

    public double meanTreatmentMetric() {
        return mean(treatment);
    }

    public int countTreatmentBetter() {
        int better = 0;
        for (int i = 0; i < baseline.length; i++) {
            if (baseline[i] < treatment[i]) {
                better++;
            }
        }
        return better;
    }

    public int countBaselineBetter() {
        int better = 0;
        for (int i = 0; i < baseline.length; i++) {
            if (baseline[i] > treatment[i]) {
                better++;
            }
        }
        return better;
    }

    public int countEqual() {
        int same = 0;
        for (int i = 0; i < baseline.length; i++) {
            if (baseline[i] == treatment[i]) {
                same++;
            }
        }
        return same;
    }

    public double supportedHypothesis(String testName, double pvalue) {
        double currentBoost = 1.0;
        double currentPvalue = test(testName, currentBoost);
        double lastBoost = 1.0;
        double lastPvalue = currentPvalue;
        int iterations = 0;
        while ((lastPvalue < pvalue) == (currentPvalue < pvalue)) {
            double nextBoost = currentBoost;
            if (currentPvalue < pvalue) {
                nextBoost *= 1.05;
            } else if (currentPvalue > pvalue) {
                nextBoost *= 0.95;
            }
            double nextPvalue = test(testName, nextBoost);
            lastBoost = currentBoost;
            lastPvalue = currentPvalue;
            currentBoost = nextBoost;
            currentPvalue = nextPvalue;
            iterations++;
            if (iterations > 50) {
                return 0;
            }
        }
        double lowBoost = Math.min(lastBoost, currentBoost);
        double highBoost = Math.max(lastBoost, currentBoost);
        while (highBoost - lowBoost > 0.00005) {
            double middleBoost = (highBoost + lowBoost) / 2;
            currentPvalue = test(testName, middleBoost);
            if (currentPvalue > pvalue) {
                highBoost = middleBoost;
            } else {
                lowBoost = middleBoost;
            }
            iterations++;
            if (iterations > 100) {
                return 0;
            }
        }
        return lowBoost;
    }

    public double test(String testName, double boost) {
        if (testName.compareToIgnoreCase("ttest") == 0 || testName.compareToIgnoreCase("pairedTTest") == 0) {
            return pairedTTest(boost);
        } else if (testName.compareToIgnoreCase("sign") == 0) {
            return signTest(boost);
        } else if (testName.compareToIgnoreCase("randomized") == 0) {
            return randomizedTest(boost);
        } else {
            throw new RuntimeException("'" + testName + "' is not a recognized test.");
        }
    }

    public double pairedTTest() {
        return pairedTTest(1.0);
    }

    public double pairedTTest(double boost) {
        double[] boostedBaseline = multiply(baseline, boost);
        double sampleSum = 0;
        double sampleSumSquares = 0;
        int n = boostedBaseline.length;
        for (int i = 0; i < baseline.length; i++) {
            double delta = treatment[i] - boostedBaseline[i];
            sampleSum += delta;
            sampleSumSquares += delta * delta;
        }
        double sampleVariance = sampleSumSquares / (n - 1);
        double sampleMean = sampleSum / baseline.length;
        double sampleDeviation = Math.sqrt(sampleVariance);
        double meanDeviation = sampleDeviation / Math.sqrt(n);
        double t = sampleMean / meanDeviation;
        return 1.0 - Stat.studentTProb(t, n - 1);
    }

    public double signTest() {
        return signTest(1.0);
    }

    public double signTest(double boost) {
        int treatmentIsBetter = 0;
        int different = 0;
        for (int i = 0; i < treatment.length; i++) {
            double boostedBaseline = baseline[i] * boost;
            if (treatment[i] > boostedBaseline) {
                treatmentIsBetter++;
            }
            if (treatment[i] != boostedBaseline) {
                different++;
            }
        }
        double pvalue = Stat.binomialProb(0.5, different, treatmentIsBetter);
        return pvalue;
    }

    public double randomizedTest() {
        return randomizedTest(1.0);
    }

    public double randomizedTest(double boost) {
        double[] boostedBaseline = multiply(baseline, boost);
        double baseMean = mean(boostedBaseline);
        double treatmentMean = mean(treatment);
        double difference = treatmentMean - baseMean;
        int batch = 10000;
        final int maxIterationsWithoutMatch = 1000000;
        long iterations = 0;
        long matches = 0;
        double[] leftSample = new double[boostedBaseline.length];
        double[] rightSample = new double[boostedBaseline.length];
        Random random = new Random();
        double pValue = 0.0;
        while (true) {
            for (int i = 0; i < batch; i++) {
                for (int j = 0; j < boostedBaseline.length; j++) {
                    if (random.nextBoolean()) {
                        leftSample[j] = boostedBaseline[j];
                        rightSample[j] = treatment[j];
                    } else {
                        leftSample[j] = treatment[j];
                        rightSample[j] = boostedBaseline[j];
                    }
                }
                double sampleDifference = mean(leftSample) - mean(rightSample);
                if (difference <= sampleDifference) {
                    matches++;
                }
            }
            iterations += batch;
            pValue = (double) matches / (double) iterations;
            if (matches == 0) {
                if (iterations < maxIterationsWithoutMatch) {
                    continue;
                } else {
                    break;
                }
            }
            double maxDeviation = Math.max(0.0000005 / pValue, Math.min(0.00005 / pValue, 0.05));
            double estimatedIterations = Math.sqrt(pValue * (1.0 - pValue)) / maxDeviation;
            if (estimatedIterations > iterations) {
                break;
            }
        }
        return pValue;
    }
}
