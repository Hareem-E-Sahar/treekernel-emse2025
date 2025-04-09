package org.mcisb.kinetics;

import java.io.*;
import java.util.*;
import org.apache.commons.math.linear.*;
import org.apache.commons.math.stat.descriptive.rank.*;
import org.mcisb.ontology.sbo.*;
import org.mcisb.util.*;
import org.mcisb.util.math.*;

/**
 * 
 * @author Neil Swainston
 */
public class KineticsCalculator extends PropertyChangeSupported implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
	 * 
	 */
    public static final String UPDATE = "UPDATE";

    /**
	 * 
	 */
    public static final double DEFAULT_HILL_COEFFICIENT = 1;

    /**
	 * 
	 */
    public static final double DEFAULT_HILL_COEFFICIENT_ERROR = 0;

    /**
	 * 
	 */
    private static final double TAU = 1e-3;

    /**
	 * 
	 */
    private static final int RESIDUES = 0;

    /**
	 * 
	 */
    private static final int V_MAX = 1;

    /**
	 * 
	 */
    private static final int KM = 2;

    /**
	 * 
	 */
    private static final int V_MAX_STANDARD_ERROR = 3;

    /**
	 * 
	 */
    private static final int KM_STANDARD_ERROR = 4;

    /**
	 * 
	 */
    private static final int HILL_COEFFICIENT = 5;

    /**
	 * 
	 */
    private static final int HILL_COEFFICIENT_ERROR = 6;

    /**
	 * 
	 */
    private static final int NUM_POINTS = 16;

    /**
	 * 
	 */
    private final double[] timepoints;

    /**
	 * 
	 */
    private final double[] substrateConcentrations;

    /**
	 * 
	 */
    private final double[][] productConcentrations;

    /**
	 * 
	 */
    private final double enzymeConcentration;

    /**
	 * 
	 */
    private final Map<Integer, Double> values = new java.util.HashMap<Integer, Double>();

    /**
	 * 
	 */
    private final Map<Integer, Double> errors = new java.util.HashMap<Integer, Double>();

    /**
	 * 
	 */
    private boolean calculated = false;

    /**
	 * 
	 */
    private boolean negative = false;

    /**
	 * 
	 */
    private double[] initialRates = null;

    /**
	 * 
	 */
    private double[] nonControlSubstrateConcentrations = null;

    /**
	 * 
	 */
    private double[] nonControlInitialRates = null;

    /**
	 * 
	 */
    private final boolean considerHillCoefficient;

    /**
	 * 
	 */
    private double hillCoefficient = DEFAULT_HILL_COEFFICIENT;

    /**
	 * 
	 */
    private boolean hillCoefficientSet = false;

    /**
	 * 
	 * @param timepoints
	 * @param substrateConcentrations
	 * @param productConcentrations
	 * @param enzymeConcentration
	 * @param initialRates
	 * @param considerHillCoefficient
	 */
    public KineticsCalculator(final double[] timepoints, final double[] substrateConcentrations, final double[][] productConcentrations, final double enzymeConcentration, final double[] initialRates, final boolean considerHillCoefficient) {
        this(timepoints, substrateConcentrations, productConcentrations, enzymeConcentration, considerHillCoefficient);
        this.initialRates = new double[substrateConcentrations.length];
        int y = 0;
        for (int i = 0; i < initialRates.length; i++) {
            if (initialRates[i] != NumberUtils.UNDEFINED) {
                this.initialRates[y++] = initialRates[i];
            }
        }
    }

    /**
	 * 
	 * @param timepoints
	 * @param substrateConcentrations
	 * @param productConcentrations
	 * @param enzymeConcentration
	 * @param considerHillCoefficient
	 */
    public KineticsCalculator(final double[] timepoints, final double[] substrateConcentrations, final double[][] productConcentrations, final double enzymeConcentration, final boolean considerHillCoefficient) {
        final double[] timepointsCopy = new double[timepoints.length];
        System.arraycopy(timepoints, 0, timepointsCopy, 0, timepoints.length);
        final double[] substrateConcentrationsCopy = new double[substrateConcentrations.length];
        System.arraycopy(substrateConcentrations, 0, substrateConcentrationsCopy, 0, substrateConcentrations.length);
        this.timepoints = timepointsCopy;
        this.substrateConcentrations = substrateConcentrationsCopy;
        this.productConcentrations = CollectionUtils.copy(productConcentrations);
        this.enzymeConcentration = enzymeConcentration;
        this.considerHillCoefficient = considerHillCoefficient;
    }

    /**
	 * @return the timepoints
	 */
    public double[] getTimepoints() {
        return Arrays.copyOf(timepoints, timepoints.length);
    }

    /**
	 * @return the substrateConcentrations
	 */
    public double[] getSubstrateConcentrations() {
        return Arrays.copyOf(substrateConcentrations, substrateConcentrations.length);
    }

    /**
	 * @return the productConcentrations
	 */
    public double[][] getProductConcentrations() {
        final double[][] productConcentrationsCopy = new double[productConcentrations.length][];
        for (int i = 0; i < productConcentrationsCopy.length; i++) {
            productConcentrationsCopy[i] = Arrays.copyOf(productConcentrations[i], productConcentrations[i].length);
        }
        return productConcentrationsCopy;
    }

    /**
	 * @return the enzymeConcentration
	 */
    public double getEnzymeConcentration() {
        return enzymeConcentration;
    }

    /**
	 * 
	 * @return considerHillCoefficient
	 */
    public boolean getConsiderHillCoefficient() {
        return considerHillCoefficient;
    }

    /**
	 * 
	 * @param index
	 * @param initialRate
	 */
    public synchronized void setInitialRate(final int index, final double initialRate) {
        calcInitialRates();
        final double oldInitialRate = initialRates[index];
        initialRates[index] = initialRate;
        calculated = false;
        support.firePropertyChange(UPDATE, Double.valueOf(oldInitialRate), Double.valueOf(initialRate));
    }

    /**
	 * @param hillCoefficient
	 */
    public synchronized void setHillCoefficient(final double hillCoefficient) {
        final double oldHillCoefficient = this.hillCoefficient;
        this.hillCoefficient = hillCoefficient;
        hillCoefficientSet = true;
        calculated = false;
        support.firePropertyChange(UPDATE, Double.valueOf(oldHillCoefficient), Double.valueOf(hillCoefficient));
    }

    /**
	 *
	 * @param id
	 * @return double
	 */
    public double getValue(final int id) {
        doCalculation();
        return values.get(Integer.valueOf(id)).doubleValue();
    }

    /**
	 *
	 * @param id
	 * @return double
	 */
    public double getError(final int id) {
        doCalculation();
        return errors.get(Integer.valueOf(id)).doubleValue();
    }

    /**
	 * 
	 * @return boolean
	 */
    public boolean isNegative() {
        doCalculation();
        return negative;
    }

    /**
	 * @return double[]
	 */
    public synchronized double[] getInitialRates() {
        calcInitialRates();
        return Arrays.copyOf(initialRates, initialRates.length);
    }

    /**
	 * 
	 */
    private synchronized void doCalculation() {
        if (!calculated) {
            calcInitialRates();
            final double[] parameters = performCalculation();
            values.put(Integer.valueOf(SboUtils.MICHAELIS_CONSTANT), Double.valueOf(parameters[KM]));
            values.put(Integer.valueOf(SboUtils.CATALYTIC_RATE_CONSTANT), Double.valueOf(parameters[V_MAX] / enzymeConcentration));
            values.put(Integer.valueOf(SboUtils.HILL_COEFFICIENT), Double.valueOf(parameters[HILL_COEFFICIENT]));
            errors.put(Integer.valueOf(SboUtils.MICHAELIS_CONSTANT), Double.valueOf(parameters[KM_STANDARD_ERROR]));
            errors.put(Integer.valueOf(SboUtils.CATALYTIC_RATE_CONSTANT), Double.valueOf(parameters[V_MAX_STANDARD_ERROR] / enzymeConcentration));
            errors.put(Integer.valueOf(SboUtils.HILL_COEFFICIENT), Double.valueOf(parameters[HILL_COEFFICIENT_ERROR]));
            calculated = true;
        }
    }

    /**
	 * 
	 * @return double[]
	 */
    private double[] performCalculation() {
        if (considerHillCoefficient && !hillCoefficientSet) {
            final int MINIMUM_HILL_COEFFICIENT = 0;
            final int MAXIMUM_HILL_COEFFICIENT = 10;
            final double PHI = (1 + Math.sqrt(2)) / 2;
            final double RESPHI = 2 - PHI;
            double a = MINIMUM_HILL_COEFFICIENT;
            double b = hillCoefficient;
            double c = MAXIMUM_HILL_COEFFICIENT;
            double fb = hillFit(MathUtils.mapPow(nonControlSubstrateConcentrations, b), nonControlInitialRates)[RESIDUES];
            boolean finished = false;
            while (!finished) {
                double x;
                if (c - b > b - a) {
                    x = b + RESPHI * (c - b);
                } else {
                    x = b - RESPHI * (b - a);
                }
                if (Math.abs(c - a) < TAU * (Math.abs(b) + Math.abs(x))) {
                    hillCoefficient = (c + a) / 2;
                    finished = true;
                    break;
                }
                double fx = hillFit(MathUtils.mapPow(nonControlSubstrateConcentrations, x), nonControlInitialRates)[RESIDUES];
                double aNew;
                double bNew;
                double cNew;
                if (fx < fb) {
                    bNew = x;
                    fb = fx;
                    if (c - b > b - a) {
                        aNew = b;
                        cNew = c;
                    } else {
                        aNew = a;
                        cNew = b;
                    }
                } else {
                    bNew = b;
                    if (c - b > b - a) {
                        aNew = a;
                        cNew = x;
                    } else {
                        aNew = x;
                        cNew = c;
                    }
                }
                a = aNew;
                b = bNew;
                c = cNew;
            }
        }
        return performFit();
    }

    /**
	 * 
	 * @return double[]
	 */
    private double[] performFit() {
        double hillCoefficientError = DEFAULT_HILL_COEFFICIENT_ERROR;
        if (considerHillCoefficient && !hillCoefficientSet) {
            double f0 = hillFit(MathUtils.mapPow(nonControlSubstrateConcentrations, hillCoefficient - TAU), nonControlInitialRates)[RESIDUES];
            double f1 = hillFit(MathUtils.mapPow(nonControlSubstrateConcentrations, hillCoefficient), nonControlInitialRates)[RESIDUES];
            double f2 = hillFit(MathUtils.mapPow(nonControlSubstrateConcentrations, hillCoefficient + TAU), nonControlInitialRates)[RESIDUES];
            double H = (f0 + f2 - (2 * f1)) / Math.pow(TAU, 2);
            hillCoefficientError = Math.sqrt(1 / H);
        }
        final double[] hillFit = hillFit(MathUtils.mapPow(nonControlSubstrateConcentrations, hillCoefficient), nonControlInitialRates);
        double kM = Math.pow(hillFit[KM], (1 / hillCoefficient));
        double kMError = Math.pow(hillFit[KM_STANDARD_ERROR], (1 / hillCoefficient));
        return new double[] { hillFit[RESIDUES], hillFit[V_MAX], kM, hillFit[V_MAX_STANDARD_ERROR], kMError, hillCoefficient, hillCoefficientError };
    }

    /**
	 * 
	 */
    private void calcInitialRates() {
        final int FIRST = 0;
        double[] productConcentrationRanges = new double[productConcentrations.length];
        for (int i = 0; i < productConcentrations.length; i++) {
            final double[] currentProductConcentrations = productConcentrations[i];
            productConcentrationRanges[i] = currentProductConcentrations[currentProductConcentrations.length - 1] - currentProductConcentrations[FIRST];
        }
        negative = (new Median().evaluate(productConcentrationRanges) < 0);
        if (initialRates == null) {
            double[] calculatedInitialRates = new double[productConcentrations.length];
            for (int i = 0; i < productConcentrations.length; i++) {
                final double[] linearFit = MathUtils.linearFit(timepoints, productConcentrations[i], NUM_POINTS);
                calculatedInitialRates[i] = linearFit[1];
            }
            if (negative) {
                calculatedInitialRates = MathUtils.scalarMultiply(calculatedInitialRates, -1);
            }
            initialRates = calculatedInitialRates;
        }
        applyControls();
    }

    /**
	 * 
	 * @return double[][]
	 */
    private void applyControls() {
        final List<Double> controlInitialRates = new ArrayList<Double>();
        final List<Double> nonControlSubstrateConcentrationsList = new ArrayList<Double>();
        final List<Double> nonControlInitialRatesList = new ArrayList<Double>();
        for (int i = 0; i < substrateConcentrations.length; i++) {
            if (substrateConcentrations[i] == 0) {
                controlInitialRates.add(Double.valueOf(initialRates[i]));
            } else {
                nonControlSubstrateConcentrationsList.add(Double.valueOf(substrateConcentrations[i]));
                nonControlInitialRatesList.add(Double.valueOf(initialRates[i]));
            }
        }
        final Set<Double> substrateConcentrationSet = new HashSet<Double>(nonControlSubstrateConcentrationsList);
        if (substrateConcentrationSet.size() == 1) {
            final double[] zeroSubtrateConcentrations = new double[controlInitialRates.size()];
            for (int i = 0; i < controlInitialRates.size(); i++) {
                nonControlInitialRatesList.add(Double.valueOf(Math.max(0.0, controlInitialRates.get(i).doubleValue())));
            }
            nonControlSubstrateConcentrationsList.addAll(CollectionUtils.toList(zeroSubtrateConcentrations));
        } else {
            final double controlInitialRate = (controlInitialRates.size() == 0) ? 0 : new Median().evaluate(CollectionUtils.toDoubleArray(controlInitialRates));
            for (int i = 0; i < nonControlInitialRatesList.size(); i++) {
                nonControlInitialRatesList.set(i, Double.valueOf(nonControlInitialRatesList.get(i).doubleValue() - controlInitialRate));
            }
        }
        nonControlSubstrateConcentrations = CollectionUtils.toDoubleArray(nonControlSubstrateConcentrationsList);
        nonControlInitialRates = CollectionUtils.toDoubleArray(nonControlInitialRatesList);
    }

    /**
	 * 
	 * @param substrateConcentrations
	 * @param initialRates
	 * @param n
	 * @return double[]
	 */
    private static double[] hillFit(final double[] substrateConcentrations, final double[] initialRates) {
        final double[] eadieHofstee = eadieHofstee(substrateConcentrations, initialRates);
        double vMax = eadieHofstee[MathUtils.INTERSECTION];
        double kM = Math.max(0, -eadieHofstee[MathUtils.GRADIENT]);
        double vMaxError = 0;
        double kMError = 0;
        try {
            final double[] levenbergMarquardt = levenbergMarquardt(substrateConcentrations, initialRates, vMax, kM);
            vMax = levenbergMarquardt[0];
            kM = levenbergMarquardt[1];
            vMaxError = levenbergMarquardt[2];
            kMError = levenbergMarquardt[3];
        } catch (SingularMatrixException e) {
            e.printStackTrace();
        }
        final double[] substrateConcentrationsPlusKm = Arrays.copyOf(substrateConcentrations, substrateConcentrations.length);
        MathUtils.add(substrateConcentrationsPlusKm, kM);
        final double[] initialRatesNew = MathUtils.ebeDivide(MathUtils.scalarMultiply(substrateConcentrations, vMax), substrateConcentrationsPlusKm);
        final double[] subtracted = MathUtils.subtract(initialRates, initialRatesNew);
        final double residues = MathUtils.norm(subtracted);
        return new double[] { residues, vMax, kM, vMaxError, kMError };
    }

    /**
	 * 
	 * @param substrateConcentrations
	 * @param initialRates
	 * @param hillCoefficient
	 * @return double[]
	 */
    private static double[] eadieHofstee(final double[] substrateConcentrations, final double[] initialRates) {
        final double[] scaledRates = new double[initialRates.length];
        for (int i = 0; i < scaledRates.length; i++) {
            scaledRates[i] = initialRates[i] / substrateConcentrations[i] == 0.0 ? 1.0 : substrateConcentrations[i];
        }
        return MathUtils.linearFit(scaledRates, initialRates);
    }

    /**
	 * 
	 * @param substrateConcentrations
	 * @param initialRates
	 * @param vMax
	 * @param kM
	 * @return double[]
	 * @throws SingularMatrixException
	 */
    private static double[] levenbergMarquardt(final double[] substrateConcentrations, final double[] initialRates, final double vMax, final double kM) throws SingularMatrixException {
        final double TOLERANCE_X = 1e-9;
        final double TOLERANCE_F = 1e-6;
        final double MAX_ITERATIONS = 100;
        double lambda = 1e-3;
        double[] parameters = new double[] { vMax, kM };
        double[] parametersChange = new double[parameters.length];
        double[] errorsOld = new double[substrateConcentrations.length];
        Arrays.fill(errorsOld, Double.POSITIVE_INFINITY);
        double[] errorsNew = null;
        final double[][] j = new double[substrateConcentrations.length][parameters.length];
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            parameters = MathUtils.add(parameters, parametersChange);
            final double[] f = new double[substrateConcentrations.length];
            for (int s = 0; s < substrateConcentrations.length; s++) {
                final double denominator = Math.max(parameters[1] + substrateConcentrations[s], Math.sqrt(Double.MIN_VALUE));
                f[s] = parameters[0] * substrateConcentrations[s] / denominator;
                j[s][0] = substrateConcentrations[s] / denominator;
                j[s][1] = -parameters[0] * substrateConcentrations[s] / Math.pow(denominator, 2);
            }
            final double[][] jtj = MathUtils.multiply(MathUtils.transpose(j), j);
            final double[][] im = MathUtils.getIdentityMatrix(parameters.length);
            final double[][] iml = MathUtils.scalarMultiply(im, lambda);
            final double[][] jtjiml = MathUtils.add(jtj, iml);
            final double[][] c = MathUtils.inverse(jtjiml);
            errorsNew = MathUtils.subtract(initialRates, f);
            parametersChange = MathUtils.multiply(MathUtils.multiply(c, MathUtils.transpose(j)), errorsNew);
            double error = 0;
            for (int s = 0; s < parameters.length; s++) {
                error = Math.max(error, Math.abs(parametersChange[s] / parameters[s]));
            }
            if (MathUtils.twoNorm(errorsNew) > MathUtils.twoNorm(errorsOld)) {
                lambda *= 10;
            } else {
                lambda /= 10;
                if (error < TOLERANCE_X || ((MathUtils.twoNorm(errorsOld) - MathUtils.twoNorm(errorsNew)) / MathUtils.twoNorm(errorsOld) < TOLERANCE_F)) {
                    break;
                }
            }
            errorsOld = errorsNew;
        }
        final double vMaxRecalculated = parameters[0];
        final double kMRecalculated = parameters[1];
        final double sigma = MathUtils.dot(errorsNew, errorsNew) / (substrateConcentrations.length - parameters.length);
        final double[][] c = MathUtils.scalarMultiply(MathUtils.inverse(MathUtils.multiply(MathUtils.transpose(j), j), Double.MIN_VALUE), sigma);
        return new double[] { vMaxRecalculated, kMRecalculated, Math.sqrt(c[0][0]), Math.sqrt(c[1][1]) };
    }
}
