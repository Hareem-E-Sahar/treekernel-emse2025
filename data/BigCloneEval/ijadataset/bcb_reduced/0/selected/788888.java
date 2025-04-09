package net.sf.javaml.clustering;

import java.util.Vector;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.utils.GammaFunction;
import net.sf.javaml.utils.MathUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

/**
 * 
 * This class implements the Adaptive Quality-based Clustering Algorithm, based
 * on the implementation in MATLAB by De Smet et al., ESAT - SCD (SISTA),
 * K.U.Leuven, Belgium.
 * 
 * @author Thomas Abeel
 */
public class AQBC implements Clusterer {

    private double RADNW;

    private int E;

    class TaggedInstance {

        Instance inst;

        private static final long serialVersionUID = 8990262697388049283L;

        private int tag;

        TaggedInstance(Instance i, int tag) {
            this.inst = i;
            this.tag = tag;
        }

        public int getTag() {
            return tag;
        }
    }

    private Dataset data;

    private boolean normalize;

    /**
	 * XXX write doc
	 * 
	 * FIXME remove output on the console
	 */
    public Dataset[] cluster(Dataset data) {
        this.data = data;
        dm = new EuclideanDistance();
        Vector<TaggedInstance> SP;
        if (normalize) SP = normalize(data); else SP = dontnormalize(data);
        System.out.println("Remaining datapoints = " + SP.size());
        int NRNOCONV = 0;
        int maxNRNOCONV = 2;
        int TOOFEWPOINTS = 0;
        int TFPTH = 10;
        int BPR = 0;
        int RRES = 0;
        int BPRTH = 10;
        double REITERTHR = 0.1;
        E = data.noAttributes();
        if (E > 170) throw new RuntimeException("AQBC is unable to work for more than 170 dimensions! This is a limitation of the Gamma function");
        double R = Math.sqrt(E - 1);
        double EXTTRESH = R / 2.0f;
        int MINNRGENES = 2;
        int cluster = 0;
        while (NRNOCONV < maxNRNOCONV && TOOFEWPOINTS < TFPTH && BPR < BPRTH && RRES < 2) {
            boolean clusterLocalisationConverged = wan_shr_adap(SP, EXTTRESH);
            if (clusterLocalisationConverged) {
                System.out.println("Found cluster -> EM");
                System.out.println("Starting EM");
                boolean emConverged = exp_max(SP, ME, EXTTRESH2, S);
                if (emConverged) {
                    System.out.println("EM converged, predicting radius...");
                    NRNOCONV = 0;
                    if (Math.abs(RADNW - EXTTRESH) / EXTTRESH < REITERTHR) {
                        Vector<TaggedInstance> Q = retrieveInstances(SP, ME, RADNW);
                        if (Q.size() == 0) {
                            System.err.println("Significance level not reached");
                        }
                        if (Q.size() > MINNRGENES) {
                            cluster++;
                            outputCluster(Q, cluster);
                            removeInstances(SP, Q);
                            TOOFEWPOINTS = 0;
                            EXTTRESH = RADNW;
                        } else {
                            removeInstances(SP, Q);
                            TOOFEWPOINTS++;
                        }
                    } else {
                        EXTTRESH = RADNW;
                        BPR++;
                        if (BPR == BPRTH) {
                            System.out.println("Radius cannot be predicted!");
                        } else {
                            System.out.println("Trying new radius...");
                        }
                    }
                } else {
                    NRNOCONV++;
                    if (NRNOCONV < maxNRNOCONV) {
                        EXTTRESH = R / 2;
                        RRES++;
                        System.out.println("Resetting radius to: " + EXTTRESH);
                        if (RRES == 2) {
                            System.out.println("No convergence: Algorithm aborted - RRES exceeded!");
                            break;
                        } else {
                            BPR = 0;
                        }
                    } else {
                        System.out.println("No convergence: Algorithm aborted - NRNOCONV exceeded!");
                        break;
                    }
                }
                if (TOOFEWPOINTS == TFPTH) {
                    System.out.println("No more significant clusters found: Algorithms aborted!");
                    break;
                }
            }
        }
        Dataset[] output = new Dataset[clusters.size()];
        for (int i = 0; i < clusters.size(); i++) {
            output[i] = clusters.get(i);
        }
        return output;
    }

