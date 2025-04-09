package visad;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
   Gridded1DDoubleSet is a Gridded1DSet with double-precision samples.<P>
*/
public class Gridded1DDoubleSet extends Gridded1DSet implements GriddedDoubleSet {

    double[] Low = new double[1];

    double[] Hi = new double[1];

    double LowX, HiX;

    double[][] Samples;

    /**
   * A canonicalizing cache of previously-created instances.  Because instances
   * are immutable, a cache can be used to reduce memory usage by ensuring
   * that each instance is truely unique.  By implementing the cache using a
   * {@link WeakHashMap}, this can be accomplished without the technique itself
   * adversely affecting memory usage.
   */
    private static final WeakHashMap cache = new WeakHashMap();

    /** a 1-D sequence with no regular interval with null errors,
      CoordinateSystem and Units are defaults from type */
    public Gridded1DDoubleSet(MathType type, float[][] samples, int lengthX) throws VisADException {
        this(type, Set.floatToDouble(samples), lengthX, null, null, null, true);
    }

    public Gridded1DDoubleSet(MathType type, float[][] samples, int lengthX, CoordinateSystem coord_sys, Unit[] units, ErrorEstimate[] errors) throws VisADException {
        this(type, Set.floatToDouble(samples), lengthX, coord_sys, units, errors, true);
    }

    /** a 1-D sorted sequence with no regular interval. samples array
      is organized float[1][number_of_samples] where lengthX =
      number_of_samples. samples must be sorted (either increasing
      or decreasing). coordinate_system and units must be compatible
      with defaults for type, or may be null. errors may be null */
    public Gridded1DDoubleSet(MathType type, float[][] samples, int lengthX, CoordinateSystem coord_sys, Unit[] units, ErrorEstimate[] errors, boolean copy) throws VisADException {
        this(type, Set.floatToDouble(samples), lengthX, coord_sys, units, errors, copy);
    }

