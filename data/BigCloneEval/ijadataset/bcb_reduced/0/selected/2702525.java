package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.DoubleChoiceGenerator;

/**
 * ChoiceGenerator instance that produces a simple 3 value enumeration
 * 
 */
public class DoubleThresholdGenerator extends DoubleChoiceGenerator {

    double[] values = new double[3];

    int count;

    public DoubleThresholdGenerator(Config conf, String id) {
        super(id);
        values[0] = conf.getDouble(id + ".low");
        values[1] = conf.getDouble(id + ".threshold");
        values[2] = conf.getDouble(id + ".high");
        count = -1;
    }

    public void reset() {
        count = -1;
    }

    public boolean hasMoreChoices() {
        return !isDone && (count < 2);
    }

    public Double getNextChoice() {
        if (count >= 0) {
            return new Double(values[count]);
        } else {
            return new Double(values[0]);
        }
    }

    public void advance() {
        if (count < 2) count++;
    }

    public int getTotalNumberOfChoices() {
        return 3;
    }

    public int getProcessedNumberOfChoices() {
        return count + 1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[id=\"");
        sb.append(id);
        sb.append("\",");
        for (int i = 0; i < 3; i++) {
            if (count == i) {
                sb.append(MARKER);
            }
            sb.append(values[i]);
            if (count < 2) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public DoubleThresholdGenerator randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }
}
