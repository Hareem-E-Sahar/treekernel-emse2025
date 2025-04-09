package bagaturchess.search.impl.utils;

import bagaturchess.search.api.internal.ISearch;

public class SearchUtils {

    public static final int getMateVal(int depth) {
        if (depth <= 0) {
            throw new IllegalStateException("depth=" + depth);
        }
        return ISearch.MAX_MAT_INTERVAL * (ISearch.MAX_DEPTH - depth + 1);
    }

    public static final boolean isMateVal(int val) {
        val = Math.abs(val);
        return val != ISearch.MAX && val >= ISearch.MAX_MAT_INTERVAL;
    }

    public static final int getMateDepth(int score) {
        int norm_score = Math.abs(score) / ISearch.MAX_MAT_INTERVAL;
        int depth = ISearch.MAX_DEPTH + 1 - norm_score;
        depth = (depth + 1) / 2;
        return score > 0 ? depth : -depth;
    }

    public static final int normDepth(int maxdepth) {
        return maxdepth / ISearch.PLY;
    }
}
