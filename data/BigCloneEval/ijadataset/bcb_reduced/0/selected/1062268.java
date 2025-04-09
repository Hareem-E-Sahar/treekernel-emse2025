package org.az.hhp.predictors;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.az.hhp.params.ClaimParameter;
import org.az.hhp.params.ProcedureAtPrimaryConditionParam;
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
 * evaluated BestConst:       0.4826941782421123<br>
 * 
 * 
 * using Emphasis bias=1000:
 * evaluated on proofSet     :0.473236700781851<br>
 * evaluated on targetMembers:0.45618083877088<br>
 * 
 * using Emphasis bias=2:<br>
 * and distributionOnDIH<br>
 * evaluated on proofSet     :0.47270784614187283<br>
 * evaluated on targetMembers:0.45561266065726885<br>
 * 
 * 
 * evaluated on proofSet     :0.4727185182386752
 * evaluated on targetMembers:0.4555777865108689
 * 
 * evaluated on proofSet     :0.47017786280012497
 * evaluated on targetMembers:0.45375373288856946
 * 
 * evaluated on proofSet     :0.47002091284666736
 * evaluated on targetMembers:0.45352188409362254
 * 
 * 
 * 
 * 
 * 
 * 
 */
public class PCGImpactAnalyser4 extends AbstractPredictorBase {

    private static final String BEST_CONST_BLEND = "best const blend";

    private static final String DIH_DIVIDER = "dih divider";

    private static final String DIH_FREQUENCY = "dih frequency";

    private PCGDistributionOnDIH distributionOnDIH;

    private static final String FREQ_OF_CLAIM = "claim frequency";

    private static final String NUMBER_OF_CLAIMS = "number of claims";

    private static final String NUMBER_OF_HOSPITALISATIONS = "number of members with dih>0";

    private static final String PROBABILITY_OF_DIH = "probability of dih>0";

    private static final String PREDICTABILITY = "PREDICTABILITY";

    public static void main(final String[] args) throws IOException, NonTrainedException {
        final Year year = Year.Y3;
        final DataImporter dataImporter = DataImporter.instance();
        final Map<Integer, Integer> DIH = dataImporter.loadYearsDataDihOnly(year);
        final Map<Integer, Member> targetMembers = dataImporter.loadTargetSetMembers();
        final Collection<Integer> trainset = TrainSetManager.instance().getTrainSetY3();
        final Collection<Integer> proofSet = TrainSetManager.instance().getProoveSetY3();
        final PCGImpactAnalyser4 pia = new PCGImpactAnalyser4("all");
        pia.train(trainset, proofSet);
        final double rmsle = pia.evaluate(proofSet, DIH, year);
        final double rmsleT = pia.evaluate(targetMembers.keySet(), DIH, year);
        System.out.println("evaluated on proofSet     :" + rmsle);
        System.out.println("evaluated on targetMembers:" + rmsleT);
    }

    private final ClaimsList claimsY1 = DataImporter.instance().loadClaims("Y1_Claims.csv");

    private final ClaimsList claimsY2 = DataImporter.instance().loadClaims("Y2_Claims.csv");

    private final ClaimsList claimsY3 = DataImporter.instance().loadClaims("Y3_Claims.csv");

    private final Map<Integer, Integer> Dih2 = DataImporter.instance().loadYearsDataDihOnly(Year.Y2);

    private final Map<Integer, Integer> Dih3 = DataImporter.instance().loadYearsDataDihOnly(Year.Y3);

    private ClaimParameter parameter;

    private final StatsMatrix sm2;

    private boolean trained = false;

    private Collection<Integer> trainset;

    public PCGImpactAnalyser4(final String name) throws IOException {
        super(name);
        sm2 = new StatsMatrix(6);
    }

    /**
     * part of genome
     */
    private Float dihDivider, bestConstBlend;

    @Override
    public final void cacheGenomeValues() {
        this.dihDivider = genome(DIH_DIVIDER);
        this.bestConstBlend = genome(BEST_CONST_BLEND);
    }

