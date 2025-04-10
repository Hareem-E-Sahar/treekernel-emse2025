package com.mlib.algorithm.search;

/**
 * 二分搜索法
 * 
 * @author zhaotao
 * 
 */
public class Dichotomizing<T extends Comparable<T>> extends AbstractSearcher<T> {

    @Override
    public int search(T[] values, T value) {
        int start = 0;
        int end = values.length;
        super.searchTimes = 0;
        while (start != end) {
            super.searchTimes++;
            int middle = (start + end) / 2;
            if (value.compareTo(values[middle]) == 0) return middle; else if (value.compareTo(values[middle]) > 0) start = middle; else end = middle;
        }
        return -1;
    }
}
