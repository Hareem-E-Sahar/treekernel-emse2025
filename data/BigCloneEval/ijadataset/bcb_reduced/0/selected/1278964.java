package weka.clusterers;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.rules.DecisionTable;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

/**
 * <!-- globalinfo-start --> Cluster data using the k means algorithm <p/> <!-- globalinfo-end --> <!-- options-start
 * --> Valid options are: <p/>
 * 
 * <pre>
 *  -N &lt;num&gt;
 *  number of clusters.
 *  (default 2).
 * </pre>
 * 
 * <pre>
 *  -S &lt;num&gt;
 *  Random number seed.
 *  (default 10)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.2 $
 * @see RandomizableClusterer
 */
public class DocumentKMeans extends SimpleKMeans implements NumberOfClustersRequestable, WeightedInstancesHandler {

    /** for serialization */
    static final long serialVersionUID = -3235809600124455376L;

    /**
	 * replace missing values in training instances
	 */
    private ReplaceMissingValues m_ReplaceMissingFilter;

    /**
	 * number of clusters to generate
	 */
    private int m_NumClusters = 2;

    /**
	 * holds the cluster centroids
	 */
    private Instances m_ClusterCentroids;

    /**
	 * Holds the standard deviations of the numeric attributes in each cluster
	 */
    private Instances m_ClusterStdDevs;

    /**
	 * For each cluster, holds the frequency counts for the values of each nominal attribute
	 */
    private int[][][] m_ClusterNominalCounts;

    /**
	 * The number of instances in each cluster
	 */
    private int[] m_ClusterSizes;

    /**
	 * attribute min values
	 */
    private double[] m_Min;

    /**
	 * attribute max values
	 */
    private double[] m_Max;

    /**
	 * Keep track of the number of iterations completed before convergence
	 */
    private int m_Iterations = 0;

    /**
	 * Holds the squared errors for all clusters
	 */
    private double[] m_squaredErrors;

    /**
	 * the default constructor
	 */
    public DocumentKMeans() {
        super();
        m_SeedDefault = 10;
        setSeed(m_SeedDefault);
    }

    /**
	 * Returns a string describing this clusterer
	 * 
	 * @return a description of the evaluator suitable for displaying in the explorer/experimenter gui
	 */
    public String globalInfo() {
        return "Cluster data using the k means algorithm";
    }

