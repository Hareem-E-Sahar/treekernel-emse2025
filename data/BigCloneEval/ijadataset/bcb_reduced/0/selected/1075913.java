package mil.army.usace.ehlschlaeger.rgik.util;

import java.util.Random;

/**
 * Index into ranges of real numbers, normalized to [0,1).
 * Ranges are not arbitrary (this is object is not a map); each range is
 * described only by a size, and the ends are calculated by summing consecutive
 * sizes.  The grand total is then normalized to the range [0.0, 1.0).
 * <P>
 * For example, (1,2,3,4) gets normalized to (0, .1, .3, .6), which represents
 * the ranges [0, .1), [.1, .3), [.3, .6), and [.6, 1.0).  Calling get(0.5)
 * will then return 2, which is the index of the range covering that value.
 * <P>
 * The notation "[<i>a</i>,<i>b</i>)" represents the set of real numbers <i>x</i>
 * where <i>a</i> <= <i>x</i> < <i>b</i>.
 * <p>
 * Copyright <a href="http://faculty.wiu.edu/CR-Ehlschlaeger2/">Charles R.
 * Ehlschlaeger</a>, work: 309-298-1841, fax: 309-298-3003, This software is
 * freely usable for research and educational purposes. Contact C. R.
 * Ehlschlaeger for permission for other purposes. Use of this software requires
 * appropriate citation in all published and unpublished documentation.
 *
 * @author William R. Zwicky
 */
public class NormalizedDoubleRangeIndex {

    /**
     * Value at bottom of each range.
     */
    protected double[] lo_end;

    /**
     * Construct instance where each range has the given size.
     * 
     * @param ranges list of range sizes to index
     */
    public NormalizedDoubleRangeIndex(double[] ranges) {
        this.lo_end = new double[ranges.length];
        double total = 0;
        for (int i = 0; i < ranges.length; i++) total += ranges[i];
        double sum = 0;
        for (int i = 0; i < ranges.length; i++) {
            this.lo_end[i] = sum / total;
            sum += ranges[i];
        }
    }

    /**
     * Get the index of the range that covers the given value.
     * 
     * @param value number to locate
     * @return int index
     */
    public int get(double value) {
        if (value < 0.0 || value > 1.0) throw new IllegalArgumentException("value must be in the range [0,1].");
        int minCell = 0;
        int maxCell = lo_end.length - 1;
        while (maxCell - 1 > minCell) {
            int checkCell = (maxCell + minCell) / 2;
            double cellValue = lo_end[checkCell];
            if (value < cellValue) {
                maxCell = checkCell;
            } else {
                minCell = checkCell;
            }
        }
        return minCell;
    }

    /**
     * Choose an index at random, where the size of each range gives the 
     * probability that index will be chosen.
     * 
     * @param source random-number generator to poll
     * @return int index
     */
    public int get(Random source) {
        return get(source.nextDouble());
    }
}
