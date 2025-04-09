package jp.go.ipa.jgcl;

/**
 * Static helper methods for geometry
 *
 * @author Information-technology Promotion Agency, Japan
 */
public class GeometryUtil {

    private GeometryUtil() {
    }

    /**
	 *
	 * @param array
	 */
    public static void sortDoubleArray(double[] array) {
        sortDoubleArray(array, 0, array.length);
    }

    /**
	 *
	 * @param array
	 * @param low
	 * @param up
	 */
    public static void sortDoubleArray(double[] array, int low, int up) {
        int lidx = low;
        int uidx = up;
        double key = array[(low + up) / 2];
        double swap;
        for (; lidx < uidx; ) {
            for (; array[lidx] < key; lidx++) ;
            for (; key < array[uidx]; uidx--) ;
            if (lidx <= uidx) {
                swap = array[uidx];
                array[uidx] = array[lidx];
                array[lidx] = swap;
                lidx++;
                uidx--;
            }
        }
        if (low < uidx) {
            sortDoubleArray(array, low, uidx);
        }
        if (lidx < up) {
            sortDoubleArray(array, lidx, up);
        }
    }

    /**
	 * Binary search a double array
	 *
	 * @param array
	 * @param min
	 * @param max
	 * @param value
	 * @return
	 */
    public static int bsearchDoubleArray(double[] array, int min, int max, double value) {
        if (value < array[min]) {
            return min - 1;
        } else {
            if (array[max] <= value) {
                return max;
            } else {
                int mid;
                while (min + 1 < max) {
                    mid = (min + max) / 2;
                    if (value < array[mid]) {
                        max = mid;
                    } else {
                        min = mid;
                    }
                }
                return min;
            }
        }
    }

    /**
	 * Returns true if value is greater than zero
	 *
	 * @param value
	 * @return
	 */
    public static boolean isReciprocatable(double value) {
        if (Math.abs(value) < MachineEpsilon.DOUBLE) {
            return false;
        }
        return true;
    }

    /**
	 * Returns true if a can be divided with b.
	 *
	 * @param a
	 * @param b
	 * @return
	 */
    public static boolean isDividable(double a, double b) {
        double c;
        c = b / a;
        if (Double.isNaN(c) || !isReciprocatable(c)) {
            return false;
        }
        return true;
    }
}
