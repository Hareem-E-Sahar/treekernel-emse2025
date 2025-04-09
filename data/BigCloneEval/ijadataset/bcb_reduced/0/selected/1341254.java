package org.neuroph.easyneurons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Vector;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.core.transfer.Trapezoid;
import org.neuroph.easyneurons.dialog.AdalineWizard;
import org.neuroph.easyneurons.dialog.BamWizard;
import org.neuroph.easyneurons.dialog.CompetitiveNetworkWizard;
import org.neuroph.easyneurons.dialog.HopfieldWizard;
import org.neuroph.easyneurons.dialog.InstarWizard;
import org.neuroph.easyneurons.dialog.KohonenWizard;
import org.neuroph.easyneurons.dialog.MLPerceptronWizard;
import org.neuroph.easyneurons.dialog.MaxnetWizard;
import org.neuroph.easyneurons.dialog.OutstarWizard;
import org.neuroph.easyneurons.dialog.PerceptronWizard;
import org.neuroph.easyneurons.dialog.RbfWizard;
import org.neuroph.easyneurons.dialog.RenameNodeDialog;
import org.neuroph.easyneurons.dialog.SupervisedHebbianWizard;
import org.neuroph.easyneurons.dialog.TrainingSetWizard;
import org.neuroph.easyneurons.dialog.UnsupervisedHebbianWizard;
import org.neuroph.easyneurons.errorgraph.GraphFrame;
import org.neuroph.easyneurons.file.FileFilterAdapter;
import org.neuroph.easyneurons.file.FileIO;
import org.neuroph.easyneurons.file.FileObserver;
import org.neuroph.easyneurons.file.FileUtils;
import org.neuroph.easyneurons.file.WindowObserver;
import org.neuroph.easyneurons.samples.BasicNeuronSample;
import org.neuroph.easyneurons.samples.KohonenSample;
import org.neuroph.easyneurons.samples.NFRSample;
import org.neuroph.nnet.Adaline;
import org.neuroph.nnet.BAM;
import org.neuroph.nnet.CompetitiveNetwork;
import org.neuroph.nnet.Hopfield;
import org.neuroph.nnet.Instar;
import org.neuroph.nnet.Kohonen;
import org.neuroph.nnet.MaxNet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.NeuroFuzzyReasoner;
import org.neuroph.nnet.Outstar;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.RbfNetwork;
import org.neuroph.nnet.SupervisedHebbianNetwork;
import org.neuroph.nnet.UnsupervisedHebbianNetwork;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.plugins.LabelsPlugin;

/**
 * The application's main frame.
 */
public class EasyNeuronsApplicationView extends FrameView implements Serializable {

    private static final long serialVersionUID = 1L;

    private EasyNeuronsProject easyNeuronsProject;

    private NeuralNetworkViewFrame netActiveWindow = null;

    private TrainingSet traActiveWindow = null;

    private FileObserver fileObserver = new FileObserver();

    private JFileChooser fileChooser = new JFileChooser();

    private JFrame mainFrame = EasyNeuronsApplication.getApplication().getMainFrame();

