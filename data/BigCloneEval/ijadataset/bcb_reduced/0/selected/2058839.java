package ants.p2p.utils.indexer;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.beans.*;
import ants.p2p.*;
import ants.p2p.filesharing.*;
import ants.p2p.gui.*;
import ants.p2p.query.*;
import ants.p2p.utils.indexer.*;
import ants.p2p.utils.encoding.*;
import ants.p2p.utils.addresses.*;
import org.apache.log4j.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;

public class BackgroundEngine extends Thread implements PropertyChangeListener {

    Hashtable partialFiles = new Hashtable();

    Hashtable partialED2KFiles = new Hashtable();

    public Hashtable sharedFilesIndexName = new Hashtable();

    public Hashtable sharedFilesIndexHash = new Hashtable();

    public Hashtable sharedFilesIndexED2KHash = new Hashtable();

    public ArrayList sharedDirectories = new ArrayList();

    public Hashtable nestedDirectories = new Hashtable();

    public Hashtable remoteFilesIndexHash = new Hashtable();

    public Hashtable remoteFilesIndexED2KHash = new Hashtable();

    long totalLocalSharedFileSize = 0;

    long totalRemoteSharedFileSize = 0;

    Hashtable supernodeList = new Hashtable();

    Hashtable httpServers = new Hashtable();

    int uploadListToSupernodes = 3;

    ArrayList lastUsedSuperNodes = new ArrayList();

    int isUploadingFileList = 0;

    public Hashtable lastTimeUploadedFileList = new Hashtable();

    public boolean forceUploadingLists = false;

    Hashtable lastUploadedLists = new Hashtable();

    SupernodeEngine supernodeEngine = new SupernodeEngine(this);

    static BackgroundEngine instance;

    public static int refreshRate = 5000;

    public static int maxRemoteDocsToTrace = 10000;

    public static int broadcastTimeToLive = 2000;

    public static int remoteIndexedDocumentsTimeout = 60 * 60 * 1000;

    public static boolean shareDownloadPath = true;

    File store = new File(WarriorAnt.workingPath + "/sharedFiles.ant");

    File remoteSharedStore = new File(WarriorAnt.workingPath + "/remoteSharedFiles.ant");

    File storeIndex = new File(WarriorAnt.workingPath + "/sharedIndex");

    File remoteStoreIndex = new File(WarriorAnt.workingPath + "/remoteSharedIndex");

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    QueryMessage currentQuery = null;

    WarriorAnt wa = null;

    static Logger _logger = Logger.getLogger(BackgroundEngine.class.getName());

    public static boolean recursiveExplore = true;

    private boolean synchronizingRemote = false;

    private boolean loggingOut = false;

    boolean terminate = false;

    boolean resetCycle = false;

    boolean forceIndexing = false;

    String localIndexMonitor = "";

    public static BackgroundEngine getInstance(File store) {
        try {
            if (instance == null) {
                instance = new BackgroundEngine(store);
                instance.setPriority(1);
                instance.start();
                return instance;
            } else {
                return instance;
            }
        } catch (Exception ex) {
            _logger.error("", ex);
            return null;
        }
    }

    public static BackgroundEngine getInstance() {
        try {
            if (instance == null) {
                instance = new BackgroundEngine();
                instance.start();
                return instance;
            } else {
                return instance;
            }
        } catch (Exception ex) {
            _logger.error("", ex);
            return null;
        }
    }

    public void terminate(boolean join) {
        this.terminate = true;
        if (join) {
            try {
                this.join();
            } catch (InterruptedException ex) {
                _logger.error("BackgroundEngine interrupted", ex);
            }
            BackgroundEngine.instance = null;
        }
    }

    public Hashtable getSupernodeList() {
        return this.supernodeList;
    }

    public Hashtable getHttpServersList() {
        return this.httpServers;
    }

    public long getTotalLocalSharedSize() {
        return this.totalLocalSharedFileSize;
    }

    public long getTotalRemoteSharedSize() {
        return this.totalRemoteSharedFileSize;
    }

    public int getSupernodesSize() {
        return this.supernodeList.size();
    }

    public int getHttpServersSize() {
        return this.httpServers.size();
    }

    public long computeRemoteIndexedSharedSize() {
        long sharedSize = 0;
        Enumeration keys = this.supernodeList.keys();
        while (keys.hasMoreElements()) {
            String curKey = (String) keys.nextElement();
            QuerySupernodeTuple qst = (QuerySupernodeTuple) this.supernodeList.get(curKey);
            if (qst != null) sharedSize += qst.getTotalShareDimension().longValue();
        }
        return sharedSize;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.propertyChangeSupport.addPropertyChangeListener(pcl);
        DigestManager.addPropertyChangeListener(pcl);
        IndexerGraphicEngine.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.propertyChangeSupport.removePropertyChangeListener(pcl);
        DigestManager.removePropertyChangeListener(pcl);
        IndexerGraphicEngine.removePropertyChangeListener(pcl);
    }

