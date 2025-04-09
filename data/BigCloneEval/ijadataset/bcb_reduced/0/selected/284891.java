package dr.evolution.distance;

import dr.evolution.alignment.PatternList;
import dr.evolution.datatype.DataType;
import dr.evolution.util.Taxon;
import dr.evolution.util.TaxonList;
import dr.matrix.Matrix;
import java.util.*;

/**
 * storage for pairwise distance matrices.<p>
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: DistanceMatrix.java,v 1.23 2005/07/11 14:06:25 rambaut Exp $
 */
public class DistanceMatrix extends Matrix.AbstractMatrix implements TaxonList {

    public static final double MAX_DISTANCE = 1000.0;

    /**
     * constructor
     */
    public DistanceMatrix() {
        super();
    }

    /**
     * constructor taking a dimension
     */
    public DistanceMatrix(TaxonList taxa) {
        super();
        this.taxa = taxa;
        dimension = taxa.getTaxonCount();
        distances = new double[dimension][dimension];
        distancesKnown = true;
    }

    /**
     * constructor taking a pattern source
     */
    public DistanceMatrix(PatternList patterns) {
        super();
        setPatterns(patterns);
    }

    /**
     * set the pattern source
     */
    public void setPatterns(PatternList patterns) {
        this.taxa = patterns;
        this.patterns = patterns;
        dimension = patterns.getTaxonCount();
        dataType = patterns.getDataType();
        distancesKnown = false;
    }

    /**
     * @return the number of rows
     */
    public int getRowCount() {
        return dimension;
    }

    /**
     * @return the number of columns
     */
    public int getColumnCount() {
        return dimension;
    }

    /**
     * @return an element
     */
    public double getElement(int row, int column) {
        if (!distancesKnown) {
            calculateDistances();
        }
        return distances[row][column];
    }

    /**
     * set an element - this overwrites any existing elements
     */
    public void setElement(int row, int column, double value) {
        if (!distancesKnown) {
            calculateDistances();
        }
        distances[row][column] = value;
    }

    /**
     * Calculate the distances
     */
    public void calculateDistances() {
        distances = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i + 1; j < dimension; j++) {
                distances[i][j] = calculatePairwiseDistance(i, j);
                distances[j][i] = distances[i][j];
            }
            distances[i][i] = 0.0;
        }
        distancesKnown = true;
    }

    /**
     * Calculate a pairwise distance
     */
    protected double calculatePairwiseDistance(int taxon1, int taxon2) {
        int state1, state2;
        int n = patterns.getPatternCount();
        double weight, distance;
        double sumDistance = 0.0;
        double sumWeight = 0.0;
        int[] pattern;
        for (int i = 0; i < n; i++) {
            pattern = patterns.getPattern(i);
            state1 = pattern[taxon1];
            state2 = pattern[taxon2];
            weight = patterns.getPatternWeight(i);
            if (!dataType.isAmbiguousState(state1) && !dataType.isAmbiguousState(state2) && state1 != state2) {
                sumDistance += weight;
            }
            sumWeight += weight;
        }
        distance = sumDistance / sumWeight;
        return distance;
    }

    /**
     * Returns the mean pairwise distance of this matrix
     */
    public double getMeanDistance() {
        if (!distancesKnown) {
            calculateDistances();
        }
        double dist = 0.0;
        int count = 0;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (i != j) {
                    dist += distances[i][j];
                    count += 1;
                }
            }
        }
        return dist / (double) count;
    }

    public String toString() {
        try {
            double[] dists = getUpperTriangle();
            StringBuffer buffer = new StringBuffer(String.valueOf(dists[0]));
            for (int i = 1; i < dists.length; i++) {
                buffer.append(", ").append(String.valueOf(dists[i]));
            }
            return buffer.toString();
        } catch (Matrix.NotSquareException e) {
            return e.toString();
        }
    }

    /**
     * @return a count of the number of taxa in the list.
     */
    public int getTaxonCount() {
        return taxa.getTaxonCount();
    }

    /**
     * @return the ith taxon.
     */
    public Taxon getTaxon(int taxonIndex) {
        return taxa.getTaxon(taxonIndex);
    }

    /**
     * @return the ID of the ith taxon.
     */
    public String getTaxonId(int taxonIndex) {
        return taxa.getTaxonId(taxonIndex);
    }

    /**
     * returns the index of the taxon with the given id.
     */
    public int getTaxonIndex(String id) {
        return taxa.getTaxonIndex(id);
    }

    /**
     * returns the index of the given taxon.
     */
    public int getTaxonIndex(Taxon taxon) {
        return taxa.getTaxonIndex(taxon);
    }

    public List<Taxon> asList() {
        List<Taxon> taxa = new ArrayList<Taxon>();
        for (int i = 0, n = getTaxonCount(); i < n; i++) {
            taxa.add(getTaxon(i));
        }
        return taxa;
    }

    public Iterator<Taxon> iterator() {
        return new Iterator<Taxon>() {

            private int index = -1;

            public boolean hasNext() {
                return index < getTaxonCount() - 1;
            }

            public Taxon next() {
                index++;
                return getTaxon(index);
            }

            public void remove() {
            }
        };
    }

    /**
     * @param taxonIndex the index of the taxon whose attribute is being fetched.
     * @param name       the name of the attribute of interest.
     * @return an object representing the named attributed for the given taxon.
     */
    public Object getTaxonAttribute(int taxonIndex, String name) {
        return taxa.getTaxonAttribute(taxonIndex, name);
    }

    protected String id = null;

    /**
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     */
    public void setId(String id) {
        this.id = id;
    }

    protected DataType dataType = null;

    int dimension = 0;

    boolean distancesKnown;

    private double[][] distances = null;

    protected PatternList patterns = null;

    private TaxonList taxa = null;
}
