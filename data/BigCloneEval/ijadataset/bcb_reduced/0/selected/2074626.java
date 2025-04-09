package binarySearch;

public class Main {

    private static final byte NOT_FOUND = -1;

    private static short length = 100;

    private static short test[] = new short[length];

    public static void main(String[] args) {
        System.out.println("started");
        for (int loop = 0; loop < length; loop++) {
            System.out.println(loop);
            test[loop] = (short) (loop - 30);
        }
        System.out.println(binarySearch(test, length - 50));
        System.out.println(System.currentTimeMillis());
    }

    public static int binarySearch(short[] list, int toFind) {
        int low = 0;
        int high = list.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (list[mid] < toFind) {
                low = mid + 1;
            } else if (list[mid] > toFind) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return NOT_FOUND;
    }
}
