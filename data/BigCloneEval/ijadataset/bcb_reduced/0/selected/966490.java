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
import org.az.hhp.domain.Target;
import org.az.hhp.interfaces.NonTrainedException;
import org.az.hhp.params.ClaimParameter;
import org.az.hhp.params.PrimaryConditionGroupParam;
import org.az.hhp.tools.ArraysTools;
import org.az.hhp.tools.DataImporter;
import org.az.hhp.tools.Genetics;
import org.az.hhp.tools.Genetics.FitnessFunction;
import org.az.hhp.tools.PredictionContext;
import org.az.hhp.tools.Year;

/**
 * Analyze primary conditions groups statistics among those who spent more than
 * 0 days in hospital in y2,y3.
 * 
 * SITE RMSLE ************0.480374********************
 * 
 */
public class PCGImpactAnalyser extends AbstractPredictorBase {

    private static final int MAX_DIH_THRESHOLD = 5;

    private static final String HAVE_DIH_MORE = "number of claims of users who have DIH = ";

    private static final int MAX_DIH = 16;

    private static final String NUMBER_OF_CLAIMS = "number of claims";

    final ClaimsList claimsY1 = DataImporter.instance().loadClaims("Y1_Claims.csv");

    final ClaimsList claimsY2 = DataImporter.instance().loadClaims("Y2_Claims.csv");

    final ClaimsList claimsY3 = DataImporter.instance().loadClaims("Y3_Claims.csv");

    final Map<Integer, Integer> Dih2 = DataImporter.instance().loadYearsDataDihOnly(Year.Y2);

    final Map<Integer, Integer> Dih3 = DataImporter.instance().loadYearsDataDihOnly(Year.Y3);

    final StatsMatrix sm;

    private ClaimParameter parameter;

    private boolean trained = false;

    private Collection<Integer> trainset;

    public static void main(final String[] args) throws IOException, NonTrainedException {
        final PCGImpactAnalyser pia = new PCGImpactAnalyser("all");
        final Year year = Year.Y3;
        final DataImporter dataImporter = DataImporter.instance();
        final Map<Integer, Integer> DIH = dataImporter.loadYearsDataDihOnly(year);
        final Map<Integer, Member> targetMembers = dataImporter.loadTargetSetMembers();
        final Map<Integer, Member> m = dataImporter.loadMembers();
        final Set<Integer> trainSet = new HashSet<Integer>();
        final Set<Integer> prooveSet1 = new HashSet<Integer>();
        prooveSet1.addAll(targetMembers.keySet());
        prooveSet1.retainAll(DIH.keySet());
        final Set<Integer> prooveSet = PredictionContext.getRandomPortion(prooveSet1, 0.7);
        trainSet.addAll(m.keySet());
        trainSet.removeAll(prooveSet);
        System.out.println("trainSet.size  =" + trainSet.size());
        System.out.println("prooveSet.size =" + prooveSet.size());
        pia.train(trainSet);
        final double rmsle = pia.evaluate(prooveSet, DIH, year);
        System.out.println(rmsle);
        final double rmsleT = pia.evaluate(targetMembers.keySet(), DIH, year);
        System.out.println(rmsleT);
        final List<Target> targets = dataImporter.loadTargetTable(dataImporter.file("Target.csv"));
        for (final Target target : targets) {
            final Integer memberId = target.getMemberId();
            final double predictedvalue = pia.predict(memberId, Year.Y4);
            target.setPredictedValue(predictedvalue);
        }
        dataImporter.saveTargeFile("Target_Y4_" + pia.getClass().getSimpleName() + ".csv", targets);
    }

    private static double tuneWeek(double ret) {
        final double blend = 0.4;
        if ((int) Math.round(ret) == 6 || (int) Math.round(ret) == 8) {
            ret = (ret * blend + 7 * (1 - blend));
        }
        if (Math.round(ret) == 13 || Math.round(ret) == 15) {
            ret = (ret + 14) / 2;
        }
        return ret;
    }

    public PCGImpactAnalyser(final String name) throws IOException {
        super(name);
        sm = new StatsMatrix(MAX_DIH + 1);
        for (int dih = 0; dih < Consts.MAX_DAYS; dih++) {
            sm.getColumnNames().getCode(HAVE_DIH_MORE + dih);
        }
    }

    public double blendProbabilitiesOnIndex(final double[] ppi) {
        double ret = 0;
        ArraysTools.normalize(ppi);
        for (int f = 0; f < ppi.length; f++) {
            ret += f * ppi[f];
        }
        ret = ret / genome("dih divider");
        ret = tuneWeek(ret);
        final float bestConstBlend = genome("best const blend");
        ret = ret + bestConstBlend;
        if (Double.isNaN(ret)) {
            throw new RuntimeException("cant be NAN");
        }
        return ret;
    }

