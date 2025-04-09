package data;

import gui.HyperFrame;
import gui.PublishDialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import run.Main;

/**
 * The RemotePublish class is responsible for taking exported data and syncing or
 * publishing remotely.
 * This class extends AbstractAction so it can be added to the HyperFrame's menu directly.
 */
public class RemotePublish extends AbstractAction {

    public static final String SETTINGS_LABEL = "publishSettings";

    public static final int PUBLISH_FTP = 1;

    public static final int PUBLISH_WEBDAV = 2;

    public static final int FORMAT_HTML = 1;

    public static final int FORMAT_OBJECT = 2;

    public static final int RETURN_SUCCESS = 1;

    public static final int RETURN_CONNECT_ERROR = -1;

    public static final int RETURN_INVALID_PUB_TYPE = -2;

    public static final int RETURN_INVALID_FORMAT = -3;

    public static final int RETURN_TRANSMIT_ERROR = -4;

    public static final int RETURN_UNKNOWN_HOST = -6;

    public static final int RETURN_OVERWRITE_ERROR = -7;

    public static final int RETURN_PARAM_ERROR = -5;

    /** Creates a new instance of RemotePublish */
    public RemotePublish() {
        super("Publish");
    }

    /** Publishes current document to a server of type @param publishType, in format of @param formatType.
     *  @return false on fail.
     *  @param username login username. If null, annon login will be attempted
     *
     *  FTP is done with the help of apache.commons.net FTP support. Checkout
     *  http://commons.apache.org/net/apidocs/org/apache/commons/net/ftp/FTPClient.html
     *  for details of the API.
     */
    public int remotePublish(int publishType, int formatType, boolean overwrite, String url, String username, String password, String deployDir) {
        String temp = System.getProperty("java.io.tmpdir");
        File tempDir = new File(temp + "/hypernotes");
        tempDir.mkdir();
        switch(formatType) {
            case FORMAT_HTML:
                ExportAsHTML x = new ExportAsHTML();
                Node children[] = HyperFrame.getActiveFrame().getNodeTree().getRootNode().getChildren();
                for (int i = 0; i < children.length; i++) {
                    x.createFileStructure(tempDir, HyperFrame.getActiveFrame().getNodeTree(), children[i], false);
                }
                break;
            case FORMAT_OBJECT:
                tempDir.delete();
                System.out.println("RemotePublish : Object Format not impl");
                return RETURN_PARAM_ERROR;
            default:
                System.out.println("Remote Publish : Uknown format: " + formatType + ": Aborting");
                tempDir.delete();
                return RETURN_PARAM_ERROR;
        }
        if (deployDir == null || deployDir.trim().equals("")) {
            deployDir = HyperFrame.getActiveFrame().getNodeTree().getRootNode().getTitle();
        }
        switch(publishType) {
            case PUBLISH_FTP:
                {
                    try {
                        FTPClient ftp = new FTPClient();
                        ftp.connect(url);
                        if (username != null) {
                            ftp.login(username, password);
                        }
                        int reply = ftp.getReplyCode();
                        if (FTPReply.isPositiveCompletion(reply)) {
                            ftp.enterLocalPassiveMode();
                            if (!overwrite) {
                                String[] dir = ftp.listNames();
                                for (int x = 0; x < dir.length; x++) {
                                    String curr = dir[x];
                                    if (curr.equals(deployDir)) {
                                        ftp.disconnect();
                                        return RETURN_OVERWRITE_ERROR;
                                    }
                                }
                            } else {
                                ftp.dele(deployDir);
                            }
                            ftp.mkd(deployDir);
                            ftp.changeWorkingDirectory(deployDir);
                            createRemoteDirectory(tempDir, ftp, "");
                            ftp.disconnect();
                            deleteLocalDirectory(tempDir);
                        } else {
                            return RETURN_CONNECT_ERROR;
                        }
                    } catch (UnknownHostException uhe) {
                        return RETURN_UNKNOWN_HOST;
                    } catch (SocketException ex) {
                        return RETURN_CONNECT_ERROR;
                    } catch (IOException ex) {
                        return RETURN_CONNECT_ERROR;
                    }
                    break;
                }
            default:
                {
                    System.out.println("RemotePublish : Publish type not supported");
                }
                break;
        }
        return RETURN_SUCCESS;
    }

