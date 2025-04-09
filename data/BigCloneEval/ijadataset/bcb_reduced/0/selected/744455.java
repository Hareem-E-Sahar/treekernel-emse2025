package com.rapidminer.gui.community;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import com.rapidminer.Process;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceActionAdapter;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.io.process.XMLImporter;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttributes;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypePassword;
import com.rapidminer.parameter.ParameterTypeRepositoryLocation;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.ProcessEntry;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.RepositoryVisitor;
import com.rapidminer.repository.gui.RepositoryTree;
import com.rapidminer.tools.ProgressListener;
import com.rapidminer.tools.patterns.Visitor;

/**
 * 
 * @author Simon Fischer
 *
 */
public class UploadProcessBundleDialog extends ButtonDialog {

    private static final long serialVersionUID = 1L;

    private JCheckBox excludeFiles = new JCheckBox(new ResourceActionAdapter("community.exclude_files"));

    private JCheckBox excludeAttributes = new JCheckBox(new ResourceActionAdapter("community.exclude_attributes"));

    private RepositoryTree repositoryTree = new RepositoryTree(this);

    private JRadioButton includeParametersButton = new JRadioButton(new ResourceActionAdapter("community.include_parameters"));

    private JRadioButton onlyStructureButton = new JRadioButton(new ResourceActionAdapter("community.only_structure"));

    private JCheckBox excludeDescription = new JCheckBox(new ResourceActionAdapter("community.exclude_description"));

    public UploadProcessBundleDialog() {
        super("community.upload_process_bundle", false);
        onlyStructureButton.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                boolean on = !onlyStructureButton.isSelected();
                excludeAttributes.setEnabled(on);
                excludeFiles.setEnabled(on);
            }
        });
        ButtonGroup exportTypeGroup = new ButtonGroup();
        exportTypeGroup.add(includeParametersButton);
        exportTypeGroup.add(onlyStructureButton);
        includeParametersButton.setSelected(true);
        excludeFiles.setSelected(false);
        excludeAttributes.setSelected(false);
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = c.weightx = 1;
        c.insets = new Insets(4, 4, 4, 4);
        c.ipadx = c.ipady = 4;
        c.gridwidth = GridBagConstraints.REMAINDER;
        main.add(includeParametersButton, c);
        c.insets = new Insets(0, 24, 0, 0);
        main.add(excludeFiles, c);
        main.add(excludeAttributes, c);
        c.insets = new Insets(4, 4, 4, 4);
        main.add(onlyStructureButton, c);
        main.add(excludeDescription, c);
        main.add(new JScrollPane(repositoryTree), c);
        layoutDefault(main, DEFAULT_SIZE, makeOkButton("community.upload_process_bundle_now"), makeCancelButton());
    }

    @Override
    protected void ok() {
        new ProgressThread("community.uploading_bundle", true) {

            @Override
            public void run() {
                getProgressListener().setTotal(100);
                getProgressListener().setCompleted(5);
                startUpload(getProgressListener());
                getProgressListener().complete();
            }
        }.start();
    }

    private void startUpload(ProgressListener progressListener) {
        final boolean stripComments = excludeDescription.isSelected();
        final boolean stripFiles = excludeFiles.isSelected();
        final boolean stripAtts = excludeAttributes.isSelected();
        final boolean stripAll = onlyStructureButton.isSelected();
        final Visitor<Operator> stripParametersVisitor = new Visitor<Operator>() {

            @Override
            public void visit(Operator op) {
                if (stripComments) {
                    log("Removing comment");
                    op.setUserDescription(null);
                }
                Parameters params = op.getParameters();
                for (ParameterType type : params.getParameterTypes()) {
                    if (stripAll || (type instanceof ParameterTypePassword) || (stripFiles && ((type instanceof ParameterTypeFile) || (type instanceof ParameterTypeDirectory) || (type instanceof ParameterTypeRepositoryLocation))) || (stripAtts && ((type instanceof ParameterTypeAttributes) || (type instanceof ParameterTypeAttributes)))) {
                        log("Removing: " + type.getKey());
                        params.setParameter(type.getKey(), "--STRIPPED--");
                    }
                }
            }
        };
        final AtomicInteger count = new AtomicInteger();
        final ZipOutputStream zipOut;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(new File("/home/simon/test.zip")));
        } catch (FileNotFoundException e1) {
            SwingTools.showSimpleErrorMessage("community.error_uploading_process_bundle", e1, e1.toString());
            return;
        }
        RepositoryVisitor<ProcessEntry> visitor = new RepositoryVisitor<ProcessEntry>() {

            @Override
            public boolean visit(ProcessEntry entry) {
                log("Visiting: " + entry.getLocation());
                try {
                    Process process = new Process(entry.retrieveXML());
                    process.getRootOperator().walk(stripParametersVisitor);
                    ZipEntry zipEntry = new ZipEntry(count.incrementAndGet() + ".xml");
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(process.getRootOperator().getXML(false).getBytes(XMLImporter.PROCESS_FILE_CHARSET));
                    zipOut.closeEntry();
                } catch (Exception e) {
                    SwingTools.showSimpleErrorMessage("community.error_uploading_process_bundle", e, e.toString());
                    return false;
                }
                return true;
            }
        };
        try {
            for (TreePath path : repositoryTree.getSelectionPaths()) {
                Entry entry = (Entry) path.getLastPathComponent();
                RepositoryManager.getInstance(null).walk(entry, visitor, ProcessEntry.class);
            }
        } catch (RepositoryException e) {
            SwingTools.showSimpleErrorMessage("community.error_uploading_process_bundle", e, e.toString());
        } finally {
            try {
                zipOut.close();
            } catch (IOException e) {
                SwingTools.showSimpleErrorMessage("community.error_uploading_process_bundle", e, e.toString());
            }
        }
        super.ok();
    }

    private void log(String string) {
        System.out.println(string);
    }

    public static void showDialog() {
        new UploadProcessBundleDialog().setVisible(true);
    }
}
