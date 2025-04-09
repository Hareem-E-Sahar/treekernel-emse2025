package com.google.code.wizard;

import com.google.code.model.ZipFragmentModel;
import com.google.code.tree.ZipFileSelectionTree;
import com.google.code.util.QuickZipBundle;
import com.google.code.util.QuickZipHelper;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Date: 12/19/10
 * Time: 2:54 AM
 */
public class MakeZipFileStep extends AbstractQuickZipStep {

    private static final Icon QUICK_ZIP_ICON = IconLoader.getIcon("/help/icons/zip-16.png");

    private final JPanel myRightPanel = new JPanel(new BorderLayout());

    private final JTextField zipFileNameField = new JTextField(30);

    public MakeZipFileStep(final Project project, final List<ZipFragmentModel> selectedFragments, final QuickZipWizard parentWizard) {
        super(project, selectedFragments, parentWizard);
    }

    public String getHelpId() {
        return QuickZipBundle.message("quick-zip.make-zip-file.helpId");
    }

    @Override
    protected boolean addToolbarForLeftSection() {
        return false;
    }

    @Override
    protected JPanel createRightSectionForCenterPanel() {
        return myRightPanel;
    }

    private String getDefaultZipFileLocation() {
        return myProject.getBaseDir().getPath() + "/quick-zip.zip";
    }

    @Override
    protected void updateRightPanel(final ZipFragmentModel fragmentModel) {
        final ZipFileSelectionTree selectionTreePanel = new ZipFileSelectionTree(myProject, mySelectedFragments);
        final JComponent toolbarComponent = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, selectionTreePanel.getTreeActions(), true).getComponent();
        final String defaultZipFileLoc = getDefaultZipFileLocation();
        zipFileNameField.setText(defaultZipFileLoc);
        final JButton zipFileSaveButton = new JButton(QuickZipBundle.message("select-zip-file.quick-zip.action.name"), QUICK_ZIP_ICON);
        zipFileSaveButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                final JFileChooser fc = new JFileChooser(new File(myProject.getBaseDir().getPath()));
                fc.setSelectedFile(new File(zipFileNameField.getText()));
                final int selectionMode = fc.showSaveDialog(myWholePanel);
                if (selectionMode == JFileChooser.APPROVE_OPTION) {
                    final File selFile = fc.getSelectedFile();
                    zipFileNameField.setText(selFile.getAbsolutePath());
                }
            }
        });
        final JPanel fragmentPanel = new JPanel();
        fragmentPanel.add(toolbarComponent, BorderLayout.NORTH);
        fragmentPanel.add(selectionTreePanel, BorderLayout.CENTER);
        final JPanel zipFilePanel = new JPanel(new BorderLayout());
        zipFilePanel.setBackground(Color.white);
        zipFilePanel.add(zipFileNameField, BorderLayout.CENTER);
        zipFilePanel.add(zipFileSaveButton, BorderLayout.EAST);
        myRightPanel.add(fragmentPanel, BorderLayout.CENTER);
        myRightPanel.add(zipFilePanel, BorderLayout.SOUTH);
        final Dimension d = myWholePanel.getPreferredSize();
        d.width = Math.max(d.width, 780);
        d.height = Math.max(d.height, 580);
        final int rightPanelWidth = (int) Math.max(400, d.width * (1 - proportion - 0.05));
        myRightPanel.setPreferredSize(new Dimension(rightPanelWidth, d.height - 100));
    }

    @Override
    public void _commit(final boolean finishChosen) throws CommitStepException {
        if (finishChosen) {
            final Runnable runnableProcess = new Runnable() {

                public void run() {
                    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                    final String zipFileName = zipFileNameField.getText();
                    final List<List<String>> rawFilesToZip = QuickZipHelper.getFilesStructureToZip(myProject, mySelectedFragments);
                    int totalFilesAndFolders = 0;
                    for (final List<String> loopData : rawFilesToZip) {
                        totalFilesAndFolders += loopData.size();
                    }
                    try {
                        final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
                        outputStream.setMethod(ZipOutputStream.DEFLATED);
                        outputStream.setComment("Zip file created by QuickZip");
                        int filesZipped = 0;
                        for (final List<String> loopData : rawFilesToZip) {
                            boolean firstEntryProcessed = false;
                            int prefixLengthToSkip = 0;
                            for (final String loopFileName : loopData) {
                                progressIndicator.setFraction((1.0 * filesZipped) / totalFilesAndFolders);
                                progressIndicator.setText2(QuickZipBundle.message("title.quick-zip.zip-message", loopFileName));
                                if (!firstEntryProcessed) {
                                    prefixLengthToSkip = loopFileName.lastIndexOf("/") + 1;
                                    firstEntryProcessed = true;
                                } else {
                                    final File loopFile = new File(loopFileName);
                                    if (loopFile.isFile()) {
                                        final String zipEntryPath = loopFileName.substring(prefixLengthToSkip);
                                        final byte[] buf = new byte[1024];
                                        outputStream.putNextEntry(new ZipEntry(zipEntryPath));
                                        final FileInputStream loopInputStream = new FileInputStream(loopFile);
                                        int len;
                                        while ((len = loopInputStream.read(buf)) > 0) {
                                            outputStream.write(buf, 0, len);
                                        }
                                        outputStream.closeEntry();
                                        loopInputStream.close();
                                    }
                                }
                                filesZipped++;
                            }
                        }
                        outputStream.close();
                    } catch (Exception ex) {
                        Messages.showErrorDialog(myProject, ex.getMessage(), "Error");
                    }
                }
            };
            final String progressTitle = QuickZipBundle.message("title.quick-zip.zip-dialog");
            ProgressManager.getInstance().runProcessWithProgressSynchronously(runnableProcess, progressTitle, false, myProject);
        }
    }
}
