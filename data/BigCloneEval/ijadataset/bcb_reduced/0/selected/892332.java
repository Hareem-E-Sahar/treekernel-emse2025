package com.bbn.vessel.author.workspace;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.JDOMException;
import com.bbn.vessel.author.util.IOHelper;
import com.bbn.vessel.core.util.XMLHelper;

/**
 * this file manages the contents of a single folder
 *
 * @author jostwald
 *
 */
public class FolderFileManager implements FileManager {

    private static final String STARTUP_DIRECTORY_FILENAME = "startupDirectory.txt";

    private static final String RECENT_WORKSPACES_FILENAME = "recentWorkspaces.xml";

    static final String README_FILE_NAME = "vessel-readme.txt";

    static final String README_FILE_CONTENTS = "This folder contains" + " the the data of a VESSEL workspace.  Use caution if modifying " + "it by hand.";

    private static final String DEFAULTS_FOLDER = "data/defaults/";

    private static final String STATIC_FOLDER = "static-data/";

    private static final int MAX_RECENT_FILES = 5;

    private static final String TAG_RECENT_WORKSPACES = "recent-workspaces";

    private static final String TAG_WORKSPACE = "workspace";

    private static final Logger logger = Logger.getLogger(FolderFileManager.class);

    protected File folder;

    private static Vector<File> recentWorkspaces;

    private static JFileChooser fileChooser;

