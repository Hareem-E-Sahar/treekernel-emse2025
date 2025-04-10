package org.jcvi.common.core.seq.read.trace.sanger.phd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.jcvi.common.core.Range;
import org.jcvi.common.core.datastore.DataStoreException;
import org.jcvi.common.core.datastore.DataStoreFilter;
import org.jcvi.common.core.io.IOUtil;
import org.jcvi.common.core.symbol.ShortSymbol;
import org.jcvi.common.core.symbol.qual.PhredQuality;
import org.jcvi.common.core.symbol.residue.nt.Nucleotide;
import org.jcvi.common.core.util.ByteBufferInputStream;
import org.jcvi.common.core.util.DefaultIndexedFileRange;
import org.jcvi.common.core.util.IndexedFileRange;
import org.jcvi.common.core.util.iter.CloseableIterator;

/**
 * {@code IndexedPhdFileDataStore} is an implementation of 
 * {@link PhdDataStore} that only stores an index containing
 * file offsets to the various phd records contained
 * inside the phdball file.  This allows large files to provide random 
 * access without taking up much memory.  The downside is every time {@link #get(String)}
 * is called, the phd file must be re-read to seek to the appropriate
 * offset in order to re-parse the portion of the file that pertains
 * to that one particular phd record.
 * @author dkatzel
 *
 */
public final class IndexedPhdFileDataStore implements PhdDataStore {

    private final IndexedFileRange recordLocations;

    private final File phdBall;

    /**
     * Create a new {@link PhdDataStoreBuilder} for the given
     * {@literal phd.ball} file.  The returned builder
     * can only parse one phd ball file.  If a second
     * phd file is parsed (or the same file parsed twice)
     * then the implementation will throw a IllegalStateException.
     * @param phdBall the {@literal phd.ball} to parse.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall is null.
     */
    public static PhdDataStoreBuilder createBuilder(File phdBall) {
        return new IndexedPhdDataStoreBuilder(phdBall);
    }

    /**
     * Create a new {@link PhdDataStoreBuilder} for the given
     * {@literal phd.ball} file.  The returned builder
     * can only parse one phd ball file.  If a second
     * phd file is parsed (or the same file parsed twice)
     * then the implementation will throw a IllegalStateException.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param initialCapacity the initial capacity of the index used
     * to store/lookup file offsets into the phd file.  If the initialCapacity
     * is larger than the number of phd records parsed, then there will be no
     * performance hit to resize the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall is null.
     * @throws IllegalArgumentException if {@code initialCapacity < 0}
     */
    public static PhdDataStoreBuilder createBuilder(File phdBall, int initialCapacity) {
        return new IndexedPhdDataStoreBuilder(phdBall, initialCapacity);
    }

    /**
     * Create a new {@link PhdDataStoreBuilder} for the given
     * {@literal phd.ball} file.  The returned builder
     * can only parse one phd ball file.  If a second
     * phd file is parsed (or the same file parsed twice)
     * then the implementation will throw a IllegalStateException.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param filter the {@link DataStoreFilter} to use to filter
     * which reads get stored in the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall or filter are null.
     */
    public static PhdDataStoreBuilder createBuilder(File phdBall, DataStoreFilter filter) {
        return new IndexedPhdDataStoreBuilder(phdBall, filter);
    }

    /**
     * Create a new {@link PhdDataStoreBuilder} for the given
     * {@literal phd.ball} file.  The returned builder
     * can only parse one phd ball file.  If a second
     * phd file is parsed (or the same file parsed twice)
     * then the implementation will throw a IllegalStateException.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param filter the {@link DataStoreFilter} to use to filter
     * which reads get stored in the index.
     * @param initialCapacity the initial capacity of the index used
     * to store/lookup file offsets into the phd file.  If the initialCapacity
     * is larger than the number of phd records parsed, then there will be no
     * performance hit to resize the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall or filter are null.
     * @throws IllegalArgumentException if {@code initialCapacity < 0}
     */
    public static PhdDataStoreBuilder createBuilder(File phdBall, DataStoreFilter filter, int initialCapacity) {
        return new IndexedPhdDataStoreBuilder(phdBall, filter, initialCapacity);
    }

