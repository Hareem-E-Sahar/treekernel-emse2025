package net.didion.jwnl.princeton.file;

import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.file.DictionaryFile;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files named with Princeton's dictionary file naming convention.
 * Uses java.nio.channels.FileChannel for file access.
 */
public class PrincetonChannelDictionaryFile extends AbstractPrincetonRandomAccessDictionaryFile {

    /** The random-access file. */
    private CharBuffer _buffer = null;

    private FileChannel _channel = null;

    public PrincetonChannelDictionaryFile() {
    }

    public DictionaryFile newInstance(String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonChannelDictionaryFile(path, pos, fileType);
    }

    public PrincetonChannelDictionaryFile(String path, POS pos, DictionaryFileType fileType) {
        super(path, pos, fileType);
    }

    public String readLine() throws IOException {
        if (isOpen()) {
            StringBuffer input = new StringBuffer();
            char c = (char) -1;
            boolean eol = false;
            while (!eol) {
                c = _buffer.get((int) getFilePointer());
                _buffer.position((int) getFilePointer() + 1);
                switch(c) {
                    case (char) -1:
                    case '\n':
                        eol = true;
                        break;
                    case '\r':
                        eol = true;
                        if ((_buffer.get((int) getFilePointer() + 1)) == '\n') _buffer.position((int) getFilePointer() + 1);
                        break;
                    default:
                        input.append(c);
                        break;
                }
            }
            return ((c == -1) && (input.length() == 0)) ? null : input.toString();
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public void seek(long pos) throws IOException {
        _buffer.position((int) pos);
    }

    public long getFilePointer() throws IOException {
        return (long) _buffer.position();
    }

    public boolean isOpen() {
        return _channel != null;
    }

    public void close() {
        try {
            _buffer = null;
            _channel.close();
        } catch (IOException ex) {
        } finally {
            _channel = null;
        }
    }

    protected void openFile(File file) throws IOException {
        _channel = new FileInputStream(file).getChannel();
        _buffer = Charset.forName("ISO-8859-15").newDecoder().decode(_channel.map(FileChannel.MapMode.READ_ONLY, 0, _channel.size()));
    }

    public long length() throws IOException {
        return _channel.size();
    }

    public int read() throws IOException {
        return (int) _buffer.get();
    }
}
