package com.aptana.ide.syncing;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.CRC32;
import com.aptana.ide.core.FileUtils;
import com.aptana.ide.core.IdeLog;
import com.aptana.ide.core.StringUtils;
import com.aptana.ide.core.io.ConnectionException;
import com.aptana.ide.core.io.IVirtualFile;
import com.aptana.ide.core.io.IVirtualFileManager;
import com.aptana.ide.core.io.VirtualFileManagerException;
import com.aptana.ide.core.io.sync.ISyncEventHandler;
import com.aptana.ide.core.io.sync.SyncState;
import com.aptana.ide.core.io.sync.VirtualFileSyncPair;
import com.aptana.ide.core.ui.syncing.SyncingConsole;

/**
 * @author Kevin Lindsey
 */
public class Synchronizer {

    private boolean _useCRC;

    private int _clientDirectoryCreatedCount;

    private int _clientDirectoryDeletedCount;

    private int _clientFileDeletedCount;

    private int _clientFileTransferedCount;

    private int _serverDirectoryCreatedCount;

    private int _serverDirectoryDeletedCount;

    private int _serverFileDeletedCount;

    private int _serverFileTransferedCount;

    IVirtualFileManager _clientFileManager;

    IVirtualFileManager _serverFileManager;

    ISyncEventHandler _eventHandler;

    private long _timeTolerance;

    private static final int _defaultTimeTolerance = 1000;

    /**
	 * SyncManager
	 */
    public Synchronizer() {
        this(false, _defaultTimeTolerance);
    }

    /**
	 * SyncManager
	 * 
	 * @param calculateCrc
	 *            A flag indicating whether two files should be compared by their CRC when their modification times
	 *            match
	 * @param timeTolerance
	 *            The number of seconds a client and server file can differ in their modification times to still be
	 *            considered equal
	 */
    public Synchronizer(boolean calculateCrc, int timeTolerance) {
        if (timeTolerance < 0) {
            timeTolerance = -timeTolerance;
        }
        this._useCRC = calculateCrc;
        this._timeTolerance = timeTolerance;
    }

