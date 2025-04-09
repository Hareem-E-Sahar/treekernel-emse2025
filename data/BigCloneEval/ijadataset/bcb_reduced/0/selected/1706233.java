package org.tigr.microarray.mev.r;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.tree.DefaultMutableTreeNode;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.Manager;
import org.tigr.microarray.mev.MultipleArrayMenubar;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;

/**
 * @author iVu
 */
public class Rama {

    public static String COMMA = ",";

    public static String END_LINE = "\r\n";

    public static String TAB = "\t";

    public static String R_VECTOR_NAME = "ramaData";

    private MultipleArrayViewer mav;

    private MultipleArrayMenubar menuBar;

    private IData data;

    private RamaInitDialog initDialog;

    private int B;

    private int minIter;

    private int iGene;

    private RconnectionManager rcMan;

    private Rconnection rc;

    /**
	 * 
	 * @param mavP
	 * @param menuBarP
	 */
    public Rama(MultipleArrayViewer mavP, MultipleArrayMenubar menuBarP) {
        this.mav = mavP;
        this.menuBar = menuBarP;
        this.data = this.mav.getData();
        if (this.data.getFeaturesCount() < 2) {
            this.error("The loaded dataset doesn't appear to be \"Ramalizable\"" + "\r\nYou must have at least 2 replicates of each sample");
        } else if (data.getDataType() == IData.DATA_TYPE_RATIO_ONLY) {
            this.error("Rama does not work on Ratio data.\nIt only works with Intensity data.");
        } else if (data.getDataType() == IData.DATA_TYPE_AFFY_MEAN) {
            this.error("Rama does not work on Ratio data.\nIt only works with Intensity data.");
        } else if (data.getDataType() == IData.DATA_TYPE_AFFY_MEDIAN) {
            this.error("Rama does not work on Ratio data.\nIt only works with Intensity data.");
        } else if (data.getDataType() == IData.DATA_TYPE_AFFY_REF) {
            this.error("Rama does not work on Ratio data.\nIt only works with Intensity data.");
        } else if (data.getDataType() == IData.DATA_TYPE_AFFY_ABS) {
            this.ramify(this.data, true);
        } else {
            this.ramify(this.data, false);
        }
    }

