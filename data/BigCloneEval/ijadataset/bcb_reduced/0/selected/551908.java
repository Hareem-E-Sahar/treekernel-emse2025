package org.sourceseed.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sourceseed.tools.backup.backThatThingUp.Destination;
import org.sourceseed.tools.backup.backThatThingUp.EmailNotification;
import org.sourceseed.tools.backup.backThatThingUp.Project;
import org.sourceseed.tools.backup.tasks.EmailTrigger;

/**
 * Utility program which provides simple methods for manipulating FileObjects
 * 
 * @author Dean Del Ponte
 */
public class VfsUtils {

    private static Logger logger = Logger.getLogger(VfsUtils.class);

    public VfsUtils() {
        PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    }

    /**
     * 
     * @param fileObject
     * @return Size of FileObject in bytes
     */
    public long getSize(FileObject fileObject) {
        long dirSize = 0;
        try {
            FileObject[] fileObjects = fileObject.findFiles(new AllFileSelector());
            for (FileObject fo : fileObjects) {
                if (fo.getType() != FileType.FOLDER) {
                    dirSize = dirSize + fo.getContent().getSize();
                }
            }
        } catch (FileSystemException ex) {
            logger.error("Error determining directory size", ex);
        }
        return dirSize;
    }

    /**
     * 
     * @param fileObjects
     */
    public void deleteFileObjects(Collection<FileObject> fileObjects) {
        for (FileObject fileObject : fileObjects) {
            deleteFileObject(fileObject);
        }
    }

    /**
     * 
     * @param fileObject
     */
    public void deleteFileObject(FileObject fileObject) {
        try {
            fileObject.delete(new AllFileSelector());
        } catch (FileSystemException ex) {
            logger.error("Error deleting file " + fileObject.getName(), ex);
        }
    }

    /**
     * 
     * @param file The file to copy
     * @param destinations Locations to which the file will be copied
     * @param newFileName The new name for the copied file
     */
    public void copyFile(File file, List<Destination> destinations, String newFileName) {
        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject zipFileObject = fsManager.toFileObject(file);
            for (Destination destination : destinations) {
                if (!destination.getPath().endsWith("/")) {
                    destination.setPath(destination.getPath() + "/");
                }
                Destination backupFileDest = new Destination(destination.getPath() + newFileName, destination.getFtpMode(), destination.getUsername(), destination.getPassword());
                FileObject destFileObject = backupFileDest.getFileObject();
                destFileObject.copyFrom(zipFileObject, new AllFileSelector());
            }
        } catch (FileSystemException ex) {
            logger.error("Error copying file " + file.getName(), ex);
        }
    }

    /**
     * 
     * @param file The file to be transformed into a FileObject
     * @return FileObject
     */
    public FileObject toFileObject(File file) {
        FileObject fileObject = null;
        try {
            FileSystemManager fsManager = VFS.getManager();
            fileObject = fsManager.toFileObject(file);
        } catch (FileSystemException ex) {
            java.util.logging.Logger.getLogger(VfsUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileObject;
    }

    /**
     * 
     * @param filesToZip Collection of absolute paths to files which need backing up
     * @param project The project as defined in the config file
     * @param backupFileName The name of the backup (zip) file
     */
    public void createZips(Collection<String> filesToZip, Project project, String backupFileName) {
        try {
            for (Destination destination : project.getDestinations()) {
                String backupFilePath = destination.getPath();
                if (!backupFilePath.endsWith(File.separator) && !backupFilePath.endsWith("/")) {
                    backupFilePath = backupFilePath + "/";
                }
                backupFilePath = backupFilePath + backupFileName;
                Destination backupDestination = new Destination(destination);
                backupDestination.setPath(backupFilePath);
                OutputStream outputStream = backupDestination.getFileObject().getContent().getOutputStream();
                byte[] buffer = new byte[18024];
                ZipOutputStream out = new ZipOutputStream(outputStream);
                out.setLevel(Deflater.DEFAULT_COMPRESSION);
                for (String filePath : filesToZip) {
                    FileInputStream in = new FileInputStream(filePath);
                    try {
                        out.putNextEntry(new ZipEntry(filePath));
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    } catch (ZipException ze) {
                        ze.printStackTrace();
                        logger.error(ze);
                    }
                    out.closeEntry();
                    in.close();
                }
                out.close();
                sendEmail(project, EmailTrigger.SUCCESS, project.getName() + " backup successful");
            }
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            sendEmail(project, EmailTrigger.FAILURE, project.getName() + " backup failed\n" + iae.getMessage());
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            sendEmail(project, EmailTrigger.FAILURE, project.getName() + " backup failed\n" + fnfe.getMessage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            sendEmail(project, EmailTrigger.FAILURE, project.getName() + " backup failed\n" + ioe.getMessage());
        }
    }

    /**
     * Send email if appropriate
     * @param project Project
     * @param trigger EmailTrigger - defines when to send emails
     * @param msg The message to be included in the email
     */
    public void sendEmail(Project project, EmailTrigger trigger, String msg) {
        if (project.getMailServer() != null && project.getEmailNotifications() != null) {
            for (EmailNotification emailNotification : project.getEmailNotifications()) {
                if (emailNotification.getEmailTrigger().getMessage().equals(EmailTrigger.ALL.getMessage()) && emailNotification.isValid()) {
                    sendEmail(project, msg, trigger, emailNotification);
                } else if (emailNotification.getEmailTrigger().equals(trigger) && emailNotification.isValid()) {
                    sendEmail(project, msg, trigger, emailNotification);
                }
            }
        }
    }

    private void sendEmail(Project project, String msg, EmailTrigger trigger, EmailNotification emailNotification) {
        for (String emailAddress : emailNotification.getEmailAddresses()) {
            try {
                SimpleEmail email = new SimpleEmail();
                email.setHostName(project.getMailServer().getHostname());
                email.addTo(emailAddress);
                email.setFrom(emailNotification.getFrom());
                email.setSubject(emailNotification.getSubject() + " - " + trigger.getMessage());
                email.setMsg(emailNotification.getMessage() + "\n\n" + msg);
                email.setAuthentication(project.getMailServer().getUsername(), project.getMailServer().getPassword());
                email.send();
            } catch (EmailException ex) {
                logger.error("Error sending email for project " + project.getName(), ex);
            }
        }
    }
}
