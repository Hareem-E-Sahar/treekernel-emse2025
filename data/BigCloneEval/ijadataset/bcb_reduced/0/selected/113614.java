package net.nexttext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class SpatialList {

    static final int LEFT = 0, RIGHT = 1, TOP = 2, BOTTOM = 3;

    LinkedList<Edge> xAxis = new LinkedList<Edge>();

    LinkedList<Edge> yAxis = new LinkedList<Edge>();

    /**
     * An edge of a TextObject.
     */
    class Edge implements Comparable<Edge> {

        int position;

        TextObjectGlyph to;

        Edge(TextObjectGlyph to, int position) {
            this.to = to;
            this.position = position;
        }

        float getValue() {
            if (position == LEFT) return (float) to.getBounds().getMinX();
            if (position == RIGHT) return (float) to.getBounds().getMaxX();
            if (position == TOP) return (float) to.getBounds().getMinY();
            if (position == BOTTOM) return (float) to.getBounds().getMaxY();
            throw new RuntimeException("Invalid Position: " + position);
        }

        public int compareTo(Edge e) {
            return Float.compare(getValue(), e.getValue());
        }
    }

    HashMap<TextObject, HashSet<TextObjectGlyph>> xCollisions = new HashMap<TextObject, HashSet<TextObjectGlyph>>();

    HashMap<TextObject, HashSet<TextObjectGlyph>> yCollisions = new HashMap<TextObject, HashSet<TextObjectGlyph>>();

    int tests = 0;

    int avrg = 0;

    /**
	 * Sorts the X and Y axis interval lists. 
	 */
    public void update() {
        sort(xAxis, 0);
        sort(yAxis, 1);
        avrg += tests;
        avrg /= 2;
        tests = 0;
    }

    /**
     * Get position in the xAxis and yAxis lists for each edge of a TextObject.
     *
     * <p>An array of 4 elements is returned, each of which is an index within
     * the axes (xAxis and yAxis) where the corresponding edge should be
     * inserted.  The returned array is indexed using the static finals 'LEFT',
     * 'RIGHT', 'TOP', and 'BOTTOM'.  </p>
     *
     * <p>The returned indices are determined from the current state of the
     * list.  When both edges have been added to the list in their correct
     * places, the positions of the RIGHT, and BOTTOM edges will be off by one
     * from the returned values, because the LEFT and TOP edges have also been
     * inserted in the lists in a lower position.  </p>
	 */
    private int[] getPosition(TextObjectGlyph to) {
        int[] position = { 0, 0, 0, 0 };
        if (xAxis.size() != 0) {
            position[LEFT] = binarySearch(xAxis, (float) to.getBounds().getMinX());
            position[RIGHT] = binarySearch(xAxis, (float) to.getBounds().getMaxX());
        }
        if (yAxis.size() != 0) {
            position[TOP] = binarySearch(yAxis, (float) to.getBounds().getMinY());
            position[BOTTOM] = binarySearch(yAxis, (float) to.getBounds().getMaxY());
        }
        return position;
    }

    /**
     * Determine the index in the provided list where an Edge with the given
     * value should be placed.
	 */
    private int binarySearch(LinkedList<Edge> list, float value) {
        int lower = 0, middle, upper = list.size() - 1;
        while (upper >= lower) {
            middle = (upper + lower) / 2;
            int result = Float.compare(value, list.get(middle).getValue());
            if (result > 0) lower = middle + 1; else if (result < 0) upper = middle - 1; else return middle;
        }
        return lower;
    }

    /**
	 * Adds a single TextObjectGlyph to the spatial list
	 */
    public void add(TextObjectGlyph to) {
        if (to.toString().equals(" ")) {
            return;
        }
        int[] position = getPosition(to);
        if (position[RIGHT] >= xAxis.size() - position[LEFT]) {
            xAxis.add(position[RIGHT], new Edge(to, RIGHT));
            xAxis.add(new Edge(to, LEFT));
        } else {
            xAxis.add(position[LEFT], new Edge(to, LEFT));
            xAxis.addFirst(new Edge(to, RIGHT));
        }
        if (position[BOTTOM] >= yAxis.size() - position[TOP]) {
            yAxis.add(position[BOTTOM], new Edge(to, BOTTOM));
            yAxis.add(new Edge(to, TOP));
        } else {
            yAxis.add(position[TOP], new Edge(to, TOP));
            yAxis.addFirst(new Edge(to, BOTTOM));
        }
        xCollisions.put(to, new HashSet<TextObjectGlyph>());
        yCollisions.put(to, new HashSet<TextObjectGlyph>());
        sort(xAxis, 0);
        sort(yAxis, 1);
    }

    /**
	 * Removes an object from the spatial list
	 */
    public void remove(TextObjectGlyph to) {
        Iterator<Edge> ei = xAxis.iterator();
        while (ei.hasNext()) {
            if (ei.next().to == to) ei.remove();
        }
        ei = yAxis.iterator();
        while (ei.hasNext()) {
            if (ei.next().to == to) ei.remove();
        }
        HashSet<TextObjectGlyph> xcol = xCollisions.get(to);
        HashSet<TextObjectGlyph> ycol = yCollisions.get(to);
        if (xcol != null) {
            for (Iterator<TextObjectGlyph> i = xcol.iterator(); i.hasNext(); ) {
                HashSet<TextObjectGlyph> temp = xCollisions.get(i.next());
                temp.remove(to);
            }
        }
        if (ycol != null) {
            for (Iterator<TextObjectGlyph> i = ycol.iterator(); i.hasNext(); ) {
                HashSet<TextObjectGlyph> temp = yCollisions.get(i.next());
                temp.remove(to);
            }
        }
        xCollisions.remove(to);
        yCollisions.remove(to);
    }

    /**
	 * Adds all the glyphs part of a TextObjectGroup to the spatial list.
	 */
    public void add(TextObjectGroup tog) {
        TextObjectIterator toi = new TextObjectIterator(tog);
        while (toi.hasNext()) {
            TextObject to = toi.next();
            if (to instanceof TextObjectGlyph) {
                add((TextObjectGlyph) to);
            }
        }
    }

    /**
	 * Adds a TextObject to the spatial list.  Use this method to avoid casting
	 * the object as group or a glyph
	 */
    public void add(TextObject to) {
        if (to instanceof TextObjectGlyph) {
            add((TextObjectGlyph) to);
        }
        if (to instanceof TextObjectGroup) {
            add((TextObjectGroup) to);
        }
    }

    /**
	 * Removes a TextObject to the spatial list.  Use this method to avoid casting
	 * the object as group or a glyph
	 */
    public void remove(TextObject to) {
        if (to instanceof TextObjectGlyph) {
            remove((TextObjectGlyph) to);
        }
        if (to instanceof TextObjectGroup) {
            remove((TextObjectGroup) to);
        }
    }

    /**
	 * Removes all the glyphs part of a TextObjectGroup from the spatial list
	 */
    public void remove(TextObjectGroup tog) {
        TextObjectIterator toi = new TextObjectIterator(tog);
        while (toi.hasNext()) {
            TextObject to = toi.next();
            if (to instanceof TextObjectGlyph) {
                remove((TextObjectGlyph) to);
            }
        }
    }

    /**
	 * Redirects to the proper implementation of getPotentialCollisions based
	 * on type (TextObjectGlyph or TextObjectGroup)
	 */
    public HashSet<TextObjectGlyph> getPotentialCollisions(TextObject to) {
        if (to instanceof TextObjectGlyph) {
            return getPotentialCollisions((TextObjectGlyph) to);
        }
        if (to instanceof TextObjectGroup) {
            return getPotentialCollisions((TextObjectGroup) to);
        }
        return null;
    }

    /**
	 * Given a TextObjectGlyph, get a list of objects which's bounding box
	 * are overlapping.  It returns an empty hashset if there is none. 
	 *
	 * @param to  A TextObjectGlyph to test for collisions
	 */
    public HashSet<TextObjectGlyph> getPotentialCollisions(TextObjectGlyph to) {
        HashSet<TextObjectGlyph> collisions = new HashSet<TextObjectGlyph>();
        HashSet<TextObjectGlyph> xCol = xCollisions.get(to);
        HashSet<TextObjectGlyph> yCol = yCollisions.get(to);
        if (xCol == null || yCol == null) {
            String msg = "Collisions query for object not in SpatialList: " + to;
            throw new ObjectNotFoundException(msg);
        }
        if (xCol.size() == 0 || yCol.size() == 0) return collisions;
        Iterator<TextObjectGlyph> i = xCol.iterator();
        while (i.hasNext()) {
            TextObjectGlyph someTo = i.next();
            if (yCol.contains(someTo)) {
                collisions.add(someTo);
            }
        }
        return collisions;
    }

    /**
	 * Given a TextObjectGroup, find all the glyphs which's bounding boxes are 
	 * overlapping with any of the given group's glyphs.  Returns an empty set if 
	 * no objects are colliding with the group.
	 */
    public HashSet<TextObjectGlyph> getPotentialCollisions(TextObjectGroup tog) {
        HashSet<TextObjectGlyph> collisions = new HashSet<TextObjectGlyph>();
        TextObjectIterator toi = new TextObjectIterator(tog);
        while (toi.hasNext()) {
            TextObject to = toi.next();
            if (to instanceof TextObjectGlyph) {
                collisions.addAll(getPotentialCollisions((TextObjectGlyph) to));
            }
        }
        return collisions;
    }

    /**
	 * Returns the average number of collision tests performed by the sorting
	 * function
	 */
    public int getNumCollisionTests() {
        return avrg;
    }

    /**
	 * Sort objects  in a list using  insertion sort.
	 *
	 * Normally insertion sort has O(n2) running time, however because of 
	 * spatial coherence we can expect the lists to be almost sorted, resulting
	 * in an expected O(n) running time.
	 *
	 * Every time a swap is peformed, update the "overlap" status for the two
	 * objects invovled is updated.
	 * 
	 * @param list a list of edges to sort
	 * @param axis 0-X axis, 1-Y axis
	 */
    private void sort(LinkedList<Edge> list, int axis) {
        int n = list.size();
        int j = 0;
        for (int i = 1; i < n; i++) {
            j = i - 1;
            Edge a = list.get(i);
            while (j >= 0 && (a.compareTo(list.get(j)) < 0)) {
                swap(list, j + 1, j, axis);
                j--;
                tests++;
            }
        }
    }

    /**
	 * swaps two elements i and j in the interval list.  updates their overlap 
	 * status to reflect the new changes
	 */
    private void swap(LinkedList<Edge> list, int i, int j, int axis) {
        list.set(i, (list.set(j, list.get(i))));
        TextObjectGlyph glyphA = list.get(i).to;
        TextObjectGlyph glyphB = list.get(j).to;
        if (glyphA.parent == glyphB.parent) {
            return;
        }
        float s1, e1;
        float s2, e2;
        if (axis == 0) {
            s1 = (float) glyphA.getBounds().getMinX();
            e1 = (float) glyphA.getBounds().getMaxX();
            s2 = (float) glyphB.getBounds().getMinX();
            e2 = (float) glyphB.getBounds().getMaxX();
        } else {
            s1 = (float) glyphA.getBounds().getMinY();
            e1 = (float) glyphA.getBounds().getMaxY();
            s2 = (float) glyphB.getBounds().getMinY();
            e2 = (float) glyphB.getBounds().getMaxY();
        }
        if (intervalOverlap(s1, e1, s2, e2)) {
            if (axis == 0) {
                xCollisions.get(glyphA).add(glyphB);
                xCollisions.get(glyphB).add(glyphA);
            } else {
                yCollisions.get(glyphA).add(glyphB);
                yCollisions.get(glyphB).add(glyphA);
            }
        } else {
            if (axis == 0) {
                xCollisions.get(glyphA).remove(glyphB);
                xCollisions.get(glyphB).remove(glyphA);
            } else {
                yCollisions.get(glyphA).remove(glyphB);
                yCollisions.get(glyphB).remove(glyphA);
            }
        }
    }

    /**
	 * Determine if intervals [s1, e1] and [s2, e2] overlap or not
	 */
    private boolean intervalOverlap(float s1, float e1, float s2, float e2) {
        if ((e2 > s1) && (e1 > s2)) return true;
        return false;
    }
}
