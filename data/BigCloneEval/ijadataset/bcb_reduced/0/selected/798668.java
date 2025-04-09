package de.exilab.pixmgr.dialog.zip;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import de.exilab.pixmgr.dialog.AbstractExportCtrl;
import de.exilab.pixmgr.gui.model.AlbumEntry;
import de.exilab.util.progress.ProgressDialog;
import de.exilab.util.progress.ProgressEvent;
import de.exilab.util.progress.ProgressListener;

/**
 * Controler for the ZIP export dialog
 * @author <a href="andreas@exilab.de">Andreas Heidt</a>
 * @version $Revision: 1.3 $ - $Date: 2004/08/03 15:40:14 $
 */
public class ZipExportDialogCtrl extends AbstractExportCtrl implements Runnable, ProgressListener {

    /**
     * Static logger for this class
     */
    private static Logger log = Logger.getLogger(ZipExportDialogCtrl.class.getName());

    /**
     * Array with all album entries to export
     */
    private AlbumEntry[] m_albumEntry;

    /**
     * Reference to the dialog
     */
    private ZipExportDialog m_dialog;

    /**
     * Thread for the export process
     */
    private Thread m_thread;

    /**
     * The progress dialog
     */
    private ProgressDialog m_dialogProgress;

    /**
     * Reference to the parent frame
     */
    private Frame m_parentFrame;

    /**
     * Constructor of the class <code>ZipExportDialogCtrl</code>
     * @param parent Reference to the parent frame
     * @param entries Array with the entries of the album
     */
    public ZipExportDialogCtrl(Frame parent, AlbumEntry[] entries) {
        m_albumEntry = entries;
        m_parentFrame = parent;
        m_dialog = new ZipExportDialog(parent);
    }

    /**
     * Exports the album as a ZIP file     
     */
    public void export() {
        m_dialog.setVisible(true);
        if (m_dialog.isCancelled()) {
            return;
        }
        m_dialogProgress = new ProgressDialog(m_parentFrame, "Exporting To ZIP File");
        m_dialogProgress.addProgressListener(this);
        m_dialogProgress.setLocation(m_parentFrame.getLocation().x + ((m_parentFrame.getSize().width - m_dialogProgress.getSize().width) / 2), m_parentFrame.getLocation().y + ((m_parentFrame.getSize().height - m_dialogProgress.getSize().height) / 2));
        m_thread = new Thread(this);
        m_thread.start();
        m_dialogProgress.setVisible(true);
    }

    /**
     * Performs the export     
     */
    public void run() {
        boolean bScale = m_dialog.getPanelExport().getCheckScale().isSelected();
        Dimension scaleSize = null;
        if (bScale) {
            scaleSize = getScaleSize(m_dialog.getPanelExport());
            if (scaleSize == null) {
                m_dialogProgress.setVisible(false);
                m_thread = null;
                return;
            }
        }
        File zipFile = getTargetFile(m_dialog.getPanelFile());
        if (zipFile == null) {
            m_dialogProgress.setVisible(false);
            m_thread = null;
            return;
        }
        String strTempPath = zipFile.getAbsolutePath().substring(0, zipFile.getAbsolutePath().lastIndexOf(File.separator));
        File tempPath = new File(strTempPath + File.separator + String.valueOf(System.currentTimeMillis()));
        if (!tempPath.mkdir()) {
            JOptionPane.showMessageDialog(m_dialog, "Failed to create temporary directory: \n'" + strTempPath + "'", "Error", JOptionPane.ERROR_MESSAGE);
            m_dialogProgress.setVisible(false);
            m_thread = null;
            return;
        }
        try {
            m_dialogProgress.setCurrentAction("Exporting files to temporary dir..", m_albumEntry.length);
            for (int i = 0; i < m_albumEntry.length; i++) {
                m_dialogProgress.click();
                copyImage(m_dialog, m_albumEntry[i].getFilename(), (bScale) ? scaleSize : null, tempPath);
                if (m_thread == null) {
                    m_dialogProgress.setVisible(false);
                    m_thread = null;
                    return;
                }
            }
            m_dialogProgress.setCurrentAction("Compressing files..", 2);
            m_dialogProgress.click();
            zipImages(zipFile, tempPath);
            m_dialogProgress.click();
            tempPath.delete();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(m_dialog, "An error has occured during the export\n" + "(See logfile for details)", "Error", JOptionPane.ERROR_MESSAGE);
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "Failed to export images:\n" + e.getMessage());
            }
            return;
        } finally {
            m_dialogProgress.setVisible(false);
            m_thread = null;
        }
    }

    /**
     * Creates a ZIP file with all files in a given source directory
     * @param zipFile The ZIP file to create
     * @param srcDir The source directory which files will be included
     * in the ZIP file
     * @throws IOException If an error has occured
     */
    private void zipImages(File zipFile, File srcDir) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            ZipEntry entry = new ZipEntry(files[i].getName());
            out.putNextEntry(entry);
            FileInputStream in = new FileInputStream(files[i]);
            int bytesRead = -1;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            files[i].delete();
        }
        out.close();
    }

    public void processCancelled(ProgressEvent event) {
        m_dialog.setVisible(false);
        m_thread = null;
        return;
    }
}
