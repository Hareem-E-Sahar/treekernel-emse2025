package ForceGraph;

public abstract class FirstOrderOptimizationProcedure extends ForceDirectedOptimizationProcedure {

    protected double maxCos = 1;

    FirstOrderOptimizationProcedure(Graph g, ForceModel fm, double accuracy) {
        super(g, fm);
        maxCos = accuracy;
    }

    protected double negativeGradient[][] = null;

    protected double descentDirection[][] = null;

    protected double penaltyVector[][] = null;

    protected double penaltyFactor = 0;

    public double improveGraph() {
        int n = graph.getNumberOfVertices(), d = graph.getDimensions();
        if ((negativeGradient == null) || (negativeGradient.length != n)) {
            negativeGradient = new double[n][d];
            penaltyVector = new double[n][d];
            getNegativeGradient();
        }
        computeDescentDirection();
        return lineSearch();
    }

    public void reset() {
        negativeGradient = null;
        penaltyFactor = 0;
    }

    private void computePenaltyFactor() {
        double m1 = l2Norm(negativeGradient);
        double m2 = l2Norm(penaltyVector);
        if (m2 == 0) {
            penaltyFactor = 0;
        } else if (m1 == 0) {
            penaltyFactor = 1;
        } else {
            double cos = dotProduct(negativeGradient, penaltyVector) / (m1 * m2);
            penaltyFactor = Math.max(0.00000001, (0.00000001 - cos)) * Math.max(1, (m1 / m2));
        }
    }

    private void getNegativeGradient() {
        forceModel.getNegativeGradient(negativeGradient);
        if (constrained) {
            getPenaltyVector();
            computePenaltyFactor();
            int n = graph.getNumberOfVertices(), d = graph.getDimensions();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    negativeGradient[i][j] += penaltyFactor * penaltyVector[i][j];
                }
            }
        }
    }

    private void getPenaltyVector() {
        forceModel.getPenaltyVector(penaltyVector);
    }

    protected abstract void computeDescentDirection();

    private double stepSize = 0.1, previousStepSize = 0;

    protected double lineSearch() {
        previousStepSize = 0;
        double magDescDir = l2Norm(descentDirection);
        if (magDescDir < 0.0001) {
            return 0;
        }
        double magLo = l2Norm(negativeGradient);
        step();
        getNegativeGradient();
        double magHi = l2Norm(negativeGradient);
        double m = magDescDir * magHi;
        double cos = dotProduct(negativeGradient, descentDirection) / m;
        double lo = 0, hi = Double.MAX_VALUE;
        int i = 0;
        while (((cos < 0) || (cos > maxCos)) && (hi - lo > 0.00000001)) {
            if (cos < 0) {
                hi = stepSize;
                stepSize = (lo + hi) / 2;
            } else {
                if (hi < Double.MAX_VALUE) {
                    lo = stepSize;
                    stepSize = (lo + hi) / 2;
                } else {
                    lo = stepSize;
                    stepSize *= 2;
                }
            }
            step();
            getNegativeGradient();
            m = magDescDir * l2Norm(negativeGradient);
            cos = dotProduct(negativeGradient, descentDirection) / m;
        }
        return l2Norm(negativeGradient);
    }

    private void step() {
        double s = stepSize - previousStepSize;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            if (!((Vertex) (graph.vertices.get(i))).isFixed()) {
                ((Vertex) graph.vertices.get(i)).translate(s, descentDirection[i]);
            }
        }
        previousStepSize = stepSize;
    }

    protected double dotProduct(double[][] u, double[][] v) {
        double sum = 0;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getDimensions(); j++) {
                sum += u[i][j] * v[i][j];
            }
        }
        return sum;
    }

    protected double l2Norm(double[][] vect) {
        return Math.sqrt(dotProduct(vect, vect));
    }

    protected double lInfinityNorm(double[][] vect) {
        double max = 0;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getDimensions(); j++) {
                max = Math.max(max, Math.abs(vect[i][j]));
            }
        }
        return max;
    }
}
