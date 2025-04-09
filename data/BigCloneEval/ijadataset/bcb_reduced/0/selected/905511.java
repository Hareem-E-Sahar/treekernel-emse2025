package jstella.desktop;

import java.awt.Color;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import jstella.runner.*;
import jstella.util.*;
import java.util.zip.*;
import static jstella.runner.JStellaRunnerUtil.*;
import static jstella.desktop.JStellaDesktop.*;
import static jstella.desktop.JStellaImporter.*;

/**
 *
 * @author  J.L. Allen
 */
public class JStellaRepository extends javax.swing.JFrame implements JStellaGamePanel.IfcGamePanelClient {

    private static final long serialVersionUID = 2271542183636011052L;

    public static final Color COLOR_REGULAR = javax.swing.UIManager.getDefaults().getColor("Panel.background");

    public static final Color COLOR_SELECTED = Color.GREEN;

    public static final Color COLOR_ROMLESS = Color.RED;

    public static final int COLUMN_COUNT = 6;

    public static final int ROW_COUNT = 0;

    public static final double ICON_SCALE_LARGE = 1.0;

    public static final double ICON_SCALE_MEDIUM = 0.66;

    public static final double ICON_SCALE_SMALL = 0.33;

    public static final int ICON_MAX_WIDTH = 250;

    public static final int ICON_MAX_HEIGHT = 250;

    public static final int GAME_TITLE_MAX_LENGTH = 20;

    public static final String NEGATIVE_SORT_STRING = "~~~~~~";

    public static final long ROM_LENGTH_MAX = 40000L;

    public static final int SCROLL_BAR_UNIT_INCREMENT = 20;

    private java.util.List<JStellaGamePanel> myGamePanelList = new java.util.ArrayList<JStellaGamePanel>();

    private JStellaGamePanel mySelectedGamePanel = null;

    private java.util.Comparator myCurrentComparator = new IconComparator();

    private boolean myIconlessPanelsVisible = true;

