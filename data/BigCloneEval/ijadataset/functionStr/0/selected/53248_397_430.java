public class Test {    public void testPalyndrome3() throws Exception {
        query.setSlop(0);
        query.add(new Term("field", "one"));
        query.add(new Term("field", "two"));
        query.add(new Term("field", "three"));
        ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals("phrase found with exact phrase scorer", 1, hits.length);
        float score0 = hits[0].score;
        QueryUtils.check(query, searcher);
        query.setSlop(4);
        hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals("just sloppy enough", 1, hits.length);
        float score1 = hits[0].score;
        assertEquals("exact scorer and sloppy scorer score the same when slop does not matter", score0, score1, SCORE_COMP_THRESH);
        QueryUtils.check(query, searcher);
        query = new PhraseQuery();
        query.setSlop(4);
        query.add(new Term("palindrome", "one"));
        query.add(new Term("palindrome", "two"));
        query.add(new Term("palindrome", "three"));
        hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals("just sloppy enough", 1, hits.length);
        float score2 = hits[0].score;
        QueryUtils.check(query, searcher);
        query = new PhraseQuery();
        query.setSlop(4);
        query.add(new Term("palindrome", "three"));
        query.add(new Term("palindrome", "two"));
        query.add(new Term("palindrome", "one"));
        hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals("just sloppy enough", 1, hits.length);
        float score3 = hits[0].score;
        QueryUtils.check(query, searcher);
    }
}