package lusc.net.sourceforge;

import java.util.*;

public class SWMLEntropyEstimate {

    Random random = new Random(System.currentTimeMillis() + 45678);

    double eval = 1 / Math.log(2);

    double[] rhos;

    double[] deviations;

    int bestK = 0;

    double bestRho = 0;

    float[][] invrhos;

    double[] bootstrapScores;

    double bootstrapMedian;

    double bootstrapSD;

    public SWMLEntropyEstimate(int[] locations, int[][] assignments, int[][] individuals, int[][] lookUpSyls) {
        int totalElementCount = 0;
        for (int i = 0; i < locations.length; i++) {
            totalElementCount += locations[i];
        }
        int[] labels = new int[totalElementCount];
        int a = 0;
        for (int i = 0; i < locations.length; i++) {
            for (int j = 0; j < locations[i]; j++) {
                labels[a] = i;
                a++;
            }
        }
        int[] individualLabel = new int[totalElementCount];
        for (int i = 0; i < individuals.length; i++) {
            for (int j = 0; j < individuals[i].length; j++) {
                for (int k = 0; k < lookUpSyls.length; k++) {
                    if (individuals[i][j] == lookUpSyls[k][0]) {
                        individualLabel[k] = i;
                    }
                }
            }
        }
        rhos = new double[assignments.length];
        for (int i = 0; i < assignments.length; i++) {
            rhos[i] = calculateSWMLEntropy(assignments[i], labels);
        }
        bestRho = 0;
        for (int i = 0; i < rhos.length; i++) {
            System.out.println((i + 2) + " " + rhos[i]);
            if (rhos[i] > bestRho) {
                bestRho = rhos[i];
                bestK = i + 2;
            }
        }
        System.out.println(bestK);
        invrhos = calculateSWMLEntropy(assignments[bestK - 2], labels, individualLabel);
        bootstrap();
        deviations = new double[invrhos.length];
    }

    public SWMLEntropyEstimate(int[] locations, int[] assignments, int[][] individuals, int[][] lookUpSyls, int kv) {
        int totalElementCount = 0;
        for (int i = 0; i < locations.length; i++) {
            totalElementCount += locations[i];
        }
        int[] labels = new int[totalElementCount];
        int a = 0;
        for (int i = 0; i < locations.length; i++) {
            for (int j = 0; j < locations[i]; j++) {
                labels[a] = i;
                a++;
            }
        }
        int[] individualLabel = new int[totalElementCount];
        for (int i = 0; i < individuals.length; i++) {
            for (int j = 0; j < individuals[i].length; j++) {
                for (int k = 0; k < lookUpSyls.length; k++) {
                    if (individuals[i][j] == lookUpSyls[k][0]) {
                        individualLabel[k] = i;
                    }
                }
            }
        }
        bestK = kv;
        System.out.println(bestK);
        invrhos = calculateSWMLEntropy(assignments, labels, individualLabel);
        int indnum = invrhos.length;
        for (int i = 0; i < indnum; i++) {
            for (int j = 0; j < i; j++) {
                bestRho += invrhos[i][j];
            }
        }
        deviations = new double[invrhos.length];
        bestRho /= indnum * (indnum - 1) * 0.5;
    }

    public void bootstrap() {
        int indnum = invrhos.length;
        int bootstrapreps = 1000000;
        int[] props = { 250, 2500, 25000, 500000, 975000, 997500, 999750 };
        double[] bootstrapests = new double[bootstrapreps];
        for (int i = 0; i < bootstrapreps; i++) {
            int[] times = new int[indnum];
            for (int j = 0; j < indnum; j++) {
                times[random.nextInt(indnum)]++;
            }
            double bscore = 0;
            double fcount = 0;
            for (int j = 0; j < indnum; j++) {
                for (int k = 0; k < j; k++) {
                    bscore += (invrhos[j][k]) * times[j] * times[k];
                    fcount += times[j] * times[k];
                }
            }
            bootstrapests[i] = bscore / fcount;
        }
        Arrays.sort(bootstrapests);
        bootstrapScores = new double[props.length];
        for (int i = 0; i < props.length; i++) {
            bootstrapScores[i] = bootstrapests[props[i]];
        }
        BasicStatistics bs = new BasicStatistics();
        bootstrapSD = bs.calculateSD(bootstrapests, true);
    }

