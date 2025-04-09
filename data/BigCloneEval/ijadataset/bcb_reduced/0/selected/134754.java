package cunei.util;

import java.io.Serializable;
import cunei.bits.ResizableUnsignedArray;
import cunei.bits.UnsignedArray;

public class IntegerBoundIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int CACHE_SHIFT = 6;

    private transient UnsignedArray cache;

    private UnsignedArray bounds;

    public IntegerBoundIndex() {
        bounds = new ResizableUnsignedArray();
    }

    public final boolean contains(int lower, int upper) {
        final int id = getId(lower);
        return getUpperBound(id) == upper && getLowerBound(id) == lower;
    }

    public final Bounds getBounds(int id) {
        return new Bounds(getLowerBound(id), getUpperBound(id));
    }

    public final int getId(int bound) {
        int lower;
        int upper;
        if (cache == null) {
            lower = 0;
            upper = size() - 1;
        } else {
            final int key = bound >> CACHE_SHIFT;
            lower = (int) cache.get(key);
            upper = (int) cache.get(key + 1);
        }
        while (lower != upper) {
            int id = lower + (upper - lower) / 2;
            int loc = getUpperBound(id);
            if (loc > bound) upper = id; else if (loc < bound) lower = id + 1; else return id + 1;
        }
        return lower;
    }

    public final int getLowerBound(int id) {
        return id == 0 ? 0 : getUpperBound(id - 1);
    }

    public final int getUpperBound(int id) {
        return (int) bounds.get(id);
    }

    public final void load(String path) {
        bounds.load(path);
        cache = new ResizableUnsignedArray();
        int bound = 0;
        for (int id = 0; id < bounds.size(); id++) {
            int upper = getUpperBound(id);
            while (bound << CACHE_SHIFT < upper) {
                cache.set(bound, id);
                bound++;
            }
        }
        cache.set(bound, size() - 1);
    }

    public final void remove(String path) {
        bounds.remove(path);
    }

    public IntegerBoundIndex save(String path, String file) {
        bounds = bounds.save(path, file);
        return this;
    }

    public void setUpperBound(int pos, int upperBound) {
        cache = null;
        bounds.set(pos, upperBound);
    }

    public final int size() {
        return bounds.size();
    }
}
