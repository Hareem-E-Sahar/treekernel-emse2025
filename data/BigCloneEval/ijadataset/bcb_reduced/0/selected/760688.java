package org.az.hhp.predictors;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.az.hhp.Consts;
import org.az.hhp.collections.StatsMatrix;
import org.az.hhp.domain.Claim;
import org.az.hhp.domain.ClaimsList;
import org.az.hhp.domain.Member;
import org.az.hhp.interfaces.NonTrainedException;
import org.az.hhp.params.CharlsonIndexAndPCGComboParam;
import org.az.hhp.params.ClaimParameter;
import org.az.hhp.params.ClaimsAfter6MonthsBias;
import org.az.hhp.params.MonthDetector;
import org.az.hhp.tools.ArraysTools;
import org.az.hhp.tools.DataImporter;
import org.az.hhp.tools.Genetics;
import org.az.hhp.tools.Genetics.FitnessFunction;
import org.az.hhp.tools.TrainSetManager;
import org.az.hhp.tools.Year;

/**
 * Analyze primary conditions groups statistics among those who spent more than
 * 0 days in hospital in y2,y3.
 * 
 * evaluated BestConst: 0.4826941782421123<br>
 * 
 * 
 * 
 * PCGImpactAnalyser2 :0.47002091284666736
 * 
 * 
 * evaluated on proofSet : 0.470303412050473 0.4701493590798302
 * 
 * 
 * evaluated on targetMembers:0.4553491992718777
 * 
 * 
 * 
 * 
 * 
 * 
 */
public class PCGImpactAnalyser5 extends AbstractPredictorBase {

    private static ClaimsAfter6MonthsBias claimsAfter6MonthsBias;

    private static final String DIH_FREQUENCY = "dih frequency";

    private PCGDistributionOnDIH distributionOnDIH;

    private static final String FREQ_OF_CLAIM = "claim frequency";

    private static final String NUMBER_OF_CLAIMS = "number of claims";

    private static final String NUMBER_OF_HOSPITALISATIONS = "number of members with dih>0";

    private static final String PROBABILITY_OF_DIH = "probability of dih>0";

    public static void main(final String[] args) throws IOException, NonTrainedException {
        final Year year = Year.Y3;
        final DataImporter dataImporter = DataImporter.instance();
        final Map<Integer, Integer> DIH = dataImporter.loadYearsDataDihOnly(year);
        final Map<Integer, Member> targetMembers = dataImporter.loadTargetSetMembers();
        final Collection<Integer> trainset = TrainSetManager.instance().getTrainSetY3();
        final Collection<Integer> proofSet = TrainSetManager.instance().getProoveSetY3();
        final PCGImpactAnalyser5 pia = new PCGImpactAnalyser5("all", new CharlsonIndexAndPCGComboParam());
        pia.train(trainset, proofSet);
        final double rmsle = pia.evaluate(proofSet, DIH, year);
        final double rmsleT = pia.evaluate(targetMembers.keySet(), DIH, year);
        System.out.println("evaluated on proofSet     :" + rmsle);
        System.out.println("evaluated on targetMembers:" + rmsleT);
        pia.genenticalTrain(proofSet, trainset, DIH, year);
    }

    private final ClaimsList claimsY1 = DataImporter.instance().loadClaims("Y1_Claims.csv");

    private final ClaimsList claimsY2 = DataImporter.instance().loadClaims("Y2_Claims.csv");

    private final ClaimsList claimsY3 = DataImporter.instance().loadClaims("Y3_Claims.csv");

    private final Map<Integer, Integer> Dih2 = DataImporter.instance().loadYearsDataDihOnly(Year.Y2);

    private final Map<Integer, Integer> Dih3 = DataImporter.instance().loadYearsDataDihOnly(Year.Y3);

    private final ClaimParameter parameter;

    private final String selectedPAram = "xxxxxx";

    private final StatsMatrix sm2;

    private boolean trained = false;

    private Collection<Integer> trainset;

    public PCGImpactAnalyser5(final String name, final ClaimParameter parameter) throws IOException {
        super(name);
        this.parameter = parameter;
        sm2 = new StatsMatrix(5);
        init();
        if (genomeMatrix.getDimension() != initDefaultGenome().getDimension()) {
            genomeMatrix = initDefaultGenome();
            cacheGenomeValues();
        }
    }

    private double emphasisOnMainPCG = 3;

    @Override
    protected void cacheGenomeValues() {
        super.cacheGenomeValues();
        try {
            emphasisOnMainPCG = genome("emphasis on main PCG");
        } catch (final Exception e) {
            System.err.println("genome has changed, re-training REQUIRED");
        }
    }

