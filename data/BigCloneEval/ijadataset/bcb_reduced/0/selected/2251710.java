package ec.util;

/**
 * RandomChoice organizes arrays of floats into distributions which can
 * be used to pick randomly from.  You can provide three kinds of arrays:
 * 
 <ul>
 <li> An array of floats
 <li> An array of doubles
 <li> An array of arbitrary objects, plus a RandomChoiceChooser which knows
 how to get and set the appropriate "float" value of objects in this array.
 </ul>
 *
 * <p>Before the RandomChoice can pick randomly from your array, it must first
 * organize it.  It does this by doing the following.  First, it normalizes the
 * values in the array.  Then it modifies them to their sums.  That is, each item i
 * in the array is set to the sum of the original values for items 0...i.  If you
 * cannot allow your objects to be modified, then this is not the class for you.
 *
 * <p>An array is valid if (1) it has no negative values and (2) not all of its
 * values are zero.  This RandomChoice code <i>should</i> (I hope) guarantee that
 * an element of zero probability is never returned.  RandomChoice uses a binary
 * search to find your index, followed by linear probing (marching up or down
 * the list) to find the first non-zero probability item in the vacinity of that
 * index.  As long as there are not a whole lot of zero-valued items in a row,
 * RandomChoice is efficient.
 *
 * You organize your array with organizeDistribution().  Then you can have the
 * RandomChoice pick random items from the array and return their indexes to you.
 * You do this by calling pickFromDistribution(), passing it a random floating
 * point value between 0.0 and 1.0.  You call organizeDistribution() only once;
 * after which you may call pickFromDistribution() as many times as you like.
 * You should not modify the array thereafter.
 *
 * @author Sean Luke
 * @version 1.0 
 */
public class RandomChoice {

    /** Same as organizeDistribution(probabilities,  <b>false</b>); */
    public static void organizeDistribution(final float[] probabilities) {
        organizeDistribution(probabilities, false);
    }

    /** Normalizes probabilities, then converts them into continuing
        sums.  This prepares them for being usable in pickFromDistribution.
        If the probabilities are all 0, then selection is uniform, unless allowAllZeros
        is false, in which case an ArithmeticException is thrown.  If any of them are negative,
        or if the distribution is empty, then an ArithmeticException is thrown.
        For example, 
        {0.6, 0.4, 0.2, 0.8} -> {0.3, 0.2, 0.1, 0.4} -> {0.3, 0.5, 0.6, 1.0} */
    public static void organizeDistribution(final float[] probabilities, final boolean allowAllZeros) {
        double sum = 0.0;
        if (probabilities.length == 0) throw new ArithmeticException("Distribution has no elements");
        for (int x = 0; x < probabilities.length; x++) {
            if (probabilities[x] < 0.0) throw new ArithmeticException("Distribution has negative probabilities");
            sum += probabilities[x];
        }
        if (sum == 0.0) if (!allowAllZeros) throw new ArithmeticException("Distribution has all zero probabilities"); else {
            for (int x = 0; x < probabilities.length; x++) probabilities[x] = 1.0f;
            sum = probabilities.length;
        }
        for (int x = 0; x < probabilities.length; x++) probabilities[x] /= sum;
        sum = 0.0;
        for (int x = 0; x < probabilities.length; x++) {
            sum += probabilities[x];
            probabilities[x] = (float) sum;
        }
        int x;
        for (x = probabilities.length - 1; x > 0; x--) if (probabilities[x] == probabilities[x - 1]) probabilities[x] = 1.0f; else break;
        probabilities[x] = 1.0f;
    }

    /** Same as organizeDistribution(probabilities,  <b>false</b>); */
    public static void organizeDistribution(final double[] probabilities) {
        organizeDistribution(probabilities, false);
    }

