package android.media;

import android.graphics.Bitmap;
import android.media.ThumbnailUtil;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Hashtable;

/**
 * This class handles the mini-thumb file. A mini-thumb file consists
 * of blocks, indexed by id. Each block has BYTES_PER_MINTHUMB bytes in the
 * following format:
 *
 * 1 byte status (0 = empty, 1 = mini-thumb available)
 * 8 bytes magic (a magic number to match what's in the database)
 * 4 bytes data length (LEN)
 * LEN bytes jpeg data
 * (the remaining bytes are unused)
 *
 * @hide This file is shared between MediaStore and MediaProvider and should remained internal use
 *       only.
 */
public class MiniThumbFile {

    private static final String TAG = "MiniThumbFile";

    private static final int MINI_THUMB_DATA_FILE_VERSION = 3;

    public static final int BYTES_PER_MINTHUMB = 10000;

    private static final int HEADER_SIZE = 1 + 8 + 4;

    private Uri mUri;

    private RandomAccessFile mMiniThumbFile;

    private FileChannel mChannel;

    private ByteBuffer mBuffer;

    private static Hashtable<String, MiniThumbFile> sThumbFiles = new Hashtable<String, MiniThumbFile>();

    /**
     * We store different types of thumbnails in different files. To remain backward compatibility,
     * we should hashcode of content://media/external/images/media remains the same.
     */
    public static synchronized void reset() {
        for (MiniThumbFile file : sThumbFiles.values()) {
            file.deactivate();
        }
        sThumbFiles.clear();
    }

    public static synchronized MiniThumbFile instance(Uri uri) {
        String type = uri.getPathSegments().get(1);
        MiniThumbFile file = sThumbFiles.get(type);
        if (file == null) {
            file = new MiniThumbFile(Uri.parse("content://media/external/" + type + "/media"));
            sThumbFiles.put(type, file);
        }
        return file;
    }

    private String randomAccessFilePath(int version) {
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails";
        return directoryName + "/.thumbdata" + version + "-" + mUri.hashCode();
    }

    private void removeOldFile() {
        String oldPath = randomAccessFilePath(MINI_THUMB_DATA_FILE_VERSION - 1);
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            try {
                oldFile.delete();
            } catch (SecurityException ex) {
            }
        }
    }

    private RandomAccessFile miniThumbDataFile() {
        if (mMiniThumbFile == null) {
            removeOldFile();
            String path = randomAccessFilePath(MINI_THUMB_DATA_FILE_VERSION);
            File directory = new File(path).getParentFile();
            if (!directory.isDirectory()) {
                if (!directory.mkdirs()) {
                    Log.e(TAG, "Unable to create .thumbnails directory " + directory.toString());
                }
            }
            File f = new File(path);
            try {
                mMiniThumbFile = new RandomAccessFile(f, "rw");
            } catch (IOException ex) {
                try {
                    mMiniThumbFile = new RandomAccessFile(f, "r");
                } catch (IOException ex2) {
                }
            }
            if (mMiniThumbFile != null) {
                mChannel = mMiniThumbFile.getChannel();
            }
        }
        return mMiniThumbFile;
    }

    public MiniThumbFile(Uri uri) {
        mUri = uri;
        mBuffer = ByteBuffer.allocateDirect(BYTES_PER_MINTHUMB);
    }

    public synchronized void deactivate() {
        if (mMiniThumbFile != null) {
            try {
                mMiniThumbFile.close();
                mMiniThumbFile = null;
            } catch (IOException ex) {
            }
        }
    }

    public synchronized long getMagic(long id) {
        RandomAccessFile r = miniThumbDataFile();
        if (r != null) {
            long pos = id * BYTES_PER_MINTHUMB;
            FileLock lock = null;
            try {
                mBuffer.clear();
                mBuffer.limit(1 + 8);
                lock = mChannel.lock(pos, 1 + 8, true);
                if (mChannel.read(mBuffer, pos) == 9) {
                    mBuffer.position(0);
                    if (mBuffer.get() == 1) {
                        return mBuffer.getLong();
                    }
                }
            } catch (IOException ex) {
                Log.v(TAG, "Got exception checking file magic: ", ex);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Got exception when reading magic, id = " + id + ", disk full or mount read-only? " + ex.getClass());
            } finally {
                try {
                    if (lock != null) lock.release();
                } catch (IOException ex) {
                }
            }
        }
        return 0;
    }

    public synchronized void saveMiniThumbToFile(byte[] data, long id, long magic) throws IOException {
        RandomAccessFile r = miniThumbDataFile();
        if (r == null) return;
        long pos = id * BYTES_PER_MINTHUMB;
        FileLock lock = null;
        try {
            if (data != null) {
                if (data.length > BYTES_PER_MINTHUMB - HEADER_SIZE) {
                    return;
                }
                mBuffer.clear();
                mBuffer.put((byte) 1);
                mBuffer.putLong(magic);
                mBuffer.putInt(data.length);
                mBuffer.put(data);
                mBuffer.flip();
                lock = mChannel.lock(pos, BYTES_PER_MINTHUMB, false);
                mChannel.write(mBuffer, pos);
            }
        } catch (IOException ex) {
            Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; ", ex);
            throw ex;
        } catch (RuntimeException ex) {
            Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; disk full or mount read-only? " + ex.getClass());
        } finally {
            try {
                if (lock != null) lock.release();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Gallery app can use this method to retrieve mini-thumbnail. Full size
     * images share the same IDs with their corresponding thumbnails.
     *
     * @param id the ID of the image (same of full size image).
     * @param data the buffer to store mini-thumbnail.
     */
    public synchronized byte[] getMiniThumbFromFile(long id, byte[] data) {
        RandomAccessFile r = miniThumbDataFile();
        if (r == null) return null;
        long pos = id * BYTES_PER_MINTHUMB;
        FileLock lock = null;
        try {
            mBuffer.clear();
            lock = mChannel.lock(pos, BYTES_PER_MINTHUMB, true);
            int size = mChannel.read(mBuffer, pos);
            if (size > 1 + 8 + 4) {
                mBuffer.position(0);
                byte flag = mBuffer.get();
                long magic = mBuffer.getLong();
                int length = mBuffer.getInt();
                if (size >= 1 + 8 + 4 + length && data.length >= length) {
                    mBuffer.get(data, 0, length);
                    return data;
                }
            }
        } catch (IOException ex) {
            Log.w(TAG, "got exception when reading thumbnail id=" + id + ", exception: " + ex);
        } catch (RuntimeException ex) {
            Log.e(TAG, "Got exception when reading thumbnail, id = " + id + ", disk full or mount read-only? " + ex.getClass());
        } finally {
            try {
                if (lock != null) lock.release();
            } catch (IOException ex) {
            }
        }
        return null;
    }
}
