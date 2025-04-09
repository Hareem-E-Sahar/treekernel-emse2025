package uk.ac.rothamsted.ovtk.Filter.timeseries;

import it.unimi.dsi.fastutil.ints.IntIterator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import net.sf.javaml.clustering.SOM;
import net.sf.javaml.clustering.SOM.GridType;
import net.sf.javaml.clustering.SOM.LearningType;
import net.sf.javaml.clustering.SOM.NeighbourhoodFunction;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SimpleDataset;
import net.sf.javaml.core.SimpleInstance;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import uk.ac.rothamsted.ovtk.GUI.MainFrame;
import uk.ac.rothamsted.ovtk.GUI.util.ResultButtonHeaderRenderer;
import uk.ac.rothamsted.ovtk.GUI.util.ResultDoubleLog2TableCellRenderer;
import uk.ac.rothamsted.ovtk.GUI.util.ResultHeaderListener;
import uk.ac.rothamsted.ovtk.GUI.util.ResultStringTableCellRenderer;
import uk.ac.rothamsted.ovtk.GUI.util.ResultTableModel;
import uk.ac.rothamsted.ovtk.GUI.util.ResultTableUtil;
import uk.ac.rothamsted.ovtk.GUI.util.SpringUtilities;
import uk.ac.rothamsted.ovtk.Graph.VisualONDEXGraph;
import uk.ac.rothamsted.ovtk.Util.printing.BufferedImageDocument;
import backend.core.AbstractConcept;
import backend.core.AbstractONDEXGraph;
import backend.core.AbstractONDEXIterator;
import backend.core.AbstractRelation;
import backend.core.AbstractRelationTypeSet;
import backend.core.AttributeName;
import backend.core.CV;
import backend.core.ConceptAccession;
import backend.core.ConceptClass;
import backend.core.EvidenceType;
import backend.core.ONDEXView;
import backend.core.RelationType;
import backend.core.security.Session;

/**
 * 
 * @author hindlem
 *
 */
