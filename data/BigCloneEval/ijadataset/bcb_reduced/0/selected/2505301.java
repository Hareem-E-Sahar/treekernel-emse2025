package unibg.overencrypt.client;

import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import unibg.overencrypt.client.managers.CheckPinManager;
import unibg.overencrypt.client.managers.ClientDeleteManager;
import unibg.overencrypt.client.managers.ClientLogoutManager;
import unibg.overencrypt.client.managers.ClientPropertiesManager;
import unibg.overencrypt.client.managers.ClientUploadFileManager;
import unibg.overencrypt.client.managers.DHKeyAgreementManager;
import unibg.overencrypt.client.managers.ClientDownloadManager;
import unibg.overencrypt.client.managers.ClientUpdatePermissionsManager;
import unibg.overencrypt.client.managers.ClientUploadFolderManager;
import unibg.overencrypt.client.view.ProcessesManager;
import unibg.overencrypt.protocol.OperationType;
import unibg.overencrypt.utility.FileSystemUtils;

/**
 * Execute OverEncryption java client through command line
 * 
 * Commands: 
 * 			- '-gKP' or '--generateKeyPairs': DH key pairs generation
 * 			- '-cP'  or '--checkPin'		: the user has already done DH key agreement, check if
 * 												he has his PIN and UserID in local private resource else
 * 												prompt for ask PIN and save.
 * 			- '-fld' or '--folder' 			: start folder creation process
 * 			- '-e'   or '--error'			: show response error received from server
 * 			- '-uP'  or '--updatePerms' 	: start update permissions process
 *  		- '-sU'  or '--startUpload'		: start upload process
 *			- '-sD'  or '--startDownload'	: start download process
 *			- '-eP'  or '--editPermission'	: start edit permission process 
 * 			- '-d'   or '--delete'			: start folder deleting process
 * 			- '-p'   or '--properties'		: show the properties of the selected folder
 * 			- '-l'   or '--logout'			: close the actual session
 * 
 * 
 * @author Flavio Giovarruscio & Riccardo Tribbia
 * @version 1.0
 */
public class Main {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private static ProcessesManager processManager = null;

    /**
	 * Main method for OverEncrypt-client.jar that receives args in input and return nothing
	 * @param args argument specified in class description
	 */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Error while set Look and Feel - " + e.getMessage());
        } catch (InstantiationException e) {
            LOGGER.warn("Error while set Look and Feel - " + e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.warn("Error while set Look and Feel - " + e.getMessage());
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.warn("Error while set Look and Feel - " + e.getMessage());
        }
        File localPath = new File(ClientConfiguration.getLOCAL_PRIVATE_RESOURCES_PATH());
        while (!localPath.exists() || !localPath.isDirectory()) {
            localPath = FileSystemUtils.mkdir(ClientConfiguration.getLOCAL_PRIVATE_RESOURCES_PATH());
        }
        File localTempPath = new File(ClientConfiguration.getLOCAL_TMP_PATH());
        while (!localTempPath.exists() || !localTempPath.isDirectory()) {
            localTempPath = FileSystemUtils.mkdir(ClientConfiguration.getLOCAL_TMP_PATH());
        }
        LOGGER.debug("Args passed: ");
        for (int i = 0; i < args.length; i++) {
            LOGGER.debug("Args[" + i + "]: " + args[i]);
        }
        if (args.length > 0 && args[0] != "") {
            String[] split = args[1].split("/");
            String urlServer = new String();
            for (int i = 0; i < 4; i++) {
                urlServer = urlServer.concat(split[i] + "/");
            }
            ClientConfiguration.setURL_WEBDAV_SERVER(urlServer.substring(0, urlServer.lastIndexOf("/")));
            if ("-cP".equals(args[0]) || "--checkPin".equals(args[0])) {
                LOGGER.debug("--checkPin recognised.");
                CheckPinManager.checkPin(args[1], args[2], args[3]);
                System.exit(0);
            }
            if ("-gKP".equals(args[0]) || "--generateKeyPairs".equals(args[0])) {
                LOGGER.debug("--generateKeyPairs recognised.");
                DHKeyAgreementManager.generateKeyPairs(args[1], args[2], args[3]);
                System.exit(0);
            }
            if ("-fld".equals(args[0]) || "--folder".equals(args[0])) {
                LOGGER.debug("--folder recognised.");
                ClientUploadFolderManager.startUploadFolder(args[1]);
            }
            if ("-e".equals(args[0]) || "--error".equals(args[0])) {
                LOGGER.debug("--error recognised.");
                OverEncryptClient.showError(args[2]);
                System.exit(0);
            }
            if ("-uP".equals(args[0]) || "--updatePerms".equals(args[0])) {
                LOGGER.debug("--updatePermissions recognised.");
                ClientUpdatePermissionsManager.updatePermissionsManager(args[1], args[2], args[3]);
                System.exit(0);
            }
            if ("-sU".equals(args[0]) || "--startUpload".equals(args[0])) {
                LOGGER.debug("--startUpload recognised.");
                ClientUploadFileManager.uploadStarted(args[1]);
                System.exit(0);
            }
            if ("-sD".equals(args[0]) || "--startDownload".equals(args[0])) {
                LOGGER.debug("--startDownload recognised.");
                ClientDownloadManager.downloadStarted(args[1], args[2]);
                System.exit(0);
            }
            if ("-eP".equals(args[0]) || "--editPermission".equals(args[0])) {
                LOGGER.debug("--editPermission recognised.");
                processManager = new ProcessesManager(OperationType.EDIT, args[1], args[2]);
                processManager.execute();
            }
            if ("-d".equals(args[0]) || "--delete".equals(args[0])) {
                LOGGER.debug("--delete recognised.");
                ClientDeleteManager.deleteFolder(args[1], args[2]);
                System.exit(0);
            }
            if ("-p".equals(args[0]) || "--properties".equals(args[0])) {
                LOGGER.debug("--properties recognised.");
                ClientPropertiesManager.generateProperties(args[1], args[2]);
            }
            if ("-l".equals(args[0]) || "--logout".equals(args[0])) {
                LOGGER.debug("--logout recognised.");
                ClientLogoutManager.logout(args[1]);
                System.exit(0);
            }
        }
    }
}
