package org.opt4j.operator.mutate;

import java.util.Collections;
import java.util.Random;
import org.opt4j.common.random.Rand;
import org.opt4j.genotype.PermutationGenotype;
import com.google.inject.Inject;

/**
 * <p>
 * Mutate for the {@link PermutationGenotype}. With a given mutation rate two
 * elements are selected and the list between is inverted.
 * </p>
 * 
 * <p>
 * Given a permutation {@code 1 2 3 4 5 6 7 8}, this might result in {@code 1 2
 * 7 6 5 3 4}.
 * </p>
 * 
 * @author lukasiewycz
 * 
 */
public class MutatePermutationRevert implements MutatePermutation {

    protected final Random random;

    /**
	 * Constructs a new {@link MutatePermutation} with the given mutation rate.
	 * 
	 * @param random
	 *            the random number generator
	 */
    @Inject
    public MutatePermutationRevert(Rand random) {
        this.random = random;
    }

    @Override
    public void mutate(PermutationGenotype<?> genotype, double p) {
        int size = genotype.size();
        if (size > 1) {
            for (int a = 0; a < size - 1; a++) {
                if (random.nextDouble() < p) {
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
    }
}
