package de.vdheide.mp3;

import java.util.*;
import java.net.URL;
import java.util.Enumeration;
import java.io.*;

public class ID3v2 {

    private File file;

    private ID3v2Header header;

    private ID3v2ExtendedHeader extended_header;

    private Vector frames;

    private boolean is_changed = false;

    private boolean use_padding = true;

    private boolean use_crc = true;

    private boolean use_unsynchronization = true;

    private static int _tmpCount = 0;

    /**
   * Provides access to ID3v2 tag. When used with an InputStream, no writes are
   * possible (<code>update</code> will fail with an <code>IOException</code>,
   * so make sure you just read.
   *
   * @param in Input stream to read from. Stream position must be set to
   * beginning of file (i.e. position of ID3v2 tag).
   * @exception IOException If I/O errors occur ID3v2IllegalVersionException If
   *            file contains an IDv2 tag of higher version than
   *            <code>VERSION</code>.<code>REVISION</code>
   * @exception ID3v2WrongCRCException If file contains CRC and this differs
   * from CRC calculated from the frames
   * @exception ID3v2DecompressionException If a decompression error occured
   * while decompressing a compressed frame 
   */
    public ID3v2(InputStream in) throws IOException, ID3v2BadParsingException, ID3v2IllegalVersionException, ID3v2WrongCRCException, ID3v2BadHeaderException, ID3v2DecompressionException {
        try {
            this.file = null;
            try {
                readHeader(in);
            } catch (NoID3v2HeaderException e) {
                header = null;
                extended_header = null;
                frames = null;
                return;
            }
            if (header.hasExtendedHeader()) {
                readExtendedHeader(in);
            } else {
                extended_header = null;
            }
            readFrames(in);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ignored) {
            }
            is_changed = false;
        }
    }

    /**
   * Provides access to <code>file</code>'s ID3v2 tag
   *
   * @param file File to access
   * @exception IOException If I/O errors occur
   * @exception ID3v2IllegalVersionException If file contains an IDv2 tag of
   *            higher version than <code>VERSION</code>.<code>REVISION</code>
   * @exception ID3v2WrongCRCException If file contains CRC and this differs
   * from CRC calculated from the frames
   * @exception ID3v2DecompressionException If a decompression error occured
   * while decompressing a compressed frame
   */
    public ID3v2(File file) throws IOException, ID3v2IllegalVersionException, ID3v2WrongCRCException, ID3v2DecompressionException, ID3v2BadHeaderException, ID3v2BadParsingException {
        this(new FileInputStream(file));
        this.file = file;
    }

    /**
   * ID3v2 version
   */
    public static final byte VERSION = 3;

    /**
   * ID3v2 revision
   */
    public static final byte REVISION = 0;

    /**
   * This method undoes the effect of the unsynchronization scheme
   * by replacing $FF $00 by $FF
   *
   * @param in Array of bytes to be "synchronized"
   * @return Changed array or null if no "synchronization" was necessary
   */
    public static byte[] synchronize(byte[] in) {
        final byte FF = (byte) 0xFF;
        final byte OO = (byte) 0x00;
        boolean did_synch = false;
        byte out[] = new byte[in.length];
        int outpos = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] == FF) {
                if (in[i + 1] == OO) {
                    did_synch = true;
                    out[outpos++] = FF;
                    i++;
                } else out[outpos++] = FF;
            } else out[outpos++] = in[i];
        }
        if (outpos != in.length) {
            byte[] tmp = new byte[outpos];
            System.arraycopy(out, 0, tmp, 0, outpos);
            out = tmp;
        }
        if (did_synch == true) return out; else return null;
    }

    /**
   * Unsynchronizes an array of bytes by replacing $FF 00 with
   * $FF 00 00 and %11111111 111xxxxx with
   * %11111111 00000000 111xxxxx.
   *
   * @param in Array of bytes to be "unsynchronized"
   * @return Changed array or null if no change was necessary
   */
    public static byte[] unsynchronize(byte[] in) {
        byte[] out = new byte[in.length];
        int outpos = 0;
        boolean did_unsync = false;
        for (int i = 0; i < in.length; i++) {
            if (in[i] == -1) {
                if (((i + 1) < in.length) && ((in[i + 1] & 0xff) >= 0xe0 || in[i + 1] == 0)) {
                    byte[] tmp = new byte[out.length + 1];
                    System.arraycopy(out, 0, tmp, 0, outpos);
                    out = tmp;
                    tmp = null;
                    out[outpos++] = -1;
                    out[outpos++] = 0;
                    out[outpos++] = in[i + 1];
                    i++;
                    did_unsync = true;
                } else {
                    out[outpos++] = in[i];
                }
            } else {
                out[outpos++] = in[i];
            }
        }
        if (did_unsync) return out;
        return null;
    }

    /**
   * Enables or disables use of padding (enabled by default)
   *
   * @param use_padding True if padding should be used
   */
    public void setUsePadding(boolean use_padding) {
        if (this.use_padding != use_padding) {
            is_changed = true;
            this.use_padding = use_padding;
        }
    }

    /**
   * @return True if padding is used
   */
    public boolean getUsePadding() {
        return use_padding;
    }

    /**
   * Enables / disables use of CRC
   *
   * @param use_crc True if CRC should be used
   */
    public void setUseCRC(boolean use_crc) {
        if (this.use_crc != use_crc) {
            is_changed = true;
            this.use_crc = use_crc;
        }
    }

    /**
   * @return True if CRC is used
   */
    public boolean getUseCRC() {
        return use_crc;
    }

    /**
   * Enables / disables use of unsynchronization
   *
   * @param use_crc True if unsynchronization should be used
   */
    public void setUseUnsynchronization(boolean use_unsynch) {
        if (this.use_unsynchronization != use_unsynch) {
            is_changed = true;
            this.use_unsynchronization = use_unsynch;
        }
    }

    /**
   * @return True if unsynchronization should be used
   */
    public boolean getUseUnsynchronization() {
        return use_unsynchronization;
    }

    /**
   * Test if file already has an ID3v2 tag
   *
   * @return true if file has IDv2 tag
   */
    public boolean hasTag() {
        if (header == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
   * Get all frames
   *
   * @return <code>Vector</code> of all frames
   * @exception NoID3v2TagException If file does not contain ID3v2 tag
   */
    public Vector getFrames() throws NoID3v2TagException {
        if (frames == null) {
            throw new NoID3v2TagException();
        }
        return frames;
    }

    /**
   * Return all frame with ID <code>id</code>
   *
   * @param id Frame ID
   * @return Requested frames
   * @exception NoID3v2TagException If file does not contain ID3v2Tag
   * @exception ID3v2NoSuchFrameException If file does not contain requested ID3v2 frame
   */
    public Vector getFrame(String id) throws NoID3v2TagException, ID3v2NoSuchFrameException {
        if (frames == null) {
            throw new NoID3v2TagException();
        }
        Vector res = new Vector();
        ID3v2Frame tmp;
        for (Enumeration e = frames.elements(); e.hasMoreElements(); ) {
            tmp = (ID3v2Frame) e.nextElement();
            if (tmp.getID().equals(id)) {
                res.addElement(tmp);
            }
        }
        if (res.size() == 0) {
            throw new ID3v2NoSuchFrameException();
        } else {
            return res;
        }
    }

    /**
   * Add a frame
   *
   * @param frame Frame to add
   */
    public void addFrame(ID3v2Frame frame) {
        if (frames == null) {
            frames = new Vector();
        }
        frames.addElement(frame);
        is_changed = true;
    }

    /**
   * Remove a frame.
   *
   * @param frame Frame to remove
   * @exception NoID3v2TagException If file does not contain ID3v2Tag
   * @exception ID3v2NoSuchFrameException If file does not contain requested ID3v2 frame
   */
    public void removeFrame(ID3v2Frame frame) throws NoID3v2TagException, ID3v2NoSuchFrameException {
        if (frames == null) {
            throw new NoID3v2TagException();
        }
        if (frames.removeElement(frame) == false) {
            throw new ID3v2NoSuchFrameException();
        }
        is_changed = true;
    }

    /**
   * Remove all frames with a given id.
   *
   * @param id ID of frames to remove
   * @exception NoID3v2TagException If file does not contain ID3v2Tag
   * @exception ID3v2NoSuchFrameException If file does not contain requested ID3v2 frame
   */
    public void removeFrame(String id) throws NoID3v2TagException, ID3v2NoSuchFrameException {
        if (frames == null) {
            throw new NoID3v2TagException();
        }
        ID3v2Frame tmp;
        boolean found = false;
        for (Enumeration e = frames.elements(); e.hasMoreElements(); ) {
            tmp = (ID3v2Frame) e.nextElement();
            if (tmp.getID().equals(id)) {
                frames.removeElement(tmp);
                found = true;
            }
        }
        if (found == false) {
            throw new ID3v2NoSuchFrameException();
        }
        is_changed = true;
    }

    /**
   * Remove a spefic frames with a given id. A number is given to identify the frame
   * if more than one frame exists
   *
   * @param id ID of frames to remove
   * @param number Number of frame to remove (the first frame gets number 0)
   * @exception NoID3v2TagException If file does not contain ID3v2Tag
   * @exception ID3v2NoSuchFrameException If file does not contain requested ID3v2 frame
   */
    public void removeFrame(String id, int number) throws NoID3v2TagException, ID3v2NoSuchFrameException {
        if (frames == null) {
            throw new NoID3v2TagException();
        }
        ID3v2Frame tmp;
        int count = 0;
        boolean removed = false;
        for (Enumeration e = frames.elements(); e.hasMoreElements(); ) {
            tmp = (ID3v2Frame) e.nextElement();
            if (tmp.getID().equals(id)) {
                if (count == number) {
                    frames.removeElement(tmp);
                    removed = true;
                } else {
                    count++;
                }
            }
        }
        if (removed == false) {
            throw new ID3v2NoSuchFrameException();
        }
        is_changed = true;
    }

    /**
   * Remove all frames
   */
    public void removeFrames() {
        if (frames != null) {
            frames = new Vector();
        }
    }

    public void update() throws IOException {
        if (!is_changed) return;
        boolean usesUnsynchronization = false;
        byte[] newFramesBytes = convertFramesToArrayOfBytes();
        int crc = 0;
        if (use_crc) {
            java.util.zip.CRC32 crcCalculator = new java.util.zip.CRC32();
            crcCalculator.update(newFramesBytes);
            crc = (int) crcCalculator.getValue();
        }
        ID3v2ExtendedHeader newExtHeader = new ID3v2ExtendedHeader(use_crc, crc, 0);
        byte[] newExtHeaderBytes = newExtHeader.getBytes();
        if (use_unsynchronization) {
            byte[] unsynchExtHeader = unsynchronize(newExtHeaderBytes);
            if (unsynchExtHeader != null) {
                usesUnsynchronization = true;
                newExtHeaderBytes = unsynchExtHeader;
            }
            byte[] unsynchFrames = unsynchronize(newFramesBytes);
            if (unsynchFrames != null) {
                usesUnsynchronization = true;
                newFramesBytes = unsynchFrames;
            }
        }
        int newHeaderLen = newExtHeaderBytes.length + newFramesBytes.length;
        ID3v2Header newHeader = new ID3v2Header(VERSION, REVISION, usesUnsynchronization, true, false, newHeaderLen);
        byte[] newHeaderBytes = newHeader.getBytes();
        int oldHeaderLen = 0;
        if (header == null) {
            oldHeaderLen = 0;
        } else {
            oldHeaderLen = header.getTagSize() + 10;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(newHeaderBytes);
        baos.write(newExtHeaderBytes);
        baos.write(newFramesBytes);
        long contentLen = file.length() - oldHeaderLen;
        long newFileLength = contentLen + newHeaderLen;
        if (use_padding) {
            long padding = 0;
            if (oldHeaderLen > newHeaderLen) {
                padding = oldHeaderLen - newHeaderLen;
            }
            for (long i = 0; i < padding; i++) baos.write(0);
        }
        byte[] finalNewHeader = baos.toByteArray();
        int minBuffSize = Math.abs(finalNewHeader.length - oldHeaderLen);
        int buffSize = Math.max(2048, minBuffSize);
        byte[] buf = new byte[buffSize];
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            long oldPointer = oldHeaderLen == 0 ? 0 : oldHeaderLen - 1;
            long newPointer = 0;
            raf.seek(oldPointer);
            long amountToRead = contentLen;
            if (amountToRead < buffSize) throw new IOException("Bad File");
            int read = raf.read(buf, 0, buffSize);
            amountToRead -= read;
            oldPointer = raf.getFilePointer();
            raf.seek(newPointer);
            raf.write(finalNewHeader);
            newPointer = raf.getFilePointer();
            while (amountToRead > 0) {
                byte[] buf2 = new byte[buffSize];
                raf.seek(oldPointer);
                read = raf.read(buf2, 0, Math.min(buffSize, (int) amountToRead));
                if (read == -1) break;
                oldPointer = raf.getFilePointer();
                amountToRead -= read;
                raf.seek(newPointer);
                raf.write(buf);
                newPointer = raf.getFilePointer();
                buf = buf2;
            }
            raf.seek(newPointer);
            raf.write(buf);
            header = newHeader;
            extended_header = newExtHeader;
            is_changed = false;
        } finally {
            try {
                if (raf != null) raf.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
   * Read ID3v2 header from file <code>in</code>
   */
    private void readHeader(InputStream in) throws NoID3v2HeaderException, ID3v2IllegalVersionException, IOException {
        header = new ID3v2Header(in);
    }

    /**
   * Read extended ID3v2 header from input stream <tt>in</tt>
   *
   * @param in Input stream to read from
   */
    private void readExtendedHeader(InputStream in) throws IOException {
        extended_header = new ID3v2ExtendedHeader(in);
    }

    /**
   * Read ID3v2 frames from stream <tt>in</tt>
   * Stream position must be set to beginning of frames
   *
   * @param in Stream to read from
   *
   */
    private void readFrames(InputStream in) throws IOException, ID3v2BadParsingException, ID3v2BadHeaderException, ID3v2WrongCRCException, ID3v2DecompressionException {
        int bytes_to_read;
        if (extended_header != null) {
            int crc = extended_header.hasCRC() ? 0 : 4;
            bytes_to_read = header.getTagSize() - (extended_header.getSize() + crc) - extended_header.getPaddingSize();
        } else {
            bytes_to_read = header.getTagSize();
        }
        byte[] unsynch_frames_as_byte = null;
        try {
            unsynch_frames_as_byte = new byte[bytes_to_read];
        } catch (NegativeArraySizeException nax) {
            throw new ID3v2BadHeaderException();
        } catch (OutOfMemoryError ome) {
            throw new ID3v2BadParsingException(ome);
        }
        fillBuffer(unsynch_frames_as_byte, in);
        byte[] frames_as_byte = null;
        if (header.getUnsynchronization()) {
            frames_as_byte = synchronize(unsynch_frames_as_byte);
            if (frames_as_byte == null) {
                frames_as_byte = unsynch_frames_as_byte;
            }
        } else {
            frames_as_byte = unsynch_frames_as_byte;
        }
        if (extended_header != null && extended_header.hasCRC() == true) {
            java.util.zip.CRC32 crc_calculator = new java.util.zip.CRC32();
            crc_calculator.update(frames_as_byte);
            int crc = (int) crc_calculator.getValue();
            if ((int) crc != (int) extended_header.getCRC()) {
            }
        }
        frames = new Vector();
        ByteArrayInputStream bis = new ByteArrayInputStream(frames_as_byte);
        ID3v2Frame frame = null;
        boolean cont = true;
        while ((bis.available() > 0) && (cont == true)) {
            try {
                frame = new ID3v2Frame(bis);
            } catch (ID3v2BadFrameException e) {
                continue;
            }
            if (frame.getID() == ID3v2Frame.ID_INVALID) {
                cont = false;
            } else {
                frames.addElement(frame);
            }
        }
    }

    /**
   * Convert all frames to an array of bytes
   */
    private byte[] convertFramesToArrayOfBytes() {
        ID3v2Frame tmp = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream(500);
        for (Enumeration e = frames.elements(); e.hasMoreElements(); ) {
            tmp = (ID3v2Frame) e.nextElement();
            byte frame_in_bytes[] = tmp.getBytes();
            out.write(frame_in_bytes, 0, frame_in_bytes.length);
        }
        return out.toByteArray();
    }

    public static void fillBuffer(byte[] buffer, InputStream in) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = in.read(buffer, offset, buffer.length - offset);
            if (read == -1) {
                throw new IOException("eof");
            } else if (read == 0) {
                throw new IOException("can't read");
            } else {
                offset += read;
            }
        }
    }

    /**
     * Skips however much data was padded for the given length.
     */
    public static void skipData(int length, InputStream in) throws IOException {
        long skipped = 0;
        while (skipped < length) {
            long current = in.skip(length - skipped);
            if (current == -1) throw new IOException("eof"); else if (current == 0) throw new IOException("can't read"); else skipped += current;
        }
    }
}
