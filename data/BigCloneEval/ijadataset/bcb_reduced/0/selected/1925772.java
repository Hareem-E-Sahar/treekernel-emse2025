package com.google.gwt.dev.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Wrapper around a {@link DiskCache} token that allows easy serialization.
 */
public class DiskCacheToken implements Serializable {

    private transient DiskCache diskCache;

    private transient long token;

    /**
   * Create a wrapper for a token associated with {@link DiskCache#INSTANCE}.
   */
    public DiskCacheToken(long token) {
        this(DiskCache.INSTANCE, token);
    }

    /**
   * Create a wrapper for a token associated with the given diskCache.
   */
    DiskCacheToken(DiskCache diskCache, long token) {
        assert token >= 0;
        this.diskCache = diskCache;
        this.token = token;
    }

    /**
   * Retrieve the underlying bytes.
   * 
   * @return the bytes that were written
   */
    public synchronized byte[] readByteArray() {
        return diskCache.readByteArray(token);
    }

    /**
   * Deserialize the underlying bytes as an object.
   * 
   * @param <T> the type of the object to deserialize
   * @param type the type of the object to deserialize
   * @return the deserialized object
   */
    public <T> T readObject(Class<T> type) {
        return diskCache.readObject(token, type);
    }

    /**
   * Read the underlying bytes as a String.
   * 
   * @return the String that was written
   */
    public String readString() {
        return diskCache.readString(token);
    }

    /**
   * Writes the underlying bytes into the specified output stream.
   * 
   * @param out the stream to write into
   */
    public synchronized void transferToStream(OutputStream out) {
        diskCache.transferToStream(token, out);
    }

    private void readObject(ObjectInputStream inputStream) {
        diskCache = DiskCache.INSTANCE;
        token = diskCache.transferFromStream(inputStream);
    }

    private void writeObject(ObjectOutputStream outputStream) {
        diskCache.transferToStream(token, outputStream);
    }
}
