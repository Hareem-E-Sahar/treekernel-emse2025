package org.bitdrive.file.movie.themoviedb.impl;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

/**
 * Hash code is based on Media Player Classic. In natural language it calculates: size + 64bit
 * checksum of the first and last 64k (even if they overlap because the file is smaller than
 * 128k).
 */
public class OpenSubtitlesHasher {

    /**
         * Size of the chunks that will be hashed in bytes (64 KB)
         */
    private static final int HASH_CHUNK_SIZE = 64 * 1024;

    public static String computeHash(File file) throws IOException {
        long size = file.length();
        long chunkSizeForFile = Math.min(HASH_CHUNK_SIZE, size);
        FileChannel fileChannel = new FileInputStream(file).getChannel();
        try {
            long head = computeHashForChunk(fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, chunkSizeForFile));
            long tail = computeHashForChunk(fileChannel.map(FileChannel.MapMode.READ_ONLY, Math.max(size - HASH_CHUNK_SIZE, 0), chunkSizeForFile));
            return String.format("%016x", size + head + tail);
        } finally {
            fileChannel.close();
        }
    }

    public static String computeHash(InputStream stream, long length) throws IOException {
        int chunkSizeForFile = (int) Math.min(HASH_CHUNK_SIZE, length);
        byte[] chunkBytes = new byte[(int) Math.min(2 * HASH_CHUNK_SIZE, length)];
        DataInputStream in = new DataInputStream(stream);
        in.readFully(chunkBytes, 0, chunkSizeForFile);
        long position = chunkSizeForFile;
        long tailChunkPosition = length - chunkSizeForFile;
        while (position < tailChunkPosition && (position += in.skip(tailChunkPosition - position)) >= 0) ;
        in.readFully(chunkBytes, chunkSizeForFile, chunkBytes.length - chunkSizeForFile);
        long head = computeHashForChunk(ByteBuffer.wrap(chunkBytes, 0, chunkSizeForFile));
        long tail = computeHashForChunk(ByteBuffer.wrap(chunkBytes, chunkBytes.length - chunkSizeForFile, chunkSizeForFile));
        return String.format("%016x", length + head + tail);
    }

    private static long computeHashForChunk(ByteBuffer buffer) {
        LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
        long hash = 0;
        while (longBuffer.hasRemaining()) {
            hash += longBuffer.get();
        }
        return hash;
    }
}
