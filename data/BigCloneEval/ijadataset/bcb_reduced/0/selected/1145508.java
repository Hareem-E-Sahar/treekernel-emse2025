package ro.wpcs.traser.client.storage.impl;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import ro.wpcs.traser.client.ApplicationConstants;
import ro.wpcs.traser.client.storage.AccessDeniedException;
import ro.wpcs.traser.client.storage.Credentials;
import ro.wpcs.traser.client.storage.RemoteStorage;
import ro.wpcs.traser.client.storage.UnsupportedRemoteStorageException;
import ro.wpcs.traser.client.storage.crypto.FileDecryptor;
import ro.wpcs.traser.client.storage.crypto.FileEncryptor;
import ro.wpcs.traser.controllers.CheckOutDataManager;
import ro.wpcs.traser.model.CheckOutKey;
import ro.wpcs.traser.model.File;
import ro.wpcs.traser.model.Node;
import ro.wpcs.traser.model.Project;
import ro.wpcs.traser.model.Version;

/**
 * Class used to wrap FTP operations
 * 
 * @author Tomita Militaru, Alina Hila
 * @date Nov 5, 2008
 */
public class FTPAdapter {

    /** Class logger. */
    private static final Logger logger = Logger.getLogger(FTPAdapter.class);

    private static RemoteStorage rms = RemoteStorage.getInstance();

    private static final String DEFAULT_FTP_PROTOCOL = "ftp://localhost/";

    /** Login data for the FS */
    private Credentials credentials;

    /** Configuration file */
    private Properties prop;

    public FTPAdapter() {
        prop = ApplicationConstants.getConfiguration();
        credentials = new Credentials(prop.getProperty("username"), prop.getProperty("password"));
    }

    public static boolean validateConnection() {
        try {
            return rms.checkConnection(DEFAULT_FTP_PROTOCOL);
        } catch (UnsupportedRemoteStorageException e) {
            logger.info(e.getMessage());
            return false;
        }
    }

    public static boolean validateConnection(String host, Credentials credentials) {
        try {
            return rms.checkConnection(host, credentials);
        } catch (UnsupportedRemoteStorageException e) {
            logger.info(e.getMessage());
            return false;
        }
    }