    /**
     * Create a new {@link PhdDataStore} for the given
     * {@literal phd.ball} file.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param initialCapacity the initial capacity of the index used
     * to store/lookup file offsets into the phd file.  If the initialCapacity
     * is larger than the number of phd records parsed, then there will be no
     * performance hit to resize the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall is null.
     */
    public static PhdDataStore create(File phdBall) throws FileNotFoundException {
        PhdDataStoreBuilder builder = createBuilder(phdBall);
        PhdParser.parsePhd(phdBall, builder);
        return builder.build();
    }

    /**
     * Create a new {@link PhdDataStore} for the given
     * {@literal phd.ball} file.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param initialCapacity the initial capacity of the index used
     * to store/lookup file offsets into the phd file.  If the initialCapacity
     * is larger than the number of phd records parsed, then there will be no
     * performance hit to resize the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall is null.
     * @throws IllegalArgumentException if {@code initialCapacity < 0}
     */
    public static PhdDataStore create(File phdBall, int initialCapacity) throws FileNotFoundException {
        PhdDataStoreBuilder builder = createBuilder(phdBall, initialCapacity);
        PhdParser.parsePhd(phdBall, builder);
        return builder.build();
    }

    /**
     * Create a new {@link PhdDataStore} for the given
     * {@literal phd.ball} file.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param filter the {@link DataStoreFilter} to use to filter
     * which reads get stored in the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall or filter are null.
     */
    public static PhdDataStore create(File phdBall, DataStoreFilter filter) throws FileNotFoundException {
        PhdDataStoreBuilder builder = createBuilder(phdBall, filter);
        PhdParser.parsePhd(phdBall, builder);
        return builder.build();
    }

    /**
     * Create a new {@link PhdDataStore} for the given
     * {@literal phd.ball} file.
     * @param phdBall the {@literal phd.ball} to parse.
     * @param filter the {@link DataStoreFilter} to use to filter
     * which reads get stored in the index.
     * @param initialCapacity the initial capacity of the index used
     * to store/lookup file offsets into the phd file.  If the initialCapacity
     * is larger than the number of phd records parsed, then there will be no
     * performance hit to resize the index.
     * @return a new IndexedPhdFileDataStore; never null.
     * @throws FileNotFoundException if the given file
     * does not exist.
     * @throws NullPointerException if phdBall or filter are null.
     * @throws IllegalArgumentException if {@code initialCapacity < 0}
     */
    public static PhdDataStore create(File phdBall, DataStoreFilter filter, int initialCapacity) throws FileNotFoundException {
        PhdDataStoreBuilder builder = createBuilder(phdBall, filter, initialCapacity);
        PhdParser.parsePhd(phdBall, builder);
        return builder.build();
    }

    private IndexedPhdFileDataStore(File phdBall, IndexedFileRange recordLocations) {
        this.recordLocations = recordLocations;
        this.phdBall = phdBall;
    }

    private static final class IndexedPhdDataStoreBuilder extends AbstractPhdDataStoreBuilder {

        private final IndexedFileRange recordLocations;

        private long currentStartOffset = 0;

        private long currentOffset = currentStartOffset;

        private final File phdBall;

        private int currentLineLength;

        private boolean firstPhd = true;

        private IndexedPhdDataStoreBuilder(File phdBall) {
            super();
            this.phdBall = phdBall;
            this.recordLocations = new DefaultIndexedFileRange();
        }

        private IndexedPhdDataStoreBuilder(File phdBall, int initialSizeOfIndex) {
            super();
            if (initialSizeOfIndex < 1) {
                throw new IllegalArgumentException("intialSize can not be null");
            }
            this.phdBall = phdBall;
            this.recordLocations = new DefaultIndexedFileRange(initialSizeOfIndex);
        }

        private IndexedPhdDataStoreBuilder(File phdBall, DataStoreFilter filter) {
            super(filter);
            this.phdBall = phdBall;
            this.recordLocations = new DefaultIndexedFileRange();
        }

