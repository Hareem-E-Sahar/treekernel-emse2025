package gov.nasa.jpf.jvm.choice.ui;

import java.util.ArrayList;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.ChoiceGenerator;

public class UIActionFromSet extends UIActionGenerator {

    ArrayList<UIAction> values;

    int count;

    public UIActionFromSet() {
        count = -1;
        values = new ArrayList<UIAction>();
    }

    public UIActionFromSet(Config conf) {
        count = -1;
        values = new ArrayList<UIAction>();
    }

    public void add(UIAction action) {
        values.add(action);
    }

    public void advance() {
        count++;
    }

    public int getProcessedNumberOfChoices() {
        return count + 1;
    }

    public int getTotalNumberOfChoices() {
        return values.size();
    }

    public boolean hasMoreChoices() {
        return !isDone && (count < values.size() - 1);
    }

    public ChoiceGenerator randomize() {
        for (int i = values.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            UIAction tmp = values.get(i);
            values.set(i, values.get(j));
            values.set(j, tmp);
        }
        return this;
    }

    public void reset() {
        count = -1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("[id=\"");
        sb.append(id);
        sb.append("\",");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (i == count) {
                sb.append(MARKER);
            }
            sb.append(values.get(i).toString());
        }
        sb.append(']');
        return sb.toString();
    }

    public UIAction getNextChoice() {
        if ((count >= 0) && (count < values.size())) {
            return values.get(count);
        } else {
            return null;
        }
    }
}
