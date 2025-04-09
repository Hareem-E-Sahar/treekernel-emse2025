package edu.sharif.ce.dml.toposim.netanalyzer.analyze;

import edu.sharif.ce.dml.toposim.netanalyzer.entity.Link;
import edu.sharif.ce.dml.toposim.netanalyzer.entity.Path;

/**
 * Created by IntelliJ IDEA.
 * User: Ali
 * Date: Feb 20, 2007
 * Time: 7:35:41 PM
 */
public class PathAnalyzer {

    private Link[][] linkMatrix;

    private Path workingPath = new Path();

    private Path bestPath = new Path();

    private double[][] mostRealReliabilities;

    private double[][] mostPredReliabilities;

    private Path[][] mostRealReliablePaths;

    private Path[][] mostPredReliablePaths;

    public PathAnalyzer(Link[][] linkMatrix) {
        this.linkMatrix = linkMatrix;
        int nodesCount = linkMatrix.length;
        int simLength = linkMatrix[0][1].getSrcNode().getHistory().size();
        mostRealReliabilities = new double[nodesCount][simLength];
        mostPredReliabilities = new double[nodesCount][simLength];
        mostRealReliablePaths = new Path[nodesCount][simLength];
        mostPredReliablePaths = new Path[nodesCount][simLength];
    }

    public Link[][] getLinkMatrix() {
        return linkMatrix;
    }

    public double[][] getMostRealReliabilities() {
        return mostRealReliabilities;
    }

    public double[][] getMostPredReliabilities() {
        return mostPredReliabilities;
    }

    public Path[][] getMostRealReliablePaths() {
        return mostRealReliablePaths;
    }

    public Path[][] getMostPredReliablePaths() {
        return mostPredReliablePaths;
    }

    private Link[][] prune(double lowerBound, int time, char mode) {
        int nodeNumber = linkMatrix.length;
        Link[][] subMtx = new Link[nodeNumber][nodeNumber];
        if (mode == 'r') {
            for (int i = 0; i < nodeNumber; i++) {
                for (int j = 0; j < nodeNumber; j++) {
                    if (i != j && linkMatrix[i][j].computeDuration(time) >= lowerBound) {
                        subMtx[i][j] = linkMatrix[i][j];
                    }
                }
            }
        } else if (mode == 'p') {
            for (int i = 0; i < nodeNumber; i++) {
                for (int j = 0; j < nodeNumber; j++) {
                    if (i != j && linkMatrix[i][j].computePredicetedDuration(time) >= lowerBound) {
                        subMtx[i][j] = linkMatrix[i][j];
                    }
                }
            }
        }
        return subMtx;
    }

    private boolean canRoute(int src, int dest, Link[][] matrix, int time, boolean[] flag) {
        workingPath.addNode(src);
        flag[src] = true;
        if (src == dest) {
            return true;
        }
        for (int nextNode = 0; nextNode < matrix.length; nextNode++) {
            if (!flag[nextNode] && !workingPath.contains(nextNode) && matrix[src][nextNode] != null && matrix[src][nextNode].getStatus(time)) {
                if (canRoute(nextNode, dest, matrix, time, flag)) {
                    return true;
                } else workingPath.removeLastNode();
            }
        }
        return false;
    }

    public double getMostReliability(int src, int dest, int time, char mode) {
        int[] minAndMax = findMinAndMaxDuration(time);
        double minDuration = minAndMax[0];
        double maxDuration = minAndMax[1];
        Link[][] mtx;
        bestPath.clear();
        while ((int) maxDuration - (int) minDuration > 1) {
            boolean[] flag = new boolean[linkMatrix.length];
            workingPath.clear();
            double lowerBound = (minDuration + maxDuration) / 2;
            mtx = prune(lowerBound, time, mode);
            if (canRoute(src, dest, mtx, time, flag)) {
                minDuration = lowerBound;
                bestPath = new Path(workingPath);
            } else {
                maxDuration = lowerBound;
            }
        }
        return maxDuration;
    }

    private int[] findMinAndMaxDuration(int time) {
        int minDuration = Integer.MAX_VALUE;
        int maxDuration = 0;
        for (int i = 0; i < linkMatrix.length - 1; i++) {
            for (int j = i + 1; j < linkMatrix.length; j++) {
                int tempDuration = linkMatrix[i][j].computeDuration(time);
                if (tempDuration > maxDuration) {
                    maxDuration = tempDuration;
                }
                if (tempDuration < minDuration) {
                    minDuration = tempDuration;
                }
            }
        }
        return new int[] { minDuration, maxDuration };
    }

