package preprocessing.methods.FeatureSelection.StatMeasures;

import java.util.HashMap;
import java.util.Map;
import weka.core.Utils;

public class MIMeasureOK extends Measure {

    /** The number of each class value occurs in the dataset */
    private int[] m_ClassCounts;

    /** The number of each attribute value occurs in the dataset */
    private int[] m_AttCounts;

    /** The number of two attributes values occurs in the dataset */
    private int[][] m_AttAttCounts;

    /** The number of class and two attributes values occurs in the dataset */
    private int[][][] m_ClassAttAttCounts;

    /** The number of values for each attribute in the dataset */
    private int[] m_NumAttValues;

    /** The number of values for all attributes in the dataset */
    private int m_TotalAttValues;

    /** The number of classes in the dataset */
    private int m_NumClasses;

    /** The number of attributes including class in the dataset */
    private int m_NumAttributes;

    /** The number of instances in the dataset */
    private int m_NumInstances;

    /** The index of the class attribute in the dataset */
    private int m_ClassIndex;

    /** The starting index of each attribute in the dataset */
    private int[] m_StartAttIndex;

    /** The array of mutual information between each attribute and class */
    private double[] m_mutualInformation;

    private double[][] m_MImatrix;

    private double[] m_attEntropy;

    public double getMIbetweenTwoAtt(int i, int j) {
        if (m_MImatrix == null) computeMImatrix();
        return m_MImatrix[i][j];
    }

    public double[][] getMImatrix() {
        if (m_MImatrix == null) computeMImatrix();
        return m_MImatrix;
    }

    public double[] getClassAttMIarray() {
        return m_mutualInformation;
    }

    /** whether to print more internals in the toString method
     * @see #toString() */
    private Map[] infoMaps;

    public MIMeasureOK() {
        name = "MIMeasureOK";
    }

    public int NUM_ATTRIBUTES;

    public int NUM_CLASSES;

    private int NUM_INSTANCES;

    public int CLASS_INDEX;

    private double[][] data;

    @Override
    public void setData(double[][] data) {
        NUM_ATTRIBUTES = data[0].length;
        NUM_INSTANCES = data.length;
        CLASS_INDEX = NUM_ATTRIBUTES - 1;
        this.data = data;
        computeValuesCounts(this.data);
        NUM_CLASSES = infoMaps[CLASS_INDEX].size();
        try {
            buildMeasure(this.data);
        } catch (Exception e) {
            System.out.println("error in setData() in MIMeasureOK");
            e.printStackTrace();
        }
    }

    private void computeValuesCounts(double[][] data) {
        infoMaps = new HashMap[NUM_ATTRIBUTES];
        for (int i = 0; i < NUM_ATTRIBUTES; i++) {
            infoMaps[i] = new HashMap<Double, Value>();
        }
        for (int i = 0; i < NUM_INSTANCES; i++) {
            for (int j = 0; j < NUM_ATTRIBUTES; j++) {
                updateMap(j, data[i][j]);
            }
        }
    }

    private void updateMap(int attIndex, double value) {
        int count = 1;
        if (infoMaps[attIndex].containsKey(value)) ((Value) infoMaps[attIndex].get(value)).incrementValueCount(); else infoMaps[attIndex].put(value, new Value(count, infoMaps[attIndex].size()));
    }

    /**
     * Generates the classifier.
     *
     *
     * @throws Exception if the classifier has not been generated successfully
     */
    public void buildMeasure(double[][] data) throws Exception {
        if (NUM_ATTRIBUTES == 0) {
            System.err.println("Cannot build model (attribute present in data!)");
            return;
        }
        m_NumClasses = NUM_CLASSES;
        m_ClassIndex = CLASS_INDEX;
        m_NumAttributes = NUM_ATTRIBUTES;
        m_NumInstances = data.length;
        m_TotalAttValues = 0;
        m_StartAttIndex = new int[m_NumAttributes];
        m_NumAttValues = new int[m_NumAttributes];
        for (int i = 0; i < m_NumAttributes; i++) {
            if (i != m_ClassIndex) {
                m_StartAttIndex[i] = m_TotalAttValues;
                m_NumAttValues[i] = infoMaps[i].size();
                m_TotalAttValues += m_NumAttValues[i];
            } else {
                m_StartAttIndex[i] = -1;
                m_NumAttValues[i] = m_NumClasses;
            }
        }
        m_ClassCounts = new int[m_NumClasses];
        m_AttCounts = new int[m_TotalAttValues];
        m_AttAttCounts = new int[m_TotalAttValues][m_TotalAttValues];
        m_ClassAttAttCounts = new int[m_NumClasses][m_TotalAttValues][m_TotalAttValues];
        for (int k = 0; k < m_NumInstances; k++) {
            int classVal = (int) data[k][m_ClassIndex];
            m_ClassCounts[classVal]++;
            int[] attIndex = new int[m_NumAttributes];
            for (int i = 0; i < m_NumAttributes; i++) {
                if (i == m_ClassIndex) {
                    attIndex[i] = -1;
                } else {
                    attIndex[i] = m_StartAttIndex[i] + ((Value) infoMaps[i].get(data[k][i])).getValueIndex();
                    m_AttCounts[attIndex[i]]++;
                }
            }
            for (int Att1 = 0; Att1 < m_NumAttributes; Att1++) {
                if (attIndex[Att1] == -1) {
                    continue;
                }
                for (int Att2 = 0; Att2 < m_NumAttributes; Att2++) {
                    if ((attIndex[Att2] != -1)) {
                        m_AttAttCounts[attIndex[Att1]][attIndex[Att2]]++;
                        m_ClassAttAttCounts[classVal][attIndex[Att1]][attIndex[Att2]]++;
                    }
                }
            }
        }
        m_mutualInformation = new double[m_NumAttributes];
        for (int att = 0; att < m_NumAttributes; att++) {
            if (att == m_ClassIndex) {
                continue;
            }
            m_mutualInformation[att] = getMutualInfoWithClassAtt(att);
        }
    }

