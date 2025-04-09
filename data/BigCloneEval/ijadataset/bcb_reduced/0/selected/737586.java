package org.exist.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.exist.dom.NodeProxy;

/**
  * This class implements a version 
  * of the Introspective Sort Algorithm.
  * 
  * Reference: David R. Musser
  * "Introspective Sorting and Selection Algorithms"
  * Software--Practice and Experience, (8): 983-993 (1997)
  * 
  * The implementation is mainly inspired
  * on the article describing the algorithm,
  * but also in the work of Michael
  * Maniscalco in C++. It is also slightly
  * based on the previous implementation of
  * FastQSort in eXist.
  * 
  * http://www.cs.rpi.edu/~musser/
  * http://www.cs.rpi.edu/~musser/gp/introsort.ps
  * http://www.michael-maniscalco.com/sorting.htm
  * 
  * See also an alternate implementation at:
  * 
  * http://ralphunden.net/?q=a-guide-to-introsort#AB2
  * 
  * @author José María Fernández (jmfg@users.sourceforge.net)
  */
public final class FastQSort {

    private static final int M = 10;

    private static final double LOG2 = Math.log(2.0);

    private static final <C extends Comparable<? super C>> void IntroSort(C a[], int lo, int hi) {
        IntroSortLoop(a, lo, hi, 2 * (int) Math.floor(Math.log(hi - lo + 1) / LOG2));
        InsertionSort.sort(a, lo, hi);
    }

    private static final <C extends Comparable<? super C>> void IntroSort(C a[], int lo, int hi, int b[]) {
        IntroSortLoop(a, lo, hi, b, 2 * (int) Math.floor(Math.log(hi - lo + 1) / LOG2));
        InsertionSort.sort(a, lo, hi, b);
    }

    private static final <C> void IntroSort(C a[], Comparator<C> comp, int lo, int hi) {
        IntroSortLoop(a, comp, lo, hi, 2 * (int) Math.floor(Math.log(hi - lo + 1) / LOG2));
        InsertionSort.sort(a, comp, lo, hi);
    }

    private static final <C extends Comparable<? super C>> void IntroSort(List<C> a, int lo, int hi) {
        IntroSortLoop(a, lo, hi, 2 * (int) Math.floor(Math.log(hi - lo + 1) / LOG2));
        InsertionSort.sort(a, lo, hi);
    }

    private static final void IntroSort(long a[], int lo, int hi, Object b[]) {
        IntroSortLoop(a, lo, hi, b, 2 * (int) Math.floor(Math.log(hi - lo + 1) / LOG2));
        InsertionSort.sort(a, lo, hi, b);
    }

    private static final void IntroSortByNodeId(NodeProxy a[], int lo, int hi) {
        IntroSortLoopByNodeId(a, lo, hi, 2 * (int) Math.floor(Math.log(hi - lo + 1) / LOG2));
        InsertionSort.sortByNodeId(a, lo, hi);
    }

    private static final <C extends Comparable<? super C>> void IntroSortLoop(C a[], int l, int r, int maxdepth) {
        while ((r - l) > M) {
            if (maxdepth <= 0) {
                HeapSort.sort(a, l, r);
                return;
            }
            maxdepth--;
            int i = (l + r) / 2;
            int j;
            C partionElement;
            if (a[l].compareTo(a[i]) > 0) SwapVals.swap(a, l, i);
            if (a[l].compareTo(a[r]) > 0) SwapVals.swap(a, l, r);
            if (a[i].compareTo(a[r]) > 0) SwapVals.swap(a, i, r);
            partionElement = a[i];
            i = l + 1;
            j = r - 1;
            while (i <= j) {
                while ((i < r) && (partionElement.compareTo(a[i]) > 0)) ++i;
                while ((j > l) && (partionElement.compareTo(a[j]) < 0)) --j;
                if (i <= j) {
                    SwapVals.swap(a, i, j);
                    ++i;
                    --j;
                }
            }
            if (l < j) IntroSortLoop(a, l, j, maxdepth);
            if (i >= r) break;
            l = i;
        }
    }

    private static final <C extends Comparable<? super C>> void IntroSortLoop(C a[], int l, int r, int b[], int maxdepth) {
        while ((r - l) > M) {
            if (maxdepth <= 0) {
                HeapSort.sort(a, l, r, b);
                return;
            }
            maxdepth--;
            int i = (l + r) / 2;
            int j;
            C partionElement;
            if (a[l].compareTo(a[i]) > 0) {
                SwapVals.swap(a, l, i);
                if (b != null) SwapVals.swap(b, l, i);
            }
            if (a[l].compareTo(a[r]) > 0) {
                SwapVals.swap(a, l, r);
                if (b != null) SwapVals.swap(b, l, r);
            }
            if (a[i].compareTo(a[r]) > 0) {
                SwapVals.swap(a, i, r);
                if (b != null) SwapVals.swap(b, i, r);
            }
            partionElement = a[i];
            i = l + 1;
            j = r - 1;
            while (i <= j) {
                while ((i < r) && (partionElement.compareTo(a[i]) > 0)) ++i;
                while ((j > l) && (partionElement.compareTo(a[j]) < 0)) --j;
                if (i <= j) {
                    SwapVals.swap(a, i, j);
                    if (b != null) SwapVals.swap(b, i, j);
                    ++i;
                    --j;
                }
            }
            if (l < j) IntroSortLoop(a, l, j, b, maxdepth);
            if (i >= r) break;
            l = i;
        }
    }

