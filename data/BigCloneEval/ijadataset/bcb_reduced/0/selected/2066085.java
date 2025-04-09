package edu.yale.csgp.vitapad.algorithm;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import edu.yale.csgp.vitapad.graph.PathwayGraph;
import edu.yale.csgp.vitapad.graph.PathwayVertex;

/**
 * An implementation of Kamada and Kawai's spring embedded layout
 * algorithm.  Courtesy of GINY project.
 */
public class SpringEmbeddedLayout {

    public static final int DEFAULT_NUM_LAYOUT_PASSES = 2;

    public static final double DEFAULT_AVERAGE_ITERATIONS_PER_NODE = 20.0;

    public static final double[] DEFAULT_NODE_DISTANCE_SPRING_SCALARS = new double[] { 1.0, 1.0 };

    public static final double DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT = 15.0;

    public static final double DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT = 200.0;

    public static final double DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH = .05;

    public static final double DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH = 250.0;

    public static final double[] DEFAULT_ANTICOLLISION_SPRING_SCALARS = new double[] { 0.0, 1.0 };

    public static final double DEFAULT_ANTICOLLISION_SPRING_STRENGTH = 100.0;

    protected int numLayoutPasses = DEFAULT_NUM_LAYOUT_PASSES;

    protected double averageIterationsPerNode = DEFAULT_AVERAGE_ITERATIONS_PER_NODE;

    protected double[] nodeDistanceSpringScalars = DEFAULT_NODE_DISTANCE_SPRING_SCALARS;

    protected double nodeDistanceStrengthConstant = DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT;

    protected double nodeDistanceRestLengthConstant = DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT;

    protected double disconnectedNodeDistanceSpringStrength = DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH;

    protected double disconnectedNodeDistanceSpringRestLength = DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH;

    protected double[][] nodeDistanceSpringStrengths;

    protected double[][] nodeDistanceSpringRestLengths;

    protected double[] anticollisionSpringScalars = DEFAULT_ANTICOLLISION_SPRING_SCALARS;

    protected double anticollisionSpringStrength = DEFAULT_ANTICOLLISION_SPRING_STRENGTH;

    protected PathwayGraph pGraph;

    protected int vertexCount;

    protected int edgeCount;

    protected int layoutPass;

    public SpringEmbeddedLayout(PathwayGraph pGraph) {
        setGraph(pGraph);
        initializeSpringEmbeddedLayouter();
    }

    public void setGraph(PathwayGraph newPathwayGraph) {
        pGraph = newPathwayGraph;
    }

    public PathwayGraph getPathwayGraph() {
        for (int i = 0; i < pGraph.getVertexCount(); i++) {
            PathwayVertex pv = (PathwayVertex) pGraph.getVertexList().get(i);
        }
        return pGraph;
    }

    protected void initializeSpringEmbeddedLayouter() {
    }

    public void doLayout() {
        vertexCount = pGraph.getVertexCount();
        edgeCount = pGraph.getEdgeCount();
        double euclidean_distance_threshold = (0.5 * (vertexCount + edgeCount));
        double potential_energy_percent_change_threshold = .001;
        int num_iterations = (int) ((vertexCount * averageIterationsPerNode) / numLayoutPasses);
        List partials_list = createPartialsList();
        PotentialEnergy potential_energy = new PotentialEnergy();
        ListIterator vertexIterator;
        PathwayVertex vertex;
        PartialDerivatives partials;
        PartialDerivatives furthest_node_partials = null;
        double current_progress_temp;
        double setup_progress = 0.0;
        for (layoutPass = 0; layoutPass < numLayoutPasses; layoutPass++) {
            setupForLayoutPass();
            potential_energy.reset();
            partials_list.clear();
            vertexIterator = pGraph.getVertexIterator();
            while (vertexIterator.hasNext()) {
                vertex = (PathwayVertex) vertexIterator.next();
                partials = new PartialDerivatives(vertex);
                calculatePartials(partials, null, potential_energy, false);
                partials_list.add(partials);
                if ((furthest_node_partials == null) || (partials.euclideanDistance > furthest_node_partials.euclideanDistance)) {
                    furthest_node_partials = partials;
                }
            }
            for (int iterations_i = 0; ((iterations_i < num_iterations) && (furthest_node_partials.euclideanDistance >= euclidean_distance_threshold)); iterations_i++) {
                furthest_node_partials = moveNode(furthest_node_partials, partials_list, potential_energy);
            }
        }
    }

