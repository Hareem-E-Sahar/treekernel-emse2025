package org.jcvi.common.core.seq.read.trace.sanger.chromat;

import java.util.Map;
import org.jcvi.common.core.symbol.pos.SangerPeak;
import org.jcvi.common.core.symbol.qual.QualitySequence;
import org.jcvi.common.core.symbol.residue.nt.NucleotideSequence;

/**
 * {@code BasicChromatogramFile} is a Chromatogram implementation
 * that is also a {@link ChromatogramFileVisitor}.  This chromatogram
 * object gets built by listening to the visit messages
 * it receives from a {@link Chromatogram} parser.
 * @author dkatzel
 *
 *
 */
public class BasicChromatogramFile implements Chromatogram, ChromatogramFileVisitor {

    private Chromatogram delegate;

    private BasicChromatogramBuilder builder;

    public BasicChromatogramFile(String id) {
        builder = new BasicChromatogramBuilder(id);
    }

    @Override
    public String getId() {
        return builder.id();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitFile() {
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitEndOfFile() {
        delegate = builder.build();
        builder = null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitBasecalls(NucleotideSequence basecalls) {
        builder.basecalls(basecalls);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitPeaks(short[] peaks) {
        builder.peaks(peaks);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitComments(Map<String, String> comments) {
        builder.properties(comments);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitAPositions(short[] positions) {
        builder.aPositions(positions);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitCPositions(short[] positions) {
        builder.cPositions(positions);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitGPositions(short[] positions) {
        builder.gPositions(positions);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitTPositions(short[] positions) {
        builder.tPositions(positions);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public ChannelGroup getChannelGroup() {
        return delegate.getChannelGroup();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Map<String, String> getComments() {
        return delegate.getComments();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public SangerPeak getPeaks() {
        return delegate.getPeaks();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public int getNumberOfTracePositions() {
        return delegate.getNumberOfTracePositions();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public NucleotideSequence getNucleotideSequence() {
        return delegate.getNucleotideSequence();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public QualitySequence getQualities() {
        return delegate.getQualities();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitAConfidence(byte[] confidence) {
        builder.aConfidence(confidence);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitCConfidence(byte[] confidence) {
        builder.cConfidence(confidence);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitGConfidence(byte[] confidence) {
        builder.gConfidence(confidence);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void visitTConfidence(byte[] confidence) {
        builder.tConfidence(confidence);
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
