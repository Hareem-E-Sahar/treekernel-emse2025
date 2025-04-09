package com.mockturtlesolutions.snifflib.statmodeltools.workbench;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import javax.swing.ImageIcon;
import javax.swing.AbstractCellEditor;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import com.mockturtlesolutions.snifflib.guitools.components.DomainNameEvent;
import com.mockturtlesolutions.snifflib.guitools.components.DomainNameListener;
import com.mockturtlesolutions.snifflib.datatypes.DblMatrix;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryStorageXML;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryConnectivity;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryConnectivity;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryStorage;
import com.mockturtlesolutions.snifflib.datatypes.DataSetReader;
import com.mockturtlesolutions.snifflib.datatypes.DataSet;
import com.mockturtlesolutions.snifflib.datatypes.DataSetPanel;
import com.mockturtlesolutions.snifflib.statmodeltools.database.*;
import com.mockturtlesolutions.snifflib.guitools.components.IconifiedDomainNameTextField;
import com.mockturtlesolutions.snifflib.guitools.components.DomainListCellRenderer;
import com.mockturtlesolutions.snifflib.guitools.components.DomainEntry;
import com.mockturtlesolutions.snifflib.guitools.components.IconServer;
import com.mockturtlesolutions.snifflib.invprobs.StatisticalModel;
import com.mockturtlesolutions.snifflib.datatypes.DblParamSet;
import com.mockturtlesolutions.snifflib.guitools.components.DomainNameTree;
import com.mockturtlesolutions.snifflib.guitools.components.ServedDomainNameNode;
import com.mockturtlesolutions.snifflib.guitools.components.NamedParameterNode;
import com.mockturtlesolutions.snifflib.guitools.components.DomainNameNode;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.ToolTipManager;
import java.util.HashSet;
import java.util.EventListener;
import javax.swing.SpringLayout;
import javax.swing.text.Document;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.DefaultCellEditor;
import java.net.URI;
import java.net.URL;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.JComponent;
import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.DefaultCellEditor;
import com.mockturtlesolutions.snifflib.invprobs.DblParamSetFrame;
import com.mockturtlesolutions.snifflib.invprobs.DblParamSetEditor;
import com.mockturtlesolutions.snifflib.invprobs.HierarchyTree;
import java.util.Collections;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JCheckBox;
import com.mockturtlesolutions.snifflib.reposconfig.graphical.ReposConfigFrame;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryListener;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryStorageTransferAgent;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryConnectionHandler;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryEvent;
import com.mockturtlesolutions.snifflib.guitools.components.PrefsConfigFrame;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.reflect.Constructor;
import javax.swing.border.*;
import javax.swing.BorderFactory;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;
import javax.swing.AbstractListModel;
import com.mockturtlesolutions.snifflib.reposconfig.graphical.DefaultMultiConfigNameSelectorGroup;
import java.util.EventObject;
import javax.swing.event.CellEditorListener;
import com.mockturtlesolutions.snifflib.reposconfig.database.ReposConfigurable;
import com.mockturtlesolutions.snifflib.reposconfig.database.GraphicalRepositoryEditor;

public class GLMStorageFrame extends JFrame implements GraphicalRepositoryEditor, GLMStorage {

    private ReposConfigFrame repositoryEditor;

    protected StatisticalModelConfig Config;

    protected RepositoryConnectionHandler connectionHandler;

    protected StatisticalModelStorageConnectivity Connection;

    private PrefsConfigFrame prefsEditor;

    private GLMStorage backingStorage;

    private IconifiedDomainNameTextField nicknameText;

    private JButton declareModelVariableButton;

    private JTextField declareModelVariableText;

    private JButton declareModelParameterButton;

    private JTextField declareModelParameterText;

    private JButton repositoryView;

    private JButton uploadButton;

    private JButton editPrefsButton;

    private JTextArea commentTextArea;

    private JTextField createdOnText;

    private JTextField createdByText;

    private JTable variableMappingsTable;

    private MappingTableModel mappingTableModel;

    private ParameterMappingTableModel paramMappingTableModel;

    private HierarchyTree parameterTree;

    private JList variableGrabber;

    private JButton addLeftHandSideButton;

    private JButton addRightHandSideButton;

    private JButton addModelClassTermButton;

    private JButton addNewModelClassTermButton;

    private JButton addAnalysisByTermButton;

    private JButton addRandomTermButton;

    private JButton addNewRandomTermButton;

    private JButton removeParameterButton;

    private JButton removeVariableMappingButton;

    private JButton editDataSetButton;

    private JTextField modelLeftHandSide;

    private JTextField modelRightHandSide;

    private JTable modelClassTerms;

    private JTextField analysisByTerms;

    private JTable randomTerms;

    private JButton removeRandomTermButton;

    private JButton removeClassTermButton;

    private JTextField statisticalModelClass;

    private JComboBox dataConfigSelector;

    private JButton dataReposView;

    private IconifiedDomainNameTextField dataSetText;

    private JRadioButton enabledRadio;

    private int lastselectedsourcerow;

    private JLabel waitIndicator;

    private GLMStorageTransferAgent transferAgent;

    private boolean NoCallbackChangeMode;

    private Vector reposListeners;

    private IconServer iconServer;

    private StatisticalModelToolsPrefs Prefs;

    private GLMStorageFindNameDialog findGLM;

    private DefaultMultiConfigNameSelectorGroup dataSetGroup;

