package com.android.ddmlib;

import com.android.ddmlib.AdbHelper.AdbResponse;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.utils.ArrayHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Sync service class to push/pull to/from devices/emulators, through the debug bridge.
 * <p/>
 * To get a {@link SyncService} object, use {@link Device#getSyncService()}.
 */
public final class SyncService {

    private static final byte[] ID_OKAY = { 'O', 'K', 'A', 'Y' };

    private static final byte[] ID_FAIL = { 'F', 'A', 'I', 'L' };

    private static final byte[] ID_STAT = { 'S', 'T', 'A', 'T' };

    private static final byte[] ID_RECV = { 'R', 'E', 'C', 'V' };

    private static final byte[] ID_DATA = { 'D', 'A', 'T', 'A' };

    private static final byte[] ID_DONE = { 'D', 'O', 'N', 'E' };

    private static final byte[] ID_SEND = { 'S', 'E', 'N', 'D' };

    private static final NullSyncProgresMonitor sNullSyncProgressMonitor = new NullSyncProgresMonitor();

    private static final int S_ISOCK = 0xC000;

    private static final int S_IFLNK = 0xA000;

    private static final int S_IFREG = 0x8000;

    private static final int S_IFBLK = 0x6000;

    private static final int S_IFDIR = 0x4000;

    private static final int S_IFCHR = 0x2000;

    private static final int S_IFIFO = 0x1000;

    private static final int SYNC_DATA_MAX = 64 * 1024;

    private static final int REMOTE_PATH_MAX_LENGTH = 1024;

    /** Result code for transfer success. */
    public static final int RESULT_OK = 0;

    /** Result code for canceled transfer */
    public static final int RESULT_CANCELED = 1;

    /** Result code for unknown error */
    public static final int RESULT_UNKNOWN_ERROR = 2;

    /** Result code for network connection error */
    public static final int RESULT_CONNECTION_ERROR = 3;

    /** Result code for unknown remote object during a pull */
    public static final int RESULT_NO_REMOTE_OBJECT = 4;

    /** Result code when attempting to pull multiple files into a file */
    public static final int RESULT_TARGET_IS_FILE = 5;

    /** Result code when attempting to pull multiple into a directory that does not exist. */
    public static final int RESULT_NO_DIR_TARGET = 6;

    /** Result code for wrong encoding on the remote path. */
    public static final int RESULT_REMOTE_PATH_ENCODING = 7;

    /** Result code for remote path that is too long. */
    public static final int RESULT_REMOTE_PATH_LENGTH = 8;

    /** Result code for error while writing local file. */
    public static final int RESULT_FILE_WRITE_ERROR = 9;

    /** Result code for error while reading local file. */
    public static final int RESULT_FILE_READ_ERROR = 10;

    /** Result code for attempting to push a file that does not exist. */
    public static final int RESULT_NO_LOCAL_FILE = 11;

    /** Result code for attempting to push a directory. */
    public static final int RESULT_LOCAL_IS_DIRECTORY = 12;

    /** Result code for when the target path of a multi file push is a file. */
    public static final int RESULT_REMOTE_IS_FILE = 13;

    /** Result code for receiving too much data from the remove device at once */
    public static final int RESULT_BUFFER_OVERRUN = 14;

    /**
     * A file transfer result.
     * <p/>
     * This contains a code, and an optional string
     */
    public static class SyncResult {

        private int mCode;

        private String mMessage;

        SyncResult(int code, String message) {
            mCode = code;
            mMessage = message;
        }

        SyncResult(int code, Exception e) {
            this(code, e.getMessage());
        }

        SyncResult(int code) {
            this(code, errorCodeToString(code));
        }

        public int getCode() {
            return mCode;
        }

        public String getMessage() {
            return mMessage;
        }
    }

    /**
     * Classes which implement this interface provide methods that deal
     * with displaying transfer progress.
     */
    public interface ISyncProgressMonitor {

        /**
         * Sent when the transfer starts
         * @param totalWork the total amount of work.
         */
        public void start(int totalWork);

        /**
         * Sent when the transfer is finished or interrupted.
         */
        public void stop();

        /**
         * Sent to query for possible cancellation.
         * @return true if the transfer should be stopped.
         */
        public boolean isCanceled();

        /**
         * Sent when a sub task is started.
         * @param name the name of the sub task.
         */
        public void startSubTask(String name);

        /**
         * Sent when some progress have been made.
         * @param work the amount of work done.
         */
        public void advance(int work);
    }

    /**
     * A Sync progress monitor that does nothing
     */
    private static class NullSyncProgresMonitor implements ISyncProgressMonitor {

        public void advance(int work) {
        }

        public boolean isCanceled() {
            return false;
        }

        public void start(int totalWork) {
        }

        public void startSubTask(String name) {
        }

        public void stop() {
        }
    }

    private InetSocketAddress mAddress;

    private Device mDevice;

    private SocketChannel mChannel;

    /**
     * Buffer used to send data. Allocated when needed and reused afterward.
     */
    private byte[] mBuffer;

    /**
     * Creates a Sync service object.
     * @param address The address to connect to
     * @param device the {@link Device} that the service connects to.
     */
    SyncService(InetSocketAddress address, Device device) {
        mAddress = address;
        mDevice = device;
    }

    /**
     * Opens the sync connection. This must be called before any calls to push[File] / pull[File].
     * @return true if the connection opened, false if adb refuse the connection. This can happen
     * if the {@link Device} is invalid.
     * @throws IOException If the connection to adb failed.
     */
    boolean openSync() throws IOException {
        try {
            mChannel = SocketChannel.open(mAddress);
            mChannel.configureBlocking(false);
            AdbHelper.setDevice(mChannel, mDevice);
            byte[] request = AdbHelper.formAdbRequest("sync:");
            AdbHelper.write(mChannel, request, -1, DdmPreferences.getTimeOut());
            AdbResponse resp = AdbHelper.readAdbResponse(mChannel, false);
            if (!resp.ioSuccess || !resp.okay) {
                Log.w("ddms", "Got timeout or unhappy response from ADB sync req: " + resp.message);
                mChannel.close();
                mChannel = null;
                return false;
            }
        } catch (IOException e) {
            if (mChannel != null) {
                try {
                    mChannel.close();
                } catch (IOException e2) {
                }
                mChannel = null;
            }
            throw e;
        }
        return true;
    }

    /**
     * Closes the connection.
     */
    public void close() {
        if (mChannel != null) {
            try {
                mChannel.close();
            } catch (IOException e) {
            }
            mChannel = null;
        }
    }

    /**
     * Returns a sync progress monitor that does nothing. This allows background tasks that don't
     * want/need to display ui, to pass a valid {@link ISyncProgressMonitor}.
     * <p/>This object can be reused multiple times and can be used by concurrent threads.
     */
    public static ISyncProgressMonitor getNullProgressMonitor() {
        return sNullSyncProgressMonitor;
    }

    /**
     * Converts an error code into a non-localized string
     * @param code the error code;
     */
    private static String errorCodeToString(int code) {
        switch(code) {
            case RESULT_OK:
                return "Success.";
            case RESULT_CANCELED:
                return "Tranfert canceled by the user.";
            case RESULT_UNKNOWN_ERROR:
                return "Unknown Error.";
            case RESULT_CONNECTION_ERROR:
                return "Adb Connection Error.";
            case RESULT_NO_REMOTE_OBJECT:
                return "Remote object doesn't exist!";
            case RESULT_TARGET_IS_FILE:
                return "Target object is a file.";
            case RESULT_NO_DIR_TARGET:
                return "Target directory doesn't exist.";
            case RESULT_REMOTE_PATH_ENCODING:
                return "Remote Path encoding is not supported.";
            case RESULT_REMOTE_PATH_LENGTH:
                return "Remove path is too long.";
            case RESULT_FILE_WRITE_ERROR:
                return "Writing local file failed!";
            case RESULT_FILE_READ_ERROR:
                return "Reading local file failed!";
            case RESULT_NO_LOCAL_FILE:
                return "Local file doesn't exist.";
            case RESULT_LOCAL_IS_DIRECTORY:
                return "Local path is a directory.";
            case RESULT_REMOTE_IS_FILE:
                return "Remote path is a file.";
            case RESULT_BUFFER_OVERRUN:
                return "Receiving too much data.";
        }
        throw new RuntimeException();
    }

    /**
     * Pulls file(s) or folder(s).
     * @param entries the remote item(s) to pull
     * @param localPath The local destination. If the entries count is > 1 or
     *      if the unique entry is a folder, this should be a folder.
     * @param monitor The progress monitor. Cannot be null.
     * @return a {@link SyncResult} object with a code and an optional message.
     *
     * @see FileListingService.FileEntry
     * @see #getNullProgressMonitor()
     */
    public SyncResult pull(FileEntry[] entries, String localPath, ISyncProgressMonitor monitor) {
        File f = new File(localPath);
        if (f.exists() == false) {
            return new SyncResult(RESULT_NO_DIR_TARGET);
        }
        if (f.isDirectory() == false) {
            return new SyncResult(RESULT_TARGET_IS_FILE);
        }
        FileListingService fls = new FileListingService(mDevice);
        int total = getTotalRemoteFileSize(entries, fls);
        monitor.start(total);
        SyncResult result = doPull(entries, localPath, fls, monitor);
        monitor.stop();
        return result;
    }

    /**
     * Pulls a single file.
     * @param remote the remote file
     * @param localFilename The local destination.
     * @param monitor The progress monitor. Cannot be null.
     * @return a {@link SyncResult} object with a code and an optional message.
     *
     * @see FileListingService.FileEntry
     * @see #getNullProgressMonitor()
     */
    public SyncResult pullFile(FileEntry remote, String localFilename, ISyncProgressMonitor monitor) {
        int total = remote.getSizeValue();
        monitor.start(total);
        SyncResult result = doPullFile(remote.getFullPath(), localFilename, monitor);
        monitor.stop();
        return result;
    }

    /**
     * Pulls a single file.
     * <p/>Because this method just deals with a String for the remote file instead of a
     * {@link FileEntry}, the size of the file being pulled is unknown and the
     * {@link ISyncProgressMonitor} will not properly show the progress
     * @param remoteFilepath the full path to the remote file
     * @param localFilename The local destination.
     * @param monitor The progress monitor. Cannot be null.
     * @return a {@link SyncResult} object with a code and an optional message.
     *
     * @see #getNullProgressMonitor()
     */
    public SyncResult pullFile(String remoteFilepath, String localFilename, ISyncProgressMonitor monitor) {
        monitor.start(0);
        SyncResult result = doPullFile(remoteFilepath, localFilename, monitor);
        monitor.stop();
        return result;
    }

    /**
     * Push several files.
     * @param local An array of loca files to push
     * @param remote the remote {@link FileEntry} representing a directory.
     * @param monitor The progress monitor. Cannot be null.
     * @return a {@link SyncResult} object with a code and an optional message.
     */
    public SyncResult push(String[] local, FileEntry remote, ISyncProgressMonitor monitor) {
        if (remote.isDirectory() == false) {
            return new SyncResult(RESULT_REMOTE_IS_FILE);
        }
        ArrayList<File> files = new ArrayList<File>();
        for (String path : local) {
            files.add(new File(path));
        }
        File[] fileArray = files.toArray(new File[files.size()]);
        int total = getTotalLocalFileSize(fileArray);
        monitor.start(total);
        SyncResult result = doPush(fileArray, remote.getFullPath(), monitor);
        monitor.stop();
        return result;
    }

    /**
     * Push a single file.
     * @param local the local filepath.
     * @param remote The remote filepath.
     * @param monitor The progress monitor. Cannot be null.
     * @return a {@link SyncResult} object with a code and an optional message.
     */
    public SyncResult pushFile(String local, String remote, ISyncProgressMonitor monitor) {
        File f = new File(local);
        if (f.exists() == false) {
            return new SyncResult(RESULT_NO_LOCAL_FILE);
        }
        if (f.isDirectory()) {
            return new SyncResult(RESULT_LOCAL_IS_DIRECTORY);
        }
        monitor.start((int) f.length());
        SyncResult result = doPushFile(local, remote, monitor);
        monitor.stop();
        return result;
    }

    /**
     * compute the recursive file size of all the files in the list. Folder
     * have a weight of 1.
     * @param entries
     * @param fls
     * @return
     */
    private int getTotalRemoteFileSize(FileEntry[] entries, FileListingService fls) {
        int count = 0;
        for (FileEntry e : entries) {
            int type = e.getType();
            if (type == FileListingService.TYPE_DIRECTORY) {
                FileEntry[] children = fls.getChildren(e, false, null);
                count += getTotalRemoteFileSize(children, fls) + 1;
            } else if (type == FileListingService.TYPE_FILE) {
                count += e.getSizeValue();
            }
        }
        return count;
    }

    /**
     * compute the recursive file size of all the files in the list. Folder
     * have a weight of 1.
     * This does not check for circular links.
     * @param files
     * @return
     */
    private int getTotalLocalFileSize(File[] files) {
        int count = 0;
        for (File f : files) {
            if (f.exists()) {
                if (f.isDirectory()) {
                    return getTotalLocalFileSize(f.listFiles()) + 1;
                } else if (f.isFile()) {
                    count += f.length();
                }
            }
        }
        return count;
    }

    /**
     * Pulls multiple files/folders recursively.
     * @param entries The list of entry to pull
     * @param localPath the localpath to a directory
     * @param fileListingService a FileListingService object to browse through remote directories.
     * @param monitor the progress monitor. Must be started already.
     * @return a {@link SyncResult} object with a code and an optional message.
     */
    private SyncResult doPull(FileEntry[] entries, String localPath, FileListingService fileListingService, ISyncProgressMonitor monitor) {
        for (FileEntry e : entries) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            int type = e.getType();
            if (type == FileListingService.TYPE_DIRECTORY) {
                monitor.startSubTask(e.getFullPath());
                String dest = localPath + File.separator + e.getName();
                File d = new File(dest);
                d.mkdir();
                FileEntry[] children = fileListingService.getChildren(e, true, null);
                SyncResult result = doPull(children, dest, fileListingService, monitor);
                if (result.mCode != RESULT_OK) {
                    return result;
                }
                monitor.advance(1);
            } else if (type == FileListingService.TYPE_FILE) {
                monitor.startSubTask(e.getFullPath());
                String dest = localPath + File.separator + e.getName();
                SyncResult result = doPullFile(e.getFullPath(), dest, monitor);
                if (result.mCode != RESULT_OK) {
                    return result;
                }
            }
        }
        return new SyncResult(RESULT_OK);
    }

    /**
     * Pulls a remote file
     * @param remotePath the remote file (length max is 1024)
     * @param localPath the local destination
     * @param monitor the monitor. The monitor must be started already.
     * @return a {@link SyncResult} object with a code and an optional message.
     */
    private SyncResult doPullFile(String remotePath, String localPath, ISyncProgressMonitor monitor) {
        byte[] msg = null;
        byte[] pullResult = new byte[8];
        final int timeOut = DdmPreferences.getTimeOut();
        try {
            byte[] remotePathContent = remotePath.getBytes(AdbHelper.DEFAULT_ENCODING);
            if (remotePathContent.length > REMOTE_PATH_MAX_LENGTH) {
                return new SyncResult(RESULT_REMOTE_PATH_LENGTH);
            }
            msg = createFileReq(ID_RECV, remotePathContent);
            AdbHelper.write(mChannel, msg, -1, timeOut);
            AdbHelper.read(mChannel, pullResult, -1, timeOut);
            if (checkResult(pullResult, ID_DATA) == false && checkResult(pullResult, ID_DONE) == false) {
                return new SyncResult(RESULT_CONNECTION_ERROR);
            }
        } catch (UnsupportedEncodingException e) {
            return new SyncResult(RESULT_REMOTE_PATH_ENCODING, e);
        } catch (IOException e) {
            return new SyncResult(RESULT_CONNECTION_ERROR, e);
        }
        File f = new File(localPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            return new SyncResult(RESULT_FILE_WRITE_ERROR, e);
        }
        byte[] data = new byte[SYNC_DATA_MAX];
        while (true) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            if (checkResult(pullResult, ID_DONE)) {
                break;
            }
            if (checkResult(pullResult, ID_DATA) == false) {
                return new SyncResult(RESULT_CONNECTION_ERROR);
            }
            int length = ArrayHelper.swap32bitFromArray(pullResult, 4);
            if (length > SYNC_DATA_MAX) {
                return new SyncResult(RESULT_BUFFER_OVERRUN);
            }
            try {
                AdbHelper.read(mChannel, data, length, timeOut);
                AdbHelper.read(mChannel, pullResult, -1, timeOut);
            } catch (IOException e) {
                return new SyncResult(RESULT_CONNECTION_ERROR, e);
            }
            try {
                fos.write(data, 0, length);
            } catch (IOException e) {
                return new SyncResult(RESULT_FILE_WRITE_ERROR, e);
            }
            monitor.advance(length);
        }
        try {
            fos.flush();
        } catch (IOException e) {
            return new SyncResult(RESULT_FILE_WRITE_ERROR, e);
        }
        return new SyncResult(RESULT_OK);
    }

    /**
     * Push multiple files
     * @param fileArray
     * @param remotePath
     * @param monitor
     * @return a {@link SyncResult} object with a code and an optional message.
     */
    private SyncResult doPush(File[] fileArray, String remotePath, ISyncProgressMonitor monitor) {
        for (File f : fileArray) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            if (f.exists()) {
                if (f.isDirectory()) {
                    String dest = remotePath + "/" + f.getName();
                    monitor.startSubTask(dest);
                    SyncResult result = doPush(f.listFiles(), dest, monitor);
                    if (result.mCode != RESULT_OK) {
                        return result;
                    }
                    monitor.advance(1);
                } else if (f.isFile()) {
                    String remoteFile = remotePath + "/" + f.getName();
                    monitor.startSubTask(remoteFile);
                    SyncResult result = doPushFile(f.getAbsolutePath(), remoteFile, monitor);
                    if (result.mCode != RESULT_OK) {
                        return result;
                    }
                }
            }
        }
        return new SyncResult(RESULT_OK);
    }

    /**
     * Push a single file
     * @param localPath the local file to push
     * @param remotePath the remote file (length max is 1024)
     * @param monitor the monitor. The monitor must be started already.
     * @return a {@link SyncResult} object with a code and an optional message.
     */
    private SyncResult doPushFile(String localPath, String remotePath, ISyncProgressMonitor monitor) {
        FileInputStream fis = null;
        byte[] msg;
        final int timeOut = DdmPreferences.getTimeOut();
        try {
            byte[] remotePathContent = remotePath.getBytes(AdbHelper.DEFAULT_ENCODING);
            if (remotePathContent.length > REMOTE_PATH_MAX_LENGTH) {
                return new SyncResult(RESULT_REMOTE_PATH_LENGTH);
            }
            File f = new File(localPath);
            if (f.exists() == false) {
                return new SyncResult(RESULT_NO_LOCAL_FILE);
            }
            fis = new FileInputStream(f);
            msg = createSendFileReq(ID_SEND, remotePathContent, 0644);
        } catch (UnsupportedEncodingException e) {
            return new SyncResult(RESULT_REMOTE_PATH_ENCODING, e);
        } catch (FileNotFoundException e) {
            return new SyncResult(RESULT_FILE_READ_ERROR, e);
        }
        try {
            AdbHelper.write(mChannel, msg, -1, timeOut);
        } catch (IOException e) {
            return new SyncResult(RESULT_CONNECTION_ERROR, e);
        }
        if (mBuffer == null) {
            mBuffer = new byte[SYNC_DATA_MAX + 8];
        }
        System.arraycopy(ID_DATA, 0, mBuffer, 0, ID_DATA.length);
        while (true) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            int readCount = 0;
            try {
                readCount = fis.read(mBuffer, 8, SYNC_DATA_MAX);
            } catch (IOException e) {
                return new SyncResult(RESULT_FILE_READ_ERROR, e);
            }
            if (readCount == -1) {
                break;
            }
            ArrayHelper.swap32bitsToArray(readCount, mBuffer, 4);
            try {
                AdbHelper.write(mChannel, mBuffer, readCount + 8, timeOut);
            } catch (IOException e) {
                return new SyncResult(RESULT_CONNECTION_ERROR, e);
            }
            monitor.advance(readCount);
        }
        try {
            fis.close();
        } catch (IOException e) {
            return new SyncResult(RESULT_FILE_READ_ERROR, e);
        }
        try {
            long time = System.currentTimeMillis() / 1000;
            msg = createReq(ID_DONE, (int) time);
            AdbHelper.write(mChannel, msg, -1, timeOut);
            byte[] result = new byte[8];
            AdbHelper.read(mChannel, result, -1, timeOut);
            if (checkResult(result, ID_OKAY) == false) {
                if (checkResult(result, ID_FAIL)) {
                    int len = ArrayHelper.swap32bitFromArray(result, 4);
                    AdbHelper.read(mChannel, mBuffer, len, timeOut);
                    String message = new String(mBuffer, 0, len);
                    Log.e("ddms", "transfer error: " + message);
                    return new SyncResult(RESULT_UNKNOWN_ERROR, message);
                }
                return new SyncResult(RESULT_UNKNOWN_ERROR);
            }
        } catch (IOException e) {
            return new SyncResult(RESULT_CONNECTION_ERROR, e);
        }
        return new SyncResult(RESULT_OK);
    }

    /**
     * Returns the mode of the remote file.
     * @param path the remote file
     * @return and Integer containing the mode if all went well or null
     *      otherwise
     */
    private Integer readMode(String path) {
        try {
            byte[] msg = createFileReq(ID_STAT, path);
            AdbHelper.write(mChannel, msg, -1, DdmPreferences.getTimeOut());
            byte[] statResult = new byte[16];
            AdbHelper.read(mChannel, statResult, -1, DdmPreferences.getTimeOut());
            if (checkResult(statResult, ID_STAT) == false) {
                return null;
            }
            return ArrayHelper.swap32bitFromArray(statResult, 4);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Create a command with a code and an int values
     * @param command
     * @param value
     * @return
     */
    private static byte[] createReq(byte[] command, int value) {
        byte[] array = new byte[8];
        System.arraycopy(command, 0, array, 0, 4);
        ArrayHelper.swap32bitsToArray(value, array, 4);
        return array;
    }

    /**
     * Creates the data array for a stat request.
     * @param command the 4 byte command (ID_STAT, ID_RECV, ...)
     * @param path The path of the remote file on which to execute the command
     * @return the byte[] to send to the device through adb
     */
    private static byte[] createFileReq(byte[] command, String path) {
        byte[] pathContent = null;
        try {
            pathContent = path.getBytes(AdbHelper.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return createFileReq(command, pathContent);
    }

    /**
     * Creates the data array for a file request. This creates an array with a 4 byte command + the
     * remote file name.
     * @param command the 4 byte command (ID_STAT, ID_RECV, ...).
     * @param path The path, as a byte array, of the remote file on which to
     *      execute the command.
     * @return the byte[] to send to the device through adb
     */
    private static byte[] createFileReq(byte[] command, byte[] path) {
        byte[] array = new byte[8 + path.length];
        System.arraycopy(command, 0, array, 0, 4);
        ArrayHelper.swap32bitsToArray(path.length, array, 4);
        System.arraycopy(path, 0, array, 8, path.length);
        return array;
    }

    private static byte[] createSendFileReq(byte[] command, byte[] path, int mode) {
        String modeStr = "," + (mode & 0777);
        byte[] modeContent = null;
        try {
            modeContent = modeStr.getBytes(AdbHelper.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        byte[] array = new byte[8 + path.length + modeContent.length];
        System.arraycopy(command, 0, array, 0, 4);
        ArrayHelper.swap32bitsToArray(path.length + modeContent.length, array, 4);
        System.arraycopy(path, 0, array, 8, path.length);
        System.arraycopy(modeContent, 0, array, 8 + path.length, modeContent.length);
        return array;
    }

    /**
     * Checks the result array starts with the provided code
     * @param result The result array to check
     * @param code The 4 byte code.
     * @return true if the code matches.
     */
    private static boolean checkResult(byte[] result, byte[] code) {
        if (result[0] != code[0] || result[1] != code[1] || result[2] != code[2] || result[3] != code[3]) {
            return false;
        }
        return true;
    }

    private static int getFileType(int mode) {
        if ((mode & S_ISOCK) == S_ISOCK) {
            return FileListingService.TYPE_SOCKET;
        }
        if ((mode & S_IFLNK) == S_IFLNK) {
            return FileListingService.TYPE_LINK;
        }
        if ((mode & S_IFREG) == S_IFREG) {
            return FileListingService.TYPE_FILE;
        }
        if ((mode & S_IFBLK) == S_IFBLK) {
            return FileListingService.TYPE_BLOCK;
        }
        if ((mode & S_IFDIR) == S_IFDIR) {
            return FileListingService.TYPE_DIRECTORY;
        }
        if ((mode & S_IFCHR) == S_IFCHR) {
            return FileListingService.TYPE_CHARACTER;
        }
        if ((mode & S_IFIFO) == S_IFIFO) {
            return FileListingService.TYPE_FIFO;
        }
        return FileListingService.TYPE_OTHER;
    }
}
