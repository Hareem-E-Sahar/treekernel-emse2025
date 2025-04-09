package com.ivis.xprocess.framework.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ivis.xprocess.framework.XchangeElement;
import com.ivis.xprocess.framework.XchangeElementContainer;
import com.ivis.xprocess.framework.Xelement;
import com.ivis.xprocess.framework.exceptions.ContainerUnavailableException;
import com.ivis.xprocess.framework.vcs.VcsProvider;
import com.ivis.xprocess.framework.vcs.exceptions.VCSException;
import com.ivis.xprocess.framework.vcs.impl.svn.SVNEventObserver;
import com.ivis.xprocess.framework.vcs.impl.svn.SubversionProvider.PathChangeType;
import com.ivis.xprocess.util.FileUtils;
import com.ivis.xprocess.util.UuidUtils;

/**
 * The internal data model built from the files on the file system.
 *
 */
public class FileIndex implements IFileIndex, SVNEventObserver {

    private static final Logger logger = Logger.getLogger(FileIndex.class.getName());

    private boolean validIndex = false;

    private int openDirLength;

    private IPersistenceHelper ph;

    private Map<String, String> idToPath = Collections.synchronizedMap(new HashMap<String, String>());

    private Map<String, String> idToForwarderPath = Collections.synchronizedMap(new HashMap<String, String>());

    public FileIndex(IPersistenceHelper persistenceHelper) {
        this.ph = persistenceHelper;
    }

    public synchronized Set<String> index() {
        Set<String> containerIds = null;
        synchronized (idToPath) {
            invalidateFileIndex();
            try {
                openDirLength = ph.getDataLayout().getOpenDir().getCanonicalPath().length();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error resolving path", e);
            }
            containerIds = doIndex();
            validIndex = true;
        }
        return containerIds;
    }

    private Set<String> doIndex() {
        File directory = ph.getDataLayout().getOpenDir();
        if (!directory.exists()) {
            return null;
        }
        for (File xmlFile : directory.listFiles(XMLFilenameFilter.getInstance())) {
            indexXMLFile(xmlFile);
        }
        Set<String> containerIds = new HashSet<String>(idToPath.keySet());
        Set<String> svnBasedContainersWithoutXPXs = new HashSet<String>();
        for (File subDirectory : directory.listFiles(DirectoryFilter.getInstance())) {
            String pathToXPXFile = subDirectory.getAbsolutePath() + XMLifier.XML_EXTENSION;
            File XPXFile = new File(pathToXPXFile);
            if (!XPXFile.exists() && subDirectory.getName().length() == 18) {
                if (ph.getDataSource().getVcsProvider() != null) {
                    logger.log(Level.WARNING, "Found a container that does not have an XPX file - " + subDirectory + ". As this is a VCS based datasource, to fix the issue the container is being deleted.");
                    svnBasedContainersWithoutXPXs.add(subDirectory.getAbsolutePath());
                } else {
                    logger.log(Level.WARNING, "Found a container that does not have an XPX file - " + subDirectory + ". Container not being loaded");
                    indexDirectory(subDirectory);
                }
            } else {
                indexDirectory(subDirectory);
            }
        }
        if (svnBasedContainersWithoutXPXs.size() > 0) {
            for (String pathToDelete : svnBasedContainersWithoutXPXs) {
                File file = new File(pathToDelete);
                if (file.isDirectory()) {
                    boolean hasSvnDir = false;
                    for (String filename : file.list()) {
                        if (filename.equals(".svn")) {
                            hasSvnDir = true;
                        }
                    }
                    if (!hasSvnDir) {
                        FileUtils.deleteDir(file);
                    }
                }
            }
        }
        return containerIds;
    }

