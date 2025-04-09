package name.huzhenbo.java.algorithm.search;

class BinarySearch {

    public int go(int[] array, int target) {
        int start = 0;
        int end = array.length - 1;
        while (end >= start) {
            int currentPosition = (end + start) / 2;
            if (array[currentPosition] == target) return currentPosition; else if (array[currentPosition] > target) {
                end = currentPosition - 1;
            } else {
                start = currentPosition + 1;
            }
        }
        return -1;
    }

    public int goRecursive(int[] array, int target) {
        return _goRecursive(array, target, 0, array.length - 1);
    }

    private int _goRecursive(int[] array, int target, int start, int end) {
        if (start > end) return -1;
        int currentPosition = (end + start) / 2;
        if (array[currentPosition] == target) return currentPosition; else if (array[currentPosition] > target) {
            end = currentPosition - 1;
        } else {
            start = currentPosition + 1;
        }
        return _goRecursive(array, target, start, end);
    }

    public int goExtended(int[] array, int target) {
        int start = 0;
        int end = array.length - 1;
        while (end > start) {
            int m = (start + end) / 2;
            if (array[m] == target) {
                end = m;
            } else if (array[m] > target) {
                end = m - 1;
            } else {
                start = m + 1;
            }
        }
        int p = end;
        if (p >= array.length || array[p] != target) {
            p = -1;
        }
        return p;
    }
}
