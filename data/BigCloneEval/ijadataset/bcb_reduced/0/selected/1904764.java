package CADI.Common.Util;

import java.util.Arrays;

/**
 * This class contains several functions which are useful to convert the
 * identification of the components from an one-dimesion array of integers
 * or booleans to a two-dimension array of integers with ranges of components,
 * and vice-versa.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2012/02/11
 */
public class ArraysUtil {

    /**
   * Converts a one-dimensional array of indexes to a two-dimensional array
   * of range of indexes.
   * <p>
   * This method supposes that the input one-dimensional array has not
   * repeated values. Otherwise, output data could not be obtained or it
   * could be wrong.
   *
   * @param indexes an array of indexes.
   *
   * @return an array of ranges.
   */
    public static int[][] indexesToRanges(int[] indexes) {
        if (indexes == null) throw new NullPointerException();
        int numRanges = 1;
        for (int i = 1; i < indexes.length; i++) {
            if (indexes[i - 1] != (indexes[i] - 1)) {
                numRanges++;
            }
        }
        int[][] ranges = new int[numRanges][2];
        int rangeIndex = 0;
        ranges[0][0] = indexes[0];
        for (int i = 1; i < indexes.length; i++) {
            if (indexes[i - 1] != (indexes[i] - 1)) {
                ranges[rangeIndex][1] = indexes[i - 1];
                rangeIndex++;
                ranges[rangeIndex][0] = indexes[i];
            }
        }
        ranges[numRanges - 1][1] = indexes[indexes.length - 1];
        return ranges;
    }

    /**
   * Converts from a bi-dimensional array of ranges of values to an
   * one-dimensional array of indexes.
   * <p>
   * Ranges are supposed that they are not overlpped. Otherwise, output could
   * be wrong.
	 * 
	 * @param ranges a bi-dimensional array with the ranges of the
	 * 						indexes. The first dimension is the index of the
	 * 						range and the second dimension indicates: 0 is the first
   *            index of the range, and 1 is the last index of the range.
	 * 
	 * @return a one-dimension array with the indexes.
	 */
    public static int[] rangesToIndexes(int[][] ranges) {
        assert (ranges != null);
        int compsLength = 0;
        for (int i = 0; i < ranges.length; i++) {
            compsLength += ranges[i][1] - ranges[i][0] + 1;
        }
        int[] indexes = new int[compsLength];
        int index = 0;
        for (int i = 0; i < ranges.length; i++) {
            for (int comp = ranges[i][0]; comp <= ranges[i][1]; comp++) {
                indexes[index++] = comp;
            }
        }
        return indexes;
    }

    /**
   * Converts a bi-dimensional array of ranges of values to an one-dimensional
   * array where the positions belonging to the ranges are set to true, and 
   * others to false.
	 * 
	 * @param ranges a bi-dimensional array with the ranges of the
	 * 						indexes. The first dimension is the index of the
	 * 						range and the second dimension indicates: 0 is the first
   *            index of the range, and 1 is the last index of the range.
	 * @param maxIndex is the maximum value of indexes.
	 * 
	 * @return a one-dimension array of booleans. If the index is <code>true
	 * 			</code>, it means that the indexe was in the range. Otherwise, it was
   *      not.
	 */
    public static boolean[] rangesToExpandedIndexes(int[][] ranges, int maxIndex) {
        int[] indexes = rangesToIndexes(ranges);
        boolean[] expanded = new boolean[maxIndex];
        Arrays.fill(expanded, false);
        for (int i = 0; i < indexes.length; i++) {
            expanded[indexes[i]] = true;
        }
        return expanded;
    }

    /**
	 * Changes the representation from the components as an one-dimensional
	 * array to a bi-dimensional array of ranges. The input parameter <code>
	 * comps</code> is an one-dimensional with length the maximum image
	 * components where the selected components are <code>true</code>.
	 * 
	 * @param comps an one-dimensional array with the indexes of the selected
	 * 			components set to <code>true</code> value.
	 * 
	 * @return a bi-dimensioal array of component ranges.
	 */
    public static int[][] expandedIndexesToRanges(boolean[] expanded) {
        int numRanges = 0;
        if (expanded[0]) numRanges++;
        for (int i = 0; i < expanded.length - 1; i++) {
            if (!expanded[i] && expanded[i + 1]) {
                numRanges++;
            }
        }
        int[][] ranges = new int[numRanges][2];
        int rangeIndex = 0;
        if (expanded[0]) ranges[0][0] = 0;
        for (int i = 0; i < expanded.length - 1; i++) {
            if (!expanded[i] && expanded[i + 1]) {
                ranges[rangeIndex][0] = i + 1;
            }
            if (expanded[i] && !expanded[i + 1]) {
                ranges[rangeIndex][1] = i;
                rangeIndex++;
            }
        }
        if (expanded[expanded.length - 1]) ranges[ranges.length - 1][1] = expanded.length - 1;
        return ranges;
    }

