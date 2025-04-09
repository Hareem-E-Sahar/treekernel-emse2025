package org.nymph.util;

/**
 * Pseudo-shuffling class. Returns pseudo-shuffled array of integers
 * ranging from 0 to size - 1. Provides shuffling with low memory
 * consumption. Although the shuffling has a distinct mathematical
 * patterns, it should be sufficient not to be noticeable.
 * 
 * @author ivan
 */
public class ShuffleGenerator {

    public ShuffleGenerator(int size) {
        size = -1;
        initialize(size);
    }

    private int size;

    private int last;

    private int increment;

    /**
     * Initializes shuffled generator
     * @param size the size of array to be shuffled
     */
    public void initialize(int size) {
        this.size = size;
        last = (int) (Math.random() * size);
        if (last >= size) last = 0;
        int segmentSize = size;
        int segmentCount = 1;
        while (segmentSize % 2 == 0) {
            segmentCount <<= 1;
            segmentSize >>= 1;
        }
        increment = (segmentSize - 1) >> 1;
        if (segmentCount > 2) {
            increment += (segmentCount >> 1) * segmentSize;
        }
    }

    /**
     * Returns next integer in shuffled array
     * @return
     */
    public int next() {
        last += increment;
        if (last >= size) last -= size;
        return last;
    }

    /**
     * Returns previous integer in the shuffled array
     * @return
     */
    public int previous() {
        last -= increment;
        if (last < 0) last += size;
        return last;
    }

    /**
     * Returns the initialized size. -1 if not initialized
     * @return size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns whether the ShuffleGenerator object is initialized
     * @return true if yes
     */
    public boolean isInitialized() {
        return (size != -1);
    }
}