    /**
     * construct a folderFileManager
     */
    public FolderFileManager() {
        if (fileChooser == null) {
            File currentDirectory = getStartupCurrentDirectory();
            fileChooser = new JFileChooser(currentDirectory);
            fileChooser.setFileView(new VesselFileView());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        if (recentWorkspaces == null) {
            readInRecentWorkspaces();
        }
    }

    @Override
    public List<String> listFiles() throws IOException {
        if (folder == null) {
            return Collections.emptyList();
        }
        ArrayList<String> list = new ArrayList<String>();
        File[] listFiles = folder.listFiles();
        for (File f : listFiles) {
            list.add(f.getName());
        }
        return list;
    }

    @Override
    public Element load(String fileName, DocType... docTypes) throws JDOMException, IOException {
        if (!fileName.endsWith(".xml")) {
            fileName += ".xml";
        }
        FileInputStream fis = new FileInputStream(new File(folder, fileName));
        Element rootElement = XMLHelper.getRoot(fis, docTypes);
        fis.close();
        return rootElement;
    }

    @Override
    public Element loadDefault(String fileName, DocType... docTypes) throws IOException, JDOMException {
        if (!fileName.endsWith(".xml")) {
            fileName += ".xml";
        }
        FileInputStream fis = new FileInputStream(new File(DEFAULTS_FOLDER, fileName));
        Element rootElement = XMLHelper.getRoot(fis, docTypes);
        fis.close();
        return rootElement;
    }

    @Override
    public void importDefault(String fileName) throws IOException {
        if (!fileName.endsWith(".xml")) {
            fileName += ".xml";
        }
        FileInputStream is = new FileInputStream(new File(DEFAULTS_FOLDER, fileName));
        importFile(fileName, is);
        is.close();
    }

    @Override
    public void save(String fileName, Element rootElement, DocType docType) throws IOException {
        if (!fileName.endsWith(".xml")) {
            fileName += ".xml";
        }
        if (!folder.exists()) {
            folder.mkdirs();
            ByteArrayInputStream is = new ByteArrayInputStream(README_FILE_CONTENTS.getBytes());
            writeFileToFolderFromStream(README_FILE_NAME, is);
            is.close();
        }
        IOHelper.safeWriteXML(fileName, rootElement, folder, docType);
    }

    static boolean containsReadme(File f) {
        if (f.isDirectory()) {
            for (File f2 : f.listFiles()) {
                if (f2.getName().equals(FolderFileManager.README_FILE_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    void importFile(String fileName, InputStream is) throws IOException {
        if (!fileName.endsWith(".xml")) {
            fileName += ".xml";
        }
        writeFileToFolderFromStream(fileName, is);
    }

    private void writeFileToFolderFromStream(String fileName, InputStream is) throws FileNotFoundException, IOException, SyncFailedException {
        File folderToWrite = folder;
        IOHelper.safeWrite(fileName, is, folderToWrite);
    }

    @Override
    public boolean letUserSelectDataSource(Component dialogParent, String promptText) throws IOException {
        int option;
        if (promptText.equalsIgnoreCase("open")) {
            option = fileChooser.showOpenDialog(dialogParent);
        } else if (promptText.equalsIgnoreCase("save")) {
            option = fileChooser.showSaveDialog(dialogParent);
        } else {
            option = fileChooser.showDialog(dialogParent, promptText);
        }
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            setFolder(selectedFile);
            writeOutStartupCurrentDirectory(selectedFile.getParentFile());
            addRecentWorkspace(selectedFile);
            return true;
        } else {
            return false;
        }
    }

    /**
     * adds a workspace to the recentWorkspaces list (or moves it to the top if
     * it's already there
     *
     * @param w
     *            a workspace folder
     */
    public void addRecentWorkspace(File w) {
        if (recentWorkspaces.contains(w)) {
            recentWorkspaces.remove(w);
        }
        recentWorkspaces.add(0, w);
        while (recentWorkspaces.size() > MAX_RECENT_FILES) {
            recentWorkspaces.remove(recentWorkspaces.size() - 1);
        }
        writeOutRecentWorkspaces();
    }

    private void writeOutRecentWorkspaces() {
        Element workspacesElt = new Element(TAG_RECENT_WORKSPACES);
        for (File f : recentWorkspaces) {
            XMLHelper.addStringElement(workspacesElt, TAG_WORKSPACE, f.getPath());
        }
        try {
            IOHelper.safeWriteXML(RECENT_WORKSPACES_FILENAME, workspacesElt, new File(STATIC_FOLDER), null);
        } catch (IOException e) {
            logger.error("couldn't write out recent files", e);
        }
    }

    /**
     *
     * @return recently ordered workspaces, in order of decreasing recency
     */
    public List<File> getRecentWorkspaces() {
        return Collections.unmodifiableList(recentWorkspaces);
    }

    private void readInRecentWorkspaces() {
        recentWorkspaces = new Vector<File>();
        boolean havePrunedAny = false;
        try {
            FileInputStream fis = new FileInputStream(new File(STATIC_FOLDER, RECENT_WORKSPACES_FILENAME));
            Element root = XMLHelper.getRoot(fis);
            for (Element workspaceElement : XMLHelper.getChildren(root, TAG_WORKSPACE)) {
                File workspaceFile = new File(workspaceElement.getText());
                if (workspaceFile.exists()) {
                    recentWorkspaces.add(workspaceFile);
                } else {
                    havePrunedAny = true;
                }
            }
        } catch (IOException e) {
        } catch (JDOMException e) {
            logger.warn("recent workspaces file corrupted, so ignoring them");
        }
        if (havePrunedAny) {
            writeOutRecentWorkspaces();
        }
    }

    private void writeOutStartupCurrentDirectory(File file) {
        try {
            IOHelper.safeWrite(STARTUP_DIRECTORY_FILENAME, new ByteArrayInputStream(file.getPath().getBytes()), new File(STATIC_FOLDER));
        } catch (IOException e) {
            logger.error("couldn't write out startup dir", e);
        }
    }

    /**
     * read in the current directory so that we can set up the jfileChooser
     *
     * @return
     */
    private File getStartupCurrentDirectory() {
        File toReturn = new File("data");
        try {
            InputStream inputStream = new FileInputStream(new File(STATIC_FOLDER, STARTUP_DIRECTORY_FILENAME));
            toReturn = new File(new String(IOHelper.inputStreamToByteArray(inputStream)));
            inputStream.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return toReturn;
    }

    @Override
    public void setDataSource(String datasource) throws IOException {
        File dataSourceFolder = new File(datasource);
        setFolder(dataSourceFolder);
    }

    private void setFolder(File newFolder) throws IOException {
        if (newFolder.exists() && (!newFolder.isDirectory())) {
            throw new IllegalArgumentException("file " + newFolder + " is not a folder");
        }
        this.folder = newFolder;
    }

    @Override
    public boolean hasDataSource() {
        return folder != null;
    }

    @Override
    public void unSetDataSource() {
        folder = null;
    }

    @Override
    public boolean dataSourceExists() {
        return ((folder != null) && (folder.exists()));
    }

    @Override
    public boolean clearDataSourceWithUserConfirm(Component dialogParent) throws IOException {
        int overwriteOption = JOptionPane.showConfirmDialog(dialogParent, "File " + folder.getName() + " exists.  Overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION);
        if (overwriteOption == JOptionPane.NO_OPTION) {
            return false;
        }
        if (!IOHelper.recursivelyDeleteFile(folder)) {
            throw new IOException("couldn't delete " + folder);
        }
        return true;
    }

    @Override
    public String getDataSourcePrettyName() {
        return folder == null ? null : folder.getName();
    }

    /**
     * @see com.bbn.vessel.author.workspace.FileManager#canRead(java.lang.String)
     */
    @Override
    public boolean canRead(String fileName) {
        return folder != null && new File(folder, fileName).canRead();
    }
}
