package org.hypergraphdb.viewer.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.hypergraphdb.viewer.FNode;
import org.hypergraphdb.viewer.GraphView;
import org.hypergraphdb.viewer.layout.util.NodeDistances;
import org.hypergraphdb.viewer.phoebe.PNodeView;

/**
 * An implementation of Kamada and Kawai's spring embedded layout algorithm.
 */
public class SpringEmbeddedLayout implements Layout {

    public static final int DEFAULT_NUM_LAYOUT_PASSES = 2;

    public static final double DEFAULT_AVERAGE_ITERATIONS_PER_NODE = 20.0;

    public static final double[] DEFAULT_NODE_DISTANCE_SPRING_SCALARS = new double[] { 1.0, 1.0 };

    public static final double DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT = 15.0;

    public static final double DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT = 200.0;

    public static final double DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH = .05;

    public static final double DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH = 2500.0;

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

    protected GraphView view;

    protected int nodeCount;

    protected int edgeCount;

    protected int layoutPass;

    protected HashMap<PNodeView, Integer> nodeIndexToMatrixIndexMap;

    protected TreeMap<Integer, PNodeView> matrixIndexToNodeIndexMap;

    public SpringEmbeddedLayout() {
    }

    public String getName() {
        return "Spring Embedded";
    }

    public void applyLayout(GraphView view) {
        this.view = view;
        if (view == null) return;
        setGraphView(view);
        initializeSpringEmbeddedLayouter();
        doLayout();
    }

    public void setGraphView(GraphView new_graph_view) {
        view = new_graph_view;
    }

    public GraphView getGraphView() {
        return view;
    }

    protected void initializeSpringEmbeddedLayouter() {
    }

