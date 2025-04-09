package araword.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import say.swing.JFontChooser;
import araword.G;
import araword.classes.AWElement;
import araword.configuration.TLanguage;
import araword.configuration.TSetup;
import araword.db.DBManagement;
import araword.utils.RelativePath;
import araword.utils.TextUtils;

public class GUI extends FrameView {

    public GUI(SingleFrameApplication app) {
        super(app);
        initComponents();
        setMenuBar(menuBar);
        textZone.setBackground(Color.WHITE);
        toolBarButtonFileNew.setIcon(new ImageIcon("resources/imgToolbar/archive-new-22.png"));
        toolBarButtonFileOpen.setIcon(new ImageIcon("resources/imgToolbar/archive-open-22.png"));
        toolBarButtonFileSave.setIcon(new ImageIcon("resources/imgToolbar/archive-save-as-22.png"));
        toolBarButtonEditUndo.setIcon(new ImageIcon("resources/imgToolbar/edit-undo-22.png"));
        toolBarButtonEditRedo.setIcon(new ImageIcon("resources/imgToolbar/edit-redo-22.png"));
        toolBarButtonEditCut.setIcon(new ImageIcon("resources/imgToolbar/edit-cut-22.png"));
        toolBarButtonEditCopy.setIcon(new ImageIcon("resources/imgToolbar/edit-copy-22.png"));
        toolBarButtonEditPaste.setIcon(new ImageIcon("resources/imgToolbar/edit-paste-22.png"));
        toolBarButtonPictogramsNextImage.setIcon(new ImageIcon(new ImageIcon("resources/imgToolbar/pictograms-next-image.png").getImage().getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH)));
        toolBarButtonPictogramsCompoundSplitWord.setIcon(new ImageIcon(new ImageIcon("resources/imgToolbar/pictograms-compound-split-word.png").getImage().getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH)));
        toolBarButtonPictogramsChangeName.setIcon(new ImageIcon(new ImageIcon("resources/imgToolbar/pictograms-change-name.png").getImage().getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH)));
        setToolBar(toolBar);
        aboutDialogImage.setIcon(new ImageIcon("resources/logoSmall.png"));
        try {
            TSetup.load();
            TLanguage.initLanguage(G.applicationLanguage);
        } catch (Exception e) {
            System.out.println(e);
        }
        G.documentLanguage = G.defaultDocumentLanguage;
        G.imagesSize = G.defaultImagesSize;
        G.font = new Font(G.defaultFont.getName(), G.defaultFont.getStyle(), G.defaultFont.getSize());
        G.color = new Color(G.defaultColor.getRed(), G.defaultColor.getBlue(), G.defaultColor.getGreen());
        G.textBelowPictogram = G.defaultTextBelowPictogram;
        ImageIcon image = new ImageIcon("resources/404.jpg");
        G.notFound = new ImageIcon(image.getImage().getScaledInstance(-1, G.imagesSize, 0));
        setApplicationLanguage();
        menuFileNew.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
        menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
        menuFileSave.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
        menuFileExit.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.Event.CTRL_MASK));
        menuEditCut.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.Event.CTRL_MASK));
        menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.Event.CTRL_MASK));
        menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.Event.CTRL_MASK));
        menuEditUndo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.Event.CTRL_MASK));
        menuEditRedo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.Event.CTRL_MASK));
        menuEditFind.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.Event.CTRL_MASK));
        menuEditSelectAll.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.Event.CTRL_MASK));
        menuPictogramsNextImage.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        menuPictogramsCompoundSplitWord.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        menuPictogramsChangeName.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        menuHelpShowHelp.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        this.getFrame().pack();
        this.getFrame().setIconImage(Toolkit.getDefaultToolkit().getImage("resources/logo.png"));
        G.giveMePrivateVariables(textZone);
        try {
            DBManagement.connectDB();
            DBManagement.connectVerbsDB();
            DBManagement.createAraWordView(G.documentLanguage);
            TextUtils.newDocument();
        } catch (Exception exc) {
            System.out.println(exc);
        }
        this.getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.getFrame().addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(getFrame(), TLanguage.getString("FILE_MENU_EXIT_WARNING"), TLanguage.getString("WARNING"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    System.exit(0);
                }
            }
        });
    }

    private void setApplicationLanguage() {
        menuFile.setText(TLanguage.getString("FILE_MENU"));
        menuFileNew.setText(TLanguage.getString("FILE_MENU_NEW"));
        menuFileOpen.setText(TLanguage.getString("FILE_MENU_LOAD"));
        menuFileSave.setText(TLanguage.getString("FILE_MENU_SAVE"));
        menuFileSaveAs.setText(TLanguage.getString("FILE_MENU_SAVE_AS"));
        menuFileExport.setText(TLanguage.getString("FILE_MENU_EXPORT"));
        menuFileExit.setText(TLanguage.getString("FILE_MENU_EXIT"));
        menuEdit.setText(TLanguage.getString("EDIT_MENU"));
        menuEditCut.setText(TLanguage.getString("EDIT_MENU_CUT"));
        menuEditCopy.setText(TLanguage.getString("EDIT_MENU_COPY"));
        menuEditPaste.setText(TLanguage.getString("EDIT_MENU_PASTE"));
        menuEditUndo.setText(TLanguage.getString("EDIT_MENU_UNDO"));
        menuEditRedo.setText(TLanguage.getString("EDIT_MENU_REDO"));
        menuEditFind.setText(TLanguage.getString("EDIT_MENU_FIND"));
        menuEditSelectAll.setText(TLanguage.getString("EDIT_MENU_SELECT_ALL"));
        menuText.setText(TLanguage.getString("TEXT_MENU"));
        menuTextFont.setText(TLanguage.getString("TEXT_MENU_FONT"));
        menuTextColor.setText(TLanguage.getString("TEXT_MENU_COLOR"));
        menuTextPlacement.setText(TLanguage.getString("TEXT_MENU_PLACEMENT"));
        menuTextPlacementAbovePictogram.setText(TLanguage.getString("TEXT_MENU_PLACEMENT_ABOVE_PICTOGRAM"));
        menuTextPlacementBelowPictogram.setText(TLanguage.getString("TEXT_MENU_PLACEMENT_BELOW_PICTOGRAM"));
        menuTextToUpperCase.setText(TLanguage.getString("TEXT_MENU_TO_UPPER_CASE"));
        menuTextToUpperCaseActiveElement.setText(TLanguage.getString("TEXT_MENU_TO_UPPER_CASE_ACTIVE_ELEMENT"));
        menuTextToUpperCaseAllElements.setText(TLanguage.getString("TEXT_MENU_TO_UPPER_CASE_ALL_ELEMENTS"));
        menuTextToLowerCase.setText(TLanguage.getString("TEXT_MENU_TO_LOWER_CASE"));
        menuTextToLowerCaseActiveElement.setText(TLanguage.getString("TEXT_MENU_TO_LOWER_CASE_ACTIVE_ELEMENT"));
        menuTextToLowerCaseAllElements.setText(TLanguage.getString("TEXT_MENU_TO_LOWER_CASE_ALL_ELEMENTS"));
        menuTextDocumentLanguage.setText(TLanguage.getString("TEXT_MENU_DOCUMENT_LANGUAGE"));
        menuPictograms.setText(TLanguage.getString("PICTOGRAMS_MENU"));
        menuPictogramsSize.setText(TLanguage.getString("PICTOGRAMS_MENU_SIZE"));
        menuPictogramsNextImage.setText(TLanguage.getString("PICTOGRAMS_MENU_NEXT_IMAGE"));
        menuPictogramsCompoundSplitWord.setText(TLanguage.getString("PICTOGRAMS_MENU_COMPOUND_SPLIT_WORD"));
        menuPictogramsChangeName.setText(TLanguage.getString("PICTOGRAMS_MENU_CHANGE_NAME"));
        menuPictogramsHide.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE"));
        menuPictogramsHideBorder.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE_BORDER"));
        menuPictogramsHideBorderActiveElement.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE_BORDER_ACTIVE_ELEMENT"));
        menuPictogramsHideBorderAllElements.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE_BORDER_ALL_ELEMENTS"));
        menuPictogramsHideImage.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE_IMAGE"));
        menuPictogramsHideImageActiveElement.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE_IMAGE_ACTIVE_ELEMENT"));
        menuPictogramsHideImageAllElements.setText(TLanguage.getString("PICTOGRAMS_MENU_HIDE_IMAGE_ALL_ELEMENTS"));
        menuPictogramsShow.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW"));
        menuPictogramsShowBorder.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW_BORDER"));
        menuPictogramsShowBorderActiveElement.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW_BORDER_ACTIVE_ELEMENT"));
        menuPictogramsShowBorderAllElements.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW_BORDER_ALL_ELEMENTS"));
        menuPictogramsShowImage.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW_IMAGE"));
        menuPictogramsShowImageActiveElement.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW_IMAGE_ACTIVE_ELEMENT"));
        menuPictogramsShowImageAllElements.setText(TLanguage.getString("PICTOGRAMS_MENU_SHOW_IMAGE_ALL_ELEMENTS"));
        menuTools.setText(TLanguage.getString("TOOLS_MENU"));
        menuToolsResourceManager.setText(TLanguage.getString("TOOLS_MENU_RESOURCE_MANAGER"));
        menuToolsGeneralPreferences.setText(TLanguage.getString("TOOLS_MENU_GENERAL_PREFERENCES"));
        menuHelp.setText(TLanguage.getString("HELP_MENU"));
        menuHelpShowHelp.setText(TLanguage.getString("HELP_MENU_SHOW_HELP"));
        menuHelpAbout.setText(TLanguage.getString("HELP_MENU_ABOUT"));
        generalPreferencesDialog.setTitle(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_TITLE"));
        generalPreferencesDialogApplicationLanguageLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_APPLICATION_LANGUAGE_LABEL"));
        generalPreferencesDialogDocumentLanguageLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_DOCUMENT_LANGUAGE_LABEL"));
        generalPreferencesDialogImagesSizeLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_IMAGES_SIZE_LABEL"));
        generalPreferencesDialogMaxLengthCompoundWordsLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_MAX_LENGTH_COMPOUND_WORDS_LABEL"));
        generalPreferencesDialogMaxUndoLevelLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_MAX_UNDO_LEVEL_LABEL"));
        generalPreferencesDialogPictogramsPathLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_PICTOGRAMS_PATH_LABEL"));
        generalPreferencesDialogChoosePictogramsPathButton.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_PICTOGRAMS_PATH_CHOOSE_BUTTON"));
        generalPreferencesDialogTextFontLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_TEXT_FONT_LABEL"));
        generalPreferencesDialogChooseTextFontButton.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_TEXT_FONT_CHOOSE_BUTTON"));
        generalPreferencesDialogTextColorLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_TEXT_COLOR_LABEL"));
        generalPreferencesDialogChooseTextColorButton.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_TEXT_COLOR_CHOOSE_BUTTON"));
        generalPreferencesDialogTextPlacementLabel.setText(TLanguage.getString("GENERAL_PREFERENCES_DIALOG_TEXT_PLACEMENT_LABEL"));
        generalPreferencesDialogOKButton.setText(TLanguage.getString("OK"));
        generalPreferencesDialogCancelButton.setText(TLanguage.getString("CANCEL"));
        documentLanguageDialog.setTitle(TLanguage.getString("DOCUMENT_LANGUAGE_DIALOG_TITLE"));
        documentLanguageDialogDocumentLanguageLabel.setText(TLanguage.getString("DOCUMENT_LANGUAGE_DIALOG_DOCUMENT_LANGUAGE_LABEL"));
        documentLanguageDialogOKButton.setText(TLanguage.getString("OK"));
        documentLanguageDialogCancelButton.setText(TLanguage.getString("CANCEL"));
        imagesSizeDialog.setTitle(TLanguage.getString("IMAGES_SIZE_DIALOG_TITLE"));
        imagesSizeDialogImagesSizeLabel.setText(TLanguage.getString("IMAGES_SIZE_DIALOG_IMAGES_SIZE_LABEL"));
        imagesSizeDialogOKButton.setText(TLanguage.getString("OK"));
        imagesSizeDialogCancelButton.setText(TLanguage.getString("CANCEL"));
        aboutDialog.setTitle(TLanguage.getString("ABOUT_DIALOG_TITLE"));
        aboutDialogCloseButton.setText(TLanguage.getString("CLOSE"));
        String str = "";
        str = "******* AraWord 1.0 *******\n\n";
        str = str + "--- " + TLanguage.getString("TAboutDialog.DEVELOPERS") + " ---\n";
        str = str + "# Joaquín Pérez Marco\n";
        str = str + "--- " + TLanguage.getString("TAboutDialog.DIRECTOR") + " ---\n";
        str = str + "# Dr. Joaquín Ezpeleta Mateo (EINA)\n";
        str = str + "--- " + TLanguage.getString("TAboutDialog.COLLABORATORS") + " ---\n";
        str = str + "# César Canalis (CPEE Alborada)\n";
        str = str + "# José Manuel Marcos (CPEE Alborada)\n";
        str = str + "# David Romero Corral (ARASAAC)\n";
        str = str + "--- " + TLanguage.getString("TAboutDialog.ORGANIZATIONS") + " ---\n";
        str = str + "# Escuela de Ingeniería y Arquitectura (EINA)\n";
        str = str + "# Universidad de Zaragoza (UZ)\n";
        str = str + "# Colegio Público de Educación Especial Alborada (CPEE Alborada)\n";
        str = str + "# Portal Aragonés de la Comunicación Aumentativa y Alternativa (ARASAAC)\n";
        str = str + "--- " + TLanguage.getString("TAboutDialog.YEAR") + " 2011 ---\n";
        str = str + "--- " + TLanguage.getString("TAboutDialog.LICENSE") + " GPL v3 ---\n\n";
        str = str + "******* Pictogramas *******\n\n";
        str = str + "# Autor: Sergio Palao\n";
        str = str + "# Procedencia: ARASAAC (http://arasaac.org)\n";
        str = str + "# Licencia: Creative Commons (BY-NC-SA)";
        aboutDialogTextArea.setText(str);
        toolBarButtonFileNew.setToolTipText(menuFileNew.getText());
        toolBarButtonFileOpen.setToolTipText(menuFileOpen.getText());
        toolBarButtonFileSave.setToolTipText(menuFileSave.getText());
        toolBarButtonEditUndo.setToolTipText(menuEditUndo.getText());
        toolBarButtonEditRedo.setToolTipText(menuEditRedo.getText());
        toolBarButtonEditCut.setToolTipText(menuEditCut.getText());
        toolBarButtonEditCopy.setToolTipText(menuEditCopy.getText());
        toolBarButtonEditPaste.setToolTipText(menuEditPaste.getText());
        toolBarButtonPictogramsNextImage.setToolTipText(menuPictogramsNextImage.getText());
        toolBarButtonPictogramsCompoundSplitWord.setToolTipText(menuPictogramsCompoundSplitWord.getText());
        toolBarButtonPictogramsChangeName.setToolTipText(menuPictogramsChangeName.getText());
        SpinnerModel model = new SpinnerListModel(G.applicationLanguages);
        generalPreferencesDialogSpinnerApplicationLanguage.setModel(model);
        SpinnerModel model2 = new SpinnerListModel(G.documentLanguages);
        generalPreferencesDialogSpinnerDocumentLanguage.setModel(model2);
        SpinnerModel model3 = new SpinnerListModel(G.documentLanguages);
        documentLanguageDialogSpinnerDocumentLanguage.setModel(model3);
        SpinnerModel model4 = new SpinnerListModel(new String[] { TLanguage.getString("SPINNER_TEXT_BELOW_PICTOGRAM"), TLanguage.getString("SPINNER_TEXT_ABOVE_PICTOGRAM") });
        generalPreferencesDialogSpinnerTextPlacement.setModel(model4);
        generalPreferencesDialogSpinnerImagesSize.setValue(G.defaultImagesSize);
        generalPreferencesDialogSpinnerMaxLengthCompoundWords.setValue(G.maxLengthCompoundWords);
        generalPreferencesDialogSpinnerMaxUndoLevel.setValue(G.maxUndoLevel);
        generalPreferencesDialogSpinnerDocumentLanguage.setValue(G.defaultDocumentLanguage);
        generalPreferencesDialogSpinnerApplicationLanguage.setValue(G.applicationLanguage);
        if (G.textBelowPictogram) generalPreferencesDialogSpinnerTextPlacement.setValue(TLanguage.getString("SPINNER_TEXT_BELOW_PICTOGRAM")); else generalPreferencesDialogSpinnerTextPlacement.setValue(TLanguage.getString("SPINNER_TEXT_ABOVE_PICTOGRAM"));
        documentLanguageDialogSpinnerDocumentLanguage.setValue(G.documentLanguage);
    }

    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        scrollTextZone = new javax.swing.JScrollPane();
        textZone = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuFileNew = new javax.swing.JMenuItem();
        menuFileOpen = new javax.swing.JMenuItem();
        menuFileSave = new javax.swing.JMenuItem();
        menuFileSaveAs = new javax.swing.JMenuItem();
        menuFileExport = new javax.swing.JMenuItem();
        menuFileExit = new javax.swing.JMenuItem();
        menuEdit = new javax.swing.JMenu();
        menuEditUndo = new javax.swing.JMenuItem();
        menuEditRedo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuEditCut = new javax.swing.JMenuItem();
        menuEditCopy = new javax.swing.JMenuItem();
        menuEditPaste = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menuEditFind = new javax.swing.JMenuItem();
        menuEditSelectAll = new javax.swing.JMenuItem();
        menuText = new javax.swing.JMenu();
        menuTextFont = new javax.swing.JMenuItem();
        menuTextColor = new javax.swing.JMenuItem();
        menuTextPlacement = new javax.swing.JMenu();
        menuTextPlacementAbovePictogram = new javax.swing.JMenuItem();
        menuTextPlacementBelowPictogram = new javax.swing.JMenuItem();
        menuTextToUpperCase = new javax.swing.JMenu();
        menuTextToUpperCaseActiveElement = new javax.swing.JMenuItem();
        menuTextToUpperCaseAllElements = new javax.swing.JMenuItem();
        menuTextToLowerCase = new javax.swing.JMenu();
        menuTextToLowerCaseActiveElement = new javax.swing.JMenuItem();
        menuTextToLowerCaseAllElements = new javax.swing.JMenuItem();
        menuTextDocumentLanguage = new javax.swing.JMenuItem();
        menuPictograms = new javax.swing.JMenu();
        menuPictogramsSize = new javax.swing.JMenuItem();
        menuPictogramsNextImage = new javax.swing.JMenuItem();
        menuPictogramsCompoundSplitWord = new javax.swing.JMenuItem();
        menuPictogramsChangeName = new javax.swing.JMenuItem();
        menuPictogramsShow = new javax.swing.JMenu();
        menuPictogramsShowImage = new javax.swing.JMenu();
        menuPictogramsShowImageActiveElement = new javax.swing.JMenuItem();
        menuPictogramsShowImageAllElements = new javax.swing.JMenuItem();
        menuPictogramsShowBorder = new javax.swing.JMenu();
        menuPictogramsShowBorderActiveElement = new javax.swing.JMenuItem();
        menuPictogramsShowBorderAllElements = new javax.swing.JMenuItem();
        menuPictogramsHide = new javax.swing.JMenu();
        menuPictogramsHideImage = new javax.swing.JMenu();
        menuPictogramsHideImageActiveElement = new javax.swing.JMenuItem();
        menuPictogramsHideImageAllElements = new javax.swing.JMenuItem();
        menuPictogramsHideBorder = new javax.swing.JMenu();
        menuPictogramsHideBorderActiveElement = new javax.swing.JMenuItem();
        menuPictogramsHideBorderAllElements = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        menuToolsResourceManager = new javax.swing.JMenuItem();
        menuToolsGeneralPreferences = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        menuHelpShowHelp = new javax.swing.JMenuItem();
        menuHelpAbout = new javax.swing.JMenuItem();
        aboutDialog = new javax.swing.JDialog();
        aboutDialogCloseButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        aboutDialogTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        aboutDialogImage = new javax.swing.JLabel();
        findDialog = new javax.swing.JDialog();
        findDialogFindTextLabel = new javax.swing.JLabel();
        findDialogTextField = new javax.swing.JTextField();
        findDialogFindButton = new javax.swing.JButton();
        findDialogExitButton = new javax.swing.JButton();
        generalPreferencesDialog = new javax.swing.JDialog();
        generalPreferencesDialogSpinnerImagesSize = new javax.swing.JSpinner();
        generalPreferencesDialogImagesSizeLabel = new javax.swing.JLabel();
        generalPreferencesDialogMaxUndoLevelLabel = new javax.swing.JLabel();
        generalPreferencesDialogSpinnerMaxUndoLevel = new javax.swing.JSpinner();
        generalPreferencesDialogMaxLengthCompoundWordsLabel = new javax.swing.JLabel();
        generalPreferencesDialogSpinnerMaxLengthCompoundWords = new javax.swing.JSpinner();
        generalPreferencesDialogApplicationLanguageLabel = new javax.swing.JLabel();
        generalPreferencesDialogSpinnerApplicationLanguage = new javax.swing.JSpinner();
        generalPreferencesDialogDocumentLanguageLabel = new javax.swing.JLabel();
        generalPreferencesDialogSpinnerDocumentLanguage = new javax.swing.JSpinner();
        generalPreferencesDialogOKButton = new javax.swing.JButton();
        generalPreferencesDialogCancelButton = new javax.swing.JButton();
        generalPreferencesDialogPictogramsPathLabel = new javax.swing.JLabel();
        generalPreferencesDialogChoosePictogramsPathButton = new javax.swing.JButton();
        generalPreferencesDialogTextFontLabel = new javax.swing.JLabel();
        generalPreferencesDialogChooseTextFontButton = new javax.swing.JButton();
        generalPreferencesDialogTextColorLabel = new javax.swing.JLabel();
        generalPreferencesDialogChooseTextColorButton = new javax.swing.JButton();
        generalPreferencesDialogTextPlacementLabel = new javax.swing.JLabel();
        generalPreferencesDialogSpinnerTextPlacement = new javax.swing.JSpinner();
        toolBar = new javax.swing.JToolBar();
        toolBarButtonFileNew = new javax.swing.JButton();
        toolBarButtonFileOpen = new javax.swing.JButton();
        toolBarButtonFileSave = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        toolBarButtonEditUndo = new javax.swing.JButton();
        toolBarButtonEditRedo = new javax.swing.JButton();
        toolBarButtonEditCut = new javax.swing.JButton();
        toolBarButtonEditCopy = new javax.swing.JButton();
        toolBarButtonEditPaste = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        toolBarButtonPictogramsNextImage = new javax.swing.JButton();
        toolBarButtonPictogramsCompoundSplitWord = new javax.swing.JButton();
        toolBarButtonPictogramsChangeName = new javax.swing.JButton();
        documentLanguageDialog = new javax.swing.JDialog();
        documentLanguageDialogDocumentLanguageLabel = new javax.swing.JLabel();
        documentLanguageDialogSpinnerDocumentLanguage = new javax.swing.JSpinner();
        documentLanguageDialogOKButton = new javax.swing.JButton();
        documentLanguageDialogCancelButton = new javax.swing.JButton();
        imagesSizeDialog = new javax.swing.JDialog();
        imagesSizeDialogImagesSizeLabel = new javax.swing.JLabel();
        imagesSizeDialogSpinnerImagesSize = new javax.swing.JSpinner();
        imagesSizeDialogCancelButton = new javax.swing.JButton();
        imagesSizeDialogOKButton = new javax.swing.JButton();
        mainPanel.setName("mainPanel");
        scrollTextZone.setName("scrollTextZone");
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(araword.AraWordApp.class).getContext().getResourceMap(GUI.class);
        textZone.setContentType(resourceMap.getString("textZone.contentType"));
        textZone.setEditable(false);
        textZone.setFocusable(false);
        textZone.setHighlighter(null);
        textZone.setMargin(new java.awt.Insets(10, 10, 10, 10));
        textZone.setName("textZone");
        textZone.setRequestFocusEnabled(false);
        scrollTextZone.setViewportView(textZone);
        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(mainPanelLayout.createSequentialGroup().addContainerGap().add(scrollTextZone, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 859, Short.MAX_VALUE).addContainerGap()));
        mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(mainPanelLayout.createSequentialGroup().addContainerGap().add(scrollTextZone, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE).addContainerGap()));
        menuBar.setName("menuBar");
        menuFile.setText(resourceMap.getString("menuFile.text"));
        menuFile.setName("menuFile");
        menuFileNew.setText(resourceMap.getString("menuFileNew.text"));
        menuFileNew.setName("menuFileNew");
        menuFileNew.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileNewActionPerformed(evt);
            }
        });
        menuFile.add(menuFileNew);
        menuFileOpen.setText(resourceMap.getString("menuFileOpen.text"));
        menuFileOpen.setName("menuFileOpen");
        menuFileOpen.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileOpenActionPerformed(evt);
            }
        });
        menuFile.add(menuFileOpen);
        menuFileSave.setText(resourceMap.getString("menuFileSave.text"));
        menuFileSave.setName("menuFileSave");
        menuFileSave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuFileSave);
        menuFileSaveAs.setText(resourceMap.getString("menuFileSaveAs.text"));
        menuFileSaveAs.setName("menuFileSaveAs");
        menuFileSaveAs.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuFileSaveAs);
        menuFileExport.setText(resourceMap.getString("menuFileExport.text"));
        menuFileExport.setName("menuFileExport");
        menuFileExport.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileExportActionPerformed(evt);
            }
        });
        menuFile.add(menuFileExport);
        menuFileExit.setText(resourceMap.getString("menuFileExit.text"));
        menuFileExit.setName("menuFileExit");
        menuFileExit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileExitActionPerformed(evt);
            }
        });
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
        menuEdit.setText(resourceMap.getString("menuEdit.text"));
        menuEdit.setName("menuEdit");
        menuEditUndo.setText(resourceMap.getString("menuEditUndo.text"));
        menuEditUndo.setName("menuEditUndo");
        menuEditUndo.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditUndoActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditUndo);
        menuEditRedo.setText(resourceMap.getString("menuEditRedo.text"));
        menuEditRedo.setName("menuEditRedo");
        menuEditRedo.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditRedoActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditRedo);
        jSeparator1.setName("jSeparator1");
        menuEdit.add(jSeparator1);
        menuEditCut.setText(resourceMap.getString("menuEditCut.text"));
        menuEditCut.setName("menuEditCut");
        menuEditCut.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditCutActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditCut);
        menuEditCopy.setText(resourceMap.getString("menuEditCopy.text"));
        menuEditCopy.setName("menuEditCopy");
        menuEditCopy.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditCopyActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditCopy);
        menuEditPaste.setText(resourceMap.getString("menuEditPaste.text"));
        menuEditPaste.setName("menuEditPaste");
        menuEditPaste.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditPasteActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditPaste);
        jSeparator2.setName("jSeparator2");
        menuEdit.add(jSeparator2);
        menuEditFind.setText(resourceMap.getString("menuEditFind.text"));
        menuEditFind.setName("menuEditFind");
        menuEditFind.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditFindActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditFind);
        menuEditSelectAll.setText(resourceMap.getString("menuEditSelectAll.text"));
        menuEditSelectAll.setName("menuEditSelectAll");
        menuEditSelectAll.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditSelectAllActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditSelectAll);
        menuBar.add(menuEdit);
        menuText.setText(resourceMap.getString("menuText.text"));
        menuText.setName("menuText");
        menuTextFont.setText(resourceMap.getString("menuTextFont.text"));
        menuTextFont.setName("menuTextFont");
        menuTextFont.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextFontActionPerformed(evt);
            }
        });
        menuText.add(menuTextFont);
        menuTextColor.setText(resourceMap.getString("menuTextColor.text"));
        menuTextColor.setName("menuTextColor");
        menuTextColor.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextColorActionPerformed(evt);
            }
        });
        menuText.add(menuTextColor);
        menuTextPlacement.setText(resourceMap.getString("menuTextPlacement.text"));
        menuTextPlacement.setName("menuTextPlacement");
        menuTextPlacementAbovePictogram.setText(resourceMap.getString("menuTextPlacementAbovePictogram.text"));
        menuTextPlacementAbovePictogram.setName("menuTextPlacementAbovePictogram");
        menuTextPlacementAbovePictogram.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextPlacementAbovePictogramActionPerformed(evt);
            }
        });
        menuTextPlacement.add(menuTextPlacementAbovePictogram);
        menuTextPlacementBelowPictogram.setText(resourceMap.getString("menuTextPlacementBelowPictogram.text"));
        menuTextPlacementBelowPictogram.setName("menuTextPlacementBelowPictogram");
        menuTextPlacementBelowPictogram.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextPlacementBelowPictogramActionPerformed(evt);
            }
        });
        menuTextPlacement.add(menuTextPlacementBelowPictogram);
        menuText.add(menuTextPlacement);
        menuTextToUpperCase.setText(resourceMap.getString("menuTextToUpperCase.text"));
        menuTextToUpperCase.setName("menuTextToUpperCase");
        menuTextToUpperCaseActiveElement.setText(resourceMap.getString("menuTextToUpperCaseActiveElement.text"));
        menuTextToUpperCaseActiveElement.setName("menuTextToUpperCaseActiveElement");
        menuTextToUpperCaseActiveElement.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextToUpperCaseActiveElementActionPerformed(evt);
            }
        });
        menuTextToUpperCase.add(menuTextToUpperCaseActiveElement);
        menuTextToUpperCaseAllElements.setText(resourceMap.getString("menuTextToUpperCaseAllElements.text"));
        menuTextToUpperCaseAllElements.setName("menuTextToUpperCaseAllElements");
        menuTextToUpperCaseAllElements.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextToUpperCaseAllElementsActionPerformed(evt);
            }
        });
        menuTextToUpperCase.add(menuTextToUpperCaseAllElements);
        menuText.add(menuTextToUpperCase);
        menuTextToLowerCase.setText(resourceMap.getString("menuTextToLowerCase.text"));
        menuTextToLowerCase.setName("menuTextToLowerCase");
        menuTextToLowerCaseActiveElement.setText(resourceMap.getString("menuTextToLowerCaseActiveElement.text"));
        menuTextToLowerCaseActiveElement.setName("menuTextToLowerCaseActiveElement");
        menuTextToLowerCaseActiveElement.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextToLowerCaseActiveElementActionPerformed(evt);
            }
        });
        menuTextToLowerCase.add(menuTextToLowerCaseActiveElement);
        menuTextToLowerCaseAllElements.setText(resourceMap.getString("menuTextToLowerCaseAllElements.text"));
        menuTextToLowerCaseAllElements.setName("menuTextToLowerCaseAllElements");
        menuTextToLowerCaseAllElements.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextToLowerCaseAllElementsActionPerformed(evt);
            }
        });
        menuTextToLowerCase.add(menuTextToLowerCaseAllElements);
        menuText.add(menuTextToLowerCase);
        menuTextDocumentLanguage.setText(resourceMap.getString("menuTextDocumentLanguage.text"));
        menuTextDocumentLanguage.setName("menuTextDocumentLanguage");
        menuTextDocumentLanguage.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTextDocumentLanguageActionPerformed(evt);
            }
        });
        menuText.add(menuTextDocumentLanguage);
        menuBar.add(menuText);
        menuPictograms.setText(resourceMap.getString("menuPictograms.text"));
        menuPictograms.setName("menuPictograms");
        menuPictogramsSize.setText(resourceMap.getString("menuPictogramsSize.text"));
        menuPictogramsSize.setName("menuPictogramsSize");
        menuPictogramsSize.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsSizeActionPerformed(evt);
            }
        });
        menuPictograms.add(menuPictogramsSize);
        menuPictogramsNextImage.setText(resourceMap.getString("menuPictogramsNextImage.text"));
        menuPictogramsNextImage.setName("menuPictogramsNextImage");
        menuPictogramsNextImage.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsNextImageActionPerformed(evt);
            }
        });
        menuPictograms.add(menuPictogramsNextImage);
        menuPictogramsCompoundSplitWord.setText(resourceMap.getString("menuPictogramsCompoundSplitWord.text"));
        menuPictogramsCompoundSplitWord.setName("menuPictogramsCompoundSplitWord");
        menuPictogramsCompoundSplitWord.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsCompoundSplitWordActionPerformed(evt);
            }
        });
        menuPictograms.add(menuPictogramsCompoundSplitWord);
        menuPictogramsChangeName.setText(resourceMap.getString("menuPictogramsChangeName.text"));
        menuPictogramsChangeName.setName("menuPictogramsChangeName");
        menuPictogramsChangeName.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsChangeNameActionPerformed(evt);
            }
        });
        menuPictograms.add(menuPictogramsChangeName);
        menuPictogramsShow.setText(resourceMap.getString("menuPictogramsShow.text"));
        menuPictogramsShow.setName("menuPictogramsShow");
        menuPictogramsShowImage.setText(resourceMap.getString("menuPictogramsShowImage.text"));
        menuPictogramsShowImage.setName("menuPictogramsShowImage");
        menuPictogramsShowImageActiveElement.setText(resourceMap.getString("menuPictogramsShowImageActiveElement.text"));
        menuPictogramsShowImageActiveElement.setName("menuPictogramsShowImageActiveElement");
        menuPictogramsShowImageActiveElement.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsShowImageActiveElementActionPerformed(evt);
            }
        });
        menuPictogramsShowImage.add(menuPictogramsShowImageActiveElement);
        menuPictogramsShowImageAllElements.setText(resourceMap.getString("menuPictogramsShowImageAllElements.text"));
        menuPictogramsShowImageAllElements.setName("menuPictogramsShowImageAllElements");
        menuPictogramsShowImageAllElements.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsShowImageAllElementsActionPerformed(evt);
            }
        });
        menuPictogramsShowImage.add(menuPictogramsShowImageAllElements);
        menuPictogramsShow.add(menuPictogramsShowImage);
        menuPictogramsShowBorder.setText(resourceMap.getString("menuPictogramsShowBorder.text"));
        menuPictogramsShowBorder.setName("menuPictogramsShowBorder");
        menuPictogramsShowBorderActiveElement.setText(resourceMap.getString("menuPictogramsShowBorderActiveElement.text"));
        menuPictogramsShowBorderActiveElement.setName("menuPictogramsShowBorderActiveElement");
        menuPictogramsShowBorderActiveElement.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsShowBorderActiveElementActionPerformed(evt);
            }
        });
        menuPictogramsShowBorder.add(menuPictogramsShowBorderActiveElement);
        menuPictogramsShowBorderAllElements.setText(resourceMap.getString("menuPictogramsShowBorderAllElements.text"));
        menuPictogramsShowBorderAllElements.setName("menuPictogramsShowBorderAllElements");
        menuPictogramsShowBorderAllElements.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsShowBorderAllElementsActionPerformed(evt);
            }
        });
        menuPictogramsShowBorder.add(menuPictogramsShowBorderAllElements);
        menuPictogramsShow.add(menuPictogramsShowBorder);
        menuPictograms.add(menuPictogramsShow);
        menuPictogramsHide.setText(resourceMap.getString("menuPictogramsHide.text"));
        menuPictogramsHide.setName("menuPictogramsHide");
        menuPictogramsHideImage.setText(resourceMap.getString("menuPictogramsHideImage.text"));
        menuPictogramsHideImage.setName("menuPictogramsHideImage");
        menuPictogramsHideImageActiveElement.setText(resourceMap.getString("menuPictogramsHideImageActiveElement.text"));
        menuPictogramsHideImageActiveElement.setName("menuPictogramsHideImageActiveElement");
        menuPictogramsHideImageActiveElement.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsHideImageActiveElementActionPerformed(evt);
            }
        });
        menuPictogramsHideImage.add(menuPictogramsHideImageActiveElement);
        menuPictogramsHideImageAllElements.setText(resourceMap.getString("menuPictogramsHideImageAllElements.text"));
        menuPictogramsHideImageAllElements.setName("menuPictogramsHideImageAllElements");
        menuPictogramsHideImageAllElements.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsHideImageAllElementsActionPerformed(evt);
            }
        });
        menuPictogramsHideImage.add(menuPictogramsHideImageAllElements);
        menuPictogramsHide.add(menuPictogramsHideImage);
        menuPictogramsHideBorder.setText(resourceMap.getString("menuPictogramsHideBorder.text"));
        menuPictogramsHideBorder.setName("menuPictogramsHideBorder");
        menuPictogramsHideBorderActiveElement.setText(resourceMap.getString("menuPictogramsHideBorderActiveElement.text"));
        menuPictogramsHideBorderActiveElement.setName("menuPictogramsHideBorderActiveElement");
        menuPictogramsHideBorderActiveElement.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsHideBorderActiveElementActionPerformed(evt);
            }
        });
        menuPictogramsHideBorder.add(menuPictogramsHideBorderActiveElement);
        menuPictogramsHideBorderAllElements.setText(resourceMap.getString("menuPictogramsHideBorderAllElements.text"));
        menuPictogramsHideBorderAllElements.setName("menuPictogramsHideBorderAllElements");
        menuPictogramsHideBorderAllElements.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPictogramsHideBorderAllElementsActionPerformed(evt);
            }
        });
        menuPictogramsHideBorder.add(menuPictogramsHideBorderAllElements);
        menuPictogramsHide.add(menuPictogramsHideBorder);
        menuPictograms.add(menuPictogramsHide);
        menuBar.add(menuPictograms);
        menuTools.setText(resourceMap.getString("menuTools.text"));
        menuToolsResourceManager.setText(resourceMap.getString("menuToolsResourceManager.text"));
        menuToolsResourceManager.setName("menuToolsResourceManager");
        menuToolsResourceManager.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuToolsResourceManagerActionPerformed(evt);
            }
        });
        menuTools.add(menuToolsResourceManager);
        menuToolsGeneralPreferences.setText(resourceMap.getString("menuToolsGeneralPreferences.text"));
        menuToolsGeneralPreferences.setName("menuToolsGeneralPreferences");
        menuToolsGeneralPreferences.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuToolsGeneralPreferencesActionPerformed(evt);
            }
        });
        menuTools.add(menuToolsGeneralPreferences);
        menuBar.add(menuTools);
        menuHelp.setText(resourceMap.getString("menuHelp.text"));
        menuHelp.setName("menuHelp");
        menuHelpShowHelp.setText(resourceMap.getString("menuHelpShowHelp.text"));
        menuHelpShowHelp.setName("menuHelpShowHelp");
        menuHelpShowHelp.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuHelpShowHelpActionPerformed(evt);
            }
        });
        menuHelp.add(menuHelpShowHelp);
        menuHelpAbout.setText(resourceMap.getString("menuHelpAbout.text"));
        menuHelpAbout.setName("menuHelpAbout");
        menuHelpAbout.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuHelpAboutActionPerformed(evt);
            }
        });
        menuHelp.add(menuHelpAbout);
        menuBar.add(menuHelp);
        aboutDialog.setTitle(resourceMap.getString("aboutDialog.title"));
        aboutDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        aboutDialog.setModal(true);
        aboutDialog.setName("aboutDialog");
        aboutDialog.setResizable(false);
        aboutDialogCloseButton.setText(resourceMap.getString("aboutDialogCloseButton.text"));
        aboutDialogCloseButton.setName("aboutDialogCloseButton");
        aboutDialogCloseButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutDialogCloseButtonActionPerformed(evt);
            }
        });
        jScrollPane1.setName("jScrollPane1");
        aboutDialogTextArea.setColumns(20);
        aboutDialogTextArea.setEditable(false);
        aboutDialogTextArea.setRows(5);
        aboutDialogTextArea.setFocusable(false);
        aboutDialogTextArea.setName("aboutDialogTextArea");
        jScrollPane1.setViewportView(aboutDialogTextArea);
        jPanel1.setName("jPanel1");
        aboutDialogImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aboutDialogImage.setText(resourceMap.getString("aboutDialogImage.text"));
        aboutDialogImage.setName("aboutDialogImage");
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(aboutDialogImage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 702, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, aboutDialogImage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE));
        org.jdesktop.layout.GroupLayout aboutDialogLayout = new org.jdesktop.layout.GroupLayout(aboutDialog.getContentPane());
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);
        aboutDialogLayout.setHorizontalGroup(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(aboutDialogLayout.createSequentialGroup().addContainerGap().add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, aboutDialogLayout.createSequentialGroup().add(aboutDialogCloseButton).addContainerGap()).add(org.jdesktop.layout.GroupLayout.TRAILING, aboutDialogLayout.createSequentialGroup().add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 702, Short.MAX_VALUE)).addContainerGap()))));
        aboutDialogLayout.setVerticalGroup(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(aboutDialogLayout.createSequentialGroup().addContainerGap().add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(12, 12, 12).add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 480, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(aboutDialogCloseButton).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        findDialog.setTitle(resourceMap.getString("findDialog.title"));
        findDialog.setName("findDialog");
        findDialog.setResizable(false);
        findDialogFindTextLabel.setText(resourceMap.getString("findDialogFindTextLabel.text"));
        findDialogFindTextLabel.setName("findDialogFindTextLabel");
        findDialogTextField.setText(resourceMap.getString("findDialogTextField.text"));
        findDialogTextField.setName("findDialogTextField");
        findDialogFindButton.setText(resourceMap.getString("findDialogFindButton.text"));
        findDialogFindButton.setName("findDialogFindButton");
        findDialogFindButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findDialogFindButtonActionPerformed(evt);
            }
        });
        findDialogExitButton.setText(resourceMap.getString("findDialogExitButton.text"));
        findDialogExitButton.setName("findDialogExitButton");
        findDialogExitButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findDialogExitButtonActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout findDialogLayout = new org.jdesktop.layout.GroupLayout(findDialog.getContentPane());
        findDialog.getContentPane().setLayout(findDialogLayout);
        findDialogLayout.setHorizontalGroup(findDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(findDialogLayout.createSequentialGroup().addContainerGap().add(findDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(findDialogFindTextLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(findDialogLayout.createSequentialGroup().add(findDialogFindButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(findDialogExitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(findDialogTextField)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        findDialogLayout.setVerticalGroup(findDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(findDialogLayout.createSequentialGroup().addContainerGap().add(findDialogFindTextLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(findDialogTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(findDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(findDialogFindButton).add(findDialogExitButton)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        generalPreferencesDialog.setModal(true);
        generalPreferencesDialog.setName("generalPreferencesDialog");
        generalPreferencesDialog.setResizable(false);
        generalPreferencesDialogSpinnerImagesSize.setModel(new javax.swing.SpinnerNumberModel(100, 25, 500, 5));
        generalPreferencesDialogSpinnerImagesSize.setName("generalPreferencesDialogSpinnerImagesSize");
        generalPreferencesDialogImagesSizeLabel.setText(resourceMap.getString("generalPreferencesDialogImagesSizeLabel.text"));
        generalPreferencesDialogImagesSizeLabel.setName("generalPreferencesDialogImagesSizeLabel");
        generalPreferencesDialogMaxUndoLevelLabel.setText(resourceMap.getString("generalPreferencesDialogMaxUndoLevelLabel.text"));
        generalPreferencesDialogMaxUndoLevelLabel.setName("generalPreferencesDialogMaxUndoLevelLabel");
        generalPreferencesDialogSpinnerMaxUndoLevel.setModel(new javax.swing.SpinnerNumberModel(5, 1, 20, 1));
        generalPreferencesDialogSpinnerMaxUndoLevel.setName("generalPreferencesDialogSpinnerMaxUndoLevel");
        generalPreferencesDialogMaxLengthCompoundWordsLabel.setText(resourceMap.getString("generalPreferencesDialogMaxLengthCompoundWordsLabel.text"));
        generalPreferencesDialogMaxLengthCompoundWordsLabel.setName("generalPreferencesDialogMaxLengthCompoundWordsLabel");
        generalPreferencesDialogSpinnerMaxLengthCompoundWords.setModel(new javax.swing.SpinnerNumberModel(3, 0, 5, 1));
        generalPreferencesDialogSpinnerMaxLengthCompoundWords.setName("generalPreferencesDialogSpinnerMaxLengthCompoundWords");
        generalPreferencesDialogApplicationLanguageLabel.setText(resourceMap.getString("generalPreferencesDialogApplicationLanguageLabel.text"));
        generalPreferencesDialogApplicationLanguageLabel.setName("generalPreferencesDialogApplicationLanguageLabel");
        generalPreferencesDialogSpinnerApplicationLanguage.setModel(new javax.swing.SpinnerListModel(new String[] { "Español", "English", "Français", "Deutsch", "Català" }));
        generalPreferencesDialogSpinnerApplicationLanguage.setName("generalPreferencesDialogSpinnerApplicationLanguage");
        generalPreferencesDialogDocumentLanguageLabel.setText(resourceMap.getString("generalPreferencesDialogDocumentLanguageLabel.text"));
        generalPreferencesDialogDocumentLanguageLabel.setName("generalPreferencesDialogDocumentLanguageLabel");
        generalPreferencesDialogSpinnerDocumentLanguage.setModel(new javax.swing.SpinnerListModel(new String[] { "(todos)", "Español", "English", "Français", "Deutsch", "Català" }));
        generalPreferencesDialogSpinnerDocumentLanguage.setName("generalPreferencesDialogSpinnerDocumentLanguage");
        generalPreferencesDialogOKButton.setText(resourceMap.getString("generalPreferencesDialogOKButton.text"));
        generalPreferencesDialogOKButton.setName("generalPreferencesDialogOKButton");
        generalPreferencesDialogOKButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalPreferencesDialogOKButtonActionPerformed(evt);
            }
        });
        generalPreferencesDialogCancelButton.setText(resourceMap.getString("generalPreferencesDialogCancelButton.text"));
        generalPreferencesDialogCancelButton.setName("generalPreferencesDialogCancelButton");
        generalPreferencesDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalPreferencesDialogCancelButtonActionPerformed(evt);
            }
        });
        generalPreferencesDialogPictogramsPathLabel.setText(resourceMap.getString("generalPreferencesDialogPictogramsPathLabel.text"));
        generalPreferencesDialogPictogramsPathLabel.setName("generalPreferencesDialogPictogramsPathLabel");
        generalPreferencesDialogChoosePictogramsPathButton.setText(resourceMap.getString("generalPreferencesDialogChoosePictogramsPathButton.text"));
        generalPreferencesDialogChoosePictogramsPathButton.setName("generalPreferencesDialogChoosePictogramsPathButton");
        generalPreferencesDialogChoosePictogramsPathButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalPreferencesDialogChoosePictogramsPathButtonActionPerformed(evt);
            }
        });
        generalPreferencesDialogTextFontLabel.setText(resourceMap.getString("generalPreferencesDialogTextFontLabel.text"));
        generalPreferencesDialogTextFontLabel.setName("generalPreferencesDialogTextFontLabel");
        generalPreferencesDialogChooseTextFontButton.setText(resourceMap.getString("generalPreferencesDialogChooseTextFontButton.text"));
        generalPreferencesDialogChooseTextFontButton.setName("generalPreferencesDialogChooseTextFontButton");
        generalPreferencesDialogChooseTextFontButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalPreferencesDialogChooseTextFontButtonActionPerformed(evt);
            }
        });
        generalPreferencesDialogTextColorLabel.setText(resourceMap.getString("generalPreferencesDialogTextColorLabel.text"));
        generalPreferencesDialogTextColorLabel.setName("generalPreferencesDialogTextColorLabel");
        generalPreferencesDialogChooseTextColorButton.setText(resourceMap.getString("generalPreferencesDialogChooseTextColorButton.text"));
        generalPreferencesDialogChooseTextColorButton.setName("generalPreferencesDialogChooseTextColorButton");
        generalPreferencesDialogChooseTextColorButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalPreferencesDialogChooseTextColorButtonActionPerformed(evt);
            }
        });
        generalPreferencesDialogTextPlacementLabel.setText(resourceMap.getString("generalPreferencesDialogTextPlacementLabel.text"));
        generalPreferencesDialogTextPlacementLabel.setName("generalPreferencesDialogTextPlacementLabel");
        generalPreferencesDialogSpinnerTextPlacement.setModel(new javax.swing.SpinnerListModel(new String[] { "Encima del pictograma", "Debajo del pictograma" }));
        generalPreferencesDialogSpinnerTextPlacement.setName("generalPreferencesDialogSpinnerTextPlacement");
        org.jdesktop.layout.GroupLayout generalPreferencesDialogLayout = new org.jdesktop.layout.GroupLayout(generalPreferencesDialog.getContentPane());
        generalPreferencesDialog.getContentPane().setLayout(generalPreferencesDialogLayout);
        generalPreferencesDialogLayout.setHorizontalGroup(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(generalPreferencesDialogLayout.createSequentialGroup().addContainerGap().add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(generalPreferencesDialogLayout.createSequentialGroup().add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(generalPreferencesDialogImagesSizeLabel).add(generalPreferencesDialogMaxUndoLevelLabel).add(generalPreferencesDialogApplicationLanguageLabel).add(generalPreferencesDialogPictogramsPathLabel).add(generalPreferencesDialogDocumentLanguageLabel).add(generalPreferencesDialogMaxLengthCompoundWordsLabel)).add(18, 18, 18).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(generalPreferencesDialogSpinnerImagesSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE).add(generalPreferencesDialogChoosePictogramsPathButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE).add(generalPreferencesDialogSpinnerDocumentLanguage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE).add(generalPreferencesDialogSpinnerApplicationLanguage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE).add(generalPreferencesDialogSpinnerMaxLengthCompoundWords, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE).add(generalPreferencesDialogSpinnerMaxUndoLevel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))).add(generalPreferencesDialogTextFontLabel).add(generalPreferencesDialogTextColorLabel).add(generalPreferencesDialogLayout.createSequentialGroup().add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(generalPreferencesDialogOKButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(generalPreferencesDialogTextPlacementLabel)).add(18, 18, 18).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, generalPreferencesDialogChooseTextColorButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING, generalPreferencesDialogChooseTextFontButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING, generalPreferencesDialogSpinnerTextPlacement, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE).add(generalPreferencesDialogCancelButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)))).addContainerGap()));
        generalPreferencesDialogLayout.setVerticalGroup(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(generalPreferencesDialogLayout.createSequentialGroup().addContainerGap().add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogImagesSizeLabel).add(generalPreferencesDialogSpinnerImagesSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(5, 5, 5).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogMaxUndoLevelLabel).add(generalPreferencesDialogSpinnerMaxUndoLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogMaxLengthCompoundWordsLabel).add(generalPreferencesDialogSpinnerMaxLengthCompoundWords, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(9, 9, 9).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogApplicationLanguageLabel).add(generalPreferencesDialogSpinnerApplicationLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogDocumentLanguageLabel).add(generalPreferencesDialogSpinnerDocumentLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogPictogramsPathLabel).add(generalPreferencesDialogChoosePictogramsPathButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogTextFontLabel).add(generalPreferencesDialogChooseTextFontButton)).add(7, 7, 7).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogTextColorLabel).add(generalPreferencesDialogChooseTextColorButton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogTextPlacementLabel).add(generalPreferencesDialogSpinnerTextPlacement, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(34, 34, 34).add(generalPreferencesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(generalPreferencesDialogOKButton).add(generalPreferencesDialogCancelButton)).addContainerGap()));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setName("toolBar");
        toolBarButtonFileNew.setText(resourceMap.getString("toolBarButtonFileNew.text"));
        toolBarButtonFileNew.setFocusable(false);
        toolBarButtonFileNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonFileNew.setName("toolBarButtonFileNew");
        toolBarButtonFileNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonFileNew.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonFileNewActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonFileNew);
        toolBarButtonFileOpen.setFocusable(false);
        toolBarButtonFileOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonFileOpen.setName("toolBarButtonFileOpen");
        toolBarButtonFileOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonFileOpen.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonFileOpenActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonFileOpen);
        toolBarButtonFileSave.setFocusable(false);
        toolBarButtonFileSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonFileSave.setName("toolBarButtonFileSave");
        toolBarButtonFileSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonFileSave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonFileSaveActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonFileSave);
        jSeparator3.setName("jSeparator3");
        toolBar.add(jSeparator3);
        toolBarButtonEditUndo.setFocusable(false);
        toolBarButtonEditUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonEditUndo.setName("toolBarButtonEditUndo");
        toolBarButtonEditUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonEditUndo.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonEditUndoActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonEditUndo);
        toolBarButtonEditRedo.setFocusable(false);
        toolBarButtonEditRedo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonEditRedo.setName("toolBarButtonEditRedo");
        toolBarButtonEditRedo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonEditRedo.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonEditRedoActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonEditRedo);
        toolBarButtonEditCut.setFocusable(false);
        toolBarButtonEditCut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonEditCut.setName("toolBarButtonEditCut");
        toolBarButtonEditCut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonEditCut.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonEditCutActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonEditCut);
        toolBarButtonEditCopy.setFocusable(false);
        toolBarButtonEditCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonEditCopy.setName("toolBarButtonEditCopy");
        toolBarButtonEditCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonEditCopy.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonEditCopyActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonEditCopy);
        toolBarButtonEditPaste.setFocusable(false);
        toolBarButtonEditPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonEditPaste.setName("toolBarButtonEditPaste");
        toolBarButtonEditPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonEditPaste.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonEditPasteActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonEditPaste);
        jSeparator4.setName("jSeparator4");
        toolBar.add(jSeparator4);
        toolBarButtonPictogramsNextImage.setText(resourceMap.getString("toolBarButtonPictogramsNextImage.text"));
        toolBarButtonPictogramsNextImage.setFocusable(false);
        toolBarButtonPictogramsNextImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonPictogramsNextImage.setName("toolBarButtonPictogramsNextImage");
        toolBarButtonPictogramsNextImage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonPictogramsNextImage.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonPictogramsNextImageActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonPictogramsNextImage);
        toolBarButtonPictogramsCompoundSplitWord.setText(resourceMap.getString("toolBarButtonPictogramsCompoundSplitWord.text"));
        toolBarButtonPictogramsCompoundSplitWord.setFocusable(false);
        toolBarButtonPictogramsCompoundSplitWord.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonPictogramsCompoundSplitWord.setName("toolBarButtonPictogramsCompoundSplitWord");
        toolBarButtonPictogramsCompoundSplitWord.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonPictogramsCompoundSplitWord.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonPictogramsCompoundSplitWordActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonPictogramsCompoundSplitWord);
        toolBarButtonPictogramsChangeName.setText(resourceMap.getString("toolBarButtonPictogramsChangeName.text"));
        toolBarButtonPictogramsChangeName.setFocusable(false);
        toolBarButtonPictogramsChangeName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarButtonPictogramsChangeName.setName("toolBarButtonPictogramsChangeName");
        toolBarButtonPictogramsChangeName.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarButtonPictogramsChangeName.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarButtonPictogramsChangeNameActionPerformed(evt);
            }
        });
        toolBar.add(toolBarButtonPictogramsChangeName);
        documentLanguageDialog.setModal(true);
        documentLanguageDialog.setName("documentLanguageDialog");
        documentLanguageDialog.setResizable(false);
        documentLanguageDialogDocumentLanguageLabel.setText(resourceMap.getString("documentLanguageDialogDocumentLanguageLabel.text"));
        documentLanguageDialogDocumentLanguageLabel.setName("documentLanguageDialogDocumentLanguageLabel");
        documentLanguageDialogSpinnerDocumentLanguage.setModel(new javax.swing.SpinnerListModel(new String[] { "(todos)", "Español", "English", "Français", "Deutsch", "Català" }));
        documentLanguageDialogSpinnerDocumentLanguage.setName("documentLanguageDialogSpinnerDocumentLanguage");
        documentLanguageDialogOKButton.setText(resourceMap.getString("documentLanguageDialogOKButton.text"));
        documentLanguageDialogOKButton.setName("documentLanguageDialogOKButton");
        documentLanguageDialogOKButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentLanguageDialogOKButtonActionPerformed(evt);
            }
        });
        documentLanguageDialogCancelButton.setText(resourceMap.getString("documentLanguageDialogCancelButton.text"));
        documentLanguageDialogCancelButton.setName("documentLanguageDialogCancelButton");
        documentLanguageDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentLanguageDialogCancelButtonActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout documentLanguageDialogLayout = new org.jdesktop.layout.GroupLayout(documentLanguageDialog.getContentPane());
        documentLanguageDialog.getContentPane().setLayout(documentLanguageDialogLayout);
        documentLanguageDialogLayout.setHorizontalGroup(documentLanguageDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(documentLanguageDialogLayout.createSequentialGroup().addContainerGap().add(documentLanguageDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(documentLanguageDialogLayout.createSequentialGroup().add(documentLanguageDialogDocumentLanguageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(35, 35, 35).add(documentLanguageDialogSpinnerDocumentLanguage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)).add(org.jdesktop.layout.GroupLayout.TRAILING, documentLanguageDialogLayout.createSequentialGroup().add(documentLanguageDialogOKButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(documentLanguageDialogCancelButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))).addContainerGap()));
        documentLanguageDialogLayout.setVerticalGroup(documentLanguageDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(documentLanguageDialogLayout.createSequentialGroup().addContainerGap().add(documentLanguageDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(documentLanguageDialogSpinnerDocumentLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(documentLanguageDialogDocumentLanguageLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(documentLanguageDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(documentLanguageDialogCancelButton).add(documentLanguageDialogOKButton)).addContainerGap()));
        imagesSizeDialog.setModal(true);
        imagesSizeDialog.setName("imagesSizeDialog");
        imagesSizeDialog.setResizable(false);
        imagesSizeDialogImagesSizeLabel.setText(resourceMap.getString("imagesSizeDialogImagesSizeLabel.text"));
        imagesSizeDialogImagesSizeLabel.setName("imagesSizeDialogImagesSizeLabel");
        imagesSizeDialogSpinnerImagesSize.setModel(new javax.swing.SpinnerNumberModel(100, 25, 500, 5));
        imagesSizeDialogSpinnerImagesSize.setName("imagesSizeDialogSpinnerImagesSize");
        imagesSizeDialogCancelButton.setText(resourceMap.getString("imagesSizeDialogCancelButton.text"));
        imagesSizeDialogCancelButton.setName("imagesSizeDialogCancelButton");
        imagesSizeDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imagesSizeDialogCancelButtonActionPerformed(evt);
            }
        });
        imagesSizeDialogOKButton.setText(resourceMap.getString("imagesSizeDialogOKButton.text"));
        imagesSizeDialogOKButton.setName("imagesSizeDialogOKButton");
        imagesSizeDialogOKButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imagesSizeDialogOKButtonActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout imagesSizeDialogLayout = new org.jdesktop.layout.GroupLayout(imagesSizeDialog.getContentPane());
        imagesSizeDialog.getContentPane().setLayout(imagesSizeDialogLayout);
        imagesSizeDialogLayout.setHorizontalGroup(imagesSizeDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(imagesSizeDialogLayout.createSequentialGroup().addContainerGap().add(imagesSizeDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(imagesSizeDialogOKButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(imagesSizeDialogImagesSizeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(imagesSizeDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(imagesSizeDialogSpinnerImagesSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE).add(imagesSizeDialogCancelButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)).addContainerGap()));
        imagesSizeDialogLayout.setVerticalGroup(imagesSizeDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(imagesSizeDialogLayout.createSequentialGroup().addContainerGap().add(imagesSizeDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(imagesSizeDialogImagesSizeLabel).add(imagesSizeDialogSpinnerImagesSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(imagesSizeDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(imagesSizeDialogOKButton).add(imagesSizeDialogCancelButton)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        setComponent(mainPanel);
    }

    private void menuFileNewActionPerformed(java.awt.event.ActionEvent evt) {
        if (JOptionPane.showConfirmDialog(getFrame(), TLanguage.getString("FILE_MENU_NEW_WARNING_DISCARD"), TLanguage.getString("WARNING"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            TextUtils.newDocument();
        }
    }

    private void menuFileOpenActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.fileOpen();
    }

    private void menuFileSaveActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.fileSave();
    }

    private void menuFileSaveAsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.fileSaveAs();
    }

    private void menuFileExportActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.fileExport();
    }

    private void menuFileExitActionPerformed(java.awt.event.ActionEvent evt) {
        if (JOptionPane.showConfirmDialog(getFrame(), TLanguage.getString("FILE_MENU_EXIT_WARNING"), TLanguage.getString("WARNING"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
    }

    private void menuEditCutActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editCut();
    }

    private void menuEditCopyActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editCopy();
    }

    private void menuEditPasteActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editPaste(G.activeElementPosition + 1);
    }

    private void menuEditUndoActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editUndo();
    }

    private void menuEditRedoActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editRedo();
    }

    private void menuEditFindActionPerformed(java.awt.event.ActionEvent evt) {
        findDialog.pack();
        G.lastPositionFound = -1;
        findDialog.setModal(true);
        findDialog.setLocationRelativeTo(null);
        findDialog.setVisible(true);
    }

    private void findDialogFindButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String strToFind = findDialogTextField.getText();
        boolean somethingFound = false;
        if (strToFind.equals("")) {
            JOptionPane.showMessageDialog(null, TLanguage.getString("EDIT_MENU_FIND_UNABLE_EMPTY_STRING_SEARCH"), TLanguage.getString("WARNING"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!strToFind.equals(G.lastStringAsked)) {
            G.lastPositionFound = -1;
            G.lastStringAsked = strToFind;
        }
        String wholeText = TextUtils.ElementList2String(G.elementList);
        if (wholeText.indexOf(strToFind) == -1) return;
        searchLoop: for (int i = G.lastPositionFound + 1; i < G.elementList.size(); i++) {
            AWElement elem = G.elementList.get(i);
            if (elem.getType() == 0) {
                String wordText = elem.getTextField().getText();
                if (wordText.length() >= strToFind.length()) {
                    if (wordText.indexOf(strToFind) != -1) {
                        G.lastPositionFound = i;
                        somethingFound = true;
                        break searchLoop;
                    }
                } else {
                    if (strToFind.startsWith(wordText)) {
                        boolean stop = false;
                        int tmpPos = i;
                        String words = "";
                        ArrayList<AWElement> eL = new ArrayList<AWElement>();
                        eL.add(elem);
                        while (!stop) {
                            if (tmpPos < G.elementList.size()) {
                                eL.add(G.elementList.get(tmpPos));
                                tmpPos++;
                                words = TextUtils.ElementList2String(eL);
                                if (words.length() >= strToFind.length()) {
                                    if (words.indexOf(strToFind) != -1) {
                                        G.lastPositionFound = i;
                                        somethingFound = true;
                                        break searchLoop;
                                    } else stop = true;
                                }
                            } else stop = true;
                        }
                    }
                }
            }
        }
        findDialog.setVisible(false);
        if (somethingFound) {
            G.elementList.get(G.lastPositionFound).getTextField().requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(null, TLanguage.getString("EDIT_MENU_FIND_NOT_FOUND_TEXT"), TLanguage.getString("WARNING"), JOptionPane.INFORMATION_MESSAGE);
            G.lastPositionFound = -1;
        }
        findDialog.setVisible(true);
    }

    private void findDialogExitButtonActionPerformed(java.awt.event.ActionEvent evt) {
        findDialog.setVisible(false);
    }

    private void menuEditSelectAllActionPerformed(java.awt.event.ActionEvent evt) {
        G.indexSelectionFrom = 0;
        G.indexSelectionTo = G.elementList.size() - 1;
        for (int i = G.indexSelectionFrom; i <= G.indexSelectionTo; i++) {
            G.elementList.get(i).setBackground(Color.BLUE);
        }
        G.wereDrag = false;
        G.selectionState = 2;
    }

    private void menuTextFontActionPerformed(java.awt.event.ActionEvent evt) {
        JFontChooser fontChooser = new JFontChooser();
        if (fontChooser.showDialog(null) == JFontChooser.OK_OPTION) {
            Font font = fontChooser.getSelectedFont();
            G.font = font;
        }
        TextUtils.regenerateDocument();
    }

    private void menuTextColorActionPerformed(java.awt.event.ActionEvent evt) {
        Color tmpColor = JColorChooser.showDialog(null, "", Color.BLACK);
        if (tmpColor != null) G.color = tmpColor;
        TextUtils.regenerateDocument();
    }

    private void menuTextPlacementAbovePictogramActionPerformed(java.awt.event.ActionEvent evt) {
        G.textBelowPictogram = false;
        TextUtils.regenerateDocument();
    }

    private void menuTextPlacementBelowPictogramActionPerformed(java.awt.event.ActionEvent evt) {
        G.textBelowPictogram = true;
        TextUtils.regenerateDocument();
    }

    private void menuTextToUpperCaseActiveElementActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramToUpperCaseActiveElement();
    }

    private void menuTextToUpperCaseAllElementsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramToUpperCaseAllElements();
    }

    private void menuTextToLowerCaseActiveElementActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramToLowerCaseActiveElement();
    }

    private void menuTextToLowerCaseAllElementsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramToLowerCaseAllElements();
    }

    private void menuTextDocumentLanguageActionPerformed(java.awt.event.ActionEvent evt) {
        documentLanguageDialog.pack();
        documentLanguageDialog.setModal(true);
        documentLanguageDialog.setLocationRelativeTo(null);
        documentLanguageDialog.setVisible(true);
    }

    private void documentLanguageDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        documentLanguageDialog.setVisible(false);
    }

    private void documentLanguageDialogOKButtonActionPerformed(java.awt.event.ActionEvent evt) {
        G.documentLanguage = (String) documentLanguageDialogSpinnerDocumentLanguage.getValue();
        DBManagement.connectVerbsDB();
        DBManagement.createAraWordView(G.documentLanguage);
        TextUtils.regenerateDocument();
        documentLanguageDialog.setVisible(false);
    }

    private void menuPictogramsSizeActionPerformed(java.awt.event.ActionEvent evt) {
        imagesSizeDialog.pack();
        imagesSizeDialog.setModal(true);
        imagesSizeDialog.setLocationRelativeTo(null);
        imagesSizeDialog.setVisible(true);
    }

    private void imagesSizeDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        imagesSizeDialog.setVisible(false);
    }

    private void imagesSizeDialogOKButtonActionPerformed(java.awt.event.ActionEvent evt) {
        G.imagesSize = ((Integer) imagesSizeDialogSpinnerImagesSize.getValue()).intValue();
        ImageIcon image = new ImageIcon("resources/404.jpg");
        G.notFound = new ImageIcon(image.getImage().getScaledInstance(-1, G.imagesSize, 0));
        TextUtils.regenerateDocument();
        imagesSizeDialog.setVisible(false);
    }

    private void menuPictogramsNextImageActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramNextImage();
    }

    private void menuPictogramsCompoundSplitWordActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramCompoundSplit();
    }

    private void menuPictogramsChangeNameActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramChangeName();
    }

    private void menuPictogramsShowBorderAllElementsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramShowBorderAllElements();
    }

    private void menuPictogramsHideBorderAllElementsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramHideBorderAllElements();
    }

    private void menuPictogramsShowImageActiveElementActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramShowImageActiveElement();
    }

    private void menuPictogramsShowImageAllElementsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramShowImageAllElements();
    }

    private void menuPictogramsShowBorderActiveElementActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramShowBorderActiveElement();
    }

    private void menuPictogramsHideImageActiveElementActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramHideImageActiveElement();
    }

    private void menuPictogramsHideImageAllElementsActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramHideImageAllElements();
    }

    private void menuPictogramsHideBorderActiveElementActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramHideBorderActiveElement();
    }

    private void menuToolsResourceManagerActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            Runtime.getRuntime().exec("java -jar ." + File.separator + ".." + File.separator + "ResourceManager" + File.separator + "ResourceManager.jar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void menuToolsGeneralPreferencesActionPerformed(java.awt.event.ActionEvent evt) {
        generalPreferencesDialog.pack();
        generalPreferencesDialog.setLocationRelativeTo(null);
        G.tempDefaultFont = G.defaultFont;
        G.tempDefaultColor = G.defaultColor;
        G.tempPictogramsPath = G.pictogramsPath;
        generalPreferencesDialog.setVisible(true);
    }

    private void generalPreferencesDialogOKButtonActionPerformed(java.awt.event.ActionEvent evt) {
        G.defaultTextBelowPictogram = ((String) generalPreferencesDialogSpinnerTextPlacement.getValue()).equals(TLanguage.getString("SPINNER_TEXT_BELOW_PICTOGRAM"));
        G.defaultImagesSize = ((Integer) generalPreferencesDialogSpinnerImagesSize.getValue()).intValue();
        G.maxLengthCompoundWords = ((Integer) generalPreferencesDialogSpinnerMaxLengthCompoundWords.getValue()).intValue();
        G.maxUndoLevel = ((Integer) generalPreferencesDialogSpinnerMaxUndoLevel.getValue()).intValue();
        G.applicationLanguage = ((String) generalPreferencesDialogSpinnerApplicationLanguage.getValue());
        G.defaultDocumentLanguage = ((String) generalPreferencesDialogSpinnerDocumentLanguage.getValue());
        G.defaultFont = G.tempDefaultFont;
        G.defaultColor = G.tempDefaultColor;
        G.pictogramsPath = G.tempPictogramsPath;
        try {
            TSetup.save();
            TLanguage.initLanguage(G.applicationLanguage);
            setApplicationLanguage();
        } catch (Exception e) {
            System.out.println(e);
        }
        generalPreferencesDialog.setVisible(false);
    }

    private void generalPreferencesDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        generalPreferencesDialog.setVisible(false);
    }

    private void generalPreferencesDialogChooseTextFontButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFontChooser fontChooser = new JFontChooser();
        if (fontChooser.showDialog(null) == JFontChooser.OK_OPTION) {
            Font font = fontChooser.getSelectedFont();
            G.tempDefaultFont = font;
        }
    }

    private void generalPreferencesDialogChoosePictogramsPathButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            JFileChooser fc = new JFileChooser(".");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(G.textZone) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                G.tempPictogramsPath = RelativePath.getRelativePath(new File("."), file);
            }
        } catch (Exception exc) {
            System.out.println(exc);
        }
    }

    private void toolBarButtonFileNewActionPerformed(java.awt.event.ActionEvent evt) {
        if (JOptionPane.showConfirmDialog(getFrame(), TLanguage.getString("FILE_MENU_NEW_WARNING_DISCARD"), TLanguage.getString("WARNING"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            TextUtils.newDocument();
        }
    }

    private void toolBarButtonFileOpenActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.fileOpen();
    }

    private void toolBarButtonFileSaveActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.fileSave();
    }

    private void toolBarButtonEditUndoActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editUndo();
    }

    private void toolBarButtonEditRedoActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editRedo();
    }

    private void toolBarButtonEditCutActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editCut();
    }

    private void toolBarButtonEditCopyActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editCopy();
    }

    private void toolBarButtonEditPasteActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.editPaste(G.activeElementPosition + 1);
    }

    private void toolBarButtonPictogramsNextImageActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramNextImage();
    }

    private void toolBarButtonPictogramsCompoundSplitWordActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramCompoundSplit();
    }

    private void toolBarButtonPictogramsChangeNameActionPerformed(java.awt.event.ActionEvent evt) {
        MenuFunctions.pictogramChangeName();
    }

    private void generalPreferencesDialogChooseTextColorButtonActionPerformed(java.awt.event.ActionEvent evt) {
        G.tempDefaultColor = JColorChooser.showDialog(null, "", Color.BLACK);
    }

    private void menuHelpShowHelpActionPerformed(java.awt.event.ActionEvent evt) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(new File("resources/ayuda.txt"));
            } catch (Exception exc) {
                System.out.println(exc);
            }
        }
    }

    private void menuHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }

    private void aboutDialogCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        aboutDialog.setVisible(false);
    }

    private javax.swing.JDialog aboutDialog;

    private javax.swing.JButton aboutDialogCloseButton;

    private javax.swing.JLabel aboutDialogImage;

    private javax.swing.JTextArea aboutDialogTextArea;

    private javax.swing.JDialog documentLanguageDialog;

    private javax.swing.JButton documentLanguageDialogCancelButton;

    private javax.swing.JLabel documentLanguageDialogDocumentLanguageLabel;

    private javax.swing.JButton documentLanguageDialogOKButton;

    private javax.swing.JSpinner documentLanguageDialogSpinnerDocumentLanguage;

    private javax.swing.JDialog findDialog;

    private javax.swing.JButton findDialogExitButton;

    private javax.swing.JButton findDialogFindButton;

    private javax.swing.JLabel findDialogFindTextLabel;

    private javax.swing.JTextField findDialogTextField;

    private javax.swing.JDialog generalPreferencesDialog;

    private javax.swing.JLabel generalPreferencesDialogApplicationLanguageLabel;

    private javax.swing.JButton generalPreferencesDialogCancelButton;

    private javax.swing.JButton generalPreferencesDialogChoosePictogramsPathButton;

    private javax.swing.JButton generalPreferencesDialogChooseTextColorButton;

    private javax.swing.JButton generalPreferencesDialogChooseTextFontButton;

    private javax.swing.JLabel generalPreferencesDialogDocumentLanguageLabel;

    private javax.swing.JLabel generalPreferencesDialogImagesSizeLabel;

    private javax.swing.JLabel generalPreferencesDialogMaxLengthCompoundWordsLabel;

    private javax.swing.JLabel generalPreferencesDialogMaxUndoLevelLabel;

    private javax.swing.JButton generalPreferencesDialogOKButton;

    private javax.swing.JLabel generalPreferencesDialogPictogramsPathLabel;

    private javax.swing.JSpinner generalPreferencesDialogSpinnerApplicationLanguage;

    private javax.swing.JSpinner generalPreferencesDialogSpinnerDocumentLanguage;

    private javax.swing.JSpinner generalPreferencesDialogSpinnerImagesSize;

    private javax.swing.JSpinner generalPreferencesDialogSpinnerMaxLengthCompoundWords;

    private javax.swing.JSpinner generalPreferencesDialogSpinnerMaxUndoLevel;

    private javax.swing.JSpinner generalPreferencesDialogSpinnerTextPlacement;

    private javax.swing.JLabel generalPreferencesDialogTextColorLabel;

    private javax.swing.JLabel generalPreferencesDialogTextFontLabel;

    private javax.swing.JLabel generalPreferencesDialogTextPlacementLabel;

    private javax.swing.JDialog imagesSizeDialog;

    private javax.swing.JButton imagesSizeDialogCancelButton;

    private javax.swing.JLabel imagesSizeDialogImagesSizeLabel;

    private javax.swing.JButton imagesSizeDialogOKButton;

    private javax.swing.JSpinner imagesSizeDialogSpinnerImagesSize;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JPopupMenu.Separator jSeparator1;

    private javax.swing.JPopupMenu.Separator jSeparator2;

    private javax.swing.JToolBar.Separator jSeparator3;

    private javax.swing.JToolBar.Separator jSeparator4;

    private javax.swing.JPanel mainPanel;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JMenu menuEdit;

    private javax.swing.JMenuItem menuEditCopy;

    private javax.swing.JMenuItem menuEditCut;

    private javax.swing.JMenuItem menuEditFind;

    private javax.swing.JMenuItem menuEditPaste;

    private javax.swing.JMenuItem menuEditRedo;

    private javax.swing.JMenuItem menuEditSelectAll;

    private javax.swing.JMenuItem menuEditUndo;

    private javax.swing.JMenu menuFile;

    private javax.swing.JMenuItem menuFileExit;

    private javax.swing.JMenuItem menuFileExport;

    private javax.swing.JMenuItem menuFileNew;

    private javax.swing.JMenuItem menuFileOpen;

    private javax.swing.JMenuItem menuFileSave;

    private javax.swing.JMenuItem menuFileSaveAs;

    private javax.swing.JMenu menuHelp;

    private javax.swing.JMenuItem menuHelpAbout;

    private javax.swing.JMenuItem menuHelpShowHelp;

    private javax.swing.JMenu menuPictograms;

    private javax.swing.JMenuItem menuPictogramsChangeName;

    private javax.swing.JMenuItem menuPictogramsCompoundSplitWord;

    private javax.swing.JMenu menuPictogramsHide;

    private javax.swing.JMenu menuPictogramsHideBorder;

    private javax.swing.JMenuItem menuPictogramsHideBorderActiveElement;

    private javax.swing.JMenuItem menuPictogramsHideBorderAllElements;

    private javax.swing.JMenu menuPictogramsHideImage;

    private javax.swing.JMenuItem menuPictogramsHideImageActiveElement;

    private javax.swing.JMenuItem menuPictogramsHideImageAllElements;

    private javax.swing.JMenuItem menuPictogramsNextImage;

    private javax.swing.JMenu menuPictogramsShow;

    private javax.swing.JMenu menuPictogramsShowBorder;

    private javax.swing.JMenuItem menuPictogramsShowBorderActiveElement;

    private javax.swing.JMenuItem menuPictogramsShowBorderAllElements;

    private javax.swing.JMenu menuPictogramsShowImage;

    private javax.swing.JMenuItem menuPictogramsShowImageActiveElement;

    private javax.swing.JMenuItem menuPictogramsShowImageAllElements;

    private javax.swing.JMenuItem menuPictogramsSize;

    private javax.swing.JMenu menuText;

    private javax.swing.JMenuItem menuTextColor;

    private javax.swing.JMenuItem menuTextDocumentLanguage;

    private javax.swing.JMenuItem menuTextFont;

    private javax.swing.JMenu menuTextPlacement;

    private javax.swing.JMenuItem menuTextPlacementAbovePictogram;

    private javax.swing.JMenuItem menuTextPlacementBelowPictogram;

    private javax.swing.JMenu menuTextToLowerCase;

    private javax.swing.JMenuItem menuTextToLowerCaseActiveElement;

    private javax.swing.JMenuItem menuTextToLowerCaseAllElements;

    private javax.swing.JMenu menuTextToUpperCase;

    private javax.swing.JMenuItem menuTextToUpperCaseActiveElement;

    private javax.swing.JMenuItem menuTextToUpperCaseAllElements;

    private javax.swing.JMenu menuTools;

    private javax.swing.JMenuItem menuToolsGeneralPreferences;

    private javax.swing.JMenuItem menuToolsResourceManager;

    private javax.swing.JScrollPane scrollTextZone;

    private javax.swing.JTextPane textZone;

    private javax.swing.JToolBar toolBar;

    private javax.swing.JButton toolBarButtonEditCopy;

    private javax.swing.JButton toolBarButtonEditCut;

    private javax.swing.JButton toolBarButtonEditPaste;

    private javax.swing.JButton toolBarButtonEditRedo;

    private javax.swing.JButton toolBarButtonEditUndo;

    private javax.swing.JButton toolBarButtonFileNew;

    private javax.swing.JButton toolBarButtonFileOpen;

    private javax.swing.JButton toolBarButtonFileSave;

    private javax.swing.JButton toolBarButtonPictogramsChangeName;

    private javax.swing.JButton toolBarButtonPictogramsCompoundSplitWord;

    private javax.swing.JButton toolBarButtonPictogramsNextImage;
}
