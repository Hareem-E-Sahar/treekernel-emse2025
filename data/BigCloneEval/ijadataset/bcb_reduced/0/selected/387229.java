package org.galagosearch.core.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>A retrieval evaluator object computes a variety of standard
 * information retrieval metrics commonly used in TREC, including
 * binary preference (BPREF), geometric mean average precision (GMAP),
 * mean average precision (MAP), and standard precision and recall.
 * In addition, the object gives access to the relevant documents that
 * were found, and the relevant documents that were missed.</p>
 *
 * <p>BPREF is defined in Buckley and Voorhees, "Retrieval Evaluation
 * with Incomplete Information", SIGIR 2004.</p>
 *
 * @author Trevor Strohman
 */
public class RetrievalEvaluator {

    /**
     * This class represents a document returned by a retrieval
     * system.  It can be subclassed if you want to add system-dependent
     * information to the document representation.
     */
    public static class Document {

        /**
         * Constructs a new Document object.
         *
         * @param documentNumber The document identifier.
         * @param rank The rank of the document in a retrieved ranked list.
         * @param score The score given to this document by the retrieval system.
         */
        public Document(String documentNumber, int rank, double score) {
            this.documentNumber = documentNumber;
            this.rank = rank;
            this.score = score;
        }

        /**
         * Constructs a new Document object.
         *
         * @param documentNumber The document identifier.
         */
        public Document(String documentNumber) {
            this.documentNumber = documentNumber;
            this.rank = Integer.MAX_VALUE;
            this.score = Double.NEGATIVE_INFINITY;
        }

        /** The rank of the document in a retrieved ranked list. */
        public int rank;

        /** The document identifier. */
        public String documentNumber;

        /** The score given to this document by the retrieval system. */
        public double score;
    }

    /**
     * This class represents a relevance judgment of a particular document
     * for a specific query.
     */
    public static class Judgment {

        /**
         * Constructs a new Judgment instance.
         *
         * @param documentNumber The document identifier.
         * @param judgment The relevance judgment for this document, where positive values mean relevant, and zero means not relevant.
         */
        public Judgment(String documentNumber, int judgment) {
            this.documentNumber = documentNumber;
            this.judgment = judgment;
        }

        /** The document identifier. */
        public String documentNumber;

        /** The relevance judgment for this document, where positive values mean relevant, and zero means not relevant. */
        public int judgment;
    }

    private String _queryName;

    private ArrayList<Document> _retrieved;

    private ArrayList<Document> _judgedMissed;

    private ArrayList<Document> _relevant;

    private ArrayList<Document> _relevantRetrieved;

    private ArrayList<Document> _judgedIrrelevantRetrieved;

    private ArrayList<Document> _irrelevantRetrieved;

    private ArrayList<Document> _relevantMissed;

    private HashMap<String, Judgment> _judgments;

    /**
     * Creates a new instance of RetrievalEvaluator
     *
     * @param retrieved A ranked list of retrieved documents.
     * @param judgments A collection of relevance judgments.
     */
    public RetrievalEvaluator(String queryName, List<Document> retrieved, Collection<Judgment> judgments) {
        _queryName = queryName;
        _retrieved = new ArrayList<Document>(retrieved);
        _buildJudgments(judgments);
        _judgeRetrievedDocuments();
        _findMissedDocuments();
        _findRelevantDocuments();
    }

    private void _buildJudgments(Collection<Judgment> judgments) {
        _judgments = new HashMap<String, Judgment>();
        for (Judgment judgment : judgments) {
            _judgments.put(judgment.documentNumber, judgment);
        }
    }

    private void _judgeRetrievedDocuments() {
        _irrelevantRetrieved = new ArrayList<Document>();
        _relevantRetrieved = new ArrayList<Document>();
        _judgedIrrelevantRetrieved = new ArrayList<Document>();
        for (Document document : _retrieved) {
            boolean relevant = false;
            boolean judgedIrrelevant = false;
            Judgment judgment = _judgments.get(document.documentNumber);
            relevant = (judgment != null) && (judgment.judgment > 0);
            judgedIrrelevant = (judgment != null) && (judgment.judgment <= 0);
            if (relevant) {
                _relevantRetrieved.add(document);
            } else {
                _irrelevantRetrieved.add(document);
                if (judgedIrrelevant) {
                    _judgedIrrelevantRetrieved.add(document);
                }
            }
        }
    }

    private void _findMissedDocuments() {
        HashMap<String, Judgment> missedDocuments = new HashMap<String, Judgment>(_judgments);
        for (Document document : _relevantRetrieved) {
            missedDocuments.remove(document.documentNumber);
        }
        for (Document document : _judgedIrrelevantRetrieved) {
            missedDocuments.remove(document.documentNumber);
        }
        _judgedMissed = new ArrayList<Document>();
        _relevantMissed = new ArrayList<Document>();
        for (Judgment judgment : missedDocuments.values()) {
            Document document = new Document(judgment.documentNumber);
            _judgedMissed.add(document);
            if (judgment.judgment > 0) {
                _relevantMissed.add(document);
            }
        }
    }