    /** a 1-D sequence with no regular interval with null errors,
      CoordinateSystem and Units are defaults from type */
    public Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX) throws VisADException {
        this(type, samples, lengthX, null, null, null, true);
    }

    public Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX, CoordinateSystem coord_sys, Unit[] units, ErrorEstimate[] errors) throws VisADException {
        this(type, samples, lengthX, coord_sys, units, errors, true);
    }

    /** a 1-D sorted sequence with no regular interval. samples array
      is organized double[1][number_of_samples] where lengthX =
      number_of_samples. samples must be sorted (either increasing
      or decreasing). coordinate_system and units must be compatible
      with defaults for type, or may be null. errors may be null */
    public Gridded1DDoubleSet(MathType type, double[][] samples, int lengthX, CoordinateSystem coord_sys, Unit[] units, ErrorEstimate[] errors, boolean copy) throws VisADException {
        super(type, null, lengthX, coord_sys, units, errors, copy);
        if (samples == null) {
            throw new SetException("Gridded1DDoubleSet: samples are null");
        }
        init_doubles(samples, copy);
        LowX = Low[0];
        HiX = Hi[0];
        LengthX = Lengths[0];
        if (Samples != null && Lengths[0] > 1) {
            for (int i = 0; i < Length; i++) {
                if (Samples[0][i] != Samples[0][i]) {
                    throw new SetException("Gridded1DDoubleSet: samples values may not be missing");
                }
            }
            Ascending = (Samples[0][LengthX - 1] > Samples[0][0]);
            if (Ascending) {
                for (int i = 1; i < LengthX; i++) {
                    if (Samples[0][i] < Samples[0][i - 1]) {
                        throw new SetException("Gridded1DDoubleSet: samples do not form a valid grid (" + i + ")");
                    }
                }
            } else {
                for (int i = 1; i < LengthX; i++) {
                    if (Samples[0][i] > Samples[0][i - 1]) {
                        throw new SetException("Gridded1DDoubleSet: samples do not form a valid grid (" + i + ")");
                    }
                }
            }
        }
    }

    /**
   * Returns an instance of this class.  This method uses a weak cache of
   * previously-created instances to reduce memory usage.
   *
   * @param type                The type of the set.  Must be a {@link 
   *                            RealType} or a single-component {@link
   *                            RealTupleType} or {@link SetType}.
   * @param samples             The values in the set.
   *                            <code>samples[i]</code> is the value of
   *                            the ith sample point.  Must be sorted (either
   *                            increasing or decreasing).  May be
   *                            <code>null</code>.  The array is not copied, so
   *                            either don't modify it or clone it first.
   * @param coord_sys           The coordinate system for this, particular, set.
   *                            Must be compatible with the default coordinate
   *                            system.  May be <code>null</code>.
   * @param unit                The unit for the samples.  Must be compatible
   *                            with the default unit.  May be 
   *                            <code>null</code>.
   * @param error               The error estimate of the samples.  May be
   *                            <code>null</code>.
   */
    public static synchronized Gridded1DDoubleSet create(MathType type, double[] samples, CoordinateSystem coordSys, Unit unit, ErrorEstimate error) throws VisADException {
        Gridded1DDoubleSet newSet = new Gridded1DDoubleSet(type, new double[][] { samples }, samples.length, coordSys, new Unit[] { unit }, new ErrorEstimate[] { error }, false);
        WeakReference ref = (WeakReference) cache.get(newSet);
        if (ref == null) {
            cache.put(newSet, new WeakReference(newSet));
        } else {
            Gridded1DDoubleSet oldSet = (Gridded1DDoubleSet) ref.get();
            if (oldSet == null) {
                cache.put(newSet, new WeakReference(newSet));
            } else {
                newSet = oldSet;
            }
        }
        return newSet;
    }

    public float[][] getSamples() throws VisADException {
        return getSamples(true);
    }

    public float[][] getSamples(boolean copy) throws VisADException {
        return Set.doubleToFloat(Samples);
    }

    /** convert an array of 1-D indices to an array of values in
      R^DomainDimension */
    public float[][] indexToValue(int[] index) throws VisADException {
        return Set.doubleToFloat(indexToDouble(index));
    }

    /**
   * Convert an array of values in R^DomainDimension to an array of
   * 1-D indices.  This Gridded1DDoubleSet must have at least two points in the
   * set.
   * @param value       An array of coordinates.  <code>value[i][j]
   *                    <code> contains the <code>i</code>th component of the
   *                    <code>j</code>th point.
   * @return            Indices of nearest points.  RETURN_VALUE<code>[i]</code>
   *                    will contain the index of the point in the set closest
   *                    to <code>value[][i]</code> or <code>-1</code> if
   *                    <code>value[][i]</code> lies outside the set.
   */
    public int[] valueToIndex(float[][] value) throws VisADException {
        return doubleToIndex(Set.floatToDouble(value));
    }

    public float[][] gridToValue(float[][] grid) throws VisADException {
        return Set.doubleToFloat(gridToDouble(Set.floatToDouble(grid)));
    }

    /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
    public float[][] valueToGrid(float[][] value) throws VisADException {
        return Set.doubleToFloat(doubleToGrid(Set.floatToDouble(value)));
    }

    /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
    public void valueToInterp(float[][] value, int[][] indices, float[][] weights) throws VisADException {
        int len = weights.length;
        double[][] w = new double[len][];
        doubleToInterp(Set.floatToDouble(value), indices, w);
        for (int i = 0; i < len; i++) {
            if (w[i] != null) {
                weights[i] = new float[w[i].length];
                for (int j = 0; j < w[i].length; j++) {
                    weights[i][j] = (float) w[i][j];
                }
            }
        }
    }

    public float getLowX() {
        return (float) LowX;
    }

    public float getHiX() {
        return (float) HiX;
    }

    public double[][] getDoubles() throws VisADException {
        return getDoubles(true);
    }

    public double[][] getDoubles(boolean copy) throws VisADException {
        return copy ? Set.copyDoubles(Samples) : Samples;
    }

    /** convert an array of 1-D indices to an array of values in
      R^DomainDimension */
    public double[][] indexToDouble(int[] index) throws VisADException {
        int length = index.length;
        if (Samples == null) {
            double[][] grid = new double[ManifoldDimension][length];
            for (int i = 0; i < length; i++) {
                if (0 <= index[i] && index[i] < Length) {
                    grid[0][i] = (double) index[i];
                } else {
                    grid[0][i] = -1;
                }
            }
            return gridToDouble(grid);
        } else {
            double[][] values = new double[1][length];
            for (int i = 0; i < length; i++) {
                if (0 <= index[i] && index[i] < Length) {
                    values[0][i] = Samples[0][index[i]];
                } else {
                    values[0][i] = Double.NaN;
                }
            }
            return values;
        }
    }

    public int[] doubleToIndex(double[][] value) throws VisADException {
        if (value.length != DomainDimension) {
            throw new SetException("Gridded1DDoubleSet.doubleToIndex: value dimension " + value.length + " not equal to Domain dimension " + DomainDimension);
        }
        int length = value[0].length;
        int[] index = new int[length];
        double[][] grid = doubleToGrid(value);
        double[] grid0 = grid[0];
        double g;
        for (int i = 0; i < length; i++) {
            g = grid0[i];
            index[i] = Double.isNaN(g) ? -1 : ((int) (g + 0.5));
        }
        return index;
    }

    /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
    public double[][] gridToDouble(double[][] grid) throws VisADException {
        if (grid.length < DomainDimension) {
            throw new SetException("Gridded1DDoubleSet.gridToDouble: grid dimension " + grid.length + " not equal to Domain dimension " + DomainDimension);
        }
        int length = grid[0].length;
        double[][] value = new double[1][length];
        for (int i = 0; i < length; i++) {
            double g = grid[0][i];
            if ((g < -0.5) || (g > LengthX - 0.5)) {
                value[0][i] = Double.NaN;
            } else if (Length == 1) {
                value[0][i] = Samples[0][0];
            } else {
                int ig;
                if (g < 0) ig = 0; else if (g >= LengthX - 1) ig = LengthX - 2; else ig = (int) g;
                double A = g - ig;
                value[0][i] = (1 - A) * Samples[0][ig] + A * Samples[0][ig + 1];
            }
        }
        return value;
    }

    private int ig = -1;

    /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
    public double[][] doubleToGrid(double[][] value) throws VisADException {
        if (value.length < DomainDimension) {
            throw new SetException("Gridded1DDoubleSet.doubleToGrid: value dimension " + value.length + " not equal to Domain dimension " + DomainDimension);
        }
        double[] vals = value[0];
        int length = vals.length;
        double[] samps = Samples[0];
        double[][] grid = new double[1][length];
        if (ig < 0 || ig >= LengthX) {
            ig = (LengthX - 1) / 2;
        }
        for (int i = 0; i < length; i++) {
            if (Double.isNaN(vals[i])) {
                grid[0][i] = Double.NaN;
            } else if (Length == 1) {
                grid[0][i] = 0;
            } else {
                int lower = 0;
                int upper = LengthX - 1;
                while (lower < upper) {
                    if ((vals[i] - samps[ig]) * (vals[i] - samps[ig + 1]) <= 0) break;
                    if (Ascending ? samps[ig + 1] < vals[i] : samps[ig + 1] > vals[i]) {
                        lower = ig + 1;
                    } else if (Ascending ? samps[ig] > vals[i] : samps[ig] < vals[i]) {
                        upper = ig;
                    }
                    if (lower < upper) ig = (lower + upper) / 2;
                }
                double solv = ig + (vals[i] - samps[ig]) / (samps[ig + 1] - samps[ig]);
                if (solv > -0.5 && solv < LengthX - 0.5) grid[0][i] = solv; else {
                    grid[0][i] = Double.NaN;
                    ig = (LengthX - 1) / 2;
                }
            }
        }
        return grid;
    }

    /** for each of an array of values in R^DomainDimension, compute an array
      of 1-D indices and an array of weights, to be used for interpolation;
      indices[i] and weights[i] are null if i-th value is outside grid
      (i.e., if no interpolation is possible) */
    public void doubleToInterp(double[][] value, int[][] indices, double[][] weights) throws VisADException {
        if (value.length != DomainDimension) {
            throw new SetException("Gridded1DDoubleSet.doubleToInterp: value dimension " + value.length + " not equal to Domain dimension " + DomainDimension);
        }
        int length = value[0].length;
        if (indices.length != length) {
            throw new SetException("Gridded1DDoubleSet.doubleToInterp: indices length " + indices.length + " doesn't match value[0] length " + value[0].length);
        }
        if (weights.length != length) {
            throw new SetException("Gridded1DDoubleSet.doubleToInterp: weights length " + weights.length + " doesn't match value[0] length " + value[0].length);
        }
        double[][] grid = doubleToGrid(value);
        int i, j, k;
        int lis;
        int length_is;
        int isoff;
        double a, b;
        int[] is;
        double[] cs;
        int base;
        int[] l = new int[ManifoldDimension];
        double[] c = new double[ManifoldDimension];
        int[] off = new int[ManifoldDimension];
        off[0] = 1;
        for (j = 1; j < ManifoldDimension; j++) off[j] = off[j - 1] * Lengths[j - 1];
        for (i = 0; i < length; i++) {
            length_is = 1;
            if (Double.isNaN(grid[ManifoldDimension - 1][i])) {
                base = -1;
            } else {
                l[ManifoldDimension - 1] = (int) (grid[ManifoldDimension - 1][i] + 0.5);
                if (l[ManifoldDimension - 1] == Lengths[ManifoldDimension - 1]) {
                    l[ManifoldDimension - 1]--;
                }
                c[ManifoldDimension - 1] = grid[ManifoldDimension - 1][i] - ((double) l[ManifoldDimension - 1]);
                if (!((l[ManifoldDimension - 1] == 0 && c[ManifoldDimension - 1] <= 0.0) || (l[ManifoldDimension - 1] == Lengths[ManifoldDimension - 1] - 1 && c[ManifoldDimension - 1] >= 0.0))) {
                    length_is *= 2;
                }
                base = l[ManifoldDimension - 1];
            }
            for (j = ManifoldDimension - 2; j >= 0 && base >= 0; j--) {
                if (Double.isNaN(grid[j][i])) {
                    base = -1;
                } else {
                    l[j] = (int) (grid[j][i] + 0.5);
                    if (l[j] == Lengths[j]) l[j]--;
                    c[j] = grid[j][i] - ((double) l[j]);
                    if (!((l[j] == 0 && c[j] <= 0.0) || (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
                        length_is *= 2;
                    }
                    base = l[j] + Lengths[j] * base;
                }
            }
            if (base < 0) {
                is = null;
                cs = null;
            } else {
                is = new int[length_is];
                cs = new double[length_is];
                is[0] = base;
                cs[0] = 1.0f;
                lis = 1;
                for (j = 0; j < ManifoldDimension; j++) {
                    if (!((l[j] == 0 && c[j] <= 0.0) || (l[j] == Lengths[j] - 1 && c[j] >= 0.0))) {
                        if (c[j] >= 0.0) {
                            isoff = off[j];
                            a = 1.0f - c[j];
                            b = c[j];
                        } else {
                            isoff = -off[j];
                            a = 1.0f + c[j];
                            b = -c[j];
                        }
                        for (k = 0; k < lis; k++) {
                            is[k + lis] = is[k] + isoff;
                            cs[k + lis] = cs[k] * b;
                            cs[k] *= a;
                        }
                        lis *= 2;
                    }
                }
            }
            indices[i] = is;
            weights[i] = cs;
        }
    }

    public double getDoubleLowX() {
        return LowX;
    }

    public double getDoubleHiX() {
        return HiX;
    }

    void init_doubles(double[][] samples, boolean copy) throws VisADException {
        if (samples.length != DomainDimension) {
            throw new SetException("Gridded1DDoubleSet.init_doubles:" + " samples dimension " + samples.length + " not equal to Domain dimension " + DomainDimension);
        }
        if (Length == 0) {
            Length = samples[0].length;
        } else {
            if (Length != samples[0].length) {
                throw new SetException("Gridded1DDoubleSet.init_doubles: samples[0] length " + samples[0].length + " doesn't match expected length " + Length);
            }
        }
        if (copy) {
            Samples = new double[DomainDimension][Length];
        } else {
            Samples = samples;
        }
        for (int j = 0; j < DomainDimension; j++) {
            if (samples[j].length != Length) {
                throw new SetException("Gridded1DDoubleSet.init_doubles: samples[" + j + "] length " + samples[0].length + " doesn't match expected length " + Length);
            }
            double[] samplesJ = samples[j];
            double[] SamplesJ = Samples[j];
            if (copy) {
                System.arraycopy(samplesJ, 0, SamplesJ, 0, Length);
            }
            Low[j] = Double.POSITIVE_INFINITY;
            Hi[j] = Double.NEGATIVE_INFINITY;
            double sum = 0.0f;
            for (int i = 0; i < Length; i++) {
                if (SamplesJ[i] == SamplesJ[i] && !Double.isInfinite(SamplesJ[i])) {
                    if (SamplesJ[i] < Low[j]) Low[j] = SamplesJ[i];
                    if (SamplesJ[i] > Hi[j]) Hi[j] = SamplesJ[i];
                } else {
                    SamplesJ[i] = Double.NaN;
                }
                sum += SamplesJ[i];
            }
            if (SetErrors[j] != null) {
                SetErrors[j] = new ErrorEstimate(SetErrors[j].getErrorValue(), sum / Length, Length, SetErrors[j].getUnit());
            }
            super.Low[j] = (float) Low[j];
            super.Hi[j] = (float) Hi[j];
        }
    }

    public void cram_missing(boolean[] range_select) {
        int n = Math.min(range_select.length, Samples[0].length);
        for (int i = 0; i < n; i++) {
            if (!range_select[i]) Samples[0][i] = Double.NaN;
        }
    }

    public boolean isMissing() {
        return (Samples == null);
    }

    public boolean equals(Object set) {
        if (!(set instanceof Gridded1DDoubleSet) || set == null) return false;
        if (this == set) return true;
        if (testNotEqualsCache((Set) set)) return false;
        if (testEqualsCache((Set) set)) return true;
        if (!equalUnitAndCS((Set) set)) return false;
        try {
            int i, j;
            if (DomainDimension != ((Gridded1DDoubleSet) set).getDimension() || ManifoldDimension != ((Gridded1DDoubleSet) set).getManifoldDimension() || Length != ((Gridded1DDoubleSet) set).getLength()) return false;
            for (j = 0; j < ManifoldDimension; j++) {
                if (Lengths[j] != ((Gridded1DDoubleSet) set).getLength(j)) {
                    return false;
                }
            }
            double[][] samples = ((Gridded1DDoubleSet) set).getDoubles(false);
            if (Samples != null && samples != null) {
                for (j = 0; j < DomainDimension; j++) {
                    for (i = 0; i < Length; i++) {
                        if (Samples[j][i] != samples[j][i]) {
                            addNotEqualsCache((Set) set);
                            return false;
                        }
                    }
                }
            } else {
                double[][] this_samples = getDoubles(false);
                if (this_samples == null) {
                    if (samples != null) {
                        return false;
                    }
                } else if (samples == null) {
                    return false;
                } else {
                    for (j = 0; j < DomainDimension; j++) {
                        for (i = 0; i < Length; i++) {
                            if (this_samples[j][i] != samples[j][i]) {
                                addNotEqualsCache((Set) set);
                                return false;
                            }
                        }
                    }
                }
            }
            addEqualsCache((Set) set);
            return true;
        } catch (VisADException e) {
            return false;
        }
    }

    /**
   * Returns the hash code of this instance. {@link Object#hashCode()} should be
   * overridden whenever {@link Object#equals(Object)} is.
   * @return                    The hash code of this instance (includes the
   *                            values).
   */
    public int hashCode() {
        if (!hashCodeSet) {
            hashCode = unitAndCSHashCode();
            hashCode ^= DomainDimension ^ ManifoldDimension ^ Length;
            for (int j = 0; j < ManifoldDimension; j++) hashCode ^= Lengths[j];
            if (Samples != null) for (int j = 0; j < DomainDimension; j++) for (int i = 0; i < Length; i++) hashCode ^= new Double(Samples[j][i]).hashCode();
            hashCodeSet = true;
        }
        return hashCode;
    }

    /**
   * Clones this instance.
   *
   * @return                    A clone of this instance.
   */
    public Object clone() {
        Gridded1DDoubleSet clone = (Gridded1DDoubleSet) super.clone();
        if (Samples != null) {
            clone.Samples = (double[][]) Samples.clone();
            for (int i = 0; i < Samples.length; i++) clone.Samples[i] = (double[]) Samples[i].clone();
        }
        return clone;
    }

    public Object cloneButType(MathType type) throws VisADException {
        return new Gridded1DDoubleSet(type, Samples, Length, DomainCoordinateSystem, SetUnits, SetErrors);
    }
}