    /**
	 * Convert the full path of the specified file into a canonical form. This will remove the base directory set by the
	 * file's server and it will convert all '\' characters to '/' characters
	 * 
	 * @param file
	 *            The file to use when computing the canonical path
	 * @return Return the file's canonical path
	 */
    private String getCanonicalPath(IVirtualFile file) {
        String basePath = null;
        String result = null;
        try {
            basePath = file.getFileManager().getBaseFile().getAbsolutePath();
            if (basePath.equals(file.getFileManager().getFileSeparator())) {
                result = file.getAbsolutePath();
            } else {
                result = file.getAbsolutePath().substring(basePath.length());
            }
            if (result.indexOf('\\') != -1) {
                result = result.replace('\\', '/');
            }
            if (result.startsWith("/")) {
                result = result.substring(1);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' is not contained inside '" + basePath + "'.");
        }
        return result;
    }

    /**
	 * getCreatedDirectoryCount
	 * 
	 * @return Returns the number of directories that were created on the server.
	 */
    public int getClientDirectoryCreatedCount() {
        return this._clientDirectoryCreatedCount;
    }

    /**
	 * getClientDirectoryDeletedCount
	 * 
	 * @return Returns the clientDirectoryDeletedCount.
	 */
    public int getClientDirectoryDeletedCount() {
        return this._clientDirectoryDeletedCount;
    }

    /**
	 * getClientFileDeletedCount
	 * 
	 * @return Returns the clientFileDeletedCount.
	 */
    public int getClientFileDeletedCount() {
        return this._clientFileDeletedCount;
    }

    /**
	 * getUploadedFileCount
	 * 
	 * @return Returns the number of files that were uploaded to the server.
	 */
    public int getClientFileTransferedCount() {
        return this._clientFileTransferedCount;
    }

    /**
	 * getEventHandler
	 * 
	 * @return ISyncEventHandler
	 */
    public ISyncEventHandler getEventHandler() {
        return this._eventHandler;
    }

    /**
	 * setEventHandler
	 * 
	 * @param eventHandler
	 */
    public void setEventHandler(ISyncEventHandler eventHandler) {
        this._eventHandler = eventHandler;
    }

    /**
	 * getDeletedDirectoryCount
	 * 
	 * @return Returns the number of directories that were deleted from the server.
	 */
    public int getServerDirectoryCreatedCount() {
        return this._serverDirectoryCreatedCount;
    }

    /**
	 * getServerDirectoryDeletedCount
	 * 
	 * @return Returns the serverDirectoryDeletedCount.
	 */
    public int getServerDirectoryDeletedCount() {
        return this._serverDirectoryDeletedCount;
    }

    /**
	 * getServerFileDeletedCount
	 * 
	 * @return Returns the serverFileDeletedCount.
	 */
    public int getServerFileDeletedCount() {
        return this._serverFileDeletedCount;
    }

    /**
	 * getDeletedFileCount
	 * 
	 * @return Returns the number of files that were deleted on the server.
	 */
    public int getServerFileTransferedCount() {
        return this._serverFileTransferedCount;
    }

    /**
	 * calculateLists
	 * 
	 * @param client
	 * @param server
	 * @return List
	 * @throws IOException
	 * @throws ConnectionException
	 * @throws VirtualFileManagerException
	 */
    public VirtualFileSyncPair[] getSyncItems(IVirtualFile client, IVirtualFile server) throws IOException, ConnectionException, VirtualFileManagerException {
        int eventItemCount = 0;
        int eventItemTotal = 0;
        this._clientFileManager = client.getFileManager();
        this._serverFileManager = server.getFileManager();
        IVirtualFile[] clientFiles = new IVirtualFile[0];
        IVirtualFile[] serverFiles = new IVirtualFile[0];
        try {
            setClientEventHandler(client, server);
            clientFiles = client.getFileManager().getFiles(client, true, false);
            serverFiles = server.getFileManager().getFiles(server, true, false);
        } finally {
            removeClientEventHandler(client, server);
        }
        if (this._eventHandler != null) {
            eventItemTotal = clientFiles.length + serverFiles.length;
            if (this._eventHandler.syncEvent(null, eventItemCount++, eventItemTotal) == false) {
                return null;
            }
        }
        return createSyncItems(clientFiles, serverFiles);
    }

    /**
	 * @param clientFiles
	 * @param serverFiles
	 * @return VirtualFileSyncPair[]
	 * @throws ConnectionException
	 * @throws VirtualFileManagerException
	 * @throws IOException
	 */
    public VirtualFileSyncPair[] createSyncItems(IVirtualFile[] clientFiles, IVirtualFile[] serverFiles) throws ConnectionException, VirtualFileManagerException, IOException {
        int eventItemCount = 1;
        int eventItemTotal = clientFiles.length + serverFiles.length;
        HashMap fileList = new HashMap();
        this.reset();
        for (int i = 0; i < clientFiles.length; i++) {
            IVirtualFile clientFile = clientFiles[i];
            String relativePath = this.getCanonicalPath(clientFile);
            VirtualFileSyncPair item = new VirtualFileSyncPair(clientFile, null, relativePath, SyncState.ClientItemOnly);
            fileList.put(item.getRelativePath(), item);
            if (this._eventHandler != null) {
                if (this._eventHandler.syncEvent(item, eventItemCount++, eventItemTotal) == false) {
                    return null;
                }
            }
        }
        for (int i = 0; i < serverFiles.length; i++) {
            IVirtualFile serverFile = serverFiles[i];
            String relativePath = this.getCanonicalPath(serverFile);
            if (fileList.containsKey(relativePath)) {
                VirtualFileSyncPair item = (VirtualFileSyncPair) fileList.get(relativePath);
                item.setDestinationFile(serverFile);
                if (item.getSourceFile().isDirectory() == serverFile.isDirectory()) {
                    if (serverFile.isDirectory()) {
                        fileList.remove(relativePath);
                        continue;
                    }
                    long serverFileTime = serverFile.getModificationMillis();
                    long clientFileTime = item.getSourceFile().getModificationMillis();
                    long timeDiff = serverFileTime - clientFileTime;
                    if (-this._timeTolerance <= timeDiff && timeDiff <= this._timeTolerance) {
                        if (this._useCRC && serverFile.isFile()) {
                            item.setSyncState(this.compareCRC(item));
                        } else {
                            item.setSyncState(SyncState.ItemsMatch);
                        }
                    } else {
                        if (timeDiff < 0) {
                            item.setSyncState(SyncState.ClientItemIsNewer);
                        } else {
                            item.setSyncState(SyncState.ServerItemIsNewer);
                        }
                    }
                } else {
                    item.setSyncState(SyncState.IncompatibleFileTypes);
                }
                if (this._eventHandler != null) {
                    if (this._eventHandler.syncEvent(item, eventItemCount++, eventItemTotal) == false) {
                        return null;
                    }
                }
            } else {
                VirtualFileSyncPair item = new VirtualFileSyncPair(null, serverFile, relativePath, SyncState.ServerItemOnly);
                fileList.put(relativePath, item);
                if (this._eventHandler != null) {
                    if (this._eventHandler.syncEvent(item, eventItemCount++, eventItemTotal) == false) {
                        return null;
                    }
                }
            }
        }
        Set keySet = fileList.keySet();
        String[] keys = (String[]) keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keys);
        VirtualFileSyncPair[] syncItems = new VirtualFileSyncPair[keys.length];
        for (int i = 0; i < keys.length; i++) {
            syncItems[i] = (VirtualFileSyncPair) fileList.get(keys[i]);
        }
        return syncItems;
    }