    /**
	 * Creates a new File on FTP
	 * 
	 * @param theNewFile
	 *            the new file
	 * @param browsePath
	 */
    public boolean newFile(File theNewFile, String browsePath) {
        InputStream input = null;
        boolean succes = false;
        try {
            input = new FileInputStream(browsePath);
            if (theNewFile.getEncryptionKey() != null) input = FileEncryptor.getInstance().getEncryptionInputStream(theNewFile, input);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        try {
            rms.saveDocument(credentials, ((Project) (theNewFile.getParent())).getStorage() + "/" + ((Project) (theNewFile.getParent())).getNodeText() + "/" + theNewFile.getRemoteFSFilename(), input);
            succes = true;
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return succes;
    }

    /**
	 * Opens a file
	 * 
	 * @param file
	 *            the file to be opened
	 * @return
	 */
    public boolean open(File file) {
        java.io.File temp = newEmptyTempFile(file);
        boolean succes = checkout(file, temp.getPath());
        openWithSpecificApp(temp);
        return succes;
    }

    /**
	 * Opens a version
	 * 
	 * @param version
	 *            the version file to be opened
	 */
    public void open(Version version) {
        java.io.File temp = newEmptyTempFile(version);
        try {
            OutputStream output = new FileOutputStream(temp);
            if (version.getEncryptionKey() != null) {
                output = FileDecryptor.getInstance().getDecryptionOutputStream(version, output);
            }
            rms.loadDocument(credentials, ((Project) (version.getParent().getParent())).getStorage() + "/" + ((Project) (version.getParent().getParent())).getNodeText() + "/" + version.getRemoteFSFilename(), output);
            output.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        } catch (ro.wpcs.traser.client.storage.FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        openWithSpecificApp(temp);
    }

    /**
	 * Creates a temporary read only file at a location described in the
	 * properties file
	 * 
	 * @param node
	 *            the node in the tree to be opened
	 * @return the local path where the temporary file will be created
	 */
    public java.io.File newEmptyTempFile(Node node) {
        java.io.File temp = null;
        String localPath = System.getProperty("user.dir") + "\\" + prop.getProperty("temp") + "\\" + node.getNodeText();
        logger.info("Save to temporary folder: " + localPath);
        java.io.File test = new java.io.File(localPath);
        if (test.exists()) {
            boolean succes = test.delete();
            logger.info("Have deleted the existing temporary file(T/F): " + succes);
        }
        temp = new java.io.File(localPath);
        temp.setReadOnly();
        temp.deleteOnExit();
        return temp;
    }

    /**
	 * Pops up Windows 'open with' dialog
	 * 
	 * @param temp
	 *            the file to be opened
	 */
    public static void openWithSpecificApp(java.io.File temp) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                if (!temp.exists()) logger.error("There was an error downloading as temporary file"); else {
                    desktop.open(temp);
                }
            } catch (IOException e1) {
                if (e1.getMessage().contains("The parameter is incorrect")) {
                    try {
                        Runtime.getRuntime().exec("rundll32 shell32,OpenAs_RunDLL " + temp);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                    logger.error(e1.getMessage());
                }
            }
        }
    }

    /**
	 * Refreshes the local copy of a file
	 * 
	 * @param file
	 *            the file to be refreshed
	 */
    public boolean refreshLocalFile(File file) {
        OutputStream output;
        boolean succes = false;
        String remoteFile = ((Project) (file.getParent())).getStorage() + "/" + ((Project) (file.getParent())).getName() + "/" + file.getRemoteFSFilename();
        try {
            output = new FileOutputStream(file.getLocalPath());
            if (file.getEncryptionKey() != null) output = FileDecryptor.getInstance().getDecryptionOutputStream(file, output);
            rms.loadDocument(credentials, remoteFile, output);
            output.close();
            succes = true;
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        } catch (ro.wpcs.traser.client.storage.FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return succes;
    }

    /**
	 * Checks out a file at the specified destination
	 * 
	 * @param file
	 *            the file to be checked out
	 * @param destination
	 *            path where to be checked out on local machine
	 */
    public boolean checkout(File file, String destination) {
        boolean succes = false;
        try {
            OutputStream output = new FileOutputStream(destination);
            String remotePath = ((Project) (file.getParent())).getStorage() + "/" + ((Project) (file.getParent())).getNodeText() + "/" + file.getRemoteFSFilename();
            if (file.getEncryptionKey() != null) {
                output = FileDecryptor.getInstance().getDecryptionOutputStream(file, output);
            }
            rms.loadDocument(credentials, remotePath, output);
            logger.info("The remote path is " + remotePath);
            output.close();
            succes = true;
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        } catch (ro.wpcs.traser.client.storage.FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return succes;
    }

    /**
	 * Checks in a file on FTP. Creates a version on FTP
	 * 
	 * @param newVersion
	 *            the new version
	 */
    public boolean createVersion(Version newVersion) {
        boolean succes = false;
        String remoteFile = ((Project) (((File) (newVersion.getParent())).getParent())).getStorage() + "/" + ((Project) (((File) (newVersion.getParent())).getParent())).getNodeText() + "/" + ((File) (newVersion.getParent())).getRemoteFSFilename();
        String renamedRemoteFile = ((Project) (((File) (newVersion.getParent())).getParent())).getStorage() + "/" + ((Project) (((File) (newVersion.getParent())).getParent())).getNodeText() + "/" + newVersion.getRemoteFSFilename();
        logger.info("Remote file: " + remoteFile);
        logger.info("Renamed remote file:" + renamedRemoteFile);
        InputStream input;
        try {
            CheckOutDataManager obj = CheckOutDataManager.getInstance();
            CheckOutKey key = CheckOutDataManager.getInstance().contructKeyForFile((File) newVersion.getParent());
            String localPath = (String) obj.getLocalPath(key);
            if (localPath != null) {
                input = new FileInputStream(localPath);
                logger.info("localPath from createVersion: " + localPath);
                rms.renameDocument(credentials, remoteFile, renamedRemoteFile);
                if (((File) newVersion.getParent()).getEncryptionKey() != null) input = FileEncryptor.getInstance().getEncryptionInputStream((File) newVersion.getParent(), input);
                rms.saveDocument(credentials, remoteFile, input);
                input.close();
                java.io.File deleteFile = new java.io.File(localPath);
                deleteFile.delete();
                succes = true;
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return succes;
    }

    /**
	 * Removes the project on FTP
	 * 
	 * @param newVersion
	 *            the version to be removed
	 */
    public boolean removeProject(Project prj) {
        boolean succes = false;
        String remoteFolder = prj.getStorage() + "/" + prj.getNodeText();
        logger.info("Will delete this folder: " + remoteFolder);
        try {
            rms.deleteFolder(credentials, remoteFolder);
            succes = true;
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
        } catch (SocketException e) {
            logger.error(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        }
        return succes;
    }

    /**
	 * Removes the file on FTP
	 * 
	 * @param selectedFile
	 *            the file to be removed
	 */
    public boolean removeFile(File selectedFile) {
        boolean succes = false;
        List versions = selectedFile.getChildren();
        for (int i = 0; i < versions.size(); i++) {
            removeVersion((Version) (versions.get(i)));
        }
        logger.info("Deleting " + ((Project) (selectedFile.getParent())).getStorage() + "/" + ((Project) (selectedFile.getParent())).getNodeText() + "/" + selectedFile.getRemoteFSFilename());
        try {
            rms.deleteDocument(credentials, ((Project) (selectedFile.getParent())).getStorage() + "/" + ((Project) (selectedFile.getParent())).getNodeText() + "/" + selectedFile.getRemoteFSFilename());
            succes = true;
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return succes;
    }

    /**
	 * Removes the version on FTP
	 * 
	 * @param selectedVersion
	 *            the version to be removed
	 */
    public boolean removeVersion(Version selectedVersion) {
        boolean succes = false;
        try {
            rms.deleteDocument(credentials, ((Project) ((File) (selectedVersion.getParent())).getParent()).getStorage() + "/" + ((Project) ((File) (selectedVersion.getParent())).getParent()).getNodeText() + "/" + selectedVersion.getRemoteFSFilename());
            succes = true;
        } catch (AccessDeniedException e) {
            logger.error(e.getMessage());
        } catch (UnsupportedRemoteStorageException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return succes;
    }
}
