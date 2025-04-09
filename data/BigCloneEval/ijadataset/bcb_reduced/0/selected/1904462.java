package plugins.fileplugin;

import gui.JPTrayIcon;
import gui.MainWindow;
import hanasu.p2p.Message;
import hanasu.p2p.P2PConnection;
import hanasu.tools.ByteBufferUtilities;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.sf.jcarrierpigeon.WindowPosition;
import net.sf.jtelegraph.Telegraph;
import net.sf.jtelegraph.TelegraphType;
import plugins.fileplugin.FileShareBrowser.FileShareBrowserEvents;
import plugins.fileplugin.LoadBar.LoadBarEvent;

public class FileWindow extends JFrame {

    private ArrayList<Load> downloads = new ArrayList<Load>();

    private ArrayList<Load> uploads = new ArrayList<Load>();

    private static final long serialVersionUID = 3259316704555751058L;

    private JPanel pnlShares;

    private FileShareBrowser browserShares;

    P2PConnection connection;

    private FileShareDirectory parentDirectory;

    private String username;

    private File fileShares, fileLoads, fileDownloads, fileParts;

    private LoadBar lbDownloads;

    private static final int partsLength = 1024 * 32;

    private LoadBar lbUploads;

    private JTabbedPane tabbedPane;

    private JTabbedPane tbpTransfers;

