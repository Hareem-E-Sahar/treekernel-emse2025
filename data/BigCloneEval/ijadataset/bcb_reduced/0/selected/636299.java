package de.wilanthaou.songbookcreator.io;

import de.wilanthaou.songbookcreator.model.Playlist;
import de.wilanthaou.songbookcreator.model.Song;
import de.wilanthaou.songbookcreator.model.Songbook;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Provides methods to write {@link Song}s and {@link Songbook}s
 * to {@link OutputStream}s.
 * @author Alexander Metzner
 * @version $Revision: 1.9 $
 * @since 1.0
 */
public class SongWriter {

    private static final Log LOG = LogFactory.getLog(SongWriter.class);

    private static DocumentFactory df = DocumentFactory.getInstance();

    private Charset charset = Charset.forName(IOConstants.DEFAULT_CHARSET_NAME);

    /**
     * Saves the given song to the given output stream.
     * @param song the song
     * @param os the output stream
     * @throws IOException on i/o errors
     */
    public void saveSong(Song song, OutputStream os) throws IOException {
        saveSong(song, os, true);
    }

    /**
     * Saves the given song to the given output stream.
     * @param song the song
     * @param os the output stream
     * @param close close the stream
     * @throws IOException on i/o errors
     */
    public void saveSong(Song song, OutputStream os, boolean close) throws IOException {
        LOG.debug("Saving song " + song);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, charset));
        saveSong(song, pw, close);
    }

    /**
     * Saves the given song.
     * @param song the song to save
     * @param writer the writer
     * @param close close the writer after writing the song?
     * @throws IOException on i/o errors
     */
    public void saveSong(Song song, Writer writer, boolean close) throws IOException {
        LOG.debug("Saving song " + song);
        writer.write(song.getSourceCode());
        writer.flush();
        if (close) {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Saves the given song to the given file.
     * @param song the song
     * @param file the file
     * @throws IOException on i/o errors
     */
    public void saveSong(Song song, File file) throws IOException {
        saveSong(song, new FileOutputStream(file), true);
    }

    /**
     * Saves the given songbook as a directory
     * @param songbook the songbook
     * @param directory the directory
     * @throws IOException on i/o errors
     */
    public void saveSongbookAsDirectory(Songbook songbook, File directory) throws IOException {
        if (!directory.exists()) {
            directory.mkdir();
        }
        File idx = new File(directory.getAbsolutePath() + File.separator + IOConstants.METADATA_DIRECTORY + File.separator + IOConstants.INDEX_FILE_NAME);
        idx.getParentFile().mkdir();
        FileOutputStream fos = new FileOutputStream(idx);
        writeIndex(songbook, fos);
        IOUtils.closeQuietly(fos);
        File pl = new File(directory.getAbsolutePath() + File.separator + IOConstants.METADATA_DIRECTORY + File.separator + IOConstants.PLAYLISTS_FILE_NAME);
        fos = new FileOutputStream(pl);
        writePlaylists(songbook, fos);
        IOUtils.closeQuietly(fos);
        for (Song s : songbook.getSongs()) {
            File f = new File(directory.getAbsolutePath() + File.separator + convertToFileName(s));
            if (f.exists()) {
                f = new File(directory.getAbsolutePath() + File.separator + convertToFileName(s) + System.currentTimeMillis());
            }
            saveSong(s, f);
        }
    }

    /**
     * Writes all playlists.
     * @param songbook the songbook to save
     * @param os the output stream
     * @throws IOException on i/o errors
     */
    private void writePlaylists(Songbook songbook, OutputStream os) throws IOException {
        Element root = createElement(IOConstants.PL_ROOT_TAG, null);
        for (Playlist l : songbook.getPlaylists()) {
            Element list = createElement(IOConstants.PLAYLIST_TAG, null);
            list.add(createElement(IOConstants.TITLE_TAG, l.getTitle()));
            list.add(createElement(IOConstants.DESCRIPTION_TAG, l.getDescription()));
            for (Song s : l) {
                Element se = createElement(IOConstants.SONG_TAG, null);
                se.add(df.createAttribute(se, IOConstants.TITLE_ATTR, s.getTitle()));
                se.add(df.createAttribute(se, IOConstants.SUBTITLE_ATTR, s.getSubtitle()));
                list.add(se);
            }
            root.add(list);
        }
        writeElementAsDoc(root, os);
    }

    /**
     * Saves the given song book as a zip file.
     * @param songbook the songbook
     * @param songbookfile the file
     * @throws IOException
     */
    public void saveSongbookAsZip(Songbook songbook, File songbookfile) throws IOException {
        FileOutputStream fos = new FileOutputStream(songbookfile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        zos.setComment("LeadSheetMaker Songbook file. Saved on " + new Date().toString() + ".");
        ZipEntry e = new ZipEntry(IOConstants.METADATA_DIRECTORY + "/" + IOConstants.INDEX_FILE_NAME);
        zos.putNextEntry(e);
        writeIndex(songbook, zos);
        zos.closeEntry();
        ZipEntry ple = new ZipEntry(IOConstants.METADATA_DIRECTORY + "/" + IOConstants.PLAYLISTS_FILE_NAME);
        zos.putNextEntry(ple);
        writePlaylists(songbook, zos);
        zos.closeEntry();
        for (Song s : songbook.getSongs()) {
            e = new ZipEntry(convertToFileName(s));
            try {
                zos.putNextEntry(e);
            } catch (ZipException e1) {
                e = new ZipEntry(convertToFileName(s) + System.currentTimeMillis());
                zos.putNextEntry(e);
            }
            saveSong(s, zos, false);
            zos.closeEntry();
        }
        IOUtils.closeQuietly(zos);
    }

    /**
     * Saves the index data to the given output stream.
     * @param songbook the songbook
     * @param os the output stream
     * @throws IOException on i/o errors
     */
    private void writeIndex(Songbook songbook, OutputStream os) throws IOException {
        Element root = df.createElement(IOConstants.ROOT_TAG);
        root.add(createElement(IOConstants.TITLE_TAG, songbook.getTitle()));
        root.add(createElement(IOConstants.ASSEMBLER_TAG, songbook.getAssembler()));
        root.add(createElement(IOConstants.DESCRIPTION_TAG, songbook.getDescription()));
        writeElementAsDoc(root, os);
    }

    /**
     * Writes a document with the given root element to the given outputstream.
     * @param root the root element
     * @param os the outputstream
     * @throws IOException on i/o errors
     */
    private void writeElementAsDoc(Element root, final OutputStream os) throws IOException {
        Document d = df.createDocument(root);
        OutputFormat of = new OutputFormat();
        of.setIndent(true);
        of.setIndentSize(2);
        of.setNewlines(true);
        XMLWriter writer = new XMLWriter(os, of);
        writer.write(d);
    }

    /**
     * Creates an XML element.
     * @param name the element's name (the tag name)
     * @param value the text value
     * @return the element
     */
    private Element createElement(String name, String value) {
        Element ret = df.createElement(name);
        if (!StringUtils.isBlank(value)) {
            ret.setText(value);
        }
        return ret;
    }

    /**
     * Converts a song title to a filename.
     * @param song the song
     * @return the filename
     */
    private String convertToFileName(Song song) {
        String ret = song.getTitle();
        ret = ret.replaceAll("[^a-zA-Z0-9_-]", "");
        ret += ("." + IOConstants.SONG_FILE_EXTENSION);
        return ret;
    }
}
