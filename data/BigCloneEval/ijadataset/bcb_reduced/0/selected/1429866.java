package clustering.testing;

import java.awt.*;
import java.awt.event.*;
import clustering.implementations.*;
import clustering.framework.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.FileOutputStream;

;

/**
 * @author Tudor
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code	 Style - Code Templates
 */
public class ClusteringMain {

    static Frame frmWindow = null;

    static Object[] loadMatrix(String sMatrixFile) throws Exception {
        File f = new File(sMatrixFile);
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        String sMatrix = new String(data);
        sMatrix = sMatrix.replace('-', ' ');
        sMatrix = sMatrix.replaceAll("\r\n ", " ");
        StringTokenizer stLines = new StringTokenizer(sMatrix, "\n\r");
        String[] sLines = new String[stLines.countTokens()];
        int lines = 0;
        while (stLines.hasMoreTokens()) {
            sLines[lines++] = stLines.nextToken();
        }
        String sn = "";
        for (int i = 0; i < sLines[0].length(); i++) {
            if (sLines[0].charAt(i) != ' ') {
                sn += sLines[0].charAt(i);
            }
        }
        int n = Integer.parseInt(sn);
        double[][] matrix = new double[n][n];
        String[] filesList = new String[n];
        for (int i = 0; i < n; i++) {
            StringTokenizer stValues = new StringTokenizer(sLines[i + 1], "\t ");
            filesList[i] = stValues.nextToken();
            for (int j = 0; j < n; j++) {
                matrix[i][j] = Double.parseDouble(stValues.nextToken());
            }
        }
        Object[] oToReturn = new Object[2];
        oToReturn[0] = filesList;
        oToReturn[1] = matrix;
        return oToReturn;
    }

    static String[] loadDMList(String sMatrixFile) throws Exception {
        File f = new File(sMatrixFile);
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        String sMatrix = new String(data);
        StringTokenizer stLines = new StringTokenizer(sMatrix, "\n\r");
        String[] sLines = new String[stLines.countTokens()];
        int lines = 0;
        while (stLines.hasMoreTokens()) {
            sLines[lines++] = stLines.nextToken();
        }
        return sLines;
    }

    public static Choice chFileType;

    public static Choice chComp;

    public static Choice chAlgo;

    public static TextArea taDisplay;

    public static TextField tfFile;

    public static TextField tfSteps;

    public static TextField tfTree1;

    public static TextField tfTree2;

    public static Button btnStart;

    public static Button btnCompareTrees;

    public static Panel pLevel1;

    public static Panel pLevel2;

    public static Panel pLevel3;

    public static Panel pLevel4;

    public static ButtonListener blCluster = new ButtonListener();

    static double[][] symm(double[][] dMatrix) {
        for (int i = 0; i < dMatrix.length; i++) {
            for (int j = i; j < dMatrix.length; j++) {
                if (i == j) {
                    dMatrix[i][j] = 0;
                    dMatrix[j][i] = 0;
                }
                {
                    double dist = (dMatrix[i][j] + dMatrix[j][i]) / 2.0;
                    dMatrix[i][j] = dist;
                    dMatrix[j][i] = dMatrix[i][j];
                }
            }
        }
        return dMatrix;
    }

    public static void saveTreeFiles(String[] filesList, double[][] dMatrix, String[] allTrees) throws Exception {
        FileOutputStream fos = new FileOutputStream("alltrees.out");
        for (int k = 0; k < allTrees.length; k++) {
            for (int i = dMatrix.length - 1; i >= 0; i--) {
                allTrees[k] = allTrees[k].replaceAll(i + "", getUniqueCode(i));
            }
            for (int i = dMatrix.length - 1; i >= 0; i--) {
                allTrees[k] = allTrees[k].replaceAll(getUniqueCode(i), filesList[i].substring(filesList[i].lastIndexOf("\\") + 1));
            }
            fos.write((allTrees[k] + "\n").getBytes());
        }
        fos.close();
    }