    /**
	 * 
	 * @param data
	 */
    private void ramify(IData data, boolean isAffy) {
        String[] hybNames = this.gatherHybNames(data);
        if (isAffy) {
            this.initDialog = new RamaInitDialog(this.mav.getFrame(), hybNames, IData.DATA_TYPE_AFFY_ABS);
        } else {
            this.initDialog = new RamaInitDialog(this.mav.getFrame(), hybNames, IData.DATA_TYPE_TWO_INTENSITY);
        }
        if (this.initDialog.showModal() == JOptionPane.OK_OPTION) {
            this.B = this.initDialog.getNumIter();
            this.minIter = this.initDialog.getBurnIn();
            boolean allOut = this.initDialog.getAllOut();
            String sConnPort = this.initDialog.getSelectedConnString();
            String sConn = this.parseSPort(sConnPort);
            int iPort = this.parseIPort(sConnPort);
            RDataFormatter rDataFormatter = new RDataFormatter(data);
            String sData;
            int nbCol1;
            int iHybKount;
            int iColorKount;
            int iTwo;
            RHybSet rhs = this.initDialog.getRamaHybSet();
            if (isAffy) {
                sData = rDataFormatter.rNonSwapString(Rama.R_VECTOR_NAME, rhs.getVRamaHyb());
                this.iGene = data.getExperiment().getNumberOfGenes();
                nbCol1 = 0;
                iHybKount = rhs.getVRamaHyb().size();
                iColorKount = iHybKount * 2;
                iTwo = iHybKount + 1;
            } else {
                if (rhs.isFlip()) {
                    Vector vTreatCy3 = this.getVRamaHybTreatCy3(rhs.getVRamaHyb());
                    Vector vTreatCy5 = this.getVRamaHybTreatCy5(rhs.getVRamaHyb());
                    sData = rDataFormatter.rSwapString(Rama.R_VECTOR_NAME, vTreatCy3, vTreatCy5);
                    this.iGene = data.getExperiment().getNumberOfGenes();
                    nbCol1 = vTreatCy3.size();
                    iHybKount = vTreatCy3.size() + vTreatCy5.size();
                    iColorKount = iHybKount * 2;
                    iTwo = iHybKount + 1;
                } else {
                    sData = rDataFormatter.rNonSwapString(Rama.R_VECTOR_NAME, rhs.getVRamaHyb());
                    this.iGene = data.getExperiment().getNumberOfGenes();
                    nbCol1 = 0;
                    iHybKount = rhs.getVRamaHyb().size();
                    iColorKount = iHybKount * 2;
                    iTwo = iHybKount + 1;
                }
            }
            String message = "As a reference, 4 arrays (640 genes) takes about half an hour";
            RProgress progress = new RProgress(this.mav.getFrame(), message);
            this.rcMan = new RconnectionManager(this.mav.getFrame(), sConn, iPort);
            this.rc = this.rcMan.getConnection();
            if (rc != null) {
                String sClear = "rm( " + Rama.R_VECTOR_NAME + " )";
                String sLibrary = "library(rama)";
                String sReform = "dim(" + Rama.R_VECTOR_NAME + ") <- c(" + iGene + "," + iColorKount + ")";
                String sMcmc;
                if (rhs.isFlip()) {
                    sMcmc = this.createMcMc(allOut, iGene, iHybKount, iTwo, iColorKount, B, minIter, nbCol1, true);
                } else {
                    sMcmc = this.createMcMc(allOut, iGene, iHybKount, iTwo, iColorKount, B, minIter, nbCol1, false);
                }
                String sAvgGamma1 = "gamma1<-mat.mean(mcmc." + Rama.R_VECTOR_NAME + "$gamma1)[,1]";
                String sAvgGamma2 = "gamma2<-mat.mean(mcmc." + Rama.R_VECTOR_NAME + "$gamma2)[,1]";
                String sQLo = "mcmc." + Rama.R_VECTOR_NAME + "$q.low";
                String sQUp = "mcmc." + Rama.R_VECTOR_NAME + "$q.up";
                String sShift = "mcmc." + Rama.R_VECTOR_NAME + "$shift";
                final RSwingWorker rThread = new RSwingWorker(rc, sClear, sLibrary, sData, sReform, sMcmc, allOut, sAvgGamma1, sAvgGamma2, sQLo, sQUp, sShift, progress, this);
                rThread.start();
            } else {
                progress.kill();
                this.error("MeV could not establish a connection with Rserve");
                System.out.println("MeV could not establish a connection with Rserve");
            }
        }
    }

    /**
	 * 
	 * @param worker
	 * @param result
	 */
    public void fireThreadFinished(RSwingWorker worker, RamaResult result) {
        if (worker.isOk()) {
            String[] geneNames = new String[this.iGene];
            for (int g = 0; g < iGene; g++) {
                geneNames[g] = data.getGeneName(g);
            }
            for (int i = 0; i < geneNames.length; i++) {
                if (!geneNames[i].equalsIgnoreCase("")) {
                    result.setGenes(geneNames);
                }
            }
            result.setB(this.B);
            result.setMinIter(this.minIter);
            result.saveRamaResult(this.mav.getFrame());
            if (initDialog.connAdded()) {
                TMEV.updateRPath(this.initDialog.getRPathToWrite());
            }
            MultipleArrayViewer newMav = this.spawnNewMav(data, result.getGamma1(), result.getGamma2(), result.getGenes(), result.getShift());
            RamaSummaryViewer sumViewer = new RamaSummaryViewer(result);
            LeafInfo li = new LeafInfo("Rama Summary", sumViewer);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("Rama");
            node.add(new DefaultMutableTreeNode(li));
            newMav.addAnalysisResult(node);
        }
    }

