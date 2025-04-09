package edu.uga.dawgpack.singlematch.align;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.mapred.JobConf;
import cern.colt.list.LongArrayList;
import edu.uga.dawgpack.allvalid.align.AlignmentDetails;
import edu.uga.dawgpack.common.Constants;
import edu.uga.dawgpack.common.GenomeMetadata;
import edu.uga.dawgpack.common.Sequence;
import edu.uga.dawgpack.common.Utils;
import edu.uga.dawgpack.index.util.BytesWritable;
import edu.uga.dawgpack.index.util.HugeByteArray;
import edu.uga.dawgpack.index.util.HugeLongArray;
import edu.uga.dawgpack.test.DebugHelper;

/**
 * @author Juber Ahamad Patel
 * 
 *         represents the BBWT index of a chromosome
 * 
 * 
 * 
 */
public class Index {

    /**
	 * the number of the chromosome represented by this index
	 */
    private String chromosomeNumbers;

    /**
	 * bitwise representation of BWT of the chromosome
	 */
    private Sequence bwt;

    /**
	 * bitwise representation of BWT of the reverse of the chromosome
	 */
    private Sequence rbwt;

    /**
	 * the length of the segment represented by this index
	 */
    private long genomeLength;

    /**
	 * array of 4 elements - total number of characters in the chromosome that are smaller than
	 * given character
	 */
    private long[] count;

    /**
	 * 4 X k array storing the number of times a given character occurs in first n positions of bwt
	 * Only values at specified interval are stored and rest are calculated as needed
	 */
    private int[][] occ;

    /**
	 * 4 X k array storing the number of times a given character occurs in first n positions of rbwt
	 * Only values at specified interval are stored and rest are calculated as needed
	 */
    private int[][] rocc;

    /**
	 * the interval at which SA values are sampled and stored in sa[]
	 */
    private int saInterval;

    /**
	 * the SA this field is empty in production mode but is used in test mode
	 */
    protected HugeLongArray sa;

    protected GenomeMetadata metadata;

    /**
	 * the interval at which occ values are sampled and stored in occ[]
	 * 
	 * this value must be a power of 2
	 */
    private int occInterval;

    /**
	 *the log base 2 of occInterval, used for bit operations.
	 */
    private int logOccInterval;

    /**
	 * keep track of the index at which the special null character is in bwt
	 */
    private long bwtNullPosition;

    /**
	 * keep track of the index at which the special null character is in rbwt
	 */
    private long rbwtNullPosition;

    /**
	 * array to store plan for each position of a single mismatch 1-> backward, 2->forward
	 */
    protected int[] oneMismatchPlan;

    /**
	 * array to store plan for each pair of mismatches 1-> backward, 2->forward, 3->bidirectional
	 * backward-forward, 4 -> bidirectional forward-backward
	 */
    protected int[][] twoMismatchPlan;

    /**
	 * the maximum length for the bidirectional segment starting at an the index location the index
	 * in the array is the index location and value at that place is the maximum right index it has
	 * to reach according to the plan. this is how much we need to store for bidirectional traversal
	 */
    protected int[] bidirectionalOutreach;

    /**
	 * length of each read
	 */
    public final int readLength;

    /**
	 * allowed mismatches
	 */
    protected int mismatches;

    /**
	 * allowed gaps
	 */
    private int gaps;

    /**
	 * for testing the correctness of alignment
	 * 
	 * genome text for this segment
	 */
    protected HugeByteArray genomeText;

    /**
	 * queue for the single point update of saDensities[]
	 */
    private BlockingQueue<long[]> updateQueue;

    /**
	 * minimum distance between the pair of reads
	 */
    private int minDistance;

    /**
	 * maximum distance between the pair of reads
	 */
    private int maxDistance;

