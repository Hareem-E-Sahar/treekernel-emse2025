package clustering.implementations;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.io.*;
import clustering.framework.*;
import clustering.testing.Debug;

/**
 * @author Tudor.Ionescu@supelec.fr

MCQPTreeConstructor

This class is an implementation of the maximum consistency based quartet puzzling clustering method (MCQP). Before calling the ConstructXMLTree method, make sure to set the number of steps K (K is a public int member of the class). The default value of K is 6.

 */
public class MTCQPRandomTreeConstructor implements IClusterTreeConstructor {

    QNode[] qNodes = null;

    int last_operation = -1;

    public String[] filesList = null;

    public int K = 6;

    int[] selected_q = null;

    boolean[] visited_node = null;

    int N = 0;

    Random rand = new Random((new Date()).getTime());

    public Quartet getRandomQuartet(ArrayList alQuartets) {
        Random rand = new Random(this.rand.nextLong());
        int index = rand.nextInt(alQuartets.size());
        return (Quartet) alQuartets.get(index);
    }

    public int[] generateRandomNodesList(int n) {
        Random rand = new Random(this.rand.nextLong());
        int index = rand.nextInt(n);
        int[] selected_t = new int[n];
        int[] list = new int[n];
        String randoms = "";
        for (int i = 0; i < n; i++) {
            rand = new Random(this.rand.nextLong());
            while (selected_t[index] == 1) {
                index = rand.nextInt(n);
            }
            randoms += "" + index + "; ";
            list[i] = index;
            selected_t[index] = 1;
        }
        Debug.append(randoms);
        return list;
    }

    boolean mutationCountExceeded = false;

    int total_index = 0;

    int total_index2 = 0;

    int mutation_count = 0;

    ArrayList alIndexes = new ArrayList();

    IndexPair last_mutation = null;

    public int[] mutateNodesList(int[] nodes_list) {
        if (alIndexes.size() == 0) {
            mutationCountExceeded = true;
            return nodes_list;
        } else {
            mutationCountExceeded = false;
        }
        Random rand = new Random(this.rand.nextLong());
        int ipair = rand.nextInt(alIndexes.size());
        IndexPair ip = (IndexPair) alIndexes.get(ipair);
        last_mutation = ip;
        alIndexes.remove(ipair);
        total_index += ip.i;
        total_index2 += ip.j;
        int[] new_list = new int[nodes_list.length];
        for (int i = 0; i < new_list.length; i++) {
            new_list[i] = nodes_list[i];
        }
        int aux = new_list[ip.i];
        new_list[ip.i] = new_list[ip.j];
        new_list[ip.j] = aux;
        return new_list;
    }

    ArrayList alEdges = null;

    double last_tree_score;

    QNode getBestNode(int node_index, double[][] dm) {
        QNode min_node = null;
        double min_dist = Double.MAX_VALUE;
        for (int i = 0; i < node_count; i++) {
            QNode node = qNodes[i];
            if (node.label == -1) continue;
            if (dm[node.label][node_index] < min_dist) {
                min_dist = dm[node.label][node_index];
                min_node = node;
            }
        }
        return min_node;
    }

    void addEdges(QNode qn, QNode parent) {
        alEdges.add(new TreeEdge(qn, parent));
        labelEdge(qn, parent);
        if (qn.label != -1) {
            return;
        }
        for (int i = 0; i < qn.adj.length; i++) {
            if (qn.adj[i] != parent) {
                addEdges(qn.adj[i], qn);
            }
        }
    }

    void populateEdgesList() {
        alEdges.clear();
        for (int i = 0; i < node_count; i++) {
            QNode qn = qNodes[i];
            for (int j = 0; j < 3; j++) {
                if (qn.adj[j] != null) {
                    TreeEdge te = new TreeEdge(qn, qn.adj[j]);
                    if (!containsEdge(te)) {
                        alEdges.add(te);
                        labelEdge(te.q1, te.q2);
                    }
                }
            }
        }
    }

    boolean containsEdge(TreeEdge te) {
        for (int i = 0; i < alEdges.size(); i++) {
            TreeEdge t = (TreeEdge) alEdges.get(i);
            if ((t.q1 == te.q1 && t.q2 == te.q2) || (t.q2 == te.q1 && t.q1 == te.q2)) return true;
        }
        return false;
    }

