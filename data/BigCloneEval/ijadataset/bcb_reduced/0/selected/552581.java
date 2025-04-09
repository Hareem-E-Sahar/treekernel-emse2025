package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.jvm.ThreadChoiceGenerator;
import gov.nasa.jpf.jvm.ThreadInfo;
import java.io.PrintWriter;

public class ThreadChoiceFromSet extends ThreadChoiceGenerator {

    protected ThreadInfo[] values;

    protected int count;

    public ThreadChoiceFromSet(ThreadInfo[] set, boolean isSchedulingPoint) {
        super(isSchedulingPoint);
        values = set;
        count = -1;
    }

    public void reset() {
        count = -1;
    }

    public ThreadInfo getNextChoice() {
        if ((count >= 0) && (count < values.length)) {
            return values[count];
        } else {
            return null;
        }
    }

    public boolean hasMoreChoices() {
        return !isDone && (count < values.length - 1);
    }

    /**
   * this has to handle timeouts, which we do with temporary thread status
   * changes (i.e. the TIMEOUT_WAITING threads are in our list of choices, but
   * only change their status to TIMEDOUT when they are picked as the next choice)
   */
    public void advance() {
        if (count >= 0) {
            if (values[count].isTimedOut()) {
                values[count].resetTimedOut();
            }
        }
        if (count < values.length - 1) {
            count++;
            if (values[count].isTimeoutWaiting()) {
                values[count].setTimedOut();
            }
        }
    }

    public int getTotalNumberOfChoices() {
        return values.length;
    }

    public int getProcessedNumberOfChoices() {
        return count + 1;
    }

    public Object getNextChoiceObject() {
        return getNextChoice();
    }

    public void printOn(PrintWriter pw) {
        pw.print(getClass().getName());
        pw.print(" {");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) pw.print(',');
            if (i == count) {
                pw.print(MARKER);
            }
            pw.print(values[i].getName());
        }
        pw.print("}");
    }

    public ThreadChoiceFromSet randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            ThreadInfo tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }

    public ThreadInfo[] getAllThreadChoices() {
        return values;
    }
}
