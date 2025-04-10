import java.util.*;
import java.io.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;

public class JDBCConnectCustomizer extends javax.swing.JPanel implements java.beans.Customizer {

    private JDBCConnect dbConnection;

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        tabbedPane = new javax.swing.JTabbedPane();
        driverPanel = new javax.swing.JPanel();
        driverInputPanel = new javax.swing.JPanel();
        useCheckBox = new javax.swing.JCheckBox();
        jarLabel = new javax.swing.JLabel();
        jarField = new javax.swing.JTextField();
        findJarButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlField = new javax.swing.JTextField();
        dbLabel = new javax.swing.JLabel();
        dbField = new javax.swing.JTextField();
        tableLabel = new javax.swing.JLabel();
        tableField = new javax.swing.JTextField();
        odbcCheckBox = new javax.swing.JCheckBox();
        useDatabaseCheckBox = new javax.swing.JCheckBox();
        driverButtonPanel = new javax.swing.JPanel();
        driverOK = new javax.swing.JButton();
        driverTest = new javax.swing.JButton();
        fieldPanel = new javax.swing.JPanel();
        fieldScrollPane = new javax.swing.JScrollPane();
        fieldMapping = new javax.swing.JTable();
        fieldButtonPanel = new javax.swing.JPanel();
        fieldOK = new javax.swing.JButton();
        fieldRefresh = new javax.swing.JButton();
        mapPanel = new javax.swing.JPanel();
        mapScrollPane = new javax.swing.JScrollPane();
        projectMapping = new javax.swing.JTable();
        mapButtonPanel = new javax.swing.JPanel();
        mapOK = new javax.swing.JButton();
        optionPanel = new javax.swing.JPanel();
        optionInputPanel = new javax.swing.JPanel();
        hourLabel = new javax.swing.JLabel();
        hourComboBox = new javax.swing.JComboBox();
        projectCaseCheckBox = new javax.swing.JCheckBox();
        projValidateCheckBox = new javax.swing.JCheckBox();
        projDBLabel = new javax.swing.JLabel();
        projDBField = new javax.swing.JTextField();
        projTableLabel = new javax.swing.JLabel();
        projTableField = new javax.swing.JTextField();
        projFieldLabel = new javax.swing.JLabel();
        projFieldComboBox = new javax.swing.JComboBox();
        optionButtonPanel = new javax.swing.JPanel();
        optionOK = new javax.swing.JButton();
        optionApply = new javax.swing.JButton();
        setLayout(new java.awt.BorderLayout());
        setMinimumSize(new java.awt.Dimension(387, 254));
        tabbedPane.setPreferredSize(new java.awt.Dimension(387, 254));
        driverPanel.setLayout(new java.awt.BorderLayout());
        driverPanel.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentShown(java.awt.event.ComponentEvent evt) {
                showDriverPanel(evt);
            }
        });
        driverInputPanel.setLayout(new java.awt.GridBagLayout());
        useCheckBox.setSelected(dbConnection.isUse());
        useCheckBox.setText("Use plugin?");
        useCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        driverInputPanel.add(useCheckBox, gridBagConstraints);
        jarLabel.setText("Driver File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(jarLabel, gridBagConstraints);
        jarField.setColumns(20);
        jarField.setText(dbConnection.getJarFile());
        jarField.setMinimumSize(new java.awt.Dimension(400, 19));
        jarField.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(jarField, gridBagConstraints);
        findJarButton.setFont(new java.awt.Font("Dialog", 1, 10));
        findJarButton.setText("Browse...");
        findJarButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findJar(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        driverInputPanel.add(findJarButton, gridBagConstraints);
        nameLabel.setText("Driver Name ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(nameLabel, gridBagConstraints);
        nameField.setColumns(20);
        nameField.setText(dbConnection.getName());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(nameField, gridBagConstraints);
        urlLabel.setText("URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(urlLabel, gridBagConstraints);
        urlField.setText(dbConnection.getUrl());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(urlField, gridBagConstraints);
        dbLabel.setText("Database");
        dbLabel.setEnabled(!useDatabaseCheckBox.isSelected());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(dbLabel, gridBagConstraints);
        dbField.setText(dbConnection.getDatabase());
        dbField.setEnabled(!useDatabaseCheckBox.isSelected());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(dbField, gridBagConstraints);
        tableLabel.setText("Table");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(tableLabel, gridBagConstraints);
        tableField.setText(dbConnection.getTable());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(tableField, gridBagConstraints);
        odbcCheckBox.setForeground(new java.awt.Color(102, 102, 153));
        odbcCheckBox.setSelected(dbConnection.getName().equals(JDBCConnect.ODBCDRIVERNAME));
        odbcCheckBox.setText("Use ODBC Bridge");
        odbcCheckBox.setToolTipText("Use the JDBC-ODBC bridge instead of a driver");
        odbcCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleODBC(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(odbcCheckBox, gridBagConstraints);
        useDatabaseCheckBox.setForeground(new java.awt.Color(102, 102, 153));
        useDatabaseCheckBox.setSelected(dbConnection.getDatabase() == null);
        useDatabaseCheckBox.setText("Don't Specify A Database");
        useDatabaseCheckBox.setToolTipText("Don't use a database name in queries");
        useDatabaseCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleNoDatabase(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        driverInputPanel.add(useDatabaseCheckBox, gridBagConstraints);
        driverPanel.add(driverInputPanel, java.awt.BorderLayout.CENTER);
        driverOK.setText("Save");
        driverOK.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDriverSettings(evt);
            }
        });
        driverButtonPanel.add(driverOK);
        driverTest.setText("Test");
        driverTest.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testDriverSettings(evt);
            }
        });
        driverButtonPanel.add(driverTest);
        driverPanel.add(driverButtonPanel, java.awt.BorderLayout.SOUTH);
        tabbedPane.addTab("JDBC Driver", null, driverPanel, "Change JDBC Driver Settings");
        fieldPanel.setLayout(new java.awt.BorderLayout());
        fieldPanel.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentShown(java.awt.event.ComponentEvent evt) {
                showFieldMap(evt);
            }
        });
        fieldMapping.setModel(dbConnection.getTableMap().toFieldValuesTableModel());
        fieldScrollPane.setViewportView(fieldMapping);
        fieldPanel.add(fieldScrollPane, java.awt.BorderLayout.CENTER);
        fieldOK.setText("Save");
        fieldOK.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDriverSettings(evt);
            }
        });
        fieldButtonPanel.add(fieldOK);
        fieldRefresh.setText("Refresh");
        fieldRefresh.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshFieldMap(evt);
            }
        });
        fieldButtonPanel.add(fieldRefresh);
        fieldPanel.add(fieldButtonPanel, java.awt.BorderLayout.SOUTH);
        tabbedPane.addTab("Field Values", null, fieldPanel, "Define values for table fields");
        mapPanel.setLayout(new java.awt.BorderLayout());
        mapPanel.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentShown(java.awt.event.ComponentEvent evt) {
                showProjectNames(evt);
            }
        });
        projectMapping.setModel(dbConnection.getTableMap().toProjectNamesTableModel());
        mapScrollPane.setViewportView(projectMapping);
        mapPanel.add(mapScrollPane, java.awt.BorderLayout.CENTER);
        mapOK.setText("Save");
        mapOK.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDriverSettings(evt);
            }
        });
        mapButtonPanel.add(mapOK);
        mapPanel.add(mapButtonPanel, java.awt.BorderLayout.SOUTH);
        tabbedPane.addTab("Project Names", mapPanel);
        optionPanel.setLayout(new java.awt.BorderLayout());
        optionPanel.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentShown(java.awt.event.ComponentEvent evt) {
                showOptionPanel(evt);
            }
        });
        optionInputPanel.setLayout(new java.awt.GridBagLayout());
        hourLabel.setText("Export hours by:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(hourLabel, gridBagConstraints);
        hourComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Full Precision", "Quarter of an hour", "Tenth of an hour" }));
        hourComboBox.setSelectedIndex(dbConnection.getHourFormat());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        optionInputPanel.add(hourComboBox, gridBagConstraints);
        projectCaseCheckBox.setForeground(new java.awt.Color(102, 102, 153));
        projectCaseCheckBox.setSelected(dbConnection.getProjectCase());
        projectCaseCheckBox.setText("Upper-Case Project");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projectCaseCheckBox, gridBagConstraints);
        projValidateCheckBox.setForeground(new java.awt.Color(102, 102, 153));
        projValidateCheckBox.setSelected(dbConnection.isProjectValidate());
        projValidateCheckBox.setText("Validate Project");
        projValidateCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleValidateProject(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projValidateCheckBox, gridBagConstraints);
        projDBLabel.setText("Project Database:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projDBLabel, gridBagConstraints);
        projDBField.setText(dbConnection.getProjectDatabase());
        projDBField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projDBField, gridBagConstraints);
        projTableLabel.setText("Project Table:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projTableLabel, gridBagConstraints);
        projTableField.setText(dbConnection.getProjectTable());
        projTableField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projTableField, gridBagConstraints);
        projFieldLabel.setText("Project Field");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(projFieldLabel, gridBagConstraints);
        projFieldComboBox.setModel(new DefaultComboBoxModel(new String[] { dbConnection.getProjectField() }));
        projFieldComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        optionInputPanel.add(projFieldComboBox, gridBagConstraints);
        optionPanel.add(optionInputPanel, java.awt.BorderLayout.CENTER);
        optionOK.setText("Save");
        optionOK.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDriverSettings(evt);
            }
        });
        optionButtonPanel.add(optionOK);
        optionApply.setText("Refresh");
        optionApply.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyOptions(evt);
            }
        });
        optionButtonPanel.add(optionApply);
        optionPanel.add(optionButtonPanel, java.awt.BorderLayout.SOUTH);
        tabbedPane.addTab("Options", null, optionPanel, "Set variable options");
        add(tabbedPane, java.awt.BorderLayout.CENTER);
    }

    private void findJar(java.awt.event.ActionEvent evt) {
        final javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            File jar = fc.getSelectedFile();
            String jarFile = jar.toString();
            jarField.setText(jarFile);
        }
    }

    private void toggleNoDatabase(java.awt.event.ActionEvent evt) {
        if (useDatabaseCheckBox.isSelected()) {
            dbField.setEnabled(false);
            dbLabel.setEnabled(false);
        } else {
            dbField.setEnabled(true);
            dbLabel.setEnabled(true);
        }
        driverInputPanel.repaint();
    }

    private void showProjectNames(java.awt.event.ComponentEvent evt) {
        getRootPane().setDefaultButton(mapOK);
        projectMapping.setModel(dbConnection.getTableMap().toProjectNamesTableModel());
        projectMapping.getColumnModel().getColumn(0).setPreferredWidth(1);
        projectMapping.repaint();
    }

    private void applyOptions(java.awt.event.ActionEvent evt) {
        dbConnection.setUse(useCheckBox.isSelected());
        dbConnection.setJarFile(jarField.getText());
        dbConnection.setProjectDatabase(projDBField.getText());
        dbConnection.setProjectTable(projTableField.getText());
        int projFieldIndex = projFieldComboBox.getSelectedIndex();
        dbConnection.setProjectField((String) projFieldComboBox.getItemAt(projFieldIndex));
        String[] fieldNames = dbConnection.getProjectFieldNames();
        DefaultComboBoxModel boxModel;
        if (fieldNames == null) {
            boxModel = new DefaultComboBoxModel(new String[] { "No Fields Found" });
            projFieldComboBox.setEnabled(false);
        } else {
            boxModel = new DefaultComboBoxModel(fieldNames);
            projFieldComboBox.setEnabled(true);
        }
        projFieldComboBox.setModel(boxModel);
        optionInputPanel.repaint();
    }

    private void toggleValidateProject(java.awt.event.ActionEvent evt) {
        if (projValidateCheckBox.isSelected()) {
            projDBField.setEnabled(true);
            projTableField.setEnabled(true);
        } else {
            projDBField.setEnabled(false);
            projTableField.setEnabled(false);
        }
        if (useDatabaseCheckBox.isSelected()) {
            projDBField.setEnabled(false);
            projDBLabel.setEnabled(false);
        }
        optionInputPanel.repaint();
    }

    private void showDriverPanel(java.awt.event.ComponentEvent evt) {
        getRootPane().setDefaultButton(driverOK);
    }

    private void showOptionPanel(java.awt.event.ComponentEvent evt) {
        populate();
        getRootPane().setDefaultButton(optionOK);
    }

    private void toggleODBC(java.awt.event.ActionEvent evt) {
        if (odbcCheckBox.isSelected()) {
            nameField.setText(JDBCConnect.ODBCDRIVERNAME);
            nameField.setEnabled(false);
            nameLabel.setEnabled(false);
            urlLabel.setText("Data Source");
            int lastColon = dbConnection.getUrl().lastIndexOf(':') + 1;
            urlField.setText(dbConnection.getUrl().substring(lastColon));
        } else {
            nameField.setText(dbConnection.getName());
            nameField.setEnabled(true);
            nameLabel.setEnabled(true);
            urlLabel.setText("URL");
            urlField.setText(dbConnection.getUrl());
        }
        driverInputPanel.repaint();
    }

    private void refreshFieldMap(java.awt.event.ActionEvent evt) {
        try {
            dbConnection.getTableMap().clearFieldMaps();
            fieldMapping.setModel(dbConnection.getTableMap().toFieldValuesTableModel());
            fieldMapping.repaint();
        } catch (java.sql.SQLException e) {
            System.err.println("Couldn't initialize table mapping");
        }
    }

    private void showFieldMap(java.awt.event.ComponentEvent evt) {
        populate();
        getRootPane().setDefaultButton(fieldOK);
        if (dbConnection.getTableMap().getFieldMaps().size() == 0) {
            try {
                dbConnection.getTableMap().clearFieldMaps();
                fieldMapping.setModel(dbConnection.getTableMap().toFieldValuesTableModel());
                fieldMapping.repaint();
            } catch (java.sql.SQLException e) {
                System.err.println("Couldn't initialize table mapping");
            }
        }
    }

    private void testDriverSettings(java.awt.event.ActionEvent evt) {
        String name = nameField.getText();
        String url = urlField.getText();
        String database = dbField.getText();
        String table = tableField.getText();
        JDBCConnect testConnection = new JDBCConnect(name, url, database, table);
        testConnection.setJarFile(jarField.getText());
        testConnection.testDriverSettings();
    }

    private void saveDriverSettings(java.awt.event.ActionEvent evt) {
        populate();
        try {
            PluginManager.serializeObject(dbConnection);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Error saving prefs for JDBC plugin");
        }
        Object grandpa = getTopLevelAncestor();
        if (grandpa.getClass() == PluginManager.class) ((PluginManager) grandpa).exitForm(); else ((JFrame) grandpa).setVisible(false);
    }

    private void populate() {
        dbConnection.setUse(useCheckBox.isSelected());
        dbConnection.setJarFile(jarField.getText());
        dbConnection.setName(nameField.getText());
        dbConnection.setUrl(urlField.getText());
        if (dbConnection.getName().equals(JDBCConnect.ODBCDRIVERNAME)) dbConnection.setUrl("jdbc:odbc:" + dbConnection.getUrl());
        dbConnection.setDatabase(useDatabaseCheckBox.isSelected() ? null : dbField.getText());
        dbConnection.setTable(tableField.getText());
        dbConnection.setProjectDatabase(useDatabaseCheckBox.isSelected() ? null : projDBField.getText());
        dbConnection.setProjectTable(projTableField.getText());
        if (projFieldComboBox.getItemCount() != 0) {
            int projFieldIndex = projFieldComboBox.getSelectedIndex();
            dbConnection.setProjectField((String) projFieldComboBox.getItemAt(projFieldIndex));
        }
        dbConnection.setProjectValidate(projValidateCheckBox.isSelected());
        dbConnection.setHourFormat(hourComboBox.getSelectedIndex());
        dbConnection.setProjectCase(projectCaseCheckBox.isSelected());
        TableMap tableMap = dbConnection.getTableMap();
        Vector fieldMaps = tableMap.getFieldMaps();
        Hashtable projectMaps = new Hashtable();
        for (int i = 0; i < fieldMaps.size(); i++) {
            String value = (String) fieldMapping.getValueAt(i, 2);
            FieldMap record = (FieldMap) fieldMaps.elementAt(i);
            record.setValueExpression(value);
        }
        tableMap.setFieldMaps(fieldMaps);
        for (int i = 0; i < projectMapping.getRowCount(); i++) {
            String projectName = (String) projectMapping.getValueAt(i, 1);
            boolean export = ((Boolean) projectMapping.getValueAt(i, 0)).booleanValue();
            String alias = (String) projectMapping.getValueAt(i, 2);
            projectMaps.put(projectName, new ProjectMap(alias, export));
        }
        tableMap.setProjectMaps(projectMaps);
        dbConnection.setTableMap(tableMap);
    }

    public void setObject(Object obj) {
        dbConnection = (JDBCConnect) obj;
        initComponents();
        ActionEvent evt = new ActionEvent(this, 0, "Refresh");
        toggleNoDatabase(evt);
        toggleODBC(evt);
        toggleValidateProject(evt);
    }

    private javax.swing.JTextField dbField;

    private javax.swing.JLabel dbLabel;

    private javax.swing.JPanel driverButtonPanel;

    private javax.swing.JPanel driverInputPanel;

    private javax.swing.JButton driverOK;

    private javax.swing.JPanel driverPanel;

    private javax.swing.JButton driverTest;

    private javax.swing.JPanel fieldButtonPanel;

    private javax.swing.JTable fieldMapping;

    private javax.swing.JButton fieldOK;

    private javax.swing.JPanel fieldPanel;

    private javax.swing.JButton fieldRefresh;

    private javax.swing.JScrollPane fieldScrollPane;

    private javax.swing.JButton findJarButton;

    private javax.swing.JComboBox hourComboBox;

    private javax.swing.JLabel hourLabel;

    private javax.swing.JTextField jarField;

    private javax.swing.JLabel jarLabel;

    private javax.swing.JPanel mapButtonPanel;

    private javax.swing.JButton mapOK;

    private javax.swing.JPanel mapPanel;

    private javax.swing.JScrollPane mapScrollPane;

    private javax.swing.JTextField nameField;

    private javax.swing.JLabel nameLabel;

    private javax.swing.JCheckBox odbcCheckBox;

    private javax.swing.JButton optionApply;

    private javax.swing.JPanel optionButtonPanel;

    private javax.swing.JPanel optionInputPanel;

    private javax.swing.JButton optionOK;

    private javax.swing.JPanel optionPanel;

    private javax.swing.JTextField projDBField;

    private javax.swing.JLabel projDBLabel;

    private javax.swing.JComboBox projFieldComboBox;

    private javax.swing.JLabel projFieldLabel;

    private javax.swing.JTextField projTableField;

    private javax.swing.JLabel projTableLabel;

    private javax.swing.JCheckBox projValidateCheckBox;

    private javax.swing.JCheckBox projectCaseCheckBox;

    private javax.swing.JTable projectMapping;

    javax.swing.JTabbedPane tabbedPane;

    private javax.swing.JTextField tableField;

    private javax.swing.JLabel tableLabel;

    private javax.swing.JTextField urlField;

    private javax.swing.JLabel urlLabel;

    private javax.swing.JCheckBox useCheckBox;

    private javax.swing.JCheckBox useDatabaseCheckBox;
}
