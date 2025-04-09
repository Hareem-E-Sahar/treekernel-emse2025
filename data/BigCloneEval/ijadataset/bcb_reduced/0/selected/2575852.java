package tr.com.iontek.biotools.scf;

import java.io.IOException;

public abstract class Trace {

    protected int A = 0, C = 1, G = 2, T = 3, N = 4;

    protected int TraceData[][];

    protected int basePosition[];

    protected int base[];

    protected int quality[];

    protected int baseNumFromLetter[] = new int[100];

    protected char baseLetterFromNum[] = { 'A', 'C', 'G', 'T', 'N' };

    public abstract void read() throws IOException;

    public abstract String getVersion();

    public abstract String getComments();

    public Trace() {
        for (int i = 0; i < baseNumFromLetter.length; i++) {
            baseNumFromLetter[i] = 4;
        }
        baseNumFromLetter[65] = 0;
        baseNumFromLetter[67] = 1;
        baseNumFromLetter[71] = 2;
        baseNumFromLetter[84] = 3;
    }

    public int getTraceData(int base, int Index) {
        return (TraceData[base][Index]);
    }

    public int getBase(int Index) {
        return (base[Index]);
    }

    public int getQual(int Index) {
        return (quality[Index]);
    }

    public int getNumBases() {
        return ((int) base.length);
    }

    public int getTraceLength() {
        return ((int) TraceData[A].length);
    }

    public int getBasePosition(int Index) {
        return (basePosition[Index]);
    }

    public void CopyBogusQuality() {
        quality = new int[base.length];
    }

    public void CopyQuality(int QualitySource[], int NumValues) {
        if (base.length != 0) {
            if (NumValues != base.length) {
                System.err.println("Warning!  Number of bases does not match quality.");
                System.err.println("NumBases=" + base.length + " NumQual=" + NumValues);
                for (int i = 0; i < NumValues; i++) {
                    System.err.print(QualitySource[i] + " ");
                    if ((i % 25) == 0) {
                        System.err.println();
                    }
                }
            }
        }
        quality = new int[NumValues];
        System.arraycopy(QualitySource, 0, quality, 0, NumValues);
    }

    public int GetLastBaseBeforeSample(int sampleNum) {
        int first, last, mid;
        first = 1;
        last = base.length - 1;
        mid = (last - first) / 2;
        while ((last - first) >= 2) {
            if (sampleNum < basePosition[mid]) {
                last = mid;
            } else if (sampleNum > basePosition[mid]) {
                first = mid;
            } else {
                return (mid);
            }
            mid = first + (last - first) / 2;
        }
        if ((sampleNum > basePosition[first]) && (sampleNum < basePosition[last])) {
            return (first);
        }
        if (sampleNum > basePosition[last]) {
            return (last);
        }
        return (0);
    }

    public int GetNextBaseAfterSample(int sampleNum) {
        int first, last, mid;
        first = 1;
        last = base.length - 1;
        mid = (last - first) / 2;
        while ((last - first) >= 2) {
            if (sampleNum < basePosition[mid]) {
                last = mid;
            } else if (sampleNum > basePosition[mid]) {
                first = mid;
            } else {
                return (mid);
            }
            mid = first + (last - first) / 2;
        }
        if (sampleNum < basePosition[first]) {
            return (first);
        }
        if ((sampleNum > basePosition[first]) && (sampleNum < basePosition[last])) {
            return (last);
        }
        return (0);
    }
}