    /**
	 * This method joins overlaped ranges.
	 * 
	 * @param compsRanges a bi-dimensional array with the ranges of the
	 * 						indexes. The first dimension is the index of the
	 * 						range and the second dimension indicates: 0 is the first
   *            index of the range, and 1 is the last index of the range.
	 * 
	 * @return a bi-dimension array with the joined ranges.
	 */
    public static int[][] joinOverlapedRanges(int[][] ranges) {
        int numRanges = 1;
        for (int i = 1; i < ranges.length; i++) {
            if ((ranges[i][0] - ranges[i - 1][1]) > 1) {
                numRanges++;
            }
        }
        if (numRanges == ranges.length) return ranges;
        int[][] joinedRanges = new int[numRanges][2];
        int rangeIndex = 0;
        joinedRanges[0][0] = ranges[0][0];
        for (int i = 1; i < ranges.length; i++) {
            if ((ranges[i][0] - ranges[i - 1][1]) > 1) {
                joinedRanges[rangeIndex++][1] = ranges[i - 1][1];
                joinedRanges[rangeIndex][0] = ranges[i][0];
            }
        }
        joinedRanges[numRanges - 1][1] = ranges[ranges.length - 1][1];
        return joinedRanges;
    }

    /**
   * Prints out an array of bytes.
   * <p>
   * Only for debugging purposes. 
   * 
   * @param buffer
   * @param length
   */
    public static void printByteStream(byte[] buffer, int length) {
        for (int index = 0; index < length; index++) {
            if ((0xFF & buffer[index]) < 16) System.out.print("0");
            System.out.print(Integer.toHexString(0xFF & buffer[index]));
        }
    }

    /**
	 * This method converts the number of components from an one-dimension
	 * array structure to a two-dimension array with ranges of components.
	 * This method supposes that the input one-dimensional array has not
	 * repeated values. Otherwise, output data could not be obtained or it
	 * could be wrong. 
   * <p>
   * OBS: This method will be deprecated in next releases. It will be replaced
   * by {@link #indexesToRanges(int[])}.
	 * 
	 * @param comps an array with the component number.
	 * @return an array of ranges.
	 */
    @Deprecated
    public static int[][] componentsToRangeOfComponents(int[] comps) {
        return indexesToRanges(comps);
    }

    /**
	 * This method converts form a bi-dimensional array with the ranges of
	 * components (first and last component) to an one-dimensional array with
	 * the number of the components.
   * <p>
   * OBS: This method will be deprecated in next releases. It will be replaced
   * by {@link #rangesToIndexes(int[])}.
	 * 
	 * @param rangeOfComps a bi-dimensional array with the ranges of the
	 * 						components. The first dimension is the index of the
	 * 						range and the second dimension indicates: 0 the
	 * 						first component of the range, and 1 the last
	 * 						component of the range.
	 * 
	 * @return a one-dimension array with the components.
	 */
    @Deprecated
    public static int[] rangeOfComponentsToComponents(int[][] rangeOfComps) {
        return rangesToIndexes(rangeOfComps);
    }

    /**
	 * This method converts form a bi-dimensional array with the ranges of
	 * components (first and last component) to an one-dimensional array with
	 * the number of the components.
   * <p>
   * OBS: This method will be deprecated in next releases. It will be replaced
   * by {@link #rangesToExpandedIndexes(int[])}.
	 * 
	 * @param rangeOfComps a bi-dimensional array with the ranges of the
	 * 						components. The first dimension is the index of the
	 * 						range and the second dimension indicates: 0 the
	 * 						first component of the range, and 1 the last
	 * 						component of the range.
	 * @param maxComps is the maximum number of components.
	 * 
	 * @return a one-dimension array of booleans. If the index is <code>true
	 * 			</code>, it means the component is chosen. Otherwise, it is not
	 * 			chosen.
	 */
    @Deprecated
    public static boolean[] rangeOfComponentsToComponentsExpanded(int[][] rangeOfComps, int maxComps) {
        return rangesToExpandedIndexes(rangeOfComps, maxComps);
    }

    /**
	 * Changes the representation from the components as an one-dimensional
	 * array to a bi-dimensional array of ranges. The input parameter <code>
	 * comps</code> is an one-dimensional with length the maximum image
	 * components where the selected components are <code>true</code>.
   * <p>
   * OBS: This method will be deprecated in next releases. It will be replaced
   * by {@link #expandedIndexesToRanges(int[])}.
	 * 
	 * @param comps an one-dimensional array with the indexes of the selected
	 * 			components set to <code>true</code> value.
	 * 
	 * @return a bi-dimensioal array of component ranges.
	 */
    @Deprecated
    public static int[][] componentsExpandedToRangeOfComponents(boolean[] comps) {
        return expandedIndexesToRanges(comps);
    }
}