    public Set<String> getPathsForContainerContents(XchangeElementContainer container) throws ContainerUnavailableException {
        String containerXpxPath = idToPath.get(container.getId());
        String containerDir = containerXpxPath.substring(0, containerXpxPath.length() - 4);
        File containerFile = new File(containerDir);
        if (!containerFile.exists()) {
            throw new ContainerUnavailableException(container);
        }
        Set<String> containerPaths = new HashSet<String>();
        synchronized (idToPath) {
            for (String path : idToPath.values()) {
                if (!path.equals(containerXpxPath) && path.startsWith(containerDir)) {
                    containerPaths.add(path);
                }
            }
        }
        return containerPaths;
    }

    private void indexDirectory(File directory) {
        try {
            if (!directory.exists()) {
                return;
            }
            for (File subDirectory : directory.listFiles(DirectoryFilter.getInstance())) {
                indexDirectory(subDirectory);
            }
            for (File xmlFile : directory.listFiles(XMLFilenameFilter.getInstance())) {
                indexXMLFile(xmlFile);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error indexing directory", e);
        }
    }

    private void indexXMLFile(File xpxFile) {
        String absPath = xpxFile.getAbsolutePath();
        if (absPath.endsWith(XMLifier.XML_EXTENSION)) {
            idToPath.put(UuidUtils.getIdfromPath(absPath), absPath);
        } else {
            idToForwarderPath.put(UuidUtils.getIdfromPath(absPath), absPath);
        }
    }

    public synchronized void addElement(String id, String fullXmlPath) {
        synchronized (idToPath) {
            idToPath.put(id, fullXmlPath);
        }
    }

    private void invalidateFileIndex() {
        idToPath.clear();
        idToForwarderPath.clear();
        validIndex = false;
    }

    public String getRelativePath(File xpx) {
        try {
            return xpx.getCanonicalPath().substring(openDirLength);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error resolving path", e);
            return null;
        }
    }

    public Collection<String> getForwarderPaths() {
        return new ArrayList<String>(idToForwarderPath.values());
    }

    /**
     * @return a Set containing all ids in the index The XchangeElements with
     *         these ids may not be loaded
     */
    public Set<String> getIDs() {
        return new HashSet<String>(idToPath.keySet());
    }

    public boolean hasElement(String id) {
        return idToPath.containsKey(id);
    }

    /**
     * Returns the uuid of an xelement's container The rule is that every
     * xelement is represented in the open dir fragment If the uuid for a top
     * level ExchgangeElementContainer is passed, null returned.
     *
     * @param id
     * @return the container uuid
     */
    public String getContainerUUID(String id) {
        String path = idToPath.get(id);
        if (path == null) {
            String debugProperty = System.getProperty("debug");
            if ((debugProperty != null) && debugProperty.toLowerCase().equals("true")) {
                logger.log(Level.WARNING, "[FileIndex - getContainerUUID()] NULL path.... - " + id, new RuntimeException());
            }
            return null;
        }
        String[] uuids = UuidUtils.getUuidsFromPath(path);
        if (!id.equals(uuids[0])) {
            return uuids[0];
        } else {
            if (uuids.length == 1) {
                return null;
            }
            return uuids[1];
        }
    }

    /**
     * @param id
     * @return path to the XPX file in the open fragment - operation could do
     *         with a better name!
     */
    public String getOpenPathFragment(String id) {
        if (!validIndex) {
            throw new RuntimeException("The index is not valid");
        }
        String fullPath = idToPath.get(id);
        if (fullPath == null) {
            return null;
        }
        return fullPath.substring(openDirLength, fullPath.length());
    }

    public synchronized void deleteXML(Xelement xelement) {
        String fullContainerPath;
        File fileToDelete = null;
        String forwarderPath;
        File dirToDelete = null;
        File forwarderToDelete = null;
        VcsProvider vcsp = ph.getDataSource().getVcsProvider();
        String fullXMLPath = getFullPath(xelement.getId());
        if (fullXMLPath == null) {
            fullXMLPath = getFullForwarderPath(xelement.getId());
        }
        if (fullXMLPath != null) {
            fileToDelete = new File(fullXMLPath);
            if (xelement instanceof XchangeElementContainer) {
                fullContainerPath = ph.getDataSource().getLocalRootDirectory() + File.separator + "open" + File.separator + xelement.getId();
                dirToDelete = new File(fullContainerPath);
            } else if (xelement instanceof XchangeElement) {
                dirToDelete = null;
                forwarderPath = idToForwarderPath.get(xelement.getId());
                if (forwarderPath != null) {
                    forwarderToDelete = new File(forwarderPath);
                }
            } else if (xelement instanceof Xelement) {
                dirToDelete = null;
                fileToDelete = null;
            }
            if (vcsp != null) {
                try {
                    if (dirToDelete != null) {
                        vcsp.delete(dirToDelete);
                    }
                    if ((fileToDelete != null) && fileToDelete.exists()) {
                        vcsp.delete(fileToDelete);
                    }
                    if ((forwarderToDelete != null) && forwarderToDelete.exists()) {
                        vcsp.delete(forwarderToDelete);
                    }
                } catch (VCSException e) {
                    logger.log(Level.WARNING, "VCS operation failed in deleteXML", e);
                }
            } else {
                if (fileToDelete != null) {
                    fileToDelete.delete();
                }
                if (forwarderToDelete != null) {
                    boolean successful = forwarderToDelete.delete();
                    if (!successful) {
                        logger.log(Level.SEVERE, "Error: XPX still exists after deletion - " + forwarderToDelete);
                    }
                }
                if (dirToDelete != null) {
                    boolean successful = FileUtils.deleteDir(dirToDelete);
                    if (!successful) {
                        logger.log(Level.SEVERE, "Error: XPX still exists after deletion - " + dirToDelete);
                    }
                }
            }
        }
        synchronized (idToPath) {
            idToPath.remove(xelement.getId());
        }
        synchronized (idToForwarderPath) {
            idToForwarderPath.remove(xelement.getId());
        }
    }

    public String dumpContents() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : idToPath.entrySet()) {
            sb.append("\n\t UUID " + entry.getKey() + " Path " + entry.getValue());
        }
        return sb.toString();
    }

    public Collection<String> getPaths(Xelement xelement) {
        Collection<String> paths = new ArrayList<String>();
        String path = getOpenPathFragment(xelement.getId());
        if (path != null) {
            paths.add("open" + path);
        }
        return paths;
    }

    public synchronized void remove(String uuid) {
        synchronized (idToPath) {
            idToPath.remove(uuid);
        }
    }

    public synchronized void pathChangeOccurred(PathChangeType changeType, String path) {
        if (changeType == PathChangeType.UPDATE_ADD) {
            if (!path.endsWith(XMLifier.XML_EXTENSION)) {
                return;
            }
            String id = UuidUtils.getIdfromPath(path);
            if ((id != null) && (id.length() > 0)) {
                File f = new File(path);
                if (f.getAbsolutePath().startsWith(ph.getDataSource().getLocalRootDirectory() + File.separator)) {
                    synchronized (idToPath) {
                        idToPath.put(id, f.getAbsolutePath());
                    }
                }
            }
        }
    }

    public String getFullPath(String id) {
        return idToPath.get(id);
    }

    public String getFullForwarderPath(String id) {
        return idToForwarderPath.get(id);
    }

    public String getFullPath(XchangeElement xchangeElement) {
        if (xchangeElement.isForwarder()) {
            return idToForwarderPath.get(xchangeElement.getId());
        } else {
            return idToPath.get(xchangeElement.getId());
        }
    }

    public void addForwarder(String id, String fullXmlPath) {
        synchronized (idToForwarderPath) {
            idToForwarderPath.put(id, fullXmlPath);
        }
    }

    public boolean hasForwarder(String id) {
        return idToForwarderPath.containsKey(id);
    }

    public void removeForwarder(String id) {
        synchronized (idToForwarderPath) {
            idToForwarderPath.remove(id);
        }
    }
}