    /**
     * Computes mutual information between each attribute and class attribute.
     *
     * @param att is the attribute
     * @return the conditional mutual information between son and parent given class
     */
    private double getMutualInfoWithClassAtt(int att) {
        double mutualInfo = 0;
        int attIndex = m_StartAttIndex[att];
        double[] PriorsClass = new double[m_NumClasses];
        double[] PriorsAttribute = new double[m_NumAttValues[att]];
        double[][] PriorsClassAttribute = new double[m_NumClasses][m_NumAttValues[att]];
        for (int i = 0; i < m_NumClasses; i++) {
            PriorsClass[i] = (double) m_ClassCounts[i] / m_NumInstances;
        }
        for (int j = 0; j < m_NumAttValues[att]; j++) {
            PriorsAttribute[j] = (double) m_AttCounts[attIndex + j] / m_NumInstances;
        }
        for (int i = 0; i < m_NumClasses; i++) {
            for (int j = 0; j < m_NumAttValues[att]; j++) {
                PriorsClassAttribute[i][j] = (double) m_ClassAttAttCounts[i][attIndex + j][attIndex + j] / m_NumInstances;
            }
        }
        for (int i = 0; i < m_NumClasses; i++) {
            for (int j = 0; j < m_NumAttValues[att]; j++) {
                mutualInfo += PriorsClassAttribute[i][j] * log2(PriorsClassAttribute[i][j], PriorsClass[i] * PriorsAttribute[j]);
            }
        }
        return mutualInfo;
    }

    /**
     * compute the logarithm whose base is 2.
     *
     * @param x numerator of the fraction.
     * @param y denominator of the fraction.
     * @return the natual logarithm of this fraction.
     */
    private double log2(double x, double y) {
        if (x < Utils.SMALL || y < Utils.SMALL) {
            return 0.0;
        } else {
            return Math.log(x / y) / Math.log(2);
        }
    }

    /**
     * compute the logarithm whose base is 2.
     *
     * @param x the value which log is computed from
     * @return the natual logarithm of this fraction.
     */
    private double log2(double x) {
        if (x < Utils.SMALL) {
            return 0.0;
        } else {
            return Math.log(x) / Math.log(2);
        }
    }

    /**
     * Computes mutual information between pair of attributes
     *
     * @param att1 is the first attribute
     * @param att2 is the second attribute
     * @return the conditional mutual information between son and parent
     */
    private double getAttAttMI(int att1, int att2) {
        double mutualInfo = 0;
        int attIndex1 = m_StartAttIndex[att1];
        int attIndex2 = m_StartAttIndex[att2];
        double[] PriorsAttribute1 = new double[m_NumAttValues[att1]];
        double[] PriorsAttribute2 = new double[m_NumAttValues[att2]];
        double[][] PriorsAtt1Att2 = new double[m_NumAttValues[1]][m_NumAttValues[2]];
        for (int i = 0; i < m_NumAttValues[att1]; i++) {
            PriorsAttribute1[i] = (double) m_AttCounts[attIndex1 + i] / m_NumInstances;
        }
        for (int j = 0; j < m_NumAttValues[att2]; j++) {
            PriorsAttribute2[j] = (double) m_AttCounts[attIndex2 + j] / m_NumInstances;
        }
        for (int i = 0; i < m_NumAttValues[att1]; i++) {
            for (int j = 0; j < m_NumAttValues[att2]; j++) {
                PriorsAtt1Att2[i][j] = (double) m_AttAttCounts[attIndex1 + i][attIndex2 + j] / m_NumInstances;
            }
        }
        for (int i = 0; i < m_NumAttValues[att1]; i++) {
            for (int j = 0; j < m_NumAttValues[att2]; j++) {
                mutualInfo += PriorsAtt1Att2[i][j] * log2(PriorsAtt1Att2[i][j], PriorsAttribute1[i] * PriorsAttribute2[j]);
            }
        }
        return mutualInfo;
    }

    public double getAttEntropy(int att) {
        if (m_attEntropy == null) computeAllAttEntropy();
        return m_attEntropy[att];
    }

    private void computeAllAttEntropy() {
        double ent;
        m_attEntropy = new double[NUM_ATTRIBUTES];
        for (int att = 0; att < NUM_ATTRIBUTES; att++) {
            ent = 0;
            for (int i = 0; i < m_NumAttValues[att]; i++) {
                ent += m_AttCounts[att] * log2(m_AttCounts[att]);
            }
            m_attEntropy[att] = ent;
        }
    }

    private class Value {

        private int valueIndex = 0;

        public int getValueIndex() {
            return valueIndex;
        }

        public void setValueIndex(int valueIndex) {
            this.valueIndex = valueIndex;
        }

        private int valueCount = 0;

        public Value(int count, int index) {
            valueCount = 1;
            setValueIndex(index);
        }

        public void incrementValueCount() {
            valueCount++;
        }

        public int getValueCount() {
            return valueCount;
        }
    }

    private void computeMImatrix() {
        int numInAtt = NUM_ATTRIBUTES - 1;
        m_MImatrix = new double[numInAtt][numInAtt];
        for (int i = 0; i < numInAtt; i++) {
            for (int j = i; j < numInAtt; j++) {
                m_MImatrix[i][j] = getAttAttMI(i, j);
                m_MImatrix[j][i] = m_MImatrix[i][j];
            }
        }
    }
}
