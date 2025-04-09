package org.proteinshader.structure.sort;

import org.proteinshader.structure.*;

/*******************************************************************************
Knows how to sort an array of Drawables by their distance from the
camera.
*******************************************************************************/
public class DrawableSorter {

    private Drawable[] m_A, m_B;

    /***************************************************************************
    Constructs a DrawableSorter.
    ***************************************************************************/
    public DrawableSorter() {
        m_A = null;
        m_B = null;
    }

    /***************************************************************************
    Sorts an array of Drawable objects in ascending order based on
    camera distance.

    @param d  the array of Drawables to sort.
    ***************************************************************************/
    public void ascendingMergeSort(Drawable[] d) {
        if (d != null && d.length > 1) {
            m_A = d;
            m_B = new Drawable[d.length];
            mergeSort(0, d.length - 1);
        }
    }

    /***************************************************************************
    This helper method of ascendingMergeSort() will sort the array of
    Drawable objects in ascending order.

    @param i  the lowest index number of the subarray.
    @param j  the highest index number of the subarray.
    ***************************************************************************/
    private void mergeSort(int i, int j) {
        if (i < j) {
            int m = (i + j) / 2;
            mergeSort(i, m);
            mergeSort(m + 1, j);
            merge(i, m, j);
        }
    }

    /***************************************************************************
    This helper method of mergeSort() will merge two sorted lists into
    a single sorted list.

    @param i  the lowest index number of the first list.
    @param m  the highest index number of the first list
              (and m + 1 begins the second list).
    @param j  the highest index number of the second list.
    ***************************************************************************/
    private void merge(int i, int m, int j) {
        int x = i, y = m + 1;
        for (int b = i; b <= j; ++b) {
            if (y > j) {
                m_B[b] = m_A[x];
                ++x;
            } else if (x > m) {
                m_B[b] = m_A[y];
                ++y;
            } else if (m_A[x].getCameraDistance() < m_A[y].getCameraDistance()) {
                m_B[b] = m_A[x];
                ++x;
            } else {
                m_B[b] = m_A[y];
                ++y;
            }
        }
        for (int k = i; k <= j; ++k) {
            m_A[k] = m_B[k];
        }
    }
}