    private void _findRelevantDocuments() {
        _relevant = new ArrayList<Document>();
        _relevant.addAll(_relevantRetrieved);
        _relevant.addAll(_relevantMissed);
    }

    /**
     * Returns the name of the query represented by this evaluator.
     */
    public String queryName() {
        return _queryName;
    }

    /**
     * Returns the precision of the retrieval at a given number of documents retrieved.
     * The precision is the number of relevant documents retrieved
     * divided by the total number of documents retrieved.
     *
     * @param documentsRetrieved The evaluation rank.
     */
    public double precision(int documentsRetrieved) {
        return (double) relevantRetrieved(documentsRetrieved) / (double) documentsRetrieved;
    }

    /**
     * Returns the recall of the retrieval at a given number of documents retrieved.
     * The recall is the number of relevant documents retrieved
     * divided by the total number of relevant documents for the query.
     *
     * @param documentsRetrieved The evaluation rank.
     */
    public double recall(int documentsRetrieved) {
        return (double) relevantRetrieved(documentsRetrieved) / (double) _relevant.size();
    }

    /**
     * Returns the precision at the rank equal to the total number of
     * relevant documents retrieved.  This method is equivalent to
     * precision(relevantDocuments().size()).
     */
    public double rPrecision() {
        int relevantCount = _relevant.size();
        int retrievedCount = _retrieved.size();
        if (relevantCount > retrievedCount) {
            return 0;
        }
        return precision(relevantCount);
    }

    /**
     * Returns the reciprocal of the rank of the first relevant document
     * retrieved, or zero if no relevant documents were retrieved.
     */
    public double reciprocalRank() {
        if (_relevantRetrieved.size() == 0) {
            return 0;
        }
        return 1.0 / (double) _relevantRetrieved.get(0).rank;
    }

    /**
     * Returns the average precision of the query.<p>
     *
     * Suppose the precision is evaluated once at the rank of
     * each relevant document in the retrieval.  If a document is
     * not retrieved, we assume that it was retrieved at rank infinity.
     * The mean of all these precision values is the average precision.
     */
    public double averagePrecision() {
        double sumPrecision = 0;
        int relevantCount = 0;
        for (Document document : _relevantRetrieved) {
            relevantCount++;
            sumPrecision += relevantCount / (double) document.rank;
        }
        return (double) sumPrecision / _relevant.size();
    }

    /**
     * <p>The binary preference measure, as presented in Buckley, Voorhees
     * "Retrieval Evaluation with Incomplete Information", SIGIR 2004.
     * This implemenation is the 'pure' version, which is the one
     * used in Buckley's trec_eval.</p>
     *
     * <p>The formula is:
     * <tt>1/R \sum_{r} 1 - |n ranked greater than r| / R</tt>
     * where R is the number of relevant documents for this topic, and
     * n is a member of the set of first R judged irrelevant documents
     * retrieved.</p>
     */
    public double binaryPreference() {
        int totalRelevant = _relevant.size();
        int i = 0;
        int j = 0;
        int irrelevantCount = Math.min(totalRelevant, _judgedIrrelevantRetrieved.size());
        List<Document> irrelevant = _judgedIrrelevantRetrieved.subList(0, irrelevantCount);
        double sum = 0;
        while (i < _relevantRetrieved.size() && j < irrelevant.size()) {
            Document rel = _relevantRetrieved.get(i);
            Document irr = irrelevant.get(j);
            if (rel.rank < irr.rank) {
                assert j <= totalRelevant;
                sum += 1.0 - ((double) j / (double) irrelevantCount);
                i++;
            } else {
                j++;
            }
        }
        return sum / (double) totalRelevant;
    }

    /** 
     * <p>Normalized Discounted Cumulative Gain </p>
     *
     * This measure was introduced in Jarvelin, Kekalainen, "IR Evaluation Methods
     * for Retrieving Highly Relevant Documents" SIGIR 2001.  I copied the formula
     * from Vassilvitskii, "Using Web-Graph Distance for Relevance Feedback in Web
     * Search", SIGIR 2006.
     *
     * Score = N \sum_i (2^{r(i)} - 1) / \log(1 + i)
     *
     * Where N is such that the score cannot be greater than 1.  We compute this
     * by computing the DCG (unnormalized) of a perfect ranking.
     */
    public double normalizedDiscountedCumulativeGain() {
        return normalizedDiscountedCumulativeGain(Math.max(_retrieved.size(), _judgments.size()));
    }

