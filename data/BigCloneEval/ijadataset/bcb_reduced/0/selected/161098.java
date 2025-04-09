package com.dhb.optimizing;

/**
 * This type was created in VisualAge.
 */
public abstract class GeneticOptimizer extends MultiVariableOptimizer {

    /**
	 * Chromosome manager.
	 */
    private ChromosomeManager chromosomeManager;

    /**
 * GeneticOptimizer constructor comment.
 * @param func DhbInterfaces.ManyVariableFunction
 * @param pointCreator DhbOptimizing.OptimizingPointFactory
 * @param chrManager ChromosomeManager
 */
    public GeneticOptimizer(com.dhb.interfaces.ManyVariableFunction func, OptimizingPointFactory pointCreator, ChromosomeManager chrManager) {
        super(func, pointCreator, null);
        chromosomeManager = chrManager;
    }

    /**
 * This method was created in VisualAge.
 * @param x java.lang.Object
 */
    public abstract void collectPoint(Object x);

    /**
 * This method was created in VisualAge.
 */
    public void collectPoints() {
        reset();
        for (int i = 0; i < chromosomeManager.getPopulationSize(); i++) collectPoint(chromosomeManager.individualAt(i));
    }

    /**
 * This method causes the receiver to exhaust the maximum number of iterations.
 * It may be overloaded by a subclass (hence "protected") if a convergence criteria can be defined.
 * @return double
 */
    protected double computePrecision() {
        return 1;
    }

    /**
 * This method was created in VisualAge.
 * @return double
 */
    public double evaluateIteration() {
        double[] randomScale = randomScale();
        chromosomeManager.reset();
        while (!chromosomeManager.isFullyPopulated()) {
            chromosomeManager.process(individualAt(randomIndex(randomScale)), individualAt(randomIndex(randomScale)));
        }
        collectPoints();
        return computePrecision();
    }

    /**
 * This method was created in VisualAge.
 * @return java.lang.Object
 * @param n int
 */
    public abstract Object individualAt(int n);

    /**
 * This method was created in VisualAge.
 */
    public void initializeIterations() {
        initializeIterations(chromosomeManager.getPopulationSize());
        chromosomeManager.randomnizePopulation();
        collectPoints();
    }

    /**
 * This method was created in VisualAge.
 * @param n int
 */
    public abstract void initializeIterations(int n);

    /**
 * This method was created in VisualAge.
 * @return int
 * @param randomScale double[]
 */
    protected int randomIndex(double[] randomScale) {
        double roll = chromosomeManager.nextDouble();
        if (roll < randomScale[0]) return 0;
        int n = 0;
        int m = randomScale.length;
        int k;
        while (n < m - 1) {
            k = (n + m) / 2;
            if (roll < randomScale[k]) m = k; else n = k;
        }
        return m;
    }

    /**
 * This method was created in VisualAge.
 * @return double[]
 */
    public abstract double[] randomScale();

    /**
 * This method was created in VisualAge.
 */
    public abstract void reset();
}