        private IndexedPhdDataStoreBuilder(File phdBall, DataStoreFilter filter, int initialSizeOfIndex) {
            super(filter);
            this.phdBall = phdBall;
            this.recordLocations = new DefaultIndexedFileRange(initialSizeOfIndex);
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public synchronized void visitFile() {
            if (!firstPhd) {
                throw new IllegalStateException("can only read 1 phd (or phd.ball) file");
            }
            firstPhd = false;
            super.visitFile();
        }

        @Override
        public synchronized void visitLine(String line) {
            super.visitLine(line);
            currentLineLength = line.length();
            currentOffset += currentLineLength;
        }

        @Override
        protected synchronized boolean visitPhd(String id, List<Nucleotide> bases, List<PhredQuality> qualities, List<ShortSymbol> positions, Properties comments, List<PhdTag> tags) {
            long endOfOldRecord = currentOffset - currentLineLength - 1;
            recordLocations.put(id, Range.create(currentStartOffset, endOfOldRecord));
            currentStartOffset = endOfOldRecord;
            return true;
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public PhdDataStore build() {
            return new IndexedPhdFileDataStore(phdBall, recordLocations);
        }
    }

    @Override
    public boolean contains(String id) throws DataStoreException {
        return recordLocations.contains(id);
    }

    @Override
    public Phd get(String id) throws DataStoreException {
        FileChannel fastaFileChannel = null;
        PhdDataStore dataStore = null;
        InputStream in = null;
        FileInputStream fileInputStream = null;
        try {
            if (!recordLocations.contains(id)) {
                throw new DataStoreException(id + " does not exist");
            }
            Range range = recordLocations.getRangeFor(id);
            fileInputStream = new FileInputStream(phdBall);
            MappedByteBuffer buf = fileInputStream.getChannel().map(FileChannel.MapMode.READ_ONLY, range.getBegin(), range.getLength());
            in = new ByteBufferInputStream(buf);
            PhdDataStoreBuilder builder = DefaultPhdFileDataStore.createBuilder();
            PhdParser.parsePhd(in, builder);
            dataStore = builder.build();
            return dataStore.get(id);
        } catch (IOException e) {
            throw new DataStoreException("error getting " + id, e);
        } finally {
            IOUtil.closeAndIgnoreErrors(fastaFileChannel);
            IOUtil.closeAndIgnoreErrors(dataStore);
            IOUtil.closeAndIgnoreErrors(in);
            IOUtil.closeAndIgnoreErrors(fileInputStream);
        }
    }

    @Override
    public CloseableIterator<String> idIterator() throws DataStoreException {
        return recordLocations.getIds();
    }

    @Override
    public long getNumberOfRecords() throws DataStoreException {
        return recordLocations.size();
    }

    @Override
    public synchronized void close() throws IOException {
        recordLocations.close();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean isClosed() throws DataStoreException {
        return recordLocations.isClosed();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public CloseableIterator<Phd> iterator() {
        return new IndexedIterator();
    }

    /**
     * Wrapper around {@link LargePhdIterator} to filter
     * out any phds that we don't have in our
     * index.
     * @author dkatzel
     */
    private class IndexedIterator implements CloseableIterator<Phd> {

        private LargePhdIterator iterator = LargePhdIterator.createNewIterator(phdBall);

        private final Object endOfIterator = new Object();

        private Object next;

        public IndexedIterator() {
            updateNext();
        }

        private void updateNext() {
            Object newNext = endOfIterator;
            while (iterator.hasNext() && newNext == endOfIterator) {
                Phd nextCandidate = iterator.next();
                if (recordLocations.contains(nextCandidate.getId())) {
                    newNext = nextCandidate;
                }
            }
            next = newNext;
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public synchronized boolean hasNext() {
            return next != endOfIterator;
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public synchronized void close() throws IOException {
            iterator.close();
        }

        /**
        * {@inheritDoc}
        */
        @Override
        public synchronized Phd next() {
            if (!hasNext()) {
                throw new NoSuchElementException("no more elements in iterator");
            }
            Phd ret = (Phd) next;
            updateNext();
            return ret;
        }
    }
}
