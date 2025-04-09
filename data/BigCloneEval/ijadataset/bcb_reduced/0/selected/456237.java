package genomicMap.worker;

import genomicMap.data.DataSource;
import genomicMap.data.PDataSource;
import genomicMap.inputModel.ComPLHoodIModel;
import genomicMap.worker.helper.PhysicalMapHelper;
import javautil.collections.ArrayUtil;

/**
 * A class dedicated to the physical likelihood of
 *  probe/clone orders. Note: This is a negative-log likelihood.
 */
public class PLikeliHood extends LHood {

    /**
     * physical data source
     */
    private PDataSource pData = null;

    private double[] probeSpacing = null;

    /**
     * Constructor
     */
    public PLikeliHood() {
    }

    public PLikeliHood(ComPLHoodIModel inputModel) {
        setPData(DataSource.getPDataSource(inputModel.getLinkageGroup(), inputModel.getOrder()));
    }

    public static void main(String[] args) {
        PLikeliHood pHood = new PLikeliHood();
        pHood.setPData(DataSource.getPDataSource(1, new int[] { 1, 2, 3, 4, 5 }));
        pHood.setVerbose(true);
        pHood.runPHood();
    }

    public double runPHood() {
        setStatusInfo("Physical Likleihood Computation has Started");
        setProgress(10);
        int probe_count = pData.getOrder().length;
        int clone_count = pData.getCloneCount();
        int[][] data = pData.getdata();
        double probe_false_pos = pData.getProbFalsePos();
        double probe_false_neg = pData.getProbFalseNeg();
        int ch_length = pData.getChLength();
        int clone_length = pData.getCloneLength();
        int[] probeOrder = ArrayUtil.IntegerSequence(probe_count, false);
        int[][] joint = new int[probe_count][probe_count];
        for (int probeIndex1 = 0; probeIndex1 < probe_count; probeIndex1++) {
            for (int probeIndex2 = probeIndex1 + 1; probeIndex2 < probe_count; probeIndex2++) {
                for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
                    if ((data[cloneIndex][probeIndex1] == 1) && (data[cloneIndex][probeIndex2] == 1)) {
                        joint[probeIndex1][probeIndex2]++;
                    }
                }
                joint[probeIndex2][probeIndex1] = joint[probeIndex1][probeIndex2];
            }
        }
        double[][] aa = new double[clone_count][probe_count + 1];
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
                if (probeIndex == 0) {
                    aa[cloneIndex][probeIndex] = 0.0;
                } else if (data[cloneIndex][probeIndex - 1] == 0) {
                    aa[cloneIndex][probeIndex] = probe_false_neg / (1 - probe_false_pos);
                } else {
                    aa[cloneIndex][probeIndex] = (1 - probe_false_neg) / probe_false_pos;
                }
            }
        }
        int P = 0;
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
                if (data[cloneIndex][probeIndex] == 1) {
                    P++;
                }
            }
        }
        double const1 = clone_count * Math.log(ch_length - clone_length) - P * Math.log(probe_false_pos / (1 - probe_false_pos)) - probe_count * clone_count * Math.log(1 - probe_false_pos);
        int gap = ch_length - (probe_count * clone_length);
        setStatusInfo("Computing initial probe spacings");
        setProgress(30);
        double[] y = PhysicalMapHelper.Initial_Ys(probeOrder, probe_count, clone_count, ch_length, clone_length, gap, joint);
        double[] R = PhysicalMapHelper.getR(ch_length, clone_length, probe_count, clone_count, aa, probeOrder);
        setStatusInfo("Running Steepest Descent Algorithm");
        setProgress(70);
        setLhood(PhysicalMapHelper.Conj(y, probe_count, clone_count, aa, probeOrder, clone_length, const1, R));
        setProbeSpacing(y);
        if (isVerbose()) {
            System.out.println("The log-likelihood is " + getLhood());
        }
        setStatusInfo("The Physical Log-likelihood is " + getLhood());
        setProgress(100);
        return getLhood();
    }

    /**
     * computes the function which is used for simulated annealing
     * Since Plikelihood by default returns the negative-loglikelihood
     * it is not futher negativitized. This will used as part of Annealable
     * interface to Simulated Annealing to serach for an order with minimum value
     * and thus max. loh-likelihood.
     */
    @Override
    public double compute() {
        getPData().setOrder(getOrder());
        return runPHood();
    }

    @Override
    public double initialValue() {
        return compute();
    }

    /**
     * physical data source
     */
    public PDataSource getPData() {
        return pData;
    }

    /**
     * physical data source
     *
     * @param newVal
     */
    @Override
    public void setPData(PDataSource pData) {
        this.pData = pData;
        setOrder(pData.getOrder());
    }

    public double[] getProbeSpacing() {
        return probeSpacing;
    }

    public void setProbeSpacing(double[] probeSpacing) {
        this.probeSpacing = probeSpacing;
    }

    @Override
    public double getLogLikeliHood() {
        return -getLhood();
    }
}
