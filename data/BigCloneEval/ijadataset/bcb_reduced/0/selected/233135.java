package org.az.hhp.predictors;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import org.az.hhp.collections.StatsMatrix;
import org.az.hhp.interfaces.NonTrainedException;
import org.az.hhp.tools.Year;

/**
 * 
  * 
 */
public class PredictorFitting extends AbstractPredictorBase implements ClusterSpecific {

    private final AbstractPredictorBase subPredictor;

    public AbstractPredictorBase getSubPredictor() {
        return subPredictor;
    }

    public PredictorFitting(final String name, final AbstractPredictorBase subPredictor) throws IOException {
        super(name);
        this.subPredictor = subPredictor;
        init();
        if (genomeMatrix.getDimension() != initDefaultGenome().getDimension()) {
            genomeMatrix = initDefaultGenome();
            cacheGenomeValues();
        }
    }

    /**
     * cache is used on training phase only
     */
    private final HashMap<Integer, Double> predictionsCache = new HashMap<Integer, Double>();

    private int cacheExpirationCount;

    /**
     * returns probability of DIH>0
     * 
     * @param memberId
     * @param y
     * @return
     * @throws NonTrainedException 
     */
    public double getProbalilityOfHospitalisation(final Integer memberId, final Year y) throws NonTrainedException {
        final Double cached = predictionsCache.get(memberId);
        double summ = 0;
        if (cached == null) {
            summ = subPredictor.predict(memberId, y);
            predictionsCache.put(memberId, summ);
        } else {
            cacheExpirationCount++;
            summ = cached;
        }
        summ = (Math.pow(summ + 1f, genome("power")) - 1f) * genome("mult");
        final float bestConstBlend = genome("addon");
        summ = summ + bestConstBlend;
        summ = tuneWeek(summ);
        return summ;
    }

    private double tuneWeek(double ret) {
        final double blend = 0.4;
        if ((int) Math.round(ret) == 6 || (int) Math.round(ret) == 8) {
            ret = (ret * blend + 7 * (1 - blend));
        }
        if (Math.round(ret) == 13 || Math.round(ret) == 15) {
            ret = (ret + 14) / 2;
        }
        return ret;
    }

    @Override
    public synchronized double predict(final Integer memberId, final Year yearToPredict) throws NonTrainedException {
        final double ret = getProbalilityOfHospitalisation(memberId, yearToPredict);
        if (ret < 0) {
            return 0;
        }
        if (ret > 15) {
            System.err.println(ret);
            return 15;
        }
        return ret;
    }

    @Override
    public void train(final Collection<Integer> trainset, final Collection<Integer> proofSet) throws IOException {
        predictionsCache.clear();
        subPredictor.train(trainset, proofSet);
    }

    @Override
    protected void cacheGenomeValues() {
        super.cacheGenomeValues();
    }

    @Override
    protected StatsMatrix initDefaultGenome() {
        final StatsMatrix genomeMatrix = new StatsMatrix(3);
        genomeMatrix.increaseCount(GENOME_ROW, "power", 1f);
        genomeMatrix.increaseCount(GENOME_ROW, "mult", 1f);
        genomeMatrix.increaseCount(GENOME_ROW, "addon", 0f);
        return genomeMatrix;
    }

    @Override
    protected String makeGenomeFilename() {
        return "genome_FITTING_" + this.getClass().getSimpleName() + "-" + this.subPredictor.makeGenomeFilename() + getName() + ".csv";
    }
}
