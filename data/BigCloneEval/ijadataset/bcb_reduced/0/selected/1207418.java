package org.hardtokenmgmt.admin.ui.panels.manreports;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.hardtokenmgmt.admin.control.AdminInterfacesFactory;
import org.hardtokenmgmt.admin.ui.AdminUIUtils;
import org.hardtokenmgmt.common.Constants;
import org.hardtokenmgmt.common.vo.GeneratedReportDataVO;
import org.hardtokenmgmt.core.log.LocalLog;
import org.hardtokenmgmt.core.ui.UIHelper;
import org.hardtokenmgmt.core.util.CommonUtils;
import org.hardtokenmgmt.ws.gen.AuthorizationDeniedException_Exception;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Open or save report dialog
 * 
 * 
 * @author Philip Vendil 26 juni 2010
 *
 * @version $Id$
 */
public class OpenOrSaveReportDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private Dimension DIALOG_SIZE = new Dimension(640, 314);

    private GeneratedReportDataVO report = null;

    private JDialog thisPanel = this;

    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    private JLabel statusLabel;

    private JLabel statusHeaderLabel;

    private boolean useDepartments;

    public OpenOrSaveReportDialog() {
        super();
        initComponents();
    }

    public OpenOrSaveReportDialog(GeneratedReportDataVO report, boolean useDepartments, Window owner) {
        super(owner, ModalityType.MODELESS);
        this.report = report;
        this.useDepartments = useDepartments;
        initComponents();
    }

    private void initComponents() {
        setSize(DIALOG_SIZE);
        setTitle(UIHelper.getText("manreports.openorsavetitle"));
        setIconImage(AdminUIUtils.getAdminWindowIcon());
        getContentPane().setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("53dlu"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("43dlu"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("19dlu"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("77dlu"), ColumnSpec.decode("4dlu:grow(1.0)"), ColumnSpec.decode("100dlu"), FormFactory.RELATED_GAP_COLSPEC }, new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("32dlu"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("7dlu:grow(1.0)"), RowSpec.decode("34dlu"), FormFactory.RELATED_GAP_ROWSPEC }));
        final JButton openButton = new JButton();
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                try {
                    File file = File.createTempFile(generateReportName(), generateReportSuffix());
                    Thread t = new Thread(new DownloadReportRunnable(report, file, true));
                    t.start();
                } catch (IOException e1) {
                    LocalLog.getLogger().log(Level.SEVERE, "Error occured when writing report to file : " + e1.getMessage(), e1);
                    AdminUIUtils.showErrorMsg("manreports.errorwritingtofile", thisPanel);
                }
            }
        });
        openButton.setIcon(UIHelper.getImage("view_report_small.png"));
        openButton.setText(UIHelper.getText("manreports.openreport"));
        getContentPane().add(openButton, new CellConstraints(2, 16, 3, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JButton saveButton = new JButton();
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                String fileName = generateReportName() + generateReportSuffix();
                JFileChooser fileChooser = new JFileChooser(CommonUtils.getCurrentSaveDirectory() + "/" + fileName);
                fileChooser.setSelectedFile(new File(fileName));
                int returnVal = fileChooser.showSaveDialog(thisPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    CommonUtils.setCurrentSaveDirectory(fileChooser.getSelectedFile());
                    File file = fileChooser.getSelectedFile();
                    if (file.isDirectory()) {
                        AdminUIUtils.showErrorMsg("adminca.fileisdirectory", thisPanel);
                        return;
                    }
                    if (file.exists()) {
                        if (!confirmOverwrite()) {
                            return;
                        } else {
                            if (!file.canWrite()) {
                                AdminUIUtils.showErrorMsg("adminca.filenotwritable", thisPanel);
                                return;
                            }
                        }
                    }
                    Thread t = new Thread(new DownloadReportRunnable(report, file, false));
                    t.start();
                }
            }
        });
        saveButton.setIcon(UIHelper.getImage("download.gif"));
        saveButton.setText(UIHelper.getText("manreports.savereport"));
        getContentPane().add(saveButton, new CellConstraints(6, 16, 3, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JButton closeButton = new JButton();
        closeButton.setText(UIHelper.getText("close"));
        closeButton.setIcon(UIHelper.getImage("exit.png"));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                thisPanel.setVisible(false);
            }
        });
        getContentPane().add(closeButton, new CellConstraints(10, 16, CellConstraints.DEFAULT, CellConstraints.FILL));
        statusHeaderLabel = new JLabel();
        statusHeaderLabel.setText(UIHelper.getText("manreports.status") + ":");
        statusHeaderLabel.setVisible(false);
        getContentPane().add(statusHeaderLabel, new CellConstraints(2, 4, 5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel descriptionLabel = new JLabel();
        descriptionLabel.setText(UIHelper.getText("manreports.openorsavereport"));
        descriptionLabel.setFont(UIHelper.getLabelFontBold());
        getContentPane().add(descriptionLabel, new CellConstraints(4, 2, 7, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        statusLabel = new JLabel();
        statusLabel.setText("");
        statusLabel.setVisible(false);
        getContentPane().add(statusLabel, new CellConstraints(8, 4, 3, 1));
        final JLabel iconLabel = new JLabel();
        iconLabel.setIcon(UIHelper.getImage("fetchreport_large.png"));
        getContentPane().add(iconLabel, new CellConstraints(2, 2, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel nameHeaderLabel = new JLabel();
        nameHeaderLabel.setText(UIHelper.getText("manreports.name") + ":");
        getContentPane().add(nameHeaderLabel, new CellConstraints(2, 6, 5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel generatedHeaderLabel = new JLabel();
        generatedHeaderLabel.setText(UIHelper.getText("manreports.generationdate") + ":");
        getContentPane().add(generatedHeaderLabel, new CellConstraints(2, 10, 5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel timeSpanHeaderLabel = new JLabel();
        timeSpanHeaderLabel.setText(UIHelper.getText("manreports.timespan") + ":");
        getContentPane().add(timeSpanHeaderLabel, new CellConstraints(2, 12, 5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel nameLabel = new JLabel();
        String name = report.getName();
        if (useDepartments && report.getDepartment() != null && !report.getDepartment().equals("")) {
            if (report.getDepartment().equals("department.all")) {
                name += ", " + UIHelper.getText("manreports.department.all");
            } else {
                name += ", " + report.getDepartment();
            }
        }
        nameLabel.setText(name);
        getContentPane().add(nameLabel, new CellConstraints(8, 6, 3, 1));
        final JLabel generatedTimeLabel = new JLabel();
        generatedTimeLabel.setText(dateFormat.format(new Date(report.getGenerationDate())));
        getContentPane().add(generatedTimeLabel, new CellConstraints(8, 10, 3, 1));
        final JLabel timeSpanLabel = new JLabel();
        timeSpanLabel.setText(dateFormat.format(new Date(report.getTimeSpanStartDate())) + " " + UIHelper.getText("manreports.to") + " " + dateFormat.format(new Date(report.getTimeSpanEndDate())));
        getContentPane().add(timeSpanLabel, new CellConstraints(8, 12, 3, 1));
        final JLabel fileSizeHeaderLabel = new JLabel();
        fileSizeHeaderLabel.setText(UIHelper.getText("manreports.filesize") + ":");
        getContentPane().add(fileSizeHeaderLabel, new CellConstraints(2, 14, 5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel fileSizeLabel = new JLabel();
        fileSizeLabel.setText(AdminUIUtils.getReadableDataSizes(report.getDataSize()));
        getContentPane().add(fileSizeLabel, new CellConstraints(8, 14, 3, 1));
        final JLabel reportFormatHeaderLabel = new JLabel();
        reportFormatHeaderLabel.setText(UIHelper.getText("manreports.reportformat") + ":");
        getContentPane().add(reportFormatHeaderLabel, new CellConstraints(2, 8, 5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel reportFormatLabel = new JLabel();
        reportFormatLabel.setText(AdminUIUtils.getReportFormatText(report.getReportFormat()));
        getContentPane().add(reportFormatLabel, new CellConstraints(8, 8, 3, 1));
    }

    boolean confirmOverwrite() {
        Object[] options = { UIHelper.getText("yes"), UIHelper.getText("no") };
        int n = JOptionPane.showOptionDialog(this, UIHelper.getText("manreports.overwritefile"), UIHelper.getText("manreports.overwritetitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        return n == 0;
    }

    private String generateReportName() {
        String retval = report.getName();
        if (useDepartments && report.getDepartment() != null && !report.getDepartment().equals("")) {
            if (report.getDepartment().equals("department.all")) {
                retval += "_" + UIHelper.getText("manreports.department.all");
            } else {
                retval += "_" + report.getDepartment();
            }
        }
        retval += "_" + dateFormat.format(report.getGenerationDate());
        retval = retval.replaceAll(" ", "_");
        return retval;
    }

    private String generateReportSuffix() {
        if (report.getReportFormat().equals(Constants.REPORT_FORMAT_PDF)) {
            return ".pdf";
        }
        if (report.getReportFormat().equals(Constants.REPORT_FORMAT_XHTML)) {
            return ".x.html";
        }
        if (report.getReportFormat().equals(Constants.REPORT_FORMAT_EXCEL)) {
            return ".xls";
        }
        return null;
    }

    public static void main(String[] args) throws IOException, AuthorizationDeniedException_Exception {
        UIHelper.setTheme();
        GeneratedReportDataVO report = AdminInterfacesFactory.getGeneratedReportsManager().getReport(1);
        OpenOrSaveReportDialog d = new OpenOrSaveReportDialog(report, false, null);
        d.setVisible(true);
    }

    private class DownloadReportRunnable implements Runnable {

        private GeneratedReportDataVO report = null;

        private File outputFile = null;

        private boolean open = false;

        DownloadReportRunnable(GeneratedReportDataVO report, File outputFile, boolean open) {
            this.report = report;
            this.outputFile = outputFile;
            this.open = open;
        }

        @Override
        public void run() {
            updateStatus("manreports.downloading", UIHelper.getErrorMsgForeground());
            try {
                GeneratedReportDataVO reportWithData = AdminInterfacesFactory.getGeneratedReportsManager().getReport(report.getId());
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(reportWithData.getData());
                fos.close();
                if (open) {
                    updateStatus("manreports.openingreport", UIHelper.getNormalMsgForeground());
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(outputFile);
                        closeWindow();
                    } else {
                        updateStatus("manreports.desktopnotsupported", UIHelper.getErrorMsgForeground());
                    }
                } else {
                    JOptionPane.showMessageDialog(thisPanel, UIHelper.getText("manreports.filedownloadedmsg") + ": " + outputFile, UIHelper.getText("manreports.filedownloadedtitle"), JOptionPane.INFORMATION_MESSAGE);
                }
                updateStatus("manreports.downloadcomplete", UIHelper.getNormalMsgForeground());
            } catch (IOException e1) {
                LocalLog.getLogger().log(Level.SEVERE, "Error occured when writing report to file : " + e1.getMessage(), e1);
                AdminUIUtils.showErrorMsg("manreports.errorwritingtofile", thisPanel);
            } catch (AuthorizationDeniedException_Exception e) {
                LocalLog.getLogger().log(Level.SEVERE, "Error not authorized to download report: " + e.getMessage(), e);
                AdminUIUtils.showErrorMsg("manreports.errornotauthtoreport", thisPanel);
            }
        }

        private void updateStatus(final String statusMsg, final Color color) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    statusHeaderLabel.setVisible(true);
                    statusLabel.setVisible(true);
                    statusLabel.setText(UIHelper.getText(statusMsg));
                    statusLabel.setForeground(color);
                }
            });
        }
    }

    private void closeWindow() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                dispose();
            }
        });
    }
}
