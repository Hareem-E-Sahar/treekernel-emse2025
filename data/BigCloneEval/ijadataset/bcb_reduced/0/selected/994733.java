package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.r.ClassAssigner;

/**
 * General loader.  
 * 
 * Handles the specific cases:
 * 1	Read all the data that was already loaded into MeV
 * 2	Read only the test hybs that were loaded into MeV
 * 3	Read a training file
 * 
 * In every case, the data is formatted into USCHybSets
 * 
 * @author vu
 */
public class USCTrainFileLoader {

    private USCHybSet trainHybSet;

    private USCHybSet testHybSet;

    private double delta;

    private double rho;

    /**
	 * Creates a USCHybSet from an IData implementation
	 * @param data
	 * @param hybLabels	[ String ] names of hybs that are "Unknown (Test)"
	 */
    public USCTrainFileLoader(IData data, String[] hybLabels) {
        int[] sortedIndices = data.getSortedIndices(0);
        Vector vInclude = new Vector();
        Vector vTest = new Vector();
        for (int i = 0; i < hybLabels.length; i++) {
            if (!hybLabels[i].equals(ClassAssigner.TEST_CLASS_STRING)) {
                vInclude.add(new Integer(i));
            } else {
                vTest.add(new Integer(i));
            }
        }
        int hybKount = vInclude.size();
        int testKount = vTest.size();
        USCHyb[] hybArray = new USCHyb[hybKount];
        USCHyb[] testArray = new USCHyb[testKount];
        double[][] ratios = USCGUI.castFloatToDoubleArray(this.transpose(data.getExperiment().getValues()));
        for (int i = 0; i < ratios.length; i++) {
            for (int j = 0; j < ratios[i].length; j++) {
                if (ratios[i][j] == Float.NaN) {
                    System.out.println("Nan");
                } else if (ratios[i][j] == Float.NEGATIVE_INFINITY || ratios[i][j] == Float.POSITIVE_INFINITY) {
                    System.out.println("Infinity");
                }
            }
        }
        for (int h = 0; h < hybKount; h++) {
            Integer I = (Integer) vInclude.elementAt(h);
            int iIndex = I.intValue();
            String sHybName = data.getFullSampleName(iIndex);
            USCHyb hyb = new USCHyb(h, hybLabels[iIndex], sHybName, ratios[iIndex]);
            hybArray[h] = hyb;
        }
        for (int h = 0; h < testKount; h++) {
            Integer I = (Integer) vTest.elementAt(h);
            int iIndex = I.intValue();
            String sHybName = data.getFullSampleName(iIndex);
            USCHyb hyb = new USCHyb(h, ClassAssigner.TEST_CLASS_STRING, sHybName, ratios[iIndex]);
            testArray[h] = hyb;
        }
        this.trainHybSet = new USCHybSet(hybArray, this.createGeneList(data));
        this.testHybSet = new USCHybSet(testArray, this.createGeneList(data));
    }

    /**
	 * Creates a USCHybSet from an IData implementation.  Used when getting test
	 * data to test against Saved Train Data
	 * @param data
	 */
    public USCTrainFileLoader(IData data) {
        int[] sortedIndices = data.getSortedIndices(0);
        int testKount = data.getFeaturesCount();
        USCHyb[] testArray = new USCHyb[testKount];
        float[][] tempRatios = data.getExperiment().getValues();
        double[][] ratios = USCGUI.castFloatToDoubleArray(this.transpose(tempRatios));
        for (int h = 0; h < ratios.length; h++) {
            testArray[h] = new USCHyb(h, ClassAssigner.TEST_CLASS_STRING, data.getFullSampleName(h), ratios[h]);
        }
        this.testHybSet = new USCHybSet(testArray, this.createGeneList(data));
    }