public class TimeSeriesMicroarrayAnnotator extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    private static final String TS_MICRO_DATA = "TSMicroArrayData";

    private AttributeName attrName;

    private MainFrame mainFrame;

    private AbstractONDEXGraph graph;

    private VisualONDEXGraph vog;

    private Session s;

    private Container contentPane;

    private HashMap<String, HashMap<Integer, HashMap<String, Double>>> data;

    private HashMap<String, HashMap<Integer, HashMap<String, Double>>> ratiosPreTreatment;

    private HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>> ratiosAccrossTreatment;

    private HashMap<String, Integer> targetSeqs = new HashMap<String, Integer>();

    private StatsCalc ratiosPreTreatmentStats;

    private JTabbedPane tabs;

    private HashMap<String, HashMap<String, Integer>> clusters;

    private HashMap<Integer, HashSet<String>> consensusClusters;

    public TimeSeriesMicroarrayAnnotator(MainFrame mainFrame) {
        super("Time Series Microarray Annotator");
        this.mainFrame = mainFrame;
        this.graph = mainFrame.getCurrentGraph();
        this.vog = mainFrame.getVisualONDEXGraph();
        this.s = mainFrame.getSession();
        attrName = graph.getONDEXGraphData(s).getAttributeName(s, TS_MICRO_DATA);
        if (attrName == null) {
            attrName = graph.getONDEXGraphData(s).createAttributeName(s, "MicroArrayData", java.lang.Double.class);
        }
        setName("Time Series Microarray Filter");
        setResizable(true);
        setIconifiable(true);
        setMaximizable(true);
        setClosable(true);
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        int returnVal = chooser.showOpenDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            getTSData(file.getAbsolutePath());
            idTargetSeqInGraph(true);
            contentPane = this.getContentPane();
            buildGUI();
            pack();
            mainFrame.getDesktop().getJDesktopPane().add(this);
            setVisible(true);
        }
    }

    private void idTargetSeqInGraph(boolean hideOtherTARGETSEQ) {
        ConceptClass cc = graph.getONDEXGraphData(s).getConceptClass(s, "TARGETSEQ");
        ONDEXView<AbstractConcept> concepts = graph.getConceptsOfConceptClass(s, cc);
        while (concepts.hasNext()) {
            AbstractConcept concept = concepts.next();
            AbstractONDEXIterator<ConceptAccession> accs = concept.getConceptAccessions(s);
            while (accs.hasNext()) {
                String acc = accs.next().getAccession(s).toUpperCase();
                if (targetSeqs.containsKey(acc)) {
                    targetSeqs.put(acc, concept.getId(s));
                } else if (hideOtherTARGETSEQ) {
                    IntIterator rels = vog.getIncomingRelations(concept).iterator();
                    while (rels.hasNext()) {
                        vog.setRelationVisible(rels.next(), false);
                    }
                    rels = vog.getOutgoingRelations(concept).iterator();
                    while (rels.hasNext()) {
                        vog.setRelationVisible(rels.next(), false);
                    }
                    vog.setConceptVisible(concept.getId(s), false);
                }
            }
        }
        concepts.close();
    }

    private static final String TREATMENT_RATIO = "TREATMENT_RATIO";

    private static final String PRE_STRESS_RATIO = "PRE_TREATMENT_RATIO";

    private static final String CONCEPT_ID = "ID_int";

    private static final String CLUSTERS_FOR_THIS_TREATMENT = "CLUSTERS_FOR_THIS_TREATMENT";

    private static final String CONSENSUS_CLUSTERS = "CONSENSUS_CLUSTERS";

    private void buildGUI() {
        contentPane.setLayout(new SpringLayout());
        JPanel optionsPanel = new JPanel();
        contentPane.add(optionsPanel);
        JComboBox ratioTypes = new JComboBox();
        ratioTypes.addItem(TREATMENT_RATIO);
        ratioTypes.addItem(PRE_STRESS_RATIO);
        optionsPanel.add(ratioTypes);
        tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.TOP);
        contentPane.add(tabs);
        ratioTypes.setSelectedItem(PRE_STRESS_RATIO);
        addTablesForPreStressRatio();
        ratioTypes.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                tabs.removeAll();
                if (((JComboBox) evt.getSource()).getSelectedItem().equals(TREATMENT_RATIO)) {
                    System.out.println("TREATMENT_RATIO selected");
                } else if (((JComboBox) evt.getSource()).getSelectedItem().equals(PRE_STRESS_RATIO)) {
                    System.out.println("PRE_STRESS_RATIO selected");
                    addTablesForPreStressRatio();
                }
            }
        });
        SpringUtilities.makeCompactGrid(contentPane, 2, 1, 0, 0, 5, 5);
    }

    private HashMap<String, JTable[]> addTablesForPreStressRatio() {
        final HashMap<String, JTable[]> tables = new HashMap<String, JTable[]>();
        tabs.removeAll();
        Iterator<String> treatments = ratiosPreTreatment.keySet().iterator();
        while (treatments.hasNext()) {
            final String treatment = treatments.next();
            JTabbedPane treatmentTab = new JTabbedPane();
            treatmentTab.setName(treatment);
            JTable[] timepointTables = new JTable[ratiosPreTreatment.get(treatment).keySet().size()];
            int timepointat = 0;
            Iterator<Integer> timepoints = ratiosPreTreatment.get(treatment).keySet().iterator();
            while (timepoints.hasNext()) {
                Integer timePoint = timepoints.next();
                String[] headers = new String[] { "PID", ResultTableModel.INT_ID, "TARGET_SEQUENCE", "LOG2(RATIO)" };
                Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
                HashMap<String, Double> tsAtTimepoint = ratiosPreTreatment.get(treatment).get(timePoint);
                Iterator<String> targetSequencesIt = tsAtTimepoint.keySet().iterator();
                int found = 0;
                while (targetSequencesIt.hasNext()) {
                    String targetSequence = targetSequencesIt.next();
                    Double value = tsAtTimepoint.get(targetSequence);
                    Vector<Object> row = new Vector<Object>();
                    Integer concept = targetSeqs.get(targetSequence.toUpperCase());
                    AbstractConcept c = null;
                    String pid = "Not Found";
                    if (concept != null) {
                        c = graph.getConcept(s, concept);
                    }
                    if (c != null) {
                        pid = c.getPID(s);
                        found++;
                    } else {
                        concept = -1;
                    }
                    row.add(0, pid);
                    row.add(1, concept);
                    row.add(2, targetSequence);
                    row.add(3, value);
                    rows.add(row);
                }
                System.out.println("Found " + found + " out of " + tsAtTimepoint.size());
                JScrollPane tableScrollPane = getResultsTable(headers, rows, ratiosPreTreatmentStats.getStandardDeviation());
                JTable table = (JTable) ((JViewport) tableScrollPane.getComponent(0)).getComponent(0);
                timepointTables[timepointat] = table;
                tableScrollPane.setName("LOG2(t" + timePoint + ":t0)");
                tableScrollPane.setToolTipText("LOG2 Ratio (L2R) " + timePoint + ":0");
                treatmentTab.add(tableScrollPane);
                timepointat++;
            }
            treatmentTab.add(getClusterGraphList(treatment));
            JPanel panel = new JPanel();
            panel.setName("Other");
            JButton d3d = new JButton("3D View of this treament");
            panel.add(d3d);
            panel.add(new JLabel());
            d3d.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                }
            });
            treatmentTab.add(panel);
            tables.put(treatment, timepointTables);
            treatmentTab.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    System.out.println("Changed TAB");
                    if (e.getSource() instanceof JTabbedPane) {
                        Object component = ((JTabbedPane) e.getSource()).getSelectedComponent();
                        if (component instanceof JScrollPane) {
                            JScrollPane selectedPane = (JScrollPane) component;
                            JTable table = (JTable) ((JViewport) selectedPane.getComponent(0)).getComponent(0);
                            colorGraph(table);
                        }
                    }
                }
            });
            treatmentTab.setSelectedIndex(0);
            tabs.add(treatment, treatmentTab);
        }
        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                System.out.println("Changed TAB");
                JTabbedPane timePoint = (JTabbedPane) ((JTabbedPane) e.getSource()).getSelectedComponent();
                if (timePoint != null) {
                    timePoint.setSelectedIndex(0);
                    JScrollPane selectedPane = (JScrollPane) (timePoint).getSelectedComponent();
                    JTable table = (JTable) ((JViewport) selectedPane.getComponent(0)).getComponent(0);
                    colorGraph(table);
                }
            }
        });
        tabs.revalidate();
        contentPane.validate();
        tabs.repaint();
        contentPane.repaint();
        return tables;
    }

    /**
	 * Create the panel to open clusters for this treatment
	 * @param treatment
	 * @return
	 */
    private JPanel getClusterGraphList(final String treatment) {
        final JPanel clusterOptions = new JPanel(new SpringLayout());
        clusterOptions.setName("Expression Clusters");
        JPanel clusterGenerator = new JPanel(new SpringLayout());
        TitledBorder titleBoreder = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Generate Clusters");
        titleBoreder.setTitlePosition(TitledBorder.ABOVE_TOP);
        clusterGenerator.setBorder(titleBoreder);
        clusterGenerator.add(new JLabel("X nodes"));
        final JFormattedTextField x = new JFormattedTextField();
        x.setValue(new Integer(11));
        clusterGenerator.add(x);
        clusterGenerator.add(new JLabel("Y nodes"));
        final JFormattedTextField y = new JFormattedTextField();
        y.setValue(new Integer(7));
        clusterGenerator.add(y);
        clusterGenerator.add(new JLabel("Iterations (> 1000)"));
        final JFormattedTextField its = new JFormattedTextField();
        its.setValue(new Integer(2000));
        clusterGenerator.add(its);
        clusterGenerator.add(new JLabel("Learning Rate"));
        final JFormattedTextField learningRate = new JFormattedTextField();
        learningRate.setValue(new Double(0.1));
        clusterGenerator.add(learningRate);
        clusterGenerator.add(new JLabel("Initial Radius"));
        final JFormattedTextField initRad = new JFormattedTextField();
        initRad.setValue(new Integer(1));
        clusterGenerator.add(initRad);
        JButton generate = new JButton("Generate Clusters");
        clusterGenerator.add(generate);
        clusterGenerator.add(new JLabel());
        SpringUtilities.makeCompactGrid(clusterGenerator, clusterGenerator.getComponentCount() / 2, 2, 0, 0, 5, 5);
        clusterOptions.add(clusterGenerator);
        SpringUtilities.makeCompactGrid(clusterOptions, clusterOptions.getComponentCount(), 1, 0, 0, 5, 5);
        generate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                boolean runInits = (clusters == null);
                clusters = findSOMClusters(createTreatmentToTargetSequenceIndex(ratiosPreTreatment), (Integer) x.getValue(), (Integer) y.getValue(), (Integer) its.getValue(), (Double) learningRate.getValue(), (Integer) initRad.getValue());
                HashMap<String, HashMap<Integer, HashSet<String>>> invertedTreatments = new HashMap<String, HashMap<Integer, HashSet<String>>>();
                Iterator<String> it = clusters.keySet().iterator();
                while (it.hasNext()) {
                    String treat = it.next();
                    invertedTreatments.put(treat, invertIndexToClusterToSequence(clusters.get(treat)));
                }
                calculateConsensusClusters(invertedTreatments, false);
                if (runInits) {
                    JPanel showAndSave = new JPanel(new SpringLayout());
                    JButton show = new JButton("Show Clusters for " + treatment);
                    show.setActionCommand(CLUSTERS_FOR_THIS_TREATMENT);
                    show.addActionListener(new ViewChartClustersListener(treatment, true));
                    showAndSave.add(show);
                    JButton save = new JButton("Export Clusters for " + treatment);
                    save.setActionCommand(CLUSTERS_FOR_THIS_TREATMENT);
                    save.addActionListener(new ExportClusters(treatment));
                    showAndSave.add(save);
                    JButton showCon = new JButton("Show Consensus Clusters");
                    showCon.setActionCommand(CONSENSUS_CLUSTERS);
                    showAndSave.add(showCon);
                    showCon.addActionListener(new ViewChartClustersListener(treatment, true));
                    JButton saveCon = new JButton("Export Consensus Clusters");
                    saveCon.setEnabled(false);
                    saveCon.setActionCommand(CONSENSUS_CLUSTERS);
                    saveCon.addActionListener(new ExportClusters(treatment));
                    showAndSave.add(saveCon);
                    SpringUtilities.makeCompactGrid(showAndSave, showAndSave.getComponentCount() / 2, 2, 0, 0, 5, 5);
                    clusterOptions.add(showAndSave);
                    JPanel editOndexGraph = new JPanel(new SpringLayout());
                    JCheckBox redundant = new JCheckBox("Create redundant cluster nodes?");
                    ClusterAddListener clusterAddListener = new ClusterAddListener(treatment, redundant);
                    JButton clusterAdd = new JButton("Add Cluster as Nodes connected to TargetSequences for this treatment only");
                    clusterAdd.setActionCommand(CLUSTERS_FOR_THIS_TREATMENT);
                    editOndexGraph.add(clusterAdd);
                    clusterAdd.addActionListener(clusterAddListener);
                    editOndexGraph.add(redundant);
                    JButton consensuClusterAdd = new JButton("Add Consensus Cluster as Nodes connected to TargetSequences");
                    consensuClusterAdd.setActionCommand(CONSENSUS_CLUSTERS);
                    editOndexGraph.add(consensuClusterAdd);
                    consensuClusterAdd.addActionListener(clusterAddListener);
                    editOndexGraph.add(new JLabel());
                    SpringUtilities.makeCompactGrid(editOndexGraph, editOndexGraph.getComponentCount() / 2, 2, 0, 0, 5, 5);
                    clusterOptions.add(editOndexGraph);
                    SpringUtilities.makeCompactGrid(clusterOptions, clusterOptions.getComponentCount(), 1, 0, 0, 5, 5);
                    clusterOptions.revalidate();
                    clusterOptions.repaint();
                }
            }
        });
        return clusterOptions;
    }

    private class ExportClusters implements ActionListener {

        private String treatment;

        public ExportClusters(String treatment) {
            this.treatment = treatment;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            int returnVal = chooser.showSaveDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(chooser.getSelectedFile().getAbsoluteFile() + File.separator + treatment + "clusters.cls")));
                    Iterator<String> treatmentsIt = clusters.keySet().iterator();
                    while (treatmentsIt.hasNext()) {
                        String treatment = treatmentsIt.next();
                        bw.write(treatment + "\n");
                        HashMap<String, Integer> targets = clusters.get(treatment);
                        Iterator<String> targetIt = targets.keySet().iterator();
                        while (targetIt.hasNext()) {
                            String target = targetIt.next();
                            Integer value = targets.get(target);
                            bw.write(target + "\t" + value + "\n");
                        }
                    }
                    bw.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private HashMap<Integer, HashSet<String>> invertIndexToClusterToSequence(HashMap<String, Integer> targetHash) {
        HashMap<Integer, HashSet<String>> clusterToSeq = new HashMap<Integer, HashSet<String>>();
        Iterator<String> targetIt = targetHash.keySet().iterator();
        while (targetIt.hasNext()) {
            String target = targetIt.next();
            Integer value = targetHash.get(target);
            HashSet<String> targets = clusterToSeq.get(value);
            if (targets == null) {
                targets = new HashSet<String>(3);
                clusterToSeq.put(value, targets);
            }
            targets.add(target);
        }
        return clusterToSeq;
    }

    /**
	 * listens for a signal to create a frame for charts of clusters
	 * @author hindlem
	 *
	 */
    private class ViewChartClustersListener implements ActionListener {

        private String treatment;

        private boolean asImage;

        /**
		 * 
		 * @param treatment
		 * @param asImage indicated to render chart to image first (faster but graph is static)
		 */
        public ViewChartClustersListener(String treatment, boolean asImage) {
            this.treatment = treatment;
            this.asImage = asImage;
        }

        public void actionPerformed(ActionEvent e) {
            JInternalFrame frame = new JInternalFrame();
            JPanel mainPanel = new JPanel(new BorderLayout());
            frame.setContentPane(mainPanel);
            JPanel allGraphs = new JPanel(new SpringLayout());
            HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
            HashMap<Integer, HashSet<String>> clusterToSeq = null;
            if (e.getActionCommand() == CONSENSUS_CLUSTERS) {
                clusterToSeq = consensusClusters;
            } else {
                clusterToSeq = invertIndexToClusterToSequence(clusters.get(treatment));
            }
            ArrayList<String> treatments = new ArrayList<String>();
            if (e.getActionCommand() == CONSENSUS_CLUSTERS) {
                treatments.addAll(clusters.keySet());
            } else {
                treatments.add(treatment);
            }
            Iterator<Integer> clusters = clusterToSeq.keySet().iterator();
            while (clusters.hasNext()) {
                Integer clusterNo = clusters.next();
                Iterator<String> treatmentIt = treatments.iterator();
                while (treatmentIt.hasNext()) {
                    String thisTreatment = treatmentIt.next();
                    JFreeChart chart = ChartDrawer.createChart(getDataSetOnTargetSequences(ratiosPreTreatment.get(thisTreatment), clusterToSeq.get(clusterNo)));
                    chart.setTitle("Cluster " + clusterNo + " for " + thisTreatment);
                    if (asImage) {
                        BufferedImage image = chart.createBufferedImage(500, 400);
                        images.put("Cluster " + clusterNo + " for " + thisTreatment, image);
                        JLabel lblChart = new JLabel();
                        lblChart.setIcon(new ImageIcon(image));
                        allGraphs.add(lblChart);
                    } else {
                        ChartPanel panel = new ChartPanel(chart);
                        panel.setName("Cluster " + clusterNo + " for " + thisTreatment);
                        allGraphs.add(panel);
                    }
                }
            }
            int cols = treatments.size();
            int rows = (int) Math.floor(allGraphs.getComponentCount() / cols);
            SpringUtilities.makeCompactGrid(allGraphs, rows, cols, 0, 0, 0, 0);
            mainPanel.add(new JScrollPane(allGraphs), BorderLayout.CENTER);
            JPanel buttonPanel = new JPanel();
            JButton save = new JButton("Save Graphs to Disk");
            buttonPanel.add(save);
            save.addActionListener(new PlotSaveListener(treatment, images));
            JButton print = new JButton("Print Graphs");
            buttonPanel.add(print);
            print.addActionListener(new GraphPrintListener(treatment, images));
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            frame.setName("Expression Clusters for " + treatment);
            frame.setResizable(true);
            frame.setIconifiable(true);
            frame.setMaximizable(true);
            frame.setClosable(true);
            frame.pack();
            frame.setSize(600, 600);
            mainFrame.getDesktop().getJDesktopPane().add(frame);
            frame.setVisible(true);
        }
    }

    /**
	 * Returns the clusters of series that are grouped together across all treatments
	 * @param treatments
	 * @return
	 */
    private void calculateConsensusClusters(HashMap<String, HashMap<Integer, HashSet<String>>> treatments, boolean overlapBetweenClusters) {
        consensusClusters = new HashMap<Integer, HashSet<String>>();
        if (treatments.size() < 2) {
            throw new RuntimeException("Consensus can not be calcualted on less than 2 treatments");
        }
        HashMap<Integer, HashSet<String>> firstTreatment = treatments.values().iterator().next();
        Iterator<Integer> clustersOnTreat = firstTreatment.keySet().iterator();
        while (clustersOnTreat.hasNext()) {
            Integer cluster = clustersOnTreat.next();
            HashSet<String> coClusteredSet = firstTreatment.get(cluster);
            String[] coClustered = coClusteredSet.toArray(new String[coClusteredSet.size()]);
            HashSet<String> consensusCluster = new HashSet<String>();
            for (int i = 0; i < coClustered.length - 1; i++) {
                String first = coClustered[i];
                String comparison = coClustered[i + 1];
                boolean presentInAll = true;
                Iterator<HashMap<Integer, HashSet<String>>> clustersOnOtherTreats = treatments.values().iterator();
                while (clustersOnOtherTreats.hasNext()) {
                    HashMap<Integer, HashSet<String>> treatment = clustersOnOtherTreats.next();
                    if (treatment.equals(firstTreatment)) {
                        continue;
                    }
                    boolean presentInTreatment = false;
                    Iterator<HashSet<String>> itClusters = treatment.values().iterator();
                    while (itClusters.hasNext()) {
                        HashSet<String> clusterGroup = itClusters.next();
                        boolean containsFirst = clusterGroup.contains(first);
                        boolean containsSecond = clusterGroup.contains(comparison);
                        if (containsFirst || containsSecond) {
                            if (containsFirst && containsSecond) {
                                presentInTreatment = true;
                                break;
                            }
                            if (!overlapBetweenClusters) {
                                presentInTreatment = false;
                                break;
                            }
                        }
                    }
                    if (!presentInTreatment) {
                        presentInAll = false;
                        break;
                    }
                }
                if (presentInAll) {
                    consensusCluster.add(first);
                    consensusCluster.add(comparison);
                }
            }
            if (consensusCluster.size() > 0) {
                consensusClusters.put(consensusClusters.size() + 1, consensusCluster);
            }
        }
        Iterator<Integer> clusterNumIt = consensusClusters.keySet().iterator();
        while (clusterNumIt.hasNext()) {
            Integer clusterNum = clusterNumIt.next();
            HashSet<String> cluster = consensusClusters.get(clusterNum);
            Iterator<String> treatmentIt = treatments.keySet().iterator();
            while (treatmentIt.hasNext()) {
                String treatment = treatmentIt.next();
                boolean foundInTreatment = false;
                HashMap<Integer, HashSet<String>> clustersForTreat = treatments.get(treatment);
                Iterator<Integer> clusterTarNumIt = clustersForTreat.keySet().iterator();
                while (clusterTarNumIt.hasNext() && !foundInTreatment) {
                    Integer clusterTarNum = clusterTarNumIt.next();
                    HashSet<String> clusterTar = clustersForTreat.get(clusterTarNum);
                    Iterator<String> itemsInConCluster = cluster.iterator();
                    while (itemsInConCluster.hasNext()) {
                        if (clusterTar.contains(itemsInConCluster.next())) {
                            HashSet<String> targetClusterNumCluster = clustersForTreat.get(clusterNum);
                            if (targetClusterNumCluster != null) {
                                clustersForTreat.put(clusterTarNum, targetClusterNumCluster);
                            }
                            clustersForTreat.put(clusterNum, clusterTar);
                            foundInTreatment = true;
                        }
                    }
                }
            }
        }
    }

    /**
	 * Listens for a signal to add cluster information to an ondex graph
	 * @author hindlem
	 *
	 */
    private class ClusterAddListener implements ActionListener {

        private String treatment;

        private JCheckBox redundant;

        boolean redundantClusters = false;

        public ClusterAddListener(String treatment, JCheckBox redundant) {
            this.treatment = treatment;
            this.redundant = redundant;
        }

        public void actionPerformed(ActionEvent e) {
            redundantClusters = redundant.isSelected();
            HashMap<Integer, HashSet<String>> clusterToSeq = null;
            if (e.getActionCommand() == CONSENSUS_CLUSTERS) {
                clusterToSeq = consensusClusters;
            } else {
                clusterToSeq = invertIndexToClusterToSequence(clusters.get(treatment));
            }
            ConceptClass cc = graph.getONDEXGraphData(s).getConceptClass(s, MetaData.CLUSTER);
            if (cc == null) {
                cc = graph.getONDEXGraphData(s).createConceptClass(s, MetaData.CLUSTER);
            }
            CV cv = graph.getONDEXGraphData(s).getCV(s, MetaData.OVTK_ANALYSIS);
            if (cv == null) {
                cv = graph.getONDEXGraphData(s).createCV(s, MetaData.OVTK_ANALYSIS, "Product of OVTK analysis");
            }
            EvidenceType et = graph.getONDEXGraphData(s).getEvidenceType(s, MetaData.SOM_CLUSTERING);
            if (et == null) {
                et = graph.getONDEXGraphData(s).createEvidenceType(s, MetaData.SOM_CLUSTERING);
            }
            AttributeName treatmentAtt = graph.getONDEXGraphData(s).getAttributeName(s, MetaData.TREATMENT);
            if (treatmentAtt == null) {
                treatmentAtt = graph.getONDEXGraphData(s).createAttributeName(s, MetaData.TREATMENT, String.class);
            }
            AttributeName redundant = graph.getONDEXGraphData(s).getAttributeName(s, MetaData.REDUNDANT_CLUSTER);
            if (redundant == null) {
                redundant = graph.getONDEXGraphData(s).createAttributeName(s, MetaData.REDUNDANT_CLUSTER, "Cluster is redundant?", Boolean.class);
            }
            AbstractRelationTypeSet hse = graph.getONDEXGraphData(s).getRelationTypeSet(s, MetaData.HAS_SIMILAR_EXPRESSION);
            if (hse == null) {
                RelationType rt = graph.getONDEXGraphData(s).getRelationType(s, MetaData.HAS_SIMILAR_EXPRESSION);
                if (rt == null) {
                    rt = graph.getONDEXGraphData(s).createRelationType(s, MetaData.HAS_SIMILAR_EXPRESSION);
                }
                hse = graph.getONDEXGraphData(s).createRelationTypeSet(s, MetaData.HAS_SIMILAR_EXPRESSION, rt);
            }
            Iterator<Integer> clusters = clusterToSeq.keySet().iterator();
            while (clusters.hasNext()) {
                Integer clusterNo = clusters.next();
                System.out.println("Annotating TARGETSEQ for " + clusterNo);
                Iterator<String> seqIt = clusterToSeq.get(clusterNo).iterator();
                AbstractConcept clusterConcept = null;
                if (!redundantClusters) {
                    clusterConcept = vog.createConcept(treatment + "_" + MetaData.CLUSTER + "_" + clusterNo, "Cluster for " + treatment, "", cv, cc, et);
                    clusterConcept.createConceptGDS(s, treatmentAtt, treatment, false);
                    clusterConcept.createConceptGDS(s, redundant, Boolean.FALSE, false);
                }
                while (seqIt.hasNext()) {
                    String seq = seqIt.next();
                    Integer cid = targetSeqs.get(seq.toUpperCase());
                    if (cid != null) {
                        System.out.println("Adding annotation...4.." + cid);
                        AbstractConcept tarSeq = vog.getConcept(cid);
                        if (redundantClusters) {
                            clusterConcept = vog.createConcept(treatment + "_" + MetaData.CLUSTER + "_" + clusterNo, "Cluster for " + treatment, "", cv, cc, et);
                            clusterConcept.createConceptGDS(s, treatmentAtt, treatment, false);
                            clusterConcept.createConceptGDS(s, redundant, Boolean.TRUE, false);
                        }
                        AbstractRelation relation = vog.createRelation(tarSeq, clusterConcept, hse, et);
                        relation.createRelationGDS(s, treatmentAtt, treatment, false);
                    }
                }
            }
            mainFrame.updateGraph();
        }
    }

    ;

    /**
		 * listens for print signal
		 * @author hindlem
		 *
		 */
    private class GraphPrintListener implements ActionListener {

        private HashMap<String, BufferedImage> images;

        public GraphPrintListener(String treatment, HashMap<String, BufferedImage> images) {
            this.images = images;
        }

        public void actionPerformed(ActionEvent e) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            Pageable document = new BufferedImageDocument(images.values());
            printJob.setPageable(document);
            if (printJob.printDialog()) {
                try {
                    printJob.print();
                } catch (PrinterException pe) {
                    System.out.println("Error printing: " + pe);
                }
            }
        }
    }

    /**
		 * Listens for save signal
		 * @author hindlem
		 *
		 */
    private class PlotSaveListener implements ActionListener {

        private HashMap<String, BufferedImage> images;

        private String treatment;

        public PlotSaveListener(String treatment, HashMap<String, BufferedImage> images) {
            this.images = images;
            this.treatment = treatment;
        }

        public void actionPerformed(ActionEvent e) {
            JPanel options = new JPanel(new SpringLayout());
            JCheckBox asZipFile = new JCheckBox("Save all images to ZIP file");
            options.add(asZipFile);
            options.add(new JLabel());
            ButtonGroup group = new ButtonGroup();
            JRadioButton jpeg = new JRadioButton("Save as JPEG");
            group.add(jpeg);
            jpeg.setActionCommand("JPEG");
            JRadioButton png = new JRadioButton("Save as PNG");
            png.setActionCommand("PNG");
            group.add(png);
            options.add(jpeg);
            options.add(png);
            jpeg.setSelected(true);
            SpringUtilities.makeCompactGrid(options, options.getComponentCount() / 2, 2, 0, 0, 2, 2);
            JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setAccessory(options);
            int returnVal = chooser.showSaveDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    OutputStream out = null;
                    if (asZipFile.isSelected()) {
                        out = new ZipOutputStream(new FileOutputStream(chooser.getSelectedFile().getAbsolutePath() + File.separator + "Clusters_" + treatment + ".zip"));
                    }
                    Iterator<String> it = images.keySet().iterator();
                    while (it.hasNext()) {
                        String name = it.next();
                        File f = new File(chooser.getSelectedFile().getAbsolutePath() + File.separator + name + ".jpg");
                        if (asZipFile.isSelected()) {
                            ((ZipOutputStream) out).putNextEntry(new ZipEntry(f.getName()));
                        } else {
                            out = new BufferedOutputStream(new FileOutputStream(f));
                        }
                        if (group.getSelection().getActionCommand().equals("JPEG")) {
                            ChartUtilities.writeBufferedImageAsJPEG(out, images.get(name));
                        } else if (group.getSelection().getActionCommand().equals("PNG")) {
                            ChartUtilities.writeBufferedImageAsPNG(out, images.get(name));
                        }
                        if (asZipFile.isSelected()) {
                            ((ZipOutputStream) out).closeEntry();
                        } else {
                            out.close();
                        }
                    }
                    if (asZipFile.isSelected()) {
                        out.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
		 * Extracts a DefaultCategoryDataset from the raw data when a set of targetSequences is provided (e.g. all or a subset for a cluster)
		 * @param rawData
		 * @param treatment (e.g. cultivar)
		 * @param targetSequence affyTargetSequenceid
		 * @return
		 */
    private DefaultCategoryDataset getDataSetOnTargetSequences(HashMap<Integer, HashMap<String, Double>> ratios, Collection<String> targetSequences) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        HashMap<String, HashMap<Integer, Double>> invertedHash = new HashMap<String, HashMap<Integer, Double>>();
        Iterator<Integer> timeIt = ratios.keySet().iterator();
        while (timeIt.hasNext()) {
            Integer time = timeIt.next();
            HashMap<String, Double> seqHash = ratios.get(time);
            Iterator<String> seqIt = seqHash.keySet().iterator();
            while (seqIt.hasNext()) {
                String seq = seqIt.next();
                Double value = seqHash.get(seq);
                HashMap<Integer, Double> timeHash = invertedHash.get(seq);
                if (timeHash == null) {
                    timeHash = new HashMap<Integer, Double>();
                    invertedHash.put(seq, timeHash);
                }
                timeHash.put(time, value);
            }
        }
        Iterator<String> seqIt = targetSequences.iterator();
        while (seqIt.hasNext()) {
            String seq = seqIt.next();
            HashMap<Integer, Double> timePointsHash = invertedHash.get(seq);
            Iterator<Integer> timePointsIt = timePointsHash.keySet().iterator();
            while (timePointsIt.hasNext()) {
                Integer time = timePointsIt.next();
                Double value = timePointsHash.get(time);
                dataset.addValue(value, seq, time);
            }
        }
        return dataset;
    }

    /**
		 * converts a TreatmentToTimePointIndex to a TreatmentToTargetSequenceIndex
		 * @param values
		 * @return
		 */
    private HashMap<String, HashMap<String, Double[]>> createTreatmentToTargetSequenceIndex(HashMap<String, HashMap<Integer, HashMap<String, Double>>> values) {
        HashMap<String, HashMap<String, Double[]>> targetSeqIndex = new HashMap<String, HashMap<String, Double[]>>();
        Iterator<String> treatmentIt = values.keySet().iterator();
        while (treatmentIt.hasNext()) {
            String treatment = treatmentIt.next();
            HashMap<Integer, HashMap<String, Double>> timepointHash = values.get(treatment);
            int noTimePoints = timepointHash.keySet().size();
            Iterator<Integer> timepointsIt = timepointHash.keySet().iterator();
            while (timepointsIt.hasNext()) {
                Integer time = timepointsIt.next();
                HashMap<String, Double> tarSeqHash = timepointHash.get(time);
                Iterator<String> targSeqIt = tarSeqHash.keySet().iterator();
                while (targSeqIt.hasNext()) {
                    String targSeq = targSeqIt.next();
                    Double value = tarSeqHash.get(targSeq);
                    HashMap<String, Double[]> treatHash = targetSeqIndex.get(treatment);
                    if (treatHash == null) {
                        treatHash = new HashMap<String, Double[]>();
                        targetSeqIndex.put(treatment, treatHash);
                    }
                    Double[] timePoints = treatHash.get(targSeq);
                    if (timePoints == null) {
                        timePoints = new Double[noTimePoints];
                        treatHash.put(targSeq, timePoints);
                    }
                    timePoints[time - 2] = value;
                }
            }
        }
        return targetSeqIndex;
    }

    /**
		 * 
		 * @param significance
		 */
    private HashMap<String, HashMap<String, Integer>> findSOMClusters(HashMap<String, HashMap<String, Double[]>> values, int xdim, int ydim, int iterations, double learningRate, int initialRadius) {
        HashMap<String, HashMap<String, Integer>> clusterResults = new HashMap<String, HashMap<String, Integer>>();
        Iterator<String> treamentIt = values.keySet().iterator();
        while (treamentIt.hasNext()) {
            String treatment = treamentIt.next();
            HashMap<SimpleInstance, String> instanceToTargetSeq = new HashMap<SimpleInstance, String>();
            Dataset set = new SimpleDataset();
            HashMap<String, Double[]> targetSeqsHash = values.get(treatment);
            Iterator<String> targetSeqIt = targetSeqsHash.keySet().iterator();
            while (targetSeqIt.hasNext()) {
                String targetSeq = targetSeqIt.next();
                Double[] ratioValues = targetSeqsHash.get(targetSeq);
                double[] unboxed = new double[ratioValues.length];
                for (int i = 0; i < ratioValues.length; i++) {
                    unboxed[i] = ratioValues[i];
                }
                SimpleInstance instance = new SimpleInstance(unboxed);
                set.addInstance(instance);
                instanceToTargetSeq.put(instance, targetSeq);
            }
            System.out.println("Execute clustering for " + treatment + " on " + set.size() + " series");
            SOM clusterer = new SOM(xdim, ydim, GridType.HEXAGONAL, iterations, learningRate, initialRadius, LearningType.LINEAR, NeighbourhoodFunction.GAUSSIAN);
            Dataset[] results = clusterer.executeClustering(set);
            if (results.length == 0) {
                throw new NullPointerException("No results");
            }
            for (int i = 0; i < results.length; i++) {
                Iterator<Instance> instanceIt = results[i].iterator();
                while (instanceIt.hasNext()) {
                    Instance instance = instanceIt.next();
                    String targetSeq = instanceToTargetSeq.get(instance);
                    HashMap<String, Integer> treatHash = clusterResults.get(treatment);
                    if (treatHash == null) {
                        treatHash = new HashMap<String, Integer>();
                        clusterResults.put(treatment, treatHash);
                    }
                    treatHash.put(targetSeq, Integer.valueOf(i));
                }
            }
        }
        return clusterResults;
    }

    private void colorGraph(final JTable table) {
        for (int j = 0; j < table.getModel().getRowCount(); j++) {
            Integer cid = ((Integer) table.getValueAt(j, table.getTableHeader().getColumnModel().getColumnIndex("ID_int")));
            Double value = ((Double) table.getValueAt(j, table.getTableHeader().getColumnModel().getColumnIndex("LOG2(RATIO)")));
            Color color = ResultDoubleLog2TableCellRenderer.calculateColor(value, ratiosPreTreatmentStats.getStandardDeviation());
            vog.setConceptColor(cid, color);
            int green = color.getGreen();
            int red = color.getRed();
            if (green < 255) {
                if ((green >= 0) && (green <= 50)) {
                    vog.setSize(cid, 4);
                } else if ((green > 50) && (green <= 100)) {
                    vog.setSize(cid, 3);
                } else if ((green > 100) && (green <= 200)) {
                    vog.setSize(cid, 2);
                } else {
                    vog.setSize(cid, 1);
                }
            } else if (red < 255) {
                if ((red >= 0) && (red <= 50)) {
                    vog.setSize(cid, 4);
                } else if ((red > 50) && (red <= 100)) {
                    vog.setSize(cid, 3);
                } else if ((red > 100) && (red <= 200)) {
                    vog.setSize(cid, 2);
                } else {
                    vog.setSize(cid, 1);
                }
            }
        }
        for (int j = 0; j < table.getModel().getRowCount(); j++) {
            Integer cid = ((Integer) table.getValueAt(j, table.getTableHeader().getColumnModel().getColumnIndex("ID_int")));
            mainFrame.getGraphLibraryAdapter().updateConcept(cid);
        }
        mainFrame.updateGraph();
    }

    public void getTSData(String filename) {
        data = new HashMap<String, HashMap<Integer, HashMap<String, Double>>>();
        HashMap<Integer, String> colToTreatment = new HashMap<Integer, String>();
        HashMap<Integer, Integer> colToTimeSeries = new HashMap<Integer, Integer>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while (br.ready()) {
                String line = br.readLine().trim();
                if (line.length() == 0) {
                    continue;
                }
                String[] values = line.split("\t");
                if (data.size() == 0) {
                    for (int i = 0; i < values.length; i++) {
                        String value = removeQuotesInValue(values[i]);
                        if (value.length() > 0) {
                            colToTreatment.put(i + 1, value);
                            if (!data.containsKey(value)) {
                                data.put(value, new HashMap<Integer, HashMap<String, Double>>());
                            }
                        }
                    }
                } else if (colToTimeSeries.size() == 0) {
                    for (int i = 0; i < values.length; i++) {
                        Integer value = null;
                        try {
                            value = Integer.parseInt(removeQuotesInValue(values[i]));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (value != null) {
                            colToTimeSeries.put(i + 1, value);
                            HashMap<Integer, HashMap<String, Double>> treatment = data.get(colToTreatment.get(i + 1));
                            if (!treatment.containsKey(value)) {
                                treatment.put(value, new HashMap<String, Double>());
                            } else {
                                throw new RuntimeException("Time series " + value + " appears more than once for treatment " + colToTreatment.get(i));
                            }
                        }
                    }
                } else {
                    String currentTargetSequ = null;
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i].trim();
                        if (value.length() != 0) {
                            if (currentTargetSequ == null) {
                                currentTargetSequ = removeQuotesInValue(value);
                                if (!targetSeqs.keySet().contains(targetSeqs)) targetSeqs.put(currentTargetSequ.toUpperCase(), null);
                                continue;
                            } else if (colToTreatment.get(i) != null && colToTimeSeries.get(i) != null) {
                                HashMap<String, Double> timepoint = data.get(colToTreatment.get(i)).get(colToTimeSeries.get(i));
                                if (!timepoint.containsKey(currentTargetSequ)) {
                                    timepoint.put(currentTargetSequ, Double.parseDouble(value));
                                } else {
                                    throw new RuntimeException("Treatment " + colToTreatment.get(i) + " for time point " + colToTimeSeries.get(i) + " has multiple entries for " + currentTargetSequ);
                                }
                            } else {
                                System.out.println("unknown for " + i + " " + colToTreatment.get(i) + "   " + colToTimeSeries.get(i) + "   " + value);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Sucess in importing data on " + data.keySet().size() + " treatments ");
        ratiosPreTreatment = new HashMap<String, HashMap<Integer, HashMap<String, Double>>>();
        ratiosPreTreatmentStats = new StatsCalc();
        ratiosAccrossTreatment = new HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>>();
        new StatsCalc();
        Iterator<String> treatmentsIt = data.keySet().iterator();
        while (treatmentsIt.hasNext()) {
            String treatment = treatmentsIt.next();
            Iterator<Integer> timePointsIt = data.get(treatment).keySet().iterator();
            while (timePointsIt.hasNext()) {
                Integer timePoint = timePointsIt.next();
                Iterator<String> targetIt = data.get(treatment).get(timePoint).keySet().iterator();
                while (targetIt.hasNext()) {
                    String target = targetIt.next();
                    Double currentValue = data.get(treatment).get(timePoint).get(target);
                    if (timePoint > 0) {
                        Double preTreatmentTimePoint = data.get(treatment).get(0).get(target);
                        if (!ratiosPreTreatment.containsKey(treatment)) {
                            ratiosPreTreatment.put(treatment, new HashMap<Integer, HashMap<String, Double>>());
                        }
                        HashMap<Integer, HashMap<String, Double>> treatmentMap = ratiosPreTreatment.get(treatment);
                        if (!treatmentMap.containsKey(timePoint)) {
                            treatmentMap.put(timePoint, new HashMap<String, Double>());
                        }
                        HashMap<String, Double> timeSeries = treatmentMap.get(timePoint);
                        Double ratio = currentValue / preTreatmentTimePoint;
                        Double normalized = StrictMath.log(ratio) / StrictMath.log(2.0);
                        ratiosPreTreatmentStats.enter(normalized);
                        timeSeries.put(target, normalized);
                    }
                    if (!ratiosAccrossTreatment.containsKey(treatment)) {
                        ratiosAccrossTreatment.put(treatment, new HashMap<Integer, HashMap<String, HashMap<String, Double>>>());
                    }
                    HashMap<Integer, HashMap<String, HashMap<String, Double>>> treatmentMap = ratiosAccrossTreatment.get(treatment);
                    if (!treatmentMap.containsKey(timePoint)) {
                        treatmentMap.put(timePoint, new HashMap<String, HashMap<String, Double>>());
                    }
                    HashMap<String, HashMap<String, Double>> targetsTocomparisonRatios = treatmentMap.get(timePoint);
                    HashMap<String, Double> comparisons = new HashMap<String, Double>();
                    targetsTocomparisonRatios.put(target, comparisons);
                    Iterator<String> treatmentsIt2 = data.keySet().iterator();
                    while (treatmentsIt2.hasNext()) {
                        String otherTreatment = treatmentsIt2.next();
                        if (otherTreatment != treatment) {
                            Double comparisonValue = data.get(otherTreatment).get(timePoint).get(target);
                            Double ratio = currentValue / comparisonValue;
                            Double normalized = StrictMath.log(ratio) / StrictMath.log(2.0);
                            comparisons.put(otherTreatment, normalized);
                        }
                    }
                }
            }
        }
    }

    private String removeQuotesInValue(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("\'") && value.endsWith("\'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private JScrollPane getResultsTable(String[] columnNames, Vector<Vector<Object>> data, double stdev) {
        Object[][] tableData = new Object[data.size()][columnNames.length];
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < columnNames.length; j++) {
                tableData[i][j] = ((Vector<?>) data.get(i)).get(j);
            }
        }
        ResultTableModel myTableModel = new ResultTableModel(tableData, columnNames);
        final JTable myTable = new JTable(myTableModel);
        myTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = myTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    int index = ((Integer) myTable.getValueAt(selectedRow, myTable.getTableHeader().getColumnModel().getColumnIndex(CONCEPT_ID))).intValue();
                    if (index > 0) {
                        if (vog.isConceptVisible(index)) {
                            mainFrame.graphElementClicked(index, true, null);
                            mainFrame.getGraphLibraryAdapter().zoomTo(index);
                        }
                    }
                }
            }
        });
        ResultStringTableCellRenderer myStringTableCellRenderer = new ResultStringTableCellRenderer(vog);
        myTable.setDefaultRenderer(String.class, myStringTableCellRenderer);
        ResultDoubleLog2TableCellRenderer myDoubleTableCellRenderer = new ResultDoubleLog2TableCellRenderer(vog, myTableModel, stdev);
        myTable.setDefaultRenderer(Double.class, myDoubleTableCellRenderer);
        ResultButtonHeaderRenderer renderer = new ResultButtonHeaderRenderer();
        TableColumnModel model = myTable.getColumnModel();
        int n = myTableModel.getColumnCount();
        for (int i = 0; i < n; i++) {
            model.getColumn(i).setHeaderRenderer(renderer);
        }
        JTableHeader header = myTable.getTableHeader();
        header.addMouseListener(new ResultHeaderListener(header, renderer, myTable));
        myTable.setTableHeader(header);
        ResultTableUtil.calcColumnWidths(myTable);
        JScrollPane scrollPane = new JScrollPane(myTable);
        return scrollPane;
    }
}