    /**
	 * @param client
	 * @param server
	 */
    private void setClientEventHandler(IVirtualFile client, IVirtualFile server) {
        client.getFileManager().setEventHandler(this._eventHandler);
        server.getFileManager().setEventHandler(this._eventHandler);
    }

    /**
	 * @param client
	 * @param server
	 */
    private void removeClientEventHandler(IVirtualFile client, IVirtualFile server) {
        client.getFileManager().setEventHandler(null);
        server.getFileManager().setEventHandler(null);
    }

    /**
	 * getTimeTolerance
	 * 
	 * @return Returns the timeTolerance.
	 */
    public long getTimeTolerance() {
        return this._timeTolerance;
    }

    /**
	 * setTimeTolerance
	 * 
	 * @param timeTolerance
	 *            The timeTolerance to set.
	 */
    public void setTimeTolerance(int timeTolerance) {
        this._timeTolerance = timeTolerance;
    }

    /**
	 * setCalculateCrc
	 * 
	 * @param calculateCrc
	 *            The calculateCrc to set.
	 */
    public void setUseCRC(boolean calculateCrc) {
        this._useCRC = calculateCrc;
    }

    /**
	 * isCalculateCrc
	 * 
	 * @return Returns the calculateCrc.
	 */
    public boolean getUseCRC() {
        return this._useCRC;
    }

    /**
	 * compareCRC
	 * 
	 * @param item
	 * @return SyncState
	 */
    private int compareCRC(VirtualFileSyncPair item) throws ConnectionException, VirtualFileManagerException, IOException {
        InputStream clientStream = item.getSourceInputStream();
        InputStream serverStream = item.getDestinationInputStream();
        int result;
        if (clientStream != null && serverStream != null) {
            long clientCRC = getCRC(clientStream);
            long serverCRC = getCRC(serverStream);
            try {
                clientStream.close();
                serverStream.close();
            } catch (IOException e) {
                IdeLog.logError(SyncingPlugin.getDefault(), "Error closing streams during CRC comparison of '" + item.getRelativePath() + "'", e);
            }
            result = (clientCRC == serverCRC) ? SyncState.ItemsMatch : SyncState.CRCMismatch;
        } else {
            result = (clientStream == serverStream) ? SyncState.ItemsMatch : SyncState.CRCMismatch;
        }
        return result;
    }