    public void pcoDeviation() {
        int ndi = invrhos.length;
        boolean completed = false;
        while (completed == false) {
            try {
                float[][] inv2 = new float[invrhos.length][];
                int l1 = invrhos.length;
                float sum = 0;
                for (int i = 0; i < l1; i++) {
                    inv2[i] = new float[i + 1];
                    for (int j = 0; j < i; j++) {
                        inv2[i][j] = (1 - invrhos[i][j]) + (random.nextFloat() * 0.002f) - 0.0001f;
                        sum += (1 - invrhos[i][j]);
                    }
                }
                MultiDimensionalScaling mds = new MultiDimensionalScaling(inv2, ndi);
                float sum3 = 0;
                for (int i = 0; i < l1; i++) {
                    for (int j = 0; j < i; j++) {
                        double q = 0;
                        for (int k = 0; k < ndi; k++) {
                            double r = 1;
                            if (mds.eigenValues[k] < 0) {
                                r = -1;
                            }
                            q += r * (mds.out[i][k] - mds.out[j][k]) * (mds.out[i][k] - mds.out[j][k]);
                        }
                        invrhos[i][j] = (float) Math.sqrt(q);
                        sum3 += invrhos[i][j];
                    }
                }
                double multiplier = (sum / sum3);
                double[] means = new double[ndi];
                for (int i = 0; i < mds.out.length; i++) {
                    for (int j = 0; j < mds.out[i].length; j++) {
                        mds.out[i][j] = (float) (mds.out[i][j] * multiplier);
                        means[j] += mds.out[i][j];
                    }
                }
                for (int i = 0; i < ndi; i++) {
                    means[i] /= l1 + 0.0;
                }
                deviations = new double[mds.out.length];
                for (int i = 0; i < mds.out.length; i++) {
                    double score = 0;
                    for (int j = 0; j < mds.out[i].length; j++) {
                        double r = 1;
                        if (mds.eigenValues[j] < 0) {
                            r = -1;
                        }
                        score += r * (mds.out[i][j] - means[j]) * (mds.out[i][j] - means[j]);
                    }
                    deviations[i] = (Math.sqrt(score));
                }
                completed = true;
            } catch (Exception e) {
                System.out.println("Failed mds attempt");
            }
        }
    }

    public double calculateSWMLEntropy(int[] labels, int[] elementLabels) {
        LinkedList<int[]> pos = new LinkedList<int[]>();
        int x = 0;
        int y = 0;
        for (int i = 1; i < elementLabels.length; i++) {
            if (elementLabels[i] != elementLabels[i - 1]) {
                y = i;
                int[] a = { x, y };
                pos.add(a);
                x = i;
            }
        }
        int[] d = { x, elementLabels.length };
        pos.add(d);
        int[][] dat = new int[pos.size()][];
        int[] lengths = new int[dat.length];
        int[] starts = new int[pos.size()];
        int[] ends = new int[starts.length];
        for (int i = 0; i < dat.length; i++) {
            int[] a = pos.get(i);
            starts[i] = a[0];
            ends[i] = a[1];
            int w = a[1] - a[0];
            lengths[i] = w + 1;
            dat[i] = new int[w + 1];
            for (int j = a[0]; j < a[1]; j++) {
                dat[i][j - a[0] + 1] = labels[j];
            }
            dat[i][0] = -1;
        }
        double score = 0;
        double count = 0;
        for (int i = 0; i < dat.length; i++) {
            for (int j = 0; j < dat.length; j++) {
                if (i != j) {
                    score += getMaxSeq(dat[i], dat[j]);
                    count += dat[i].length;
                }
            }
        }
        score = count / score;
        int npermutations = 1000;
        double pscore = 0;
        double pcount = 0;
        for (int k = 0; k < npermutations; k++) {
            for (int i = 0; i < dat.length; i++) {
                for (int j = 0; j < dat.length; j++) {
                    if (i != j) {
                        int[] x1 = shuffle(dat[i]);
                        int[] x2 = shuffle(dat[j]);
                        pscore += getMaxSeq(x1, x2);
                        pcount += dat[i].length;
                    }
                }
            }
        }
        pscore = pcount / pscore;
        System.out.println(score + " " + count + " " + pscore + " " + pcount);
        double rho = (pscore - score) / pscore;
        return rho;
    }

