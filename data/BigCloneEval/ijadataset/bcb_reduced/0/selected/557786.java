package org.dyn4j.collision.broadphase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dyn4j.collision.Collidable;
import org.dyn4j.collision.narrowphase.NarrowphaseDetector;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Interval;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Shape;
import org.dyn4j.geometry.Vector2;

/**
 * Implementation of the Sweep and Prune broad-phase collision detection algorithm.
 * <p>
 * This implementation maintains an unsorted list of {@link Collidable}s and each time
 * the {@link #detect()} method is called the list is resorted.
 * <p>
 * Projects all {@link Collidable}s on both the x and y axes and performs overlap checks
 * on all the projections to test for possible collisions (AABB tests).
 * <p>
 * The overlap checks are performed faster when the {@link Interval}s created by the projections
 * are sorted by their minimum value.  Doing so will allow the detector to ignore any projections
 * after the first {@link Interval} that does not overlap.
 * <p>
 * If a {@link Collidable} is made up of more than one {@link Shape} and the {@link Shape}s 
 * are not connected, this detection algorithm may cause false hits.  For example,
 * if your {@link Collidable} consists of the following geometry: (the line below the {@link Shape}s
 * is the projection that will be used for the broad-phase)
 * <pre>
 * +--------+     +--------+  |
 * | body1  |     | body1  |  |
 * | shape1 |     | shape2 |  | y-axis projection
 * |        |     |        |  |
 * +--------+     +--------+  |
 * 
 * -------------------------
 *     x-axis projection
 * </pre>
 * So if following configuration is encountered it will generate a hit:
 * <pre>
 *             +--------+               |
 * +--------+  | body2  |  +--------+   | |
 * | body1  |  | shape1 |  | body1  |   | |
 * | shape1 |  |        |  | shape2 |   | | y-axis projection
 * |        |  |        |  |        |   | |
 * +--------+  |        |  +--------+   | |
 *             +--------+               |
 * 
 *             ----------
 * ----------------------------------
 *         x-axis projection
 * </pre>
 * These cases are OK since the {@link NarrowphaseDetector}s will handle these cases.
 * However, allowing this causes more work for the {@link NarrowphaseDetector}s whose
 * algorithms are more complex.  These situations should be avoided for maximum performance.
 * @author William Bittle
 * @version 3.1.0
 * @since 1.0.0
 * @param <E> the {@link Collidable} type
 */
public class SapBruteForce<E extends Collidable> extends AbstractAABBDetector<E> implements BroadphaseDetector<E> {

    /**
	 * Internal class to hold the {@link Collidable} to {@link AABB} relationship.
	 * @author William Bittle
	 * @version 3.0.0
	 * @since 3.0.0
	 */
    protected class Proxy implements Comparable<Proxy> {

        /** The collidable */
        public E collidable;

        /** The collidable's aabb */
        public AABB aabb;