    /**
	 * Normalizes the data to mean 0 and standard deviation 1. This method
	 * discards all instances that cannot be normalized, i.e. they have the same
	 * value for all attributes.
	 * 
	 * @param data
	 * @return
	 */
    private Vector<TaggedInstance> dontnormalize(Dataset data) {
        Vector<TaggedInstance> out = new Vector<TaggedInstance>();
        for (int i = 0; i < data.size(); i++) {
            out.add(new TaggedInstance(data.instance(i), i));
        }
        return out;
    }

    /**
	 * Normalizes the data to mean 0 and standard deviation 1. This method
	 * discards all instances that cannot be normalized, i.e. they have the same
	 * value for all attributes.
	 * 
	 * @param data
	 * @return
	 */
    private Vector<TaggedInstance> normalize(Dataset data) {
        Vector<TaggedInstance> out = new Vector<TaggedInstance>();
        for (int i = 0; i < data.size(); i++) {
            Double[] old = data.instance(i).values().toArray(new Double[0]);
            double[] conv = new double[old.length];
            for (int j = 0; j < old.length; j++) {
                conv[j] = old[j];
            }
            Mean m = new Mean();
            double MU = m.evaluate(conv);
            StandardDeviation std = new StandardDeviation();
            double SIGM = std.evaluate(conv, MU);
            if (!MathUtils.eq(SIGM, 0)) {
                double[] val = new double[old.length];
                for (int j = 0; j < old.length; j++) {
                    val[j] = (float) ((old[j] - MU) / SIGM);
                }
                out.add(new TaggedInstance(new DenseInstance(val, data.instance(i).classValue()), i));
            }
        }
        return out;
    }

    /**
	 * Remove the instances in q from sp
	 * 
	 * @param sp
	 * @param q
	 */
    private void removeInstances(Vector<TaggedInstance> sp, Vector<TaggedInstance> q) {
        sp.removeAll(q);
    }

    /**
	 * XXX write doc
	 * 
	 * @param significance
	 */
    public AQBC(double significance) {
        this(significance, true);
    }

    /**
	 * XXX write doc
	 * 
	 * default constructor
	 */
    public AQBC() {
        this(0.95);
    }

    public AQBC(double sig, boolean normalize) {
        this.normalize = normalize;
        this.S = sig;
    }

    private Vector<Dataset> clusters = new Vector<Dataset>();

    /**
	 * output all the instances in q as a single cluster with the given index
	 * 
	 * The index is ignored.
	 * 
	 * @param q
	 * @param cluster
	 */
    private void outputCluster(Vector<TaggedInstance> q, int index) {
        Dataset tmp = new DefaultDataset();
        for (TaggedInstance i : q) {
            tmp.add(data.instance(i.getTag()));
        }
        clusters.add(tmp);
    }

    private DistanceMeasure dm;

    private Vector<TaggedInstance> retrieveInstances(Vector<TaggedInstance> sp, double[] me2, double radnw2) {
        Instance tmp = new DenseInstance(me2);
        Vector<TaggedInstance> out = new Vector<TaggedInstance>();
        for (TaggedInstance inst : sp) {
            if (dm.measure(inst.inst, tmp) < radnw2) out.add(inst);
        }
        return out;
    }

