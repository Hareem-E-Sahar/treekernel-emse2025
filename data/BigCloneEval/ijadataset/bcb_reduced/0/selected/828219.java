package freestyleLearning.learningUnitViewAPI.elementsStructurePanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import freestyleLearning.homeCore.learningUnitsManager.FSLLearningUnitLinkChecker;
import freestyleLearning.homeCore.learningUnitsManager.FSLLearningUnitsManager;
import freestyleLearning.homeCore.learningUnitsManager.data.xmlBindingSubclasses.FSLLearningUnitDescriptor;
import freestyleLearning.homeCore.learningUnitsManager.data.xmlBindingSubclasses.FSLLearningUnitsDescriptor;
import freestyleLearning.learningUnitViewAPI.FSLLearningUnitViewElement;
import freestyleLearning.learningUnitViewAPI.FSLLearningUnitViewElementsManager;
import freestyleLearning.learningUnitViewAPI.FSLLearningUnitViewManager;
import freestyleLearning.learningUnitViewAPI.FSLLearningUnitViewOpenUSSWebServiceClient;
import freestyleLearning.learningUnitViewAPI.elementsStructurePanel.dialogs.FSLLearningUnitViewLinkCheckerDialog;
import freestyleLearning.learningUnitViewAPI.elementsStructurePanel.dialogs.FSLLearningUnitViewModifyElementDialog;
import freestyleLearning.learningUnitViewAPI.elementsStructurePanel.dialogs.FSLLearningUnitViewNewElementDialog;
import freestyleLearning.learningUnitViewAPI.elementsStructurePanel.dialogs.FSLLearningUnitViewSortElementsDialog;
import freestyleLearning.learningUnitViewAPI.events.learningUnitEvent.FSLLearningUnitAdapter;
import freestyleLearning.learningUnitViewAPI.events.learningUnitEvent.FSLLearningUnitEvent;
import freestyleLearning.learningUnitViewAPI.events.learningUnitEvent.FSLLearningUnitEventGenerator;
import freestyleLearning.learningUnitViewAPI.events.learningUnitViewEvent.FSLLearningUnitViewEvent;
import freestyleLearning.learningUnitViewAPI.events.learningUnitViewEvent.FSLLearningUnitViewVetoableAdapter;
import freestyleLearning.learningUnitViewAPI.events.learningUnitViewEvent.FSLLearningUnitViewVetoableEvent;
import freestyleLearning.learningUnitViewAPI.FSLLearningUnitViewSynchronizeManager;
import freestyleLearningGroup.independent.gui.FLGColumnLayout;
import freestyleLearningGroup.independent.gui.FLGEditToolBarButton;
import freestyleLearningGroup.independent.gui.FLGEffectPanel;
import freestyleLearningGroup.independent.gui.FLGImageProgressDialog;
import freestyleLearningGroup.independent.gui.FLGImageUtility;
import freestyleLearningGroup.independent.gui.FLGLeftToRightLayout;
import freestyleLearningGroup.independent.gui.FLGOptionPane;
import freestyleLearningGroup.independent.gui.FLGUIUtilities;
import freestyleLearningGroup.independent.util.FLGInternationalization;

/**
 * FSLLearningUnitViewElementsStructureEditToolBar.
 * Manager Class for creating LearningUnitViewManager-
 * StrucutreTree-EditToolBar to insert, modify, remove etc.
 * Learning Unit View Structure Tree Elements.
 * @author Freestyle Learning Group
 */
public class FSLLearningUnitViewElementsStructureEditToolBar extends FLGEffectPanel {

    public static final int DEFAULT_EDIT_TOOLBAR = 0;

    public static final int DEFAULT_EDIT_TOOLBAR_SORTABLE = 1;

    public static final int DEFAULT_EDIT_TOOLBAR_SORTABLE_EXPORT = 2;

    public static final int REMOVE_ONLY_EDIT_TOOLBAR = 3;

    private int toolBarType;

    private FLGEditToolBarButton newElementButton;

    private FLGEditToolBarButton removeElementButton;

    private FLGEditToolBarButton modifyElementButton;

    private FLGEditToolBarButton sortByNameButton;

    private FLGEditToolBarButton viewImportButton;

    private FLGEditToolBarButton viewExportButton;

    private FSLLearningUnitViewNewElementDialog newElementDialog;

    private FSLLearningUnitViewModifyElementDialog modifyElementDialog;

    private FSLLearningUnitViewSortElementsDialog sortElementsDialog;

    private FSLLearningUnitViewElementsManager learningUnitViewElementsManager;

    private FSLLearningUnitViewManager learningUnitViewManager;

    private FSLLearningUnitEventGenerator learningUnitEventGenerator;

    private String selectedLearningUnitViewElementId;

    private String activeLearningUnitViewElementId;

    private String secondaryActiveLearningUnitViewElementId;

    private FLGInternationalization internationalization;

