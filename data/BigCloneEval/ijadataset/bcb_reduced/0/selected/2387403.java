package de.vogella.algorithms.sort.mergesort;

public class Mergesort {

    private int[] numbers;

    private int number;

    public void sort(int[] values) {
        this.numbers = values;
        number = values.length;
        mergesort(0, number - 1);
    }

    private void mergesort(int low, int high) {
        if (low < high) {
            int middle = (low + high) / 2;
            mergesort(low, middle);
            mergesort(middle + 1, high);
            merge(low, middle, high);
        }
    }

    private void merge(int low, int middle, int high) {
        int[] helper = new int[number];
        for (int i = low; i <= high; i++) {
            helper[i] = numbers[i];
        }
        int i = low;
        int j = middle + 1;
        int k = low;
        while (i <= middle && j <= high) {
            if (helper[i] <= helper[j]) {
                numbers[k] = helper[i];
            } else {
                numbers[k] = helper[j];
            }
            k++;
            i++;
        }
        while (i <= middle) {
            numbers[k] = helper[i];
            k++;
            i++;
        }
        helper = null;
    }
}
