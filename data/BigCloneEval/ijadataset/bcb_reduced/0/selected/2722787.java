package org.apache.lucene.search.spans;

import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CheckHits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryUtils;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;

public class TestFieldMaskingSpanQuery extends LuceneTestCase {

    protected static Document doc(Field[] fields) {
        Document doc = new Document();
        for (int i = 0; i < fields.length; i++) {
            doc.add(fields[i]);
        }
        return doc;
    }

    protected static Field field(String name, String value) {
        return new Field(name, value, Field.Store.NO, Field.Index.ANALYZED);
    }

    protected IndexSearcher searcher;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RAMDirectory directory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(directory, new WhitespaceAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        writer.addDocument(doc(new Field[] { field("id", "0"), field("gender", "male"), field("first", "james"), field("last", "jones") }));
        writer.addDocument(doc(new Field[] { field("id", "1"), field("gender", "male"), field("first", "james"), field("last", "smith"), field("gender", "female"), field("first", "sally"), field("last", "jones") }));
        writer.addDocument(doc(new Field[] { field("id", "2"), field("gender", "female"), field("first", "greta"), field("last", "jones"), field("gender", "female"), field("first", "sally"), field("last", "smith"), field("gender", "male"), field("first", "james"), field("last", "jones") }));
        writer.addDocument(doc(new Field[] { field("id", "3"), field("gender", "female"), field("first", "lisa"), field("last", "jones"), field("gender", "male"), field("first", "bob"), field("last", "costas") }));
        writer.addDocument(doc(new Field[] { field("id", "4"), field("gender", "female"), field("first", "sally"), field("last", "smith"), field("gender", "female"), field("first", "linda"), field("last", "dixit"), field("gender", "male"), field("first", "bubba"), field("last", "jones") }));
        writer.close();
        searcher = new IndexSearcher(directory, true);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        searcher.close();
    }

    protected void check(SpanQuery q, int[] docs) throws Exception {
        CheckHits.checkHitCollector(q, null, searcher, docs);
    }

    public void testRewrite0() throws Exception {
        SpanQuery q = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")), "first");
        q.setBoost(8.7654321f);
        SpanQuery qr = (SpanQuery) searcher.rewrite(q);
        QueryUtils.checkEqual(q, qr);
        Set<Term> terms = new HashSet<Term>();
        qr.extractTerms(terms);
        assertEquals(1, terms.size());
    }

