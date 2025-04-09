package net.sf.gateway.mef.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * Utility methods for working with Zip files.
 */
public final class ZipUtils {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private ZipUtils() {
    }

    /**
     * Checks if the byte array contains a zip file.
     * 
     * @param rawZipFile
     *        the zip file as a byte[]
     * @return true if rawZipFile contains the magic number.
     */
    public static boolean isZipFile(byte[] rawZipFile) {
        if (rawZipFile == null) {
            return false;
        }
        InputStream bais = new ByteArrayInputStream(rawZipFile);
        ZipInputStream zin = new ZipInputStream(bais);
        ZipEntry ze;
        try {
            ze = zin.getNextEntry();
            ze.getName();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                zin.close();
                bais.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

    /**
     * Create a zip file.
     */
    public static byte[] toRawZipFile(List<ZipEntry> entries, List<byte[]> files) throws IOException {
        if (entries.size() != files.size()) {
            return null;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bytes);
        Iterator<ZipEntry> entriesItr = entries.iterator();
        Iterator<byte[]> filesItr = files.iterator();
        while (entriesItr.hasNext()) {
            byte[] file = filesItr.next();
            ZipEntry entry = entriesItr.next();
            zip.putNextEntry(entry);
            zip.write(file, 0, file.length);
        }
        zip.close();
        return bytes.toByteArray();
    }

    /**
     * Extract the ZipEntries from a zip file.
     */
    public static List<ZipEntry> toZipEntryList(byte[] rawZipFile) throws IOException {
        ArrayList<ZipEntry> entries = new ArrayList<ZipEntry>();
        ByteArrayInputStream bytes = new ByteArrayInputStream(rawZipFile);
        ZipInputStream zip = new ZipInputStream(bytes);
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            entries.add(entry);
            entry = zip.getNextEntry();
        }
        zip.close();
        return entries;
    }

    /**
     * Extract the files from a zip file.
     */
    public static List<byte[]> toByteArrayList(byte[] rawZipFile) throws IOException {
        ArrayList<byte[]> files = new ArrayList<byte[]>();
        ByteArrayInputStream bytes = new ByteArrayInputStream(rawZipFile);
        ZipInputStream zip = new ZipInputStream(bytes);
        int len;
        ByteArrayOutputStream file;
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            file = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while ((len = zip.read(buf, 0, 4096)) != -1) {
                file.write(buf, 0, len);
            }
            files.add(file.toByteArray());
            entry = zip.getNextEntry();
        }
        zip.close();
        return files;
    }

    /**
     * Takes a Map of strings to byte arrays (filenames to files). Creates a zip
     * file and returns it as another byte array
     * 
     * @param files
     *        a map containing strings mapped to files represented as byte
     *        arrays
     * @return a byte array representing the zipped data
     * @throws IOException
     */
    public static byte[] zipFiles(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        Iterator<Entry<String, byte[]>> itr = files.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, byte[]> entry = itr.next();
            ZipEntry zipEntry = new ZipEntry(entry.getKey());
            out.putNextEntry(zipEntry);
            IOUtils.write(entry.getValue(), out);
        }
        out.close();
        byte[] outBytes = dest.toByteArray();
        dest.close();
        return outBytes;
    }

    /**
     * Takes a Map of ZipEntry to byte arrays (zip metadata and file data).
     * Creates a zip file and returns it as another byte array
     * 
     * @param files
     *        a map containing zip metadata and file data.
     * @return a byte array representing the zipped data
     * @throws IOException
     */
    public static byte[] zipEntriesAndFiles(Map<ZipEntry, byte[]> files) throws IOException {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        byte[] data = new byte[2048];
        Iterator<Entry<ZipEntry, byte[]>> itr = files.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<ZipEntry, byte[]> entry = itr.next();
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(entry.getValue());
            BufferedInputStream origin = new BufferedInputStream(bytesIn, 2048);
            out.putNextEntry(entry.getKey());
            int count;
            while ((count = origin.read(data, 0, 2048)) != -1) {
                out.write(data, 0, count);
            }
            bytesIn.close();
            origin.close();
        }
        out.close();
        byte[] outBytes = dest.toByteArray();
        dest.close();
        return outBytes;
    }

    /**
     * Takes a byte array representing a zip file and unzips all the contained
     * files into a map.
     * 
     * @param zipBytes
     *        zip file
     * @return Map<String, byte[]> (unzipped stuff)
     * @throws IOException
     */
    public static Map<String, byte[]> unzipFiles(byte[] zipBytes) throws IOException {
        InputStream bais = new ByteArrayInputStream(zipBytes);
        ZipInputStream zin = new ZipInputStream(bais);
        ZipEntry ze;
        Map<String, byte[]> extractedFiles = new HashMap<String, byte[]>();
        while ((ze = zin.getNextEntry()) != null) {
            byte[] entryBytes = IOUtils.toByteArray(zin);
            extractedFiles.put(ze.getName(), entryBytes);
        }
        zin.close();
        bais.close();
        return extractedFiles;
    }
}