    private static final <C> void IntroSortLoop(C a[], Comparator<C> comp, int l, int r, int maxdepth) {
        while ((r - l) > M) {
            if (maxdepth <= 0) {
                HeapSort.sort(a, comp, l, r);
                return;
            }
            maxdepth--;
            int i = (l + r) / 2;
            int j;
            C partionElement;
            if (comp.compare(a[l], a[i]) > 0) SwapVals.swap(a, l, i);
            if (comp.compare(a[l], a[r]) > 0) SwapVals.swap(a, l, r);
            if (comp.compare(a[i], a[r]) > 0) SwapVals.swap(a, i, r);
            partionElement = a[i];
            i = l + 1;
            j = r - 1;
            while (i <= j) {
                while ((i < r) && (comp.compare(partionElement, a[i]) > 0)) ++i;
                while ((j > l) && (comp.compare(partionElement, a[j]) < 0)) --j;
                if (i <= j) {
                    SwapVals.swap(a, i, j);
                    ++i;
                    --j;
                }
            }
            if (l < j) IntroSortLoop(a, comp, l, j, maxdepth);
            if (i >= r) break;
            l = i;
        }
    }

    private static final <C extends Comparable<? super C>> void IntroSortLoop(List<C> a, int l, int r, int maxdepth) {
        while ((r - l) > M) {
            if (maxdepth <= 0) {
                HeapSort.sort(a, l, r);
                return;
            }
            maxdepth--;
            int i = (l + r) / 2;
            int j;
            C partionElement;
            if ((a.get(l)).compareTo(a.get(i)) > 0) SwapVals.swap(a, l, i);
            if ((a.get(l)).compareTo(a.get(r)) > 0) SwapVals.swap(a, l, r);
            if ((a.get(i)).compareTo(a.get(r)) > 0) SwapVals.swap(a, i, r);
            partionElement = a.get(i);
            i = l + 1;
            j = r - 1;
            while (i <= j) {
                while ((i < r) && (partionElement.compareTo(a.get(i)) > 0)) ++i;
                while ((j > l) && (partionElement.compareTo(a.get(j)) < 0)) --j;
                if (i <= j) {
                    SwapVals.swap(a, i, j);
                    ++i;
                    --j;
                }
            }
            if (l < j) IntroSortLoop(a, l, j, maxdepth);
            if (i >= r) break;
            l = i;
        }
    }

    private static final void IntroSortLoop(long a[], int l, int r, Object b[], int maxdepth) {
        while ((r - l) > M) {
            if (maxdepth <= 0) {
                HeapSort.sort(a, l, r, b);
                return;
            }
            maxdepth--;
            int i = (l + r) / 2;
            int j;
            long partionElement;
            if (a[l] > a[i]) {
                SwapVals.swap(a, l, i);
                if (b != null) SwapVals.swap(b, l, i);
            }
            if (a[l] > a[r]) {
                SwapVals.swap(a, l, r);
                if (b != null) SwapVals.swap(b, l, r);
            }
            if (a[i] > a[r]) {
                SwapVals.swap(a, i, r);
                if (b != null) SwapVals.swap(b, i, r);
            }
            partionElement = a[i];
            i = l + 1;
            j = r - 1;
            while (i <= j) {
                while ((i < r) && (partionElement > a[i])) ++i;
                while ((j > l) && (partionElement < a[j])) --j;
                if (i <= j) {
                    SwapVals.swap(a, i, j);
                    if (b != null) SwapVals.swap(b, i, j);
                    ++i;
                    --j;
                }
            }
            if (l < j) IntroSortLoop(a, l, j, b, maxdepth);
            if (i >= r) break;
            l = i;
        }
    }

    private static final void IntroSortLoopByNodeId(NodeProxy a[], int l, int r, int maxdepth) {
        while ((r - l) > M) {
            if (maxdepth <= 0) {
                HeapSort.sortByNodeId(a, l, r);
                return;
            }
            maxdepth--;
            int i = (l + r) / 2;
            int j;
            NodeProxy partionElement;
            if (a[l].getNodeId().compareTo(a[i].getNodeId()) > 0) SwapVals.swap(a, l, i);
            if (a[l].getNodeId().compareTo(a[r].getNodeId()) > 0) SwapVals.swap(a, l, r);
            if (a[i].getNodeId().compareTo(a[r].getNodeId()) > 0) SwapVals.swap(a, i, r);
            partionElement = a[i];
            i = l + 1;
            j = r - 1;
            while (i <= j) {
                while ((i < r) && (partionElement.getNodeId().compareTo(a[i].getNodeId()) > 0)) ++i;
                while ((j > l) && (partionElement.getNodeId().compareTo(a[j].getNodeId()) < 0)) --j;
                if (i <= j) {
                    SwapVals.swap(a, i, j);
                    ++i;
                    --j;
                }
            }
            if (l < j) IntroSortLoopByNodeId(a, l, j, maxdepth);
            if (i >= r) break;
            l = i;
        }
    }

    public static <C extends Comparable<? super C>> void sort(C[] a, int lo, int hi) {
        if (lo >= hi) return;
        IntroSort(a, lo, hi);
    }

    public static <C extends Comparable<? super C>> void sort(C[] a, int lo, int hi, int[] b) {
        if (lo >= hi) return;
        IntroSort(a, lo, hi, b);
    }

    public static <C> void sort(C[] a, Comparator<C> c, int lo, int hi) {
        if (lo >= hi) return;
        IntroSort(a, c, lo, hi);
    }

    public static <C extends Comparable<? super C>> void sort(List<C> a, int lo, int hi) {
        if (lo >= hi) return;
        IntroSort(a, lo, hi);
    }

    public static void sortByNodeId(NodeProxy[] a, int lo, int hi) {
        if (lo >= hi) return;
        IntroSortByNodeId(a, lo, hi);
    }

    public static void sort(long[] a, int lo, int hi, Object b[]) {
        if (lo >= hi) return;
        IntroSort(a, lo, hi, b);
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
