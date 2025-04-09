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
import org.az.hhp.params.ClaimsAfter6MonthsBias;
import org.az.hhp.params.PrimaryConditionGroupParam;
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
 * using Emphasis bias=1000: evaluated on proofSet :0.473236700781851<br>
 * evaluated on targetMembers:0.45618083877088<br>
 * 
 * using Emphasis bias=2:<br>
 * and distributionOnDIH<br>
 * evaluated on proofSet :0.47270784614187283<br>
 * evaluated on targetMembers:0.45561266065726885<br>
 * 
 * 
 * evaluated on proofSet :0.4727185182386752 evaluated on
 * targetMembers:0.4555777865108689
 * 
 * evaluated on proofSet :0.47017786280012497 evaluated on
 * targetMembers:0.45375373288856946
 * 
 * evaluated on proofSet :0.47002091284666736 evaluated on
 * targetMembers:0.45352188409362254
 * 
 * 
 * 
 * 
 * 
 * 
 */
public class PCGImpactAnalyser2 extends AbstractPredictorBase {

    enum Mode {

        EMPHASIZE_PARAM, IGNORE_PARAM, NORMAL
    }

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
        final PCGImpactAnalyser2 pia = new PCGImpactAnalyser2("all", new PrimaryConditionGroupParam());
        pia.train(trainset, proofSet);
        pia.mode = Mode.NORMAL;
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

    private StatsMatrix emphasis = null;

    private Mode mode;

    private final ClaimParameter parameter;

    private String selectedPAram = "xxxxxx";

    private final StatsMatrix sm2;

    private boolean trained = false;

    private Collection<Integer> trainset;

    public PCGImpactAnalyser2(final String name, final ClaimParameter parameter) throws IOException {
        super(name);
        this.parameter = parameter;
        sm2 = new StatsMatrix(5);
        init();
    }

