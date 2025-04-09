package kmeans;

import data.TextBasedData;
import data.TextBasedDataSet;
import data.VectorSpaceModel;
import io.PropertiesLoader;
import io.PropertiesXMLLoader;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import log.LogAdmin;
import tools.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import preProcess.LoadData;
import postProcess.SaveClusterResult;

public class TextKMeansCluster {

    private int m_NumClusters = 1000;

    private long m_seed = 6;

    private List<TextBasedData> m_ClusterCentroids = null;

    private TextBasedDataSet m_ClusterCentroidsOwner = null;

    private int m_Iterations = 0;

    private int m_MaxIterations = 6;

    private int[] m_clusterAssignments, m_ClusterSizes;

    private List<Pair<String, Integer>> m_clusterResult = null;

    private LogAdmin m_logAdmin;

    private boolean m_normalization = true;

    private String m_algorithmName;

    public TextKMeansCluster(LogAdmin logAdmin) {
        m_logAdmin = logAdmin;
    }

    public void buildCluster(TextBasedDataSet dataSet) {
        try {
            inintCluster(dataSet);
            int i;
            boolean converged = false;
            int numText = dataSet.getTextNumber();
            while (!converged && m_Iterations < m_MaxIterations) {
                m_Iterations++;
                m_logAdmin.printLog("Iterations: " + m_Iterations);
                m_logAdmin.printLog("cluserNum " + m_NumClusters);
                converged = true;
                m_logAdmin.printLog("textNum " + numText);
                for (i = 0; i < numText; i++) {
                    if (i % 1000 == 0) System.out.println("i " + i);
                    TextBasedData toCluster = dataSet.getByListPosition(i);
                    int newC = clusterProcessedInstance(toCluster);
                    if (newC != m_clusterAssignments[i]) {
                        converged = false;
                    }
                    m_clusterAssignments[i] = newC;
                }
                if (!converged) {
                    m_logAdmin.printLog("update ClusterCentroids");
                    updateCentroids(dataSet);
                    m_logAdmin.printLog("update ClusterCentroids  over");
                }
            }
            m_clusterResult = new ArrayList<Pair<String, Integer>>();
            for (i = 0; i < numText; i++) {
                m_clusterResult.add(new Pair<String, Integer>(dataSet.getKeyByPosition(i), m_clusterAssignments[i]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        m_logAdmin.printLog("converge");
    }

    public void setSeed(long seed) {
        m_seed = seed;
    }

    public long getSeed() {
        return m_seed;
    }

    private void inintCluster(TextBasedDataSet dataSet) throws Exception {
        dataSet.generatePKList();
        m_Iterations = 0;
        m_ClusterCentroids = new ArrayList<TextBasedData>(m_NumClusters);
        m_ClusterCentroidsOwner = new TextBasedDataSet();
        m_ClusterCentroidsOwner.setAttrMeta(dataSet.getAttrMeta());
        m_clusterAssignments = new int[dataSet.getTextNumber()];
        Random RandomO = new Random(getSeed());
        int instIndex;
        HashMap<String, Object> initC = new HashMap<String, Object>();
        String textKey = null;
        for (int j = dataSet.getTextNumber() - 1; j >= 0; j--) {
            instIndex = RandomO.nextInt(j + 1);
            textKey = dataSet.getKeyByPosition(instIndex);
            if (!initC.containsKey(textKey)) {
                m_ClusterCentroids.add(dataSet.getByListPosition(instIndex));
                initC.put(textKey, null);
            }
            try {
                dataSet.swap(j, instIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (m_ClusterCentroids.size() == m_NumClusters) {
                break;
            }
        }
        m_NumClusters = m_ClusterCentroids.size();
    }

    private void updateCentroids(TextBasedDataSet dataSet) throws Exception {
        int i, j;
        int emptyClusterCount = 0;
        int textAttrNum = dataSet.getAttNumber();
        List<Pair<String, Double>> textAttributeMeta = dataSet.getAttrMeta();
        TextBasedDataSet[] tempI = new TextBasedDataSet[m_NumClusters];
        for (i = 0; i < m_NumClusters; i++) {
            tempI[i] = new TextBasedDataSet();
            tempI[i].setAttrMeta(textAttributeMeta);
        }
        for (i = 0; i < dataSet.getTextNumber(); i++) {
            tempI[m_clusterAssignments[i]].insertNewData(dataSet.getByListPosition(i));
        }
        for (i = 0, j = 0; i < m_NumClusters; i++) {
            if (tempI[j].getTextNumber() == 0) {
                emptyClusterCount++;
                for (int move = j; move < m_NumClusters - emptyClusterCount; move++) {
                    tempI[move] = tempI[move + 1];
                }
            } else j++;
        }
        if (emptyClusterCount > 0) {
            m_NumClusters -= emptyClusterCount;
        }
        m_ClusterCentroids = new ArrayList<TextBasedData>(m_NumClusters);
        for (i = 0; i < m_NumClusters; i++) {
            List<VectorSpaceModel<String, Double>> vals = new ArrayList<VectorSpaceModel<String, Double>>();
            for (j = 0; j < textAttrNum; j++) {
                vals.add(j, tempI[i].meanOrMode(j));
            }
            m_ClusterCentroids.add(new TextBasedData(vals, m_ClusterCentroidsOwner));
        }
        m_ClusterSizes = new int[m_NumClusters];
        for (i = 0; i < m_NumClusters; i++) {
            m_ClusterSizes[i] = tempI[i].getTextNumber();
            m_logAdmin.printLog("cluster: " + i + "numText: " + m_ClusterSizes[i]);
        }
    }

    private int clusterProcessedInstance(TextBasedData instance) {
        int bestCluster = 0;
        try {
            double minDist = Integer.MAX_VALUE;
            for (int i = 0; i < m_NumClusters; i++) {
                double dist = distanceEuild(instance, m_ClusterCentroids.get(i));
                if (dist < minDist) {
                    minDist = dist;
                    bestCluster = i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestCluster;
    }

    /**
	  * ���� ŷʽ����
	  * @param instanceA
	  * @param instanceB
	  * @return
	  */
    private double distanceEuild(TextBasedData instanceA, TextBasedData instanceB) {
        double distance = instanceA.distanceTo(instanceB, m_normalization);
        return distance;
    }

    public void setNormalization(boolean normalization) {
        m_normalization = normalization;
    }

    public void setMaxIteration(int n) throws Exception {
        if (n <= 0) {
            throw new Exception("Number of clusters must be > 0");
        }
        m_MaxIterations = n;
    }

    public void setNumClusters(int n) throws Exception {
        if (n <= 0) {
            throw new Exception("Number of clusters must be > 0");
        }
        m_NumClusters = n;
    }

    public int getNumClusters() {
        return m_NumClusters;
    }

    public List<Pair<String, Integer>> getClusterResult() {
        return m_clusterResult;
    }

    public void setAlgorithmName(String name) {
        m_algorithmName = name;
    }

    public String getAlgorithmName() {
        return m_algorithmName;
    }

    public static TextKMeansCluster getCluster(LogAdmin logAdmin, String dataSetName, PropertiesXMLLoader pro) {
        TextKMeansCluster textCluster = new TextKMeansCluster(logAdmin);
        String[] parameters = { "para" };
        String experment_name = pro.getProperty(parameters).getProperty("experiment_name");
        textCluster.setAlgorithmName(experment_name);
        int max_cluster_num = Integer.parseInt(pro.getProperty(parameters).getProperty("max_cluster_num"));
        int max_iteration = Integer.parseInt(pro.getProperty(parameters).getProperty("max_iteration"));
        String normalization = pro.getProperty(parameters).getProperty("normalization");
        logAdmin.printLog("dataset and algorith info: " + dataSetName + " " + experment_name);
        logAdmin.printLog("---------- ");
        long seed = System.currentTimeMillis();
        logAdmin.printLog("seed: " + seed);
        logAdmin.printLog("---------- ");
        textCluster.setSeed(seed);
        try {
            logAdmin.printLog("max_cluster_num: " + max_cluster_num);
            logAdmin.printLog("---------- ");
            textCluster.setNumClusters(max_cluster_num);
            logAdmin.printLog("max_cluster_num: " + max_iteration);
            logAdmin.printLog("---------- ");
            textCluster.setMaxIteration(max_iteration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logAdmin.printLog("normalization: " + normalization);
        logAdmin.printLog("---------- ");
        if (normalization.equalsIgnoreCase("true")) textCluster.setNormalization(true); else textCluster.setNormalization(false);
        return textCluster;
    }

    public static void main(String[] args) {
    }
}
