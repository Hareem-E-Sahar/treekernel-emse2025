package org.openscience.cdk.ringsearch.cyclebasis;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.alg.ConnectivityInspector;
import org._3pq.jgrapht.graph.SimpleDirectedGraph;
import org._3pq.jgrapht.graph.SimpleGraph;
import org._3pq.jgrapht.graph.Subgraph;
import org.openscience.cdk.graph.BFSShortestPath;
import org.openscience.cdk.graph.MinimalPathIterator;
import java.util.*;

/**
 * Auxiliary class for <code>CycleBasis</code>.
 * 
 * @author Ulrich Bauer <ulrich.bauer@alumni.tum.de>
 * 
 *
 * @cdk.module standard
 * @cdk.githash
 *
 * @cdk.builddepends jgrapht-0.5.3.jar
 * @cdk.depends jgrapht-0.5.3.jar
 */
public class SimpleCycleBasis {

    private List edgeList;

    private List cycles;

    private UndirectedGraph graph;

    private boolean isMinimized = false;

    private HashMap edgeIndexMap;

    public SimpleCycleBasis(List cycles, List edgeList, UndirectedGraph graph) {
        this.edgeList = edgeList;
        this.cycles = cycles;
        this.graph = graph;
        edgeIndexMap = createEdgeIndexMap(edgeList);
    }

    public SimpleCycleBasis(UndirectedGraph graph) {
        this.cycles = new ArrayList();
        this.edgeList = new ArrayList();
        this.graph = graph;
        createMinimumCycleBasis();
    }

    private void createMinimumCycleBasis() {
        Graph subgraph = new Subgraph(graph, null, null);
        Set remainingEdges = new HashSet(graph.edgeSet());
        Set selectedEdges = new HashSet();
        while (!remainingEdges.isEmpty()) {
            Edge edge = (Edge) remainingEdges.iterator().next();
            subgraph.removeEdge(edge);
            List path = BFSShortestPath.findPathBetween(subgraph, edge.getSource(), edge.getTarget());
            path.add(edge);
            SimpleCycle cycle = new SimpleCycle(graph, path);
            subgraph.addEdge(edge);
            selectedEdges.add(edge);
            cycles.add(0, cycle);
            edgeList.add(0, edge);
            remainingEdges.removeAll(path);
        }
        subgraph.removeAllEdges(selectedEdges);
        int startIndex = cycles.size();
        Object currentVertex = graph.vertexSet().iterator().next();
        Graph spanningTree = new Subgraph(graph, new HashSet(), new HashSet());
        Set visitedEdges = new HashSet();
        LinkedList vertexQueue = new LinkedList();
        spanningTree.addVertex(currentVertex);
        vertexQueue.addLast(currentVertex);
        List treeEdges = new Vector();
        while (!vertexQueue.isEmpty()) {
            currentVertex = vertexQueue.removeFirst();
            Iterator edges = subgraph.edgesOf(currentVertex).iterator();
            while (edges.hasNext()) {
                Edge edge = (Edge) edges.next();
                if (!visitedEdges.contains(edge)) {
                    visitedEdges.add(edge);
                    Object nextVertex = edge.oppositeVertex(currentVertex);
                    if (!spanningTree.containsVertex(nextVertex)) {
                        treeEdges.add(edge);
                        spanningTree.addVertex(nextVertex);
                        spanningTree.addEdge(currentVertex, nextVertex);
                        vertexQueue.addLast(nextVertex);
                    } else {
                        List edgesOfCycle = BFSShortestPath.findPathBetween(spanningTree, edge.getSource(), edge.getTarget());
                        edgesOfCycle.add(edge);
                        edgeList.add(edge);
                        SimpleCycle newCycle = new SimpleCycle(graph, edgesOfCycle);
                        cycles.add(newCycle);
                    }
                }
            }
        }
        edgeList.addAll(treeEdges);
        edgeIndexMap = createEdgeIndexMap(edgeList);
        minimize(startIndex);
    }

    boolean[][] getCycleEdgeIncidenceMatrix() {
        return getCycleEdgeIncidenceMatrix((Object[]) cycles.toArray());
    }

