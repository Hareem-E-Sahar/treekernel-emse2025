package gov.nasa.jpf.complexcoverage.choice;

import java.util.logging.Logger;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.DoubleChoiceGenerator;

/**
 * @author staats
 */
public class TaggedDoubleChoiceFromSet extends DoubleChoiceGenerator {

    static Logger log = JPF.getLogger("gov.nasa.jpf.jvm.choice");

    double[] values;

    int count;

    String varName = "";

    public TaggedDoubleChoiceFromSet(double[] choiceValues, String id) {
        super(id);
        values = choiceValues.clone();
        varName = id;
        count = -1;
    }

    public String getVarName() {
        return varName;
    }

    public void reset() {
        count = -1;
    }

    public Double getNextChoice() {
        if ((count >= 0) && (count < values.length)) {
            return values[count];
        }
        return Double.NaN;
    }

    public boolean hasMoreChoices() {
        return !isDone && (count < values.length - 1);
    }

    public void advance() {
        if (count < values.length - 1) count++;
    }

    public int getTotalNumberOfChoices() {
        return values.length;
    }

    public int getProcessedNumberOfChoices() {
        return count + 1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("[id=\"");
        sb.append(id);
        sb.append("\",");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (i == count) {
                sb.append(MARKER);
            }
            sb.append(values[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    public TaggedDoubleChoiceFromSet randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }
}
