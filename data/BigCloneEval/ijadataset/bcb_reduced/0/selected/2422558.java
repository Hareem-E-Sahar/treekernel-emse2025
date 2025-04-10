package cc.mallet.types;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.OutputStream;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Label;

public class RankedFeatureVector extends FeatureVector {

    int[] rankOrder;

    private static final int SORTINIT = -1;

    int sortedTo = SORTINIT;

    public RankedFeatureVector(Alphabet dict, int[] indices, double[] values) {
        super(dict, indices, values);
    }

    public RankedFeatureVector(Alphabet dict, double[] values) {
        super(dict, values);
    }

    private static double[] subArray(double[] a, int begin, int length) {
        double[] ret = new double[length];
        System.arraycopy(a, begin, ret, 0, length);
        return ret;
    }

    public RankedFeatureVector(Alphabet dict, double[] values, int begin, int length) {
        super(dict, subArray(values, begin, length));
    }

    public RankedFeatureVector(Alphabet dict, DenseVector v) {
        this(dict, v.values);
    }

    public RankedFeatureVector(Alphabet dict, AugmentableFeatureVector v) {
        super(dict, v.indices, v.values, v.size, v.size, true, true, true);
    }

    public RankedFeatureVector(Alphabet dict, SparseVector v) {
        super(dict, v.indices, v.values);
    }

    protected void setRankOrder() {
        this.rankOrder = new int[values.length];
        for (int i = 0; i < rankOrder.length; i++) {
            rankOrder[i] = i;
            assert (!Double.isNaN(values[i]));
        }
        for (int i = rankOrder.length - 1; i >= 0; i--) {
            boolean swapped = false;
            for (int j = 0; j < i; j++) if (values[rankOrder[j]] < values[rankOrder[j + 1]]) {
                int r = rankOrder[j];
                rankOrder[j] = rankOrder[j + 1];
                rankOrder[j + 1] = r;
            }
        }
    }

    protected void setRankOrder(int extent, boolean reset) {
        int sortExtent;
        sortExtent = (extent >= values.length) ? values.length - 1 : extent;
        if (sortedTo == SORTINIT || reset) {
            this.rankOrder = new int[values.length];
            for (int i = 0; i < rankOrder.length; i++) {
                rankOrder[i] = i;
                assert (!Double.isNaN(values[i]));
            }
        }
        for (int i = sortedTo + 1; i <= sortExtent; i++) {
            double max = values[rankOrder[i]];
            int maxIndex = i;
            for (int j = i + 1; j < rankOrder.length; j++) {
                if (values[rankOrder[j]] > max) {
                    max = values[rankOrder[j]];
                    maxIndex = j;
                }
            }
            int r = rankOrder[maxIndex];
            rankOrder[maxIndex] = rankOrder[i];
            rankOrder[i] = r;
            sortedTo = i;
        }
    }

    protected void setRankOrder(int extent) {
        setRankOrder(extent, false);
    }

    public int getMaxValuedIndex() {
        if (rankOrder == null) setRankOrder(0);
        return getIndexAtRank(0);
    }

    public Object getMaxValuedObject() {
        return dictionary.lookupObject(getMaxValuedIndex());
    }

    public int getMaxValuedIndexIn(FeatureSelection fs) {
        if (fs == null) return getMaxValuedIndex();
        assert (fs.getAlphabet() == dictionary);
        int i = 0;
        while (!fs.contains(rankOrder[i])) {
            setRankOrder(i);
            i++;
        }
        return getIndexAtRank(i);
    }

    public Object getMaxValuedObjectIn(FeatureSelection fs) {
        return dictionary.lookupObject(getMaxValuedIndexIn(fs));
    }

    public double getMaxValue() {
        if (rankOrder == null) setRankOrder(0);
        return values[rankOrder[0]];
    }

    public double getMaxValueIn(FeatureSelection fs) {
        if (fs == null) return getMaxValue();
        int i = 0;
        while (!fs.contains(i)) {
            setRankOrder(i);
            i++;
        }
        return values[rankOrder[i]];
    }

    public int getIndexAtRank(int rank) {
        setRankOrder(rank);
        return indexAtLocation(rankOrder[rank]);
    }

    public Object getObjectAtRank(int rank) {
        setRankOrder(rank);
        return dictionary.lookupObject(getIndexAtRank(rank));
    }

    public double getValueAtRank(int rank) {
        if (values == null) return 1.0;
        setRankOrder(rank);
        if (rank >= rankOrder.length) {
            rank = rankOrder.length - 1;
            System.err.println("rank larger than rankOrder.length. rank = " + rank + "rankOrder.length = " + rankOrder.length);
        }
        if (rankOrder[rank] >= values.length) {
            System.err.println("rankOrder[rank] out of range.");
            return 1.0;
        }
        return values[rankOrder[rank]];
    }

    /**
   * Prints a human-readable version of this vector, with features listed in ranked order.
   * @param out Stream to write to
   */
    public void printByRank(OutputStream out) {
        printByRank(new PrintWriter(new OutputStreamWriter(out), true));
    }

    /**
   * Prints a human-readable version of this vector, with features listed in ranked order.
   * @param out Writer to write to
   */
    public void printByRank(PrintWriter out) {
        for (int rank = 0; rank < numLocations(); rank++) {
            int idx = getIndexAtRank(rank);
            double val = getValueAtRank(rank);
            Object obj = dictionary.lookupObject(idx);
            out.print(obj + ":" + val + " ");
        }
    }

    public void printTopK(PrintWriter out, int num) {
        int length = numLocations();
        if (num > length) num = length;
        for (int rank = 0; rank < num; rank++) {
            int idx = getIndexAtRank(rank);
            double val = getValueAtRank(rank);
            Object obj = dictionary.lookupObject(idx);
            out.print(obj + ":" + val + " ");
        }
    }

    public void printLowerK(PrintWriter out, int num) {
        int length = numLocations();
        if (num > length) num = length;
        for (int rank = length - num; rank < length; rank++) {
            int idx = getIndexAtRank(rank);
            double val = getValueAtRank(rank);
            Object obj = dictionary.lookupObject(idx);
            out.print(obj + ":" + val + " ");
        }
    }

    public int getRank(Object o) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int getRank(int index) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void set(int i, double v) {
        throw new UnsupportedOperationException(RankedFeatureVector.class.getName() + " is immutable");
    }

    public interface Factory {

        public RankedFeatureVector newRankedFeatureVector(InstanceList ilist);
    }

    public interface PerLabelFactory {

        public RankedFeatureVector[] newRankedFeatureVectors(InstanceList ilist);
    }
}
