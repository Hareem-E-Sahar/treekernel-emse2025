package org.jcvi.trace.sanger.chromatogram.scf;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.jcvi.glyph.nuc.NucleotideEncodedGlyphs;
import org.jcvi.glyph.phredQuality.QualityEncodedGlyphs;
import org.jcvi.sequence.Confidence;
import org.jcvi.sequence.Peaks;
import org.jcvi.trace.TraceDecoderException;
import org.jcvi.trace.sanger.chromatogram.ChannelGroup;

/**
 * @author dkatzel
 *
 *
 */
public class SCFChromatogramFile implements SCFChromatogram, SCFChromatogramFileVisitor {

    private SCFChromatogram delegate;

    private SCFChromatogramBuilder builder;

    public SCFChromatogramFile() {
        builder = new SCFChromatogramBuilder();
    }

    public SCFChromatogramFile(File scfFile) throws TraceDecoderException, IOException {
        this();
        SCFChromatogramFileParser.parseSCFFile(scfFile, this);
    }

    @Override
    public ChannelGroup getChannelGroup() {
        return delegate.getChannelGroup();
    }

    @Override
    public Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public NucleotideEncodedGlyphs getBasecalls() {
        return delegate.getBasecalls();
    }

    @Override
    public QualityEncodedGlyphs getQualities() {
        return delegate.getQualities();
    }

    @Override
    public void visitBasecalls(String basecalls) {
        builder.basecalls(basecalls);
    }

    @Override
    public void visitPeaks(short[] peaks) {
        builder.peaks(peaks);
    }

    @Override
    public void visitAPositions(short[] positions) {
        builder.aPositions(positions);
    }

    @Override
    public void visitCPositions(short[] positions) {
        builder.cPositions(positions);
    }

    @Override
    public void visitGPositions(short[] positions) {
        builder.gPositions(positions);
    }

    @Override
    public void visitTPositions(short[] positions) {
        builder.tPositions(positions);
    }

    @Override
    public void visitAConfidence(byte[] confidence) {
        builder.aConfidence(confidence);
    }

    @Override
    public void visitCConfidence(byte[] confidence) {
        builder.cConfidence(confidence);
    }

    @Override
    public void visitGConfidence(byte[] confidence) {
        builder.gConfidence(confidence);
    }

    @Override
    public void visitTConfidence(byte[] confidence) {
        builder.tConfidence(confidence);
    }

    @Override
    public void visitComments(Map<String, String> comments) {
        builder.properties(comments);
    }

    @Override
    public void visitFile() {
    }

    @Override
    public void visitEndOfFile() {
        delegate = builder.build();
        builder = null;
    }

    @Override
    public Peaks getPeaks() {
        return delegate.getPeaks();
    }

    @Override
    public int getNumberOfTracePositions() {
        return delegate.getNumberOfTracePositions();
    }

    @Override
    public void visitPrivateData(byte[] privateData) {
        builder.privateData(privateData);
    }

    @Override
    public void visitSubstitutionConfidence(byte[] confidence) {
        builder.substitutionConfidence(confidence);
    }

    @Override
    public void visitInsertionConfidence(byte[] confidence) {
        builder.insertionConfidence(confidence);
    }

    @Override
    public void visitDeletionConfidence(byte[] confidence) {
        builder.deletionConfidence(confidence);
    }

    @Override
    public PrivateData getPrivateData() {
        return delegate.getPrivateData();
    }

    @Override
    public Confidence getSubstitutionConfidence() {
        return delegate.getSubstitutionConfidence();
    }

    @Override
    public Confidence getInsertionConfidence() {
        return delegate.getInsertionConfidence();
    }

    @Override
    public Confidence getDeletionConfidence() {
        return delegate.getDeletionConfidence();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitNewTrace() {
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitEndOfTrace() {
    }
}
