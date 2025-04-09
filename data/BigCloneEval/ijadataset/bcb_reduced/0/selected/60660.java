package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingWorker;
import com.enterprisedt.net.ftp.FTPException;
import comm.FTPCommLink;

public class ArchiveManager extends SwingWorker<Void, Void> {

    private CommentedProperties properties;

    private MessageHandler msg;

    private ActionListener actionListener;

    private FTPCommLink ftp;

    private String localDirectory;

    private String remoteDirectory;

    public ArchiveManager(MessageHandler msg, FTPCommLink ftp, CommentedProperties properties, ActionListener actionListener) {
        this.msg = msg;
        this.properties = properties;
        this.actionListener = actionListener;
        this.ftp = ftp;
    }

    public void archiveLocalFolder(String localDirectory, String remoteDirectory) throws FTPException, IOException {
        this.localDirectory = localDirectory;
        this.remoteDirectory = remoteDirectory;
        msg.addMsg("Attempting to connect to " + properties.getProperty("FTP_HOST") + " ...");
        if (ftp.connect(properties.getProperty("FTP_HOST"), properties.getProperty("FTP_USER"), properties.getProperty("FTP_PASSWORD"))) {
            msg.addMsg("Connected.");
            execute();
        } else {
            done();
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            uploadArchive(createArchive());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public void done() {
        try {
            ftp.disconnect();
            msg.addMsg("Disconnected.");
        } catch (FTPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ActionCommand.CMD_ARCHIVE_END.toString()));
    }

    private File createArchive() throws IOException {
        DateFormat dateFormat;
        File inFolder, outFolder;
        ZipOutputStream out;
        BufferedInputStream in;
        byte[] data;
        String files[];
        String archiveName;
        setProgress(0);
        inFolder = new File(localDirectory);
        outFolder = null;
        if (inFolder.exists()) {
            dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            archiveName = inFolder.getName() + "(" + dateFormat.format(new Date()) + ").zip";
            msg.addMsg("Creating archive: " + archiveName + " ...");
            outFolder = new File(localDirectory + "\\" + archiveName);
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFolder)));
            data = new byte[1024];
            files = inFolder.list(new FileExtensionFilter(properties.getProperty("ACCEPTED_EXTENSIONS").toLowerCase().split(",")));
            for (int i = 0; i < files.length; i++) {
                in = new BufferedInputStream(new FileInputStream(inFolder.getPath() + "//" + files[i]), 1024);
                out.putNextEntry(new ZipEntry(files[i]));
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                }
                out.closeEntry();
                setProgress(Math.min((i * 100) / files.length, 100));
            }
            out.flush();
            out.close();
            msg.addMsg("Archive created.");
        }
        return outFolder;
    }

    private void uploadArchive(File archiveFile) throws FTPException, IOException {
        if (archiveFile != null) {
            changeToRemoteDirectory();
            msg.addMsg("Uploading archive...");
            ftp.uploadFile(archiveFile.getAbsolutePath(), archiveFile.getName());
            setProgress(100);
            msg.addMsg("Archive uploaded.");
        }
    }

    private void changeToRemoteDirectory() throws FTPException, IOException {
        msg.addMsg("Changing to remote directory: " + remoteDirectory);
        ftp.changeDirectory(remoteDirectory);
    }
}