    /**
	 * 
	 * @param allOut
	 * @param iGene
	 * @param iHybKount
	 * @param iTwo
	 * @param iColorKount
	 * @param B
	 * @param minIter
	 * @param nbCol1
	 * @param isDyeSwap
	 * @return
	 */
    private String createMcMc(boolean allOut, int iGene, int iHybKount, int iTwo, int iColorKount, int B, int minIter, int nbCol1, boolean isDyeSwap) {
        StringBuffer sb = new StringBuffer();
        sb.append("mcmc." + Rama.R_VECTOR_NAME + " <- fit.model( " + Rama.R_VECTOR_NAME + "[ 1:");
        sb.append(iGene);
        sb.append(" , c( 1:");
        sb.append(iHybKount);
        sb.append(" )], " + Rama.R_VECTOR_NAME + "[ 1:");
        sb.append(iGene);
        sb.append(", c( ");
        sb.append(iTwo);
        sb.append(":");
        sb.append(iColorKount);
        sb.append(" )], B = ");
        sb.append(B);
        sb.append(", min.iter = ");
        sb.append(minIter);
        sb.append(", batch = 1, shift = NULL, mcmc.obj = NULL, dye.swap = ");
        if (isDyeSwap) {
            sb.append("TRUE");
            sb.append(", nb.col1 = ");
            sb.append(nbCol1);
        } else {
            sb.append("FALSE");
        }
        if (allOut) {
            sb.append(", all.out = TRUE");
        } else {
            sb.append(", all.out = FALSE");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
	 * 
	 * @param data
	 * @return
	 */
    private String[] gatherHybNames(IData data) {
        String[] hybNames = new String[data.getFeaturesCount()];
        for (int h = 0; h < hybNames.length; h++) {
            hybNames[h] = data.getFullSampleName(h);
        }
        return hybNames;
    }

    /**
	 * 
	 * @return
	 */
    private JProgressBar createProgress() {
        JProgressBar bar = new JProgressBar();
        bar.setString("");
        bar.setIndeterminate(true);
        bar.repaint();
        JPanel barPanel = new JPanel();
        barPanel.add(bar);
        barPanel.setSize(200, 100);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame("Talking to R");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation((screenSize.width - frame.getSize().width) / 2, (screenSize.height - frame.getSize().height) / 2);
        frame.setContentPane(barPanel);
        frame.show();
        return bar;
    }

    /**
	 * 
	 * @param gamma1
	 * @param gamma2
	 * @param genes
	 */
    private MultipleArrayViewer spawnNewMav(IData data, double[] gamma1, double[] gamma2, String[] genes, double shift) {
        String[] currentFieldNames = data.getFieldNames();
        Manager.createNewMultipleArrayViewer(20, 20);
        double[] norm1 = this.unLogify(gamma1, 2);
        double[] norm2 = this.unLogify(gamma2, 2);
        ISlideData[] features = new ISlideData[1];
        SlideData slideData = new SlideData(gamma1.length, 1);
        slideData.setFieldNames(currentFieldNames);
        for (int i = 0; i < norm1.length; i++) {
            int[] rows = new int[3];
            int[] cols = new int[3];
            float[] intensities = new float[2];
            rows[0] = (i + 1);
            rows[1] = 1;
            rows[2] = 0;
            cols[0] = 1;
            cols[1] = 1;
            cols[2] = 0;
            intensities[0] = (float) norm1[i];
            intensities[1] = (float) norm2[i];
            String[] extraFields = new String[currentFieldNames.length];
            SlideDataElement loadedSDE = (SlideDataElement) data.getSlideDataElement(0, i);
            for (int e = 0; e < extraFields.length; e++) {
                extraFields[e] = loadedSDE.getFieldAt(e);
            }
            SlideDataElement sde = new SlideDataElement(data.getUniqueId(i), rows, cols, intensities, extraFields);
            slideData.add(sde);
        }
        features[0] = slideData;
        features[0].setSlideFileName("Rama Intensities");
        features[0].setSlideDataName("Rama Intensities");
        MultipleArrayViewer newMav = (MultipleArrayViewer) Manager.getLastComponent();
        newMav.fireDataLoaded(features, IData.DATA_TYPE_TWO_INTENSITY);
        this.mav.getFrame().dispose();
        return newMav;
    }

    /**
	 * Takes a connection string in the form ipaddress:port# and returns just the
	 * port number part
	 * @param connPort
	 * @return
	 */
    private int parseIPort(String connPort) {
        if (connPort == null) {
            return 6311;
        } else {
            int iColon = connPort.indexOf(":");
            if (iColon == -1) {
                return 6311;
            } else {
                int toReturn = Integer.parseInt(connPort.substring(iColon + 1));
                return toReturn;
            }
        }
    }

    /**
	 * Takes a connection string in the form ipaddress:port# and returns just the
	 * ipaddress part
	 * @param connPort
	 * @return
	 */
    private String parseSPort(String connPort) {
        if (connPort == null) {
            return "127.0.0.1";
        } else {
            int iColon = connPort.indexOf(":");
            if (iColon == -1) {
                return "127.0.0.1";
            } else {
                return connPort.substring(0, iColon);
            }
        }
    }

    /**
	 * Creates a Vector of RamaHybs where: treated-Cy3 | control-Cy5
	 * @return
	 */
    private Vector getVRamaHybTreatCy3(Vector ramaHybs) {
        Vector vReturn = new Vector();
        for (int h = 0; h < ramaHybs.size(); h++) {
            RHyb hyb = (RHyb) ramaHybs.elementAt(h);
            if (!hyb.controlCy3()) {
                vReturn.add(hyb);
            }
        }
        return vReturn;
    }

    /**
	 * Creates a Vector of RamaHybs where: control-Cy3 | treated-Cy5
	 * @return
	 */
    private Vector getVRamaHybTreatCy5(Vector ramaHybs) {
        Vector vReturn = new Vector();
        for (int h = 0; h < ramaHybs.size(); h++) {
            RHyb hyb = (RHyb) ramaHybs.elementAt(h);
            if (hyb.controlCy3()) {
                vReturn.add(hyb);
            }
        }
        return vReturn;
    }

    /**
	 * Just un Log transforms a double array by base
	 * @param log
	 * @param base
	 * @return
	 */
    private double[] unLogify(double[] log, int base) {
        double[] toReturn = new double[log.length];
        for (int i = 0; i < log.length; i++) {
            double d = log[i];
            double y = Math.pow(2, d);
            toReturn[i] = y;
        }
        return toReturn;
    }

    /**
     * 
     * @param gamma1
     * @param gamma2
     * @param genes
     */
    private void onSave(double[] gamma1, double[] gamma2, double[] qLo, double[] qUp, boolean allOut, String[] genes) {
        String currentPath = TMEV.getDataPath();
        RamaTextFileFilter textFilter = new RamaTextFileFilter();
        JFileChooser chooser = new JFileChooser(currentPath);
        chooser.addChoosableFileFilter(textFilter);
        if (chooser.showSaveDialog(this.mav.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File saveFile;
            if (chooser.getFileFilter() == textFilter) {
                String path = chooser.getSelectedFile().getPath();
                if (path.toLowerCase().endsWith("txt")) {
                    saveFile = new File(path);
                } else {
                    String subPath;
                    int period = path.lastIndexOf(".txt");
                    if (period != -1) {
                        subPath = path.substring(0, period);
                    } else {
                        subPath = path;
                    }
                    String newPath = subPath + ".txt";
                    saveFile = new File(newPath);
                }
            } else {
                saveFile = chooser.getSelectedFile();
            }
            StringBuffer sb = new StringBuffer();
            sb.append("GeneName");
            sb.append(Rama.TAB);
            sb.append("IntensityA");
            sb.append(Rama.TAB);
            sb.append("IntensityB");
            if (allOut) {
                sb.append(Rama.TAB);
                sb.append("qLow");
                sb.append(Rama.TAB);
                sb.append("qUp");
            }
            sb.append(Rama.END_LINE);
            for (int i = 0; i < genes.length; i++) {
                sb.append(genes[i]);
                sb.append(Rama.TAB);
                sb.append(gamma1[i]);
                sb.append(Rama.TAB);
                sb.append(gamma2[i]);
                if (allOut) {
                    sb.append(Rama.TAB);
                    sb.append(qLo[i]);
                    sb.append(Rama.TAB);
                    sb.append(qUp[i]);
                }
                sb.append(Rama.END_LINE);
            }
            this.writeFile(saveFile, sb.toString());
        } else {
        }
    }

    /**
     * Write the String s to File f
     * 
     * @param f
     * @param s
     */
    private void writeFile(File f, String s) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
	 * Casts a float[][] to a double[][]
	 * @param floatMatrix
	 * @return
	 */
    static double[][] castFloatToDoubleArray(float[][] floatMatrix) {
        double[][] toReturn = new double[floatMatrix.length][floatMatrix[0].length];
        for (int i = 0; i < floatMatrix.length; i++) {
            for (int j = 0; j < floatMatrix[i].length; j++) {
                toReturn[i][j] = (double) floatMatrix[i][j];
            }
        }
        return toReturn;
    }

    /**
	 * 
	 * @param message
	 */
    public void error(String message) {
        JOptionPane.showMessageDialog((JFrame) this.mav.getFrame(), message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }
}
