package org.apache.lucene.search;

import org.apache.lucene.index.IndexReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

/**
 * A query that generates the union of the documents produced by its subqueries, and that scores each document as the maximum
 * score for that document produced by any subquery plus a tie breaking increment for any additional matching subqueries.
 * This is useful to search for a word in multiple fields with different boost factors (so that the fields cannot be
 * combined equivalently into a single search field).  We want the primary score to be the one associated with the highest boost,
 * not the sum of the field scores (as BooleanQuery would give).
 * If the query is "albino elephant" this ensures that "albino" matching one field and "elephant" matching
 * another gets a higher score than "albino" matching both fields.
 * To get this result, use both BooleanQuery and DisjunctionMaxQuery:  for each term a DisjunctionMaxQuery searches for it in
 * each field, while the set of these DisjunctionMaxQuery's is combined into a BooleanQuery.
 * The tie breaker capability allows results that include the same term in multiple fields to be judged better than results that
 * include this term in only the best of those multiple fields, without confusing this with the better case of two different terms
 * in the multiple fields.
 * @author Chuck Williams
 */
public class DisjunctionMaxQuery extends Query {

    private ArrayList disjuncts = new ArrayList();

    private float tieBreakerMultiplier = 0.0f;

    /** Creates a new empty DisjunctionMaxQuery.  Use add() to add the subqueries.
   * @param tieBreakerMultiplier this score of each non-maximum disjunct for a document is multiplied by this weight
   *        and added into the final score.  If non-zero, the value should be small, on the order of 0.1, which says that
   *        10 occurrences of word in a lower-scored field that is also in a higher scored field is just as good as a unique
   *        word in the lower scored field (i.e., one that is not in any higher scored field.
   */
    public DisjunctionMaxQuery(float tieBreakerMultiplier) {
        this.tieBreakerMultiplier = tieBreakerMultiplier;
    }

    /**
   * Creates a new DisjunctionMaxQuery
   * @param disjuncts a Collection<Query> of all the disjuncts to add
   * @param tieBreakerMultiplier   the weight to give to each matching non-maximum disjunct
   */
    public DisjunctionMaxQuery(Collection disjuncts, float tieBreakerMultiplier) {
        this.tieBreakerMultiplier = tieBreakerMultiplier;
        add(disjuncts);
    }

    /** Add a subquery to this disjunction
   * @param query the disjunct added
   */
    public void add(Query query) {
        disjuncts.add(query);
    }

    /** Add a collection of disjuncts to this disjunction
   * via Iterable<Query>
   */
    public void add(Collection disjuncts) {
        this.disjuncts.addAll(disjuncts);
    }

    /** An Iterator<Query> over the disjuncts */
    public Iterator iterator() {
        return disjuncts.iterator();
    }

    private class DisjunctionMaxWeight implements Weight {

        private Searcher searcher;

        private ArrayList weights = new ArrayList();

        public DisjunctionMaxWeight(Searcher searcher) throws IOException {
            this.searcher = searcher;
            for (int i = 0; i < disjuncts.size(); i++) weights.add(((Query) disjuncts.get(i)).createWeight(searcher));
        }

        public Query getQuery() {
            return DisjunctionMaxQuery.this;
        }

        public float getValue() {
            return getBoost();
        }

        public float sumOfSquaredWeights() throws IOException {
            float max = 0.0f, sum = 0.0f;
            for (int i = 0; i < weights.size(); i++) {
                float sub = ((Weight) weights.get(i)).sumOfSquaredWeights();
                sum += sub;
                max = Math.max(max, sub);
            }
            return (((sum - max) * tieBreakerMultiplier * tieBreakerMultiplier) + max) * getBoost() * getBoost();
        }

        public void normalize(float norm) {
            norm *= getBoost();
            for (int i = 0; i < weights.size(); i++) ((Weight) weights.get(i)).normalize(norm);
        }

        public Scorer scorer(IndexReader reader) throws IOException {
            DisjunctionMaxScorer result = new DisjunctionMaxScorer(tieBreakerMultiplier, getSimilarity(searcher));
            for (int i = 0; i < weights.size(); i++) {
                Weight w = (Weight) weights.get(i);
                Scorer subScorer = w.scorer(reader);
                if (subScorer == null) return null;
                result.add(subScorer);
            }
            return result;
        }