    private boolean exp_max(Vector<TaggedInstance> AS, double[] CK, double QUAL, double S) {
        double D = E - 2;
        double R = Math.sqrt(E - 1);
        double[] RD = calculateDistances(AS, CK);
        int samples = RD.length;
        int MAXITER = 500;
        double CDIF = 0.001;
        double count = 0;
        for (int i = 0; i < RD.length; i++) {
            if (RD[i] < QUAL) {
                count++;
            }
        }
        double PC = count / RD.length;
        double PB = 1 - PC;
        double tmpVAR = 0;
        for (int i = 0; i < RD.length; i++) {
            if (RD[i] < QUAL) {
                tmpVAR += RD[i] * RD[i];
            }
        }
        double VAR = (1 / D) * tmpVAR / count;
        boolean CONV = false;
        for (int i = 0; i < MAXITER && !CONV; i++) {
            double[] prc = clusterdistrib(RD, VAR, D, R);
            double[] prb = background(RD, D, R);
            double[] prcpc = new double[prc.length];
            for (int j = 0; j < prc.length; j++) {
                prcpc[j] = prc[j] * PC;
            }
            double[] prbpb = new double[prb.length];
            for (int j = 0; j < prb.length; j++) {
                prbpb[j] = prb[j] * PB;
            }
            double[] pr = new double[prcpc.length];
            for (int j = 0; j < prc.length; j++) {
                pr[j] = prcpc[j] + prbpb[j];
            }
            double[] pcr = new double[prcpc.length];
            for (int j = 0; j < prc.length; j++) {
                pcr[j] = prcpc[j] / pr[j];
            }
            double SM = 0;
            for (int j = 0; j < prc.length; j++) {
                SM += pcr[j];
            }
            if (MathUtils.eq(SM, 0) || Double.isInfinite(SM)) {
                i = MAXITER;
            }
            float tmpVAR_new = 0;
            for (int j = 0; j < prc.length; j++) {
                tmpVAR_new += RD[j] * RD[j] * pcr[j];
            }
            double VAR_new = (1 / D) * tmpVAR_new / SM;
            double PC_new = SM / samples;
            double PB_new = 1 - PC_new;
            if (Math.abs(VAR_new - VAR) < CDIF && Math.abs(PC_new - PC) < CDIF) {
                CONV = true;
            }
            PC = PC_new;
            PB = PB_new;
            VAR = VAR_new;
        }
        if (CONV) {
            if (MathUtils.eq(PC, 0) || MathUtils.eq(PB, 0)) {
                System.out.println("EM: No or incorrect convergence! - PC==0 || PB==0");
                CONV = false;
                RADNW = 0;
                return false;
            }
            double SD = (2 * Math.pow(Math.PI, D / 2)) / (GammaFunction.gamma(D / 2));
            double SD1 = (2 * Math.pow(Math.PI, (D + 1) / 2)) / (GammaFunction.gamma((D + 1) / 2));
            double CC = SD * (1 / (Math.pow(2 * Math.PI * VAR, D / 2)));
            double CB = (SD / (SD1 * Math.pow(Math.sqrt(D + 1), D)));
            double LO = (S / (1 - S)) * ((PB * CB) / (PC * CC));
            if (LO <= 0) {
                System.out.println("EM: Impossible to calculate radius - LO<0!");
                return false;
            }
            double DIS = -2 * VAR * Math.log(LO);
            if (DIS <= 0) {
                System.out.println("EM: Impossible to calculate radius - DIS<0!");
                System.out.println();
                return false;
            }
            RADNW = (float) Math.sqrt(DIS);
            return true;
        } else {
            System.out.println("EM: No or incorrect convergence! Probably not enough iterations for EM");
            return false;
        }
    }

    /**
	 * implements background.m
	 * 
	 * @param r
	 * @param D
	 * @param R
	 * @return
	 */
    private double[] background(double[] r, double D, double R) {
        double SD = (2 * Math.pow(Math.PI, D / 2)) / (GammaFunction.gamma(D / 2));
        double SD1 = (2 * Math.pow(Math.PI, (D + 1) / 2)) / (GammaFunction.gamma((D + 1) / 2));
        double[] out = new double[r.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = ((SD / (SD1 * (Math.pow(R, D)))) * (Math.pow(r[i], D - 1)));
        }
        return out;
    }

    /**
	 * implements clusterdistrib
	 * 
	 * @param r
	 * @param VAR
	 * @param D
	 * @param R
	 * @return
	 */
    private double[] clusterdistrib(double[] r, double VAR, double D, double R) {
        double[] out = new double[r.length];
        if (MathUtils.eq(VAR, 0)) {
            for (int i = 0; i < r.length; i++) {
                if (MathUtils.eq(r[i], 0)) {
                    out[i] = Float.POSITIVE_INFINITY;
                }
            }
        } else {
            double SD = (2 * Math.pow(Math.PI, D / 2)) / (GammaFunction.gamma(D / 2));
            double tmp_piVAR = 2 * Math.PI * VAR;
            double tmp_piVARpow = Math.pow(tmp_piVAR, D / 2);
            double tmp_piVARpowINV = 1 / tmp_piVARpow;
            for (int i = 0; i < r.length; i++) {
                double tmp_exp = -((r[i] * r[i]) / (2 * VAR));
                out[i] = (float) (SD * tmp_piVARpowINV * Math.pow(r[i], D - 1) * Math.exp(tmp_exp));
            }
            for (int i = 0; i < r.length; i++) {
                if (MathUtils.eq(r[i], 0)) out[i] = 1;
            }
        }
        return out;
    }

