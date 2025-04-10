package org.exist.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.exist.dom.NodeProxy;
import org.exist.numbering.NodeId;

/**
	This class implements Floyd's version 
	of the heapsort algorithm.

	http://users.encs.concordia.ca/~chvatal/notes/hsort.html
	http://en.wikipedia.org/wiki/Heapsort#Variations
	
	@author José María Fernández (jmfg@users.sourceforge.net)
*/
public final class HSort {

    public static <C extends Comparable<? super C>> void sort(C[] a, int lo, int hi) {
        if (lo >= hi) return;
        int drop = 1;
        int first = (hi + lo - 1) / 2;
        for (int k = first; k >= lo; k--) {
            if (k == (first - 1) / 2) {
                drop++;
                first = k;
            }
            siftdown(a, hi + 1, k, a[k], drop);
        }
        int last = Integer.highestOneBit(hi - lo);
        drop = 31 - Integer.numberOfLeadingZeros(last);
        int lastlo = last + lo;
        for (int k = hi; k > lo; k--) {
            C temp = a[k];
            a[k] = a[lo];
            siftdown(a, k, lo, temp, drop);
            if (k == lastlo) {
                drop--;
                last /= 2;
                lastlo = last + lo;
            }
        }
    }

    public static <C extends Comparable<? super C>> void sort(C[] a, int lo, int hi, int[] b) {
        if (lo >= hi) return;
        int drop = 1;
        int first = (hi + lo - 1) / 2;
        for (int k = first; k >= lo; k--) {
            if (k == (first - 1) / 2) {
                drop++;
                first = k;
            }
            siftdown(a, b, hi + 1, k, a[k], (b != null) ? b[k] : 0, drop);
        }
        int last = Integer.highestOneBit(hi - lo);
        drop = 31 - Integer.numberOfLeadingZeros(last);
        int lastlo = last + lo;
        for (int k = hi; k > lo; k--) {
            C temp = a[k];
            a[k] = a[lo];
            int tempB;
            if (b != null) {
                tempB = b[k];
                b[k] = b[lo];
            } else {
                tempB = 0;
            }
            siftdown(a, b, k, lo, temp, tempB, drop);
            if (k == lastlo) {
                drop--;
                last /= 2;
                lastlo = last + lo;
            }
        }
    }

    public static <C> void sort(C[] a, Comparator<C> c, int lo, int hi) {
        if (lo >= hi) return;
        int drop = 1;
        int first = (hi + lo - 1) / 2;
        for (int k = first; k >= lo; k--) {
            if (k == (first - 1) / 2) {
                drop++;
                first = k;
            }
            siftdown(a, c, hi + 1, k, a[k], drop);
        }
        int last = Integer.highestOneBit(hi - lo);
        drop = 31 - Integer.numberOfLeadingZeros(last);
        int lastlo = last + lo;
        for (int k = hi; k > lo; k--) {
            C temp = a[k];
            a[k] = a[lo];
            siftdown(a, c, k, lo, temp, drop);
            if (k == lastlo) {
                drop--;
                last /= 2;
                lastlo = last + lo;
            }
        }
    }

    public static <C extends Comparable<? super C>> void sort(List<C> a, int lo, int hi) {
        if (lo >= hi) return;
        int drop = 1;
        int first = (hi + lo - 1) / 2;
        for (int k = first; k >= lo; k--) {
            if (k == (first - 1) / 2) {
                drop++;
                first = k;
            }
            siftdown(a, hi + 1, k, a.get(k), drop);
        }
        int last = Integer.highestOneBit(hi - lo);
        drop = 31 - Integer.numberOfLeadingZeros(last);
        int lastlo = last + lo;
        for (int k = hi; k > lo; k--) {
            C temp = a.get(k);
            a.set(k, a.get(lo));
            siftdown(a, k, lo, temp, drop);
            if (k == lastlo) {
                drop--;
                last /= 2;
                lastlo = last + lo;
            }
        }
    }

    public static void sort(long[] a, int lo, int hi, Object[] b) {
        if (lo >= hi) return;
        int drop = 1;
        int first = (hi + lo - 1) / 2;
        for (int k = first; k >= lo; k--) {
            if (k == (first - 1) / 2) {
                drop++;
                first = k;
            }
            siftdown(a, b, hi + 1, k, a[k], (b != null) ? b[k] : null, drop);
        }
        int last = Integer.highestOneBit(hi - lo);
        drop = 31 - Integer.numberOfLeadingZeros(last);
        int lastlo = last + lo;
        for (int k = hi; k > lo; k--) {
            long temp = a[k];
            a[k] = a[lo];
            Object tempB;
            if (b != null) {
                tempB = b[k];
                b[k] = b[lo];
            } else {
                tempB = null;
            }
            siftdown(a, b, k, lo, temp, tempB, drop);
            if (k == lastlo) {
                drop--;
                last /= 2;
                lastlo = last + lo;
            }
        }
    }

