package org.opt4j.operator.neighbor;

import java.util.Collections;
import java.util.Random;
import org.opt4j.common.random.Rand;
import org.opt4j.genotype.PermutationGenotype;
import com.google.inject.Inject;

/**
 * <p>
 * Neighbor for the {@link PermutationGenotype}. Reverts a sublist.
 * </p>
 * 
 * <p>
 * Given a permutation {@code 1 2 3 4 5 6 7 8}, this might result in
 * {@code 1 2 7 6 5 4 3 8}.
 * </p>
 * 
 * @author lukasiewycz
 * 
 */
public class NeighborPermutationRevert implements NeighborPermutation {

    protected final Random random;

    /**
	 * Constructs a {@link NeighborPermutationRevert} operator for the
	 * {@link PermutationGenotype}.
	 * 
	 * @param random
	 *            the random number generator
	 */
    @Inject
    public NeighborPermutationRevert(Rand random) {
        this.random = random;
    }

    @Override
    public void neighbor(PermutationGenotype<?> genotype) {
        int size = genotype.size();
        if (size > 1) {
            int a = random.nextInt(size - 1);
            int b;
            do {
                b = a + random.nextInt(size - a);
            } while (b == a);
            while (a < b) {
                Collections.swap(genotype, a, b);
                a++;
                b--;
            }
        }
    }
}
