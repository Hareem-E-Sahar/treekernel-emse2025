package neembuuuploader;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.utils.NULogger;

/**
 * This class is used by NeembuuUploader class for displaying Popup Menu
 * Don't mess with this class. (package private)
 * @author vigneshwaran
 */
class PopupBuilder implements ClipboardOwner {

    private static final PopupBuilder INSTANCE = new PopupBuilder();

    private Clipboard clipboard = null;

    private JPopupMenu popup = null;

    private JMenuItem copyDownloadURL = null;

    private JMenuItem copyDeleteURL = null;

    private JMenuItem exportLinksURL = null;

    private JMenuItem gotoDownloadURL = null;

    private JMenuItem removeFromQueue = null;

    private JMenuItem removeFinished = null;

    private JMenuItem stopUpload = null;

    private JTable table = null;

    private int[] selectedrows = null;

    private boolean multiple = false;

    private PopupBuilder() {
        NULogger.getLogger().info("Initializing PopupBuilder");
        table = NeembuuUploader.getInstance().getTable();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        popup = new JPopupMenu();
        copyDownloadURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/copydownloadurl.png")));
        copyDownloadURL.addActionListener(new CopyDownloadURLActionListener());
        popup.add(copyDownloadURL);
        copyDeleteURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/copydeleteurl.png")));
        copyDeleteURL.addActionListener(new CopyDeleteURLActionListener());
        popup.add(copyDeleteURL);
        exportLinksURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/exporturl.png")));
        exportLinksURL.addActionListener(new ExportLinksActionListener());
        popup.add(exportLinksURL);
        gotoDownloadURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/gotodownloadurl.png")));
        gotoDownloadURL.addActionListener(new GotoDownloadURLActionListener());
        popup.add(gotoDownloadURL);
        popup.add(new JSeparator());
        removeFromQueue = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/removefromlist.png")));
        removeFromQueue.addActionListener(new RemoveFromQueueActionListener());
        popup.add(removeFromQueue);
        removeFinished = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/removefinished.png")));
        removeFinished.addActionListener(new RemoveFinishedActionListener());
        popup.add(removeFinished);
        popup.add(new JSeparator());
        stopUpload = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/stopupload.png")));
        stopUpload.addActionListener(new StopUploadActionListener());
        popup.add(stopUpload);
    }

    /**
     * 
     * @return singleton instance of popupbuilder
     */
    public static PopupBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Display popup menu at the specified parameters (component, x and y position)
     * @param invoker
     * @param x
     * @param y 
     */
    public void show(Component invoker, int x, int y) {
        selectedrows = table.getSelectedRows();
        if (table.getSelectedRowCount() > 1) {
            multiple = true;
        } else {
            multiple = false;
        }
        copyDownloadURL.setEnabled(canCopyDownloadURLEnable());
        copyDeleteURL.setEnabled(canCopyDeleteURLEnable());
        exportLinksURL.setEnabled(canExportLinksEnable());
        gotoDownloadURL.setEnabled(canGotoDownloadURLEnable());
        removeFromQueue.setEnabled(canRemoveFromQueueEnable());
        removeFinished.setEnabled(canRemoveFinishedEnable());
        stopUpload.setEnabled(canStopUploadEnable());
        popup.show(invoker, x, y);
        NULogger.getLogger().info("Popup Menu displayed");
    }