    /**
	 * getCRC
	 * 
	 * @param stream
	 * @return CRC
	 */
    private long getCRC(InputStream stream) {
        CRC32 crc = new CRC32();
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                crc.update(buffer, 0, length);
            }
        } catch (IOException e) {
            IdeLog.logError(SyncingPlugin.getDefault(), "Error retrieving CRC", e);
        }
        return crc.getValue();
    }

    /**
	 * download
	 * 
	 * @param fileList
	 * @return success
	 * @throws ConnectionException
	 * @throws VirtualFileManagerException
	 */
    public boolean download(VirtualFileSyncPair[] fileList) throws ConnectionException, VirtualFileManagerException {
        return this.downloadAndDelete(fileList, false);
    }

    /**
	 * downloadAndDelete
	 * 
	 * @param fileList
	 * @return success
	 * @throws ConnectionException
	 * @throws VirtualFileManagerException
	 */
    public boolean downloadAndDelete(VirtualFileSyncPair[] fileList) throws ConnectionException, VirtualFileManagerException {
        return this.downloadAndDelete(fileList, true);
    }

    /**
	 * downloadAndDelete
	 * 
	 * @param fileList
	 * @param delete
	 * @return success
	 */
    private boolean downloadAndDelete(VirtualFileSyncPair[] fileList, boolean delete) throws ConnectionException, VirtualFileManagerException {
        if (_clientFileManager == null) {
            throw new NullPointerException("Client file manager cannot be null");
        }
        if (_serverFileManager == null) {
            throw new NullPointerException("Server file manager cannot be null");
        }
        Date d = new Date();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        SyncingConsole.println(FileUtils.NEW_LINE + FileUtils.NEW_LINE + "Beginning download: " + df.format(d));
        boolean result = true;
        int totalItems = (delete) ? fileList.length * 2 : fileList.length;
        this.reset();
        FILE_LOOP: for (int i = 0; i < fileList.length; i++) {
            VirtualFileSyncPair item = fileList[i];
            IVirtualFile clientFile = item.getSourceFile();
            IVirtualFile serverFile = item.getDestinationFile();
            if (this._eventHandler != null) {
                if (this._eventHandler.syncEvent(item, i, totalItems) == false) {
                    delete = false;
                    break;
                }
            }
            switch(item.getSyncState()) {
                case SyncState.ServerItemOnly:
                    if (serverFile.isDirectory()) {
                        String clientDirectoryPath = constructDestinationPath(this._clientFileManager.getBasePath(), _clientFileManager, item);
                        clientFile = this._clientFileManager.createVirtualDirectory(clientDirectoryPath);
                        SyncingConsole.println(FileUtils.NEW_LINE + "Created directory: " + clientFile.getAbsolutePath());
                        this._clientFileManager.createLocalDirectory(clientFile);
                        SyncingConsole.println("...Success");
                        this._clientDirectoryCreatedCount++;
                    } else {
                        String clientFileName = constructDestinationPath(this._clientFileManager.getBasePath(), _clientFileManager, item);
                        IVirtualFile targetClientFile = this._clientFileManager.createVirtualFile(clientFileName);
                        try {
                            SyncingConsole.println(FileUtils.NEW_LINE + "Downloading: " + serverFile.getAbsolutePath());
                            targetClientFile.putStream(serverFile.getStream());
                            SyncingConsole.println("...Success");
                            this._serverFileTransferedCount++;
                        } catch (IOException e) {
                            SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                            if (!this._eventHandler.syncErrorEvent(item, e)) {
                                break FILE_LOOP;
                            }
                        }
                    }
                    break;
                case SyncState.ServerItemIsNewer:
                case SyncState.CRCMismatch:
                    if (serverFile.isFile()) {
                        try {
                            SyncingConsole.println(FileUtils.NEW_LINE + "Downloading: " + serverFile.getAbsolutePath());
                            clientFile.putStream(serverFile.getStream());
                            SyncingConsole.println("...Success");
                            this._serverFileTransferedCount++;
                        } catch (IOException e) {
                            SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                            if (!this._eventHandler.syncErrorEvent(item, e)) {
                                break FILE_LOOP;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (delete) {
            for (int i = fileList.length - 1; i >= 0; i--) {
                VirtualFileSyncPair item = fileList[i];
                IVirtualFile clientFile = item.getSourceFile();
                if (this._eventHandler != null) {
                    if (this._eventHandler.syncEvent(item, i + fileList.length, totalItems) == false) {
                        break;
                    }
                }
                switch(item.getSyncState()) {
                    case SyncState.ClientItemOnly:
                        if (clientFile.isDirectory()) {
                            this._clientFileManager.deleteFile(clientFile);
                            this._clientDirectoryDeletedCount++;
                        } else {
                            this._clientFileManager.deleteFile(clientFile);
                            this._clientFileDeletedCount++;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }

    /**
	 * fullSync
	 * 
	 * @param fileList
	 * @return success
	 */
    public boolean fullSync(VirtualFileSyncPair[] fileList) {
        Date d = new Date();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        SyncingConsole.println(FileUtils.NEW_LINE + FileUtils.NEW_LINE + "Beginning full sync: " + df.format(d));
        boolean result = true;
        IVirtualFileManager client = this._clientFileManager;
        IVirtualFileManager server = this._serverFileManager;
        String clientBasePath = client.getBasePath();
        String serverBasePath = server.getBasePath();
        if (clientBasePath.equals(client.getFileSeparator())) {
            clientBasePath = StringUtils.EMPTY;
        }
        if (serverBasePath.equals(server.getFileSeparator())) {
            serverBasePath = StringUtils.EMPTY;
        }
        this.reset();
        FILE_LOOP: for (int i = 0; i < fileList.length; i++) {
            VirtualFileSyncPair item = fileList[i];
            IVirtualFile clientFile = item.getSourceFile();
            IVirtualFile serverFile = item.getDestinationFile();
            try {
                if (this._eventHandler != null) {
                    if (this._eventHandler.syncEvent(item, i, fileList.length) == false) {
                        result = false;
                        break FILE_LOOP;
                    }
                }
                switch(item.getSyncState()) {
                    case SyncState.ClientItemIsNewer:
                        if (clientFile.isFile()) {
                            try {
                                SyncingConsole.println(FileUtils.NEW_LINE + "Uploading: " + serverFile.getAbsolutePath());
                                serverFile.putStream(clientFile.getStream());
                                serverFile.setModificationMillis(clientFile.getModificationMillis());
                                SyncingConsole.println("...Success");
                                this._clientFileTransferedCount++;
                            } catch (IOException e) {
                                SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                                if (!this._eventHandler.syncErrorEvent(item, e)) {
                                    break FILE_LOOP;
                                }
                            }
                        } else {
                            serverFile.setModificationMillis(clientFile.getModificationMillis());
                        }
                        break;
                    case SyncState.ClientItemOnly:
                        if (clientFile.isDirectory()) {
                            String serverDirectoryPath = constructDestinationPath(serverBasePath, _serverFileManager, item);
                            serverFile = server.createVirtualDirectory(serverDirectoryPath);
                            SyncingConsole.println(FileUtils.NEW_LINE + "Created directory: " + serverFile.getAbsolutePath());
                            server.createLocalDirectory(serverFile);
                            this._serverDirectoryCreatedCount++;
                            SyncingConsole.println("...Success");
                        } else {
                            String serverFileName = constructDestinationPath(serverBasePath, _serverFileManager, item);
                            IVirtualFile targetServerFile = server.createVirtualFile(serverFileName);
                            try {
                                SyncingConsole.println(FileUtils.NEW_LINE + "Uploading: " + clientFile.getAbsolutePath());
                                targetServerFile.putStream(clientFile.getStream());
                                this._clientFileTransferedCount++;
                                targetServerFile.setModificationMillis(clientFile.getModificationMillis());
                                SyncingConsole.println("...Success");
                            } catch (IOException e) {
                                SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                                if (!this._eventHandler.syncErrorEvent(item, e)) {
                                    break FILE_LOOP;
                                }
                            }
                        }
                        break;
                    case SyncState.ServerItemIsNewer:
                        if (serverFile.isFile()) {
                            try {
                                SyncingConsole.println(FileUtils.NEW_LINE + "Downloading: " + clientFile.getAbsolutePath());
                                clientFile.putStream(serverFile.getStream());
                                this._serverFileTransferedCount++;
                                clientFile.setModificationMillis(serverFile.getModificationMillis());
                                SyncingConsole.println("...Success");
                            } catch (IOException e) {
                                SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                                if (!this._eventHandler.syncErrorEvent(item, e)) {
                                    break FILE_LOOP;
                                }
                            }
                        } else {
                            clientFile.setModificationMillis(serverFile.getModificationMillis());
                        }
                        break;
                    case SyncState.ServerItemOnly:
                        if (serverFile.isDirectory()) {
                            String clientDirectoryPath = constructDestinationPath(clientBasePath, _clientFileManager, item);
                            clientFile = client.createVirtualDirectory(clientDirectoryPath);
                            SyncingConsole.println(FileUtils.NEW_LINE + "Created directory: " + clientFile.getAbsolutePath());
                            client.createLocalDirectory(clientFile);
                            this._clientDirectoryCreatedCount++;
                            SyncingConsole.println("...Success");
                        } else {
                            String clientFileName = constructDestinationPath(clientBasePath, _clientFileManager, item);
                            IVirtualFile targetClientFile = client.createVirtualFile(clientFileName);
                            try {
                                SyncingConsole.println(FileUtils.NEW_LINE + "Downloading: " + targetClientFile.getAbsolutePath());
                                targetClientFile.putStream(serverFile.getStream());
                                this._serverFileTransferedCount++;
                                targetClientFile.setModificationMillis(serverFile.getModificationMillis());
                                SyncingConsole.println("...Success");
                            } catch (IOException e) {
                                SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                                if (!this._eventHandler.syncErrorEvent(item, e)) {
                                    break FILE_LOOP;
                                }
                            }
                        }
                        break;
                    case SyncState.CRCMismatch:
                        result = false;
                        IdeLog.logError(SyncingPlugin.getDefault(), "Full Sync cannot handle CRC mismatches: " + item.getRelativePath());
                        break;
                    case SyncState.Ignore:
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                IdeLog.logError(SyncingPlugin.getDefault(), "Error during sync", ex);
                result = false;
                if (this._eventHandler.syncErrorEvent(item, ex) == false) {
                    break FILE_LOOP;
                }
            }
        }
        return result;
    }

    /**
	 * Constructs the path for use on the destination
	 * 
	 * @param basePath
	 * @param manager
	 * @param item
	 * @return String
	 */
    private String constructDestinationPath(String basePath, IVirtualFileManager manager, VirtualFileSyncPair item) {
        if (basePath.endsWith(manager.getFileSeparator())) {
            return basePath + item.getRelativePath();
        } else {
            return basePath + manager.getFileSeparator() + item.getRelativePath();
        }
    }

    /**
	 * resetStats
	 */
    private void reset() {
        this._clientDirectoryCreatedCount = 0;
        this._clientDirectoryDeletedCount = 0;
        this._clientFileDeletedCount = 0;
        this._clientFileTransferedCount = 0;
        this._serverDirectoryCreatedCount = 0;
        this._serverDirectoryDeletedCount = 0;
        this._serverFileDeletedCount = 0;
        this._serverFileTransferedCount = 0;
    }

    /**
	 * upload
	 * 
	 * @param fileList
	 * @return success
	 * @throws ConnectionException
	 * @throws VirtualFileManagerException
	 */
    public boolean upload(VirtualFileSyncPair[] fileList) throws ConnectionException, VirtualFileManagerException {
        return this.uploadAndDelete(fileList, false);
    }

    /**
	 * uploadAndDelete
	 * 
	 * @param fileList
	 * @return success
	 * @throws ConnectionException
	 * @throws VirtualFileManagerException
	 */
    public boolean uploadAndDelete(VirtualFileSyncPair[] fileList) throws ConnectionException, VirtualFileManagerException {
        return this.uploadAndDelete(fileList, true);
    }

    /**
	 * uploadAndDelete
	 * 
	 * @param fileList
	 * @param delete
	 * @return success
	 */
    private boolean uploadAndDelete(VirtualFileSyncPair[] fileList, boolean delete) throws ConnectionException, VirtualFileManagerException {
        if (_clientFileManager == null) {
            throw new NullPointerException("Client file manager cannot be null");
        }
        if (_serverFileManager == null) {
            throw new NullPointerException("Server file manager cannot be null");
        }
        Date d = new Date();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        SyncingConsole.println(FileUtils.NEW_LINE + FileUtils.NEW_LINE + "Beginning upload: " + df.format(d));
        boolean result = true;
        int totalItems = (delete) ? fileList.length * 2 : fileList.length;
        this.reset();
        FILE_LOOP: for (int i = 0; i < fileList.length; i++) {
            VirtualFileSyncPair item = fileList[i];
            IVirtualFile clientFile = item.getSourceFile();
            IVirtualFile serverFile = item.getDestinationFile();
            if (this._eventHandler != null) {
                if (this._eventHandler.syncEvent(item, i, totalItems) == false) {
                    delete = false;
                    break;
                }
            }
            switch(item.getSyncState()) {
                case SyncState.ClientItemOnly:
                    if (clientFile.isDirectory()) {
                        String serverDirectoryPath = constructDestinationPath(this._serverFileManager.getBasePath(), _serverFileManager, item);
                        serverFile = this._serverFileManager.createVirtualDirectory(serverDirectoryPath);
                        this._serverFileManager.createLocalDirectory(serverFile);
                        this._serverDirectoryCreatedCount++;
                    } else {
                        String serverFileName = constructDestinationPath(this._serverFileManager.getBasePath(), _serverFileManager, item);
                        IVirtualFile targetServerFile = this._serverFileManager.createVirtualFile(serverFileName);
                        try {
                            SyncingConsole.println(FileUtils.NEW_LINE + "Uploading: " + clientFile.getAbsolutePath());
                            targetServerFile.putStream(clientFile.getStream());
                            SyncingConsole.println("...Success");
                            this._clientFileTransferedCount++;
                        } catch (IOException e) {
                            SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                            if (!this._eventHandler.syncErrorEvent(item, e)) {
                                break FILE_LOOP;
                            }
                        }
                    }
                    break;
                case SyncState.ClientItemIsNewer:
                case SyncState.CRCMismatch:
                    if (clientFile.isFile()) {
                        try {
                            SyncingConsole.println(FileUtils.NEW_LINE + "Uploading: " + clientFile.getAbsolutePath());
                            serverFile.putStream(clientFile.getStream());
                            SyncingConsole.println("...Success");
                            this._clientFileTransferedCount++;
                        } catch (IOException e) {
                            SyncingConsole.println("...Error: " + e.getLocalizedMessage());
                            if (!this._eventHandler.syncErrorEvent(item, e)) {
                                break FILE_LOOP;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (delete) {
            for (int i = fileList.length - 1; i >= 0; i--) {
                VirtualFileSyncPair item = fileList[i];
                IVirtualFile serverFile = item.getDestinationFile();
                if (this._eventHandler != null) {
                    if (this._eventHandler.syncEvent(item, i + fileList.length, totalItems) == false) {
                        break;
                    }
                }
                switch(item.getSyncState()) {
                    case SyncState.ServerItemOnly:
                        if (serverFile.isDirectory()) {
                            this._serverFileManager.deleteFile(serverFile);
                            this._serverDirectoryDeletedCount++;
                        } else {
                            this._serverFileManager.deleteFile(serverFile);
                            this._serverFileDeletedCount++;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }

    /**
	 * @return Returns the clientFileManager.
	 */
    public IVirtualFileManager getClientFileManager() {
        return _clientFileManager;
    }

    /**
	 * @param fileManager
	 *            The clientFileManager to set.
	 */
    public void setClientFileManager(IVirtualFileManager fileManager) {
        _clientFileManager = fileManager;
    }

    /**
	 * @return Returns the serverFileManager.
	 */
    public IVirtualFileManager getServerFileManager() {
        return _serverFileManager;
    }

    /**
	 * @param fileManager
	 *            The serverFileManager to set.
	 */
    public void setServerFileManager(IVirtualFileManager fileManager) {
        _serverFileManager = fileManager;
    }
}
