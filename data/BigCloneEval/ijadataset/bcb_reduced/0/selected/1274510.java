package visitpc.distjar;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.*;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import service_manager.PlatformHandler;
import visitpc.destclient.DestClient;
import visitpc.ClientConfig;
import visitpc.destclient.VNCDestClient;
import visitpc.destclient.VNCDestClientConfig;
import visitpc.destclient.gui.GenericDestConfigDialog;
import visitpc.destclient.gui.VNCDestConfigDialog;
import visitpc.destclient.GenericDestClientConfig;
import visitpc.launcher.LauncherFrame;
import visitpc.lib.gui.GenericConfigDialog;
import visitpc.lib.gui.LongNumberField;
import visitpc.lib.gui.UI;
import visitpc.lib.io.FileIO;
import visitpc.lib.io.IncorrectKeyException;
import visitpc.lib.io.SimpleConfigHelper;
import visitpc.lib.io.SimpleConfigList;
import visitpc.VisitPCException;
import visitpc.filetransfer.dest.*;
import visitpc.srcclient.*;
import visitpc.srcclient.gui.VNCSrcClientDialog;
import visitpc.server.*;
import visitpc.webserver.*;
import visitpc.VisitPCConstants;

public class DistJarFactory implements ChangeListener {

    public enum JAR_FILE_TYPES {

        VNC_DEST_CLIENT, FILE_TRANSFER_DEST_CLIENT, PORT_FORWARD_DEST_CLIENT, SERVER, PORT_FORWARD_SOURCE_CLIENT, WEB_SERVER
    }

    public static final String JAR_SIGNER_PROGRAM_FILE_CONFIG = "visitpc.distjar.jarsigner_file";

    public static final String SMALL_LOGO_IMAGE_FILENAME = "small_logo.png";

    public static final String DIST_JAR__DIALOG_CONFIG_FILENAME = "visitpc.createDistJarDialog";

    private GenericConfigDialog createDistJarDialog;

    private LauncherFrame launcherFrame;

    private int checkServerReachableProgressCount;

    private ProgressMonitor checkServerReachableProgressMonitor;

    private Socket checkServerReachableSocket = null;

