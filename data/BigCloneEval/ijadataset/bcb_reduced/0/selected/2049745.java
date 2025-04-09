package preprocessing.methods.FeatureSelection.StatClassesLib;

/**
 *
 * @author pilnya1
 */
public class CrossCorrelationMatrix extends CorrelationBase {

    protected double[][] matrix;

    public double[][] getMatrix() {
        return matrix;
    }

    public CrossCorrelationMatrix(double[][] data) {
        super();
        createSpecUnits(data);
        matrix = computeCrossCorrelationsMatrix(data[0].length);
    }

    protected double[][] computeCrossCorrelationsMatrix(int size) {
        double[][] m = new double[size][size];
        SpecUnit x, y;
        int pom;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j <= i; j++) {
                if (i == j) {
                    m[i][j] = 1;
                } else {
                    m[i][j] = computeCorrelation((SpecUnit) mapUnits.get(i), (SpecUnit) mapUnits.get(j));
                    m[j][i] = m[i][j];
                }
            }
        }
        return m;
    }
}