    /** Normalizes probabilities, then converts them into continuing
        sums.  This prepares them for being usable in pickFromDistribution.
        If the probabilities are all 0, then selection is uniform, unless allowAllZeros
        is false, in which case an ArithmeticException is thrown.  If any of them are negative,
        or if the distribution is empty, then an ArithmeticException is thrown.
        For example, 
        {0.6, 0.4, 0.2, 0.8} -> {0.3, 0.2, 0.1, 0.4} -> {0.3, 0.5, 0.6, 1.0} */
    public static void organizeDistribution(final double[] probabilities, final boolean allowAllZeros) {
        double sum = 0.0;
        if (probabilities.length == 0) throw new ArithmeticException("Distribution has no elements");
        for (int x = 0; x < probabilities.length; x++) {
            if (probabilities[x] < 0.0) throw new ArithmeticException("Distribution has negative probabilities");
            sum += probabilities[x];
        }
        if (sum == 0.0) if (!allowAllZeros) throw new ArithmeticException("Distribution has all zero probabilities"); else {
            for (int x = 0; x < probabilities.length; x++) probabilities[x] = 1.0;
            sum = probabilities.length;
        }
        for (int x = 0; x < probabilities.length; x++) probabilities[x] /= sum;
        sum = 0.0;
        for (int x = 0; x < probabilities.length; x++) {
            sum += probabilities[x];
            probabilities[x] = sum;
        }
        int x;
        for (x = probabilities.length - 1; x > 0; x--) if (probabilities[x] == probabilities[x - 1]) probabilities[x] = 1.0; else break;
        probabilities[x] = 1.0;
    }

    /** Same as organizeDistribution(objs, chooser, <b>false</b>); */
    public static void organizeDistribution(final Object[] objs, final RandomChoiceChooser chooser) {
        organizeDistribution(objs, chooser, false);
    }

    /** Normalizes the probabilities associated
        with an array of objects, then converts them into continuing
        sums.  This prepares them for being usable in pickFromDistribution.
        If the probabilities are all 0, then selection is uniform, unless allowAllZeros
        is false, in which case an ArithmeticException is thrown.  If any of them are negative,
        or if the distribution is empty, then an ArithmeticException is thrown.
        For example, 
        {0.6, 0.4, 0.2, 0.8} -> {0.3, 0.2, 0.1, 0.4} -> {0.3, 0.5, 0.6, 1.0} 
        The probabilities are retrieved and set using chooser.*/
    public static void organizeDistribution(final Object[] objs, final RandomChoiceChooser chooser, final boolean allowAllZeros) {
        double sum = 0.0;
        if (objs.length == 0) throw new ArithmeticException("Distribution has no elements");
        for (int x = 0; x < objs.length; x++) {
            if (chooser.getProbability(objs[x]) < 0.0) throw new ArithmeticException("Distribution has negative probabilities");
            sum += chooser.getProbability(objs[x]);
        }
        if (sum == 0.0) if (!allowAllZeros) throw new ArithmeticException("Distribution has all zero probabilities"); else {
            for (int x = 0; x < objs.length; x++) chooser.setProbability(objs[x], 1.0f);
            sum = objs.length;
        }
        for (int x = 0; x < objs.length; x++) chooser.setProbability(objs[x], (float) (chooser.getProbability(objs[x]) / sum));
        sum = 0.0;
        for (int x = 0; x < objs.length; x++) {
            sum += chooser.getProbability(objs[x]);
            chooser.setProbability(objs[x], (float) sum);
        }
        int x;
        for (x = objs.length - 1; x > 0; x--) if (chooser.getProbability(objs[x]) == chooser.getProbability(objs[x - 1])) chooser.setProbability(objs[x], 1.0f); else break;
        chooser.setProbability(objs[x], 1.0f);
    }

    /** Same as organizeDistribution(objs, chooser, <b>false</b>); */
    public static void organizeDistribution(final Object[] objs, final RandomChoiceChooserD chooser) {
        organizeDistribution(objs, chooser, false);
    }

    /** Normalizes the probabilities associated
        with an array of objects, then converts them into continuing
        sums.  This prepares them for being usable in pickFromDistribution.
        If the probabilities are all 0, then selection is uniform, unless allowAllZeros
        is false, in which case an ArithmeticException is thrown.  If any of them are negative,
        or if the distribution is empty, then an ArithmeticException is thrown.
        For example, 
        {0.6, 0.4, 0.2, 0.8} -> {0.3, 0.2, 0.1, 0.4} -> {0.3, 0.5, 0.6, 1.0} 
        The probabilities are retrieved and set using chooser.*/
    public static void organizeDistribution(final Object[] objs, final RandomChoiceChooserD chooser, final boolean allowAllZeros) {
        double sum = 0.0;
        if (objs.length == 0) throw new ArithmeticException("Distribution has no elements");
        for (int x = 0; x < objs.length; x++) {
            if (chooser.getProbability(objs[x]) < 0.0) throw new ArithmeticException("Distribution has negative probabilities");
            sum += chooser.getProbability(objs[x]);
        }
        if (sum == 0.0) if (!allowAllZeros) throw new ArithmeticException("Distribution has all zero probabilities"); else {
            for (int x = 0; x < objs.length; x++) chooser.setProbability(objs[x], 1.0);
            sum = objs.length;
        }
        for (int x = 0; x < objs.length; x++) chooser.setProbability(objs[x], (double) (chooser.getProbability(objs[x]) / sum));
        sum = 0.0;
        for (int x = 0; x < objs.length; x++) {
            sum += chooser.getProbability(objs[x]);
            chooser.setProbability(objs[x], (double) sum);
        }
        int x;
        for (x = objs.length - 1; x > 0; x--) if (chooser.getProbability(objs[x]) == chooser.getProbability(objs[x - 1])) chooser.setProbability(objs[x], 1.0); else break;
        chooser.setProbability(objs[x], 1.0);
    }