    @Override
    public void genenticalTrain(final Collection<Integer> prooveSet, final Collection<Integer> trainSet, final Map<Integer, Integer> DIH, final Year year) throws IOException {
        train(trainSet, prooveSet);
        final Genetics g = new Genetics(new FitnessFunction() {

            double bestR = Double.MAX_VALUE;

            @Override
            public synchronized double estimate(final List<Double> genome) {
                setParametersGenome(genome);
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
        final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(getClaims(memberId, y));
        if (uniqueDiagnoses.isEmpty()) {
            return 0.21;
        }
        final double[] probabilities = new double[uniqueDiagnoses.size()];
        final StatsMatrix dihD = new StatsMatrix(Consts.MAX_DAYS - 1);
        int i = 0;
        for (final String pcg : uniqueDiagnoses) {
            final double claimProbability = sm2.getValue(pcg, PROBABILITY_OF_DIH);
            probabilities[i] = claimProbability;
            final List<Float> dihDistribution = distributionOnDIH.getDistribution(pcg);
            if (dihDistribution != null) {
                dihD.addValues(pcg, dihDistribution);
                dihD.mulValues(pcg, claimProbability * sm2.getValue(pcg, PREDICTABILITY));
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
        ret = ret / dihDivider;
        ret = tuneWeek(ret);
        ret = ret + bestConstBlend;
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
        this.trainset = trainset;
        if (distributionOnDIH == null) {
            distributionOnDIH = new PCGDistributionOnDIH(trainset, parameter);
        }
        trained = true;
        trainInternal();
        DataImporter.instance().saveStatsMatrix(sm2, this.getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_claimCount_" + getName() + ".csv");
    }

    public void trainInternal() {
        float divider = 0;
        divider += fillMatrix(sm2, claimsY1, Dih2);
        divider += fillMatrix(sm2, claimsY2, Dih3);
        divider += fillMatrix(sm2, claimsY3, null);
        sm2.divideColumn(FREQ_OF_CLAIM, divider);
        sm2.divideColumn(DIH_FREQUENCY, divider);
        sm2.divideColumn(DIH_FREQUENCY, FREQ_OF_CLAIM, PROBABILITY_OF_DIH);
        removeRarePatterns();
        trainPredictability();
    }

    private void trainPredictability() {
        for (final String key : sm2.keySet()) {
            sm2.increaseCount(key, PREDICTABILITY);
        }
        sm2.normalizeColumn(PREDICTABILITY);
        for (int f = 0; f < 6; f++) {
            for (final Integer memberId : Dih3.keySet()) {
                final double dih = Dih3.get(memberId);
                final double predicted = this.predict(memberId, Year.Y3);
                final double diff = Math.log(0.01 + (predicted - dih) * (predicted - dih));
                final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(getClaims(memberId, Year.Y3));
                if (!uniqueDiagnoses.isEmpty()) {
                    for (final String s : uniqueDiagnoses) {
                        if (sm2.keySet().contains(s)) {
                            sm2.increaseCount(s, PREDICTABILITY, (float) (-diff / 100000f));
                        }
                    }
                }
                sm2.normalizeColumn(PREDICTABILITY);
            }
        }
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
    protected void init() {
        parameter = new ProcedureAtPrimaryConditionParam();
        super.init();
    }

    @Override
    protected StatsMatrix initDefaultGenome() {
        final StatsMatrix genomeMatrix = new StatsMatrix(2);
        genomeMatrix.increaseCount(GENOME_ROW, DIH_DIVIDER, 1f);
        genomeMatrix.increaseCount(GENOME_ROW, BEST_CONST_BLEND, 0.00000577f);
        cacheGenomeValues();
        return genomeMatrix;
    }

    @Override
    protected String makeGenomeFilename() {
        return "genome_" + this.getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_" + getName() + ".csv";
    }

    private List<String> collectDiagnoses(final List<Claim> mclaims) {
        final List<String> uniqueDiagnoses = new ArrayList<String>();
        for (final Claim c : mclaims) {
            final String pcg = parameter.valueOf(c);
            uniqueDiagnoses.add(pcg);
        }
        return uniqueDiagnoses;
    }

    private Set<String> collectuniqueDiagnoses(final List<Claim> mclaims) {
        final Set<String> uniqueDiagnoses = new HashSet<String>();
        for (final Claim c : mclaims) {
            final String pcg = parameter.valueOf(c);
            uniqueDiagnoses.add(pcg);
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
        int numberOfMembersProcessed = 0;
        for (final Integer memberId : membersGroup) {
            final List<Claim> mclaims = claimsY.getClaimsByMember(memberId);
            final int dih = DIH == null ? -1 : DIH.get(memberId);
            if (mclaims != null) {
                final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(mclaims);
                if (!uniqueDiagnoses.isEmpty()) {
                    final float p = 1.0f - (float) Math.pow(0.5, 1.0 / uniqueDiagnoses.size());
                    for (final String pcg : uniqueDiagnoses) {
                        sm2.increaseCount(pcg, NUMBER_OF_CLAIMS, 1);
                        sm2.increaseCount(pcg, FREQ_OF_CLAIM, 1);
                        if (DIH != null) {
                            if (dih > 0) {
                                sm2.increaseCount(pcg, DIH_FREQUENCY, p);
                                sm2.increaseCount(pcg, NUMBER_OF_HOSPITALISATIONS, p);
                            }
                        }
                    }
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
