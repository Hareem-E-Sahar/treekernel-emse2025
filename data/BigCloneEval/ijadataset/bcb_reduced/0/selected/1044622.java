package com.ava.sort;

import java.lang.reflect.Array;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * ������������ ���������� ��������, ���������� <code>ForkJoinPool</code>
 * 
 * @author Vasiliy Akimushkin
 * 
 * @param <T>
 *
 */
public class ParallelMergeForkJoinSorter<T extends Comparable<? super T>> {

    /**
	 * ��� �������
	 */
    private static final ForkJoinPool threadPool = new ForkJoinPool();

    /**
	 * ������ ��� ����������
	 */
    T[] array;

    /**
	 * ������, ��� ������� �� ������� ���������� ��������� ���������� �� ������
	 */
    private int indivisibleSize;

    public ParallelMergeForkJoinSorter(T[] array, int indivisibleSize) {
        this.array = array;
        this.indivisibleSize = indivisibleSize;
    }

    public T[] sort() {
        threadPool.invoke(new SortTask(0, array.length - 1));
        return array;
    }

    @SuppressWarnings("serial")
    class SortTask extends RecursiveAction {

        private int leftIndex;

        private int rightIndex;

        public SortTask(int leftIndex, int rightIndex) {
            this.leftIndex = leftIndex;
            this.rightIndex = rightIndex;
        }

        /**
		 * ��������� ���������� ���������
		 */
        private void insertionsort() {
            for (int i = leftIndex + 1; i <= rightIndex; i++) {
                int j = i;
                T currentMinimum = array[j];
                while (j > leftIndex && currentMinimum.compareTo(array[j - 1]) < 0) {
                    array[j] = array[j - 1];
                    j--;
                }
                array[j] = currentMinimum;
            }
        }

        @Override
        protected void compute() {
            if (rightIndex - leftIndex < indivisibleSize) {
                insertionsort();
                return;
            }
            int centerIndex = (leftIndex + rightIndex) / 2;
            invokeAll(new SortTask(leftIndex, centerIndex), new SortTask(centerIndex + 1, rightIndex));
            merge();
        }

        /**
		 * ��������� ������� ���� ������ ����������
		 */
        @SuppressWarnings("unchecked")
        private void merge() {
            int centerIndex = (leftIndex + rightIndex) / 2 + 1;
            int amountOfElements = rightIndex - leftIndex + 1;
            T[] tempSortedArray = (T[]) Array.newInstance(array.getClass().getComponentType(), amountOfElements);
            int tempSortedIndex = 0;
            int tempLeftIndex = leftIndex;
            int tempRightIndex = centerIndex;
            while (tempLeftIndex < centerIndex && tempRightIndex <= rightIndex) if (array[tempLeftIndex].compareTo(array[tempRightIndex]) <= 0) tempSortedArray[tempSortedIndex++] = array[tempLeftIndex++]; else tempSortedArray[tempSortedIndex++] = array[tempRightIndex++];
            while (tempLeftIndex < centerIndex) tempSortedArray[tempSortedIndex++] = array[tempLeftIndex++];
            while (tempRightIndex <= rightIndex) tempSortedArray[tempSortedIndex++] = array[tempRightIndex++];
            for (int i = 0; i < amountOfElements; i++) array[leftIndex + i] = tempSortedArray[i];
        }
    }
}
