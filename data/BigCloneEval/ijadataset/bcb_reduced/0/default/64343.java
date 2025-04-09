import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import tuffy.learn.DNLearner;
import tuffy.main.NonPartInfer;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;

public class Janela extends JPanel {

    private static final long serialVersionUID = 1L;

    private JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private JSplitPane splitPaneV1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private JSplitPane splitPaneV2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private JPanel inferPanel = new JPanel();

    private JPanel loadPanel = new JPanel();

    private JPanel parametersPanel = new JPanel();

    private JTabbedPane tabbedPane1 = new JTabbedPane();

    private JTabbedPane tabbedPane2 = new JTabbedPane();

    private JTextField arqMLN = null;

    private JTextField arqEvd = null;

    private JTextField arqQry = null;

    private JTextField arqOut = null;

    private JTextField cwaPreds = null;

    private JTextField textField = null;

    private String MLNFile = "";

    private String confFile = "paramConfig.txt";

    private JComboBox jCB = null;

    private JCheckBox checkBox = null;

    private JScrollPane jSP = null;

    private JScrollPane jSPinfer = null;

    private JScrollPane jSPMLNLoad = null;

    private JScrollPane jSPMLNTree = null;

    private JScrollPane jSPMLNTree2 = null;

    private JScrollPane jSPParameters = null;

    private JTextArea jTA_MLN = null;

    private JTextArea jTAInfer = null;

    private JTextArea jTA_Tree = null;

    private JTextArea jTA_Parameters = null;

    private JButton jB = null;

    private JButton inferButton = null;

    private JButton MLNLoadButton = null;

    private JButton EvdLoadButton = null;

    private JButton QryLoadButton = null;

    private JButton MLNSaveButton = null;

    private JButton EvdSaveButton = null;

    private JButton QrySaveButton = null;

    private JButton paramApplyButton = null;

    private JButton paramSaveButton = null;

    ArrayList<Parameter> parameters = new ArrayList<Parameter>();

    Map<String, Parameter> parameterMap = new HashMap<String, Parameter>();

    Map<String, JComponent> parameterGuiMap = new HashMap<String, JComponent>();

    private File loadedFile = null;

    private String result = "";

    GridBagConstraints c = new GridBagConstraints();

    GridBagConstraints c2 = new GridBagConstraints();

    public Janela() {
        super();
        initialize();
    }

    private void initialize() {
        paineis();
        labels();
        fields();
        readConfiguration();
        parametersTable();
        parameters();
        simpleTree();
        scrollPane();
        comboboxes();
        buttons();
        tabbedPane();
        listeners();
    }

