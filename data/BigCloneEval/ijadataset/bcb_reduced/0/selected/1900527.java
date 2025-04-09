package it.unisa.dia.jzks.gui;

import it.unisa.dia.jzks.edb.CommitmentMerkleTree;
import it.unisa.dia.jzks.edb.FailedZKSVerifyException;
import it.unisa.dia.jzks.edb.InvalidECParameterException;
import it.unisa.dia.jzks.edb.KeyMismatchZKSVerifyException;
import it.unisa.dia.jzks.edb.ParameterValueMismatch;
import it.unisa.dia.jzks.edb.PiGreek;
import it.unisa.dia.jzks.edb.SecurityParameterNotSatisfied;
import it.unisa.dia.jzks.edb.Utils;
import it.unisa.dia.jzks.edb.ZKSVerifier;
import it.unisa.dia.jzks.edb.ZeroKnowledgeSet;
import it.unisa.dia.jzks.gui.utils.SpinnerNumberModelPow2;
import it.unisa.dia.jzks.merkleTree.InvalidQParameterException;
import it.unisa.dia.jzks.merkleTree.LinkedMerkleTree;
import it.unisa.dia.jzks.merkleTree.RootMerkleNode;
import it.unisa.dia.jzks.utils.MerkleTree2D;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.ComboBoxModel;
import javax.swing.DebugGraphics;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Operating extends javax.swing.JFrame {

    class ThreadCommitment extends Thread {

        public void run() {
            Logger log = Logger.getLogger("it.unisa.dia.jzks.gui");
            Integer rParameter = (Integer) jSpinnerRParameter.getValue();
            Integer qParameter = (Integer) jSpinnerQParameter.getValue();
            Integer q_aryTree = (Integer) jSpinnerQAryTree.getValue();
            String hashType = (String) jComboBoxHashAlgoType.getSelectedItem();
            log.info("START");
            try {
                comm = new CommitmentMerkleTree(rParameter.intValue(), qParameter.intValue(), q_aryTree.intValue(), hashType);
                if (!comm.populateTreeLeaves(hashTableData)) jLabelErrorData.setText("Error during create the Merkle tree"); else {
                    log.info(String.valueOf(comm.getTree().size()));
                    comm.commit();
                    log.info("stop");
                    jButtonSave.setEnabled(true);
                    jButtonViewTreeGenerate.setEnabled(true);
                    jLabelGenerateStatus.setText("Process finished.");
                }
            } catch (NoSuchAlgorithmException e) {
                jLabelErrorParameterCommitment.setText("Hashing algorith not valid");
            } catch (InvalidQParameterException e) {
                jLabelErrorParameterCommitment.setText("Q-ary parameter not valid");
            } catch (InvalidECParameterException e) {
                jLabelErrorParameterCommitment.setText("EC parameter not valid");
            } catch (SecurityParameterNotSatisfied e) {
                jLabelErrorParameterCommitment.setText("The depth of the tree does not satisfy the security parameter. Must be {lambda >= |Digest|}");
            } catch (ParameterValueMismatch e) {
                jLabelErrorParameterCommitment.setText("The Digest legth mismath with q paramter. Must be {|Digest| mod (log2 q) = 0 }");
            }
        }
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int BUFFER = 2048;

    private JLabel jLabelInformation;

    private JPanel jPanelStatus;

    private JPanel jPanelCaricamentoDatiDB;

    private JSplitPane jSplitPaneGenerazione;

    private JPanel jPanelZKSProverTab;

    private JPanel jPanelZKSGenerateTab;

    private JTabbedPane tab;

    private JButton jButtonSave;

    private JSpinner jSpinnerQAryTree;

    private JLabel jLabelNumberOfChild;

    private JSpinner jSpinnerQParameter;

    private JLabel jLabelQParameter;

    private JSpinner jSpinnerRParameter;

    private JLabel jLabelRBits;

    private JPanel jPanelSecureParameters;

    private JTabbedPane jTabbedPaneRightCreateZKS;

    private JButton jButtonDelete;

    private JButton jButtonGenerateZKS;

    private JList jListData;

    private JLabel jLabelErrorData;

    private JButton jButtonLoadFromFile;

    private JButton jButtonAddKeyValue;

    private JTextField jTextFieldValue;

    private JTextField jTextFieldKey;

    private JLabel jLabelValue;

    private JLabel jLabelChiave;

    private JLabel jLabelInformationImageToolTip;

    private JComboBox jComboBoxHashAlgoType;

    private JLabel jLabelHashingAlgorithm;

    private JScrollPane scroll;

    private JFileChooser fc;

    private JLabel jLabelSeedProverWriter;

    private JLabel jLabelSaveResult;

    private JButton jButtonSaveProof;

    private JTextField jTextFieldSearchValue;

    private JLabel jLabelSearchValue;

    private JLabel jLabelResultProver;

    private JButton jButtonProver;

    private JTextField jTextFieldSearchKey;

    private JLabel jLabelSearchKey;

    private JLabel jLabelTreeHeightProverWriter;

    private JLabel jLabelTreeHeight;

    private JLabel jLabelSeedProver;

    private JLabel jLabelLambdaValueProverWriter;

    private JLabel jLabelLambdaValueProver;

    private JLabel jLabelHashingAlgorithmWriter;

    private JLabel jLabelHashAlgorithmProver;

    private JButton jButtonLoadFilesIntoTabProver;

    private JLabel jLabelQValueWrite;

    private JLabel jLabelQValue;

    private JPanel jPanelInformationWriter;

    private JLabel jLabelProofInformation;

    private JLabel jLabelCertInformation;

    private JLabel jLabelPKInformation;

    private JLabel jLabelLoadFileInformationToolTip;

    private JLabel jLabelLoadCertStatusVerifier;

    private JButton jButtonLoadCertVerifier;

    private JLabel jLabelErrorParameterCommitment;

    private JLabel jLabelGenerateStatus;

    private JButton jButtonViewTreeProof;

    private JButton jButtonCancelGenerate;

    private JButton jButtonViewTreeGenerate;

    private JTextField jTextKeyWriteVerifier;

    private JLabel jLabelKeyVerifier;

    private JLabel jLabelVerifyVerifierStatus;

    private JButton jButtonVerifyVerifier;

    private JLabel jLabelLoadProofVerifierStatus;

    private JButton jButtonLoadProofVerifier;

    private JLabel jLabelLoadPKVerifierStatus;

    private JButton jButtonLoadPK;

    private JPanel jPanelZKSVerifier;

    private JPanel jPanelZKSProver;

    private JSplitPane jSplitPaneProver;

    private JLabel jLabelErrorIntoLoadTab;

    private Thread treadCommitment;

    private static String TEMP_PATH = System.getProperty("java.io.tmpdir");

    private LinkedMerkleTree tree;

    private RootMerkleNode root;

    private Hashtable<String, Object> hashTableData;

    private Vector<String> dataIntoJList;

    private Logger logger = Logger.getLogger("it.unisa.dia.zks");

    private CommitmentMerkleTree comm;

    private String xmlPKPath;

    private PiGreek piGreek;

    private String ext;

    public Operating() {
        super();
        initGUI();
        hashTableData = new Hashtable<String, Object>();
        dataIntoJList = new Vector<String>();
        logger.setLevel(Level.INFO);
    }

    private void initGUI() {
        try {
            {
                getContentPane().setLayout(null);
                this.setTitle("Java Zero-Knowledge Sets");
                this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                {
                    tab = new JTabbedPane();
                    getContentPane().add(tab);
                    tab.setBounds(7, 8, 770, 551);
                    tab.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent evt) {
                            tabStateChanged(evt);
                        }
                    });
                    {
                        jPanelZKSGenerateTab = new JPanel();
                        tab.addTab("ZKS Generator", null, jPanelZKSGenerateTab, null);
                        jPanelZKSGenerateTab.setPreferredSize(new java.awt.Dimension(719, 365));
                        {
                            jSplitPaneGenerazione = new JSplitPane();
                            jPanelZKSGenerateTab.add(jSplitPaneGenerazione);
                            jSplitPaneGenerazione.setPreferredSize(new java.awt.Dimension(765, 368));
                            {
                                jPanelCaricamentoDatiDB = new JPanel();
                                jSplitPaneGenerazione.add(jPanelCaricamentoDatiDB, JSplitPane.LEFT);
                                jPanelCaricamentoDatiDB.setPreferredSize(new java.awt.Dimension(360, 100));
                                jPanelCaricamentoDatiDB.setSize(360, 100);
                                jPanelCaricamentoDatiDB.setLayout(null);
                                {
                                    jLabelInformation = new JLabel();
                                    FlowLayout jLabelInformationLayout = new FlowLayout();
                                    jLabelInformation.setLayout(jLabelInformationLayout);
                                    jPanelCaricamentoDatiDB.add(jLabelInformation, "1, 0 1 1");
                                    jLabelInformation.setText("Loading Information");
                                    jLabelInformation.setBounds(18, 15, 121, 28);
                                }
                                {
                                    jLabelChiave = new JLabel();
                                    jPanelCaricamentoDatiDB.add(jLabelChiave);
                                    jLabelChiave.setText("Key");
                                    jLabelChiave.setBounds(70, 49, 37, 22);
                                }
                                {
                                    jLabelValue = new JLabel();
                                    jPanelCaricamentoDatiDB.add(jLabelValue);
                                    jLabelValue.setText("Value");
                                    jLabelValue.setBounds(215, 50, 87, 16);
                                }
                                {
                                    jTextFieldKey = new JTextField();
                                    jPanelCaricamentoDatiDB.add(jTextFieldKey);
                                    jTextFieldKey.setText("Key");
                                    jTextFieldKey.setBounds(12, 75, 159, 23);
                                }
                                {
                                    jTextFieldValue = new JTextField();
                                    jPanelCaricamentoDatiDB.add(jTextFieldValue);
                                    jTextFieldValue.setText("Value");
                                    jTextFieldValue.setBounds(189, 75, 159, 23);
                                }
                                {
                                    jButtonAddKeyValue = new JButton();
                                    jPanelCaricamentoDatiDB.add(jButtonAddKeyValue);
                                    jButtonAddKeyValue.setText("Add");
                                    jButtonAddKeyValue.setBounds(67, 115, 81, 23);
                                    jButtonAddKeyValue.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent evt) {
                                            jButtonAddKeyValueActionPerformed(evt);
                                        }
                                    });
                                }
                                {
                                    jButtonLoadFromFile = new JButton();
                                    jPanelCaricamentoDatiDB.add(jButtonLoadFromFile);
                                    jButtonLoadFromFile.setText("Load from file...");
                                    jButtonLoadFromFile.setBounds(189, 115, 134, 23);
                                    jButtonLoadFromFile.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent evt) {
                                            jButtonLoadFromFileActionPerformed(evt);
                                        }
                                    });
                                }
                                {
                                    jLabelErrorData = new JLabel();
                                    jPanelCaricamentoDatiDB.add(jLabelErrorData);
                                    jLabelErrorData.setBounds(43, 157, 204, 18);
                                }
                                {
                                    ListModel jListDataModel = new DefaultComboBoxModel(new String[] { "" });
                                    jListData = new JList();
                                    jListData.setModel(jListDataModel);
                                    jListData.setBounds(232, 181, 121, 179);
                                    jListData.setPreferredSize(new java.awt.Dimension(121, 165));
                                    jListData.addListSelectionListener(new ListSelectionListener() {

                                        public void valueChanged(ListSelectionEvent evt) {
                                            jListDataValueChanged(evt);
                                        }
                                    });
                                }
                                {
                                    scroll = new JScrollPane(jListData);
                                    jPanelCaricamentoDatiDB.add(scroll);
                                    scroll.setBounds(12, 187, 336, 167);
                                    scroll.setMaximumSize(new java.awt.Dimension(200, 200));
                                }
                                {
                                    jButtonDelete = new JButton();
                                    jPanelCaricamentoDatiDB.add(jButtonDelete);
                                    jButtonDelete.setText("Delete entry");
                                    jButtonDelete.setBounds(246, 155, 102, 23);
                                    jButtonDelete.setEnabled(false);
                                    jButtonDelete.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent evt) {
                                            jButtonDeleteActionPerformed(evt);
                                        }
                                    });
                                }
                                {
                                    jLabelInformationImageToolTip = new JLabel();
                                    jPanelCaricamentoDatiDB.add(jLabelInformationImageToolTip);
                                    jLabelInformationImageToolTip.setText("ImageToolTip");
                                    jLabelInformationImageToolTip.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/information16.jpg")));
                                    jLabelInformationImageToolTip.setBounds(328, 118, 21, 16);
                                    jLabelInformationImageToolTip.setToolTipText("<html><body>" + "The information into the text file must be divided by a <b>';'</b> <br>" + "every couple in a newline<br>" + "Es:<br>" + "    Key;value" + "</body></html>");
                                }
                            }
                            {
                                jTabbedPaneRightCreateZKS = new JTabbedPane();
                                jSplitPaneGenerazione.add(jTabbedPaneRightCreateZKS, JSplitPane.RIGHT);
                                {
                                    jPanelSecureParameters = new JPanel();
                                    jTabbedPaneRightCreateZKS.addTab("Secure parameters", null, jPanelSecureParameters, null);
                                    jPanelSecureParameters.setLayout(null);
                                    {
                                        jLabelRBits = new JLabel();
                                        jPanelSecureParameters.add(jLabelRBits);
                                        jLabelRBits.setText("Number of bits for EC r parameter (lambda)");
                                        jLabelRBits.setBounds(11, 18, 247, 22);
                                    }
                                    {
                                        SpinnerNumberModel snb = new SpinnerNumberModel();
                                        snb.setMinimum(new Integer(0));
                                        snb.setValue(new Integer(16));
                                        snb.setStepSize(new Integer(1));
                                        jSpinnerRParameter = new JSpinner(snb);
                                        jPanelSecureParameters.add(jSpinnerRParameter);
                                        jSpinnerRParameter.setBounds(270, 18, 111, 20);
                                    }
                                    {
                                        jLabelQParameter = new JLabel();
                                        jPanelSecureParameters.add(jLabelQParameter);
                                        jLabelQParameter.setText("Number of bits for EC q parameter");
                                        jLabelQParameter.setBounds(11, 65, 213, 16);
                                    }
                                    {
                                        SpinnerNumberModel snb = new SpinnerNumberModel();
                                        snb.setMinimum(new Integer(0));
                                        snb.setValue(new Integer(64));
                                        snb.setStepSize(new Integer(1));
                                        jSpinnerQParameter = new JSpinner(snb);
                                        jPanelSecureParameters.add(jSpinnerQParameter);
                                        jSpinnerQParameter.setBounds(270, 58, 109, 20);
                                    }
                                    {
                                        jLabelNumberOfChild = new JLabel();
                                        jPanelSecureParameters.add(jLabelNumberOfChild);
                                        jLabelNumberOfChild.setText("Number of child (q-ary Tree)");
                                        jLabelNumberOfChild.setBounds(12, 108, 212, 19);
                                    }
                                    {
                                        SpinnerNumberModelPow2 snb = new SpinnerNumberModelPow2();
                                        snb.setValue(new Integer(16));
                                        jSpinnerQAryTree = new JSpinner(snb);
                                        jPanelSecureParameters.add(jSpinnerQAryTree);
                                        jSpinnerQAryTree.setBounds(270, 105, 110, 24);
                                    }
                                    {
                                        jLabelHashingAlgorithm = new JLabel();
                                        jPanelSecureParameters.add(jLabelHashingAlgorithm);
                                        jLabelHashingAlgorithm.setText("Hashing Algorithm");
                                        jLabelHashingAlgorithm.setBounds(12, 150, 153, 21);
                                    }
                                    {
                                        ComboBoxModel jComboBoxHashAlgoTypeModel = new DefaultComboBoxModel(new String[] { "SHA-1", "SHA-256", "SHA-384", "SHA-512" });
                                        jComboBoxHashAlgoType = new JComboBox();
                                        jPanelSecureParameters.add(jComboBoxHashAlgoType);
                                        jComboBoxHashAlgoType.setModel(jComboBoxHashAlgoTypeModel);
                                        jComboBoxHashAlgoType.setBounds(270, 148, 109, 23);
                                        jComboBoxHashAlgoTypeModel.setSelectedItem("SHA-1");
                                    }
                                    {
                                        jLabelErrorParameterCommitment = new JLabel();
                                        jPanelSecureParameters.add(jLabelErrorParameterCommitment);
                                        jLabelErrorParameterCommitment.setBounds(68, 262, 287, 20);
                                    }
                                }
                            }
                        }
                        {
                            jPanelStatus = new JPanel();
                            jPanelZKSGenerateTab.add(jPanelStatus);
                            jPanelStatus.setPreferredSize(new java.awt.Dimension(765, 141));
                            jPanelStatus.setLayout(null);
                            jPanelStatus.setDebugGraphicsOptions(DebugGraphics.BUFFERED_OPTION);
                            {
                                jButtonGenerateZKS = new JButton();
                                jPanelStatus.add(jButtonGenerateZKS);
                                jButtonGenerateZKS.setText("Generate ZKS");
                                jButtonGenerateZKS.setBounds(31, 16, 120, 30);
                                jButtonGenerateZKS.setEnabled(false);
                                jButtonGenerateZKS.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        jButtonGenerateZKSActionPerformed(evt);
                                    }
                                });
                            }
                            {
                                jButtonSave = new JButton();
                                jPanelStatus.add(jButtonSave);
                                jButtonSave.setText("Save files");
                                jButtonSave.setBounds(356, 18, 115, 30);
                                jButtonSave.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        jButtonSaveActionPerformed(evt);
                                    }
                                });
                                jButtonSave.setEnabled(false);
                            }
                            {
                                jButtonViewTreeGenerate = new JButton();
                                jPanelStatus.add(jButtonViewTreeGenerate);
                                jButtonViewTreeGenerate.setText("View tree");
                                jButtonViewTreeGenerate.setBounds(514, 19, 127, 26);
                                jButtonViewTreeGenerate.setEnabled(false);
                                jButtonViewTreeGenerate.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        jButtonViewTreeGenerateActionPerformed(evt);
                                    }
                                });
                            }
                            {
                                jButtonCancelGenerate = new JButton();
                                jPanelStatus.add(jButtonCancelGenerate);
                                jButtonCancelGenerate.setText("Cancel");
                                jButtonCancelGenerate.setBounds(195, 19, 112, 27);
                                jButtonCancelGenerate.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent evt) {
                                        jButtonCancelGenerateActionPerformed(evt);
                                    }
                                });
                                jButtonCancelGenerate.setEnabled(false);
                            }
                            {
                                jLabelGenerateStatus = new JLabel();
                                jPanelStatus.add(jLabelGenerateStatus);
                                jLabelGenerateStatus.setBounds(111, 93, 537, 23);
                            }
                        }
                    }
                    {
                        jPanelZKSProverTab = new JPanel();
                        tab.addTab("ZKS Prover", null, jPanelZKSProverTab, null);
                        jPanelZKSProverTab.setPreferredSize(new java.awt.Dimension(526, 287));
                        jPanelZKSProverTab.setLayout(null);
                        {
                            jButtonLoadFilesIntoTabProver = new JButton();
                            jPanelZKSProverTab.add(jButtonLoadFilesIntoTabProver);
                            jButtonLoadFilesIntoTabProver.setText("Load Files...");
                            jButtonLoadFilesIntoTabProver.setBounds(300, 48, 104, 26);
                            jButtonLoadFilesIntoTabProver.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {
                                    jButtonLoadFilesIntoTabProverActionPerformed(evt);
                                }
                            });
                        }
                        {
                            jLabelErrorIntoLoadTab = new JLabel();
                            jPanelZKSProverTab.add(jLabelErrorIntoLoadTab);
                            jLabelErrorIntoLoadTab.setBounds(36, 19, 522, 23);
                        }
                        {
                            jSplitPaneProver = new JSplitPane();
                            jPanelZKSProverTab.add(jSplitPaneProver);
                            jSplitPaneProver.setBounds(12, 87, 741, 393);
                            {
                                jPanelZKSProver = new JPanel();
                                jSplitPaneProver.add(jPanelZKSProver, JSplitPane.LEFT);
                                jPanelZKSProver.setPreferredSize(new java.awt.Dimension(365, 391));
                                jPanelZKSProver.setLayout(null);
                                {
                                    jLabelSearchKey = new JLabel();
                                    jPanelZKSProver.add(jLabelSearchKey);
                                    jLabelSearchKey.setText("Search key");
                                    jLabelSearchKey.setBounds(37, 44, 84, 21);
                                }
                                {
                                    jTextFieldSearchKey = new JTextField();
                                    jPanelZKSProver.add(jTextFieldSearchKey);
                                    jTextFieldSearchKey.setText("key");
                                    jTextFieldSearchKey.setBounds(186, 44, 144, 21);
                                    jTextFieldSearchKey.addMouseListener(new MouseAdapter() {

                                        public void mouseClicked(MouseEvent evt) {
                                            jTextFieldSearchKeyMouseClicked(evt);
                                        }
                                    });
                                }
                                {
                                    jButtonProver = new JButton();
                                    jPanelZKSProver.add(jButtonProver);
                                    jButtonProver.setText("Generate proof");
                                    jButtonProver.setBounds(123, 138, 119, 23);
                                    jButtonProver.setEnabled(false);
                                    jButtonProver.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent evt) {
                                            jButtonGenerateProofActionPerformed(evt);
                                        }
                                    });
                                }
                                {
                                    jLabelResultProver = new JLabel();
                                    jPanelZKSProver.add(jLabelResultProver);
                                    jLabelResultProver.setBounds(32, 203, 293, 19);
                                }
                                {
                                    jLabelSearchValue = new JLabel();
                                    jPanelZKSProver.add(jLabelSearchValue);
                                    jLabelSearchValue.setText("Search value");
                                    jLabelSearchValue.setBounds(37, 83, 85, 22);
                                }
                                {
                                    jTextFieldSearchValue = new JTextField();
                                    jPanelZKSProver.add(jTextFieldSearchValue);
                                    jTextFieldSearchValue.setText("Value");
                                    jTextFieldSearchValue.setBounds(186, 83, 144, 22);
                                    jTextFieldSearchValue.addMouseListener(new MouseAdapter() {

                                        public void mouseClicked(MouseEvent evt) {
                                            jTextFieldSearchValueMouseClicked(evt);
                                        }
                                    });
                                }
                                {
                                    jButtonSaveProof = new JButton();
                                    jPanelZKSProver.add(jButtonSaveProof);
                                    jButtonSaveProof.setText("Save proof");
                                    jButtonSaveProof.setBounds(126, 283, 97, 22);
                                    jButtonSaveProof.setEnabled(false);
                                    jButtonSaveProof.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent evt) {
                                            jButtonSaveProofActionPerformed(evt);
                                        }
                                    });
                                }
                                {
                                    jLabelSaveResult = new JLabel();
                                    jPanelZKSProver.add(jLabelSaveResult);
                                    jLabelSaveResult.setBounds(91, 345, 171, 20);
                                }
                            }
                            {
                                jPanelInformationWriter = new JPanel();
                                jSplitPaneProver.add(jPanelInformationWriter, JSplitPane.RIGHT);
                                jPanelInformationWriter.setLayout(null);
                                {
                                    jLabelQValue = new JLabel();
                                    jPanelInformationWriter.add(jLabelQValue);
                                    jLabelQValue.setText("Number of child (q-value)");
                                    jLabelQValue.setBounds(20, 43, 157, 19);
                                }
                                {
                                    jLabelQValueWrite = new JLabel();
                                    jPanelInformationWriter.add(jLabelQValueWrite);
                                    jLabelQValueWrite.setBounds(209, 43, 87, 18);
                                }
                                {
                                    jLabelHashAlgorithmProver = new JLabel();
                                    jPanelInformationWriter.add(jLabelHashAlgorithmProver);
                                    jLabelHashAlgorithmProver.setText("Hashing Algorithm");
                                    jLabelHashAlgorithmProver.setBounds(20, 74, 111, 16);
                                }
                                {
                                    jLabelHashingAlgorithmWriter = new JLabel();
                                    jPanelInformationWriter.add(jLabelHashingAlgorithmWriter);
                                    jLabelHashingAlgorithmWriter.setBounds(209, 73, 124, 17);
                                }
                                {
                                    jLabelLambdaValueProver = new JLabel();
                                    jPanelInformationWriter.add(jLabelLambdaValueProver);
                                    jLabelLambdaValueProver.setText("Lambda (secure param)");
                                    jLabelLambdaValueProver.setBounds(20, 102, 157, 19);
                                }
                                {
                                    jLabelLambdaValueProverWriter = new JLabel();
                                    jPanelInformationWriter.add(jLabelLambdaValueProverWriter);
                                    jLabelLambdaValueProverWriter.setBounds(209, 96, 132, 23);
                                }
                                {
                                    jLabelSeedProver = new JLabel();
                                    jPanelInformationWriter.add(jLabelSeedProver);
                                    jLabelSeedProver.setText("Seed");
                                    jLabelSeedProver.setBounds(20, 133, 80, 18);
                                }
                                {
                                    jLabelSeedProverWriter = new JLabel();
                                    jPanelInformationWriter.add(jLabelSeedProverWriter);
                                    jLabelSeedProverWriter.setBounds(209, 129, 132, 22);
                                }
                                {
                                    jLabelTreeHeight = new JLabel();
                                    jPanelInformationWriter.add(jLabelTreeHeight);
                                    jLabelTreeHeight.setText("Height of tree");
                                    jLabelTreeHeight.setBounds(20, 163, 105, 24);
                                }
                                {
                                    jLabelTreeHeightProverWriter = new JLabel();
                                    jPanelInformationWriter.add(jLabelTreeHeightProverWriter);
                                    jLabelTreeHeightProverWriter.setBounds(209, 171, 126, 17);
                                }
                                {
                                    jButtonViewTreeProof = new JButton();
                                    jPanelInformationWriter.add(jButtonViewTreeProof);
                                    jButtonViewTreeProof.setText("View tree");
                                    jButtonViewTreeProof.setBounds(125, 229, 115, 28);
                                    jButtonViewTreeProof.setEnabled(false);
                                    jButtonViewTreeProof.addActionListener(new ActionListener() {

                                        public void actionPerformed(ActionEvent evt) {
                                            jButtonViewTreeProofActionPerformed(evt);
                                        }
                                    });
                                }
                            }
                        }
                        {
                            jLabelLoadFileInformationToolTip = new JLabel();
                            jPanelZKSProverTab.add(jLabelLoadFileInformationToolTip);
                            jLabelLoadFileInformationToolTip.setText("ImageToolTip");
                            jLabelLoadFileInformationToolTip.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/information16.jpg")));
                            jLabelLoadFileInformationToolTip.setBounds(421, 52, 21, 19);
                            jLabelLoadFileInformationToolTip.setToolTipText("<html><body>" + "Load the zip file that contains the PK and SK xml files" + "</body></html>");
                        }
                    }
                    {
                        jPanelZKSVerifier = new JPanel();
                        tab.addTab("ZKS Verifier", null, jPanelZKSVerifier, null);
                        jPanelZKSVerifier.setLayout(null);
                        {
                            jButtonLoadPK = new JButton();
                            jPanelZKSVerifier.add(jButtonLoadPK);
                            jButtonLoadPK.setText("Load Public Key ...");
                            jButtonLoadPK.setBounds(49, 67, 150, 28);
                            jButtonLoadPK.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {
                                    jButtonLoadPKActionPerformed(evt);
                                }
                            });
                        }
                        {
                            jLabelLoadPKVerifierStatus = new JLabel();
                            jPanelZKSVerifier.add(jLabelLoadPKVerifierStatus);
                            jLabelLoadPKVerifierStatus.setBounds(291, 67, 385, 28);
                        }
                        {
                            jButtonLoadProofVerifier = new JButton();
                            jPanelZKSVerifier.add(jButtonLoadProofVerifier);
                            jButtonLoadProofVerifier.setText("Load Proof ...");
                            jButtonLoadProofVerifier.setBounds(49, 203, 150, 26);
                            jButtonLoadProofVerifier.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {
                                    jButtonLoadProofVerifierActionPerformed(evt);
                                }
                            });
                        }
                        {
                            jLabelLoadProofVerifierStatus = new JLabel();
                            jPanelZKSVerifier.add(jLabelLoadProofVerifierStatus);
                            jLabelLoadProofVerifierStatus.setBounds(291, 207, 411, 25);
                        }
                        {
                            jButtonVerifyVerifier = new JButton();
                            jPanelZKSVerifier.add(jButtonVerifyVerifier);
                            jButtonVerifyVerifier.setText("Verify");
                            jButtonVerifyVerifier.setBounds(329, 332, 113, 27);
                            jButtonVerifyVerifier.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {
                                    jButtonVerifyVerifierActionPerformed(evt);
                                }
                            });
                        }
                        {
                            jLabelVerifyVerifierStatus = new JLabel();
                            jPanelZKSVerifier.add(jLabelVerifyVerifierStatus);
                            jLabelVerifyVerifierStatus.setBounds(204, 391, 365, 28);
                        }
                        {
                            jLabelKeyVerifier = new JLabel();
                            jPanelZKSVerifier.add(jLabelKeyVerifier);
                            jLabelKeyVerifier.setText("Key");
                            jLabelKeyVerifier.setBounds(165, 272, 67, 23);
                        }
                        {
                            jTextKeyWriteVerifier = new JTextField();
                            jPanelZKSVerifier.add(jTextKeyWriteVerifier);
                            jTextKeyWriteVerifier.setText("key");
                            jTextKeyWriteVerifier.setBounds(291, 271, 240, 24);
                            jTextKeyWriteVerifier.addMouseListener(new MouseAdapter() {

                                public void mouseClicked(MouseEvent evt) {
                                    jTextKeyWriteVerifierMouseClicked(evt);
                                }
                            });
                        }
                        {
                            jButtonLoadCertVerifier = new JButton();
                            jPanelZKSVerifier.add(jButtonLoadCertVerifier);
                            jButtonLoadCertVerifier.setText("Load cert...");
                            jButtonLoadCertVerifier.setBounds(49, 138, 150, 29);
                            jButtonLoadCertVerifier.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent evt) {
                                    jButtonLoadCertVerifierActionPerformed(evt);
                                }
                            });
                        }
                        {
                            jLabelLoadCertStatusVerifier = new JLabel();
                            jPanelZKSVerifier.add(jLabelLoadCertStatusVerifier);
                            jLabelLoadCertStatusVerifier.setBounds(291, 138, 385, 25);
                        }
                        {
                            jLabelPKInformation = new JLabel();
                            jPanelZKSVerifier.add(jLabelPKInformation);
                            jLabelPKInformation.setText("ImageToolTip");
                            jLabelPKInformation.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/information16.jpg")));
                            jLabelPKInformation.setBounds(216, 71, 23, 20);
                            jLabelPKInformation.setToolTipText("<html><body>" + "Load the xml file PK.xml" + "</body></html>");
                        }
                        {
                            jLabelCertInformation = new JLabel();
                            jPanelZKSVerifier.add(jLabelCertInformation);
                            jLabelCertInformation.setText("ImageToolTip");
                            jLabelCertInformation.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/information16.jpg")));
                            jLabelCertInformation.setBounds(216, 145, 23, 17);
                            jLabelCertInformation.setToolTipText("<html><body>" + "Load the .cert file that contains certificate with the extension ZKS" + "</body></html>");
                        }
                        {
                            jLabelProofInformation = new JLabel();
                            jPanelZKSVerifier.add(jLabelProofInformation);
                            jLabelProofInformation.setText("ImageToolTip");
                            jLabelProofInformation.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/information16.jpg")));
                            jLabelProofInformation.setBounds(216, 208, 23, 21);
                            jLabelProofInformation.setToolTipText("<html><body>" + "Load the .xml file that contains the proof" + "</body></html>");
                        }
                    }
                }
            }
            pack();
            this.setSize(800, 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jButtonAddKeyValueActionPerformed(ActionEvent evt) {
        String key = jTextFieldKey.getText();
        Object value = jTextFieldValue.getText();
        if (hashTableData.containsKey(key)) {
            jLabelErrorData.setText("Error!!! Key just is into Database");
        } else {
            hashTableData.put(key, value);
            dataIntoJList.add(key + ":   " + value);
            jListData.setListData(dataIntoJList);
            jLabelErrorData.setText("");
            if (hashTableData.size() > 6) jListData.setPreferredSize(new java.awt.Dimension(121, 165 + (hashTableData.size() - 7) * 15));
            scroll.revalidate();
            scroll.repaint();
            if (!jButtonGenerateZKS.isEnabled()) jButtonGenerateZKS.setEnabled(true);
        }
    }

    private void jButtonCancelGenerateActionPerformed(ActionEvent evt) {
        treadCommitment.stop();
        jLabelGenerateStatus.setText("Process canceled.");
    }

    private void jButtonDeleteActionPerformed(ActionEvent evt) {
        String stringToDelete = (String) jListData.getSelectedValue();
        String keyString = stringToDelete.substring(0, stringToDelete.indexOf(":   "));
        hashTableData.remove(keyString);
        dataIntoJList.removeElement(stringToDelete);
        jListData.setListData(dataIntoJList);
        scroll.revalidate();
        scroll.repaint();
        jButtonDelete.setEnabled(false);
        logger.info("[jButtonDeleteActionPerformed] Hash table size " + hashTableData.size());
        if (hashTableData.size() == 0) jButtonGenerateZKS.setEnabled(false);
    }

    private void jButtonGenerateProofActionPerformed(ActionEvent evt) {
        String key = jTextFieldSearchKey.getText();
        String value = jTextFieldSearchValue.getText();
        logger.info(key);
        if (key.length() < 1 || value.length() < 1) jLabelResultProver.setText("Insert key and value, please!"); else {
            hashTableData.clear();
            hashTableData.put(key, value);
            logger.info("size hash table =" + hashTableData.size());
            ZeroKnowledgeSet zks = new ZeroKnowledgeSet(hashTableData, tree);
            if (zks.belong(key)) jLabelResultProver.setText("The key is into DB"); else jLabelResultProver.setText("The key is not into DB");
            piGreek = new PiGreek();
            piGreek = zks.getPiGreek();
            piGreek.saveToXML(TEMP_PATH + "/piGreek.xml", "UTF-8");
            jButtonSaveProof.setEnabled(true);
        }
    }

    private void jButtonGenerateZKSActionPerformed(ActionEvent evt) {
        jLabelGenerateStatus.setText("Generation starting. The process needs some minutes. Please wait!");
        jButtonCancelGenerate.setEnabled(true);
        treadCommitment = new Thread(new ThreadCommitment());
        treadCommitment.start();
    }

    private void jButtonLoadCertVerifierActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                FileInputStream in = new FileInputStream(fc.getSelectedFile().getAbsolutePath());
                Certificate c = cf.generateCertificate(in);
                in.close();
                X509Certificate t = (X509Certificate) c;
                ext = new String(t.getExtensionValue("1.2.31.42")).trim();
                jLabelLoadCertStatusVerifier.setText("Cert file loaded correctly");
            } catch (Exception e) {
                jLabelLoadCertStatusVerifier.setText("Error during load cert");
                e.printStackTrace();
            }
        } else {
            logger.info("Command cancelled by user.");
        }
    }

    private void jButtonLoadFilesIntoTabProverActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedOutputStream dest = null;
                BufferedInputStream bis = null;
                ZipEntry entry;
                ZipFile zipfile = new ZipFile(fc.getSelectedFile().getAbsolutePath());
                Enumeration e = zipfile.entries();
                while (e.hasMoreElements()) {
                    entry = (ZipEntry) e.nextElement();
                    bis = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(TEMP_PATH + "/" + entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = bis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    bis.close();
                    logger.info("Saved: " + TEMP_PATH + "/" + entry.getName());
                }
                root = RootMerkleNode.loadFromXML(TEMP_PATH + "/PK.xml");
                tree = LinkedMerkleTree.loadFromXML(TEMP_PATH + "/SK.xml");
                jLabelErrorIntoLoadTab.setText("Files loaded correctly");
                jLabelQValueWrite.setText(String.valueOf(tree.getQ()));
                jLabelHashingAlgorithmWriter.setText(root.getHashAlgo());
                jLabelLambdaValueProverWriter.setText(String.valueOf(tree.getLambda()));
                jLabelSeedProverWriter.setText(String.valueOf(tree.getBaseSeed()));
                jLabelTreeHeightProverWriter.setText(String.valueOf(tree.height()));
                jButtonProver.setEnabled(true);
                jButtonViewTreeProof.setEnabled(true);
            } catch (Exception e) {
                jLabelErrorIntoLoadTab.setText("Error during load files.");
                e.printStackTrace();
            }
        } else {
            logger.info("Command cancelled by user.");
        }
    }

    private void jButtonLoadFromFileActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            logger.info("Opening: " + file.getName());
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = input.readLine()) != null) {
                    int sep = line.indexOf(";");
                    String key = line.substring(0, sep);
                    String value = line.substring(sep + 1);
                    hashTableData.put(key, value);
                    dataIntoJList.add(key + ":   " + value);
                    jListData.setListData(dataIntoJList);
                    jLabelErrorData.setText("");
                    if (hashTableData.size() > 6) jListData.setPreferredSize(new java.awt.Dimension(121, 165 + (hashTableData.size() - 7) * 15));
                    scroll.revalidate();
                    scroll.repaint();
                    if (!jButtonGenerateZKS.isEnabled()) jButtonGenerateZKS.setEnabled(true);
                }
                input.close();
            } catch (IOException ex) {
                jLabelErrorData.setText("Error during process the file!");
            }
        } else {
            logger.info("Command cancelled by user.");
        }
    }

    private void jButtonLoadPKActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                xmlPKPath = fc.getSelectedFile().getAbsolutePath();
                root = RootMerkleNode.loadFromXML(xmlPKPath);
                jLabelLoadPKVerifierStatus.setText("Public Key loaded correctly");
            } catch (Exception e) {
                jLabelLoadPKVerifierStatus.setText("Error during load files.");
                e.printStackTrace();
            }
        } else {
            logger.info("Command cancelled by user.");
        }
    }

    private void jButtonLoadProofVerifierActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                piGreek = PiGreek.loadFromXML(fc.getSelectedFile().getAbsolutePath());
                jLabelLoadProofVerifierStatus.setText("Proof loaded correctly");
            } catch (Exception e) {
                jLabelLoadProofVerifierStatus.setText("Error during load files. Be sure that the selected is file the proof's xml file. ");
                e.printStackTrace();
            }
        } else {
            logger.info("Command cancelled by user.");
        }
    }

    private void jButtonSaveActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showSaveDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String pathToSave = fc.getSelectedFile().getAbsolutePath();
            logger.info("Saving: ");
            comm.saveTreeToXML(TEMP_PATH + "/SK.xml", "UTF-8");
            ((RootMerkleNode) comm.getTree().root().element()).saveToXML(TEMP_PATH + "/PK.xml", "UTF-8");
            try {
                FileOutputStream dest;
                logger.info(pathToSave.substring(pathToSave.length() - 4));
                if (pathToSave.substring(pathToSave.length() - 4).equals(".zip")) dest = new FileOutputStream(pathToSave); else dest = new FileOutputStream(pathToSave + "/ZKS.zip");
                BufferedInputStream origin = null;
                CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
                out.setComment("ZKS contains:\n" + " SK.xml - the xml file that contains the ZKS Secret Key (contains all the stored elements)\n" + " PK.xml - the xml file that contains the ZKS Public Key");
                byte data[] = new byte[BUFFER];
                String files[] = { TEMP_PATH + "/SK.xml", TEMP_PATH + "/PK.xml" };
                for (int i = 0; i < files.length; i++) {
                    logger.info("Adding: " + files[i]);
                    FileInputStream fi = new FileInputStream(files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    String tmp[] = files[i].split("/");
                    String nameEntry = tmp[tmp.length - 1];
                    ZipEntry entry = new ZipEntry(nameEntry);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                out.close();
                jLabelGenerateStatus.setText("Files saved");
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Attenzione! Selezionare un percorso valido!", "ERRORE", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Save command cancelled by user.");
        }
    }

    private void jButtonSaveProofActionPerformed(ActionEvent evt) {
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showSaveDialog(Operating.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String pathToSave = fc.getSelectedFile().getAbsolutePath();
            logger.info("Saving: ");
            try {
                BufferedInputStream origin = null;
                FileOutputStream dest;
                logger.info(pathToSave.substring(pathToSave.length() - 4));
                if (pathToSave.substring(pathToSave.length() - 4).equals(".zip")) dest = new FileOutputStream(pathToSave); else dest = new FileOutputStream(pathToSave + "/Proof.zip");
                CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
                out.setComment("Proof contains:\n" + " piGreek.xml - the xml file where the Prof of membership/non membership is stored\n");
                byte data[] = new byte[BUFFER];
                String files[] = { TEMP_PATH + "/piGreek.xml" };
                for (int i = 0; i < files.length; i++) {
                    logger.info("Adding: " + files[i]);
                    FileInputStream fi = new FileInputStream(files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    String nameEntry = files[i].substring(files[i].indexOf("/") + 1, files[i].length());
                    ZipEntry entry = new ZipEntry(nameEntry);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            jLabelSaveResult.setText("File Saved");
        } else {
            logger.info("Save command cancelled by user.");
        }
    }

    private void jButtonVerifyVerifierActionPerformed(ActionEvent evt) {
        String key = jTextKeyWriteVerifier.getText();
        try {
            if (ext == null) jLabelVerifyVerifierStatus.setText("Are you sure that all information are loaded!"); else {
                String[] extsplit = ext.split(":");
                String hashAlgo = extsplit[0].toLowerCase();
                String hash = extsplit[1];
                if (hashAlgo.indexOf("sha512") != -1) {
                    hashAlgo = "SHA-512";
                } else if (hashAlgo.indexOf("sha1") != -1) {
                    hashAlgo = "SHA-1";
                } else if (hashAlgo.indexOf("sha256") != -1) {
                    hashAlgo = "SHA-256";
                } else if (hashAlgo.indexOf("sha384") != -1) {
                    hashAlgo = "SHA-384";
                } else throw new NoSuchAlgorithmException();
                StringBuffer fileData = new StringBuffer(1000);
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(xmlPKPath));
                    char[] buf = new char[1024];
                    int numRead = 0;
                    while ((numRead = reader.read(buf)) != -1) {
                        fileData.append(buf, 0, numRead);
                    }
                    reader.close();
                    Utils ut = new Utils();
                    ut.setHashAlgo(hashAlgo);
                    String result = ut.getHexString(ut.getDigestValue(fileData.toString().getBytes()));
                    if (!result.equals(hash)) {
                        jLabelVerifyVerifierStatus.setText("The PK is not for this Certificate");
                        return;
                    }
                } catch (Exception e) {
                    jLabelLoadPKVerifierStatus.setText("Error during load PK information");
                }
                ZKSVerifier ver = new ZKSVerifier();
                Object result = ver.verifier(piGreek, key, root);
                if (result == null) jLabelVerifyVerifierStatus.setText("Proof correct! The key doesn't beelong to DataBase."); else jLabelVerifyVerifierStatus.setText("The key is into DB. The value is '" + ver.verifier(piGreek, key, root).toString() + "'");
            }
        } catch (FailedZKSVerifyException e) {
            jLabelVerifyVerifierStatus.setText("The key is not into DB");
            e.printStackTrace();
        } catch (KeyMismatchZKSVerifyException e) {
            jLabelVerifyVerifierStatus.setText("The key mismatch with the key into the proof.");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            jLabelVerifyVerifierStatus.setText("Hash algorithm not found.");
            e.printStackTrace();
        }
    }

    private void jButtonViewTreeGenerateActionPerformed(ActionEvent evt) {
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setTitle("Merkle Tree view");
        content.add(new MerkleTree2D(comm.getTree()));
        frame.pack();
        frame.setVisible(true);
    }

    private void jButtonViewTreeProofActionPerformed(ActionEvent evt) {
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setTitle("Merkle Tree view");
        content.add(new MerkleTree2D(tree));
        frame.pack();
        frame.setVisible(true);
    }

    private void jListDataValueChanged(ListSelectionEvent evt) {
        if (hashTableData.size() > 0 && !jButtonDelete.isEnabled()) jButtonDelete.setEnabled(true);
    }

    private void jTextFieldSearchKeyMouseClicked(MouseEvent evt) {
        jTextFieldSearchKey.setText("");
    }

    private void jTextFieldSearchValueMouseClicked(MouseEvent evt) {
        jTextFieldSearchValue.setText("");
        jTextFieldSearchKey.setText("");
    }

    private void jTextKeyWriteVerifierMouseClicked(MouseEvent evt) {
        jTextKeyWriteVerifier.setText("");
    }

    private void tabStateChanged(ChangeEvent evt) {
        hashTableData = new Hashtable<String, Object>();
        root = new RootMerkleNode();
        tree = new LinkedMerkleTree();
        dataIntoJList = new Vector<String>();
        comm = null;
        piGreek = new PiGreek();
    }
}
