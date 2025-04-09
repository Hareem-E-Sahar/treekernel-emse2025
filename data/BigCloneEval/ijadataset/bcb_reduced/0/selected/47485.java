package net.hawk.digiextractor.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import net.hawk.digiextractor.GUI.Configuration;

/**
 * The Class DebugWriter.
 * Implements a writer for dump files. Use the singleton pattern, since we want
 * to have only one instance
 */
public final class DebugWriter {

    /** The instance. */
    private static DebugWriter instance = new DebugWriter();

    /** The zipfile. */
    private ZipOutputStream zipfile;

    /** The date str. */
    private String dateStr;

    /**
	 * Instantiates a new debug writer.
	 */
    private DebugWriter() {
        if (Configuration.getInstance().getDump()) {
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            dateStr = formatter.format(date);
            File f = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "DigiExtractor_dump" + dateStr + ".zip");
            try {
                zipfile = new ZipOutputStream(new FileOutputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * Gets the single instance of DebugWriter.
	 * 
	 * @return single instance of DebugWriter
	 */
    public static DebugWriter getInstance() {
        return instance;
    }

    /**
	 * Dump a given ByteBuffer into a file, very useful for debugging purpose.
	 * 
	 * @param byteBuffer the Buffer to be dumped.
	 * @param filename the name of the file.
	 */
    public void dumpBuffer(final ByteBuffer byteBuffer, final String filename) {
        try {
            byte[] array = new byte[byteBuffer.capacity()];
            byteBuffer.get(array);
            byteBuffer.rewind();
            try {
                zipfile.putNextEntry(new ZipEntry("dump" + dateStr + "/" + filename));
                zipfile.write(array);
                zipfile.closeEntry();
            } catch (ZipException e) {
                e.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (zipfile != null && Configuration.getInstance().getDump()) {
                zipfile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.finalize();
        }
    }
}
