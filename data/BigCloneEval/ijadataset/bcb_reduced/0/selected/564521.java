package name.huzhenbo.java.algorithm.talent;

public class NumberOfInversions {

    private int[] array;

    private int count;

    public NumberOfInversions(int[] array) {
        this.array = array;
        count = 0;
    }

    public int numberOfInversions() {
        mergingCount(0, array.length - 1);
        return count;
    }

    private void mergingCount(int start, int end) {
        if (end <= start) return;
        if (end == start + 1) {
            if (array[end] < array[start]) {
                count++;
                swap(start, end);
            }
            return;
        }
        int m = (start + end) / 2;
        mergingCount(start, m);
        mergingCount(m + 1, end);
        merge(start, end);
    }

    private void merge(int start, int end) {
        int[] temps = new int[end - start + 1];
        int m = (start + end) / 2;
        int i = start;
        int j = m + 1;
        for (int k = 0; k < temps.length; k++) {
            if (array[i] < array[j]) {
                temps[k] = array[i++];
                count += j - m - 1;
            } else {
                temps[k] = array[j++];
            }
            if (i > m || j > end) {
                break;
            }
        }
        for (int k = i + j - m - 1, remainingStart = i > m ? j : i; k < temps.length; k++, remainingStart++) {
            temps[k] = array[remainingStart];
            if (remainingStart <= m) count += j - m - 1;
        }
        for (int k = 0; k < temps.length; k++) {
            array[start++] = temps[k];
        }
    }

    private void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