    /**
	 * Comparable to dist_misval
	 * 
	 * Calculates the distance between each instance and the instance given as a
	 * float array.
	 * 
	 * @param as
	 * @param ck
	 * @return
	 */
    private double[] calculateDistances(Vector<TaggedInstance> as, double[] ck) {
        double[] out = new double[as.size()];
        for (int i = 0; i < as.size(); i++) {
            Double[] values = as.get(i).inst.values().toArray(new Double[0]);
            float sum = 0;
            for (int j = 0; j < values.length; j++) {
                double dif = values[j] - ck[j];
                sum += dif * dif;
            }
            out[i] = Math.sqrt(sum);
        }
        return out;
    }

    private double S = 0.95f;

    private double EXTTRESH2;

    private double[] ME;

    /**
	 * returns true if this step converged
	 */
    private boolean wan_shr_adap(Vector<TaggedInstance> A, double EXTTRESH) {
        int samples = A.size();
        double[] CE = new double[samples];
        int MAXITER = 100;
        double NRWAN = 30;
        double[] ME1 = mean(A);
        double[] DMI = calculateDistances(A, ME1);
        double maxDMI = DMI[0];
        double minDMI = DMI[0];
        for (int i = 1; i < DMI.length; i++) {
            if (DMI[i] > maxDMI) maxDMI = DMI[i];
            if (DMI[i] < minDMI) minDMI = DMI[i];
        }
        EXTTRESH2 = maxDMI;
        double MDIS = minDMI;
        if (MathUtils.eq(MDIS, EXTTRESH2)) {
            ME = ME1;
            for (int i = 0; i < CE.length; i++) CE[i] = 1;
            EXTTRESH2 += 0.000001;
            System.out.println("Cluster center localisation did not reach preliminary estimate of radius!");
            return true;
        }
        double DELTARAD = (EXTTRESH2 - EXTTRESH) / NRWAN;
        double RADPR = EXTTRESH2;
        EXTTRESH2 = EXTTRESH2 - DELTARAD;
        if (EXTTRESH2 <= MDIS) {
            EXTTRESH2 = (RADPR + MDIS) / 2;
        }
        Vector<Integer> Q = findLower(DMI, EXTTRESH2);
        for (int i = 0; Q.size() != 0 && i < MAXITER; i++) {
            double[] ME2 = mean(select(A, Q));
            if (MathUtils.eq(ME1, ME2) && MathUtils.eq(RADPR, EXTTRESH2)) {
                ME = ME2;
                for (Integer index : Q) {
                    CE[index] = 1;
                }
                return true;
            }
            RADPR = EXTTRESH2;
            DMI = calculateDistances(A, ME2);
            if (EXTTRESH2 > EXTTRESH) {
                EXTTRESH2 = Math.max(EXTTRESH, EXTTRESH2 - DELTARAD);
                if (EXTTRESH2 < MathUtils.min(DMI)) {
                    EXTTRESH2 = RADPR;
                }
            }
            Q = findLower(DMI, EXTTRESH2);
            ME1 = ME2;
        }
        System.out.println("Preliminary cluster location did not converge");
        System.out.println("\t EXTTRESH2 = " + EXTTRESH2);
        return false;
    }

    /**
	 * return all the indices that are lower that the threshold
	 * 
	 * @param array
	 * @param thres
	 * @return
	 */
    private Vector<Integer> findLower(double[] array, double threshold) {
        Vector<Integer> out = new Vector<Integer>();
        for (int i = 0; i < array.length; i++) {
            if (array[i] < threshold) out.add(i);
        }
        return out;
    }

    /**
	 * Return a vector with all instances that have their index in the indices
	 * vector.
	 * 
	 * @param instances
	 * @param indices
	 * @return
	 */
    private Vector<TaggedInstance> select(Vector<TaggedInstance> instances, Vector<Integer> indices) {
        Vector<TaggedInstance> out = new Vector<TaggedInstance>();
        for (Integer index : indices) {
            out.add(instances.get(index));
        }
        return out;
    }

    private double[] mean(Vector<TaggedInstance> a) {
        double[] out = new double[a.get(0).inst.noAttributes()];
        for (int i = 0; i < a.size(); i++) {
            for (int j = 0; j < a.get(0).inst.noAttributes(); j++) out[j] += a.get(i).inst.value(j);
        }
        for (int j = 0; j < a.get(0).inst.noAttributes(); j++) {
            out[j] /= a.size();
        }
        return out;
    }
}