    public static void saveTreeFiles(String[] filesList, double[][] dMatrix, String allTrees) throws Exception {
        FileOutputStream fos = new FileOutputStream("alltrees.out");
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            allTrees = allTrees.replaceAll(i + "", getUniqueCode(i));
        }
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            allTrees = allTrees.replaceAll(getUniqueCode(i), filesList[i].substring(filesList[i].lastIndexOf("\\") + 1));
        }
        fos.write((allTrees + "\n").getBytes());
        fos.close();
    }

    public static String removeBranchLengths(String newick) {
        String s = "";
        boolean ignore = false;
        for (int i = 0; i < newick.length(); i++) {
            char c = newick.charAt(i);
            if (c == ':') ignore = true; else if (c == ',' || c == ')') ignore = false;
            if (!ignore) {
                s += c;
            }
        }
        return s;
    }

    public static void main(String[] args) throws Exception {
        String[] filesList;
        double[][] dMatrix;
        ButtonListener bl = new ButtonListener();
        if (args.length != 0) {
            String fileName = args[1];
            if (args[0].toLowerCase().compareTo("dr") == 0) {
                int steps = 1;
                try {
                    steps = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    steps = 1;
                }
                Object[] oFilesAndMatrix = ClusteringMain.loadMatrix(fileName);
                filesList = (String[]) oFilesAndMatrix[0];
                dMatrix = (double[][]) oFilesAndMatrix[1];
                dMatrix = symm(dMatrix);
                try {
                    MTCQPRandomTreeConstructor.QC = Integer.parseInt(args[3]) - 1;
                } catch (Exception ex) {
                    MTCQPRandomTreeConstructor.QC = dMatrix.length;
                }
                bl = new ButtonListener();
                bl.ConstructRandomTree(dMatrix, filesList, false, steps);
                saveTreeFiles(filesList, dMatrix, MTCQPRandomTreeConstructor.allTrees);
            } else if (args[0].toLowerCase().compareTo("fm") == 0) {
                int steps = 1;
                try {
                    steps = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    steps = 1;
                }
                Object[] oFilesAndMatrix = ClusteringMain.loadMatrix(fileName);
                filesList = (String[]) oFilesAndMatrix[0];
                dMatrix = (double[][]) oFilesAndMatrix[1];
                dMatrix = symm(dMatrix);
                try {
                    MTCQPExternalCriterionTreeConstructor.QC = Integer.parseInt(args[3]) - 1;
                } catch (Exception ex) {
                    MTCQPExternalCriterionTreeConstructor.QC = dMatrix.length;
                }
                bl = new ButtonListener();
                bl.ConstructExternalCriterionTree(dMatrix, filesList, false, steps, new LeastSquaresEvaluator());
            } else if (args[0].toLowerCase().compareTo("ml") == 0) {
                int steps = 1;
                try {
                    steps = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    steps = 1;
                }
                Object[] oFilesAndMatrix = ClusteringMain.loadMatrix(fileName);
                filesList = (String[]) oFilesAndMatrix[0];
                dMatrix = (double[][]) oFilesAndMatrix[1];
                dMatrix = symm(dMatrix);
                try {
                    MTCQPExternalCriterionTreeConstructor.QC = Integer.parseInt(args[3]) - 1;
                } catch (Exception ex) {
                    MTCQPExternalCriterionTreeConstructor.QC = dMatrix.length;
                }
                bl = new ButtonListener();
                bl.ConstructExternalCriterionTree(dMatrix, filesList, false, steps, new MaximumLikelihoodEvaluator());
            } else if (args[0].toLowerCase().compareTo("td") == 0) {
                int steps = 1;
                try {
                    steps = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    steps = 1;
                }
                Object[] oFilesAndMatrix = ClusteringMain.loadMatrix(fileName);
                filesList = (String[]) oFilesAndMatrix[0];
                dMatrix = (double[][]) oFilesAndMatrix[1];
                dMatrix = symm(dMatrix);
                try {
                    MTCQPExternalCriterionTreeConstructor.QC = Integer.parseInt(args[3]) - 1;
                } catch (Exception ex) {
                    MTCQPExternalCriterionTreeConstructor.QC = dMatrix.length;
                }
                bl = new ButtonListener();
                bl.ConstructExternalCriterionTree(dMatrix, filesList, false, steps, new TreedistEvaluator());
            } else if (args[0].toLowerCase().compareTo("dm") == 0) {
                int steps = 1;
                try {
                    steps = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    steps = 1;
                }
                Object[] oFilesAndMatrix = ClusteringMain.loadMatrix(fileName);
                filesList = (String[]) oFilesAndMatrix[0];
                dMatrix = (double[][]) oFilesAndMatrix[1];
                dMatrix = symm(dMatrix);
                bl = new ButtonListener();
                bl.ConstructTree(dMatrix, filesList, false, steps);
            }
        } else {
            System.out.println("Please refer to the README file for a valid list of arguments.");
        }
    }

    static String getUniqueCode(int k) {
        String code = "";
        char[] digits = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j' };
        for (int i = 0; i < 5; i++) {
            code = digits[k % 10] + code;
            k = k / 10;
        }
        return code;
    }

    static double evolution_rate = 0.25;

    static String getTree(IClusterTreeConstructor utc, double[][] dMatrix, String[] filesList) throws Exception {
        String xmlTree = utc.ConstructXMLTree(dMatrix);
        File f = new File("test.tree");
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        fis.close();
        String phyTree = new String(data);
        ArrayList bl_al = new ArrayList();
        Random rand = new Random();
        double sum = 0;
        for (int i = 0; i < 2 * dMatrix.length - 3; i++) {
            double number = 1 + rand.nextInt(dMatrix.length) + rand.nextDouble();
            sum += number;
            bl_al.add(new Double(number));
        }
        double scale_factor = sum / ((double) (2 * dMatrix.length - 3) * evolution_rate);
        for (int i = 0; i < 2 * dMatrix.length - 3; i++) {
            Double number = (Double) bl_al.get(i);
            number = new Double(number.doubleValue() / scale_factor);
            bl_al.set(i, number);
        }
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            phyTree = phyTree.replaceAll(i + "", getUniqueCode(i));
        }
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            if (bl_al.get(0).toString().length() < 8) {
                phyTree = phyTree.replaceFirst(getUniqueCode(i), getUniqueCode(i) + ":" + bl_al.get(0).toString());
            } else {
                phyTree = phyTree.replaceFirst(getUniqueCode(i), getUniqueCode(i) + ":" + bl_al.get(0).toString().substring(0, 8));
            }
            bl_al.remove(0);
            phyTree = phyTree.replaceAll(getUniqueCode(i), filesList[i].substring(filesList[i].lastIndexOf("\\") + 1));
        }
        String newPhyTree = "";
        for (int i = 0; i < phyTree.length(); i++) {
            char c = phyTree.charAt(i);
            if (c == ')' && i < phyTree.length() - 2) {
                if (bl_al.get(0).toString().length() < 8) {
                    newPhyTree += phyTree.charAt(i) + ":" + bl_al.get(0).toString();
                } else {
                    newPhyTree += phyTree.charAt(i) + ":" + bl_al.get(0).toString().substring(0, 8);
                }
                bl_al.remove(0);
            } else {
                newPhyTree += phyTree.charAt(i);
            }
        }
        phyTree = newPhyTree;
        System.out.println(phyTree);
        FileOutputStream fos = new FileOutputStream("test.xml");
        fos.write(xmlTree.getBytes());
        fos.close();
        fos = new FileOutputStream("clustio.tree");
        fos.write(phyTree.getBytes());
        fos.close();
        return phyTree;
    }

    static String getTreeExternal(MTCQPExternalCriterionTreeConstructor utc, double[][] dMatrix, String[] filesList, IEvaluator eval) throws Exception {
        String phyTree = utc.ConstructXMLTree(dMatrix, filesList, eval);
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            phyTree = phyTree.replaceAll(i + "", getUniqueCode(i));
        }
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            phyTree = phyTree.replaceAll(getUniqueCode(i), filesList[i].substring(filesList[i].lastIndexOf("\\") + 1));
        }
        FileOutputStream fos = new FileOutputStream("clustio.tree");
        fos.write(phyTree.getBytes());
        fos.close();
        return phyTree;
    }

    static String getTreeExternal2(MTCQPExternalCriterion2 utc, double[][] dMatrix, String[] filesList, IEvaluator eval) throws Exception {
        String xmlTree = utc.ConstructXMLTree(dMatrix, filesList, eval);
        File f = new File("test.tree");
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        fis.close();
        String phyTree = new String(data);
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            phyTree = phyTree.replaceAll(i + "", getUniqueCode(i));
        }
        for (int i = dMatrix.length - 1; i >= 0; i--) {
            phyTree = phyTree.replaceAll(getUniqueCode(i), filesList[i].substring(filesList[i].lastIndexOf("\\") + 1));
        }
        FileOutputStream fos = new FileOutputStream("test.xml");
        fos.write(xmlTree.getBytes());
        fos.close();
        fos = new FileOutputStream("clustio.tree");
        fos.write(phyTree.getBytes());
        fos.close();
        return phyTree;
    }
}