    private void paineis() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1200, 800));
        loadPanel.setLayout(new GridBagLayout());
        inferPanel.setLayout(new GridBagLayout());
        parametersPanel.setLayout(new GridBagLayout());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(10, 10, 10, 10);
        splitPaneH.setMinimumSize(new Dimension(1200, 1000));
        c.gridx = 0;
        c.gridy = 0;
        add(splitPaneH, BorderLayout.CENTER);
        splitPaneH.setLeftComponent(splitPaneV1);
        splitPaneH.setRightComponent(splitPaneV2);
        splitPaneV1.setLeftComponent(loadPanel);
        splitPaneV1.setRightComponent(tabbedPane1);
        splitPaneV2.setLeftComponent(inferPanel);
        splitPaneV2.setRightComponent(tabbedPane2);
    }

    private void labels() {
        c.gridx = 0;
        c.gridy = 0;
        inferPanel.add(new JLabel("ComboBox local"), c);
        c.gridx = 0;
        c.gridy = 5;
        inferPanel.add(new JLabel("Infer Results"), c);
        c.gridx = 0;
        c.gridy = 0;
        loadPanel.add(new JLabel("MLN File"), c);
        c.gridx = 0;
        c.gridy = 1;
        loadPanel.add(new JLabel("Evidency File"), c);
        c.gridx = 0;
        c.gridy = 2;
        loadPanel.add(new JLabel("Query"), c);
    }

    private void fields() {
        c.gridwidth = 4;
        c.gridx = 1;
        c.gridy = 0;
        arqMLN = getJTextField(arqMLN, "samples/smoke/prog.mln");
        loadPanel.add(arqMLN, c);
        c.gridx = 1;
        c.gridy = 1;
        arqEvd = getJTextField(arqEvd, "samples/smoke/evidence.db");
        loadPanel.add(arqEvd, c);
        c.gridx = 1;
        c.gridy = 2;
        arqQry = getJTextField(arqQry, "samples/smoke/query.db");
        loadPanel.add(arqQry, c);
        c.gridwidth = 1;
    }

    private void readConfiguration() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("samples/smoke/par�metros.txt"));
            String[] lineParam;
            String line = "";
            while (in.ready()) {
                line = in.readLine();
                if (line.substring(0, 2).equals("//")) ; else {
                    Parameter parameter = new Parameter();
                    lineParam = line.split(";", 5);
                    parameter.setLabel(lineParam[0]);
                    parameter.setAttribute(lineParam[1]);
                    parameter.setDescription(lineParam[2]);
                    if (lineParam[3].equals("String")) parameter.setVariableType(Parameter.VariableType.String); else if (lineParam[3].equals("Integer")) parameter.setVariableType(Parameter.VariableType.Integer); else if (lineParam[3].equals("Float")) parameter.setVariableType(Parameter.VariableType.Float); else if (lineParam[3].equals("Boolean")) parameter.setVariableType(Parameter.VariableType.Boolean);
                    parameter.setDefaultValue(lineParam[4]);
                    parameters.add(parameter);
                }
            }
            in.close();
        } catch (IOException ex) {
        }
    }

    private void parametersTable() {
        for (Parameter param : parameters) {
            System.out.println(param.getAttribute());
            System.out.println(param.getDescription());
            if (param.getVariableType().equals(Parameter.VariableType.String)) {
                System.out.println("[estrutura <->]");
            }
            if (param.getVariableType().equals(Parameter.VariableType.Integer)) {
                System.out.println("[campo num�rico]");
            }
            if (param.getVariableType().equals(Parameter.VariableType.Float)) {
                System.out.println("[campo num�rico]");
            }
            if (param.getVariableType().equals(Parameter.VariableType.Boolean)) {
                System.out.println("[checkbox]");
            }
            System.out.println(param.getDefaultValue());
            System.out.println("---------------------------------------");
        }
        int x = 0, y = 0;
        c.gridx = x;
        c.gridy = y;
        parametersPanel.add(new JLabel("Parameter"), c);
        c.gridx = ++x;
        parametersPanel.add(new JLabel("Description"), c);
        c.gridx = ++x;
        parametersPanel.add(new JLabel("Value"), c);
        x = 0;
        y++;
        for (Parameter param : parameters) {
            parameterMap.put(param.getAttribute(), param);
            c.gridx = x;
            c.gridy = y;
            parametersPanel.add(new JLabel(param.getLabel()), c);
            c.gridx = ++x;
            parametersPanel.add(new JLabel(param.getDescription()), c);
            c.gridx = ++x;
            if ((param.getVariableType().equals(Parameter.VariableType.String)) || (param.getVariableType().equals(Parameter.VariableType.Integer)) || (param.getVariableType().equals(Parameter.VariableType.Float))) {
                textField = null;
                textField = getJTextField(textField, param.getDefaultValue());
                parameterGuiMap.put(param.getAttribute(), textField);
                parametersPanel.add(textField, c);
            } else if (param.getVariableType().equals(Parameter.VariableType.Boolean)) {
                checkBox = null;
                if (param.getDefaultValue().equals("true")) {
                    checkBox = getJCheckBox(checkBox, true);
                } else checkBox = getJCheckBox(checkBox, false);
                parameterGuiMap.put(param.getAttribute(), checkBox);
                parametersPanel.add(checkBox, c);
            }
            x = 0;
            y++;
        }
        c.gridx = 2;
        c.gridy = y;
        paramApplyButton = getJButton("Apply");
        paramApplyButton.setEnabled(true);
        parametersPanel.add(paramApplyButton, c);
        c.gridx = 3;
        c.gridy = y;
        paramSaveButton = getJButton("Save");
        paramSaveButton.setEnabled(true);
        parametersPanel.add(paramSaveButton, c);
    }

    private void comboboxes() {
        c.gridwidth = 4;
        c.gridx = 1;
        c.gridy = 0;
        inferPanel.add(getComboBox(), c);
        c.gridwidth = 1;
    }

    private void scrollPane() {
        jTAInfer = getJTextArea(jTAInfer);
        c.gridx = 1;
        c.gridy = 5;
        c.gridheight = 7;
        c.gridwidth = 4;
        jSPinfer = getJScrollPaneTA(jTAInfer);
        inferPanel.add(jSPinfer, c);
        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 5;
        c.gridheight = 7;
        c.gridwidth = 4;
        jSPParameters = getJScrollPanePanel(parametersPanel);
        inferPanel.add(jSPParameters, c);
        c.gridheight = 1;
        c.gridwidth = 1;
        jTA_MLN = getJTextArea(jTA_MLN);
        c.gridx = 0;
        c.gridy = 2;
        c.gridheight = 7;
        c.gridwidth = 5;
        jSPMLNLoad = getJScrollPaneTA(jTA_MLN);
        loadPanel.add(jSPMLNLoad, c);
        c.gridheight = 1;
        jTA_Tree = getJTextArea(jTA_Tree);
        c.gridx = 0;
        c.gridy = 9;
        c.gridheight = 7;
        c.gridwidth = 5;
        jSPMLNTree = getJScrollPaneTA(jTA_Tree);
        loadPanel.add(jSPMLNTree, c);
        c.gridheight = 1;
        c.gridwidth = 1;
    }

    private void parameters() {
    }

    private void buttons() {
        c.gridx = 1;
        c.gridy = 4;
        inferButton = getJButton("Infer");
        inferButton.setEnabled(false);
        inferPanel.add(inferButton, c);
        c.gridx = 5;
        c.gridy = 0;
        MLNLoadButton = getJButton("Load");
        loadPanel.add(MLNLoadButton, c);
        c.gridx = 5;
        c.gridy = 1;
        EvdLoadButton = getJButton("Load");
        loadPanel.add(EvdLoadButton, c);
        c.gridx = 5;
        c.gridy = 2;
        QryLoadButton = getJButton("Load");
        loadPanel.add(QryLoadButton, c);
        c.gridx = 6;
        c.gridy = 0;
        MLNSaveButton = getJButton("Save");
        loadPanel.add(MLNSaveButton, c);
        c.gridx = 6;
        c.gridy = 1;
        EvdSaveButton = getJButton("Save");
        loadPanel.add(EvdSaveButton, c);
        c.gridx = 6;
        c.gridy = 2;
        QrySaveButton = getJButton("Save");
        loadPanel.add(QrySaveButton, c);
    }

    private void tabbedPane() {
        tabbedPane1.addTab("tree", null, jSPMLNTree, "mln");
        tabbedPane1.setMnemonicAt(0, KeyEvent.VK_2);
        tabbedPane1.addTab("tree2", null, jSPMLNTree2, "mln2");
        tabbedPane1.setMnemonicAt(0, KeyEvent.VK_3);
        tabbedPane2.addTab("inference", null, jSPinfer, "inference");
        tabbedPane2.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane2.addTab("mln", null, jSPMLNLoad, "mln");
        tabbedPane2.setMnemonicAt(0, KeyEvent.VK_4);
        tabbedPane2.addTab("Parameters", null, jSPParameters, "parameters");
        tabbedPane2.setMnemonicAt(0, KeyEvent.VK_5);
        c.gridx = 0;
        c.gridy = 5;
        c.gridheight = 7;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.gridwidth = 1;
    }

    public void simpleTree() {
        Object[] hierarchy = { "javax.swing", "javax.swing.border", "javax.swing.colorchooser", "javax.swing.event", "javax.swing.filechooser", new Object[] { "javax.swing.plaf", "javax.swing.plaf.basic", "javax.swing.plaf.metal", "javax.swing.plaf.multi" }, "javax.swing.table", new Object[] { "javax.swing.text", new Object[] { "javax.swing.text.html", "javax.swing.text.html.parser" }, "javax.swing.text.rtf" }, "javax.swing.tree", "javax.swing.undo" };
        DefaultMutableTreeNode root = processHierarchy(hierarchy);
        JTree tree = new JTree(root);
        jSPMLNTree2 = new JScrollPane(tree);
    }

    private void listeners() {
        inferButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String args = "-marginal -i samples/smoke/prog.mln -e samples/smoke/evidence.db " + "-queryFile samples/smoke/query.db -r out.txt";
                String[] argsArray;
                argsArray = args.split(" ");
                for (final String arg : argsArray) {
                    System.out.println(arg);
                }
                System.out.println("\nresultado da combobox: \n" + jCB.getSelectedItem());
                System.out.println("*** Welcome to mine " + Config.product_name + "!");
                CommandOptions options = new CommandOptions();
                Config.db_url = "jdbc:postgresql://localhost:5432/tuffydb";
                Config.db_username = "tuffer";
                Config.db_password = "strongPasswoRd";
                String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                String user = System.getProperty("user.name").toLowerCase().replaceAll("\\W", "_");
                String machine = null;
                try {
                    machine = java.net.InetAddress.getLocalHost().getHostName().toLowerCase().replaceAll("\\W", "_");
                } catch (UnknownHostException e2) {
                    e2.printStackTrace();
                }
                System.out.println(machine);
                String prod = Config.product_line;
                Config.db_schema += prod + "_" + machine + "_" + user + "_" + pid;
                if (jCB.getSelectedItem().equals("Marginal")) {
                    options.marginal = true;
                }
                if (jCB.getSelectedItem().equals("Dual")) {
                    options.dual = true;
                }
                options.fprog = MLNFile;
                options.fevid = arqEvd.getText();
                options.fquery = arqQry.getText();
                options.fout = "out.txt";
                if (!options.isDLearningMode) {
                    System.out.println("disablePartition" + options.disablePartition);
                    if (!options.disablePartition) {
                        result += (String) new NewPartInfer().run(options);
                    } else {
                        new NonPartInfer().run(options);
                    }
                } else {
                    DNLearner l = new DNLearner();
                    try {
                        l.run(options);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
                jTAInfer.setText(result);
            }
        });
        MLNLoadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setCurrentDirectory(new File("."));
                int res = fc.showOpenDialog(null);
                if (res == JFileChooser.APPROVE_OPTION) {
                    loadedFile = fc.getSelectedFile();
                    System.out.println(loadedFile);
                    MLNFile = loadedFile.toString();
                    System.out.println(MLNFile);
                    if (MLNFile != "") {
                        inferButton.setEnabled(true);
                    }
                }
                try {
                    BufferedReader in = new BufferedReader(new FileReader(MLNFile));
                    String str = "";
                    while (in.ready()) {
                        str += in.readLine() + "\n";
                    }
                    jTA_MLN.setText(str);
                    in.close();
                } catch (IOException ex) {
                }
                try {
                    BufferedReader in = new BufferedReader(new FileReader(MLNFile));
                    String str = "";
                    String line = "";
                    String comment = "//";
                    while (in.ready()) {
                        line = in.readLine() + "\n";
                        if (line.length() == 1) ; else if (line.substring(0, 2).equals(comment)) ; else {
                            str += line;
                            System.out.println("str: " + str);
                        }
                        System.out.println("line: " + line);
                    }
                    jTA_Tree.setText(str);
                    in.close();
                } catch (IOException ex) {
                }
            }
        });
        MLNSaveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(MLNFile));
                    out.write(jTA_MLN.getText());
                    out.close();
                } catch (IOException ex) {
                }
            }
        });
        paramApplyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.out.println("aplicou");
            }
        });
        paramSaveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(confFile));
                    Set<Map.Entry<String, Parameter>> sParam = parameterMap.entrySet();
                    Iterator itParam = sParam.iterator();
                    System.out.println(sParam.getClass());
                    Set<Map.Entry<String, JComponent>> sGui = parameterGuiMap.entrySet();
                    Iterator itGui = sGui.iterator();
                    System.out.println(sGui.getClass());
                    while (itParam.hasNext()) {
                        Map.Entry mParam = (Map.Entry) itParam.next();
                        System.out.println("mParam -> " + mParam.getKey());
                        while (itGui.hasNext()) {
                            Map.Entry mGui = (Map.Entry) itGui.next();
                            System.out.println("paramKey " + mParam.getKey() + ", GuiKey " + mGui.getKey());
                            if (mParam.getKey().equals(mGui.getKey())) {
                                System.out.println("entrou");
                                Parameter param = ((Entry<String, Parameter>) mParam).getValue();
                                if (mGui.getValue().getClass().equals(textField.getClass())) {
                                    JTextField value = ((Entry<String, JTextField>) mGui).getValue();
                                    out.write(param.getAttribute() + " = " + value.getText() + "\n");
                                } else if (mGui.getValue().getClass().equals(checkBox.getClass())) {
                                    JCheckBox value = ((Entry<String, JCheckBox>) mGui).getValue();
                                    out.write(param.getAttribute() + " = " + value.isSelected() + "\n");
                                } else System.out.println(mGui.getValue().getClass());
                                System.out.println("passou");
                            }
                        }
                        itGui = sGui.iterator();
                    }
                    out.close();
                } catch (IOException ex) {
                }
            }
        });
    }

    private JTextField getJTextField(JTextField jTF, String nome) {
        if (jTF == null) {
            jTF = new JTextField(15);
            jTF.setText(nome);
        }
        return jTF;
    }

    private JTextArea getJTextArea(JTextArea jTA) {
        if (jTA == null) {
            jTA = new JTextArea();
        }
        return jTA;
    }

    public JScrollPane getJScrollPaneTA(JTextArea jTA) {
        jSP = new JScrollPane(getJTextArea(jTA));
        jSP.setPreferredSize(new Dimension(250, 180));
        return jSP;
    }

    public JScrollPane getJScrollPanePanel(JPanel jPanel) {
        jSP = new JScrollPane(jPanel);
        jSP.setPreferredSize(new Dimension(250, 180));
        return jSP;
    }

    private JComboBox getComboBox() {
        if (jCB == null) {
            jCB = new JComboBox();
            jCB.addItem("Dual");
            jCB.addItem("Marginal");
            jCB.addItem("MAP Inference");
        }
        return jCB;
    }

    private JCheckBox getJCheckBox(JCheckBox jCheckBox, Boolean value) {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
            jCheckBox.setSelected(value);
        }
        return jCheckBox;
    }

    private JButton getJButton(String name) {
        jB = new JButton(name);
        return jB;
    }

    private DefaultMutableTreeNode processHierarchy(Object[] hierarchy) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(hierarchy[0]);
        DefaultMutableTreeNode child;
        for (int i = 1; i < hierarchy.length; i++) {
            Object nodeSpecifier = hierarchy[i];
            if (nodeSpecifier instanceof Object[]) child = processHierarchy((Object[]) nodeSpecifier); else child = new DefaultMutableTreeNode(nodeSpecifier);
            node.add(child);
        }
        return (node);
    }
}