    public void _genenticalTrain(final Collection<Integer> prooveSet, final Collection<Integer> trainSet, final Map<Integer, Integer> DIH, final Year year) throws IOException {
        train(trainSet, prooveSet);
        final Genetics g = new Genetics(new FitnessFunction() {

            double bestR = Double.MAX_VALUE;

            @Override
            public synchronized double estimate(final List<Double> genome) {
                setParametersGenome(genome);
                trainInternal();
                double rmsleee;
                try {
                    rmsleee = evaluate(prooveSet, DIH, year);
                    if (rmsleee < bestR) {
                        bestR = rmsleee;
                        saveGenome();
                    }
                    System.out.println(rmsleee);
                    return rmsleee;
                } catch (final NonTrainedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, getParametersGenome().size(), 32);
        g.seed(getParametersGenome());
        final PrintWriter out = new PrintWriter(new FileWriter(DataImporter.instance().file("learning_" + getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_" + getName() + ".txt")));
        g.multiThreadProcess(out, "T-" + getName());
    }

    public ClaimParameter getParameter() {
        return parameter;
    }

    private String getMainDiagnosis(final List<Claim>... claimsListOfLists) {
        final StatsMatrix sm = new StatsMatrix(1);
        for (final List<Claim> claims : claimsListOfLists) {
            if (claims != null) {
                for (final Claim c : claims) {
                    final String s = parameter.valueOf(c);
                    sm.increaseCount(s, 0);
                }
            }
        }
        return sm.max(0);
    }

    /**
     * returns probability of DIH>0
     * 
     * @param memberId
     * @param y
     * @return
     */
    public double getProbalilityOfHospitalisation(final Integer memberId, final Year y) {
        if (!trained) {
            throw new RuntimeException("not trained");
        }
        final List<Claim> claims = getClaims(memberId, y);
        final List<Claim> prevClaims = getClaims(memberId, y.prev());
        List<Claim> prevPrevClaims = null;
        if (y.prev() != null) {
            prevPrevClaims = getClaims(memberId, y.prev().prev());
        }
        final String main = getMainDiagnosis(claims, prevClaims, prevPrevClaims);
        final double[] probabilities = new double[claims.size()];
        final StatsMatrix dihD = new StatsMatrix(Consts.MAX_DAYS - 1);
        final List<Byte> dsfs = MonthDetector.claimMonth(claims);
        int i = 0;
        for (final Claim claim : claims) {
            double bias = ClaimsAfter6MonthsBias.bias(dsfs.get(i));
            if (claim.getDSFS() == 0) {
                bias = 1;
            } else {
                final float bias2 = genome("bias2");
                bias = Math.pow(bias, bias2);
            }
            final String pcg = parameter.valueOf(claim);
            double emphasisOnMain = 1;
            if (pcg.equals(main)) {
                emphasisOnMain = 1d + (emphasisOnMainPCG / claims.size());
            }
            final double claimProbability = bias * sm2.getValue(pcg, PROBABILITY_OF_DIH);
            if (Double.isNaN(claimProbability)) {
                throw new RuntimeException("cant be NAN");
            }
            probabilities[i] = claimProbability;
            final List<Float> dihDistribution = distributionOnDIH.getDistribution(pcg);
            if (dihDistribution != null) {
                dihD.addValues(pcg, dihDistribution);
                dihD.mulValues(pcg, claimProbability * emphasisOnMain);
            }
            i++;
        }
        double ret = ArraysTools.probalility(probabilities);
        if (Double.isNaN(ret)) {
            throw new RuntimeException("cant be NAN");
        }
        if (!dihD.keySet().isEmpty()) {
            double numberOfDays = 0;
            final List<Float> dihDistribution = dihD.getScoresForEachDimension();
            ArraysTools.normalize(dihDistribution);
            for (int f = 0; f < dihDistribution.size(); f++) {
                numberOfDays += (f + 1) * dihDistribution.get(f);
            }
            ret *= numberOfDays;
        }
        ret *= claimsAfter6MonthsBias.getValue(claims);
        return ret;
    }

    @Override
    public synchronized double predict(final Integer memberId, final Year yearToPredict) {
        final double ret = getProbalilityOfHospitalisation(memberId, yearToPredict);
        if (ret < 0) {
            return 0;
        }
        return ret;
    }

    @Override
    public void train(final Collection<Integer> trainset, final Collection<Integer> proofSet) {
        System.out.println("trining " + getName() + " trainset.size=" + trainset.size() + " proofSet.size=" + proofSet.size());
        this.trainset = trainset;
        if (distributionOnDIH == null) {
            distributionOnDIH = new PCGDistributionOnDIH(trainset, parameter);
        }
        if (claimsAfter6MonthsBias == null) {
            claimsAfter6MonthsBias = new ClaimsAfter6MonthsBias();
        }
        trained = true;
        trainInternal();
    }

    public void trainInternal() {
        float divider = 0;
        divider += fillMatrix(sm2, claimsY1, Dih2);
        divider += fillMatrix(sm2, claimsY2, Dih3);
        divider += fillMatrix(sm2, claimsY3, null);
        removeRarePatterns();
        sm2.divideColumn(FREQ_OF_CLAIM, divider);
        sm2.divideColumn(DIH_FREQUENCY, divider);
        sm2.divideColumn(DIH_FREQUENCY, FREQ_OF_CLAIM, PROBABILITY_OF_DIH);
    }

    private void removeRarePatterns() {
        final HashSet<String> keysToRemove = new HashSet<String>();
        for (final String key : sm2.keySet()) {
            if (sm2.getValue(key, NUMBER_OF_CLAIMS) < 20) {
                keysToRemove.add(key);
            }
        }
        for (final String key : keysToRemove) {
            sm2.removeRow(key);
        }
    }

    @Override
    protected StatsMatrix initDefaultGenome() {
        final StatsMatrix genomeMatrix = new StatsMatrix(5);
        genomeMatrix.increaseCount(GENOME_ROW, "dih divider", 1.33f);
        genomeMatrix.increaseCount(GENOME_ROW, "best const blend", 0.0535f);
        genomeMatrix.increaseCount(GENOME_ROW, "bias1", 1f);
        genomeMatrix.increaseCount(GENOME_ROW, "bias2", 1.777f);
        genomeMatrix.increaseCount(GENOME_ROW, "emphasis on main PCG", 20f);
        return genomeMatrix;
    }

    @Override
    protected String makeGenomeFilename() {
        return "genome_" + this.getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_" + getName() + ".csv";
    }

    @Deprecated
    private Set<String> collectuniquePairs(final List<Claim> mclaims) {
        final Set<String> uniqueDiagnoses = new HashSet<String>();
        for (final Claim c : mclaims) {
            final String pcg = parameter.valueOf(c);
            for (final Claim c2 : mclaims) {
                final String pcg2 = parameter.valueOf(c2);
                if (pcg2.compareTo(pcg) > 0) {
                    uniqueDiagnoses.add(pcg + "&" + pcg2);
                }
            }
        }
        return uniqueDiagnoses;
    }

    private int fillMatrix(final StatsMatrix sm2, final ClaimsList claimsY, final Map<Integer, Integer> DIH) {
        final Set<Integer> membersGroup = new HashSet<Integer>();
        if (DIH != null) {
            membersGroup.addAll(DIH.keySet());
        } else {
            membersGroup.addAll(claimsY.getClaimsByMemberMap().keySet());
        }
        membersGroup.retainAll(trainset);
        final float bias1 = genome("bias1");
        int numberOfMembersProcessed = 0;
        for (final Integer memberId : membersGroup) {
            final List<Claim> mclaims = claimsY.getClaimsByMember(memberId);
            final int dih = DIH == null ? -1 : DIH.get(memberId);
            if (mclaims != null && !mclaims.isEmpty()) {
                final float p = 1.0f - (float) Math.pow(0.5, 1d / mclaims.size());
                final List<Byte> dsfs = MonthDetector.claimMonth(mclaims);
                int claimNo = 0;
                for (final Claim claim : mclaims) {
                    float mul = ClaimsAfter6MonthsBias.bias(dsfs.get(claimNo));
                    if (claim.getDSFS() == 0) {
                        mul = 1;
                    } else {
                        mul *= mul;
                    }
                    final String pcg = parameter.valueOf(claim);
                    sm2.increaseCount(pcg, NUMBER_OF_CLAIMS, 1);
                    sm2.increaseCount(pcg, FREQ_OF_CLAIM, 1);
                    if (DIH != null) {
                        if (dih > 0) {
                            sm2.increaseCount(pcg, DIH_FREQUENCY, p * mul);
                            sm2.increaseCount(pcg, NUMBER_OF_HOSPITALISATIONS, p * mul);
                        }
                    }
                    claimNo++;
                }
            }
            numberOfMembersProcessed++;
        }
        return numberOfMembersProcessed;
    }

    private List<Claim> getClaims(final Integer memberId, final Year y) {
        List<Claim> mclaims = null;
        if (y == Year.Y3) {
            mclaims = claimsY2.getClaimsByMember(memberId);
        } else if (y == Year.Y2) {
            mclaims = claimsY1.getClaimsByMember(memberId);
        } else {
            try {
                mclaims = DataImporter.instance().loadClaims("Y3_Claims.csv").getClaimsByMember(memberId);
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return mclaims;
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
}