        public int compareTo(Proxy o) {
            if (this == o) return 0;
            double diff = this.aabb.getMinX() - o.aabb.getMinX();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                diff = this.aabb.getMinY() - o.aabb.getMinY();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    return this.collidable.getId().compareTo(o.collidable.getId());
                }
            }
        }

        @Override
        public String toString() {
            return aabb.toString();
        }
    }

    /**
	 * Represents a list of potential pairs.
	 * @author William Bittle
	 * @version 3.0.0
	 * @since 3.0.0
	 */
    protected class PairList {

        /** The proxy */
        public Proxy proxy;

        /** The proxy's potential pairs */
        public List<Proxy> potentials = new ArrayList<Proxy>();
    }

    /** Sorted list of proxies */
    protected List<Proxy> proxyList;

    /** Id to proxy map for fast lookup */
    protected Map<String, Proxy> proxyMap;

    /** Reusable list for storing detected pairs */
    protected ArrayList<BroadphasePair<E>> pairs;

    /** Flag used to indicate that the proxyList must be sorted before use */
    protected boolean sort = false;

    /** Default constructor. */
    public SapBruteForce() {
        this(50);
    }

    /**
	 * Full constructor.
	 * <p>
	 * Allows fine tuning of the initial capacity of local storage for faster running times.
	 * @param initialCapacity the initial capacity of local storage
	 */
    public SapBruteForce(int initialCapacity) {
        this.proxyList = new ArrayList<Proxy>(initialCapacity);
        this.proxyMap = new HashMap<String, Proxy>(initialCapacity);
        int eSize = ((initialCapacity * initialCapacity) - initialCapacity) / 10;
        this.pairs = new ArrayList<BroadphasePair<E>>(eSize);
    }

    @Override
    public void add(E collidable) {
        String id = collidable.getId();
        AABB aabb = collidable.createAABB();
        aabb.expand(this.expansion);
        Proxy p = new Proxy();
        p.collidable = collidable;
        p.aabb = aabb;
        this.proxyList.add(p);
        this.proxyMap.put(id, p);
        this.sort = true;
    }

    @Override
    public void remove(E collidable) {
        Iterator<Proxy> it = this.proxyList.iterator();
        while (it.hasNext()) {
            Proxy p = it.next();
            if (p.collidable == collidable) {
                it.remove();
                break;
            }
        }
        this.proxyMap.remove(collidable.getId());
    }

    @Override
    public void update(E collidable) {
        Proxy p0 = this.proxyMap.get(collidable.getId());
        if (p0 == null) return;
        AABB aabb = collidable.createAABB();
        if (p0.aabb.contains(aabb)) {
            return;
        } else {
            aabb.expand(this.expansion);
        }
        p0.aabb = aabb;
        this.sort = true;
    }

    @Override
    public void clear() {
        this.proxyList.clear();
        this.proxyMap.clear();
    }

    @Override
    public AABB getAABB(E collidable) {
        Proxy proxy = this.proxyMap.get(collidable.getId());
        if (proxy != null) {
            return proxy.aabb;
        }
        return null;
    }

    @Override
    public List<BroadphasePair<E>> detect() {
        int size = proxyList.size();
        if (size == 0) {
            this.pairs.clear();
            return this.pairs;
        }
        int eSize = ((size * size) - size) / 10;
        this.pairs.clear();
        this.pairs.ensureCapacity(eSize);
        if (this.sort) {
            Collections.sort(this.proxyList);
            this.sort = false;
        }
        List<PairList> potentialPairs = new ArrayList<PairList>(size);
        PairList pl = new PairList();
        for (int i = 0; i < size; i++) {
            Proxy current = this.proxyList.get(i);
            for (int j = i + 1; j < size; j++) {
                Proxy test = this.proxyList.get(j);
                if (current.aabb.getMaxX() >= test.aabb.getMinX()) {
                    pl.potentials.add(test);
                } else {
                    break;
                }
            }
            if (pl.potentials.size() > 0) {
                pl.proxy = current;
                potentialPairs.add(pl);
                pl = new PairList();
            }
        }
        size = potentialPairs.size();
        for (int i = 0; i < size; i++) {
            PairList current = potentialPairs.get(i);
            int pls = current.potentials.size();
            for (int j = 0; j < pls; j++) {
                Proxy test = current.potentials.get(j);
                if (current.proxy.aabb.overlaps(test.aabb)) {
                    BroadphasePair<E> pair = new BroadphasePair<E>();
                    pair.a = current.proxy.collidable;
                    pair.b = test.collidable;
                    this.pairs.add(pair);
                }
            }
        }
        return this.pairs;
    }

    @Override
    public List<E> detect(AABB aabb) {
        int size = this.proxyList.size();
        List<E> list;
        if (size == 0) {
            return new ArrayList<E>();
        } else {
            list = new ArrayList<E>(size);
        }
        if (this.sort) {
            Collections.sort(this.proxyList);
            this.sort = false;
        }
        int index = size / 2;
        int max = size;
        int min = 0;
        while (true) {
            Proxy p = this.proxyList.get(index);
            if (p.aabb.getMinX() < aabb.getMinX()) {
                min = index;
            } else {
                max = index;
            }
            if (max - min == 1) {
                break;
            }
            index = (min + max) / 2;
        }
        for (int i = 0; i < size; i++) {
            Proxy p = this.proxyList.get(i);
            if (p.aabb.getMaxX() > aabb.getMinX()) {
                if (p.aabb.overlaps(aabb)) {
                    list.add(p.collidable);
                }
            } else {
                if (i >= index) break;
            }
        }
        return list;
    }

    @Override
    public List<E> raycast(Ray ray, double length) {
        if (this.proxyList.size() == 0) {
            return new ArrayList<E>();
        }
        Vector2 s = ray.getStart();
        Vector2 d = ray.getDirectionVector();
        double l = length;
        if (length <= 0.0) l = Double.MAX_VALUE;
        double x1 = s.x;
        double x2 = s.x + d.x * l;
        double y1 = s.y;
        double y2 = s.y + d.y * l;
        Vector2 min = new Vector2(Math.min(x1, x2), Math.min(y1, y2));
        Vector2 max = new Vector2(Math.max(x1, x2), Math.max(y1, y2));
        AABB aabb = new AABB(min, max);
        return this.detect(aabb);
    }

    @Override
    public void shiftCoordinates(Vector2 shift) {
        int pSize = this.proxyList.size();
        for (int i = 0; i < pSize; i++) {
            Proxy proxy = this.proxyList.get(i);
            proxy.aabb.translate(shift);
        }
    }
}
