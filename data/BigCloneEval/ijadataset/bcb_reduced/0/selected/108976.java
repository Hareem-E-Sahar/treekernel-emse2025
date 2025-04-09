package org.exteca.pattern;

/**
 * Holds the runtime relevance information for a ConceptRule
 * 
 * @author Llewelyn Fernandes
 * @author Mauro Talevi
 */
public class ConceptRelevance {

    /**
	 * The concept relevance is a measure of how much of the concept the
	 * document covers.
	 */
    private int conceptRelevance = 0;

    /**
	 * The document relevance is a measure of how much of the document relates
	 * to the concept.
	 */
    private int documentRelevance = 0;

    /**
	 * The default relevance is a measure of the relative weight of both concept
	 * and document relevances.
	 */
    private int defaultRelevance = 0;

    /** Creates ConceptRelevance */
    public ConceptRelevance(int conceptRelevance, int documentRelevance) {
        this.conceptRelevance = conceptRelevance;
        this.documentRelevance = documentRelevance;
        calculateDefault();
    }

    /**
	 * Calcuates default relevance. Currently, simply an average of concept and
	 * document relevances
	 */
    private void calculateDefault() {
        defaultRelevance = (conceptRelevance + documentRelevance) / 2;
    }

    /**
	 * Returns the concept relevance
	 * 
	 * @return The int
	 */
    public int getConceptRelevance() {
        return conceptRelevance;
    }

    /**
	 * Returns the document relevance
	 * 
	 * @return The int
	 */
    public int getDocumentRelevance() {
        return documentRelevance;
    }

    /**
	 * Returns the default relevance as computed from concept and document
	 * relevances. Currently, simply an average of the two, but can be refined.
	 * 
	 * @return The int
	 */
    public int getDefaultRelevance() {
        return defaultRelevance;
    }

    /**
	 * String representation
	 * 
	 * @return The String representation
	 */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ConceptRelevance conceptRelevance=");
        sb.append(conceptRelevance);
        sb.append(", documentRelevance=");
        sb.append(documentRelevance);
        sb.append(", defaultRelevance=");
        sb.append(defaultRelevance);
        sb.append("]");
        return sb.toString();
    }
}