    public Index(int readLength, int mismatches, int gaps, int minDistance, int maxDistance, int occInterval, long cacheSize, HugeByteArray genomeText) {
        this(readLength, mismatches, gaps, occInterval, cacheSize);
        this.genomeText = genomeText;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public Index(int readLength, int mismatches, int gaps, int occInterval, long cacheSize) {
        this.readLength = readLength;
        this.mismatches = mismatches;
        this.gaps = gaps;
        this.occInterval = occInterval;
    }

    private void createOccArrays(int interval) {
        occInterval = interval;
        logOccInterval = -1;
        while (occInterval != 0) {
            occInterval = occInterval >> 1;
            logOccInterval++;
        }
        occInterval = 1 << logOccInterval;
        if (occInterval > 32) {
            occInterval = 32;
            logOccInterval = 5;
        }
        int occArrayLength = (int) ((genomeLength - 1) / occInterval) + 1;
        System.out.println(new Date() + " DEBUUUUUUUG: creating occ arrays with length " + occArrayLength);
        occ = new int[4][occArrayLength];
        rocc = new int[4][occArrayLength];
    }

    private void populateOccArrays() {
        System.out.println(new Date() + " DEBUUUUUUUG: populating occ arrays ...");
        int intervalCounter = occInterval - 1;
        int[] occValue = new int[4];
        int[] roccValue = new int[4];
        int occIndex = 0;
        int roccIndex = 0;
        int bwtValue;
        int rbwtValue;
        for (long i = 0; i < genomeLength; i++) {
            bwtValue = bwt(i);
            rbwtValue = rbwt(i);
            if (bwtValue == 0) {
            } else if (bwtValue == 1) {
                occValue[0] = occValue[0] + 1;
            } else if (bwtValue == 2) {
                occValue[1] = occValue[1] + 1;
            } else if (bwtValue == 3) {
                occValue[2] = occValue[2] + 1;
            } else {
                occValue[3] = occValue[3] + 1;
            }
            if (rbwtValue == 0) {
            } else if (rbwtValue == 1) {
                roccValue[0] = roccValue[0] + 1;
            } else if (rbwtValue == 2) {
                roccValue[1] = roccValue[1] + 1;
            } else if (rbwtValue == 3) {
                roccValue[2] = roccValue[2] + 1;
            } else {
                roccValue[3] = roccValue[3] + 1;
            }
            intervalCounter++;
            if (intervalCounter == occInterval) {
                occ[0][occIndex] = occValue[0];
                occ[1][occIndex] = occValue[1];
                occ[2][occIndex] = occValue[2];
                occ[3][occIndex] = occValue[3];
                occIndex++;
                rocc[0][roccIndex] = roccValue[0];
                rocc[1][roccIndex] = roccValue[1];
                rocc[2][roccIndex] = roccValue[2];
                rocc[3][roccIndex] = roccValue[3];
                roccIndex++;
                intervalCounter = 0;
            }
        }
    }

    /**
	 * for each set of mismatches, decide which of the matching approach is best 1 -> backward, 2 ->
	 * forward, 3 -> bidirectional backward-forward, 4 -> bidirectional forward-backward
	 */
    private void createMatchingPlan() {
        System.out.println(new Date() + " DEBUUUUUUUG: creating matching plan.....");
        oneMismatchPlan = new int[readLength];
        twoMismatchPlan = new int[readLength][readLength];
        bidirectionalOutreach = new int[readLength];
        double forwardCost = 0;
        double backwardCost = 0;
        double bfCost = 0;
        double fbCost = 0;
        int x;
        int y;
        int z;
        for (int i = 0; i < readLength; i++) {
            x = i;
            y = readLength - x - 1;
            if (x < 2.5 * y) oneMismatchPlan[i] = 1; else oneMismatchPlan[i] = 2;
            for (int j = i + 1; j < readLength; j++) {
                x = i;
                y = j - i - 1;
                z = readLength - 1 - j;
                backwardCost = y + 3 * x;
                forwardCost = 2.5 * y + 7.5 * z;
                bfCost = 2.5 * x + 7.5 * z;
                fbCost = 2.5 * z + 3 * x;
                if (backwardCost <= forwardCost && backwardCost <= bfCost && backwardCost <= fbCost) {
                    twoMismatchPlan[i][j] = 1;
                } else if (forwardCost <= backwardCost && forwardCost <= bfCost && forwardCost <= fbCost) {
                    twoMismatchPlan[i][j] = 2;
                } else if (bfCost <= backwardCost && bfCost <= forwardCost && bfCost <= fbCost) {
                    twoMismatchPlan[i][j] = 3;
                    if (bidirectionalOutreach[i + 1] < j - 1) bidirectionalOutreach[i + 1] = j - 1;
                } else {
                    twoMismatchPlan[i][j] = 4;
                    if (bidirectionalOutreach[i + 1] < j - 1) bidirectionalOutreach[i + 1] = j - 1;
                }
            }
        }
    }

    /**
	 * return the character at given index in bwt as if bwt were not bit-encoded
	 * 
	 * @param bwt
	 * @param index
	 * @return 0 to 4 depending on the character
	 */
    private int bwt(long index) {
        if (index == bwtNullPosition) return 0; else return bwt.get(index);
    }

    /**
	 * return the character at given index in rbwt as if bwt were not bit-encoded
	 * 
	 * @param bwt
	 * @param index
	 * @return 0 to 4 depending on the character
	 */
    private int rbwt(long index) {
        if (index == rbwtNullPosition) return 0; else return rbwt.get(index);
    }

    /**
	 * return index in the alphabet of the given character
	 * 
	 * @param c
	 * @return
	 */
    private int indexOf(char c) {
        if (c == 'A') return 1; else if (c == 'C') return 2; else if (c == 'G') return 3; else if (c == 'T') return 4; else if (c == '#') return 0; else return -1;
    }

    /**
	 * return number of occurrences in [0, till] of the given character in bwt
	 * 
	 * The complexity of this method (and rocc()) is due to performance engineering. The program
	 * spends most of its time in these methods and methods called from these methods, like
	 * Sequence.getOccurrences. These are the places to optimize
	 * 
	 * @param character
	 *            1 to 4 depending on the character
	 * @param till
	 * @return
	 */
    private int occ(int character, long till) {
        long occIndex = till >> logOccInterval;
        long fromBWTIndex = (occIndex << logOccInterval) + 1;
        int occurrences = occ[character - 1][(int) occIndex];
        if (fromBWTIndex <= till) occurrences += bwt.getOccurrences(character, fromBWTIndex, till);
        if (character == 1 && bwtNullPosition >= fromBWTIndex && bwtNullPosition <= till) occurrences--;
        return occurrences;
    }

    private int occWithNullCheck(int character, long till) {
        if (character == 0) {
            if (till >= bwtNullPosition) return 1; else return 0;
        } else {
            return occ(character, till);
        }
    }

    /**
	 * return number of occurrences in [0, till] of the given character in rbwt
	 * 
	 * @param character
	 *            0 to 4 depending on the character
	 * @param till
	 * @return
	 */
    private int rocc(int character, long till) {
        if (character == 0) {
            if (till >= rbwtNullPosition) return 1; else return 0;
        }
        long roccIndex = (int) (till >> logOccInterval);
        long fromRBWTIndex = (roccIndex << logOccInterval) + 1;
        int occurrences = rocc[character - 1][(int) roccIndex];
        if (fromRBWTIndex <= till) occurrences = occurrences + rbwt.getOccurrences(character, fromRBWTIndex, till);
        if (character == 1 && rbwtNullPosition >= fromRBWTIndex && rbwtNullPosition <= till) occurrences--;
        return occurrences;
    }

    /**
	 * calculate the sa range of the segment [i, j] and all sub-suffixes, from the scratch
	 */
    protected void saRange(byte[] read, int start, int end, long[][][] saRanges) {
        long rangeStart;
        long rangeEnd;
        int prefix = read[end];
        rangeStart = count[prefix];
        if (prefix == 4) rangeEnd = genomeLength - 1; else rangeEnd = count[prefix + 1] - 1;
        saRanges[end][end][0] = rangeStart;
        saRanges[end][end][1] = rangeEnd;
        for (int i = end - 1; i >= start; i--) {
            prefix = read[i];
            rangeStart = count[prefix] + occ(prefix, rangeStart - 1);
            rangeEnd = count[prefix] + occ(prefix, rangeEnd) - 1;
            if (rangeStart > rangeEnd) {
                while (i >= 0) {
                    saRanges[i][end][0] = AlignerConstants.INVALID;
                    i--;
                }
                return;
            } else {
                saRanges[i][end][0] = rangeStart;
                saRanges[i][end][1] = rangeEnd;
            }
        }
    }

    /**
	 * calculate the sa and rsa range of the segment [i, j] and all sub-prefixes, from the scratch
	 */
    protected void rsaRange(byte[] read, int start, int end, long[][][] saRanges, long[][][] rsaRanges) {
        long offset;
        int suffix;
        long rangeStart;
        long rangeEnd;
        suffix = read[start];
        rangeStart = count[suffix];
        if (suffix == 4) rangeEnd = genomeLength - 1; else rangeEnd = count[suffix + 1] - 1;
        rsaRanges[start][start][0] = rangeStart;
        rsaRanges[start][start][1] = rangeEnd;
        saRanges[start][start][0] = rangeStart;
        saRanges[start][start][1] = rangeEnd;
        for (int index = start + 1; index <= end; index++) {
            suffix = read[index];
            offset = 0;
            for (int i = 0; i < suffix; i++) {
                rangeStart = count[i] + rocc(i, rsaRanges[start][index - 1][0] - 1);
                rangeEnd = count[i] + rocc(i, rsaRanges[start][index - 1][1]) - 1;
                if (rangeStart > rangeEnd) continue;
                offset = offset + (rangeEnd - rangeStart + 1);
            }
            rangeStart = count[suffix] + rocc(suffix, rsaRanges[start][index - 1][0] - 1);
            rangeEnd = count[suffix] + rocc(suffix, rsaRanges[start][index - 1][1]) - 1;
            if (rangeStart > rangeEnd) {
                while (index <= end) {
                    saRanges[start][index][0] = AlignerConstants.INVALID;
                    index++;
                }
                return;
            }
            long own = rangeEnd - rangeStart + 1;
            rsaRanges[start][index][0] = rangeStart;
            rsaRanges[start][index][1] = rangeEnd;
            saRanges[start][index][0] = saRanges[start][index - 1][0] + offset;
            saRanges[start][index][1] = saRanges[start][index][0] + own - 1;
        }
    }

    /**
	 * find the SA range of prefix + suffix given SA range of suffix
	 * 
	 * @param prefix
	 * @param range
	 * @return
	 */
    private long[] saRangecP(int prefix, long[] range) {
        long[] ar = new long[2];
        ar[0] = count[prefix] + occ(prefix, range[0] - 1);
        ar[1] = count[prefix] + occ(prefix, range[1]) - 1;
        return ar;
    }

    private long[] rsaRangecP(int[] pattern, int index, long[] saRange, long[] rsaRange) {
        long offset = 0;
        long[] ar;
        int prefix = pattern[index];
        for (int i = 0; i < prefix; i++) {
            ar = saRangecP(i, saRange);
            if (ar[0] > ar[1]) continue;
            offset = offset + (ar[1] - ar[0] + 1);
        }
        ar = saRangecP(prefix, saRange);
        long own = ar[1] - ar[0] + 1;
        ar[0] = rsaRange[0] + offset;
        ar[1] = ar[0] + own - 1;
        return ar;
    }

    /**
	 * find the SA range of prefix + suffix given SA and reverse SA ranges of prefix
	 * 
	 * adding suffix is 2.5 times costlier than adding prefix
	 * 
	 * @param suffix
	 * @param saRange
	 * @param rsaRange
	 * @return
	 */
    private long[] saRangePc(int suffix, long[] saRange, long[] rsaRange) {
        long offset = 0;
        long[] ar;
        for (int i = 0; i < suffix; i++) {
            ar = rsaRangePc(i, rsaRange);
            if (ar[0] > ar[1]) continue;
            offset = offset + (ar[1] - ar[0] + 1);
        }
        ar = rsaRangePc(suffix, rsaRange);
        long own = ar[1] - ar[0] + 1;
        ar[0] = saRange[0] + offset;
        ar[1] = ar[0] + own - 1;
        return ar;
    }

    /**
	 * find the reverse SA range of reverse(prefix + suffix) given reverse SA range of prefix
	 * 
	 * adding suffix is 2.5 times costlier than adding prefix
	 * 
	 * @param suffix
	 * @param range
	 * @return
	 */
    private long[] rsaRangePc(int suffix, long[] range) {
        long[] ar = new long[2];
        ar[0] = count[suffix] + rocc(suffix, range[0] - 1);
        ar[1] = count[suffix] + rocc(suffix, range[1]) - 1;
        return ar;
    }

    /**
	 * align the given batch of read pairs against the genome and return the matches
	 * 
	 * @param reads
	 *            a batch of reads
	 * @param saDensities
	 * @param mismatches
	 * @param gaps
	 * @return
	 * @throws InterruptedException
	 */
    public long alignPairs(byte[][] reads, BlockingQueue<long[][]> outQueue) throws InterruptedException {
        long[][] alignments = new long[reads.length][2];
        int alignmentCounter = 0;
        boolean success;
        byte[] read1;
        byte[] read2;
        byte[] read1Reverse;
        byte[] read2Reverse;
        for (int i = 0; i < reads.length; i += 2) {
            read1 = reads[i];
            read2 = reads[i + 1];
            read2Reverse = edu.uga.dawgpack.common.Utils.reverseComplement(read2);
            read1Reverse = edu.uga.dawgpack.common.Utils.reverseComplement(read1);
        }
        alignments[alignmentCounter][0] = Constants.INVALID;
        alignments[alignmentCounter][1] = Constants.INVALID;
        outQueue.put(alignments);
        return alignmentCounter;
    }

    /**
	 * align the given batch of reads against the genome and return the matches
	 * 
	 * @param reads
	 *            a batch of reads
	 * @param saDensities
	 * @param mismatches
	 * @param gaps
	 * @return
	 * @throws InterruptedException
	 */
    public long align(byte[][] reads) throws InterruptedException {
        long[] matchRanges;
        LongArrayList rangeList;
        long total = 0;
        for (int i = 0; i < reads.length; i++) {
            rangeList = new LongArrayList(100);
            matchRanges = rangeList.elements();
            matchRanges = Arrays.copyOf(matchRanges, rangeList.size());
            long matchCount = 0;
            for (int j = 1; j < matchRanges.length; j = j + 2) {
                matchCount = matchCount + matchRanges[j] - matchRanges[j - 1] + 1;
            }
            if (matchCount > 100000) {
                System.out.println(new Date() + " DEBUUUUUUUG: " + reads[i] + " has " + matchCount + " matches");
                continue;
            }
            total += matchCount;
        }
        return total;
    }

    /**
	 * currently implementing two-mismatch, zero gaps search, extend afterwards
	 * 
	 * 
	 */
    protected AlignmentDetails alignForUnique(byte[] read1, byte[] read2) {
        if (Utils.decode(read1).equals("TTGCCTGGGTCTAAATCTGGACTGTATGATCTTTGG")) {
            int a = 5;
        }
        if (Utils.decode(read1).equals("TACGTGGCTTTCTTGTCCCACTCTATAGCAAGCTAA") || Utils.decode(read2).equals("TACGTGGCTTTCTTGTCCCACTCTATAGCAAGCTAA")) {
            int a = 5;
        }
        AlignmentDetails alignmentDetails = null;
        int lastSize = 0;
        long[][][] read1SARanges = new long[readLength][readLength][2];
        long[][][] read2SARanges = new long[readLength][readLength][2];
        saRange(read1, 0, read1.length - 1, read1SARanges);
        if (read1SARanges[0][readLength - 1][0] != Constants.INVALID) {
            if (read1SARanges[0][readLength - 1][0] == read1SARanges[0][readLength - 1][1]) {
                alignmentDetails = new AlignmentDetails(read1SARanges[0][readLength - 1][0], true, 0, -1, -1);
            } else {
                return null;
            }
        }
        saRange(read2, 0, read2.length - 1, read2SARanges);
        if (read2SARanges[0][readLength - 1][0] != Constants.INVALID) {
            if (alignmentDetails != null) {
                return null;
            } else {
                if (read2SARanges[0][readLength - 1][0] != read2SARanges[0][readLength - 1][1]) {
                    return null;
                } else {
                    return new AlignmentDetails(read2SARanges[0][readLength - 1][0], false, 0, -1, -1);
                }
            }
        }
        if (alignmentDetails != null) return alignmentDetails;
        long[][][] read1RSARanges = new long[readLength][readLength][2];
        long[][][] read2RSARanges = new long[readLength][readLength][2];
        rsaRange(read1, 0, read1.length - 1, read1SARanges, read1RSARanges);
        rsaRange(read2, 0, read2.length - 1, read2SARanges, read2RSARanges);
        int approach;
        LongArrayList matchSARanges = new LongArrayList();
        boolean onPositiveStrand = false;
        for (int i = 0; i < readLength; i++) {
            approach = oneMismatchPlan[i];
            switch(approach) {
                case 1:
                    if (read1SARanges[i + 1][readLength - 1][0] != AlignerConstants.INVALID) {
                        combineBackward(read1, i, read1SARanges[i + 1][readLength - 1][0], read1SARanges[i + 1][readLength - 1][1], matchSARanges);
                        if (matchSARanges.size() != lastSize) {
                            lastSize = matchSARanges.size();
                            if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                return null;
                            } else {
                                if (alignmentDetails != null) {
                                    return null;
                                } else {
                                    alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), true, 1, i, -1);
                                }
                            }
                        }
                    }
                    if (read2SARanges[i + 1][readLength - 1][0] != AlignerConstants.INVALID) {
                        combineBackward(read2, i, read2SARanges[i + 1][readLength - 1][0], read2SARanges[i + 1][readLength - 1][1], matchSARanges);
                        if (matchSARanges.size() != lastSize) {
                            lastSize = matchSARanges.size();
                            if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                return null;
                            } else {
                                if (alignmentDetails != null) {
                                    return null;
                                } else {
                                    alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), false, 1, i, -1);
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    if (read1SARanges[0][i - 1][0] != AlignerConstants.INVALID) {
                        combineForward(read1, i, read1SARanges[0][i - 1][0], read1SARanges[0][i - 1][1], read1RSARanges[0][i - 1][0], read1RSARanges[0][i - 1][1], matchSARanges);
                        if (matchSARanges.size() != lastSize) {
                            lastSize = matchSARanges.size();
                            if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                return null;
                            } else {
                                if (alignmentDetails != null) {
                                    return null;
                                } else {
                                    alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), true, 1, i, -1);
                                }
                            }
                        }
                    }
                    if (read2SARanges[0][i - 1][0] != AlignerConstants.INVALID) {
                        combineForward(read2, i, read2SARanges[0][i - 1][0], read2SARanges[0][i - 1][1], read2RSARanges[0][i - 1][0], read2RSARanges[0][i - 1][1], matchSARanges);
                        if (matchSARanges.size() != lastSize) {
                            lastSize = matchSARanges.size();
                            if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                return null;
                            } else {
                                if (alignmentDetails != null) {
                                    return null;
                                } else {
                                    alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), false, 1, i, -1);
                                }
                            }
                        }
                    }
                    break;
            }
        }
        if (alignmentDetails != null) return alignmentDetails;
        for (int i = 1; i < bidirectionalOutreach.length; i++) {
            rsaRange(read1, i, bidirectionalOutreach[i], read1SARanges, read1RSARanges);
            rsaRange(read2, i, bidirectionalOutreach[i], read2SARanges, read2RSARanges);
        }
        for (int i = 0; i < readLength; i++) {
            for (int j = i + 1; j < readLength; j++) {
                approach = twoMismatchPlan[i][j];
                switch(approach) {
                    case 1:
                        if (read1SARanges[j + 1][readLength - 1][0] != AlignerConstants.INVALID) {
                            combineBackward(read1, i, j, read1SARanges[j + 1][readLength - 1][0], read1SARanges[j + 1][readLength - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), true, 2, i, j);
                                    }
                                }
                            }
                        }
                        if (read2SARanges[j + 1][readLength - 1][0] != AlignerConstants.INVALID) {
                            combineBackward(read2, i, j, read2SARanges[j + 1][readLength - 1][0], read2SARanges[j + 1][readLength - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), false, 2, i, j);
                                    }
                                }
                            }
                        }
                        break;
                    case 2:
                        if (read1SARanges[0][i - 1][0] != AlignerConstants.INVALID) {
                            combineForward(read1, i, j, read1SARanges[0][i - 1][0], read1SARanges[0][i - 1][1], read1RSARanges[0][i - 1][0], read1RSARanges[0][i - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), true, 2, i, j);
                                    }
                                }
                            }
                        }
                        if (read2SARanges[0][i - 1][0] != AlignerConstants.INVALID) {
                            combineForward(read2, i, j, read2SARanges[0][i - 1][0], read2SARanges[0][i - 1][1], read2RSARanges[0][i - 1][0], read2RSARanges[0][i - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), false, 2, i, j);
                                    }
                                }
                            }
                        }
                        break;
                    case 3:
                        if (read1SARanges[i + 1][j - 1][0] != AlignerConstants.INVALID) {
                            combineBidirectionalBF(read1, i, j, read1SARanges[i + 1][j - 1][0], read1SARanges[i + 1][j - 1][1], read1RSARanges[i + 1][j - 1][0], read1RSARanges[i + 1][j - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), true, 2, i, j);
                                    }
                                }
                            }
                        }
                        if (read2SARanges[i + 1][j - 1][0] != AlignerConstants.INVALID) {
                            combineBidirectionalBF(read2, i, j, read2SARanges[i + 1][j - 1][0], read2SARanges[i + 1][j - 1][1], read2RSARanges[i + 1][j - 1][0], read2RSARanges[i + 1][j - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), false, 2, i, j);
                                    }
                                }
                            }
                        }
                        break;
                    case 4:
                        if (read1SARanges[i + 1][j - 1][0] != AlignerConstants.INVALID) {
                            combineBidirectionalFB(read1, i, j, read1SARanges[i + 1][j - 1][0], read1SARanges[i + 1][j - 1][1], read1RSARanges[i + 1][j - 1][0], read1RSARanges[i + 1][j - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), true, 2, i, j);
                                    }
                                }
                            }
                        }
                        if (read2SARanges[i + 1][j - 1][0] != AlignerConstants.INVALID) {
                            combineBidirectionalFB(read2, i, j, read2SARanges[i + 1][j - 1][0], read2SARanges[i + 1][j - 1][1], read2RSARanges[i + 1][j - 1][0], read2RSARanges[i + 1][j - 1][1], matchSARanges);
                            if (matchSARanges.size() != lastSize) {
                                lastSize = matchSARanges.size();
                                if (matchSARanges.size() > 2 || matchSARanges.getQuick(0) != matchSARanges.getQuick(1)) {
                                    return null;
                                } else {
                                    if (alignmentDetails != null) {
                                        return null;
                                    } else {
                                        alignmentDetails = new AlignmentDetails(matchSARanges.getQuick(0), false, 2, i, j);
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        }
        return alignmentDetails;
    }

    /**
	 * look for a valid pair of matches among the new and existing matches If such a pair is not
	 * found, add the new matches to existing matches
	 * 
	 * @param matchSARanges1
	 *            the SA ranges for newly found matches on the forward strand
	 * @param matchSARanges2
	 *            the SA ranges for newly found matches on the reverse strand
	 * @param updateRange
	 *            the array to be filled with the genomic range to be updated when a valid match
	 *            pair is found
	 * @param read1Matches
	 *            the sorted genomic positions of the existing matches on forward strand
	 * 
	 * @param read2Matches
	 *            the sorted genomic positions of the existing matches on the reverse strand
	 * 
	 * @return true if a valid match pair is found
	 */
    private boolean checkForValidPair(LongArrayList matchSARanges1, LongArrayList matchSARanges2, long[] updateRange, LongArrayList read1Matches, LongArrayList read2Matches) {
        long end;
        long value;
        long mate;
        int position;
        int size = matchSARanges1.size();
        for (int i = 0; i < size; i += 2) {
            end = matchSARanges1.get(i + 1);
            for (long j = matchSARanges1.get(i); j <= end; j++) {
                value = sa.get(j);
                mate = search(read2Matches, value, true);
                if (mate != Constants.INVALID) {
                    updateRange[0] = value;
                    updateRange[1] = mate + readLength;
                    return true;
                }
                read1Matches.add(value);
            }
        }
        read1Matches.sort();
        size = matchSARanges2.size();
        for (int i = 0; i < size; i += 2) {
            end = matchSARanges2.get(i + 1);
            for (long j = matchSARanges2.get(i); j <= end; j++) {
                value = sa.get(j);
                mate = search(read1Matches, value, false);
                if (mate != Constants.INVALID) {
                    updateRange[0] = mate;
                    updateRange[1] = value + readLength;
                    return true;
                }
                read2Matches.add(value);
            }
        }
        read2Matches.sort();
        return false;
    }

    /**
	 * search for the pair of the value at index in the sorted array
	 * 
	 * @param matches
	 * @param separator
	 * @return
	 */
    private long search(LongArrayList matches, long value, boolean onReverse) {
        int left = 0;
        int right = matches.size() - 1;
        int middle = right / 2;
        long distance;
        while (left <= right) {
            if (onReverse) {
                distance = matches.getQuick(middle) + readLength - value;
            } else {
                distance = value + readLength - matches.getQuick(middle);
            }
            if (distance >= minDistance && distance <= maxDistance) {
                return matches.getQuick(middle);
            }
            if (distance < minDistance) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
            middle = (left + right) / 2;
        }
        return Constants.INVALID;
    }

    /**
	 * combine all possibilities at given mismatch position and add resulting ranges to matchRanges
	 * rangeStart and rangeEnd represent the range so far.
	 * 
	 * @param read
	 * @param rangeStart
	 * @param rangeEnd
	 * @param matchRanges
	 */
    protected void combineBackward(byte[] read, int mismatchPosition, long rangeStart, long rangeEnd, LongArrayList matchRanges) {
        long rangeStart1;
        long rangeEnd1;
        int prefix;
        for (int i = 1; i <= 4; i++) {
            if (i == read[mismatchPosition]) continue;
            rangeStart1 = count[i] + occ(i, rangeStart - 1);
            rangeEnd1 = count[i] + occ(i, rangeEnd) - 1;
            if (rangeStart1 > rangeEnd1) continue;
            for (int k = mismatchPosition - 1; k >= 0; k--) {
                prefix = read[k];
                rangeStart1 = count[prefix] + occ(prefix, rangeStart1 - 1);
                rangeEnd1 = count[prefix] + occ(prefix, rangeEnd1) - 1;
                if (rangeStart1 > rangeEnd1) break;
            }
            if (rangeStart1 <= rangeEnd1) {
                matchRanges.add(rangeStart1);
                matchRanges.add(rangeEnd1);
            }
        }
    }

    /**
	 * combine all possibilities at given mismatch positions and add resulting ranges to locations
	 * rangeStart and rangeEnd represent the range so far.
	 * 
	 * @param read
	 * @param rightMismatch
	 * @param leftMismatch
	 * @param saRangeStart
	 * @param saRangeEnd
	 * @param matchRanges
	 */
    protected void combineForward(byte[] read, int mismatchPosition, long saRangeStart, long saRangeEnd, long rsaRangeStart, long rsaRangeEnd, LongArrayList matchRanges) {
        long saRangeStart1 = saRangeStart;
        long saRangeEnd1 = saRangeEnd;
        long rsaRangeStart1 = rsaRangeStart;
        long rsaRangeEnd1 = rsaRangeEnd;
        long rsaRangeStart2 = rsaRangeStart;
        long rsaRangeEnd2 = rsaRangeEnd;
        int suffix;
        long offset;
        for (int i = 1; i <= 4; i++) {
            if (i == read[mismatchPosition]) continue;
            offset = 0;
            for (int x = 0; x < i; x++) {
                rsaRangeStart1 = count[x] + rocc(x, rsaRangeStart - 1);
                rsaRangeEnd1 = count[x] + rocc(x, rsaRangeEnd) - 1;
                if (rsaRangeStart1 > rsaRangeEnd1) continue;
                offset = offset + (rsaRangeEnd1 - rsaRangeStart1 + 1);
            }
            rsaRangeStart1 = count[i] + rocc(i, rsaRangeStart - 1);
            rsaRangeEnd1 = count[i] + rocc(i, rsaRangeEnd) - 1;
            long own = rsaRangeEnd1 - rsaRangeStart1 + 1;
            saRangeStart1 = saRangeStart + offset;
            saRangeEnd1 = saRangeStart1 + own - 1;
            if (saRangeStart1 > saRangeEnd1) continue;
            for (int k = mismatchPosition + 1; k < readLength; k++) {
                suffix = read[k];
                offset = 0;
                for (int x = 0; x < suffix; x++) {
                    rsaRangeStart2 = count[x] + rocc(x, rsaRangeStart1 - 1);
                    rsaRangeEnd2 = count[x] + rocc(x, rsaRangeEnd1) - 1;
                    if (rsaRangeStart2 > rsaRangeEnd2) continue;
                    offset = offset + (rsaRangeEnd2 - rsaRangeStart2 + 1);
                }
                rsaRangeStart1 = count[suffix] + rocc(suffix, rsaRangeStart1 - 1);
                rsaRangeEnd1 = count[suffix] + rocc(suffix, rsaRangeEnd1) - 1;
                own = rsaRangeEnd1 - rsaRangeStart1 + 1;
                saRangeStart1 = saRangeStart1 + offset;
                saRangeEnd1 = saRangeStart1 + own - 1;
                if (saRangeStart1 > saRangeEnd1) break;
            }
            if (saRangeStart1 <= saRangeEnd1) {
                matchRanges.add(saRangeStart1);
                matchRanges.add(saRangeEnd1);
            }
        }
    }

    /**
	 * combine all possibilities at given mismatch positions and add resulting ranges to locations
	 * rangeStart and rangeEnd represent the range so far.
	 * 
	 * @param read
	 * @param rightMismatch
	 * @param leftMismatch
	 * @param rangeStart
	 * @param rangeEnd
	 * @param matchRanges
	 * @param isInCache
	 */
    protected void combineBackward(byte[] read, int leftMismatch, int rightMismatch, long rangeStart, long rangeEnd, LongArrayList matchRanges) {
        long rangeStart1;
        long rangeEnd1;
        long rangeStart2;
        long rangeEnd2;
        int prefix;
        for (int i = 1; i <= 4; i++) {
            if (i == read[rightMismatch]) continue;
            rangeStart1 = count[i] + occ(i, rangeStart - 1);
            rangeEnd1 = count[i] + occ(i, rangeEnd) - 1;
            if (rangeStart1 > rangeEnd1) continue;
            for (int k = rightMismatch - 1; k > leftMismatch; k--) {
                prefix = read[k];
                rangeStart1 = count[prefix] + occ(prefix, rangeStart1 - 1);
                rangeEnd1 = count[prefix] + occ(prefix, rangeEnd1) - 1;
                if (rangeStart1 > rangeEnd1) break;
            }
            if (rangeStart1 > rangeEnd1) continue;
            for (int j = 1; j <= 4; j++) {
                if (j == read[leftMismatch]) continue;
                rangeStart2 = count[j] + occ(j, rangeStart1 - 1);
                rangeEnd2 = count[j] + occ(j, rangeEnd1) - 1;
                if (rangeStart2 > rangeEnd2) continue;
                for (int k = leftMismatch - 1; k >= 0; k--) {
                    prefix = read[k];
                    rangeStart2 = count[prefix] + occ(prefix, rangeStart2 - 1);
                    rangeEnd2 = count[prefix] + occ(prefix, rangeEnd2) - 1;
                    if (rangeStart2 > rangeEnd2) break;
                }
                if (rangeStart2 <= rangeEnd2) {
                    matchRanges.add(rangeStart2);
                    matchRanges.add(rangeEnd2);
                }
            }
        }
    }

    /**
	 * combine all possibilities at given mismatch positions and add resulting ranges to locations
	 * rangeStart and rangeEnd represent the range so far.
	 * 
	 * @param read
	 * @param rightMismatch
	 * @param leftMismatch
	 * @param saRangeStart
	 * @param rangeEnd
	 * @param matchRanges
	 * @param isInCache
	 */
    protected void combineBidirectionalBF(byte[] read, int leftMismatch, int rightMismatch, long saRangeStart, long saRangeEnd, long rsaRangeStart, long rsaRangeEnd, LongArrayList matchRanges) {
        long saRangeStart1 = saRangeStart;
        long saRangeEnd1 = saRangeEnd;
        long rsaRangeStart1 = rsaRangeStart;
        long rsaRangeEnd1 = rsaRangeEnd;
        long saRangeStart2 = saRangeStart;
        long saRangeEnd2 = saRangeEnd;
        long rsaRangeStart2 = rsaRangeStart;
        long rsaRangeEnd2 = rsaRangeEnd;
        long rsaRangeStart3 = rsaRangeStart;
        long rsaRangeEnd3 = rsaRangeEnd;
        int prefix;
        int suffix;
        long offset;
        for (int i = 1; i <= 4; i++) {
            if (i == read[leftMismatch]) continue;
            offset = 0;
            for (int x = 0; x < i; x++) {
                saRangeStart1 = count[x] + occWithNullCheck(x, saRangeStart - 1);
                saRangeEnd1 = count[x] + occWithNullCheck(x, saRangeEnd) - 1;
                if (saRangeStart1 > saRangeEnd1) continue;
                offset = offset + (saRangeEnd1 - saRangeStart1 + 1);
            }
            saRangeStart1 = count[i] + occ(i, saRangeStart - 1);
            saRangeEnd1 = count[i] + occ(i, saRangeEnd) - 1;
            long own = saRangeEnd1 - saRangeStart1 + 1;
            rsaRangeStart1 = rsaRangeStart + offset;
            rsaRangeEnd1 = rsaRangeStart1 + own - 1;
            if (rsaRangeStart1 > rsaRangeEnd1) continue;
            for (int k = leftMismatch - 1; k >= 0; k--) {
                prefix = read[k];
                offset = 0;
                for (int x = 0; x < prefix; x++) {
                    saRangeStart2 = count[x] + occWithNullCheck(x, saRangeStart1 - 1);
                    saRangeEnd2 = count[x] + occWithNullCheck(x, saRangeEnd1) - 1;
                    if (saRangeStart2 > saRangeEnd2) continue;
                    offset = offset + (saRangeEnd2 - saRangeStart2 + 1);
                }
                saRangeStart1 = count[prefix] + occ(prefix, saRangeStart1 - 1);
                saRangeEnd1 = count[prefix] + occ(prefix, saRangeEnd1) - 1;
                own = saRangeEnd1 - saRangeStart1 + 1;
                rsaRangeStart1 = rsaRangeStart1 + offset;
                rsaRangeEnd1 = rsaRangeStart1 + own - 1;
                if (rsaRangeStart1 > rsaRangeEnd1) break;
            }
            if (rsaRangeStart1 > rsaRangeEnd1) continue;
            for (int j = 1; j <= 4; j++) {
                if (j == read[rightMismatch]) continue;
                offset = 0;
                for (int x = 0; x < j; x++) {
                    rsaRangeStart2 = count[x] + rocc(x, rsaRangeStart1 - 1);
                    rsaRangeEnd2 = count[x] + rocc(x, rsaRangeEnd1) - 1;
                    if (rsaRangeStart2 > rsaRangeEnd2) continue;
                    offset = offset + (rsaRangeEnd2 - rsaRangeStart2 + 1);
                }
                rsaRangeStart2 = count[j] + rocc(j, rsaRangeStart1 - 1);
                rsaRangeEnd2 = count[j] + rocc(j, rsaRangeEnd1) - 1;
                own = rsaRangeEnd2 - rsaRangeStart2 + 1;
                saRangeStart2 = saRangeStart1 + offset;
                saRangeEnd2 = saRangeStart1 + own - 1;
                if (saRangeStart2 > saRangeEnd2) continue;
                for (int k = rightMismatch + 1; k < readLength; k++) {
                    suffix = read[k];
                    offset = 0;
                    for (int x = 0; x < suffix; x++) {
                        rsaRangeStart3 = count[x] + rocc(x, rsaRangeStart2 - 1);
                        rsaRangeEnd3 = count[x] + rocc(x, rsaRangeEnd2) - 1;
                        if (rsaRangeStart3 > rsaRangeEnd3) continue;
                        offset = offset + (rsaRangeEnd3 - rsaRangeStart3 + 1);
                    }
                    rsaRangeStart2 = count[suffix] + rocc(suffix, rsaRangeStart2 - 1);
                    rsaRangeEnd2 = count[suffix] + rocc(suffix, rsaRangeEnd2) - 1;
                    own = rsaRangeEnd2 - rsaRangeStart2 + 1;
                    saRangeStart2 = saRangeStart2 + offset;
                    saRangeEnd2 = saRangeStart2 + own - 1;
                    if (saRangeStart2 > saRangeEnd2) break;
                }
                if (saRangeStart2 <= saRangeEnd2) {
                    matchRanges.add(saRangeStart2);
                    matchRanges.add(saRangeEnd2);
                }
            }
        }
    }

    /**
	 * combine all possibilities at given mismatch positions and add resulting ranges to locations
	 * rangeStart and rangeEnd represent the range so far.
	 * 
	 * @param read
	 * @param rightMismatch
	 * @param leftMismatch
	 * @param saRangeStart
	 * @param rangeEnd
	 * @param matchRanges
	 * @param isInCache
	 */
    protected void combineBidirectionalFB(byte[] read, int leftMismatch, int rightMismatch, long saRangeStart, long saRangeEnd, long rsaRangeStart, long rsaRangeEnd, LongArrayList matchRanges) {
        long saRangeStart1 = saRangeStart;
        long saRangeEnd1 = saRangeEnd;
        long rsaRangeStart1 = rsaRangeStart;
        long rsaRangeEnd1 = rsaRangeEnd;
        long saRangeStart2 = saRangeStart;
        long saRangeEnd2 = saRangeEnd;
        long rsaRangeStart2 = rsaRangeStart;
        long rsaRangeEnd2 = rsaRangeEnd;
        int prefix;
        int suffix;
        long offset;
        for (int i = 1; i <= 4; i++) {
            if (i == read[rightMismatch]) continue;
            offset = 0;
            for (int x = 0; x < i; x++) {
                rsaRangeStart1 = count[x] + rocc(x, rsaRangeStart - 1);
                rsaRangeEnd1 = count[x] + rocc(x, rsaRangeEnd) - 1;
                if (rsaRangeStart1 > rsaRangeEnd1) continue;
                offset = offset + (rsaRangeEnd1 - rsaRangeStart1 + 1);
            }
            rsaRangeStart1 = count[i] + rocc(i, rsaRangeStart - 1);
            rsaRangeEnd1 = count[i] + rocc(i, rsaRangeEnd) - 1;
            long own = rsaRangeEnd1 - rsaRangeStart1 + 1;
            saRangeStart1 = saRangeStart + offset;
            saRangeEnd1 = saRangeStart1 + own - 1;
            if (saRangeStart1 > saRangeEnd1) continue;
            for (int k = rightMismatch + 1; k < readLength; k++) {
                suffix = read[k];
                offset = 0;
                for (int x = 0; x < suffix; x++) {
                    rsaRangeStart2 = count[x] + rocc(x, rsaRangeStart1 - 1);
                    rsaRangeEnd2 = count[x] + rocc(x, rsaRangeEnd1) - 1;
                    if (rsaRangeStart2 > rsaRangeEnd2) continue;
                    offset = offset + (rsaRangeEnd2 - rsaRangeStart2 + 1);
                }
                rsaRangeStart1 = count[suffix] + rocc(suffix, rsaRangeStart1 - 1);
                rsaRangeEnd1 = count[suffix] + rocc(suffix, rsaRangeEnd1) - 1;
                own = rsaRangeEnd1 - rsaRangeStart1 + 1;
                saRangeStart1 = saRangeStart1 + offset;
                saRangeEnd1 = saRangeStart1 + own - 1;
                if (saRangeStart1 > saRangeEnd1) break;
            }
            if (saRangeStart1 > saRangeEnd1) continue;
            for (int j = 1; j <= 4; j++) {
                if (j == read[leftMismatch]) continue;
                saRangeStart2 = count[j] + occ(j, saRangeStart1 - 1);
                saRangeEnd2 = count[j] + occ(j, saRangeEnd1) - 1;
                if (saRangeStart2 > saRangeEnd2) continue;
                for (int k = leftMismatch - 1; k >= 0; k--) {
                    prefix = read[k];
                    saRangeStart2 = count[prefix] + occ(prefix, saRangeStart2 - 1);
                    saRangeEnd2 = count[prefix] + occ(prefix, saRangeEnd2) - 1;
                    if (saRangeStart2 > saRangeEnd2) break;
                }
                if (saRangeStart2 <= saRangeEnd2) {
                    matchRanges.add(saRangeStart2);
                    matchRanges.add(saRangeEnd2);
                }
            }
        }
    }

    /**
	 * combine all possibilities at given mismatch positions and add resulting ranges to locations
	 * rangeStart and rangeEnd represent the range so far.
	 * 
	 * @param read
	 * @param rightMismatch
	 * @param leftMismatch
	 * @param saRangeStart
	 * @param saRangeEnd
	 * @param matchRanges
	 * @param isInCache
	 */
    protected void combineForward(byte[] read, int leftMismatch, int rightMismatch, long saRangeStart, long saRangeEnd, long rsaRangeStart, long rsaRangeEnd, LongArrayList matchRanges) {
        long saRangeStart1 = saRangeStart;
        long saRangeEnd1 = saRangeEnd;
        long rsaRangeStart1 = rsaRangeStart;
        long rsaRangeEnd1 = rsaRangeEnd;
        long saRangeStart2 = saRangeStart;
        long saRangeEnd2 = saRangeEnd;
        long rsaRangeStart2 = rsaRangeStart;
        long rsaRangeEnd2 = rsaRangeEnd;
        long rsaRangeStart3 = rsaRangeStart;
        long rsaRangeEnd3 = rsaRangeEnd;
        int suffix;
        long offset;
        for (int i = 1; i <= 4; i++) {
            if (i == read[leftMismatch]) continue;
            offset = 0;
            for (int x = 0; x < i; x++) {
                rsaRangeStart1 = count[x] + rocc(x, rsaRangeStart - 1);
                rsaRangeEnd1 = count[x] + rocc(x, rsaRangeEnd) - 1;
                if (rsaRangeStart1 > rsaRangeEnd1) continue;
                offset = offset + (rsaRangeEnd1 - rsaRangeStart1 + 1);
            }
            rsaRangeStart1 = count[i] + rocc(i, rsaRangeStart - 1);
            rsaRangeEnd1 = count[i] + rocc(i, rsaRangeEnd) - 1;
            long own = rsaRangeEnd1 - rsaRangeStart1 + 1;
            saRangeStart1 = saRangeStart + offset;
            saRangeEnd1 = saRangeStart1 + own - 1;
            if (saRangeStart1 > saRangeEnd1) continue;
            for (int k = leftMismatch + 1; k < rightMismatch; k++) {
                suffix = read[k];
                offset = 0;
                for (int x = 0; x < suffix; x++) {
                    rsaRangeStart2 = count[x] + rocc(x, rsaRangeStart1 - 1);
                    rsaRangeEnd2 = count[x] + rocc(x, rsaRangeEnd1) - 1;
                    if (rsaRangeStart2 > rsaRangeEnd2) continue;
                    offset = offset + (rsaRangeEnd2 - rsaRangeStart2 + 1);
                }
                rsaRangeStart1 = count[suffix] + rocc(suffix, rsaRangeStart1 - 1);
                rsaRangeEnd1 = count[suffix] + rocc(suffix, rsaRangeEnd1) - 1;
                own = rsaRangeEnd1 - rsaRangeStart1 + 1;
                saRangeStart1 = saRangeStart1 + offset;
                saRangeEnd1 = saRangeStart1 + own - 1;
                if (saRangeStart1 > saRangeEnd1) break;
            }
            if (saRangeStart1 > saRangeEnd1) continue;
            for (int j = 1; j <= 4; j++) {
                if (j == read[rightMismatch]) continue;
                offset = 0;
                for (int x = 0; x < j; x++) {
                    rsaRangeStart2 = count[x] + rocc(x, rsaRangeStart1 - 1);
                    rsaRangeEnd2 = count[x] + rocc(x, rsaRangeEnd1) - 1;
                    if (rsaRangeStart2 > rsaRangeEnd2) continue;
                    offset = offset + (rsaRangeEnd2 - rsaRangeStart2 + 1);
                }
                rsaRangeStart2 = count[j] + rocc(j, rsaRangeStart1 - 1);
                rsaRangeEnd2 = count[j] + rocc(j, rsaRangeEnd1) - 1;
                own = rsaRangeEnd2 - rsaRangeStart2 + 1;
                saRangeStart2 = saRangeStart1 + offset;
                saRangeEnd2 = saRangeStart1 + own - 1;
                if (saRangeStart2 > saRangeEnd2) continue;
                for (int k = rightMismatch + 1; k < readLength; k++) {
                    suffix = read[k];
                    offset = 0;
                    for (int x = 0; x < suffix; x++) {
                        rsaRangeStart3 = count[x] + rocc(x, rsaRangeStart2 - 1);
                        rsaRangeEnd3 = count[x] + rocc(x, rsaRangeEnd2) - 1;
                        if (rsaRangeStart3 > rsaRangeEnd3) continue;
                        offset = offset + (rsaRangeEnd3 - rsaRangeStart3 + 1);
                    }
                    rsaRangeStart2 = count[suffix] + rocc(suffix, rsaRangeStart2 - 1);
                    rsaRangeEnd2 = count[suffix] + rocc(suffix, rsaRangeEnd2) - 1;
                    own = rsaRangeEnd2 - rsaRangeStart2 + 1;
                    saRangeStart2 = saRangeStart2 + offset;
                    saRangeEnd2 = saRangeStart2 + own - 1;
                    if (saRangeStart2 > saRangeEnd2) break;
                }
                if (saRangeStart2 <= saRangeEnd2) {
                    matchRanges.add(saRangeStart2);
                    matchRanges.add(saRangeEnd2);
                }
            }
        }
    }

    public void readMetadata(DataInput in) throws IOException {
        metadata = new GenomeMetadata();
        metadata.readFields(in);
    }

    public void readBWT(DataInput in) throws IOException {
        readMetadata(in);
        genomeLength = metadata.getGenomeSize();
        System.out.println(new Date() + " DEBUUUUUUUG: genome length is " + genomeLength);
        System.out.println(new Date() + " DEBUUUUUUUG: creating occ arrays.....");
        createOccArrays(occInterval);
        JobConf conf = new JobConf();
        BytesWritable bytes = new BytesWritable();
        try {
            bytes.readFields(in);
            bwt = getBitset(bytes);
            bytes = new BytesWritable();
            bytes.readFields(in);
            rbwt = getBitset(bytes);
            bytes = null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("problem reading back bwt and rbwt", e);
        }
        count = (long[]) ObjectWritable.readObject(in, conf);
        bwtNullPosition = in.readLong();
        rbwtNullPosition = in.readLong();
        genomeLength = in.readLong();
        populateOccArrays();
        System.gc();
        System.out.println(new Date() + " DEBUUUUUUUG: finished reading index");
        System.out.println(new Date() + " DEBUUUUUUUG: occ interval: " + this.occInterval);
        createMatchingPlan();
    }

    public void readSA(DataInput in) throws IOException {
        readMetadata(in);
        saInterval = in.readInt();
        System.out.println(new Date() + " DEBUUUUUUUG: SA interval is " + saInterval);
        JobConf conf = new JobConf();
        int[] saArrayIndexes = (int[]) ObjectWritable.readObject(in, conf);
        int[] saArrayValues = (int[]) ObjectWritable.readObject(in, conf);
        System.out.println(new Date() + " read " + saArrayIndexes.length + " values from SA array");
        populateSA(saArrayIndexes, saArrayValues);
    }

    public void populateSA(int[] saArrayIndexes, int[] saArrayValues) {
        System.out.println(new Date() + " creating SA array");
        sa = new HugeLongArray(genomeLength);
        for (long i = 0; i < sa.size; i++) {
            sa.set(AlignerConstants.INVALID, i);
        }
        System.out.println(new Date() + " populating full sa array");
        long position;
        long value;
        for (int i = 0; i < saArrayIndexes.length; i++) {
            position = edu.uga.dawgpack.index.util.Utils.toRealIndex(saArrayIndexes[i]);
            value = edu.uga.dawgpack.index.util.Utils.toRealIndex(saArrayValues[i]);
            sa.set(value, position);
        }
        final int n = 64;
        final long taskSize = sa.size / n;
        ExecutorService executor = Executors.newFixedThreadPool(n + 1);
        for (int i = 0; i < n + 1; i++) {
            final long start = taskSize * i;
            final long end = start + taskSize - 1;
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        System.out.println(new Date() + " DEBUUUUUUUG: calculating sa from " + start + " to " + end);
                        for (long j = start; j < sa.size && j <= end; j++) {
                            if (sa.getInt(j) == AlignerConstants.INVALID) {
                                calculateSA(j);
                            }
                        }
                    } catch (Throwable e) {
                        System.out.println("problem while populating");
                        e.printStackTrace();
                    }
                }
            });
        }
        try {
            executor.shutdown();
            executor.awaitTermination(3, TimeUnit.DAYS);
        } catch (Throwable e) {
            System.out.println(new Date() + " DEBUUUUUUUG: populateSA executor interrupted");
            e.printStackTrace();
        }
        executor = null;
        saArrayIndexes = null;
        saArrayValues = null;
        System.gc();
    }

    private void calculateSA(long tempIndex) {
        int character = bwt(tempIndex);
        long index = count[character] + occWithNullCheck(character, tempIndex) - 1;
        int valueInt = sa.getInt(index);
        if (valueInt == AlignerConstants.INVALID) {
            calculateSA(index);
        }
        long value = sa.get(index) + 1;
        if (value == genomeLength) sa.set(0, tempIndex); else sa.set(value, tempIndex);
    }

    private BytesWritable getBytesWritable(Sequence bitset) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(bitset);
        objectStream.flush();
        objectStream.close();
        byteStream.close();
        BytesWritable bytes = new BytesWritable(byteStream.toByteArray());
        return bytes;
    }

    private Sequence getBitset(BytesWritable bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes.get());
        ObjectInputStream obj = new ObjectInputStream(in);
        Sequence bitset = (Sequence) obj.readObject();
        obj.close();
        in.close();
        return bitset;
    }

    public void setChromosomeNumber(String chrNumbers) {
        chromosomeNumbers = chrNumbers;
    }

    public String getChromosomeNumbers() {
        return chromosomeNumbers;
    }

    public long getGenomeLength() {
        return genomeLength;
    }

    public HugeLongArray getSA() {
        return sa;
    }

    public GenomeMetadata getMetadata() {
        return metadata;
    }
}