    public int[] shuffle(int[] d) {
        int n = d.length;
        int[] res = new int[n];
        System.arraycopy(d, 0, res, 0, n);
        for (int i = 0; i < n; i++) {
            int p = i + random.nextInt(n - i);
            int q = res[p];
            res[p] = res[i];
            res[i] = q;
        }
        return (res);
    }

    public float[][] calculateSWMLEntropy(int[] labels, int[] elementLabels, int[] individualLabels) {
        int maxInd = 0;
        for (int i = 0; i < individualLabels.length; i++) {
            if (individualLabels[i] > maxInd) {
                maxInd = individualLabels[i];
            }
        }
        maxInd++;
        float[][] results = new float[maxInd][];
        double[][] counter = new double[maxInd][maxInd];
        double[][] scores = new double[maxInd][maxInd];
        for (int i = 0; i < maxInd; i++) {
            results[i] = new float[maxInd + 1];
        }
        LinkedList<int[]> pos = new LinkedList<int[]>();
        int x = 0;
        int y = 0;
        for (int i = 1; i < elementLabels.length; i++) {
            if (elementLabels[i] != elementLabels[i - 1]) {
                y = i;
                int[] a = { x, y };
                pos.add(a);
                x = i;
            }
        }
        int[] d = { x, elementLabels.length };
        pos.add(d);
        int[][] dat = new int[pos.size()][];
        int[] lengths = new int[dat.length];
        int[] inds = new int[pos.size()];
        for (int i = 0; i < dat.length; i++) {
            int[] a = pos.get(i);
            inds[i] = individualLabels[a[0]];
            int w = a[1] - a[0];
            lengths[i] = w + 1;
            dat[i] = new int[w + 1];
            for (int j = a[0]; j < a[1]; j++) {
                dat[i][j - a[0] + 1] = labels[j];
            }
            dat[i][0] = -1;
        }
        double score = 0;
        double count = 0;
        for (int i = 0; i < dat.length; i++) {
            for (int j = 0; j < dat.length; j++) {
                if (i != j) {
                    int a = inds[i];
                    int b = inds[j];
                    double sc = getMaxSeq(dat[i], dat[j]);
                    scores[a][b] += sc;
                    counter[a][b] += dat[i].length;
                    score += sc;
                    count += dat[i].length;
                }
            }
        }
        int npermutations = 1000;
        double pscore = 0;
        double pcount = 0;
        for (int k = 0; k < npermutations; k++) {
            for (int i = 0; i < dat.length; i++) {
                for (int j = 0; j < dat.length; j++) {
                    if (i != j) {
                        int[] x1 = shuffle(dat[i]);
                        int[] x2 = shuffle(dat[j]);
                        pscore += getMaxSeq(x1, x2);
                        pcount += dat[i].length;
                    }
                }
            }
        }
        pscore = pcount / pscore;
        double sum = 0;
        for (int i = 0; i < maxInd; i++) {
            for (int j = 0; j < i; j++) {
                double a = ((pscore - (counter[i][j] / scores[i][j])) / pscore);
                double b = ((pscore - (counter[j][i] / scores[j][i])) / pscore);
                results[i][j] = (float) (0.5 * (a + b));
                sum += results[i][j];
            }
        }
        System.out.println(pscore + " " + sum / (maxInd * (maxInd - 1) * 0.5) + " " + ((pscore - (count / score)) / pscore));
        return results;
    }

    public double getMaxSeq(int[] a, int[] b) {
        int xa = a.length;
        int xb = b.length;
        int xc = xa;
        if (xb < xa) {
            xc = xb;
        }
        double score = 0;
        for (int i = 0; i < xa; i++) {
            int maxc = 0;
            for (int j = 0; j < xb; j++) {
                if (a[i] == b[j]) {
                    int ii = i;
                    int jj = j;
                    int c = 0;
                    while ((c < xc) && (a[ii] == b[jj])) {
                        c++;
                        ii++;
                        if (ii == xa) {
                            ii = 0;
                        }
                        jj++;
                        if (jj == xb) {
                            jj = 0;
                        }
                    }
                    if (c > maxc) {
                        maxc = c;
                    }
                }
            }
            score += maxc / (Math.log(xc) * eval);
        }
        return score;
    }
}
