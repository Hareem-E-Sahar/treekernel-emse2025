import java.util.*;

class Protein {

    private final Vector<String> proteinInfo;

    private double[] spectralCounts;

    ProteinPermutation nullHypothesis;

    Protein(Vector<String> proteinInfo, double[] spectralCounts) {
        this.proteinInfo = proteinInfo;
        this.spectralCounts = spectralCounts;
    }

    Protein(Vector<String> proteinInfo, int nCounts) {
        this.proteinInfo = proteinInfo;
        this.spectralCounts = new double[nCounts];
    }

    Vector<String> getProteinInfo() {
        return proteinInfo;
    }

    double getGStatistic() {
        return this.getNullHypothesis().getGStatistic();
    }

    public void insertCount(double value, int slot) {
        if (slot < spectralCounts.length) {
            spectralCounts[slot] = value;
        }
    }

    double[] getSpectralCounts() {
        return this.spectralCounts;
    }

    ProteinPermutation doPermutation(CombinationPair combo) {
        int[] leftIndices = combo.getLeft();
        int[] rightIndices = combo.getRight();
        double[] left = new double[leftIndices.length];
        double[] right = new double[rightIndices.length];
        double lmean = 0;
        double rmean = 0;
        for (int i = leftIndices.length; i-- > 0; ) {
            left[i] = this.spectralCounts[leftIndices[i]];
            lmean += left[i];
            right[i] = this.spectralCounts[rightIndices[i]];
            rmean += right[i];
        }
        lmean /= leftIndices.length;
        rmean /= leftIndices.length;
        double t = this.calculatePValue(left, right, lmean, rmean);
        double g = this.calculateGStatistic(lmean, rmean);
        if (Double.isNaN(t)) {
            t = 1;
        }
        return new ProteinPermutation(g, t);
    }

    ProteinPermutation getNullHypothesis() {
        return nullHypothesis;
    }

    String ar2str(int[] left, int[] right) {
        String ret = "";
        for (int i : left) {
            ret += i;
        }
        ret += " - ";
        for (int i : right) {
            ret += i;
        }
        return ret;
    }

    double calculatePValue(double[] left, double[] right, double lmean, double rmean) {
        double sdleft = 0;
        double sdright = 0;
        double len = left.length;
        double sqrtlen = Math.sqrt(len);
        for (double d : left) {
            d -= lmean;
            sdleft += d * d;
        }
        sdleft = (Math.sqrt(sdleft / (len - 1)));
        sdleft /= sqrtlen;
        sdleft = sdleft * sdleft;
        for (double d : right) {
            d -= rmean;
            sdright += d * d;
        }
        sdright = (Math.sqrt(sdright / (len - 1)));
        sdright /= sqrtlen;
        sdright = sdright * sdright;
        double tvalue = ((rmean - lmean) / Math.sqrt(sdright + sdleft));
        return studT(tvalue, ((len * 2) - 2));
    }

    double calculateGStatistic(double lmean, double rmean) {
        double rlmean = (lmean + rmean) / 2;
        double lentropy;
        double rentropy;
        if (lmean > 0.1) lentropy = lmean * Math.log(lmean / rlmean); else lentropy = 0;
        if (rmean > 0.1) rentropy = rmean * Math.log(rmean / rlmean); else rentropy = 0;
        return 2 * (lentropy + rentropy);
    }

    double studT(double t, double n) {
        double w = Math.abs(t) / Math.sqrt(n);
        double th = Math.atan(w);
        if (n == 1) {
            return 1 - th / (Math.PI / 2);
        }
        double sth = Math.sin(th);
        double cth = Math.cos(th);
        if ((n % 2) == 1) {
            return 1 - (th + sth * cth * statCom(cth * cth, 2, n - 3, -1)) / (Math.PI / 2);
        } else {
            return 1 - sth * statCom(cth * cth, 1, n - 3, -1);
        }
    }

    double statCom(double q, double i, double j, double b) {
        double zz = 1;
        double z = 1;
        double k = i;
        while (k <= j) {
            zz = zz * q * k / (k - b);
            z = z + zz;
            k = k + 2;
        }
        return z;
    }

    double getPValue() {
        return this.getNullHypothesis().getPValue();
    }
}