    /** Creates new form JStellaRepository 
     * 
     */
    public JStellaRepository() {
        initComponents();
        SPCenter.getVerticalScrollBar().setUnitIncrement(SCROLL_BAR_UNIT_INCREMENT);
        ((java.awt.GridLayout) PanelGames.getLayout()).setColumns(COLUMN_COUNT);
        ((java.awt.GridLayout) PanelGames.getLayout()).setRows(ROW_COUNT);
        JStellaGamePanel.setIconScaleFactor(ICON_SCALE_MEDIUM);
        loadGames();
        updateLayout(true);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        PopupMain = new javax.swing.JPopupMenu();
        PMIRunSelected = new javax.swing.JMenuItem();
        PMIEditSelected = new javax.swing.JMenuItem();
        PMIDeleteSelected = new javax.swing.JMenuItem();
        ButtonGroupIconSize = new javax.swing.ButtonGroup();
        ButtonGroupSort = new javax.swing.ButtonGroup();
        myProgressDialog = new javax.swing.JDialog();
        PanelCenter = new javax.swing.JPanel();
        LabelProcess = new javax.swing.JLabel();
        LabelProgress = new javax.swing.JLabel();
        SPCenter = new javax.swing.JScrollPane();
        PanelGames = new javax.swing.JPanel();
        MBMain = new javax.swing.JMenuBar();
        MenuFile = new javax.swing.JMenu();
        MIRunSetupWizard = new javax.swing.JMenuItem();
        MIAddGames = new javax.swing.JMenuItem();
        MIConfig = new javax.swing.JMenuItem();
        MIExitJStella = new javax.swing.JMenuItem();
        MenuView = new javax.swing.JMenu();
        MenuSort = new javax.swing.JMenu();
        RBSortTitle = new javax.swing.JRadioButtonMenuItem();
        RBSortMaker = new javax.swing.JRadioButtonMenuItem();
        RBSortYear = new javax.swing.JRadioButtonMenuItem();
        RBSortIcon = new javax.swing.JRadioButtonMenuItem();
        RBSortController = new javax.swing.JRadioButtonMenuItem();
        SepViewA = new javax.swing.JSeparator();
        MenuViewSize = new javax.swing.JMenu();
        RBIconsSmall = new javax.swing.JRadioButtonMenuItem();
        RBIconsMed = new javax.swing.JRadioButtonMenuItem();
        RBIconsLarge = new javax.swing.JRadioButtonMenuItem();
        CBMIShowTitles = new javax.swing.JCheckBoxMenuItem();
        CBMIShowIconlessPanels = new javax.swing.JCheckBoxMenuItem();
        MenuAdvanced = new javax.swing.JMenu();
        MIImportStandardMetadata = new javax.swing.JMenuItem();
        MIExportMetadata = new javax.swing.JMenuItem();
        MenuHelp = new javax.swing.JMenu();
        MIHelpContents = new javax.swing.JMenuItem();
        PopupMain.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }

            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }

            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                PopupMainPopupMenuWillBecomeVisible(evt);
            }
        });
        PMIRunSelected.setText("Run");
        PMIRunSelected.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PMIRunSelectedActionPerformed(evt);
            }
        });
        PopupMain.add(PMIRunSelected);
        PMIEditSelected.setText("Edit");
        PMIEditSelected.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PMIEditSelectedActionPerformed(evt);
            }
        });
        PopupMain.add(PMIEditSelected);
        PMIDeleteSelected.setText("Delete");
        PMIDeleteSelected.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PMIDeleteSelectedActionPerformed(evt);
            }
        });
        PopupMain.add(PMIDeleteSelected);
        myProgressDialog.setTitle("Importing");
        myProgressDialog.setMinimumSize(new java.awt.Dimension(300, 150));
        PanelCenter.setDoubleBuffered(false);
        PanelCenter.setLayout(new java.awt.GridBagLayout());
        LabelProcess.setText("Importing metadata...");
        PanelCenter.add(LabelProcess, new java.awt.GridBagConstraints());
        LabelProgress.setText("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        PanelCenter.add(LabelProgress, gridBagConstraints);
        myProgressDialog.getContentPane().add(PanelCenter, java.awt.BorderLayout.CENTER);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("JStella Game Repository");
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        PanelGames.setComponentPopupMenu(PopupMain);
        PanelGames.setLayout(new java.awt.GridLayout(1, 0));
        SPCenter.setViewportView(PanelGames);
        getContentPane().add(SPCenter, java.awt.BorderLayout.CENTER);
        MenuFile.setText("File");
        MIRunSetupWizard.setText("Run setup wizard");
        MIRunSetupWizard.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIRunSetupWizardActionPerformed(evt);
            }
        });
        MenuFile.add(MIRunSetupWizard);
        MIAddGames.setText("Add ROMs to repository");
        MIAddGames.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIAddGamesActionPerformed(evt);
            }
        });
        MenuFile.add(MIAddGames);
        MIConfig.setText("Configure");
        MIConfig.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIConfigActionPerformed(evt);
            }
        });
        MenuFile.add(MIConfig);
        MIExitJStella.setText("Exit JStella");
        MIExitJStella.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIExitJStellaActionPerformed(evt);
            }
        });
        MenuFile.add(MIExitJStella);
        MBMain.add(MenuFile);
        MenuView.setText("View");
        MenuSort.setText("Arrange by...");
        ButtonGroupSort.add(RBSortTitle);
        RBSortTitle.setSelected(true);
        RBSortTitle.setText("Title");
        RBSortTitle.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBSortTitleActionPerformed(evt);
            }
        });
        MenuSort.add(RBSortTitle);
        ButtonGroupSort.add(RBSortMaker);
        RBSortMaker.setText("Maker");
        RBSortMaker.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBSortMakerActionPerformed(evt);
            }
        });
        MenuSort.add(RBSortMaker);
        ButtonGroupSort.add(RBSortYear);
        RBSortYear.setText("Year");
        RBSortYear.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBSortYearActionPerformed(evt);
            }
        });
        MenuSort.add(RBSortYear);
        ButtonGroupSort.add(RBSortIcon);
        RBSortIcon.setText("Icon");
        RBSortIcon.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBSortIconActionPerformed(evt);
            }
        });
        MenuSort.add(RBSortIcon);
        ButtonGroupSort.add(RBSortController);
        RBSortController.setText("Controller type");
        RBSortController.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBSortControllerActionPerformed(evt);
            }
        });
        MenuSort.add(RBSortController);
        MenuView.add(MenuSort);
        MenuView.add(SepViewA);
        MenuViewSize.setText("Icon size");
        ButtonGroupIconSize.add(RBIconsSmall);
        RBIconsSmall.setText("Small");
        RBIconsSmall.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBIconsSmallActionPerformed(evt);
            }
        });
        MenuViewSize.add(RBIconsSmall);
        ButtonGroupIconSize.add(RBIconsMed);
        RBIconsMed.setSelected(true);
        RBIconsMed.setText("Medium");
        RBIconsMed.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBIconsMedActionPerformed(evt);
            }
        });
        MenuViewSize.add(RBIconsMed);
        ButtonGroupIconSize.add(RBIconsLarge);
        RBIconsLarge.setText("Large");
        RBIconsLarge.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RBIconsLargeActionPerformed(evt);
            }
        });
        MenuViewSize.add(RBIconsLarge);
        MenuView.add(MenuViewSize);
        CBMIShowTitles.setSelected(true);
        CBMIShowTitles.setText("Show game titles");
        CBMIShowTitles.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CBMIShowTitlesActionPerformed(evt);
            }
        });
        MenuView.add(CBMIShowTitles);
        CBMIShowIconlessPanels.setSelected(true);
        CBMIShowIconlessPanels.setText("Include iconless games");
        CBMIShowIconlessPanels.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CBMIShowIconlessPanelsActionPerformed(evt);
            }
        });
        MenuView.add(CBMIShowIconlessPanels);
        MBMain.add(MenuView);
        MenuAdvanced.setText("Advanced");
        MIImportStandardMetadata.setText("Import standard metadata");
        MIImportStandardMetadata.setEnabled(false);
        MIImportStandardMetadata.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIImportStandardMetadataActionPerformed(evt);
            }
        });
        MenuAdvanced.add(MIImportStandardMetadata);
        MIExportMetadata.setText("Export metadata");
        MIExportMetadata.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIExportMetadataActionPerformed(evt);
            }
        });
        MenuAdvanced.add(MIExportMetadata);
        MBMain.add(MenuAdvanced);
        MenuHelp.setText("Help");
        MIHelpContents.setText("Help contents");
        MIHelpContents.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MIHelpContentsActionPerformed(evt);
            }
        });
        MenuHelp.add(MIHelpContents);
        MBMain.add(MenuHelp);
        setJMenuBar(MBMain);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 505) / 2, (screenSize.height - 478) / 2, 505, 478);
    }

    private void PMIEditSelectedActionPerformed(java.awt.event.ActionEvent evt) {
        editSelected();
    }

    private void PopupMainPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
        PMIEditSelected.setEnabled(getSelectedGamePanel() != null);
        PMIRunSelected.setEnabled(getSelectedGamePanel() != null);
        PMIDeleteSelected.setEnabled(getSelectedGamePanel() != null);
    }

    private void PMIRunSelectedActionPerformed(java.awt.event.ActionEvent evt) {
        runSelected();
    }

    private void RBIconsSmallActionPerformed(java.awt.event.ActionEvent evt) {
        setIconScaleFactor(ICON_SCALE_SMALL);
    }

    private void RBIconsMedActionPerformed(java.awt.event.ActionEvent evt) {
        setIconScaleFactor(ICON_SCALE_MEDIUM);
    }

    private void RBIconsLargeActionPerformed(java.awt.event.ActionEvent evt) {
        setIconScaleFactor(ICON_SCALE_LARGE);
    }

    private void CBMIShowTitlesActionPerformed(java.awt.event.ActionEvent evt) {
        setShowTitle(CBMIShowTitles.isSelected());
    }

    private void PMIDeleteSelectedActionPerformed(java.awt.event.ActionEvent evt) {
        deleteSelected();
    }

    private void formComponentResized(java.awt.event.ComponentEvent evt) {
        readjustGrid();
    }

    private void MIConfigActionPerformed(java.awt.event.ActionEvent evt) {
        File zOldReposDir = getRepositoryDirectory();
        JStellaDesktop.showConfigurationDialog(this);
        File zNewReposDir = getRepositoryDirectory();
        if (!zNewReposDir.equals(zOldReposDir)) {
            loadGames();
            updateLayout(true);
        }
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        JStellaDesktop.closeFrame(this);
    }

    private void MIExitJStellaActionPerformed(java.awt.event.ActionEvent evt) {
        JStellaDesktop.closeFrame(this);
    }

    private void RBSortTitleActionPerformed(java.awt.event.ActionEvent evt) {
        sortGamePanels(new TitleComparator());
    }

    private void RBSortMakerActionPerformed(java.awt.event.ActionEvent evt) {
        sortGamePanels(new ConfigKeyComparator(CONFIG_KEY_GAME_MAKER));
    }

    private void RBSortYearActionPerformed(java.awt.event.ActionEvent evt) {
        sortGamePanels(new ConfigKeyComparator(CONFIG_KEY_GAME_YEAR));
    }

    private void RBSortIconActionPerformed(java.awt.event.ActionEvent evt) {
        sortGamePanels(new IconComparator());
    }

    private void CBMIShowIconlessPanelsActionPerformed(java.awt.event.ActionEvent evt) {
        setIconlessPanelsVisible(CBMIShowIconlessPanels.isSelected());
    }

    private void MIExportMetadataActionPerformed(java.awt.event.ActionEvent evt) {
        exportMetadata();
    }

    private void MIImportStandardMetadataActionPerformed(java.awt.event.ActionEvent evt) {
        importStandardMetadata();
    }

    private void MIHelpContentsActionPerformed(java.awt.event.ActionEvent evt) {
        JStellaHelp.runJStellaHelp(this);
    }

    private void RBSortControllerActionPerformed(java.awt.event.ActionEvent evt) {
        sortGamePanels(new ConfigKeyComparator(CONFIG_KEY_CONTROLLER_LEFT));
    }

    private void MIAddGamesActionPerformed(java.awt.event.ActionEvent evt) {
        addROMsToRepository();
    }

    private void MIRunSetupWizardActionPerformed(java.awt.event.ActionEvent evt) {
        JStellaDesktop.doWizard();
        this.setVisible(false);
    }

    private void setIconScaleFactor(double aFactor) {
        JStellaGamePanel.setIconScaleFactor(aFactor);
        updateLayout(false);
    }

    private void setShowTitle(boolean aShowTitle) {
        JStellaGamePanel.setShowTitle(aShowTitle);
        updateLayout(true);
    }

    private void updateLayout(boolean aUpdatePanels) {
        for (JStellaGamePanel zPanel : myGamePanelList) {
            if (aUpdatePanels == true) zPanel.updateGamePanel();
            zPanel.setPreferredSize(null);
            zPanel.revalidate();
        }
        PanelGames.validate();
        PanelGames.repaint();
    }

    /**
     * This method resizes the grid of cartridges showing to better match the dimensions
     * of the window.
     * 
     */
    private void readjustGrid() {
        int zCol = COLUMN_COUNT;
        int zRow = ROW_COUNT;
        GridLayout zGL = (GridLayout) PanelGames.getLayout();
        if (PanelGames.isVisible()) {
            Dimension zPrefDim = zGL.preferredLayoutSize(PanelGames);
            int zItemWidth = zPrefDim.width / zGL.getColumns();
            int zViewWidth = SPCenter.getViewport().getSize().width;
            int zPotentialWidth = (int) Math.round((double) zViewWidth / zItemWidth);
            zCol = Math.max(zPotentialWidth, 2);
            zRow = 0;
        } else {
            zCol = COLUMN_COUNT;
            zRow = ROW_COUNT;
        }
        zGL.setColumns(zCol);
        zGL.setRows(zRow);
        zGL.layoutContainer(PanelGames);
        updateLayout(false);
    }

    private void sortGamePanels(java.util.Comparator aComparator, boolean aShowIconlessPanels) {
        myCurrentComparator = aComparator;
        java.util.Collections.sort(myGamePanelList, aComparator);
        PanelGames.removeAll();
        for (JStellaGamePanel zGP : myGamePanelList) {
            if ((aShowIconlessPanels) || (zGP.hasIcon() == true)) PanelGames.add(zGP);
        }
        updateLayout(true);
    }

    private void sortGamePanels(java.util.Comparator aComparator) {
        sortGamePanels(aComparator, myIconlessPanelsVisible);
    }

    private void sortGamePanels() {
        sortGamePanels(myCurrentComparator, myIconlessPanelsVisible);
    }

    private void setIconlessPanelsVisible(boolean aVisible) {
        myIconlessPanelsVisible = aVisible;
        sortGamePanels();
    }

    private java.util.Map<String, JStellaGamePanel> getROMlessPanelMap() {
        java.util.Map<String, JStellaGamePanel> zReturn = new java.util.HashMap<String, JStellaGamePanel>();
        for (JStellaGamePanel zPanel : myGamePanelList) {
            JStellaGame zGame = zPanel.getGame();
            if ((zGame.getROMData() == null) && (zGame.getMD5() != null) && (!zGame.getMD5().equals(""))) {
                zReturn.put(zGame.getMD5(), zPanel);
            }
        }
        return zReturn;
    }

    private java.util.Map<String, String> getConfiguration() {
        return JStellaDesktop.getConfiguration();
    }

    /**
    * Returns the game panel that is currently selected.
    * @return the selected panel
    */
    public JStellaGamePanel getSelectedGamePanel() {
        return mySelectedGamePanel;
    }

    /**
    * Sets the currently selected panel.
    * @param aGamePanel the game panel that is to become selected
    */
    public void setSelectedGamePanel(JStellaGamePanel aGamePanel) {
        if (aGamePanel != mySelectedGamePanel) {
            JStellaGamePanel zPrevSelection = mySelectedGamePanel;
            mySelectedGamePanel = aGamePanel;
            Rectangle zDebugRect = new Rectangle(0, 0, 10, 10);
            if (zPrevSelection != null) zPrevSelection.repaint();
            if (aGamePanel != null) aGamePanel.repaint();
        }
    }

    public Color getRegularColor() {
        return COLOR_REGULAR;
    }

    public Color getSelectedColor() {
        return COLOR_SELECTED;
    }

    public Color getROMlessColor() {
        return COLOR_ROMLESS;
    }

    public boolean isSelected(JStellaGamePanel aGamePanel) {
        return (aGamePanel == mySelectedGamePanel);
    }

    private void addROMsToRepository() {
        File zRepositoryDir = getRepositoryDirectory();
        if (zRepositoryDir != null) {
            File zROMDir = getValidDirectory(CONFIG_KEY_DEFAULT_ROM_DIR, getConfiguration());
            JStellaDesktop.configureFileBrowser(true, false, true, zROMDir, null, JSFileNameExtensionFilter.FILTER_ROMS);
            int zResult = getFileBrowser().showOpenDialog(this);
            if (zResult == JFileChooser.APPROVE_OPTION) {
                setWaitingMode(true);
                importROMsAndStandardMetadata(getFileBrowser().getSelectedFiles());
                System.out.println("Loading games");
            }
        } else {
            JOptionPane.showMessageDialog(this, "You must first select a valid repository directory");
        }
    }

    /**
     * This loads the games from a previously specified repository directory.
     */
    public void loadGames() {
        try {
            PanelGames.removeAll();
            myGamePanelList.clear();
            ((java.awt.GridLayout) PanelGames.getLayout()).setColumns(COLUMN_COUNT);
            File zDir = getRepositoryDirectory();
            if (zDir != null) {
                addGameFiles(createGameFileList(zDir));
                java.util.Collections.sort(myGamePanelList, new TitleComparator());
                sortGamePanels();
            } else System.out.println("Error: invalid repository directory");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addGameFile(File aGameFile, boolean aSort) throws IOException, ClassNotFoundException {
        JStellaGamePanel zGPanel = new JStellaGamePanel(this, aGameFile);
        myGamePanelList.add(zGPanel);
        if (aSort == false) PanelGames.add(zGPanel); else sortGamePanels();
    }

    private void addGameFiles(java.util.List<File> aGameFileList) throws IOException, ClassNotFoundException {
        for (File zFile : aGameFileList) {
            JStellaGamePanel zGPanel = new JStellaGamePanel(this, zFile);
            myGamePanelList.add(zGPanel);
        }
    }

    private java.util.List<File> createGameFileList(File aRepositoryDir) {
        java.util.List<File> zGameFileList = new java.util.ArrayList<File>();
        File[] zFileArray = createGameFileArray(aRepositoryDir);
        zGameFileList.addAll(java.util.Arrays.asList(zFileArray));
        return zGameFileList;
    }

    private File[] createGameFileArray(File aRepositoryDir) {
        return aRepositoryDir.listFiles(JSFileNameExtensionFilter.FILTER_JSTELLAGAME);
    }

    private void saveGame(JStellaGame aGame) {
        try {
            File zGameFile = new File(getRepositoryDirectory(), aGame.getGameFilename());
            ObjectOutputStream zOOS = new ObjectOutputStream(new FileOutputStream(zGameFile));
            zOOS.writeObject(aGame);
            zOOS.close();
            System.out.println("Game saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editSelected() {
        if (mySelectedGamePanel != null) {
            boolean zKeepChanges = JStellaGamePanelEditor.runGamePanelEditor(this, mySelectedGamePanel.getGame());
            if (zKeepChanges == true) {
                saveGame(mySelectedGamePanel.getGame());
                mySelectedGamePanel.updateGamePanel();
                sortGamePanels();
            } else {
                try {
                    mySelectedGamePanel.reloadGame(getRepositoryDirectory());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            this.repaint();
        }
    }

    private void deleteSelected() {
        if (mySelectedGamePanel != null) {
            int zResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete: " + mySelectedGamePanel.getGame().getGameTitle() + "?", "Delete game?", JOptionPane.YES_NO_OPTION);
            if (zResult == JOptionPane.YES_OPTION) {
                JStellaGamePanel zDelPanel = mySelectedGamePanel;
                mySelectedGamePanel = null;
                PanelGames.remove(zDelPanel);
                PanelGames.validate();
                File zGameFile = new File(getRepositoryDirectory(), zDelPanel.getGame().getGameFilename());
                if (zGameFile.getName().toLowerCase().endsWith("j26")) {
                    System.out.println("Deleting file: " + zGameFile.getName());
                    if (zGameFile.exists() == true) zGameFile.delete();
                }
            }
        }
    }

    private void updateGamePanels() {
        for (JStellaGamePanel zPanel : myGamePanelList) {
            zPanel.updateGamePanel();
        }
    }

    private void runSelected() {
        if (mySelectedGamePanel != null) {
            runGame(mySelectedGamePanel);
        }
    }

    public void runGame(JStellaGamePanel aGamePanel) {
        if (aGamePanel != null) {
            if (aGamePanel.getGame().getROMData() != null) JStellaDesktop.playJStellaGame(aGamePanel.getGame());
        }
    }

    public JPopupMenu getPopupMenu() {
        return PopupMain;
    }

    private void postProgressUpdate(String aMessage) {
        LabelProgress.setText(aMessage);
    }

    public void setWaitingMode(boolean aWait) {
        setCursor(aWait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : null);
        MBMain.setEnabled(!aWait);
        getGlassPane().setVisible(aWait);
    }

    private JStellaGame[] createMetadata() {
        java.util.List<JStellaGame> zMetadataList = new java.util.ArrayList<JStellaGame>();
        Component[] zComponents = PanelGames.getComponents();
        for (int i = 0; i < zComponents.length; i++) {
            if (zComponents[i] instanceof JStellaGamePanel) {
                JStellaGamePanel zGamePanel = (JStellaGamePanel) zComponents[i];
                zMetadataList.add(zGamePanel.getGame().createMetadataObject());
            }
        }
        JStellaGame[] zReturn = new JStellaGame[zMetadataList.size()];
        zReturn = zMetadataList.toArray(zReturn);
        return zReturn;
    }

    private void exportMetadata() {
        configureFileBrowser(false, false, true, null, null, JSFileNameExtensionFilter.FILTER_METADATA_COLLECTION);
        int zResult = getFileBrowser().showSaveDialog(this);
        if (zResult == JFileChooser.APPROVE_OPTION) {
            File zSelected = getFileBrowser().getSelectedFile();
            String zMainExtension = "." + JSFileNameExtensionFilter.FILTER_METADATA_COLLECTION.getMainExtension();
            if (!zSelected.getPath().endsWith(zMainExtension)) zSelected = new File(zSelected.getPath() + zMainExtension);
            JStellaGame[] zMetadata = createMetadata();
            addMetadataToCollection(zMetadata, zSelected);
        }
    }

    public void importStandardMetadata() {
        JOptionPane.showMessageDialog(this, "Preparing to import metadata.  This may take a few minutes.");
        myProgressDialog.setVisible(true);
        File[] zFileArray = getRepositoryDirectory().listFiles(JSFileNameExtensionFilter.FILTER_JSTELLAGAME);
        importStandardMetadata(zFileArray);
    }

    /**
      * This method imports the "standard" metadata that is quasi-bundled with JStella.
      * 
      */
    public void importStandardMetadata(File[] aGameFiles) {
        try {
            InputStream zStream = JStellaImporter.class.getResourceAsStream("/jstella/resources/metadata/metadata.j26mc");
            java.util.Map<String, File> zMD5Map = createMD5FileMap(aGameFiles);
            launchMetadataImporter(this, zMD5Map, zStream, new ResumeRepositoryOperations());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importROMsAndStandardMetadata(File[] aROMFiles) {
        try {
            InputStream zStream = JStellaImporter.class.getResourceAsStream("/jstella/resources/metadata/metadata.j26mc");
            launchROMAndMetadataImporter(this, aROMFiles, zStream, new ResumeRepositoryOperations());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exportMetadataItem(ZipOutputStream aZOS, JStellaGame aMetadata) throws IOException {
        ZipEntry zEntry = new ZipEntry(aMetadata.getGameFilename());
        aZOS.putNextEntry(zEntry);
        byte[] zData = toByteArray(aMetadata);
        aZOS.write(zData);
        aZOS.closeEntry();
    }

    protected static void addMetadataToCollection(JStellaGame[] aMetadata, File aMetadataCollection) {
        try {
            ZipOutputStream zZOS = new ZipOutputStream(new FileOutputStream(aMetadataCollection));
            zZOS.setMethod(ZipOutputStream.DEFLATED);
            for (int i = 0; i < aMetadata.length; i++) {
                exportMetadataItem(zZOS, aMetadata[i]);
            }
            zZOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private javax.swing.ButtonGroup ButtonGroupIconSize;

    private javax.swing.ButtonGroup ButtonGroupSort;

    private javax.swing.JCheckBoxMenuItem CBMIShowIconlessPanels;

    private javax.swing.JCheckBoxMenuItem CBMIShowTitles;

    private javax.swing.JLabel LabelProcess;

    private javax.swing.JLabel LabelProgress;

    private javax.swing.JMenuBar MBMain;

    private javax.swing.JMenuItem MIAddGames;

    private javax.swing.JMenuItem MIConfig;

    private javax.swing.JMenuItem MIExitJStella;

    private javax.swing.JMenuItem MIExportMetadata;

    private javax.swing.JMenuItem MIHelpContents;

    private javax.swing.JMenuItem MIImportStandardMetadata;

    private javax.swing.JMenuItem MIRunSetupWizard;

    private javax.swing.JMenu MenuAdvanced;

    private javax.swing.JMenu MenuFile;

    private javax.swing.JMenu MenuHelp;

    private javax.swing.JMenu MenuSort;

    private javax.swing.JMenu MenuView;

    private javax.swing.JMenu MenuViewSize;

    private javax.swing.JMenuItem PMIDeleteSelected;

    private javax.swing.JMenuItem PMIEditSelected;

    private javax.swing.JMenuItem PMIRunSelected;

    private javax.swing.JPanel PanelCenter;

    private javax.swing.JPanel PanelGames;

    private javax.swing.JPopupMenu PopupMain;

    private javax.swing.JRadioButtonMenuItem RBIconsLarge;

    private javax.swing.JRadioButtonMenuItem RBIconsMed;

    private javax.swing.JRadioButtonMenuItem RBIconsSmall;

    private javax.swing.JRadioButtonMenuItem RBSortController;

    private javax.swing.JRadioButtonMenuItem RBSortIcon;

    private javax.swing.JRadioButtonMenuItem RBSortMaker;

    private javax.swing.JRadioButtonMenuItem RBSortTitle;

    private javax.swing.JRadioButtonMenuItem RBSortYear;

    private javax.swing.JScrollPane SPCenter;

    private javax.swing.JSeparator SepViewA;

    private javax.swing.JDialog myProgressDialog;

    private class TitleComparator implements java.util.Comparator<JStellaGamePanel> {

        public int compare(JStellaGamePanel o1, JStellaGamePanel o2) {
            return o1.getGame().getGameTitle().compareTo(o2.getGame().getGameTitle());
        }
    }

    private class ConfigKeyComparator implements java.util.Comparator<JStellaGamePanel> {

        private String myConfigKey = "";

        public ConfigKeyComparator(String aConfigKey) {
            myConfigKey = aConfigKey;
        }

        private String getValue(JStellaGamePanel zGP) {
            String zValue = zGP.getGame().getGameConfig().get(myConfigKey);
            if ((zValue == null) || (zValue.equals(""))) zValue = NEGATIVE_SORT_STRING;
            return zValue;
        }

        public int compare(JStellaGamePanel o1, JStellaGamePanel o2) {
            return getValue(o1).compareTo(getValue(o2));
        }
    }

    private class IconComparator implements java.util.Comparator<JStellaGamePanel> {

        private int getIconValue(JStellaGamePanel aGP) {
            int zValue = (aGP.getGame().getGraphicData() == null ? 1 : -1);
            return zValue;
        }

        public int compare(JStellaGamePanel o1, JStellaGamePanel o2) {
            return getIconValue(o1) - getIconValue(o2);
        }
    }

    public class ResumeRepositoryOperations implements Runnable {

        public void run() {
            loadGames();
            updateLayout(true);
            setWaitingMode(false);
        }
    }
}
