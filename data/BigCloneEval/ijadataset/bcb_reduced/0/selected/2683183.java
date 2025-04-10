package org.jcvi.common.core.symbol.residue.nt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jcvi.common.core.Range;
import org.jcvi.common.core.io.IOUtil;
import org.jcvi.common.core.io.ValueSizeStrategy;
import org.jcvi.common.core.symbol.Sequence;
import org.jcvi.common.core.util.iter.ArrayIterator;

final class DefaultReferenceEncodedNucleotideSequence extends AbstractNucleotideSequence implements ReferenceEncodedNucleotideSequence {

    private static final int BITS_PER_SNP_VALUE = 4;

    private final int length;

    private final int startOffset;

    private final NucleotideSequence reference;

    /**
     * All of the differences between this 
     * read and the reference it is aligned 
     * to (which could be a contig consensus)
     * are encoded here. If there are no SNPs in this read:
     * meaning that this read aligns to its reference at 100%
     * identity, then this value <strong> will be null</strong>.
     * <br/>
     * The encoding uses {@link ValueSizeStrategy}
     * to pack the data in as few bytes as possible.
     * Here is the current encoding:<br/>
     * <ul>
     *  <li>first byte is the ordinal value of the {@link ValueSizeStrategy}
     *   used to store the number of elements</li>
     *  
     *  <li>the next 1,2 or 4 bytes (depending on the {@link ValueSizeStrategy}
     *  specified in the previous byte) denote the number of SNPs encoded.</li>
     *  
     *  <li>the next byte is the ordinal value of the {@link ValueSizeStrategy}
     *   used to store the number of bytes
     *   required for each SNP offset value</li>
     *  
     *  <li>the next $num_snps * 1,2 or 4 bytes (depending on the {@link ValueSizeStrategy}
     *  specified in the previous byte) encode the SNP offsets in the read.</li>
     *  
     *  </li> the remaining bytes store the actual SNP values as Nucleotide ordinal values
     *  packed as 4 bits each.  This means that each byte actually stores
     *  2 SNPs.</li>
     *  <ul/>
     */
    private final byte[] encodedSnpsInfo;

    @Override
    public Map<Integer, Nucleotide> getDifferenceMap() {
        if (encodedSnpsInfo == null) {
            return Collections.emptyMap();
        }
        ByteBuffer buf = ByteBuffer.wrap(encodedSnpsInfo);
        ValueSizeStrategy numSnpsSizeStrategy = ValueSizeStrategy.values()[buf.get()];
        int size = numSnpsSizeStrategy.getNext(buf);
        ValueSizeStrategy snpSizeStrategy = ValueSizeStrategy.values()[buf.get()];
        BitSet bits = getSnpBitSet(numSnpsSizeStrategy, size, snpSizeStrategy);
        Map<Integer, Nucleotide> differenceMap = new HashMap<Integer, Nucleotide>();
        for (int i = 0; i < size; i++) {
            Integer offset = snpSizeStrategy.getNext(buf);
            differenceMap.put(offset, getSnpValueFrom(bits, i));
        }
        return Collections.unmodifiableMap(differenceMap);
    }

