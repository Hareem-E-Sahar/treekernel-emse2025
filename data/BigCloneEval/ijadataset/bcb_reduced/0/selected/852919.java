package com.dhb.optimizing;

import com.dhb.matrixalgebra.DhbVector;

public class VectorGeneticOptimizer extends GeneticOptimizer {

    /**
	 * Best values found so far.
	 */
    private OptimizingVector[] bestPoints;

    /**
	 * Number of points filled so far.
	 */
    private int fillIndex;

    /**
 * VectorGeneticOptimizer constructor comment.
 * @param func DhbInterfaces.ManyVariableFunction
 * @param pointCreator DhbOptimizing.OptimizingPointFactory
 * @param chrManager DhbOptimizing.ChromosomeManager
 */
    public VectorGeneticOptimizer(com.dhb.interfaces.ManyVariableFunction func, OptimizingPointFactory pointCreator, ChromosomeManager chrManager) {
        super(func, pointCreator, chrManager);
    }

    /**
 * @param x DhbVector
 */
    public void collectPoint(Object x) {
        OptimizingVector v = pointFactory.createVector((DhbVector) x, f);
        if (fillIndex == 0 || bestPoints[fillIndex - 1].betterThan(v)) {
            bestPoints[fillIndex++] = v;
            return;
        }
        int n = 0;
        int m = fillIndex - 1;
        if (bestPoints[0].betterThan(v)) {
            int k;
            while (m - n > 1) {
                k = (n + m) / 2;
                if (v.betterThan(bestPoints[k])) m = k; else n = k;
            }
            n = m;
        }
        for (m = fillIndex; m > n; m--) bestPoints[m] = bestPoints[m - 1];
        bestPoints[n] = v;
        fillIndex += 1;
    }

    /**
 * This method was created in VisualAge.
 * @return double[]
 */
    public double[] getResult() {
        return bestPoints[0].getPosition();
    }

    /**
 * This method was created in VisualAge.
 * @return java.lang.Object
 * @param n int
 */
    public Object individualAt(int n) {
        try {
            return new DhbVector(bestPoints[n].getPosition());
        } catch (NegativeArraySizeException e) {
            return null;
        }
    }

    /**
 * This method was created in VisualAge.
 * @param n int
 */
    public void initializeIterations(int n) {
        bestPoints = new OptimizingVector[n];
    }

    /**
 * This method was created in VisualAge.
 * @return double[]
 */
    public double[] randomScale() {
        double[] integral = new double[bestPoints.length];
        double sum = 0;
        for (int i = 0; i < bestPoints.length; i++) {
            integral[i] = bestPoints[i].getValue() + sum;
            sum += bestPoints[i].getValue();
        }
        sum = 1 / sum;
        for (int i = 0; i < bestPoints.length; i++) integral[i] *= sum;
        return integral;
    }

    /**
 * This method was created in VisualAge.
 */
    public void reset() {
        fillIndex = 0;
    }

    /**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(bestPoints[0]);
        for (int i = 1; i < Math.min(bestPoints.length, 30); i++) {
            sb.append('\n');
            sb.append(bestPoints[i]);
        }
        return sb.toString();
    }
}
