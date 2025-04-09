package hadit;

import java.util.Arrays;

public class CopyNumberBitSetSpillover {

    private static final int ArraySpaceIncrement = 5;

    private int[] mSpillovers;

    private int mSpilloversCount;

    public CopyNumberBitSetSpillover() {
        mSpillovers = null;
        mSpilloversCount = 0;
    }

    /** Returns the number of spillover indices. */
    public int getSpilloverCount() {
        return mSpilloversCount;
    }

    /** Given a sample index, this searches for the spillover counts for that
	 *  sample index.  If the sample index is not found, null is returned.  
	 *  Otherwise, a byte array of the counts are returned. 
	 */
    public byte[] getSpilloverCountsForSampleIndex(short sampleIndex, byte[] sampleCountsReturned) {
        if (mSpillovers == null) {
            System.out.println("SPILLOVER NULL!");
            return null;
        } else {
            return findCountsForSampleIndex(sampleIndex, mSpillovers, mSpilloversCount, sampleCountsReturned);
        }
    }

    /** Given a sample index, and two allele counts, this registers the sample index
	 *  and allele counts in the spillover array. */
    public void registerSpilloverSampleIndexAndCounts(short sampleIndex, byte count1, byte count2) {
        ensureSpilloversCapacity();
        int intCompactForm = getSpilloverCompactForm(sampleIndex, count1, count2);
        int resultIndex = binarySearchSampleIndex(sampleIndex, mSpillovers, mSpilloversCount);
        if (resultIndex >= 0) {
            mSpillovers[resultIndex] = intCompactForm;
        } else {
            mSpillovers[mSpilloversCount] = intCompactForm;
            mSpilloversCount++;
            if (mSpilloversCount > 1) {
                if (mSpillovers[mSpilloversCount - 2] >= mSpillovers[mSpilloversCount - 1]) {
                    Arrays.sort(mSpillovers, 0, mSpilloversCount);
                }
            }
        }
    }

    /** Allocates memory for spillovers. */
    private void ensureSpilloversCapacity() {
        if (mSpillovers == null) {
            mSpillovers = new int[ArraySpaceIncrement];
            Arrays.fill(mSpillovers, 0);
            mSpilloversCount = 0;
        }
        if (mSpilloversCount == mSpillovers.length) {
            int[] newArray = new int[mSpilloversCount + ArraySpaceIncrement];
            Arrays.fill(newArray, 0);
            System.arraycopy(mSpillovers, 0, newArray, 0, mSpilloversCount);
            mSpillovers = newArray;
        }
    }

    /** Given a sample index < 65536 and a pair of counts, this returns an
	 *  integer that contains the index and counts packed. */
    private static int getSpilloverCompactForm(short sampleIndex, byte count1, byte count2) {
        return ((sampleIndex << 16) | (count1 << 8) | count2);
    }

    /** Given a sample index and an integer, this returns whether the sample
	 *  index is contained within the integer. */
    private static boolean sampleIndexInInteger(short sampleIndex, int theInteger) {
        return (sampleIndex == extractSampleIndexFromInteger(theInteger));
    }

    /** Given an integer, this returns the two least significant bytes, which 
	 *  represent counts.  These are returned in array format. */
    private static byte[] extractCountsFromInteger(int theInteger, byte[] rV) {
        rV[0] = (byte) ((0x0000FF00 & theInteger) >> 8);
        rV[1] = (byte) (0x000000FF & theInteger);
        return rV;
    }

    /** Given an integer, this returns the sample index from that integer. */
    private static short extractSampleIndexFromInteger(int theInteger) {
        return ((short) ((theInteger >> 16) & 0x0000FFFF));
    }

    /** Given a sample index and integer array, this performs a binary search
	 *  for the sample index on the integer array.  If the sample index is 
	 *  found, an index >= 0 is returned, else -1 is returned. */
    private static int binarySearchSampleIndex(short sampleIndex, int[] intArray, int trueLength) {
        int lowerIndex = 0;
        int upperIndex = trueLength - 1;
        int midIndex = 0;
        short valueAtMidIndex;
        while (lowerIndex <= upperIndex) {
            midIndex = (lowerIndex + upperIndex) / 2;
            valueAtMidIndex = extractSampleIndexFromInteger(intArray[midIndex]);
            if (sampleIndex == valueAtMidIndex) {
                return midIndex;
            } else if (sampleIndex > valueAtMidIndex) {
                lowerIndex = midIndex + 1;
            } else {
                upperIndex = midIndex - 1;
            }
        }
        return -1;
    }

    /** Given a sample index, this searches for the sample index in the given 
	  * array.  If it cannot find it, then null is returned.  If the sample 
	  * index is found, then the counts contained within the integer are returned.
	 */
    private static byte[] findCountsForSampleIndex(short sampleIndex, int[] intArray, int trueLength, byte[] sampleCountsReturned) {
        int resultIndex = binarySearchSampleIndex(sampleIndex, intArray, trueLength);
        if (resultIndex >= 0) {
            return extractCountsFromInteger(intArray[resultIndex], sampleCountsReturned);
        } else {
            return null;
        }
    }

    public static void TestSpilloverPacking() {
        short sampleIndex = (short) 32767;
        byte count1 = (byte) -1;
        byte count2 = (byte) -1;
        int result = getSpilloverCompactForm(sampleIndex, count1, count2);
        System.out.println("Num = " + result);
        System.out.println("Hex Representation: " + Integer.toHexString(result));
        System.out.println("Sample Index in Integer = " + sampleIndexInInteger(sampleIndex, result));
        System.out.println("Sample Index = " + extractSampleIndexFromInteger(result));
        byte[] counts = new byte[CopyNumberACPTranslator.NumAllelesInCallSet];
        extractCountsFromInteger(result, counts);
        System.out.println("Byte0 = " + counts[0]);
        System.out.println("Byte1 = " + counts[1]);
        CopyNumberBitSetSpillover cnbss = new CopyNumberBitSetSpillover();
        for (int i = 0; i < 10; i++) {
            sampleIndex = (short) (i * 3);
            cnbss.registerSpilloverSampleIndexAndCounts(sampleIndex, (byte) i, (byte) (i * 2));
        }
        for (int i = 0; i < 10; i++) {
            sampleIndex = (short) (i * 3);
            byte[] results = cnbss.getSpilloverCountsForSampleIndex(sampleIndex, counts);
            System.out.println("(" + sampleIndex + ", " + counts[0] + ", " + counts[1] + ")");
        }
        System.out.println("---- Testing Binary Search ----");
        int[] arr = { 2, 3, 7 };
        for (int i = 0; i < arr.length; i++) {
            System.out.println("Index of " + arr[i] + ": " + binarySearchSampleIndex((short) arr[i], arr, arr.length));
        }
        System.out.println("Index of 4: " + binarySearchSampleIndex((short) 4, arr, arr.length));
        System.out.println("Index of 1: " + binarySearchSampleIndex((short) 1, arr, arr.length));
        System.out.println("Index of 22: " + binarySearchSampleIndex((short) 22, arr, arr.length));
        System.out.println("Index of 101: " + binarySearchSampleIndex((short) 101, arr, arr.length));
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        TestSpilloverPacking();
    }
}
