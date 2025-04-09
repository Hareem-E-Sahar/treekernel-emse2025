package artic.gui;

import artic.model.ModelFacade;
import artic.util.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The application's main frame.
 */
public class ArticEditorView extends FrameView {

    private static final String TEST_REGEXP = "(\\S+)\\((\\d+)\\)";

    private XMLFileFilter xmlFileFilter = new XMLFileFilter();

    private Pattern testPattern;

    public ArticEditorView(SingleFrameApplication app) {
        super(app);
        initComponents();
        ArticEditor ae = ArticEditor.getApplication();
        Map<String, JPanel> panelMap = ae.getPanelMap();
        panelMap.put(ArticEditor.PANEL_TRANSITION, transitionPanel);
        panelMap.put(ArticEditor.PANEL_SPECIAL_TRANSITION, specialTransitionPanel);
        panelMap.put(ArticEditor.PANEL_EQUATION, equationPanel);
        Logger.getInstance().setTextPane(messagesTextPane);
        loadOptions();
        messagesTextPane2.setStyledDocument(messagesTextPane.getStyledDocument());
        testPattern = Pattern.compile(TEST_REGEXP);
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = ArticEditor.getApplication().getMainFrame();
            aboutBox = new ArticEditorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ArticEditor.getApplication().show(aboutBox);
    }

    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        postureListTextField = new javax.swing.JTextField();
        postureListLabel = new javax.swing.JLabel();
        testButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        mainTabbedPane = new javax.swing.JTabbedPane();
        posturePanel = new artic.gui.PosturePanel();
        equationPanel = new artic.gui.EquationPanel();
        transitionPanel = new artic.gui.TransitionPanel();
        specialTransitionPanel = new artic.gui.TransitionPanel();
        rulePanel = new artic.gui.RulePanel();
        testGraphPanel = new artic.gui.TestGraphPanel();
        messagesPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        messagesTextPane2 = new javax.swing.JTextPane();
        optionPanel = new javax.swing.JPanel();
        synthPathLabel = new javax.swing.JLabel();
        synthOptionsLabel = new javax.swing.JLabel();
        tempDirLabel = new javax.swing.JLabel();
        synthPathTextField = new javax.swing.JTextField();
        synthOptionTextField = new javax.swing.JTextField();
        tempDirTextField = new javax.swing.JTextField();
        setOptionsButton = new javax.swing.JButton();
        chooseSynthPathButton = new javax.swing.JButton();
        chooseTempDirButton = new javax.swing.JButton();
        chooseAudioPlayerPathButton = new javax.swing.JButton();
        audioPlayerTextField = new javax.swing.JTextField();
        audioPlayerPathLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messagesTextPane = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        reloadTestGraphMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        openFileChooser = new javax.swing.JFileChooser();
        saveFileChooser = new javax.swing.JFileChooser();
        synthPathFileChooser = new javax.swing.JFileChooser();
        tempDirFileChooser = new javax.swing.JFileChooser();
        audioPlayerPathFileChooser = new javax.swing.JFileChooser();
        mainPanel.setName("mainPanel");
        jPanel4.setName("jPanel4");
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(artic.gui.ArticEditor.class).getContext().getResourceMap(ArticEditorView.class);
        postureListTextField.setFont(resourceMap.getFont("postureListTextField.font"));
        postureListTextField.setText(resourceMap.getString("postureListTextField.text"));
        postureListTextField.setName("postureListTextField");
        postureListTextField.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postureListTextFieldActionPerformed(evt);
            }
        });
        postureListLabel.setText(resourceMap.getString("postureListLabel.text"));
        postureListLabel.setName("postureListLabel");
        testButton.setText(resourceMap.getString("testButton.text"));
        testButton.setName("testButton");
        testButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().addContainerGap().add(postureListLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(postureListTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(testButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 123, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().addContainerGap().add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(postureListLabel).add(testButton).add(postureListTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jSplitPane1.setDividerSize(8);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1");
        jSplitPane1.setOneTouchExpandable(true);
        mainTabbedPane.setName("mainTabbedPane");
        posturePanel.setName("posturePanel");
        mainTabbedPane.addTab(resourceMap.getString("posturePanel.TabConstraints.tabTitle"), posturePanel);
        equationPanel.setName("equationPanel");
        mainTabbedPane.addTab(resourceMap.getString("equationPanel.TabConstraints.tabTitle"), equationPanel);
        transitionPanel.setName("transitionPanel");
        mainTabbedPane.addTab(resourceMap.getString("transitionPanel.TabConstraints.tabTitle"), transitionPanel);
        specialTransitionPanel.setName("specialTransitionPanel");
        mainTabbedPane.addTab(resourceMap.getString("specialTransitionPanel.TabConstraints.tabTitle"), specialTransitionPanel);
        rulePanel.setName("rulePanel");
        mainTabbedPane.addTab(resourceMap.getString("rulePanel.TabConstraints.tabTitle"), rulePanel);
        testGraphPanel.setName("testGraphPanel");
        mainTabbedPane.addTab(resourceMap.getString("testGraphPanel.TabConstraints.tabTitle"), testGraphPanel);
        messagesPanel.setName("messagesPanel");
        jScrollPane2.setName("jScrollPane2");
        messagesTextPane2.setEditable(false);
        messagesTextPane2.setFont(resourceMap.getFont("messagesTextPane2.font"));
        messagesTextPane2.setName("messagesTextPane2");
        jScrollPane2.setViewportView(messagesTextPane2);
        org.jdesktop.layout.GroupLayout messagesPanelLayout = new org.jdesktop.layout.GroupLayout(messagesPanel);
        messagesPanel.setLayout(messagesPanelLayout);
        messagesPanelLayout.setHorizontalGroup(messagesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(messagesPanelLayout.createSequentialGroup().addContainerGap().add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 988, Short.MAX_VALUE).addContainerGap()));
        messagesPanelLayout.setVerticalGroup(messagesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(messagesPanelLayout.createSequentialGroup().addContainerGap().add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE).addContainerGap()));
        mainTabbedPane.addTab(resourceMap.getString("messagesPanel.TabConstraints.tabTitle"), messagesPanel);
        optionPanel.setName("optionPanel");
        synthPathLabel.setText(resourceMap.getString("synthPathLabel.text"));
        synthPathLabel.setName("synthPathLabel");
        synthOptionsLabel.setText(resourceMap.getString("synthOptionsLabel.text"));
        synthOptionsLabel.setName("synthOptionsLabel");
        tempDirLabel.setText(resourceMap.getString("tempDirLabel.text"));
        tempDirLabel.setName("tempDirLabel");
        synthPathTextField.setFont(resourceMap.getFont("synthPathTextField.font"));
        synthPathTextField.setText(resourceMap.getString("synthPathTextField.text"));
        synthPathTextField.setName("synthPathTextField");
        synthOptionTextField.setColumns(10);
        synthOptionTextField.setFont(resourceMap.getFont("synthOptionTextField.font"));
        synthOptionTextField.setText(resourceMap.getString("synthOptionTextField.text"));
        synthOptionTextField.setName("synthOptionTextField");
        tempDirTextField.setFont(resourceMap.getFont("tempDirTextField.font"));
        tempDirTextField.setText(resourceMap.getString("tempDirTextField.text"));
        tempDirTextField.setName("tempDirTextField");
        setOptionsButton.setText(resourceMap.getString("setOptionsButton.text"));
        setOptionsButton.setName("setOptionsButton");
        setOptionsButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setOptionsButtonActionPerformed(evt);
            }
        });
        chooseSynthPathButton.setText(resourceMap.getString("chooseSynthPathButton.text"));
        chooseSynthPathButton.setName("chooseSynthPathButton");
        chooseSynthPathButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseSynthPathButtonActionPerformed(evt);
            }
        });
        chooseTempDirButton.setText(resourceMap.getString("chooseTempDirButton.text"));
        chooseTempDirButton.setName("chooseTempDirButton");
        chooseTempDirButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseTempDirButtonActionPerformed(evt);
            }
        });
        chooseAudioPlayerPathButton.setText(resourceMap.getString("chooseAudioPlayerPathButton.text"));
        chooseAudioPlayerPathButton.setName("chooseAudioPlayerPathButton");
        chooseAudioPlayerPathButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseAudioPlayerPathButtonActionPerformed(evt);
            }
        });
        audioPlayerTextField.setFont(resourceMap.getFont("audioPlayerTextField.font"));
        audioPlayerTextField.setText(resourceMap.getString("audioPlayerTextField.text"));
        audioPlayerTextField.setName("audioPlayerTextField");
        audioPlayerPathLabel.setText(resourceMap.getString("audioPlayerPathLabel.text"));
        audioPlayerPathLabel.setName("audioPlayerPathLabel");
        org.jdesktop.layout.GroupLayout optionPanelLayout = new org.jdesktop.layout.GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(optionPanelLayout.createSequentialGroup().addContainerGap().add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, setOptionsButton).add(optionPanelLayout.createSequentialGroup().add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(synthOptionsLabel).add(synthPathLabel).add(tempDirLabel).add(audioPlayerPathLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(synthOptionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(org.jdesktop.layout.GroupLayout.TRAILING, optionPanelLayout.createSequentialGroup().add(audioPlayerTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(chooseAudioPlayerPathButton)).add(optionPanelLayout.createSequentialGroup().add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(synthPathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.LEADING, tempDirTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(chooseTempDirButton).add(chooseSynthPathButton)))))).addContainerGap()));
        optionPanelLayout.setVerticalGroup(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(optionPanelLayout.createSequentialGroup().addContainerGap().add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(chooseSynthPathButton).add(synthPathLabel).add(synthPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(synthOptionsLabel).add(synthOptionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(tempDirTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(tempDirLabel).add(chooseTempDirButton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(optionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(chooseAudioPlayerPathButton).add(audioPlayerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(audioPlayerPathLabel)).add(51, 51, 51).add(setOptionsButton).addContainerGap(339, Short.MAX_VALUE)));
        mainTabbedPane.addTab(resourceMap.getString("optionPanel.TabConstraints.tabTitle"), optionPanel);
        jSplitPane1.setLeftComponent(mainTabbedPane);
        mainTabbedPane.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName"));
        jScrollPane1.setName("jScrollPane1");
        messagesTextPane.setFont(resourceMap.getFont("messagesTextPane.font"));
        messagesTextPane.setName("messagesTextPane");
        jScrollPane1.setViewportView(messagesTextPane);
        jSplitPane1.setRightComponent(jScrollPane1);
        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE));
        mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(mainPanelLayout.createSequentialGroup().add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE)));
        menuBar.setName("menuBar");
        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");
        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText(resourceMap.getString("openMenuItem.text"));
        openMenuItem.setName("openMenuItem");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);
        openMenuItem.getAccessibleContext().setAccessibleName(resourceMap.getString("openMenuItem.AccessibleContext.accessibleName"));
        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText(resourceMap.getString("saveMenuItem.text"));
        saveMenuItem.setEnabled(false);
        saveMenuItem.setName("saveMenuItem");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);
        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setText(resourceMap.getString("saveAsMenuItem.text"));
        saveAsMenuItem.setEnabled(false);
        saveAsMenuItem.setName("saveAsMenuItem");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        reloadTestGraphMenuItem.setText(resourceMap.getString("reloadTestGraphMenuItem.text"));
        reloadTestGraphMenuItem.setEnabled(false);
        reloadTestGraphMenuItem.setName("reloadTestGraphMenuItem");
        reloadTestGraphMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadTestGraphMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(reloadTestGraphMenuItem);
        jSeparator1.setName("jSeparator1");
        fileMenu.add(jSeparator1);
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(artic.gui.ArticEditor.class).getContext().getActionMap(ArticEditorView.class, this);
        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem");
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");
        aboutMenuItem.setAction(actionMap.get("showAboutBox"));
        aboutMenuItem.setName("aboutMenuItem");
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        openFileChooser.setAcceptAllFileFilterUsed(false);
        openFileChooser.setDialogTitle(resourceMap.getString("openFileChooser.dialogTitle"));
        openFileChooser.setFileFilter(xmlFileFilter);
        openFileChooser.setName("openFileChooser");
        saveFileChooser.setAcceptAllFileFilterUsed(false);
        saveFileChooser.setDialogTitle(resourceMap.getString("saveFileChooser.dialogTitle"));
        saveFileChooser.setFileFilter(xmlFileFilter);
        saveFileChooser.setName("saveFileChooser");
        synthPathFileChooser.setDialogTitle(resourceMap.getString("synthPathFileChooser.dialogTitle"));
        synthPathFileChooser.setName("synthPathFileChooser");
        tempDirFileChooser.setDialogTitle(resourceMap.getString("tempDirFileChooser.dialogTitle"));
        tempDirFileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        tempDirFileChooser.setName("tempDirFileChooser");
        audioPlayerPathFileChooser.setDialogTitle(resourceMap.getString("audioPlayerPathFileChooser.dialogTitle"));
        audioPlayerPathFileChooser.setName("audioPlayerPathFileChooser");
        setComponent(mainPanel);
        setMenuBar(menuBar);
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        int retVal = openFileChooser.showOpenDialog(mainPanel);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                ModelFacade.load(openFileChooser.getSelectedFile());
                posturePanel.updateData();
                transitionPanel.updateData(false);
                specialTransitionPanel.updateData(true);
                rulePanel.updateData();
                equationPanel.updateData();
                testGraphPanel.updateData(null);
                Logger.getInstance().info("File " + openFileChooser.getSelectedFile().getName() + " loaded.");
                saveMenuItem.setEnabled(true);
                saveAsMenuItem.setEnabled(true);
                reloadTestGraphMenuItem.setEnabled(true);
                saveFileChooser.setSelectedFile(null);
            } catch (Exception e) {
                Logger.getInstance().error(e);
            }
        }
    }

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (saveFileChooser.getSelectedFile() == null) {
            saveFileChooser.setSelectedFile(openFileChooser.getSelectedFile());
        }
        int retVal = saveFileChooser.showSaveDialog(mainPanel);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                ModelFacade.save(saveFileChooser.getSelectedFile());
                Logger.getInstance().info("File " + saveFileChooser.getSelectedFile().getName() + " saved.");
            } catch (Exception e) {
                Logger.getInstance().error(e);
            }
        }
    }

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {
        test();
    }

    private void chooseSynthPathButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int retVal = synthPathFileChooser.showOpenDialog(mainPanel);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                synthPathTextField.setText(synthPathFileChooser.getSelectedFile().getCanonicalPath());
            } catch (Exception e) {
                Logger.getInstance().error(e);
            }
        }
    }

    private void chooseTempDirButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int retVal = tempDirFileChooser.showOpenDialog(mainPanel);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                tempDirTextField.setText(tempDirFileChooser.getSelectedFile().getCanonicalPath());
            } catch (Exception e) {
                Logger.getInstance().error(e);
            }
        }
    }

    private void chooseAudioPlayerPathButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int retVal = audioPlayerPathFileChooser.showOpenDialog(mainPanel);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                audioPlayerTextField.setText(audioPlayerPathFileChooser.getSelectedFile().getCanonicalPath());
            } catch (Exception e) {
                Logger.getInstance().error(e);
            }
        }
    }

    private void setOptionsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Preferences pref = Preferences.userNodeForPackage(ArticEditor.class);
        pref.put(ArticEditor.PREF_KEY_SYNTH_PATH, synthPathTextField.getText());
        pref.put(ArticEditor.PREF_KEY_SYNTH_OPTIONS, synthOptionTextField.getText());
        pref.put(ArticEditor.PREF_KEY_TEMP_DIR, tempDirTextField.getText());
        pref.put(ArticEditor.PREF_KEY_AUDIO_PLAYER_PATH, audioPlayerTextField.getText());
        try {
            pref.flush();
            Logger.getInstance().info("Options saved.");
        } catch (Exception e) {
            Logger.getInstance().error(e);
        }
    }

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (saveFileChooser.getSelectedFile() == null) {
            saveFileChooser.setSelectedFile(openFileChooser.getSelectedFile());
            int retVal = saveFileChooser.showSaveDialog(mainPanel);
            if (retVal != JFileChooser.APPROVE_OPTION) return;
        }
        try {
            ModelFacade.save(saveFileChooser.getSelectedFile());
            Logger.getInstance().info("File " + saveFileChooser.getSelectedFile().getName() + " saved.");
        } catch (Exception e) {
            Logger.getInstance().error(e);
        }
    }

    private void postureListTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
        test();
    }

    private void reloadTestGraphMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        String tempDir = tempDirTextField.getText().trim();
        if (tempDir.length() == 0) {
            Logger.getInstance().warn("Empty temporary directory option.");
            return;
        }
        try {
            String path = tempDir + '/' + ArticEditor.SYNTH_PARAM_OUTPUT_FILE;
            testGraphPanel.updateData(path);
            Logger.getInstance().info("Test graph loaded from: " + path);
        } catch (Exception e) {
            Logger.getInstance().error(e);
        }
    }

    private javax.swing.JFileChooser audioPlayerPathFileChooser;

    private javax.swing.JLabel audioPlayerPathLabel;

    private javax.swing.JTextField audioPlayerTextField;

    private javax.swing.JButton chooseAudioPlayerPathButton;

    private javax.swing.JButton chooseSynthPathButton;

    private javax.swing.JButton chooseTempDirButton;

    private artic.gui.EquationPanel equationPanel;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JPanel mainPanel;

    private javax.swing.JTabbedPane mainTabbedPane;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JPanel messagesPanel;

    private javax.swing.JTextPane messagesTextPane;

    private javax.swing.JTextPane messagesTextPane2;

    private javax.swing.JFileChooser openFileChooser;

    private javax.swing.JMenuItem openMenuItem;

    private javax.swing.JPanel optionPanel;

    private javax.swing.JLabel postureListLabel;

    private javax.swing.JTextField postureListTextField;

    private artic.gui.PosturePanel posturePanel;

    private javax.swing.JMenuItem reloadTestGraphMenuItem;

    private artic.gui.RulePanel rulePanel;

    private javax.swing.JMenuItem saveAsMenuItem;

    private javax.swing.JFileChooser saveFileChooser;

    private javax.swing.JMenuItem saveMenuItem;

    private javax.swing.JButton setOptionsButton;

    private artic.gui.TransitionPanel specialTransitionPanel;

    private javax.swing.JTextField synthOptionTextField;

    private javax.swing.JLabel synthOptionsLabel;

    private javax.swing.JFileChooser synthPathFileChooser;

    private javax.swing.JLabel synthPathLabel;

    private javax.swing.JTextField synthPathTextField;

    private javax.swing.JFileChooser tempDirFileChooser;

    private javax.swing.JLabel tempDirLabel;

    private javax.swing.JTextField tempDirTextField;

    private javax.swing.JButton testButton;

    private artic.gui.TestGraphPanel testGraphPanel;

    private artic.gui.TransitionPanel transitionPanel;

    private JDialog aboutBox;

    public void loadOptions() {
        Preferences pref = Preferences.userNodeForPackage(ArticEditor.class);
        synthPathTextField.setText(pref.get(ArticEditor.PREF_KEY_SYNTH_PATH, null));
        synthOptionTextField.setText(pref.get(ArticEditor.PREF_KEY_SYNTH_OPTIONS, null));
        tempDirTextField.setText(pref.get(ArticEditor.PREF_KEY_TEMP_DIR, null));
        audioPlayerTextField.setText(pref.get(ArticEditor.PREF_KEY_AUDIO_PLAYER_PATH, null));
    }

    private void test() {
        String postureString = postureListTextField.getText().trim();
        if (postureString.length() == 0) {
            return;
        }
        String synthPath = synthPathTextField.getText().trim();
        if (synthPath.length() == 0) {
            Logger.getInstance().warn("Empty synth. path option.");
            return;
        }
        String synthOption = synthOptionTextField.getText().trim();
        String tempDir = tempDirTextField.getText().trim();
        if (tempDir.length() == 0) {
            Logger.getInstance().warn("Empty temporary directory option.");
            return;
        }
        String audioPlayerPath = audioPlayerTextField.getText().trim();
        if (audioPlayerPath.length() == 0) {
            Logger.getInstance().warn("Empty audio player path option.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        try {
            final String prefix = tempDir + '/';
            PrintWriter out = new PrintWriter(prefix + ArticEditor.SYNTH_INPUT_FILE, "US-ASCII");
            String[] sa = postureString.split("\\s+");
            out.println("# 0");
            for (String item : sa) {
                Matcher m = testPattern.matcher(item);
                if (m.matches()) {
                    out.print(m.group(1));
                    out.print(' ');
                    out.println(m.group(2));
                } else {
                    out.print(item);
                    out.println(" 0");
                }
            }
            out.println("# 0");
            if (out.checkError()) {
                throw new Exception("Error while writing the synth input file.");
            }
            out.close();
            ModelFacade.save(new File(prefix + ArticEditor.SYNTH_CONFIG_DIR + '/' + ArticEditor.SYNTH_XML_FILE));
            List<String> argList = new ArrayList<String>();
            argList.add(synthPath);
            if (synthOption.length() > 0) {
                argList.add(synthOption);
            }
            argList.add(prefix + ArticEditor.SYNTH_CONFIG_DIR);
            argList.add(prefix + ArticEditor.SYNTH_INPUT_FILE);
            argList.add(prefix + ArticEditor.SYNTH_OUTPUT_FILE);
            ProcessBuilder pb = new ProcessBuilder(argList);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append('\n');
            }
            int exitValue = p.waitFor();
            sb.append("Synth exit value: " + exitValue);
            sb.append('\n');
            if (exitValue != 0) {
                Logger.getInstance().error(sb.toString());
                return;
            }
            pb = new ProcessBuilder(audioPlayerPath, prefix + ArticEditor.SYNTH_OUTPUT_FILE);
            pb.redirectErrorStream(true);
            p = pb.start();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                sb.append(line).append('\n');
            }
            exitValue = p.waitFor();
            sb.append("Audio player exit value: " + exitValue);
            Logger.getInstance().info(sb.toString());
            testGraphPanel.updateData(prefix + ArticEditor.SYNTH_PARAM_OUTPUT_FILE);
        } catch (Exception e) {
            if (sb.length() > 0) {
                Logger.getInstance().error(sb.toString());
            }
            Logger.getInstance().error(e);
        }
    }
}