    public void addPartialFile(MultipleSourcesDownloadManager msdm) {
        synchronized (this.localIndexMonitor) {
            this.partialFiles.put(msdm.getFileHash(), msdm);
            this.partialED2KFiles.put(msdm.getED2KFileHash(), msdm);
            this.forceUploadingLists = true;
            try {
                IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), false);
                writer.addDocument(PartialFileDocument.Document(msdm));
                writer.close();
            } catch (IOException e) {
                _logger.error("Error in indexing partial file: " + msdm.getFileHash(), e);
            }
        }
    }

    public void addPartialFile(InterruptedDownload id) {
        synchronized (this.localIndexMonitor) {
            this.partialFiles.put(id.getFileHash(), id);
            this.partialED2KFiles.put(id.getED2KFileHash(), id);
            this.forceUploadingLists = true;
            try {
                IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), false);
                writer.addDocument(PartialFileDocument.Document(id));
                writer.close();
            } catch (IOException e) {
                _logger.error("Error in indexing partial file: " + id.getFileHash(), e);
            }
        }
    }

    public void resetPartialFiles() {
        synchronized (this.localIndexMonitor) {
            Enumeration keys = this.partialFiles.keys();
            while (keys.hasMoreElements()) {
                String hash = null;
                try {
                    hash = (String) keys.nextElement();
                    IndexReader reader = IndexReader.open(storeIndex);
                    Term term = new Term("PartialFileHash", hash);
                    int deleted = reader.deleteDocuments(term);
                    reader.close();
                } catch (IOException e) {
                    _logger.error("Error in removing partial file from index: " + hash, e);
                }
            }
            this.partialFiles = new Hashtable();
            this.partialED2KFiles = new Hashtable();
            this.forceUploadingLists = true;
        }
    }

    public void removePartialFile(String hash) {
        synchronized (this.localIndexMonitor) {
            Object pf = this.getPartialFile(hash, QueryHashItem.ANTS_HASH);
            if (pf instanceof MultipleSourcesDownloadManager) this.partialED2KFiles.remove(((MultipleSourcesDownloadManager) pf).getED2KFileHash()); else if (pf instanceof InterruptedDownload) this.partialED2KFiles.remove(((InterruptedDownload) pf).getED2KFileHash());
            this.partialFiles.remove(hash);
            this.forceUploadingLists = true;
            try {
                IndexReader reader = IndexReader.open(storeIndex);
                Term term = new Term("PartialFileHash", hash);
                int deleted = reader.deleteDocuments(term);
                reader.close();
            } catch (IOException e) {
                _logger.error("Error in removing partial file from index: " + hash, e);
            }
        }
    }

    public void addRemoteFile(QueryFileTuple qft) {
        try {
            if (this.remoteFilesIndexHash.size() > BackgroundEngine.maxRemoteDocsToTrace) {
                Enumeration keys = this.remoteFilesIndexHash.keys();
                Object toBeRemoved = keys.nextElement();
                RemoteFileInfos rfiRemoved = (RemoteFileInfos) this.remoteFilesIndexHash.remove(toBeRemoved);
                this.remoteFilesIndexED2KHash.remove(rfiRemoved.getED2KFileHash());
            }
            if (qft instanceof QueryRemoteFileTuple) {
                QueryRemoteFileTuple qrft = (QueryRemoteFileTuple) qft;
                if (qrft.getLastTimeSeen().longValue() > System.currentTimeMillis()) {
                    qrft.resetLastTimeSeen();
                }
            }
            if (this.remoteFilesIndexHash.get(qft.getFileHash()) != null) {
                RemoteFileInfos localCached = (RemoteFileInfos) this.remoteFilesIndexHash.get(qft.getFileHash());
                if (localCached.getOwners().get(qft.getOwnerID()) == null) {
                    if (qft instanceof QueryRemoteFileTuple) {
                        QueryRemoteFileTuple qrft = (QueryRemoteFileTuple) qft;
                        localCached.getOwners().put(qrft.getOwnerID(), qrft.getLastTimeSeen());
                    } else {
                        localCached.getOwners().put(qft.getOwnerID(), new Long(System.currentTimeMillis()));
                    }
                } else {
                    Long lastTimeSeen = (Long) localCached.getOwners().get(qft.getOwnerID());
                    if (qft instanceof QueryRemoteFileTuple) {
                        QueryRemoteFileTuple qrft = (QueryRemoteFileTuple) qft;
                        if (lastTimeSeen.longValue() < qrft.getLastTimeSeen().longValue()) localCached.getOwners().put(qrft.getOwnerID(), qrft.getLastTimeSeen());
                    } else {
                        localCached.getOwners().put(qft.getOwnerID(), new Long(System.currentTimeMillis()));
                    }
                    _logger.debug("Added remote source [" + qft.getOwnerID().substring(0, 10) + "] " + qft.getFileName());
                }
                this.remoteFilesIndexHash.put(localCached.getHash(), localCached);
                this.remoteFilesIndexED2KHash.put(localCached.getED2KFileHash(), localCached);
            } else {
                RemoteFileInfos localCached = new RemoteFileInfos(qft);
                this.remoteFilesIndexHash.put(localCached.getHash(), localCached);
                this.remoteFilesIndexED2KHash.put(localCached.getED2KFileHash(), localCached);
                _logger.debug("Added new remote file [" + qft.getOwnerID().substring(0, 10) + "] " + qft.getFileName());
            }
        } catch (Exception ex) {
            _logger.error("Cannot add the remote file", ex);
        }
    }

    public Enumeration getPartialFiles() {
        return this.partialFiles.keys();
    }

    public ArrayList getPartialFilesHashes() {
        ArrayList hashes = new ArrayList();
        Enumeration keys = this.partialFiles.keys();
        while (keys.hasMoreElements()) {
            hashes.add(new StringHash((String) keys.nextElement()));
        }
        return hashes;
    }

    public Object getPartialFile(String hash, String hashType) {
        if (hashType.equals(QueryHashItem.ANTS_HASH)) return this.partialFiles.get(hash); else if (hashType.equals(QueryHashItem.ED2K_HASH)) return this.partialED2KFiles.get(hash); else return null;
    }

    public RemoteFileInfos getRemoteFile(String hash, String hashType) {
        if (hashType.equals(QueryHashItem.ANTS_HASH)) return (RemoteFileInfos) this.remoteFilesIndexHash.get(hash); else if (hashType.equals(QueryHashItem.ED2K_HASH)) return (RemoteFileInfos) this.remoteFilesIndexED2KHash.get(hash); else return null;
    }

    public FileInfos getLocalFile(String hash, String hashType) {
        if (hashType.equals(QueryHashItem.ANTS_HASH)) return this.getLocalFileANts(hash); else if (hashType.equals(QueryHashItem.ED2K_HASH)) return this.getLocalFileED2K(hash); else return null;
    }

    private FileInfos getLocalFileANts(String hash) {
        return (FileInfos) this.sharedFilesIndexHash.get(hash);
    }

    private FileInfos getLocalFileED2K(String hash) {
        return (FileInfos) this.sharedFilesIndexED2KHash.get(hash);
    }

    public QueryPartialFileTuple getPartialFileTuple(String sessionKey, String fileHash, String type, String ownerID, Integer freeSlots, String connectionType, boolean getChunkHashes) {
        Object partialFile = this.getPartialFile(fileHash, type);
        String ed2kFileHash = null;
        Object[] chunkHashes = null;
        String fileName = null;
        Long fileLength = null;
        Integer blockSize = null;
        Integer blocksPerSource = null;
        String percentage = null;
        String extendedInfos = null;
        boolean[] downloadedBlockGroups = null;
        if (partialFile instanceof MultipleSourcesDownloadManager) {
            MultipleSourcesDownloadManager msdmPartialFile = (MultipleSourcesDownloadManager) partialFile;
            ed2kFileHash = msdmPartialFile.getED2KFileHash();
            chunkHashes = msdmPartialFile.getChunkHashes();
            fileName = msdmPartialFile.getFileName();
            fileLength = new Long(msdmPartialFile.getFileSize());
            blockSize = new Integer(msdmPartialFile.getBlockSize());
            blocksPerSource = new Integer(MultipleSourcesDownloadManager.blocksPerSource);
            downloadedBlockGroups = msdmPartialFile.getDownloadedBlockGroups();
            percentage = msdmPartialFile.getPercentage();
            extendedInfos = msdmPartialFile.getExtendedInfos();
        } else if (partialFile instanceof InterruptedDownload) {
            InterruptedDownload idPartialFile = (InterruptedDownload) partialFile;
            ed2kFileHash = idPartialFile.getED2KFileHash();
            chunkHashes = idPartialFile.getChunkHashes();
            fileName = idPartialFile.getFileName();
            fileLength = new Long(idPartialFile.getFileSize());
            blockSize = new Integer(idPartialFile.getBlockSize());
            blocksPerSource = new Integer(MultipleSourcesDownloadManager.blocksPerSource);
            downloadedBlockGroups = idPartialFile.getDownloadedBlockGroups();
            percentage = idPartialFile.getPercentage();
            extendedInfos = idPartialFile.getExtendedInfos();
        }
        if (fileName != null && fileLength != null && blockSize != null && downloadedBlockGroups != null && percentage != null) {
            return new QueryPartialFileTuple(sessionKey, fileName, fileHash, ed2kFileHash, getChunkHashes ? chunkHashes : null, fileLength, blockSize, blocksPerSource, downloadedBlockGroups, ownerID, wa.getLocalInetAddress(), freeSlots, connectionType, percentage, extendedInfos);
        } else {
            throw new NullPointerException("Null pointer in QueryPartialFileTuple parameters");
        }
    }

    public void synchronizeLocalIndex(boolean optimize, boolean showExit) throws Exception {
        try {
            synchronized (this.localIndexMonitor) {
                IndexReader reader = IndexReader.open(storeIndex);
                ArrayList filesArray = new ArrayList();
                Enumeration filesEnum = this.sharedFilesIndexName.keys();
                while (filesEnum.hasMoreElements()) {
                    filesArray.add(filesEnum.nextElement());
                }
                for (int i = 0; i < reader.maxDoc() && !this.terminate; i++) {
                    if (reader.isDeleted(i)) continue;
                    Document document = reader.document(i);
                    String path = document.get("Path");
                    if (!filesArray.contains(path)) {
                        reader.deleteDocument(i);
                    } else {
                        filesArray.remove(path);
                    }
                }
                reader.close();
                IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), false);
                for (int i = 0; i < filesArray.size() && !this.terminate; i++) {
                    File curFile = new File((String) filesArray.get(i));
                    FileInfos fI = (FileInfos) this.sharedFilesIndexName.get(filesArray.get(i));
                    if (curFile.exists() && curFile.isFile()) writer.addDocument(FileDocument.Document(fI));
                }
                if (optimize) writer.optimize();
                writer.close();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void synchronizeRemoteIndex(boolean showFrame) throws Exception {
        synchronized (this) {
            if (this.loggingOut) return;
            while (this.synchronizingRemote) {
                this.wait();
            }
            this.synchronizingRemote = true;
        }
        IndexReader reader = null;
        IndexWriter writer = null;
        try {
            reader = IndexReader.open(remoteStoreIndex);
            ArrayList remoteFilesArray = new ArrayList();
            Enumeration remoteFilesEnum = this.remoteFilesIndexHash.keys();
            while (remoteFilesEnum.hasMoreElements()) {
                remoteFilesArray.add(remoteFilesEnum.nextElement());
            }
            for (int i = 0; i < reader.maxDoc() && !this.terminate; i++) {
                if (reader.isDeleted(i)) continue;
                Document document = reader.document(i);
                String remoteHash = document.get("RemoteFileHash");
                if (!remoteFilesArray.contains(remoteHash) || !((RemoteFileInfos) this.remoteFilesIndexHash.get(remoteHash)).hasSources(BackgroundEngine.remoteIndexedDocumentsTimeout)) {
                    reader.deleteDocument(i);
                    RemoteFileInfos rfi = (RemoteFileInfos) this.remoteFilesIndexHash.get(remoteHash);
                    if (rfi != null) {
                        this.remoteFilesIndexHash.remove(remoteHash);
                        this.remoteFilesIndexED2KHash.remove(rfi.getED2KFileHash());
                    }
                }
                remoteFilesArray.remove(remoteHash);
            }
            reader.close();
            writer = new IndexWriter(remoteStoreIndex, new StandardAnalyzer(), false);
            for (int i = 0; i < remoteFilesArray.size() && !this.terminate; i++) {
                RemoteFileInfos rfi = (RemoteFileInfos) this.remoteFilesIndexHash.get(remoteFilesArray.get(i));
                if (rfi != null && rfi.hasSources(BackgroundEngine.remoteIndexedDocumentsTimeout)) writer.addDocument(RemoteFileDocument.Document(rfi)); else {
                    this.remoteFilesIndexHash.remove(remoteFilesArray.get(i));
                    this.remoteFilesIndexED2KHash.remove(rfi.getED2KFileHash());
                }
            }
            if (showFrame) writer.optimize();
            writer.close();
            this.storeRemote(remoteSharedStore);
            synchronized (this) {
                this.synchronizingRemote = false;
                this.notifyAll();
            }
        } catch (Exception e) {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            synchronized (this) {
                this.synchronizingRemote = false;
                this.notifyAll();
            }
            throw e;
        }
    }

    private void showErrorFrame() {
        final JFrame connectionDialog = new JFrame("ANts Fatal Error");
        connectionDialog.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER));
        connectionDialog.getContentPane().add(new JLabel("A former ANts intance wasn't able to clear lucene cache, you need to restart ANts."));
        JButton confirmConnection = new JButton("Ok");
        confirmConnection.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connectionDialog.setVisible(false);
            }
        });
        connectionDialog.getContentPane().add(confirmConnection);
        connectionDialog.pack();
        connectionDialog.setLocation(300, 300);
        connectionDialog.setVisible(true);
        while (connectionDialog.isVisible()) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex1) {
            }
        }
    }

    private void initializeIndexes() {
        if (store.exists() && IndexReader.indexExists(storeIndex)) {
            try {
                load(store);
                this.synchronizeLocalIndex(false, true);
            } catch (Exception e) {
                _logger.error("Error in synchronizing index", e);
                try {
                    File[] files = storeIndex.listFiles();
                    for (int x = 0; x < files.length; x++) {
                        boolean deleted = files[x].delete();
                        System.out.println(files[x].getName() + " " + deleted);
                        if (!deleted) files[x].deleteOnExit();
                    }
                    storeIndex.delete();
                    IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), true);
                    writer.optimize();
                    writer.close();
                    this.synchronizeLocalIndex(false, true);
                } catch (Exception ex) {
                    _logger.error("Fatal error in creating index", ex);
                    this.showErrorFrame();
                    System.exit(0);
                }
            }
        } else if (store.exists() && !IndexReader.indexExists(storeIndex)) {
            try {
                IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), true);
                writer.optimize();
                writer.close();
            } catch (IOException e) {
                _logger.error("Cannot create index", e);
                System.exit(0);
            }
            try {
                load(store);
                this.synchronizeLocalIndex(false, true);
            } catch (Exception e) {
                _logger.error("Error in synchronizing index", e);
                try {
                    File[] files = storeIndex.listFiles();
                    for (int x = 0; x < files.length; x++) {
                        boolean deleted = files[x].delete();
                        System.out.println(files[x].getName() + " " + deleted);
                        if (!deleted) files[x].deleteOnExit();
                    }
                    storeIndex.delete();
                    IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), true);
                    writer.optimize();
                    writer.close();
                    this.synchronizeLocalIndex(false, true);
                } catch (Exception ex) {
                    _logger.error("Fatal error in creating index", ex);
                    this.showErrorFrame();
                    System.exit(0);
                }
            }
        } else if (!store.exists() && !IndexReader.indexExists(storeIndex) || !store.exists() && IndexReader.indexExists(storeIndex)) {
            try {
                IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), true);
                writer.optimize();
                writer.close();
            } catch (IOException e) {
                _logger.error("Cannot create index", e);
                System.exit(0);
            }
        }
        if (remoteSharedStore.exists() && IndexReader.indexExists(remoteStoreIndex)) {
            try {
                loadRemote(remoteSharedStore);
                this.synchronizeRemoteIndex(true);
            } catch (Exception e) {
                _logger.error("Error in synchronizing index", e);
                try {
                    File[] files = remoteStoreIndex.listFiles();
                    for (int x = 0; x < files.length; x++) {
                        boolean deleted = files[x].delete();
                        System.out.println(files[x].getName() + " " + deleted);
                        if (!deleted) files[x].deleteOnExit();
                    }
                    remoteStoreIndex.delete();
                    IndexWriter writer = new IndexWriter(remoteStoreIndex, new StandardAnalyzer(), true);
                    writer.optimize();
                    writer.close();
                    this.synchronizeRemoteIndex(true);
                } catch (Exception ex) {
                    _logger.error("Fatal error in creating index", ex);
                    this.showErrorFrame();
                    System.exit(0);
                }
            }
        } else if (remoteSharedStore.exists() && !IndexReader.indexExists(remoteStoreIndex)) {
            try {
                IndexWriter writer = new IndexWriter(remoteStoreIndex, new StandardAnalyzer(), true);
                writer.optimize();
                writer.close();
            } catch (IOException e) {
                _logger.error("Cannot create index", e);
                System.exit(0);
            }
            try {
                loadRemote(remoteSharedStore);
                this.synchronizeRemoteIndex(true);
            } catch (Exception e) {
                _logger.error("Error in synchronizing index", e);
                try {
                    File[] files = remoteStoreIndex.listFiles();
                    for (int x = 0; x < files.length; x++) {
                        boolean deleted = files[x].delete();
                        System.out.println(files[x].getName() + " " + deleted);
                        if (!deleted) files[x].deleteOnExit();
                    }
                    remoteStoreIndex.delete();
                    IndexWriter writer = new IndexWriter(remoteStoreIndex, new StandardAnalyzer(), true);
                    writer.optimize();
                    writer.close();
                    this.synchronizeRemoteIndex(true);
                } catch (Exception ex) {
                    _logger.error("Fatal error in creating index", ex);
                    this.showErrorFrame();
                    System.exit(0);
                }
            }
        } else if (!remoteSharedStore.exists() && !IndexReader.indexExists(storeIndex) || !remoteSharedStore.exists() && IndexReader.indexExists(storeIndex)) {
            try {
                IndexWriter writer = new IndexWriter(remoteStoreIndex, new StandardAnalyzer(), true);
                writer.optimize();
                writer.close();
            } catch (IOException e) {
                _logger.error("Cannot create index", e);
                System.exit(0);
            }
        }
    }

    public boolean isUploadingFileList() {
        return this.isUploadingFileList > 0;
    }

    public void uploadingFileList() {
        this.isUploadingFileList++;
    }

    public void finishedUploadingFileList() {
        this.isUploadingFileList--;
    }

    public void setLastUploadedList(String dest, ArrayList lastUploadedList) {
        if (lastUploadedList == null) this.lastUploadedLists.remove(dest); else this.lastUploadedLists.put(dest, lastUploadedList);
    }

    public ArrayList getLastUploadedList(String dest) {
        ArrayList list = (ArrayList) this.lastUploadedLists.get(dest);
        return list == null ? new ArrayList() : list;
    }

    BackgroundEngine() throws IOException, ClassNotFoundException {
        this.initializeIndexes();
        this.setPriority(1);
    }

    BackgroundEngine(File f) throws IOException, ClassNotFoundException {
        store = f;
        this.initializeIndexes();
        this.setPriority(1);
    }

    public void forceExternalUpdate() {
        this.propertyChangeSupport.firePropertyChange("SharedDirectoriesModification", null, this);
    }

    public void setStoreFile(File store) {
        this.store = store;
    }

    private void load(File source) throws Exception {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(source));
            int blockSize = ois.readInt();
            if (blockSize != WarriorAnt.blockSizeInDownload) {
                ois.close();
                throw new Exception("Shared files blocksize uncompatible");
            }
            Object directories = ois.readObject();
            sharedDirectories = (ArrayList) directories;
            for (int x = 0; x < sharedDirectories.size(); x++) {
                if (!(sharedDirectories.get(x) instanceof DirInfos)) throw new Exception("Shared directories corrupted!");
            }
            sharedFilesIndexName = (Hashtable) ois.readObject();
            ois.close();
            this.sharedFilesIndexHash = new Hashtable();
            Enumeration fileNames = this.sharedFilesIndexName.keys();
            while (fileNames.hasMoreElements()) {
                FileInfos infos = (FileInfos) this.sharedFilesIndexName.get((String) fileNames.nextElement());
                this.sharedFilesIndexHash.put(infos.getHash(), infos);
            }
        } catch (Exception e) {
            this.forceIndexing = true;
            _logger.debug("TRACE", e);
        }
    }

    private void loadRemote(File source) throws Exception {
    }

    private void store(File dest) {
        try {
            synchronized (this.localIndexMonitor) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dest));
                oos.writeInt(WarriorAnt.blockSizeInDownload);
                oos.writeObject(sharedDirectories);
                oos.writeObject(sharedFilesIndexName);
                oos.flush();
                oos.close();
            }
        } catch (Exception e) {
            _logger.info("Error storing shared files infos", e);
            _logger.debug("TRACE", e);
        }
    }

    public void storeLocal() {
        this.store(store);
    }

    private void storeRemote(File dest) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dest));
            oos.writeInt(WarriorAnt.blockSizeInDownload);
            oos.writeObject(remoteFilesIndexHash);
            oos.writeObject(remoteFilesIndexED2KHash);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            _logger.info("Error storing remote shared files infos", e);
            _logger.debug("TRACE", e);
        }
    }

    public synchronized void addDirectory(File directory) {
        if (directory.isDirectory()) {
            if (!this.sharedDirectories.contains(new DirInfos(directory.getAbsolutePath(), directory.lastModified()))) {
                this.sharedDirectories.add(new DirInfos(directory.getAbsolutePath(), directory.lastModified()));
                forceIndexing = true;
                this.propertyChangeSupport.firePropertyChange("SharedDirectoriesModification", null, this);
            }
        }
    }

    public synchronized void removeDirectory(File directory) {
        if (this.sharedDirectories.remove(new DirInfos(directory.getAbsolutePath(), directory.lastModified()))) {
            forceIndexing = true;
            this.propertyChangeSupport.firePropertyChange("SharedDirectoriesModification", null, this);
        }
    }

    public void recursiveExplore(File directory, ArrayList fileList, Hashtable tempHashtable) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int y = 0; y < files.length; y++) {
                if (files[y].isFile() && !fileList.contains(files[y].getAbsolutePath())) {
                    fileList.add(files[y].getAbsolutePath());
                    tempHashtable.remove(files[y].getAbsolutePath());
                } else if (files[y].isDirectory() && BackgroundEngine.recursiveExplore) {
                    DirInfos infos = new DirInfos(files[y].getAbsolutePath(), files[y].lastModified());
                    this.nestedDirectories.put(files[y].getAbsolutePath(), infos);
                    this.recursiveExplore(files[y], fileList, tempHashtable);
                }
                try {
                    this.sleep(50);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public boolean recursiveExploreForModifications(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int y = 0; y < files.length; y++) {
                if (files[y].isDirectory() && BackgroundEngine.recursiveExplore) {
                    DirInfos indexedDir = (DirInfos) nestedDirectories.get(files[y].getAbsolutePath());
                    if (indexedDir == null) {
                        return true;
                    } else if (indexedDir.getLastModified() != indexedDir.getFile().lastModified() || this.recursiveExploreForModifications(files[y])) {
                        return true;
                    }
                }
                try {
                    this.sleep(50);
                } catch (InterruptedException ex) {
                }
            }
        }
        return false;
    }

    private void processCurrentEvent(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("httpServerQueryCompleted")) {
            synchronized (this) {
                QueryMessage eventQuerySource = (QueryMessage) e.getOldValue();
                ArrayList resultSet = (ArrayList) e.getNewValue();
                for (int x = 0; x < resultSet.size(); x++) {
                    if (resultSet.get(x) != null) {
                        HttpServerInfo httpInfo = (HttpServerInfo) resultSet.get(x);
                        if (this.wa != null && !this.wa.getIdent().equals(httpInfo.getOwnerId())) {
                            this.httpServers.put(httpInfo.getOwnerId(), httpInfo);
                            _logger.info("Added http server to list: " + httpInfo.getOwnerId().substring(0, 10));
                        }
                    }
                }
            }
        } else if (e.getPropertyName().equals("supernodeQueryCompleted")) {
            synchronized (this) {
                QueryMessage eventQuerySource = (QueryMessage) e.getOldValue();
                ArrayList resultSet = (ArrayList) e.getNewValue();
                for (int x = 0; x < resultSet.size(); x++) {
                    if (resultSet.get(x) != null) {
                        QuerySupernodeTuple qft = (QuerySupernodeTuple) resultSet.get(x);
                        if (this.wa != null && !(this.wa.isSupernode() && this.wa.getIdent().equals(qft.getOwnerID()))) {
                            QuerySupernodeTuple oldQft = (QuerySupernodeTuple) this.supernodeList.get(qft.getOwnerID());
                            if (oldQft == null || oldQft.getSeenOn().longValue() < qft.getSeenOn().longValue()) {
                                this.supernodeList.put(qft.getOwnerID(), qft);
                                _logger.info("Added supernode to list: " + qft.getOwnerID().substring(0, 10));
                            }
                        }
                    }
                }
            }
        } else if (e.getPropertyName().equals("queryCompleted")) {
            synchronized (this) {
                QueryMessage eventQuerySource = (QueryMessage) e.getOldValue();
                if (eventQuerySource.getQuery() instanceof QueryStringItem || eventQuerySource.getQuery() instanceof QueryHashItem || (eventQuerySource.getQuery() instanceof QueryFileListItem && ((QueryFileListItem) eventQuerySource.getQuery()).getAction()) || eventQuerySource.getQuery() instanceof QueryRandomItem) {
                    ArrayList resultSet = (ArrayList) e.getNewValue();
                    for (int x = 0; x < resultSet.size(); x++) {
                        if (resultSet.get(x) != null) {
                            QueryFileTuple qft = (QueryFileTuple) resultSet.get(x);
                            BackgroundEngine.getInstance().addRemoteFile(qft);
                        }
                    }
                    this.storeRemote(remoteSharedStore);
                    try {
                        this.synchronizeRemoteIndex(false);
                    } catch (Exception ex) {
                        _logger.error("Error in synchronizing remote index", ex);
                    }
                } else if ((eventQuerySource.getQuery() instanceof QueryFileListItem && !((QueryFileListItem) eventQuerySource.getQuery()).getAction())) {
                    ArrayList resultSet = (ArrayList) e.getNewValue();
                    for (int x = 0; x < resultSet.size(); x++) {
                        if (resultSet.get(x) != null) {
                            QueryFileTuple qft = (QueryFileTuple) resultSet.get(x);
                            String curKey = qft.getFileHash();
                            RemoteFileInfos localCached = (RemoteFileInfos) this.remoteFilesIndexHash.get(curKey);
                            localCached.getOwners().remove(qft.getOwnerID());
                            if (!localCached.hasSources(BackgroundEngine.remoteIndexedDocumentsTimeout)) {
                                this.remoteFilesIndexHash.remove(curKey);
                                this.remoteFilesIndexED2KHash.remove(curKey);
                            }
                        }
                    }
                    this.storeRemote(remoteSharedStore);
                    try {
                        this.synchronizeRemoteIndex(false);
                    } catch (Exception ex) {
                        _logger.error("Error in synchronizing remote index", ex);
                    }
                }
            }
        } else if (e.getPropertyName().equals("routeLost")) {
            synchronized (this) {
                String nodeId = (String) e.getNewValue();
                if (this.supernodeList.remove(nodeId) != null) _logger.info("Removed supernode: " + nodeId.substring(0, 10));
                if (this.httpServers.remove(nodeId) != null) _logger.info("Removed http server: " + nodeId.substring(0, 10));
                Enumeration keys = this.remoteFilesIndexHash.keys();
                while (keys.hasMoreElements()) {
                    String curKey = (String) keys.nextElement();
                    RemoteFileInfos localCached = (RemoteFileInfos) this.remoteFilesIndexHash.get(curKey);
                    localCached.getOwners().remove(nodeId);
                    if (!localCached.hasSources(BackgroundEngine.remoteIndexedDocumentsTimeout)) {
                        this.remoteFilesIndexHash.remove(curKey);
                        this.remoteFilesIndexED2KHash.remove(curKey);
                    }
                }
                this.storeRemote(remoteSharedStore);
                try {
                    this.synchronizeRemoteIndex(false);
                } catch (Exception ex) {
                    _logger.error("Error in synchronizing remote index", ex);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        final PropertyChangeEvent event = e;
        Thread processor = new Thread() {

            public void run() {
                processCurrentEvent(event);
            }
        };
        processor.setPriority(1);
        processor.start();
    }

    public void resetIndexingCycle() {
        this.resetCycle = true;
    }

    public void refreshIndex() {
        forceIndexing = true;
    }

    public void run() {
        int counter = 0;
        this.supernodeEngine.start();
        while (!this.terminate) {
            if (this.resetCycle == true) {
                this.resetCycle = false;
                this.forceIndexing = true;
            }
            try {
                boolean changes = false;
                this.propertyChangeSupport.firePropertyChange("fileIndexingCompleted", null, null);
                Thread.sleep(this.forceIndexing ? 0 : BackgroundEngine.refreshRate);
                while (wa != null && wa.writingFileLock.intValue() > 0) {
                    Thread.sleep(BackgroundEngine.refreshRate);
                }
                this.propertyChangeSupport.firePropertyChange("fileIndexingInit", null, null);
                ArrayList fileList = new ArrayList();
                Hashtable tempHashtable = (Hashtable) sharedFilesIndexName.clone();
                boolean modifications = false;
                for (int x = 0; x < this.sharedDirectories.size(); x++) {
                    DirInfos sharedDir = (DirInfos) this.sharedDirectories.get(x);
                    if (!sharedDir.getIndexed() || sharedDir.getLastModified() != sharedDir.getFile().lastModified() || recursiveExploreForModifications(sharedDir.getFile())) {
                        modifications = true;
                    }
                }
                if (!modifications && !forceIndexing) {
                    continue;
                } else {
                    nestedDirectories = new Hashtable();
                    forceIndexing = false;
                }
                _logger.info("Shared directory structure modification! Analyzing..." + this.sharedDirectories.size());
                for (int x = 0; x < this.sharedDirectories.size(); x++) {
                    DirInfos sharedDir = (DirInfos) this.sharedDirectories.get(x);
                    File[] files = sharedDir.getFile().listFiles();
                    if (files != null) {
                        for (int y = 0; y < files.length; y++) {
                            if (files[y].isFile() && !fileList.contains(files[y].getAbsolutePath())) {
                                fileList.add(files[y].getAbsolutePath());
                                tempHashtable.remove(files[y].getAbsolutePath());
                            } else if (files[y].isDirectory() && BackgroundEngine.recursiveExplore) {
                                DirInfos infos = new DirInfos(files[y].getAbsolutePath(), files[y].lastModified());
                                this.nestedDirectories.put(files[y].getAbsolutePath(), infos);
                                this.recursiveExplore(files[y], fileList, tempHashtable);
                            }
                            try {
                                this.sleep(50);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                    sharedDir.setLastModified(sharedDir.getFile().lastModified());
                    sharedDir.setIndexed();
                }
                _logger.info("Total files: " + fileList.size() + "   Removing: " + tempHashtable.size());
                Enumeration toBeRemoved = tempHashtable.keys();
                while (toBeRemoved.hasMoreElements()) {
                    changes = true;
                    String removeKey = (String) toBeRemoved.nextElement();
                    sharedFilesIndexHash.remove(((FileInfos) sharedFilesIndexName.get(removeKey)).getHash());
                    sharedFilesIndexName.remove(removeKey);
                    synchronized (this.localIndexMonitor) {
                        IndexReader reader = IndexReader.open(storeIndex);
                        Term term = new Term("Path", removeKey);
                        int deleted = reader.deleteDocuments(term);
                        reader.close();
                    }
                }
                _logger.info("Total shared files: " + sharedFilesIndexName.size() + "   Modifier: " + (sharedFilesIndexName.size() - fileList.size()));
                for (int y = fileList.size() - 1; y >= 0 && !this.terminate && !this.resetCycle && (wa == null || !(wa.writingFileLock.intValue() > 0)); y--) {
                    if (!sharedFilesIndexName.containsKey(fileList.get(y))) {
                        changes = true;
                        FileInfos infos = new FileInfos(new File((String) fileList.get(y)));
                        sharedFilesIndexName.put(fileList.get(y), infos);
                        sharedFilesIndexHash.put(infos.getHash(), infos);
                        synchronized (this.localIndexMonitor) {
                            IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), false);
                            IndexerGraphicEngine ige = new IndexerGraphicEngine(new File((String) fileList.get(y)));
                            ige.start();
                            writer.addDocument(FileDocument.Document(infos));
                            writer.close();
                            ige.terminate();
                        }
                        this.store(store);
                        this.propertyChangeSupport.firePropertyChange("fileIndexed", new Integer(fileList.size()), new Integer(y));
                    } else {
                        if (!(((FileInfos) sharedFilesIndexName.get(fileList.get(y))).getLastModified() == (new File((String) fileList.get(y))).lastModified())) {
                            changes = true;
                            FileInfos infos = new FileInfos(new File((String) fileList.get(y)));
                            sharedFilesIndexName.put(fileList.get(y), infos);
                            sharedFilesIndexHash.put(infos.getHash(), infos);
                            synchronized (this.localIndexMonitor) {
                                IndexWriter writer = new IndexWriter(storeIndex, new StandardAnalyzer(), false);
                                writer.addDocument(FileDocument.Document(infos));
                                writer.close();
                            }
                            this.store(store);
                            this.propertyChangeSupport.firePropertyChange("fileIndexed", new Integer(fileList.size()), new Integer(y));
                        }
                    }
                }
                if (changes) {
                    this.forceUploadingLists = true;
                    this.store(store);
                    if (Logger.getRootLogger().getEffectiveLevel().toInt() <= Level.DEBUG_INT) {
                        Enumeration visualizeKeys = sharedFilesIndexName.keys();
                        Enumeration visualizeValues = sharedFilesIndexName.elements();
                        while (visualizeKeys.hasMoreElements()) {
                            _logger.debug(visualizeKeys.nextElement() + ".....");
                            FileInfos fi = (FileInfos) visualizeValues.nextElement();
                            _logger.debug(fi.getHash() + "....." + fi.getLastModified() + "\n");
                        }
                    }
                    this.propertyChangeSupport.firePropertyChange("SharedDirectoriesModification", null, this);
                }
                totalLocalSharedFileSize = 0;
                Enumeration localFiles = this.sharedFilesIndexHash.elements();
                while (localFiles.hasMoreElements()) {
                    totalLocalSharedFileSize += ((FileInfos) localFiles.nextElement()).getSize();
                }
                totalRemoteSharedFileSize = 0;
                Enumeration remoteFiles = this.remoteFilesIndexHash.elements();
                while (remoteFiles.hasMoreElements()) {
                    totalRemoteSharedFileSize += ((RemoteFileInfos) remoteFiles.nextElement()).getSize();
                }
                if (this.getTimesToRemoteCrawling() > 0) {
                    if (this.wa != null && counter == 0) {
                        this.currentQuery = this.wa.doRandomQuery(broadcastTimeToLive);
                    }
                    counter = (counter + 1) % this.getTimesToRemoteCrawling();
                }
                System.gc();
            } catch (Exception e) {
                _logger.error("Background Indexer Error", e);
                this.propertyChangeSupport.firePropertyChange("fileIndexingCompleted", null, null);
            }
        }
        synchronized (this) {
            while (this.synchronizingRemote) {
                try {
                    this.wait();
                } catch (InterruptedException ex1) {
                }
            }
            this.loggingOut = true;
        }
    }

    public int getTimesToRemoteCrawling() {
        return -1;
    }

    public void setWarriorAnt(WarriorAnt wa) {
        this.wa = wa;
    }

    public ArrayList getCompleteFileList() {
        try {
            ArrayList results = new ArrayList();
            Enumeration completeEnum = this.sharedFilesIndexName.keys();
            while (completeEnum.hasMoreElements()) {
                results.add(completeEnum.nextElement());
            }
            Enumeration partialEnum = this.partialFiles.keys();
            while (partialEnum.hasMoreElements()) {
                results.add(new StringHash((String) partialEnum.nextElement()));
            }
            return results;
        } catch (Exception ex) {
            _logger.error("Invalid query string", ex);
            return new ArrayList();
        }
    }

    public ArrayList search(String item, boolean content) {
        try {
            ArrayList results = new ArrayList();
            IndexReader reader = IndexReader.open(storeIndex);
            Searcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("TextPath", analyzer);
            Query query = parser.parse(item);
            Hits hits = searcher.search(query);
            for (int i = 0; i < hits.length() && i < QueryManager.resultSize; i++) {
                Document doc = hits.doc(i);
                String path = doc.get("Path");
                if (!results.contains(path)) results.add(path);
            }
            if (content) {
                parser = new QueryParser("Contents", analyzer);
                query = parser.parse(item);
                hits = searcher.search(query);
                for (int i = 0; i < hits.length() && i < QueryManager.resultSize; i++) {
                    Document doc = hits.doc(i);
                    String path = doc.get("Path");
                    if (!results.contains(path)) results.add(path);
                }
                parser = new QueryParser("ExtendedInfosContent", analyzer);
                query = parser.parse(item);
                hits = searcher.search(query);
                for (int i = 0; i < hits.length() && i < QueryManager.resultSize; i++) {
                    Document doc = hits.doc(i);
                    String path = doc.get("Path");
                    if (!results.contains(path)) results.add(path);
                }
            }
            parser = new QueryParser("PartialFileName", analyzer);
            query = parser.parse(item);
            hits = searcher.search(query);
            for (int i = 0; i < hits.length() && i < QueryManager.resultSize; i++) {
                Document doc = hits.doc(i);
                StringHash hash = new StringHash(doc.get("PartialFileHash"));
                if (!results.contains(hash)) results.add(hash);
            }
            reader.close();
            return results;
        } catch (Exception ex) {
            _logger.error("Invalid query string", ex);
            return new ArrayList();
        }
    }

    public ArrayList searchRemoteFiles(String item, boolean localSearch) {
        try {
            ArrayList results = new ArrayList();
            IndexReader reader = IndexReader.open(remoteStoreIndex);
            Searcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("RemoteFileContent", analyzer);
            Query query = parser.parse(item);
            Hits hits = searcher.search(query);
            for (int i = 0; i < hits.length() && (localSearch || i < QueryManager.resultSize); i++) {
                Document doc = hits.doc(i);
                String hash = doc.get("RemoteFileHash");
                if (!results.contains(hash)) results.add(hash);
            }
            reader.close();
            return results;
        } catch (Exception ex) {
            _logger.error("Invalid query string", ex);
            return new ArrayList();
        }
    }
}

class IndexerGraphicEngine extends Thread {

    boolean terminate = false;

    public static ArrayList propertyChangeListeners = new ArrayList();

    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    File file;

    public IndexerGraphicEngine(File file) {
        this.file = file;
        for (int x = 0; x < propertyChangeListeners.size(); x++) {
            this.propertyChangeSupport.addPropertyChangeListener((PropertyChangeListener) propertyChangeListeners.get(x));
        }
        this.setPriority(1);
    }

    public void terminate() {
        this.terminate = true;
    }

    public void run() {
        int y = 0;
        boolean direction = true;
        while (!terminate) {
            try {
                sleep(100);
            } catch (InterruptedException ex) {
            }
            if (direction && y < 100) y++; else if (direction && y >= 100) direction = false; else if (!direction && y > 0) y++; else if (!direction && y <= 0) direction = true;
            this.propertyChangeSupport.firePropertyChange("fileIndexingInProgress", "[Indexing...] " + file.getName(), new Integer(y));
        }
        for (int x = 0; x < propertyChangeListeners.size(); x++) {
            this.propertyChangeSupport.removePropertyChangeListener((PropertyChangeListener) propertyChangeListeners.get(x));
        }
    }

    static void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeListeners.add(pcl);
    }

    static void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeListeners.remove(pcl);
    }
}