    @Override
    public void genenticalTrain(final Collection<Integer> prooveSet, final Collection<Integer> trainSet, final Map<Integer, Integer> DIH, final Year year) throws IOException {
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
        final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(claims);
        final double[] probabilities = new double[uniqueDiagnoses.size()];
        final StatsMatrix dihD = new StatsMatrix(Consts.MAX_DAYS - 1);
        int i = 0;
        for (final String pcg : uniqueDiagnoses) {
            final double claimProbability = sm2.getValue(pcg, PROBABILITY_OF_DIH);
            if (Double.isNaN(claimProbability)) {
                throw new RuntimeException("cant be NAN");
            }
            probabilities[i] = claimProbability;
            final List<Float> dihDistribution = distributionOnDIH.getDistribution(pcg);
            if (dihDistribution != null) {
                dihD.addValues(pcg, dihDistribution);
                dihD.mulValues(pcg, claimProbability);
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
        if (emphasis == null) {
            evaluateOrLoadEmphasis(proofSet);
        }
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
        final StatsMatrix genomeMatrix = new StatsMatrix(4);
        genomeMatrix.increaseCount(GENOME_ROW, "dih divider", 1.199f);
        genomeMatrix.increaseCount(GENOME_ROW, "best const blend", 0.0577f);
        genomeMatrix.increaseCount(GENOME_ROW, "bias1", 3f);
        genomeMatrix.increaseCount(GENOME_ROW, "bias2", 5f);
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
            if (mode == Mode.NORMAL) {
                uniqueDiagnoses.add(pcg);
            } else if (mode == Mode.EMPHASIZE_PARAM) {
                uniqueDiagnoses.add(pcg);
                if (selectedPAram.equals(pcg)) {
                    uniqueDiagnoses.add(pcg + "powered");
                }
            } else if (mode == Mode.IGNORE_PARAM) {
                if (!selectedPAram.equals(pcg)) {
                    uniqueDiagnoses.add(pcg);
                }
            } else {
                throw new RuntimeException("illegal mode");
            }
        }
        return uniqueDiagnoses;
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

    private void evaluateOrLoadEmphasis(final Collection<Integer> prooveSetY3) {
        emphasis = DataImporter.instance().loadStatsMatrix(makeEmphasisFilename());
        if (emphasis == null) {
            emphasis = evaluateEmphasis(prooveSetY3);
        }
        mode = Mode.NORMAL;
        trainInternal();
    }

    private StatsMatrix evaluateEmphasis(final Collection<Integer> prooveSetY3) {
        selectedPAram = "XXXXX";
        Map<Integer, Integer> dih;
        final Year year = Year.Y3;
        try {
            dih = DataImporter.instance().loadYearsDataDihOnly(year);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        mode = Mode.NORMAL;
        trainInternal();
        final Set<String> keys = new HashSet<String>();
        keys.addAll(sm2.keySet());
        final StatsMatrix emphasis = new StatsMatrix(4);
        try {
            trainInternal();
            final double RMSLE = evaluate(prooveSetY3, dih, year);
            System.out.println("evaluated on proofSet   \t\t" + RMSLE);
            for (final String p2i : keys) {
                selectedPAram = p2i;
                {
                    mode = Mode.IGNORE_PARAM;
                    trainInternal();
                    final double rmsleI = evaluate(prooveSetY3, dih, year);
                    System.out.println("evaluated on proofSet   \t\t" + rmsleI + "\t" + selectedPAram);
                    emphasis.increaseCount(p2i, Mode.IGNORE_PARAM.toString(), (float) rmsleI);
                }
                {
                    mode = Mode.EMPHASIZE_PARAM;
                    trainInternal();
                    final double rmsleE = evaluate(prooveSetY3, dih, year);
                    System.out.println("evaluated on proofSet   \t\t" + rmsleE + "\t" + selectedPAram);
                    emphasis.increaseCount(p2i, Mode.EMPHASIZE_PARAM.toString(), (float) rmsleE);
                }
            }
            for (final String p2i : keys) {
                final Float value2 = emphasis.getValue(p2i, Mode.IGNORE_PARAM.toString());
                final Float value3 = emphasis.getValue(p2i, Mode.EMPHASIZE_PARAM.toString());
                emphasis.setValue(p2i, "k1 (ignore)", (float) (value2 / RMSLE));
                emphasis.setValue(p2i, "k2 (emphasize)", (float) (RMSLE / value3));
            }
            DataImporter.instance().saveStatsMatrix(emphasis, makeEmphasisFilename());
        } catch (final NonTrainedException e) {
            throw new RuntimeException(e);
        }
        return emphasis;
    }

    private String makeEmphasisFilename() {
        return "emphasis-" + this.getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_" + getName() + ".csv";
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
        final float bias2 = genome("bias2");
        int numberOfMembersProcessed = 0;
        for (final Integer memberId : membersGroup) {
            final List<Claim> mclaims = claimsY.getClaimsByMember(memberId);
            final int dih = DIH == null ? -1 : DIH.get(memberId);
            if (mclaims != null) {
                final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(mclaims);
                if (!uniqueDiagnoses.isEmpty()) {
                    final float p = 1.0f - (float) Math.pow(0.5, 1.0 / uniqueDiagnoses.size());
                    for (final String pcg : uniqueDiagnoses) {
                        float emphasisVlaue1 = 1;
                        float emphasisVlaue2 = 1;
                        if (emphasis != null) {
                            emphasisVlaue1 = emphasis.getValue(pcg, "k1 (ignore)");
                            emphasisVlaue2 = emphasis.getValue(pcg, "k2 (emphasize)");
                            emphasisVlaue1 = (float) Math.pow(emphasisVlaue1, bias1);
                            emphasisVlaue2 = (float) Math.pow(emphasisVlaue2, bias2);
                        }
                        if (Double.isNaN(emphasisVlaue2) || Double.isNaN(emphasisVlaue1)) {
                            throw new RuntimeException("emphasisVlaue can not be NAN");
                        }
                        sm2.increaseCount(pcg, NUMBER_OF_CLAIMS, 1);
                        sm2.increaseCount(pcg, FREQ_OF_CLAIM, 1);
                        if (DIH != null) {
                            if (dih > 0) {
                                sm2.increaseCount(pcg, DIH_FREQUENCY, p * emphasisVlaue1 * emphasisVlaue2);
                                sm2.increaseCount(pcg, NUMBER_OF_HOSPITALISATIONS, p * emphasisVlaue1 * emphasisVlaue2);
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
