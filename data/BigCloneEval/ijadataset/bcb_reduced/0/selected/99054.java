package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.jvm.IntChoiceGenerator;

/**
 * a generic IntChoiceGenerator randomizer. Not very efficient for large int intervals
 */
public class RandomOrderIntCG extends IntChoiceGenerator {

    protected int[] choices;

    protected int nextIdx;

    public RandomOrderIntCG(IntChoiceGenerator sub) {
        super(sub.id);
        setPreviousChoiceGenerator(sub.getPreviousChoiceGenerator());
        choices = new int[sub.getTotalNumberOfChoices()];
        for (int i = 0; i < choices.length; i++) {
            sub.advance();
            choices[i] = sub.getNextChoice();
        }
        for (int i = choices.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = choices[i];
            choices[i] = choices[j];
            choices[j] = tmp;
        }
        nextIdx = -1;
    }

    public Integer getNextChoice() {
        return new Integer(choices[nextIdx]);
    }

    public void advance() {
        if (nextIdx + 1 < choices.length) nextIdx++;
    }

    public int getProcessedNumberOfChoices() {
        return nextIdx + 1;
    }

    public int getTotalNumberOfChoices() {
        return choices.length;
    }

    public boolean hasMoreChoices() {
        return !isDone && (nextIdx + 1 < choices.length);
    }

    public void reset() {
        nextIdx = -1;
    }
}
