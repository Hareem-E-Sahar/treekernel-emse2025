package org.apache.lucene.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.ArrayUtil;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.text.NumberFormat;

/**
 * This class accepts multiple added documents and directly
 * writes a single segment file.  It does this more
 * efficiently than creating a single segment per document
 * (with DocumentWriter) and doing standard merges on those
 * segments.
 *
 * Each added document is passed to the {@link DocConsumer},
 * which in turn processes the document and interacts with
 * other consumers in the indexing chain.  Certain
 * consumers, like {@link StoredFieldsWriter} and {@link
 * TermVectorsTermsWriter}, digest a document and
 * immediately write bytes to the "doc store" files (ie,
 * they do not consume RAM per document, except while they
 * are processing the document).
 *
 * Other consumers, eg {@link FreqProxTermsWriter} and
 * {@link NormsWriter}, buffer bytes in RAM and flush only
 * when a new segment is produced.

 * Once we have used our allowed RAM buffer, or the number
 * of added docs is large enough (in the case we are
 * flushing by doc count instead of RAM usage), we create a
 * real segment and flush it to the Directory.
 *
 * Threads:
 *
 * Multiple threads are allowed into addDocument at once.
 * There is an initial synchronized call to getThreadState
 * which allocates a ThreadState for this thread.  The same
 * thread will get the same ThreadState over time (thread
 * affinity) so that if there are consistent patterns (for
 * example each thread is indexing a different content
 * source) then we make better use of RAM.  Then
 * processDocument is called on that ThreadState without
 * synchronization (most of the "heavy lifting" is in this
 * call).  Finally the synchronized "finishDocument" is
 * called to flush changes to the directory.
 *
 * When flush is called by IndexWriter, or, we flush
 * internally when autoCommit=false, we forcefully idle all
 * threads and flush only once they are all idle.  This
 * means you can call flush with a given thread even while
 * other threads are actively adding/deleting documents.
 *
 *
 * Exceptions:
 *
 * Because this class directly updates in-memory posting
 * lists, and flushes stored fields and term vectors
 * directly to files in the directory, there are certain
 * limited times when an exception can corrupt this state.
 * For example, a disk full while flushing stored fields
 * leaves this file in a corrupt state.  Or, an OOM
 * exception while appending to the in-memory posting lists
 * can corrupt that posting list.  We call such exceptions
 * "aborting exceptions".  In these cases we must call
 * abort() to discard all docs added since the last flush.
 *
 * All other exceptions ("non-aborting exceptions") can
 * still partially update the index structures.  These
 * updates are consistent, but, they represent only a part
 * of the document seen up until the exception was hit.
 * When this happens, we immediately mark the document as
 * deleted so that the document is always atomically ("all
 * or none") added to the index.
 */
final class DocumentsWriter {

    IndexWriter writer;

    Directory directory;

    String segment;

    private String docStoreSegment;

    private int docStoreOffset;

    private int nextDocID;

    private int numDocsInRAM;

    int numDocsInStore;

    private static final int MAX_THREAD_STATE = 5;

    private DocumentsWriterThreadState[] threadStates = new DocumentsWriterThreadState[0];

    private final HashMap threadBindings = new HashMap();

    private int pauseThreads;

    boolean flushPending;

    boolean bufferIsFull;

    private boolean aborting;

    private DocFieldProcessor docFieldProcessor;

    PrintStream infoStream;

    int maxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

    Similarity similarity;

    List newFiles;

    static class DocState {

        DocumentsWriter docWriter;

        Analyzer analyzer;

        int maxFieldLength;

        PrintStream infoStream;

        Similarity similarity;

        int docID;

        Document doc;

        String maxTermPrefix;

        public boolean testPoint(String name) {
            return docWriter.writer.testPoint(name);
        }
    }

    static class FlushState {

        DocumentsWriter docWriter;

        Directory directory;

        String segmentName;

        String docStoreSegmentName;

        int numDocsInRAM;

        int numDocsInStore;

        Collection flushedFiles;

        public String segmentFileName(String ext) {
            return segmentName + "." + ext;
        }
    }

    /** Consumer returns this on each doc.  This holds any
   *  state that must be flushed synchronized "in docID
   *  order".  We gather these and flush them in order. */
    abstract static class DocWriter {

        DocWriter next;

        int docID;

        abstract void finish() throws IOException;

        abstract void abort();

        abstract long sizeInBytes();

        void setNext(DocWriter next) {
            this.next = next;
        }
    }

    ;

    final DocConsumer consumer;

    private BufferedDeletes deletesInRAM = new BufferedDeletes();

    private BufferedDeletes deletesFlushed = new BufferedDeletes();

    private int maxBufferedDeleteTerms = IndexWriter.DEFAULT_MAX_BUFFERED_DELETE_TERMS;

