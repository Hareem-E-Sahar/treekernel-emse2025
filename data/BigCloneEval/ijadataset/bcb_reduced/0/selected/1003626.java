package org.jcvi.trace.sanger.chromatogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jcvi.glyph.encoder.RunLengthEncodedGlyphCodec;
import org.jcvi.glyph.nuc.DefaultNucleotideEncodedGlyphs;
import org.jcvi.glyph.nuc.NucleotideGlyph;
import org.jcvi.glyph.phredQuality.DefaultQualityEncodedGlyphs;
import org.jcvi.glyph.phredQuality.PhredQuality;
import org.jcvi.glyph.phredQuality.QualityEncodedGlyphs;
import org.jcvi.sequence.Peaks;

public class BasicChromatogramBuilder {

    private static final RunLengthEncodedGlyphCodec RUN_LENGTH_CODEC = RunLengthEncodedGlyphCodec.DEFAULT_INSTANCE;

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};

    private short[] peaks;

    private String basecalls;

    private byte[] aConfidence = EMPTY_BYTE_ARRAY;

    private byte[] cConfidence = EMPTY_BYTE_ARRAY;

    private byte[] gConfidence = EMPTY_BYTE_ARRAY;

    private byte[] tConfidence = EMPTY_BYTE_ARRAY;

    private short[] aPositions;

    private short[] cPositions;

    private short[] gPositions;

    private short[] tPositions;

    private Map<String, String> properties;

    /**
         * empty constructor.
         */
    public BasicChromatogramBuilder() {
    }

    /**
         * Builds a builder starting with the following default values.
         * @param basecalls the basecalls may be null.
         * @param peaks the peaks cannot be null.
         * @param channelGroup the channel group containing
         *  position and confidence data on all 4 channels can not be null.
         * @param properties the properties may be null.
         */
    public BasicChromatogramBuilder(String basecalls, short[] peaks, ChannelGroup channelGroup, Map<String, String> properties) {
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

    public final short[] peaks() {
        return Arrays.copyOf(peaks, peaks.length);
    }

    public final BasicChromatogramBuilder peaks(short[] peaks) {
        this.peaks = Arrays.copyOf(peaks, peaks.length);
        return this;
    }

    public final String basecalls() {
        return basecalls;
    }

    public final BasicChromatogramBuilder basecalls(String basecalls) {
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
        return Arrays.copyOf(aPositions, aPositions.length);
    }

    public final BasicChromatogramBuilder aPositions(short[] positions) {
        aPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final short[] cPositions() {
        return Arrays.copyOf(cPositions, cPositions.length);
    }

    public final BasicChromatogramBuilder cPositions(short[] positions) {
        cPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final short[] gPositions() {
        return Arrays.copyOf(gPositions, gPositions.length);
    }

    public final BasicChromatogramBuilder gPositions(short[] positions) {
        gPositions = Arrays.copyOf(positions, positions.length);
        return this;
    }

    public final short[] tPositions() {
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
        this.properties = new HashMap<String, String>(properties);
        return this;
    }

    private QualityEncodedGlyphs generateQualities(ChannelGroup channelGroup) {
        List<PhredQuality> qualities = new ArrayList<PhredQuality>(basecalls.length());
        for (int i = 0; i < basecalls.length(); i++) {
            NucleotideGlyph base = NucleotideGlyph.getGlyphFor(basecalls.charAt(i));
            final byte[] data = channelGroup.getChannel(base).getConfidence().getData();
            if (i == data.length) {
                break;
            }
            qualities.add(PhredQuality.valueOf(data[i]));
        }
        return new DefaultQualityEncodedGlyphs(RUN_LENGTH_CODEC, qualities);
    }

    public Chromatogram build() {
        final ChannelGroup channelGroup = new DefaultChannelGroup(new Channel(aConfidence(), aPositions()), new Channel(cConfidence(), cPositions()), new Channel(gConfidence(), gPositions()), new Channel(tConfidence(), tPositions()));
        return new BasicChromatogram(new DefaultNucleotideEncodedGlyphs(basecalls()), generateQualities(channelGroup), new Peaks(peaks()), channelGroup, properties());
    }
}
