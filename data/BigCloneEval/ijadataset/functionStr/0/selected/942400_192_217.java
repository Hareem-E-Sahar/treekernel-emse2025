public class Test {    public int inverseFInt(double u) {
        int i, j, k;
        if (u < 0.0 || u > 1.0) throw new IllegalArgumentException("u is not in [0,1]");
        if (u <= 0.0) return supportA;
        if (u >= 1.0) return supportB;
        if (u <= cdf[xmed - xmin]) {
            if (u <= cdf[0]) return xmin;
            i = 0;
            j = xmed - xmin;
            while (i < j) {
                k = (i + j) / 2;
                if (u > cdf[k]) i = k + 1; else j = k;
            }
        } else {
            u = 1 - u;
            if (u < cdf[xmax - xmin]) return xmax;
            i = xmed - xmin + 1;
            j = xmax - xmin;
            while (i < j) {
                k = (i + j) / 2;
                if (u < cdf[k]) i = k + 1; else j = k;
            }
            i--;
        }
        return i + xmin;
    }
}