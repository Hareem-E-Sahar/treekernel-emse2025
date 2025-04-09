package edu.cmu.cs.bungee.client.query.tetrad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import edu.cmu.cs.bungee.client.query.ItemPredicate;
import edu.cmu.cs.bungee.javaExtensions.PerspectiveObserver;
import edu.cmu.cs.bungee.javaExtensions.Util;
import edu.cmu.cs.bungee.javaExtensions.graph.Edge;
import edu.cmu.cs.bungee.javaExtensions.graph.Graph;
import edu.cmu.cs.bungee.javaExtensions.graph.Node;
import edu.cmu.cs.bungee.javaExtensions.graph.Graph.GraphWeigher;

public class GraphicalModel extends Distribution {

    /**
	 * If weights get too big, predicted probabilities go to zero, and KL and z
	 * go to infinity.
	 */
    protected static final double MAX_WEIGHT = 15;

    private boolean edgesFixed = false;

    private static final boolean isSymmetric = true;

    /**
	 * {node1, node2) => weight
	 */
    private final double[] weights;

    /**
	 * Non-existent edges are coded -2; uncached are coded -1
	 */
    private final double[] expWeights;

    private double[] energies;

    private double[] expEnergies;

    private final int[][] edgeIndexes;

    private final int[][] edgeStates;

    private final double[] logDistribution;

    private double z = -1;

    private final int nEdges;

    private final int nEdgesPlusBiases;

    GraphicalModel(List<ItemPredicate> facets, Set<SimpleEdge> edges, boolean isSymmetric, int count) {
        super(facets, count);
        assert isSymmetric;
        energies = new double[nStates()];
        expEnergies = new double[nStates()];
        Arrays.fill(expEnergies, 1);
        logDistribution = new double[nStates()];
        edgeIndexes = getEdgeIndexes();
        if (edges == null) edges = allEdges(facets, facets);
        assert !isSymmetric || isEdgesCanonical(edges);
        setEdges(edges);
        nEdges = edges.size();
        nEdgesPlusBiases = nEdges + nFacets;
        weights = new double[nEdgesPlusBiases];
        expWeights = new double[nEdgesPlusBiases];
        Arrays.fill(expWeights, 1);
        edgeStates = edgeStates();
        resetWeights();
    }

    public double[] getLogDistribution() {
        return logDistribution;
    }

    private boolean isEdgesCanonical(Set<SimpleEdge> edges) {
        for (Iterator<SimpleEdge> it = edges.iterator(); it.hasNext(); ) {
            SimpleEdge edge = it.next();
            assert edge.p1.compareTo(edge.p2) < 0 : edges + " " + edge;
        }
        return true;
    }

    int nUsedFacets() {
        int result = 0;
        for (Iterator<ItemPredicate> it = facets.iterator(); it.hasNext(); ) {
            ItemPredicate p = it.next();
            if (nEdges(p) > 0) result++;
        }
        return result;
    }

    List<ItemPredicate> unusedFacets() {
        List<ItemPredicate> result = new LinkedList<ItemPredicate>();
        for (Iterator<ItemPredicate> it = facets.iterator(); it.hasNext(); ) {
            ItemPredicate p = it.next();
            if (nEdges(p) == 0) result.add(p);
        }
        return result;
    }

    int nEdges(ItemPredicate p) {
        int result = 0;
        for (Iterator<SimpleEdge> it = getEdges(false).iterator(); it.hasNext(); ) {
            SimpleEdge edge = it.next();
            if (edge.p1 == p || edge.p2 == p) {
                result++;
            }
        }
        return result;
    }

    protected boolean hasEdge(ItemPredicate cause, ItemPredicate caused) {
        int causeNode = facetIndexOrNot(cause);
        if (causeNode < 0) return false;
        int causedNode = facetIndexOrNot(caused);
        if (causedNode < 0) return false;
        return edgeIndexes[causeNode][causedNode] >= 0;
    }

    protected boolean hasEdge(int causeNode, int causedNode) {
        return edgeIndexes[causeNode][causedNode] >= 0;
    }

