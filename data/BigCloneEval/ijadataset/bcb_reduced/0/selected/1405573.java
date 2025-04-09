package org.jmathematics.calc.util.cache;

import org.jmathematics.calc.UniVariateDD;
import org.jmathematics.calc.util.UniVariateDDCache;

public class SimpleUniVariateDDCache implements UniVariateDDCache {

    private final UniVariateDDCache.CachePosition cachePosition;

    private final UniVariateDD calculator;

    private final double minVariant;

    private final double maxVariant;

    private final double minPosition;

    private final double maxPosition;

    private final double positionScale;

    private final int numValues;

    private double[] cache = null;

    private double[] variant = null;

    public SimpleUniVariateDDCache(UniVariateDD calculator, double minVariant, double maxVariant, int numValues, UniVariateDDCache.CachePosition cachePosition) {
        if (calculator == null) throw new NullPointerException("calculator");
        if (minVariant >= maxVariant) throw new IllegalArgumentException("minVariant [" + minVariant + "] >= maxVariant [" + maxVariant + "]");
        if (numValues < 2) throw new IllegalArgumentException("numValues [" + numValues + "] < 2");
        this.calculator = calculator;
        this.minVariant = minVariant;
        this.maxVariant = maxVariant;
        this.numValues = numValues;
        this.cachePosition = (cachePosition == null) ? LinearUniVariateDCachePosition.SINGLETON : cachePosition;
        this.minPosition = this.cachePosition.getCachePosition(minVariant);
        this.maxPosition = this.cachePosition.getCachePosition(maxVariant);
        this.positionScale = (maxPosition - minPosition) / (double) numValues;
    }

    private final double index2Position(int index) {
        return minPosition + (positionScale * index);
    }

    private final int position2Index(double position) {
        return (int) Math.round((position - minPosition) / positionScale);
    }

    protected void transformCachedValues(double[] cache, double[] variant) {
    }

    private final synchronized double[] variants() {
        if (variant != null) return variant;
        variant = new double[numValues];
        for (int i = 0; i < numValues; i++) variant[i] = cachePosition.getVariant(index2Position(i));
        return variant;
    }

    private final synchronized double[] cache() {
        if (cache != null) return cache;
        cache = new double[numValues];
        double[] variant = variants();
        for (int i = 0; i < numValues; i++) cache[i] = calculator.calc(variant[i]);
        transformCachedValues(cache, variant);
        return cache;
    }

    public final double getValue(double variant) {
        if (variant < minVariant) throw new IllegalArgumentException("variant [" + variant + "] < minVariant [" + minVariant + "]");
        if (variant > maxVariant) throw new IllegalArgumentException("variant [" + variant + "] > maxVariant [" + maxVariant + "]");
        double[] cache = cache();
        return cache[position2Index(cachePosition.getCachePosition(variant))];
    }

    private double getVariant(double[] cache, double[] variant, double value, int lower, int higher) {
        if (lower < higher) {
            while (lower < higher - 1) {
                if (value == cache[lower]) higher = lower + 1; else if (value == cache[higher]) lower = higher - 1; else {
                    int mid = (lower + higher) / 2;
                    if (value < cache[mid]) higher = mid; else lower = mid;
                }
            }
        } else {
            while (higher < lower - 1) {
                if (value == cache[lower]) higher = lower - 1; else if (value == cache[higher]) lower = higher + 1; else {
                    int mid = (lower + higher) / 2;
                    if (value < cache[mid]) higher = mid; else lower = mid;
                }
            }
        }
        if (value == cache[lower]) return variant[lower];
        if (value == cache[higher]) return variant[higher];
        double lower_part = (cache[higher] - value) / (cache[higher] - cache[lower]);
        double position = index2Position(lower) * lower_part + index2Position(higher) * (1.0 - lower_part);
        return cachePosition.getVariant(position);
    }

    public final double getVariant(double value) {
        double[] cache = cache();
        double[] variant = variants();
        if (cache[0] <= cache[numValues - 1]) {
            if (value < cache[0]) return Double.NEGATIVE_INFINITY;
            if (value > cache[numValues - 1]) return Double.POSITIVE_INFINITY;
            return getVariant(cache, variant, value, 0, numValues - 1);
        }
        if (value > cache[0]) return Double.POSITIVE_INFINITY;
        if (value < cache[numValues - 1]) return Double.NEGATIVE_INFINITY;
        return getVariant(cache, variant, value, numValues - 1, 0);
    }
}
