package edu.cmu.sphinx.knowledge.language.large;

/**
 * Implements a buffer for bigrams read from disk.
 */
public class BigramBuffer extends NGramBuffer {

    /**
     * Constructs a BigramBuffer object with the given byte[].
     *
     * @param bigramsOnDisk the byte[] with bigrams
     * @param numberNGrams the number of bigram follows in the byte[]
     */
    public BigramBuffer(byte[] bigramsOnDisk, int numberNGrams, boolean bigEndian) {
        super(bigramsOnDisk, numberNGrams, bigEndian);
    }

    /**
     * Finds the bigram probabilities for the given second word in a bigram.
     *
     * @param secondWordID the ID of the second word
     *
     * @return the BigramProbability of the given second word
     */
    public BigramProbability findBigram(int secondWordID) {
        int mid, start = 0, end = getNumberNGrams() - 1;
        BigramProbability bigram = null;
        while ((end - start) > 0) {
            mid = (start + end) / 2;
            int midWordID = getWordID(mid);
            if (midWordID < secondWordID) {
                start = mid + 1;
            } else if (midWordID > secondWordID) {
                end = mid;
            } else {
                bigram = getBigramProbability(mid);
                break;
            }
        }
        return bigram;
    }

    /**
     * Returns the BigramProbability of the nth follower.
     *
     * @param nthFollower which follower
     *
     * @return the BigramProbability of the nth follower
     */
    public final BigramProbability getBigramProbability(int nthFollower) {
        int nthPosition = nthFollower * LargeTrigramModel.BYTES_PER_BIGRAM;
        setPosition(nthPosition);
        int wordID = readTwoBytesAsInt();
        int probID = readTwoBytesAsInt();
        int backoffID = readTwoBytesAsInt();
        int firstTrigram = readTwoBytesAsInt();
        return (new BigramProbability(nthFollower, wordID, probID, backoffID, firstTrigram));
    }
}