    int partK = -1;

    double avg_partK = 0;

    int[][] oldPartTable = null;

    TreeEdge oldEdge = null;

    int count_partK = 0;

    double[] avg_contribution = null;

    void initPartTables(int dm_len) {
        if (partTable == null) {
            partTable = new int[dm_len][dm_len];
            sumTable = new int[dm_len][dm_len];
        } else {
            for (int i = 0; i < dm_len; i++) {
                for (int j = i; j < dm_len; j++) {
                    partTable[i][j] = 0;
                    partTable[i][dm_len - j - 1] = 0;
                    sumTable[i][j] = 0;
                    sumTable[i][dm_len - j - 1] = 0;
                }
            }
        }
    }

    void addNodeInBestPlace(QuartetTree qt, int node_index, double[][] dm) {
        double best_score = Double.MAX_VALUE;
        TreeEdge best_edge = new TreeEdge(null, null);
        QNode cn_qn = new QNode(-1, null, null, null, node_id++);
        QNode cn_qr = new QNode(-1, null, null, null, node_id++);
        int d = (node_count + 2) / 2;
        if (d == 4) populateEdgesList();
        int best_edge_index = -1;
        int alEdges_size = alEdges.size();
        for (int i = 0; i < alEdges_size; i++) {
            TreeEdge te = (TreeEdge) alEdges.get(i);
            if (i == 0) {
                partK = -1;
                initPartTables(dm.length);
            } else {
                partK = Integer.MAX_VALUE;
            }
            connectNode(qt, te, node_index, cn_qn, cn_qr);
            rootEdge.q1 = qt.root;
            rootEdge.q2 = qt.root.adj[0];
            partitionTree(qt, rootEdge, dm.length);
            count_partK++;
            double score = computeTreeScore(qt, te, d, dm, i);
            if (score < best_score) {
                best_score = score;
                best_edge.q1 = te.q1;
                best_edge.q2 = te.q2;
                best_edge_index = i;
            }
            disconnectNode(cn_qn);
        }
        connectNode(qt, best_edge, node_index, cn_qn, cn_qr);
        alEdges.set(best_edge_index, new TreeEdge(best_edge.q1, cn_qr));
        labelEdge(best_edge.q1, cn_qr);
        alEdges.add(best_edge_index, new TreeEdge(best_edge.q2, cn_qr));
        labelEdge(best_edge.q2, cn_qr);
        alEdges.add(best_edge_index, new TreeEdge(cn_qn, cn_qr));
        labelEdge(cn_qn, cn_qr);
        oldEdge = best_edge;
        qt.root = cn_qr;
        last_tree_score = best_score;
    }

    boolean treeContainsQuartet(Quartet q) {
        int count = 0;
        for (int i = 0; i < node_count; i++) {
            QNode qn = (QNode) qNodes[i];
            if (qn.label == q.nodes[0] || qn.label == q.nodes[1] || qn.label == q.nodes[2] || qn.label == q.nodes[3]) count++;
        }
        if (count == 4) {
            return true;
        }
        return false;
    }

    int[] labels = null;

    long part_q = 0;

    long non_part_q = 0;

    double computeHistoryScore(int d, TreeEdge te, double[][] dm) {
        double score = 0;
        for (int i = 0; i < d; i++) {
            score += scoreHistory[labels[i]][eiTable[te.q1.id][te.q2.id]] * dm[labels[i]][labels[d]];
        }
        return score + edgeScoreHistory[eiTable[te.q1.id][te.q2.id]];
    }

    double[] edgeScoreHistory = null;

    Quartet q = new Quartet(0, 0, 0, 0, new double[1][1]);

