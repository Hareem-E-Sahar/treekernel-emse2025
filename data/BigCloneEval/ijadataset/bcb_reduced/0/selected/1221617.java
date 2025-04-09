package rescuecore.tools.mapgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * A road weighter that decides on road importance by making 10,000 trips
 * through the RescueMap between random pairs of nodes.
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class AntsRoadWeighter implements RoadWeighter {

    /** Number of runs to use to determine main roads. **/
    private static final int RUNS = 15000;

    /** Base percentage of 3 lane roads. **/
    private static final int THREELANE = 10;

    /** Base percentage of 2 lane roads. **/
    private static final int TWOLANE = 20;

    /** A count of how many times each road (adjacent node pair) has been used. **/
    private int[][] usedCount;

    /** The Euclidean distances between all pairs of nodes. **/
    private int[][] distances;

    public void connect(RescueMap rm, int uniformity, boolean nooneway, Random rand) {
        int nodes = rm.getNodeCount();
        distances = new int[nodes][nodes];
        for (int i = 0; i < nodes; i++) for (int j = 0; j < i; j++) {
            int x = rm.getX(i) - rm.getX(j);
            int y = rm.getY(i) - rm.getY(j);
            distances[i][j] = (int) Math.sqrt(x * x + y * y);
            distances[j][i] = distances[i][j];
        }
        usedCount = new int[nodes][nodes];
        System.out.print("Simulating road use.");
        System.out.flush();
        int steps = RUNS / 20;
        int[] prevs = new int[nodes];
        int[] dists = new int[nodes];
        for (int i = 0; i < RUNS; i++) {
            int[] picked = pickNodes(rm, rand);
            runPath(prevs, dists, rm, picked[0], picked[1]);
            if (i % steps == 0) {
                System.out.print(".");
                System.out.flush();
            }
        }
        System.out.println("done.");
        ArrayList l = new ArrayList(nodes * 5);
        for (int i = 0; i < nodes; i++) for (int j = 0; j < nodes; j++) if (rm.getRoad(i, j) > 0) {
            l.add(new Integer(usedCount[i][j]));
        }
        Collections.sort(l);
        int index1 = (int) (l.size() * (1 - THREELANE / 100.0));
        int v1 = ((Integer) (l.get(index1))).intValue();
        int v2 = ((Integer) (l.get(index1 - (int) (l.size() * TWOLANE / 100.0)))).intValue();
        for (int i = 0; i < nodes; i++) for (int j = 0; j < nodes; j++) {
            if (usedCount[i][j] >= v1 || (nooneway && usedCount[j][i] >= v1)) {
                rm.setRoad(i, j, 3);
                if (nooneway) rm.setRoad(j, i, 3);
            } else if (usedCount[i][j] >= v2 || (nooneway && usedCount[j][i] >= v2)) {
                rm.setRoad(i, j, 2);
                if (nooneway) rm.setRoad(j, i, 2);
            }
        }
    }

    public int[] pickNodes(RescueMap rm, Random rand) {
        return new int[] { (int) (rand.nextDouble() * rm.getNodeCount()), (int) (rand.nextDouble() * rm.getNodeCount()) };
    }

    private void runPath(int[] prevs, int[] dists, RescueMap rm, int start, int end) {
        int nodes = rm.getNodeCount();
        for (int i = 0; i < dists.length; i++) dists[i] = Integer.MAX_VALUE;
        PairHeap q = new PairHeap();
        prevs[start] = -1;
        dists[start] = distances[start][end];
        int next = start;
        while (next != end) {
            for (int j = 0; j < nodes; j++) {
                if (j != next && rm.getRoad(next, j) > 0) {
                    int guess = dists[next] + distances[next][j] + distances[j][end];
                    if (dists[j] > guess) {
                        dists[j] = guess;
                        prevs[j] = next;
                        q.insert(j, guess);
                    }
                }
            }
            next = q.deleteMin();
        }
        int prev = end;
        while (prevs[prev] != -1) {
            usedCount[prevs[prev]][prev]++;
            prev = prevs[prev];
        }
    }
}
