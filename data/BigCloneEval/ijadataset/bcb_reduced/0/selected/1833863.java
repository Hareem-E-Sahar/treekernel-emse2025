package gov.nasa.jpf.jvm.choice;

import java.util.ArrayList;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.ReferenceChoiceGenerator;

/**
 * a choice generator that enumerates the set of all objects of a certain type. This
 * is a replacement for the old 'Verify.randomObject'
 */
public class TypedObjectChoice extends ReferenceChoiceGenerator {

    String type;

    int[] values;

    int count;

    public TypedObjectChoice(Config conf, String id) throws Config.Exception {
        DynamicArea heap = DynamicArea.getHeap();
        type = conf.getString(id + ".type");
        if (type == null) {
            throw conf.exception("missing 'type' property for TypedObjectGenerator " + id);
        }
        ArrayList<ElementInfo> list = new ArrayList<ElementInfo>();
        for (ElementInfo ei : heap) {
            ClassInfo ci = ei.getClassInfo();
            if (ci.isInstanceOf(type)) {
                list.add(ei);
            }
        }
        values = new int[list.size()];
        int i = 0;
        for (ElementInfo ei : list) {
            values[i++] = ei.getIndex();
        }
        count = -1;
    }

    @Override
    public void advance() {
        count++;
    }

    @Override
    public int getProcessedNumberOfChoices() {
        return count + 1;
    }

    @Override
    public int getTotalNumberOfChoices() {
        return values.length;
    }

    @Override
    public boolean hasMoreChoices() {
        return !isDone && (count < values.length - 1);
    }

    @Override
    public void reset() {
        count = -1;
    }

    public Integer getNextChoice() {
        if ((count >= 0) && (count < values.length)) {
            return new Integer(values[count]);
        } else {
            return new Integer(-1);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("TypedObjectGenerator [id=");
        sb.append(id);
        sb.append(",type=");
        sb.append(type);
        sb.append(",values=");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (i == count) {
                sb.append("=>");
            }
            sb.append(values[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    public TypedObjectChoice randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }
}
