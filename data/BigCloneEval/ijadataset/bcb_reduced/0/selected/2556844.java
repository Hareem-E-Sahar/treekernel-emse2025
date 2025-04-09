package org.dcopolis.ui;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * <p><b>NOTE:</b> this layout algorithm will have unexpected results if the
 * graph is not connected!</p>
 *
 * <p>The general heuristic/algorithm is as follows:
 * <ol>
 *   <li><em>Q</em> &larr; a HashSet containing all of the nodes in the graph.</li>
 *   <li><em>F</em> &larr; a priority queue that returns nodes in order of descending {@link #pageRank(boolean[][]) PageRank}; this is initially empty.</li>
 *   <li><em>P</em> &larr; a hashtable mapping nodes to other nodes; initially empty.</li>
 *   <li><em>n</em> &larr; the node in <em>Q</em> with the highest PageRank.</li>
 *   <li>Remove(<em>n</em>, <em>Q</em>)</li>
 *   <li>Add(<em>n</em>, <em>F</em>)</li>
 *   <li><b>while</b> <em>F</em> is not empty <b>do</b>
 *     <ol start="8">
         <li><em>n</em> &larr; Poll(<em>F</em>)</li>
         <li><b>if</b> <em>n</em> is not a key in <em>P</em> <b>then</b>
           <ol start="10">
             <li>Place <em>n</em> at the origin.</li>
           </ol>
         </li>
         <li value="11"><b>else</b>
           <ol start="12">
             <li><em>p</em> &larr; Get(<em>n</em>, <em>P</em>)</li>
             <li>Place <em>n</em> not closer than <code>MINIMUM_NODE_SEPARATION</code> pixels to <em>p</em> such that it is not occupying the same space as another already-placed node.</li>
           </ol>
         </li>
         <li value="14"><b>for each</b> <em>c</em> &isin; <em>Q</em> such that {@link #isConnected(Component, Component) isConnected}(<em>c</em>, <em>n</em>) <b>do</b>
           <ol start="15">
             <li>Remove(<em>c</em>, <em>Q</em>)</li>
             <li>Add(<em>c</em>, <em>F</em>)</li>
             <li>Put(<em>c</em> &rarr; <em>n</em>, <em>P</em>)</li>
           </ol>
         </li>
       </ol>
 *   </li>
 * </ol>
 * </p>
 *
 * @author <a href="http://www.sultanik.com/" target="_blank">Evan A. Sultanik</a>
 */
public class PageRankLayoutAlgorithm implements GraphLayoutAlgorithm {

    public static int MINIMUM_NODE_SEPARATION = 45;

    public PageRankLayoutAlgorithm() {
    }

    public GraphLayout layoutNodes(JGraph graph) {
        LinkedHashSet<Component> nodes = graph.getNodes();
        if (nodes.size() <= 0) return null;
        Component[] nodeArray = nodes.toArray(new Component[0]);
        boolean[][] adj = new boolean[nodeArray.length][nodeArray.length];
        for (int i = 0; i < nodeArray.length; i++) {
            adj[i][i] = false;
            for (int j = i + 1; j < nodeArray.length; j++) {
                adj[i][j] = graph.isConnected(nodeArray[i], nodeArray[j]);
                adj[j][i] = adj[i][j];
            }
        }
        double d[] = pageRank(adj);
        Hashtable<RankedGraphNode, RankedGraphNode> parents = new Hashtable<RankedGraphNode, RankedGraphNode>();
        PriorityQueue<RankedGraphNode> frontier = new PriorityQueue<RankedGraphNode>();
        LinkedHashSet<RankedGraphNode> remainingNodes = new LinkedHashSet<RankedGraphNode>();
        RankedGraphNode max = null;
        for (int i = 0; i < nodeArray.length; i++) {
            RankedGraphNode rgn = new RankedGraphNode(nodeArray[i], d[i], i);
            remainingNodes.add(rgn);
            if (max == null || d[i] > max.getPageRank()) max = rgn;
        }
        remainingNodes.remove(max);
        frontier.add(max);
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        Rectangle2D.Double placements[] = new Rectangle2D.Double[d.length];
        Arrays.fill(placements, null);
        int NODE_INTERSECTION_PENALTY = 5000;
        while (!frontier.isEmpty()) {
            RankedGraphNode n = frontier.poll();
            RankedGraphNode parent = parents.get(n);
            int idx = n.getIndex();
            Dimension s = n.getComponent().getPreferredSize();
            if (parent == null) {
                placements[idx] = new Rectangle2D.Double(0.0 - s.getWidth() / 2.0, 0.0 - s.getHeight() / 2.0, s.getWidth(), s.getHeight());
            } else {
                int parentIdx = parent.getIndex();
                double minCost = Double.MAX_VALUE;
                Rectangle2D.Double bestPlacement = null;
                for (double distance = (double) MINIMUM_NODE_SEPARATION; distance < (double) (MINIMUM_NODE_SEPARATION * 2); distance += 5.0) {
                    for (double angle = -1.0 * Math.PI; angle < Math.PI; angle += Math.PI / 8.0) {
                        double centerX = placements[parentIdx].getX() + placements[parentIdx].getWidth() / 2.0 + distance * Math.cos(angle);
                        double centerY = placements[parentIdx].getY() + placements[parentIdx].getHeight() / 2.0 + distance * Math.sin(angle);
                        double edgeCost = 0;
                        int numEdges = 0;
                        double nonEdgeCost = 0;
                        int numNonEdges = 0;
                        Rectangle2D.Double loc = new Rectangle2D.Double(centerX - s.getWidth() / 2.0, centerY - s.getHeight() / 2.0, s.getWidth(), s.getHeight());
                        double cost = 0;
                        for (int i = 0; i < placements.length; i++) {
                            if (i == idx || placements[i] == null) continue;
                            if (loc.intersects(placements[i])) cost += NODE_INTERSECTION_PENALTY;
                            double neighborCenterX = placements[i].getX() + placements[i].getWidth() / 2.0;
                            double neighborCenterY = placements[i].getY() + placements[i].getHeight() / 2.0;
                            double xDiff = neighborCenterX - centerX;
                            double yDiff = neighborCenterY - centerY;
                            double h = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                            if (graph.isConnected(n.getComponent(), nodeArray[i]) && h >= MINIMUM_NODE_SEPARATION) {
                                edgeCost += h;
                                numEdges++;
                            } else {
                                if (h < MINIMUM_NODE_SEPARATION) h = MINIMUM_NODE_SEPARATION;
                                nonEdgeCost += h;
                                numNonEdges++;
                            }
                        }
                        double avgEdgeCost = 0;
                        double avgNonEdgeCost = 0;
                        if (numNonEdges > 0) avgNonEdgeCost = nonEdgeCost / (double) numNonEdges; else avgNonEdgeCost = 0;
                        if (edgeCost == 0) {
                            cost += 0 - avgNonEdgeCost;
                        } else {
                            avgEdgeCost = edgeCost / (double) numEdges;
                            cost += avgEdgeCost - avgNonEdgeCost;
                        }
                        if (bestPlacement == null || cost < minCost) {
                            minCost = cost;
                            bestPlacement = loc;
                        }
                    }
                }
                placements[idx] = bestPlacement;
                if (bestPlacement.getX() < minX) minX = bestPlacement.getX();
                if (bestPlacement.getX() + bestPlacement.getWidth() > maxX) maxX = bestPlacement.getX() + bestPlacement.getWidth();
                if (bestPlacement.getY() < minY) minY = bestPlacement.getY();
                if (bestPlacement.getY() + bestPlacement.getHeight() > maxY) maxY = bestPlacement.getY() + bestPlacement.getHeight();
            }
            for (Iterator<RankedGraphNode> iter = remainingNodes.iterator(); iter.hasNext(); ) {
                RankedGraphNode rgn = iter.next();
                if (graph.isConnected(rgn.getComponent(), n.getComponent())) {
                    iter.remove();
                    frontier.add(rgn);
                    parents.put(rgn, n);
                }
            }
        }
        Dimension size = new Dimension((int) (maxX - minX + 0.5), (int) (maxY - minY + 0.5));
        for (int i = 0; i < nodeArray.length; i++) {
            if (placements[i] != null) nodeArray[i].setBounds((int) (placements[i].getX() - minX + 0.5), (int) (placements[i].getY() - minY + 0.5), (int) (placements[i].getWidth()), (int) (placements[i].getHeight()));
        }
        return new GraphLayout(size, nodeArray);
    }

    private class RankedGraphNode implements Comparable<RankedGraphNode> {

        Component component;

        double pageRank;

        int idx;

        public RankedGraphNode(Component component, double pageRank, int index) {
            this.component = component;
            this.pageRank = pageRank;
            idx = index;
        }

        public Component getComponent() {
            return component;
        }

        public double getPageRank() {
            return pageRank;
        }

        public int compareTo(RankedGraphNode rgn) {
            double diff = rgn.getPageRank() - pageRank;
            if (diff == 0.0) return 0; else if (diff > 0.0) return 1; else return -1;
        }

        public int getIndex() {
            return idx;
        }
    }

    /**
     * Uses the PageRank algorithm to compute an approximation of the
     * primary right eigenvector of a graph's adjacency matrix.  This
     * algorithm runs in worst-case <em>O</em>(<em>n</em><sup>2</sup>)
     * time, where <em>n</em> is the number of nodes in the graph.
     */
    public static double[] pageRank(boolean adj[][]) {
        double DAMPING_FACTOR = 0.15;
        int PAGE_RANK_ITERATIONS = 1000;
        double[] d = new double[adj.length];
        if (adj.length <= 0) return d;
        int[] numNeighbors = new int[adj.length];
        Arrays.fill(numNeighbors, 0);
        for (int i = 0; i < adj.length; i++) for (int j = 0; j < adj.length; j++) if (i != j && adj[i][j]) numNeighbors[i]++;
        double[] newD = new double[adj.length];
        Arrays.fill(newD, 1.0 / (double) adj.length);
        for (int k = 0; k < PAGE_RANK_ITERATIONS; k++) {
            for (int i = 0; i < adj.length; i++) d[i] = newD[i];
            for (int i = 0; i < adj.length; i++) {
                if (numNeighbors[i] <= 0) continue;
                double giveAwayDelta = d[i] * DAMPING_FACTOR / (double) numNeighbors[i];
                newD[i] -= d[i] * DAMPING_FACTOR;
                for (int j = 0; j < adj.length; j++) if (i != j && adj[i][j]) newD[j] += giveAwayDelta;
            }
        }
        return newD;
    }
}