    boolean[][] getCycleEdgeIncidenceMatrix(Object[] cycleArray) {
        boolean[][] result = new boolean[cycleArray.length][edgeList.size()];
        for (int i = 0; i < cycleArray.length; i++) {
            SimpleCycle cycle = (SimpleCycle) cycleArray[i];
            for (int j = 0; j < edgeList.size(); j++) {
                Edge edge = (Edge) edgeList.get(j);
                result[i][j] = cycle.containsEdge(edge);
            }
        }
        return result;
    }

    private void minimize(int startIndex) {
        if (isMinimized) return;
        boolean[][] a = getCycleEdgeIncidenceMatrix();
        for (int i = startIndex; i < cycles.size(); i++) {
            boolean[] u = constructKernelVector(edgeList.size(), a, i);
            AuxiliaryGraph gu = new AuxiliaryGraph(graph, u);
            SimpleCycle shortestCycle = (SimpleCycle) cycles.get(i);
            Iterator vertexIterator = graph.vertexSet().iterator();
            while (vertexIterator.hasNext()) {
                Object vertex = vertexIterator.next();
                boolean shouldSearchCycle = false;
                Collection incidentEdges = graph.edgesOf(vertex);
                Iterator edgeIterator = incidentEdges.iterator();
                while (edgeIterator.hasNext()) {
                    Edge edge = (Edge) edgeIterator.next();
                    int index = getEdgeIndex(edge);
                    if (u[index]) {
                        shouldSearchCycle = true;
                        break;
                    }
                }
                if (shouldSearchCycle) {
                    Object auxVertex0 = gu.auxVertex0(vertex);
                    Object auxVertex1 = gu.auxVertex1(vertex);
                    List auxPath = BFSShortestPath.findPathBetween(gu, auxVertex0, auxVertex1);
                    List edgesOfNewCycle = new Vector();
                    Object v = vertex;
                    edgeIterator = auxPath.iterator();
                    while (edgeIterator.hasNext()) {
                        Edge auxEdge = (Edge) edgeIterator.next();
                        Edge e = (Edge) gu.edge(auxEdge);
                        if (edgesOfNewCycle.contains(e)) {
                            edgesOfNewCycle.remove(e);
                        } else {
                            edgesOfNewCycle.add(e);
                        }
                        v = e.oppositeVertex(v);
                    }
                    SimpleCycle newCycle = new SimpleCycle(graph, edgesOfNewCycle);
                    if (newCycle.weight() < shortestCycle.weight()) {
                        shortestCycle = newCycle;
                    }
                }
            }
            cycles.set(i, shortestCycle);
            for (int j = 0; j < edgeList.size(); j++) {
                a[i][j] = shortestCycle.containsEdge((Edge) edgeList.get(j));
            }
            for (int j = 0; j < i; j++) {
                if (a[i][j]) {
                    for (int k = 0; k < edgeList.size(); k++) {
                        a[i][k] = (a[i][k] != a[j][k]);
                    }
                }
            }
        }
        isMinimized = true;
    }

    static boolean[] constructKernelVector(int size, boolean[][] a, int i) {
        boolean[] u = new boolean[size];
        u[i] = true;
        for (int j = i - 1; j >= 0; j--) {
            u[j] = false;
            for (int k = i; k > j; k--) {
                u[j] = (u[j] != (a[j][k] && u[k]));
            }
        }
        return u;
    }

    public int[] weightVector() {
        int[] result = new int[cycles.size()];
        for (int i = 0; i < cycles.size(); i++) {
            SimpleCycle cycle = (SimpleCycle) cycles.get(i);
            result[i] = (int) cycle.weight();
        }
        Arrays.sort(result);
        return result;
    }

    public List edges() {
        return edgeList;
    }

    public List cycles() {
        return cycles;
    }