    private long ramBufferSize = (long) (IndexWriter.DEFAULT_RAM_BUFFER_SIZE_MB * 1024 * 1024);

    private long waitQueuePauseBytes = (long) (ramBufferSize * 0.1);

    private long waitQueueResumeBytes = (long) (ramBufferSize * 0.05);

    private long freeTrigger = (long) (IndexWriter.DEFAULT_RAM_BUFFER_SIZE_MB * 1024 * 1024 * 1.05);

    private long freeLevel = (long) (IndexWriter.DEFAULT_RAM_BUFFER_SIZE_MB * 1024 * 1024 * 0.95);

    private int maxBufferedDocs = IndexWriter.DEFAULT_MAX_BUFFERED_DOCS;

    private int flushedDocCount;

    synchronized void updateFlushedDocCount(int n) {
        flushedDocCount += n;
    }

    synchronized int getFlushedDocCount() {
        return flushedDocCount;
    }

    synchronized void setFlushedDocCount(int n) {
        flushedDocCount = n;
    }

    private boolean closed;

    DocumentsWriter(Directory directory, IndexWriter writer) throws IOException {
        this.directory = directory;
        this.writer = writer;
        this.similarity = writer.getSimilarity();
        flushedDocCount = writer.maxDoc();
        final TermsHashConsumer termVectorsWriter = new TermVectorsTermsWriter(this);
        final TermsHashConsumer freqProxWriter = new FreqProxTermsWriter();
        final InvertedDocConsumer termsHash = new TermsHash(this, true, freqProxWriter, new TermsHash(this, false, termVectorsWriter, null));
        final NormsWriter normsWriter = new NormsWriter();
        final DocInverter docInverter = new DocInverter(termsHash, normsWriter);
        final StoredFieldsWriter fieldsWriter = new StoredFieldsWriter(this);
        final DocFieldConsumers docFieldConsumers = new DocFieldConsumers(docInverter, fieldsWriter);
        consumer = docFieldProcessor = new DocFieldProcessor(this, docFieldConsumers);
    }

    /** Returns true if any of the fields in the current
   *  buffered docs have omitTf==false */
    boolean hasProx() {
        return docFieldProcessor.fieldInfos.hasProx();
    }

    /** If non-null, various details of indexing are printed
   *  here. */
    synchronized void setInfoStream(PrintStream infoStream) {
        this.infoStream = infoStream;
        for (int i = 0; i < threadStates.length; i++) threadStates[i].docState.infoStream = infoStream;
    }

    synchronized void setMaxFieldLength(int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
        for (int i = 0; i < threadStates.length; i++) threadStates[i].docState.maxFieldLength = maxFieldLength;
    }