    public void printAdjacentMatrix(Link[][] matrix, int time) {
        int con;
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("\t" + i);
        }
        System.out.println();
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(i + "\t");
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[i][j] != null && matrix[i][j].getStatus(time)) {
                    con = 1;
                } else con = 0;
                System.out.print(con + "\t");
            }
            System.out.println();
        }
    }

    public double computeAvgRealPathDuration(Link[][] matrix) {
        int simTime = matrix[0][1].getSrcNode().getHistory().size();
        long sumRel = 0;
        long count = 0;
        double mostRealReliability;
        for (int j = 1; j < matrix.length; j++) {
            System.out.println("R:\t" + j);
            for (int time = 1; time <= simTime; time++) {
                mostRealReliability = getMostReliability(0, j, time, 'r');
                mostRealReliabilities[j][time - 1] = mostRealReliability;
                mostRealReliablePaths[j][time - 1] = new Path(bestPath);
                if (!Double.isNaN(mostRealReliability)) {
                    sumRel += mostRealReliability;
                    count++;
                }
            }
        }
        return (1.0 * sumRel) / (count);
    }

    public double computeAvgPredictedPathDuration(Link[][] matrix) {
        int simTime = matrix[0][1].getSrcNode().getHistory().size();
        long sumRel = 0;
        long count = 0;
        double mostPredictedReliability;
        for (int j = 1; j < matrix.length; j++) {
            System.out.println("P:\t" + j);
            for (int time = 1; time <= simTime; time++) {
                mostPredictedReliability = getMostReliability(0, j, time, 'p');
                mostPredReliabilities[j][time - 1] = mostPredictedReliability;
                mostPredReliablePaths[j][time - 1] = new Path(bestPath);
                if (!Double.isNaN(mostPredictedReliability)) {
                    sumRel += mostPredictedReliability;
                    count++;
                }
            }
        }
        return (1.0 * sumRel) / (count);
    }

    public double computeMSEofPDsAndPPDs() {
        return 0;
    }

    public double computeAvgRealPathDuration(Link[][] matrix, int samplingRate) {
        int simTime = matrix[0][1].getSrcNode().getHistory().size();
        long sumRel = 0;
        long count = 0;
        double mostRealReliability;
        for (int j = 1; j < matrix.length; j++) {
            System.out.println("R:\t" + j);
            for (int time = 1; time <= simTime; time++) {
                mostRealReliability = getMostReliability(0, j, time, 'r', samplingRate);
                mostRealReliabilities[j][time - 1] = mostRealReliability;
                mostRealReliablePaths[j][time - 1] = new Path(bestPath);
                if (!Double.isNaN(mostRealReliability)) {
                    sumRel += mostRealReliability;
                    count++;
                }
            }
        }
        return (1.0 * sumRel) / (count);
    }

    public double computeAvgPredictedPathDuration(Link[][] matrix, int sampligRate) {
        int simTime = matrix[0][1].getSrcNode().getHistory().size();
        long sumRel = 0;
        long count = 0;
        double mostPredictedReliability;
        for (int j = 1; j < matrix.length; j++) {
            System.out.println("P:\t" + j);
            for (int time = 1; time <= simTime; time++) {
                mostPredictedReliability = getMostReliability(0, j, time, 'p', sampligRate);
                mostPredReliabilities[j][time - 1] = mostPredictedReliability;
                mostPredReliablePaths[j][time - 1] = new Path(bestPath);
                if (!Double.isNaN(mostPredictedReliability)) {
                    sumRel += mostPredictedReliability;
                    count++;
                }
            }
        }
        return (1.0 * sumRel) / (count);
    }

    public double getMostReliability(int src, int dest, int time, char mode, int samplingRate) {
        int[] minAndMax = findMinAndMaxDuration(time);
        double minDuration = minAndMax[0];
        double maxDuration = minAndMax[1];
        Link[][] mtx;
        while ((int) maxDuration - (int) minDuration > 1) {
            boolean[] flag = new boolean[linkMatrix.length];
            workingPath.clear();
            double lowerBound = (minDuration + maxDuration) / 2;
            mtx = prune(lowerBound, time, mode, samplingRate);
            if (canRoute(src, dest, mtx, time, flag)) {
                minDuration = lowerBound;
                bestPath = new Path(workingPath);
            } else {
                maxDuration = lowerBound;
            }
        }
        return maxDuration;
    }

    private Link[][] prune(double lowerBound, int time, char mode, int samplingRate) {
        int nodeNumber = linkMatrix.length;
        Link[][] subMtx = new Link[nodeNumber][nodeNumber];
        if (mode == 'r') {
            for (int i = 0; i < nodeNumber; i++) {
                for (int j = 0; j < nodeNumber; j++) {
                    if (i != j && linkMatrix[i][j].computeDuration(time, samplingRate) >= lowerBound) {
                        subMtx[i][j] = linkMatrix[i][j];
                    }
                }
            }
        } else if (mode == 'p') {
            for (int i = 0; i < nodeNumber; i++) {
                for (int j = 0; j < nodeNumber; j++) {
                    if (i != j && linkMatrix[i][j].computePredicetedDuration(time, samplingRate) >= lowerBound) {
                        subMtx[i][j] = linkMatrix[i][j];
                    }
                }
            }
        }
        return subMtx;
    }
}