    /**
	 * Returns default capabilities of the clusterer.
	 * 
	 * @return the capabilities of this clusterer
	 */
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enable(Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);
        return result;
    }

    /**
	 * Generates a clusterer. Has to initialize all fields of the clusterer that are not being set via options.
	 * 
	 * @param data
	 *            set of instances serving as training data
	 * @throws Exception
	 *             if the clusterer has not been generated successfully
	 */
    public void buildClusterer(Instances data) throws Exception {
        getCapabilities().testWithFail(data);
        m_Iterations = 0;
        m_ReplaceMissingFilter = new ReplaceMissingValues();
        Instances instances = new Instances(data);
        instances.setClassIndex(-1);
        m_ReplaceMissingFilter.setInputFormat(instances);
        instances = Filter.useFilter(instances, m_ReplaceMissingFilter);
        m_Min = new double[instances.numAttributes()];
        m_Max = new double[instances.numAttributes()];
        for (int i = 0; i < instances.numAttributes(); i++) {
            m_Min[i] = m_Max[i] = Double.NaN;
        }
        m_ClusterCentroids = new Instances(instances, m_NumClusters);
        int[] clusterAssignments = new int[instances.numInstances()];
        for (int i = 0; i < instances.numInstances(); i++) {
            updateMinMax(instances.instance(i));
        }
        Random RandomO = new Random(getSeed());
        int instIndex;
        HashMap initC = new HashMap();
        DecisionTable.hashKey hk = null;
        for (int j = instances.numInstances() - 1; j >= 0; j--) {
            instIndex = RandomO.nextInt(j + 1);
            hk = new DecisionTable.hashKey(instances.instance(instIndex), instances.numAttributes(), true);
            if (!initC.containsKey(hk)) {
                m_ClusterCentroids.add(instances.instance(instIndex));
                initC.put(hk, null);
            }
            instances.swap(j, instIndex);
            if (m_ClusterCentroids.numInstances() == m_NumClusters) {
                break;
            }
        }
        m_NumClusters = m_ClusterCentroids.numInstances();
        int i;
        boolean converged = false;
        int emptyClusterCount;
        Instances[] tempI = new Instances[m_NumClusters];
        m_squaredErrors = new double[m_NumClusters];
        m_ClusterNominalCounts = new int[m_NumClusters][instances.numAttributes()][0];
        while (!converged) {
            emptyClusterCount = 0;
            m_Iterations++;
            converged = true;
            for (i = 0; i < instances.numInstances(); i++) {
                Instance toCluster = instances.instance(i);
                int newC = clusterProcessedInstance(toCluster, true);
                if (newC != clusterAssignments[i]) {
                    converged = false;
                }
                clusterAssignments[i] = newC;
            }
            m_ClusterCentroids = new Instances(instances, m_NumClusters);
            for (i = 0; i < m_NumClusters; i++) {
                tempI[i] = new Instances(instances, 0);
            }
            for (i = 0; i < instances.numInstances(); i++) {
                tempI[clusterAssignments[i]].add(instances.instance(i));
            }
            for (i = 0; i < m_NumClusters; i++) {
                double[] vals = new double[instances.numAttributes()];
                if (tempI[i].numInstances() == 0) {
                    emptyClusterCount++;
                } else {
                    for (int j = 0; j < instances.numAttributes(); j++) {
                        vals[j] = tempI[i].meanOrMode(j);
                        m_ClusterNominalCounts[i][j] = tempI[i].attributeStats(j).nominalCounts;
                    }
                    m_ClusterCentroids.add(new Instance(1.0, vals));
                }
            }
            if (emptyClusterCount > 0) {
                m_NumClusters -= emptyClusterCount;
                tempI = new Instances[m_NumClusters];
            }
            if (!converged) {
                m_squaredErrors = new double[m_NumClusters];
                m_ClusterNominalCounts = new int[m_NumClusters][instances.numAttributes()][0];
            }
        }
        m_ClusterStdDevs = new Instances(instances, m_NumClusters);
        m_ClusterSizes = new int[m_NumClusters];
        for (i = 0; i < m_NumClusters; i++) {
            double[] vals2 = new double[instances.numAttributes()];
            for (int j = 0; j < instances.numAttributes(); j++) {
                if (instances.attribute(j).isNumeric()) {
                    vals2[j] = Math.sqrt(tempI[i].variance(j));
                } else {
                    vals2[j] = Instance.missingValue();
                }
            }
            m_ClusterStdDevs.add(new Instance(1.0, vals2));
            m_ClusterSizes[i] = tempI[i].numInstances();
        }
    }

    /**
	 * clusters an instance that has been through the filters
	 * 
	 * @param instance
	 *            the instance to assign a cluster to
	 * @param updateErrors
	 *            if true, update the within clusters sum of errors
	 * @return a cluster number
	 */
    private int clusterProcessedInstance(Instance instance, boolean updateErrors) {
        double minDist = Integer.MAX_VALUE;
        int bestCluster = 0;
        for (int i = 0; i < m_NumClusters; i++) {
            double dist = distance(instance, m_ClusterCentroids.instance(i));
            if (dist < minDist) {
                minDist = dist;
                bestCluster = i;
            }
        }
        if (updateErrors) {
            m_squaredErrors[bestCluster] += minDist;
        }
        return bestCluster;
    }

    /**
	 * Classifies a given instance.
	 * 
	 * @param instance
	 *            the instance to be assigned to a cluster
	 * @return the number of the assigned cluster as an interger if the class is enumerated, otherwise the predicted
	 *         value
	 * @throws Exception
	 *             if instance could not be classified successfully
	 */
    public int clusterInstance(Instance instance) throws Exception {
        m_ReplaceMissingFilter.input(instance);
        m_ReplaceMissingFilter.batchFinished();
        Instance inst = m_ReplaceMissingFilter.output();
        return clusterProcessedInstance(inst, false);
    }

    /**
	 * Calculates the distance between two instances
	 * 
	 * @param first
	 *            the first instance
	 * @param second
	 *            the second instance
	 * @return the distance between the two given instances, between 0 and 1
	 */
    private double distance(Instance first, Instance second) {
        double num = 0;
        double den1 = 0;
        double den2 = 0;
        double[] a = first.toDoubleArray();
        double[] b = second.toDoubleArray();
        for (int i = 0; i < a.length; i++) {
            if (a[i] != 0 && b[i] != 0) num += a[i] * b[i];
            if (a[i] != 0) den1 += a[i] * a[i];
            if (b[i] != 0) den2 += b[i] * b[i];
        }
        if (num != 0 && den1 != 0 && den2 != 0) return 1.0 - num / Math.sqrt(den1 * den2); else return 1;
    }

    /**
	 * Updates the minimum and maximum values for all the attributes based on a new instance.
	 * 
	 * @param instance
	 *            the new instance
	 */
    private void updateMinMax(Instance instance) {
        for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
            if (!instance.isMissing(j)) {
                if (Double.isNaN(m_Min[j])) {
                    m_Min[j] = instance.value(j);
                    m_Max[j] = instance.value(j);
                } else {
                    if (instance.value(j) < m_Min[j]) {
                        m_Min[j] = instance.value(j);
                    } else {
                        if (instance.value(j) > m_Max[j]) {
                            m_Max[j] = instance.value(j);
                        }
                    }
                }
            }
        }
    }

    /**
	 * Returns the number of clusters.
	 * 
	 * @return the number of clusters generated for a training dataset.
	 * @throws Exception
	 *             if number of clusters could not be returned successfully
	 */
    public int numberOfClusters() throws Exception {
        return m_NumClusters;
    }

    /**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
    public Enumeration listOptions() {
        Vector result = new Vector();
        result.addElement(new Option("\tnumber of clusters.\n" + "\t(default 2).", "N", 1, "-N <num>"));
        Enumeration en = super.listOptions();
        while (en.hasMoreElements()) result.addElement(en.nextElement());
        return result.elements();
    }

    /**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
    public String numClustersTipText() {
        return "set number of clusters";
    }

    /**
	 * set the number of clusters to generate
	 * 
	 * @param n
	 *            the number of clusters to generate
	 * @throws Exception
	 *             if number of clusters is negative
	 */
    public void setNumClusters(int n) throws Exception {
        if (n <= 0) {
            throw new Exception("Number of clusters must be > 0");
        }
        m_NumClusters = n;
    }

    /**
	 * gets the number of clusters to generate
	 * 
	 * @return the number of clusters to generate
	 */
    public int getNumClusters() {
        return m_NumClusters;
    }

    /**
	 * Parses a given list of options. <p/> <!-- options-start --> Valid options are: <p/>
	 * 
	 * <pre>
	 *  -N &lt;num&gt;
	 *  number of clusters.
	 *  (default 2).
	 * </pre>
	 * 
	 * <pre>
	 *  -S &lt;num&gt;
	 *  Random number seed.
	 *  (default 10)
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
    public void setOptions(String[] options) throws Exception {
        String optionString = Utils.getOption('N', options);
        if (optionString.length() != 0) {
            setNumClusters(Integer.parseInt(optionString));
        }
        super.setOptions(options);
    }

    /**
	 * Gets the current settings of SimpleKMeans
	 * 
	 * @return an array of strings suitable for passing to setOptions()
	 */
    public String[] getOptions() {
        int i;
        Vector result;
        String[] options;
        result = new Vector();
        result.add("-N");
        result.add("" + getNumClusters());
        options = super.getOptions();
        for (i = 0; i < options.length; i++) result.add(options[i]);
        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
	 * return a string describing this clusterer
	 * 
	 * @return a description of the clusterer as a string
	 */
    public String toString() {
        int maxWidth = 0;
        for (int i = 0; i < m_NumClusters; i++) {
            for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
                if (m_ClusterCentroids.attribute(j).isNumeric()) {
                    double width = Math.log(Math.abs(m_ClusterCentroids.instance(i).value(j))) / Math.log(10.0);
                    width += 1.0;
                    if ((int) width > maxWidth) {
                        maxWidth = (int) width;
                    }
                }
            }
        }
        StringBuffer temp = new StringBuffer();
        String naString = "N/A";
        for (int i = 0; i < maxWidth + 2; i++) {
            naString += " ";
        }
        temp.append("\nkMeans\n======\n");
        temp.append("\nNumber of iterations: " + m_Iterations + "\n");
        temp.append("Within cluster sum of squared errors: " + Utils.sum(m_squaredErrors));
        temp.append("\n\nCluster centroids:\n");
        for (int i = 0; i < m_NumClusters; i++) {
            temp.append("\nCluster " + i + "\n\t");
            temp.append("Mean/Mode: ");
            for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
                if (m_ClusterCentroids.attribute(j).isNominal()) {
                    temp.append(" " + m_ClusterCentroids.attribute(j).value((int) m_ClusterCentroids.instance(i).value(j)));
                } else {
                    temp.append(" " + Utils.doubleToString(m_ClusterCentroids.instance(i).value(j), maxWidth + 5, 4));
                }
            }
            temp.append("\n\tStd Devs:  ");
            for (int j = 0; j < m_ClusterStdDevs.numAttributes(); j++) {
                if (m_ClusterStdDevs.attribute(j).isNumeric()) {
                    temp.append(" " + Utils.doubleToString(m_ClusterStdDevs.instance(i).value(j), maxWidth + 5, 4));
                } else {
                    temp.append(" " + naString);
                }
            }
        }
        temp.append("\n\n");
        return temp.toString();
    }

    /**
	 * Gets the the cluster centroids
	 * 
	 * @return the cluster centroids
	 */
    public Instances getClusterCentroids() {
        return m_ClusterCentroids;
    }

    /**
	 * Gets the standard deviations of the numeric attributes in each cluster
	 * 
	 * @return the standard deviations of the numeric attributes in each cluster
	 */
    public Instances getClusterStandardDevs() {
        return m_ClusterStdDevs;
    }

    /**
	 * Returns for each cluster the frequency counts for the values of each nominal attribute
	 * 
	 * @return the counts
	 */
    public int[][][] getClusterNominalCounts() {
        return m_ClusterNominalCounts;
    }

    /**
	 * Gets the squared error for all clusters
	 * 
	 * @return the squared error
	 */
    public double getSquaredError() {
        return Utils.sum(m_squaredErrors);
    }

    /**
	 * Gets the number of instances in each cluster
	 * 
	 * @return The number of instances in each cluster
	 */
    public int[] getClusterSizes() {
        return m_ClusterSizes;
    }

    /**
	 * Main method for testing this class.
	 * 
	 * @param argv
	 *            should contain the following arguments:
	 *            <p>
	 *            -t training file [-N number of clusters]
	 */
    public static void main(String[] argv) {
        runClusterer(new DocumentKMeans(), argv);
    }
}