    /**
	 * Create JAR files that the user may run to start a
	 * 
	 * VNC Dest client, File Transfer Dest Client, Port Forward Dest Client
	 * or a VNC/File Transfer Src Client
	 * 
	 * Separate Jar files are created for each client. Each client must be
	 * configured before createDistributableJarFiles is called. The Jar file
	 * will include (embedded within it) the customised configuration required
	 * to connect to the VisitPC server.
	 * 
	 */
    public void createDistributableJarFiles(LauncherFrame launcherFrame) {
        this.launcherFrame = launcherFrame;
        DistJarConfig distJarConfig = new DistJarConfig();
        launcherFrame.enableInput(false);
        try {
            distJarConfig.defaultEncryptKey = DestClient.GetDefaultEncryptKey();
            distJarConfig.distJarDialogConfig = getDistJarConfig();
            if (distJarConfig.distJarDialogConfig == null) {
                launcherFrame.enableInput(true);
                return;
            }
            distJarConfig.tmpDir = new File(distJarConfig.distJarDialogConfig.outputPath, "tmp");
            distJarConfig.srvMgrDir = new File(distJarConfig.tmpDir, "service_manager");
            distJarConfig.vncServerDir = new File(distJarConfig.tmpDir, "visitpc" + System.getProperty("file.separator") + "destclient" + System.getProperty("file.separator") + "vncserver");
            distJarConfig.configDir = new File(distJarConfig.tmpDir, SimpleConfigHelper.JAR_CONFIG_PATH);
            distJarConfig.visitPCJarFile = JarIt.GetVisitPCJarFile();
            if (distJarConfig.distJarDialogConfig.jarTypeList.equals(DistJarDialogConfig.VNC_DEST_CLIENT)) {
                VNCDestClientConfig vncDestClientConfig = getVNCDestClientConfig(distJarConfig.defaultEncryptKey);
                if (vncDestClientConfig == null) {
                    launcherFrame.enableInput(true);
                    return;
                }
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.VNC_DEST_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = vncDestClientConfig.serverName;
                distJarConfig.serverPort = vncDestClientConfig.serverPort;
                distJarConfig.trustStoreFile = vncDestClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.VNC_DEST_CLIENT_CONFIG_FILE;
                distJarConfig.startupClass = "visitpc.destclient.VNCDestClient";
                distJarConfig.programName = "VisitPC Destination VNC Client";
            } else if (distJarConfig.distJarDialogConfig.jarTypeList.equals(DistJarDialogConfig.FILE_DEST_CLIENT)) {
                FileTransferClientConfig fileTransferClientConfig = getFileTransferDestClientConfig(distJarConfig.defaultEncryptKey);
                if (fileTransferClientConfig == null) {
                    launcherFrame.enableInput(true);
                    return;
                }
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.FILE_TRANSFER_DEST_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = fileTransferClientConfig.serverName;
                distJarConfig.serverPort = fileTransferClientConfig.serverPort;
                distJarConfig.trustStoreFile = fileTransferClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.FILE_TRANSFER_CONFIG_FILE;
                distJarConfig.startupClass = "visitpc.filetransfer.dest.FileTransferClient";
                distJarConfig.programName = "VisitPC Destination File Transfer Client";
            } else if (distJarConfig.distJarDialogConfig.jarTypeList.equals(DistJarDialogConfig.PORT_FORWARD_DEST_CLIENT)) {
                GenericDestClientConfig genericDestClientConfig = getGenericDestClientConfig(distJarConfig.defaultEncryptKey);
                if (genericDestClientConfig == null) {
                    launcherFrame.enableInput(true);
                    return;
                }
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.PORT_FORWARD_DEST_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = genericDestClientConfig.serverName;
                distJarConfig.serverPort = genericDestClientConfig.serverPort;
                distJarConfig.trustStoreFile = genericDestClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.DEST_CLIENT_CONFIG_FILE;
                distJarConfig.startupClass = "visitpc.destclient.GenericDestClient";
                distJarConfig.programName = "VisitPC Destination Port Forward Client";
            } else if (distJarConfig.distJarDialogConfig.jarTypeList.equals(DistJarDialogConfig.VNC_FILE_TRANSFER_SRC_CLIENT)) {
                VNCSrcClientConfig vncSrcClientConfig = getVNCSrcClientConfig(distJarConfig.defaultEncryptKey);
                if (vncSrcClientConfig == null) {
                    launcherFrame.enableInput(true);
                    return;
                }
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.VNC_FILE_TRANSFER_SRC_CLIENT_FILENAME);
                distJarConfig.serverName = vncSrcClientConfig.serverName;
                distJarConfig.serverPort = vncSrcClientConfig.serverPort;
                distJarConfig.trustStoreFile = vncSrcClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.VNC_SRC_CLIENT_CONFIG_FILE;
                distJarConfig.startupClass = "visitpc.srcclient.VNCSrcClient";
                distJarConfig.programName = "VisitPC Source VNC Client";
            } else {
                throw new IOException("Unsupported option: " + distJarConfig.distJarDialogConfig.jarTypeList);
            }
            int response = JOptionPane.showConfirmDialog(this.launcherFrame, "The VisitPC web server can be setup to distribute VisitPC jar files.\nThe " + distJarConfig.jarFile + "\n file can be copied to the web root path. Do you wish\nto setup the web server ?", "Setup Web Server", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                distJarConfig.webServerConfig = getWebServerConfig(distJarConfig, true, distJarConfig.defaultEncryptKey);
                if (distJarConfig.webServerConfig == null) {
                    launcherFrame.enableInput(true);
                    return;
                }
            }
            distJarConfig.check(true);
            createDistJar(distJarConfig, distJarConfig.distJarDialogConfig.includeUserCredentials);
            JOptionPane.showMessageDialog(launcherFrame, distJarConfig.visitPCJarFile + " jar file created.", "Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(launcherFrame, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (distJarConfig.tmpDir != null && distJarConfig.tmpDir.isDirectory()) {
                try {
                    removeDirectory(distJarConfig.tmpDir);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (distJarConfig.pm != null) {
            distJarConfig.pm.setProgress(distJarConfig.pm.getMaximum());
        }
        launcherFrame.enableInput(true);
    }

    /**
	 * The startup manager requires a modified version of the visitpc.jar file as installed when VisitPC is installed.
	 * This jar file includes all the user configuration. This is required so that when the jar file is executed using
	 * the startup manager it does not need to be be executed in the context of the current user.
	 */
    public void createStartupManagerJar(File outputPath, DistJarFactory.JAR_FILE_TYPES jarFileType) throws IllegalAccessException, IOException, IncorrectKeyException, NoSuchAlgorithmException, VisitPCException, CertificateException, KeyStoreException, ClassNotFoundException, InstantiationException {
        DistJarConfig distJarConfig = new DistJarConfig();
        try {
            if (!outputPath.isDirectory()) {
                throw new IOException(outputPath.getAbsolutePath() + " directory not found.");
            }
            if (!outputPath.canWrite()) {
                throw new IOException("Cannot write to " + outputPath.getAbsolutePath());
            }
            distJarConfig.defaultEncryptKey = DestClient.GetDefaultEncryptKey();
            distJarConfig.distJarDialogConfig = new DistJarDialogConfig();
            distJarConfig.distJarDialogConfig.allowUserConfigChange = false;
            distJarConfig.distJarDialogConfig.clearPCName = false;
            distJarConfig.distJarDialogConfig.guiMode = false;
            distJarConfig.distJarDialogConfig.outputPath = outputPath.getAbsolutePath();
            distJarConfig.tmpDir = new File(distJarConfig.distJarDialogConfig.outputPath, "tmp");
            distJarConfig.srvMgrDir = new File(distJarConfig.tmpDir, "service_manager");
            distJarConfig.vncServerDir = new File(distJarConfig.tmpDir, "visitpc" + System.getProperty("file.separator") + "destclient" + System.getProperty("file.separator") + "vncserver");
            distJarConfig.configDir = new File(distJarConfig.tmpDir, SimpleConfigHelper.JAR_CONFIG_PATH);
            distJarConfig.visitPCJarFile = JarIt.GetVisitPCJarFile();
            if (jarFileType == DistJarFactory.JAR_FILE_TYPES.VNC_DEST_CLIENT) {
                VNCDestClientConfig vncDestClientConfig = new VNCDestClientConfig();
                vncDestClientConfig.load(VisitPCConstants.VNC_DEST_CLIENT_CONFIG_FILE, true, distJarConfig.defaultEncryptKey);
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.VNC_DEST_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = vncDestClientConfig.serverName;
                distJarConfig.serverPort = vncDestClientConfig.serverPort;
                distJarConfig.trustStoreFile = vncDestClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.VNC_DEST_CLIENT_CONFIG_FILE;
                distJarConfig.startupClass = VisitPCConstants.VNCDestClientStart;
                distJarConfig.programName = "VisitPC Destination VNC Client";
            } else if (jarFileType == DistJarFactory.JAR_FILE_TYPES.FILE_TRANSFER_DEST_CLIENT) {
                FileTransferClientConfig fileTransferClientConfig = new FileTransferClientConfig();
                fileTransferClientConfig.load(VisitPCConstants.FILE_TRANSFER_CONFIG_FILE, true, distJarConfig.defaultEncryptKey);
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.FILE_TRANSFER_DEST_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = fileTransferClientConfig.serverName;
                distJarConfig.serverPort = fileTransferClientConfig.serverPort;
                distJarConfig.trustStoreFile = fileTransferClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.FILE_TRANSFER_CONFIG_FILE;
                distJarConfig.startupClass = VisitPCConstants.FileTransferClientStartup;
                distJarConfig.programName = "VisitPC Destination File Transfer Client";
            } else if (jarFileType == DistJarFactory.JAR_FILE_TYPES.PORT_FORWARD_DEST_CLIENT) {
                GenericDestClientConfig genericDestClientConfig = new GenericDestClientConfig();
                genericDestClientConfig.load(VisitPCConstants.DEST_CLIENT_CONFIG_FILE, true, distJarConfig.defaultEncryptKey);
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.PORT_FORWARD_DEST_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = genericDestClientConfig.serverName;
                distJarConfig.serverPort = genericDestClientConfig.serverPort;
                distJarConfig.trustStoreFile = genericDestClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.DEST_CLIENT_CONFIG_FILE;
                distJarConfig.startupClass = VisitPCConstants.GenericDestClientStartup;
                distJarConfig.programName = "VisitPC Destination Port Forward Client";
            } else if (jarFileType == DistJarFactory.JAR_FILE_TYPES.SERVER) {
                ServerConfig serverConfig = new ServerConfig();
                serverConfig.load(VisitPCConstants.SERVER_CONFIG_FILE, true, distJarConfig.defaultEncryptKey);
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.SERVER_JAR_FILENAME);
                distJarConfig.serverName = "";
                distJarConfig.serverPort = serverConfig.serverPort;
                distJarConfig.trustStoreFile = serverConfig.sslKeyStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.SERVER_CONFIG_FILE;
                distJarConfig.startupClass = VisitPCConstants.ServerStartup;
                distJarConfig.programName = "VisitPC Server";
            } else if (jarFileType == DistJarFactory.JAR_FILE_TYPES.PORT_FORWARD_SOURCE_CLIENT) {
                GenericSrcClientConfig genericSrcClientConfig = new GenericSrcClientConfig();
                genericSrcClientConfig.load(VisitPCConstants.VNC_SRC_CLIENT_CONFIG_FILE, true, distJarConfig.defaultEncryptKey);
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.PORT_FORWARD_SRC_CLIENT_JAR_FILENAME);
                distJarConfig.serverName = genericSrcClientConfig.serverName;
                distJarConfig.serverPort = genericSrcClientConfig.serverPort;
                distJarConfig.trustStoreFile = genericSrcClientConfig.sslTrustStoreFile;
                distJarConfig.embeddedConfigFile = VisitPCConstants.GENERIC_SRC_CLIENT_CONFIG_FILE;
                distJarConfig.startupClass = VisitPCConstants.GenericSourceClientSetup;
                distJarConfig.programName = "VisitPC Port Forwarding Source Client";
            } else if (jarFileType == DistJarFactory.JAR_FILE_TYPES.WEB_SERVER) {
                WebServerConfig webServerConfig = new WebServerConfig();
                webServerConfig.load(VisitPCConstants.WEBSERVER_CONFIG_FILE, true, distJarConfig.defaultEncryptKey);
                distJarConfig.jarFile = new File(distJarConfig.distJarDialogConfig.outputPath, VisitPCConstants.WEB_SERVER_JAR_FIELNAME);
                distJarConfig.serverName = "";
                distJarConfig.serverPort = webServerConfig.port;
                distJarConfig.trustStoreFile = "dummy";
                distJarConfig.embeddedConfigFile = VisitPCConstants.WEBSERVER_CONFIG_FILE;
                distJarConfig.startupClass = VisitPCConstants.WebServerStartup;
                distJarConfig.programName = "VisitPC Web Server";
            }
            distJarConfig.pm = new ProgressMonitor(null, "Creating " + distJarConfig.jarFile.getName() + " file\nPlease wait...", null, 0, 0);
            distJarConfig.check(false);
            createDistJar(distJarConfig, true);
        } finally {
            if (distJarConfig.tmpDir != null && distJarConfig.tmpDir.isDirectory()) {
                try {
                    removeDirectory(distJarConfig.tmpDir);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (distJarConfig.pm != null) {
            distJarConfig.pm.setProgress(distJarConfig.pm.getMaximum());
        }
    }

    /**
	 * Create a distributable jar file (non interactive)
	 */
    public void createDistJar(DistJarConfig distJarConfig, boolean includeUserCredentials) throws IOException, IncorrectKeyException, IllegalAccessException, NoSuchAlgorithmException, KeyStoreException, CertificateException, VisitPCException, ClassNotFoundException, InstantiationException {
        Properties configProperties = new Properties();
        if (distJarConfig.serverName != null && distJarConfig.serverName.trim().length() > 0 && distJarConfig.serverPort > 0) {
            checkServerReachable(distJarConfig.serverName, distJarConfig.serverPort, 30000);
            checkServerNotOnLocalSubnet(distJarConfig.serverName);
        }
        if (!new File(distJarConfig.distJarDialogConfig.outputPath).isDirectory()) {
            throw new IOException(distJarConfig.distJarDialogConfig.outputPath + " jar file output directory not found.");
        }
        File configPropertiesFile = new File(distJarConfig.configDir, SimpleConfigHelper.JAR_CONFIG_FILENAME);
        if (distJarConfig.pm != null) {
            distJarConfig.pm.setMinimum(0);
            distJarConfig.pm.setMaximum(6);
            distJarConfig.pm.setProgress(1);
        }
        if (distJarConfig.tmpDir.isDirectory()) {
            removeDirectory(distJarConfig.tmpDir);
        }
        if (!distJarConfig.tmpDir.mkdir()) {
            throw new IOException("Unable to create the " + distJarConfig.tmpDir + " directory.");
        }
        JarIt jarIt = new JarIt(distJarConfig.visitPCJarFile.getAbsolutePath(), distJarConfig.tmpDir.getAbsolutePath());
        jarIt.decompress();
        if (distJarConfig.pm != null) {
            distJarConfig.pm.setProgress(2);
        }
        removeDirectory(distJarConfig.srvMgrDir);
        if (!distJarConfig.distJarDialogConfig.jarTypeList.equals(DistJarDialogConfig.VNC_DEST_CLIENT)) {
            removeDirectory(distJarConfig.vncServerDir);
        }
        if (!distJarConfig.configDir.mkdir()) {
            throw new IOException("Unable to create the " + distJarConfig.configDir + " directory.");
        }
        addGenericConfig(distJarConfig.distJarDialogConfig, configProperties, distJarConfig.configDir, SimpleConfigHelper.OBFUSRACTION_KEY);
        if (distJarConfig.trustStoreFile != null && distJarConfig.trustStoreFile.trim().length() > 0) {
            String trustStoreFilename = copyTrustStoreFile(distJarConfig.trustStoreFile, distJarConfig.configDir.getAbsolutePath());
            configProperties.put(SimpleConfigHelper.CLIENT_TRUSTSTORE_FILE_KEY, SimpleConfigHelper.JAR_CONFIG_PATH + trustStoreFilename);
        }
        copyEmbeddedConfigFile(distJarConfig.embeddedConfigFile, distJarConfig.configDir, distJarConfig.defaultEncryptKey, SimpleConfigHelper.OBFUSRACTION_KEY, includeUserCredentials, distJarConfig.distJarDialogConfig.clearPCName);
        File manifestDir = new File(distJarConfig.tmpDir, "META-INF");
        File manifestFile = new File(manifestDir, "MANIFEST.MF");
        if (!manifestFile.isFile()) {
            throw new IOException(manifestFile + " file not found.");
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(manifestFile));
        bw.write("Manifest-Version: 1.0" + System.getProperty("line.separator"));
        bw.write("Main-Class: " + distJarConfig.startupClass + System.getProperty("line.separator"));
        bw.close();
        if (distJarConfig.pm != null) {
            distJarConfig.pm.setProgress(3);
        }
        Vector<String> configFileList = new Vector<String>();
        File destFile = new File(distJarConfig.configDir, "." + distJarConfig.embeddedConfigFile);
        configFileList.add(SimpleConfigHelper.JAR_CONFIG_PATH + destFile.getName());
        if (distJarConfig.distJarDialogConfig.logoFile != null && distJarConfig.distJarDialogConfig.logoFile.length() > 0) {
            File lcf = new File(distJarConfig.distJarDialogConfig.logoFile);
            configProperties.put(SimpleConfigHelper.LOGO_IMAGE_FILE_KEY, SimpleConfigHelper.JAR_CONFIG_PATH + lcf.getName());
        }
        if (distJarConfig.distJarDialogConfig.smallLogoFile != null && distJarConfig.distJarDialogConfig.smallLogoFile.length() > 0) {
            File slcf = new File(distJarConfig.distJarDialogConfig.smallLogoFile);
            configProperties.put(SimpleConfigHelper.SMALL_LOGO_IMAGE_FILE_KEY, SimpleConfigHelper.JAR_CONFIG_PATH + slcf.getName());
        }
        bw = new BufferedWriter(new FileWriter(configPropertiesFile));
        int index = 0;
        StringBuffer strBuffer = new StringBuffer();
        for (String configFile : configFileList) {
            if (index == 0) {
                strBuffer.append(configFile);
            } else {
                strBuffer.append("\t" + configFile);
            }
            index++;
        }
        configProperties.put(SimpleConfigHelper.FILES_KEY, strBuffer.toString());
        configProperties.store(bw, distJarConfig.embeddedConfigFile);
        bw.close();
        if (distJarConfig.distJarDialogConfig.jarTypeList.equals(DistJarDialogConfig.VNC_DEST_CLIENT)) {
            if (PlatformHandler.GetOSName().startsWith("Windows")) {
                File srcFile = VNCDestClient.GetVNCServerINI();
                if (!srcFile.isFile()) {
                    throw new IOException(srcFile + " file not found.");
                }
                destFile = new File(distJarConfig.tmpDir.toString() + "\\visitpc\\destclient\\vncserver\\win\\ultravnc.ini");
                if (!destFile.isFile()) {
                    throw new IOException(destFile + " file not found.");
                }
                if (!destFile.delete()) {
                    throw new IOException("Failed to delete " + destFile);
                }
                FileIO.CopyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            }
        }
        try {
            if (distJarConfig.pm != null) {
                distJarConfig.pm.setProgress(4);
            }
            jarIt.createJar(distJarConfig.tmpDir, distJarConfig.jarFile.toString());
            if (distJarConfig.pm != null) {
                distJarConfig.pm.setProgress(5);
            }
            if (distJarConfig.webServerConfig != null) {
                copyToWebServer(distJarConfig);
            }
        } finally {
            if (distJarConfig.pm != null) {
                distJarConfig.pm.setProgress(6);
            }
        }
    }

    /**
	 * Copy to web server
	 * 
	 * @param distJarConfig
	 */
    private void copyToWebServer(DistJarConfig distJarConfig) throws IOException {
        for (String f : visitpc.webserver.Main.WEB_SERVER_FILES) {
            File destFile = new File(distJarConfig.webServerConfig.webPath, f);
            if (!destFile.exists()) {
                File srcFile = new File(distJarConfig.tmpDir.getAbsoluteFile() + System.getProperty("file.separator") + "visitpc" + System.getProperty("file.separator") + "webserver" + System.getProperty("file.separator") + "webfiles" + System.getProperty("file.separator") + f);
                if (!srcFile.exists()) {
                    throw new IOException(srcFile + " file not found (from visitpc.jar file).");
                }
                FileIO.CopyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            }
        }
        File srcFile = new File(distJarConfig.tmpDir.getAbsoluteFile() + System.getProperty("file.separator") + "visitpc" + System.getProperty("file.separator") + "webserver" + System.getProperty("file.separator") + "webfiles" + System.getProperty("file.separator") + "template.jnlp");
        if (!srcFile.exists()) {
            throw new IOException(srcFile + " file not found (from visitpc.jar file).");
        }
        if (distJarConfig.distJarDialogConfig.logoFile != null && distJarConfig.distJarDialogConfig.logoFile.length() > 0) {
            srcFile = new File(distJarConfig.distJarDialogConfig.logoFile);
            if (srcFile.exists()) {
                File destImagePath = new File(distJarConfig.webServerConfig.webPath, "images");
                if (!destImagePath.isDirectory()) {
                    destImagePath.mkdirs();
                }
                File destFile = new File(destImagePath, "logo.png");
                FileIO.CopyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            } else {
                throw new IOException(srcFile + " file not found.");
            }
        }
        if (distJarConfig.distJarDialogConfig.smallLogoFile != null && distJarConfig.distJarDialogConfig.smallLogoFile.length() > 0) {
            srcFile = new File(distJarConfig.distJarDialogConfig.smallLogoFile);
            if (srcFile.exists()) {
                File destFile = new File(distJarConfig.webServerConfig.webPath, "favicon.ico");
                FileIO.CopyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            } else {
                throw new IOException(srcFile + " file not found.");
            }
        }
        File destFile = new File(distJarConfig.webServerConfig.webPath, distJarConfig.jarFile.getName());
        if (destFile.exists()) {
            destFile.delete();
        }
        FileIO.CopyFile(distJarConfig.jarFile.getAbsolutePath(), destFile.getAbsolutePath());
    }

    /**
	 * Allow the user to enter the web server configuration and make some checks pertinent to distribution of the jar file via the web server.
	 * 
	 * @param distJarConfig
	 * @param useJavaHomePath
	 * @param encryptKey
	 * @return
	 * @throws IllegalAccessException
	 * @throws NoSuchAlgorithmException
	 * @throws IncorrectKeyException
	 * @throws IOException
	 * @throws VisitPCException
	 */
    private WebServerConfig getWebServerConfig(DistJarConfig distJarConfig, boolean useJavaHomePath, String encryptKey) throws IllegalAccessException, NoSuchAlgorithmException, IncorrectKeyException, IOException, VisitPCException {
        WebServerConfig webServerConfig = new WebServerConfig();
        try {
            webServerConfig.load(VisitPCConstants.WEBSERVER_CONFIG_FILE, useJavaHomePath, encryptKey);
        } catch (IncorrectKeyException e) {
            throw e;
        } catch (IOException e) {
        }
        GenericConfigDialog webServerConfigDialog = new GenericConfigDialog();
        webServerConfigDialog.setConfig(webServerConfig);
        webServerConfigDialog.setTitle("Web Server Configuration");
        webServerConfigDialog.setVisible(true);
        webServerConfig = (WebServerConfig) webServerConfigDialog.getConfig(webServerConfig);
        if (!webServerConfigDialog.isOkSelected()) {
            return null;
        }
        webServerConfig = (WebServerConfig) webServerConfigDialog.getConfig(webServerConfig);
        webServerConfig.save(VisitPCConstants.WEBSERVER_CONFIG_FILE, useJavaHomePath, encryptKey);
        if (!distJarConfig.distJarDialogConfig.clearPCName) {
            int response = JOptionPane.showConfirmDialog(this.launcherFrame, "You have not elected to clear the client PC name in your configuration.\n" + "This means that all users who download the " + distJarConfig.visitPCJarFile.getName() + " file\n" + "may have the same PC name. Are you sure you want to do this ?", "Web Server Distribution", JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return webServerConfig;
    }

    /**
	 * Add the Generic config parameters to the properties
	 * 
	 * @param config The DistJarDialogConfig object
	 * @param configProperties The properties to add to
	 */
    private void addGenericConfig(DistJarDialogConfig config, Properties configProperties, File configDir, String configPassword) throws IOException, NoSuchAlgorithmException {
        configProperties.put(SimpleConfigHelper.PASSWD_MD5SUM_KEY, SimpleConfigHelper.GetMD5SUM(configPassword));
        File logoFiles[] = getLogoImageFiles();
        if (logoFiles[0] != null) {
            config.logoFile = logoFiles[0].getAbsolutePath();
        }
        if (logoFiles[1] != null) {
            config.smallLogoFile = logoFiles[1].getAbsolutePath();
        }
        if (config.logoFile != null && config.logoFile.length() > 0) {
            File lcf = new File(config.logoFile);
            if (lcf.isFile()) {
                File destFile = new File(configDir, lcf.getName());
                FileIO.CopyFile(lcf.getAbsolutePath(), destFile.getAbsolutePath());
            }
        }
        if (config.smallLogoFile != null && config.smallLogoFile.length() > 0) {
            File slcf = new File(config.smallLogoFile);
            if (slcf.isFile()) {
                File destFile = new File(configDir, slcf.getName());
                FileIO.CopyFile(slcf.getAbsolutePath(), destFile.getAbsolutePath());
            }
        }
        if (config.guiMode) {
            SimpleConfigHelper.AddCommandLineArgs(configProperties, "--gui");
        }
        if (config.jarTypeList.equals(DistJarDialogConfig.VNC_FILE_TRANSFER_SRC_CLIENT)) {
            SimpleConfigHelper.AddCommandLineArgs(configProperties, "--show_remote_vnc_panel");
        }
        if (config.allowUserConfigChange) {
            configProperties.put(SimpleConfigHelper.ALLOW_USER_CONFIG_CHANGE_KEY, SimpleConfigHelper.YES);
        } else {
            configProperties.put(SimpleConfigHelper.ALLOW_USER_CONFIG_CHANGE_KEY, SimpleConfigHelper.NO);
        }
    }

    /**
	 * Copy the embedded config file from the src to the destination dir
	 * 
	 * @param embeddedConfigFile
	 * @param configDir
	 * @throws IOException
	 */
    private void copyEmbeddedConfigFile(String embeddedConfigFile, File configDir, String defaultEncryptKey, String newEncryptionKey, boolean includeUserCredentials, boolean clearPCName) throws IOException, IncorrectKeyException, IllegalAccessException, NoSuchAlgorithmException, ClassNotFoundException, InstantiationException {
        ClientConfig clientConfig = null;
        File destFile = new File(configDir, "." + embeddedConfigFile);
        if (embeddedConfigFile.equals(VisitPCConstants.VNC_DEST_CLIENT_CONFIG_FILE)) {
            VNCDestClientConfig cc = new VNCDestClientConfig();
            cc.load(embeddedConfigFile, true, defaultEncryptKey);
            if (clearPCName) {
                cc.pcName = "";
            }
            clientConfig = cc;
        } else if (embeddedConfigFile.equals(VisitPCConstants.FILE_TRANSFER_CONFIG_FILE)) {
            FileTransferClientConfig cc = new FileTransferClientConfig();
            cc.load(embeddedConfigFile, true, defaultEncryptKey);
            if (clearPCName) {
                cc.pcName = "";
            }
            clientConfig = cc;
        } else if (embeddedConfigFile.equals(VisitPCConstants.DEST_CLIENT_CONFIG_FILE)) {
            GenericDestClientConfig cc = new GenericDestClientConfig();
            cc.load(embeddedConfigFile, true, defaultEncryptKey);
            if (clearPCName) {
                cc.pcName = "";
            }
            clientConfig = cc;
        } else if (embeddedConfigFile.equals(VisitPCConstants.VNC_SRC_CLIENT_CONFIG_FILE)) {
            clientConfig = new VNCSrcClientConfig();
            clientConfig.load(embeddedConfigFile, true, defaultEncryptKey);
        } else if (embeddedConfigFile.equals(VisitPCConstants.GENERIC_SRC_CLIENT_CONFIG_FILE)) {
            GenericSrcClientConfig genericSrcClientConfig = new GenericSrcClientConfig();
            genericSrcClientConfig.load(embeddedConfigFile, true, defaultEncryptKey);
            File destFile2 = new File(configDir, "." + VisitPCConstants.GENERIC_SRC_CLIENTS_CONFIG_FILE);
            SimpleConfigList genericSrcClientConfigs = new SimpleConfigList();
            genericSrcClientConfigs.load(VisitPCConstants.GENERIC_SRC_CLIENTS_CONFIG_FILE, defaultEncryptKey);
            for (Object object : genericSrcClientConfigs) {
                GenericSrcClientConfig gscc = (GenericSrcClientConfig) object;
                gscc.serverName = genericSrcClientConfig.serverName;
                gscc.serverPort = genericSrcClientConfig.serverPort;
                gscc.username = genericSrcClientConfig.username;
                gscc.password = genericSrcClientConfig.password;
                gscc.expertMode = genericSrcClientConfig.expertMode;
                gscc.guiMode = genericSrcClientConfig.guiMode;
                gscc.sslTrustStoreFile = genericSrcClientConfig.sslTrustStoreFile;
                gscc.autoConnect = genericSrcClientConfig.autoConnect;
                gscc.serverReconnectDelaySeconds = genericSrcClientConfig.serverReconnectDelaySeconds;
                gscc.allowAnonymousSSL = genericSrcClientConfig.allowAnonymousSSL;
                gscc.useCRC32 = genericSrcClientConfig.useCRC32;
                gscc.greetingResponseTimeoutMillis = genericSrcClientConfig.greetingResponseTimeoutMillis;
                gscc.connectSrcAndDestAtServerResponseTimeoutMillis = genericSrcClientConfig.connectSrcAndDestAtServerResponseTimeoutMillis;
                gscc.connectDestPCToDestServerSocketResponseTimeoutMillis = genericSrcClientConfig.connectDestPCToDestServerSocketResponseTimeoutMillis;
            }
            genericSrcClientConfigs.save(destFile2.getAbsolutePath(), false, newEncryptionKey);
            clientConfig = genericSrcClientConfig;
        } else if (embeddedConfigFile.equals(VisitPCConstants.SERVER_CONFIG_FILE)) {
            ServerConfig serverConfig = new ServerConfig();
            serverConfig.load(embeddedConfigFile, true, defaultEncryptKey);
            serverConfig.save(destFile.getAbsolutePath(), false, newEncryptionKey);
        } else if (embeddedConfigFile.equals(VisitPCConstants.WEBSERVER_CONFIG_FILE)) {
            WebServerConfig webServerConfig = new WebServerConfig();
            webServerConfig.load(VisitPCConstants.WEBSERVER_CONFIG_FILE, true, defaultEncryptKey);
            File destFile2 = new File(configDir, "." + VisitPCConstants.WEBSERVER_CONFIG_FILE);
            webServerConfig.save(destFile2.getAbsolutePath(), false, newEncryptionKey);
        } else {
            throw new IOException(embeddedConfigFile + " is unsupported.");
        }
        if (!includeUserCredentials && clientConfig != null) {
            clientConfig.username = "";
            clientConfig.password = "";
        }
        if (clientConfig != null) {
            clientConfig.save(destFile.getAbsolutePath(), false, newEncryptionKey);
        }
    }

    /**
	 * Get the VNC dest client config
	 * 
	 * @return The VNCDestClientConfig object or null if the user cancelled
	 */
    private VNCDestClientConfig getVNCDestClientConfig(String encryptKey) throws IllegalAccessException, IOException, IncorrectKeyException, NoSuchAlgorithmException, VisitPCException {
        VNCDestClientConfig vncDestClientConfig = new VNCDestClientConfig();
        try {
            vncDestClientConfig.load(VisitPCConstants.VNC_DEST_CLIENT_CONFIG_FILE, true, encryptKey);
        } catch (IOException e) {
            throw new IOException("No VNC destination client configuration found.\nPlease configure this first and check it works.");
        }
        VNCDestConfigDialog vncDestConfigDialog = new VNCDestConfigDialog(null, VNCDestConfigDialog.DLG_TITLE, vncDestClientConfig);
        vncDestConfigDialog.setVisible(true);
        if (!vncDestConfigDialog.isOkSelected()) {
            return null;
        }
        vncDestClientConfig = vncDestConfigDialog.getConfig(vncDestClientConfig);
        vncDestClientConfig.checkValid();
        vncDestClientConfig.save(VisitPCConstants.VNC_DEST_CLIENT_CONFIG_FILE, true, encryptKey);
        return vncDestClientConfig;
    }

    /**
	 * Get the File transfer  dest client config
	 * 
	 * @return The FileTransferClientConfig object or null if the user cancelled
	 */
    private FileTransferClientConfig getFileTransferDestClientConfig(String encryptKey) throws IllegalAccessException, IOException, IncorrectKeyException, NoSuchAlgorithmException, VisitPCException {
        FileTransferClientConfig fileTransferClientConfig = new FileTransferClientConfig();
        try {
            fileTransferClientConfig.load(VisitPCConstants.FILE_TRANSFER_CONFIG_FILE, true, encryptKey);
        } catch (IOException e) {
            throw new IOException("No File transfer destination client configuration found.\nPlease configure this first and check it works.");
        }
        FileTransferDestConfigDialog fileTransferDestConfigDialog = new FileTransferDestConfigDialog(null, FileTransferDestConfigDialog.DLG_TITLE, fileTransferClientConfig);
        fileTransferDestConfigDialog.setVisible(true);
        if (!fileTransferDestConfigDialog.isOkSelected()) {
            return null;
        }
        fileTransferClientConfig = fileTransferDestConfigDialog.getConfig(fileTransferClientConfig);
        fileTransferClientConfig.checkValid();
        fileTransferClientConfig.save(VisitPCConstants.FILE_TRANSFER_CONFIG_FILE, true, encryptKey);
        return fileTransferClientConfig;
    }

    /**
	 * Get the Generic/Port forward dest client config
	 * 
	 * @return The GenericDestClientConfig object or null if the user cancelled
	 */
    private GenericDestClientConfig getGenericDestClientConfig(String encryptKey) throws IllegalAccessException, IOException, IncorrectKeyException, NoSuchAlgorithmException, VisitPCException {
        GenericDestClientConfig genericDestClientConfig = new GenericDestClientConfig();
        try {
            genericDestClientConfig.load(VisitPCConstants.DEST_CLIENT_CONFIG_FILE, true, encryptKey);
        } catch (IOException e) {
            throw new IOException("No Port forward destination client configuration found.\nPlease configure this first and check it works.");
        }
        GenericDestConfigDialog genericDestConfigDialog = new GenericDestConfigDialog(null, GenericDestConfigDialog.DLG_TITLE, genericDestClientConfig);
        genericDestConfigDialog.setVisible(true);
        if (!genericDestConfigDialog.isOkSelected()) {
            return null;
        }
        genericDestClientConfig = genericDestConfigDialog.getConfig(genericDestClientConfig);
        genericDestClientConfig.checkValid();
        genericDestClientConfig.save(VisitPCConstants.DEST_CLIENT_CONFIG_FILE, true, encryptKey);
        return genericDestClientConfig;
    }

    /**
	 * Get the VNC Src client config
	 * 
	 * @param encryptKey
	 * @return
	 */
    private VNCSrcClientConfig getVNCSrcClientConfig(String encryptKey) throws IllegalAccessException, IOException, IncorrectKeyException, NoSuchAlgorithmException, VisitPCException {
        VNCSrcClientConfig vncSrcClientConfig = new VNCSrcClientConfig();
        try {
            vncSrcClientConfig.load(VisitPCConstants.VNC_SRC_CLIENT_CONFIG_FILE, true, encryptKey);
        } catch (IOException e) {
            throw new IOException("No Port forward destination client configuration found.\nPlease configure this first and check it works.");
        }
        VNCSrcClientDialog vncSrcClientDialog = new VNCSrcClientDialog(null, VNCSrcClientDialog.DLG_TITLE, vncSrcClientConfig);
        vncSrcClientDialog.setVisible(true);
        if (!vncSrcClientDialog.isOkSelected()) {
            return null;
        }
        vncSrcClientConfig = vncSrcClientDialog.getConfig(vncSrcClientConfig);
        vncSrcClientConfig.checkValid();
        vncSrcClientConfig.save(VisitPCConstants.VNC_SRC_CLIENT_CONFIG_FILE, true, encryptKey);
        return vncSrcClientConfig;
    }

    /**
	 * Get the Dist Jar configuration
	 * 
	 * @return The DistJarDialogConfig object or null if the user cancelled.
	 * 
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
    private DistJarDialogConfig getDistJarConfig() throws IllegalAccessException, IOException {
        createDistJarDialog = new GenericConfigDialog(null, "Create Distributable Jar");
        DistJarDialogConfig config = new DistJarDialogConfig();
        try {
            config.load(DIST_JAR__DIALOG_CONFIG_FILENAME, true);
        } catch (Exception e) {
        }
        createDistJarDialog.setConfig(config);
        createDistJarDialog.addChangeListener(this);
        UI.CenterOnScreen(createDistJarDialog);
        createDistJarDialog.setVisible(true);
        if (!createDistJarDialog.isOkSelected()) {
            return null;
        }
        config = (DistJarDialogConfig) createDistJarDialog.getConfig(config);
        File opf = new File(config.outputPath);
        if (!opf.isDirectory()) {
            throw new IOException(config.outputPath + " directory not found.");
        }
        try {
            config.save(DIST_JAR__DIALOG_CONFIG_FILENAME, true);
        } catch (Exception e) {
        }
        return config;
    }

    public void stateChanged(ChangeEvent e) {
        if (createDistJarDialog == null) {
            return;
        }
        try {
            DistJarDialogConfig config = (DistJarDialogConfig) createDistJarDialog.getConfig();
            if (config.jarTypeList.equals(DistJarDialogConfig.VNC_FILE_TRANSFER_SRC_CLIENT)) {
                config.guiMode = true;
                config.clearPCName = true;
                createDistJarDialog.enableField(4, false);
                createDistJarDialog.enableField(6, false);
            } else {
                createDistJarDialog.enableField(4, true);
                createDistJarDialog.enableField(6, true);
            }
            File opDir = new File(config.outputPath);
            if (config.outputPath.length() == 0 || !opDir.isDirectory()) {
                createDistJarDialog.setFieldBackgroundColor(1, LongNumberField.INVALID_VALUE_COLOR);
            } else {
                createDistJarDialog.setFieldBackgroundColor(1, Color.WHITE);
            }
            createDistJarDialog.updateView(config);
        } catch (IllegalAccessException ex) {
        }
    }

    /**
	 * Get any logo files that have been created by the user in the jar file
	 * path to the config.
	 * 
	 * @return A File array with two elements. 0 = The logo file or null if not
	 *         created by the user. 1 = The small logo file or null if not
	 *         created by the user.
	 */
    private File[] getLogoImageFiles() throws IOException {
        File logFileList[] = new File[2];
        String userDir = System.getProperty("user.dir");
        File fileList[] = new File(userDir).listFiles();
        for (File f : fileList) {
            String filename = f.getName().toLowerCase();
            if (filename.startsWith("logo") && filename.endsWith(".png")) {
                logFileList[0] = f;
            }
        }
        File small_logo_file = new File(userDir, SMALL_LOGO_IMAGE_FILENAME);
        if (small_logo_file.isFile()) {
            logFileList[1] = small_logo_file;
        }
        return logFileList;
    }

    /**
	 * Copy the trust store file from the source into the dest.
	 * 
	 * @param trustStoreFile
	 * @param destPath
	 * @return
	 * @throws IOException
	 */
    private String copyTrustStoreFile(String trustStoreFile, String destPath) throws IOException {
        String trustStoreFilename = "";
        if (trustStoreFile != null && trustStoreFile.length() > 0) {
            File sslTrustStoreFile = new File(trustStoreFile);
            if (sslTrustStoreFile.isFile()) {
                String sslTrustStoreFileName = sslTrustStoreFile.getName();
                File destSSLTrustStoreFile = new File(destPath, sslTrustStoreFileName);
                String driveLessDestPath = destPath;
                int pos = destPath.indexOf(":");
                if (pos >= 1) {
                    driveLessDestPath = destPath.substring(pos + 1);
                }
                if (!driveLessDestPath.startsWith(System.getProperty("file.separator"))) {
                    driveLessDestPath = System.getProperty("file.separator") + driveLessDestPath;
                }
                FileIO.CopyFile(sslTrustStoreFile.getAbsolutePath(), destSSLTrustStoreFile.getAbsolutePath());
                trustStoreFilename = sslTrustStoreFileName;
            }
        }
        return trustStoreFilename;
    }

    /**
	 * Remove directory and all the files it contains
	 * 
	 * @param dir
	 */
    private void removeDirectory(File dir) throws IOException {
        if (!dir.isDirectory()) {
            return;
        }
        File entryList[] = dir.listFiles();
        for (File f : entryList) {
            if (f.isDirectory()) {
                removeDirectory(f);
            }
            if (f.isDirectory() || f.isFile()) {
                if (!f.delete()) {
                    throw new IOException("Failed to delete " + f);
                }
            }
        }
        if (!dir.delete()) {
            throw new IOException("Failed to delete " + dir);
        }
    }

    /**
	 * Check that it is possible to connect to the VisitPC server.
	 * 
	 * @param server
	 *            The VisitPC server address
	 * @param port
	 *            The VisitPC server port
	 * @param connectTimeoutMS
	 *            The timeout for the socket connect in milli seconds
	 * @throws IOException
	 *             Thrown if a connection cannot be established with the server.
	 */
    private void checkServerReachable(String server, int port, int connectTimeoutMS) throws IOException {
        boolean serverReachable = false;
        Timer timer = null;
        checkServerReachableProgressCount = 0;
        class Ticker implements ActionListener {

            public void actionPerformed(ActionEvent event) {
                if (checkServerReachableProgressMonitor != null) {
                    if (checkServerReachableProgressMonitor.isCanceled() && checkServerReachableSocket != null) {
                        try {
                            checkServerReachableSocket.close();
                        } catch (IOException e) {
                        }
                    }
                    checkServerReachableProgressMonitor.setProgress(checkServerReachableProgressCount);
                }
                checkServerReachableProgressCount++;
            }
        }
        checkServerReachableProgressMonitor = new ProgressMonitor(null, "Checking the VisitPC server (" + server + ":" + port + ") is reachable.", null, 0, 120);
        timer = new Timer(1000, new Ticker());
        timer.start();
        try {
            checkServerReachableSocket = new Socket();
            InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(server), port);
            checkServerReachableSocket.connect(isa, connectTimeoutMS);
            serverReachable = true;
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        } finally {
            if (checkServerReachableSocket != null) {
                checkServerReachableSocket.close();
            }
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            if (checkServerReachableProgressMonitor != null) {
                checkServerReachableProgressMonitor.close();
                checkServerReachableProgressMonitor = null;
            }
        }
        if (!serverReachable) {
            throw new IOException("Unable to connect to a VisitPC server on " + server + ":" + port + ". Please check your configuration.");
        }
    }

    /**
	 * Check to see if the server is on a local subnet. It is easier to setup
	 * the VisitPC software without having an address which is addressable on
	 * the Internet. We don't want to create jar files with VisitPC server
	 * addresses set like this of the Jar files created will not be usable
	 * outside the local subnet and so if they are distributed to users they
	 * won't work for them. Therefore this method is called to check to see if
	 * the server IP address configured is on a local subnet.
	 * 
	 * Should only be called after checkServerReachable()
	 * 
	 * @param server
	 *            The VisitPC server address
	 * @throws IOException
	 *             Thrown if the server is on a local subnet.
	 */
    private void checkServerNotOnLocalSubnet(String server) throws IOException {
        byte ipAddressBytes[] = DistJarFactory.ParseIPAddress(server);
        if (ipAddressBytes != null) {
            Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
            while (eni.hasMoreElements()) {
                NetworkInterface networkCard = eni.nextElement();
                List<InterfaceAddress> ncAddrList = networkCard.getInterfaceAddresses();
                Iterator<InterfaceAddress> ncAddrIterator = ncAddrList.iterator();
                while (ncAddrIterator.hasNext()) {
                    InterfaceAddress networkCardAddress = ncAddrIterator.next();
                    if (networkCardAddress.getAddress().isLoopbackAddress()) {
                        continue;
                    }
                    long subnetMask = DistJarFactory.GetSubnetMask(networkCardAddress.getNetworkPrefixLength());
                    long ifIPAddress = DistJarFactory.GetIPAddressLong(networkCardAddress.getAddress().getHostAddress());
                    long ipAddress = DistJarFactory.ConvertToLong(ipAddressBytes);
                    if ((ipAddress & subnetMask) == (ifIPAddress & subnetMask)) {
                        throw new IOException("The server IP address (" + server + ") is on a local subnet. Therefore it is unlikley\nthat the VisitPC server will be reachable over the Internet. Please configure\nan address (name or IP address) that is reachable over the Internet.");
                    }
                }
            }
        }
    }

    /**
	 * Parse an IP address
	 * 
	 * @param address
	 *            The IP address string
	 * @return a byte array of four bytes, the IP address in 32 bit format or
	 *         null if the IP address is invalid
	 * 
	 *         May throw a NumberFormatException
	 */
    public static byte[] ParseIPAddress(String address) {
        byte ip_address[] = new byte[4];
        StringTokenizer strTok = new StringTokenizer(address, ".");
        if (strTok.countTokens() == 4) {
            try {
                ip_address[0] = (byte) Integer.parseInt(strTok.nextToken());
                ip_address[1] = (byte) Integer.parseInt(strTok.nextToken());
                ip_address[2] = (byte) Integer.parseInt(strTok.nextToken());
                ip_address[3] = (byte) Integer.parseInt(strTok.nextToken());
                return ip_address;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
	 * Get the sub network mask given the number of bits in the subnet mask.
	 * This supports only IPV4.
	 * 
	 * @param networkPrefixLength
	 *            The number of bits used in the subnet. E.G 24 = 255.255.255.0
	 *            = 0xffffff00
	 * @return The subnet mask. 0xffffff00 from the above example.
	 */
    public static long GetSubnetMask(int networkPrefixLength) {
        long subnetMask = 0xffffffff;
        if (networkPrefixLength < 32) {
            int zeroBitCount = 32 - networkPrefixLength;
            for (int b = 0; b < zeroBitCount; b++) {
                subnetMask = subnetMask & ~(1 << b);
            }
        }
        subnetMask = subnetMask & 0xffffffffL;
        return subnetMask;
    }

    /**
	 * Get the IP address as a long value. Only supports IPV4.
	 * 
	 * @param ipAddress
	 *            The IP address String.
	 * @return The long value representing the above IP address (0 if ipAddress
	 *         is not an IP address)
	 * 
	 */
    public static long GetIPAddressLong(String ipAddress) {
        long longIPAddress = 0;
        byte ipAddressBytes[] = DistJarFactory.ParseIPAddress(ipAddress);
        if (ipAddressBytes != null) {
            longIPAddress = DistJarFactory.ConvertToLong(ipAddressBytes);
        }
        longIPAddress = longIPAddress & 0xffffffffL;
        return longIPAddress;
    }

    /**
	 * Convert the byte array to a long value.
	 * 
	 * @param array
	 *            A four element , byte array
	 * @return The long value. 0 is returned if the byte arrray is not four
	 *         bytes long.
	 */
    public static long ConvertToLong(byte array[]) {
        long longValue = 0;
        if (array.length == 4) {
            for (byte b : array) {
                longValue = (longValue << 8) | (b & 0xff);
            }
        }
        return longValue;
    }
}
