package nz.ac.waikato.jdsl.core.algo.sorts;

import nz.ac.waikato.jdsl.core.api.*;

/** 
 * Performs a merge-sort in O(n log n) time, provided that the
 * atRank(int) method of the Sequence works in O(1) time. <P>
 * 
 * @author Benoit Hudson
 * @author Keith Schmidt
 * @version JDSL 2.1.1 
 */
public class ArrayMergeSort implements SortObject {

    public void sort(Sequence S, Comparator c) {
        mergeSortHelper(S, 0, (S.size() - 1), c);
    }

    /** 
    * Recursively divides a Sequence into (roughly) equal subsequences
    * and merges them back together once sorted.
    *
    * @param S     the sequence of which to merge subsequences
    * @param start the first index of the subsequence
    * @param end   the last index of the subsequence
    * @param c     the comparator to use 
    */
    private void mergeSortHelper(Sequence S, int start, int end, Comparator c) {
        if (start < end) {
            int middle = (start + end) / 2;
            mergeSortHelper(S, start, middle, c);
            mergeSortHelper(S, middle + 1, end, c);
            merge(S, start, middle, end, c);
        }
    }

    /** 
    * Merges the two adjacent (and hopefully sorted) subsequences.<P>
    * Uses O(n) space.
    * @param S    the sequence of which to merge subsequences
    * @param p    the first index of the first subsequence
    * @param q    the last index of the first subsequence
    * @param r    the last index of the second subsequence
    * @param c    the comparator to use
    */
    private void merge(Sequence S, int p, int q, int r, Comparator c) {
        Sequence S1 = (Sequence) S.newContainer();
        Sequence S2 = (Sequence) S.newContainer();
        for (int i = p; i <= q; i++) {
            S1.insertLast((S.atRank(i)).element());
        }
        for (int i = q + 1; i <= r; i++) {
            S2.insertLast((S.atRank(i)).element());
        }
        int S1index = 0;
        int S2index = 0;
        int Sindex = p;
        for (; ; ) {
            if (c.isLessThan(S1.atRank(S1index).element(), S2.atRank(S2index).element())) {
                S.replaceElement(S.atRank(Sindex), (S1.atRank(S1index)).element());
                S1index++;
                Sindex++;
                if (S1index >= S1.size()) break;
            } else {
                S.replaceElement(S.atRank(Sindex), (S2.atRank(S2index)).element());
                S2index++;
                Sindex++;
                if (S2index >= S2.size()) break;
            }
        }
        while (S1index < S1.size()) {
            S.replaceElement(S.atRank(Sindex), (S1.atRank(S1index)).element());
            S1index++;
            Sindex++;
        }
        while (S2index < S2.size()) {
            S.replaceElement(S.atRank(Sindex), (S2.atRank(S2index)).element());
            S2index++;
            Sindex++;
        }
    }
}