    synchronized void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
        for (int i = 0; i < threadStates.length; i++) threadStates[i].docState.similarity = similarity;
    }

    /** Set how much RAM we can use before flushing. */
    synchronized void setRAMBufferSizeMB(double mb) {
        if (mb == IndexWriter.DISABLE_AUTO_FLUSH) {
            ramBufferSize = IndexWriter.DISABLE_AUTO_FLUSH;
            waitQueuePauseBytes = 4 * 1024 * 1024;
            waitQueueResumeBytes = 2 * 1024 * 1024;
        } else {
            ramBufferSize = (long) (mb * 1024 * 1024);
            waitQueuePauseBytes = (long) (ramBufferSize * 0.1);
            waitQueueResumeBytes = (long) (ramBufferSize * 0.05);
            freeTrigger = (long) (1.05 * ramBufferSize);
            freeLevel = (long) (0.95 * ramBufferSize);
        }
    }

    synchronized double getRAMBufferSizeMB() {
        if (ramBufferSize == IndexWriter.DISABLE_AUTO_FLUSH) {
            return ramBufferSize;
        } else {
            return ramBufferSize / 1024. / 1024.;
        }
    }

    /** Set max buffered docs, which means we will flush by
   *  doc count instead of by RAM usage. */
    void setMaxBufferedDocs(int count) {
        maxBufferedDocs = count;
    }

    int getMaxBufferedDocs() {
        return maxBufferedDocs;
    }

    /** Get current segment name we are writing. */
    String getSegment() {
        return segment;
    }

    /** Returns how many docs are currently buffered in RAM. */
    int getNumDocsInRAM() {
        return numDocsInRAM;
    }

    /** Returns the current doc store segment we are writing
   *  to.  This will be the same as segment when autoCommit
   *  * is true. */
    synchronized String getDocStoreSegment() {
        return docStoreSegment;
    }

    /** Returns the doc offset into the shared doc store for
   *  the current buffered docs. */
    int getDocStoreOffset() {
        return docStoreOffset;
    }

    /** Closes the current open doc stores an returns the doc
   *  store segment name.  This returns null if there are *
   *  no buffered documents. */
    synchronized String closeDocStore() throws IOException {
        assert allThreadsIdle();
        if (infoStream != null) message("closeDocStore: " + openFiles.size() + " files to flush to segment " + docStoreSegment + " numDocs=" + numDocsInStore);
        boolean success = false;
        try {
            initFlushState(true);
            closedFiles.clear();
            consumer.closeDocStore(flushState);
            assert 0 == openFiles.size();
            String s = docStoreSegment;
            docStoreSegment = null;
            docStoreOffset = 0;
            numDocsInStore = 0;
            success = true;
            return s;
        } finally {
            if (!success) {
                abort();
            }
        }
    }

    private Collection abortedFiles;

    private FlushState flushState;

    Collection abortedFiles() {
        return abortedFiles;
    }

    void message(String message) {
        writer.message("DW: " + message);
    }

    final List openFiles = new ArrayList();

    final List closedFiles = new ArrayList();

    synchronized List openFiles() {
        return (List) ((ArrayList) openFiles).clone();
    }

    synchronized List closedFiles() {
        return (List) ((ArrayList) closedFiles).clone();
    }

    synchronized void addOpenFile(String name) {
        assert !openFiles.contains(name);
        openFiles.add(name);
    }

    synchronized void removeOpenFile(String name) {
        assert openFiles.contains(name);
        openFiles.remove(name);
        closedFiles.add(name);
    }

    synchronized void setAborting() {
        aborting = true;
    }

    /** Called if we hit an exception at a bad time (when
   *  updating the index files) and must discard all
   *  currently buffered docs.  This resets our state,
   *  discarding any docs added since last flush. */
    synchronized void abort() throws IOException {
        try {
            message("docWriter: now abort");
            waitQueue.abort();
            pauseAllThreads();
            try {
                assert 0 == waitQueue.numWaiting;
                waitQueue.waitingBytes = 0;
                try {
                    abortedFiles = openFiles();
                } catch (Throwable t) {
                    abortedFiles = null;
                }
                deletesInRAM.clear();
                openFiles.clear();
                for (int i = 0; i < threadStates.length; i++) try {
                    threadStates[i].consumer.abort();
                } catch (Throwable t) {
                }
                try {
                    consumer.abort();
                } catch (Throwable t) {
                }
                docStoreSegment = null;
                numDocsInStore = 0;
                docStoreOffset = 0;
                doAfterFlush();
            } finally {
                resumeAllThreads();
            }
        } finally {
            aborting = false;
            notifyAll();
        }
    }

    /** Reset after a flush */
    private void doAfterFlush() throws IOException {
        assert allThreadsIdle();
        threadBindings.clear();
        waitQueue.reset();
        segment = null;
        numDocsInRAM = 0;
        nextDocID = 0;
        bufferIsFull = false;
        flushPending = false;
        for (int i = 0; i < threadStates.length; i++) threadStates[i].doAfterFlush();
        numBytesUsed = 0;
    }

    synchronized boolean pauseAllThreads() {
        pauseThreads++;
        while (!allThreadsIdle()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return aborting;
    }

    synchronized void resumeAllThreads() {
        pauseThreads--;
        assert pauseThreads >= 0;
        if (0 == pauseThreads) notifyAll();
    }

    private synchronized boolean allThreadsIdle() {
        for (int i = 0; i < threadStates.length; i++) if (!threadStates[i].isIdle) return false;
        return true;
    }

    private synchronized void initFlushState(boolean onlyDocStore) {
        initSegmentName(onlyDocStore);
        if (flushState == null) {
            flushState = new FlushState();
            flushState.directory = directory;
            flushState.docWriter = this;
        }
        flushState.docStoreSegmentName = docStoreSegment;
        flushState.segmentName = segment;
        flushState.numDocsInRAM = numDocsInRAM;
        flushState.numDocsInStore = numDocsInStore;
        flushState.flushedFiles = new HashSet();
    }

    /** Flush all pending docs to a new segment */
    synchronized int flush(boolean closeDocStore) throws IOException {
        assert allThreadsIdle();
        assert numDocsInRAM > 0;
        assert nextDocID == numDocsInRAM;
        assert waitQueue.numWaiting == 0;
        assert waitQueue.waitingBytes == 0;
        initFlushState(false);
        docStoreOffset = numDocsInStore;
        if (infoStream != null) message("flush postings as segment " + flushState.segmentName + " numDocs=" + numDocsInRAM);
        boolean success = false;
        try {
            if (closeDocStore) {
                assert flushState.docStoreSegmentName != null;
                assert flushState.docStoreSegmentName.equals(flushState.segmentName);
                closeDocStore();
                flushState.numDocsInStore = 0;
            }
            Collection threads = new HashSet();
            for (int i = 0; i < threadStates.length; i++) threads.add(threadStates[i].consumer);
            consumer.flush(threads, flushState);
            if (infoStream != null) {
                final long newSegmentSize = segmentSize(flushState.segmentName);
                String message = "  oldRAMSize=" + numBytesUsed + " newFlushedSize=" + newSegmentSize + " docs/MB=" + nf.format(numDocsInRAM / (newSegmentSize / 1024. / 1024.)) + " new/old=" + nf.format(100.0 * newSegmentSize / numBytesUsed) + "%";
                message(message);
            }
            flushedDocCount += flushState.numDocsInRAM;
            doAfterFlush();
            success = true;
        } finally {
            if (!success) {
                abort();
            }
        }
        assert waitQueue.waitingBytes == 0;
        return flushState.numDocsInRAM;
    }

    /** Build compound file for the segment we just flushed */
    void createCompoundFile(String segment) throws IOException {
        CompoundFileWriter cfsWriter = new CompoundFileWriter(directory, segment + "." + IndexFileNames.COMPOUND_FILE_EXTENSION);
        Iterator it = flushState.flushedFiles.iterator();
        while (it.hasNext()) cfsWriter.addFile((String) it.next());
        cfsWriter.close();
    }

    /** Set flushPending if it is not already set and returns
   *  whether it was set. This is used by IndexWriter to
   *  trigger a single flush even when multiple threads are
   *  trying to do so. */
    synchronized boolean setFlushPending() {
        if (flushPending) return false; else {
            flushPending = true;
            return true;
        }
    }

    synchronized void clearFlushPending() {
        flushPending = false;
    }

    synchronized void pushDeletes() {
        deletesFlushed.update(deletesInRAM);
    }

    synchronized void close() {
        closed = true;
        notifyAll();
    }

    synchronized void initSegmentName(boolean onlyDocStore) {
        if (segment == null && (!onlyDocStore || docStoreSegment == null)) {
            segment = writer.newSegmentName();
            assert numDocsInRAM == 0;
        }
        if (docStoreSegment == null) {
            docStoreSegment = segment;
            assert numDocsInStore == 0;
        }
    }

    /** Returns a free (idle) ThreadState that may be used for
   * indexing this one document.  This call also pauses if a
   * flush is pending.  If delTerm is non-null then we
   * buffer this deleted term after the thread state has
   * been acquired. */
    synchronized DocumentsWriterThreadState getThreadState(Document doc, Term delTerm) throws IOException {
        DocumentsWriterThreadState state = (DocumentsWriterThreadState) threadBindings.get(Thread.currentThread());
        if (state == null) {
            DocumentsWriterThreadState minThreadState = null;
            for (int i = 0; i < threadStates.length; i++) {
                DocumentsWriterThreadState ts = threadStates[i];
                if (minThreadState == null || ts.numThreads < minThreadState.numThreads) minThreadState = ts;
            }
            if (minThreadState != null && (minThreadState.numThreads == 0 || threadStates.length >= MAX_THREAD_STATE)) {
                state = minThreadState;
                state.numThreads++;
            } else {
                DocumentsWriterThreadState[] newArray = new DocumentsWriterThreadState[1 + threadStates.length];
                if (threadStates.length > 0) System.arraycopy(threadStates, 0, newArray, 0, threadStates.length);
                state = newArray[threadStates.length] = new DocumentsWriterThreadState(this);
                threadStates = newArray;
            }
            threadBindings.put(Thread.currentThread(), state);
        }
        waitReady(state);
        initSegmentName(false);
        state.isIdle = false;
        boolean success = false;
        try {
            state.docState.docID = nextDocID;
            assert writer.testPoint("DocumentsWriter.ThreadState.init start");
            if (delTerm != null) {
                addDeleteTerm(delTerm, state.docState.docID);
                state.doFlushAfter = timeToFlushDeletes();
            }
            assert writer.testPoint("DocumentsWriter.ThreadState.init after delTerm");
            nextDocID++;
            numDocsInRAM++;
            if (!flushPending && maxBufferedDocs != IndexWriter.DISABLE_AUTO_FLUSH && numDocsInRAM >= maxBufferedDocs) {
                flushPending = true;
                state.doFlushAfter = true;
            }
            success = true;
        } finally {
            if (!success) {
                state.isIdle = true;
                notifyAll();
                if (state.doFlushAfter) {
                    state.doFlushAfter = false;
                    flushPending = false;
                }
            }
        }
        return state;
    }

    /** Returns true if the caller (IndexWriter) should now
   * flush. */
    boolean addDocument(Document doc, Analyzer analyzer) throws CorruptIndexException, IOException {
        return updateDocument(doc, analyzer, null);
    }

    boolean updateDocument(Term t, Document doc, Analyzer analyzer) throws CorruptIndexException, IOException {
        return updateDocument(doc, analyzer, t);
    }

    boolean updateDocument(Document doc, Analyzer analyzer, Term delTerm) throws CorruptIndexException, IOException {
        final DocumentsWriterThreadState state = getThreadState(doc, delTerm);
        final DocState docState = state.docState;
        docState.doc = doc;
        docState.analyzer = analyzer;
        boolean success = false;
        try {
            final DocWriter perDoc = state.consumer.processDocument();
            finishDocument(state, perDoc);
            success = true;
        } finally {
            if (!success) {
                synchronized (this) {
                    if (aborting) {
                        state.isIdle = true;
                        notifyAll();
                        abort();
                    } else {
                        skipDocWriter.docID = docState.docID;
                        boolean success2 = false;
                        try {
                            waitQueue.add(skipDocWriter);
                            success2 = true;
                        } finally {
                            if (!success2) {
                                state.isIdle = true;
                                notifyAll();
                                abort();
                                return false;
                            }
                        }
                        state.isIdle = true;
                        notifyAll();
                        if (state.doFlushAfter) {
                            state.doFlushAfter = false;
                            flushPending = false;
                            notifyAll();
                        }
                        addDeleteDocID(state.docState.docID);
                    }
                }
            }
        }
        return state.doFlushAfter || timeToFlushDeletes();
    }

    synchronized int getNumBufferedDeleteTerms() {
        return deletesInRAM.numTerms;
    }

    synchronized HashMap getBufferedDeleteTerms() {
        return deletesInRAM.terms;
    }

    /** Called whenever a merge has completed and the merged segments had deletions */
    synchronized void remapDeletes(SegmentInfos infos, int[][] docMaps, int[] delCounts, MergePolicy.OneMerge merge, int mergeDocCount) {
        if (docMaps == null) return;
        MergeDocIDRemapper mapper = new MergeDocIDRemapper(infos, docMaps, delCounts, merge, mergeDocCount);
        deletesInRAM.remap(mapper, infos, docMaps, delCounts, merge, mergeDocCount);
        deletesFlushed.remap(mapper, infos, docMaps, delCounts, merge, mergeDocCount);
        flushedDocCount -= mapper.docShift;
    }

    private synchronized void waitReady(DocumentsWriterThreadState state) {
        while (!closed && ((state != null && !state.isIdle) || pauseThreads != 0 || flushPending || aborting)) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (closed) throw new AlreadyClosedException("this IndexWriter is closed");
    }

    synchronized boolean bufferDeleteTerms(Term[] terms) throws IOException {
        waitReady(null);
        for (int i = 0; i < terms.length; i++) addDeleteTerm(terms[i], numDocsInRAM);
        return timeToFlushDeletes();
    }

    synchronized boolean bufferDeleteTerm(Term term) throws IOException {
        waitReady(null);
        addDeleteTerm(term, numDocsInRAM);
        return timeToFlushDeletes();
    }

    synchronized boolean bufferDeleteQueries(Query[] queries) throws IOException {
        waitReady(null);
        for (int i = 0; i < queries.length; i++) addDeleteQuery(queries[i], numDocsInRAM);
        return timeToFlushDeletes();
    }

    synchronized boolean bufferDeleteQuery(Query query) throws IOException {
        waitReady(null);
        addDeleteQuery(query, numDocsInRAM);
        return timeToFlushDeletes();
    }

    synchronized boolean deletesFull() {
        return maxBufferedDeleteTerms != IndexWriter.DISABLE_AUTO_FLUSH && ((deletesInRAM.numTerms + deletesInRAM.queries.size() + deletesInRAM.docIDs.size()) >= maxBufferedDeleteTerms);
    }

    private synchronized boolean timeToFlushDeletes() {
        return (bufferIsFull || deletesFull()) && setFlushPending();
    }

    void setMaxBufferedDeleteTerms(int maxBufferedDeleteTerms) {
        this.maxBufferedDeleteTerms = maxBufferedDeleteTerms;
    }

    int getMaxBufferedDeleteTerms() {
        return maxBufferedDeleteTerms;
    }

    synchronized boolean hasDeletes() {
        return deletesFlushed.any();
    }

    synchronized boolean applyDeletes(SegmentInfos infos) throws IOException {
        if (!hasDeletes()) return false;
        if (infoStream != null) message("apply " + deletesFlushed.numTerms + " buffered deleted terms and " + deletesFlushed.docIDs.size() + " deleted docIDs and " + deletesFlushed.queries.size() + " deleted queries on " + +infos.size() + " segments.");
        final int infosEnd = infos.size();
        int docStart = 0;
        boolean any = false;
        for (int i = 0; i < infosEnd; i++) {
            IndexReader reader = SegmentReader.get(infos.info(i), false);
            boolean success = false;
            try {
                any |= applyDeletes(reader, docStart);
                docStart += reader.maxDoc();
                success = true;
            } finally {
                if (reader != null) {
                    try {
                        if (success) reader.doCommit();
                    } finally {
                        reader.doClose();
                    }
                }
            }
        }
        deletesFlushed.clear();
        return any;
    }

    private final synchronized boolean applyDeletes(IndexReader reader, int docIDStart) throws CorruptIndexException, IOException {
        final int docEnd = docIDStart + reader.maxDoc();
        boolean any = false;
        Iterator iter = deletesFlushed.terms.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            Term term = (Term) entry.getKey();
            TermDocs docs = reader.termDocs(term);
            if (docs != null) {
                int limit = ((BufferedDeletes.Num) entry.getValue()).getNum();
                try {
                    while (docs.next()) {
                        int docID = docs.doc();
                        if (docIDStart + docID >= limit) break;
                        reader.deleteDocument(docID);
                        any = true;
                    }
                } finally {
                    docs.close();
                }
            }
        }
        iter = deletesFlushed.docIDs.iterator();
        while (iter.hasNext()) {
            int docID = ((Integer) iter.next()).intValue();
            if (docID >= docIDStart && docID < docEnd) {
                reader.deleteDocument(docID - docIDStart);
                any = true;
            }
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        iter = deletesFlushed.queries.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            Query query = (Query) entry.getKey();
            int limit = ((Integer) entry.getValue()).intValue();
            Weight weight = query.weight(searcher);
            Scorer scorer = weight.scorer(reader);
            while (scorer.next()) {
                final int docID = scorer.doc();
                if (docIDStart + docID >= limit) break;
                reader.deleteDocument(docID);
                any = true;
            }
        }
        searcher.close();
        return any;
    }

    private synchronized void addDeleteTerm(Term term, int docCount) {
        BufferedDeletes.Num num = (BufferedDeletes.Num) deletesInRAM.terms.get(term);
        final int docIDUpto = flushedDocCount + docCount;
        if (num == null) deletesInRAM.terms.put(term, new BufferedDeletes.Num(docIDUpto)); else num.setNum(docIDUpto);
        deletesInRAM.numTerms++;
    }

    private synchronized void addDeleteDocID(int docID) {
        deletesInRAM.docIDs.add(new Integer(flushedDocCount + docID));
    }

    private synchronized void addDeleteQuery(Query query, int docID) {
        deletesInRAM.queries.put(query, new Integer(flushedDocCount + docID));
    }

    synchronized boolean doBalanceRAM() {
        return ramBufferSize != IndexWriter.DISABLE_AUTO_FLUSH && !bufferIsFull && (numBytesUsed >= ramBufferSize || numBytesAlloc >= freeTrigger);
    }

    /** Does the synchronized work to finish/flush the
   *  inverted document. */
    private void finishDocument(DocumentsWriterThreadState perThread, DocWriter docWriter) throws IOException {
        if (doBalanceRAM()) balanceRAM();
        synchronized (this) {
            assert docWriter == null || docWriter.docID == perThread.docState.docID;
            if (aborting) {
                if (docWriter != null) try {
                    docWriter.abort();
                } catch (Throwable t) {
                }
                perThread.isIdle = true;
                notifyAll();
                return;
            }
            final boolean doPause;
            if (docWriter != null) doPause = waitQueue.add(docWriter); else {
                skipDocWriter.docID = perThread.docState.docID;
                doPause = waitQueue.add(skipDocWriter);
            }
            if (doPause) waitForWaitQueue();
            if (bufferIsFull && !flushPending) {
                flushPending = true;
                perThread.doFlushAfter = true;
            }
            perThread.isIdle = true;
            notifyAll();
        }
    }

    synchronized void waitForWaitQueue() {
        do {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } while (!waitQueue.doResume());
    }

    private static class SkipDocWriter extends DocWriter {

        void finish() {
        }

        void abort() {
        }

        long sizeInBytes() {
            return 0;
        }
    }

    final SkipDocWriter skipDocWriter = new SkipDocWriter();

    long getRAMUsed() {
        return numBytesUsed;
    }

    long numBytesAlloc;

    long numBytesUsed;

    NumberFormat nf = NumberFormat.getInstance();

    private long segmentSize(String segmentName) throws IOException {
        assert infoStream != null;
        long size = directory.fileLength(segmentName + ".tii") + directory.fileLength(segmentName + ".tis") + directory.fileLength(segmentName + ".frq") + directory.fileLength(segmentName + ".prx");
        final String normFileName = segmentName + ".nrm";
        if (directory.fileExists(normFileName)) size += directory.fileLength(normFileName);
        return size;
    }

    static final int OBJECT_HEADER_BYTES = 8;

    static final int POINTER_NUM_BYTE = 4;

    static final int INT_NUM_BYTE = 4;

    static final int CHAR_NUM_BYTE = 2;

    static final int BYTE_BLOCK_SHIFT = 15;

    static final int BYTE_BLOCK_SIZE = (int) (1 << BYTE_BLOCK_SHIFT);

    static final int BYTE_BLOCK_MASK = BYTE_BLOCK_SIZE - 1;

    static final int BYTE_BLOCK_NOT_MASK = ~BYTE_BLOCK_MASK;

    private class ByteBlockAllocator extends ByteBlockPool.Allocator {

        ArrayList freeByteBlocks = new ArrayList();

        byte[] getByteBlock(boolean trackAllocations) {
            synchronized (DocumentsWriter.this) {
                final int size = freeByteBlocks.size();
                final byte[] b;
                if (0 == size) {
                    numBytesAlloc += BYTE_BLOCK_SIZE;
                    b = new byte[BYTE_BLOCK_SIZE];
                } else b = (byte[]) freeByteBlocks.remove(size - 1);
                if (trackAllocations) numBytesUsed += BYTE_BLOCK_SIZE;
                assert numBytesUsed <= numBytesAlloc;
                return b;
            }
        }

        void recycleByteBlocks(byte[][] blocks, int start, int end) {
            synchronized (DocumentsWriter.this) {
                for (int i = start; i < end; i++) freeByteBlocks.add(blocks[i]);
            }
        }
    }

    static final int INT_BLOCK_SHIFT = 13;

    static final int INT_BLOCK_SIZE = (int) (1 << INT_BLOCK_SHIFT);

    static final int INT_BLOCK_MASK = INT_BLOCK_SIZE - 1;

    private ArrayList freeIntBlocks = new ArrayList();

    synchronized int[] getIntBlock(boolean trackAllocations) {
        final int size = freeIntBlocks.size();
        final int[] b;
        if (0 == size) {
            numBytesAlloc += INT_BLOCK_SIZE * INT_NUM_BYTE;
            b = new int[INT_BLOCK_SIZE];
        } else b = (int[]) freeIntBlocks.remove(size - 1);
        if (trackAllocations) numBytesUsed += INT_BLOCK_SIZE * INT_NUM_BYTE;
        assert numBytesUsed <= numBytesAlloc;
        return b;
    }

    synchronized void bytesAllocated(long numBytes) {
        numBytesAlloc += numBytes;
        assert numBytesUsed <= numBytesAlloc;
    }

    synchronized void bytesUsed(long numBytes) {
        numBytesUsed += numBytes;
        assert numBytesUsed <= numBytesAlloc;
    }

    synchronized void recycleIntBlocks(int[][] blocks, int start, int end) {
        for (int i = start; i < end; i++) freeIntBlocks.add(blocks[i]);
    }

    ByteBlockAllocator byteBlockAllocator = new ByteBlockAllocator();

    static final int CHAR_BLOCK_SHIFT = 14;

    static final int CHAR_BLOCK_SIZE = (int) (1 << CHAR_BLOCK_SHIFT);

    static final int CHAR_BLOCK_MASK = CHAR_BLOCK_SIZE - 1;

    static final int MAX_TERM_LENGTH = CHAR_BLOCK_SIZE - 1;

    private ArrayList freeCharBlocks = new ArrayList();

    synchronized char[] getCharBlock() {
        final int size = freeCharBlocks.size();
        final char[] c;
        if (0 == size) {
            numBytesAlloc += CHAR_BLOCK_SIZE * CHAR_NUM_BYTE;
            c = new char[CHAR_BLOCK_SIZE];
        } else c = (char[]) freeCharBlocks.remove(size - 1);
        numBytesUsed += CHAR_BLOCK_SIZE * CHAR_NUM_BYTE;
        assert numBytesUsed <= numBytesAlloc;
        return c;
    }

    synchronized void recycleCharBlocks(char[][] blocks, int numBlocks) {
        for (int i = 0; i < numBlocks; i++) freeCharBlocks.add(blocks[i]);
    }

    String toMB(long v) {
        return nf.format(v / 1024. / 1024.);
    }

    void balanceRAM() {
        final long flushTrigger = (long) ramBufferSize;
        if (numBytesAlloc > freeTrigger) {
            if (infoStream != null) message("  RAM: now balance allocations: usedMB=" + toMB(numBytesUsed) + " vs trigger=" + toMB(flushTrigger) + " allocMB=" + toMB(numBytesAlloc) + " vs trigger=" + toMB(freeTrigger) + " byteBlockFree=" + toMB(byteBlockAllocator.freeByteBlocks.size() * BYTE_BLOCK_SIZE) + " charBlockFree=" + toMB(freeCharBlocks.size() * CHAR_BLOCK_SIZE * CHAR_NUM_BYTE));
            final long startBytesAlloc = numBytesAlloc;
            int iter = 0;
            boolean any = true;
            while (numBytesAlloc > freeLevel) {
                synchronized (this) {
                    if (0 == byteBlockAllocator.freeByteBlocks.size() && 0 == freeCharBlocks.size() && 0 == freeIntBlocks.size() && !any) {
                        bufferIsFull = numBytesUsed > flushTrigger;
                        if (infoStream != null) {
                            if (numBytesUsed > flushTrigger) message("    nothing to free; now set bufferIsFull"); else message("    nothing to free");
                        }
                        assert numBytesUsed <= numBytesAlloc;
                        break;
                    }
                    if ((0 == iter % 4) && byteBlockAllocator.freeByteBlocks.size() > 0) {
                        byteBlockAllocator.freeByteBlocks.remove(byteBlockAllocator.freeByteBlocks.size() - 1);
                        numBytesAlloc -= BYTE_BLOCK_SIZE;
                    }
                    if ((1 == iter % 4) && freeCharBlocks.size() > 0) {
                        freeCharBlocks.remove(freeCharBlocks.size() - 1);
                        numBytesAlloc -= CHAR_BLOCK_SIZE * CHAR_NUM_BYTE;
                    }
                    if ((2 == iter % 4) && freeIntBlocks.size() > 0) {
                        freeIntBlocks.remove(freeIntBlocks.size() - 1);
                        numBytesAlloc -= INT_BLOCK_SIZE * INT_NUM_BYTE;
                    }
                }
                if ((3 == iter % 4) && any) any = consumer.freeRAM();
                iter++;
            }
            if (infoStream != null) message("    after free: freedMB=" + nf.format((startBytesAlloc - numBytesAlloc) / 1024. / 1024.) + " usedMB=" + nf.format(numBytesUsed / 1024. / 1024.) + " allocMB=" + nf.format(numBytesAlloc / 1024. / 1024.));
        } else {
            synchronized (this) {
                if (numBytesUsed > flushTrigger) {
                    if (infoStream != null) message("  RAM: now flush @ usedMB=" + nf.format(numBytesUsed / 1024. / 1024.) + " allocMB=" + nf.format(numBytesAlloc / 1024. / 1024.) + " triggerMB=" + nf.format(flushTrigger / 1024. / 1024.));
                    bufferIsFull = true;
                }
            }
        }
    }

    final WaitQueue waitQueue = new WaitQueue();

    private class WaitQueue {

        DocWriter[] waiting;

        int nextWriteDocID;

        int nextWriteLoc;

        int numWaiting;

        long waitingBytes;

        public WaitQueue() {
            waiting = new DocWriter[10];
        }

        synchronized void reset() {
            assert numWaiting == 0;
            assert waitingBytes == 0;
            nextWriteDocID = 0;
        }

        synchronized boolean doResume() {
            return waitingBytes <= waitQueueResumeBytes;
        }

        synchronized boolean doPause() {
            return waitingBytes > waitQueuePauseBytes;
        }

        synchronized void abort() {
            int count = 0;
            for (int i = 0; i < waiting.length; i++) {
                final DocWriter doc = waiting[i];
                if (doc != null) {
                    doc.abort();
                    waiting[i] = null;
                    count++;
                }
            }
            waitingBytes = 0;
            assert count == numWaiting;
            numWaiting = 0;
        }

        private void writeDocument(DocWriter doc) throws IOException {
            assert doc == skipDocWriter || nextWriteDocID == doc.docID;
            boolean success = false;
            try {
                doc.finish();
                nextWriteDocID++;
                numDocsInStore++;
                nextWriteLoc++;
                assert nextWriteLoc <= waiting.length;
                if (nextWriteLoc == waiting.length) nextWriteLoc = 0;
                success = true;
            } finally {
                if (!success) setAborting();
            }
        }

        public synchronized boolean add(DocWriter doc) throws IOException {
            assert doc.docID >= nextWriteDocID;
            if (doc.docID == nextWriteDocID) {
                writeDocument(doc);
                while (true) {
                    doc = waiting[nextWriteLoc];
                    if (doc != null) {
                        numWaiting--;
                        waiting[nextWriteLoc] = null;
                        waitingBytes -= doc.sizeInBytes();
                        writeDocument(doc);
                    } else break;
                }
            } else {
                int gap = doc.docID - nextWriteDocID;
                if (gap >= waiting.length) {
                    DocWriter[] newArray = new DocWriter[ArrayUtil.getNextSize(gap)];
                    assert nextWriteLoc >= 0;
                    System.arraycopy(waiting, nextWriteLoc, newArray, 0, waiting.length - nextWriteLoc);
                    System.arraycopy(waiting, 0, newArray, waiting.length - nextWriteLoc, nextWriteLoc);
                    nextWriteLoc = 0;
                    waiting = newArray;
                    gap = doc.docID - nextWriteDocID;
                }
                int loc = nextWriteLoc + gap;
                if (loc >= waiting.length) loc -= waiting.length;
                assert loc < waiting.length;
                assert waiting[loc] == null;
                waiting[loc] = doc;
                numWaiting++;
                waitingBytes += doc.sizeInBytes();
            }
            return doPause();
        }
    }
}