    /** 
     * <p>Normalized Discounted Cumulative Gain </p>
     *
     * This measure was introduced in Jarvelin, Kekalainen, "IR Evaluation Methods
     * for Retrieving Highly Relevant Documents" SIGIR 2001.  I copied the formula
     * from Vassilvitskii, "Using Web-Graph Distance for Relevance Feedback in Web
     * Search", SIGIR 2006.
     *
     * Score = N \sum_i (2^{r(i)} - 1) / \log(1 + i)
     *
     * Where N is such that the score cannot be greater than 1.  We compute this
     * by computing the DCG (unnormalized) of a perfect ranking.
     */
    public double normalizedDiscountedCumulativeGain(int documentsRetrieved) {
        double normalizer = normalizationTermNDCG(documentsRetrieved);
        double dcg = 0;
        List<Document> truncated = _retrieved;
        if (_retrieved.size() > documentsRetrieved) {
            truncated = _retrieved.subList(0, documentsRetrieved);
        }
        for (Document document : truncated) {
            Judgment judgment = _judgments.get(document.documentNumber);
            if (judgment != null && judgment.judgment > 0) {
                dcg += (Math.pow(2, judgment.judgment) - 1.0) / Math.log(1 + document.rank);
            }
        }
        return dcg / normalizer;
    }

    protected double normalizationTermNDCG(int documentsRetrieved) {
        TreeMap<Integer, Integer> relevanceCounts = new TreeMap();
        for (Judgment judgment : _judgments.values()) {
            if (judgment.judgment == 0) {
                continue;
            }
            if (!relevanceCounts.containsKey(-judgment.judgment)) {
                relevanceCounts.put(-judgment.judgment, 0);
            }
            relevanceCounts.put(-judgment.judgment, relevanceCounts.get(-judgment.judgment) + 1);
        }
        double normalizer = 0;
        int countsSoFar = 0;
        int documentsProcessed = 0;
        for (Integer negativeRelevanceValue : relevanceCounts.keySet()) {
            int relevanceCount = (int) relevanceCounts.get(negativeRelevanceValue);
            int relevanceValue = -negativeRelevanceValue;
            relevanceCount = Math.min(relevanceCount, documentsRetrieved - documentsProcessed);
            for (int i = 1; i <= relevanceCount; i++) {
                normalizer += (Math.pow(2, relevanceValue) - 1.0) / Math.log(1 + i + countsSoFar);
            }
            documentsProcessed += relevanceCount;
            if (documentsProcessed >= documentsRetrieved) {
                break;
            }
        }
        return normalizer;
    }

    /**
     * The number of relevant documents retrieved at a particular
     * rank.  This is equivalent to <tt>n * precision(n)</tt>.
     */
    public int relevantRetrieved(int documentsRetrieved) {
        int low = 0;
        int high = _relevantRetrieved.size() - 1;
        if (_relevantRetrieved.size() == 0) {
            return 0;
        }
        Document lastRelevant = _relevantRetrieved.get(high);
        if (lastRelevant.rank <= documentsRetrieved) {
            return _relevantRetrieved.size();
        }
        Document firstRelevant = _relevantRetrieved.get(low);
        if (firstRelevant.rank > documentsRetrieved) {
            return 0;
        }
        while ((high - low) >= 2) {
            int middle = low + (high - low) / 2;
            Document middleDocument = _relevantRetrieved.get(middle);
            if (middleDocument.rank == documentsRetrieved) {
                return middle + 1;
            } else if (middleDocument.rank > documentsRetrieved) {
                high = middle;
            } else {
                low = middle;
            }
        }
        assert _relevantRetrieved.get(low).rank <= documentsRetrieved && _relevantRetrieved.get(high).rank > documentsRetrieved;
        return low + 1;
    }

    /**
     * @return The list of retrieved documents.
     */
    public ArrayList<Document> retrievedDocuments() {
        return _retrieved;
    }

    /**
     * @return The list of all documents retrieved that were explicitly judged irrelevant.
     */
    public ArrayList<Document> judgedIrrelevantRetrievedDocuments() {
        return _judgedIrrelevantRetrieved;
    }

    /**
     * This method returns a list of all documents that were retrieved
     * but assumed to be irrelevant.  This includes both documents that were
     * judged to be irrelevant and those that were not judged at all.
     * The list is returned in retrieval order.
     */
    public ArrayList<Document> irrelevantRetrievedDocuments() {
        return _irrelevantRetrieved;
    }

    /**
     * Returns a list of retrieved documents that were judged relevant,
     * in the order that they were retrieved.
     */
    public ArrayList<Document> relevantRetrievedDocuments() {
        return _relevantRetrieved;
    }

    /**
     * Returns a list of all documents judged relevant, whether they were
     * retrieved or not.  Documents are listed in the order they were retrieved,
     * with those not retrieved coming last.
     */
    public ArrayList<Document> relevantDocuments() {
        return _relevant;
    }

    /**
     * Returns a list of documents that were judged relevant that
     * were not retrieved.
     */
    public ArrayList<Document> relevantMissedDocuments() {
        return _relevantMissed;
    }
}