    double computeTreeScore(QuartetTree qt, TreeEdge te, int d, double[][] dm, int edge_index) {
        double Ct = 0;
        Quartet[] alQuartets = null;
        boolean full = false;
        if (edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] == 0) {
            if (oldEdge == null) {
                full = true;
            } else {
                for (int i = 0; i < dm.length; i++) {
                    scoreHistory[labels[i]][eiTable[te.q1.id][te.q2.id]] += scoreHistory[labels[i]][eiTable[oldEdge.q1.id][oldEdge.q2.id]];
                }
                edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] = edgeScoreHistory[eiTable[oldEdge.q1.id][oldEdge.q2.id]];
            }
        }
        alQuartets = quartetLists[d - 4];
        double historyScore = computeHistoryScore(d, te, dm);
        double esh = 0;
        for (int i = alQuartets.length - 1; i >= 0; i--) {
            Quartet q2 = alQuartets[i];
            q.nodes[0] = labels[q2.nodes[0]];
            q.nodes[1] = labels[q2.nodes[1]];
            q.nodes[2] = labels[q2.nodes[2]];
            q.nodes[3] = labels[q2.nodes[3]];
            if (q2.K <= partK) {
                double cost = dm[q.nodes[0]][q.nodes[1]] + dm[q.nodes[2]][q.nodes[3]];
                Ct += cost;
                try {
                    esh += dm[q.nodes[q2.pni1]][q.nodes[q2.pni2]];
                    scoreHistory[q.nodes[q2.cni]][eiTable[te.q1.id][te.q2.id]]++;
                } catch (Exception ex) {
                }
                part_q++;
            } else {
                if (!isConsistent(q)) {
                    q.nodes[1] = labels[q2.nodes[2]];
                    q.nodes[2] = labels[q2.nodes[1]];
                    if (!isConsistent(q)) {
                        int aux = q2.nodes[1];
                        q2.nodes[1] = q2.nodes[3];
                        q2.nodes[3] = aux;
                    } else {
                        int aux = q2.nodes[1];
                        q2.nodes[1] = q2.nodes[2];
                        q2.nodes[2] = aux;
                    }
                }
                if (q2.nodes[0] == d) {
                    q2.cni = 1;
                    q2.pni1 = 2;
                    q2.pni2 = 3;
                } else if (q2.nodes[1] == d) {
                    q2.cni = 0;
                    q2.pni1 = 2;
                    q2.pni2 = 3;
                } else if (q2.nodes[2] == d) {
                    q2.cni = 3;
                    q2.pni1 = 0;
                    q2.pni2 = 1;
                } else if (q2.nodes[3] == d) {
                    q2.cni = 2;
                    q2.pni1 = 0;
                    q2.pni2 = 1;
                } else {
                    q2.cni = -1;
                    q2.pni1 = -1;
                    q2.pni2 = -1;
                }
                q.nodes[0] = labels[q2.nodes[0]];
                q.nodes[1] = labels[q2.nodes[1]];
                q.nodes[2] = labels[q2.nodes[2]];
                q.nodes[3] = labels[q2.nodes[3]];
                double cost = dm[q.nodes[0]][q.nodes[1]] + dm[q.nodes[2]][q.nodes[3]];
                Ct += cost;
                try {
                    esh += dm[q.nodes[q2.pni1]][q.nodes[q2.pni2]];
                    scoreHistory[q.nodes[q2.cni]][eiTable[te.q1.id][te.q2.id]]++;
                } catch (Exception ex) {
                }
                q2.consistent = q.consistent;
                q2.K = q.K;
                non_part_q++;
            }
            step_count++;
        }
        edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] += esh;
        double Ct2 = 0;
        if (full) {
            Ct2 = Ct + last_tree_score;
        } else {
            Ct2 = Ct + last_tree_score + historyScore;
        }
        return Ct2;
    }

    long step_count = 0;

    void disconnectNode(QNode qn) {
        QNode nr = qn.adj[0];
        QNode qn1;
        QNode qn2;
        if (nr.adj[0] == qn) {
            qn1 = nr.adj[1];
            qn2 = nr.adj[2];
        } else if (nr.adj[1] == qn) {
            qn1 = nr.adj[0];
            qn2 = nr.adj[2];
        } else {
            qn1 = nr.adj[0];
            qn2 = nr.adj[1];
        }
        if (qn1.adj[0] == nr) qn1.adj[0] = qn2; else if (qn1.adj[1] == nr) qn1.adj[1] = qn2; else if (qn1.adj[2] == nr) qn1.adj[2] = qn2;
        if (qn2.adj[0] == nr) qn2.adj[0] = qn1; else if (qn2.adj[1] == nr) qn2.adj[1] = qn1; else if (qn2.adj[2] == nr) qn2.adj[2] = qn1;
        node_count -= 2;
    }

    void connectNode(QuartetTree qt, TreeEdge te, int node_index, QNode cn_qn, QNode cn_qr) {
        cn_qn.label = node_index;
        cn_qr.label = -1;
        cn_qr.adj[0] = te.q1;
        cn_qr.adj[1] = te.q2;
        cn_qr.adj[2] = cn_qn;
        cn_qn.adj[0] = cn_qr;
        cn_qn.adj[1] = null;
        cn_qn.adj[2] = null;
        if (te.q1.adj[0] == te.q2) te.q1.adj[0] = cn_qr; else if (te.q1.adj[1] == te.q2) te.q1.adj[1] = cn_qr; else if (te.q1.adj[2] == te.q2) te.q1.adj[2] = cn_qr;
        if (te.q2.adj[0] == te.q1) te.q2.adj[0] = cn_qr; else if (te.q2.adj[1] == te.q1) te.q2.adj[1] = cn_qr; else if (te.q2.adj[2] == te.q1) te.q2.adj[2] = cn_qr;
        qNodes[node_count++] = cn_qr;
        qNodes[node_count++] = cn_qn;
    }

    Quartet[][] quartetLists = null;

    Quartet[][] quartetLists2 = null;

    public static int QC = 1;

    double[][] scoreHistory = null;

    public void populateQuartetLists(double[][] dm) {
        quartetLists = new Quartet[dm.length - 4][];
        quartetLists2 = new Quartet[dm.length - 4][];
        for (int i = 0; i < dm.length - 4; i++) {
            double[][] temp_dm = new double[5 + i][5 + i];
            if (i == 0) {
                quartetLists[i] = Quartet.generateFullQuartetList2(temp_dm);
                Quartet.quartetCount = new int[dm.length + 1];
            } else {
                quartetLists[i] = Quartet.generateDiff2QuartetListReduced(temp_dm, Quartet.quartetCount[QC]);
            }
        }
    }

    public ArrayList generateIndexes(int n) {
        ArrayList al = new ArrayList();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                al.add(new IndexPair(i, j));
            }
        }
        return al;
    }

    TreeEdge rootEdge = null;

    int cur_node = -1;

    public static double St = 0;

    public static String progressTrees = "";

    public static String[] allTrees = null;

    public static String allDiffs = "";

    public static double avg_dist;

    public String ConstructXMLTree(double[][] dDistanceMatrix) throws Exception {
        qNodes = new QNode[2 * dDistanceMatrix.length - 2];
        visited_node = new boolean[2 * dDistanceMatrix.length - 2];
        N = dDistanceMatrix.length;
        avg_contribution = new double[N];
        alEdges = new ArrayList();
        QNode[] best_nodes = null;
        QuartetTree qt = null;
        double best_score = Double.MAX_VALUE;
        populateQuartetLists(dDistanceMatrix);
        double avg_count = 0;
        Quartet.computeWorstAndBestCosts(dDistanceMatrix);
        int[] best_nodes_list = new int[dDistanceMatrix.length];
        int stop_steps = 0;
        int i = 0;
        int[] nodes_list = new int[dDistanceMatrix.length];
        boolean foundBetterScore = false;
        alIndexes = generateIndexes(dDistanceMatrix.length);
        Date d = new Date();
        System.out.println("Started at: " + d.toString());
        St = 0;
        allTrees = new String[Math.abs(K)];
        double[] tree_scores = new double[Math.abs(K)];
        while (i < Math.abs(K)) {
            oldEdge = null;
            step_count = 0;
            Quartet q;
            qt = new QuartetTree();
            if (labels == null) {
                labels = new int[dDistanceMatrix.length];
            } else {
                for (int g = 0; g < labels.length; g++) {
                    labels[g] = 0;
                }
            }
            nodes_list = generateRandomNodesList(dDistanceMatrix.length);
            q = new Quartet(nodes_list[0], nodes_list[1], nodes_list[2], nodes_list[3], dDistanceMatrix);
            double bs = -1;
            for (int j = 0; j < 4; j++) {
                if (bs < (dDistanceMatrix[nodes_list[j % 4]][(j + 1) % 4] + dDistanceMatrix[nodes_list[(j + 2) % 4]][(j + 3) % 4])) {
                    bs = dDistanceMatrix[nodes_list[j % 4]][(j + 1) % 4] + dDistanceMatrix[nodes_list[(j + 2) % 4]][(j + 3) % 4];
                    q.nodes[0] = nodes_list[j % 4];
                    q.nodes[1] = nodes_list[(j + 1) % 4];
                    q.nodes[2] = nodes_list[(j + 2) % 4];
                    q.nodes[3] = nodes_list[(j + 3) % 4];
                }
            }
            labels[0] = q.nodes[0];
            labels[1] = q.nodes[1];
            labels[2] = q.nodes[2];
            labels[3] = q.nodes[3];
            initializeFromQuartet(qt, q, dDistanceMatrix);
            if (scoreHistory == null) {
                scoreHistory = new double[dDistanceMatrix.length][3 * dDistanceMatrix.length];
                edgeScoreHistory = new double[3 * dDistanceMatrix.length];
            } else {
                for (int g = 0; g < 3 * dDistanceMatrix.length; g++) {
                    for (int h = 0; h < dDistanceMatrix.length; h++) {
                        scoreHistory[h][g] = 0;
                    }
                    edgeScoreHistory[g] = 0;
                }
            }
            for (int j = 4; j < nodes_list.length; j++) {
                labels[j] = nodes_list[j];
            }
            for (int j = 4; j < nodes_list.length; j++) {
                cur_node = nodes_list[j];
                addNodeInBestPlace(qt, nodes_list[j], dDistanceMatrix);
            }
            String sTree = qt.toTreeFile();
            allTrees[i] = sTree;
            tree_scores[i] = last_tree_score;
            if (last_tree_score <= best_score) {
                stop_steps = 0;
                foundBetterScore = true;
                best_score = last_tree_score;
                best_nodes = this.cloneQuartetNodes(qNodes, qt);
                for (int j = 0; j < best_nodes_list.length; j++) {
                    best_nodes_list[j] = nodes_list[j];
                }
                St = (Quartet.worst_cost.doubleValue() - last_tree_score) / (Quartet.worst_cost.doubleValue() - Quartet.best_cost.doubleValue());
                System.out.println((i + 1) + ".) new best score = " + St);
            } else {
                stop_steps++;
            }
            if (i % 1000 == 0) {
                System.out.println("step = " + (i + 1) + "/" + Math.abs(K));
            }
            i++;
            last_tree_score = 0;
        }
        d = new Date();
        System.out.println("Finished at: " + d.toString());
        qNodes = best_nodes;
        qt.root = this.getRandomInternNode();
        qt.St = (Quartet.worst_cost.doubleValue() - best_score) / (Quartet.worst_cost.doubleValue() - Quartet.best_cost.doubleValue());
        System.out.println("part_q = " + part_q);
        System.out.println("non_part_q = " + non_part_q);
        System.out.println("avg_partK = " + (avg_partK / (double) count_partK));
        System.out.println("pq/nqp = " + (double) part_q / (double) non_part_q);
        System.out.println("operation_count = " + ((double) operation_count / (double) non_part_q));
        String sTree = qt.toTreeFile();
        File f = new File("test.tree");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(sTree.getBytes());
        fos.close();
        f = new File("best.score");
        fos = new FileOutputStream(f);
        fos.write(("" + St).getBytes());
        fos.close();
        return sTree;
    }

    int getNodeIndex(QNode qn, QNode[] nodesList) {
        for (int i = 0; i < nodesList.length; i++) {
            if (nodesList[i] == qn) return i;
        }
        return -1;
    }

    QNode[] cloneQuartetNodes(QNode[] listNodes, QuartetTree qt) {
        QNode[] newNodes = new QNode[listNodes.length];
        for (int i = 0; i < newNodes.length; i++) newNodes[i] = new QNode(-1, null, null, null, -1);
        for (int i = 0; i < listNodes.length; i++) {
            newNodes[i].label = listNodes[i].label;
            newNodes[i].id = listNodes[i].id;
            for (int j = 0; j < newNodes[i].adj.length; j++) {
                int index = getNodeIndex(listNodes[i].adj[j], listNodes);
                if (index != -1) newNodes[i].adj[j] = newNodes[index];
            }
        }
        qt.root = getRandomInternNode();
        return newNodes;
    }

    QNode getNodeByLabel(int label) {
        for (int i = 0; i < qNodes.length; i++) {
            if (qNodes[i] != null) {
                QNode qn = qNodes[i];
                if (qn.label == label) return qn;
            }
        }
        return null;
    }

    QNode getRandomNode() {
        Random rand = new Random(this.rand.nextLong());
        int k = rand.nextInt(qNodes.length);
        return qNodes[k];
    }

    QNode getRandomLeafNode() {
        QNode qn = getRandomNode();
        while (qn.label == -1) qn = getRandomNode();
        return qn;
    }

    QNode getRandomInternNode() {
        QNode qn = getRandomNode();
        while (qn.label != -1) qn = getRandomNode();
        return qn;
    }

    void labelEdge(QNode n1, QNode n2) {
        if (eiTable[n1.id][n2.id] == 0) {
            eIndex++;
            eiTable[n1.id][n2.id] = eIndex;
            eiTable[n2.id][n1.id] = eIndex;
        }
    }

    void unlabelEdge(QNode n1, QNode n2) {
        if (eiTable[n1.id][n2.id] != 0) {
            eIndex--;
            eiTable[n1.id][n2.id] = 0;
            eiTable[n2.id][n1.id] = 0;
        }
    }

    int node_id = 0;

    int[][] eiTable = null;

    int eIndex = 0;

    public void initializeFromQuartet(QuartetTree qt, Quartet q, double[][] dm) {
        if (eiTable == null) {
            eiTable = new int[3 * dm.length][3 * dm.length];
        } else {
            for (int i = 0; i < 3 * dm.length; i++) {
                for (int j = i; j < 3 * dm.length; j++) {
                    eiTable[i][j] = 0;
                    eiTable[i][3 * dm.length - j - 1] = 0;
                }
            }
        }
        eIndex = 0;
        node_count = 0;
        node_id = 0;
        qNodes[node_count++] = qt.root;
        qt.root.id = node_id++;
        qt.root.adj[0] = new QNode(-1, qt.root, null, null, node_id++);
        qNodes[node_count++] = qt.root.adj[0];
        rootEdge = new TreeEdge(qt.root, qt.root.adj[0]);
        labelEdge(qt.root, qt.root.adj[0]);
        qt.root.adj[1] = new QNode(q.nodes[0], qt.root, null, null, node_id++);
        qNodes[node_count++] = qt.root.adj[1];
        labelEdge(qt.root, qt.root.adj[1]);
        qt.root.adj[2] = new QNode(q.nodes[1], qt.root, null, null, node_id++);
        qNodes[node_count++] = qt.root.adj[2];
        labelEdge(qt.root, qt.root.adj[2]);
        qt.root.adj[0].adj[1] = new QNode(q.nodes[2], qt.root.adj[0], null, null, node_id++);
        qNodes[node_count++] = qt.root.adj[0].adj[1];
        labelEdge(qt.root.adj[0], qt.root.adj[0].adj[1]);
        qt.root.adj[0].adj[2] = new QNode(q.nodes[3], qt.root.adj[0], null, null, node_id++);
        qNodes[node_count++] = qt.root.adj[0].adj[2];
        labelEdge(qt.root.adj[0], qt.root.adj[0].adj[2]);
    }

    int node_count = 0;

    int[][] partTable = null;

    long operation_count = 0;

    boolean isConsistent(Quartet q) {
        boolean nonDetermination = true;
        boolean consistent = false;
        int k = partK;
        int a = q.nodes[0];
        int b = q.nodes[1];
        int c = q.nodes[2];
        int d = q.nodes[3];
        if (k == -1) {
            sumTable[a][k + 1] = 0;
            sumTable[b][k + 1] = 0;
            sumTable[c][k + 1] = 0;
            sumTable[d][k + 1] = 0;
            k = 0;
        } else {
            sumTable[a][k + 1] = sumTable[a][k];
            sumTable[b][k + 1] = sumTable[b][k];
            sumTable[c][k + 1] = sumTable[c][k];
            sumTable[d][k + 1] = sumTable[d][k];
        }
        int[][] pt = partTable;
        boolean nd1 = false;
        boolean nd2 = false;
        while (nonDetermination) {
            operation_count++;
            sumTable[a][k + 1] = sumTable[a][k] + pt[a][k];
            sumTable[b][k + 1] = sumTable[b][k] + pt[b][k];
            sumTable[c][k + 1] = sumTable[c][k] + pt[c][k];
            sumTable[d][k + 1] = sumTable[d][k] + pt[d][k];
            if (sumTable[a][k + 1] == sumTable[b][k + 1] && sumTable[c][k + 1] == sumTable[d][k + 1] && sumTable[a][k + 1] != sumTable[c][k + 1]) {
                nonDetermination = false;
                consistent = true;
            } else if (pt[a][k] != pt[b][k] && pt[c][k] != pt[d][k]) {
                nonDetermination = false;
                consistent = false;
            } else if (sumTable[c][k + 1] == sumTable[d][k + 1] && sumTable[c][k + 1] != sumTable[a][k + 1] && sumTable[c][k + 1] != sumTable[b][k + 1]) {
                nonDetermination = false;
                consistent = true;
            } else if (pt[c][k] == pt[d][k] && pt[c][k] != pt[a][k] && pt[c][k] != pt[b][k]) {
                nonDetermination = false;
                consistent = true;
            } else if (sumTable[a][k + 1] == sumTable[b][k + 1] && sumTable[a][k + 1] != sumTable[c][k + 1] && sumTable[a][k + 1] != sumTable[d][k + 1]) {
                nonDetermination = false;
                consistent = true;
            } else if (pt[a][k] == pt[b][k] && pt[a][k] != pt[c][k] && pt[a][k] != pt[d][k]) {
                nonDetermination = false;
                consistent = true;
            } else if ((pt[a][k] == 0 && pt[b][k] == 0) || (pt[c][k] == 0 && pt[d][k] == 0)) {
                nonDetermination = false;
                consistent = true;
            } else {
                if ((pt[a][k] != pt[b][k])) {
                    nd1 = true;
                    sumTable[c][k + 1] = 0;
                    sumTable[d][k + 1] = 0;
                    if (pt[a][k] == pt[c][k]) {
                        sumTable[a][k + 1] = 0;
                    } else {
                        sumTable[b][k + 1] = 0;
                    }
                }
                if ((pt[c][k] != pt[d][k])) {
                    nd2 = true;
                    sumTable[a][k + 1] = 0;
                    sumTable[b][k + 1] = 0;
                    if (pt[c][k] == pt[a][k]) {
                        sumTable[c][k + 1] = 0;
                    } else {
                        sumTable[d][k + 1] = 0;
                    }
                }
                if (nd1 && nd2) {
                    nonDetermination = false;
                    consistent = false;
                }
            }
            k++;
        }
        q.consistent = consistent;
        q.K = k;
        return consistent;
    }

    int[] partList = null;

    void partitionTree(QuartetTree qt, TreeEdge te, int n) {
        if (partList == null) {
            partList = new int[n];
        } else {
            for (int i = 0; i < partList.length; i++) {
                partList[i] = 0;
            }
        }
        partList[0] = 1;
        biPart(te.q1, te.q2, partList, 0);
        partList[0] = 2;
        biPart(te.q2, te.q1, partList, 0);
    }

    int[][] sumTable;

    void biPart(QNode qn, QNode parent, int[] partList, int depth) {
        if (qn.label != -1) {
            for (int i = 0; i < depth; i++) {
                if (partTable[qn.label][i] != partList[i] && i < partK) {
                    partK = i;
                    avg_partK += i;
                }
                partTable[qn.label][i] = partList[i];
            }
            partTable[qn.label][depth] = 0;
            return;
        }
        boolean first = true;
        for (int i = 0; i < qn.adj.length; i++) {
            if (qn.adj[i] != parent) {
                if (first) {
                    partList[depth + 1] = 1;
                    first = false;
                } else {
                    partList[depth + 1] = 2;
                }
                biPart(qn.adj[i], qn, partList, depth + 1);
            }
        }
    }
}
