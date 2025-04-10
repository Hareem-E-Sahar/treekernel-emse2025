package algo.graph.dynamicflow;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import algo.graph.staticflow.StaticTransshipment;
import algo.graph.util.PathDecomposition;
import ds.graph.Edge;
import ds.graph.IdentifiableCollection;
import ds.mapping.IdentifiableIntegerMapping;
import ds.graph.network.AbstractNetwork;
import ds.graph.Node;
import ds.graph.flow.PathBasedFlow;
import ds.graph.flow.StaticPathFlow;
import algo.graph.Flags;
import ds.graph.GraphLocalization;

/** 
 * Calculates an upper bound for the time horizon needed
 * to fulfill all supplies and demands.
 */
public class TransshipmentBoundEstimator {

    public static int calculateBoundByLongestPath(AbstractNetwork network, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Node> supplies) {
        int upperBoundForLongestPath = 0;
        int neededFlow = 0;
        IdentifiableCollection<Edge> originalEdges = network.edges();
        LinkedList<Integer> edgesList = new LinkedList<Integer>();
        int minCap = Integer.MAX_VALUE;
        for (Edge edge : originalEdges) {
            edgesList.add(transitTimes.get(edge));
            if (edgeCapacities.get(edge) < minCap) minCap = edgeCapacities.get(edge);
        }
        Collections.sort(edgesList, Collections.reverseOrder());
        Iterator<Integer> it = edgesList.iterator();
        for (int i = 0; i < network.nodes().size(); i++) {
            if (it.hasNext()) upperBoundForLongestPath += it.next();
            int s = supplies.get(network.nodes().get(i));
            if (s > 0) neededFlow += s;
        }
        int timeNeededWhileFlowing = (int) Math.ceil(neededFlow / minCap);
        return (upperBoundForLongestPath + timeNeededWhileFlowing + 1);
    }