class ButtonListener {

    String[] extractFilesList(Document xmlTree) {
        ArrayList alFiles = new ArrayList();
        NodeList nl = xmlTree.getElementsByTagName("node");
        int leafs = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getAttributes().getNamedItem("id").getNodeValue().compareTo("-1") != 0) {
                alFiles.add(n.getAttributes().getNamedItem("id").getNodeValue());
                n.getAttributes().getNamedItem("id").setNodeValue("" + leafs);
                leafs++;
            }
        }
        String[] filesList = new String[alFiles.size()];
        for (int i = 0; i < alFiles.size(); i++) {
            filesList[i] = (String) alFiles.get(i);
        }
        return filesList;
    }

    public void actionPerformed(ActionEvent evt) {
    }

    String ConstructTree(double[][] dMatrix, String[] filesList, boolean displaytree, int K) throws Exception {
        String xmlDoc = null;
        Date now = new Date();
        IClusterTreeConstructor utc = null;
        String sDebug = "";
        utc = new MCQP11TreeConstructor();
        ((MCQP11TreeConstructor) utc).K = K;
        xmlDoc = ClusteringMain.getTree(utc, dMatrix, filesList);
        now = new Date();
        return xmlDoc;
    }

    String ConstructRandomTree(double[][] dMatrix, String[] filesList, boolean displaytree, int K) throws Exception {
        String xmlDoc = null;
        Date now = new Date();
        IClusterTreeConstructor utc = null;
        String sDebug = "";
        utc = new MTCQPRandomTreeConstructor();
        ((MTCQPRandomTreeConstructor) utc).K = K;
        xmlDoc = ClusteringMain.getTree(utc, dMatrix, filesList);
        now = new Date();
        return xmlDoc;
    }

    String ConstructExternalCriterionTree(double[][] dMatrix, String[] filesList, boolean displaytree, int K, IEvaluator eval) throws Exception {
        String xmlDoc = null;
        Date now = new Date();
        MTCQPExternalCriterionTreeConstructor utc = null;
        String sDebug = "";
        utc = new MTCQPExternalCriterionTreeConstructor();
        ((MTCQPExternalCriterionTreeConstructor) utc).K = K;
        xmlDoc = ClusteringMain.getTreeExternal(utc, dMatrix, filesList, eval);
        now = new Date();
        return xmlDoc;
    }

    String ConstructExternalCriterionTree2(double[][] dMatrix, String[] filesList, boolean displaytree, int K, IEvaluator eval) throws Exception {
        String xmlDoc = null;
        Date now = new Date();
        MTCQPExternalCriterion2 utc = null;
        String sDebug = "";
        utc = new MTCQPExternalCriterion2();
        ((MTCQPExternalCriterion2) utc).K = K;
        xmlDoc = ClusteringMain.getTreeExternal2(utc, dMatrix, filesList, eval);
        now = new Date();
        return xmlDoc;
    }

    File convertToXml(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[(int) f.length()];
        fis.read(data);
        fis.close();
        String newick = new String(data);
        String clean_newick = "";
        boolean ignore = false;
        for (int i = 0; i < newick.length(); i++) {
            if (newick.charAt(i) == ':') {
                ignore = true;
            } else if (newick.charAt(i) == ',' || newick.charAt(i) == ')') {
                ignore = false;
            }
            if (!ignore) {
                clean_newick += newick.charAt(i);
            }
        }
        clean_newick = clean_newick.replaceAll("\r", "");
        clean_newick = clean_newick.replaceAll("\n", "");
        data = new byte[clean_newick.length()];
        data = clean_newick.getBytes();
        String xml = QuartetTree.newick2Xml(new String(data));
        f = new File(f.getPath() + ".xml");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(xml.getBytes());
        fos.close();
        return f;
    }
}
