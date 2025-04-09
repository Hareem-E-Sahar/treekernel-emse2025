package edu.cmu.sphinx.knowledge.language.large;

/**
 * Implements a buffer for trigrams read from disk.
 */
public class TrigramBuffer extends NGramBuffer {

    /**
     * Constructs a TrigramBuffer object with the given byte[].
     *
     * @param trigramsOnDisk the byte[] with trigrams
     * @param numberNGrams the number of trigram follows in the byte[]
     */
    public TrigramBuffer(byte[] trigramsOnDisk, int numberNGrams, boolean bigEndian) {
        super(trigramsOnDisk, numberNGrams, bigEndian);
    }

    /**
     * Finds the trigram probability ID for the given third word in a trigram.
     *
     * @param thirdWordID the ID of the third word
     *
     * @return the Trigram Probability ID of the given third word
     */
    public int findProbabilityID(int thirdWordID) {
        int mid, start = 0, end = getNumberNGrams();
        int trigram = -1;
        while ((end - start) > 0) {
            mid = (start + end) / 2;
            int midWordID = getWordID(mid);
            if (midWordID < thirdWordID) {
                start = mid + 1;
            } else if (midWordID > thirdWordID) {
                end = mid;
            } else {
                trigram = getProbabilityID(mid);
                break;
            }
        }
        return trigram;
    }

    /**
     * Returns the TrigramProbability of the nth follower.
     *
     * @param nthFollower which follower
     *
     * @return the TrigramProbability of the nth follower
     */
    public final int getProbabilityID(int nthFollower) {
        int nthPosition = nthFollower * LargeTrigramModel.BYTES_PER_TRIGRAM;
        setPosition(nthPosition + 2);
        return readTwoBytesAsInt();
    }
}