    public void testRewrite1() throws Exception {
        SpanQuery q = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")) {

            @Override
            public Query rewrite(IndexReader reader) {
                return new SpanOrQuery(new SpanQuery[] { new SpanTermQuery(new Term("first", "sally")), new SpanTermQuery(new Term("first", "james")) });
            }
        }, "first");
        SpanQuery qr = (SpanQuery) searcher.rewrite(q);
        QueryUtils.checkUnequal(q, qr);
        Set<Term> terms = new HashSet<Term>();
        qr.extractTerms(terms);
        assertEquals(2, terms.size());
    }

    public void testRewrite2() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("last", "smith"));
        SpanQuery q2 = new SpanTermQuery(new Term("last", "jones"));
        SpanQuery q = new SpanNearQuery(new SpanQuery[] { q1, new FieldMaskingSpanQuery(q2, "last") }, 1, true);
        Query qr = searcher.rewrite(q);
        QueryUtils.checkEqual(q, qr);
        HashSet set = new HashSet();
        qr.extractTerms(set);
        assertEquals(2, set.size());
    }

    public void testEquality1() {
        SpanQuery q1 = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")), "first");
        SpanQuery q2 = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")), "first");
        SpanQuery q3 = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")), "XXXXX");
        SpanQuery q4 = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "XXXXX")), "first");
        SpanQuery q5 = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("xXXX", "sally")), "first");
        QueryUtils.checkEqual(q1, q2);
        QueryUtils.checkUnequal(q1, q3);
        QueryUtils.checkUnequal(q1, q4);
        QueryUtils.checkUnequal(q1, q5);
        SpanQuery qA = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")), "first");
        qA.setBoost(9f);
        SpanQuery qB = new FieldMaskingSpanQuery(new SpanTermQuery(new Term("last", "sally")), "first");
        QueryUtils.checkUnequal(qA, qB);
        qB.setBoost(9f);
        QueryUtils.checkEqual(qA, qB);
    }

    public void testNoop0() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("last", "sally"));
        SpanQuery q = new FieldMaskingSpanQuery(q1, "first");
        check(q, new int[] {});
    }

    public void testNoop1() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("last", "smith"));
        SpanQuery q2 = new SpanTermQuery(new Term("last", "jones"));
        SpanQuery q = new SpanNearQuery(new SpanQuery[] { q1, new FieldMaskingSpanQuery(q2, "last") }, 0, true);
        check(q, new int[] { 1, 2 });
        q = new SpanNearQuery(new SpanQuery[] { new FieldMaskingSpanQuery(q1, "last"), new FieldMaskingSpanQuery(q2, "last") }, 0, true);
        check(q, new int[] { 1, 2 });
    }

    public void testSimple1() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("first", "james"));
        SpanQuery q2 = new SpanTermQuery(new Term("last", "jones"));
        SpanQuery q = new SpanNearQuery(new SpanQuery[] { q1, new FieldMaskingSpanQuery(q2, "first") }, -1, false);
        check(q, new int[] { 0, 2 });
        q = new SpanNearQuery(new SpanQuery[] { new FieldMaskingSpanQuery(q2, "first"), q1 }, -1, false);
        check(q, new int[] { 0, 2 });
        q = new SpanNearQuery(new SpanQuery[] { q2, new FieldMaskingSpanQuery(q1, "last") }, -1, false);
        check(q, new int[] { 0, 2 });
        q = new SpanNearQuery(new SpanQuery[] { new FieldMaskingSpanQuery(q1, "last"), q2 }, -1, false);
        check(q, new int[] { 0, 2 });
    }

    public void testSimple2() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("gender", "female"));
        SpanQuery q2 = new SpanTermQuery(new Term("last", "smith"));
        SpanQuery q = new SpanNearQuery(new SpanQuery[] { q1, new FieldMaskingSpanQuery(q2, "gender") }, -1, false);
        check(q, new int[] { 2, 4 });
        q = new SpanNearQuery(new SpanQuery[] { new FieldMaskingSpanQuery(q1, "id"), new FieldMaskingSpanQuery(q2, "id") }, -1, false);
        check(q, new int[] { 2, 4 });
    }

    public void testSpans0() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("gender", "female"));
        SpanQuery q2 = new SpanTermQuery(new Term("first", "james"));
        SpanQuery q = new SpanOrQuery(new SpanQuery[] { q1, new FieldMaskingSpanQuery(q2, "gender") });
        check(q, new int[] { 0, 1, 2, 3, 4 });
        Spans span = q.getSpans(searcher.getIndexReader());
        assertEquals(true, span.next());
        assertEquals(s(0, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(1, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(1, 1, 2), s(span));
        assertEquals(true, span.next());
        assertEquals(s(2, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(2, 1, 2), s(span));
        assertEquals(true, span.next());
        assertEquals(s(2, 2, 3), s(span));
        assertEquals(true, span.next());
        assertEquals(s(3, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(4, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(4, 1, 2), s(span));
        assertEquals(false, span.next());
    }

    public void testSpans1() throws Exception {
        SpanQuery q1 = new SpanTermQuery(new Term("first", "sally"));
        SpanQuery q2 = new SpanTermQuery(new Term("first", "james"));
        SpanQuery qA = new SpanOrQuery(new SpanQuery[] { q1, q2 });
        SpanQuery qB = new FieldMaskingSpanQuery(qA, "id");
        check(qA, new int[] { 0, 1, 2, 4 });
        check(qB, new int[] { 0, 1, 2, 4 });
        Spans spanA = qA.getSpans(searcher.getIndexReader());
        Spans spanB = qB.getSpans(searcher.getIndexReader());
        while (spanA.next()) {
            assertTrue("spanB not still going", spanB.next());
            assertEquals("spanA not equal spanB", s(spanA), s(spanB));
        }
        assertTrue("spanB still going even tough spanA is done", !(spanB.next()));
    }

    public void testSpans2() throws Exception {
        SpanQuery qA1 = new SpanTermQuery(new Term("gender", "female"));
        SpanQuery qA2 = new SpanTermQuery(new Term("first", "james"));
        SpanQuery qA = new SpanOrQuery(new SpanQuery[] { qA1, new FieldMaskingSpanQuery(qA2, "gender") });
        SpanQuery qB = new SpanTermQuery(new Term("last", "jones"));
        SpanQuery q = new SpanNearQuery(new SpanQuery[] { new FieldMaskingSpanQuery(qA, "id"), new FieldMaskingSpanQuery(qB, "id") }, -1, false);
        check(q, new int[] { 0, 1, 2, 3 });
        Spans span = q.getSpans(searcher.getIndexReader());
        assertEquals(true, span.next());
        assertEquals(s(0, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(1, 1, 2), s(span));
        assertEquals(true, span.next());
        assertEquals(s(2, 0, 1), s(span));
        assertEquals(true, span.next());
        assertEquals(s(2, 2, 3), s(span));
        assertEquals(true, span.next());
        assertEquals(s(3, 0, 1), s(span));
        assertEquals(false, span.next());
    }

    public String s(Spans span) {
        return s(span.doc(), span.start(), span.end());
    }

    public String s(int doc, int start, int end) {
        return "s(" + doc + "," + start + "," + end + ")";
    }
}
