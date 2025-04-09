package com.yellowninja.backup.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BackupTask extends Thread {

    private Logger logger;

    private final PropertyChangeSupport pcs;

    private String currentFile;

    private boolean stop, done;

    private long totalBytes;

    private byte[] buf;

    private Document doc;

    private Element root;

    private Backup backup;

    private ZipOutputStream out;

    private int archiveNumber;

    private SimpleDateFormat fileFormat, dateFormat;

    /**
	 * Set up the backup and enable logging.
	 * 
	 * @param backup
	 *            The Backup to run.
	 */
    public BackupTask(Backup backup) {
        this.backup = backup;
        this.pcs = new PropertyChangeSupport(this);
        buf = new byte[1024];
        currentFile = "";
        stop = false;
        done = false;
        archiveNumber = 0;
        logger = Logger.getLogger(this.getClass());
    }

    public void finished() {
        System.out.println("Should never be called.");
    }

    public void run() {
        try {
            if (BackupUtils.checkDirectory(backup.getSavePath())) {
                logger.debug("Backup directory created.");
            }
            if (BackupUtils.checkBackupFile(backup.getBackupFilePath())) {
                logger.debug("Backup file created.");
            }
            if (backup.getType() == Backup.COMPLETE) logger.info("Running full backup."); else logger.info("Running incremental backup.");
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(new File(backup.getBackupFilePath()));
            root = new Element("backup");
            backup.setLastRunDate(Calendar.getInstance().getTime());
            dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
            fileFormat = new SimpleDateFormat("MM.dd.yyyy.HH.mm.ss");
            root.setAttribute("date", dateFormat.format(backup.getLastRunDate()));
            traverse(new File(backup.getBackupLocation()));
            doc.getRootElement().addContent(root);
            if (!stop) {
                XMLOutputter serializer = new XMLOutputter();
                Format format = serializer.getFormat();
                format.setIndent("\t");
                format.setLineSeparator("\n");
                format.setTextMode(Format.TextMode.NORMALIZE);
                serializer.setFormat(format);
                serializer.output(doc, new FileWriter(backup.getBackupFilePath()));
                if (out != null) out.close();
            }
            setDone(true);
            logger.debug("Done with backup: " + backup.getName());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        finished();
    }

    private void traverse(File dir) {
        if (!stop) {
            if (dir.isDirectory()) {
                Element node = new Element("dir");
                node.setAttribute("path", dir.getAbsolutePath());
                root.addContent(node);
                File[] children = dir.listFiles();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) traverse(children[i]);
                } else {
                    logger.debug("Directory is empty: " + dir);
                }
            } else {
                try {
                    process(dir);
                } catch (IOException ioe) {
                    logger.error(ioe.getLocalizedMessage(), ioe);
                    logger.error("FILE NOT BACKED UP: " + dir.getAbsolutePath());
                }
            }
            setCurrentFile(dir.getName());
        }
    }

    private void process(File file) throws IOException {
        if ((int) ((float) file.length() / (float) 1048576) <= 2000) {
            backupFile(file);
            int mb = (int) ((float) totalBytes / (float) 1048576);
            if (mb > 2000) {
                logger.debug("Archive is too big, segmenting.");
                completeFile();
            }
        } else {
            logger.debug("File is too big, just transfering.");
            if (totalBytes > 0) {
                logger.debug("Archive is too big, segmenting.");
                completeFile();
            }
            backupFile(file);
            completeFile();
        }
    }

    private void completeFile() throws IOException {
        out.close();
        archiveNumber++;
        totalBytes = 0;
        out = new ZipOutputStream(new FileOutputStream(backup.getSavePath() + File.separator + fileFormat.format(backup.getLastRunDate()) + ".a" + archiveNumber + ".zip"));
    }

    private void backupFile(File file) {
        CheckedInputStream cis;
        try {
            cis = new CheckedInputStream(new FileInputStream(file), new Adler32());
            byte[] tempBuf = new byte[128];
            while (cis.read(tempBuf) >= 0) {
            }
            long checksum = cis.getChecksum().getValue();
            cis.close();
            if (backup.getType() == Backup.INCREMENTAL) {
                if (file.isDirectory()) zipFile(file); else if (hasFileChanged(file.getAbsolutePath(), checksum)) zipFile(file);
            } else {
                zipFile(file);
            }
            Element node = new Element("file");
            node.setAttribute("path", file.getAbsolutePath());
            node.setAttribute("checksum", String.valueOf(checksum));
            root.addContent(node);
            logger.debug("File Added: " + file.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            logger.error("FILE NOT BACKED UP: " + file.getAbsolutePath());
        }
    }

    private void zipFile(File file) throws IOException, FileNotFoundException {
        if (out == null) {
            out = new ZipOutputStream(new FileOutputStream(backup.getSavePath() + File.separator + fileFormat.format(backup.getLastRunDate()) + ".a" + archiveNumber + ".zip"));
        }
        FileInputStream in = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(getRelativeFilePath(file.getAbsolutePath()));
        out.putNextEntry(entry);
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        totalBytes += entry.getCompressedSize();
        in.close();
    }

    private String getRelativeFilePath(String path) {
        return path.substring(backup.getBackupLocation().length() + 1);
    }

    @SuppressWarnings("unchecked")
    private boolean hasFileChanged(String path, long checksum) throws ParseException {
        List<Element> backups = doc.getRootElement().getChildren();
        Iterator backupIt = backups.iterator();
        Calendar date = null;
        Element backup = null;
        while (backupIt.hasNext()) {
            Object child = backupIt.next();
            if (child instanceof Element) {
                if (date != null) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.setTime((Date) dateFormat.parse(((Element) child).getAttribute("date").getValue()));
                    if (newDate.after(date)) {
                        date = newDate;
                        backup = (Element) child;
                    }
                } else {
                    date = Calendar.getInstance();
                    date.setTime((Date) dateFormat.parse(((Element) child).getAttribute("date").getValue()));
                    backup = (Element) child;
                }
            }
        }
        if (backup != null) {
            Iterator iterator = backup.getChildren().iterator();
            while (iterator.hasNext()) {
                Object child = iterator.next();
                if (child instanceof Element && ((Element) child).getName().equals("file") && ((Element) child).getAttribute("path").getValue().equals(path)) {
                    if (((Element) child).getAttribute("checksum").getValue().equals("" + checksum)) {
                        logger.debug("File not changed.");
                        return false;
                    } else {
                        logger.debug("File changed.");
                        return true;
                    }
                }
            }
        }
        logger.debug("File not found.");
        return true;
    }

    /**
	 * Stops the thread from completing.
	 */
    public void stopTask() {
        this.stop = true;
    }

    public Backup getBackup() {
        return backup;
    }

    public boolean getDone() {
        return done;
    }

    public void setDone(boolean done) {
        boolean old = this.done;
        this.done = done;
        this.pcs.firePropertyChange("done", old, done);
    }

    /**
	 * @return the currentFile
	 */
    public String getCurrentFile() {
        return currentFile;
    }

    /**
	 * @param currentFile
	 *            the currentFile to set
	 */
    public void setCurrentFile(String currentFile) {
        String old = this.currentFile;
        this.currentFile = currentFile;
        this.pcs.firePropertyChange("currentFile", old, currentFile);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
}
