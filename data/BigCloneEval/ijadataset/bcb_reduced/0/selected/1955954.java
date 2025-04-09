package clustering.framework;

import java.util.Random;
import java.util.Arrays;

public class Randomize {

    public static Object[] randomizeThis(Object[] arg) {
        if ((arg == null) || (arg.length == 0)) throw (new IllegalArgumentException());
        int dim = arg.length;
        int[] indexes = new int[dim * 1000];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = -1;
        }
        Random r = new Random();
        int index = r.nextInt(dim * 1000);
        int count = 0;
        while (count < dim) {
            if (indexes[index] == -1) {
                indexes[index] = count++;
            }
            index = r.nextInt(dim * 1000);
        }
        Arrays.sort(indexes);
        Object[] result = new Object[arg.length];
        count = 0;
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] != -1) {
                result[count++] = arg[indexes[i]];
            }
        }
        return result;
    }

    private static void merge(int[][] array, int start, int middle, int end) {
        int dim = end - start + 1;
        int[][] tmp = new int[2][dim];
        int i = start;
        int k = 0;
        int j = middle + 1;
        while ((i <= middle) && (j <= end)) {
            if (array[0][i] < array[0][j]) {
                tmp[0][k] = array[0][i];
                tmp[1][k] = array[1][i];
                i++;
            } else {
                tmp[0][k] = array[0][j];
                tmp[1][k] = array[1][j];
                j++;
            }
            k++;
        }
        int l = k - 1;
        if (i <= middle) {
            j = end - start - k;
            for (int h = 0; h <= j; h++) {
                tmp[0][k + h] = array[0][i + h];
                tmp[1][k + h] = array[1][i + h];
                l++;
            }
        }
        k = l;
        for (i = 0; i <= k; i++) {
            array[0][start + i] = tmp[0][i];
            array[1][start + i] = tmp[1][i];
        }
    }

    private static void mergeSort(int[][] array, int start, int end) {
        if (start < end) {
            int middle = (end + start) / 2;
            mergeSort(array, start, middle);
            mergeSort(array, middle + 1, end);
            merge(array, start, middle, end);
        }
    }

    public static void sort(int[][] array) {
        mergeSort(array, 0, array[0].length - 1);
    }
}
