package org.matsim.utils.collections;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * An implementation of a QuadTree to store geometric point data. The expected
 * bounds of all added points must be given to the constructor for working
 * properly. While the data structure will still work if points outside the
 * given bounds are added, the performance is likely to drop to that of a linked
 * list.<br />
 * At one location, several different objects can be put. An object can be put
 * to the QuadTree at different locations. But an object cannot be put more than
 * once at the same location.
 *
 * @author mrieser
 * @param <T> The type of data to be stored in the QuadTree.
 */
public class QuadTree<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The top node or root of the tree */
    protected Node<T> top = null;

    /** The number of entries in the tree */
    private int size = 0;

    /** The number of structural modifications to the tree. */
    private transient int modCount = 0;

    /**
	 * A cache to store all values of the QuadTree so it does not have to be
	 * computed for every call to {@link #values()}. This is similar to
	 * TreeMap.java and AbstractMap.java
	 */
    transient volatile Collection<T> values = null;

    private void incrementSize() {
        this.modCount++;
        this.size++;
        this.values = null;
    }

    private void decrementSize() {
        this.modCount++;
        this.size--;
        this.values = null;
    }

    /**
	 * Creates an empty QuadTree with the bounds minX/minY -- maxX/maxY. For
	 * optimal performance, all points should be evenly distributed within this
	 * rectangle.
	 *
	 * @param minX The smallest x coordinate (easting, longitude) expected
	 * @param minY The smallest y coordinate (northing, latitude) expected
	 * @param maxX The largest x coordinate (easting, longitude) expected
	 * @param maxY The largest y coordinate (northing, latitude) expected
	 */
    public QuadTree(final double minX, final double minY, final double maxX, final double maxY) {
        setTopNode(minX, minY, maxX, maxY);
    }

    /**
	 * Associates the specified value with the specified coordinates in this
	 * QuadTree.
	 *
	 * @param x x-coordinate where the specified value is to be associated.
	 * @param y y-coordinate where the specified value is to be associated.
	 * @param value value to be associated with the specified coordinates.
	 *
	 * @return true if insertion was successful and the data structure changed,
	 *         false otherwise.
	 */
    public boolean put(final double x, final double y, final T value) {
        if (this.top.put(x, y, value)) {
            incrementSize();
            return true;
        }
        return false;
    }

    /**
	 * Removes the specified object from the specified location.
	 *
	 * @param x x-coordinate from which the specified value should be removed
	 * @param y y-coordinate from which the specified value should be removed
	 * @param value the value to be removed from the specified coordinates
	 *
	 * @return true if the specified value was found at the specified coordinates
	 *         and was successfully removed (data structure changed), false
	 *         otherwise.
	 */
    public boolean remove(final double x, final double y, final T value) {
        if (this.top.remove(x, y, value)) {
            decrementSize();
            return true;
        }
        return false;
    }

    /** Clear the QuadTree. */
    public void clear() {
        this.top.clear();
        this.size = 0;
        this.modCount++;
    }

    /**
	 * Gets the object closest to x/y
	 *
	 * @param x easting, left-right location, longitude
	 * @param y northing, up-down location, latitude
	 * @return the object found closest to x/y
	 */
    public T get(final double x, final double y) {
        return this.top.get(x, y, new MutableDouble(Double.POSITIVE_INFINITY));
    }

    /**
	 * Gets all objects within a certain distance around x/y
	 *
	 * @param x left-right location, longitude
	 * @param y up-down location, latitude
	 * @param distance the maximal distance returned objects can be away from x/y
	 * @return the objects found within distance to x/y
	 */
    public Collection<T> get(final double x, final double y, final double distance) {
        return this.top.get(x, y, distance, new ArrayList<T>());
    }

    /**
	 * Gets all objects inside the specified boundary. Objects on the border of the
	 * boundary are not included.
	 *
	 * @param bounds The bounds of the area of interest.
	 * @param values A collection to store the found objects in.
	 * @return The objects found within the area.
	 */
    public Collection<T> get(final Rect bounds, final Collection<T> values) {
        return this.top.get(bounds, values);
    }

    /**
	 * Gets all objects inside the specified area. Objects on the border of
	 * the area are not included.
	 *
	 * @param minX The minimum left-right location, longitude
	 * @param minY The minimum up-down location, latitude
	 * @param maxX The maximum left-right location, longitude
	 * @param maxY The maximum up-down location, latitude
	 * @param values A collection to store the found objects in.
	 * @return The objects found within the area.
	 */
    public Collection<T> get(final double minX, final double minY, final double maxX, final double maxY, final Collection<T> values) {
        return get(new Rect(minX, minY, maxX, maxY), values);
    }

    /**
	 * Executes executor on all objects inside a certain boundary
	 *
	 * @param bounds The boundary in which the executor will be applied.
	 * @param executor is executed on the fitting objects
	 * @return the count of objects found within the bounds.
	 */
    public int execute(final Rect bounds, final Executor<T> executor) {
        if (bounds == null) {
            return this.top.execute(this.top.getBounds(), executor);
        }
        return this.top.execute(bounds, executor);
    }

    /**
	 * Executes executor on all objects inside the rectangle (minX,minY):(maxX,maxY)
	 *
	 * @param minX The minimum left-right location, longitude
	 * @param minY The minimum up-down location, latitude
	 * @param maxX The maximum left-right location, longitude
	 * @param maxY The maximum up-down location, latitude
	 * @param executor is executed on the fitting objects
	 * @return the count of objects found within the rectangle.
	 */
    public int execute(final double minX, final double minY, final double maxX, final double maxY, final Executor<T> executor) {
        return execute(new Rect(minX, minY, maxX, maxY), executor);
    }

    /**
	 * Returns the number of entries in this QuadTree.
	 *
	 * @return the number of entries in this QuadTree.
	 */
    public int size() {
        return this.size;
    }

    /**
	 * Sets a new top node in case the extremities from the c'tor are not
	 * good anymore, it also clear the QuadTree
	 * @param minX The smallest x coordinate expected
	 * @param minY The smallest y coordinate expected
	 * @param maxX The largest x coordinate expected
	 * @param maxY The largest y coordinate expected
	 */
    protected void setTopNode(final double minX, final double minY, final double maxX, final double maxY) {
        this.top = new Node<T>(minX, minY, maxX, maxY);
    }

    /** @return the minimum x coordinate (left-right, longitude, easting) of the bounds of the QuadTree. */
    public double getMinEasting() {
        return this.top.getBounds().minX;
    }

    /** @return the maximum x coordinate (left-right, longitude, easting) of the bounds of the QuadTree. */
    public double getMaxEasting() {
        return this.top.getBounds().maxX;
    }

    /** @return the minimum y coordinate (up-down, latitude, northing) of the bounds of the QuadTree. */
    public double getMinNorthing() {
        return this.top.getBounds().minY;
    }

    /** @return the minimum y coordinate (up-down, latitude, northing) of the bounds of the QuadTree. */
    public double getMaxNorthing() {
        return this.top.getBounds().maxY;
    }

    /**
   * Returns a collection view of the values contained in this map.  The
   * collection's iterator will return the values in the order that their
   * corresponding keys appear in the tree.  The collection is backed by
   * this <tt>TreeMap</tt> instance, so changes to this map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map through
   * the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
    public Collection<T> values() {
        if (this.values == null) {
            this.values = new AbstractCollection<T>() {

                @Override
                public Iterator<T> iterator() {
                    Iterator<T> iterator = new Iterator<T>() {

                        private Leaf<T> currentLeaf = firstLeaf();

                        private int nextIndex = 0;

                        private T next = first();

                        private T first() {
                            if (this.currentLeaf == null) {
                                return null;
                            }
                            this.nextIndex = 0;
                            loadNext();
                            return this.next;
                        }

                        public boolean hasNext() {
                            return this.next != null;
                        }

                        public T next() {
                            if (this.next == null) {
                                return null;
                            }
                            T current = this.next;
                            loadNext();
                            return current;
                        }

                        public void loadNext() {
                            boolean searching = true;
                            while (searching) {
                                if (this.nextIndex < this.currentLeaf.values.size()) {
                                    this.nextIndex++;
                                    this.next = this.currentLeaf.values.get(this.nextIndex - 1);
                                    searching = false;
                                } else {
                                    this.currentLeaf = nextLeaf(this.currentLeaf);
                                    if (this.currentLeaf == null) {
                                        this.next = null;
                                        searching = false;
                                    } else {
                                        this.nextIndex = 0;
                                    }
                                }
                            }
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                    return iterator;
                }

                @Override
                public int size() {
                    return QuadTree.this.size;
                }
            };
        }
        return this.values;
    }

    private Leaf<T> firstLeaf() {
        return this.top.firstLeaf();
    }

    private Leaf<T> nextLeaf(final Leaf<T> currentLeaf) {
        return this.top.nextLeaf(currentLeaf);
    }

    /**
	 * An internal class to hold variable parameters when calling methods.
	 * Here a double value is packaged within an object so the value can be
	 * changed in a method and the changed value is available outside of a method.
	 */
    private static class MutableDouble {

        public double value;

        public MutableDouble(final double value) {
            this.value = value;
        }
    }

    /**
	 * An internal class to hold variable parameters when calling methods.
	 * Here a Leaf value is packaged within an object so the value can be
	 * changed in a method and the changed value is available outside of a method.
	 *
	 * @param <T> the type for the Leaf
	 */
    private static class MutableLeaf<T> {

        public Leaf<T> value;

        public MutableLeaf(final Leaf<T> value) {
            this.value = value;
        }
    }

    public static class Rect implements Serializable {

        private static final long serialVersionUID = -837712701959689133L;

        public final double minX;

        public final double minY;

        public final double maxX;

        public final double maxY;

        public final double centerX;

        public final double centerY;

        public Rect(final double minX, final double minY, final double maxX, final double maxY) {
            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.centerX = (minX + maxX) / 2;
            this.centerY = (minY + maxY) / 2;
        }

        /**
		 * Calculates the distance of a given point to the border of the
		 * rectangle. If the point lies within the rectangle, the distance
		 * is zero.
		 *
		 * @param x left-right location
		 * @param y up-down location
		 * @return distance to border, 0 if inside rectangle or on border
		 */
        public double calcDistance(final double x, final double y) {
            double distanceX;
            double distanceY;
            if (this.minX <= x && x <= this.maxX) {
                distanceX = 0;
            } else {
                distanceX = Math.min(Math.abs(this.minX - x), Math.abs(this.maxX - x));
            }
            if (this.minY <= y && y <= this.maxY) {
                distanceY = 0;
            } else {
                distanceY = Math.min(Math.abs(this.minY - y), Math.abs(this.maxY - y));
            }
            return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        }

        /**
		 * Tests if a specified coordinate is inside the boundary of this <code>Rect</code>.
		 * @param x the x-coordinate to test
		 * @param y the y-coordinate to test
		 * @return <code>true</code> if the specified coordinates are
		 * inside the boundary of this <code>Rect</code>;
		 * <code>false</code> otherwise.
		 */
        public boolean contains(final double x, final double y) {
            return (x >= this.minX && y >= this.minY && x < this.maxX && y < this.maxY);
        }

        /**
		 * Tests if a specified rect is inside or on the boundary of this <code>Rect</code>.
		 * @param rect the rect to test
		 * @return <code>true</code> if the specified rect is
		 * inside or on the boundary of this <code>Rect</code>;
		 * <code>false</code> otherwise.
		 */
        public boolean containsOrEquals(final Rect rect) {
            return (rect.minX >= this.minX && rect.minY >= this.minY && rect.maxX <= this.maxX && rect.maxY <= this.maxY);
        }

        /**
		 * Tests if the interior of this <code>Rect</code>
		 * intersects the interior of another <code>Rect</code>.
		 * @param other The rectangle that should be tested for intersection.
		 * @return <code>true</code> if this <code>Rect</code>
		 * intersects the interior of the other <code>Rect</code>; <code>false</code> otherwise.
		 */
        public boolean intersects(final Rect other) {
            if ((this.maxX - this.minX) <= 0 || (this.maxY - this.minY) <= 0) {
                return false;
            }
            return (other.maxX > this.minX && other.maxY > this.minY && other.minX < this.maxX && other.minY < this.maxY);
        }

        /**
	     * Computes the intersection of this <code>Rect</code> with the 
	     * specified <code>Rect</code>. Returns a new <code>Rect</code> 
	     * that represents the intersection of the two rectangles.
	     * If the two rectangles do not intersect, the result will be
	     * null.
	     *
	     * @param     r   the specified <code>Rectangle</code>
	     * @return    the largest <code>Rectangle</code> contained in both the 
	     *            specified <code>Rectangle</code> and in 
	     *		  this <code>Rectangle</code>; or if the rectangles
	     *            do not intersect, an empty rectangle.
	     */
        public Rect intersection(Rect r) {
            double tx1 = this.minX;
            double ty1 = this.minY;
            double tx2 = this.maxX;
            double ty2 = this.maxY;
            if (this.minX < r.minX) tx1 = r.minX;
            if (this.minY < r.minY) ty1 = r.minY;
            if (tx2 > r.maxX) tx2 = r.maxX;
            if (ty2 > r.maxY) ty2 = r.maxY;
            if (tx2 - tx1 <= 0.f || ty2 - ty1 <= 0.f) return null;
            return new Rect(tx1, ty1, tx2, ty2);
        }

        /**
	     * Adds a <code>Rect</code> to this <code>Rect</code>. 
	     * The resulting <code>Rect</code> is the union of the two
	     * rectangles. 
	     * @param  r the specified <code>Rect</code>
	     */
        public Rect union(Rect r) {
            return new Rect(Math.min(minX, r.minX), Math.min(minY, r.minY), Math.max(maxX, r.maxX), Math.max(maxY, r.maxY));
        }

        public Rect scale(double scaleX, double scaleY) {
            scaleY *= centerY - minY;
            scaleX *= centerX - minX;
            return new Rect(minX - scaleX, minY - scaleY, maxX + scaleX, maxY + scaleY);
        }
    }

    private static class Leaf<T> implements Serializable {

        private static final long serialVersionUID = -6527830222532634476L;

        public final double x;

        public final double y;

        public final ArrayList<T> values;

        public Leaf(final double x, final double y, final T value) {
            this.x = x;
            this.y = y;
            this.values = new ArrayList<T>(1);
            this.values.add(value);
        }
    }

    protected static class Node<T> implements Serializable {

        private static final long serialVersionUID = 8151154226742383421L;

        private Leaf<T> leaf = null;

        private boolean hasChilds = false;

        private Node<T> northwest = null;

        private Node<T> northeast = null;

        private Node<T> southeast = null;

        private Node<T> southwest = null;

        private Rect bounds = null;

        public Node(final double minX, final double minY, final double maxX, final double maxY) {
            this.bounds = new Rect(minX, minY, maxX, maxY);
        }

        public boolean put(final Leaf<T> leaf) {
            if (this.hasChilds) return getChild(leaf.x, leaf.y).put(leaf);
            if (this.leaf == null) {
                this.leaf = leaf;
                return true;
            }
            if (this.leaf.x == leaf.x && this.leaf.y == leaf.y) {
                boolean changed = false;
                for (T value : leaf.values) {
                    if (!this.leaf.values.contains(value)) {
                        changed = this.leaf.values.add(value) || changed;
                    }
                }
                return changed;
            }
            this.split();
            return getChild(leaf.x, leaf.y).put(leaf);
        }

        public boolean put(final double x, final double y, final T value) {
            return put(new Leaf<T>(x, y, value));
        }

        public boolean remove(final double x, final double y, final T value) {
            if (this.hasChilds) return getChild(x, y).remove(x, y, value);
            if (this.leaf != null && this.leaf.x == x && this.leaf.y == y) {
                if (this.leaf.values.remove(value)) {
                    if (this.leaf.values.size() == 0) {
                        this.leaf = null;
                    }
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            if (this.hasChilds) {
                this.northwest.clear();
                this.northeast.clear();
                this.southeast.clear();
                this.southwest.clear();
                this.northwest = null;
                this.northeast = null;
                this.southeast = null;
                this.southwest = null;
                this.hasChilds = false;
            } else {
                if (this.leaf != null) {
                    this.leaf.values.clear();
                    this.leaf = null;
                }
            }
        }

        T get(final double x, final double y, final MutableDouble bestDistance) {
            if (this.hasChilds) {
                T closest = null;
                if (this.northwest.bounds.calcDistance(x, y) < bestDistance.value) {
                    T value = this.northwest.get(x, y, bestDistance);
                    if (value != null) {
                        closest = value;
                    }
                }
                if (this.northeast.bounds.calcDistance(x, y) < bestDistance.value) {
                    T value = this.northeast.get(x, y, bestDistance);
                    if (value != null) {
                        closest = value;
                    }
                }
                if (this.southeast.bounds.calcDistance(x, y) < bestDistance.value) {
                    T value = this.southeast.get(x, y, bestDistance);
                    if (value != null) {
                        closest = value;
                    }
                }
                if (this.southwest.bounds.calcDistance(x, y) < bestDistance.value) {
                    T value = this.southwest.get(x, y, bestDistance);
                    if (value != null) {
                        closest = value;
                    }
                }
                return closest;
            }
            if (this.leaf != null && this.leaf.values.size() > 0) {
                T value = this.leaf.values.get(0);
                double distance = Math.sqrt((this.leaf.x - x) * (this.leaf.x - x) + (this.leaf.y - y) * (this.leaf.y - y));
                if (distance < bestDistance.value) {
                    bestDistance.value = distance;
                    return value;
                }
            }
            return null;
        }

        Collection<T> get(final double x, final double y, final double maxDistance, final Collection<T> values) {
            if (this.hasChilds) {
                if (this.northwest.bounds.calcDistance(x, y) <= maxDistance) {
                    this.northwest.get(x, y, maxDistance, values);
                }
                if (this.northeast.bounds.calcDistance(x, y) <= maxDistance) {
                    this.northeast.get(x, y, maxDistance, values);
                }
                if (this.southeast.bounds.calcDistance(x, y) <= maxDistance) {
                    this.southeast.get(x, y, maxDistance, values);
                }
                if (this.southwest.bounds.calcDistance(x, y) <= maxDistance) {
                    this.southwest.get(x, y, maxDistance, values);
                }
                return values;
            }
            if (this.leaf != null && this.leaf.values.size() > 0) {
                double distance = Math.sqrt((this.leaf.x - x) * (this.leaf.x - x) + (this.leaf.y - y) * (this.leaf.y - y));
                if (distance <= maxDistance) {
                    values.addAll(this.leaf.values);
                }
            }
            return values;
        }

        Collection<T> get(final Rect bounds, final Collection<T> values) {
            if (this.hasChilds) {
                if (this.northwest.bounds.intersects(bounds)) {
                    this.northwest.get(bounds, values);
                }
                if (this.northeast.bounds.intersects(bounds)) {
                    this.northeast.get(bounds, values);
                }
                if (this.southeast.bounds.intersects(bounds)) {
                    this.southeast.get(bounds, values);
                }
                if (this.southwest.bounds.intersects(bounds)) {
                    this.southwest.get(bounds, values);
                }
                return values;
            }
            if (this.leaf != null && this.leaf.values.size() > 0) {
                if (bounds.contains(this.leaf.x, this.leaf.y)) {
                    values.addAll(this.leaf.values);
                }
            }
            return values;
        }

        int execute(final Rect globalBounds, final Executor<T> executor) {
            int count = 0;
            if (this.hasChilds) {
                if (this.northwest.bounds.intersects(globalBounds)) {
                    count += this.northwest.execute(globalBounds, executor);
                }
                if (this.northeast.bounds.intersects(globalBounds)) {
                    count += this.northeast.execute(globalBounds, executor);
                }
                if (this.southeast.bounds.intersects(globalBounds)) {
                    count += this.southeast.execute(globalBounds, executor);
                }
                if (this.southwest.bounds.intersects(globalBounds)) {
                    count += this.southwest.execute(globalBounds, executor);
                }
                return count;
            }
            if (this.leaf != null && this.leaf.values.size() > 0) {
                if (globalBounds.contains(this.leaf.x, this.leaf.y)) {
                    count += this.leaf.values.size();
                    for (T object : this.leaf.values) executor.execute(this.leaf.x, this.leaf.y, object);
                }
            }
            return count;
        }

        private void split() {
            this.northwest = new Node<T>(this.bounds.minX, this.bounds.centerY, this.bounds.centerX, this.bounds.maxY);
            this.northeast = new Node<T>(this.bounds.centerX, this.bounds.centerY, this.bounds.maxX, this.bounds.maxY);
            this.southeast = new Node<T>(this.bounds.centerX, this.bounds.minY, this.bounds.maxX, this.bounds.centerY);
            this.southwest = new Node<T>(this.bounds.minX, this.bounds.minY, this.bounds.centerX, this.bounds.centerY);
            this.hasChilds = true;
            if (this.leaf != null) {
                getChild(this.leaf.x, this.leaf.y).put(this.leaf);
                this.leaf = null;
            }
        }

        private Node<T> getChild(final double x, final double y) {
            if (this.hasChilds) {
                if (x < this.bounds.centerX) {
                    if (y < this.bounds.centerY) return this.southwest;
                    return this.northwest;
                }
                if (y < this.bounds.centerY) return this.southeast;
                return this.northeast;
            }
            return null;
        }

        Leaf<T> firstLeaf() {
            if (this.hasChilds) {
                Leaf<T> leaf = this.southwest.firstLeaf();
                if (leaf == null) {
                    leaf = this.northwest.firstLeaf();
                }
                if (leaf == null) {
                    leaf = this.southeast.firstLeaf();
                }
                if (leaf == null) {
                    leaf = this.northeast.firstLeaf();
                }
                return leaf;
            }
            return this.leaf;
        }

        boolean nextLeaf(final Leaf<T> currentLeaf, final MutableLeaf<T> nextLeaf) {
            if (this.hasChilds) {
                if (!this.bounds.contains(currentLeaf.x, currentLeaf.y)) return false;
                boolean found = this.southwest.nextLeaf(currentLeaf, nextLeaf);
                if (found) {
                    if (nextLeaf.value == null) {
                        nextLeaf.value = this.northwest.firstLeaf();
                    }
                    if (nextLeaf.value == null) {
                        nextLeaf.value = this.southeast.firstLeaf();
                    }
                    if (nextLeaf.value == null) {
                        nextLeaf.value = this.northeast.firstLeaf();
                    }
                    return true;
                }
                found = this.northwest.nextLeaf(currentLeaf, nextLeaf);
                if (found) {
                    if (nextLeaf.value == null) {
                        nextLeaf.value = this.southeast.firstLeaf();
                    }
                    if (nextLeaf.value == null) {
                        nextLeaf.value = this.northeast.firstLeaf();
                    }
                    return true;
                }
                found = this.southeast.nextLeaf(currentLeaf, nextLeaf);
                if (found) {
                    if (nextLeaf.value == null) {
                        nextLeaf.value = this.northeast.firstLeaf();
                    }
                    return true;
                }
                return this.northeast.nextLeaf(currentLeaf, nextLeaf);
            }
            return currentLeaf == this.leaf;
        }

        public Leaf<T> nextLeaf(final Leaf<T> currentLeaf) {
            MutableLeaf<T> nextLeaf = new MutableLeaf<T>(null);
            nextLeaf(currentLeaf, nextLeaf);
            return nextLeaf.value;
        }

        public Rect getBounds() {
            return this.bounds;
        }
    }

    public abstract static class Executor<T> {

        public abstract void execute(double x, double y, T object);
    }
}