    double getExpWeightOrZero(ItemPredicate cause, ItemPredicate caused) {
        return hasEdge(cause, caused) ? getExpWeight(facetIndex(cause), facetIndex(caused)) : 1;
    }

    private double getExpWeight(int i, int j) {
        double result = expWeights[edgeIndexes[i][j]];
        assert result >= 0 : result + " " + i + " " + j + " " + getWeight(i, j);
        assert !Double.isNaN(result);
        assert !Double.isInfinite(result);
        return result;
    }

    protected double getWeight(ItemPredicate caused, ItemPredicate caused2) {
        return getWeight(facetIndex(caused), facetIndex(caused2));
    }

    double getWeightOrZero(ItemPredicate cause, ItemPredicate caused) {
        return hasEdge(cause, caused) ? getWeight(facetIndex(cause), facetIndex(caused)) : 0;
    }

    protected double getWeight(int cause, int caused) {
        assert hasEdge(cause, caused) : cause + " " + caused + " " + facets() + " " + Util.valueOfDeep(expWeights);
        double result = weights[edgeIndexes[cause][caused]];
        return result;
    }

    protected double getStdDevNormalizedWeight(ItemPredicate p1, ItemPredicate p2) {
        return getWeight(p1, p2) * stdDev(p1);
    }

    protected double[] getWeights() {
        return Util.copy(weights);
    }

    protected boolean setWeights(double[] argument) {
        boolean result = false;
        decacheOdds();
        Arrays.fill(energies, 0);
        Arrays.fill(expEnergies, 1);
        for (int edgeIndex = 0; edgeIndex < nEdgesPlusBiases; edgeIndex++) {
            double weight = argument[edgeIndex];
            if (weights[edgeIndex] != weight) {
                result = true;
            }
            double expWeight = Math.exp(weight);
            assert expWeight > 0 && !Double.isNaN(expWeight) && !Double.isInfinite(expWeight) : weights[edgeIndex] + " => " + weight + "\n" + Util.valueOfDeep(argument);
            weights[edgeIndex] = weight;
            expWeights[edgeIndex] = expWeight;
            int[] statesAffected = edgeStates[edgeIndex];
            int nsa = statesAffected.length;
            for (int j = 0; j < nsa; j++) {
                int state = statesAffected[j];
                energies[state] += weight;
                expEnergies[state] *= expWeight;
            }
        }
        if (result) updateLogPredictedDistribution();
        return result;
    }

    protected boolean setWeight(ItemPredicate p, ItemPredicate p2, double weight) {
        return setWeight(facetIndex(p), facetIndex(p2), weight);
    }

    protected void resetWeights() {
        setWeights(getWeights());
        updateLogPredictedDistribution();
    }

    /**
	 * This just "remembers" the weight; MUST call resetWeights afterwards to
	 * cache other info.
	 */
    protected boolean setWeight(int cause, int caused, double weight) {
        assert !Double.isInfinite(weight);
        assert !Double.isNaN(weight);
        int edgeIndex = edgeIndexes[cause][caused];
        boolean result = weights[edgeIndex] != weight;
        weights[edgeIndex] = weight;
        return result;
    }

    private void addEdge(ItemPredicate cause, ItemPredicate caused) {
        addEdge(facetIndex(cause), facetIndex(caused));
    }

    private void addEdge(int cause, int caused) {
        assert !edgesFixed;
        assert cause != caused : cause + " => " + caused + " " + this;
        assert !hasEdge(cause, caused);
        edgeIndexes[cause][caused] = 1;
        edgeIndexes[caused][cause] = 1;
    }

    /**
	 * Just add biases; setEdges will set the others
	 */
    int[][] getEdgeIndexes() {
        int[][] edgeIndexes1 = new int[nFacets][];
        for (int cause = 0; cause < nFacets; cause++) {
            edgeIndexes1[cause] = new int[nFacets];
            Arrays.fill(edgeIndexes1[cause], -1);
            edgeIndexes1[cause][cause] = cause;
        }
        return edgeIndexes1;
    }

