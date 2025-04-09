package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.IntChoiceGenerator;

/**
 * @author jpenix
 *
 * choose from a set of values provided in configuration as
 * xxx.class = IntChoiceFromSet
 * xxx.values = {1, 2, 3, 400}
 * where "xxx" is the choice id.
 * 
 * choices can then made using: getInt("xxx");
 */
public class IntChoiceFromSet extends IntChoiceGenerator {

    String[] values;

    int count = -1;

    /**
	 * @param conf JPF configuration object
	 * @param id name used in choice config
	 */
    public IntChoiceFromSet(Config conf, String id) {
        super(id);
        values = conf.getStringArray(id + ".values");
        if (values == null) {
            throw new JPFException("value set for <" + id + "> choice did not load");
        }
    }

    /** super constructor for subclasses that want to configure themselves
	 * 
	 * @param id name used in choice config
	 */
    protected IntChoiceFromSet(String id) {
        super(id);
    }

    public void reset() {
        count = -1;
    }

    /** 
	 * @see gov.nasa.jpf.jvm.IntChoiceGenerator#getNextChoice()
	 **/
    public Integer getNextChoice() {
        int ret;
        ret = IntSpec.eval(values[count]);
        return new Integer(ret);
    }

    /**
	 * @see gov.nasa.jpf.jvm.ChoiceGenerator#hasMoreChoices()
	 **/
    public boolean hasMoreChoices() {
        if (!isDone && (count < values.length - 1)) return true; else return false;
    }

    /**
	 * @see gov.nasa.jpf.jvm.ChoiceGenerator#advance()
	 **/
    public void advance() {
        if (count < values.length - 1) count++;
    }

    /**
	 * get String label of current value, as specified in config file
	 **/
    public String getValueLabel() {
        return values[count];
    }

    public int getTotalNumberOfChoices() {
        return values.length;
    }

    public int getProcessedNumberOfChoices() {
        return count + 1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
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

    public IntChoiceFromSet randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }
}