        public Explanation explain(IndexReader reader, int doc) throws IOException {
            if (disjuncts.size() == 1) return ((Weight) weights.get(0)).explain(reader, doc);
            Explanation result = new Explanation();
            float max = 0.0f, sum = 0.0f;
            result.setDescription(tieBreakerMultiplier == 0.0f ? "max of:" : "max plus " + tieBreakerMultiplier + " times others of:");
            for (int i = 0; i < weights.size(); i++) {
                Explanation e = ((Weight) weights.get(i)).explain(reader, doc);
                if (e.getValue() > 0) {
                    result.addDetail(e);
                    sum += e.getValue();
                    max = Math.max(max, e.getValue());
                }
            }
            result.setValue(max + (sum - max) * tieBreakerMultiplier);
            return result;
        }
    }

    protected Weight createWeight(Searcher searcher) throws IOException {
        return new DisjunctionMaxWeight(searcher);
    }

    /** Optimize our representation and our subqueries representations
   * @param reader the IndexReader we query
   * @return an optimized copy of us (which may not be a copy if there is nothing to optimize) */
    public Query rewrite(IndexReader reader) throws IOException {
        if (disjuncts.size() == 1) {
            Query singleton = (Query) disjuncts.get(0);
            Query result = singleton.rewrite(reader);
            if (getBoost() != 1.0f) {
                if (result == singleton) result = (Query) result.clone();
                result.setBoost(getBoost() * result.getBoost());
            }
            return result;
        }
        DisjunctionMaxQuery clone = null;
        for (int i = 0; i < disjuncts.size(); i++) {
            Query clause = (Query) disjuncts.get(i);
            Query rewrite = clause.rewrite(reader);
            if (rewrite != clause) {
                if (clone == null) clone = (DisjunctionMaxQuery) this.clone();
                clone.disjuncts.set(i, rewrite);
            }
        }
        if (clone != null) return clone; else return this;
    }

    /** Create a shallow copy of us -- used in rewriting if necessary
   * @return a copy of us (but reuse, don't copy, our subqueries) */
    public Object clone() {
        DisjunctionMaxQuery clone = (DisjunctionMaxQuery) super.clone();
        clone.disjuncts = (ArrayList) this.disjuncts.clone();
        return clone;
    }

    /** Prettyprint us.
   * @param field the field to which we are applied
   * @return a string that shows what we do, of the form "(disjunct1 | disjunct2 | ... | disjunctn)^boost"
   */
    public String toString(String field) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        for (int i = 0; i < disjuncts.size(); i++) {
            Query subquery = (Query) disjuncts.get(i);
            if (subquery instanceof BooleanQuery) {
                buffer.append("(");
                buffer.append(subquery.toString(field));
                buffer.append(")");
            } else buffer.append(subquery.toString(field));
            if (i != disjuncts.size() - 1) buffer.append(" | ");
        }
        buffer.append(")");
        if (tieBreakerMultiplier != 0.0f) {
            buffer.append("~");
            buffer.append(tieBreakerMultiplier);
        }
        if (getBoost() != 1.0) {
            buffer.append("^");
            buffer.append(getBoost());
        }
        return buffer.toString();
    }

    /** Return true iff we represent the same query as o
   * @param o another object
   * @return true iff o is a DisjunctionMaxQuery with the same boost and the same subqueries, in the same order, as us
   */
    public boolean equals(Object o) {
        if (!(o instanceof DisjunctionMaxQuery)) return false;
        DisjunctionMaxQuery other = (DisjunctionMaxQuery) o;
        return this.getBoost() == other.getBoost() && this.tieBreakerMultiplier == other.tieBreakerMultiplier && this.disjuncts.equals(other.disjuncts);
    }

    /** Compute a hash code for hashing us
   * @return the hash code
   */
    public int hashCode() {
        return Float.floatToIntBits(getBoost()) + Float.floatToIntBits(tieBreakerMultiplier) + disjuncts.hashCode();
    }
}