    void removeEdge(int cause, int caused) {
        assert hasEdge(cause, caused);
        assert !edgesFixed;
        edgeIndexes[cause][caused] = -1;
        edgeIndexes[caused][cause] = -1;
    }

    /**
	 * @return [cause, caused] in this order [0, 1], [0, 2], [0, 3], [1, 2], [1,
	 *         3], [2, 3]
	 * 
	 *         i.e. for (int cause = 0; cause < nFacets; cause++) { for (int
	 *         caused = cause; caused <nFacets; caused++) {
	 * 
	 *         for xvec, these follow the bias weights
	 */
    protected EdgeIterator getEdgeIterator() {
        return new EdgeIterator();
    }

    class EdgeIterator implements Iterator<int[]> {

        int cause = 0;

        int caused = -1;

        int nextCause = -1;

        int nextCaused = -1;

        public boolean hasNext() {
            if (nextCause < 0) {
                nextCause = cause;
                nextCaused = caused + 1;
                for (; nextCause < nFacets; nextCause++) {
                    for (; nextCaused < nFacets; nextCaused++) {
                        if (nextCause != nextCaused && hasEdge(nextCause, nextCaused) && (!isSymmetric || nextCause < nextCaused)) return true;
                    }
                    nextCaused = 0;
                }
            }
            return nextCause < nFacets;
        }

