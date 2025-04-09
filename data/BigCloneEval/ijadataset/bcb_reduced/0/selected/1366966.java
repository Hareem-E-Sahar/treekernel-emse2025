package org.exolab.jms.common.util;

import java.util.Comparator;
import java.util.Vector;

/**
 * The OrderedQueue is responsible for managing the expiration of the leases.
 * The LeaseComparator is used to determine where they are inserted and the
 * lease with the shortest duration is removed from the queue first. It is
 * implemented suing a Vector but this could be changed to improve performance.
 *
 * @author <a href="mailto:jima@exoffice.com">Jim Alateras</a>
 * @version $Revision: 1.1 $ $Date: 2004/11/26 01:50:35 $
 */
public class OrderedQueue {

    /***
     * The queue
     */
    private Vector _queue = null;

    /**
     * The comparator for ordering the queue
     */
    private Comparator _comparator = null;

    /**
     * Construct an instance of this class with the comparator to order the
     * elements in the queue. Elements with the same order value are placed
     * after each other.
     *
     * @param comparator used for ordering
     */
    public OrderedQueue(Comparator comparator) {
        _comparator = comparator;
        _queue = new Vector();
    }

    /**
     * Add this element to the queue in the required order. It uses a binary
     * search to locate the correct position
     *
     * @param object object to add
     */
    public synchronized void add(Object object) {
        if (_queue.size() == 0) {
            _queue.addElement(object);
        } else {
            int start = 0;
            int end = _queue.size() - 1;
            if (_comparator.compare(object, _queue.firstElement()) < 0) {
                _queue.insertElementAt(object, 0);
            } else if (_comparator.compare(object, _queue.lastElement()) > 0) {
                _queue.addElement(object);
            } else {
                while (true) {
                    int midpoint = start + (end - start) / 2;
                    if (((end - start) % 2) != 0) {
                        midpoint++;
                    }
                    int result = _comparator.compare(object, _queue.elementAt(midpoint));
                    if (result == 0) {
                        _queue.insertElementAt(object, midpoint);
                        break;
                    } else if ((start + 1) == end) {
                        _queue.insertElementAt(object, end);
                        break;
                    } else {
                        if (result > 0) {
                            start = midpoint;
                        } else {
                            end = midpoint;
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove the object from the queue
     *
     * @param object object to remove
     * @return <code>true</code> if the object was removed
     */
    public synchronized boolean remove(Object object) {
        return _queue.remove(object);
    }

    /**
     * Remove all the elements from the queue
     */
    public synchronized void clear() {
        _queue.clear();
    }

    /**
     * Return the number elements in the queue
     *
     * @return int         size of the queue
     */
    public int size() {
        return _queue.size();
    }

    /**
     * Return the first element on the queue
     *
     * @return Object
     */
    public Object firstElement() {
        return _queue.firstElement();
    }

    /**
     * Remove the first element from the queue or null if there are no elements
     * on the queue.
     *
     * @return Object
     */
    public synchronized Object removeFirstElement() {
        return _queue.remove(0);
    }
}