    @SuppressWarnings("deprecation")
    public FileWindow(String username, final P2PConnection connection2, boolean startDirectly) {
        this.connection = connection2;
        this.username = username;
        setTitle(Messages.getString("FileWindow.ShareFilesWithContact").replace("$CONTACT$", username));
        setIconImage(Toolkit.getDefaultToolkit().getImage(FileWindow.class.getResource("/gui/64.png")));
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        pnlShares = new JPanel();
        tabbedPane.addTab(Messages.getString("FileWindow.Shares"), null, pnlShares, null);
        pnlShares.setLayout(new BorderLayout(0, 0));
        browserShares = new FileShareBrowser();
        pnlShares.add(browserShares, BorderLayout.CENTER);
        JPanel pnlTransfers = new JPanel();
        tabbedPane.addTab(Messages.getString("FileWindow.Transfers"), null, pnlTransfers, null);
        pnlTransfers.setLayout(new BorderLayout(0, 0));
        tbpTransfers = new JTabbedPane(JTabbedPane.TOP);
        pnlTransfers.add(tbpTransfers, BorderLayout.CENTER);
        JPanel pnlDownloads = new JPanel();
        tbpTransfers.addTab(Messages.getString("FileWindow.Downloads"), null, pnlDownloads, null);
        pnlDownloads.setLayout(new BorderLayout(0, 0));
        lbDownloads = new LoadBar();
        pnlDownloads.add(lbDownloads, BorderLayout.CENTER);
        JPanel panel = new JPanel();
        pnlDownloads.add(panel, BorderLayout.NORTH);
        JButton btnShowDownloadFolder = new JButton(Messages.getString("FileWindow.ShowDownloadFolder"));
        btnShowDownloadFolder.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                showDownloadFolder();
            }
        });
        panel.add(btnShowDownloadFolder);
        JPanel pnlUploads = new JPanel();
        tbpTransfers.addTab(Messages.getString("FileWindow.Uploads"), null, pnlUploads, null);
        pnlUploads.setLayout(new BorderLayout(0, 0));
        lbUploads = new LoadBar();
        pnlUploads.add(lbUploads);
        this.setLocationRelativeTo(null);
        loadShares();
        if (connection.hasInitiatedConnection()) {
            fileSharesHaveUpdated();
        }
        this.setSize(712, 480);
        if (startDirectly) this.show(); else this.setVisible(false);
    }

    protected void showDownloadFolder() {
        if (Desktop.isDesktopSupported()) {
            try {
                if (JPTrayIcon.isWindows()) Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \"" + fileDownloads.getAbsolutePath() + "\""); else Desktop.getDesktop().browse(getFileURI(fileDownloads.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else JOptionPane.showMessageDialog(this, Messages.getString("FileWindow.DirectoryOpenNotSupported").replace("$PATH$", fileDownloads.getAbsolutePath()));
    }

    private URI getFileURI(String filePath) {
        URI uri = null;
        filePath = filePath.trim();
        if (filePath.indexOf("http") == 0 || filePath.indexOf("\\") == 0) {
            if (filePath.indexOf("\\") == 0) filePath = "file:" + filePath;
            try {
                filePath = filePath.replaceAll(" ", "%20");
                URL url = new URL(filePath);
                uri = url.toURI();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        } else {
            File file = new File(filePath);
            uri = file.toURI();
        }
        return uri;
    }

    private FileWindow getMe() {
        return this;
    }

    private void loadShares() {
        lbDownloads.setLoadbarEvent(new LoadBarEvent() {

            @Override
            public void cancel(Load load, boolean ask) {
                if (!downloads.contains(load)) return;
                if (!ask || JOptionPane.showConfirmDialog(getMe(), Messages.getString("FileWindow.CancelDownload"), Messages.getString("FileWindow.ConfirmCancel"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                    downloads.remove(load);
                    lbDownloads.removeLoad(load);
                    String filetitle = getFileTitle(load);
                    File targetFile = new File(fileDownloads.getAbsolutePath() + "/" + filetitle);
                    if (targetFile.exists()) {
                        try {
                            targetFile.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    saveLoads();
                }
            }

            @Override
            public void pause(LoadEntity entity, boolean continueLoad) {
                entity.getLoad().setPaused(!continueLoad);
                if (continueLoad) {
                    continueLoad(entity.getLoad());
                }
                saveLoads();
            }
        });
        lbUploads.setLoadbarEvent(new LoadBarEvent() {

            @Override
            public void cancel(Load load, boolean ask) {
                if (!uploads.contains(load)) {
                    lbUploads.removeLoad(load);
                    return;
                }
                if (!ask || JOptionPane.showConfirmDialog(getMe(), Messages.getString("FileWindow.SureCancelUpload"), Messages.getString("FileWindow.ConfirmCancel"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                    uploads.remove(load);
                    lbUploads.removeLoad(load);
                    saveLoads();
                }
            }

            @Override
            public void pause(LoadEntity entity, boolean continueLoad) {
                entity.getLoad().setPaused(!continueLoad);
                saveLoads();
            }
        });
        fileShares = new File(username + ".shares");
        fileLoads = new File(username + ".loads");
        fileDownloads = new File("Downloads/" + username);
        fileParts = new File(fileDownloads.getAbsoluteFile() + "/parts/");
        fileParts.mkdirs();
        if (fileShares.exists()) {
            FileInputStream input;
            try {
                input = new FileInputStream(fileShares);
                byte[] bytInput = new byte[(int) fileShares.length()];
                input.read(bytInput);
                input.close();
                parentDirectory = FileShareDirectory.deserialize(bytInput);
            } catch (IOException e) {
                e.printStackTrace();
                parentDirectory = new FileShareDirectory();
            }
        } else parentDirectory = new FileShareDirectory();
        browserShares.setParentDirectory(parentDirectory);
        browserShares.setEventListener(new FileShareBrowserEvents() {

            @Override
            public void fileSharesUpdated() {
                fileSharesHaveUpdated();
            }

            @Override
            public void download(FileShareFile[] files) {
                for (FileShareFile download : files) {
                    Load load = new Load();
                    Load loadRemove = null;
                    for (Load cload : lbDownloads.getLoads()) {
                        if (cload.getFilename().equals(download.getFilename())) {
                            loadRemove = cload;
                            break;
                        }
                    }
                    load.setFilename(download.getFilename());
                    load.setDownload(true);
                    File targetFile = new File(fileDownloads.getAbsolutePath() + "/" + getFileTitle(load));
                    if (targetFile.exists()) targetFile.delete();
                    if (loadRemove != null) {
                        loadRemove.setFinished(true);
                        lbDownloads.removeLoad(loadRemove);
                    }
                    downloads.add(load);
                    lbDownloads.addLoad(load);
                    requestDownload(download.getFilename(), 0);
                }
                saveLoads();
                tabbedPane.setSelectedIndex(1);
                tbpTransfers.setSelectedIndex(0);
            }
        });
        loadLoads();
    }

    protected void fileSharesHaveUpdated() {
        Message msg = new Message(parentDirectory.serialize(), (byte) 0, FilePlugin.FileGUID);
        try {
            connection.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void continueLoad(Load load) {
        if (load.getPartsMax() == 0) {
            requestDownload(load.getFilename(), 0);
            return;
        }
        int part = getFirstMissingPart(load);
        requestDownload(load.getFilename(), part);
    }

    private void requestDownload(String filename, int part) {
        FileMessage filemsg = new FileMessage();
        filemsg.setFilename(filename);
        filemsg.setPart(part);
        Load dl = findDownload(filename);
        int finished = 0;
        if (dl != null) finished = dl.getFinishedParts();
        filemsg.setMessage(ByteBufferUtilities.getByteArrayFromInt(finished));
        Message msg = new Message(filemsg.getSerializedMessage(), (byte) 1, FilePlugin.FileGUID);
        try {
            connection.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private Load findDownload(String filename) {
        for (Load load : downloads) {
            if (load.getFilename().equals(filename)) return load;
        }
        return null;
    }

    private Load findUpload(String filename) {
        for (Load load : uploads) {
            if (load.getFilename().equals(filename)) return load;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void loadLoads() {
        try {
            if (fileLoads.exists()) {
                FileInputStream stream = new FileInputStream(fileLoads);
                ObjectInputStream in = new ObjectInputStream(stream);
                downloads = (ArrayList<Load>) in.readObject();
                uploads = (ArrayList<Load>) in.readObject();
                in.close();
                stream.close();
                for (Load upload : uploads) lbUploads.addLoad(upload);
                for (Load download : downloads) lbUploads.addLoad(download);
            }
            File[] files = fileParts.listFiles();
            String[] strLoadHashes = new String[downloads.size()];
            for (int i = 0; i < strLoadHashes.length; i++) strLoadHashes[i] = downloads.get(i).getHashedFilename().substring(0, 15);
            for (File file : files) {
                boolean foundFile = false;
                for (String hash : strLoadHashes) {
                    if (file.getName().startsWith(hash)) foundFile = true;
                }
                if (!foundFile) file.delete();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveLoads() {
        try {
            FileOutputStream stream = new FileOutputStream(fileLoads);
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(downloads);
            out.writeObject(uploads);
            out.close();
            stream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addMessage(Message filemsg) {
        switch(filemsg.getMessageType()) {
            case 0:
                FileShareDirectory dir = FileShareDirectory.deserialize(filemsg.getMessage());
                if (!checkValidShare(dir)) return;
                parentDirectory = dir;
                browserShares.setParentDirectory(dir);
                FileOutputStream output;
                try {
                    output = new FileOutputStream(fileShares);
                    output.write(parentDirectory.serialize());
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                FileMessage msg = FileMessage.loadSerializedMessage(filemsg.getMessage());
                Load upload = findUpload(msg.getFilename());
                if (upload == null) {
                    String requestedFile = msg.getFilename();
                    FileShareFile fileShared = isShared(requestedFile);
                    if (fileShared == null || !fileShared.getShareUsername().equals(MainWindow.getUsername())) return;
                    Load load = new Load();
                    load.setFilename(fileShared.getFilename());
                    load.setDownload(false);
                    uploads.add(load);
                    lbUploads.addLoad(load);
                    upload = load;
                    saveLoads();
                }
                if (msg.getPart() == -1 && upload != null) {
                    upload.setFinished(true);
                    upload.setFinishedParts(upload.getPartsMax());
                    MainWindow.addTelegraph(new Telegraph(Messages.getString("FileWindow.UploadCompletedMsg").replace("$FILE$", msg.getFilename()).replace("$CONTACT$", username), Messages.getString("FileWindow.UploadCompleted"), TelegraphType.LOAD_UPLOAD, WindowPosition.TOPRIGHT, 4000));
                    saveLoads();
                    return;
                }
                if (upload.isPaused()) return;
                upload = findUpload(msg.getFilename());
                if (upload != null) {
                    int finished = ByteBufferUtilities.getIntFromByteArray(msg.getMessage());
                    int offset = partsLength * msg.getPart();
                    try {
                        File fileInput = new File(upload.getFilename());
                        FileInputStream inputstream = new FileInputStream(fileInput);
                        inputstream.skip(offset);
                        byte[] part = new byte[partsLength];
                        int read = inputstream.read(part);
                        if (read == -1) {
                            part = new byte[0];
                        } else {
                            if (read < part.length) {
                                byte[] npart = new byte[read];
                                System.arraycopy(part, 0, npart, 0, read);
                                part = npart;
                            }
                        }
                        long length = fileInput.length();
                        msg.setPartMax((int) Math.ceil((double) length / (double) partsLength));
                        msg.setMessage(part);
                        Message sendmsg = new Message(msg.getSerializedMessage(), (byte) 3, FilePlugin.FileGUID);
                        try {
                            connection.sendMessage(sendmsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        upload.setFinishedParts(finished);
                        upload.setPartsMax(msg.getPartMax());
                        upload.setCounterPerS(upload.getPartsMax() + read);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 3:
                msg = FileMessage.loadSerializedMessage(filemsg.getMessage());
                Load download = findDownload(msg.getFilename());
                if (download != null) {
                    download.setPartsMax(msg.getPartMax());
                    download.setCounterPerS(download.getPartsMax() + msg.getMessage().length);
                    download.setFinishedParts(msg.getPart());
                    int part = getFirstMissingPart(download);
                    if (msg.getMessage().length == 0) part = -1;
                    String filetitle = getFileTitle(download);
                    try {
                        FileOutputStream outputstream = new FileOutputStream(fileDownloads + "/" + filetitle, true);
                        outputstream.write(msg.getMessage(), 0, msg.getMessage().length);
                        outputstream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (part == -1) {
                        MainWindow.addTelegraph(new Telegraph(Messages.getString("FileWindow.DownloadCompletedMsg").replace("$FILE$", filetitle).replace("$CONTACT$", username), Messages.getString("FileWindow.DownloadCompleted"), TelegraphType.LOAD_DOWNLOAD, WindowPosition.TOPRIGHT, 4000));
                        download.setFinished(true);
                        requestDownload(msg.getFilename(), -1);
                        download.setFinished(true);
                    } else {
                        if (!download.isPaused()) requestDownload(msg.getFilename(), part);
                    }
                    saveLoads();
                }
                break;
        }
    }

    private String getFileTitle(Load load) {
        String filetitle = load.getFilename();
        int c = filetitle.lastIndexOf("/");
        if (c != -1) filetitle = filetitle.substring(c + 1);
        c = filetitle.lastIndexOf("\\");
        if (c != -1) filetitle = filetitle.substring(c + 1);
        return filetitle;
    }

    private int getFirstMissingPart(Load download) {
        return download.getFinishedParts() + 1;
    }

    private FileShareFile isShared(String requestedFile) {
        return isShared(requestedFile, parentDirectory);
    }

    private FileShareFile isShared(String requestedFile, FileShareDirectory dir) {
        for (FileShareDirectory cdir : dir.getSubdirs()) {
            FileShareFile file = isShared(requestedFile, cdir);
            if (file != null) return file;
        }
        for (FileShareFile file : dir.getFiles()) {
            if (file.getFilename().equals(requestedFile) && file.getShareUsername().equals(MainWindow.getUsername())) return file;
        }
        return null;
    }

    private boolean checkValidShare(FileShareDirectory dir) {
        for (FileShareDirectory subdir : dir.getSubdirs()) {
            if (!checkValidShare(subdir)) return false;
        }
        for (FileShareFile subdir : dir.getFiles()) {
            if (subdir.getShareUsername().equals(MainWindow.getUsername()) && isShared(subdir.getFilename()) == null) return false;
        }
        return true;
    }
}