    /**
	 * Parse the Training File into USCHybSet
	 * <p>
	 * 'GeneID'	Hyb1Name	Hyb2Name	etc<br>
	 * 'blank'	Hyb1Label	Hyb2Label	etc<br>
	 * GeneID1	UID1		ratio1		ratio2		etc<br>
	 * GeneID2	UID2		ratio1		ratio2		etc<br>
	 * etc		etc			etc			etc<br>
	 * <p>
	 * Important: When the labels are unknown, 'blank' should be used in lieu of labels
	 */
    public USCTrainFileLoader(File f) throws IOException {
        int hybKount = 0;
        int geneKount = 0;
        String[] geneNames = null;
        String[] geneIndex = null;
        USCHyb[] hybs = null;
        Reader r = new Reader();
        r.readFile(f);
        Vector v = r.getVNullLine(USCGUI.NULL_REPLACER);
        for (int i = 0; i < v.size(); i++) {
            String line = (String) v.elementAt(i);
            StringTokenizer st = new StringTokenizer(line, USCGUI.TAB);
            int tokenKount = st.countTokens();
            if (i == 0) {
                hybKount = tokenKount - 2;
                hybs = new USCHyb[hybKount];
                geneKount = v.size() - 2;
                geneNames = new String[geneKount];
                geneIndex = new String[geneKount];
                for (int j = 0; j < tokenKount; j++) {
                    String hybName = st.nextToken();
                    if (j == 0) {
                        this.delta = this.parseDR(hybName);
                    } else if (j == 1) {
                    } else {
                        USCHyb hyb = new USCHyb((j - 1), hybName, geneKount);
                        hybs[(j - 2)] = hyb;
                    }
                }
            } else if (i == 1) {
                for (int j = 0; j < tokenKount; j++) {
                    String s = st.nextToken();
                    if (j == 0) {
                        this.rho = this.parseDR(s);
                    } else if (j == 1) {
                    } else {
                        hybs[(j - 2)].setHybLabel(s);
                    }
                }
            } else {
                for (int j = 0; j < tokenKount; j++) {
                    String s = st.nextToken();
                    if (j == 0) {
                        geneNames[(i - 2)] = s;
                    } else if (j == 1) {
                        geneIndex[(i - 2)] = s;
                    } else {
                        Float FRatio = new Float(s);
                        if (FRatio.isNaN()) {
                            hybs[(j - 2)].setRatio((i - 2), 0.0f);
                        } else {
                            hybs[(j - 2)].setRatio((i - 2), FRatio.doubleValue());
                        }
                    }
                }
            }
        }
        USCGene[] genes = new USCGene[geneNames.length];
        for (int i = 0; i < geneNames.length; i++) {
            genes[i] = new USCGene(geneNames[i], null);
        }
        int[] geneIndices = this.intifyStringArray(geneIndex);
        this.trainHybSet = new USCHybSet(hybs, genes);
    }

    /**
	 * 
	 * @param data
	 * @param geneIndices
	 * @return
	 */
    private USCGene[] createGeneList(IData data) {
        int numGenes = data.getFeaturesSize();
        USCGene[] genes = new USCGene[numGenes];
        for (int i = 0; i < numGenes; i++) {
            String geneName = data.getGeneName(i);
            String[] extraFields = data.getSlideDataElement(0, i).getExtraFields();
            USCGene gene = new USCGene(geneName, extraFields);
            genes[i] = gene;
        }
        return genes;
    }

    /**
	 * Removes ratios that were not used during training and thus are not present in 
	 * the Training Result File and will not be used for classification
	 * @param ratios
	 * @param geneIndex
	 * @return
	 */
    private float[][] condenseRatios(float[][] ratios, int[] geneIndex) {
        float[][] toReturn = new float[geneIndex.length][ratios.length];
        for (int i = 0; i < geneIndex.length; i++) {
            toReturn[i] = ratios[geneIndex[i]];
        }
        return toReturn;
    }

    /**
	 * Converst the String[] to an int[]
	 * @param sInts
	 * @return
	 */
    private int[] intifyStringArray(String[] sInts) {
        int[] toReturn = new int[sInts.length];
        for (int i = 0; i < sInts.length; i++) {
            toReturn[i] = new Integer(sInts[i]).intValue();
        }
        return toReturn;
    }

    /**
	 * 
	 * @param sDR
	 * @return
	 */
    private double parseDR(String sDR) {
        int iEqual = sDR.indexOf("=");
        Float F = new Float(sDR.substring(iEqual + 1));
        return F.doubleValue();
    }

    /**
	 * Constructor for testing purposes
	 * @param m
	 */
    public USCTrainFileLoader(double[][] m) {
    }

    /**
	 * Transposes the ith and jth elements of a 2D double[ i ][ j ] matrix
	 * @param m
	 * @return
	 */
    private float[][] transpose(float[][] m) {
        float[][] toReturn = new float[m[0].length][m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                toReturn[j][i] = m[i][j];
            }
        }
        return toReturn;
    }

    public static void main(String[] args) {
        double[][] m = new double[3][2];
        m[0][0] = 0;
        m[1][0] = 1;
        m[2][0] = 2;
        m[0][1] = 3;
        m[1][1] = 4;
        m[2][1] = 5;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                System.out.println(i + "," + j + " = " + m[i][j]);
            }
        }
        USCTrainFileLoader loader = new USCTrainFileLoader(m);
    }

    public USCHybSet getTrainHybSet() {
        return this.trainHybSet;
    }

    public USCHybSet getTestHybSet() {
        return this.testHybSet;
    }

    public double getDelta() {
        return this.delta;
    }

    public double getRho() {
        return this.rho;
    }
}