    public static void sortByNodeId(NodeProxy[] a, int lo, int hi) {
        if (lo >= hi) return;
        int drop = 1;
        int first = (hi + lo - 1) / 2;
        for (int k = first; k >= lo; k--) {
            if (k == (first - 1) / 2) {
                drop++;
                first = k;
            }
            siftdownByNodeId(a, hi + 1, k, a[k], drop);
        }
        int last = Integer.highestOneBit(hi - lo);
        drop = 31 - Integer.numberOfLeadingZeros(last);
        int lastlo = last + lo;
        for (int k = hi; k > lo; k--) {
            NodeProxy temp = a[k];
            a[k] = a[lo];
            siftdownByNodeId(a, k, lo, temp, drop);
            if (k == lastlo) {
                drop--;
                last /= 2;
                lastlo = last + lo;
            }
        }
    }

    private static <C extends Comparable<? super C>> void siftdown(C[] a, int n, int vacant, C missing, int drop) {
        int memo = vacant;
        int child, parent;
        int count, next_peek;
        count = 0;
        next_peek = (drop + 1) / 2;
        child = 2 * (vacant + 1);
        while (child < n) {
            if (a[child].compareTo(a[child - 1]) < 0) child--;
            a[vacant] = a[child];
            vacant = child;
            child = 2 * (vacant + 1);
            count++;
            if (count == next_peek) {
                if (a[(vacant - 1) / 2].compareTo(missing) <= 0) break; else next_peek = (count + drop + 1) / 2;
            }
        }
        if (child == n) {
            a[vacant] = a[n - 1];
            vacant = n - 1;
        }
        parent = (vacant - 1) / 2;
        while (vacant > memo) {
            if (a[parent].compareTo(missing) < 0) {
                a[vacant] = a[parent];
                vacant = parent;
                parent = (vacant - 1) / 2;
            } else break;
        }
        a[vacant] = missing;
    }

    private static <C extends Comparable<? super C>> void siftdown(C[] a, int[] b, int n, int vacant, C missing, int missingB, int drop) {
        int memo = vacant;
        int child, parent;
        int count, next_peek;
        count = 0;
        next_peek = (drop + 1) / 2;
        child = 2 * (vacant + 1);
        while (child < n) {
            if (a[child].compareTo(a[child - 1]) < 0) child--;
            a[vacant] = a[child];
            if (b != null) b[vacant] = b[child];
            vacant = child;
            child = 2 * (vacant + 1);
            count++;
            if (count == next_peek) {
                if (a[(vacant - 1) / 2].compareTo(missing) <= 0) break; else next_peek = (count + drop + 1) / 2;
            }
        }
        if (child == n) {
            a[vacant] = a[n - 1];
            if (b != null) b[vacant] = b[n - 1];
            vacant = n - 1;
        }
        parent = (vacant - 1) / 2;
        while (vacant > memo) {
            if (a[parent].compareTo(missing) < 0) {
                a[vacant] = a[parent];
                if (b != null) b[vacant] = b[parent];
                vacant = parent;
                parent = (vacant - 1) / 2;
            } else break;
        }
        a[vacant] = missing;
        if (b != null) b[vacant] = missingB;
    }

    private static <C> void siftdown(C[] a, Comparator<C> c, int n, int vacant, C missing, int drop) {
        int memo = vacant;
        int child, parent;
        int count, next_peek;
        count = 0;
        next_peek = (drop + 1) / 2;
        child = 2 * (vacant + 1);
        while (child < n) {
            if (c.compare(a[child], a[child - 1]) < 0) child--;
            a[vacant] = a[child];
            vacant = child;
            child = 2 * (vacant + 1);
            count++;
            if (count == next_peek) {
                if (c.compare(a[(vacant - 1) / 2], missing) <= 0) break; else next_peek = (count + drop + 1) / 2;
            }
        }
        if (child == n) {
            a[vacant] = a[n - 1];
            vacant = n - 1;
        }
        parent = (vacant - 1) / 2;
        while (vacant > memo) {
            if (c.compare(a[parent], missing) < 0) {
                a[vacant] = a[parent];
                vacant = parent;
                parent = (vacant - 1) / 2;
            } else break;
        }
        a[vacant] = missing;
    }

