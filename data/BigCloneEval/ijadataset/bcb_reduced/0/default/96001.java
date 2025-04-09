import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: vince
 * Date: 5/14/11
 * Time: 5:58 PM
 */
public class StatusWindow {

    private JButton closeButton;

    private JLabel statusLabel;

    public JPanel mainPanel;

    private JProgressBar downloadProgress;

    private JLabel progressField;

    private JButton abortCurrentDownloadButton;

    private JLabel timeRemainingField;

    private JButton openFileButton;

    private JButton openDirectoryButton;

    private JButton removeFromListButton;

    private JButton clearAllButton;

    private JTable finishedFiles;

    private boolean running = true;

    private JFrame frame;

    public StatusWindow(JFrame jframe) {
        this.frame = jframe;
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                frame.setVisible(false);
                running = false;
            }
        });
        Thread downloadProgressThread = new Thread() {

            public void run() {
                while (running) {
                    killTransferIfTransferDied();
                    setStatusComponents();
                    updateCompletedTable();
                    System.out.print(".");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        downloadProgressThread.start();
        abortCurrentDownloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                XDCCConnectionManager.currentFileTransfer.close();
            }
        });
        openFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    try {
                        desktop.open(XDCCConnectionManager.completedFiles.get(finishedFiles.getSelectedRow()));
                    } catch (IOException e) {
                        DialogBuilder.showErrorDialog("Error", "Could not open file!");
                    }
                }
            }
        });
        openDirectoryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    try {
                        desktop.open(XDCCConnectionManager.completedFiles.get(finishedFiles.getSelectedRow()).getParentFile());
                    } catch (IOException e) {
                        DialogBuilder.showErrorDialog("Error", "Could not open folder!");
                    }
                }
            }
        });
        removeFromListButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                XDCCConnectionManager.completedFiles.remove(finishedFiles.getSelectedRow());
            }
        });
        clearAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                XDCCConnectionManager.completedFiles.clear();
            }
        });
    }

    private void updateCompletedTable() {
        if (finishedFilesTableShouldChange()) {
            ((DefaultTableModel) finishedFiles.getModel()).setDataVector(buildStringArrayFromFinishedFiles(), new String[] { "File Name" });
        }
    }

    private boolean finishedFilesTableShouldChange() {
        return finishedFiles.getRowCount() != XDCCConnectionManager.completedFiles.size();
    }

    private void setStatusComponents() {
        if (XDCCConnectionManager.currentFileTransfer == null || XDCCConnectionManager.currentFileTransfer.getProgressPercentage() == 100 || !XDCCConnectionManager.currentFileTransfer.isOpen()) {
            statusLabel.setText("Idle");
            downloadProgress.setValue(0);
            progressField.setText("");
            timeRemainingField.setText("");
            abortCurrentDownloadButton.setEnabled(false);
        } else {
            statusLabel.setText("Downloading " + XDCCConnectionManager.currentFileTransfer.getFile().getName());
            downloadProgress.setValue((int) XDCCConnectionManager.currentFileTransfer.getProgressPercentage());
            String progressInMB = roundTwoDecimals(XDCCConnectionManager.currentFileTransfer.getProgress() / 1048576.0) + "MB";
            String totalSizeInMB = roundTwoDecimals(XDCCConnectionManager.currentFileTransfer.getSize() / 1048576.0) + "MB";
            String transferRateInKBPS = (int) (XDCCConnectionManager.currentFileTransfer.getTransferRate() / 1024) + "KB/s";
            progressField.setText(progressInMB + " / " + totalSizeInMB + " - " + transferRateInKBPS);
            timeRemainingField.setText("Time Remaining: " + XDCCConnectionManager.currentFileTransfer.getTimeRemaining());
            abortCurrentDownloadButton.setEnabled(true);
        }
    }

    private void killTransferIfTransferDied() {
        if (XDCCConnectionManager.currentFileTransfer != null && XDCCConnectionManager.currentFileTransfer.getTransferRate() == 0) {
            try {
                Thread.sleep(10000);
                if (XDCCConnectionManager.currentFileTransfer != null && XDCCConnectionManager.currentFileTransfer.getTransferRate() == 0) {
                    XDCCConnectionManager.currentFileTransfer.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.###");
        return Double.valueOf(twoDForm.format(d));
    }

    private void createUIComponents() {
        DefaultTableModel tableModel = new DefaultTableModel(buildStringArrayFromFinishedFiles(), new String[] { "File Name" });
        finishedFiles = new JTable(tableModel);
    }

    private String[][] buildStringArrayFromFinishedFiles() {
        String[][] completedFiles = new String[XDCCConnectionManager.completedFiles.size()][1];
        int i = 0;
        for (File completedFile : XDCCConnectionManager.completedFiles) {
            completedFiles[i++][0] = completedFile.getName();
        }
        return completedFiles;
    }
}
