package com.ava.sort;

import java.lang.reflect.Array;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * ������������ ���������� ��������
 * 
 * 
 * @author Vasiliy Akimushkin
 * 
 * @param <T>
 */
public class ParallelMergeSorter<T extends Comparable<? super T>> {

    /**
	 * ��� �������
	 */
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
	 * ������ ��� ����������
	 */
    private T[] array;

    /**
	 * ������, ��� ������� �� ������� ���������� ��������� ���������� �� ������
	 */
    private int indivisibleSize;

    /**
	 * �����������
	 * 
	 * @param array
	 *            - ������ ��� ����������
	 * @param indivisibleSize 
	 * 			 - ��������� ������
	 * 
	 */
    public ParallelMergeSorter(T[] array, int indivisibleSize) {
        this.array = array;
        this.indivisibleSize = indivisibleSize;
    }

    /**
	 * ���������� ��������������� ������
	 * 
	 * @return
	 */
    public T[] sort() {
        try {
            executor.submit(new SortTask(0, array.length - 1)).get();
            executor.shutdown();
            return array;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class SortTask implements Runnable {

        private int leftIndex;

        private int rightIndex;

        public SortTask(int leftIndex, int rightIndex) {
            this.leftIndex = leftIndex;
            this.rightIndex = rightIndex;
        }

        @Override
        public void run() {
            if (leftIndex == rightIndex) return;
            if (rightIndex - leftIndex > indivisibleSize) {
                try {
                    int centerIndex = (leftIndex + rightIndex) / 2;
                    Future<?> task1 = executor.submit(new SortTask(leftIndex, centerIndex));
                    Future<?> task2 = executor.submit(new SortTask(centerIndex + 1, rightIndex));
                    task1.get();
                    task2.get();
                    merge();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                insertionsort();
            }
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