    public GLMStorageFrame() {
        super("Specify Your GLM - Untitled");
        this.lastselectedsourcerow = -1;
        try {
            Class transferAgentClass = this.getStorageTransferAgentClass();
            if (transferAgentClass == null) {
                throw new RuntimeException("Transfer agent class can not be null.");
            }
            Class[] parameterTypes = new Class[] { RepositoryStorage.class };
            Constructor constr = transferAgentClass.getConstructor(parameterTypes);
            Object[] actualValues = new Object[] { this };
            this.transferAgent = (GLMStorageTransferAgent) constr.newInstance(actualValues);
        } catch (Exception err) {
            throw new RuntimeException("Unable to instantiate transfer agent.", err);
        }
        this.waitIndicator = new JLabel("X");
        this.waitIndicator.setHorizontalAlignment(JLabel.CENTER);
        this.waitIndicator.setPreferredSize(new Dimension(25, 25));
        this.waitIndicator.setMaximumSize(new Dimension(25, 25));
        this.waitIndicator.setMinimumSize(new Dimension(25, 25));
        this.NoCallbackChangeMode = false;
        this.setSize(new Dimension(1100, 650));
        this.setLayout(new GridLayout(1, 1));
        this.Config = new GLMStorageConfig();
        this.Config.initialize();
        this.connectionHandler = new RepositoryConnectionHandler(this.Config);
        this.Connection = (StatisticalModelStorageConnectivity) this.connectionHandler.getConnection("default");
        this.Prefs = new StatisticalModelToolsPrefs();
        this.Prefs.initialize();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formatted_date = formatter.format(new Date());
        this.createdOnText = new JTextField(formatted_date);
        this.createdByText = new JTextField(this.Prefs.getConfigValue("createdby"));
        this.reposListeners = new Vector();
        this.iconServer = new IconServer();
        this.iconServer.setConfigFile(this.Prefs.getConfigValue("default", "iconmapfile"));
        this.findGLM = new GLMStorageFindNameDialog(Config, iconServer);
        this.findGLM.setSearchClass(GLMStorage.class);
        this.nicknameText = new IconifiedDomainNameTextField(findGLM, this.iconServer);
        int stdButtonHeight = this.nicknameText.getButtonHeight();
        int setBoxHeight = this.nicknameText.getHeight();
        this.enabledRadio = new JRadioButton("Enabled:");
        this.enabledRadio.setSelected(true);
        this.enabledRadio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                if (!enabledRadio.isSelected()) {
                    int t = JOptionPane.showConfirmDialog(null, "Note, disabling a storage deprecates it and schedules it for deletion.  Disable this storage?", "Deprecate storage?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (t != JOptionPane.YES_OPTION) {
                        enabledRadio.setEnabled(false);
                        enabledRadio.setSelected(true);
                        enabledRadio.setEnabled(true);
                    }
                }
            }
        });
        this.editPrefsButton = new JButton("Preferences...");
        this.editPrefsButton.setPreferredSize(new Dimension(4 * stdButtonHeight, stdButtonHeight));
        this.editPrefsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                prefsEditor.setVisible(true);
            }
        });
        this.commentTextArea = new JTextArea(2, 16);
        this.commentTextArea.setToolTipText("A detailed (possibly formatted) description including guidance to future developers of this set.");
        this.commentTextArea.setText(" ");
        this.findGLM.addOkListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                String selectedSet = findGLM.getSelectedName();
                if (selectedSet != null) {
                    GLMStorage set = (GLMStorage) Connection.getStorage(selectedSet);
                    GLMStorageFrame.this.transferStorage(set);
                } else {
                }
            }
        });
        URL url = this.getClass().getResource("images/file_write_icon.png");
        ImageIcon icon = new ImageIcon(url);
        Image im = icon.getImage();
        icon.setImage(im.getScaledInstance(25, -1, Image.SCALE_SMOOTH));
        this.nicknameText.setPreferredSize(new Dimension(200, 25));
        this.nicknameText.setText(this.Prefs.getConfigValue("default", "domainname") + ".");
        this.nicknameText.setNameTextToolTipText("Right click to search the database.");
        String[] configs = new String[] { "com.mockturtlesolutions.snifflib.flatfiletools.database.FlatFileToolsConfig" };
        this.dataSetGroup = new DefaultMultiConfigNameSelectorGroup(configs);
        this.dataConfigSelector = (JComboBox) this.dataSetGroup.getConfigSelector();
        this.dataReposView = (JButton) this.dataSetGroup.getRepositorySelector();
        this.dataSetText = (IconifiedDomainNameTextField) this.dataSetGroup.getNicknameSelector();
        this.dataConfigSelector.setPreferredSize(new Dimension(100, stdButtonHeight));
        url = this.getClass().getResource("images/file_write_icon.png");
        icon = new ImageIcon(url);
        im = icon.getImage();
        icon.setImage(im.getScaledInstance(25, -1, Image.SCALE_SMOOTH));
        this.uploadButton = new JButton(icon);
        this.uploadButton.setPreferredSize(new Dimension(60, stdButtonHeight));
        this.uploadButton.setToolTipText("Uploads entire set configuration to repository.");
        this.uploadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                boolean do_transfer = false;
                try {
                    String expname = getNickname();
                    int split = expname.lastIndexOf('.');
                    String domain = "";
                    String name = "";
                    String usersdomain = Prefs.getConfigValue("default", "domainname");
                    if (split > 0) {
                        domain = expname.substring(0, split);
                        name = expname.substring(split + 1, expname.length());
                    } else {
                        name = expname;
                    }
                    name = name.trim();
                    if (name.equals("")) {
                        JOptionPane.showMessageDialog(null, "Cowardly refusing to upload with an empty buffer name...");
                        return;
                    }
                    if (!domain.equals(usersdomain)) {
                        int s = JOptionPane.showConfirmDialog(null, "If you are not the original author, you may wish to switch the current domain name " + domain + " to \nyour domain name " + usersdomain + ".  Would you like to do this?\n (If you'll be using this domain often, you may want to set it in your preferences.)", "Potential WWW name-space clash!", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (s == JOptionPane.YES_OPTION) {
                            setNickname(usersdomain + "." + name);
                            do_transfer = executeTransfer();
                        }
                        if (s == JOptionPane.NO_OPTION) {
                            do_transfer = executeTransfer();
                        }
                    } else {
                        do_transfer = executeTransfer();
                    }
                    setTitle("Specify your GLM - " + expname);
                } catch (Exception err) {
                    throw new RuntimeException("Problem uploading storage.", err);
                }
                setEditable(true);
            }
        });
        this.repositoryView = new JButton("default");
        this.repositoryView.setPreferredSize(new Dimension(3 * stdButtonHeight, stdButtonHeight));
        this.repositoryView.addActionListener(new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                setRepository(repositoryView.getText());
                repositoryEditor.setVisible(true);
            }
        });
        this.prefsEditor = new PrefsConfigFrame(this.Prefs);
        this.prefsEditor.setVisible(false);
        this.prefsEditor.addCloseListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                prefsEditor.setVisible(false);
            }
        });
        this.prefsEditor.addSelectListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                prefsEditor.setVisible(false);
            }
        });
        this.repositoryEditor = new ReposConfigFrame(this.Config);
        this.repositoryEditor.addSelectListener(new SelectListener());
        this.repositoryEditor.addCloseListener(new CloseListener());
        this.mappingTableModel = new MappingTableModel();
        this.variableMappingsTable = new JTable(this.mappingTableModel);
        TableColumn col = this.variableMappingsTable.getColumnModel().getColumn(1);
        col.setCellEditor(new MappingTableCellEditor());
        Vector varnames = new Vector();
        varnames.add("Height");
        varnames.add("Weight");
        varnames.add("Length");
        varnames.add("Lattitude");
        Collections.sort(varnames);
        this.variableGrabber = new JList(new VariableListModel());
        this.modelLeftHandSide = new JTextField("");
        this.modelRightHandSide = new JTextField("");
        this.modelClassTerms = new JTable(new ClassTermsTableModel());
        this.analysisByTerms = new JTextField("");
        this.randomTerms = new JTable(new RandomTermsTableModel());
        this.addLeftHandSideButton = new JButton("->");
        this.addRightHandSideButton = new JButton("->");
        this.addModelClassTermButton = new JButton("->");
        this.addAnalysisByTermButton = new JButton("->");
        this.addRandomTermButton = new JButton("->");
        this.removeRandomTermButton = new JButton("Remove");
        this.removeClassTermButton = new JButton("Remove");
        this.addNewModelClassTermButton = new JButton("New");
        this.addNewRandomTermButton = new JButton("New");
        this.removeParameterButton = new JButton("Remove");
        this.removeParameterButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                TreePath[] paths = parameterTree.getSelectionPaths();
                for (int i = 0; i < paths.length; i++) {
                    TreePath p = paths[i];
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) p.getLastPathComponent();
                    if (selectedNode != null) {
                        DomainNameNode uo = (DomainNameNode) selectedNode.getUserObject();
                        String paramName = uo.getDomain();
                        parameterTree.removeDomainNameNode(paramName);
                    }
                }
            }
        });
        this.removeRandomTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                int row = randomTerms.getSelectedRow();
                RandomTermsTableModel model = (RandomTermsTableModel) randomTerms.getModel();
                if (row >= 0) {
                    model.removeRow(row);
                }
            }
        });
        this.removeClassTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                int row = modelClassTerms.getSelectedRow();
                ClassTermsTableModel model = (ClassTermsTableModel) modelClassTerms.getModel();
                if (row >= 0) {
                    model.removeRow(row);
                }
            }
        });
        this.addLeftHandSideButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                String currText = modelLeftHandSide.getText();
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                modelLeftHandSide.setText(currText);
            }
        });
        this.addRightHandSideButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                String currText = modelRightHandSide.getText();
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                modelRightHandSide.setText(currText);
            }
        });
        this.addNewModelClassTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                int row = -1;
                String currText = "";
                ClassTermsTableModel model = (ClassTermsTableModel) modelClassTerms.getModel();
                if (row < 0) {
                    model.addRow(currText);
                    row = model.getRowCount() - 1;
                }
                currText = (String) model.getValueAt(row, 0);
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                model.setValueAt(currText, row, 0);
            }
        });
        this.addModelClassTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                int row = modelClassTerms.getSelectedRow();
                String currText = "";
                ClassTermsTableModel model = (ClassTermsTableModel) modelClassTerms.getModel();
                if (row < 0) {
                    model.addRow(currText);
                    row = model.getRowCount() - 1;
                }
                currText = (String) model.getValueAt(row, 0);
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                model.setValueAt(currText, row, 0);
            }
        });
        this.addAnalysisByTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                String currText = analysisByTerms.getText();
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                analysisByTerms.setText(currText);
            }
        });
        this.addNewRandomTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                int row = -1;
                String currText = "";
                RandomTermsTableModel model = (RandomTermsTableModel) randomTerms.getModel();
                if (row < 0) {
                    model.addRow(currText);
                    row = model.getRowCount() - 1;
                }
                currText = (String) model.getValueAt(row, 0);
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                model.setValueAt(currText, row, 0);
            }
        });
        this.addRandomTermButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Object[] vars = variableGrabber.getSelectedValues();
                if (vars.length <= 0) {
                    return;
                }
                int row = randomTerms.getSelectedRow();
                String currText = "";
                RandomTermsTableModel model = (RandomTermsTableModel) randomTerms.getModel();
                if (row < 0) {
                    model.addRow(currText);
                    row = model.getRowCount() - 1;
                }
                currText = (String) model.getValueAt(row, 0);
                currText = currText.trim();
                if (currText.length() == 0) {
                    currText = (String) vars[0];
                } else {
                    currText = currText + vars[0];
                }
                for (int j = 1; j < vars.length; j++) {
                    currText = currText + vars[j];
                }
                model.setValueAt(currText, row, 0);
            }
        });
        JPanel setBox = new JPanel();
        setBox.setBackground(Color.gray);
        SpringLayout layout = new SpringLayout();
        setBox.setLayout(layout);
        setBox.add(this.editPrefsButton);
        Box jointBox1 = Box.createHorizontalBox();
        JLabel label = new JLabel("Created On:");
        jointBox1.add(label);
        JScrollPane js1 = new JScrollPane(this.createdOnText);
        js1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        js1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        js1.setPreferredSize(new Dimension(100, 50));
        js1.setMaximumSize(new Dimension(100, 50));
        js1.setMinimumSize(new Dimension(100, 50));
        jointBox1.add(js1);
        setBox.add(jointBox1);
        Box jointBox2 = Box.createHorizontalBox();
        label = new JLabel("Created By:");
        jointBox2.add(label);
        JScrollPane js2 = new JScrollPane(this.createdByText);
        js2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        js2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        js2.setPreferredSize(new Dimension(100, 50));
        js2.setMaximumSize(new Dimension(100, 50));
        js2.setMinimumSize(new Dimension(100, 50));
        jointBox2.add(js2);
        setBox.add(jointBox2);
        setBox.add(this.uploadButton);
        setBox.add(this.repositoryView);
        setBox.add(this.nicknameText);
        setBox.add(this.enabledRadio);
        JPanel rightBorder = new JPanel();
        setBox.add(rightBorder);
        layout.putConstraint(SpringLayout.EAST, rightBorder, 0, SpringLayout.EAST, setBox);
        layout.putConstraint(SpringLayout.WEST, this.editPrefsButton, 0, SpringLayout.WEST, setBox);
        layout.putConstraint(SpringLayout.NORTH, this.editPrefsButton, 0, SpringLayout.NORTH, setBox);
        layout.putConstraint(SpringLayout.EAST, this.editPrefsButton, 0, SpringLayout.WEST, jointBox1);
        layout.putConstraint(SpringLayout.EAST, jointBox1, 0, SpringLayout.WEST, jointBox2);
        layout.putConstraint(SpringLayout.EAST, jointBox2, 0, SpringLayout.WEST, this.uploadButton);
        layout.putConstraint(SpringLayout.EAST, this.uploadButton, 0, SpringLayout.WEST, this.repositoryView);
        layout.putConstraint(SpringLayout.EAST, this.repositoryView, 0, SpringLayout.WEST, this.nicknameText);
        layout.putConstraint(SpringLayout.EAST, this.nicknameText, 0, SpringLayout.WEST, this.enabledRadio);
        layout.putConstraint(SpringLayout.EAST, this.enabledRadio, 0, SpringLayout.WEST, rightBorder);
        layout.putConstraint(SpringLayout.SOUTH, this.enabledRadio, 0, SpringLayout.SOUTH, this.nicknameText);
        layout.putConstraint(SpringLayout.NORTH, this.enabledRadio, 0, SpringLayout.NORTH, this.nicknameText);
        layout.putConstraint(SpringLayout.SOUTH, setBox, 0, SpringLayout.SOUTH, this.nicknameText);
        Box SETBOX = Box.createHorizontalBox();
        SETBOX.add(setBox);
        Box declareBox = Box.createHorizontalBox();
        this.declareModelVariableButton = new JButton("New Variable:");
        this.declareModelVariableButton.setPreferredSize(new Dimension(150, 50));
        this.declareModelVariableButton.setMaximumSize(new Dimension(150, 50));
        this.declareModelVariableButton.setMinimumSize(new Dimension(150, 50));
        this.declareModelVariableText = new JTextField("");
        this.declareModelVariableButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                String var = declareModelVariableText.getText();
                VariableListModel mod = (VariableListModel) (variableGrabber.getModel());
                mod.add(var);
            }
        });
        this.declareModelParameterButton = new JButton("New Parameter:");
        this.declareModelParameterButton.setPreferredSize(new Dimension(150, 50));
        this.declareModelParameterButton.setMaximumSize(new Dimension(150, 50));
        this.declareModelParameterButton.setMinimumSize(new Dimension(150, 50));
        this.declareModelParameterText = new JTextField("");
        this.declareModelParameterButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                String var = declareModelParameterText.getText();
                NamedParameterNode newnode = new NamedParameterNode(var, new DblMatrix(1.0));
                parameterTree.insertDomainNameNode(newnode);
            }
        });
        JScrollPane jsp3 = new JScrollPane(this.declareModelVariableText);
        jsp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp3.setPreferredSize(new Dimension(100, 50));
        jsp3.setMaximumSize(new Dimension(100, 50));
        jsp3.setMinimumSize(new Dimension(100, 50));
        declareBox.add(this.declareModelVariableButton);
        declareBox.add(jsp3);
        Box declareParamBox = Box.createHorizontalBox();
        JScrollPane jsp10 = new JScrollPane(this.declareModelParameterText);
        jsp10.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp10.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp10.setPreferredSize(new Dimension(100, 50));
        jsp10.setMaximumSize(new Dimension(100, 50));
        jsp10.setMinimumSize(new Dimension(100, 50));
        declareParamBox.add(this.declareModelParameterButton);
        declareParamBox.add(jsp10);
        declareParamBox.add(this.removeParameterButton);
        Box mainbox = Box.createVerticalBox();
        Box varMapBox = Box.createHorizontalBox();
        JScrollPane jsp = new JScrollPane(this.variableMappingsTable);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setPreferredSize(new Dimension(200, 100));
        jsp.setMaximumSize(new Dimension(200, 100));
        jsp.setMinimumSize(new Dimension(200, 100));
        Box varbox = Box.createVerticalBox();
        varbox.add(new JLabel("Variables:"));
        JScrollPane jsp2 = new JScrollPane(this.variableGrabber);
        jsp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp2.setPreferredSize(new Dimension(300, 100));
        jsp2.setMaximumSize(new Dimension(300, 100));
        jsp2.setMinimumSize(new Dimension(300, 100));
        varbox.add(jsp2);
        JButton mapButton = new JButton("Map");
        mapButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
                VariableListModel mod = (VariableListModel) (variableGrabber.getModel());
                String var = (String) variableGrabber.getSelectedValue();
                if (var != null) {
                    model.addRow(var);
                }
                System.out.println("Just added a row to model");
            }
        });
        JButton removeVariableButton = new JButton("Remove Variable");
        removeVariableButton.setPreferredSize(new Dimension(150, 50));
        removeVariableButton.setMaximumSize(new Dimension(150, 50));
        removeVariableButton.setMinimumSize(new Dimension(150, 50));
        removeVariableButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                VariableListModel mod = (VariableListModel) (variableGrabber.getModel());
                String var = (String) variableGrabber.getSelectedValue();
                if (var != null) {
                    mod.remove(var);
                    MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
                    model.removeMappingsFor(var);
                }
            }
        });
        mapButton.setPreferredSize(new Dimension(100, 50));
        mapButton.setMaximumSize(new Dimension(100, 50));
        mapButton.setMinimumSize(new Dimension(100, 50));
        Box variableControlBox = Box.createHorizontalBox();
        variableControlBox.add(declareBox);
        variableControlBox.add(removeVariableButton);
        varbox.add(variableControlBox);
        varMapBox.add(varbox);
        this.removeVariableMappingButton = new JButton("Remove");
        this.removeVariableMappingButton.setPreferredSize(new Dimension(100, 50));
        this.removeVariableMappingButton.setMaximumSize(new Dimension(100, 50));
        this.removeVariableMappingButton.setMinimumSize(new Dimension(100, 50));
        this.removeVariableMappingButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
                int selectedrow = variableMappingsTable.getSelectedRow();
                if (selectedrow >= 0) {
                    model.removeRow(selectedrow);
                }
                System.out.println("Just removed a row from model");
            }
        });
        Box mapControl = Box.createHorizontalBox();
        Box tableBox = Box.createVerticalBox();
        tableBox.add(new JLabel("Variable Mappings:"));
        tableBox.add(jsp);
        mapControl.add(mapButton);
        mapControl.add(this.removeVariableMappingButton);
        tableBox.add(mapControl);
        varMapBox.add(tableBox);
        varMapBox.add(Box.createHorizontalGlue());
        Box glmbox = Box.createHorizontalBox();
        glmbox.add(new JLabel("Model: "));
        Box addbox = Box.createVerticalBox();
        JScrollPane jsp5 = new JScrollPane(this.modelLeftHandSide);
        jsp5.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp5.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp5.setPreferredSize(new Dimension(100, 50));
        jsp5.setMaximumSize(new Dimension(100, 50));
        jsp5.setMinimumSize(new Dimension(100, 50));
        addbox.add(jsp5);
        addbox.add(this.addLeftHandSideButton);
        glmbox.add(addbox);
        url = this.getClass().getResource("images/equals_icon.png");
        icon = new ImageIcon(url);
        im = icon.getImage();
        icon.setImage(im.getScaledInstance(25, -1, Image.SCALE_SMOOTH));
        JLabel eqLabel = new JLabel(icon);
        eqLabel.setPreferredSize(new Dimension(50, 50));
        eqLabel.setMaximumSize(new Dimension(50, 50));
        eqLabel.setMinimumSize(new Dimension(50, 50));
        glmbox.add(eqLabel);
        addbox = Box.createVerticalBox();
        JScrollPane jsp6 = new JScrollPane(this.modelRightHandSide);
        jsp6.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp6.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp6.setPreferredSize(new Dimension(300, 50));
        jsp6.setMaximumSize(new Dimension(300, 50));
        jsp6.setMinimumSize(new Dimension(300, 50));
        addbox.add(jsp6);
        addbox.add(this.addRightHandSideButton);
        glmbox.add(addbox);
        Box factorsbox = Box.createHorizontalBox();
        addbox = Box.createVerticalBox();
        addbox.add(new JLabel("Analysis By:"));
        JScrollPane jsp7 = new JScrollPane(this.analysisByTerms);
        jsp7.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp7.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsp7.setPreferredSize(new Dimension(200, 50));
        jsp7.setMaximumSize(new Dimension(200, 50));
        jsp7.setMinimumSize(new Dimension(200, 50));
        addbox.add(jsp7);
        addbox.add(this.addAnalysisByTermButton);
        factorsbox.add(addbox);
        addbox = Box.createVerticalBox();
        addbox.add(new JLabel("Class Terms:"));
        JScrollPane jsp8 = new JScrollPane(this.modelClassTerms);
        jsp8.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp8.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp8.setPreferredSize(new Dimension(200, 200));
        jsp8.setMaximumSize(new Dimension(200, 200));
        jsp8.setMinimumSize(new Dimension(200, 200));
        addbox.add(jsp8);
        Box ccontBox = Box.createHorizontalBox();
        ccontBox.add(this.addModelClassTermButton);
        ccontBox.add(this.addNewModelClassTermButton);
        ccontBox.add(this.removeClassTermButton);
        addbox.add(ccontBox);
        factorsbox.add(addbox);
        addbox = Box.createVerticalBox();
        addbox.add(new JLabel("Random Terms:"));
        JScrollPane jsp9 = new JScrollPane(this.randomTerms);
        jsp9.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp9.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp9.setPreferredSize(new Dimension(200, 200));
        jsp9.setMaximumSize(new Dimension(200, 200));
        jsp9.setMinimumSize(new Dimension(200, 200));
        addbox.add(jsp9);
        Box ccontBox2 = Box.createHorizontalBox();
        ccontBox2.add(this.addRandomTermButton);
        ccontBox2.add(this.addNewRandomTermButton);
        ccontBox2.add(this.removeRandomTermButton);
        addbox.add(ccontBox2);
        factorsbox.add(addbox);
        Box databox = Box.createHorizontalBox();
        databox.add(new JLabel("Data Set:"));
        this.dataConfigSelector.setEditable(true);
        this.dataConfigSelector.setToolTipText((String) this.dataConfigSelector.getSelectedItem());
        this.editDataSetButton = new JButton("Edit");
        this.editDataSetButton.setToolTipText("Edit the data storage.");
        this.editDataSetButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                Component edframe = dataSetGroup.getGraphicalEditor();
                if (edframe == null) {
                    return;
                } else {
                    if (edframe instanceof Frame) {
                        edframe.setVisible(true);
                    } else if (edframe instanceof Container) {
                        JFrame out = new JFrame();
                        out.add(edframe);
                        edframe.setVisible(true);
                        out.setVisible(true);
                    } else {
                        JFrame out = new JFrame();
                        out.setLayout(new GridLayout(1, 1));
                        JPanel panel = new JPanel();
                        panel.add(edframe);
                        edframe.setVisible(true);
                        out.add(panel);
                        out.setVisible(true);
                    }
                }
            }
        });
        databox.add(this.dataConfigSelector);
        databox.add(this.dataReposView);
        databox.add(this.dataSetText);
        databox.add(this.editDataSetButton);
        mainbox.add(databox);
        mainbox.add(varMapBox);
        mainbox.add(Box.createVerticalStrut(5));
        mainbox.add(Box.createVerticalGlue());
        mainbox.add(Box.createHorizontalGlue());
        mainbox.add(glmbox);
        mainbox.add(factorsbox);
        DomainNameTree tree = new DomainNameTree();
        if (tree.getTree() == null) {
            System.out.println("The gotten tree is null!" + tree.getTree());
        }
        this.parameterTree = new HierarchyTree(tree.getTree());
        ToolTipManager.sharedInstance().registerComponent(this.parameterTree);
        Box paramBox = Box.createVerticalBox();
        JScrollPane jsp11 = new JScrollPane(this.parameterTree);
        jsp11.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp11.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp11.setPreferredSize(new Dimension(200, 300));
        jsp11.setMaximumSize(new Dimension(200, 300));
        jsp11.setMinimumSize(new Dimension(200, 300));
        paramBox.add(new JLabel("Model Parameters"));
        paramBox.add(jsp11);
        paramBox.add(declareParamBox);
        Box OUTBOX = Box.createHorizontalBox();
        OUTBOX.add(mainbox);
        OUTBOX.add(paramBox);
        Box TOPBOX = Box.createVerticalBox();
        TOPBOX.add(SETBOX);
        Box commentBox = Box.createVerticalBox();
        commentBox.add(new JLabel("Comment:"));
        commentBox.add(new JScrollPane(this.commentTextArea));
        TOPBOX.add(commentBox);
        TOPBOX.add(OUTBOX);
        this.add(TOPBOX);
        String last_repos = Prefs.getConfigValue("default", "lastrepository").trim();
        if (this.Config.hasRepository(last_repos)) {
            this.setRepository(last_repos);
        }
    }

    public void fireRepositoryChanged(RepositoryEvent ev) {
        for (int j = 0; j < this.reposListeners.size(); j++) {
            RepositoryListener l = (RepositoryListener) this.reposListeners.get(j);
            l.setRepository(ev.getRepository());
        }
    }

    public Class getDefaultGraphicalEditorClass() {
        return (GLMStorageFrame.class);
    }

    public void beforeCopyStorage() {
    }

    public void afterCopyStorage() {
    }

    public void copyStorageCommands(RepositoryStorage x) {
        this.transferAgent.copyStorageCommands(x);
    }

    public void copyStorage(RepositoryStorage x) {
        this.transferAgent.copyStorage(x);
    }

    public void beforeTransferStorage() {
    }

    public void afterTransferStorage() {
    }

    public void transferStorageCommands(RepositoryStorage x) {
        this.transferAgent.transferStorageCommands(x);
    }

    public void setRepository(String repos) {
        try {
            this.repositoryView.setText(repos);
            this.Connection = (StatisticalModelStorageConnectivity) this.connectionHandler.getConnection(repos);
            this.findGLM.restrictToRepository(repos);
            this.repositoryEditor.setCurrentRepository(repos);
            Prefs.setConfigValue("default", "lastrepository", repositoryView.getText());
            Prefs.saveConfig();
            for (int j = 0; j < this.reposListeners.size(); j++) {
                RepositoryListener l = (RepositoryListener) this.reposListeners.get(j);
                l.setRepository(repos);
            }
        } catch (Exception err) {
            String msg = err.getMessage();
            JOptionPane.showMessageDialog(null, msg + "\n" + "Unable to connect to repository " + repos + ".  Maintaining connection to 'default' repository instead.");
            setRepository("default");
        }
    }

    public String getRepository() {
        return (this.repositoryView.getText());
    }

    private class SelectListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String newrepos = repositoryEditor.getCurrentRepository();
            setRepository(newrepos);
            Prefs.setConfigValue("default", "lastrepository", repositoryView.getText());
            Prefs.saveConfig();
            repositoryEditor.setVisible(false);
        }
    }

    private class CloseListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            repositoryEditor.setVisible(false);
        }
    }

    public void setEditable(boolean b) {
        this.nicknameText.setEditable(b);
        this.createdByText.setEditable(b);
        this.createdOnText.setEditable(b);
        this.commentTextArea.setEditable(b);
        this.uploadButton.setEnabled(b);
        this.repositoryView.setEnabled(b);
        this.enabledRadio.setEnabled(b);
        String title = this.getTitle();
        int k = title.lastIndexOf("(Read only)");
        if (k == -1) {
            if (b == false) {
                title = title + "(Read only)";
            }
        } else {
            if (b == true) {
                title = title.substring(0, k);
            }
        }
        this.setTitle(title);
    }

    public void setComment(String cmt) {
        this.commentTextArea.setText(cmt);
    }

    public String getComment() {
        return (this.commentTextArea.getText());
    }

    public void setCreatedOn(String on) {
        this.createdOnText.setText(on);
    }

    public String getCreatedOn() {
        return (this.createdOnText.getText());
    }

    public void setCreatedBy(String on) {
        this.createdByText.setText(on);
    }

    public String getCreatedBy() {
        return (this.createdByText.getText());
    }

    public void setNickname(String name) {
        this.nicknameText.setText(name);
    }

    public String getNickname() {
        return (this.nicknameText.getText());
    }

    public void setEnabled(String n) {
        boolean isenabled = false;
        if (n == null) {
            throw new IllegalArgumentException("Enabled value can not be null.");
        }
        if ((n.equals("1")) || (n.equals("true"))) {
            isenabled = true;
        }
        this.enabledRadio.setSelected(isenabled);
    }

    public String getEnabled() {
        String out = null;
        if (this.enabledRadio.isSelected()) {
            out = "true";
        } else {
            out = "false";
        }
        return (out);
    }

    public void removeRepositoryListener(RepositoryListener l) {
        this.reposListeners.remove(l);
    }

    public void addRepositoryListener(RepositoryListener l) {
        this.reposListeners.add(l);
    }

    public void transferStorage(RepositoryStorage that) {
        this.transferAgent.transferStorage(that);
    }

    public Class getDOMStorageClass() {
        return (GLMStorageDOM.class);
    }

    public boolean executeTransfer() {
        boolean out = false;
        this.setEditable(false);
        String repos = this.repositoryView.getText();
        String setName = this.getNickname();
        if (!this.Connection.storageExists(setName)) {
            boolean success = this.Connection.createStorage(GLMStorage.class, setName);
            if (!success) {
                throw new RuntimeException("Failed to create storage of " + GLMStorageXML.class + " named " + setName + ".");
            }
        } else {
            Object[] options = { "Ok", "Cancel" };
            int n = JOptionPane.showOptionDialog(GLMStorageFrame.this, "Overwrite the existing definition " + setName + " in repository " + repos + "?", "Previously defined storage", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            String ans = (String) options[n];
            if (ans.equalsIgnoreCase("Cancel")) {
                return (out);
            }
        }
        this.backingStorage = (GLMStorage) this.Connection.getStorage(setName);
        if (this.backingStorage == null) {
            try {
                boolean success = this.Connection.createStorage(GLMStorage.class, setName);
                if (success) {
                    this.backingStorage = (GLMStorage) this.Connection.getStorage(setName);
                }
            } catch (Exception err) {
                throw new RuntimeException("Unable to retrieve storage " + setName + " from repository " + repos + ".", err);
            }
        }
        if (this.backingStorage == null) {
            throw new RuntimeException("Retrieved storage is null.");
        }
        this.backingStorage.transferStorage(this);
        this.setEditable(true);
        out = true;
        return (out);
    }

    public class VariableListModel extends AbstractListModel {

        private Vector list;

        private Vector listdatalisteners;

        private Vector listlisteners;

        public VariableListModel() {
            this.list = new Vector();
            this.listdatalisteners = new Vector();
        }

        public void add(Object X) {
            if (!this.list.contains(X)) {
                this.list.add(X);
                Collections.sort(this.list);
                this.fireContentsChanged(this, 0, this.getSize() - 1);
            }
        }

        public void remove(Object X) {
            if (this.list.contains(X)) {
                this.list.remove(X);
                Collections.sort(this.list);
                this.fireContentsChanged(this, 0, this.getSize() - 1);
            }
        }

        public void fireContentsChanged(Object src, int index, int index1) {
            ListDataEvent ev = new ListDataEvent(src, ListDataEvent.CONTENTS_CHANGED, index, index1);
            for (int i = 0; i < this.listdatalisteners.size(); i++) {
                ListDataListener l = (ListDataListener) this.listdatalisteners.get(i);
                l.contentsChanged(ev);
            }
        }

        public void fireIntervalAdded(Object src, int index, int index1) {
            ListDataEvent ev = new ListDataEvent(src, ListDataEvent.INTERVAL_ADDED, index, index1);
            for (int i = 0; i < this.listdatalisteners.size(); i++) {
                ListDataListener l = (ListDataListener) this.listdatalisteners.get(i);
                l.intervalAdded(ev);
            }
        }

        public void fireIntervalRemoved(Object src, int index, int index1) {
            ListDataEvent ev = new ListDataEvent(src, ListDataEvent.INTERVAL_REMOVED, index, index1);
            for (int i = 0; i < this.listdatalisteners.size(); i++) {
                ListDataListener l = (ListDataListener) this.listdatalisteners.get(i);
                l.intervalRemoved(ev);
            }
        }

        public void addListDataListener(ListDataListener l) {
            this.listdatalisteners.add(l);
        }

        public ListDataListener[] getListDataListeners() {
            ListDataListener[] out = new ListDataListener[this.listdatalisteners.size()];
            for (int i = 0; i < this.listdatalisteners.size(); i++) {
                out[i] = (ListDataListener) this.listdatalisteners.get(i);
            }
            return (out);
        }

        public EventListener[] getListeners(Class listenclass) {
            return (null);
        }

        public Object getElementAt(int n) {
            return (list.get(n));
        }

        public int getSize() {
            return (this.list.size());
        }
    }

    public class MappingTableCellEditor implements TableCellEditor {

        protected Vector listeners;

        private JComboBox component;

        public MappingTableCellEditor() {
            this.listeners = new Vector();
            this.component = new JComboBox();
            this.component.setEditable(true);
            this.component.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ev) {
                    System.out.println("Current selection is:" + component.getSelectedItem());
                    stopCellEditing();
                }
            });
        }

        public boolean stopCellEditing() {
            this.fireEditingStopped();
            return (true);
        }

        public void cancelCellEditing() {
            this.fireEditingCanceled();
        }

        public boolean shouldSelectCell(EventObject eo) {
            return (true);
        }

        protected void fireEditingCanceled() {
            System.out.println("Editing cancelled");
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = this.listeners.size() - 1; i >= 0; i--) {
                ((CellEditorListener) listeners.elementAt(i)).editingCanceled(ce);
            }
        }

        protected void fireEditingStopped() {
            System.out.println("Editing stopped");
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = this.listeners.size() - 1; i >= 0; i--) {
                System.out.println("    Calling a listener...");
                ((CellEditorListener) listeners.elementAt(i)).editingStopped(ce);
            }
        }

        public boolean isCellEditable(EventObject eo) {
            return (true);
        }

        public void removeCellEditorListener(CellEditorListener cl) {
            this.listeners.remove(cl);
        }

        public void addCellEditorListener(CellEditorListener cl) {
            System.out.println("A CellEditorListener was added!!");
            this.listeners.add(cl);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
            System.out.println("Setting the selected item to:" + value);
            ((JComboBox) this.component).setSelectedItem((String) value);
            return (this.component);
        }

        public Object getCellEditorValue() {
            System.out.println("Returning selected item:" + this.component.getSelectedItem());
            String newval = (String) this.component.getSelectedItem();
            if (newval == null) {
                newval = "";
            }
            return (newval);
        }
    }

    public class ClassTermsTableModel extends AbstractTableModel {

        private Vector terms;

        public ClassTermsTableModel() {
            super();
            this.terms = new Vector();
        }

        public void removeRow(int j) {
            this.terms.removeElementAt(j);
            fireTableRowsDeleted(j, j);
        }

        public void removeRow(String term) {
            int r = this.terms.indexOf(term);
            if (r >= 0) {
                this.terms.remove(r);
                fireTableRowsDeleted(r, r);
            }
        }

        public void addRow(String varname) {
            this.terms.add(varname);
            fireTableRowsInserted(this.terms.size() - 1, this.terms.size() - 1);
        }

        public void addRow(String varname, int j) {
            this.terms.insertElementAt(varname, j);
            fireTableRowsInserted(j, j);
        }

        public Class getColumnClass(int colindex) {
            return (String.class);
        }

        public int getColumnCount() {
            return (1);
        }

        public String getColumnName(int col) {
            String out = null;
            if (col == 0) {
                out = "Class Terms";
            } else {
                throw new RuntimeException("Invalid column request.");
            }
            return (out);
        }

        public int getRowCount() {
            return (this.terms.size());
        }

        public Object getValueAt(int r, int c) {
            return (this.terms.get(r));
        }

        public void setValueAt(Object v, int r, int c) {
            System.out.println("Setting value at r=" + r + " to " + v);
            if (r < this.getRowCount()) {
                this.terms.setElementAt(v, r);
            } else {
                throw new RuntimeException("Index out of range.");
            }
            fireTableCellUpdated(r, c);
        }

        public boolean isCellEditable(int r, int c) {
            boolean out = true;
            return (out);
        }
    }

    public class RandomTermsTableModel extends AbstractTableModel {

        private Vector terms;

        public RandomTermsTableModel() {
            super();
            this.terms = new Vector();
        }

        public void removeRow(int j) {
            this.terms.removeElementAt(j);
            fireTableRowsDeleted(j, j);
        }

        public void removeRow(String term) {
            int r = this.terms.indexOf(term);
            if (r >= 0) {
                this.terms.remove(r);
                fireTableRowsDeleted(r, r);
            }
        }

        public void addRow(String varname) {
            this.terms.add(varname);
            fireTableRowsInserted(this.terms.size() - 1, this.terms.size() - 1);
        }

        public void addRow(String varname, int j) {
            this.terms.insertElementAt(varname, j);
            fireTableRowsInserted(j, j);
        }

        public Class getColumnClass(int colindex) {
            return (String.class);
        }

        public int getColumnCount() {
            return (1);
        }

        public String getColumnName(int col) {
            String out = null;
            if (col == 0) {
                out = "Random Terms";
            } else {
                throw new RuntimeException("Invalid column request.");
            }
            return (out);
        }

        public int getRowCount() {
            return (this.terms.size());
        }

        public Object getValueAt(int r, int c) {
            return (this.terms.get(r));
        }

        public void setValueAt(Object v, int r, int c) {
            System.out.println("Setting value at r=" + r + " to " + v);
            if (r < this.getRowCount()) {
                this.terms.setElementAt(v, r);
            } else {
                throw new RuntimeException("Index out of range.");
            }
            fireTableCellUpdated(r, c);
        }

        public boolean isCellEditable(int r, int c) {
            boolean out = true;
            return (out);
        }
    }

    public class ParameterMappingTableModel extends AbstractTableModel {

        private Vector mappings;

        public ParameterMappingTableModel() {
            super();
            this.mappings = new Vector();
        }

        public int indexOf(String var) {
            int k = 0;
            while (k < mappings.size()) {
                String[] map = (String[]) mappings.get(k);
                if (map[0].equals(var)) {
                    break;
                } else {
                    k++;
                }
            }
            if (k >= mappings.size()) {
                k = -1;
            }
            return (k);
        }

        public void removeMappingsFor(String var) {
            int k = 0;
            while (k < mappings.size()) {
                String[] map = (String[]) mappings.get(k);
                if (map[0].equals(var)) {
                    this.removeRow(k);
                } else {
                    k++;
                }
            }
        }

        public void removeRow(int k) {
            mappings.removeElementAt(k);
            fireTableRowsDeleted(k, k);
        }

        public void addRow(String varname) {
            String[] map = new String[2];
            map[0] = varname;
            this.mappings.add(map);
            fireTableRowsInserted(this.mappings.size() - 1, this.mappings.size() - 1);
        }

        public void addRow(String varname, int j) {
            String[] map = new String[2];
            map[0] = varname;
            this.mappings.insertElementAt(map, j);
        }

        public Class getColumnClass(int colindex) {
            return (String.class);
        }

        public int getColumnCount() {
            return (2);
        }

        public String getColumnName(int col) {
            String out = null;
            if (col == 0) {
                out = "Model Parameter";
            } else if (col == 1) {
                out = "Mapped To:";
            } else {
                throw new RuntimeException("Invalid column request.");
            }
            return (out);
        }

        public int getRowCount() {
            return (this.mappings.size());
        }

        public Object getMappedValue(int r) {
            return (this.getValueAt(r, 1));
        }

        public void setMappedValue(int r, Object v) {
            this.setValueAt(v, r, 1);
        }

        public Object getValueAt(int r, int c) {
            String[] map = (String[]) this.mappings.get(r);
            System.out.println("Got value at r=" + r + " c=" + c + " = " + map[c]);
            return (map[c]);
        }

        public void setValueAt(Object v, int r, int c) {
            System.out.println("Setting value at r=" + r + " to " + v);
            if (r < this.getRowCount()) {
                String[] map = (String[]) this.mappings.get(r);
                map[c] = (String) v;
            } else {
                throw new RuntimeException("Index out of range.");
            }
            fireTableCellUpdated(r, c);
        }

        public boolean isCellEditable(int r, int c) {
            boolean out = false;
            if (c == 1) {
                out = true;
            }
            return (out);
        }
    }

    public class MappingTableModel extends AbstractTableModel {

        private Vector mappings;

        public MappingTableModel() {
            super();
            this.mappings = new Vector();
        }

        public int indexOf(String var) {
            int k = 0;
            while (k < mappings.size()) {
                String[] map = (String[]) mappings.get(k);
                if (map[0].equals(var)) {
                    break;
                } else {
                    k++;
                }
            }
            if (k >= mappings.size()) {
                k = -1;
            }
            return (k);
        }

        public void removeMappingsFor(String var) {
            int k = 0;
            while (k < mappings.size()) {
                String[] map = (String[]) mappings.get(k);
                if (map[0].equals(var)) {
                    this.removeRow(k);
                } else {
                    k++;
                }
            }
        }

        public void removeRow(int k) {
            mappings.removeElementAt(k);
            fireTableRowsDeleted(k, k);
        }

        public void addRow(String varname) {
            String[] map = new String[2];
            map[0] = varname;
            this.mappings.add(map);
            fireTableRowsInserted(this.mappings.size() - 1, this.mappings.size() - 1);
        }

        public void addRow(String varname, int j) {
            String[] map = new String[2];
            map[0] = varname;
            this.mappings.insertElementAt(map, j);
        }

        public Class getColumnClass(int colindex) {
            return (String.class);
        }

        public int getColumnCount() {
            return (2);
        }

        public String getColumnName(int col) {
            String out = null;
            if (col == 0) {
                out = "Model Variable";
            } else if (col == 1) {
                out = "Data Column";
            } else {
                throw new RuntimeException("Invalid column request.");
            }
            return (out);
        }

        public int getRowCount() {
            return (this.mappings.size());
        }

        public Object getMappedValue(int r) {
            return (this.getValueAt(r, 1));
        }

        public void setMappedValue(int r, Object v) {
            this.setValueAt(v, r, 1);
        }

        public Object getValueAt(int r, int c) {
            String[] map = (String[]) this.mappings.get(r);
            System.out.println("Got value at r=" + r + " c=" + c + " = " + map[c]);
            return (map[c]);
        }

        public void setValueAt(Object v, int r, int c) {
            System.out.println("Setting value at r=" + r + " to " + v);
            if (r < this.getRowCount()) {
                String[] map = (String[]) this.mappings.get(r);
                map[c] = (String) v;
            } else {
                throw new RuntimeException("Index out of range.");
            }
            fireTableCellUpdated(r, c);
        }

        public boolean isCellEditable(int r, int c) {
            boolean out = false;
            if (c == 1) {
                out = true;
            }
            return (out);
        }
    }

    public Class getStorageTransferAgentClass() {
        return (GLMStorageTransferAgent.class);
    }

    public Vector getRandomTerms() {
        RandomTermsTableModel model = (RandomTermsTableModel) this.randomTerms.getModel();
        Vector out = new Vector();
        for (int i = 0; i < model.getRowCount(); i++) {
            out.add((String) model.getValueAt(i, 0));
        }
        return (out);
    }

    public void addRandomTerm(String s) {
        RandomTermsTableModel model = (RandomTermsTableModel) this.randomTerms.getModel();
        model.addRow(s);
    }

    public void removeRandomTerm(String s) {
        RandomTermsTableModel model = (RandomTermsTableModel) this.randomTerms.getModel();
        model.removeRow(s);
    }

    public String getModelRightHandSideTerms() {
        return (modelRightHandSide.getText());
    }

    public void setModelRightHandSideTerms(String s) {
        this.modelRightHandSide.setText(s);
    }

    public String getAnalysisByTerms() {
        return (this.analysisByTerms.getText());
    }

    public void setAnalysisByTerms(String s) {
        this.analysisByTerms.setText(s);
    }

    public Vector getVariables() {
        Vector out = new Vector();
        VariableListModel mod = (VariableListModel) (variableGrabber.getModel());
        for (int j = 0; j < mod.getSize(); j++) {
            out.add(mod.getElementAt(j));
        }
        return (out);
    }

    public void addVariable(String var) {
        VariableListModel mod = (VariableListModel) (variableGrabber.getModel());
        mod.add(var);
    }

    public void removeVariable(String var) {
        VariableListModel mod = (VariableListModel) (variableGrabber.getModel());
        mod.remove(var);
    }

    public Vector getClassTerms() {
        ClassTermsTableModel model = (ClassTermsTableModel) this.modelClassTerms.getModel();
        Vector out = new Vector();
        for (int i = 0; i < model.getRowCount(); i++) {
            out.add((String) model.getValueAt(i, 0));
        }
        return (out);
    }

    public void addClassTerm(String s) {
        ClassTermsTableModel model = (ClassTermsTableModel) this.modelClassTerms.getModel();
        model.addRow(s);
    }

    public void removeClassTerm(String s) {
        ClassTermsTableModel model = (ClassTermsTableModel) this.modelClassTerms.getModel();
        model.removeRow(s);
    }

    public void setModelLeftHandSide(String s) {
        this.modelLeftHandSide.setText(s);
    }

    public String getModelLeftHandSide() {
        return (this.modelLeftHandSide.getText());
    }

    public String getStatisticalModelClassName() {
        return (null);
    }

    public void setStatisticalModelClassName(String s) {
    }

    public StatisticalModel getStatisticalModel() {
        return (null);
    }

    public Class getGraphicalEditorClass() {
        return (this.getClass());
    }

    public void setFlatFileSetStorageNickname(String s) {
        this.dataSetText.setText(s);
    }

    public String getFlatFileSetStorageNickname() {
        return (this.dataSetText.getText());
    }

    public DataSet getDataSet() {
        return (null);
    }

    public void addDoubleParameter(String param) {
        NamedParameterNode node = new NamedParameterNode(param);
        this.parameterTree.insertDomainNameNode(node);
    }

    public void removeDoubleParameter(String param) {
        NamedParameterNode node = new NamedParameterNode(param);
        this.parameterTree.removeDomainNameNode(node);
    }

    public void setDoubleParameterValueFor(String param, String value) {
        String val = "";
        ;
        if (value != null) {
            val = value.trim();
        }
        NamedParameterNode node = new NamedParameterNode(param);
        if (val.length() != 0) {
            node.setParameterValue(new DblMatrix(new Double(val)));
        }
        this.parameterTree.insertDomainNameNode(node);
    }

    public String getDoubleParameterValueFor(String param) {
        String out = null;
        System.out.println("Getting value for parameter " + param);
        NamedParameterNode node = (NamedParameterNode) this.parameterTree.getDomainNameNodeForDomain(param);
        DblMatrix val = node.getParameterValue();
        if (val.getN() == 1) {
            out = val.getDoubleAt(0).toString();
        }
        return (out);
    }

    public Vector getParameterNames() {
        DblParamSet par = this.getParameterSet();
        String[] params = par.parameterSet();
        Vector out = new Vector();
        for (int j = 0; j < params.length; j++) {
            out.add(params[j]);
        }
        return (out);
    }

    public DblParamSet getParameterSet() {
        Vector leafnodes = this.parameterTree.getLeafNodes();
        DefaultMutableTreeNode node = null;
        DblParamSet out = new DblParamSet();
        for (int j = 0; j < leafnodes.size(); j++) {
            node = (DefaultMutableTreeNode) leafnodes.get(j);
            Object uo = node.getUserObject();
            if (uo instanceof NamedParameterNode) {
                String name = ((NamedParameterNode) uo).getDomain();
                DblMatrix val = ((NamedParameterNode) uo).getParameterValue();
                out.setParam(name, val);
            }
        }
        return (out);
    }

    public Set getDeclaredVariables() {
        Vector vars = this.getVariables();
        HashSet out = new HashSet();
        for (int j = 0; j < vars.size(); j++) {
            out.add(vars.get(j));
        }
        return (out);
    }

    public void declareVariable(String var) {
        this.addVariable(var);
    }

    public void revokeVariable(String var) {
        this.removeVariable(var);
    }

    public Set getDeclaredParameters() {
        Vector pars = this.getParameterNames();
        HashSet out = new HashSet();
        for (int j = 0; j < pars.size(); j++) {
            out.add(pars.get(j));
        }
        return (out);
    }

    public void declareParameter(String var) {
        this.addDoubleParameter(var);
    }

    public void revokeParameter(String var) {
        this.removeDoubleParameter(var);
    }

    public Vector getMappedVariables() {
        Vector out = new Vector();
        MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
        for (int j = 0; j < model.getRowCount(); j++) {
            out.add((String) model.getValueAt(j, 0));
        }
        return (out);
    }

    public void addMappedVariable(String var) {
        MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
        int selectedrow = model.indexOf(var);
        if (selectedrow == -1) {
            model.addRow(var);
        }
    }

    public void removeMappedVariable(String var) {
        MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
        int selectedrow = model.indexOf(var);
        if (selectedrow >= 0) {
            model.removeRow(selectedrow);
        }
    }

    public String getDataSetColumnMappedFor(String modelvarname) {
        String out = null;
        MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
        int selectedrow = model.indexOf(modelvarname);
        if (selectedrow >= 0) {
            out = (String) model.getMappedValue(selectedrow);
        }
        return (out);
    }

    public void setDataSetColumnMappedFor(String modelvarname, String datacolname) {
        String out = null;
        MappingTableModel model = (MappingTableModel) variableMappingsTable.getModel();
        int selectedrow = model.indexOf(modelvarname);
        if (selectedrow >= 0) {
            model.setMappedValue(selectedrow, datacolname);
        }
    }
}
