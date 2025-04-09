package org.dcopolis.util;

import java.util.*;

@SuppressWarnings("unchecked")
public class Graph<V extends Vertex> {

    LinkedListHashSet<V> vertices;

    public Graph() {
        this(new HashSet<V>());
    }

    public Graph(Set<V> vertices) {
        this.vertices = new LinkedListHashSet<V>(vertices);
        LinkedList<V> test = new LinkedList<V>(vertices);
        while (!test.isEmpty()) {
            V v = test.poll();
            for (Object vv2 : v.getNeighbors()) {
                V v2 = (V) vv2;
                if (!vertices.contains(v2)) {
                    vertices.add(v2);
                    test.add(v2);
                }
            }
        }
    }

    static void dfsVisit(boolean[][] graph, int idx, boolean alreadyVisited[]) {
        if (alreadyVisited[idx]) return;
        alreadyVisited[idx] = true;
        for (int i = 0; i < graph.length; i++) {
            if (graph[idx][i] && !alreadyVisited[i]) dfsVisit(graph, i, alreadyVisited);
        }
    }

    /**
     * Checks whether or not an undirected graph is connected.
     */
    public static boolean isConnected(boolean[][] graph) {
        int n = graph.length;
        boolean alreadyVisited[] = new boolean[n];
        int i;
        for (i = 0; i < n; i++) alreadyVisited[i] = false;
        dfsVisit(graph, 0, alreadyVisited);
        for (i = 0; i < n; i++) {
            if (!alreadyVisited[i]) return false;
        }
        return true;
    }

    /**
     * Calculates the Latora-Marchiori measure of network efficiency.
     */
    public double calculateLMMeasure() {
        if (!isConnected()) return Double.MAX_VALUE;
        Hashtable<Path<V>, Path<V>> apsp = Path.allPairsShortestPath(getVertices().iterator().next());
        int n = size();
        double sum = 0;
        for (Path<V> p : apsp.values()) {
            int d = p.getDistance();
            if (d > 0) sum += 1.0 / (double) d;
        }
        return sum / (double) (n * (n - 1));
    }

    /**
     * Tests whether or not this graph is connected.
     */
    public boolean isConnected() {
        return isConnected(getAdjacency());
    }

    Tree getSpanningTree(Vertex forNode, Tree parent, HashSet<Vertex> history) {
        Tree node = new Tree(parent);
        history.add(forNode);
        node.setLabel(Integer.toString(history.size()));
        for (Vertex n : (Set<Vertex>) forNode.getNeighbors()) {
            if (!history.contains(n)) getSpanningTree(n, node, history);
        }
        return node;
    }

    /**
     * Returns a spanning tree for this graph using a depth-first
     * traversal.  This assumes that the graph is connected.
     */
    public Tree getSpanningTree() {
        return getSpanningTree(vertices.getFirst(), null, new HashSet<Vertex>());
    }

    /**
     * Calculates the average size of a cut in this graph.  The size
     * (sometimes called "weight") of a cut is the number of edges in
     * it.
     */
    public double calculateAverageCutSize() {
        long cutsizetotal = 0;
        int n = size();
        if (n < 2) return 0.0;
        for (int cutVertices = 1; cutVertices < n; cutVertices++) {
            for (Set<V> cut : new CombinationGenerator<V>(getVertices(), cutVertices)) {
                for (V v : cut) for (Object b : v.getNeighbors()) if (!cut.contains(b)) cutsizetotal++;
            }
        }
        return (double) cutsizetotal / Math.pow(2.0, (double) n);
    }

    public static Graph<GraphVertex> newInstance(boolean adjacency[][]) {
        Graph<GraphVertex> graph = new Graph<GraphVertex>();
        GraphVertex vertices[] = new GraphVertex[adjacency.length];
        for (int i = 0; i < adjacency.length; i++) {
            if (vertices[i] == null) {
                vertices[i] = new GraphVertex();
                graph.vertices.add(vertices[i]);
            }
            for (int j = i + 1; j < adjacency[i].length; j++) {
                if (vertices[j] == null) {
                    vertices[j] = new GraphVertex();
                    graph.vertices.add(vertices[j]);
                }
                if (adjacency[i][j] || adjacency[j][i]) vertices[i].addNeighbor(vertices[j]);
            }
        }
        return graph;
    }