    public DefaultReferenceEncodedNucleotideSequence(NucleotideSequence reference, String toBeEncoded, int startOffset) {
        List<Integer> tempGapList = new ArrayList<Integer>();
        this.startOffset = startOffset;
        this.length = toBeEncoded.length();
        this.reference = reference;
        TreeMap<Integer, Nucleotide> differentGlyphMap = populateFields(reference, toBeEncoded, startOffset, tempGapList);
        int numSnps = differentGlyphMap.size();
        if (numSnps > 0) {
            ValueSizeStrategy snpSizeStrategy = ValueSizeStrategy.getStrategyFor(differentGlyphMap.lastKey().intValue());
            int length = computeNumberOfBytesToStore(numSnps, snpSizeStrategy);
            ValueSizeStrategy numSnpsStrategy = ValueSizeStrategy.getStrategyFor(length);
            int bufferSize = numSnpsStrategy.getNumberOfBytesPerValue() + length;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            buffer.put((byte) numSnpsStrategy.ordinal());
            numSnpsStrategy.put(buffer, numSnps);
            buffer.put((byte) snpSizeStrategy.ordinal());
            try {
                for (Integer offset : differentGlyphMap.keySet()) {
                    snpSizeStrategy.put(buffer, offset.intValue());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int nbits = BITS_PER_SNP_VALUE * numSnps;
            BitSet bits = new BitSet(nbits);
            int i = 0;
            for (Nucleotide n : differentGlyphMap.values()) {
                byte ordinal = n.getOrdinalAsByte();
                BitSet temp = IOUtil.toBitSet(ordinal);
                for (int j = 0; j < BITS_PER_SNP_VALUE; j++) {
                    if (temp.get(j)) {
                        bits.set(i + j);
                    }
                }
                i += BITS_PER_SNP_VALUE;
            }
            byte[] byteArray = IOUtil.toByteArray(bits, nbits);
            try {
                buffer.put(byteArray);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            encodedSnpsInfo = buffer.array();
        } else {
            encodedSnpsInfo = null;
        }
    }

    private int computeNumberOfBytesToStore(int numSnps, ValueSizeStrategy snpSizeStrategy) {
        int numBytesPerSnpIndex = snpSizeStrategy.getNumberOfBytesPerValue();
        int numBytesRequiredToStoreSnps = (numSnps + 1) / 2;
        int numberOfBytesToStoreSnpOffsets = numBytesPerSnpIndex * numSnps;
        return 2 + numberOfBytesToStoreSnpOffsets + numBytesRequiredToStoreSnps;
    }

    private TreeMap<Integer, Nucleotide> populateFields(Sequence<Nucleotide> reference, String toBeEncoded, int startOffset, List<Integer> tempGapList) {
        handleBeforeReference(startOffset);
        handleAfterReference(reference, toBeEncoded, startOffset);
        TreeMap<Integer, Nucleotide> differentGlyphMap = new TreeMap<Integer, Nucleotide>();
        int startReferenceEncodingOffset = 0;
        int endReferenceEncodingOffset = toBeEncoded.length();
        for (int i = startReferenceEncodingOffset; i < endReferenceEncodingOffset; i++) {
            int referenceIndex = i + startOffset;
            Nucleotide g = Nucleotide.parse(toBeEncoded.charAt(i));
            final Nucleotide referenceGlyph = reference.get(referenceIndex);
            final Integer indexAsInteger = Integer.valueOf(i);
            if (g.isGap()) {
                tempGapList.add(indexAsInteger);
            }
            if (isDifferent(g, referenceGlyph)) {
                differentGlyphMap.put(indexAsInteger, g);
            }
        }
        return differentGlyphMap;
    }

    private void handleAfterReference(Sequence<Nucleotide> reference, String toBeEncoded, int startOffset) {
        int lastOffsetOfSequence = toBeEncoded.length() + startOffset;
        if (lastOffsetOfSequence > reference.getLength()) {
            int overhang = (int) (toBeEncoded.length() + startOffset - reference.getLength());
            throw new IllegalArgumentException(String.format("sequences extends beyond reference by %d bases", overhang));
        }
    }

    private void handleBeforeReference(int startOffset) {
        if (startOffset < 0) {
            throw new IllegalArgumentException("can not start before reference: " + startOffset);
        }
    }

    private boolean isDifferent(Nucleotide g, final Nucleotide referenceGlyph) {
        return g != referenceGlyph;
    }

    @Override
    public Iterator<Nucleotide> iterator() {
        Nucleotide[] array = asNucleotideArray();
        return new ArrayIterator<Nucleotide>(array);
    }

    @Override
    public List<Nucleotide> asList() {
        Nucleotide[] array = asNucleotideArray();
        return Arrays.asList(array);
    }

    private Nucleotide[] asNucleotideArray() {
        Nucleotide[] array = reference.asList(Range.createOfLength(startOffset, length)).toArray(new Nucleotide[0]);
        if (encodedSnpsInfo != null) {
            ByteBuffer buf = ByteBuffer.wrap(encodedSnpsInfo);
            ValueSizeStrategy numSnpsSizeStrategy = ValueSizeStrategy.values()[buf.get()];
            int size = numSnpsSizeStrategy.getNext(buf);
            ValueSizeStrategy sizeStrategy = ValueSizeStrategy.values()[buf.get()];
            BitSet bits = getSnpBitSet(numSnpsSizeStrategy, size, sizeStrategy);
            for (int i = 0; i < size; i++) {
                int index = sizeStrategy.getNext(buf);
                array[index] = getSnpValueFrom(bits, i);
            }
        }
        return array;
    }

    @Override
    public Nucleotide get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("invalid offset " + index);
        }
        if (encodedSnpsInfo != null) {
            ByteBuffer buf = ByteBuffer.wrap(encodedSnpsInfo);
            ValueSizeStrategy numSnpsSizeStrategy = ValueSizeStrategy.values()[buf.get()];
            int size = numSnpsSizeStrategy.getNext(buf);
            ValueSizeStrategy sizeStrategy = ValueSizeStrategy.values()[buf.get()];
            BitSet bits = getSnpBitSet(numSnpsSizeStrategy, size, sizeStrategy);
            for (int i = 0; i < size; i++) {
                int nextValue = sizeStrategy.getNext(buf);
                if (index == nextValue) {
                    return getSnpValueFrom(bits, i);
                }
            }
        }
        int referenceIndex = index + startOffset;
        return reference.get(referenceIndex);
    }

    private BitSet getSnpBitSet(ValueSizeStrategy numSnpsSizeStrategy, int size, ValueSizeStrategy sizeStrategy) {
        int from = numSnpsSizeStrategy.getNumberOfBytesPerValue() + 2 + size * sizeStrategy.getNumberOfBytesPerValue();
        byte[] snpSubArray = Arrays.copyOfRange(encodedSnpsInfo, from, encodedSnpsInfo.length);
        BitSet bits = IOUtil.toBitSet(snpSubArray);
        return bits;
    }

    private Nucleotide getSnpValueFrom(BitSet bits, int offset) {
        int i = offset * BITS_PER_SNP_VALUE;
        byte[] byteArray = IOUtil.toByteArray(bits.get(i, i + BITS_PER_SNP_VALUE), 8);
        final int ordinal = new BigInteger(byteArray).intValue();
        return Nucleotide.values()[ordinal];
    }

    @Override
    public boolean isGap(int index) {
        return getGapOffsets().contains(Integer.valueOf(index));
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public List<Integer> getGapOffsets() {
        List<Integer> referenceGapOffsets = shiftReferenceGaps();
        if (encodedSnpsInfo != null) {
            return modifyForSnps(referenceGapOffsets);
        }
        return referenceGapOffsets;
    }

    private List<Integer> modifyForSnps(List<Integer> gaps) {
        ByteBuffer buf = ByteBuffer.wrap(encodedSnpsInfo);
        int size = ValueSizeStrategy.values()[buf.get()].getNext(buf);
        ValueSizeStrategy sizeStrategy = ValueSizeStrategy.values()[buf.get()];
        List<Integer> snps = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            Integer snpOffset = sizeStrategy.getNext(buf);
            if (gaps.contains(snpOffset)) {
                gaps.remove(snpOffset);
            }
            snps.add(snpOffset);
        }
        if (buf.hasRemaining()) {
            int numBytesRemaining = buf.remaining();
            byte[] snpSubArray = Arrays.copyOfRange(encodedSnpsInfo, encodedSnpsInfo.length - numBytesRemaining, encodedSnpsInfo.length);
            BitSet bits = IOUtil.toBitSet(snpSubArray);
            for (int i = 0; i < size; i++) {
                if (Nucleotide.Gap == getSnpValueFrom(bits, i)) {
                    gaps.add(snps.get(i));
                }
            }
        }
        Collections.sort(gaps);
        return gaps;
    }

    /**
	 * Most reference gaps should also
	 * be present in our gapped sequence
	 * so we need to get the reference gaps
	 * that overlap with our sequence range
	 * and shift them accordingly so read coordinate space.
	 * (offset 0 is first base in our read)
	 * @return
	 */
    private List<Integer> shiftReferenceGaps() {
        List<Integer> refGapOffsets = reference.getGapOffsets();
        List<Integer> gaps = new ArrayList<Integer>(refGapOffsets.size());
        for (Integer refGap : refGapOffsets) {
            int adjustedCoordinate = refGap.intValue() - startOffset;
            if (adjustedCoordinate >= 0 && adjustedCoordinate < length) {
                gaps.add(Integer.valueOf(adjustedCoordinate));
            }
        }
        return gaps;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + toString().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NucleotideSequence)) {
            return false;
        }
        NucleotideSequence other = (NucleotideSequence) obj;
        return toString().equals(other.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfGaps() {
        return getGapOffsets().size();
    }

    @Override
    public String toString() {
        return Nucleotides.asString(asList());
    }
}