    private FSLAbstractLearningUnitViewElementsStructurePanel learningUnitViewElementsStructurePanel;

    private FLGImageProgressDialog exportProgressDialog;

    private FLGImageProgressDialog importProgressDialog;

    /**
     * Inits FSLLearningUnitViewElementsStructureEditToolBar.
     * @param <code>FSLLearningUnitViewManager</code> learningUnitViewManager
     * @param <code>FSLLearningUnitEventGenerator</code> learningUnitEventGenerator
     * @param <code>FSLAbstractLearningUnitViewElementsStructurePanel</code> learningUnitViewElementsStructurePanel
     * @param <code>boolean</code> editMode
     * @param <code>int</code> toolBarType
     */
    public void init(FSLLearningUnitViewManager learningUnitViewManager, FSLLearningUnitEventGenerator learningUnitEventGenerator, FSLAbstractLearningUnitViewElementsStructurePanel learningUnitViewElementsStructurePanel, boolean editMode, int toolBarType) {
        internationalization = new FLGInternationalization("freestyleLearning.learningUnitViewAPI.elementsStructurePanel.internationalization", getClass().getClassLoader());
        this.learningUnitViewManager = learningUnitViewManager;
        this.learningUnitEventGenerator = learningUnitEventGenerator;
        this.learningUnitViewElementsStructurePanel = learningUnitViewElementsStructurePanel;
        this.toolBarType = toolBarType;
        learningUnitEventGenerator.addLearningUnitListener(new FSLLearningUnitElementsStructureEditToolBar_LearningUnitAdapter());
        learningUnitViewManager.addLearningUnitViewListener(new FSLLearningUnitViewElementsStructureEditToolBar_LearningUnitViewAdapter());
        buildIndependentUI();
        setVisible(editMode);
    }

    /**
     * Returns true if user decided import folder.
     * @return <code>boolean</code> folderImportIsSelected
     */
    public boolean folderImportIsSelected() {
        if (newElementDialog != null) {
            return newElementDialog.folderImportIsSelected();
        } else {
            return false;
        }
    }

    /**
     * Resets folder import.
     */
    public void resetFolderImport() {
        newElementDialog.deselectFolderImport();
    }

    /**
     * Builds independent UI.
     * @param <code>FSLLearningUnitViewElementsManager</code> learningUnitViewElementsManager
     */
    public void setLearningUnitViewElementsManager(FSLLearningUnitViewElementsManager learningUnitViewElementsManager) {
        this.learningUnitViewElementsManager = learningUnitViewElementsManager;
        selectedLearningUnitViewElementId = null;
    }

    /**
     * @param <code>String</code> element title in new element dialog
     */
    public void setLearningUnitViewElementTitle(String title) {
        if (newElementDialog.getElementTitle() == null) {
            newElementDialog.setElementTitle(title);
        } else if (newElementDialog.getElementTitle().equals("")) {
            newElementDialog.setElementTitle(title);
        }
    }

    /**
     * @return <code>String</code> element title from new element dialog
     */
    public String getLearningUnitViewElementTitle() {
        return newElementDialog.getElementTitle();
    }

