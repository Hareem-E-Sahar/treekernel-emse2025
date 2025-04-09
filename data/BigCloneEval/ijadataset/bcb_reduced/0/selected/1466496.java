package game.visualizations.roc;

import game.classifiers.Classifier;
import java.awt.Color;
import java.util.Arrays;
import com.rapidminer.operator.OperatorException;

/**
 * ROC curve generator for GAME classifiers (objects of the {@link Classifier} class).
 * 
 * @author janf
 *
 */
public class GameClassifierROCCurveGenerator extends AbstractROCCurveGenerator {

    private static final long serialVersionUID = 7176709480635235540L;

    protected int[] displayedClasses;

    protected Classifier classifier;

    public GameClassifierROCCurveGenerator() {
        this.classifier = null;
        displayedClasses = new int[0];
        updateResults();
    }

    public GameClassifierROCCurveGenerator(Classifier classifier) {
        this.classifier = classifier;
        displayedClasses = new int[classifier.getOutputsNumber()];
        for (int i = 0; i < displayedClasses.length; i++) displayedClasses[i] = i;
        updateResults();
    }

    @Override
    protected void updateResults() {
        removeAllSeries();
        try {
            for (int i : displayedClasses) {
                this.addSeries(countSeries(i));
            }
        } catch (OperatorException ex) {
            removeAllSeries();
        }
    }

    @Override
    protected struct[] getPole(int classIndex) throws OperatorException {
        if (classifier == null) {
            return new struct[0];
        }
        int vectNumber = 0;
        double[][] vectors = classifier.getLearningInputVectors();
        double[][] inputs = new double[0][0];
        double[][] outputs = classifier.getLearningOutputVectors();
        if (vectors.length > 0) {
            inputs = new double[vectors[0].length][vectors.length];
        }
        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < vectors[0].length; j++) {
                inputs[j][i] = vectors[i][j];
            }
        }
        if (inputs.length > 0) {
            vectNumber = inputs.length;
        }
        struct[] pole = new struct[vectNumber];
        for (int i = 0; i < inputs.length; i++) {
            pole[i] = new struct();
            pole[i].response = classifier.getOutputProbabilities(inputs[i])[classIndex];
            double expectedOutput = outputs[classIndex][i];
            assert expectedOutput == 0 || expectedOutput == 1;
            pole[i].expected = expectedOutput;
        }
        return pole;
    }

    @Override
    public String[] getClasses() {
        if (classifier == null) return new String[0];
        String[] classes = new String[classifier.getOutputsNumber()];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = Integer.toString(i);
        }
        return classes;
    }

    @Override
    public String[] getSelectedClasses() {
        String[] result = new String[displayedClasses.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.toString(displayedClasses[i]);
        }
        return result;
    }

    @Override
    public void setSelectedClasses(String[] classes) {
        displayedClasses = new int[classes.length];
        for (int i = 0; i < displayedClasses.length; i++) {
            displayedClasses[i] = Integer.parseInt(classes[i]);
        }
        Arrays.sort(displayedClasses);
        updateResults();
    }

    @Override
    public Color getSeriesColor(int series) {
        if (classifier.getOutputsNumber() == 0) return Color.RED;
        return getColorProvider().getPointColor((double) displayedClasses[series] / (double) (classifier.getOutputsNumber() - 1));
    }

    @Override
    public int[] getSelectedClassIndices() {
        return displayedClasses;
    }

    @Override
    public void setSelectedClassIndices(int[] indices) {
        displayedClasses = indices;
        updateResults();
    }

    @Override
    protected String getClassName(int classIndex) {
        if (classIndex < classifier.getOutputsNumber() && classIndex >= 0) {
            return Integer.toString(classIndex);
        }
        return null;
    }
}