    /**
     * Called at the beginning of each layoutPass iteration.
     */
    protected void setupForLayoutPass() {
        setupNodeDistanceSprings();
    }

    protected void setupNodeDistanceSprings() {
        if (layoutPass != 0) {
            return;
        }
        nodeDistanceSpringRestLengths = new double[vertexCount][vertexCount];
        nodeDistanceSpringStrengths = new double[vertexCount][vertexCount];
        if (nodeDistanceSpringScalars[layoutPass] == 0.0) {
            return;
        }
        NodeDistances ind = new NodeDistances(pGraph.getVertexList(), null, pGraph);
        int[][] node_distances = (int[][]) ind.calculate();
        if (node_distances == null) {
            return;
        }
        double node_distance_strength_constant = nodeDistanceStrengthConstant;
        double node_distance_rest_length_constant = nodeDistanceRestLengthConstant;
        for (int node_i = 0; node_i < vertexCount; node_i++) {
            for (int node_j = (node_i + 1); node_j < vertexCount; node_j++) {
                if (node_distances[node_i][node_j] == Integer.MAX_VALUE) {
                    nodeDistanceSpringRestLengths[node_i][node_j] = disconnectedNodeDistanceSpringRestLength;
                } else {
                    nodeDistanceSpringRestLengths[node_i][node_j] = (node_distance_rest_length_constant * node_distances[node_i][node_j]);
                }
                nodeDistanceSpringRestLengths[node_j][node_i] = nodeDistanceSpringRestLengths[node_i][node_j];
                if (node_distances[node_i][node_j] == Integer.MAX_VALUE) {
                    nodeDistanceSpringStrengths[node_i][node_j] = disconnectedNodeDistanceSpringStrength;
                } else {
                    nodeDistanceSpringStrengths[node_i][node_j] = (node_distance_strength_constant / (node_distances[node_i][node_j] * node_distances[node_i][node_j]));
                }
                nodeDistanceSpringStrengths[node_j][node_i] = nodeDistanceSpringStrengths[node_i][node_j];
            }
        }
    }

