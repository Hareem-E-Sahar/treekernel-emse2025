package edu.cmu.sphinx.linguist.language.ngram.large4;

/**
 * Implements a buffer for bigrams read from disk.
 */
class BigramBuffer extends NGramBuffer {

    /**
     * Constructs a BigramBuffer object with the given byte[].
     *
     * @param bigramsOnDisk the byte[] with bigrams
     * @param numberNGrams the number of bigram follows in the byte[]
     */
    public BigramBuffer(byte[] bigramsOnDisk, int numberNGrams, boolean bigEndian, int bytesPerIDField) {
        super(bigramsOnDisk, numberNGrams, bigEndian, bytesPerIDField);
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
        while ((end - start) > 0) {
            mid = (start + end) / 2;
            int midWordID = getWordID(mid);
            if (midWordID < secondWordID) {
                start = mid + 1;
            } else end = mid;
        }
        if (end != getNumberNGrams() - 1 && secondWordID == getWordID(end)) return getBigramProbability(end);
        return null;
    }

    /**
     * Returns the BigramProbability of the nth follower.
     *
     * @param nthFollower which follower
     *
     * @return the BigramProbability of the nth follower
     */
    public final BigramProbability getBigramProbability(int nthFollower) {
        int nthPosition = nthFollower * LargeQuadrigramModel.ID_FIELDS_PER_BIGRAM * getBytesPerIDField();
        setPosition(nthPosition);
        int wordID = readIDField();
        int probID = readIDField();
        int backoffID = readIDField();
        int firstTrigram = readIDField();
        return (new BigramProbability(nthFollower, wordID, probID, backoffID, firstTrigram));
    }
}