    /**
     * Implemented method. Hope this method may never be called.
     * @param clipboard
     * @param contents 
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        NULogger.getLogger().log(Level.WARNING, "{0}: Lost clipboard ownership", getClass().getName());
    }

    /**
     * 
     * @return whether the menuitem can be enabled or not depending on the status of selected rows
     */
    private boolean canCopyDownloadURLEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED) {
                i++;
            }
        }
        copyDownloadURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.copyDownloadURL"));
        if (i >= 1) {
            if (multiple) {
                copyDownloadURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.copyDownloadURL") + " (" + i + ")");
            }
            return true;
        }
        return false;
    }

    private boolean canCopyDeleteURLEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED && table.getValueAt(selectedrow, NUTableModel.DELETEURL) != UploadStatus.NA) {
                i++;
            }
        }
        copyDeleteURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.copyDeleteURL"));
        if (i >= 1) {
            if (multiple) {
                copyDeleteURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.copyDeleteURL") + " (" + i + ")");
            }
            return true;
        }
        return false;
    }

    private boolean canExportLinksEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED) {
                i++;
            }
        }
        exportLinksURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.exportLinks"));
        if (i >= 1) {
            if (multiple) {
                exportLinksURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.exportLinks") + " (" + i + ")");
            }
            return true;
        }
        return false;
    }

    private boolean canGotoDownloadURLEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED) {
                i++;
            }
        }
        gotoDownloadURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.gotoDownloadURL"));
        if (i >= 1) {
            if (multiple) {
                gotoDownloadURL.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.gotoDownloadURL") + " (" + i + ")");
            }
            return true;
        }
        return false;
    }

    private boolean canRemoveFromQueueEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.QUEUED || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFAILED || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADSTOPPED) {
                i++;
            }
        }
        removeFromQueue.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.removeFromQueue"));
        if (i >= 1) {
            if (multiple) {
                removeFromQueue.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.removeFromQueue") + " (" + i + ")");
            }
            return true;
        }
        return false;
    }

    private boolean canRemoveFinishedEnable() {
        int finishedrows = 0;
        for (int i = 0; i < NUTableModel.uploadList.size(); i++) {
            if (table.getValueAt(i, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED) {
                finishedrows++;
            }
        }
        removeFinished.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.removeFinished"));
        if (finishedrows > 0) {
            removeFinished.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.removeFinished") + " (" + finishedrows + ")");
            return true;
        }
        return false;
    }

    private boolean canStopUploadEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.INITIALISING || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADING) {
                i++;
            }
        }
        stopUpload.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.stopUpload"));
        if (i >= 1) {
            if (multiple) {
                stopUpload.setText(TranslationProvider.get("neembuuuploader.PopupBuilder.stopUpload") + " (" + i + ")");
            }
            return true;
        }
        return false;
    }

    /**
     * Action listener for CopyDownloadURL menu item
     */
    class CopyDownloadURLActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            NULogger.getLogger().info("Copy Download URL clicked");
            StringBuilder listofurls = new StringBuilder();
            for (int selectedrow : selectedrows) {
                listofurls.append(table.getValueAt(selectedrow, NUTableModel.DOWNLOADURL).toString()).append("\n");
            }
            setClipboardContent(listofurls.toString());
        }
    }

    class CopyDeleteURLActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            NULogger.getLogger().info("Copy Delete URL clicked");
            StringBuilder listofurls = new StringBuilder();
            for (int selectedrow : selectedrows) {
                listofurls.append(table.getValueAt(selectedrow, NUTableModel.DELETEURL).toString()).append("\n");
            }
            setClipboardContent(listofurls.toString());
        }
    }

    class ExportLinksActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            NULogger.getLogger().info("Export links clicked");
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogTitle(TranslationProvider.get("neembuuuploader.PopupBuilder.exportLinkDialog"));
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new FileNameExtensionFilter("HTML File", new String[] { "html" }));
            if (fc.showSaveDialog(NeembuuUploader.getInstance()) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File htmlfile = fc.getSelectedFile();
            int startingrow = table.getSelectedRow();
            int endingrow = startingrow + table.getSelectedRowCount() - 1;
            StringBuilder sb = new StringBuilder("<html>" + "<head><title>Neembuu Uploader</title>" + "<style type='text/css'>" + " table { display:block; width:90%; border-top:1px solid #e5eff8; border-right:1px solid #e5eff8; margin:1em auto;border-collapse:collapse;}" + " td { color:#7892a8;border-bottom:1px solid #e5eff8;border-left:1px solid #e5eff8;padding:.3em 1em;text-align:center;}" + " tr.odd td { background:#f7fbff } tr.odd .column1 {background:#f4f9fe;} .column1 {background:#f9fcfe;}" + " th, h3 { background:#f4f9fe; text-align:center; font:bold 1.2em/2em 'Century Gothic','Trebuchet MS',Arial,Helvetica,sans-serif; color:#66a3d3; }" + "</style></head>" + "<body><center><h3>Neembuu Uploader Exported Linkset</h3></center>" + "<table><tr class='odd'><th class='column1'>File</th><th>Size</th><th>Host</th><th>Download URL</th><th>Delete URL if any</th></tr>");
            int i = 1;
            for (int row = startingrow; row <= endingrow; row++) {
                if (table.getValueAt(row, NUTableModel.STATUS) != UploadStatus.UPLOADFINISHED) {
                    continue;
                }
                if (i % 2 == 0) {
                    sb.append("<tr class='odd'>");
                } else {
                    sb.append("<tr>");
                }
                for (int column = 0; column < table.getModel().getColumnCount(); column++) {
                    if (column == NUTableModel.STATUS || column == NUTableModel.PROGRESS) {
                        continue;
                    }
                    if (column == 0) {
                        sb.append("<td class='column1'>");
                    } else {
                        sb.append("<td>");
                    }
                    if (column == NUTableModel.DOWNLOADURL && !(table.getModel().getValueAt(row, column).equals(UploadStatus.NA.getDefaultLocaleSpecificString()) || table.getModel().getValueAt(row, column).equals(UploadStatus.NA.getLocaleSpecificString()))) {
                        sb.append("<a target='_blank' href='").append(table.getModel().getValueAt(row, column)).append("'>").append(table.getModel().getValueAt(row, column)).append("</a>");
                    } else {
                        sb.append(table.getModel().getValueAt(row, column));
                    }
                    sb.append("</td>");
                }
                sb.append("</tr>");
                i++;
            }
            sb.append("</table></body></html>");
            try {
                NULogger.getLogger().log(Level.INFO, "{0}: Writing links to html file..", getClass().getName());
                if (!(htmlfile.getName().toLowerCase().endsWith(".html") || htmlfile.getName().toLowerCase().endsWith(".htm"))) {
                    htmlfile = new File(htmlfile.getAbsolutePath() + ".html");
                }
                PrintWriter writer = new PrintWriter(new FileWriter(htmlfile));
                writer.write(sb.toString());
                writer.close();
            } catch (Exception ex) {
                NULogger.getLogger().log(Level.INFO, "{0}: Error while writing html file\n{1}", new Object[] { getClass().getName(), ex });
                System.err.println(ex);
            }
        }
    }

    class GotoDownloadURLActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            NULogger.getLogger().info("Goto Download URL clicked..");
            for (int selectedrow : selectedrows) {
                String url = table.getValueAt(selectedrow, NUTableModel.DOWNLOADURL).toString();
                if (!Desktop.isDesktopSupported()) {
                    return;
                }
                try {
                    NULogger.getLogger().log(Level.INFO, "Opening url in browser: {0}", url);
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    NULogger.getLogger().log(Level.WARNING, "{0}: Cannot load url: {1}", new Object[] { getClass().getName(), url });
                    System.err.println(ex);
                }
            }
        }
    }

    class RemoveFromQueueActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Remove from Queue menu item clicked");
                    QueueManager.getInstance().setQueueLock(true);
                    int selectedrow;
                    for (int i = selectedrows.length - 1; i >= 0; i--) {
                        selectedrow = selectedrows[i];
                        if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.QUEUED || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADFAILED || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADSTOPPED) {
                            NUTableModel.getInstance().removeUpload(selectedrow);
                            NULogger.getLogger().log(Level.INFO, "{0}: Removed row no. {1}", new Object[] { getClass().getName(), selectedrow });
                        }
                    }
                    QueueManager.getInstance().setQueueLock(false);
                }
            }.start();
        }
    }

    class RemoveFinishedActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Remove Finished menu item clicked..");
                    QueueManager.getInstance().setQueueLock(true);
                    for (int i = NUTableModel.uploadList.size() - 1; i >= 0; i--) {
                        if (table.getValueAt(i, NUTableModel.STATUS) == UploadStatus.UPLOADFINISHED) {
                            NUTableModel.getInstance().removeUpload(i);
                            NULogger.getLogger().log(Level.INFO, "{0}: Removed row no. {1}", new Object[] { getClass().getName(), i });
                        }
                    }
                    QueueManager.getInstance().setQueueLock(false);
                }
            }.start();
        }
    }

    class StopUploadActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Stop Upload clicked..");
                    for (int selectedrow : selectedrows) {
                        if (table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.INITIALISING || table.getValueAt(selectedrow, NUTableModel.STATUS) == UploadStatus.UPLOADING) {
                            NUTableModel.uploadList.get(selectedrow).stopUpload();
                            NULogger.getLogger().log(Level.INFO, "Stopped upload : {0}", selectedrow);
                        }
                    }
                }
            }.start();
        }
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the clipboard contents.
     */
    public void setClipboardContent(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        clipboard.setContents(stringSelection, this);
        NULogger.getLogger().info("Copied to clipboard");
    }
}