    private static <C extends Comparable<? super C>> void siftdown(List<C> a, int n, int vacant, C missing, int drop) {
        int memo = vacant;
        int child, parent;
        int count, next_peek;
        count = 0;
        next_peek = (drop + 1) / 2;
        child = 2 * (vacant + 1);
        while (child < n) {
            if (a.get(child).compareTo(a.get(child - 1)) < 0) child--;
            a.set(vacant, a.get(child));
            vacant = child;
            child = 2 * (vacant + 1);
            count++;
            if (count == next_peek) {
                if (a.get((vacant - 1) / 2).compareTo(missing) <= 0) break; else next_peek = (count + drop + 1) / 2;
            }
        }
        if (child == n) {
            a.set(vacant, a.get(n - 1));
            vacant = n - 1;
        }
        parent = (vacant - 1) / 2;
        while (vacant > memo) {
            if (a.get(parent).compareTo(missing) < 0) {
                a.set(vacant, a.get(parent));
                vacant = parent;
                parent = (vacant - 1) / 2;
            } else break;
        }
        a.set(vacant, missing);
    }

    private static void siftdown(long[] a, Object[] b, int n, int vacant, long missing, Object missingB, int drop) {
        int memo = vacant;
        int child, parent;
        int count, next_peek;
        count = 0;
        next_peek = (drop + 1) / 2;
        child = 2 * (vacant + 1);
        while (child < n) {
            if (a[child] < a[child - 1]) child--;
            a[vacant] = a[child];
            if (b != null) b[vacant] = b[child];
            vacant = child;
            child = 2 * (vacant + 1);
            count++;
            if (count == next_peek) {
                if (a[(vacant - 1) / 2] <= missing) break; else next_peek = (count + drop + 1) / 2;
            }
        }
        if (child == n) {
            a[vacant] = a[n - 1];
            if (b != null) b[vacant] = b[n - 1];
            vacant = n - 1;
        }
        parent = (vacant - 1) / 2;
        while (vacant > memo) {
            if (a[parent] < missing) {
                a[vacant] = a[parent];
                if (b != null) b[vacant] = b[parent];
                vacant = parent;
                parent = (vacant - 1) / 2;
            } else break;
        }
        a[vacant] = missing;
        if (b != null) b[vacant] = missingB;
    }

    private static void siftdownByNodeId(NodeProxy[] a, int n, int vacant, NodeProxy missing, int drop) {
        int memo = vacant;
        int count = 0;
        int next_peek = (drop + 1) / 2;
        int child = 2 * (vacant + 1);
        NodeId missingNodeId = missing.getNodeId();
        while (child < n) {
            if (a[child].getNodeId().compareTo(a[child - 1].getNodeId()) < 0) child--;
            a[vacant] = a[child];
            vacant = child;
            child = 2 * (vacant + 1);
            count++;
            if (count == next_peek) {
                if (a[(vacant - 1) / 2].getNodeId().compareTo(missingNodeId) <= 0) break; else next_peek = (count + drop + 1) / 2;
            }
        }
        if (child == n) {
            a[vacant] = a[n - 1];
            vacant = n - 1;
        }
        int parent = (vacant - 1) / 2;
        while (vacant > memo) {
            if (a[parent].getNodeId().compareTo(missingNodeId) < 0) {
                a[vacant] = a[parent];
                vacant = parent;
                parent = (vacant - 1) / 2;
            } else break;
        }
        a[vacant] = missing;
    }

    public static void main(String[] args) throws Exception {
        List<String> l = new ArrayList<String>();
        if (args.length == 0) {
            String[] a = new String[] { "Rudi", "Herbert", "Anton", "Berta", "Olga", "Willi", "Heinz" };
            for (int i = 0; i < a.length; i++) l.add(a[i]);
        } else {
            System.err.println("Ordering file " + args[0] + "\n");
            try {
                java.io.BufferedReader is = new java.io.BufferedReader(new java.io.FileReader(args[0]));
                String rr;
                while ((rr = is.readLine()) != null) {
                    l.add(rr);
                }
                is.close();
            } catch (Exception e) {
            }
        }
        long a;
        long b;
        a = System.currentTimeMillis();
        sort(l, 0, l.size() - 1);
        b = System.currentTimeMillis();
        System.err.println("Ellapsed time: " + (b - a) + " size: " + l.size());
        for (int i = 0; i < l.size(); i++) System.out.println(l.get(i));
    }
}