    public static int calculateBoundByStaticMaxFlows(AbstractNetwork network, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Node> supplies) {
        if (Flags.ALGO_PROGRESS) {
            System.out.println("Bound calculation by static max flow started.");
        }
        LinkedList<Node> sources = new LinkedList<Node>();
        LinkedList<Node> sinks = new LinkedList<Node>();
        int maxSupply = 0;
        Node sink = null;
        for (Node node : network.nodes()) {
            if (supplies.get(node) < 0) {
                if (sink != null) throw new AssertionError(GraphLocalization.getSingleton().getString("algo.graph.dynamicflow.OnlyOneSinkException"));
                if (sink == null) sink = node;
            }
            if (supplies.get(node) > 0) {
                sources.add(node);
                if (supplies.get(node) > maxSupply) maxSupply = supplies.get(node);
            }
        }
        sinks.add(sink);
        IdentifiableIntegerMapping<Node> restSupplies = new IdentifiableIntegerMapping<Node>(supplies.getDomainSize());
        for (Node n : network.nodes()) {
            restSupplies.set(n, supplies.get(n));
        }
        int maxLength = 0;
        int sumOfMaxSupplies = 0;
        IdentifiableIntegerMapping<Node> flowLeavingSource;
        do {
            flowLeavingSource = new IdentifiableIntegerMapping<Node>(supplies.getDomainSize());
            int maxSupplyInThisRun = 0;
            if (Flags.BOUND_ESTIMATOR_STATIC_FLOW) {
                System.out.println("New run");
            }
            if (Flags.ALGO_PROGRESS) {
                System.out.println("New run of the max flow algorithm starts.");
            }
            StaticTransshipment staticTransshipment = new StaticTransshipment(network, edgeCapacities, restSupplies);
            staticTransshipment.run();
            IdentifiableIntegerMapping<Edge> staticFlow = staticTransshipment.getFlowEvenIfNotFeasible();
            for (Edge e : network.edges()) {
                Edge f = network.getEdge(e.end(), e.start());
                if (f != null) {
                    int m = Math.min(staticFlow.get(e), staticFlow.get(f));
                    staticFlow.decrease(e, m);
                    staticFlow.decrease(f, m);
                }
            }
            PathBasedFlow staticFlowAsPaths = PathDecomposition.calculatePathDecomposition(network, supplies, sources, sinks, staticFlow);
            for (StaticPathFlow staticPathFlow : staticFlowAsPaths) {
                int length = 0;
                if (Flags.BOUND_ESTIMATOR_STATIC_FLOW) {
                    System.out.println("Source: " + staticPathFlow.firstEdge().start() + " Amount: " + staticPathFlow.getAmount());
                }
                if (Flags.ALGO_PROGRESS) {
                    System.out.println("Removed " + staticPathFlow.getAmount() + " supply from " + staticPathFlow.firstEdge().start() + ". ");
                }
                restSupplies.decrease(staticPathFlow.firstEdge().start(), staticPathFlow.getAmount());
                flowLeavingSource.increase(staticPathFlow.firstEdge().start(), staticPathFlow.getAmount());
                int hasLeft = flowLeavingSource.get(staticPathFlow.firstEdge().start());
                if (hasLeft > maxSupplyInThisRun) {
                    maxSupplyInThisRun = hasLeft;
                }
                if (restSupplies.get(staticPathFlow.firstEdge().start()) == 0) {
                    sources.remove(staticPathFlow.firstEdge().start());
                }
                restSupplies.increase(sink, staticPathFlow.getAmount());
                if (Flags.BOUND_ESTIMATOR_STATIC_FLOW) {
                    System.out.println("Sink: " + restSupplies.get(sink));
                }
                for (Edge edge : staticPathFlow) {
                    length += transitTimes.get(edge);
                }
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            if (Flags.BOUND_ESTIMATOR_STATIC_FLOW) {
                System.out.println("max supply in this run " + maxSupplyInThisRun);
                System.out.println("max length " + maxLength);
            }
            sumOfMaxSupplies += maxSupplyInThisRun;
            if (Flags.BOUND_ESTIMATOR_STATIC_FLOW) {
                System.out.println(sources);
            }
            if (Flags.ALGO_PROGRESS) {
                System.out.println("Max flow run finished. Remaining supplies: " + (-restSupplies.get(sink)));
            }
        } while (restSupplies.get(sink) < 0);
        if (Flags.BOUND_ESTIMATOR_STATIC_FLOW) {
            System.out.println("sum of max " + sumOfMaxSupplies);
        }
        return maxLength + sumOfMaxSupplies + 1;
    }

    public static int calculateBoundByStaticTransshipmentAndScaleFactorSearch(AbstractNetwork network, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Node> supplies) {
        Node sink = null;
        IdentifiableIntegerMapping<Node> oneSupplies = new IdentifiableIntegerMapping<Node>(supplies.getDomainSize());
        LinkedList<Node> sources = new LinkedList<Node>();
        LinkedList<Node> sinks = new LinkedList<Node>();
        int maxSupply = 0;
        for (Node node : network.nodes()) {
            if (supplies.get(node) < 0) {
                if (sink != null) throw new AssertionError(GraphLocalization.getSingleton().getString("algo.graph.dynamicflow.OnlyOneSinkException"));
                if (sink == null) sink = node;
            }
            if (supplies.get(node) > 0) {
                oneSupplies.set(node, 1);
                sources.add(node);
                if (supplies.get(node) > maxSupply) maxSupply = supplies.get(node);
            }
        }
        oneSupplies.set(sink, -sources.size());
        sinks.add(sink);
        int upperBound = sources.size() + 1;
        int left = 1, right = upperBound;
        StaticTransshipment staticTransshipment = null;
        IdentifiableIntegerMapping<Edge> staticFlow = null;
        IdentifiableIntegerMapping<Edge> resultStaticFlow = null;
        staticTransshipment = new StaticTransshipment(network, edgeCapacities, oneSupplies);
        staticTransshipment.run();
        staticFlow = staticTransshipment.getFlow();
        boolean found = false;
        int nonFeasibleT = 0;
        int feasibleT = -1;
        if (staticFlow == null) nonFeasibleT = 1; else {
            nonFeasibleT = 0;
            feasibleT = 1;
            found = true;
        }
        while (!found) {
            int testScaleFactor = (nonFeasibleT * 2);
            if (testScaleFactor >= upperBound) {
                feasibleT = upperBound;
                found = true;
            } else {
                IdentifiableIntegerMapping<Edge> multipliedCapacities = new IdentifiableIntegerMapping<Edge>(edgeCapacities.getDomainSize());
                for (Edge edge : network.edges()) {
                    int eCap = edgeCapacities.get(edge);
                    if (eCap >= Math.floor(Integer.MAX_VALUE / testScaleFactor)) multipliedCapacities.set(edge, Integer.MAX_VALUE); else multipliedCapacities.set(edge, edgeCapacities.get(edge) * testScaleFactor);
                }
                staticTransshipment = new StaticTransshipment(network, multipliedCapacities, oneSupplies);
                staticTransshipment.run();
                staticFlow = staticTransshipment.getFlow();
                if (staticFlow == null) nonFeasibleT = testScaleFactor; else {
                    feasibleT = testScaleFactor;
                    found = true;
                }
            }
        }
        left = nonFeasibleT;
        right = Math.min(feasibleT + 1, upperBound);
        do {
            int testScaleFactor = (left + right) / 2;
            IdentifiableIntegerMapping<Edge> multipliedCapacities = new IdentifiableIntegerMapping<Edge>(edgeCapacities.getDomainSize());
            for (Edge edge : network.edges()) {
                int eCap = edgeCapacities.get(edge);
                if (eCap >= Math.floor(Integer.MAX_VALUE / testScaleFactor)) multipliedCapacities.set(edge, Integer.MAX_VALUE); else multipliedCapacities.set(edge, edgeCapacities.get(edge) * testScaleFactor);
            }
            staticTransshipment = new StaticTransshipment(network, multipliedCapacities, oneSupplies);
            staticTransshipment.run();
            staticFlow = staticTransshipment.getFlow();
            if (staticFlow == null) left = testScaleFactor; else {
                right = testScaleFactor;
                resultStaticFlow = staticFlow;
            }
        } while (left < right - 1);
        if (left == right - 1 && resultStaticFlow != null) {
            PathBasedFlow pathFlows = PathDecomposition.calculatePathDecomposition(network, supplies, sources, sinks, staticFlow);
            int maxLength = 0;
            for (StaticPathFlow staticPathFlow : pathFlows) {
                int length = 0;
                for (Edge edge : staticPathFlow) {
                    length += transitTimes.get(edge);
                }
                if (length > maxLength) maxLength = length;
            }
            if (Flags.BOUND_ESTIMATOR_LONG) {
                System.out.println("Path decomposition: " + pathFlows);
            }
            if (Flags.BOUND_ESTIMATOR) {
                System.out.println();
                System.out.println("Max Length: " + maxLength + " Max Supply: " + maxSupply + " Sum:" + (maxLength + maxSupply) * sources.size());
                System.out.println();
            }
            return ((maxLength + maxSupply) * right + 1);
        }
        throw new AssertionError("Binary search found no working testScaleFactor.");
    }

    public static int calculateBoundByStaticTransshipment(AbstractNetwork network, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Node> supplies) {
        Node sink = null;
        IdentifiableIntegerMapping<Node> oneSupplies = new IdentifiableIntegerMapping<Node>(supplies.getDomainSize());
        LinkedList<Node> sources = new LinkedList<Node>();
        LinkedList<Node> sinks = new LinkedList<Node>();
        int maxSupply = 0;
        for (Node node : network.nodes()) {
            if (supplies.get(node) < 0) {
                if (sink != null) throw new AssertionError(GraphLocalization.getSingleton().getString("algo.graph.dynamicflow.OnlyOneSinkException"));
                if (sink == null) sink = node;
            }
            if (supplies.get(node) > 0) {
                oneSupplies.set(node, 1);
                sources.add(node);
                if (supplies.get(node) > maxSupply) maxSupply = supplies.get(node);
            }
        }
        oneSupplies.set(sink, -sources.size());
        sinks.add(sink);
        IdentifiableIntegerMapping<Edge> multipliedCapacities = new IdentifiableIntegerMapping<Edge>(edgeCapacities.getDomainSize());
        for (Edge edge : network.edges()) {
            int eCap = edgeCapacities.get(edge);
            if (eCap >= Math.floor(Integer.MAX_VALUE / sources.size())) multipliedCapacities.set(edge, Integer.MAX_VALUE); else multipliedCapacities.set(edge, edgeCapacities.get(edge) * sources.size());
        }
        if (Flags.BOUND_ESTIMATOR_LONG) {
            System.out.println();
            System.out.println();
            System.out.println("network: " + network);
            System.out.println("capacities: " + edgeCapacities);
            System.out.println("multipliedCapacities: " + multipliedCapacities);
            System.out.println("supplies: " + supplies);
            System.out.println("oneSupplies " + oneSupplies);
        }
        if (Flags.ALGO_PROGRESS) {
            System.out.println("Progress: Static transshipment algorithm is called for calculation of upper bound for time horizon..");
        }
        StaticTransshipment staticTransshipment = new StaticTransshipment(network, multipliedCapacities, oneSupplies);
        staticTransshipment.run();
        IdentifiableIntegerMapping<Edge> staticFlow = staticTransshipment.getFlow();
        if (Flags.ALGO_PROGRESS) {
            System.out.println("Progress: .. call of static transshipment algorithm finished.");
        }
        if (Flags.BOUND_ESTIMATOR_LONG) System.out.println("Calculated static flow for upper bound: " + staticFlow);
        PathBasedFlow pathFlows = PathDecomposition.calculatePathDecomposition(network, supplies, sources, sinks, staticFlow);
        int maxLength = 0;
        for (StaticPathFlow staticPathFlow : pathFlows) {
            int length = 0;
            for (Edge edge : staticPathFlow) {
                length += transitTimes.get(edge);
            }
            if (length > maxLength) maxLength = length;
        }
        if (Flags.BOUND_ESTIMATOR_LONG) {
            System.out.println("Path decomposition: " + pathFlows);
        }
        if (Flags.BOUND_ESTIMATOR) {
            System.out.println();
            System.out.println("Max Length: " + maxLength + " Max Supply: " + maxSupply + " Sum:" + (maxLength + maxSupply) * sources.size());
            System.out.println();
        }
        return ((maxLength + maxSupply) * sources.size() + 1);
    }

    public static int calculateBound(AbstractNetwork network, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Edge> edgeCapacities, IdentifiableIntegerMapping<Node> supplies) {
        if (Flags.BOUND_ESTIMATOR) {
            System.out.println("");
        }
        int c = Integer.MAX_VALUE;
        int a = Integer.MAX_VALUE;
        int b = calculateBoundByLongestPath(network, transitTimes, edgeCapacities, supplies);
        int d = Integer.MAX_VALUE;
        System.out.println("Bounds calculated: " + a + " " + b + " " + c + " " + d);
        return Math.min(a, Math.min(b, c));
    }
}
