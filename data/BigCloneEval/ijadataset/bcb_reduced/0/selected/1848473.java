package pt.utl.ist.lucene.utils.dijkstra;

import pt.utl.ist.lucene.utils.dijkstra.DijkstraNode;
import java.util.ArrayList;
import java.util.List;

/**
 * This map stores routes in a matrix, a nxn array. It is most useful when
 * there are lots of routes, otherwise using a sparse representation is
 * recommended.
 * 
 * @author Renaud Waldura &lt;renaud+tw@waldura.com&gt;
 * @version $Id: DenseRoutesMap.java 2367 2007-08-20 21:47:25Z renaud $
 */
public class DenseRoutesMap implements RoutesMap {

    private final int[][] distances;

    public DenseRoutesMap(int numNodes) {
        distances = new int[numNodes][numNodes];
    }

    /**
	 * Link two cities by a direct route with the given distance.
	 */
    public void addDirectRoute(DijkstraNode start, DijkstraNode end, int distance) {
        distances[start.getIndex()][end.getIndex()] = distance;
    }

    /**
	 * @return the distance between the two cities, or 0 if no path exists.
	 */
    public int getDistance(DijkstraNode start, DijkstraNode end) {
        return distances[start.getIndex()][end.getIndex()];
    }

    /**
	 * @return the list of all valid destinations from the given city.
	 */
    public List<DijkstraNode> getDestinations(DijkstraNode node) {
        List<DijkstraNode> list = new ArrayList<DijkstraNode>();
        for (int i = 0; i < distances.length; i++) {
            if (distances[node.getIndex()][i] > 0) {
                list.add(node.getNode(i));
            }
        }
        return list;
    }

    /**
	 * @return the list of all cities leading to the given city.
	 */
    public List<DijkstraNode> getPredecessors(DijkstraNode node) {
        List<DijkstraNode> list = new ArrayList<DijkstraNode>();
        for (int i = 0; i < distances.length; i++) {
            if (distances[i][node.getIndex()] > 0) {
                list.add(node.getNode(i));
            }
        }
        return list;
    }

    /**
	 * @return the transposed graph of this graph, as a new RoutesMap instance.
	 */
    public RoutesMap getInverse() {
        DenseRoutesMap transposed = new DenseRoutesMap(distances.length);
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances.length; j++) {
                transposed.distances[i][j] = distances[j][i];
            }
        }
        return transposed;
    }
}