    public EasyNeuronsApplicationView(SingleFrameApplication app) {
        super(app);
        initComponents();
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        JFrame mainFrame2 = this.getFrame();
        mainFrame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.easyNeuronsProject = new EasyNeuronsProject();
        MouseListener ml = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int selRow = projectTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 2) {
                        treeNodeDoubleClick(selRow, selPath);
                    }
                }
            }
        };
        projectTree.addMouseListener(ml);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        projectTree.setCellRenderer(renderer);
        this.updateProjectTree();
        desktopPanel.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        newTrainingSetButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        desktopPanel = new javax.swing.JDesktopPane();
        projectTreeScrollPane = new javax.swing.JScrollPane();
        projectTree = new javax.swing.JTree();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewBlockMenuItem = new javax.swing.JMenuItem();
        viewGraphMenuItem = new javax.swing.JMenuItem();
        viewRefreshMenuItem = new javax.swing.JMenuItem();
        networksMenu = new javax.swing.JMenu();
        adalineMenuItem = new javax.swing.JMenuItem();
        perceptronMenuItem = new javax.swing.JMenuItem();
        mlpMenuItem = new javax.swing.JMenuItem();
        hopfieldMenuItem = new javax.swing.JMenuItem();
        bamMenuItem = new javax.swing.JMenuItem();
        kohonenMenuItem = new javax.swing.JMenuItem();
        supervisedHebbianMenuItem = new javax.swing.JMenuItem();
        unsupervisedHebbianMenuItem = new javax.swing.JMenuItem();
        maxnetMenuItem = new javax.swing.JMenuItem();
        competitiveMenuItem = new javax.swing.JMenuItem();
        rbfMenuItem = new javax.swing.JMenuItem();
        instarMenuItem = new javax.swing.JMenuItem();
        outstarMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        trainingMenu = new javax.swing.JMenu();
        trainingSetWizardMenuItem = new javax.swing.JMenuItem();
        trainMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        imageRecognitionMenuItem = new javax.swing.JMenuItem();
        samplesMenu = new javax.swing.JMenu();
        sampleBasicNeuron = new javax.swing.JMenuItem();
        sampleKohonenMenuItem = new javax.swing.JMenuItem();
        sampleNFRMenuItem = new javax.swing.JMenuItem();
        sampleRecommenderMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        treePopupMenu = new javax.swing.JPopupMenu();
        renameMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();
        mainPanel.setName("mainPanel");
        mainPanel.setPreferredSize(new java.awt.Dimension(900, 600));
        mainPanel.setLayout(new java.awt.BorderLayout(2, 2));
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1");
        jToolBar1.setPreferredSize(new java.awt.Dimension(154, 33));
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.neuroph.easyneurons.EasyNeuronsApplication.class).getContext().getActionMap(EasyNeuronsApplicationView.class, this);
        openButton.setAction(actionMap.get("showOpenDialog"));
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.neuroph.easyneurons.EasyNeuronsApplication.class).getContext().getResourceMap(EasyNeuronsApplicationView.class);
        openButton.setIcon(resourceMap.getIcon("openButton.icon"));
        openButton.setText(resourceMap.getString("openButton.text"));
        openButton.setToolTipText(resourceMap.getString("openButton.toolTipText"));
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setName("openButton");
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(openButton);
        saveButton.setAction(actionMap.get("showSaveDialog"));
        saveButton.setIcon(resourceMap.getIcon("saveButton.icon"));
        saveButton.setText(resourceMap.getString("saveButton.text"));
        saveButton.setToolTipText(resourceMap.getString("saveButton.toolTipText"));
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setName("saveButton");
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(saveButton);
        newTrainingSetButton.setAction(actionMap.get("showTrainingSetWizard"));
        newTrainingSetButton.setIcon(resourceMap.getIcon("newTrainingSetButton.icon"));
        newTrainingSetButton.setText(resourceMap.getString("newTrainingSetButton.text"));
        newTrainingSetButton.setToolTipText(resourceMap.getString("newTrainingSetButton.toolTipText"));
        newTrainingSetButton.setFocusable(false);
        newTrainingSetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newTrainingSetButton.setName("newTrainingSetButton");
        newTrainingSetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(newTrainingSetButton);
        mainPanel.add(jToolBar1, java.awt.BorderLayout.PAGE_START);
        jSplitPane1.setMaximumSize(null);
        jSplitPane1.setName("jSplitPane1");
        desktopPanel.setName("desktopPanel");
        jSplitPane1.setRightComponent(desktopPanel);
        projectTreeScrollPane.setAlignmentX(1.0F);
        projectTreeScrollPane.setAlignmentY(1.0F);
        projectTreeScrollPane.setName("projectTreeScrollPane");
        projectTree.setAutoscrolls(true);
        projectTree.setComponentPopupMenu(treePopupMenu);
        projectTree.setDragEnabled(true);
        projectTree.setMaximumSize(new java.awt.Dimension(100, 64));
        projectTree.setName("projectTree");
        projectTree.setPreferredSize(new java.awt.Dimension(150, 100));
        projectTree.setRowHeight(22);
        projectTreeScrollPane.setViewportView(projectTree);
        jSplitPane1.setLeftComponent(projectTreeScrollPane);
        mainPanel.add(jSplitPane1, java.awt.BorderLayout.CENTER);
        menuBar.setName("menuBar");
        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");
        openMenuItem.setAction(actionMap.get("showOpenDialog"));
        openMenuItem.setToolTipText(resourceMap.getString("openMenuItem.toolTipText"));
        openMenuItem.setName("openMenuItem");
        fileMenu.add(openMenuItem);
        saveMenuItem.setAction(actionMap.get("showSaveDialog"));
        saveMenuItem.setText(resourceMap.getString("saveMenuItem.text"));
        saveMenuItem.setToolTipText(resourceMap.getString("saveMenuItem.toolTipText"));
        saveMenuItem.setName("saveMenuItem");
        fileMenu.add(saveMenuItem);
        saveAsMenuItem.setAction(actionMap.get("showSaveAsDialog"));
        saveAsMenuItem.setText(resourceMap.getString("saveAsMenuItem.text"));
        saveAsMenuItem.setToolTipText(resourceMap.getString("saveAsMenuItem.toolTipText"));
        saveAsMenuItem.setName("saveAsMenuItem");
        fileMenu.add(saveAsMenuItem);
        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem");
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        editMenu.setText(resourceMap.getString("editMenu.text"));
        editMenu.setName("editMenu");
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text"));
        jMenuItem1.setEnabled(false);
        jMenuItem1.setName("jMenuItem1");
        editMenu.add(jMenuItem1);
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text"));
        jMenuItem2.setEnabled(false);
        jMenuItem2.setName("jMenuItem2");
        editMenu.add(jMenuItem2);
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text"));
        jMenuItem3.setEnabled(false);
        jMenuItem3.setName("jMenuItem3");
        editMenu.add(jMenuItem3);
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text"));
        jMenuItem4.setEnabled(false);
        jMenuItem4.setName("jMenuItem4");
        editMenu.add(jMenuItem4);
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text"));
        jMenuItem6.setEnabled(false);
        jMenuItem6.setName("jMenuItem6");
        editMenu.add(jMenuItem6);
        menuBar.add(editMenu);
        viewMenu.setText(resourceMap.getString("viewMenu.text"));
        viewMenu.setName("viewMenu");
        viewBlockMenuItem.setAction(actionMap.get("switchToBlockView"));
        viewBlockMenuItem.setText(resourceMap.getString("viewBlockMenuItem.text"));
        viewBlockMenuItem.setName("viewBlockMenuItem");
        viewMenu.add(viewBlockMenuItem);
        viewGraphMenuItem.setAction(actionMap.get("switchToGraphView"));
        viewGraphMenuItem.setText(resourceMap.getString("viewGraphMenuItem.text"));
        viewGraphMenuItem.setName("viewGraphMenuItem");
        viewMenu.add(viewGraphMenuItem);
        viewRefreshMenuItem.setAction(actionMap.get("refreshView"));
        viewRefreshMenuItem.setText(resourceMap.getString("viewRefreshMenuItem.text"));
        viewRefreshMenuItem.setName("viewRefreshMenuItem");
        viewMenu.add(viewRefreshMenuItem);
        menuBar.add(viewMenu);
        networksMenu.setText(resourceMap.getString("networksMenu.text"));
        networksMenu.setName("networksMenu");
        adalineMenuItem.setAction(actionMap.get("showAdalineWizard"));
        adalineMenuItem.setText(resourceMap.getString("adalineMenuItem.text"));
        adalineMenuItem.setToolTipText(resourceMap.getString("adalineMenuItem.toolTipText"));
        adalineMenuItem.setName("adalineMenuItem");
        networksMenu.add(adalineMenuItem);
        perceptronMenuItem.setAction(actionMap.get("showPerceptronWizard"));
        perceptronMenuItem.setText(resourceMap.getString("perceptronMenuItem.text"));
        perceptronMenuItem.setToolTipText(resourceMap.getString("perceptronMenuItem.toolTipText"));
        perceptronMenuItem.setName("perceptronMenuItem");
        networksMenu.add(perceptronMenuItem);
        mlpMenuItem.setAction(actionMap.get("showMLPerceptronWizard"));
        mlpMenuItem.setText(resourceMap.getString("mlpMenuItem.text"));
        mlpMenuItem.setToolTipText(resourceMap.getString("mlpMenuItem.toolTipText"));
        mlpMenuItem.setName("mlpMenuItem");
        networksMenu.add(mlpMenuItem);
        hopfieldMenuItem.setAction(actionMap.get("showHopfieldWizard"));
        hopfieldMenuItem.setText(resourceMap.getString("hopfieldMenuItem.text"));
        hopfieldMenuItem.setToolTipText(resourceMap.getString("hopfieldMenuItem.toolTipText"));
        hopfieldMenuItem.setName("hopfieldMenuItem");
        networksMenu.add(hopfieldMenuItem);
        bamMenuItem.setAction(actionMap.get("showBAMWizard"));
        bamMenuItem.setText(resourceMap.getString("bamMenuItem.text"));
        bamMenuItem.setName("bamMenuItem");
        networksMenu.add(bamMenuItem);
        kohonenMenuItem.setAction(actionMap.get("showKohonenWizard"));
        kohonenMenuItem.setToolTipText(resourceMap.getString("kohonenMenuItem.toolTipText"));
        kohonenMenuItem.setName("kohonenMenuItem");
        networksMenu.add(kohonenMenuItem);
        supervisedHebbianMenuItem.setAction(actionMap.get("showHebbianWizard"));
        supervisedHebbianMenuItem.setText(resourceMap.getString("supervisedHebbianMenuItem.text"));
        supervisedHebbianMenuItem.setToolTipText(resourceMap.getString("supervisedHebbianMenuItem.toolTipText"));
        supervisedHebbianMenuItem.setName("supervisedHebbianMenuItem");
        networksMenu.add(supervisedHebbianMenuItem);
        unsupervisedHebbianMenuItem.setAction(actionMap.get("showUnsupervisedHebbianWizard"));
        unsupervisedHebbianMenuItem.setText(resourceMap.getString("unsupervisedHebbianMenuItem.text"));
        unsupervisedHebbianMenuItem.setName("unsupervisedHebbianMenuItem");
        networksMenu.add(unsupervisedHebbianMenuItem);
        maxnetMenuItem.setAction(actionMap.get("showMaxnetWizard"));
        maxnetMenuItem.setText(resourceMap.getString("maxnetMenuItem.text"));
        maxnetMenuItem.setName("maxnetMenuItem");
        networksMenu.add(maxnetMenuItem);
        competitiveMenuItem.setAction(actionMap.get("showCompetitiveNetworkWizard"));
        competitiveMenuItem.setText(resourceMap.getString("competitiveMenuItem.text"));
        competitiveMenuItem.setName("competitiveMenuItem");
        networksMenu.add(competitiveMenuItem);
        rbfMenuItem.setAction(actionMap.get("showRbfWizard"));
        rbfMenuItem.setName("rbfMenuItem");
        networksMenu.add(rbfMenuItem);
        instarMenuItem.setAction(actionMap.get("showInstarWizard"));
        instarMenuItem.setText(resourceMap.getString("instarMenuItem.text"));
        instarMenuItem.setName("instarMenuItem");
        networksMenu.add(instarMenuItem);
        outstarMenuItem.setAction(actionMap.get("showOutstarWizard"));
        outstarMenuItem.setText(resourceMap.getString("outstarMenuItem.text"));
        outstarMenuItem.setName("outstarMenuItem");
        networksMenu.add(outstarMenuItem);
        jSeparator1.setName("jSeparator1");
        networksMenu.add(jSeparator1);
        menuBar.add(networksMenu);
        trainingMenu.setText(resourceMap.getString("trainingMenu.text"));
        trainingMenu.setName("trainingMenu");
        trainingSetWizardMenuItem.setAction(actionMap.get("showTrainingSetWizard"));
        trainingSetWizardMenuItem.setText(resourceMap.getString("trainingSetWizardMenuItem.text"));
        trainingSetWizardMenuItem.setToolTipText(resourceMap.getString("trainingSetWizardMenuItem.toolTipText"));
        trainingSetWizardMenuItem.setName("trainingSetWizardMenuItem");
        trainingMenu.add(trainingSetWizardMenuItem);
        trainMenuItem.setAction(actionMap.get("trainNetwork"));
        trainMenuItem.setText(resourceMap.getString("trainMenuItem.text"));
        trainMenuItem.setToolTipText(resourceMap.getString("trainMenuItem.toolTipText"));
        trainMenuItem.setName("trainMenuItem");
        trainingMenu.add(trainMenuItem);
        menuBar.add(trainingMenu);
        toolsMenu.setText(resourceMap.getString("toolsMenu.text"));
        toolsMenu.setName("toolsMenu");
        imageRecognitionMenuItem.setAction(actionMap.get("imageRecognitionSample"));
        imageRecognitionMenuItem.setText(resourceMap.getString("imageRecognitionMenuItem.text"));
        imageRecognitionMenuItem.setName("imageRecognitionMenuItem");
        toolsMenu.add(imageRecognitionMenuItem);
        menuBar.add(toolsMenu);
        samplesMenu.setText(resourceMap.getString("samplesMenu.text"));
        samplesMenu.setName("samplesMenu");
        sampleBasicNeuron.setAction(actionMap.get("showBasicNeuronSample"));
        sampleBasicNeuron.setText(resourceMap.getString("sampleBasicNeuron.text"));
        sampleBasicNeuron.setName("sampleBasicNeuron");
        samplesMenu.add(sampleBasicNeuron);
        sampleKohonenMenuItem.setAction(actionMap.get("kohonenSample"));
        sampleKohonenMenuItem.setText(resourceMap.getString("sampleKohonenMenuItem.text"));
        sampleKohonenMenuItem.setToolTipText(resourceMap.getString("sampleKohonenMenuItem.toolTipText"));
        sampleKohonenMenuItem.setName("sampleKohonenMenuItem");
        samplesMenu.add(sampleKohonenMenuItem);
        sampleNFRMenuItem.setAction(actionMap.get("nfrSample"));
        sampleNFRMenuItem.setText(resourceMap.getString("sampleNFRMenuItem.text"));
        sampleNFRMenuItem.setToolTipText(resourceMap.getString("sampleNFRMenuItem.toolTipText"));
        sampleNFRMenuItem.setName("sampleNFRMenuItem");
        samplesMenu.add(sampleNFRMenuItem);
        sampleRecommenderMenuItem.setAction(actionMap.get("recommenderSample"));
        sampleRecommenderMenuItem.setName("sampleRecommenderMenuItem");
        samplesMenu.add(sampleRecommenderMenuItem);
        menuBar.add(samplesMenu);
        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");
        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text"));
        jMenuItem7.setName("jMenuItem7");
        jMenuItem7.addActionListener((showHelpContents()));
        helpMenu.add(jMenuItem7);
        aboutMenuItem.setAction(actionMap.get("showAboutBox"));
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText"));
        aboutMenuItem.setName("aboutMenuItem");
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        statusPanel.setName("statusPanel");
        statusPanelSeparator.setName("statusPanelSeparator");
        statusMessageLabel.setName("statusMessageLabel");
        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel");
        progressBar.setName("progressBar");
        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE).addGroup(statusPanelLayout.createSequentialGroup().addContainerGap().addComponent(statusMessageLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 451, Short.MAX_VALUE).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(statusAnimationLabel).addContainerGap()));
        statusPanelLayout.setVerticalGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(statusPanelLayout.createSequentialGroup().addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(statusMessageLabel).addComponent(statusAnimationLabel).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(3, 3, 3)));
        treePopupMenu.setName("treePopupMenu");
        renameMenuItem.setAction(actionMap.get("renameTreeNode"));
        renameMenuItem.setName("renameMenuItem");
        treePopupMenu.add(renameMenuItem);
        removeMenuItem.setAction(actionMap.get("removeTreeNode"));
        removeMenuItem.setText(resourceMap.getString("removeMenuItem.text"));
        removeMenuItem.setName("removeMenuItem");
        treePopupMenu.add(removeMenuItem);
        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(jToolBar1);
    }

    public void addNewNetworkToProject(NeuralNetwork nnet) {
        int count = easyNeuronsProject.getNeuralNetworks().size() + 1;
        LabelsPlugin labels = (LabelsPlugin) nnet.getPlugin("LabelsPlugin");
        if ((labels.getLabel(nnet) == null) || (labels.getLabel(nnet).equals(""))) labels.setLabel(nnet, "NewNetwork" + count);
        this.addNetworkToProject(nnet);
    }

    private void addNetworkToProject(NeuralNetwork nnet) {
        easyNeuronsProject.addNeuralNetwork(nnet);
        updateProjectTree();
        this.openNetworkViewFrame(nnet);
    }

    public void updateProjectTree() {
        projectTree.setModel(easyNeuronsProject.getTreeModel());
        int row = 0;
        while (row < projectTree.getRowCount()) {
            projectTree.expandRow(row);
            row++;
        }
    }

    public void updateFrameTitles() {
        JInternalFrame frames[] = desktopPanel.getAllFrames();
        for (int i = 0; i < frames.length; i++) {
            ((NeuralNetworkViewFrame) frames[i]).updateTitle();
        }
    }

    private void treeNodeDoubleClick(int selRow, TreePath selPath) {
        Object[] nodes = selPath.getPath();
        Object selectedNode = ((DefaultMutableTreeNode) nodes[nodes.length - 1]).getUserObject();
        if (selectedNode instanceof NeuralNetwork) {
            NeuralNetwork nnet = (NeuralNetwork) selectedNode;
            this.openNetworkViewFrame(nnet);
        } else if (selectedNode instanceof TrainingSet) {
            TrainingSet trainingSet = (TrainingSet) selectedNode;
            this.openTrainingSetEditFrame(trainingSet);
        }
    }

    private void openNetworkViewFrame(NeuralNetwork nnet) {
        NeuralNetworkViewFrame networkViewFrame = new NeuralNetworkViewFrame(nnet, easyNeuronsProject.getTrainingSets(), this);
        networkViewFrame.addInternalFrameListener(new WindowObserver(networkViewFrame) {

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                netActiveWindow = nn;
                traActiveWindow = null;
            }
        });
        networkViewFrame.setVisible(true);
        desktopPanel.add(networkViewFrame);
        try {
            networkViewFrame.setSelected(true);
            networkViewFrame.setMaximum(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    private void openTrainingSetEditFrame(TrainingSet trainingSet) {
        TrainingSetEditFrame trainingSetEditFrame = new TrainingSetEditFrame(this, trainingSet);
        trainingSetEditFrame.setVisible(true);
        trainingSetEditFrame.addInternalFrameListener(new WindowObserver(trainingSet) {

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                traActiveWindow = ts;
                netActiveWindow = null;
            }
        });
        desktopPanel.add(trainingSetEditFrame);
        try {
            trainingSetEditFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    private void showWizard(JDialog wizardDialog) {
        wizardDialog.setLocationRelativeTo(mainFrame);
        EasyNeuronsApplication.getApplication().show(wizardDialog);
    }

    @Action
    public void showOpenDialog() {
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new FileFilterAdapter.NeuralNetworkBinaryFileFilter());
        fileChooser.addChoosableFileFilter(new FileFilterAdapter.NeuralNetworkXmlFileFilter());
        fileChooser.addChoosableFileFilter(new FileFilterAdapter.TrainingSetBinaryFileFilter());
        fileChooser.addChoosableFileFilter(new FileFilterAdapter.TrainingSetXmlFileFilter());
        int option = fileChooser.showOpenDialog(mainFrame);
        if (option == JFileChooser.CANCEL_OPTION) {
        }
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileExtension = FileUtils.getExtension(selectedFile);
            if (fileExtension != null) {
                if (FileUtils.isNeuralNetwork(fileExtension)) {
                    String location = selectedFile.getPath();
                    String fileName = selectedFile.getName();
                    FileIO io = new FileIO();
                    NeuralNetwork nnet = io.loadNeuralNetwork(location);
                    this.addNetworkToProject(nnet);
                    fileObserver.put(location, fileName);
                }
                if (FileUtils.isTrainingSet(fileExtension)) {
                    String location = selectedFile.getPath();
                    String fileName = selectedFile.getName();
                    fileObserver.put(location, fileName);
                    FileIO io = new FileIO();
                    TrainingSet trainingSet = io.loadTrainingSet(location);
                    updateTrainingSets(trainingSet);
                    this.openTrainingSetEditFrame(trainingSet);
                    fileObserver.put(location, fileName);
                }
            } else {
            }
        } else {
        }
    }

    @Action
    public void showSaveDialog() {
        if (netActiveWindow != null) {
            if (fileObserver.containsKey(netActiveWindow.getFilePath())) {
                FileIO io = new FileIO();
                io.saveNeuralNetwork(netActiveWindow.getNeuralNetwork(), netActiveWindow.getNeuralNetwork().toString(), netActiveWindow.getFilePath());
            } else {
                fileChooser.resetChoosableFileFilters();
                fileChooser.addChoosableFileFilter(new FileFilterAdapter.NeuralNetworkBinaryFileFilter());
                fileChooser.addChoosableFileFilter(new FileFilterAdapter.NeuralNetworkXmlFileFilter());
                int option = fileChooser.showSaveDialog(mainFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    int count = fileChooser.getCurrentDirectory().toString().length();
                    String name = fileChooser.getSelectedFile().toString().substring(count + 1);
                    String location = fileChooser.getSelectedFile().toString() + fileChooser.getFileFilter().getDescription();
                    FileIO io = new FileIO();
                    io.saveNeuralNetwork(netActiveWindow.getNeuralNetwork(), name, location);
                    fileObserver.put(location, name);
                }
                updateProjectTree();
                updateFrameTitles();
            }
        }
        if (traActiveWindow != null) {
            if (fileObserver.containsKey(traActiveWindow.getFilePath())) {
                FileIO io = new FileIO();
                io.saveTrainingSet(traActiveWindow, traActiveWindow.getLabel(), traActiveWindow.getFilePath());
            } else {
                fileChooser.resetChoosableFileFilters();
                fileChooser.addChoosableFileFilter(new FileFilterAdapter.TrainingSetBinaryFileFilter());
                fileChooser.addChoosableFileFilter(new FileFilterAdapter.TrainingSetXmlFileFilter());
                int option = fileChooser.showSaveDialog(mainFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    int count = fileChooser.getCurrentDirectory().toString().length();
                    String name = fileChooser.getSelectedFile().toString().substring(count + 1);
                    String location = fileChooser.getSelectedFile().toString();
                    FileIO io = new FileIO();
                    io.saveTrainingSet(traActiveWindow, name, location);
                    fileObserver.put(location, name);
                }
                updateProjectTree();
                updateFrameTitles();
            }
        }
    }

    @Action
    public void showSaveAsDialog() {
        if (netActiveWindow != null) {
            fileChooser.resetChoosableFileFilters();
            fileChooser.addChoosableFileFilter(new FileFilterAdapter.NeuralNetworkBinaryFileFilter());
            fileChooser.addChoosableFileFilter(new FileFilterAdapter.NeuralNetworkXmlFileFilter());
            int option = fileChooser.showSaveDialog(mainFrame);
            if (option == JFileChooser.APPROVE_OPTION) {
                int count = fileChooser.getCurrentDirectory().toString().length();
                String name = fileChooser.getSelectedFile().toString().substring(count + 1);
                String location = fileChooser.getSelectedFile().toString() + fileChooser.getFileFilter().getDescription();
                FileIO io = new FileIO();
                io.saveNeuralNetwork(netActiveWindow.getNeuralNetwork(), name, location);
                fileObserver.put(location, name);
            }
            updateProjectTree();
            updateFrameTitles();
        }
        if (traActiveWindow != null) {
            fileChooser.resetChoosableFileFilters();
            fileChooser.addChoosableFileFilter(new FileFilterAdapter.TrainingSetBinaryFileFilter());
            fileChooser.addChoosableFileFilter(new FileFilterAdapter.TrainingSetXmlFileFilter());
            int option = fileChooser.showSaveDialog(mainFrame);
            if (option == JFileChooser.APPROVE_OPTION) {
                int count = fileChooser.getCurrentDirectory().toString().length();
                String name = fileChooser.getSelectedFile().toString().substring(count + 1);
                String location = fileChooser.getSelectedFile().toString();
                FileIO io = new FileIO();
                io.saveTrainingSet(traActiveWindow, name, location);
                fileObserver.put(location, name);
            }
            updateProjectTree();
            updateFrameTitles();
        }
    }

    @Action
    public void showAdalineWizard() {
        AdalineWizard wizard = new AdalineWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showPerceptronWizard() {
        PerceptronWizard wizard = new PerceptronWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showMLPerceptronWizard() {
        MLPerceptronWizard wizard = new MLPerceptronWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showHopfieldWizard() {
        HopfieldWizard wizard = new HopfieldWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showBAMWizard() {
        BamWizard wizard = new BamWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showKohonenWizard() {
        KohonenWizard wizard = new KohonenWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showHebbianWizard() {
        SupervisedHebbianWizard wizard = new SupervisedHebbianWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showUnsupervisedHebbianWizard() {
        UnsupervisedHebbianWizard wizard = new UnsupervisedHebbianWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showCompetitiveNetworkWizard() {
        CompetitiveNetworkWizard wizard = new CompetitiveNetworkWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showMaxnetWizard() {
        MaxnetWizard wizard = new MaxnetWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showInstarWizard() {
        InstarWizard wizard = new InstarWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showOutstarWizard() {
        OutstarWizard wizard = new OutstarWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showRbfWizard() {
        RbfWizard wizard = new RbfWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    @Action
    public void showTrainingSetWizard() {
        TrainingSetWizard wizard = new TrainingSetWizard(mainFrame, true, this);
        showWizard(wizard);
    }

    public void newAdalineNetwork(int inputNeurons) {
        Adaline nnet = NeuralNetworkFactory.createAdaline(inputNeurons);
        addNewNetworkToProject(nnet);
    }

    public void newPerceptronNetwork(int inputNeurons, int outputNeurons, TransferFunctionType transferFunction) {
        Perceptron nnet = NeuralNetworkFactory.createPerceptron(inputNeurons, outputNeurons, transferFunction);
        addNewNetworkToProject(nnet);
    }

    public void newInstarNetwork(int inputNeuronsNum) {
        Instar nnet = NeuralNetworkFactory.createInstar(inputNeuronsNum);
        addNewNetworkToProject(nnet);
    }

    public void newOutstarNetwork(int outputNeuronsNum) {
        Outstar nnet = NeuralNetworkFactory.createOutstar(outputNeuronsNum);
        addNewNetworkToProject(nnet);
    }

    public void newMaxnetNetwork(int neuronsNum) {
        MaxNet nnet = NeuralNetworkFactory.createMaxNet(neuronsNum);
        addNewNetworkToProject(nnet);
    }

    public void newCompetitiveNetwork(int inputNeuronsNum, int outputNeuronsNum) {
        CompetitiveNetwork nnet = NeuralNetworkFactory.createCompetitiveNetwork(inputNeuronsNum, outputNeuronsNum);
        addNewNetworkToProject(nnet);
    }

    public void newMLPerceptronNetwork(String neuronsNum, TransferFunctionType transferFunctionType) {
        MultiLayerPerceptron nnet = NeuralNetworkFactory.createMLPerceptron(neuronsNum, transferFunctionType);
        addNewNetworkToProject(nnet);
    }

    public void newHopfieldNetwork(int inputNeurons) {
        Hopfield nnet = NeuralNetworkFactory.createHopfield(inputNeurons);
        addNewNetworkToProject(nnet);
    }

    public void newBamNetwork(int inputNeurons, int outputNeurons) {
        BAM nnet = NeuralNetworkFactory.createBam(inputNeurons, outputNeurons);
        addNewNetworkToProject(nnet);
    }

    public void newKohonenNetwork(int inputNeurons, int mapNeurons) {
        Kohonen nnet = NeuralNetworkFactory.createKohonen(inputNeurons, mapNeurons);
        addNewNetworkToProject(nnet);
    }

    public void newHebbianNetwork(int inputNeurons, int outputNeurons, TransferFunctionType transferFunction) {
        SupervisedHebbianNetwork nnet = NeuralNetworkFactory.createSupervisedHebbian(inputNeurons, outputNeurons, transferFunction);
        addNewNetworkToProject(nnet);
    }

    public void newUnsupervisedHebbianNetwork(int inputNeurons, int outputNeurons, TransferFunctionType transferFunction) {
        UnsupervisedHebbianNetwork nnet = NeuralNetworkFactory.createUnsupervisedHebbian(inputNeurons, outputNeurons, transferFunction);
        addNewNetworkToProject(nnet);
    }

    public void newRbfNetwork(int inputNeuronsNum, int rbfNeuronsNum, int outputNeuronsNum) {
        RbfNetwork nnet = NeuralNetworkFactory.createRbfNetwork(inputNeuronsNum, rbfNeuronsNum, outputNeuronsNum);
        addNewNetworkToProject(nnet);
    }

    public void showTrainingSetEditFrame(int inputs, int outputs, String type, String label) {
        TrainingSet trainingSet = new TrainingSet(label);
        TrainingSetEditFrame trainingSetEditFrame = new TrainingSetEditFrame(this, trainingSet, type, inputs, outputs);
        trainingSetEditFrame.setVisible(true);
        desktopPanel.add(trainingSetEditFrame);
        try {
            trainingSetEditFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    public void updateTrainingSets(TrainingSet trainingSet) {
        if (!easyNeuronsProject.getTrainingSets().contains(trainingSet)) {
            easyNeuronsProject.addTrainingSet(trainingSet);
        }
        updateProjectTree();
    }

    public GraphFrame openErrorGraphFrame() {
        JInternalFrame iframes[] = desktopPanel.getAllFrames();
        for (int i = 0; i < iframes.length; i++) {
            if (iframes[i] instanceof GraphFrame) {
                try {
                    iframes[i].setSelected(true);
                } catch (java.beans.PropertyVetoException e) {
                }
                return (GraphFrame) iframes[i];
            }
        }
        GraphFrame graphFrame = new GraphFrame();
        graphFrame.setVisible(true);
        desktopPanel.add(graphFrame);
        try {
            graphFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
        return graphFrame;
    }

    @Action
    public void trainNetwork() {
        JInternalFrame selectedFrame = desktopPanel.getSelectedFrame();
        if (selectedFrame != null) {
            if (selectedFrame instanceof NeuralNetworkViewFrame) {
                ((NeuralNetworkViewFrame) selectedFrame).train();
            }
        }
    }

    @Action
    public void removeTreeNode() {
        Object[] nodes = projectTree.getSelectionPath().getPath();
        Object selectedNode = ((DefaultMutableTreeNode) nodes[nodes.length - 1]).getUserObject();
        if (selectedNode instanceof NeuralNetwork) {
            NeuralNetwork nnet = (NeuralNetwork) selectedNode;
            easyNeuronsProject.removeNeuralNetwork(nnet);
            this.updateProjectTree();
        } else if (selectedNode instanceof TrainingSet) {
            TrainingSet trainingSet = (TrainingSet) selectedNode;
            easyNeuronsProject.removeTrainingSet(trainingSet);
            this.updateProjectTree();
        }
    }

    @Action
    public void kohonenSample() {
        int sampleSize = 100;
        NeuralNetwork neuralNet = new Kohonen(new Integer(2), new Integer(sampleSize));
        ((LabelsPlugin) neuralNet.getPlugin("LabelsPlugin")).setLabel(neuralNet, "Kohonen sample");
        easyNeuronsProject.addNeuralNetwork(neuralNet);
        TrainingSet trainingSet = new TrainingSet();
        trainingSet.setLabel("Sample training set");
        for (int i = 0; i < sampleSize; i++) {
            Vector<Double> trainVect = new Vector<Double>();
            trainVect.add(Math.random());
            trainVect.add(Math.random());
            TrainingElement te = new TrainingElement(trainVect);
            trainingSet.addElement(te);
        }
        easyNeuronsProject.addTrainingSet(trainingSet);
        updateProjectTree();
        TrainingController controller = new TrainingController(neuralNet, trainingSet);
        KohonenSample kohonenVisualizer = new KohonenSample(controller);
        neuralNet.getLearningRule().addObserver(kohonenVisualizer);
        neuralNet.addObserver(kohonenVisualizer);
        kohonenVisualizer.setVisible(true);
        desktopPanel.add(kohonenVisualizer);
        try {
            kohonenVisualizer.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    @Action
    public void nfrSample() {
        double[][] pointsSets = { { 0, 0, 20, 22 }, { 20, 22, 40, 42 }, { 40, 42, 80, 82 }, { 80, 82, 100, 100 } };
        double[][] timeSets = { { 15, 15, 20, 25 }, { 20, 25, 35, 40 }, { 35, 40, 1000, 1000 } };
        NeuralNetwork nnet = new NeuroFuzzyReasoner(pointsSets, timeSets);
        TrainingSet tSet = new TrainingSet();
        Layer setLayer = nnet.getLayerAt(1);
        int outClass = 0;
        for (int i = 0; i <= 3; i++) {
            Neuron icell = setLayer.getNeuronAt(i);
            Trapezoid tfi = (Trapezoid) icell.getTransferFunction();
            double r1i = tfi.getRightLow();
            double l2i = tfi.getLeftHigh();
            double r2i = tfi.getRightHigh();
            double right_intersection_i = r2i + (r1i - r2i) / 2;
            for (int j = 6; j >= 4; j--) {
                Neuron jcell = setLayer.getNeuronAt(j);
                Trapezoid tfj = (Trapezoid) jcell.getTransferFunction();
                double r1j = tfj.getRightLow();
                double l2j = tfj.getLeftHigh();
                double r2j = tfj.getRightHigh();
                double right_intersection_j = r2j + (r1j - r2j) / 2;
                String outputPattern;
                if (outClass <= 3) {
                    outputPattern = "1 0 0 0";
                } else if ((outClass >= 4) && (outClass <= 6)) {
                    outputPattern = "0 1 0 0";
                } else if ((outClass >= 7) && (outClass <= 9)) {
                    outputPattern = "0 0 1 0";
                } else {
                    outputPattern = "0 0 0 1";
                }
                String inputPattern = Double.toString(l2i) + " " + Double.toString(l2j);
                SupervisedTrainingElement tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(l2i) + " " + Double.toString(r2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(l2i) + " " + Double.toString(right_intersection_j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(r2i) + " " + Double.toString(l2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(r2i) + " " + Double.toString(r2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(r2i) + " " + Double.toString(right_intersection_j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(right_intersection_i) + " " + Double.toString(l2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(right_intersection_i) + " " + Double.toString(r2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(right_intersection_i) + " " + Double.toString(right_intersection_j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                outClass++;
            }
        }
        ((LabelsPlugin) nnet.getPlugin("LabelsPlugin")).setLabel(nnet, "NFR sample");
        tSet.setLabel("NFR tset");
        easyNeuronsProject.addNeuralNetwork(nnet);
        easyNeuronsProject.addTrainingSet(tSet);
        updateProjectTree();
        TrainingController controller = new TrainingController(nnet, tSet);
        NFRSample frame = new NFRSample(controller);
        frame.setVisible(true);
        desktopPanel.add(frame);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            aboutBox = new AboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        EasyNeuronsApplication.getApplication().show(aboutBox);
    }

    @Action
    public void renameTreeNode() {
        Object[] nodes = projectTree.getSelectionPath().getPath();
        Object selectedNode = ((DefaultMutableTreeNode) nodes[nodes.length - 1]).getUserObject();
        RenameNodeDialog dialog = new RenameNodeDialog(mainFrame, true, this, selectedNode);
        dialog.setLocationRelativeTo(mainFrame);
        EasyNeuronsApplication.getApplication().show(dialog);
    }

    public void treeNodesChanged(TreeModelEvent e) {
        Object[] nodes = projectTree.getSelectionPath().getPath();
        Object selectedNode = ((DefaultMutableTreeNode) nodes[nodes.length - 1]).getUserObject();
        String newLabel = selectedNode.toString();
        if (selectedNode instanceof NeuralNetwork) {
            NeuralNetwork nnet = (NeuralNetwork) selectedNode;
            ((LabelsPlugin) nnet.getPlugin("LabelsPlugin")).setLabel(nnet, newLabel);
        } else if (selectedNode instanceof TrainingSet) {
            TrainingSet trainingSet = (TrainingSet) selectedNode;
            trainingSet.setLabel(newLabel);
        }
    }

    public JTree getProjectTree() {
        return this.projectTree;
    }

    @Action
    public void switchToGraphView() {
        JInternalFrame iframe = desktopPanel.getSelectedFrame();
        if (iframe instanceof NeuralNetworkViewFrame) {
            ((NeuralNetworkViewFrame) iframe).switchToView(NeuralNetworkViewFrame.GRAPH_VIEW);
        }
    }

    @Action
    public void switchToBlockView() {
        JInternalFrame iframe = desktopPanel.getSelectedFrame();
        if (iframe instanceof NeuralNetworkViewFrame) {
            ((NeuralNetworkViewFrame) iframe).switchToView(NeuralNetworkViewFrame.BLOCK_VIEW);
        }
    }

    @Action
    public void refreshView() {
        JInternalFrame iframe = desktopPanel.getSelectedFrame();
        if (iframe instanceof NeuralNetworkViewFrame) {
            ((NeuralNetworkViewFrame) iframe).refresh();
        }
    }

    @Action
    public void showBasicNeuronSample() {
        BasicNeuronSample sample = new BasicNeuronSample();
        sample.setVisible(true);
        desktopPanel.add(sample);
        try {
            sample.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    public ActionListener showHelpContents() {
        String path = "org/neuroph/easyneurons/help/EasyNeurons.hs";
        ClassLoader loader = this.getClass().getClassLoader();
        HelpSet hs = null;
        try {
            URL hsURL = HelpSet.findHelpSet(loader, path);
            hs = new HelpSet(loader, hsURL);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        HelpBroker hb = hs.createHelpBroker();
        return new CSH.DisplayHelpFromSource(hb);
    }

    @Action
    public void recommenderSample() {
        NeuralNetwork nnet = new org.neuroph.contrib.RecommenderNetwork();
        ((org.neuroph.contrib.RecommenderNetwork) nnet).createDemoNetwork();
        TrainingSet tSet = new TrainingSet();
        ((LabelsPlugin) nnet.getPlugin("LabelsPlugin")).setLabel(nnet, "Recommender sample");
        tSet.setLabel("E-commerce tset");
        easyNeuronsProject.addNeuralNetwork(nnet);
        easyNeuronsProject.addTrainingSet(tSet);
        updateProjectTree();
        TrainingController controller = new TrainingController(nnet, tSet);
    }

    @Action
    public void imageRecognitionSample() {
        org.neuroph.easyneurons.imgrec.ImageRecognitionFrame sample = new org.neuroph.easyneurons.imgrec.ImageRecognitionFrame(this);
        sample.setVisible(true);
        desktopPanel.add(sample);
        try {
            sample.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }
    }

    public EasyNeuronsProject getProject() {
        return this.easyNeuronsProject;
    }

    private javax.swing.JMenuItem adalineMenuItem;

    private javax.swing.JMenuItem bamMenuItem;

    private javax.swing.JMenuItem competitiveMenuItem;

    private javax.swing.JDesktopPane desktopPanel;

    private javax.swing.JMenu editMenu;

    private javax.swing.JMenuItem hopfieldMenuItem;

    private javax.swing.JMenuItem imageRecognitionMenuItem;

    private javax.swing.JMenuItem instarMenuItem;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JMenuItem jMenuItem2;

    private javax.swing.JMenuItem jMenuItem3;

    private javax.swing.JMenuItem jMenuItem4;

    private javax.swing.JMenuItem jMenuItem6;

    private javax.swing.JMenuItem jMenuItem7;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JToolBar jToolBar1;

    private javax.swing.JMenuItem kohonenMenuItem;

    private javax.swing.JPanel mainPanel;

    private javax.swing.JMenuItem maxnetMenuItem;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JMenuItem mlpMenuItem;

    private javax.swing.JMenu networksMenu;

    private javax.swing.JButton newTrainingSetButton;

    private javax.swing.JButton openButton;

    private javax.swing.JMenuItem openMenuItem;

    private javax.swing.JMenuItem outstarMenuItem;

    private javax.swing.JMenuItem perceptronMenuItem;

    private javax.swing.JProgressBar progressBar;

    private javax.swing.JTree projectTree;

    private javax.swing.JScrollPane projectTreeScrollPane;

    private javax.swing.JMenuItem rbfMenuItem;

    private javax.swing.JMenuItem removeMenuItem;

    private javax.swing.JMenuItem renameMenuItem;

    private javax.swing.JMenuItem sampleBasicNeuron;

    private javax.swing.JMenuItem sampleKohonenMenuItem;

    private javax.swing.JMenuItem sampleNFRMenuItem;

    private javax.swing.JMenuItem sampleRecommenderMenuItem;

    private javax.swing.JMenu samplesMenu;

    private javax.swing.JMenuItem saveAsMenuItem;

    private javax.swing.JButton saveButton;

    private javax.swing.JMenuItem saveMenuItem;

    private javax.swing.JLabel statusAnimationLabel;

    private javax.swing.JLabel statusMessageLabel;

    private javax.swing.JPanel statusPanel;

    private javax.swing.JMenuItem supervisedHebbianMenuItem;

    private javax.swing.JMenu toolsMenu;

    private javax.swing.JMenuItem trainMenuItem;

    private javax.swing.JMenu trainingMenu;

    private javax.swing.JMenuItem trainingSetWizardMenuItem;

    private javax.swing.JPopupMenu treePopupMenu;

    private javax.swing.JMenuItem unsupervisedHebbianMenuItem;

    private javax.swing.JMenuItem viewBlockMenuItem;

    private javax.swing.JMenuItem viewGraphMenuItem;

    private javax.swing.JMenu viewMenu;

    private javax.swing.JMenuItem viewRefreshMenuItem;

    private final Timer messageTimer;

    private final Timer busyIconTimer;

    private final Icon idleIcon;

    private final Icon[] busyIcons = new Icon[15];

    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
