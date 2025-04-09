package net.boogie.calamari.genetic.function.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.boogie.calamari.genetic.model.IGene;
import net.boogie.calamari.genetic.model.IGenome;
import org.apache.log4j.Logger;

public class ShiftMutationFunction extends AbstractMutationFunction<IGenome> {

    private static Logger s_log = Logger.getLogger(ShiftMutationFunction.class);

    private int _maxDelta;

    private static Random s_rnd = new Random();

    public ShiftMutationFunction(double mutationRate, int maxDelta) {
        super(mutationRate);
        _maxDelta = maxDelta;
    }

    @Override
    public IGenome mutate(IGenome genome) {
        double mutationRate = getMutationRate();
        if (s_rnd.nextDouble() < mutationRate) {
            int maxDelta = Math.abs(getMaxDelta());
            int delta = s_rnd.nextInt(maxDelta + 1);
            if (delta == 0) {
                return genome;
            }
            if (s_rnd.nextBoolean()) {
                delta = -delta;
            }
            List<IGene> genes = genome.getGenes();
            if (delta % genes.size() == 0) {
                return genome;
            }
            List<IGene> childGenes = Collections.synchronizedList(new ArrayList<IGene>(genes));
            Collections.rotate(childGenes, delta);
            IGenome childGenome = genome.createClone(true);
            childGenome.setGenes(childGenes);
            if (s_log.isDebugEnabled()) {
                s_log.debug("[MUTATE]  Shifted " + genome + " [" + delta + "] --> " + childGenome);
            }
            return childGenome;
        } else {
            return genome;
        }
    }

    protected int getMaxDelta() {
        return _maxDelta;
    }
}
