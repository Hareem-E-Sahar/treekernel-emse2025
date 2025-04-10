package net.sf.saxon.sort;

/**
 * This is a generic version of C.A.R Hoare's Quick Sort 
 * algorithm.  This will handle arrays that are already
 * sorted, and arrays with duplicate keys.<p>
 *
 * @author Patrick C. Beard (beard@netscape.com)
 * Java Runtime Enthusiast -- "Will invoke interfaces for food."
 *
 * This code reached me (Michael Kay) via meteko.com; I'm assuming that it's OK
 * to use because they copied it freely to me.
 *
 * Modified by MHK in May 2001 to sort any object that implements the Sortable
 * interface, not only an array.
 *
 */
public abstract class QuickSort {

    /** This is a generic version of C.A.R Hoare's Quick Sort 
    * algorithm.  This will handle arrays that are already
    * sorted, and arrays with duplicate keys. <br>
    *
    * If you think of a one dimensional array as going from
    * the lowest index on the left to the highest index on the right
    * then the parameters to this function are lowest index or
    * left and highest index or right.  The first time you call
    * this function it will be with the parameters 0, a.length - 1.
    *
    * @param a       a Sortable object
    * @param lo0     index of first element (initially typically 0)
    * @param hi0     index of last element (initially typically length-1)
    */
    public static void sort(Sortable a, int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        if (hi0 > lo0) {
            int mid = (lo0 + hi0) / 2;
            while (lo <= hi) {
                while ((lo < hi0) && (a.compare(lo, mid) < 0)) ++lo;
                while ((hi > lo0) && (a.compare(hi, mid) > 0)) --hi;
                if (lo <= hi) {
                    if (lo != hi) {
                        a.swap(lo, hi);
                        if (lo == mid) {
                            mid = hi;
                        } else if (hi == mid) {
                            mid = lo;
                        }
                    }
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi) sort(a, lo0, hi);
            if (lo < hi0) sort(a, lo, hi0);
        }
    }
}