    public boolean[][] getAdjacency() {
        int n = vertices.size();
        GraphVertex verts[] = vertices.toArray(new GraphVertex[0]);
        boolean adj[][] = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            adj[i][i] = false;
            for (int j = i + 1; j < n; j++) {
                adj[i][j] = verts[i].getNeighbors().contains(verts[j]) || verts[j].getNeighbors().contains(verts[i]);
                adj[j][i] = adj[i][j];
            }
        }
        return adj;
    }

    public int size() {
        return getVertices().size();
    }

    public Set<V> getVertices() {
        return vertices;
    }

    /**
     * Returns the first vertex returned by
     * <code>getVertices()</code>.  If this graph has no vertices then
     * <code>null</code> is returned.
     */
    public V getVertex() {
        V v = null;
        for (V v2 : getVertices()) {
            v = v2;
            break;
        }
        return v;
    }

    /**
     * Generates a random, undirected, unweighted, connected graph.
     *
     * @param n number of vertices
     * @param p edge probability
     * @param random a {@link java.util.Random} object using which to
     * generate the random edges.
     *
     * @return the adjacency matrix representation of the graph, or
     * <code>null</code> if <code>n&le;0</code>.  The diagonal of the
     * adjacency matrix will always be <code>false</code>
     * (<em>i.e.</em> the graph will have no loops).
     */
    public static boolean[][] randomConnectedGraph(int n, double p, Random random) {
        if (p == 0 && n > 1) return null;
        boolean graph[][];
        do {
            graph = randomGraph(n, p, random);
        } while (!isConnected(graph));
        return graph;
    }

    /**
     * Generates a random, undirected, unweighted, acyclic graph.  In
     * other words, a forest.
     *
     * @param n number of vertices
     * @param p edge probability
     * @param random a {@link java.util.Random} object using which to
     * generate the random edges.
     *
     * @return the adjacency matrix representation of the graph, or
     * <code>null</code> if <code>n&le;0</code>.  The diagonal of the
     * adjacency matrix will always be <code>false</code>
     * (<em>i.e.</em> the graph will have no loops).
     */
    public static boolean[][] randomForest(int n, double p, Random random) {
        if (n <= 0) return null;
        boolean g[][] = new boolean[n][n];
        if (p > 1.0) p = 1.0;
        if (p < 0.0) p = 0.0;
        int connectedComponent[] = new int[n];
        for (int i = 0; i < n; i++) connectedComponent[i] = i;
        for (int i = 0; i < n; i++) {
            g[i][i] = false;
            for (int j = i + 1; j < n; j++) {
                if (connectedComponent[i] == connectedComponent[j]) {
                    g[i][j] = false;
                    g[j][i] = false;
                } else {
                    g[i][j] = (random.nextDouble() <= p);
                    g[j][i] = g[i][j];
                    if (g[i][j]) {
                        int oldComponent = connectedComponent[j];
                        for (int k = 0; k < n; k++) if (connectedComponent[k] == oldComponent) connectedComponent[k] = connectedComponent[i];
                    }
                }
            }
        }
        return g;
    }

    /**
     * Generates a random, undirected, unweighted graph.
     *
     * @param n number of vertices
     * @param p edge probability
     * @param random a {@link java.util.Random} object using which to
     * generate the random edges.
     *
     * @return the adjacency matrix representation of the graph, or
     * <code>null</code> if <code>n&le;0</code>.  The diagonal of the
     * adjacency matrix will always be <code>false</code>
     * (<em>i.e.</em> the graph will have no loops).
     */
    public static boolean[][] randomGraph(int n, double p, Random random) {
        if (n <= 0) return null;
        boolean g[][] = new boolean[n][n];
        if (p > 1.0) p = 1.0;
        if (p < 0.0) p = 0.0;
        for (int i = 0; i < n; i++) {
            g[i][i] = false;
            for (int j = i + 1; j < n; j++) {
                g[i][j] = (random.nextDouble() <= p);
                g[j][i] = g[i][j];
            }
        }
        return g;
    }

    public String toString() {
        boolean adj[][] = getAdjacency();
        String s = "[ ";
        for (int i = 0; i < adj.length; i++) {
            if (i > 0) s += "\n  ";
            s += "[ ";
            for (int j = 0; j < adj[i].length; j++) s += (adj[i][j] ? "1 " : "0 ");
            s += "]";
        }
        return s + " ]";
    }

    public static void main(String[] args) throws Exception {
        for (int n = 1; n <= 18; n++) {
            double total = 0.0;
            long num = 0;
            for (Graph<Tree> t : new FreeTreeGenerator(n)) {
                total += t.calculateAverageCutSize();
                num++;
            }
            System.out.println("Tree size: " + n + "\tAvg: " + (total / (double) num));
        }
    }
}
