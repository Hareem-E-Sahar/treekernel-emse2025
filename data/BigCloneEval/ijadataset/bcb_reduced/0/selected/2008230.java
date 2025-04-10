package org.jcvi.common.core.seq.read.trace.sanger.chromat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jcvi.common.core.symbol.RunLengthEncodedGlyphCodec;
import org.jcvi.common.core.symbol.ShortSymbol;
import org.jcvi.common.core.symbol.pos.SangerPeak;
import org.jcvi.common.core.symbol.qual.EncodedQualitySequence;
import org.jcvi.common.core.symbol.qual.PhredQuality;
import org.jcvi.common.core.symbol.qual.QualitySequence;
import org.jcvi.common.core.symbol.residue.nt.Nucleotide;
import org.jcvi.common.core.symbol.residue.nt.NucleotideSequence;
import org.jcvi.common.core.symbol.residue.nt.NucleotideSequenceBuilder;

public final class BasicChromatogramBuilder {

    private static final RunLengthEncodedGlyphCodec RUN_LENGTH_CODEC = RunLengthEncodedGlyphCodec.DEFAULT_INSTANCE;

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};

    private short[] peaks;

    private NucleotideSequence basecalls;

    private byte[] aConfidence = EMPTY_BYTE_ARRAY;

    private byte[] cConfidence = EMPTY_BYTE_ARRAY;

    private byte[] gConfidence = EMPTY_BYTE_ARRAY;

    private byte[] tConfidence = EMPTY_BYTE_ARRAY;

    private short[] aPositions;

    private short[] cPositions;

    private short[] gPositions;

    private short[] tPositions;

    private Map<String, String> properties;

    private String id;

    /**
         * empty constructor.
         */
    public BasicChromatogramBuilder(String id) {
        if (id == null) {
            throw new NullPointerException("id can not be null");
        }
        this.id = id;
    }

    /**
         * Builds a builder starting with the following default values.
         * @param basecalls the basecalls may be null.
         * @param peaks the peaks cannot be null.
         * @param channelGroup the channel group containing
         *  position and confidence data on all 4 channels can not be null.
         * @param properties the properties may be null.
         */
    private BasicChromatogramBuilder(String id, NucleotideSequence basecalls, short[] peaks, ChannelGroup channelGroup, Map<String, String> properties) {
        id(id);
        basecalls(basecalls);
        peaks(peaks);
        aConfidence(channelGroup.getAChannel().getConfidence().getData());
        aPositions(channelGroup.getAChannel().getPositions().array());
        cConfidence(channelGroup.getCChannel().getConfidence().getData());
        cPositions(channelGroup.getCChannel().getPositions().array());
        gConfidence(channelGroup.getGChannel().getConfidence().getData());
        gPositions(channelGroup.getGChannel().getPositions().array());
        tConfidence(channelGroup.getTChannel().getConfidence().getData());
        tPositions(channelGroup.getTChannel().getPositions().array());
        properties(properties);
    }

    public BasicChromatogramBuilder(Chromatogram copy) {
        this(copy.getId(), copy.getNucleotideSequence(), ShortSymbol.toArray(copy.getPeaks().getData().asList()), copy.getChannelGroup(), copy.getComments());
    }

    public final short[] peaks() {
        return Arrays.copyOf(peaks, peaks.length);
    }

    public final BasicChromatogramBuilder peaks(short[] peaks) {
        this.peaks = Arrays.copyOf(peaks, peaks.length);
        return this;
    }

    public final String id() {
        return id;
    }

    public BasicChromatogramBuilder id(String id) {
        if (id == null) {
            throw new NullPointerException("id can not be null");
        }
        this.id = id;
        return this;
    }

    public final NucleotideSequence basecalls() {
        return basecalls;
    }

    public BasicChromatogramBuilder basecalls(NucleotideSequence basecalls) {
        this.basecalls = basecalls;
        return this;
    }

    public final byte[] aConfidence() {
        return Arrays.copyOf(aConfidence, aConfidence.length);
    }

    public final BasicChromatogramBuilder aConfidence(byte[] confidence) {
        aConfidence = Arrays.copyOf(confidence, confidence.length);
        return this;
    }

    public final byte[] cConfidence() {
        return Arrays.copyOf(cConfidence, cConfidence.length);
    }

    public final BasicChromatogramBuilder cConfidence(byte[] confidence) {
        cConfidence = Arrays.copyOf(confidence, confidence.length);
        return this;
    }

    public final byte[] gConfidence() {
        return Arrays.copyOf(gConfidence, gConfidence.length);
    }

    public final BasicChromatogramBuilder gConfidence(byte[] confidence) {
        gConfidence = Arrays.copyOf(confidence, confidence.length);
        return this;
    }

    public final byte[] tConfidence() {
        return Arrays.copyOf(tConfidence, tConfidence.length);
    }

    public final BasicChromatogramBuilder tConfidence(byte[] confidence) {
        tConfidence = Arrays.copyOf(confidence, confidence.length);
        return this;
    }

    public final short[] aPositions() {
        if (aPositions == null) {
            return new short[] {};
        }
        return Arrays.copyOf(aPositions, aPositions.length);
    }

    public final BasicChromatogramBuilder aPositions(short[] positions) {
        aPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final short[] cPositions() {
        if (cPositions == null) {
            return new short[] {};
        }
        return Arrays.copyOf(cPositions, cPositions.length);
    }

    public final BasicChromatogramBuilder cPositions(short[] positions) {
        cPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final short[] gPositions() {
        if (gPositions == null) {
            return new short[] {};
        }
        return Arrays.copyOf(gPositions, gPositions.length);
    }

    public final BasicChromatogramBuilder gPositions(short[] positions) {
        gPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final short[] tPositions() {
        if (tPositions == null) {
            return new short[] {};
        }
        return Arrays.copyOf(tPositions, tPositions.length);
    }

    public final BasicChromatogramBuilder tPositions(short[] positions) {
        tPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final Map<String, String> properties() {
        return properties == null ? null : new HashMap<String, String>(properties);
    }

    public final BasicChromatogramBuilder properties(Map<String, String> properties) {
        this.properties = new HashMap<String, String>();
        for (Entry<String, String> entry : properties.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    private QualitySequence generateQualities(ChannelGroup channelGroup) {
        int length = (int) basecalls.getLength();
        List<PhredQuality> qualities = new ArrayList<PhredQuality>(length);
        for (int i = 0; i < length; i++) {
            Nucleotide base = basecalls.get(i);
            final byte[] data = channelGroup.getChannel(base).getConfidence().getData();
            if (i == data.length) {
                break;
            }
            qualities.add(PhredQuality.valueOf(data[i]));
        }
        return new EncodedQualitySequence(RUN_LENGTH_CODEC, qualities);
    }

    public Chromatogram build() {
        final ChannelGroup channelGroup = new DefaultChannelGroup(new Channel(aConfidence(), aPositions()), new Channel(cConfidence(), cPositions()), new Channel(gConfidence(), gPositions()), new Channel(tConfidence(), tPositions()));
        return new BasicChromatogram(id, new NucleotideSequenceBuilder(basecalls()).build(), generateQualities(channelGroup), new SangerPeak(peaks()), channelGroup, properties());
    }
}
