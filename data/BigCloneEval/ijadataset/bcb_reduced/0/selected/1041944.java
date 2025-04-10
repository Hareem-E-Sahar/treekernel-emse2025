package org.apache.lucene.index;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.index.SegmentReader.Norm;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.AlreadyClosedException;

/**
 * Tests cloning multiple types of readers, modifying the deletedDocs and norms
 * and verifies copy on write semantics of the deletedDocs and norms is
 * implemented properly
 */
public class TestIndexReaderClone extends LuceneTestCase {

    public void testCloneReadOnlySegmentReader() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        IndexReader reader = IndexReader.open(dir1, false);
        IndexReader readOnlyReader = reader.clone(true);
        if (!isReadOnly(readOnlyReader)) {
            fail("reader isn't read only");
        }
        if (deleteWorked(1, readOnlyReader)) {
            fail("deleting from the original should not have worked");
        }
        reader.close();
        readOnlyReader.close();
        dir1.close();
    }

    public void testCloneNoChangesStillReadOnly() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader r1 = IndexReader.open(dir1, false);
        IndexReader r2 = r1.clone(false);
        if (!deleteWorked(1, r2)) {
            fail("deleting from the cloned should have worked");
        }
        r1.close();
        r2.close();
        dir1.close();
    }

    public void testCloneWriteToOrig() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader r1 = IndexReader.open(dir1, false);
        IndexReader r2 = r1.clone(false);
        if (!deleteWorked(1, r1)) {
            fail("deleting from the original should have worked");
        }
        r1.close();
        r2.close();
        dir1.close();
    }

    public void testCloneWriteToClone() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader r1 = IndexReader.open(dir1, false);
        IndexReader r2 = r1.clone(false);
        if (!deleteWorked(1, r2)) {
            fail("deleting from the original should have worked");
        }
        assertTrue("first reader should not be able to delete", !deleteWorked(1, r1));
        r2.close();
        assertTrue("first reader should not be able to delete", !deleteWorked(1, r1));
        r1.close();
        dir1.close();
    }

    public void testReopenSegmentReaderToMultiReader() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        IndexReader reader1 = IndexReader.open(dir1, false);
        TestIndexReaderReopen.modifyIndex(5, dir1);
        IndexReader reader2 = reader1.reopen();
        assertTrue(reader1 != reader2);
        assertTrue(deleteWorked(1, reader2));
        reader1.close();
        reader2.close();
        dir1.close();
    }

    public void testCloneWriteableToReadOnly() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader reader = IndexReader.open(dir1, false);
        IndexReader readOnlyReader = reader.clone(true);
        if (!isReadOnly(readOnlyReader)) {
            fail("reader isn't read only");
        }
        if (deleteWorked(1, readOnlyReader)) {
            fail("deleting from the original should not have worked");
        }
        if (readOnlyReader.hasChanges) {
            fail("readOnlyReader has a write lock");
        }
        reader.close();
        readOnlyReader.close();
        dir1.close();
    }

    public void testReopenWriteableToReadOnly() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader reader = IndexReader.open(dir1, false);
        final int docCount = reader.numDocs();
        assertTrue(deleteWorked(1, reader));
        assertEquals(docCount - 1, reader.numDocs());
        IndexReader readOnlyReader = reader.reopen(true);
        if (!isReadOnly(readOnlyReader)) {
            fail("reader isn't read only");
        }
        assertFalse(deleteWorked(1, readOnlyReader));
        assertEquals(docCount - 1, readOnlyReader.numDocs());
        reader.close();
        readOnlyReader.close();
        dir1.close();
    }

    public void testCloneReadOnlyToWriteable() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader reader1 = IndexReader.open(dir1, true);
        IndexReader reader2 = reader1.clone(false);
        if (isReadOnly(reader2)) {
            fail("reader should not be read only");
        }
        assertFalse("deleting from the original reader should not have worked", deleteWorked(1, reader1));
        if (reader2.hasChanges) {
            fail("cloned reader should not have write lock");
        }
        assertTrue("deleting from the cloned reader should have worked", deleteWorked(1, reader2));
        reader1.close();
        reader2.close();
        dir1.close();
    }

    public void testReadOnlyCloneAfterOptimize() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader reader1 = IndexReader.open(dir1, false);
        IndexWriter w = new IndexWriter(dir1, new SimpleAnalyzer(), IndexWriter.MaxFieldLength.LIMITED);
        w.optimize();
        w.close();
        IndexReader reader2 = reader1.clone(true);
        assertTrue(isReadOnly(reader2));
        reader1.close();
        reader2.close();
        dir1.close();
    }

    private static boolean deleteWorked(int doc, IndexReader r) {
        boolean exception = false;
        try {
            r.deleteDocument(doc);
        } catch (Exception ex) {
            exception = true;
        }
        return !exception;
    }

    public void testCloneReadOnlyDirectoryReader() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader reader = IndexReader.open(dir1, false);
        IndexReader readOnlyReader = reader.clone(true);
        if (!isReadOnly(readOnlyReader)) {
            fail("reader isn't read only");
        }
        reader.close();
        readOnlyReader.close();
        dir1.close();
    }

    public static boolean isReadOnly(IndexReader r) {
        if (r instanceof ReadOnlySegmentReader || r instanceof ReadOnlyDirectoryReader) return true;
        return false;
    }

    public void testParallelReader() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        final Directory dir2 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir2, true);
        IndexReader r1 = IndexReader.open(dir1, false);
        IndexReader r2 = IndexReader.open(dir2, false);
        ParallelReader pr1 = new ParallelReader();
        pr1.add(r1);
        pr1.add(r2);
        performDefaultTests(pr1);
        pr1.close();
        dir1.close();
        dir2.close();
    }

    /**
   * 1. Get a norm from the original reader 2. Clone the original reader 3.
   * Delete a document and set the norm of the cloned reader 4. Verify the norms
   * are not the same on each reader 5. Verify the doc deleted is only in the
   * cloned reader 6. Try to delete a document in the original reader, an
   * exception should be thrown
   * 
   * @param r1 IndexReader to perform tests on
   * @throws Exception
   */
    private void performDefaultTests(IndexReader r1) throws Exception {
        float norm1 = Similarity.decodeNorm(r1.norms("field1")[4]);
        IndexReader pr1Clone = (IndexReader) r1.clone();
        pr1Clone.deleteDocument(10);
        pr1Clone.setNorm(4, "field1", 0.5f);
        assertTrue(Similarity.decodeNorm(r1.norms("field1")[4]) == norm1);
        assertTrue(Similarity.decodeNorm(pr1Clone.norms("field1")[4]) != norm1);
        assertTrue(!r1.isDeleted(10));
        assertTrue(pr1Clone.isDeleted(10));
        try {
            r1.deleteDocument(11);
            fail("Tried to delete doc 11 and an exception should have been thrown");
        } catch (Exception exception) {
        }
        pr1Clone.close();
    }

    public void testMixedReaders() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        final Directory dir2 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir2, true);
        IndexReader r1 = IndexReader.open(dir1, false);
        IndexReader r2 = IndexReader.open(dir2, false);
        MultiReader multiReader = new MultiReader(new IndexReader[] { r1, r2 });
        performDefaultTests(multiReader);
        multiReader.close();
        dir1.close();
        dir2.close();
    }

    public void testSegmentReaderUndeleteall() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        SegmentReader origSegmentReader = SegmentReader.getOnlySegmentReader(dir1);
        origSegmentReader.deleteDocument(10);
        assertDelDocsRefCountEquals(1, origSegmentReader);
        origSegmentReader.undeleteAll();
        assertNull(origSegmentReader.deletedDocsRef);
        origSegmentReader.close();
        dir1.close();
    }

    public void testSegmentReaderCloseReferencing() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        SegmentReader origSegmentReader = SegmentReader.getOnlySegmentReader(dir1);
        origSegmentReader.deleteDocument(1);
        origSegmentReader.setNorm(4, "field1", 0.5f);
        SegmentReader clonedSegmentReader = (SegmentReader) origSegmentReader.clone();
        assertDelDocsRefCountEquals(2, origSegmentReader);
        origSegmentReader.close();
        assertDelDocsRefCountEquals(1, origSegmentReader);
        Norm norm = clonedSegmentReader.norms.get("field1");
        assertEquals(1, norm.bytesRef().refCount());
        clonedSegmentReader.close();
        dir1.close();
    }

    public void testSegmentReaderDelDocsReferenceCounting() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        IndexReader origReader = IndexReader.open(dir1, false);
        SegmentReader origSegmentReader = SegmentReader.getOnlySegmentReader(origReader);
        assertNull(origSegmentReader.deletedDocsRef);
        origReader.deleteDocument(1);
        assertDelDocsRefCountEquals(1, origSegmentReader);
        IndexReader clonedReader = (IndexReader) origReader.clone();
        SegmentReader clonedSegmentReader = SegmentReader.getOnlySegmentReader(clonedReader);
        assertDelDocsRefCountEquals(2, origSegmentReader);
        clonedReader.deleteDocument(2);
        assertDelDocsRefCountEquals(1, origSegmentReader);
        assertDelDocsRefCountEquals(1, clonedSegmentReader);
        assertTrue(origSegmentReader.deletedDocs != clonedSegmentReader.deletedDocs);
        assertDocDeleted(origSegmentReader, clonedSegmentReader, 1);
        assertTrue(!origSegmentReader.isDeleted(2));
        assertTrue(clonedSegmentReader.isDeleted(2));
        try {
            origReader.deleteDocument(4);
            fail("expected exception");
        } catch (LockObtainFailedException lbfe) {
        }
        origReader.close();
        clonedReader.deleteDocument(3);
        clonedReader.flush();
        assertDelDocsRefCountEquals(1, clonedSegmentReader);
        IndexReader reopenedReader = clonedReader.reopen();
        IndexReader cloneReader2 = (IndexReader) reopenedReader.clone();
        SegmentReader cloneSegmentReader2 = SegmentReader.getOnlySegmentReader(cloneReader2);
        assertDelDocsRefCountEquals(2, cloneSegmentReader2);
        clonedReader.close();
        reopenedReader.close();
        cloneReader2.close();
        dir1.close();
    }

    public void testCloneWithDeletes() throws Throwable {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        IndexReader origReader = IndexReader.open(dir1, false);
        origReader.deleteDocument(1);
        IndexReader clonedReader = (IndexReader) origReader.clone();
        origReader.close();
        clonedReader.close();
        IndexReader r = IndexReader.open(dir1, false);
        assertTrue(r.isDeleted(1));
        r.close();
        dir1.close();
    }

    public void testCloneWithSetNorm() throws Throwable {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        IndexReader orig = IndexReader.open(dir1, false);
        orig.setNorm(1, "field1", 17.0f);
        final byte encoded = Similarity.encodeNorm(17.0f);
        assertEquals(encoded, orig.norms("field1")[1]);
        IndexReader clonedReader = (IndexReader) orig.clone();
        orig.close();
        clonedReader.close();
        IndexReader r = IndexReader.open(dir1, false);
        assertEquals(encoded, r.norms("field1")[1]);
        r.close();
        dir1.close();
    }

    private void assertDocDeleted(SegmentReader reader, SegmentReader reader2, int doc) {
        assertEquals(reader.isDeleted(doc), reader2.isDeleted(doc));
    }

    private void assertDelDocsRefCountEquals(int refCount, SegmentReader reader) {
        assertEquals(refCount, reader.deletedDocsRef.refCount());
    }

    public void testCloneSubreaders() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, true);
        IndexReader reader = IndexReader.open(dir1, false);
        reader.deleteDocument(1);
        IndexReader[] subs = reader.getSequentialSubReaders();
        assert subs.length > 1;
        IndexReader[] clones = new IndexReader[subs.length];
        for (int x = 0; x < subs.length; x++) {
            clones[x] = (IndexReader) subs[x].clone();
        }
        reader.close();
        for (int x = 0; x < subs.length; x++) {
            clones[x].close();
        }
        dir1.close();
    }

    public void testLucene1516Bug() throws Exception {
        final Directory dir1 = new MockRAMDirectory();
        TestIndexReaderReopen.createIndex(dir1, false);
        IndexReader r1 = IndexReader.open(dir1, false);
        r1.incRef();
        IndexReader r2 = r1.clone(false);
        r1.deleteDocument(5);
        r1.decRef();
        r1.incRef();
        r2.close();
        r1.decRef();
        r1.close();
        dir1.close();
    }

    public void testCloseStoredFields() throws Exception {
        final Directory dir = new MockRAMDirectory();
        IndexWriter w = new IndexWriter(dir, new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
        w.setUseCompoundFile(false);
        Document doc = new Document();
        doc.add(new Field("field", "yes it's stored", Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
        w.close();
        IndexReader r1 = IndexReader.open(dir, false);
        IndexReader r2 = r1.clone(false);
        r1.close();
        r2.close();
        dir.close();
    }
}