    /**
     * If partials_list is given, adjust all partials (bidirectional)
     * for the current location of the given partials and return the
     * new furthest node's partials. Otherwise, just adjust the given
     * partials (using the graphView's nodeViewsIterator), and return
     * it. If reversed is true then partials_list must be provided and
     * all adjustments made by a non-reversed call (with the same
     * partials with the same graphNodeView at the same location) will
     * be undone. Complexity is O( #Nodes ).
     */
    protected PartialDerivatives calculatePartials(PartialDerivatives partials, List partials_list, PotentialEnergy potential_energy, boolean reversed) {
        partials.reset();
        PathwayVertex vertex = partials.getVertex();
        int node_view_index = pGraph.getVertexList().indexOf(vertex);
        double node_view_radius = vertex.getBounds().width;
        double node_view_x = vertex.getXPosition();
        double node_view_y = vertex.getYPosition();
        PartialDerivatives other_node_partials = null;
        PathwayVertex otherVertex;
        int other_node_view_index;
        double other_node_view_radius;
        PartialDerivatives furthest_partials = null;
        ListIterator iterator;
        if (partials_list == null) {
            iterator = pGraph.getVertexIterator();
        } else {
            iterator = partials_list.listIterator();
        }
        double delta_x;
        double delta_y;
        double euclidean_distance;
        double euclidean_distance_cubed;
        double distance_from_rest;
        double distance_from_touching;
        double incremental_change;
        while (iterator.hasNext()) {
            if (partials_list == null) {
                otherVertex = (PathwayVertex) iterator.next();
            } else {
                other_node_partials = (PartialDerivatives) iterator.next();
                otherVertex = other_node_partials.getVertex();
            }
            if (pGraph.getVertexList().indexOf(vertex) == pGraph.getVertexList().indexOf(otherVertex)) {
                continue;
            }
            other_node_view_index = pGraph.getVertexList().indexOf(otherVertex);
            other_node_view_radius = otherVertex.getBounds().width;
            delta_x = (node_view_x - otherVertex.getXPosition());
            delta_y = (node_view_y - otherVertex.getYPosition());
            euclidean_distance = Math.sqrt((delta_x * delta_x) + (delta_y * delta_y));
            euclidean_distance_cubed = Math.pow(euclidean_distance, 3);
            distance_from_touching = (euclidean_distance - (node_view_radius + other_node_view_radius));
            incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (delta_x - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * delta_x) / euclidean_distance))));
            if (!reversed) {
                partials.x += incremental_change;
            }
            if (other_node_partials != null) {
                incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[other_node_view_index][node_view_index] * (-delta_x - ((nodeDistanceSpringRestLengths[other_node_view_index][node_view_index] * -delta_x) / euclidean_distance))));
                if (reversed) {
                    other_node_partials.x -= incremental_change;
                } else {
                    other_node_partials.x += incremental_change;
                }
            }
            if (distance_from_touching < 0.0) {
                incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (delta_x - (((node_view_radius + other_node_view_radius) * delta_x) / euclidean_distance))));
                if (!reversed) {
                    partials.x += incremental_change;
                }
                if (other_node_partials != null) {
                    incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (-delta_x - (((node_view_radius + other_node_view_radius) * -delta_x) / euclidean_distance))));
                    if (reversed) {
                        other_node_partials.x -= incremental_change;
                    } else {
                        other_node_partials.x += incremental_change;
                    }
                }
            }
            incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (delta_y - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * delta_y) / euclidean_distance))));
            if (!reversed) {
                partials.y += incremental_change;
            }
            if (other_node_partials != null) {
                incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[other_node_view_index][node_view_index] * (-delta_y - ((nodeDistanceSpringRestLengths[other_node_view_index][node_view_index] * -delta_y) / euclidean_distance))));
                if (reversed) {
                    other_node_partials.y -= incremental_change;
                } else {
                    other_node_partials.y += incremental_change;
                }
            }
            if (distance_from_touching < 0.0) {
                incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (delta_y - (((node_view_radius + other_node_view_radius) * delta_y) / euclidean_distance))));
                if (!reversed) {
                    partials.y += incremental_change;
                }
                if (other_node_partials != null) {
                    incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (-delta_y - (((node_view_radius + other_node_view_radius) * -delta_y) / euclidean_distance))));
                    if (reversed) {
                        other_node_partials.y -= incremental_change;
                    } else {
                        other_node_partials.y += incremental_change;
                    }
                }
            }
            incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (1.0 - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * (delta_y * delta_y)) / euclidean_distance_cubed))));
            if (reversed) {
                if (other_node_partials != null) {
                    other_node_partials.xx -= incremental_change;
                }
            } else {
                partials.xx += incremental_change;
                if (other_node_partials != null) {
                    other_node_partials.xx += incremental_change;
                }
            }
            if (distance_from_touching < 0.0) {
                incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (1.0 - (((node_view_radius + other_node_view_radius) * (delta_y * delta_y)) / euclidean_distance_cubed))));
                if (reversed) {
                    if (other_node_partials != null) {
                        other_node_partials.xx -= incremental_change;
                    }
                } else {
                    partials.xx += incremental_change;
                    if (other_node_partials != null) {
                        other_node_partials.xx += incremental_change;
                    }
                }
            }
            incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (1.0 - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * (delta_x * delta_x)) / euclidean_distance_cubed))));
            if (reversed) {
                if (other_node_partials != null) {
                    other_node_partials.yy -= incremental_change;
                }
            } else {
                partials.yy += incremental_change;
                if (other_node_partials != null) {
                    other_node_partials.yy += incremental_change;
                }
            }
            if (distance_from_touching < 0.0) {
                incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (1.0 - (((node_view_radius + other_node_view_radius) * (delta_x * delta_x)) / euclidean_distance_cubed))));
                if (reversed) {
                    if (other_node_partials != null) {
                        other_node_partials.yy -= incremental_change;
                    }
                } else {
                    partials.yy += incremental_change;
                    if (other_node_partials != null) {
                        other_node_partials.yy += incremental_change;
                    }
                }
            }
            incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * (delta_x * delta_y)) / euclidean_distance_cubed)));
            if (reversed) {
                if (other_node_partials != null) {
                    other_node_partials.xy -= incremental_change;
                }
            } else {
                partials.xy += incremental_change;
                if (other_node_partials != null) {
                    other_node_partials.xy += incremental_change;
                }
            }
            if (distance_from_touching < 0.0) {
                incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (((node_view_radius + other_node_view_radius) * (delta_x * delta_y)) / euclidean_distance_cubed)));
                if (reversed) {
                    if (other_node_partials != null) {
                        other_node_partials.xy -= incremental_change;
                    }
                } else {
                    partials.xy += incremental_change;
                    if (other_node_partials != null) {
                        other_node_partials.xy += incremental_change;
                    }
                }
            }
            distance_from_rest = (euclidean_distance - nodeDistanceSpringRestLengths[node_view_index][other_node_view_index]);
            incremental_change = (nodeDistanceSpringScalars[layoutPass] * ((nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (distance_from_rest * distance_from_rest)) / 2));
            if (reversed) {
                if (other_node_partials != null) {
                    potential_energy.totalEnergy -= incremental_change;
                }
            } else {
                potential_energy.totalEnergy += incremental_change;
                if (other_node_partials != null) {
                    potential_energy.totalEnergy += incremental_change;
                }
            }
            if (distance_from_touching < 0.0) {
                incremental_change = (anticollisionSpringScalars[layoutPass] * ((anticollisionSpringStrength * (distance_from_touching * distance_from_touching)) / 2));
                if (reversed) {
                    if (other_node_partials != null) {
                        potential_energy.totalEnergy -= incremental_change;
                    }
                } else {
                    potential_energy.totalEnergy += incremental_change;
                    if (other_node_partials != null) {
                        potential_energy.totalEnergy += incremental_change;
                    }
                }
            }
            if (other_node_partials != null) {
                other_node_partials.euclideanDistance = Math.sqrt((other_node_partials.x * other_node_partials.x) + (other_node_partials.y * other_node_partials.y));
                if ((furthest_partials == null) || (other_node_partials.euclideanDistance > furthest_partials.euclideanDistance)) {
                    furthest_partials = other_node_partials;
                }
            }
        }
        if (!reversed) {
            partials.euclideanDistance = Math.sqrt((partials.x * partials.x) + (partials.y * partials.y));
        }
        if ((furthest_partials == null) || (partials.euclideanDistance > furthest_partials.euclideanDistance)) {
            furthest_partials = partials;
        }
        return furthest_partials;
    }

    /**
     * Move the node with the given partials and adjust all partials
     * in the given List to reflect that move, and adjust the
     * potential energy too.
     * 
     * @return the PartialDerivatives of the furthest node after the
     *         move.
     */
    protected PartialDerivatives moveNode(PartialDerivatives partials, List partials_list, PotentialEnergy potential_energy) {
        PathwayVertex vertex = partials.getVertex();
        PartialDerivatives starting_partials = new PartialDerivatives(partials);
        calculatePartials(partials, partials_list, potential_energy, true);
        simpleMoveNode(starting_partials);
        return calculatePartials(partials, partials_list, potential_energy, false);
    }

    protected void simpleMoveNode(PartialDerivatives partials) {
        PathwayVertex vertex = partials.getVertex();
        double denomenator = ((partials.xx * partials.yy) - (partials.xy * partials.xy));
        double delta_x = (((-partials.x * partials.yy) - (-partials.y * partials.xy)) / denomenator);
        double delta_y = (((-partials.y * partials.xx) - (-partials.x * partials.xy)) / denomenator);
        vertex.setXPosition((int) (vertex.getXPosition() + delta_x));
        vertex.setYPosition((int) (vertex.getYPosition() + delta_y));
    }

    protected List createPartialsList() {
        return new ArrayList();
    }

    class PartialDerivatives {

        protected PathwayVertex vertex;

        public double x;

        public double y;

        public double xx;

        public double yy;

        public double xy;

        public double euclideanDistance;

        public PartialDerivatives(PathwayVertex vertex) {
            this.vertex = vertex;
        }

        public PartialDerivatives(PartialDerivatives copy_from) {
            vertex = copy_from.getVertex();
            copyFrom(copy_from);
        }

        public void reset() {
            x = 0.0;
            y = 0.0;
            xx = 0.0;
            yy = 0.0;
            xy = 0.0;
            euclideanDistance = 0.0;
        }

        public PathwayVertex getVertex() {
            return vertex;
        }

        public void copyFrom(PartialDerivatives other_partial_derivatives) {
            x = other_partial_derivatives.x;
            y = other_partial_derivatives.y;
            xx = other_partial_derivatives.xx;
            yy = other_partial_derivatives.yy;
            xy = other_partial_derivatives.xy;
            euclideanDistance = other_partial_derivatives.euclideanDistance;
        }

        public String toString() {
            return "PartialDerivatives( \"" + getVertex() + "\", x=" + x + ", y=" + y + ", xx=" + xx + ", yy=" + yy + ", xy=" + xy + ", euclideanDistance=" + euclideanDistance + " )";
        }
    }

    class PotentialEnergy {

        public double totalEnergy = 0.0;

        public void reset() {
            totalEnergy = 0.0;
        }
    }

    class NodeDistances {

        private int[][] distances;

        private List vertexList;

        private PathwayGraph pGraph;

        public NodeDistances(List vertexList, int[][] distances, PathwayGraph pGraph) {
            this.pGraph = pGraph;
            this.vertexList = vertexList;
            if (distances == null) {
                this.distances = new int[vertexList.size()][];
            } else {
                this.distances = distances;
            }
        }

        public int[][] calculate() {
            PathwayVertex[] vertices = new PathwayVertex[vertexList.size()];
            Integer[] integers = new Integer[vertices.length];
            int index;
            PathwayVertex fromVertex;
            for (int i = 0; i < vertices.length; i++) {
                fromVertex = (PathwayVertex) vertexList.get(i);
                if (fromVertex == null) {
                    continue;
                }
                index = pGraph.getVertexList().indexOf(fromVertex);
                if ((index < 0) || (index >= vertices.length)) {
                    System.err.println("WARNING: GraphNode \"" + fromVertex + "\" has an index value that is out of range: " + index + ".  Graph indices should be maintained such " + "that no index is unused.");
                    return null;
                }
                if (vertices[index] != null) {
                    System.err.println("WARNING: GraphNode \"" + fromVertex + "\" has an index value ( " + index + " ) that is the same as " + "that of another GraphNode ( \"" + vertices[index] + "\" ).  Graph indices should be maintained such " + "that indices are unique.");
                    return null;
                }
                vertices[index] = fromVertex;
                Integer in = new Integer(index);
                integers[index] = in;
            }
            LinkedList queue = new LinkedList();
            boolean[] completed_nodes = new boolean[vertices.length];
            Iterator neighbors;
            PathwayVertex toVertex;
            PathwayVertex neighbor;
            int neighbor_index;
            int to_node_distance;
            int neighbor_distance;
            for (int from_node_index = 0; from_node_index < vertices.length; from_node_index++) {
                fromVertex = vertices[from_node_index];
                if (fromVertex == null) {
                    if (distances[from_node_index] == null) {
                        distances[from_node_index] = new int[vertices.length];
                    }
                    Arrays.fill(distances[from_node_index], Integer.MAX_VALUE);
                    continue;
                }
                if (distances[from_node_index] == null) {
                    distances[from_node_index] = new int[vertices.length];
                }
                Arrays.fill(distances[from_node_index], Integer.MAX_VALUE);
                distances[from_node_index][from_node_index] = 0;
                Arrays.fill(completed_nodes, false);
                queue.add(integers[from_node_index]);
                while (!(queue.isEmpty())) {
                    index = ((Integer) queue.removeFirst()).intValue();
                    if (completed_nodes[index]) {
                        continue;
                    }
                    completed_nodes[index] = true;
                    toVertex = vertices[index];
                    to_node_distance = distances[from_node_index][index];
                    if (index < from_node_index) {
                        int distance_through_to_node;
                        for (int i = 0; i < vertices.length; i++) {
                            if (distances[index][i] == Integer.MAX_VALUE) {
                                continue;
                            }
                            distance_through_to_node = to_node_distance + distances[index][i];
                            if (distance_through_to_node <= distances[from_node_index][i]) {
                                if (distances[index][i] == 1) {
                                    completed_nodes[i] = true;
                                }
                                distances[from_node_index][i] = distance_through_to_node;
                            }
                        }
                        continue;
                    }
                    neighbors = pGraph.getNeighborList(toVertex).iterator();
                    while (neighbors.hasNext()) {
                        neighbor = (PathwayVertex) neighbors.next();
                        neighbor_index = pGraph.getVertexList().indexOf(neighbor);
                        if (vertices[neighbor_index] == null) {
                            distances[from_node_index][neighbor_index] = Integer.MAX_VALUE;
                            continue;
                        }
                        if (completed_nodes[neighbor_index]) {
                            continue;
                        }
                        neighbor_distance = distances[from_node_index][neighbor_index];
                        if ((to_node_distance != Integer.MAX_VALUE) && (neighbor_distance > (to_node_distance + 1))) {
                            distances[from_node_index][neighbor_index] = (to_node_distance + 1);
                            queue.addLast(integers[neighbor_index]);
                        }
                    }
                }
            }
            return distances;
        }
    }
}