    public void doLayout() {
        nodeCount = view.getNodeViewCount();
        edgeCount = view.getEdgeViewCount();
        nodeIndexToMatrixIndexMap = new HashMap<PNodeView, Integer>();
        matrixIndexToNodeIndexMap = new TreeMap<Integer, PNodeView>();
        int count = 0;
        for (PNodeView nodeView : view.getNodeViews()) {
            nodeIndexToMatrixIndexMap.put(nodeView, new Integer(count));
            matrixIndexToNodeIndexMap.put(new Integer(count), nodeView);
            count++;
        }
        double euclidean_distance_threshold = (0.5 * (nodeCount + edgeCount));
        double potential_energy_percent_change_threshold = .001;
        int num_iterations = (int) ((nodeCount * averageIterationsPerNode) / numLayoutPasses);
        List<PartialDerivatives> partials_list = new ArrayList<PartialDerivatives>();
        PotentialEnergy potential_energy = new PotentialEnergy();
        PartialDerivatives partials;
        PartialDerivatives furthest_node_partials = null;
        for (layoutPass = 0; layoutPass < numLayoutPasses; layoutPass++) {
            setupForLayoutPass();
            potential_energy.reset();
            partials_list.clear();
            for (PNodeView node_view : view.getNodeViews()) {
                partials = new PartialDerivatives(node_view);
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
        nodeDistanceSpringRestLengths = new double[nodeCount][nodeCount];
        nodeDistanceSpringStrengths = new double[nodeCount][nodeCount];
        if (nodeDistanceSpringScalars[layoutPass] == 0.0) {
            return;
        }
        ArrayList<PNodeView> nodeList = new ArrayList<PNodeView>();
        Collection<PNodeView> matrixIndices = matrixIndexToNodeIndexMap.values();
        int i = 0;
        for (Iterator<PNodeView> iterator = matrixIndices.iterator(); iterator.hasNext(); ) {
            PNodeView nodeIndex = iterator.next();
            nodeList.add(i, nodeIndex);
            i++;
        }
        NodeDistances ind = new NodeDistances(nodeList, view, nodeIndexToMatrixIndexMap);
        int[][] node_distances = ind.calculate();
        if (node_distances == null) return;
        double node_distance_strength_constant = nodeDistanceStrengthConstant;
        double node_distance_rest_length_constant = nodeDistanceRestLengthConstant;
        System.out.println("node_distances: " + nodeDistanceSpringRestLengths + ":" + node_distances);
        for (int ii = 0; ii < nodeCount; ii++) {
            for (int jj = (ii + 1); jj < nodeCount; jj++) {
                if (node_distances[ii][jj] == Integer.MAX_VALUE) {
                    nodeDistanceSpringRestLengths[ii][jj] = disconnectedNodeDistanceSpringRestLength;
                } else {
                    nodeDistanceSpringRestLengths[ii][jj] = (node_distance_rest_length_constant * node_distances[ii][jj]);
                }
                nodeDistanceSpringRestLengths[jj][ii] = nodeDistanceSpringRestLengths[ii][jj];
                if (node_distances[ii][jj] == Integer.MAX_VALUE) {
                    nodeDistanceSpringStrengths[ii][jj] = disconnectedNodeDistanceSpringStrength;
                } else {
                    nodeDistanceSpringStrengths[ii][jj] = (node_distance_strength_constant / (node_distances[ii][jj] * node_distances[ii][jj]));
                }
                nodeDistanceSpringStrengths[jj][ii] = nodeDistanceSpringStrengths[ii][jj];
            }
        }
    }

    /**
     * If partials_list is given, adjust all partials (bidirectional) for the
     * current location of the given partials and return the new furthest node's
     * partials. Otherwise, just adjust the given partials (using the
     * graphView's nodeViewsIterator), and return it. If reversed is true then
     * partials_list must be provided and all adjustments made by a non-reversed
     * call (with the same partials with the same graphNodeView at the same
     * location) will be undone. Complexity is O( #Nodes ).
     */
    protected PartialDerivatives calculatePartials(PartialDerivatives partials, List partials_list, PotentialEnergy potential_energy, boolean reversed) {
        partials.reset();
        PNodeView node_view = partials.getNodeView();
        int node_view_index = nodeIndexToMatrixIndexMap.get(node_view);
        double node_view_radius = node_view.getWidth();
        double node_view_x = node_view.getXPosition();
        double node_view_y = node_view.getYPosition();
        PartialDerivatives other_node_partials = null;
        PNodeView other_node_view;
        int other_node_view_index;
        double other_node_view_radius;
        PartialDerivatives furthest_partials = null;
        Iterator iterator;
        if (partials_list == null) {
            iterator = view.getNodeViews().iterator();
        } else {
            iterator = partials_list.iterator();
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
                other_node_view = (PNodeView) iterator.next();
            } else {
                other_node_partials = (PartialDerivatives) iterator.next();
                other_node_view = other_node_partials.getNodeView();
            }
            if (node_view.getNode().getHandle().equals(other_node_view.getNode().getHandle())) {
                continue;
            }
            other_node_view_index = nodeIndexToMatrixIndexMap.get(other_node_view);
            other_node_view_radius = other_node_view.getWidth();
            delta_x = (node_view_x - other_node_view.getXPosition());
            delta_y = (node_view_y - other_node_view.getYPosition());
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
     * Move the node with the given partials and adjust all partials in the
     * given List to reflect that move, and adjust the potential energy too.
     * 
     * @return the PartialDerivatives of the furthest node after the move.
     */
    protected PartialDerivatives moveNode(PartialDerivatives partials, List<PartialDerivatives> partials_list, PotentialEnergy potential_energy) {
        PartialDerivatives starting_partials = new PartialDerivatives(partials);
        calculatePartials(partials, partials_list, potential_energy, true);
        simpleMoveNode(starting_partials);
        return calculatePartials(partials, partials_list, potential_energy, false);
    }

    protected void simpleMoveNode(PartialDerivatives partials) {
        PNodeView node_view = partials.getNodeView();
        double denomenator = ((partials.xx * partials.yy) - (partials.xy * partials.xy));
        double delta_x = (((-partials.x * partials.yy) - (-partials.y * partials.xy)) / denomenator);
        double delta_y = (((-partials.y * partials.xx) - (-partials.x * partials.xy)) / denomenator);
        Point2D p = node_view.getOffset();
        node_view.setOffset(p.getX() + delta_x, p.getY() + delta_y);
    }

    class PartialDerivatives {

        protected PNodeView nodeView;

        public double x;

        public double y;

        public double xx;

        public double yy;

        public double xy;

        public double euclideanDistance;

        public PartialDerivatives(PNodeView node_view) {
            nodeView = node_view;
        }

        public PartialDerivatives(PartialDerivatives copy_from) {
            nodeView = copy_from.getNodeView();
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

        public PNodeView getNodeView() {
            return nodeView;
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
            return "PartialDerivatives( \"" + getNodeView() + "\", x=" + x + ", y=" + y + ", xx=" + xx + ", yy=" + yy + ", xy=" + xy + ", euclideanDistance=" + euclideanDistance + " )";
        }
    }

    class PotentialEnergy {

        public double totalEnergy = 0.0;

        public void reset() {
            totalEnergy = 0.0;
        }
    }
}
