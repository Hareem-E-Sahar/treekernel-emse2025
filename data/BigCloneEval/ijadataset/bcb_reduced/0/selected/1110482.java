package org.dcopolis.ui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ForceDirectedGraphLayoutAlgorithm implements GraphLayoutAlgorithm {

    public static double SPRING_CONSTANT = 2.0;

    public static double DAMPING_FACTOR = 0.2;

    public static double TIME_STEP = 0.01;

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
        double velocityX[] = new double[nodeArray.length];
        double velocityY[] = new double[nodeArray.length];
        double posX[] = new double[nodeArray.length];
        double posY[] = new double[nodeArray.length];
        double mass[] = new double[nodeArray.length];
        Random rand = new Random();
        int minX = Integer.MAX_VALUE;
        int maxX = 0;
        int minY = Integer.MAX_VALUE;
        int maxY = 0;
        for (int i = 0; i < nodeArray.length; i++) {
            velocityX[i] = 0.0;
            velocityY[i] = 0.0;
            mass[i] = nodeArray[i].getPreferredSize().getWidth() * nodeArray[i].getPreferredSize().getHeight();
            boolean alreadyExists;
            int x, y;
            do {
                alreadyExists = false;
                x = rand.nextInt(nodeArray.length * 10 + 1);
                y = rand.nextInt(nodeArray.length * 10 + 1);
                for (int j = 0; j < i; j++) {
                    if ((int) posX[j] == x && (int) posY[j] == y) {
                        alreadyExists = true;
                        break;
                    }
                }
            } while (alreadyExists);
            posX[i] = (double) x;
            posY[i] = (double) y;
            System.out.println(Integer.toString(i) + " = (" + posX[i] + ", " + posY[i] + ")");
        }
        double kineticEnergy;
        do {
            kineticEnergy = 0;
            for (int i = 0; i < nodeArray.length; i++) {
                double forceX = 0;
                double forceY = 0;
                for (int j = 0; j < nodeArray.length; j++) {
                    if (i == j) continue;
                    double dist;
                    if (posX[i] < posX[j]) dist = posX[i] + nodeArray[i].getPreferredSize().getWidth() / 2.0 - (posX[j] - nodeArray[j].getPreferredSize().getWidth() / 2.0); else dist = posX[i] - nodeArray[i].getPreferredSize().getWidth() / 2.0 - (posX[j] + nodeArray[j].getPreferredSize().getWidth() / 2.0);
                    if (dist == 0) forceX = 0.0; else forceX = forceX + 1000.0 / dist;
                    if (posY[i] < posY[j]) dist = posY[i] + nodeArray[i].getPreferredSize().getHeight() / 2.0 - (posY[j] - nodeArray[j].getPreferredSize().getHeight() / 2.0); else dist = posY[i] - nodeArray[i].getPreferredSize().getHeight() / 2.0 - (posY[j] + nodeArray[j].getPreferredSize().getHeight() / 2.0);
                    if (dist == 0) forceY = 0.0; else forceY = forceY + 1000.0 / dist;
                }
                for (int j = 0; j < nodeArray.length; j++) {
                    if (i == j || !graph.isConnected(nodeArray[i], nodeArray[j])) continue;
                    forceX = forceX - SPRING_CONSTANT * (posX[i] - posX[j]);
                    forceY = forceY - SPRING_CONSTANT * (posY[i] - posY[j]);
                }
                velocityX[i] = (velocityX[i] + TIME_STEP * forceX) * DAMPING_FACTOR;
                velocityY[i] = (velocityY[i] + TIME_STEP * forceY) * DAMPING_FACTOR;
                posX[i] = posX[i] + TIME_STEP * velocityX[i];
                posY[i] = posY[i] + TIME_STEP * velocityY[i];
                if (posX[i] + 0.5 < minX) minX = (int) (posX[i] + 0.5);
                if (posX[i] + 0.5 > maxX) maxX = (int) (posX[i] + 0.5);
                if (posY[i] + 0.5 < minY) minY = (int) (posY[i] + 0.5);
                if (posY[i] + 0.5 > maxY) maxY = (int) (posY[i] + 0.5);
                double v = Math.sqrt(velocityX[i] * velocityX[i] + velocityY[i] * velocityY[i]);
                kineticEnergy += mass[i] * v * v;
            }
            System.out.println("KE: " + kineticEnergy);
        } while (kineticEnergy > 800);
        Dimension size = new Dimension((int) (maxX - minX + 0.5), (int) (maxY - minY + 0.5));
        for (int i = 0; i < nodeArray.length; i++) nodeArray[i].setBounds((int) (posX[i] - minX + 0.5), (int) (posY[i] - minY + 0.5), (int) (nodeArray[i].getPreferredSize().getWidth()), (int) (nodeArray[i].getPreferredSize().getHeight()));
        return new GraphLayout(size, nodeArray);
    }

    public static void main(String args[]) throws Exception {
        JFrame frame = new JFrame("Force Directed JGraph");
        JFrame frame2 = new JFrame("Regular JGraph (Custom Heuristic)");
        JGraph graph = new JGraph(new ForceDirectedGraphLayoutAlgorithm());
        JGraph graph2 = new JGraph();
        graph.setMinimumSize(new Dimension(50, 50));
        graph2.setMinimumSize(new Dimension(50, 50));
        boolean adj[][] = org.dcopolis.util.Graph.randomConnectedGraph(10, 0.2, new Random());
        JLabel nodes[] = new JLabel[adj.length];
        JLabel nodes2[] = new JLabel[adj.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new JLabel(Integer.toString(i + 1));
            nodes2[i] = new JLabel(Integer.toString(i + 1));
            graph.addNode(nodes[i], false);
            graph2.addNode(nodes2[i], false);
        }
        for (int i = 0; i < nodes.length; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                if (adj[i][j]) {
                    graph.addEdge(nodes[i], nodes[j], false);
                    graph2.addEdge(nodes2[i], nodes2[j], false);
                }
            }
        }
        graph.layoutNodes();
        graph2.layoutNodes();
        frame.getContentPane().add(new JScrollPane(graph));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(100, 100));
        frame.setVisible(true);
        frame2.getContentPane().add(new JScrollPane(graph2));
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(new Dimension(100, 100));
        frame2.setVisible(true);
        for (Component node : graph.nodes) {
            System.out.println(((JLabel) node).getText() + ": " + node.getLocation());
        }
        for (Component node : graph2.nodes) {
            System.out.println(((JLabel) node).getText() + " (Regular Graph): " + node.getLocation());
        }
    }
}