    public void genenticalTrain(final Set<Integer> prooveSet, final Set<Integer> trainSet, final Map<Integer, Integer> DIH, final Year year) throws IOException {
        final Genetics g = new Genetics(new FitnessFunction() {

            double bestR = Double.MAX_VALUE;

            @Override
            public synchronized double estimate(final List<Double> genome) {
                if (bias() < 1) {
                    return Double.MAX_VALUE;
                }
                setParametersGenome(genome);
                train(trainSet);
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
     * returns probability of DIH>1
     * 
     * @param memberId
     * @param y
     * @return
     */
    public double[] getProbalilityOfHospitalisation(final Integer memberId, final Year y) {
        if (!trained) {
            throw new RuntimeException("not trained");
        }
        final List<Claim> mclaims = getClaims(memberId, y);
        final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(mclaims);
        final double[][] probabilities = new double[MAX_DIH][uniqueDiagnoses.size()];
        int i = 0;
        for (final String pcg : uniqueDiagnoses) {
            final double numberOfClaims = sm.getValue(pcg, NUMBER_OF_CLAIMS);
            if (numberOfClaims > 0) {
                for (int f = 0; f < MAX_DIH; f++) {
                    final double numberOfClaimsToDih = sm.getValue(pcg, HAVE_DIH_MORE + f);
                    final double p = numberOfClaimsToDih / numberOfClaims;
                    probabilities[f][i] = p;
                }
            }
            i++;
        }
        final double[] ret = new double[MAX_DIH];
        for (int f = 0; f < MAX_DIH; f++) {
            final double p = ArraysTools.probalility(probabilities[f]);
            ret[f] = p;
        }
        return ret;
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

    @Override
    public synchronized double predict(final Integer memberId, final Year yearToPredict) {
        final double[] pp = getProbalilityOfHospitalisation(memberId, yearToPredict);
        final double ret = blendProbabilitiesOnIndex(pp);
        if (ret < 0) {
            return 0;
        }
        return ret;
    }

    @Override
    public void train(final Collection<Integer> trainset) {
        this.trainset = trainset;
        final DataImporter di = DataImporter.instance();
        fillMatrix(sm, claimsY1, Dih2);
        fillMatrix(sm, claimsY2, Dih3);
        fillMatrix(sm, claimsY3, null);
        di.saveStatsMatrix(sm, this.getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_" + getName() + ".csv");
        trained = true;
    }

    @Override
    protected void init() {
        parameter = new PrimaryConditionGroupParam();
        super.init();
    }

    @Override
    protected StatsMatrix initDefaultGenome() {
        final StatsMatrix genomeMatrix = new StatsMatrix(4);
        genomeMatrix.increaseCount(GENOME_ROW, "dih divider", 3f);
        genomeMatrix.increaseCount(GENOME_ROW, "probability decay bias", 2.5f);
        genomeMatrix.increaseCount(GENOME_ROW, "probability decay speed", 3f);
        genomeMatrix.increaseCount(GENOME_ROW, "best const blend", 0f);
        return genomeMatrix;
    }

    @Override
    protected String makeGenomeFilename() {
        return "genome_" + this.getClass().getSimpleName() + "-" + this.parameter.getClass().getSimpleName() + "_" + getName() + ".csv";
    }

    private float bias() {
        float b = genome("probability decay bias");
        if (b < 1.1) {
            b = 1.1f;
        }
        return b;
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

    private void fillMatrix(final StatsMatrix sm, final ClaimsList claimsY, final Map<Integer, Integer> DIH) {
        final Set<Integer> membersGroup = new HashSet<Integer>();
        if (DIH != null) {
            membersGroup.addAll(DIH.keySet());
        } else {
            membersGroup.addAll(claimsY.getClaimsByMemberMap().keySet());
        }
        membersGroup.retainAll(trainset);
        final float bias = bias();
        final float decaySpeed = genome("probability decay speed");
        for (final Integer memberId : membersGroup) {
            final List<Claim> mclaims = claimsY.getClaimsByMember(memberId);
            final int dih = DIH == null ? -1 : DIH.get(memberId);
            if (mclaims != null) {
                final Collection<String> uniqueDiagnoses = collectuniqueDiagnoses(mclaims);
                final float divider = uniqueDiagnoses.size();
                final double p = 1 - Math.pow((1 - 1 / bias), decaySpeed / divider);
                for (final String pcg : uniqueDiagnoses) {
                    sm.increaseCount(pcg, NUMBER_OF_CLAIMS, 1);
                    if (DIH != null) {
                        if (dih >= MAX_DIH_THRESHOLD) {
                            sm.increaseCount(pcg, HAVE_DIH_MORE + MAX_DIH_THRESHOLD, (float) p);
                        } else {
                            sm.increaseCount(pcg, HAVE_DIH_MORE + dih, (float) p);
                        }
                    }
                }
            }
        }
    }
}