    static boolean[][] inverseBinaryMatrix(boolean[][] m, int n) {
        boolean[][] a = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = m[i][j];
            }
        }
        boolean[][] r = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            r[i][i] = true;
        }
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (a[j][i]) {
                    for (int k = 0; k < n; k++) {
                        if ((k != j) && (a[k][i])) {
                            for (int l = 0; l < n; l++) {
                                a[k][l] = (a[k][l] != a[j][l]);
                                r[k][l] = (r[k][l] != r[j][l]);
                            }
                        }
                    }
                    if (i != j) {
                        boolean[] swap = a[i];
                        a[i] = a[j];
                        a[j] = swap;
                        swap = r[i];
                        r[i] = r[j];
                        r[j] = swap;
                    }
                    break;
                }
            }
        }
        return r;
    }

    public Collection essentialCycles() {
        Collection result = new HashSet();
        boolean[][] a = getCycleEdgeIncidenceMatrix();
        boolean[][] ai = inverseBinaryMatrix(a, cycles.size());
        for (int i = 0; i < cycles.size(); i++) {
            boolean[] u = new boolean[edgeList.size()];
            for (int j = 0; j < cycles.size(); j++) {
                u[j] = ai[j][i];
            }
            AuxiliaryGraph gu = new AuxiliaryGraph(graph, u);
            boolean isEssential = true;
            Iterator vertexIterator = graph.vertexSet().iterator();
            while (isEssential && vertexIterator.hasNext()) {
                Object vertex = vertexIterator.next();
                Collection incidentEdges = graph.edgesOf(vertex);
                boolean shouldSearchCycle = false;
                for (Iterator it = incidentEdges.iterator(); it.hasNext(); ) {
                    Edge edge = (Edge) it.next();
                    int index = getEdgeIndex(edge);
                    if (u[index]) {
                        shouldSearchCycle = true;
                        break;
                    }
                }
                if (shouldSearchCycle) {
                    Object auxVertex0 = gu.auxVertex0(vertex);
                    Object auxVertex1 = gu.auxVertex1(vertex);
                    for (Iterator minPaths = new MinimalPathIterator(gu, auxVertex0, auxVertex1); minPaths.hasNext(); ) {
                        List auxPath = (List) minPaths.next();
                        List edgesOfNewCycle = new ArrayList(auxPath.size());
                        for (Iterator it = auxPath.iterator(); it.hasNext(); ) {
                            Edge auxEdge = (Edge) it.next();
                            Edge e = (Edge) gu.edge(auxEdge);
                            if (edgesOfNewCycle.contains(e)) {
                                edgesOfNewCycle.remove(e);
                            } else {
                                edgesOfNewCycle.add(e);
                            }
                        }
                        SimpleCycle cycle = new SimpleCycle(graph, edgesOfNewCycle);
                        if (cycle.weight() > ((SimpleCycle) cycles.get(i)).weight()) {
                            break;
                        }
                        if (!cycle.equals((SimpleCycle) cycles.get(i))) {
                            isEssential = false;
                            break;
                        }
                    }
                }
            }
            if (isEssential) {
                result.add((SimpleCycle) cycles.get(i));
            }
        }
        return result;
    }

    public Map relevantCycles() {
        Map result = new HashMap();
        boolean[][] a = getCycleEdgeIncidenceMatrix();
        boolean[][] ai = inverseBinaryMatrix(a, cycles.size());
        for (int i = 0; i < cycles.size(); i++) {
            boolean[] u = new boolean[edgeList.size()];
            for (int j = 0; j < cycles.size(); j++) {
                u[j] = ai[j][i];
            }
            AuxiliaryGraph gu = new AuxiliaryGraph(graph, u);
            Iterator vertexIterator = graph.vertexSet().iterator();
            while (vertexIterator.hasNext()) {
                Object vertex = vertexIterator.next();
                Collection incidentEdges = graph.edgesOf(vertex);
                boolean shouldSearchCycle = false;
                for (Iterator it = incidentEdges.iterator(); it.hasNext(); ) {
                    Edge edge = (Edge) it.next();
                    int index = getEdgeIndex(edge);
                    if (u[index]) {
                        shouldSearchCycle = true;
                        break;
                    }
                }
                if (shouldSearchCycle) {
                    Object auxVertex0 = gu.auxVertex0(vertex);
                    Object auxVertex1 = gu.auxVertex1(vertex);
                    for (Iterator minPaths = new MinimalPathIterator(gu, auxVertex0, auxVertex1); minPaths.hasNext(); ) {
                        List auxPath = (List) minPaths.next();
                        List edgesOfNewCycle = new ArrayList(auxPath.size());
                        Iterator edgeIterator = auxPath.iterator();
                        while (edgeIterator.hasNext()) {
                            Edge auxEdge = (Edge) edgeIterator.next();
                            Edge e = (Edge) gu.edge(auxEdge);
                            if (edgesOfNewCycle.contains(e)) {
                                edgesOfNewCycle.remove(e);
                            } else {
                                edgesOfNewCycle.add(e);
                            }
                        }
                        SimpleCycle cycle = new SimpleCycle(graph, edgesOfNewCycle);
                        if (cycle.weight() > ((SimpleCycle) cycles.get(i)).weight()) {
                            break;
                        }
                        result.put(cycle, (SimpleCycle) cycles.get(i));
                    }
                }
            }
        }
        return result;
    }

    public List equivalenceClasses() {
        int[] weight = weightVector();
        Object[] cyclesArray = (Object[]) cycles.toArray();
        Arrays.sort(cyclesArray, new Comparator() {

            public int compare(Object o1, Object o2) {
                return (int) (((SimpleCycle) o1).weight() - ((SimpleCycle) o2).weight());
            }
        });
        Collection essentialCycles = essentialCycles();
        boolean[][] u = new boolean[cyclesArray.length][edgeList.size()];
        boolean[][] a = getCycleEdgeIncidenceMatrix(cyclesArray);
        boolean[][] ai = inverseBinaryMatrix(a, cyclesArray.length);
        for (int i = 0; i < cyclesArray.length; i++) {
            for (int j = 0; j < cyclesArray.length; j++) {
                u[i][j] = ai[j][i];
            }
        }
        UndirectedGraph h = new SimpleGraph();
        h.addAllVertices(cycles);
        ConnectivityInspector connectivityInspector = new ConnectivityInspector(h);
        int left = 0;
        for (int right = 0; right < weight.length; right++) {
            if ((right < weight.length - 1) && (weight[right + 1] == weight[right])) continue;
            for (int i = left; i <= right; i++) {
                if (essentialCycles.contains((SimpleCycle) cyclesArray[i])) continue;
                for (int j = i + 1; j <= right; j++) {
                    if (essentialCycles.contains((SimpleCycle) cyclesArray[j])) continue;
                    if (connectivityInspector.pathExists(cyclesArray[i], cyclesArray[j])) continue;
                    boolean sameClass = false;
                    AuxiliaryGraph2 auxGraph = new AuxiliaryGraph2(graph, edgeList, u[i], u[j]);
                    for (Iterator it = graph.vertexSet().iterator(); it.hasNext(); ) {
                        Object vertex = it.next();
                        boolean shouldSearchCycle = false;
                        Collection incidentEdges = graph.edgesOf(vertex);
                        Iterator edgeIterator = incidentEdges.iterator();
                        while (edgeIterator.hasNext()) {
                            Edge edge = (Edge) edgeIterator.next();
                            int index = getEdgeIndex(edge);
                            if (u[i][index] || u[j][index]) {
                                shouldSearchCycle = true;
                                break;
                            }
                        }
                        if (shouldSearchCycle) {
                            Object auxVertex00 = auxGraph.auxVertex00(vertex);
                            Object auxVertex11 = auxGraph.auxVertex11(vertex);
                            List auxPath = BFSShortestPath.findPathBetween(auxGraph, auxVertex00, auxVertex11);
                            double pathWeight = auxPath.size();
                            if (pathWeight == weight[left]) {
                                sameClass = true;
                                break;
                            }
                        }
                    }
                    if (sameClass) {
                        h.addEdge(cyclesArray[i], cyclesArray[j]);
                    }
                }
            }
            for (int i = left; i <= right; i++) {
                if (essentialCycles.contains((SimpleCycle) cyclesArray[i])) continue;
                for (int j = i + 1; j <= right; j++) {
                    if (essentialCycles.contains((SimpleCycle) cyclesArray[j])) continue;
                    if (connectivityInspector.pathExists(cyclesArray[i], cyclesArray[j])) continue;
                    boolean sameClass = false;
                    for (int k = 0; ((SimpleCycle) cyclesArray[k]).weight() < weight[left]; k++) {
                        AuxiliaryGraph2 auxGraph = new AuxiliaryGraph2(graph, edgeList, u[i], u[k]);
                        boolean shortestPathFound = false;
                        for (Iterator it = graph.vertexSet().iterator(); it.hasNext(); ) {
                            Object vertex = it.next();
                            Object auxVertex00 = auxGraph.auxVertex00(vertex);
                            Object auxVertex11 = auxGraph.auxVertex11(vertex);
                            List auxPath = BFSShortestPath.findPathBetween(auxGraph, auxVertex00, auxVertex11);
                            double pathWeight = auxPath.size();
                            if (pathWeight == weight[left]) {
                                shortestPathFound = true;
                                break;
                            }
                        }
                        if (!shortestPathFound) continue;
                        auxGraph = new AuxiliaryGraph2(graph, edgeList, u[j], u[k]);
                        for (Iterator it = graph.vertexSet().iterator(); it.hasNext(); ) {
                            Object vertex = it.next();
                            Object auxVertex00 = auxGraph.auxVertex00(vertex);
                            Object auxVertex11 = auxGraph.auxVertex11(vertex);
                            List auxPath = BFSShortestPath.findPathBetween(auxGraph, auxVertex00, auxVertex11);
                            double pathWeight = auxPath.size();
                            if (pathWeight == weight[left]) {
                                sameClass = true;
                                break;
                            }
                        }
                        if (sameClass) break;
                    }
                    if (sameClass) {
                        h.addEdge(cyclesArray[i], cyclesArray[j]);
                    }
                }
            }
            left = right + 1;
        }
        return connectivityInspector.connectedSets();
    }

    private HashMap createEdgeIndexMap(List edgeList) {
        HashMap map = new HashMap();
        for (int i = 0; i < edgeList.size(); i++) {
            map.put(edgeList.get(i), Integer.valueOf(i));
        }
        return map;
    }

    private int getEdgeIndex(Edge edge) {
        return ((Integer) edgeIndexMap.get(edge)).intValue();
    }

    private class AuxiliaryGraph extends SimpleGraph {

        private static final long serialVersionUID = 857337988734567429L;

        HashMap vertexMap0 = new HashMap();

        HashMap vertexMap1 = new HashMap();

        HashMap auxVertexMap = new HashMap();

        Map auxEdgeMap = new HashMap();

        Graph g;

        boolean[] u;

        AuxiliaryGraph(Graph graph, boolean[] u) {
            g = graph;
            this.u = u;
        }

        public List edgesOf(Object auxVertex) {
            Object vertex = auxVertexMap.get(auxVertex);
            for (Iterator edgeIterator = g.edgesOf(vertex).iterator(); edgeIterator.hasNext(); ) {
                Edge edge = (Edge) edgeIterator.next();
                int j = getEdgeIndex(edge);
                Object vertex1 = edge.getSource();
                Object vertex2 = edge.getTarget();
                if (u[j]) {
                    Object vertex1u = auxVertex0(vertex1);
                    Object vertex2u = auxVertex1(vertex2);
                    Edge auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex1(vertex1);
                    vertex2u = auxVertex0(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                } else {
                    Object vertex1u = auxVertex0(vertex1);
                    Object vertex2u = auxVertex0(vertex2);
                    Edge auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex1(vertex1);
                    vertex2u = auxVertex1(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                }
            }
            return super.edgesOf(auxVertex);
        }

        Object auxVertex0(Object vertex) {
            if (vertexMap0.get(vertex) == null) {
                Object newVertex0 = vertex + "-0";
                vertexMap0.put(vertex, newVertex0);
                addVertex(newVertex0);
                auxVertexMap.put(newVertex0, vertex);
                return newVertex0;
            }
            return vertexMap0.get(vertex);
        }

        Object auxVertex1(Object vertex) {
            if (vertexMap1.get(vertex) == null) {
                Object newVertex1 = vertex + "-1";
                vertexMap1.put(vertex, newVertex1);
                addVertex(newVertex1);
                auxVertexMap.put(newVertex1, vertex);
                return newVertex1;
            }
            return vertexMap1.get(vertex);
        }

        Object edge(Object auxEdge) {
            return auxEdgeMap.get(auxEdge);
        }
    }

    private class AuxiliaryGraph2 extends SimpleGraph {

        private static final long serialVersionUID = 5930876716644738726L;

        private HashMap vertexMap00 = new HashMap();

        private HashMap vertexMap01 = new HashMap();

        private HashMap vertexMap10 = new HashMap();

        private HashMap vertexMap11 = new HashMap();

        private HashMap auxVertexMap = new HashMap();

        private Map auxEdgeMap = new HashMap();

        private Graph g;

        private boolean[] ui;

        private boolean[] uj;

        AuxiliaryGraph2(Graph graph, List edgeList, boolean[] ui, boolean[] uj) {
            g = graph;
            this.ui = ui;
            this.uj = uj;
        }

        Object auxVertex00(Object vertex) {
            if (vertexMap00.get(vertex) == null) {
                Object newVertex = vertex + "-00";
                vertexMap00.put(vertex, newVertex);
                addVertex(newVertex);
                auxVertexMap.put(newVertex, vertex);
                return newVertex;
            }
            return vertexMap00.get(vertex);
        }

        Object auxVertex01(Object vertex) {
            if (vertexMap01.get(vertex) == null) {
                Object newVertex = vertex + "-01";
                vertexMap01.put(vertex, newVertex);
                addVertex(newVertex);
                auxVertexMap.put(newVertex, vertex);
                return newVertex;
            }
            return vertexMap01.get(vertex);
        }

        Object auxVertex10(Object vertex) {
            if (vertexMap10.get(vertex) == null) {
                Object newVertex = vertex + "-10";
                vertexMap10.put(vertex, newVertex);
                addVertex(newVertex);
                auxVertexMap.put(newVertex, vertex);
                return newVertex;
            }
            return vertexMap10.get(vertex);
        }

        Object auxVertex11(Object vertex) {
            if (vertexMap11.get(vertex) == null) {
                Object newVertex = vertex + "-11";
                vertexMap11.put(vertex, newVertex);
                addVertex(newVertex);
                auxVertexMap.put(newVertex, vertex);
                return newVertex;
            }
            return vertexMap11.get(vertex);
        }

        public List edgesOf(Object auxVertex) {
            Object vertex = auxVertexMap.get(auxVertex);
            for (Iterator edgeIterator = g.edgesOf(vertex).iterator(); edgeIterator.hasNext(); ) {
                Edge edge = (Edge) edgeIterator.next();
                int k = getEdgeIndex(edge);
                Object vertex1 = edge.getSource();
                Object vertex2 = edge.getTarget();
                if (!ui[k] && !uj[k]) {
                    Object vertex1u = auxVertex00(vertex1);
                    Object vertex2u = auxVertex00(vertex2);
                    Edge auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex01(vertex1);
                    vertex2u = auxVertex01(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex10(vertex1);
                    vertex2u = auxVertex10(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex11(vertex1);
                    vertex2u = auxVertex11(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                } else if (ui[k] && !uj[k]) {
                    Object vertex1u = auxVertex00(vertex1);
                    Object vertex2u = auxVertex10(vertex2);
                    Edge auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex01(vertex1);
                    vertex2u = auxVertex11(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex10(vertex1);
                    vertex2u = auxVertex00(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex11(vertex1);
                    vertex2u = auxVertex01(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                } else if (!ui[k] && uj[k]) {
                    Object vertex1u = auxVertex00(vertex1);
                    Object vertex2u = auxVertex01(vertex2);
                    Edge auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex01(vertex1);
                    vertex2u = auxVertex00(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex10(vertex1);
                    vertex2u = auxVertex11(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex11(vertex1);
                    vertex2u = auxVertex10(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                } else if (ui[k] && uj[k]) {
                    Object vertex1u = auxVertex00(vertex1);
                    Object vertex2u = auxVertex11(vertex2);
                    Edge auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex01(vertex1);
                    vertex2u = auxVertex10(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex10(vertex1);
                    vertex2u = auxVertex01(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                    vertex1u = auxVertex11(vertex1);
                    vertex2u = auxVertex00(vertex2);
                    auxEdge = addEdge(vertex1u, vertex2u);
                    auxEdgeMap.put(auxEdge, edge);
                }
            }
            return super.edgesOf(auxVertex);
        }
    }

    public void printIncidenceMatrix() {
        boolean[][] incidMatr = getCycleEdgeIncidenceMatrix();
        for (int i = 0; i < incidMatr.length; i++) {
            for (int j = 0; j < incidMatr[i].length; j++) {
                System.out.print(incidMatr[i][j] ? 1 : 0);
            }
            System.out.println();
        }
    }
}