        public int[] next() {
            if (hasNext()) {
                cause = nextCause;
                caused = nextCaused;
                nextCause = -1;
                int[] edge = { cause, caused };
                return edge;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            assert false;
            if (caused > 0) {
                removeEdge(cause, caused);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private int[][] stateWeights;

    protected int[][] stateWeights() {
        if (stateWeights == null) {
            stateWeights = new int[nStates()][];
            for (int state = 0; state < stateWeights.length; state++) {
                int[] wts = new int[0];
                int argIndex = nFacets;
                for (int cause = 0; cause < nFacets; cause++) {
                    for (int caused = cause; caused < nFacets; caused++) {
                        if (hasEdge(cause, caused)) {
                            if (Util.isBit(state, caused)) {
                                if (cause == caused) {
                                    wts = Util.push(wts, cause);
                                } else if (Util.isBit(state, cause)) {
                                    wts = Util.push(wts, argIndex);
                                }
                            }
                            if (caused > cause) argIndex++;
                        }
                    }
                }
                stateWeights[state] = wts;
            }
        }
        return stateWeights;
    }

    private int[][] edgeStates() {
        int[][] edgeStates1 = new int[nEdgesPlusBiases][];
        int nStates = nStates();
        int[] tempStates = new int[nStates];
        for (int cause = 0; cause < nFacets; cause++) {
            for (int caused = cause; caused < nFacets; caused++) {
                if (hasEdge(cause, caused)) {
                    int stateIndex = 0;
                    for (int state = 0; state < nStates; state++) {
                        if (Util.isBit(state, cause) && Util.isBit(state, caused)) {
                            tempStates[stateIndex++] = state;
                        }
                    }
                    int[] es = new int[stateIndex];
                    System.arraycopy(tempStates, 0, es, 0, stateIndex);
                    int edgeIndex = edgeIndexes[cause][caused];
                    edgeStates1[edgeIndex] = es;
                }
            }
        }
        return edgeStates1;
    }

    double effectiveWeight(ItemPredicate cause, ItemPredicate caused) {
        if (NonAlchemyModel.USE_SIGMOID) {
            double expw = getExpWeightOrZero(cause, caused);
            assert Util.approxEquals(expw, Math.exp(getWeightOrZero(cause, caused))) : expw + " " + Math.exp(getWeightOrZero(cause, caused));
            return expw / (expw + 1);
        } else {
            return getWeightOrZero(cause, caused);
        }
    }

    public static String formatWeight(double weight) {
        if (Double.isNaN(weight)) return "?";
        return Integer.toString((int) Math.rint(100 * weight));
    }

    protected void updateLogPredictedDistribution() {
        int nStates = nStates();
        double logZ = Math.log(z());
        for (int state = 0; state < nStates; state++) {
            logDistribution[state] = energy(state) - logZ;
        }
    }

    /**
	 * Computes distribution as a side effect
	 */
    private double z() {
        int nStates = nStates();
        z = Util.kahanSum(expEnergies);
        if (Double.isInfinite(z) || Double.isNaN(z)) {
            z = 0;
            for (int state = 0; state < nStates; state++) {
                if (Double.isInfinite(expEnergies[state])) {
                    z++;
                    distribution[state] = 1;
                } else {
                    distribution[state] = 0;
                }
            }
            for (int state = 0; state < nStates; state++) {
                distribution[state] /= z;
            }
        } else for (int state = 0; state < nStates; state++) {
            distribution[state] = expEnergies[state] / z;
        }
        assert checkDist(distribution);
        assert z >= 0 : z;
        return z;
    }

    private double energy(int state) {
        assert !Double.isNaN(energies[state]) && !Double.isInfinite(energies[state]) : Util.valueOfDeep(energies);
        return energies[state];
    }

    @Override
    public Distribution getMarginalDistribution(List<ItemPredicate> subFacets) {
        return new Distribution(subFacets, getMarginalCounts(subFacets));
    }

    private void setEdges(Set<SimpleEdge> edges) {
        for (Iterator<SimpleEdge> it = edges.iterator(); it.hasNext(); ) {
            SimpleEdge edge = it.next();
            ItemPredicate cause = edge.p1;
            ItemPredicate caused = edge.p2;
            assert cause != caused : "Biases are implicit";
            addEdge(cause, caused);
        }
        int edgeIndex = nFacets;
        for (int cause = 0; cause < nFacets; cause++) {
            for (int caused = cause + 1; caused < nFacets; caused++) {
                if (hasEdge(cause, caused)) {
                    edgeIndexes[caused][cause] = edgeIndex;
                    edgeIndexes[cause][caused] = edgeIndex++;
                }
            }
        }
        edgesFixed = true;
    }

    /**
	 * @param nullModel
	 *            this is just to label the "before" weights.
	 * @param debug
	 */
    protected Graph<ItemPredicate> buildGraph(double[] Rs, double[][] RnormalizedWeights, double KL, Explanation nullModel, PerspectiveObserver redrawer, boolean debug) {
        Graph<ItemPredicate> graph = new Graph<ItemPredicate>((GraphWeigher<ItemPredicate>) null);
        Map<ItemPredicate, Node<ItemPredicate>> nodeMap = new HashMap<ItemPredicate, Node<ItemPredicate>>();
        for (Iterator<ItemPredicate> it = facets.iterator(); it.hasNext(); ) {
            ItemPredicate p = it.next();
            ensureNode(Rs, graph, nodeMap, p, redrawer);
        }
        for (Iterator<int[]> it = getEdgeIterator(); it.hasNext(); ) {
            int[] edge = it.next();
            ItemPredicate cause = getFacet(edge[0]);
            ItemPredicate caused = getFacet(edge[1]);
            addEdge(Rs, RnormalizedWeights, graph, nodeMap, cause, caused, nullModel, redrawer, debug);
            if (isSymmetric) addEdge(Rs, RnormalizedWeights, graph, nodeMap, caused, cause, nullModel, redrawer, debug);
        }
        assert !graph.getNodes().isEmpty() : printGraph(Rs, RnormalizedWeights, KL);
        return graph;
    }

    protected String printGraph(double[] Rs, double[][] RnormalizedWeights, double KL) {
        Util.print("printGraph " + this + " KL=" + KL);
        for (Iterator<ItemPredicate> it = facets().iterator(); it.hasNext(); ) {
            ItemPredicate caused = it.next();
            Util.print(getWeight(caused, caused) + " (" + Rs[facetIndex(caused)] + ") " + caused);
        }
        for (Iterator<int[]> it = getEdgeIterator(); it.hasNext(); ) {
            int[] edge = it.next();
            ItemPredicate cause = getFacet(edge[0]);
            ItemPredicate caused = getFacet(edge[1]);
            double weight = effectiveWeight(cause, caused);
            Util.print(weight + " (" + RnormalizedWeights[facetIndex(caused)][facetIndex(cause)] + ") " + cause + " => (" + RnormalizedWeights[facetIndex(cause)][facetIndex(caused)] + ") " + caused);
        }
        Util.print("");
        return "";
    }

    private Node<ItemPredicate> ensureNode(double[] Rs, Graph<ItemPredicate> graph, Map<ItemPredicate, Node<ItemPredicate>> nodeMap, ItemPredicate facet, PerspectiveObserver redrawer) {
        assert graph != null;
        assert facet != null;
        Node<ItemPredicate> result = nodeMap.get(facet);
        if (result == null) {
            String label = redrawer == null ? facet.toString() : facet.toString(redrawer);
            label = formatWeight(Rs[facetIndex(facet)]) + " " + label;
            result = graph.addNode(facet, " " + label);
            nodeMap.put(facet, result);
        }
        assert result != null : facet;
        return result;
    }

    private void addEdge(double[] Rs, double[][] RnormalizedWeights, Graph<ItemPredicate> graph, Map<ItemPredicate, Node<ItemPredicate>> nodeMap, ItemPredicate cause, ItemPredicate caused, Explanation nullModel, PerspectiveObserver redrawer, boolean debug) {
        Node<ItemPredicate> posNode = ensureNode(Rs, graph, nodeMap, caused, redrawer);
        Node<ItemPredicate> negNode = ensureNode(Rs, graph, nodeMap, cause, redrawer);
        Edge<ItemPredicate> edge = graph.getEdge(posNode, negNode);
        if (edge == null) edge = graph.addEdge((String) null, posNode, negNode);
        int causeIndex = facetIndex(cause);
        int causedIndex = facetIndex(caused);
        double forwardWeight = RnormalizedWeights[causeIndex][causedIndex];
        if (debug) {
            String label = formatWeight(forwardWeight);
            if (nullModel.facets().contains(cause) && nullModel.facets().contains(caused)) label = formatWeight(nullModel.getRNormalizedWeight(cause, caused)) + " > " + label;
            edge.setLabel("        " + label + "        ", posNode);
            edge.setLabel(formatWeight(getWeight(cause, caused)) + " (" + formatWeight(effectiveWeight(cause, caused)) + ")", Edge.CENTER_LABEL);
        } else {
            double backwardWeight = RnormalizedWeights[causedIndex][causeIndex];
            double averageWeight = (forwardWeight + backwardWeight) / 2;
            edge.setLabel(formatWeight(averageWeight), Edge.CENTER_LABEL);
        }
    }

    /**
	 * @param causes
	 * @param causeds
	 * @return [[cause1, caused1], ... Does not return biases
	 */
    protected static Set<SimpleEdge> allEdges(Collection<ItemPredicate> causes, Collection<ItemPredicate> causeds) {
        HashSet<SimpleEdge> result = new HashSet<SimpleEdge>();
        for (Iterator<ItemPredicate> it1 = causeds.iterator(); it1.hasNext(); ) {
            ItemPredicate caused = it1.next();
            for (Iterator<ItemPredicate> it2 = causes.iterator(); it2.hasNext(); ) {
                ItemPredicate cause = it2.next();
                if (caused != cause) {
                    result.add(SimpleEdge.getInstance(cause, caused));
                }
            }
        }
        return result;
    }

    protected int[][] edgesToIndexes(Set<SimpleEdge> edges) {
        List<int[]> intEdges = new ArrayList<int[]>(edges.size());
        for (Iterator<SimpleEdge> it = edges.iterator(); it.hasNext(); ) {
            SimpleEdge edge = it.next();
            int[] intEdge = new int[2];
            intEdge[0] = facetIndex(edge.p1);
            intEdge[1] = facetIndex(edge.p2);
            intEdges.add(intEdge);
        }
        return intEdges.toArray(new int[0][]);
    }

    protected Set<SimpleEdge> getEdges(boolean includeBiases) {
        Set<SimpleEdge> result = getEdgesAmong(facets);
        if (includeBiases) {
            for (Iterator<ItemPredicate> it = facets.iterator(); it.hasNext(); ) {
                ItemPredicate p = it.next();
                result.add(SimpleEdge.getInstance(p, p));
            }
        }
        return result;
    }

    private Set<SimpleEdge> getEdgesAmong(List<ItemPredicate> prevfacets) {
        HashSet<SimpleEdge> result = new HashSet<SimpleEdge>();
        for (Iterator<int[]> it = getEdgeIterator(); it.hasNext(); ) {
            int[] edge = it.next();
            ItemPredicate cause = getFacet(edge[0]);
            ItemPredicate caused = getFacet(edge[1]);
            assert caused != cause;
            if (prevfacets.contains(cause) && prevfacets.contains(caused)) {
                result.add(SimpleEdge.getInstance(cause, caused));
            }
        }
        return result;
    }

    static class SimpleEdge {

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
            result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            SimpleEdge other = (SimpleEdge) obj;
            if (p1 == null) {
                if (other.p1 != null) return false;
            } else if (!p1.equals(other.p1)) return false;
            if (p2 == null) {
                if (other.p2 != null) return false;
            } else if (!p2.equals(other.p2)) return false;
            return true;
        }

        ItemPredicate p1;

        ItemPredicate p2;

        static SimpleEdge getInstance(ItemPredicate cause, ItemPredicate caused) {
            if (cause.compareTo(caused) < 0) {
                return new SimpleEdge(cause, caused);
            } else {
                return new SimpleEdge(caused, cause);
            }
        }

        SimpleEdge(ItemPredicate cause, ItemPredicate caused) {
            this.p1 = cause;
            this.p2 = caused;
        }

        @Override
        public String toString() {
            return "<SimpleEdge " + p1 + ", " + p2 + ">";
        }
    }

    protected static SimpleEdge getEdge(ItemPredicate cause, ItemPredicate caused) {
        if (cause.compareTo(caused) < 0) {
            return new SimpleEdge(cause, caused);
        } else {
            return new SimpleEdge(caused, cause);
        }
    }

    protected static Collection<SimpleEdge> getEdgesTo(Collection<ItemPredicate> x, ItemPredicate caused) {
        Collection<SimpleEdge> result = new ArrayList<SimpleEdge>(x.size());
        for (Iterator<ItemPredicate> it = x.iterator(); it.hasNext(); ) {
            ItemPredicate p = it.next();
            result.add(SimpleEdge.getInstance(p, caused));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<").append(Util.shortClassName(this)).append(" ").append(facets);
        if (true) {
            buf.append(" nEdges=").append(nEdges);
        } else {
            buf.append(getEdges(false));
        }
        buf.append(" ").append(Util.valueOfDeep(getCounts())).append(">");
        return buf.toString();
    }

    protected int nEdges() {
        return nEdges;
    }

    protected int getNumEdgesPlusBiases() {
        return nEdgesPlusBiases;
    }

    protected double bigWeightPenalty() {
        double result = 0;
        for (int edgeIndex = 0; edgeIndex < nEdgesPlusBiases; edgeIndex++) {
            double excess = Math.abs(weights[edgeIndex]) - MAX_WEIGHT;
            if (excess > 0) result += excess * excess;
        }
        return result;
    }

    protected void bigWeightGradient(double[] gradient) {
        int nWeights = weights.length;
        for (int edgeIndex = 0; edgeIndex < nWeights; edgeIndex++) {
            double w = weights[edgeIndex];
            double excess = Math.abs(w) - MAX_WEIGHT;
            gradient[edgeIndex] = excess > 0 ? 2 * excess * Util.sgn(w) : 0;
            assert !Double.isNaN(gradient[edgeIndex]) : excess + " " + w;
        }
    }
}
