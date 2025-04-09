package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.DoubleChoiceGenerator;
import java.util.logging.Logger;

/**
 * simple DoubleChoiceGenerator that takes it's values from a single
 * property "values" (comma or blank separated list)
 */
public class DoubleChoiceFromSet extends DoubleChoiceGenerator {

    static Logger log = JPF.getLogger("gov.nasa.jpf.jvm.choice");

    String[] values;

    int count;

    public DoubleChoiceFromSet(Config conf, String id) {
        super(id);
        values = conf.getStringArray(id + ".values");
        if (values == null) {
            throw new JPFException("value set for <" + id + "> choice did not load");
        }
        count = -1;
    }

    public void reset() {
        count = -1;
    }

    public Double getNextChoice() {
        if ((count >= 0) && (count < values.length)) {
            return new Double(DoubleSpec.eval(values[count]));
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

    public DoubleChoiceFromSet randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }
}