    /** Retrieve files from server. Files are expected to be of object type */
    public int remoteRetrieve(int publishType, String url, String username, String password) {
        return RETURN_CONNECT_ERROR;
    }

    /** Recurse through directories sending files  */
    private void createRemoteDirectory(File root, FTPClient ftp, String path) throws IOException {
        File[] files = root.listFiles();
        for (int x = 0; x < files.length; x++) {
            File f = files[x];
            if (f.isDirectory()) {
                ftp.mkd(f.getName());
                ftp.changeWorkingDirectory(f.getName());
                path += "/" + f.getName();
                createRemoteDirectory(f, ftp, path);
            } else {
                ftp.changeWorkingDirectory(path);
                FileInputStream is = new FileInputStream(f);
                ftp.storeFile(f.getName(), is);
                is.close();
            }
        }
    }

    /** Recurse thru directories, deleting them */
    private boolean deleteLocalDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteLocalDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /** Menu item clicked */
    public void actionPerformed(ActionEvent e) {
        PublishDialog pd = null;
        File f = new File(Main.SETTINGS_FILENAME);
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                Properties p = new Properties();
                try {
                    p.load(fis);
                    String url = (String) p.get("url");
                    String user = (String) p.get("user");
                    String pass = (String) p.get("pass");
                    String anon = (String) p.get("anon");
                    String deployDir = (String) p.get("deployDir");
                    String overwrite = (String) p.get("overwrite");
                    pd = new PublishDialog(null, true, url, user, pass, deployDir, Boolean.parseBoolean(anon), Boolean.parseBoolean(overwrite));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } else {
            pd = new PublishDialog(null, true);
        }
        if (pd.okPressed()) {
            String url = pd.getURL();
            String user = pd.getUsername();
            String pass = pd.getPassword();
            String deployDir = pd.getDeployDir();
            boolean anon = pd.isAnon();
            boolean overwrite = pd.isOverwrite();
            boolean remember = pd.isRemember();
            if (anon) {
                user = "";
                pass = "";
            }
            if (remember) {
                Properties p = new Properties();
                p.put("url", url);
                p.put("user", user);
                p.put("pass", pass);
                p.put("anon", new Boolean(anon).toString());
                p.put("overwrite", new Boolean(overwrite).toString());
                p.put("deployDir", deployDir);
                Util.saveSettings(Main.SETTINGS_FILENAME, SETTINGS_LABEL, p);
            } else {
                if (f.exists()) {
                    f.delete();
                }
            }
            if (url.trim().equals("") || ((user.trim().equals("") || pass.trim().equals("")) && !anon)) {
                JOptionPane.showMessageDialog(null, "Invalid Publish Details", "Publish Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int ret = remotePublish(PUBLISH_FTP, FORMAT_HTML, overwrite, url, user, pass, deployDir);
            switch(ret) {
                case RETURN_SUCCESS:
                    JOptionPane.showMessageDialog(null, "Documents published successfully.", "Publish Succesful", JOptionPane.ERROR_MESSAGE);
                    break;
                case RETURN_CONNECT_ERROR:
                    JOptionPane.showMessageDialog(null, "Unable to connect to host: " + url, "Publish Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case RETURN_UNKNOWN_HOST:
                    JOptionPane.showMessageDialog(null, "Unknown host: " + url, "Publish Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case RETURN_OVERWRITE_ERROR:
                    JOptionPane.showMessageDialog(null, "Directory Exists; Cannot overwrite.", "Publish Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        }
    }
}
