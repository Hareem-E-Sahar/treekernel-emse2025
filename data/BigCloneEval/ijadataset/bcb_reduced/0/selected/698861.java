package jp.ac.jaist.ceqea;

import java.io.*;
import java.util.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.datatransfer.*;
import java.awt.Font;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.TransferHandler.*;
import jp.ac.jaist.ceqea.CEq_enums.*;
import jp.ac.jaist.ceqea.io.*;
import jp.ac.jaist.ceqea.A_MIG.*;
import jp.ac.jaist.ceqea.D_inSitu.*;
import jp.ac.jaist.ceqea.G_systems.*;

public class CEq_0_gui extends JFrame {

    private static JTextArea linenumbering;

    class SpcfctnDrop extends TransferHandler {

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            for (DataFlavor flavor : flavors) {
                if (flavor.equals(DataFlavor.javaFileListFlavor) || flavor.equals(DataFlavor.stringFlavor)) return true;
            }
            return false;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                for (DataFlavor flavor : t.getTransferDataFlavors()) {
                    if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                        importFileList((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));
                        return true;
                    }
                    if (flavor.equals(DataFlavor.stringFlavor)) {
                        String dropped = (String) t.getTransferData(DataFlavor.stringFlavor);
                        if (dropped.startsWith("file://")) {
                            List<File> filelist = new ArrayList();
                            for (String name : dropped.split("\r\n")) filelist.add(new File(new URI(name)));
                            importFileList(filelist);
                        } else {
                            if (migeditor.getSelectedText() != null) migeditor.replaceSelection(dropped); else migeditor.insert(dropped, migeditor.getCaretPosition());
                        }
                        return true;
                    }
                }
            } catch (Exception ex) {
                CEq_feedback.exit(CEq_feedback.ERR.CEqUI_import_error, ex.getMessage());
            }
            return false;
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            String selection = migeditor.getSelectedText();
            StringSelection data = new StringSelection(selection);
            clip.setContents(data, data);
        }
    }

    private static final HashMap<String, HashSet<Process>> zgrProcesses = new HashMap();

    private static final HashMap<String, JMenuItem> clearZGRmenu = new HashMap();

    private static String canonical_tmp;

    public CEq_0_gui(CEq_specification spec) {
        this.commonInit(spec.env());
        this.setInput(spec);
    }

    public CEq_0_gui(CEq_environment env) {
        this.commonInit(env);
        this.clearName();
        this.clearEditor();
    }

    private void commonInit(CEq_environment env) {
        if (CEq_system.zgrviewer == null || !new File(CEq_system.zgrviewer).exists()) {
            System.err.println("CRITICAL ERROR: CEqEA cannot find ZGRViewer at " + CEq_system.zgrviewer + "; try the \"" + CEq_system.zgrviewer_Name + "\" environment variable.");
            System.exit(1);
        }
        if (CEq_system.ceqea_tmp == null) {
            System.err.println("CRITICAL ERROR: CEqEA does not have a tmp dir; try the \"" + CEq_system.ceqea_tmp_Name + "\" environment variable.");
            System.exit(1);
        }
        final File tmp_dir = new File(CEq_system.ceqea_tmp);
        if (!tmp_dir.exists()) {
            tmp_dir.mkdir();
            tmp_dir.deleteOnExit();
        }
        if (!tmp_dir.exists() || !tmp_dir.canRead() || !tmp_dir.canWrite()) {
            System.err.println("CRITICAL ERROR: \"" + CEq_system.ceqea_tmp + "\" cannot be used as tmp dir; try/omit the \"" + CEq_system.ceqea_tmp_Name + "\" environment variable.");
            System.exit(1);
        }
        try {
            canonical_tmp = tmp_dir.getCanonicalPath() + File.separator;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        this.initComponents();
        this.alwaysRadio.setIcon(new ImageIcon(""));
        this.alwaysRadio.setSelectedIcon(new ImageIcon(""));
        this.transientRadio.setIcon(new ImageIcon(""));
        this.transientRadio.setSelectedIcon(new ImageIcon(""));
        this.neverRadio.setIcon(new ImageIcon(""));
        this.neverRadio.setSelectedIcon(new ImageIcon(""));
        linenumbering = new JTextArea(" 1 \n");
        linenumbering.setBackground(Color.LIGHT_GRAY);
        linenumbering.setEditable(false);
        linenumbering.setFont(migeditor.getFont());
        migeditor.getDocument().addDocumentListener(new DocumentListener() {

            private final String space = " ";

            public String getText() {
                int newLineno = migeditor.getDocument().getDefaultRootElement().getElementCount();
                int width = (int) Math.log10(newLineno);
                StringBuilder lineBldr = new StringBuilder();
                for (int i = 1; i <= newLineno; i++) {
                    for (int j = (int) Math.log10(i); j < width; j++) lineBldr.append(space);
                    lineBldr.append(i).append(space).append("\n");
                }
                return lineBldr.toString();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                linenumbering.setText(getText());
                alignLinenos2Editor();
            }

            @Override
            public void insertUpdate(DocumentEvent de) {
                linenumbering.setText(getText());
                alignLinenos2Editor();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                linenumbering.setText(getText());
                alignLinenos2Editor();
            }

            private void alignLinenos2Editor() {
                int currLineno = migeditor.getDocument().getDefaultRootElement().getElementIndex(migeditor.getCaretPosition());
                linenumbering.setCaretPosition(linenumbering.getDocument().getDefaultRootElement().getElement(currLineno).getStartOffset());
            }
        });
        edit_scroll.setRowHeaderView(linenumbering);
        this.clearPP();
        this.setArgs(env);
        migeditor.setDragEnabled(true);
        migeditor.setTransferHandler(new SpcfctnDrop());
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                clearZgrViews();
            }
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int vwblHeight = (int) (0.8 * screenSize.height);
        int vwblWidth = (int) (0.8 * screenSize.width);
        Dimension size = new Dimension((vwblWidth > this.getWidth()) ? this.getWidth() : vwblWidth, (vwblHeight > this.getHeight()) ? this.getHeight() : vwblHeight);
        this.setSize(size);
        this.setLocationRelativeTo(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        ppPopup = new javax.swing.JPopupMenu();
        ppAll = new javax.swing.JMenuItem();
        ppNone = new javax.swing.JMenuItem();
        ppsep0 = new javax.swing.JPopupMenu.Separator();
        ppArgs = new javax.swing.JCheckBoxMenuItem();
        ppReads = new javax.swing.JCheckBoxMenuItem();
        ppLex = new javax.swing.JCheckBoxMenuItem();
        ppSymbols = new javax.swing.JCheckBoxMenuItem();
        ppMdlts = new javax.swing.JCheckBoxMenuItem();
        ppStrong = new javax.swing.JCheckBoxMenuItem();
        ppInhib = new javax.swing.JCheckBoxMenuItem();
        ppLinear = new javax.swing.JCheckBoxMenuItem();
        ppAtom = new javax.swing.JCheckBoxMenuItem();
        ppVacuo = new javax.swing.JCheckBoxMenuItem();
        ppStch = new javax.swing.JCheckBoxMenuItem();
        ppDgrms = new javax.swing.JCheckBoxMenuItem();
        ppEq = new javax.swing.JCheckBoxMenuItem();
        ppSys = new javax.swing.JCheckBoxMenuItem();
        ppPhys = new javax.swing.JCheckBoxMenuItem();
        inhibitorsGroup = new javax.swing.ButtonGroup();
        maxGroup = new javax.swing.ButtonGroup();
        normGroup = new javax.swing.ButtonGroup();
        PoIGroup = new javax.swing.ButtonGroup();
        sysGroup = new javax.swing.ButtonGroup();
        tabs = new javax.swing.JTabbedPane();
        input = new javax.swing.JSplitPane();
        arg_scroll = new javax.swing.JScrollPane();
        ceqarguments = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        migname = new javax.swing.JTextField();
        analyzeButton = new javax.swing.JButton();
        dgrmLabel = new javax.swing.JLabel();
        dgrmCasc = new javax.swing.JRadioButton();
        dgrmSS = new javax.swing.JRadioButton();
        eqLabel = new javax.swing.JLabel();
        doSubEq = new javax.swing.JCheckBox();
        doPreEq = new javax.swing.JCheckBox();
        doIndxEq = new javax.swing.JCheckBox();
        alwaysRadio = new javax.swing.JRadioButton();
        alwaysInhb = new javax.swing.JTextField();
        transientRadio = new javax.swing.JRadioButton();
        transientInhb = new javax.swing.JTextField();
        neverRadio = new javax.swing.JRadioButton();
        neverInhb = new javax.swing.JTextField();
        doPP = new javax.swing.JCheckBox();
        ppStages = new javax.swing.JButton();
        sysSep = new javax.swing.JSeparator();
        dgrmSys = new javax.swing.JCheckBox();
        orthgnlCoexist = new javax.swing.JRadioButton();
        intrlvngCoexist = new javax.swing.JRadioButton();
        maximalityLabel = new javax.swing.JLabel();
        maximalityNode = new javax.swing.JRadioButton();
        maximalityEq = new javax.swing.JRadioButton();
        maximalityNoMax = new javax.swing.JRadioButton();
        sysInhibition = new javax.swing.JCheckBox();
        sysSuppress = new javax.swing.JCheckBox();
        sysInter = new javax.swing.JCheckBox();
        sysTrgtRqrd = new javax.swing.JCheckBox();
        sysUnits = new javax.swing.JCheckBox();
        edit_scroll = new javax.swing.JScrollPane();
        migeditor = new javax.swing.JTextArea();
        ppscroll = new javax.swing.JScrollPane();
        prettyprinting = new javax.swing.JTextArea();
        topmenu = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        NewMI = new javax.swing.JMenuItem();
        OpenMI = new javax.swing.JMenuItem();
        SaveMI = new javax.swing.JMenuItem();
        filesep1 = new javax.swing.JPopupMenu.Separator();
        QuitMI = new javax.swing.JMenuItem();
        RunMenu = new javax.swing.JMenu();
        AnalyzeMI = new javax.swing.JMenuItem();
        showUniDgrms = new javax.swing.JCheckBoxMenuItem();
        normLabel = new javax.swing.JPopupMenu.Separator();
        normAlgDNF = new javax.swing.JRadioButtonMenuItem();
        normFullDNF = new javax.swing.JRadioButtonMenuItem();
        expandLabel = new javax.swing.JPopupMenu.Separator();
        normExpandAll = new javax.swing.JCheckBoxMenuItem();
        normKeepAll = new javax.swing.JCheckBoxMenuItem();
        normAggroInhib = new javax.swing.JCheckBoxMenuItem();
        ClearMenu = new javax.swing.JMenu();
        ClearAll = new javax.swing.JMenuItem();
        ClearName = new javax.swing.JMenuItem();
        ClearArgs = new javax.swing.JMenuItem();
        ClearEditor = new javax.swing.JMenuItem();
        ClearPP = new javax.swing.JMenuItem();
        ClearZGRs = new javax.swing.JMenu();
        ClearAllZGRs = new javax.swing.JMenuItem();
        autoClearZGRs = new javax.swing.JCheckBoxMenuItem();
        clearZGRsep = new javax.swing.JPopupMenu.Separator();
        HelpMenu = new javax.swing.JMenu();
        HelpKeywords = new javax.swing.JMenuItem();
        HelpSyntax = new javax.swing.JMenuItem();
        HepModalities = new javax.swing.JMenuItem();
        HelpSep1 = new javax.swing.JPopupMenu.Separator();
        HelpCLI = new javax.swing.JMenuItem();
        HelpGUI = new javax.swing.JMenuItem();
        HelpPlugin = new javax.swing.JMenuItem();
        HelpSep2 = new javax.swing.JPopupMenu.Separator();
        HelpLibraries = new javax.swing.JMenuItem();
        HelpLicense = new javax.swing.JMenuItem();
        HelpAbout = new javax.swing.JMenuItem();
        ppAll.setText("<all>");
        ppAll.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppAllActionPerformed(evt);
            }
        });
        ppPopup.add(ppAll);
        ppNone.setText("<none>");
        ppNone.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppNoneActionPerformed(evt);
            }
        });
        ppPopup.add(ppNone);
        ppPopup.add(ppsep0);
        ppArgs.setSelected(true);
        ppArgs.setText("CEqEA arguments");
        ppPopup.add(ppArgs);
        ppReads.setSelected(true);
        ppReads.setText("specification read");
        ppPopup.add(ppReads);
        ppLex.setSelected(true);
        ppLex.setText("lexer");
        ppPopup.add(ppLex);
        ppSymbols.setSelected(true);
        ppSymbols.setText("symbol parser");
        ppPopup.add(ppSymbols);
        ppMdlts.setSelected(true);
        ppMdlts.setText("modality parser");
        ppPopup.add(ppMdlts);
        ppStrong.setSelected(true);
        ppStrong.setText("strong-free influences");
        ppPopup.add(ppStrong);
        ppInhib.setSelected(true);
        ppInhib.setText("inhibition-free influences");
        ppPopup.add(ppInhib);
        ppLinear.setSelected(true);
        ppLinear.setText("linear influences");
        ppPopup.add(ppLinear);
        ppAtom.setSelected(true);
        ppAtom.setText("atomic influences/reactions");
        ppPopup.add(ppAtom);
        ppVacuo.setSelected(true);
        ppVacuo.setText("uniform in-vacuo transitions");
        ppPopup.add(ppVacuo);
        ppStch.setSelected(true);
        ppStch.setText("stochastic in-vacuo transitions");
        ppPopup.add(ppStch);
        ppDgrms.setSelected(true);
        ppDgrms.setText("diagrams");
        ppPopup.add(ppDgrms);
        ppEq.setSelected(true);
        ppEq.setText("sustained equilibria");
        ppPopup.add(ppEq);
        ppSys.setSelected(true);
        ppSys.setText("systems diagrams");
        ppPopup.add(ppSys);
        ppPhys.setSelected(true);
        ppPhys.setText("physiological MIG");
        ppPopup.add(ppPhys);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(jp.ac.jaist.ceqea.CEq_environment.build);
        tabs.setMinimumSize(new java.awt.Dimension(0, 0));
        nameLabel.setText("Name");
        analyzeButton.setText("analyze");
        analyzeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzeButton_analyze(evt);
            }
        });
        dgrmLabel.setText("Causation points-of-interaction by");
        PoIGroup.add(dgrmCasc);
        dgrmCasc.setSelected(true);
        dgrmCasc.setText("cascading + composition");
        PoIGroup.add(dgrmSS);
        dgrmSS.setText("state-space");
        eqLabel.setText("Inhibitor-sustained equilibria");
        eqLabel.setToolTipText("Indicate sustained equilibria w/boxes. Dashed boxes have out-edges: they are *collapsible*. Angular boxes have no uninhibited in-edges: they are *preventable*.");
        doSubEq.setForeground(java.awt.Color.blue);
        doSubEq.setText("sub-equilibria [I/0/0-sustained]");
        doSubEq.setToolTipText("This option results in boxes with this colour.");
        doPreEq.setForeground(java.awt.Color.red);
        doPreEq.setText("pre-equilibria [0/I/0-sustained]");
        doPreEq.setToolTipText("This option results in boxes with this colour.");
        doIndxEq.setForeground(java.awt.Color.green);
        doIndxEq.setText("ad-hoc equilibria [A/T/N-sustained]");
        doIndxEq.setToolTipText("This option results in boxes with this colour.");
        doIndxEq.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doIndxEqActionPerformed(evt);
            }
        });
        inhibitorsGroup.add(alwaysRadio);
        alwaysRadio.setText("Always-inhibs");
        alwaysRadio.setToolTipText("Click label to move _the rest_ to this category.");
        alwaysRadio.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        alwaysRadio.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alwaysRadioActionPerformed(evt);
            }
        });
        alwaysInhb.setToolTipText("Click label to move _the rest_ to this category.");
        inhibitorsGroup.add(transientRadio);
        transientRadio.setText("Transient-inhibs");
        transientRadio.setToolTipText("Click label to move _the rest_ to this category.");
        transientRadio.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        transientRadio.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transientRadioActionPerformed(evt);
            }
        });
        transientInhb.setToolTipText("Click label to move _the rest_ to this category.");
        inhibitorsGroup.add(neverRadio);
        neverRadio.setText("Never-inhibitors");
        neverRadio.setToolTipText("Click label to move _the rest_ to this category.");
        neverRadio.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        neverRadio.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neverRadioActionPerformed(evt);
            }
        });
        neverInhb.setToolTipText("Click label to move _the rest_ to this category.");
        doPP.setText("certify");
        doPP.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doPPActionPerformed(evt);
            }
        });
        ppStages.setText("stages");
        ppStages.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ppOptionsMouseClicked(evt);
            }
        });
        dgrmSys.setText("systems formation");
        dgrmSys.setToolTipText("Systems formation can also be requested from a given causation diagram, which avoids the combinatorial explosion of this option.");
        dgrmSys.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dgrmSysActionPerformed(evt);
            }
        });
        sysGroup.add(orthgnlCoexist);
        orthgnlCoexist.setSelected(true);
        orthgnlCoexist.setText("orthogonal synchronization points");
        orthgnlCoexist.setToolTipText("No two nodes from different eqs may have incoherent content; also, all eqs must remain strongly-connected under inhibition by the sync content.");
        sysGroup.add(intrlvngCoexist);
        intrlvngCoexist.setText("interleaving synchronization points");
        intrlvngCoexist.setToolTipText("");
        maximalityLabel.setText("  maximality test for sync points");
        maximalityLabel.setToolTipText("Only maximal synchronization points are considered.");
        maxGroup.add(maximalityNode);
        maximalityNode.setText("node level [compact diagrams]");
        maximalityNode.setToolTipText("Nodes/points-of-interaction in nested equilibria may *not* pair up with given equilibria sets multiple times.");
        maxGroup.add(maximalityEq);
        maximalityEq.setText("eq level [dispersed diagrams]");
        maximalityEq.setToolTipText("Nodes/points-of-interaction in nested equilibria *may* pair up with given equilibria sets multiple times.");
        maxGroup.add(maximalityNoMax);
        maximalityNoMax.setText("none [combinatorial diagrams]");
        maximalityNoMax.setToolTipText("No maximality condition is enforced on sets of co-existable equilibria.");
        sysInhibition.setText("enforce lifted inhibition");
        sysInhibition.setToolTipText("Causation-level inhibition is enforced at systems level (presupposes *no way out*).");
        sysSuppress.setText("enforce lifted suppression");
        sysSuppress.setToolTipText("Causation-level inhibition is subject to suppression before determining systems level equilibria (presupposes *no other way out*).");
        sysInter.setText("allow inter-if-intra edges");
        sysInter.setToolTipText("Allow an edge to go between equilibria sets even when used reflexively (presupposes *lack of determinism*).");
        sysTrgtRqrd.setText("indicate target required");
        sysUnits.setText("causation components");
        javax.swing.GroupLayout ceqargumentsLayout = new javax.swing.GroupLayout(ceqarguments);
        ceqarguments.setLayout(ceqargumentsLayout);
        ceqargumentsLayout.setHorizontalGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(ceqargumentsLayout.createSequentialGroup().addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(ceqargumentsLayout.createSequentialGroup().addContainerGap().addComponent(doPP).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(ppStages)).addGroup(ceqargumentsLayout.createSequentialGroup().addGap(26, 26, 26).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(transientRadio).addComponent(alwaysRadio).addComponent(neverRadio)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(alwaysInhb, javax.swing.GroupLayout.Alignment.LEADING).addComponent(transientInhb, javax.swing.GroupLayout.Alignment.LEADING).addComponent(neverInhb, javax.swing.GroupLayout.Alignment.LEADING))).addGroup(ceqargumentsLayout.createSequentialGroup().addContainerGap().addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(ceqargumentsLayout.createSequentialGroup().addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(dgrmLabel).addComponent(eqLabel).addComponent(nameLabel)).addGap(0, 0, Short.MAX_VALUE)).addGroup(ceqargumentsLayout.createSequentialGroup().addGap(12, 12, 12).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(ceqargumentsLayout.createSequentialGroup().addComponent(migname).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(analyzeButton)).addGroup(ceqargumentsLayout.createSequentialGroup().addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(doPreEq).addComponent(doSubEq).addComponent(doIndxEq).addComponent(dgrmCasc).addComponent(dgrmSS)).addGap(0, 49, Short.MAX_VALUE))))))).addGap(16, 16, 16)).addGroup(ceqargumentsLayout.createSequentialGroup().addContainerGap().addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(sysSep).addGroup(ceqargumentsLayout.createSequentialGroup().addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(dgrmSys).addGroup(ceqargumentsLayout.createSequentialGroup().addGap(12, 12, 12).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(intrlvngCoexist).addComponent(orthgnlCoexist).addComponent(sysInhibition).addComponent(sysSuppress).addComponent(sysTrgtRqrd).addComponent(sysInter).addComponent(maximalityLabel).addGroup(ceqargumentsLayout.createSequentialGroup().addGap(19, 19, 19).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(maximalityEq).addComponent(maximalityNode).addComponent(maximalityNoMax))).addComponent(sysUnits)))).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
        ceqargumentsLayout.setVerticalGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(ceqargumentsLayout.createSequentialGroup().addContainerGap().addComponent(nameLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(migname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(analyzeButton)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(dgrmLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(dgrmCasc).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(dgrmSS).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(eqLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(doSubEq).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(doPreEq).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(doIndxEq).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(alwaysRadio).addComponent(alwaysInhb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(transientRadio).addComponent(transientInhb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(neverRadio).addComponent(neverInhb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(ceqargumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(doPP).addComponent(ppStages, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sysSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(dgrmSys).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(orthgnlCoexist).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(intrlvngCoexist).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sysInhibition).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sysSuppress).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sysTrgtRqrd).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sysInter).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(maximalityLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(maximalityNode).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(maximalityEq).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(maximalityNoMax).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sysUnits).addContainerGap(16, Short.MAX_VALUE)));
        arg_scroll.setViewportView(ceqarguments);
        input.setLeftComponent(arg_scroll);
        arg_scroll.getAccessibleContext().setAccessibleParent(input);
        edit_scroll.setPreferredSize(new java.awt.Dimension(350, 600));
        migeditor.setColumns(20);
        migeditor.setFont(new java.awt.Font("Monospaced", 0, 14));
        migeditor.setRows(5);
        migeditor.setMinimumSize(new java.awt.Dimension(0, 0));
        edit_scroll.setViewportView(migeditor);
        input.setRightComponent(edit_scroll);
        tabs.addTab("Analysis", input);
        prettyprinting.setColumns(20);
        prettyprinting.setEditable(false);
        prettyprinting.setFont(new java.awt.Font("Monospaced", 0, 14));
        prettyprinting.setRows(5);
        ppscroll.setViewportView(prettyprinting);
        tabs.addTab("Certificates", ppscroll);
        FileMenu.setText("File");
        NewMI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        NewMI.setText("New");
        NewMI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewMIActionPerformed(evt);
            }
        });
        FileMenu.add(NewMI);
        NewMI.getAccessibleContext().setAccessibleName("NewWindow");
        OpenMI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        OpenMI.setLabel("Open ...");
        OpenMI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenMIActionPerformed(evt);
            }
        });
        FileMenu.add(OpenMI);
        OpenMI.getAccessibleContext().setAccessibleName("OpenFile");
        SaveMI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        SaveMI.setText("Save as ...");
        SaveMI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveMIActionPerformed(evt);
            }
        });
        FileMenu.add(SaveMI);
        FileMenu.add(filesep1);
        QuitMI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        QuitMI.setText("Quit");
        QuitMI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QuitMIActionPerformed(evt);
            }
        });
        FileMenu.add(QuitMI);
        topmenu.add(FileMenu);
        RunMenu.setText("Run");
        AnalyzeMI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        AnalyzeMI.setText("Analyze");
        AnalyzeMI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzeButton_analyze(evt);
            }
        });
        RunMenu.add(AnalyzeMI);
        showUniDgrms.setSelected(true);
        showUniDgrms.setText("Show uniform diagrams");
        RunMenu.add(showUniDgrms);
        RunMenu.add(normLabel);
        normGroup.add(normAlgDNF);
        normAlgDNF.setSelected(true);
        normAlgDNF.setText("algebraic DNF (intended)");
        RunMenu.add(normAlgDNF);
        normGroup.add(normFullDNF);
        normFullDNF.setForeground(java.awt.Color.magenta);
        normFullDNF.setText("full DNF (fast approximation)");
        normFullDNF.setToolTipText("NB! this option may not respect the desired operational semantics: it may unduly sequentialize concurrency.");
        RunMenu.add(normFullDNF);
        RunMenu.add(expandLabel);
        normExpandAll.setForeground(java.awt.Color.pink);
        normExpandAll.setSelected(true);
        normExpandAll.setText("fully expand also top-level |--s");
        normExpandAll.setToolTipText("NB! this option may be expensive and only expands the certification; diagrams are not affected.");
        RunMenu.add(normExpandAll);
        normKeepAll.setForeground(java.awt.Color.pink);
        normKeepAll.setSelected(true);
        normKeepAll.setText("process null-and-void transitions");
        normKeepAll.setToolTipText("NB! this option may be expensive and only expands the certification; diagrams are not affected.");
        RunMenu.add(normKeepAll);
        normAggroInhib.setForeground(java.awt.Color.magenta);
        normAggroInhib.setSelected(true);
        normAggroInhib.setText("preemptive inhibition filtering");
        normAggroInhib.setToolTipText("NB! this option may not respect the desired operational semantics: it may circumvent seeded modalities.");
        RunMenu.add(normAggroInhib);
        topmenu.add(RunMenu);
        ClearMenu.setText("Clear");
        ClearAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        ClearAll.setText("all");
        ClearAll.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearAllActionPerformed(evt);
            }
        });
        ClearMenu.add(ClearAll);
        ClearAll.getAccessibleContext().setAccessibleName("Clear");
        ClearName.setText("name");
        ClearName.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearNameActionPerformed(evt);
            }
        });
        ClearMenu.add(ClearName);
        ClearArgs.setText("arguments");
        ClearArgs.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearArgsActionPerformed(evt);
            }
        });
        ClearMenu.add(ClearArgs);
        ClearEditor.setText("editor");
        ClearEditor.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearEditorActionPerformed(evt);
            }
        });
        ClearMenu.add(ClearEditor);
        ClearPP.setText("certificates");
        ClearPP.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearPPActionPerformed(evt);
            }
        });
        ClearMenu.add(ClearPP);
        ClearZGRs.setText("ZGRViewers");
        ClearAllZGRs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        ClearAllZGRs.setText("all");
        ClearAllZGRs.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearAllZGRsActionPerformed(evt);
            }
        });
        ClearZGRs.add(ClearAllZGRs);
        autoClearZGRs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        autoClearZGRs.setSelected(true);
        autoClearZGRs.setText("auto");
        ClearZGRs.add(autoClearZGRs);
        ClearZGRs.add(clearZGRsep);
        ClearMenu.add(ClearZGRs);
        topmenu.add(ClearMenu);
        HelpMenu.setText("Help");
        HelpKeywords.setText("MIG keywords (browser)");
        HelpKeywords.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpKeywordsActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpKeywords);
        HelpSyntax.setText("MIG syntax (browser)");
        HelpSyntax.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpSyntaxActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpSyntax);
        HepModalities.setText("MIG modalities (browser)");
        HepModalities.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HepModalitiesActionPerformed(evt);
            }
        });
        HelpMenu.add(HepModalities);
        HelpMenu.add(HelpSep1);
        HelpCLI.setText("command line");
        HelpCLI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpCLIActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpCLI);
        HelpGUI.setText("graphical user interface");
        HelpGUI.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpGUIActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpGUI);
        HelpPlugin.setText("ZGRViewer plugin");
        HelpPlugin.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpPluginActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpPlugin);
        HelpMenu.add(HelpSep2);
        HelpLibraries.setText("libraries");
        HelpLibraries.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpLibrariesActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpLibraries);
        HelpLicense.setText("license");
        HelpLicense.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpLicenseActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpLicense);
        HelpAbout.setText("about");
        HelpAbout.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpAboutActionPerformed(evt);
            }
        });
        HelpMenu.add(HelpAbout);
        topmenu.add(HelpMenu);
        setJMenuBar(topmenu);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 952, Short.MAX_VALUE).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE).addContainerGap()));
        pack();
    }

    private void clearAll() {
        this.clearInput();
        this.clearPP();
        this.clearZgrViews();
    }

    private void clearInput() {
        this.clearName();
        this.setArgs(CEq_environment.defaultEnv);
        this.clearEditor();
    }

    private void clearZgrViews() {
        ArrayList<String> processes = new ArrayList(this.zgrProcesses.keySet());
        for (String ID : processes) clearZgrViews(ID);
    }

    private void clearZgrViews(String runID) {
        for (Process zgrViewer : zgrProcesses.get(runID)) zgrViewer.destroy();
        zgrProcesses.remove(runID);
        ClearZGRs.remove(clearZGRmenu.get(runID));
        clearZGRmenu.remove(runID);
    }

    private void clearName() {
        migname.setText("");
    }

    private void clearEditor() {
        migeditor.setText("");
    }

    private void clearPP() {
        tabs.setEnabledAt(tabs.indexOfComponent(ppscroll), false);
        prettyprinting.setText("");
    }

    private void setInput(CEq_specification spec) {
        migname.setText(spec.name());
        migeditor.setText(spec.spcfctnText());
        migeditor.setCaretPosition(0);
    }

    private void setArgs(CEq_environment env) {
        this.dgrmCasc.setSelected(env.doModel(CEq_MODELS.Casc));
        this.dgrmSys.setSelected(env.doModel(CEq_MODELS.SysEq) || env.doModel(CEq_MODELS.SysNode) || env.doModel(CEq_MODELS.SysNoMax));
        this.dgrmSS.setSelected(env.doModel(CEq_MODELS.Sspc));
        this.sysInhibition.setSelected(env.sysInhibit());
        this.sysSuppress.setSelected(env.sysSuppress());
        this.sysTrgtRqrd.setSelected(env.sysTrgtRqrd());
        this.sysInter.setSelected(env.sysInter());
        this.maximalityNoMax.setSelected(env.doModel(CEq_MODELS.SysNoMax));
        this.maximalityEq.setSelected(env.doModel(CEq_MODELS.SysEq));
        this.maximalityNode.setSelected(env.doModel(CEq_MODELS.SysNode));
        if (!env.doModel(CEq_MODELS.SysEq) && !env.doModel(CEq_MODELS.SysNode) && !env.doModel(CEq_MODELS.SysNoMax)) this.maximalityNode.setSelected(true);
        this.sysUnits.setSelected(env.sysUnits());
        this.doSubEq.setSelected(env.doEq(SUSTAIN.Sub));
        this.doPreEq.setSelected(env.doEq(SUSTAIN.Pre));
        this.doIndxEq.setSelected(env.doEq(SUSTAIN.Indx));
        this.neverRadio.doClick();
        this.normFullDNF.setSelected(env.doNorm(NORM.FullDNF));
        this.normAlgDNF.setSelected(env.doNorm(NORM.AlgDNF));
        this.normExpandAll.setSelected(env.doNorm(NORM.ExpandAll));
        this.normKeepAll.setSelected(env.doNorm(NORM.KeepAll));
        this.normAggroInhib.setSelected(env.doNorm(NORM.AggroInhib));
        this.showUniDgrms.setSelected(env.show(CEq_INFO.Uniform));
        this.autoClearZGRs.setSelected(false);
        boolean ppSpcfd = false;
        for (CEq_STAGES stg : CEq_STAGES.values()) if (env.ppRqstd(stg)) {
            ppSpcfd = true;
            break;
        }
        if (ppSpcfd) {
            ppArgs.setSelected(env.ppRqstd(CEq_STAGES.Arg));
            ppReads.setSelected(env.ppRqstd(CEq_STAGES.Read));
            ppLex.setSelected(env.ppRqstd(CEq_STAGES.Lex));
            ppSymbols.setSelected(env.ppRqstd(CEq_STAGES.Symb));
            ppMdlts.setSelected(env.ppRqstd(CEq_STAGES.Mdlt));
            ppStrong.setSelected(env.ppRqstd(CEq_STAGES.Strong));
            ppInhib.setSelected(env.ppRqstd(CEq_STAGES.Inhib));
            ppLinear.setSelected(env.ppRqstd(CEq_STAGES.Linear));
            ppAtom.setSelected(env.ppRqstd(CEq_STAGES.Atom));
            ppVacuo.setSelected(env.ppRqstd(CEq_STAGES.Vacuo));
            ppStch.setSelected(env.ppRqstd(CEq_STAGES.Stch));
            ppDgrms.setSelected(env.ppRqstd(CEq_STAGES.Dgrm));
            ppEq.setSelected(env.ppRqstd(CEq_STAGES.Eq));
            ppSys.setSelected(env.ppRqstd(CEq_STAGES.Sys));
            ppPhys.setSelected(env.ppRqstd(CEq_STAGES.Phys));
            doPP.setSelected(true);
        } else {
            ppSetSelected(true);
            doPP.setSelected(true);
        }
        ppLex.setEnabled(false);
        ppLex.setSelected(false);
        doIndxEqActionPerformed(null);
        doPPActionPerformed(null);
        sysShowings = 0;
        dgrmSysActionPerformed(null);
    }

    private CEq_environment readArgs() {
        boolean[] doStage = new boolean[CEq_STAGES.values().length];
        for (CEq_STAGES stg : CEq_STAGES.values()) doStage[stg.ordinal()] = true;
        boolean[] doEq = new boolean[SUSTAIN.values().length];
        doEq[SUSTAIN.Sub.ordinal()] = this.doSubEq.isSelected();
        doEq[SUSTAIN.Pre.ordinal()] = this.doPreEq.isSelected();
        doEq[SUSTAIN.Indx.ordinal()] = this.doIndxEq.isSelected();
        boolean[] doModel = new boolean[CEq_MODELS.values().length];
        doModel[CEq_MODELS.Casc.ordinal()] = dgrmCasc.isSelected();
        doModel[CEq_MODELS.SysNode.ordinal()] = dgrmSys.isSelected() && this.orthgnlCoexist.isSelected() && maximalityNode.isSelected();
        doModel[CEq_MODELS.SysEq.ordinal()] = dgrmSys.isSelected() && this.orthgnlCoexist.isSelected() && maximalityEq.isSelected();
        doModel[CEq_MODELS.SysNoMax.ordinal()] = dgrmSys.isSelected() && this.orthgnlCoexist.isSelected() && maximalityNoMax.isSelected();
        doModel[CEq_MODELS.Sspc.ordinal()] = dgrmSS.isSelected();
        boolean[] doNorm = new boolean[NORM.values().length];
        doNorm[NORM.AlgDNF.ordinal()] = this.normAlgDNF.isSelected();
        doNorm[NORM.FullDNF.ordinal()] = this.normFullDNF.isSelected();
        doNorm[NORM.ExpandAll.ordinal()] = this.normExpandAll.isSelected();
        doNorm[NORM.KeepAll.ordinal()] = this.normKeepAll.isSelected();
        doNorm[NORM.AggroInhib.ordinal()] = this.normAggroInhib.isSelected();
        boolean[] pp = new boolean[CEq_STAGES.values().length];
        pp[CEq_STAGES.Arg.ordinal()] = ppArgs.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Read.ordinal()] = ppReads.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Lex.ordinal()] = ppLex.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Symb.ordinal()] = ppSymbols.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Mdlt.ordinal()] = ppMdlts.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Strong.ordinal()] = ppStrong.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Inhib.ordinal()] = ppInhib.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Linear.ordinal()] = ppLinear.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Atom.ordinal()] = ppAtom.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Vacuo.ordinal()] = ppVacuo.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Stch.ordinal()] = ppStch.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Dgrm.ordinal()] = ppDgrms.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Eq.ordinal()] = ppEq.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Sys.ordinal()] = ppSys.isSelected() && doPP.isSelected();
        pp[CEq_STAGES.Phys.ordinal()] = ppPhys.isSelected() && doPP.isSelected();
        boolean[] show = new boolean[CEq_INFO.values().length];
        for (CEq_INFO info : CEq_INFO.values()) show[info.ordinal()] = false;
        show[CEq_INFO.Uniform.ordinal()] = this.showUniDgrms.isSelected();
        return new CEq_environment(doStage, doEq, doModel, doNorm, sysInhibition.isSelected(), sysSuppress.isSelected(), sysTrgtRqrd.isSelected(), sysInter.isSelected(), sysUnits.isSelected(), pp, show, false);
    }

    private void importFileList(List<File> filelist) {
        boolean first = true;
        for (File file : filelist) {
            if (!file.exists() || !file.getName().endsWith(".mig")) continue;
            CEq_specification spec = new CEq_specification(file, CEq_environment.defaultEnv);
            if (first) {
                this.clearInput();
                this.clearPP();
                this.setInput(spec);
                first = false;
            } else CEq_0_cli.startGUI(spec);
        }
    }

    private void analyzeButton_analyze(java.awt.event.ActionEvent evt) {
        analyzeButton.setEnabled(false);
        clearPP();
        if (this.autoClearZGRs.isSelected()) this.clearZgrViews();
        String name = migname.getText();
        if (name.isEmpty()) name = "NN";
        CEq_1_perSpcfctn perS = new CEq_1_perSpcfctn(new CEq_specification(name, migeditor.getText(), (!this.doIndxEq.isSelected() || this.alwaysRadio.isSelected()) ? null : this.alwaysInhb.getText(), (!this.doIndxEq.isSelected() || this.transientRadio.isSelected()) ? null : this.transientInhb.getText(), (!this.doIndxEq.isSelected() || this.neverRadio.isSelected()) ? null : this.neverInhb.getText(), this.readArgs()));
        perS.start();
        ArrayList<StringBuilder> ppSBs = CEq_1_perSpcfctn.wait_cleanup(perS);
        for (CEq_2_perDiagram perD : perS.dgrmProcesses()) {
            this.visualizeDgrm(perD.dgrm());
            for (CEq_systDgrm sD : perD.systDgrms) this.visualizeDgrm(sD);
        }
        if (perS.uniformCasc != null) this.visualizeDgrm(perS.uniformCasc);
        if (perS.uniformSspc != null) this.visualizeDgrm(perS.uniformSspc);
        boolean ppd = false;
        for (StringBuilder str : ppSBs) if (str.length() > 0) {
            prettyprinting.append(str.toString());
            ppd = true;
        }
        if (ppd) {
            tabs.setEnabledAt(tabs.indexOfComponent(ppscroll), true);
            prettyprinting.setCaretPosition(0);
        }
        analyzeButton.setEnabled(true);
    }

    private void visualizeDgrm(CEq_diagram dgrm) {
        if (dgrm.dot == null) return;
        String fullDgrm = canonical_tmp + dgrm.fnBase;
        String freshName = null;
        File outFile = null;
        int i = 1;
        do {
            freshName = fullDgrm + i++ + ".dot";
            outFile = new File(freshName);
        } while (outFile.exists());
        try {
            outFile.createNewFile();
            outFile.deleteOnExit();
            Files.setContents(outFile, dgrm.dot);
        } catch (Exception e) {
            CEq_feedback.exit(CEq_feedback.ERR.File_or_generic_error, e);
        }
        try {
            String[] cmd = (dgrm instanceof CEq_systDgrm) ? CEq_system.ZGRVvanilla(freshName) : CEq_system.ZGRVplugin(freshName);
            Process zgr = Runtime.getRuntime().exec(cmd);
            final String runID = dgrm.process.spcfctn.name() + "_" + dgrm.dgrmType() + "_run" + (i - 1);
            if (zgrProcesses.get(runID) == null) {
                zgrProcesses.put(runID, new HashSet());
                JMenuItem clearRunID = new javax.swing.JMenuItem();
                clearZGRmenu.put(runID, clearRunID);
                clearRunID.setText(runID);
                clearRunID.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        clearZgrViews(runID);
                    }
                });
                ClearZGRs.add(clearRunID);
            }
            zgrProcesses.get(runID).add(zgr);
        } catch (Exception e) {
            CEq_feedback.exit(CEq_feedback.ERR.CEqUI_environment_error, e);
        }
    }

    private void NewMIActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_cli.startGUI(CEq_environment.defaultEnv);
    }

    private void ClearAllActionPerformed(java.awt.event.ActionEvent evt) {
        clearAll();
    }

    private void ClearNameActionPerformed(java.awt.event.ActionEvent evt) {
        clearName();
    }

    private void ClearArgsActionPerformed(java.awt.event.ActionEvent evt) {
        this.setArgs(CEq_environment.defaultEnv);
    }

    private void ClearEditorActionPerformed(java.awt.event.ActionEvent evt) {
        clearEditor();
    }

    private void ClearPPActionPerformed(java.awt.event.ActionEvent evt) {
        clearPP();
    }

    private void OpenMIActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fileopen = new JFileChooser();
        fileopen.setMultiSelectionEnabled(true);
        FileFilter filter = new FileNameExtensionFilter("mig files", "mig");
        fileopen.addChoosableFileFilter(filter);
        int ret = fileopen.showDialog(null, "Open file");
        if (ret == JFileChooser.APPROVE_OPTION) {
            importFileList(Arrays.asList(fileopen.getSelectedFiles()));
        }
    }

    private void QuitMIActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void ppOptionsMouseClicked(java.awt.event.MouseEvent evt) {
        this.ppPopup.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void ppSetSelected(boolean val) {
        ppArgs.setSelected(val);
        ppReads.setSelected(val);
        ppSymbols.setSelected(val);
        ppMdlts.setSelected(val);
        ppStrong.setSelected(val);
        ppInhib.setSelected(val);
        ppLinear.setSelected(val);
        ppAtom.setSelected(val);
        ppVacuo.setSelected(val);
        ppStch.setSelected(val);
        ppDgrms.setSelected(val);
        ppEq.setSelected(val);
        ppSys.setSelected(val);
        ppPhys.setSelected(val);
    }

    private void ppAllActionPerformed(java.awt.event.ActionEvent evt) {
        ppSetSelected(true);
    }

    private void ppNoneActionPerformed(java.awt.event.ActionEvent evt) {
        ppSetSelected(false);
    }

    private void SaveMIActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fileopen = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter("mig files", "mig");
        fileopen.addChoosableFileFilter(filter);
        int ret = fileopen.showDialog(null, "Save editor as ...");
        File file = fileopen.getSelectedFile();
        if (ret == JFileChooser.APPROVE_OPTION) {
            if (file.exists()) {
                CEq_feedback.warning("The selected file already exists; please (choose a directory and) type in a fresh name.");
                return;
            }
            if (!file.getName().endsWith(".mig")) {
                CEq_feedback.warning("MIG specifications must end with .mig; please try again.");
                return;
            }
            try {
                file.createNewFile();
                Files.setContents(file, migeditor.getText());
            } catch (Exception e) {
                CEq_feedback.exit(CEq_feedback.ERR.File_or_generic_error, e);
            }
        }
    }

    private void doIndxEqActionPerformed(java.awt.event.ActionEvent evt) {
        this.alwaysRadio.setEnabled(this.doIndxEq.isSelected());
        this.transientRadio.setEnabled(this.doIndxEq.isSelected());
        this.neverRadio.setEnabled(this.doIndxEq.isSelected());
        this.alwaysInhb.setEnabled(this.doIndxEq.isSelected() && !this.alwaysRadio.isSelected());
        this.transientInhb.setEnabled(this.doIndxEq.isSelected() && !this.transientRadio.isSelected());
        this.neverInhb.setEnabled(this.doIndxEq.isSelected() && !this.neverRadio.isSelected());
    }

    private void doPPActionPerformed(java.awt.event.ActionEvent evt) {
        this.ppStages.setEnabled(this.doPP.isSelected());
    }

    private void ClearAllZGRsActionPerformed(java.awt.event.ActionEvent evt) {
        this.clearZgrViews();
    }

    private void showInhibs(JTextField inhibs, JRadioButton radio) {
        if (!inhibs.isEnabled()) {
            inhibs.setHorizontalAlignment(javax.swing.JTextField.LEADING);
            inhibs.setText("");
            inhibs.setFont(new Font(inhibs.getFont().getName(), Font.PLAIN, inhibs.getFont().getSize()));
            inhibs.setEnabled(true);
        }
        radio.setFont(new Font(radio.getFont().getName(), Font.BOLD, radio.getFont().getSize()));
    }

    private void hideInhibs(JTextField inhibs, JRadioButton radio) {
        inhibs.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        inhibs.setText("the rest");
        inhibs.setFont(new Font(inhibs.getFont().getName(), Font.ITALIC, inhibs.getFont().getSize()));
        inhibs.setEnabled(false);
        radio.setFont(new Font(radio.getFont().getName(), Font.ITALIC, radio.getFont().getSize()));
    }

    private void alwaysRadioActionPerformed(java.awt.event.ActionEvent evt) {
        hideInhibs(this.alwaysInhb, this.alwaysRadio);
        showInhibs(this.transientInhb, this.transientRadio);
        showInhibs(this.neverInhb, this.neverRadio);
    }

    private void transientRadioActionPerformed(java.awt.event.ActionEvent evt) {
        showInhibs(this.alwaysInhb, this.alwaysRadio);
        hideInhibs(this.transientInhb, this.transientRadio);
        showInhibs(this.neverInhb, this.neverRadio);
    }

    private void neverRadioActionPerformed(java.awt.event.ActionEvent evt) {
        showInhibs(this.alwaysInhb, this.alwaysRadio);
        showInhibs(this.transientInhb, this.transientRadio);
        hideInhibs(this.neverInhb, this.neverRadio);
    }

    private void HelpLicenseActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_gui.info(CEq_environment.license(), "CEqEA license");
    }

    private void HelpLibrariesActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_gui.info(CEq_environment.libraries(), "CEqEA libraries and licenses");
    }

    private void HelpCLIActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_gui.info(CEq_environment.cli(), "CEqEA command-line interface");
    }

    private void HelpGUIActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_gui.info(CEq_environment.gui(), "CEqEA graphical user interface");
    }

    private void HelpPluginActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_gui.info(CEq_environment.plugin(), "CEqEA plugin to ZGRViewer");
    }

    private void HelpAboutActionPerformed(java.awt.event.ActionEvent evt) {
        CEq_0_gui.info(CEq_environment.about(), "about CEqEA and its ZGRViewer plugin");
    }

    private void HelpKeywordsActionPerformed(java.awt.event.ActionEvent evt) {
        HelpMIG("symbols.html");
    }

    private void HelpSyntaxActionPerformed(java.awt.event.ActionEvent evt) {
        HelpMIG("syntax.html");
    }

    private void HepModalitiesActionPerformed(java.awt.event.ActionEvent evt) {
        HelpMIG("modalities.html");
    }

    private int sysShowings = 0;

    private void dgrmSysActionPerformed(java.awt.event.ActionEvent evt) {
        boolean sysValue = this.dgrmSys.isSelected();
        this.sysInhibition.setEnabled(sysValue);
        this.sysSuppress.setEnabled(sysValue);
        this.sysTrgtRqrd.setEnabled(sysValue);
        this.sysInter.setEnabled(sysValue);
        this.orthgnlCoexist.setEnabled(sysValue);
        this.intrlvngCoexist.setEnabled(false);
        this.maximalityLabel.setEnabled(sysValue);
        this.maximalityNode.setEnabled(sysValue);
        this.maximalityEq.setEnabled(sysValue);
        this.maximalityNoMax.setEnabled(sysValue);
        this.sysUnits.setEnabled(sysValue);
        if (sysShowings == 1) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int n = JOptionPane.showConfirmDialog(null, "Requesting this option may result in a very large number of systems diagrams, each with opaque origins.\n" + "In most cases it is better to request specific systems diagrams from within a given causation diagram.\n" + "Are you sure you want to request systems diagrams here?", "Confirmation", JOptionPane.YES_NO_OPTION);
                    if (n != JOptionPane.YES_OPTION) {
                        dgrmSys.doClick();
                        sysShowings = 1;
                    }
                }
            });
        }
        sysShowings++;
    }

    private void HelpMIG(String str) {
        boolean browseOK = false;
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                browseOK = true;
                try {
                    java.net.URI uri = new java.net.URI("file://" + CEq_system.MIG_dir + str);
                    desktop.browse(uri);
                } catch (Exception e) {
                    browseOK = false;
                }
            }
        }
        if (!browseOK) CEq_0_gui.info(CEq_environment.mig(), "MIG language");
    }

    private javax.swing.JMenuItem AnalyzeMI;

    private javax.swing.JMenuItem ClearAll;

    private javax.swing.JMenuItem ClearAllZGRs;

    private javax.swing.JMenuItem ClearArgs;

    private javax.swing.JMenuItem ClearEditor;

    private javax.swing.JMenu ClearMenu;

    private javax.swing.JMenuItem ClearName;

    private javax.swing.JMenuItem ClearPP;

    private javax.swing.JMenu ClearZGRs;

    private javax.swing.JMenu FileMenu;

    private javax.swing.JMenuItem HelpAbout;

    private javax.swing.JMenuItem HelpCLI;

    private javax.swing.JMenuItem HelpGUI;

    private javax.swing.JMenuItem HelpKeywords;

    private javax.swing.JMenuItem HelpLibraries;

    private javax.swing.JMenuItem HelpLicense;

    private javax.swing.JMenu HelpMenu;

    private javax.swing.JMenuItem HelpPlugin;

    private javax.swing.JPopupMenu.Separator HelpSep1;

    private javax.swing.JPopupMenu.Separator HelpSep2;

    private javax.swing.JMenuItem HelpSyntax;

    private javax.swing.JMenuItem HepModalities;

    private javax.swing.JMenuItem NewMI;

    private javax.swing.JMenuItem OpenMI;

    private javax.swing.ButtonGroup PoIGroup;

    private javax.swing.JMenuItem QuitMI;

    private javax.swing.JMenu RunMenu;

    private javax.swing.JMenuItem SaveMI;

    private javax.swing.JTextField alwaysInhb;

    private javax.swing.JRadioButton alwaysRadio;

    private javax.swing.JButton analyzeButton;

    private javax.swing.JScrollPane arg_scroll;

    private javax.swing.JCheckBoxMenuItem autoClearZGRs;

    private javax.swing.JPanel ceqarguments;

    private javax.swing.JPopupMenu.Separator clearZGRsep;

    private javax.swing.JRadioButton dgrmCasc;

    private javax.swing.JLabel dgrmLabel;

    private javax.swing.JRadioButton dgrmSS;

    private javax.swing.JCheckBox dgrmSys;

    private javax.swing.JCheckBox doIndxEq;

    private javax.swing.JCheckBox doPP;

    private javax.swing.JCheckBox doPreEq;

    private javax.swing.JCheckBox doSubEq;

    private javax.swing.JScrollPane edit_scroll;

    private javax.swing.JLabel eqLabel;

    private javax.swing.JPopupMenu.Separator expandLabel;

    private javax.swing.JPopupMenu.Separator filesep1;

    private javax.swing.ButtonGroup inhibitorsGroup;

    private javax.swing.JSplitPane input;

    private javax.swing.JRadioButton intrlvngCoexist;

    private javax.swing.ButtonGroup maxGroup;

    private javax.swing.JRadioButton maximalityEq;

    private javax.swing.JLabel maximalityLabel;

    private javax.swing.JRadioButton maximalityNoMax;

    private javax.swing.JRadioButton maximalityNode;

    private javax.swing.JTextArea migeditor;

    private javax.swing.JTextField migname;

    private javax.swing.JLabel nameLabel;

    private javax.swing.JTextField neverInhb;

    private javax.swing.JRadioButton neverRadio;

    private javax.swing.JCheckBoxMenuItem normAggroInhib;

    private javax.swing.JRadioButtonMenuItem normAlgDNF;

    private javax.swing.JCheckBoxMenuItem normExpandAll;

    private javax.swing.JRadioButtonMenuItem normFullDNF;

    private javax.swing.ButtonGroup normGroup;

    private javax.swing.JCheckBoxMenuItem normKeepAll;

    private javax.swing.JPopupMenu.Separator normLabel;

    private javax.swing.JRadioButton orthgnlCoexist;

    private javax.swing.JMenuItem ppAll;

    private javax.swing.JCheckBoxMenuItem ppArgs;

    private javax.swing.JCheckBoxMenuItem ppAtom;

    private javax.swing.JCheckBoxMenuItem ppDgrms;

    private javax.swing.JCheckBoxMenuItem ppEq;

    private javax.swing.JCheckBoxMenuItem ppInhib;

    private javax.swing.JCheckBoxMenuItem ppLex;

    private javax.swing.JCheckBoxMenuItem ppLinear;

    private javax.swing.JCheckBoxMenuItem ppMdlts;

    private javax.swing.JMenuItem ppNone;

    private javax.swing.JCheckBoxMenuItem ppPhys;

    private javax.swing.JPopupMenu ppPopup;

    private javax.swing.JCheckBoxMenuItem ppReads;

    private javax.swing.JButton ppStages;

    private javax.swing.JCheckBoxMenuItem ppStch;

    private javax.swing.JCheckBoxMenuItem ppStrong;

    private javax.swing.JCheckBoxMenuItem ppSymbols;

    private javax.swing.JCheckBoxMenuItem ppSys;

    private javax.swing.JCheckBoxMenuItem ppVacuo;

    private javax.swing.JScrollPane ppscroll;

    private javax.swing.JPopupMenu.Separator ppsep0;

    private javax.swing.JTextArea prettyprinting;

    private javax.swing.JCheckBoxMenuItem showUniDgrms;

    private javax.swing.ButtonGroup sysGroup;

    private javax.swing.JCheckBox sysInhibition;

    private javax.swing.JCheckBox sysInter;

    private javax.swing.JSeparator sysSep;

    private javax.swing.JCheckBox sysSuppress;

    private javax.swing.JCheckBox sysTrgtRqrd;

    private javax.swing.JCheckBox sysUnits;

    private javax.swing.JTabbedPane tabs;

    private javax.swing.JMenuBar topmenu;

    private javax.swing.JTextField transientInhb;

    private javax.swing.JRadioButton transientRadio;

    private static void info(final String info, final String title) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int vwblHeight = (int) (0.5 * screenSize.height);
                int vwblWidth = (int) (0.5 * screenSize.width);
                String[] info_lines = info.split("\n");
                int max = -1;
                int index_max = -1;
                for (int i = 0; i < info_lines.length; i++) if (info_lines[i].length() > max) {
                    max = info_lines[i].length();
                    index_max = i;
                }
                JTextArea info_area = new JTextArea(info);
                info_area.setCaretPosition(0);
                info_area.setEditable(false);
                int height = info_area.getFontMetrics(info_area.getFont()).getHeight() * (info_area.getLineCount() + 1);
                int width = info_area.getFontMetrics(info_area.getFont()).stringWidth(info_lines[index_max]) + 200;
                JScrollPane info_scroll = new JScrollPane(info_area);
                info_scroll.setPreferredSize(new Dimension(vwblWidth < width ? vwblWidth : width, vwblHeight < height ? vwblHeight : height));
                JOptionPane.showMessageDialog(null, info_scroll, "Information: " + title, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