    /**
     * Builds dependent UI.
     */
    public void buildIndependentUI() {
        setEffect("FSLMainFrameColor3", true);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        setLayout(new FLGLeftToRightLayout(5));
        newElementButton = new FLGEditToolBarButton(loadImage("editToolBarButtonNewElement.gif"));
        newElementButton.setToolTipText(internationalization.getString("button.newElement.toolTipText"));
        removeElementButton = new FLGEditToolBarButton(loadImage("editToolBarButtonRemoveElement.gif"));
        removeElementButton.setToolTipText(internationalization.getString("button.removeElement.toolTipText"));
        modifyElementButton = new FLGEditToolBarButton(loadImage("editToolBarButtonModifyElement.gif"));
        modifyElementButton.setToolTipText(internationalization.getString("button.modifyElement.toolTipText"));
        viewImportButton = new FLGEditToolBarButton(loadImage("viewImportButton.gif"));
        viewImportButton.setToolTipText(internationalization.getString("button.viewImport.toolTipText"));
        viewExportButton = new FLGEditToolBarButton(loadImage("viewExportButton.gif"));
        viewExportButton.setToolTipText(internationalization.getString("button.viewExport.toolTipText"));
        sortByNameButton = new FLGEditToolBarButton(loadImage("editToolBarButtonSortByName.gif"));
        sortByNameButton.setToolTipText(internationalization.getString("button.sortByName.toolTipText"));
        removeAll();
        switch(toolBarType) {
            case REMOVE_ONLY_EDIT_TOOLBAR:
                {
                    add(removeElementButton);
                    add(viewImportButton);
                    add(viewExportButton);
                    break;
                }
            case DEFAULT_EDIT_TOOLBAR_SORTABLE:
                {
                    add(newElementButton);
                    add(removeElementButton);
                    add(modifyElementButton);
                    add(sortByNameButton);
                    add(viewImportButton);
                    add(viewExportButton);
                    break;
                }
            default:
                {
                    add(newElementButton);
                    add(removeElementButton);
                    add(modifyElementButton);
                    add(viewImportButton);
                    add(viewExportButton);
                }
        }
        newElementButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showNewElementDialog();
            }
        });
        modifyElementButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showModifyElementDialog();
            }
        });
        removeElementButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showRemoveElementDialog();
            }
        });
        sortByNameButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showSortElementsDialog();
            }
        });
        viewImportButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showViewImportDialog();
            }
        });
        viewExportButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showViewExportDialog();
            }
        });
        modifyElementDialog = new FSLLearningUnitViewModifyElementDialog();
        sortElementsDialog = new FSLLearningUnitViewSortElementsDialog();
    }

    /**
     * Builds dependent UI.
     */
    public void buildDependentUI() {
        modifyElementButton.setEnabled(selectedLearningUnitViewElementId != null);
        sortByNameButton.setEnabled(learningUnitViewElementsManager != null && learningUnitViewElementsManager.isOriginalElementsOnly() && selectedLearningUnitViewElementId != null);
        if (learningUnitViewElementsManager != null) {
            viewExportButton.setEnabled(learningUnitViewElementsManager.isOriginalElementsOnly());
            removeElementButton.setEnabled(selectedLearningUnitViewElementId != null && (learningUnitViewElementsManager.isOriginalElementsOnly() || learningUnitViewElementsManager.getLearningUnitViewUserElement(selectedLearningUnitViewElementId) != null));
        }
    }

    private void showViewImportDialog() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new FLGColumnLayout());
        JRadioButton localViewImport_radioButton = new JRadioButton(internationalization.getString("dialog.viewImport.radioButton.localViewImport"));
        localViewImport_radioButton.setSelected(true);
        JRadioButton serverViewImport_radioButton = new JRadioButton(internationalization.getString("dialog.viewImport.radioButton.serverViewImport"));
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(localViewImport_radioButton);
        buttonGroup.add(serverViewImport_radioButton);
        dialogPanel.add(localViewImport_radioButton, FLGColumnLayout.LEFTEND);
        dialogPanel.add(serverViewImport_radioButton, FLGColumnLayout.LEFTEND);
        int returnValue = FLGOptionPane.showConfirmDialog(dialogPanel, internationalization.getString("dialog.viewImport.title"), FLGOptionPane.OK_CANCEL_OPTION, FLGOptionPane.PLAIN_MESSAGE);
        if (returnValue == FLGOptionPane.OK_OPTION) {
            FSLLearningUnitsManager learningUnitsManager = (FSLLearningUnitsManager) learningUnitEventGenerator;
            if (localViewImport_radioButton.isSelected()) {
                JFileChooser fileDialog = new JFileChooser();
                fileDialog.setDialogTitle(internationalization.getString("fileDialog.viewImport.title"));
                java.awt.Dimension screenDim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                fileDialog.setLocation((int) (screenDim.getWidth() - fileDialog.getWidth()) / 2, (int) (screenDim.getHeight() - fileDialog.getHeight()) / 2);
                String[] fileExtensions = { ".fslv" };
                fileDialog.setFileFilter(new FLGUIUtilities.FLGFileFilter(fileExtensions, ".fslv"));
                if (fileDialog.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileDialog.getSelectedFile();
                    FSLLearningUnitViewSynchronizeManager manager = new FSLLearningUnitViewSynchronizeManager(learningUnitsManager, learningUnitViewManager, learningUnitViewElementsManager, null);
                    if (manager.showSynchronizeManagerDialog(selectedFile.getAbsolutePath(), true, true)) {
                        if (manager.getImportStructure()) {
                            returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.viewImport.viewOverwriteMessage.message"), internationalization.getString("dialog.viewImport.viewOverwriteMessage.title"), FLGOptionPane.OK_CANCEL_OPTION, FLGOptionPane.QUESTION_MESSAGE);
                            if (returnValue == FLGOptionPane.OK_OPTION) {
                                FSLLearningUnitViewLinkCheckerDialog linkCheckerDialog = new FSLLearningUnitViewLinkCheckerDialog();
                                if (linkCheckerDialog.showDialog() == FLGOptionPane.OK_OPTION) {
                                    importLearningUnitView(false, selectedFile, linkCheckerDialog.checkLinkInOtherUnits(), linkCheckerDialog.checkLinkInOtherViews(), linkCheckerDialog.checkLinksOnlyOnExistingElements());
                                }
                            }
                        }
                    }
                }
            } else {
                FSLLearningUnitsDescriptor learningUnitsDescriptor = learningUnitsManager.getLearningUnitsDescriptor();
                FSLLearningUnitDescriptor learningUnitDescriptor = learningUnitsDescriptor.getDescriptorById(learningUnitsManager.getActiveLearningUnitId());
                String serverName = learningUnitDescriptor.getOpenUssServerName();
                if (serverName != null) {
                    if (!serverName.equals("")) {
                        if (learningUnitsManager.openUSSServerConnectionIsEstablished()) {
                            FSLLearningUnitViewOpenUSSWebServiceClient webServiceClient = new FSLLearningUnitViewOpenUSSWebServiceClient(learningUnitViewManager, learningUnitViewElementsManager, serverName, learningUnitsManager.getOpenussUserName(), learningUnitsManager.getOpenussPassword());
                            FSLLearningUnitViewSynchronizeManager synchronizeManager = new FSLLearningUnitViewSynchronizeManager(learningUnitsManager, learningUnitViewManager, learningUnitViewElementsManager, webServiceClient);
                            if (synchronizeManager.showSynchronizeManagerDialog(null, true, false)) {
                                if (synchronizeManager.getImportStructure()) {
                                    returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.viewImport.viewOverwriteMessage.message"), internationalization.getString("dialog.viewImport.viewOverwriteMessage.title"), FLGOptionPane.OK_CANCEL_OPTION, FLGOptionPane.QUESTION_MESSAGE);
                                    if (returnValue == FLGOptionPane.OK_OPTION) {
                                        File fslvFile = webServiceClient.downloadLearningUnitView(learningUnitDescriptor.getEnrollmentId(), learningUnitsManager.getActiveLearningUnitId(), learningUnitViewManager.getLearningUnitViewOriginalDataDirectory().getName());
                                        FSLLearningUnitViewLinkCheckerDialog linkCheckerDialog = new FSLLearningUnitViewLinkCheckerDialog();
                                        if (linkCheckerDialog.showDialog() == FLGOptionPane.OK_OPTION) {
                                            importLearningUnitView(true, fslvFile, linkCheckerDialog.checkLinkInOtherUnits(), linkCheckerDialog.checkLinkInOtherViews(), linkCheckerDialog.checkLinksOnlyOnExistingElements());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.viewImport.noSeverConfiguration.message"), internationalization.getString("dialog.viewImport.noSeverCOnfiguration.title"), FLGOptionPane.OK_OPTION, FLGOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.viewImport.noSeverConfiguration.message"), internationalization.getString("dialog.viewImport.noSeverCOnfiguration.title"), FLGOptionPane.OK_OPTION, FLGOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showViewExportDialog() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new FLGColumnLayout());
        JRadioButton localViewExport_radioButton = new JRadioButton(internationalization.getString("dialog.viewExport.radioButton.localViewExport"));
        localViewExport_radioButton.setSelected(true);
        JRadioButton wwwViewExport_radioButton = new JRadioButton(internationalization.getString("dialog.viewExport.radioButton.wwwViewExport"));
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(localViewExport_radioButton);
        buttonGroup.add(wwwViewExport_radioButton);
        dialogPanel.add(localViewExport_radioButton, FLGColumnLayout.LEFTEND);
        dialogPanel.add(wwwViewExport_radioButton, FLGColumnLayout.LEFTEND);
        int returnValue = FLGOptionPane.showConfirmDialog(dialogPanel, internationalization.getString("dialog.viewExport.title"), FLGOptionPane.OK_CANCEL_OPTION, FLGOptionPane.PLAIN_MESSAGE);
        if (returnValue == FLGOptionPane.OK_OPTION) {
            if (localViewExport_radioButton.isSelected()) {
                JFileChooser fileDialog = new JFileChooser();
                fileDialog.setDialogTitle(internationalization.getString("fileDialog.viewExport.title"));
                java.awt.Dimension screenDim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                fileDialog.setLocation((int) (screenDim.getWidth() - fileDialog.getWidth()) / 2, (int) (screenDim.getHeight() - fileDialog.getHeight()) / 2);
                String[] fileExtensions = { ".fslv" };
                fileDialog.setFileFilter(new FLGUIUtilities.FLGFileFilter(fileExtensions, ".fslv"));
                if (fileDialog.showSaveDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
                    exportLearningUnitView(fileDialog.getSelectedFile());
                }
            }
            if (wwwViewExport_radioButton.isSelected()) {
                FSLLearningUnitsManager learningUnitsManager = (FSLLearningUnitsManager) learningUnitEventGenerator;
                FSLLearningUnitsDescriptor learningUnitsDescriptor = learningUnitsManager.getLearningUnitsDescriptor();
                FSLLearningUnitDescriptor learningUnitDescriptor = learningUnitsDescriptor.getDescriptorById(learningUnitsManager.getActiveLearningUnitId());
                String serverName = learningUnitDescriptor.getOpenUssServerName();
                if (serverName != null) {
                    if (!serverName.equals("")) {
                        if (learningUnitsManager.openUSSServerConnectionIsEstablished()) {
                            FSLLearningUnitViewOpenUSSWebServiceClient webServiceClient = new FSLLearningUnitViewOpenUSSWebServiceClient(learningUnitViewManager, learningUnitViewElementsManager, serverName, learningUnitsManager.getOpenussUserName(), learningUnitsManager.getOpenussPassword());
                            FSLLearningUnitViewSynchronizeManager synchronizeManager = new FSLLearningUnitViewSynchronizeManager(learningUnitsManager, learningUnitViewManager, learningUnitViewElementsManager, webServiceClient);
                            if (synchronizeManager.showSynchronizeManagerDialog(null, false, false)) {
                                File tmpExportFile = new File(learningUnitsManager.getLearningUnitsDirectory() + System.getProperty("file.separator") + learningUnitViewManager.getLearningUnitViewOriginalDataDirectory().getName() + ".fslv");
                                webServiceClient.uploadLearningUnitVew(learningUnitDescriptor.getEnrollmentId(), learningUnitDescriptor.getId(), learningUnitViewManager.getLearningUnitViewOriginalDataDirectory().getName(), tmpExportFile);
                            }
                        }
                    } else {
                        returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.viewImport.noSeverConfiguration.message"), internationalization.getString("dialog.viewImport.noSeverCOnfiguration.title"), FLGOptionPane.OK_OPTION, FLGOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.viewImport.noSeverConfiguration.message"), internationalization.getString("dialog.viewImport.noSeverCOnfiguration.title"), FLGOptionPane.OK_OPTION, FLGOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void importLearningUnitView(boolean serverImport, File selectedFile, boolean linksInOtherUnits, boolean linksInOtherViews, boolean linksOnExistingElementsInOtherViews) {
        try {
            final boolean serverImport_ = serverImport;
            final boolean linksInOtherUnits_ = linksInOtherUnits;
            final boolean linksInOtherViews_ = linksInOtherViews;
            final boolean linksOnExistingElementsInOtherViews_ = linksOnExistingElementsInOtherViews;
            final File inputFile = selectedFile;
            if (inputFile != null) {
                new Thread() {

                    public void run() {
                        try {
                            ZipFile zipFile = new ZipFile(inputFile);
                            FileInputStream fileInputStr = new FileInputStream(inputFile);
                            ZipInputStream zipInputStream = new ZipInputStream(fileInputStr);
                            ZipEntry entry = zipInputStream.getNextEntry();
                            int step = 1;
                            int maxSteps = zipFile.size();
                            if (entry != null) {
                                importProgressDialog = new FLGImageProgressDialog(null, 0, maxSteps, 0, getClass().getClassLoader().getResource("freestyleLearning/homeCore/images/fsl.gif"), (Color) UIManager.get("FSLColorBlue"), (Color) UIManager.get("FSLColorRed"), internationalization.getString("learningUnitViewImport.rogressbarText"));
                                importProgressDialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                importProgressDialog.setBarValue(step);
                            }
                            while (entry != null) {
                                if (!learningUnitViewElementsManager.getLearningUnitViewOriginalDataDirectory().exists()) {
                                    learningUnitViewElementsManager.getLearningUnitViewOriginalDataDirectory().mkdir();
                                }
                                FileOutputStream out = new FileOutputStream(learningUnitViewElementsManager.getLearningUnitViewOriginalDataDirectory().getAbsoluteFile() + System.getProperty("file.separator") + entry.getName());
                                byte[] buf = new byte[4096];
                                int len;
                                while ((len = zipInputStream.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                                }
                                out.close();
                                zipInputStream.closeEntry();
                                entry = zipInputStream.getNextEntry();
                                importProgressDialog.setBarValue(step++);
                            }
                            zipFile.close();
                            fileInputStr.close();
                            zipInputStream.close();
                            importProgressDialog.setBarValue(maxSteps);
                            importProgressDialog.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            importProgressDialog.dispose();
                            if (serverImport_) {
                                inputFile.delete();
                            }
                            FSLLearningUnitViewEvent event = FSLLearningUnitViewEvent.createViewImportEvent(learningUnitViewManager.getLearningUnitViewManagerId());
                            learningUnitViewManager.fireLearningUnitViewEvent(event);
                            FSLLearningUnitsManager learningUnitsManager = (FSLLearningUnitsManager) learningUnitEventGenerator;
                            new FSLLearningUnitLinkChecker(learningUnitsManager, null).checkLinksForLearningUnitView(learningUnitViewManager, linksInOtherUnits_, linksInOtherViews_, linksOnExistingElementsInOtherViews_);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportLearningUnitView(File selectedFile) {
        if (learningUnitViewElementsManager.isOriginalElementsOnly()) {
            String[] elementIds = learningUnitViewElementsManager.getAllLearningUnitViewElementIds();
            for (int i = 0; i < elementIds.length; i++) {
                FSLLearningUnitViewElement element = learningUnitViewElementsManager.getLearningUnitViewElement(elementIds[i], false);
                if (element.getLastModificationDate() == null) {
                    element.setLastModificationDate(String.valueOf(new Date().getTime()));
                    element.setModified(true);
                }
            }
            learningUnitViewElementsManager.setModified(true);
            learningUnitViewManager.saveLearningUnitViewData();
            if (selectedFile != null) {
                try {
                    File outputFile = selectedFile;
                    String fileName = outputFile.getName();
                    StringBuffer extension = new StringBuffer();
                    if (fileName.length() >= 5) {
                        for (int i = 5; i > 0; i--) {
                            extension.append(fileName.charAt(fileName.length() - i));
                        }
                    }
                    if (!extension.toString().equals(".fslv")) {
                        outputFile.renameTo(new File(outputFile.getAbsolutePath() + ".fslv"));
                        outputFile = new File(outputFile.getAbsolutePath() + ".fslv");
                    }
                    File files[] = selectedFile.getParentFile().listFiles();
                    int returnValue = FLGOptionPane.OK_OPTION;
                    for (int i = 0; i < files.length; i++) {
                        if (outputFile.getAbsolutePath().equals(files[i].getAbsolutePath())) {
                            returnValue = FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.exportLearningUnitView.fileExits.message"), internationalization.getString("dialog.exportLearningUnitView.fileExits.title"), FLGOptionPane.OK_CANCEL_OPTION, FLGOptionPane.QUESTION_MESSAGE);
                            break;
                        }
                    }
                    if (returnValue == FLGOptionPane.OK_OPTION) {
                        FileOutputStream os = new FileOutputStream(outputFile);
                        ZipOutputStream zipOutputStream = new ZipOutputStream(os);
                        ZipEntry zipEntry = new ZipEntry("dummy");
                        zipOutputStream.putNextEntry(zipEntry);
                        zipOutputStream.closeEntry();
                        zipOutputStream.flush();
                        zipOutputStream.finish();
                        zipOutputStream.close();
                        final File outFile = outputFile;
                        (new Thread() {

                            public void run() {
                                try {
                                    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outFile));
                                    File[] files = (new File(learningUnitViewManager.getLearningUnitViewOriginalDataDirectory().getPath())).listFiles();
                                    int maxSteps = files.length;
                                    int step = 1;
                                    exportProgressDialog = new FLGImageProgressDialog(null, 0, maxSteps, 0, getClass().getClassLoader().getResource("freestyleLearning/homeCore/images/fsl.gif"), (Color) UIManager.get("FSLColorBlue"), (Color) UIManager.get("FSLColorRed"), internationalization.getString("learningUnitViewExport.rogressbarText"));
                                    exportProgressDialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                    exportProgressDialog.setBarValue(step);
                                    buildExportZipFile("", zipOutputStream, files, step);
                                    zipOutputStream.flush();
                                    zipOutputStream.finish();
                                    zipOutputStream.close();
                                    exportProgressDialog.setBarValue(maxSteps);
                                    exportProgressDialog.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                    exportProgressDialog.dispose();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        os.close();
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        } else {
        }
    }

    private void buildExportZipFile(String parentDirectory, ZipOutputStream zipOutputStream, File[] files, int step) {
        try {
            for (int i = 0; i < files.length; i++) {
                exportProgressDialog.setBarValue(step++);
                if (files[i].isDirectory()) {
                    if (parentDirectory.equals("")) {
                        ZipEntry newZipEntry = new ZipEntry(files[i].getName() + "/");
                        zipOutputStream.putNextEntry(newZipEntry);
                        zipOutputStream.closeEntry();
                        zipOutputStream.flush();
                        File[] subFiles = files[i].listFiles();
                        buildExportZipFile(files[i].getName(), zipOutputStream, subFiles, step);
                    } else {
                        ZipEntry newZipEntry = new ZipEntry(parentDirectory + System.getProperty("file.separator") + files[i].getName() + "/");
                        zipOutputStream.putNextEntry(newZipEntry);
                        zipOutputStream.closeEntry();
                        zipOutputStream.flush();
                        File[] subFiles = files[i].listFiles();
                        buildExportZipFile(parentDirectory + System.getProperty("file.separator") + files[i].getName(), zipOutputStream, subFiles, step);
                    }
                } else {
                    ZipEntry newZipEntry;
                    if (parentDirectory.equals("")) {
                        newZipEntry = new ZipEntry(files[i].getName());
                    } else {
                        newZipEntry = new ZipEntry(parentDirectory + System.getProperty("file.separator") + files[i].getName());
                    }
                    zipOutputStream.putNextEntry(newZipEntry);
                    InputStream is = new FileInputStream(files[i]);
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                    is.close();
                    zipOutputStream.closeEntry();
                    zipOutputStream.flush();
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void showNewElementDialog() {
        newElementDialog = new FSLLearningUnitViewNewElementDialog(learningUnitViewManager.getFolderComponent(), learningUnitViewManager.getFolderImportComponent(), learningUnitViewManager.folderImportSupported());
        if (newElementDialog.showDialog(learningUnitViewElementsManager, selectedLearningUnitViewElementId, learningUnitViewElementsStructurePanel.getLearningUnitViewNewAndModifyElementDialogViewSpecificPane())) {
            if (learningUnitViewElementsStructurePanel.getFolderImport()) {
                String parentId = "none";
                if (newElementDialog.getInsertCommand() == FSLLearningUnitViewNewElementDialog.INSERT_AS_CHILD) {
                    if (selectedLearningUnitViewElementId != null) {
                        parentId = selectedLearningUnitViewElementId;
                    } else {
                        parentId = "none";
                    }
                }
                learningUnitViewElementsStructurePanel.createElements(parentId, newElementDialog.getInsertCommand(), selectedLearningUnitViewElementId);
            } else {
                String elementId = learningUnitViewElementsManager.createLearningUnitViewElementId();
                String parentId;
                if (newElementDialog.getInsertCommand() == FSLLearningUnitViewNewElementDialog.INSERT_AS_CHILD) {
                    if (selectedLearningUnitViewElementId != null) parentId = selectedLearningUnitViewElementId; else parentId = "none";
                } else {
                    if (selectedLearningUnitViewElementId != null) parentId = learningUnitViewElementsManager.getLearningUnitViewElement(selectedLearningUnitViewElementId, false).getParentId(); else parentId = "none";
                }
                String title = newElementDialog.getElementTitle();
                boolean folder = newElementDialog.isElementFolder();
                FSLLearningUnitViewElement newElement = learningUnitViewElementsStructurePanel.createLearningUnitViewElement(elementId, parentId, title, folder);
                newElement.setModified(true);
                learningUnitViewElementsManager.setModified(true);
                switch(newElementDialog.getInsertCommand()) {
                    case FSLLearningUnitViewNewElementDialog.INSERT_BEFORE:
                        learningUnitViewElementsManager.insertLearningUnitViewElementBefore(newElement, selectedLearningUnitViewElementId);
                        break;
                    case FSLLearningUnitViewNewElementDialog.INSERT_AFTER:
                        learningUnitViewElementsManager.insertLearningUnitViewElementAfter(newElement, selectedLearningUnitViewElementId);
                        break;
                    default:
                        learningUnitViewElementsManager.addLearningUnitViewElement(newElement, learningUnitViewElementsManager.isOriginalElementsOnly());
                        break;
                }
                FSLLearningUnitViewEvent event = FSLLearningUnitViewEvent.createElementsCreatedEvent(learningUnitViewManager.getLearningUnitViewManagerId(), new String[] { newElement.getId() });
                learningUnitViewManager.fireLearningUnitViewEvent(event);
                activeLearningUnitViewElementId = newElement.getId();
                event = FSLLearningUnitViewEvent.createElementActivatedEvent(learningUnitViewManager.getLearningUnitViewManagerId(), activeLearningUnitViewElementId, secondaryActiveLearningUnitViewElementId, false);
                learningUnitViewManager.fireLearningUnitViewEvent(event);
            }
        }
    }

    private void showModifyElementDialog() {
        if (modifyElementDialog.showDialog(learningUnitViewManager, learningUnitViewElementsManager, selectedLearningUnitViewElementId, learningUnitViewElementsStructurePanel.getLearningUnitViewNewAndModifyElementDialogViewSpecificPane())) {
            FSLLearningUnitViewElement element = learningUnitViewElementsManager.getLearningUnitViewElement(selectedLearningUnitViewElementId, true);
            String title = modifyElementDialog.getElementTitle();
            boolean folder = modifyElementDialog.isElementFolder();
            element.setTitle(title);
            element.setFolder(folder);
            learningUnitViewElementsStructurePanel.modifyLearningUnitViewElement(element);
            element.setModified(true);
            learningUnitViewElementsManager.setModified(true);
            FSLLearningUnitViewEvent event = FSLLearningUnitViewEvent.createElementsModifiedEvent(learningUnitViewManager.getLearningUnitViewManagerId(), new String[] { selectedLearningUnitViewElementId });
            learningUnitViewManager.fireLearningUnitViewEvent(event);
        }
    }

    private void showRemoveElementDialog() {
        if (FLGOptionPane.showConfirmDialog(internationalization.getString("dialog.removeElement.message"), internationalization.getString("dialog.removeElement.title"), FLGOptionPane.YES_NO_OPTION, FLGOptionPane.QUESTION_MESSAGE) == FLGOptionPane.YES_OPTION) {
            String[] elementsIdsRemoved;
            if (learningUnitViewElementsManager.isOriginalElementsOnly() || learningUnitViewElementsManager.getLearningUnitViewOriginalElement(selectedLearningUnitViewElementId) == null) {
                String[] descendantElementIds = learningUnitViewElementsManager.getDescendantIdsOfLearningUnitViewElement(selectedLearningUnitViewElementId);
                elementsIdsRemoved = new String[descendantElementIds.length + 1];
                for (int i = 0; i < descendantElementIds.length; i++) {
                    learningUnitViewElementsManager.removeLearningUnitViewElement(descendantElementIds[i]);
                    elementsIdsRemoved[i] = descendantElementIds[i];
                }
                learningUnitViewElementsManager.removeLearningUnitViewElement(selectedLearningUnitViewElementId);
                elementsIdsRemoved[descendantElementIds.length] = selectedLearningUnitViewElementId;
            } else {
                learningUnitViewElementsManager.removeLearningUnitViewElement(selectedLearningUnitViewElementId);
                elementsIdsRemoved = new String[] { selectedLearningUnitViewElementId };
            }
            FSLLearningUnitViewVetoableEvent vetoableEvent = FSLLearningUnitViewVetoableEvent.createElementsRemovingEvent(learningUnitViewManager.getLearningUnitViewManagerId(), elementsIdsRemoved);
            learningUnitViewManager.fireLearningUnitViewEvent(vetoableEvent);
            if (!vetoableEvent.isVeto()) {
                FSLLearningUnitViewEvent event = FSLLearningUnitViewEvent.createElementsRemovedEvent(learningUnitViewManager.getLearningUnitViewManagerId(), elementsIdsRemoved);
                learningUnitViewManager.fireLearningUnitViewEvent(event);
            }
            learningUnitViewElementsManager.setModified(true);
        }
    }

    private void showSortElementsDialog() {
        if (sortElementsDialog.showDialog()) {
            FSLLearningUnitViewElement element = learningUnitViewElementsManager.getLearningUnitViewElement(selectedLearningUnitViewElementId, true);
            String title = modifyElementDialog.getElementTitle();
            learningUnitViewElementsStructurePanel.structureTree.sortElements(selectedLearningUnitViewElementId, (sortElementsDialog.getSortSelection() == sortElementsDialog.SORT_ASCENDING));
            learningUnitViewElementsManager.setModified(true);
            FSLLearningUnitViewEvent event = FSLLearningUnitViewEvent.createElementsModifiedEvent(learningUnitViewManager.getLearningUnitViewManagerId(), new String[] { selectedLearningUnitViewElementId });
            learningUnitViewManager.fireLearningUnitViewEvent(event);
        }
    }

    private Image loadImage(String imageFileName) {
        return FLGImageUtility.loadImageAndWait(getClass().getClassLoader().getResource("freestyleLearning/learningUnitViewAPI/elementsStructurePanel/images/" + imageFileName));
    }

    class FSLLearningUnitElementsStructureEditToolBar_LearningUnitAdapter extends FSLLearningUnitAdapter {

        public void learningUnitEditModeChanged(FSLLearningUnitEvent event) {
            setVisible(event.isEditMode());
        }

        public void learningUnitUserViewChanged(FSLLearningUnitEvent event) {
            buildDependentUI();
        }
    }

    class FSLLearningUnitViewElementsStructureEditToolBar_LearningUnitViewAdapter extends FSLLearningUnitViewVetoableAdapter {

        public void performF2SpecificAction(FSLLearningUnitViewEvent event) {
            if (event.getLearningUnitViewElementId().equals(activeLearningUnitViewElementId)) {
                showModifyElementDialog();
            }
        }

        public void learningUnitViewElementsSelected(FSLLearningUnitViewEvent event) {
            if (event.getLearningUnitViewManagerId().equals(learningUnitViewManager.getLearningUnitViewManagerId())) {
                if (event.getLearningUnitViewElementIds() != null) selectedLearningUnitViewElementId = event.getLearningUnitViewElementIds()[0];
            }
        }

        public void learningUnitViewElementActivated(FSLLearningUnitViewEvent event) {
            if (event.getLearningUnitViewManagerId().equals(learningUnitViewManager.getLearningUnitViewManagerId())) {
                selectedLearningUnitViewElementId = event.getActiveLearningUnitViewElementId();
                activeLearningUnitViewElementId = selectedLearningUnitViewElementId;
                secondaryActiveLearningUnitViewElementId = event.getSecondaryActiveLearningUnitViewElementId();
                buildDependentUI();
            }
        }

        public void learningUnitViewElementsModified(FSLLearningUnitViewEvent event) {
            if (event.getLearningUnitViewManagerId().equals(learningUnitViewManager.getLearningUnitViewManagerId())) {
                buildDependentUI();
            }
        }

        public void learningUnitViewActivated(FSLLearningUnitViewEvent event) {
            if (event.getLearningUnitViewManagerId().equals(learningUnitViewManager.getLearningUnitViewManagerId())) {
                buildDependentUI();
            }
        }

        public void learningUnitViewElementsUserVersionCreated(FSLLearningUnitViewEvent event) {
            if (event.getLearningUnitViewManagerId().equals(learningUnitViewManager.getLearningUnitViewManagerId())) {
                buildDependentUI();
            }
        }
    }
}
