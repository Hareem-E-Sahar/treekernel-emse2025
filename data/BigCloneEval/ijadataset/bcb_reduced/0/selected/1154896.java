package org.xiph.speex.spi;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.SequenceInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import org.xiph.speex.OggCrc;

/**
 * Provider for Speex audio file reading services.
 * This implementation can parse the format information from Speex audio file,
 * and can produce audio input streams from files of this type.
 * 
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class SpeexAudioFileReader extends AudioFileReader {

    /** */
    public static final int OGG_HEADERSIZE = 27;

    /** The size of the Speex header. */
    public static final int SPEEX_HEADERSIZE = 80;

    /** */
    public static final int SEGOFFSET = 26;

    /** The String that identifies the beginning of an Ogg packet. */
    public static final String OGGID = "OggS";

    /** The String that identifies the beginning of the Speex header. */
    public static final String SPEEXID = "Speex   ";

    /**
   * Obtains the audio file format of the File provided.
   * The File must point to valid audio file data.
   * @param file the File from which file format information should be
   * extracted.
   * @return an AudioFileFormat object describing the audio file format.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    public AudioFileFormat getAudioFileFormat(final File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return getAudioFileFormat(inputStream, (int) file.length());
        } finally {
            inputStream.close();
        }
    }

    /**
   * Obtains an audio input stream from the URL provided.
   * The URL must point to valid audio file data.
   * @param url the URL for which the AudioInputStream should be constructed.
   * @return an AudioInputStream object based on the audio file data pointed to
   * by the URL.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    public AudioFileFormat getAudioFileFormat(final URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = url.openStream();
        try {
            return getAudioFileFormat(inputStream);
        } finally {
            inputStream.close();
        }
    }

    /**
   * Obtains an audio input stream from the input stream provided.
   * @param stream the input stream from which the AudioInputStream should be
   * constructed.
   * @return an AudioInputStream object based on the audio file data contained
   * in the input stream.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    public AudioFileFormat getAudioFileFormat(final InputStream stream) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(stream, AudioSystem.NOT_SPECIFIED);
    }

    /**
   * Return the AudioFileFormat from the given InputStream.
   * @param stream the input stream from which the AudioInputStream should be
   * constructed.
   * @param medialength
   * @return an AudioInputStream object based on the audio file data contained
   * in the input stream.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    protected AudioFileFormat getAudioFileFormat(final InputStream stream, final int medialength) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(stream, null, medialength);
    }

    /**
   * Return the AudioFileFormat from the given InputStream. Implementation.
   * @param bitStream
   * @param baos
   * @param mediaLength
   * @return an AudioInputStream object based on the audio file data contained
   * in the input stream.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    protected AudioFileFormat getAudioFileFormat(final InputStream bitStream, ByteArrayOutputStream baos, final int mediaLength) throws UnsupportedAudioFileException, IOException {
        AudioFormat format;
        try {
            if (bitStream.markSupported()) {
                bitStream.mark(3 * OGG_HEADERSIZE + SPEEX_HEADERSIZE + 256 + 256 + 2);
            }
            int mode = -1;
            int sampleRate = 0;
            int channels = 0;
            int frameSize = AudioSystem.NOT_SPECIFIED;
            float frameRate = AudioSystem.NOT_SPECIFIED;
            byte[] header = new byte[128];
            int segments = 0;
            int bodybytes = 0;
            DataInputStream dis = new DataInputStream(bitStream);
            if (baos == null) baos = new ByteArrayOutputStream(128);
            int origchksum;
            int chksum;
            dis.readFully(header, 0, OGG_HEADERSIZE);
            baos.write(header, 0, OGG_HEADERSIZE);
            origchksum = readInt(header, 22);
            header[22] = 0;
            header[23] = 0;
            header[24] = 0;
            header[25] = 0;
            chksum = OggCrc.checksum(0, header, 0, OGG_HEADERSIZE);
            if (!OGGID.equals(new String(header, 0, 4))) {
                throw new UnsupportedAudioFileException("missing ogg id!");
            }
            segments = header[SEGOFFSET] & 0xFF;
            if (segments > 1) {
                throw new UnsupportedAudioFileException("Corrupt Speex Header: more than 1 segments");
            }
            dis.readFully(header, OGG_HEADERSIZE, segments);
            baos.write(header, OGG_HEADERSIZE, segments);
            chksum = OggCrc.checksum(chksum, header, OGG_HEADERSIZE, segments);
            bodybytes = header[OGG_HEADERSIZE] & 0xFF;
            if (bodybytes != SPEEX_HEADERSIZE) {
                throw new UnsupportedAudioFileException("Corrupt Speex Header: size=" + bodybytes);
            }
            dis.readFully(header, OGG_HEADERSIZE + 1, bodybytes);
            baos.write(header, OGG_HEADERSIZE + 1, bodybytes);
            chksum = OggCrc.checksum(chksum, header, OGG_HEADERSIZE + 1, bodybytes);
            if (!SPEEXID.equals(new String(header, OGG_HEADERSIZE + 1, 8))) {
                throw new UnsupportedAudioFileException("Corrupt Speex Header: missing Speex ID");
            }
            mode = readInt(header, OGG_HEADERSIZE + 1 + 40);
            sampleRate = readInt(header, OGG_HEADERSIZE + 1 + 36);
            channels = readInt(header, OGG_HEADERSIZE + 1 + 48);
            int nframes = readInt(header, OGG_HEADERSIZE + 1 + 64);
            boolean vbr = readInt(header, OGG_HEADERSIZE + 1 + 60) == 1;
            if (chksum != origchksum) throw new IOException("Ogg CheckSums do not match");
            if (!vbr) {
            }
            if (mode >= 0 && mode <= 2 && nframes > 0) {
                frameRate = ((float) sampleRate) / ((mode == 0 ? 160f : (mode == 1 ? 320f : 640f)) * ((float) nframes));
            }
            format = new AudioFormat(SpeexEncoding.SPEEX, (float) sampleRate, AudioSystem.NOT_SPECIFIED, channels, frameSize, frameRate, false);
        } catch (UnsupportedAudioFileException e) {
            if (bitStream.markSupported()) {
                bitStream.reset();
            }
            throw e;
        } catch (IOException ioe) {
            if (bitStream.markSupported()) {
                bitStream.reset();
            }
            throw new UnsupportedAudioFileException(ioe.getMessage());
        }
        return new AudioFileFormat(SpeexFileFormatType.SPEEX, format, AudioSystem.NOT_SPECIFIED);
    }

    /**
   * Obtains an audio input stream from the File provided.
   * The File must point to valid audio file data.
   * @param file the File for which the AudioInputStream should be constructed.
   * @return an AudioInputStream object based on the audio file data pointed to
   * by the File.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    public AudioInputStream getAudioInputStream(final File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = new FileInputStream(file);
        try {
            return getAudioInputStream(inputStream, (int) file.length());
        } catch (UnsupportedAudioFileException e) {
            inputStream.close();
            throw e;
        } catch (IOException e) {
            inputStream.close();
            throw e;
        }
    }

    /**
   * Obtains an audio input stream from the URL provided.
   * The URL must point to valid audio file data.
   * @param url the URL for which the AudioInputStream should be constructed.
   * @return an AudioInputStream object based on the audio file data pointed to
   * by the URL.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    public AudioInputStream getAudioInputStream(final URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = url.openStream();
        try {
            return getAudioInputStream(inputStream);
        } catch (UnsupportedAudioFileException e) {
            inputStream.close();
            throw e;
        } catch (IOException e) {
            inputStream.close();
            throw e;
        }
    }

    /**
   * Obtains an audio input stream from the input stream provided.
   * The stream must point to valid audio file data.
   * @param stream the input stream from which the AudioInputStream should be
   * constructed.
   * @return an AudioInputStream object based on the audio file data contained
   * in the input stream.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    public AudioInputStream getAudioInputStream(final InputStream stream) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(stream, AudioSystem.NOT_SPECIFIED);
    }

    /**
   * Obtains an audio input stream from the input stream provided.
   * The stream must point to valid audio file data.
   * @param inputStream the input stream from which the AudioInputStream should
   * be constructed.
   * @param medialength
   * @return an AudioInputStream object based on the audio file data contained
   * in the input stream.
   * @exception UnsupportedAudioFileException if the File does not point to
   * a valid audio file data recognized by the system.
   * @exception IOException if an I/O exception occurs.
   */
    protected AudioInputStream getAudioInputStream(final InputStream inputStream, final int medialength) throws UnsupportedAudioFileException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        AudioFileFormat audioFileFormat = getAudioFileFormat(inputStream, baos, medialength);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SequenceInputStream sequenceInputStream = new SequenceInputStream(bais, inputStream);
        return new AudioInputStream(sequenceInputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());
    }

    /**
   * Converts Little Endian (Windows) bytes to an int (Java uses Big Endian).
   * @param data the data to read.
   * @param offset the offset from which to start reading.
   * @return the integer value of the reassembled bytes.
   */
    private static int readInt(final byte[] data, final int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8) | ((data[offset + 2] & 0xff) << 16) | (data[offset + 3] << 24);
    }
}