    private static final int exemptZeroes(final float[] probabilities, int index) {
        if (probabilities[index] == 0.0f) {
            while (index < probabilities.length - 1 && probabilities[index] == 0.0f) index++;
        } else {
            while (index > 0 && probabilities[index] == probabilities[index - 1]) index--;
        }
        return index;
    }

    private static final int exemptZeroes(final double[] probabilities, int index) {
        if (probabilities[index] == 0.0) {
            while (index < probabilities.length - 1 && probabilities[index] == 0.0) index++;
        } else {
            while (index > 0 && probabilities[index] == probabilities[index - 1]) index--;
        }
        return index;
    }

    private static final int exemptZeroes(final Object[] objs, final RandomChoiceChooser chooser, int index) {
        if (chooser.getProbability(objs[index]) == 0.0f) {
            while (index < objs.length - 1 && chooser.getProbability(objs[index]) == 0.0f) index++;
        } else {
            while (index > 0 && chooser.getProbability(objs[index]) == chooser.getProbability(objs[index - 1])) index--;
        }
        return index;
    }

    private static final int exemptZeroes(final Object[] objs, final RandomChoiceChooserD chooser, int index) {
        if (chooser.getProbability(objs[index]) == 0.0) {
            while (index < objs.length - 1 && chooser.getProbability(objs[index]) == 0.0) index++;
        } else {
            while (index > 0 && chooser.getProbability(objs[index]) == chooser.getProbability(objs[index - 1])) index--;
        }
        return index;
    }

    public static final int CHECKBOUNDARY = 8;

    /** Picks a random item from an array of probabilities,
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < CHECKBOUNDARY,
        then a linear search is used, else a binary search is used. */
    public static int pickFromDistribution(final float[] probabilities, final float prob) {
        return pickFromDistribution(probabilities, prob, CHECKBOUNDARY);
    }

    /** Picks a random item from an array of probabilities,
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < checkboundary,
        then a linear search is used, else a binary search is used.
        @deprecated
    */
    public static int pickFromDistribution(final float[] probabilities, final float prob, final int checkboundary) {
        if (prob < 0.0f || prob > 1.0f) throw new ArithmeticException("Invalid probability for pickFromDistribution (must be 0.0<=x<=1.0)"); else if (probabilities.length == 1) return 0; else if (probabilities.length < checkboundary) {
            for (int x = 0; x < probabilities.length - 1; x++) if (probabilities[x] > prob) return exemptZeroes(probabilities, x);
            return exemptZeroes(probabilities, probabilities.length - 1);
        } else {
            int top = probabilities.length - 1;
            int bottom = 0;
            int cur;
            while (top != bottom) {
                cur = (top + bottom) / 2;
                if (probabilities[cur] > prob) if (cur == 0 || probabilities[cur - 1] <= prob) return exemptZeroes(probabilities, cur); else top = cur; else if (cur == probabilities.length - 1) return exemptZeroes(probabilities, cur); else if (bottom == cur) bottom++; else bottom = cur;
            }
            return exemptZeroes(probabilities, bottom);
        }
    }

    /** Picks a random item from an array of probabilities,
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < CHECKBOUNDARY,
        then a linear search is used, else a binary search is used. */
    public static int pickFromDistribution(final double[] probabilities, final double prob) {
        return pickFromDistribution(probabilities, prob, CHECKBOUNDARY);
    }

