public class Test {    @Override
    public void reComputeFunctionValueAndGradient(double[] weights) {
        double[] weights2 = weights;
        if (shouldComputeGradientForScalingFactor) {
            if (weights.length != numFeatures + 1) {
                System.out.println("number of weights is not right");
                System.exit(1);
            }
            scalingFactor = weights[0];
            weights2 = new double[numFeatures];
            for (int i = 0; i < numFeatures; i++) weights2[i] = weights[i + 1];
        }
        for (int i = 0; i < gradientsForTheta.length; i++) gradientsForTheta[i] = 0;
        if (shouldComputeGradientForScalingFactor) gradientForScalingFactor = 0;
        functionValue = 0;
        sumGain = 0;
        sumEntropy = 0;
        hgFactory.startLoop();
        if (numThreads <= 1) reComputeFunctionValueAndGradientHelperSingleThread(weights2); else reComputeFunctionValueAndGradientHelper(weights2);
        hgFactory.endLoop();
        printLastestStatistics();
        numCalls++;
    }
}