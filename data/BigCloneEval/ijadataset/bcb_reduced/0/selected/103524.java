package org.leeing.test;

import java.util.Random;
import peersim.dynamics.*;
import peersim.graph.*;
import peersim.core.*;
import peersim.config.*;

/**
 * Takes a {@link Linkable} protocol and adds random connections. Note that no
 * connections are removed, they are only added. So it can be used in
 * combination with other initializers.
 *
 * 取得一个Linkable协议并添加随机的连接，注意没有连接被删除，它们只是被添加。所以它能与其它
 * initializers 一起使用。
 * @see GraphFactory#wireKOut
 */
public class WireTopKOut extends WireGraph {

    /**
     * The number of outgoing edges to generate from each node.
     * Passed to {@link GraphFactory#wireKOut}.
     * No loop edges are generated.
     * In the undirected case, the degree
     * of nodes will be on average almost twice as much because the incoming links
     * also become links out of each node.
     *
     * 每个节点生成的outgoing edges。
     * 不会有循环的edges。
     * 在无向图的情形中，节点的度在平均情况下是原来的两倍
     * @config
     */
    private static final String PAR_DEGREE = "k";

    /**
     * The number of outgoing edges to generate from each node.
     */
    private final int k;

    /**
     * Standard constructor that reads the configuration parameters.
     * Invoked by the simulation engine.
     * @param prefix the configuration prefix for this class
     */
    public WireTopKOut(String prefix) {
        super(prefix);
        k = Configuration.getInt(prefix + "." + PAR_DEGREE);
    }

    /** Calls {@link GraphFactory#wireKOut}. */
    public void wire(Graph g) {
    }

    public static Graph wireKOut(Graph g, int k, Random r) {
        final int n = g.size();
        if (n < 2) {
            return g;
        }
        if (n <= k) {
            k = n - 1;
        }
        int[] nodes = new int[n];
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i] = i;
        }
        for (int i = 0; i < n; ++i) {
            int j = 0;
            while (j < k) {
                int newedge = j + r.nextInt(n - j);
                int tmp = nodes[j];
                nodes[j] = nodes[newedge];
                nodes[newedge] = tmp;
                if (nodes[j] != i) {
                    g.setEdge(i, nodes[j]);
                    j++;
                }
            }
        }
        return g;
    }
}