    /** Picks a random item from an array of probabilities,
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < checkboundary,
        then a linear search is used, else a binary search is used.
        @deprecated
    */
    public static int pickFromDistribution(final double[] probabilities, final double prob, final int checkboundary) {
        if (prob < 0.0 || prob > 1.0) throw new ArithmeticException("Invalid probability for pickFromDistribution (must be 0.0<=x<=1.0)");
        if (probabilities.length == 1) return 0; else if (probabilities.length < checkboundary) {
            for (int x = 0; x < probabilities.length - 1; x++) if (probabilities[x] > prob) return exemptZeroes(probabilities, x);
            return exemptZeroes(probabilities, probabilities.length - 1);
        } else {
            int top = probabilities.length - 1;
            int bottom = 0;
            int cur;
            while (top != bottom) {
                cur = (top + bottom) / 2;
                if (probabilities[cur] > prob) if (cur == 0 || probabilities[cur - 1] <= prob) return exemptZeroes(probabilities, cur); else top = cur; else if (cur == probabilities.length - 1) return exemptZeroes(probabilities, cur); else if (bottom == cur) bottom++; else bottom = cur;
            }
            return exemptZeroes(probabilities, bottom);
        }
    }

    /** Picks a random item from an array of objects, each with an
        associated probability that is accessed by taking an object
        and passing it to chooser.getProbability(obj).  The objects'
        probabilities are 
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < CHECKBOUNDARY,
        then a linear search is used, else a binary search is used. */
    public static int pickFromDistribution(final Object[] objs, final RandomChoiceChooser chooser, final float prob) {
        return pickFromDistribution(objs, chooser, prob, CHECKBOUNDARY);
    }

    /** Picks a random item from an array of objects, each with an
        associated probability that is accessed by taking an object
        and passing it to chooser.getProbability(obj).  The objects'
        probabilities are 
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < checkboundary,
        then a linear search is used, else a binary search is used.
        @deprecated
    */
    public static int pickFromDistribution(final Object[] objs, final RandomChoiceChooser chooser, final float prob, final int checkboundary) {
        if (prob < 0.0f || prob > 1.0f) throw new ArithmeticException("Invalid probability for pickFromDistribution (must be 0.0<=x<=1.0)");
        if (objs.length == 1) return 0; else if (objs.length < checkboundary) {
            for (int x = 0; x < objs.length - 1; x++) if (chooser.getProbability(objs[x]) > prob) return exemptZeroes(objs, chooser, x);
            return exemptZeroes(objs, chooser, objs.length - 1);
        } else {
            int top = objs.length - 1;
            int bottom = 0;
            int cur;
            while (top != bottom) {
                cur = (top + bottom) / 2;
                if (chooser.getProbability(objs[cur]) > prob) if (cur == 0 || chooser.getProbability(objs[cur - 1]) <= prob) return exemptZeroes(objs, chooser, cur); else top = cur; else if (cur == objs.length - 1) return exemptZeroes(objs, chooser, cur); else if (bottom == cur) bottom++; else bottom = cur;
            }
            return exemptZeroes(objs, chooser, bottom);
        }
    }

    /** Picks a random item from an array of objects, each with an
        associated probability that is accessed by taking an object
        and passing it to chooser.getProbability(obj).  The objects'
        probabilities are 
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < CHECKBOUNDARY,
        then a linear search is used, else a binary search is used. */
    public static int pickFromDistribution(final Object[] objs, final RandomChoiceChooserD chooser, final double prob) {
        return pickFromDistribution(objs, chooser, prob, CHECKBOUNDARY);
    }

    /** Picks a random item from an array of objects, each with an
        associated probability that is accessed by taking an object
        and passing it to chooser.getProbability(obj).  The objects'
        probabilities are 
        normalized and summed as follows:  For example,
        if four probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.  If probabilities.length < checkboundary,
        then a linear search is used, else a binary search is used.
        @deprecated
    */
    public static int pickFromDistribution(final Object[] objs, final RandomChoiceChooserD chooser, final double prob, final int checkboundary) {
        if (prob < 0.0 || prob > 1.0) throw new ArithmeticException("Invalid probability for pickFromDistribution (must be 0.0<=x<=1.0)");
        if (objs.length == 1) return 0; else if (objs.length < checkboundary) {
            for (int x = 0; x < objs.length - 1; x++) if (chooser.getProbability(objs[x]) > prob) return exemptZeroes(objs, chooser, x);
            return exemptZeroes(objs, chooser, objs.length - 1);
        } else {
            int top = objs.length - 1;
            int bottom = 0;
            int cur;
            while (top != bottom) {
                cur = (top + bottom) / 2;
                if (chooser.getProbability(objs[cur]) > prob) if (cur == 0 || chooser.getProbability(objs[cur - 1]) <= prob) return exemptZeroes(objs, chooser, cur); else top = cur; else if (cur == objs.length - 1) return exemptZeroes(objs, chooser, cur); else if (bottom == cur) bottom++; else bottom = cur;
            }
            return exemptZeroes(objs, chooser, bottom);
        }
    }
}
