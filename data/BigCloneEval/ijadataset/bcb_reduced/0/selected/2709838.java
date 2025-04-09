package jopt.csp.spi.arcalgorithm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import jopt.csp.spi.arcalgorithm.graph.arc.Arc;

/**
 * Represents a queue of arcs to be propagated
 */
public class ArcQueue {

    private ArrayList<Arc> list;

    private int requiredMinComplexity;

    /**
     * Constructor
     */
    public ArcQueue() {
        this.list = new ArrayList<Arc>();
    }

    /**
     * Removes all arcs in the queue
     */
    public void clear() {
        list.clear();
    }

    /**
     * Returns size of arc queue
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if queue has a next arc available
     */
    public boolean hasNext() {
        return !list.isEmpty();
    }

    /**
     * Returns next arc on queue or null if none is available
     */
    public Arc next() {
        if (list.isEmpty()) return null;
        return (Arc) list.remove(0);
    }

    /**
     * Adds an arc to the queue
     */
    public void add(Arc arc) {
        int complexity = arc.getComplexity();
        if (complexity < requiredMinComplexity) return;
        if (!list.contains(arc)) {
            if (list.size() == 0) {
                list.add(arc);
            } else {
                int lowIdx = 0;
                int highIdx = list.size() - 1;
                int idx = 0;
                while (lowIdx <= highIdx) {
                    idx = (lowIdx + highIdx) / 2;
                    Arc curArc = (Arc) list.get(idx);
                    int curComplexity = curArc.getComplexity();
                    if (complexity == curComplexity) {
                        list.add(idx + 1, arc);
                        return;
                    }
                    if (complexity < curComplexity) highIdx = idx - 1; else lowIdx = idx + 1;
                }
                list.add(lowIdx, arc);
            }
        }
    }

    /**
     * Removes a node change from the queue
     */
    public boolean remove(Arc arc) {
        return list.remove(arc);
    }

    /**
     * Adds an arc to the queue
     */
    public void addAll(Collection<Arc> col) {
        Iterator<Arc> i = col.iterator();
        while (i.hasNext()) add((Arc) i.next());
    }

    public String toString() {
        return list.toString();
    }

    /**
     * Returns the smallest complexity value for an arc in the queue
     */
    public int getMinComplexity() {
        if (list.size() == 0) return -1;
        Arc firstArc = (Arc) list.get(0);
        return firstArc.getComplexity();
    }

    /**
     * Returns minimum complexity value that arc must return to be added to the queue
     */
    public int getRequiredMinComplexity() {
        return requiredMinComplexity;
    }

    /**
     * Sets the minimum complexity value that arc must return to be added to the queue
     */
    public void setRequiredMinComplexity(int requiredMinComplexity) {
        this.requiredMinComplexity = requiredMinComplexity;
    }
}
