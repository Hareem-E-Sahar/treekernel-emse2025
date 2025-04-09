package jaga.evolve;

import jaga.BitSet;
import java.util.Random;

/** This Library contains useful functions used within the evolve package.
 *
 * @author  Michael Garvie
 * @version 
 */
public abstract class EvolveLib extends Object {

    static Random rnd = new Random();

    /** Chooses a position from an array according to the cumulative 
     * probabilities stored in that array.  cumProb[ 0 ] must be equal to 0 and
     * cumProb[ 1 ] will be equal to the first cumulative frequency (fitness or rank value)
     * of the first individual.  
     * <p>Note cumProb.length = pop.size() + 1.  The index returned by this procedure
     * is in the range [ 0, cumProb.length - 2 ] since there is an extra element.
     * <p>A random number r is generated and this method searches the array for 
     * the position p such that cumProb[ p ] < r < cumProb[ p + 1 ].  Binary 
     * chop algorithm is used to guarantee O(log(n)) time of search.
     * WARNING: does not work with negative probabilities!
     */
    public static int pickPosition(double[] cumProb) {
        int roof = cumProb.length - 1;
        int floor = 1;
        double totalProb = cumProb[roof];
        double r = rnd.nextDouble() * totalProb;
        int pos = (floor + roof) / 2;
        boolean inUpper = false, inLower = false;
        while (!(inUpper && inLower)) {
            inUpper = r <= cumProb[pos];
            inLower = r >= cumProb[pos - 1];
            if (!inUpper) {
                floor = pos + 1;
            } else if (!inLower) {
                roof = pos - 1;
            }
            pos = (roof + floor) / 2;
        }
        return pos - 1;
    }

    public static BitSet randomize(BitSet blob) {
        for (int bitLoop = 0; bitLoop < blob.length(); bitLoop++) {
            blob.setTo(bitLoop, rnd.nextBoolean());
        }
        return blob;
    }
}
