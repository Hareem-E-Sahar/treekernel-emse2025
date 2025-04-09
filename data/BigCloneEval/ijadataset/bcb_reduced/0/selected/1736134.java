package clustering.implementations;

import java.util.ArrayList;
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
public class MTCQPTreeConstructor implements IClusterTreeConstructor {

    QNode[] qNodes = null;

    int last_operation = -1;

    Hashtable htPath = new Hashtable();

    public String[] filesList = null;

    public int K = 6;

    int[] selected_q = null;

    boolean[] visited_node = null;

    int N = 0;

    public Quartet getRandomQuartet(ArrayList alQuartets) {
        Random rand = new Random();
        int index = rand.nextInt(alQuartets.size());
        return (Quartet) alQuartets.get(index);
    }

    public int[] generateRandomNodesList(int n) {
        Random rand = new Random();
        int index = rand.nextInt(n);
        int[] selected_t = new int[n];
        int[] list = new int[n];
        String randoms = "";
        for (int i = 0; i < n; i++) {
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
        Random rand = new Random();
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

    void copyPartTables(double[][] dm) {
        oldPartTable = new int[dm.length][dm.length];
        for (int i = 0; i < partTable.length; i++) {
            for (int j = 0; j < partTable.length; j++) {
                oldPartTable[i][j] = partTable[i][j];
            }
        }
    }

    int comparePartTables() {
        for (int k = 0; k < partTable.length; k++) {
            int count = 0;
            for (int i = 0; i < partTable.length; i++) {
                if (partTable[i][k] == oldPartTable[i][k]) count++;
            }
            if (count < partTable.length) {
                return k;
            }
        }
        return partTable.length;
    }

    int partK = -1;

    int[][] oldPartTable = null;

    TreeEdge oldEdge = null;

    void addNodeInBestPlace(QuartetTree qt, int node_index, double[][] dm) {
        double best_score = Double.MAX_VALUE;
        TreeEdge best_edge = new TreeEdge(null, null);
        QNode cn_qn = new QNode(-1, null, null, null, node_id++);
        QNode cn_qr = new QNode(-1, null, null, null, node_id++);
        int d = (node_count + 2) / 2;
        if (d == 4) populateEdgesList();
        int best_edge_index = -1;
        for (int i = 0; i < alEdges.size(); i++) {
            if (i == 0) {
                partK = -1;
                partTable = new int[dm.length][dm.length];
            } else {
                partK = Integer.MAX_VALUE;
            }
            TreeEdge te = (TreeEdge) alEdges.get(i);
            connectNode(qt, te, node_index, cn_qn, cn_qr);
            rootEdge.q1 = qt.root;
            rootEdge.q2 = qt.root.adj[0];
            partitionTree(qt, rootEdge, dm.length);
            double score = computeTreeScore(qt, te, d, dm);
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

    QNode[] endNodes = null;

    long part_q = 0;

    long non_part_q = 0;

    void updateQuartetCosts(Quartet q, int d, TreeEdge te, double[][] dm) {
        if (q.nodes[0] == d) {
            edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] += dm[labels[q.nodes[2]]][labels[q.nodes[3]]];
            scoreHistory[labels[q.nodes[1]]][eiTable[te.q1.id][te.q2.id]]++;
        } else if (q.nodes[1] == d) {
            edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] += dm[labels[q.nodes[2]]][labels[q.nodes[3]]];
            scoreHistory[labels[q.nodes[0]]][eiTable[te.q1.id][te.q2.id]]++;
        } else if (q.nodes[2] == d) {
            edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] += dm[labels[q.nodes[0]]][labels[q.nodes[1]]];
            scoreHistory[labels[q.nodes[3]]][eiTable[te.q1.id][te.q2.id]]++;
        } else if (q.nodes[3] == d) {
            edgeScoreHistory[eiTable[te.q1.id][te.q2.id]] += dm[labels[q.nodes[0]]][labels[q.nodes[1]]];
            scoreHistory[labels[q.nodes[2]]][eiTable[te.q1.id][te.q2.id]]++;
        }
    }

    double computeHistoryScore(int d, TreeEdge te, double[][] dm) {
        double score = 0;
        for (int i = 0; i < d; i++) {
            score += scoreHistory[labels[i]][eiTable[te.q1.id][te.q2.id]] * dm[labels[i]][labels[d]];
        }
        return score + edgeScoreHistory[eiTable[te.q1.id][te.q2.id]];
    }

    double[] edgeScoreHistory = null;

    double computeTreeScore(QuartetTree qt, TreeEdge te, int d, double[][] dm) {
        double Ct = 0;
        htPath.clear();
        double count = 0;
        ArrayList alQuartets = null;
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
        Quartet q = new Quartet(0, 0, 0, 0, new double[1][1]);
        QNode[] qNodes = new QNode[node_count];
        endNodes = new QNode[N];
        for (int i = 0; i < node_count; i++) {
            qNodes[i] = this.qNodes[i];
            if (this.qNodes[i].label != -1) {
                endNodes[this.qNodes[i].label] = this.qNodes[i];
            }
        }
        for (int i = 0; i < alQuartets.size(); i++) {
            Quartet q2 = (Quartet) alQuartets.get(i);
            q.nodes[0] = labels[q2.nodes[0]];
            q.nodes[1] = labels[q2.nodes[1]];
            q.nodes[2] = labels[q2.nodes[2]];
            q.nodes[3] = labels[q2.nodes[3]];
            if (q2.K <= partK) {
                if (q2.consistent) {
                    Ct += dm[q.nodes[0]][q.nodes[1]] + dm[q.nodes[2]][q.nodes[3]];
                    updateQuartetCosts(q2, d, te, dm);
                    count++;
                }
                part_q++;
            } else {
                if (isConsistent(q)) {
                    Ct += dm[q.nodes[0]][q.nodes[1]] + dm[q.nodes[2]][q.nodes[3]];
                    updateQuartetCosts(q2, d, te, dm);
                    count++;
                }
                q2.consistent = q.consistent;
                q2.K = q.K;
                non_part_q++;
            }
            step_count++;
        }
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
        QNode qn1 = nr.adj[0];
        QNode qn2 = nr.adj[1];
        for (int i = 0; i < 3; i++) {
            if (qn1.adj[i] == nr) qn1.adj[i] = qn2;
            if (qn2.adj[i] == nr) qn2.adj[i] = qn1;
        }
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
        for (int i = 0; i < 3; i++) {
            if (te.q1.adj[i] == te.q2) te.q1.adj[i] = cn_qr;
        }
        for (int i = 0; i < 3; i++) {
            if (te.q2.adj[i] == te.q1) te.q2.adj[i] = cn_qr;
        }
        qNodes[node_count++] = cn_qr;
        qNodes[node_count++] = cn_qn;
    }

    ArrayList[] quartetLists = null;

    ArrayList[] quartetLists2 = null;

    double[][] scoreHistory = null;

    public void populateQuartetLists(double[][] dm) {
        quartetLists = new ArrayList[dm.length - 4];
        quartetLists2 = new ArrayList[dm.length - 4];
        for (int i = 0; i < dm.length - 4; i++) {
            double[][] temp_dm = new double[5 + i][5 + i];
            if (i == 0) {
                quartetLists[i] = Quartet.generateFullQuartetList(temp_dm);
            } else {
                quartetLists[i] = Quartet.generateDiff2QuartetList(temp_dm);
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

    public String ConstructXMLTree(double[][] dDistanceMatrix) throws Exception {
        qNodes = new QNode[2 * dDistanceMatrix.length - 2];
        visited_node = new boolean[2 * dDistanceMatrix.length - 2];
        N = dDistanceMatrix.length;
        alEdges = new ArrayList();
        QNode[] best_nodes = null;
        QuartetTree qt = null;
        double best_score = Double.MAX_VALUE;
        populateQuartetLists(dDistanceMatrix);
        Quartet.computeWorstAndBestCosts(dDistanceMatrix);
        int[] best_nodes_list = new int[dDistanceMatrix.length];
        int stop_steps = 0;
        int i = 0;
        int[] nodes_list = new int[dDistanceMatrix.length];
        ArrayList goodMutations = new ArrayList();
        ArrayList badMutations = new ArrayList();
        boolean foundBetterScore = false;
        alIndexes = generateIndexes(dDistanceMatrix.length);
        int runs = 0;
        while (((!mutationCountExceeded || foundBetterScore) && runs < Math.abs(K))) {
            if (K < 0 && i > Math.abs(K)) break;
            if (mutationCountExceeded && foundBetterScore) {
                alIndexes = generateIndexes(dDistanceMatrix.length);
                foundBetterScore = false;
                runs++;
            }
            oldEdge = null;
            step_count = 0;
            qt = new QuartetTree();
            labels = new int[dDistanceMatrix.length];
            int count = 0;
            Quartet q;
            if (i == 0) {
                nodes_list = generateRandomNodesList(dDistanceMatrix.length);
            } else {
                nodes_list = mutateNodesList(best_nodes_list);
            }
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
            scoreHistory = new double[dDistanceMatrix.length][1000];
            edgeScoreHistory = new double[1000];
            initializeFromQuartet(qt, q);
            for (int j = 4; j < nodes_list.length; j++) {
                labels[j] = nodes_list[j];
            }
            for (int j = 4; j < nodes_list.length; j++) {
                cur_node = nodes_list[j];
                addNodeInBestPlace(qt, nodes_list[j], dDistanceMatrix);
            }
            double St = (Quartet.worst_cost.doubleValue() - last_tree_score) / (Quartet.worst_cost.doubleValue() - Quartet.best_cost.doubleValue());
            if (last_tree_score <= best_score) {
                stop_steps = 0;
                if (last_tree_score < best_score) {
                    if (i > 0) goodMutations.add(last_mutation);
                    foundBetterScore = true;
                    Debug.append((i + 1) + ".) new best score = " + St + "; count = " + count);
                    System.out.println((i + 1) + ".) new best score = " + St + "; count = " + count);
                }
                best_score = last_tree_score;
                best_nodes = this.cloneQuartetNodes(qNodes, qt);
                for (int j = 0; j < best_nodes_list.length; j++) {
                    best_nodes_list[j] = nodes_list[j];
                }
            } else {
                stop_steps++;
                if (i > 0) {
                    boolean add_mutation = true;
                    for (int h = 0; h < goodMutations.size(); h++) {
                        IndexPair ip = (IndexPair) goodMutations.get(h);
                        if ((ip.i == last_mutation.i && ip.j == last_mutation.j) || (ip.i == last_mutation.j && ip.j == last_mutation.i)) {
                            add_mutation = false;
                            break;
                        }
                    }
                    if (add_mutation) badMutations.add(last_mutation);
                }
            }
            if (i % 100 == 0) {
                Debug.append((i + 1) + ".) score = " + St);
                System.out.println((i + 1) + ".) score = " + St);
                Debug.append("remaining mutations = " + alIndexes.size());
                System.out.println("remaining mutations = " + alIndexes.size());
                Debug.append("pq/nqp = " + (double) part_q / (double) non_part_q + "; avg. index1 = " + (double) total_index / (double) mutation_count + "; avg. index2 = " + (double) total_index2 / (double) mutation_count);
                Debug.append(qt.toTreeFile());
            }
            i++;
            last_tree_score = 0;
        }
        qNodes = best_nodes;
        qt.root = this.getRandomInternNode();
        qt.St = (Quartet.worst_cost.doubleValue() - best_score) / (Quartet.worst_cost.doubleValue() - Quartet.best_cost.doubleValue());
        Debug.append("part_q = " + part_q);
        Debug.append("non_part_q = " + non_part_q);
        Debug.append("pq/nqp = " + (double) part_q / (double) non_part_q);
        Debug.append("operation_count = " + ((double) operation_count / (double) non_part_q));
        String good_mutations = "";
        for (int h = 0; h < goodMutations.size(); h++) {
            IndexPair ip = (IndexPair) goodMutations.get(h);
            int bads = 0;
            for (int g = 0; g < badMutations.size(); g++) {
                IndexPair bip = (IndexPair) badMutations.get(g);
                if ((ip.i == bip.i && ip.j == bip.j) || (ip.i == bip.j && ip.j == bip.i)) {
                    bads++;
                }
            }
            good_mutations += "bads: " + bads + "; " + ip.i + "," + ip.j + " | ";
        }
        Debug.append(good_mutations);
        File f = new File("test.tree");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(qt.toTreeFile().getBytes());
        fos.close();
        Debug.append(qt.toTreeFile());
        return qt.toString();
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
        for (int i = 0; i < node_count; i++) {
            QNode qn = qNodes[i];
            if (qn.label == label) return qn;
        }
        return null;
    }

    Random rand = new Random();

    QNode getRandomNode() {
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

    int node_id = 0;

    int[][] eiTable = null;

    int eIndex = 0;

    public void initializeFromQuartet(QuartetTree qt, Quartet q) {
        eiTable = new int[1000][1000];
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
        int k = 0;
        int a = q.nodes[0];
        int b = q.nodes[1];
        int c = q.nodes[2];
        int d = q.nodes[3];
        int[][] pt = partTable;
        int[] sum = new int[4];
        boolean nd1 = false;
        boolean nd2 = false;
        while (nonDetermination) {
            operation_count++;
            sum[0] += pt[a][k];
            sum[1] += pt[b][k];
            sum[2] += pt[c][k];
            sum[3] += pt[d][k];
            if (sum[0] == sum[1] && sum[2] == sum[3] && sum[0] != sum[2]) {
                nonDetermination = false;
                consistent = true;
            } else if (pt[a][k] != pt[b][k] && pt[c][k] != pt[d][k]) {
                nonDetermination = false;
                consistent = false;
            } else if (sum[2] == sum[3] && sum[2] != sum[0] && sum[2] != sum[1]) {
                nonDetermination = false;
                consistent = true;
            } else if (pt[c][k] == pt[d][k] && pt[c][k] != pt[a][k] && pt[c][k] != pt[b][k]) {
                nonDetermination = false;
                consistent = true;
            } else if (sum[0] == sum[1] && sum[0] != sum[2] && sum[0] != sum[3]) {
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
                    sum[2] = 0;
                    sum[3] = 0;
                    if (pt[a][k] == pt[c][k]) {
                        sum[0] = 0;
                    } else {
                        sum[1] = 0;
                    }
                }
                if ((pt[c][k] != pt[d][k])) {
                    nd2 = true;
                    sum[0] = 0;
                    sum[1] = 0;
                    if (pt[c][k] == pt[a][k]) {
                        sum[2] = 0;
                    } else {
                        sum[3] = 0;
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

    void partitionTree(QuartetTree qt, TreeEdge te, int n) {
        int[] partList = new int[n];
        partList[0] = 1;
        biPart(te.q1, te.q2, partList, 0);
        partList[0] = 2;
        biPart(te.q2, te.q1, partList, 0);
    }

    void biPart(QNode qn, QNode parent, int[] partList, int depth) {
        if (qn.label != -1) {
            for (int i = 0; i < depth; i++) {
                if (partTable[qn.label][i] != partList[i] && i < partK) {
                    partK = i;
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
